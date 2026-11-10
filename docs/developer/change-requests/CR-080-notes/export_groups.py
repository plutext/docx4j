# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Writes phase 3's test oracle for docx4j-chart-render's group and canvas drawing,
docx4j-chart-render/src/test/resources/shapes/cr080-word-groups.jsonl.gz (gzip, no name or time), from phase 0's round 2
(probes.tsv's fp90, fp91 and fp92), phase0-word.json (read_shapes.py) and the probe files themselves (each file's k-th
wp:anchor is its k-th probe). One JSON object a line:
    probe, note, size (points, "w x h"), graphic (the anchor's a:graphicData child, wps:wsp, wpg:wgp or wpc:wpc, as XML,
    its prefixes undeclared), theme ("2023" or "2013"), glyph (Word's first glyph's box, [l, t, r, b], or null), draws
    (as export_drawn.py writes them)

usage: export_groups.py [--json FILE --out FILE] <results dir: one folder a job, holding the job's source .docx>
--json and --out read another renderer's draws (read_shapes.py --out) and write them alike, for the PDF's comparison"""
import csv, gzip, io, json, os, re, sys, zipfile

from export_drawn import HERE, OUT_DIR, convert

FILES = ('fp90', 'fp91', 'fp92')

def graphics(docx):
    z = zipfile.ZipFile(docx)
    x = z.read('word/document.xml').decode('utf-8')
    out = []
    for m in re.finditer(r'<wp:anchor\b.*?</wp:anchor>', x, re.S):
        g = re.search(r'<a:graphicData\b[^>]*>(.*)</a:graphicData>', m.group(0), re.S)
        out.append(g.group(1) if g else '')
    return out, z.read('word/theme/theme1.xml').decode('utf-8')

def main(results, json_in=None, out_file=None):
    data = json.load(open(json_in or os.path.join(HERE, 'phase0-word.json')))
    probes = [p for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t') if p['file'].startswith(FILES)]
    by_file, seen, rows = {}, set(), []
    for p in probes:
        if p['probe'] in seen or p['probe'] not in data:
            continue
        seen.add(p['probe'])
        f = p['file']
        if f not in by_file:
            src = [n for n in os.listdir(os.path.join(results, f)) if n.endswith('.docx') and not n.endswith('.word.docx')][0]
            by_file[f] = graphics(os.path.join(results, f, src))
        gs, theme_xml = by_file[f]
        theme = '2013' if 'Office Theme' in theme_xml and 'Aptos' not in theme_xml else '2023'
        d = data[p['probe']]
        rows.append({'probe': p['probe'], 'note': p['note'], 'size': p['size'], 'graphic': gs[int(p['page']) - 1],
                     'theme': theme, 'glyph': d.get('glyph'), 'draws': convert(d)})
    os.makedirs(OUT_DIR, exist_ok=True)
    out = out_file or os.path.join(OUT_DIR, 'cr080-word-groups.jsonl.gz')
    with open(out, 'wb') as raw, gzip.GzipFile(filename='', mode='wb', fileobj=raw, mtime=0) as gz, \
            io.TextIOWrapper(gz, encoding='utf-8', newline='') as fo:
        for r in rows:
            fo.write(json.dumps(r, separators=(',', ':'), sort_keys=True) + '\n')
    print(len(rows), 'probes ->', os.path.normpath(out), os.path.getsize(out), 'bytes')

if __name__ == '__main__':
    a = sys.argv[1:]
    json_in = out_file = None
    while a and a[0] in ('--json', '--out'):
        if a[0] == '--json': json_in = a[1]
        else: out_file = a[1]
        a = a[2:]
    main(a[0], json_in, out_file)
