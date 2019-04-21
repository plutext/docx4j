package org.docx4j.model;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;

/**
 * Property resolver which assumes that style properties will not be modified
 * (it works by actually copying the property objects, so it can be used only 
 *  when they won't be modified).
 * 
 *  @author Adam Schmideg
 *  
 *  @see also org.docx4j.model.styles.StyleUtil.apply methods
 */
public class ImmutablePropertyResolver extends PropertyResolver {

	public ImmutablePropertyResolver(WordprocessingMLPackage wordMLPackage)
			throws Docx4JException {
		super(wordMLPackage);
	}

//	public ImmutablePropertyResolver(StyleDefinitionsPart styleDefinitionsPart,
//			ThemePart themePart,
//			NumberingDefinitionsPart numberingDefinitionsPart)
//			throws Docx4JException {
//		super(styleDefinitionsPart, themePart, numberingDefinitionsPart);
//	}
	


	protected void applyPPr(PPr src, PPr dest) {
		if (src == null) {
			return;
		}
		// not copying: class, parent, rpr
		if (src.getAdjustRightInd() != null)
			dest.setAdjustRightInd(src.getAdjustRightInd());
		if (src.getAutoSpaceDE() != null)
			dest.setAutoSpaceDE(src.getAutoSpaceDE());
		if (src.getAutoSpaceDN() != null)
			dest.setAutoSpaceDN(src.getAutoSpaceDN());
		if (src.getBidi() != null)
			dest.setBidi(src.getBidi());
		if (src.getCnfStyle() != null)
			dest.setCnfStyle(src.getCnfStyle());
		if (src.getContextualSpacing() != null)
			dest.setContextualSpacing(src.getContextualSpacing());
		if (src.getDivId() != null)
			dest.setDivId(src.getDivId());
		if (src.getFramePr() != null)
			dest.setFramePr(src.getFramePr());
		if (src.getInd() != null)
			dest.setInd(src.getInd());
		if (src.getJc() != null)
			dest.setJc(src.getJc());
		if (src.getKeepLines() != null)
			dest.setKeepLines(src.getKeepLines());
		if (src.getKeepNext() != null)
			dest.setKeepNext(src.getKeepNext());
		if (src.getKinsoku() != null)
			dest.setKinsoku(src.getKinsoku());
		if (src.getMirrorIndents() != null)
			dest.setMirrorIndents(src.getMirrorIndents());
		if (src.getNumPr() != null)
			dest.setNumPr(src.getNumPr());
		if (src.getOutlineLvl() != null)
			dest.setOutlineLvl(src.getOutlineLvl());
		if (src.getOverflowPunct() != null)
			dest.setOverflowPunct(src.getOverflowPunct());
		if (src.getPageBreakBefore() != null)
			dest.setPageBreakBefore(src.getPageBreakBefore());
		if (src.getPBdr() != null)
			dest.setPBdr(src.getPBdr());
		if (src.getPPrChange() != null)
			dest.setPPrChange(src.getPPrChange());
		if (src.getPStyle() != null)
			dest.setPStyle(src.getPStyle());
		if (src.getSectPr() != null)
			dest.setSectPr(src.getSectPr());
		if (src.getShd() != null)
			dest.setShd(src.getShd());
		if (src.getSnapToGrid() != null)
			dest.setSnapToGrid(src.getSnapToGrid());
		if (src.getSpacing() != null)
			dest.setSpacing(src.getSpacing());
		if (src.getSuppressAutoHyphens() != null)
			dest.setSuppressAutoHyphens(src.getSuppressAutoHyphens());
		if (src.getSuppressLineNumbers() != null)
			dest.setSuppressLineNumbers(src.getSuppressLineNumbers());
		if (src.getSuppressOverlap() != null)
			dest.setSuppressOverlap(src.getSuppressOverlap());
		if (src.getTabs() != null)
			dest.setTabs(src.getTabs());
		if (src.getTextAlignment() != null)
			dest.setTextAlignment(src.getTextAlignment());
		if (src.getTextboxTightWrap() != null)
			dest.setTextboxTightWrap(src.getTextboxTightWrap());
		if (src.getTextDirection() != null)
			dest.setTextDirection(src.getTextDirection());
		if (src.getTopLinePunct() != null)
			dest.setTopLinePunct(src.getTopLinePunct());
		if (src.getWidowControl() != null)
			dest.setWidowControl(src.getWidowControl());
		if (src.getWordWrap() != null)
			dest.setWordWrap(src.getWordWrap());
	}

