# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 0: purpose-built probes for how Word draws DrawingML effects in a PDF, and how its save normalises them.
Jason, 2026-11-09: "CR-081's phase 0 text to name the word-bridge route then Go ahead to build the probe generator and
stage the jobs under bridge/staging/". The route is CR-081 section 6.

One family to a file, one probe to a page. The page's first paragraph names the probe and states its effect's XML, so
that a page of Word's PDF can be matched to its probe; probes.tsv, written beside this script, lists every probe with
its file and page. Each file is staged twice (-a, -b), so that a tolerance can come from Word against itself.

The sets:
  cr081-phase0     the effects in two dimensions, on pictures and shapes; preset shadows; effectRef; wrap; effect extent
  cr081-phase0-3d  scene3d and sp3d on pictures and shapes, with a longer timeout
  cr081-phase0-r2  round 2: the inline families again with a generous effect extent, since Word clips an inline
                   drawing's effects to its paragraph's height; the preset shadows on a shape
  cr081-phase0-r3  round 3: the reflection's blur with no fade, and its offset (dist along dir)
  cr081-phase0-styles  the 28 picture styles: Jason's hand-made document (cr081-styles-hand.docx, staged as it is),
                   and Word's XML for each style (styles-word.tsv, harvested from it by build_styles_start.py) over the
                   photo, the photo as a JPEG and both line arts, each with the effect extent Word gave the style
  cr081-phase4-r1  after phase 4 (2026-11-11; Jason: "build and stage the round"): the preset shadows at other dists
                   and dirs, in other colours, on other outlines, with other effects, and on other sizes
  cr081-phase5a-r1 phase 5a's round (2026-11-11; Jason: "CR-081 phase 5a: start it"): the 3-D geometry. All 62
                   cameras on two shapes (144 by 108 pt, 72 by 144 pt) and some on the photo; rot, fov and zoom; depth,
                   contour, z and bevels under turned cameras; and the frame in the layout. Each drawing but the layout
                   family's is anchored at a fixed place on the page (216 pt, 216 pt), in front of the text, so that its
                   frame is known exactly
  cr081-phase5a-r2 phase 5a's second round: the cameras again with 36 pt of depth, since a flat drawing can't tell a
                   view from its mirror and shows no oblique; and the bevels' profiles, seen edge on (lon 90)
  cr081-phase5a-r3 phase 5a's third round (2026-11-11; Jason: "please do the measures: text on a 3-D shape, and how a
                   shadow on a 3-D drawing is aligned"): a shape's text under turned cameras, with and without
                   a:flatTx; and an unblurred shadow on 3-D drawings, at algn tl, ctr and br, scaled 50%
The hostile probes (D10) are unit tests.

The pictures are synthetic: photo.png, a gradient with a red disc at its top left, so that a flip shows; photo.jpeg,
the same as a JPEG; lineart.png, black lines on white; lineart-alpha.png, the same lines on a transparent ground. Every
package holds a settings part at compatibility mode 15 and a theme (2023, unless the family is about the theme).

usage: build_phase0.py <staging dir> [set ...]   (every set if none is named; probes.tsv always lists every set)"""
import csv, hashlib, io, json, os, re, shutil, sys, zipfile
from xml.sax.saxutils import escape
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.normpath(os.path.join(HERE, '..', '..', '..', '..'))
THEMES = os.path.join(REPO, 'docx4j-core', 'src', 'main', 'resources', 'org', 'docx4j', 'openpackaging', 'parts',
                      'WordprocessingML')

NS = {
    'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
    'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships',
    'wp': 'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing',
    'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
    'pic': 'http://schemas.openxmlformats.org/drawingml/2006/picture',
    'wps': 'http://schemas.microsoft.com/office/word/2010/wordprocessingShape',
    'mc': 'http://schemas.openxmlformats.org/markup-compatibility/2006',
}
DECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
URI_PIC = 'http://schemas.openxmlformats.org/drawingml/2006/picture'
URI_WPS = 'http://schemas.microsoft.com/office/word/2010/wordprocessingShape'
SECT = ('<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" '
        'w:header="708" w:footer="708" w:gutter="0"/></w:sectPr>')
SETTINGS = (f'{DECL}<w:settings xmlns:w="{NS["w"]}"><w:compat><w:compatSetting w:name="compatibilityMode" '
            'w:uri="http://schemas.microsoft.com/office/word" w:val="15"/></w:compat></w:settings>')

CX, CY = 1828800, 1371600          # every picture and shape: 2 in by 1.5 in (144 pt by 108 pt), the pictures' 4:3

# ---------------------------------------------------------------------------------------------------------- pictures

def _png(img):
    b = io.BytesIO()
    img.save(b, 'PNG', optimize=False)
    return b.getvalue()

def photo():
    w, h = 480, 360
    img = Image.new('RGB', (w, h))
    px = img.load()
    for y in range(h):
        for x in range(w):
            px[x, y] = (40 + 180 * x // w, 90 + 100 * y // h, 200 - 120 * x // w)
    d = ImageDraw.Draw(img)
    d.polygon([(0, 300), (140, 230), (300, 280), (480, 210), (480, 360), (0, 360)], fill=(30, 100, 50))
    d.ellipse([30, 30, 110, 110], fill=(210, 30, 30))          # the mark, top left: a flip or a turn shows
    return _png(img)

def lineart(alpha):
    w, h = 480, 360
    img = Image.new('RGBA', (w, h), (255, 255, 255, 0 if alpha else 255))
    d = ImageDraw.Draw(img)
    ink = (0, 0, 0, 255)
    d.line([(60, 300), (60, 60), (200, 60)], fill=ink, width=10)   # an F, asymmetric
    d.line([(60, 170), (160, 170)], fill=ink, width=10)
    d.ellipse([260, 60, 420, 220], outline=ink, width=8)
    d.line([(240, 300), (290, 250), (340, 300), (390, 250), (440, 300)], fill=ink, width=6)
    if not alpha:
        img = img.convert('RGB')
    return _png(img)

def photo_jpeg():
    b = io.BytesIO()
    Image.open(io.BytesIO(photo())).save(b, 'JPEG', quality=90)
    return b.getvalue()

PICTURES = {'photo.png': photo, 'photo.jpeg': photo_jpeg, 'lineart.png': lambda: lineart(False),
            'lineart-alpha.png': lambda: lineart(True)}

# ------------------------------------------------------------------------------------------------------------ colours

def srgb(val, alpha=None, **mods):
    return _clr('srgbClr', val, alpha, mods)

def scheme(val, alpha=None, **mods):
    return _clr('schemeClr', val, alpha, mods)

def prst(val, alpha=None):
    return _clr('prstClr', val, alpha, {})

def _clr(el, val, alpha, mods):
    inner = ''.join(f'<a:{k} val="{v}"/>' for k, v in mods.items())
    if alpha is not None:
        inner += f'<a:alpha val="{alpha}"/>'
    return f'<a:{el} val="{val}">{inner}</a:{el}>' if inner else f'<a:{el} val="{val}"/>'

BLACK40 = srgb('000000', 40000)

# ------------------------------------------------------------------------------------------------------------ effects

def _attrs(d):
    return ''.join(f' {k}="{v}"' for k, v in d.items() if v is not None)

def outer(color=BLACK40, **a):
    return f'<a:outerShdw{_attrs(a)}>{color}</a:outerShdw>'

def inner(color=None, **a):
    return f'<a:innerShdw{_attrs(a)}>{color or prst("black", 50000)}</a:innerShdw>'

def glow(rad, color=None):
    return f'<a:glow rad="{rad}">{color or scheme("accent1", 40000)}</a:glow>'

def soft(rad):
    return f'<a:softEdge rad="{rad}"/>'

def refl(**a):
    return f'<a:reflection{_attrs(a)}/>'

def prstshdw(p, color=None, **a):
    return f'<a:prstShdw prst="{p}"{_attrs(a)}>{color or prst("black")}</a:prstShdw>'

ORDER = ['glow', 'innerShdw', 'outerShdw', 'prstShdw', 'reflection', 'softEdge']    # CT_EffectList's sequence

def effects(*parts):
    """An a:effectLst of the parts, put in the schema's order whatever order they're given in."""
    parts = sorted(parts, key=lambda x: ORDER.index(re.match(r'<a:(\w+)', x).group(1)))
    return '<a:effectLst>' + ''.join(parts) + '</a:effectLst>'

# Word's own presets, as Word writes them, the bases of the sweeps
OUTER = dict(blurRad=50800, dist=38100, dir=2700000, algn='tl', rotWithShape=0)      # Offset: bottom right
INNER = dict(blurRad=63500, dist=50800, dir=13500000)                                  # Inside: top left
REFL = dict(blurRad=6350, stA=52000, endA=300, endPos=35000, dir=5400000, sy=-100000, algn='bl', rotWithShape=0)

def o(color=BLACK40, **over):
    return outer(color, **{**OUTER, **over})

def rf(**over):
    return refl(**{**REFL, **over})

# ------------------------------------------------------------------------------------------------------------- probes

class Probe:
    """One page: a picture (pic) or a shape (wps), inline unless anchor is given."""
    def __init__(self, note, sppr='', picture='photo.png', geom='rect', ln='', rot=None, src_rect='', shape=False,
                 fill='<a:solidFill><a:srgbClr val="4472C4"/></a:solidFill>', style='', text='', anchor=None,
                 extent='zero', extent_est=(0, 0, 0, 0), in_line=False, scene='', raw='', cx=CX, cy=CY, body=''):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def sp_pr(self):
        if self.raw:     # a picture style: Word's spPr after its xfrm, as it is
            return f'<a:xfrm><a:off x="0" y="0"/><a:ext cx="{self.cx}" cy="{self.cy}"/></a:xfrm>{self.raw}'
        rot = f' rot="{self.rot}"' if self.rot else ''
        geom = f'<a:prstGeom prst="{self.geom}"><a:avLst/></a:prstGeom>'
        fill = self.fill if self.shape else ''
        return f'<a:xfrm{rot}><a:off x="0" y="0"/><a:ext cx="{self.cx}" cy="{self.cy}"/></a:xfrm>{geom}{fill}{self.ln}{self.sppr}{self.scene}'

def pic(note, *eff, **kw):
    return Probe(note, effects(*eff) if eff else '', **kw)

def shp(note, *eff, **kw):
    return Probe(note, effects(*eff) if eff else '', shape=True, **kw)

def describe(xml):
    """The XML as words: element names and attribute values, for the page's label."""
    s = re.sub(r'</[^>]+>', '', xml)
    s = re.sub(r'<a:|<', ' ', s).replace('/>', ' ').replace('>', ' ').replace('"', '')
    return re.sub(r'\s+', ' ', s).strip()

