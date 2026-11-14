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
package org.docx4j.convert.out.fo;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.chart.render.api.TextEffectPainter;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.model.shapes.WordArtText;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A body run's Word 2010 text effects through the FO renderer fork (CR-083 phase 5, D20 to D22; fop/CR-043), where the
 * renderer advertises {@code text-effects}.
 *
 * <ul>
 * <li>A run Word draws more of than its text in its colour ({@link WordArtText#hasBodyEffect}: an outline, a
 * gradient, a transparent fill, none, a shadow, a glow, a reflection) gets an id on its {@code fo:inline}
 * ({@code docx4j:text-effect}, D21), and its effects go into the conversion's table under it, as a WordArt run of its
 * formatting ({@link WordArtText#bodyRun}).</li>
 * <li>A run with a shadow, glow or reflection gets the room Word gives its line ({@code docx4j:effect-room}, above and
 * below in points, D22): phase 3's rule ({@link TextEffects#reach}) on the run's box at its stated size, unraised (phase
 * 5 findings section 6.2: a superscript's room is its run's at its stated size). An empty paragraph whose mark has one
 * gets it on its {@code fo:block} (wb81-23); Word draws nothing there.</li>
 * <li>A numbered paragraph's label (its number or bullet) is painted with its mark's effects too
 * ({@link #labelInline}): the id on an {@code fo:inline} around the label's text, the room above on the label's block,
 * folded into the label's ascent (wb82-30, -31).</li>
 * <li>The renderer finds the table through the painter docx4j sets on the conversion's {@code FOUserAgent}
 * ({@link #painter}), the {@link TextEffectPainter}, which draws each run's part on a line as Word does.</li>
 * </ul>
 *
 * <p>Both are written as hints ({@link #HINT_TEXT_EFFECT}, {@link #HINT_EFFECT_ROOM}) which {@link WordLayoutFixups}
 * turns into the {@code docx4j:} attributes, as it does a run's font, where the renderer takes them. Without the
 * capability nothing is written and nothing is set: the effects are dropped, a solid fill kept as the run's colour
 * (TextFill), as before.</p>
 *
 * @since 17.3.7
 */
public final class TextEffectsFo {

	private static final Logger log = LoggerFactory.getLogger(TextEffectsFo.class);

	/** The run's id into the conversion's table, a hint until WordLayoutFixups writes it as docx4j:text-effect */
	public static final String HINT_TEXT_EFFECT = "docx4j-text-effect";

	/** "above below" in points, a hint until WordLayoutFixups writes it as docx4j:effect-room */
	public static final String HINT_EFFECT_ROOM = "docx4j-effect-room";

	/**
	 * The run's formatting as a key, a hint WordLayoutFixups reads and removes: a run whose formatting is its previous
	 * sibling run's takes that run's id, as Word draws two such runs as one (cr083-phase5's wb80-15: one picture for two
	 * runs with one shadow)
	 */
	public static final String HINT_TEXT_EFFECT_KEY = "docx4j-text-effect-key";

	/**
	 * The renderer's attribute for a run's id (fop/CR-043's {@code WordLayoutElementMapping.TEXT_EFFECT}); kept here, not
	 * in docx4j's {@link org.docx4j.fop.wordlayout.WordLayoutElementMapping}, which names the attributes of the renderer
	 * docx4j is built against (2.12-docx4j.1, without text effects: WordLayoutAttributesTest), until docx4j moves to
	 * the renderer that has them
	 */
	public static final String TEXT_EFFECT = "text-effect";

	/** The renderer's attribute for the room (fop/CR-043's {@code WordLayoutElementMapping.EFFECT_ROOM}); as {@link #TEXT_EFFECT} */
	public static final String EFFECT_ROOM = "effect-room";

	/** The conversion settings' key of its table */
	static final String TABLE = "docx4j.textEffects";

	private TextEffectsFo() {}

	/**
	 * The room above and below a numbered paragraph's label (its number or bullet) which the mark's shadow, glow or
	 * reflection takes, "above below" in points: a hint on the label's {@code fo:block} until
	 * {@link WordLayoutFixups#listLabelLines} folds the room above into the label's ascent (see {@link #labelInline})
	 */
	public static final String HINT_LABEL_ROOM = "docx4j-label-effect-room";

	/** Tests only: whether the renderer draws text effects, null to ask it */
	static Boolean enabledForTest;

	/** Whether the renderer draws text effects (fop/CR-043) */
	public static boolean enabled() {
		if (enabledForTest != null) return enabledForTest;
		return FopCapabilities.has(FopCapabilities.Capability.TEXT_EFFECTS);
	}

	/** A conversion's runs by their ids */
	static final class Table {
		final Map<String, TextEffectPainter.Entry> runs = new ConcurrentHashMap<String, TextEffectPainter.Entry>();
		final AtomicInteger next = new AtomicInteger();
		volatile ThemeColors theme;

		String add(TextEffectPainter.Entry e) {
			String id = "fx" + next.incrementAndGet();
			runs.put(id, e);
			return id;
		}
	}

	/** The conversion's table, made on first use */
	static Table table(AbstractConversionSettings settings) {
		if (settings == null) return null;
		Map<String, Object> m = settings.getSettings();
		synchronized (m) {
			Object t = m.get(TABLE);
			if (!(t instanceof Table)) {
				t = new Table();
				m.put(TABLE, t);
			}
			return (Table) t;
		}
	}

	/**
	 * Marks a run's {@code fo:inline} with its effect's id and room, its effects put in the conversion's table; nothing
	 * where the renderer lacks the capability or the run has no effect.
	 *
	 * @param rPr the run's effective properties
	 */
	public static void mark(OpcPackage pkg, AbstractConversionSettings settings, RPr rPr, Element foInline) {
		String room = enter(pkg, settings, rPr, foInline);
		if (foInline != null && foInline.hasAttribute(HINT_TEXT_EFFECT)) foInline.setAttribute(HINT_TEXT_EFFECT_KEY, key(rPr));
		if (room != null) foInline.setAttribute(HINT_EFFECT_ROOM, room);
	}

	/**
	 * A numbered paragraph's label painted with its paragraph mark's effects (wb82-30: the number's glow a picture;
	 * wb82-31: the number filled with the mark's gradient): the {@code fo:inline} to hold the label's text, carrying
	 * the id (the renderer reads an id from the inlines above a text, never from the label's block, so the number
	 * can't be marked on its block), or null where there is nothing to mark, the label's text then going straight into
	 * its block as before.
	 *
	 * <p>The room: Word gave the one numbered line measured the mark's room above and none below (wb82-30: 2.88 pt at
	 * 24 pt with a glow of 8, phase 3's rule; the paragraph after it at the plain pitch), which is the label's own
	 * rule, its ascent raising the line and its descent counting for nothing ({@link WordLayoutFixups#listLabelLines}).
	 * So the room goes on the label's block as {@link #HINT_LABEL_ROOM}, for that method to add its above to the
	 * label's ascent, and not on the inline as a run's ({@link #HINT_EFFECT_ROOM}), which the renderer would read as
	 * room on both sides of the label's line alone, setting the label's baseline off the text's. One probe; the round
	 * cr083-phase5b asks the rest (a reflection, a bullet, 12 pt, each spacing rule, effects on the runs too).</p>
	 *
	 * @param rPr the label's effective properties: the paragraph mark's, under the level's where it has them
	 * @param labelBlock the label's {@code fo:block}, still empty
	 */
	public static Element labelInline(OpcPackage pkg, AbstractConversionSettings settings, RPr rPr, Element labelBlock) {
		if (labelBlock == null || !WordLayoutFixups.isEnabled()) return null;
		Element inline = labelBlock.getOwnerDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
		String room = enter(pkg, settings, rPr, inline);
		if (!inline.hasAttribute(HINT_TEXT_EFFECT)) return null;
		if (room != null) labelBlock.setAttribute(HINT_LABEL_ROOM, room);
		return inline;
	}

	/**
	 * A label drawn on the paragraph's own line (a centred or right-aligned numbered paragraph: an {@code fo:inline}
	 * at the head of its block): its id, so that the renderer paints it; no room, which is unmeasured there.
	 *
	 * @param rPr the label's effective properties, as {@link #labelInline}'s
	 */
	public static void markInlineLabel(OpcPackage pkg, AbstractConversionSettings settings, RPr rPr, Element label) {
		enter(pkg, settings, rPr, label);
	}

	/**
	 * The id on the element and the run's effects in the table, where the renderer has the capability and the run has
	 * an effect; returns the room its shadow, glow or reflection takes ({@link #room}), null for none or where nothing
	 * was marked.
	 */
	private static String enter(OpcPackage pkg, AbstractConversionSettings settings, RPr rPr, Element el) {
		if (rPr == null || el == null || !(pkg instanceof WordprocessingMLPackage) || !enabled()
				|| !WordArtText.hasBodyEffect(rPr)) {
			return null;
		}
		Table t = table(settings);
		if (t == null) return null;
		try {
			WordArtText.Run run = WordArtText.bodyRun((WordprocessingMLPackage) pkg, rPr);
			if (run == null) return null;
			if (t.theme == null) t.theme = ThemeColors.of((WordprocessingMLPackage) pkg);
			el.setAttribute(HINT_TEXT_EFFECT, t.add(new TextEffectPainter.Entry(run, t.theme)));
			return room(run, rPr);
		} catch (RuntimeException e) {
			log.warn(WordArtText.BODY_EFFECT + ": a run's text effects couldn't be read: " + e.getMessage());
			return null;
		}
	}

	/** The run's formatting as a key: its effective properties as XML, hashed */
	static String key(RPr rPr) {
		try {
			String xml = org.docx4j.XmlUtils.marshaltoString(rPr, true, false);
			return Integer.toHexString(xml.hashCode()) + "-" + xml.length();
		} catch (RuntimeException e) {
			return "x" + System.identityHashCode(rPr);
		}
	}

	/**
	 * The room an empty paragraph's mark takes on its {@code fo:block}, where its mark has a shadow, glow or
	 * reflection (wb81-23); nothing where the renderer lacks the capability
	 *
	 * @param rPrMark the paragraph mark's effective properties
	 */
	public static void markEmpty(OpcPackage pkg, RPr rPrMark, Element foBlock) {
		if (rPrMark == null || foBlock == null || !(pkg instanceof WordprocessingMLPackage) || !enabled()) return;
		if (rPrMark.getGlow() == null && rPrMark.getShadow14() == null && rPrMark.getReflection() == null) return;
		WordArtText.Run run = WordArtText.bodyRun((WordprocessingMLPackage) pkg, rPrMark);
		String room = run == null ? null : room(run, rPrMark);
		if (room != null) foBlock.setAttribute(HINT_EFFECT_ROOM, room);
	}

	/**
	 * The room above and below a run's box its shadow, glow and reflection take, as "above below" in points; null for
	 * none. Phase 3's rule on the run's box at the size it states, unraised (a superscript's at its run's size: wb81-21,
	 * -22), with Word's metrics for its face; a shadow skewed down ({@code ky}) is taken at no width (its room across
	 * the run is unmeasured).
	 */
	static String room(WordArtText.Run run, RPr rPr) {
		if (run.getEffects() == null) return null;
		double size = rPr.getSz() == null || rPr.getSz().getVal() == null ? 10 : rPr.getSz().getVal().doubleValue() / 2;
		double asc = (run.getAscent() + run.getLeading()) * size, desc = run.getDescent() * size;
		double[] reach = TextEffects.reach(run.getEffects(), size, asc, desc, 0);
		double above = Math.max(0, reach[0] - asc), below = Math.max(0, reach[1] - desc);
		if (above < 0.005 && below < 0.005) return null;
		return String.format(Locale.ROOT, "%.3f %.3f", above, below);
	}

	/**
	 * The painter for a conversion: the {@link TextEffectPainter} over its table; null where the renderer lacks the
	 * capability. Set on the conversion's {@code FOUserAgent} by {@code FORendererApacheFOP}.
	 */
	public static TextEffectPainter painter(AbstractConversionSettings settings) {
		if (!enabled()) return null;
		Table t = table(settings);
		return t == null ? null : new TextEffectPainter(id -> t.runs.get(id));
	}
}
