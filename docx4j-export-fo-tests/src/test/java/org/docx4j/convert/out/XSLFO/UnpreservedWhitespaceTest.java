package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Word discards the leading and trailing white space of a {@code w:t} which does not
 * carry {@code xml:space="preserve"} - the attribute is what makes it significant, and
 * Word writes it whenever the space matters, so a {@code w:t} without it that has such
 * space came from another producer.
 *
 * <p>Measured against Word 365: a centred paragraph of {@code <w:t>Chantier
 * d'enl&#232;vement d'amiante</w:t>} then {@code <w:t>\n${caze.descriptive}</w:t>},
 * neither with {@code xml:space}, is 136.6..458.6 = 322.0pt in Word and was
 * 132.7..458.5 = 325.9 for us; {@code <w:t>WEIGHT: </w:t>} is 72.0..218.6 in Word and
 * was 72.0..220.4.  Both FO pathways, and (being in RunFontSelector) the HTML exporter
 * too.</p>
 *
 * @since 17.1.0
 */
public class UnpreservedWhitespaceTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String XML = "xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " " + XML + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/**
	 * The runs' own text: every text node whose parent is an {@code fo:inline}.  Taking
	 * the whole document's, or even the flow's, would pick up the indentation the XSLT
	 * pathway writes between elements as well.
	 */
	private static String runText(String body, int flags) throws Exception {
		StringBuilder sb = new StringBuilder();
		NodeList inlines = fo(body, flags).getElementsByTagNameNS(FO, "inline");
		for (int i = 0; i < inlines.getLength(); i++) {
			for (Node n = inlines.item(i).getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n.getNodeType() == Node.TEXT_NODE) sb.append(n.getNodeValue());
			}
		}
		return sb.toString();
	}

	/** The text of the first fo:block of the body flow. */
	private static String firstBlockText(String body, int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body, flags);
		NodeList flows = doc.getElementsByTagNameNS(FO, "flow");
		if (flows.getLength() == 0) return "";
		NodeList blocks = ((Element) flows.item(0)).getElementsByTagNameNS(FO, "block");
		return blocks.getLength() == 0 ? "" : blocks.item(0).getTextContent();
	}

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	@Test
	public void aTrailingSpaceGoesWithoutPreserve() throws Exception {
		String body = "<w:p><w:r><w:t>WEIGHT: </w:t></w:r><w:r><w:t>7</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "WEIGHT:7", runText(body, flag));
		}
	}

	@Test
	public void aLeadingSpaceGoesWithoutPreserve() throws Exception {
		String body = "<w:p><w:r><w:t>abc</w:t></w:r><w:r><w:t>\n def</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "abcdef", runText(body, flag));
		}
	}

	@Test
	public void spaceStaysWithPreserve() throws Exception {
		String body = "<w:p><w:r><w:t xml:space=\"preserve\">WEIGHT: </w:t></w:r>"
				+ "<w:r><w:t>7</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "WEIGHT: 7", runText(body, flag));
		}
	}

	/** White space <em>inside</em> the text is not leading or trailing and stays (FOP
	 *  collapses it to one space at layout, as Word does). */
	@Test
	public void internalSpaceStays() throws Exception {
		String body = "<w:p><w:r><w:t>a\n b</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), "a b", runText(body, flag).replaceAll("\\s+", " "));
		}
	}

	/**
	 * A w:t of nothing but white space keeps it: emptying one changes what the paragraph
	 * is rather than how wide it is, and the FO layer then treats the block as having no
	 * content and gives it a line of its own (measured: one corpus page opened 20.6pt
	 * below Word's).
	 */
	@Test
	public void aWhitespaceOnlyRunKeepsItsSpace() throws Exception {
		String body = "<w:p><w:r><w:t> </w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			assertEquals(flagName(flag), " ", firstBlockText(body, flag));
		}
	}
}
