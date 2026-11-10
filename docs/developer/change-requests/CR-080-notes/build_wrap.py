# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-080 phase 4: purpose-built probes for how Word wraps text round a DrawingML shape with no text, beside the same
anchor holding a picture. Jason, 2026-11-10: "Build and stage the probe round, tell portfolio to run it" (asked after
phase 4 was built with docx4j's reading of five rules no Word file had shown: CR-080 section 6, phase 4, "Unmeasured").

One probe to a page. Each page: two label lines (the probe's id and note, then the object), then the anchor's paragraph
and one more, of numbered filler words, so that where Word breaks each line, beside the object or below it, can be read
from its PDF by the word that opens the line. The words are the same on every page, so two pages differ only in their
object and its anchor.

- fw10-shapes: a rectangle with no text (fill 4472C4, a 1pt black line), in each wrap and position;
- fw11-pictures: the same pages, the same anchors, with a picture of the same extent (CR-081's photo.png) in place of the
  shape: Word's twin. Page k of fw10 and page k of fw11 are one probe's two objects;
- fw12-shape-only: questions only a shape asks: one that paints nothing, a line with no height, a group, a thick line,
  an ellipse, its wrap polygon a box or its own.

Every object is 144 x 108pt unless its note says; distances 3.6pt top and bottom, 9pt left and right (Word's for an
inserted shape); effect extent r = b = 12700, as Word writes it for a shape with a 1pt line (Word's saves in the
corpora: r and b only, the line's width), the twin's the same. Fonts and spacing from the package's docDefaults:
Arial 10pt, single, no space after. Each file is staged once: Word was deterministic over CR-081's measured draws.

usage: build_wrap.py <staging dir>   (also writes probes-phase4.tsv beside this script)"""
import hashlib, importlib.util, json, math, os, sys, zipfile
from xml.sax.saxutils import escape

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('cr081', os.path.join(HERE, '..', 'CR-081-notes', 'build_phase0.py'))
fx = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(fx)

SET = 'cr080-phase4'
EMU = 12700
CX, CY = 144 * EMU, 108 * EMU
SMALL = (72 * EMU, 54 * EMU)                  # inline, and in a table cell
NS = dict(fx.NS)
NS['wpg'] = 'http://schemas.microsoft.com/office/word/2010/wordprocessingGroup'
URI_WPG = NS['wpg']
FILL = '<a:solidFill><a:srgbClr val="4472C4"/></a:solidFill>'
LINE = '<a:ln w="12700"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>'
EFFECT = (0, 0, 12700, 12700)                 # Word's for a 1pt line: r and b
ROT30 = 1800000

def words(first, last):
    """Numbered filler words of three widths, so that a line's first word says where Word broke it."""
    stems = ('ab', 'abcde', 'abcdefgh')
    return ' '.join(f'{stems[n % 3]}{n:03d}' for n in range(first, last + 1)) + ' '

def polygon(points):
    s, rest = points[0], points[1:] + [points[0]]
    return (f'<wp:wrapPolygon edited="0"><wp:start x="{s[0]}" y="{s[1]}"/>'
            + ''.join(f'<wp:lineTo x="{x}" y="{y}"/>' for x, y in rest) + '</wp:wrapPolygon>')

BOX_POLY = polygon([(0, 0), (0, 21600), (21600, 21600), (21600, 0)])
ELLIPSE_POLY = polygon([(round(10800 + 10800 * math.cos(2 * math.pi * k / 16)), round(10800 + 10800 * math.sin(2 * math.pi * k / 16)))
                        for k in range(16)])

class Probe:
    """One page: an object anchored (or inline) in a paragraph of filler words.
    wrap: 'square', 'topAndBottom', 'tight', 'through', 'none', or 'inline'; side: wrapText; h, v: the position
    (('column', 0), ('column', 'center'), ('page', 254) ...: an offset in pt or an alignment); kind: 'shape', 'picture'
    or 'group'; where: 'body' or 'cell'."""
    def __init__(self, note, wrap='square', side='bothSides', h=('column', 0), v=('paragraph', 24), size=(CX, CY),
                 effect=EFFECT, behind=False, poly=BOX_POLY, rot=0, geom='rect', fill=FILL, ln=LINE, where='body'):
        self.__dict__.update(locals())
        del self.__dict__['self']

    def twin(self, kind):
        p = Probe(**{k: v for k, v in self.__dict__.items()})
        p.kind = kind
        return p

def pos(axis, ref, at):
    tag = 'positionH' if axis == 'h' else 'positionV'
    inner = f'<wp:align>{at}</wp:align>' if isinstance(at, str) else f'<wp:posOffset>{round(at * EMU)}</wp:posOffset>'
    return f'<wp:{tag} relativeFrom="{ref}">{inner}</wp:{tag}>'

def sp_pr(p):
    cx, cy = p.size
    rot = f' rot="{p.rot}"' if p.rot else ''
    return f'<a:xfrm{rot}><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="{p.geom}"><a:avLst/></a:prstGeom>'

def graphic(p, n):
    cx, cy = p.size
    if p.kind == 'picture':
        return (f'<a:graphic><a:graphicData uri="{fx.URI_PIC}"><pic:pic><pic:nvPicPr><pic:cNvPr id="{n}" name="photo.png"/>'
                f'<pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="rId10"/><a:stretch><a:fillRect/></a:stretch>'
                f'</pic:blipFill><pic:spPr>{sp_pr(p)}</pic:spPr></pic:pic></a:graphicData></a:graphic>')
    if p.kind == 'group':
        def child(i, x, geom, colour):
            return (f'<wps:wsp><wps:cNvPr id="{n * 1000 + i}" name="Child {i}"/><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x="{x}" y="0"/>'
                    f'<a:ext cx="{cx // 2}" cy="{cy}"/></a:xfrm><a:prstGeom prst="{geom}"><a:avLst/></a:prstGeom><a:solidFill>'
                    f'<a:srgbClr val="{colour}"/></a:solidFill>{LINE}</wps:spPr><wps:bodyPr/></wps:wsp>')
        return (f'<a:graphic><a:graphicData uri="{URI_WPG}"><wpg:wgp><wpg:cNvGrpSpPr/><wpg:grpSpPr><a:xfrm><a:off x="0" y="0"/>'
                f'<a:ext cx="{cx}" cy="{cy}"/><a:chOff x="0" y="0"/><a:chExt cx="{cx}" cy="{cy}"/></a:xfrm></wpg:grpSpPr>'
                f'{child(1, 0, "rect", "4472C4")}{child(2, cx // 2, "ellipse", "ED7D31")}</wpg:wgp></a:graphicData></a:graphic>')
    return (f'<a:graphic><a:graphicData uri="{fx.URI_WPS}"><wps:wsp><wps:cNvSpPr/><wps:spPr>{sp_pr(p)}{p.fill}{p.ln}</wps:spPr>'
            f'<wps:bodyPr/></wps:wsp></a:graphicData></a:graphic>')

def wrap_el(p):
    if p.wrap == 'square':
        return f'<wp:wrapSquare wrapText="{p.side}"/>'
    if p.wrap == 'topAndBottom':
        return '<wp:wrapTopAndBottom/>'
    if p.wrap in ('tight', 'through'):
        tag = 'wrapTight' if p.wrap == 'tight' else 'wrapThrough'
        return f'<wp:{tag} wrapText="{p.side}">{p.poly}</wp:{tag}>'
    return '<wp:wrapNone/>'

def drawing(p, n):
    cx, cy = p.size
    l, t, r, b = p.effect
    ee = f'<wp:effectExtent l="{l}" t="{t}" r="{r}" b="{b}"/>'
    name = {'picture': 'Picture', 'group': 'Group', 'shape': 'Shape'}[p.kind]
    docpr = f'<wp:docPr id="{n}" name="{name} {n}"/>'
    frame = ('<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect="1"/></wp:cNvGraphicFramePr>' if p.kind == 'picture'
             else '<wp:cNvGraphicFramePr/>')
    if p.wrap == 'inline':
        d = (f'<wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="{cx}" cy="{cy}"/>{ee}{docpr}{frame}'
             f'{graphic(p, n)}</wp:inline>')
    else:
        d = (f'<wp:anchor distT="45720" distB="45720" distL="114300" distR="114300" simplePos="0" '
             f'relativeHeight="{251658240 + n}" behindDoc="{1 if p.behind else 0}" locked="0" layoutInCell="1" allowOverlap="1">'
             f'<wp:simplePos x="0" y="0"/>{pos("h", *p.h)}{pos("v", *p.v)}<wp:extent cx="{cx}" cy="{cy}"/>{ee}{wrap_el(p)}'
             f'{docpr}{frame}{graphic(p, n)}</wp:anchor>')
    d = f'<w:drawing>{d}</w:drawing>'
    if p.kind != 'picture':
        d = f'<mc:AlternateContent><mc:Choice Requires="{"wpg" if p.kind == "group" else "wps"}">{d}</mc:Choice></mc:AlternateContent>'
    return f'<w:r>{d}</w:r>'

def run(text):
    return f'<w:r><w:t xml:space="preserve">{escape(text)}</w:t></w:r>'

def label(pid, p, first):
    ppr = '' if first else '<w:pPr><w:pageBreakBefore/></w:pPr>'
    what = {'picture': 'picture photo.png', 'group': 'group, a rect and an ellipse',
            'shape': f'shape {p.geom}' + (', no fill, no line' if not p.fill and not p.ln else '')}[p.kind]
    return f'<w:p>{ppr}{run(f"{pid}: {p.note}.")}</w:p><w:p>{run(what)}</w:p>'

def body(p, n):
    if p.wrap == 'inline':
        return (f'<w:p>{run(words(1, 30))}{drawing(p, n)}{run(words(31, 120))}</w:p>'
                f'<w:p>{run(words(121, 200))}</w:p>')
    if p.where == 'cell':
        tc = lambda w, inner: f'<w:tc><w:tcPr><w:tcW w:w="{w}" w:type="dxa"/></w:tcPr>{inner}</w:tc>'
        border = ''.join(f'<w:{e} w:val="single" w:sz="4" w:space="0" w:color="000000"/>' for e in
                         ('top', 'left', 'bottom', 'right', 'insideH', 'insideV'))
        return (f'<w:tbl><w:tblPr><w:tblW w:w="9026" w:type="dxa"/><w:tblBorders>{border}</w:tblBorders>'
                '<w:tblLayout w:type="fixed"/></w:tblPr><w:tblGrid><w:gridCol w:w="4513"/><w:gridCol w:w="4513"/></w:tblGrid>'
                f'<w:tr>{tc(4513, f"<w:p>{drawing(p, n)}{run(words(1, 80))}</w:p>")}{tc(4513, f"<w:p>{run(words(81, 120))}</w:p>")}'
                f'</w:tr></w:tbl><w:p>{run(words(121, 200))}</w:p>')
    return f'<w:p>{drawing(p, n)}{run(words(1, 130))}</w:p><w:p>{run(words(131, 200))}</w:p>'

def document(probes):
    xmlns = ' '.join(f'xmlns:{k}="{v}"' for k, v in NS.items())
    parts = []
    for i, (pid, p) in enumerate(probes):
        parts.append(label(pid, p, i == 0))
        parts.append(body(p, i + 1))
    return f'{fx.DECL}<w:document {xmlns}><w:body>{"".join(parts)}{fx.SECT}</w:body></w:document>'

STYLES = (f'{fx.DECL}<w:styles xmlns:w="{NS["w"]}"><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Arial" '
          'w:hAnsi="Arial" w:eastAsia="Arial" w:cs="Arial"/><w:sz w:val="20"/><w:szCs w:val="20"/><w:lang w:val="en-US"/></w:rPr>'
          '</w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after="0" w:line="240" w:lineRule="auto"/></w:pPr></w:pPrDefault>'
          '</w:docDefaults></w:styles>')
CT = fx.CT.replace('<Override PartName="/word/settings.xml"', '<Override PartName="/word/styles.xml" ContentType="application/'
                   'vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/><Override PartName="/word/settings.xml"')

def package(path, probes):
    pics = any(p.kind == 'picture' for _, p in probes)
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>',
            f'<Relationship Id="rId3" Type="{fx.REL}styles" Target="styles.xml"/>']
    if pics:
        rels.append(f'<Relationship Id="rId10" Type="{fx.REL}image" Target="media/photo.png"/>')
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + CT)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', document(probes))
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/styles.xml', STYLES)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
        if pics:
            fx.entry(z, 'word/media/photo.png', fx.PICTURES['photo.png']())

# ------------------------------------------------------------------------------------------------------ the probes

def twinned():
    """Each probe once, its object a shape in fw10 and a picture in fw11."""
    rot_ext = (220345, 365252, 220345, 365252)       # a 144 x 108pt box turned 30 degrees: 178.7 x 165.5pt bounds
    return [
        Probe('square both sides, at the column left'),
        Probe('square both sides, aligned right', h=('column', 'right')),
        Probe('square both sides, centred in the column', h=('column', 'center')),
        Probe('square largest, 100pt in', side='largest', h=('column', 100)),
        Probe('square, text on the left only, centred', side='left', h=('column', 'center')),
        Probe('square, text on the right only, centred', side='right', h=('column', 'center')),
        Probe('top and bottom', wrap='topAndBottom', h=('column', 100)),
        Probe('tight both sides, a box polygon, at the column left', wrap='tight'),
        Probe('through both sides, a box polygon, at the column left', wrap='through'),
        Probe('tight both sides, a box polygon, centred', wrap='tight', h=('column', 'center')),
        Probe('tight both sides, an ellipse polygon, centred', wrap='tight', h=('column', 'center'), poly=ELLIPSE_POLY,
              geom='ellipse'),
        Probe('behind the text, no wrap', wrap='none', behind=True),
        Probe('behind the text, square', behind=True),
        Probe('in front of the text, no wrap', wrap='none'),
        Probe('square, against the page: 254pt in, 200pt down', h=('page', 254), v=('page', 200)),
        Probe('square, effect extent 3pt on every side', effect=(38100, 38100, 38100, 38100)),
        Probe('square, turned 30 degrees, effect extent as written for a 1pt line', rot=ROT30),
        Probe('square, turned 30 degrees, effect extent the turned bounds', rot=ROT30, effect=rot_ext),
        Probe('inline 72 x 54pt, in a line of words, effect extent 0', wrap='inline', size=SMALL, effect=(0, 0, 0, 0)),
        Probe('inline 72 x 54pt, effect extent 6pt on every side', wrap='inline', size=SMALL,
              effect=(76200, 76200, 76200, 76200)),
        Probe('square both sides, 72 x 54pt, in a table cell', size=SMALL, where='cell'),
    ]

def shape_only():
    line_size = (CX, 0)
    return [
        Probe('square, painting nothing (fw10-01 painted)', fill='<a:noFill/>', ln='<a:ln><a:noFill/></a:ln>'),
        Probe('square, painting nothing, effect extent 0', fill='<a:noFill/>', ln='<a:ln><a:noFill/></a:ln>', effect=(0, 0, 0, 0)),
        Probe('square, a 1pt line, effect extent 0 (fw10-01 r = b = 1pt)', effect=(0, 0, 0, 0)),
        Probe('square, a 6pt line, effect extent 0', ln='<a:ln w="76200"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>',
              effect=(0, 0, 0, 0)),
        Probe('square, a 6pt line, effect extent r = b = 6pt', ln='<a:ln w="76200"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>',
              effect=(0, 0, 76200, 76200)),
        Probe('top and bottom, a line 144pt long, no height, 2pt wide, effect extent 0', wrap='topAndBottom', geom='line', fill='',
              ln='<a:ln w="25400"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>', size=line_size, effect=(0, 0, 0, 0),
              h=('column', 100)),
        Probe('top and bottom, the line, effect extent t = b = 1pt', wrap='topAndBottom', geom='line', fill='',
              ln='<a:ln w="25400"><a:solidFill><a:srgbClr val="000000"/></a:solidFill></a:ln>', size=line_size,
              effect=(0, 12700, 0, 12700), h=('column', 100)),
        Probe('square both sides, an ellipse, at the column left', geom='ellipse'),
        Probe('tight both sides, an ellipse with a box polygon, centred', wrap='tight', h=('column', 'center'), geom='ellipse'),
        Probe('square both sides, a group of two shapes, at the column left', ).twin('group'),
    ]

def files():
    t = twinned()
    shapes = [p.twin('shape') for p in t]
    pictures = [p.twin('picture') for p in t]
    only = [p if hasattr(p, 'kind') else p.twin('shape') for p in shape_only()]
    return [('fw10-shapes', shapes), ('fw11-pictures', pictures), ('fw12-shape-only', only)]

def main(staging):
    manifest = ['set\tfile\tpage\tprobe\tobject\twrap\tside\th\tv\tsize\teffect\tbehind\trot\tgeom\twhere\tnote\n']
    sums, order = [], []
    for name, probes in files():
        prefix = name.split('-')[0]
        pages = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(probes)]
        for page, (pid, p) in enumerate(pages, 1):
            manifest.append('\t'.join(str(x) for x in (SET, name, page, pid, p.kind, p.wrap, p.side, p.h, p.v,
                            f'{p.size[0] / EMU:g}x{p.size[1] / EMU:g}', ' '.join(str(e) for e in p.effect), int(p.behind),
                            p.rot, p.geom, p.where, p.note)) + '\n')
        d = os.path.join(staging, SET, name)
        os.makedirs(d)
        f = os.path.join(d, name + '.docx')
        package(f, pages)
        j = os.path.join(d, 'job.json')
        with open(j, 'w', newline='') as out:
            json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 300, 'formats': ['docx', 'pdf']}, out)
        for x in (j, f):
            sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{name}/{os.path.basename(x)}\n')
        order.append(name + '\n')
        print(name, len(pages), 'probes')
    open(os.path.join(HERE, 'probes-phase4.tsv'), 'w', newline='').writelines(manifest)
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines(order)

if __name__ == '__main__':
    main(sys.argv[1])
