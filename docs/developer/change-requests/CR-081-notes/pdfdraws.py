# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""What a page of Word's PDF draws, read with mutool: a small content-stream interpreter (q/Q, cm, paths, clips, fills,
strokes, Do) that is enough for Word's output. Each draw is a dict:
  image: name, obj, ctm (the image's placement), box (x0, y0, x1, y1 in PDF points, y up), clip (the clip's box),
         w, h, filter, cs, smask
  fill/stroke: op, box (the path's box), colour (the fill colour, rg/g), clip
Text is skipped. pixels() gives an image's RGB (un-premultiplied when its soft mask has /Matte) and alpha as numpy
arrays, row 0 at the top. Used by read_phase0.py."""
import re, subprocess
import numpy as np

def show(pdf, obj, raw=False):
    args = ['mutool', 'show'] + (['-b'] if raw else []) + [pdf, str(obj)]
    return subprocess.run(args, capture_output=True, check=True).stdout

def pages(pdf):
    """The page objects, in order."""
    return [int(m) for m in re.findall(r'page \d+ = (\d+) 0 R', show(pdf, 'pages').decode('latin-1'))]

def _ref(d, key):
    m = re.search(rf'/{key} (\d+) 0 R', d)
    return int(m.group(1)) if m else None

_TOKEN = re.compile(rb'\s*(?:(%[^\r\n]*)|(\((?:\\.|[^\\()]|\((?:\\.|[^\\()])*\))*\))|(<<|>>)|(<[0-9A-Fa-f\s]*>)|(\[|\])|'
                    rb'(/[^\s/\[\]<>(){}%]*)|([-+]?(?:\d+\.?\d*|\.\d+))|([A-Za-z\'"*0-9]+))')

def _tokens(data):
    pos = 0
    while pos < len(data):
        m = _TOKEN.match(data, pos)
        if not m or m.end() == pos:
            pos += 1
            continue
        pos = m.end()
        comment, string, dict_, hexs, arr, name, num, op = m.groups()
        if num is not None:
            yield ('n', float(num))
        elif op is not None:
            yield ('o', op.decode('latin-1'))
        elif name is not None:
            yield ('/', name.decode('latin-1'))
        elif dict_ is not None or arr is not None:
            yield ('d', (dict_ or arr).decode())
        else:
            yield ('s', None)

def _mul(m, n):
    a, b, c, d, e, f = m
    a2, b2, c2, d2, e2, f2 = n
    return (a * a2 + b * c2, a * b2 + b * d2, c * a2 + d * c2, c * b2 + d * d2, e * a2 + f * c2 + e2, e * b2 + f * d2 + f2)

def _pt(m, x, y):
    a, b, c, d, e, f = m
    return a * x + c * y + e, b * x + d * y + f

def _box(points):
    xs, ys = [p[0] for p in points], [p[1] for p in points]
    return (min(xs), min(ys), max(xs), max(ys)) if points else None

def page_draws(pdf, page_obj):
    d = show(pdf, page_obj).decode('latin-1')
    xo = {}
    m = re.search(r'/XObject <<(.*?)>>', d, re.S)
    if m:
        xo = {k: int(v) for k, v in re.findall(r'/(\w+) (\d+) 0 R', m.group(1))}
    contents = re.search(r'/Contents (?:\[(.*?)\]|(\d+) 0 R)', d, re.S)
    objs = [int(x) for x in re.findall(r'(\d+) 0 R', contents.group(1))] if contents.group(1) else [int(contents.group(2))]
    data = b'\n'.join(show(pdf, o, raw=True) for o in objs)
    ident = (1, 0, 0, 1, 0, 0)
    state = {'ctm': ident, 'clip': None, 'fill': None}
    stack, operands, path, draws = [], [], [], []
    pending_clip = False
    in_text = False
    for kind, val in _tokens(data):
        if kind != 'o':
            operands.append(val)
            continue
        op, args = val, [a for a in operands if isinstance(a, float)] if val != 'Do' else operands
        operands = []
        if op == 'BT':
            in_text = True
        elif op == 'ET':
            in_text = False
        elif in_text:
            continue
        elif op == 'q':
            stack.append(dict(state))
        elif op == 'Q':
            state = stack.pop() if stack else state
        elif op == 'cm' and len(args) >= 6:
            state['ctm'] = _mul(tuple(args[-6:]), state['ctm'])
        elif op == 're' and len(args) >= 4:
            x, y, w, h = args[-4:]
            c = state['ctm']
            path += [_pt(c, x, y), _pt(c, x + w, y + h), _pt(c, x + w, y), _pt(c, x, y + h)]
        elif op in ('m', 'l') and len(args) >= 2:
            path.append(_pt(state['ctm'], *args[-2:]))
        elif op == 'c' and len(args) >= 6:
            path += [_pt(state['ctm'], args[-6 + i], args[-5 + i]) for i in (0, 2, 4)]
        elif op in ('v', 'y') and len(args) >= 4:
            path += [_pt(state['ctm'], args[-4 + i], args[-3 + i]) for i in (0, 2)]
        elif op in ('W', 'W*'):
            pending_clip = True
        elif op in ('rg', 'g', 'k', 'sc', 'scn'):
            state['fill'] = tuple(args)
        elif op in ('f', 'f*', 'F', 'B', 'B*', 'b', 'b*', 'S', 's', 'n'):
            box = _box(path)
            if pending_clip and box:
                cl = state['clip']
                state['clip'] = box if not cl else (max(box[0], cl[0]), max(box[1], cl[1]), min(box[2], cl[2]), min(box[3], cl[3]))
            if op != 'n' and box:
                draws.append({'kind': 'stroke' if op in ('S', 's') else 'fill', 'op': op, 'box': box,
                              'colour': state['fill'], 'clip': state['clip']})
            path, pending_clip = [], False
        elif op == 'Do' and args:
            name = str(args[-1]).lstrip('/')
            ctm = state['ctm']
            dr = {'kind': 'image', 'name': name, 'obj': xo.get(name), 'ctm': ctm,
                  'box': _box([_pt(ctm, 0, 0), _pt(ctm, 1, 0), _pt(ctm, 0, 1), _pt(ctm, 1, 1)]), 'clip': state['clip']}
            if dr['obj']:
                dr.update(image_info(pdf, dr['obj']))
            draws.append(dr)
    return draws

_info_cache = {}

def image_info(pdf, obj):
    key = (pdf, obj)
    if key not in _info_cache:
        d = show(pdf, obj).decode('latin-1')
        w, h = re.search(r'/Width (\d+)', d), re.search(r'/Height (\d+)', d)
        filt = re.search(r'/Filter /(\w+)', d)
        cs = re.search(r'/ColorSpace /(\w+)', d)
        _info_cache[key] = {'w': int(w.group(1)) if w else None, 'h': int(h.group(1)) if h else None,
                            'filter': filt.group(1) if filt else None, 'cs': cs.group(1) if cs else None,
                            'smask': _ref(d, 'SMask'), 'subtype': 'Form' if '/Subtype /Form' in d else 'Image'}
    return _info_cache[key]

def alpha_of(pdf, obj):
    """An image's soft mask as floats 0..1 (rows top first), and whether it has /Matte; (None, False) without one."""
    i = image_info(pdf, obj)
    if not i['smask']:
        return None, False
    s = image_info(pdf, i['smask'])
    a = np.frombuffer(show(pdf, i['smask'], raw=True), np.uint8)[:s['w'] * s['h']].reshape(s['h'], s['w']) / 255.0
    return a, '/Matte' in show(pdf, i['smask']).decode('latin-1')

def pixels(pdf, obj):
    """(rgb, alpha): rgb None unless the image is Flate; alpha None without a soft mask. A /Matte [0 0 0] mask's colour
    is premultiplied against black, and is divided out here."""
    i = image_info(pdf, obj)
    alpha, matte = alpha_of(pdf, obj)
    rgb = None
    if i['filter'] == 'FlateDecode':
        n = 3 if i['cs'] == 'DeviceRGB' else 1
        rgb = np.frombuffer(show(pdf, obj, raw=True), np.uint8)[:i['w'] * i['h'] * n].reshape(i['h'], i['w'], n) / 255.0
        if matte and alpha is not None:
            with np.errstate(divide='ignore', invalid='ignore'):
                rgb = np.where(alpha[..., None] > 0, np.clip(rgb / alpha[..., None], 0, 1), 0)
    return rgb, alpha
