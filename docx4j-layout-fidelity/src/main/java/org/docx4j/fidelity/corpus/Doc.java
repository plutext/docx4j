package org.docx4j.fidelity.corpus;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
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

	public MainDocumentPart mdp() {
		return mdp;
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

	// ---------------------------------------------------------------- styles

	public void addParagraphStyle(String styleId, String basedOn, Consumer<PPr> pPrCustomiser) {
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
		pPrCustomiser.accept(ppr);
		s.setPPr(ppr);
		mdp.getStyleDefinitionsPart().getJaxbElement().getStyle().add(s);
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

	static P plainParagraph(String text, String font, int halfPts) {
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

		public Tbl build() {
			return tbl;
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

		@SuppressWarnings("unchecked")
		public P build() {
			if (label) {
				p.getContent().add(Doc.run(doc.nextLabel(), font, halfPts, null));
			}
						for (Object[] part : parts) {
				if (part[0] instanceof R) {
					p.getContent().add((R) part[0]);
					continue;
				}
				String f = part[1] == null ? font : (String) part[1];
				int sz = part[2] == null ? halfPts : (Integer) part[2];
				p.getContent().add(Doc.run((String) part[0], f, sz, (Consumer<RPr>) part[3]));
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
