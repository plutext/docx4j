# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a: the camera fitted to Word's 3-D pictures (phase5a-word.tsv, from read_3d.py).

The model, as the round reads (phase5a-findings.md):
- the frame's corners (x, y, 0), points from its centre, y down, z towards the viewer;
- turned by lon about the vertical axis (x' = x cos lon - z sin lon, z' = x sin lon + z cos lon), then by lat about
  the horizontal (y'' = y cos lat + z' sin lat, z'' = -y sin lat + z' cos lat), then by rev in the picture's plane,
  counter-clockwise (X = x cos rev + y sin rev, Y = -x sin rev + y cos rev);
- orthographic: (X, Y) as they are; perspective: scaled by D / (D - z), D = C / tan(fov / 2), then by K.

Fits C and K on fxc8 (the rot family, its angles known), then each camera preset's lat, lon, rev (and fov, for a
perspective one) on its two shapes (fxc1-3 at 144 by 108 pt, fxc4-6 at 72 by 144 pt) together.

usage: fit_3d.py"""
import csv, os, re
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
R = np.pi / 180

def rows():
    return list(csv.DictReader(open(os.path.join(HERE, 'phase5a-word.tsv')), delimiter='\t'))

def corners_of(r):
    return np.array([[float(r[f'x{i}']), float(r[f'y{i}'])] for i in range(4)])

def project(w, h, lat, lon, rev, persp=False, fov=45.0, C=450.0, K=1.0, z0=0.0, zoom=1.0):
    """The frame's four corners, clockwise from the top left (before turning), projected."""
    pts = np.array([[-w / 2, -h / 2], [w / 2, -h / 2], [w / 2, h / 2], [-w / 2, h / 2]])
    out = []
    for x, y in pts:
        z = z0
        x1, z1 = x * np.cos(lon * R) - z * np.sin(lon * R), x * np.sin(lon * R) + z * np.cos(lon * R)
        y2, z2 = y * np.cos(lat * R) + z1 * np.sin(lat * R), -y * np.sin(lat * R) + z1 * np.cos(lat * R)
        X, Y = x1 * np.cos(rev * R) + y2 * np.sin(rev * R), -x1 * np.sin(rev * R) + y2 * np.cos(rev * R)
        if persp:
            D = C / np.tan(fov * R / 2)
            s = K * D / (D - z2)
            X, Y = X * s, Y * s
        out.append((X * zoom, Y * zoom))
    return np.array(out)

def match(pred, meas):
    """The smallest summed squared distance over the four rotations of the corner order (read_3d orders the measured
    corners from the top-leftmost, which after a turn needn't be the frame's top left)."""
    best = None
    for k in range(4):
        d = np.roll(pred, k, axis=0) - meas
        e = float((d ** 2).sum())
        if best is None or e < best[0]:
            best = (e, d)
    return best

def lm(f, p0, steps=200):
    """Levenberg-Marquardt on f(p) -> residual vector."""
    p = np.array(p0, float)
    lam = 1e-3
    r = f(p)
    for _ in range(steps):
        J = np.empty((len(r), len(p)))
        for j in range(len(p)):
            dp = np.zeros_like(p); dp[j] = 1e-5 * max(1.0, abs(p[j]))
            J[:, j] = (f(p + dp) - r) / dp[j]
        A = J.T @ J
        g = J.T @ r
        while True:
            step = np.linalg.solve(A + lam * np.diag(np.diag(A) + 1e-9), -g)
            r2 = f(p + step)
            if r2 @ r2 < r @ r:
                p, r, lam = p + step, r2, lam / 3
                break
            lam *= 4
            if lam > 1e12:
                return p, r
        if np.abs(step).max() < 1e-9:
            break
    return p, r

def resid(pairs):
    """pairs: list of (predicted corners, measured corners) -> residual vector, the best order each."""
    return np.concatenate([match(p, m)[1].ravel() for p, m in pairs])

