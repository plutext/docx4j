package org.docx4j.fidelity.corpus;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.CTCompatSetting;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

/**
 * Small fluent builder for probe documents. Every paragraph added through
 * {@link Para#add()} is prefixed with a label ("P01 ", "P02 ", ...) so that a
 * line in either PDF can be attributed to its paragraph without heuristics.
 *
 * Fonts are restricted to four that must be installed on both the Linux build
 * host and the Windows reference VM: Liberation Serif and Liberation Sans
 * (docx4j-export-fo-fonts-liberation), Carlito (docx4j-export-fo-fonts-crosextra)
 * and DejaVu Sans (not in any docx4j font module; from the dejavu-fonts project).
 */
public final class Doc {

	public static final String SERIF = "Liberation Serif";
	public static final String SANS = "Liberation Sans";
	public static final String CARLITO = "Carlito";
	public static final String DEJAVU = "DejaVu Sans";

	static final ObjectFactory F = Context.getWmlObjectFactory();

	private final WordprocessingMLPackage pkg;
	private final MainDocumentPart mdp;
	private int paraCounter = 0;
	private int hdrFtrCounter = 0;
	private int imageCounter = 0;

	private Doc(WordprocessingMLPackage pkg) {
		this.pkg = pkg;
		this.mdp = pkg.getMainDocumentPart();
	}

