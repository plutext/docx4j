package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A <code>w:br</code> which <b>opens</b> a paragraph takes a line of its own, as one which
 * ends it does: the paragraph's text is on its second line.
 *
 * <p>Measured (CR-001 &#xa7;4.3): after a one-line paragraph, Word's gap before the text of
 * <code>&lt;w:p&gt;&lt;w:r&gt;&lt;w:br/&gt;&lt;/w:r&gt;&lt;w:r&gt;...</code> is 25.7pt -
 * two 12.85pt lines - where docx4j's was 13.1pt, so everything below it was 12.6pt high.</p>
 *
 * @since 17.0.6
 */
public class LeadingBreakLineTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	/** The height of the lines the flow produced, in millipoints.  A w:br is an
	 *  fo:block of no height, which FOP counts as a line but which draws none; only
	 *  the total tells whether Word's line is there. */
	private static int lineHeight(org.w3c.dom.Document areaTree) {
		org.w3c.dom.NodeList las = areaTree.getElementsByTagName("lineArea");
		int total = 0;
		for (int i = 0; i < las.getLength(); i++) {
			String bpda = ((org.w3c.dom.Element) las.item(i)).getAttribute("bpda");
			if (bpda.length() > 0) total += Integer.parseInt(bpda.trim());
		}
		return total;
	}

	private void check(int flags) throws Exception {

		int plain = lineHeight(areaTree(pkg(
				"<w:p><w:r><w:t>first</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>second</w:t></w:r></w:p>"), flags));

		int leadingBreak = lineHeight(areaTree(pkg(
				"<w:p><w:r><w:t>first</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:br/></w:r><w:r><w:t>second</w:t></w:r></w:p>"), flags));

		assertEquals("a leading w:br takes a line of its own",
				plain + plain / 2, leadingBreak);   // two lines become three
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
