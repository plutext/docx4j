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
package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTDocPart;
import org.docx4j.wml.GlossaryDocument;
import org.docx4j.wml.Style;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * CR-063: a content control's placeholder in Word's form, from the three binders alike. An empty bound
 * value shows the glossary entry the control's {@code w:placeholder} names (D2), a copy of the entry's
 * paragraphs as the glossary holds them, the control's rPr not applied (D9, check 68); a text control
 * naming none, or a missing entry, is
 * given Word's default entry, {@code DefaultPlaceholder_-1854013440}, the glossary made where the
 * document has none (D3, D7: the glossary document part and a styles part holding Placeholder Text);
 * the reference survives binding, bound or not (D1); a date, list, building-block or repeating-section-item
 * control naming none gets Word's entry for its kind (D8, §13: Jason's placeholders.docx), a picture keeps
 * today's run; binding twice adds nothing. The default's wording is Word 365's (D5). Word's behaviour is
 * core-ts's checks 64 to 66 (CR-063 §1, §9, §10); the content copy follows core-ts's section 63.18.
 */
public class PlaceholderEntriesTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";
	private static final String[] IMPLEMENTATIONS = { "BindingTraverserXSLT", "BindingTraverserNonXSLT", "BindingTraverserStAX" };

	private static final String ITEM_ID = "{A1B2C3D4-0000-4000-8000-000000000063}";
	private static final String DEFAULT = PlaceholderEntries.DEFAULT_ENTRY;
	private static final String DEFAULT_TEXT = PlaceholderEntries.DEFAULT_TEXT;
	private static final String ENTRY = "6767ABCDEF0123456789ABCDEF012345";

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A bound run-level text control, with this alias, over this XPath, naming this entry (or none). */
	private static String runControl(String alias, String xpath, String entry, String rPr) {
		return "<w:p><w:r><w:t xml:space=\"preserve\">Before </w:t></w:r><w:sdt><w:sdtPr>" + (rPr == null ? "" : rPr)
				+ "<w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ (entry == null ? "" : "<w:placeholder><w:docPart w:val=\"" + entry + "\"/></w:placeholder>")
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/><w:text/></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:t>STALE</w:t></w:r></w:sdtContent></w:sdt><w:r><w:t xml:space=\"preserve\"> after.</w:t></w:r></w:p>";
	}

	/** A bound block-level text control whose paragraph has a w:pPr. */
	private static String blockControl(String alias, String xpath, String entry, String rPr) {
		return "<w:sdt><w:sdtPr>" + (rPr == null ? "" : rPr)
				+ "<w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ (entry == null ? "" : "<w:placeholder><w:docPart w:val=\"" + entry + "\"/></w:placeholder>")
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/><w:text/></w:sdtPr>"
				+ "<w:sdtContent><w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:t>STALE</w:t></w:r></w:p></w:sdtContent></w:sdt>";
	}

	private static String dateControl(String alias, String xpath) {
		return "<w:p><w:sdt><w:sdtPr><w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/>"
				+ "<w:date><w:dateFormat w:val=\"d MMMM yyyy\"/><w:lid w:val=\"en-AU\"/><w:storeMappedDataAs w:val=\"dateTime\"/></w:date></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:t>STALE</w:t></w:r></w:sdtContent></w:sdt></w:p>";
	}

	private static String listControl(String alias, String xpath, String kind) {
		return "<w:p><w:sdt><w:sdtPr><w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/>"
				+ "<w:" + kind + "><w:listItem w:displayText=\"One\" w:value=\"1\"/><w:listItem w:displayText=\"Two\" w:value=\"2\"/></w:" + kind + "></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:t>STALE</w:t></w:r></w:sdtContent></w:sdt></w:p>";
	}

	private static String pictureControl(String alias, String xpath) {
		return "<w:p><w:sdt><w:sdtPr><w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/><w:picture/></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:t>STALE</w:t></w:r></w:sdtContent></w:sdt></w:p>";
	}

	private static String docPart(String name, String body) {
		return "<w:docPart><w:docPartPr><w:name w:val=\"" + name + "\"/><w:category><w:name w:val=\"General\"/>"
				+ "<w:gallery w:val=\"placeholder\"/></w:category><w:types><w:type w:val=\"bbPlcHdr\"/></w:types>"
				+ "<w:behaviors><w:behavior w:val=\"content\"/></w:behaviors><w:guid w:val=\"{11111111-2222-4333-8444-555555555555}\"/></w:docPartPr>"
				+ "<w:docPartBody>" + body + "</w:docPartBody></w:docPart>";
	}

	/** An entry of two paragraphs (a bold run and a plain one; one more), each paragraph styled in the glossary. */
	private static final String TWO_PARAGRAPH_ENTRY = docPart(ENTRY,
			"<w:p><w:pPr><w:pStyle w:val=\"" + ENTRY + "\"/></w:pPr>"
			+ "<w:r><w:rPr><w:rStyle w:val=\"PlaceholderText\"/><w:b/></w:rPr><w:t xml:space=\"preserve\">Enter </w:t></w:r>"
			+ "<w:r><w:rPr><w:rStyle w:val=\"PlaceholderText\"/></w:rPr><w:t>the name,</w:t></w:r></w:p>"
			+ "<w:p><w:pPr><w:pStyle w:val=\"" + ENTRY + "\"/></w:pPr>"
			+ "<w:r><w:rPr><w:rStyle w:val=\"PlaceholderText\"/></w:rPr><w:t>then the date.</w:t></w:r></w:p>");

	/**
	 * A code-built template: the body's blocks, a custom XML part {@code <root><a>VALUE</a><empty/></root>},
	 * and a glossary of these entries (none where null). Saved and reloaded, as a template is.
	 */
	private static WordprocessingMLPackage template(String blocks, String glossaryEntries) throws Exception {
		return template(blocks, glossaryEntries, "<root><a>VALUE</a><empty></empty></root>");
	}

	private static WordprocessingMLPackage template(String blocks, String glossaryEntries, String dataXml) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart main = pkg.getMainDocumentPart();
		// a section: an image sized from its own dimensions needs the page's (createImageInline)
		main.setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + blocks
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>"
				+ "</w:body></w:document>"));

		CustomXmlDataStoragePart cx = new CustomXmlDataStoragePart();
		CustomXmlDataStorage data = new CustomXmlDataStorageImpl();
		data.setDocument(XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(dataXml))));
		cx.setData(data);
		main.addTargetPart(cx);
		CustomXmlDataStoragePropertiesPart props = new CustomXmlDataStoragePropertiesPart();
		DatastoreItem dsi = new org.docx4j.customXmlProperties.ObjectFactory().createDatastoreItem();
		dsi.setItemID(ITEM_ID);
		props.setJaxbElement(dsi);
		cx.addTargetPart(props);

		if (glossaryEntries != null) {
			GlossaryDocumentPart glossary = new GlossaryDocumentPart();
			glossary.setJaxbElement((GlossaryDocument) XmlUtils.unmarshalString(
					"<w:glossaryDocument " + W + "><w:docParts>" + glossaryEntries + "</w:docParts></w:glossaryDocument>"));
			main.addTargetPart(glossary);
		}
		return reload(pkg);
	}

	private static WordprocessingMLPackage reload(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
	}

	private static WordprocessingMLPackage bind(WordprocessingMLPackage pkg, String implementation) throws Exception {
		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			Docx4J.bind(pkg, (org.w3c.dom.Document) null, Docx4J.FLAG_BIND_BIND_XML);
			return pkg;
		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	private static String mainXml(WordprocessingMLPackage pkg) throws Exception {
		return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
	}

	/** The control with this alias, as marshalled. */
	private static String control(String xml, String alias) {
		Matcher m = Pattern.compile("<w:sdt>(?:(?!<w:sdt>).)*?<w:alias w:val=\"" + alias + "\"/>.*?</w:sdt>", Pattern.DOTALL).matcher(xml);
		assertTrue("control " + alias + " in " + xml, m.find());
		return m.group();
	}

	private static String sdtPr(String control) {
		return control.substring(0, control.indexOf("</w:sdtPr>"));
	}

	private static String sdtContent(String control) {
		return control.substring(control.indexOf("<w:sdtContent>"));
	}

	/** The control's text: its {@code w:t} contents joined. */
	private static String shown(String control) {
		Matcher m = Pattern.compile("<w:t(?: [^>]*)?>(.*?)</w:t>", Pattern.DOTALL).matcher(sdtContent(control));
		StringBuilder sb = new StringBuilder();
		while (m.find()) sb.append(m.group(1));
		return sb.toString();
	}

	private static String reference(String control) {
		Matcher m = Pattern.compile("<w:placeholder><w:docPart w:val=\"([^\"]*)\"/></w:placeholder>").matcher(sdtPr(control));
		return m.find() ? m.group(1) : null;
	}

	private static List<String> entryNames(WordprocessingMLPackage pkg) throws Exception {
		List<String> names = new ArrayList<String>();
		GlossaryDocumentPart glossary = pkg.getMainDocumentPart().getGlossaryDocumentPart();
		if (glossary == null) return names;
		for (CTDocPart part : glossary.getContents().getDocParts().getDocPart()) {
			names.add(PlaceholderEntries.nameOf(part));
		}
		return names;
	}

	private static void assertGlossaryStyles(String implementation, WordprocessingMLPackage pkg) throws Exception {
		GlossaryDocumentPart glossary = pkg.getMainDocumentPart().getGlossaryDocumentPart();
		assertNotNull(implementation + ": glossary part", glossary);
		assertEquals(implementation, "/word/glossary/document.xml", glossary.getPartName().getName());
		StyleDefinitionsPart styles = glossary.getStyleDefinitionsPart();
		assertNotNull(implementation + ": the glossary's styles part", styles);
		assertEquals(implementation, "/word/glossary/styles.xml", styles.getPartName().getName());
		assertNotNull(implementation + ": related from the glossary", glossary.getRelationshipsPart().getRel(new PartName("/word/glossary/styles.xml")));
		Style placeholder = null, defaultFont = null;
		for (Style s : styles.getContents().getStyle()) {
			if ("PlaceholderText".equals(s.getStyleId())) placeholder = s;
			if ("DefaultParagraphFont".equals(s.getStyleId())) defaultFont = s;
		}
		assertNotNull(implementation + ": Default Paragraph Font", defaultFont);
		assertTrue(implementation, Boolean.TRUE.equals(defaultFont.isDefault()) && "character".equals(defaultFont.getType()));
		assertNotNull(implementation + ": Placeholder Text", placeholder);
		assertEquals(implementation, "DefaultParagraphFont", placeholder.getBasedOn().getVal());
		assertEquals(implementation, "666666", placeholder.getRPr().getColor().getVal());
	}

	/**
	 * D3, D7: a text control naming no entry, empty, shows Word's default entry, which is added to a
	 * glossary made for it (the document part and a styles part); a control with a value is bound as
	 * before; binding again adds nothing.
	 */
	@Test
	public void aTextControlNamingNoneGetsWordsDefaultEntryAndTheGlossary() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			WordprocessingMLPackage pkg = template(
					runControl("Empty", "/root/empty", null, null)
					+ runControl("Value", "/root/a", null, null)
					+ blockControl("EmptyBlock", "/root/empty", null, "<w:rPr><w:i/></w:rPr>"), null);
			assertNull(implementation, pkg.getMainDocumentPart().getGlossaryDocumentPart());

			String xml = mainXml(bind(pkg, implementation));
			String empty = control(xml, "Empty");
			assertEquals(implementation + ": " + empty, DEFAULT_TEXT, shown(empty));
			assertTrue(implementation + ": " + empty, sdtPr(empty).contains("<w:showingPlcHdr/>"));
			assertEquals(implementation + ": " + empty, DEFAULT, reference(empty));
			assertTrue(implementation + ": the run in Placeholder Text: " + empty, sdtContent(empty).contains("<w:rStyle w:val=\"PlaceholderText\"/>"));
			// the reference where Word writes it: before w:showingPlcHdr (the XSLT puts the flag first; either order is Word's)
			assertEquals(implementation + ": one reference: " + empty, 1, sdtPr(empty).split("<w:placeholder>", -1).length - 1);

			String block = control(xml, "EmptyBlock");
			assertEquals(implementation + ": " + block, DEFAULT_TEXT, shown(block));
			assertEquals(implementation + ": " + block, DEFAULT, reference(block));
			assertTrue(implementation + ": the paragraph's pPr kept: " + block, sdtContent(block).contains("<w:jc w:val=\"center\"/>"));
			assertFalse(implementation + ": the control's rPr not applied to the entry (D9): " + block, sdtContent(block).contains("<w:i/>"));
			assertTrue(implementation + ": the control's rPr stays in its sdtPr: " + block, sdtPr(block).contains("<w:i/>"));
			assertTrue(implementation + ": the entry's style: " + block, sdtContent(block).contains("<w:rStyle w:val=\"PlaceholderText\"/>"));

			String value = control(xml, "Value");
			assertEquals(implementation + ": " + value, "VALUE", shown(value));
			assertFalse(implementation + ": " + value, sdtPr(value).contains("showingPlcHdr"));

			assertEquals(implementation, java.util.Arrays.asList(DEFAULT), entryNames(pkg));
			assertGlossaryStyles(implementation, pkg);
			assertNotNull(PlaceholderEntries.entryOf(pkg.getMainDocumentPart().getGlossaryDocumentPart(), DEFAULT));
			String entryXml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getGlossaryDocumentPart().getJaxbElement(), true, false);
			for (String expected : new String[] { "<w:name w:val=\"General\"/>", "<w:gallery w:val=\"placeholder\"/>",
					"<w:type w:val=\"bbPlcHdr\"/>", "<w:behavior w:val=\"content\"/>",
					"<w:rStyle w:val=\"PlaceholderText\"/>", "<w:t>" + DEFAULT_TEXT + "</w:t>" }) {
				assertTrue(implementation + ": " + expected + " in " + entryXml, entryXml.contains(expected));
			}
			// a GUID of the document's own, as Word gives an entry it adds (§16)
			assertTrue(implementation + ": a GUID in " + entryXml, Pattern.compile(
					"<w:guid w:val=\"\\{[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}\\}\"/>").matcher(entryXml).find());
			assertFalse(implementation + ": not the GUID of any Word file: " + entryXml, entryXml.contains("{7D21F9BC-12A3-463E-B08F-7DD4FFC77A03}"));

			// the output round-trips, and a second binding run adds nothing
			WordprocessingMLPackage again = bind(reload(pkg), implementation);
			assertEquals(implementation + ": bound twice", java.util.Arrays.asList(DEFAULT), entryNames(again));
			assertGlossaryStyles(implementation, again);
			assertEquals(implementation + ": bound twice", 1, again.getMainDocumentPart().getGlossaryDocumentPart()
					.getRelationshipsPart().getRelationshipsByType(Namespaces.STYLES).size());
			String emptyAgain = control(mainXml(again), "Empty");
			assertEquals(implementation, DEFAULT, reference(emptyAgain));
			assertEquals(implementation, DEFAULT_TEXT, shown(emptyAgain));
		}
	}

	/**
	 * D2 (core-ts's 63.18) and D9 (check 68): a control naming an entry shows a copy of the entry's
	 * content: a block control both paragraphs, each run as the glossary holds it, the control's run
	 * properties not applied, the paragraph's own w:pPr kept (the entry's, naming the glossary's styles,
	 * left behind); a
	 * run-level control the first paragraph's runs. D1: a bound control with a value keeps its
	 * reference, and so does an unbound control. Nothing is added to the glossary.
	 */
	@Test
	public void anEntryTheControlNamesIsShownParagraphsAndAll() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			WordprocessingMLPackage pkg = template(
					blockControl("Block", "/root/empty", ENTRY, "<w:rPr><w:color w:val=\"FF0000\"/></w:rPr>")
					+ runControl("Run", "/root/empty", ENTRY, null)
					+ runControl("Value", "/root/a", ENTRY, null)
					+ "<w:p><w:sdt><w:sdtPr><w:alias w:val=\"Unbound\"/><w:tag w:val=\"Unbound\"/><w:id w:val=\"7\"/>"
					+ "<w:placeholder><w:docPart w:val=\"" + ENTRY + "\"/></w:placeholder><w:text/></w:sdtPr>"
					+ "<w:sdtContent><w:r><w:t>Typed</w:t></w:r></w:sdtContent></w:sdt></w:p>",
					TWO_PARAGRAPH_ENTRY);

			String xml = mainXml(bind(pkg, implementation));

			String block = control(xml, "Block");
			assertEquals(implementation + ": " + block, ENTRY, reference(block));
			assertTrue(implementation + ": " + block, sdtPr(block).contains("<w:showingPlcHdr/>"));
			assertEquals(implementation + ": " + block, "Enter the name,then the date.", shown(block));
			String content = sdtContent(block);
			assertEquals(implementation + ": two paragraphs: " + content, 2, content.split("<w:p>", -1).length - 1);
			assertEquals(implementation + ": the control's pPr on each: " + content, 2, content.split("<w:jc w:val=\"center\"/>", -1).length - 1);
			assertFalse(implementation + ": the entry's pStyle stays in the glossary: " + content, content.contains("<w:pStyle"));
			assertFalse(implementation + ": the control's colour not on the entry's runs (D9, check 68): " + content, content.contains("<w:color w:val=\"FF0000\"/>"));
			assertTrue(implementation + ": the control's rPr stays in its sdtPr: " + block, sdtPr(block).contains("<w:color w:val=\"FF0000\"/>"));
			assertEquals(implementation + ": each run's style: " + content, 3, content.split("<w:rStyle w:val=\"PlaceholderText\"/>", -1).length - 1);
			assertTrue(implementation + ": the bold run's bold: " + content, content.contains("<w:b/>"));

			String run = control(xml, "Run");
			assertEquals(implementation + ": " + run, ENTRY, reference(run));
			assertEquals(implementation + ": the first paragraph's runs: " + run, "Enter the name,", shown(run));
			assertFalse(implementation + ": no paragraph in a run-level control: " + run, sdtContent(run).contains("<w:p>"));

			String value = control(xml, "Value");
			assertEquals(implementation + ": D1, bound with a value: " + value, ENTRY, reference(value));
			assertEquals(implementation, "VALUE", shown(value));
			String unbound = control(xml, "Unbound");
			assertEquals(implementation + ": D1, unbound: " + unbound, ENTRY, reference(unbound));
			assertEquals(implementation, "Typed", shown(unbound));

			assertEquals(implementation + ": nothing added", java.util.Arrays.asList(ENTRY), entryNames(pkg));
			assertNull(implementation + ": no styles part made where no entry was added",
					pkg.getMainDocumentPart().getGlossaryDocumentPart().getStyleDefinitionsPart());
		}
	}

	/**
	 * D3 (revised): a text control naming an entry the glossary lacks is pointed at Word's default
	 * entry, added beside the glossary's own; nothing is written under the missing name. The glossary,
	 * which had no styles part, gets one holding Placeholder Text.
	 */
	@Test
	public void aMissingEntryIsReplacedByTheDefault() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			WordprocessingMLPackage pkg = template(runControl("Missing", "/root/empty", "NOTINGLOSSARY", null), TWO_PARAGRAPH_ENTRY);
			String missing = control(mainXml(bind(pkg, implementation)), "Missing");
			assertEquals(implementation + ": " + missing, DEFAULT, reference(missing));
			assertEquals(implementation + ": " + missing, DEFAULT_TEXT, shown(missing));
			assertEquals(implementation, java.util.Arrays.asList(ENTRY, DEFAULT), entryNames(pkg));
			assertGlossaryStyles(implementation, pkg);
		}
	}

	/**
	 * D8 (§13): a date control naming no entry gets Word's date entry, DefaultPlaceholder_-1854013437 ("Click
	 * or tap to enter a date."), a drop-down list and a combo box Word's list entry, -1854013438 ("Choose an
	 * item."), each added to the glossary once, with a GUID of its own (§16); the list's own items are kept. The
	 * configured run (D4) is the text kinds' only, so these show Word's wording whatever is configured.
	 */
	@Test
	public void aDateOrListControlNamingNoneGetsWordsEntryForItsKind() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			WordprocessingMLPackage pkg = template(dateControl("Date", "/root/empty")
					+ listControl("Drop", "/root/empty", "dropDownList") + listControl("Combo", "/root/empty", "comboBox"), null);
			String xml = mainXml(bind(pkg, implementation));
			String date = control(xml, "Date");
			assertEquals(implementation + ": " + date, "Click or tap to enter a date.", shown(date));
			assertTrue(implementation + ": " + date, sdtPr(date).contains("<w:showingPlcHdr/>"));
			assertEquals(implementation + ": " + date, "DefaultPlaceholder_-1854013437", reference(date));
			for (String alias : new String[] { "Drop", "Combo" }) {
				String list = control(xml, alias);
				assertEquals(implementation + ": " + list, "Choose an item.", shown(list));
				assertEquals(implementation + ": " + list, "DefaultPlaceholder_-1854013438", reference(list));
				assertTrue(implementation + ": the template's items kept: " + list, sdtPr(list).contains("w:displayText=\"Two\""));
				assertFalse(implementation + ": Word's own first item not emulated: " + list, sdtPr(list).contains("Choose an item.\""));
			}
			assertEquals(implementation, java.util.Arrays.asList("DefaultPlaceholder_-1854013437", "DefaultPlaceholder_-1854013438"), entryNames(pkg));
			String glossaryXml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getGlossaryDocumentPart().getJaxbElement(), true, false);
			Matcher guids = Pattern.compile("<w:guid w:val=\"(\\{[0-9A-F-]{36}\\})\"/>").matcher(glossaryXml);
			java.util.Set<String> seen = new java.util.HashSet<String>();
			while (guids.find()) seen.add(guids.group(1));
			assertEquals(implementation + ": two entries, two GUIDs of their own: " + glossaryXml, 2, seen.size());
			assertGlossaryStyles(implementation, pkg);
		}
	}

	/** A block-level picture control (the sdt directly in the body), bound. */
	private static String pictureControlBlock(String alias, String xpath) {
		return "<w:sdt><w:sdtPr><w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/><w:picture/></w:sdtPr>"
				+ "<w:sdtContent><w:p><w:r><w:t>STALE</w:t></w:r></w:p></w:sdtContent></w:sdt>";
	}

	/** A run-level picture control holding an authored drawing (its blip naming a relationship that isn't there), flagged or not. */
	private static String pictureControlWithDrawing(String alias, String xpath, boolean flagged) {
		return "<w:p><w:sdt><w:sdtPr><w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ (flagged ? "<w:showingPlcHdr/>" : "") + "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/><w:picture/></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:drawing><wp:inline xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:extent cx=\"609600\" cy=\"609600\"/><wp:docPr id=\"7\" name=\"authored\"/><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"7\" name=\"authored\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"rId999\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"609600\" cy=\"609600\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:sdtContent></w:sdt></w:p>";
	}

	/** A 64x64 blue PNG, a node's value. */
	private static final String BLUE_PNG = "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAIAAAAlC+aJAAAAT0lEQVR42u3PQQkAAAgEsItjHPtjGCP4FgYrsFTPaxEQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQuCz7JkE8tKGrEgAAAABJRU5ErkJggg==";

	/** The bytes of the image part a control's first blip names. */
	private static byte[] blipBytes(WordprocessingMLPackage pkg, String control) throws Exception {
		Matcher m = Pattern.compile("<a:blip [^>]*r:embed=\"([^\"]+)\"").matcher(control);
		assertTrue("a blip in " + control, m.find());
		org.docx4j.relationships.Relationship rel = pkg.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(m.group(1));
		assertNotNull("relationship " + m.group(1), rel);
		return ((org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage) pkg.getMainDocumentPart().getRelationshipsPart().getPart(rel)).getBytes();
	}

	/** How many image parts related from the main document part hold the placeholder picture. */
	private static int placeholderParts(WordprocessingMLPackage pkg) throws Exception {
		int n = 0;
		for (org.docx4j.relationships.Relationship rel : pkg.getMainDocumentPart().getRelationshipsPart().getRelationships().getRelationship()) {
			org.docx4j.openpackaging.parts.Part part = pkg.getMainDocumentPart().getRelationshipsPart().getPart(rel);
			if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					&& java.util.Arrays.equals(((org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage) part).getBytes(), PicturePlaceholder.bytes())) n++;
		}
		return n;
	}

	private static void assertPlaceholderPicture(String implementation, WordprocessingMLPackage pkg, String control, boolean blockLevel) throws Exception {
		assertTrue(implementation + ": the flag: " + control, sdtPr(control).contains("<w:showingPlcHdr/>"));
		String content = sdtContent(control);
		assertTrue(implementation + ": Word's extent: " + content, content.contains("<wp:extent cx=\"1270000\" cy=\"1270000\"/>"));
		assertFalse(implementation + ": no text: " + content, content.contains("<w:t"));
		assertEquals(implementation + ": in a paragraph where block-level: " + content, blockLevel,
				content.replaceFirst("^\\s*<w:sdtContent>", "").trim().startsWith("<w:p"));
		assertTrue(implementation + ": the placeholder picture", java.util.Arrays.equals(PicturePlaceholder.bytes(), blipBytes(pkg, control)));
	}

	/**
	 * D11 (§18, check 71): a picture control whose node is empty shows docx4j's placeholder picture in
	 * Word's form, whatever it held (stale text, an unflagged authored drawing; run-level or block-level);
	 * one already showing a placeholder drawing is left as it is; the image part is added once and
	 * reused; a node holding a picture shows that picture, with no flag; bound again, the same.
	 */
	@Test
	public void aPictureControlWithAnEmptyNodeShowsThePlaceholderPicture() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			WordprocessingMLPackage pkg = template(pictureControl("Pic", "/root/empty") + pictureControlBlock("PicBlock", "/root/empty")
					+ pictureControlWithDrawing("PicDrawing", "/root/empty", false) + pictureControlWithDrawing("PicShowing", "/root/empty", true)
					+ pictureControl("PicValue", "/root/png"),
					null, "<root><empty></empty><png>" + BLUE_PNG + "</png></root>");
			bind(pkg, implementation);
			String xml = mainXml(pkg);
			assertPlaceholderPicture(implementation, pkg, control(xml, "Pic"), false);
			assertPlaceholderPicture(implementation, pkg, control(xml, "PicBlock"), true);
			assertPlaceholderPicture(implementation, pkg, control(xml, "PicDrawing"), false);
			// already in Word's form (the flag, a drawing): left as it is, authored drawing and all
			String showing = control(xml, "PicShowing");
			assertTrue(implementation + ": left as it is: " + showing, sdtPr(showing).contains("<w:showingPlcHdr/>")
					&& sdtContent(showing).contains("name=\"authored\"") && sdtContent(showing).contains("r:embed=\"rId999\""));
			String value = control(xml, "PicValue");
			assertFalse(implementation + ": a value, no flag: " + value, sdtPr(value).contains("<w:showingPlcHdr/>"));
			assertTrue(implementation + ": the node's picture", java.util.Arrays.equals(java.util.Base64.getDecoder().decode(BLUE_PNG), blipBytes(pkg, value)));
			assertEquals(implementation + ": one placeholder image part", 1, placeholderParts(pkg));
			assertNull(implementation + ": no glossary, no text placeholder", pkg.getMainDocumentPart().getGlossaryDocumentPart());

			WordprocessingMLPackage again = bind(reload(pkg), implementation);
			String xml2 = mainXml(again);
			assertPlaceholderPicture(implementation, again, control(xml2, "Pic"), false);
			assertEquals(implementation + ": bound twice, still one part", 1, placeholderParts(again));
		}
	}

	/** The main styles part's style with this id, or null. */
	private static Style mainStyle(WordprocessingMLPackage pkg, String styleId) throws Exception {
		Style found = null;
		for (Style s : pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle()) {
			if (styleId.equals(s.getStyleId())) {
				assertNull("one " + styleId, found);
				found = s;
			}
		}
		return found;
	}

	/**
	 * D10 (§17, check 65c): where a placeholder run is written and the main styles part lacks Placeholder
	 * Text, the style is added as Word defines it (else Word drops it from every placeholder run on
	 * opening and shows the placeholder black). Every kind, whether the control shows an entry or its own
	 * run; a style the part defines is kept as it is; a document given no placeholder gains nothing.
	 */
	@Test
	public void thePlaceholderStyleIsAddedToTheMainStylesPart() throws Exception {
		for (String implementation : IMPLEMENTATIONS) {
			// a text, a date and a picture control, each with an empty value
			WordprocessingMLPackage pkg = template(runControl("Empty", "/root/empty", null, null)
					+ dateControl("Date", "/root/empty") + pictureControl("Pic", "/root/empty"), null);
			assertNull(implementation + ": a code-built package's styles lack it", mainStyle(pkg, "PlaceholderText"));
			assertNotNull(implementation, mainStyle(pkg, "DefaultParagraphFont"));
			bind(pkg, implementation);
			Style style = mainStyle(pkg, "PlaceholderText");
			assertNotNull(implementation + ": Placeholder Text added to the main styles part", style);
			String xml = XmlUtils.marshaltoString(style, true, false);
			for (String expected : new String[] { "w:type=\"character\"", "<w:name w:val=\"Placeholder Text\"/>",
					"<w:basedOn w:val=\"DefaultParagraphFont\"/>", "<w:uiPriority w:val=\"99\"/>", "<w:unhideWhenUsed/>",
					"<w:color w:val=\"666666\"/>" }) {
				assertTrue(implementation + ": " + expected + " in " + xml, xml.contains(expected));
			}
			assertNotNull(implementation, mainStyle(pkg, "DefaultParagraphFont"));
			// bound again: still one of each
			WordprocessingMLPackage again = bind(reload(pkg), implementation);
			assertNotNull(implementation + ": bound twice", mainStyle(again, "PlaceholderText"));

			// a picture control alone, a kind with no entry: the style follows the run (the NonXSLT and StAX
			// binders write today's placeholder run; the XSLT binder writes nothing for an empty picture)
			WordprocessingMLPackage pic = bind(template(pictureControl("Pic", "/root/empty"), null), implementation);
			boolean run = control(mainXml(pic), "Pic").contains("<w:rStyle w:val=\"PlaceholderText\"/>");
			assertEquals(implementation + ": a kind with no entry, the style where the run is", run, mainStyle(pic, "PlaceholderText") != null);

			// a styles part defining the style keeps its definition
			WordprocessingMLPackage own = template(runControl("Empty", "/root/empty", null, null), null);
			own.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle().add((Style) XmlUtils.unmarshalString(
					"<w:style " + W + " w:type=\"character\" w:styleId=\"PlaceholderText\"><w:name w:val=\"Placeholder Text\"/>"
					+ "<w:rPr><w:color w:val=\"999999\"/></w:rPr></w:style>"));
			own = bind(reload(own), implementation);
			String kept = XmlUtils.marshaltoString(mainStyle(own, "PlaceholderText"), true, false);
			assertTrue(implementation + ": kept: " + kept, kept.contains("<w:color w:val=\"999999\"/>") && !kept.contains("basedOn"));

			// a document whose controls all hold values gains nothing
			WordprocessingMLPackage value = bind(template(runControl("Value", "/root/a", null, null), null), implementation);
			assertNull(implementation + ": no placeholder, no style", mainStyle(value, "PlaceholderText"));
		}
	}

	/** The three binders give the same document text and the same glossary. */
	@Test
	public void theBindersAgree() throws Exception {
		String blocks = runControl("Empty", "/root/empty", null, null)
				+ blockControl("Block", "/root/empty", ENTRY, "<w:rPr><w:color w:val=\"FF0000\"/></w:rPr>")
				+ runControl("Missing", "/root/empty", "NOTINGLOSSARY", null)
				+ dateControl("Date", "/root/empty");
		String text = null, glossary = null;
		for (String implementation : IMPLEMENTATIONS) {
			WordprocessingMLPackage pkg = bind(template(blocks, TWO_PARAGRAPH_ENTRY), implementation);
			java.io.StringWriter sw = new java.io.StringWriter();
			org.docx4j.TextUtils.extractText(pkg.getMainDocumentPart().getContents(), sw);
			String xml = mainXml(pkg);
			String refs = reference(control(xml, "Empty")) + " " + reference(control(xml, "Block")) + " "
					+ reference(control(xml, "Missing")) + " " + reference(control(xml, "Date"));
			String g = entryNames(pkg).toString();
			if (text == null) {
				text = sw.toString() + refs;
				glossary = g;
			} else {
				assertEquals(implementation, text, sw.toString() + refs);
				assertEquals(implementation, glossary, g);
			}
		}
		assertEquals("[" + ENTRY + ", " + DEFAULT + ", DefaultPlaceholder_-1854013437]", glossary);
	}
}
