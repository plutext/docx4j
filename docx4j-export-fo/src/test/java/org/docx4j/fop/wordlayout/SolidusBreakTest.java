package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Word does not break a line after a solidus (measured on the Getting Started
 * guide, CR-001 §6.10), where UAX #14 allows one; the line manager suppresses
 * those break opportunities.  Courier 12pt on a 200pt line: 27 characters.
 */
public class SolidusBreakTest {

	private static String fo(String text) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"200pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\">" + text + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	private static List<String> lines(String fo, boolean wordLayout) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		if (wordLayout) b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		FopFactory factory = b.build();
		FOUserAgent ua = factory.newFOUserAgent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))), new SAXResult(fop.getDefaultHandler()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		List<String> lines = new ArrayList<>();
		NodeList las = doc.getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			StringBuilder sb = new StringBuilder();
			collectWords((Element) las.item(i), sb);
			lines.add(sb.toString().trim().replaceAll(" +", " "));
		}
		return lines;
	}

	private static void collectWords(Element el, StringBuilder sb) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			if ("word".equals(c.getLocalName())) sb.append(c.getTextContent());
			else if ("space".equals(c.getLocalName())) sb.append(' ');
			else collectWords(c, sb);
		}
	}

	// "Most docx files use" is 19 characters; "http://schemas.example.org/main" 31: it
	// cannot share the first line, and cannot be split at a solidus
	private static final String URL = "Most docx files use http://schemas.example.org/main namespace";

	@Test
	public void noBreakAfterSolidus() throws Exception {
		List<String> got = lines(fo(URL), true);
		assertEquals("Most docx files use", got.get(0));
		assertEquals("http://schemas.example.org/main", got.get(1));
		for (String l : got) assertFalse(l, l.endsWith("/"));
	}

	@Test
	public void fopItselfBreaksThere() throws Exception {
		List<String> got = lines(fo(URL), false);
		boolean broken = false;
		for (String l : got) broken |= l.endsWith("/");
		assertEquals("FOP (UAX #14) breaks after a solidus", true, broken);
	}

	@Test
	public void slashJoinedWordsStayTogether() throws Exception {
		// "aaaa bbbb cccc dddd eeee" = 24 chars; "OpenOffice/jodconverter" (23) must go whole to line 2
		List<String> got = lines(fo("aaaa bbbb cccc dddd eeee OpenOffice/jodconverter can"), true);
		assertEquals("aaaa bbbb cccc dddd eeee", got.get(0));
		assertEquals("OpenOffice/jodconverter can", got.get(1));
	}
}
