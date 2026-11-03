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
package org.docx4j.textextraction;

import static org.docx4j.textextraction.ExtractionPhase0Cases.override;
import static org.docx4j.textextraction.ExtractionPhase0Cases.relationship;
import static org.docx4j.textextraction.ExtractionPhase0Cases.rels;
import static org.docx4j.textextraction.ExtractionPhase0Cases.types;
import static org.docx4j.textextraction.ExtractionPhase0Cases.zip;
import static org.docx4j.textextraction.ExtractionProbeBuilder.NS;
import static org.docx4j.textextraction.ExtractionProbeBuilder.PAGE;
import static org.docx4j.textextraction.ExtractionProbeBuilder.ZZ9;
import static org.docx4j.textextraction.ExtractionProbeBuilder.ac;
import static org.docx4j.textextraction.ExtractionProbeBuilder.choice;
import static org.docx4j.textextraction.ExtractionProbeBuilder.fallback;
import static org.docx4j.textextraction.ExtractionProbeBuilder.field;
import static org.docx4j.textextraction.ExtractionProbeBuilder.p;
import static org.docx4j.textextraction.ExtractionProbeBuilder.pt;
import static org.docx4j.textextraction.ExtractionProbeBuilder.r;
import static org.docx4j.textextraction.ExtractionProbeBuilder.tblPr;
import static org.docx4j.textextraction.ExtractionProbeBuilder.tc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * The compact identification probe (docx-tool-identifier CR-001 phase 2): the 21 cases that separate every
 * pair of readers the identifier's library separates, in as few files as Word's behaviour allows.
 *
 * <ul>
 * <li>{@value #CONTENT}: cases 4, 15, 21, 25, 30, 32, 33, 36, 38 of the 38-case probe, 84 and 85 of the
 * opaque fixture, case 5 of caps-lang5, and three fields of the IF-field probe's round 2 (R08, R18, and
 * R14, whose w:dirty makes Word ask on open, so that its "update fields" reading stays measurable);</li>
 * <li>{@value #UPDATE}: w:updateFields on (Word recomputes every field on open), round 1's C04 and C16,
 * dpp's C06 and case 5 of caps-lang7d;</li>
 * <li>round 8's printdate file ({@value #PRINTDATE}), reused as it is: its PRINTDATE makes Word recompute every
 * field on open, which is what its controls measure.</li>
 * </ul>
 *
 * <p>Each case is the source's: the 38-case and opaque cases from the builders' own helpers (the case XML is the
 * source's but for drawing, shape, revision and relationship ids), the caps, C06 and field paragraphs copied from
 * the source files, and every picture, embedding, custom XML part and property part the cases use copied byte for
 * byte from the committed source files. The expected rows are the source tables' rows, the fixture column
 * changed; case numbers are the sources' (unique within each file).</p>
 *
 * <p>Two placements are Word's: every character outside Windows-1252 lies before character {@value #WD001} of
 * its file (counting from 0), or Word's Save As Plain Text writes nothing (WD-001, docx4j-portfolio
 * docs/word_defects.md, as revised 4 November 2026); and R08, an IF over PAGE, stays on page 1, as in round 2. caps-lang5's and C06's
 * literal H&#x2082;O are in different files, so that each file's count is its source's.</p>
 *
 * <p>Usage, from the repository root, with docx4j-core-tests' test classes on the class path:
 * {@code CompactProbeBuilder [resources]} rewrites {@code text-extraction/compact/} under the test resources.</p>
 *
 * @since 17.3.6
 */
public final class CompactProbeBuilder {

	static final String DIR = "text-extraction/compact";
	static final String CONTENT = "compact-probe.docx";
	static final String UPDATE = "compact-probe-updatefields.docx";
	/** Reused unchanged, relative to {@link #DIR} */
	static final String PRINTDATE = "../../fields/cr067-iffield/round8/corpus/iffield-probe-r8-printdate.docx";

	/**
	 * Word's Save As Plain Text writes nothing when a character outside the code page, stored, case-mapped or a
	 * w:sym's, lies at this 0-based offset or later, wherever its run starts (WD-001 as revised 4 November 2026; the
	 * first reading, a run starting at 246, came from probes holding a second such character ten on)
	 */
	static final int WD001 = 256;

	static final String HEADER = "case\ttoken\taccepted\toriginal\tmarkup\toption\twith_option\tnote\tfixture\tstatements";

	private final Path resources;

	CompactProbeBuilder(Path resources) {
		this.resources = resources;
	}

	public static void main(String[] args) throws Exception {
		Path resources = Paths.get(args.length > 0 ? args[0] : "docx4j-core-tests/src/test/resources");
		CompactProbeBuilder c = new CompactProbeBuilder(resources);
		Path out = resources.resolve(DIR);
		Files.createDirectories(out);
		byte[] content = c.content();
		byte[] update = c.update();
		for (byte[] docx : new byte[][] { content, update }) {
			WordprocessingMLPackage.load(new ByteArrayInputStream(docx)).getMainDocumentPart().getContents();
			check(docx);
		}
		Files.write(out.resolve(CONTENT), content);
		Files.write(out.resolve(UPDATE), update);
		Files.writeString(out.resolve("expected-tokens.tsv"), c.tokens(), StandardCharsets.UTF_8);
		Files.writeString(out.resolve("expected-findings.tsv"), "case\tfixture\tchannel\tkind\tselector\tscenario\texpected\tstatements\n", StandardCharsets.UTF_8);
		Files.writeString(out.resolve("goldens.tsv"), "fixture\tcase\tview\tscenario\tformat\tgolden\tstatements\n", StandardCharsets.UTF_8);
		Files.writeString(out.resolve("compact-probe.values.tsv"),
				c.values("fields/cr067-iffield/expected-round2.tsv", "R08_OVER_PAGE", "R18_DOCVARIABLE", "R14_SIMPLE_DIRTY"), StandardCharsets.UTF_8);
		Files.writeString(out.resolve("compact-probe-updatefields.values.tsv"),
				c.values("fields/cr067-iffield/expected.tsv", "C04_DATE_COND", "C16_WILDCARD"), StandardCharsets.UTF_8);
		System.out.println("wrote " + out);
	}

	// ------------------------------------------------------------------ the content file

	byte[] content() throws Exception {
		ExtractionProbeBuilder b = new ExtractionProbeBuilder(); // its helpers and its id counters
		byte[] probe38 = read("text-extraction/extraction-probe.docx");
		byte[] opaque = read("text-extraction/phase0/extraction-probe-opaque.docx");
		Map<String, byte[]> png33 = png33(probe38);
		StringBuilder body = new StringBuilder();

		body.append(p("<w:pPr><w:pStyle w:val='Title'/></w:pPr>" + r("Compact identification probe")));
		// first, so that its U+2082 lies before character 256 (WD-001)
		body.append(heading(5, "case mapping (caps-lang5)"));
		body.append(paragraph("text-extraction/dpp/caps/caps-lang5.docx", "L02_ "));
		body.append(paragraph("text-extraction/dpp/caps/caps-lang5.docx", "L03_ "));
		// on page 1, as in round 2: R08 is an IF over PAGE
		body.append(p("<w:pPr><w:pStyle w:val='Heading2'/></w:pPr>" + r("IF fields, their stored results (IF-field round 2)")));
		for (String token : new String[] { "R08_OVER_PAGE", "R18_DOCVARIABLE", "R14_SIMPLE_DIRTY" })
			body.append(paragraph("fields/cr067-iffield/iffield-probe-r2.docx", token));

		// the 38-case probe's cases, each as ExtractionProbeBuilder writes it
		body.append(heading(4, "two Choices, the first requiring an unknown namespace, the second wps; a Fallback"));
		body.append(p(r("The box: ") + "<w:r>" + ac(ZZ9, choice("zz9", "<w:t xml:space='preserve'>CHOICE1_UNKNOWN_ZETA </w:t>")
				+ choice("wps", b.wpsInline("wps", pt("CHOICE2_WPS_ETA")))
				+ fallback(b.vmlBox(pt("FALLBACK_EPSILON")))) + "</w:r>"));

		body.append(heading(15, "an inserted field and a deleted field"));
		body.append(p(r("Inserted: ") + "<w:ins " + b.rev() + ">" + field(" QUOTE inserted ", "TC15_INSERTED_FIELD", "")
				+ "</w:ins>" + r(" Deleted: ") + "<w:del " + b.rev() + ">"
				+ "<w:r><w:fldChar w:fldCharType='begin'/></w:r><w:r><w:delInstrText xml:space='preserve'> QUOTE deleted "
				+ "</w:delInstrText></w:r><w:r><w:fldChar w:fldCharType='separate'/></w:r><w:r><w:delText>TC15_DELETED_FIELD"
				+ "</w:delText></w:r><w:r><w:fldChar w:fldCharType='end'/></w:r></w:del>"));

		body.append(heading(21, "hyperlinks: a w:hyperlink with r:id, and a HYPERLINK field"));
		body.append(p(r("w:hyperlink: ") + "<w:hyperlink r:id='rId3' w:history='1'>"
				+ r("<w:rStyle w:val='Hyperlink'/>", "FLD21_LINK_TEXT") + "</w:hyperlink>"));
		body.append(p(r("HYPERLINK field: ") + field(" HYPERLINK &quot;https://example.com/FLD21_FIELD_URL_MARK&quot; ",
				"FLD21_FIELD_LINK_TEXT", "<w:rStyle w:val='Hyperlink'/>")));

		body.append(heading(25, "text boxes and legacy WordArt"));
		body.append(p(r("A floating DrawingML box: ") + "<w:r>" + b.wpsAnchor(pt("TBX25_FLOAT")) + "</w:r>"));
		body.append(p(r("An inline DrawingML box: ") + "<w:r>" + b.wpsInline("wps", pt("TBX25_INLINE")) + "</w:r>"));
		body.append(p(r("A VML box, no mc: ") + "<w:r>" + b.vmlBox(pt("TBX25_VML_ONLY")) + "</w:r>"));
		body.append("<w:tbl>" + tblPr() + "<w:tblGrid><w:gridCol w:w='6000'/></w:tblGrid><w:tr>"
				+ tc(6000, "", p(r("In a cell: ") + "<w:r>" + b.wpsInline("wps", pt("TBX25_IN_CELL")) + "</w:r>"))
				+ "</w:tr></w:tbl>");
		body.append(p(r("Legacy WordArt (v:textpath): ") + "<w:r>" + b.wordArt("WA25_TEXTPATH") + "</w:r>"));
		String header = "<w:hdr" + NS + ">" + p("<w:r>" + b.wpsInline("wps", pt("TBX25_IN_HEADER")) + "</w:r>") + "</w:hdr>";

		body.append(heading(30, "breaks, a tab and hyphens"));
		body.append(p(r("BRK30_BEFORE") + "<w:r><w:br/></w:r>" + r("BRK30_AFTER")));
		body.append(p("<w:r><w:br w:type='page'/></w:r>" + r("BRK30_PAGE")));
		body.append(p("<w:r><w:br w:type='column'/></w:r>" + r("BRK30_COLUMN")));
		body.append(p(r("TAB30_LEFT") + "<w:r><w:tab/></w:r>" + r("TAB30_RIGHT")));
		body.append(p(r("HY30_NON") + "<w:r><w:noBreakHyphen/></w:r>" + r("BREAK") + r(" ") + r("HY30_SO")
				+ "<w:r><w:softHyphen/></w:r>" + r("FT")));

		body.append(heading(32, "an equation with a fraction"));
		String math = "<w:rPr><w:rFonts w:ascii='Cambria Math' w:hAnsi='Cambria Math'/></w:rPr>";
		body.append("<w:p><m:oMathPara><m:oMath><m:r>" + math + "<m:t>MTH32_A</m:t></m:r><m:r>" + math + "<m:t>+</m:t></m:r>"
				+ "<m:f><m:fPr><m:ctrlPr>" + math.replace("</w:rPr>", "<w:i/></w:rPr>") + "</m:ctrlPr></m:fPr>"
				+ "<m:num><m:r>" + math + "<m:t>MTH32_B</m:t></m:r></m:num><m:den><m:r>" + math + "<m:t>2</m:t></m:r></m:den></m:f>"
				+ "</m:oMath></m:oMathPara></w:p>");

		body.append(heading(33, "pictures: alt text and a title, decorative, and an SVG with text in it"));
		body.append(p(r("Alt text and title: ") + b.picture("rId5", null, " descr='IMG33_ALT_TEXT' title='IMG33_TITLE'", "")));
		body.append(p(r("Decorative: ") + b.picture("rId6", null, " descr='IMG33_DECORATIVE_DESCR'",
				"<a:extLst><a:ext uri='{C183D7F6-B498-43B3-948B-1728B52AA6E4}'>"
						+ "<adec:decorative xmlns:adec='http://schemas.microsoft.com/office/drawing/2017/decorative' val='1'/>"
						+ "</a:ext></a:extLst>")));
		body.append(p(r("SVG, its PNG fallback in a:blip: ") + b.picture("rId7", "rId8", " descr='IMG33_SVG_ALT'", "")));

		body.append(heading(36, "an altChunk holding HTML"));
		body.append("<w:altChunk r:id='rId9'/>");
		body.append(pt("After the altChunk."));

		body.append(heading(38, "document properties and a custom XML part"));
		body.append(pt("The core properties hold a title and a subject; the custom XML part case 23 binds to holds "
				+ "one more value."));

		// the opaque fixture's two
		body.append(heading(84, "a PDF embedded as Acrobat embeds it"));
		body.append(p(ExtractionPhase0Cases.object(b, "rId11", "rId10", ExtractionOpaqueCases.ACROBAT_PROGID, false)));
		byte[] drawn = entry(opaque, "word/media/drawn85.png");
		body.append(heading(85, "a raster picture whose only text is drawn"));
		body.append(p(ExtractionOpaqueCases.picture("rId12", drawn, b.docPrId++)));

		body.append(pt("Built by docx4j (CompactProbeBuilder, in docx4j-core-tests) for CR-001 phase 2 of the docx-tool-identifier."
				+ " The README beside this file gives the source of each case."));
		body.append("<w:sectPr><w:headerReference w:type='default' r:id='rId2'/>" + PAGE + "</w:sectPr>");

		Map<String, byte[]> entries = new LinkedHashMap<>();
		String overrides = override("/word/document.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")
				+ override("/word/styles.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml")
				+ override("/word/header1.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml")
				+ override("/docProps/core.xml", "application/vnd.openxmlformats-package.core-properties+xml")
				+ override("/customXml/itemProps1.xml", "application/vnd.openxmlformats-officedocument.customXmlProperties+xml")
				+ override("/word/afchunk.htm", "text/html")
				+ override("/word/media/probe-svg33.svg", "image/svg+xml")
				+ override("/word/embeddings/opaque84.bin", ExtractionOpaqueCases.OLE_OBJECT);
		entries.put("[Content_Types].xml", xml(types(overrides)));
		entries.put("_rels/.rels", xml(rels(relationship("rId1", "officeDocument", "word/document.xml", false)
				+ relationship("rId2", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties", "docProps/core.xml", false))));
		entries.put("word/document.xml", xml("<w:document" + NS + "><w:body>" + body + "</w:body></w:document>"));
		entries.put("word/_rels/document.xml.rels", xml(rels(relationship("rId1", "styles", "styles.xml", false)
				+ relationship("rId2", "header", "header1.xml", false)
				+ relationship("rId3", "hyperlink", "https://example.com/FLD21_URL_MARK", true)
				+ relationship("rId4", "customXml", "../customXml/item1.xml", false)
				+ relationship("rId5", "image", "media/" + name(png33, "a"), false)
				+ relationship("rId6", "image", "media/" + name(png33, "b"), false)
				+ relationship("rId7", "image", "media/" + name(png33, "c"), false)
				+ relationship("rId8", "image", "media/probe-svg33.svg", false)
				+ relationship("rId9", "aFChunk", "afchunk.htm", false)
				+ relationship("rId10", "image", "media/preview83.png", false)
				+ relationship("rId11", "oleObject", "embeddings/opaque84.bin", false)
				+ relationship("rId12", "image", "media/drawn85.png", false))));
		entries.put("word/styles.xml", xml(ExtractionProbeBuilder.stylesXml()));
		entries.put("word/header1.xml", xml(header));
		// the parts the cases use, the bytes of the committed files that were measured
		for (String name : new String[] { "docProps/core.xml", "customXml/item1.xml", "customXml/itemProps1.xml",
				"customXml/_rels/item1.xml.rels", "word/media/probe-svg33.svg", "word/afchunk.htm" })
			entries.put(name, entry(probe38, name));
		for (Map.Entry<String, byte[]> e : png33.entrySet()) entries.put("word/media/" + e.getKey().substring(2), e.getValue());
		entries.put("word/media/preview83.png", entry(opaque, "word/media/preview83.png"));
		entries.put("word/embeddings/opaque84.bin", entry(opaque, "word/embeddings/opaque84.bin"));
		entries.put("word/media/drawn85.png", drawn);
		return zip(entries);
	}

	// ------------------------------------------------------------------ the w:updateFields file

	byte[] update() throws Exception {
		StringBuilder body = new StringBuilder();
		body.append(p("<w:pPr><w:pStyle w:val='Title'/></w:pPr>" + r("Compact identification probe, w:updateFields")));
		// first, so that their U+2082, U+FB01 and U+01C6 lie before character 256 (WD-001)
		body.append(heading(6, "dpp bisect C06"));
		body.append(paragraph("text-extraction/dpp/bisect/C06.docx", "C06_ "));
		body.append(heading(5, "caps-lang7d"));
		body.append(paragraph("text-extraction/dpp/caps/caps-lang7d.docx", "L02_ "));
		body.append(p("<w:pPr><w:pStyle w:val='Heading2'/></w:pPr>" + r("IF fields under w:updateFields (IF-field round 1)")));
		for (String token : new String[] { "C04_DATE_COND", "C16_WILDCARD" })
			body.append(paragraph("fields/cr067-iffield/iffield-probe-r1-updatefields.docx", token));
		body.append(pt("Built by docx4j (CompactProbeBuilder, in docx4j-core-tests) for CR-001 phase 2 of the docx-tool-identifier."
				+ " The README beside this file gives the source of each case."));
		body.append("<w:sectPr>" + PAGE + "</w:sectPr>");

		Map<String, byte[]> entries = new LinkedHashMap<>();
		entries.put("[Content_Types].xml", xml(types(
				override("/word/document.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")
				+ override("/word/styles.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml")
				+ override("/word/settings.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"))));
		entries.put("_rels/.rels", xml(rels(relationship("rId1", "officeDocument", "word/document.xml", false))));
		entries.put("word/document.xml", xml("<w:document" + NS + "><w:body>" + body + "</w:body></w:document>"));
		entries.put("word/_rels/document.xml.rels", xml(rels(relationship("rId1", "styles", "styles.xml", false)
				+ relationship("rId2", "settings", "settings.xml", false))));
		entries.put("word/styles.xml", xml(ExtractionProbeBuilder.stylesXml()));
		entries.put("word/settings.xml", xml("<w:settings" + NS + "><w:updateFields/></w:settings>"));
		return zip(entries);
	}

	// ------------------------------------------------------------------ the tables

	/** The source tables' rows of the cases, the fixture column naming the compact file. */
	String tokens() throws Exception {
		StringBuilder s = new StringBuilder(HEADER).append('\n');
		rows(s, "text-extraction/dpp/expected-tokens.tsv", "5", "caps/caps-lang5.docx", CONTENT);
		for (String n : new String[] { "4", "15", "21", "25", "30", "32", "33", "36", "38" })
			rows(s, "text-extraction/phase0/expected-tokens.tsv", n, "extraction-probe.docx", CONTENT);
		for (String n : new String[] { "84", "85" })
			rows(s, "text-extraction/phase0/expected-tokens.tsv", n, ExtractionOpaqueCases.FIXTURE, CONTENT);
		rows(s, "text-extraction/dpp/expected-tokens.tsv", "6", "dpp-forks-word365.docx", UPDATE);
		rows(s, "text-extraction/dpp/expected-tokens.tsv", "5", "caps/caps-lang7d.docx", UPDATE);
		return s.toString();
	}

	private void rows(StringBuilder s, String table, String n, String fixture, String compact) throws Exception {
		List<String> lines = Files.readAllLines(resources.resolve(table), StandardCharsets.UTF_8);
		if (!lines.get(0).equals(HEADER)) throw new IllegalStateException(table + ": another header");
		int found = 0;
		for (String line : lines.subList(1, lines.size())) {
			String[] f = line.split("\t", -1);
			if (f[0].equals(n) && f[8].equals(fixture)) {
				f[8] = compact;
				s.append(String.join("\t", f)).append('\n');
				found++;
			}
		}
		if (found == 0) throw new IllegalStateException(table + ": no rows for case " + n + " of " + fixture);
	}

	/** A value table's header and the rows of these tokens, as the source has them. */
	String values(String table, String... tokens) throws Exception {
		List<String> lines = Files.readAllLines(resources.resolve(table), StandardCharsets.UTF_8);
		StringBuilder s = new StringBuilder(lines.get(0)).append('\n');
		for (String t : tokens) {
			String row = null;
			for (String line : lines) if (line.startsWith(t + "\t")) row = line;
			if (row == null) throw new IllegalStateException(table + ": no row for " + t);
			s.append(row).append('\n');
		}
		return s.toString();
	}

	// ------------------------------------------------------------------ pieces

	static String heading(int n, String title) {
		return p("<w:pPr><w:pStyle w:val='Heading2'/></w:pPr>" + r("Case " + n + ": " + title));
	}

	private static final Pattern PARAGRAPH = Pattern.compile("<w:p[ >].*?</w:p>", Pattern.DOTALL);

	/**
	 * The one paragraph of the source's main part holding this text, as the source has it, less the indentation of
	 * a pretty-printed part (whitespace holding a line break between tags).
	 */
	String paragraph(String source, String holding) throws Exception {
		String xml = new String(entry(read(source), "word/document.xml"), StandardCharsets.UTF_8);
		List<String> found = new ArrayList<>();
		Matcher m = PARAGRAPH.matcher(xml);
		while (m.find()) if (m.group().contains(holding)) found.add(m.group());
		if (found.size() != 1) throw new IllegalStateException(source + ": " + found.size() + " paragraphs hold " + holding);
		String p = found.get(0).replaceAll(">\\s*\\n\\s*<", "><");
		if (p.indexOf('\'') >= 0) throw new IllegalStateException(source + ": an apostrophe in " + holding + "'s paragraph");
		return p;
	}

	/** Case 33's three PNGs, keyed a, b, c (as the builder makes them: blue, olive, yellow), from the source's bytes. */
	private static Map<String, byte[]> png33(byte[] probe38) throws Exception {
		Map<Integer, String> colours = Map.of(0x3366cc, "a", 0x99aa33, "b", 0xffe08a, "c");
		Map<String, byte[]> out = new LinkedHashMap<>();
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(probe38))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null;) {
				if (!e.getName().startsWith("word/media/") || !e.getName().endsWith(".png")) continue;
				byte[] bytes = z.readAllBytes();
				BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes));
				String key = colours.get(img.getRGB(0, 0) & 0xffffff);
				if (key != null) out.put(key + ":" + e.getName().substring("word/media/".length()), bytes);
			}
		}
		if (out.size() != 3) throw new IllegalStateException("case 33's pictures: " + out.keySet());
		Map<String, byte[]> sorted = new LinkedHashMap<>();
		for (String k : new String[] { "a", "b", "c" })
			for (Map.Entry<String, byte[]> e : out.entrySet()) if (e.getKey().startsWith(k + ":")) sorted.put(e.getKey(), e.getValue());
		return sorted;
	}

	private static String name(Map<String, byte[]> png33, String key) {
		for (String k : png33.keySet()) if (k.startsWith(key + ":")) return k.substring(2);
		throw new IllegalStateException(key);
	}

	byte[] read(String resource) throws Exception {
		return Files.readAllBytes(resources.resolve(resource));
	}

	static byte[] entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null;) if (e.getName().equals(name)) return z.readAllBytes();
		}
		throw new IllegalStateException("no " + name);
	}

	/** XML written with single quotes, as the builders write it; the copied paragraphs hold no apostrophe. */
	private static byte[] xml(String s) {
		return s.replace('\'', '"').getBytes(StandardCharsets.UTF_8);
	}

	// ------------------------------------------------------------------ checks

	private static final Charset CP1252 = Charset.forName("windows-1252");
	private static final Pattern MAIN_STORY = Pattern.compile(
			"<w:txbxContent>.*?</w:txbxContent>|<w:p[ >/]|</w:p>|<w:r>|<w:r |<w:t(?: [^>]*)?>([^<]*)</w:t>|<w:instrText(?: [^>]*)?>([^<]*)</w:instrText>|<w:sym [^>]*w:char=\"([0-9A-Fa-f]+)\"[^>]*/>|<w:tab/>|<w:br(?: [^>]*)?/>|<w:noBreakHyphen/>|<w:softHyphen/>",
			Pattern.DOTALL);

	/**
	 * Where each run of the main story starts, in Word's units (a paragraph mark counts two, CR LF; a field's code
	 * is counted, which can only move a run later), with the run's text, a w:sym as its private-use character: for
	 * WD-001's rule.
	 */
	static List<Object[]> runStarts(byte[] docx) throws Exception {
		String xml = new String(entry(docx, "word/document.xml"), StandardCharsets.UTF_8);
		List<Object[]> runs = new ArrayList<>();
		int offset = 0;
		Object[] run = null;
		Matcher m = MAIN_STORY.matcher(xml);
		while (m.find()) {
			String t = m.group();
			if (t.startsWith("<w:txbxContent")) continue;               // a text box is a story of its own
			if (t.equals("</w:p>")) offset += 2;
			else if (t.startsWith("<w:r>") || t.startsWith("<w:r ")) runs.add(run = new Object[] { offset, "" });
			else if (m.group(1) != null || m.group(2) != null) {
				String text = m.group(1) != null ? m.group(1) : m.group(2);
				if (run != null) run[1] = run[1] + text;
				offset += text.length();
			} else if (m.group(3) != null) {                             // a w:sym: one character, outside the code page
				if (run != null) run[1] = run[1] + new String(Character.toChars(Integer.parseInt(m.group(3), 16)));
				offset += 1;
			} else if (!t.startsWith("<w:p")) offset += 1;              // a tab, a break, a hyphen
		}
		return runs;
	}

	private static String unescape(String s) {
		Matcher m = Pattern.compile("&(?:#x([0-9A-Fa-f]+)|#([0-9]+)|(lt|gt|amp|quot|apos));").matcher(s);
		StringBuilder out = new StringBuilder();
		while (m.find()) {
			String c = m.group(1) != null ? new String(Character.toChars(Integer.parseInt(m.group(1), 16)))
					: m.group(2) != null ? new String(Character.toChars(Integer.parseInt(m.group(2))))
					: Map.of("lt", "<", "gt", ">", "amp", "&", "quot", "\"", "apos", "'").get(m.group(3));
			m.appendReplacement(out, Matcher.quoteReplacement(c));
		}
		return m.appendTail(out).toString();
	}

	/** WD-001: every character outside Windows-1252 lies before character {@value #WD001}, counting from 0. */
	static void check(byte[] docx) throws Exception {
		for (Object[] run : runStarts(docx)) {
			String text = unescape((String) run[1]);
			for (int i = 0; i < text.length(); i += Character.charCount(text.codePointAt(i))) {
				String c = new String(Character.toChars(text.codePointAt(i)));
				if (!CP1252.newEncoder().canEncode(c) && (Integer) run[0] + i >= WD001)
					throw new IllegalStateException("WD-001: " + c + " at character " + ((Integer) run[0] + i) + ", in a run from " + run[0]);
			}
		}
	}
}