# The families: (file, [probe ...]). Ids are <file prefix>-<nn>, numbered in order.

DIRS = [0, 2700000, 5400000, 8100000, 10800000, 13500000, 16200000, 18900000]
ALIGNS = ['tl', 't', 'tr', 'l', 'ctr', 'r', 'bl', 'b', 'br']

def fam_outer_offset():
    return ([pic(f'dist {d}', o(dist=d)) for d in (0, 12700, 38100, 76200, 152400, 304800)]
            + [pic(f'dir {d}', o(dist=101600, dir=d)) for d in DIRS])

def fam_outer_blur():
    return ([pic(f'blur {b}', o(blurRad=b, dist=76200)) for b in (0, 12700, 25400, 50800, 101600, 203200, 406400)]
            + [pic(f'blur {b}, no offset', o(blurRad=b, dist=0)) for b in (25400, 101600, 406400)]
            + [pic(f'blur {b}, opaque, far', o(blurRad=b, dist=304800, color=srgb('000000'))) for b in (0, 101600)])

PERSPECTIVE = [   # shadows written as Word's perspective presets are; the hand-made styles document has Word's own
    ('perspective upper left', dict(blurRad=76200, dist=0, dir=13500000, sy=23000, kx=1200000, algn='br', rotWithShape=0)),
    ('perspective upper right', dict(blurRad=76200, dist=0, dir=18900000, sy=23000, kx=-1200000, algn='bl', rotWithShape=0)),
    ('perspective below', dict(blurRad=152400, dist=317500, dir=5400000, sx=90000, sy=-19000, rotWithShape=0)),
    ('perspective lower left', dict(blurRad=76200, dist=12700, dir=8100000, sy=-23000, kx=800400, algn='br', rotWithShape=0)),
    ('perspective lower right', dict(blurRad=76200, dist=12700, dir=2700000, sy=-23000, kx=-800400, algn='bl', rotWithShape=0)),
]

def fam_outer_scale():
    base = dict(blurRad=0, dist=152400, dir=0, algn='ctr', rotWithShape=0)
    scale = [pic(f'sx {sx} sy {sy}', outer(**base, sx=sx, sy=sy))
             for sx, sy in ((100000, 100000), (50000, 50000), (90000, 90000), (110000, 110000), (100000, 50000),
                            (50000, 100000), (100000, -100000), (-100000, 100000))]
    skew = [pic(f'kx {kx} ky {ky}', outer(**base, kx=kx, ky=ky)) for kx, ky in ((1200000, 0), (-1200000, 0), (0, 600000))]
    align = [pic(f'algn {a} at 120%', outer(blurRad=0, dist=0, sx=120000, sy=120000, algn=a, rotWithShape=0)) for a in ALIGNS]
    persp = [pic(n, outer(**a)) for n, a in PERSPECTIVE]
    rot = [pic(f'picture turned 30 degrees, rotWithShape {r}', outer(blurRad=25400, dist=152400, dir=5400000, rotWithShape=r),
               rot=1800000) for r in (0, 1)]
    return scale + skew + align + persp + rot

def fam_outer_colour():
    return [pic(n, o(color=c)) for n, c in (
        ('black, opaque', srgb('000000')), ('black 40%', BLACK40), ('red 60%', srgb('FF0000', 60000)),
        ('accent1', scheme('accent1')), ('accent1 lumMod 50%', scheme('accent1', lumMod=50000)),
        ('preset black 40%', prst('black', 40000)))]

def fam_inner():
    return ([pic(f'blur {b}', inner(**{**INNER, 'blurRad': b})) for b in (0, 25400, 63500, 127000, 254000)]
            + [pic(f'dist {d}', inner(**{**INNER, 'dist': d})) for d in (0, 25400, 101600, 203200)]
            + [pic(f'dir {d}', inner(**{**INNER, 'dir': d})) for d in DIRS if d != 13500000]
            + [pic('red, opaque', inner(srgb('FF0000'), **INNER)), pic('accent1', inner(scheme('accent1'), **INNER))])

def fam_glow():
    return ([pic(f'rad {r}', glow(r)) for r in (25400, 63500, 101600, 139700, 228600, 406400)]
            + [pic('rad 139700 accent1 satMod 175% alpha 40% (as Word writes it)',
                   glow(139700, scheme('accent1', 40000, satMod=175000)))]
            + [pic(n, glow(139700, c)) for n, c in (('red 60%', srgb('FF0000', 60000)), ('black, opaque', srgb('000000')),
                                                    ('green 40%', srgb('00B050', 40000)))])

def fam_softedge():
    return [pic(f'rad {r}', soft(r)) for r in (12700, 31750, 63500, 127000, 317500, 635000, 1000000)]

def fam_reflection():
    sweeps = [('endPos', (20000, 55000, 90000, 100000)), ('stA', (100000, 30000)), ('endA', (0, 50000)),
              ('stPos', (20000, 50000)), ('dist', (38100, 50800, 101600)), ('blurRad', (0, 25400, 101600)),
              ('dir', (0, 10800000, 16200000)), ('fadeDir', (0, 10800000, 16200000))]
    out = [pic('tight reflection, touching (the base)', rf())]
    for k, vals in sweeps:
        out += [pic(f'{k} {v}', rf(**{k: v})) for v in vals]
    out += [pic('upward: dir 16200000 algn tl', rf(dir=16200000, algn='tl')),
            pic('sy -50000', rf(sy=-50000)), pic('sy -150000', rf(sy=-150000)),
            pic('to the side: sx -100000 sy 100000 dir 0 algn tr', rf(sx=-100000, sy=100000, dir=0, algn='tr')),
            pic('kx 1200000', rf(kx=1200000)), pic('ky 600000', rf(ky=600000))]
    out += [pic(f'algn {a}', rf(algn=a)) for a in ('tl', 'ctr', 'b', 'br')]
    out += [pic(f'picture turned 30 degrees, rotWithShape {r}', rf(rotWithShape=r), rot=1800000) for r in (0, 1)]
    return out

