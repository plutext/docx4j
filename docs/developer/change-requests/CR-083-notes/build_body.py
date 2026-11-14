# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 5: the round cr083-phase5, what Word does with a run's w14 text effects in body text that phases 0 and
3 left open (phase5-findings.md section 3, with the review's additions, section 4, finding 12). Jason, 2026-11-14:
"commence phase 5".

One probe to a page, its first paragraph naming it (in Word's default face), the second giving its effects, then the
probe's own paragraphs in Aptos (unless the probe says otherwise), black, left-aligned, single spaced, no space before
or after. A page "at the top" has its probe's paragraphs first and the label after. The effects are those of phases 0
and 3, light against dark text so that read_effects.py --detail tells the ink from the effect: a 1 pt red outline
(C00000) over the black fill; a gradient red to blue (FF0000, 0000FF) at 0 or 90 degrees; a shadow blurred 4 pt, 3 pt
away at 45 degrees, black at 40% (SH); a glow of 5 pt, ED7D31 at 40% (GL); a reflection blurred 0.5 pt (RF).

  wb80  a run broken across lines and what it sits in: each kind on a run broken across two lines (24 pt), three of
        them across three; justified; letter spacing; a tab inside the run; a trailing space; two neighbouring runs
        whose shadows and glows overlap (the drawing order); an underlined and a struck run with an outline and a
        gradient fill, and a hollow one underlined; highlight and shading under a glow; a gradient at 45 degrees with
        scaled 0 and 1; path gradients (rect, shape)
  wb81  the room at body sizes and under each spacing rule: each page a plain line, then the effect's line alone, a
        plain line, a line with the effect on its middle run and a plain line, all at one size under one rule, so that
        the page holds its own reference; a shadow, a glow and a reflection 4 pt down at 10, 11, 12 and 14 pt; a glow
        and that reflection at 14 pt under auto 1.15, double, exact and atLeast below and above the line's height; a
        superscript and a subscript with a glow; an empty paragraph whose mark has one; a run of spaces alone with one;
        a run in a table cell; a paragraph at the top of a page with and without one; in a header (three lines that
        pass the top margin) with and without one
  wb82  where effects come from and other forms: a character style, a paragraph style; a hyperlink's text outlined;
        outlines of 0.25 to 6 pt on 11 pt text; an outline with alpha; a text box with a run's effects and no warp,
        sized to its text and fixed; a shape's own gradient fill in three forms (two stops at the ends, two inside,
        three; does Word blend a shape's as a run's?); a run's gradient in each stop form at Calibri 20 pt and Times New
        Roman 48 pt, and two stops at 0 and 60%, four stops; a run split by soft hyphens and by a font fallback; a face
        with no bold or italic of its own (Sylfaen); a right-to-left run (Hebrew, Arial); a numbered paragraph whose
        mark carries the effect; two runs with unequal glows on one line under atLeast spacing
  wb83  a run split by automatic hyphenation (the file has w:autoHyphenation)

A second set, cr083-phase5b (build_body.py <staging dir> b; probes-body-b.tsv), asks what the round's one numbered
probe left open (findings section 6.2: wb82-30's line took the mark's glow's room above and none below):

  wb84  a numbered paragraph's mark with a glow, a shadow and a reflection at 24 and 12 pt, a bullet's, the run's own
        effect beside the mark's, the level's rPr, each spacing rule, an unnumbered paragraph's mark, a centred number

usage: build_body.py <staging dir> [b]   (writes probes-body.tsv beside this script; stages cr083-phase5)"""
import hashlib, json, os, re, sys, zipfile
from xml.sax.saxutils import escape

import build_wordart as bw
import build_effects as be

HERE = os.path.dirname(os.path.abspath(__file__))
fx = bw.fx
EMU = bw.EMU
SET = 'cr083-phase5'
TIMEOUT = 600
W14 = 'http://schemas.microsoft.com/office/word/2010/wordml'

# ------------------------------------------------------------------------------------------------- the effects

RED, BLUE = 'FF0000', '0000FF'


def grad(stops, ang=0, scaled=0, path=None):
    """w14:gradFill; stops [(pos 0..100000, colour)]; ang in degrees"""
    gs = ''.join(f'<w14:gs w14:pos="{p}">{bw.w14_clr(c)}</w14:gs>' for p, c in stops)
    shade = (f'<w14:path w14:path="{path}"><w14:fillToRect w14:l="50000" w14:t="50000" w14:r="50000" w14:b="50000"/>'
             '</w14:path>' if path else f'<w14:lin w14:ang="{int(ang * 60000)}" w14:scaled="{scaled}"/>')
    return f'<w14:gradFill><w14:gsLst>{gs}</w14:gsLst>{shade}</w14:gradFill>'


def tf(inner):
    return bw.text_fill(inner)


ENDS = [(0, RED), (100000, BLUE)]
INSIDE = [(21000, RED), (88000, BLUE)]
THREE = [(0, RED), (50000, 'FFFF00'), (100000, BLUE)]
G0 = tf(grad(ENDS, 0))
G90 = tf(grad(ENDS, 90))
OUT = bw.outline(12700, bw.solid('C00000'))
SH = be.SH_A
GL = be.GL5
GL8 = be.GL8
RF = be.rf()
RF4 = be.rf(0, 4)
KINDS = [('outline 1 pt red', OUT), ('gradient 0 degrees', G0), ('gradient 90 degrees', G90), ('shadow', SH),
         ('glow 5 pt', GL), ('reflection', RF)]


def fxs(*parts):
    return bw.effects(*parts)


# ------------------------------------------------------------------------------------------------- the XML

# CT_RPr's sequence (the w14 effects after it, in EG_RPrTextEffects' order, as Word writes them)
RPR = ['rStyle', 'rFonts', 'b', 'bCs', 'i', 'iCs', 'caps', 'smallCaps', 'strike', 'dstrike', 'vanish', 'color',
       'spacing', 'w', 'kern', 'position', 'sz', 'szCs', 'highlight', 'u', 'effect', 'bdr', 'shd', 'fitText',
       'vertAlign', 'rtl', 'cs', 'em', 'lang']


def rpr(size=24, font='Aptos', color='000000', fx='', style=None, cs_font=None, **kw):
    """A w:rPr's content. kw: CT_RPr elements by name, each the element's XML or True for an empty one."""
    el = dict(kw)
    if style:
        el['rStyle'] = f'<w:rStyle w:val="{style}"/>'
    if font:
        c = cs_font or font
        el['rFonts'] = f'<w:rFonts w:ascii="{font}" w:hAnsi="{font}" w:cs="{c}"/>'
    if color:
        el['color'] = f'<w:color w:val="{color}"/>'
    if size:
        hp = int(round(size * 2))
        el['sz'], el['szCs'] = f'<w:sz w:val="{hp}"/>', f'<w:szCs w:val="{hp}"/>'
    for k in RPR:
        if el.get(k) is True:
            el[k] = f'<w:{k}/>'
    unknown = set(el) - set(RPR)
    assert not unknown, unknown
    return ''.join(el[k] for k in RPR if el.get(k)) + fx


def R(text, **kw):
    """A run; text may hold \t (a w:tab) and \xad (a w:softHyphen)"""
    parts = []
    for piece in re.split(r'(\t|\xad)', text):
        if piece == '\t':
            parts.append('<w:tab/>')
        elif piece == '\xad':
            parts.append('<w:softHyphen/>')
        elif piece:
            parts.append(f'<w:t xml:space="preserve">{escape(piece)}</w:t>')
    return f'<w:r><w:rPr>{rpr(**kw)}</w:rPr>{"".join(parts)}</w:r>'


def P(runs, jc='left', line=240, rule='auto', before=0, after=0, style=None, num=None, bidi=False, mark='', ind=''):
    """A paragraph; runs a list of run XML; mark the paragraph mark's w:rPr content"""
    ps = f'<w:pStyle w:val="{style}"/>' if style else ''
    nm = f'<w:numPr><w:ilvl w:val="0"/><w:numId w:val="{num}"/></w:numPr>' if num else ''
    bd = '<w:bidi/>' if bidi else ''
    sp = f'<w:spacing w:before="{before}" w:after="{after}" w:line="{line}" w:lineRule="{rule}"/>'
    mk = f'<w:rPr>{mark}</w:rPr>' if mark else ''
    return f'<w:p><w:pPr>{ps}{nm}{bd}{sp}{ind}<w:jc w:val="{jc}"/>{mk}</w:pPr>{"".join(runs)}</w:p>'


LONG2 = 'this run has the effect and breaks onto a second line'      # Aptos 24 pt: lines 1 and 2
LONG3 = 'this run has the effect and runs on long enough to break onto a second line and then a third'   # 1 to 3


class Page:
    """One probe: its paragraphs (XML); text, the line read_effects.py --text reads; top, the probe's paragraphs before
    its label (at the top of the page); header, a header's paragraphs (XML) for the probe's own section."""
    kind = 'body'

    def __init__(self, note, paras, text='', top=False, header=None):
        self.note, self.paras, self._text, self.top, self.header = note, paras, text, top, header

    def summary(self):
        xml = ''.join(self.paras) + (''.join(self.header) if self.header else '')
        names = []
        for m in re.findall(r'<w14:(\w+)', xml):
            if m in bw.ORDER and m not in names:
                names.append(m)
        return ' '.join(names) or 'no effect'

    def text(self):
        return self._text


def label(pid, p, br):
    pb = '<w:pPr><w:pageBreakBefore/></w:pPr>' if br else ''
    return [f'<w:p>{pb}{fx.run(f"{pid}: {p.note}.")}</w:p>', f'<w:p>{fx.run(p.summary())}</w:p>']


def brk(xml):
    """the paragraph xml with a page break before it"""
    if xml.startswith('<w:p><w:pPr>'):
        return xml.replace('<w:p><w:pPr>', '<w:p><w:pPr><w:pageBreakBefore/>', 1)
    if xml.startswith('<w:p>'):
        return xml.replace('<w:p>', '<w:p><w:pPr><w:pageBreakBefore/></w:pPr>', 1)
    return '<w:p><w:pPr><w:pageBreakBefore/></w:pPr></w:p>' + xml        # a table: an empty paragraph breaks


HDR = (f'{fx.DECL}<w:hdr xmlns:w="{fx.NS["w"]}" xmlns:w14="{W14}" '
       'xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="w14">{}</w:hdr>')


def document(probes):
    """The body; probes with a header come last, each its own section (a section with no header reference would take
    the one before it)"""
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in bw.NS.items())
    out, hdr = [], []
    plain = [(pid, p) for pid, p in probes if not p.header]
    headed = [(pid, p) for pid, p in probes if p.header]
    assert probes[:len(plain)] == plain, 'probes with a header go last'
    for i, (pid, p) in enumerate(plain):
        lab = label(pid, p, i > 0 and not p.top)
        if p.top:
            out += ([brk(p.paras[0])] if i > 0 else p.paras[:1]) + p.paras[1:] + lab
        else:
            out += lab + p.paras
    sect = fx.SECT
    for j, (pid, p) in enumerate(headed):
        if j == 0 and plain:
            out.append(f'<w:p><w:pPr>{fx.SECT}</w:pPr></w:p>')
        n = j + 1
        rid = f'rIdH{n}'
        hdr.append((n, rid, HDR.format(''.join(p.header))))
        sect = fx.SECT.replace('<w:sectPr>', f'<w:sectPr><w:headerReference w:type="default" r:id="{rid}"/>')
        out += label(pid, p, False) + p.paras
        if j < len(headed) - 1:
            out.append(f'<w:p><w:pPr>{sect}</w:pPr></w:p>')
    return (f'{fx.DECL}<w:document {xmlns} mc:Ignorable="w14"><w:body>{"".join(out)}{sect}</w:body></w:document>', hdr)


STYLES = (f'{fx.DECL}<w:styles xmlns:w="{fx.NS["w"]}" xmlns:w14="{W14}" '
          'xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="w14">'
          '<w:style w:type="character" w:customStyle="1" w:styleId="EffectChar"><w:name w:val="Effect Char"/>'
          '<w:rPr>{char}</w:rPr></w:style>'
          '<w:style w:type="paragraph" w:customStyle="1" w:styleId="EffectPara"><w:name w:val="Effect Para"/>'
          '<w:pPr><w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/></w:pPr><w:rPr>{para}</w:rPr>'
          '</w:style></w:styles>')
NUMBERING = (f'{fx.DECL}<w:numbering xmlns:w="{fx.NS["w"]}"><w:abstractNum w:abstractNumId="0">'
             '<w:multiLevelType w:val="singleLevel"/><w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/>'
             '<w:lvlText w:val="%1."/><w:lvlJc w:val="left"/><w:pPr><w:ind w:left="720" w:hanging="720"/></w:pPr>'
             '</w:lvl></w:abstractNum><w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num></w:numbering>')
CT_STYLES = 'application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml'
CT_NUMBERING = 'application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml'
CT_HDR = bw.CT_HDR


def package(path, probes, styles=None, numbering=False, hyphenate=False):
    doc, hdr = document(probes)
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>']
    over = [f'<Override PartName="/word/header{n}.xml" ContentType="{CT_HDR}"/>' for n, _, _ in hdr]
    rels += [f'<Relationship Id="{rid}" Type="{fx.REL}header" Target="header{n}.xml"/>' for n, rid, _ in hdr]
    if styles:
        rels.append(f'<Relationship Id="rIdS" Type="{fx.REL}styles" Target="styles.xml"/>')
        over.append(f'<Override PartName="/word/styles.xml" ContentType="{CT_STYLES}"/>')
    if numbering:
        rels.append(f'<Relationship Id="rIdN" Type="{fx.REL}numbering" Target="numbering.xml"/>')
        over.append(f'<Override PartName="/word/numbering.xml" ContentType="{CT_NUMBERING}"/>')
    ct = fx.CT.replace('</Types>', ''.join(over) + '</Types>')
    settings = fx.SETTINGS
    if hyphenate:
        settings = settings.replace('<w:compat>', '<w:autoHyphenation/><w:compat>')
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + ct)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', doc)
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', settings)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
        if styles:
            fx.entry(z, 'word/styles.xml', styles)
        if numbering:
            fx.entry(z, 'word/numbering.xml', NUMBERING if numbering is True else numbering)
        for n, _, xml in hdr:
            fx.entry(z, f'word/header{n}.xml', xml)


