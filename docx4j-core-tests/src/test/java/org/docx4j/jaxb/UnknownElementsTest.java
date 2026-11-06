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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * An element Word doesn't know, read as Word reads it (CR-076).  The files in
 * {@code unknown-elements/} are core-ts's Word check 76, compared with Word 365's saves of
 * them: paragraphs, rows, cells, the words in their hosts, breaks, tabs, what run and paragraph
 * properties the check uses, and move markers.  A field's code is left out, since Word writes
 * a {@code w:fldSimple} as a complex field.
 */
public class UnknownElementsTest {

	private static final String DIR = "unknown-elements/";

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:ext=\"urn:docx4j:check76\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" mc:Ignorable=\"ext\"";

	/** w:id etc. are absent on purpose: docx4j's reading isn't asked about them. */
	private static final String TRACKED = "w:id=\"9\" w:author=\"A\"";

	private static final String HIDDEN = "<w:r><w:t>HIDDEN WORDS</w:t></w:r>";

	// ---------------------------------------------------------------- Word's saves

	/** Every file Word saved reads as Word's save of it, but for D11's two. */
	@Test public void readsAsWordsSave() throws Exception {
		List<String> different = new ArrayList<String>();
		int compared = 0;
		for (String name : sources()) {
			if (name.equals("76t44-madeup-in-choice") || name.equals("76t45-madeup-in-fallback")
					|| name.equals("76t71-held-paragraph-in-hyperlink")) continue;
			if (!exists(name + "-word365.docx")) continue;
			compared++;
			Map<String, List<String>> ours = tokens(load(name + ".docx"));
			Map<String, List<String>> word = tokens(load(name + "-word365.docx"));
			for (Map.Entry<String, List<String>> part : ours.entrySet()) {
				List<String> w = word.get(part.getKey());
				if (w != null && !w.equals(part.getValue())) {
					different.add(name + " " + part.getKey() + "\n  ours " + part.getValue() + "\n  Word " + w);
				}
			}
		}
		assertEquals("files with a save", 76, compared);
		assertTrue(String.join("\n", different), different.isEmpty());
	}

	/** Word's reading of round 8's corners, stated, so that a change to both can't pass unseen. */
	@Test public void runsInAPPrGoToTheParagraphsHead() throws Exception {
		assertEquals("[t:HIDDEN WORDS SECOND WORDSTARGET TEXT, ¶]", target("76t37-two-runs-in-pPr"));
		assertEquals("[t:HIDDEN WORDSTARGET TEXT, ¶]", target("76t38-run-and-property-in-pPr"));
		assertEquals("[jc=center, t:HIDDEN WORDSTARGET TEXT, ¶]", target("76t40-run-in-pPrChange"));
	}

	/** D8: the change keeps its author and date, where Word writes Unknown and none. */
	@Test public void aChangeKeepsItsAuthor() throws Exception {
		String xml = XmlUtils.marshaltoString(load("76t40-run-in-pPrChange.docx").getMainDocumentPart().getJaxbElement(), true, false);
		assertTrue(xml, xml.contains("w:author=\"Check 76\""));
	}

	/** D9: the section break goes with the w:sectPr. */
	@Test public void aRunInASectPrTakesTheSectionBreak() throws Exception {
		assertEquals("[t:HIDDEN WORDSTARGET TEXT, ¶]", target("76t50-run-in-sectPr"));
	}

