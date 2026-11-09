# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-080 phase 0: purpose-built probes for how Word draws DrawingML shapes in a PDF: geometry (the 187 presets and
custom geometry), lines, arrowheads, fills, style references, and text rectangles. Jason, 2026-11-10: "CR-080 next"
(asked whether to start CR-080's phase 0, probes of every preset shape through the bridge). The route is CR-081's
(word-bridge; CR-081-notes/build_phase0.py), whose package parts and pictures this script reuses.

One probe to a page, the page's first paragraph naming it; probes.tsv, written beside this script, lists every probe
with its file and page. Each shape is anchored at a fixed place on its page (180 pt from the left, 252 pt from the top,
wrapNone), so that its frame in the PDF is known exactly, with no paragraph clip. Each file is staged once: Word was
deterministic over CR-081's 696 measured draws, each run twice (CR-081-notes/phase0-findings.md).

Round 1 (cr080-phase0): single shapes and connectors. Groups and canvases (wpg, wpc) are round 2, with their schemas.

usage: build_shapes.py <staging dir> [set ...]   (every set if none is named; probes.tsv always lists every set)"""
import hashlib, importlib.util, json, os, re, sys, zipfile
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('cr081', os.path.join(HERE, '..', 'CR-081-notes', 'build_phase0.py'))
fx = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(fx)
A = '{http://schemas.openxmlformats.org/drawingml/2006/main}'
PRESET_XML = os.path.join(fx.REPO, 'docx4j-core', 'src', 'main', 'resources', 'org', 'docx4j', 'model', 'shapes',
                          'presetShapeDefinitions.xml')

EMU = 12700
X0, Y0 = 180 * EMU, 252 * EMU                      # the shape's top left on the page
SQ = (100 * EMU, 100 * EMU)                         # CR-080's gate size: every preset's path within 0.1 pt at 100 by 100
WIDE = (144 * EMU, 72 * EMU)                        # not square, so that w, h and ss are told apart
BOX = (144 * EMU, 108 * EMU)
FILL = '<a:solidFill><a:srgbClr val="4472C4"/></a:solidFill>'
LINE = '<a:ln w="12700"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>'
NOLINE = '<a:ln><a:noFill/></a:ln>'

def presets():
    """(name, [(adj name, default value)]) in the definitions' order."""
    out = []
    for el in ET.parse(PRESET_XML).getroot():
        av = el.find(A + 'avLst')
        adj = [(g.get('name'), int(g.get('fmla').split()[1])) for g in (av if av is not None else [])]
        out.append((el.tag, adj))
    return out

class Shape:
    """One page: a wps:wsp anchored at (X0, Y0)."""
    def __init__(self, note, geom, size=WIDE, fill=FILL, ln=LINE, rot=0, flip='', cxn=False, style='', text=None,
                 body='', xfrm=True, theme=fx.THEME_2023, graphic='', requires='wps'):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def sp_pr(self):
        cx, cy = self.size
        a = (f' rot="{self.rot}"' if self.rot else '') + ''.join(f' {f}="1"' for f in self.flip.split() if f)
        x = f'<a:xfrm{a}><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm>' if self.xfrm else ''
        return f'{x}{self.geom}{self.fill}{self.ln}'

    def xml(self):
        return self.graphic or (self.geom + self.fill + self.ln + self.style + (self.body or ''))

def prst(name, adj=()):
    gd = ''.join(f'<a:gd name="{n}" fmla="val {v}"/>' for n, v in adj)
    return f'<a:prstGeom prst="{name}"><a:avLst>{gd}</a:avLst></a:prstGeom>'

def cust(paths, gd='', rect='<a:rect l="l" t="t" r="r" b="b"/>'):
    return (f'<a:custGeom><a:avLst/><a:gdLst>{gd}</a:gdLst><a:ahLst/><a:cxnLst/>{rect}'
            f'<a:pathLst>{paths}</a:pathLst></a:custGeom>')

def path(body, **a):
    return f'<a:path{fx._attrs(a)}>{body}</a:path>'

def pt(x, y):
    return f'<a:pt x="{x}" y="{y}"/>'

def mv(x, y): return f'<a:moveTo>{pt(x, y)}</a:moveTo>'
def ln_(x, y): return f'<a:lnTo>{pt(x, y)}</a:lnTo>'
def arc(wr, hr, st, sw): return f'<a:arcTo wR="{wr}" hR="{hr}" stAng="{st}" swAng="{sw}"/>'
CLOSE = '<a:close/>'

def line(w=12700, fill='<a:solidFill><a:srgbClr val="000000"/></a:solidFill>', extra='', **a):
    return f'<a:ln w="{w}"{fx._attrs(a)}>{fill}{extra}</a:ln>'

# ------------------------------------------------------------------------------------------------------ the families

def fam_presets(size):
    def f():
        return [Shape(f'{n}, default adjustments', prst(n), size=size) for n, _ in presets()]
    return f

def fam_presets_adj(factor):
    """Each adjustment scaled from its default (0 becomes 20000); Word's own pins apply where the value is past them."""
    def f():
        out = []
        for n, adj in presets():
            if adj:
                vals = [(a, int(v * factor) if v else 20000) for a, v in adj]
                out.append(Shape(f'{n}, ' + ', '.join(f'{a} {v}' for a, v in vals), prst(n, vals)))
        return out
    return f

# hd10, wd12 and wd32 are in neither the standard's list of built-in guides nor POI's (wd32 is POI's), but presets use
# hd10 (flowChartManualInput, flowChartPunchedTape) and wd12 (rtTriangle) undefined. They are probed in a file of their
# own, last in the order, in case Word refuses a custom geometry that names them.
GUIDES_X = ['l', 'wd32', 'wd12', 'wd10', 'wd8', 'wd6', 'wd5', 'wd4', 'wd3', 'wd2', 'hc', 'r']
GUIDES_Y = ['t', 'hd10', 'hd8', 'hd6', 'hd5', 'hd4', 'hd3', 'hd2', 'vc', 'b']
GUIDES_SS = ['ssd32', 'ssd16', 'ssd8', 'ssd6', 'ssd4', 'ssd2', 'ss', 'ls']
OPS = [  # (note, formula): each guide g places a vertex at x = g; the comment is the value expected in EMU at 144 by 72
    ('*/ w 2 5', '*/ w 2 5'), ('+- w 0 457200', '+- w 0 457200'), ('+/ w 0 2', '+/ w 0 2'), ('?: -1 w hc', '?: -1 w hc'),
    ('?: 1 wd4 hc', '?: 1 wd4 hc'), ('abs -914400', 'abs -914400'), ('cat2 w 3 4', 'cat2 w 3 4'),
    ('sat2 w 3 4', 'sat2 w 3 4'), ('cos w 3600000', 'cos w 3600000'), ('sin w 1800000', 'sin w 1800000'),
    ('tan 457200 2700000', 'tan 457200 2700000'), ('max 300000 w', 'max 300000 w'), ('min 300000 w', 'min 300000 w'),
    ('mod 548640 731520 0', 'mod 548640 731520 0'), ('pin 0 2000000 w', 'pin 0 2000000 w'),
    ('sqrt 836127360000', 'sqrt 836127360000'),
    ('val 600000', 'val 600000'),
]

def vlines(gs):
    return path(''.join(mv(g, 't') + ln_(g, 'b') for g in gs), fill='none')

def fam_custgeom_edge():
    """Last in the order: the built-in guides (hd10, wd12 and wd32 among them), and sqrt of a negative number, which
    [MS-OI29500] 2.1.1311 says Office takes as sqrt(|x|)."""
    tri = lambda g: path(mv('l', 'b') + ln_('l', 't') + ln_(g, 'vc') + CLOSE)
    return [Shape('built-in x guides: a vertical line at each of ' + ' '.join(GUIDES_X), cust(vlines(GUIDES_X)), fill=''),
            Shape('built-in y guides: a horizontal line at each of ' + ' '.join(GUIDES_Y),
                  cust(path(''.join(mv('l', g) + ln_('r', g) for g in GUIDES_Y), fill='none')), fill=''),
            Shape('built-in ss guides along x: ' + ' '.join(GUIDES_SS), cust(vlines(GUIDES_SS)), fill=''),
            Shape('guide sqrt of a negative (Office: |x|): a vertex at x = g',
                  cust(tri('g'), gd='<a:gd name="g" fmla="sqrt -836127360000"/>'))]

def fam_custgeom():
    out = []
    tri = lambda g: path(mv('l', 'b') + ln_('l', 't') + ln_(g, 'vc') + CLOSE)
    for note, f in OPS:
        out.append(Shape(f'guide {note}: a vertex at x = g', cust(tri('g'), gd=f'<a:gd name="g" fmla="{f}"/>')))
    out.append(Shape('at2 into cos: x = cos(w, at2(1, 1)) = w cos 45', cust(tri('g'), gd='<a:gd name="a" fmla="at2 1 1"/>'
                                                                         '<a:gd name="g" fmla="cos w a"/>')))
    arcs = [('quarter: stAng 0 swAng 90', 'hc', 'vc', 'wd4', 'hd4', 0, 5400000),
            ('negative sweep: stAng 0 swAng -90', 'hc', 'vc', 'wd4', 'hd4', 0, -5400000),
            ('full turn: swAng 360', 'hc', 'vc', 'wd4', 'hd4', 0, 21600000),
            ('past a full turn: swAng 450', 'hc', 'vc', 'wd4', 'hd4', 0, 27000000),
            ('elliptical, from 45 degrees: wR 2 hR, stAng 45 swAng 45', 'hc', 'vc', 'wd4', 'hd4', 2700000, 2700000),
            ('elliptical, stAng 30 swAng 120', 'hc', 'vc', 'wd3', 'hd6', 1800000, 7200000),
            ('circular, stAng 200 swAng 100', 'hc', 'vc', 'hd3', 'hd3', 12000000, 6000000)]
    for note, x, y, wr, hr, st, sw in arcs:
        out.append(Shape(f'arcTo {note}', cust(path(mv(x, y) + arc(wr, hr, st, sw), fill='none')), fill=''))
    out += [
        Shape('quadBezTo', cust(path(mv('l', 'b') + f'<a:quadBezTo>{pt("hc", "t")}{pt("r", "b")}</a:quadBezTo>', fill='none')), fill=''),
        Shape('cubicBezTo', cust(path(mv('l', 'b') + f'<a:cubicBezTo>{pt("wd4", "t")}{pt("r", "t")}{pt("r", "b")}</a:cubicBezTo>', fill='none')), fill=''),
        Shape('an open path filled (no close)', cust(path(mv('l', 'b') + ln_('hc', 't') + ln_('r', 'b')))),
        Shape('a closed path, stroked', cust(path(mv('l', 'b') + ln_('hc', 't') + ln_('r', 'b') + CLOSE)), fill=''),
        Shape('path w 100 h 100 in a 144 by 72 shape: a triangle at path (10,90) (50,10) (90,90)',
              cust(path(mv(10, 90) + ln_(50, 10) + ln_(90, 90) + CLOSE, w=100, h=100))),
        Shape('stroke 0: filled, not stroked', cust(path(mv('l', 'b') + ln_('hc', 't') + ln_('r', 'b') + CLOSE, stroke=0))),
        Shape('two subpaths in one path, the inner reversed (a hole under nonzero?)',
              cust(path(mv('l', 't') + ln_('r', 't') + ln_('r', 'b') + ln_('l', 'b') + CLOSE
                        + mv('wd4', 'hd4') + ln_('wd4', '3hd4') + ln_('3wd4', '3hd4') + ln_('3wd4', 'hd4') + CLOSE),
                   gd='<a:gd name="3hd4" fmla="*/ h 3 4"/><a:gd name="3wd4" fmla="*/ w 3 4"/>')),
        Shape('two subpaths in one path, the inner the same way round',
              cust(path(mv('l', 't') + ln_('r', 't') + ln_('r', 'b') + ln_('l', 'b') + CLOSE
                        + mv('wd4', 'hd4') + ln_('3wd4', 'hd4') + ln_('3wd4', '3hd4') + ln_('wd4', '3hd4') + CLOSE),
                   gd='<a:gd name="3hd4" fmla="*/ h 3 4"/><a:gd name="3wd4" fmla="*/ w 3 4"/>')),
    ]
    bands = ''.join(path(mv(f'x{i}', 't') + ln_(f'x{i + 1}', 't') + ln_(f'x{i + 1}', 'b') + ln_(f'x{i}', 'b') + CLOSE, fill=m)
                    for i, m in enumerate(['norm', 'lighten', 'lightenLess', 'darken', 'darkenLess', 'none']))
    gd = ''.join(f'<a:gd name="x{i}" fmla="*/ w {i} 6"/>' for i in range(7))
    out.append(Shape('fill modes, a band each: norm lighten lightenLess darken darkenLess none', cust(bands, gd=gd)))
    return out

DASHES = ['solid', 'dot', 'dash', 'lgDash', 'dashDot', 'lgDashDot', 'lgDashDotDot', 'sysDash', 'sysDot', 'sysDashDot',
          'sysDashDotDot']

def fam_lines():
    r = prst('rect')
    out = [Shape(f'line width {w} pt', r, fill='', ln=line(int(w * EMU))) for w in (0, 0.25, 0.5, 0.75, 1, 1.5, 2.25, 4.5, 6, 12)]
    for w, cap in ((1, 'flat'), (4, 'flat'), (4, 'rnd')):
        out += [Shape(f'prstDash {d}, {w} pt, cap {cap}', r, fill='', ln=line(w * EMU, cap=cap,
                      extra=f'<a:prstDash val="{d}"/>')) for d in DASHES]
    out += [Shape('prstDash dash, 4 pt, no cap stated', r, fill='', ln=line(4 * EMU, extra='<a:prstDash val="dash"/>'))]
    out += [Shape(f'custDash {n}, 4 pt, cap flat', r, fill='', ln=line(4 * EMU, cap='flat', extra=f'<a:custDash>{d}</a:custDash>'))
            for n, d in (('d 300% sp 100%', '<a:ds d="300000" sp="100000"/>'),
                         ('d 100% sp 200%, d 400% sp 100%', '<a:ds d="100000" sp="200000"/><a:ds d="400000" sp="100000"/>'))]
    out += [Shape(f'cap {c}, 8 pt, on a line', prst('line'), fill='', ln=line(8 * EMU, cap=c), cxn=True) for c in ('flat', 'rnd', 'sq')]
    out += [Shape(f'join {j}, 8 pt, on a triangle', prst('triangle'), fill='', ln=line(8 * EMU, extra=x))
            for j, x in (('round', '<a:round/>'), ('bevel', '<a:bevel/>'), ('miter lim 800%', '<a:miter lim="800000"/>'),
                         ('miter lim 100%', '<a:miter lim="100000"/>'), ('not stated', ''))]
    out += [Shape(f'cmpd {c}, 9 pt', r, fill='', ln=line(9 * EMU, cmpd=c)) for c in ('sng', 'dbl', 'thickThin', 'thinThick', 'tri')]
    out += [Shape(f'algn {a}, 9 pt', r, fill='', ln=line(9 * EMU, algn=a)) for a in ('ctr', 'in')]
    out += [Shape('line colour at alpha 50%, 6 pt', r, fill='', ln=line(6 * EMU, fill='<a:solidFill><a:srgbClr val="C00000"><a:alpha val="50000"/></a:srgbClr></a:solidFill>')),
            Shape('a gradient line, 6 pt', r, fill='', ln=line(6 * EMU, fill=GRAD2.format(ang=0, scaled=0))),
            Shape('a pattern line (dkDnDiag), 6 pt', r, fill='', ln=line(6 * EMU, fill=patt('dkDnDiag'))),
            Shape('noFill line', r, ln='<a:ln w="38100"><a:noFill/></a:ln>'),
            Shape('no a:ln at all', r, ln='')]
    return out

ARROWS = ['triangle', 'stealth', 'diamond', 'oval', 'arrow']
SIZES = ['sm', 'med', 'lg']

def arrow_line(w, head='', tail=''):
    return f'<a:ln w="{w}"><a:solidFill><a:srgbClr val="000000"/></a:solidFill>{head}{tail}</a:ln>'

def end(tag, t, w='med', l='med'):
    return f'<a:{tag} type="{t}" w="{w}" len="{l}"/>'

def fam_arrows():
    c = prst('straightConnector1')
    out = [Shape(f'tailEnd {t} w {w} len {l}, 1 pt', c, fill='', cxn=True, ln=arrow_line(EMU, tail=end('tailEnd', t, w, l)))
           for t in ARROWS for w in SIZES for l in SIZES]
    out += [Shape(f'tailEnd {t} w {s} len {s}, 4.5 pt', c, fill='', cxn=True, ln=arrow_line(int(4.5 * EMU), tail=end('tailEnd', t, s, s)))
            for t in ARROWS for s in SIZES]
    out += [Shape('headEnd triangle only, 2 pt', c, fill='', cxn=True, ln=arrow_line(2 * EMU, head=end('headEnd', 'triangle'))),
            Shape('headEnd oval and tailEnd stealth, 2 pt', c, fill='', cxn=True,
                  ln=arrow_line(2 * EMU, head=end('headEnd', 'oval'), tail=end('tailEnd', 'stealth')))]
    out += [Shape(f'tailEnd triangle, 2 pt, {f}', c, fill='', cxn=True, flip=f, ln=arrow_line(2 * EMU, tail=end('tailEnd', 'triangle')))
            for f in ('flipH', 'flipV', 'flipH flipV')]
    out += [Shape(f'{p}, tailEnd triangle, 2 pt', prst(p), fill='', cxn=cx, ln=arrow_line(2 * EMU, tail=end('tailEnd', 'triangle')))
            for p, cx in (('line', True), ('bentConnector3', True), ('curvedConnector3', True), ('arc', False))]
    for p in ('bentConnector2', 'bentConnector4', 'bentConnector5', 'curvedConnector2', 'curvedConnector4', 'curvedConnector5'):
        out.append(Shape(f'{p}, 2 pt', prst(p), fill='', cxn=True, ln=arrow_line(2 * EMU)))
    return out

GRAD2 = ('<a:gradFill rotWithShape="1"><a:gsLst><a:gs pos="0"><a:srgbClr val="4472C4"/></a:gs>'
         '<a:gs pos="100000"><a:srgbClr val="FFC000"/></a:gs></a:gsLst><a:lin ang="{ang}" scaled="{scaled}"/></a:gradFill>')

def grad(stops, shade, rot=1, flip='', tile=''):
    gs = ''.join(f'<a:gs pos="{p}">{c}</a:gs>' for p, c in stops)
    f = f' flip="{flip}"' if flip else ''
    return f'<a:gradFill{f} rotWithShape="{rot}"><a:gsLst>{gs}</a:gsLst>{shade}{tile}</a:gradFill>'

BLUE, GOLD = '<a:srgbClr val="4472C4"/>', '<a:srgbClr val="FFC000"/>'
PATTERNS = ['pct5', 'pct10', 'pct20', 'pct25', 'pct30', 'pct40', 'pct50', 'pct60', 'pct70', 'pct75', 'pct80', 'pct90',
            'horz', 'vert', 'ltHorz', 'ltVert', 'dkHorz', 'dkVert', 'narHorz', 'narVert', 'dashHorz', 'dashVert', 'cross',
            'dnDiag', 'upDiag', 'ltDnDiag', 'ltUpDiag', 'dkDnDiag', 'dkUpDiag', 'wdDnDiag', 'wdUpDiag', 'dashDnDiag',
            'dashUpDiag', 'diagCross', 'smCheck', 'lgCheck', 'smGrid', 'lgGrid', 'dotGrid', 'smConfetti', 'lgConfetti',
            'horzBrick', 'diagBrick', 'solidDmnd', 'openDmnd', 'dotDmnd', 'plaid', 'sphere', 'weave', 'divot', 'shingle',
            'wave', 'trellis', 'zigZag']

def patt(p):
    return f'<a:pattFill prst="{p}"><a:fgClr><a:srgbClr val="4472C4"/></a:fgClr><a:bgClr><a:srgbClr val="FFFFFF"/></a:bgClr></a:pattFill>'

def blip(inner):
    return f'<a:blipFill rotWithShape="1"><a:blip r:embed="rId10"/>{inner}</a:blipFill>'

def fam_fills():
    r = prst('rect')
    out = [Shape('solid at alpha 50%', r, ln=NOLINE, fill='<a:solidFill><a:srgbClr val="4472C4"><a:alpha val="50000"/></a:srgbClr></a:solidFill>'),
           Shape('solid accent1 lumMod 75%', r, ln=NOLINE, fill='<a:solidFill><a:schemeClr val="accent1"><a:lumMod val="75000"/></a:schemeClr></a:solidFill>')]
    for ang in (0, 2700000, 5400000, 8100000):
        for sc in (1, 0):
            out.append(Shape(f'linear gradient ang {ang // 60000} scaled {sc}', r, ln=NOLINE,
                             fill=grad([(0, BLUE), (100000, GOLD)], f'<a:lin ang="{ang}" scaled="{sc}"/>')))
    out += [Shape('linear gradient, three stops (0, 30%, 100%)', r, ln=NOLINE,
                  fill=grad([(0, BLUE), (30000, '<a:srgbClr val="FFFFFF"/>'), (100000, GOLD)], '<a:lin ang="0" scaled="1"/>')),
            Shape('linear gradient, stops at alpha 100% and 0%', r, ln=NOLINE,
                  fill=grad([(0, BLUE), (100000, '<a:srgbClr val="4472C4"><a:alpha val="0"/></a:srgbClr>')], '<a:lin ang="0" scaled="1"/>'))]
    out += [Shape(f'linear gradient ang 0, shape turned 30 degrees, rotWithShape {rw}', r, ln=NOLINE, rot=1800000,
                  fill=grad([(0, BLUE), (100000, GOLD)], '<a:lin ang="0" scaled="1"/>', rot=rw)) for rw in (1, 0)]
    out += [Shape(f'linear gradient, tileRect r -50%, flip {fl}', r, ln=NOLINE,
                  fill=grad([(0, BLUE), (100000, GOLD)], '<a:lin ang="0" scaled="1"/>', flip=fl, tile='<a:tileRect r="-50000"/>'))
            for fl in ('none', 'x')]
    for p in ('circle', 'rect', 'shape'):
        for g in ('rect', 'ellipse'):
            out.append(Shape(f'path gradient {p}, from the centre, on a {g}', prst(g), ln=NOLINE,
                             fill=grad([(0, BLUE), (100000, GOLD)], f'<a:path path="{p}"><a:fillToRect l="50000" t="50000" r="50000" b="50000"/></a:path>')))
    out += [Shape('path gradient circle, from the top left corner', r, ln=NOLINE,
                  fill=grad([(0, BLUE), (100000, GOLD)], '<a:path path="circle"><a:fillToRect r="100000" b="100000"/></a:path>'))]
    out += [Shape(f'pattern {p}', r, ln=NOLINE, fill=patt(p)) for p in PATTERNS]
    out += [Shape('picture fill, stretched', r, ln=NOLINE, fill=blip('<a:stretch><a:fillRect/></a:stretch>')),
            Shape('picture fill, stretched to a rect inset 10% each side', r, ln=NOLINE,
                  fill=blip('<a:stretch><a:fillRect l="10000" t="10000" r="10000" b="10000"/></a:stretch>')),
            Shape('picture fill, cropped (srcRect l 25%), stretched', r, ln=NOLINE,
                  fill=blip('<a:srcRect l="25000"/><a:stretch><a:fillRect/></a:stretch>')),
            Shape('picture fill, tiled at 25%, algn tl', r, ln=NOLINE,
                  fill=blip('<a:tile tx="0" ty="0" sx="25000" sy="25000" flip="none" algn="tl"/>')),
            Shape('picture fill, tiled at 25%, flip xy, algn ctr', r, ln=NOLINE,
                  fill=blip('<a:tile tx="0" ty="0" sx="25000" sy="25000" flip="xy" algn="ctr"/>')),
            Shape('picture fill, tiled at 25%, offset tx 9 pt ty 4.5 pt', r, ln=NOLINE,
                  fill=blip('<a:tile tx="114300" ty="57150" sx="25000" sy="25000" flip="none" algn="tl"/>')),
            Shape('picture fill on an ellipse, stretched', prst('ellipse'), ln=NOLINE, fill=blip('<a:stretch><a:fillRect/></a:stretch>')),
            Shape('noFill', r, fill='<a:noFill/>')]
    return out

def style(ln=2, fill=1, eff=0):
    return (f'<wps:style><a:lnRef idx="{ln}"><a:schemeClr val="accent1"><a:shade val="50000"/></a:schemeClr></a:lnRef>'
            f'<a:fillRef idx="{fill}"><a:schemeClr val="accent1"/></a:fillRef><a:effectRef idx="{eff}"><a:schemeClr val="accent1"/></a:effectRef>'
            '<a:fontRef idx="minor"><a:schemeClr val="lt1"/></a:fontRef></wps:style>')

def fam_styles(theme):
    def f():
        r = prst('rect')
        out = [Shape(f'style lnRef {i}, fillRef 1, no fill or line in spPr', r, fill='', ln='', style=style(ln=i), theme=theme) for i in (0, 1, 2, 3)]
        out += [Shape(f'style fillRef {i}, lnRef 2, no fill or line in spPr', r, fill='', ln='', style=style(fill=i), theme=theme)
                for i in (0, 2, 3, 1001, 1002, 1003)]
        out += [Shape('style fillRef 1 lnRef 2, spPr fill and line override them', r, style=style(), theme=theme),
                Shape('style lnRef 2, spPr line with only w (6 pt): the style colour stays?', r, fill='',
                      ln='<a:ln w="76200"/>', style=style(), theme=theme)]
        return out
    return f

def fam_text(anchor, jc):
    def f():
        return [Shape(f'{n}, text "X" Arial 8 pt, anchor {anchor}, {jc}, insets 0', prst(n), size=BOX, text='X',
                      body=f'anchor="{anchor}"', ln=NOLINE, fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>')
                for n, _ in presets()] + [
               Shape(f'rect with no xfrm in its spPr (2.1.1315), anchor {anchor}, {jc}', prst('rect'), size=BOX, text='X',
                     body=f'anchor="{anchor}"', ln=NOLINE, xfrm=False, fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>')]
    f.jc = jc
    return f

def fam_text_part(anchor, jc, part, parts=4):
    """Round 1's text family split, for the bisection after fp60's save failed ("A file error has occurred"): a quarter
    of the presets, without the no-xfrm probe."""
    def f():
        ps = presets()
        k = -(-len(ps) // parts)
        return [Shape(f'{n}, text "X" Arial 8 pt, anchor {anchor}, {jc}, insets 0', prst(n), size=BOX, text='X',
                      body=f'anchor="{anchor}"', ln=NOLINE, fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>')
                for n, _ in ps[part * k:(part + 1) * k]]
    f.jc = jc
    return f

def fam_no_xfrm(text):
    """A shape whose spPr has no xfrm ([MS-OI29500] 2.1.1315): the suspect in fp60's failed save, so last."""
    def f():
        if not text:
            return [Shape('rect with no xfrm in its spPr, no text', prst('rect'), size=BOX, xfrm=False)]
        return [Shape(f'rect with no xfrm in its spPr (2.1.1315), text anchor {a}, {j}', prst('rect'), size=BOX, text='X',
                      body=f'anchor="{a}"', ln=NOLINE, xfrm=False, fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>')
                for a, j in (('t', 'left'),)]
    return f

def fam_pair(text):
    """The strict pair for fp79's failed save (Jason, 2026-11-10: "stage the strict two-file pair for the save
    failure"): the same shape, with no xfrm, the same fill, line and bodyPr, with and without a text box; no label, so
    that the two document parts differ only in the wps:txbx."""
    def f():
        return [Shape(f'rect, no xfrm, {"with" if text else "without"} a text box', prst('rect'), size=BOX,
                      text='X' if text else None, body='anchor="t"', ln=NOLINE, xfrm=False,
                      fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>')]
    f.bare = True
    return f

GROUP_NS = {'wpg': 'http://schemas.microsoft.com/office/word/2010/wordprocessingGroup',
            'wpc': 'http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas'}
URI_WPG, URI_WPC = GROUP_NS['wpg'], GROUP_NS['wpc']
COLOURS = ['4472C4', 'ED7D31', '70AD47', 'FFC000', '7030A0']

def xfrm(off, ext, rot=0, flip='', ch=None):
    a = (f' rot="{rot}"' if rot else '') + ''.join(f' {f}="1"' for f in flip.split() if f)
    c = f'<a:chOff x="{ch[0]}" y="{ch[1]}"/><a:chExt cx="{ch[2]}" cy="{ch[3]}"/>' if ch else ''
    return f'<a:xfrm{a}><a:off x="{off[0]}" y="{off[1]}"/><a:ext cx="{ext[0]}" cy="{ext[1]}"/>{c}</a:xfrm>'

def child(i, off, ext, geom='rtTriangle', rot=0, flip='', fill=None, text=None, cxn=None):
    """A wps:wsp inside a group or canvas: its own cNvPr (id i), its place in its parent's child space."""
    f = '<a:grpFill/>' if fill == 'grp' else f'<a:solidFill><a:srgbClr val="{fill or COLOURS[i % 5]}"/></a:solidFill>'
    nv = (f'<wps:cNvCnPr><a:stCxn id="{cxn[0]}" idx="{cxn[1]}"/><a:endCxn id="{cxn[2]}" idx="{cxn[3]}"/></wps:cNvCnPr>'
          if cxn else '<wps:cNvSpPr/>')
    if cxn:
        f = ''
    ln = LINE if not cxn else arrow_line(2 * EMU, tail=end('tailEnd', 'triangle'))
    tx = (f'<wps:txbx><w:txbxContent><w:p><w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/><w:sz w:val="16"/></w:rPr>'
          f'<w:t>{text}</w:t></w:r></w:p></w:txbxContent></wps:txbx>') if text else ''
    body = '<wps:bodyPr lIns="0" tIns="0" rIns="0" bIns="0" anchor="t"><a:noAutofit/></wps:bodyPr>' if text else '<wps:bodyPr/>'
    return (f'<wps:wsp><wps:cNvPr id="{i}" name="Child {i}"/>{nv}<wps:spPr>{xfrm(off, ext, rot, flip)}{prst(geom)}{f}{ln}'
            f'</wps:spPr>{tx}{body}</wps:wsp>')

def grp(i, off, ext, ch, kids, rot=0, flip='', fill=''):
    return (f'<wpg:grpSp><wpg:cNvPr id="{i}" name="Group {i}"/><wpg:cNvGrpSpPr/><wpg:grpSpPr>{xfrm(off, ext, rot, flip, ch)}'
            f'{fill}</wpg:grpSpPr>{kids}</wpg:grpSp>')

def wgp(kids, ch, rot=0, flip='', fill='', size=BOX):
    return (f'<a:graphic><a:graphicData uri="{URI_WPG}"><wpg:wgp><wpg:cNvGrpSpPr/><wpg:grpSpPr>'
            f'{xfrm((0, 0), size, rot, flip, ch)}{fill}</wpg:grpSpPr>{kids}</wpg:wgp></a:graphicData></a:graphic>')

def wpc(kids, bg='', whole=''):
    return (f'<a:graphic><a:graphicData uri="{URI_WPC}"><wpc:wpc>{bg}{whole}{kids}</wpc:wpc></a:graphicData></a:graphic>')

def gshape(note, graphic, kind='wpg', size=BOX):
    return Shape(note, '', size=size, fill='', ln='', graphic=graphic, requires=kind)

W, H = BOX
HALF = (W // 2, H)
PAIR = child(101, (0, 0), HALF) + child(102, (W // 2, 0), HALF, geom='rect')     # a right triangle and a rect, side by side
ID = (0, 0, W, H)

def fam_transforms():
    """Single shapes turned and flipped (round 1 had almost none): an asymmetric preset, so that a flip shows."""
    out = [Shape(f'rtTriangle, rot {r // 60000}', prst('rtTriangle'), size=BOX, rot=r) for r in (1800000, 5400000, 10800000, 16200000)]
    out += [Shape(f'rtTriangle, {f}', prst('rtTriangle'), size=BOX, flip=f) for f in ('flipH', 'flipV', 'flipH flipV')]
    out += [Shape('rtTriangle, rot 30, flipH', prst('rtTriangle'), size=BOX, rot=1800000, flip='flipH'),
            Shape('rect with text, rot 30', prst('rect'), size=BOX, rot=1800000, text='X', body='anchor="t"', ln=NOLINE,
                  fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>'),
            Shape('rect with text, rot 30, bodyPr upright 1', prst('rect'), size=BOX, rot=1800000, text='X',
                  body='anchor="t" upright="1"', ln=NOLINE, fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>'),
            Shape('rect with text, flipH', prst('rect'), size=BOX, flip='flipH', text='X', body='anchor="t"', ln=NOLINE,
                  fill='<a:solidFill><a:srgbClr val="DEEAF6"/></a:solidFill>')]
    return out

def fam_groups():
    out = [gshape('group: a right triangle and a rect, child space = the group', wgp(PAIR, ID)),
           gshape('group: child space twice the group (children at half size)', wgp(PAIR, (0, 0, 2 * W, 2 * H))),
           gshape('group: child offset (1in, 0.5in), children moved by minus it', wgp(PAIR, (914400, 457200, W, H)))]
    out += [gshape(f'group rot {r // 60000}', wgp(PAIR, ID, rot=r)) for r in (1800000, 5400000)]
    out += [gshape(f'group {f}', wgp(PAIR, ID, flip=f)) for f in ('flipH', 'flipV', 'flipH flipV')]
    out += [gshape('group rot 30, flipH', wgp(PAIR, ID, rot=1800000, flip='flipH')),
            gshape('group: child rot 45', wgp(child(101, (W // 4, H // 4), HALF, rot=2700000), ID)),
            gshape('group flipH: child flipV', wgp(child(101, (0, 0), HALF, flip='flipV') + child(102, (W // 2, 0), HALF, geom='rect'), ID, flip='flipH'))]
    inner = child(104, (0, 0), (W, H))
    g3 = grp(103, (0, 0), (W, H), (0, 0, W, H), inner, rot=900000)
    g2 = grp(102, (W // 4, 0), (W // 2, H), (0, 0, W, H), g3, flip='flipH')
    out += [gshape('nested three deep: group rot 20 > grpSp flipH, half width > grpSp rot 15 > rtTriangle',
                   wgp(g2, ID, rot=1200000)),
            gshape('nested: grpSp child space twice its extent > rtTriangle',
                   wgp(grp(102, (0, 0), (W, H), (0, 0, 2 * W, 2 * H), child(103, (0, 0), (W, H))), ID)),
            gshape('group fill: the group solidFill 70AD47, a child grpFill', wgp(child(101, (0, 0), HALF, fill='grp')
                   + child(102, (W // 2, 0), HALF, geom='rect'), ID, fill='<a:solidFill><a:srgbClr val="70AD47"/></a:solidFill>')),
            gshape('group: a child with text', wgp(child(101, (0, 0), (W, H), geom='rect', text='X', fill='DEEAF6'), ID)),
            gshape('group: a picture child', wgp(
                '<pic:pic><pic:nvPicPr><pic:cNvPr id="101" name="photo.png"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill>'
                '<a:blip r:embed="rId10"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr>'
                f'{xfrm((0, 0), HALF)}<a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>'
                + child(102, (W // 2, 0), HALF, geom='rect'), ID)),
            gshape('group: two rects and a straight connector from the first (site 3) to the second (site 1)', wgp(
                child(101, (0, 0), (W // 4, H // 4), geom='rect') + child(102, (3 * W // 4, 3 * H // 4), (W // 4, H // 4), geom='rect')
                + child(103, (W // 8, H // 4), (5 * W // 8, H // 2), geom='straightConnector1', cxn=(101, 2, 102, 0)), ID))]
    return out

def fam_canvas():
    two = child(101, (W // 8, H // 8), (W // 4, H // 4), geom='rect') + child(102, (5 * W // 8, 5 * H // 8), (W // 4, H // 4), geom='ellipse')
    bg = '<wpc:bg><a:solidFill><a:srgbClr val="F2F2F2"/></a:solidFill></wpc:bg>'
    whole = '<wpc:whole><a:ln w="12700"><a:solidFill><a:srgbClr val="C00000"/></a:solidFill></a:ln></wpc:whole>'
    out = [gshape('canvas: background F2F2F2, outline C00000, a rect and an ellipse', wpc(two, bg, whole), 'wpc'),
           gshape('canvas: no background or outline, a rect and an ellipse', wpc(two), 'wpc'),
           gshape('canvas: background only, empty', wpc('', bg), 'wpc')]
    for c in ('straightConnector1', 'bentConnector3', 'curvedConnector3'):
        out.append(gshape(f'canvas: {c} from the rect (site 3) to the ellipse (site 1)', wpc(
            two + child(103, (3 * W // 8, H // 4), (3 * W // 8, H // 2), geom=c, cxn=(101, 3, 102, 1)), bg), 'wpc'))
    out += [gshape('canvas: a group inside (rot 30)', wpc(
                '<wpg:wgp><wpg:cNvGrpSpPr/><wpg:grpSpPr>' + xfrm((W // 4, H // 4), (W // 2, H // 2), 1800000, '', (0, 0, W, H))
                + '</wpg:grpSpPr>' + PAIR + '</wpg:wgp>', bg), 'wpc'),
            gshape('canvas: a shape with text', wpc(child(101, (W // 8, H // 8), (3 * W // 4, 3 * H // 4), geom='rect', text='X', fill='DEEAF6'), bg), 'wpc')]
    return out

# --------------------------------------------------------------------------------------------------------- the XML

def label(pid, s, first):
    ppr = '' if first else '<w:pPr><w:pageBreakBefore/></w:pPr>'
    what = fx.describe(s.xml())
    what = what if len(what) < 400 else what[:400] + ' ...'
    return f'<w:p>{ppr}{fx.run(f"{pid}: {s.note}.")}</w:p><w:p>{fx.run(what)}</w:p>'

def wsp(s, jc):
    if s.graphic:
        return s.graphic
    body = (f'<wps:bodyPr rot="0" vert="horz" wrap="square" lIns="0" tIns="0" rIns="0" bIns="0" {s.body}><a:noAutofit/></wps:bodyPr>'
            if s.text or s.body else '<wps:bodyPr/>')
    txbx = ''
    if s.text:
        txbx = (f'<wps:txbx><w:txbxContent><w:p><w:pPr><w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/>'
                f'<w:jc w:val="{jc}"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/><w:color w:val="000000"/>'
                f'<w:sz w:val="16"/></w:rPr><w:t>{escape(s.text)}</w:t></w:r></w:p></w:txbxContent></wps:txbx>')
    nv = '<wps:cNvCnPr/>' if s.cxn else '<wps:cNvSpPr/>'
    return (f'<a:graphic><a:graphicData uri="{fx.URI_WPS}"><wps:wsp>{nv}<wps:spPr>{s.sp_pr()}</wps:spPr>{s.style}{txbx}'
            f'{body}</wps:wsp></a:graphicData></a:graphic>')

def child_ids(g, n):
    """A group's or canvas's child ids (101 and up, with the connectors' references to them) made unique in the
    document: page n's become n * 1000 + the id."""
    return re.sub(r'\bid="(1\d\d)"', lambda m: f'id="{n * 1000 + int(m.group(1))}"', g)

def drawing(s, n, jc):
    cx, cy = s.size
    d = (f'<wp:anchor distT="0" distB="0" distL="0" distR="0" simplePos="0" relativeHeight="{251658240 + n}" behindDoc="0" '
         'locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>'
         f'<wp:positionH relativeFrom="page"><wp:posOffset>{X0}</wp:posOffset></wp:positionH>'
         f'<wp:positionV relativeFrom="page"><wp:posOffset>{Y0}</wp:posOffset></wp:positionV>'
         f'<wp:extent cx="{cx}" cy="{cy}"/><wp:effectExtent l="0" t="0" r="0" b="0"/><wp:wrapNone/>'
         f'<wp:docPr id="{n}" name="Shape {n}"/><wp:cNvGraphicFramePr/>{child_ids(wsp(s, jc), n)}</wp:anchor>')
    return (f'<w:r><mc:AlternateContent><mc:Choice Requires="{s.requires}"><w:drawing>{d}</w:drawing></mc:Choice>'
            '</mc:AlternateContent></w:r>')

def document(shapes, jc, bare=False):
    ns = dict(fx.NS)
    if any(s.requires != 'wps' for _, s in shapes):
        ns.update(GROUP_NS)
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in ns.items())
    parts = []
    if bare:                                     # one shape, no label: two files can differ only in the shape
        parts = [f'<w:p>{drawing(s, i + 1, jc)}</w:p>' for i, (_, s) in enumerate(shapes)]
        shapes = []
    for i, (pid, s) in enumerate(shapes):
        parts.append(label(pid, s, i == 0).replace('</w:p><w:p>', '</w:p><w:p>' + drawing(s, i + 1, jc), 1))
    return f'{fx.DECL}<w:document {xmlns}><w:body>{"".join(parts)}{fx.SECT}</w:body></w:document>'

def package(path, shapes, theme, jc, bare=False):
    uses_pic = any('r:embed="rId10"' in s.fill + s.graphic for _, s in shapes)
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>']
    if uses_pic:
        rels.append(f'<Relationship Id="rId10" Type="{fx.REL}image" Target="media/photo.png"/>')
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + fx.CT)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', document(shapes, jc, bare))
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, theme), encoding='utf-8').read())
        if uses_pic:
            fx.entry(z, 'word/media/photo.png', fx.PICTURES['photo.png']())

# --------------------------------------------------------------------------------------------------------- the sets

SETS = {
    'cr080-phase0': [
        ('fp10-presets-100x100', fam_presets(SQ)),
        ('fp11-presets-144x72', fam_presets(WIDE)),
        ('fp12-presets-adj-half', fam_presets_adj(0.5)),
        ('fp13-presets-adj-double', fam_presets_adj(2)),
        ('fp20-custgeom', fam_custgeom),
        ('fp30-lines', fam_lines),
        ('fp31-arrows', fam_arrows),
        ('fp40-fills', fam_fills),
        ('fp50-styles-2023', fam_styles(fx.THEME_2023)),
        ('fp51-styles-2013', fam_styles(fx.THEME_2013)),
        ('fp60-text-top-left', fam_text('t', 'left')),
        ('fp61-text-bottom-right', fam_text('b', 'right')),
        ('fp21-custgeom-edge', fam_custgeom_edge),
    ],
    # Round 1b (2026-11-10): fp60 opened silently, but Word's save failed ("A file error has occurred"), so fp61 and fp21
    # didn't run. The text probes again in quarters, then fp21, then the no-xfrm probes last, without and with text.
    'cr080-phase0-b': [
        ('fp70-text-tl-1', fam_text_part('t', 'left', 0)),
        ('fp71-text-tl-2', fam_text_part('t', 'left', 1)),
        ('fp72-text-tl-3', fam_text_part('t', 'left', 2)),
        ('fp73-text-tl-4', fam_text_part('t', 'left', 3)),
        ('fp74-text-br-1', fam_text_part('b', 'right', 0)),
        ('fp75-text-br-2', fam_text_part('b', 'right', 1)),
        ('fp76-text-br-3', fam_text_part('b', 'right', 2)),
        ('fp77-text-br-4', fam_text_part('b', 'right', 3)),
        ('fp21-custgeom-edge', fam_custgeom_edge),
        ('fp78-no-xfrm', fam_no_xfrm(False)),
        ('fp79-no-xfrm-text', fam_no_xfrm(True)),
    ],
    # The strict pair (2026-11-10): without the text box first (expected to save), with it last (expected to fail).
    'cr080-phase0-c': [
        ('fp80-pair-no-text', fam_pair(False)),
        ('fp81-pair-text', fam_pair(True)),
    ],
    # Round 2 (2026-11-10): single shapes turned and flipped; groups (child space, nesting, turns, flips, group fill,
    # a picture, text, a connector between children); canvases (background, outline, connectors, a group, text).
    'cr080-phase0-r2': [
        ('fp90-transforms', fam_transforms),
        ('fp91-groups', fam_groups),
        ('fp92-canvas', fam_canvas),
    ],
}
TIMEOUT = {'cr080-phase0': 300, 'cr080-phase0-b': 300, 'cr080-phase0-c': 300, 'cr080-phase0-r2': 300}

def main(staging, only=()):
    manifest = ['set\tfile\tpage\tprobe\tnote\tsize\txml\n']
    for setname, files in SETS.items():
        for name, fam in files:
            prefix = name.split('-')[0]
            for page, s in enumerate(fam(), 1):
                manifest.append(f'{setname}\t{name}\t{page}\t{prefix}-{page:03d}\t{s.note}\t{s.size[0] / EMU:g}x{s.size[1] / EMU:g}'
                                f'\t{s.xml()}{"" if s.xfrm else " [no xfrm]"}{" [cxn]" if s.cxn else ""}'
                                f'{f" [rot {s.rot} {s.flip}]" if s.rot or s.flip else ""}\n')
    open(os.path.join(HERE, 'probes.tsv'), 'w', newline='').writelines(manifest)
    for setname, files in SETS.items():
        if only and setname not in only:
            continue
        sums, order, count = [], [], 0
        for name, fam in files:
            prefix = name.split('-')[0]
            shapes = [(f'{prefix}-{i + 1:03d}', s) for i, s in enumerate(fam())]
            count += len(shapes)
            d = os.path.join(staging, setname, name)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            package(f, shapes, shapes[0][1].theme, getattr(fam, 'jc', 'left'), getattr(fam, 'bare', False))
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as out:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT[setname],
                           'formats': ['docx', 'pdf']}, out)
            for x in (j, f):
                sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {setname}/{name}/{os.path.basename(x)}\n')
            order.append(name + '\n')
        open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
        open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
        print(setname, len(files), 'files,', len(order), 'jobs,', count, 'probes')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