# ------------------------------------------------------------------------------------------------- wb80

def lead(effect_run, size=24, jc='left', after_text=' and plain words after it.', **pkw):
    """plain words, the effect's run, plain words: one paragraph"""
    return P([R('Plain words before it, then ', size=size), effect_run, R(after_text, size=size)], jc=jc, **pkw)


def fam_lines():
    out = []
    for note, e in KINDS:
        out.append(Page(f'{note}, a run broken across two lines (24 pt)', [lead(R(LONG2, fx=e))], 'Plain words'))
    for note, e in KINDS[1:3] + KINDS[4:5]:
        out.append(Page(f'{note}, a run broken across three lines (24 pt)', [lead(R(LONG3, fx=e))], 'Plain words'))
    out += [
        Page('gradient 0 degrees, a run broken across lines in a justified paragraph',
             [lead(R(LONG2, fx=G0), jc='both', after_text=' and plain words after it, so that the last line is '
                   'justified too and runs on to a third line.')], 'Plain words'),
        Page('gradient 0 degrees, a run with letter spacing 3 pt (w:spacing 60)',
             [lead(R('spaced out', fx=G0, spacing='<w:spacing w:val="60"/>'))], 'Plain words'),
        Page('gradient 0 degrees, a tab inside the run (default stops)',
             [lead(R('before\tafter the tab', fx=G0))], 'Plain words'),
        Page('glow 5 pt, a tab inside the run (default stops)',
             [lead(R('before\tafter the tab', fx=GL))], 'Plain words'),
        Page('gradient 0 degrees, a run ending in a trailing space, mid-line',
             [lead(R('a gradient run ', fx=G0), after_text='and plain words after it.')], 'Plain words'),
        Page('two neighbouring runs, each a shadow blurred 6 pt, 4 pt away at 0 degrees (the drawing order)',
             [P([R('Plain ', size=36), R('Shadow', size=36, fx=be.sh(6, 4, 0)), R('Next', size=36, fx=be.sh(6, 4, 0)),
                 R(' plain', size=36)])], 'Plain'),
        Page('two neighbouring runs, a glow 8 pt orange then a glow 8 pt blue (the drawing order)',
             [P([R('Plain ', size=36), R('Orange', size=36, fx=be.gl(8)), R('Blue', size=36, fx=be.gl(8, '4472C4')),
                 R(' plain', size=36)])], 'Plain'),
        Page('underlined, a 1 pt red outline over a gradient fill at 0 degrees',
             [lead(R('underlined run', size=36, fx=fxs(OUT, G0), u='<w:u w:val="single"/>'), size=36,
                   after_text=' plain.')], 'Plain words'),
        Page('struck through, a 1 pt red outline over a gradient fill at 0 degrees',
             [lead(R('struck run', size=36, fx=fxs(OUT, G0), strike=True), size=36, after_text=' plain.')],
             'Plain words'),
        Page('underlined, hollow (no fill, a 1 pt black outline)',
             [lead(R('hollow run', size=36, fx=fxs(tf(bw.NOFILL), bw.outline(12700)), u='<w:u w:val="single"/>'),
                   size=36, after_text=' plain.')], 'Plain words'),
        Page('highlighted yellow, a glow 8 pt',
             [lead(R('highlighted', size=36, fx=GL8, highlight='<w:highlight w:val="yellow"/>'), size=36,
                   after_text=' plain.')], 'Plain words'),
        Page('shaded D9E2F3, a glow 8 pt',
             [lead(R('shaded run', size=36, fx=GL8, shd='<w:shd w:val="clear" w:color="auto" w:fill="D9E2F3"/>'),
                   size=36, after_text=' plain.')], 'Plain words'),
        Page('gradient at 45 degrees, scaled 0, 36 pt alone',
             [P([R('Gradient 45', size=36, fx=tf(grad(ENDS, 45, 0)))])], 'Gradient 45'),
        Page('gradient at 45 degrees, scaled 1, 36 pt alone',
             [P([R('Gradient 45', size=36, fx=tf(grad(ENDS, 45, 1)))])], 'Gradient 45'),
        Page('path gradient rect, 36 pt alone', [P([R('Path rect', size=36, fx=tf(grad(ENDS, path='rect')))])],
             'Path rect'),
        Page('path gradient shape, 36 pt alone', [P([R('Path shape', size=36, fx=tf(grad(ENDS, path='shape')))])],
             'Path shape'),
    ]
    return out


