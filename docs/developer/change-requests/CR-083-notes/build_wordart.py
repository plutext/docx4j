# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 0: purpose-built probes for how Word draws WordArt in a PDF: the 40 text warps (a:prstTxWarp), the
w14 run effects on WordArt and on body text, 3-D text, and VML WordArt (v:textpath). Jason, 2026-11-12: "do CR-083
phase 0". The route is CR-081's and CR-080's (word-bridge; CR-081-notes/build_phase0.py, whose package parts this
script reuses).

One probe to a page, the page's first paragraph naming it; probes.tsv, written beside this script, lists every probe
with its file and page. Each WordArt is a wps text box anchored at a fixed place on its page (144 pt from the left,
252 pt from the top, wrapNone), 288 pt by 144 pt unless the probe says otherwise, so that its frame in the PDF is known
exactly. Each file is staged once: Word was deterministic over CR-081's 696 measured draws, each run twice.

The text is Aptos (the theme-2023 face; the VM's own Aptos files are in the CloudFonts snapshot on the share, so that a
warped outline can be compared with the exact font), 36 pt, black, centred, unless the probe says otherwise. One line
is "WordArt"; two lines are "WordArt" and "Two lines"; three add "Three". The labels are in Word's default face
(no styles part), so that they are told apart from the probes' text.

Word's own form for WordArt is Jason's document (cr083-wordart-jason.docx, made by hand in Word 2026-11-11, staged
as it is as wa00): a wps text box (txBox="1"), rect, no fill, no line, its bodyPr wrap="none", fromWordArt="0",
default insets, a:spAutoFit, and a:prstTxWarp; stored at 144 by 144 pt whatever its text, a Transform (Follow Path)
included. Word's form is wa10 and wa11; the other families give the box a fixed size (a:noAutofit), so that the warp is
evaluated on a known box. Every generated WordArt has a 0.25 pt grey line (BFBFBF) instead of none, so that the box
Word used shows in its PDF.

Round 1 (cr083-phase0):
  wa00        Jason's document as it is (Word's PDF of Word's own WordArt)
  wa10, wa11  the 40 warps in Word's form (spAutoFit, 144 by 144 pt), default adjustments, one line and two lines
  wa15, wa16  the same in a fixed box (288 by 144 pt)
  wa12, wa13  the 40 warps with one other adjustment, in the fixed box, one line and two lines
  wa14        three lines on the multi-path presets (and two two-path ones), for the band rule
  wa20        fromWordArt, autofit, wrap, insets and the font size, on three presets
  wa21        the text's rectangle: ink or the font's height ("wave", x-height only; "Warpy", a descender; spaces)
  wa30        the w14 run effects alone and together on flat WordArt (textNoShape)
  wa31        the same effects on warped WordArt
  wa40        two runs of different size and colour, flat and warped
  wa60        3-D: Word's two forms (Bevel: bodyPr a:scene3d and a:sp3d, and w14:props3d on the runs; 3-D Rotation:
              bodyPr a:scene3d alone), and others: w14:props3d and w14:scene3d on runs, a:sp3d without w14
  wb50        the w14 run effects on body text (D3)
  wb60        3-D on body text (D10)
  wv10        the 40 warps as wps WordArt in a Word 2007 mode document (compatibilityMode 12): Word's save converts
              them, or not, to VML; its shapetypes and o:spt numbers are the oracle for D6's table
  wv20        one VML WordArt for each text shape type, o:spt 136 to 175, in Word 2007 mode, each shapetype carrying
              only its o:spt (no path or formulas), so that Word's PDF shows whether it draws by o:spt; and 136 whole,
              as Word writes a watermark, and 136 with another type's path
  wv30        text watermarks (VML 136 in a header, as Word writes them), in the current mode

Round 1b (cr083-phase0-styles, 2026-11-12): the 15 gallery styles as Word wrote them in Jason's hand-made document
(build_hand_start.py harvest -> styles-word.tsv; the WordArt and Text Effects galleries are one):
  wg00        Jason's document as it is
  wg10..wg12  the 15 styles on WordArt in Word's form: flat, textArchUp (Follow Path), textWave1 (a Warp)
  wg20        the 15 styles on body text, 36 pt alone and 14 pt within a line

