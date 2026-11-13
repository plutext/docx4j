# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 4's probe round, cr083-phase4: how Word draws VML WordArt, for the rules findings sections 5 and 6 left
provisional (phase4-findings.md section 2, as the review revised it). Jason, 2026-11-13: "build the cr083-phase4 round
with build_vml.py".

The probes are build_wordart.py's form (one to a page, the page's first paragraph naming it), each a VML WordArt as
Word's own insert writes it (cr083-phase4a's wv40-shapetypes.docx, read by harvest.py): Word's shapetype for its o:spt
(shapetypes-word.xml), a v:shape of only its type and its style, 288 by 144 pt at (144, 252) on the page, with
<v:shadow color="#868686"/> (no on: no shadow, wv40), the text path as Word writes it for that type (the curves, 144 to
147, fitshape and trim; the xscale types xscale="f" with trim and fitpath; the rest trim and fitpath), "WordArt" in
Aptos, and w10:wrap; VML's own fill and stroke (white, and black 0.75 pt) unless the probe gives them. Mode 12, as
Word writes legacy WordArt, but the watermarks (wv47) and wv48, mode 15. A shapetype is written once a file, before
its first use, as Word does; a probe that needs its own definition gives it an id of its own.

Files (the probe counts in probes-vml.tsv):
  wv41  the 40 types on two lines ("WordArt" over "Two lines")
  wv42  each type with its adjust values moved (each value with a handle range toward its far end; an angle by 45
        degrees), the second value of the two-value types, and three out of their handle's range
  wv43  geometry and inheritance: a shape's own path over its shapetype's; adj with a value left out; coordsize and
        coordorigin; a shape with no type and its own path; a type that doesn't resolve; two shapetypes of one id; a
        shapetype after its shape; one only in a header; one by o:spt only before a whole one; on="f"; textpathok="f";
        conflicting text path attributes; coordsize 0 and absent; custom arcs swept each way (al, ae, ar, wr); formula
        edge cases each moving a line (a forward @n, an #n past the adj list, a division by zero, if at 0, an overflow);
        qx/qy and qb paths; nf/ns sets; three text paths with three lines
  wv44  the text path: fitshape, fitpath, trim, xscale on and off; font-size; bold and italic; v-text-align's values on
        two lines; v-same-letter-heights; v-rotate-letters; v-text-kern; v-text-spacing in both modes; v-text-reverse;
        an underline; three lines; an empty string; a long one; a variable face's overlapping contours (the fill rule);
        "gyp" and "WAVE" with trim on and off in Aptos, Calibri and Times New Roman (the no-trim box); Follow Path in
        Calibri and Times New Roman and with "gyp" (its rise)
  wv45  paint: fill colours in VML's forms, no fill, opacity, gradients (angle, focus, a colours list, radial), a tile
        picture; the stroke (none, weights, colour, dash, join, cap, a compound line, opacity); v:shadow (on, offsets,
        a percentage, colour and opacity, double, perspective, emboss, obscured, one shadow on two box sizes); 3-D
  wv46  placement: in a line; anchored to the margin, column and paragraph; centred; turned 30, 90 and 315 degrees;
        flipped; turned and flipped; wrapped square and top and bottom; behind and in front of body text; in a table
        cell; groups (WordArt alone, with a picture, nested, turned) and a w:object holding one
  wv47  watermarks in Word's form, each in its own section's header over body text: diagonal and horizontal;
        semitransparent or not; faces; fixed sizes; colours; page sizes and margins; the COM form wv40 wrote; one placed
        off centre; one turned 30
  wv48  five of the above in a mode-15 document (legacy WordArt kept in a current document)

usage: build_vml.py <staging dir>"""
import hashlib, json, os, re, struct, sys, zipfile, zlib
from xml.sax.saxutils import escape

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import build_wordart as bw

SET = 'cr083-phase4'
TIMEOUT = 600
EMU = bw.EMU
X0, Y0 = 144, 252
W, H = 288, 144
FONT = 'Aptos'

# ------------------------------------------------------------------------------------------------------ shapetypes

_ST = open(os.path.join(HERE, 'shapetypes-word.xml'), encoding='utf-8').read()
TYPES = {int(m.group(2)): m.group(0) for m in
         re.finditer(r'<v:shapetype id="_x0000_t(\d+)"[^>]*?o:spt="(\d+)".*?</v:shapetype>', _ST, re.S)}
assert sorted(TYPES) == list(range(136, 176)), sorted(TYPES)

def attr(xml, name):
    m = re.search(r'\s' + re.escape(name) + r'="([^"]*)"', xml)
    return m.group(1) if m else None

def handles(spt):
    """{adj index: (lo, hi)} from the shapetype's handles' ranges"""
    out = {}
    for h in re.findall(r'<v:h\b[^>]*>', TYPES[spt]):
        pos = attr(h, 'position') or ''
        for i, rng in ((0, attr(h, 'xrange')), (1, attr(h, 'yrange'))):
            refs = re.findall(r'#(\d)', pos.split(',')[i] if ',' in pos else pos)
            if rng and refs:
                lo, hi = (int(float(v)) for v in rng.split(','))
                out[int(refs[0])] = (lo, hi)
        if attr(h, 'radiusrange'):
            refs = re.findall(r'#(\d)', attr(h, 'polar') or '') + re.findall(r'#(\d)', pos)
            if refs:
                lo, hi = (int(float(v)) for v in attr(h, 'radiusrange').split(','))
                out.setdefault(int(refs[-1]), (lo, hi))
    return out

def adj_values(spt):
    a = attr(TYPES[spt], 'adj')
    return [] if a is None else a.split(',')

def renamed(spt, tid):
    """Word's shapetype for spt, under another id"""
    return TYPES[spt].replace(f'id="_x0000_t{spt}"', f'id="_x0000_t{tid}"', 1)

def custom(tid, path, formulas=(), adj=None, coordsize='21600,21600', coordorigin=None, textpath='on="t" fitshape="t"',
           textpathok='t', extra=''):
    """A shapetype of a probe's own"""
    f = ''.join(f'<v:f eqn="{e}"/>' for e in formulas)
    a = f' adj="{adj}"' if adj is not None else ''
    cs = f' coordsize="{coordsize}"' if coordsize is not None else ''
    co = f' coordorigin="{coordorigin}"' if coordorigin else ''
    p = f' path="{path}"' if path is not None else ''
    ok = f' textpathok="{textpathok}"' if textpathok else ''
    return (f'<v:shapetype id="_x0000_t{tid}"{cs}{co} o:spt="100"{a}{p}>' + (f'<v:formulas>{f}</v:formulas>' if f else '')
            + f'<v:path{ok}/><v:textpath {textpath}/>{extra}<o:lock v:ext="edit" text="t" shapetype="t"/></v:shapetype>')

def word_textpath(spt):
    """The shape's text path as Word's insert writes it for that type (wv40), without the string"""
    st = f'font-family:&quot;{FONT}&quot;'
    if 144 <= spt <= 147:
        return dict(style=st, fitshape='t', trim='t')
    if attr(re.search(r'<v:textpath[^>]*>', TYPES[spt]).group(0), 'xscale') == 't':
        return dict(style=st + ';v-text-kern:t', trim='t', fitpath='t', xscale='f')
    return dict(style=st + ';v-text-kern:t', trim='t', fitpath='t')

# ---------------------------------------------------------------------------------------------------------- probes

def pos_style(x=X0, y=Y0, w=W, h=H, z=None, extra='', rel='page'):
    return (f'position:absolute;margin-left:{x:g}pt;margin-top:{y:g}pt;width:{w:g}pt;height:{h:g}pt;z-index:{z or 1};'
            f'{extra}mso-position-horizontal:absolute;mso-position-horizontal-relative:{rel};'
            f'mso-position-vertical:absolute;mso-position-vertical-relative:{rel}')

class V:
    """One page: one VML WordArt (or what the probe draws instead)"""
    kind = 'vml'

    def __init__(self, note, spt=136, text='WordArt', tid=None, shapetype=None, style=None, attrs='', textpath=None,
                 tp_extra=None, children=None, after='', wrap='<w10:wrap anchorx="page" anchory="page"/>', raw=None,
                 font_style=None, own=''):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def tid_(self):
        return self.tid or str(self.spt)

    def summary(self):
        return f'type #_x0000_t{self.tid_()} (o:spt {self.spt}); "{self.text}"'

    def shape_xml(self, n, style=None):
        tp = dict(word_textpath(self.spt)) if self.textpath is None else dict(self.textpath)
        if self.font_style is not None:
            tp['style'] = self.font_style
        if self.tp_extra:
            tp.update(self.tp_extra)
        tp_xml = ' '.join(f'{k}="{v}"' for k, v in tp.items() if v is not None)
        string = escape(self.text).replace('\n', '&#xA;')
        kids = '<v:shadow color="#868686"/>' if self.children is None else self.children
        typ = f' type="#_x0000_t{self.tid_()}"' if self.tid_() != 'none' else ''
        return (f'<v:shape id="WordArt{n}" o:spid="_x0000_s{1024 + n}"{typ} style="{style or self.style or pos_style(z=n)}"'
                f'{(" " + self.attrs) if self.attrs else ""}{self.own}>{kids}<v:textpath {tp_xml} string="{string}"/>{self.wrap}</v:shape>')

    def type_xml(self, typed):
        key = self.tid_()
        if key == 'none' or key in typed:
            return ''
        typed.add(key)
        if self.shapetype is not None:
            return self.shapetype
        return TYPES[self.spt] if self.tid is None else renamed(self.spt, self.tid)

    def page(self, n, typed):
        if self.raw is not None:
            inline, after = self.raw(self, n, typed)
            return inline, after
        return f'<w:r><w:pict>{self.type_xml(typed)}{self.shape_xml(n)}</w:pict></w:r>', self.after

BODY = 'Body text the WordArt is set over and beside, as a page of a document has it. ' * 5

def body(text=BODY):
    return bw.para([bw.Run(text, size=11)], jc='left', before=120)

# ----------------------------------------------------------------------------------------------------------- wv41

def fam_two_lines():
    return [V(f'o:spt {s} at its defaults, two lines', s, text='WordArt\nTwo lines') for s in range(136, 176)]

# ----------------------------------------------------------------------------------------------------------- wv42

def moved(value, rng):
    v = int(value)
    if rng:
        lo, hi = rng
        far = hi if abs(hi - v) >= abs(v - lo) else lo
        return int(v + (far - v) * 0.6)
    if abs(v) >= 65536 * 5:                       # an angle in fd
        return v + 45 * 65536 * (-1 if v > 0 else 1)
    return int(v * 0.6) if v else 5400

def fam_adj():
    out = []
    for s in range(136, 176):
        vals, hs = adj_values(s), handles(s)
        if not vals:
            out.append(V(f'o:spt {s} with an adj of 5400 (it has none)', s, attrs='adj="5400"'))
            continue
        first = vals[0] if vals[0] else '0'
        out.append(V(f'o:spt {s} with its first adj moved from {first}', s, attrs=f'adj="{moved(first, hs.get(0))}"'))
        if len(vals) > 1 and vals[1]:
            out.append(V(f'o:spt {s} with its second adj moved from {vals[1]}', s,
                         attrs=f'adj=",{moved(vals[1], hs.get(1))}"'))
    for s in (136, 137, 160):
        lo, hi = handles(s).get(0, (0, 21600))
        out.append(V(f'o:spt {s} with an adj past its handle\'s range ({lo} to {hi})', s, attrs=f'adj="{hi + (hi - lo)}"'))
    return out

# ----------------------------------------------------------------------------------------------------------- wv43

TWO = 'm0,0l21600,0m0,21600l21600,21600e'          # a plain two-path text path

def fam_geometry():
    out = []
    t = 0
    def tid():
        nonlocal t
        t += 1
        return f'90{t:02d}'
    a = tid()
    out.append(V("the shape's own path over its shapetype's (136's, slanted on the shape)", 136, tid=a,
                 shapetype=renamed(136, a), own=' path="m0,0l21600,10800m0,21600l21600,21600e"'))
    a = tid()
    out.append(V('a two-value type (148) with its first adj left out (",3000")', 148, tid=a, shapetype=renamed(148, a),
                 attrs='adj=",3000"'))
    a = tid()
    out.append(V('a coordsize of 1000 (the path in its units)', tid=a,
                 shapetype=custom(a, 'm0,0l1000,250m0,1000l1000,1000e', coordsize='1000,1000')))
    a = tid()
    out.append(V('a coordorigin of 500,500 (the path in its space)', tid=a,
                 shapetype=custom(a, 'm500,500l1500,750m500,1500l1500,1500e', coordsize='1000,1000', coordorigin='500,500')))
    out.append(V('no type, the path on the shape (Word\'s save of phase 0\'s wv20-42)', tid='none', shapetype='',
                 own=' coordsize="21600,21600" o:spt="100" path="m,l21600,10800m,21600r21600,e"',
                 textpath=dict(on='t', fitshape='t', style=f'font-family:&quot;{FONT}&quot;', trim='t')))
    out.append(V('a type that doesn\'t resolve', tid='9099', shapetype=''))
    a = tid()
    dup = custom(a, 'm0,0l21600,10800m0,21600l21600,21600e') + custom(a, 'm0,10800l21600,0m0,21600l21600,21600e')
    out.append(V('two shapetypes of one id, the first slanted down to the right, the second up', tid=a, shapetype=dup))
    a = tid()
    out.append(V('the shapetype after its shape', tid=a, shapetype='',
                 raw=lambda p, n, typed: (f'<w:r><w:pict>{p.shape_xml(n)}{custom(a, "m0,0l21600,10800m0,21600l21600,21600e")}'
                                          '</w:pict></w:r>', '')))
    a = tid()
    out.append(V('the shapetype only in a header (this page\'s section\'s header holds it, unused there)', tid=a,
                 shapetype='', raw=None))
    out[-1].header_type = custom(a, 'm0,0l21600,10800m0,21600l21600,21600e')
    a = tid()
    out.append(V('a shapetype with only its o:spt 136, then a whole one (another id) used', tid=a,
                 shapetype=bw.spt_only(136, '136s') + renamed(136, a)))
    out.append(V('the shape\'s text path on="f" (its shapetype\'s on)', tid=None, tp_extra=dict(on='f')))
    a = tid()
    out.append(V('the shapetype\'s textpathok="f"', tid=a, shapetype=custom(a, TWO, textpathok='f')))
    a = tid()
    out.append(V('the shape\'s fitshape="f" against its shapetype\'s "t"', tid=a, shapetype=custom(a, TWO),
                 tp_extra=dict(fitshape='f')))
    a = tid()
    out.append(V('coordsize="0,0"', tid=a, shapetype=custom(a, 'm0,0l1000,250m0,1000l1000,1000e', coordsize='0,0')))
    a = tid()
    out.append(V('no coordsize (the path in 1000s)', tid=a, shapetype=custom(a, 'm0,0l1000,250m0,1000l1000,1000e', coordsize=None)))
    for path, note in (('al10800,10800,10800,10800,11796480,-11796480e', 'al from 180 degrees, sweep -180'),
                       ('al10800,10800,10800,10800,11796480,11796480e', 'al from 180 degrees, sweep +180'),
                       ('m0,10800ae10800,10800,10800,10800,11796480,-11796480e', 'ae from 180 degrees, sweep -180, after a moveto'),
                       ('ar0,0,21600,21600,0,10800,21600,10800e', 'ar from the left to the right point'),
                       ('wr0,0,21600,21600,0,10800,21600,10800e', 'wr from the left to the right point')):
        a = tid()
        out.append(V(f'one text path: {note}', tid=a, shapetype=custom(a, path, textpath='on="t" fitpath="t"')))
    for formulas, ref, note in ((['sum @1 0 0', 'val 5400'], '@0', 'a forward @n (sum @1 0 0)'),
                                (['val #3'], '@0', 'an #n past the adj list (val #3, adj one value)'),
                                (['prod 5400 2 0'], '@0', 'a division by zero (prod 5400 2 0)'),
                                (['if 0 0 5400'], '@0', 'if at 0 (if 0 0 5400)'),
                                (['sum 2147483647 2147483647 0', 'min @0 5400'], '@1', 'an overflow, held (sum MAX MAX)')):
        a = tid()
        out.append(V(f'the top line at a formula: {note}', tid=a,
                     shapetype=custom(a, f'm0,{ref}l21600,{ref}m0,21600l21600,21600e', formulas, adj='3000')))
    a = tid()
    out.append(V('qx and qy: the top a quarter ellipse each side, the bottom straight', tid=a,
                 shapetype=custom(a, 'm0,10800qx10800,0qy21600,10800m0,21600l21600,21600e')))
    a = tid()
    out.append(V('qb: the top a quadratic through (10800,-5400)', tid=a,
                 shapetype=custom(a, 'm0,5400qb10800,-5400,21600,5400m0,21600l21600,21600e')))
    a = tid()
    out.append(V('two sets: the top nf, the bottom ns', tid=a, shapetype=custom(a, 'm0,0l21600,5400nfem0,21600l21600,21600nse')))
    a = tid()
    out.append(V('three text paths (one-path bands), three lines', tid=a, text='One\nTwo\nThree',
                 shapetype=custom(a, 'm0,3600l21600,3600m0,10800l21600,10800m0,18000l21600,18000e', textpath='on="t" fitpath="t"')))
    out.append(V('o:spt on the shape, no type', tid='none', shapetype='', own=' o:spt="136"'))
    a = tid()
    out.append(V('the text path\'s style on the shapetype only (Times New Roman)', tid=a,
                 shapetype=custom(a, TWO, textpath='on="t" fitshape="t" style="font-family:&quot;Times New Roman&quot;"'),
                 textpath=dict(trim='t', fitpath='t')))
    return out

# ----------------------------------------------------------------------------------------------------------- wv44

def st(font=FONT, extra=''):
    return f'font-family:&quot;{font}&quot;;v-text-kern:t' + (';' + extra if extra else '')

def fam_textpath():
    out = []
    out += [V('136, fitshape="f" on the shape', 136, tp_extra=dict(fitshape='f')),
            V('136, fitpath="f"', 136, tp_extra=dict(fitpath='f')),
            V('144 (Arch Up), fitpath="t" on the shape', 144, tp_extra=dict(fitpath='t')),
            V('144, fitshape="f"', 144, tp_extra=dict(fitshape='f')),
            V('136, trim="f"', 136, tp_extra=dict(trim='f')),
            V('144, trim="f"', 144, tp_extra=dict(trim='f')),
            V('136, xscale="t"', 136, tp_extra=dict(xscale='t')),
            V('152 (Curve Up), xscale="t" (its shapetype\'s)', 152, tp_extra=dict(xscale='t')),
            V('152, xscale="f" (Word\'s insert)', 152),
            V('160 (Inflate), xscale="t"', 160, tp_extra=dict(xscale='t'))]
    for size in (1, 12, 36, 72):
        out.append(V(f'136, font-size {size} pt', 136, font_style=st(extra=f'font-size:{size}pt')))
    out += [V('136, bold', 136, font_style=st(extra='font-weight:bold')),
            V('136, italic', 136, font_style=st(extra='font-style:italic')),
            V('136, bold italic', 136, font_style=st(extra='font-weight:bold;font-style:italic'))]
    for al in ('left', 'right', 'center', 'justify', 'letter-justify', 'stretch-justify'):
        out.append(V(f'136, two lines of unequal length, v-text-align:{al}', 136, text='WordArt\nTwo lines here',
                     font_style=st(extra=f'v-text-align:{al}')))
    for al in ('left', 'right'):
        out.append(V(f'144, v-text-align:{al}', 144, font_style=f'font-family:&quot;{FONT}&quot;;v-text-align:{al}'))
    out += [V('136, v-same-letter-heights:t', 136, text='Wordart', font_style=st(extra='v-same-letter-heights:t')),
            V('136, v-rotate-letters:t', 136, font_style=st(extra='v-rotate-letters:t')),
            V('136, "AVATAR" kerned', 136, text='AVATAR', font_style=st()),
            V('136, "AVATAR" not kerned', 136, text='AVATAR', font_style=f'font-family:&quot;{FONT}&quot;')]
    for mode in ('tightening', 'tracking'):
        for sp in (80, 150):
            out.append(V(f'136, v-text-spacing:{sp} ({mode})', 136,
                         font_style=st(extra=f'v-text-spacing-mode:{mode};v-text-spacing:{sp}')))
    out += [V('136, v-text-reverse:t, two lines', 136, text='WordArt\nTwo lines', font_style=st(extra='v-text-reverse:t')),
            V('136, text-decoration:underline (kept, not drawn, [MS-OI29500] 2.1.1812)', 136,
              font_style=st(extra='text-decoration:underline')),
            V('136, three lines', 136, text='WordArt\nTwo lines\nThree'),
            V('136, an empty string', 136, text=''),
            V('136, a long string (60 characters)', 136, text='The quick brown fox jumps over the lazy dog, then naps again.'),
            V('136, Bahnschrift (a variable face: overlapping contours, the fill rule)', 136, text='WordArt Bahnschrift',
              font_style=st('Bahnschrift'))]
    for font in ('Aptos', 'Calibri', 'Times New Roman'):
        for text in ('gyp', 'WAVE'):
            for trim in ('t', 'f'):
                out.append(V(f'136, "{text}" in {font}, trim="{trim}"', 136, text=text, font_style=st(font),
                             tp_extra=dict(trim=trim)))
    for font in ('Calibri', 'Times New Roman'):
        out.append(V(f'144 (Follow Path) in {font}', 144, font_style=f'font-family:&quot;{font}&quot;'))
    out.append(V('144, "gyp"', 144, text='gyp'))
    return out

# ----------------------------------------------------------------------------------------------------------- wv45

def fam_paint():
    k = '<v:shadow color="#868686"/>'
    out = []
    for c in ('red', '#336699', '#336699 [4]', 'windowText'):
        out.append(V(f'fillcolor="{c}"', 136, attrs=f'fillcolor="{c}"'))
    out.append(V('fillcolor red, strokecolor "fill darken(118)"', 136, attrs='fillcolor="red" strokecolor="fill darken(118)"'))
    out.append(V('filled="f" (the stroke alone)', 136, attrs='filled="f"'))
    for o in ('.5', '.2'):
        out.append(V(f'fill opacity {o}', 136, attrs='fillcolor="#336699"', children=k + f'<v:fill opacity="{o}"/>'))
    for ang in (0, 45, 90):
        out.append(V(f'gradient red to blue, angle {ang}', 136, attrs='fillcolor="red"',
                     children=k + f'<v:fill color2="blue" angle="{ang}" type="gradient"/>'))
    for focus in ('50%', '100%', '-50%'):
        out.append(V(f'gradient red to blue, angle 0, focus {focus}', 136, attrs='fillcolor="red"',
                     children=k + f'<v:fill color2="blue" focus="{focus}" type="gradient"/>'))
    out.append(V('gradient with a colours list (red, yellow, blue)', 136, attrs='fillcolor="red"',
                 children=k + '<v:fill color2="blue" colors="0 red;32768f yellow;1 blue" method="none" type="gradient"/>'))
    out.append(V('gradientRadial red to blue, focus at the middle', 136, attrs='fillcolor="red"',
                 children=k + '<v:fill color2="blue" focusposition=".5,.5" focussize="" type="gradientRadial"/>'))
    out.append(V('a tile picture fill', 136, attrs='fillcolor="red"',
                 children=k + '<v:fill r:id="rIdImg1" o:title="" type="tile"/>'))
    out += [V('stroked="f"', 136, attrs='stroked="f"'),
            V('strokeweight 0.25 pt', 136, attrs='strokeweight=".25pt"'),
            V('strokeweight 2 pt', 136, attrs='strokeweight="2pt"'),
            V('strokeweight 6 pt, fill yellow', 136, attrs='fillcolor="yellow" strokeweight="6pt"'),
            V('strokecolor red, 2 pt', 136, attrs='strokecolor="red" strokeweight="2pt"'),
            V('dashstyle dash, 2 pt', 136, attrs='strokeweight="2pt"', children=k + '<v:stroke dashstyle="dash"/>'),
            V('joinstyle bevel, 4 pt', 136, attrs='strokeweight="4pt"', children=k + '<v:stroke joinstyle="bevel"/>'),
            V('endcap round, 4 pt', 136, attrs='strokeweight="4pt"', children=k + '<v:stroke endcap="round"/>'),
            V('linestyle thickThin, 6 pt', 136, attrs='strokeweight="6pt"', children=k + '<v:stroke linestyle="thickThin"/>'),
            V('stroke opacity .5, 4 pt', 136, attrs='strokeweight="4pt"', children=k + '<v:stroke opacity=".5"/>')]
    sh = [('on="t"', 'v:shadow on, its defaults'),
          ('on="t" offset="4pt,4pt"', 'offset 4pt,4pt'),
          ('on="t" offset="-3pt,2pt"', 'offset -3pt,2pt'),
          ('on="t" offset="10%,10%"', 'offset as a percentage, 10%,10%'),
          ('on="t" color="red" opacity=".5" offset="4pt,4pt"', 'red at .5'),
          ('on="t" type="double" color="#868686" color2="red" offset="3pt,3pt" offset2="6pt,6pt"', 'double'),
          ('on="t" type="perspective" color="#868686" opacity=".5" offset="0,0" origin="0,.5" matrix=",,,.5,,-4768371582e-16"', 'perspective'),
          ('on="t" type="emboss" color="#868686"', 'emboss'),
          ('on="t" obscured="t" offset="4pt,4pt"', 'obscured')]
    for a, note in sh:
        out.append(V(f'v:shadow: {note}', 136, attrs='fillcolor="#336699"', children=f'<v:shadow {a}/>'))
    out.append(V('v:shadow offset 4pt,4pt on a box of 144 by 72 pt', 136, attrs='fillcolor="#336699"',
                 children='<v:shadow on="t" offset="4pt,4pt"/>', style=pos_style(w=144, h=72)))
    out.append(V('o:extrusion on (3-D)', 136, attrs='fillcolor="#336699"',
                 children=k + '<o:extrusion v:ext="view" on="t"/>'))
    return out

# ----------------------------------------------------------------------------------------------------------- wv46

def group_page(note, members, gstyle, coordsize='2000,1000', coordorigin='0,0', in_object=False, picture=False):
    """A v:group of WordArt (and a picture), in a w:pict or a w:object"""
    def raw(p, n, typed):
        kids = []
        for i, (spt, left, top, w, h, text) in enumerate(members):
            q = V('', spt, text=text)
            t = q.type_xml(typed)
            kids.append(t + q.shape_xml(3000 + n * 10 + i, style=f'position:absolute;left:{left};top:{top};width:{w};height:{h}')
                        .replace('<w10:wrap anchorx="page" anchory="page"/>', ''))
        if picture:
            t = '' if '75' in typed else ('<v:shapetype id="_x0000_t75" coordsize="21600,21600" o:spt="75" o:preferrelative="t" '
                                          'path="m@4@5l@4@11@9@11@9@5xe" filled="f" stroked="f"><v:stroke joinstyle="miter"/>'
                                          '<v:path o:extrusionok="f" gradientshapeok="t" o:connecttype="rect"/>'
                                          '<o:lock v:ext="edit" aspectratio="t"/></v:shapetype>')
            typed.add('75')
            kids.append(t + f'<v:shape id="Pic{n}" type="#_x0000_t75" style="position:absolute;left:0;top:0;width:600;height:1000">'
                        '<v:imagedata r:id="rIdImg1" o:title=""/></v:shape>')
        g = (f'<v:group id="Group{n}" style="{gstyle}" coordorigin="{coordorigin}" coordsize="{coordsize}">'
             + ''.join(kids) + '</v:group>')
        if in_object:
            return f'<w:r><w:object w:dxaOrig="4000" w:dyaOrig="2000">{g}</w:object></w:r>', ''
        return f'<w:r><w:pict>{g}</w:pict></w:r>', ''
    return V(note, raw=raw)

def fam_placement():
    out = [V('in a line (no position): text before it and after it', 136, style=f'width:{W}pt;height:{H}pt',
             wrap='', after=''),
           V('anchored to the margin at 36, 36', 136, style=pos_style(x=36, y=36, rel='margin').replace(
               'mso-position-horizontal-relative:margin;', 'mso-position-horizontal-relative:margin;')),
           V('anchored to the column and the paragraph (text) at 36, 20', 136,
             style=pos_style(x=36, y=20).replace('-relative:page', '-relative:text')),
           V('centred across the margin, at 252 down the page', 136,
             style=pos_style().replace('mso-position-horizontal:absolute;mso-position-horizontal-relative:page',
                                       'mso-position-horizontal:center;mso-position-horizontal-relative:margin')),
           V('centred down the page, at 144 across', 136,
             style=pos_style().replace('mso-position-vertical:absolute', 'mso-position-vertical:center'))]
    for rot in (30, 90, 315):
        out.append(V(f'turned {rot} degrees', 136, style=pos_style(extra=f'rotation:{rot};')))
    for flip in ('x', 'y', 'x y'):
        out.append(V(f'flip:{flip}', 136, style=pos_style(extra=f'flip:{flip};')))
    out.append(V('turned 30 and flip:x', 136, style=pos_style(extra='rotation:30;flip:x;')))
    out.append(V('144 (Follow Path) turned 30', 144, style=pos_style(extra='rotation:30;')))
    for wrap in ('square', 'topAndBottom'):
        out.append(V(f'wrapped {wrap}, body text round it', 136, style=pos_style(y=150, rel='page'),
                     wrap=f'<w10:wrap type="{wrap}" anchorx="page" anchory="page"/>', after=body()))
    out.append(V('behind the body text (z-index negative)', 136, attrs='fillcolor="silver" stroked="f"',
                 style=pos_style(y=150, z=-251658240), after=body()))
    out.append(V('in front of the body text', 136, attrs='fillcolor="silver" stroked="f"', style=pos_style(y=150), after=body()))
    def cell(p, n, typed):
        pict = f'<w:r><w:pict>{p.type_xml(typed)}{p.shape_xml(n, style=pos_style(x=10, y=10, w=144, h=72).replace("-relative:page", "-relative:text"))}</w:pict></w:r>'
        tbl = ('<w:tbl><w:tblPr><w:tblW w:w="5000" w:type="dxa"/><w:tblBorders><w:top w:val="single" w:sz="4"/>'
               '<w:left w:val="single" w:sz="4"/><w:bottom w:val="single" w:sz="4"/><w:right w:val="single" w:sz="4"/>'
               '</w:tblBorders><w:tblLayout w:type="fixed"/></w:tblPr><w:tblGrid><w:gridCol w:w="5000"/></w:tblGrid>'
               '<w:tr><w:trPr><w:trHeight w:val="2400"/></w:trPr><w:tc><w:tcPr><w:tcW w:w="5000" w:type="dxa"/></w:tcPr>'
               f'<w:p>{pict}<w:r><w:t>In a cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>')
        return '', tbl + '<w:p/>'
    out.append(V('in a table cell, 10 and 10 from the cell (o:allowincell default)', 136, raw=cell))
    gs = pos_style(w=200, h=100).replace('z-index:1;', 'z-index:1;')
    out.append(group_page('a group: one WordArt at (200, 100) of 2000 by 1000 units, 1000 by 500',
                          [(136, 200, 100, 1000, 500, 'Grouped')], gs))
    out.append(group_page('a group: a picture (left) and a WordArt (right)', [(136, 800, 200, 1100, 600, 'Beside')], gs,
                          picture=True))
    out.append(group_page('a group, coordorigin 1000,1000: a WordArt at (1200, 1100)', [(136, 1200, 1100, 1000, 500, 'Origin')],
                          gs, coordorigin='1000,1000'))
    out.append(group_page('a group turned 30 degrees', [(136, 200, 100, 1600, 800, 'Turned')],
                          pos_style(w=200, h=100, extra='rotation:30;')))
    out.append(group_page('a w:object holding a group (real3\'s form, printed)', [(136, 200, 100, 1000, 500, 'Object')], gs,
                          in_object=True))
    def nested(p, n, typed):
        q = V('', 136, text='Nested')
        t = q.type_xml(typed)
        inner = (f'<v:group id="Inner{n}" style="position:absolute;left:1000;top:0;width:1000;height:1000" '
                 'coordorigin="0,0" coordsize="100,100">'
                 + t + q.shape_xml(3000 + n * 10, style='position:absolute;left:10;top:20;width:80;height:40')
                 .replace('<w10:wrap anchorx="page" anchory="page"/>', '') + '</v:group>')
        g = f'<v:group id="Group{n}" style="{gs}" coordorigin="0,0" coordsize="2000,1000">{inner}</v:group>'
        return f'<w:r><w:pict>{g}</w:pict></w:r>', ''
    out.append(V('a group in a group: the inner (1000, 0) 1000 by 1000 of 100s, the WordArt (10, 20) 80 by 40', raw=nested))
    return out

# ----------------------------------------------------------------------------------------------------------- wv47

class Mark(bw.Mark):
    """build_wordart's watermark with Word's own shapetype and a page of its own size and margins"""

    def __init__(self, note, rotation=315, string='WATERMARK', fill='fillcolor="silver" stroked="f"', opacity='.5',
                 font='Aptos', width=468, height=117, size='1pt', pg=None, pos=None, extra_tp='', kids=''):
        super().__init__(note, rotation, string, fill, opacity, font, width, height)
        self.size, self.pg, self.pos, self.extra_tp, self.kids = size, pg, pos, extra_tp, kids

    def header(self, n):
        xml = super().header(n)
        xml = xml.replace(bw.SPT136, TYPES[136])
        xml = xml.replace('font-size:1pt"', f'font-size:{self.size}"' + (' ' + self.extra_tp if self.extra_tp else ''))
        if self.pos:
            xml = re.sub(r'style="position:absolute;[^"]*"', f'style="{self.pos}"', xml, count=1)
        if self.kids:
            head, sep, tail = xml.rpartition('<v:textpath ')       # the shape's, after its shapetype's
            xml = head + self.kids + sep + tail
        return xml

def fam_marks():
    letter = '<w:pgSz w:w="12240" w:h="15840"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>'
    land = '<w:pgSz w:w="16838" w:h="11906" w:orient="landscape"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>'
    uneq = '<w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="720" w:right="2880" w:bottom="2160" w:left="720" w:header="708" w:footer="708" w:gutter="0"/>'
    out = [Mark('diagonal, silver at 50%'),
           Mark('horizontal, silver at 50%', rotation=0),
           Mark('diagonal, silver, not semitransparent', opacity=''),
           Mark('diagonal, Calibri', font='Calibri'),
           Mark('diagonal, Times New Roman', font='Times New Roman'),
           Mark('diagonal, 36 pt (a box Word\'s dialog might give: 230 by 46)', size='36pt', width=230, height=46),
           Mark('diagonal, 72 pt (460 by 92)', size='72pt', width=460, height=92),
           Mark('horizontal, 144 pt (460 by 184)', rotation=0, size='144pt', width=460, height=184),
           Mark('diagonal, red, opaque', fill='fillcolor="red" stroked="f"', opacity=''),
           Mark('diagonal, blue at 50%', fill='fillcolor="#4472C4" stroked="f"'),
           Mark('diagonal, a two-word text', string='DO NOT COPY'),
           Mark('diagonal, a Letter page', pg=letter),
           Mark('diagonal, A4 landscape', pg=land),
           Mark('diagonal, unequal margins (left 36, right 144, top 36, bottom 108)', pg=uneq),
           Mark('diagonal, the form wv40\'s COM call wrote (trim, fitpath, 473.3 by 129.1 pt)', width=473.3, height=129.1,
                extra_tp='trim="t" fitpath="t"', kids='<v:shadow color="#868686"/>'),
           Mark('diagonal, placed off centre (margin-left 50, margin-top 100, relative to the margin)',
                pos='position:absolute;margin-left:50pt;margin-top:100pt;width:468pt;height:117pt;rotation:315;'
                    'z-index:-251657216;mso-position-horizontal-relative:margin;mso-position-vertical-relative:margin'),
           Mark('turned 30 degrees', rotation=30),
           Mark('turned 90 degrees', rotation=90),
           Mark('diagonal, a one-letter text', string='X'),
           Mark('horizontal, a 60-character text', rotation=0, string='CONFIDENTIAL DRAFT FOR REVIEW ONLY NOT FOR DISTRIBUTION 2026')]
    return out

def fam_mode15():
    return [V('136 at its defaults, in a mode-15 document', 136),
            V('144 (Follow Path), in a mode-15 document', 144),
            V('160 (Inflate), in a mode-15 document', 160),
            V('136 turned 315, in a mode-15 document', 136, style=pos_style(extra='rotation:315;')),
            group_page('a group with a WordArt, in a mode-15 document', [(136, 200, 100, 1000, 500, 'Grouped')],
                       pos_style(w=200, h=100))]

# ------------------------------------------------------------------------------------------------------- packages

def png():
    """A 24 by 24 picture: four colour quarters"""
    rows = b''
    for y in range(24):
        row = b'\x00'
        for x in range(24):
            row += bytes((220, 60, 60) if (x < 12) == (y < 12) else (60, 90, 200))
        rows += row
    def chunk(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    return (b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 24, 24, 8, 2, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(rows)) + chunk(b'IEND', b''))

def document(probes, mode):
    """build_wordart's document, with a header part for a probe whose shapetype lives only there, and pages sized"""
    doc, hdr = bw.document(probes, mode)
    # a page's own size and margins (wv47): the section's sectPr after its watermark
    for n, (pid, p) in enumerate(probes, 1):
        if getattr(p, 'pg', None):
            rid = f'rIdH{n}'
            doc = re.sub(r'(<w:sectPr><w:headerReference w:type="default" r:id="' + rid + r'"/>)<w:pgSz[^>]*/><w:pgMar[^>]*/>',
                         lambda m: m.group(1) + p.pg, doc, count=1)
    extra = [p.header_type for _, p in probes if getattr(p, 'header_type', None)]
    if extra:
        xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in bw.NS.items())
        n = 900
        hdr.append((n, 'rIdH900', f'{bw.fx.DECL}<w:hdr {xmlns}><w:p><w:r><w:pict>{"".join(extra)}</w:pict></w:r></w:p></w:hdr>'))
        doc = doc.replace('<w:sectPr>', '<w:sectPr><w:headerReference w:type="default" r:id="rIdH900"/>', 1) \
            if '<w:headerReference' not in doc else doc
    return doc, hdr

