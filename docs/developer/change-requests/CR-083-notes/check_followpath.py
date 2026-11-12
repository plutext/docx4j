# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 2, the Follow Path round: Word's Follow Path against the rule, and docx4j's against Word's.

From read_followpath.py's JSON (Word's PDFs, and docx4j's renders of the same docx):
- scale: Word's glyph size over the run's against k = D / (D + 2T), D the text rectangle's larger side (the box Word
  drew, less the insets and half the 0.25 pt line), T the lines' ink heights added up at their sizes (KernInk.java);
  lines in Word's own form, fixed boxes, any preset. Probes where Word's is less are capped (the text longer than its
  curve) or not Aptos (Word put Calibri for Georgia Pro, whose ink isn't measured here).
- places (with a docx4j JSON): each glyph's origin, Word's against docx4j's, the largest distance a probe, by family.

usage: check_followpath.py <Word's JSON> ... [--d4j <docx4j's JSON>]"""
import collections, json, math, statistics as st, sys

import numpy as np


def rect(p, line=0.25):
    b = p['word_box']
    l, t, r, bo = p['ins']
    return (b[2] - b[0]) - l - r - line, (b[3] - b[1]) - t - bo - line


def scale(word):
    near, off = 0, []
    for p in word:
        lines = [l for l in p['lines'] if l]
        if not p['glyphs'] or not p['word_box'] or not lines or p['warp'] == 'textButton':
            continue
        if len({l['size'] for l in lines}) > 1:
            continue                    # a scale a line: below
        w, h = rect(p)
        T = sum((l['ink'][1] - l['ink'][0]) * l['size'] for l in lines)
        k = max(w, h) / (max(w, h) + 2 * T)
        s = st.median(g['size'] for g in p['glyphs']) / lines[0]['size']
        if abs(s - k) <= 0.003:
            near += 1
        else:
            off.append('%s %s %s s %.4f k %.4f' % (p['probe'], p['warp'], lines[0]['text'][:24], s, k))
    print('scale: %d probes within 0.003 of D / (D + 2T); %d not:' % (near, len(off)))
    for o in off:
        print('   ' + o)


def places(word, d4j):
    d4j = {p['probe']: p for p in d4j}
    fam, allm, skipped = collections.defaultdict(list), [], []
    for p in word:
        q = d4j.get(p['probe'])
        if q is None or any(l and l['face'] == 'Georgia Pro' for l in p['lines']):
            continue
        a, b = p['glyphs'], q['glyphs']
        if not a or len(a) != len(b):
            skipped.append(p['probe'])
            continue
        e = max(math.hypot(x['x'] - y['x'], x['y'] - y['y']) for x, y in zip(a, b))
        fam[p['probe'][:4]].append(e)
        allm.append(e)
    for f in sorted(fam):
        x = np.array(fam[f])
        print('   %s n %3d median %5.2f within 1 pt %3d within 2 pt %3d worst %6.2f' % (f, len(x), np.median(x), (x <= 1).sum(), (x <= 2).sum(), x.max()))
    a = np.array(allm)
    print('places: %d probes, median %.2f pt, within 1 pt %d, within 2 pt %d, within 5 pt %d, worst %.2f; glyph counts differ: %s'
          % (len(a), np.median(a), (a <= 1).sum(), (a <= 2).sum(), (a <= 5).sum(), a.max(), ' '.join(skipped) or 'none'))


if __name__ == '__main__':
    args = sys.argv[1:]
    d4j = None
    if '--d4j' in args:
        i = args.index('--d4j')
        d4j = json.load(open(args[i + 1]))
        args = args[:i] + args[i + 2:]
    word = [p for f in args for p in json.load(open(f))]
    scale(word)
    if d4j is not None:
        places(word, d4j)
