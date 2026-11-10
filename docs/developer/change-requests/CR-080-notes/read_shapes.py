# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Reads Word's PDFs of CR-080 phase 0's probes (probes.tsv) into:
- phase0-word.json: for each probe, what its page draws, in the shape's own frame: points from the shape's top left,
  y down, in points. Fills and strokes keep every segment (to 0.01 pt), their colours, line width, cap, join, dash and
  alpha; shadings their dictionary; pictures their box. For the text probes (fp60, fp61), the glyph's box.
- phase0-word.tsv: a line a probe: how many fills, strokes, shadings and pictures; the box of the fills and of the
  strokes; the line's width, cap, join and dash; the fill colours; the glyph's box.
Every shape is anchored at (180 pt, 252 pt) from the page's top left (build_shapes.py), so its frame is known exactly.

usage: read_shapes.py [--out DIR] <results dir: one folder a job, <file>/<file>.pdf> [file ...]
--out writes the two files to DIR (another renderer's PDFs, say: CR-080 phase 2 reads docx4j's so)"""
import csv, json, os, re, subprocess, sys
import xml.etree.ElementTree as ET
import pdfpaths

HERE = os.path.dirname(os.path.abspath(__file__))
X0, Y0 = 180.0, 252.0

def local(h):
    top = h - Y0
    def f(x, y):
        return round(x - X0, 2), round(top - y, 2)
    return f

def seg_local(seg, f):
    out = [seg[0]]
    for i in range(1, len(seg), 2):
        out += list(f(seg[i], seg[i + 1]))
    return out

def hexcol(c):
    if not c or c[0] == 'pattern':
        return c[1] if c else ''
    if len(c) == 1:
        c = (c[0],) * 3
    return '%02X%02X%02X' % tuple(int(round(v * 255)) for v in c[:3]) if len(c) >= 3 else ''

def glyphs(pdf):
    """Page number -> the characters mutool reads, each (c, x0, y0, x1, y1, size) in page points from the top left."""
    xml = subprocess.run(['mutool', 'draw', '-q', '-F', 'stext', '-o', '-', pdf], capture_output=True, check=True).stdout
    out = {}
    for n, page in enumerate(ET.fromstring(xml).iter('page'), 1):
        chars = []
        for font in page.iter('font'):
            size = float(font.get('size', 0))
            for ch in font.iter('char'):
                q = [float(v) for v in ch.get('quad').split()]
                chars.append((ch.get('c'), min(q[0::2]), min(q[1::2]), max(q[0::2]), max(q[1::2]), size))
        out[n] = chars
    return out

def main(results, only, out_dir=HERE):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'))
    seen, uniq = set(), []                      # a file staged in two sets (fp21) is read once
    for p in probes:
        if (p['file'], p['page']) not in seen:
            seen.add((p['file'], p['page']))
            uniq.append(p)
    probes = uniq
    files = sorted({p['file'] for p in probes if not only or p['file'] in only})
    data, rows = {}, []
    for name in files:
        pdf = os.path.join(results, name, name + '.pdf')
        if not os.path.exists(pdf):
            continue
        pages = pdfpaths.pd.pages(pdf)
        text = glyphs(pdf) if name.startswith(('fp6', 'fp7', 'fp90', 'fp91', 'fp92')) else {}
        for p in (q for q in probes if q['file'] == name):
            n = int(p['page'])
            po = pages[n - 1]
            f = local(pdfpaths.page_height(pdf, po))
            out = []
            for d in pdfpaths.page_paths(pdf, po):
                if d['kind'] in ('fill', 'stroke', 'fillstroke'):
                    b = d['box']
                    (l, t), (r, bt) = f(b[0], b[3]), f(b[2], b[1])
                    out.append({'kind': d['kind'], 'op': d['op'], 'box': [l, t, r, bt],
                                'subpaths': [[seg_local(s, f) for s in sp] for sp in d['subpaths']],
                                'fill': hexcol(d['fill']), 'stroke': hexcol(d['stroke']), 'lw': round(d['lw'], 3),
                                'cap': d['cap'], 'join': d['join'], 'miter': d['miter'],
                                'dash': [[round(v, 3) for v in d['dash'][0]], round(d['dash'][1], 3)],
                                'alpha': list(d['alpha']),
                                'pattern': {str(k): v for k, v in (d.get('pattern') or {}).items()} or None})
                elif d['kind'] == 'shading':
                    out.append({'kind': 'shading', 'dict': d['dict'], 'ctm': [round(v, 4) for v in d['ctm']],
                                'alpha': list(d['alpha'])})
                elif d['kind'] == 'image':
                    b = d['box']
                    (l, t), (r, bt) = f(b[0], b[3]), f(b[2], b[1])
                    out.append({'kind': 'image', 'box': [l, t, r, bt], 'px': [d.get('w'), d.get('h')],
                                'filter': d.get('filter'), 'smask': bool(d.get('smask'))})
            glyph = None
            for c, gx0, gy0, gx1, gy1, size in text.get(n, []):
                if c == 'X' and abs(size - 8) < 0.5 and gy0 > Y0 - 40:          # the probe's glyph, not the label's
                    glyph = [round(gx0 - X0, 2), round(gy0 - Y0, 2), round(gx1 - X0, 2), round(gy1 - Y0, 2)]
            data[p['probe']] = {'file': name, 'page': n, 'note': p['note'], 'size': p['size'], 'draws': out, 'glyph': glyph}
            kinds = [d['kind'] for d in out]
            def ubox(ds):
                bs = [d['box'] for d in ds]
                return ' '.join(f'{v:.2f}' for v in (min(b[0] for b in bs), min(b[1] for b in bs),
                                                     max(b[2] for b in bs), max(b[3] for b in bs))) if bs else ''
            fills = [d for d in out if d['kind'] in ('fill', 'fillstroke')]
            strokes = [d for d in out if d['kind'] in ('stroke', 'fillstroke')]
            s0 = strokes[0] if strokes else {}
            rows.append({'file': name, 'page': n, 'probe': p['probe'], 'note': p['note'], 'size': p['size'],
                         'fills': kinds.count('fill') + kinds.count('fillstroke'), 'strokes': len(strokes),
                         'shadings': kinds.count('shading'), 'images': kinds.count('image'),
                         'fill_box': ubox(fills), 'stroke_box': ubox(strokes),
                         'lw': s0.get('lw', ''), 'cap': s0.get('cap', ''), 'join': s0.get('join', ''),
                         'dash': ' '.join(f'{v:g}' for v in s0['dash'][0]) if s0 else '',
                         'fill_colours': ' '.join(sorted({d['fill'] for d in fills if d['fill']})),
                         'glyph': ' '.join(f'{v:.2f}' for v in glyph) if glyph else ''})
    with open(os.path.join(out_dir, "phase0-word.json"), "w") as fjson:
        json.dump(data, fjson, separators=(',', ':'))
    cols = ['file', 'page', 'probe', 'note', 'size', 'fills', 'strokes', 'shadings', 'images', 'fill_box', 'stroke_box',
            'lw', 'cap', 'join', 'dash', 'fill_colours', 'glyph']
    with open(os.path.join(out_dir, "phase0-word.tsv"), "w", newline="") as ftsv:
        w = csv.DictWriter(ftsv, cols, delimiter='\t', lineterminator='\n')
        w.writeheader()
        w.writerows(rows)
    print(len(rows), 'probes read')

if __name__ == '__main__':
    a = sys.argv[1:]
    out = HERE
    if a and a[0] == '--out':
        out, a = a[1], a[2:]
    main(a[0], a[1:], out)
