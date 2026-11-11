# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a, round 2: the cameras with 36 pt of depth (fxd1 to fxd3). The silhouette of the extruded frame is
the convex hull of its eight corners projected; each preset's candidates (the fitted turn of round 1 and its mirror;
for an oblique, the shear) are scored by the rms distance of Word's mask crossings to that hull.

usage: fit_depth.py <results dir>"""
import csv, os, re, sys
import numpy as np
import read_3d as r3
import fit_3d as f3

HERE = os.path.dirname(os.path.abspath(__file__))
W, H, DEPTH = 144.0, 108.0, 36.0

def crossings_pt(pdf, page):
    d = r3.the_picture(pdf, page)
    _, a = r3.pd.pixels(pdf, d['obj'])
    x0, y0, x1, y1 = d['box']
    h, w = a.shape
    sx, sy = (x1 - x0) / w, (y1 - y0) / h
    pts = r3.crossings(a)
    cx, cy = r3.FIXED[0] + W / 2, r3.FIXED[1] + H / 2
    top = r3.PAGE_H - y1
    return np.array([(x0 + px * sx - cx, top + py * sy - cy) for px, py in pts]), a

def hull(pts):
    return np.array(r3.hull([tuple(p) for p in pts]))

def dist_to_poly(pts, poly):
    """Each point's distance to the polygon's boundary."""
    best = np.full(len(pts), np.inf)
    for i in range(len(poly)):
        a, b = poly[i], poly[(i + 1) % len(poly)]
        ab = b - a
        t = np.clip(((pts - a) @ ab) / max(ab @ ab, 1e-12), 0, 1)
        q = a + t[:, None] * ab
        best = np.minimum(best, np.hypot(*(pts - q).T))
    return best

def box(model):
    """The extruded frame's eight corners projected by model(x, y, z) -> (X, Y)."""
    return np.array([model(x, y, z) for z in (0.0, -DEPTH) for x in (-W / 2, W / 2) for y in (-H / 2, H / 2)])

def turn(lat, lon, rev, persp=False, fov=45.0, C=f3.C if hasattr(f3, 'C') else 452.85, K=1.0011, ox=0.0, oy=0.0):
    R = np.pi / 180
    def m(x, y, z):
        x1, z1 = x * np.cos(lon * R) - z * np.sin(lon * R), x * np.sin(lon * R) + z * np.cos(lon * R)
        y2, z2 = y * np.cos(lat * R) + z1 * np.sin(lat * R), -y * np.sin(lat * R) + z1 * np.cos(lat * R)
        X, Y = x1 * np.cos(rev * R) + y2 * np.sin(rev * R), -x1 * np.sin(rev * R) + y2 * np.cos(rev * R)
        if persp:
            D = C / np.tan(fov * R / 2)
            s = K * D / (D - z2)
            X, Y = X * s, Y * s
        return (X - z * ox, Y - z * oy)
    return m

def score(pts, model):
    return float(np.sqrt(np.mean(dist_to_poly(pts, hull(box(model))) ** 2)))

def candidates(preset, row):
    lat, lon, rev, fov = (float(row[k]) for k in ('lat', 'lon', 'rev', 'fov'))
    persp = row['projection'] == 'perspective'
    out = {}
    for sl, sn, sr in ((1, 1, 1), (-1, -1, 1), (1, -1, -1), (-1, 1, -1)):
        # the same flat picture: (lat, lon, rev), and the turns that mirror it front to back
        out[f'lat {sl * lat:+.2f} lon {sn * lon:+.2f} rev {sr * rev:+.2f}'] = (sl * lat, sn * lon, sr * rev)
    return persp, fov, out

STARTS = [(a, b) for a in (-0.3, 0.0, 0.3) for b in (-0.3, 0.0, 0.3)]

def main(results):
    probes = [p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t') if p['set'] == 'cr081-phase5a-r2'
              and p['probe'].startswith(('fxd1', 'fxd2', 'fxd3'))]
    table = {r['preset']: r for r in csv.DictReader((l for l in open(os.path.join(HERE, 'phase5a-cameras.tsv')) if not l.startswith('#')),
                                                    delimiter='\t')}
    out = ['preset\tchoice\tlat\tlon\trev\tfov\tox\toy\trms_pt\tnext_rms_pt\n']
    for p in probes:
        name = re.match(r'camera (\w+),', p['note']).group(1)
        pdf = os.path.join(results, f"{p['file']}-a", f"{p['file']}.pdf")
        pts, _ = crossings_pt(pdf, int(p['page']))
        row = table[name]
        persp, fov, cands = candidates(name, row)
        scored = []
        if 'blique' in name:
            # the shear: fitted, its factor and direction free
            def f(q):
                return dist_to_poly(pts, hull(box(turn(0, 0, 0, ox=q[0], oy=q[1]))))
            best = None
            for start in STARTS:
                q, r = f3.lm(f, list(start))
                e = float(np.sqrt(np.mean(r ** 2)))
                if best is None or e < best[0]:
                    best = (e, q)
            scored.append((best[0], 'oblique', (0.0, 0.0, 0.0), best[1]))
        elif name.startswith('legacyPerspective'):
            # a perspective whose camera is moved towards the named side: fitted as a shear plus perspective
            def f(q):
                return dist_to_poly(pts, hull(box(turn(0, 0, 0, True, 45.0, ox=q[0], oy=q[1]))))
            best = None
            for start in STARTS:
                q, r = f3.lm(f, list(start))
                e = float(np.sqrt(np.mean(r ** 2)))
                if best is None or e < best[0]:
                    best = (e, q)
            scored.append((best[0], 'perspective + shear', (0.0, 0.0, 0.0), best[1]))
        else:
            for label, (a, b, c) in cands.items():
                scored.append((score(pts, turn(a, b, c, persp, fov)), label, (a, b, c), (0.0, 0.0)))
        scored.sort(key=lambda t: t[0])
        e, label, (a, b, c), q = scored[0]
        nxt = scored[1][0] if len(scored) > 1 else float('nan')
        print(f'{name:36s} {label:40s} rms {e:6.3f}  next {nxt:6.3f}  shear {q[0]:+.4f} {q[1]:+.4f}')
        out.append(f'{name}\t{label}\t{a:.3f}\t{b:.3f}\t{c:.3f}\t{fov:.3f}\t{q[0]:.4f}\t{q[1]:.4f}\t{e:.3f}\t{nxt:.3f}\n')
    open(os.path.join(HERE, 'phase5a-cameras-depth.tsv'), 'w', newline='').writelines(out)

if __name__ == '__main__':
    main(sys.argv[1])
