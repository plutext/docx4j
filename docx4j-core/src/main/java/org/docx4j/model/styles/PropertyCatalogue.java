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
package org.docx4j.model.styles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTCnf;
import org.docx4j.wml.CTEastAsianLayout;
import org.docx4j.wml.CTEm;
import org.docx4j.wml.CTFitText;
import org.docx4j.wml.CTFramePr;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTSignedHpsMeasure;
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.CTTextEffect;
import org.docx4j.wml.CTTextScale;
import org.docx4j.wml.CTTextboxTightWrap;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.Color;
import org.docx4j.wml.Highlight;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.TextDirection;
import org.docx4j.wml.U;

/**
 * The members of a properties element ({@code w:rPr}, {@code w:pPr}), each with how to
 * read it, write it, merge a more specific value over an inherited one, and whether it
 * is formatting.  One list per element type drives {@link StyleUtil#apply},
 * {@link StyleUtil#isEmpty}, {@link StyleUtil#unset} and
 * {@link StyleUtil#hasDirectFormatting}, so the four cannot drift apart as the four
 * hand-maintained lists they replace had (CR-015: {@code w:rtl}-only direct
 * formatting was not direct formatting, a style stating only {@code w:lang} was
 * empty, an {@code ilvl}-only {@code w:numPr} was dropped).
 *
 * <p>{@link #RUN} covers {@code w:rPr} and {@code w:pPr/w:rPr} together: the two
 * generated classes ({@link RPr}, {@link ParaRPr}) have the same members and no common
 * supertype, so each entry carries an accessor pair for each and a value moves between
 * them without a copy of the table.  {@link #PARAGRAPH} covers {@link PPrBase}, which
 * {@code w:pPr} extends.  Members that are not formatting - a style reference, a
 * revision record, Word's table-condition cache - are in the lists (so that {@code apply}
 * carries them and {@code isEmpty} sees them) but flagged, and
 * {@link #hasDirectFormatting} leaves them out.</p>
 *
 * <p>Order is schema order.  The merge of each entry is the leaf {@code StyleUtil.apply}
 * for its type where one exists (those carry the measured rules: {@code w:ind},
 * {@code w:tabs}, {@code w:framePr}, {@code w:spacing}) and "the source's value if it
 * has one" otherwise.</p>
 *
 * @since 17.1.1
 */
public final class PropertyCatalogue {

	private PropertyCatalogue() {}

	/**
	 * One member: name, value type, accessors, merge and emptiness.
	 *
	 * @param <O> the owning element type ({@link PPrBase}; or {@link Object} for the run
	 *            members, which accept an {@link RPr} or a {@link ParaRPr})
	 * @param <V> the member's type
	 */
	public static final class Property<O, V> {

		private final String name;
		private final Class<V> type;
		private final boolean formatting;
		private final Function<O, V> getter;
		private final BiConsumer<O, V> setter;
		private final BinaryOperator<V> merge;
		private final Predicate<V> empty;

		Property(String name, Class<V> type, boolean formatting, Function<O, V> getter,
				BiConsumer<O, V> setter, BinaryOperator<V> merge, Predicate<V> empty) {
			this.name = name;
			this.type = type;
			this.formatting = formatting;
			this.getter = getter;
			this.setter = setter;
			this.merge = merge;
			this.empty = empty;
		}

		/** The member's local name in the schema, e.g. {@code rFonts}. */
		public String name() { return name; }

		/** The member's Java type. */
		public Class<V> type() { return type; }

		/** Whether the member formats the content (as opposed to naming a style, recording a revision, or caching a computed condition). */
		public boolean isFormatting() { return formatting; }

		public V get(O owner) { return getter.apply(owner); }

		public void set(O owner, V value) { setter.accept(owner, value); }

		/** The value that results from applying {@code source} (the more specific) over {@code destination} (the inherited). */
		public V merge(V source, V destination) { return merge.apply(source, destination); }

