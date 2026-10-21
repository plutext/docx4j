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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * CR-052: a save declares every prefix a part's {@code mc} attributes name (the root's
 * {@code mc:Ignorable}, a Choice's {@code Requires}, {@code mc:Ignorable} and
 * {@code mc:MustUnderstand}), where Word 365 puts it (D4 (c)): a prefix bound as docx4j's table
 * binds it on the root, as Word's fixed set; any other on the {@code mc:Choice} that names it, with
 * the source's binding, ahead of {@code Requires}; a prefix the root's {@code mc:Ignorable} names
 * once, on the root.  Each case saves a package and checks every such prefix of the saved
 * {@code word/document.xml} against the declarations in scope where it is named, and on which
 * element the declaration sits.
 *
 * <p>Phase 0 of the CR measured what the same cases did before (its section 1): once a part was
 * unmarshalled, a prefix the table lacked was never declared, and content moved or copied into
 * another part brought nothing with it.  The mechanism now (section 1.1 and {@link McSourceBindings},
 * {@link McDeclarations}): at unmarshal each Choice, and the root, records what its prefixes were
 * bound to in the source; a copy gets the record; at marshal the tree is read once and each prefix
 * placed from its record, the table only where there is none.</p>
 */
public class McSourcePrefixesTest {

	private static final String MCE_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";
	private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String ZZ9 = "http://example.com/zz9";
	private static final String WPS = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
	private static final String CX1 = "http://schemas.microsoft.com/office/drawing/2015/9/8/chartex";
	private static final String SML = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
	private static final String EXCEL_VML = "urn:schemas-microsoft-com:office:excel";

	/** core-tests' text extraction probe: Choices naming zz9 (three) and shape2010 (one), each declared on its mc:AlternateContent. */
	private static final String PROBE = "text-extraction/extraction-probe.docx";
	/** Word 365's waterfall chart: a Choice Requires="cx1" over cx content, cx1 declared on the root only. */
	private static final String WATERFALL = "charts/waterfall.docx";

	private static final Predicate<AlternateContent.Choice> ZZ9_ONLY = c -> "zz9".equals(c.getRequires());
	private static final Predicate<AlternateContent.Choice> ANY = c -> true;

	/**
	 * What the check found in one part: each named prefix that resolves, with the local names of
	 * the elements declaring it where it is named; and each that does not, with how often.
	 */
	private static final class Found {
		final Set<String> resolved = new TreeSet<String>();
		final Map<String, Set<String>> declaredOn = new TreeMap<String, Set<String>>();
		final Map<String, Integer> undeclared = new TreeMap<String, Integer>();
		final Element root;
		Found(Element root) {
			this.root = root;
		}
		/** The one kind of element declaring the prefix wherever it is named. */
		String on(String prefix) {
			Set<String> s = declaredOn.get(prefix);
			assertNotNull(prefix + " resolved: " + this, s);
			assertEquals(prefix + " declared on one kind of element: " + this, 1, s.size());
			return s.iterator().next();
		}
		@Override
		public String toString() {
			return "resolved " + resolved + " on " + declaredOn + ", undeclared " + undeclared;
		}
	}

	private static WordprocessingMLPackage load(String resource) throws Exception {
		try (InputStream is = McSourcePrefixesTest.class.getClassLoader().getResourceAsStream(resource)) {
			assertNotNull(resource, is);
			return WordprocessingMLPackage.load(is);
		}
	}

	private static byte[] save(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		pkg.save(os);
		return os.toByteArray();
	}

