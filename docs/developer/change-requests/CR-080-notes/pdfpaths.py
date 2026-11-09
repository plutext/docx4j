# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""What a page of Word's PDF draws, with each path's points: CR-081's interpreter (CR-081-notes/pdfdraws.py) records
only a path's box, and CR-080's geometry needs the path. Each draw is a dict:
  fill / stroke / fillstroke: op; subpaths, each a list of segments ('m', x, y), ('l', x, y),
      ('c', x1, y1, x2, y2, x3, y3), ('h',), in PDF points (y up); box; fill (colour tuple, or ('pattern', name));
      stroke (colour tuple); lw (the line width in points, scaled by the transform); cap, join, miter; dash (the
      array, scaled, and phase); alpha (fill, stroke: from the ExtGState); clip (the clip's box)
  shading: name, obj, the shading's dictionary as text, ctm, clip
  image: as pdfdraws
A pattern fill (gradients, the preset patterns and tiled pictures are all PDF patterns in Word's output) also carries
'pattern': the pattern's object and every object it refers to, as text, with a tiling pattern's cell stream.
Text is skipped. Used by read_shapes.py."""
import math, os, re, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'CR-081-notes'))
import pdfdraws as pd                                                     # noqa: E402  (show, pages, the tokenizer)

def _resources(pdf, page_obj):
    d = pd.show(pdf, page_obj).decode('latin-1')
    res = {}
    for kind in ('XObject', 'ExtGState', 'Shading', 'Pattern'):
        m = re.search(rf'/{kind} <<(.*?)>>', d, re.S)
        res[kind] = {k: int(v) for k, v in re.findall(r'/(\w+) (\d+) 0 R', m.group(1))} if m else {}
    contents = re.search(r'/Contents (?:\[(.*?)\]|(\d+) 0 R)', d, re.S)
    objs = [int(x) for x in re.findall(r'(\d+) 0 R', contents.group(1))] if contents.group(1) else [int(contents.group(2))]
    return res, objs

_gs_cache = {}

def _gs(pdf, obj):
    key = (pdf, obj)
    if key not in _gs_cache:
        d = pd.show(pdf, obj).decode('latin-1')
        ca = re.search(r'/ca ([\d.]+)', d)
        CA = re.search(r'/CA ([\d.]+)', d)
        _gs_cache[key] = (float(ca.group(1)) if ca else None, float(CA.group(1)) if CA else None)
    return _gs_cache[key]

_SKIP = ('/Parent', '/Group')

def resolve(pdf, obj, depth=3, limit=12):
    """An object and the objects it refers to, as {number: text}; a stream's decoded content (to 4000 characters)
    under the key 'stream N', except an image's."""
    out, todo = {}, [(obj, 0)]
    while todo and len(out) < limit:
        n, d = todo.pop(0)
        if n in out:
            continue
        text = pd.show(pdf, n).decode('latin-1')
        out[n] = text
        if 'stream' in text and '/Subtype /Image' not in text:
            out[f'stream {n}'] = pd.show(pdf, n, raw=True)[:4000].decode('latin-1')
        if d < depth:
            head = text.split('stream')[0]
            for key, ref in re.findall(r'(/\w+)?\s*(\d+) 0 R', head):
                if key not in _SKIP:
                    todo.append((int(ref), d + 1))
    return out

_pattern_cache = {}

def _scale(m):
    a, b, c, d, _, _ = m
    return math.sqrt(abs(a * d - b * c))

def page_paths(pdf, page_obj):
    res, objs = _resources(pdf, page_obj)
    data = b'\n'.join(pd.show(pdf, o, raw=True) for o in objs)
    ident = (1, 0, 0, 1, 0, 0)
    state = {'ctm': ident, 'clip': None, 'fill': None, 'stroke': None, 'lw': 1.0, 'cap': 0, 'join': 0, 'miter': 10.0,
             'dash': ([], 0), 'ca': 1.0, 'CA': 1.0, 'fillcs': None}
    stack, operands, subs, cur, draws = [], [], [], None, []
    pending_clip, in_text = False, False
    pts = []                                       # every point of the path, for its box

    def point(x, y):
        p = pd._pt(state['ctm'], x, y)
        pts.append(p)
        return p

    for kind, val in pd._tokens(data):
        if kind != 'o':
            operands.append((kind, val))
            continue
        op = val
        nums = [v for k, v in operands if k == 'n']
        names = [v for k, v in operands if k == '/']
        arrays = [v for k, v in operands if k == 'd']
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
        elif op == 'cm' and len(nums) >= 6:
            state['ctm'] = pd._mul(tuple(nums[-6:]), state['ctm'])
        elif op == 'w' and nums:
            state['lw'] = nums[-1]
        elif op == 'J' and nums:
            state['cap'] = int(nums[-1])
        elif op == 'j' and nums:
            state['join'] = int(nums[-1])
        elif op == 'M' and nums:
            state['miter'] = nums[-1]
        elif op == 'd':
            state['dash'] = (nums[:-1], nums[-1] if nums else 0)
        elif op == 'gs' and names:
            obj = res['ExtGState'].get(names[-1].lstrip('/'))
            if obj:
                ca, CA = _gs(pdf, obj)
                if ca is not None:
                    state['ca'] = ca
                if CA is not None:
                    state['CA'] = CA
        elif op in ('rg', 'g', 'k', 'sc'):
            state['fill'] = tuple(nums)
        elif op == 'scn':
            state['fill'] = ('pattern', names[-1].lstrip('/')) if names else tuple(nums)
        elif op in ('RG', 'G', 'K', 'SC', 'SCN'):
            state['stroke'] = tuple(nums)
        elif op == 'cs':
            state['fillcs'] = names[-1] if names else None
        elif op == 're' and len(nums) >= 4:
            x, y, w, h = nums[-4:]
            subs.append([('m',) + point(x, y), ('l',) + point(x + w, y), ('l',) + point(x + w, y + h),
                         ('l',) + point(x, y + h), ('h',)])
            cur = None
        elif op == 'm' and len(nums) >= 2:
            cur = [('m',) + point(*nums[-2:])]
            subs.append(cur)
        elif op == 'l' and len(nums) >= 2 and cur is not None:
            cur.append(('l',) + point(*nums[-2:]))
        elif op == 'c' and len(nums) >= 6 and cur is not None:
            cur.append(('c',) + point(*nums[-6:-4]) + point(*nums[-4:-2]) + point(*nums[-2:]))
        elif op in ('v', 'y') and len(nums) >= 4 and cur is not None:
            last = cur[-1][-2:]
            if op == 'v':
                cur.append(('c',) + tuple(last) + point(*nums[-4:-2]) + point(*nums[-2:]))
            else:
                p2 = point(*nums[-2:])
                cur.append(('c',) + point(*nums[-4:-2]) + p2 + p2)
        elif op == 'h' and cur is not None:
            cur.append(('h',))
        elif op in ('W', 'W*'):
            pending_clip = True
        elif op in ('f', 'f*', 'F', 'B', 'B*', 'b', 'b*', 'S', 's', 'n'):
            box = pd._box(pts)
            if pending_clip and box:
                cl = state['clip']
                state['clip'] = box if not cl else (max(box[0], cl[0]), max(box[1], cl[1]), min(box[2], cl[2]), min(box[3], cl[3]))
            if op != 'n' and box:
                if op in ('s', 'b', 'b*'):
                    for sp in subs:
                        if sp and sp[-1] != ('h',):
                            sp.append(('h',))
                kind_ = 'stroke' if op in ('S', 's') else 'fill' if op in ('f', 'f*', 'F') else 'fillstroke'
                sc = _scale(state['ctm'])
                draws.append({'kind': kind_, 'op': op, 'subpaths': subs, 'box': box, 'clip': state['clip'],
                              'fill': state['fill'] if kind_ != 'stroke' else None,
                              'stroke': state['stroke'] if kind_ != 'fill' else None,
                              'lw': state['lw'] * sc, 'cap': state['cap'], 'join': state['join'], 'miter': state['miter'],
                              'dash': ([v * sc for v in state['dash'][0]], state['dash'][1] * sc),
                              'alpha': (state['ca'], state['CA'])})
                fl = draws[-1]['fill']
                if fl and fl[0] == 'pattern':
                    obj = res['Pattern'].get(fl[1])
                    if obj and (pdf, obj) not in _pattern_cache:
                        _pattern_cache[(pdf, obj)] = resolve(pdf, obj)
                    draws[-1]['pattern'] = _pattern_cache.get((pdf, obj)) if obj else None
            subs, cur, pts, pending_clip = [], None, [], False
        elif op == 'sh' and names:
            name = names[-1].lstrip('/')
            obj = res['Shading'].get(name)
            draws.append({'kind': 'shading', 'name': name, 'obj': obj, 'ctm': state['ctm'], 'clip': state['clip'],
                          'dict': pd.show(pdf, obj).decode('latin-1') if obj else None, 'alpha': (state['ca'], state['CA'])})
        elif op == 'Do' and names:
            name = names[-1].lstrip('/')
            ctm = state['ctm']
            dr = {'kind': 'image', 'name': name, 'obj': res['XObject'].get(name), 'ctm': ctm, 'clip': state['clip'],
                  'box': pd._box([pd._pt(ctm, 0, 0), pd._pt(ctm, 1, 0), pd._pt(ctm, 0, 1), pd._pt(ctm, 1, 1)])}
            if dr['obj']:
                dr.update(pd.image_info(pdf, dr['obj']))
            draws.append(dr)
    return draws

def page_height(pdf, page_obj):
    m = re.search(r'/MediaBox \[\s*[-\d.]+\s+[-\d.]+\s+[-\d.]+\s+([-\d.]+)\s*\]', pd.show(pdf, page_obj).decode('latin-1'))
    return float(m.group(1)) if m else 841.92
