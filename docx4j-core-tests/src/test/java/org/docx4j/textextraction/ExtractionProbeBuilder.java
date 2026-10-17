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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.dml.diagram.CTDiagramDefinition;
import org.docx4j.model.chart.ChartCreator;
import org.docx4j.model.chart.ChartDefinition;
import org.docx4j.model.chart.ChartType;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.R;
import org.glox4j.openpackaging.packages.GloxPackage;

/**
 * The text extraction probe: one document with a marker token per case, for testing what a reader
 * extracts from a docx (which {@code mc:AlternateContent} branch it takes, which revision view it
 * reads, what it drops or duplicates), whether the reader is a library or a hosted assistant given
 * the file. It is the first fixture of the portfolio's text model (docx4j-portfolio
 * {@code docs/text_extraction_behaviour_model.md}, section 8), whose sections 3 to 5 the expected
 * results here follow. Every marker is upper case with digits and underscores and occurs once in the
 * file, unless its case is about a duplicate; every case starts with a heading "Case n: ...".
 *
 * <p>The story parts (document, styles, settings, numbering, headers, footers, notes, comments) are
 * written here as XML text, not through JAXB: the {@code mc:AlternateContent} cases need namespace
 * declarations that no element uses (a {@code Requires} names a prefix), which JAXB drops. docx4j
 * makes everything else (relationships, content types, the chart, pictures, SmartArt, the custom XML
 * part, the altChunk). The SmartArt layout is Hier2Level.glox, read from docx4j-samples-glox4j.</p>
 *
 * <p>Usage, from the repository root: {@code ExtractionProbeBuilder <outDir>} writes
 * {@code extraction-probe.docx}, {@code expected-tokens.tsv} and {@code README.md}.</p>
 *
 * @since 17.3.4
 */
public class ExtractionProbeBuilder {

	static final String NS = " xmlns:w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'"
			+ " xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships'"
			+ " xmlns:wp='http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing'"
			+ " xmlns:a='http://schemas.openxmlformats.org/drawingml/2006/main'"
			+ " xmlns:pic='http://schemas.openxmlformats.org/drawingml/2006/picture'"
			+ " xmlns:wps='http://schemas.microsoft.com/office/word/2010/wordprocessingShape'"
			+ " xmlns:mc='http://schemas.openxmlformats.org/markup-compatibility/2006'"
			+ " xmlns:v='urn:schemas-microsoft-com:vml'"
			+ " xmlns:o='urn:schemas-microsoft-com:office:office'"
			+ " xmlns:w10='urn:schemas-microsoft-com:office:word'"
			+ " xmlns:w14='http://schemas.microsoft.com/office/word/2010/wordml'"
			+ " xmlns:w15='http://schemas.microsoft.com/office/word/2012/wordml'"
			+ " xmlns:m='http://schemas.openxmlformats.org/officeDocument/2006/math'"
			+ " mc:Ignorable='w14 w15'";

	/** The namespace no reader understands, declared where a Requires names it */
	static final String ZZ9 = " xmlns:zz9='http://example.com/zz9'";

	static final String WPS_URI = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";

	static final String DATE = "2026-10-17T00:00:00Z";

	/** The custom XML part case 23 binds to */
	static final String STORE_ITEM = "{6A0C3C7E-3B1F-4E21-9C5A-7E2D1F0B2A23}";
	static final String PROBE_NS = "urn:docx4j:text-extraction-probe";

	static final String LAYOUT_ID = "urn:docx4j.org/text-extraction-probe/hier2level";

	/** Marker tokens: an upper-case word, then underscore-joined upper-case words and digits */
	static final Pattern TOKEN = Pattern.compile("[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+");

	/** A marker and how often it appears in each view (README, "Columns") */
	static final class Token {
		final String name, accepted, original, markup, option, withOption, note;

		Token(String name, String accepted, String original, String markup, String option, String withOption,
				String note) {
			this.name = name;
			this.accepted = accepted;
			this.original = original;
			this.markup = markup;
			this.option = option;
			this.withOption = withOption;
			this.note = note;
		}
	}

	static final class Case {
		final int n;
		final String title;
		final List<Token> tokens = new ArrayList<Token>();
		String note = "";

		Case(int n, String title) {
			this.n = n;
			this.title = title;
		}
	}

	final List<Case> cases = new ArrayList<Case>();
	Case current;
	final StringBuilder body = new StringBuilder();
	/** part name to the XML written over docx4j's */
	final Map<String, String> raw = new LinkedHashMap<String, String>();
	int docPrId = 1;
	int revId = 1;
	int shapeId = 1025;
	int sdtId = 701;

	public static void main(String[] args) throws Exception {
		File out = new File(args.length > 0 ? args[0] : ".");
		out.mkdirs();
		ExtractionProbeBuilder b = new ExtractionProbeBuilder();
		byte[] docx = b.build(gloxFile());
        byte[] external = ExtractionPhase0Cases.external(b);
        ExtractionFixtureChecks.check(docx, external, b.cases);
        WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
        WordprocessingMLPackage.load(new ByteArrayInputStream(external));
        Files.write(new File(out, "extraction-probe.docx").toPath(), docx);
        Files.write(new File(out, "extraction-probe-external.docm").toPath(), external);
        Files.writeString(new File(out, "expected-tokens.tsv").toPath(), b.tsv(), StandardCharsets.UTF_8);
        Files.writeString(new File(out, "expected-findings.tsv").toPath(), ExtractionFixtureChecks.findings(), StandardCharsets.UTF_8);
        Files.writeString(new File(out, "case-statements.tsv").toPath(), b.statements(), StandardCharsets.UTF_8);
        Files.writeString(new File(out, "README.md").toPath(), b.readme(), StandardCharsets.UTF_8);
        java.nio.file.Path goldens = new File(out, "goldens.tsv").toPath();
        if (!Files.exists(goldens)) Files.writeString(goldens,
                "fixture\tcase\tview\tscenario\tformat\tgolden\tstatements\n", StandardCharsets.UTF_8);
        System.out.println("wrote " + out + ", " + b.cases.size() + " cases, " + b.tokenCount() + " tokens");
	}

	static File gloxFile() {
		for (String dir : new String[] { ".", ".." }) {
			File f = new File(dir, "docx4j-samples-glox4j/sample-docs/Hier2Level.glox");
			if (f.exists()) return f;
		}
		throw new IllegalStateException("run from the repository root: Hier2Level.glox not found");
	}

	// ------------------------------------------------------------------ the document

	byte[] build(File glox) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage(PageSizePaper.A4, false);
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		// parts docx4j registers, whose XML is written below (raw)
		HeaderPart hDefault = header(mdp, "/word/header1.xml");
		HeaderPart hFirst = header(mdp, "/word/header2.xml");
		HeaderPart hEven = header(mdp, "/word/header3.xml");
		HeaderPart hSection2 = header(mdp, "/word/header4.xml");
		header(mdp, "/word/header5.xml");   // no section references it (case 26)
		FooterPart fDefault = new FooterPart(new PartName("/word/footer1.xml"));
		fDefault.setJaxbElement(new Ftr());
		String fDefaultId = mdp.addTargetPart(fDefault).getId();
		FootnotesPart footnotes = new FootnotesPart();
		footnotes.setJaxbElement(new CTFootnotes());
		mdp.addTargetPart(footnotes);
		EndnotesPart endnotes = new EndnotesPart();
		endnotes.setJaxbElement(new CTEndnotes());
		mdp.addTargetPart(endnotes);
		CommentsPart comments = new CommentsPart();
		comments.setJaxbElement(new Comments());
		mdp.addTargetPart(comments);
		CommentsExtendedPart commentsEx = new CommentsExtendedPart();
		commentsEx.setJaxbElement(new org.docx4j.w15.CTCommentsEx());
		mdp.addTargetPart(commentsEx);
		NumberingDefinitionsPart numbering = new NumberingDefinitionsPart();
		numbering.setJaxbElement(new Numbering());
		mdp.addTargetPart(numbering);
		DocumentSettingsPart settings = mdp.getDocumentSettingsPart();
		if (settings == null) {
			settings = new DocumentSettingsPart();
			settings.setJaxbElement(new CTSettings());
			mdp.addTargetPart(settings);
		}

		// parts docx4j makes
		Relationship hyperlink = new org.docx4j.relationships.ObjectFactory().createRelationship();
		hyperlink.setType(Namespaces.HYPERLINK);
		hyperlink.setTarget("https://example.com/FLD21_URL_MARK");
		hyperlink.setTargetMode("External");
		mdp.getRelationshipsPart().addRelationship(hyperlink);
		customXml(mdp);
		properties(pkg);
		BinaryPartAbstractImage png33a = BinaryPartAbstractImage.createImagePart(pkg, png(96, 48, 0x3366cc));
		BinaryPartAbstractImage png33b = BinaryPartAbstractImage.createImagePart(pkg, png(96, 48, 0x99aa33));
		BinaryPartAbstractImage png33c = BinaryPartAbstractImage.createImagePart(pkg, png(96, 48, 0xffe08a));
		BinaryPart svg33 = new BinaryPart(new PartName("/word/media/probe-svg33.svg"));
		svg33.setContentType(new ContentType("image/svg+xml"));
		svg33.setRelationshipType(Namespaces.IMAGE);
		svg33.setBinaryData(("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"96\" height=\"48\" viewBox=\"0 0 96 48\">"
				+ "<rect width=\"96\" height=\"48\" fill=\"#ffe08a\"/>"
				+ "<text x=\"4\" y=\"28\" font-family=\"Arial\" font-size=\"8\">SVG33_INNER_TEXT</text></svg>")
				.getBytes(StandardCharsets.UTF_8));
		String svg33Id = mdp.addTargetPart(svg33).getId();
		AlternativeFormatInputPart chunk = new AlternativeFormatInputPart(new PartName("/word/afchunk.htm"));
		chunk.setBinaryData(("<html><head><meta charset=\"utf-8\"><title>chunk</title></head>"
				+ "<body><p>ALT36_CHUNK_TEXT</p></body></html>").getBytes(StandardCharsets.UTF_8));
		String chunkId = mdp.addTargetPart(chunk).getId();
		String smartArt = smartArt(mdp, glox);
		String chart = chart(pkg);

		StringBuilder hdrDefault = new StringBuilder();
		StringBuilder fns = new StringBuilder();
		StringBuilder ens = new StringBuilder();
		StringBuilder cmts = new StringBuilder();

		body.append("<w:p><w:pPr><w:pStyle w:val='Title'/></w:pPr>" + r("Text extraction probe") + "</w:p>");
		body.append(pt("Built by docx4j (ExtractionProbeBuilder, in docx4j-core-tests). Each case starts with a "
				+ "heading, Case and its number. Marker tokens are upper-case words joined by underscores; the "
				+ "README beside this file says where each should appear."));

