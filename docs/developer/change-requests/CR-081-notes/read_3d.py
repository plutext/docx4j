# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a: Word's 3-D pictures read for their geometry. A 3-D drawing is one lit picture with a soft mask
(phase 0 findings section 4); its silhouette is where the mask crosses one half. For a flat drawing (no depth, no
bevel showing) the silhouette is the frame's rectangle under the camera: a quadrilateral, whose corners this finds to
a fraction of a pixel. The mask is read along every row and column for its 0.5 crossings, interpolated; the crossings'
hull gives four rough corners; each side is then a least-squares line through the crossings along it, and the corners
are the sides' intersections.

Coordinates are points, x right and y down, from the frame's centre: for cr081-phase5a-r1 the frame is at FIXED
(216 pt, 216 pt from the page's top left) at the probe's size.

usage: read_3d.py <results dir> [prefix ...]   (prints one line a probe; writes phase5a-word.tsv beside this script)
       read_3d.py <pdf> <pages> [WxH] [frame x,y]   (one file's pages, as JSON)"""
import csv, json, os, sys
import numpy as np
import pdfdraws as pd

HERE = os.path.dirname(os.path.abspath(__file__))
PAGE_H = 841.89
FIXED = (216.0, 216.0)

def the_picture(pdf, page):
    """The page's largest image: Word's 3-D drawing."""
    draws = pd.page_draws(pdf, pd.pages(pdf)[page - 1])
    imgs = [d for d in draws if d.get('kind') == 'image' or 'name' in d]
    return max(imgs, key=lambda d: (d['box'][2] - d['box'][0]) * (d['box'][3] - d['box'][1])) if imgs else None

def crossings(a):
    """The mask's 0.5 crossings, (x, y) in pixels (0 at the image's left and top edges): each row's first and last,
    each column's first and last, interpolated."""
    out = []
    h, w = a.shape
    for horizontal in (True, False):
        m = a if horizontal else a.T
        n, length = m.shape
        for i in range(n):
            row = m[i]
            above = np.nonzero(row >= 0.5)[0]
            if not len(above):
                continue
            for j, step in ((above[0], -1), (above[-1], 1)):
                k = j + step                      # the pixel outside, below one half
                if 0 <= k < length:
                    t = (0.5 - row[j]) / (row[k] - row[j]) if row[k] != row[j] else 0.5
                    pos = j + 0.5 + step * t      # pixel centres are at index + 0.5
                else:
                    pos = j + 0.5 + step * 0.5
                out.append((pos, i + 0.5) if horizontal else (i + 0.5, pos))
    return np.array(out)

def hull(pts):
    pts = sorted(set(map(tuple, pts)))
    def cross(o, a, b): return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])
    lo, up = [], []
    for p in pts:
        while len(lo) >= 2 and cross(lo[-2], lo[-1], p) <= 0: lo.pop()
        lo.append(p)
    for p in reversed(pts):
        while len(up) >= 2 and cross(up[-2], up[-1], p) <= 0: up.pop()
        up.append(p)
    return lo[:-1] + up[:-1]

def reduce(poly, n):
    poly = list(poly)
    while len(poly) > n:
        areas = []
        for i in range(len(poly)):
            a, b, c = poly[i - 1], poly[i], poly[(i + 1) % len(poly)]
            areas.append(abs((b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])))
        poly.pop(int(np.argmin(areas)))
    return poly

def fit_line(pts):
    """Total least squares: a point on the line and its direction."""
    c = pts.mean(axis=0)
    _, _, vt = np.linalg.svd(pts - c)
    return c, vt[0]

def intersect(l1, l2):
    (p, d), (q, e) = l1, l2
    m = np.array([d, -e]).T
    t = np.linalg.solve(m, q - p)
    return p + t[0] * d

def quad(a, margin=3.0):
    """The silhouette's four corners in pixels, refined as the intersections of its sides' fitted lines; and the rms
    distance of the crossings from their sides (a quadrilateral fits within a fraction of a pixel)."""
    pts = crossings(a)
    rough = np.array(reduce(hull(pts), 4))
    sides, resid = [], []
    for i in range(4):
        p0, p1 = rough[i], rough[(i + 1) % 4]
        d = p1 - p0
        length = np.hypot(*d)
        u = d / length
        rel = pts - p0
        along = rel @ u
        off = np.abs(rel @ np.array([-u[1], u[0]]))
        sel = pts[(along > margin) & (along < length - margin) & (off < 1.5)]
        if len(sel) < 4:
            sel = np.array([p0, p1])
        line = fit_line(sel)
        sides.append(line)
        c, dd = line
        r = sel - c
        resid.append(float(np.sqrt(np.mean((r @ np.array([-dd[1], dd[0]])) ** 2))))
    corners = [intersect(sides[i - 1], sides[i]) for i in range(4)]
    return np.array(corners), max(resid)

