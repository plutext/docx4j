# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Compares docx4j's PDFs of CR-081's phase 1 probes with Word's effect pictures (export_effects.py's oracle): for each
probe, on its page, each effect picture docx4j draws against Word's, by layer and order: its place against the
drawing's box (dx, dy, dw, dh in points), its pixels per point, its largest alpha, its colour, and its alpha along Word's
three rows and three columns, sampled at Word's pixels (mean and max absolute difference, 0 to 255, each profile at its
best of five phases a Word pixel either way, as EffectsTest compares).

docx4j's drawing on a page is its picture: the image in the drawing's frame (its box, or its clip where a crop makes
the bitmap's box larger) with no soft mask, else of the stored bitmap's pixel size; or, where a soft edge replaces it,
the last image of the drawing's size.

usage: compare_effects.py <docx4j pdf dir: <file>.pdf> [--oracle FILE] [--all]"""
import gzip, json, os, sys
import numpy as np
from pdfdraws import pages, page_draws, image_info, pixels

HERE = os.path.dirname(os.path.abspath(__file__))
ORACLE = os.path.join(HERE, '..', '..', '..', '..', 'docx4j-chart-render', 'src', 'test', 'resources', 'effects',
                      'cr081-word-effects.jsonl.gz')
W, H = 144.0, 108.0
SOURCE_PX = {(480, 360)}

def page_of(pdf, page):
    return [d for d in page_draws(pdf, pages(pdf)[page - 1]) if d['kind'] == 'image']

def drawing(imgs, pdf):
    """(the drawing, its frame (PDF space), how found): the stored bitmap's image, its frame its clip where a crop
    makes the bitmap's box larger than the frame; else the soft-edged picture in its place"""
    def framed(r):
        return r and abs(r[2] - r[0] - W) < 0.6 and abs(r[3] - r[1] - H) < 0.6
    # in its frame (its box, or its clip where a crop leaves the bitmap larger): one with no soft mask (an effect
    # picture always has one), else one of the bitmap's pixel size (a transparent picture's own mask), which an effect
    # picture can share (a 120% shadow)
    framed_imgs = [d for d in imgs if framed(d['clip']) or framed(d['box'])]
    plain = [d for d in framed_imgs if not image_info(pdf, d['obj'])['smask']]
    sized = [d for d in framed_imgs if (image_info(pdf, d['obj'])['w'], image_info(pdf, d['obj'])['h']) in SOURCE_PX]
    for d in plain + sized:
        return d, d['clip'] if framed(d['clip']) else d['box'], 'picture'
    sized = [d for d in imgs if abs(d['box'][2] - d['box'][0] - W) < 0.6 and abs(d['box'][3] - d['box'][1] - H) < 0.6]
    return (sized[-1], sized[-1]['box'], 'replace') if sized else (None, None, None)

def alpha_at(alpha, box, x, y):
    """alpha (rows top first) of an image drawn in box (PDF space, y up) at frame point (x, y) (from the drawing's
    top left, y down, given here already as PDF x and y)"""
    h, w = alpha.shape
    fx = (x - box[0]) / (box[2] - box[0]) * w - 0.5
    fy = (box[3] - y) / (box[3] - box[1]) * h - 0.5
    x0, y0 = int(np.floor(fx)), int(np.floor(fy))
    tx, ty = fx - x0, fy - y0
    def a(xx, yy):
        return 0.0 if xx < 0 or yy < 0 or xx >= w or yy >= h else alpha[yy, xx] * 255
    return (1 - ty) * ((1 - tx) * a(x0, y0) + tx * a(x0 + 1, y0)) + ty * ((1 - tx) * a(x0, y0 + 1) + tx * a(x0 + 1, y0 + 1))

def compare(pdf, d, dbox, word):
    i = image_info(pdf, d['obj'])
    rgb, alpha = pixels(pdf, d['obj'])
    a0, b0, a1, b1 = d['box']
    out = {'dx': (a0 - dbox[0]) - word['x'], 'dy': (dbox[3] - b1) - word['y'], 'dw': (a1 - a0) - word['w'], 'dh': (b1 - b0) - word['h']}
    if word.get('kind') != 'image' or 'pw' not in word:
        return out
    out['ppt'] = (i['w'] / (a1 - a0)) / word['pxpt'] if i['w'] else 0
    if alpha is None:
        alpha = np.ones((i['h'], i['w']))
    out['amax'] = float(alpha.max())
    if rgb is not None and alpha.max() > 0:
        sel = alpha > alpha.max() / 2
        out['colour'] = '%02X%02X%02X' % tuple(int(round(v * 255)) for v in rgb[sel].mean(0)[:3])
    wx, wy, ww, wh, wpw, wph = word['x'], word['y'], word['w'], word['h'], word['pw'], word['ph']
    total, worst, n = 0.0, 0.0, 0
    for axis in ('rows', 'cols'):
        for f, values in (word.get(axis) or {}).items():
            f = float(f)
            best = None
            for shift in (0, -0.5, 0.5, -1, 1):
                s, m = 0.0, 0.0
                for k, v in enumerate(values):
                    if axis == 'rows':
                        x = wx + (k + 0.5 + shift) * ww / wpw
                        y = wy + (min(wph - 1, np.floor(f * wph)) + 0.5) * wh / wph
                    else:
                        x = wx + (min(wpw - 1, np.floor(f * wpw)) + 0.5) * ww / wpw
                        y = wy + (k + 0.5 + shift) * wh / wph
                    e = abs(alpha_at(alpha, d['box'], dbox[0] + x, dbox[3] - y) - v)
                    s += e
                    m = max(m, e)
                if best is None or s < best[0]:
                    best = (s, m)
            total += best[0]
            worst = max(worst, best[1])
            n += len(values)
    out['mean'] = total / n if n else 0
    out['max'] = worst
    return out

def main(pdfdir, oracle, show_all):
    recs = [json.loads(l) for l in gzip.open(oracle)]
    pages_of = {}
    for r in recs:
        pages_of.setdefault(r['file'], []).append(r)
    means = []
    for f, rs in pages_of.items():
        pdf = os.path.join(pdfdir, f + '.pdf')
        if not os.path.exists(pdf):
            print(f, 'no PDF')
            continue
        npages = len(pages(pdf))
        for i, r in enumerate(rs, 1):
            page = r.get('page', i)
            if 'turned' in r['note']:
                continue
            if page > npages:
                print(r['probe'], 'no page')
                continue
            imgs = page_of(pdf, page)
            dr, frame, how = drawing(imgs, pdf)
            if dr is None:
                print(r['probe'], 'no drawing found')
                continue
            order = imgs.index(dr)
            mine = {'under': [d for d in imgs[:order]], 'over': [d for d in imgs[order + 1:]], 'replace': [dr] if how == 'replace' else []}
            soft = 'softEdge' in r['xml']
            word = {'under': [], 'over': [], 'replace': []}
            for e in r['effects']:
                if e['role'] == 'replace' and not soft or e.get('kind') != 'image':
                    continue
                word[e['role']].append(e)
            for role in ('under', 'replace', 'over'):
                if len(mine[role]) != len(word[role]):
                    print(f"{r['probe']:9s} {role:7s} COUNT docx4j {len(mine[role])} Word {len(word[role])}  | {r['note']}")
                    continue
                for d, e in zip(mine[role], word[role]):
                    c = compare(pdf, d, frame, e)
                    means.append(c.get('mean', 0))
                    flag = (abs(c['dx']) > 1 or abs(c['dy']) > 1 or abs(c['dw']) > 2.2 or abs(c['dh']) > 2.2
                            or c.get('mean', 0) > 3.5 or abs(c.get('amax', e.get('amax', 0)) - e.get('amax', 0)) > 0.025)
                    if flag or show_all:
                        print(f"{r['probe']:9s} {role:7s} place {c['dx']:+6.2f} {c['dy']:+6.2f} {c['dw']:+6.2f} {c['dh']:+6.2f}"
                              f"  ppt x{c.get('ppt', 0):.3f}  alpha mean {c.get('mean', 0):5.1f} max {c.get('max', 0):5.1f}"
                              f"  amax {c.get('amax', 0):.3f}/{e.get('amax', 0):.3f}  {c.get('colour')}/{e.get('colour')}  | {r['note']}")
    if means:
        m = sorted(means)
        print(f"{len(m)} effect pictures: alpha mean {sum(m) / len(m):.2f}, median {m[len(m) // 2]:.2f}, p90 {m[int(len(m) * 0.9)]:.2f}, worst {m[-1]:.2f}")

if __name__ == '__main__':
    a = sys.argv[1:]
    oracle = ORACLE
    if '--oracle' in a:
        i = a.index('--oracle')
        oracle = a[i + 1]
        del a[i:i + 2]
    show_all = '--all' in a
    a = [x for x in a if x != '--all']
    main(a[0], oracle, show_all)
