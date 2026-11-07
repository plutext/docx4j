# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Word's reading of the rels-namespace files, from the bridge's results. usage: read_rels_namespace.py <bridge dir>"""
import glob, json, os, re, subprocess, sys, zipfile

def read(d, n):
    r = json.load(open(d + '/result.json', encoding='utf-8-sig'))
    o = {'opened': r.get('opened'), 'dialogs': len(r.get('dialogs', [])), 'first': ((r.get('dialogs') or [{}])[0].get('text') or '').replace('\n', ' ')[:90]}
    f = '%s/%s.word.docx' % (d, n)
    if not os.path.exists(f):
        return o
    z = zipfile.ZipFile(f)
    names = z.namelist()
    x = z.read('word/document.xml').decode('utf8')
    s = z.read('word/styles.xml').decode('utf8') if 'word/styles.xml' in names else ''
    m = re.search(r'w:styleId="Probe".*?</w:style>', s, re.S)
    o['Probe style'] = bool(m)
    o['Probe jc'] = (re.findall(r'<w:jc w:val="(\w+)"', m.group()) if m else [])
    o['pStyle'] = re.findall(r'<w:pStyle w:val="(\w+)"', x)
    st = z.read('word/settings.xml').decode('utf8') if 'word/settings.xml' in names else ''
    o['tab'] = re.findall(r'<w:defaultTabStop w:val="(\d+)"', st)
    o['body'] = re.findall(r'<w:t(?: [^>]*)?>([^<]*)', x)
    o['hdr'] = [w for p in names if re.match(r'word/header\d*\.xml', p) for w in re.findall(r'<w:t(?: [^>]*)?>([^<]*)', z.read(p).decode('utf8'))]
    o['note'] = re.findall(r'<w:t(?: [^>]*)?>([^<]*)', z.read('word/footnotes.xml').decode('utf8')) if 'word/footnotes.xml' in names else []
    o['refs'] = (x.count('headerReference'), x.count('footnoteReference'))
    t = '%s/%s.pdf' % (d, n)
    o['pdf'] = ' '.join(w for w in subprocess.run(['pdftotext', t, '-'], capture_output=True, text=True).stdout.split() ) if os.path.exists(t) else None
    return o

def main(bridge):
    order = [l.strip() for l in open(bridge + '/staging/rels-namespace.order.txt') if l.strip()]
    for n in dict.fromkeys(j[:-2] for j in order):
        a, b = read('%s/portfolio/%s-a' % (bridge, n), n), read('%s/portfolio/%s-b' % (bridge, n), n)
        same = all(a.get(k) == b.get(k) for k in a if k != 'first')
        print(n[3:].ljust(24), str(a['opened']).ljust(9), 'dlg', a['dialogs'], '' if same else 'RUNS DIFFER', json.dumps({k: v for k, v in a.items() if k not in ('opened', 'dialogs', 'first')}))
    print(read('%s/portfolio/rn-02-plain-wrong-a' % bridge, 'rn-02-plain-wrong')['first'])

if __name__ == '__main__':
    main(sys.argv[1])
