# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Word's reading of the mr-* files, from the bridge's results. usage: read_mr.py <bridge/portfolio dir> <order file>
One line a file: how it opened, in both runs, and what Word's save holds. The two runs are compared with ids stripped."""
import glob, json, os, re, sys, zipfile

def norm(x):
    x = re.sub(r' w:rsid\w*="[^"]*"', '', x)
    x = re.sub(r' w14:(paraId|textId)="[^"]*"', '', x)
    return x

def story(x):
    b = x[x.find('<w:body'):]
    return re.sub(r'<w:sectPr.*?</w:sectPr>', '', b, flags=re.S)

def read(d, name):
    r = json.load(open(f'{d}/result.json', encoding='utf-8-sig'))
    out = {'opened': r.get('opened'), 'dialog': [q.get('text', '').split('\n')[1][:70] if '\n' in q.get('text', '') else q.get('text', '')[:70] for q in r.get('dialogs', [])]}
    s = f'{d}/{name}.word.docx'
    if not os.path.exists(s):
        out['save'] = None
        return out
    z = zipfile.ZipFile(s)
    x = norm(z.read('word/document.xml').decode('utf8'))
    out['save'] = x
    out['words'] = re.findall(r'<w:t(?: [^>]*)?>([^<]*)', x)
    out['paras'] = len(re.findall(r'<w:p[ >/]', story(x)))
    out['page'] = re.findall(r'<w:pgSz [^>]*>', x)
    out['shape'] = [t for t in ('w:tbl', 'w:sdt', 'w:jc', 'w:b/', 'w:pStyle') if f'<{t}' in x]
    other = {}
    for p in z.namelist():
        if re.match(r'word/(footnotes|endnotes|comments|header\d*|footer\d*)\.xml', p):
            w = re.findall(r'<w:t(?: [^>]*)?>([^<]*)', z.read(p).decode('utf8'))
            if w: other[p] = w
    out['other'] = other
    return out

def main(portfolio, order):
    jobs = [l.strip() for l in open(order) if l.strip()]
    names = []
    for j in jobs:
        n = j[:-2]
        if n not in names: names.append(n)
    for n in names:
        a, b = read(f'{portfolio}/{n}-a', n), read(f'{portfolio}/{n}-b', n)
        same = a['opened'] == b['opened'] and a.get('save') == b.get('save')
        line = f"{n}\t{a['opened']}/{b['opened']}\truns {'equal' if same else 'DIFFER'}"
        if a.get('save') is None:
            line += f"\tno save\t{a['dialog']}"
        else:
            line += f"\twords={a['words']}\tparas={a['paras']}\tpage={[re.sub(r'<w:pgSz |/>', '', p) for p in a['page']]}\tshape={a['shape']}\tother={a['other']}\t{a['dialog']}"
        print(line)

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
