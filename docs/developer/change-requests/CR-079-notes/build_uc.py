# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Choice-only probes for the namespaces understood-namespaces.txt lacks (WD 0.97 REQ-305, REQ-306, its OPEN (a)).
Jason: "add the three now and run a round of Choice-only files over the rest". Each file is the req305 check's: a block-level
mc:AlternateContent whose one mc:Choice requires the namespace, with no mc:Fallback. Word shows CHOICE WORDS if it understands
the namespace, and neither if it doesn't. The rest: every Office namespace declared anywhere in the corpora, or on Word's roots,
that the list lacks, and c16r2, which the WD names.
usage: build_uc.py <staging dir> <set name>"""
import hashlib, json, os, sys, zipfile

W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
MC = 'http://schemas.openxmlformats.org/markup-compatibility/2006'
SECT = ('<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" '
        'w:header="708" w:footer="708" w:gutter="0"/></w:sectPr>')
FILES = [
    ('uc-00-control-w14', 'w14', 'http://schemas.microsoft.com/office/word/2010/wordml'),            # shows: req305's control
    ('uc-01-unknown-ms', 'z', 'http://schemas.microsoft.com/office/word/2099/wordml/madeup'),        # doesn't: req305's
    ('uc-10-cx4', 'cx4', 'http://schemas.microsoft.com/office/drawing/2016/5/10/chartex'),
    ('uc-11-cx5', 'cx5', 'http://schemas.microsoft.com/office/drawing/2016/5/11/chartex'),
    ('uc-12-cx6', 'cx6', 'http://schemas.microsoft.com/office/drawing/2016/5/12/chartex'),
    ('uc-13-cx7', 'cx7', 'http://schemas.microsoft.com/office/drawing/2016/5/13/chartex'),
    ('uc-14-cx8', 'cx8', 'http://schemas.microsoft.com/office/drawing/2016/5/14/chartex'),
    ('uc-20-c16r2', 'c16r2', 'http://schemas.microsoft.com/office/drawing/2015/06/chart'),
    ('uc-21-mo', 'mo', 'http://schemas.microsoft.com/office/mac/office/2008/main'),
    ('uc-22-ax', 'ax', 'http://schemas.microsoft.com/office/2006/activeX'),
    ('uc-23-arto', 'arto', 'http://schemas.microsoft.com/office/word/2006/arto'),
    ('uc-24-xsi', 'xsi', 'http://www.w3.org/2001/XMLSchema-instance'),
    ('uc-25-dcmitype', 'dcmitype', 'http://purl.org/dc/dcmitype/'),
]
CT = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
      '<Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>')
RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>')

def p(words):
    return f'<w:p><w:r><w:t xml:space="preserve">{words}</w:t></w:r></w:p>'

def document(prefix, uri):
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            f'<w:document xmlns:mc="{MC}" xmlns:w="{W}" xmlns:{prefix}="{uri}"><w:body>{p("Case in body")}'
            f'<mc:AlternateContent><mc:Choice Requires="{prefix}">{p("CHOICE WORDS")}</mc:Choice></mc:AlternateContent>'
            f'{p("LAST LINE")}{SECT}</w:body></w:document>')

def entry(z, name, text):
    i = zipfile.ZipInfo(name, (2026, 11, 7, 0, 0, 0))
    i.compress_type = zipfile.ZIP_DEFLATED
    z.writestr(i, text.encode('utf-8'))

def main(staging, setname):
    sums, order = [], []
    for name, prefix, uri in FILES:
        for copy in 'ab':
            job = f'{name}-{copy}'
            d = os.path.join(staging, setname, job)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            with zipfile.ZipFile(f, 'w') as z:
                entry(z, '[Content_Types].xml', CT)
                entry(z, '_rels/.rels', RELS)
                entry(z, 'word/document.xml', document(prefix, uri))
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as o:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 120, 'formats': ['docx', 'txt', 'pdf']}, o)
            for x in (j, f):
                sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {setname}/{job}/{os.path.basename(x)}\n')
            order.append(job + '\n')
    open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
    print(len(FILES), 'files,', len(order), 'jobs')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
