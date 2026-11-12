# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 2: Follow Path (textArchUp, textArchDown, textCircle) fitted to Word's PDFs of phase 0's probes.

Word's invisible text on a Follow Path WordArt is each glyph's own, where the glyph is, turned: so each glyph's place,
turn and scale come from mutool stext (origin, quad, size). The model WordArtWarps.followPath implements:
- the preset's path in the text rectangle; the baseline the path moved h_ref of the size away from the glyphs' tops
  (a normal offset), the advances measured along the curve h_adv above the baseline, each glyph's middle there;
- the line centred on the path by arc length; then everything scaled about the rectangle's centre by the text's scale
  (taken here from Word's size, so that the place is fitted apart from the scale).

usage: fit_follow_path.py <Word's results dir> <kern file>
  the kern file: a line a text, "text pos0 pos1 ... posN" in ems (kerned glyph positions at 1 em), as Java's
  layoutGlyphVector with TextAttribute.KERNING gives them (WordArt, wave, Warpy)
prints the best (h_ref, h_adv) by the largest error, and each probe's errors (points) and turn errors (degrees)."""
import functools, math, sys
import numpy as np
import warp_model as wm

wm.pd.pages = functools.lru_cache(maxsize=64)(wm.pd.pages)

CASES = [('textArchUp', 'wa15-warps-fixed-1', 3, 36), ('textArchUp', 'wa10-warps-default-1', 3, 36), ('textArchUp', 'wa20-form', 2, 36),
         ('textArchUp', 'wa20-form', 9, 36), ('textArchUp', 'wa20-form', 28, 18), ('textArchUp', 'wa20-form', 29, 72),
         ('textArchDown', 'wa15-warps-fixed-1', 1, 36), ('textArchDown', 'wa10-warps-default-1', 1, 36),
         ('textCircle', 'wa15-warps-fixed-1', 13, 36), ('textCircle', 'wa10-warps-default-1', 13, 36), ('textCircle', 'wa20-form', 33, 36),
         ('textArchUp', 'wa21-text-rect', 10, 36), ('textArchUp', 'wa21-text-rect', 11, 36)]

@functools.lru_cache(maxsize=None)
def glyphs(results, job, page, ins, line=0.25):
    """The text rectangle and Word's invisible glyphs: c, size, origin, turn"""
    w = wm.word_page('%s/%s/%s.pdf' % (results, job, job), page)
    bx, h = w['box'], line / 2
    rect = (bx[0] + ins[0] + h, bx[1] + ins[1] + h, bx[2] - ins[2] - h, bx[3] - ins[3] - h)
    out = []
    for c in w['text']:
        if c['alpha'] != '#00' or not c['c'].strip():
            continue
        q = c['quad']
        out.append(dict(c=c['c'], s=c['size'], x=c['x'], y=c['y'], ang=math.atan2(q[3] - q[1], q[2] - q[0])))
    return rect, out

def offset(path, d):
    t = np.gradient(path, axis=0)
    t /= np.hypot(*t.T)[:, None]
    return path + np.stack([t[:, 1], -t[:, 0]], 1) * d

def model(results, kern, name, job, page, size, h_ref, h_adv):
    ins = (0, 0, 0, 0) if (job, page) == ('wa20-form', 9) else (7.2, 3.6, 7.2, 3.6)
    (x0, y0, x1, y1), g = glyphs(results, job, page, ins)
    C = np.array([(x0 + x1) / 2, (y0 + y1) / 2])
    s = g[0]['s'] / size
    path = wm.preset_paths(name, x1 - x0, y1 - y0)[0] + np.array([x0, y0])
    base = offset(path, -h_ref * size)
    adv = offset(base, h_adv * size)
    text = ''.join(d['c'] for d in g)
    pos = [v * size for v in kern[text]]
    La = np.r_[0, np.cumsum(np.hypot(*np.diff(adv, axis=0).T))]
    start = (La[-1] - pos[-1]) / 2
    err, turn = [], []
    for i, d in enumerate(g):
        j = np.interp(start + (pos[i] + pos[i + 1]) / 2, La, np.arange(len(La)))
        k = int(min(len(base) - 2, max(0, math.floor(j))))
        f = j - k
        p = C + s * (base[k] * (1 - f) + base[k + 1] * f - C)
        tv = base[k + 1] - base[k]
        a = (kern[text][i + 1] - kern[text][i]) * d['s'] / 2
        word = np.array([d['x'] + math.cos(d['ang']) * a, d['y'] + math.sin(d['ang']) * a])
        err.append(float(np.hypot(*(p - word))))
        turn.append(math.degrees(math.atan2(tv[1], tv[0]) - d['ang']))
    return np.array(err), np.array(turn)

def main(results, kernfile):
    kern = {l.split()[0]: [float(v) for v in l.split()[1:]] for l in open(kernfile)}
    best = None
    for h1 in np.linspace(0.05, 0.25, 21):
        for h2 in np.linspace(-0.2, 0.6, 33):
            worst = max(model(results, kern, n, j, p, z, h1, h2)[0].max() for n, j, p, z in CASES)
            if best is None or worst < best[0]:
                best = (worst, h1, h2)
    print('best: h_ref %.3f h_adv %.3f, largest error %.2f pt' % (best[1], best[2], best[0]))
    for n, j, p, z in CASES:
        e, t = model(results, kern, n, j, p, z, best[1], best[2])
        print('  %-12s %-22s %2d  max %.2f  %s  turn %s' % (n, j, p, e.max(), np.round(e, 2), np.round(t, 1)))

if __name__ == '__main__':
    main(*sys.argv[1:3])