usage: build_wordart.py <staging dir> [set ...]   (every set if none is named; probes.tsv always lists every set)"""
import csv, hashlib, importlib.util, json, os, re, shutil, sys, zipfile
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('cr081', os.path.join(HERE, '..', 'CR-081-notes', 'build_phase0.py'))
fx = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(fx)
A = '{http://schemas.openxmlformats.org/drawingml/2006/main}'
WARP_XML = os.path.join(fx.REPO, 'docx4j-core', 'src', 'main', 'resources', 'org', 'docx4j', 'model', 'shapes',
                        'presetTextWarpDefinitions.xml')

EMU = 12700
X0, Y0 = 144 * EMU, 252 * EMU                       # the text box's top left on the page
BOX = (288 * EMU, 144 * EMU)
WORD_BOX = (144 * EMU, 144 * EMU)                   # the size Word stores for every WordArt it inserts
HAIRLINE = '<a:ln w="3175"><a:solidFill><a:srgbClr val="BFBFBF"/></a:solidFill></a:ln>'
JASON = os.path.join(HERE, 'cr083-wordart-jason.docx')
INS = (91440, 45720, 91440, 45720)                  # Word's text box insets: 7.2 pt left and right, 3.6 pt top and bottom
FONT = 'Aptos'
ONE, TWO, THREE = ['WordArt'], ['WordArt', 'Two lines'], ['WordArt', 'Two lines', 'Three']

NS = dict(fx.NS, w14='http://schemas.microsoft.com/office/word/2010/wordml', v='urn:schemas-microsoft-com:vml',
          o='urn:schemas-microsoft-com:office:office', w10='urn:schemas-microsoft-com:office:word')

# ------------------------------------------------------------------------------------------------------------ warps

CD = {'cd2': 10800000, 'cd4': 5400000, '0': 0}

def warps():
    """(name, [(adj name, default)], [(adj name, min, max, polar)]) in the definitions' order."""
    out = []
    for el in ET.parse(WARP_XML).getroot():
        av = el.find(A + 'avLst')
        adj = []
        for g in (av if av is not None else []):
            v = g.get('fmla').split()[1]
            adj.append((g.get('name'), CD[v] if v in CD else int(v)))
        rng = []
        ah = el.find(A + 'ahLst')
        for h in (ah if ah is not None else []):
            if h.tag == A + 'ahPolar':
                rng.append((h.get('gdRefAng'), int(h.get('minAng')), int(h.get('maxAng')), True))
                if h.get('gdRefR'):
                    rng.append((h.get('gdRefR'), int(h.get('minR')), int(h.get('maxR')), False))
            else:
                ref = h.get('gdRefY') or h.get('gdRefX')
                lo, hi = (h.get('minY'), h.get('maxY')) if h.get('gdRefY') else (h.get('minX'), h.get('maxX'))
                rng.append((ref, int(lo), int(hi), False))
        out.append((el.tag, adj, rng))
    return out

def other_adj(adj, rng):
    """One other setting: a Pour preset's thickness (its second adjustment), otherwise the first adjustment; an angle
    60 degrees on, a length three quarters of the way along its handle's range (a quarter, if that is the default)."""
    lims = {name: (lo, hi, polar) for name, lo, hi, polar in rng}
    which = adj[1][0] if len(adj) == 2 and lims.get(adj[0][0], (0, 0, False))[2] else adj[0][0]
    out = []
    for name, v in adj:
        if name == which:
            lo, hi, polar = lims[name]
            if polar:
                v = (v + 3600000) % 21600000
            else:
                q = lo + 3 * (hi - lo) // 4
                v = q if abs(q - v) > (hi - lo) // 10 else lo + (hi - lo) // 4
        out.append((name, v))
    return out

# ------------------------------------------------------------------------------------------------------- w14 effects

def w14_clr(val, alpha=None, scheme=False):
    tag = 'schemeClr' if scheme else 'srgbClr'
    a = f'<w14:alpha w14:val="{alpha}"/>' if alpha is not None else ''
    return f'<w14:{tag} w14:val="{val}">{a}</w14:{tag}>' if a else f'<w14:{tag} w14:val="{val}"/>'

def solid(val, alpha=None):
    return f'<w14:solidFill>{w14_clr(val, alpha)}</w14:solidFill>'

def grad(stops, lin=5400000, path=None):
    gs = ''.join(f'<w14:gs w14:pos="{p}">{w14_clr(c)}</w14:gs>' for p, c in stops)
    shade = (f'<w14:path w14:path="{path}"><w14:fillToRect w14:l="50000" w14:t="50000" w14:r="50000" w14:b="50000"/></w14:path>'
             if path else f'<w14:lin w14:ang="{lin}" w14:scaled="0"/>')
    return f'<w14:gradFill><w14:gsLst>{gs}</w14:gsLst>{shade}</w14:gradFill>'

def w14a(d):
    return ''.join(f' w14:{k}="{v}"' for k, v in d.items())

def glow(rad=63500, val='ED7D31', alpha=60000):
    return f'<w14:glow w14:rad="{rad}">{w14_clr(val, alpha)}</w14:glow>'

SHADOW = dict(blurRad=50800, dist=38100, dir=2700000, sx=100000, sy=100000, kx=0, ky=0, algn='tl')

def shadow(val='000000', alpha=60000, **over):
    return f'<w14:shadow{w14a({**SHADOW, **over})}>{w14_clr(val, alpha)}</w14:shadow>'

REFLECTION = dict(blurRad=6350, stA=55000, stPos=0, endA=300, endPos=45500, dist=0, dir=5400000, fadeDir=5400000,
                  sx=100000, sy=-100000, kx=0, ky=0, algn='bl')

def reflection(**over):
    return f'<w14:reflection{w14a({**REFLECTION, **over})}/>'

def outline(w=12700, fill=None, dash='solid', join='round', cap='flat', cmpd='sng', algn='ctr'):
    fill = solid('000000') if fill is None else fill
    j = '<w14:miter w14:lim="800000"/>' if join == 'miter' else f'<w14:{join}/>'
    return (f'<w14:textOutline w14:w="{w}" w14:cap="{cap}" w14:cmpd="{cmpd}" w14:algn="{algn}">{fill}'
            f'<w14:prstDash w14:val="{dash}"/>{j}</w14:textOutline>')

def text_fill(inner):
    return f'<w14:textFill>{inner}</w14:textFill>'

NOFILL = '<w14:noFill/>'

def scene3d(camera='orthographicFront', rig='threePt', d='t'):
    return (f'<w14:scene3d><w14:camera w14:prst="{camera}"/><w14:lightRig w14:rig="{rig}" w14:dir="{d}">'
            '<w14:rot w14:lat="0" w14:lon="0" w14:rev="0"/></w14:lightRig></w14:scene3d>')

