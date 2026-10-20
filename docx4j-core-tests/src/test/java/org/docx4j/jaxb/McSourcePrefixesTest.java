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

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * CR-052 phase 0: where docx4j declares, today, the prefixes a part's {@code mc} attributes name
 * (the root's {@code mc:Ignorable}, a Choice's {@code Requires}, {@code mc:Ignorable} and
 * {@code mc:MustUnderstand}), as the CR's section 1 measured it.  Each case saves a package and
 * checks every such prefix of {@code word/document.xml} against the declarations in scope where it
 * is named.  A test whose result phase 1 changes says so ("Phase 1 changes this"), with what the
 * result becomes.
 *
 * <p>The mechanism (CR section 1.1): at unmarshal, {@link Docx4jUnmarshallerListener} adds each
 * Choice's prefixes, as strings, to the part ({@code addMcChoiceNamespace}), and records the URIs a
 * Choice's {@code Requires} resolved to on the Choice ({@link McSelection#requiresNamespaces}); at
 * marshal, the part's prefixes and its root's {@code mc:Ignorable} are declared on the root through
 * docx4j's prefix table alone.  So a prefix the table lacks is never declared once the part has been
 * unmarshalled; a part nobody unmarshalled is saved from its source bytes, which is why it keeps
 * every declaration; and content moved or copied into another part brings nothing with it, the
 * destination part's own prefixes being all its marshal declares.</p>
 */
public class McSourcePrefixesTest {

	private static final String MCE_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";
	private static final String ZZ9 = "http://example.com/zz9";
	private static final String WPS = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
	private static final String CX1 = "http://schemas.microsoft.com/office/drawing/2015/9/8/chartex";

	/** core-tests' text extraction probe: Choices naming zz9 (three) and shape2010 (one), each declared on its mc:AlternateContent. */
	private static final String PROBE = "text-extraction/extraction-probe.docx";
	/** Word 365's waterfall chart: a Choice Requires="cx1" over cx content, cx1 declared on the root only. */
	private static final String WATERFALL = "charts/waterfall.docx";

	private static final Predicate<AlternateContent.Choice> ZZ9_ONLY = c -> "zz9".equals(c.getRequires());
	private static final Predicate<AlternateContent.Choice> ANY = c -> true;

