package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.NodeList;

/**
 * Text which we generate ourselves - a footnote or endnote number, the dots of a
 * tab leader, the space which stands in for an otherwise empty paragraph - has no
 * w:t to hang a font off.  Unless we set one, it is rendered (and measured) in the
 * renderer's default font, rather than the font which applies to it.
 *
 * @since 17.0.3
 */
public class GeneratedTextFontTest extends AbstractXSLFOTest {

	private static final String FONT = "Courier New";
	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String RPR =
			"<w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/></w:rPr>";

	/** the font-family of the ordinary text in the document; everything we generate
	 *  in that paragraph should be in the same font */
	private String textFont(org.w3c.dom.Document doc, String startsWith) {

		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i=0; i<nl.getLength(); i++) {
			org.w3c.dom.Element el = (org.w3c.dom.Element)nl.item(i);
			if (el.getTextContent().startsWith(startsWith)
					&& el.getAttribute("font-family").length()>0) {
				return el.getAttribute("font-family");
			}
		}
		return null;
	}

	@Test
	public void testNoteReferenceMarks() throws Exception {

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(notesPkg()));

		String font = textFont(doc, "Body text");
		assertTrue("no font on the ordinary text", font!=null);

		// a footnote reference, an endnote reference, and the numbers in the notes:
		// four generated "1"s, each in the font of the run it belongs to (since
		// 17.0.5 nothing else is added: Word raises a note number only if its run is
		// a superscript)
		assertTrue("expected 4 note numbers in the text's font",
				4 == countMarks(doc, font));
		assertTrue("a note number has no font, or not the font of the text",
				isAbsent(doc, "//fo:inline[text()='1'][not(@font-family='" + font + "')]"));

		// the footnote's content sits directly in the footnote body (no list-block),
		// its number inline in the first paragraph, found by w:id
		assertTrue("the footnote's paragraph is not the footnote body's block",
				isPresent(doc, "//fo:footnote-body/fo:block//fo:inline[@font-family='" + font + "'][text()='1']"));
		assertTrue("the footnote's text was not found by w:id",
				isPresent(doc, "//fo:footnote-body//fo:inline[contains(text(),'The footnote.')]"));
		assertTrue("footnote body still a list-block",
				isAbsent(doc, "//fo:footnote-body//fo:list-block"));
	}

	private int countMarks(org.w3c.dom.Document doc, String font) {
		int count = 0;
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i=0; i<nl.getLength(); i++) {
			org.w3c.dom.Element el = (org.w3c.dom.Element)nl.item(i);
			if ("1".equals(el.getTextContent()) && font.equals(el.getAttribute("font-family"))) count++;
		}
		return count;
	}

	@Test
	public void testTabLeaderAndTabSpaces() throws Exception {

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(tabsPkg()));

		String font = textFont(doc, "Chapter one");
		assertTrue("no font on the ordinary text", font!=null);

		assertTrue("the dot leader has no font, or not the font of the text",
				isPresent(doc, "//fo:leader[@leader-pattern='dots'][@font-family='" + font + "']"));

		// a tab where the paragraph has no dot leader is a leader of no length, which
		// the Word line manager sizes during layout (since 17.0.5); it too carries the
		// run's font, since a leader it lands on is drawn in it
		assertTrue("the leader standing in for a tab has no font, or not the font of the text",
				isPresent(doc, "//fo:leader[@leader-length='0pt'][@font-family='" + font + "']"));
	}

	@Test
	public void testEmptyParagraph() throws Exception {

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(toFO(tabsPkg()));

		// the block's own content stands for the paragraph mark, so it is the
		// paragraph mark's font which applies
		assertTrue("the space in an empty block has no font",
				isPresent(doc, "//fo:block[@white-space-treatment='preserve'][@font-family]"));

				// Since 17.0.5 (CR-001 Phase 1) a block with content also carries a font:
		// the one of the run owning most of its text, together with the line-height
		// Word gives that run, because FOP sizes lines from the block's font and
		// line-height (line-height on fo:inline is ignored for line stacking).
		// The runs' own fonts still apply to the text itself.
		assertTrue("a block with content has no font/line-height of its dominant run",
				isPresent(doc, "//fo:block[@font-family][contains(@line-height,'pt')][not(@white-space-treatment='preserve')]"));
	}

	private byte[] toFO(WordprocessingMLPackage wordMLPackage) throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(wordMLPackage);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);
		return baos.toByteArray();
	}

	/** body text with a footnote and an endnote, all runs in FONT */
	private WordprocessingMLPackage notesPkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:r>" + RPR + "<w:t xml:space=\"preserve\">Body text </w:t></w:r>"
				+ "<w:r>" + RPR + "<w:footnoteReference w:id=\"2\"/></w:r>"
				+ "<w:r>" + RPR + "<w:t xml:space=\"preserve\"> and </w:t></w:r>"
				+ "<w:r>" + RPR + "<w:endnoteReference w:id=\"2\"/></w:r>"
				+ "</w:p></w:body></w:document>"));

		FootnotesPart fp = new FootnotesPart();
		fp.setJaxbElement((CTFootnotes)XmlUtils.unmarshalString(
				"<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"0\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				// NB getFootnote indexes by position, so this has to be here for the ids to line up
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"1\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"2\"><w:p>"
				+   "<w:r>" + RPR + "<w:footnoteRef/></w:r>"
				+   "<w:r>" + RPR + "<w:t xml:space=\"preserve\"> The footnote.</w:t></w:r>"
				+ "</w:p></w:footnote></w:footnotes>", Context.jc, CTFootnotes.class));
		pkg.getMainDocumentPart().addTargetPart(fp);

		EndnotesPart ep = new EndnotesPart();
		ep.setJaxbElement((CTEndnotes)XmlUtils.unmarshalString(
				"<w:endnotes " + W + ">"
				+ "<w:endnote w:type=\"separator\" w:id=\"0\"><w:p><w:r><w:separator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:type=\"continuationSeparator\" w:id=\"1\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:id=\"2\"><w:p>"
				+   "<w:r>" + RPR + "<w:endnoteRef/></w:r>"
				+   "<w:r>" + RPR + "<w:t xml:space=\"preserve\"> The endnote.</w:t></w:r>"
				+ "</w:p></w:endnote></w:endnotes>", Context.jc, CTEndnotes.class));
		pkg.getMainDocumentPart().addTargetPart(ep);

		return pkg;
	}

	/** a dot leader tab, a plain tab, and an empty paragraph */
	private WordprocessingMLPackage tabsPkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"9016\"/></w:tabs></w:pPr>"
				+   "<w:r>" + RPR + "<w:t>Chapter one</w:t></w:r>"
				+   "<w:r>" + RPR + "<w:tab/></w:r>"
				+   "<w:r>" + RPR + "<w:t>7</w:t></w:r>"
				+ "</w:p>"
				+ "<w:p>"
				+   "<w:r>" + RPR + "<w:t>Before</w:t></w:r>"
				+   "<w:r>" + RPR + "<w:tab/></w:r>"
				+   "<w:r>" + RPR + "<w:t>After</w:t></w:r>"
				+ "</w:p>"
				// empty, with the font on the paragraph mark
				+ "<w:p><w:pPr><w:rPr><w:rFonts w:ascii=\"" + FONT + "\" w:hAnsi=\"" + FONT + "\"/>"
				+   "<w:sz w:val=\"48\"/></w:rPr></w:pPr></w:p>"
				+ "</w:body></w:document>"));

		return pkg;
	}
}
