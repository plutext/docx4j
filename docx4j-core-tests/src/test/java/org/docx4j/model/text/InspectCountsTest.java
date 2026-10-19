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

import static org.docx4j.model.text.InspectTest.NS;
import static org.docx4j.model.text.InspectTest.fixture;
import static org.docx4j.model.text.InspectTest.of;
import static org.docx4j.model.text.InspectTest.reasons;
import static org.docx4j.model.text.InspectTest.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.model.text.InspectTest.Doc;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * CR-050 phase 2: the inspection counts every extraction result carries (WD REQ-903), {@code inspect:
 * false}, and the rulings taken before the phase: an embedded face's tables charged to the total, an
 * unanchored comment left out of the text, and a note read through its best reference. The suite's
 * check of the counts on the fixtures is {@code ExtractionConformanceTest}'s.
 */
public class InspectCountsTest {

	/** Option sets whose counts differ on the fixtures (each moves findings between groups), with the counts asked for. */
	static List<TextOptions> optionSets() {
		List<TextOptions> sets = Arrays.asList(new TextOptions(), new TextOptions().hidden(true), new TextOptions().comments(true),
				TextOptions.of(TextView.MARKUP), TextOptions.of(TextView.ORIGINAL), new TextOptions().embedded(true),
				new TextOptions().include(TextOptions.Include.HEADERS, TextOptions.Include.FOOTERS, TextOptions.Include.GLOSSARY),
				new TextOptions().metadata(true).altText(true).fieldCodes(true).toc(true),
				new TextOptions().bindings(TextOptions.Bindings.SURFACE).smartart(false).notes(false));
		for (TextOptions o : sets) o.inspect(true);
		return sets;
	}

	static TextOptions counted() {
		return new TextOptions().inspect(true);
	}

	// --- the counts --------------------------------------------------------------------------------------

	/**
	 * A package's result carries, for its own options, in each format, the counts the inspector gives
	 * (REQ-903): the extraction's inspection, which makes no addresses, finds what {@link Inspect#of}
	 * finds. The metadata map carries the same, unseen first.
	 */
	@Test
	public void everyResultCarriesTheInspectorsCounts() throws Exception {
		Set<Inspection.Capability> capabilities = AltChunks.importerPresent() ? EnumSet.of(Inspection.Capability.XHTML_IMPORTER)
				: EnumSet.noneOf(Inspection.Capability.class);
		for (String name : InspectTest.FIXTURES) {
			WordprocessingMLPackage pkg = fixture(name);
			Inspection inspection = Inspect.of(pkg);
			for (TextOptions o : optionSets()) {
				TextResult r = Texts.of(pkg, o);
				Map<?, ?> inspect = (Map<?, ?>) r.metadata().get(TextResult.INSPECT);
				assertNotNull(name + " " + o, inspect);
				assertEquals(Arrays.asList("status", "lower_bound", "plain", "markdown", "blocks"), new ArrayList<Object>(inspect.keySet()));
				assertEquals(inspection.status().name(), inspect.get("status"));
				for (Inspection.Format f : Inspection.Format.values()) {
					Inspection.Counts want = inspection.counts(o, f, capabilities);
					assertEquals(name + " " + o + " " + f, want, r.counts(f));
					Map<?, ?> m = (Map<?, ?>) inspect.get(f.name().toLowerCase(java.util.Locale.ROOT));
					assertEquals(Arrays.asList("unseen", "omitted", "active", "info"), new ArrayList<Object>(m.keySet()));
					for (Group g : Group.values()) assertEquals(want.get(g), m.get(g.id()));
					assertEquals(want.isLowerBound(), inspect.get("lower_bound"));
				}
			}
		}
	}

