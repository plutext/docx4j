package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A tab in the middle of a line becomes an fo:leader of no length marked
 * docx4j:tab, and its paragraph's block carries the tab stops the line manager
 * places it against (docx4j:tabs, docx4j:tab-default, docx4j:tab-ind).  Both FO
 * pathways.  What the line manager then does with them is
 * org.docx4j.fop.wordlayout.TabStopTest.
 *
 * @since 17.0.5
 */
public class TabStopHintsTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String NS = "http://docx4j.org/fop/word-layout";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static org.w3c.dom.Document fo(String body, String defaultTabStop, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		if (defaultTabStop!=null) {
			DocumentSettingsPart settings = new DocumentSettingsPart();
			settings.setJaxbElement((CTSettings)XmlUtils.unmarshalString(
					"<w:settings " + W + "><w:defaultTabStop w:val=\"" + defaultTabStop + "\"/>"
					+ "<w:decimalSymbol w:val=\",\"/></w:settings>", org.docx4j.jaxb.Context.jc, CTSettings.class));
			pkg.getMainDocumentPart().addTargetPart(settings);
		}
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element firstBlockWithTabs(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element b = (Element) nl.item(i);
			if (b.hasAttributeNS(NS, "tabs")) return b;
		}
		return null;
	}

	private static Element firstTabLeader(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "leader");
		for (int i = 0; i < nl.getLength(); i++) {
			Element l = (Element) nl.item(i);
			if (l.hasAttributeNS(NS, "tab")) return l;
		}
		return null;
	}

	@Test
	public void aTabAfterTextIsALeaderAndItsBlockCarriesTheStops() throws Exception {
		String body = "<w:p><w:pPr><w:tabs>"
				+ "<w:tab w:val=\"left\" w:pos=\"1000\"/>"
				+ "<w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"4000\"/>"
				+ "<w:tab w:val=\"clear\" w:pos=\"5000\"/>"
				+ "</w:tabs><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>"
				+ "<w:r><w:t>a</w:t><w:tab/><w:t>b</w:t><w:tab/><w:t>c</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(body, "567", flag);
			Element block = firstBlockWithTabs(doc);
			assertTrue("no tab stops on the block", block!=null);
			assertEquals("1000:left:none;4000:right:dot;5000:clear:none",
					block.getAttributeNS(NS, "tabs"));
			assertEquals("567", block.getAttributeNS(NS, "tab-default"));
			assertEquals("720:-360:,", block.getAttributeNS(NS, "tab-ind"));

			Element leader = firstTabLeader(doc);
			assertTrue("no tab leader", leader!=null);
			assertEquals("0pt", leader.getAttribute("leader-length"));
			// which stop a tab reaches is decided at layout time, so every tab of the
			// paragraph gets the paragraph's own leader and the line manager keeps it
			// only on the tabs whose resolved stop draws one (@since 17.0.6; until then
			// the n-th tab took the n-th stop's leader, which lost the dots of a TOC
			// entry whose tab count differs from its stop count)
			NodeList leaders = doc.getElementsByTagNameNS(FO, "leader");
			assertEquals(2, leaders.getLength());
			assertEquals("dots", ((Element) leaders.item(0)).getAttribute("leader-pattern"));
			assertEquals("dots", ((Element) leaders.item(1)).getAttribute("leader-pattern"));
			assertEquals("reference-area", leader.getAttribute("leader-alignment"));
		}
	}

	@Test
	public void aParagraphWithoutTabsCarriesNoStops() throws Exception {
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo("<w:p><w:r><w:t>plain</w:t></w:r></w:p>", null, flag);
			assertTrue(firstBlockWithTabs(doc)==null);
		}
	}

	@Test
	public void theDefaultTabStopIs720TwipsWhenTheDocumentDoesNotSayOtherwise() throws Exception {
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(
					"<w:p><w:r><w:t>a</w:t><w:tab/><w:t>b</w:t></w:r></w:p>", null, flag);
			Element block = firstBlockWithTabs(doc);
			assertTrue("no tab stops on the block", block!=null);
			assertEquals("", block.getAttributeNS(NS, "tabs"));
			assertEquals("720", block.getAttributeNS(NS, "tab-default"));
			assertEquals("0:0:.", block.getAttributeNS(NS, "tab-ind"));
		}
	}

	/** With the Word layout managers off there is nothing to place a tab during
	 *  layout, so a tab after text keeps the old stand-in of three no-break spaces. */
	@Test
	public void withoutTheLayoutManagersATabAfterTextIsStillThreeSpaces() throws Exception {
		org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.wordLayout", Boolean.FALSE);
		try {
			for (int flag : FLAGS) {
				org.w3c.dom.Document doc = fo(
						"<w:p><w:r><w:t>a</w:t><w:tab/><w:t>b</w:t></w:r></w:p>", null, flag);
				assertTrue(firstTabLeader(doc)==null);
				assertTrue(firstBlockWithTabs(doc)==null);
				assertEquals(0, doc.getElementsByTagNameNS(FO, "leader").getLength());
				boolean spaces = false;
				NodeList nl = doc.getElementsByTagNameNS(FO, "inline");
				for (int i = 0; i < nl.getLength(); i++) {
					if ("   ".equals(nl.item(i).getTextContent())) spaces = true;
				}
				assertTrue("no three-space stand-in", spaces);
			}
		} finally {
			org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.wordLayout", Boolean.TRUE);
		}
	}

	/** A table of contents keeps the stretching dot leader it has always had: its
	 *  stop is the right margin and FOP's text-align-last="justify" puts the page
	 *  number there, absorbing what an unresolved page-number citation loses. */
	@Test
	public void aTableOfContentsKeepsItsStretchingLeader() throws Exception {
		String body = "<w:p><w:pPr><w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"9016\"/></w:tabs></w:pPr>"
				+ "<w:r><w:t>Chapter one</w:t><w:tab/><w:t>7</w:t></w:r></w:p>";
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(body, null, flag);
			assertTrue("a TOC tab must not become a laid-out tab", firstTabLeader(doc)==null);
			assertTrue("no stops belong on a TOC block", firstBlockWithTabs(doc)==null);
			Element leader = (Element) doc.getElementsByTagNameNS(FO, "leader").item(0);
			assertEquals("dots", leader.getAttribute("leader-pattern"));
			assertEquals("100%", leader.getAttribute("leader-length.maximum"));
		}
	}
}