def props3d(bevel=None, extrusion=0, material='warmMatte', extrusion_clr=None):
    b = f'<w14:bevelT w14:w="{bevel[1]}" w14:h="{bevel[2]}" w14:prst="{bevel[0]}"/>' if bevel else ''
    e = f'<w14:extrusionClr>{w14_clr(extrusion_clr)}</w14:extrusionClr>' if extrusion_clr else ''
    h = f' w14:extrusionH="{extrusion}"' if extrusion else ''
    return f'<w14:props3d{h} w14:prstMaterial="{material}">{b}{e}</w14:props3d>'

ORDER = ['glow', 'shadow', 'reflection', 'textOutline', 'textFill', 'scene3d', 'props3d']   # EG_RPrTextEffects

def effects(*parts):
    return ''.join(sorted(parts, key=lambda x: ORDER.index(re.match(r'<w14:(\w+)', x).group(1))))

# ------------------------------------------------------------------------------------------------------------ probes

class Run:
    """A run; raw, if given, is its w:rPr's content as Word wrote it (a gallery style), used as it is."""
    def __init__(self, text, size=36, color='000000', fx='', font=FONT, bold=False, raw=None):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def xml(self):
        if self.raw is not None:
            return f'<w:r><w:rPr>{self.raw}</w:rPr><w:t xml:space="preserve">{escape(self.text)}</w:t></w:r>'
        hp = int(round(self.size * 2))
        b = '<w:b/><w:bCs/>' if self.bold else ''
        return (f'<w:r><w:rPr><w:rFonts w:ascii="{self.font}" w:hAnsi="{self.font}" w:cs="{self.font}"/>{b}'
                f'<w:color w:val="{self.color}"/><w:sz w:val="{hp}"/><w:szCs w:val="{hp}"/>{self.fx}</w:rPr>'
                f'<w:t xml:space="preserve">{escape(self.text)}</w:t></w:r>')

def para(runs, jc='center', before=0):
    return (f'<w:p><w:pPr><w:spacing w:before="{before}" w:after="0" w:line="240" w:lineRule="auto"/><w:jc w:val="{jc}"/>'
            f'</w:pPr>{"".join(r.xml() for r in runs)}</w:p>')

def lines(texts, **run):
    """Each line its own paragraph of one run, as Word writes WordArt typed with Enter."""
    return [[Run(t, **run)] for t in texts]

class Art:
    """One page: a WordArt, a wps text box anchored at (X0, Y0)."""
    kind = 'wordart'

    def __init__(self, note, paras, warp='textNoShape', adj=(), from_wordart=False, autofit='noAutofit', wrap='none',
                 ins=INS, size=BOX, body3d='', jc='center', anchor='t', ln=HAIRLINE):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def body_pr(self):
        avl = ''.join(f'<a:gd name="{n}" fmla="val {v}"/>' for n, v in self.adj)
        l, t, r, b = self.ins
        fit = {'noAutofit': '<a:noAutofit/>', 'spAutoFit': '<a:spAutoFit/>', 'normAutofit': '<a:normAutofit/>'}[self.autofit]
        return (f'<wps:bodyPr rot="0" spcFirstLastPara="0" vertOverflow="overflow" horzOverflow="overflow" vert="horz" '
                f'wrap="{self.wrap}" lIns="{l}" tIns="{t}" rIns="{r}" bIns="{b}" numCol="1" spcCol="0" rtlCol="0" '
                f'fromWordArt="{int(self.from_wordart)}" anchor="{self.anchor}" anchorCtr="0" forceAA="0" compatLnSpc="1">'
                f'<a:prstTxWarp prst="{self.warp}"><a:avLst>{avl}</a:avLst></a:prstTxWarp>{fit}{self.body3d}</wps:bodyPr>')

    def graphic(self):
        cx, cy = self.size
        txbx = ''.join(para(p, self.jc) for p in self.paras)
        return (f'<a:graphic><a:graphicData uri="{fx.URI_WPS}"><wps:wsp><wps:cNvSpPr txBox="1"/><wps:spPr>'
                f'<a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/>'
                f'</a:prstGeom><a:noFill/>{self.ln}</wps:spPr>'
                f'<wps:txbx><w:txbxContent>{txbx}</w:txbxContent></wps:txbx>{self.body_pr()}</wps:wsp></a:graphicData></a:graphic>')

    def summary(self):
        adj = ' '.join(f'{n}={v}' for n, v in self.adj) or 'default'
        texts = ' / '.join(''.join(r.text for r in p) for p in self.paras)
        fxs = ' '.join(sorted({m for p in self.paras for r in p for m in re.findall(r'<w14:(\w+)', r.raw or r.fx)} & set(ORDER)))
        return (f'{self.warp} adj {adj}; fromWordArt {int(self.from_wordart)} {self.autofit} wrap {self.wrap} '
                f'ins {"/".join(str(i) for i in self.ins)} {self.size[0] / EMU:g}x{self.size[1] / EMU:g}pt; '
                f'"{texts}"{"; " + fxs if fxs else ""}{"; bodyPr 3-D" if self.body3d else ""}')

    def page(self, n):
        cx, cy = self.size
        d = (f'<wp:anchor distT="0" distB="0" distL="0" distR="0" simplePos="0" relativeHeight="{251658240 + n}" behindDoc="0" '
             'locked="0" layoutInCell="1" allowOverlap="1"><wp:simplePos x="0" y="0"/>'
             f'<wp:positionH relativeFrom="page"><wp:posOffset>{X0}</wp:posOffset></wp:positionH>'
             f'<wp:positionV relativeFrom="page"><wp:posOffset>{Y0}</wp:posOffset></wp:positionV>'
             f'<wp:extent cx="{cx}" cy="{cy}"/><wp:effectExtent l="0" t="0" r="0" b="0"/><wp:wrapNone/>'
             f'<wp:docPr id="{n}" name="Text Box {n}"/><wp:cNvGraphicFramePr/>{self.graphic()}</wp:anchor>')
        return (f'<w:r><mc:AlternateContent><mc:Choice Requires="wps"><w:drawing>{d}</w:drawing></mc:Choice>'
                '</mc:AlternateContent></w:r>'), ''