def package(path, probes, mode):
    doc, hdr = document(probes, mode)
    image = 'rIdImg1' in doc
    rels = [f'<Relationship Id="rId1" Type="{bw.fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{bw.fx.REL}theme" Target="theme/theme1.xml"/>']
    rels += [f'<Relationship Id="{rid}" Type="{bw.fx.REL}header" Target="header{n}.xml"/>' for n, rid, _ in hdr]
    if image:
        rels.append(f'<Relationship Id="rIdImg1" Type="{bw.fx.REL}image" Target="media/image1.png"/>')
    ct = bw.fx.CT.replace('</Types>', ''.join(f'<Override PartName="/word/header{n}.xml" ContentType="{bw.CT_HDR}"/>'
                                              for n, _, _ in hdr) + '</Types>')
    if image and 'Extension="png"' not in ct:
        ct = ct.replace('<Default ', '<Default Extension="png" ContentType="image/png"/><Default ', 1)
    settings = bw.fx.SETTINGS.replace('w:val="15"', f'w:val="{mode}"')
    with zipfile.ZipFile(path, 'w') as z:
        bw.fx.entry(z, '[Content_Types].xml', bw.fx.DECL + ct)
        bw.fx.entry(z, '_rels/.rels', bw.fx.DECL + bw.fx.RELS)
        bw.fx.entry(z, 'word/document.xml', doc)
        bw.fx.entry(z, 'word/_rels/document.xml.rels', bw.fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                    'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        bw.fx.entry(z, 'word/settings.xml', settings)
        bw.fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(bw.fx.THEMES, bw.fx.THEME_2023), encoding='utf-8').read())
        for n, _, xml in hdr:
            bw.fx.entry(z, f'word/header{n}.xml', xml)
        if image:
            z.writestr('word/media/image1.png', png())

