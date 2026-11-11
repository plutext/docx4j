# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a, round 3: how a shadow on a 3-D drawing is placed (fxe2). Word's one picture holds the drawing and its
shadow: the drawing is where the alpha is over 0.75, the shadow where it is about one half (black at 50%, unblurred).
For each probe, the visible shadow is compared with three hypotheses, each the shadow's transform (dist along dir; sx,
sy about its algn point) applied to a silhouette, less the drawing:
  silhouette/frame   the 3-D silhouette, its algn point on the frame
  silhouette/bounds  the 3-D silhouette, its algn point on the silhouette's bounds
  frame              the flat frame (the drawing as it is before 3-D)
scored by the visible shadows' overlap (intersection over union).

usage: read_shadow_3d.py <round 3 results dir>"""
import os, re, sys, csv
import numpy as np
import pdfdraws as pd, read_3d as r3

HERE = os.path.dirname(os.path.abspath(__file__))
W, H = 144.0, 108.0
ALIGN = {'tl': (0, 0), 't': (0.5, 0), 'tr': (1, 0), 'l': (0, 0.5), 'ctr': (0.5, 0.5), 'r': (1, 0.5), 'bl': (0, 1), 'b': (0.5, 1),
         'br': (1, 1)}

def grid(pdf, page):
    d = r3.the_picture(pdf, page)
    _, a = pd.pixels(pdf, d['obj'])
    x0, y0, x1, y1 = d['box']
    h, w = a.shape
    sx, sy = (x1 - x0) / w, (y1 - y0) / h
    xs = x0 + (np.arange(w) + 0.5) * sx - r3.FIXED[0]          # points from the frame's top left
    ys = (r3.PAGE_H - y1) + (np.arange(h) + 0.5) * sy - r3.FIXED[1]
    return a, xs, ys

def transformed(mask, xs, ys, dist, direc, sx, sy, anchor):
    """The mask under the shadow's transform: q = anchor + S (p - anchor) + d; sampled back (q -> p)."""
    dx, dy = dist * np.cos(np.radians(direc)), dist * np.sin(np.radians(direc))
    X, Y = np.meshgrid(xs, ys)
    px = anchor[0] + (X - dx - anchor[0]) / sx
    py = anchor[1] + (Y - dy - anchor[1]) / sy
    step_x, step_y = xs[1] - xs[0], ys[1] - ys[0]
    ix = np.round((px - xs[0]) / step_x).astype(int)
    iy = np.round((py - ys[0]) / step_y).astype(int)
    ok = (ix >= 0) & (ix < len(xs)) & (iy >= 0) & (iy < len(ys))
    out = np.zeros_like(mask)
    out[ok] = mask[iy[ok], ix[ok]]
    return out

def iou(a, b):
    u = (a | b).sum()
    return float((a & b).sum()) / u if u else 1.0

def main(results):
    probes = [p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t') if p['probe'].startswith('fxe2')]
    pdf = os.path.join(results, 'fxe2-shadow-3d-a', 'fxe2-shadow-3d.pdf')
    for p in probes:
        page = int(p['page'])
        m = re.search(r'outerShdw blurRad="(\d+)" dist="(\d+)" dir="(\d+)" algn="(\w+)" sx="(\d+)" sy="(\d+)"', p['xml'])
        if not m:
            print(f"{p['probe']} {p['note']}: no shadow")
            continue
        blur, dist, direc, algn, sx, sy = m.groups()
        dist, direc, sx, sy = int(dist) / 12700, int(direc) / 60000, int(sx) / 100000, int(sy) / 100000
        d = r3.the_picture(pdf, page)
        if d is None or not pd.image_info(pdf, d['obj']).get('smask'):
            print(f"{p['probe']} {p['note']}: no 3-D picture (a flat drawing: its shadow drawn apart)")
            continue
        a, xs, ys = grid(pdf, page)
        drawing = a > 0.75
        shadow = (a > 0.3) & (a < 0.7)
        if not drawing.any():
            print(f"{p['probe']} {p['note']}: its largest picture holds no drawing (a flat drawing: its shadow drawn apart)")
            continue
        ys_, xs_ = np.nonzero(drawing)
        bounds = (xs[xs_.min()], ys[ys_.min()], xs[xs_.max()], ys[ys_.max()])
        X, Y = np.meshgrid(xs, ys)
        frame = (X >= 0) & (X <= W) & (Y >= 0) & (Y <= H)
        fx, fy = ALIGN[algn]
        hyp = {'silhouette/frame': (drawing, (fx * W, fy * H)),
               'silhouette/bounds': (drawing, (bounds[0] + fx * (bounds[2] - bounds[0]), bounds[1] + fy * (bounds[3] - bounds[1]))),
               'frame': (frame, (fx * W, fy * H))}
        scores = {k: iou(transformed(mk, xs, ys, dist, direc, sx, sy, an) & ~drawing, shadow) for k, (mk, an) in hyp.items()}
        best = max(scores, key=scores.get)
        print(f"{p['probe']} {p['note'][8:52]:44s} shadow px {int(shadow.sum()):6d}  " +
              '  '.join(f'{k} {v:.3f}' for k, v in scores.items()) + f'  -> {best}')

if __name__ == '__main__':
    main(sys.argv[1])
