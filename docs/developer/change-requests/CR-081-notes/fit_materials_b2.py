# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-2: the other materials fitted, each alone, from cr081-phase5b2-r1's fxh4 (the grey extruded
ellipse under threePt from four views), with docx4j's own normals and 5b-1's threePt lights (phase5b-light.tsv):

    colour = min(255, (c (ka A + kd sum_i D_i max(0, n.L_i)) + 255 kw) max(0, n.v)^ke + 255 ks sum_i S_i max(0, R_i.v)^p
                      + 255 kf (1 - max(0, n.v))^pf)

ka and kd scale the rig's ambient and diffuse light, kw washes towards white, ke darkens towards the edges (n.v the
normal's facing the viewer), ks and p are the highlight's strength and sharpness, kf and pf a rim that brightens
towards the edges, as Word's metal and powder do.

usage: fit_materials_b2.py <oracle .jsonl.gz> <normal-map dir> <material>"""
import os, sys
import numpy as np
import fit_light_model as fm

ROWS = [l.rstrip('\n').split('\t') for l in open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'phase5b-light.tsv'))
        if not l.startswith(('#', 'kind'))]
A3 = float([x for x in ROWS if x[0] == 'ambient' and x[1] == 'threePt'][0][5])
L3 = [(np.array([float(x[2]), float(x[3]), float(x[4])]), float(x[5]), float(x[6])) for x in ROWS if x[0] == 'light' and x[1] == 'threePt']
NAMES = ('ka', 'kd', 'kw', 'ke', 'ks', 'p', 'kf', 'pf')
BLINN = os.environ.get('HIGHLIGHT') == 'blinn'   # Blinn's half vector in place of Phong's reflection

def unpack(q):
    ka, kd, kw, ke, ks, p, kf, pf = q
    return ka ** 2, kd ** 2, kw, ke ** 2, ks ** 2, max(1.0, p), kf ** 2, max(0.5, pf)

def shade(q, pr):
    ka, kd, kw, ke, ks, p, kf, pf = unpack(q)
    n, v = pr['n'], pr['view']
    diff = np.full(len(n), ka * A3)
    spec = np.zeros(len(n))
    for l, D, S in L3:
        l = np.array([l[0] * np.cos(pr['turn']) - l[1] * np.sin(pr['turn']), l[0] * np.sin(pr['turn']) + l[1] * np.cos(pr['turn']), l[2]])
        nl = n @ l
        diff += kd * D * np.maximum(0, nl)
        if BLINN:
            h = (l + v) / np.linalg.norm(l + v)
            rv = n @ h
        else:
            rv = 2 * nl * (n @ v) - l @ v
        spec += S * np.where(nl > 0, np.maximum(0, rv), 0) ** p
    edge = np.maximum(0, n @ v) ** ke
    base = (pr['fill'][None, :] * diff[:, None] + 255 * kw) * edge[:, None]
    rim = kf * (1 - np.maximum(0, n @ v)) ** pf
    return np.minimum(255, base + (255 * (ks * spec + rim))[:, None])

def residuals(q, d):
    return np.concatenate([(shade(q, pr) - pr['c']).ravel() / np.sqrt(len(pr['c'])) for pr in d])

def lm(q, d, iters):
    lam = 1e-2
    r = residuals(q, d)
    cost = r @ r
    for it in range(iters):
        J = np.empty((len(r), len(q)))
        for j in range(len(q)):
            dq = np.zeros(len(q)); dq[j] = 1e-5 * max(1, abs(q[j]))
            J[:, j] = (residuals(q + dq, d) - r) / dq[j]
        g, H = J.T @ r, J.T @ J
        while True:
            q2 = q + np.linalg.solve(H + lam * np.diag(np.diag(H) + 1e-9), -g)
            r2 = residuals(q2, d)
            if r2 @ r2 < cost:
                q, r, cost, lam = q2, r2, r2 @ r2, lam / 3
                break
            lam *= 4
            if lam > 1e8:
                return q, cost
    return q, cost

if __name__ == '__main__':
    oracle, dump, mat = sys.argv[1:4]
    d = [pr for pr in fm.probes(oracle, dump) if pr['probe'].startswith('fxh4') and pr['material'] == mat]
    sub = [dict(pr, n=pr['n'][::3], c=pr['c'][::3]) for pr in d]
    best = None
    for q0 in ([1, 1, 0, 0.01, 1, 8, 0.01, 2], [1, 1, 0.1, 0.5, 0.5, 20, 0.5, 3], [0.7, 0.7, 0, 0.7, 1.4, 4, 0.7, 1.5],
               [1.2, 0.5, 0.3, 0.01, 0.3, 40, 0.3, 5]):
        q, cost = lm(np.array(q0, float), sub, 50)
        if best is None or cost < best[1]:
            best = (q, cost)
    q, cost = lm(best[0], d, 10)
    print(mat, ' '.join(f'{k} {v:.4f}' for k, v in zip(NAMES, unpack(q))))
    es = []
    for pr in d:
        e = np.abs(shade(q, pr) - pr['c']).mean()
        es.append(e)
        print(f'  {pr["probe"]} {pr["note"][:50]:50s} {e:5.2f} / 255 over {len(pr["c"])}')
    print(f'  {mat} mean {np.mean(es):.2f}, worst {np.max(es):.2f}')
