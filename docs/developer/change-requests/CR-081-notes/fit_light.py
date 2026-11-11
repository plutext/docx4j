# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-1: a light rig's lights and the materials fitted to Word's pictures (read_light.py's samples).

The model, per pixel with normal n, the view v = (0, 0, 1), the fill's colour c (0..255):
    colour = min(255, c * kd_m * (A + sum_i D_i max(0, n.L_i)) + ks_m * sum_i S_i max(0, n.H_i)^p_m)
with H_i the half vector of L_i and v. A rig's lights (L_i, D_i, S_i) and its ambient A are shared by its materials;
a material's (kd, ks, p) by the rigs. matte: kd 1, ks 0.

usage: fit_light.py <results dir> <rig> <lights>"""
import os, sys
import numpy as np
import read_light as rl

HERE = os.path.dirname(os.path.abspath(__file__))
PAGES = {('twoPt', 'warmMatte'): 1, ('twoPt', 'matte'): 2, ('twoPt', 'plastic'): 3,
         ('threePt', 'warmMatte'): 4, ('threePt', 'matte'): 5, ('threePt', 'plastic'): 6,
         ('contrasting', 'warmMatte'): 7, ('contrasting', 'matte'): 8, ('contrasting', 'plastic'): 9,
         ('soft', 'warmMatte'): 10, ('soft', 'matte'): 11, ('soft', 'plastic'): 12}
MATERIALS = ('matte', 'warmMatte', 'plastic')

def picture(pdf, page, step, geom):
    rgb, a, box, filt = rl.colours(pdf, page)
    h, w = a.shape
    xs = np.arange(1.2, rl.W - 1.2, step)
    ys = np.arange(1.2, rl.H - 1.2, step)
    X, Y = np.meshgrid(xs, ys)
    if geom == 'rect':
        n, d = rl.normals(xs, ys)
        dd = np.sort(np.stack([X, rl.W - X, Y, rl.H - Y]), axis=0)
        keep = (dd[1] - dd[0] > 1.0) | (dd[0] > rl.BW + 1)       # off the mitres, where the normal turns
    else:
        n, d, keep = rl.ellipse_normals(xs, ys)
        keep &= d > 1.2
    ix = np.clip(np.floor((X - box[0]) / (box[2] - box[0]) * w).astype(int), 0, w - 1)
    iy = np.clip(np.floor((Y - box[1]) / (box[3] - box[1]) * h).astype(int), 0, h - 1)
    c = rgb[iy, ix] * 255
    keep &= a[iy, ix] > 0.99
    return n[keep], c[keep], filt

ELLIPSE = dict(PAGES)
ELLIPSE.update({('threePt', 'warmMatte', 'blue'): 13, ('threePt', 'plastic', 'blue'): 14, ('twoPt', 'warmMatte', 'blue'): 15})
BLUE = np.array([0x44, 0x72, 0xC4], float)

def data(results, rig, step=0.72, sets=('rect', 'ellipse')):
    """(material, normals (N, 3), the fill (3), Word's colours (N, 3)): the rectangle's and the ellipse's pixels"""
    out = []
    for geom in sets:
        pdf = (os.path.join(results, 'fxg1-light-a', 'fxg1-light.pdf') if geom == 'rect'
               else os.path.join(results.replace('word1', 'word2'), 'fxg2-light-ellipse-a', 'fxg2-light-ellipse.pdf'))
        for key, page in (PAGES if geom == 'rect' else ELLIPSE).items():
            if key[0] != rig: continue
            n, c, filt = picture(pdf, page, step, geom)
            fill = BLUE if len(key) == 3 else np.array([128.0] * 3)
            out.append((key[1], n, fill, c, f'{geom} {key[1]}{" blue" if len(key) == 3 else ""} ({filt[:4]})'))
    return out

def unpack(q, k):
    A = q[0] ** 2
    lights = q[1:1 + 4 * k].reshape(k, 4).copy()          # polar, azimuth (radians), D, S
    lights[:, 2:] = lights[:, 2:] ** 2
    m = q[1 + 4 * k:]
    mats = {'matte': (1.0, 0.0, 1.0), 'warmMatte': (m[0], m[1] ** 2, m[2]), 'plastic': (m[3], m[4] ** 2, m[5])}
    return A, lights, mats

def shade(q, k, n, m, fill):
    """(N, 3) colours"""
    A, lights, mats = unpack(q, k)
    kd, ks, p = mats[m]
    diff = np.full(len(n), A)
    spec = np.zeros(len(n))
    for pol, az, D, S in lights:
        L = np.array([np.sin(pol) * np.cos(az), np.sin(pol) * np.sin(az), np.cos(pol)])
        Hv = L + [0, 0, 1]
        Hv /= max(1e-9, np.linalg.norm(Hv))
        diff += D * np.maximum(0, n @ L)
        spec += S * np.maximum(0, n @ Hv) ** max(1.0, p)
    return np.minimum(255, fill[None, :] * kd * diff[:, None] + (ks * spec * 255)[:, None])

def residuals(q, k, d):
    return np.concatenate([(shade(q, k, n, m, f) - c).ravel() for m, n, f, c, _ in d])

def lm(q, k, d, iters=60):
    lam = 1e-2
    r = residuals(q, k, d)
    cost = r @ r
    for _ in range(iters):
        J = np.empty((len(r), len(q)))
        for j in range(len(q)):
            dq = np.zeros(len(q)); dq[j] = 1e-5 * max(1, abs(q[j]))
            J[:, j] = (residuals(q + dq, k, d) - r) / dq[j]
        g = J.T @ r
        Hm = J.T @ J
        while True:
            step = np.linalg.solve(Hm + lam * np.diag(np.diag(Hm) + 1e-9), -g)
            q2 = q + step
            r2 = residuals(q2, k, d)
            if r2 @ r2 < cost:
                q, r, cost, lam = q2, r2, r2 @ r2, lam / 3
                break
            lam *= 4
            if lam > 1e8:
                return q, cost
    return q, cost

def fit(results, rig, k, starts=8, seed=1):
    """Several starts on a coarse sampling, the best refined on a fine one"""
    coarse = data(results, rig, step=2.0)
    rng = np.random.default_rng(seed)
    best = None
    for s in range(starts):
        q = [0.5]
        for i in range(k):
            q += [rng.uniform(0.2, 1.4), rng.uniform(-np.pi, np.pi), rng.uniform(0.3, 0.8), rng.uniform(0.0, 0.7)]
        q += [1.0, 0.55, 30.0, 1.0, 0.75, 45.0]
        q, cost = lm(np.array(q), k, coarse, iters=40)
        if best is None or cost < best[1]:
            best = (q, cost)
    fine = data(results, rig, step=0.9)
    q, cost = lm(best[0], k, fine, iters=15)
    return q, fine

def report(q, k, d):
    A, lights, mats = unpack(q, k)
    print(f'  ambient {A:.3f}')
    for pol, az, D, S in lights:
        print(f'  light: from {np.degrees(pol):6.1f} deg off the view, azimuth {np.degrees(az) % 360:6.1f} (0 right, 90 down), diffuse {D:.3f}, specular {S:.3f}')
    for m, v in mats.items():
        print(f'  {m}: kd {v[0]:.3f} ks {v[1]:.3f} p {v[2]:.1f}')
    for m, n, f, c, label in d:
        e = np.abs(shade(q, k, n, m, f) - c)
        print(f'  {label:28s} mean error {e.mean():.2f}/255, 95% within {np.percentile(e, 95):.1f}, worst {e.max():.0f}')

if __name__ == '__main__':
    k = int(sys.argv[3])
    q, d = fit(sys.argv[1], sys.argv[2], k)
    print(sys.argv[2], k, 'lights;', 'q =', '[' + ', '.join(f'{v:.5f}' for v in q) + ']')
    report(q, k, d)
