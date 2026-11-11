# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a, round 2: the bevels' profiles from Word's edge-on pictures (fxd4, orthographicFront lon 90). Seen
edge on, a column of the picture is a depth X behind the face (X = -z), and the silhouette's top is the outline inset
by the bevel there. So the profile P(t), the height as a fraction of the bevel's at a fraction t of its width in from
the outline, is 1 - X(t) / h, where X(t) is the least depth at which the inset is at most t w: the profile's
envelope as the silhouette shows it (a groove inside it doesn't show).

usage: bevel_profiles.py <results dir>   (prints each profile at t = 0, 1/32 ... 1 and writes phase5a-bevels.tsv)"""
import csv, os, sys
import numpy as np
import read_3d as r3

HERE = os.path.dirname(os.path.abspath(__file__))
N = 32

def top_edge(pdf, page, frame_h=108.0):
    """(X, inset) along the silhouette's top: X points behind the face, inset points in from the outline."""
    d = r3.the_picture(pdf, page)
    _, a = r3.pd.pixels(pdf, d['obj'])
    x0, y0, x1, y1 = d['box']
    h, w = a.shape
    sx, sy = (x1 - x0) / w, (y1 - y0) / h
    cx, cy = r3.FIXED[0] + 72, r3.FIXED[1] + frame_h / 2
    top = r3.PAGE_H - y1
    out = []
    for j in range(w):
        col = a[:, j]
        on = np.nonzero(col >= 0.5)[0]
        if not len(on):
            continue
        i = on[0]
        t = (0.5 - col[i - 1]) / (col[i] - col[i - 1]) if i > 0 and col[i] != col[i - 1] else 0.5
        y = top + (i - 0.5 + t) * sy - cy
        out.append((x0 + (j + 0.5) * sx - cx, y + frame_h / 2))
    return np.array(out)

def profile(edge, w, h):
    X, inset = edge[:, 0], edge[:, 1]
    P = []
    for k in range(N + 1):
        t = k / N
        ok = X[inset <= t * w + 1e-6]
        x_t = ok.min() if len(ok) else h
        P.append(min(1.0, max(0.0, 1 - max(0.0, x_t) / h)))
    P[0] = 0.0                       # at the outline: the foot of the bevel
    return P

def main(results):
    probes = {p['probe']: p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t')
              if p['set'] == 'cr081-phase5a-r2' and p['probe'].startswith('fxd4')}
    rows = ['bevel\tw\th\t' + '\t'.join(f't{k}' for k in range(N + 1)) + '\n']
    for pid, p in probes.items():
        note = p['note']
        if 'edge on' not in note or 'bottom' in note or 'above' in note:
            continue
        name = note.split()[1]
        dims = note.split()[2:5]                 # e.g. 24 by 24
        w, h = float(dims[0]), float(dims[2])
        pdf = os.path.join(results, f"{p['file']}-a", f"{p['file']}.pdf")
        P = profile(top_edge(pdf, int(p['page'])), w, h)
        print(f'{pid} {name:13s} {w:4.0f} x {h:4.0f}  ' + ' '.join(f'{v:.3f}' for v in P[::4]))
        rows.append(f'{name}\t{w:g}\t{h:g}\t' + '\t'.join(f'{v:.4f}' for v in P) + '\n')
    open(os.path.join(HERE, 'phase5a-bevels.tsv'), 'w', newline='').writelines(rows)

if __name__ == '__main__':
    main(sys.argv[1])
