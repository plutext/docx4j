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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTCustomXmlBlock;
import org.docx4j.wml.CTCustomXmlCell;
import org.docx4j.wml.CTCustomXmlRow;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSdtRow;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * CR-051 phase 1, the load-time mode (section 3.2): in {@link McBlockLevel#WORD} mode each
 * block-, row- or cell-level mc:AlternateContent is replaced on load by the branch Word reads
 * (section 4, "word mode, the splice"); in {@link McBlockLevel#PRESERVE} mode it is kept whole
 * ("preserve mode"); the load option and the package-level override win over the property
 * ("both modes").
 *
 * <p>Each probe's element has three branches: a Choice requiring {@code wpi}, which docx4j does
 * not understand, a Choice requiring {@code w14}, which it does, and a Fallback; each branch
 * holds one block (or row, or cell) with a marker text, so the content at the element's index
 * says which branch was promoted.</p>
 */
public class McBlockLevelLoadTest {

	private enum Kind { BLOCK, ROW, CELL }

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String tc(String text) {
		return "<w:tc>" + p(text) + "</w:tc>";
	}

	private static String tr(String text) {
		return "<w:tr>" + tc(text) + "</w:tr>";
	}

	private static String block(Kind kind, String text) {
		switch (kind) {
			case ROW: return tr(text);
			case CELL: return tc(text);
			default: return p(text);
		}
	}

	/** The three-branch element: a wpi Choice (not understood), a w14 Choice (understood) and a Fallback. */
	private static String ac(Kind kind, String tag) {
		return "<mc:AlternateContent>"
				+ "<mc:Choice Requires=\"wpi\">" + block(kind, tag + "_WPI") + "</mc:Choice>"
				+ "<mc:Choice Requires=\"w14\">" + block(kind, tag + "_W14") + "</mc:Choice>"
				+ "<mc:Fallback>" + block(kind, tag + "_FB") + "</mc:Fallback>"
				+ "</mc:AlternateContent>";
	}

	private static final String TBL_PROPS = "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>";

	private static String tbl(String rows) {
		return "<w:tbl>" + TBL_PROPS + rows + "</w:tbl>";
	}

	/** A bare wps text box in a run (Word 2010+ reads it without alternate content), holding these blocks. */
	private static String textBox(String blocks) {
		return "<w:p><w:r><w:drawing><wp:inline><wp:extent cx=\"1270000\" cy=\"635000\"/><wp:docPr id=\"1\" name=\"Text Box 1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:cNvSpPr txBox=\"1\"/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1270000\" cy=\"635000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></wps:spPr>"
				+ "<wps:txbx><w:txbxContent>" + blocks + "</w:txbxContent></wps:txbx>"
				+ "<wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
	}

	private interface Locator {
		JaxbXmlPart<?> part(WordprocessingMLPackage pkg) throws Exception;
		List<Object> content(WordprocessingMLPackage pkg) throws Exception;
	}

	/** One container of CR-051 section 3.1's table (or D2's row and cell containers). */
	private static final class Case {
		final String name;
		final Kind kind;
		final String tag;
		final McProbeDocx docx;
		final Locator locator;
		final Class<?> promotedType;

		Case(String name, Kind kind, String tag, McProbeDocx docx, Locator locator) {
			this.name = name;
			this.kind = kind;
			this.tag = tag;
			this.docx = docx;
			this.locator = locator;
			this.promotedType = kind == Kind.ROW ? Tr.class : kind == Kind.CELL ? Tc.class : P.class;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static <T> T first(Object root, final Class<T> type) {
		final List<T> found = new ArrayList<T>();
		new TraversalUtil(root, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (type.isInstance(o) && found.isEmpty()) found.add(type.cast(o));
				return null;
			}
		}, McMode.ALL);
		assertFalse("a " + type.getSimpleName() + " in the tree", found.isEmpty());
		return found.get(0);
	}

	/** The bare wps text box's content (TraversalUtil hands out a text box's content, never the CTTxbxContent itself). */
	private static List<Object> textBoxContent(WordprocessingMLPackage pkg) throws Exception {
		org.docx4j.wml.Drawing drawing = first(pkg.getMainDocumentPart().getContents(), org.docx4j.wml.Drawing.class);
		org.docx4j.dml.wordprocessingDrawing.Inline inline = (org.docx4j.dml.wordprocessingDrawing.Inline) drawing.getAnchorOrInline().get(0);
		return inline.getGraphic().getGraphicData().getWordprocessingShape().getTxbx().getTxbxContent().getContent();
	}

	private static <T extends Part> T partOfType(WordprocessingMLPackage pkg, Class<T> type) {
		for (Part part : pkg.getParts().getParts().values()) {
			if (type.isInstance(part)) return type.cast(part);
		}
		throw new AssertionError("no " + type.getSimpleName());
	}

	private static final String SECT_PR_WITH_HEADER_AND_FOOTER = "<w:sectPr><w:headerReference w:type=\"default\" r:id=\"rIdH\"/>"
			+ "<w:footerReference w:type=\"default\" r:id=\"rIdF\"/></w:sectPr>";

	/** Every container the schema admits the element in since 17.3.4, each with the element at index 1 of its content. */
	private static List<Case> cases() {
		List<Case> cases = new ArrayList<Case>();
		cases.add(new Case("w:body", Kind.BLOCK, "BODY", McProbeDocx.body(p("before") + ac(Kind.BLOCK, "BODY") + p("after")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return pkg.getMainDocumentPart().getContent(); }
		}));
		cases.add(new Case("w:hdr", Kind.BLOCK, "HDR", McProbeDocx.body(p("body") + SECT_PR_WITH_HEADER_AND_FOOTER)
				.part("header1.xml", ContentTypes.WORDPROCESSINGML_HEADER, Namespaces.HEADER, "rIdH", "w:hdr", p("before") + ac(Kind.BLOCK, "HDR") + p("after"))
				.part("footer1.xml", ContentTypes.WORDPROCESSINGML_FOOTER, Namespaces.FOOTER, "rIdF", "w:ftr", p("footer")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return partOfType(pkg, HeaderPart.class); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return partOfType(pkg, HeaderPart.class).getContents().getContent(); }
		}));
		cases.add(new Case("w:ftr", Kind.BLOCK, "FTR", McProbeDocx.body(p("body") + SECT_PR_WITH_HEADER_AND_FOOTER)
				.part("header1.xml", ContentTypes.WORDPROCESSINGML_HEADER, Namespaces.HEADER, "rIdH", "w:hdr", p("header"))
				.part("footer1.xml", ContentTypes.WORDPROCESSINGML_FOOTER, Namespaces.FOOTER, "rIdF", "w:ftr", p("before") + ac(Kind.BLOCK, "FTR") + p("after")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return partOfType(pkg, FooterPart.class); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return partOfType(pkg, FooterPart.class).getContents().getContent(); }
		}));
		cases.add(new Case("w:tc", Kind.BLOCK, "TC", McProbeDocx.body(tbl("<w:tr><w:tc>" + p("before") + ac(Kind.BLOCK, "TC") + p("after") + "</w:tc></w:tr>")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return first(pkg.getMainDocumentPart().getContents(), Tc.class).getContent(); }
		}));
		cases.add(new Case("w:footnote", Kind.BLOCK, "FN", McProbeDocx.body("<w:p><w:r><w:footnoteReference w:id=\"1\"/></w:r></w:p>")
				.part("footnotes.xml", ContentTypes.WORDPROCESSINGML_FOOTNOTES, Namespaces.FOOTNOTES, "rIdFn", "w:footnotes",
						"<w:footnote w:id=\"1\">" + p("before") + ac(Kind.BLOCK, "FN") + p("after") + "</w:footnote>"), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart().getFootnotesPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return pkg.getMainDocumentPart().getFootnotesPart().getContents().getFootnote().get(0).getContent(); }
		}));
		cases.add(new Case("w:endnote", Kind.BLOCK, "EN", McProbeDocx.body("<w:p><w:r><w:endnoteReference w:id=\"1\"/></w:r></w:p>")
				.part("endnotes.xml", ContentTypes.WORDPROCESSINGML_ENDNOTES, Namespaces.ENDNOTES, "rIdEn", "w:endnotes",
						"<w:endnote w:id=\"1\">" + p("before") + ac(Kind.BLOCK, "EN") + p("after") + "</w:endnote>"), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart().getEndNotesPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return pkg.getMainDocumentPart().getEndNotesPart().getContents().getEndnote().get(0).getContent(); }
		}));
		cases.add(new Case("w:comment", Kind.BLOCK, "CMT", McProbeDocx.body("<w:p><w:commentRangeStart w:id=\"1\"/><w:r><w:t>x</w:t></w:r><w:commentRangeEnd w:id=\"1\"/><w:r><w:commentReference w:id=\"1\"/></w:r></w:p>")
				.part("comments.xml", ContentTypes.WORDPROCESSINGML_COMMENTS, Namespaces.COMMENTS, "rIdC", "w:comments",
						"<w:comment w:id=\"1\" w:author=\"a\">" + p("before") + ac(Kind.BLOCK, "CMT") + p("after") + "</w:comment>"), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart().getCommentsPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return pkg.getMainDocumentPart().getCommentsPart().getContents().getComment().get(0).getContent(); }
		}));
		cases.add(new Case("w:txbxContent", Kind.BLOCK, "TXBX", McProbeDocx.body(textBox(p("before") + ac(Kind.BLOCK, "TXBX") + p("after"))), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return textBoxContent(pkg); }
		}));
		cases.add(new Case("w:sdtContent (block)", Kind.BLOCK, "SDTB", McProbeDocx.body("<w:sdt><w:sdtPr/><w:sdtContent>" + p("before") + ac(Kind.BLOCK, "SDTB") + p("after") + "</w:sdtContent></w:sdt>"), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return ((SdtBlock) pkg.getMainDocumentPart().getContent().get(0)).getSdtContent().getContent(); }
		}));
		cases.add(new Case("w:customXml (block)", Kind.BLOCK, "CXB", McProbeDocx.body("<w:customXml w:element=\"x\">" + p("before") + ac(Kind.BLOCK, "CXB") + p("after") + "</w:customXml>"), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return ((CTCustomXmlBlock) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(0))).getContent(); }
		}));
		cases.add(new Case("w:docPartBody", Kind.BLOCK, "GLOS", McProbeDocx.body(p("body"))
				.part("glossary/document.xml", ContentTypes.WORDPROCESSINGML_GLOSSARYDOCUMENT, Namespaces.GLOSSARY_DOCUMENT, "rIdG", "w:glossaryDocument",
						"<w:docParts><w:docPart><w:docPartPr><w:name w:val=\"n\"/></w:docPartPr><w:docPartBody>" + p("before") + ac(Kind.BLOCK, "GLOS") + p("after") + "</w:docPartBody></w:docPart></w:docParts>"), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart().getGlossaryDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return pkg.getMainDocumentPart().getGlossaryDocumentPart().getContents().getDocParts().getDocPart().get(0).getDocPartBody().getContent(); }
		}));
		cases.add(new Case("w:tbl", Kind.ROW, "TBL", McProbeDocx.body(tbl(tr("before") + ac(Kind.ROW, "TBL") + tr("after"))), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return ((Tbl) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(0))).getContent(); }
		}));
		cases.add(new Case("w:sdtContent (row)", Kind.ROW, "SDTR", McProbeDocx.body(tbl("<w:sdt><w:sdtPr/><w:sdtContent>" + tr("before") + ac(Kind.ROW, "SDTR") + tr("after") + "</w:sdtContent></w:sdt>")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return first(pkg.getMainDocumentPart().getContents(), CTSdtRow.class).getSdtContent().getContent(); }
		}));
		cases.add(new Case("w:customXml (row)", Kind.ROW, "CXR", McProbeDocx.body(tbl("<w:customXml w:element=\"x\">" + tr("before") + ac(Kind.ROW, "CXR") + tr("after") + "</w:customXml>")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return ((CTCustomXmlRow) XmlUtils.unwrap(((Tbl) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(0))).getContent().get(0))).getContent(); }
		}));
		cases.add(new Case("w:tr", Kind.CELL, "TR", McProbeDocx.body(tbl("<w:tr>" + tc("before") + ac(Kind.CELL, "TR") + tc("after") + "</w:tr>")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return first(pkg.getMainDocumentPart().getContents(), Tr.class).getContent(); }
		}));
		cases.add(new Case("w:sdtContent (cell)", Kind.CELL, "SDTC", McProbeDocx.body(tbl("<w:tr><w:sdt><w:sdtPr/><w:sdtContent>" + tc("before") + ac(Kind.CELL, "SDTC") + tc("after") + "</w:sdtContent></w:sdt></w:tr>")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return first(pkg.getMainDocumentPart().getContents(), CTSdtCell.class).getSdtContent().getContent(); }
		}));
		cases.add(new Case("w:customXml (cell)", Kind.CELL, "CXC", McProbeDocx.body(tbl("<w:tr><w:customXml w:element=\"x\">" + tc("before") + ac(Kind.CELL, "CXC") + tc("after") + "</w:customXml></w:tr>")), new Locator() {
			public JaxbXmlPart<?> part(WordprocessingMLPackage pkg) { return pkg.getMainDocumentPart(); }
			public List<Object> content(WordprocessingMLPackage pkg) throws Exception { return ((CTCustomXmlCell) XmlUtils.unwrap(first(pkg.getMainDocumentPart().getContents(), Tr.class).getContent().get(0))).getContent(); }
		}));
		return cases;
	}

	private static int alternateContentCount(Object root) {
		final int[] n = { 0 };
		new TraversalUtil(root, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof AlternateContent) n[0]++;
				return null;
			}
		}, McMode.ALL);
		return n[0];
	}

	private static int alternateContentIn(List<Object> content) {
		int n = 0;
		for (Object o : content) if (XmlUtils.unwrap(o) instanceof AlternateContent) n++;
		return n;
	}

	private static String xml(Object o) {
		return XmlUtils.marshaltoString(o, true, false, Context.jc);
	}

	private static String ignorableOf(JaxbXmlPart<?> part) {
		String s = NamespacePrefixMapperUtils.ignorableOf(part.getJaxbElement());
		return s == null ? "" : s;
	}

	private static List<String> words(String s) {
		List<String> words = new ArrayList<String>();
		for (String w : s.trim().split("\\s+")) if (!w.isEmpty()) words.add(w);
		return words;
	}

	private String propertyBefore;

	@Before
	public void rememberProperty() {
		propertyBefore = Docx4jProperties.getProperty(McBlockLevel.PROPERTY);
	}

	@After
	public void restoreProperty() {
		Docx4jProperties.setProperty(McBlockLevel.PROPERTY, propertyBefore == null ? "" : propertyBefore);
		Docx4jProperties.setProperty("docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart", "false");
	}

	// --- word mode: the splice, for each container -----------------------------------------------------

	/** The Choice Word reads replaces the element at its index; the record holds the element, its parent and index; the prefix is in mc:Ignorable; a save writes the promoted content. */
	@Test
	public void wordModeSplicesEveryContainer() throws Exception {
		for (Case c : cases()) {
			WordprocessingMLPackage pkg = c.docx.load(McBlockLevel.WORD);
			JaxbXmlPart<?> part = c.locator.part(pkg);
			List<Object> content = c.locator.content(pkg);
			assertEquals(c + ": three items, the promoted block between the two paragraphs", 3, content.size());
			Object at = XmlUtils.unwrap(content.get(1));
			assertTrue(c + ": a " + c.promotedType.getSimpleName() + " at the element's index, but " + at.getClass().getSimpleName(), c.promotedType.isInstance(at));
			String promoted = xml(at);
			assertTrue(c + ": the w14 Choice's content was promoted: " + promoted, promoted.contains(c.tag + "_W14"));
			assertFalse(c + ": not the Fallback's", promoted.contains(c.tag + "_FB"));
			assertEquals(c + ": no element left in the tree", 0, alternateContentCount(part.getJaxbElement()));
			assertEquals(c + ": no element left in the container", 0, alternateContentIn(content));
			assertSame(c + ": the promoted block's parent is the container", ((org.jvnet.jaxb.lang.Child) at).getParent(), ((org.jvnet.jaxb.lang.Child) XmlUtils.unwrap(content.get(0))).getParent());

			List<ResolvedAlternateContent> records = part.getResolvedAlternateContent();
			assertEquals(c + ": one element recorded", 1, records.size());
			ResolvedAlternateContent r = records.get(0);
			assertEquals(c + ": at its index", 1, r.getIndex());
			assertEquals(c + ": one block promoted", 1, r.getPromotedCount());
			assertEquals(c + ": the container's name", c.name.replace(" (block)", "").replace(" (row)", "").replace(" (cell)", ""), r.getContainerName());
			assertTrue(c + ": the address ends with the container: " + r.getAddress(), r.getAddress().endsWith(r.getContainerName()) || r.getAddress().matches(".*" + java.util.regex.Pattern.quote(r.getContainerName()) + "\\[\\d+\\]$"));
			assertTrue(c + ": the w14 Choice was selected", r.getBranch() instanceof AlternateContent.Choice);
			assertEquals("w14", ((AlternateContent.Choice) r.getBranch()).getRequires());
			assertEquals(c + ": the record's element still holds every branch", 2, r.getElement().getChoice().size());
			assertNotNull(r.getElement().getFallback());
			assertTrue(c + ": the record's Fallback still holds its content", xml(r.getElement()).contains(c.tag + "_FB"));

			assertTrue(c + ": w14 in the root's mc:Ignorable, but '" + ignorableOf(part) + "'", words(ignorableOf(part)).contains("w14"));

			WordprocessingMLPackage again = McProbeDocx.roundTrip(pkg, McBlockLevel.WORD);
			List<Object> saved = c.locator.content(again);
			assertEquals(c + ": the save wrote the promoted block", 3, saved.size());
			assertTrue(c + ": as ordinary content", xml(XmlUtils.unwrap(saved.get(1))).contains(c.tag + "_W14"));
			assertEquals(c + ": and no element", 0, alternateContentCount(c.locator.part(again).getJaxbElement()));
			assertTrue(c + ": nothing to record on the reload", c.locator.part(again).getResolvedAlternateContent().isEmpty());
		}
	}

	/** The text model reads the promoted branch, in a cell, a header and a block content control among the rest. */
	@Test
	public void wordModeTextIsWords() throws Exception {
		for (Case c : cases()) {
			if (c.name.equals("w:comment") || c.name.equals("w:docPartBody")) continue; // the defaults leave comments and the glossary out
			WordprocessingMLPackage pkg = c.docx.load(McBlockLevel.WORD);
			TextOptions options = new TextOptions().include(TextOptions.Include.HEADERS).include(TextOptions.Include.FOOTERS);
			String text = Texts.plainText(pkg, options);
			assertTrue(c + ": " + text, text.contains(c.tag + "_W14"));
			assertFalse(c + ": " + text, text.contains(c.tag + "_FB"));
			assertFalse(c + ": " + text, text.contains(c.tag + "_WPI"));
		}
	}

	/** With no Fallback and an understood Choice, that Choice. */
	@Test
	public void noFallbackTakesTheUnderstoodChoice() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before") + "<mc:AlternateContent>"
				+ "<mc:Choice Requires=\"wpi\">" + p("WPI") + "</mc:Choice>"
				+ "<mc:Choice Requires=\"w14\">" + p("W14") + "</mc:Choice>"
				+ "</mc:AlternateContent>" + p("after")).load(McBlockLevel.WORD);
		List<Object> content = pkg.getMainDocumentPart().getContent();
		assertEquals(3, content.size());
		assertTrue(xml(content.get(1)).contains("W14"));
		assertEquals(1, pkg.getMainDocumentPart().getResolvedAlternateContent().size());
	}

	/** With no understood Choice and no Fallback (WD REQ-305), the element stays whole, in both modes, and a save writes it as it came. */
	@Test
	public void req305ElementStaysWhole() throws Exception {
		String body = p("before") + "<mc:AlternateContent>"
				+ "<mc:Choice Requires=\"w99\">" + p("W99") + "</mc:Choice>"
				+ "<mc:Choice Requires=\"wpi\">" + p("WPI") + "</mc:Choice>"
				+ "</mc:AlternateContent>" + p("after");
		for (McBlockLevel mode : McBlockLevel.values()) {
			WordprocessingMLPackage pkg = McProbeDocx.body(body).load(mode);
			List<Object> content = pkg.getMainDocumentPart().getContent();
			assertEquals(mode + ": the element is still there", 3, content.size());
			assertTrue(mode.toString(), XmlUtils.unwrap(content.get(1)) instanceof AlternateContent);
			assertEquals(mode + ": nothing recorded", 0, pkg.getMainDocumentPart().getResolvedAlternateContent().size());
			String saved = McProbeDocx.entry(McProbeDocx.save(pkg), "word/document.xml");
			assertTrue(mode + ": both Choices written", saved.contains("Requires=\"w99\"") && saved.contains("Requires=\"wpi\""));
			// and a reader applies REQ-305's first-Choice rule at use
			assertTrue(Texts.plainText(pkg, new TextOptions()).contains("W99"));
		}
	}

	/** Nested elements resolve outermost first: one in the kept branch is resolved in its new parent, one in a discarded branch stays whole in the record. */
	@Test
	public void nestedElementsResolveOutermostFirst() throws Exception {
		String inner = "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p("INNER_W14") + "</mc:Choice><mc:Fallback>" + p("INNER_FB") + "</mc:Fallback></mc:AlternateContent>";
		String discarded = "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + p("DISCARDED_W14") + "</mc:Choice><mc:Fallback>" + p("DISCARDED_FB") + "</mc:Fallback></mc:AlternateContent>";
		String outer = "<mc:AlternateContent>"
				+ "<mc:Choice Requires=\"w14\">" + tbl("<w:tr><w:tc>" + p("cell") + inner + "</w:tc></w:tr>") + "</mc:Choice>"
				+ "<mc:Fallback>" + p("OUTER_FB") + discarded + "</mc:Fallback>"
				+ "</mc:AlternateContent>";
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before") + outer + p("after")).load(McBlockLevel.WORD);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Object> content = mdp.getContent();
		assertEquals(3, content.size());
		Tbl tbl = (Tbl) XmlUtils.unwrap(content.get(1));
		Tc tc = first(tbl, Tc.class);
		assertEquals("the inner element was resolved in the cell", 2, tc.getContent().size());
		assertTrue(xml(tc.getContent().get(1)).contains("INNER_W14"));
		assertEquals("no element left in the tree", 0, alternateContentCount(mdp.getJaxbElement()));

		List<ResolvedAlternateContent> records = mdp.getResolvedAlternateContent();
		assertEquals("the outer and the inner recorded, outermost first; the discarded branch's not", 2, records.size());
		assertEquals("w:body", records.get(0).getContainerName());
		assertEquals("w:tc", records.get(1).getContainerName());
		assertTrue(records.get(1).getAddress(), records.get(1).getAddress().contains("w:tbl[1]"));
		// the element in the discarded branch stays whole inside the recorded outer element
		String outerXml = xml(records.get(0).getElement());
		assertTrue(outerXml, outerXml.contains("DISCARDED_W14") && outerXml.contains("DISCARDED_FB") && outerXml.contains("OUTER_FB"));
		assertTrue(XmlUtils.unwrap(records.get(0).getElement().getFallback().getAny().get(1)) instanceof AlternateContent);
	}

	/** The selected Choice's prefixes join the root's mc:Ignorable: added to a list, or set where the root had none. */
	@Test
	public void selectedPrefixesJoinIgnorable() throws Exception {
		String element = "<mc:AlternateContent><mc:Choice Requires=\"w15\">" + p("W15") + "</mc:Choice><mc:Fallback>" + p("FB") + "</mc:Fallback></mc:AlternateContent>";
		WordprocessingMLPackage listed = McProbeDocx.body(" mc:Ignorable=\"w14\"", p("x") + element).load(McBlockLevel.WORD);
		assertEquals("w14 w15", ((Document) listed.getMainDocumentPart().getJaxbElement()).getIgnorable());
		String saved = McProbeDocx.rootTag(McProbeDocx.entry(McProbeDocx.save(listed), "word/document.xml"), "w:document");
		assertTrue(saved, saved.contains("mc:Ignorable=\"w14 w15\"") && saved.contains("xmlns:w15="));

		WordprocessingMLPackage none = McProbeDocx.body(p("x") + element).load(McBlockLevel.WORD);
		assertEquals("w15", ((Document) none.getMainDocumentPart().getJaxbElement()).getIgnorable());

		WordprocessingMLPackage already = McProbeDocx.body(" mc:Ignorable=\"w14 w15\"", p("x") + element).load(McBlockLevel.WORD);
		assertEquals("w14 w15", ((Document) already.getMainDocumentPart().getJaxbElement()).getIgnorable());
	}

	/** An element with no branch at all is removed and recorded in word mode, kept in preserve. */
	@Test
	public void elementWithNoBranch() throws Exception {
		String body = p("before") + "<mc:AlternateContent/>" + p("after");
		WordprocessingMLPackage word = McProbeDocx.body(body).load(McBlockLevel.WORD);
		assertEquals(2, word.getMainDocumentPart().getContent().size());
		List<ResolvedAlternateContent> records = word.getMainDocumentPart().getResolvedAlternateContent();
		assertEquals(1, records.size());
		assertNull(records.get(0).getBranch());
		assertEquals(1, records.get(0).getIndex());
		assertEquals(0, records.get(0).getPromotedCount());

		WordprocessingMLPackage preserve = McProbeDocx.body(body).load(McBlockLevel.PRESERVE);
		assertEquals(3, preserve.getMainDocumentPart().getContent().size());
		assertTrue(XmlUtils.unwrap(preserve.getMainDocumentPart().getContent().get(1)) instanceof AlternateContent);
	}

	/** A w:customXml directly in a branch binds as a DOM element; the splice binds it as the container's kind. */
	@Test
	public void customXmlInBranchIsBound() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(p("before") + "<mc:AlternateContent><mc:Choice Requires=\"w14\">"
				+ "<w:customXml w:element=\"x\">" + p("CX_W14") + "</w:customXml></mc:Choice><mc:Fallback>" + p("FB") + "</mc:Fallback></mc:AlternateContent>").load(McBlockLevel.WORD);
		Object at = XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(1));
		assertTrue("bound as CTCustomXmlBlock, but " + at.getClass().getName(), at instanceof CTCustomXmlBlock);
		assertTrue(xml(((CTCustomXmlBlock) at).getContent().get(0)).contains("CX_W14"));
		String saved = McProbeDocx.entry(McProbeDocx.save(pkg), "word/document.xml");
		assertTrue(saved, saved.contains("<w:customXml w:element=\"x\">") && saved.contains("CX_W14"));
	}

	// --- the binder paths --------------------------------------------------------------------------------

	private static final String BODY_PROBE = p("before") + ac(Kind.BLOCK, "BODY") + p("after");

	/** With the binder created eagerly, its DOM is the spliced tree's: an XPath finds the promoted paragraph, not the element. */
	@Test
	public void eagerBinderXPathFindsThePromotedParagraph() throws Exception {
		Docx4jProperties.setProperty("docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart", "true");
		WordprocessingMLPackage pkg = McProbeDocx.body(BODY_PROBE).load(McBlockLevel.WORD);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		assertEquals(3, mdp.getContent().size());
		List<Object> paragraphs = mdp.getJAXBNodesViaXPath("//w:body/w:p", false);
		assertEquals("the promoted paragraph among the body's", 3, paragraphs.size());
		assertSame("the same JAXB objects", mdp.getContent().get(1), paragraphs.get(1));
		assertTrue(mdp.getJAXBNodesViaXPath("//mc:AlternateContent", false).isEmpty());
		assertEquals(1, mdp.getResolvedAlternateContent().size());
	}

	/** A binder created later marshals the spliced tree: the same answer, and the record survives the re-unmarshal. */
	@Test
	public void lateBinderXPathFindsThePromotedParagraph() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(BODY_PROBE).load(McBlockLevel.WORD);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		assertEquals(3, mdp.getContent().size());
		assertEquals(3, mdp.getJAXBNodesViaXPath("//w:body/w:p", false).size());
		assertTrue(mdp.getJAXBNodesViaXPath("//mc:AlternateContent", false).isEmpty());
		assertEquals("the record survives the binder's re-unmarshal", 1, mdp.getResolvedAlternateContent().size());
	}

	/** On the path a part takes when StAX rejects it for another reason, the splice, the Choice prefixes and the drawing ids all land. */
	@Test
	public void rejectedPartIsSplicedToo() throws Exception {
		String drawingParagraph = "<w:p><w:r><w:t>BODY_W14</w:t></w:r><w:r><w:drawing><wp:inline><wp:extent cx=\"10\" cy=\"10\"/><wp:docPr id=\"2\" name=\"Picture 2\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"><wps:wsp><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"10\" cy=\"10\"/></a:xfrm></wps:spPr><wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
		String element = "<mc:AlternateContent><mc:Choice Requires=\"wpi\">" + p("BODY_WPI") + "</mc:Choice>"
				+ "<mc:Choice Requires=\"w14\">" + drawingParagraph + "</mc:Choice><mc:Fallback>" + p("BODY_FB") + "</mc:Fallback></mc:AlternateContent>";
		String rejected = "<w:p><w:pPr><w14:nonsense/></w:pPr><w:r><w:t>before</w:t></w:r></w:p>";
		WordprocessingMLPackage pkg = McProbeDocx.body(rejected + element + p("after")).load(McBlockLevel.WORD);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Object> content = mdp.getContent();
		assertEquals(3, content.size());
		assertTrue(xml(content.get(1)).contains("BODY_W14"));
		assertEquals(1, mdp.getResolvedAlternateContent().size());
		assertTrue("the Choice prefixes recorded: " + mdp.getMcChoiceNamespaces(), mdp.getMcChoiceNamespaces().contains("w14"));
		assertEquals("docPr id 2 registered from the promoted drawing", 3L, pkg.getDrawingPropsIdTracker().generateId());
		assertEquals("the binder's DOM is the spliced tree's", 3, mdp.getJAXBNodesViaXPath("//w:body/w:p", false).size());
		assertTrue(xml(mdp.getContent().get(1)).contains("BODY_W14"));
		assertEquals("the record survives the binder's re-creation", 1, mdp.getResolvedAlternateContent().size());
		assertTrue(mdp.getJAXBNodesViaXPath("//mc:AlternateContent", false).isEmpty());
		assertTrue(words(ignorableOf(mdp)).contains("w14"));
	}

	// --- preserve mode -----------------------------------------------------------------------------------

	/** Each block container's element survives load, a save writes every branch, the prefixes stay declared; row and cell containers' are resolved until phase 2. */
	@Test
	public void preserveModeKeepsBlockLevelElements() throws Exception {
		for (Case c : cases()) {
			WordprocessingMLPackage pkg = c.docx.load(McBlockLevel.PRESERVE);
			JaxbXmlPart<?> part = c.locator.part(pkg);
			List<Object> content = c.locator.content(pkg);
			assertEquals(c.toString(), 3, content.size());
			Object at = XmlUtils.unwrap(content.get(1));
			if (c.kind == Kind.BLOCK) {
				assertTrue(c + ": kept whole, but " + at.getClass().getSimpleName(), at instanceof AlternateContent);
				AlternateContent ac = (AlternateContent) at;
				assertEquals(2, ac.getChoice().size());
				assertNotNull(ac.getFallback());
				assertTrue(c + ": nothing recorded", part.getResolvedAlternateContent().isEmpty());

				byte[] saved = McProbeDocx.save(pkg);
				String partXml = McProbeDocx.entry(saved, part.getPartName().getName().substring(1));
				assertTrue(c + ": every branch written", partXml.contains("Requires=\"wpi\"") && partXml.contains("Requires=\"w14\"") && partXml.contains("<mc:Fallback>"));
				assertTrue(c + ": the content of each", partXml.contains(c.tag + "_WPI") && partXml.contains(c.tag + "_W14") && partXml.contains(c.tag + "_FB"));
				String root = McProbeDocx.rootTag(partXml);
				assertTrue(c + ": the Choices' prefixes declared on the root: " + root, root.contains("xmlns:wpi=") && root.contains("xmlns:w14="));

				WordprocessingMLPackage again = McProbeDocx.roundTrip(pkg, McBlockLevel.PRESERVE);
				assertTrue(c + ": still whole after the round trip", XmlUtils.unwrap(c.locator.content(again).get(1)) instanceof AlternateContent);
			} else {
				// CR-051 section 5, phase 1: row- and cell-level elements resolved as in word mode until phase 2
				assertTrue(c + ": resolved until phase 2, but " + at.getClass().getSimpleName(), c.promotedType.isInstance(at));
				assertTrue(xml(at).contains(c.tag + "_W14"));
				assertEquals(c + ": recorded", 1, part.getResolvedAlternateContent().size());
			}
		}
	}

	/** In preserve mode the text model takes the first understood Choice at use, and compares the branches. */
	@Test
	public void preserveModeTextIsWordsToo() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(BODY_PROBE).load(McBlockLevel.PRESERVE);
		org.docx4j.model.text.TextResult result = Texts.of(pkg, new TextOptions());
		String text = org.docx4j.model.text.PlainText.of(result);
		assertTrue(text, text.contains("BODY_W14") && !text.contains("BODY_FB"));
		boolean differ = false;
		for (TextWarning w : result.warnings()) if (TextWarning.MCE_BRANCHES_DIFFER.equals(w.getKind())) differ = true;
		assertTrue("mce.branches_differ raised from the kept element", differ);
	}

	// --- both modes: the option and the override win over the property ------------------------------------

	@Test
	public void loadOptionWinsOverProperty() throws Exception {
		Docx4jProperties.setProperty(McBlockLevel.PROPERTY, "preserve");
		assertEquals(McBlockLevel.PRESERVE, McBlockLevel.fromProperty());
		WordprocessingMLPackage word = McProbeDocx.body(BODY_PROBE).load(McBlockLevel.WORD);
		assertTrue(XmlUtils.unwrap(word.getMainDocumentPart().getContent().get(1)) instanceof P);
		WordprocessingMLPackage byProperty = McProbeDocx.body(BODY_PROBE).load();
		assertTrue(XmlUtils.unwrap(byProperty.getMainDocumentPart().getContent().get(1)) instanceof AlternateContent);

		Docx4jProperties.setProperty(McBlockLevel.PROPERTY, "word");
		WordprocessingMLPackage preserve = McProbeDocx.body(BODY_PROBE).load(McBlockLevel.PRESERVE);
		assertTrue(XmlUtils.unwrap(preserve.getMainDocumentPart().getContent().get(1)) instanceof AlternateContent);
		WordprocessingMLPackage byProperty2 = McProbeDocx.body(BODY_PROBE).load();
		assertTrue(XmlUtils.unwrap(byProperty2.getMainDocumentPart().getContent().get(1)) instanceof P);
	}

	@Test
	public void packageOverrideWinsOverProperty() throws Exception {
		Docx4jProperties.setProperty(McBlockLevel.PROPERTY, "word");
		WordprocessingMLPackage pkg = McProbeDocx.body(BODY_PROBE).load();
		assertFalse("the main document part is unmarshalled lazily", pkg.getMainDocumentPart().isUnmarshalled());
		pkg.setMcBlockLevel(McBlockLevel.PRESERVE);
		assertTrue(XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(1)) instanceof AlternateContent);

		Docx4jProperties.setProperty(McBlockLevel.PROPERTY, "preserve");
		WordprocessingMLPackage pkg2 = McProbeDocx.body(BODY_PROBE).load();
		pkg2.setMcBlockLevel(McBlockLevel.WORD);
		assertTrue(XmlUtils.unwrap(pkg2.getMainDocumentPart().getContent().get(1)) instanceof P);
	}

	@Test
	public void propertyValues() {
		assertEquals(McBlockLevel.WORD, McBlockLevel.parse("word"));
		assertEquals(McBlockLevel.PRESERVE, McBlockLevel.parse(" Preserve "));
		assertNull(McBlockLevel.parse("other"));
		Docx4jProperties.setProperty(McBlockLevel.PROPERTY, "");
		assertEquals("unset: word", McBlockLevel.WORD, McBlockLevel.fromProperty());
		Docx4jProperties.setProperty(McBlockLevel.PROPERTY, "nonsense");
		assertEquals("unknown: word", McBlockLevel.WORD, McBlockLevel.fromProperty());
	}
}