	private static byte[] entry(byte[] docx, String name) throws Exception {
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(docx))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
				if (e.getName().equals(name)) return z.readAllBytes();
			}
		}
		throw new AssertionError(name + " not in the package");
	}

	private static Element parse(byte[] xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml)).getDocumentElement();
	}

	/** Every prefix an mc attribute of the saved main document part names, resolved where it is named. */
	private static Found check(byte[] docx) throws Exception {
		return check(parse(entry(docx, "word/document.xml")));
	}

	private static Found check(Element root) {
		Found found = new Found(root);
		walk(root, found);
		return found;
	}

	private static void walk(Element el, Found found) {
		List<String> named = new ArrayList<String>();
		if (MCE_NS.equals(el.getNamespaceURI()) && "Choice".equals(el.getLocalName()) && el.hasAttribute("Requires")) {
			named.add(el.getAttribute("Requires"));
		}
		for (String a : new String[] { "Ignorable", "MustUnderstand" }) {
			if (el.hasAttributeNS(MCE_NS, a)) named.add(el.getAttributeNS(MCE_NS, a));
		}
		for (String value : named) {
			for (String prefix : value.trim().split("\\s+")) {
				if (prefix.isEmpty()) continue;
				if (el.lookupNamespaceURI(prefix) != null) {
					found.resolved.add(prefix);
					found.declaredOn.computeIfAbsent(prefix, k -> new TreeSet<String>()).add(declarer(el, prefix).getLocalName());
				} else {
					found.undeclared.merge(prefix, 1, Integer::sum);
				}
			}
		}
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) walk((Element) n, found);
		}
	}

	/** The nearest element, from {@code el} up, declaring the prefix. */
	private static Element declarer(Element el, String prefix) {
		for (Node n = el; n instanceof Element; n = n.getParentNode()) {
			if (((Element) n).hasAttributeNS("http://www.w3.org/2000/xmlns/", prefix)) return (Element) n;
		}
		throw new AssertionError("no declaration of " + prefix + " above " + el.getTagName());
	}

	private static Map<String, Integer> counts(Object... prefixAndCount) {
		Map<String, Integer> m = new TreeMap<String, Integer>();
		for (int i = 0; i < prefixAndCount.length; i += 2) m.put((String) prefixAndCount[i], (Integer) prefixAndCount[i + 1]);
		return m;
	}

	/** The body's paragraphs holding a Choice the predicate selects. */
	private static List<Object> paragraphs(WordprocessingMLPackage pkg, Predicate<AlternateContent.Choice> want) {
		List<Object> out = new ArrayList<Object>();
		for (Object o : pkg.getMainDocumentPart().getContent()) {
			if (!(XmlUtils.unwrap(o) instanceof P)) continue;
			if (!choices(XmlUtils.unwrap(o), want).isEmpty()) out.add(o);
		}
		return out;
	}

	private static List<AlternateContent.Choice> choices(Object tree, Predicate<AlternateContent.Choice> want) {
		List<AlternateContent.Choice> out = new ArrayList<AlternateContent.Choice>();
		new TraversalUtil(tree, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof AlternateContent.Choice && want.test((AlternateContent.Choice) o)) out.add((AlternateContent.Choice) o);
				return null;
			}
		}, McMode.ALL);
		return out;
	}

	/** A paragraph with one mc:AlternateContent, its Choice's attributes as given, the fragment declaring the prefixes it names. */
	private static String paragraphWithChoice(String choiceAttributes, String declarations, String choiceContent) {
		return "<w:p xmlns:w=\"" + W_NS + "\" xmlns:mc=\"" + MCE_NS + "\" " + declarations + ">"
				+ "<w:r><mc:AlternateContent><mc:Choice " + choiceAttributes + ">" + choiceContent + "</mc:Choice>"
				+ "<mc:Fallback><w:t>f</w:t></mc:Fallback></mc:AlternateContent></w:r></w:p>";
	}

	// ---- section 1, case 1: a part saved as loaded

	/** A part nobody unmarshalled is saved from its source bytes, so every declaration the source made stays where it was. */
	@Test
	public void aPartNobodyUnmarshalledKeepsEveryDeclaration() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		assertFalse("the load leaves the main part unread", pkg.getMainDocumentPart().isUnmarshalled());
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertTrue(f.toString(), f.resolved.containsAll(Arrays.asList("zz9", "shape2010", "wps", "w14")));
		assertEquals("AlternateContent", f.on("zz9")); // the source's placement
	}

	/**
	 * Once unmarshalled, the same part declares every prefix: wps and w14, which the table binds as
	 * the source did, on the root; zz9 (three Choices) and shape2010 (one, an alias for the wps
	 * namespace), which it does not, on the Choice naming each, with the source's binding, written
	 * ahead of Requires as Word writes it.  The content keeps the table's prefix (wps:wsp under a
	 * Choice Requires="shape2010"), which is fine: the alias is declared beside it.
	 *
	 * <p>Phase 0 measured zz9 and shape2010 undeclared.</p>
	 */
	@Test
	public void anUnmarshalledPartDeclaresEveryPrefixWhereWordDoes() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		pkg.getMainDocumentPart().getContent();
		byte[] docx = save(pkg);
		Found f = check(docx);
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("wps"));
		assertEquals("document", f.on("w14"));
		assertEquals("Choice", f.on("zz9"));
		assertEquals("Choice", f.on("shape2010"));
		assertEquals(WPS, f.root.lookupNamespaceURI("wps"));
		assertNull("not on the root", f.root.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "zz9"));

		String xml = new String(entry(docx, "word/document.xml"), StandardCharsets.UTF_8);
		assertTrue("declared ahead of Requires, as Word writes it: " + xml.substring(xml.indexOf("Requires=\"zz9\"") - 60, xml.indexOf("Requires=\"zz9\"") + 14),
				xml.contains("<mc:Choice xmlns:zz9=\"" + ZZ9 + "\" Requires=\"zz9\">"));
		assertTrue(xml.contains("<mc:Choice xmlns:shape2010=\"" + WPS + "\" Requires=\"shape2010\">"));
		assertTrue("wps content keeps the table's prefix", xml.contains("<wps:wsp>"));
		assertEquals("one declaration per Choice naming zz9", 3, xml.split("xmlns:zz9=").length - 1);
		assertFalse("nothing on mc:Fallback", xml.contains("<mc:Fallback xmlns"));
	}

	// ---- section 1, case 2: the root's mc:Ignorable naming a private prefix

	/** A document part whose root declares xmlns:zz9 and names it in mc:Ignorable. */
	private static String rootIgnorableZz9() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("x");
		String xml = new String(entry(save(pkg), "word/document.xml"), StandardCharsets.UTF_8);
		xml = xml.replaceFirst("<w:document ", "<w:document xmlns:zz9=\"" + ZZ9 + "\" ");
		String tag = xml.substring(0, xml.indexOf('>', xml.indexOf("<w:document")));
		return tag.contains("mc:Ignorable=\"")
				? xml.replaceFirst("mc:Ignorable=\"", "mc:Ignorable=\"zz9 ")
				: xml.replaceFirst("<w:document ", "<w:document mc:Ignorable=\"zz9\" ");
	}

	/** The package createPackage makes, with its main part's bytes replaced. */
	private static byte[] withDocument(String documentXml) throws Exception {
		byte[] base = save(WordprocessingMLPackage.createPackage());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(base)); ZipOutputStream o = new ZipOutputStream(out)) {
			for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
				byte[] data = z.readAllBytes();
				if (e.getName().equals("word/document.xml")) data = documentXml.getBytes(StandardCharsets.UTF_8);
				o.putNextEntry(new ZipEntry(e.getName()));
				o.write(data);
				o.closeEntry();
			}
		}
		return out.toByteArray();
	}

	/**
	 * Case 2a: the tree set from code ({@code XmlUtils.unmarshalString}, {@code setJaxbElement}): the
	 * unmarshal records what the DOM declares for the root's Ignorable prefixes (D5 (a)), so the save
	 * declares zz9 on the root, with the source's binding.
	 */
	@Test
	public void aRootIgnorablePrefixSetFromCodeIsDeclaredOnTheRoot() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(rootIgnorableZz9()));
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("zz9"));
		assertEquals(ZZ9, f.root.lookupNamespaceURI("zz9"));
	}

	/** Case 2b: the same part loaded from a package: declared whether saved unread (its bytes) or unmarshalled first (the record). */
	@Test
	public void aRootIgnorablePrefixLoadedIsDeclaredOnTheRoot() throws Exception {
		byte[] source = withDocument(rootIgnorableZz9());
		Found unread = check(save(WordprocessingMLPackage.load(new ByteArrayInputStream(source))));
		assertEquals(unread.toString(), Collections.emptyMap(), unread.undeclared);
		assertEquals(ZZ9, unread.root.lookupNamespaceURI("zz9"));

		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(source));
		pkg.getMainDocumentPart().getContent();
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("zz9"));
		assertEquals(ZZ9, f.root.lookupNamespaceURI("zz9"));
		assertEquals("the Ignorable value itself is kept", "zz9",
				f.root.getAttributeNS(MCE_NS, "Ignorable").trim().split("\\s+")[0]);
	}

	/**
	 * A prefix the root's mc:Ignorable names and a Choice also names, bound alike: declared once,
	 * on the root, and not again on the Choice (Jason, 20 October 2026, for both ports).
	 */
	@Test
	public void aPrefixTheRootIgnorableNamesIsDeclaredOnceOnTheRoot() throws Exception {
		String xml = rootIgnorableZz9();
		String p = paragraphWithChoice("Requires=\"zz9\"", "", "<w:t>c</w:t>");
		xml = xml.replace("</w:body>", p + "</w:body>");
		// the paragraph declares no zz9 of its own: the root's is in scope
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(withDocument(xml)));
		pkg.getMainDocumentPart().getContent();
		byte[] docx = save(pkg);
		Found f = check(docx);
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("zz9"));
		assertEquals(1, new String(entry(docx, "word/document.xml"), StandardCharsets.UTF_8).split("xmlns:zz9=").length - 1);
	}

	// ---- section 1, case 3: a Choice built in code

	/**
	 * A Choice built in code saying {@code Requires="cx1"}: the table binds cx1, so it goes on the
	 * root, whether or not the part's registered prefixes include it (phase 0 measured it undeclared:
	 * no listener saw it).
	 */
	@Test
	public void aChoiceBuiltInCodeNamingATablePrefixIsDeclaredOnTheRoot() throws Exception {
		assertEquals(CX1, new NamespacePrefixMappings().getNamespaceURI("cx1"));
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(
				paragraphWithChoice("Requires=\"cx1\"", "xmlns:cx1=\"" + CX1 + "\"", "<w:t>c</w:t>")));
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("cx1"));
		assertEquals(CX1, f.root.lookupNamespaceURI("cx1"));
	}

	/**
	 * A Choice built in code naming a private prefix: {@code XmlUtils.unmarshalString} records what
	 * its fragment declared (D5 (a)), so the save declares it on the Choice.  Built without a
	 * declaration anywhere, the prefix is left undeclared with a warning, as before, unless the
	 * code sets the Choice's bindings itself ({@link McSourceBindings#set}).
	 */
	@Test
	public void aChoiceBuiltInCodeNamingAPrivatePrefix() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(
				paragraphWithChoice("Requires=\"zz9\"", "xmlns:zz9=\"" + ZZ9 + "\"", "<w:t>c</w:t>")));
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("zz9"));

		// nothing declared it, so nothing is recorded: undeclared, as before
		pkg = WordprocessingMLPackage.createPackage();
		P p = (P) XmlUtils.unmarshalString(paragraphWithChoice("Requires=\"zz8\"", "xmlns:zz8=\"" + ZZ9 + "\"", "<w:t>c</w:t>"));
		AlternateContent.Choice choice = choices(p, ANY).get(0);
		choice.setRequires("zz9"); // renamed after the unmarshal: the record knows zz8, not zz9
		pkg.getMainDocumentPart().getContent().add(p);
		f = check(save(pkg));
		assertEquals(f.toString(), counts("zz9", 1), f.undeclared);

		// unless the code says what the prefix means
		McSourceBindings.set(choice, Collections.singletonMap("zz9", ZZ9));
		f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("zz9"));
	}

	// ---- section 1, case 4: content moved or copied into another package

	/** Cases 4a and 4b: the probe's two zz9-only paragraphs moved (the same objects) and deep-copied into a new package: zz9 declared on each Choice. */
	@Test
	public void zz9MovedOrCopiedIsDeclaredOnTheChoice() throws Exception {
		for (boolean copy : new boolean[] { false, true }) {
			WordprocessingMLPackage source = load(PROBE);
			WordprocessingMLPackage dest = WordprocessingMLPackage.createPackage();
			List<Object> ps = paragraphs(source, ZZ9_ONLY);
			assertEquals(2, ps.size());
			for (Object p : ps) dest.getMainDocumentPart().getContent().add(copy ? XmlUtils.deepCopy(p) : p);
			Found f = check(save(dest));
			assertEquals((copy ? "copied: " : "moved: ") + f, Collections.emptyMap(), f.undeclared);
			assertEquals("Choice", f.on("zz9"));
			assertEquals(ZZ9, declarer((Element) f.root.getElementsByTagNameNS(MCE_NS, "Choice").item(0), "zz9").lookupNamespaceURI("zz9"));
		}
	}

	/** Case 4c: every paragraph holding a Choice deep-copied into a new package: all declared, each where Word puts it. */
	@Test
	public void everyChoiceCopiedIsDeclared() throws Exception {
		WordprocessingMLPackage source = load(PROBE);
		WordprocessingMLPackage dest = WordprocessingMLPackage.createPackage();
		for (Object p : paragraphs(source, ANY)) dest.getMainDocumentPart().getContent().add(XmlUtils.deepCopy(p));
		Found f = check(save(dest));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("wps"));
		assertEquals("document", f.on("w14"));
		assertEquals("Choice", f.on("zz9"));
		assertEquals("Choice", f.on("shape2010"));
	}

	// ---- section 2's Office case: Word's chartex chart

	/** Word's chartex document, unmarshalled and saved: cx1 is declared on the root (the table binds it as Word does). */
	@Test
	public void wordsChartexChartResavedKeepsCx1() throws Exception {
		WordprocessingMLPackage pkg = load(WATERFALL);
		assertEquals(1, choices(pkg.getMainDocumentPart().getJaxbElement(), c -> "cx1".equals(c.getRequires())).size());
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("cx1"));
		assertEquals(CX1, f.root.lookupNamespaceURI("cx1"));
	}

	/**
	 * The chart's paragraph deep-copied into a new package, and moved into another loaded document
	 * (the probe, a merge): cx1 declared on the root both ways, the probe's own prefixes still where
	 * they go.  (Only the namespaces are checked: the chart part itself is not carried.)
	 */
	@Test
	public void wordsChartexChartCopiedOrMergedKeepsCx1() throws Exception {
		WordprocessingMLPackage source = load(WATERFALL);
		WordprocessingMLPackage dest = WordprocessingMLPackage.createPackage();
		for (Object p : paragraphs(source, ANY)) dest.getMainDocumentPart().getContent().add(XmlUtils.deepCopy(p));
		Found copied = check(save(dest));
		assertEquals(copied.toString(), Collections.emptyMap(), copied.undeclared);
		assertEquals("document", copied.on("cx1"));

		source = load(WATERFALL);
		WordprocessingMLPackage probe = load(PROBE);
		for (Object p : paragraphs(source, ANY)) probe.getMainDocumentPart().getContent().add(p);
		Found merged = check(save(probe));
		assertEquals(merged.toString(), Collections.emptyMap(), merged.undeclared);
		assertEquals("document", merged.on("cx1"));
		assertEquals("document", merged.on("wps"));
		assertEquals("Choice", merged.on("zz9"));
		assertEquals("Choice", merged.on("shape2010"));
	}

	// ---- D1 (b): a Choice's own mc:Ignorable and mc:MustUnderstand

	/** A Choice's own mc:Ignorable and mc:MustUnderstand follow the Requires rule on that Choice: a private prefix on the Choice, a table prefix on the root. */
	@Test
	public void aChoicesOwnIgnorableAndMustUnderstandAreDeclared() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(paragraphWithChoice(
				"Requires=\"wps\" mc:Ignorable=\"zz9\" mc:MustUnderstand=\"w14\"",
				"xmlns:zz9=\"" + ZZ9 + "\" xmlns:wps=\"" + WPS + "\" xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"",
				"<w:t>c</w:t>")));
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("wps"));
		assertEquals("document", f.on("w14"));
		assertEquals("Choice", f.on("zz9"));
	}

	// ---- D3: the source's binding first; a clash with the Choice's own content

	/**
	 * A table prefix the source bound to another namespace ({@code x}: Excel's VML binds it to
	 * urn:schemas-microsoft-com:office:excel; the table maps it to SpreadsheetML main) is declared
	 * on the Choice with the source's binding, as long as the Choice's content does not use the
	 * table's namespace; where it does, a declaration there would rebind that content, so the
	 * content's binding stands (a warning is logged) and the prefix resolves to the table's.
	 */
	@Test
	public void aTablePrefixTheSourceBoundOtherwise() throws Exception {
		assertEquals(SML, new NamespacePrefixMappings().getNamespaceURI("x"));
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(
				paragraphWithChoice("Requires=\"x\"", "xmlns:x=\"" + EXCEL_VML + "\"", "<w:t>c</w:t>")));
		byte[] docx = save(pkg);
		Found f = check(docx);
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("x"));
		Element choice = (Element) f.root.getElementsByTagNameNS(MCE_NS, "Choice").item(0);
		assertEquals(EXCEL_VML, choice.lookupNamespaceURI("x"));

		// the clash: the Choice's content is in the namespace the table binds x to
		pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(
				paragraphWithChoice("Requires=\"x\"", "xmlns:x=\"" + EXCEL_VML + "\" xmlns:s=\"" + SML + "\"",
						"<s:sheetData/>")));
		docx = save(pkg);
		f = check(docx);
		choice = (Element) f.root.getElementsByTagNameNS(MCE_NS, "Choice").item(0);
		assertFalse("not rebound on the Choice: " + f, EXCEL_VML.equals(choice.lookupNamespaceURI("x")));
		String content = XmlUtils.w3CDomNodeToString(choice);
		assertTrue("the content keeps its namespace: " + content, content.contains(SML));
	}

	/**
	 * A Choice inside a content control that is itself directly in a branch (CR-051 section 11.3
	 * binds such a control from its DOM element once the read is over): its bindings are recorded
	 * from that element, on the StAX path, the DOM path and the binder path alike.
	 */
	@Test
	public void aChoiceInsideABranchControlIsRecordedAndDeclared() throws Exception {
		String inner = "<mc:AlternateContent xmlns:zz9=\"" + ZZ9 + "\"><mc:Choice Requires=\"zz9\"><w:r><w:t>in</w:t></w:r></mc:Choice>"
				+ "<mc:Fallback><w:r><w:t>out</w:t></w:r></mc:Fallback></mc:AlternateContent>";
		String sdt = "<w:sdt><w:sdtPr><w:tag w:val=\"t\"/></w:sdtPr><w:sdtContent><w:r><w:t>s</w:t></w:r>" + inner + "</w:sdtContent></w:sdt>";
		String p = paragraphWithChoice("Requires=\"wps\"", "xmlns:wps=\"" + WPS + "\"", sdt);
		String xml = rootIgnorableZz9().replace("</w:body>", p + "</w:body>");

		// the StAX path
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(withDocument(xml)));
		pkg.getMainDocumentPart().getContent();
		List<AlternateContent.Choice> zz9 = choices(pkg.getMainDocumentPart().getJaxbElement(), ZZ9_ONLY);
		assertEquals(1, zz9.size());
		assertEquals(Collections.singletonMap("zz9", ZZ9), McSourceBindings.of(zz9.get(0)));
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("declared once, on the root, which names it in mc:Ignorable", "document", f.on("zz9"));

		// the same without the root's Ignorable naming zz9: on the inner Choice
		String xml2 = xml.replace("mc:Ignorable=\"zz9 ", "mc:Ignorable=\"").replace("mc:Ignorable=\"zz9\"", "")
				.replace("xmlns:zz9=\"" + ZZ9 + "\" ", "");
		assertFalse(xml2.substring(0, xml2.indexOf("<w:body>")), xml2.substring(0, xml2.indexOf("<w:body>")).contains("zz9"));
		pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(withDocument(xml2)));
		pkg.getMainDocumentPart().getContent();
		f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("zz9"));

		// the DOM path
		org.docx4j.wml.Document doc = (org.docx4j.wml.Document) XmlUtils.unmarshalString(xml2);
		zz9 = choices(doc, ZZ9_ONLY);
		assertEquals(1, zz9.size());
		assertEquals(Collections.singletonMap("zz9", ZZ9), McSourceBindings.of(zz9.get(0)));

		// the binder path
		String key = "docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart";
		boolean before = Docx4jProperties.getProperty(key, false);
		Docx4jProperties.setProperty(key, true);
		try {
			pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(withDocument(xml2)));
			pkg.getMainDocumentPart().getContent();
			f = check(save(pkg));
			assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
			assertEquals("Choice", f.on("zz9"));
		} finally {
			Docx4jProperties.setProperty(key, before);
		}
	}

	/**
	 * A prefix the root binds one way (a source-only binding, so a pair on the root) and a Choice's
	 * source bound another: the Choice gets its own declaration, unless its content uses the root's
	 * namespace through that prefix (content moved in from a document where the root bound it so),
	 * which a declaration on the Choice would rebind; then the content's binding stands.
	 */
	@Test
	public void aChoiceDoesNotRebindAPrefixItsContentUsesAsTheRootBindsIt() throws Exception {
		String x = "http://example.com/x", y = "http://example.com/y";
		String xml = rootIgnorableZz9().replace("xmlns:zz9=\"" + ZZ9 + "\"", "xmlns:zz9=\"" + x + "\"");
		xml = xml.replace("</w:body>", paragraphWithChoice("Requires=\"zz9\"", "xmlns:zz9=\"" + y + "\"", "<w:t>c</w:t>") + "</w:body>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(withDocument(xml)));
		pkg.getMainDocumentPart().getContent();
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals(x, f.root.lookupNamespaceURI("zz9"));
		Element choice = (Element) f.root.getElementsByTagNameNS(MCE_NS, "Choice").item(0);
		assertEquals("the Choice's own binding", y, choice.lookupNamespaceURI("zz9"));

		// content in the root's namespace, by the prefix the root binds (as content moved in from
		// a document that bound zz9 so would be): a DOM element with no declaration of its own
		AlternateContent.Choice c = choices(pkg.getMainDocumentPart().getJaxbElement(), ZZ9_ONLY).get(0);
		Element thing = parse(("<zz9:thing xmlns:zz9=\"" + x + "\"/>").getBytes(StandardCharsets.UTF_8));
		thing.removeAttributeNS("http://www.w3.org/2000/xmlns/", "zz9");
		assertEquals(x, thing.getNamespaceURI());
		c.getAny().clear();
		c.getAny().add(thing);
		f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("document", f.on("zz9"));
		choice = (Element) f.root.getElementsByTagNameNS(MCE_NS, "Choice").item(0);
		assertEquals("the content's binding stands", x, choice.lookupNamespaceURI("zz9"));
		Node first = choice.getFirstChild();
		while (first != null && !(first instanceof Element)) first = first.getNextSibling(); // past the indentation
		assertNotNull(first);
		assertEquals(x, first.getNamespaceURI());
	}

	/** The stream writer: a declaration the plan puts on a Choice gives way to the marshaller's own binding of that prefix. */
	@Test
	public void theWriterLetsTheMarshallersBindingStand() throws Exception {
		java.io.StringWriter sw = new java.io.StringWriter();
		org.docx4j.utils.XMLStreamWriterWrapper w = new org.docx4j.utils.XMLStreamWriterWrapper(null,
				javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(sw));
		w.declareOnNextStartElement(Collections.singletonMap("p", "urn:one"));
		w.writeStartElement("mc", "Choice", MCE_NS);
		w.writeNamespace("mc", MCE_NS);
		w.writeNamespace("p", "urn:two"); // the marshaller's, for content it writes as p:
		w.writeAttribute("Requires", "p");
		w.writeStartElement("p", "e", "urn:two");
		w.writeEndElement();
		w.writeEndElement();
		w.close();
		String out = sw.toString();
		assertTrue(out, out.contains("xmlns:p=\"urn:two\""));
		assertFalse(out, out.contains("urn:one"));
		assertEquals(out, 1, out.split("xmlns:p=").length - 1);

		// and the same binding twice is written once
		sw = new java.io.StringWriter();
		w = new org.docx4j.utils.XMLStreamWriterWrapper(null, javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(sw));
		w.declareOnNextStartElement(Collections.singletonMap("p", "urn:one"));
		w.writeStartElement("mc", "Choice", MCE_NS);
		w.writeNamespace("p", "urn:one");
		w.writeEndElement();
		w.close();
		assertEquals(sw.toString(), 1, sw.toString().split("xmlns:p=").length - 1);
	}

	// ---- every marshal path (section 4.1)

	/** The paragraphs of the probe with a private prefix, in a package of their own. */
	private static WordprocessingMLPackage zz9Package() throws Exception {
		WordprocessingMLPackage source = load(PROBE);
		WordprocessingMLPackage dest = WordprocessingMLPackage.createPackage();
		for (Object p : paragraphs(source, ANY)) dest.getMainDocumentPart().getContent().add(p);
		return dest;
	}

	/** getXML() and XmlUtils' marshal to a String declare on the Choice too. */
	@Test
	public void marshalToStringDeclaresOnTheChoice() throws Exception {
		MainDocumentPart mdp = zz9Package().getMainDocumentPart();
		Found f = check(parse(mdp.getXML().getBytes(StandardCharsets.UTF_8)));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("zz9"));
		assertEquals("Choice", f.on("shape2010"));
		assertEquals("document", f.on("wps"));

		// the stream route writes the declaration the default path writes
		String whole = XmlUtils.marshaltoString(mdp.getJaxbElement(), false, false);
		assertTrue(whole.substring(0, 80), whole.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
		assertEquals(1, whole.split("<\\?xml").length - 1);

		// a fragment: the paragraph alone, its Choice naming zz9
		Object p = paragraphs(zz9Package(), ZZ9_ONLY).get(0);
		String xml = XmlUtils.marshaltoString(p, true, false);
		assertTrue(xml, xml.contains("<mc:Choice xmlns:zz9=\"" + ZZ9 + "\" Requires=\"zz9\">"));
		// and not pretty-printed inside w:t
		assertTrue(xml, xml.contains("<w:t>") || xml.contains("<w:t xml:space=\"preserve\">"));

		// the variant for an object without @XmlRootElement
		String xml2 = XmlUtils.marshaltoString(XmlUtils.unwrap(p), true, true, Context.jc, W_NS, "p", P.class);
		assertTrue(xml2, xml2.contains("xmlns:zz9=\"" + ZZ9 + "\" Requires=\"zz9\""));
	}

	/** A marshal to a DOM (the binder's, the XSLT pathways') declares on the Choice. */
	@Test
	public void marshalToDomDeclaresOnTheChoice() throws Exception {
		MainDocumentPart mdp = zz9Package().getMainDocumentPart();
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(mdp.getJaxbElement());
		Found f = check(doc.getDocumentElement());
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("zz9"));
		assertEquals("document", f.on("wps"));

		org.w3c.dom.Document doc2 = XmlUtils.neww3cDomDocument();
		mdp.marshal(doc2);
		f = check(doc2.getDocumentElement());
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("zz9"));

		// and the stream the pipe reads
		Found piped = check(parse(XmlUtils.marshaltoInputStream(mdp.getJaxbElement(), false, Context.jc).readAllBytes()));
		assertEquals("Choice", piped.on("zz9"));
	}

	/** The Flat OPC form (trimmed namespaces) keeps a Choice's own declaration. */
	@Test
	public void flatOpcDeclaresOnTheChoice() throws Exception {
		WordprocessingMLPackage pkg = zz9Package();
		FlatOpcXmlCreator creator = new FlatOpcXmlCreator(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		creator.marshal(os);
		Element flat = parse(os.toByteArray());
		Element document = (Element) flat.getElementsByTagNameNS(W_NS, "document").item(0);
		assertNotNull(document);
		Found f = check(document);
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals("Choice", f.on("zz9"));
		assertEquals("Choice", f.on("shape2010"));
	}

	/** The binder path (the XPath-aware part's eager binder) records the bindings from its DOM and declares them. */
	@Test
	public void theBinderPathRecordsAndDeclares() throws Exception {
		String key = "docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart";
		boolean before = Docx4jProperties.getProperty(key, false);
		Docx4jProperties.setProperty(key, true);
		try {
			WordprocessingMLPackage pkg = load(PROBE);
			pkg.getMainDocumentPart().getContent();
			assertNotNull("a binder", pkg.getMainDocumentPart().getBinder());
			List<AlternateContent.Choice> zz9 = choices(pkg.getMainDocumentPart().getJaxbElement(), ZZ9_ONLY);
			assertEquals(2, zz9.size());
			for (AlternateContent.Choice c : zz9) assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(c));
			Found f = check(save(pkg));
			assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
			assertEquals("Choice", f.on("zz9"));
			assertEquals("Choice", f.on("shape2010"));
			assertEquals("document", f.on("wps"));
		} finally {
			Docx4jProperties.setProperty(key, before);
		}
	}

	/** A document saved twice (the canonicalizing path) still declares where Word does. */
	@Test
	public void theCanonicalizingPathDeclares() throws Exception {
		boolean before = Docx4jProperties.getProperty("docx4j.jaxb.marshal.canonicalize", false);
		Docx4jProperties.setProperty("docx4j.jaxb.marshal.canonicalize", true);
		try {
			Found f = check(save(zz9Package()));
			assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
			assertEquals("Choice", f.on("zz9"));
			assertEquals("document", f.on("wps"));
		} finally {
			Docx4jProperties.setProperty("docx4j.jaxb.marshal.canonicalize", before);
		}
	}

	// ---- the mechanism (CR section 1.1, as it now is)

	/** What a load records: each Choice's bindings, and the root's. */
	@Test
	public void aLoadRecordsTheBindingsOnTheChoiceAndTheRoot() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		pkg.getMainDocumentPart().getContent();
		String table = new NamespacePrefixMappings().getNamespaceURI("zz9");
		assertTrue("the table has no zz9: " + table, table == null || table.isEmpty());

		List<AlternateContent.Choice> zz9 = choices(pkg.getMainDocumentPart().getJaxbElement(), ZZ9_ONLY);
		assertEquals(2, zz9.size());
		for (AlternateContent.Choice c : zz9) {
			assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(c));
			assertEquals(Collections.singletonMap("zz9", ZZ9), McSourceBindings.of(c));
		}
		AlternateContent.Choice both = choices(pkg.getMainDocumentPart().getJaxbElement(), c -> "wps zz9".equals(c.getRequires())).get(0);
		assertEquals(Arrays.asList(WPS, ZZ9), McSelection.requiresNamespaces(both));

		Map<String, String> root = McSourceBindings.of(pkg.getMainDocumentPart().getJaxbElement());
		assertNotNull("the root's Ignorable bindings", root);
		assertEquals("http://schemas.microsoft.com/office/word/2010/wordml", root.get("w14"));
		assertEquals("http://schemas.microsoft.com/office/word/2012/wordml", root.get("w15"));
	}

	/** A move keeps the Choice's record (the same object); a deep copy carries it (D2 (a)). */
	@Test
	public void aDeepCopyCarriesTheChoicesRecord() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		Object p = paragraphs(pkg, ZZ9_ONLY).get(0);
		AlternateContent.Choice moved = choices(XmlUtils.unwrap(p), ZZ9_ONLY).get(0);
		assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(moved));

		AlternateContent.Choice copied = choices(XmlUtils.unwrap(XmlUtils.deepCopy(p)), ZZ9_ONLY).get(0);
		assertEquals("zz9", copied.getRequires());
		assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(copied));

		// the element itself copied
		AlternateContent ac = (AlternateContent) moved.getParent();
		AlternateContent acCopy = XmlUtils.deepCopy(ac);
		assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(acCopy.getChoice().get(0)));
		// and a Choice on its own
		assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(XmlUtils.deepCopy(moved)));
		// and the root
		org.docx4j.wml.Document docCopy = XmlUtils.deepCopy(pkg.getMainDocumentPart().getJaxbElement());
		assertNotNull(McSourceBindings.of(docCopy));
	}

	/** The plan a marshal of the probe makes, read directly. */
	@Test
	public void thePlanPlacesEachPrefix() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		McDeclarations.Plan plan = McDeclarations.plan(mdp.getJaxbElement(), mdp.getMcChoiceNamespaces());
		assertTrue(plan.toString(), plan.declaresOnChoices());
		assertEquals(plan.toString(), Collections.emptySet(), plan.undeclared());
		assertTrue(plan.toString(), Arrays.asList(plan.tablePrefixes().split(" ")).containsAll(Arrays.asList("w14", "w15", "wps")));
		assertEquals(Collections.emptyMap(), plan.rootPairs());
		assertFalse(plan.onRoot().containsKey("zz9"));
		int onChoice = 0;
		for (AlternateContent.Choice c : choices(mdp.getJaxbElement(), ANY)) {
			Map<String, String> d = plan.onChoice(c);
			if (d == null) continue;
			onChoice++;
			for (String p : d.keySet()) assertTrue(p, p.equals("zz9") || p.equals("shape2010"));
		}
		assertEquals(4, onChoice);
	}
}
