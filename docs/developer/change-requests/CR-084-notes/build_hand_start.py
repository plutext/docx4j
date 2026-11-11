# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084 phase 0: Word's interface presets, by hand. No COM call applies them through the bridge, so Jason applies each
once in Word to a copy of a calibration picture and saves. That save gives Word's own XML for each preset (the a:blip's
children, or a14:imgProps), and, where Word keeps an original beside an adjusted picture (a14:imgLayer), both
pictures; the bridge's save of it gives Word's drawing. build_recolour.py's sweeps cover the XML the interface doesn't
reach.

The document, one task to a page, the page's first paragraph naming it, the picture inline below the instruction
(calib-colour.png unless the task says otherwise; build_recolour.py's pictures):
  hp-01..20  Picture Format > Color > Recolor: the 20 presets (row 1 after No Recolor; rows 2 and 3)
  hp-21..24  Color Saturation, first and last; Color Tone, first and last
  hp-25..30  Picture Format > Transparency: the six presets after 0%
  hp-31      Color > Set Transparent Color, on calib-near.png's red block
  hp-32..38  Corrections: Soften 50%, Sharpen 50%; Brightness and Contrast at the gallery's four corners and its middle
             right
  hp-39..40  Artistic Effects: Pencil Sketch, Blur
  hp-41      Remove Background, on the photo
  hp-42..43  the EMF: Recolor Grayscale; Transparency 50%

harvest: reads Jason's save (cr084-recolour-hand.docx, made 2026-11-12 in Word 365) into presets-word.tsv: for each
         page, its task, the media its blip points to, the blip's a: children as Word wrote them (its extLst left out),
         the a14:imgEffect children, and the media of an a14:imgLayer. It checks that a page's blip without a14 is
         still the original picture (a: children only), and that one with a14 points to a new picture, the original
         kept as the layer.

usage: build_hand_start.py start <out.docx>
       build_hand_start.py harvest <hand.docx>"""
import csv, importlib.util, os, re, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('recolour', os.path.join(HERE, 'build_recolour.py'))
rc = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(rc)
fx = rc.fx

RECOLOR = (['Grayscale', 'Sepia', 'Washout', 'Black and White: 25%', 'Black and White: 50%', 'Black and White: 75%']
           + [f'Dark variation {i}' for i in range(1, 8)] + [f'Light variation {i}' for i in range(1, 8)])

def recolor_place(i):
    return f'row 1, column {i + 2}' if i < 6 else f'row {2 if i < 13 else 3}, column {(i - 6) % 7 + 1}'

C = 'calib-colour.png'
TASKS = ([(f'Recolor: {n}', f'Picture Format > Color > Recolor, the preset at {recolor_place(i)}'
           + (f' ("{n}").' if i < 6 else '.'), C) for i, n in enumerate(RECOLOR)]
         + [('Color Saturation: the first (0%)', 'Picture Format > Color > Color Saturation, the first preset.', C),
            ('Color Saturation: the last (300%)', 'Picture Format > Color > Color Saturation, the last preset.', C),
            ('Color Tone: the first (4700 K)', 'Picture Format > Color > Color Tone, the first preset.', C),
            ('Color Tone: the last (11200 K)', 'Picture Format > Color > Color Tone, the last preset.', C)]
         + [(f'Transparency: {t}%', f'Picture Format > Transparency, the {t}% preset.', C) for t in (15, 30, 50, 65, 80, 95)]
         + [('Set Transparent Color', 'Picture Format > Color > Set Transparent Color, then click the red block at the '
             "picture's top left.", 'calib-near.png')]
         + [('Corrections: Soften 50%', 'Picture Format > Corrections, Sharpen/Soften: the first preset (Soften: 50%).', C),
            ('Corrections: Sharpen 50%', 'Picture Format > Corrections, Sharpen/Soften: the last preset (Sharpen: 50%).', C)]
         + [(f'Corrections: Brightness {b} Contrast {c}', f'Picture Format > Corrections, Brightness/Contrast: "Brightness: {b} '
             f'Contrast: {c}" ({where}).', C)
            for b, c, where in (('-40%', '-40%', 'top left'), ('+40%', '-40%', 'top right'), ('-40%', '+40%', 'bottom left'),
                                ('+40%', '+40%', 'bottom right'), ('+40%', '0% (Normal)', 'middle row, right'))]
         + [('Artistic Effects: Pencil Sketch', 'Picture Format > Artistic Effects, Pencil Sketch.', C),
            ('Artistic Effects: Blur', 'Picture Format > Artistic Effects, Blur.', C),
            ('Remove Background', 'Picture Format > Remove Background, then Keep Changes (whatever Word marks).', 'photo.png'),
            ('The EMF: Recolor Grayscale', 'Picture Format > Color > Recolor, Grayscale (if Word offers it for this picture; if '
             'not, leave the page as it is).', 'blocks.emf'),
            ('The EMF: Transparency 50%', 'Picture Format > Transparency, the 50% preset (if Word offers it; if not, leave the '
             'page as it is).', 'blocks.emf')])

def inline(picture, n, rid):
    cx, cy = rc.frame(picture)
    return (f'<w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="{cx}" cy="{cy}"/>'
            f'<wp:effectExtent l="0" t="0" r="0" b="0"/><wp:docPr id="{n}" name="Picture {n}"/><wp:cNvGraphicFramePr>'
            '<a:graphicFrameLocks noChangeAspect="1"/></wp:cNvGraphicFramePr>'
            f'<a:graphic><a:graphicData uri="{fx.URI_PIC}"><pic:pic><pic:nvPicPr><pic:cNvPr id="{n}" name="{picture}"/>'
            f'<pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="{rid}"/><a:stretch><a:fillRect/></a:stretch>'
            f'</pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm>'
            '<a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic>'
            '</wp:inline></w:drawing></w:r>')

def start(out):
    pics = sorted({t[2] for t in TASKS})
    rids = {name: f'rId{10 + i}' for i, name in enumerate(pics)}
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in rc.NS.items())
    parts = []
    for i, (title, how, pic) in enumerate(TASKS):
        ppr = '' if i == 0 else '<w:pPr><w:pageBreakBefore/></w:pPr>'
        pid = f'hp-{i + 1:02d}'
        parts.append(f'<w:p>{ppr}{fx.run(f"{pid}: {title}.")}</w:p><w:p>{fx.run(how + " Click the picture first. Nothing else.")}</w:p>'
                     f'<w:p><w:pPr><w:spacing w:before="240"/></w:pPr>{inline(pic, i + 1, rids[pic])}</w:p>')
    doc = f'{fx.DECL}<w:document {xmlns}><w:body>{"".join(parts)}{fx.SECT}</w:body></w:document>'
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>']
    rels += [f'<Relationship Id="{rids[n]}" Type="{fx.REL}image" Target="media/{n}"/>' for n in pics]
    ct = fx.CT.replace('<Override ', '<Default Extension="emf" ContentType="image/x-emf"/><Override ', 1)
    with zipfile.ZipFile(out, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + ct)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', doc)
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
        for n in pics:
            fx.entry(z, f'word/media/{n}', rc.PICTURES[n]())
    print(out, len(TASKS), 'pages')

PRESETS_TSV = os.path.join(HERE, 'presets-word.tsv')

def harvest(hand):
    z = zipfile.ZipFile(hand)
    d = z.read('word/document.xml').decode('utf-8')
    rels = {m.group(1): m.group(2).split('/')[-1] for m in
            re.finditer(r'<Relationship [^>]*?Id="(rId\d+)"[^>]*?Target="([^"]+)"', z.read('word/_rels/document.xml.rels').decode())}
    rels.update({m.group(2): m.group(1).split('/')[-1] for m in
                 re.finditer(r'<Relationship [^>]*?Target="([^"]+)"[^>]*?Id="(rId\d+)"', z.read('word/_rels/document.xml.rels').decode())})
    labels = [(m.start(), m.group(1)) for m in re.finditer(r'<w:t[^>]*>(hp-\d\d): ', d)]
    assert len(labels) == len(TASKS)
    rows = []
    for m in re.finditer(r'<pic:pic\b.*?</pic:pic>', d, re.S):
        pid = [n for p, n in labels if p < m.start()][-1]
        blip = re.search(r'<a:blip\b[^>]*?(?:/>|>(.*?)</a:blip>)', m.group(0), re.S)
        media = rels[re.search(r'r:embed="(rId\d+)"', blip.group(0)).group(1)]
        inner = blip.group(1) or ''
        layer = re.search(r'<a14:imgLayer r:embed="(rId\d+)">(.*?)</a14:imgLayer>', inner, re.S)
        effects = ' '.join(re.findall(r'<a14:imgEffect>(.*?)</a14:imgEffect>', inner, re.S))
        children = re.sub(r'<a:extLst>.*</a:extLst>', '', inner, flags=re.S)
        task = TASKS[int(pid[3:]) - 1]
        if layer:
            assert media != rels[layer.group(1)], pid
        rows.append({'id': pid, 'task': task[0], 'picture': task[2], 'media': media, 'a': children, 'a14': effects,
                     'layer': rels[layer.group(1)] if layer else ''})
    with open(PRESETS_TSV, 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, ['id', 'task', 'picture', 'media', 'a', 'a14', 'layer'], delimiter='\t', lineterminator='\n')
        w.writeheader()
        w.writerows(rows)
    print(PRESETS_TSV, len(rows), 'pictures')

if __name__ == '__main__':
    {'start': start, 'harvest': harvest}[sys.argv[1]](sys.argv[2])
