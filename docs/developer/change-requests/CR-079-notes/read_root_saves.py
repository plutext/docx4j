import json,os,subprocess,sys,hashlib,tempfile,glob
B=os.path.expanduser('~/vbShares/Office 2016/bridge/portfolio')
names=[l.split('\t')[0] for l in open(sys.argv[1])]
def norm(p):
    b=open(p,'rb').read()
    for enc in ('utf-16','utf-8-sig','cp1252'):
        try: t=b.decode(enc); break
        except Exception: pass
    return ' '.join(t.split())
def pages(pdf):
    d=tempfile.mkdtemp(dir=sys.argv[2]); subprocess.run(['pdftoppm','-r','50','-png',pdf,d+'/p'],check=True,capture_output=True)
    return [hashlib.sha256(open(f,'rb').read()).hexdigest() for f in sorted(glob.glob(d+'/p*.png'))]
out=[];bad=0
for n in names:
    a=f'{B}/rs-{n}-a'; b=f'{B}/rs-{n}-b'
    r=[json.load(open(x+'/result.json',encoding='utf-8-sig')) for x in (a,b)]
    op=[x.get('opened') for x in r]; dl=[len(x.get('dialogs') or []) for x in r]
    ta=norm(f'{a}/rs-{n}.txt'); tb=norm(f'{b}/rs-{n}.txt')
    pa=pages(f'{a}/rs-{n}.pdf'); pb=pages(f'{b}/rs-{n}.pdf')
    src=None
    for job in (f'{B}/{n}-a',f'{B}/{n}'):
        if os.path.isdir(job): src=job;break
    st=sp=None
    if src:
        if os.path.exists(f'{src}/{n}.txt'): st=norm(f'{src}/{n}.txt')
        if os.path.exists(f'{src}/{n}.pdf'): sp=pages(f'{src}/{n}.pdf')
    tv='no-source' if st is None else ('same' if st==ta else 'DIFF')
    pv='no-source' if sp is None else ('same' if sp==pa else 'DIFF(%d/%d pages)'%(len(pa),len(sp)))
    row=(n,'/'.join(op),sum(dl),'runs-same' if ta==tb and pa==pb else 'RUNS-DIFFER',tv,pv,ta[:44],(st or '')[:44])
    out.append(row); print('\t'.join(map(str,row)))
