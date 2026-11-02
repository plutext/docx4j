# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""The br-placement probe: one small docx per place a w:br is found outside a w:r in the
Common Crawl corpus (docx4j load survey, 2 November 2026: 206,351 documents), so that a
repair in one file cannot hide the others.  Each case reads "BRn before" and "BRn after"
either side of the w:br; Word's re-save shows whether, and where, it kept the break.

Run from this directory: python3 build_br_placement.py .  (README.md has the results.)"""
import sys, os, zipfile

W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"'
CT = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
      '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
      '<Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
      '{footer}</Types>')
FOOTER_CT = '<Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/>'
RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>'
        '</Relationships>')
DOC_RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rIdF1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/>'
            '</Relationships>')

def p(text):
    return '<w:p><w:r><w:t xml:space="preserve">%s</w:t></w:r></w:p>' % text

def r(text):
    return '<w:r><w:t xml:space="preserve">%s</w:t></w:r>' % text

def sect(footer=False):
    ref = '<w:footerReference w:type="default" r:id="rIdF1"/>' if footer else ''
    return ('<w:sectPr>%s<w:pgSz w:w="11906" w:h="16838"/>'
            '<w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>'
            '</w:sectPr>' % ref)

# (file, what it reproduces, body, footer or None)
CASES = [
    ('br-1-p-alone', 'a paragraph that is only a w:br, the web generator\'s blank line (body/p/br, 195,642 documents)',
     p('BR1 line one') + '<w:p><w:br/></w:p>' + p('BR1 line three'), None),
    ('br-2-p-between-runs', 'a w:br between two runs, directly in the w:p (body/p/br)',
     '<w:p>' + r('BR2 before') + '<w:br/>' + r('BR2 after') + '</w:p>', None),
    ('br-3-t-mixed', 'a w:br inside a w:t, between its text (p/r/t/br, PHPWord, 7,398 documents with case 4)',
     '<w:p><w:r><w:t xml:space="preserve">BR3 before<w:br/>BR3 after</w:t></w:r></w:p>', None),
    ('br-4-t-nested', 'w:t elements nested in a w:t, a w:br between them (p/r/t/br, a template filler)',
     '<w:p><w:r><w:t><w:t>BR4 before</w:t><w:br/><w:t>BR4 after</w:t></w:t></w:r></w:p>', None),
    ('br-5-hyperlink', 'a w:br directly in a w:hyperlink, between its runs (p/hyperlink/br, 603 documents)',
     '<w:p><w:hyperlink w:anchor="_top">' + r('BR5 before') + '<w:br/>' + r('BR5 after') + '</w:hyperlink></w:p>', None),
    ('br-6-cell', 'case 2 in a table cell (tbl/tr/tc/p/br, 2,272 documents)',
     '<w:tbl><w:tblPr><w:tblW w:w="4000" w:type="dxa"/></w:tblPr><w:tblGrid><w:gridCol w:w="4000"/></w:tblGrid>'
     '<w:tr><w:tc><w:tcPr><w:tcW w:w="4000" w:type="dxa"/></w:tcPr>'
     '<w:p>' + r('BR6 before') + '<w:br/>' + r('BR6 after') + '</w:p></w:tc></w:tr></w:tbl>' + p('BR6 below the table'), None),
    ('br-7-footer', 'case 2 in a footer (ftr/p/br, 332 documents)',
     p('BR7 body text; the case is in the footer'),
     '<w:p>' + r('BR7 before') + '<w:br/>' + r('BR7 after') + '</w:p>'),
    ('br-8-p-page', 'a page break directly in the w:p after a bookmark (the shape a LibreOffice-labelled document carries)',
     '<w:p>' + r('BR8 before') + '<w:bookmarkStart w:id="1" w:name="br8"/><w:bookmarkEnd w:id="1"/><w:br w:type="page"/></w:p>'
     + p('BR8 after'), None),
]

def put(z, name, text):
    # a fixed date, so that a rebuild is byte-identical
    info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    z.writestr(info, text)

def build(out):
    os.makedirs(out, exist_ok=True)
    for name, what, body, footer in CASES:
        doc = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:document %s><w:body>%s%s</w:body></w:document>'
               % (W, body, sect(footer is not None)))
        with zipfile.ZipFile(os.path.join(out, name + '.docx'), 'w', zipfile.ZIP_DEFLATED) as z:
            put(z, '[Content_Types].xml', CT.format(footer=FOOTER_CT if footer else ''))
            put(z, '_rels/.rels', RELS)
            put(z, 'word/document.xml', doc)
            if footer:
                put(z, 'word/_rels/document.xml.rels', DOC_RELS)
                put(z, 'word/footer1.xml', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:ftr %s>%s</w:ftr>' % (W, footer))
        print(name + '.docx: ' + what)

if __name__ == '__main__':
    build(sys.argv[1] if len(sys.argv) > 1 else '.')
