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
import java.io.ByteArrayOutputStream;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.*;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.docx4j.wml.GlossaryDocument;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.Sld;

/** Cases 53 and 54, requested by Jason on 18 October 2026 against WD 0.5. */
final class ExtractionAdditionalCases {
    static final String PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    static final String P = "http://schemas.openxmlformats.org/presentationml/2006/main";
    static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
    static final String[] VISIBLE = {"EMB53_SLIDE_ONE", "EMB53_GROUP_FIRST", "EMB53_GROUP_NESTED",
            "EMB53_AFTER_GROUP", "EMB53_TABLE_A", "EMB53_TABLE_B", "EMB53_TABLE_C", "EMB53_TABLE_D", "EMB53_SLIDE_TWO"};

    static void add(ExtractionProbeBuilder b, WordprocessingMLPackage pkg) throws Exception {
        MainDocumentPart mdp = pkg.getMainDocumentPart();
        b.begin(53, "an embedded presentation with groups, a table, a hidden slide and speaker notes");
        String preview = mdp.addTargetPart(BinaryPartAbstractImage.createImagePart(pkg, png(64, 32, 0x8866aa))).getId();
        String embedded = ExtractionPhase0Cases.part(mdp, "/word/embeddings/probe53.pptx", PPTX, "package", presentation());
        // External DOCM geometry uses the same builder counters, but a different ID scope.
        // Keep that already checked file unchanged when adding shapes to this DOCX.
        int shapeId = b.shapeId;
        b.body.append(p(ExtractionPhase0Cases.object(b, embedded, preview, "PowerPoint.Show.12", false)));
        b.shapeId = shapeId;
        for (String marker : VISIBLE)
            b.gated(marker, "embedded: true", "slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605)");
        b.token("EMB53_HIDDEN_SLIDE", "0", "0", "0", "embedded: true; hidden: true", "1",
                "hidden slide show=0; absent by default and with embedded: true; read with embedded: true and hidden: true, as hidden text is");
        b.never("EMB53_SPEAKER_NOTES", "speaker notes omitted, including with embedded: true");

        b.begin(54, "a glossary document with two building-block entries");
        b.body.append(pt("The glossary holds two entries; neither is inserted into the main story."));
        GlossaryDocumentPart glossary = new GlossaryDocumentPart();
        glossary.setJaxbElement(new GlossaryDocument());
        mdp.addTargetPart(glossary);
        b.raw.put(glossary.getPartName().getName(), "<w:glossaryDocument" + NS + "><w:docParts>"
                + glossaryEntry("One", "GLS54_ENTRY_ONE", "A153C174-0664-4354-953A-111111111111")
                + glossaryEntry("Two", "GLS54_ENTRY_TWO", "A153C174-0664-4354-953A-222222222222")
                + "</w:docParts></w:glossaryDocument>");
        b.gated("GLS54_ENTRY_ONE", "include: glossary", "glossary comes last, after endnotes; first docPart (REQ-501, REQ-516)");
        b.gated("GLS54_ENTRY_TWO", "include: glossary", "second docPart, after the first (REQ-501, REQ-516)");
    }

    static String glossaryEntry(String name, String marker, String guid) {
        return "<w:docPart><w:docPartPr><w:name w:val='Probe glossary " + name + "'/>"
                + "<w:category><w:name w:val='General'/><w:gallery w:val='autoTxt'/></w:category>"
                + "<w:types><w:type w:val='autoExp'/></w:types><w:behaviors><w:behavior w:val='content'/></w:behaviors>"
                + "<w:guid w:val='{" + guid + "}'/></w:docPartPr><w:docPartBody>" + pt(marker)
                + "</w:docPartBody></w:docPart>";
    }

