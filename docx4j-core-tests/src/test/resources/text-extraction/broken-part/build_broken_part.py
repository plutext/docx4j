# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Builds the broken-part probe family (docx-tool-identifier CR-001 section 3c; Jason, 3 November 2026: "add the
broken-part family"): one document, and the same with one part broken in one way, so that what a reader does with a
part it cannot read (refuses the document, reads the part as absent, recovers what it can, or never looks at it)
shows in the tokens it returns.  Every effect that depends on a part has a token whose count or spelling changes with
that part; BP_BEGIN and BP_END depend on no part.  Also store-not-read and its intact twin: two custom XML stores
whose roots fit the same XPath, the first not well formed in store-not-read.docx.

It also writes expected-tokens.tsv, the tokens as Word 365 read each file (WORD_READINGS, from Jason's runs of 3 and 4
November 2026), expected-findings.tsv (docx4j's part.not_read) and an empty goldens.tsv, for ExtractionConformance.

Run from this folder or give it the output folder: python3 -I build_broken_part.py [dir].  The zips are stored, not
deflated, with fixed dates and no docx4j comment, so a rebuild is byte-identical."""
import os
import sys
import zipfile

W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"'
W14 = 'xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"'
W15 = 'xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"'
MC = 'xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"'
R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
OD = 'application/vnd.openxmlformats-officedocument.'
WML = OD + 'wordprocessingml.'
DECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'


def r(t, rpr=''):
    return '<w:r>%s<w:t xml:space="preserve">%s</w:t></w:r>' % ('<w:rPr>%s</w:rPr>' % rpr if rpr else '', t)


def p(body, ppr=''):
    return '<w:p>%s%s</w:p>' % ('<w:pPr>%s</w:pPr>' % ppr if ppr else '', body)


def dirty(instr, cached):
    """A field Word offers to update on open (w:dirty, as the compact probe's R14)."""
    return '<w:fldSimple w:instr=" %s " w:dirty="true">%s</w:fldSimple>' % (instr, r(cached))


REV = 'w:author="Author A" w:date="2026-11-03T00:00:00Z"'
NUMBERED = '<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>'
BEGIN = [p(r('BP_BEGIN'))]
CAPS_LANG = [p(r('theme language caps: ') + r('istanbul_theme', '<w:caps/>')),            # no w:lang; themeFontLang tr-TR no link
             p(r('direct language caps: ') + r('istanbul_lang', '<w:caps/><w:lang w:val="tr-TR"/>'))]
LISTS = [p(r('first heading'), '<w:pStyle w:val="Heading1"/>'),                          # numbered by its style
         p(r('second heading'), '<w:pStyle w:val="Heading1"/>'),
         p(r('first item'), NUMBERED),                                                     # numbered directly
         p(r('second item'), NUMBERED)]
# (g) the lists interleaved: does a numbering part Word cannot read count per w:numId, or as one counter that restarts?
LISTS_ORDER = [p(r('heading one'), '<w:pStyle w:val="Heading1"/>'), p(r('item one'), NUMBERED), p(r('item two'), NUMBERED),
               p(r('heading two'), '<w:pStyle w:val="Heading1"/>'), p(r('item three'), NUMBERED)]
STYLED = [p(r('visible ') + r('HIDDEN_BY_STYLE', '<w:rStyle w:val="Hid"/>') + r(' shown')),
          p(r('caps_by_style', '<w:rStyle w:val="Caps"/>')),
          p(r('small_caps_by_style'), '<w:pStyle w:val="SmallCapsPara"/>')]
DIRECT = [p(r('direct ') + r('HIDDEN_DIRECT', '<w:vanish/>') + r(' shown')),
          p(r('kept ') + '<w:ins w:id="11" %s>' % REV + r('INSERTED_RUN ') + '</w:ins>'
            + '<w:del w:id="12" %s><w:r><w:delText xml:space="preserve">DELETED_RUN </w:delText></w:r></w:del>' % REV
            + r('end'))]
FIELDS = [p(r('title: ') + dirty('TITLE', 'TITLE_FIELD_STALE')),
          p(r('company: ') + dirty('DOCPROPERTY Company', 'COMPANY_FIELD_STALE')),
          p(r('custom: ') + dirty('DOCPROPERTY Custom1', 'CUSTOM_FIELD_STALE'))]
COMMENTED = [('<w:p><w:commentRangeStart w:id="0"/><w:commentRangeStart w:id="1"/>' + r('commented text')
              + '<w:commentRangeEnd w:id="0"/><w:commentRangeEnd w:id="1"/>'
              + '<w:r><w:commentReference w:id="0"/></w:r><w:r><w:commentReference w:id="1"/></w:r></w:p>')]
END = [p(r('BP_END'))]
BODY = BEGIN + CAPS_LANG + LISTS + STYLED + DIRECT + FIELDS + COMMENTED
ENTITY = p(r('entity: ') + '<w:r><w:t>&bp;</w:t></w:r>')
ENCODING = p(r('encoding: ') + r('ENC_CAFÉ_X'))
TRUNC_A, TRUNC_B = p(r('TRUNC_A')), p(r('TRUNC_B'))
SECT = ('<w:sectPr><w:pgSz w:w="11906" w:h="16838"/>'
        '<w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>'
        '</w:sectPr>')


def document(paras, doctype=''):
    return DECL + doctype + '<w:document %s><w:body>%s%s</w:body></w:document>' % (W, ''.join(paras), SECT)


STYLES_EARLY = (DECL + '<w:styles %s>' % W
                + '<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val="22"/></w:rPr></w:rPrDefault><w:pPrDefault/></w:docDefaults>'
                '<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style>'
                '<w:style w:type="character" w:default="1" w:styleId="DefaultParagraphFont"><w:name w:val="Default Paragraph Font"/></w:style>'
                '<w:style w:type="character" w:styleId="Hid"><w:name w:val="Hid"/><w:rPr><w:vanish/></w:rPr></w:style>'
                '<w:style w:type="character" w:styleId="Caps"><w:name w:val="Caps"/><w:rPr><w:caps/></w:rPr></w:style>')
STYLES_LATE = ('<w:style w:type="paragraph" w:styleId="SmallCapsPara"><w:name w:val="SmallCapsPara"/><w:basedOn w:val="Normal"/>'
               '<w:rPr><w:smallCaps/></w:rPr></w:style>'
               '<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/>'
               '<w:pPr><w:numPr><w:numId w:val="2"/></w:numPr><w:outlineLvl w:val="0"/></w:pPr></w:style>')
STYLES = STYLES_EARLY + STYLES_LATE + '</w:styles>'
NUMBERING = (DECL + '<w:numbering %s>' % W
             + '<w:abstractNum w:abstractNumId="0"><w:multiLevelType w:val="singleLevel"/>'
             '<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:lvlText w:val="DIRECT_LIST_%1."/></w:lvl></w:abstractNum>'
             '<w:abstractNum w:abstractNumId="1"><w:multiLevelType w:val="singleLevel"/>'
             '<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="decimal"/><w:pStyle w:val="Heading1"/>'
             '<w:lvlText w:val="STYLE_LIST_%1"/></w:lvl></w:abstractNum>'
             '<w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num><w:num w:numId="2"><w:abstractNumId w:val="1"/></w:num>'
             '</w:numbering>')
SETTINGS = (DECL + '<w:settings %s>' % W
            + '<w:defaultTabStop w:val="720"/>'
            '<w:compat><w:compatSetting w:name="compatibilityMode" w:uri="http://schemas.microsoft.com/office/word" w:val="15"/></w:compat>'
            '<w:themeFontLang w:val="tr-TR"/></w:settings>')
COMMENTS = (DECL + '<w:comments %s %s %s mc:Ignorable="w14">' % (W, W14, MC)
            + '<w:comment w:id="0" w:author="Author A" w:initials="AA" w:date="2026-11-03T00:00:00Z">'
            '<w:p w14:paraId="1A2B3C4D" w14:textId="77777777">' + r('COMMENT_TEXT_A') + '</w:p></w:comment>'
            '<w:comment w:id="1" w:author="Author B" w:initials="AB" w:date="2026-11-03T00:00:00Z">'
            '<w:p w14:paraId="2B3C4D5E" w14:textId="77777777">' + r('COMMENT_REPLY_B') + '</w:p></w:comment></w:comments>')
COMMENTS_EXT = (DECL + '<w15:commentsEx %s>' % W15
                + '<w15:commentEx w15:paraId="1A2B3C4D" w15:done="1"/>'
                '<w15:commentEx w15:paraId="2B3C4D5E" w15:paraIdParent="1A2B3C4D" w15:done="0"/></w15:commentsEx>')
CORE = (DECL + '<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" '
        'xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" '
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
        '<dc:title>META_CORE_TITLE</dc:title><dc:creator>probe author</dc:creator></cp:coreProperties>')
APP = (DECL + '<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">'
       '<Application>probe builder</Application><Company>META_APP_COMPANY</Company></Properties>')
CUSTOM = (DECL + '<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" '
          'xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">'
          '<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Custom1">'
          '<vt:lpwstr>META_CUSTOM_VALUE</vt:lpwstr></property></Properties>')

# (key, path, content type, relationship owner, relationship type, xml); each keeps its relationship id
PARTS = [
    ('document', 'word/document.xml', WML + 'document.main+xml', 'package', R + '/officeDocument', None),
    ('core', 'docProps/core.xml', 'application/vnd.openxmlformats-package.core-properties+xml', 'package',
     'http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties', CORE),
    ('app', 'docProps/app.xml', OD + 'extended-properties+xml', 'package', R + '/extended-properties', APP),
    ('custom', 'docProps/custom.xml', OD + 'custom-properties+xml', 'package', R + '/custom-properties', CUSTOM),
    ('styles', 'word/styles.xml', WML + 'styles+xml', 'document', R + '/styles', STYLES),
    ('numbering', 'word/numbering.xml', WML + 'numbering+xml', 'document', R + '/numbering', NUMBERING),
    ('settings', 'word/settings.xml', WML + 'settings+xml', 'document', R + '/settings', SETTINGS),
    ('comments', 'word/comments.xml', WML + 'comments+xml', 'document', R + '/comments', COMMENTS),
    ('commentsExtended', 'word/commentsExtended.xml', WML + 'commentsExtended+xml', 'document',
     'http://schemas.microsoft.com/office/2011/relationships/commentsExtended', COMMENTS_EXT),
]


def cut_early(xml):
    """Not well formed early: the root's start tag and the first few characters of its first child, so that a parser
    that recovers what it can recovers an empty root."""
    start = xml.index('>', xml.index('?>') + 2) + 1
    return xml[:start + 6]


def styles_late():
    """(a) Not well formed late: cut after the hidden and caps character styles, inside the next definition, so that
    a parser that recovers what it can keeps those two and loses the small caps and the list-linked styles."""
    return STYLES_EARLY + '<w:style w:type="paragraph" w:st'


def styles_invalid():
    """(b) Well formed, but the root is in a foreign namespace; its content is the styles part's."""
    foreign = 'xmlns:x="urn:broken-part:not-wordprocessingml"'
    return STYLES.replace('<w:styles %s>' % W, '<x:styles %s %s>' % (foreign, W)).replace('</w:styles>', '</x:styles>')


def relationships(owner, absent):
    rels = ['<Relationship Id="rId%d" Type="%s" Target="%s"/>'
            % (i + 1, t, path if owner == 'package' else path[len('word/'):])
            for i, (key, path, ct, o, t, xml) in enumerate(PARTS) if o == owner and key != absent]
    return (DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            + ''.join(rels) + '</Relationships>')


def content_types(absent):
    overrides = ['<Override PartName="/%s" ContentType="%s"/>' % (path, ct)
                 for key, path, ct, o, t, xml in PARTS if key != absent]
    return (DECL + '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
            '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
            '<Default Extension="xml" ContentType="application/xml"/>' + ''.join(overrides) + '</Types>')


def package(doc=None, replace=None, absent=None):
    """The intact package, with the main part's bytes `doc` (default: the intact body), part `replace` = (key, text)
    replaced, or part `absent` removed with its relationship and override."""
    if doc is None:
        doc = document(BODY + END).encode('utf-8')
    entries = [('[Content_Types].xml', content_types(absent)), ('_rels/.rels', relationships('package', absent)),
               ('word/_rels/document.xml.rels', relationships('document', absent)), ('word/document.xml', doc)]
    for key, path, ct, o, t, xml in PARTS:
        if key == 'document' or key == absent:
            continue
        entries.append((path, replace[1] if replace and replace[0] == key else xml))
    return entries


def rels_cut(xml, n):
    """A relationships part cut after its nth relationship, inside the Relationships element: a parser that recovers
    what it can keeps those n."""
    end = 0
    for i in range(n):
        end = xml.index('/>', end) + 2
    return xml[:end]


def with_entry(entries, name, data):
    return [(e, data if e == name else d) for e, d in entries]


def truncated():
    """(f) The main part cut after TRUNC_A's paragraph, before TRUNC_B's."""
    xml = document(BODY + [TRUNC_A, TRUNC_B] + END)
    return xml[:xml.index(TRUNC_A) + len(TRUNC_A)].encode('utf-8')


# The store probe (docx4j's WD 0.67 question (c), measured in Word 365 on 3 November 2026): two stores whose roots fit
# /ns0:root[1]/ns0:name[1]; control A names store 1, B store 2, C no store.
ID1 = '{5D0E1A11-1111-4111-8111-111111111111}'
ID2 = '{5D0E1A22-2222-4222-8222-222222222222}'
ID_NONE = '{5D0E1A99-9999-4999-8999-999999999999}'


def store_xml(value):
    return ('<?xml version="1.0" encoding="UTF-8" standalone="no"?>'
            '<root xmlns="urn:probe"><name>%s</name><other>%s other</other></root>' % (value, value))


STORE1 = store_xml('STORE1_VALUE')
STORE1_CUT = STORE1[:STORE1.index('<other>') + len('<other>STORE1')]   # cut after </name>: <other> and <root> left open


def store_package(item1):
    def item_rels(n):
        return (DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
                '<Relationship Id="rId1" Type="%s/customXmlProps" Target="itemProps%d.xml"/></Relationships>' % (R, n))

    def item_props(i):
        return ('<?xml version="1.0" encoding="UTF-8" standalone="no"?>'
                '<ds:datastoreItem ds:itemID="%s" xmlns:ds="http://schemas.openxmlformats.org/officeDocument/2006/customXml">'
                '<ds:schemaRefs/></ds:datastoreItem>' % i)

    def bound(label, store_id, surface):
        return ('<w:p>' + r(label) + '<w:sdt><w:sdtPr><w:alias w:val="%s"/>'
                '<w:dataBinding w:prefixMappings="xmlns:ns0=\'urn:probe\'" w:xpath="/ns0:root[1]/ns0:name[1]" w:storeItemID="%s"/>'
                '<w:text/></w:sdtPr><w:sdtContent>' % (label.split()[0], store_id) + r(surface) + '</w:sdtContent></w:sdt></w:p>')

    body = (p(r('SNR_BEGIN')) + bound('A (names store 1): ', ID1, 'A_STALE_SURFACE')
            + bound('B (names store 2): ', ID2, 'B_STALE_SURFACE') + bound('C (names no store): ', ID_NONE, 'C_STALE_SURFACE')
            + p(r('SNR_END')))
    ct = (DECL + '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
          '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
          '<Default Extension="xml" ContentType="application/xml"/>'
          '<Override PartName="/word/document.xml" ContentType="%sdocument.main+xml"/>'
          '<Override PartName="/customXml/itemProps1.xml" ContentType="%scustomXmlProperties+xml"/>'
          '<Override PartName="/customXml/itemProps2.xml" ContentType="%scustomXmlProperties+xml"/></Types>' % (WML, OD, OD))
    doc_rels = (DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
                '<Relationship Id="rId1" Type="%s/customXml" Target="../customXml/item1.xml"/>'
                '<Relationship Id="rId2" Type="%s/customXml" Target="../customXml/item2.xml"/></Relationships>' % (R, R))
    rels = (DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="%s/officeDocument" Target="word/document.xml"/></Relationships>' % R)
    return [('[Content_Types].xml', ct), ('_rels/.rels', rels),
            ('word/_rels/document.xml.rels', doc_rels),
            ('word/document.xml', DECL + '<w:document %s><w:body>%s%s</w:body></w:document>' % (W, body, SECT)),
            ('customXml/item1.xml', item1), ('customXml/_rels/item1.xml.rels', item_rels(1)),
            ('customXml/itemProps1.xml', item_props(ID1)),
            ('customXml/item2.xml', store_xml('STORE2_VALUE')), ('customXml/_rels/item2.xml.rels', item_rels(2)),
            ('customXml/itemProps2.xml', item_props(ID2))]


def fixtures():
    """(file name, entries) in the order of the README's table."""
    out = [('broken-part-intact.docx', package())]
    for key, xml in (('styles', STYLES), ('numbering', NUMBERING), ('settings', SETTINGS)):
        out.append(('broken-part-%s.docx' % key, package(replace=(key, cut_early(xml)))))
        out.append(('broken-part-%s-absent.docx' % key, package(absent=key)))
    out.append(('broken-part-numbering-unneeded.docx',
                package(doc=document(BEGIN + CAPS_LANG + STYLED + DIRECT + FIELDS + COMMENTED + END).encode('utf-8'),
                        replace=('numbering', cut_early(NUMBERING)))))
    ordered = document(BEGIN + CAPS_LANG + LISTS_ORDER + STYLED + DIRECT + FIELDS + COMMENTED + END).encode('utf-8')
    out.append(('broken-part-numbering-order.docx', package(doc=ordered, replace=('numbering', cut_early(NUMBERING)))))
    out.append(('broken-part-numbering-order-absent.docx', package(doc=ordered, absent='numbering')))
    out.append(('broken-part-styles-late.docx', package(replace=('styles', styles_late()))))
    out.append(('broken-part-styles-invalid.docx', package(replace=('styles', styles_invalid()))))
    out.append(('broken-part-document-entity.docx', package(doc=document(
        BODY + [ENTITY] + END, '<!DOCTYPE w:document [<!ENTITY bp "ENTITY_EXPANDED">]>\n').encode('utf-8'))))
    out.append(('broken-part-document-encoding.docx', package(doc=document(BODY + [ENCODING] + END).encode('cp1252'))))
    out.append(('broken-part-document-truncated.docx', package(doc=truncated())))
    for key, xml in (('core', CORE), ('app', APP), ('custom', CUSTOM), ('commentsExtended', COMMENTS_EXT)):
        out.append(('broken-part-%s.docx' % key, package(replace=(key, cut_early(xml)))))
    # the main part's relationships keep the styles, numbering and settings and lose the comments; the package's
    # keep the main part and lose the three properties parts (docx4j-python CR-031 phase B's hand-off, 3 November 2026)
    out.append(('broken-part-document-rels.docx', with_entry(package(), 'word/_rels/document.xml.rels',
                                                             rels_cut(relationships('document', None), 3))))
    out.append(('broken-part-package-rels.docx', with_entry(package(), '_rels/.rels',
                                                            rels_cut(relationships('package', None), 1))))
    out.append(('store-not-read.docx', store_package(STORE1_CUT)))
    out.append(('store-not-read-intact.docx', store_package(STORE1)))
    return out


def write(path, entries):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_STORED) as z:
        for name, data in entries:
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.external_attr = 0o644 << 16
            z.writestr(info, data if isinstance(data, bytes) else data.encode('utf-8'))


# Word 365's readings (Jason's runs on the VM of 3 and 4 November 2026: each file opened, "update fields?" answered Yes,
# saved as Plain Text in UTF-8; *-word365-utf8.txt), as the token counts of expected-tokens.tsv.  WORD is that save's
# scenario: the word-text preset with the labels Word writes and the fields it updated.  The default columns are the
# WD's default reading of the document as Word read it, after any repair; with_option is Word's own count.
WORD = 'profile: word-text; list_labels: true; fields: update'
FIRST_CASE = 100          # ExtractionConformance's supplemental scenarios key on cases 36 to 63

# The open items and the defect a row waits on where docx4j reads otherwise today (README.md): a label that is not a
# REQ id leaves the row pending.
DEFAULT_LIST = 'WD 0.72 (2)'      # no numbering part: Word's default decimal list, one shared counter (0.73)
STYLES_PREFIX = 'WD 0.72 (3)'     # a styles part cut late: Word keeps the styles before the break
PROPERTIES = 'WD 0.72 (4)'        # a properties part not well formed, under fields: update: read as absent
DEFECT_D = 'defect (d)'           # docx4j: an unreadable custom part leaves every property field on its cached result
DEFECT_E = 'defect (e)'           # docx4j: a property part absent leaves its property fields on their cached results
RELATIONSHIPS = 'WD 0.73 (rels)'  # a relationships part not well formed: Word keeps the relationships before the break
STORE = 'WD 0.69 (c)'             # a custom XML store not well formed: read as absent, the binding repaired

FIELD_TOKENS = [('core', 'TITLE_FIELD_STALE', 'META_CORE_TITLE'), ('app', 'COMPANY_FIELD_STALE', 'META_APP_COMPANY'),
                ('custom', 'CUSTOM_FIELD_STALE', 'META_CUSTOM_VALUE')]
UNKNOWN_PROPERTY = 'Error! Unknown document property name.'


class Reading:
    """What Word read of one file: the styles (all, none, or the prefix before a late cut), the numbering (the file's,
    or Word's default list), whether the comments kept their text, the properties parts it lost, and which lists the
    main part holds."""

    def __init__(self, styles='all', numbering='file', comments=True, lost=(), lists='both'):
        self.styles, self.numbering, self.comments, self.lost, self.lists = styles, numbering, comments, set(lost), lists


def token_rows(reading):
    """(token, accepted, original, markup, with_option under WORD, statements) for every token of the family."""
    rows = [('BP_BEGIN', 1, 1, 1, 1, 'REQ-501'), ('BP_END', 1, 1, 1, 1, 'REQ-501'),
            # Word maps the theme run by its editing language (en-AU), never w:themeFontLang's tr-TR
            ('İSTANBUL_THEME', 0, 0, 0, 0, 'REQ-607'), ('ISTANBUL_THEME', 1, 1, 1, 1, 'REQ-607'),
            ('İSTANBUL_LANG', 1, 1, 1, 1, 'REQ-607')]
    style_list = reading.lists == 'both' and reading.styles == 'all' and reading.numbering == 'file'
    direct_list = reading.lists == 'both' and reading.numbering == 'file'
    default_list = reading.lists == 'both' and reading.numbering == 'default'
    for token, labelled in (('STYLE_LIST_1', style_list), ('STYLE_LIST_2', style_list),
                            # the direct labels with their text: "1. first item" is in "DIRECT_LIST_1. first item"
                            ('DIRECT_LIST_1. first item', direct_list), ('DIRECT_LIST_2. second item', direct_list),
                            ('1. first heading', default_list), ('2. second heading', default_list),
                            ('1. first item', default_list), ('2. second item', default_list)):
        rows.append((token, 0, 0, 0, int(labelled), 'REQ-510'))
    order = reading.lists == 'order'
    for token in ('1. heading one', '1. item one', '2. item two', '3. heading two', '4. item three'):
        rows.append((token, 0, 0, 0, int(order), 'REQ-510'))
    hid = caps = reading.styles in ('all', 'prefix')
    small = reading.styles == 'all'
    rows += [('HIDDEN_BY_STYLE', int(not hid), int(not hid), int(not hid), 1, 'REQ-507'),
             ('CAPS_BY_STYLE', int(caps), int(caps), int(caps), int(caps), 'REQ-607'),
             ('caps_by_style', int(not caps), int(not caps), int(not caps), int(not caps), 'REQ-607'),
             # small_caps: asis by default, upper under word-text
             ('SMALL_CAPS_BY_STYLE', 0, 0, 0, int(small), 'REQ-607'),
             ('small_caps_by_style', 1, 1, 1, int(not small), 'REQ-607'),
             ('HIDDEN_DIRECT', 0, 0, 0, 1, 'REQ-507'),
             ('INSERTED_RUN', 1, 0, 'ins', None, 'REQ-401'), ('DELETED_RUN', 0, 1, 'del', None, 'REQ-401')]
    for part, stale, value in FIELD_TOKENS:
        rows += [(stale, 1, 1, 1, 0, 'REQ-506'), (value, 0, 0, 0, int(part not in reading.lost), 'REQ-506')]
    rows.append((UNKNOWN_PROPERTY, 0, 0, 0, int('custom' in reading.lost), 'REQ-506'))
    for token in ('COMMENT_TEXT_A', 'COMMENT_REPLY_B'):
        rows.append((token, 0, 0, 'cmt' if reading.comments else 0, int(reading.comments), 'REQ-504'))
    return rows


GROUPS = {'main': ['BP_BEGIN', 'BP_END'], 'theme': ['İSTANBUL_THEME', 'ISTANBUL_THEME'], 'lang': ['İSTANBUL_LANG'],
          'lists': ['STYLE_LIST_1', 'STYLE_LIST_2', 'DIRECT_LIST_1. first item', 'DIRECT_LIST_2. second item', '1. first heading',
                    '2. second heading', '1. first item', '2. second item'],
          'order': ['1. heading one', '1. item one', '2. item two', '3. heading two', '4. item three'],
          'styled': ['HIDDEN_BY_STYLE', 'CAPS_BY_STYLE', 'caps_by_style', 'SMALL_CAPS_BY_STYLE', 'small_caps_by_style'],
          'direct': ['HIDDEN_DIRECT', 'INSERTED_RUN', 'DELETED_RUN'],
          'fields': [t for part in FIELD_TOKENS for t in part[1:]] + [UNKNOWN_PROPERTY],
          'comments': ['COMMENT_TEXT_A', 'COMMENT_REPLY_B']}
EVERYTHING = ['main', 'theme', 'lang', 'lists', 'styled', 'direct', 'fields', 'comments']
LIST_LABELS = GROUPS['lists'][4:]
STYLED_DIFFERENCE = ['HIDDEN_BY_STYLE', 'CAPS_BY_STYLE', 'caps_by_style']

# file: (what Word did on opening it, what it read, the token groups its rows hold, {token: label} where docx4j reads
# otherwise today).  The three main-part files have no rows: Word would not open them and docx4j refuses to read them.
WORD_READINGS = {
    # (WD 0.72 (1), ruled 4 November 2026 as WD 0.80: w:themeFontLang is no link in REQ-607's chain; docx4j follows)
    'broken-part-intact.docx': ('no repair', Reading(), EVERYTHING, {}),
    'broken-part-styles.docx': ('repaired (Styles): no styles, as absent', Reading(styles='none'), ['main', 'lists', 'styled'], {}),
    'broken-part-styles-absent.docx': ('no repair', Reading(styles='none'), ['main', 'lists', 'styled'], {}),
    'broken-part-numbering.docx': ('repaired (Lists): its default decimal list in place of the numbering',
                                   Reading(numbering='default'), ['main', 'lists'], dict.fromkeys(LIST_LABELS, DEFAULT_LIST)),
    'broken-part-numbering-absent.docx': ('no repair; its default decimal list', Reading(numbering='default'),
                                          ['main', 'lists'], dict.fromkeys(LIST_LABELS, DEFAULT_LIST)),
    'broken-part-settings.docx': ('repaired (Summary Info): no settings, as absent', Reading(), ['main', 'theme'], {}),
    'broken-part-settings-absent.docx': ('no repair', Reading(), ['main', 'theme'], {}),
    'broken-part-numbering-unneeded.docx': ('repaired (Lists); no paragraph is numbered', Reading(lists='none'),
                                            ['main', 'lists'], {}),
    'broken-part-numbering-order.docx': ('repaired (Lists): its default list, one counter that the items restart',
                                         Reading(numbering='default', lists='order'), ['main', 'order'],
                                         dict.fromkeys(GROUPS['order'], DEFAULT_LIST)),
    'broken-part-numbering-order-absent.docx': ('no repair; its default list, one counter that the items restart',
                                                Reading(numbering='default', lists='order'), ['main', 'order'],
                                                dict.fromkeys(GROUPS['order'], DEFAULT_LIST)),
    'broken-part-styles-late.docx': ('repaired (Styles): the Hid and Caps styles before the break kept',
                                     Reading(styles='prefix'), ['main', 'lists', 'styled'],
                                     dict.fromkeys(STYLED_DIFFERENCE, STYLES_PREFIX)),
    'broken-part-styles-invalid.docx': ('repaired (Styles): no styles, as absent', Reading(styles='none'),
                                        ['main', 'lists', 'styled'], {}),
    'broken-part-core.docx': ('repaired: TITLE empty', Reading(lost=['core']), ['main', 'fields'],
                              {'TITLE_FIELD_STALE': PROPERTIES}),
    'broken-part-app.docx': ('repaired: DOCPROPERTY Company empty', Reading(lost=['app']), ['main', 'fields'],
                             {'COMPANY_FIELD_STALE': PROPERTIES}),
    'broken-part-custom.docx': ('repaired: DOCPROPERTY Custom1 an error', Reading(lost=['custom']), ['main', 'fields'],
                                # META_CUSTOM_VALUE is 0 both ways, docx4j's stale result being no value either
                                dict([(t, DEFECT_D) for t in GROUPS['fields'][:4]]
                                     + [(t, PROPERTIES + ', ' + DEFECT_D) for t in ('CUSTOM_FIELD_STALE', UNKNOWN_PROPERTY)])),
    'broken-part-commentsExtended.docx': ('repaired: the reply link lost ([AB2R1] becomes [AB2]), no token changes',
                                          Reading(), ['main', 'comments'], {}),
    'broken-part-document-rels.docx': ('repaired (Comments): the three relationships before the break kept, the '
                                       "comments' lost, their references left as empty comments",
                                       Reading(comments=False), ['main', 'theme', 'lists', 'styled', 'comments'],
                                       dict.fromkeys(GROUPS['lists'][:4] + GROUPS['styled'], RELATIONSHIPS)),
    'broken-part-package-rels.docx': ("repaired: the main part's relationship before the break kept, the three "
                                      "properties parts' lost", Reading(lost=['core', 'app', 'custom']),
                                      ['main', 'fields'], {**dict.fromkeys(GROUPS['main'], RELATIONSHIPS), **dict.fromkeys(GROUPS['fields'], DEFECT_E)}),
}
# the store files: (token, count in every view and under WORD); bindings: bound is both the default and word-text's
STORE_READINGS = {
    'store-not-read.docx': ('repaired: store 1 dropped, every control bound to store 2, whose root fits',
                            [('SNR_BEGIN', 1), ('SNR_END', 1), ('STORE1_VALUE', 0), ('STORE2_VALUE', 3),
                             ('A_STALE_SURFACE', 0), ('B_STALE_SURFACE', 0), ('C_STALE_SURFACE', 0)], STORE),
    'store-not-read-intact.docx': ('no repair; C, naming no store, bound to store 1, the first whose root fits',
                                   [('SNR_BEGIN', 1), ('SNR_END', 1), ('STORE1_VALUE', 2), ('STORE2_VALUE', 1),
                                    ('A_STALE_SURFACE', 0), ('B_STALE_SURFACE', 0), ('C_STALE_SURFACE', 0)], None),
}
# docx4j's part.not_read where the reading needs the part (REQ-801), in every view: the file's case, the part
NOT_READ = [('broken-part-styles.docx', '/word/styles.xml'), ('broken-part-numbering.docx', '/word/numbering.xml'),
            ('broken-part-settings.docx', '/word/settings.xml'), ('broken-part-numbering-order.docx', '/word/numbering.xml'),
            ('broken-part-styles-late.docx', '/word/styles.xml'), ('broken-part-styles-invalid.docx', '/word/styles.xml'),
            ('broken-part-document-rels.docx', '/word/_rels/document.xml.rels'), ('broken-part-package-rels.docx', '/_rels/.rels'),
            ('store-not-read.docx', '/customXml/item1.xml')]


def cases():
    return dict((name, FIRST_CASE + i) for i, (name, entries) in enumerate(fixtures()))


def tokens_table():
    case = cases()
    out = ['case\ttoken\taccepted\toriginal\tmarkup\toption\twith_option\tnote\tfixture\tstatements']
    for name, entries in fixtures():
        if name in WORD_READINGS:
            note, reading, groups, departures = WORD_READINGS[name]
            wanted = [t for g in groups for t in GROUPS[g]]
            rows = dict((row[0], row) for row in token_rows(reading))
            for i, token in enumerate(wanted):
                t, accepted, original, markup, word, statements = rows[token]
                if name != 'broken-part-intact.docx':
                    statements += ', REQ-801'
                if token in departures:
                    statements += ', ' + departures[token]
                option, with_option = (WORD, str(word)) if word is not None else ('', '')
                out.append('\t'.join([str(case[name]), token, str(accepted), str(original), str(markup), option,
                                      with_option, 'Word: ' + note if i == 0 else '', name, statements]))
        elif name in STORE_READINGS:
            note, counts, label = STORE_READINGS[name]
            for i, (token, n) in enumerate(counts):
                statements = 'REQ-509' + (', REQ-801, ' + label if label else '')
                out.append('\t'.join([str(case[name]), token, str(n), str(n), str(n), WORD, str(n),
                                      'Word: ' + note if i == 0 else '', name, statements]))
    return '\n'.join(out) + '\n'


def findings_table():
    case = cases()
    out = ['case\tfixture\tchannel\tkind\tselector\tscenario\texpected\tstatements']
    for name, part in NOT_READ:
        out.append('\t'.join([str(case[name]), name, 'warning', 'part.not_read', part, 'default', '1', 'REQ-801']))
    return '\n'.join(out) + '\n'


GOLDENS = 'fixture\tcase\tview\tscenario\tformat\tgolden\tstatements\n'


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.abspath(__file__))
    for name, entries in fixtures():
        write(os.path.join(out, name), entries)
        print('wrote', name)
    for name, text in (('expected-tokens.tsv', tokens_table()), ('expected-findings.tsv', findings_table()),
                       ('goldens.tsv', GOLDENS)):
        with open(os.path.join(out, name), 'w', encoding='utf-8', newline='\n') as f:
            f.write(text)
        print('wrote', name)
