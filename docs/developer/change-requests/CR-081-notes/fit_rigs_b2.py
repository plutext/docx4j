# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-2: the other 23 light rigs fitted, each alone, from cr081-phase5b2-r1's fxh1 to fxh3 (the grey
extruded ellipse from five views, warmMatte and matte), with docx4j's own normals (fit_light_model.py's normal maps).
A light may be coloured: its diffuse and highlight strengths are each red, green and blue, and so is the ambient.

    colour_c = min(255, fill_c (A_c + sum_i D_ic max(0, n.L_i)) + 255 ks sum_i S_ic max(0, R_i.v)^p)
with 5b-1's materials (warmMatte ks 1, p 8.02; matte none).

usage: fit_rigs_b2.py <oracle .jsonl.gz> <normal-map dir> <rig> [lights]"""
import os, re, sys
import numpy as np
import fit_light_model as fm

MATS = {'warmMatte': (1.0, 8.02), 'matte': (0.0, 1.0)}

def unpack(q, k):
    A = q[:3] ** 2
    L = q[3:].reshape(k, 8).copy()
    L[:, 2:] = L[:, 2:] ** 2
    return A, L

def shade(q, k, pr):
    A, L = unpack(q, k)
    ks, p = MATS[pr['material']]
    n, v = pr['n'], pr['view']
    diff = np.tile(A, (len(n), 1))
    spec = np.zeros((len(n), 3))
    for pol, az, dr, dg, db, sr, sg, sb in L:
        l = fm.lights(pol, az, pr['turn'])
        nl = n @ l
        diff += np.maximum(0, nl)[:, None] * np.array([dr, dg, db])[None, :]
        if ks > 0:
            rv = 2 * nl * (n @ v) - l @ v
            spec += (np.where(nl > 0, np.maximum(0, rv), 0) ** p)[:, None] * np.array([sr, sg, sb])[None, :]
    return np.minimum(255, pr['fill'][None, :] * diff + 255 * ks * spec)

def residuals(q, k, d):
    return np.concatenate([(shade(q, k, pr) - pr['c']).ravel() / np.sqrt(len(pr['c'])) for pr in d])

def lm(q, k, d, iters):
    lam = 1e-2
    r = residuals(q, k, d)
    cost = r @ r
    for it in range(iters):
        J = np.empty((len(r), len(q)))
        for j in range(len(q)):
            dq = np.zeros(len(q)); dq[j] = 1e-5 * max(1, abs(q[j]))
            J[:, j] = (residuals(q + dq, k, d) - r) / dq[j]
        g, H = J.T @ r, J.T @ J
        while True:
            q2 = q + np.linalg.solve(H + lam * np.diag(np.diag(H) + 1e-9), -g)
            r2 = residuals(q2, k, d)
            if r2 @ r2 < cost:
                q, r, cost, lam = q2, r2, r2 @ r2, lam / 3
                break
            lam *= 4
            if lam > 1e8:
                return q, cost
    return q, cost

def start(k, rng):
    q = list(np.sqrt([0.3, 0.3, 0.3]))
    for i in range(k):
        d = rng.uniform(0.3, 0.7)
        s = rng.uniform(0.1, 0.4)
        q += [rng.uniform(0.2, 2.8), rng.uniform(-np.pi, np.pi)] + [np.sqrt(d)] * 3 + [np.sqrt(s)] * 3
    return np.array(q)

def report(rig, q, k, d):
    A, L = unpack(q, k)
    print(f'{rig}: ambient ({A[0]:.4f}, {A[1]:.4f}, {A[2]:.4f})')
    for pol, az, dr, dg, db, sr, sg, sb in L:
        l = fm.lights(pol, az, 0)
        print(f'    light ({l[0]:+.4f}, {l[1]:+.4f}, {l[2]:+.4f}) diffuse ({dr:.4f}, {dg:.4f}, {db:.4f}) highlight ({sr:.4f}, {sg:.4f}, {sb:.4f})')
    es = []
    for pr in d:
        e = np.abs(shade(q, k, pr) - pr['c']).mean()
        es.append(e)
        print(f'  {pr["probe"]} {pr["note"][:50]:50s} {e:5.2f} / 255')
    print(f'  {rig} mean {np.mean(es):.2f}, worst {np.max(es):.2f}')

if __name__ == '__main__':
    oracle, dump, rig = sys.argv[1:4]
    k = int(sys.argv[4]) if len(sys.argv) > 4 else 4
    d = [pr for pr in fm.probes(oracle, dump) if pr['rig'] == rig and pr['probe'].startswith(('fxh1', 'fxh2', 'fxh3'))]
    sub = [dict(pr, n=pr['n'][::4], c=pr['c'][::4]) for pr in d]
    rng = np.random.default_rng(7)
    best = None
    for s in range(int(os.environ.get('STARTS', '6'))):
        q, cost = lm(start(k, rng), k, sub, 40)
        if best is None or cost < best[1]:
            best = (q, cost)
    q, cost = lm(best[0], k, d, 10)
    print('q =', '[' + ', '.join(f'{v:.6f}' for v in q) + ']')
    report(rig, q, k, d)
