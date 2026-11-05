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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.FieldInstruction;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;
import org.junit.Test;

/** What FormCheckBoxes hands the FO writer: the size, state and colour of a FORMCHECKBOX. */
public class FormCheckBoxesTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static String field(String rPr, String checkBox, boolean separate) {
		String r = "<w:r>" + rPr;
		return r + "<w:fldChar w:fldCharType=\"begin\"><w:ffData><w:name w:val=\"Check\"/><w:enabled/>"
				+ "<w:checkBox>" + checkBox + "</w:checkBox></w:ffData></w:fldChar></w:r>"
				+ r + "<w:instrText xml:space=\"preserve\"> FORMCHECKBOX </w:instrText></w:r>"
				+ (separate ? r + "<w:fldChar w:fldCharType=\"separate\"/></w:r>" : "")
				+ r + "<w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private static final String AUTO = "<w:sizeAuto/><w:default w:val=\"0\"/>";
	private static final String SIZE_19 = "<w:rPr><w:sz w:val=\"19\"/></w:rPr>";

	private static P processed(String paragraphContent) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		return processed(pkg, paragraphContent);
	}

	private static P processed(WordprocessingMLPackage pkg, String paragraphContent) throws Exception {
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>" + paragraphContent + "</w:p></w:body></w:document>"));
		FormCheckBoxes.process(pkg);
		return (P) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(0));
	}

	private static List<String> parameters(P p, int item) {
		CTSimpleField field = (CTSimpleField) XmlUtils.unwrap(p.getContent().get(item));
		FieldInstruction instruction = FieldInstruction.parse(field.getInstr());
		assertEquals("FORMCHECKBOX", instruction.getName());
		return instruction.getParameters();
	}

	@Test
	public void theFieldBecomesOneSimpleFieldBeforeItsText() throws Exception {
		P p = processed(field(SIZE_19, AUTO, true) + "<w:r><w:t>label</w:t></w:r>");
		assertEquals(2, p.getContent().size());
		assertEquals(java.util.Arrays.asList("19", "0", "auto"), parameters(p, 0));
		assertTrue(XmlUtils.unwrap(p.getContent().get(1)) instanceof R);
	}

	/** No text, as Word's has none, so a tab after the box is still a leading tab; but a run
	 *  with the field's properties, which the line's height counts. */
	@Test
	public void itKeepsAnEmptyRun() throws Exception {
		P p = processed(field(SIZE_19, AUTO, true));
		R run = (R) XmlUtils.unwrap(((CTSimpleField) XmlUtils.unwrap(p.getContent().get(0))).getContent().get(0));
		assertTrue(run.getContent().isEmpty());
		assertEquals(19, run.getRPr().getSz().getVal().intValue());
	}

	/** An exact size lifts the line as a glyph of that size would, so its run has that size. */
	@Test
	public void anExactSizeGoesOnTheRun() throws Exception {
		P p = processed(field(SIZE_19, "<w:size w:val=\"40\"/><w:default w:val=\"0\"/>", true));
		R run = (R) XmlUtils.unwrap(((CTSimpleField) XmlUtils.unwrap(p.getContent().get(0))).getContent().get(0));
		assertEquals(40, run.getRPr().getSz().getVal().intValue());
	}

	@Test
	public void aFieldWithNoSeparate() throws Exception {
		assertEquals("19", parameters(processed(field(SIZE_19, AUTO, false)), 0).get(0));
	}

	/** w:size wins over the run's size, as a font size would. */
	@Test
	public void anExactSize() throws Exception {
		assertEquals("40", parameters(processed(field(SIZE_19, "<w:size w:val=\"40\"/><w:default w:val=\"0\"/>", true)), 0).get(0));
	}

	/** w:checked where stated, else w:default. */
	@Test
	public void theState() throws Exception {
		assertEquals("1", parameters(processed(field(SIZE_19, "<w:sizeAuto/><w:default w:val=\"0\"/><w:checked/>", true)), 0).get(1));
		assertEquals("1", parameters(processed(field(SIZE_19, "<w:sizeAuto/><w:default w:val=\"1\"/>", true)), 0).get(1));
		assertEquals("0", parameters(processed(field(SIZE_19, "<w:sizeAuto/><w:default w:val=\"1\"/><w:checked w:val=\"0\"/>", true)), 0).get(1));
	}

	@Test
	public void theRunsColour() throws Exception {
		assertEquals("000080", parameters(processed(field("<w:rPr><w:color w:val=\"000080\"/></w:rPr>", AUTO, true)), 0).get(2));
	}

	/** The size and colour may be the style's. */
	@Test
	public void fromACharacterStyle() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString(
				"<w:style " + W + " w:type=\"character\" w:styleId=\"Big\"><w:name w:val=\"Big\"/>"
				+ "<w:rPr><w:color w:val=\"FF0000\"/><w:sz w:val=\"36\"/></w:rPr></w:style>"));
		List<String> parameters = parameters(processed(pkg, field("<w:rPr><w:rStyle w:val=\"Big\"/></w:rPr>", AUTO, true)), 0);
		assertEquals("36", parameters.get(0));
		assertEquals("FF0000", parameters.get(2));
	}

	@Test
	public void aHiddenFieldIsLeftAlone() throws Exception {
		P p = processed(field("<w:rPr><w:vanish/></w:rPr>", AUTO, true));
		assertEquals(4, p.getContent().size());
	}

	@Test
	public void aFieldThatDoesNotEndHereIsLeftAlone() throws Exception {
		String begin = field(SIZE_19, AUTO, false);
		P p = processed(begin.substring(0, begin.lastIndexOf("<w:r>")));
		assertEquals(2, p.getContent().size());
	}

	@Test
	public void aBookmarkInsideTheFieldStays() throws Exception {
		String f = field(SIZE_19, AUTO, true);
		int cut = f.lastIndexOf("<w:r>");
		P p = processed(f.substring(0, cut) + "<w:bookmarkStart w:id=\"1\" w:name=\"b\"/><w:bookmarkEnd w:id=\"1\"/>" + f.substring(cut));
		assertEquals("the simple field, two bookmark tags", 3, p.getContent().size());
	}

	@Test
	public void anotherFieldIsLeftAlone() throws Exception {
		P p = processed("<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText> PAGE </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>");
		assertEquals(3, p.getContent().size());
	}
}
