# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Text on both sides of an object, the fourth round: a paragraph that starts with a tab or an inline picture beside a
gap too narrow to use. The FOP fork's CR-024 section 13.9 item 7 left it unmeasured (r80 keeps the old restart for
a paragraph opening with a tab or an inline object), and Jason ordered it: "measure what Word does with a paragraph
that starts with a tab or an inline picture beside a too-narrow gap".

fb10's geometry (build_bothsides.py, build_bothsides2.py): A4, 1in margins (the column x 72 to 523.3), Arial 10pt
single, Word's default tab stops (every 36pt from the margin); the object a rectangle shape 150 x 120pt, square on both
sides, distL and distR 9pt, distT and distB 0, anchored in its paragraph 12pt below its top. The anchor paragraph holds
fb10's 12 words (a line, then part of the first band line) and the case's paragraph B follows it, starting in the band.
A narrow gap is 16pt (under Word's 18pt minimum, fb20), so the segment on that side isn't used; the other is 267.3pt.

  W1   the left gap 16pt; B starts with a word (the control: the skip rule, fb10)
  K1   the left gap 16pt; B starts with a tab (the default stops)
  K2   the left gap 16pt; B with a left tab stop at 226.8pt (as a label's tab), starts with a tab, then "label : value"
  K3   the left gap 16pt; B with a left tab stop at 120pt (under the object), starts with a tab
  K4   the left gap 16pt; B starts with two tabs (the default stops)
  K5   the left gap 16pt; B with a right tab stop at the column's end (451.3pt), starts with a tab, then "page 12"
  K6   the left gap 16pt; B with a centre tab stop at the column's middle (225.65pt), starts with a tab
  K7   the left gap 40pt (used); B starts with a tab (the default stops)
  K8   the right gap 16pt (the wide gap on the left); B starts with a tab (the default stops)
  Q1   the left gap 16pt; B starts with an inline picture 60 x 20pt, then words
  Q2   the left gap 16pt; the picture 260pt wide (fits the right segment, 267.3pt)
  Q3   the left gap 16pt; the picture 300pt wide (wider than the segment, narrower than the column)
  Q4   the left gap 16pt; the picture 451.3pt wide (the column's width)
  Q5   the left gap 16pt; the picture 300pt wide alone in B (no words)
  Q6   the left gap 40pt (used); the picture 30pt wide (fits the left gap)
  Q7   the left gap 40pt (used); the picture 60pt wide (wider than the left gap, fits the right)
  Q8   the right gap 16pt (the wide gap on the left); the picture 60pt wide
  Q9   the left gap 16pt; B starts with a tab, then the picture 60pt wide

usage: build_bothsides4.py <staging dir>   (also writes probes-bothsides4.tsv beside this script)"""
import hashlib, importlib.util, io, json, os, sys, zipfile

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('bothsides2', os.path.join(HERE, 'build_bothsides2.py'))
b2 = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(b2)
bs, fx, bw = b2.bs, b2.fx, b2.bw
EMU, COL, OW = b2.EMU, b2.COL, b2.OW

SET = 'fop-cr024-bothsides-r4'
NAME = 'fb50-leading'
NARROW = 16
LEFT_NARROW = NARROW + 9                                   # x of the object: the left gap 16pt
LEFT_USED = 40 + 9                                         # the left gap 40pt
RIGHT_NARROW = COL - NARROW - 9 - OW                       # the right gap 16pt
PIC_REL = 'rId10'
TAB = '<w:r><w:tab/></w:r>'

def tw(pt):
    return round(pt * 20)

def tabs(*stops):
    return '<w:tabs>' + ''.join(f'<w:tab w:val="{v}" w:pos="{tw(p)}"/>' for v, p in stops) + '</w:tabs>'

def picture(n, w, h=20):
    """An inline picture w x h pt (the one bitmap, stretched)"""
    cx, cy = round(w * EMU), round(h * EMU)
    return (f'<w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="{cx}" cy="{cy}"/>'
            f'<wp:effectExtent l="0" t="0" r="0" b="0"/><wp:docPr id="{1000 + n}" name="Picture {1000 + n}"/>'
            f'<wp:cNvGraphicFramePr/><a:graphic><a:graphicData uri="{fx.URI_PIC}"><pic:pic><pic:nvPicPr>'
            f'<pic:cNvPr id="0" name="block.png"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="{PIC_REL}"/>'
            f'<a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/>'
            f'<a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>'
            f'</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>')

def anchor_and_b(b_content, b_ppr=''):
    """fb10's anchor paragraph (12 words), then paragraph B (b_content(n): its runs), then a paragraph of words"""
    def build(n, case):
        return (b2.p(b2.obj(n, case.x, case.dist_l, case.dist_r) + b2.run(bs.words_text(1, 12)))
                + b2.p(b_content(n), b_ppr) + b2.p(b2.run(bs.words_text(200, 40))))
    return build

def words(first=13, count=60):
    return lambda n: b2.run(bs.words_text(first, count))

def tab_then(text, count=1):
    return lambda n: TAB * count + b2.run(text)

def pic_then(w, text=True):
    return lambda n: picture(n, w) + (b2.run(' ' + bs.words_text(13, 60)) if text else '')

def cases():
    C = b2.Case
    after = bs.words_text(13, 60)
    return [
        C('W1', 'left gap 16pt; B starts with a word (the control)', LEFT_NARROW, 9, 9, anchor_and_b(words())),
        C('K1', 'left gap 16pt; B starts with a tab, default stops', LEFT_NARROW, 9, 9, anchor_and_b(tab_then(after))),
        C('K2', 'left gap 16pt; B with a left tab stop at 226.8pt, starts with a tab, then label : value', LEFT_NARROW, 9, 9,
          anchor_and_b(tab_then('Uitgifte : 31 januari 2017 ' + bs.words_text(13, 40)), tabs(('left', 226.8)))),
        C('K3', 'left gap 16pt; B with a left tab stop at 120pt (under the object), starts with a tab', LEFT_NARROW, 9, 9,
          anchor_and_b(tab_then(after), tabs(('left', 120)))),
        C('K4', 'left gap 16pt; B starts with two tabs, default stops', LEFT_NARROW, 9, 9, anchor_and_b(tab_then(after, 2))),
        C('K5', 'left gap 16pt; B with a right tab stop at 451.3pt, starts with a tab, then page 12', LEFT_NARROW, 9, 9,
          anchor_and_b(tab_then('page 12'), tabs(('right', COL)))),
        C('K6', 'left gap 16pt; B with a centre tab stop at 225.65pt, starts with a tab', LEFT_NARROW, 9, 9,
          anchor_and_b(tab_then('centred words here ' + bs.words_text(13, 40)), tabs(('center', COL / 2)))),
        C('K7', 'left gap 40pt (used); B starts with a tab, default stops', LEFT_USED, 9, 9, anchor_and_b(tab_then(after))),
        C('K8', 'right gap 16pt (the wide gap on the left); B starts with a tab, default stops', RIGHT_NARROW, 9, 9,
          anchor_and_b(tab_then(after))),
        C('Q1', 'left gap 16pt; B starts with an inline picture 60 x 20pt, then words', LEFT_NARROW, 9, 9, anchor_and_b(pic_then(60))),
        C('Q2', 'left gap 16pt; B starts with a picture 260pt wide (fits the right segment)', LEFT_NARROW, 9, 9, anchor_and_b(pic_then(260))),
        C('Q3', 'left gap 16pt; B starts with a picture 300pt wide (wider than the segment)', LEFT_NARROW, 9, 9, anchor_and_b(pic_then(300))),
        C('Q4', 'left gap 16pt; B starts with a picture 451.3pt wide (the column)', LEFT_NARROW, 9, 9, anchor_and_b(pic_then(COL))),
        C('Q5', 'left gap 16pt; B is a picture 300pt wide alone', LEFT_NARROW, 9, 9, anchor_and_b(pic_then(300, text=False))),
        C('Q6', 'left gap 40pt (used); B starts with a picture 30pt wide (fits the left gap)', LEFT_USED, 9, 9, anchor_and_b(pic_then(30))),
        C('Q7', 'left gap 40pt (used); B starts with a picture 60pt wide (wider than the left gap)', LEFT_USED, 9, 9, anchor_and_b(pic_then(60))),
        C('Q8', 'right gap 16pt (the wide gap on the left); B starts with a picture 60pt wide', RIGHT_NARROW, 9, 9, anchor_and_b(pic_then(60))),
        C('Q9', 'left gap 16pt; B starts with a tab, then a picture 60pt wide', LEFT_NARROW, 9, 9,
          anchor_and_b(lambda n: TAB + picture(n, 60) + b2.run(' ' + after))),
    ]

def block_png():
    """The picture: a blue block, so that Word's PDF shows it as an image of its own"""
    buf = io.BytesIO()
    Image.new('RGB', (300, 20), (0x44, 0x72, 0xC4)).save(buf, 'PNG')
    return buf.getvalue()

def package(path, cs):
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>',
            f'<Relationship Id="rId3" Type="{fx.REL}styles" Target="styles.xml"/>',
            f'<Relationship Id="{PIC_REL}" Type="{fx.REL}image" Target="media/block.png"/>']
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + bw.CT)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', b2.document(cs))
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/styles.xml', bw.STYLES)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
        z.writestr(zipfile.ZipInfo('word/media/block.png', date_time=(2026, 11, 11, 0, 0, 0)), block_png())

def main(staging):
    cs = cases()
    d = os.path.join(staging, SET, NAME)
    os.makedirs(d)
    f = os.path.join(d, NAME + '.docx')
    package(f, cs)
    j = os.path.join(d, 'job.json')
    with open(j, 'w', newline='') as out:
        json.dump({'verb': 'save', 'input': NAME + '.docx', 'timeoutSeconds': 300, 'formats': ['docx', 'pdf']}, out)
    sums = [f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{NAME}/{os.path.basename(x)}\n' for x in (j, f)]
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines([NAME + '\n'])
    rows = ['set\tfile\tpage\tcase\tnote\tgap_left\tgap_right\n']
    for i, c in enumerate(cs):
        gl, gr = c.gaps()
        rows.append(f'{SET}\t{NAME}\t{i + 1}\t{c.cid}\t{c.note}\t{gl:.2f}\t{gr:.2f}\n')
    open(os.path.join(HERE, 'probes-bothsides4.tsv'), 'w', newline='').writelines(rows)
    print(NAME, len(cs), 'cases')

if __name__ == '__main__':
    main(sys.argv[1])
