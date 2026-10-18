/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.textextraction;

import static org.docx4j.textextraction.ExtractionProbeBuilder.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.docx4j.relationships.Relationship;
import org.docx4j.org.apache.poi.poifs.filesystem.*;

/** Cases 39–52 (53–54 are in ExtractionAdditionalCases). The authored packages, rather than Word's normalized saves, are the fixtures. */
final class ExtractionPhase0Cases {
    static final String REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/";
    static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    static final String DOCX_CHUNK = AltChunkType.WordprocessingML.getContentType();
    static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static final Path SUPPORT = Path.of("docx4j-core-tests/src/test/resources/text-extraction/support");
    static final String[] REQUIREMENTS = {
        "", "301,302,518", "302", "302", "302", "302", "303", "302", "301",
        "402,403", "404", "402,405", "402", "404", "404", "404", "404", "404,518,503",
        "507,902", "505", "505", "703", "508", "509", "517", "518,511,502", "502",
        "503,402", "504", "601,510", "601,603", "605", "604", "514", "511", "511",
        "513", "604", "515,516", "512", "512", "512", "512,902", "513", "513", "513",
        "513", "513", "902,602,518,507", "506,902", "506,801,902", "902", "515", "512,605", "501,516",
        "520,505", "703,510", "512,507"
    };

    static String statements(int n) {
        StringJoiner out = new StringJoiner(", ");
        for (String req : REQUIREMENTS[n].split(",")) out.add("REQ-" + req);
        return out.toString();
    }

    static String fixture(int n) {
        if (n >= 55) return ExtractionStructureCases.FIXTURE; // 19 October 2026, a fixture of their own
        return n >= 50 && n <= 52 ? "extraction-probe-external.docm" : "extraction-probe.docx";
    }