		// ---------------------------------------------------------- mc:AlternateContent
		begin(1, "a text box: a wps Choice and a VML Fallback with different text");
		body.append(p(r("The box: ") + "<w:r>" + ac("", choice("wps", wpsInline("wps", pt("CHOICE_ALPHA_123")))
				+ fallback(vmlBox(pt("FALLBACK_BETA_456")))) + "</w:r>"));
		box("CHOICE_ALPHA_123", "");
		never("FALLBACK_BETA_456", "the Fallback; Word never shows it");

		begin(2, "a wps Choice and no Fallback");
		body.append(p(r("The box: ") + "<w:r>" + ac("", choice("wps", wpsInline("wps", pt("CHOICE_ONLY_789"))))
				+ "</w:r>"));
		box("CHOICE_ONLY_789", "");

		begin(3, "a Choice requiring an unknown namespace, and a Fallback, each a run");
		body.append(p(r("The runs: ") + ac(ZZ9, choice("zz9", r("UNSUPPORTED_GAMMA ")) + fallback(r("SUPPORTED_DELTA ")))));
		never("UNSUPPORTED_GAMMA", "Requires zz9, which no reader understands");
		always("SUPPORTED_DELTA", "");

		begin(4, "two Choices, the first requiring an unknown namespace, the second wps; a Fallback");
		body.append(p(r("The box: ") + "<w:r>" + ac(ZZ9, choice("zz9", "<w:t xml:space='preserve'>CHOICE1_UNKNOWN_ZETA </w:t>")
				+ choice("wps", wpsInline("wps", pt("CHOICE2_WPS_ETA")))
				+ fallback(vmlBox(pt("FALLBACK_EPSILON")))) + "</w:r>"));
		never("CHOICE1_UNKNOWN_ZETA", "");
		box("CHOICE2_WPS_ETA", "the first Choice whose Requires is understood");
		never("FALLBACK_EPSILON", "");

		begin(5, "two Choices both understood (w14, then wps), each a run; a Fallback");
		body.append(p(r("The runs: ") + ac("", choice("w14", r("MCE5_FIRST_SUPPORTED "))
				+ choice("wps", r("MCE5_SECOND_SUPPORTED ")) + fallback(r("MCE5_FALLBACK ")))));
		always("MCE5_FIRST_SUPPORTED", "the first understood Choice wins");
		never("MCE5_SECOND_SUPPORTED", "");
		never("MCE5_FALLBACK", "");

		begin(6, "the wps namespace bound to another prefix (shape2010)");
		body.append(p(r("The box: ") + "<w:r>" + ac(" xmlns:shape2010='" + WPS_URI + "'",
				choice("shape2010", wpsInline("shape2010", pt("MCE6_REBOUND_CHOICE")))
				+ fallback(vmlBox(pt("MCE6_FALLBACK")))) + "</w:r>"));
		box("MCE6_REBOUND_CHOICE", "the namespace URI decides, not the prefix: a reader matching the prefix wps takes the Fallback");
		never("MCE6_FALLBACK", "");

		begin(7, "a Choice requiring two namespaces, one unknown (wps zz9)");
		body.append(p(r("The box: ") + "<w:r>" + ac(ZZ9, choice("wps zz9", wpsInline("wps", pt("MCE7_TWO_REQ_CHOICE")))
				+ fallback(vmlBox(pt("MCE7_FALLBACK")))) + "</w:r>"));
		never("MCE7_TWO_REQ_CHOICE", "every namespace a Choice requires must be understood");
		box("MCE7_FALLBACK", "");

		begin(8, "nested: a text box in a wps Choice whose paragraph holds another AlternateContent");
		body.append(p(r("The box: ") + "<w:r>" + ac("", choice("wps", wpsInline("wps",
				p(r("MCE8_OUTER ") + ac("", choice("w14", r("MCE8_INNER_CHOICE ")) + fallback(r("MCE8_INNER_FALLBACK "))))))
				+ fallback(vmlBox(pt("MCE8_OUTER_FALLBACK")))) + "</w:r>"));
		box("MCE8_OUTER", "");
		box("MCE8_INNER_CHOICE", "");
		never("MCE8_INNER_FALLBACK", "");
		never("MCE8_OUTER_FALLBACK", "the outer Fallback (a VML box)");

		// ---------------------------------------------------------- tracked changes
		begin(9, "an insertion and a deletion in one paragraph");
		body.append(p(r("TC9_BASE ") + ins(r("TC9_INSERTED ")) + delRun("TC9_DELETED")));
		always("TC9_BASE", "");
		inserted("TC9_INSERTED", "");
		deleted("TC9_DELETED", "");

		begin(10, "a move from paragraph A to paragraph B, with its range markers");
		body.append(p(r("TC10_PARA_A ") + "<w:moveFromRangeStart " + rev() + " w:name='move10'/>"
				+ "<w:moveFrom " + rev() + ">" + r("TC10_MOVED") + "</w:moveFrom><w:moveFromRangeEnd w:id='"
				+ (revId - 2) + "'/>"));
		body.append(p(r("TC10_PARA_B ") + "<w:moveToRangeStart " + rev() + " w:name='move10'/>"
				+ "<w:moveTo " + rev() + ">" + r("TC10_MOVED") + "</w:moveTo><w:moveToRangeEnd w:id='"
				+ (revId - 2) + "'/>"));
		always("TC10_PARA_A", "");
		always("TC10_PARA_B", "");
		token("TC10_MOVED", "1", "1", "del+ins", "", "", "in the file twice (moveFrom, moveTo); accepted: once, after "
				+ "TC10_PARA_B; original: once, after TC10_PARA_A; markup: {--…--} after A and {++…++} after B");

		begin(11, "a deleted paragraph mark between two paragraphs");
		body.append("<w:p><w:pPr><w:rPr><w:del " + rev() + "/></w:rPr></w:pPr>" + r("TC11_FIRST ") + "</w:p>");
		body.append(pt("TC11_SECOND"));
		always("TC11_FIRST", "accepted: one line, TC11_FIRST TC11_SECOND; original and markup: two lines");
		always("TC11_SECOND", "");

		begin(12, "an inserted paragraph mark splitting a paragraph");
		body.append("<w:p><w:pPr><w:rPr><w:ins " + rev() + "/></w:rPr></w:pPr>" + r("TC12_ONE ") + "</w:p>");
		body.append(pt("TC12_TWO"));
		always("TC12_ONE", "accepted and markup: two lines; original: one line, TC12_ONE TC12_TWO");
		always("TC12_TWO", "");

		begin(13, "a table with a plain row, a deleted row and an inserted row");
		body.append("<w:tbl>" + tblPr() + "<w:tblGrid><w:gridCol w:w='6000'/></w:tblGrid>"
				+ "<w:tr>" + tc(6000, "", pt("TC13_PLAIN_ROW")) + "</w:tr>"
				+ "<w:tr><w:trPr><w:del " + rev() + "/></w:trPr>" + tc(6000, "", "<w:p><w:pPr><w:rPr><w:del " + rev()
				+ "/></w:rPr></w:pPr>" + delRun("TC13_DELETED_ROW") + "</w:p>") + "</w:tr>"
				+ "<w:tr><w:trPr><w:ins " + rev() + "/></w:trPr>" + tc(6000, "", "<w:p><w:pPr><w:rPr><w:ins " + rev()
				+ "/></w:rPr></w:pPr>" + ins(r("TC13_INSERTED_ROW")) + "</w:p>") + "</w:tr></w:tbl>");
		body.append(pt("After the table."));
		always("TC13_PLAIN_ROW", "");
		deleted("TC13_DELETED_ROW", "the row is gone in accepted");
		inserted("TC13_INSERTED_ROW", "the row is gone in original");

		begin(14, "a deletion inside an insertion (a second author deletes part of the first author’s insertion)");
		body.append(p("<w:ins " + rev() + ">" + r("TC14_INS_KEPT ") + "<w:del w:id='" + (revId++)
				+ "' w:author='Probe Two' w:date='2026-10-17T01:00:00Z'><w:r><w:delText>TC14_DEL_IN_INS</w:delText></w:r>"
				+ "</w:del></w:ins>"));
		inserted("TC14_INS_KEPT", "");
		token("TC14_DEL_IN_INS", "0", "0", "del", "", "", "the innermost change wins in markup");

		begin(15, "an inserted field and a deleted field");
		body.append(p(r("Inserted: ") + "<w:ins " + rev() + ">" + field(" QUOTE inserted ", "TC15_INSERTED_FIELD", "")
				+ "</w:ins>" + r(" Deleted: ") + "<w:del " + rev() + ">"
				+ "<w:r><w:fldChar w:fldCharType='begin'/></w:r><w:r><w:delInstrText xml:space='preserve'> QUOTE deleted "
				+ "</w:delInstrText></w:r><w:r><w:fldChar w:fldCharType='separate'/></w:r><w:r><w:delText>TC15_DELETED_FIELD"
				+ "</w:delText></w:r><w:r><w:fldChar w:fldCharType='end'/></w:r></w:del>"));
		inserted("TC15_INSERTED_FIELD", "the field's result");
		deleted("TC15_DELETED_FIELD", "the field's result; w:delInstrText is code, never text");

		begin(16, "a formatting-only change");
		body.append(p(r("A run made bold, the change tracked: ") + "<w:r><w:rPr><w:b/><w:rPrChange " + rev()
				+ "><w:rPr/></w:rPrChange></w:rPr><w:t>TC16_FORMAT_ONLY</w:t></w:r>"));
		always("TC16_FORMAT_ONLY", "no marker in markup");

		begin(17, "tracked changes in a text box and in a footnote");
		body.append(p(r("The box (wps, no mc): ") + "<w:r>" + wpsInline("wps", p(r("TC17_BOX_BASE ")
				+ ins(r("TC17_BOX_INS ")) + delRun("TC17_BOX_DEL"))) + "</w:r>"));
		body.append(p(r("TC17_NOTE_REF_HERE ") + noteRef("footnote", 1)));
		fns.append(note("footnote", 1, r("TC17_NOTE_BASE ") + delRun("TC17_NOTE_DEL")));
		box("TC17_BOX_BASE", "");
		token("TC17_BOX_INS", "1", "0", "ins", "text_boxes: false", "0", "");
		token("TC17_BOX_DEL", "0", "1", "del", "text_boxes: false", "0", "");
		always("TC17_NOTE_REF_HERE", "");
		noteText("TC17_NOTE_BASE", "");
		token("TC17_NOTE_DEL", "0", "1", "del", "notes: false", "0", "");

