# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084 phase 0: purpose-built probes for how Word draws a picture's colour adjustments in a PDF: the effect children
of a:blip (ISO/IEC 29500-1 20.1.8), and VML v:imagedata's colour attributes. Jason, 2026-11-12: "now let's do CR-084
phase 0". The route is CR-081's (word-bridge; CR-081-notes/build_phase0.py, whose package parts this script reuses).

One probe to a page, the page's first paragraph naming it; probes.tsv, written beside this script, lists every probe.
Each picture is anchored at a fixed place on its page (216 pt, 216 pt from the top left, wrapNone, in front of the
text), so that Word's PDF picture can be told from the labels. Each file is staged once (Word is deterministic, CR-081).

The pictures (all generated here, deterministic; 16 px patches, so that a patch's centre survives a JPEG or a
resampling):
  calib-grey.png    a 256-level grey ramp, 16 by 16 patches of 16 px (256 by 256 px), level = patch index
  calib-colour.png  the 6 by 6 by 6 RGB cube (levels 0, 51, ... 255), 24 pure hues (saturation 1, lightness 0.5),
                    16 tints and shades of red, green, blue and grey: 256 patches
  calib-alpha.png   16 colours by 16 alphas (0, 17, ... 255): 256 patches, RGBA
  calib-near.png    reds near pure red, for clrChange's tolerance: R = 255 - 2i, G = 2j (i, j 0..15), B = 0; the top
                    left 2 by 2 patches pure red (a target for Set Transparent Color by hand)
  photo.png, photo.jpeg, lineart-alpha.png   CR-081's
  blocks.emf        an EMF of 16 coloured rectangles (a vector picture, D5)
The calibration pictures and the EMF display at 0.75 pt a pixel (96 dpi, 192 by 192 pt), the photo and line art at 0.5
(240 by 180 pt).

The set (cr084-phase0):
  rk10  a:lum, brightness by contrast, on the grey ramp; and a few on the colours
  rk20  the colour effects on calib-colour (grayscl, biLevel, duotone, hsl, tint, clrRepl, fillOverlay, blur), and
        grayscl, duotone and biLevel on the grey ramp
  rk30  the alpha effects on calib-alpha (alphaModFix, alphaBiLevel, alphaCeiling, alphaFloor, alphaInv, alphaMod,
        alphaRepl), and alphaModFix on an opaque picture
  rk40  clrChange on calib-near and calib-colour: to transparent, to a colour, with and without useA
  rk50  two effects in both orders
  rk60  four effects (grayscl, lum, alphaModFix, duotone) on the photo (PNG and JPEG), the transparent line art, a cropped
        picture, a picture with an outer shadow, and the EMF; and a transparent picture's shadow with alphaModFix
  rk70  VML: v:imagedata's gain, blacklevel, gamma, grayscale, bilevel and chromakey, and Word's Washout
The first page of each file is its picture unadjusted (the control).

The presets set (cr084-phase0-presets, 2026-11-12): Jason applied Word's picture presets by hand
(cr084-recolour-hand.docx; build_hand_start.py harvest -> presets-word.tsv):
  rk80  Jason's document as it is (Word's PDF of Word's own presets on calib-colour)
  rk81  the 20 Recolor presets' XML, as Word wrote it, on the grey ramp
  rk82  the same on the photo as a JPEG

