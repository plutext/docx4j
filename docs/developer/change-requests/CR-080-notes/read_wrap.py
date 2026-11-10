# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Reads PDFs of CR-080 phase 4's wrap probes (build_wrap.py, set cr080-phase4): on each page, every run of the filler
words on one baseline, split where a gap is wider than a word space could be (an object between), as a segment
(baseline y, x of its first glyph, x of its last glyph's end, its first and last word, y down from the page's top);
and the object's drawn box (the largest fill, stroke or picture drawn, with what overlaps it: its outline).

usage: read_wrap.py <word dir> <docx4j dir> [--tsv FILE]
  <word dir>: one folder a job, <file>/<file>.pdf (Word's); <docx4j dir>: <file>.pdf (docx4j's, by the fidelity
  harness's render with Word's fonts). Prints, for each probe: Word's shape page against its picture twin (fw10 against
  fw11, page for page), and docx4j's page against Word's, by the word opening each segment. --tsv writes every
  segment of every page, both renderers, for the record."""
import importlib.util, os, re, subprocess, sys
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('pdfdraws', os.path.join(HERE, '..', 'CR-081-notes', 'pdfdraws.py'))
pdfdraws = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(pdfdraws)

FILES = ('fw10-shapes', 'fw11-pictures', 'fw12-shape-only')
WORD = re.compile(r'^(ab|abcde|abcdefgh)\d{3}$')
GAP = 20.0             # wider than any word space at 10pt, narrower than any object's gap

def segments(pdf):
    """{page: [(y, x0, x1, first, last)]}, y down, sorted by y then x; filler words only."""
    out = subprocess.run(['mutool', 'draw', '-F', 'stext', '-o', '-', pdf], capture_output=True, check=True).stdout
    root = ET.fromstring(out)
    pages = {}
    for n, page in enumerate(root.iter('page'), 1):
        rows = {}
        for ch in page.iter('char'):
            q = [float(v) for v in ch.get('quad').split()]
            rows.setdefault(round(float(ch.get('y')), 1), []).append((float(ch.get('x')), max(q[2], q[6]), ch.get('c')))
        segs = []
        for y, chars in rows.items():
            chars.sort()
            run = [chars[0]]
            for c in chars[1:]:
                if c[0] - run[-1][1] > GAP:
                    segs.append((y, run))
                    run = []
                run.append(c)
            segs.append((y, run))
        page_segs = []
        for y, run in segs:
            text = ''.join(c[2] for c in run).split()
            text = [t for t in text if WORD.match(t)]
            if not text:
                continue
            page_segs.append((y, round(run[0][0], 2), round(run[-1][1], 2), text[0], text[-1]))
        pages[n] = sorted(page_segs)
    return pages

def objects(pdf):
    """{page: (x0, top, x1, bottom)} the object's drawn box, y down; None where nothing is drawn."""
    out = {}
    for n, obj in enumerate(pdfdraws.pages(pdf), 1):
        mb = re.search(rb'/MediaBox\s*\[\s*[-\d.]+\s+[-\d.]+\s+[-\d.]+\s+([-\d.]+)', pdfdraws.show(pdf, obj))
        h = float(mb.group(1)) if mb else 841.92
        boxes = [d['box'] for d in pdfdraws.page_draws(pdf, obj) if d.get('box')]
        # table borders and hairlines aside: the largest box, then whatever overlaps it
        boxes = [b for b in boxes if (b[2] - b[0]) * (b[3] - b[1]) > 0 or max(b[2] - b[0], b[3] - b[1]) > 50]
        if not boxes:
            out[n] = None
            continue
        big = max(boxes, key=lambda b: (b[2] - b[0] + 1) * (b[3] - b[1] + 1))
        u = list(big)
        for b in boxes:
            if b[0] <= big[2] + 2 and b[2] >= big[0] - 2 and b[1] <= big[3] + 2 and b[3] >= big[1] - 2:
                u = [min(u[0], b[0]), min(u[1], b[1]), max(u[2], b[2]), max(u[3], b[3])]
        out[n] = (round(u[0], 2), round(h - u[3], 2), round(u[2], 2), round(h - u[1], 2))
    return out

def compare(a, b):
    """Segments of page a against page b, matched by first word: (only in a, only in b, max |dx0|, max |dy|, worst)."""
    ia, ib = {s[3]: s for s in a}, {s[3]: s for s in b}
    only_a = [s for s in a if s[3] not in ib]
    only_b = [s for s in b if s[3] not in ia]
    dx = dy = 0.0
    worst = None
    for k in ia.keys() & ib.keys():
        sa, sb = ia[k], ib[k]
        ex, ey = abs(sa[1] - sb[1]), abs(sa[0] - sb[0])
        if max(ex, ey) > max(dx, dy):
            worst = (k, round(sa[1] - sb[1], 2), round(sa[0] - sb[0], 2))
        dx, dy = max(dx, ex), max(dy, ey)
    return only_a, only_b, round(dx, 2), round(dy, 2), worst

def beside(segs, obj):
    """The segments on the object's rows: their x0 and x1, to show the gap left either side of it."""
    if not obj:
        return []
    return [s for s in segs if obj[1] - 2 <= s[0] - 7 and s[0] <= obj[3] + 12]

def notes():
    rows = {}
    for line in open(os.path.join(HERE, 'probes-phase4.tsv'), encoding='utf-8').read().splitlines()[1:]:
        f = line.split('\t')
        rows[f[3]] = f[-1]
    return rows

