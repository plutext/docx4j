# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5a, round 3: Word's 3-D pictures with their effects as docx4j-chart-render's test oracle
(Scene3DTest.theEffectsAreInTheDrawingsPlane). For each shape probe of fxe2 that Word draws as one 3-D picture: its XML,
and the picture's alpha (a greyscale PNG, base64) with its place, points from the frame's top left.

One JSON object a line, gzip, no name or time.

usage: export_3d_effects.py <round 3 results dir> [out]
(default out: docx4j-chart-render/src/test/resources/effects/cr081-word-3d-effects.jsonl.gz)"""
import base64, csv, gzip, io, json, os, sys
import numpy as np
from PIL import Image
import pdfdraws as pd, read_3d as r3

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.normpath(os.path.join(HERE, '..', '..', '..', '..'))
OUT = os.path.join(REPO, 'docx4j-chart-render', 'src', 'test', 'resources', 'effects', 'cr081-word-3d-effects.jsonl.gz')

def main(results, out=OUT):
    pdf = os.path.join(results, 'fxe2-shadow-3d-a', 'fxe2-shadow-3d.pdf')
    lines = []
    for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'):
        if not p['probe'].startswith('fxe2') or 'shape' not in p['object']:
            continue
        d = r3.the_picture(pdf, int(p['page']))
        if d is None or not pd.image_info(pdf, d['obj']).get('smask'):
            continue
        _, a = pd.pixels(pdf, d['obj'])
        if a.max() < 0.75:
            continue                     # a flat drawing: its shadow drawn apart
        x0, y0, x1, y1 = d['box']
        buf = io.BytesIO()
        Image.fromarray(np.round(a * 255).astype(np.uint8), 'L').save(buf, 'PNG', optimize=True)
        lines.append({'probe': p['probe'], 'note': p['note'], 'xml': p['xml'],
                      'word': {'alpha': base64.b64encode(buf.getvalue()).decode('ascii'),
                               'place': [round(x0 - r3.FIXED[0], 3), round(r3.PAGE_H - y1 - r3.FIXED[1], 3), round(x1 - x0, 3), round(y1 - y0, 3)]}})
    data = ''.join(json.dumps(r, separators=(',', ':')) + '\n' for r in lines).encode('utf-8')
    b = io.BytesIO()
    with gzip.GzipFile(filename='', mode='wb', fileobj=b, mtime=0) as g:
        g.write(data)
    open(out, 'wb').write(b.getvalue())
    print(len(lines), 'probes ->', out, len(b.getvalue()), 'bytes')

if __name__ == '__main__':
    main(*sys.argv[1:])
