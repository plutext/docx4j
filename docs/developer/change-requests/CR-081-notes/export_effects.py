# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Writes CR-081 phase 1's test oracle: Word's effect pictures for the probes of a rectangular picture, read from
Word's PDFs of phase 0 (read_phase0.py's reading of the page, pdfdraws.py's pixels).

For each probe of the families below, on its page of the -a copy's PDF: the drawing's box, and each effect picture
Word draws, under or over the drawing (a soft edge's is the drawing's own picture, role 'replace'):
    role, x and y (its top left, points from the drawing box's top left, y down), w and h (points), pw and ph (its
    pixels), pxpt, amax (its largest alpha), colour (hex, un-premultiplied, where alpha is over half its largest),
    clipped (whether a clip cuts it), and its alpha (0..255) along rows and columns at 1/4, 1/2 and 3/4 of its height
    and width, at its own pixels: rows {"0.25": [...], ...}, cols likewise; mass, the mean alpha.
One JSON object a line, gzip, no name or time:
    probe, file, note, object, extent, xml (the probe's effect XML, probes.tsv), dw, dh, effects [...]

usage: export_effects.py <results dir: one folder a job, <file>-a/<file>.pdf> [--out FILE] [--files f1,f2,...]
(default out: docx4j-chart-render/src/test/resources/effects/cr081-word-effects.jsonl.gz; default files: phase 1's
rectangular-picture families, FILES)"""
import csv, gzip, io, json, os, sys
import numpy as np
from read_phase0 import page_of, drawing, near
from pdfdraws import pixels

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, '..', '..', '..', '..', 'docx4j-chart-render', 'src', 'test', 'resources', 'effects',
                   'cr081-word-effects.jsonl.gz')
# phase 1: a rectangular picture's effects (round 2 where it re-ran a family with room for the effects)
FILES = ['fxr10-outer-offset', 'fxr11-outer-blur', 'fxr12-outer-scale', 'fxr13-outer-colour', 'fx20-inner', 'fxr30-glow',
         'fx40-softedge', 'fxr50-reflection', 'fxr52-reflection-blur', 'fxr53-reflection-offset', 'fxr70-combo', 'fxr72-alpha']
FRACS = (0.25, 0.5, 0.75)

def profiles(alpha):
    h, w = alpha.shape
    q = np.rint(alpha * 255).astype(int)
    rows = {str(f): q[min(h - 1, int(f * h)), :].tolist() for f in FRACS}
    cols = {str(f): q[:, min(w - 1, int(f * w))].tolist() for f in FRACS}
    return rows, cols

def effect(pdf, d, box, role):
    x0, y0, x1, y1 = box
    a0, b0, a1, b1 = d['box']
    e = {'role': role, 'x': round(a0 - x0, 3), 'y': round(y1 - b1, 3), 'w': round(a1 - a0, 3), 'h': round(b1 - b0, 3)}
    c = d['clip']
    e['clipped'] = bool(c and (c[0] > a0 + 0.1 or c[1] > b0 + 0.1 or c[2] < a1 - 0.1 or c[3] < b1 - 0.1))
    if d['kind'] != 'image':
        e['kind'] = d['kind']
        e['colour'] = ' '.join(f'{v:.3f}' for v in (d['colour'] or ()))
        return e
    e['kind'] = 'image'
    e['pw'], e['ph'] = d['w'], d['h']
    e['pxpt'] = round(d['w'] / (a1 - a0), 4)
    rgb, alpha = pixels(pdf, d['obj'])
    if alpha is None:
        return e
    e['amax'] = round(float(alpha.max()), 4)
    if rgb is not None and alpha.max() > 0:
        sel = alpha > alpha.max() / 2
        e['colour'] = '%02X%02X%02X' % tuple(int(round(v * 255)) for v in rgb[sel].mean(0)[:3])
    e['rows'], e['cols'] = profiles(alpha)
    e['mass'] = round(float(alpha.mean()), 5)
    return e

def main(results, out, files=FILES):
    probes = [r for r in csv.DictReader(open(os.path.join(HERE, 'probes.tsv'), encoding='utf-8'), delimiter='\t')
              if r['file'] in files]
    lines = []
    for f in files:
        pdf = os.path.join(results, f + '-a', f + '.pdf')
        for p in [p for p in probes if p['file'] == f]:
            draws = page_of(pdf, int(p['page']))
            dr = drawing(draws, p)
            rec = {k: p[k] for k in ('probe', 'file', 'note', 'object', 'extent', 'xml')}
            if dr is None:
                rec['effects'] = []
                lines.append(rec)
                continue
            box = dr['box']
            rec['dw'], rec['dh'] = round(box[2] - box[0], 3), round(box[3] - box[1], 3)
            order = draws.index(dr)
            effects = [effect(pdf, d, box, 'under' if i < order else 'over') for i, d in enumerate(draws) if d is not dr]
            # a soft edge redraws the picture itself with a soft mask: its alpha is the effect
            if dr['kind'] == 'image' and dr.get('smask'):
                effects.append(effect(pdf, dr, box, 'replace'))
            rec['effects'] = effects
            lines.append(rec)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    data = ''.join(json.dumps(r, separators=(',', ':')) + '\n' for r in lines).encode('utf-8')
    with open(out, 'wb') as fh:
        with gzip.GzipFile(filename='', mode='wb', fileobj=fh, mtime=0) as g:
            g.write(data)
    print(len(lines), 'probes,', sum(len(r['effects']) for r in lines), 'effects ->', out, os.path.getsize(out), 'bytes')

if __name__ == '__main__':
    a = sys.argv[1:]
    out = OUT
    if '--out' in a:
        i = a.index('--out')
        out = a[i + 1]
        del a[i:i + 2]
    files = FILES
    if '--files' in a:
        i = a.index('--files')
        files = a[i + 1].split(',')
        del a[i:i + 2]
    main(a[0], out, files)
