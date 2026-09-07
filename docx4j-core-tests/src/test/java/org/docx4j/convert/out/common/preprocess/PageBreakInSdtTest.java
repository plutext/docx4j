package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Br;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.junit.Test;

/**
 * {@code PageBreak} walked the body's own children only, so a paragraph inside a
 * {@code w:sdt} - a table of contents, a cover page, any building block - was never
 * visited: neither the {@code w:pageBreakBefore} conversion nor the 17.1.0 split at the
 * break reached it, and the {@code w:br w:type="page"} stayed nested in an
 * {@code fo:inline}, where FOP ignores it.
 *
 * <p>Measured on a document whose contents control wraps two such paragraphs: the FO came
 * out as
 * {@code <block space-before="12pt" start-indent="17.85pt" text-indent="-17.85pt"><inline><block break-before="page"/></inline><inline>Copyrights...}
 * - FOP laid the 12pt space-before down on the <em>previous</em> page and the leading
 * block-level child ate the first-line indent.  Word's page 2 heading is at y=97.0 x=72.0
 * against ours at 85.1 / 89.8, and every line of the page carried the -11.9.</p>
 *
 * @since 17.1.0
 */
public class PageBreakInSdtTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static WordprocessingMLPackage pkg(String bodyContent) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + bodyContent + "</w:body></w:document>"));
		return pkg;
	}

	private static String sdt(String inner) {
		return "<w:sdt><w:sdtPr><w:id w:val=\"1\"/></w:sdtPr><w:sdtContent>" + inner
				+ "</w:sdtContent></w:sdt>";
	}

	private static List<Object> sdtContent(WordprocessingMLPackage pkg) {
		Object o = XmlUtils.unwrap(
				pkg.getMainDocumentPart().getJaxbElement().getBody().getContent().get(0));
		assertTrue("expected the w:sdt, got " + o.getClass().getName(), o instanceof SdtBlock);
		return ((SdtBlock) o).getSdtContent().getContent();
	}

	private static boolean holdsPageBreak(P p) {
		for (Object o : p.getContent()) {
			if (!(o instanceof R)) continue;
			for (Object c : ((R) o).getContent()) {
				if (c instanceof Br && org.docx4j.wml.STBrType.PAGE.equals(((Br) c).getType())) {
					return true;
				}
			}
		}
		return false;
	}

	/** A break which is the paragraph's first content becomes the paragraph's own
	 *  {@code w:pageBreakBefore}, inside a content control as at body level. */
	@Test
	public void leadingBreakInSdtBecomesPageBreakBefore() throws Exception {

		WordprocessingMLPackage pkg = pkg(sdt(
				"<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>"
				+ "<w:r><w:br w:type=\"page\"/></w:r>"
				+ "<w:r><w:t>Copyrights, Trademarks and Disclaimers</w:t></w:r></w:p>"));

		PageBreak.process(pkg);

		List<Object> content = sdtContent(pkg);
		assertEquals("the paragraph should not have been split", 1, content.size());
		P p = (P) content.get(0);
		assertFalse("the w:br should have gone", holdsPageBreak(p));
		assertNotNull("no w:pPr", p.getPPr());
		assertNotNull("the break should have become w:pageBreakBefore", p.getPPr().getPageBreakBefore());
		assertTrue(p.getPPr().getPageBreakBefore().isVal());
	}

	/** And a break which follows content still splits the paragraph in two there. */
	@Test
	public void breakAfterContentInSdtSplitsTheParagraph() throws Exception {

		WordprocessingMLPackage pkg = pkg(sdt(
				"<w:p><w:r><w:t>before the break</w:t></w:r>"
				+ "<w:r><w:br w:type=\"page\"/></w:r>"
				+ "<w:r><w:t>after the break</w:t></w:r></w:p>"));

		PageBreak.process(pkg);

		List<Object> content = sdtContent(pkg);
		assertEquals("the paragraph should have been split at the break", 2, content.size());
		P second = (P) content.get(1);
		assertNotNull("no w:pPr on the continuation", second.getPPr());
		assertNotNull("the continuation carries the break",
				second.getPPr().getPageBreakBefore());
	}

	/** The body-level behaviour is unchanged. */
	@Test
	public void bodyLevelIsUnchanged() throws Exception {

		WordprocessingMLPackage pkg = pkg(
				"<w:p><w:r><w:br w:type=\"page\"/></w:r><w:r><w:t>text</w:t></w:r></w:p>");

		PageBreak.process(pkg);

		P p = (P) pkg.getMainDocumentPart().getJaxbElement().getBody().getContent().get(0);
		assertFalse(holdsPageBreak(p));
		assertNotNull(p.getPPr().getPageBreakBefore());
	}
}