	/** D10: a held paragraph's mark ends a paragraph, and its properties are that paragraph's.  The
	 *  host's own mark is the empty paragraph after. */
	@Test public void aParagraphInAParagraph() throws Exception {
		assertEquals("[t:TARGET TEXTHIDDEN WORDS, ¶, ¶]", target("76t43-paragraph-in-madeup-in-p"));
		assertEquals("[t:TARGET TEXTHIDDEN WORDS, ¶, t:SECOND WORDS, ¶, ¶]", target("76t56-two-held-paragraphs"));
		assertEquals("[jc=center, t:TARGET TEXTHIDDEN WORDS, ¶, ¶]", target("76t58-held-paragraph-centred"));
		assertEquals("[jc=center, t:TARGET TEXTHIDDEN WORDS, ¶, jc=right, ¶]", target("76t68-host-right-held-centred"));
		assertEquals("[jc=right, t:TARGET TEXTHIDDEN WORDS, ¶, jc=right, ¶]", target("76t69-host-right-held-plain"));
		assertEquals("[t:TARGET TEXTHIDDEN WORDS, ¶, ¶]", target("76t70-held-paragraph-two-deep"));
	}

	/** Word's save has a complex field for the link, its end in the next paragraph; the words and
	 *  paragraphs are the same, and here they stay in the w:hyperlink. */
	@Test public void aParagraphHeldInAHyperlink() throws Exception {
		assertEquals("[hyperlink|t:TARGET TEXTHIDDEN WORDS, ¶, ¶]", target("76t71-held-paragraph-in-hyperlink"));
		List<String> word = tokens(load("76t71-held-paragraph-in-hyperlink-word365.docx")).get("/word/document.xml");
		assertEquals("[t:TARGET TEXTHIDDEN WORDS, ¶, ¶]", word.subList(word.indexOf("¶") + 1, word.lastIndexOf("t:LAST LINE.")).toString());
	}

	/** The host's mark takes its w:pPr, and a run rule 1b took from it comes once. */
	@Test public void aRunInThePPrOfAHostThatIsSplit() throws Exception {
		String read = bodyTokens("<w:p><w:pPr><w:madeUp><w:r><w:t>x</w:t></w:r></w:madeUp></w:pPr><w:r><w:t>a</w:t></w:r>"
				+ "<w:madeUp><w:p><w:r><w:t>b</w:t></w:r></w:p></w:madeUp></w:p>");
		assertEquals(read, 1, count(read, "x"));
	}

	/** Beside something else in the element nothing is measured, and no word is lost. */
	@Test public void aHeldParagraphBesideARun() throws Exception {
		String read = bodyTokens("<w:p><w:r><w:t>a</w:t></w:r><w:madeUp><w:r><w:t>b</w:t></w:r><w:p><w:r><w:t>c</w:t></w:r></w:p></w:madeUp></w:p>");
		assertEquals("[t:abc, ¶]", read);
	}

	/** A known element holding a w:p isn't this rule's. */
	@Test public void aKnownElementHoldingAParagraph() throws Exception {
		String out = preprocess("<w:document " + NS + "><w:body><w:p><w:r><w:t>a</w:t></w:r><w:smartTag><w:p>" + HIDDEN
				+ "</w:p></w:smartTag></w:p></w:body></w:document>");
		assertTrue(out, out.contains("smartTag"));
	}

	/** Word refuses these.  docx4j reads what it can: every run where it stands. */
	@Test public void aParagraphInAParagraphWordRefuses() throws Exception {
		for (String name : new String[] { "76t53-held-paragraph-then-a-run", "76t54-held-paragraph-first", "76t55-held-paragraph-alone",
				"76t57-two-held-paragraphs-then-a-run", "76t59-both-aligned-then-a-run" }) {
			String read = tokens(load(name + ".docx")).get("/word/document.xml").toString();
			assertTrue(read, read.substring(read.indexOf("¶")).contains("HIDDEN WORDS"));
		}
	}

	@Test public void aBreakHeldIsABreak() throws Exception {
		assertEquals("[t:TARGET, br, t:TEXT, ¶]", target("76t42-break-in-madeup"));
	}

	@Test public void textHeldDirectlyGoes() throws Exception {
		assertEquals("[t:TARGET TEXT, ¶]", target("76t41-text-in-madeup"));
	}

