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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A table-of-contents entry whose page number does not fit after its text breaks before the
 * tab, as Word's does: the leader and the number go down to a line of their own, the dots
 * running from the indent.  FOP allows no break before an {@code fo:leader}, so docx4j broke
 * the entry inside its text instead - the long token went down to join the number - or ran
 * the number past the margin with no dots at all.  Measured on corpus document 11657, whose
 * {@code toc 2} entries are {@code 4.46<tab>Constante «...type»<tab>65}: Word sets
 * "4.46 Constante «...type»" and then "...... 65".
 *
 * <p>The entries here sweep the token's length across the margin, so that whatever the
 * machine's substitute for Times New Roman, some lengths fit the token on the first line and
 * leave no room for the number.</p>
 *
 * @since 17.3.1
 */
public class TocBreakBeforeTabTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String PROPERTY = "docx4j.convert.out.fo.wordLayout.tocBreakBeforeTab";

	/** the corpus shape: a hanging indent, and one right dot-leader stop at the margin */
	private static final String PPR =
			"<w:pPr><w:tabs><w:tab w:val=\"right\" w:leader=\"dot\" w:pos=\"9026\"/></w:tabs>"
			+ "<w:spacing w:before=\"0\" w:after=\"0\"/><w:ind w:left=\"624\" w:hanging=\"624\"/></w:pPr>";

	private static final String RPR = "<w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
			+ "<w:sz w:val=\"24\"/></w:rPr>";

	private static String run(String text) {
		return "<w:r>" + RPR + "<w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	private static final String TAB = "<w:r>" + RPR + "<w:tab/></w:r>";

	private static final String PATTERN = "comptabiliteTiersoperationTiersparamTypeFacturationImputationtype";

	private static WordprocessingMLPackage pkg() throws Exception {
		StringBuilder body = new StringBuilder();
		for (int n = 60; n < 110; n += 2) {
			StringBuilder token = new StringBuilder();
			while (token.length() < n) token.append(PATTERN);
			body.append("<w:p>").append(PPR).append(run("4." + n)).append(TAB)
					.append(run("Constante «" + token.substring(0, n) + "»")).append(TAB)
					.append(run("65")).append("</w:p>");
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	/** Each lineArea's text, its words joined (a leader's dots are words too). */
	private static List<String> lines(org.w3c.dom.Document areaTree) {
		List<String> out = new ArrayList<String>();
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		for (int i = 0; i < lines.getLength(); i++) {
			StringBuilder sb = new StringBuilder();
			words((Element) lines.item(i), sb);
			out.add(sb.toString().trim());
		}
		return out;
	}

	private static void words(Node n, StringBuilder sb) {
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element && ("word".equals(c.getNodeName()) || "space".equals(c.getNodeName()))) {
				sb.append(c.getTextContent());
			} else {
				words(c, sb);
			}
		}
	}

	/** a line holding only the entry's leader and its number */
	private static boolean leaderAndNumberOnly(String line) {
		return line.matches("\\.{3,}\\s*65");
	}

	private List<String> render(int flags, boolean on) throws Exception {
		String was = Docx4jProperties.getProperty(PROPERTY);
		Docx4jProperties.setProperty(PROPERTY, Boolean.toString(on));
		try {
			return lines(areaTree(pkg(), flags));
		} finally {
			if (was == null) Docx4jProperties.getProperties().remove(PROPERTY);
			else Docx4jProperties.setProperty(PROPERTY, was);
		}
	}

	private void check(int flags) throws Exception {
		List<String> lines = render(flags, true);
		boolean found = false;
		for (int i = 1; i < lines.size(); i++) {
			if (leaderAndNumberOnly(lines.get(i))) {
				found = true;
				assertTrue("the line before the number's own line ends in the entry's text: " + lines,
						lines.get(i - 1).endsWith("»"));
			}
		}
		assertTrue("some entry's number goes down to a line of its own, behind its dots: " + lines, found);

		for (String line : render(flags, false)) {
			assertFalse("without the break the leader never starts a line: " + line, leaderAndNumberOnly(line));
		}
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
