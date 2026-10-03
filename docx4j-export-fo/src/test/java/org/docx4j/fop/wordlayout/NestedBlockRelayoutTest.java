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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A block nested in an inline (how docx4j writes a {@code w:br} inside a run) whose
 * paragraph is line-broken a second time: after an {@code fo:float}, or when the page
 * that follows has another width.  The nested block's elements are kept between the
 * passes and were wrapped again on the second, so adding their areas re-entered the
 * paragraph's block: the lines after the nested block were lost and FOP threw in
 * {@code TraitSetter.setVisibility} (Enterprise CR-001 §6.6 item 36; the same defect in
 * FOP's own line manager is the docx4j FO renderer's fop/CR-011).
 *
 * @since 17.3.1
 */
public class NestedBlockRelayoutTest {

	private static final String NS = "http://www.w3.org/1999/XSL/Format";

	private static final String NESTED = "<fo:block>Before <fo:inline>the run<fo:block linefeed-treatment=\"preserve\">&#x2028;</fo:block>"
			+ "after the break and on.</fo:inline> Closing words.</fo:block><fo:block>Last block.</fo:block>";

	/** The paragraph after one a float sits beside: laid out again on the float's pass. */
	private static final String AFTER_FLOAT = "<fo:root xmlns:fo=\"" + NS + "\"><fo:layout-master-set>"
			+ "<fo:simple-page-master master-name=\"page\" page-width=\"595pt\" page-height=\"842pt\" margin=\"72pt\"><fo:region-body/></fo:simple-page-master>"
			+ "</fo:layout-master-set><fo:page-sequence master-reference=\"page\"><fo:flow flow-name=\"xsl-region-body\">"
			+ "<fo:block><fo:float float=\"start\"><fo:block-container width=\"100pt\" height=\"60pt\"><fo:block>Float</fo:block></fo:block-container></fo:float>"
			+ "The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block>"
			+ NESTED + "</fo:flow></fo:page-sequence></fo:root>";

	/** The paragraph falls across a page break onto a narrower page: laid out again for its width. */
	private static final String IPD_CHANGE = "<fo:root xmlns:fo=\"" + NS + "\"><fo:layout-master-set>"
			+ "<fo:simple-page-master master-name=\"first\" page-width=\"595pt\" page-height=\"842pt\" margin=\"72pt\"><fo:region-body/></fo:simple-page-master>"
			+ "<fo:simple-page-master master-name=\"rest\" page-width=\"595pt\" page-height=\"842pt\" margin=\"72pt\" margin-left=\"172pt\"><fo:region-body/></fo:simple-page-master>"
			+ "<fo:page-sequence-master master-name=\"pages\"><fo:single-page-master-reference master-reference=\"first\"/>"
			+ "<fo:repeatable-page-master-reference master-reference=\"rest\"/></fo:page-sequence-master>"
			+ "</fo:layout-master-set><fo:page-sequence master-reference=\"pages\"><fo:flow flow-name=\"xsl-region-body\">"
			+ "BLOCKS"
			+ "<fo:block>Before <fo:inline>the run<fo:block linefeed-treatment=\"preserve\">&#x2028;</fo:block>"
			+ "after the break and on, and on, and on to the end of the paragraph.</fo:inline> Closing words.</fo:block>"
			+ "<fo:block>Last block.</fo:block></fo:flow></fo:page-sequence></fo:root>";

	private static List<String> lines(String fo) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
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

	private static boolean hasLineStarting(List<String> lines, String start) {
		for (String l : lines) {
			if (l.startsWith(start)) return true;
		}
		return false;
	}

	@Test
	public void afterAFloatTheLinesAfterTheNestedBlockAreKept() throws Exception {
		List<String> lines = lines(AFTER_FLOAT);
		assertTrue(lines.toString(), hasLineStarting(lines, "Before the run"));
		assertTrue(lines.toString(), hasLineStarting(lines, "after the break and on. Closing words."));
		assertTrue(lines.toString(), lines.contains("Last block."));
	}

	@Test
	public void onANarrowerPageTheLinesAfterTheNestedBlockAreKept() throws Exception {
		List<String> lines = lines(IPD_CHANGE.replace("BLOCKS", BLOCKS));
		assertTrue(lines.toString(), hasLineStarting(lines, "after the break and on"));
		assertTrue(lines.toString(), lines.contains("Last block."));
	}

	private static final String BLOCKS = "<fo:block>0. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>1. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>2. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>3. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>4. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>5. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>6. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>7. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>8. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>9. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>10. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>11. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>12. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>13. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>14. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>15. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>16. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>17. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>18. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>19. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>20. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>21. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>22. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>23. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>24. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>25. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>26. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>27. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>28. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>29. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>30. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>31. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>32. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>33. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>34. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>35. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>36. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>37. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>38. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>39. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>40. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>41. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>42. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block><fo:block>43. The quick brown fox jumps over the lazy dog, and then it runs back across the field again before the evening comes.</fo:block>";
}
