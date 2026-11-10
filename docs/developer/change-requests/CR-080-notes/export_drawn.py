# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Writes phase 2's test oracle for docx4j-chart-render's ShapeRenderer,
docx4j-chart-render/src/test/resources/shapes/cr080-word-drawn.jsonl.gz (gzip, no name or time), from phase 0's
probes.tsv, phase0-word.json (read_shapes.py) and the probe files themselves (each file's k-th wps:wsp is its k-th
probe). One JSON object a line:
    probe, note, size (points, "w x h"), sppr and style (the shape's wps:spPr and wps:style, as XML), theme
    ("2023" or "2013"), draws: Word's, each {k: F or S, fill, stroke (hex), alpha [ca, CA], lw, cap, join, dash
    [[array], phase], path ('M x y', 'L x y', 'C ...', 'Z', points from the shape's top left, y down), pattern (a
    pattern or shading fill), evenodd (the even-odd rule)}; a picture's {k: image, box [l, t, r, b]}
Also copies the probes' picture (word/media/photo.png) beside it, for the picture fills.

usage: export_drawn.py [--json FILE --out FILE] <results dir: one folder a job, holding the job's source .docx>
--json and --out read another renderer's draws (read_shapes.py --out) and write them alike, for CR-080 phase 2's
comparison of docx4j's PDFs with Word's"""
import csv, gzip, io, json, os, re, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
OUT_DIR = os.path.join(HERE, '..', '..', '..', '..', 'docx4j-chart-render', 'src', 'test', 'resources', 'shapes')
FILES = ('fp10', 'fp11', 'fp20', 'fp21', 'fp30', 'fp31', 'fp40', 'fp50', 'fp51')

def num(v):
    s = f'{v:.2f}'.rstrip('0').rstrip('.')
    return '0' if s in ('-0', '') else s

def path(subpaths):
    out = []
    for sp in subpaths:
        for seg in sp:
            out.append(' '.join([seg[0].upper().replace('H', 'Z')] + [num(v) for v in seg[1:]]))
    return ' '.join(out)

def shapes(docx):
    x = zipfile.ZipFile(docx).read('word/document.xml').decode('utf-8')
    out = []
    for m in re.finditer(r'<wps:wsp>(.*?)</wps:wsp>', x, re.S):
        body = m.group(1)
        sp = re.search(r'<wps:spPr\b.*?</wps:spPr>|<wps:spPr\b[^>]*/>', body, re.S)
        st = re.search(r'<wps:style>.*?</wps:style>', body, re.S)
        out.append((sp.group(0) if sp else '', st.group(0) if st else ''))
    return out, zipfile.ZipFile(docx).read('word/theme/theme1.xml').decode('utf-8')

def main(results, json_in=None, out_file=None):
    data = json.load(open(json_in or os.path.join(HERE, 'phase0-word.json')))
    probes = [p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t') if p['file'].startswith(FILES)]
    by_file, seen, rows, photo = {}, set(), [], None
    for p in probes:
        if p['probe'] in seen or p['probe'] not in data:
            continue
        seen.add(p['probe'])
        f = p['file']
        if f not in by_file:
            src = [n for n in os.listdir(os.path.join(results, f)) if n.endswith('.docx') and not n.endswith('.word.docx')][0]
            docx = os.path.join(results, f, src)
            by_file[f] = shapes(docx)
            if photo is None and 'word/media/photo.png' in zipfile.ZipFile(docx).namelist():
                photo = zipfile.ZipFile(docx).read('word/media/photo.png')
        (sps, theme_xml) = by_file[f]
        k = int(p['page']) - 1
        sp, st = sps[k]
        theme = '2013' if 'Office Theme' in theme_xml and 'Aptos' not in theme_xml else '2023'
        d = data[p['probe']]
        draws = []
        for dr in d['draws']:
            if dr['kind'] == 'image':
                draws.append({'k': 'image', 'box': dr['box']})
                continue
            if dr['kind'] not in ('fill', 'stroke', 'fillstroke'):
                draws.append({'k': dr['kind']})
                continue
            base = {'fill': dr['fill'], 'stroke': dr['stroke'], 'alpha': dr['alpha'], 'lw': dr['lw'], 'cap': dr['cap'],
                    'join': dr['join'], 'dash': dr['dash'], 'path': path(dr['subpaths']), 'pattern': bool(dr.get('pattern')),
                    'evenodd': dr['op'].endswith('*')}
            if dr['kind'] in ('fill', 'fillstroke'):
                draws.append(dict(base, k='F'))
            if dr['kind'] in ('stroke', 'fillstroke'):
                draws.append(dict(base, k='S'))
        rows.append({'probe': p['probe'], 'note': p['note'], 'size': p['size'], 'sppr': sp, 'style': st, 'theme': theme,
                     'draws': draws})
    os.makedirs(OUT_DIR, exist_ok=True)
    out = out_file or os.path.join(OUT_DIR, 'cr080-word-drawn.jsonl.gz')
    with open(out, 'wb') as raw, gzip.GzipFile(filename='', mode='wb', fileobj=raw, mtime=0) as gz, \
            io.TextIOWrapper(gz, encoding='utf-8', newline='') as fo:
        for r in rows:
            fo.write(json.dumps(r, separators=(',', ':'), sort_keys=True) + '\n')
    if photo and not out_file:
        open(os.path.join(OUT_DIR, 'photo.png'), 'wb').write(photo)
    print(len(rows), 'probes ->', os.path.normpath(out), os.path.getsize(out), 'bytes; photo', len(photo or b''))

if __name__ == '__main__':
    a = sys.argv[1:]
    json_in = out_file = None
    while a and a[0] in ('--json', '--out'):
        if a[0] == '--json': json_in = a[1]
        else: out_file = a[1]
        a = a[2:]
    main(a[0], json_in, out_file)
