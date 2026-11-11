# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Reads Word's PDFs of CR-084 phase 0's probes (probes.tsv) into phase0-word.json: for each probe, what Word drew
for the picture, and, for the calibration pictures, each patch's colour beside the source's.

For each page: the pictures Word placed (not the fully transparent artefact some pages carry), with their box (points,
y down), size in pixels, filter (DCTDecode is a JPEG, FlateDecode lossless), soft mask, and, for a calibration
picture, the 256 patch centres sampled from Word's pixels (RGB, un-premultiplied, and alpha, each 0..255) beside the
source's (build_recolour.py's levels). Vector drawing (the EMF, if Word keeps it vector) is counted as fills.

usage: read_recolour.py <results dir: one folder a job, <job>/<job>.pdf> [job ...]"""
import csv, importlib.util, json, os, sys
import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', 'CR-080-notes'))
sys.path.insert(0, os.path.join(HERE, '..', 'CR-081-notes'))
import pdfpaths                                                               # noqa: E402
import pdfdraws as pd                                                         # noqa: E402
_spec = importlib.util.spec_from_file_location('recolour', os.path.join(HERE, 'build_recolour.py'))
rc = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(rc)

LEVELS = {'calib-grey.png': rc.grey_levels, 'calib-colour.png': rc.colour_levels, 'calib-alpha.png': rc.alpha_levels,
          'calib-near.png': rc.near_levels}
X0, Y0 = rc.X0 / rc.EMU, rc.Y0 / rc.EMU
LABEL_BOTTOM = 130.0

def pixels(pdf, obj):
    """(rgb as 0..1 floats, alpha or None): pdfdraws' for Flate; a JPEG's (DCTDecode) too, since mutool's show -b
    decodes it to pixels as it does Flate; a grey picture made RGB."""
    rgb, alpha = pd.pixels(pdf, obj)
    i = pd.image_info(pdf, obj)
    if rgb is None and i['filter'] == 'DCTDecode':
        n = 3 if i['cs'] == 'DeviceRGB' else 1
        raw = pd.show(pdf, obj, raw=True)
        rgb = np.frombuffer(raw, np.uint8)[:i['w'] * i['h'] * n].reshape(i['h'], i['w'], n) / 255.0
        if alpha is not None and pd.alpha_of(pdf, obj)[1]:
            with np.errstate(divide='ignore', invalid='ignore'):
                rgb = np.where(alpha[..., None] > 0, np.clip(rgb / alpha[..., None], 0, 1), 0)
    if rgb is not None and rgb.shape[2] == 1:
        rgb = np.repeat(rgb, 3, axis=2)
    return rgb, alpha

def centres(rgb, alpha, crop=None):
    """The 16 by 16 patch centres of a picture, as fractions of its width and height (crop: the visible part's
    fractions, l t r b, as a:srcRect has them)."""
    h, w = rgb.shape[:2]
    out = []
    for k in range(256):
        fx_, fy_ = (k % 16 + 0.5) / 16, (k // 16 + 0.5) / 16
        if crop:
            l, t, r, b = crop
            if not (l <= fx_ <= 1 - r and t <= fy_ <= 1 - b):
                out.append(None)
                continue
            fx_, fy_ = (fx_ - l) / (1 - l - r), (fy_ - t) / (1 - t - b)
        x, y = min(int(fx_ * w), w - 1), min(int(fy_ * h), h - 1)
        a = alpha[y, x] if alpha is not None else 1.0
        out.append([int(round(v * 255)) for v in rgb[y, x]] + [int(round(a * 255)) if alpha is not None else 255])
    return out

def page(pdf, page_obj, picture, crop):
    h = pdfpaths.page_height(pdf, page_obj)
    pics, fills = [], 0
    for d in pdfpaths.page_paths(pdf, page_obj):
        if d['kind'] == 'image' and d.get('obj') and pd.image_info(pdf, d['obj'])['subtype'] == 'Form':
            b = d['box']
            pics.append({'box': [round(b[0], 2), round(h - b[3], 2), round(b[2], 2), round(h - b[1], 2)], 'form': True})
        elif d['kind'] == 'image' and d.get('obj'):
            rgb, a = pixels(pdf, d['obj'])
            if a is not None and a.max() == 0:
                continue                                          # the transparent artefact
            b = d['box']
            item = {'box': [round(b[0], 2), round(h - b[3], 2), round(b[2], 2), round(h - b[1], 2)], 'w': d.get('w'),
                    'h': d.get('h'), 'filter': d.get('filter'), 'smask': bool(d.get('smask'))}
            cx, cy = (v / rc.EMU for v in rc.frame(picture))
            item['frame'] = abs(b[0] - X0) < 2 and abs(h - b[3] - Y0) < 2 and abs(b[2] - b[0] - cx) < 2 and abs(b[3] - b[1] - cy) < 2
            if picture in LEVELS and item['frame']:               # the picture itself, not an effect's picture
                item['patches'] = centres(rgb, a, crop)
            pics.append(item)
        elif d['kind'] in ('fill', 'fillstroke') and h - d['box'][3] > LABEL_BOTTOM:
            fills += 1
    return pics, fills

def crop_of(summary):
    import re
    m = re.search(r'srcRect l=(\d+) t=(\d+) r=(\d+) b=(\d+)', summary)
    return tuple(int(v) / 100000 for v in m.groups()) if m else None

def main(results, jobs):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv'), encoding='utf-8'), delimiter='\t'))
    jobs = jobs or sorted({p['file'] for p in probes if p['page'] != '-'})
    out = {}
    for job in jobs:
        pdf = os.path.join(results, job, job + '.pdf')
        if not os.path.isfile(pdf):
            print('no pdf', job)
            continue
        objs = pd.pages(pdf)
        for p in [p for p in probes if p['file'] == job]:
            pics, fills = page(pdf, objs[int(p['page']) - 1], p['picture'], crop_of(p['summary']))
            src = LEVELS[p['picture']]() if p['picture'] in LEVELS else None
            out[p['probe']] = {'note': p['note'], 'picture': p['picture'], 'summary': p['summary'], 'pictures': pics,
                               'fills': fills, 'source': [list(c) + ([255] if len(c) == 3 else []) for c in src] if src else None}
        print(job, len([p for p in probes if p['file'] == job]), 'probes')
    with open(os.path.join(HERE, 'phase0-word.json'), 'w', encoding='utf-8') as f:
        json.dump(out, f, separators=(',', ':'))
    print(os.path.join(HERE, 'phase0-word.json'), len(out), 'probes')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