class Body:
    """One page of body text: a 36 pt paragraph of the effect, then a 14 pt paragraph with the effect on its middle run.
    raw, if given, is a gallery style's w:rPr content as Word wrote it (at 36 pt), used in place of fx_xml."""
    kind = 'body'

    def __init__(self, note, fx_xml, text='Body text effect', small=True, raw=None):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def summary(self):
        return ' '.join(re.findall(r'<w14:(\w+)', self.raw or self.fx_xml)) or 'no effect'

    def page(self, n):
        small = self.raw and re.sub(r'<w:sz w:val="72"/><w:szCs w:val="72"/>', '<w:sz w:val="28"/><w:szCs w:val="28"/>', self.raw)
        first = para([Run(self.text, fx=self.fx_xml, raw=self.raw)], jc='left', before=720)
        second = para([Run('Plain words before the effect, ', size=14), Run('this run has it', size=14, fx=self.fx_xml, raw=small),
                       Run(', and plain words after it in the same line.', size=14)], jc='left', before=480) if self.small else ''
        return '', first + second

# VML: the shapetypes Word writes for text shape type 136 (a watermark's, read from a Word-written watermark), and a
# shapetype with nothing but its o:spt for the rest.
SPT136 = ('<v:shapetype id="_x0000_t136" coordsize="21600,21600" o:spt="136" adj="10800" path="m@7,l@8,m@5,21600l@6,21600e">'
          '<v:formulas><v:f eqn="sum #0 0 10800"/><v:f eqn="prod #0 2 1"/><v:f eqn="sum 21600 0 @1"/><v:f eqn="sum 0 0 @2"/>'
          '<v:f eqn="sum 21600 0 @3"/><v:f eqn="if @0 @3 0"/><v:f eqn="if @0 21600 @1"/><v:f eqn="if @0 0 @2"/>'
          '<v:f eqn="if @0 @4 21600"/><v:f eqn="mid @5 @6"/><v:f eqn="mid @8 @5"/><v:f eqn="mid @7 @8"/><v:f eqn="mid @6 @7"/>'
          '<v:f eqn="sum @6 0 @5"/></v:formulas><v:path textpathok="t" o:connecttype="custom" '
          'o:connectlocs="@9,0;@10,10800;@11,21600;@12,10800" o:connectangles="270,180,90,0"/><v:textpath on="t" fitshape="t"/>'
          '<v:handles><v:h position="#0,bottomRight" xrange="6629,14971"/></v:handles><o:lock v:ext="edit" text="t" shapetype="t"/>'
          '</v:shapetype>')

def spt_only(spt, tid=None):
    return (f'<v:shapetype id="_x0000_t{tid or spt}" coordsize="21600,21600" o:spt="{spt}"><v:path textpathok="t"/>'
            '<v:textpath on="t" fitshape="t"/><o:lock v:ext="edit" text="t" shapetype="t"/></v:shapetype>')

# 136 with another path: its top line slanted down to the middle of the right side, its bottom line as it was. If Word
# draws by the path, the text leans; if by o:spt, it is plain.
SPT136_OTHER_PATH = SPT136.replace('id="_x0000_t136"', 'id="_x0000_t136x"').replace(
    'path="m@7,l@8,m@5,21600l@6,21600e"', 'path="m0,0l21600,10800m0,21600l21600,21600e"')

class Vml:
    """One page: a VML WordArt at (X0, Y0), BOX, in front of the text."""
    kind = 'vml'

    def __init__(self, note, spt, shapetype, string='WordArt', tid=None, style=None, fill='fillcolor="black" stroked="f"', extra=''):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def summary(self):
        return f'o:spt {self.spt} type #_x0000_t{self.tid or self.spt}; "{self.string}"'

    def shape(self, n, pos=None, z=None):
        cx, cy = BOX
        pos = pos or (f'position:absolute;margin-left:{X0 / EMU:g}pt;margin-top:{Y0 / EMU:g}pt;width:{cx / EMU:g}pt;'
                      f'height:{cy / EMU:g}pt;z-index:{z or n};mso-position-horizontal-relative:page;'
                      'mso-position-vertical-relative:page')
        style = self.style or f'font-family:&quot;{FONT}&quot;;font-size:36pt;v-text-kern:t'
        return (f'<v:shape id="WordArt{n}" o:spid="_x0000_s{1024 + n}" type="#_x0000_t{self.tid or self.spt}" style="{pos}" '
                f'{self.fill}>{self.extra}<v:textpath style="{style}" trim="t" fitpath="t" string="{escape(self.string)}"/></v:shape>')

    def page(self, n, typed=None):
        st = '' if typed is not None and (self.tid or self.spt) in typed else self.shapetype
        if typed is not None:
            typed.add(self.tid or self.spt)
        return f'<w:r><w:pict>{st}{self.shape(n)}</w:pict></w:r>', ''

