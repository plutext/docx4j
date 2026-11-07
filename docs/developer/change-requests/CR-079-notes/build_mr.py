# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Purpose-built probes for an unknown root in word/document.xml (CR-079 D9, WD 0.96 REQ-318 to REQ-320).
Jason: "let's do a round for w:madeUp holding a w:p now". Word opens w:madeUp holding a w:p (or-31), and core-ts's madeup round
(mu-10 to mu-12) showed that the content decides for a root in the w: namespace: this asks how far that reading goes.
The package is or-*'s: [Content_Types].xml, _rels/.rels, word/document.xml. Each file's words say where they stand.
usage: build_mr.py <staging dir> <set name>"""
import hashlib, json, os, sys, zipfile

W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
XW = f'xmlns:w="{W}"'
Z = 'xmlns:z="urn:docx4j:check79"'
MC = 'xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"'
W14 = 'xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"'
DECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
MARGINS = 'w:header="708" w:footer="708" w:gutter="0"/>'
SECT = f'<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" {MARGINS}</w:sectPr>'
LAND = f'<w:sectPr><w:pgSz w:w="15840" w:h="12240" w:orient="landscape"/><w:pgMar w:top="720" w:right="720" w:bottom="720" w:left="720" {MARGINS}</w:sectPr>'

def run(words, rpr=''):
    return f'<w:r>{rpr}<w:t xml:space="preserve">{words}</w:t></w:r>'

def p(words, ppr='', rpr=''):
    return f'<w:p>{ppr}{run(words, rpr)}</w:p>'

def doc(words):
    return f'<w:document><w:body>{p(words)}{SECT}</w:body></w:document>'

def root(name, inner, attrs=XW):
    return f'<{name} {attrs}>{inner}</{name}>'

TBL = ('<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/></w:tblPr><w:tblGrid><w:gridCol w:w="4000"/></w:tblGrid><w:tr><w:tc><w:tcPr>'
       f'<w:tcW w:w="4000" w:type="dxa"/></w:tcPr>{p("CELL WORDS")}</w:tc></w:tr></w:tbl>{p("AFTER TABLE WORDS")}')
SDT = f'<w:sdt><w:sdtPr><w:id w:val="12345"/></w:sdtPr><w:sdtContent>{p("CONTROL WORDS")}</w:sdtContent></w:sdt>'
BRANCH = (f'<mc:AlternateContent {MC} {XW} {W14}><mc:Choice Requires="w14">{root("w:madeUp", p("CHOICE WORDS"), "").replace("w:madeUp >", "w:madeUp>")}'
          f'</mc:Choice><mc:Fallback>{doc("FALLBACK WORDS")}</mc:Fallback></mc:AlternateContent>')
STYLE = '<w:pPr><w:pStyle w:val="Probe"/></w:pPr>'
STYLES = (f'{DECL}<w:styles {XW}><w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style>'
          '<w:style w:type="paragraph" w:styleId="Probe"><w:name w:val="Probe"/><w:basedOn w:val="Normal"/><w:pPr><w:jc w:val="right"/></w:pPr></w:style></w:styles>')

FILES = [
    # The control, and or-31 again under this round's Word
    ('mr-00-control', root('w:document', f'<w:body>{p("CONTROL WORDS")}{SECT}</w:body>')),
    ('mr-01-madeup-p', root('w:madeUp', p('MADEUP WORDS'))),
    # w:foo holding one w:document: rw-12 held two, core-ts's mu-10 was w:madeUp
    ('mr-03-foo-document', root('w:foo', doc('DOCUMENT WORDS'))),
    # How far the reading goes
    ('mr-10-two-p', root('w:madeUp', p('FIRST WORDS') + p('SECOND WORDS'))),
    ('mr-11-p-sectpr', root('w:madeUp', p('SECTION WORDS') + LAND)),
    ('mr-12-tbl', root('w:madeUp', TBL)),
    ('mr-13-sdt', root('w:madeUp', SDT)),
    ('mr-14-run', root('w:madeUp', run('RUN WORDS'))),
    ('mr-15-body', root('w:madeUp', f'<w:body>{p("BODY WORDS")}{LAND}</w:body>')),
    ('mr-16-empty', f'<w:madeUp {XW}/>'),
    ('mr-17-nested', root('w:madeUp', f'<w:other>{p("NESTED WORDS")}</w:other>')),
    ('mr-18-p-properties', root('w:madeUp', p('PROPERTY WORDS', '<w:pPr><w:jc w:val="center"/></w:pPr>', '<w:rPr><w:b/></w:rPr>'))),
    # The namespace
    ('mr-20-other-namespace-p', root('z:madeUp', p('OTHER NAMESPACE WORDS'), f'{Z} {XW}')),
    ('mr-21-no-namespace-p', root('madeUp', p('NO NAMESPACE WORDS'))),
    ('mr-22-ignorable-p', root('z:madeUp', p('IGNORABLE WORDS'), f'{MC} {Z} {XW} mc:Ignorable="z"')),
    # A known name that is no part's root: does "unknown" decide?
    ('mr-30-sdtcontent-p', root('w:sdtContent', p('SDTCONTENT WORDS'))),
    ('mr-31-tc-p', root('w:tc', p('CELL WORDS'))),
    ('mr-32-p-root', f'<w:p {XW}>{run("PARAGRAPH WORDS")}</w:p>'),
    # In a branch taken, and in a package with a styles part
    ('mr-40-branch-madeup-p', BRANCH),
    ('mr-50-madeup-p-styles', root('w:madeUp', p('STYLED WORDS', STYLE))),
]

CT = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
      '<Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>{}</Types>')
CT_STYLES = '<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>'
RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>')
DOC_RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>')

def entry(z, name, text):
    i = zipfile.ZipInfo(name, (2026, 11, 7, 0, 0, 0))
    i.compress_type = zipfile.ZIP_DEFLATED
    z.writestr(i, text.encode('utf-8'))

def build(path, name, xml):
    styled = name.endswith('-styles')
    with zipfile.ZipFile(path, 'w') as z:
        entry(z, '[Content_Types].xml', CT.format(CT_STYLES if styled else ''))
        entry(z, '_rels/.rels', RELS)
        entry(z, 'word/document.xml', DECL + xml)
        if styled:
            entry(z, 'word/_rels/document.xml.rels', DOC_RELS)
            entry(z, 'word/styles.xml', STYLES)

def main(staging, setname):
    sums, order = [], []
    for name, xml in FILES:
        for copy in 'ab':
            job = f'{name}-{copy}'
            d = os.path.join(staging, setname, job)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            build(f, name, xml)
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as o:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 120, 'formats': ['docx', 'txt', 'pdf']}, o)
            for x in (j, f):
                h = hashlib.sha256(open(x, 'rb').read()).hexdigest()
                sums.append(f'{h}  {setname}/{job}/{os.path.basename(x)}\n')
            order.append(job + '\n')
    open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
    print(len(FILES), 'files,', len(order), 'jobs')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