# ------------------------------------------------------------------------------------------------- wb81

def room(note, effect, size=14, line=240, rule='auto', alone=None, mid=None):
    """a plain line; the effect's line alone; a plain line; a line with the effect on its middle run; a plain line"""
    k = dict(line=line, rule=rule)
    alone = alone or [R('WordArt', size=size, fx=effect)]
    mid = mid or [R('Plain ', size=size), R('WordArt', size=size, fx=effect), R(' plain', size=size)]
    return Page(note, [P([R('Plain line before.', size=size)], **k), P(alone, **k),
                       P([R('Plain line after the line alone.', size=size)], **k), P(mid, **k),
                       P([R('Plain line after the line with the run.', size=size)], **k)], 'WordArt')


RULES = [('auto 1.15', 276, 'auto'), ('double', 480, 'auto'), ('exact 24 pt', 480, 'exact'),
         ('atLeast 12 pt (under the line)', 240, 'atLeast'), ('atLeast 30 pt (over the line)', 600, 'atLeast')]


def table(rows):
    b = ''.join(f'<w:{s} w:val="single" w:sz="4" w:space="0" w:color="808080"/>'
                for s in ('top', 'left', 'bottom', 'right', 'insideH', 'insideV'))
    tr = ''.join('<w:tr>' + ''.join(f'<w:tc><w:tcPr><w:tcW w:w="3600" w:type="dxa"/></w:tcPr>{c}</w:tc>' for c in r)
                 + '</w:tr>' for r in rows)
    return (f'<w:tbl><w:tblPr><w:tblW w:w="7200" w:type="dxa"/><w:tblBorders>{b}</w:tblBorders><w:tblLayout '
            'w:type="fixed"/></w:tblPr><w:tblGrid><w:gridCol w:w="3600"/><w:gridCol w:w="3600"/></w:tblGrid>'
            f'{tr}</w:tbl>')


