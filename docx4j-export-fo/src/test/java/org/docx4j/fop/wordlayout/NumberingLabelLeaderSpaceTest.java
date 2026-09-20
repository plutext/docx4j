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
package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.w3c.dom.NodeList;

/**
 * The blank between a numbering label and its leader run is a <b>space character</b>, not
 * a jump of the pen.
 *
 * <p>Word's own PDF of the {@code numbering-leader-kinds} golden writes three text objects
 * for each of its five items - the label, a space, then the run - and the space is in the
 * <b>leader's</b> face at the label's size, standing at the label's end while the run opens
 * on Word's 1/300-inch grid:</p>
 *
 * <pre>
 * /F1 11.04 Tf 1 0 0 1  90.048 705.55 Tm [(1.)] TJ                  LiberationSerif
 * /F3 11.04 Tf 1 0 0 1  98.208 705.55 Tm [( )] TJ                   ArialMT
 * /F3 11.04 Tf 1 0 0 1  99.888 705.55 Tm 0.0509 Tc[(...............)] TJ
 * </pre>
 *
 * <p>docx4j wrote the label and the run and nothing between them, so a PDF text extractor
 * read {@code 1.------------} where Word's reads {@code 1. ------------}.  Since 17.2.0 the
 * phase is written as the space Word writes ({@code WordLayoutCustomizer.tabSpaces}, the
 * same switch the paragraph tab's blank takes).</p>
 *
 * <p>Courier 12pt here, so every position is computable: the glyph advance is 7.2pt, which
 * is 30 cells of Word's grid exactly.  The label {@code 1.} starts at the list block's 18pt
 * start-indent and ends at 32.4pt; the next multiple of 7.2 is 36.0; so the phase - and the
 * space - is <b>3.6pt</b>.</p>
 *
 * @since 17.2.0 (CR-001 batch 49 item 1)
 */
public class NumberingLabelLeaderSpaceTest {

	private static final String NS = WordLayoutElementMapping.URI;

	/** The label block as XsltFOFunctions emits it for a level whose numbering tab reaches
	 *  a stop with a w:leader: the number, then an fo:leader repeating the kind's own
	 *  character on the reference area's grid. */
	private static String fo() {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:docx4j=\"" + NS + "\">"
				+ "<fo:layout-master-set>"
				+ "<fo:simple-page-master master-name=\"m\" page-width=\"400pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:list-block provisional-distance-between-starts=\"126pt\" start-indent=\"18pt\""
				+ " font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\">"
				+ "<fo:list-item><fo:list-item-label end-indent=\"label-end()\">"
				+ "<fo:block docx4j:label-suffix=\"tab\">1."
				+ "<fo:leader docx4j:toc-leader=\"dot\" font-family=\"Courier\" font-size=\"12pt\""
				+ " leader-alignment=\"reference-area\" leader-length=\"45pt\""
				+ " leader-pattern=\"use-content\">.</fo:leader>"
				+ "</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body start-indent=\"body-start()\">"
				+ "<fo:block>the item's text</fo:block>"
				+ "</fo:list-item-body></fo:list-item>"
				+ "</fo:list-block></fo:flow></fo:page-sequence></fo:root>";
	}

	/** FOP's area tree for this FO, with the Word layout managers in place. */
	private static Document area(String fo) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		FopFactory factory = b.build();
		FOUserAgent ua = factory.newFOUserAgent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))),
				new SAXResult(fop.getDefaultHandler()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
	}

	/** The leader wrapper on the label's line: the innermost inlineparent that holds
	 *  another inlineparent, which is LBP.PhasedLeaderArea around the filled area. */
	private static Element leaderWrapper(Document doc) {
		NodeList parents = doc.getElementsByTagName("inlineparent");
		for (int i = 0; i < parents.getLength(); i++) {
			Element parent = (Element) parents.item(i);
			List<Element> kids = children(parent);
			if (kids.size() == 2 && "inlineparent".equals(kids.get(1).getLocalName())) {
				return parent;
			}
		}
		return null;
	}

	private static List<Element> children(Element el) {
		List<Element> out = new ArrayList<Element>();
		NodeList nl = el.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) out.add((Element) nl.item(i));
		}
		return out;
	}

	private static double pt(Element el, String attr) {
		String v = el.getAttribute(attr);
		assertTrue(attr + " missing", v != null && v.length() > 0);
		return Integer.parseInt(v.trim()) / 1000.0;
	}

	private static Document withTabSpaces(String value) throws Exception {
		String was = System.getProperty(WordLayoutCustomizer.TAB_SPACES);
		if (value == null) {
			System.clearProperty(WordLayoutCustomizer.TAB_SPACES);
		} else {
			System.setProperty(WordLayoutCustomizer.TAB_SPACES, value);
		}
		try {
			return area(fo());
		} finally {
			if (was == null) {
				System.clearProperty(WordLayoutCustomizer.TAB_SPACES);
			} else {
				System.setProperty(WordLayoutCustomizer.TAB_SPACES, was);
			}
		}
	}

	/** On by default: the phase is a space character of its own width, and the run keeps
	 *  the place and the width the grid gave it. */
	@Test
	public void thePhaseIsWrittenAsASpaceCharacter() throws Exception {

		Element wrapper = leaderWrapper(withTabSpaces("true"));
		assertNotNull("no phased leader on the label's line", wrapper);
		List<Element> kids = children(wrapper);

		Element blank = kids.get(0);
		assertEquals("the phase is not a text area", "text", blank.getLocalName());
		assertEquals("the phase is not one space character", " ", blank.getTextContent());
		assertEquals("the label ends at 32.4pt and the next cell of the 7.2pt grid is 36.0",
				3.6, pt(blank, "ipd"), 0.001);

		// and it sits on the run's own line, not on a baseline of its own: the space is a
		// sibling of the filled area, so it carries the whole chain's offset
		Element run = kids.get(1);
		assertEquals("the space is off the leader's line",
				pt(run, "offset") + baselineOf(run), pt(blank, "offset") + pt(blank, "baseline"),
				0.001);
	}

	/** The block-progression offset the chain down to the first text area accumulates,
	 *  plus that area's baseline: where a renderer draws it. */
	private static double baselineOf(Element el) {
		for (Element child : children(el)) {
			if ("text".equals(child.getLocalName())) {
				return pt(child, "offset") + pt(child, "baseline");
			}
			double found = baselineOf(child);
			if (found != 0) return found;
		}
		return 0;
	}

	/** Off: the phase is the plain blank it was before 17.2.0, and nothing is in the text
	 *  layer between the label and the run. */
	@Test
	public void theSwitchRestoresThePlainBlank() throws Exception {

		Element wrapper = leaderWrapper(withTabSpaces("false"));
		assertNotNull("no phased leader on the label's line", wrapper);
		Element blank = children(wrapper).get(0);
		assertEquals("the phase should be a plain space area", "space", blank.getLocalName());
		assertEquals("and of the same width", 3.6, pt(blank, "ipd"), 0.001);
	}
}
