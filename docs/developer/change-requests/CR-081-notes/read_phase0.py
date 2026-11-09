# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Reads Word's PDFs of CR-081 phase 0's probes into phase0-word.tsv: one row for each thing a probe's page draws
besides its drawing, placed against the drawing's box.

For each probe (probes.tsv), on its page of the -a copy's PDF:
- the drawing: a picture is the image Word clips to its own box; a shape is the fill in the shape's colour; a 3-D
  drawing, a soft edge or a picture with transparency is the image of the drawing's size drawn last;
- every other draw (effect pictures, vector fills) gets a row: before or after the drawing (under or over); its
  extents beyond the drawing's box (l, t, r, b, in points, positive outward); its pixel size and pixels per point;
  for a picture, the largest alpha and the colour (un-premultiplied) where alpha is over half its largest; whether a
  clip cuts it.
The -b copy is compared with the -a copy (placement and pixels), and the result recorded in the row.

usage: read_phase0.py <results dir: one folder a job, <file>-a/<file>.pdf> [set ...]"""
import csv, os, sys
import numpy as np
from pdfdraws import pages, page_draws, pixels

HERE = os.path.dirname(os.path.abspath(__file__))
SHAPE_FILL = (0.267, 0.447, 0.769)            # 4472C4, the shapes' fill
W, H = 144.0, 108.0

def near(a, b, tol=0.06):
    return all(abs(x - y) <= tol for x, y in zip(a, b))

def drawing(draws, probe):
    imgs = [d for d in draws if d['kind'] == 'image']
    if probe['object'].startswith('shape'):
        fills = [d for d in draws if d['kind'] == 'fill' and d['colour'] and len(d['colour']) == 3
                 and near(d['colour'], SHAPE_FILL, 0.01)]
        if fills:
            return fills[-1]
        sized = [d for d in draws if d['kind'] == 'fill' and d['colour'] and len(d['colour']) == 3          # a theme's fill
                 and abs(d['box'][2] - d['box'][0] - W) < 0.6 and abs(d['box'][3] - d['box'][1] - H) < 0.6]
        if sized:
            return sized[-1]
    for d in imgs:
        if d['clip'] and near(d['clip'], d['box']) and abs(d['box'][2] - d['box'][0] - W) < 0.6:
            return d
    sized = [d for d in imgs if abs(d['box'][2] - d['box'][0] - W) < 1.0 and abs(d['box'][3] - d['box'][1] - H) < 1.0]
    if sized:
        return sized[-1]
    return imgs[-1] if imgs else None

def rows_for(pdf, page, probe):
    draws = [d for d in page_draws(pdf, pages(pdf)[page - 1]) if not (d['kind'] != 'image' and d['box'][2] - d['box'][0] > 590)]
    dr = drawing(draws, probe)
    out = []
    if dr is None:
        return [dict(probe, role='none')], draws
    x0, y0, x1, y1 = dr['box']
    order = draws.index(dr)
    base = dict(probe, dw=f'{x1 - x0:.2f}', dh=f'{y1 - y0:.2f}', dkind=dr['kind'] + ('' if dr['kind'] != 'image' else ' ' + (dr['filter'] or '') + (' smask' if dr['smask'] else '')))
    for i, d in enumerate(draws):
        if d is dr:
            continue
        a0, b0, a1, b1 = d['box']
        row = dict(base, role='under' if i < order else 'over', kind=d['kind'] if d['kind'] != 'image' else 'image ' + (d['filter'] or ''),
                   l=f'{x0 - a0:.2f}', t=f'{b1 - y1:.2f}', r=f'{a1 - x1:.2f}', b=f'{y0 - b0:.2f}', w=f'{a1 - a0:.2f}', h=f'{b1 - b0:.2f}')
        c = d['clip']
        row['clipped'] = 'yes' if c and (c[0] > a0 + 0.1 or c[1] > b0 + 0.1 or c[2] < a1 - 0.1 or c[3] < b1 - 0.1) else ''
        if d['kind'] == 'image':
            row['px'] = f"{d['w']}x{d['h']}"
            row['pxpt'] = f"{d['w'] / (a1 - a0):.3f}"
            rgb, alpha = pixels(pdf, d['obj'])
            if alpha is not None:
                row['amax'] = f'{alpha.max():.3f}'
                if rgb is not None and alpha.max() > 0:
                    sel = alpha > alpha.max() / 2
                    row['colour'] = '%02X%02X%02X' % tuple(int(round(v * 255)) for v in rgb[sel].mean(0)[:3])
        else:
            row['colour'] = ' '.join(f'{v:.3f}' for v in (d['colour'] or ()))
        out.append(row)
    if not out:
        out.append(dict(base, role='nothing'))
    return out, draws

def same(pa, pb, page):
    da = page_draws(pa, pages(pa)[page - 1])
    db = page_draws(pb, pages(pb)[page - 1])
    if len(da) != len(db):
        return 'count'
    for x, y in zip(da, db):
        if not near(x['box'], y['box'], 0.005):
            return 'placement'
        if x['kind'] == 'image' and x.get('smask'):
            ra, aa = pixels(pa, x['obj'])
            rb, ab = pixels(pb, y['obj'])
            if not np.array_equal(aa, ab) or (ra is not None and not np.array_equal(ra, rb)):
                return 'pixels'
    return 'same'

COLS = ['set', 'file', 'page', 'probe', 'object', 'note', 'extent', 'dkind', 'dw', 'dh', 'role', 'kind', 'l', 't', 'r', 'b',
        'w', 'h', 'px', 'pxpt', 'amax', 'colour', 'clipped', 'ab']

def main(results, only):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'))
    out = []
    for p in probes:
        if only and p['set'] not in only:
            continue
        pa = os.path.join(results, f"{p['file']}-a", f"{p['file']}.pdf")
        pb = os.path.join(results, f"{p['file']}-b", f"{p['file']}.pdf")
        if not os.path.exists(pa):
            continue
        base = {k: p[k] for k in ('set', 'file', 'page', 'probe', 'object', 'note', 'extent')}
        rows, _ = rows_for(pa, int(p['page']), base)
        ab = same(pa, pb, int(p['page'])) if os.path.exists(pb) else 'no b'
        for r in rows:
            r['ab'] = ab
        out += rows
    with open(os.path.join(HERE, 'phase0-word.tsv'), 'w', newline='') as f:
        w = csv.DictWriter(f, COLS, delimiter='\t', extrasaction='ignore', lineterminator='\n')
        w.writeheader()
        w.writerows(out)
    print(len(out), 'rows')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