	/** D11: the unknown element in a kept branch reaches no stylesheet.  The branch stays whole, so
	 *  a save keeps the words and Word reads them, and docx4j's own text misses them. */
	@Test public void aKeptBranchIsLeftWhole() throws Exception {
		for (String name : new String[] { "76t44-madeup-in-choice", "76t45-madeup-in-fallback" }) {
			WordprocessingMLPackage pkg = load(name + ".docx");
			String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
			assertTrue(name, xml.contains("madeUp"));
			String text = Texts.plainText(pkg, new TextOptions());
			assertTrue(text, text.contains("TARGET TEXT, then  and the rest."));
		}
	}

	/** When the stylesheet runs for another reason, it reads through in the branch. */
	@Test public void aBranchWhenTheStylesheetRuns() throws Exception {
		String out = preprocess("<w:document " + NS + "><w:body><w:p><mc:AlternateContent>"
				+ "<mc:Choice Requires=\"ext\">" + "<w:r><w:t>C</w:t></w:r></mc:Choice>"
				+ "<mc:Fallback><w:madeUp>" + HIDDEN + "</w:madeUp></mc:Fallback>"
				+ "</mc:AlternateContent></w:p></w:body></w:document>");
		assertFalse(out, out.contains("madeUp"));
		assertTrue(out, out.contains("HIDDEN WORDS"));
	}

	/** Word refuses these.  docx4j goes on reading what it can, the element dropped. */
	@Test public void whatWordRefuses() throws Exception {
		for (String name : new String[] { "76t28-madeup-holding-a-run-in-rPr", "76t46-run-in-sdtPr", "76t47-run-in-trPr",
				"76t48-run-in-tcPr", "76t49-run-in-tblPr" }) {
			String read = tokens(load(name + ".docx")).get("/word/document.xml").toString();
			assertTrue(read, read.contains("TARGET TEXT"));
			assertEquals(read, 1, count(read, "HIDDEN WORDS")); // the description line
		}
	}

	// ---------------------------------------------------------------- the rules' edges

	/** Those names are declared for other things too, which no file measures: rule 1 leaves them. */
	@Test public void aHostsNameWhereItIsNoHost() throws Exception {
		String made = "<w:madeUp>" + HIDDEN + "</w:madeUp>";
		for (String shape : new String[] {
				"<w:p><w:pPr><w:rPr><w:ins " + TRACKED + ">" + made + "</w:ins></w:rPr></w:pPr></w:p>",
				"<w:p><w:pPr><w:rPr><w:del " + TRACKED + ">" + made + "</w:del></w:rPr></w:pPr></w:p>",
				"<w:p><w:pPr><w:rPr><w:moveFrom " + TRACKED + ">" + made + "</w:moveFrom></w:rPr></w:pPr></w:p>",
				"<w:p><w:pPr><w:rPr><w:moveTo " + TRACKED + ">" + made + "</w:moveTo></w:rPr></w:pPr></w:p>",
				"<w:p><w:pPr><w:numPr><w:ins " + TRACKED + ">" + made + "</w:ins></w:numPr></w:pPr></w:p>",
				"<w:tbl><w:tr><w:trPr><w:ins " + TRACKED + ">" + made + "</w:ins></w:trPr></w:tr></w:tbl>",
				"<w:tbl><w:tr><w:trPr><w:del " + TRACKED + ">" + made + "</w:del></w:trPr></w:tr></w:tbl>",
				"<w:p><m:oMath><m:r><w:ins " + TRACKED + ">" + made + "</w:ins></m:r></m:oMath></w:p>",
				"<w:p><m:oMath><m:r><w:del " + TRACKED + ">" + made + "</w:del></m:r></m:oMath></w:p>",
				"<w:p><m:oMath><m:f><m:fPr><m:ctrlPr><w:ins " + TRACKED + ">" + made + "</w:ins></m:ctrlPr></m:fPr></m:f></m:oMath></w:p>",
				"<w:p><m:oMath><m:f><m:fPr><m:ctrlPr><w:del " + TRACKED + ">" + made + "</w:del></m:ctrlPr></m:fPr></m:f></m:oMath></w:p>",
		}) {
			String out = preprocess("<w:document " + NS + "><w:body>" + shape + "</w:body></w:document>");
			assertTrue(shape, out.contains("madeUp"));
		}
		for (String shape : new String[] { "<w:footnotePr><w:footnote w:id=\"0\">" + made + "</w:footnote></w:footnotePr>",
				"<w:endnotePr><w:endnote w:id=\"0\">" + made + "</w:endnote></w:endnotePr>" }) {
			String out = preprocess("<w:settings " + NS + ">" + shape + "</w:settings>");
			assertTrue(shape, out.contains("madeUp"));
		}
	}

