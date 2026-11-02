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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.Hdr;
import org.junit.Test;

/**
 * Two text-model field defects found reviewing docx4j-python's CR-026 and CR-027 (17.3.6).
 * (1) Under {@code fields: update} a list number in a header, footer or note keeps its stored result (CR-067
 * section 14.4; WD REQ-506): {@code FieldEvaluators.listNumber} has that guard, but {@code FieldValues} never told
 * the field context which part the field is in, so a header's LISTNUM showed the body's next number.
 * (2) A SET's stored value is never shown (CR-067 section 13.2): the complex form hid it, a {@code w:fldSimple}
 * showed it under {@code fields: cached}.
 */
public class FieldPartAndSimpleSetTest {

    private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

    private static String listNum(String stored) {
        return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> LISTNUM </w:instrText></w:r>"
                + "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + stored + "</w:t></w:r>"
                + "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
    }

    @Test
    public void aListNumberInAHeaderKeepsItsStoredResult() throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        MainDocumentPart mdp = pkg.getMainDocumentPart();
        Body body = (Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + ">"
                + "<w:p>" + listNum("STALE_A") + "<w:r><w:t xml:space=\"preserve\"> first</w:t></w:r></w:p>"
                + "<w:p>" + listNum("STALE_B") + "<w:r><w:t xml:space=\"preserve\"> second</w:t></w:r></w:p></w:body>"));
        mdp.getContent().addAll(body.getContent());
        HeaderPart hp = new HeaderPart();
        Relationship rel = mdp.addTargetPart(hp);
        hp.setJaxbElement((Hdr) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:hdr " + W + "><w:p>" + listNum("HDR_STORED") + "</w:p></w:hdr>")));
        mdp.getJaxbElement().getBody().setSectPr((org.docx4j.wml.SectPr) XmlUtils.unmarshalString("<w:sectPr " + W
                + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<w:headerReference w:type=\"default\" r:id=\"" + rel.getId() + "\"/></w:sectPr>"));

        TextResult r = Texts.of(pkg, new TextOptions().fields(TextOptions.Fields.UPDATE).include(TextOptions.Include.HEADERS));
        StringBuilder main = new StringBuilder(), header = new StringBuilder();
        for (TextBlock b : r.blocks()) (b.getStory() == TextBlock.Story.HEADER ? header : main).append(b.getText()).append('\n');
        assertTrue(header.toString(), header.toString().contains("HDR_STORED"));
        assertFalse("the body's list numbers are computed", main.toString().contains("STALE_"));
    }

    @Test
    public void aSimpleSetHidesItsStoredValueAsTheComplexFormDoes() throws Exception {
        for (TextOptions.Fields mode : TextOptions.Fields.values()) {
            WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
            Body body = (Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + ">"
                    + "<w:p><w:fldSimple w:instr=\" SET Price 5 \"><w:r><w:t>SIMPLE_STORED</w:t></w:r></w:fldSimple>"
                    + "<w:r><w:t xml:space=\"preserve\"> after</w:t></w:r></w:p>"
                    + "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> SET Cost 7 </w:instrText></w:r>"
                    + "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>COMPLEX_STORED</w:t></w:r>"
                    + "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r><w:r><w:t xml:space=\"preserve\"> after</w:t></w:r></w:p></w:body>"));
            pkg.getMainDocumentPart().getContent().addAll(body.getContent());

            TextResult r = Texts.of(pkg, new TextOptions().fields(mode));
            String text = r.blocks().get(0).getText() + "\n" + r.blocks().get(1).getText();
            assertFalse(mode + ": " + text, text.contains("SIMPLE_STORED"));
            assertFalse(mode + ": " + text, text.contains("COMPLEX_STORED"));
            assertTrue(text, text.contains("after"));
            TextField simple = r.blocks().get(0).getFields().get(0);
            assertNotNull(simple.getResult());
            assertEquals(mode + ": a SET shows nothing, in either form", "", simple.getResult());
            assertEquals(r.blocks().get(1).getFields().get(0).getResult(), simple.getResult());
        }
    }
}
