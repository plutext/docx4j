# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 3: a round of probes for how Word draws a run's shadow, glow and reflection on WordArt, and the room
they take (phase3-findings.md section 2). Jason, 2026-11-13: "commence phase 3".

The probes are build_wordart.py's (one to a page, the page's first paragraph naming it, the WordArt a wps text box
anchored at 144 pt, 252 pt on its page, wrapNone, a 0.25 pt grey line so that Word's box shows). Unless the probe says
otherwise each is in Word's own form (spAutoFit, wrap="none", stored 144 by 144 pt, Word's insets, anchored at the
top), "WordArt" in Aptos 36 pt, black, centred, with no warp. The text is black and each effect's opacity at most 0.6
(a shadow black at w14:alpha 60000, so 0.40; a glow ED7D31 at 0.40; a reflection's stA 55000), so that
read_effects.py --detail can tell the ink from the effect.

The files are named we (WordArt effects) and wb70 (body text), not fx and fb as section 2 first had them: CR-081's
rounds used those names on the bridge (fx10 to fxr83, fb10 to fb50).

Round cr083-phase3:
  we10  a shadow's room: blur 0, 1, 2, 4, 8 pt by dist 0, 2, 6 pt at 90 degrees; blur 4, dist 3 at seven other
        directions; sharp shadows (no blur, no dist) scaled 50% and 150% about tl, ctr and b, and sy alone, sx alone
        (the frame: the scaled ink's bounds give the point it is scaled about)
  we11  a glow's room (1 to 25 pt); a reflection's: blur by dist, sy -50%, endPos 35% and 90%, dist swept 0 to 16 pt at
        90 and 270 degrees (the frame and the scale of dist); the glows and the reflections again on "Typography"
        (descenders)
  we12  size and face: three shadows and two glows at 12, 24, 72 pt and in Calibri and Times New Roman at 36 pt; a sharp
        shadow 12 pt long at 0 and 90 degrees at each, and at Aptos 36 pt (the scale, read exactly)
  we13  the room's placement: anchor ctr and b, a fixed box (noAutofit, wrap square) at t, ctr and b; two lines with the
        glow on the first, the second, both; two runs on one line, the glow on one; 72 pt beside 24 pt, the glow on
        each; the shadow and the reflection on "Typography"
  we14  which glyphs share a picture: two runs with different effects flat and on textPlain, textWave1, textInflate,
        textArchUp; two lines flat; textArchUp and textCircle with two lines; textButton with three; a glow and a
        reflection on textCircle
  we15  the silhouette: a shadow and a glow on hollow text, a 6 pt outline, a gradient fill, a fill with transparency,
        no fill and no outline
  we16  the inner shadow: sx="0" sy="0" algn="none" with blur, dist and dir swept; sx 0 alone, sy 0 alone; algn none at
        100%; sx and sy 0 with algn tl
  we17  theme colours: lumMod, lumMod with lumOff, shade, tint; w14:alpha 0 and 100000
  we18  a shape's own a:effectLst (outer shadow, glow) on a WordArt with no fill, its runs none: with and without a line
  we19  the box turned 30 degrees: a shadow at 90 degrees, a reflection, a glow, and the shadow on textArchUp
  wb70  body text: the line alone (36 pt "WordArt"), then a 36 pt line with the effect on its middle run, each followed
        by a 14 pt paragraph, so that the room above and below is read in each context (phase 5)
we10 and we14 are staged twice (-a, -b), so that the tolerances come from Word against itself.

Round cr083-phase3b (2026-11-13, after reading the first: phase3-findings.md section 4), for what it left open:
  we20  a reflection's distance and fade by size and face: sharp reflections at dist 0, 4 and 16 pt, and one with blur
        2 pt, at Aptos 12, 24, 72 and 96 pt and Calibri and Times New Roman at 36 pt (at 36 pt Aptos the reflection
        moved 2.70 times its dist, the fade spanned endPos times the ascent)
  we21  the scale past 72 pt: sharp shadows 12 pt long at 90 degrees and glows 18 pt at 96 and 144 pt
  we22  warped WordArt at other sizes (at 36 pt Word drew a warped run's effects at their stated size, flat text's at
        (size / 72)^0.7 of it): the shadow and the glow on textPlain at 12 and 72 pt, the shadow on textArchUp at 72
        pt, and a reflection 4 pt down on textPlain at 36 pt

Round cr083-phase3c (2026-11-13, Jason: "let's run the probes"), where the text's 1/300 inch grid is (phase3-findings.md
section 4: the text moves down by the room above in steps of 0.24 pt, the same room landing a step apart between faces):
  we23  the box moved down 0 to 0.27 pt in steps of 0.03 pt, with no effect and with a glow 3 pt (room above 1.385 pt);
        the room above swept across a step (1.20 to 1.44 pt in 0.03, by the glow's radius); Aptos at 33 to 40 pt, no effect

usage: build_effects.py <staging dir> [set]   (the set: cr083-phase3, the default, cr083-phase3b or cr083-phase3c; writes probes-effects.tsv beside this script; a column 'text' gives the
                                        line read_effects.py --text reads)"""
import hashlib, json, os, re, sys

import build_wordart as bw

HERE = os.path.dirname(os.path.abspath(__file__))
EMU = bw.EMU
SET = 'cr083-phase3'
TIMEOUT = 600
FIXED = (288 * EMU, 144 * EMU)
WORD_FORM = dict(autofit='spAutoFit', size=bw.WORD_BOX, ins=bw.INS, wrap='none')
NO_LINE = '<a:ln><a:noFill/></a:ln>'


def E(pt):
    return int(round(pt * EMU))


# ------------------------------------------------------------------------------------------------- the effects

def sh(blur, dist, deg=90, alpha=60000, val='000000', **kw):
    """w14:shadow, lengths in points, its direction in degrees"""
    return bw.shadow(val=val, alpha=alpha, blurRad=E(blur), dist=E(dist), dir=int(deg * 60000), **kw)


def gl(rad, val='ED7D31', alpha=60000):
    return bw.glow(E(rad), val, alpha)


def rf(blur=0.5, dist=0, deg=90, **kw):
    return bw.reflection(blurRad=E(blur), dist=E(dist), dir=int(deg * 60000), **kw)


def scheme_glow(rad, val, *mods, alpha=60000):
    """A glow whose colour is a theme colour with its transforms, then its alpha"""
    m = ''.join(f'<w14:{k} w14:val="{v}"/>' for k, v in mods) + f'<w14:alpha w14:val="{alpha}"/>'
    return f'<w14:glow w14:rad="{E(rad)}"><w14:schemeClr w14:val="{val}">{m}</w14:schemeClr></w14:glow>'


SH_A = sh(4, 3, 45)              # wa30-17's: room 1.20 above
SH_B = sh(0, 6, 45)              # wa30-19's: drawn 2.50 across and down
SH_C = sh(8, 2, 90)
GL5, GL8, GL18 = gl(5), gl(8), gl(18)
INNER = dict(sx=0, sy=0, algn='none')


def fx(*parts):
    return bw.effects(*parts)


# ------------------------------------------------------------------------------------------------- the probes

class Art(bw.Art):
    """build_wordart's WordArt in Word's form unless told otherwise, with a turn (a:xfrm rot, in degrees) and a shape
    effect list (a:effectLst in wps:spPr)"""

    def __init__(self, note, paras, rot=0, spfx='', **kw):
        for k, v in WORD_FORM.items():
            kw.setdefault(k, v)
        super().__init__(note, paras, **kw)
        self.rot, self.spfx = rot, spfx

    def graphic(self):
        g = super().graphic()
        if self.rot:
            g = g.replace('<a:xfrm>', f'<a:xfrm rot="{int(self.rot * 60000)}">', 1)
        if self.spfx:
            g = g.replace('</wps:spPr>', self.spfx + '</wps:spPr>', 1)
        return g

    def _summary(self):
        s = super().summary()
        if self.rot:
            s += f'; turned {self.rot:g} degrees'
        if self.spfx:
            s += '; spPr ' + ' '.join(re.findall(r'<a:(outerShdw|glow|innerShdw|reflection)\b', self.spfx))
        return s

    def text(self):
        return ''.join(r.text for r in self.paras[0]) if self.paras else ''

    def page(self, n):
        out = super().page(n)
        dy = getattr(self, 'dy', 0)
        if not dy:
            return out
        a = f'<wp:posOffset>{bw.Y0}</wp:posOffset></wp:positionV>'
        return (out[0].replace(a, f'<wp:posOffset>{bw.Y0 + dy}</wp:posOffset></wp:positionV>', 1),) + tuple(out[1:])

    def summary(self):
        s = self._summary()
        dy = getattr(self, 'dy', 0)
        return s + (f'; moved down {dy / EMU:g} pt' if dy else '')


def one(text='WordArt', **run):
    return bw.lines([text], **run)


class Body:
    """One page of body text: "WordArt" at 36 pt alone on its line with the effect, a 14 pt paragraph, then a 36 pt
    line whose middle run has the effect, and a 14 pt paragraph: the room above and below in each context."""
    kind = 'body'

    def __init__(self, note, fx_xml):
        self.note, self.fx_xml = note, fx_xml

    def summary(self):
        return ' '.join(m for m in re.findall(r'<w14:(\w+)', self.fx_xml) if m in bw.ORDER) or 'no effect'

    def text(self):
        return 'WordArt'

    def page(self, n):
        R = bw.Run
        p = bw.para
        return '', (p([R('WordArt', fx=self.fx_xml)], jc='left', before=720)
                    + p([R('The paragraph after the line alone.', size=14)], jc='left')
                    + p([R('Plain ', size=36), R('WordArt', fx=self.fx_xml), R(' plain', size=36)], jc='left', before=480)
                    + p([R('The paragraph after the line with the run in it.', size=14)], jc='left'))


# ----------------------------------------------------------------------------------------------- the families

def fam_shadow_room():
    out = []
    for dist in (0, 2, 6):
        for blur in (0, 1, 2, 4, 8):
            out.append(Art(f'shadow blur {blur} pt, dist {dist} pt at 90 degrees', one(fx=sh(blur, dist, 90))))
    for deg in (0, 45, 135, 180, 225, 270, 315):
        out.append(Art(f'shadow blur 4 pt, dist 3 pt at {deg} degrees', one(fx=sh(4, 3, deg))))
    for s in (50, 150):
        for algn in ('tl', 'ctr', 'b'):
            out.append(Art(f'shadow sharp, no dist, sx and sy {s}% about {algn}',
                           one(fx=sh(0, 0, sx=s * 1000, sy=s * 1000, algn=algn))))
    out.append(Art('shadow sharp, no dist, sy 50% alone about b', one(fx=sh(0, 0, sy=50000, algn='b'))))
    out.append(Art('shadow sharp, no dist, sx 50% alone about l', one(fx=sh(0, 0, sx=50000, algn='l'))))
    return out


def fam_glow_reflection_room():
    out = [Art(f'glow {r} pt', one(fx=gl(r))) for r in (1, 2, 3, 5, 8, 12, 18, 25)]
    for dist in (0, 4):
        for blur in (0, 0.5, 2, 6):
            out.append(Art(f'reflection blur {blur:g} pt, dist {dist} pt', one(fx=rf(blur, dist))))
    for dist in (0, 4):
        out.append(Art(f'reflection blur 0.5 pt, dist {dist} pt, sy -50%', one(fx=rf(0.5, dist, sy=-50000))))
    for end in (35, 90):
        out.append(Art(f'reflection blur 0.5 pt, endPos {end}%', one(fx=rf(0.5, 0, endPos=end * 1000))))
    for dist in (1, 2, 8, 16):
        out.append(Art(f'reflection sharp, dist {dist} pt at 90 degrees', one(fx=rf(0, dist, 90))))
    for dist in (1, 2, 4, 8, 16):
        out.append(Art(f'reflection sharp, dist {dist} pt at 270 degrees', one(fx=rf(0, dist, 270))))
    t = 'Typography'
    for r in (3, 8, 18):
        out.append(Art(f'"{t}", glow {r} pt', one(t, fx=gl(r))))
    for dist in (0, 1, 2, 4, 8, 16):
        out.append(Art(f'"{t}", reflection sharp, dist {dist} pt at 90 degrees', one(t, fx=rf(0, dist, 90))))
    out.append(Art(f'"{t}", reflection blur 0.5 pt (the default here)', one(t, fx=rf())))
    out.append(Art(f'"{t}", reflection blur 0.5 pt, sy -50%', one(t, fx=rf(0.5, 0, sy=-50000))))
    return out


SIZES_FACES = [('Aptos 12 pt', dict(size=12)), ('Aptos 24 pt', dict(size=24)), ('Aptos 72 pt', dict(size=72)),
               ('Calibri 36 pt', dict(font='Calibri')), ('Times New Roman 36 pt', dict(font='Times New Roman'))]


def fam_size_face():
    out = []
    for label, run in SIZES_FACES:
        for note, e in (('shadow blur 4 pt, dist 3 pt at 45 degrees', SH_A), ('shadow sharp, dist 6 pt at 45 degrees', SH_B),
                        ('shadow blur 8 pt, dist 2 pt at 90 degrees', SH_C), ('glow 5 pt', GL5), ('glow 18 pt', GL18)):
            out.append(Art(f'{label}, {note}', one(fx=e, **run)))
    for label, run in [('Aptos 36 pt', {})] + SIZES_FACES:
        for deg in (0, 90):
            out.append(Art(f'{label}, shadow sharp, dist 12 pt at {deg} degrees', one(fx=sh(0, 12, deg), **run)))
    return out


def fam_room_lines():
    R = bw.Run
    fixed = dict(autofit='noAutofit', size=FIXED, wrap='square')
    t = 'Typography'
    return [
        Art('glow 8 pt, anchored at the middle', one(fx=GL8), anchor='ctr'),
        Art('glow 8 pt, anchored at the foot', one(fx=GL8), anchor='b'),
        Art('glow 8 pt, a fixed box 288 by 144 pt (noAutofit, wrap square), anchored at the top', one(fx=GL8), **fixed),
        Art('glow 8 pt, a fixed box 288 by 144 pt, anchored at the middle', one(fx=GL8), anchor='ctr', **fixed),
        Art('glow 8 pt, a fixed box 288 by 144 pt, anchored at the foot', one(fx=GL8), anchor='b', **fixed),
        Art('two lines, glow 8 pt on the first', [[R('WordArt', fx=GL8)], [R('Two lines')]]),
        Art('two lines, glow 8 pt on the second', [[R('WordArt')], [R('Two lines', fx=GL8)]]),
        Art('two lines, glow 8 pt on both', bw.lines(bw.TWO, fx=GL8)),
        Art('two runs "Word" "Art", glow 8 pt on "Word"', [[R('Word', fx=GL8), R('Art')]]),
        Art('two runs "Word" "Art", glow 8 pt on "Art"', [[R('Word'), R('Art', fx=GL8)]]),
        Art('"Word" 72 pt, "Art" 24 pt with glow 8 pt', [[R('Word', size=72), R('Art', size=24, fx=GL8)]]),
        Art('"Word" 72 pt with glow 8 pt, "Art" 24 pt', [[R('Word', size=72, fx=GL8), R('Art', size=24)]]),
        Art(f'"{t}", shadow blur 4 pt, dist 3 pt at 45 degrees', one(t, fx=SH_A)),
        Art(f'"{t}", reflection sharp, dist 4 pt', one(t, fx=rf(0, 4))),
    ]


GL_BLUE = gl(5, '4472C4')


def fam_units():
    R = bw.Run
    out = []
    for w in ('textNoShape', 'textPlain', 'textWave1', 'textInflate', 'textArchUp'):
        where = 'flat' if w == 'textNoShape' else w
        out.append(Art(f'{where}, two runs: "Word" glow 5 pt orange, "Art" glow 5 pt blue',
                       [[R('Word', fx=GL5), R('Art', fx=GL_BLUE)]], warp=w))
        if w != 'textArchUp':
            out.append(Art(f'{where}, two runs: "Word" glow 5 pt, "Art" shadow blur 4 pt, dist 3 pt at 45 degrees',
                           [[R('Word', fx=GL5), R('Art', fx=SH_A)]], warp=w))
    out += [
        Art('flat, two lines, glow 5 pt on both', bw.lines(bw.TWO, fx=GL5)),
        Art('flat, two lines, glow 5 pt orange then blue', [[R('WordArt', fx=GL5)], [R('Two lines', fx=GL_BLUE)]]),
        Art('textArchUp, two lines, glow 5 pt on both', bw.lines(bw.TWO, fx=GL5), warp='textArchUp'),
        Art('textCircle, two lines, glow 5 pt on both', bw.lines(bw.TWO, fx=GL5), warp='textCircle'),
        Art('textButton, three lines, glow 5 pt on all', bw.lines(bw.THREE, fx=GL5), warp='textButton'),
        Art('textCircle, glow 5 pt', one(fx=GL5), warp='textCircle'),
        Art('textCircle, reflection blur 0.5 pt', one(fx=rf()), warp='textCircle'),
    ]
    return out


def fam_silhouettes():
    black = bw.solid('000000')
    fills = [
        ('hollow (no fill, a 1 pt black outline)', [bw.text_fill(bw.NOFILL), bw.outline(12700, black)]),
        ('a 6 pt dark blue outline on the black fill', [bw.outline(76200, bw.solid('1F3864'))]),
        ('a gradient fill red to blue', [bw.text_fill(bw.grad([(0, 'FF0000'), (100000, '0000FF')], lin=0))]),
        ('a black fill at w14:alpha 50000', [bw.text_fill(bw.solid('000000', 50000))]),
        ('no fill and no outline', [bw.text_fill(bw.NOFILL), bw.outline(12700, fill=bw.NOFILL)]),
    ]
    out = []
    for note, parts in fills:
        out.append(Art(f'{note}, shadow blur 4 pt, dist 3 pt at 45 degrees', one(fx=fx(SH_A, *parts))))
        out.append(Art(f'{note}, glow 5 pt', one(fx=fx(GL5, *parts))))
    return out


def fam_inner():
    out = []
    style15 = dict(alpha=50000)
    for blur in (0, 2, 5, 10):
        out.append(Art(f'sx 0, sy 0, algn none, blur {blur} pt, dist 4 pt at 225 degrees',
                       one(fx=sh(blur, 4, 225, **style15, **INNER))))
    for dist in (0, 2, 8):
        out.append(Art(f'sx 0, sy 0, algn none, blur 5 pt, dist {dist} pt at 225 degrees',
                       one(fx=sh(5, dist, 225, **style15, **INNER))))
    out.append(Art('sx 0, sy 0, algn none, blur 5 pt, dist 4 pt at 45 degrees', one(fx=sh(5, 4, 45, **style15, **INNER))))
    out.append(Art('sx 0 alone (sy 100%), algn none, blur 5 pt, dist 4 pt at 225 degrees',
                   one(fx=sh(5, 4, 225, **style15, sx=0, sy=100000, algn='none'))))
    out.append(Art('sy 0 alone (sx 100%), algn none, blur 5 pt, dist 4 pt at 225 degrees',
                   one(fx=sh(5, 4, 225, **style15, sx=100000, sy=0, algn='none'))))
    out.append(Art('sx and sy 100%, algn none, blur 5 pt, dist 4 pt at 225 degrees',
                   one(fx=sh(5, 4, 225, **style15, sx=100000, sy=100000, algn='none'))))
    out.append(Art('sx 0, sy 0, algn tl, blur 5 pt, dist 4 pt at 225 degrees',
                   one(fx=sh(5, 4, 225, **style15, sx=0, sy=0, algn='tl'))))
    return out


def fam_colours():
    return [
        Art('glow 8 pt, accent2 lumMod 75%', one(fx=scheme_glow(8, 'accent2', ('lumMod', 75000)))),
        Art('glow 8 pt, accent2 lumMod 60% lumOff 40%', one(fx=scheme_glow(8, 'accent2', ('lumMod', 60000), ('lumOff', 40000)))),
        Art('glow 8 pt, accent2 shade 50%', one(fx=scheme_glow(8, 'accent2', ('shade', 50000)))),
        Art('glow 8 pt, accent2 tint 40%', one(fx=scheme_glow(8, 'accent2', ('tint', 40000)))),
        Art('glow 8 pt, ED7D31 at w14:alpha 0 (opaque)', one(fx=gl(8, alpha=0))),
        Art('glow 8 pt, ED7D31 at w14:alpha 100000 (transparent)', one(fx=gl(8, alpha=100000))),
    ]


SP_SHADOW = ('<a:effectLst><a:outerShdw blurRad="50800" dist="38100" dir="2700000" algn="tl" rotWithShape="0">'
             '<a:srgbClr val="000000"><a:alpha val="40000"/></a:srgbClr></a:outerShdw></a:effectLst>')
SP_GLOW = ('<a:effectLst><a:glow rad="101600"><a:srgbClr val="ED7D31"><a:alpha val="40000"/></a:srgbClr></a:glow>'
           '</a:effectLst>')


def fam_shape_effects():
    out = []
    for note, e in (('an outer shadow 4 pt blur, 3 pt at 45 degrees, black at 40%', SP_SHADOW),
                    ('a glow 8 pt, ED7D31 at 40%', SP_GLOW)):
        out.append(Art(f'the shape\'s own effect list: {note}; no fill, no line, runs without effects', one(),
                       ln=NO_LINE, spfx=e))
        out.append(Art(f'the shape\'s own effect list: {note}; no fill, the grey line, runs without effects', one(),
                       spfx=e))
    return out


def fam_rotated():
    return [
        Art('turned 30 degrees, shadow sharp, dist 6 pt at 90 degrees', one(fx=sh(0, 6, 90)), rot=30),
        Art('turned 30 degrees, reflection sharp, dist 4 pt', one(fx=rf(0, 4)), rot=30),
        Art('turned 30 degrees, glow 8 pt', one(fx=GL8), rot=30),
        Art('turned 30 degrees, textArchUp, shadow sharp, dist 6 pt at 90 degrees', one(fx=sh(0, 6, 90)),
            warp='textArchUp', rot=30),
    ]


def fam_body_room():
    return [Body(note, e) for note, e in [
        ('no effect (the reference)', ''),
        ('shadow sharp, dist 6 pt at 90 degrees', sh(0, 6, 90)),
        ('shadow blur 4 pt, no dist', sh(4, 0)),
        ('shadow blur 4 pt, dist 6 pt at 90 degrees', sh(4, 6, 90)),
        ('shadow blur 8 pt, dist 2 pt at 90 degrees', SH_C),
        ('shadow blur 4 pt, dist 3 pt at 270 degrees', sh(4, 3, 270)),
        ('shadow blur 4 pt, dist 3 pt at 45 degrees', SH_A),
        ('glow 2 pt', gl(2)),
        ('glow 5 pt', GL5),
        ('glow 12 pt', gl(12)),
        ('glow 25 pt', gl(25)),
        ('reflection blur 0.5 pt', rf()),
        ('reflection sharp, dist 4 pt', rf(0, 4)),
        ('reflection sharp, dist 16 pt', rf(0, 16)),
        ('reflection blur 0.5 pt, sy -50%', rf(0.5, 0, sy=-50000)),
        ('reflection blur 0.5 pt, endPos 90%', rf(0.5, 0, endPos=90000)),
    ]]


def fam_reflection_size():
    out = []
    for label, run in [('Aptos 12 pt', dict(size=12)), ('Aptos 24 pt', dict(size=24)), ('Aptos 72 pt', dict(size=72)),
                       ('Aptos 96 pt', dict(size=96)), ('Calibri 36 pt', dict(font='Calibri')),
                       ('Times New Roman 36 pt', dict(font='Times New Roman'))]:
        for dist in (0, 4, 16):
            out.append(Art(f'{label}, reflection sharp, dist {dist} pt', one(fx=rf(0, dist), **run)))
        out.append(Art(f'{label}, reflection blur 2 pt, dist 0 pt', one(fx=rf(2, 0), **run)))
    return out


def fam_past_72():
    out = []
    for size in (96, 144):
        out.append(Art(f'Aptos {size} pt, shadow sharp, dist 12 pt at 90 degrees', one(fx=sh(0, 12, 90), size=size)))
        out.append(Art(f'Aptos {size} pt, glow 18 pt', one(fx=GL18, size=size)))
    return out


def fam_warped_size():
    out = []
    for size in (12, 72):
        out.append(Art(f'textPlain, Aptos {size} pt, shadow blur 4 pt, dist 3 pt at 45 degrees', one(fx=SH_A, size=size), warp='textPlain'))
        out.append(Art(f'textPlain, Aptos {size} pt, glow 5 pt', one(fx=GL5, size=size), warp='textPlain'))
    out.append(Art('textArchUp, Aptos 72 pt, shadow blur 4 pt, dist 3 pt at 45 degrees', one(fx=SH_A, size=72), warp='textArchUp'))
    out.append(Art('textPlain, Aptos 36 pt, reflection sharp, dist 4 pt', one(fx=rf(0, 4)), warp='textPlain'))
    return out


FILES = [
    ('we10-shadow-room-a', fam_shadow_room),
    ('we10-shadow-room-b', fam_shadow_room),
    ('we11-glow-reflection-room', fam_glow_reflection_room),
    ('we12-room-size-face', fam_size_face),
    ('we13-room-lines', fam_room_lines),
    ('we14-units-a', fam_units),
    ('we14-units-b', fam_units),
    ('we15-silhouettes', fam_silhouettes),
    ('we16-inner', fam_inner),
    ('we17-colours', fam_colours),
    ('we18-shape-effects', fam_shape_effects),
    ('we19-rotated', fam_rotated),
    ('wb70-body-room', fam_body_room),
]


def moved(art, pt):
    art.dy = int(round(pt * EMU))
    return art


def fam_grid():
    out = []
    for k in range(10):
        out.append(moved(Art(f'no effect, the box moved down {0.03 * k:.2f} pt', one()), 0.03 * k))
    for k in range(10):
        out.append(moved(Art(f'glow 3 pt, the box moved down {0.03 * k:.2f} pt', one(fx=gl(3))), 0.03 * k))
    s36 = 0.75 * (36 / 72) ** 0.7
    for j in range(9):
        above = 1.20 + 0.03 * j
        out.append(Art(f'glow {above / s36:.3f} pt (room above {above:.2f} pt)', one(fx=gl(above / s36))))
    for size in (33, 34, 35, 37, 38, 39, 40):
        out.append(Art(f'no effect, Aptos {size} pt', one(size=size)))
    return out


FILES_B = [
    ('we20-reflection-size', fam_reflection_size),
    ('we21-past-72', fam_past_72),
    ('we22-warped-size', fam_warped_size),
]

FILES_C = [('we23-grid', fam_grid)]

SETS = {SET: FILES, 'cr083-phase3b': FILES_B, 'cr083-phase3c': FILES_C}


def main(staging, only=SET):
    manifest = ['set\tfile\tpage\tprobe\tkind\tmode\tnote\tsummary\ttext\n']
    for setname, files in SETS.items():
        for name, fam in files:
            prefix = name.split('-')[0]
            for page, p in enumerate(fam(), 1):
                manifest.append(f'{setname}\t{name}\t{page}\t{prefix}-{page:02d}\t{p.kind}\t15\t{p.note}\t{p.summary()}\t{p.text()}\n')
    open(os.path.join(HERE, 'probes-effects.tsv'), 'w', newline='').writelines(manifest)
    sums, order, count, unique = [], [], 0, 0
    for name, fam in SETS[only]:
        prefix = name.split('-')[0]
        probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(fam())]
        count += len(probes)
        unique += 0 if name.endswith('-b') else len(probes)
        d = os.path.join(staging, only, name)
        os.makedirs(d, exist_ok=True)
        f = os.path.join(d, name + '.docx')
        bw.package(f, probes, 15)
        j = os.path.join(d, 'job.json')
        with open(j, 'w', newline='') as out:
            json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT, 'formats': ['docx', 'pdf']}, out)
        for x in (j, f):
            sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {only}/{name}/{os.path.basename(x)}\n')
        order.append(name + '\n')
    open(os.path.join(staging, only + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, only + '.order.txt'), 'w', newline='').writelines(order)
    print(only, len(order), 'files,', count, 'probes,', unique, 'of them distinct')


if __name__ == '__main__':
    main(*sys.argv[1:])