		// ---------------------------------------------------------- hidden and near-hidden
		begin(18, "hidden and near-hidden text");
		body.append(p(r("A run with w:vanish: ") + r("<w:vanish/>", "HID18_VANISH")));
		body.append(p(r("A run whose character style is hidden: ") + r("<w:rStyle w:val='ProbeHidden'/>", "HID18_STYLE_VANISH")));
		body.append("<w:p><w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr>" + r("<w:vanish/>", "HID18_HIDDEN_PARA") + "</w:p>");
		body.append(p(r("White text: ") + r("<w:color w:val='FFFFFF'/>", "HID18_WHITE_TEXT")));
		body.append(p(r("1pt text: ") + r("<w:sz w:val='2'/><w:szCs w:val='2'/>", "HID18_TINY")));
		body.append(p(r("w:webHidden: ") + r("<w:webHidden/>", "HID18_WEBHIDDEN")));
		gated("HID18_VANISH", "hidden: true", "");
		gated("HID18_STYLE_VANISH", "hidden: true", "the style's w:vanish (the paper's phase 2 resolves styles)");
		gated("HID18_HIDDEN_PARA", "hidden: true", "runs and mark hidden: no line at all");
		always("HID18_WHITE_TEXT", "not hidden");
		always("HID18_TINY", "not hidden");
		always("HID18_WEBHIDDEN", "web view only, not hidden (#3)");

		// ---------------------------------------------------------- fields
		begin(19, "complex fields: a marker in the code and the result, and one in the code only");
		body.append(p(r("QUOTE: ") + field(" QUOTE &quot;FLD19_RESULT&quot; ", "FLD19_RESULT", "")));
		body.append(p(r("DOCVARIABLE: ") + field(" DOCVARIABLE FLD19_INSTR_ONLY ", "FLD19_RESULT_TWO", "")));
		token("FLD19_RESULT", "1", "1", "1", "field_codes: true", "2", "in the code and the cached result");
		gated("FLD19_INSTR_ONLY", "field_codes: true", "in the code only");
		always("FLD19_RESULT_TWO", "");

		begin(20, "a simple field");
		body.append(p(r("fldSimple: ") + "<w:fldSimple w:instr=' DOCVARIABLE FLD20_SIMPLE_INSTR '>"
				+ r("FLD20_SIMPLE_RESULT") + "</w:fldSimple>"));
		gated("FLD20_SIMPLE_INSTR", "field_codes: true", "w:fldSimple/@instr");
		always("FLD20_SIMPLE_RESULT", "");

		begin(21, "hyperlinks: a w:hyperlink with r:id, and a HYPERLINK field");
		body.append(p(r("w:hyperlink: ") + "<w:hyperlink r:id='" + hyperlink.getId() + "' w:history='1'>"
				+ r("<w:rStyle w:val='Hyperlink'/>", "FLD21_LINK_TEXT") + "</w:hyperlink>"));
		body.append(p(r("HYPERLINK field: ") + field(" HYPERLINK &quot;https://example.com/FLD21_FIELD_URL_MARK&quot; ",
				"FLD21_FIELD_LINK_TEXT", "<w:rStyle w:val='Hyperlink'/>")));
		always("FLD21_LINK_TEXT", "");
		gated("FLD21_URL_MARK", "markdown", "the target, in the relationship: Markdown [text](target), not plain text");
		always("FLD21_FIELD_LINK_TEXT", "");
		gated("FLD21_FIELD_URL_MARK", "markdown", "the target, in the field code: Markdown carries it whatever field_codes says");

		// ---------------------------------------------------------- content controls
		begin(22, "a content control showing its placeholder");
		body.append(p(r("The control: ") + sdtRun("<w:alias w:val='Probe placeholder'/><w:tag w:val='plc22'/><w:id w:val='"
				+ (sdtId++) + "'/><w:showingPlcHdr/><w:text/>", r("<w:rStyle w:val='PlaceholderText'/>", "PLC22_PLACEHOLDER"))));
		token("PLC22_PLACEHOLDER", "1", "1", "1", "placeholders: false", "0", "Word shows it (#4)");

		begin(23, "a content control bound to a custom XML part, its surface text stale");
		body.append(p(r("The control: ") + sdtRun("<w:alias w:val='Probe bound'/><w:tag w:val='bnd23'/><w:id w:val='"
				+ (sdtId++) + "'/><w:dataBinding w:prefixMappings='xmlns:p=&apos;" + PROBE_NS + "&apos;' "
				+ "w:xpath='/p:probe[1]/p:value[1]' w:storeItemID='" + STORE_ITEM + "'/><w:text/>", r("BND23_SURFACE_STALE"))));
		token("BND23_XML_VALUE", "1", "1", "1", "bindings: surface", "0",
				"the value in customXml/item1.xml, which Word shows (it refreshes the control on open) (#4a)");
		gated("BND23_SURFACE_STALE", "bindings: surface", "the stale cached surface in w:sdtContent");

		begin(24, "a rich text control, and a block-level control in a table cell");
		body.append(p(r("Inline: ") + sdtRun("<w:alias w:val='Probe rich text'/><w:tag w:val='sdt24'/><w:id w:val='"
				+ (sdtId++) + "'/>", r("SDT24_CONTENT"))));
		body.append("<w:tbl>" + tblPr() + "<w:tblGrid><w:gridCol w:w='6000'/></w:tblGrid><w:tr>"
				+ tc(6000, "", "<w:sdt><w:sdtPr><w:alias w:val='Probe block'/><w:tag w:val='sdt24cell'/><w:id w:val='"
						+ (sdtId++) + "'/></w:sdtPr><w:sdtContent>" + pt("SDT24_IN_CELL") + "</w:sdtContent></w:sdt><w:p/>")
				+ "</w:tr></w:tbl>");
		body.append(pt("After the table."));
		always("SDT24_CONTENT", "");
		always("SDT24_IN_CELL", "");

		// ---------------------------------------------------------- text boxes and shapes
		begin(25, "text boxes and legacy WordArt");
		body.append(p(r("A floating DrawingML box: ") + "<w:r>" + wpsAnchor(pt("TBX25_FLOAT")) + "</w:r>"));
		body.append(p(r("An inline DrawingML box: ") + "<w:r>" + wpsInline("wps", pt("TBX25_INLINE")) + "</w:r>"));
		body.append(p(r("A VML box, no mc: ") + "<w:r>" + vmlBox(pt("TBX25_VML_ONLY")) + "</w:r>"));
		body.append("<w:tbl>" + tblPr() + "<w:tblGrid><w:gridCol w:w='6000'/></w:tblGrid><w:tr>"
				+ tc(6000, "", p(r("In a cell: ") + "<w:r>" + wpsInline("wps", pt("TBX25_IN_CELL")) + "</w:r>"))
				+ "</w:tr></w:tbl>");
		body.append(p(r("Legacy WordArt (v:textpath): ") + "<w:r>" + wordArt("WA25_TEXTPATH") + "</w:r>"));
		hdrDefault.append(p(r("HDR26_DEFAULT ") + "<w:r>" + wpsInline("wps", pt("TBX25_IN_HEADER")) + "</w:r>"));
		box("TBX25_FLOAT", "");
		box("TBX25_INLINE", "");
		box("TBX25_VML_ONLY", "");
		box("TBX25_IN_CELL", "after the table (the anchoring paragraph is in a cell)");
		gated("TBX25_IN_HEADER", "include: headers", "a box in section 1's default header");
		token("WA25_TEXTPATH", "1", "1", "1", "text_boxes: false", "0", "v:textpath/@string, as a one-paragraph box (#20b)");

		// ---------------------------------------------------------- stories
		begin(26, "headers and footers over two sections, and a header no section uses");
		body.append(pt("Section 1 has a default, a first-page and an even-page header, and a default footer (header1 "
				+ "also holds the box of case 25). The next paragraph starts section 2, on a new page, with its own default "
				+ "header."));
		body.append("<w:p><w:pPr><w:sectPr><w:headerReference w:type='default' r:id='" + rel(mdp, hDefault) + "'/>"
				+ "<w:headerReference w:type='first' r:id='" + rel(mdp, hFirst) + "'/>"
				+ "<w:headerReference w:type='even' r:id='" + rel(mdp, hEven) + "'/>"
				+ "<w:footerReference w:type='default' r:id='" + fDefaultId + "'/>" + PAGE + "<w:titlePg/></w:sectPr></w:pPr>"
				+ r("The last paragraph of section 1.") + "</w:p>");
		body.append(pt("The first paragraph of section 2."));
		raw.put("/word/header2.xml", "<w:hdr" + NS + ">" + pt("HDR26_FIRST") + "</w:hdr>");
		raw.put("/word/header3.xml", "<w:hdr" + NS + ">" + pt("HDR26_EVEN") + "</w:hdr>");
		raw.put("/word/header4.xml", "<w:hdr" + NS + ">" + pt("HDR26_SECTION2") + "</w:hdr>");
		raw.put("/word/header5.xml", "<w:hdr" + NS + ">" + pt("HDR26_ORPHAN") + "</w:hdr>");
		raw.put("/word/footer1.xml", "<w:ftr" + NS + ">" + pt("FTR26_DEFAULT") + "</w:ftr>");
		gated("HDR26_DEFAULT", "include: headers", "section 1");
		gated("HDR26_FIRST", "include: headers", "section 1's first page (w:titlePg)");
		gated("HDR26_EVEN", "include: headers", "even pages (w:evenAndOddHeaders), section 2's too: it has no even header of its own");
		gated("FTR26_DEFAULT", "include: footers", "section 2 inherits it");
		gated("HDR26_SECTION2", "include: headers", "");
		never("HDR26_ORPHAN", "related from the document but no section references it: Word shows none of it");

		begin(27, "a footnote, an endnote, and a footnote whose reference is deleted");
		body.append(p(r("FN27_REF_HERE ") + noteRef("footnote", 2)));
		fns.append(note("footnote", 2, r("FN27_NOTE_TEXT")));
		body.append(p(r("EN27_REF_HERE ") + noteRef("endnote", 1)));
		ens.append(note("endnote", 1, r("EN27_NOTE_TEXT")));
		body.append(p(r("FN27_DELETED_REF_HERE ") + "<w:del " + rev() + ">" + noteRef("footnote", 3) + "</w:del>"));
		fns.append(note("footnote", 3, r("FN27_DELETED_REF_NOTE")));
		always("FN27_REF_HERE", "");
		noteText("FN27_NOTE_TEXT", "after the body, [n] at the reference (#7)");
		always("EN27_REF_HERE", "");
		noteText("EN27_NOTE_TEXT", "after the footnotes");
		always("FN27_DELETED_REF_HERE", "");
		token("FN27_DELETED_REF_NOTE", "0", "1", "1", "notes: false", "0",
				"its reference is deleted: gone in accepted; in markup the note is there, its reference {--[n]--}");