NO_FADE = dict(stA=100000, endA=100000, endPos=100000)   # alpha 100% throughout, so only the blur shapes the edges

def fam_reflection_blur():
    """Round 3: the reflection's blur, with the fade turned off (round 2's fit failed, since the fade confounds it)."""
    return [pic(f'no fade, blurRad {b} pt', rf(blurRad=b * 12700, **NO_FADE)) for b in (0, 1, 2, 4, 6, 8, 12, 16, 24, 32)]

def fam_reflection_offset():
    """Round 3: the reflection's offset, dist along dir (every round 2 dir probe had dist 0), unblurred and unfaded."""
    out = [pic(f'dist 12 pt, dir {d // 60000} degrees', rf(blurRad=0, dist=152400, dir=d, **NO_FADE)) for d in DIRS]
    out += [pic(f'dist {t} pt, dir 90 degrees', rf(blurRad=0, dist=t * 12700, **NO_FADE)) for t in (0, 3, 6, 24)]
    return out

def fam_preset():
    return [pic(f'prstShdw shdw{i}', prstshdw(f'shdw{i}', dist=76200, dir=2700000)) for i in range(1, 21)]

def fam_combo():
    return [pic('all five', o(), inner(**INNER), glow(101600), soft(63500), rf()),
            pic('outer shadow and glow', o(), glow(101600)),
            pic('glow and soft edge', glow(101600), soft(63500)),
            pic('inner shadow and soft edge', inner(**INNER), soft(63500)),
            pic('outer shadow and reflection', o(), rf()),
            pic('outer and inner shadow', o(), inner(**INNER))]

def fam_frames():
    each = [('outer shadow', o()), ('glow', glow(101600)), ('inner shadow', inner(**INNER)), ('soft edge', soft(63500)),
            ('reflection', rf())]
    out = [pic(f'{g} frame, {n}', e, geom=g) for g in ('ellipse', 'roundRect') for n, e in each]
    white = '<a:ln w="88900" cap="sq"><a:solidFill><a:srgbClr val="FFFFFF"/></a:solidFill><a:miter lim="800000"/></a:ln>'
    black = '<a:ln w="76200"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>'
    out += [pic('white line 7pt, outer shadow', o(), ln=white), pic('black line 6pt, glow', glow(101600), ln=black)]
    return out

def fam_alpha():
    each = [('outer shadow', o()), ('inner shadow', inner(**INNER)), ('glow', glow(101600)), ('soft edge', soft(63500)),
            ('reflection', rf())]
    out = [pic(f'{p}, {n}', e, picture=p) for p in ('lineart.png', 'lineart-alpha.png') for n, e in each]
    out += [pic('photo cropped 25% each side, outer shadow', o(), src_rect='<a:srcRect l="25000" r="25000"/>')]
    return out

def fam_shapes():
    out = [shp(f'{g}, outer shadow', o(), geom=g) for g in ('rect', 'ellipse', 'star5', 'rightArrow')]
    out += [shp(f'ellipse, {n}', e, geom='ellipse') for n, e in (
        ('glow', glow(101600)), ('inner shadow', inner(**INNER)), ('soft edge', soft(63500)), ('reflection', rf()))]
    line = '<a:ln w="38100"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>'
    out += [shp('no fill, line 3pt, outer shadow', o(), fill='<a:noFill/>', ln=line),
            shp('no fill, no line, text, outer shadow', o(), fill='<a:noFill/>', ln='<a:ln><a:noFill/></a:ln>', text='SHADOW TEXT'),
            shp('filled, text, outer shadow', o(), text='SHADOW TEXT')]
    return out

def style(effect_idx):
    return ('<wps:style><a:lnRef idx="2"><a:schemeClr val="accent1"><a:shade val="50000"/></a:schemeClr></a:lnRef>'
            '<a:fillRef idx="1"><a:schemeClr val="accent1"/></a:fillRef>'
            f'<a:effectRef idx="{effect_idx}"><a:schemeClr val="accent2"/></a:effectRef>'
            '<a:fontRef idx="minor"><a:schemeClr val="lt1"/></a:fontRef></wps:style>')

def fam_effectref():
    out = [Probe(f'effectRef idx {i}, no effectLst', shape=True, fill='', style=style(i)) for i in range(4)]
    out += [Probe('effectRef idx 3, an empty effectLst stated', '<a:effectLst/>', shape=True, fill='', style=style(3))]
    return out

def fam_effectref_custom():
    return [Probe(f'effectRef idx {i}, no effectLst, theme with a glow at 2 and a reflection at 3', shape=True, fill='',
                  style=style(i)) for i in (1, 2, 3)]

def theme_custom():
    """The 2023 theme with effect style 2 a glow in phClr (the effectRef's accent2) and 3 a reflection."""
    t = open(os.path.join(THEMES, 'theme-2023.xml'), encoding='utf-8').read()
    lst = ('<a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle>'
           '<a:effectStyle><a:effectLst><a:glow rad="139700"><a:schemeClr val="phClr"><a:alpha val="60000"/></a:schemeClr>'
           '</a:glow></a:effectLst></a:effectStyle>'
           f'<a:effectStyle><a:effectLst>{refl(**REFL)}</a:effectLst></a:effectStyle></a:effectStyleLst>')
    t2 = re.sub(r'<a:effectStyleLst>.*?</a:effectStyleLst>', lst, t, flags=re.S)
    assert t2 != t
    return t2

WRAP_WORDS = ('The wrap probe runs these words on both sides of the picture, so that the gap Word leaves between them '
              'and the picture, its shadow, its glow or its reflection can be measured on the page. ') * 6

def est(dx=0, dy=0, blur=0, grow=0, below=0):
    """An estimate of the effect extent (l, t, r, b): a shadow's offset and blur, a glow's radius, a reflection's height."""
    return (max(0, blur + grow - dx), max(0, blur + grow - dy), max(0, dx + blur + grow), max(0, dy + blur + grow) + below)

