/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.openpackaging.parts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A child given more than once (CR-079 phases 2 and 3; WD REQ-322), against Word's saves: the files are {@code repeated/}, the rounds
 * rc-, rt-, sm-, ft-, nd- and cr079-phase0, each with Word's save as {@code -word365.docx}. The comparisons are docx4j-python's
 * (its tests/openpackaging/test_repeated.py), so the engines are held to the same reading.
 */
public class RepeatedChildrenTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

	private static File file(String name) throws Exception {
		return new File(RepeatedChildrenTest.class.getClassLoader().getResource("repeated/" + name + ".docx").toURI());
	}

	private static List<String> names(String prefix) throws Exception {
		List<String> out = new ArrayList<String>();
		for (String f : file("rc-00-control").getParentFile().list()) {
			if (f.startsWith(prefix) && f.endsWith(".docx") && !f.endsWith("-word365.docx")) out.add(f.substring(0, f.length() - 5));
		}
		java.util.Collections.sort(out);
		return out;
	}

	private static byte[] bytes(String name) throws Exception {
		return java.nio.file.Files.readAllBytes(file(name).toPath());
	}

	/** docx4j's save, the main part read. */
	private static byte[] saved(String name) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(file(name));
		pkg.getMainDocumentPart().getContents();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		return out.toByteArray();
	}

	private static byte[] entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) if (e.getName().equals(name)) return in.readAllBytes();
		}
		throw new AssertionError(name);
	}

	private static Element root(byte[] zip) throws Exception {
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(entry(zip, "word/document.xml"))).getDocumentElement();
	}

	private static List<Element> all(Element e, String local) {
		List<Element> out = new ArrayList<Element>();
		org.w3c.dom.NodeList l = e.getElementsByTagNameNS(W, local);
		for (int i = 0; i < l.getLength(); i++) out.add((Element) l.item(i));
		return out;
	}

	private static List<Element> children(Element e) {
		List<Element> out = new ArrayList<Element>();
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) if (n instanceof Element) out.add((Element) n);
		return out;
	}

	private static String text(Element e) {
		return e.getTextContent();
	}

	/** An attribute's value as Word writes an on/off one, which docx4j writes as true and false. */
	private static String spelled(String local, String value) {
		if (!"val".equals(local)) return value;
		if ("true".equals(value) || "on".equals(value)) return "1";
		if ("false".equals(value) || "off".equals(value)) return "0";
		return value;
	}

	private static Map<String, String> attributes(Element e) {
		Map<String, String> out = new TreeMap<String, String>();
		org.w3c.dom.NamedNodeMap a = e.getAttributes();
		for (int i = 0; i < a.getLength(); i++) {
			Node n = a.item(i);
			String local = n.getLocalName() == null ? n.getNodeName() : n.getLocalName();
			if (local.startsWith("rsid") || "xmlns".equals(n.getPrefix()) || "xmlns".equals(n.getNodeName())) continue;
			out.put(local, spelled(local, n.getNodeValue()));
		}
		return out;
	}

	private static Element target(Element root, String words) {
		for (Element p : all(root, "p")) if (text(p).contains(words)) return p;
		throw new AssertionError("no paragraph holding " + words);
	}

	/** python's target_properties: what the paragraph's w:pPr and w:rPr hold, each child's attributes, the last of a name. */
	private static Map<String, Map<String, String>> targetProperties(Element root, String words) {
		Map<String, Map<String, String>> out = new TreeMap<String, Map<String, String>>();
		Element p = target(root, words);
		List<Element> holders = new ArrayList<Element>();
		collect(p, holders);
		for (Element properties : holders) for (Element child : children(properties)) out.put(child.getLocalName(), attributes(child));
		// an on/off property stated off reads as one not stated, where nothing beneath sets it: Word's save writes nothing (rc-31, sm-80)
		for (String onOff : new String[] { "b", "vanish" }) if (java.util.Collections.singletonMap("val", "0").equals(out.get(onOff))) out.remove(onOff);
		return out;
	}

	private static void collect(Element e, List<Element> out) {
		for (Element c : children(e)) {
			if (W.equals(c.getNamespaceURI()) && ("pPr".equals(c.getLocalName()) || "rPr".equals(c.getLocalName()))) out.add(c);
			collect(c, out);
		}
	}

	private static final String[] PROPERTIES = { "rc-20-pPr-values", "rc-21-pPr-merge", "rc-22-rPr-values", "rc-23-rPr-merge", "rc-30-jc",
			"rc-31-b-on-off", "rc-32-b-off-on", "rc-33-sz", "rt-20-pPr-values", "rt-21-pPr-merge", "rt-22-rPr-values", "rt-23-rPr-merge",
			"rt-30-jc", "rt-31-b-on-off-on", "rt-32-b-off-on-off", "rt-33-sz" };

	@Test
	public void aPropertyGivenTwiceIsWhatWordsSaveHolds() throws Exception {
		for (String name : PROPERTIES) {
			assertEquals(name, targetProperties(root(bytes(name + "-word365")), "TARGET TEXT"), targetProperties(root(saved(name)), "TARGET TEXT"));
		}
		// cr079-phase0: a w:pPr's w:rPr merges too, and a w:pPrChange in the first copy survives
		assertEquals(targetProperties(root(bytes("c79-40-ppr-nested-rpr-word365")), "MARKED WORDS"), targetProperties(root(saved("c79-40-ppr-nested-rpr")), "MARKED WORDS"));
		assertEquals(1, all(target(root(saved("c79-42-ppr-pprchange")), "CHANGED WORDS"), "pPrChange").size());
	}

	/** c79-41: the first's w:ilvl with the second's w:numId. */
	@Test
	public void aNumPrIsAppliedToOneObject() throws Exception {
		for (byte[] zip : new byte[][] { saved("c79-41-ppr-numpr"), bytes("c79-41-ppr-numpr-word365") }) {
			Element numPr = all(target(root(zip), "LISTED WORDS"), "numPr").get(0);
			Map<String, String> got = new LinkedHashMap<String, String>();
			for (Element c : children(numPr)) got.put(c.getLocalName(), c.getAttributeNS(W, "val"));
			assertEquals("{ilvl=0, numId=2}", got.toString());
		}
	}

	/** Each paragraph's words, in order, docx4j's reading of the source against its reading of Word's save. */
	@Test
	public void theWordsAreWordsWordRead() throws Exception {
		List<String> files = new ArrayList<String>();
		for (String p : new String[] { "rc-", "rt-", "nd-2", "c79-3" }) files.addAll(names(p));
		for (String name : files) {
			if (!file(name).toPath().resolveSibling(name + "-word365.docx").toFile().exists()) continue;
			String ours = Texts.plainText(WordprocessingMLPackage.load(file(name)), new TextOptions()).trim();
			String theirs = Texts.plainText(WordprocessingMLPackage.load(file(name + "-word365")), new TextOptions()).trim();
			assertEquals(name, theirs, ours);
		}
	}

	private static List<String> pageWidths(Element root) {
		List<String> out = new ArrayList<String>();
		for (Element s : all(root, "sectPr")) for (Element sz : children(s)) if ("pgSz".equals(sz.getLocalName())) out.add(sz.getAttributeNS(W, "w"));
		return out;
	}

	/** The sections a save holds: the joined bodies', a nested body's where it stands last, a paragraph's where it is, each whole. */
	@Test
	public void aSaveHoldsWordsSections() throws Exception {
		List<String> files = new ArrayList<String>();
		for (String p : new String[] { "rc-1", "rt-1", "nd-2" }) files.addAll(names(p));
		for (String name : files) {
			Element ours = root(saved(name)), theirs = root(bytes(name + "-word365"));
			if (pageWidths(ours).isEmpty()) continue; // a file that states no section has none, where Word's save writes a default
			assertEquals(name, pageWidths(theirs), pageWidths(ours));
			List<Object> mine = new ArrayList<Object>(), word = new ArrayList<Object>();
			for (Element s : all(ours, "sectPr")) mine.add(stated(s));
			for (Element s : all(theirs, "sectPr")) word.add(stated(s));
			assertEquals(name, word, mine);
		}
	}

	/** sm-61: REQ-322 says nothing of w:tblW, and Word's save fits neither reading, so it stays as JAXB leaves it, the later copy's whole. */
	@Test
	public void aTablesWidthStaysTheLaterCopys() throws Exception {
		Element tblW = null;
		for (Element t : all(root(saved("sm-61-tblpr-attributes")), "tblW")) tblW = t;
		assertEquals("{type=auto}", attributes(tblW).toString());
		// and where the later copy states none, none: CR-079 section 4's decision, as Word's reading isn't known
		String table = "<w:tbl><w:tblPr><w:tblW w:w=\"4000\" w:type=\"dxa\"/></w:tblPr><w:tblPr><w:jc w:val=\"center\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid><w:tr><w:tc><w:p><w:r><w:t>CELL</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
		String xml = "<w:document xmlns:w=\"" + W + "\"><w:body>" + table + "<w:p/></w:body></w:document>";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (java.util.zip.ZipOutputStream z = new java.util.zip.ZipOutputStream(out); ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(bytes("rc-00-control")))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				z.putNextEntry(new ZipEntry(e.getName()));
				z.write(e.getName().equals("word/document.xml") ? xml.getBytes("UTF-8") : in.readAllBytes());
				z.closeEntry();
			}
		}
		org.docx4j.wml.Tbl tbl = (org.docx4j.wml.Tbl) XmlUtils.unwrap(WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray())).getMainDocumentPart().getContent().get(0));
		assertNull(tbl.getTblPr().getTblW());
		assertTrue(tbl.getTblPr().getJc() != null);
	}

	// --- the sectpr-merge round, python's _stated and _merged ----------------------------------------

	private static Map<String, Map<String, String>> stated(Element element) {
		Map<String, Map<String, String>> facts = new TreeMap<String, Map<String, String>>();
		boolean section = "sectPr".equals(element.getLocalName()), table = "tblPr".equals(element.getLocalName());
		int headers = 0;
		for (Element child : children(element)) {
			String name = child.getLocalName();
			if ("headerReference".equals(name)) {
				headers++;
				continue;
			}
			if (table && ("tblCellMar".equals(name) || "tblLook".equals(name))) continue; // Word's own defaults
			Map<String, String> said = attributes(child);
			if ("portrait".equals(said.get("orient"))) said.remove("orient"); // the default, which Word's save leaves out (sm-07)
			if ("cols".equals(name) && !said.containsKey("space")) said.put("space", "720"); // likewise the other way (sm-32)
			facts.put(name, said);
		}
		if (section) {
			if (!facts.containsKey("cols")) facts.put("cols", new TreeMap<String, String>(java.util.Collections.singletonMap("space", "720")));
			facts.put("headerReference", new TreeMap<String, String>(java.util.Collections.singletonMap("count", String.valueOf(headers))));
		}
		return facts;
	}

	private static Map<String, Object> merged(Element root) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
		List<Object> sections = new ArrayList<Object>(), tables = new ArrayList<Object>(), paragraph = new ArrayList<Object>(), runs = new ArrayList<Object>();
		for (Element s : all(root, "sectPr")) sections.add(stated(s));
		for (Element t : all(root, "tblPr")) tables.add(stated(t));
		Element target = target(root, "TARGET TEXT");
		for (Element c : children(target)) {
			if ("pPr".equals(c.getLocalName()) && !children(c).isEmpty()) paragraph.add(stated(c));
			if ("r".equals(c.getLocalName())) for (Element rPr : children(c)) {
				if (!"rPr".equals(rPr.getLocalName()) || children(rPr).isEmpty()) continue;
				Map<String, Map<String, String>> facts = stated(rPr);
				if (!facts.equals(java.util.Collections.singletonMap("vanish", java.util.Collections.singletonMap("val", "0")))) runs.add(facts);
			}
		}
		out.put("sections", sections);
		out.put("tables", tables);
		out.put("paragraph", paragraph);
		out.put("runs", runs);
		return out;
	}

	/** sm-61 is left out, as python leaves it: REQ-322 says nothing of w:tblW, and one file fits neither reading. */
	@Test
	public void aSaveHoldsWhatWordsSaveHolds() throws Exception {
		List<String> failed = new ArrayList<String>();
		for (String name : names("sm-")) {
			if (name.startsWith("sm-61")) continue;
			Map<String, Object> ours = merged(root(saved(name))), theirs = merged(root(bytes(name + "-word365")));
			if (!ours.equals(theirs)) failed.add(name + "\n  docx4j " + ours + "\n  Word   " + theirs);
		}
		assertTrue(String.join("\n", failed), failed.isEmpty());
	}

	// --- the footer-refs round ----------------------------------------------------------------------

	private static List<List<String>> references(byte[] zip) throws Exception {
		Element rels = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(entry(zip, "word/_rels/document.xml.rels"))).getDocumentElement();
		Map<String, String> targets = new HashMap<String, String>();
		for (Element r : children(rels)) targets.put(r.getAttribute("Id"), r.getAttribute("Target"));
		List<List<String>> found = new ArrayList<List<String>>();
		for (Element section : all(root(zip), "sectPr")) {
			List<String> held = new ArrayList<String>();
			for (Element ref : children(section)) {
				if (!"headerReference".equals(ref.getLocalName()) && !"footerReference".equals(ref.getLocalName())) continue;
				String target = targets.get(ref.getAttributeNS(R, "id"));
				String words = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(entry(zip, "word/" + target))).getDocumentElement().getTextContent().trim();
				held.add(ref.getLocalName() + " " + ref.getAttributeNS(W, "type") + " " + words);
			}
			java.util.Collections.sort(held);
			found.add(held);
		}
		return found;
	}

	/** Only the first copy's references stand, of any w:type, across joined bodies too, and a first copy with none leaves none. */
	@Test
	public void onlyTheFirstCopysReferencesStand() throws Exception {
		for (String name : names("ft-")) assertEquals(name, references(bytes(name + "-word365")), references(saved(name)));
	}

	// --- the reading itself ---------------------------------------------------------------------------

	/** Every unmarshal path joins and merges alike. */
	@Test
	public void everyPathReadsAlike() throws Exception {
		String always = "docx4j.jaxb.preprocess.always", eager = "docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart";
		String a = Docx4jProperties.getProperty(always), e = Docx4jProperties.getProperty(eager);
		String expected = Texts.plainText(WordprocessingMLPackage.load(file("rc-10-body-word365")), new TextOptions()).trim();
		try {
			for (String[] setting : new String[][] { { always, "MainDocumentPart" }, { eager, "true" } }) {
				Docx4jProperties.setProperty(setting[0], setting[1]);
				assertEquals(setting[0], expected, Texts.plainText(WordprocessingMLPackage.load(file("rc-10-body")), new TextOptions()).trim());
				Docx4jProperties.setProperty(setting[0], setting[0] == always ? (a == null ? "" : a) : (e == null ? "false" : e));
			}
		} finally {
			Docx4jProperties.setProperty(always, a == null ? "" : a);
			Docx4jProperties.setProperty(eager, e == null ? "false" : e);
		}
	}

	/** D7: one WARN a part, with the count. */
	@Test
	public void aPartWarnsOnceWithTheCount() throws Exception {
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
		appender.start();
		logger.addAppender(appender);
		try {
			WordprocessingMLPackage.load(file("rt-21-pPr-merge")).getMainDocumentPart().getContents();
		} finally {
			logger.detachAppender(appender);
		}
		List<String> told = new ArrayList<String>();
		for (ch.qos.logback.classic.spi.ILoggingEvent ev : appender.list) if (ev.getFormattedMessage().contains("given more than once")) told.add(ev.getFormattedMessage());
		assertEquals(told.toString(), 1, told.size());
		assertTrue(told.get(0), told.get(0).contains("/word/document.xml: 2 child"));
	}

	/** No other part's is measured, so another part keeps JAXB's reading, the last copy. */
	@Test
	public void anotherPartIsLeftAsJaxbReadsIt() throws Exception {
		String xml = "<w:ftr xmlns:w=\"" + W + "\"><w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:pPr><w:ind w:left=\"720\"/></w:pPr><w:r><w:t>FOOT</w:t></w:r></w:p></w:ftr>";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (java.util.zip.ZipOutputStream z = new java.util.zip.ZipOutputStream(out); ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(bytes("ft-01-footer-first-only")))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				z.putNextEntry(new ZipEntry(e.getName()));
				z.write(e.getName().equals("word/footer1.xml") ? xml.getBytes("UTF-8") : in.readAllBytes());
				z.closeEntry();
			}
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray()));
		org.docx4j.wml.Ftr ftr = (org.docx4j.wml.Ftr) ((JaxbXmlPart<?>) pkg.getParts().get(new PartName("/word/footer1.xml"))).getContents();
		org.docx4j.wml.PPr pPr = ((org.docx4j.wml.P) ftr.getContent().get(0)).getPPr();
		assertNull(pPr.getJc());
		assertEquals(java.math.BigInteger.valueOf(720), pPr.getInd().getLeft());
	}
}
