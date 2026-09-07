package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A legacy form field paints the state its {@code w:ffData} holds, which neither FO
 * pathway read: a {@code FORMDROPDOWN} came out as nothing at all.
 *
 * <p>Measured against Word 365 on a document whose drop-down offers four honorifics
 * with no {@code w:result}, and whose {@code separate} is immediately followed by its
 * {@code end}: Word paints the first of them, so its line runs 297.7..413.6 where ours
 * began at 302.7 and ended at 378.3.  With the fix ours begins at 297.7 too.</p>
 *
 * @since 17.1.0
 */
public class FormFieldResultTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String DDLIST =
			"<w:name w:val=\"Dropdown1\"/><w:enabled/><w:ddList>"
			+ "<w:listEntry w:val=\"Mr\"/><w:listEntry w:val=\"Mrs\"/>"
			+ "<w:listEntry w:val=\"Ms\"/></w:ddList>";

	private static String field(String ffData, String instr, boolean separate, String result) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"><w:ffData>" + ffData
				+ "</w:ffData></w:fldChar></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText></w:r>"
				+ (separate ? "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" : "")
				+ (result == null ? "" : "<w:r><w:t>" + result + "</w:t></w:r>")
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	private static String foText(String body, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(body));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new ByteArrayInputStream(baos.toByteArray()));
		return doc.getDocumentElement().getTextContent();
	}

	private void theSelectedEntryIsPainted(int flags) throws Exception {
		String out = foText("<w:p>" + field(DDLIST, "FORMDROPDOWN", true, null)
				+ "<w:r><w:t xml:space=\"preserve\"> and then the name</w:t></w:r></w:p>", flags);
		assertTrue("the drop-down's selected entry is not in the FO: " + out,
				out.contains("Mr"));
		assertEquals("and only the selected one", -1, out.indexOf("Mrs"));
	}

	@Test
	public void dropDownVisitor() throws Exception {
		theSelectedEntryIsPainted(Docx4J.FLAG_NONE);
	}

	@Test
	public void dropDownXslt() throws Exception {
		theSelectedEntryIsPainted(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** No separate at all: one is synthesised so the result has somewhere to go. */
	private void aFieldWithoutASeparate(int flags) throws Exception {
		String out = foText("<w:p>" + field(DDLIST, "FORMDROPDOWN", false, null) + "</w:p>", flags);
		assertTrue("the entry is painted even with no separate: " + out, out.contains("Mr"));
	}

	@Test
	public void noSeparateVisitor() throws Exception {
		aFieldWithoutASeparate(Docx4J.FLAG_NONE);
	}

	@Test
	public void noSeparateXslt() throws Exception {
		aFieldWithoutASeparate(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A text field with nothing typed into it shows its default; one with a value keeps it. */
	private void aTextInput(int flags) throws Exception {
		String ff = "<w:name w:val=\"T\"/><w:textInput><w:default w:val=\"Given\"/></w:textInput>";
		assertTrue(foText("<w:p>" + field(ff, "FORMTEXT", true, null) + "</w:p>", flags)
				.contains("Given"));
		String typed = foText("<w:p>" + field(ff, "FORMTEXT", true, "Jason") + "</w:p>", flags);
		assertTrue(typed.contains("Jason"));
		assertEquals("the default must not be painted as well", -1, typed.indexOf("Given"));
	}

	@Test
	public void textInputVisitor() throws Exception {
		aTextInput(Docx4J.FLAG_NONE);
	}

	@Test
	public void textInputXslt() throws Exception {
		aTextInput(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * Word draws a checkbox as a stroked square path and puts nothing in the PDF's text
	 * layer, so docx4j writes no character for one either (word-layout-rules.md
	 * &sect;7).
	 */
	private void aCheckBoxPaintsNoCharacter(int flags) throws Exception {
		String ff = "<w:name w:val=\"C\"/><w:checkBox><w:sizeAuto/><w:default w:val=\"0\"/></w:checkBox>";
		String out = foText("<w:p>" + field(ff, "FORMCHECKBOX", false, null)
				+ "<w:r><w:t xml:space=\"preserve\"> agreed</w:t></w:r></w:p>", flags);
		assertEquals("no empty box glyph", -1, out.indexOf('☐'));
		assertEquals("no crossed box glyph", -1, out.indexOf('☒'));
		assertTrue(out.contains("agreed"));
	}

	@Test
	public void checkBoxVisitor() throws Exception {
		aCheckBoxPaintsNoCharacter(Docx4J.FLAG_NONE);
	}

	@Test
	public void checkBoxXslt() throws Exception {
		aCheckBoxPaintsNoCharacter(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