		begin(28, "comments: one with a reply, and one resolved");
		body.append(p("<w:commentRangeStart w:id='0'/><w:commentRangeStart w:id='1'/>" + r("CMT28_ANCHOR")
				+ "<w:commentRangeEnd w:id='0'/>" + commentRef(0) + "<w:commentRangeEnd w:id='1'/>" + commentRef(1)
				+ r(" and ") + "<w:commentRangeStart w:id='2'/>" + r("CMT28_RESOLVED_ANCHOR") + "<w:commentRangeEnd w:id='2'/>"
				+ commentRef(2)));
		cmts.append(comment(0, "10000001", "CMT28_COMMENT_TEXT"));
		cmts.append(comment(1, "10000002", "CMT28_REPLY"));
		cmts.append(comment(2, "10000003", "CMT28_RESOLVED_COMMENT"));
		always("CMT28_ANCHOR", "");
		always("CMT28_RESOLVED_ANCHOR", "");
		token("CMT28_COMMENT_TEXT", "0", "0", "cmt", "comments: true", "cmt", "{>>…<<} at the end of the commented range (#8)");
		token("CMT28_REPLY", "0", "0", "cmt", "comments: true", "cmt", "a reply (commentsExtended paraIdParent), after its parent");
		token("CMT28_RESOLVED_COMMENT", "0", "0", "cmt", "comments: true", "cmt",
				"resolved (w15:done): a comment like any other; JSON adds resolved: true");

		// ---------------------------------------------------------- characters and structure
		begin(29, "symbols, a numbered list and a Symbol-font bulleted list");
		body.append(p(r("SYM29_MARK ") + "<w:r><w:sym w:font='Wingdings' w:char='F0FC'/></w:r>" + r(" ")
				+ "<w:r><w:sym w:font='Symbol' w:char='F061'/></w:r>"));
		for (String item : new String[] { "LST29_ITEM_A", "LST29_ITEM_B", "LST29_ITEM_C" }) {
			body.append("<w:p><w:pPr><w:pStyle w:val='ListParagraph'/><w:numPr><w:ilvl w:val='0'/><w:numId w:val='1'/>"
					+ "</w:numPr></w:pPr>" + r(item) + "</w:p>");
		}
		for (String item : new String[] { "LST29_BULLET_ONE", "LST29_BULLET_TWO" }) {
			body.append("<w:p><w:pPr><w:pStyle w:val='ListParagraph'/><w:numPr><w:ilvl w:val='0'/><w:numId w:val='2'/>"
					+ "</w:numPr></w:pPr>" + r(item) + "</w:p>");
		}
		always("SYM29_MARK", "then w:sym U+F0FC (Wingdings) and U+F061 (Symbol), as written (#14)");
		always("LST29_ITEM_A", "the labels 1. 2. 3. are not text by default (#9)");
		always("LST29_ITEM_B", "");
		always("LST29_ITEM_C", "");
		always("LST29_BULLET_ONE", "the bullet (Symbol U+F0B7) is not text by default");
		always("LST29_BULLET_TWO", "");

		begin(30, "breaks, a tab and hyphens");
		body.append(p(r("BRK30_BEFORE") + "<w:r><w:br/></w:r>" + r("BRK30_AFTER")));
		body.append(p("<w:r><w:br w:type='page'/></w:r>" + r("BRK30_PAGE")));
		body.append(p("<w:r><w:br w:type='column'/></w:r>" + r("BRK30_COLUMN")));
		body.append(p(r("TAB30_LEFT") + "<w:r><w:tab/></w:r>" + r("TAB30_RIGHT")));
		body.append(p(r("HY30_NON") + "<w:r><w:noBreakHyphen/></w:r>" + r("BREAK") + r(" ") + r("HY30_SO")
				+ "<w:r><w:softHyphen/></w:r>" + r("FT")));
		always("BRK30_BEFORE", "a text-wrapping w:br: \\n (#11)");
		always("BRK30_AFTER", "");
		always("BRK30_PAGE", "after a page break: \\n (#12)");
		always("BRK30_COLUMN", "after a column break: \\n");
		always("TAB30_LEFT", "\\t between the two (#10)");
		always("TAB30_RIGHT", "");
		always("HY30_NON‑BREAK", "w:noBreakHyphen is U+2011 (#13)");
		always("HY30_SO­FT", "w:softHyphen is U+00AD (#13)");

		begin(31, "a table: header row, two-paragraph cell, nested table, merged cell, empty cell");
		body.append("<w:tbl>" + tblPr() + "<w:tblGrid><w:gridCol w:w='3000'/><w:gridCol w:w='3000'/><w:gridCol w:w='3000'/>"
				+ "</w:tblGrid><w:tr><w:trPr><w:tblHeader/></w:trPr>" + tc(9000, "<w:gridSpan w:val='3'/>", pt("TBL31_HEADER"))
				+ "</w:tr><w:tr>" + tc(3000, "", pt("TBL31_CELL_P1") + pt("TBL31_CELL_P2"))
				+ tc(3000, "", "<w:tbl>" + tblPr() + "<w:tblGrid><w:gridCol w:w='2500'/></w:tblGrid><w:tr>"
						+ tc(2500, "", pt("TBL31_NESTED")) + "</w:tr></w:tbl><w:p/>")
				+ tc(3000, "", "<w:p/>") + "</w:tr><w:tr>" + tc(6000, "<w:gridSpan w:val='2'/>", pt("TBL31_MERGED"))
				+ tc(3000, "", pt("TBL31_LAST_CELL")) + "</w:tr></w:tbl>");
		body.append(pt("After the table."));
		always("TBL31_HEADER", "a row is one line, its cells tab-separated (#18)");
		always("TBL31_CELL_P1", "a cell's paragraphs joined by \\n");
		always("TBL31_CELL_P2", "");
		always("TBL31_NESTED", "flattened into its cell");
		always("TBL31_MERGED", "w:gridSpan 2");
		always("TBL31_LAST_CELL", "");

		begin(32, "an equation with a fraction");
		String math = "<w:rPr><w:rFonts w:ascii='Cambria Math' w:hAnsi='Cambria Math'/></w:rPr>";
		body.append("<w:p><m:oMathPara><m:oMath><m:r>" + math + "<m:t>MTH32_A</m:t></m:r><m:r>" + math + "<m:t>+</m:t></m:r>"
				+ "<m:f><m:fPr><m:ctrlPr>" + math.replace("</w:rPr>", "<w:i/></w:rPr>") + "</m:ctrlPr></m:fPr>"
				+ "<m:num><m:r>" + math + "<m:t>MTH32_B</m:t></m:r></m:num><m:den><m:r>" + math + "<m:t>2</m:t></m:r></m:den></m:f>"
				+ "</m:oMath></m:oMathPara></w:p>");
		always("MTH32_A", "the m:t runs in document order (#15); Markdown: LaTeX");
		always("MTH32_B", "");

		begin(33, "pictures: alt text and a title, decorative, and an SVG with text in it");
		body.append(p(r("Alt text and title: ") + picture(rel(mdp, png33a), null, " descr='IMG33_ALT_TEXT' title='IMG33_TITLE'", "")));
		body.append(p(r("Decorative: ") + picture(rel(mdp, png33b), null, " descr='IMG33_DECORATIVE_DESCR'",
				"<a:extLst><a:ext uri='{C183D7F6-B498-43B3-948B-1728B52AA6E4}'>"
						+ "<adec:decorative xmlns:adec='http://schemas.microsoft.com/office/drawing/2017/decorative' val='1'/>"
						+ "</a:ext></a:extLst>")));
		body.append(p(r("SVG, its PNG fallback in a:blip: ") + picture(rel(mdp, png33c), svg33Id, " descr='IMG33_SVG_ALT'", "")));
		gated("IMG33_ALT_TEXT", "alt_text: true",
				"wp:docPr/@descr: plain text [image: descr] with the option; Markdown ![descr](…) by default (#20)");
		never("IMG33_TITLE", "wp:docPr/@title is never read (#20)");
		never("IMG33_DECORATIVE_DESCR", "marked decorative (adec:decorative): no alt text under any option (D22)");
		gated("IMG33_SVG_ALT", "alt_text: true", "as IMG33_ALT_TEXT");
		never("SVG33_INNER_TEXT", "a <text> element in the SVG part: an image's, never text (#20)");

		begin(34, "a chart");
		body.append(p(r("The chart: ") + chart));
		for (String t : new String[] { "CHT34_TITLE", "CHT34_SERIES_ONE", "CHT34_SERIES_TWO", "CHT34_CAT_A", "CHT34_CAT_B" }) {
			token(t, "1", "1", "1", "charts: false", "0", t.equals("CHT34_TITLE")
					? "ChartTable: the title on its own line, then tab-separated rows, after the anchoring paragraph (#20a); "
							+ "the chart's embedded workbook holds these strings too"
					: "");
		}
		token("CHT34_AXIS_TITLE", "1", "1", "1", "charts: false", "0", "REQ-511; pending CR-047 axis-title support");

		begin(35, "SmartArt: a root and two children, stored out of reading order");
		body.append(p(r("The diagram: ") + smartArt));
		for (String t : new String[] { "SMA35_ROOT", "SMA35_CHILD_ONE", "SMA35_CHILD_TWO" }) {
			token(t, "1", "1", "1", "smartart: false", "0", t.equals("SMA35_ROOT")
					? "reading order ROOT, CHILD_ONE, CHILD_TWO (parOf, srcOrd); ptLst order CHILD_TWO, ROOT, CHILD_ONE; "
							+ "cxnLst order ROOT, CHILD_TWO, CHILD_ONE (#20b)"
					: "");
		}

		begin(36, "an altChunk holding HTML");
		body.append("<w:altChunk r:id='" + chunkId + "'/>");
		body.append(pt("After the altChunk."));
		token("ALT36_CHUNK_TEXT", "1", "1", "1", "altchunks: false", "0", "HTML expanded; disabled chunks warn altchunk.not_expanded");

		begin(37, "ruby");
		body.append(p(r("Ruby: ") + "<w:r><w:ruby><w:rubyPr><w:rubyAlign w:val='distributeSpace'/><w:hps w:val='10'/>"
				+ "<w:hpsRaise w:val='20'/><w:hpsBaseText w:val='22'/><w:lid w:val='ja-JP'/></w:rubyPr>"
				+ "<w:rt>" + r("<w:sz w:val='10'/>", "RBY37_RUBY") + "</w:rt><w:rubyBase>" + r("RBY37_BASE")
				+ "</w:rubyBase></w:ruby></w:r>"));
		always("RBY37_BASE", "");
		never("RBY37_RUBY", "the annotation: base text only (#16)");

		begin(38, "document properties and a custom XML part");
		body.append(pt("The core properties hold a title and a subject; the custom XML part case 23 binds to holds "
				+ "one more value."));
		gated("PRP38_TITLE", "metadata: true", "docProps/core.xml (#22)");
		gated("PRP38_SUBJECT", "metadata: true", "docProps/core.xml (#22)");
		never("CXP38_DATA", "customXml/item1.xml (#22)");

		ExtractionPhase0Cases.add(this, pkg);

		body.append("<w:sectPr><w:headerReference w:type='default' r:id='" + rel(mdp, hSection2) + "'/>" + PAGE + "</w:sectPr>");