class Mark:
    """One page and its own section: a text watermark in the section's header, as Word writes one."""
    kind = 'watermark'

    def __init__(self, note, rotation=315, string='WATERMARK', fill='fillcolor="silver" stroked="f"', opacity='.5',
                 font='Aptos', width=468, height=117):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def summary(self):
        return f'header watermark o:spt 136 rotation {self.rotation} "{self.string}" {self.width}x{self.height}pt'

    def header(self, n):
        rot = f'rotation:{self.rotation};' if self.rotation else ''
        pos = (f'position:absolute;margin-left:0;margin-top:0;width:{self.width}pt;height:{self.height}pt;{rot}z-index:-251657216;'
               'mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;'
               'mso-position-vertical-relative:margin')
        op = f'<v:fill opacity="{self.opacity}"/>' if self.opacity else ''
        shape = (f'<v:shape id="PowerPlusWaterMarkObject{n}" o:spid="_x0000_s{2048 + n}" type="#_x0000_t136" style="{pos}" '
                 f'o:allowincell="f" {self.fill}>{op}<v:textpath style="font-family:&quot;{self.font}&quot;;font-size:1pt" '
                 f'string="{escape(self.string)}"/></v:shape>')
        xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in NS.items())
        return (f'{fx.DECL}<w:hdr {xmlns} mc:Ignorable="w14"><w:p><w:r><w:pict>{SPT136}{shape}</w:pict></w:r></w:p></w:hdr>')

    def page(self, n):
        words = 'Body text over which the watermark is drawn, in the header, behind it. ' * 6
        return '', para([Run(words, size=11)], jc='left', before=240)

# ----------------------------------------------------------------------------------------------------------- families

def fam_warps(other, texts, word_form=False):
    """The 40 warps; in Word's form (spAutoFit, stored at 144 by 144 pt), or in the fixed box."""
    form = dict(autofit='spAutoFit', size=WORD_BOX) if word_form else {}
    def f():
        out = []
        for name, adj, rng in warps():
            a = other_adj(adj, rng) if other else []
            note = f'{name}, {"adjusted" if other else "default"}, {len(texts)} line{"s" if len(texts) > 1 else ""}'
            out.append(Art(note + (", in Word's form" if word_form else ''), lines(texts), warp=name, adj=a, **form))
        return out
    return f

MULTI = ['textButton', 'textButtonPour', 'textDeflateInflate', 'textDeflateInflateDeflate', 'textPlain', 'textInflate',
         'textArchUp', 'textCircle']

def fam_three():
    return [Art(f'{w}, three lines', lines(THREE), warp=w) for w in MULTI]

FIT = [('noAutofit', 'none'), ('noAutofit', 'square'), ('spAutoFit', 'none'), ('normAutofit', 'square')]

def fam_form():
    out = []
    for w in ('textArchUp', 'textInflate', 'textWave1'):
        for fwa in (True, False):
            for fit, wrap in FIT:
                out.append(Art(f'{w}, fromWordArt {int(fwa)}, {fit}, wrap {wrap}', lines(ONE), warp=w, from_wordart=fwa,
                               autofit=fit, wrap=wrap))
        out.append(Art(f'{w}, no insets', lines(ONE), warp=w, ins=(0, 0, 0, 0)))
    for w in ('textArchUp', 'textInflate'):
        for size in (18, 72):
            out.append(Art(f'{w}, {size} pt', lines(ONE, size=size), warp=w))
    out.append(Art('textInflate, a square box 144 by 144 pt', lines(ONE), warp='textInflate', size=(144 * EMU, 144 * EMU)))
    out.append(Art('textCircle, a square box 216 by 216 pt', lines(ONE), warp='textCircle', size=(216 * EMU, 216 * EMU)))
    out.append(Art('textNoShape, as Word inserts WordArt: fromWordArt 0, spAutoFit, wrap none, 144 by 144 pt', lines(ONE),
                   autofit='spAutoFit', size=WORD_BOX))
    out.append(Art('textArchUp, as Word writes Follow Path, with no line (Word\'s own: no fill, no line)', lines(ONE), warp='textArchUp',
                   autofit='spAutoFit', size=WORD_BOX, ln='<a:ln><a:noFill/></a:ln>'))
    return out

def fam_rect():
    out = []
    for w in ('textPlain', 'textInflate', 'textArchUp'):
        for t in ('WordArt', 'wave', 'Warpy', '  WordArt  '):
            out.append(Art(f'{w}, the text "{t}"', lines([t]), warp=w))
    return out

