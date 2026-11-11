# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-1: how many pieces Word cuts a circle bevel's quarter circle into (round cr081-phase5b-r5, fxg5;
also r4's fxg4-04 and -10). Each probe's four sides, pixel by pixel from the outline in, against the fitted lights
(phase5b-light.tsv) lit at the cuts of N equal-angle pieces and blended between, as Word's Gouraud shading is, and
per pixel (N infinite). The N with the least error is Word's.

usage: fit_cuts.py <r5 results dir> [r4 results dir]"""
import csv, os, re, sys
import numpy as np
import read_light as rl

HERE = os.path.dirname(os.path.abspath(__file__))
ROWS = [l.rstrip('\n').split('\t') for l in open(os.path.join(HERE, 'phase5b-light.tsv')) if not l.startswith(('#', 'kind'))]
MAT = {r[1]: (float(r[5]), float(r[6])) for r in ROWS if r[0] == 'material'}

def rig(r, rev):
    A = float([x for x in ROWS if x[0] == 'ambient' and x[1] == r][0][5])
    a = np.radians(rev); c, s = np.cos(a), np.sin(a)
    return A, [(np.array([float(x[2]) * c - float(x[3]) * s, float(x[2]) * s + float(x[3]) * c, float(x[4])]), float(x[5]), float(x[6]))
               for x in ROWS if x[0] == 'light' and x[1] == r]

def shade(n, A, Ls, c=128.0, m='warmMatte'):
    ks, p = MAT[m]
    n = n / np.linalg.norm(n)
    diff, spec = A, 0.0
    for L, D, S in Ls:
        d = n @ L
        if d <= 0: continue
        diff += D * d
        rv = 2 * d * n[2] - L[2]
        if rv > 0: spec += S * rv ** p
    return min(255.0, c * diff + 255 * ks * spec)

def normal(phi, u, w, h):
    return np.array([-u[0] * h * np.cos(phi), -u[1] * h * np.cos(phi), w * np.sin(phi)])

def predict(d, u, w, h, N, A, Ls):
    t = min(1.0, max(0.0, d / w))
    phi = np.arccos(1 - t)
    if N is None:
        return shade(normal(phi, u, w, h), A, Ls)
    step = np.pi / 2 / N
    k = min(N - 1, int(phi // step))
    p0, p1 = k * step, (k + 1) * step
    t0, t1 = 1 - np.cos(p0), 1 - np.cos(p1)
    f = (t - t0) / (t1 - t0)
    return shade(normal(p0, u, w, h), A, Ls) * (1 - f) + shade(normal(p1, u, w, h), A, Ls) * f

SIDES = (('top', (0, 1)), ('bottom', (0, -1)), ('left', (1, 0)), ('right', (-1, 0)))

def profile(pdf, page, w):
    """Word's grey at each pixel's centre along each side's middle, from the outline in to the bevel's foot and a pixel"""
    rgb, a, box, filt = rl.colours(pdf, page)
    H, W = a.shape
    px = (box[2] - box[0]) / W
    out = {}
    for side, u in SIDES:
        pts = []
        for k in range(0, int((w + 0.5) / px) + 1):
            if side in ('top', 'bottom'):
                ix = int((72 - box[0]) / px)
                iy = k if side == 'top' else H - 1 - k
                d = (box[1] + (iy + 0.5) * px) if side == 'top' else (rl.H - (box[1] + (iy + 0.5) * px))
            else:
                iy = int((54 - box[1]) / px)
                ix = k if side == 'left' else W - 1 - k
                d = (box[0] + (ix + 0.5) * px) if side == 'left' else (rl.W - (box[0] + (ix + 0.5) * px))
            if a[iy, ix] > 0.99 and 0.05 < d < w:
                pts.append((d, rgb[iy, ix].mean() * 255))
        out[side] = pts
    return out

def main(r5, r4=None):
    A, Ls = rig('twoPt', 120)
    probes = [p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t') if p['set'] == 'cr081-phase5b-r5']
    cases = [(os.path.join(r5, 'fxg5-bevel-cuts-a', 'fxg5-bevel-cuts.pdf'), int(p['page']), p['note']) for p in probes]
    if r4:
        cases += [(os.path.join(r4, 'fxg4-small-bevel-a', 'fxg4-small-bevel.pdf'), 4, 'circle 2 x 1.5 pt (r4)'),
                  (os.path.join(r4, 'fxg4-small-bevel-a', 'fxg4-small-bevel.pdf'), 10, 'circle 6 x 4.5 pt (r4)')]
    Ns = [1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 24, 32, None]
    print('probe' + ' ' * 26 + ' '.join(f'{("N" + str(n)) if n else "pixel":>6s}' for n in Ns) + '   best')
    for pdf, page, note in cases:
        m = re.search(r'(\w+) ([\d.]+) x ([\d.]+) pt', note)
        if not m or m.group(1) != 'circle':
            continue
        w, h = float(m.group(2)), float(m.group(3))
        prof = profile(pdf, page, w)
        errs = []
        for N in Ns:
            e = [abs(predict(d, u, w, h, N, A, Ls) - g) for side, u in SIDES for d, g in prof[side]]
            errs.append(np.mean(e))
        best = Ns[int(np.argmin(errs))]
        print(f'{note[:30]:30s} ' + ' '.join(f'{e:6.2f}' for e in errs) + f'   {best}')

if __name__ == '__main__':
    main(*sys.argv[1:])