# Word's pages against each other: (page, against, the question it settles)
PAIRS = [
    ('fw12-01', 'fw10-01', 'a shape painting nothing wraps as one painting'),
    ('fw12-02', 'fw10-01', 'painting nothing, effect extent 0, against a 1pt line with r = b = 1pt'),
    ('fw12-03', 'fw10-01', 'effect extent 0 against r = b = 1pt: is the effect extent in the wrap?'),
    ('fw10-16', 'fw10-01', 'effect extent 3pt every side against r = b = 1pt'),
    ('fw12-04', 'fw12-03', 'a 6pt line against a 1pt line, effect extent 0: is the line in the wrap?'),
    ('fw12-05', 'fw12-04', 'a 6pt line, effect extent r = b = 6pt against 0'),
    ('fw12-08', 'fw10-01', 'an ellipse, square, against a rect'),
    ('fw12-10', 'fw10-01', 'a group against a shape'),
    ('fw10-08', 'fw10-01', 'tight, box polygon, against square'),
    ('fw10-09', 'fw10-01', 'through, box polygon, against square'),
    ('fw10-10', 'fw10-03', 'tight, box polygon, centred, against square centred'),
    ('fw10-11', 'fw10-10', 'tight, ellipse polygon, against the box polygon: is the polygon followed?'),
    ('fw12-09', 'fw10-10', 'an ellipse with a box polygon against a rect with one: geometry or polygon?'),
    ('fw10-17', 'fw10-01', 'turned 30 degrees, effect extent r = b = 1pt, against unturned'),
    ('fw10-18', 'fw10-01', 'turned 30 degrees, effect extent the turned bounds, against unturned'),
    ('fw10-13', 'fw10-01', 'behind the text, square, against in front, square'),
    ('fw10-13', 'fw10-12', 'behind the text, square, against behind, no wrap'),
]
FILE_OF = {'fw10': 'fw10-shapes', 'fw11': 'fw11-pictures', 'fw12': 'fw12-shape-only'}

def at(src, pid):
    return src[FILE_OF[pid[:4]]][int(pid[5:])]

def main(word_dir, d4j_dir, tsv=None):
    note = notes()
    W = {f: segments(os.path.join(word_dir, f, f + '.pdf')) for f in FILES}
    D = {f: segments(os.path.join(d4j_dir, f + '.pdf')) for f in FILES}
    WO = {f: objects(os.path.join(word_dir, f, f + '.pdf')) for f in FILES}
    DO = {f: objects(os.path.join(d4j_dir, f + '.pdf')) for f in FILES}
    if tsv:
        with open(tsv, 'w', newline='') as out:
            out.write('renderer\tfile\tpage\ty\tx0\tx1\tfirst\tlast\n')
            for who, src in (('word', W), ('docx4j', D)):
                for f in FILES:
                    for p, segs in src[f].items():
                        for s in segs:
                            out.write(f'{who}\t{f}\t{p}\t' + '\t'.join(str(v) for v in s) + '\n')
    print('== Word: the shape (fw10) against its picture twin (fw11), page for page')
    for p in sorted(W['fw10-shapes']):
        oa, ob, dx, dy, worst = compare(W['fw10-shapes'][p], W['fw11-pictures'][p])
        same = not oa and not ob and dx < 0.05 and dy < 0.05
        print(f'fw10-{p:02d} {"SAME" if same else "DIFFER"}  segs {len(W["fw10-shapes"][p])}/{len(W["fw11-pictures"][p])}'
              f'  breaks only-shape {len(oa)} only-picture {len(ob)}  max dx {dx} dy {dy}  {worst or ""}'
              f'  shape obj {WO["fw10-shapes"][p]} picture obj {WO["fw11-pictures"][p]}  | {note.get(f"fw10-{p:02d}", "")}')
    print('\n== docx4j: the shape (fw10) against its picture twin (fw11), page for page')
    for p in sorted(D['fw10-shapes']):
        oa, ob, dx, dy, worst = compare(D['fw10-shapes'][p], D['fw11-pictures'].get(p, []))
        same = not oa and not ob and dx < 0.05 and dy < 0.05
        print(f'fw10-{p:02d} {"SAME" if same else "DIFFER"}  max dx {dx} dy {dy}  {worst or ""}')
    print('\n== Word: one page against another')
    for a, b, q in PAIRS:
        oa, ob, dx, dy, worst = compare(at(W, a), at(W, b))
        same = not oa and not ob and dx < 0.05 and dy < 0.05
        print(f'{a} vs {b} {"SAME" if same else "DIFFER"}  breaks only-{a} {len(oa)} only-{b} {len(ob)}  max dx {dx} dy {dy}'
              f'  {worst or ""}  | {q}')
    print('\n== docx4j against Word, page for page')
    for f in FILES:
        prefix = f.split('-')[0]
        for p in sorted(W[f]):
            oa, ob, dx, dy, worst = compare(D[f].get(p, []), W[f][p])
            same = not oa and not ob and dx < 1 and dy < 1
            print(f'{prefix}-{p:02d} {"match" if same else "MISS "}  segs d4j {len(D[f].get(p, []))} word {len(W[f][p])}'
                  f'  breaks only-d4j {len(oa)} only-word {len(ob)}  max dx {dx} dy {dy}  {worst or ""}'
                  f'  obj d4j {DO[f].get(p)} word {WO[f][p]}  | {note.get(f"{prefix}-{p:02d}", "")}')

if __name__ == '__main__':
    a = sys.argv[1:]
    tsv = None
    if '--tsv' in a:
        i = a.index('--tsv')
        tsv = a[i + 1]
        del a[i:i + 2]
    main(a[0], a[1], tsv)
