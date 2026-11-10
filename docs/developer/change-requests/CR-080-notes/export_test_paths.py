# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Writes phase 1's test oracle, docx4j-core-tests/src/test/resources/shapes/cr080-word.tsv.gz (gzip, no name or
time, so the same input writes the same bytes), from phase 0's
probes.tsv and phase0-word.json (read_shapes.py): for each probe of a preset or custom geometry (fp10 to fp13, fp20,
fp21) and of a text rectangle (fp70 to fp77), a line of
    probe, size (points, w x h), the probe's spPr content, the glyph's box (fp7x: x0 y0 x1 y1), Word's draws
where the draws are ';'-separated, each 'F' (a fill) or 'S' (a stroke) and its path: 'M x y', 'L x y',
'C x1 y1 x2 y2 x3 y3', 'Z', in points from the shape's top left, y down, as Word's PDF drew it.

usage: export_test_paths.py"""
import csv, gzip, io, json, os

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, '..', '..', '..', '..', 'docx4j-core-tests', 'src', 'test', 'resources', 'shapes', 'cr080-word.tsv.gz')
FILES = ('fp10', 'fp11', 'fp12', 'fp13', 'fp20', 'fp21', 'fp70', 'fp71', 'fp72', 'fp73', 'fp74', 'fp75', 'fp76', 'fp77')

def num(v):
    s = f'{v:.2f}'.rstrip('0').rstrip('.')
    return '0' if s in ('-0', '') else s

def path(subpaths):
    out = []
    for sp in subpaths:
        for seg in sp:
            out.append(' '.join([seg[0].upper().replace('H', 'Z')] + [num(v) for v in seg[1:]]))
    return ' '.join(out)

def main():
    data = json.load(open(os.path.join(HERE, 'phase0-word.json')))
    seen, rows = set(), []
    for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'):
        if not p['file'].startswith(FILES) or p['probe'] in seen or p['probe'] not in data:
            continue
        seen.add(p['probe'])
        d = data[p['probe']]
        draws = []
        for dr in d['draws']:
            if dr['kind'] in ('fill', 'fillstroke'):
                draws.append('F ' + path(dr['subpaths']))
            if dr['kind'] in ('stroke', 'fillstroke'):
                draws.append('S ' + path(dr['subpaths']))
        glyph = ' '.join(num(v) for v in d['glyph']) if d.get('glyph') else ''
        rows.append([p['probe'], p['size'], p['xml'], glyph, ';'.join(draws)])
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'wb') as raw, gzip.GzipFile(filename='', mode='wb', fileobj=raw, mtime=0) as gz, \
            io.TextIOWrapper(gz, encoding='utf-8', newline='') as f:
        f.write('# CR-080 phase 0: Word 16.0.20326.20232\'s PDFs of the geometry and text-rectangle probes, read by\n')
        f.write('# CR-080-notes/read_shapes.py and written by export_test_paths.py. Points, from the shape\'s top left, y down.\n')
        f.write('\t'.join(['probe', 'size', 'xml', 'glyph', 'draws']) + '\n')
        for r in rows:
            assert not any('\t' in c or '\n' in c for c in r), r[0]
            f.write('\t'.join(r) + '\n')
    print(len(rows), 'probes ->', os.path.normpath(OUT), os.path.getsize(OUT), 'bytes')

if __name__ == '__main__':
    main()