	/** A run-level w:ins in an m:oMath is the host 76t15 measures in a w:p. */
	@Test public void aRunLevelInsInMath() throws Exception {
		String out = preprocess("<w:document " + NS + "><w:body><w:p><m:oMath><w:ins " + TRACKED + "><w:madeUp><m:r><m:t>x</m:t></m:r>"
				+ "</w:madeUp></w:ins></m:oMath></w:p></w:body></w:document>");
		assertFalse(out, out.contains("madeUp"));
	}

	/** Another namespace stops the walk: a w:madeUp in an m:r isn't read through to the w:p. */
	@Test public void anotherNamespaceStopsTheWalk() throws Exception {
		for (String shape : new String[] { "<m:oMath><m:r><w:madeUp><w:t>HIDDEN WORDS</w:t></w:madeUp><m:t>x</m:t></m:r></m:oMath>",
				"<ext:opaque><w:madeUp>" + HIDDEN + "</w:madeUp></ext:opaque>" }) {
			String out = preprocess("<w:document " + NS + "><w:body><w:p>" + shape + "</w:p></w:body></w:document>");
			assertTrue(shape, out.contains("madeUp"));
		}
	}

	/** In a w:pPr that isn't a paragraph's a run has nowhere to go. */
	@Test public void aStylesPPr() throws Exception {
		String out = preprocess("<w:styles " + NS + "><w:style w:styleId=\"a\"><w:pPr><w:madeUp>" + HIDDEN
				+ "</w:madeUp></w:pPr></w:style></w:styles>");
		assertEquals(out, 1, count(out, "HIDDEN WORDS"));
		assertTrue(out, out.contains("madeUp"));
	}

