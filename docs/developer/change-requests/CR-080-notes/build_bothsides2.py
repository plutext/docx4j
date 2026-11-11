# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Text on both sides of an object, the second rounds: the narrow segment's minimum width, and a table wider than a
gap. Asked by the FOP fork's session for its CR-024 phase B after fop-cr024-bothsides (bothsides-findings.md section
1, answers 6 and 7), on Jason's order in that session: "it is worth it. do the minimum-width round and the table wider
than either gap measurement".

fb10's geometry (build_bothsides.py): A4, 1in margins (the column x 72 to 523.3), Arial single, no space before or
after; the object a rectangle shape 150 x 120pt, square wrap on both sides, distT and distB 0, anchored in the case's
paragraph 12pt below its top. A gap is the room beside the object: x 72 to (the object's left - distL), and (the
object's right + distR) to 523.3. One case to a page, labelled.

fb20-minimum, the narrow segment's minimum (bothsides answer 6: a 15pt gap empty, 30pt used). The text is one-letter
words ("a a a ..."), so any gap above the minimum can take one.
  A1-A14  the left gap 16 to 29pt, 10pt text, distL and distR 9pt
  B1-B14  the same gaps, 20pt text: does the minimum follow the font size?
  C1-C14  the right gap 16 to 29pt, 10pt text, the wide gap on the left: is the rule the same for the right segment?
  D1-D7   the left gap 16, 18 ... 28pt, 10pt text, distL 0 and distR 9pt: the free gap or the object's distance?
fb30-tables, an ordinary in-flow table after a short anchor paragraph, its top in the band (bothsides answer 7: tables
narrower than either gap went in the left gap):
  T1  equal gaps (141.65pt each), a fixed table 200pt wide: wider than either gap
  T2  equal gaps, an autofit table whose content makes it about 200pt (one cell of unbreakable text)
  T3  the left gap 100pt, the right 183pt, a fixed table 120pt wide: it fits the right gap only
  T4  the left gap 183pt, the right 100pt, the same table: it fits the left gap only
  T5  the left gap 100pt, the right 183pt, a fixed table 80pt wide: it fits both
  T6  equal gaps, a table the column's width (100%)

usage: build_bothsides2.py <staging dir>   (also writes probes-bothsides2.tsv beside this script)"""
import hashlib, importlib.util, json, os, sys, zipfile
from xml.sax.saxutils import escape

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('bothsides', os.path.join(HERE, 'build_bothsides.py'))
bs = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(bs)
fx, bw = bs.fx, bs.bw

SET = 'fop-cr024-bothsides-r2'
EMU, COL, OW, OH = bs.EMU, bs.COL, bs.OW, bs.OH

def run(text, size=None):
    rpr = f'<w:rPr><w:sz w:val="{size * 2}"/><w:szCs w:val="{size * 2}"/></w:rPr>' if size else ''
    return f'<w:r>{rpr}<w:t xml:space="preserve">{escape(text)}</w:t></w:r>'

def p(content, ppr=''):
    return f'<w:p>{"<w:pPr>" + ppr + "</w:pPr>" if ppr else ""}{content}</w:p>'

def obj(n, x, dist_l=9, dist_r=9):
    """The object at x pt from the column's left, 12pt below its paragraph's top"""
    cx, cy = OW * EMU, OH * EMU
    d = (f'<wp:anchor distT="0" distB="0" distL="{dist_l * EMU}" distR="{dist_r * EMU}" simplePos="0" relativeHeight="{251658240 + n}" '
         f'behindDoc="0" locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>'
         f'<wp:positionH relativeFrom="column"><wp:posOffset>{round(x * EMU)}</wp:posOffset></wp:positionH>'
         f'<wp:positionV relativeFrom="paragraph"><wp:posOffset>{12 * EMU}</wp:posOffset></wp:positionV>'
         f'<wp:extent cx="{cx}" cy="{cy}"/><wp:effectExtent l="0" t="0" r="0" b="0"/>'
         f'<wp:wrapSquare wrapText="bothSides"/><wp:docPr id="{n}" name="Shape {n}"/><wp:cNvGraphicFramePr/>'
         f'<a:graphic><a:graphicData uri="{fx.URI_WPS}"><wps:wsp><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x="0" y="0"/>'
         f'<a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:solidFill>'
         f'<a:srgbClr val="D9D9D9"/></a:solidFill><a:ln><a:noFill/></a:ln></wps:spPr><wps:bodyPr/></wps:wsp>'
         f'</a:graphicData></a:graphic></wp:anchor>')
    return f'<w:r><mc:AlternateContent><mc:Choice Requires="wps"><w:drawing>{d}</w:drawing></mc:Choice></mc:AlternateContent></w:r>'

class Case:
    def __init__(self, cid, note, x, dist_l, dist_r, build):
        self.cid, self.note, self.x, self.dist_l, self.dist_r, self.build = cid, note, x, dist_l, dist_r, build

    def gaps(self):
        return self.x - self.dist_l, COL - (self.x + OW + self.dist_r)

def letters(size, count):
    def build(n, case):
        return p(obj(n, case.x, case.dist_l, case.dist_r) + run(' '.join(['a'] * count), size))
    return build

CENTRED = (COL - OW) / 2
BORDER = ''.join(f'<w:{e} w:val="single" w:sz="4" w:space="0" w:color="000000"/>' for e in
                 ('top', 'left', 'bottom', 'right', 'insideH', 'insideV'))

def fixed_table(width_pt):
    tw = round(width_pt * 20)
    rows = ''.join(f'<w:tr><w:tc><w:tcPr><w:tcW w:w="{tw}" w:type="dxa"/></w:tcPr>{p(run(bs.word(13 + k)))}</w:tc></w:tr>' for k in range(3))
    return (f'<w:tbl><w:tblPr><w:tblW w:w="{tw}" w:type="dxa"/><w:tblBorders>{BORDER}</w:tblBorders><w:tblLayout w:type="fixed"/>'
            f'</w:tblPr><w:tblGrid><w:gridCol w:w="{tw}"/></w:tblGrid>{rows}</w:tbl>')

def unbreakable(target_pt=190.0):
    """A word about that wide at 10pt Arial: the alphabet over again"""
    letters_ = 'abcdefghijklmnopqrstuvwxyz'
    out = ''
    while bs.width(out + letters_[len(out) % 26]) <= target_pt:
        out += letters_[len(out) % 26]
    return out

def auto_table():
    word = unbreakable()
    rows = ''.join(f'<w:tr><w:tc><w:tcPr><w:tcW w:w="0" w:type="auto"/></w:tcPr>{p(run(word if k == 0 else bs.word(13 + k)))}</w:tc></w:tr>'
                   for k in range(3))
    return (f'<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders>{BORDER}</w:tblBorders></w:tblPr>'
            f'<w:tblGrid><w:gridCol w:w="{round(bs.width(word) * 20) + 216}"/></w:tblGrid>{rows}</w:tbl>')

def full_table():
    rows = ''.join(f'<w:tr><w:tc><w:tcPr><w:tcW w:w="5000" w:type="pct"/></w:tcPr>{p(run(bs.word(13 + k)))}</w:tc></w:tr>' for k in range(3))
    return (f'<w:tbl><w:tblPr><w:tblW w:w="5000" w:type="pct"/><w:tblBorders>{BORDER}</w:tblBorders></w:tblPr>'
            f'<w:tblGrid><w:gridCol w:w="{round(COL * 20)}"/></w:tblGrid>{rows}</w:tbl>')

def with_table(table):
    """The anchor paragraph (12 words: a line, and part of the first band line, as fb10's 7a), the table, a paragraph"""
    def build(n, case):
        return (p(obj(n, case.x, case.dist_l, case.dist_r) + run(bs.words_text(1, 12))) + table
                + p(run(bs.words_text(32, 60))))
    return build

def cases_minimum():
    out = []
    for g in range(16, 30):
        out.append(Case(f'A{g - 15}', f'left gap {g}pt, 10pt text, distL and distR 9pt', g + 9, 9, 9, letters(None, 700)))
    for g in range(16, 30):
        out.append(Case(f'B{g - 15}', f'left gap {g}pt, 20pt text, distL and distR 9pt', g + 9, 9, 9, letters(20, 400)))
    for g in range(16, 30):
        out.append(Case(f'C{g - 15}', f'right gap {g}pt, 10pt text, distL and distR 9pt', COL - g - 9 - OW, 9, 9, letters(None, 700)))
    for k, g in enumerate(range(16, 30, 2)):
        out.append(Case(f'D{k + 1}', f'left gap {g}pt, 10pt text, distL 0 and distR 9pt', g, 0, 9, letters(None, 700)))
    return out

def cases_tables():
    left100 = 100 + 9
    left183 = 183 + 9
    return [Case('T1', 'equal gaps, a fixed table 200pt wide', CENTRED, 9, 9, with_table(fixed_table(200))),
            Case('T2', 'equal gaps, an autofit table about 200pt wide (one cell of unbreakable text)', CENTRED, 9, 9, with_table(auto_table())),
            Case('T3', 'left gap 100pt, right 183pt, a fixed table 120pt wide', left100, 9, 9, with_table(fixed_table(120))),
            Case('T4', 'left gap 183pt, right 100pt, a fixed table 120pt wide', left183, 9, 9, with_table(fixed_table(120))),
            Case('T5', 'left gap 100pt, right 183pt, a fixed table 80pt wide', left100, 9, 9, with_table(fixed_table(80))),
            Case('T6', 'equal gaps, a table the column\'s width (100%)', CENTRED, 9, 9, with_table(full_table()))]

def document(cases):
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in bw.NS.items())
    parts = []
    for i, c in enumerate(cases):
        ppr = '' if i == 0 else '<w:pPr><w:pageBreakBefore/></w:pPr>'
        gl, gr = c.gaps()
        parts.append(f'<w:p>{ppr}{run(f"case {c.cid}: {c.note}.")}</w:p>'
                     f'<w:p>{run(f"the object: 150 x 120pt, square both sides, distL {c.dist_l}pt, distR {c.dist_r}pt; gaps {gl:.2f}pt and {gr:.2f}pt.")}</w:p>')
        parts.append(c.build(i + 1, c))
    return f'{fx.DECL}<w:document {xmlns}><w:body>{"".join(parts)}{fx.SECT}</w:body></w:document>'

def package(path, cases):
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>',
            f'<Relationship Id="rId3" Type="{fx.REL}styles" Target="styles.xml"/>']
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + bw.CT)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', document(cases))
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/styles.xml', bw.STYLES)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())

FILES = [('fb20-minimum', cases_minimum), ('fb30-tables', cases_tables)]

def main(staging):
    sums, order, rows = [], [], ['set\tfile\tpage\tcase\tnote\tgap_left\tgap_right\n']
    for name, cases_of in FILES:
        cases = cases_of()
        d = os.path.join(staging, SET, name)
        os.makedirs(d)
        f = os.path.join(d, name + '.docx')
        package(f, cases)
        j = os.path.join(d, 'job.json')
        with open(j, 'w', newline='') as out:
            json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 300, 'formats': ['docx', 'pdf']}, out)
        sums += [f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{name}/{os.path.basename(x)}\n' for x in (j, f)]
        order.append(name + '\n')
        for i, c in enumerate(cases):
            gl, gr = c.gaps()
            rows.append(f'{SET}\t{name}\t{i + 1}\t{c.cid}\t{c.note}\t{gl:.2f}\t{gr:.2f}\n')
        print(name, len(cases), 'cases')
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines(order)
    open(os.path.join(HERE, 'probes-bothsides2.tsv'), 'w', newline='').writelines(rows)

if __name__ == '__main__':
    main(sys.argv[1])
