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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.model.text.Inspect;
import org.docx4j.model.text.Inspection;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Tbl;
import org.junit.Test;

/**
 * A main document part whose root isn't {@code w:document} (CR-079 D9; WD REQ-318 to REQ-321). The files are {@code mc-root/}, with
 * Word's saves of those it opens as {@code -word365.docx}.
 */
public class MainPartRootTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String ONE_P = "<w:p><w:r><w:t>WORDS</w:t></w:r></w:p>";

	/** Word reads each as a document (REQ-319, and REQ-317 for mr-40). */
	private static final String[] READ = { "mr-01-madeup-p", "mr-10-two-p", "mr-11-p-sectpr", "mr-12-tbl", "mr-13-sdt", "mr-15-body", "mr-16-empty", "mr-17-nested",
			"mr-18-p-properties", "mr-30-sdtcontent-p", "mr-32-p-root", "mr-40-branch-madeup-p", "mr-50-madeup-p-styles", "mu-12-foo-holding-p", "or-31-madeup", "or-32-body" };

	/** Word refuses each (REQ-318). */
	private static final String[] REFUSED = { "mr-03-foo-document", "mr-14-run", "mr-20-other-namespace-p", "mr-21-no-namespace-p", "mr-31-tc-p",
			"mu-10-madeup-one-document", "mu-11-madeup-two-documents", "or-12-footnotes", "or-13-endnotes", "or-15-footnotes-bare",
			"rw-10-foo-two-documents", "rw-11-zfoo-two-documents", "rw-12-wfoo-two-documents", "rw-13-foo-one-document" };

	/** Word opens each and shows nothing of it (REQ-320), and the root each names. or-14 is through Word's repair. */
	private static final String[][] BLANK = { { "or-10-hdr", "w:hdr" }, { "or-11-ftr", "w:ftr" }, { "or-14-comments", "w:comments" }, { "or-30-styles", "w:styles" },
			{ "or-50-mc-hdr-choice", "w:hdr" }, { "mr-22-ignorable-p", "z:madeUp" } };

	private static File file(String name) throws Exception {
		return new File(MainPartRootTest.class.getClassLoader().getResource("mc-root/" + name + ".docx").toURI());
	}

	private static WordprocessingMLPackage load(String name) throws Exception {
		return WordprocessingMLPackage.load(file(name));
	}

	private static String text(WordprocessingMLPackage pkg) {
		return Texts.plainText(pkg, new TextOptions()).trim();
	}

	private static byte[] saved(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		return out.toByteArray();
	}

	private static byte[] entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				if (e.getName().equals(name)) return in.readAllBytes();
			}
		}
		throw new AssertionError(name + " is not in the package");
	}

	/** The file with one entry replaced, for a shape no file of Word's round has. */
	private static byte[] rewritten(String source, String name, String xml) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipOutputStream z = new ZipOutputStream(out); ZipInputStream in = new ZipInputStream(new FileInputStream(file(source)))) {
			for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
				z.putNextEntry(new ZipEntry(e.getName()));
				z.write(e.getName().equals(name) ? xml.getBytes("UTF-8") : in.readAllBytes());
				z.closeEntry();
			}
		}
		return out.toByteArray();
	}

	private static WordprocessingMLPackage withRoot(String xml) throws Exception {
		return WordprocessingMLPackage.load(new ByteArrayInputStream(rewritten("mr-01-madeup-p", "word/document.xml", xml)));
	}

	private static List<TextWarning> unexpected(TextResult r) {
		List<TextWarning> found = new ArrayList<TextWarning>();
		for (TextWarning w : r.warnings()) if (TextWarning.PART_UNEXPECTED_ROOT.equals(w.getKind())) found.add(w);
		return found;
	}

	private static void assertRead(String words, WordprocessingMLPackage pkg) {
		assertEquals(words, text(pkg));
		assertNull(pkg.getMainDocumentPart().getUnexpectedRoot());
	}

	private static void assertBlank(String root, WordprocessingMLPackage pkg) {
		TextResult r = Texts.of(pkg, new TextOptions());
		assertEquals(root, "", PlainText.of(r).trim());
		List<TextWarning> warnings = unexpected(r);
		assertEquals(root, 1, warnings.size());
		assertEquals("/word/document.xml", warnings.get(0).getSelector());
		assertEquals(root, warnings.get(0).getDetail());
		assertNull(warnings.get(0).getAddress());
	}

	private static void assertRefused(WordprocessingMLPackage pkg) {
		try {
			text(pkg);
			fail("read");
		} catch (Docx4JRuntimeException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("/word/document.xml"));
		}
		assertTrue(pkg.getMainDocumentPart().isContentUnreadable());
		assertNull(pkg.getMainDocumentPart().getUnexpectedRoot());
	}

	@Test
	public void aRootWordReadsReadsAsItsSave() throws Exception {
		for (String name : READ) {
			WordprocessingMLPackage pkg = load(name);
			assertEquals(name, text(load(name + "-word365")), text(pkg));
			assertFalse(name, pkg.getMainDocumentPart().isContentUnreadable());
			assertTrue(name, unexpected(Texts.of(pkg, new TextOptions())).isEmpty());
		}
		assertEquals("FIRST WORDS\nSECOND WORDS", text(load("mr-10-two-p")).replace("\r", "").replaceAll("\n+", "\n"));
		assertEquals("NESTED WORDS", text(load("mr-17-nested")));
		assertEquals("SDTCONTENT WORDS", text(load("mr-30-sdtcontent-p")));
		assertEquals("CHOICE WORDS", text(load("mr-40-branch-madeup-p")));
	}

	/** A part that was read saves as a document, which reads the same. */
	@Test
	public void aRootReadSavesAsADocument() throws Exception {
		for (String name : READ) {
			WordprocessingMLPackage pkg = load(name);
			String words = text(pkg);
			byte[] zip = saved(pkg);
			String xml = new String(entry(zip, "word/document.xml"), "UTF-8");
			assertTrue(name + " " + xml, xml.contains("<w:document"));
			assertFalse(name + " " + xml, xml.contains("madeUp") || xml.contains("<w:foo") || xml.contains("<w:sdtContent>") && name.startsWith("mr-30"));
			assertEquals(name, words, text(WordprocessingMLPackage.load(new ByteArrayInputStream(zip))));
		}
	}

	/** A w:sectPr directly in the root has no effect, one in its w:body does; what the root holds stands as it stood. */
	@Test
	public void whatTheRootHoldsKeepsItsShape() throws Exception {
		Body sectionLost = load("mr-11-p-sectpr").getMainDocumentPart().getContents().getBody();
		assertNull(sectionLost.getSectPr());
		Body sectionRead = load("mr-15-body").getMainDocumentPart().getContents().getBody();
		assertEquals(STPageOrientation.LANDSCAPE, sectionRead.getSectPr().getPgSz().getOrient());

		assertTrue(XmlUtils.unwrap(load("mr-12-tbl").getMainDocumentPart().getContent().get(0)) instanceof Tbl);
		assertTrue(XmlUtils.unwrap(load("mr-13-sdt").getMainDocumentPart().getContent().get(0)) instanceof SdtBlock);
		P properties = (P) load("mr-18-p-properties").getMainDocumentPart().getContent().get(0);
		assertNotNull(properties.getPPr().getJc());
		P styled = (P) load("mr-50-madeup-p-styles").getMainDocumentPart().getContent().get(0);
		assertNotNull(styled.getPPr().getPStyle());
		assertTrue(load("mr-16-empty").getMainDocumentPart().getContent().isEmpty());
	}

	/** REQ-318, and a save keeps the bytes (CR-065 D1). */
	@Test
	public void aRootWordRefusesIsRefused() throws Exception {
		for (String name : REFUSED) {
			WordprocessingMLPackage pkg = load(name);
			assertRefused(pkg);
			assertArrayEquals(name, entry(java.nio.file.Files.readAllBytes(file(name).toPath()), "word/document.xml"), entry(saved(pkg), "word/document.xml"));
		}
	}

	/** REQ-318's roots no file of the rounds here has. */
	@Test
	public void aRootRefusedByNameIsRefused() throws Exception {
		assertRefused(withRoot("<wordDocument xmlns=\"http://schemas.microsoft.com/office/word/2003/wordml\"/>"));
		assertRefused(withRoot("<pkg:package xmlns:pkg=\"http://schemas.microsoft.com/office/2006/xmlPackage\"/>"));
	}

	/** REQ-320: no text and the warning, and everything else takes CR-065 D1's route. */
	@Test
	public void aRootWordShowsNothingOfGivesNoText() throws Exception {
		for (String[] blank : BLANK) {
			WordprocessingMLPackage pkg = load(blank[0]);
			assertBlank(blank[1], pkg);
			assertEquals(blank[1], pkg.getMainDocumentPart().getUnexpectedRoot());
			assertArrayEquals(blank[0], entry(java.nio.file.Files.readAllBytes(file(blank[0]).toPath()), "word/document.xml"), entry(saved(pkg), "word/document.xml"));
			try {
				pkg.getMainDocumentPart().getContent();
				fail("content");
			} catch (Docx4JRuntimeException refused) {
				assertTrue(refused.getMessage(), refused.getMessage().contains("could not be read"));
			}
			try {
				TextUtils.extractText(pkg.getMainDocumentPart().getJaxbElement(), new java.io.StringWriter());
				fail("TextUtils");
			} catch (Docx4JException refused) {
				assertTrue(refused.getMessage(), refused.getMessage().contains("null"));
			}
		}
	}

	/** Two templates of one pattern don't fail: the second just wins, and a second match="/" took CR-073's and CR-076's warnings with it. */
	@Test
	public void theStylesheetHasOneTemplateAPattern() throws Exception {
		org.w3c.dom.Document xslt;
		try (java.io.InputStream is = MainPartRootTest.class.getClassLoader().getResourceAsStream("org/docx4j/jaxb/mc-preprocessor.xslt")) {
			xslt = XmlUtils.getNewDocumentBuilder().parse(is);
		}
		java.util.Set<String> seen = new java.util.HashSet<String>();
		org.w3c.dom.NodeList templates = xslt.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "template");
		for (int i = 0; i < templates.getLength(); i++) {
			org.w3c.dom.Element t = (org.w3c.dom.Element) templates.item(i);
			if (!t.hasAttribute("match")) continue;
			assertTrue(t.getAttribute("match"), seen.add(t.getAttribute("match") + " " + t.getAttribute("priority") + " " + t.getAttribute("mode")));
		}
	}

	/** And what that template does, by its warnings, as a part reaches it. */
	@Test
	public void theStylesheetStillWarnsOncePerPart() throws Exception {
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.docx4j.utils.XSLTUtils.class);
		ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
		appender.start();
		logger.addAppender(appender);
		try {
			text(withRoot("<w:document " + W + "><w:body><w:p><w:br/><w:madeUp><w:r><w:t>WORDS</w:t></w:r></w:madeUp></w:p></w:body></w:document>"));
		} finally {
			logger.detachAppender(appender);
		}
		StringBuilder messages = new StringBuilder();
		for (ch.qos.logback.classic.spi.ILoggingEvent e : appender.list) messages.append(e.getFormattedMessage()).append('\n');
		assertTrue(messages.toString(), messages.toString().contains("w:br outside a w:r"));
		assertTrue(messages.toString(), messages.toString().contains("Read 1 element(s) Word doesn't know"));
	}

	/** An XPath as the first use: the binder's DOM has to hold the document, not the root that stood in its place. */
	@Test
	public void anXPathFirstFindsTheDocument() throws Exception {
		String eager = "docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart";
		String before = Docx4jProperties.getProperty(eager);
		try {
			for (String setting : new String[] { "true", "false" }) {
				Docx4jProperties.setProperty(eager, setting);
				for (String name : new String[] { "or-32-body", "mr-32-p-root" }) {
					MainDocumentPart main = load(name).getMainDocumentPart();
					List<Object> ps = main.getJAXBNodesViaXPath("//w:p", false);
					assertEquals(name, 1, ps.size());
					assertTrue(name, main.getContent().contains(XmlUtils.unwrap(ps.get(0))));
				}
			}
		} finally {
			Docx4jProperties.setProperty(eager, before == null ? "false" : before);
		}
	}

	private static final String MC = "xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	private static WordprocessingMLPackage inAChoice(String held) throws Exception {
		return withRoot("<mc:AlternateContent " + MC + " " + W + "><mc:Choice Requires=\"w14\">" + held + "</mc:Choice></mc:AlternateContent>");
	}

	/** A bound element that can't be marshalled on its own is named all the same, where the read threw, and only the second call refused. */
	@Test
	public void aBranchHoldingWhatCantBeMarshalledIsBlank() throws Exception {
		assertBlank("w:txbxContent", inAChoice("<w:txbxContent>" + ONE_P + "</w:txbxContent>"));
	}

	/** REQ-801: the root's name, not the private one a w:sdt directly in a branch has while it's read. */
	@Test
	public void aBranchSdtKeepsItsName() throws Exception {
		assertBlank("w:sdt", inAChoice("<w:sdt><w:sdtContent>" + ONE_P + "</w:sdtContent></w:sdt>"));
	}

	/** REQ-319's list as written: mc:AlternateContent isn't on it, and a w:sdt holding runs isn't a block one. Neither reads, and neither loses words on a save. */
	@Test
	public void theListIsREQ319s() throws Exception {
		assertBlank("w:madeUp", withRoot("<w:madeUp " + MC + " " + W + "><mc:AlternateContent><mc:Choice Requires=\"w14\">" + ONE_P
				+ "</mc:Choice><mc:Fallback>" + ONE_P + "</mc:Fallback></mc:AlternateContent></w:madeUp>"));
		WordprocessingMLPackage runs = withRoot("<w:madeUp " + W + "><w:sdt><w:sdtContent><w:r><w:t>WORDS</w:t></w:r></w:sdtContent></w:sdt></w:madeUp>");
		assertBlank("w:madeUp", runs);
		assertTrue(new String(entry(saved(runs), "word/document.xml"), "UTF-8").contains("WORDS"));
	}

	/** REQ-317 brings REQ-307 to the root: branches that differ are told, by the part, as a root has no address. */
	@Test
	public void branchesThatDifferAtTheRootAreTold() throws Exception {
		boolean told = false;
		for (TextWarning w : Texts.of(load("rw-20-mc-root-choice"), new TextOptions()).warnings()) {
			if (!TextWarning.MCE_BRANCHES_DIFFER.equals(w.getKind())) continue;
			told = true;
			assertEquals("mc:Choice Requires=\"w14\"", w.getSelector());
			assertEquals("/word/document.xml", w.getPartName());
			assertNull(w.getAddress());
		}
		assertTrue(told);
	}

	/** And an inspection finds the branch not read, which it passed over. */
	@Test
	public void anInspectionFindsTheBranchNotRead() throws Exception {
		Inspection i = Inspect.of(load("rw-20-mc-root-choice"));
		boolean found = false;
		for (org.docx4j.model.text.InspectFinding f : i.findings()) found |= "mce.branch".equals(f.getKind()) && f.getExcerpt().contains("FALLBACK WORDS");
		assertTrue(i.findings().toString(), found);
		assertEquals(Inspection.Status.COMPLETE, i.status());
	}

	/** What a join moves has the document it moved to as its parent. */
	@Test
	public void aJoinReparents() throws Exception {
		org.docx4j.wml.Document background = load("c79-20-two-docs-background-first").getMainDocumentPart().getContents();
		assertEquals(background, background.getBackground().getParent());
		String first = "<w:document><w:body>" + ONE_P + "<w:sectPr/></w:body></w:document>";
		String second = "<w:document><w:body>" + ONE_P + "</w:body></w:document>";
		org.docx4j.wml.Document joined = inAChoice(first + second).getMainDocumentPart().getContents();
		assertEquals(joined.getBody(), joined.getBody().getSectPr().getParent());
	}

	/** A caller's replacement content ends the state, as it ends the failed read's. */
	@Test
	public void replacingTheContentEndsIt() throws Exception {
		WordprocessingMLPackage pkg = load("or-10-hdr");
		text(pkg);
		assertEquals("w:hdr", pkg.getMainDocumentPart().getUnexpectedRoot());
		pkg.getMainDocumentPart().setJaxbElement(load("mr-01-madeup-p").getMainDocumentPart().getContents());
		assertRead("MADEUP WORDS", pkg);
	}

	/** A w:document or mc:AlternateContent whose read fails is no unexpected root: the root isn't why. */
	@Test
	public void anOrdinaryFailureIsNoUnexpectedRoot() throws Exception {
		JaxbXmlPart<?> plain = load("mr-01-madeup-p").getMainDocumentPart();
		assertEquals("w:madeUp", plain.unexpectedRootOf(new jakarta.xml.bind.JAXBException("another reason")));
		for (String root : new String[] { "<w:document " + W + "/>",
				"<mc:AlternateContent xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"/>" }) {
			JaxbXmlPart<?> part = withRoot(root).getMainDocumentPart();
			assertNull(root, part.unexpectedRootOf(new jakarta.xml.bind.JAXBException("another reason")));
		}
	}

	/** D9: the message names the root, where the first use was a ClassCastException. */
	@Test
	public void theMessageNamesTheRoot() throws Exception {
		WordprocessingMLPackage pkg = load("or-10-hdr");
		try {
			pkg.getMainDocumentPart().getContent();
			fail("content");
		} catch (Docx4JRuntimeException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("has a root it can't hold, w:hdr"));
		}
	}

	/** REQ-319's list closes: an unknown root holding anything else is REQ-320's, as is a root in an understood namespace or holding neither of REQ-318's. */
	@Test
	public void aRootOutsideEveryListIsBlank() throws Exception {
		assertBlank("w:madeUp", withRoot("<w:madeUp " + W + "><w:rPr/></w:madeUp>"));
		assertBlank("w:madeUp", withRoot("<w:madeUp " + W + ">" + ONE_P + "<w:rPr/></w:madeUp>"));
		assertBlank("w14:madeUp", withRoot("<w14:madeUp xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\" " + W + ">" + ONE_P + "</w14:madeUp>"));
		assertBlank("foo", withRoot("<foo/>"));
		assertBlank("w:numbering", withRoot("<w:numbering " + W + "/>"));
	}

	/** A w:hdr holding a bare run is a known name, so REQ-320's; the rule of what's held is for an unknown one. */
	@Test
	public void aKnownRootIsntJudgedByWhatItHolds() throws Exception {
		assertBlank("w:hdr", withRoot("<w:hdr " + W + "><w:r><w:t>WORDS</w:t></w:r></w:hdr>"));
	}

	/** Every unmarshal path reads such a root. */
	@Test
	public void everyPathReadsTheRoot() throws Exception {
		String xml = "<w:madeUp " + W + ">" + ONE_P + "</w:madeUp>";

		String always = "docx4j.jaxb.preprocess.always";
		String before = Docx4jProperties.getProperty(always);
		Docx4jProperties.setProperty(always, "MainDocumentPart");
		try {
			assertRead("WORDS", withRoot(xml));
		} finally {
			Docx4jProperties.setProperty(always, before == null ? "" : before);
		}

		String eager = "docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart";
		before = Docx4jProperties.getProperty(eager);
		Docx4jProperties.setProperty(eager, "true");
		try {
			for (String name : new String[] { "mr-01-madeup-p", "mr-40-branch-madeup-p" }) {
				MainDocumentPart main = load(name).getMainDocumentPart();
				List<Object> ps = main.getJAXBNodesViaXPath("//w:p", false);
				assertEquals(name, 1, ps.size());
				assertTrue(name, main.getContent().contains(XmlUtils.unwrap(ps.get(0))));
			}
		} finally {
			Docx4jProperties.setProperty(eager, before == null ? "false" : before);
		}

		WordprocessingMLPackage pkg = load("mr-16-empty");
		org.w3c.dom.Document dom = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		pkg.getMainDocumentPart().unmarshal(dom.getDocumentElement());
		assertRead("WORDS", pkg);
	}

	/** D9 is the main part's: an unknown root in a header isn't read, as Word hasn't been asked. */
	@Test
	public void anotherPartIsntReadThisWay() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(
				rewritten("c79-10-hdr-root", "word/header1.xml", "<w:madeUp " + W + ">" + ONE_P + "</w:madeUp>")));
		HeaderPart header = (HeaderPart) pkg.getParts().get(new PartName("/word/header1.xml"));
		assertNull(header.getJaxbElement());
		assertTrue(header.isContentUnreadable());
		assertNull(header.getUnexpectedRoot());
	}

	/** An inspection reads as partial, where it read as complete: the walk reads the part as XML, whatever its root. */
	@Test
	public void anInspectionTellsOfTheRoot() throws Exception {
		for (String[] blank : BLANK) assertSkipped(blank[0], Inspection.UNEXPECTED_ROOT);
		for (String name : REFUSED) assertSkipped(name, Inspection.UNREADABLE);
		for (String name : READ) assertEquals(name, Inspection.Status.COMPLETE, Inspect.of(load(name)).status());
	}

	/** The counts an extraction carries read the same way, as lower bounds. */
	@Test
	public void theCountsOfAnExtractionAreLowerBounds() throws Exception {
		assertEquals("PARTIAL", status("or-10-hdr"));
		assertEquals("COMPLETE", status("mr-01-madeup-p"));
	}

	private static Object status(String name) throws Exception {
		TextResult r = Texts.of(load(name), new TextOptions().inspect(true));
		return ((java.util.Map<?, ?>) r.metadata().get(TextResult.INSPECT)).get("status");
	}

	private static void assertSkipped(String name, String reason) throws Exception {
		Inspection i = Inspect.of(load(name));
		assertEquals(name, Inspection.Status.PARTIAL, i.status());
		boolean found = false;
		for (Inspection.Entry e : i.skipped()) found |= reason.equals(e.getReason()) && "|/word/document.xml|".equals(e.getLocation());
		assertTrue(name, found);
	}

	private static TextResult embedding(byte[] docx) throws Exception {
		WordprocessingMLPackage outer = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = outer.getMainDocumentPart();
		EmbeddedPackagePart embedded = new EmbeddedPackagePart(new PartName("/word/embeddings/inner.docx"));
		embedded.setContentType(new ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
		embedded.setBinaryData(docx);
		String rId = mdp.addTargetPart(embedded).getId();
		mdp.getContent().add(XmlUtils.unmarshalString("<w:p " + W
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:r><w:object><o:OLEObject Type=\"Embed\" ProgID=\"Word.Document.12\" r:id=\"" + rId + "\"/></w:object></w:r></w:p>"));
		return Texts.of(outer, new TextOptions().embedded(true));
	}

	/** REQ-801: one level down the warning names the embedded package's part, the one the host has. */
	@Test
	public void insideAnEmbeddedDocumentTheWarningNamesItsPart() throws Exception {
		List<TextWarning> warnings = unexpected(embedding(java.nio.file.Files.readAllBytes(file("or-10-hdr").toPath())));
		assertEquals(1, warnings.size());
		assertEquals("/word/embeddings/inner.docx", warnings.get(0).getSelector());
		assertEquals("w:hdr", warnings.get(0).getDetail());
	}

	/** REQ-321: refused at the top, and one level down the host isn't. */
	@Test
	public void aPackageWithNoMainPartIsRefused() throws Exception {
		try {
			Texts.plainText(new WordprocessingMLPackage(), new TextOptions());
			fail("read");
		} catch (Docx4JRuntimeException refused) {
			assertTrue(refused.getMessage(), refused.getMessage().contains("no main document part"));
		}
		byte[] noMain = rewritten("mr-01-madeup-p", "_rels/.rels", "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>");
		boolean told = false;
		for (TextWarning w : embedding(noMain).warnings()) told |= TextWarning.CONTENT_NOT_READ.equals(w.getKind()) && "/word/embeddings/inner.docx".equals(w.getSelector());
		assertTrue(told);
	}
}
