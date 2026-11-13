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
package org.docx4j.model.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTTextBodyProperties;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.color.WordColors;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.Jc;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DrawingML text box drawn as WordArt (CR-083): its paragraphs and runs, read with their effective properties, for
 * docx4j-chart-render's WordArt renderer to lay out and draw, in the PDF and HTML outputs alike.
 * <p>
 * Which text boxes ({@link #of}): one whose {@code wps:bodyPr} has an {@code a:prstTxWarp} other than {@code
 * textNoShape} (a warp); one with a run Word draws as glyph outlines ({@link Paint#PATHS}: a {@code w14:textOutline}
 * with a fill, a gradient {@code w14:textFill}, a solid one with transparency: CR-083 section 5.1, D2); and one in
 * Word's own WordArt form, {@code a:prstTxWarp prst="textNoShape"} with {@code wrap="none"}, whose box Word sizes to its
 * text (phase 0, findings section 2), where the FO output would wrap it in its stored width (phase 1). A text box
 * holding anything but paragraphs of text (a table, a picture, a field's code is fine) stays text, reported
 * ({@link #CONTENT}).
 *
 * @since 17.3.7
 */
public final class WordArtText {

	private static final Logger log = LoggerFactory.getLogger(WordArtText.class);

	/** A WordArt text box holding what the line layout doesn't lay out (a table, a picture, a symbol): left as text */
	public static final String CONTENT = "render.wordart.content";

	/** A run's shadow, glow or reflection that couldn't be drawn as Word draws it (CR-083 phase 3) */
	public static final String EFFECT = "render.wordart.effect";

	/** 3-D WordArt ({@code w14:props3d}, {@code w14:scene3d}, the bodyPr's {@code a:scene3d} or {@code a:sp3d}), drawn flat (D10) */
	public static final String FLAT_3D = "render.wordart.flat_3d";

	/** A warp docx4j has no definition of: the text drawn unwarped */
	public static final String UNKNOWN_PRESET = "render.wordart.unknown_preset";

	/** A run whose face has no font file docx4j can outline: drawn in AWT's fallback face */
	public static final String FONT = "render.wordart.font";

	/**
	 * A run's underline, strike, highlight or shading on warped WordArt, not drawn: drawn on flat WordArt as the FO
	 * output draws them; how Word warps them is not measured (phase 3's round)
	 */
	public static final String DECORATION = "render.wordart.decoration";

	/** A warp whose outlines would pass {@code WordArtRenderer}'s limit on points: the text drawn unwarped */
	public static final String OVER_LIMIT = "render.wordart.over_limit";

	/** A run's fill or colour that couldn't be resolved or drawn (a gradient with no stops, a colour that doesn't resolve) */
	public static final String FILL = "render.wordart.fill";

	/** A body run's text effect not drawn as Word draws it (CR-083 phase 5, D9, D23: in HTML, what CSS can't carry) */
	public static final String BODY_EFFECT = "render.wordart.body_effect";

	/** A run's outline that couldn't be drawn */
	public static final String OUTLINE = "render.wordart.outline";

	/** How Word's PDF draws a run (CR-083 D2, phase 0 findings section 1) */
	public enum Paint {
		/** real text, in its fill's colour */
		TEXT,
		/** the glyphs as filled and stroked paths, the text again invisibly */
		PATHS,
		/** nothing: no fill and no outline */
		NONE
	}

	/** A run of text of one formatting; a line break is a run of its own ({@link #isBreak()}) */
	public static final class Run {
		String text;
		boolean lineBreak;
		String face;
		boolean bold, italic, caps, smallCaps, kerned;
		double size, spacing;
		ResolvedColor colour;
		Object fill;
		CTLineProperties outline;
		boolean shadow, glow, reflection, threeD, textFill, alpha;
		org.docx4j.dml.CTEffectList effects;
		String lang;
		RPr rPr;
		/** the paragraph mark of a paragraph with no runs: no text, its line's height */
		boolean mark;
		org.docx4j.wml.UnderlineEnumeration underline;
		ResolvedColor underlineColour, highlight, shading;
		boolean strike, doubleStrike;
		/** a superscript's raise or a subscript's drop (negative), in points of the unreduced size */
		double rise;
		/** Word's ascent and descent, and its single line, as fractions of the size (WordLineMetrics) */
		double ascent = 0.8, descent = 0.2, single = 1.2;
		/** the font's external leading, which Word puts above the ascent, as a fraction of the size */
		double leading;
		/** a VML WordArt's: stretched to its shape's box, so its effects take no room in its line (CR-083 phase 4) */
		boolean fitted;
		/** a VML WordArt's: the descent of the em box its text path maps without trim, as a fraction of the size */
		double emDescent;

		/** Word's ascent for its font, as a fraction of its size: its single line's share above the baseline */
		public double getAscent() {
			return ascent;
		}

		/** Word's descent, as a fraction of its size */
		public double getDescent() {
			return descent;
		}

		/**
		 * The font's external leading (its line gap), as a fraction of its size: Word puts it above the ascent, so that the
		 * baseline is the ascent and the leading below the line's top (WordLineMetrics.wordBaselinePt; phase 3 findings
		 * section 4: Times New Roman 36 pt WordArt sits 0.24 above Aptos, not 1.7)
		 *
		 * @since 17.3.7 (CR-083 phase 3)
		 */
		public double getLeading() {
			return leading;
		}

		/** Word's single line height, as a multiple of its size */
		public double getSingle() {
			return single;
		}

		/** The characters, as shown (in capitals where its caps says); empty for a break */
		public String getText() {
			return text;
		}

		/** A line break ({@code w:br}, {@code w:cr}) */
		public boolean isBreak() {
			return lineBreak;
		}

		/** The face the document names (Aptos), before docx4j's font mapping */
		public String getFace() {
			return face;
		}

		public boolean isBold() {
			return bold;
		}

		public boolean isItalic() {
			return italic;
		}

		/** Small capitals: the lower case drawn as capitals at 80% (not yet: drawn as written) */
		public boolean isSmallCaps() {
			return smallCaps;
		}

		/** Kerned by the font's pairs */
		public boolean isKerned() {
			return kerned;
		}

		/** In points */
		public double getSize() {
			return size;
		}

		/** The space after each character ({@code w:spacing}), in points */
		public double getSpacing() {
			return spacing;
		}

		/** {@code w:color} as Word resolves it; black where it is automatic */
		public ResolvedColor getColour() {
			return colour;
		}

		/**
		 * Its fill as DrawingML ({@code w14:textFill} converted, {@link TextEffects}): a CTNoFillProperties,
		 * CTSolidColorFillProperties or CTGradientFillProperties; null where it has none (its {@link #getColour()})
		 */
		public Object getFill() {
			return fill;
		}

		/** Its outline as DrawingML's {@code a:ln} ({@code w14:textOutline} converted); null where it has none */
		public CTLineProperties getOutline() {
			return outline;
		}

		/** It has a shadow, glow or reflection (drawn in phase 3) */
		public boolean hasEffect() {
			return shadow || glow || reflection;
		}

		/**
		 * Its shadow, glow and reflection as DrawingML's {@code a:effectLst} ({@link TextEffects#effects(RPr)}), their
		 * lengths as written; null where it has none
		 *
		 * @since 17.3.7 (CR-083 phase 3)
		 */
		public org.docx4j.dml.CTEffectList getEffects() {
			return effects;
		}

		/** It has 3-D ({@code w14:props3d} or {@code w14:scene3d}) */
		public boolean isThreeD() {
			return threeD;
		}

		/**
		 * A VML WordArt's run, stretched to its shape's box (its text path's {@code fitshape}): its effects take no room
		 * in its line, the box being fixed
		 *
		 * @since 17.3.7 (CR-083 phase 4)
		 */
		public boolean isFitted() {
			return fitted;
		}

		/**
		 * A VML WordArt's: how far below the baseline the em box its text path maps without {@code trim} reaches, as a
		 * fraction of the size: the larger of the face's usWinDescent and its hhea descender (cr083-phase4's wv44: Aptos
		 * 0.282, Calibri 0.269, Times New Roman 0.216); 0 for a DrawingML run
		 *
		 * @since 17.3.7 (CR-083 phase 4)
		 */
		public double getEmDescent() {
			return emDescent;
		}

		/**
		 * The paragraph mark of a paragraph with no runs: no text, but its line is as high as the mark's size, as Word
		 * lays an empty paragraph out
		 */
		public boolean isMark() {
			return mark;
		}

		/** Its underline ({@code w:u}), or null for none; drawn as the FO output draws it, a single line of any kind but double */
		public org.docx4j.wml.UnderlineEnumeration getUnderline() {
			return underline;
		}

		/** The underline's colour; null for automatic (the text's) */
		public ResolvedColor getUnderlineColour() {
			return underlineColour;
		}

		/** Struck through once ({@code w:strike}) */
		public boolean isStrike() {
			return strike;
		}

		/** Struck through twice ({@code w:dstrike}) */
		public boolean isDoubleStrike() {
			return doubleStrike;
		}

		/** Its highlight ({@code w:highlight}), or null */
		public ResolvedColor getHighlight() {
			return highlight;
		}

		/** Its shading's fill ({@code w:shd}), or null */
		public ResolvedColor getShading() {
			return shading;
		}

		/**
		 * A superscript's raise, or a subscript's drop (negative), in points: of the unreduced size, as the FO output has
		 * them ({@link org.docx4j.model.properties.run.VerticalAlignment}); its {@link #getSize()} is already reduced
		 */
		public double getRise() {
			return rise;
		}

		/** It has an underline, a strike, a highlight or a shading: drawn on flat WordArt, reported on warped */
		public boolean hasDecoration() {
			return underline != null || strike || doubleStrike || highlight != null || shading != null;
		}

		/** Its language ({@code w:lang}), or null */
		public String getLang() {
			return lang;
		}

		/** Its effective properties */
		public RPr getRPr() {
			return rPr;
		}

		/** How Word's PDF draws it, warped or not (D2) */
		public Paint paint(boolean warped) {
			boolean noFill = fill instanceof CTNoFillProperties;
			boolean outlined = outline != null && outline.getNoFill() == null
					&& (outline.getSolidFill() != null || outline.getGradFill() != null || outline.getPattFill() != null);
			if (noFill && !outlined) return Paint.NONE;
			if (warped || outlined || fill instanceof CTGradientFillProperties || alpha) return Paint.PATHS;
			return Paint.TEXT;
		}
	}

	/** A paragraph: its alignment, indents and spacing, in points, and its runs */
	public static final class Paragraph {
		String align = "left";
		double indLeft, indRight, indFirst, spaceBefore, spaceAfter;
		/** auto (a multiple, {@link #line} in 240ths), exact or atLeast ({@link #line} in points) */
		String lineRule = "auto";
		double line = 240;
		final List<Run> runs = new ArrayList<Run>();

		/** left, center, right, both or distribute */
		public String getAlign() {
			return align;
		}

		public double getIndLeft() {
			return indLeft;
		}

		public double getIndRight() {
			return indRight;
		}

		/** The first line's indent, negative for a hanging one */
		public double getIndFirst() {
			return indFirst;
		}

		public double getSpaceBefore() {
			return spaceBefore;
		}

		public double getSpaceAfter() {
			return spaceAfter;
		}

		/** auto, exact or atLeast */
		public String getLineRule() {
			return lineRule;
		}

		/** For auto, 240ths of a line; for exact and atLeast, points */
		public double getLine() {
			return line;
		}

		public List<Run> getRuns() {
			return Collections.unmodifiableList(runs);
		}
	}

	private final CTWordprocessingShape shape;
	private final VmlShapes.Resolved vml;
	private final List<Paragraph> paragraphs;
	private final List<String> problems;
	private final boolean warped;

	private WordArtText(CTWordprocessingShape shape, List<Paragraph> paragraphs, List<String> problems, boolean warped) {
		this(shape, null, paragraphs, problems, warped);
	}

	WordArtText(CTWordprocessingShape shape, VmlShapes.Resolved vml, List<Paragraph> paragraphs, List<String> problems,
			boolean warped) {
		this.shape = shape;
		this.vml = vml;
		this.paragraphs = Collections.unmodifiableList(paragraphs);
		this.problems = problems;
		this.warped = warped;
	}

	/** The text box; null for VML WordArt ({@link #getVml()}) */
	public CTWordprocessingShape getShape() {
		return shape;
	}

	/**
	 * The VML shape with its shapetype, for VML WordArt ({@link VmlWordArt#of}); null for a DrawingML text box
	 *
	 * @since 17.3.7 (CR-083 phase 4)
	 */
	public VmlShapes.Resolved getVml() {
		return vml;
	}

	/** Its body properties: insets, wrap, autofit, anchor, warp; null for VML WordArt */
	public CTTextBodyProperties getBodyPr() {
		return shape == null ? null : shape.getBodyPr();
	}

	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}

	/** Its warp's name, or null where it has none or it is {@code textNoShape} */
	public String getWarp() {
		return warped ? shape.getBodyPr().getPrstTxWarp().getPrst().value() : null;
	}

	/**
	 * The gaps found reading it (stable codes, CR-083 D9): {@link #FLAT_3D}; {@link #EFFECT} for a run whose effects
	 * couldn't be read; the renderer adds its own
	 */
	public List<String> getProblems() {
		return problems;
	}

	/**
	 * The text box as WordArt, or null where it isn't drawn as WordArt (it stays text): it isn't one of the kinds the
	 * class comment lists, or it holds something the line layout doesn't lay out ({@link #CONTENT}, logged).
	 */
	public static WordArtText of(CTWordprocessingShape wsp, WordprocessingMLPackage pkg) {
		if (wsp == null || pkg == null || !ShapeText.hasText(wsp)) return null;
		CTTextBodyProperties b = wsp.getBodyPr();
		String warp = b == null || b.getPrstTxWarp() == null || b.getPrstTxWarp().getPrst() == null ? null
				: b.getPrstTxWarp().getPrst().value();
		boolean warped = warp != null && !"textNoShape".equals(warp);
		boolean wordForm = "textNoShape".equals(warp) && b.getWrap() == org.docx4j.dml.STTextWrappingType.NONE;
		// a text box with no warp is WordArt only by a run's effects: none in its runs or its styles, and it is text
		if (!warped && !wordForm && !mayHaveTextEffects(wsp.getTxbx().getTxbxContent(), pkg)) return null;
		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		List<String> problems = new ArrayList<String>();
		String unread;
		try {
			unread = read(wsp.getTxbx().getTxbxContent().getContent(), pkg, paragraphs);
		} catch (RuntimeException e) {
			log.warn("A text box couldn't be read as WordArt: " + e, e);
			return null;
		}
		boolean paths = false, any = false;
		for (Paragraph p : paragraphs) {
			for (Run r : p.runs) {
				if (r.lineBreak || r.mark) continue;
				any = true;
				if (r.paint(false) != Paint.TEXT) paths = true;
				if (r.threeD && !problems.contains(FLAT_3D)) problems.add(FLAT_3D);
				// a shadow, glow or reflection none of which could be read: nothing drawn, its room not taken
				if (r.hasEffect() && r.effects == null && !problems.contains(EFFECT)) problems.add(EFFECT);
			}
		}
		if (!(warped || wordForm || paths) || !any) return null;
		if (unread != null) {
			log.warn(CONTENT + ": WordArt holding " + unread + " is left as text (CR-083 D9)");
			return null;
		}
		if (b != null && (b.getScene3D() != null && !is2d(b.getScene3D()) || b.getSp3D() != null) && !problems.contains(FLAT_3D)) {
			problems.add(FLAT_3D);
		}
		return new WordArtText(wsp, paragraphs, problems, warped);
	}

	/**
	 * Whether a run of the text box may have a fill or an outline Word draws as glyph outlines: one of its runs states
	 * one directly, or the document's styles define one (cheap, so that an ordinary text box isn't read run by run).
	 * The styles are read each time, not cached: a style changed in memory between conversions counts (one pass over
	 * the styles a text box; CR-083 phases 1 and 2 review).
	 */
	private static boolean mayHaveTextEffects(org.docx4j.wml.CTTxbxContent content, WordprocessingMLPackage pkg) {
		final boolean[] found = { false };
		new org.docx4j.TraversalUtil(content.getContent(), new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R) {
					RPr r = ((R) o).getRPr();
					if (r != null && (r.getTextFill() != null || r.getTextOutline() != null)) found[0] = true;
				}
				return null;
			}

			@Override
			public boolean shouldTraverse(Object o) {
				return !found[0];
			}
		});
		if (found[0]) return true;
		org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart sdp = pkg.getMainDocumentPart().getStyleDefinitionsPart();
		if (sdp == null) return false;
		org.docx4j.wml.Styles styles;
		try {
			styles = sdp.getContents();
		} catch (Exception e) {
			return true;
		}
		if (styles == null) return false;
		if (styles.getDocDefaults() != null && styles.getDocDefaults().getRPrDefault() != null) {
			RPr r = styles.getDocDefaults().getRPrDefault().getRPr();
			if (r != null && (r.getTextFill() != null || r.getTextOutline() != null)) return true;
		}
		for (org.docx4j.wml.Style st : styles.getStyle()) {
			RPr r = st.getRPr();
			if (r != null && (r.getTextFill() != null || r.getTextOutline() != null)) return true;
			org.docx4j.wml.PPr pp = st.getPPr();
			if (pp != null && pp.getRPr() != null && (pp.getRPr().getTextFill() != null || pp.getRPr().getTextOutline() != null)) return true;
		}
		return false;
	}

	/** A bodyPr scene3d that is only Word's flat default (an orthographic front camera, no turn) */
	private static boolean is2d(org.docx4j.dml.CTScene3D s) {
		return s.getCamera() == null || s.getCamera().getPrst() == org.docx4j.dml.STPresetCameraType.ORTHOGRAPHIC_FRONT
				&& s.getCamera().getRot() == null;
	}

	// ------------------------------------------------------------------ reading

	/** Reads the text box's paragraphs; the first thing it can't lay out, or null */
	private static String read(List<Object> content, WordprocessingMLPackage pkg, List<Paragraph> out) {
		Reader r = new Reader(pkg);
		return r.blocks(content, out);
	}

	/**
	 * A body run's paint and effects from its effective {@code w:rPr}: its fill, outline, shadow, glow and reflection,
	 * its colour, size, face and Word's line metrics for the face, as a WordArt run of that formatting (CR-083 phase 5:
	 * the FO renderer fork's text-effect painter draws a body run as phases 1 to 3 draw a flat WordArt run).
	 *
	 * @param rPr the run's effective properties (the paragraph mark's for an empty paragraph or a number)
	 * @return null where the package can't be read
	 * @since 17.3.7
	 */
	public static Run bodyRun(WordprocessingMLPackage pkg, RPr rPr) {
		try {
			return new Reader(pkg).run(rPr, "");
		} catch (RuntimeException e) {
			log.warn("A run's text effects couldn't be read: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Whether Word draws more of a body run than its text in its colour (CR-083 phase 5): a fill other than solid and
	 * opaque (a gradient, a transparent colour, none), a visible outline, a shadow, a glow or a reflection. 3-D alone is
	 * phase 6's.
	 *
	 * @since 17.3.7
	 */
	public static boolean hasBodyEffect(RPr rPr) {
		if (rPr == null) return false;
		if (rPr.getGlow() != null || rPr.getShadow14() != null || rPr.getReflection() != null) return true;
		if (rPr.getTextOutline() != null) {
			org.docx4j.w14.CTTextOutlineEffect o = rPr.getTextOutline();
			if (o.getNoFill() == null && (o.getSolidFill() != null || o.getGradFill() != null)) return true;
		}
		org.docx4j.w14.CTFillTextEffect f = rPr.getTextFill();
		if (f != null) {
			if (f.getNoFill() != null || f.getGradFill() != null) return true;
			Object dml = TextEffects.fill(f);
			if (dml instanceof CTSolidColorFillProperties && TextEffects.hasAlpha((CTSolidColorFillProperties) dml)) return true;
		}
		return false;
	}

	private static final class Reader {
		final WordprocessingMLPackage pkg;
		final PropertyResolver resolver;
		final ThemeColors theme;
		final ThemePart themePart;
		final CTLanguage themeFontLang;

		Reader(WordprocessingMLPackage pkg) {
			this.pkg = pkg;
			PropertyResolver pr = null;
			try {
				pr = pkg.getMainDocumentPart().getPropertyResolver();
			} catch (Exception e) {
				throw new IllegalStateException("no property resolver: " + e.getMessage(), e);
			}
			this.resolver = pr;
			this.theme = ThemeColors.of(pkg);
			this.themePart = pkg.getMainDocumentPart().getThemePart();
			CTLanguage lang = null;
			try {
				if (pkg.getMainDocumentPart().getDocumentSettingsPart() != null) {
					lang = pkg.getMainDocumentPart().getDocumentSettingsPart().getContents().getThemeFontLang();
				}
			} catch (Exception e) {
				// no settings: the theme's Latin faces
			}
			this.themeFontLang = lang;
		}

		String blocks(List<Object> content, List<Paragraph> out) {
			for (Object o : content) {
				Object v = o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o;
				if (v instanceof P) {
					String bad = paragraph((P) v, out);
					if (bad != null) return bad;
				} else if (v instanceof SdtBlock) {
					SdtBlock sdt = (SdtBlock) v;
					if (sdt.getSdtContent() != null) {
						String bad = blocks(sdt.getSdtContent().getContent(), out);
						if (bad != null) return bad;
					}
				} else if (!ignored(v)) {
					return name(v);
				}
			}
			return null;
		}

		String paragraph(P p, List<Paragraph> out) {
			Paragraph para = new Paragraph();
			PPr pPr;
			try {
				pPr = resolver.getEffectivePPr(p.getPPr());
			} catch (Exception e) {
				pPr = p.getPPr();
			}
			if (pPr != null) {
				Jc jc = pPr.getJc();
				if (jc != null && jc.getVal() != null) {
					switch (jc.getVal()) {
						case CENTER: para.align = "center"; break;
						case RIGHT: case END: para.align = "right"; break;
						case BOTH: case LOW_KASHIDA: case MEDIUM_KASHIDA: case HIGH_KASHIDA: case THAI_DISTRIBUTE: para.align = "both"; break;
						case DISTRIBUTE: para.align = "distribute"; break;
						default: para.align = "left";
					}
				}
				PPrBase.Ind ind = pPr.getInd();
				if (ind != null) {
					para.indLeft = twips(ind.getLeft() != null ? ind.getLeft() : ind.getStart());
					para.indRight = twips(ind.getRight() != null ? ind.getRight() : ind.getEnd());
					if (ind.getHanging() != null) para.indFirst = -twips(ind.getHanging());
					else if (ind.getFirstLine() != null) para.indFirst = twips(ind.getFirstLine());
				}
				PPrBase.Spacing sp = pPr.getSpacing();
				if (sp != null) {
					para.spaceBefore = twips(sp.getBefore());
					para.spaceAfter = twips(sp.getAfter());
					if (sp.getLine() != null) {
						String rule = sp.getLineRule() == null ? "auto" : sp.getLineRule().value();
						para.lineRule = rule;
						para.line = "auto".equals(rule) ? sp.getLine().doubleValue() : sp.getLine().doubleValue() / 20;
					}
				}
			}
			String bad = inline(p.getContent(), p.getPPr(), para);
			if (bad != null) return bad;
			if (para.runs.isEmpty()) {
				// an empty paragraph: a line as high as its mark, as Word lays it out (it was no line at all)
				RPr markRPr;
				try {
					markRPr = resolver.getEffectiveParagraphMarkRPr(p.getPPr());
				} catch (Exception e) {
					markRPr = null;
				}
				Run mark = new Run();
				mark.mark = true;
				mark.text = "";
				mark.rPr = markRPr;
				mark.size = size(markRPr);
				mark.face = face(markRPr);
				metrics(mark);
				para.runs.add(mark);
			}
			out.add(para);
			return null;
		}

		String inline(List<Object> content, PPr pPr, Paragraph para) {
			for (Object o : content) {
				Object v = o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o;
				String bad = null;
				if (v instanceof R) {
					bad = run((R) v, pPr, para);
				} else if (v instanceof P.Hyperlink) {
					bad = inline(((P.Hyperlink) v).getContent(), pPr, para);
				} else if (v instanceof SdtRun) {
					SdtRun sdt = (SdtRun) v;
					if (sdt.getSdtContent() != null) bad = inline(sdt.getSdtContent().getContent(), pPr, para);
				} else if (v instanceof RunIns) {
					bad = inline(((RunIns) v).getCustomXmlOrSmartTagOrSdt(), pPr, para);
				} else if (v instanceof CTSmartTagRun) {
					bad = inline(((CTSmartTagRun) v).getContent(), pPr, para);
				} else if (v instanceof org.docx4j.wml.CTSimpleField) {
					bad = inline(((org.docx4j.wml.CTSimpleField) v).getContent(), pPr, para);
				} else if (v instanceof RunDel || ignored(v)) {
					// deleted text, bookmarks, proofing marks: nothing shown
				} else {
					bad = name(v);
				}
				if (bad != null) return bad;
			}
			return null;
		}

		String run(R r, PPr pPr, Paragraph para) {
			RPr rPr;
			try {
				rPr = resolver.getEffectiveRPr(r.getRPr(), pPr);
			} catch (Exception e) {
				rPr = r.getRPr();
			}
			if (rPr != null && isOn(rPr.getVanish())) return null;
			StringBuilder text = new StringBuilder();
			for (Object o : r.getContent()) {
				String element = o instanceof JAXBElement ? ((JAXBElement<?>) o).getName().getLocalPart() : null;
				Object v = o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o;
				if (v instanceof Text) {
					// w:t; a field's code (w:instrText) and deleted text (w:delText) aren't shown
					String t = ((Text) v).getValue();
					if (t != null && (element == null || "t".equals(element))) text.append(t);
				} else if (v instanceof Br || v instanceof R.Cr) {
					// a page or column break in a text box shows as a line break
					flush(text, rPr, para);
					Run brk = new Run();
					brk.lineBreak = true;
					brk.text = "";
					brk.rPr = rPr;
					brk.size = size(rPr);
					brk.face = face(rPr);
					metrics(brk);
					para.runs.add(brk);
				} else if (v instanceof R.Tab || v instanceof R.Ptab) {
					text.append(' ');
				} else if (v instanceof R.NoBreakHyphen) {
					text.append('‑');
				} else if (v instanceof R.SoftHyphen || v instanceof R.LastRenderedPageBreak || v instanceof org.docx4j.wml.FldChar
						|| v instanceof R.CommentReference || v instanceof org.docx4j.wml.DelText || ignored(v)) {
					// nothing shown: a field's result is the w:t after its separator
				} else {
					return name(v);
				}
			}
			flush(text, rPr, para);
			return null;
		}

		/** The text so far as a run of rPr's formatting */
		void flush(StringBuilder text, RPr rPr, Paragraph para) {
			if (text.length() == 0) return;
			Run run = run(rPr, text.toString());
			text.setLength(0);
			para.runs.add(run);
		}

		/** A run of rPr's formatting and this text */
		Run run(RPr rPr, String text) {
			Run run = new Run();
			run.rPr = rPr;
			run.caps = rPr != null && isOn(rPr.getCaps());
			run.text = run.caps ? text.toUpperCase(java.util.Locale.ROOT) : text;
			run.face = face(rPr);
			run.size = size(rPr);
			metrics(run);
			run.bold = rPr != null && isOn(rPr.getB());
			run.italic = rPr != null && isOn(rPr.getI());
			run.smallCaps = rPr != null && isOn(rPr.getSmallCaps());
			run.spacing = rPr == null || rPr.getSpacing() == null || rPr.getSpacing().getVal() == null ? 0
					: rPr.getSpacing().getVal().doubleValue() / 20;
			run.kerned = rPr != null && rPr.getKern() != null && rPr.getKern().getVal() != null
					&& run.size * 2 >= rPr.getKern().getVal().doubleValue();
			ResolvedColor c = null;
			if (rPr != null && rPr.getColor() != null) {
				org.docx4j.wml.Color col = rPr.getColor();
				c = WordColors.resolve(col.getVal(), col.getThemeColor(), col.getThemeTint(), col.getThemeShade(), theme);
			}
			run.colour = c == null ? ResolvedColor.BLACK : c;
			if (rPr != null) {
				if (rPr.getTextFill() != null) {
					run.textFill = true;
					run.fill = TextEffects.fill(rPr.getTextFill());
					run.alpha = run.fill instanceof CTSolidColorFillProperties && TextEffects.hasAlpha((CTSolidColorFillProperties) run.fill);
				}
				if (rPr.getTextOutline() != null) run.outline = TextEffects.line(rPr.getTextOutline());
				run.shadow = rPr.getShadow14() != null;
				run.glow = rPr.getGlow() != null;
				run.reflection = rPr.getReflection() != null;
				if (run.hasEffect()) run.effects = TextEffects.effects(rPr);
				run.threeD = rPr.getProps3D() != null || rPr.getScene3D() != null;
				if (rPr.getLang() != null) run.lang = rPr.getLang().getVal();
				decorations(run, rPr);
			}
			return run;
		}

		/**
		 * The run's underline, strikes, highlight and shading, and a superscript's or subscript's size and raise, as the FO
		 * output reads them (TextDecoration, HighlightColor, RShading, VerticalAlignment): the line layout drew none of
		 * them (CR-083 phases 1 and 2 review)
		 */
		void decorations(Run run, RPr rPr) {
			if (rPr.getU() != null && rPr.getU().getVal() != null && rPr.getU().getVal() != org.docx4j.wml.UnderlineEnumeration.NONE) {
				run.underline = rPr.getU().getVal();
				run.underlineColour = WordColors.of(rPr.getU(), theme);
			}
			run.strike = isOn(rPr.getStrike());
			run.doubleStrike = isOn(rPr.getDstrike());
			if (rPr.getHighlight() != null && rPr.getHighlight().getVal() != null && !"none".equals(rPr.getHighlight().getVal())) {
				run.highlight = ResolvedColor.ofHex(rPr.getHighlight().getHexVal());
			}
			if (rPr.getShd() != null) run.shading = WordColors.fill(rPr.getShd(), theme);
			if (rPr.getVertAlign() != null && rPr.getVertAlign().getVal() != null) {
				org.docx4j.wml.STVerticalAlignRun va = rPr.getVertAlign().getVal();
				boolean sup = va == org.docx4j.wml.STVerticalAlignRun.SUPERSCRIPT;
				if (sup || va == org.docx4j.wml.STVerticalAlignRun.SUBSCRIPT) {
					run.rise = run.size * (sup ? org.docx4j.model.properties.run.VerticalAlignment.SUPERSCRIPT_RAISE
							: -org.docx4j.model.properties.run.VerticalAlignment.SUBSCRIPT_DROP);
					run.size *= org.docx4j.model.properties.run.VerticalAlignment.SIZE_FACTOR;
				}
			}
		}

		/** Word's line metrics for the run's face: the document font's own where docx4j knows them, else its file's */
		void metrics(Run run) {
			try {
				org.docx4j.fonts.PhysicalFont pf = pkg.getFontMapper() == null ? null : pkg.getFontMapper().get(run.face);
				org.docx4j.fonts.WordLineMetrics.Metrics m = org.docx4j.fonts.WordLineMetrics.get(run.face, pf);
				run.ascent = m.winAscent;
				run.descent = m.winDescent;
				run.single = m.lineHeightFactor();
				run.leading = m.eastAsian ? 0 : m.externalLeading;
			} catch (RuntimeException e) {
				log.debug("No line metrics for " + run.face + ": " + e.getMessage());
			}
		}

		String face(RPr rPr) {
			try {
				return RunFontSelector.defaultFontOf(rPr == null ? null : rPr.getRFonts(), themePart, themeFontLang);
			} catch (RuntimeException e) {
				return "Times New Roman";
			}
		}
	}

	/** Bookmarks, proofing marks, comment ranges, permissions: nothing shown */
	private static boolean ignored(Object v) {
		return v instanceof org.docx4j.wml.CTBookmark || v instanceof org.docx4j.wml.CTMarkupRange || v instanceof org.docx4j.wml.ProofErr
				|| v instanceof org.docx4j.wml.CommentRangeStart || v instanceof org.docx4j.wml.CommentRangeEnd
				|| v instanceof org.docx4j.wml.RangePermissionStart || v instanceof org.docx4j.wml.CTPerm
				|| v instanceof org.docx4j.wml.CTMarkup || v instanceof org.docx4j.wml.CTTrackChange
				|| v instanceof org.docx4j.wml.R.LastRenderedPageBreak || v instanceof org.docx4j.wml.CTRel;
	}

	private static String name(Object v) {
		return v == null ? "nothing" : v.getClass().getSimpleName();
	}

	private static double size(RPr rPr) {
		return rPr == null || rPr.getSz() == null || rPr.getSz().getVal() == null ? 10 : rPr.getSz().getVal().doubleValue() / 2;
	}

	private static double twips(java.math.BigInteger v) {
		return v == null ? 0 : v.doubleValue() / 20;
	}

	private static boolean isOn(BooleanDefaultTrue b) {
		return b != null && b.isVal();
	}
}
