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
package org.docx4j.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What docx4j decided to draw one document font in, and why.
 *
 * <p>Until 17.1.1 the {@link Mapper} held only the <em>result</em> - the physical font,
 * its faces, the set of Word-defaulted names - and the passes said what they had done at
 * DEBUG.  So neither a user nor the layout triage could learn from the API why a font had
 * been drawn in what it was drawn in: CR-001 batch 42's first hours went on
 * reconstructing, per document, which pass had answered, with the PDF's own font list as
 * the only evidence (CR-017 gap 1).</p>
 *
 * <p>Every pass that maps a font now records one of these, and
 * {@link Mapper#getDecisions()} returns them.  The mapping itself is unchanged: this
 * records the decision, it does not make it.</p>
 *
 * <p>The volatile parts - the physical font, its faces, the line box - are read off the
 * mapper when the decision is handed out, not when the pass recorded it, since a later
 * pass may re-map the font ({@link Mapper#addNoBoldFaceAliases}) or register the family
 * whose line metrics it takes.</p>
 *
 * @since 17.1.1
 */
public final class FontDecision {

	/** Which pass answered. */
	public enum Source {

		/** The machine has a font of that name. */
		INSTALLED,
		/** The document embeds it ({@code w:embedRegular} and the rest). */
		EMBEDDED,
		/** A metric-compatible clone from {@code font-substitutes.xml}: Word's advances. */
		METRIC_CLONE,
		/** No clone exists; the closest face measured against Word's own PDF
		 *  ({@code font-substitutes.xml}, quality {@code measured}). */
		MEASURED_STAND_IN,
		/** The document's own {@code w:altName}, resolved ({@link Mapper#addAltNameSubstitutes}). */
		ALT_NAME,
		/** A face of the same class ({@link FontFallback#selectByClass}, or a table row of
		 *  quality {@code class}): the widths are not the document font's. */
		CLASS,
		/** What Word itself draws a font it cannot find in
		 *  ({@link Mapper#addWordDefaultSubstitutes}). */
		WORD_DEFAULT,
		/** The mapper's own answer: a variant of the name ({@link IdentityPlusMapper}), a
		 *  panose match or a FontSubstitutions.xml entry ({@link BestMatchingMapper}). */
		MAPPER_OWN,
		/** A symbol font, whose characters {@link RunFontSelector} draws in the face
		 *  {@link PhysicalFonts#getWDingsFont} or {@link PhysicalFonts#getSymbolFont}
		 *  picks, whatever the mapper made of the name. */
		SYMBOL,
		/** Nothing mapped it: FOP will draw it in its base-14 fallback. */
		UNMAPPED
	}

	/** Where a script (or the symbol or emoji blocks) went to another face during the
	 *  conversion - {@link RunFontSelector}'s coverage pass and the per-script substitutes
	 *  of {@code font-substitutes.xml}. */
	public static final class ScriptChoice {

		private final String script;
		private final String face;
		private final String error;

		ScriptChoice(String script, String face, String error) {
			this.script = script;
			this.face = face;
			this.error = error;
		}

		/** The coverage group: a {@link Character.UnicodeScript} name,
		 *  {@link FontFallback#SYMBOL_GROUP} or {@link FontFallback#EMOJI_GROUP}. */
		public String getScript() { return script; }

		/** The face that script was drawn in, or null where nothing installed covers it
		 *  (the text renders as notdef; {@link FontFallback#warnNoCoverage} says so). */
		public String getFace() { return face; }

		/** What was measured of that face for this font and script, where the table knows
		 *  ({@code font-substitutes.xml}); else null. */
		public String getError() { return error; }

		@Override
		public String toString() {
			return script + " -> " + (face==null ? "nothing installed covers it" : face)
					+ (error==null ? "" : " (" + error + ")");
		}
	}

	private final String documentFont;
	private final Source source;
	private final String via;
	private final String widthError;
	private final List<ScriptChoice> perScript = Collections.synchronizedList(new ArrayList<ScriptChoice>());

	private PhysicalFont physical;
	private PhysicalFont fixedPhysical;
	private String boldFace;
	private String italicFace;
	private String boldItalicFace;
	private String lineBox;
	private double widthFactor = 1;

	FontDecision(String documentFont, Source source, String via, String widthError) {
		this.documentFont = documentFont;
		this.source = source;
		this.via = via;
		this.widthError = widthError;
	}

	/** The font's name as the document has it. */
	public String getDocumentFont() { return documentFont; }

	public Source getSource() { return source; }

	/** How this source got there: the {@code w:altName} chain hop by hop, the class, the
	 *  family Word substitutes for an unknown font, the mapper's own answer.  Null where
	 *  the source says it all (an installed or embedded font). */
	public String getVia() { return via; }

	/** What is known of the substitute's width error: the measurement
	 *  {@code font-substitutes.xml} records, with the {@link WidthFactors} factor where
	 *  one applies; {@code unknown} for a face chosen on class alone; null where there is
	 *  no error to speak of (the font itself, or its metric clone). */
	public String getWidthError() {
		if (widthFactor==1) return widthError;
		String factor = "width factor " + widthFactor + " applied";
		return widthError==null ? factor : widthError + "; " + factor;
	}

	/** The font this document font is drawn in; null where nothing mapped it. */
	public PhysicalFont getPhysicalFont() { return physical; }

	/** The bold face, or {@code synthetic} where FOP emboldens the regular one - which is
	 *  what Word does for a family that has no bold of its own.  Null where the font is
	 *  unmapped. */
	public String getBoldFace() { return boldFace; }

	/** The italic face, or {@code synthetic}; null where the font is unmapped. */
	public String getItalicFace() { return italicFace; }

	/** The bold-italic face, or {@code synthetic}; null where the font is unmapped. */
	public String getBoldItalicFace() { return boldItalicFace; }

	/**
	 * Whose metrics the line box takes (&#xa7;2.7; {@link WordLineMetrics}):
	 * {@code documentFont} where the table knows this family, {@code alias:<family>} where
	 * the document's own {@code w:altName} sent it to another, {@code wordDefault:<family>}
	 * where Word's answer for a font it cannot find did, and {@code substitute} where
	 * nothing is known and the physical font's own metrics are used.
	 */
	public String getLineBox() { return lineBox; }

	/** The measured width factor {@link RunFontSelector} applies to this pair, or 1. */
	public double getWidthFactor() { return widthFactor; }

	/** Where a script went to another face during the conversion, in the order the
	 *  selector met them. */
	public List<ScriptChoice> getPerScript() {
		synchronized (perScript) {
			return Collections.unmodifiableList(new ArrayList<ScriptChoice>(perScript));
		}
	}

	// ---- filled in by the Mapper

	void addScriptChoice(String script, String face, String error) {
		synchronized (perScript) {
			for (ScriptChoice existing : perScript) {
				if (existing.getScript().equals(script)) return; // the selector caches per pair
			}
			perScript.add(new ScriptChoice(script, face, error));
		}
	}

	/** The face this decision names itself, rather than the mapper's mapping: a symbol
	 *  font is drawn in the face that has the glyphs, whatever the name mapped to. */
	void setFixedPhysical(PhysicalFont pf) {
		this.fixedPhysical = pf;
	}

	PhysicalFont getFixedPhysical() {
		return fixedPhysical;
	}

	void copyScriptChoicesFrom(FontDecision other) {
		if (other==null) return;
		for (ScriptChoice sc : other.getPerScript()) addScriptChoice(sc.getScript(), sc.getFace(), sc.getError());
	}

	void complete(PhysicalFont physical, String boldFace, String italicFace, String boldItalicFace,
			String lineBox, double widthFactor) {
		this.physical = physical;
		this.boldFace = boldFace;
		this.italicFace = italicFace;
		this.boldItalicFace = boldItalicFace;
		this.lineBox = lineBox;
		this.widthFactor = widthFactor;
	}

	/** One line, as the conversion log and a bisect want it. */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(documentFont).append(": ").append(source);
		if (physical!=null) sb.append(" -> ").append(physical.getName());
		if (via!=null) sb.append(" (").append(via).append(")");
		String error = getWidthError();
		if (error!=null) sb.append(" [").append(error).append("]");
		if (lineBox!=null) sb.append(" lineBox=").append(lineBox);
		if (boldFace!=null) {
			sb.append(" faces=").append(boldFace).append(" / ").append(italicFace)
					.append(" / ").append(boldItalicFace);
		}
		for (ScriptChoice sc : getPerScript()) sb.append("; ").append(sc);
		return sb.toString();
	}
}
