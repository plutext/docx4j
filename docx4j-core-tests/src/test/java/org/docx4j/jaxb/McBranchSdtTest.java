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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSdtRow;
import org.docx4j.wml.Document;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * CR-051 section 11.3: a {@code w:sdt} directly inside {@code mc:Choice} or {@code mc:Fallback}.
 * JAXB's lax any-element binding gave it one class for every level ({@code CTSdtRow}), which
 * dropped or failed everything but a row control; now it arrives as a DOM element in a private
 * namespace and is bound by its container's kind, on every unmarshal route, in both modes, and
 * the private namespace never reaches output.
 */
public class McBranchSdtTest {

	/** The private namespace of the way in, as a literal: no output may hold it. */
	private static final String PRIVATE = "urn:docx4j:mc-branch-sdt";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String sdt(String props, String content) {
		return "<w:sdt><w:sdtPr><w:id w:val=\"1\"/>" + props + "</w:sdtPr><w:sdtContent>" + content + "</w:sdtContent></w:sdt>";
	}

	private static String ac(String choice, String fallback) {
		return "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + choice + "</mc:Choice><mc:Fallback>" + fallback + "</mc:Fallback></mc:AlternateContent>";
	}

	private static final String TBL_PROPS = "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>";

	/**
	 * Block controls in the body's element, with a tag and with a data binding; a run control in a
	 * paragraph-level element; a row control in a table's element; a cell control in a row's element.
	 */
	private static String body(boolean rejected) {
		String pPr = rejected ? "<w:pPr><w14:textFill/></w:pPr>" : ""; // w14 in w:pPr: StAX rejects the part
		return "<w:p>" + pPr + "<w:r><w:t>before</w:t></w:r></w:p>"
				+ ac(sdt("<w:tag w:val=\"t\"/>", p("BLOCK_TAG_C")), sdt("<w:tag w:val=\"t\"/>", p("BLOCK_TAG_F")))
				+ ac(sdt("<w:dataBinding w:xpath=\"/root/name\" w:storeItemID=\"{x}\"/><w:text/>", p("BLOCK_BIND_C")),
						sdt("<w:dataBinding w:xpath=\"/root/name\" w:storeItemID=\"{x}\"/><w:text/>", p("BLOCK_BIND_F")))
				+ "<w:p><w:r><w:t>para </w:t></w:r>" + ac(sdt("", "<w:r><w:t>RUN_C</w:t></w:r>"), sdt("", "<w:r><w:t>RUN_F</w:t></w:r>")) + "</w:p>"
				+ "<w:tbl>" + TBL_PROPS + ac(sdt("", "<w:tr><w:tc>" + p("ROW_C") + "</w:tc></w:tr>"), sdt("", "<w:tr><w:tc>" + p("ROW_F") + "</w:tc></w:tr>")) + "</w:tbl>"
				+ "<w:tbl>" + TBL_PROPS + "<w:tr>" + ac(sdt("", "<w:tc>" + p("CELL_C") + "</w:tc>"), sdt("", "<w:tc>" + p("CELL_F") + "</w:tc>")) + "</w:tr></w:tbl>"
				+ p("after");
	}

	private static final String[] CHOICE = { "BLOCK_TAG_C", "BLOCK_BIND_C", "RUN_C", "ROW_C", "CELL_C" };
	private static final String[] FALLBACK = { "BLOCK_TAG_F", "BLOCK_BIND_F", "RUN_F", "ROW_F", "CELL_F" };

	private String propertyBefore;

	@Before
	public void remember() {
		propertyBefore = Docx4jProperties.getProperty("docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart");
	}

	@After
	public void restore() {
		Docx4jProperties.setProperty("docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart", propertyBefore == null ? "false" : propertyBefore);
	}

