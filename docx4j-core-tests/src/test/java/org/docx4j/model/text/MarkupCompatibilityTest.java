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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * Markup compatibility for text (WD section 3; CR-046 section 2.2): {@link McPurpose#TEXT} over the
 * published understood set, matched by URI.
 */
public class MarkupCompatibilityTest {

	private static final String W14 = "http://schemas.microsoft.com/office/word/2010/wordml";

	private static String ac(String attrs, String branches) {
		return "<w:p><w:r><w:t xml:space=\"preserve\">Before </w:t></w:r><mc:AlternateContent" + attrs + ">" + branches + "</mc:AlternateContent></w:p>";
	}

	private static String choice(String requires, String text) {
		return "<mc:Choice Requires=\"" + requires + "\"><w:r><w:t>" + text + "</w:t></w:r></mc:Choice>";
	}

	private static String fallback(String text) {
		return "<mc:Fallback><w:r><w:t>" + text + "</w:t></w:r></mc:Fallback>";
	}

	private static String text(WordprocessingMLPackage pkg) {
		return PlainText.of(Texts.of(pkg, new TextOptions()));
	}

	private static AlternateContent firstAc(WordprocessingMLPackage pkg) {
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		for (Object o : p.getContent()) {
			Object v = o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o;
			if (v instanceof AlternateContent) return (AlternateContent) v;
		}
		throw new AssertionError("no mc:AlternateContent");
	}

	/** REQ-301, REQ-302: one branch, the first Choice understood; the Fallback otherwise. */
	@Test
	public void firstUnderstoodChoiceElseFallback() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(ac(" xmlns:zz=\"urn:zz\"",
				choice("zz", "UNKNOWN") + choice("w14", "FIRST") + choice("wps", "SECOND") + fallback("FALLBACK"))).load();
		assertEquals("Before FIRST\n", text(pkg));
		pkg = MiniDocx.body(ac(" xmlns:zz=\"urn:zz\"", choice("zz", "UNKNOWN") + fallback("FALLBACK"))).load();
		assertEquals("Before FALLBACK\n", text(pkg));
		// every prefix must be understood
		pkg = MiniDocx.body(ac(" xmlns:zz=\"urn:zz\"", choice("w14 zz", "BOTH") + fallback("FALLBACK"))).load();
		assertEquals("Before FALLBACK\n", text(pkg));
	}

	/** REQ-303: by URI, through the declarations in force at the element, recorded at load. */
	@Test
	public void requiresMatchedByUri() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(ac(" xmlns:wordml2010=\"" + W14 + "\"", choice("wordml2010", "REBOUND") + fallback("FALLBACK"))).load();
		assertEquals("Before REBOUND\n", text(pkg));
		AlternateContent.Choice loaded = firstAc(pkg).getChoice().get(0);
		assertEquals(Arrays.asList(W14), McSelection.requiresNamespaces(loaded));
		// a w14 prefix bound to another namespace is not w14
		pkg = MiniDocx.body(ac(" xmlns:w14=\"urn:not-w14\"", choice("w14", "IMPOSTOR") + fallback("FALLBACK"))).load();
		assertEquals("Before FALLBACK\n", text(pkg));
		// with no declarations to ask (a Choice built in code), the conventional prefix table, as REQ-303 permits
		AlternateContent.Choice built = new AlternateContent.Choice();
		built.setRequires("wordml2010");
		assertEquals(Arrays.asList((String) null), McSelection.requiresNamespaces(built));
		assertFalse(McSelection.understands(built));
		built.setRequires("w14");
		assertTrue(McSelection.understands(built));
	}

	/** REQ-304: an empty or whitespace-only Requires is not understood. */
	@Test
	public void emptyRequiresNotUnderstood() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(ac("", choice("", "EMPTY") + choice("   ", "BLANK") + fallback("FALLBACK"))).load();
		assertEquals("Before FALLBACK\n", text(pkg));
		AlternateContent.Choice built = new AlternateContent.Choice();
		built.setRequires(" ");
		assertTrue(McSelection.requiresNamespaces(built).isEmpty());
		assertFalse(McSelection.understands(built));
	}

	/** REQ-306: a namespace Word was measured to understand, in the published set, takes its Choice over the Fallback, by URI. */
	@Test
	public void namespacesWordUnderstandsTakeTheChoice() throws Exception {
		String[][] measured = {
				{ "wpi", "http://schemas.microsoft.com/office/word/2010/wordprocessingInk" },
				{ "oel", "http://schemas.microsoft.com/office/2019/extlst" },
				{ "cx3", "http://schemas.microsoft.com/office/drawing/2016/5/9/chartex" },
				{ "cx4", "http://schemas.microsoft.com/office/drawing/2016/5/10/chartex" },
				{ "cx6", "http://schemas.microsoft.com/office/drawing/2016/5/12/chartex" },
				{ "cx7", "http://schemas.microsoft.com/office/drawing/2016/5/13/chartex" },
				{ "c16r2", "http://schemas.microsoft.com/office/drawing/2015/06/chart" },
				{ "ax", "http://schemas.microsoft.com/office/2006/activeX" },
				{ "xsi", "http://www.w3.org/2001/XMLSchema-instance" } };
		for (String[] ns : measured) {
			assertTrue(ns[0], McSelection.understoodNamespaces().contains(ns[1]));
			WordprocessingMLPackage pkg = MiniDocx.body(ac(" xmlns:" + ns[0] + "=\"" + ns[1] + "\"", choice(ns[0], "CHOICE") + fallback("FALLBACK"))).load();
			assertEquals(ns[0], "Before CHOICE\n", text(pkg));
			AlternateContent ac = firstAc(pkg);
			assertTrue(ns[0], McSelection.selectedBranch(McPurpose.DRAW, ac) == ac.getFallback()); // drawing keeps its own rule
		}
		// measured as ones Word doesn't understand, cx5 and cx8 though it declares them on its roots: the Fallback
		for (String uri : new String[] { "http://schemas.microsoft.com/office/drawing/2016/5/11/chartex", "http://schemas.microsoft.com/office/drawing/2016/5/14/chartex",
				"http://schemas.microsoft.com/office/mac/office/2008/main", "http://schemas.microsoft.com/office/word/2006/arto", "http://purl.org/dc/dcmitype/",
				// and three of the 13 a reader can traverse in their own parts
				"http://purl.org/dc/elements/1.1/", "urn:schemas-microsoft-com:office:excel", "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" }) {
			assertFalse(uri, McSelection.understoodNamespaces().contains(uri));
			assertEquals(uri, "Before FALLBACK\n", text(MiniDocx.body(ac(" xmlns:qq=\"" + uri + "\"", choice("qq", "CHOICE") + fallback("FALLBACK"))).load()));
		}
		// a different prefix for the same namespace is the same namespace, and the same prefix for another isn't (REQ-303)
		WordprocessingMLPackage pkg = MiniDocx.body(ac(" xmlns:qq=\"" + measured[0][1] + "\"", choice("qq", "CHOICE") + fallback("FALLBACK"))).load();
		assertEquals("Before CHOICE\n", text(pkg));
		pkg = MiniDocx.body(ac(" xmlns:wpi=\"urn:zz\"", choice("wpi", "CHOICE") + fallback("FALLBACK"))).load();
		assertEquals("Before FALLBACK\n", text(pkg));
	}

	/** REQ-305: Choices only, none understood: the element is kept whole at load, and its first Choice read. */
	@Test
	public void choicesOnlyReadFromTheFirst() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(ac(" xmlns:zz=\"urn:zz\" xmlns:yy=\"urn:yy\"", choice("zz", "FIRST") + choice("yy", "SECOND"))).load();
		assertEquals("Before FIRST\n", text(pkg));
		AlternateContent kept = firstAc(pkg);
		assertEquals(2, kept.getChoice().size());
		assertNull(kept.getFallback());
		assertTrue(McSelection.selectedBranch(McPurpose.TEXT, kept) == kept.getChoice().get(0));
	}

	/** REQ-307: both branches hold text and differ: a warning at the address, naming the branch, and no text (REQ-802). */
	@Test
	public void branchesDiffer() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(ac("", choice("w14", "CHOICE_TEXT") + fallback("FALLBACK_TEXT"))).load();
		List<TextWarning> warnings = Texts.of(pkg, new TextOptions()).warnings();
		assertEquals(1, warnings.size());
		TextWarning w = warnings.get(0);
		assertEquals(TextWarning.MCE_BRANCHES_DIFFER, w.getKind());
		assertEquals("body/0", w.getAddress());
		assertEquals("mc:Choice Requires=\"w14\"", w.getBranch());
		assertFalse(w.toString().contains("CHOICE_TEXT") || w.toString().contains("FALLBACK_TEXT"));
		// texts equal after REQ-1201's normalisation: no warning
		pkg = MiniDocx.body(ac("", choice("w14", "SAME_TEXT") + fallback("SAME TEXT"))).load();
		assertTrue(Texts.of(pkg, new TextOptions()).warnings().isEmpty());
		// a branch with no text: no warning
		pkg = MiniDocx.body(ac("", choice("w14", "ONLY") + "<mc:Fallback/>")).load();
		assertTrue(Texts.of(pkg, new TextOptions()).warnings().isEmpty());
		// warnings: false may skip the comparison
		pkg = MiniDocx.body(ac("", choice("w14", "CHOICE_TEXT") + fallback("FALLBACK_TEXT"))).load();
		assertTrue(Texts.of(pkg, new TextOptions().warnings(false)).warnings().isEmpty());
	}

	/** Drawing keeps its own rule: the Fallback, with docx4j.jaxb.mc.preferChoice empty (CR-021). */
	@Test
	public void drawingUnchanged() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(ac("", choice("w14", "CHOICE") + fallback("FALLBACK"))).load();
		AlternateContent ac = firstAc(pkg);
		assertTrue(McSelection.selectedBranch(McPurpose.DRAW, ac) == ac.getFallback());
		assertTrue(McSelection.selectedBranch(ac) == ac.getFallback());
		assertTrue(McSelection.selectedBranch(McPurpose.TEXT, ac) == ac.getChoice().get(0));
		assertEquals(2, McSelection.branches(McPurpose.ALL, ac).size());
	}
}