def header(effect):
    return [P([R(f'Header line {i}', size=14, fx=effect if i == 3 else '')]) for i in (1, 2, 3)]


def fam_room():
    out = []
    for size in (10, 11, 12, 14):
        for note, e in (('shadow blurred 4 pt, 3 pt at 45 degrees', SH), ('glow 5 pt', GL),
                        ('reflection sharp, 4 pt down', RF4)):
            out.append(room(f'Aptos {size} pt, {note}', e, size))
    for note, line, rule in RULES:
        out.append(room(f'Aptos 14 pt, glow 8 pt, line spacing {note}', GL8, 14, line, rule))
    for note, line, rule in (RULES[1], RULES[2], RULES[4]):
        out.append(room(f'Aptos 14 pt, reflection sharp, 4 pt down, line spacing {note}', RF4, 14, line, rule))
    for va in ('superscript', 'subscript'):
        v = f'<w:vertAlign w:val="{va}"/>'
        out.append(room(f'Aptos 14 pt, a {va} with a glow 8 pt', GL8,
                        alone=[R('WordArt', size=14), R('2', size=14, fx=GL8, vertAlign=v)],
                        mid=[R('Plain ', size=14), R('2', size=14, fx=GL8, vertAlign=v), R(' plain', size=14)]))
    out.append(Page('Aptos 14 pt, an empty paragraph whose mark has a glow 8 pt, after an empty one with none',
                    [P([R('Plain line before.', size=14)]), P([], mark=rpr(14)), P([R('Plain line between.', size=14)]),
                     P([], mark=rpr(14, fx=GL8)), P([R('Plain line after.', size=14)])], 'Plain line'))
    out.append(Page('Aptos 14 pt, a paragraph of spaces alone with a glow 8 pt, after one with none',
                    [P([R('Plain line before.', size=14)]), P([R('     ', size=14)]), P([R('Plain line between.', size=14)]),
                     P([R('     ', size=14, fx=GL8)]), P([R('Plain line after.', size=14)])], 'Plain line'))
    cell = lambda runs: P(runs)
    out.append(Page('Aptos 14 pt, a table: the second row\'s first cell a glow 8 pt, the first row plain',
                    [P([R('Plain line before.', size=14)]),
                     table([[cell([R('Plain cell', size=14)]), cell([R('Plain cell', size=14)])],
                            [cell([R('WordArt', size=14, fx=GL8)]), cell([R('Plain cell', size=14)])],
                            [cell([R('Plain cell', size=14)]), cell([R('Plain cell', size=14)])]]),
                     P([R('Plain line after the table.', size=14)])], 'WordArt'))
    for note, e in (('no effect', ''), ('a glow 8 pt', GL8)):
        out.append(Page(f'Aptos 36 pt at the top of the page, {note}',
                        [P([R('WordArt', size=36, fx=e)]), P([R('Plain line after.', size=14)])], 'WordArt', top=True))
    for note, e in (('no effect', ''), ('a glow 8 pt on its third line', GL8)):
        out.append(Page(f'a header of three 14 pt lines, past the top margin, {note}',
                        [P([R('The body\'s first line.', size=14)])], 'Header line 3', header=header(e)))
    return out