ROT = re.compile(r'rot lat (\d+) lon (\d+) rev (\d+)')

def fit_globals(rs):
    """C and K from the perspective probes of fxc8 whose angles are known."""
    probes = []
    for r in rs:
        if not r['probe'].startswith('fxc8') or 'perspectiveFront' not in r['note']:
            continue
        m = ROT.search(r['note'])
        fov = 45.0
        f = re.search(r'fov (\d+)', r['note'])
        if f:
            fov = float(f.group(1))
            lat, lon, rev = 0, 30, 0
        elif m:
            lat, lon, rev = map(float, m.groups())
        else:
            continue
        probes.append((float(r['w']), float(r['h']), lat, lon, rev, fov, corners_of(r)))
    front = [r for r in rs if r['note'] == 'camera perspectiveFront']
    for r in front:
        probes.append((float(r['w']), float(r['h']), 0, 0, 0, 45.0, corners_of(r)))
    def f(p):
        C, K = p
        return resid([(project(w, h, a, b, c, True, fov, C, K), m) for w, h, a, b, c, fov, m in probes])
    p, r = lm(f, [450.0, 1.0])
    return p, float(np.sqrt(np.mean(r ** 2))), len(probes)

def fit_preset(rs, name, C, K, persp):
    shots = [(float(r['w']), float(r['h']), corners_of(r)) for r in rs if r['note'] == f'camera {name}']
    best = None
    for start in [(a, b, c) for a in (-40, 0, 40) for b in (-50, 0, 50) for c in (-40, 0, 40)]:
        if persp:
            def f(p):
                return resid([(project(w, h, p[0], p[1], p[2], True, p[3], C, K), m) for w, h, m in shots])
            p, r = lm(f, list(start) + [45.0], 80)
        else:
            def f(p):
                return resid([(project(w, h, p[0], p[1], p[2]), m) for w, h, m in shots])
            p, r = lm(f, list(start), 80)
        e = float(np.sqrt(np.mean(r ** 2)))
        if best is None or e < best[1]:
            best = (p, e)
    return best

if __name__ == '__main__':
    rs = rows()
    (C, K), e, n = fit_globals(rs)
    print(f'globals from {n} perspective probes: C {C:.2f} pt, K {K:.5f}, rms {e:.3f} pt')
    # the orthographic rot family against the model, angles as written
    for r in rs:
        if r['probe'].startswith('fxc8') and 'orthographicFront' in r['note'] and ROT.search(r['note']):
            lat, lon, rev = map(float, ROT.search(r['note']).groups())
            e = np.sqrt(match(project(float(r['w']), float(r['h']), lat, lon, rev), corners_of(r))[0] / 8)
            print(f"  {r['probe']} {r['note']:44s} rms {e:.3f} pt")
    names = []
    for r in rs:
        m = re.match(r'camera (\w+)$', r['note'])
        if m and m.group(1) not in names:
            names.append(m.group(1))
    print('preset                               lat      lon      rev      fov    rms pt')
    table = [f'# fit_3d.py on phase5a-word.tsv: C {C:.3f} pt, K {K:.6f}; flat shapes only, so a view and its mirror are one\n',
             'preset\tprojection\tlat\tlon\trev\tfov\trms_pt\n']
    for name in names:
        persp = 'erspective' in name
        p, e = fit_preset(rs, name, C, K, persp)
        lat, lon, rev = [((v + 180) % 360) - 180 for v in p[:3]]
        fov = abs(p[3]) if persp else 0.0
        print(f'{name:36s} {lat:8.3f} {lon:8.3f} {rev:8.3f} {fov:8.3f} {e:8.3f}')
        table.append(f"{name}\t{'perspective' if persp else 'parallel'}\t{lat:.3f}\t{lon:.3f}\t{rev:.3f}\t{fov:.3f}\t{e:.3f}\n")
    open(os.path.join(HERE, 'phase5a-cameras.tsv'), 'w', newline='').writelines(table)