		/** Whether the value says nothing (null, or an element with none of its attributes set). */
		public boolean isEmpty(V value) { return empty.test(value); }

		@Override
		public String toString() { return name; }
	}

	// ---------------------------------------------------------------- generic operations

	/** Whether every member of {@code owner} is empty. */
	public static <O> boolean isEmpty(List<? extends Property<O, ?>> catalogue, O owner) {
		if (owner == null) return true;
		for (Property<O, ?> p : catalogue) {
			if (!isEmpty(p, owner)) return false;
		}
		return true;
	}

	private static <O, V> boolean isEmpty(Property<O, V> p, O owner) {
		return p.isEmpty(p.get(owner));
	}

	/** Whether any formatting member of {@code owner} is present (an element that is there, even with no attributes, counts: it was stated). */
	public static <O> boolean hasDirectFormatting(List<? extends Property<O, ?>> catalogue, O owner) {
		if (owner == null) return false;
		for (Property<O, ?> p : catalogue) {
			if (p.isFormatting() && p.get(owner) != null) return true;
		}
		return false;
	}

	/** Apply every member of {@code source} over {@code destination}, in place. */
	public static <O> void apply(List<? extends Property<O, ?>> catalogue, O source, O destination) {
		apply(catalogue, source, destination, Collections.<String>emptySet());
	}

	/** Apply every member of {@code source} over {@code destination} except those named. */
	public static <O> void apply(List<? extends Property<O, ?>> catalogue, O source, O destination,
			Set<String> except) {
		for (Property<O, ?> p : catalogue) {
			if (!except.contains(p.name())) apply(p, source, destination);
		}
	}

	/** Apply one member of {@code source} over {@code destination}. */
	public static <O, V> void apply(Property<O, V> p, O source, O destination) {
		p.set(destination, p.merge(p.get(source), p.get(destination)));
	}

	/** Unset in {@code destination} every member that {@code source} states. */
	public static <O> void unset(List<? extends Property<O, ?>> catalogue, O source, O destination) {
		for (Property<O, ?> p : catalogue) {
			unset(p, source, destination);
		}
	}

	private static <O, V> void unset(Property<O, V> p, O source, O destination) {
		if (p.get(source) != null) p.set(destination, null);
	}

	/** The member of that name, or null. */
	public static <O> Property<O, ?> named(List<? extends Property<O, ?>> catalogue, String name) {
		for (Property<O, ?> p : catalogue) {
			if (p.name().equals(name)) return p;
		}
		return null;
	}

	// ---------------------------------------------------------------- merges

	/** A copy of the source's value where it has one, else the inherited: for members with no per-attribute rule. */
	private static <V> BinaryOperator<V> replace() {
		return (source, destination) -> source == null ? destination : org.docx4j.XmlUtils.deepCopy(source);
	}

	private static <V> Predicate<V> isNull() {
		return Objects::isNull;
	}

	// ---------------------------------------------------------------- runs

	/** A run member, with accessors for both {@link RPr} and {@link ParaRPr}. */
	private static <V> Property<Object, V> run(String name, Class<V> type, boolean formatting,
			Function<RPr, V> rGet, BiConsumer<RPr, V> rSet,
			Function<ParaRPr, V> pGet, BiConsumer<ParaRPr, V> pSet,
			BinaryOperator<V> merge, Predicate<V> empty) {
		Function<Object, V> getter = owner -> owner instanceof RPr ? rGet.apply((RPr) owner)
				: pGet.apply(runOwner(owner));
		BiConsumer<Object, V> setter = (owner, value) -> {
			if (owner instanceof RPr) rSet.accept((RPr) owner, value);
			else pSet.accept(runOwner(owner), value);
		};
		return new Property<Object, V>(name, type, formatting, getter, setter, merge, empty);
	}

	private static ParaRPr runOwner(Object owner) {
		if (owner instanceof ParaRPr) return (ParaRPr) owner;
		throw new IllegalArgumentException("Not a w:rPr: "
				+ (owner == null ? "null" : owner.getClass().getName()));
	}

