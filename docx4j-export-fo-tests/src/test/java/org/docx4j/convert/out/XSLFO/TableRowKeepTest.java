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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A row whose first paragraph keeps with the next keeps with the next row (TableWriter, since
 * 17.2.0), and where it holds a nested table it is kept whole as well, as {@code w:cantSplit}
 * keeps a row: Word moved such a row to the next page on the table-nested-rowsplit-d probe
 * (CR-001 batch 53, corpus document 12301) where docx4j split it.  Only where the keep is the
 * row's first paragraph's: a cell opening with a nested table is left alone.
 *
 * @since 17.3.1
 */
public class TableRowKeepTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String p(String text, boolean keepNext) {
		return "<w:p>" + (keepNext ? "<w:pPr><w:keepNext/></w:pPr>" : "") + "<w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String row(String cellContent) {
		return "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"9000\" w:type=\"dxa\"/></w:tcPr>" + cellContent + "</w:tc></w:tr>";
	}

	private static String table(String rows) {
		return "<w:tbl><w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"9000\"/></w:tblGrid>"
				+ rows + "</w:tbl>";
	}

	/** Five rows: plain; keepNext; keepNext holding a nested table; a nested table, no keep;
	 *  a cell opening with a nested table whose heading keeps (a layout table's section). */
	private static org.w3c.dom.Document fo(int flags) throws Exception {
		String nested = table(row(p("nested 1", false)) + row(p("nested 2", false)));
		String section = table(row(p("section heading", true)) + row(p("section body", false)));
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ table(row(p("plain", false))
						+ row(p("keeps", true))
						+ row(p("keeps, then a nested table", true) + nested + "<w:p/>")
						+ row(p("a nested table, no keep", false) + nested + "<w:p/>")
						+ row(section + "<w:p/>"))
				+ p("after", false)
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** The outer table's rows, in order. */
	private static List<Element> outerRows(org.w3c.dom.Document doc) {
		Element outer = (Element) doc.getElementsByTagNameNS(FO, "table").item(0);
		List<Element> rows = new ArrayList<Element>();
		NodeList all = outer.getElementsByTagNameNS(FO, "table-row");
		for (int i = 0; i < all.getLength(); i++) {
			Element r = (Element) all.item(i);
			Element t = r;
			while (t != null && !"table".equals(t.getLocalName())) t = (Element) t.getParentNode();
			if (t == outer) rows.add(r);
		}
		return rows;
	}

	private void check(int flags) throws Exception {
		List<Element> rows = outerRows(fo(flags));
		assertEquals(5, rows.size());
		assertEquals("plain", "", rows.get(0).getAttribute("keep-with-next"));
		assertEquals("plain", "", rows.get(0).getAttribute("keep-together.within-page"));
		assertEquals("keeps", "always", rows.get(1).getAttribute("keep-with-next"));
		assertEquals("keeps, no nested table: not kept whole", "", rows.get(1).getAttribute("keep-together.within-page"));
		assertEquals("keeps, nested table", "always", rows.get(2).getAttribute("keep-with-next"));
		assertEquals("keeps, nested table: kept whole", "always", rows.get(2).getAttribute("keep-together.within-page"));
		assertEquals("a nested table, no keep", "", rows.get(3).getAttribute("keep-with-next"));
		assertEquals("a nested table, no keep", "", rows.get(3).getAttribute("keep-together.within-page"));
		assertEquals("a section keeps by its heading row", "always", rows.get(4).getAttribute("keep-with-next"));
		assertEquals("but is not kept whole (12301)", "", rows.get(4).getAttribute("keep-together.within-page"));
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
