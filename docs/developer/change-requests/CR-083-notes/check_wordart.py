# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Checks build_wordart.py's packages before they go to Word, since a repair dialog halts the bridge.

CR-081's check_probes.py does the work (ECMA-376 transitional schemas, wps written from [MS-ODRAWXML], the package's
relationships and content types). This adds what WordArt needs:
- the VML schemas (vml-main.xsd, vml-officeDrawing.xsd), which w:pict's lax wildcard then applies to v: and o:
  elements, and the header parts (a watermark is in one);
- the w14 run effects, which the transitional w:rPr doesn't hold: each run's w14 elements are cut out of its w:rPr and
  checked as [MS-DOCX]'s EG_RPrTextEffects sequence, against a schema made from docx4j's own
  xsd/wml/w14_word_2010_wordml.xsd (its types, without its imports or JAXB customisations; ST_HexColorRGB from ECMA's
  shared types, where ECMA keeps it), so that the rest of the w:rPr is checked by ECMA's.

usage: check_wordart.py <transitional xsd dir> <docx> ..."""
import importlib.util, os, re, shutil, subprocess, sys, tempfile, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('check81', os.path.join(HERE, '..', 'CR-081-notes', 'check_probes.py'))
cp = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(cp)
W14_SRC = os.path.normpath(os.path.join(HERE, '..', '..', '..', '..', 'xsd', 'wml', 'w14_word_2010_wordml.xsd'))
W14 = 'http://schemas.microsoft.com/office/word/2010/wordml'

ALL_XSD = cp.ALL_XSD.replace('</xsd:schema>',
    '  <xsd:import namespace="urn:schemas-microsoft-com:vml" schemaLocation="vml-main.xsd"/>\n'
    '  <xsd:import namespace="urn:schemas-microsoft-com:office:office" schemaLocation="vml-officeDrawing.xsd"/>\n'
    '</xsd:schema>')

def w14_xsd():
    """docx4j's w14 schema cut to the run effects' types, importing ECMA's wml.xsd and dml-main.xsd, with a root
    element holding the EG_RPrTextEffects sequence."""
    s = open(W14_SRC, encoding='utf-8-sig').read()
    start = s.index('<xsd:complexType name="CT_Percentage">')
    end = s.index('</xsd:group>', s.index('<xsd:group name="EG_RPrTextEffects">')) + len('</xsd:group>')
    body = s[start:end]
    body = re.sub(r'<xsd:annotation>.*?</xsd:annotation>', '', body, flags=re.S)
    body = re.sub(r'<!--.*?-->', '', body, flags=re.S)
    body = body.replace('"w:ST_HexColorRGB"', '"s:ST_HexColorRGB"')    # ECMA keeps it in the shared types
    onoff = re.search(r'<xsd:simpleType name="ST_OnOff">.*?</xsd:simpleType>', s, re.S).group(0)
    return f'''<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="qualified"
  targetNamespace="{W14}" xmlns="{W14}"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
  xmlns:s="http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes">
  <xsd:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes" schemaLocation="shared-commonSimpleTypes.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" schemaLocation="wml.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/drawingml/2006/main" schemaLocation="dml-main.xsd"/>
  {onoff}
  {body}
  <xsd:element name="effects"><xsd:complexType><xsd:sequence><xsd:group ref="EG_RPrTextEffects"/></xsd:sequence></xsd:complexType></xsd:element>
</xsd:schema>
'''

RPR = re.compile(r'<w:rPr>(.*?)</w:rPr>', re.S)
W14_EL = re.compile(r'<w14:(\w+)\b[^>]*?(?:/>|>.*?</w14:\1>)', re.S)

def cut_w14(xml):
    """The XML with every w:rPr's w14 elements removed, and the list of each w:rPr's w14 elements."""
    blocks = []
    def one(m):
        inner = m.group(1)
        found = ''.join(x.group(0) for x in W14_EL.finditer(inner))
        if found:
            blocks.append(found)
        return '<w:rPr>' + W14_EL.sub('', inner) + '</w:rPr>'
    return RPR.sub(one, xml), blocks

def check(sch, docx, tmp):
    errors = []
    with zipfile.ZipFile(docx) as z:
        names = z.namelist()
        hdrs = {n: z.read(n).decode() for n in names if re.match(r'word/header\d+\.xml$', n)}
        doc = z.read('word/document.xml').decode()
    for n, x in hdrs.items():
        if 'r:id=' in x or 'r:embed=' in x:
            errors.append(f'{n}: a relationship reference, and headers have no rels here')
    # check_probes' own check on the package, with its document's w14 removed
    cut, blocks = cut_w14(doc)
    cut = cut.replace(' mc:Ignorable="w14"', '', 1)          # markup compatibility's own attribute, read before the schema
    staged = os.path.join(tmp, 'cut.docx')
    with zipfile.ZipFile(docx) as src, zipfile.ZipFile(staged, 'w') as dst:
        for n in src.namelist():
            data = src.read(n)
            if n == 'word/document.xml':
                data = cut.encode('utf-8')
            dst.writestr(n, data)
    errors += cp.check(os.path.join(sch, 'all.xsd'), staged, tmp)
    for n, x in hdrs.items():
        f = os.path.join(tmp, 'hdr.xml')
        open(f, 'w', encoding='utf-8').write(x.replace(' mc:Ignorable="w14"', '', 1))
        r = subprocess.run(['xmllint', '--noout', '--schema', os.path.join(sch, 'all.xsd'), f], capture_output=True, text=True)
        if r.returncode != 0:
            errors.append(f'{n}: ' + r.stderr.strip()[:2000])
    for i, b in enumerate(sorted(set(blocks))):
        f = os.path.join(tmp, f'w14-{i}.xml')
        open(f, 'w', encoding='utf-8').write(f'<w14:effects xmlns:w14="{W14}">{b}</w14:effects>')
        r = subprocess.run(['xmllint', '--noout', '--schema', os.path.join(sch, 'w14.xsd'), f], capture_output=True, text=True)
        if r.returncode != 0:
            errors.append('w14: ' + r.stderr.strip()[:2000])
    return errors, len(set(blocks))

def main(xsd_dir, docxs):
    tmp = tempfile.mkdtemp()
    try:
        sch = os.path.join(tmp, 'xsd')
        shutil.copytree(xsd_dir, sch)
        for name, text in (('xml.xsd', cp.XML_XSD), ('wps.xsd', cp.WPS_XSD), ('wpg.xsd', cp.WPG_XSD),
                           ('wpc.xsd', cp.WPC_XSD), ('all.xsd', ALL_XSD), ('w14.xsd', w14_xsd())):
            open(os.path.join(sch, name), 'w').write(text)
        bad = 0
        for d in docxs:
            e, nb = check(sch, d, tmp)
            print(('FAIL ' if e else 'ok   ') + os.path.basename(d) + (f' ({nb} w14 blocks)' if nb else ''))
            for x in e:
                print('     ' + x)
            bad += bool(e)
        print(f'{len(docxs) - bad} of {len(docxs)} valid')
        return 1 if bad else 0
    finally:
        shutil.rmtree(tmp)

if __name__ == '__main__':
    sys.exit(main(sys.argv[1], sys.argv[2:]))
