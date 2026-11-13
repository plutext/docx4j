# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 3: Word's PDFs of the round cr083-phase3 (build_effects.py) read probe by probe, each probe's
parameters read from its own docx, as JSON lines.

    python3 read_phase3.py <results dir> <docx dir> <out.jsonl> [job ...]

<results dir>/<job>/<job>.pdf is Word's PDF; <docx dir>/<job>.docx the probe file as staged. For each page:
- the probe: its id, the runs (text, size, face, w14 effects with their attributes in points, degrees and percent),
  the warp, the box's turn, its autofit and anchor, the shape's own a:effectLst;
- read_effects.py's measures: the box (the grey line) and its height, the first line's text origin and stext box,
  Word's glyph paths (count and union), and each picture in order with its place, pixel size, alpha peak, 50% and 5%
  alpha bounds (within the clip it is drawn under), drawn offset and contours (--detail), and its colour (the
  alpha-weighted mean of its colour pixels);
- the drawing order: mutool trace's text, image and path draws in turn (T, I, P), so that a picture over the text shows;
- body text (wb70): the baselines of its four lines.
Places are PDF points, y up, but the text origin, y down as stext gives it."""
import io, json, os, re, subprocess, sys, zipfile

import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, '..', 'CR-081-notes'))
import pdfdraws as P  # noqa: E402
import read_effects as R  # noqa: E402

EMU = 12700.0
W14 = re.compile(r'<w14:(glow|shadow|reflection|textOutline|textFill)\b([^>]*?)(/>|>(.*?)</w14:\1>)', re.S)


def attrs(s):
    return {k: v for k, v in re.findall(r'w14:(\w+)="([^"]*)"', s)}


def effect(name, a, inner):
    """An effect's attributes in points, degrees and percent; its colour"""
    out = {}
    for k, v in a.items():
        if k in ('blurRad', 'dist', 'rad', 'w'):
            out[k] = int(v) / EMU
        elif k in ('dir', 'fadeDir', 'kx', 'ky'):
            out[k] = int(v) / 60000.0
        elif k in ('sx', 'sy', 'stA', 'endA', 'stPos', 'endPos'):
            out[k] = int(v) / 1000.0
        else:
            out[k] = v
    if inner:
        c = re.search(r'<w14:(srgbClr|schemeClr) w14:val="(\w+)"', inner)
        if c:
            out['colour'] = c.group(2)
        mods = re.findall(r'<w14:(alpha|lumMod|lumOff|shade|tint) w14:val="(\d+)"', inner)
        if mods:
            out['mods'] = [(k, int(v)) for k, v in mods]
        if name == 'textFill':
            out['kind'] = re.search(r'<w14:(\w+)', inner).group(1)
    return out


def runs(xml):
    out = []
    for rpr, text in re.findall(r'<w:r><w:rPr>(.*?)</w:rPr><w:t[^>]*>([^<]*)</w:t></w:r>', xml, re.S):
        sz = re.search(r'<w:sz w:val="(\d+)"', rpr)
        face = re.search(r'<w:rFonts w:ascii="([^"]+)"', rpr)
        fx = {m.group(1): effect(m.group(1), attrs(m.group(2)), m.group(4)) for m in W14.finditer(rpr)}
        out.append({'text': text, 'size': int(sz.group(1)) / 2 if sz else None, 'face': face.group(1) if face else None,
                    'fx': fx})
    return out


def probes(docx):
    """Each page's probe, in order"""
    doc = zipfile.ZipFile(docx).read('word/document.xml').decode('utf-8')
    pages = re.split(r'<w:pPr><w:pageBreakBefore/></w:pPr>', doc)
    out = []
    for pg in pages:
        pid = re.search(r'<w:t xml:space="preserve">(\w+-\d+): (.*?)\.</w:t>', pg)
        p = {'probe': pid.group(1), 'note': pid.group(2)}
        txbx = re.search(r'<w:txbxContent>(.*?)</w:txbxContent>', pg, re.S)
        if txbx:
            p['kind'] = 'wordart'
            p['runs'] = runs(txbx.group(1))
            p['lines'] = [[r['text'] for r in runs(x)] for x in re.findall(r'<w:p>.*?</w:p>', txbx.group(1), re.S)]
            warp = re.search(r'<a:prstTxWarp prst="(\w+)"', pg)
            p['warp'] = warp.group(1) if warp else None
            rot = re.search(r'<a:xfrm rot="(-?\d+)"', pg)
            p['rot'] = int(rot.group(1)) / 60000.0 if rot else 0
            p['autofit'] = re.search(r'<a:(spAutoFit|noAutofit|normAutofit)/>', pg).group(1)
            p['anchor'] = re.search(r'anchor="(\w+)" anchorCtr', pg).group(1)
            p['wrap'] = re.search(r'<wps:bodyPr[^>]* wrap="(\w+)"', pg).group(1)
            sp = re.search(r'<wps:spPr>.*?(<a:effectLst>.*?</a:effectLst>).*?</wps:spPr>', pg, re.S)
            p['spfx'] = sp.group(1) if sp else None
            ln = re.search(r'<a:ln\b.*?</a:ln>', pg, re.S)
            p['line'] = bool(ln) and 'noFill' not in ln.group(0)
        else:
            p['kind'] = 'body'
            body = pg[pg.find('</w:p>', pg.find('</w:p>') + 6) + 6:]       # after the label and the summary
            p['runs'] = runs(body)
        out.append(p)
    return out


