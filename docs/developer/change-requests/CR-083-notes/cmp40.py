# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 4: docx4j's PDF of wv40-shapetypes against Word's, page by page (a type a page): the box of each
page's filled glyph paths, and the largest difference of its four edges.

    python3 -I cmp40.py <Word's pdf> <docx4j's pdf> <harvest.json>
"""
import sys, re, subprocess, json
def bbox(pdf, page):
    out = subprocess.run(['mutool','trace',pdf,str(page)],capture_output=True,text=True).stdout
    xs=[];ys=[];inpath=False;n=0;t=None
    for line in out.splitlines():
        if '<fill_path' in line:
            inpath=True; n+=1
            t=[float(v) for v in re.search(r'transform="([^"]+)"',line).group(1).split()]; continue
        if inpath and '</fill_path>' in line: inpath=False; continue
        if inpath:
            for x,y in re.findall(r'x\d?="([-\d.]+)" y\d?="([-\d.]+)"',line):
                x=float(x);y=float(y); xs.append(t[0]*x+t[2]*y+t[4]); ys.append(t[1]*x+t[3]*y+t[5])
    return (n, min(xs),max(xs),min(ys),max(ys)) if xs else (n,)
word, ours, harvest = sys.argv[1], sys.argv[2], json.load(open(sys.argv[3]))
spts = [harvest['types'][s['type'].lstrip('#')]['spt'] for s in harvest['shapes']]
for p in range(1, 41):
    w = bbox(word, p); o = bbox(ours, p)
    if len(w) == 1 or len(o) == 1:
        print(p, spts[p-1], 'word', w, 'ours', o); continue
    d = max(abs(w[i]-o[i]) for i in range(1,5))
    print('%2d %s word x %.1f..%.1f y %.1f..%.1f | ours x %.1f..%.1f y %.1f..%.1f | max %.1f' % (p, spts[p-1], w[1],w[2],w[3],w[4], o[1],o[2],o[3],o[4], d))
