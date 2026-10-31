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
package org.docx4j.model.revisions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.Docx4J;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Hdr;

/**
 * CR-068 phase 0's probe: a manual redline (struck old words, underlined or coloured new ones) in
 * seventeen cases, M01 to M17, each a paragraph or table whose text starts with its tag, and the
 * conversions CR-068 &#xa7;3 predicts for it, written by hand here (phase 1 does not exist yet), so
 * that Word can be asked what it makes of them: whether it opens them without repair, what its
 * Review pane lists, and what its Accept All and Reject All leave. The PowerShell script
 * {@code cr068-probe.ps1} beside the output does that, and also has Word make its own tracked
 * deletion of the struck text in the base, for the shape Word gives a deletion (CR-068 D8, M11).
 *
 * <ul>
 * <li>{@code manual-redline.docx}: the base, the input of the conversion;</li>
 * <li>{@code manual-redline-default.docx}: the default conversion (D2, D5, D6, D8 to D10), author
 * {@code manual redline}, one fixed instant;</li>
 * <li>{@code manual-redline-optins.docx}: every opt-in of &#xa7;3.3 at once: style strike,
 * underline and the colour FF0000 as insertions, the comments story;</li>
 * <li>{@code manual-redline-d9.docx}: the default with M11's struck paragraph mark deleted too, the
 * alternative D9 leaves to the measurement;</li>
 * <li>{@code manual-redline-dates.docx}: four deletions differing only in {@code w:date} and
 * {@code w16du:dateUtc}, for which of the two Word shows (D4);</li>
 * <li>{@code manual-redline-phase1.docx}: {@link ManualRedline}'s own conversion of the base (phase 1),
 * for Word's second run.</li>
 * </ul>
 *
 * <p>The conversions write the date as Word 365 writes its own: {@code w16du:dateUtc} the instant
 * (2026-10-31T00:00:00Z), {@code w:date} the same instant as the wall clock of Word's machine
 * (Australia/Sydney, +11:00 that day) with a {@code Z}, which Word reads as local time whatever it
 * says (TrackedChange.getDate). Every text is ASCII, so Word's Save As Plain Text cannot hit its
 * code-page defect (the portfolio's word_defects.md, WD-001).</p>
 *
 * <p>Usage: {@code ManualRedlineProbeBuilder <outDir>}.</p>
 *
 * @since 17.3.5
 */
public class ManualRedlineProbeBuilder {

	enum Variant {
		BASE("manual-redline.docx", "the base: a manual redline, nothing tracked"),
		DEFAULT("manual-redline-default.docx", "the default conversion of CR-068, as predicted (written by hand)"),
		OPTINS("manual-redline-optins.docx", "every opt-in at once: style strike, underline and FF0000 as insertions, comments"),
		D9("manual-redline-d9.docx", "the default conversion with the struck paragraph mark of M11 deleted too (the D9 alternative)");

		final String file;
		final String label;

		Variant(String file, String label) {
			this.file = file;
			this.label = label;
		}
	}

	static final String NS = " xmlns:w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'"
			+ " xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships'"
			+ " xmlns:wp='http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing'"
			+ " xmlns:a='http://schemas.openxmlformats.org/drawingml/2006/main'"
			+ " xmlns:wps='http://schemas.microsoft.com/office/word/2010/wordprocessingShape'"
			+ " xmlns:mc='http://schemas.openxmlformats.org/markup-compatibility/2006'"
			+ " xmlns:v='urn:schemas-microsoft-com:vml'"
			+ " xmlns:o='urn:schemas-microsoft-com:office:office'"
			+ " xmlns:w14='http://schemas.microsoft.com/office/word/2010/wordml'"
			+ " xmlns:w15='http://schemas.microsoft.com/office/word/2012/wordml'"
			+ " xmlns:w16du='http://schemas.microsoft.com/office/word/2023/wordml/word16du'"
			+ " mc:Ignorable='w14 w15 w16du'";

	static final String AUTHOR = "manual redline";

