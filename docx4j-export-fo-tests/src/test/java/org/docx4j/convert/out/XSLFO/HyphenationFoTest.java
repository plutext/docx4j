package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A paragraph's fo:block carries hyphenate="true" when the document asks for
 * automatic hyphenation (w:settings/w:autoHyphenation) and the paragraph does
 * not suppress it (w:pPr/w:suppressAutoHyphens), and the language FOP picks its
 * patterns by; the document's zone, consecutive limit and all-caps rule go on
 * fo:root as docx4j: attributes.  Both FO pathways.
 *
 * What the line manager then does with them is
 * org.docx4j.fop.wordlayout.HyphenationTest.
 *
 * @since 17.0.6
 */
public class HyphenationFoTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String NS = "http://docx4j.org/fop/word-layout";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	/** The default styles part's docDefaults carry w:lang en-US, which is the
	 *  paragraph's effective language. */
	private static final String PARA =
			"<w:p><w:r><w:t>responsibilities of the administration</w:t></w:r></w:p>";
	private static final String SUPPRESSED =
			"<w:p><w:pPr><w:suppressAutoHyphens/></w:pPr><w:r>"
			+ "<w:t>responsibilities of the administration</w:t></w:r></w:p>";

	/** The property is a global; another test in this JVM may have left one set. */
	@Before
	@After
	public void clearProperty() {
		Docx4jProperties.getProperties().remove("docx4j.convert.out.fo.hyphenate");
	}

	private static org.w3c.dom.Document fo(String body, String settings, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		if (settings != null) {
			DocumentSettingsPart dsp = new DocumentSettingsPart();
			dsp.setJaxbElement((CTSettings) XmlUtils.unmarshalString(
					"<w:settings " + W + ">" + settings + "</w:settings>",
					org.docx4j.jaxb.Context.jc, CTSettings.class));
			pkg.getMainDocumentPart().addTargetPart(dsp);
		}
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element firstHyphenatingBlock(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element b = (Element) nl.item(i);
			if ("true".equals(b.getAttribute("hyphenate"))) return b;
		}
		return null;
	}

	private static int hyphenatingBlocks(org.w3c.dom.Document doc) {
		int n = 0;
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			if ("true".equals(((Element) nl.item(i)).getAttribute("hyphenate"))) n++;
		}
		return n;
	}

	private static String flagName(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "xslt";
	}

	@Test
	public void withoutAutoHyphenationNothingHyphenates() throws Exception {
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(PARA, null, flags);
			assertEquals(flagName(flags), 0, hyphenatingBlocks(doc));
			assertFalse(flagName(flags),
					doc.getDocumentElement().hasAttributeNS(NS, "hyphenation-zone"));
		}
	}

	@Test
	public void autoHyphenationPutsHyphenateAndTheLanguageOnTheBlock() throws Exception {
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(PARA, "<w:autoHyphenation/>", flags);
			Element block = firstHyphenatingBlock(doc);
			assertTrue(flagName(flags) + ": no block asks for hyphenation", block != null);
			// the language FOP picks its patterns by is the paragraph's effective w:lang,
			// which docx4j writes whether or not the paragraph is hyphenated
			// (org.docx4j.model.properties.run.Lang); here it comes from docDefaults
			assertEquals(flagName(flags), "en", block.getAttribute("language"));
			assertEquals(flagName(flags), "US", block.getAttribute("country"));
		}
	}

	@Test
	public void suppressAutoHyphensExemptsTheParagraph() throws Exception {
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(PARA + SUPPRESSED, "<w:autoHyphenation/>", flags);
			assertEquals(flagName(flags) + ": only the unsuppressed paragraph hyphenates",
					1, hyphenatingBlocks(doc));
		}
	}

	@Test
	public void theDocumentSettingsGoOnFoRoot() throws Exception {
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(PARA,
					"<w:autoHyphenation/><w:hyphenationZone w:val=\"720\"/>"
					+ "<w:consecutiveHyphenLimit w:val=\"2\"/><w:doNotHyphenateCaps/>", flags);
			Element root = doc.getDocumentElement();
			assertEquals(flagName(flags), "720", root.getAttributeNS(NS, "hyphenation-zone"));
			assertEquals(flagName(flags), "2", root.getAttributeNS(NS, "hyphen-limit"));
			assertEquals(flagName(flags), "false", root.getAttributeNS(NS, "hyphenate-caps"));
		}
	}

	/** No w:hyphenationZone: Word's 0.25 inch default.  No limit and no caps rule are
	 *  written where the document sets neither. */
	@Test
	public void theZoneDefaultsTo360AndTheOptionalSettingsAreOmitted() throws Exception {
		for (int flags : FLAGS) {
			Element root = fo(PARA, "<w:autoHyphenation/>", flags).getDocumentElement();
			assertEquals(flagName(flags), "360", root.getAttributeNS(NS, "hyphenation-zone"));
			assertFalse(flagName(flags), root.hasAttributeNS(NS, "hyphen-limit"));
			assertFalse(flagName(flags), root.hasAttributeNS(NS, "hyphenate-caps"));
		}
	}

	/** docx4j.convert.out.fo.hyphenate=true hyphenates a document which does not ask
	 *  for it - except a paragraph which suppresses hyphenation. */
	@Test
	public void thePropertyForcesHyphenationOn() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.hyphenate", "true");
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(PARA + SUPPRESSED, null, flags);
			assertEquals(flagName(flags), 1, hyphenatingBlocks(doc));
			assertEquals(flagName(flags), "360",
					doc.getDocumentElement().getAttributeNS(NS, "hyphenation-zone"));
		}
	}

	/** docx4j.convert.out.fo.hyphenate=false overrides the document the other way. */
	@Test
	public void thePropertyForcesHyphenationOff() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.hyphenate", "false");
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(PARA, "<w:autoHyphenation/>", flags);
			assertEquals(flagName(flags), 0, hyphenatingBlocks(doc));
		}
	}
}
