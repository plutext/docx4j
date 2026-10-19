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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * docx4j's inspector (WD section 9; CR-050): where the text an agent holds diverges from the document
 * a person sees and from the file Office would open, reported without judging intent.
 *
 * <pre>
 * Inspection i = Inspect.of(pkg);
 * for (InspectFinding f : i.findings()) ...
 * Inspection.Counts c = i.counts(options, Inspection.Format.PLAIN);   // unseen, omitted, active, info
 * </pre>
 *
 * <p>Three passes make one inspection. <b>The stored stories, before any selection</b>: the main
 * document, every header and footer (used by a section or not), every note and comment (referenced or
 * not), the glossary, and each {@code altChunk}'s and embedded package's own stories one level deep;
 * in each, every {@code mc:AlternateContent} branch, a bound control's stored surface as well as its
 * value, field codes as well as results, and every drawing's {@code descr}, {@code title} and
 * {@code name}. <b>The package inventory</b>: macros, ActiveX, custom XML, the people part, the
 * document variables, {@code w:updateFields}, {@code w:rsids}, the properties, every relationship,
 * every chunk and embedded object, ink, parts of an unknown kind, and every embedded font, its faces
 * read for what their glyphs draw (D18). <b>The payloads</b>, decoded by the inspector itself: an
 * HTML, XHTML or MHT chunk scanned for scripts, event handlers and external references; a docx or Flat
 * OPC chunk and an embedded docx, xlsx or pptx inspected one level deep.</p>
 *
 * <p>It is read-only (REQ-204's snapshot is the test), and it never fetches, imports or computes: no
 * field is updated, no external target resolved, no importer run. Only the package's own faces are
 * read, never an installed font, so a finding is the same on every machine. Each finding's extraction
 * address is the text model's: the package is read once through it, with no importer, for each set of
 * options the findings' reveals name (not when a global limit stopped the inspection).</p>
 *
 * <p><b>What it does not detect</b> (REQ-904), as Word's Document Inspector says of itself: an object
 * covered by another; text placed where the page does not show it for a reason only a layout knows (a
 * shape positioned relative to a paragraph that may or may not fall on the page, text clipped by a
 * fixed-height row or frame, text behind an image); white text whose background is an image; an
 * embedded face whose glyphs are swapped with care, which only recognising each glyph would see
 * ({@code font.embedded} marks the text it draws); the content of a payload it does not decode by
 * design (ActiveX, OLE binaries, VBA, parts of an unknown kind); a chart's embedded workbook. And it
 * does not judge intent: no finding says its text is, or is not, an attack.
 * {@link Inspection#LIMITATIONS} states each.</p>
 *
 * <p><b>Bounds</b> (REQ-907, CR-050 D5 and D11), each enforced while decoding or walking: at most
 * {@value #MAX_FINDINGS} findings; {@value #MAX_PAYLOAD_BYTES} bytes decoded from any one payload (an
 * embedded font's de-obfuscated bytes are one) and {@value #MAX_TOTAL_BYTES} in all; {@value
 * #MAX_ELEMENTS} elements walked; embedded packages and chunks one level deep. A payload's limit stops
 * that payload; a global limit stops the inspection, and what was not inspected is listed in
 * {@link Inspection#skipped()} (the first {@value #MAX_SKIPPED}). Either makes the inspection
 * {@code PARTIAL}, and its counts lower bounds. What Office would run or fetch outside the stories (a
 * macro project, an ActiveX control, an external relationship, {@code w:updateFields}) is found before
 * the stories, so that a limit reached in them never drops it; the report lists it in its place.</p>
 *
 * <p><b>In an extraction</b> (REQ-903, CR-050 phase 2): under {@code inspect: true},
 * {@link Texts#of(WordprocessingMLPackage, TextOptions)} inspects the package too, and its result's
 * metadata carries the counts for its own options ({@link TextResult#INSPECT}). That inspection makes no
 * addresses, which the counts do not need.</p>
 *
 * @since 17.3.4 (CR-050 phase 1)
 */
public final class Inspect {

	private static final Logger log = LoggerFactory.getLogger(Inspect.class);

	/** An excerpt's bound, in code points (D5). */
	public static final int EXCERPT = 200;
	/** The findings an inspection makes at most (D11; 10,000 from CR-050 phase 2, where ordinary field-heavy documents passed 1,000). */
	public static final int MAX_FINDINGS = 10000;
	/** The bytes decoded from any one payload at most: 16 MB. */
	public static final long MAX_PAYLOAD_BYTES = 16L * 1024 * 1024;
	/** The bytes decoded from payloads in all at most: 64 MB. */
	public static final long MAX_TOTAL_BYTES = 64L * 1024 * 1024;
	/** The elements walked in all at most. */
	public static final long MAX_ELEMENTS = 5000000L;
	/** The skipped entries listed at most; the rest are counted. */
	public static final int MAX_SKIPPED = 1000;

	private Inspect() {}

	/** The inspection of a package. */
	public static Inspection of(WordprocessingMLPackage pkg) {
		return of(pkg, run());
	}

	private static InspectRun run() {
		return new InspectRun(MAX_FINDINGS, MAX_PAYLOAD_BYTES, MAX_TOTAL_BYTES, MAX_ELEMENTS);
	}

	static Inspection of(WordprocessingMLPackage pkg, InspectRun run) {
		if (pkg == null) throw new IllegalArgumentException("No package");
		// a report a global limit stopped carries no addresses: reading the package through the text model
		// is the cost the limit refused
		boolean stopped = walk(pkg, run);
		return new Inspection(InspectAddresses.findings(pkg, run, !stopped), run.skipped, run.skippedBeyond, run.undecided);
	}

	/** The three passes; true when a global limit stopped them. */
	private static boolean walk(WordprocessingMLPackage pkg, InspectRun run) {
		InspectPackage store = new InspectPackage(pkg, new ArrayList<String>(), run);
		try {
			new InspectWalk(store, run, 0, new java.util.HashSet<String>()).run();
			return false;
		} catch (InspectRun.Stop stop) {
			run.stopped(stop.reason);
			return true;
		} finally {
			run.placeRest();
		}
	}

	/**
	 * The counts of REQ-903 for an extraction of a package under its options, in each format, as
	 * {@link Texts#of(WordprocessingMLPackage, TextOptions)} carries them: the inspection with no
	 * addresses (a finding's group needs none). An inspection that fails is reported, not thrown: what it
	 * found before is counted, and it is {@code PARTIAL} ({@code unreadable} at the main document), so
	 * that the extraction it rides on still completes and its counts read as lower bounds.
	 */
	static Map<Inspection.Format, Inspection.Counts> counts(WordprocessingMLPackage pkg, TextOptions options) {
		InspectRun run = run();
		try {
			walk(pkg, run);
		} catch (RuntimeException e) {
			log.warn("The inspection for the counts failed; its counts are lower bounds: " + e, e);
			run.walking.clear(); // the packages it was walking when it failed; only a global stop reads them
			String main = pkg.getMainDocumentPart() == null ? "" : pkg.getMainDocumentPart().getPartName().getName();
			run.skip(Inspection.UNREADABLE, "|" + main + "|");
		}
		Inspection i = new Inspection(InspectAddresses.findings(pkg, run, false), run.skipped, run.skippedBeyond, run.undecided);
		Set<Inspection.Capability> capabilities = AltChunks.importerPresent() ? EnumSet.of(Inspection.Capability.XHTML_IMPORTER)
				: EnumSet.noneOf(Inspection.Capability.class);
		Map<Inspection.Format, Inspection.Counts> out = new EnumMap<Inspection.Format, Inspection.Counts>(Inspection.Format.class);
		for (Inspection.Format f : Inspection.Format.values()) out.put(f, i.counts(options, f, capabilities));
		return out;
	}
}