    static void add(ExtractionProbeBuilder b, WordprocessingMLPackage pkg) throws Exception {
        MainDocumentPart mdp = pkg.getMainDocumentPart();
        String preview = mdp.addTargetPart(BinaryPartAbstractImage.createImagePart(pkg, png(64, 32, 0x7799bb))).getId();
        b.begin(39, "an embedded workbook");
        String xlsx = part(mdp, "/word/embeddings/probe39.xlsx", XLSX, "package", workbook());
        b.body.append(p(object(b, xlsx, preview, "Excel.Sheet.12", false)));
        b.gated("EMB39_XLSX_CELL", "embedded: true", "worksheet row at the reference; REQ-512's embedded-workbook case");

        b.begin(40, "an embedded document containing another embedded document");
        byte[] inner = document(pt("EMB40_NESTED_TEXT"), Map.of(), "", "");
        String nestedRel = relationship("rId1", "package", "embeddings/nested.docx", false)
                + relationship("rId2", "image", "media/preview.png", false);
        byte[] outer = document(pt("EMB40_DOCX_TEXT") + p(object(b, "rId1", "rId2", "Word.Document.12", false)),
                Map.of("word/embeddings/nested.docx", inner, "word/media/preview.png", png(64, 32, 0x7799bb)),
                nestedRel, override("/word/embeddings/nested.docx", DOCX));
        String docx = part(mdp, "/word/embeddings/probe40.docx", DOCX, "package", outer);
        b.body.append(p(object(b, docx, preview, "Word.Document.12", false)));
        b.gated("EMB40_DOCX_TEXT", "embedded: true", "one level only");
        b.never("EMB40_NESTED_TEXT", "nested embedded document is not read, including with embedded: true");

        b.begin(41, "an OLE package containing a plain-text file");
        String ole = part(mdp, "/word/embeddings/probe41.bin", "application/vnd.openxmlformats-officedocument.oleObject", "oleObject", ole());
        b.body.append(p(object(b, ole, preview, "Package", false)));
        b.gated("OLE41_NATIVE_TEXT", "embedded: true", "Ole10Native stream holding probe.txt");

        b.begin(42, "an ActiveX label with a preview");
        BinaryPart controlPart = new BinaryPart(new PartName("/word/activeX/activeX42.xml"));
        controlPart.setContentType(new ContentType("application/vnd.ms-office.activeX+xml"));
        controlPart.setRelationshipType(REL + "control");
        String control = mdp.addTargetPart(controlPart).getId();
        BinaryPart controlBinary = new BinaryPart(new PartName("/word/activeX/activeX42.bin"));
        controlBinary.setContentType(new ContentType("application/vnd.ms-office.activeX"));
        controlBinary.setRelationshipType("http://schemas.microsoft.com/office/2006/relationships/activeXControlBinary");
        controlBinary.setBinaryData(ProbeActiveXControl.build());
        String binaryId = controlPart.addTargetPart(controlBinary).getId();
        controlPart.setBinaryData(utf8("<ax:ocx xmlns:ax='http://schemas.microsoft.com/office/2006/activeX' "
                + "xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships' "
                + "ax:classid='" + ProbeActiveXControl.CLASS_ID + "' ax:persistence='persistStorage' r:id='" + binaryId + "'/>"));
        int shape = b.shapeId++;
        b.body.append("<w:p><w:r><w:object>" + previewShape(shape, preview)
                + "<w:control r:id='" + control + "' w:name='ProbeButton' w:shapeid='_x0000_i" + shape + "'/></w:object></w:r></w:p>");
        b.never("ACX42_CAPTION", "never extracted, including embedded: true; inspection: activex");

        b.begin(43, "a plain-text altChunk");
        chunk(b, mdp, "43.txt", "text/plain", "ALT43_TEXT_LINE_ONE\r\nALT43_TEXT_LINE_TWO\r\n".getBytes(StandardCharsets.UTF_8));
        expanded(b, "ALT43_TEXT_LINE_ONE"); expanded(b, "ALT43_TEXT_LINE_TWO");
        b.begin(44, "an MHT altChunk");
        chunk(b, mdp, "44.mht", "message/rfc822", ("MIME-Version: 1.0\r\nContent-Type: multipart/related; boundary=probe44\r\n\r\n"
                + "--probe44\r\nContent-Type: text/html; charset=utf-8\r\nContent-Transfer-Encoding: 8bit\r\n"
                + "Content-Location: file:///probe44.htm\r\n\r\n<html><body><p>ALT44_MHT_CHUNK</p></body></html>\r\n--probe44--\r\n").getBytes(StandardCharsets.UTF_8));
        b.gated("ALT44_MHT_CHUNK", "xhtml importer present", "without importer: altchunk.not_expanded; altchunks: false also omits it");
        b.begin(45, "an RTF altChunk");
        chunk(b, mdp, "45.rtf", "application/rtf", "{\\rtf1\\ansi ALT45_RTF_CHUNK\\par}".getBytes(StandardCharsets.US_ASCII));
        b.never("ALT45_RTF_CHUNK", "never expanded; warning altchunk.not_expanded");
        b.begin(46, "a Flat OPC altChunk");
        String flat = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"
                + "<?mso-application progid='Word.Document'?>"
                + "<pkg:package xmlns:pkg='http://schemas.microsoft.com/office/2006/xmlPackage'>"
                + "<pkg:part pkg:name='/_rels/.rels' pkg:contentType='application/vnd.openxmlformats-package.relationships+xml'>"
                + "<pkg:xmlData>" + rels(relationship("rId1", "officeDocument", "word/document.xml", false)) + "</pkg:xmlData></pkg:part>"
                + "<pkg:part pkg:name='/word/document.xml' pkg:contentType='application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml'>"
                + "<pkg:xmlData><w:document" + NS + "><w:body>" + pt("ALT46_FLATOPC_CHUNK")
                + "<w:sectPr/></w:body></w:document></pkg:xmlData></pkg:part></pkg:package>";
        chunk(b, mdp, "46.xml", "application/xml", utf8(flat)); expanded(b, "ALT46_FLATOPC_CHUNK");
        b.begin(47, "a document altChunk containing an HTML altChunk");
        byte[] nested = document(pt("ALT47_DOCX_CHUNK") + "<w:altChunk r:id='rId1'/>",
                Map.of("word/nested.html", utf8("<html><body><p>ALT47_NESTED_CHUNK</p></body></html>")),
                relationship("rId1", "aFChunk", "nested.html", false), override("/word/nested.html", "text/html"));
        chunk(b, mdp, "47.docx", DOCX_CHUNK, nested); expanded(b, "ALT47_DOCX_CHUNK");
        b.never("ALT47_NESTED_CHUNK", "one-level limit; warning altchunk.not_expanded for nested HTML");

        b.begin(48, "inspection kinds and characters preserved in extraction");
        b.body.append(p(r("<w:color w:val='777777'/><w:shd w:val='clear' w:fill='777777'/>", "SHD48_SHADED ")
                + r("ZW48_SP\u200bLIT TAG48_MARK\udb40\udc41\udb40\udc42 ")));
        b.body.append(p("<w:r>" + b.vmlBox(pt("OFF48_BOX")).replace("width:216pt;height:36pt", "position:absolute;margin-left:-5000pt;margin-top:-5000pt;width:216pt;height:36pt") + "</w:r>"));
        b.body.append(p("<w:r>" + b.vmlBox(pt("ZER48_BOX")).replace("width:216pt;height:36pt", "width:0pt;height:0pt") + "</w:r>"));
        b.body.append("<w:p><w:pPr><w:rPr><w:specVanish/></w:rPr></w:pPr>" + r("SPV48_SEPARATOR") + "</w:p>");
        b.always("SHD48_SHADED", "inspection: colour near background");
        b.always("ZW48_SP\u200bLIT", "U+200B retained; inspection: zero-width characters");
        b.always("TAG48_MARK", "followed by U+E0041 U+E0042, retained; inspection: tag characters");
        b.box("OFF48_BOX", "position -5000pt; inspection: off page (position requires layout)");
        b.box("ZER48_BOX", "width and height zero; inspection: zero size");
        b.always("SPV48_SEPARATOR", "specVanish is a style separator, not hidden text");

        b.begin(49, "external fields with cached results");
        b.body.append(p(field(" DDEAUTO ProbeServer DDE49_INSTR Item ", "DDE49_RESULT", "") + r(" ")
                + field(" INCLUDETEXT &quot;file:///INC49_INSTR.docx&quot; ", "INC49_RESULT", "") + r(" ")
                + field(" HYPERLINK &quot;file:///FLD49_FILE_MARK&quot; ", "Local file link", "")));
        b.gated("DDE49_INSTR", "field_codes: true", "inspection: external; never evaluated under fields: update");
        b.always("DDE49_RESULT", "cached result, including fields: update");
        b.gated("INC49_INSTR", "field_codes: true", "inspection: external; never fetched under fields: update");
        b.always("INC49_RESULT", "cached result, including fields: update");
        b.gated("FLD49_FILE_MARK", "field_codes: true", "inspection: external; Markdown also carries the link target");
    }