EFFECTS = [
    ('solid fill red, w:color black', [text_fill(solid('C00000'))]),
    ('solid fill red at alpha 40000', [text_fill(solid('C00000', 40000))]),
    ('no fill, black outline 1 pt (hollow)', [text_fill(NOFILL), outline(12700)]),
    ('no fill, no outline (invisible)', [text_fill(NOFILL), outline(12700, fill=NOFILL)]),
    ('outline 0.25 pt red', [outline(3175, solid('FF0000'))]),
    ('outline 3 pt red', [outline(38100, solid('FF0000'))]),
    ('outline 3 pt red, dash', [outline(38100, solid('FF0000'), dash='dash')]),
    ('outline 3 pt red, sysDot, round cap', [outline(38100, solid('FF0000'), dash='sysDot', cap='rnd')]),
    ('outline 3 pt red, bevel join', [outline(38100, solid('FF0000'), join='bevel')]),
    ('outline 3 pt red, miter join', [outline(38100, solid('FF0000'), join='miter')]),
    ('outline 6 pt red, compound dbl', [outline(76200, solid('FF0000'), cmpd='dbl')]),
    ('outline 3 pt gradient red to blue', [outline(38100, grad([(0, 'FF0000'), (100000, '0000FF')], lin=0))]),
    ('gradient fill red to blue, linear 0 degrees', [text_fill(grad([(0, 'FF0000'), (100000, '0000FF')], lin=0))]),
    ('gradient fill red to blue, linear 90 degrees', [text_fill(grad([(0, 'FF0000'), (100000, '0000FF')], lin=5400000))]),
    ('gradient fill three stops, linear 45 degrees', [text_fill(grad([(0, 'FF0000'), (50000, 'FFFF00'), (100000, '0000FF')], lin=2700000))]),
    ('gradient fill red to blue, path circle', [text_fill(grad([(0, 'FF0000'), (100000, '0000FF')], path='circle'))]),
    ('shadow, outer, bottom right, alpha 60000', [shadow()]),
    ('shadow, outer, bottom right, alpha 20000', [shadow(alpha=20000)]),
    ('shadow, no blur, 6 pt at 45 degrees', [shadow(blurRad=0, dist=76200)]),
    ('shadow, perspective (sx 100%, sy 23%, kx -1200000, algn bl)', [shadow(dir=16200000, dist=0, sy=23000, kx=-1200000, algn='bl', blurRad=76200)]),
    ('glow 5 pt orange alpha 60000', [glow()]),
    ('glow 18 pt blue alpha 40000', [glow(228600, '4472C4', 40000)]),
    ('reflection, tight', [reflection()]),
    ('reflection, half, 4 pt offset', [reflection(stA=50000, endPos=60000, dist=50800, blurRad=12700)]),
    ('outline, gradient fill, shadow and glow', [outline(12700, solid('1F3864')), text_fill(grad([(0, 'FFC000'), (100000, 'C00000')])),
                                                 shadow(), glow(50800, '70AD47', 40000)]),
]

def fam_effects():
    return [Art(f'flat, {note}', lines(ONE, fx=effects(*e))) for note, e in EFFECTS]

def fam_effects_warped():
    picks = [EFFECTS[i] for i in (2, 5, 12, 16, 20, 22)]
    return [Art(f'{w}, {note}', lines(ONE, fx=effects(*e)), warp=w) for w in ('textArchUp', 'textWave1') for note, e in picks]

def fam_runs():
    def two():
        return [[Run('Word', size=24, color='C00000'), Run('Art', size=48, color='2F5597')]]
    out = [Art('flat, two runs: 24 pt red, 48 pt blue', two())]
    for w in ('textPlain', 'textArchUp', 'textInflate', 'textCircle', 'textWave1'):
        out.append(Art(f'{w}, two runs: 24 pt red, 48 pt blue', two(), warp=w))
    out.append(Art('textDeflateInflate, two lines, 24 pt red then 48 pt blue',
                   [[Run('WordArt', size=24, color='C00000')], [Run('Two lines', size=48, color='2F5597')]], warp='textDeflateInflate'))
    out.append(Art('textPlain, two runs, Aptos then Aptos bold', [[Run('Word'), Run('Art', bold=True)]], warp='textPlain'))
    return out

BEVEL = ('circle', 38100, 38100)
SCENE_BODY = ('<a:scene3d><a:camera prst="isometricOffAxis1Right"/><a:lightRig rig="threePt" dir="t"/></a:scene3d>'
              '<a:sp3d extrusionH="57150"><a:bevelT w="38100" h="38100"/></a:sp3d>')

WORD_BEVEL = ('<a:scene3d><a:camera prst="orthographicFront"/><a:lightRig rig="threePt" dir="t"/></a:scene3d>'
              '<a:sp3d extrusionH="57150"><a:bevelT w="38100" h="38100"/></a:sp3d>')
WORD_BEVEL_RUN = ('<w14:props3d w14:extrusionH="57150" w14:contourW="0" w14:prstMaterial="warmMatte">'
                  '<w14:bevelT w14:w="38100" w14:h="38100" w14:prst="circle"/></w14:props3d>')

