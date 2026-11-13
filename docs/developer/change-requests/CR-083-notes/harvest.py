# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 4: Word's VML WordArt read from its save (cr083-phase4a's wv40-shapetypes.docx): each v:shapetype's
o:spt, coordsize, adj, path, formulas, v:path and v:textpath, and each shape's attributes, text path and children.

    python3 -I harvest.py <word/document.xml> <out.json>

Prints a line a shape: its shapetype's o:spt, sub-path count, adj, formula count, text path and path.
"""
import re, sys, json
s = open(sys.argv[1], encoding='utf8').read()
types = {}
for m in re.finditer(r'<v:shapetype\b(.*?)</v:shapetype>', s, re.S):
    t = m.group(0)
    head = re.match(r'<v:shapetype\b([^>]*)>', t).group(1)
    a = dict(re.findall(r'([\w:]+)="([^"]*)"', head))
    f = re.findall(r'<v:f eqn="([^"]*)"', t)
    p = re.search(r'<v:path\b([^>]*)/?>', t)
    tp = re.search(r'<v:textpath\b([^>]*)/?>', t)
    types[a.get('id')] = dict(spt=a.get('o:spt'), coordsize=a.get('coordsize'), adj=a.get('adj'), path=a.get('path'),
        formulas=f, vpath=dict(re.findall(r'([\w:]+)="([^"]*)"', p.group(1))) if p else {},
        textpath=dict(re.findall(r'([\w:]+)="([^"]*)"', tp.group(1))) if tp else {})
shapes = []
for m in re.finditer(r'<v:shape\b([^>]*)>(.*?)</v:shape>', s, re.S):
    a = dict(re.findall(r'([\w:]+)="([^"]*)"', m.group(1)))
    tp = re.search(r'<v:textpath\b([^>]*)/?>', m.group(2))
    kids = re.findall(r'<(v:\w+|o:\w+|w10:\w+)\b([^>]*)/?>', m.group(2))
    shapes.append(dict(type=a.get('type'), attrs={k: v for k, v in a.items() if k not in ('id', 'o:spid')},
        textpath=dict(re.findall(r'([\w:]+)="([^"]*)"', tp.group(1))) if tp else {}, children=[(k, v.strip()) for k, v in kids]))
json.dump(dict(types=types, shapes=shapes), open(sys.argv[2], 'w'), indent=1)
for sh in shapes:
    t = types.get((sh['type'] or '').lstrip('#'), {})
    print(t.get('spt'), 'paths', len(re.findall(r'm', (t.get('path') or '').replace('ms', ''))) , 'adj', t.get('adj'), 'nF', len(t.get('formulas', [])), 'tp', t.get('textpath'), '| path', (t.get('path') or '')[:70])
