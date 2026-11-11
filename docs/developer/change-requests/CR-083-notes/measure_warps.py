# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 0: Word's warped outlines against the model of 20.1.9.19 (warp_model.py), probe by probe.

The model, as measured (phase0-findings.md section 3):
- the preset is evaluated on the text rectangle (the box Word drew, less the insets), with Word's two departures from
  the shipped definitions: ss is the height in textDeflate and textDeflateBottom;
- the lines of text form one block, and the block's ink (the glyphs' outlines, no side bearings, no spaces) is the
  rectangle mapped;
- a point's horizontal fraction is measured by arc length along each path, and its vertical fraction down the
  straight line joining the two points;
- the outlines are the font's own, unhinted, warped densely.

The unwarped positions are Word's invisible text: Word clips each line's invisible text, and mutool's stext loses the
lines it finds clipped, so each file's layout is taken from one page that has every line (TEMPLATE), moved by the
difference between the two pages' boxes.

For each probe of a two-path preset: the largest distance either way between the model's outline and Word's, and the
mean distance from the model's to Word's, in points. One-path presets (Follow Path) and the presets with more than two
paths are listed without a number (sections 4 and 5).

usage: measure_warps.py <results dir> <font file (Word's embedded Aptos subset)>   (writes warps-word.tsv here)"""
import csv, os, sys
import numpy as np
import warp_model as wm

HERE = os.path.dirname(os.path.abspath(__file__))
INS = (7.2, 3.6, 7.2, 3.6)
SS_IS_H = {'textDeflate', 'textDeflateBottom'}
ORIGIN = (-0.07, -0.10)              # Word's glyphs sit this far from stext's origin (measured on unwarped outlines)
TEMPLATE = {'wa10-warps-default-1': 30, 'wa11-warps-default-2': 7, 'wa15-warps-fixed-1': 30, 'wa16-warps-fixed-2': 7,
            'wa12-warps-adjusted-1': 30, 'wa13-warps-adjusted-2': 7}

def adj_of(probe):
    a = probe['summary'].split(' adj ')[1].split(';')[0]
    return {} if a == 'default' else {k: float(v) for k, v in (x.split('=') for x in a.split())}

def layout(results, job):
    pdf = os.path.join(results, job, job + '.pdf')
    w = wm.word_page(pdf, TEMPLATE[job])
    return w['box'], [c for c in w['text'] if c['alpha'] == '#00']

def measure(results, font, job, page, probe, tmpl):
    pdf = os.path.join(results, job, job + '.pdf')
    name = probe['summary'].split()[0]
    paths_n = len(wm.preset_paths(name, 100, 100))
    if paths_n != 2:
        return {'paths': paths_n}
    w = wm.word_page(pdf, page)
    bx = w['box']
    tr = (bx[0] + INS[0], bx[1] + INS[1], bx[2] - INS[2], bx[3] - INS[3])
    W, H = tr[2] - tr[0], tr[3] - tr[1]
    tbx, tinv = tmpl
    dx, dy = bx[0] - tbx[0] + ORIGIN[0], bx[1] - tbx[1] + ORIGIN[1]
    adj = adj_of(probe)
    if name in SS_IS_H:
        adj['ss'] = H
    top, bottom = [p + np.array([tr[0], tr[1]]) for p in wm.preset_paths(name, W, H, adj)]
    gl = wm.glyphs(font, [(c['c'], c['size'], c['x'] + dx, c['y'] + dy) for c in tinv])
    ink = wm.bounds(gl)
    pred = [wm.warp(c, ink, top, bottom) for c in gl]
    word = [c for f in w['fills'] for c in f['contours']]
    d1 = wm.distance(np.concatenate(pred), word)
    d2 = wm.distance(np.concatenate(word), pred)
    return {'paths': 2, 'box_w': round(bx[2] - bx[0], 2), 'box_h': round(bx[3] - bx[1], 2), 'max': round(float(max(d1.max(), d2.max())), 3),
            'mean': round(float(d1.mean()), 3)}

def main(results, font):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv'), encoding='utf-8'), delimiter='\t'))
    out = []
    for job in TEMPLATE:
        tmpl = layout(results, job)
        for p in [p for p in probes if p['file'] == job]:
            r = measure(results, font, job, int(p['page']), p, tmpl)
            out.append({'probe': p['probe'], 'note': p['note'], **r})
        print(job, 'measured')
    keys = ['probe', 'note', 'paths', 'box_w', 'box_h', 'max', 'mean']
    with open(os.path.join(HERE, 'warps-word.tsv'), 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, keys, delimiter='\t', lineterminator='\n', restval='')
        w.writeheader()
        w.writerows(out)
    two = [r for r in out if r['paths'] == 2]
    for lim in (0.2, 0.5, 1.0):
        print(f'within {lim} pt: {sum(r["max"] <= lim for r in two)} of {len(two)}')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
