# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phases 1 and 2: docx4j's PDF of the phase 0 probes against Word's, page by page.

For each probe page: the box each draws (the probes' 0.25 pt grey line), and the glyphs drawn as paths (fills and
strokes, the box's line left out): the largest distance either way between docx4j's outline and Word's (each
flattened, point to segment), and the mean from docx4j's to Word's, in points, as drawn (no alignment), and again
with docx4j's moved by the difference of the two boxes' top left corners (the place, separated from the shape). A
page where neither draws paths compares the text instead: each visible character's origin (mutool stext), matched
in order.

usage: gate_wordart.py <Word's results dir> <docx4j's PDF dir> [files...]   (writes gate-<label>.tsv to the PDF dir)
       e.g. gate_wordart.py ~/scratch/p0/results ~/scratch/out1 wa15-warps-fixed-1"""
import csv, functools, os, sys
import numpy as np
import warp_model as wm

# each PDF's pages read once (word_page reads them all for every page)
wm.pd.pages = functools.lru_cache(maxsize=64)(wm.pd.pages)

HERE = os.path.dirname(os.path.abspath(__file__))

def contours(page):
    out = []
    for item in page['fills'] + page['strokes']:
        out += [c for c in item['contours'] if len(c) > 1]
    return out

def visible_text(page):
    return [c for c in page['text'] if c['alpha'] != '#00' and c['c'].strip()]

def compare(word, d4j):
    r = {}
    wb, db = word['box'], d4j['box']
    if wb and db:
        r['word_box'] = '%.1f,%.1f,%.1f,%.1f' % wb
        r['d4j_box'] = '%.1f,%.1f,%.1f,%.1f' % db
        r['box_dw'] = round((db[2] - db[0]) - (wb[2] - wb[0]), 2)
        r['box_dh'] = round((db[3] - db[1]) - (wb[3] - wb[1]), 2)
    wc, dc = contours(word), contours(d4j)
    r['word_paths'], r['d4j_paths'] = len(wc), len(dc)
    if wc and dc:
        dp, wp = np.concatenate(dc), np.concatenate(wc)
        d1, d2 = wm.distance(dp, wc), wm.distance(wp, dc)
        r['max'] = round(float(max(d1.max(), d2.max())), 3)
        r['mean'] = round(float(d1.mean()), 3)
        if wb and db:
            off = np.array([wb[0] - db[0], wb[1] - db[1]])
            moved = [c + off for c in dc]
            e1, e2 = wm.distance(np.concatenate(moved), wc), wm.distance(wp, moved)
            r['max_boxed'] = round(float(max(e1.max(), e2.max())), 3)
    wt, dt = visible_text(word), visible_text(d4j)
    r['word_chars'], r['d4j_chars'] = len(wt), len(dt)
    if not wc and not dc and wt and len(wt) == len(dt):
        e = [np.hypot(a['x'] - b['x'], a['y'] - b['y']) for a, b in zip(wt, dt)]
        r['text_max'] = round(float(max(e)), 3)
        r['text_mean'] = round(float(np.mean(e)), 3)
    return r

def main(results, pdfs, files=None):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv'), encoding='utf-8'), delimiter='\t'))
    rows = []
    for p in probes:
        job = p['file']
        if files and job not in files:
            continue
        if not p['page'].isdigit():
            continue
        wpdf = os.path.join(results, job, job + '.pdf')
        dpdf = os.path.join(pdfs, job + '.pdf')
        if not (os.path.exists(wpdf) and os.path.exists(dpdf)):
            continue
        page = int(p['page'])
        try:
            r = compare(wm.word_page(wpdf, page), wm.word_page(dpdf, page))
        except Exception as e:                                                # noqa: BLE001
            r = {'error': str(e)[:80]}
        rows.append({'probe': p['probe'], 'note': p['note'], **r})
    keys = ['probe', 'note', 'word_box', 'd4j_box', 'box_dw', 'box_dh', 'word_paths', 'd4j_paths', 'max', 'mean', 'max_boxed',
            'word_chars', 'd4j_chars', 'text_max', 'text_mean', 'error']
    label = '-'.join(files) if files and len(files) < 3 else 'all'
    out = os.path.join(pdfs, 'gate-%s.tsv' % label)
    with open(out, 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, keys, delimiter='\t', lineterminator='\n', restval='')
        w.writeheader()
        w.writerows(rows)
    m = [r['max'] for r in rows if 'max' in r]
    print(out, len(rows), 'probes;', len(m), 'with paths;', sum(x <= 0.5 for x in m), 'within 0.5 pt,', sum(x <= 1.0 for x in m),
          'within 1.0 pt')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2], sys.argv[3:] or None)
