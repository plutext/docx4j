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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.P;
import org.docx4j.wml.RunDel;
import org.junit.Test;

/**
 * CR-068 phase 1: {@link ManualRedline} on the phase 0 probe ({@code revisions/cr068}, built by
 * {@link ManualRedlineProbeBuilder}), held to Word 365's Accept All and Reject All of the conversion
 * phase 0 predicted (CR-068 section 8.4), and on small cases for what the probe does not reach.
 */
public class ManualRedlineTest {

	static final String DIR = "revisions/cr068/";
	static final Instant AT = Instant.parse("2026-10-31T00:00:00Z");
	static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	static ManualRedlineOptions options() {
		return new ManualRedlineOptions().date(AT);
	}

	/** The probe's base, read in the VM's zone (the goldens' dates are its wall clock). */
	static WordprocessingMLPackage base() throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load(DIR + "manual-redline.docx");
		pkg.setDateTimeZone(SYDNEY);
		return pkg;
	}

	static WordprocessingMLPackage copy(WordprocessingMLPackage pkg) throws Exception {
		return TrackedChangesWordTest.roundTrip(pkg);
	}

	static String text(WordprocessingMLPackage pkg) {
		return PlainText.of(Texts.of(pkg, TextOptions.of(TextView.ACCEPTED).notes(true)
				.include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS)));
	}

	static String accepted(WordprocessingMLPackage pkg) throws Exception {
		WordprocessingMLPackage c = copy(pkg);
		TrackedChanges.acceptAll(c);
		return text(c);
	}

	static String rejected(WordprocessingMLPackage pkg) throws Exception {
		WordprocessingMLPackage c = copy(pkg);
		TrackedChanges.rejectAll(c);
		return text(c);
	}

	// ---------------------------------------------------------------- the probe against Word

	/**
	 * Without the separating space (D13 off), the conversion is phase 0's D9 prediction, which Word
	 * resolved: docx4j's Accept All and Reject All of it describe as Word's do (CR-043's structural
	 * description: text, formatting, marks, tables, notes, text boxes).
	 */
	@Test
	public void withoutTheSeparatingSpaceAsWordResolvedThePrediction() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options().separatorSpace(false));
		pkg = copy(pkg);
		for (boolean accept : new boolean[] { true, false }) {
			WordprocessingMLPackage c = copy(pkg);
			if (accept) TrackedChanges.acceptAll(c);
			else TrackedChanges.rejectAll(c);
			String ours = asD9(DocumentDescription.of(copy(c)));
			String word = DocumentDescription.of(TrackedChangesWordTest.load(DIR + (accept ? "word-d9-accept-all.docx" : "word-d9-reject-all.docx")));
			assertEquals(accept ? "accept all" : "reject all", word, ours);
		}
	}

	/**
	 * The default conversion, as Word resolved it (CR-068 section 11.6): docx4j's conversion of the
	 * base, accepted and rejected by docx4j, describes as Word 365's Accept All and Reject All of
	 * {@code manual-redline-phase1.docx}, the same conversion saved.
	 */
	@Test
	public void asWordResolvedTheConversion() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options());
		pkg = copy(pkg);
		for (boolean accept : new boolean[] { true, false }) {
			WordprocessingMLPackage c = copy(pkg);
			if (accept) TrackedChanges.acceptAll(c);
			else TrackedChanges.rejectAll(c);
			String ours = DocumentDescription.of(copy(c));
			String word = DocumentDescription.of(TrackedChangesWordTest.load(DIR + (accept ? "word-phase1-accept-all.docx" : "word-phase1-reject-all.docx")));
			assertEquals(accept ? "accept all" : "reject all", word, ours);
		}
	}

	/**
	 * The probe's {@code manual-redline-phase1.docx}, which Word was given, is what the conversion
	 * writes: its story parts as docx4j reads them (the bytes depend on the marshal's formatting
	 * properties, which differ between the builder's classpath and the tests').
	 */
	@Test
	public void thePhase1FileIsTheConversion() throws Exception {
		byte[] base = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
				ManualRedlineTest.class.getClassLoader().getResource(DIR + "manual-redline.docx").toURI()));
		WordprocessingMLPackage built = WordprocessingMLPackage.load(new ByteArrayInputStream(ManualRedlineProbeBuilder.phase1(base)));
		WordprocessingMLPackage committed = TrackedChangesWordTest.load(DIR + "manual-redline-phase1.docx");
		for (String name : new String[] { "/word/document.xml", "/word/footnotes.xml", "/word/header1.xml", "/word/comments.xml" }) {
			assertEquals(name, partXml(committed, name), partXml(built, name));
		}
	}

	static String partXml(WordprocessingMLPackage pkg, String name) throws Exception {
		Object part = pkg.getParts().get(new org.docx4j.openpackaging.parts.PartName(name));
		return XmlUtils.marshaltoString(((org.docx4j.openpackaging.parts.JaxbXmlPart<?>) part).getJaxbElement(), true, false);
	}

	/** The default: as above, but each struck value takes the space that separated it (D13). */
	@Test
	public void theSeparatingSpace() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options());
		List<String> lines = Arrays.asList(accepted(pkg).split("\n"));
		for (String expected : new String[] { "M01 Rate: 1.335 per cent.", "M02 cell:\t1.335", "Widget\t$35.00",
				"M09 See example.com/new-terms for the terms.", "M10 text box: the price is $35.00.", "M13 Fee: EUR 450 a month.",
				"M14 kept words, end.", "M15 Before after.", "M17 Party: Acme Holdings Pty Ltd.",
				"[1] M10 footnote: the rate is 1.335.", "M10 header: Final version" }) {
			assertTrue(expected + " in\n" + lines, lines.contains(expected));
		}
		for (String line : lines) assertFalse("two spaces: " + line, line.contains("  "));
		// what is rejected is the original, the spaces given back: Word's Reject All of the prediction
		WordprocessingMLPackage c = copy(pkg);
		TrackedChanges.rejectAll(c);
		assertEquals(DocumentDescription.of(TrackedChangesWordTest.load(DIR + "word-d9-reject-all.docx")), asD9(DocumentDescription.of(copy(c))));
	}

	/** The base's title paragraph read as the D9 prediction's, the one paragraph in which the two files differ by construction. */
	private static String asD9(String description) {
		return description.replace(ManualRedlineProbeBuilder.Variant.BASE.label, ManualRedlineProbeBuilder.Variant.D9.label);
	}

	/** The three views of the converted document are its Accept All and Reject All (CR-068 section 3.4). */
	@Test
	public void theViewsFollow() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options());
		TextOptions o = TextOptions.of(TextView.ORIGINAL).notes(true).include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS);
		assertEquals(rejected(pkg), PlainText.of(Texts.of(pkg, o)));
		assertEquals(accepted(pkg), text(pkg));
		String markup = PlainText.of(Texts.of(pkg, TextOptions.of(TextView.MARKUP)));
		assertTrue(markup, markup.contains("M01 Rate: {--1.316 --}1.335 per cent."));
		assertTrue(markup, markup.contains("M14 {--first struck run, second struck run, --}kept words, {--third struck run --}end."));
	}

	@Test
	public void theReport() throws Exception {
		ManualRedlineReport r = ManualRedline.toTrackedChanges(base(), options());
		assertFalse(r.isPreview());
		assertEquals(18, r.getDeletions());
		assertEquals(0, r.getInsertions());
		assertEquals(17, r.getRuns());
		assertEquals("{strike=15, separator_space=7, paragraph_mark=1, double_strike=1}", r.getByKind().toString());
		assertEquals("{/word/document.xml=21, /word/footnotes.xml=2, /word/header1.xml=1}", r.getByPart().toString());
		assertEquals("{style_strike=1, in_tracked_change=1, field=2, comment=1}", r.getLeftByReason().toString());
		ManualRedlineReport.Change first = r.getChanges().get(0);
		assertEquals(ManualRedlineReport.Kind.STRIKE, first.getKind());
		assertEquals("body/1", first.getAddress());
		assertEquals("1.316", first.getText());
		assertEquals("Version 2", r.getLeft().get(2).getText());
		assertEquals("body/11/textbox:0/0", r.getChanges().get(10).getAddress());
		assertTrue(r.toJson(), r.toJson().startsWith("{\"preview\":false,\"deletions\":18,\"insertions\":0,\"runs\":17,"));
	}

	/** A second conversion changes nothing (CR-068 section 3.2). */
	@Test
	public void idempotent() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options());
		String once = xml(pkg);
		ManualRedlineReport again = ManualRedline.toTrackedChanges(pkg, options());
		assertEquals(0, again.getDeletions());
		assertTrue(again.getChanges().isEmpty());
		assertEquals(once, xml(pkg));
	}

	@Test
	public void previewChangesNothing() throws Exception {
		WordprocessingMLPackage pkg = base();
		String before = xml(pkg);
		ManualRedlineReport p = ManualRedline.preview(pkg, options());
		assertTrue(p.isPreview());
		assertEquals(before, xml(pkg));
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, options());
		assertEquals(r.toJson(), p.toJson().replace("\"preview\":true", "\"preview\":false"));
	}

	/** Fresh ids above every annotation id the document had (its comment 0, its insertion 100), none twice. */
	@Test
	public void ids() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options());
		List<BigInteger> ids = new ArrayList<BigInteger>();
		for (TrackedChange change : new Lister(pkg, false).list()) {
			for (TrackedChange piece : change.getPieces()) ids.add(new BigInteger(String.valueOf(idOf(piece))));
		}
		Set<BigInteger> unique = new HashSet<BigInteger>(ids);
		assertEquals(ids.toString(), ids.size(), unique.size());
		int above = 0;
		for (BigInteger id : ids) if (id.compareTo(BigInteger.valueOf(100)) > 0) above++;
		assertEquals(ids.size() - 1, above);   // all but CR-068 base's insertion, 100
	}

	private static Object idOf(TrackedChange change) throws Exception {
		Object target = change.target.value();
		return target instanceof CTTrackChange ? ((CTTrackChange) target).getId() : null;
	}

	/** A {@code w:del} only where the schema has one: in a paragraph, a hyperlink, an inline control's content, a smart tag. */
	@Test
	public void whereTheDeletionsStand() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options());
		List<Object> parents = new ArrayList<Object>();
		for (Object root : new Object[] { pkg.getMainDocumentPart().getJaxbElement(), pkg.getMainDocumentPart().getFootnotesPart().getJaxbElement() }) {
			Trees.walk(root, new Trees.Visitor() {
				@Override boolean enter(Object value, Object item) {
					if (value instanceof RunDel) parents.add(((RunDel) value).getParent());
					return true;
				}
			});
		}
		assertEquals(17, parents.size());   // 14 in the main story's text, the text box's twin, M09's space, the footnote's
		for (Object parent : parents) {
			assertTrue(String.valueOf(parent), parent instanceof P || parent instanceof P.Hyperlink
					|| parent instanceof CTSdtContentRun || parent instanceof CTSmartTagRun);
		}
	}

	// ---------------------------------------------------------------- dates and author

	@Test
	public void theDateAsWordWritesIt() throws Exception {
		WordprocessingMLPackage pkg = base();
		ManualRedline.toTrackedChanges(pkg, options().author("Legal review"));
		CTTrackChange del = firstDeletion(pkg);
		assertEquals("Legal review", del.getAuthor());
		assertEquals("2026-10-31T11:00:00Z", del.getDate().toXMLFormat());   // Sydney's wall clock, which Word shows
		assertEquals("2026-10-31T00:00:00Z", del.getDateUtc().toXMLFormat());

		WordprocessingMLPackage utc = base();
		utc.setDateTimeZone(ZoneId.of("UTC"));
		ManualRedline.toTrackedChanges(utc, options());
		assertEquals("2026-10-31T00:00:00Z", firstDeletion(utc).getDate().toXMLFormat());

		WordprocessingMLPackage none = base();
		ManualRedline.toTrackedChanges(none, new ManualRedlineOptions().date(null));
		assertNull(firstDeletion(none).getDate());
		assertNull(firstDeletion(none).getDateUtc());
		assertEquals(ManualRedlineOptions.DEFAULT_AUTHOR, firstDeletion(none).getAuthor());
	}

	private static CTTrackChange firstDeletion(WordprocessingMLPackage pkg) {
		CTTrackChange[] found = { null };
		Trees.walk(pkg.getMainDocumentPart().getJaxbElement(), new Trees.Visitor() {
			@Override boolean enter(Object value, Object item) {
				if (found[0] == null && value instanceof RunDel) found[0] = (RunDel) value;
				return found[0] == null;
			}
		});
		return found[0];
	}

	/** {@code w16du:dateUtc} needs its prefix ignorable: a document that had no {@code mc:Ignorable} gains one. */
	@Test
	public void w16duIgnorable() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(p("<w:r><w:t xml:space='preserve'>Rate: </w:t></w:r>"
				+ "<w:r><w:rPr><w:strike/></w:rPr><w:t>1.316</w:t></w:r><w:r><w:t xml:space='preserve'> 1.335</w:t></w:r>"));
		ManualRedline.toTrackedChanges(pkg, options());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		String document = entry(bos.toByteArray(), "word/document.xml");
		assertTrue(document, document.contains("xmlns:w16du=\"http://schemas.microsoft.com/office/word/2023/wordml/word16du\""));
		assertTrue(document, document.matches("(?s).*mc:Ignorable=\"[^\"]*\\bw16du\\b.*"));
		assertEquals("Rate: 1.335\n", accepted(WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray()))));
	}

	// ---------------------------------------------------------------- small cases

	private static P p(String content) throws Exception {
		return (P) XmlUtils.unmarshalString("<w:p xmlns:w='" + W + "'>" + content + "</w:p>");
	}

	private static String struck(String text) {
		return "<w:r><w:rPr><w:strike/></w:rPr><w:t xml:space='preserve'>" + text + "</w:t></w:r>";
	}

	private static String run(String text) {
		return "<w:r><w:t xml:space='preserve'>" + text + "</w:t></w:r>";
	}

	/** One paragraph converted, then accepted. */
	private static String acceptedOne(String content, ManualRedlineOptions o) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(p(content));
		ManualRedline.toTrackedChanges(pkg, o);
		return accepted(pkg);
	}

	@Test
	public void separatingSpaceCases() throws Exception {
		// the space before, where the struck word meets punctuation or ends the paragraph
		assertEquals("the old.\n", acceptedOne(run("the old ") + struck("term") + run("."), options()));
		assertEquals("ends with\n", acceptedOne(run("ends with ") + struck("this"), options()));
		// the paragraph's start
		assertEquals("new\n", acceptedOne(struck("old") + run(" new"), options()));
		// two struck words apart
		assertEquals("a d\n", acceptedOne(run("a ") + struck("b") + run(" ") + struck("c") + run(" d"), options()));
		// no space to take: digits meeting (dpp-forks C04), or the space inside the strike
		assertEquals("x1.335\n", acceptedOne(run("x") + struck("1.316") + run("1.335"), options()));
		assertEquals("a b\n", acceptedOne(run("a ") + struck("old ") + run("b"), options()));
		// off
		assertEquals("the old .\n", acceptedOne(run("the old ") + struck("term") + run("."), options().separatorSpace(false)));
		// a space in a field or an insertion is not taken
		assertEquals("a  b\n", acceptedOne(run("a ") + struck("old") + "<w:ins w:id='1' w:author='X'>" + run(" b") + "</w:ins>", options()));
	}

	@Test
	public void leftAlone() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		// a strike that is itself a tracked formatting change
		pkg.getMainDocumentPart().getContent().add(p("<w:r><w:rPr><w:strike/><w:rPrChange w:id='1' w:author='X'><w:rPr/></w:rPrChange></w:rPr>"
				+ "<w:t>formatted</w:t></w:r>"));
		// a struck run holding only a comment reference: nothing to delete
		pkg.getMainDocumentPart().getContent().add(p(run("kept") + "<w:r><w:rPr><w:strike/></w:rPr><w:commentReference w:id='0'/></w:r>"));
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, options());
		assertEquals(0, r.getDeletions());
		assertEquals("{in_tracked_change=1}", r.getLeftByReason().toString());
		assertEquals("formatted", r.getLeft().get(0).getText());
	}

	/** D9 as revisited: a wholly struck paragraph's struck mark is deleted only where Word would join it to a paragraph after it. */
	@Test
	public void paragraphMarks() throws Exception {
		String whole = "<w:pPr><w:rPr><w:strike/></w:rPr></w:pPr>" + struck("gone");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		List<Object> body = pkg.getMainDocumentPart().getContent();
		body.add(p(whole));                                            // joins the next
		body.add(p(run("next")));
		body.add(p(whole));                                            // before a table: left
		body.add(XmlUtils.unmarshalString("<w:tbl xmlns:w='" + W + "'><w:tblGrid><w:gridCol w:w='2000'/></w:tblGrid><w:tr><w:tc>"
				+ "<w:tcPr><w:tcW w:w='2000' w:type='dxa'/></w:tcPr><w:p>" + whole + "</w:p></w:tc></w:tr></w:tbl>"));   // a cell's last: left
		body.add(p("<w:pPr><w:rPr><w:strike/></w:rPr></w:pPr>" + struck("gone") + run("kept")));   // not whole: the mark stays, unreported
		body.add(p(run("end")));
		ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, options().separatorSpace(false));
		assertEquals("{strike=4, paragraph_mark=1}", r.getByKind().toString());
		assertEquals("{paragraph_mark=2}", r.getLeftByReason().toString());
		// joined; the paragraph before the table and the cell's, emptied; the struck word gone, its neighbour kept
		assertEquals("next\n\n\nkept\nend\n", accepted(pkg));
	}

	// ---------------------------------------------------------------- helpers

	private static String xml(WordprocessingMLPackage pkg) {
		return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
	}

	private static String entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
			ZipEntry e;
			while ((e = in.getNextEntry()) != null) {
				if (e.getName().equals(name)) return new String(in.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		return null;
	}
}
