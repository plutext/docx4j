# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 5: what a PDF draws on a probe page, in drawing order (the round cr083-phase5, phase5-findings.md
section 6).

    python3 read_body.py <pdf> [pages] [--all]

Each page's drawing from `mutool trace`, in order, page coordinates y down, points:
  T  text: how (fill, stroke, clip, invisible: alpha 0 or mode 3), colour, alpha, its glyphs' extent across (the first
     glyph's origin to the last's advance), its baseline, its text
  P  a filled path: colour, alpha, its box (with --all; else only paths that aren't the page's plain labels)
  S  a stroked path: colour, alpha, width, its box
  G  a shading: linear start and end, or radial centre and radius; the clip it fills within (its box)
  I  a picture: its box, its pixel size
The labels (the page's first two baselines; for a probe at the top of its page, its last two) are left out unless --all."""
import re, subprocess, sys
import xml.etree.ElementTree as ET


def trace(pdf, page):
    out = subprocess.run(['mutool', 'trace', pdf, str(page)], capture_output=True).stdout.decode('utf-8', 'replace')
    return ET.fromstring(out[out.index('<page'):out.rindex('</page>') + 7])


def mat(s):
    return [float(v) for v in s.split()] if s else [1, 0, 0, 1, 0, 0]


def apply(m, x, y):
    return m[0] * x + m[2] * y + m[4], m[1] * x + m[3] * y + m[5]


def mul(a, b):
    """a then b"""
    return [a[0] * b[0] + a[1] * b[2], a[0] * b[1] + a[1] * b[3], a[2] * b[0] + a[3] * b[2], a[2] * b[1] + a[3] * b[3],
            a[4] * b[0] + a[5] * b[2] + b[4], a[4] * b[1] + a[5] * b[3] + b[5]]


def path_box(el, m):
    xs, ys = [], []
    for c in el.iter():
        for k in ('', '1', '2', '3'):
            if c.get('x' + k) is not None:
                x, y = apply(m, float(c.get('x' + k)), float(c.get('y' + k)))
                xs.append(x)
                ys.append(y)
    return (min(xs), min(ys), max(xs), max(ys)) if xs else None


def box(b):
    return '%.2f,%.2f..%.2f,%.2f' % b if b else '-'


def colour(el):
    c = el.get('color', '')
    cs = el.get('colorspace', '')
    if cs == 'DeviceGray' and c:
        v = round(float(c) * 255)
        return '#%02X%02X%02X' % (v, v, v)
    if cs == 'DeviceRGB' and c:
        v = [round(float(x) * 255) for x in c.split()]
        return '#%02X%02X%02X' % tuple(v)
    return cs + ':' + c


def read(pdf, page, everything=False):
    root = trace(pdf, page)
    out = []
    clip = [None]

    def walk(el):
        tag = el.tag
        if tag == 'clip_path':
            clip.append(path_box(el, mat(el.get('transform'))))
            return
        if tag in ('clip_text', 'clip_stroke_text'):
            clip.append(text_extent(el)[0])
        if tag == 'pop_clip':
            if len(clip) > 1:
                clip.pop()
            return
        if tag in ('fill_text', 'stroke_text', 'clip_text', 'clip_stroke_text', 'ignore_text'):
            (b, base, txt, font) = text_extent(el)
            how = {'fill_text': 'fill', 'stroke_text': 'stroke', 'clip_text': 'clip', 'clip_stroke_text': 'clip-stroke',
                   'ignore_text': 'invisible'}[tag]
            if el.get('alpha') == '0':
                how += '/alpha0'
            out.append(('T', how, colour(el), el.get('alpha', '1'), b, base, txt, font))
            return
        if tag == 'fill_path':
            out.append(('P', colour(el), el.get('alpha', '1'), path_box(el, mat(el.get('transform')))))
            return
        if tag == 'stroke_path':
            out.append(('S', colour(el), el.get('alpha', '1'), el.get('linewidth'), path_box(el, mat(el.get('transform')))))
            return
        if tag == 'fill_shade':
            m = mat(el.get('transform'))
            pm = mat(el.get('pattern_matrix'))
            full = mul(pm, m)
            if el.get('type') == 'linear':
                s = apply(full, *[float(v) for v in el.get('start').split()])
                e = apply(full, *[float(v) for v in el.get('end').split()])
                out.append(('G', 'linear', s, e, clip[-1], el.get('alpha', '1')))
            else:
                out.append(('G', el.get('type'), el.attrib, None, clip[-1], el.get('alpha', '1')))
            return
        if tag == 'fill_image':
            m = mat(el.get('transform'))
            pts = [apply(m, x, y) for x, y in ((0, 0), (1, 0), (0, 1), (1, 1))]
            b = (min(p[0] for p in pts), min(p[1] for p in pts), max(p[0] for p in pts), max(p[1] for p in pts))
            out.append(('I', b, el.get('width') + 'x' + el.get('height'), el.get('alpha', '1')))
            return
        for c in el:
            walk(c)

    def text_extent(el):
        m = mat(el.get('transform'))
        xs, base, txt, font = [], None, '', ''
        for sp in el.iter('span'):
            trm = [float(v) for v in sp.get('trm').split()]
            size = abs(trm[0]) or abs(trm[3])
            font = sp.get('font', '')
            for g in sp.iter('g'):
                x, y = float(g.get('x')), float(g.get('y'))
                adv = float(g.get('adv', '0')) * size
                p0, p1 = apply(m, x, y), apply(m, x + adv, y)
                xs += [p0[0], p1[0]]
                if base is None:
                    base = p0[1]
                txt += g.get('unicode', '')
        b = (min(xs), base, max(xs), base) if xs else None
        return b, base, txt, font.split('+')[-1]

    for c in root:
        walk(c)
    if not everything:
        # the two label lines, the page's first two baselines (in the document's default face, Aptos here)
        bases = []
        for r in out:
            if r[0] == 'T' and r[5] is not None and round(r[5], 1) not in bases:
                bases.append(round(r[5], 1))
        labels = set(bases[:2])
        out = [r for r in out if not (r[0] == 'T' and r[5] is not None and round(r[5], 1) in labels)]
    return out


def show(r):
    k = r[0]
    if k == 'T':
        b = r[4]
        ext = '%.2f..%.2f' % (b[0], b[2]) if b else '-'
        base = '%.2f' % r[5] if r[5] is not None else '-'
        return 'T %-17s %s a=%s x %s base %s %s "%s"' % (r[1], r[2], r[3], ext, base, r[7], r[6][:60])
    if k == 'P':
        return 'P fill %s a=%s %s' % (r[1], r[2], box(r[3]))
    if k == 'S':
        return 'S stroke %s a=%s w=%s %s' % (r[1], r[2], r[3], box(r[4]))
    if k == 'G':
        if r[1] == 'linear':
            return 'G linear %.2f,%.2f -> %.2f,%.2f within %s' % (r[2][0], r[2][1], r[3][0], r[3][1], box(r[4]))
        return 'G %s %s within %s' % (r[1], {k: v for k, v in r[2].items() if k in ('start', 'end', 'center', 'radius', 'r0', 'r1', 'pattern_matrix')}, box(r[4]))
    if k == 'I':
        return 'I picture %s px %s a=%s' % (box(r[1]), r[2], r[3])
    return str(r)


def pages(spec, n):
    if not spec:
        return range(1, n + 1)
    out = []
    for part in spec.split(','):
        if '-' in part:
            a, b = part.split('-')
            out += range(int(a), int(b) + 1)
        else:
            out.append(int(part))
    return out


if __name__ == '__main__':
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    pdf = args[0]
    n = int(re.search(r'Pages: (\d+)', subprocess.run(['mutool', 'info', pdf], capture_output=True, text=True).stdout).group(1))
    for p in pages(args[1] if len(args) > 1 else None, n):
        print(f'== page {p}')
        for r in read(pdf, p, '--all' in sys.argv):
            print('  ' + show(r))
