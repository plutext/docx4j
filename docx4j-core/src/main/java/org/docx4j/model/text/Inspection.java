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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * What {@link Inspect#of} found (WD section 9, CR-050): the findings, in the order of section 2.4
 * (the stored stories in document order, then the inventory by part name); the counts of their
 * groups for an output; whether the inspection is complete, and what it skipped or could not decide;
 * and what it does not detect.
 *
 * @since 17.3.4 (CR-050 phase 1)
 */
public final class Inspection {

	/** {@code COMPLETE}, or {@code PARTIAL} when anything was skipped (REQ-907). */
	public enum Status { COMPLETE, PARTIAL }

	/** The output a count is for (REQ-905): what each writes differs (alt text, properties). */
	public enum Format { PLAIN, MARKDOWN, BLOCKS }

	/** What an extraction may have beyond docx4j-core (REQ-513). */
	public enum Capability {
		/** docx4j-ImportXHTML on the class path: an MHT chunk is converted. */
		XHTML_IMPORTER
	}

	// --- skipped and undecided reasons (REQ-907) -------------------------------------------------------

	/** Content beyond one level: a chunk or embedded package inside an embedded package or a chunk. */
	public static final String DEPTH = "depth";
	/** A chunk of a type the inspector does not decode (an RTF chunk). */
	public static final String NOT_DECODED = "not_decoded";
	/** A payload that cannot be read (a package docx4j cannot load, a font that is not TrueType). */
	public static final String UNREADABLE = "unreadable";
	public static final String LIMIT_PAYLOAD_BYTES = "limit.payload_bytes";
	public static final String LIMIT_TOTAL_BYTES = "limit.total_bytes";
	public static final String LIMIT_ELEMENTS = "limit.elements";
	public static final String LIMIT_FINDINGS = "limit.findings";
	/** A background docx4j cannot determine (a shading pattern, a gradient or picture fill). */
	public static final String BACKGROUND_INDETERMINATE = "background.indeterminate";
	/** A shape neither provably off the page nor provably on it (D4). */
	public static final String POSITION_INDETERMINATE = "position.indeterminate";
	/** A shape whose paragraph has no section to give a page. */
	public static final String POSITION_NO_SECTION = "position.no_section";

	/**
	 * What an inspection does not detect (REQ-904, CR-050 section 2.6), each statement starting with
	 * its identifier.
	 */
	public static final List<String> LIMITATIONS = Collections.unmodifiableList(Arrays.asList(
			"layout.covered: an object covered by another object is not detected.",
			"layout.position: a position only a layout can settle is not judged (a shape placed relative to a paragraph"
					+ " that is on the page or not depending on where the paragraph falls); it is an undecided entry.",
			"layout.clipped: text clipped by a fixed-height row or frame is not detected.",
			"background.image: text whose background is an image, or a background docx4j cannot determine, is not"
					+ " compared with its colour.",
			"font.careful_swap: an embedded face whose glyphs are swapped with care (each letter given another's glyph,"
					+ " every glyph unique, of ordinary width and unnamed) is not detected; only recognising each glyph"
					+ " would see it. font.embedded marks the text such a face draws.",
			"payload.opaque: the content of a payload not decoded by design (an ActiveX control's binary, an OLE binary"
					+ " other than a plain-text package, a VBA project, a part of an unknown type) is not inspected.",
			"chart.workbook: a chart's embedded workbook is not inspected.",
			"intent: no finding says whether its text is, or is not, an attack."));

	/** A skipped or undecided entry (REQ-907): a reason at a location, {@code reason@chain|part|path}. */
	public static final class Entry {
		private final String reason;
		private final String location;

		Entry(String reason, String location) {
			this.reason = reason;
			this.location = location;
		}

		/** One of the reasons above. */
		public String getReason() { return reason; }

		/** {@code chain|part|path}: the chain's parts {@code >} apart, the path empty for a whole part. */
		public String getLocation() { return location; }

		@Override
		public String toString() {
			return reason + "@" + location;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Entry && ((Entry) o).reason.equals(reason) && ((Entry) o).location.equals(location);
		}

		@Override
		public int hashCode() {
			return Objects.hash(reason, location);
		}
	}

	/** The number of findings in each group for an output (REQ-903, CR-050 D14). */
	public static final class Counts {
		private final int unseen, omitted, active, info;
		private final boolean lowerBound;

		Counts(int unseen, int omitted, int active, int info, boolean lowerBound) {
			this.unseen = unseen;
			this.omitted = omitted;
			this.active = active;
			this.info = info;
			this.lowerBound = lowerBound;
		}

		/** In the output, and not seen by a person: the count an agent reading the text needs before it acts on it. */
		public int getUnseen() { return unseen; }
		public int getOmitted() { return omitted; }
		public int getActive() { return active; }
		public int getInfo() { return info; }

		/** Whether these are lower bounds: the inspection was {@code PARTIAL}. */
		public boolean isLowerBound() { return lowerBound; }

		public int get(Group group) {
			switch (group) {
			case UNSEEN: return unseen;
			case OMITTED: return omitted;
			case ACTIVE: return active;
			default: return info;
			}
		}

		@Override
		public String toString() {
			return "{unseen: " + unseen + ", omitted: " + omitted + ", active: " + active + ", info: " + info
					+ (lowerBound ? ", lower_bound: true" : "") + "}";
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Counts)) return false;
			Counts c = (Counts) o;
			return c.unseen == unseen && c.omitted == omitted && c.active == active && c.info == info && c.lowerBound == lowerBound;
		}

		@Override
		public int hashCode() {
			return Objects.hash(unseen, omitted, active, info, lowerBound);
		}
	}

	private final List<InspectFinding> findings;
	private final List<Entry> skipped;
	private final int skippedBeyond;
	private final List<Entry> undecided;

	Inspection(List<InspectFinding> findings, List<Entry> skipped, int skippedBeyond, List<Entry> undecided) {
		this.findings = Collections.unmodifiableList(new ArrayList<InspectFinding>(findings));
		this.skipped = Collections.unmodifiableList(new ArrayList<Entry>(skipped));
		this.skippedBeyond = skippedBeyond;
		this.undecided = Collections.unmodifiableList(new ArrayList<Entry>(undecided));
	}

	/** Every finding, at most {@value Inspect#MAX_FINDINGS}. */
	public List<InspectFinding> findings() {
		return findings;
	}

	/**
	 * The number of findings in each group for an output: its options (null for the defaults), its
	 * format, and what its extraction had (an XHTML importer or not). Lower bounds where the inspection
	 * is {@code PARTIAL}.
	 */
	public Counts counts(TextOptions options, Format format, Set<Capability> capabilities) {
		int[] n = new int[Group.values().length];
		for (InspectFinding f : findings) n[f.group(options, format, capabilities).ordinal()]++;
		return new Counts(n[Group.UNSEEN.ordinal()], n[Group.OMITTED.ordinal()], n[Group.ACTIVE.ordinal()],
				n[Group.INFO.ordinal()], status() == Status.PARTIAL);
	}

	/** The counts for an output whose extraction had none of the optional capabilities. */
	public Counts counts(TextOptions options, Format format) {
		return counts(options, format, EnumSet.noneOf(Capability.class));
	}

	/** {@code PARTIAL} when anything was skipped; a partial report never reads as a clean one. */
	public Status status() {
		return skipped.isEmpty() ? Status.COMPLETE : Status.PARTIAL;
	}

	/** What was not inspected, and why: the first {@value Inspect#MAX_SKIPPED}; {@link #skippedBeyond()} counts the rest. */
	public List<Entry> skipped() {
		return skipped;
	}

	/** The number of skipped entries beyond those {@link #skipped()} lists. */
	public int skippedBeyond() {
		return skippedBeyond;
	}

	/** What a rule could not settle (a background, a position): not findings, and not a reason to be partial. */
	public List<Entry> undecided() {
		return undecided;
	}

	/** {@link #LIMITATIONS}. */
	public List<String> limitations() {
		return LIMITATIONS;
	}
}
