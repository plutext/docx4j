# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 0: a model of Word's text warp, to test against Word's PDFs of the probes.

- The 40 presets are evaluated from docx4j's presetTextWarpDefinitions.xml at a text rectangle's size (the guide
  formulas of ISO/IEC 29500-1 20.1.9.11, the built-in guides, and the path commands), each path flattened to a
  polyline.
- warp() maps points as 20.1.9.19 describes: a point's horizontal and vertical fractions of a rectangle (the text's
  ink, or another, as asked), the horizontal fraction measured along the top and the bottom path by arc length (or by
  the path's x, as docxide-pdf samples y(x)), and the vertical fraction down the line joining the two points.
- glyphs() gives the unwarped outlines of a line of text, from the font Word embedded and the positions of Word's
  invisible text (mutool stext origins), flattened.
- word_page() reads a probe page of Word's PDF: the drawn box (the 0.25 pt grey line), the glyph fills, the invisible
  text.

All coordinates in points, y down from the page's top."""
import math, os, re, subprocess, sys
import xml.etree.ElementTree as ET
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', 'CR-080-notes'))
sys.path.insert(0, os.path.join(HERE, '..', 'CR-081-notes'))
import pdfpaths                                                               # noqa: E402
import pdfdraws as pd                                                         # noqa: E402

A = '{http://schemas.openxmlformats.org/drawingml/2006/main}'
WARP_XML = os.path.normpath(os.path.join(HERE, '..', '..', '..', '..', 'docx4j-core', 'src', 'main', 'resources', 'org',
                                         'docx4j', 'model', 'shapes', 'presetTextWarpDefinitions.xml'))
_DEFS = {el.tag: el for el in ET.parse(WARP_XML).getroot()}
NAMES = list(_DEFS)

# ---------------------------------------------------------------------------------------------------------- presets

def _builtin(w, h):
    ss, ls = min(w, h), max(w, h)
    g = {'w': w, 'h': h, 'l': 0.0, 't': 0.0, 'r': w, 'b': h, 'hc': w / 2, 'vc': h / 2, 'ss': ss, 'ls': ls,
         'cd2': 10800000.0, 'cd4': 5400000.0, 'cd8': 2700000.0, '3cd4': 16200000.0, '3cd8': 8100000.0,
         '5cd8': 13500000.0, '7cd8': 18900000.0}
    for n in (2, 3, 4, 5, 6, 8, 10, 12, 32):
        g[f'wd{n}'], g[f'hd{n}'] = w / n, h / n
    for n in (2, 4, 6, 8, 16, 32):
        g[f'ssd{n}'] = ss / n
    return g

def _rad(a):
    return a / 60000.0 * math.pi / 180.0

def _calc(fmla, g):
    op, *args = fmla.split()
    v = [float(a) if re.fullmatch(r'-?\d+(\.\d+)?', a) else g[a] for a in args]
    if op == 'val':
        return v[0]
    if op == '*/':
        return v[0] * v[1] / v[2]
    if op == '+-':
        return v[0] + v[1] - v[2]
    if op == '+/':
        return (v[0] + v[1]) / v[2]
    if op == '?:':
        return v[1] if v[0] > 0 else v[2]
    if op == 'abs':
        return abs(v[0])
    if op == 'sqrt':
        return math.sqrt(abs(v[0]))          # Office takes sqrt(|x|) (OI 2.1.1311; CR-080 phase 0)
    if op == 'max':
        return max(v[0], v[1])
    if op == 'min':
        return min(v[0], v[1])
    if op == 'pin':
        return v[0] if v[1] < v[0] else v[2] if v[1] > v[2] else v[1]
    if op == 'sin':
        return v[0] * math.sin(_rad(v[1]))
    if op == 'cos':
        return v[0] * math.cos(_rad(v[1]))
    if op == 'tan':
        return v[0] * math.tan(_rad(v[1]))
    if op == 'at2':
        return math.degrees(math.atan2(v[1], v[0])) * 60000.0
    if op == 'cat2':
        return v[0] * math.cos(math.atan2(v[2], v[1]))
    if op == 'sat2':
        return v[0] * math.sin(math.atan2(v[2], v[1]))
    if op == 'mod':
        return math.sqrt(v[0] ** 2 + v[1] ** 2 + v[2] ** 2)
    raise ValueError(op)

def _bez(p0, pts, n=96):
    ctrl = np.array([p0] + pts)
    t = np.linspace(0, 1, n + 1)[1:, None]
    if len(ctrl) == 3:
        return (1 - t) ** 2 * ctrl[0] + 2 * (1 - t) * t * ctrl[1] + t ** 2 * ctrl[2]
    return ((1 - t) ** 3 * ctrl[0] + 3 * (1 - t) ** 2 * t * ctrl[1] + 3 * (1 - t) * t ** 2 * ctrl[2] + t ** 3 * ctrl[3])

def _param(a, wr, hr):
    """The ellipse's parametric angle for DrawingML's (visual) angle a, in radians."""
    return math.atan2(wr * math.sin(a), hr * math.cos(a))

def _arc(cur, wr, hr, st, sw, n=180):
    """arcTo from cur: the visual angles st to st + sw (radians), sampled uniformly in the visual angle."""
    a = st + sw * np.linspace(0, 1, n + 1)
    t = np.unwrap(np.arctan2(wr * np.sin(a), hr * np.cos(a)))
    t0 = _param(st, wr, hr)
    t = t - t[0] + t0
    cx, cy = cur[0] - wr * math.cos(t0), cur[1] - hr * math.sin(t0)
    return np.stack([cx + wr * np.cos(t[1:]), cy + hr * np.sin(t[1:])], axis=1)

def preset_paths(name, w, h, adj=None):
    """The preset's paths at w by h (points), each an (n, 2) array, in the rectangle's own frame."""
    el = _DEFS[name]
    g = _builtin(w, h)
    av = el.find(A + 'avLst')
    for gd in (av if av is not None else []):
        g[gd.get('name')] = _calc(gd.get('fmla'), g)
    for k, v in (adj or {}).items():
        g[k] = float(v)
    gl = el.find(A + 'gdLst')
    for gd in (gl if gl is not None else []):
        g[gd.get('name')] = _calc(gd.get('fmla'), g)

    def pt(e):
        return [g[e.get('x')] if e.get('x') in g else float(e.get('x')), g[e.get('y')] if e.get('y') in g else float(e.get('y'))]
    out = []
    for path in el.find(A + 'pathLst'):
        pts, cur = [], None
        for c in path:
            tag = c.tag.replace(A, '')
            if tag == 'moveTo':
                cur = pt(c[0])
                pts.append(np.array([cur]))
            elif tag == 'lnTo':
                nxt = pt(c[0])
                seg = np.linspace(cur, nxt, 33)[1:]
                pts.append(seg)
                cur = nxt
            elif tag in ('cubicBezTo', 'quadBezTo'):
                seg = _bez(cur, [pt(p) for p in c])
                pts.append(seg)
                cur = list(seg[-1])
            elif tag == 'arcTo':
                v = {k: (g[c.get(k)] if c.get(k) in g else float(c.get(k))) for k in ('wR', 'hR', 'stAng', 'swAng')}
                seg = _arc(cur, v['wR'], v['hR'], _rad(v['stAng']), _rad(v['swAng']))
                pts.append(seg)
                cur = list(seg[-1])
            elif tag == 'close':
                pass
        out.append(np.concatenate(pts))
    return out

# ------------------------------------------------------------------------------------------------------------- warp

class Measured:
    """A polyline measured by arc length, or by x (docxide-pdf's y(x))."""
    def __init__(self, pts, by='length'):
        self.p = pts
        if by == 'length':
            d = np.r_[0, np.cumsum(np.hypot(*np.diff(pts, axis=0).T))]
        elif by == 'param':                   # each command's own parameter (Bezier t, the arc's visual angle), the
            d = np.arange(len(pts), dtype=float)  # commands sharing u by their sample counts (preset_paths samples evenly)
        else:
            d = pts[:, 0] - pts[0, 0]
        self.s = d / d[-1] if d[-1] else d

    def at(self, u):
        return np.stack([np.interp(u, self.s, self.p[:, 0]), np.interp(u, self.s, self.p[:, 1])], axis=-1)

def warp(points, rect, top, bottom, by='length'):
    """points (n, 2) mapped from rect (x0, y0, x1, y1) between the top and bottom polylines (already in page
    coordinates)."""
    x0, y0, x1, y1 = rect
    u = (points[:, 0] - x0) / (x1 - x0)
    v = (points[:, 1] - y0) / (y1 - y0)
    t, b = Measured(top, by).at(u), Measured(bottom, by).at(u)
    return t + v[:, None] * (b - t)

def warp_blend(points, rect, top, bottom, n=801, levels=81):
    """The alternative: for a point's vertical fraction v, the curve (1 - v) top + v bottom (the two paths taken at the
    same arc-length fraction), measured by its own arc length; the point is that curve's point at the horizontal
    fraction u."""
    x0, y0, x1, y1 = rect
    u = np.clip((points[:, 0] - x0) / (x1 - x0), 0, 1)
    v = (points[:, 1] - y0) / (y1 - y0)
    s = np.linspace(0, 1, n)
    T, B = Measured(top).at(s), Measured(bottom).at(s)
    vl = np.linspace(0, 1, levels)
    grid = np.empty((levels, n, 2))
    for i, a in enumerate(vl):
        C = (1 - a) * T + a * B
        grid[i] = Measured(C).at(s)
    fi = np.clip(v, 0, 1) * (levels - 1)
    i0 = np.clip(np.floor(fi).astype(int), 0, levels - 2)
    t = (fi - i0)[:, None]
    ui = u * (n - 1)
    j0 = np.clip(np.floor(ui).astype(int), 0, n - 2)
    r = (ui - j0)[:, None]
    def g(i, j):
        return grid[i, j]
    a = g(i0, j0) * (1 - r) + g(i0, j0 + 1) * r
    b = g(i0 + 1, j0) * (1 - r) + g(i0 + 1, j0 + 1) * r
    out = a * (1 - t) + b * t
    # v outside [0, 1] (none for the ink rectangle) extrapolates along the joining line
    return out

# ----------------------------------------------------------------------------------------------------------- glyphs

def glyphs(font_path, chars):
    """chars: [(c, size, origin x, origin y)] -> the outlines, a list of (n, 2) closed polylines (page coordinates)."""
    from fontTools.ttLib import TTFont
    from fontTools.pens.basePen import BasePen
    f = TTFont(font_path)
    upm = f['head'].unitsPerEm
    cmap, gs = f.getBestCmap(), f.getGlyphSet()

    class Flat(BasePen):
        def __init__(self, gs, tf):
            super().__init__(gs)
            self.tf, self.contours, self.cur = tf, [], []
        def _moveTo(self, p):
            self.cur = [self.tf(p)]
        def _lineTo(self, p):
            a, b = np.array(self.cur[-1]), np.array(self.tf(p))
            self.cur += list(np.linspace(a, b, 9)[1:])
        def _qCurveToOne(self, p1, p2):
            self.cur += list(_bez(self.cur[-1], [self.tf(p1), self.tf(p2)], 16))
        def _curveToOne(self, p1, p2, p3):
            self.cur += list(_bez(self.cur[-1], [self.tf(p1), self.tf(p2), self.tf(p3)], 16))
        def _closePath(self):
            if self.cur:
                self._lineTo_point(self.cur[0])
                self.contours.append(np.array(self.cur))
            self.cur = []
        def _lineTo_point(self, q):
            a, b = np.array(self.cur[-1]), np.array(q)
            if np.hypot(*(b - a)) > 1e-9:
                self.cur += list(np.linspace(a, b, 9)[1:])
        def _endPath(self):
            if self.cur:
                self.contours.append(np.array(self.cur))
            self.cur = []

    out = []
    for c, size, ox, oy in chars:
        if c not in (None, ' ') and ord(c) in cmap:
            s = size / upm
            pen = Flat(gs, lambda p, s=s, ox=ox, oy=oy: [ox + p[0] * s, oy - p[1] * s])
            gs[cmap[ord(c)]].draw(pen)
            out += pen.contours
    return out

def glyph_segments(font_path, chars):
    """chars as glyphs() -> each contour as its segments, unflattened: [('m', p), ('l', p), ('q', c, p), ('c', c1, c2, p)],
    points in page coordinates (TrueType's implied on-curve points made explicit by fontTools)."""
    from fontTools.ttLib import TTFont
    from fontTools.pens.basePen import BasePen
    f = TTFont(font_path)
    upm = f['head'].unitsPerEm
    cmap, gs = f.getBestCmap(), f.getGlyphSet()

    class Seg(BasePen):
        def __init__(self, gs, tf):
            super().__init__(gs)
            self.tf, self.contours, self.cur = tf, [], []
        def _moveTo(self, p):
            self.cur = [('m', self.tf(p))]
        def _lineTo(self, p):
            self.cur.append(('l', self.tf(p)))
        def _qCurveToOne(self, p1, p2):
            self.cur.append(('q', self.tf(p1), self.tf(p2)))
        def _curveToOne(self, p1, p2, p3):
            self.cur.append(('c', self.tf(p1), self.tf(p2), self.tf(p3)))
        def _closePath(self):
            if self.cur:
                if self.cur[-1][-1] != self.cur[0][1]:
                    self.cur.append(('l', self.cur[0][1]))
                self.contours.append(self.cur)
            self.cur = []
        _endPath = _closePath

    out = []
    for c, size, ox, oy in chars:
        if c not in (None, ' ') and ord(c) in cmap:
            s = size / upm
            pen = Seg(gs, lambda p, s=s, ox=ox, oy=oy: (ox + p[0] * s, oy - p[1] * s))
            gs[cmap[ord(c)]].draw(pen)
            out += pen.contours
    return out

def warp_segments(contours, f, n=16):
    """Each contour's points (end and control points alike) mapped by f ((k, 2) -> (k, 2)), then flattened: lines stay
    lines between their mapped ends, curves are drawn through their mapped control points (20.1.9.19's method)."""
    out = []
    for con in contours:
        pts = np.array([p for seg in con for p in seg[1:]])
        mp = iter(f(pts))
        flat, cur = [], None
        for seg in con:
            q = [next(mp) for _ in seg[1:]]
            if seg[0] == 'm':
                cur = q[0]
                flat.append(cur)
            elif seg[0] == 'l':
                flat += list(np.linspace(cur, q[0], 9)[1:])
                cur = q[0]
            else:
                b = _bez(cur, q, n)
                flat += list(b)
                cur = b[-1]
        out.append(np.array(flat))
    return out

def _split(ctrl, k):
    """A Bezier (2 to 4 control points) split into k pieces of equal parameter, each as its control points."""
    ctrl = np.array(ctrl, dtype=float)
    out = []
    for i in range(k):
        a, b = i / k, (i + 1) / k
        def at(t, c=ctrl):
            while len(c) > 1:
                c = (1 - t) * c[:-1] + t * c[1:]
            return c[0]
        # the piece's control points by blossoming: for degree n, points P(a..a, b..b)
        n = len(ctrl) - 1
        pts = []
        for j in range(n + 1):
            c = ctrl.copy()
            ts = [a] * (n - j) + [b] * j
            for t in ts:
                c = (1 - t) * c[:-1] + t * c[1:]
            pts.append(c[0])
        out.append(pts)
    return out

def warp_subdivided(contours, f, k, n=16):
    """Each segment split into k pieces of equal parameter; each piece's control points mapped by f; lines drawn as
    lines and curves as curves through the mapped control points."""
    out = []
    for con in contours:
        flat, cur = [], None
        for seg in con:
            if seg[0] == 'm':
                cur = np.array(seg[1], dtype=float)
                flat.append(f(cur[None])[0])
                continue
            ctrl = [cur] + [np.array(p, dtype=float) for p in seg[1:]]
            for piece in _split(ctrl, k):
                q = f(np.array(piece))
                if seg[0] == 'l':
                    flat += list(np.linspace(q[0], q[-1], 5)[1:])
                else:
                    flat += list(_bez(q[0], list(q[1:]), n))
            cur = ctrl[-1]
        out.append(np.array(flat))
    return out

def warp_cubics(contours, f, n=24):
    """Word's form (measured): each segment made one cubic (a line's control points at its thirds, a quadratic raised
    to a cubic), its four control points mapped by f, and the cubic drawn through them."""
    out = []
    for con in contours:
        flat, cur = [], None
        for seg in con:
            if seg[0] == 'm':
                cur = np.array(seg[1], dtype=float)
                flat.append(f(cur[None])[0])
                continue
            if seg[0] == 'l':
                e = np.array(seg[1], dtype=float)
                c4 = [cur, cur + (e - cur) / 3, cur + 2 * (e - cur) / 3, e]
            elif seg[0] == 'q':
                c, e = np.array(seg[1], dtype=float), np.array(seg[2], dtype=float)
                c4 = [cur, cur + 2 * (c - cur) / 3, e + 2 * (c - e) / 3, e]
            else:
                c4 = [cur] + [np.array(p, dtype=float) for p in seg[1:]]
            q = f(np.array(c4))
            flat += list(_bez(q[0], list(q[1:]), n))
            cur = c4[-1]
        out.append(np.array(flat))
    return out

def bounds(contours):
    allp = np.concatenate(contours)
    return (allp[:, 0].min(), allp[:, 1].min(), allp[:, 0].max(), allp[:, 1].max())

# --------------------------------------------------------------------------------------------------------- Word's PDF

def _flatten_subpath(sp, n=12):
    pts, cur = [], None
    for seg in sp:
        if seg[0] == 'm':
            cur = [seg[1], seg[2]]
            pts.append(cur)
        elif seg[0] == 'l':
            nxt = [seg[1], seg[2]]
            pts += list(np.linspace(cur, nxt, 5)[1:])
            cur = nxt
        elif seg[0] == 'c':
            b = _bez(cur, [[seg[1], seg[2]], [seg[3], seg[4]], [seg[5], seg[6]]], n)
            pts += list(b)
            cur = list(b[-1])
        elif seg[0] == 'h' and pts:
            start = pts[0]
            pts += list(np.linspace(cur, start, 5)[1:])
            cur = list(start)
    return np.array(pts) if pts else None

def stext_chars(pdf, page):
    xml = subprocess.run(['mutool', 'draw', '-q', '-F', 'stext', '-o', '-', pdf, str(page)], capture_output=True,
                         check=True).stdout
    out = []
    for font in ET.fromstring(xml).iter('font'):
        for ch in font.iter('char'):
            out.append({'c': ch.get('c'), 'font': font.get('name'), 'size': float(font.get('size')),
                        'x': float(ch.get('x')), 'y': float(ch.get('y')), 'alpha': ch.get('alpha'), 'colour': ch.get('color'),
                        'quad': [float(v) for v in ch.get('quad').split()]})
    return out

def word_page(pdf, page, label_bottom=130.0):
    """{'box': (x0, y0, x1, y1) or None, 'fills': [dict with 'contours' (flattened, y down), 'fill', 'alpha'],
    'strokes': [...], 'images': [...], 'text': stext chars below the labels}"""
    po = pd.pages(pdf)[page - 1]
    h = pdfpaths.page_height(pdf, po)
    out = {'box': None, 'fills': [], 'strokes': [], 'images': [], 'shadings': [], 'text': []}
    for d in pdfpaths.page_paths(pdf, po):
        if d['kind'] in ('image', 'shading'):
            out[d['kind'] + 's'].append(d)
            continue
        if h - d['box'][3] < label_bottom:
            continue
        contours = []
        for sp in d['subpaths']:
            f = _flatten_subpath(sp)
            if f is not None and len(f) > 1:
                f[:, 1] = h - f[:, 1]
                contours.append(f)
        item = {**{k: d.get(k) for k in ('fill', 'stroke', 'lw', 'cap', 'join', 'dash', 'alpha', 'op')}, 'contours': contours}
        if d['kind'] == 'stroke' and d['stroke'] and abs(d['stroke'][0] - 0.749) < 0.01 and abs(d['lw'] - 0.25) < 0.01:
            x0, y0, x1, y1 = d['box']
            out['box'] = (x0, h - y1, x1, h - y0)
            continue
        out['fills' if d['kind'] in ('fill', 'fillstroke') else 'strokes'].append(item)
    out['text'] = [c for c in stext_chars(pdf, page) if c['y'] > label_bottom]
    return out

def distance(a, contours):
    """For each point of a ((n, 2)), the distance to the nearest segment of the polylines in contours (a list of
    (m, 2) arrays, or one array)."""
    if isinstance(contours, np.ndarray):
        contours = [contours]
    p0 = np.concatenate([c[:-1] for c in contours])
    p1 = np.concatenate([c[1:] for c in contours])
    d = p1 - p0
    dd = np.maximum((d ** 2).sum(axis=1), 1e-12)
    out = np.empty(len(a))
    for i in range(0, len(a), 256):
        q = a[i:i + 256, None, :]
        t = np.clip(((q - p0[None]) * d[None]).sum(axis=2) / dd[None], 0, 1)
        proj = p0[None] + t[..., None] * d[None]
        out[i:i + 256] = np.sqrt(((q - proj) ** 2).sum(axis=2)).min(axis=1)
    return out