	/** A4 portrait, 1 inch margins, the given Word compatibility mode (12, 14 or 15). */
	public static Doc create(int compatMode) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage(PageSizePaper.A4, false);
		Doc d = new Doc(pkg);
		d.setCompatMode(compatMode);
		SectPr sectPr = d.sectPr();
		if (sectPr.getPgMar() == null) {
			sectPr.setPgMar(F.createSectPrPgMar());
		}
		SectPr.PgMar m = sectPr.getPgMar();
		m.setTop(BigInteger.valueOf(1440));
		m.setBottom(BigInteger.valueOf(1440));
		m.setLeft(BigInteger.valueOf(1440));
		m.setRight(BigInteger.valueOf(1440));
		m.setHeader(BigInteger.valueOf(708));
		m.setFooter(BigInteger.valueOf(708));
		return d;
	}

	public WordprocessingMLPackage pkg() {
		return pkg;
	}

	/** w:pgMar header and footer distances (twips) for the current section. */
	public void headerFooterDistance(int headerTwips, int footerTwips) {
		SectPr.PgMar m = sectPr().getPgMar();
		m.setHeader(BigInteger.valueOf(headerTwips));
		m.setFooter(BigInteger.valueOf(footerTwips));
	}

	public MainDocumentPart mdp() {
		return mdp;
	}

	private int bookmarkId = 0;

	int nextBookmarkId() {
		return ++bookmarkId;
	}

	/** w:pgNumType w:start for the current section: the number its first page carries,
	 *  and so the number a PAGEREF to anything on it gives. */
	public void pageNumberStart(int start) {
		org.docx4j.wml.CTPageNumber n = F.createCTPageNumber();
		n.setStart(BigInteger.valueOf(start));
		sectPr().setPgNumType(n);
	}

	/** A PAGEREF field, which is what an entry of a table of contents holds: the page
	 *  the bookmark is on, hyperlinked (\h).  The number cached in the file is
	 *  {@code cached}; the field is marked dirty, so Word refreshes it when the document
	 *  is opened. */
	public static Object pageref(String bookmarkName, String cached, String font, int halfPts) {
		org.docx4j.wml.CTSimpleField f = F.createCTSimpleField();
		f.setInstr(" PAGEREF " + bookmarkName + " \\h ");
		f.setDirty(Boolean.TRUE);
		f.getContent().add(run(cached, font, halfPts, null));
		return F.createPFldSimple(f);
	}

	public SectPr sectPr() {
		try {
			SectPr sp = mdp.getContents().getBody().getSectPr();
			if (sp == null) {
				sp = F.createSectPr();
				mdp.getContents().getBody().setSectPr(sp);
			}
			return sp;
		} catch (org.docx4j.openpackaging.exceptions.Docx4JException e) {
			throw new IllegalStateException(e);
		}
	}

	private void setCompatMode(int mode) throws Exception {
		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart();
		if (dsp == null) {
			dsp = new DocumentSettingsPart();
			dsp.setContents(F.createCTSettings());
			mdp.addTargetPart(dsp);
		}
		CTCompat compat = dsp.getContents().getCompat();
		if (compat == null) {
			compat = F.createCTCompat();
			dsp.getContents().setCompat(compat);
		}
		CTCompatSetting cs = F.createCTCompatSetting();
		cs.setName("compatibilityMode");
		cs.setUri("http://schemas.microsoft.com/office/word");
		cs.setVal(Integer.toString(mode));
		compat.getCompatSetting().add(cs);
	}

	// ---------------------------------------------------------------- text

	/** Deterministic prose with a spread of word lengths, for line-break probes. */
	private static final String[] SENTENCES = {
		"The quick brown fox jumps over the lazy dog while the farmer watches from the gate.",
		"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
		"Pagination is the process of dividing a document into discrete pages, either electronic pages or printed pages.",
		"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
		"A typesetting engine must decide where each line ends and where each page ends; small differences accumulate.",
		"Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
		"Internationalization, characteristically, produces extraordinarily long words that constrain justification.",
		"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
	};

	/**
	 * Prose whose words are mostly 10 to 15 letters long, for the hyphenation
	 * probes: Word only hyphenates where the word that does not fit would leave a
	 * gap bigger than the hyphenation zone, which needs long words.
	 */
	private static final String[] LONG_SENTENCES = {
		"The administration acknowledged that implementation of the recommendations required extraordinary international collaboration.",
		"Documentation accompanying the specification describes the responsibilities of every participating organisation comprehensively.",
		"Considerable improvements in productivity followed the reorganisation of the manufacturing establishment near Northampton.",
		"Understanding the relationship between representation and interpretation demands considerable philosophical sophistication.",
		"Environmental considerations increasingly influence infrastructure development throughout the metropolitan municipalities.",
		"Professional qualifications and accreditation requirements were harmonised across the participating jurisdictions.",
		"Communications between the departments deteriorated whenever administrative responsibilities were redistributed unexpectedly.",
		"Preliminary investigations confirmed that the transformation programme substantially exceeded the original appropriations.",
	};

	public static String longProse(int sentences, int offset) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sentences; i++) {
			if (i > 0) sb.append(' ');
			sb.append(LONG_SENTENCES[(i + offset) % LONG_SENTENCES.length]);
		}
		return sb.toString();
	}

	public static String prose(int sentences) {
		return prose(sentences, 0);
	}

	public static String prose(int sentences, int offset) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sentences; i++) {
			if (i > 0) sb.append(' ');
			sb.append(SENTENCES[(i + offset) % SENTENCES.length]);
		}
		return sb.toString();
	}

	String nextLabel() {
		paraCounter++;
		return String.format("P%02d ", paraCounter);
	}

	public Para para() {
		return new Para(this);
	}

	public Para para(String text) {
		return new Para(this).text(text);
	}

	public P add(P p) {
		mdp.getContent().add(p);
		return p;
	}

	public Tbl add(Tbl t) {
		mdp.getContent().add(t);
		return t;
	}

	public void pageBreak() {
		P p = F.createP();
		R r = F.createR();
		Br br = F.createBr();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		p.getContent().add(r);
		mdp.getContent().add(p);
	}

	/**
	 * Ends the current section: a paragraph carrying a copy of the body sectPr
	 * (with whatever headers/footers were added so far) is appended, and the
	 * body sectPr is reset to start a new section of the given type
	 * ("nextPage" or "continuous") with no header/footer references.
	 */
		public void endSection(String nextSectionType) {
		endSection(nextSectionType, null);
	}

	/** As {@link #endSection(String)}, with explicit spacing on the section-break paragraph
	 *  (null = inherit docDefaults, which docx4j's default template sets to after=10pt). */
	public void endSection(String nextSectionType, Integer afterTwips) {
		SectPr current = sectPr();
		SectPr copy = XmlUtils.deepCopy(current);
		P p = F.createP();
		PPr ppr = F.createPPr();
		ppr.setSectPr(copy);
		if (afterTwips != null) {
			PPrBase.Spacing sp = F.createPPrBaseSpacing();
			sp.setBefore(BigInteger.ZERO);
			sp.setAfter(BigInteger.valueOf(afterTwips));
			ppr.setSpacing(sp);
		}
		p.setPPr(ppr);
		mdp.getContent().add(p);

		current.getEGHdrFtrReferences().clear();
		SectPr.Type type = F.createSectPrType();
		type.setVal(nextSectionType);
		current.setType(type);
	}

	/**
	 * Ends the current section <b>on an existing paragraph</b>: that paragraph carries a
	 * copy of the body sectPr, which is where Word puts it.  {@link #endSection} appends
	 * an empty paragraph of its own instead, and that paragraph - not the one before it -
	 * is then the section's last block, which is no use where the question is what the
	 * section's last block is.
	 */
	public void endSectionOn(P p, String nextSectionType) {
		SectPr current = sectPr();
		SectPr copy = XmlUtils.deepCopy(current);
		PPr ppr = p.getPPr();
		if (ppr == null) {
			ppr = F.createPPr();
			p.setPPr(ppr);
		}
		ppr.setSectPr(copy);
		current.getEGHdrFtrReferences().clear();
		SectPr.Type type = F.createSectPrType();
		type.setVal(nextSectionType);
		current.setType(type);
	}

	/**
	 * A section break at exactly this point, with the break type written on the
	 * break paragraph itself: an empty paragraph carrying a copy of the current
	 * sectPr whose {@code w:type} is {@code type}, or which carries no
	 * {@code w:type} at all when {@code type} is null (Word reads that as
	 * nextPage). {@link #endSection} instead names the type of the section it
	 * opens, which lands on the *next* break paragraph, and that is no use where
	 * the question is what Word does with this break.
	 *
	 * Headers and footers on the body sectPr are cleared, so the section which
	 * follows starts without them.
	 */
	public void sectionBreakHere(String type, Integer afterTwips) {
		SectPr current = sectPr();
		SectPr copy = XmlUtils.deepCopy(current);
		if (type == null) {
			copy.setType(null);
		} else {
			SectPr.Type t = F.createSectPrType();
			t.setVal(type);
			copy.setType(t);
		}
		P p = F.createP();
		PPr ppr = F.createPPr();
		ppr.setSectPr(copy);
		if (afterTwips != null) {
			PPrBase.Spacing sp = F.createPPrBaseSpacing();
			sp.setBefore(BigInteger.ZERO);
			sp.setAfter(BigInteger.valueOf(afterTwips));
			ppr.setSpacing(sp);
		}
		p.setPPr(ppr);
		mdp.getContent().add(p);
		current.getEGHdrFtrReferences().clear();
		current.setType(null);
	}

	/** An empty paragraph: no runs at all, and no label. */
	public P emptyParagraph() {
		P p = F.createP();
		PPr ppr = F.createPPr();
		PPrBase.Spacing sp = F.createPPrBaseSpacing();
		sp.setBefore(BigInteger.ZERO);
		sp.setAfter(BigInteger.ZERO);
		sp.setLine(BigInteger.valueOf(240));
		sp.setLineRule(STLineSpacingRule.AUTO);
		ppr.setSpacing(sp);
		p.setPPr(ppr);
		mdp.getContent().add(p);
		return p;
	}

		// ---------------------------------------------------------------- numbering

	private boolean numberingAdded = false;

	/** Make the docx4j default numbering definitions available (numId 1 = decimal list). */
	public void ensureNumbering() throws Exception {
		if (numberingAdded) return;
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
				new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		mdp.addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		numberingAdded = true;
	}

	/**
	 * A numbering part built from the {@code w:abstractNum} and {@code w:num} elements
	 * given as XML (without the {@code w:numbering} wrapper, which this adds).  The
	 * numbering probes turn on details a builder would hide - whether a level carries a
	 * {@code w:pStyle} link, what its own {@code w:pPr} indents say - so the caller
	 * writes the XML.  Replaces any numbering part added earlier.
	 */
	public void numberingXml(String body) throws Exception {
		String xml = "<w:numbering xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ body + "</w:numbering>";
		Object o = XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.wml.Numbering.class);
		if (o instanceof jakarta.xml.bind.JAXBElement) o = ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
				new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering) o);
		mdp.addTargetPart(ndp);
		numberingAdded = true;
	}

	/**
	 * One {@code w:lvl} of a decimal list: {@code w:ilvl}, the label's text, the
	 * indents Word's own list levels carry, and - when {@code pStyleLink} is not null -
	 * a {@code w:pStyle} link naming the paragraph style the level belongs to.  Element
	 * order follows CT_Lvl, which JAXB needs.
	 */
	public static String decimalLevel(int ilvl, String pStyleLink, int leftTwips, int hangingTwips) {
		return "<w:lvl w:ilvl=\"" + ilvl + "\">"
				+ "<w:start w:val=\"1\"/>"
				+ "<w:numFmt w:val=\"decimal\"/>"
				+ (pStyleLink == null ? "" : "<w:pStyle w:val=\"" + pStyleLink + "\"/>")
				+ "<w:lvlText w:val=\"%" + (ilvl + 1) + ".\"/>"
				+ "<w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr><w:ind w:left=\"" + leftTwips + "\" w:hanging=\"" + hangingTwips + "\"/></w:pPr>"
				+ "</w:lvl>";
	}

	// ---------------------------------------------------------------- measurement

	/**
	 * The advance width, in points, of {@code text} set in {@code fontName} at
	 * {@code halfPts} half-points: the font's own advances, with fractional metrics and
	 * no kerning, which is what Word and FOP both read out of the hmtx table for a run
	 * carrying no {@code w:kern} and no {@code w:spacing}.  A probe which has to make a
	 * quarter of a point decide a line break needs the exact number, not an estimate,
	 * so a substituted font is an error rather than an approximation.
	 */
	public static double advancePoints(String text, String fontName, int halfPts) {
		java.awt.Font f = new java.awt.Font(fontName, java.awt.Font.PLAIN, 1).deriveFont(halfPts / 2.0f);
		if (!fontName.equalsIgnoreCase(f.getFamily())) {
			throw new IllegalStateException("font '" + fontName + "' is not installed on this host"
					+ " (java.awt gave '" + f.getFamily() + "'); the measured probes cannot be built");
		}
		java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, false, true);
		return f.getStringBounds(text, frc).getWidth();
	}

	/**
	 * {@link #advancePoints} rounded up to a whole twip: the narrowest measure which
	 * still holds the text on one line, to within the resolution a docx can express.
	 * A cell whose text measure is exactly this is at most 1/20 point wider than the
	 * text, so any further charge against the measure - half a border, a whole one -
	 * wraps the last word.
	 */
	public static int advanceTwipsCeil(String text, String fontName, int halfPts) {
		return (int) Math.ceil(advancePoints(text, fontName, halfPts) * 20.0 - 1e-9);
	}

	// ------------------------------------------------------------ section vAlign

	/** {@code w:vAlign} on the current section: "top", "center", "both" or "bottom". */
	public void verticalAlignment(String val) {
		org.docx4j.wml.CTVerticalJc jc = F.createCTVerticalJc();
		jc.setVal(org.docx4j.wml.STVerticalJc.fromValue(val));
		sectPr().setVAlign(jc);
	}

	// ------------------------------------------------------------ w:compat

	/**
	 * A {@code w:settings/w:compat} flag, by its element name
	 * ("growAutofit", "doNotExpandShiftReturn", ...), written explicitly as
	 * {@code w:val="1"} or {@code w:val="0"}.
	 *
	 * <p>Every {@code w:compat} child is a {@code CT_OnOff} and the docx4j model gives
	 * each of them its own setter, so the name is turned into
	 * {@code setXxx(BooleanDefaultTrue)} by reflection: that keeps this helper as short as
	 * the schema is long.  An unknown name throws.</p>
	 *
	 * <p>The value docx4j resolves for a flag the document does <em>not</em> state comes
	 * from the compatibility mode; see
	 * {@link org.docx4j.model.CompatibilityOptions}.</p>
	 */
	public void compat(String flag, boolean value) throws Exception {
		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart();
		if (dsp == null) {
			dsp = new DocumentSettingsPart();
			dsp.setContents(F.createCTSettings());
			mdp.addTargetPart(dsp);
		}
		CTCompat compat = dsp.getContents().getCompat();
		if (compat == null) {
			compat = F.createCTCompat();
			dsp.getContents().setCompat(compat);
		}
		String setter = "set" + Character.toUpperCase(flag.charAt(0)) + flag.substring(1);
		BooleanDefaultTrue b = new BooleanDefaultTrue();
		b.setVal(Boolean.valueOf(value));
		CTCompat.class.getMethod(setter, BooleanDefaultTrue.class).invoke(compat, b);
	}

	// ------------------------------------------------------------ hyphenation

	/**
	 * The document's automatic hyphenation settings (w:settings): w:autoHyphenation,
	 * w:hyphenationZone in twips (Word's UI default is 360 = 0.25 inch), w:
	 * consecutiveHyphenLimit (0 = no limit) and w:doNotHyphenateCaps.
	 */
	public void hyphenation(boolean auto, int zoneTwips, int consecutiveLimit, boolean doNotHyphenateCaps)
			throws Exception {
		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart();
		if (dsp == null) {
			dsp = new DocumentSettingsPart();
			dsp.setContents(F.createCTSettings());
			mdp.addTargetPart(dsp);
		}
		org.docx4j.wml.CTSettings settings = dsp.getContents();
		if (auto) settings.setAutoHyphenation(new BooleanDefaultTrue());
		org.docx4j.wml.CTTwipsMeasure zone = F.createCTTwipsMeasure();
		zone.setVal(java.math.BigInteger.valueOf(zoneTwips));
		settings.setHyphenationZone(zone);
		if (consecutiveLimit > 0) {
			org.docx4j.wml.CTSettings.ConsecutiveHyphenLimit limit = new org.docx4j.wml.CTSettings.ConsecutiveHyphenLimit();
			limit.setVal(BigInteger.valueOf(consecutiveLimit));
			settings.setConsecutiveHyphenLimit(limit);
		}
		if (doNotHyphenateCaps) settings.setDoNotHyphenateCaps(new BooleanDefaultTrue());
	}

	/** w:lang on the document defaults (w:docDefaults/w:rPrDefault), which is the
	 *  language Word and FOP both choose hyphenation patterns by. */
	public void documentLanguage(String lang) {
		org.docx4j.wml.Styles styles = mdp.getStyleDefinitionsPart().getJaxbElement();
		if (styles.getDocDefaults() == null) {
			styles.setDocDefaults(F.createDocDefaults());
		}
		if (styles.getDocDefaults().getRPrDefault() == null) {
			styles.getDocDefaults().setRPrDefault(F.createDocDefaultsRPrDefault());
		}
		if (styles.getDocDefaults().getRPrDefault().getRPr() == null) {
			styles.getDocDefaults().getRPrDefault().setRPr(F.createRPr());
		}
		styles.getDocDefaults().getRPrDefault().getRPr().setLang(language(lang));
	}

	static org.docx4j.wml.CTLanguage language(String lang) {
		org.docx4j.wml.CTLanguage l = F.createCTLanguage();
		l.setVal(lang);
		return l;
	}

	// ---------------------------------------------------------------- styles

	public void addParagraphStyle(String styleId, String basedOn, Consumer<PPr> pPrCustomiser) {
		addParagraphStyle(styleId, basedOn, pPrCustomiser, null);
	}

	/** A paragraph style with run properties of its own (the font and size a run with no
	 *  w:rPr of its own inherits) as well as paragraph properties. */
	public void addParagraphStyle(String styleId, String basedOn, Consumer<PPr> pPrCustomiser,
			Consumer<RPr> rPrCustomiser) {
		Style s = F.createStyle();
		s.setType("paragraph");
		s.setStyleId(styleId);
		Style.Name n = F.createStyleName();
		n.setVal(styleId);
		s.setName(n);
		if (basedOn != null) {
			Style.BasedOn b = F.createStyleBasedOn();
			b.setVal(basedOn);
			s.setBasedOn(b);
		}
		PPr ppr = F.createPPr();
		if (pPrCustomiser != null) pPrCustomiser.accept(ppr);
		s.setPPr(ppr);
		if (rPrCustomiser != null) {
			RPr rpr = F.createRPr();
			rPrCustomiser.accept(rpr);
			s.setRPr(rpr);
		}
		mdp.getStyleDefinitionsPart().getJaxbElement().getStyle().add(s);
	}

	/** An RPr customiser setting the font (ascii/hAnsi/cs/eastAsia) and the size. */
	public static Consumer<RPr> font(String font, int halfPts) {
		return rpr -> {
			RFonts rf = F.createRFonts();
			rf.setAscii(font);
			rf.setHAnsi(font);
			rf.setCs(font);
			rf.setEastAsia(font);
			rpr.setRFonts(rf);
			HpsMeasure sz = F.createHpsMeasure();
			sz.setVal(BigInteger.valueOf(halfPts));
			rpr.setSz(sz);
			rpr.setSzCs(sz);
		};
	}

	/**
	 * The document defaults' run properties (w:docDefaults/w:rPrDefault/w:rPr): the font
	 * and size a run inherits before any style or direct formatting. Word's own
	 * application default (Aptos 11pt) applies where a document declares none.
	 */
	public void documentDefaultRun(String fontName, int halfPts) {
		org.docx4j.wml.Styles styles = mdp.getStyleDefinitionsPart().getJaxbElement();
		if (styles.getDocDefaults() == null) {
			styles.setDocDefaults(F.createDocDefaults());
		}
		if (styles.getDocDefaults().getRPrDefault() == null) {
			styles.getDocDefaults().setRPrDefault(F.createDocDefaultsRPrDefault());
		}
		if (styles.getDocDefaults().getRPrDefault().getRPr() == null) {
			styles.getDocDefaults().getRPrDefault().setRPr(F.createRPr());
		}
		font(fontName, halfPts).accept(styles.getDocDefaults().getRPrDefault().getRPr());
	}

	// ---------------------------------------------------------------- header/footer

		public void addHeader(String font, int halfPts, String... lines) throws Exception {
		addHeader(HdrFtrRef.DEFAULT, font, halfPts, lines);
	}

	/** A header of the given type (DEFAULT, FIRST, EVEN); FIRST also sets w:titlePg, EVEN sets w:evenAndOddHeaders. */
	public void addHeader(HdrFtrRef type, String font, int halfPts, String... lines) throws Exception {
		if (type == HdrFtrRef.FIRST) sectPr().setTitlePg(new BooleanDefaultTrue());
		if (type == HdrFtrRef.EVEN) evenAndOddHeaders();
		hdrFtrCounter++;
		HeaderPart hp = new HeaderPart(new PartName("/word/header" + hdrFtrCounter + ".xml"));
		Hdr hdr = F.createHdr();
		for (String line : lines) {
			hdr.getContent().add(plainParagraph(line, font, halfPts));
		}
		hp.setJaxbElement(hdr);
				Relationship rel = mdp.addTargetPart(hp);
		HeaderReference ref = F.createHeaderReference();
		ref.setId(rel.getId());
		ref.setType(type);
		sectPr().getEGHdrFtrReferences().add(ref);
	}

	/** A header whose content is the given paragraphs (see {@link #pictureParagraph}). */
	public void addHeader(HdrFtrRef type, List<P> paragraphs) throws Exception {
		if (type == HdrFtrRef.FIRST) sectPr().setTitlePg(new BooleanDefaultTrue());
		if (type == HdrFtrRef.EVEN) evenAndOddHeaders();
		hdrFtrCounter++;
		HeaderPart hp = new HeaderPart(new PartName("/word/header" + hdrFtrCounter + ".xml"));
		Relationship rel = mdp.addTargetPart(hp); // before relating pictures to it
		Hdr hdr = F.createHdr();
		for (P para : paragraphs) {
			// a picture's image part must hang off the header part
			for (Object o : para.getContent()) {
				if (o instanceof R) for (Object c : ((R) o).getContent()) {
					if (c instanceof Drawing) rehome((Drawing) c, hp);
				}
			}
			hdr.getContent().add(para);
		}
		hp.setJaxbElement(hdr);
		HeaderReference ref = F.createHeaderReference();
		ref.setId(rel.getId());
		ref.setType(type);
		sectPr().getEGHdrFtrReferences().add(ref);
	}

	/**
	 * A header whose content is the given block-level objects: paragraphs, tables, or
	 * anything else that goes in a w:hdr. Pictures anywhere inside are re-related to the
	 * header part, however deeply nested (a picture in a table cell included).
	 */
	public void addHeaderContent(HdrFtrRef type, List<Object> content) throws Exception {
		if (type == HdrFtrRef.FIRST) sectPr().setTitlePg(new BooleanDefaultTrue());
		if (type == HdrFtrRef.EVEN) evenAndOddHeaders();
		hdrFtrCounter++;
		HeaderPart hp = new HeaderPart(new PartName("/word/header" + hdrFtrCounter + ".xml"));
		Relationship rel = mdp.addTargetPart(hp); // before relating pictures to it
		Hdr hdr = F.createHdr();
		for (Object o : content) {
			rehomeDeep(o, hp);
			hdr.getContent().add(o);
		}
		hp.setJaxbElement(hdr);
		HeaderReference ref = F.createHeaderReference();
		ref.setId(rel.getId());
		ref.setType(type);
		sectPr().getEGHdrFtrReferences().add(ref);
	}

	/** Re-relate every picture inside the node - at any depth - to the header part. */
	private void rehomeDeep(Object node, HeaderPart hp) throws Exception {
		if (node instanceof jakarta.xml.bind.JAXBElement) {
			node = ((jakarta.xml.bind.JAXBElement<?>) node).getValue();
		}
		if (node instanceof Drawing) {
			rehome((Drawing) node, hp);
			return;
		}
		if (node instanceof org.docx4j.wml.ContentAccessor) {
			for (Object child : ((org.docx4j.wml.ContentAccessor) node).getContent()) {
				rehomeDeep(child, hp);
			}
		}
	}

	/** A single-spaced paragraph holding an inline picture, for headers. */
	public P pictureParagraph(int wPx, int hPx, long cxTwips) throws Exception {
		P p = plainParagraph("", SERIF_DEFAULT, 20);
		p.getContent().clear();
		p.getContent().add(inlineImage(wPx, hPx, cxTwips));
		return p;
	}

	private static final String SERIF_DEFAULT = "Liberation Serif";

	/** Re-relate a picture made against the main document part to a header part. */
	private void rehome(Drawing d, HeaderPart hp) throws Exception {
		for (Object o : d.getAnchorOrInline()) {
			if (!(o instanceof Inline)) continue;
			org.docx4j.dml.picture.Pic pic = ((Inline) o).getGraphic().getGraphicData().getPic();
			String rId = pic.getBlipFill().getBlip().getEmbed();
			Relationship old = mdp.getRelationshipsPart().getRelationshipByID(rId);
			org.docx4j.openpackaging.parts.Part imagePart = mdp.getRelationshipsPart().getPart(old);
			Relationship rel = hp.addTargetPart(imagePart);
			pic.getBlipFill().getBlip().setEmbed(rel.getId());
		}
	}

	private void evenAndOddHeaders() throws Exception {
		DocumentSettingsPart dsp = mdp.getDocumentSettingsPart();
		dsp.getContents().setEvenAndOddHeaders(new BooleanDefaultTrue());
	}

	/** Page size in twips and margins (top, right, bottom, left) for the current section. */
	public void pageGeometry(int wTwips, int hTwips, boolean landscape, int top, int right, int bottom, int left) {
		SectPr sp = sectPr();
		SectPr.PgSz sz = sp.getPgSz() == null ? F.createSectPrPgSz() : sp.getPgSz();
		sz.setW(BigInteger.valueOf(wTwips));
		sz.setH(BigInteger.valueOf(hTwips));
		if (landscape) sz.setOrient(org.docx4j.wml.STPageOrientation.LANDSCAPE);
		sp.setPgSz(sz);
		SectPr.PgMar m = sp.getPgMar();
		m.setTop(BigInteger.valueOf(top));
		m.setRight(BigInteger.valueOf(right));
		m.setBottom(BigInteger.valueOf(bottom));
		m.setLeft(BigInteger.valueOf(left));
	}

	/**
	 * The current section's w:cols: {@code widths} and {@code spaces} in twips, one
	 * entry each per column (the last space is ignored, as Word writes it).  Word marks
	 * such a section w:equalWidth="0".  Pass a single width for a one-column section.
	 */
	public void columns(int[] widths, int[] spaces) {
		org.docx4j.wml.CTColumns cols = F.createCTColumns();
		cols.setEqualWidth(Boolean.FALSE);
		if (widths.length > 1) cols.setNum(BigInteger.valueOf(widths.length));
		cols.setSpace(BigInteger.valueOf(spaces.length > 0 ? spaces[0] : 720));
		for (int i = 0; i < widths.length; i++) {
			org.docx4j.wml.CTColumn col = F.createCTColumn();
			col.setW(BigInteger.valueOf(widths[i]));
			if (i + 1 < widths.length) col.setSpace(BigInteger.valueOf(spaces[i]));
			cols.getCol().add(col);
		}
		sectPr().setCols(cols);
	}

	/** The current section's w:cols as {@code num} equal columns with one gap. */
	public void equalColumns(int num, int spaceTwips) {
		org.docx4j.wml.CTColumns cols = F.createCTColumns();
		cols.setNum(BigInteger.valueOf(num));
		cols.setSpace(BigInteger.valueOf(spaceTwips));
		sectPr().setCols(cols);
	}

	// ---------------------------------------------------------------- anchored images

	/**
	 * A generated PNG as a floating (anchored) picture. wrap: "square" (text both sides),
	 * "topAndBottom", or "none" (behind text). Horizontal: alignment "left"/"right"/"center"
	 * relative to the margin, or a posOffset in EMU when hAlign is null. Vertical: posOffset
	 * from the paragraph.
	 */
	public R anchoredImage(int wPx, int hPx, long cxEmu, long cyEmu, String wrap, String hAlign, long hOffsetEmu, long vOffsetEmu) throws Exception {
		imageCounter++;
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(pkg, mdp, png(wPx, hPx));
		String rId = imagePart.getSourceRelationship().getId();
		String wrapXml = "square".equals(wrap) ? "<wp:wrapSquare wrapText=\"bothSides\"/>"
				: "topAndBottom".equals(wrap) ? "<wp:wrapTopAndBottom/>" : "<wp:wrapNone/>";
		String posH = hAlign != null ? "<wp:align>" + hAlign + "</wp:align>" : "<wp:posOffset>" + hOffsetEmu + "</wp:posOffset>";
		String xml = "<wp:anchor xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\" relativeHeight=\"" + (251658240 + imageCounter) + "\""
				+ " behindDoc=\"" + ("none".equals(wrap) ? 1 : 0) + "\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"margin\">" + posH + "</wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>" + vOffsetEmu + "</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"" + cxEmu + "\" cy=\"" + cyEmu + "\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ wrapXml
				+ "<wp:docPr id=\"" + imageCounter + "\" name=\"anchor" + imageCounter + "\"/>"
				+ "<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect=\"1\"/></wp:cNvGraphicFramePr>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic>"
				+ "<pic:nvPicPr><pic:cNvPr id=\"" + (100 + imageCounter) + "\" name=\"anchor" + imageCounter + "\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + rId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + cxEmu + "\" cy=\"" + cyEmu + "\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>"
				+ "</pic:pic></a:graphicData></a:graphic></wp:anchor>";
		Object anchor = XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.dml.wordprocessingDrawing.Anchor.class);
		if (anchor instanceof jakarta.xml.bind.JAXBElement) anchor = ((jakarta.xml.bind.JAXBElement<?>) anchor).getValue();
		R r = F.createR();
		Drawing d = F.createDrawing();
		d.getAnchorOrInline().add((org.docx4j.dml.wordprocessingDrawing.Anchor) anchor);
		r.getContent().add(d);
		return r;
	}

	/**
	 * A generated PNG anchored to the PAGE, behind the text, at the given offsets in EMU
	 * from the page's top left corner - the shape a cover background has. Word clamps a
	 * picture whose offsets put it off the page; the raw offsets are what the docx says.
	 */
	public R pageAnchoredImage(int wPx, int hPx, long cxEmu, long cyEmu, long hOffsetEmu, long vOffsetEmu)
			throws Exception {
		imageCounter++;
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(pkg, mdp, png(wPx, hPx));
		String rId = imagePart.getSourceRelationship().getId();
		String xml = "<wp:anchor xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"" + (251658240 + imageCounter) + "\""
				+ " behindDoc=\"1\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"page\"><wp:posOffset>" + hOffsetEmu + "</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"page\"><wp:posOffset>" + vOffsetEmu + "</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"" + cxEmu + "\" cy=\"" + cyEmu + "\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ "<wp:wrapNone/>"
				+ "<wp:docPr id=\"" + imageCounter + "\" name=\"pageAnchor" + imageCounter + "\"/>"
				+ "<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect=\"1\"/></wp:cNvGraphicFramePr>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic>"
				+ "<pic:nvPicPr><pic:cNvPr id=\"" + (200 + imageCounter) + "\" name=\"pageAnchor" + imageCounter + "\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + rId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + cxEmu + "\" cy=\"" + cyEmu + "\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>"
				+ "</pic:pic></a:graphicData></a:graphic></wp:anchor>";
		Object anchor = XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.dml.wordprocessingDrawing.Anchor.class);
		if (anchor instanceof jakarta.xml.bind.JAXBElement) anchor = ((jakarta.xml.bind.JAXBElement<?>) anchor).getValue();
		R r = F.createR();
		Drawing d = F.createDrawing();
		d.getAnchorOrInline().add((org.docx4j.dml.wordprocessingDrawing.Anchor) anchor);
		r.getContent().add(d);
		return r;
	}

	private static byte[] png(int wPx, int hPx) throws Exception {
		BufferedImage img = new BufferedImage(wPx, hPx, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		for (int x = 0; x < wPx; x++) {
			int v = 80 + (x * 150) / Math.max(1, wPx);
			g.setColor(new Color(v, v, 220));
			g.drawLine(x, 0, x, hPx);
		}
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, wPx - 1, hPx - 1);
		g.drawLine(0, 0, wPx - 1, hPx - 1);
		g.dispose();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", bos);
		return bos.toByteArray();
	}

	// ---------------------------------------------------------------- footnotes

	private int footnoteCounter = 0;
	private StringBuilder footnotesXml;

	/** A run holding a footnote reference; the footnote's text goes into the footnotes part (created on first use). */
	public R footnoteRef(String footnoteText, String font, int halfPts) throws Exception {
		if (footnotesXml == null) {
			footnotesXml = new StringBuilder();
			DocumentSettingsPart dsp = mdp.getDocumentSettingsPart();
			org.docx4j.wml.CTFtnDocProps fp = F.createCTFtnDocProps();
			org.docx4j.wml.CTFtnEdnSepRef sep = F.createCTFtnEdnSepRef(); sep.setId(BigInteger.valueOf(-1));
			org.docx4j.wml.CTFtnEdnSepRef cont = F.createCTFtnEdnSepRef(); cont.setId(BigInteger.ZERO);
			fp.getFootnote().add(sep); fp.getFootnote().add(cont);
			dsp.getContents().setFootnotePr(fp);
		}
		footnoteCounter++;
		String rpr = "<w:rPr><w:rFonts w:ascii=\"" + font + "\" w:hAnsi=\"" + font + "\" w:cs=\"" + font + "\"/><w:sz w:val=\"" + halfPts + "\"/><w:szCs w:val=\"" + halfPts + "\"/></w:rPr>";
		footnotesXml.append("<w:footnote w:id=\"" + footnoteCounter + "\"><w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteRef/></w:r>"
				+ "<w:r>" + rpr + "<w:t xml:space=\"preserve\"> " + footnoteText.replace("&", "&amp;").replace("<", "&lt;") + "</w:t></w:r></w:p></w:footnote>");
		R r = F.createR();
		RPr rp = F.createRPr();
		CTVerticalAlignRun va = F.createCTVerticalAlignRun(); va.setVal(STVerticalAlignRun.SUPERSCRIPT); rp.setVertAlign(va);
		r.setRPr(rp);
		org.docx4j.wml.CTFtnEdnRef ref = F.createCTFtnEdnRef();
		ref.setId(BigInteger.valueOf(footnoteCounter));
		r.getContent().add(F.createRFootnoteReference(ref));
		return r;
	}

	/** Must be called after all footnoteRef() calls: writes the footnotes part. */
	public void finishFootnotes() throws Exception {
		if (footnotesXml == null) return;
		String xml = "<w:footnotes xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ footnotesXml + "</w:footnotes>";
		org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart fp = new org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart();
		Object o = XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.wml.CTFootnotes.class);
		if (o instanceof jakarta.xml.bind.JAXBElement) o = ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
		fp.setJaxbElement((org.docx4j.wml.CTFootnotes) o);
		mdp.addTargetPart(fp);
	}

		public void addFooter(String font, int halfPts, String... lines) throws Exception {
		addFooter(HdrFtrRef.DEFAULT, font, halfPts, lines);
	}

	public void addFooter(HdrFtrRef type, String font, int halfPts, String... lines) throws Exception {
		if (type == HdrFtrRef.FIRST) sectPr().setTitlePg(new BooleanDefaultTrue());
		if (type == HdrFtrRef.EVEN) evenAndOddHeaders();
		hdrFtrCounter++;
		FooterPart fp = new FooterPart(new PartName("/word/footer" + hdrFtrCounter + ".xml"));
		Ftr ftr = F.createFtr();
		for (String line : lines) {
			ftr.getContent().add(plainParagraph(line, font, halfPts));
		}
		fp.setJaxbElement(ftr);
				Relationship rel = mdp.addTargetPart(fp);
		FooterReference ref = F.createFooterReference();
		ref.setId(rel.getId());
		ref.setType(type);
		sectPr().getEGHdrFtrReferences().add(ref);
	}

	public static P plainParagraph(String text, String font, int halfPts) {
		P p = F.createP();
		PPr ppr = F.createPPr();
		PPrBase.Spacing sp = F.createPPrBaseSpacing();
		sp.setBefore(BigInteger.ZERO);
		sp.setAfter(BigInteger.ZERO);
		sp.setLine(BigInteger.valueOf(240));
		sp.setLineRule(STLineSpacingRule.AUTO);
		ppr.setSpacing(sp);
		p.setPPr(ppr);
		p.getContent().add(run(text, font, halfPts, null));
		return p;
	}

	static R run(String text, String font, int halfPts, Consumer<RPr> customiser) {
		R r = F.createR();
		RPr rpr = F.createRPr();
		RFonts rf = F.createRFonts();
		rf.setAscii(font);
		rf.setHAnsi(font);
		rf.setCs(font);
		rf.setEastAsia(font);
		rpr.setRFonts(rf);
		HpsMeasure sz = F.createHpsMeasure();
		sz.setVal(BigInteger.valueOf(halfPts));
		rpr.setSz(sz);
		rpr.setSzCs(sz);
		if (customiser != null) customiser.accept(rpr);
		r.setRPr(rpr);
		Text t = F.createText();
		t.setValue(text);
		t.setSpace("preserve");
		r.getContent().add(t);
		return r;
	}

	public static void superscript(RPr rpr) {
		CTVerticalAlignRun va = F.createCTVerticalAlignRun();
		va.setVal(STVerticalAlignRun.SUPERSCRIPT);
		rpr.setVertAlign(va);
	}

	public static void bold(RPr rpr) {
		rpr.setB(new BooleanDefaultTrue());
	}

	/** w:spacing on the run: expanded (positive) or condensed character spacing, in twentieths of a point. */
	public static java.util.function.Consumer<RPr> charSpacing(int twentieths) {
		return rpr -> {
			org.docx4j.wml.CTSignedTwipsMeasure sp = F.createCTSignedTwipsMeasure();
			sp.setVal(BigInteger.valueOf(twentieths));
			rpr.setSpacing(sp);
		};
	}

	/** w:kern: kern this run if its size is at least this many half-points. */
	public static java.util.function.Consumer<RPr> kern(int halfPts) {
		return rpr -> {
			HpsMeasure k = F.createHpsMeasure();
			k.setVal(BigInteger.valueOf(halfPts));
			rpr.setKern(k);
		};
	}

	// ---------------------------------------------------------------- images

	/**
	 * A generated PNG (gradient with a border and a diagonal), placed inline at the
	 * given width in twips (docx4j's createImageInline takes twips, not EMU; height
	 * follows the pixel aspect ratio).
	 */
	public R inlineImage(int wPx, int hPx, long cxTwips) throws Exception {
		imageCounter++;
		BufferedImage img = new BufferedImage(wPx, hPx, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		for (int x = 0; x < wPx; x++) {
			int v = 80 + (x * 150) / Math.max(1, wPx);
			g.setColor(new Color(v, v, 220));
			g.drawLine(x, 0, x, hPx);
		}
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, wPx - 1, hPx - 1);
		g.drawLine(0, 0, wPx - 1, hPx - 1);
		g.dispose();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", bos);
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(pkg, mdp, bos.toByteArray());
		Inline inline = imagePart.createImageInline("probe" + imageCounter, "probe image " + imageCounter,
				imageCounter, imageCounter + 100, cxTwips, false);
		R r = F.createR();
		Drawing d = F.createDrawing();
		d.getAnchorOrInline().add(inline);
		r.getContent().add(d);
		return r;
	}

	// ---------------------------------------------------------------- tables

	public static final class Table {
		final Tbl tbl = F.createTbl();
		final TblPr tblPr = F.createTblPr();
		final int[] colTwips;

		public Table(int... colTwips) {
			this.colTwips = colTwips;
			tbl.setTblPr(tblPr);
			int total = 0;
			TblGrid grid = F.createTblGrid();
			for (int w : colTwips) {
				TblGridCol c = F.createTblGridCol();
				c.setW(BigInteger.valueOf(w));
				grid.getGridCol().add(c);
				total += w;
			}
			tbl.setTblGrid(grid);
			tblPr.setTblW(width(total, "dxa"));
			borders(4);
		}

		public Table fixedLayout() {
			CTTblLayoutType lt = F.createCTTblLayoutType();
			lt.setType(STTblLayoutType.FIXED);
			tblPr.setTblLayout(lt);
			return this;
		}

				public Table autoWidth() {
			tblPr.setTblW(width(0, "auto"));
			return this;
		}

		/** w:tblW of the given type: "dxa" (twips) or "pct" (fiftieths of a percent, 5000 = 100%). */
		public Table tableWidth(int w, String type) {
			tblPr.setTblW(width(w, type));
			return this;
		}

		/** w:tblCellSpacing (twips): Word's separate-borders model. */
		public Table cellSpacing(int twips) {
			tblPr.setTblCellSpacing(width(twips, "dxa"));
			return this;
		}

		/**
		 * A floating table (w:tblpPr) at the position Word's Table Properties dialog
		 * states: the anchors name the frame ("text", "margin" or "page"), and each of
		 * x and y is either an offset in twips or a *Spec ("left"/"center"/"right",
		 * "top"/"center"/"bottom"). Pass null for the ones the docx should not carry.
		 */
		public Table floating(String vertAnchor, String horzAnchor,
				Integer xTwips, String xSpec, Integer yTwips, String ySpec) {
			org.docx4j.wml.CTTblPPr pp = F.createCTTblPPr();
			if (vertAnchor != null) pp.setVertAnchor(org.docx4j.wml.STVAnchor.fromValue(vertAnchor));
			if (horzAnchor != null) pp.setHorzAnchor(org.docx4j.wml.STHAnchor.fromValue(horzAnchor));
			if (xTwips != null) pp.setTblpX(BigInteger.valueOf(xTwips));
			if (xSpec != null) pp.setTblpXSpec(org.docx4j.wml.STXAlign.fromValue(xSpec));
			if (yTwips != null) pp.setTblpY(BigInteger.valueOf(yTwips));
			if (ySpec != null) pp.setTblpYSpec(org.docx4j.wml.STYAlign.fromValue(ySpec));
			pp.setLeftFromText(BigInteger.valueOf(180));
			pp.setRightFromText(BigInteger.valueOf(180));
			tblPr.setTblpPr(pp);
			return this;
		}

		/** A floating table (w:tblpPr) positioned relative to the margin, with text wrapping around it. */
		public Table floating(int xTwips, int yTwips) {
			org.docx4j.wml.CTTblPPr pp = F.createCTTblPPr();
			pp.setHorzAnchor(org.docx4j.wml.STHAnchor.MARGIN);
			pp.setVertAnchor(org.docx4j.wml.STVAnchor.TEXT);
			pp.setTblpX(BigInteger.valueOf(xTwips));
			pp.setTblpY(BigInteger.valueOf(yTwips));
			pp.setLeftFromText(BigInteger.valueOf(180));
			pp.setRightFromText(BigInteger.valueOf(180));
			pp.setTopFromText(BigInteger.valueOf(0));
			pp.setBottomFromText(BigInteger.valueOf(0));
			tblPr.setTblpPr(pp);
			return this;
		}

		/** A row from prepared cells (see {@link #cell}); optional height rule. */
		public Table rowOf(Integer heightTwips, org.docx4j.wml.STHeightRule rule, Tc... cells) {
			Tr tr = F.createTr();
			if (heightTwips != null) {
				org.docx4j.wml.TrPr trPr = F.createTrPr();
				org.docx4j.wml.CTHeight h = F.createCTHeight();
				h.setVal(BigInteger.valueOf(heightTwips));
				if (rule != null) h.setHRule(rule);
				trPr.getCnfStyleOrDivIdOrGridBefore().add(F.createCTTrPrBaseTrHeight(h));
				tr.setTrPr(trPr);
			}
			for (Tc tc : cells) tr.getContent().add(tc);
			tbl.getContent().add(tr);
			return this;
		}

		/** A cell with one paragraph; span > 1 sets w:gridSpan; widthTwips null = auto. */
		public Tc cell(String text, String font, int halfPts, int span, Integer widthTwips) {
			Tc tc = F.createTc();
			TcPr tcPr = F.createTcPr();
			tcPr.setTcW(widthTwips == null ? width(0, "auto") : width(widthTwips, "dxa"));
			if (span > 1) {
				org.docx4j.wml.TcPrInner.GridSpan gs = F.createTcPrInnerGridSpan();
				gs.setVal(BigInteger.valueOf(span));
				tcPr.setGridSpan(gs);
			}
			tc.setTcPr(tcPr);
			tc.getContent().add(plainParagraph(text, font, halfPts));
			return tc;
		}

		/** A cell holding prepared paragraphs; the cell's width is auto. */
		public Tc cellOf(P... paragraphs) {
			Tc tc = F.createTc();
			TcPr tcPr = F.createTcPr();
			tcPr.setTcW(width(0, "auto"));
			tc.setTcPr(tcPr);
			for (P p : paragraphs) tc.getContent().add(p);
			return tc;
		}

		/**
		 * A cell holding prepared paragraphs, with an explicit width in twips and an
		 * optional w:vAlign ("top", "center" or "bottom"; null writes none).
		 */
		public Tc cellOf(Integer widthTwips, String vAlign, P... paragraphs) {
			Tc tc = F.createTc();
			TcPr tcPr = F.createTcPr();
			tcPr.setTcW(widthTwips == null ? width(0, "auto") : width(widthTwips, "dxa"));
			if (vAlign != null) {
				org.docx4j.wml.CTVerticalJc jc = F.createCTVerticalJc();
				jc.setVal(org.docx4j.wml.STVerticalJc.fromValue(vAlign));
				tcPr.setVAlign(jc);
			}
			tc.setTcPr(tcPr);
			for (P p : paragraphs) tc.getContent().add(p);
			return tc;
		}

		/** A cell holding a nested table (plus the mandatory trailing paragraph). */
		public Tc cellWith(Tbl nested, String after, String font, int halfPts) {
			Tc tc = F.createTc();
			TcPr tcPr = F.createTcPr();
			tcPr.setTcW(width(0, "auto"));
			tc.setTcPr(tcPr);
			tc.getContent().add(nested);
			tc.getContent().add(plainParagraph(after, font, halfPts));
			return tc;
		}

		public Table indent(int twips) {
			tblPr.setTblInd(width(twips, "dxa"));
			return this;
		}

		public Table borders(int eighthsOfPoint) {
			TblBorders b = F.createTblBorders();
			b.setTop(border(eighthsOfPoint));
			b.setLeft(border(eighthsOfPoint));
			b.setBottom(border(eighthsOfPoint));
			b.setRight(border(eighthsOfPoint));
			b.setInsideH(border(eighthsOfPoint));
			b.setInsideV(border(eighthsOfPoint));
			tblPr.setTblBorders(b);
			return this;
		}

		/** w:tblBorders with every side w:val="none": a table which draws nothing, so
		 *  nothing can be charged against the cells' text measure either. */
		public Table noBorders() {
			TblBorders b = F.createTblBorders();
			b.setTop(noBorder());
			b.setLeft(noBorder());
			b.setBottom(noBorder());
			b.setRight(noBorder());
			b.setInsideH(noBorder());
			b.setInsideV(noBorder());
			tblPr.setTblBorders(b);
			return this;
		}

		private static CTBorder noBorder() {
			CTBorder b = F.createCTBorder();
			b.setVal(STBorder.NONE);
			b.setSz(BigInteger.ZERO);
			b.setSpace(BigInteger.ZERO);
			return b;
		}

		public Table cellMargins(int leftRightTwips, int topBottomTwips) {
			CTTblCellMar m = F.createCTTblCellMar();
			m.setLeft(width(leftRightTwips, "dxa"));
			m.setRight(width(leftRightTwips, "dxa"));
			m.setTop(width(topBottomTwips, "dxa"));
			m.setBottom(width(topBottomTwips, "dxa"));
			tblPr.setTblCellMar(m);
			return this;
		}

		/** One row; each cell gets one paragraph in the given font/size; null widths mean "auto". */
		public Table row(String font, int halfPts, boolean autoCellWidths, String... cellTexts) {
			Tr tr = F.createTr();
			for (int i = 0; i < cellTexts.length; i++) {
				Tc tc = F.createTc();
				TcPr tcPr = F.createTcPr();
				tcPr.setTcW(autoCellWidths ? width(0, "auto") : width(colTwips[Math.min(i, colTwips.length - 1)], "dxa"));
				tc.setTcPr(tcPr);
				tc.getContent().add(plainParagraph(cellTexts[i], font, halfPts));
				tr.getContent().add(tc);
			}
			tbl.getContent().add(tr);
			return this;
		}

		/**
		 * A row whose cells declare a {@code w:tcW} in <b>pct</b> - fiftieths of a per
		 * cent of the table's own width - rather than in dxa or auto.  A table can then
		 * state column proportions which disagree with its {@code w:tblGrid}, which is
		 * the only way to read off which of the two Word lays the table out on.
		 */
		public Table rowPct(String font, int halfPts, int[] pcts, String... cellTexts) {
			return rowOfWidths(font, halfPts, "pct", pcts, cellTexts);
		}

		/** A row whose cells declare a {@code w:tcW} in dxa which need not be the grid's. */
		public Table rowDxa(String font, int halfPts, int[] twips, String... cellTexts) {
			return rowOfWidths(font, halfPts, "dxa", twips, cellTexts);
		}

		private Table rowOfWidths(String font, int halfPts, String type, int[] widths,
				String... cellTexts) {
			Tr tr = F.createTr();
			for (int i = 0; i < cellTexts.length; i++) {
				Tc tc = F.createTc();
				TcPr tcPr = F.createTcPr();
				tcPr.setTcW(width(widths[Math.min(i, widths.length - 1)], type));
				tc.setTcPr(tcPr);
				tc.getContent().add(plainParagraph(cellTexts[i], font, halfPts));
				tr.getContent().add(tc);
			}
			tbl.getContent().add(tr);
			return this;
		}

		public Tbl build() {
			return tbl;
		}

		/**
		 * The cell's own w:tcMar left and right, in twips, overriding the table's
		 * w:tblCellMar; null leaves that side to the table.  Varying one cell's left
		 * margin against another's is how a probe puts three different text measures in
		 * one column, and so how it reads off which of them Word actually used.
		 */
		public static Tc tcMargins(Tc tc, Integer leftTwips, Integer rightTwips) {
			TcPr tcPr = tc.getTcPr();
			if (tcPr == null) {
				tcPr = F.createTcPr();
				tc.setTcPr(tcPr);
			}
			org.docx4j.wml.TcMar m = tcPr.getTcMar();
			if (m == null) {
				m = F.createTcMar();
				tcPr.setTcMar(m);
			}
			if (leftTwips != null) m.setLeft(width(leftTwips, "dxa"));
			if (rightTwips != null) m.setRight(width(rightTwips, "dxa"));
			return tc;
		}

		private static CTBorder border(int sz) {
			CTBorder b = F.createCTBorder();
			b.setVal(STBorder.SINGLE);
			b.setSz(BigInteger.valueOf(sz));
			b.setColor("000000");
			b.setSpace(BigInteger.ZERO);
			return b;
		}

		private static TblWidth width(int w, String type) {
			TblWidth tw = F.createTblWidth();
			tw.setW(BigInteger.valueOf(w));
			tw.setType(type);
			return tw;
		}
	}

	// ---------------------------------------------------------------- paragraph builder

	public static final class Para {
		private final Doc doc;
		private final P p = F.createP();
		private final PPr ppr = F.createPPr();
		private final PPrBase.Spacing sp = F.createPPrBaseSpacing();
		private String font = SERIF;
		private int halfPts = 24;
		private boolean label = true;
		/** Runs are materialised in build(), so font() applies to text() calls made before it. */
		private final java.util.List<Object[]> parts = new java.util.ArrayList<>();

		Para(Doc doc) {
			this.doc = doc;
			p.setPPr(ppr);
			ppr.setSpacing(sp);
			sp.setBefore(BigInteger.ZERO);
			sp.setAfter(BigInteger.ZERO);
			sp.setLine(BigInteger.valueOf(240));
			sp.setLineRule(STLineSpacingRule.AUTO);
		}

		/** The paragraph font, used for the label and for text() runs (wherever it is called in the chain). */
		public Para font(String font, int halfPts) {
			this.font = font;
			this.halfPts = halfPts;
			return this;
		}

		public Para noLabel() {
			label = false;
			return this;
		}

		/** Text in the paragraph font. */
		public Para text(String text) {
			parts.add(new Object[] { text, null, null, null });
			return this;
		}

		/** Text in an explicit font/size, optionally customised. */
		@SuppressWarnings("unchecked")
		public Para run(String text, String font, int halfPts, Consumer<RPr> customiser) {
			parts.add(new Object[] { text, font, halfPts, customiser });
			return this;
		}

		public Para before(int twips) {
			sp.setBefore(BigInteger.valueOf(twips));
			return this;
		}

		public Para after(int twips) {
			sp.setAfter(BigInteger.valueOf(twips));
			return this;
		}

		public Para line(int twentieths, STLineSpacingRule rule) {
			sp.setLine(BigInteger.valueOf(twentieths));
			sp.setLineRule(rule);
			return this;
		}

				/** Remove w:line entirely (inherit from style / docDefaults). */
		public Para noLine() {
			sp.setLine(null);
			sp.setLineRule(null);
			return this;
		}

		/** No direct w:spacing at all: before/after/line come from the style and docDefaults. */
		public Para inheritSpacing() {
			ppr.setSpacing(null);
			return this;
		}

		/** Make this a list item of the default decimal list (numId 1, level 0). */
		public Para listItem() throws Exception {
			doc.ensureNumbering();
			PPrBase.NumPr numPr = F.createPPrBaseNumPr();
			PPrBase.NumPr.Ilvl ilvl = F.createPPrBaseNumPrIlvl();
			ilvl.setVal(BigInteger.ZERO);
			numPr.setIlvl(ilvl);
			PPrBase.NumPr.NumId numId = F.createPPrBaseNumPrNumId();
			numId.setVal(BigInteger.ONE);
			numPr.setNumId(numId);
			ppr.setNumPr(numPr);
			return this;
		}

		/**
		 * Direct w:numPr on the paragraph: {@code numId} names a w:num of the numbering
		 * part (0 is Word's "this paragraph is not numbered", which a numbered style's
		 * numbering is switched off with), and {@code ilvl} is written only when it is
		 * not null - a w:numPr with no w:ilvl is the shape Word writes for a paragraph
		 * whose level comes from its style, and whether Word then reads it as level 0 is
		 * the whole question of the numbering-label probe.
		 */
		public Para numPr(int numId, Integer ilvl) {
			PPrBase.NumPr numPr = F.createPPrBaseNumPr();
			if (ilvl != null) {
				PPrBase.NumPr.Ilvl lvl = F.createPPrBaseNumPrIlvl();
				lvl.setVal(BigInteger.valueOf(ilvl));
				numPr.setIlvl(lvl);
			}
			PPrBase.NumPr.NumId id = F.createPPrBaseNumPrNumId();
			id.setVal(BigInteger.valueOf(numId));
			numPr.setNumId(id);
			ppr.setNumPr(numPr);
			return this;
		}

		/** No direct w:ind at all: the indents come from the style, and from the
		 *  numbering level where one applies. */
		public Para noIndent() {
			ppr.setInd(null);
			return this;
		}

		public Para autospacing(boolean before, boolean after) {
			sp.setBeforeAutospacing(before ? Boolean.TRUE : null);
			sp.setAfterAutospacing(after ? Boolean.TRUE : null);
			return this;
		}

		public Para contextual() {
			ppr.setContextualSpacing(new BooleanDefaultTrue());
			return this;
		}

		public Para style(String styleId) {
			PPrBase.PStyle ps = F.createPPrBasePStyle();
			ps.setVal(styleId);
			ppr.setPStyle(ps);
			return this;
		}

		public Para jc(JcEnumeration val) {
			Jc jc = F.createJc();
			jc.setVal(val);
			ppr.setJc(jc);
			return this;
		}

		public Para indent(int leftTwips, int firstLineTwips, int hangingTwips) {
			PPrBase.Ind ind = F.createPPrBaseInd();
			ind.setLeft(BigInteger.valueOf(leftTwips));
			if (firstLineTwips > 0) ind.setFirstLine(BigInteger.valueOf(firstLineTwips));
			if (hangingTwips > 0) ind.setHanging(BigInteger.valueOf(hangingTwips));
			ppr.setInd(ind);
			return this;
		}

				public Para keepNext() {
			ppr.setKeepNext(new BooleanDefaultTrue());
			return this;
		}

		public Para widowControl(boolean on) {
			BooleanDefaultTrue b = new BooleanDefaultTrue();
			b.setVal(on);
			ppr.setWidowControl(b);
			return this;
		}

		/** Append a prepared run (an image, a footnote reference) after the text. */
		public Para run(R r) {
			parts.add(new Object[] { r, null, null, null });
			return this;
		}

		/** Paragraph-mark run properties (font size of the pilcrow), which Word counts towards line height. */
		public Para markSize(int halfPts) {
			ParaRPr rpr = F.createParaRPr();
			HpsMeasure sz = F.createHpsMeasure();
			sz.setVal(BigInteger.valueOf(halfPts));
			rpr.setSz(sz);
			rpr.setSzCs(sz);
			ppr.setRPr(rpr);
			return this;
		}

		public Para pageBreakBefore() {
			ppr.setPageBreakBefore(new BooleanDefaultTrue());
			return this;
		}

		/** A run holding one w:br w:type="page", inside this paragraph: two of them in a
		 *  row is the shape a corpus document's near-empty page comes from. */
		public Para pageBreakRun() {
			R r = F.createR();
			Br br = F.createBr();
			br.setType(STBrType.PAGE);
			r.getContent().add(br);
			return run(r);
		}

		/** A run holding one w:br with no type: a soft return, which ends the line
		 *  without ending the paragraph. */
		public Para softReturn() {
			R r = F.createR();
			r.getContent().add(F.createBr());
			return run(r);
		}

		/** A run holding one w:br w:type="column": where Word divides the columns. */
		public Para columnBreak() {
			R r = F.createR();
			Br br = F.createBr();
			br.setType(STBrType.COLUMN);
			r.getContent().add(br);
			return run(r);
		}

		/** A run holding one w:tab and <b>no w:rPr at all</b>, which is how Word writes a
		 *  tab typed between two runs: its font is whatever a bare run inherits. */
		public Para tab() {
			R r = F.createR();
			r.getContent().add(F.createRTab());
			return run(r);
		}

		/** A run holding one w:tab with an explicit font and size of its own. */
		public Para tab(String font, int halfPts) {
			R r = F.createR();
			RPr rpr = F.createRPr();
			Doc.font(font, halfPts).accept(rpr);
			r.setRPr(rpr);
			r.getContent().add(F.createRTab());
			return run(r);
		}

		/** Text in a run with <b>no w:rPr at all</b>: its font and size are the ones the
		 *  style and the document defaults give it. */
		public Para bareText(String text) {
			R r = F.createR();
			Text t = F.createText();
			t.setValue(text);
			t.setSpace("preserve");
			r.getContent().add(t);
			return run(r);
		}

		/** A custom tab stop: position in twips from the left margin, and its alignment. */
		public Para tabStop(int posTwips, org.docx4j.wml.STTabJc align) {
			return tabStop(posTwips, align, org.docx4j.wml.STTabTlc.NONE);
		}

		/** A custom tab stop with a leader (w:leader: dot, hyphen, underscore, ...). */
		public Para tabStop(int posTwips, org.docx4j.wml.STTabJc align, org.docx4j.wml.STTabTlc leader) {
			if (ppr.getTabs() == null) ppr.setTabs(F.createTabs());
			org.docx4j.wml.CTTabStop stop = F.createCTTabStop();
			stop.setPos(BigInteger.valueOf(posTwips));
			stop.setVal(align);
			stop.setLeader(leader);
			ppr.getTabs().getTab().add(stop);
			return this;
		}

		/** Append a prepared content object (a field, a bookmark) after the text. */
		public Para content(Object o) {
			parts.add(new Object[] { o, null, null, null });
			return this;
		}

		/** Wrap this paragraph's content in a bookmark, as a Word heading a table of
		 *  contents points at is wrapped. */
		public Para bookmark(String name) {
			this.bookmarkName = name;
			return this;
		}

		private String bookmarkName;

		/** A PAGEREF field, which is what an entry of a table of contents holds: the page
		 *  its bookmark is on, hyperlinked (\h).  The result cached in the file is
		 *  {@code cached}; the field is marked dirty, so Word refreshes it on open. */
		public Para pageref(String bookmarkName, String cached) {
			return content(Doc.pageref(bookmarkName, cached, font, halfPts));
		}

		/**
		 * w:pBdr on all four sides: sz in eighths of a point, w:space in points (the
		 * gap Word leaves between the text and the border).
		 */
		public Para borders(int eighthsOfPoint, int spacePt) {
			PPrBase.PBdr bdr = F.createPPrBasePBdr();
			bdr.setTop(paraBorder(eighthsOfPoint, spacePt));
			bdr.setLeft(paraBorder(eighthsOfPoint, spacePt));
			bdr.setBottom(paraBorder(eighthsOfPoint, spacePt));
			bdr.setRight(paraBorder(eighthsOfPoint, spacePt));
			ppr.setPBdr(bdr);
			return this;
		}

		private static CTBorder paraBorder(int sz, int spacePt) {
			CTBorder b = F.createCTBorder();
			b.setVal(STBorder.SINGLE);
			b.setSz(BigInteger.valueOf(sz));
			b.setColor("000000");
			b.setSpace(BigInteger.valueOf(spacePt));
			return b;
		}

		/** w:suppressAutoHyphens: this paragraph is never hyphenated. */
		public Para suppressAutoHyphens() {
			ppr.setSuppressAutoHyphens(new BooleanDefaultTrue());
			return this;
		}

		/** w:lang on every run of this paragraph. */
		public Para lang(String lang) {
			this.lang = lang;
			return this;
		}

		private String lang;

		@SuppressWarnings("unchecked")
		public P build() {
			Consumer<RPr> langCustomiser = lang == null ? null
					: rpr -> rpr.setLang(Doc.language(lang));
			if (label) {
				p.getContent().add(Doc.run(doc.nextLabel(), font, halfPts, langCustomiser));
			}
						for (Object[] part : parts) {
				if (!(part[0] instanceof String)) {
					p.getContent().add(part[0]);
					continue;
				}
				String f = part[1] == null ? font : (String) part[1];
				int sz = part[2] == null ? halfPts : (Integer) part[2];
				Consumer<RPr> c = (Consumer<RPr>) part[3];
				Consumer<RPr> customiser = langCustomiser == null ? c
						: (c == null ? langCustomiser : langCustomiser.andThen(c));
				p.getContent().add(Doc.run((String) part[0], f, sz, customiser));
			}
			if (bookmarkName != null) {
				org.docx4j.wml.CTBookmark bm = F.createCTBookmark();
				bm.setId(BigInteger.valueOf(doc.nextBookmarkId()));
				bm.setName(bookmarkName);
				p.getContent().add(0, F.createPBookmarkStart(bm));
				org.docx4j.wml.CTMarkupRange end = F.createCTMarkupRange();
				end.setId(bm.getId());
				p.getContent().add(F.createPBookmarkEnd(end));
				bookmarkName = null;
			}
			parts.clear();
			label = false; // a second build() must not add another label
			return p;
		}

		public P add() {
			return doc.add(build());
		}
	}
}