	/** The groups move with the options, so the counts do: the main probe's hidden text is omitted, then unseen. */
	@Test
	public void theCountsFollowTheOptions() throws Exception {
		WordprocessingMLPackage pkg = fixture("extraction-probe.docx");
		Inspection.Counts defaults = Texts.of(pkg, counted()).counts(Inspection.Format.PLAIN);
		Inspection.Counts hidden = Texts.of(pkg, counted().hidden(true)).counts(Inspection.Format.PLAIN);
		assertTrue(defaults + " " + hidden, hidden.getUnseen() > defaults.getUnseen());
		assertTrue(defaults + " " + hidden, hidden.getOmitted() < defaults.getOmitted());
		assertEquals(defaults.getActive(), hidden.getActive());
		int all = defaults.getUnseen() + defaults.getOmitted() + defaults.getActive() + defaults.getInfo();
		assertEquals("a change of option moves findings between groups, never in or out of the report", all,
				hidden.getUnseen() + hidden.getOmitted() + hidden.getActive() + hidden.getInfo());
	}

	/** REQ-204: an extraction, its inspection included, changes nothing. */
	@Test
	public void theCountsChangeNothing() throws Exception {
		for (String name : InspectTest.FIXTURES) {
			WordprocessingMLPackage pkg = fixture(name);
			String before = TextsPhase2Test.snapshot(pkg);
			for (TextOptions o : optionSets()) Texts.of(pkg, o);
			assertEquals(name, before, TextsPhase2Test.snapshot(pkg));
		}
	}

