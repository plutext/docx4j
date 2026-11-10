# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Text on both sides of an object: Word's rules for the fork's CR-024 phase B (xmlgraphics-fop-plutext, CR-024 section
12.3; Enterprise CR-001 section 6.6 item 10). Asked by the fork's session for Jason, 2026-11-11; Jason: "take on the fork
session's probe of text on both sides of an anchored object".

One case to a page, labelled. Every page: A4, 1in margins (a 451.3pt column), compatibility mode 15, Arial 10pt single,
no space before or after (the package's docDefaults). The object is a rectangle shape, 150 x 120pt, filled, no line,
effect extent 0, square wrap on both sides, distL and distR 9pt, distT and distB 0, anchored in the case's first
paragraph, centred on the column (so each gap is 141.65pt) and 12pt below that paragraph's top, unless the case says.

The words are numbered (ab001, abcde002, abcdefgh003, ...), so a line's words say where Word set it. The text of each
case is laid out here by a greedy fill of the gaps with Arial's widths (Arimo's metrics), only to aim it: where a
paragraph ends, where a break falls. Word's PDF is the reading, not this.

The cases (CR-024 section 12.3):
  1a, 1b  a paragraph ending in the left gap (a third of the way in; filling it), then a second paragraph
  2a, 2b  a line break, w:br, in the left gap mid-line; in the right gap
  3a, 3b  indents (left and right 36pt, first line 24pt; hanging 36pt), the paragraph starting inside the band
  4       tab stops at 72pt and 330pt from the margin, one in each gap; default tabs; a tab whose stop is past the
          object
  5a-5d   justified (its last line in the left gap; in the right gap), centred, right-aligned
  6       the narrow gap: the object moved so the left gap is 15, 30 and 60pt, with short words and with long ones
  7a, 7b  a table in the band after the anchor paragraph: auto width; fixed 100pt
  8       a numbered list in the band
  9       case 1a with the object positioned on the page, at the same place
  10      a Hebrew paragraph beside the object (right to left)