	/** What the check found in one part: each named prefix that resolves, and each that does not with how often. */
	private static final class Found {
		final Set<String> resolved = new TreeSet<String>();
		final Map<String, Integer> undeclared = new TreeMap<String, Integer>();
		final Element root;
		Found(Element root) {
			this.root = root;
		}
		@Override
		public String toString() {
			return "resolved " + resolved + ", undeclared " + undeclared;
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

	/** Every prefix an mc attribute of the saved main document part names, resolved where it is named. */
	private static Found check(byte[] docx) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Element root = dbf.newDocumentBuilder()
				.parse(new ByteArrayInputStream(entry(docx, "word/document.xml"))).getDocumentElement();
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
				if (el.lookupNamespaceURI(prefix) != null) found.resolved.add(prefix);
				else found.undeclared.merge(prefix, 1, Integer::sum);
			}
		}
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) walk((Element) n, found);
		}
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

	// ---- section 1, case 1: a part saved as loaded

	/** A part nobody unmarshalled is saved from its source bytes, so every declaration the source made stays where it was. */
	@Test
	public void aPartNobodyUnmarshalledKeepsEveryDeclaration() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		assertFalse("the load leaves the main part unread", pkg.getMainDocumentPart().isUnmarshalled());
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertTrue(f.toString(), f.resolved.containsAll(Arrays.asList("zz9", "shape2010", "wps", "w14")));
	}

	/**
	 * Once unmarshalled, the same part loses zz9 (three Choices) and shape2010 (one): the table knows
	 * neither prefix, and the URIs recorded on each Choice are not read at marshal.  wps and w14, which
	 * the table knows, are declared on the root.
	 *
	 * <p>Phase 1 changes this: all declared, zz9 and shape2010 on the Choice naming each, with the
	 * source's binding (D4 (c)).</p>
	 */
	@Test
	public void anUnmarshalledPartLosesThePrefixesTheTableLacks() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		pkg.getMainDocumentPart().getContent();
		Found f = check(save(pkg));
		assertEquals(f.toString(), counts("shape2010", 1, "zz9", 3), f.undeclared);
		assertTrue(f.toString(), f.resolved.containsAll(Arrays.asList("wps", "w14")));
		assertEquals(WPS, f.root.lookupNamespaceURI("wps"));
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
	 * root keeps {@code mc:Ignorable="zz9"} and declares nothing for it.
	 *
	 * <p>Phase 1 changes this, where {@code XmlUtils}' unmarshal sets docx4j's listener (D5): declared
	 * on the root.</p>
	 */
	@Test
	public void aRootIgnorablePrefixSetFromCodeIsUndeclared() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(rootIgnorableZz9()));
		Found f = check(save(pkg));
		assertEquals(f.toString(), counts("zz9", 1), f.undeclared);
	}

	/**
	 * Case 2b: the same part loaded from a package.  Saved unread, declared (its bytes); unmarshalled
	 * and saved, undeclared, as case 2a: the part records the root's Ignorable prefixes, not their
	 * bindings.
	 *
	 * <p>Phase 1 changes the second: declared on the root.</p>
	 */
	@Test
	public void aRootIgnorablePrefixLoadedIsLostOnceUnmarshalled() throws Exception {
		byte[] source = withDocument(rootIgnorableZz9());
		Found unread = check(save(WordprocessingMLPackage.load(new ByteArrayInputStream(source))));
		assertEquals(unread.toString(), Collections.emptyMap(), unread.undeclared);
		assertEquals(ZZ9, unread.root.lookupNamespaceURI("zz9"));

		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(source));
		pkg.getMainDocumentPart().getContent();
		Found f = check(save(pkg));
		assertEquals(f.toString(), counts("zz9", 1), f.undeclared);
		assertEquals("the Ignorable value itself is kept", "zz9",
				f.root.getAttributeNS(MCE_NS, "Ignorable").trim().split("\\s+")[0]);
	}

	// ---- section 1, case 3: a Choice built in code

	/**
	 * A Choice built in code saying {@code Requires="cx1"} is not declared, though the table knows
	 * cx1: no listener saw it, so the part's prefixes do not include it.
	 *
	 * <p>Phase 1 changes this: declared on the root (a table prefix, D4 (c)).</p>
	 */
	@Test
	public void aChoiceBuiltInCodeIsUndeclaredThoughTheTableKnowsIt() throws Exception {
		assertEquals(CX1, new NamespacePrefixMappings().getNamespaceURI("cx1"));
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(
				"<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:mc=\"" + MCE_NS + "\" xmlns:cx1=\"" + CX1 + "\">"
				+ "<w:r><mc:AlternateContent><mc:Choice Requires=\"cx1\"><w:t>c</w:t></mc:Choice>"
				+ "<mc:Fallback><w:t>f</w:t></mc:Fallback></mc:AlternateContent></w:r></w:p>"));
		Found f = check(save(pkg));
		assertEquals(f.toString(), counts("cx1", 1), f.undeclared);
	}

	// ---- section 1, case 4: content moved or copied into another package

	/**
	 * Cases 4a and 4b: the probe's two zz9-only paragraphs moved (the same objects) and deep-copied
	 * into a new package: zz9 undeclared both ways.
	 *
	 * <p>Phase 1 changes this: declared on each Choice, with the source's binding.</p>
	 */
	@Test
	public void zz9MovedOrCopiedIsUndeclared() throws Exception {
		for (boolean copy : new boolean[] { false, true }) {
			WordprocessingMLPackage source = load(PROBE);
			WordprocessingMLPackage dest = WordprocessingMLPackage.createPackage();
			List<Object> ps = paragraphs(source, ZZ9_ONLY);
			assertEquals(2, ps.size());
			for (Object p : ps) dest.getMainDocumentPart().getContent().add(copy ? XmlUtils.deepCopy(p) : p);
			Found f = check(save(dest));
			assertEquals((copy ? "copied: " : "moved: ") + f, counts("zz9", 2), f.undeclared);
		}
	}

	/**
	 * Case 4c: every paragraph holding a Choice deep-copied into a new package.  wps and w14 are
	 * declared (createPackage's part predeclares both); zz9 and shape2010 are not.
	 *
	 * <p>Phase 1 changes this: all declared.</p>
	 */
	@Test
	public void everyChoiceCopiedKeepsOnlyThePredeclaredPrefixes() throws Exception {
		WordprocessingMLPackage source = load(PROBE);
		WordprocessingMLPackage dest = WordprocessingMLPackage.createPackage();
		for (Object p : paragraphs(source, ANY)) dest.getMainDocumentPart().getContent().add(XmlUtils.deepCopy(p));
		Found f = check(save(dest));
		assertEquals(f.toString(), counts("shape2010", 1, "zz9", 3), f.undeclared);
		assertTrue(f.toString(), f.resolved.containsAll(Arrays.asList("wps", "w14")));
	}

	// ---- section 2's Office case: Word's chartex chart

	/** Word's chartex document, unmarshalled and saved: cx1 is declared on the root (the listener saw the Choice; the table knows cx1). */
	@Test
	public void wordsChartexChartResavedKeepsCx1() throws Exception {
		WordprocessingMLPackage pkg = load(WATERFALL);
		assertEquals(1, choices(pkg.getMainDocumentPart().getJaxbElement(), c -> "cx1".equals(c.getRequires())).size());
		Found f = check(save(pkg));
		assertEquals(f.toString(), Collections.emptyMap(), f.undeclared);
		assertEquals(CX1, f.root.lookupNamespaceURI("cx1"));
	}

	/**
	 * The chart's paragraph deep-copied into a new package, and moved into another loaded document
	 * (the probe, a merge): cx1 is undeclared both ways, the destination part's prefixes being all its
	 * marshal declares.  (Only the namespaces are checked: the chart part itself is not carried.)
	 *
	 * <p>Phase 1 changes this: declared on the root.</p>
	 */
	@Test
	public void wordsChartexChartCopiedOrMergedLosesCx1() throws Exception {
		WordprocessingMLPackage source = load(WATERFALL);
		WordprocessingMLPackage dest = WordprocessingMLPackage.createPackage();
		for (Object p : paragraphs(source, ANY)) dest.getMainDocumentPart().getContent().add(XmlUtils.deepCopy(p));
		Found copied = check(save(dest));
		assertEquals(copied.toString(), counts("cx1", 1), copied.undeclared);

		source = load(WATERFALL);
		WordprocessingMLPackage probe = load(PROBE);
		for (Object p : paragraphs(source, ANY)) probe.getMainDocumentPart().getContent().add(p);
		Found merged = check(save(probe));
		assertEquals(merged.toString(), counts("cx1", 1, "shape2010", 1, "zz9", 3), merged.undeclared);
	}

	// ---- the mechanism (CR section 1.1)

	/**
	 * What a load records: the part keeps the Choices' prefixes as strings, which the table resolves
	 * at marshal, and each Choice the URIs its Requires resolved to, which nothing reads at marshal.
	 * The table has no zz9.
	 */
	@Test
	public void aLoadRecordsPrefixesOnThePartAndBindingsOnTheChoice() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		pkg.getMainDocumentPart().getContent();
		List<String> partPrefixes = Arrays.asList(pkg.getMainDocumentPart().getMcChoiceNamespaces().trim().split("\\s+"));
		assertTrue(partPrefixes.toString(), partPrefixes.containsAll(Arrays.asList("zz9", "shape2010", "wps", "w14")));
		String table = new NamespacePrefixMappings().getNamespaceURI("zz9");
		assertTrue("the table has no zz9: " + table, table == null || table.isEmpty());

		List<AlternateContent.Choice> zz9 = choices(pkg.getMainDocumentPart().getJaxbElement(), ZZ9_ONLY);
		assertEquals(2, zz9.size());
		for (AlternateContent.Choice c : zz9) assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(c));
	}

	/**
	 * A move keeps the Choice's record (the same object); a deep copy does not, so the copy's Requires
	 * resolves through the table, to nothing for zz9.
	 *
	 * <p>Phase 1 changes the copy (D2 (a)): the copy carries the record.</p>
	 */
	@Test
	public void aDeepCopyLosesTheChoicesRecord() throws Exception {
		WordprocessingMLPackage pkg = load(PROBE);
		Object p = paragraphs(pkg, ZZ9_ONLY).get(0);
		AlternateContent.Choice moved = choices(XmlUtils.unwrap(p), ZZ9_ONLY).get(0);
		assertEquals(Collections.singletonList(ZZ9), McSelection.requiresNamespaces(moved));

		AlternateContent.Choice copied = choices(XmlUtils.unwrap(XmlUtils.deepCopy(p)), ZZ9_ONLY).get(0);
		assertEquals("zz9", copied.getRequires());
		assertNull(McSelection.requiresNamespaces(copied).get(0));
	}
}
