# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-079 phase 0: what Word hasn't been asked. Purpose-built files; each word says where it stands.
usage: build_phase0.py <staging dir> <set>"""
import hashlib, json, os, sys, zipfile

W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
XW = f'xmlns:w="{W}"'
XR = 'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"'
XMC = 'xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"'
X14 = 'xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"'
XZ = 'xmlns:z="urn:docx4j:cr079"'
XWPS = 'xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"'
XWP = 'xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"'
XA = 'xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"'
DECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
SECT_INNER = ('<w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" '
              'w:header="708" w:footer="708" w:gutter="0"/>')
SECT = f'<w:sectPr>{SECT_INNER}</w:sectPr>'
ODOC = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships/'
CTW = 'application/vnd.openxmlformats-officedocument.wordprocessingml.'
ROOTS = f'{XW} {XR}'

def run(t, rpr=''):
    return f'<w:r>{rpr}<w:t xml:space="preserve">{t}</w:t></w:r>'

def p(t, ppr='', rpr=''):
    return f'<w:p>{ppr}{run(t, rpr)}</w:p>'

def document(body, attrs=ROOTS, before=''):
    return f'<w:document {attrs}>{before}<w:body>{body}</w:body></w:document>'

def inner_document(body, before='', attrs=''):
    return f'<w:document{(" " + attrs) if attrs else ""}>{before}<w:body>{body}{SECT}</w:body></w:document>'

def root_ac(choice, fallback, requires='w14', extra=''):
    fb = f'<mc:Fallback>{fallback}</mc:Fallback>' if fallback is not None else ''
    return f'<mc:AlternateContent {XMC} {ROOTS} {X14} {extra}><mc:Choice Requires="{requires}">{choice}</mc:Choice>{fb}</mc:AlternateContent>'

def footnotes(tag, words):
    item = tag[:-1]
    sep = (f'<w:{item} w:type="separator" w:id="-1"><w:p><w:r><w:separator/></w:r></w:p></w:{item}>'
           f'<w:{item} w:type="continuationSeparator" w:id="0"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:{item}>')
    return f'<w:{tag}>{sep}<w:{item} w:id="1">{p(words)}</w:{item}></w:{tag}>'

def styles(jc):
    return (f'<w:styles><w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style>'
            f'<w:style w:type="paragraph" w:styleId="Probe"><w:name w:val="Probe"/><w:basedOn w:val="Normal"/><w:pPr><w:jc w:val="{jc}"/></w:pPr></w:style></w:styles>')

def numbering(text):
    return (f'<w:numbering><w:abstractNum w:abstractNumId="0"><w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/>'
            f'<w:lvlText w:val="{text}%1"/><w:lvlJc w:val="left"/></w:lvl></w:abstractNum><w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num></w:numbering>')

NUM2 = ('<w:numbering {x}>'
        '<w:abstractNum w:abstractNumId="0"><w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="ONE%1"/><w:lvlJc w:val="left"/></w:lvl></w:abstractNum>'
        '<w:abstractNum w:abstractNumId="1"><w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="TWO%1"/><w:lvlJc w:val="left"/></w:lvl></w:abstractNum>'
        '<w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num><w:num w:numId="2"><w:abstractNumId w:val="1"/></w:num></w:numbering>').format(x=XW)

def part(name, ctype, rel, xml):
    return (name, ctype, rel, xml)

HDR_REF = f'<w:sectPr><w:headerReference w:type="default" r:id="rId9"/>{SECT_INNER}</w:sectPr>'
FTR_REF = f'<w:sectPr><w:footerReference w:type="default" r:id="rId9"/>{SECT_INNER}</w:sectPr>'
TBL_HEAD = '<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/></w:tblPr><w:tblGrid><w:gridCol w:w="4000"/></w:tblGrid>'
def tc(t): return f'<w:tc><w:tcPr><w:tcW w:w="4000" w:type="dxa"/></w:tcPr>{p(t)}</w:tc>'
WPS = (f'<w:p><w:r><mc:AlternateContent><mc:Choice Requires="wps"><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="2000000" cy="800000"/>'
       '<wp:docPr id="1" name="Box"/><a:graphic><a:graphicData uri="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"><wps:wsp><wps:cNvSpPr/><wps:spPr>'
       '<a:xfrm><a:off x="0" y="0"/><a:ext cx="2000000" cy="800000"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></wps:spPr><wps:txbx>'
       f'<w:txbxContent>{p("FIRST BOX WORDS")}</w:txbxContent><w:txbxContent>{p("SECOND BOX WORDS")}</w:txbxContent></wps:txbx><wps:bodyPr/></wps:wsp></a:graphicData></a:graphic>'
       '</wp:inline></w:drawing></mc:Choice></mc:AlternateContent></w:r></w:p>')

FILES = [
    ('c79-00-control', document(p('CONTROL WORDS') + SECT), []),
    # D1: mc:AlternateContent as another part's root
    ('c79-10-hdr-root', document(p('BODY WORDS') + HDR_REF), [part('word/header1.xml', CTW + 'header+xml', ODOC + 'header', root_ac(f'<w:hdr>{p("CHOICE HEADER")}</w:hdr>', f'<w:hdr>{p("FALLBACK HEADER")}</w:hdr>'))]),
    ('c79-11-ftr-root', document(p('BODY WORDS') + FTR_REF), [part('word/footer1.xml', CTW + 'footer+xml', ODOC + 'footer', root_ac(f'<w:ftr>{p("CHOICE FOOTER")}</w:ftr>', f'<w:ftr>{p("FALLBACK FOOTER")}</w:ftr>'))]),
    ('c79-12-footnotes-root', document('<w:p>' + run('BODY WORDS') + '<w:r><w:footnoteReference w:id="1"/></w:r></w:p>' + SECT), [part('word/footnotes.xml', CTW + 'footnotes+xml', ODOC + 'footnotes', root_ac(footnotes('footnotes', 'CHOICE NOTE'), footnotes('footnotes', 'FALLBACK NOTE')))]),
    ('c79-13-endnotes-root', document('<w:p>' + run('BODY WORDS') + '<w:r><w:endnoteReference w:id="1"/></w:r></w:p>' + SECT), [part('word/endnotes.xml', CTW + 'endnotes+xml', ODOC + 'endnotes', root_ac(footnotes('endnotes', 'CHOICE NOTE'), footnotes('endnotes', 'FALLBACK NOTE')))]),
    ('c79-14-comments-root', document('<w:p><w:commentRangeStart w:id="0"/>' + run('BODY WORDS') + '<w:commentRangeEnd w:id="0"/><w:r><w:commentReference w:id="0"/></w:r></w:p>' + SECT),
     [part('word/comments.xml', CTW + 'comments+xml', ODOC + 'comments', root_ac(f'<w:comments><w:comment w:id="0" w:author="A">{p("CHOICE COMMENT")}</w:comment></w:comments>', f'<w:comments><w:comment w:id="0" w:author="A">{p("FALLBACK COMMENT")}</w:comment></w:comments>'))]),
    ('c79-15-styles-root', document(p('STYLED WORDS', '<w:pPr><w:pStyle w:val="Probe"/></w:pPr>') + SECT), [part('word/styles.xml', CTW + 'styles+xml', ODOC + 'styles', root_ac(styles('right'), styles('center')))]),
    ('c79-16-numbering-root', document(p('LISTED WORDS', '<w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr>') + SECT), [part('word/numbering.xml', CTW + 'numbering+xml', ODOC + 'numbering', root_ac(numbering('CHOICE'), numbering('FALLBACK')))]),
    ('c79-17-settings-root', document(p('BODY WORDS') + SECT), [part('word/settings.xml', CTW + 'settings+xml', ODOC + 'settings', root_ac('<w:settings><w:defaultTabStop w:val="1000"/></w:settings>', '<w:settings><w:defaultTabStop w:val="2000"/></w:settings>'))]),
    ('c79-18-hdr-root-unknown-choice', document(p('BODY WORDS') + HDR_REF), [part('word/header1.xml', CTW + 'header+xml', ODOC + 'header', root_ac(f'<w:hdr>{p("CHOICE HEADER")}</w:hdr>', f'<w:hdr>{p("FALLBACK HEADER")}</w:hdr>', 'z', XZ))]),
    # two w:document in a branch
    ('c79-20-two-docs-background-first', root_ac(inner_document(p('FIRST WORDS'), '<w:background w:color="FF0000"/>') + inner_document(p('SECOND WORDS')), None), []),
    ('c79-21-two-docs-background-second', root_ac(inner_document(p('FIRST WORDS')) + inner_document(p('SECOND WORDS'), '<w:background w:color="00FF00"/>'), None), []),
    ('c79-22-two-docs-ignorable-first', root_ac(inner_document(p('FIRST WORDS'), attrs='mc:Ignorable="z"') + inner_document(f'<w:p><z:x>{run("HELD WORDS")}</z:x>{run("SECOND WORDS")}</w:p>'), None, extra=XZ), []),
    # sdtContent and txbxContent given twice
    ('c79-30-sdtcontent-run', document('<w:p><w:sdt><w:sdtPr><w:id w:val="1"/></w:sdtPr><w:sdtContent>' + run('FIRST RUN') + '</w:sdtContent><w:sdtContent>' + run('SECOND RUN') + '</w:sdtContent></w:sdt></w:p>' + SECT), []),
    ('c79-31-sdtcontent-row', document(TBL_HEAD + f'<w:sdt><w:sdtPr><w:id w:val="1"/></w:sdtPr><w:sdtContent><w:tr>{tc("FIRST ROW")}</w:tr></w:sdtContent><w:sdtContent><w:tr>{tc("SECOND ROW")}</w:tr></w:sdtContent></w:sdt></w:tbl>' + p('AFTER') + SECT), []),
    ('c79-32-sdtcontent-cell', document(TBL_HEAD + f'<w:tr><w:sdt><w:sdtPr><w:id w:val="1"/></w:sdtPr><w:sdtContent>{tc("FIRST CELL")}</w:sdtContent><w:sdtContent>{tc("SECOND CELL")}</w:sdtContent></w:sdt></w:tr></w:tbl>' + p('AFTER') + SECT), []),
    ('c79-33-txbx-wps', document(WPS + SECT, f'{ROOTS} {XMC} {XWPS} {XWP} {XA}'), []),
    # properties one level further in
    ('c79-40-ppr-nested-rpr', document('<w:p><w:pPr><w:rPr><w:b/></w:rPr></w:pPr><w:pPr><w:rPr><w:i/></w:rPr></w:pPr>' + run('MARKED WORDS') + '</w:p>' + SECT), []),
    ('c79-41-ppr-numpr', document('<w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr><w:pPr><w:numPr><w:numId w:val="2"/></w:numPr></w:pPr>' + run('LISTED WORDS') + '</w:p>' + SECT),
     [part('word/numbering.xml', CTW + 'numbering+xml', ODOC + 'numbering', NUM2)]),
    ('c79-42-ppr-pprchange', document('<w:p><w:pPr><w:jc w:val="center"/><w:pPrChange w:id="1" w:author="A"><w:pPr><w:jc w:val="left"/></w:pPr></w:pPrChange></w:pPr><w:pPr><w:ind w:left="720"/></w:pPr>' + run('CHANGED WORDS') + '</w:p>' + SECT), []),
    # an unknown element in a branch at the root
    ('c79-50-unknown-in-taken', root_ac(inner_document(f'<w:p>{run("CHOICE WORDS")}<w:madeUp/></w:p>'), inner_document(p('FALLBACK WORDS'))), []),
    ('c79-51-unknown-in-not-taken', root_ac(inner_document(p('CHOICE WORDS')), inner_document(f'<w:p>{run("FALLBACK WORDS")}<w:madeUp/></w:p>')), []),
    # core-ts's: a w:hdr root with a note
    ('c79-60-hdr-root-footnote', f'<w:hdr {ROOTS}><w:p>{run("HEADER WORDS")}<w:r><w:footnoteReference w:id="1"/></w:r></w:p></w:hdr>', [part('word/footnotes.xml', CTW + 'footnotes+xml', ODOC + 'footnotes', footnotes('footnotes', 'NOTE WORDS'))]),
    # a branch taken
    ('c79-70-bare-body-in-branch', root_ac(f'<w:body>{p("CHOICE WORDS")}{SECT}</w:body>', inner_document(p('FALLBACK WORDS'))), []),
    ('c79-71-empty-branch-taken', root_ac('', inner_document(p('FALLBACK WORDS'))), []),
]

def with_namespaces(xml, name):
    """A part's own root takes the declarations a branch doesn't carry; a root_ac part has them on the element."""
    if xml.startswith('<mc:AlternateContent'): return xml
    first = xml.index('>')
    head = xml[:first]
    if 'xmlns:w=' not in head:
        space = head.index(' ') if ' ' in head else len(head)
        return head[:space] + f' {XW}' + head[space:] + xml[first:]
    return xml

def entry(z, name, text):
    i = zipfile.ZipInfo(name, (2026, 11, 7, 0, 0, 0))
    i.compress_type = zipfile.ZIP_STORED
    z.writestr(i, text.encode('utf-8'))

def build(path, xml, parts):
    ct = ('<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
          '<Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="' + CTW + 'document.main+xml"/>'
          + ''.join(f'<Override PartName="/{n}" ContentType="{c}"/>' for n, c, r, x in parts) + '</Types>')
    rels = ('<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="' + ODOC + 'officeDocument" Target="word/document.xml"/></Relationships>')
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_STORED) as z:
        entry(z, '[Content_Types].xml', DECL + ct)
        entry(z, '_rels/.rels', DECL + rels)
        entry(z, 'word/document.xml', DECL + xml)
        if parts:
            dr = ''.join(f'<Relationship Id="rId{9 + i}" Type="{r}" Target="{n[5:]}"/>' for i, (n, c, r, x) in enumerate(parts))
            entry(z, 'word/_rels/document.xml.rels', DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' + dr + '</Relationships>')
            for n, c, r, x in parts:
                entry(z, n, DECL + with_namespaces(x, n))

def main(staging, setname):
    sums, order = [], []
    for name, xml, parts in FILES:
        for copy in 'ab':
            job = f'{name}-{copy}'
            d = os.path.join(staging, setname, job)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            build(f, xml, parts)
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