usage: build_recolour.py <staging dir> [set ...]   (every set if none is named; probes.tsv always lists every set)"""
import colorsys, csv, hashlib, importlib.util, io, json, os, re, shutil, struct, sys, zipfile
from xml.sax.saxutils import escape
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('cr081', os.path.join(HERE, '..', 'CR-081-notes', 'build_phase0.py'))
fx = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(fx)

EMU = 12700
X0, Y0 = fx.FIXED
PATCH, GRID = 16, 16
NS = dict(fx.NS, v='urn:schemas-microsoft-com:vml', o='urn:schemas-microsoft-com:office:office',
          w10='urn:schemas-microsoft-com:office:word')

# --------------------------------------------------------------------------------------------------------- pictures

def grey_levels():
    return [(v, v, v) for v in range(256)]

def colour_levels():
    cube = [(r * 51, g * 51, b * 51) for r in range(6) for g in range(6) for b in range(6)]
    hues = [tuple(int(round(c * 255)) for c in colorsys.hls_to_rgb(h / 24, 0.5, 1.0)) for h in range(24)]
    extra = []
    for base in ((255, 0, 0), (0, 255, 0), (0, 0, 255), (128, 128, 128)):
        for f in (0.25, 0.5, 0.75):                    # shades
            extra.append(tuple(int(round(c * f)) for c in base))
        extra.append(tuple(int(round(c + (255 - c) * 0.5)) for c in base))     # a tint
    return cube + hues + extra

ALPHA_COLOURS = [(0, 0, 0), (255, 255, 255), (255, 0, 0), (0, 255, 0), (0, 0, 255), (128, 128, 128), (255, 255, 0),
                 (0, 255, 255), (255, 0, 255), (255, 128, 0), (51, 102, 153), (204, 153, 102), (64, 64, 64),
                 (192, 192, 192), (128, 0, 0), (0, 128, 0)]

def alpha_levels():
    return [c + (a * 17,) for c in ALPHA_COLOURS for a in range(16)]

def near_levels():
    out = [(255 - 2 * i, 2 * j, 0) for i in range(16) for j in range(16)]
    for k in (1, 16, 17):                              # the top left 2 by 2 patches pure red
        out[k] = (255, 0, 0)
    return out

def patches(levels, mode='RGB'):
    img = Image.new(mode, (PATCH * GRID, PATCH * GRID))
    d = ImageDraw.Draw(img)
    for k, c in enumerate(levels):
        x, y = (k % GRID) * PATCH, (k // GRID) * PATCH
        d.rectangle([x, y, x + PATCH - 1, y + PATCH - 1], fill=c)
    return fx._png(img)

EMF_COLOURS = [(255, 0, 0), (0, 255, 0), (0, 0, 255), (255, 255, 255), (0, 0, 0), (128, 128, 128), (255, 255, 0),
               (0, 255, 255), (255, 0, 255), (255, 128, 0), (51, 102, 153), (204, 153, 102), (64, 64, 64),
               (192, 192, 192), (128, 0, 0), (0, 128, 0)]

def emf():
    """16 rectangles, 64 px each, in a 4 by 4 grid: 256 by 256 px at 96 dpi. Records: header (with extension 2),
    then for each rectangle CREATEBRUSHINDIRECT, SELECTOBJECT, RECTANGLE, DELETEOBJECT; the stock NULL_PEN selected
    first, so the rectangles have no outline; EOF."""
    recs = [struct.pack('<III', 37, 12, 0x80000008)]            # SELECTOBJECT NULL_PEN
    for k, (r, g, b) in enumerate(EMF_COLOURS):
        x, y = (k % 4) * 64, (k // 4) * 64
        recs.append(struct.pack('<IIIIII', 39, 24, 1, 0, r | g << 8 | b << 16, 0))     # CREATEBRUSHINDIRECT, solid
        recs.append(struct.pack('<III', 37, 12, 1))                                    # SELECTOBJECT
        recs.append(struct.pack('<IIiiii', 43, 24, x, y, x + 65, y + 65))              # RECTANGLE (exclusive edges)
        recs.append(struct.pack('<III', 37, 12, 0x80000000))                           # SELECTOBJECT WHITE_BRUSH
        recs.append(struct.pack('<III', 40, 12, 1))                                    # DELETEOBJECT
    recs.append(struct.pack('<IIIII', 14, 20, 0, 16, 20))                              # EOF
    body = b''.join(recs)
    size = 108 + len(body)
    mm100 = int(round(256 / 96 * 2540))                       # 256 px at 96 dpi, in hundredths of a millimetre
    header = struct.pack('<II4i4iIIIIHHIIIiiiiIIIii', 1, 108, 0, 0, 255, 255, 0, 0, mm100 - 1, mm100 - 1,
                         0x464D4520, 0x10000, size, len(recs) + 1, 2, 0, 0, 0, 0, 1920, 1080, 508, 286, 0, 0, 0,
                         508000, 286000)
    return header + body

PICTURES = {
    'calib-grey.png': lambda: patches(grey_levels()),
    'calib-colour.png': lambda: patches(colour_levels()),
    'calib-alpha.png': lambda: patches(alpha_levels(), 'RGBA'),
    'calib-near.png': lambda: patches(near_levels()),
    'photo.png': fx.photo, 'photo.jpeg': fx.photo_jpeg, 'lineart-alpha.png': lambda: fx.lineart(True),
    'blocks.emf': emf,
}
SIZE = {'photo.png': (480, 360), 'photo.jpeg': (480, 360), 'lineart-alpha.png': (480, 360)}   # pixels; others 256

def frame(picture):
    w, h = SIZE.get(picture, (256, 256))
    k = 0.5 if picture in SIZE else 0.75             # the photos at 0.5 pt a pixel (240 by 180 pt), to stay on the page
    return int(w * k * EMU), int(h * k * EMU)

# ------------------------------------------------------------------------------------------------------------ probes

def srgb(v, alpha=None):
    return f'<a:srgbClr val="{v}">' + (f'<a:alpha val="{alpha}"/>' if alpha is not None else '') + '</a:srgbClr>'

def prst(v):
    return f'<a:prstClr val="{v}"/>'

def scheme(v, *mods):
    return f'<a:schemeClr val="{v}">' + ''.join(mods) + '</a:schemeClr>'

def lum(b=0, c=0):
    return f'<a:lum bright="{b}" contrast="{c}"/>'

def duotone(c1, c2):
    return f'<a:duotone>{c1}{c2}</a:duotone>'

def clr_change(frm, to, use_a=None):
    a = '' if use_a is None else f' useA="{use_a}"'
    return f'<a:clrChange{a}><a:clrFrom>{frm}</a:clrFrom><a:clrTo>{to}</a:clrTo></a:clrChange>'

GRAYSCL = '<a:grayscl/>'

class Pic:
    """One page: a picture at (X0, Y0) in front of the text; blip is the a:blip's children, or vml the v:imagedata's
    attributes (a VML picture instead)."""
    def __init__(self, note, picture='calib-colour.png', blip='', src_rect='', effects='', vml=None):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def summary(self):
        if self.vml is not None:
            return f'VML v:imagedata {self.vml or "(no attributes)"} | {self.picture}'
        x = fx.describe(self.blip + self.src_rect + self.effects) or 'unadjusted'
        return f'{x} | {self.picture}'

def control(picture):
    return Pic('unadjusted (the control)', picture)

def fam_lum():
    out = [control('calib-grey.png')]
    steps = (-60000, -30000, 0, 30000, 60000)
    for b in steps:
        for c in steps:
            if b or c:
                out.append(Pic(f'lum bright {b // 1000}% contrast {c // 1000}%', 'calib-grey.png', lum(b, c)))
    for b, c in ((100000, 0), (-100000, 0), (0, 100000), (0, -100000), (20000, -40000), (-20000, 40000)):
        out.append(Pic(f'lum bright {b // 1000}% contrast {c // 1000}%', 'calib-grey.png', lum(b, c)))
    for b, c in ((30000, 0), (0, 30000), (-30000, -30000), (40000, 40000)):
        out.append(Pic(f'lum bright {b // 1000}% contrast {c // 1000}% on colour', 'calib-colour.png', lum(b, c)))
    return out

def fam_colour():
    c = 'calib-colour.png'
    out = [control(c), Pic('grayscl', c, GRAYSCL)]
    for t in (25000, 50000, 75000):
        out.append(Pic(f'biLevel thresh {t // 1000}%', c, f'<a:biLevel thresh="{t}"/>'))
    for c1, c2, n in ((prst('black'), srgb('FFFFFF'), 'black to white'), (prst('black'), srgb('FF0000'), 'black to red'),
                      (srgb('0000FF'), srgb('FFFF00'), 'blue to yellow'),
                      (scheme('accent1', '<a:shade val="45000"/><a:satMod val="135000"/>'), prst('white'), 'accent1 shade to white')):
        out.append(Pic(f'duotone {n}', c, duotone(c1, c2)))
    out += [Pic('hsl hue +60 degrees', c, '<a:hsl hue="3600000" sat="0" lum="0"/>'),
            Pic('hsl sat -50%', c, '<a:hsl hue="0" sat="-50000" lum="0"/>'),
            Pic('hsl lum +25%', c, '<a:hsl hue="0" sat="0" lum="25000"/>'),
            Pic('tint hue 0 amt 50%', c, '<a:tint hue="0" amt="50000"/>'),
            Pic('tint hue 240 degrees amt 50%', c, '<a:tint hue="14400000" amt="50000"/>'),
            Pic('clrRepl green', c, f'<a:clrRepl>{srgb("00FF00")}</a:clrRepl>'),
            Pic('fillOverlay mult red', c, '<a:fillOverlay blend="mult"><a:solidFill><a:srgbClr val="FF0000"/></a:solidFill></a:fillOverlay>'),
            Pic('fillOverlay screen blue', c, '<a:fillOverlay blend="screen"><a:solidFill><a:srgbClr val="0000FF"/></a:solidFill></a:fillOverlay>'),
            Pic('blur 2 pt', c, '<a:blur rad="25400" grow="1"/>')]
    g = 'calib-grey.png'
    out += [Pic('grayscl on the grey ramp', g, GRAYSCL), Pic('duotone black to red on the grey ramp', g, duotone(prst('black'), srgb('FF0000'))),
            Pic('biLevel thresh 50% on the grey ramp', g, '<a:biLevel thresh="50000"/>')]
    return out

def fam_alpha():
    a = 'calib-alpha.png'
    out = [control(a)]
    for amt in (85000, 70000, 50000, 35000, 20000, 5000, 0):
        out.append(Pic(f'alphaModFix amt {amt // 1000}%', a, f'<a:alphaModFix amt="{amt}"/>'))
    out += [Pic('alphaBiLevel thresh 50%', a, '<a:alphaBiLevel thresh="50000"/>'),
            Pic('alphaCeiling', a, '<a:alphaCeiling/>'),
            Pic('alphaFloor', a, '<a:alphaFloor/>'),
            Pic('alphaInv', a, '<a:alphaInv/>'),
            Pic('alphaMod by a container of alphaModFix 50%', a, '<a:alphaMod><a:cont><a:alphaModFix amt="50000"/></a:cont></a:alphaMod>'),
            Pic('alphaRepl a 50%', a, '<a:alphaRepl a="50000"/>'),
            Pic('alphaModFix amt 50% on an opaque picture', 'calib-colour.png', '<a:alphaModFix amt="50000"/>')]
    return out

def fam_clr_change():
    n, c = 'calib-near.png', 'calib-colour.png'
    return [control(n),
            Pic('clrChange red to transparent (Set Transparent Color)', n, clr_change(srgb('FF0000'), srgb('FF0000', 0))),
            Pic('clrChange red to blue', n, clr_change(srgb('FF0000'), srgb('0000FF'))),
            Pic('clrChange red to transparent, useA 0', n, clr_change(srgb('FF0000'), srgb('FF0000', 0), 0)),
            Pic('clrChange 336699 to transparent on the colours', c, clr_change(srgb('336699'), srgb('336699', 0))),
            Pic('clrChange white to transparent on the colours', c, clr_change(srgb('FFFFFF'), srgb('FFFFFF', 0))),
            Pic('clrChange red at alpha 0 to blue, useA 1, on the alpha picture', 'calib-alpha.png',
                clr_change(srgb('FF0000', 0), srgb('0000FF'), 1)),
            Pic('clrChange red to blue, useA 0, on the alpha picture', 'calib-alpha.png', clr_change(srgb('FF0000'), srgb('0000FF'), 0))]

def fam_orders():
    c = 'calib-colour.png'
    l = lum(30000, 30000)
    d = duotone(prst('black'), srgb('FF0000'))
    am = '<a:alphaModFix amt="50000"/>'
    return [control(c), Pic('grayscl then lum', c, GRAYSCL + l), Pic('lum then grayscl', c, l + GRAYSCL),
            Pic('duotone then lum', c, d + l), Pic('lum then duotone', c, l + d),
            Pic('alphaModFix then grayscl', c, am + GRAYSCL), Pic('grayscl then alphaModFix', c, GRAYSCL + am),
            Pic('biLevel then duotone', c, '<a:biLevel thresh="50000"/>' + d), Pic('duotone then biLevel', c, d + '<a:biLevel thresh="50000"/>')]

OUTER = ('<a:effectLst><a:outerShdw blurRad="50800" dist="76200" dir="2700000" algn="tl" rotWithShape="0">'
         '<a:srgbClr val="000000"><a:alpha val="60000"/></a:srgbClr></a:outerShdw></a:effectLst>')
FOUR = [('grayscl', GRAYSCL), ('lum bright 30% contrast 30%', lum(30000, 30000)), ('alphaModFix 50%', '<a:alphaModFix amt="50000"/>'),
        ('duotone black to red', duotone(prst('black'), srgb('FF0000')))]
CROP = '<a:srcRect l="25000" t="25000" r="25000" b="25000"/>'

def fam_pictures():
    out = []
    for pic, crop, eff, what in (('photo.png', '', '', 'the photo'), ('photo.jpeg', '', '', 'the photo as a JPEG'),
                                 ('lineart-alpha.png', '', '', 'the transparent line art'),
                                 ('calib-colour.png', CROP, '', 'the colours cropped to their middle quarter'),
                                 ('calib-colour.png', '', OUTER, 'the colours with an outer shadow'), ('blocks.emf', '', '', 'the EMF')):
        out.append(Pic(f'{what}, unadjusted', pic, src_rect=crop, effects=eff))
        for note, blip in FOUR:
            out.append(Pic(f'{what}, {note}', pic, blip, src_rect=crop, effects=eff))
    out.append(Pic('the transparent line art with an outer shadow and alphaModFix 50%', 'lineart-alpha.png',
                   '<a:alphaModFix amt="50000"/>', effects=OUTER))
    out.append(Pic('the transparent line art with an outer shadow', 'lineart-alpha.png', effects=OUTER))
    return out

def fam_vml():
    g, c = 'calib-grey.png', 'calib-colour.png'
    return [Pic('VML, unadjusted', g, vml=''), Pic('VML gain 0.5', g, vml='gain="32768f"'), Pic('VML gain 2', g, vml='gain="2"'),
            Pic('VML blacklevel +0.2', g, vml='blacklevel="13107f"'), Pic('VML blacklevel -0.2', g, vml='blacklevel="-13107f"'),
            Pic('VML gamma 2', g, vml='gamma="2"'), Pic('VML gamma 0.5', g, vml='gamma="32768f"'),
            Pic("VML Washout, as Word 2003 wrote it (gain 0.3, blacklevel 0.35)", g, vml='gain="19661f" blacklevel="22938f"'),
            Pic('VML grayscale', c, vml='grayscale="t"'), Pic('VML bilevel', c, vml='bilevel="t"'),
            Pic('VML chromakey red', 'calib-near.png', vml='chromakey="red"'), Pic('VML, unadjusted, on the colours', c, vml='')]

PRESETS_TSV = os.path.join(HERE, 'presets-word.tsv')
HAND = os.path.join(HERE, 'cr084-recolour-hand.docx')

def recolor_presets():
    rows = list(csv.DictReader(open(PRESETS_TSV, encoding='utf-8'), delimiter='\t'))
    return [(r['id'], r['task'], r['a']) for r in rows if r['task'].startswith('Recolor')]

def fam_presets(picture):
    def f():
        return [control(picture)] + [Pic(f'{task} ({pid}), as Word wrote it', picture, a) for pid, task, a in recolor_presets()]
    return f

# --------------------------------------------------------------------------------------------------------- the XML

SPT75 = ('<v:shapetype id="_x0000_t75" coordsize="21600,21600" o:spt="75" o:preferrelative="t" path="m@4@5l@4@11@9@11@9@5xe" '
         'filled="f" stroked="f"><v:stroke joinstyle="miter"/><v:formulas><v:f eqn="if lineDrawn pixelLineWidth 0"/>'
         '<v:f eqn="sum @0 1 0"/><v:f eqn="sum 0 0 @1"/><v:f eqn="prod @2 1 2"/><v:f eqn="prod @3 21600 pixelWidth"/>'
         '<v:f eqn="prod @3 21600 pixelHeight"/><v:f eqn="sum @0 0 1"/><v:f eqn="prod @6 1 2"/><v:f eqn="prod @7 21600 pixelWidth"/>'
         '<v:f eqn="sum @8 21600 0"/><v:f eqn="prod @7 21600 pixelHeight"/><v:f eqn="sum @10 21600 0"/></v:formulas>'
         '<v:path o:extrusionok="f" gradientshapeok="t" o:connecttype="rect"/><o:lock v:ext="edit" aspectratio="t"/></v:shapetype>')

def drawing(p, n, rid, typed):
    cx, cy = frame(p.picture)
    if p.vml is not None:
        st = '' if typed else SPT75
        pos = (f'position:absolute;margin-left:{X0 / EMU:g}pt;margin-top:{Y0 / EMU:g}pt;width:{cx / EMU:g}pt;height:{cy / EMU:g}pt;'
               f'z-index:{n};mso-position-horizontal-relative:page;mso-position-vertical-relative:page')
        a = f' {p.vml}' if p.vml else ''
        return (f'<w:r><w:pict>{st}<v:shape id="Picture {n}" o:spid="_x0000_s{1024 + n}" type="#_x0000_t75" style="{pos}">'
                f'<v:imagedata r:id="{rid}" o:title=""{a}/></v:shape></w:pict></w:r>'), True
    blip = f'<a:blip r:embed="{rid}">{p.blip}</a:blip>' if p.blip else f'<a:blip r:embed="{rid}"/>'
    g = (f'<a:graphic><a:graphicData uri="{fx.URI_PIC}"><pic:pic><pic:nvPicPr><pic:cNvPr id="{n}" name="{p.picture}"/>'
         f'<pic:cNvPicPr/></pic:nvPicPr><pic:blipFill>{blip}{p.src_rect}<a:stretch><a:fillRect/></a:stretch></pic:blipFill>'
         f'<pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/>'
         f'</a:prstGeom>{p.effects}</pic:spPr></pic:pic></a:graphicData></a:graphic>')
    d = (f'<wp:anchor distT="0" distB="0" distL="0" distR="0" simplePos="0" relativeHeight="{251658240 + n}" '
         'behindDoc="0" locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>'
         f'<wp:positionH relativeFrom="page"><wp:posOffset>{X0}</wp:posOffset></wp:positionH>'
         f'<wp:positionV relativeFrom="page"><wp:posOffset>{Y0}</wp:posOffset></wp:positionV>'
         f'<wp:extent cx="{cx}" cy="{cy}"/><wp:effectExtent l="0" t="0" r="0" b="0"/><wp:wrapNone/>'
         f'<wp:docPr id="{n}" name="Picture {n}"/><wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect="1"/>'
         f'</wp:cNvGraphicFramePr>{g}</wp:anchor>')
    return f'<w:r><w:drawing>{d}</w:drawing></w:r>', typed

def document(probes, rids):
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in NS.items())
    parts, typed = [], False
    for i, (pid, p) in enumerate(probes):
        ppr = '' if i == 0 else '<w:pPr><w:pageBreakBefore/></w:pPr>'
        run, typed = drawing(p, i + 1, rids[p.picture], typed)
        parts.append(f'<w:p>{ppr}{fx.run(f"{pid}: {p.note}.")}</w:p><w:p>{run}{fx.run(p.summary())}</w:p>')
    return f'{fx.DECL}<w:document {xmlns}><w:body>{"".join(parts)}{fx.SECT}</w:body></w:document>'

def package(path, probes):
    pics = sorted({p.picture for _, p in probes})
    rids = {name: f'rId{10 + i}' for i, name in enumerate(pics)}
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>']
    rels += [f'<Relationship Id="{rids[n]}" Type="{fx.REL}image" Target="media/{n}"/>' for n in pics]
    ct = fx.CT
    for ext, mime in (('jpeg', 'image/jpeg'), ('emf', 'image/x-emf')):
        if any(n.endswith('.' + ext) for n in pics):
            ct = ct.replace('<Override ', f'<Default Extension="{ext}" ContentType="{mime}"/><Override ', 1)
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + ct)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', document(probes, rids))
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
        for n in pics:
            fx.entry(z, f'word/media/{n}', PICTURES[n]())

SETS = {
    'cr084-phase0': [
        ('rk10-lum', fam_lum),
        ('rk20-colour', fam_colour),
        ('rk30-alpha', fam_alpha),
        ('rk40-clrchange', fam_clr_change),
        ('rk50-orders', fam_orders),
        ('rk60-pictures', fam_pictures),
        ('rk70-vml', fam_vml),
    ],
}
SETS['cr084-phase0-presets'] = [
    ('rk81-presets-grey', fam_presets('calib-grey.png')),
    ('rk82-presets-photo', fam_presets('photo.jpeg')),
]
TIMEOUT = {'cr084-phase0': 300, 'cr084-phase0-presets': 300}
AS_IS = {'cr084-phase0-presets': [('rk80-hand', HAND)]}

def main(staging, only=()):
    manifest = ['set\tfile\tpage\tprobe\tpicture\tnote\tsummary\n']
    for setname, jobs in AS_IS.items():
        for name, _ in jobs:
            manifest.append(f'{setname}\t{name}\t-\trk80\tas is\tJason\'s presets, made by hand in Word\tbuild_hand_start.py\'s 43 pages\n')
    for setname, files in SETS.items():
        for name, fam in files:
            prefix = name.split('-')[0]
            for page, p in enumerate(fam(), 1):
                manifest.append(f'{setname}\t{name}\t{page}\t{prefix}-{page:02d}\t{p.picture}\t{p.note}\t{p.summary()}\n')
    open(os.path.join(HERE, 'probes.tsv'), 'w', newline='').writelines(manifest)
    for setname, files in SETS.items():
        if only and setname not in only:
            continue
        sums, order, count = [], [], 0
        for name, src in AS_IS.get(setname, ()):
            d = os.path.join(staging, setname, name)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            shutil.copyfile(src, f)
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as out:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT[setname],
                           'formats': ['docx', 'pdf']}, out)
            for x in (j, f):
                sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {setname}/{name}/{os.path.basename(x)}\n')
            order.append(name + '\n')
        for name, fam in files:
            prefix = name.split('-')[0]
            probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(fam())]
            count += len(probes)
            d = os.path.join(staging, setname, name)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            package(f, probes)
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as out:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT[setname],
                           'formats': ['docx', 'pdf']}, out)
            for x in (j, f):
                sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {setname}/{name}/{os.path.basename(x)}\n')
            order.append(name + '\n')
        open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
        open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
        print(setname, len(order), 'files,', len(order), 'jobs,', count, 'probes')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
