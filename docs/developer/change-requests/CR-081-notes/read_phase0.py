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

The picture styles (cr081-phase0-styles): a 3-D style is drawn as one lit picture in place of the drawing, so there is
no drawing to place it against. Its frame is estimated from the layout instead: the paragraph's clip, which Word sets
to the line, grown by the style's effect extent (styles-word.tsv), plus the gap between them measured on the same
file's flat pages; and the indent, plus the left effect extent. Its rows have dkind 'frame (estimated)' and role 'lit'.

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

def page_of(pdf, page):
    return [d for d in page_draws(pdf, pages(pdf)[page - 1]) if not (d['kind'] != 'image' and d['box'][2] - d['box'][0] > 590)]

def line_clip(draws):
    """The paragraph's clip: Word clips an inline drawing's effects to the line, across the page."""
    clips = [d['clip'] for d in draws if d['clip'] and d['clip'][2] - d['clip'][0] > 590]
    return clips[0] if clips else None

def rows_for(pdf, page, probe, frame=None):
    draws = page_of(pdf, page)
    dr = {'kind': 'frame', 'box': frame} if frame else drawing(draws, probe)
    out = []
    if dr is None:
        return [dict(probe, role='none')], draws
    x0, y0, x1, y1 = dr['box']
    order = draws.index(dr) if not frame else len(draws)
    kind = 'frame (estimated)' if frame else dr['kind'] + ('' if dr['kind'] != 'image' else ' ' + (dr['filter'] or '') + (' smask' if dr['smask'] else ''))
    base = dict(probe, dw=f'{x1 - x0:.2f}', dh=f'{y1 - y0:.2f}', dkind=kind)
    for i, d in enumerate(draws):
        if d is dr:
            continue
        a0, b0, a1, b1 = d['box']
        role = 'lit' if frame else 'under' if i < order else 'over'
        row = dict(base, role=role, kind=d['kind'] if d['kind'] != 'image' else 'image ' + (d['filter'] or ''),
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

STYLES_SET = 'cr081-phase0-styles'
INDENT = 216.0                                # the probes' paragraphs: margin 72 pt plus indent 144 pt

def style_extents():
    """Page -> Word's effect extent for that style (l, t, r, b), in points."""
    rows = csv.DictReader(open(os.path.join(HERE, 'styles-word.tsv'), encoding='utf-8'), delimiter='\t')
    return {i: tuple(int(r[k]) / 12700 for k in 'ltrb') for i, r in enumerate(rows, 1)}

def is_3d(p):
    return '<a:scene3d' in p['xml'] or '<a:sp3d' in p['xml']

def frames(pa, probes):
    """For a styles file: page -> the estimated frame of each 3-D style's drawing. The gap between the line's clip and
    the drawing (below it, the bottom effect extent plus the line's descent; to the left, the indent plus the left
    effect extent) is measured on the file's flat pages, where the drawing is drawn on its own."""
    ee = style_extents()
    gaps = []
    for p in probes:
        if is_3d(p):
            continue
        n = int(p['page'])
        draws = page_of(pa, n)
        dr, clip = drawing(draws, p), line_clip(draws)
        if dr and clip and dr['kind'] == 'image' and abs(dr['box'][2] - dr['box'][0] - W) < 0.6:
            gaps.append((dr['box'][1] - clip[1] - ee[n][3], dr['box'][0] - INDENT - ee[n][0]))
    if not gaps:
        return {}
    dy = float(np.median([g[0] for g in gaps]))
    dx = float(np.median([g[1] for g in gaps]))
    spread = max(abs(g[0] - dy) for g in gaps), max(abs(g[1] - dx) for g in gaps)
    print(os.path.basename(pa), f'frame estimate from {len(gaps)} flat pages: below {dy:.2f} pt, left {dx:.2f} pt, '
          f'spread {spread[0]:.2f} and {spread[1]:.2f} pt')
    out = {}
    for p in probes:
        if not is_3d(p):
            continue
        n = int(p['page'])
        clip = line_clip(page_of(pa, n))
        if clip:
            x0, y0 = INDENT + dx + ee[n][0], clip[1] + dy + ee[n][3]
            out[n] = (x0, y0, x0 + W, y0 + H)
    return out

def main(results, only):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'))
    out = []
    est = {}
    for p in probes:
        if only and p['set'] not in only:
            continue
        pa = os.path.join(results, f"{p['file']}-a", f"{p['file']}.pdf")
        pb = os.path.join(results, f"{p['file']}-b", f"{p['file']}.pdf")
        if not os.path.exists(pa):
            continue
        if p['set'] == STYLES_SET and p['file'] not in est:
            est[p['file']] = frames(pa, [q for q in probes if q['file'] == p['file']])
        base = {k: p[k] for k in ('set', 'file', 'page', 'probe', 'object', 'note', 'extent')}
        rows, _ = rows_for(pa, int(p['page']), base, est.get(p['file'], {}).get(int(p['page'])))
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