	/**
	 * {@code inspect} (D9), docx4j's option outside WD section 10: off by default (its measured cost), the
	 * result then without counts and with the same text; on by its WD-style name and value too.
	 */
	@Test
	public void inspectIsAskedFor() throws Exception {
		WordprocessingMLPackage pkg = fixture("extraction-probe.docx");
		assertFalse(TextOptions.INSPECT_DEFAULT);
		assertFalse(new TextOptions().isInspect());
		assertNull(Texts.of(pkg).metadata().get(TextResult.INSPECT));
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("inspect", "true");
		TextOptions on = TextOptions.of(m);
		assertTrue(on.isInspect());
		TextResult without = Texts.of(pkg, new TextOptions().inspect(false)), with = Texts.of(pkg, on);
		assertNull(without.metadata().get(TextResult.INSPECT));
		assertNull(without.counts(Inspection.Format.PLAIN));
		assertNotNull(with.counts(Inspection.Format.PLAIN));
		assertEquals(PlainText.of(with), PlainText.of(without));
		Map<String, Object> rest = new LinkedHashMap<String, Object>(with.metadata());
		rest.remove(TextResult.INSPECT);
		assertEquals(without.metadata(), rest);
		assertEquals(TextOptions.NAMES, new ArrayList<String>(on.toMap().keySet()));
		assertTrue(on.toString(), on.toString().contains("inspect=true"));
		assertFalse(new TextOptions().toString().contains("inspect"));
		assertTrue(on.copy().isInspect());
		try {
			new TextOptions().set("inspection", true);
			fail();
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("inspect"));
		}
	}

	/** A part's, a fragment's and a resolved copy's results carry no counts: the inspection is the package's. */
	@Test
	public void aPartOrFragmentHasNone() throws Exception {
		WordprocessingMLPackage pkg = fixture("extraction-probe.docx");
		TextOptions o = counted();
		P first = null;
		for (Object c : pkg.getMainDocumentPart().getContent()) if (first == null && XmlUtils.unwrap(c) instanceof P) first = (P) XmlUtils.unwrap(c);
		for (TextResult r : Arrays.asList(Texts.of(pkg.getMainDocumentPart(), o), Texts.of(pkg, first, o), Texts.of(first, o))) {
			assertNull(r.metadata().get(TextResult.INSPECT));
			assertNull(r.counts(Inspection.Format.BLOCKS));
		}
		WordprocessingMLPackage resolved = fixture("extraction-probe.docx");
		org.docx4j.model.revisions.TrackedChanges.acceptAll(resolved);
		assertNull(Texts.ofResolved(resolved, o).metadata().get(TextResult.INSPECT));
	}

	// --- an embedded face's tables, charged to the total (REQ-907) ----------------------------------------

	static byte[] probeFace() throws Exception {
		return Files.readAllBytes(ViewAgreementTest.resources().resolve("text-extraction/support/inspect-fonts/inspect-probe-plain.ttf"));
	}

	/**
	 * The probe face with its cmap replaced by one format 12 subtable mapping the first {@code codes} code
	 * points, six at a time, to its glyphs 1 to 6: a few hundred kilobytes that map a quarter of a
	 * million code points.
	 */
	static byte[] wideFace(int codes) throws Exception {
		return wideFace(codes, 1);
	}

	/** As {@link #wideFace(int)}, each group starting at glyph {@code start} (read unsigned). */
	static byte[] wideFace(int codes, int start) throws Exception {
		byte[] font = probeFace();
		int dir = -1;
		for (int t = 0, n = ByteBuffer.wrap(font).getShort(4) & 0xffff; t < n; t++) {
			if (new String(font, 12 + 16 * t, 4, StandardCharsets.ISO_8859_1).equals("cmap")) dir = 12 + 16 * t;
		}
		int groups = (codes + 5) / 6, sub = 16 + 12 * groups;
		ByteBuffer cmap = ByteBuffer.allocate(4 + 8 + sub);
		cmap.putShort((short) 0).putShort((short) 1).putShort((short) 3).putShort((short) 10).putInt(12);
		cmap.putShort((short) 12).putShort((short) 0).putInt(sub).putInt(0).putInt(groups);
		for (int g = 0; g < groups; g++) cmap.putInt(6 * g).putInt(Math.min(6 * g + 5, codes - 1)).putInt(start);
		byte[] wide = Arrays.copyOf(font, font.length + cmap.capacity());
		System.arraycopy(cmap.array(), 0, wide, font.length, cmap.capacity());
		ByteBuffer.wrap(wide).putInt(dir + 8, font.length).putInt(dir + 12, cmap.capacity());
		return wide;
	}

	/** What a face holds: 10 bytes a code point it maps, 5 a glyph, its glyphs' names; held compactly, the map's order kept. */
	@Test
	public void aFaceHoldsItsMapCompactly() throws Exception {
		InspectFace plain = InspectFace.read(probeFace());
		assertTrue("held " + plain.held(), plain.held() >= 10 * 6 + 5 * 7 && plain.held() < 2000);
		InspectFace wide = InspectFace.read(wideFace(0x40000));
		assertTrue("held " + wide.held(), wide.held() >= 10L * 0x40000 && wide.held() < 10L * 0x40000 + 2000);
		assertEquals(Integer.valueOf(1), wide.glyph(0));
		assertEquals(Integer.valueOf(1 + 0x3FFFF % 6), wide.glyph(0x3FFFF));
		assertNull(wide.glyph(0x40000));
		List<Integer> codes = wide.codes(3);
		assertEquals(Integer.valueOf(2), codes.get(0));
		assertEquals(Integer.valueOf(8), codes.get(1));
		try {
			InspectFace.read(wideFace(0x40000), 10L * 0x40000);
			fail("over its allowance");
		} catch (InspectFace.OverBudget expected) {
		}
		// a start glyph of 0xFFFFFFFE is past the face's glyphs, not glyph 65534 (docx4j-43's review)
		InspectFace past = InspectFace.read(wideFace(6, 0xFFFFFFFE));
		assertNull(past.glyph(0));
		assertNull(past.glyph(1));
		assertNull(past.glyph(2));
		assertEquals(Integer.valueOf(1), past.glyph(3));
	}

	/**
	 * Faces each a few hundred kilobytes that map 262,144 code points, about 2.6 MB of tables each as
	 * held (of the order of 20 MB as maps of objects, before phase 2): the 64 MB total stops the
	 * inspection after some twenty, where their bytes alone (a sixth of that) would not.
	 */
	@Test(timeout = 60000)
	public void manyWideFacesReachTheTotal() throws Exception {
		Doc d = new Doc();
		d.p(run("BODY"));
		FontTablePart table = new FontTablePart();
		d.mdp.addTargetPart(table);
		StringBuilder fonts = new StringBuilder("<w:fonts" + NS + ">");
		byte[] wide = wideFace(0x40000);
		int faces = 40;
		for (int n = 0; n < faces; n++) {
			BinaryPart face = new BinaryPart(new PartName("/word/fonts/font" + n + ".ttf"));
			face.setContentType(new ContentType("application/x-font-ttf"));
			face.setRelationshipType(Namespaces.FONT);
			face.setBinaryData(wide);
			String id = table.addTargetPart(face).getId();
			fonts.append("<w:font w:name='Wide ").append(n).append("'><w:embedRegular r:id='").append(id).append("'/></w:font>");
		}
		table.setJaxbElement((org.docx4j.wml.Fonts) XmlUtils.unmarshalString(fonts + "</w:fonts>"));
		WordprocessingMLPackage pkg = d.done();
		assertTrue("the faces' bytes are within the total", (long) faces * wide.length < Inspect.MAX_TOTAL_BYTES);
		Inspection i = Inspect.of(pkg);
		assertEquals(Inspection.Status.PARTIAL, i.status());
		assertTrue(reasons(i).toString(), reasons(i).contains(Inspection.LIMIT_TOTAL_BYTES));
		Inspection.Counts c = Texts.of(pkg, counted()).counts(Inspection.Format.PLAIN);
		assertTrue(c.isLowerBound());
	}

	// --- an unanchored comment (CR-050's ruling before phase 2) --------------------------------------------

	/**
	 * A comment with no reference and no range in the document is left out of the text model's comments,
	 * as Word drops it on save; one anchored in a note is kept. The inspector agrees: the one is
	 * {@code story.unused}, the others {@code comment}.
	 */
	@Test
	public void anUnanchoredCommentIsNotText() throws Exception {
		Doc d = new Doc();
		FootnotesPart notes = new FootnotesPart();
		notes.setJaxbElement((org.docx4j.wml.CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:footnotes" + NS + ">"
				+ "<w:footnote w:id='1'><w:p>" + run("NOTE_TEXT") + "<w:r><w:commentReference w:id='2'/></w:r></w:p></w:footnote>"
				+ "</w:footnotes>")));
		d.mdp.addTargetPart(notes);
		CommentsPart comments = new CommentsPart();
		comments.setJaxbElement((org.docx4j.wml.Comments) XmlUtils.unmarshalString("<w:comments" + NS + ">"
				+ "<w:comment w:id='1' w:author='A'><w:p>" + run("IN_BODY") + "</w:p></w:comment>"
				+ "<w:comment w:id='2' w:author='A'><w:p>" + run("IN_NOTE") + "</w:p></w:comment>"
				+ "<w:comment w:id='3' w:author='A'><w:p>" + run("NOWHERE") + "</w:p></w:comment></w:comments>"));
		d.mdp.addTargetPart(comments);
		d.p("<w:commentRangeStart w:id='1'/>" + run("BODY") + "<w:commentRangeEnd w:id='1'/><w:r><w:commentReference w:id='1'/></w:r>"
				+ "<w:r><w:footnoteReference w:id='1'/></w:r>");
		WordprocessingMLPackage pkg = d.done();
		for (TextOptions o : Arrays.asList(new TextOptions().comments(true), TextOptions.of(TextView.MARKUP))) {
			TextResult r = Texts.of(pkg, o);
			assertEquals(o.toString(), Arrays.asList("1", "2"), new ArrayList<String>(r.comments().keySet()));
			assertFalse(PlainText.of(r).contains("NOWHERE"));
		}
		Inspection i = Inspect.of(pkg);
		List<String> found = new ArrayList<String>(), unused = new ArrayList<String>();
		for (InspectFinding f : of(i, InspectFinding.COMMENT)) found.add(f.getExcerpt());
		for (InspectFinding f : of(i, InspectFinding.STORY_UNUSED)) unused.add(f.getExcerpt());
		assertEquals(Arrays.asList("IN_BODY", "IN_NOTE"), found);
		assertEquals(Collections.singletonList("NOWHERE"), unused);
	}

	// --- a note read through its best reference (REQ-905) --------------------------------------------------

	static final String DEL = "<w:del w:id='90' w:author='A' w:date='2026-10-19T00:00:00Z'>";
	static final String INS = "<w:ins w:id='91' w:author='A' w:date='2026-10-19T00:00:00Z'>";

	/**
	 * A note's membership is its best reference's (fewest options beyond the defaults, live before
	 * inserted before deleted, the main document before a header): what that reference needs to be read,
	 * never one reference's story with another's revision (the portfolio's question of 19 October 2026: a
	 * deleted reference in the main document and a live one in a header made a note of the default
	 * output). Checked against the text model: under the defaults and the reveal's options, the note is
	 * in the output exactly when the text holds it; where its references need the same, under every
	 * option set. Two references of different needs are one conjunction's approximation (REQ-905 has no
	 * disjunction): under options meeting the other's needs and not the best's, the text holds the note
	 * and the counts have it out of the output.
	 */
	@Test
	public void aNoteIsReadThroughItsBestReference() throws Exception {
		String ref = "<w:r><w:footnoteReference w:id='1'/></w:r>", hiddenRef = "<w:r><w:rPr><w:vanish/></w:rPr><w:footnoteReference w:id='1'/></w:r>";
		Object[][] cases = {
				// the main document's reference, the header's, the reveal, whether every option set agrees
				{ DEL + ref + "</w:del>", ref, "options: include: headers", false },
				{ DEL + ref + "</w:del>", null, "options: view: original", true },
				{ INS + ref + "</w:ins>", null, "included", true },
				{ hiddenRef, null, "options: hidden: true", true },
				{ hiddenRef, ref, "options: hidden: true", false },
				{ ref, ref, "included", true },
				{ "<w:r><w:t>NONE</w:t></w:r>", ref, "options: include: headers", true } };
		List<TextOptions> sets = Arrays.asList(new TextOptions(), TextOptions.of(TextView.ORIGINAL), TextOptions.of(TextView.MARKUP),
				new TextOptions().include(TextOptions.Include.HEADERS), new TextOptions().hidden(true),
				new TextOptions().include(TextOptions.Include.HEADERS).view(TextView.ORIGINAL));
		for (Object[] c : cases) {
			Doc d = new Doc();
			FootnotesPart notes = new FootnotesPart();
			notes.setJaxbElement((org.docx4j.wml.CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:footnotes" + NS + ">"
					+ "<w:footnote w:id='1'><w:p>" + run("THE_NOTE") + "</w:p></w:footnote></w:footnotes>")));
			d.mdp.addTargetPart(notes);
			if (c[1] != null) {
				HeaderPart h = new HeaderPart(new PartName("/word/header1.xml"));
				h.setJaxbElement((org.docx4j.wml.Hdr) XmlUtils.unmarshalString("<w:hdr" + NS + "><w:p>" + run("HEADER") + c[1] + "</w:p></w:hdr>"));
				d.sectPr = "<w:sectPr><w:headerReference w:type='default' r:id='" + d.mdp.addTargetPart(h).getId() + "'/></w:sectPr>";
			}
			d.p(run("BODY") + c[0]);
			WordprocessingMLPackage pkg = d.done();
			List<InspectFinding> note = of(Inspect.of(pkg), InspectFinding.NOTE);
			assertEquals(Arrays.toString(c), 1, note.size());
			Reveal reveal = note.get(0).getReveal();
			assertEquals(Arrays.toString(c), c[2], reveal.toString());
			TextOptions revealed = new TextOptions();
			for (String option : reveal.getOptions()) {
				String[] nv = option.split(":\\s*", 2);
				if (nv[0].equals("include")) revealed.include(TextOptions.Include.valueOf(nv[1].toUpperCase(java.util.Locale.ROOT)));
				else revealed.set(nv[0], nv[1]);
			}
			List<TextOptions> checked = (Boolean) c[3] ? sets : Arrays.asList(new TextOptions(), revealed);
			for (TextOptions o : checked) {
				boolean text = PlainText.of(Texts.of(pkg, o)).contains("THE_NOTE");
				assertEquals(Arrays.toString(c) + " " + o, text, note.get(0).isIn(o, Inspection.Format.PLAIN, null));
			}
			assertTrue(Arrays.toString(c), PlainText.of(Texts.of(pkg, revealed)).contains("THE_NOTE"));
		}
	}
}
