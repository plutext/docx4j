# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-2: the translucent materials, clear and translucentPowder, fitted from cr081-phase5b2-r1's fxh4
(the grey extruded ellipse under threePt from four views). Word draws them with a soft mask, so its colour and its
alpha are read apart (read_light.colours), at docx4j's own normals: the nearest surface's, and the farthest's
(normals-hook.patch applied: SCENE3D_DEBUG=normals, and SCENE3D_LAYER=back for the farthest).

Word draws a cap (the face, the back) only where it faces the viewer, and the extrusion's walls from both sides: a
ray meets the nearest surface, and behind it a wall if the farthest surface is one. Each layer's

    alpha = min(1, max(0, a0 + a1 |n.v|))       (n.v the normal's facing the viewer)

composited front over back; each layer's colour fit_materials_b2's model, a wall seen from inside lit as its inside,
fitted to the picture over a white page (where the colour counts as much as it shows). The rules against Word's alpha
(mean of the four views, / 255):

    nearest surface only     clear 14.69   translucentPowder 19.66
    every surface            clear 28.53   translucentPowder 21.43
    nearest, and a wall      clear  1.46   translucentPowder  1.59

usage: fit_translucent_b2.py <Word's fxh4 PDF> <front normal-map dir> <back normal-map dir> <material>"""
import csv, os, sys
import numpy as np
from PIL import Image
import read_light as rl
import fit_materials_b2 as mb

HERE = os.path.dirname(os.path.abspath(__file__))

def _map(d, probe):
    nm = np.asarray(Image.open(os.path.join(d, probe + '.png')).convert('RGBA'), float)
    v = [float(x) for x in open(os.path.join(d, probe + '.txt')).read().split()]
    return nm, v

def samples(pdf, front, back, probe, page):
    rgb, a, box, _ = rl.colours(pdf, page)
    F, v = _map(front, probe)
    B, _ = _map(back, probe)
    px, py, pw, ph = v[:4]
    view = np.array(v[4:7]) / np.linalg.norm(v[4:7])
    H, W = F.shape[:2]; h, w = a.shape
    nf, nb, als, cs = [], [], [], []
    for j in range(2, H - 2, 2):
        for i in range(2, W - 2, 2):
            bf, bb = F[j - 1:j + 2, i - 1:i + 2], B[j - 1:j + 2, i - 1:i + 2]
            if bf[..., 3].min() < 250 or bb[..., 3].min() < 250:
                continue
            if np.ptp(bf[..., :3], axis=(0, 1)).max() > 12 or np.ptp(bb[..., :3], axis=(0, 1)).max() > 12:
                continue                      # an edge, or a crease where the normal turns
            x, y = px + (i + 0.5) / W * pw, py + (j + 0.5) / H * ph
            ix, iy = int((x - box[0]) / (box[2] - box[0]) * w), int((y - box[1]) / (box[3] - box[1]) * h)
            if not (0 <= ix < w and 0 <= iy < h):
                continue
            n1 = bf[1, 1, :3] / 127.5 - 1; n2 = bb[1, 1, :3] / 127.5 - 1
            nf.append(n1 / np.linalg.norm(n1)); nb.append(n2 / np.linalg.norm(n2))
            als.append(a[iy, ix]); cs.append(rgb[iy, ix] * 255)
    nf, nb = np.array(nf), np.array(nb)
    return dict(probe=probe, nf=nf, nb=nb, wall=np.abs(nb[:, 2]) < 0.05, alpha=np.array(als), c=np.array(cs), view=view,
                turn=0.0, fill=np.array([128.0, 128.0, 128.0]))

def layers(a0, a1, pr):
    af = np.clip(a0 + a1 * np.abs(pr['nf'] @ pr['view']), 0, 1)
    ab = np.where(pr['wall'], np.clip(a0 + a1 * np.abs(pr['nb'] @ pr['view']), 0, 1), 0)
    return af, ab

def colour(q, pr, af, ab):
    cf = mb.shade(q, dict(pr, n=pr['nf']))
    cb = mb.shade(q, dict(pr, n=-pr['nb']))       # a wall seen from inside: its inside faces the viewer
    wb = (1 - af) * ab
    return (af[:, None] * cf + wb[:, None] * cb) / np.maximum(1e-6, af + wb)[:, None]

if __name__ == '__main__':
    pdf, front, back, mat = sys.argv[1:5]
    rows = [p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t')
            if p['set'] == 'cr081-phase5b2-r1' and p['probe'].startswith('fxh4') and f', {mat},' in p['note']]
    d = [dict(samples(pdf, front, back, p['probe'], int(p['page'])), note=p['note']) for p in rows]
    # alpha: a grid search, the mean error over the views
    best = None
    for a0 in np.linspace(-0.5, 1.5, 201):
        for a1 in np.linspace(-1.5, 1.5, 301):
            e = np.mean([np.abs(1 - (1 - af) * (1 - ab) - pr['alpha']).mean() for pr in d for af, ab in [layers(a0, a1, pr)]])
            if best is None or e < best[0]:
                best = (e, a0, a1)
    _, a0, a1 = best
    print(f'{mat} alpha = {a0:.3f} {a1:+.3f} |n.v|')
    # colour: the picture over a white page, Word's and the model's
    fit = []
    for pr in d:
        af, ab = layers(a0, a1, pr)
        fit.append(dict(pr, af=af, ab=ab, at=1 - (1 - af) * (1 - ab), page=pr['alpha'][:, None] * pr['c'] + (1 - pr['alpha'][:, None]) * 255))
    def over_white(q, pr):
        return pr['at'][:, None] * colour(q, pr, pr['af'], pr['ab']) + (1 - pr['at'][:, None]) * 255
    def residuals(q):
        return np.concatenate([(over_white(q, pr) - pr['page']).ravel() / np.sqrt(len(pr['c'])) for pr in fit])
    best = None
    for q0 in ([1, 1, 0, 0.01, 1, 8, 0.01, 2], [1, 1, 0.1, 0.5, 0.5, 20, 0.5, 3], [0.7, 0.7, 0, 0.7, 1.4, 4, 0.7, 1.5]):
        q = np.array(q0, float); lam = 1e-2; r = residuals(q); cost = r @ r
        for it in range(60):
            J = np.empty((len(r), len(q)))
            for j in range(len(q)):
                dq = np.zeros(len(q)); dq[j] = 1e-5 * max(1, abs(q[j]))
                J[:, j] = (residuals(q + dq) - r) / dq[j]
            g, Hm = J.T @ r, J.T @ J
            while lam < 1e8:
                q2 = q + np.linalg.solve(Hm + lam * np.diag(np.diag(Hm) + 1e-9), -g)
                r2 = residuals(q2)
                if r2 @ r2 < cost:
                    q, r, cost, lam = q2, r2, r2 @ r2, lam / 3
                    break
                lam *= 4
            if lam >= 1e8:
                break
        if best is None or cost < best[1]:
            best = (q, cost)
    q = best[0]
    print(mat, ' '.join(f'{k} {v:.4f}' for k, v in zip(mb.NAMES, mb.unpack(q))))
    ea, ec = [], []
    for pr, fp in zip(d, fit):
        af, ab = layers(a0, a1, pr)
        e_a = np.abs(1 - (1 - af) * (1 - ab) - pr['alpha']).mean() * 255
        e_c = np.abs(over_white(q, fp) - fp['page']).mean()
        ea.append(e_a); ec.append(e_c)
        print(f'  {pr["probe"]} {pr["note"][:44]:44s} alpha {e_a:5.2f} / 255, over white {e_c:5.2f} / 255 over {len(fp["c"])}')
    print(f'  {mat} alpha mean {np.mean(ea):.2f}, worst {np.max(ea):.2f}; over white mean {np.mean(ec):.2f}, worst {np.max(ec):.2f}')
