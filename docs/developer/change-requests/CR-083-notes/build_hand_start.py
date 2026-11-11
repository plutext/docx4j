# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 0: the gallery styles, by hand. No COM call is known to apply a WordArt or Text Effects gallery style (as
none was for CR-081's picture styles), so Jason applies them in Word, once each, and saves. That save gives Word's own
XML for WordArt (its body properties, fromWordArt, autofit, the box Word sizes, the w14 run effects of each style) and
for the same styles on body text; the bridge's save of it gives Word's drawing. build_wordart.py's own form for a
WordArt is a guess until then.

The document, one task to a page, the page's first paragraph naming it:
  ha-01..15  the 15 styles of Insert > WordArt, each made from the word on its page
  ha-16..19  a WordArt (the first style) with a Transform: Follow Path Arch; Warp Square, Inflate, Wave: Down
  ha-20..21  a WordArt (the first style) with 3-D: Bevel Circle; 3-D Rotation, the first Perspective
  hb-01..15  the 15 styles of Home > Text Effects and Typography, on the line on its page

harvest: reads Jason's save (cr083-styles-hand.docx, made 2026-11-12 in Word 365) into styles-word.tsv: for each of
         the 15 styles, the WordArt's run properties and the body run's, as Word wrote them (asserted equal: the two
         galleries are one), and what the style adds to a WordArt's bodyPr (styles 5 and 10: a:scene3d and a:sp3d).
         Also checks the Transform and 3-D pages' warps. build_wordart.py writes the styles over its probes.

usage: build_hand_start.py start <out.docx>
       build_hand_start.py harvest <hand.docx>"""
import csv, importlib.util, os, re, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('wordart', os.path.join(HERE, 'build_wordart.py'))
wa = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(wa)
fx = wa.fx

def pos(i):
    return f'row {i // 5 + 1}, column {i % 5 + 1}'

TASKS = ([(f'ha-{i + 1:02d}', f'WordArt style {i + 1} of 15 ({pos(i)})',
           f'Select the word below (the word only, not its paragraph mark), then Insert > WordArt > the style at {pos(i)} '
           'of the gallery. Nothing else.', 'WordArt') for i in range(15)]
         + [(f'ha-{16 + i:02d}', f'WordArt style 1 with Transform: {name}',
             'Select the word below, Insert > WordArt > the first style (row 1, column 1); then, with the WordArt still '
             f'selected, Shape Format > Text Effects > Transform > {where}. Nothing else.', 'WordArt')
            for i, (name, where) in enumerate([('Arch', 'Follow Path, the first ("Arch")'),
                                               ('Square', 'Warp, the first ("Square")'),
                                               ('Inflate', 'Warp, the one named "Inflate"'),
                                               ('Wave: Down', 'Warp, the one named "Wave: Down"')])]
         + [('ha-20', 'WordArt style 1 with Bevel: Circle',
             'Select the word below, Insert > WordArt > the first style; then Shape Format > Text Effects > Bevel > '
             'the first ("Circle"). Nothing else.', 'WordArt'),
            ('ha-21', 'WordArt style 1 with 3-D Rotation: the first Perspective',
             'Select the word below, Insert > WordArt > the first style; then Shape Format > Text Effects > 3-D Rotation > '
             'the first under Perspective. Nothing else.', 'WordArt')]
         + [(f'hb-{i + 1:02d}', f'Text Effects style {i + 1} of 15 ({pos(i)}), on body text',
             f'Select the line below (its text only), then Home > Text Effects and Typography > the style at {pos(i)}. '
             'Nothing else.', 'Body text effect') for i in range(15)])

def start(out):
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in wa.NS.items())
    parts = []
    for i, (pid, title, how, word) in enumerate(TASKS):
        ppr = '' if i == 0 else '<w:pPr><w:pageBreakBefore/></w:pPr>'
        parts.append(f'<w:p>{ppr}{fx.run(f"{pid}: {title}.")}</w:p><w:p>{fx.run(how)}</w:p>')
        parts.append(wa.para([wa.Run(word)], jc='left', before=720))
    doc = f'{fx.DECL}<w:document {xmlns} mc:Ignorable="w14"><w:body>{"".join(parts)}{fx.SECT}</w:body></w:document>'
    with __import__('zipfile').ZipFile(out, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + fx.CT)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', doc)
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 f'package/2006/relationships"><Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>'
                 f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/></Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
    print(out, len(TASKS), 'pages')

STYLES_TSV = os.path.join(HERE, 'styles-word.tsv')

def harvest(hand):
    d = zipfile.ZipFile(hand).read('word/document.xml').decode('utf-8')
    d = re.sub(r'<mc:Fallback>.*?</mc:Fallback>', '', d, flags=re.S)
    d = re.sub(r' w:rsid\w*="\w+"| w14:(?:paraId|textId)="\w+"', '', d)
    labels = [(m.start(), m.group(1)) for m in re.finditer(r'<w:t[^>]*>((?:ha|hb)-\d\d):', d)]
    assert [n for _, n in labels] == [t[0] for t in TASKS], 'the pages are not the start document\'s'

    def page(pos):
        return [n for p, n in labels if p < pos][-1]

    art, body = {}, {}
    for m in re.finditer(r'<wps:wsp>.*?</wps:wsp>', d, re.S):
        w = m.group(0)
        assert ''.join(re.findall(r'<w:t[^>]*>([^<]*)</w:t>', w)) == 'WordArt'
        rprs = set(re.findall(r'<w:r><w:rPr>(.*?)</w:rPr><w:t', w, re.S))
        assert len(rprs) == 1, page(m.start())
        bp = re.search(r'<wps:bodyPr ([^>]*)>(.*?)</wps:bodyPr>', w, re.S)
        warp = re.search(r'<a:prstTxWarp prst="(\w+)"><a:avLst/></a:prstTxWarp><a:spAutoFit/>', bp.group(2))
        assert warp and 'wrap="none"' in bp.group(1) and 'fromWordArt="0"' in bp.group(1), page(m.start())
        assert re.search(r'<a:ext cx="1828800" cy="1828800"/>', w), page(m.start())
        art[page(m.start())] = (rprs.pop(), warp.group(1), bp.group(2)[warp.end():])
    for m in re.finditer(r'<w:r><w:rPr>((?:(?!</w:rPr>).)*)</w:rPr><w:t[^>]*>Body text effect</w:t>', d, re.S):
        body[page(m.start())] = m.group(1)
    rows = []
    for i in range(1, 16):
        rpr, warp, extra = art[f'ha-{i:02d}']
        assert warp == 'textNoShape', i
        assert body[f'hb-{i:02d}'] == rpr, f'style {i}: the WordArt and the body run differ'
        rows.append({'id': i, 'position': pos(i - 1), 'rpr': rpr, 'bodypr_extra': extra})
    warps = {n: art[n][1] for n in ('ha-16', 'ha-17', 'ha-18', 'ha-19')}
    assert warps == {'ha-16': 'textArchUp', 'ha-17': 'textPlain', 'ha-18': 'textInflate', 'ha-19': 'textWave1'}, warps
    with open(STYLES_TSV, 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, ['id', 'position', 'rpr', 'bodypr_extra'], delimiter='\t', lineterminator='\n')
        w.writeheader()
        w.writerows(rows)
    print(STYLES_TSV, len(rows), 'styles; transforms', warps, '; ha-20 bodyPr', art['ha-20'][2][:60], '; ha-21 bodyPr',
          repr(art['ha-21'][2]) or 'none')

if __name__ == '__main__':
    {'start': start, 'harvest': harvest}[sys.argv[1]](sys.argv[2])
