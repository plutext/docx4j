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
package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Style;
import org.junit.Test;

/** Probe spacing-autospacing-after-table's rule; the cases are the probe's. */
public class AutoSpacingAfterFloatingTableTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String CELLS = "<w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr><w:tc><w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
	private static final String FLOATING = "<w:tbl><w:tblPr><w:tblpPr w:vertAnchor=\"text\" w:tblpY=\"4369\"/></w:tblPr>" + CELLS;
	private static final String INLINE = "<w:tbl><w:tblPr/>" + CELLS;

	private static final String AUTO = "<w:p><w:pPr><w:spacing w:before=\"100\" w:beforeAutospacing=\"1\" w:after=\"100\" "
			+ "w:afterAutospacing=\"1\"/></w:pPr><w:r><w:t>under test</w:t></w:r></w:p>";
	private static final String PLAIN = "<w:p><w:r><w:t>plain</w:t></w:r></w:p>";

	private static WordprocessingMLPackage processed(String blocks) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + blocks + "</w:body></w:document>"));
		AutoSpacingAfterFloatingTable.process(pkg);
		return pkg;
	}

	private static PPrBase.Spacing spacing(WordprocessingMLPackage pkg, int block) {
		return ((P) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(block))).getPPr().getSpacing();
	}

	private static void assertDropped(PPrBase.Spacing s) {
		assertFalse(s.isBeforeAutospacing());
		assertEquals(BigInteger.ZERO, s.getBefore());
	}

	private static void assertKept(PPrBase.Spacing s) {
		assertTrue(s.isBeforeAutospacing());
		assertEquals(BigInteger.valueOf(100), s.getBefore());
	}

	@Test
	public void droppedAfterAFloatingTable() throws Exception {
		PPrBase.Spacing s = spacing(processed(FLOATING + AUTO), 1);
		assertDropped(s);
		assertTrue("the auto space after stands", s.isAfterAutospacing());
	}

	@Test
	public void droppedAfterTwoFloatingTables() throws Exception {
		assertDropped(spacing(processed(FLOATING + FLOATING + AUTO), 2));
	}

	@Test
	public void keptAfterAnInlineTable() throws Exception {
		assertKept(spacing(processed(INLINE + AUTO), 1));
	}

	@Test
	public void keptWithAParagraphBetween() throws Exception {
		assertKept(spacing(processed(FLOATING + PLAIN + AUTO), 2));
	}

	@Test
	public void anExplicitSpaceStays() throws Exception {
		PPrBase.Spacing s = spacing(processed(FLOATING
				+ "<w:p><w:pPr><w:spacing w:before=\"280\"/></w:pPr><w:r><w:t>x</w:t></w:r></w:p>"), 1);
		assertNull(s.isBeforeAutospacing());
		assertEquals(BigInteger.valueOf(280), s.getBefore());
	}

	@Test
	public void aBookmarkBetweenIsNotABlock() throws Exception {
		assertDropped(spacing(processed(FLOATING + "<w:bookmarkStart w:id=\"1\" w:name=\"b\"/><w:bookmarkEnd w:id=\"1\"/>" + AUTO), 3));
	}

	@Test
	public void insideABlockContentControl() throws Exception {
		WordprocessingMLPackage pkg = processed("<w:sdt><w:sdtContent>" + FLOATING + AUTO + "</w:sdtContent></w:sdt>");
		SdtBlock sdt = (SdtBlock) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(0));
		assertDropped(((P) XmlUtils.unwrap(sdt.getSdtContent().getContent().get(1))).getPPr().getSpacing());
	}

	/** Auto spacing from the paragraph's style: the paragraph's own w:spacing overrides it. */
	@Test
	public void fromTheParagraphStyle() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString(
				"<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Web\"><w:name w:val=\"Web\"/>"
				+ "<w:pPr><w:spacing w:beforeAutospacing=\"1\"/></w:pPr></w:style>"));
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + FLOATING
				+ "<w:p><w:pPr><w:pStyle w:val=\"Web\"/></w:pPr><w:r><w:t>x</w:t></w:r></w:p></w:body></w:document>"));
		AutoSpacingAfterFloatingTable.process(pkg);
		assertDropped(spacing(pkg, 1));
	}

	/** Where the compat flag turns auto spacing off the writers use the explicit values; Word there is not measured. */
	@Test
	public void leftAloneUnderTheCompatFlag() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart(true);
		CTSettings settings = dsp.getContents();
		CTCompat compat = settings.getCompat() == null ? new CTCompat() : settings.getCompat();
		compat.setDoNotUseHTMLParagraphAutoSpacing(new org.docx4j.wml.BooleanDefaultTrue());
		settings.setCompat(compat);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + FLOATING + AUTO + "</w:body></w:document>"));
		AutoSpacingAfterFloatingTable.process(pkg);
		assertKept(spacing(pkg, 1));
	}
}