# ------------------------------------------------------------------------------------------------- wb82

WB82_CHAR = fxs(GL, G0)
WB82_PARA = fxs(GL, OUT)
STYLES_WB82 = STYLES.format(char=WB82_CHAR, para=rpr(24, fx=WB82_PARA))


def shape_fill(note, stops):
    gs = ''.join(f'<a:gs pos="{p}"><a:srgbClr val="{c}"/></a:gs>' for p, c in stops)
    cx, cy = 288 * EMU, 72 * EMU
    g = (f'<a:graphic><a:graphicData uri="{fx.URI_WPS}"><wps:wsp><wps:cNvSpPr/><wps:spPr>'
         f'<a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/>'
         f'</a:prstGeom><a:gradFill rotWithShape="1"><a:gsLst>{gs}</a:gsLst><a:lin ang="0" scaled="0"/></a:gradFill>'
         '<a:ln><a:noFill/></a:ln></wps:spPr><wps:bodyPr/></wps:wsp></a:graphicData></a:graphic>')
    return Page(note, [anchored(g, cx, cy, 1)], '')


def anchored(graphic, cx, cy, n):
    d = (f'<wp:anchor distT="0" distB="0" distL="0" distR="0" simplePos="0" relativeHeight="{251658240 + n}" '
         'behindDoc="0" locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>'
         f'<wp:positionH relativeFrom="page"><wp:posOffset>{bw.X0}</wp:posOffset></wp:positionH>'
         f'<wp:positionV relativeFrom="page"><wp:posOffset>{bw.Y0}</wp:posOffset></wp:positionV>'
         f'<wp:extent cx="{cx}" cy="{cy}"/><wp:effectExtent l="0" t="0" r="0" b="0"/><wp:wrapNone/>'
         f'<wp:docPr id="{n}" name="Shape {n}"/><wp:cNvGraphicFramePr/>{graphic}</wp:anchor>')
    return (f'<w:p><w:r><mc:AlternateContent><mc:Choice Requires="wps"><w:drawing>{d}</w:drawing></mc:Choice>'
            '</mc:AlternateContent></w:r></w:p>')


