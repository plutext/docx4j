# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a: Word's 3-D silhouettes as docx4j-chart-render's test oracle (Scene3DTest). For every fixed probe
of cr081-phase5a-r1, -r2, -r4 and -r5 (the layout family aside): the probe's size and XML, and Word's silhouette there, the
convex hull of its mask's 0.5 crossings (points from the frame's centre, y down) reduced to at most 64 vertices (16 cut an acute corner), and
the crossings' bounds.

One JSON object a line, gzip, no name or time.

usage: export_3d.py <round 1 dir> <round 2 dir> <round 4 dir> <round 5 dir> [out]
(default out: docx4j-chart-render/src/test/resources/effects/cr081-word-3d.jsonl.gz)"""
import csv, gzip, io, json, os, sys
import numpy as np
import read_3d as r3

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.normpath(os.path.join(HERE, '..', '..', '..', '..'))
OUT = os.path.join(REPO, 'docx4j-chart-render', 'src', 'test', 'resources', 'effects', 'cr081-word-3d.jsonl.gz')
TALL = ('fxc4', 'fxc5', 'fxc6')

def silhouette(pdf, page, size):
    d = r3.the_picture(pdf, page)
    _, a = r3.pd.pixels(pdf, d['obj'])
    x0, y0, x1, y1 = d['box']
    h, w = a.shape
    sx, sy = (x1 - x0) / w, (y1 - y0) / h
    cx, cy = r3.FIXED[0] + size[0] / 2, r3.FIXED[1] + size[1] / 2
    top = r3.PAGE_H - y1
    pts = np.array([(x0 + px * sx - cx, top + py * sy - cy) for px, py in r3.crossings(a)])
    hull = r3.reduce(r3.hull([tuple(p) for p in pts]), 64)
    return ([[round(x, 2), round(y, 2)] for x, y in hull],
            [round(float(v), 2) for v in (pts[:, 0].min(), pts[:, 1].min(), pts[:, 0].max(), pts[:, 1].max())])

RESULTS = ('cr081-phase5a-r1', 'cr081-phase5a-r2', 'cr081-phase5a-r4', 'cr081-phase5a-r5')

def main(r1, r2, r4, r5, out=OUT):
    lines = []
    for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'):
        if p['set'] not in RESULTS or p['probe'].startswith('fxca'):
            continue
        prefix = p['probe'].split('-')[0]
        size = (72.0, 144.0) if prefix in TALL else (144.0, 108.0)
        res = {'cr081-phase5a-r1': r1, 'cr081-phase5a-r2': r2, 'cr081-phase5a-r4': r4, 'cr081-phase5a-r5': r5}[p['set']]
        pdf = os.path.join(res, f"{p['file']}-a", f"{p['file']}.pdf")
        hull, bounds = silhouette(pdf, int(p['page']), size)
        lines.append({'probe': p['probe'], 'note': p['note'], 'w': size[0], 'h': size[1], 'shape': 'shape' in p['object'],
                      'xml': p['xml'], 'word': {'hull': hull, 'bounds': bounds}})
    data = ''.join(json.dumps(r, separators=(',', ':')) + '\n' for r in lines).encode('utf-8')
    buf = io.BytesIO()
    with gzip.GzipFile(filename='', mode='wb', fileobj=buf, mtime=0) as g:
        g.write(data)
    open(out, 'wb').write(buf.getvalue())
    print(len(lines), 'probes ->', out)

if __name__ == '__main__':
    main(*sys.argv[1:])