def fam_3d():
    gold = text_fill(solid('FFC000'))
    word = dict(autofit='spAutoFit', size=WORD_BOX)
    return [
        Art("Word's Bevel Circle: bodyPr scene3d and sp3d, w14:props3d on the run", lines(ONE, fx=WORD_BEVEL_RUN), body3d=WORD_BEVEL, **word),
        Art("Word's Bevel Circle on a gold fill", lines(ONE, fx=effects(gold, WORD_BEVEL_RUN)), body3d=WORD_BEVEL, **word),
        Art("Word's 3-D Rotation: bodyPr scene3d isometricLeftDown alone", lines(ONE),
            body3d='<a:scene3d><a:camera prst="isometricLeftDown"/><a:lightRig rig="threePt" dir="t"/></a:scene3d>', **word),
        Art("Word's 3-D Rotation and Bevel together", lines(ONE, fx=WORD_BEVEL_RUN),
            body3d=WORD_BEVEL.replace('orthographicFront', 'isometricLeftDown'), **word),
        Art('flat, w14 bevel circle 3 pt', lines(ONE, fx=effects(gold, scene3d(), props3d(BEVEL)))),
        Art('flat, w14 bevel angle 6 pt, metal', lines(ONE, fx=effects(gold, scene3d(), props3d(('angle', 76200, 76200), material='metal')))),
        Art('flat, w14 extrusion 4.5 pt, perspectiveRelaxed', lines(ONE, fx=effects(gold, scene3d('perspectiveRelaxed'), props3d(extrusion=57150, extrusion_clr='7F6000')))),
        Art('flat, w14 bevel and extrusion, isometricOffAxis1Right', lines(ONE, fx=effects(gold, scene3d('isometricOffAxis1Right'), props3d(BEVEL, 57150)))),
        Art('flat, bodyPr scene3d isometricOffAxis1Right and sp3d, no w14:props3d', lines(ONE, fx=gold), body3d=SCENE_BODY),
        Art('flat, bodyPr scene3d and flatTx', lines(ONE, fx=gold),
            body3d='<a:scene3d><a:camera prst="perspectiveRelaxed"/><a:lightRig rig="threePt" dir="t"/></a:scene3d><a:flatTx/>'),
        Art('textArchUp, w14 bevel circle 3 pt', lines(ONE, fx=effects(gold, scene3d(), props3d(BEVEL))), warp='textArchUp'),
        Art('textWave1, bodyPr scene3d isometricOffAxis1Right and sp3d', lines(ONE, fx=gold), warp='textWave1', body3d=SCENE_BODY),
    ]

def fam_body():
    return [Body(note, effects(*e)) for note, e in EFFECTS]

def fam_body_3d():
    gold = text_fill(solid('FFC000'))
    return [
        Body('w14 bevel circle 3 pt', effects(gold, scene3d(), props3d(BEVEL))),
        Body('w14 bevel angle 6 pt, metal', effects(gold, scene3d(), props3d(('angle', 76200, 76200), material='metal'))),
        Body('w14 extrusion 4.5 pt, perspectiveRelaxed', effects(gold, scene3d('perspectiveRelaxed'), props3d(extrusion=57150, extrusion_clr='7F6000'))),
        Body('w14 bevel and extrusion, isometricOffAxis1Right', effects(gold, scene3d('isometricOffAxis1Right'), props3d(BEVEL, 57150))),
        Body('w14 props3d bevel without scene3d', effects(gold, props3d(BEVEL))),
    ]

def fam_dml_mode12():
    return [Art(f'{name} in Word 2007 mode', lines(ONE), warp=name, ln='<a:ln><a:noFill/></a:ln>') for name, _, _ in warps()]

def fam_vml():
    out = [Vml(f'o:spt {s}, shapetype with only its o:spt', s, spt_only(s)) for s in range(136, 176)]
    out.append(Vml('o:spt 136, the shapetype whole, as Word writes a watermark', 136, SPT136, tid='136w'))
    out.append(Vml('o:spt 136, the shapetype whole but its top line slanted', 136, SPT136_OTHER_PATH, tid='136x'))
    return out

def fam_marks():
    return [Mark('diagonal, silver at 50%, as Word writes the DRAFT watermark'),
            Mark('horizontal, silver at 50%', rotation=0),
            Mark('diagonal, red, opaque, two words', string='TOP SECRET', fill='fillcolor="#c00000" stroked="f"', opacity='')]

STYLES_TSV = os.path.join(HERE, 'styles-word.tsv')
STYLES_HAND = os.path.join(HERE, 'cr083-styles-hand.docx')

def styles_word():
    """The 15 gallery styles as Word wrote them in Jason's document (build_hand_start.py harvest): (id, run properties,
    what a WordArt's bodyPr gains)."""
    rows = list(csv.DictReader(open(STYLES_TSV, encoding='utf-8'), delimiter='\t'))
    return [(int(r['id']), r['rpr'], r['bodypr_extra']) for r in rows]

def fam_styles(warp):
    def f():
        return [Art(f'gallery style {i} on {warp}, in Word\'s form', [[Run('WordArt', raw=rpr)]], warp=warp, body3d=extra,
                    autofit='spAutoFit', size=WORD_BOX) for i, rpr, extra in styles_word()]
    f.__name__ = 'fam_styles_' + warp
    return f

def fam_styles_body():
    return [Body(f'gallery style {i} on body text', '', raw=rpr) for i, rpr, _ in styles_word()]

# --------------------------------------------------------------------------------------------------------- the XML

def label(pid, p, first):
    ppr = '' if first else '<w:pPr><w:pageBreakBefore/></w:pPr>'
    return f'<w:p>{ppr}{fx.run(f"{pid}: {p.note}.")}</w:p>', f'<w:p>{{drawing}}{fx.run(p.summary())}</w:p>'

SECT = fx.SECT

