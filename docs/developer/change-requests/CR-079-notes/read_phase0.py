# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Word's reading of the cr079-phase0 files, from the bridge's results. usage: read_phase0.py <bridge dir>"""
import glob, json, os, re, subprocess, sys, zipfile

def words(x):
    return re.findall(r'<w:t(?: [^>]*)?>([^<]*)', x)

def clean(x):
    x = re.sub(r' w:rsid\w*="[^"]*"', '', x)
    x = re.sub(r' w14:(paraId|textId)="[^"]*"', '', x)
    return re.sub(r' xmlns:\w+="[^"]*"', '', x)

def read(d, n):
    r = json.load(open(d + '/result.json', encoding='utf-8-sig'))
    out = {'opened': r.get('opened'), 'dialogs': [q.get('text', '').replace('\n', ' ')[:60] for q in r.get('dialogs', [])]}
    f = f'{d}/{n}.word.docx'
    if not os.path.exists(f):
        return out
    z = zipfile.ZipFile(f)
    x = clean(z.read('word/document.xml').decode('utf8'))
    out['body'] = words(x)
    out['bg'] = re.findall(r'<w:background [^>]*w:color="(\w+)"', x)
    out['ignorable'] = re.findall(r'mc:Ignorable="([^"]*)"', x)[:1]
    for p in z.namelist():
        m = re.match(r'word/(header|footer|footnotes|endnotes|comments)\d*\.xml', p)
        if m:
            w = words(z.read(p).decode('utf8'))
            if w: out[m.group(1)] = w
    if 'word/styles.xml' in z.namelist():
        s = z.read('word/styles.xml').decode('utf8')
        m = re.search(r'w:styleId="Probe".*?</w:style>', s, re.S)
        if m: out['Probe jc'] = re.findall(r'<w:jc w:val="(\w+)"', m.group())
    if 'word/numbering.xml' in z.namelist():
        out['lvlText'] = sorted(set(re.findall(r'<w:lvlText w:val="([^"]*)"', z.read('word/numbering.xml').decode('utf8'))))[:4]
    if 'word/settings.xml' in z.namelist():
        out['tab'] = re.findall(r'<w:defaultTabStop w:val="(\d+)"', z.read('word/settings.xml').decode('utf8'))
    body = x[x.find('<w:body'):]
    out['pPr'] = re.findall(r'<w:pPr>.*?</w:pPr>', body, re.S)[:2]
    out['sdt'] = body.count('<w:sdtContent>')
    out['txbx'] = body.count('<w:txbxContent>')
    out['rows'] = body.count('<w:tr>') + body.count('<w:tr ')
    out['cells'] = body.count('<w:tc>')
    t = f'{d}/{n}.txt'
    out['txt'] = open(t, encoding='utf-8-sig', errors='replace').read().split('\n') if os.path.exists(t) else None
    pdf = f'{d}/{n}.pdf'
    out['pdf'] = subprocess.run(['pdftotext', pdf, '-'], capture_output=True, text=True).stdout.split('\n') if os.path.exists(pdf) else None
    return out

def main(bridge):
    order = [l.strip() for l in open(bridge + '/staging/cr079-phase0.order.txt') if l.strip()]
    names = list(dict.fromkeys(j[:-2] for j in order))
    for n in names:
        a, b = read(f'{bridge}/portfolio/{n}-a', n), read(f'{bridge}/portfolio/{n}-b', n)
        same = {k: v for k, v in a.items() if k not in ('dialogs',)} == {k: v for k, v in b.items() if k not in ('dialogs',)}
        show = {k: v for k, v in a.items() if v and k not in ('opened', 'txt', 'pdf', 'dialogs')}
        pdf = ' '.join(w for w in (a.get('pdf') or []) if w.strip())
        print(n[4:].ljust(32), a['opened'], '' if same else 'RUNS DIFFER', json.dumps(show)[:340], '| PDF:', pdf[:90], a['dialogs'][:1] if a['dialogs'] else '')

if __name__ == '__main__':
    main(sys.argv[1])