    static byte[] presentation() throws Exception {
        PresentationMLPackage pptx = PresentationMLPackage.createPackage();
        MainPresentationPart main = pptx.getMainPresentationPart();
        SlideLayoutPart layout = (SlideLayoutPart) pptx.getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));
        // Filenames deliberately disagree with slide order. Positions deliberately disagree with tree order.
        String group = "<p:grpSp>" + nonVisualGroup(3, "Outer group") + groupTransform()
                + shape(4, "EMB53_GROUP_FIRST", 3000000, 3000000)
                + "<p:grpSp>" + nonVisualGroup(5, "Nested group") + groupTransform()
                + shape(6, "EMB53_GROUP_NESTED", 0, 0) + "</p:grpSp></p:grpSp>";
        SlidePart first = slide(main, layout, 9, false, shape(2, "EMB53_SLIDE_ONE", 5000000, 5000000)
                + group + shape(7, "EMB53_AFTER_GROUP", 100000, 100000) + table());
        slide(main, layout, 4, true, shape(2, "EMB53_HIDDEN_SLIDE", 0, 0));
        slide(main, layout, 1, false, shape(2, "EMB53_SLIDE_TWO", 0, 0));
        first.createNotesSlidePart(main.getNotesMasterPart(true)).addNoteTextPara("EMB53_SPEAKER_NOTES", "en-AU");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Docx4J.save(pptx, out, Docx4J.FLAG_NONE);
        return redate(out.toByteArray());
    }

    static SlidePart slide(MainPresentationPart main, SlideLayoutPart layout, int fileNumber, boolean hidden, String shapes) throws Exception {
        SlidePart slide = new SlidePart(new PartName("/ppt/slides/slide" + fileNumber + ".xml"));
        String xml = "<p:sld xmlns:p='" + P + "' xmlns:a='" + A + "'" + (hidden ? " show='0'" : "")
                + "><p:cSld><p:spTree>" + nonVisualGroup(1, "") + "<p:grpSpPr/>" + shapes
                + "</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sld>";
        slide.setJaxbElement((Sld) XmlUtils.unmarshalString(xml, Context.jcPML, Sld.class));
        main.addSlideIdListEntry(slide);
        slide.addTargetPart(layout);
        return slide;
    }
    static String nonVisualGroup(int id, String name) {
        return "<p:nvGrpSpPr><p:cNvPr id='" + id + "' name='" + name + "'/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>";
    }
    static String groupTransform() {
        return "<p:grpSpPr><a:xfrm><a:off x='0' y='0'/><a:ext cx='4000000' cy='4000000'/>"
                + "<a:chOff x='0' y='0'/><a:chExt cx='4000000' cy='4000000'/></a:xfrm></p:grpSpPr>";
    }
    static String shape(int id, String marker, int x, int y) {
        return "<p:sp><p:nvSpPr><p:cNvPr id='" + id + "' name='Probe shape " + id + "'/><p:cNvSpPr txBox='1'/><p:nvPr/></p:nvSpPr>"
                + "<p:spPr><a:xfrm><a:off x='" + x + "' y='" + y + "'/><a:ext cx='2500000' cy='400000'/></a:xfrm>"
                + "<a:prstGeom prst='rect'><a:avLst/></a:prstGeom></p:spPr><p:txBody>" + textBody(marker) + "</p:txBody></p:sp>";
    }
    static String textBody(String marker) { return "<a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>" + marker + "</a:t></a:r></a:p>"; }
    static String table() {
        StringBuilder xml = new StringBuilder("<p:graphicFrame><p:nvGraphicFramePr><p:cNvPr id='8' name='Probe table'/>"
                + "<p:cNvGraphicFramePr/><p:nvPr/></p:nvGraphicFramePr><p:xfrm><a:off x='100000' y='6000000'/>"
                + "<a:ext cx='5000000' cy='800000'/></p:xfrm><a:graphic><a:graphicData uri='" + A + "/table'>"
                + "<a:tbl><a:tblPr/><a:tblGrid><a:gridCol w='2500000'/><a:gridCol w='2500000'/></a:tblGrid>");
        for (String[] row : new String[][] {{"EMB53_TABLE_A", "EMB53_TABLE_B"}, {"EMB53_TABLE_C", "EMB53_TABLE_D"}}) {
            xml.append("<a:tr h='400000'>");
            for (String marker : row) xml.append("<a:tc><a:txBody>").append(textBody(marker)).append("</a:txBody><a:tcPr/></a:tc>");
            xml.append("</a:tr>");
        }
        return xml.append("</a:tbl></a:graphicData></a:graphic></p:graphicFrame>").toString();
    }

    static String orders() {
        return "case\tfixture\tscenario\ttokens\tstatements\n"
                + "53\textraction-probe.docx\tembedded: true\t" + String.join(" > ", VISIBLE) + "\tREQ-512, REQ-605\n"
                + "54\textraction-probe.docx\tinclude: glossary\tEN27_NOTE_TEXT > GLS54_ENTRY_ONE > GLS54_ENTRY_TWO\tREQ-501, REQ-503, REQ-516\n";
    }
}