def document(probes, mode):
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in NS.items())
    parts, typed, hdr = [], set(), []
    for i, (pid, p) in enumerate(probes):
        first, second = label(pid, p, i == 0)
        n = i + 1
        inline, after = p.page(n, typed) if p.kind == 'vml' else p.page(n)
        parts.append(first + second.replace('{drawing}', inline) + after)
        if p.kind == 'watermark':
            rid = f'rIdH{n}'
            hdr.append((n, rid, p.header(n)))
            sect = SECT.replace('<w:sectPr>', f'<w:sectPr><w:headerReference w:type="default" r:id="{rid}"/>')
            if i < len(probes) - 1:      # each watermark its own section; the last section's sectPr is the body's
                parts.append(f'<w:p><w:pPr>{sect}</w:pPr></w:p>')
            else:
                last = sect
    body_sect = last if probes and probes[-1][1].kind == 'watermark' else SECT
    if any(p.kind == 'watermark' for _, p in probes):
        # a section break's own paragraph starts the next section on a new page; the labels' pageBreakBefore would
        # add a blank page, so they go
        parts = [x.replace('<w:pPr><w:pageBreakBefore/></w:pPr>', '') for x in parts]
    return (f'{fx.DECL}<w:document {xmlns} mc:Ignorable="w14"><w:body>{"".join(parts)}{body_sect}</w:body></w:document>', hdr)

CT_HDR = 'application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml'

def package(path, probes, mode=15):
    doc, hdr = document(probes, mode)
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>']
    rels += [f'<Relationship Id="{rid}" Type="{fx.REL}header" Target="header{n}.xml"/>' for n, rid, _ in hdr]
    ct = fx.CT.replace('</Types>', ''.join(f'<Override PartName="/word/header{n}.xml" ContentType="{CT_HDR}"/>'
                                           for n, _, _ in hdr) + '</Types>')
    settings = fx.SETTINGS.replace('w:val="15"', f'w:val="{mode}"')
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + ct)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', doc)
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', settings)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
        for n, _, xml in hdr:
            fx.entry(z, f'word/header{n}.xml', xml)

# --------------------------------------------------------------------------------------------------------- the sets

SETS = {
    'cr083-phase0': [
        ('wa10-warps-default-1', fam_warps(False, ONE, True), 15),
        ('wa11-warps-default-2', fam_warps(False, TWO, True), 15),
        ('wa15-warps-fixed-1', fam_warps(False, ONE), 15),
        ('wa16-warps-fixed-2', fam_warps(False, TWO), 15),
        ('wa12-warps-adjusted-1', fam_warps(True, ONE), 15),
        ('wa13-warps-adjusted-2', fam_warps(True, TWO), 15),
        ('wa14-three-lines', fam_three, 15),
        ('wa20-form', fam_form, 15),
        ('wa21-text-rect', fam_rect, 15),
        ('wa30-effects-flat', fam_effects, 15),
        ('wa31-effects-warped', fam_effects_warped, 15),
        ('wa40-runs', fam_runs, 15),
        ('wa60-3d', fam_3d, 15),
        ('wb50-body-effects', fam_body, 15),
        ('wb60-body-3d', fam_body_3d, 15),
        ('wv30-watermarks', fam_marks, 15),
        ('wv20-vml-spt-mode12', fam_vml, 12),
        ('wv10-dml-mode12', fam_dml_mode12, 12),
    ],
}
# The gallery styles (2026-11-12): Jason applied the 15 WordArt styles, four Transforms and Bevel by hand, and the 15
# Text Effects styles to body text (cr083-styles-hand.docx, staged as it is, first); these write each style over
# WordArt warped as Follow Path and as a Warp, flat, and over body text.
SETS['cr083-phase0-styles'] = [
    ('wg10-styles-flat', fam_styles('textNoShape'), 15),
    ('wg11-styles-arch', fam_styles('textArchUp'), 15),
    ('wg12-styles-wave', fam_styles('textWave1'), 15),
    ('wg20-styles-body', fam_styles_body, 15),
]
TIMEOUT = {'cr083-phase0': 300, 'cr083-phase0-styles': 300}
AS_IS = {'cr083-phase0': [('wa00-hand-jason', JASON)], 'cr083-phase0-styles': [('wg00-hand-styles', STYLES_HAND)]}
AS_IS_NOTE = {'wa00-hand-jason': 'seven WordArt "Hello World - ...": plain, shadow, reflection, bevel, 3D rotation, follow path, glow',
              'wg00-hand-styles': 'build_hand_start.py\'s 36 pages: the 15 WordArt styles, Transforms Arch, Square, Inflate, '
                                  'Wave: Down, Bevel Round (circle), 3-D Rotation (not applied), the 15 Text Effects on body text'}

def main(staging, only=()):
    manifest = ['set\tfile\tpage\tprobe\tkind\tmode\tnote\tsummary\n']
    for setname, jobs in AS_IS.items():
        for name, _ in jobs:
            manifest.append(f'{setname}\t{name}\t-\t{name.split("-")[0]}\tas is\t15\tJason\'s, made by hand in Word\t'
                            + AS_IS_NOTE[name] + '\n')
    for setname, files in SETS.items():
        for name, fam, mode in files:
            prefix = name.split('-')[0]
            for page, p in enumerate(fam(), 1):
                manifest.append(f'{setname}\t{name}\t{page}\t{prefix}-{page:02d}\t{p.kind}\t{mode}\t{p.note}\t{p.summary()}\n')
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
        for name, fam, mode in files:
            prefix = name.split('-')[0]
            probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(fam())]
            count += len(probes)
            d = os.path.join(staging, setname, name)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            package(f, probes, mode)
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as out:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT[setname],
                           'formats': ['docx', 'pdf']}, out)
            for x in (j, f):
                sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {setname}/{name}/{os.path.basename(x)}\n')
            order.append(name + '\n')
        open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
        open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
        print(setname, len(order), 'files,', len(order), 'jobs,', count, 'probes, and', len(AS_IS.get(setname, ())), 'as is')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