	/** The conversion's instant, and its wall clock on Word's machine (Australia/Sydney, AEDT) */
	static final String DATE_UTC = "2026-10-31T00:00:00Z";
	static final String DATE_LOCAL = "2026-10-31T11:00:00Z";

	static final String STRIKE = "<w:strike/>";
	static final String U = "<w:u w:val='single'/>";
	static final String LINK = "<w:rStyle w:val='Hyperlink'/>";
	static final String STYLE_STRIKE = "<w:rStyle w:val='StruckText'/>";

	final Variant v;
	/** Annotation ids the base uses: comment 0, M07's insertion 100; the conversions' from 101 */
	int nextId = 101;
	int docPrId = 1;

	ManualRedlineProbeBuilder(Variant v) {
		this.v = v;
	}

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		for (Variant v : Variant.values()) {
			Files.write(new File(out, v.file).toPath(), new ManualRedlineProbeBuilder(v).build());
			System.out.println("wrote " + new File(out, v.file));
		}
		Files.write(new File(out, "manual-redline-dates.docx").toPath(), new ManualRedlineProbeBuilder(Variant.BASE).dates());
		System.out.println("wrote " + new File(out, "manual-redline-dates.docx"));
		Files.write(new File(out, "manual-redline-phase1.docx").toPath(), phase1(new ManualRedlineProbeBuilder(Variant.BASE).build()));
		System.out.println("wrote " + new File(out, "manual-redline-phase1.docx"));
	}

	/**
	 * CR-068 phase 1's own conversion of the base, the default options (the separating space taken,
	 * D13; the struck mark deleted, D9 as revisited) at the probe's instant in Word's zone: what Word is
	 * asked about the second time (CR-068 section 11).
	 */
	static byte[] phase1(byte[] base) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(base));
		pkg.setDateTimeZone(java.time.ZoneId.of("Australia/Sydney"));
		ManualRedline.toTrackedChanges(pkg, new ManualRedlineOptions().date(java.time.Instant.parse(DATE_UTC)));
		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		Docx4J.save(pkg, saved, Docx4J.FLAG_NONE);
		return rewrite(saved.toByteArray(), new LinkedHashMap<String, String>());
	}

	/** Converted by the default rules (direct strike and double strike to w:del) */
	boolean converted() {
		return v != Variant.BASE;
	}

	/** The opt-ins too */
	boolean optins() {
		return v == Variant.OPTINS;
	}

	// ------------------------------------------------------------------ the cases

	byte[] build() throws Exception {
		Package pkg = new Package();
		StringBuilder b = new StringBuilder();
		b.append(p(r("CR-068 phase 0 probe: " + v.label + ". Built by docx4j (ManualRedlineProbeBuilder); "
				+ "the text of each case starts with its tag, M01 to M17.")));

		// M01: dpp-forks C03, a struck value, a space, its underlined replacement, in prose
		b.append(p(r("M01 Rate: ") + strike("", "1.316") + r(" ") + underlined("1.335") + r(" per cent.")));

		// M02: dpp-forks C04, the same in a table cell, no space between
		b.append(table(new int[] { 2000, 2000 }, row(cell(2000, p(r("M02 cell:"))),
				cell(2000, p(strike("", "1.316") + underlined("1.335"))))));

		// M03: a contract clause, a struck sentence (its space inside the strike) and its underlined replacement
		b.append(p(r("M03 The Supplier shall deliver the Goods within 30 days of the Order Date. ")
				+ strike("", "Late delivery incurs no penalty. ") + underlined("Late delivery incurs a penalty of 2 per cent a week.")));

		// M04: a price table: a struck price with the new one underlined beside it, and a row struck whole
		b.append(table(new int[] { 2400, 3600 },
				row(cell(2400, p(r("M04 Item"))), cell(3600, p(r("Price"))))
						+ row(cell(2400, p(r("Widget"))), cell(3600, p(strike("", "$40.00") + r(" ") + underlined("$35.00"))))
						+ row(cell(2400, p(r("Gadget"))), cell(3600, p(r("$12.00"))))
						+ row(cell(2400, p(strike("", "Gizmo"))), cell(3600, p(strike("", "$99.00"))))));

		// M05: strike from a character style only: converted only under styleStrike(true)
		b.append(p(r("M05 ") + (optins() ? del(dr(STYLE_STRIKE + "<w:strike w:val='0'/>", "style-struck words"))
				: r(STYLE_STRIKE, "style-struck words")) + r(" then plain words.")));

		// M06: the same style, its strike turned off by the run's own w:strike w:val="0": nothing to convert
		b.append(p(r("M06 ") + r(STYLE_STRIKE + "<w:strike w:val='0'/>", "style strike turned off by the run") + r(" then plain.")));

		// M07: struck text inside a tracked insertion: left alone (in_tracked_change)
		b.append(p(r("M07 ") + "<w:ins w:id='100' w:author='CR-068 base' w:date='2026-10-30T09:00:00Z'>"
				+ r(STRIKE, "struck inserted words") + "</w:ins>" + r(" after the insertion.")));

		// M08: a field whose every run is struck, and a struck run in a fldSimple: left alone (field)
		b.append(p(r("M08 Quoted: ") + field(STRIKE, " QUOTE \"Version 2\" ", "Version 2") + r(" (every run of the field struck).")));
		b.append(p(r("M08b Quoted: ") + "<w:fldSimple w:instr=' QUOTE &quot;simple field&quot; '>" + r(STRIKE, "simple field")
				+ "</w:fldSimple>" + r(" (a struck run in a fldSimple).")));

		// M09: a struck hyperlink beside its replacement: the deletion goes inside the hyperlink
		String oldLink = pkg.hyperlink("https://example.com/old-terms");
		String newLink = pkg.hyperlink("https://example.com/new-terms");
		b.append(p(r("M09 See ") + "<w:hyperlink r:id='" + oldLink + "'>" + strike(LINK, "example.com/old-terms") + "</w:hyperlink>"
				+ r(" ") + "<w:hyperlink r:id='" + newLink + "'>" + r(LINK, "example.com/new-terms") + "</w:hyperlink>"
				+ r(" for the terms.")));

		// M10: a text box (both mc branches), a footnote and the header
		b.append(p(r("M10 A text box follows, and a footnote") + footnoteRef(1) + r(": ") + textBox()));

		// M11: a wholly struck paragraph, its mark struck too (D9)
		String mark = v == Variant.D9 ? "<w:pPr><w:rPr>" + mark("del") + "</w:rPr></w:pPr>" : "<w:pPr><w:rPr>" + STRIKE + "</w:rPr></w:pPr>";
		b.append("<w:p>" + mark + strike("", "M11 This whole paragraph is struck through, its mark too.") + "</w:p>");
		b.append(p(r("M11 after: the paragraph after the struck one.")));

		// M12: a red run beside black text: converted only under colourAsInsertion("FF0000")
		b.append(p(r("M12 The party shall ") + (optins() ? ins(r("", "promptly ")) : r("<w:color w:val='FF0000'/>", "promptly "))
				+ r("notify the other.")));

		// M13: double strike
		b.append(p(r("M13 Fee: ") + (converted() ? del(dr("", "EUR 500")) : r("<w:dstrike/>", "EUR 500")) + r(" ")
				+ underlined("EUR 450") + r(" a month.")));

		// M14: two adjacent struck runs of different sizes (one w:del), a kept run, a third struck run (a second w:del)
		String s24 = "<w:sz w:val='24'/><w:szCs w:val='24'/>";
		String s32 = "<w:sz w:val='32'/><w:szCs w:val='32'/>";
		b.append(p(r("M14 ") + (converted() ? del(dr(s24, "first struck run, ") + dr(s32, "second struck run, "))
				: r(STRIKE + s24, "first struck run, ") + r(STRIKE + s32, "second struck run, "))
				+ r("kept words, ") + strike("", "third struck run ") + r("end.")));

		// M15: a struck run holding a tab and a break
		b.append(p(r("M15 Before ") + (converted()
				? del("<w:r>" + dt("old A") + "<w:tab/>" + dt("old B") + "<w:br/>" + dt("old C ") + "</w:r>")
				: "<w:r><w:rPr>" + STRIKE + "</w:rPr>" + t("old A") + "<w:tab/>" + t("old B") + "<w:br/>" + t("old C ") + "</w:r>")
				+ r("after.")));

		// M16: struck text in a comment: converted only under comments(true)
		b.append(p("<w:commentRangeStart w:id='0'/>" + r("M16 A comment is anchored on these words.")
				+ "<w:commentRangeEnd w:id='0'/>" + commentRef(0)));

		// M17: a struck run and its underlined replacement in an inline content control
		b.append(p(r("M17 Party: ") + "<w:sdt><w:sdtPr><w:alias w:val='Party'/><w:tag w:val='party'/><w:id w:val='1700'/>"
				+ "</w:sdtPr><w:sdtContent>" + strike("", "Acme Pty Ltd") + r(" ") + underlined("Acme Holdings Pty Ltd")
				+ "</w:sdtContent></w:sdt>" + r(".")));

		b.append(p(r("End of the probe.")));
		b.append("<w:sectPr><w:headerReference w:type='default' r:id='" + pkg.headerId + "'/>" + PAGE + "</w:sectPr>");

		String header = p(r("M10 header: ") + strike("", "Draft ") + r("Final version"));
		String footnote = note(1, r("M10 footnote: the rate is ") + strike("", "1.316") + r(" ") + underlined("1.335") + r("."));
		String comment = comment(0, r("M16 comment: ") + (optins() ? del(dr("", "withdrawn remark, ")) : r(STRIKE, "withdrawn remark, "))
				+ r("kept remark."));
		return pkg.write(b.toString(), header, footnote, comment);
	}

	/**
	 * Which of w:date and w16du:dateUtc Word shows: four deletions by {@code manual redline}, D1 a
	 * w:date alone (00:00Z), D2 a dateUtc alone (00:00Z), D3 both, naming different instants on
	 * Word's machine (w:date 00:00Z read as Sydney's midnight; dateUtc 00:00Z, 11:00 there), D4 both
	 * as the conversions write them (the same instant).
	 */
	byte[] dates() throws Exception {
		Package pkg = new Package();
		String attr = "w:author='" + AUTHOR + "'";
		StringBuilder b = new StringBuilder();
		b.append(p(r("CR-068 phase 0 date probe: which date Word shows for a deletion. Built by docx4j (ManualRedlineProbeBuilder).")));
		b.append(p(r("D1 w:date 2026-10-31T00:00:00Z alone: ") + "<w:del w:id='101' " + attr + " w:date='2026-10-31T00:00:00Z'>"
				+ dr("", "deleted D1") + "</w:del>"));
		b.append(p(r("D2 w16du:dateUtc 2026-10-31T00:00:00Z alone: ") + "<w:del w:id='102' " + attr
				+ " w16du:dateUtc='2026-10-31T00:00:00Z'>" + dr("", "deleted D2") + "</w:del>"));
		b.append(p(r("D3 w:date 2026-10-31T00:00:00Z and w16du:dateUtc 2026-10-31T00:00:00Z: ") + "<w:del w:id='103' " + attr
				+ " w:date='2026-10-31T00:00:00Z' w16du:dateUtc='2026-10-31T00:00:00Z'>" + dr("", "deleted D3") + "</w:del>"));
		b.append(p(r("D4 w:date 2026-10-31T11:00:00Z and w16du:dateUtc 2026-10-31T00:00:00Z (as the conversions): ")
				+ "<w:del w:id='104' " + attr + " w:date='" + DATE_LOCAL + "' w16du:dateUtc='" + DATE_UTC + "'>"
				+ dr("", "deleted D4") + "</w:del>"));
		b.append("<w:sectPr><w:headerReference w:type='default' r:id='" + pkg.headerId + "'/>" + PAGE + "</w:sectPr>");
		return pkg.write(b.toString(), p(r("")), note(1, r("")), comment(0, r("")));
	}

	/** A run struck by its own properties: in the conversions, a deletion with the strike removed (D6) */
	String strike(String rPr, String text) {
		return converted() ? del(dr(rPr, text)) : r(rPr + STRIKE, text);
	}

	/** An underlined run: under underlineAsInsertion(true), an insertion with the underline removed (D7) */
	String underlined(String text) {
		return optins() ? ins(r("", text)) : r(U, text);
	}

	/** A text box holding M10's redline, as Word writes one: a wps Choice and its VML Fallback, each converted alike */
	String textBox() {
		int id = docPrId++;
		String choice = "<w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='3200400' cy='457200'/>"
				+ "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='Text Box " + id + "'/>"
				+ "<wp:cNvGraphicFramePr/><a:graphic><a:graphicData uri='http://schemas.microsoft.com/office/word/2010/wordprocessingShape'>"
				+ "<wps:wsp><wps:cNvSpPr txBox='1'/><wps:spPr><a:xfrm><a:off x='0' y='0'/><a:ext cx='3200400' cy='457200'/></a:xfrm>"
				+ "<a:prstGeom prst='rect'><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val='FFFFFF'/></a:solidFill>"
				+ "<a:ln w='6350'><a:solidFill><a:srgbClr val='000000'/></a:solidFill></a:ln></wps:spPr>"
				+ "<wps:txbx><w:txbxContent>" + textBoxParagraph() + "</w:txbxContent></wps:txbx>"
				+ "<wps:bodyPr rot='0' vert='horz' wrap='square' lIns='91440' tIns='45720' rIns='91440' bIns='45720' anchor='t' anchorCtr='0'>"
				+ "<a:noAutofit/></wps:bodyPr></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing>";
		String fallback = "<w:pict><v:shapetype id='_x0000_t202' coordsize='21600,21600' o:spt='202' path='m,l,21600r21600,l21600,xe'>"
				+ "<v:stroke joinstyle='miter'/><v:path gradientshapeok='t' o:connecttype='rect'/></v:shapetype>"
				+ "<v:shape id='Text Box " + id + "' o:spid='_x0000_s1026' type='#_x0000_t202' style='width:252pt;height:36pt' "
				+ "strokeweight='.5pt'><v:textbox><w:txbxContent>" + textBoxParagraph() + "</w:txbxContent></v:textbox></v:shape></w:pict>";
		return "<w:r><mc:AlternateContent><mc:Choice Requires='wps'>" + choice + "</mc:Choice><mc:Fallback>" + fallback
				+ "</mc:Fallback></mc:AlternateContent></w:r>";
	}

	/** Called once per branch, so that each branch's tracked changes take their own ids, as Word's do */
	String textBoxParagraph() {
		return p(r("M10 text box: the price is ") + strike("", "$40.00") + r(" ") + underlined("$35.00") + r("."));
	}

	// ------------------------------------------------------------------ markup

	static String t(String text) {
		return "<w:t xml:space='preserve'>" + text + "</w:t>";
	}

	static String dt(String text) {
		return "<w:delText xml:space='preserve'>" + text + "</w:delText>";
	}

	static String r(String text) {
		return r("", text);
	}

	static String r(String rPr, String text) {
		return "<w:r>" + (rPr.isEmpty() ? "" : "<w:rPr>" + rPr + "</w:rPr>") + t(text) + "</w:r>";
	}

	/** A deleted run: its text as w:delText */
	static String dr(String rPr, String text) {
		return "<w:r>" + (rPr.isEmpty() ? "" : "<w:rPr>" + rPr + "</w:rPr>") + dt(text) + "</w:r>";
	}

	static String p(String content) {
		return "<w:p>" + content + "</w:p>";
	}

	String rev() {
		return "w:id='" + (nextId++) + "' w:author='" + AUTHOR + "' w:date='" + DATE_LOCAL + "' w16du:dateUtc='" + DATE_UTC + "'";
	}

	String del(String runs) {
		return "<w:del " + rev() + ">" + runs + "</w:del>";
	}

	String ins(String runs) {
		return "<w:ins " + rev() + ">" + runs + "</w:ins>";
	}

	/** A paragraph mark's tracked change, w:pPr/w:rPr/w:ins or w:del */
	String mark(String kind) {
		return "<w:" + kind + " " + rev() + "/>";
	}

	static String field(String rPr, String instr, String result) {
		String pr = "<w:rPr>" + rPr + "</w:rPr>";
		return "<w:r>" + pr + "<w:fldChar w:fldCharType='begin'/></w:r>"
				+ "<w:r>" + pr + "<w:instrText xml:space='preserve'>" + instr + "</w:instrText></w:r>"
				+ "<w:r>" + pr + "<w:fldChar w:fldCharType='separate'/></w:r>" + r(rPr, result)
				+ "<w:r>" + pr + "<w:fldChar w:fldCharType='end'/></w:r>";
	}

	static String footnoteRef(int id) {
		return "<w:r><w:rPr><w:rStyle w:val='FootnoteReference'/></w:rPr><w:footnoteReference w:id='" + id + "'/></w:r>";
	}

	static String note(int id, String content) {
		return "<w:footnote w:id='" + id + "'><w:p><w:pPr><w:pStyle w:val='FootnoteText'/></w:pPr><w:r><w:rPr>"
				+ "<w:rStyle w:val='FootnoteReference'/></w:rPr><w:footnoteRef/></w:r>" + r(" ") + content + "</w:p></w:footnote>";
	}

	static String commentRef(int id) {
		return "<w:r><w:rPr><w:rStyle w:val='CommentReference'/></w:rPr><w:commentReference w:id='" + id + "'/></w:r>";
	}

	static String comment(int id, String content) {
		return "<w:comment w:id='" + id + "' w:author='CR-068 base' w:date='2026-10-30T09:00:00Z' w:initials='CB'><w:p><w:pPr>"
				+ "<w:pStyle w:val='CommentText'/></w:pPr><w:r><w:rPr><w:rStyle w:val='CommentReference'/></w:rPr><w:annotationRef/>"
				+ "</w:r>" + content + "</w:p></w:comment>";
	}

	static String table(int[] grid, String rows) {
		String border = "w:val='single' w:sz='4' w:space='0' w:color='000000'";
		StringBuilder t = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w='0' w:type='auto'/><w:tblBorders><w:top " + border
				+ "/><w:left " + border + "/><w:bottom " + border + "/><w:right " + border + "/><w:insideH " + border
				+ "/><w:insideV " + border + "/></w:tblBorders><w:tblLayout w:type='fixed'/></w:tblPr><w:tblGrid>");
		for (int w : grid) t.append("<w:gridCol w:w='" + w + "'/>");
		return t.append("</w:tblGrid>").append(rows).append("</w:tbl>").toString();
	}

	static String row(String... cells) {
		return "<w:tr>" + String.join("", cells) + "</w:tr>";
	}

	static String cell(int width, String content) {
		return "<w:tc><w:tcPr><w:tcW w:w='" + width + "' w:type='dxa'/></w:tcPr>" + content + "</w:tc>";
	}

	static final String PAGE = "<w:pgSz w:w='11906' w:h='16838'/><w:pgMar w:top='1440' w:right='1440' w:bottom='1440' "
			+ "w:left='1440' w:header='708' w:footer='708' w:gutter='0'/><w:cols w:space='708'/>";

	static String settingsXml() {
		return "<w:settings" + NS + "><w:zoom w:percent='100'/><w:defaultTabStop w:val='720'/>"
				+ "<w:characterSpacingControl w:val='doNotCompress'/>"
				+ "<w:footnotePr><w:footnote w:id='-1'/><w:footnote w:id='0'/></w:footnotePr>"
				+ "<w:compat><w:compatSetting w:name='compatibilityMode' w:uri='http://schemas.microsoft.com/office/word' w:val='15'/>"
				+ "</w:compat><w:themeFontLang w:val='en-US'/><w:decimalSymbol w:val='.'/><w:listSeparator w:val=','/></w:settings>";
	}

	static String stylesXml() {
		String small = "<w:pPr><w:spacing w:after='0' w:line='240' w:lineRule='auto'/></w:pPr><w:rPr><w:sz w:val='20'/>"
				+ "<w:szCs w:val='20'/></w:rPr>";
		return "<w:styles" + NS + "><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii='Calibri' w:eastAsia='Calibri' "
				+ "w:hAnsi='Calibri' w:cs='Times New Roman'/><w:sz w:val='22'/><w:szCs w:val='22'/><w:lang w:val='en-US' "
				+ "w:eastAsia='en-US' w:bidi='ar-SA'/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after='160' "
				+ "w:line='259' w:lineRule='auto'/></w:pPr></w:pPrDefault></w:docDefaults>"
				+ "<w:style w:type='paragraph' w:default='1' w:styleId='Normal'><w:name w:val='Normal'/><w:qFormat/></w:style>"
				+ "<w:style w:type='paragraph' w:styleId='FootnoteText'><w:name w:val='footnote text'/><w:basedOn w:val='Normal'/>"
				+ small + "</w:style>"
				+ "<w:style w:type='paragraph' w:styleId='CommentText'><w:name w:val='annotation text'/><w:basedOn w:val='Normal'/>"
				+ small + "</w:style>"
				+ "<w:style w:type='character' w:default='1' w:styleId='DefaultParagraphFont'><w:name w:val='Default Paragraph Font'/>"
				+ "<w:uiPriority w:val='1'/><w:semiHidden/><w:unhideWhenUsed/></w:style>"
				+ "<w:style w:type='character' w:styleId='StruckText'><w:name w:val='Struck Text'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:rPr><w:strike/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='Hyperlink'><w:name w:val='Hyperlink'/><w:basedOn w:val='DefaultParagraphFont'/>"
				+ "<w:unhideWhenUsed/><w:rPr><w:color w:val='0563C1'/><w:u w:val='single'/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='FootnoteReference'><w:name w:val='footnote reference'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:rPr><w:vertAlign w:val='superscript'/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='CommentReference'><w:name w:val='annotation reference'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:rPr><w:sz w:val='16'/><w:szCs w:val='16'/></w:rPr></w:style>"
				+ "<w:style w:type='table' w:default='1' w:styleId='TableNormal'><w:name w:val='Normal Table'/><w:uiPriority w:val='99'/>"
				+ "<w:semiHidden/><w:unhideWhenUsed/><w:tblPr><w:tblInd w:w='0' w:type='dxa'/><w:tblCellMar><w:top w:w='0' w:type='dxa'/>"
				+ "<w:left w:w='108' w:type='dxa'/><w:bottom w:w='0' w:type='dxa'/><w:right w:w='108' w:type='dxa'/></w:tblCellMar>"
				+ "</w:tblPr></w:style>"
				+ "<w:style w:type='numbering' w:default='1' w:styleId='NoList'><w:name w:val='No List'/><w:uiPriority w:val='99'/>"
				+ "<w:semiHidden/><w:unhideWhenUsed/></w:style></w:styles>";
	}

	// ------------------------------------------------------------------ the package

	/** The parts docx4j makes and registers (relationships, content types); their XML replaced by the strings written here */
	static class Package {
		final WordprocessingMLPackage pkg;
		final MainDocumentPart mdp;
		final HeaderPart header;
		final FootnotesPart footnotes;
		final CommentsPart comments;
		final DocumentSettingsPart settings;
		final String headerId;

		Package() throws Exception {
			pkg = WordprocessingMLPackage.createPackage(PageSizePaper.A4, false);
			mdp = pkg.getMainDocumentPart();
			header = new HeaderPart(new PartName("/word/header1.xml"));
			header.setJaxbElement(new Hdr());
			headerId = mdp.addTargetPart(header).getId();
			footnotes = new FootnotesPart();
			footnotes.setJaxbElement(new CTFootnotes());
			mdp.addTargetPart(footnotes);
			comments = new CommentsPart();
			comments.setJaxbElement(new Comments());
			mdp.addTargetPart(comments);
			DocumentSettingsPart s = mdp.getDocumentSettingsPart();
			if (s == null) {
				s = new DocumentSettingsPart();
				s.setJaxbElement(new CTSettings());
				mdp.addTargetPart(s);
			}
			settings = s;
		}

		String hyperlink(String target) {
			Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
			rel.setType(Namespaces.HYPERLINK);
			rel.setTarget(target);
			rel.setTargetMode("External");
			mdp.getRelationshipsPart().addRelationship(rel);
			return rel.getId();
		}

		byte[] write(String body, String headerContent, String footnote, String comment) throws Exception {
			Map<String, String> raw = new LinkedHashMap<String, String>();
			raw.put(mdp.getPartName().getName(), "<w:document" + NS + "><w:body>" + body + "</w:body></w:document>");
			raw.put(header.getPartName().getName(), "<w:hdr" + NS + ">" + headerContent + "</w:hdr>");
			raw.put(footnotes.getPartName().getName(), "<w:footnotes" + NS + ">" + separators() + footnote + "</w:footnotes>");
			raw.put(comments.getPartName().getName(), "<w:comments" + NS + ">" + comment + "</w:comments>");
			raw.put(mdp.getStyleDefinitionsPart().getPartName().getName(), stylesXml());
			raw.put(settings.getPartName().getName(), settingsXml());
			ByteArrayOutputStream saved = new ByteArrayOutputStream();
			Docx4J.save(pkg, saved, Docx4J.FLAG_NONE);
			return rewrite(saved.toByteArray(), raw);
		}

		static String separators() {
			String spacing = "<w:pPr><w:spacing w:after='0' w:line='240' w:lineRule='auto'/></w:pPr>";
			return "<w:footnote w:type='separator' w:id='-1'><w:p>" + spacing + "<w:r><w:separator/></w:r></w:p></w:footnote>"
					+ "<w:footnote w:type='continuationSeparator' w:id='0'><w:p>" + spacing + "<w:r><w:continuationSeparator/></w:r>"
					+ "</w:p></w:footnote>";
		}
	}

	/** docx4j's save, the parts named replaced by the XML given (each checked well-formed), every entry dated 1980 */
	static byte[] rewrite(byte[] saved, Map<String, String> raw) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Map<String, byte[]> replacements = new LinkedHashMap<String, byte[]>();
		for (Map.Entry<String, String> e : raw.entrySet()) {
			String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\r\n" + e.getValue();
			byte[] bytes = xml.replace('\'', '"').getBytes(StandardCharsets.UTF_8);
			dbf.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));   // well-formed, or this throws
			replacements.put(e.getKey().substring(1), bytes);
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Set<String> written = new LinkedHashSet<String>();
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(saved));
				ZipOutputStream zip = new ZipOutputStream(out)) {
			ZipEntry e;
			while ((e = in.getNextEntry()) != null) {
				byte[] bytes = replacements.containsKey(e.getName()) ? replacements.get(e.getName()) : readAll(in);
				ZipEntry entry = new ZipEntry(e.getName());
				entry.setTimeLocal(java.time.LocalDateTime.of(1980, 1, 1, 0, 0));
				zip.putNextEntry(entry);
				zip.write(bytes);
				zip.closeEntry();
				written.add(e.getName());
			}
		}
		for (String name : replacements.keySet()) {
			if (!written.contains(name)) throw new IllegalStateException("no part " + name + " in docx4j's save");
		}
		return out.toByteArray();
	}

	static byte[] readAll(InputStream in) throws Exception {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
		return b.toByteArray();
	}
}
