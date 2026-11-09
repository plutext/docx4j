# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Checks build_phase0.py's packages before they go to Word, since a repair dialog halts the bridge.

Each package's document, settings and theme parts are validated with xmllint against the ECMA-376 transitional schemas
(Part 4's OfficeOpenXML-XMLSchema-Transitional.zip, unzipped), with a schema for wps:wsp written here from
[MS-ODRAWXML] 2.6 (CT_WordprocessingShape, CT_TextboxInfo), which ECMA-376 doesn't hold. mc:AlternateContent is read as
Word reads it: its mc:Choice (Requires="wps") stands in its place. Also checked: every wp:docPr id is unique in its
package, every relationship target is in the package, and every part has a content type.

usage: check_probes.py <transitional xsd dir> <docx> ..."""
import os, re, shutil, subprocess, sys, tempfile, zipfile

WPS_XSD = '''<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified"
  targetNamespace="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"
  xmlns="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"
  xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <xsd:import namespace="http://schemas.openxmlformats.org/drawingml/2006/main" schemaLocation="dml-main.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" schemaLocation="wml.xsd"/>
  <xsd:complexType name="CT_TextboxInfo">
    <xsd:sequence>
      <xsd:element ref="w:txbxContent" minOccurs="0"/>
      <xsd:element name="extLst" type="a:CT_OfficeArtExtensionList" minOccurs="0"/>
    </xsd:sequence>
    <xsd:attribute name="id" type="xsd:unsignedShort" use="optional" default="0"/>
  </xsd:complexType>
  <xsd:complexType name="CT_WordprocessingShape">
    <xsd:sequence>
      <xsd:element name="cNvPr" type="a:CT_NonVisualDrawingProps" minOccurs="0"/>
      <xsd:choice>
        <xsd:element name="cNvSpPr" type="a:CT_NonVisualDrawingShapeProps"/>
        <xsd:element name="cNvCnPr" type="a:CT_NonVisualConnectorProperties"/>
      </xsd:choice>
      <xsd:element name="spPr" type="a:CT_ShapeProperties"/>
      <xsd:element name="style" type="a:CT_ShapeStyle" minOccurs="0"/>
      <xsd:element name="extLst" type="a:CT_OfficeArtExtensionList" minOccurs="0"/>
      <xsd:element name="txbx" type="CT_TextboxInfo" minOccurs="0"/>
      <xsd:element name="bodyPr" type="a:CT_TextBodyProperties"/>
    </xsd:sequence>
    <xsd:attribute name="normalEastAsianFlow" type="xsd:boolean" use="optional" default="false"/>
  </xsd:complexType>
  <xsd:element name="wsp" type="CT_WordprocessingShape"/>
</xsd:schema>
'''

XML_XSD = '''<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" targetNamespace="http://www.w3.org/XML/1998/namespace">
  <xsd:attribute name="lang" type="xsd:language"/>
  <xsd:attribute name="space">
    <xsd:simpleType>
      <xsd:restriction base="xsd:NCName"><xsd:enumeration value="default"/><xsd:enumeration value="preserve"/></xsd:restriction>
    </xsd:simpleType>
  </xsd:attribute>
</xsd:schema>
'''

ALL_XSD = '''<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <xsd:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/wordprocessingml/2006/main" schemaLocation="wml.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/drawingml/2006/main" schemaLocation="dml-main.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/drawingml/2006/picture" schemaLocation="dml-picture.xsd"/>
  <xsd:import namespace="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" schemaLocation="dml-wordprocessingDrawing.xsd"/>
  <xsd:import namespace="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" schemaLocation="wps.xsd"/>
</xsd:schema>
'''

AC = re.compile(r'<mc:AlternateContent><mc:Choice Requires="wps">(.*?)</mc:Choice></mc:AlternateContent>', re.S)

def check(schema, docx, tmp):
    errors = []
    with zipfile.ZipFile(docx) as z:
        names = set(z.namelist())
        ct = z.read('[Content_Types].xml').decode()
        defaults = set(re.findall(r'Default Extension="([^"]+)"', ct))
        overrides = set(re.findall(r'Override PartName="/([^"]+)"', ct))
        for n in names:
            if n not in overrides and n.rsplit('.', 1)[-1] not in defaults:
                errors.append(f'no content type: {n}')
        for rels in [n for n in names if n.endswith('.rels')]:
            base = os.path.dirname(os.path.dirname(rels))
            for t in re.findall(r'Target="([^"]+)"', z.read(rels).decode()):
                if os.path.normpath(os.path.join(base, t)) not in names:
                    errors.append(f'{rels}: target missing: {t}')
        doc = z.read('word/document.xml').decode()
        ids = re.findall(r'<wp:docPr id="(\d+)"', doc)
        if len(ids) != len(set(ids)):
            errors.append('wp:docPr ids repeat')
        if doc.count('<mc:AlternateContent') != len(AC.findall(doc)):
            errors.append('an mc:AlternateContent of another form')
        parts = {'document': AC.sub(r'\1', doc), 'settings': z.read('word/settings.xml').decode(),
                 'theme': z.read('word/theme/theme1.xml').decode()}
    for k, xml in parts.items():
        f = os.path.join(tmp, k + '.xml')
        open(f, 'w', encoding='utf-8').write(xml)
        r = subprocess.run(['xmllint', '--noout', '--schema', schema, f], capture_output=True, text=True)
        if r.returncode != 0:
            errors.append(f'{k}: ' + r.stderr.strip()[:2000])
    return errors

def main(xsd_dir, docxs):
    tmp = tempfile.mkdtemp()
    try:
        sch = os.path.join(tmp, 'xsd')
        shutil.copytree(xsd_dir, sch)
        open(os.path.join(sch, 'xml.xsd'), 'w').write(XML_XSD)    # the schemas import it with no schemaLocation
        open(os.path.join(sch, 'wps.xsd'), 'w').write(WPS_XSD)
        open(os.path.join(sch, 'all.xsd'), 'w').write(ALL_XSD)
        bad = 0
        for d in docxs:
            e = check(os.path.join(sch, 'all.xsd'), d, tmp)
            print(('FAIL ' if e else 'ok   ') + os.path.basename(d))
            for x in e:
                print('     ' + x)
            bad += bool(e)
        print(f'{len(docxs) - bad} of {len(docxs)} valid')
        return 1 if bad else 0
    finally:
        shutil.rmtree(tmp)

if __name__ == '__main__':
    sys.exit(main(sys.argv[1], sys.argv[2:]))
