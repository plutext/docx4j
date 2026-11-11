# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a: each camera preset refitted on all its readings at once, as Scene3DTest scores them: Word's mask
crossings (round 1's two flat shapes and round 2's shape extruded 36 pt) against the convex hull of the model's
projected box. Starts from phase5a-cameras-depth.tsv (fit_depth.py). A turned preset: lat, lon, rev (and fov, for a
perspective one). An oblique: its shear. A legacy perspective: a camera at distance D, its eye moved by (ex, ey).

usage: fit_all.py <round 1 results dir> <round 2 results dir>   (writes phase5a-presets.tsv)"""
import csv, os, re, sys
import numpy as np
import fit_3d as f3, fit_depth as fd, read_3d as r3

HERE = os.path.dirname(os.path.abspath(__file__))
R = np.pi / 180
K = 1.0011
C = 452.85

def shots(r1, r2, name):
    """(w, h, depth, crossings) for each probe of this preset."""
    out = []
    for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'):
        m = re.match(r'camera (\w+)(, depth 36 pt)?$', p['note'])
        if not m or m.group(1) != name or p['set'] not in ('cr081-phase5a-r1', 'cr081-phase5a-r2'):
            continue
        tall = p['probe'].split('-')[0] in ('fxc4', 'fxc5', 'fxc6')
        w, h = (72.0, 144.0) if tall else (144.0, 108.0)
        res = r1 if p['set'] == 'cr081-phase5a-r1' else r2
        pdf = os.path.join(res, f"{p['file']}-a", f"{p['file']}.pdf")
        d = r3.the_picture(pdf, int(p['page']))
        _, a = r3.pd.pixels(pdf, d['obj'])
        x0, y0, x1, y1 = d['box']
        hh, ww = a.shape
        sx, sy = (x1 - x0) / ww, (y1 - y0) / hh
        cx, cy = r3.FIXED[0] + w / 2, r3.FIXED[1] + h / 2
        top = r3.PAGE_H - y1
        pts = np.array([(x0 + px * sx - cx, top + py * sy - cy) for px, py in r3.crossings(a)])
        out.append((w, h, 36.0 if m.group(2) else 0.0, pts))
    return out

def box(model, w, h, depth):
    zs = (0.0, -depth) if depth else (0.0,)
    return np.array([model(x, y, z) for z in zs for x in (-w / 2, w / 2) for y in (-h / 2, h / 2)])

def turned(lat, lon, rev, persp, fov):
    def m(x, y, z):
        x1, z1 = x * np.cos(lon * R) - z * np.sin(lon * R), x * np.sin(lon * R) + z * np.cos(lon * R)
        y2, z2 = y * np.cos(lat * R) + z1 * np.sin(lat * R), -y * np.sin(lat * R) + z1 * np.cos(lat * R)
        X, Y = x1 * np.cos(rev * R) + y2 * np.sin(rev * R), -x1 * np.sin(rev * R) + y2 * np.cos(rev * R)
        if persp:
            D = C / np.tan(fov * R / 2)
            s = K * D / (D - z2)
            X, Y = X * s, Y * s
        return (X, Y)
    return m

def eye(ex, ey, D):
    """A camera at distance D, its eye moved by (ex, ey): the drawing scaled by K D / (D - z), its depth towards the eye"""
    def m(x, y, z):
        s = D / (D - z)
        return (K * x * s + ex * (1 - s), K * y * s + ey * (1 - s))
    return m

def sheared(ox, oy):
    return lambda x, y, z: (x - z * ox, y - z * oy)

def residual(shots, model):
    return np.concatenate([fd.dist_to_poly(pts, fd.hull(box(model, w, h, d))) for w, h, d, pts in shots])

def main(r1, r2):
    start = list(csv.DictReader(open(os.path.join(HERE, 'phase5a-cameras-depth.tsv')), delimiter='\t'))
    out = ['preset\tkind\ta\tb\tc\td\trms_pt\tmax_pt\n']
    for row in start:
        name = row['preset']
        sh = shots(r1, r2, name)
        if row['choice'] == 'oblique':
            kind, mk, p0 = 'oblique', lambda q: sheared(q[0], q[1]), [float(row['ox']), float(row['oy'])]
        elif row['choice'] == 'perspective + shear':
            kind, mk = 'eye', lambda q: eye(q[0], q[1], q[2])
            p0 = [np.sign(float(row['ox'])) * 174 if abs(float(row['ox'])) > 0.1 else 0.0,
                  np.sign(float(row['oy'])) * 157 if abs(float(row['oy'])) > 0.1 else 0.0, 700.0]
        elif 'erspective' in name:
            kind = 'perspective'
            mk = lambda q: turned(q[0], q[1], q[2], True, q[3])
            fov = float(row['fov'])
            p0 = [float(row['lat']), float(row['lon']), float(row['rev']), 45.0 if fov > 170 or 40 < fov < 50 else fov]
        else:
            kind, mk = 'parallel', lambda q: turned(q[0], q[1], q[2], False, 0)
            p0 = [float(row['lat']), float(row['lon']), float(row['rev'])]
        if name == 'perspectiveFront' or name == 'orthographicFront':
            q = np.array(p0, float)
        else:
            q, _ = f3.lm(lambda q: residual(sh, mk(q)), p0, 150)
        d = residual(sh, mk(q))
        vals = list(q) + [0.0] * (4 - len(q))
        print(f'{name:36s} {kind:12s} ' + ' '.join(f'{v:9.4f}' for v in vals) + f'   rms {np.sqrt(np.mean(d ** 2)):.3f} max {d.max():.3f}')
        out.append(f'{name}\t{kind}\t' + '\t'.join(f'{v:.4f}' for v in vals) + f'\t{np.sqrt(np.mean(d ** 2)):.3f}\t{d.max():.3f}\n')
    open(os.path.join(HERE, 'phase5a-presets.tsv'), 'w', newline='').writelines(out)

if __name__ == '__main__':
    main(*sys.argv[1:])
