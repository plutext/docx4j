# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-1: Word's 3-D pictures' colours as docx4j-chart-render's test oracle (Scene3DTest's shading test).
For every probe of cr081-phase5b-r1 (fxg1), -r2 (fxg2), -r3 (fxg3), -r4 (fxg4) and -r5 (fxg5), but r4's white line
(fxg4-03: a line drawn, not lit) and its whole style (fxg4-07: a shadow, a fill): its size, geometry, fill and XML, and Word's picture's
colours on a grid of 1.5 pt over its picture's box (points from the frame's top left, y down), where it is opaque and
so are its neighbours; elsewhere "......".

usage: export_3d_light.py <r1 results dir> ... <r5 results dir> [out]
(default out: docx4j-chart-render/src/test/resources/effects/cr081-word-3d-light.jsonl.gz)"""
import csv, gzip, io, json, os, sys
import numpy as np
import read_light as rl

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, '..', '..', '..', '..'))
OUT = os.path.join(REPO, 'docx4j-chart-render', 'src', 'test', 'resources', 'effects', 'cr081-word-3d-light.jsonl.gz')
STEP = 1.5

def colours(pdf, page):
    rgb, a, box, filt = rl.colours(pdf, page)
    h, w = a.shape
    x0, y0, x1, y1 = box
    xs = np.arange(x0 + STEP / 2, x1, STEP)
    ys = np.arange(y0 + STEP / 2, y1, STEP)
    cells = []
    for y in ys:
        iy = int((y - y0) / (y1 - y0) * h)
        for x in xs:
            ix = int((x - x0) / (x1 - x0) * w)
            if 0 < ix < w - 1 and 0 < iy < h - 1 and a[iy - 1:iy + 2, ix - 1:ix + 2].min() > 0.99:
                c = np.round(rgb[iy, ix] * 255).astype(int)
                cells.append('%02x%02x%02x' % tuple(c))
            else:
                cells.append('......')
    return {'x0': round(x0 + STEP / 2, 3), 'y0': round(y0 + STEP / 2, 3), 'cols': len(xs), 'rows': len(ys), 'step': STEP,
            'jpeg': filt == 'DCTDecode', 'rgb': ''.join(cells)}

SKIP = ('fxg4-03', 'fxg4-07')

def main(r1, r2, r3, r4, r5, out=OUT):
    lines = []
    for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'):
        res = {'cr081-phase5b-r1': r1, 'cr081-phase5b-r2': r2, 'cr081-phase5b-r3': r3, 'cr081-phase5b-r4': r4,
               'cr081-phase5b-r5': r5}.get(p['set'])
        if res is None or p['probe'] in SKIP:
            continue
        pdf = os.path.join(res, f"{p['file']}-a", f"{p['file']}.pdf")
        if not os.path.exists(pdf):
            print('no', pdf, '(left out)')
            continue
        word = colours(pdf, int(p['page']))
        lines.append({'probe': p['probe'], 'note': p['note'], 'w': rl.W, 'h': rl.H, 'shape': 'shape' in p['object'],
                      'geom': 'ellipse' if 'ellipse' in p['object'] else 'rect', 'fill': '4472C4' if 'blue' in p['note'] else '808080',
                      'xml': p['xml'], 'word': word})
    data = ''.join(json.dumps(r, separators=(',', ':')) + '\n' for r in lines).encode('utf-8')
    buf = io.BytesIO()
    with gzip.GzipFile(filename='', mode='wb', fileobj=buf, mtime=0) as g:
        g.write(data)
    open(out, 'wb').write(buf.getvalue())
    print(len(lines), 'probes ->', out, len(buf.getvalue()), 'bytes')

if __name__ == '__main__':
    main(*sys.argv[1:])
