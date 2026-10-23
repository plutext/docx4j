"""CR-043 section 17, the unmeasured side: a probe of the three mark rules where text is left, and of an
emptied paragraph before a control. Tracked markup in Word's own form (w:del in the mark's w:rPr, w:del
around the runs), written as XML text with zipfile. Each case is a region BEGINn .. ENDn."""
import sys, zipfile, os
W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" mc:Ignorable="w14"'
XMLDECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
REL = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
CT = 'application/vnd.openxmlformats-officedocument.wordprocessingml'
AUTHOR = 'w:author="Author A" w:date="2026-10-23T00:00:00Z"'
_id = [100]
def nid():
    _id[0] += 1; return _id[0]
def esc(s): return s.replace('&','&amp;').replace('<','&lt;')
def r(s, rpr=''): return '<w:r>%s<w:t xml:space="preserve">%s</w:t></w:r>' % (rpr, esc(s))
def dr(s, rpr=''): return '<w:del w:id="%d" %s><w:r>%s<w:delText xml:space="preserve">%s</w:delText></w:r></w:del>' % (nid(), AUTHOR, rpr, esc(s))
def delmark(): return '<w:rPr><w:del w:id="%d" %s/></w:rPr>' % (nid(), AUTHOR)
def p(content, ppr_inner='', mark_deleted=False):
    ppr = ''
    if ppr_inner or mark_deleted:
        ppr = '<w:pPr>' + ppr_inner + (delmark() if mark_deleted else '') + '</w:pPr>'
    return '<w:p>' + ppr + content + '</w:p>'
def plain(s): return p(r(s))
def tbl(cells):
    return ('<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders><w:top w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:left w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:right w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:insideH w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:insideV w:val="single" w:sz="4" w:space="0" w:color="auto"/></w:tblBorders></w:tblPr>'
            '<w:tblGrid>' + ''.join('<w:gridCol w:w="3000"/>' for _ in cells) + '</w:tblGrid><w:tr>'
            + ''.join('<w:tc><w:tcPr><w:tcW w:w="3000" w:type="dxa"/></w:tcPr>%s</w:tc>' % plain(c) for c in cells) + '</w:tr></w:tbl>')
def sdt(alias, paras):
    return ('<w:sdt><w:sdtPr><w:alias w:val="%s"/><w:tag w:val="%s"/><w:id w:val="%d"/></w:sdtPr><w:sdtContent>%s</w:sdtContent></w:sdt>'
            % (alias, alias, 54000 + nid(), ''.join(paras)))
LIST = '<w:pStyle w:val="ListParagraph"/><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>'
CENTER = '<w:jc w:val="center"/>'
HEAD = '<w:pStyle w:val="Heading1"/>'

body = [
    plain('CR-043 section 17 probe: the three paragraph-mark rules where text is left, and an emptied paragraph before a control (23 October 2026). Each case: BEGINn, the case, ENDn.'),
    # X1: text left, mark deleted, table next (rule 1 variant)
    plain('BEGIN1 text left before a table: HEAD1A kept, HEAD1B deleted, the mark deleted, a table next'),
    p(r('HEAD1A ') + dr('HEAD1B HEAD1C'), HEAD, True),
    tbl(['T1R1C1', 'T1R1C2']),
    plain('END1'),
    # X2: control's last paragraph, text left, mark deleted (rule 2 variant)
    plain('BEGIN2 a control whose last paragraph keeps text: LIST2A kept, LIST2B deleted, the mark deleted'),
    sdt('Probe control 2', [plain('PLAIN2A in the control'), p(r('LIST2A ') + dr('LIST2B LIST2C'), LIST, True)]),
    plain('END2'),
    # X3: emptied paragraph, mark deleted, block control next (rule 3 variant)
    plain('BEGIN3 an emptied paragraph before a control: HEAD3A HEAD3B deleted, the mark deleted, a control next'),
    p(dr('HEAD3A HEAD3B'), HEAD, True),
    sdt('Probe control 3', [p(r('BODY3A in the control'), CENTER)]),
    plain('END3'),
    # X4: emptied paragraph, mark deleted, at the end of a cell (U1 check)
    plain('BEGIN4 an emptied last paragraph of a table cell: C4A deleted, the mark deleted; the cell keeps a paragraph?'),
    ('<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders><w:top w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:left w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:right w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:insideH w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:insideV w:val="single" w:sz="4" w:space="0" w:color="auto"/></w:tblBorders></w:tblPr>'
     '<w:tblGrid><w:gridCol w:w="6000"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w="6000" w:type="dxa"/></w:tcPr>'
     + plain('C4 first paragraph kept') + p(dr('C4A C4B'), LIST, True) + '</w:tc></w:tr></w:tbl>'),
    plain('END4'),
    # X5: text left, mark deleted, control next whose first paragraph is a list item (rule 3 with numbering)
    plain('BEGIN5 text left before a control whose first paragraph is a numbered item: HEAD5A kept, HEAD5B deleted, the mark deleted'),
    p(r('HEAD5A ') + dr('HEAD5B HEAD5C'), HEAD, True),
    sdt('Probe control 5', [p(r('LIST5A in the control'), LIST), p(r('PLAIN5B in the control'))]),
    plain('END5'),
    plain('END_OF_BODY'),
]
doc = XMLDECL + '<w:document %s><w:body>%s<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/></w:sectPr></w:body></w:document>' % (W, ''.join(body))
styles = XMLDECL + ('<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
    '<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/></w:style>'
    '<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/><w:spacing w:before="240" w:after="0"/><w:outlineLvl w:val="0"/></w:pPr><w:rPr><w:b/><w:sz w:val="32"/></w:rPr></w:style>'
    '<w:style w:type="paragraph" w:styleId="ListParagraph"><w:name w:val="List Paragraph"/><w:basedOn w:val="Normal"/><w:qFormat/><w:pPr><w:ind w:left="720"/><w:contextualSpacing/></w:pPr></w:style>'
    '</w:styles>')
numbering = XMLDECL + ('<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
    '<w:abstractNum w:abstractNumId="0"><w:multiLevelType w:val="hybridMultilevel"/><w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="%1."/><w:lvlJc w:val="left"/><w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr></w:lvl></w:abstractNum>'
    '<w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num></w:numbering>')
settings = XMLDECL + '<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:trackRevisions/></w:settings>'
out = sys.argv[1]
with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as z:
    z.writestr('[Content_Types].xml', XMLDECL + '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/>'
               '<Override PartName="/word/document.xml" ContentType="%s.document.main+xml"/><Override PartName="/word/styles.xml" ContentType="%s.styles+xml"/><Override PartName="/word/numbering.xml" ContentType="%s.numbering+xml"/><Override PartName="/word/settings.xml" ContentType="%s.settings+xml"/></Types>' % (CT, CT, CT, CT))
    z.writestr('_rels/.rels', XMLDECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="%s/officeDocument" Target="word/document.xml"/></Relationships>' % REL)
    z.writestr('word/document.xml', doc)
    z.writestr('word/_rels/document.xml.rels', XMLDECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="%s/styles" Target="styles.xml"/><Relationship Id="rId2" Type="%s/numbering" Target="numbering.xml"/><Relationship Id="rId3" Type="%s/settings" Target="settings.xml"/></Relationships>' % (REL, REL, REL))
    z.writestr('word/styles.xml', styles)
    z.writestr('word/numbering.xml', numbering)
    z.writestr('word/settings.xml', settings)
print('wrote', out)
