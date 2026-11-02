# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""The br-placement probe, round 2 (CR-073 phase 0): which run properties Word gives a w:br found outside a w:r
(D1), and how it reads one in a w:instrText or directly in the w:body (D2).  Round 1's files are
build_br_placement.py's.  Run from this directory: python3 build_br_placement_2.py .  (a rebuild is
byte-identical; README.md has the results)."""
import sys, os, zipfile

W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"'
XML = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
CT = (XML + '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
      '<Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
      '{header}</Types>')
HEADER_CT = '<Override PartName="/word/header1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml"/>'
RELS = (XML + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>'
        '</Relationships>')
DOC_RELS = (XML + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rIdH1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header" Target="header1.xml"/>'
            '</Relationships>')

# single spacing, nothing before or after, so that each line's height is its tallest character's
SPACING = '<w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/>'
BEFORE = '<w:rPr><w:b/><w:sz w:val="40"/></w:rPr>'   # 20pt bold: the run before the w:br
AFTER = '<w:rPr><w:i/><w:sz w:val="16"/></w:rPr>'    # 8pt italic: the run after it
MARK = '<w:rPr><w:sz w:val="28"/></w:rPr>'           # 14pt: the paragraph mark

def p(body, mark=False):
    return '<w:p><w:pPr>%s%s</w:pPr>%s</w:p>' % (SPACING, MARK if mark else '', body)

def r(rpr, text):
    return '<w:r>%s<w:t xml:space="preserve">%s</w:t></w:r>' % (rpr, text)

def plain(text):
    return p(r('', text))

def sect(header=False):
    ref = '<w:headerReference w:type="default" r:id="rIdH1"/>' if header else ''
    return ('<w:sectPr>%s<w:pgSz w:w="11906" w:h="16838"/>'
            '<w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>'
            '</w:sectPr>' % ref)

FIELD = ('<w:r><w:fldChar w:fldCharType="begin"/></w:r>'
         '<w:r><w:instrText xml:space="preserve"> QUOTE "I1 code before<w:br/>I1 code after" </w:instrText></w:r>'
         '<w:r><w:fldChar w:fldCharType="separate"/></w:r>' + r('', 'I1 result') +
         '<w:r><w:fldChar w:fldCharType="end"/></w:r>')

# (file, decision, what it asks, body, header or None)
CASES = [
    ('br2-1-run-properties', 'D1',
     'the run properties of a w:br between, before or after runs that differ (20pt bold before, 8pt italic after, a 14pt mark)',
     plain('R1 to R8: the w:br between a 20pt bold run and an 8pt italic run, the mark 14pt where stated')
     + p(r(BEFORE, 'R1 before') + '<w:br/>' + r(AFTER, 'R1 after'))
     + p(r(BEFORE, 'R2 before') + '<w:br/><w:br/>' + r(AFTER, 'R2 after'))
     + p('<w:br/>' + r(AFTER, 'R3 after'), mark=True)
     + p(r(BEFORE, 'R4 before') + '<w:br/>', mark=True)
     + plain('R5 below: a paragraph that is only a w:br, its mark 14pt')
     + p('<w:br/>', mark=True)
     + plain('R5 above')
     + p('<w:r>' + BEFORE + '<w:t xml:space="preserve">R6 before<w:br/>R6 after</w:t></w:r>')
     + p(r(BEFORE, 'R7 before') + '<w:br/><w:hyperlink w:anchor="_top">' + r(AFTER, 'R7 after') + '</w:hyperlink>')
     + p('<w:hyperlink w:anchor="_top">' + r(BEFORE, 'R8 before') + '<w:br/>' + r(AFTER, 'R8 after') + '</w:hyperlink>')
     + plain('R end'), None),
    ('br2-2-instrtext-field', 'D2',
     'a w:br in the w:instrText of a QUOTE field (its result stored, not updated)',
     p(r('', 'I1 start ') + FIELD + r('', ' I1 end')), None),
    ('br2-3-instrtext-bare-header', 'D2',
     'a w:instrText with w:br in it and no field around it, in a header (the shape of the corpus samples, 29 documents)',
     plain('I2 body text; the case is in the header'),
     p('<w:r><w:instrText xml:space="preserve"> <w:br/> I2 line one <w:br/> I2 line two </w:instrText></w:r>')),
    ('br2-4-body-br', 'D2',
     'a w:br directly in the w:body, between two paragraphs (10 documents)',
     plain('B1 before') + '<w:br/>' + plain('B1 after'), None),
    ('br2-5-body-br-page', 'D2',
     'a w:br w:type="page" directly in the w:body, between two paragraphs',
     plain('B2 before') + '<w:br w:type="page"/>' + plain('B2 after'), None),
]

def put(z, name, text):
    # a fixed date, so that a rebuild is byte-identical
    info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    z.writestr(info, text)

def build(out):
    os.makedirs(out, exist_ok=True)
    for name, decision, what, body, header in CASES:
        doc = XML + '<w:document %s><w:body>%s%s</w:body></w:document>' % (W, body, sect(header is not None))
        with zipfile.ZipFile(os.path.join(out, name + '.docx'), 'w', zipfile.ZIP_DEFLATED) as z:
            put(z, '[Content_Types].xml', CT.format(header=HEADER_CT if header else ''))
            put(z, '_rels/.rels', RELS)
            put(z, 'word/document.xml', doc)
            if header:
                put(z, 'word/_rels/document.xml.rels', DOC_RELS)
                put(z, 'word/header1.xml', XML + '<w:hdr %s>%s</w:hdr>' % (W, header))
        print('%s.docx (%s): %s' % (name, decision, what))

if __name__ == '__main__':
    build(sys.argv[1] if len(sys.argv) > 1 else '.')