	/** The body's own w:sectPr isn't measured, and stays. */
	@Test public void theBodysSectPrStays() throws Exception {
		String out = preprocess("<w:document " + NS + "><w:body><w:sectPr><w:madeUp>" + HIDDEN + "</w:madeUp>"
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/></w:sectPr></w:body></w:document>");
		assertTrue(out, out.contains("pgSz"));
	}

	/** A paragraph the nested-paragraph template splits: rule 1 applies in its segments, and rule 1b's
	 *  runs come once. */
	@Test public void aParagraphThatIsSplit() throws Exception {
		String read = bodyTokens("<w:p><w:r><w:t>a</w:t><w:p><w:r><w:t>b</w:t></w:r></w:p><w:t>c</w:t></w:r>"
				+ "<w:madeUp><w:r><w:t>d</w:t></w:r></w:madeUp></w:p>");
		assertEquals(read, 1, count(read, "d"));
		read = bodyTokens("<w:p><w:pPr><w:madeUp><w:r><w:t>x</w:t></w:r></w:madeUp></w:pPr>"
				+ "<w:r><w:t>a</w:t><w:p><w:r><w:t>b</w:t></w:r></w:p><w:t>c</w:t></w:r></w:p>");
		assertEquals(read, 1, count(read, "x"));
	}

	/** A paragraph after a w:br in w:body goes through CR-073's w:pPr template, which takes rule 1b too. */
	@Test public void aRunInAPPrAfterABreakInTheBody() throws Exception {
		String read = bodyTokens("<w:br/><w:p><w:pPr><w:madeUp><w:r><w:t>x</w:t></w:r></w:madeUp></w:pPr><w:r><w:t>a</w:t></w:r></w:p>");
		assertEquals("[br, t:xa, ¶]", read);
	}

	/** Rule 2: a namespace docx4j understands, other than w:, gives its text, as one in no namespace does. */
	@Test public void aKnownNamespaceInAText() throws Exception {
		assertEquals("[t:abc, ¶]", bodyTokens("<w:p><w:r><w:t>a<w14:x>b</w14:x>c</w:t></w:r></w:p>"));
		assertEquals("[t:abc, ¶]", bodyTokens("<w:p><w:r><w:t>a<x xmlns=\"\">b</x>c</w:t></w:r></w:p>"));
		assertEquals("[t:ac, ¶]", bodyTokens("<w:p><w:r><w:t>a<ext:x>b</ext:x>c</w:t></w:r></w:p>"));
		assertEquals("[t:ac, ¶]", bodyTokens("<w:p><w:r><w:t>a<w:madeUp>b</w:madeUp>c</w:t></w:r></w:p>"));
	}

	/** The list in the stylesheet is the schema's (D2): a schema change fails here until the list follows. */
	@Test public void theStylesheetsNamesAreTheSchemas() throws Exception {
		File xsd = new File("../xsd/wml/wml.xsd");
		if (!xsd.exists()) xsd = new File("xsd/wml/wml.xsd");
		Set<String> schema = new TreeSet<String>();
		Matcher m = Pattern.compile("<xsd:element\\s+name=\"([^\"]+)\"")
				.matcher(new String(java.nio.file.Files.readAllBytes(xsd.toPath()), StandardCharsets.UTF_8));
		while (m.find()) schema.add(m.group(1));
		String stylesheet;
		try (InputStream is = ResourceUtils.getResource("org/docx4j/jaxb/mc-preprocessor.xslt")) {
			stylesheet = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
		Matcher list = Pattern.compile("name=\"wml-names\" select=\"' ([^']+) '\"").matcher(stylesheet);
		assertTrue(list.find());
		assertEquals(schema, new TreeSet<String>(java.util.Arrays.asList(list.group(1).split(" "))));
	}

	// ---------------------------------------------------------------- helpers

	private static List<String> sources() {
		File dir = new File("src/test/resources/" + DIR);
		if (!dir.isDirectory()) dir = new File("docx4j-core-tests/src/test/resources/" + DIR);
		List<String> names = new ArrayList<String>();
		for (String f : dir.list()) {
			if (f.endsWith(".docx") && !f.endsWith("-word365.docx")) names.add(f.substring(0, f.length() - ".docx".length()));
		}
		java.util.Collections.sort(names);
		return names;
	}

	private static boolean exists(String name) {
		return UnknownElementsTest.class.getClassLoader().getResource(DIR + name) != null;
	}

	private static WordprocessingMLPackage load(String name) throws Exception {
		return WordprocessingMLPackage.load(ResourceUtils.getResource(DIR + name));
	}

	private static int count(String in, String what) {
		int n = 0;
		for (int i = in.indexOf(what); i >= 0; i = in.indexOf(what, i + what.length())) n++;
		return n;
	}

	/** The stylesheet alone, on a part's XML. */
	private static String preprocess(String xml) throws Exception {
		Templates mc = JaxbValidationEventHandler.getMcPreprocessor();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlUtils.transform(new StreamSource(new StringReader(xml)), mc, null, new StreamResult(out));
		return new String(out.toByteArray(), StandardCharsets.UTF_8);
	}

	/** A body through the load, as a part's content is read. */
	private static String bodyTokens(String content) throws Exception {
		MainDocumentPart part = new MainDocumentPart();
		part.unmarshal(new ByteArrayInputStream(("<w:document " + NS + "><w:body>" + content + "</w:body></w:document>")
				.getBytes(StandardCharsets.UTF_8)));
		List<String> out = new ArrayList<String>();
		walk(element(part), "", out);
		return out.toString();
	}

	/** The paragraph or table after the description line, up to the last paragraph, as Word's and ours agree. */
	private static String target(String name) throws Exception {
		List<String> all = tokens(load(name + ".docx")).get("/word/document.xml");
		int first = all.indexOf("¶");
		int last = all.lastIndexOf("t:LAST LINE.");
		return all.subList(first + 1, last).toString();
	}

	private static Map<String, List<String>> tokens(WordprocessingMLPackage pkg) throws Exception {
		Map<String, List<String>> parts = new TreeMap<String, List<String>>();
		for (Part part : pkg.getParts().getParts().values()) {
			String name = part.getPartName().getName();
			if (!(part instanceof JaxbXmlPart)
					|| !name.matches("/word/(document|header\\d*|footer\\d*|comments|footnotes|endnotes)\\.xml")) continue;
			List<String> out = new ArrayList<String>();
			walk(element((JaxbXmlPart<?>) part), "", out);
			parts.put(name, out);
		}
		return parts;
	}

	private static Element element(JaxbXmlPart<?> part) throws Exception {
		String xml = XmlUtils.marshaltoString(part.getJaxbElement(), true, false);
		return XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
	}

	/** Walks a part: tracked and link hosts prefix the words, and adjacent words of one host and
	 *  one set of properties join, as Word joins runs.  Of a kept branch, the one Word took. */
	private static void walk(Element e, String host, List<String> out) {
		String name = e.getLocalName();
		switch (name) {
		case "AlternateContent":
			Element choice = child(e, "Choice"), fallback = child(e, "Fallback");
			Element taken = choice != null && "w14".equals(choice.getAttribute("Requires")) ? choice : fallback;
			if (taken != null) children(taken, host, out);
			return;
		case "instrText": case "fldChar": case "sectPr": case "rPr": case "tblPr": case "tblGrid": case "trPr": case "tcPr": case "sdtPr":
			return;
		case "pPr":
			Element jc = child(e, "jc");
			if (jc != null) out.add("jc=" + jc.getAttributeNS(e.getNamespaceURI(), "val"));
			if (child(e, "sectPr") != null) out.add("sectPr");
			return;
		case "t": case "delText":
			if (!"http://schemas.openxmlformats.org/wordprocessingml/2006/main".equals(e.getNamespaceURI())) return;
			String head = host + "t" + props((Element) e.getParentNode()) + ":";
			int last = out.size() - 1;
			if (last >= 0 && out.get(last).startsWith(head)) out.set(last, out.get(last) + e.getTextContent());
			else out.add(head + e.getTextContent());
			return;
		case "br":
			out.add("br");
			return;
		case "tab":
			if ("r".equals(e.getParentNode().getLocalName())) out.add("tab");
			return;
		case "moveFromRangeStart": case "moveFromRangeEnd": case "moveToRangeStart": case "moveToRangeEnd":
			out.add(name);
			return;
		case "ins": case "del": case "moveFrom": case "moveTo": case "hyperlink":
			children(e, host + name + "|", out);
			return;
		case "tr":
			out.add("row");
			children(e, host, out);
			return;
		case "tc":
			out.add("cell");
			children(e, host, out);
			return;
		case "p":
			children(e, host, out);
			out.add("¶");
			return;
		default:
			children(e, host, out);
		}
	}

	private static void children(Element e, String host, List<String> out) {
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) walk((Element) n, host, out);
		}
	}

	private static Element child(Element e, String name) {
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && name.equals(n.getLocalName())) return (Element) n;
		}
		return null;
	}

	/** The run properties the check uses. */
	private static String props(Element run) {
		Element rPr = child(run, "rPr");
		return rPr != null && child(rPr, "b") != null ? "[b]" : "";
	}
}