def text_box(note, runs, fit):
    """a text box as Insert > Text Box makes it (no warp, not WordArt), 0.25 pt grey line"""
    cx, cy = 288 * EMU, 72 * EMU
    l, t, r, b = bw.INS
    f = '<a:spAutoFit/>' if fit else '<a:noAutofit/>'
    g = (f'<a:graphic><a:graphicData uri="{fx.URI_WPS}"><wps:wsp><wps:cNvSpPr txBox="1"/><wps:spPr>'
         f'<a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/>'
         f'</a:prstGeom><a:noFill/>{bw.HAIRLINE}</wps:spPr><wps:txbx><w:txbxContent>{P(runs)}</w:txbxContent></wps:txbx>'
         f'<wps:bodyPr rot="0" vert="horz" wrap="square" lIns="{l}" tIns="{t}" rIns="{r}" bIns="{b}" anchor="t" '
         f'anchorCtr="0">{f}</wps:bodyPr></wps:wsp></a:graphicData></a:graphic>')
    return Page(note, [anchored(g, cx, cy, 1)], 'Text box')


SOFT = ('su\xadper\xadcal\xadi\xadfra\xadgil\xadis\xadtic ex\xadpi\xadal\xadi\xaddo\xadcious in\xadcom\xadpre\xadhen'
        '\xadsi\xadbil\xadi\xadties coun\xadter\xadrev\xado\xadlu\xadtion\xadar\xadies')
HEBREW = 'שלום עולם ומלואו'


def fam_forms():
    out = [
        Page('a character style with a glow 5 pt and a gradient at 0 degrees',
             [lead(R('styled run', style='EffectChar'))], 'Plain words'),
        Page('a paragraph style with a glow 5 pt and a 1 pt red outline (24 pt)',
             [P([R('A paragraph in the effect style', size=None, font=None, color=None)], style='EffectPara')],
             'A paragraph'),
        Page('a hyperlink\'s text (to a bookmark) with a 1 pt red outline',
             [P(['<w:bookmarkStart w:id="0" w:name="target"/>', R('The target. ', size=24), '<w:bookmarkEnd w:id="0"/>',
                 '<w:hyperlink w:anchor="target" w:history="1">'
                 + R('a hyperlink', size=24, color='0563C1', u='<w:u w:val="single"/>', fx=OUT) + '</w:hyperlink>',
                 R(' plain.', size=24)])], 'The target'),
    ]
    for w in (0.25, 0.5, 1, 2, 3, 6):
        o = bw.outline(int(round(w * EMU)), bw.solid('C00000'))
        out.append(Page(f'an outline {w:g} pt red on 11 pt text',
                        [P([R('Outlined', size=11, fx=o)]), lead(R('outlined run', size=11, fx=o), size=11)],
                        'Outlined'))
    out.append(Page('an outline 2 pt red at alpha 50000 (24 pt)',
                    [lead(R('outlined run', fx=bw.outline(25400, bw.solid('C00000', 50000))))], 'Plain words'))
    out.append(text_box('a text box (not WordArt), sized to its text, a run with a glow 8 pt',
                        [R('Text box', fx=GL8)], True))
    out.append(text_box('a text box (not WordArt), fixed 288 by 72 pt, a run with a gradient at 0 degrees',
                        [R('Text box', fx=G0)], False))
    out.append(shape_fill('a shape\'s gradient fill, red to blue at 0 and 100%, 0 degrees', ENDS))
    out.append(shape_fill('a shape\'s gradient fill, red to blue at 21 and 88%, 0 degrees', INSIDE))
    out.append(shape_fill('a shape\'s gradient fill, red, yellow, blue at 0, 50, 100%, 0 degrees', THREE))
    for note, stops in (('red to blue at 0 and 100%', ENDS), ('red to blue at 21 and 88%', INSIDE),
                        ('red, yellow, blue at 0, 50, 100%', THREE)):
        out.append(Page(f'Calibri 20 pt, a gradient {note}, 90 degrees',
                        [lead(R('gradient run', size=20, font='Calibri', fx=tf(grad(stops, 90))), size=20)],
                        'Plain words'))
    for ang in (0, 90):
        out.append(Page(f'Times New Roman 48 pt alone, a gradient red to blue at 0 and 100%, {ang} degrees',
                        [P([R('Gradient', size=48, font='Times New Roman', fx=tf(grad(ENDS, ang)))])], 'Gradient'))
    out.append(Page('Aptos 36 pt alone, a gradient red to blue at 0 and 60%, 0 degrees',
                    [P([R('Two stops', size=36, fx=tf(grad([(0, RED), (60000, BLUE)], 0)))])], 'Two stops'))
    out.append(Page('Aptos 36 pt alone, a gradient of four stops, red, yellow, green, blue at 0, 30, 70, 100%, 0 degrees',
                    [P([R('Four stops', size=36, fx=tf(grad([(0, RED), (30000, 'FFFF00'), (70000, '00B050'),
                                                              (100000, BLUE)], 0)))])], 'Four stops'))
    out.append(Page('soft hyphens in a gradient run at 0 degrees, one of them at a line\'s end',
                    [lead(R(SOFT, fx=G0))], 'Plain words'))
    out.append(Page('a gradient run at 0 degrees with characters Aptos lacks (a font fallback)',
                    [lead(R('stars ★★★ and ☎ here', fx=G0))], 'Plain words'))
    out.append(Page('a glow 5 pt on a run with characters Aptos lacks (a font fallback)',
                    [lead(R('stars ★★★ and ☎ here', fx=GL))], 'Plain words'))
    out.append(Page('Sylfaen bold (no bold face of its own), a gradient at 0 degrees, 28 pt',
                    [lead(R('simulated bold', size=28, font='Sylfaen', fx=G0, b=True, bCs=True), size=28)],
                    'Plain words'))
    out.append(Page('Sylfaen italic (no italic face of its own), a 1 pt red outline, 28 pt',
                    [lead(R('simulated italic', size=28, font='Sylfaen', fx=OUT, i=True, iCs=True), size=28)],
                    'Plain words'))
    for note, e in (('a gradient at 0 degrees', G0), ('a glow 5 pt', GL)):
        out.append(Page(f'a right-to-left paragraph, Hebrew in Arial 24 pt, {note} on its middle run',
                        [P([R('שלום ', font='Arial', rtl=True), R(HEBREW, font='Arial', fx=e, rtl=True),
                            R(' סוף', font='Arial', rtl=True)], jc='right', bidi=True)], ''))
    for note, e in (('a glow 8 pt', GL8), ('a gradient at 0 degrees', G0)):
        out.append(Page(f'a numbered paragraph (24 pt) whose mark has {note}, its text plain',
                        [P([R('The numbered paragraph.')], num=1, mark=rpr(24, fx=e)),
                         P([R('A plain paragraph after it.')])], 'The numbered'))
    out.append(Page('Aptos 14 pt under atLeast 30 pt, a line with no effect, then one with a glow 5 pt run and a '
                    'glow 18 pt run',
                    [P([R('Plain line with no effect.', size=14)], line=600, rule='atLeast'),
                     P([R('Plain ', size=14), R('glow five', size=14, fx=GL), R(' and ', size=14),
                        R('glow eighteen', size=14, fx=be.GL18), R(' plain', size=14)], line=600, rule='atLeast'),
                     P([R('Plain line after.', size=14)], line=600, rule='atLeast')], 'Plain line'))
    return out