	protected void applyRPr(RPr src, RPr dest) {
		if (src == null) {
			return;
		}
		// not copying: class, parent
		if (src.getB() != null)
			dest.setB(src.getB());
		if (src.getBCs() != null)
			dest.setBCs(src.getBCs());
		if (src.getBdr() != null)
			dest.setBdr(src.getBdr());
		if (src.getCaps() != null)
			dest.setCaps(src.getCaps());
		if (src.getColor() != null)
			dest.setColor(src.getColor());
		if (src.getCs() != null)
			dest.setCs(src.getCs());
		if (src.getDstrike() != null)
			dest.setDstrike(src.getDstrike());
		if (src.getEastAsianLayout() != null)
			dest.setEastAsianLayout(src.getEastAsianLayout());
		if (src.getEffect() != null)
			dest.setEffect(src.getEffect());
		if (src.getEm() != null)
			dest.setEm(src.getEm());
		if (src.getEmboss() != null)
			dest.setEmboss(src.getEmboss());
		if (src.getFitText() != null)
			dest.setFitText(src.getFitText());
		if (src.getHighlight() != null)
			dest.setHighlight(src.getHighlight());
		if (src.getI() != null)
			dest.setI(src.getI());
		if (src.getICs() != null)
			dest.setICs(src.getICs());
		if (src.getImprint() != null)
			dest.setImprint(src.getImprint());
		if (src.getKern() != null)
			dest.setKern(src.getKern());
		if (src.getLang() != null)
			dest.setLang(src.getLang());
		if (src.getNoProof() != null)
			dest.setNoProof(src.getNoProof());
		if (src.getOMath() != null)
			dest.setOMath(src.getOMath());
		if (src.getOutline() != null)
			dest.setOutline(src.getOutline());
		if (src.getPosition() != null)
			dest.setPosition(src.getPosition());
		if (src.getRFonts() != null)
			dest.setRFonts(src.getRFonts());
		if (src.getRPrChange() != null)
			dest.setRPrChange(src.getRPrChange());
		if (src.getRStyle() != null)
			dest.setRStyle(src.getRStyle());
		if (src.getRtl() != null)
			dest.setRtl(src.getRtl());
		if (src.getShadow() != null)
			dest.setShadow(src.getShadow());
		if (src.getShd() != null)
			dest.setShd(src.getShd());
		if (src.getSmallCaps() != null)
			dest.setSmallCaps(src.getSmallCaps());
		if (src.getSnapToGrid() != null)
			dest.setSnapToGrid(src.getSnapToGrid());
		if (src.getSpacing() != null)
			dest.setSpacing(src.getSpacing());
		if (src.getSpecVanish() != null)
			dest.setSpecVanish(src.getSpecVanish());
		if (src.getStrike() != null)
			dest.setStrike(src.getStrike());
		if (src.getSz() != null)
			dest.setSz(src.getSz());
		if (src.getSzCs() != null)
			dest.setSzCs(src.getSzCs());
		if (src.getU() != null)
			dest.setU(src.getU());
		if (src.getVanish() != null)
			dest.setVanish(src.getVanish());
		if (src.getVertAlign() != null)
			dest.setVertAlign(src.getVertAlign());
		if (src.getW() != null)
			dest.setW(src.getW());
		if (src.getWebHidden() != null)
			dest.setWebHidden(src.getWebHidden());
	}
}