usage: build_bothsides.py <staging dir>   (also writes probes-bothsides.tsv beside this script)"""
import hashlib, importlib.util, json, os, sys, zipfile
from xml.sax.saxutils import escape
from fontTools.ttLib import TTFont

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('wrap', os.path.join(HERE, 'build_wrap.py'))
bw = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(bw)
fx = bw.fx

SET = 'fop-cr024-bothsides'
EMU = 12700
COL = 451.3                                   # the column: A4 less 1in margins
OW, OH = 150, 120                             # the object
DIST = 9                                      # distL and distR
LINE_H = 11.5                                 # Arial 10pt, single

# ------------------------------------------------------------------------------------------- widths, to aim the text

_FONT = TTFont('/usr/share/fonts/croscore/Arimo-Regular.ttf')
_CMAP, _HMTX, _UPEM = _FONT.getBestCmap(), _FONT['hmtx'], _FONT['head'].unitsPerEm

def width(text, size=10):
    return sum(_HMTX[_CMAP.get(ord(c), _CMAP[ord('?')])][0] for c in text) * size / _UPEM

def word(n):
    return ('ab', 'abcde', 'abcdefgh')[n % 3] + f'{n:03d}'

def gaps(x=None):
    """The left and right gaps, in points from the margin, the object at x (centred when None)"""
    x = (COL - OW) / 2 if x is None else x
    return [(0, x - DIST), (x + OW + DIST, COL)]

def fill(words, lines, start=0, indent=0, first=0):
    """A greedy fill of the words into lines (each a list of (x0, x1) segments, or None for the full column), from
    word index start: each line's first segment is set in by the indent (and the first line's by first). Returns a list
    of (line number, segment number, words set there) and the index of the next word."""
    out, i = [], start
    for n, segs in enumerate(lines):
        segs = segs or [(0, COL)]
        for k, (x0, x1) in enumerate(segs):
            left = x0 + (indent + (first if n == 0 else 0) if k == 0 else 0)
            room, put = x1 - left, []
            while i < len(words):
                w = width(' '.join(put + [words[i]]))
                if w > room:
                    break
                put.append(words[i])
                i += 1
            out.append((n, k, put))
            if i >= len(words):
                return out, i
    return out, i

def band(lines_before, total, x=None):
    """Line shapes for a paragraph whose first line is lines_before lines below the anchor paragraph's top: the full
    column above the object (12pt down), the gaps beside it (to 132pt), the full column below"""
    out = []
    for k in range(total):
        top = (lines_before + k) * LINE_H
        out.append(gaps(x) if top + LINE_H > 12 and top < 12 + OH else None)
    return out

def words_ending(first, line_shapes, where):
    """The fewest words from number first that fill the lines up to a band line, ending as where says: ('left', f)
    ends the paragraph with the left gap f full (f 1 for full), ('right', 1) with the right gap full. Returns the count."""
    pool = [word(n) for n in range(first, first + 400)]
    for count in range(1, 400):
        placed, nxt = fill(pool[:count], line_shapes)
        line, seg, put = placed[-1]
        if line_shapes[line] is None or not put:
            continue
        side, frac = where
        x0, x1 = line_shapes[line][seg]
        used = width(' '.join(put))
        if side == 'left' and seg == 0:
            if frac >= 1:
                more, _ = fill(pool[:count + 1], line_shapes)
                if more[-1][0] != line or more[-1][1] != 0:
                    return count
            elif frac * (x1 - x0) - 20 <= used <= frac * (x1 - x0) + 30:
                return count
        if side == 'right' and seg == 1:
            more, _ = fill(pool[:count + 1], line_shapes)
            if more[-1][0] != line or more[-1][1] != 1:
                return count
    raise ValueError(where)

# ---------------------------------------------------------------------------------------------------- the XML parts

def run(text, rpr=''):
    return f'<w:r>{rpr}<w:t xml:space="preserve">{escape(text)}</w:t></w:r>'

BR = '<w:r><w:br/></w:r>'
TAB = '<w:r><w:tab/></w:r>'

def words_text(first, count):
    return ' '.join(word(n) for n in range(first, first + count))

def obj(n, h=('column', 'center'), v=('paragraph', 12)):
    cx, cy = OW * EMU, OH * EMU
    def pos(tag, ref, at):
        inner = f'<wp:align>{at}</wp:align>' if isinstance(at, str) else f'<wp:posOffset>{round(at * EMU)}</wp:posOffset>'
        return f'<wp:{tag} relativeFrom="{ref}">{inner}</wp:{tag}>'
    d = (f'<wp:anchor distT="0" distB="0" distL="{DIST * EMU}" distR="{DIST * EMU}" simplePos="0" relativeHeight="{251658240 + n}" '
         f'behindDoc="0" locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>{pos("positionH", *h)}'
         f'{pos("positionV", *v)}<wp:extent cx="{cx}" cy="{cy}"/><wp:effectExtent l="0" t="0" r="0" b="0"/>'
         f'<wp:wrapSquare wrapText="bothSides"/><wp:docPr id="{n}" name="Shape {n}"/><wp:cNvGraphicFramePr/>'
         f'<a:graphic><a:graphicData uri="{fx.URI_WPS}"><wps:wsp><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x="0" y="0"/>'
         f'<a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:solidFill>'
         f'<a:srgbClr val="D9D9D9"/></a:solidFill><a:ln><a:noFill/></a:ln></wps:spPr><wps:bodyPr/></wps:wsp>'
         f'</a:graphicData></a:graphic></wp:anchor>')
    return f'<w:r><mc:AlternateContent><mc:Choice Requires="wps"><w:drawing>{d}</w:drawing></mc:Choice></mc:AlternateContent></w:r>'

def p(content, ppr=''):
    return f'<w:p>{"<w:pPr>" + ppr + "</w:pPr>" if ppr else ""}{content}</w:p>'

# ---------------------------------------------------------------------------------------------------------- the cases

class Case:
    def __init__(self, cid, note, build):
        self.cid, self.note, self.build = cid, note, build

def anchor_then(n, first_count, x=None, v=('paragraph', 12), h=None):
    """The anchor paragraph A: the object and first_count words from 1"""
    hh = h or (('column', 'center') if x is None else ('column', x))
    return p(obj(n, hh, v) + run(words_text(1, first_count)))

def case1(fraction, page_anchor=False):
    def build(n):
        shapes = band(0, 20)
        count = words_ending(1, shapes, ('left', fraction))
        # case 9: the object on the page where case 1a's is: the label's two lines above the anchor paragraph
        v = ('page', 72 + 2 * LINE_H + 12) if page_anchor else ('paragraph', 12)
        h = ('page', 72 + (COL - OW) / 2) if page_anchor else None
        return anchor_then(n, count, v=v, h=h) + p(run(words_text(count + 1, 60))) + p(run(words_text(count + 61, 30)))
    return build

def case2(side):
    def build(n):
        shapes = band(0, 20)
        # the break after the first word of a band line's gap (the third band line's left gap, or its right)
        pool = [word(k) for k in range(1, 300)]
        placed, _ = fill(pool, shapes)
        target = [x for x in placed if x[0] == 4 and x[1] == (0 if side == 'left' else 1)][0]
        before = sum(len(x[2]) for x in placed[:placed.index(target)]) + 1
        return (p(obj(n) + run(words_text(1, before)) + BR + run(words_text(before + 1, 60)))
                + p(run(words_text(before + 61, 30))))
    return build

def case3(hanging):
    def build(n):
        shapes = band(0, 20)
        a = words_ending(1, shapes, ('right', 1))
        ind = '<w:ind w:left="720" w:hanging="720"/>' if hanging else '<w:ind w:left="720" w:right="720" w:firstLine="480"/>'
        return anchor_then(n, a) + p(run(words_text(a + 1, 70)), ind) + p(run(words_text(a + 71, 20)))
    return build

def case4(n):
    shapes = band(0, 20)
    a = words_ending(1, shapes, ('right', 1))
    tabs = '<w:tabs><w:tab w:val="left" w:pos="1440"/><w:tab w:val="left" w:pos="6600"/></w:tabs>'
    rows = ''.join(p(run(word(k)) + TAB + run(word(k + 1)) + TAB + run(word(k + 2)), tabs) for k in range(a + 1, a + 13, 3))
    past = p(run(f'{word(a + 13)} {word(a + 14)}') + TAB + run(word(a + 15)), tabs)          # text past 72pt: the next stop is over the gap
    default = p(TAB.join(run(word(k)) for k in range(a + 16, a + 24)))                       # default stops, every 36pt
    return anchor_then(n, a) + rows + past + default + p(run(words_text(a + 24, 40)))

def case5(jc, last_side=None):
    def build(n):
        shapes = band(0, 20)
        a = words_ending(1, shapes, ('right', 1))
        lines_a = fill([word(k) for k in range(1, a + 1)], shapes)[0][-1][0] + 1
        rest = band(lines_a, 20)
        if last_side == 'right':
            b = words_ending(a + 1, rest, ('right', 1))
        else:
            b = words_ending(a + 1, rest, ('left', 0.4))
        return anchor_then(n, a) + p(run(words_text(a + 1, b)), f'<w:jc w:val="{jc}"/>') + p(run(words_text(a + b + 1, 40)))
    return build

SHORT = ['a', 'to', 'the']

def case6(gap, short):
    def build(n):
        x = gap + DIST
        if short:
            text = ' '.join(SHORT[k % 3] for k in range(150))
            return p(obj(n, ('column', x)) + run(text)) + p(run(words_text(1, 30)))
        return p(obj(n, ('column', x)) + run(words_text(1, 70))) + p(run(words_text(71, 30)))
    return build

def case7(fixed):
    def build(n):
        a = 12      # line 1 and part of the first band line
        border = ''.join(f'<w:{e} w:val="single" w:sz="4" w:space="0" w:color="000000"/>' for e in
                         ('top', 'left', 'bottom', 'right', 'insideH', 'insideV'))
        if fixed:
            tbl = (f'<w:tbl><w:tblPr><w:tblW w:w="2000" w:type="dxa"/><w:tblBorders>{border}</w:tblBorders><w:tblLayout w:type="fixed"/>'
                   '</w:tblPr><w:tblGrid><w:gridCol w:w="2000"/></w:tblGrid>'
                   + ''.join(f'<w:tr><w:tc><w:tcPr><w:tcW w:w="2000" w:type="dxa"/></w:tcPr>{p(run(word(a + k)))}</w:tc></w:tr>'
                             for k in range(1, 4)) + '</w:tbl>')
        else:
            tbl = (f'<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders>{border}</w:tblBorders></w:tblPr>'
                   '<w:tblGrid><w:gridCol w:w="1200"/><w:gridCol w:w="1200"/></w:tblGrid>'
                   + ''.join(f'<w:tr><w:tc><w:tcPr><w:tcW w:w="0" w:type="auto"/></w:tcPr>{p(run(word(a + 2 * k)))}</w:tc>'
                             f'<w:tc><w:tcPr><w:tcW w:w="0" w:type="auto"/></w:tcPr>{p(run(word(a + 2 * k + 1)))}</w:tc></w:tr>'
                             for k in range(1, 4)) + '</w:tbl>')
        return anchor_then(n, a) + tbl + p(run(words_text(a + 20, 60)))
    return build

def case8(n):
    a = 12
    items = ''.join(p(run(words_text(a + 1 + 7 * k, 7)), '<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>')
                    for k in range(8))
    return anchor_then(n, a) + items + p(run(words_text(a + 60, 40)))

HEB = 'אבגדהוזחטי'                             # the digits 0 to 9, in Hebrew letters, to number the words

def heb(k):
    return ('של', 'שלומ', 'שלומעול')[k % 3] + ''.join(HEB[int(c)] for c in f'{k:03d}')

def case10(n):
    rtl = '<w:rPr><w:rtl/><w:lang w:bidi="he-IL"/></w:rPr>'
    text = ' '.join(heb(k) for k in range(1, 90))
    return (p(obj(n) + run(text, rtl), '<w:bidi/><w:jc w:val="left"/>')
            + p(run(' '.join(heb(k) for k in range(90, 120)), rtl), '<w:bidi/><w:jc w:val="left"/>'))

CASES = [
    Case('1a', 'paragraph A ends a third of the way into the left gap, then paragraph B', case1(0.33)),
    Case('1b', 'paragraph A ends filling the left gap, then paragraph B', case1(1)),
    Case('2a', 'a line break (w:br) after the first word of a band line\'s left gap', case2('left')),
    Case('2b', 'a line break (w:br) after the first word of a band line\'s right gap', case2('right')),
    Case('3a', 'paragraph B in the band: left 36pt, right 36pt, first line 24pt', case3(False)),
    Case('3b', 'paragraph B in the band: left 36pt, hanging 36pt', case3(True)),
    Case('4', 'tab stops left at 72pt and 330pt from the margin; a tab past the left gap; default tabs', case4),
    Case('5a', 'paragraph B justified, its last line in the left gap', case5('both')),
    Case('5b', 'paragraph B justified, its last line ending the right gap', case5('both', 'right')),
    Case('5c', 'paragraph B centred', case5('center')),
    Case('5d', 'paragraph B right-aligned', case5('right')),
    Case('6a', 'left gap 15pt, short words (a, to, the)', case6(15, True)),
    Case('6b', 'left gap 15pt, long words', case6(15, False)),
    Case('6c', 'left gap 30pt, short words', case6(30, True)),
    Case('6d', 'left gap 30pt, long words', case6(30, False)),
    Case('6e', 'left gap 60pt, short words', case6(60, True)),
    Case('6f', 'left gap 60pt, long words', case6(60, False)),
    Case('7a', 'an auto-width table after the anchor paragraph, beside the object', case7(False)),
    Case('7b', 'a fixed 100pt table after the anchor paragraph, beside the object', case7(True)),
    Case('8', 'a numbered list (label, tab, text) beside the object', case8),
    Case('9', 'case 1a with the object positioned on the page, where 1a\'s is', case1(0.33, page_anchor=True)),
    Case('10', 'a Hebrew paragraph (right to left) beside the object', case10),
]

def document():
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in bw.NS.items())
    parts = []
    for i, c in enumerate(CASES):
        ppr = '' if i == 0 else '<w:pPr><w:pageBreakBefore/></w:pPr>'
        parts.append(f'<w:p>{ppr}{run(f"case {c.cid}: {c.note}.")}</w:p><w:p>{run("the object: a 150 x 120pt rectangle, square both sides, distL and distR 9pt.")}</w:p>')
        parts.append(c.build(i + 1))
    return f'{fx.DECL}<w:document {xmlns}><w:body>{"".join(parts)}{fx.SECT}</w:body></w:document>'

NUMBERING = (f'{fx.DECL}<w:numbering xmlns:w="{bw.NS["w"]}"><w:abstractNum w:abstractNumId="0"><w:multiLevelType w:val="singleLevel"/>'
             '<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="%1."/><w:lvlJc w:val="left"/>'
             '<w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr></w:lvl></w:abstractNum><w:num w:numId="1"><w:abstractNumId w:val="0"/>'
             '</w:num></w:numbering>')
CT = bw.CT.replace('<Override PartName="/word/settings.xml"', '<Override PartName="/word/numbering.xml" ContentType="application/'
                   'vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/><Override PartName="/word/settings.xml"')

def package(path):
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>',
            f'<Relationship Id="rId3" Type="{fx.REL}styles" Target="styles.xml"/>',
            f'<Relationship Id="rId4" Type="{fx.REL}numbering" Target="numbering.xml"/>']
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + CT)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', document())
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/styles.xml', bw.STYLES)
        fx.entry(z, 'word/numbering.xml', NUMBERING)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())

def main(staging):
    name = 'fb10-bothsides'
    d = os.path.join(staging, SET, name)
    os.makedirs(d)
    f = os.path.join(d, name + '.docx')
    package(f)
    j = os.path.join(d, 'job.json')
    with open(j, 'w', newline='') as out:
        json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 300, 'formats': ['docx', 'pdf']}, out)
    sums = [f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{name}/{os.path.basename(x)}\n' for x in (j, f)]
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines([name + '\n'])
    rows = ['set\tfile\tpage\tcase\tnote\n'] + [f'{SET}\t{name}\t{i + 1}\t{c.cid}\t{c.note}\n' for i, c in enumerate(CASES)]
    open(os.path.join(HERE, 'probes-bothsides.tsv'), 'w', newline='').writelines(rows)
    print(name, len(CASES), 'cases')

if __name__ == '__main__':
    main(sys.argv[1])