# ------------------------------------------------------------------------------------------------- wb83

HYPHEN = ('internationalization incomprehensibilities counterrevolutionaries electroencephalographically '
          'uncharacteristically interdisciplinary')


def fam_hyphen():
    lang = '<w:lang w:val="en-US"/>'
    return [Page(f'{note}, a run of long words, automatic hyphenation on',
                 [lead(R(HYPHEN, fx=e, lang=lang))], 'Plain words') for note, e in (('gradient 0 degrees', G0),
                                                                                  ('glow 5 pt', GL))]


FILES = [
    ('wb80-body-lines', fam_lines, {}),
    ('wb81-body-room', fam_room, {}),
    ('wb82-body-forms', fam_forms, dict(styles=STYLES_WB82, numbering=True)),
    ('wb83-body-hyphen', fam_hyphen, dict(hyphenate=True)),
]

# ------------------------------------------------------------------------------------------------- wb84 (cr083-phase5b)

SET_B = 'cr083-phase5b'
IGNORABLE = (f'xmlns:w14="{W14}" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" '
             'mc:Ignorable="w14"')
LVL_DECIMAL = ('<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="%1."/>'
               '<w:lvlJc w:val="left"/><w:pPr><w:ind w:left="720" w:hanging="720"/></w:pPr>{rpr}</w:lvl>')
LVL_BULLET = ('<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="bullet"/><w:lvlText w:val=""/>'
              '<w:lvlJc w:val="left"/><w:pPr><w:ind w:left="720" w:hanging="720"/></w:pPr>'
              '<w:rPr><w:rFonts w:ascii="Symbol" w:hAnsi="Symbol" w:hint="default"/></w:rPr></w:lvl>')
# numId 1 a decimal level; 2 Word's Symbol bullet; 3 a decimal level whose own w:rPr has a glow 8 pt
NUMBERING_B = (f'{fx.DECL}<w:numbering xmlns:w="{fx.NS["w"]}" {IGNORABLE}>'
               f'<w:abstractNum w:abstractNumId="0"><w:multiLevelType w:val="singleLevel"/>{LVL_DECIMAL.format(rpr="")}'
               f'</w:abstractNum><w:abstractNum w:abstractNumId="1"><w:multiLevelType w:val="singleLevel"/>{LVL_BULLET}'
               f'</w:abstractNum><w:abstractNum w:abstractNumId="2"><w:multiLevelType w:val="singleLevel"/>'
               f'{LVL_DECIMAL.format(rpr="<w:rPr>" + GL8 + "</w:rPr>")}</w:abstractNum>'
               '<w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num><w:num w:numId="2"><w:abstractNumId w:val="1"/>'
               '</w:num><w:num w:numId="3"><w:abstractNumId w:val="2"/></w:num></w:numbering>')


