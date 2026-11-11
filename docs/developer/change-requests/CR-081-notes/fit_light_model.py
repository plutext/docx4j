# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-1: the rigs' lights fitted in the drawing's own space, from every probe of rounds 1 to 3: Word's
colours (export_3d_light.py's oracle) against docx4j's own geometry, the normal at each point as Scene3D draws it (its
normal map: Scene3DTest.theShadingIsWords, with normals-hook.patch applied, run with SCENE3D_DEBUG=normals
SCENE3D_DUMP=<dir>, each probe's <probe>.png and its place and view's direction in <probe>.txt).

    colour = min(255, c (A + sum_i D_i max(0, n.L_i)) + 255 ks_m sum_i S_i max(0, n.H_i)^p_m), H_i = |L_i + v|
n, L and v in the drawing's space; a rig's lights turned about its z by dir (tl -45 degrees) and rev (clockwise).

usage: fit_light_model.py <oracle .jsonl.gz> <dump dir> <lights a rig> [start .tsv]"""
import gzip, json, os, re, sys
import numpy as np
from PIL import Image

RIGS = ('twoPt', 'threePt', 'contrasting', 'soft')
DIRS = {'t': 0, 'tl': -45, 'l': -90, 'bl': -135, 'b': 180, 'br': 135, 'r': 90, 'tr': 45}

def probes(oracle, dump):
    out = []
    for line in gzip.open(oracle, 'rt'):
        p = json.loads(line)
        if not p['shape']:
            continue                          # the photo: its colours vary
        f = os.path.join(dump, p['probe'] + '.png')
        if not os.path.exists(f):
            continue
        nm = np.asarray(Image.open(f).convert('RGBA'), float)
        v = [float(x) for x in open(os.path.join(dump, p['probe'] + '.txt')).read().split()]
        px, py, pw, ph, view = v[0], v[1], v[2], v[3], np.array(v[4:7])
        w = p['word']
        H, W = nm.shape[:2]
        ns, cs = [], []
        for j in range(w['rows']):
            for i in range(w['cols']):
                cell = w['rgb'][(j * w['cols'] + i) * 6:(j * w['cols'] + i) * 6 + 6]
                if cell[0] == '.':
                    continue
                x, y = w['x0'] + i * w['step'], w['y0'] + j * w['step']
                ix, iy = int((x - px) / pw * W), int((y - py) / ph * H)
                if not (1 <= ix < W - 1 and 1 <= iy < H - 1):
                    continue
                blk = nm[iy - 1:iy + 2, ix - 1:ix + 2]
                if blk[..., 3].min() < 250 or np.ptp(blk[..., :3], axis=(0, 1)).max() > 12:
                    continue                  # an edge, or a crease where the normal turns
                n = blk[1, 1, :3] / 127.5 - 1
                ns.append(n / np.linalg.norm(n))
                cs.append([int(cell[k:k + 2], 16) for k in (0, 2, 4)])
        x = p['xml']
        rig = re.search(r'lightRig rig="(\w+)" dir="(\w+)"', x)
        rev = re.search(r'lightRig[^>]*><a:rot lat="0" lon="0" rev="(\d+)"', x)
        turn = DIRS[rig.group(2)] + (int(rev.group(1)) / 60000 if rev else 0)
        mat = re.search(r'prstMaterial="(\w+)"', x)
        fill = np.array([int(p['fill'][k:k + 2], 16) for k in (0, 2, 4)], float)
        out.append(dict(probe=p['probe'], note=p['note'], rig=rig.group(1), turn=np.radians(turn), material=mat.group(1) if mat else 'warmMatte',
                        fill=fill, view=view / np.linalg.norm(view), n=np.array(ns), c=np.array(cs, float)))
    return out

def unpack(q, k):
    pw, pp, kp = q[0], q[1], q[2] ** 2
    rigs = {}
    for j, r in enumerate(RIGS):
        b = q[3 + j * (1 + 4 * k):3 + (j + 1) * (1 + 4 * k)]
        L = b[1:].reshape(k, 4).copy()
        L[:, 2:] = L[:, 2:] ** 2
        rigs[r] = (b[0] ** 2, L)
    return {'matte': (0.0, 1.0), 'warmMatte': (1.0, pw), 'plastic': (kp, pp)}, rigs

def lights(pol, az, turn):
    a = az + turn
    return np.array([np.sin(pol) * np.cos(a), np.sin(pol) * np.sin(a), np.cos(pol)])

def shade(q, k, pr):
    mats, rigs = unpack(q, k)
    ks, p = mats[pr['material']]
    A, Ls = rigs[pr['rig']]
    n = pr['n']
    diff = np.full(len(n), A)
    spec = np.zeros(len(n))
    phong = os.environ.get('SPEC') == 'phong'
    for pol, az, D, S in Ls:
        L = lights(pol, az, pr['turn'])
        nl = n @ L
        diff += D * np.maximum(0, nl)
        if phong:                              # the reflection's with the view, not the half vector's with the normal
            rv = 2 * nl * (n @ pr['view']) - L @ pr['view']
            spec += S * np.where(nl > 0, np.maximum(0, rv), 0) ** max(1.0, p)
        else:
            Hv = L + pr['view']
            Hv /= max(1e-9, np.linalg.norm(Hv))
            spec += S * np.maximum(0, n @ Hv) ** max(1.0, p)
    return np.minimum(255, pr['fill'][None, :] * diff[:, None] + (ks * spec * 255)[:, None])

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
        g, Hm = J.T @ r, J.T @ J
        while True:
            q2 = q + np.linalg.solve(Hm + lam * np.diag(np.diag(Hm) + 1e-9), -g)
            r2 = residuals(q2, k, d)
            if r2 @ r2 < cost:
                q, r, cost, lam = q2, r2, r2 @ r2, lam / 3
                break
            lam *= 4
            if lam > 1e8:
                return q, cost
    return q, cost

def start(tsv, k, rng):
    """From phase5b-light.tsv's lights, each rig's extra ones random (behind the drawing as often as not)"""
    rows = [l.rstrip('\n').split('\t') for l in open(tsv) if not l.startswith(('#', 'kind'))]
    q = [31.5, 45.2, np.sqrt(1.976)]
    for r in RIGS:
        A = float([x for x in rows if x[0] == 'ambient' and x[1] == r][0][5])
        b = [np.sqrt(A)]
        Ls = [x for x in rows if x[0] == 'light' and x[1] == r]
        for x in Ls[:k]:
            v = np.array([float(x[2]), float(x[3]), float(x[4])])
            b += [np.arccos(np.clip(v[2], -1, 1)), np.arctan2(v[1], v[0]), np.sqrt(float(x[5])), np.sqrt(float(x[6]))]
        for e in range(k - len(Ls)):
            b += [rng.uniform(1.2, 2.6), rng.uniform(-np.pi, np.pi), rng.uniform(0.2, 0.5), rng.uniform(0.1, 0.5)]
        q += b
    return np.array(q)

def report(q, k, d):
    mats, rigs = unpack(q, k)
    for m, (ks, p) in mats.items():
        print(f'  {m}: ks {ks:.4f} p {p:.2f}')
    for r, (A, Ls) in rigs.items():
        print(f'  {r}: ambient {A:.4f}')
        for pol, az, D, S in Ls:
            L = lights(pol, az, 0)
            print(f'    light ({L[0]:+.4f}, {L[1]:+.4f}, {L[2]:+.4f}) diffuse {D:.4f} specular {S:.4f}')
    tot = []
    for pr in d:
        e = np.abs(shade(q, k, pr) - pr['c']).mean()
        tot.append(e)
        print(f'  {pr["probe"]} {pr["note"][:58]:58s} {e:5.2f} / 255 over {len(pr["c"])}')
    print(f'  mean {np.mean(tot):.2f}, worst {np.max(tot):.2f}')

def grow(q, k, rng):
    """A k-light q with one more light a rig, random"""
    out = list(q[:3])
    for j in range(len(RIGS)):
        out += list(q[3 + j * (1 + 4 * k):3 + (j + 1) * (1 + 4 * k)])
        out += [rng.uniform(0.3, 2.8), rng.uniform(-np.pi, np.pi), rng.uniform(0.2, 0.5), rng.uniform(0.1, 0.4)]
    return np.array(out)

if __name__ == '__main__' and os.environ.get('GROW'):
    k = int(sys.argv[3])
    d = probes(sys.argv[1], sys.argv[2])
    sub = [dict(pr, n=pr['n'][::4], c=pr['c'][::4]) for pr in d]
    q0 = np.array([float(x) for x in re.search(r'q = \[([^\]]*)\]', open(os.environ['GROW']).read()).group(1).split(',')])
    rng = np.random.default_rng(5)
    best = None
    for s in range(int(os.environ.get('STARTS', '4'))):
        q, cost = lm(grow(q0, k - 1, rng), k, sub, 40)
        print('start', s, 'cost', round(cost, 1), flush=True)
        if best is None or cost < best[1]:
            best = (q, cost)
    q, cost = lm(best[0], k, d, 10)
    print('q =', '[' + ', '.join(f'{v:.6f}' for v in q) + ']')
    report(q, k, d)
    sys.exit(0)

if __name__ == '__main__':
    k = int(sys.argv[3])
    d = probes(sys.argv[1], sys.argv[2])
    sub = [dict(pr, n=pr['n'][::4], c=pr['c'][::4]) for pr in d]
    rng = np.random.default_rng(3)
    best = None
    for s in range(int(os.environ.get('STARTS', '4'))):
        q, cost = lm(start(sys.argv[4] if len(sys.argv) > 4 else 'phase5b-light.tsv', k, rng), k, sub, 40)
        print('start', s, 'cost', round(cost, 1), flush=True)
        if best is None or cost < best[1]:
            best = (q, cost)
    q, cost = lm(best[0], k, d, 10)
    print('q =', '[' + ', '.join(f'{v:.6f}' for v in q) + ']')
    report(q, k, d)