def read(pdf, page, size=None, frame=FIXED):
    """The probe's picture: its box, pixel size, and corners from the frame's centre (points, y down)."""
    d = the_picture(pdf, page)
    if d is None:
        return None
    _, a = pd.pixels(pdf, d['obj'])
    x0, y0, x1, y1 = d['box']
    h, w = a.shape
    sx, sy = (x1 - x0) / w, (y1 - y0) / h
    top = PAGE_H - y1
    corners, resid = quad(a)
    cx = frame[0] + (size[0] / 2 if size else 0)
    cy = frame[1] + (size[1] / 2 if size else 0)
    pts = [(x0 + px * sx - cx, top + py * sy - cy) for px, py in corners]
    # clockwise from the top left
    order = sorted(range(4), key=lambda i: np.arctan2(pts[i][1], pts[i][0]))
    pts = [pts[i] for i in order]
    k = min(range(4), key=lambda i: pts[i][0] + pts[i][1])
    pts = pts[k:] + pts[:k]
    return {'box': [round(x0 - cx, 3), round(top - cy, 3), round(x1 - cx, 3), round(PAGE_H - y0 - cy, 3)],
            'px': [w, h], 'pt_per_px': [round(sx, 5), round(sy, 5)],
            'corners': [[round(x, 3), round(y, 3)] for x, y in pts], 'fit_rms_px': round(resid, 3),
            'mask_area_pt2': round(float((a >= 0.5).sum()) * sx * sy, 1)}

SET = 'cr081-phase5a-r1'
SIZES = {'fxc4': (72.0, 144.0), 'fxc5': (72.0, 144.0), 'fxc6': (72.0, 144.0)}    # the rest 144 by 108 pt
COLS = ['file', 'page', 'probe', 'note', 'w', 'h', 'quad', 'x0', 'y0', 'x1', 'y1', 'x2', 'y2', 'x3', 'y3', 'fit_rms_px',
        'bl', 'bt', 'br', 'bb', 'mask_l', 'mask_t', 'mask_r', 'mask_b', 'px_w', 'px_h', 'ab']

def mask_box(pdf, page, size):
    """The covered pixels' bounds (alpha >= 0.5), points from the frame's centre."""
    d = the_picture(pdf, page)
    _, a = pd.pixels(pdf, d['obj'])
    x0, y0, x1, y1 = d['box']
    h, w = a.shape
    sx, sy = (x1 - x0) / w, (y1 - y0) / h
    ys, xs = np.nonzero(a >= 0.5)
    cx, cy = FIXED[0] + size[0] / 2, FIXED[1] + size[1] / 2
    top = PAGE_H - y1
    return (x0 + xs.min() * sx - cx, top + ys.min() * sy - cy, x0 + (xs.max() + 1) * sx - cx, top + (ys.max() + 1) * sy - cy)

def main(results, only=()):
    probes = [p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t') if p['set'] == SET]
    rows = []
    for p in probes:
        prefix = p['probe'].split('-')[0]
        if prefix == 'fxca' or (only and prefix not in only):
            continue
        size = SIZES.get(prefix, (144.0, 108.0))
        pa = os.path.join(results, f"{p['file']}-a", f"{p['file']}.pdf")
        pb = os.path.join(results, f"{p['file']}-b", f"{p['file']}.pdf")
        page = int(p['page'])
        r = read(pa, page, size)
        rb = read(pb, page, size)
        mb = mask_box(pa, page, size)
        flat = prefix != 'fxc9'
        row = {'file': p['file'], 'page': page, 'probe': p['probe'], 'note': p['note'], 'w': size[0], 'h': size[1],
               'quad': 'yes' if flat else 'no', 'fit_rms_px': r['fit_rms_px'], 'px_w': r['px'][0], 'px_h': r['px'][1],
               'ab': 'same' if r == rb else 'differ'}
        for i, (x, y) in enumerate(r['corners']):
            row[f'x{i}'], row[f'y{i}'] = x, y
        row.update(dict(zip(['bl', 'bt', 'br', 'bb'], r['box'])))
        row.update(dict(zip(['mask_l', 'mask_t', 'mask_r', 'mask_b'], [round(v, 3) for v in mb])))
        rows.append(row)
        print(p['probe'], p['note'][:44].ljust(44), r['fit_rms_px'], row['ab'],
              ' '.join(f'({x:.2f},{y:.2f})' for x, y in r['corners']))
    with open(os.path.join(HERE, 'phase5a-word.tsv'), 'w', newline='') as f:
        w = csv.DictWriter(f, COLS, delimiter='\t', lineterminator='\n')
        w.writeheader()
        w.writerows(rows)

if __name__ == '__main__':
    if len(sys.argv) > 2 and sys.argv[2].replace(',', '').isdigit():
        pdf = sys.argv[1]
        size = tuple(map(float, sys.argv[3].split('x'))) if len(sys.argv) > 3 else None
        frame = tuple(map(float, sys.argv[4].split(','))) if len(sys.argv) > 4 else FIXED
        for pg in map(int, sys.argv[2].split(',')):
            print(pg, json.dumps(read(pdf, pg, size, frame)))
    else:
        main(sys.argv[1], sys.argv[2:])