    static byte[] external(ExtractionProbeBuilder b) throws Exception {
        StringBuilder body = new StringBuilder();
        b.begin(50, "external relationships and updateFields");
        body.append(pt("Case 50: external relationships and updateFields"));
        body.append(p(b.picture("rId4", null, "", "").replace("r:embed=", "r:link=")));
        body.append(p(object(b, "rId5", "rId2", "Excel.Sheet.12", true)));
        for (String marker : new String[] {"TPL50_MARK", "MRG50_MARK", "LNK50_MARK", "OLE50_LINK_MARK"})
            b.never(marker, "external target; inspection: external; never fetched under any option");
        b.begin(51, "a VBA module");
        body.append(pt("Case 51: a VBA module containing a comment marker and no executable procedures"));
        b.never("VBA51_MODULE_MARK", "inspection: macro; never extracted under any option");
        b.begin(52, "custom and application properties");
        body.append(pt("Case 52: custom and application properties"));
        b.gated("CPR52_CUSTOM", "metadata: true", "always in block-model metadata");
        b.gated("APP52_COMPANY", "metadata: true", "always in block-model metadata");
        byte[] vba = Files.readAllBytes(SUPPORT.resolve("vba-project.bin"));
        ProbeVbaProject.check(vba);
        Map<String, byte[]> extra = new LinkedHashMap<>();
        extra.put("word/vbaProject.bin", vba);
        extra.put("word/media/preview.png", png(64, 32, 0x7799bb));
        extra.put("word/settings.xml", utf8("<w:settings" + NS + "><w:attachedTemplate r:id='rId1'/>"
                + "<w:mailMerge><w:mainDocumentType w:val='formLetters'/><w:dataType w:val='textFile'/>"
                + "<w:connectString w:val=''/><w:dataSource r:id='rId2'/></w:mailMerge>"
                + "<w:updateFields w:val='true'/></w:settings>"));
        extra.put("word/_rels/settings.xml.rels", utf8(rels(
                relationship("rId1", "attachedTemplate", "file:///CR046-MISSING/TPL50_MARK.dotx", true)
                + relationship("rId2", "mailMergeSource", "file:///CR046-MISSING/MRG50_MARK.txt", true))));
        extra.put("docProps/custom.xml", utf8("<Properties xmlns='http://schemas.openxmlformats.org/officeDocument/2006/custom-properties' "
                + "xmlns:vt='http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes'>"
                + "<property fmtid='{D5CDD505-2E9C-101B-9397-08002B2CF9AE}' pid='2' name='ProbeCustom'>"
                + "<vt:lpwstr>CPR52_CUSTOM</vt:lpwstr></property></Properties>"));
        extra.put("docProps/app.xml", utf8("<Properties xmlns='http://schemas.openxmlformats.org/officeDocument/2006/extended-properties'>"
                + "<Application>Microsoft Office Word</Application><Company>APP52_COMPANY</Company></Properties>"));
        String rels = relationship("rId1", "settings", "settings.xml", false)
                + relationship("rId3", "http://schemas.microsoft.com/office/2006/relationships/vbaProject", "vbaProject.bin", false)
                + relationship("rId2", "image", "media/preview.png", false)
                + relationship("rId4", "image", "file:///CR046-MISSING/LNK50_MARK.png", true)
                + relationship("rId5", "oleObject", "file:///CR046-MISSING/OLE50_LINK_MARK.xlsx", true);
        String types = override("/word/settings.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml")
                + override("/word/vbaProject.bin", "application/vnd.ms-office.vbaProject")
                + override("/docProps/custom.xml", "application/vnd.openxmlformats-officedocument.custom-properties+xml")
                + override("/docProps/app.xml", "application/vnd.openxmlformats-officedocument.extended-properties+xml");
        return document(body.toString(), extra, rels, types, true);
    }

