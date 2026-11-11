# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""A first look at Word's PDFs of CR-083 phase 0's probes (probes.tsv): for each probe's page, what Word put there
outside the label lines, so that D2 (does Word keep WordArt as text, or draw it?) and the forms of the effects can be
read before anything is measured.

For each page, one line:
  text      the characters Word wrote as text below the labels (mutool stext), their fonts, and how many are drawn
            invisibly (render mode 3, which stext reports as a char with no colour change; read from the content stream:
            the Tr operators in force)
  fills     filled paths below the labels, by colour (CR-080's pdfpaths.py), and how many look like glyph outlines
            (a path of curves with an even-odd or nonzero fill, not a rectangle)
  strokes   stroked paths, by colour and width (the 0.25 pt grey line is the WordArt's box: its corners are given)
  images    pictures (effects are pictures in Word's PDFs, CR-081)
  shadings  shading patterns and sh operators (gradients)

usage: survey_wordart.py <results dir: one folder a job, <job>/<job>.pdf> [job ...]   (writes survey-word.tsv beside
this script)"""
import collections, csv, os, re, subprocess, sys
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', 'CR-080-notes'))
sys.path.insert(0, os.path.join(HERE, '..', 'CR-081-notes'))
import pdfpaths                                                               # noqa: E402
import pdfdraws as pd                                                         # noqa: E402

LABEL_BOTTOM = 130.0          # the two label lines end above this, in points from the page's top (generated files)

def stext(pdf):
    """Per page: [(char, font, size, x0, y0, x1, y1, colour, alpha)] with y from the page's top."""
    xml = subprocess.run(['mutool', 'draw', '-q', '-F', 'stext', '-o', '-', pdf], capture_output=True, check=True).stdout
    out = []
    for page in ET.fromstring(xml).iter('page'):
        chars = []
        for font in page.iter('font'):
            name, size = font.get('name'), float(font.get('size'))
            for ch in font.iter('char'):
                q = [float(v) for v in (ch.get('quad') or ch.get('bbox')).split()]
                xs, ys = q[0::2], q[1::2]
                chars.append((ch.get('c'), name, size, min(xs), min(ys), max(xs), max(ys), ch.get('color'), ch.get('alpha')))
        out.append(chars)
    return out

TR = re.compile(rb'(\d)\s+Tr\b')

def render_modes(pdf, page_obj):
    """The text rendering modes set on the page (Tr), counted; 0 if none is set."""
    res, objs = pdfpaths._resources(pdf, page_obj)
    data = b''.join(pd.show(pdf, o, raw=True) for o in objs)
    for name, obj in res.get('XObject', {}).items():
        d = pd.show(pdf, obj).decode('latin-1')
        if '/Subtype /Form' in d:
            data += pd.show(pdf, obj, raw=True)
    modes = collections.Counter(int(m) for m in TR.findall(data))
    return dict(modes) or {0: 0}

def colour(c):
    if c is None:
        return '-'
    if isinstance(c, tuple) and c and c[0] == 'pattern':
        return 'pattern'
    return ''.join(f'{int(round(v * 255)):02X}' for v in (c if len(c) == 3 else (c[0],) * 3))

def survey(pdf):
    h_pages = pd.pages(pdf)
    texts = stext(pdf)
    rows = []
    for i, page_obj in enumerate(h_pages):
        h = pdfpaths.page_height(pdf, page_obj)
        below = [c for c in texts[i] if c[4] > LABEL_BOTTOM]
        fonts = collections.Counter(f'{c[1]} {c[2]:g} {c[7]}{c[8] or ""}' for c in below)
        draws = pdfpaths.page_paths(pdf, page_obj)
        fills, strokes, images, shadings, box = collections.Counter(), collections.Counter(), 0, 0, ''
        curvy = 0
        for d in draws:
            kind = d['kind']
            if kind == 'image':
                images += 1
                continue
            if kind == 'shading':
                shadings += 1
                continue
            if h - d['box'][3] < LABEL_BOTTOM:                  # in the labels' band (their underlines and the like)
                continue
            if kind in ('fill', 'fillstroke'):
                fills[colour(d['fill'])] += 1
                if any(seg[0] == 'c' for sp in d['subpaths'] for seg in sp):
                    curvy += 1
            if kind in ('stroke', 'fillstroke'):
                strokes[f'{colour(d["stroke"])}@{d["lw"]:.2f}'] += 1
                if colour(d['stroke']) == 'BFBFBF':
                    x0, y0, x1, y1 = d['box']
                    box = f'{x0:.1f},{h - y1:.1f},{x1:.1f},{h - y0:.1f}'
        rows.append({'page': i + 1, 'chars': len(below), 'text': ''.join(c[0] for c in below)[:60],
                     'fonts': ' '.join(f'{k}:{v}' for k, v in fonts.most_common()), 'modes': render_modes(pdf, page_obj),
                     'fills': sum(fills.values()), 'curvy': curvy, 'fill_colours': ' '.join(f'{k}:{v}' for k, v in fills.most_common(6)),
                     'strokes': ' '.join(f'{k}:{v}' for k, v in strokes.most_common(4)), 'box': box,
                     'images': images, 'shadings': shadings})
    return rows

def main(results, jobs):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv'), encoding='utf-8'), delimiter='\t'))
    by = collections.defaultdict(dict)
    for p in probes:
        if p['page'] != '-':
            by[p['file']][int(p['page'])] = p
    jobs = jobs or sorted(d for d in os.listdir(results) if os.path.isfile(os.path.join(results, d, d + '.pdf')))
    out = []
    for job in jobs:
        pdf = os.path.join(results, job, job + '.pdf')
        if not os.path.isfile(pdf):
            print('no pdf', job)
            continue
        for r in survey(pdf):
            p = by.get(job, {}).get(r['page'], {})
            out.append({'job': job, 'probe': p.get('probe', ''), 'note': p.get('note', ''), **r})
        print(job, 'read')
    path = os.path.join(HERE, 'survey-word.tsv')
    with open(path, 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, list(out[0].keys()), delimiter='\t', lineterminator='\n')
        w.writeheader()
        w.writerows(out)
    print(path, len(out), 'pages')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
