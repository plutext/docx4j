# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-1: the four rigs fitted together, the materials shared (fit_light.py fits a rig alone).

    colour = min(255, c * (A + sum_i D_i max(0, n.L_i)) + 255 ks_m sum_i S_i max(0, n.H_i)^p_m)
matte ks 0; warmMatte ks 1 (its scale is the lights' S); plastic ks and both exponents fitted.

usage: fit_light_joint.py <results dir> <fits dir>   (the fits dir: fit_light.py's <rig>-3.txt, the starting point)"""
import os, re, sys
import numpy as np
import fit_light as fl

RIGS = ('twoPt', 'threePt', 'contrasting', 'soft')
K = 3

def unpack(q):
    pw, pp, kp = q[0], q[1], q[2] ** 2
    rigs = {}
    for j, r in enumerate(RIGS):
        b = q[3 + j * (1 + 4 * K):3 + (j + 1) * (1 + 4 * K)]
        L = b[1:].reshape(K, 4).copy()
        L[:, 2:] = L[:, 2:] ** 2
        rigs[r] = (b[0] ** 2, L)
    return {'matte': (0.0, 1.0), 'warmMatte': (1.0, pw), 'plastic': (kp, pp)}, rigs

def shade(q, rig, n, m, fill):
    mats, rigs = unpack(q)
    ks, p = mats[m]
    A, lights = rigs[rig]
    diff = np.full(len(n), A)
    spec = np.zeros(len(n))
    for pol, az, D, S in lights:
        L = np.array([np.sin(pol) * np.cos(az), np.sin(pol) * np.sin(az), np.cos(pol)])
        Hv = L + [0, 0, 1]
        Hv /= max(1e-9, np.linalg.norm(Hv))
        diff += D * np.maximum(0, n @ L)
        spec += S * np.maximum(0, n @ Hv) ** max(1.0, p)
    return np.minimum(255, fill[None, :] * diff[:, None] + (ks * spec * 255)[:, None])

def residuals(q, d):
    return np.concatenate([(shade(q, rig, n, m, f) - c).ravel() for rig, (m, n, f, c, _) in d])

def lm(q, d, iters):
    lam = 1e-2
    r = residuals(q, d)
    cost = r @ r
    for it in range(iters):
        J = np.empty((len(r), len(q)))
        for j in range(len(q)):
            dq = np.zeros(len(q)); dq[j] = 1e-5 * max(1, abs(q[j]))
            J[:, j] = (residuals(q + dq, d) - r) / dq[j]
        g, Hm = J.T @ r, J.T @ J
        while True:
            q2 = q + np.linalg.solve(Hm + lam * np.diag(np.diag(Hm) + 1e-9), -g)
            r2 = residuals(q2, d)
            if r2 @ r2 < cost:
                q, r, cost, lam = q2, r2, r2 @ r2, lam / 3
                break
            lam *= 4
            if lam > 1e8:
                return q
    return q

def start(fits):
    q = [32.0, 46.0, np.sqrt(1.95)]
    for r in RIGS:
        v = np.array([float(x) for x in re.search(r'q = \[([^\]]*)\]', open(os.path.join(fits, f'{r}-3.txt')).read()).group(1).split(',')])
        ksw = v[1 + 4 * K + 1] ** 2
        b = v[:1 + 4 * K].copy()
        for i in range(K):
            b[1 + 4 * i + 3] = np.sqrt(b[1 + 4 * i + 3] ** 2 * ksw)
        q += list(b)
    return np.array(q)

def data(results, step):
    return [(r, x) for r in RIGS for x in fl.data(results, r, step=step)]

def report(q, d):
    mats, rigs = unpack(q)
    for m, (ks, p) in mats.items():
        print(f'  {m}: ks {ks:.3f} p {p:.1f}')
    for r, (A, lights) in rigs.items():
        print(f'  {r}: ambient {A:.4f}')
        for pol, az, D, S in lights:
            L = [np.sin(pol) * np.cos(az), np.sin(pol) * np.sin(az), np.cos(pol)]
            print(f'    light ({L[0]:+.4f}, {L[1]:+.4f}, {L[2]:+.4f}) diffuse {D:.4f} specular {S:.4f}')
    for rig, (m, n, f, c, label) in d:
        e = np.abs(shade(q, rig, n, m, f) - c)
        print(f'  {rig:12s} {label:28s} mean {e.mean():.2f}/255, 95% within {np.percentile(e, 95):.1f}, worst {e.max():.0f}')

if __name__ == '__main__':
    q = start(sys.argv[2])
    q = lm(q, data(sys.argv[1], 2.0), 30)
    d = data(sys.argv[1], 0.9)
    q = lm(q, d, 8)
    print('q =', '[' + ', '.join(f'{v:.6f}' for v in q) + ']')
    report(q, d)
