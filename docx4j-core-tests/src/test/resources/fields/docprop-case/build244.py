# Probe for docx4j #244: does Word read DOCPROPERTY's property name case-insensitively?
# Builds docprop-case.docx: custom property Client = "Acme Ltd", title "The Title"; each marker's field result is OLD.
import zipfile
W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"'
def p(marker, instr):
    big = '<w:rPr><w:sz w:val="32"/></w:rPr>'
    return ('<w:p><w:r><w:t xml:space="preserve">%s %s: </w:t></w:r>' % (marker, instr.replace('&', '&amp;').replace('<', '&lt;'))
            + '<w:r>%s<w:fldChar w:fldCharType="begin"/></w:r><w:r>%s<w:instrText xml:space="preserve"> %s </w:instrText></w:r>' % (big, big, instr)
            + '<w:r>%s<w:fldChar w:fldCharType="separate"/></w:r><w:r>%s<w:t>OLD</w:t></w:r><w:r>%s<w:fldChar w:fldCharType="end"/></w:r>' % (big, big, big)
            + '<w:r><w:t xml:space="preserve"> (end)</w:t></w:r></w:p>')
cases = [('K1', 'DOCPROPERTY Client'), ('K2', 'DOCPROPERTY client'), ('K3', 'DOCPROPERTY CLIENT'), ('K4', 'DOCPROPERTY "cLiEnT"'),
         ('K5', 'DOCPROPERTY Title'), ('K6', 'DOCPROPERTY title'), ('K7', 'DOCPROPERTY Nope')]
body = '<w:p><w:r><w:t>docx4j #244: custom property Client = "Acme Ltd", title "The Title"; every result starts as OLD. Select all, F9, then PDFand.</w:t></w:r></w:p>'
body += ''.join(p(m, i) for m, i in cases)
doc = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:document %s><w:body>%s<w:sectPr/></w:body></w:document>' % (W, body)
ct = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
      '<Override PartName="/docProps/custom.xml" ContentType="application/vnd.openxmlformats-officedocument.custom-properties+xml"/>'
      '<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/></Types>')
rels = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>'
        '<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties" Target="docProps/custom.xml"/>'
        '<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/></Relationships>')
custom = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" '
          'xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"><property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Client">'
          '<vt:lpwstr>Acme Ltd</vt:lpwstr></property></Properties>')
core = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" '
        'xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>The Title</dc:title></cp:coreProperties>')
with zipfile.ZipFile('docprop-case.docx', 'w', zipfile.ZIP_DEFLATED) as z:
    z.writestr('[Content_Types].xml', ct); z.writestr('_rels/.rels', rels); z.writestr('word/document.xml', doc)
    z.writestr('docProps/custom.xml', custom); z.writestr('docProps/core.xml', core)
