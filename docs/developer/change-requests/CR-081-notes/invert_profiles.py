# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-1: a bevel's true profile, its groove included, read from Word's shading (round cr081-phase5b-r5:
fxg5-13 hardEdge 24 x 12 pt, fxg5-14 relaxedInset 30 x 9 pt, a grey rectangle under twoPt rev 120). Phase 5a's
profiles are its silhouettes' envelope, which a groove doesn't show. With the fitted lights (phase5b-light.tsv), each
depth's slope is the one whose colours on the four sides are nearest Word's; the slopes are integrated, the first
pixel's (lost to antialiasing) taken as the second's, and the profile scaled so that its ridge is the bevel's height
(the envelope's 1). Printed as 65 heights at t = 0, 1/64 ... 1 for Scene3D's PROFILES.

usage: invert_profiles.py <r5 results dir>
       invert_profiles.py <5b-2 r1 results dir> b2   (the other nine presets, cr081-phase5b2-r1's fxh5)"""
import os, sys
import numpy as np
import fit_cuts as fc

S = np.linspace(-6, 6, 4801)

def invert(pdf, page, w):
    A, Ls = fc.rig('twoPt', 120)
    tables = {side: np.array([fc.shade(np.array([-s * u[0], -s * u[1], 1.0]), A, Ls) for s in S]) for side, u in fc.SIDES}
    prof = fc.profile(pdf, page, w)
    by = {side: dict((round(d, 2), g) for d, g in prof[side]) for side, u in fc.SIDES}
    depths = sorted(by['top'])
    out = []
    for d in depths:
        cost = np.zeros(len(S))
        for side, u in fc.SIDES:
            ks = [k for k in by[side] if abs(k - d) < 0.2]
            if ks:
                cost += (tables[side] - by[side][min(ks, key=lambda k: abs(k - d))]) ** 2
        out.append((d, S[int(np.argmin(cost))]))
    return out

def table(slopes, w):
    d = np.array([0.0] + [x for x, s in slopes] + [w])
    s = np.array([slopes[0][1]] + [s for x, s in slopes] + [0.0])
    fine = np.linspace(0, w, 6401)
    sl = np.interp(fine, d, s)
    z = np.concatenate([[0], np.cumsum((sl[1:] + sl[:-1]) / 2 * np.diff(fine))])
    z /= z.max()
    return np.interp(np.linspace(0, w, 65), fine, z)

PRESETS_B2 = ('angle', 'artDeco', 'convex', 'coolSlant', 'cross', 'divot', 'riblet', 'slope', 'softRound')

def java_name(p):
    return {'angle': 'ANGLE', 'artDeco': 'ART_DECO', 'convex': 'CONVEX', 'coolSlant': 'COOL_SLANT', 'cross': 'CROSS',
            'divot': 'DIVOT', 'riblet': 'RIBLET', 'slope': 'SLOPE', 'softRound': 'SOFT_ROUND'}[p]

if __name__ == '__main__':
    if len(sys.argv) > 2 and sys.argv[2] == 'b2':
        # phase 5b-2: cr081-phase5b2-r1's fxh5, each preset at 24 by 12 pt (pages 1, 3, ...)
        pdf = os.path.join(sys.argv[1], 'fxh5-presets-dirs-a', 'fxh5-presets-dirs.pdf')
        for k, p in enumerate(PRESETS_B2):
            t = table(invert(pdf, 2 * k + 1, 24.0), 24.0)
            print(f'		PROFILES.put(STBevelPresetType.{java_name(p)}, new double[] {{ ' + ', '.join(f'{v:.3f}' for v in t) + ' });')
        sys.exit(0)
    pdf = os.path.join(sys.argv[1], 'fxg5-bevel-cuts-a', 'fxg5-bevel-cuts.pdf')
    for page, name, w in ((13, 'HARD_EDGE', 24.0), (14, 'RELAXED_INSET', 30.0)):
        t = table(invert(pdf, page, w), w)
        print(f'		PROFILES.put(STBevelPresetType.{name}, new double[] {{ ' + ', '.join(f'{v:.3f}' for v in t) + ' });')