		raw.put(mdp.getPartName().getName(), "<w:document" + NS + "><w:body>" + body + "</w:body></w:document>");
		raw.put(hDefault.getPartName().getName(), "<w:hdr" + NS + ">" + hdrDefault + "</w:hdr>");
		raw.put(footnotes.getPartName().getName(), "<w:footnotes" + NS + ">" + separators("footnote") + fns + "</w:footnotes>");
		raw.put(endnotes.getPartName().getName(), "<w:endnotes" + NS + ">" + separators("endnote") + ens + "</w:endnotes>");
		raw.put(comments.getPartName().getName(), "<w:comments" + NS + ">" + cmts + "</w:comments>");
		raw.put(commentsEx.getPartName().getName(), "<w15:commentsEx xmlns:mc='http://schemas.openxmlformats.org/markup-compatibility/2006'"
				+ " xmlns:w15='http://schemas.microsoft.com/office/word/2012/wordml' mc:Ignorable='w15'>"
				+ "<w15:commentEx w15:paraId='10000001' w15:done='0'/>"
				+ "<w15:commentEx w15:paraId='10000002' w15:paraIdParent='10000001' w15:done='0'/>"
				+ "<w15:commentEx w15:paraId='10000003' w15:done='1'/></w15:commentsEx>");
		raw.put(numbering.getPartName().getName(), numberingXml());
		raw.put(pkg.getMainDocumentPart().getStyleDefinitionsPart().getPartName().getName(), stylesXml());
		raw.put(settings.getPartName().getName(), settingsXml());

		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		Docx4J.save(pkg, saved, Docx4J.FLAG_NONE);
		return rewrite(saved.toByteArray());
	}

	static final String PAGE = "<w:pgSz w:w='11906' w:h='16838'/><w:pgMar w:top='1440' w:right='1440' w:bottom='1440' "
			+ "w:left='1440' w:header='708' w:footer='708' w:gutter='0'/><w:cols w:space='708'/>";

	HeaderPart header(MainDocumentPart mdp, String name) throws Exception {
		HeaderPart h = new HeaderPart(new PartName(name));
		h.setJaxbElement(new Hdr());
		mdp.addTargetPart(h);
		return h;
	}

	static String rel(MainDocumentPart mdp, Part part) {
		return mdp.getRelationshipsPart().getRel(part.getPartName()).getId();
	}

	void customXml(MainDocumentPart mdp) throws Exception {
		CustomXmlDataStoragePart part = new CustomXmlDataStoragePart();
		CustomXmlDataStorageImpl data = new CustomXmlDataStorageImpl();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		data.setDocument(dbf.newDocumentBuilder().parse(new ByteArrayInputStream(("<probe xmlns=\"" + PROBE_NS + "\">"
				+ "<value>BND23_XML_VALUE</value><data>CXP38_DATA</data></probe>").getBytes(StandardCharsets.UTF_8))));
		part.setData(data);
		mdp.addTargetPart(part, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		CustomXmlDataStoragePropertiesPart props = new CustomXmlDataStoragePropertiesPart();
		DatastoreItem item = new org.docx4j.customXmlProperties.ObjectFactory().createDatastoreItem();
		item.setItemID(STORE_ITEM);
		props.setJaxbElement(item);
		part.addTargetPart(props);
	}

	static void properties(WordprocessingMLPackage pkg) throws Exception {
		DocPropsCorePart core = pkg.getDocPropsCorePart();
		if (core == null) {
			pkg.addDocPropsCorePart();
			core = pkg.getDocPropsCorePart();
		}
		org.docx4j.docProps.core.dc.elements.ObjectFactory dc = new org.docx4j.docProps.core.dc.elements.ObjectFactory();
		org.docx4j.docProps.core.dc.elements.SimpleLiteral title = dc.createSimpleLiteral();
		title.getContent().add("PRP38_TITLE");
		core.getJaxbElement().setTitle(dc.createTitle(title));
		org.docx4j.docProps.core.dc.elements.SimpleLiteral subject = dc.createSimpleLiteral();
		subject.getContent().add("PRP38_SUBJECT");
		core.getJaxbElement().setSubject(subject);
	}

	/** The chart as a run, written as Word writes one; docx4j makes the chart and its parts */
	String chart(WordprocessingMLPackage pkg) throws Exception {
		ChartDefinition def = ChartDefinition.of(ChartType.COLUMN_CLUSTERED).title("CHT34_TITLE")
				.categories("CHT34_CAT_A", "CHT34_CAT_B").series("CHT34_SERIES_ONE", 3, 5).series("CHT34_SERIES_TWO", 4, 2)
				.valueAxis(a -> a.title("CHT34_AXIS_TITLE"));
		R run = new ChartCreator(new Random(1)).inDocument(pkg, def, 3657600L, 2286000L);
		Matcher m = Pattern.compile("<c:chart [^>]*r:id=\"([^\"]+)\"").matcher(XmlUtils.marshaltoString(run, true, false));
		if (!m.find()) throw new IllegalStateException("no c:chart in ChartCreator's run");
		int id = docPrId++;
		return "<w:r><w:rPr><w:noProof/></w:rPr><w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'>"
				+ "<wp:extent cx='3657600' cy='2286000'/><wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id
				+ "' name='Chart " + id + "'/><wp:cNvGraphicFramePr/><a:graphic><a:graphicData "
				+ "uri='http://schemas.openxmlformats.org/drawingml/2006/chart'><c:chart "
				+ "xmlns:c='http://schemas.openxmlformats.org/drawingml/2006/chart' r:id='" + m.group(1) + "'/></a:graphicData>"
				+ "</a:graphic></wp:inline></w:drawing></w:r>";
	}

	/**
	 * SmartArt with the Hier2Level layout: a root and two children, the points stored in the order
	 * CHILD_TWO, ROOT, CHILD_ONE and the connections ROOT, CHILD_TWO, CHILD_ONE, while the reading
	 * order (parOf, by srcOrd) is ROOT, CHILD_ONE, CHILD_TWO. No drawing part: Word lays it out.
	 */
	String smartArt(MainDocumentPart mdp, File glox) throws Exception {
		CTDiagramDefinition definition = GloxPackage.load(glox).getDiagramLayoutPart().getJaxbElement();
		definition.setUniqueId(LAYOUT_ID);
		DiagramLayoutPart layout = new DiagramLayoutPart();
		layout.setJaxbElement(definition);
		DiagramColorsPart colors = new DiagramColorsPart();
		colors.unmarshal("colorsDef-accent1_2.xml");
		DiagramStylePart style = new DiagramStylePart();
		style.unmarshal("quickStyle-simple1.xml");
		String doc = "{5A1B0000-0000-4000-8000-000000000000}";
		String root = "{5A1B0000-0000-4000-8000-000000000001}";
		String one = "{5A1B0000-0000-4000-8000-000000000002}";
		String two = "{5A1B0000-0000-4000-8000-000000000003}";
		StringBuilder pts = new StringBuilder();
		pts.append("<dgm:pt modelId='" + doc + "' type='doc'><dgm:prSet loTypeId='" + LAYOUT_ID + "' loCatId='hierarchy' qsTypeId='"
				+ style.getJaxbElement().getUniqueId() + "' qsCatId='simple' csTypeId='" + colors.getJaxbElement().getUniqueId()
				+ "' csCatId='accent1'/><dgm:spPr/><dgm:t><a:bodyPr/><a:lstStyle/><a:p><a:endParaRPr lang='en-US'/></a:p></dgm:t></dgm:pt>");
		pts.append(node(two, "SMA35_CHILD_TWO"));
		pts.append(node(root, "SMA35_ROOT"));
		pts.append(node(one, "SMA35_CHILD_ONE"));
		StringBuilder cxns = new StringBuilder();
		String[][] links = { { doc, root, "0", "1" }, { root, two, "1", "3" }, { root, one, "0", "2" } };
		for (String[] l : links) {
			String cxn = "{5A1B0000-0000-4000-8000-0000000001" + l[3] + "0}";
			String par = "{5A1B0000-0000-4000-8000-0000000002" + l[3] + "0}";
			String sib = "{5A1B0000-0000-4000-8000-0000000003" + l[3] + "0}";
			pts.append(trans(par, "parTrans", cxn)).append(trans(sib, "sibTrans", cxn));
			cxns.append("<dgm:cxn modelId='" + cxn + "' srcId='" + l[0] + "' destId='" + l[1] + "' srcOrd='" + l[2]
					+ "' destOrd='0' parTransId='" + par + "' sibTransId='" + sib + "'/>");
		}
		String dataXml = ("<dgm:dataModel xmlns:dgm='http://schemas.openxmlformats.org/drawingml/2006/diagram'"
				+ " xmlns:a='http://schemas.openxmlformats.org/drawingml/2006/main'><dgm:ptLst>" + pts + "</dgm:ptLst><dgm:cxnLst>"
				+ cxns + "</dgm:cxnLst><dgm:bg/><dgm:whole/></dgm:dataModel>").replace('\'', '"');
		DiagramDataPart data = new DiagramDataPart();
		data.unmarshal(new ByteArrayInputStream(dataXml.getBytes(StandardCharsets.UTF_8)));
		String dm = mdp.addTargetPart(data).getId();
		String lo = mdp.addTargetPart(layout).getId();
		String qs = mdp.addTargetPart(style).getId();
		String cs = mdp.addTargetPart(colors).getId();
		int id = docPrId++;
		return "<w:r><w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='4572000' cy='2286000'/>"
				+ "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='Diagram " + id + "'/>"
				+ "<wp:cNvGraphicFramePr/><a:graphic><a:graphicData uri='http://schemas.openxmlformats.org/drawingml/2006/diagram'>"
				+ "<dgm:relIds xmlns:dgm='http://schemas.openxmlformats.org/drawingml/2006/diagram' r:dm='" + dm + "' r:lo='" + lo
				+ "' r:qs='" + qs + "' r:cs='" + cs + "'/></a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
	}

	static String node(String id, String text) {
		return "<dgm:pt modelId='" + id + "'><dgm:prSet/><dgm:spPr/><dgm:t><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang='en-US'/>"
				+ "<a:t>" + text + "</a:t></a:r></a:p></dgm:t></dgm:pt>";
	}

	static String trans(String id, String type, String cxn) {
		return "<dgm:pt modelId='" + id + "' type='" + type + "' cxnId='" + cxn + "'><dgm:prSet/><dgm:spPr/><dgm:t><a:bodyPr/>"
				+ "<a:lstStyle/><a:p><a:endParaRPr lang='en-US'/></a:p></dgm:t></dgm:pt>";
	}

	// ------------------------------------------------------------------ XML pieces

	void begin(int n, String title) {
		current = new Case(n, title);
		cases.add(current);
		body.append("<w:p><w:pPr><w:pStyle w:val='Heading2'/></w:pPr>" + r("Case " + n + ": " + title) + "</w:p>");
	}

	static String r(String text) {
		return "<w:r><w:t xml:space='preserve'>" + text + "</w:t></w:r>";
	}

	static String r(String rPr, String text) {
		return "<w:r><w:rPr>" + rPr + "</w:rPr><w:t xml:space='preserve'>" + text + "</w:t></w:r>";
	}

	static String p(String content) {
		return "<w:p>" + content + "</w:p>";
	}

	static String pt(String text) {
		return p(r(text));
	}

	String rev() {
		return "w:id='" + (revId++) + "' w:author='Probe' w:date='" + DATE + "'";
	}

	String ins(String content) {
		return "<w:ins " + rev() + ">" + content + "</w:ins>";
	}

	String delRun(String text) {
		return "<w:del " + rev() + "><w:r><w:delText xml:space='preserve'>" + text + "</w:delText></w:r></w:del>";
	}

	static String field(String instr, String result, String resultRPr) {
		return "<w:r><w:fldChar w:fldCharType='begin'/></w:r>"
				+ "<w:r><w:instrText xml:space='preserve'>" + instr + "</w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType='separate'/></w:r>"
				+ (resultRPr.isEmpty() ? r(result) : r(resultRPr, result))
				+ "<w:r><w:fldChar w:fldCharType='end'/></w:r>";
	}

	static String ac(String declarations, String branches) {
		return "<mc:AlternateContent" + declarations + ">" + branches + "</mc:AlternateContent>";
	}

	static String choice(String requires, String content) {
		return "<mc:Choice Requires='" + requires + "'>" + content + "</mc:Choice>";
	}

	static String fallback(String content) {
		return "<mc:Fallback>" + content + "</mc:Fallback>";
	}

	/** An inline DrawingML text box, its wps elements in the prefix given */
	String wpsInline(String x, String paragraphs) {
		int id = docPrId++;
		return "<w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='2743200' cy='457200'/>"
				+ "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='Text Box " + id + "'/>"
				+ "<wp:cNvGraphicFramePr/><a:graphic><a:graphicData uri='" + WPS_URI + "'>" + wsp(x, paragraphs, 2743200)
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing>";
	}

	/** A floating DrawingML text box, to the right of the column, text wrapping around it */
	String wpsAnchor(String paragraphs) {
		int id = docPrId++;
		return "<w:drawing><wp:anchor distT='45720' distB='45720' distL='114300' distR='114300' simplePos='0' relativeHeight='"
				+ (251658240 + id) + "' behindDoc='0' locked='0' layoutInCell='1' allowOverlap='1'><wp:simplePos x='0' y='0'/>"
				+ "<wp:positionH relativeFrom='column'><wp:posOffset>3429000</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom='paragraph'><wp:posOffset>0</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx='2286000' cy='457200'/><wp:effectExtent l='0' t='0' r='0' b='0'/><wp:wrapSquare wrapText='bothSides'/>"
				+ "<wp:docPr id='" + id + "' name='Text Box " + id + "'/><wp:cNvGraphicFramePr/>"
				+ "<a:graphic><a:graphicData uri='" + WPS_URI + "'>" + wsp("wps", paragraphs, 2286000)
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing>";
	}

	static String wsp(String x, String paragraphs, long cx) {
		return "<" + x + ":wsp><" + x + ":cNvSpPr txBox='1'/><" + x + ":spPr><a:xfrm><a:off x='0' y='0'/><a:ext cx='" + cx
				+ "' cy='457200'/></a:xfrm><a:prstGeom prst='rect'><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val='FFFFFF'/>"
				+ "</a:solidFill><a:ln w='6350'><a:solidFill><a:srgbClr val='000000'/></a:solidFill></a:ln></" + x + ":spPr>"
				+ "<" + x + ":txbx><w:txbxContent>" + paragraphs + "</w:txbxContent></" + x + ":txbx>"
				+ "<" + x + ":bodyPr rot='0' vert='horz' wrap='square' lIns='91440' tIns='45720' rIns='91440' bIns='45720' "
				+ "anchor='t' anchorCtr='0'><a:noAutofit/></" + x + ":bodyPr></" + x + ":wsp>";
	}

	static final String SHAPETYPE_TEXTBOX = "<v:shapetype id='_x0000_t202' coordsize='21600,21600' o:spt='202' "
			+ "path='m,l,21600r21600,l21600,xe'><v:stroke joinstyle='miter'/><v:path gradientshapeok='t' o:connecttype='rect'/>"
			+ "</v:shapetype>";

	/** An inline VML text box */
	String vmlBox(String paragraphs) {
		int id = shapeId++;
		return "<w:pict>" + SHAPETYPE_TEXTBOX + "<v:shape id='TextBox" + id + "' o:spid='_x0000_s" + id + "' type='#_x0000_t202' "
				+ "style='width:216pt;height:36pt' strokeweight='.5pt'><v:textbox><w:txbxContent>" + paragraphs
				+ "</w:txbxContent></v:textbox></v:shape></w:pict>";
	}

	/** Legacy WordArt: a VML plain-text shape (spt 136) whose text is v:textpath/@string */
	String wordArt(String text) {
		int id = shapeId++;
		return "<w:pict><v:shapetype id='_x0000_t136' coordsize='21600,21600' o:spt='136' adj='10800' "
				+ "path='m@7,l@8,m@5,21600l@6,21600e'><v:formulas><v:f eqn='sum #0 0 10800'/><v:f eqn='prod #0 2 1'/>"
				+ "<v:f eqn='sum 21600 0 @1'/><v:f eqn='sum 0 0 @2'/><v:f eqn='sum 21600 0 @3'/><v:f eqn='if @0 @3 0'/>"
				+ "<v:f eqn='if @0 21600 @1'/><v:f eqn='if @0 0 @2'/><v:f eqn='if @0 @4 21600'/><v:f eqn='mid @5 @6'/>"
				+ "<v:f eqn='mid @8 @5'/><v:f eqn='mid @7 @8'/><v:f eqn='mid @6 @7'/><v:f eqn='sum @6 0 @5'/></v:formulas>"
				+ "<v:path textpathok='t' o:connecttype='custom' o:connectlocs='@9,0;@10,10800;@11,21600;@12,10800' "
				+ "o:connectangles='270,180,90,0'/><v:textpath on='t' fitshape='t'/><v:handles><v:h position='#0,bottomRight' "
				+ "xrange='6629,14971'/></v:handles><o:lock v:ext='edit' text='t' shapetype='t'/></v:shapetype>"
				+ "<v:shape id='WordArt" + id + "' o:spid='_x0000_s" + id + "' type='#_x0000_t136' style='width:216pt;height:24pt' "
				+ "fillcolor='black' stroked='f'><v:textpath style='font-family:&quot;Arial Black&quot;;v-text-kern:t' trim='t' "
				+ "fitpath='t' string='" + text + "'/></v:shape></w:pict>";
	}

	/** An inline picture; svgId, where given, is an SVG the blip's extLst names */
	String picture(String pngId, String svgId, String docPrAttributes, String docPrChildren) {
		int id = docPrId++;
		String blip = svgId == null ? "<a:blip r:embed='" + pngId + "'/>"
				: "<a:blip r:embed='" + pngId + "'><a:extLst><a:ext uri='{96DAC541-7B7A-43D3-8B79-37D633B846F1}'>"
						+ "<asvg:svgBlip xmlns:asvg='http://schemas.microsoft.com/office/drawing/2016/SVG/main' r:embed='" + svgId
						+ "'/></a:ext></a:extLst></a:blip>";
		return "<w:r><w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='914400' cy='457200'/>"
				+ "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='Picture " + id + "'" + docPrAttributes
				+ (docPrChildren.isEmpty() ? "/>" : ">" + docPrChildren + "</wp:docPr>")
				+ "<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect='1'/></wp:cNvGraphicFramePr>"
				+ "<a:graphic><a:graphicData uri='http://schemas.openxmlformats.org/drawingml/2006/picture'><pic:pic><pic:nvPicPr>"
				+ "<pic:cNvPr id='0' name='Picture " + id + "'/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill>" + blip
				+ "<a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x='0' y='0'/>"
				+ "<a:ext cx='914400' cy='457200'/></a:xfrm><a:prstGeom prst='rect'><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
	}

	static String sdtRun(String sdtPr, String content) {
		return "<w:sdt><w:sdtPr>" + sdtPr + "</w:sdtPr><w:sdtContent>" + content + "</w:sdtContent></w:sdt>";
	}

	static String tblPr() {
		return "<w:tblPr><w:tblStyle w:val='TableGrid'/><w:tblW w:w='0' w:type='auto'/><w:tblLook w:val='04A0' "
				+ "w:firstRow='1' w:lastRow='0' w:firstColumn='1' w:lastColumn='0' w:noHBand='0' w:noVBand='1'/></w:tblPr>";
	}

	static String tc(int width, String tcPr, String content) {
		return "<w:tc><w:tcPr><w:tcW w:w='" + width + "' w:type='dxa'/>" + tcPr + "</w:tcPr>" + content + "</w:tc>";
	}

	static String noteRef(String kind, int id) {
		String style = kind.equals("footnote") ? "FootnoteReference" : "EndnoteReference";
		return "<w:r><w:rPr><w:rStyle w:val='" + style + "'/></w:rPr><w:" + kind + "Reference w:id='" + id + "'/></w:r>";
	}

	static String note(String kind, int id, String content) {
		String text = kind.equals("footnote") ? "FootnoteText" : "EndnoteText";
		String ref = kind.equals("footnote") ? "FootnoteReference" : "EndnoteReference";
		return "<w:" + kind + " w:id='" + id + "'><w:p><w:pPr><w:pStyle w:val='" + text + "'/></w:pPr><w:r><w:rPr><w:rStyle w:val='"
				+ ref + "'/></w:rPr><w:" + kind + "Ref/></w:r>" + r(" ") + content + "</w:p></w:" + kind + ">";
	}

	static String separators(String kind) {
		return "<w:" + kind + " w:type='separator' w:id='-1'><w:p><w:pPr><w:spacing w:after='0' w:line='240' w:lineRule='auto'/>"
				+ "</w:pPr><w:r><w:separator/></w:r></w:p></w:" + kind + "><w:" + kind + " w:type='continuationSeparator' w:id='0'>"
				+ "<w:p><w:pPr><w:spacing w:after='0' w:line='240' w:lineRule='auto'/></w:pPr><w:r><w:continuationSeparator/></w:r>"
				+ "</w:p></w:" + kind + ">";
	}

	static String commentRef(int id) {
		return "<w:r><w:rPr><w:rStyle w:val='CommentReference'/></w:rPr><w:commentReference w:id='" + id + "'/></w:r>";
	}

	static String comment(int id, String paraId, String text) {
		return "<w:comment w:id='" + id + "' w:author='Probe' w:date='" + DATE + "' w:initials='P'><w:p w14:paraId='" + paraId
				+ "' w14:textId='77777777'><w:pPr><w:pStyle w:val='CommentText'/></w:pPr><w:r><w:rPr><w:rStyle "
				+ "w:val='CommentReference'/></w:rPr><w:annotationRef/></w:r>" + r(text) + "</w:p></w:comment>";
	}

	static String numberingXml() {
		String ind = "<w:pPr><w:ind w:left='720' w:hanging='360'/></w:pPr>";
		return "<w:numbering" + NS + "><w:abstractNum w:abstractNumId='0'><w:multiLevelType w:val='hybridMultilevel'/>"
				+ "<w:lvl w:ilvl='0'><w:start w:val='1'/><w:numFmt w:val='decimal'/><w:lvlText w:val='%1.'/><w:lvlJc w:val='left'/>"
				+ ind + "</w:lvl></w:abstractNum><w:abstractNum w:abstractNumId='1'><w:multiLevelType w:val='hybridMultilevel'/>"
				+ "<w:lvl w:ilvl='0'><w:start w:val='1'/><w:numFmt w:val='bullet'/><w:lvlText w:val=''/><w:lvlJc w:val='left'/>"
				+ ind + "<w:rPr><w:rFonts w:ascii='Symbol' w:hAnsi='Symbol' w:hint='default'/></w:rPr></w:lvl></w:abstractNum>"
				+ "<w:num w:numId='1'><w:abstractNumId w:val='0'/></w:num><w:num w:numId='2'><w:abstractNumId w:val='1'/></w:num>"
				+ "</w:numbering>";
	}

	static String settingsXml() {
		String compat = "";
		for (String[] s : new String[][] { { "compatibilityMode", "15" }, { "overrideTableStyleFontSizeAndJustification", "1" },
				{ "enableOpenTypeFeatures", "1" }, { "doNotFlipMirrorIndents", "1" }, { "differentiateMultirowTableHeaders", "1" } }) {
			compat += "<w:compatSetting w:name='" + s[0] + "' w:uri='http://schemas.microsoft.com/office/word' w:val='" + s[1] + "'/>";
		}
		return "<w:settings" + NS + "><w:zoom w:percent='100'/><w:defaultTabStop w:val='720'/><w:evenAndOddHeaders/>"
				+ "<w:characterSpacingControl w:val='doNotCompress'/>"
				+ "<w:footnotePr><w:footnote w:id='-1'/><w:footnote w:id='0'/></w:footnotePr>"
				+ "<w:endnotePr><w:endnote w:id='-1'/><w:endnote w:id='0'/></w:endnotePr>"
				+ "<w:compat>" + compat + "</w:compat><w:themeFontLang w:val='en-US'/><w:decimalSymbol w:val='.'/>"
				+ "<w:listSeparator w:val=','/></w:settings>";
	}

	static String stylesXml() {
		String border = "w:val='single' w:sz='4' w:space='0' w:color='auto'";
		String small = "<w:pPr><w:spacing w:after='0' w:line='240' w:lineRule='auto'/></w:pPr><w:rPr><w:sz w:val='20'/>"
				+ "<w:szCs w:val='20'/></w:rPr>";
		return "<w:styles" + NS + "><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii='Calibri' w:eastAsia='Calibri' "
				+ "w:hAnsi='Calibri' w:cs='Times New Roman'/><w:sz w:val='22'/><w:szCs w:val='22'/><w:lang w:val='en-US' "
				+ "w:eastAsia='en-US' w:bidi='ar-SA'/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after='160' "
				+ "w:line='259' w:lineRule='auto'/></w:pPr></w:pPrDefault></w:docDefaults>"
				+ "<w:style w:type='paragraph' w:default='1' w:styleId='Normal'><w:name w:val='Normal'/><w:qFormat/></w:style>"
				+ "<w:style w:type='paragraph' w:styleId='Title'><w:name w:val='Title'/><w:basedOn w:val='Normal'/>"
				+ "<w:next w:val='Normal'/><w:qFormat/><w:rPr><w:sz w:val='48'/><w:szCs w:val='48'/></w:rPr></w:style>"
				+ "<w:style w:type='paragraph' w:styleId='Heading2'><w:name w:val='heading 2'/><w:basedOn w:val='Normal'/>"
				+ "<w:next w:val='Normal'/><w:qFormat/><w:pPr><w:keepNext/><w:spacing w:before='240' w:after='80'/>"
				+ "<w:outlineLvl w:val='1'/></w:pPr><w:rPr><w:b/><w:color w:val='2F5496'/><w:sz w:val='26'/><w:szCs w:val='26'/>"
				+ "</w:rPr></w:style>"
				+ "<w:style w:type='paragraph' w:styleId='ListParagraph'><w:name w:val='List Paragraph'/><w:basedOn w:val='Normal'/>"
				+ "<w:qFormat/><w:pPr><w:ind w:left='720'/><w:contextualSpacing/></w:pPr></w:style>"
				+ "<w:style w:type='paragraph' w:styleId='FootnoteText'><w:name w:val='footnote text'/><w:basedOn w:val='Normal'/>"
				+ small + "</w:style>"
				+ "<w:style w:type='paragraph' w:styleId='EndnoteText'><w:name w:val='endnote text'/><w:basedOn w:val='Normal'/>"
				+ small + "</w:style>"
				+ "<w:style w:type='paragraph' w:styleId='CommentText'><w:name w:val='annotation text'/><w:basedOn w:val='Normal'/>"
				+ small + "</w:style>"
				+ "<w:style w:type='character' w:default='1' w:styleId='DefaultParagraphFont'><w:name w:val='Default Paragraph Font'/>"
				+ "<w:uiPriority w:val='1'/><w:semiHidden/><w:unhideWhenUsed/></w:style>"
				+ "<w:style w:type='character' w:styleId='ProbeHidden'><w:name w:val='Probe Hidden'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:rPr><w:vanish/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='Hyperlink'><w:name w:val='Hyperlink'/><w:basedOn w:val='DefaultParagraphFont'/>"
				+ "<w:unhideWhenUsed/><w:rPr><w:color w:val='0563C1'/><w:u w:val='single'/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='PlaceholderText'><w:name w:val='Placeholder Text'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:semiHidden/><w:rPr><w:color w:val='808080'/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='FootnoteReference'><w:name w:val='footnote reference'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:rPr><w:vertAlign w:val='superscript'/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='EndnoteReference'><w:name w:val='endnote reference'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:rPr><w:vertAlign w:val='superscript'/></w:rPr></w:style>"
				+ "<w:style w:type='character' w:styleId='CommentReference'><w:name w:val='annotation reference'/>"
				+ "<w:basedOn w:val='DefaultParagraphFont'/><w:rPr><w:sz w:val='16'/><w:szCs w:val='16'/></w:rPr></w:style>"
				+ "<w:style w:type='table' w:default='1' w:styleId='TableNormal'><w:name w:val='Normal Table'/><w:uiPriority w:val='99'/>"
				+ "<w:semiHidden/><w:unhideWhenUsed/><w:tblPr><w:tblInd w:w='0' w:type='dxa'/><w:tblCellMar><w:top w:w='0' w:type='dxa'/>"
				+ "<w:left w:w='108' w:type='dxa'/><w:bottom w:w='0' w:type='dxa'/><w:right w:w='108' w:type='dxa'/></w:tblCellMar>"
				+ "</w:tblPr></w:style>"
				+ "<w:style w:type='table' w:styleId='TableGrid'><w:name w:val='Table Grid'/><w:basedOn w:val='TableNormal'/>"
				+ "<w:pPr><w:spacing w:after='0' w:line='240' w:lineRule='auto'/></w:pPr><w:tblPr><w:tblBorders><w:top " + border
				+ "/><w:left " + border + "/><w:bottom " + border + "/><w:right " + border + "/><w:insideH " + border
				+ "/><w:insideV " + border + "/></w:tblBorders></w:tblPr></w:style>"
				+ "<w:style w:type='numbering' w:default='1' w:styleId='NoList'><w:name w:val='No List'/><w:uiPriority w:val='99'/>"
				+ "<w:semiHidden/><w:unhideWhenUsed/></w:style></w:styles>";
	}

	// ------------------------------------------------------------------ writing the package

	/** docx4j's save, its story parts replaced by the XML written here, every entry dated 1980 */
	byte[] rewrite(byte[] saved) throws Exception {
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
				if (e.getName().endsWith(".xlsx")) bytes = redate(bytes);   // the chart's workbook
				put(zip, e.getName(), bytes);
				written.add(e.getName());
			}
		}
		for (String name : replacements.keySet()) {
			if (!written.contains(name)) throw new IllegalStateException("no part " + name + " in docx4j's save");
		}
		return out.toByteArray();
	}

	/** A zip with every entry dated 1980, so that a rebuild gives the same bytes */
	static byte[] redate(byte[] zipBytes) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zipBytes));
				ZipOutputStream zip = new ZipOutputStream(out)) {
			ZipEntry e;
			while ((e = in.getNextEntry()) != null) put(zip, e.getName(), readAll(in));
		}
		return out.toByteArray();
	}

	static void put(ZipOutputStream zip, String name, byte[] bytes) throws Exception {
		ZipEntry entry = new ZipEntry(name);
		entry.setTimeLocal(java.time.LocalDateTime.of(1980, 1, 1, 0, 0));
		zip.putNextEntry(entry);
		zip.write(bytes);
		zip.closeEntry();
	}

	static byte[] readAll(InputStream in) throws Exception {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
		return b.toByteArray();
	}

	/**
	 * Every token declared is in the package's XML, and every token-like string there is declared;
	 * prints how often each occurs in the file.
	 */
	void check(byte[] docx) throws Exception {
		Map<String, Integer> found = new TreeMap<String, Integer>();
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(docx))) {
			ZipEntry e;
			while ((e = in.getNextEntry()) != null) {
				if (!(e.getName().endsWith(".xml") || e.getName().endsWith(".rels") || e.getName().endsWith(".svg")
						|| e.getName().endsWith(".htm") || e.getName().endsWith(".html"))) continue;
				Matcher m = TOKEN.matcher(new String(readAll(in), StandardCharsets.UTF_8));
				while (m.find()) found.merge(m.group(), 1, Integer::sum);
			}
		}
		Set<String> declared = new LinkedHashSet<String>();
		for (Case c : cases) {
			for (Token t : c.tokens) {
				String name = t.name.replaceAll("[‑­].*", "");
				declared.add(name);
				if (!found.containsKey(name)) throw new IllegalStateException("token " + t.name + " is not in the file");
			}
		}
		for (Map.Entry<String, Integer> f : found.entrySet()) {
			if (!declared.contains(f.getKey())) System.out.println("undeclared: " + f.getKey() + " x" + f.getValue());
			else if (f.getValue() > 1) System.out.println("in the file " + f.getValue() + " times: " + f.getKey());
		}
	}

	// ------------------------------------------------------------------ expectations

	void token(String name, String accepted, String original, String markup, String option, String withOption, String note) {
		current.tokens.add(new Token(name, accepted, original, markup, option, withOption, note));
	}

	void always(String name, String note) {
		token(name, "1", "1", "1", "", "", note);
	}

	void never(String name, String note) {
		token(name, "0", "0", "0", "", "", note);
	}

	void inserted(String name, String note) {
		token(name, "1", "0", "ins", "", "", note);
	}

	void deleted(String name, String note) {
		token(name, "0", "1", "del", "", "", note);
	}

	/** Absent by default, present (once, in every view) with the option */
	void gated(String name, String option, String note) {
		token(name, "0", "0", "0", option, "1", note);
	}

	/** Text in a text box: there by default, after the anchoring paragraph (#5) */
	void box(String name, String note) {
		token(name, "1", "1", "1", "text_boxes: false", "0", note);
	}

	/** Text in a note: there by default, after the body (#7) */
	void noteText(String name, String note) {
		token(name, "1", "1", "1", "notes: false", "0", note);
	}

	int tokenCount() {
		int n = 0;
		for (Case c : cases) n += c.tokens.size();
		return n;
	}

	String tsv() {
		StringBuilder s = new StringBuilder("case\ttoken\taccepted\toriginal\tmarkup\toption\twith_option\tnote\tfixture\tstatements\n");
		for (Case c : cases) {
			for (Token t : c.tokens) {
				s.append(c.n).append('\t').append(t.name).append('\t').append(t.accepted).append('\t').append(t.original)
						.append('\t').append(t.markup).append('\t').append(t.option).append('\t').append(t.withOption)
						.append('\t').append(t.note).append('\t').append(ExtractionPhase0Cases.fixture(c.n))
                        .append('\t').append(ExtractionPhase0Cases.statements(c.n)).append('\n');
			}
		}
		return s.toString();
	}

	static String cell(String v) {
		switch (v) {
		case "0": return "–";
		case "1": return "yes";
		case "2": return "twice";
		case "ins": return "`{++…++}`";
		case "del": return "`{--…--}`";
		case "cmt": return "`{>>…<<}`";
		case "del+ins": return "`{--…--}`, `{++…++}`";
		default: return v;
		}
	}

    String statements() {
        StringBuilder out = new StringBuilder("case\tfixture\tstatements\n");
        for (Case c : cases) out.append(c.n).append('\t').append(ExtractionPhase0Cases.fixture(c.n))
                .append('\t').append(ExtractionPhase0Cases.statements(c.n)).append('\n');
        return out.toString();
    }

	String readme() {
		StringBuilder s = new StringBuilder();
		s.append("# Text extraction probe\n\n");
		s.append("`extraction-probe.docx` is one document with a marker token per case, for testing what a reader extracts\n"
				+ "from a docx: which `mc:AlternateContent` branch it takes, which revision view it reads, and what it drops\n"
				+ "or duplicates. It is the first fixture of the portfolio's text model (docx4j-portfolio\n"
				+ "`docs/text_extraction_behaviour_model.md`, section 8); the expected results below follow that paper's\n"
				+ "sections 3 to 5, whose item numbers (#n) the notes cite.\n\n");
		s.append("Every marker is upper-case words and digits joined by underscores, and occurs once in the file unless its\n"
				+ "case is about a duplicate (TC10_MOVED, FLD19_RESULT). Each case starts with a heading, \"Case n: ...\".\n\n");
		s.append("## Files\n\n"
				+ "- `extraction-probe.docx`: the probe.\n"
				+ "- `expected-tokens.tsv`: the tables below, one row a token, for a set comparison by script.\n"
				+ "- `README.md`: this file.\n\n"
				+ "All three are written by `org.docx4j.textextraction.ExtractionProbeBuilder` (docx4j-core-tests test\n"
				+ "sources). From the repository root, with docx4j installed (`mvn install -DskipTests`):\n\n"
				+ "```\n"
				+ "mvn -q -pl docx4j-core-tests test-compile\n"
				+ "mvn -q -pl docx4j-core-tests dependency:build-classpath -Dmdep.outputFile=cp.txt\n"
				+ "java -cp docx4j-core-tests/target/test-classes:$(cat docx4j-core-tests/cp.txt) \\\n"
				+ "  org.docx4j.textextraction.ExtractionProbeBuilder docx4j-core-tests/src/test/resources/text-extraction/phase0\n"
				+ "```\n\n"
				+ "The story parts are written by the builder as XML text: JAXB would drop the namespace declarations\n"
				+ "the `mc` cases' `Requires` attributes name. The SmartArt layout is `Hier2Level.glox` from\n"
				+ "docx4j-samples-glox4j, by Scott Sherman, under the Creative Commons Attribution 2.5 licence.\n\n");
		s.append("## Using it\n\n"
				+ "With a library: extract the text, find every match of `[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+` (the two hyphen\n"
				+ "tokens of case 30 hold U+2011 and U+00AD), and compare the counts with the table, view by view. Tokens\n"
				+ "are followed by a space wherever a reader might otherwise glue them to what comes next, except in case\n"
				+ "30: there a match such as `BRK30_BEFOREBRK30_AFTER` is the finding, a break or tab the reader dropped.\n\n"
				+ "With a hosted assistant (ChatGPT, Claude.ai, Gemini, NotebookLM, Copilot): upload the file in a fresh\n"
				+ "conversation and ask \"List every marker token in this document exactly as your document processing\n"
				+ "presents it; do not inspect the XML with code\". Record the date and the product version.\n\n");
		s.append("## Columns\n\n"
				+ "- **accepted**, **original**, **markup**: the token in that view's text, with the default scope: \"yes\"\n"
				+ "  once, \"–\" not at all, \"twice\"; in markup, `{++…++}` an insertion, `{--…--}` a deletion,\n"
				+ "  `{>>…<<}` a comment.\n"
				+ "- **option**: the option that changes the token's result, and **with option** that result (in every\n"
				+ "  view, except a comment's, which is markup's). \"markdown\" marks text only Markdown carries.\n\n");
		s.append("## Word\n\n"
				+ "Word 365 opened the original 38-case probe without a repair prompt (checked 17 October 2026).\n"
				+ "`extraction-probe.word-resave.docx` is its save of the probe, unedited. The tables below are for\n"
				+ "the probe. What Word changed on the way, read from the re-save:\n\n"
				+ "- Every wps text box is now an `mc:AlternateContent` whose VML Fallback Word wrote from the Choice.\n"
				+ "  The authored Fallbacks are gone (FALLBACK_BETA_456, FALLBACK_EPSILON, MCE6_FALLBACK,\n"
				+ "  MCE8_OUTER_FALLBACK), and each box's tokens are in the file twice, so a reader that takes both\n"
				+ "  branches shows them twice. The boxes that had no `mc` (cases 17 and 25) got one too. The cause is\n"
				+ "  case 36: with a `w:altChunk` in the document, Word regenerates every Fallback on save; without one\n"
				+ "  it keeps an unedited box's authored Fallback (CR-021 section 8.5).\n"
				+ "- Every other `mc:AlternateContent` is resolved to its selected branch alone: UNSUPPORTED_GAMMA,\n"
				+ "  CHOICE1_UNKNOWN_ZETA, MCE5_SECOND_SUPPORTED, MCE5_FALLBACK, MCE7_TWO_REQ_CHOICE and\n"
				+ "  MCE8_INNER_FALLBACK are gone, and case 7's Fallback box is a plain `w:pict`.\n"
				+ "- The HYPERLINK field is a `w:hyperlink`; FLD21_FIELD_URL_MARK is in the relationships.\n"
				+ "- The bound control shows BND23_XML_VALUE; BND23_SURFACE_STALE is gone, so `bindings: surface` gives\n"
				+ "  the XML value.\n"
				+ "- The altChunk is expanded: ALT36_CHUNK_TEXT is an ordinary paragraph, there in every view.\n"
				+ "- The header no section uses (HDR26_ORPHAN) is dropped, and the headers are renumbered.\n"
				+ "- The SmartArt has a drawing part, so the three SMA35 tokens are in the file twice (data and drawing).\n"
				+ "- Each `m:t` is split where letters meet digits (`MTH`, `32_`, `A`): the text is the same, but a\n"
				+ "  reader that looks for tokens run by run loses MTH32_A and MTH32_B.\n\n");
		s.append("## Rulings the probe prompted\n\n"
				+ "Three cases had no rule in the paper when the probe was built; it has since ruled (17 October 2026):\n\n"
				+ "- Case 33: a picture marked decorative (`adec:decorative`) gives no alt text under any option, even\n"
				+ "  with a `descr` (D22).\n"
				+ "- Case 28: a resolved comment (`w15:done`) is a comment like any other; JSON adds `resolved: true`.\n"
				+ "- Case 21: Markdown gives a hyperlink's target as `[text](target)` for both forms, the HYPERLINK\n"
				+ "  field's too, whatever `field_codes` says: the instruction supplies the address, not text.\n\n");
		s.append("## Phase 0 extension\n\n"
                + "Cases 39–49 extend the main probe; cases 50–52 are in `extraction-probe-external.docm`.\n"
                + "Jason confirmed the r2 main DOCX opens without repair (18 October 2026), with field updates declined.\n"
                + "The external DOCM check also passed after declining its SQL prompt. The Word findings above describe the original 38-case\n"
                + "fixture, retained in the parent directory with its re-save.\n"
                + "`case-statements.tsv` mirrors WD 0.3 Appendix A; `expected-findings.tsv` separates warnings,\n"
                + "metadata and future inspector findings from text counts. Inspector requirements are pending a later CR.\n"
                + "The default test environment has no XHTML importer. The MHT row's capability scenario supplies one.\n"
                + "All external targets are deliberately missing local files. Nothing is fetched by the suite.\n"
                + "The VBA project has attribute-only modules and a comment marker; it contains no executable procedures.\n"
                + "See `../support/README.md` for its provenance and rebuild command.\n\n"
                + "Golden conventions and baseline commands are in `../GOLDENS.md`.\n\n");
        s.append("## Cases\n\n");
		for (Case c : cases) {
			s.append("### Case ").append(c.n).append(": ").append(c.title).append("\n\n");
			s.append("| Token | accepted | original | markup | option | with option | note | statements |\n");
			s.append("|---|---|---|---|---|---|---|---|\n");
			for (Token t : c.tokens) {
				s.append("| `").append(t.name).append("` | ").append(cell(t.accepted)).append(" | ").append(cell(t.original))
						.append(" | ").append(cell(t.markup)).append(" | ").append(t.option.isEmpty() ? "" : "`" + t.option + "`")
						.append(" | ").append(t.withOption.isEmpty() ? "" : cell(t.withOption)).append(" | ")
						.append(t.note.replace("|", "\\|")).append(" | ").append(ExtractionPhase0Cases.statements(c.n)).append(" |\n");
			}
			s.append('\n');
		}
		return s.toString();
	}

	static byte[] png(int w, int h, int rgb) throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) img.setRGB(x, y, rgb);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", bytes);
		return bytes.toByteArray();
	}
}