FILES = [('wv41-two-lines', fam_two_lines, 12),
         ('wv42-adj', fam_adj, 12),
         ('wv43-geometry', fam_geometry, 12),
         ('wv44-textpath', fam_textpath, 12),
         ('wv45-paint', fam_paint, 12),
         ('wv46-placement', fam_placement, 12),
         ('wv47-watermarks', fam_marks, 15),
         ('wv48-mode15', fam_mode15, 15)]

def main(staging):
    manifest = ['set\tfile\tpage\tprobe\tkind\tmode\tnote\tsummary\n']
    sums, order, count = [], [], 0
    for name, fam, mode in FILES:
        prefix = name.split('-')[0]
        probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(fam())]
        for i, (pid, p) in enumerate(probes, 1):
            summary = p.summary().replace('\n', ' / ')
            manifest.append(f'{SET}\t{name}\t{i}\t{pid}\t{p.kind}\t{mode}\t{p.note}\t{summary}\n')
        count += len(probes)
        d = os.path.join(staging, SET, name)
        os.makedirs(d, exist_ok=True)
        f = os.path.join(d, name + '.docx')
        package(f, probes, mode)
        j = os.path.join(d, 'job.json')
        with open(j, 'w', newline='') as out:
            json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT, 'formats': ['docx', 'pdf']}, out)
        for x in (j, f):
            sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{name}/{os.path.basename(x)}\n')
        order.append(name + '\n')
        print(f'{name}: {len(probes)} probes, mode {mode}')
    open(os.path.join(HERE, 'probes-vml.tsv'), 'w', newline='').writelines(manifest)
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines(order)
    print(SET, len(order), 'files,', count, 'probes')

if __name__ == '__main__':
    main(sys.argv[1])
