# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Builds the part.not_read fixture (WD 0.66 REQ-801): one document, and the same with its styles, numbering or
settings part not well formed (cut to two thirds of its characters) or absent (part, relationship and override
removed), and a numbering part not well formed in a document that numbers no paragraph.

Run from this folder or give it the output folder: python3 -I build_part_not_read.py [dir]. The zips are stored,
not deflated, with fixed dates, so a rebuild is byte-identical."""
import os
import re
import sys
import zipfile

W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"'
R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
CT = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
      '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
      '<Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
      '<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>'
      '<Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>'
      '<Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>'
      '</Types>')
RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
        '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="%s/officeDocument" Target="word/document.xml"/></Relationships>' % R)
DOC_RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="%s/styles" Target="styles.xml"/>'
            '<Relationship Id="rId2" Type="%s/numbering" Target="numbering.xml"/>'
            '<Relationship Id="rId3" Type="%s/settings" Target="settings.xml"/></Relationships>' % (R, R, R))


def p(body, ppr=''):
    return '<w:p>%s%s</w:p>' % ('<w:pPr>%s</w:pPr>' % ppr if ppr else '', body)


def r(t, rpr=''):
    return '<w:r>%s<w:t xml:space="preserve">%s</w:t></w:r>' % ('<w:rPr>%s</w:rPr>' % rpr if rpr else '', t)


NUMBERED = '<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>'
PARAS = [
    p(r('Definitions'), '<w:pStyle w:val="Heading1"/>'),               # numbered by its style: "Article I"
    p(r('Interpretation'), '<w:pStyle w:val="Heading1"/>'),
    p(r('first item'), NUMBERED),                                      # numbered directly: "1."
    p(r('second item'), NUMBERED),
    p(r('visible ') + r('hidden-by-style ', '<w:rStyle w:val="Hid"/>') + r('end')),
    p(r('istanbul caps-by-style', '<w:rStyle w:val="Caps"/>')),       # no w:lang: the settings' themeFontLang tr-TR
    p(r('small caps by style'), '<w:pStyle w:val="SmallCapsPara"/>'),
    p(r('istanbul direct caps', '<w:caps/>')),
    p(r('direct ') + r('hidden-direct ', '<w:vanish/>') + r('shown')),
    p(r('kept ') + '<w:ins w:id="1" w:author="A" w:date="2026-11-03T00:00:00Z">' + r('inserted ') + '</w:ins>'
      + '<w:del w:id="2" w:author="A" w:date="2026-11-03T00:00:00Z"><w:r><w:delText xml:space="preserve">deleted </w:delText>'
      + '</w:r></w:del>' + r('end')),
]
UNNUMBERED = [p(r('plain one')), p(r('plain two'))]
SECT = '<w:sectPr><w:pgSz w:w="11906" w:h="16838"/></w:sectPr>'


def document(paras):
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<w:document %s><w:body>%s%s</w:body></w:document>'
            % (W, ''.join(paras), SECT))


STYLES = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<w:styles %s>'
          '<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val="22"/></w:rPr></w:rPrDefault><w:pPrDefault/></w:docDefaults>'
          '<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style>'
          '<w:style w:type="character" w:default="1" w:styleId="DefaultParagraphFont"><w:name w:val="Default Paragraph Font"/></w:style>'
          '<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/>'
          '<w:pPr><w:numPr><w:numId w:val="2"/></w:numPr><w:outlineLvl w:val="0"/></w:pPr></w:style>'
          '<w:style w:type="character" w:styleId="Hid"><w:name w:val="Hid"/><w:rPr><w:vanish/></w:rPr></w:style>'
          '<w:style w:type="character" w:styleId="Caps"><w:name w:val="Caps"/><w:rPr><w:caps/></w:rPr></w:style>'
          '<w:style w:type="paragraph" w:styleId="SmallCapsPara"><w:name w:val="SmallCapsPara"/><w:basedOn w:val="Normal"/>'
          '<w:rPr><w:smallCaps/></w:rPr></w:style></w:styles>' % W)
NUMBERING = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<w:numbering %s>'
             '<w:abstractNum w:abstractNumId="0"><w:multiLevelType w:val="singleLevel"/>'
             '<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="%%1."/></w:lvl></w:abstractNum>'
             '<w:abstractNum w:abstractNumId="1"><w:multiLevelType w:val="singleLevel"/>'
             '<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="upperRoman"/><w:pStyle w:val="Heading1"/>'
             '<w:lvlText w:val="Article %%1"/></w:lvl></w:abstractNum>'
             '<w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num><w:num w:numId="2"><w:abstractNumId w:val="1"/></w:num>'
             '</w:numbering>' % W)
SETTINGS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<w:settings %s>'
            '<w:defaultTabStop w:val="720"/><w:themeFontLang w:val="tr-TR"/>'
            '<w:compat><w:compatSetting w:name="compatibilityMode" w:uri="http://schemas.microsoft.com/office/word" w:val="15"/>'
            '</w:compat></w:settings>' % W)
PARTS = [('styles', STYLES), ('numbering', NUMBERING), ('settings', SETTINGS)]


def cut(xml):
    """Not well formed: the root's content cut short, its end tags lost."""
    return xml[:len(xml) * 2 // 3]


def write(path, entries):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_STORED) as z:
        for name, text in entries:
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.external_attr = 0o644 << 16
            z.writestr(info, text.encode('utf-8'))


def package(paras, broken=None, absent=None):
    ct, rels = CT, DOC_RELS
    if absent:
        ct = re.sub(r'<Override PartName="/word/%s.xml"[^>]*/>' % absent, '', ct)
        rels = re.sub(r'<Relationship Id="rId\d" Type="[^"]*" Target="%s.xml"/>' % absent, '', rels)
    entries = [('[Content_Types].xml', ct), ('_rels/.rels', RELS), ('word/_rels/document.xml.rels', rels),
               ('word/document.xml', document(paras))]
    for name, xml in PARTS:
        if name != absent:
            entries.append(('word/%s.xml' % name, cut(xml) if name == broken else xml))
    return entries


FIXTURES = [('part-not-read-intact.docx', PARAS, None, None)]
FIXTURES += [('part-not-read-%s.docx' % n, PARAS, n, None) for n, _ in PARTS]
FIXTURES += [('part-not-read-%s-absent.docx' % n, PARAS, None, n) for n, _ in PARTS]
FIXTURES += [('part-not-read-numbering-unneeded.docx', UNNUMBERED, 'numbering', None)]

if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.abspath(__file__))
    for name, paras, broken, absent in FIXTURES:
        write(os.path.join(out, name), package(paras, broken, absent))
        print('wrote', name)
