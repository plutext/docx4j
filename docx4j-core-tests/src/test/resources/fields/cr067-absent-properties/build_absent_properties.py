# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-067's addition: what a property field reads where its properties part, or the property, is absent.
Every field docx4j's evaluator knows over a property, each dirty, so Word offers to update it on open.
usage: build_absent_properties.py <staging dir> <set>"""
import hashlib, json, os, sys, zipfile

DECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
PKG = 'http://schemas.openxmlformats.org/package/2006/relationships'
OD = 'application/vnd.openxmlformats-officedocument.'

FIELDS = ['TITLE', 'SUBJECT', 'AUTHOR', 'KEYWORDS', 'COMMENTS', 'LASTSAVEDBY', 'REVNUM', 'CREATEDATE', 'SAVEDATE', 'PRINTDATE', 'INFO Title']
FIELDS += ['DOCPROPERTY ' + n for n in (
    'Title', 'Subject', 'Author', 'Keywords', 'Comments', 'LastSavedBy', 'RevisionNumber', 'CreateTime', 'LastSavedTime', 'LastPrinted',
    'Company', 'Manager', 'HyperlinkBase', 'Template', 'NameofApplication', 'Pages', 'Words', 'Characters', 'CharactersWithSpaces', 'Lines',
    'Paragraphs', 'Security', 'TotalEditingTime', 'Custom1', 'NoSuchName')]

def field(instr):
    tag = instr.replace(' ', '_').upper()
    return ('<w:p><w:r><w:t xml:space="preserve">%s = </w:t></w:r><w:fldSimple w:instr=" %s " w:dirty="true"><w:r><w:t>STALE_%s</w:t></w:r></w:fldSimple></w:p>'
            % (tag, instr, tag))

DOCUMENT = (DECL + '<w:document xmlns:w="%s"><w:body>' % W + ''.join(field(f) for f in FIELDS)
            + '<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/></w:sectPr></w:body></w:document>')

CORE_NS = ('xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" '
           'xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"')
CORE = (DECL + '<cp:coreProperties %s><dc:title>META_TITLE</dc:title><dc:subject>META_SUBJECT</dc:subject><dc:creator>META_AUTHOR</dc:creator>'
        '<cp:keywords>META_KEYWORDS</cp:keywords><dc:description>META_COMMENTS</dc:description><cp:lastModifiedBy>META_LASTSAVEDBY</cp:lastModifiedBy>'
        '<cp:revision>33</cp:revision><cp:lastPrinted>2003-03-03T03:03:00Z</cp:lastPrinted>'
        '<dcterms:created xsi:type="dcterms:W3CDTF">2001-01-01T01:01:00Z</dcterms:created><dcterms:modified xsi:type="dcterms:W3CDTF">2002-02-02T02:02:00Z</dcterms:modified>'
        '</cp:coreProperties>' % CORE_NS)
CORE_EMPTY = DECL + '<cp:coreProperties %s/>' % CORE_NS
APP_NS = 'xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"'
APP = (DECL + '<Properties %s><Template>META_TEMPLATE.dotm</Template><TotalTime>44</TotalTime><Pages>77</Pages><Words>777</Words><Characters>7777</Characters>'
       '<Application>META_APPLICATION</Application><DocSecurity>0</DocSecurity><Lines>66</Lines><Paragraphs>55</Paragraphs><Manager>META_MANAGER</Manager>'
       '<Company>META_COMPANY</Company><CharactersWithSpaces>8888</CharactersWithSpaces><HyperlinkBase>META_BASE</HyperlinkBase></Properties>' % APP_NS)
APP_EMPTY = DECL + '<Properties %s/>' % APP_NS
CUSTOM_HEAD = DECL + ('<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" '
                      'xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">')
def custom(name):
    return CUSTOM_HEAD + '<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="%s"><vt:lpwstr>META_CUSTOM</vt:lpwstr></property></Properties>' % name

PARTS = {
    'core': ('docProps/core.xml', 'application/vnd.openxmlformats-package.core-properties+xml', 'http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties'),
    'app': ('docProps/app.xml', OD + 'extended-properties+xml', R + '/extended-properties'),
    'custom': ('docProps/custom.xml', OD + 'custom-properties+xml', R + '/custom-properties'),
}
FULL = {'core': CORE, 'app': APP, 'custom': custom('Custom1')}

FILES = [
    ('fp-00-control', FULL),
    ('fp-01-no-core', {k: v for k, v in FULL.items() if k != 'core'}),
    ('fp-02-no-app', {k: v for k, v in FULL.items() if k != 'app'}),
    ('fp-03-no-custom', {k: v for k, v in FULL.items() if k != 'custom'}),
    ('fp-04-no-properties', {}),
    ('fp-05-properties-missing', {'core': CORE_EMPTY, 'app': APP_EMPTY, 'custom': custom('Other')}),
]

def entry(z, name, text):
    i = zipfile.ZipInfo(name, (2026, 11, 7, 0, 0, 0))
    i.compress_type = zipfile.ZIP_STORED
    z.writestr(i, text.encode('utf-8'))

def build(path, parts):
    ct = ('<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
          '<Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="' + OD + 'wordprocessingml.document.main+xml"/>'
          + ''.join('<Override PartName="/%s" ContentType="%s"/>' % (PARTS[k][0], PARTS[k][1]) for k in parts) + '</Types>')
    rels = ('<Relationships xmlns="%s"><Relationship Id="rId1" Type="%s/officeDocument" Target="word/document.xml"/>' % (PKG, R)
            + ''.join('<Relationship Id="rId%d" Type="%s" Target="%s"/>' % (i + 2, PARTS[k][2], PARTS[k][0]) for i, k in enumerate(parts)) + '</Relationships>')
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_STORED) as z:
        entry(z, '[Content_Types].xml', DECL + ct)
        entry(z, '_rels/.rels', DECL + rels)
        entry(z, 'word/document.xml', DOCUMENT)
        for k, xml in parts.items():
            entry(z, PARTS[k][0], xml)

def main(staging, setname):
    sums, order = [], []
    for name, parts in FILES:
        for copy in 'ab':
            job = '%s-%s' % (name, copy)
            d = os.path.join(staging, setname, job)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            build(f, parts)
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as o:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 120, 'updateFields': True, 'formats': ['docx', 'txt', 'pdf']}, o)
            for x in (j, f):
                sums.append('%s  %s/%s/%s\n' % (hashlib.sha256(open(x, 'rb').read()).hexdigest(), setname, job, os.path.basename(x)))
            order.append(job + '\n')
    open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
    print(len(FILES), 'files,', len(order), 'jobs,', len(FIELDS), 'fields')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