def numbered(note, size=24, num=1, mark='', run_fx='', line=240, rule='auto', jc='left'):
    """A numbered paragraph (num 1; 2 a bullet; 3 the level with the glow; None unnumbered) whose mark has the effect
    `mark` and whose one run has `run_fx`, then a plain paragraph, both at `size` under one spacing rule: the page's
    label lines above the first give its room above, the paragraph after it the room below"""
    return Page(note, [P([R('The numbered paragraph.', size=size, fx=run_fx)], num=num, mark=rpr(size, fx=mark),
                         line=line, rule=rule, jc=jc),
                       P([R('A plain paragraph after it.', size=size)], line=line, rule=rule)], 'The numbered')


def fam_marks():
    """The room a numbered paragraph's line takes from its mark's effects (phase 5 findings section 6.2: wb82-30 got
    the glow's room above and none below, one probe), and which label effects Word paints: by effect kind (a glow's
    room is on both sides, a shadow's mostly below, a reflection's below only), at 12 pt, on a bullet, with the run's
    own effect beside the mark's, from the level's rPr, under each spacing rule, on a plain paragraph's mark (no
    label: is the room the label's or the mark's?), and on a centred number (drawn on the text's line)"""
    return [
        numbered('a numbered paragraph (24 pt) whose mark has a glow 8 pt, its text plain (wb82-30 again)', mark=GL8),
        numbered('a numbered paragraph (24 pt) whose mark has a shadow (blur 4, 3 pt at 45 degrees), its text plain',
                 mark=SH),
        numbered('a numbered paragraph (24 pt) whose mark has a reflection 4 pt down, its text plain', mark=RF4),
        numbered('a numbered paragraph (12 pt) whose mark has a glow 8 pt, its text plain', size=12, mark=GL8),
        numbered('a numbered paragraph (12 pt) whose mark has a reflection 4 pt down, its text plain', size=12,
                 mark=RF4),
        numbered('a bulleted paragraph (24 pt) whose mark has a glow 8 pt, its text plain', num=2, mark=GL8),
        numbered('a bulleted paragraph (24 pt) whose mark has a reflection 4 pt down, its text plain', num=2,
                 mark=RF4),
        numbered('a numbered paragraph (24 pt) whose mark and its text both have a glow 8 pt', mark=GL8, run_fx=GL8),
        numbered('a numbered paragraph (24 pt) whose text has a glow 8 pt, its mark plain', run_fx=GL8),
        numbered('a numbered paragraph (24 pt) whose level w:rPr has a glow 8 pt, its mark and text plain', num=3),
        numbered('a numbered paragraph (24 pt) whose mark has a glow 8 pt, under auto 1.15', mark=GL8, line=276),
        numbered('a numbered paragraph (24 pt) whose mark has a glow 8 pt, under atLeast 40 pt', mark=GL8, line=800,
                 rule='atLeast'),
        numbered('a numbered paragraph (24 pt) whose mark has a glow 8 pt, under exact 30 pt', mark=GL8, line=600,
                 rule='exact'),
        numbered('an unnumbered paragraph (24 pt) whose mark has a glow 8 pt, its text plain', num=None, mark=GL8),
        numbered('an unnumbered paragraph (24 pt) whose mark has a reflection 4 pt down, its text plain', num=None,
                 mark=RF4),
        numbered('a centred numbered paragraph (24 pt) whose mark has a glow 8 pt, its text plain', mark=GL8,
                 jc='center'),
    ]


FILES_B = [
    ('wb84-mark-room', fam_marks, dict(numbering=NUMBERING_B)),
]


def main(staging, which='a'):
    """which: 'a' the round cr083-phase5 (probes-body.tsv), 'b' cr083-phase5b (probes-body-b.tsv)"""
    set_name, files, tsv = (SET, FILES, 'probes-body.tsv') if which == 'a' else (SET_B, FILES_B, 'probes-body-b.tsv')
    manifest = ['set\tfile\tpage\tprobe\tkind\tmode\tnote\tsummary\ttext\n']
    sums, order, count = [], [], 0
    for name, fam, kw in files:
        prefix = name.split('-')[0]
        probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(fam())]
        plain = [x for x in probes if not x[1].header]
        probes = plain + [x for x in probes if x[1].header]          # the headed probes last, as document() needs
        for page, (pid, p) in enumerate(probes, 1):
            manifest.append(f'{set_name}\t{name}\t{page}\t{pid}\t{p.kind}\t15\t{p.note}\t{p.summary()}\t{p.text()}\n')
        count += len(probes)
        d = os.path.join(staging, set_name, name)
        os.makedirs(d, exist_ok=True)
        f = os.path.join(d, name + '.docx')
        package(f, probes, **kw)
        j = os.path.join(d, 'job.json')
        with open(j, 'w', newline='') as out:
            json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT, 'formats': ['docx', 'pdf']},
                      out)
        for x in (j, f):
            sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {set_name}/{name}/{os.path.basename(x)}\n')
        order.append(name + '\n')
    open(os.path.join(HERE, tsv), 'w', newline='').writelines(manifest)
    open(os.path.join(staging, set_name + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, set_name + '.order.txt'), 'w', newline='').writelines(order)
    print(set_name, len(order), 'files,', count, 'probes')


if __name__ == '__main__':
    main(*sys.argv[1:])