	/**
	 * The members of {@code w:rPr} (17.3.2) and of the paragraph mark's {@code w:pPr/w:rPr},
	 * in schema order, plus the w14 extensions the generated classes carry.  Not listed:
	 * {@code w:rPrChange} and the paragraph mark's {@code w:ins}/{@code w:del}/{@code w:moveFrom}/
	 * {@code w:moveTo}, which record revisions and are never inherited.
	 */
	public static final List<Property<Object, ?>> RUN;

	static {
		List<Property<Object, ?>> l = new ArrayList<Property<Object, ?>>();
		l.add(run("rStyle", RStyle.class, false, RPr::getRStyle, RPr::setRStyle, ParaRPr::getRStyle, ParaRPr::setRStyle, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("rFonts", RFonts.class, true, RPr::getRFonts, RPr::setRFonts, ParaRPr::getRFonts, ParaRPr::setRFonts, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(bool("b", RPr::getB, RPr::setB, ParaRPr::getB, ParaRPr::setB));
		l.add(bool("bCs", RPr::getBCs, RPr::setBCs, ParaRPr::getBCs, ParaRPr::setBCs));
		l.add(bool("i", RPr::getI, RPr::setI, ParaRPr::getI, ParaRPr::setI));
		l.add(bool("iCs", RPr::getICs, RPr::setICs, ParaRPr::getICs, ParaRPr::setICs));
		l.add(bool("caps", RPr::getCaps, RPr::setCaps, ParaRPr::getCaps, ParaRPr::setCaps));
		l.add(bool("smallCaps", RPr::getSmallCaps, RPr::setSmallCaps, ParaRPr::getSmallCaps, ParaRPr::setSmallCaps));
		l.add(bool("strike", RPr::getStrike, RPr::setStrike, ParaRPr::getStrike, ParaRPr::setStrike));
		l.add(bool("dstrike", RPr::getDstrike, RPr::setDstrike, ParaRPr::getDstrike, ParaRPr::setDstrike));
		l.add(bool("outline", RPr::getOutline, RPr::setOutline, ParaRPr::getOutline, ParaRPr::setOutline));
		l.add(bool("shadow", RPr::getShadow, RPr::setShadow, ParaRPr::getShadow, ParaRPr::setShadow));
		l.add(bool("emboss", RPr::getEmboss, RPr::setEmboss, ParaRPr::getEmboss, ParaRPr::setEmboss));
		l.add(bool("imprint", RPr::getImprint, RPr::setImprint, ParaRPr::getImprint, ParaRPr::setImprint));
		l.add(bool("noProof", RPr::getNoProof, RPr::setNoProof, ParaRPr::getNoProof, ParaRPr::setNoProof));
		l.add(bool("snapToGrid", RPr::getSnapToGrid, RPr::setSnapToGrid, ParaRPr::getSnapToGrid, ParaRPr::setSnapToGrid));
		l.add(bool("vanish", RPr::getVanish, RPr::setVanish, ParaRPr::getVanish, ParaRPr::setVanish));
		l.add(bool("webHidden", RPr::getWebHidden, RPr::setWebHidden, ParaRPr::getWebHidden, ParaRPr::setWebHidden));
		l.add(run("color", Color.class, true, RPr::getColor, RPr::setColor, ParaRPr::getColor, ParaRPr::setColor, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("spacing", CTSignedTwipsMeasure.class, true, RPr::getSpacing, RPr::setSpacing, ParaRPr::getSpacing, ParaRPr::setSpacing, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("w", CTTextScale.class, true, RPr::getW, RPr::setW, ParaRPr::getW, ParaRPr::setW, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("kern", HpsMeasure.class, true, RPr::getKern, RPr::setKern, ParaRPr::getKern, ParaRPr::setKern, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("position", CTSignedHpsMeasure.class, true, RPr::getPosition, RPr::setPosition, ParaRPr::getPosition, ParaRPr::setPosition, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("sz", HpsMeasure.class, true, RPr::getSz, RPr::setSz, ParaRPr::getSz, ParaRPr::setSz, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("szCs", HpsMeasure.class, true, RPr::getSzCs, RPr::setSzCs, ParaRPr::getSzCs, ParaRPr::setSzCs, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("highlight", Highlight.class, true, RPr::getHighlight, RPr::setHighlight, ParaRPr::getHighlight, ParaRPr::setHighlight, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("u", U.class, true, RPr::getU, RPr::setU, ParaRPr::getU, ParaRPr::setU, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("effect", CTTextEffect.class, true, RPr::getEffect, RPr::setEffect, ParaRPr::getEffect, ParaRPr::setEffect, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("bdr", CTBorder.class, true, RPr::getBdr, RPr::setBdr, ParaRPr::getBdr, ParaRPr::setBdr, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("shd", CTShd.class, true, RPr::getShd, RPr::setShd, ParaRPr::getShd, ParaRPr::setShd, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("fitText", CTFitText.class, true, RPr::getFitText, RPr::setFitText, ParaRPr::getFitText, ParaRPr::setFitText, replace(), isNull()));
		l.add(run("vertAlign", CTVerticalAlignRun.class, true, RPr::getVertAlign, RPr::setVertAlign, ParaRPr::getVertAlign, ParaRPr::setVertAlign, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(bool("rtl", RPr::getRtl, RPr::setRtl, ParaRPr::getRtl, ParaRPr::setRtl));
		l.add(bool("cs", RPr::getCs, RPr::setCs, ParaRPr::getCs, ParaRPr::setCs));
		l.add(run("em", CTEm.class, true, RPr::getEm, RPr::setEm, ParaRPr::getEm, ParaRPr::setEm, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("lang", CTLanguage.class, true, RPr::getLang, RPr::setLang, ParaRPr::getLang, ParaRPr::setLang, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(run("eastAsianLayout", CTEastAsianLayout.class, true, RPr::getEastAsianLayout, RPr::setEastAsianLayout, ParaRPr::getEastAsianLayout, ParaRPr::setEastAsianLayout, replace(), isNull()));
		l.add(bool("specVanish", RPr::getSpecVanish, RPr::setSpecVanish, ParaRPr::getSpecVanish, ParaRPr::setSpecVanish));
		l.add(bool("oMath", RPr::getOMath, RPr::setOMath, ParaRPr::getOMath, ParaRPr::setOMath));
		// w14 (Office 2010 text effects and OpenType features)
		l.add(run("glow", org.docx4j.w14.CTGlow.class, true, RPr::getGlow, RPr::setGlow, ParaRPr::getGlow, ParaRPr::setGlow, replace(), isNull()));
		l.add(run("shadow14", org.docx4j.w14.CTShadow.class, true, RPr::getShadow14, RPr::setShadow14, ParaRPr::getShadow14, ParaRPr::setShadow14, replace(), isNull()));
		l.add(run("reflection", org.docx4j.w14.CTReflection.class, true, RPr::getReflection, RPr::setReflection, ParaRPr::getReflection, ParaRPr::setReflection, replace(), isNull()));
		l.add(run("textOutline", org.docx4j.w14.CTTextOutlineEffect.class, true, RPr::getTextOutline, RPr::setTextOutline, ParaRPr::getTextOutline, ParaRPr::setTextOutline, replace(), isNull()));
		l.add(run("textFill", org.docx4j.w14.CTFillTextEffect.class, true, RPr::getTextFill, RPr::setTextFill, ParaRPr::getTextFill, ParaRPr::setTextFill, replace(), isNull()));
		l.add(run("scene3d", org.docx4j.w14.CTScene3D.class, true, RPr::getScene3D, RPr::setScene3D, ParaRPr::getScene3D, ParaRPr::setScene3D, replace(), isNull()));
		l.add(run("props3d", org.docx4j.w14.CTProps3D.class, true, RPr::getProps3D, RPr::setProps3D, ParaRPr::getProps3D, ParaRPr::setProps3D, replace(), isNull()));
		l.add(run("ligatures", org.docx4j.w14.CTLigatures.class, true, RPr::getLigatures, RPr::setLigatures, ParaRPr::getLigatures, ParaRPr::setLigatures, replace(), isNull()));
		l.add(run("numForm", org.docx4j.w14.CTNumForm.class, true, RPr::getNumForm, RPr::setNumForm, ParaRPr::getNumForm, ParaRPr::setNumForm, replace(), isNull()));
		l.add(run("numSpacing", org.docx4j.w14.CTNumSpacing.class, true, RPr::getNumSpacing, RPr::setNumSpacing, ParaRPr::getNumSpacing, ParaRPr::setNumSpacing, replace(), isNull()));
		l.add(run("stylisticSets", org.docx4j.w14.CTStylisticSets.class, true, RPr::getStylisticSets, RPr::setStylisticSets, ParaRPr::getStylisticSets, ParaRPr::setStylisticSets, replace(), isNull()));
		l.add(run("cntxtAlts", org.docx4j.w14.CTOnOff.class, true, RPr::getCntxtAlts, RPr::setCntxtAlts, ParaRPr::getCntxtAlts, ParaRPr::setCntxtAlts, replace(), isNull()));
		RUN = Collections.unmodifiableList(l);
	}

	private static Property<Object, BooleanDefaultTrue> bool(String name,
			Function<RPr, BooleanDefaultTrue> rGet, BiConsumer<RPr, BooleanDefaultTrue> rSet,
			Function<ParaRPr, BooleanDefaultTrue> pGet, BiConsumer<ParaRPr, BooleanDefaultTrue> pSet) {
		return run(name, BooleanDefaultTrue.class, true, rGet, rSet, pGet, pSet, StyleUtil::apply, StyleUtil::isEmpty);
	}

	// ---------------------------------------------------------------- paragraphs

	private static <V> Property<PPrBase, V> para(String name, Class<V> type, boolean formatting,
			Function<PPrBase, V> getter, BiConsumer<PPrBase, V> setter,
			BinaryOperator<V> merge, Predicate<V> empty) {
		return new Property<PPrBase, V>(name, type, formatting, getter, setter, merge, empty);
	}

	private static Property<PPrBase, BooleanDefaultTrue> pbool(String name,
			Function<PPrBase, BooleanDefaultTrue> getter, BiConsumer<PPrBase, BooleanDefaultTrue> setter) {
		return para(name, BooleanDefaultTrue.class, true, getter, setter, StyleUtil::apply, StyleUtil::isEmpty);
	}

	/** The name of the {@code w:ind} member, which {@link StyleUtil#apply(PPrBase, PPrBase, org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart)} applies last, after the numbering level's. */
	public static final String IND = "ind";

	/**
	 * The members of {@code w:pPr} shared with a style's {@code w:pPr} ({@link PPrBase},
	 * 17.3.1), in schema order.  Not formatting: {@code w:pStyle} (the style reference),
	 * {@code w:divId} (an HTML import artefact), {@code w:cnfStyle} (Word's cache of the
	 * table conditions that apply) and {@code w14:collapsed} (the outline view's state).
	 * A {@code w:pPr}'s own {@code w:rPr} (the paragraph mark) and {@code w:sectPr} are
	 * outside {@code PPrBase} and outside this list.
	 */
	public static final List<Property<PPrBase, ?>> PARAGRAPH;

	static {
		List<Property<PPrBase, ?>> l = new ArrayList<Property<PPrBase, ?>>();
		l.add(para("pStyle", PPrBase.PStyle.class, false, PPrBase::getPStyle, PPrBase::setPStyle, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(pbool("keepNext", PPrBase::getKeepNext, PPrBase::setKeepNext));
		l.add(pbool("keepLines", PPrBase::getKeepLines, PPrBase::setKeepLines));
		l.add(pbool("pageBreakBefore", PPrBase::getPageBreakBefore, PPrBase::setPageBreakBefore));
		l.add(para("framePr", CTFramePr.class, true, PPrBase::getFramePr, PPrBase::setFramePr, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(pbool("widowControl", PPrBase::getWidowControl, PPrBase::setWidowControl));
		l.add(para("numPr", PPrBase.NumPr.class, true, PPrBase::getNumPr, PPrBase::setNumPr, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(pbool("suppressLineNumbers", PPrBase::getSuppressLineNumbers, PPrBase::setSuppressLineNumbers));
		l.add(para("pBdr", PPrBase.PBdr.class, true, PPrBase::getPBdr, PPrBase::setPBdr, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("shd", CTShd.class, true, PPrBase::getShd, PPrBase::setShd, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("tabs", Tabs.class, true, PPrBase::getTabs, PPrBase::setTabs, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(pbool("suppressAutoHyphens", PPrBase::getSuppressAutoHyphens, PPrBase::setSuppressAutoHyphens));
		l.add(pbool("kinsoku", PPrBase::getKinsoku, PPrBase::setKinsoku));
		l.add(pbool("wordWrap", PPrBase::getWordWrap, PPrBase::setWordWrap));
		l.add(pbool("overflowPunct", PPrBase::getOverflowPunct, PPrBase::setOverflowPunct));
		l.add(pbool("topLinePunct", PPrBase::getTopLinePunct, PPrBase::setTopLinePunct));
		l.add(pbool("autoSpaceDE", PPrBase::getAutoSpaceDE, PPrBase::setAutoSpaceDE));
		l.add(pbool("autoSpaceDN", PPrBase::getAutoSpaceDN, PPrBase::setAutoSpaceDN));
		l.add(pbool("bidi", PPrBase::getBidi, PPrBase::setBidi));
		l.add(pbool("adjustRightInd", PPrBase::getAdjustRightInd, PPrBase::setAdjustRightInd));
		l.add(pbool("snapToGrid", PPrBase::getSnapToGrid, PPrBase::setSnapToGrid));
		l.add(para("spacing", PPrBase.Spacing.class, true, PPrBase::getSpacing, PPrBase::setSpacing, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para(IND, PPrBase.Ind.class, true, PPrBase::getInd, PPrBase::setInd, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(pbool("contextualSpacing", PPrBase::getContextualSpacing, PPrBase::setContextualSpacing));
		l.add(pbool("mirrorIndents", PPrBase::getMirrorIndents, PPrBase::setMirrorIndents));
		l.add(pbool("suppressOverlap", PPrBase::getSuppressOverlap, PPrBase::setSuppressOverlap));
		l.add(para("jc", Jc.class, true, PPrBase::getJc, PPrBase::setJc, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("textDirection", TextDirection.class, true, PPrBase::getTextDirection, PPrBase::setTextDirection, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("textAlignment", PPrBase.TextAlignment.class, true, PPrBase::getTextAlignment, PPrBase::setTextAlignment, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("textboxTightWrap", CTTextboxTightWrap.class, true, PPrBase::getTextboxTightWrap, PPrBase::setTextboxTightWrap, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("outlineLvl", PPrBase.OutlineLvl.class, true, PPrBase::getOutlineLvl, PPrBase::setOutlineLvl, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("divId", PPrBase.DivId.class, false, PPrBase::getDivId, PPrBase::setDivId, replace(), isNull()));
		l.add(para("cnfStyle", CTCnf.class, false, PPrBase::getCnfStyle, PPrBase::setCnfStyle, StyleUtil::apply, StyleUtil::isEmpty));
		l.add(para("collapsed", BooleanDefaultTrue.class, false, PPrBase::getCollapsed, PPrBase::setCollapsed, StyleUtil::apply, StyleUtil::isEmpty));
		PARAGRAPH = Collections.unmodifiableList(l);
	}
}