def fam_anchor():
    right = outer(srgb('000000'), blurRad=50800, dist=152400, dir=0, algn='ctr', rotWithShape=0)
    left = outer(srgb('000000'), blurRad=50800, dist=152400, dir=10800000, algn='ctr', rotWithShape=0)
    gl = glow(228600)
    rfl = refl(**{**REFL, 'endPos': 90000})
    e_right, e_left = est(dx=152400, blur=50800), est(dx=-152400, blur=50800)
    e_glow, e_refl = est(grow=228600), est(blur=6350, below=CY * 90 // 100)
    out = [Probe('square wrap, no effect (the control)', anchor='square')]
    out += [Probe(f'square wrap, shadow right, effectExtent {x}', effects(right), anchor='square', extent=x, extent_est=e_right)
            for x in ('omitted', 'zero', 'estimate')]
    out += [Probe(f'square wrap, shadow left, effectExtent {x}', effects(left), anchor='square', extent=x, extent_est=e_left)
            for x in ('zero', 'estimate')]
    out += [Probe(f'square wrap, glow 18pt, effectExtent {x}', effects(gl), anchor='square', extent=x, extent_est=e_glow)
            for x in ('zero', 'estimate')]
    out += [Probe(f'{w} wrap, reflection to 90%, effectExtent {x}', effects(rfl), anchor=w, extent=x, extent_est=e_refl)
            for w in ('square', 'topAndBottom') for x in ('zero', 'estimate')]
    return out

def fam_inline():
    sh = outer(srgb('000000'), blurRad=50800, dist=152400, dir=2700000, algn='tl', rotWithShape=0)
    e_sh = est(dx=107763, dy=107763, blur=50800)
    rfl = refl(**{**REFL, 'endPos': 90000})
    out = [Probe('in a line of words, no effect (the control)', in_line=True)]
    out += [Probe(f'in a line of words, shadow, effectExtent {x}', effects(sh), in_line=True, extent=x, extent_est=e_sh)
            for x in ('omitted', 'zero', 'estimate')]
    out += [Probe(f'in a line of words, reflection to 90%, effectExtent {x}', effects(rfl), in_line=True, extent=x,
                  extent_est=est(blur=6350, below=CY * 90 // 100)) for x in ('zero', 'estimate')]
    return out

# 3-D

FRONT = '<a:camera prst="orthographicFront"/>'
THREE_PT = '<a:lightRig rig="threePt" dir="t"/>'

def scene(camera=FRONT, light=THREE_PT):
    return f'<a:scene3d>{camera}{light}</a:scene3d>'

def sp3d(inner_xml='', **a):
    return f'<a:sp3d{_attrs(a)}>{inner_xml}</a:sp3d>'

def bevel(p='circle', w=76200, h=76200, top=True):
    return f'<a:bevel{"T" if top else "B"} w="{w}" h="{h}" prst="{p}"/>'

BEVELS = ['relaxedInset', 'circle', 'slope', 'cross', 'angle', 'softRound', 'convex', 'coolSlant', 'divot', 'riblet',
          'hardEdge', 'artDeco']
RIGS = ['threePt', 'balanced', 'soft', 'harsh', 'flood', 'contrasting', 'morning', 'sunrise', 'sunset', 'chilly',
        'freezing', 'flat', 'twoPt', 'glow', 'brightRoom']
MATERIALS = ['legacyMatte', 'legacyPlastic', 'legacyMetal', 'legacyWireframe', 'matte', 'plastic', 'metal', 'warmMatte',
             'translucentPowder', 'powder', 'dkEdge', 'softEdge', 'clear', 'flat', 'softmetal']
CAMERAS = ['perspectiveFront', 'perspectiveRelaxedModerately', 'isometricOffAxis1Right', 'obliqueTopLeft',
           'perspectiveContrastingRightFacing', 'isometricTopUp']

def fam_3d_pictures():
    out = [Probe(f'bevel {b}', scene=scene() + sp3d(bevel(b))) for b in BEVELS]
    out += [Probe(f'camera {c}', scene=scene(f'<a:camera prst="{c}"/>') + sp3d()) for c in CAMERAS]
    out += [Probe(f'camera turned: lat {lat} lon {lon}',
                  scene=scene(f'<a:camera prst="orthographicFront"><a:rot lat="{lat}" lon="{lon}" rev="0"/></a:camera>') + sp3d())
            for lat, lon in ((0, 1200000), (1200000, 0))]
    return out

def fam_3d_light():
    out = [Probe(f'light rig {r}, from t', shape=True, scene=scene(light=f'<a:lightRig rig="{r}" dir="t"/>') + sp3d(bevel()))
           for r in RIGS]
    out += [Probe(f'light rig threePt, from {d}', shape=True,
                  scene=scene(light=f'<a:lightRig rig="threePt" dir="{d}"/>') + sp3d(bevel()))
            for d in ('tl', 'tr', 'l', 'r', 'bl', 'b', 'br')]
    return out

def fam_3d_material():
    cam = '<a:camera prst="perspectiveRelaxedModerately"/>'
    return [Probe(f'material {m}', shape=True, scene=scene(cam) + sp3d(bevel(), prstMaterial=m)) for m in MATERIALS]

def fam_3d_depth():
    cam = '<a:camera prst="isometricOffAxis1Right"/>'
    ex = '<a:extrusionClr><a:srgbClr val="1F3864"/></a:extrusionClr>'
    co = '<a:contourClr><a:srgbClr val="C00000"/></a:contourClr>'
    out = [Probe(f'depth {h}', shape=True, scene=scene(cam) + sp3d(ex, extrusionH=h)) for h in (0, 76200, 254000)]
    out += [Probe('depth 76200 and contour 38100', shape=True, scene=scene(cam) + sp3d(ex + co, extrusionH=76200, contourW=38100)),
            Probe('top and bottom bevels, depth 76200', shape=True,
                  scene=scene(cam) + sp3d(bevel() + bevel('angle', top=False) + ex, extrusionH=76200)),
            Probe('z 152400, depth 76200', shape=True, scene=scene(cam) + sp3d(ex, z=152400, extrusionH=76200)),
            Probe('picture, depth 76200', scene=scene(cam) + sp3d(ex, extrusionH=76200))]
    return out

# Phase 5a's round: the 3-D geometry

FIXED = (2743200, 2743200)          # the fixed anchor's place on the page: 216 pt, 216 pt from its top left
ALL_CAMERAS = ['legacyObliqueTopLeft', 'legacyObliqueTop', 'legacyObliqueTopRight', 'legacyObliqueLeft',
               'legacyObliqueFront', 'legacyObliqueRight', 'legacyObliqueBottomLeft', 'legacyObliqueBottom',
               'legacyObliqueBottomRight', 'legacyPerspectiveTopLeft', 'legacyPerspectiveTop', 'legacyPerspectiveTopRight',
               'legacyPerspectiveLeft', 'legacyPerspectiveFront', 'legacyPerspectiveRight', 'legacyPerspectiveBottomLeft',
               'legacyPerspectiveBottom', 'legacyPerspectiveBottomRight', 'orthographicFront', 'isometricTopUp',
               'isometricTopDown', 'isometricBottomUp', 'isometricBottomDown', 'isometricLeftUp', 'isometricLeftDown',
               'isometricRightUp', 'isometricRightDown', 'isometricOffAxis1Left', 'isometricOffAxis1Right',
               'isometricOffAxis1Top', 'isometricOffAxis2Left', 'isometricOffAxis2Right', 'isometricOffAxis2Top',
               'isometricOffAxis3Left', 'isometricOffAxis3Right', 'isometricOffAxis3Bottom', 'isometricOffAxis4Left',
               'isometricOffAxis4Right', 'isometricOffAxis4Bottom', 'obliqueTopLeft', 'obliqueTop', 'obliqueTopRight',
               'obliqueLeft', 'obliqueRight', 'obliqueBottomLeft', 'obliqueBottom', 'obliqueBottomRight',
               'perspectiveFront', 'perspectiveLeft', 'perspectiveRight', 'perspectiveAbove', 'perspectiveBelow',
               'perspectiveAboveLeftFacing', 'perspectiveAboveRightFacing', 'perspectiveContrastingLeftFacing',
               'perspectiveContrastingRightFacing', 'perspectiveHeroicLeftFacing', 'perspectiveHeroicRightFacing',
               'perspectiveHeroicExtremeLeftFacing', 'perspectiveHeroicExtremeRightFacing', 'perspectiveRelaxed',
               'perspectiveRelaxedModerately']          # ST_PresetCameraType's 62, in the schema's order
DEG = 60000
TALL = (914400, 1828800)            # the second size: 72 by 144 pt

def cam(prst, lat=None, lon=None, rev=None, **a):
    rot = f'<a:rot lat="{(lat or 0) * DEG}" lon="{(lon or 0) * DEG}" rev="{(rev or 0) * DEG}"/>' if (lat, lon, rev) != (None, None, None) else ''
    return f'<a:camera prst="{prst}"{_attrs(a)}>{rot}</a:camera>' if rot else f'<a:camera prst="{prst}"{_attrs(a)}/>'

def fixed(note, camera, sp='', size=(CX, CY), **kw):
    """A shape (or with picture=, a picture) at the fixed place, its scene that camera and its sp3d that."""
    shape = 'picture' not in kw
    return Probe(note, shape=shape, scene=scene(camera) + (sp or sp3d()), anchor='fixed', cx=size[0], cy=size[1], **kw)

def fam_cameras(part, size):
    """A third of the cameras (part 0, 1, 2) on a shape of that size."""
    cams = ALL_CAMERAS[part * 21:(part + 1) * 21]
    return lambda: [fixed(f'camera {c}', cam(c), size=size) for c in cams]

def fam_cameras_picture():
    cams = ['orthographicFront', 'perspectiveFront', 'isometricTopUp', 'obliqueTopLeft', 'perspectiveContrastingRightFacing',
            'legacyPerspectiveTopLeft', 'legacyObliqueTopLeft', 'perspectiveHeroicExtremeLeftFacing']
    return [fixed(f'camera {c}, the photo', cam(c), picture='photo.png') for c in cams]

ROTS = [(30, 0, 0), (0, 30, 0), (0, 0, 30), (30, 30, 0), (0, 30, 30), (30, 0, 30), (30, 30, 30), (0, 60, 0), (330, 0, 0),
        (0, 330, 0), (0, 0, 90), (45, 45, 0)]

def fam_rot():
    out = [fixed(f'orthographicFront, rot lat {a} lon {b} rev {c}', cam('orthographicFront', a, b, c)) for a, b, c in ROTS]
    out += [fixed(f'perspectiveFront, rot lat {a} lon {b} rev {c}', cam('perspectiveFront', a, b, c))
            for a, b, c in ((30, 0, 0), (0, 30, 0), (0, 0, 30), (30, 30, 30))]
    out += [fixed(f'perspectiveFront fov {f}, rot lon 30', cam('perspectiveFront', 0, 30, 0, fov=f * DEG)) for f in (30, 90)]
    out += [fixed(f'orthographicFront zoom {z // 1000}%', cam('orthographicFront', zoom=z)) for z in (50000, 200000)]
    out += [fixed('isometricTopUp, rot 0 0 0', cam('isometricTopUp', 0, 0, 0)),
            fixed('perspectiveRelaxedModerately, rot lon 30', cam('perspectiveRelaxedModerately', 0, 30, 0))]
    return out

EX = '<a:extrusionClr><a:srgbClr val="1F3864"/></a:extrusionClr>'
CO = '<a:contourClr><a:srgbClr val="C00000"/></a:contourClr>'

def fam_depth():
    d36 = 457200
    cams = [('orthographicFront', cam('orthographicFront')), ('obliqueTopLeft', cam('obliqueTopLeft')),
            ('obliqueBottomRight', cam('obliqueBottomRight')), ('isometricTopUp', cam('isometricTopUp')),
            ('isometricOffAxis1Right', cam('isometricOffAxis1Right')), ('perspectiveFront', cam('perspectiveFront')),
            ('perspectiveRelaxedModerately', cam('perspectiveRelaxedModerately')),
            ('perspectiveAboveLeftFacing', cam('perspectiveAboveLeftFacing')),
            ('legacyObliqueTopLeft', cam('legacyObliqueTopLeft')), ('legacyPerspectiveTopLeft', cam('legacyPerspectiveTopLeft')),
            ('orthographicFront lon 30', cam('orthographicFront', 0, 30, 0)), ('orthographicFront lat 30', cam('orthographicFront', 30, 0, 0))]
    out = [fixed(f'depth 36 pt, {n}', c, sp3d(EX, extrusionH=d36)) for n, c in cams]
    out += [fixed(f'depth 72 pt, {n}', cam(n), sp3d(EX, extrusionH=2 * d36)) for n in ('isometricOffAxis1Right', 'obliqueTopLeft')]
    out += [fixed(f'contour 6 pt, {n}', cam(n), sp3d(CO, contourW=76200)) for n in ('orthographicFront', 'isometricOffAxis1Right')]
    out += [fixed(f'z 36 pt, {n}', cam(n), sp3d(z=d36)) for n in ('perspectiveFront', 'isometricOffAxis1Right')]
    out += [fixed('z -36 pt, perspectiveFront', cam('perspectiveFront'), sp3d(z=-d36)),
            fixed('bevel 12 pt circle, orthographicFront lon 45', cam('orthographicFront', 0, 45, 0), sp3d(bevel(w=152400, h=152400))),
            fixed('top and bottom bevels 12 pt, depth 36 pt, isometricOffAxis1Right', cam('isometricOffAxis1Right'),
                  sp3d(bevel(w=152400, h=152400) + bevel('angle', 152400, 152400, top=False) + EX, extrusionH=d36))]
    return out

def fam_layout():
    """The frame in the layout: a 3-D drawing whose picture is wider (isometricTopUp) or taller
    (perspectiveContrastingRightFacing) than its frame, in a line of words and wrapped; and the flat controls."""
    wide, tall = scene(cam('isometricTopUp')) + sp3d(), scene(cam('perspectiveContrastingRightFacing')) + sp3d()
    return [Probe('in a line of words, flat (the control)', shape=True, in_line=True),
            Probe('in a line of words, isometricTopUp', shape=True, in_line=True, scene=wide),
            Probe('in a line of words, perspectiveContrastingRightFacing', shape=True, in_line=True, scene=tall),
            Probe('square wrap, flat (the control)', shape=True, anchor='square'),
            Probe('square wrap, isometricTopUp', shape=True, anchor='square', scene=wide),
            Probe('top and bottom wrap, perspectiveContrastingRightFacing', shape=True, anchor='topAndBottom', scene=tall)]

def fam_cameras_depth(part):
    """A third of the cameras on the 144 by 108 pt shape, extruded 36 pt."""
    cams = ALL_CAMERAS[part * 21:(part + 1) * 21]
    return lambda: [fixed(f'camera {c}, depth 36 pt', cam(c), sp3d(EX, extrusionH=457200)) for c in cams]

def fam_bevel_profiles():
    """Each bevel at 24 by 24 pt seen edge on (orthographicFront, lon 90): the silhouette's top and bottom trace its
    profile. Then the circle at other widths and heights, and a bottom bevel behind 36 pt of depth."""
    edge = cam('orthographicFront', 0, 90, 0)
    out = [fixed(f'bevel {b} 24 by 24 pt, edge on', edge, sp3d(bevel(b, 304800, 304800))) for b in BEVELS]
    out += [fixed(f'bevel circle {w // 12700} by {h // 12700} pt, edge on', edge, sp3d(bevel('circle', w, h)))
            for w, h in ((152400, 304800), (304800, 152400))]
    out += [fixed('bevel angle 24 by 24 pt at the bottom, depth 36 pt, edge on', edge,
                  sp3d(bevel('angle', 304800, 304800, top=False) + EX, extrusionH=457200)),
            fixed('bevel circle 24 by 24 pt, edge on from above (lat 90)', cam('orthographicFront', 90, 0, 0),
                  sp3d(bevel('circle', 304800, 304800)))]
    return out

YELLOW = '<a:solidFill><a:srgbClr val="FFE699"/></a:solidFill>'

def fam_text_3d():
    """A shape's text (AB, bold 22 pt, black) on a light shape under 3-D cameras, with and without a:flatTx"""
    cases = [('flat (the control)', '', ''),
             ('orthographicFront, bevel circle 6 pt', scene(cam('orthographicFront')) + sp3d(bevel(w=76200, h=76200)), ''),
             ('isometricTopUp', scene(cam('isometricTopUp')) + sp3d(), ''),
             ('isometricTopUp, flatTx', scene(cam('isometricTopUp')) + sp3d(), '<a:flatTx/>'),
             ('orthographicFront lon 45', scene(cam('orthographicFront', 0, 45, 0)) + sp3d(), ''),
             ('orthographicFront lon 45, flatTx', scene(cam('orthographicFront', 0, 45, 0)) + sp3d(), '<a:flatTx/>'),
             ('perspectiveContrastingRightFacing', scene(cam('perspectiveContrastingRightFacing')) + sp3d(), ''),
             ('obliqueTopLeft, depth 36 pt', scene(cam('obliqueTopLeft')) + sp3d(EX, extrusionH=457200), ''),
             ('isometricOffAxis1Right, depth 36 pt', scene(cam('isometricOffAxis1Right')) + sp3d(EX, extrusionH=457200), ''),
             ('orthographicFront lat 30 lon 30 rev 30', scene(cam('orthographicFront', 30, 30, 30)) + sp3d(), '')]
    return [Probe(f'text, {n}', shape=True, fill=YELLOW, text='AB', scene=sc, body=b, anchor='fixed') for n, sc, b in cases]

def sharp(dist=228600, dir=2700000, algn='tl', sx=100000, sy=100000, blur=0):
    return effects(outer(srgb('000000', 50000), blurRad=blur, dist=dist, dir=dir, algn=algn, sx=sx, sy=sy, rotWithShape=0))

def fam_shadow_3d():
    """An unblurred black shadow at 50% (18 pt at 45 degrees) on 3-D drawings; scaled 50% about tl, ctr and br"""
    iso = scene(cam('isometricTopUp')) + sp3d()
    cases = [('flat, shadow 18 pt at 45 (the control)', sharp(), ''),
             ('orthographicFront, bevel circle 6 pt', sharp(), scene(cam('orthographicFront')) + sp3d(bevel(w=76200, h=76200))),
             ('isometricTopUp', sharp(), iso),
             ('isometricTopUp, shadow 50% about ctr', sharp(dist=0, algn='ctr', sx=50000, sy=50000), iso),
             ('isometricTopUp, shadow 50% about tl', sharp(dist=0, algn='tl', sx=50000, sy=50000), iso),
             ('isometricTopUp, shadow 50% about br', sharp(dist=0, algn='br', sx=50000, sy=50000), iso),
             ('flat, shadow 50% about br (the control)', sharp(dist=0, algn='br', sx=50000, sy=50000), ''),
             ('perspectiveContrastingRightFacing', sharp(), scene(cam('perspectiveContrastingRightFacing')) + sp3d()),
             ('isometricOffAxis1Right, depth 36 pt', sharp(), scene(cam('isometricOffAxis1Right')) + sp3d(EX, extrusionH=457200)),
             ('obliqueTopLeft, depth 36 pt', sharp(), scene(cam('obliqueTopLeft')) + sp3d(EX, extrusionH=457200)),
             ('orthographicFront lon 45', sharp(), scene(cam('orthographicFront', 0, 45, 0)) + sp3d()),
             ('isometricTopUp, shadow blurred 6 pt', sharp(blur=76200), iso),
             ('isometricTopUp, glow 6 pt', effects(glow(76200, srgb('C00000', 60000))), iso),
             ('isometricTopUp, reflection', effects(refl(**{**REFL, 'endPos': 90000})), iso)]
    out = [Probe(f'shadow, {n}', fx, shape=True, fill=YELLOW, scene=sc, anchor='fixed') for n, fx, sc in cases]
    out.append(Probe('shadow, isometricTopUp, the photo', sharp(), scene=iso, anchor='fixed'))
    return out

def fam_preset_shapes():
    return [shp(f'prstShdw shdw{i} on a shape', prstshdw(f'shdw{i}', dist=76200, dir=2700000)) for i in range(1, 21)]

# ------------------------------------------------------------- after phase 4: what its findings §3 left unmeasured

PRESET_SET = (1, 3, 7, 9, 10, 13, 17, 19, 20)     # one of each kind of preset: moved, leaning, mirrored, scaled, doubled
P6 = dict(dist=76200, dir=2700000)                 # phase 0's: 6 pt at 45 degrees

def fam_preset_move():
    """The presets at two other distances and directions (phase 0 had only 6 pt at 45 degrees), and with none."""
    out = [pic(f'prstShdw shdw{i}, dist 12 pt, dir 135 degrees', prstshdw(f'shdw{i}', dist=152400, dir=8100000)) for i in PRESET_SET]
    out += [pic(f'prstShdw shdw{i}, dist 3 pt, dir 270 degrees', prstshdw(f'shdw{i}', dist=38100, dir=16200000)) for i in PRESET_SET]
    out += [pic('prstShdw shdw3, dist 6 pt, dir 0', prstshdw('shdw3', dist=76200, dir=0)),
            pic('prstShdw shdw1, no dist', prstshdw('shdw1'))]
    return out

def fam_preset_colour():
    """The second fill of shdw13 and shdw17 under colours other than black (phase 0: 0.4 grey under black)."""
    out = []
    for i in (13, 17):
        out += [pic(f'prstShdw shdw{i}, red', prstshdw(f'shdw{i}', srgb('FF0000'), **P6)),
                pic(f'prstShdw shdw{i}, blue at 50%', prstshdw(f'shdw{i}', srgb('0000FF', 50000), **P6)),
                pic(f'prstShdw shdw{i}, accent1', prstshdw(f'shdw{i}', scheme('accent1'), **P6))]
    out += [pic('prstShdw shdw1, red at 50%', prstshdw('shdw1', srgb('FF0000', 50000), **P6))]
    return out

def fam_preset_outline():
    """The presets on outlines other than a rectangle's box (phase 0's were all rectangles), and on a transparent picture."""
    line = '<a:ln w="38100"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>'
    out = []
    for i in (1, 3):
        e = prstshdw(f'shdw{i}', **P6)
        out += [pic(f'prstShdw shdw{i}, ellipse frame', e, geom='ellipse'),
                pic(f'prstShdw shdw{i}, lineart-alpha.png', e, picture='lineart-alpha.png'),
                shp(f'prstShdw shdw{i}, star5 shape', e, geom='star5'),
                shp(f'prstShdw shdw{i}, ellipse shape', e, geom='ellipse'),
                shp(f'prstShdw shdw{i}, no fill, line 3pt', e, fill='<a:noFill/>', ln=line)]
    return out

def fam_preset_combo():
    """A preset shadow with other effects, and with a picture's line."""
    e = prstshdw('shdw1', **P6)
    black = '<a:ln w="76200"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>'
    return [pic('prstShdw shdw1 and reflection', e, rf()),
            pic('prstShdw shdw1 and glow', e, glow(101600)),
            pic('prstShdw shdw1 and outer shadow', e, o()),
            pic('prstShdw shdw3 and soft edge', prstshdw('shdw3', **P6), soft(63500)),
            pic('prstShdw shdw1 and inner shadow', e, inner(**INNER)),
            pic('prstShdw shdw1, black line 6pt', e, ln=black)]

def fam_preset_size():
    """The presets on a tall and a wide picture (phase 0's were all 144 by 108 pt): do the lean and the scales follow the box?"""
    tall, wide = dict(cx=914400, cy=1828800), dict(cx=3657600, cy=914400)
    out = [pic(f'prstShdw shdw{i}, 72 by 144 pt', prstshdw(f'shdw{i}', **P6), **tall) for i in (3, 7, 9, 10)]
    out += [pic(f'prstShdw shdw{i}, 288 by 72 pt', prstshdw(f'shdw{i}', **P6), **wide) for i in (3, 10)]
    return out

STYLES_TSV = os.path.join(HERE, 'styles-word.tsv')
HAND = ('fs00-hand', os.path.join(HERE, 'cr081-styles-hand.docx'))

def styles_word():
    """The 28 picture styles as Word wrote them in Jason's document: (id, name, effect extent, spPr after its xfrm)."""
    rows = list(csv.DictReader(open(STYLES_TSV, encoding='utf-8'), delimiter='\t'))
    return [(r['id'], r['name'], tuple(int(r[k]) for k in 'ltrb'), r['sppr']) for r in rows]

def fam_styles(picture):
    def f():
        return [Probe(name, picture=picture, extent='word', extent_est=ee, raw=sppr) for _, name, ee, sppr in styles_word()]
    f.__name__ = 'fam_styles_' + picture
    return f

def generous(fam):
    """Round 2: the family again with a generous effect extent. Round 1 showed that Word clips an inline drawing's
    effects to its paragraph's height (the page's width), and that an effect extent grows the paragraph."""
    def f():
        out = fam()
        for p in out:
            p.extent = 'generous'
        return out
    f.__name__ = fam.__name__
    return f

THEME_2023, THEME_2013 = 'theme-2023.xml', 'theme-2013.xml'

SETS = {
    'cr081-phase0': [
        ('fx10-outer-offset', fam_outer_offset, THEME_2023),
        ('fx11-outer-blur', fam_outer_blur, THEME_2023),
        ('fx12-outer-scale', fam_outer_scale, THEME_2023),
        ('fx13-outer-colour', fam_outer_colour, THEME_2023),
        ('fx20-inner', fam_inner, THEME_2023),
        ('fx30-glow', fam_glow, THEME_2023),
        ('fx40-softedge', fam_softedge, THEME_2023),
        ('fx50-reflection', fam_reflection, THEME_2023),
        ('fx60-preset', fam_preset, THEME_2023),
        ('fx70-combo', fam_combo, THEME_2023),
        ('fx71-frames', fam_frames, THEME_2023),
        ('fx72-alpha', fam_alpha, THEME_2023),
        ('fx80-shapes', fam_shapes, THEME_2023),
        ('fx81-effectref-2013', fam_effectref, THEME_2013),
        ('fx82-effectref-2023', fam_effectref, THEME_2023),
        ('fx83-effectref-custom', fam_effectref_custom, 'custom'),
        ('fx90-anchor-wrap', fam_anchor, THEME_2023),
        ('fx91-inline-extent', fam_inline, THEME_2023),
    ],
    'cr081-phase0-3d': [
        ('fxa0-3d-pictures', fam_3d_pictures, THEME_2023),
        ('fxa1-3d-light', fam_3d_light, THEME_2023),
        ('fxa2-3d-material', fam_3d_material, THEME_2023),
        ('fxa3-3d-depth', fam_3d_depth, THEME_2023),
    ],
    # Round 2 (2026-11-09, after round 1's results): the inline families whose effects reach past the picture, again,
    # each with a generous effect extent, so that none is clipped; and the preset shadows on a shape, since Word drew
    # none of them on a picture. Probe numbers are round 1's, under the prefix fxr.
    'cr081-phase0-r2': [
        ('fxr10-outer-offset', generous(fam_outer_offset), THEME_2023),
        ('fxr11-outer-blur', generous(fam_outer_blur), THEME_2023),
        ('fxr12-outer-scale', generous(fam_outer_scale), THEME_2023),
        ('fxr13-outer-colour', generous(fam_outer_colour), THEME_2023),
        ('fxr30-glow', generous(fam_glow), THEME_2023),
        ('fxr50-reflection', generous(fam_reflection), THEME_2023),
        ('fxr60-preset', generous(fam_preset), THEME_2023),
        ('fxr61-preset-shapes', generous(fam_preset_shapes), THEME_2023),
        ('fxr70-combo', generous(fam_combo), THEME_2023),
        ('fxr71-frames', generous(fam_frames), THEME_2023),
        ('fxr72-alpha', generous(fam_alpha), THEME_2023),
        ('fxr80-shapes', generous(fam_shapes), THEME_2023),
        ('fxr81-effectref-2013', generous(fam_effectref), THEME_2013),
        ('fxr82-effectref-2023', generous(fam_effectref), THEME_2023),
        ('fxr83-effectref-custom', generous(fam_effectref_custom), 'custom'),
    ],
    # The picture styles (2026-11-10): Jason applied the 28 by hand to build_styles_start.py's document; that document
    # is staged as it is (HAND, first), and these write each style's XML over each picture.
    # Round 3 (2026-11-10): the reflection's blur and offset, to close phase 0's gate (Jason: "Small round").
    'cr081-phase0-r3': [
        ('fxr52-reflection-blur', generous(fam_reflection_blur), THEME_2023),
        ('fxr53-reflection-offset', generous(fam_reflection_offset), THEME_2023),
    ],
    # After phase 4 (2026-11-11; Jason: "commit phase 4 then build and stage the round"): phase 4 findings section 3.
    'cr081-phase4-r1': [
        ('fxp1-preset-move', generous(fam_preset_move), THEME_2023),
        ('fxp2-preset-colour', generous(fam_preset_colour), THEME_2023),
        ('fxp3-preset-outline', generous(fam_preset_outline), THEME_2023),
        ('fxp4-preset-combo', generous(fam_preset_combo), THEME_2023),
        ('fxp5-preset-size', generous(fam_preset_size), THEME_2023),
    ],
    # Phase 5a (2026-11-11; Jason: "CR-081 phase 5a: start it"): the 3-D geometry.
    'cr081-phase5a-r1': [
        ('fxc1-cameras-a', fam_cameras(0, (CX, CY)), THEME_2023),
        ('fxc2-cameras-b', fam_cameras(1, (CX, CY)), THEME_2023),
        ('fxc3-cameras-c', fam_cameras(2, (CX, CY)), THEME_2023),
        ('fxc4-cameras-tall-a', fam_cameras(0, TALL), THEME_2023),
        ('fxc5-cameras-tall-b', fam_cameras(1, TALL), THEME_2023),
        ('fxc6-cameras-tall-c', fam_cameras(2, TALL), THEME_2023),
        ('fxc7-cameras-photo', fam_cameras_picture, THEME_2023),
        ('fxc8-rot', fam_rot, THEME_2023),
        ('fxc9-depth', fam_depth, THEME_2023),
        ('fxca-layout', fam_layout, THEME_2023),
    ],
    'cr081-phase5a-r3': [
        ('fxe1-text-3d', fam_text_3d, THEME_2023),
        ('fxe2-shadow-3d', fam_shadow_3d, THEME_2023),
    ],
    'cr081-phase5a-r2': [
        ('fxd1-cameras-depth-a', fam_cameras_depth(0), THEME_2023),
        ('fxd2-cameras-depth-b', fam_cameras_depth(1), THEME_2023),
        ('fxd3-cameras-depth-c', fam_cameras_depth(2), THEME_2023),
        ('fxd4-bevel-profiles', fam_bevel_profiles, THEME_2023),
    ],
    'cr081-phase0-styles': [
        ('fs10-photo', fam_styles('photo.png'), THEME_2023),
        ('fs11-photo-jpeg', fam_styles('photo.jpeg'), THEME_2023),
        ('fs20-lineart', fam_styles('lineart.png'), THEME_2023),
        ('fs21-lineart-alpha', fam_styles('lineart-alpha.png'), THEME_2023),
    ],
}
AS_IS = {'cr081-phase0-styles': [HAND]}
TIMEOUT = {'cr081-phase0': 180, 'cr081-phase0-3d': 300, 'cr081-phase0-r2': 180, 'cr081-phase0-r3': 180,
           'cr081-phase0-styles': 300, 'cr081-phase4-r1': 180, 'cr081-phase5a-r1': 300, 'cr081-phase5a-r2': 300, 'cr081-phase5a-r3': 300}

# ---------------------------------------------------------------------------------------------------------- the XML

def run(words):
    return f'<w:r><w:t xml:space="preserve">{escape(words)}</w:t></w:r>'

def label(pid, probe, first):
    ppr = '' if first else '<w:pPr><w:pageBreakBefore/></w:pPr>'
    what = describe(probe.raw or probe.sppr + probe.scene) or 'no effect'
    extra = ' '.join(x for x in (describe(probe.src_rect), describe(probe.ln), describe(probe.style),
                                 f'geom {probe.geom}' if probe.geom != 'rect' else '',
                                 f'rot {probe.rot}' if probe.rot else '', probe.picture if not probe.shape else 'shape') if x)
    return f'<w:p>{ppr}{run(f"{pid}: {probe.note}.")}</w:p><w:p>{run(f"{what} | {extra}")}</w:p>'

GENEROUS = (762000, 762000, 762000, 2286000)   # 60 pt left, top and right, 180 pt below: room for every r2 effect

def effect_extent(probe):
    if probe.extent == 'omitted':
        return ''
    l, t, r, b = {'estimate': probe.extent_est, 'word': probe.extent_est, 'generous': GENEROUS}.get(probe.extent, (0, 0, 0, 0))
    return f'<wp:effectExtent l="{l}" t="{t}" r="{r}" b="{b}"/>'

def graphic(probe, n, rid):
    if probe.shape:
        txbx = (f'<wps:txbx><w:txbxContent><w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="44"/>'
                f'</w:rPr><w:t>{escape(probe.text)}</w:t></w:r></w:p></w:txbxContent></wps:txbx>') if probe.text else ''
        body = ('<wps:bodyPr rot="0" vert="horz" wrap="square" lIns="91440" tIns="45720" rIns="91440" bIns="45720" anchor="ctr" anchorCtr="0">'
                f'<a:noAutofit/>{probe.body}</wps:bodyPr>')
        return (f'<a:graphic><a:graphicData uri="{URI_WPS}"><wps:wsp><wps:cNvSpPr/><wps:spPr>{probe.sp_pr()}</wps:spPr>'
                f'{probe.style}{txbx}{body}</wps:wsp></a:graphicData></a:graphic>')
    return (f'<a:graphic><a:graphicData uri="{URI_PIC}"><pic:pic><pic:nvPicPr><pic:cNvPr id="{n}" name="{probe.picture}"/>'
            f'<pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="{rid}"/>{probe.src_rect}<a:stretch><a:fillRect/>'
            f'</a:stretch></pic:blipFill><pic:spPr>{probe.sp_pr()}</pic:spPr></pic:pic></a:graphicData></a:graphic>')

def drawing(probe, n, rid):
    kind = 'Shape' if probe.shape else 'Picture'
    docpr = f'<wp:docPr id="{n}" name="{kind} {n}"/>'
    frame = '' if probe.shape else '<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect="1"/></wp:cNvGraphicFramePr>'
    g = graphic(probe, n, rid)
    if probe.anchor == 'fixed':      # at FIXED on the page, in front of the text: its frame known exactly
        d = (f'<wp:anchor distT="0" distB="0" distL="0" distR="0" simplePos="0" relativeHeight="{251658240 + n}" '
             'behindDoc="0" locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>'
             f'<wp:positionH relativeFrom="page"><wp:posOffset>{FIXED[0]}</wp:posOffset></wp:positionH>'
             f'<wp:positionV relativeFrom="page"><wp:posOffset>{FIXED[1]}</wp:posOffset></wp:positionV>'
             f'<wp:extent cx="{probe.cx}" cy="{probe.cy}"/>{effect_extent(probe)}<wp:wrapNone/>{docpr}{frame}{g}</wp:anchor>')
    elif probe.anchor:
        wrap = '<wp:wrapSquare wrapText="bothSides"/>' if probe.anchor == 'square' else '<wp:wrapTopAndBottom/>'
        d = (f'<wp:anchor distT="0" distB="0" distL="114300" distR="114300" simplePos="0" relativeHeight="{251658240 + n}" '
             'behindDoc="0" locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>'
             '<wp:positionH relativeFrom="column"><wp:posOffset>1828800</wp:posOffset></wp:positionH>'
             '<wp:positionV relativeFrom="paragraph"><wp:posOffset>0</wp:posOffset></wp:positionV>'
             f'<wp:extent cx="{probe.cx}" cy="{probe.cy}"/>{effect_extent(probe)}{wrap}{docpr}{frame}{g}</wp:anchor>')
    else:
        d = (f'<wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="{probe.cx}" cy="{probe.cy}"/>{effect_extent(probe)}'
             f'{docpr}{frame}{g}</wp:inline>')
    d = f'<w:drawing>{d}</w:drawing>'
    if probe.shape:
        d = f'<mc:AlternateContent><mc:Choice Requires="wps">{d}</mc:Choice></mc:AlternateContent>'
    return f'<w:r>{d}</w:r>'

def body_of(probe, n, rid):
    if probe.anchor == 'fixed':
        return f'<w:p>{drawing(probe, n, rid)}</w:p>'
    if probe.anchor:
        return f'<w:p>{drawing(probe, n, rid)}{run(WRAP_WORDS)}</w:p><w:p>{run(WRAP_WORDS)}</w:p>'
    if probe.in_line:
        words = 'Words before the picture in the same line. ' * 3
        return (f'<w:p>{run(words)}{drawing(probe, n, rid)}{run(" Words after the picture in the same line. " * 3)}</w:p>'
                f'<w:p>{run(words * 2)}</w:p>')
    return ('<w:p><w:pPr><w:spacing w:before="720" w:after="0"/><w:ind w:left="2880"/></w:pPr>'
            f'{drawing(probe, n, rid)}</w:p>')

def document(probes, rids):
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in NS.items())
    parts = []
    for i, (pid, probe) in enumerate(probes):
        parts.append(label(pid, probe, i == 0))
        parts.append(body_of(probe, i + 1, rids.get(probe.picture)))
    return f'{DECL}<w:document {xmlns}><w:body>{"".join(parts)}{SECT}</w:body></w:document>'

# ------------------------------------------------------------------------------------------------------- the package

CT = ('<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
      '<Default Extension="xml" ContentType="application/xml"/><Default Extension="png" ContentType="image/png"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
      '<Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>'
      '<Override PartName="/word/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/></Types>')
REL = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships/'
RELS = (f'<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        f'<Relationship Id="rId1" Type="{REL}officeDocument" Target="word/document.xml"/></Relationships>')

def entry(z, name, data):
    i = zipfile.ZipInfo(name, (2026, 11, 9, 0, 0, 0))
    i.compress_type = zipfile.ZIP_DEFLATED
    z.writestr(i, data if isinstance(data, bytes) else data.encode('utf-8'))

def package(path, probes, theme):
    pics = sorted({p.picture for _, p in probes if not p.shape})
    rids = {name: f'rId{10 + i}' for i, name in enumerate(pics)}
    rels = [f'<Relationship Id="rId1" Type="{REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{REL}theme" Target="theme/theme1.xml"/>']
    rels += [f'<Relationship Id="{rids[n]}" Type="{REL}image" Target="media/{n}"/>' for n in pics]
    theme_xml = theme_custom() if theme == 'custom' else open(os.path.join(THEMES, theme), encoding='utf-8').read()
    ct = CT
    if any(n.endswith('.jpeg') for n in pics):     # only then, so that the earlier sets' packages keep their bytes
        ct = ct.replace('<Override ', '<Default Extension="jpeg" ContentType="image/jpeg"/><Override ', 1)
    with zipfile.ZipFile(path, 'w') as z:
        entry(z, '[Content_Types].xml', DECL + ct)
        entry(z, '_rels/.rels', DECL + RELS)
        entry(z, 'word/document.xml', document(probes, rids))
        entry(z, 'word/_rels/document.xml.rels',
              DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        entry(z, 'word/settings.xml', SETTINGS)
        entry(z, 'word/theme/theme1.xml', theme_xml)
        for n in pics:
            entry(z, f'word/media/{n}', PICTURES[n]())

def main(staging, only=()):
    """Every set's probes go in probes.tsv; the packages of the sets named (all, if none is) go to staging."""
    manifest = ['set\tfile\tpage\tprobe\tobject\tnote\textent\txml\n']
    for setname, files in SETS.items():
        for name, path in AS_IS.get(setname, ()):          # Jason's document: its pages are the styles, on the photo
            for page, (_, sname, _, sppr) in enumerate(styles_word(), 1):
                manifest.append(f'{setname}\t{name}\t{page}\t{name.split("-")[0]}-{page:02d}\tphoto.png\t{sname}\tword\t{sppr}\n')
        for name, fam, theme in files:
            prefix = name.split('-')[0]
            for page, p in enumerate(fam(), 1):
                obj = 'shape ' + p.geom if p.shape else p.picture + ('' if p.geom == 'rect' else ' ' + p.geom)
                if p.anchor:
                    obj += f' anchored {p.anchor}'
                xml = p.raw or p.src_rect + p.ln + p.sppr + p.scene + p.style
                manifest.append(f'{setname}\t{name}\t{page}\t{prefix}-{page:02d}\t{obj}\t{p.note}\t{p.extent}\t{xml}\n')
    open(os.path.join(HERE, 'probes.tsv'), 'w', newline='').writelines(manifest)
    for setname, files in SETS.items():
        if only and setname not in only:
            continue
        sums, order = [], []
        jobs = [(name, None, path) for name, path in AS_IS.get(setname, ())] + [(n, (fam, theme), None) for n, fam, theme in files]
        for name, gen, path in jobs:
            if gen:
                prefix = name.split('-')[0]
                probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(gen[0]())]
            for copy in 'ab':
                job = f'{name}-{copy}'
                d = os.path.join(staging, setname, job)
                os.makedirs(d, exist_ok=True)
                f = os.path.join(d, name + '.docx')
                if gen:
                    package(f, probes, gen[1])
                else:
                    shutil.copyfile(path, f)
                j = os.path.join(d, 'job.json')
                with open(j, 'w', newline='') as out:
                    json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT[setname],
                               'formats': ['docx', 'pdf']}, out)
                for x in (j, f):
                    h = hashlib.sha256(open(x, 'rb').read()).hexdigest()
                    sums.append(f'{h}  {setname}/{job}/{os.path.basename(x)}\n')
                order.append(job + '\n')
        open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
        open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
        print(setname, len(jobs), 'files,', len(order), 'jobs,',
              sum(len(f[1]()) for f in files) + 28 * len(AS_IS.get(setname, ())), 'probes')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
