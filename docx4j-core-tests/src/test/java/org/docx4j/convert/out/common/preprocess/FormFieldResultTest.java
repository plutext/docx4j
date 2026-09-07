package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A legacy form field keeps its state in the {@code w:ffData} of its
 * {@code w:fldChar w:fldCharType="begin"}, and Word paints that state rather than the
 * field result: for a drop-down, the {@code w:listEntry} which {@code w:ddList/w:result}
 * selects; for a text field with nothing typed into it, {@code w:textInput/w:default}.
 * Neither FO pathway nor HTML reads {@code w:ffData}, so a drop-down came out as
 * nothing but the empty {@code fo:inline} of its bookmark.
 *
 * <p>Measured against Word 365 on a document whose drop-down offers four honorifics
 * with no {@code w:result}, and whose {@code separate} is immediately followed by its
 * {@code end}: Word paints the first of them, so its line runs 297.7..413.6 where ours
 * began at 302.7 and ended at 378.3 - the whole entry missing.</p>
 *
 * @since 17.0.6
 */
public class FormFieldResultTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String process(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FieldsCombiner.process(pkg);
		return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true);
	}

	private static String begin(String ffData) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"><w:ffData>" + ffData
				+ "</w:ffData></w:fldChar></w:r>";
	}
	private static String instr(String s) {
		return "<w:r><w:instrText xml:space=\"preserve\">" + s + "</w:instrText></w:r>";
	}
	private static final String SEP = "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>";
	private static final String END = "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";

	private static final String DDLIST =
			"<w:name w:val=\"Dropdown1\"/><w:enabled/><w:ddList>"
			+ "<w:listEntry w:val=\"Mr\"/><w:listEntry w:val=\"Mrs\"/>"
			+ "<w:listEntry w:val=\"Ms\"/></w:ddList>";

	/** No w:result: Word paints the first w:listEntry (ECMA-376 17.16.20). */
	@Test
	public void aDropDownPaintsTheSelectedEntry() throws Exception {
		String out = process("<w:p>" + begin(DDLIST) + instr(" FORMDROPDOWN ") + SEP + END + "</w:p>");
		assertTrue("the selected entry is the field result", out.contains("Mr"));
		assertFalse("and not one of the others", out.contains("Mrs"));
	}

	/** w:result is a zero-based index into the w:listEntry list. */
	@Test
	public void aDropDownResultIndexSelects() throws Exception {
		String dd = DDLIST.replace("<w:ddList>", "<w:ddList><w:result w:val=\"2\"/>");
		String out = process("<w:p>" + begin(dd) + instr(" FORMDROPDOWN ") + SEP + END + "</w:p>");
		assertTrue(out.contains("Ms"));
		assertFalse(out.contains("Mr"));
	}

	/** A field with no separate has no result at all, so one is synthesised. */
	@Test
	public void aDropDownWithoutASeparateGetsOne() throws Exception {
		String out = process("<w:p>" + begin(DDLIST) + instr(" FORMDROPDOWN ") + END + "</w:p>");
		assertTrue("the entry is painted", out.contains("Mr"));
		// with a separate and a result, the field combines to a w:fldSimple
		assertTrue("the field is now combinable", out.contains("fldSimple"));
	}

	/** A text field with nothing typed into it shows its default. */
	@Test
	public void aTextInputShowsItsDefault() throws Exception {
		String ff = "<w:name w:val=\"Text2\"/><w:textInput><w:default w:val=\"Given\"/></w:textInput>";
		String out = process("<w:p>" + begin(ff) + instr(" FORMTEXT ") + SEP + END + "</w:p>");
		assertTrue(out.contains("Given"));
	}

	/** What the user typed wins over the default: the field result stays as it is. */
	@Test
	public void aTypedValueIsNotReplacedByTheDefault() throws Exception {
		String ff = "<w:name w:val=\"Text2\"/><w:textInput><w:default w:val=\"Given\"/></w:textInput>";
		String out = process("<w:p>" + begin(ff) + instr(" FORMTEXT ") + SEP
				+ "<w:r><w:t>Jason</w:t></w:r>" + END + "</w:p>");
		assertTrue(out.contains("Jason"));
		assertFalse("the default must not be added as well", out.contains("Given"));
	}

	/** Word draws a checkbox as a stroked square path with nothing in the text layer,
	 *  so no character is written for it (word-layout-rules.md &sect;5.7). */
	@Test
	public void aCheckBoxGetsNoGlyph() throws Exception {
		String ff = "<w:name w:val=\"Check1\"/><w:checkBox><w:sizeAuto/><w:default w:val=\"0\"/></w:checkBox>";
		String out = process("<w:p>" + begin(ff) + instr(" FORMCHECKBOX ") + END + "</w:p>");
		assertFalse(out.contains("☐"));
		assertFalse(out.contains("☒"));
		assertFalse("no separate is synthesised either", out.contains("separate"));
	}

	/** The begin's run properties carry to the result Word paints in them. */
	@Test
	public void theResultKeepsTheFieldsRunProperties() throws Exception {
		String out = process("<w:p><w:r><w:rPr><w:b/></w:rPr>"
				+ "<w:fldChar w:fldCharType=\"begin\"><w:ffData>" + DDLIST
				+ "</w:ffData></w:fldChar></w:r>"
				+ instr(" FORMDROPDOWN ") + SEP + END + "</w:p>");
		assertTrue(out.contains("Mr"));
		int t = out.indexOf("Mr");
		assertTrue("the result run is bold", out.lastIndexOf("<w:b/>", t) > 0);
	}
}