	private static List<SdtElement> controls(Object root) {
		final List<SdtElement> out = new ArrayList<SdtElement>();
		new TraversalUtil(root, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof SdtElement) out.add((SdtElement) o);
				return null;
			}
		}, McMode.ALL);
		return out;
	}

	private static String classes(List<SdtElement> controls) {
		StringBuilder sb = new StringBuilder();
		for (SdtElement s : controls) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(s.getClass().getSimpleName());
		}
		return sb.toString();
	}

	private static void assertAllThere(String where, String xml, String... tokens) {
		for (String t : tokens) assertTrue(where + ": " + t + " missing from " + xml, xml.contains(t));
	}

	/** Every control survives with its class, in every part of the package as it stands, and the saved package writes w:sdt only. */
	private static void check(String where, WordprocessingMLPackage pkg, McBlockLevel mode) throws Exception {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		String xml = XmlUtils.marshaltoString(mdp.getJaxbElement(), true, false);
		assertFalse(where + ": private namespace in the tree: " + xml, xml.contains(PRIVATE));
		assertAllThere(where, xml, CHOICE);
		if (mode == McBlockLevel.PRESERVE) {
			// block and paragraph level kept whole; row and cell level still resolved until CR-051 phase 2
			assertAllThere(where, xml, "BLOCK_TAG_F", "BLOCK_BIND_F", "RUN_F");
			assertFalse(where + ": " + xml, xml.contains("ROW_F") || xml.contains("CELL_F"));
			assertEquals(where + ": " + xml, "SdtBlock SdtBlock SdtBlock SdtBlock SdtRun SdtRun CTSdtRow CTSdtCell", classes(controls(mdp.getJaxbElement())));
		} else {
			// block, row and cell level resolved to the w14 Choice; the paragraph-level element kept whole
			assertFalse(where + ": " + xml, xml.contains("BLOCK_TAG_F") || xml.contains("ROW_F") || xml.contains("CELL_F"));
			assertTrue(where + ": " + xml, xml.contains("RUN_F"));
			assertEquals(where + ": " + xml, "SdtBlock SdtBlock SdtRun SdtRun CTSdtRow CTSdtCell", classes(controls(mdp.getJaxbElement())));
		}
		String text = Texts.plainText(pkg, new TextOptions());
		assertAllThere(where + " text", text, CHOICE);
		for (String t : FALLBACK) assertFalse(where + " text: " + text, text.contains(t));
		assertFalse(where + ": getXML", mdp.getXML().contains(PRIVATE));
		// the save writes w:sdt, in the w namespace, and the private namespace nowhere
		byte[] saved = McProbeDocx.save(pkg);
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(saved))) {
			for (ZipEntry e; (e = zip.getNextEntry()) != null; ) {
				byte[] buf = new byte[8192];
				StringBuilder sb = new StringBuilder();
				for (int n; (n = zip.read(buf)) > 0; ) sb.append(new String(buf, 0, n, "UTF-8"));
				assertFalse(where + ": " + e.getName() + " holds the private namespace", sb.toString().contains(PRIVATE));
			}
		}
		String document = McProbeDocx.entry(saved, "word/document.xml");
		assertTrue(where + ": " + document, document.contains("<w:sdt>") || document.contains("<w:sdt "));
		assertFalse(where + ": " + document, document.contains(":sdt>") && !document.contains("w:sdt>"));
		WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(saved), mode);
		again.getMainDocumentPart().getContents();
		String reloaded = XmlUtils.marshaltoString(again.getMainDocumentPart().getJaxbElement(), true, false);
		assertAllThere(where + " reloaded", reloaded, CHOICE);
	}

	@Test
	public void staxPath() throws Exception {
		for (McBlockLevel mode : McBlockLevel.values()) {
			WordprocessingMLPackage pkg = McProbeDocx.body(body(false)).load(mode);
			pkg.getMainDocumentPart().getContents();
			check("stax " + mode, pkg, mode);
		}
	}

	/** A part StAX rejects (w14 in w:pPr) goes through the preprocessor and the binder. */
	@Test
	public void preprocessorPath() throws Exception {
		for (McBlockLevel mode : McBlockLevel.values()) {
			WordprocessingMLPackage pkg = McProbeDocx.body(body(true)).load(mode);
			pkg.getMainDocumentPart().getContents();
			check("preprocessor " + mode, pkg, mode);
		}
	}

	/** The eager binder parses a DOM and unmarshals it through a binder. */
	@Test
	public void binderPath() throws Exception {
		Docx4jProperties.setProperty("docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart", "true");
		for (McBlockLevel mode : McBlockLevel.values()) {
			WordprocessingMLPackage pkg = McProbeDocx.body(body(false)).load(mode);
			pkg.getMainDocumentPart().getContents();
			check("binder " + mode, pkg, mode);
			// an XPath over the binder finds the controls the splice promoted (ordinary content), not
			// those in a kept branch, which have no DOM association: word mode promotes block, row and
			// cell level (4), preserve mode row and cell level only (2)
			assertEquals(mode == McBlockLevel.PRESERVE ? 2 : 4, pkg.getMainDocumentPart().getJAXBNodesViaXPath("//w:sdt", false).size());
		}
	}

	/** Content unmarshalled outside a part: every control typed, the elements whole. */
	@Test
	public void xmlUtilsUnmarshalString() throws Exception {
		Document doc = (Document) XmlUtils.unmarshalString("<w:document " + McProbeDocx.NS + "><w:body>" + body(false) + "</w:body></w:document>");
		String xml = XmlUtils.marshaltoString(doc, true, false);
		assertFalse(xml, xml.contains(PRIVATE));
		assertAllThere("unmarshalString", xml, CHOICE);
		assertAllThere("unmarshalString", xml, FALLBACK);
		assertEquals(xml, "SdtBlock SdtBlock SdtBlock SdtBlock SdtRun SdtRun CTSdtRow CTSdtRow CTSdtCell CTSdtCell", classes(controls(doc)));
		Object fromStream = XmlUtils.unmarshal(new ByteArrayInputStream(("<w:document " + McProbeDocx.NS + "><w:body>" + body(false) + "</w:body></w:document>").getBytes("UTF-8")));
		assertEquals("SdtBlock SdtBlock SdtBlock SdtBlock SdtRun SdtRun CTSdtRow CTSdtRow CTSdtCell CTSdtCell", classes(controls(XmlUtils.unwrap(fromStream))));
	}

	/** A bound block control in a kept branch is bound by the default (DOM) binder, in each branch. */
	@Test
	public void domBinderBindsAControlInAKeptBranch() throws Exception {
		WordprocessingMLPackage pkg = McBlockLevelConsumersTest.withCustomXmlFromXml(p("before")
				+ ac(sdt("<w:dataBinding w:xpath=\"/root/name\" w:storeItemID=\"" + McBlockLevelConsumersTest.STORE_ITEM + "\"/><w:text/>", p("stale")),
						sdt("<w:dataBinding w:xpath=\"/root/name\" w:storeItemID=\"" + McBlockLevelConsumersTest.STORE_ITEM + "\"/><w:text/>", p("stale")))
				+ p("after"));
		pkg.getMainDocumentPart().getContents();
		new BindingHandler(pkg).applyBindings(pkg.getMainDocumentPart());
		String out = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		assertEquals(out, 2, countOf(out, "BOUND VALUE"));
		assertFalse(out, out.contains("stale"));
	}

	private static int countOf(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}
}
