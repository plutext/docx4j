# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 0: the picture styles (cr081-phase0-styles). No COM call applies a gallery style, so Jason applies the
28 by hand in Word, once each, to 28 copies of build_phase0.py's photo, and saves. That save gives Word's own XML for
each style, and the effect extent Word's interface computes for it; the bridge's save of it gives Word's drawing.

start:   writes the starting document. One style to a page, in the gallery's order (left to right, top to bottom). The
         page's first paragraph names the style; the picture is inline, 144 pt by 108 pt, with no effect and a zero
         effect extent, as Word writes a picture it inserts.
harvest: reads Jason's save (cr081-styles-hand.docx, 2026-11-10) into styles-word.tsv: for each page, the style's id
         and name, Word's effect extent (l t r b, EMU), and the picture's spPr after its xfrm, as Word wrote it.
         build_phase0.py writes that XML over each picture.

usage: build_styles_start.py start <out.docx>
       build_styles_start.py harvest <hand.docx>"""
import csv, os, re, sys, zipfile
import build_phase0 as bp

STYLES = [
    'Simple Frame, White', 'Beveled Matte, White', 'Metal Frame', 'Drop Shadow Rectangle',
    'Reflected Rounded Rectangle', 'Soft Edge Rectangle', 'Double Frame, Black',
    'Thick Matte, Black', 'Simple Frame, Black', 'Beveled Oval, Black', 'Compound Frame, Black',
    'Moderate Frame, Black', 'Center Shadow Rectangle', 'Rounded Diagonal Corner, White',
    'Snip Diagonal Corner, White', 'Moderate Frame, White', 'Rotated, White', 'Perspective Shadow, White',
    'Relaxed Perspective, White', 'Soft Edge Oval', 'Bevel Rectangle',
    'Bevel Perspective', 'Reflected Perspective Right', 'Bevel Perspective Left, White', 'Reflected Bevel, White',
    'Reflected Bevel, Black', 'Metal Rounded Rectangle', 'Metal Oval',
]
assert len(STYLES) == 28

def label(pid, probe, first):
    ppr = '' if first else '<w:pPr><w:pageBreakBefore/></w:pPr>'
    n = int(pid.split('-')[1])
    return (f'<w:p>{ppr}{bp.run(f"{pid}: {probe.note}")}</w:p>'
            f'<w:p>{bp.run(f"Style {n} of 28 in the Picture Styles gallery. Click the picture, then apply this style, and nothing else.")}</w:p>')

def start(out):
    bp.label = label
    probes = [(f'st-{i + 1:02d}', bp.Probe(name)) for i, name in enumerate(STYLES)]
    bp.package(out, probes, bp.THEME_2023)
    print(out, len(probes), 'pages')

XFRM = f'<a:xfrm><a:off x="0" y="0"/><a:ext cx="{bp.CX}" cy="{bp.CY}"/></a:xfrm>'

def harvest(hand):
    doc = zipfile.ZipFile(hand).read('word/document.xml').decode('utf-8')
    rows, cur = [], None
    for p in re.findall(r'<w:p[ >].*?</w:p>', doc, re.S):
        text = ''.join(re.findall(r'<w:t[^>]*>([^<]*)</w:t>', p))
        m = re.match(r'(st-\d\d): (.*)', text)
        if m:
            cur = m.groups()
            continue
        if '<w:drawing>' not in p:
            continue
        d = re.search(r'<w:drawing>(.*?)</w:drawing>', p, re.S).group(1)
        assert d.startswith('<wp:inline') and f'<wp:extent cx="{bp.CX}" cy="{bp.CY}"/>' in d, cur
        ee = re.search(r'<wp:effectExtent l="(\d+)" t="(\d+)" r="(\d+)" b="(\d+)"/>', d).groups()
        sppr = re.search(r'<pic:spPr[^>]*>(.*?)</pic:spPr>', d, re.S).group(1)
        assert sppr.startswith(XFRM), cur              # no style moves, turns or resizes the frame itself
        name = STYLES[int(cur[0][3:]) - 1]
        assert cur[1] == name, (cur, name)
        rows.append({'id': cur[0], 'name': name, 'l': ee[0], 't': ee[1], 'r': ee[2], 'b': ee[3],
                     'sppr': sppr[len(XFRM):]})
        cur = None
    assert [r['id'] for r in rows] == [f'st-{i:02d}' for i in range(1, 29)]
    assert len({r['sppr'] for r in rows}) == 28, 'two pages have the same style'
    with open(bp.STYLES_TSV, 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, ['id', 'name', 'l', 't', 'r', 'b', 'sppr'], delimiter='\t', lineterminator='\n')
        w.writeheader()
        w.writerows(rows)
    print(bp.STYLES_TSV, len(rows), 'styles')

if __name__ == '__main__':
    {'start': start, 'harvest': harvest}[sys.argv[1]](sys.argv[2])