def colour(pdf, d):
    """The picture's colour: its colour pixels' mean, weighted by alpha; None where they can't be read"""
    try:
        rgb, alpha = P.pixels(pdf, d['obj'])
        if rgb is None and d.get('filter') == 'DCTDecode':
            from PIL import Image
            im = Image.open(io.BytesIO(P.show(pdf, d['obj'], raw=True))).convert('RGB')
            rgb = np.asarray(im) / 255.0
        if rgb is None or alpha is None or alpha.sum() == 0:
            return None
        if rgb.shape[:2] != alpha.shape:
            return None
        w = alpha / alpha.sum()
        return [round(float((rgb[..., min(i, rgb.shape[2] - 1)] * w).sum()) * 255, 1) for i in range(3)]
    except Exception:
        return None


def order(pdf, page):
    """The page's draws in order: T text, I image, P path"""
    tr = subprocess.run(['mutool', 'trace', pdf, str(page)], capture_output=True).stdout.decode('utf-8', 'replace')
    out = []
    for m in re.finditer(r'<(fill_text|stroke_text|fill_image|fill_path|stroke_path|clip_text|ignore_text)\b', tr):
        k = {'fill_text': 'T', 'stroke_text': 'T', 'ignore_text': 't', 'fill_image': 'I', 'fill_path': 'P', 'stroke_path': 'S'}.get(m.group(1))
        if k:
            out.append(k)
    s = ''.join(out)
    return re.sub(r'(.)\1+', lambda m: f'{m.group(1)}{len(m.group(0))}', s)


def baselines(pdf, page, height):
    """The body probe's lines: text and baseline (y down), from stext"""
    out = R.stext(pdf, page)
    lines = []
    for m in re.finditer(r'<line bbox="([-\d. ]+)"[^>]*? text="([^"]*)">(.*?)</line>', out, re.S):
        y = re.search(r'<char [^>]*? y="([-\d.]+)"', m.group(3))
        if y:
            lines.append({'text': m.group(2), 'baseline': float(y.group(1))})
    return lines


def cut(b, clip):
    """Bounds within the draw's clip (what is shown of a picture drawn under a clip path)"""
    if not b or not clip:
        return b
    out = (max(b[0], clip[0]), max(b[1], clip[1]), min(b[2], clip[2]), min(b[3], clip[3]))
    return out if out[0] < out[2] and out[1] < out[3] else None


def measure(pdf, n, text, height, objs):
    draws = P.page_draws(pdf, objs[n - 1])
    strokes = [d for d in draws if d.get('kind') == 'stroke']
    box = strokes[0]['box'] if strokes else None
    near = box or (0, 0, 1e9, 1e9)
    fills = [d['box'] for d in draws if d.get('kind') == 'fill' and R.near(d['box'], box)] if box else []
    t = R.text_line(pdf, n, text) if text else None
    m = {'box': box, 'height': box[3] - box[1] if box else None,
         'origin': [t[0], t[1]] if t else None, 'textline': list(t[2]) if t else None,
         'paths': len(fills),
         'paths_union': [min(b[0] for b in fills), min(b[1] for b in fills), max(b[2] for b in fills), max(b[3] for b in fills)] if fills else None,
         'order': order(pdf, n), 'pictures': []}
    grid = R.Grid(pdf, n, box, height) if box else None
    for d in draws:
        if d.get('kind') != 'image' or not d.get('smask'):
            continue
        a, _ = P.alpha_of(pdf, d['obj'])
        b = d['box']
        top = float(a.max()) if a is not None else 0.0
        pic = {'place': list(b), 'px': [d['w'], d['h']], 'ppt': d['w'] / (b[2] - b[0]), 'filter': d.get('filter'), 'alpha': top}
        if top > 0:
            pic['b50'] = cut(R.bounds(a, b, 0.5 * top), d.get('clip'))
            pic['b5'] = cut(R.bounds(a, b, 0.05 * top), d.get('clip'))
            if d.get('clip'):
                pic['clip'] = list(d['clip'])
            pic['colour'] = colour(pdf, d)
            if grid is not None:
                al = grid.place(a, b)
                pic['offset'] = list(grid.offset(al))
                pic['c50'], pic['c5'] = grid.contours(al, top)
        m['pictures'].append(pic)
    return m


def main(results, docxs, out, *jobs):
    jobs = jobs or sorted(os.listdir(results))
    with open(out, 'w') as f:
        for job in jobs:
            pdf = os.path.join(results, job, job + '.pdf')
            objs = P.pages(pdf)
            height = float(re.search(r'/MediaBox \[\s*[-\d.]+ [-\d.]+ [-\d.]+ ([-\d.]+)',
                                     P.show(pdf, objs[0]).decode('latin-1')).group(1))
            for n, p in enumerate(probes(os.path.join(docxs, job + '.docx')), 1):
                p['file'], p['page'] = job, n
                if p['kind'] == 'wordart':
                    first = ''.join(p['lines'][0]) if p.get('lines') else 'WordArt'
                    p.update(measure(pdf, n, first, height, objs))
                else:
                    p['baselines'] = baselines(pdf, n, height)
                    p['order'] = order(pdf, n)
                f.write(json.dumps(p) + '\n')
            print(job, len(objs), 'pages', file=sys.stderr)


if __name__ == '__main__':
    main(*sys.argv[1:])
