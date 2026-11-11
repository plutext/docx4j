# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084 phase 0: each blip effect's function, as fitted to Word's PDFs (phase0-word.json, from read_recolour.py), and
its error over every probe of it: the largest and the mean difference between the model's patch and Word's, in levels
of 255, over the 256 patches (Word's JPEG re-encoding of some pictures adds a level or two; the filter is listed).

All the arithmetic is on the sRGB values as stored (0..1), not in linear light (grayscale's fit decides it: Rec. 709's
weights on the stored values within 1 level, against 73 levels in linear light).

usage: fit_recolour.py   (prints the table; writes fits-word.tsv beside this script)"""
import csv, json, os, re
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
REC709 = np.array([0.2126, 0.7152, 0.0722])

def luma(rgb):
    return rgb[:, :3] @ REC709

def contrast_slope(c):
    return 1 + c if c <= 0 else (1 / (1 - c) if c < 1 else 1e9)

def lum(rgb, b, c):
    """a:lum: contrast a slope s about mid-grey; brightness a shift of b (1 + s) / 2."""
    s = contrast_slope(c)
    out = rgb.copy()
    out[:, :3] = s * (rgb[:, :3] - 0.5) + 0.5 + b * (1 + s) / 2
    return np.clip(out, 0, 1)

def grayscl(rgb):
    out = rgb.copy()
    out[:, :3] = luma(rgb)[:, None]
    return out

def bilevel(rgb, t):
    out = rgb.copy()
    out[:, :3] = (luma(rgb) >= t)[:, None].astype(float)
    return out

def duotone(rgb, c1, c2):
    """a:duotone: the luma's line from clr1 (black) to clr2 (white)."""
    y = luma(rgb)[:, None]
    out = rgb.copy()
    out[:, :3] = np.array(c1) + (np.array(c2) - np.array(c1)) * y
    return out

def alpha_mod_fix(rgb, amt):
    out = rgb.copy()
    out[:, 3] = rgb[:, 3] * amt
    return out

def rgb_to_hsl(rgb):
    import colorsys
    return np.array([colorsys.rgb_to_hls(*c) for c in rgb[:, :3]])        # (h, l, s), h in 0..1

def hsl_to_rgb(hls):
    import colorsys
    return np.array([colorsys.hls_to_rgb(*c) for c in hls])

def tint(rgb, hue, amt):
    """a:tint as Word draws it: each colour's hue turned amt of the way to hue, the shorter way round (an opposite hue
    by the sign of the plain difference); lightness and saturation kept (greys unchanged)."""
    hls = rgb_to_hsl(rgb)
    d = hue - hls[:, 0]                       # the plain difference, wrapped only beyond half a turn: an exactly
    d = np.where(d > 0.5 + 1e-9, d - 1, np.where(d < -0.5 - 1e-9, d + 1, d))   # opposite hue keeps its sign
    hls[:, 0] = (hls[:, 0] + amt * d) % 1.0
    out = rgb.copy()
    out[:, :3] = hsl_to_rgb(hls)
    return out

def overlay(rgb, colour, blend):
    out = rgb.copy()
    c = np.array(colour)
    out[:, :3] = rgb[:, :3] * c if blend == 'mult' else 1 - (1 - rgb[:, :3]) * (1 - c)
    return out

def repl(rgb, colour):
    out = rgb.copy()
    out[:, :3] = colour
    return out

def alpha_fn(f):
    def g(rgb):
        out = rgb.copy()
        out[:, 3] = f(rgb[:, 3])
        return out
    return g

def alpha_inv(rgb):
    out = rgb.copy()
    out[:, :3] = 0
    out[:, 3] = 1 - rgb[:, 3]
    return out

def black(rgb):
    out = rgb.copy()
    out[:, :3] = 0
    return out

ONE = {   # the effects the pairs are made of
    'grayscl': grayscl, 'lum': lambda x: lum(x, 0.3, 0.3), 'duotone': lambda x: duotone(x, [0, 0, 0], [1, 0, 0]),
    'alphaModFix': lambda x: alpha_mod_fix(x, 0.5), 'biLevel': lambda x: bilevel(x, 0.5),
}

def pair(m):
    a, b = ONE[m[1]], ONE[m[2]]
    return lambda x: b(a(x))

def pair_reversed(m):
    a, b = ONE[m[1]], ONE[m[2]]
    return lambda x: a(b(x))

def vml(rgb, gain=1.0, black=0.0, gamma=1.0):
    """v:imagedata: gain a contrast slope about mid-grey, blacklevel a shift of blacklevel (1 + gain) (DrawingML's lum,
    with blacklevel = bright / 2), gamma a power 1 / gamma (applied first)."""
    out = rgb.copy()
    x = rgb[:, :3] ** (1 / gamma)
    out[:, :3] = np.clip(gain * (x - 0.5) + 0.5 + black * (1 + gain), 0, 1)
    return out

def chromakey(rgb, colour):
    out = rgb.copy()
    hit = np.all(np.abs(rgb[:, :3] - np.array(colour)) < 0.5 / 255, axis=1)
    out[hit, 3] = 0
    return out

def hexc(v):
    return [int(v[i:i + 2], 16) / 255 for i in (0, 2, 4)]

MODELS = [
    (r'^(\w+) then (\w+)$', pair),
    (r'^lum bright (-?\d+)% contrast (-?\d+)%', lambda m: lambda x: lum(x, int(m[1]) / 100, int(m[2]) / 100)),
    (r'^grayscl', lambda m: grayscl),
    (r'^biLevel thresh (\d+)%', lambda m: lambda x: bilevel(x, int(m[1]) / 100)),
    (r'^duotone black to white', lambda m: lambda x: duotone(x, [0, 0, 0], [1, 1, 1])),
    (r'^duotone black to red', lambda m: lambda x: duotone(x, [0, 0, 0], [1, 0, 0])),
    (r'^duotone blue to yellow', lambda m: lambda x: duotone(x, [0, 0, 1], [1, 1, 0])),
    (r'^alphaModFix amt (\d+)%', lambda m: lambda x: alpha_mod_fix(x, int(m[1]) / 100)),
    (r'^hsl ', lambda m: black),
    (r'^tint hue (\d+)(?: degrees)? amt (\d+)%', lambda m: lambda x: tint(x, int(m[1]) / 360, int(m[2]) / 100)),
    (r'^clrRepl green', lambda m: lambda x: repl(x, [0, 1, 0])),
    (r'^fillOverlay mult red', lambda m: lambda x: overlay(x, [1, 0, 0], 'mult')),
    (r'^fillOverlay screen blue', lambda m: lambda x: overlay(x, [0, 0, 1], 'screen')),
    (r'^alphaBiLevel thresh 50%', lambda m: alpha_fn(lambda a: (a >= 0.5).astype(float))),
    (r'^alphaCeiling', lambda m: alpha_fn(lambda a: (a > 0).astype(float))),
    (r'^alphaFloor', lambda m: alpha_fn(lambda a: (a >= 1).astype(float))),
    (r'^alphaInv', lambda m: alpha_inv),
    (r'^alphaMod by a container of alphaModFix 50%', lambda m: alpha_fn(lambda a: a * a * 0.5)),
    (r'^alphaRepl a 50%', lambda m: lambda x: alpha_fn(lambda a: np.full_like(a, 0.5))(
        np.where(x[:, 3:4] > 0, x, np.concatenate([np.zeros_like(x[:, :3]), x[:, 3:]], 1)))),
    (r'^VML, unadjusted', lambda m: lambda x: x),
    (r'^VML gain ([\d.]+)', lambda m: lambda x: vml(x, gain=float(m[1]))),
    (r'^VML blacklevel ([+-][\d.]+)', lambda m: lambda x: vml(x, black=float(m[1]))),
    (r'^VML gamma ([\d.]+)', lambda m: lambda x: vml(x, gamma=float(m[1]))),
    (r'^VML Washout', lambda m: lambda x: vml(x, gain=19661 / 65536, black=22938 / 65536)),
    (r'^VML grayscale', lambda m: grayscl),
    (r'^VML bilevel', lambda m: lambda x: bilevel(x, 0.5)),
    (r'^VML chromakey red', lambda m: lambda x: chromakey(x, [1, 0, 0])),
    (r'^Recolor: Washout', lambda m: lambda x: lum(x, 0.7, -0.7)),
]
REVERSED = [(r'^(\w+) then (\w+)$', pair_reversed)]

def main():
    d = json.load(open(os.path.join(HERE, 'phase0-word.json'), encoding='utf-8'))
    rows = []
    for k, v in sorted(d.items()):
        pics = [p for p in v['pictures'] if p.get('patches')]
        if not pics or not v['source']:
            continue
        for pat, make in MODELS:
            m = re.match(pat, v['note'])
            if not m:
                continue
            src = np.array(v['source'], float) / 255
            word = np.array([q if q else [np.nan] * 4 for q in pics[0]['patches']], float) / 255
            def score(f):
                pred = f(src)
                opaque = (word[:, 3] > 0.25) & ~np.isnan(word[:, 0])
                e = np.abs(pred[:, :3] - word[:, :3])[opaque] * 255
                ea = np.abs(pred[:, 3] - word[:, 3]) * 255
                return (round(float(e.max()), 2) if e.size else '', round(float(e.mean()), 2) if e.size else '',
                        round(float(np.nanmax(ea)), 2))
            mx, mean, amax = score(make(m))
            row = {'probe': k, 'note': v['note'], 'picture': v['picture'], 'filter': pics[0]['filter'], 'max': mx,
                   'mean': mean, 'alpha_max': amax, 'reversed_max': ''}
            for rpat, rmake in REVERSED:
                rm = re.match(rpat, v['note'])
                if rm:
                    row['reversed_max'] = score(rmake(rm))[0]
            rows.append(row)
            break
    with open(os.path.join(HERE, 'fits-word.tsv'), 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, list(rows[0]), delimiter='\t', lineterminator='\n')
        w.writeheader()
        w.writerows(rows)
    for r in rows:
        print(f"{r['probe']} {r['note'][:44]:44s} {r['picture'][:12]:12s} {r['filter'][:4]} colour max {r['max']!s:>6} mean {r['mean']!s:>5}  alpha max {r['alpha_max']:6.2f}" + (f"  (reversed: {r['reversed_max']})" if r['reversed_max'] != '' else ''))

if __name__ == '__main__':
    main()