    static void expanded(ExtractionProbeBuilder b, String token) {
        b.token(token, "1", "1", "1", "altchunks: false", "0", "disabled chunks warn altchunk.not_expanded");
    }
    static void chunk(ExtractionProbeBuilder b, MainDocumentPart mdp, String suffix, String type, byte[] bytes) throws Exception {
        AlternativeFormatInputPart part = new AlternativeFormatInputPart(new PartName("/word/chunk" + suffix));
        part.setContentType(new ContentType(type)); part.setBinaryData(bytes);
        b.body.append("<w:altChunk r:id='" + mdp.addTargetPart(part).getId() + "'/>");
    }
    static String part(MainDocumentPart mdp, String name, String type, String rel, byte[] bytes) throws Exception {
        BinaryPart part = rel.equals("package") ? new EmbeddedPackagePart(new PartName(name)) : new BinaryPart(new PartName(name));
        part.setContentType(new ContentType(type)); part.setRelationshipType(REL + rel); part.setBinaryData(bytes);
        return mdp.addTargetPart(part).getId();
    }
    static String object(ExtractionProbeBuilder b, String relationship, String image, String progId, boolean link) {
        int id = b.shapeId++;
        return "<w:r><w:object w:dxaOrig='1280' w:dyaOrig='640'>" + previewShape(id, image)
                + "<o:OLEObject Type='" + (link ? "Link" : "Embed") + "' ProgID='" + progId + "' ShapeID='_x0000_i" + id
                + "' DrawAspect='Icon' ObjectID='_" + (1000000000 + id) + "' r:id='" + relationship + "'"
                + (link ? " UpdateMode='OnCall'><o:LinkType>Picture</o:LinkType><o:LockedField>true</o:LockedField></o:OLEObject>" : "/>")
                + "</w:object></w:r>";
    }
    static String previewShape(int id, String image) {
        return "<v:shapetype id='_x0000_t75' coordsize='21600,21600' o:spt='75' o:preferrelative='t' path='m@4@5l@4@11@9@11@9@5xe' filled='f' stroked='f'><v:stroke joinstyle='miter'/><v:formulas><v:f eqn='if lineDrawn pixelLineWidth 0'/><v:f eqn='sum @0 1 0'/><v:f eqn='sum 0 0 @1'/><v:f eqn='prod @2 1 2'/><v:f eqn='prod @3 21600 pixelWidth'/><v:f eqn='prod @3 21600 pixelHeight'/><v:f eqn='sum @0 0 1'/><v:f eqn='prod @6 1 2'/><v:f eqn='prod @7 21600 pixelWidth'/><v:f eqn='sum @8 21600 0'/><v:f eqn='prod @7 21600 pixelHeight'/><v:f eqn='sum @10 21600 0'/></v:formulas><v:path o:extrusionok='f' gradientshapeok='t' o:connecttype='rect'/><o:lock v:ext='edit' aspectratio='t'/></v:shapetype><v:shape id='_x0000_i" + id + "' type='#_x0000_t75' style='width:64pt;height:32pt' o:ole=''>"
                + "<v:imagedata r:id='" + image + "' o:title=''/></v:shape>";
    }
    static byte[] ole() throws Exception {
        Ole10Native nativeData = new Ole10Native("probe.txt", "probe.txt", "probe.txt", "OLE41_NATIVE_TEXT\r\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream nativeBytes = new ByteArrayOutputStream(); nativeData.writeOut(nativeBytes);
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            Ole10Native.createOleMarkerEntry(fs);
            fs.createDocument(new ByteArrayInputStream(nativeBytes.toByteArray()), Ole10Native.OLE10_NATIVE);
            fs.getRoot().setStorageClsid(new org.docx4j.org.apache.poi.hpsf.ClassID("{0003000C-0000-0000-C000-000000000046}"));
            ByteArrayOutputStream out = new ByteArrayOutputStream(); fs.writeFilesystem(out);
            byte[] bytes = out.toByteArray();
            try (POIFSFileSystem check = new POIFSFileSystem(new ByteArrayInputStream(bytes))) {
                if (!Arrays.equals(nativeData.getDataBuffer(), Ole10Native.createFromEmbeddedOleObject(check).getDataBuffer()))
                    throw new IOException("Ole10Native round trip failed");
            }
            return bytes;
        }
    }
    static byte[] workbook() throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("[Content_Types].xml", utf8(types(override("/xl/workbook.xml", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml")
                + override("/xl/worksheets/sheet1.xml", "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"))));
        entries.put("_rels/.rels", utf8(rels(relationship("rId1", "officeDocument", "xl/workbook.xml", false))));
        entries.put("xl/workbook.xml", utf8("<workbook xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main' xmlns:r='" + REL.substring(0, REL.length()-1)
                + "'><sheets><sheet name='Probe' sheetId='1' r:id='rId1'/></sheets></workbook>"));
        entries.put("xl/_rels/workbook.xml.rels", utf8(rels(relationship("rId1", "worksheet", "worksheets/sheet1.xml", false))));
        entries.put("xl/worksheets/sheet1.xml", utf8("<worksheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'>"
                + "<sheetData><row r='1'><c r='A1' t='inlineStr'><is><t>EMB39_XLSX_CELL</t></is></c></row></sheetData></worksheet>"));
        return zip(entries);
    }
    static byte[] document(String body, Map<String, byte[]> extra, String relationships, String overrides) throws Exception {
        return document(body, extra, relationships, overrides, false);
    }
    static byte[] document(String body, Map<String, byte[]> extra, String relationships, String overrides, boolean macro) throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("[Content_Types].xml", utf8(types(override("/word/document.xml", macro
                ? "application/vnd.ms-word.document.macroEnabled.main+xml"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml") + overrides)));
        String roots = relationship("rId1", "officeDocument", "word/document.xml", false);
        if (macro) roots += relationship("rId2", "custom-properties", "docProps/custom.xml", false)
                + relationship("rId3", "extended-properties", "docProps/app.xml", false);
        entries.put("_rels/.rels", utf8(rels(roots)));
        entries.put("word/document.xml", utf8("<w:document" + NS + "><w:body>" + body + "<w:sectPr>" + PAGE + "</w:sectPr></w:body></w:document>"));
        if (!relationships.isEmpty()) entries.put("word/_rels/document.xml.rels", utf8(rels(relationships)));
        entries.putAll(extra);
        return zip(entries);
    }
    static String relationship(String id, String type, String target, boolean external) {
        return "<Relationship Id='" + id + "' Type='" + (type.startsWith("http:") ? type : REL + type)
                + "' Target='" + target + "'" + (external ? " TargetMode='External'" : "") + "/>";
    }
    static String rels(String body) { return "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'>" + body + "</Relationships>"; }
    static String override(String name, String type) { return "<Override PartName='" + name + "' ContentType='" + type + "'/>"; }
    static String types(String overrides) {
        return "<Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'>"
                + "<Default Extension='rels' ContentType='application/vnd.openxmlformats-package.relationships+xml'/>"
                + "<Default Extension='xml' ContentType='application/xml'/><Default Extension='png' ContentType='image/png'/>" + overrides + "</Types>";
    }
    static byte[] utf8(String xml) { return xml.replace('\'', '"').getBytes(StandardCharsets.UTF_8); }
    static byte[] zip(Map<String, byte[]> entries) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> e : entries.entrySet()) put(zip, e.getKey(), e.getValue());
        }
        return out.toByteArray();
    }
}
