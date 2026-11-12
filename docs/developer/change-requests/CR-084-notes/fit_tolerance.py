# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084 phase 1b: which colours Word's a:clrChange takes, from tolerance-word.json (read_tolerance.py).

For each probe that makes its target transparent: the patches Word cleared (alpha below half) against each patch's
distance from the target, on the source's levels as decoded (a JPEG's by PIL's decoder; Word's own decoder shows in the
patches it kept, which are written losslessly). Each distance measure (the largest channel's difference, their sum,
the Euclidean distance) is tried at every threshold; the table gives the best agreement, the largest distance Word
cleared and the smallest it kept.

usage: fit_tolerance.py"""
import json, math, os

HERE = os.path.dirname(os.path.abspath(__file__))
TARGET = {'white': (255, 255, 255), 'red': (255, 0, 0), 'grey': (128, 128, 128), 'blue': (0x33, 0x66, 0x99)}

METRICS = {
    'max': lambda d: max(abs(x) for x in d),
    'sum': lambda d: sum(abs(x) for x in d),
    'euclid': lambda d: math.sqrt(sum(x * x for x in d)),
}

def main():
    w = json.load(open(os.path.join(HERE, 'tolerance-word.json'), encoding='utf-8'))
    rows = []
    for k, v in sorted(w.items()):
        if 'to transparent' not in v['note']:
            continue
        base = v['note'].split(',')[0]
        t = TARGET[base]
        pic = [q for q in v['pictures'] if q.get('patches')][0]
        cleared, kept = [], []
        for i, (src, word) in enumerate(zip(v['decoded'], pic['patches'])):
            if not word:
                continue
            d = [src[c] - t[c] for c in range(3)]
            (cleared if word[3] < 128 else kept).append((i, d, src, word))
        line = f"{k} {v['note'][:52]:52s} cleared {len(cleared):3d}"
        for name, m in METRICS.items():
            c = sorted(m(x[1]) for x in cleared)
            kk = sorted(m(x[1]) for x in kept)
            line += f" | {name} cleared<= {c[-1] if c else '-':>5} kept>= {kk[0] if kk else '-':>5}" if isinstance(c[-1] if c else 0, int) else \
                f" | {name} cleared<= {c[-1]:5.1f} kept>= {kk[0]:5.1f}" if c and kk else f" | {name} -"
        rows.append(line)
    for r in rows:
        print(r)

if __name__ == '__main__':
    main()
