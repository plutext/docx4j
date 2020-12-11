/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.sharedtypes.STOnOff;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTCnf;
import org.docx4j.wml.CTEm;
import org.docx4j.wml.CTFramePr;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTShortHexNumber;
import org.docx4j.wml.CTSignedHpsMeasure;
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.CTTblLook;
import org.docx4j.wml.CTTblOverlap;
import org.docx4j.wml.CTTblPPr;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblPrBase.TblStyle;
import org.docx4j.wml.CTTblPrBase.TblStyleColBandSize;
import org.docx4j.wml.CTTblPrBase.TblStyleRowBandSize;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.CTTextEffect;
import org.docx4j.wml.CTTextScale;
import org.docx4j.wml.CTTextboxTightWrap;
import org.docx4j.wml.CTTrPrBase.GridAfter;
import org.docx4j.wml.CTTrPrBase.GridBefore;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.Color;
import org.docx4j.wml.Highlight;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.OutlineLvl;
import org.docx4j.wml.PPrBase.PBdr;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.PPrBase.TextAlignment;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STDropCap;
import org.docx4j.wml.STEm;
import org.docx4j.wml.STHAnchor;
import org.docx4j.wml.STHeightRule;
import org.docx4j.wml.STHint;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STShd;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.STTblOverlap;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.STTextEffect;
import org.docx4j.wml.STTheme;
import org.docx4j.wml.STThemeColor;
import org.docx4j.wml.STVAnchor;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.STWrap;
import org.docx4j.wml.STXAlign;
import org.docx4j.wml.STYAlign;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Style.BasedOn;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.TcMar;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.HMerge;
import org.docx4j.wml.TcPrInner.TcBorders;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.TextDirection;
import org.docx4j.wml.TrPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.List;

/*
 *  @author Alberto Zerolo
 *  @since 3.0.0
 *  
*/
public class StyleUtil {
	
	protected static Logger log = LoggerFactory.getLogger(StyleUtil.class);	
	
	
	public static final String CHARACTER_STYLE = "character";
	public static final String PARAGRAPH_STYLE = "paragraph";
	public static final String TABLE_STYLE = "table";
	public static final String NUMBERING_STYLE = "numbering";

/////////////////////////////////////////////
// areEqual-Methods
/////////////////////////////////////////////
	public static boolean areEqual(Style style1, Style style2, boolean compareIDs) {
		if (style1 == style2) 
			return true;
		if (((style1 != null) && (style2 == null)) || ((style1 == null) && (style2 != null)))
			return false;
		
		if ((compareIDs && (!areEqual(style1.getStyleId(), style2.getStyleId()))) ||
			(!areEqual(style1.getType(), style2.getType()))) {
			return false;
		}
		if (CHARACTER_STYLE.equals(style1.getType())) {
			return areEqual(style1.getBasedOn(), style2.getBasedOn()) && 
			       areEqual(style1.getRPr(), style2.getRPr());
		}
		else if (PARAGRAPH_STYLE.equals(style1.getType()) || 
				 NUMBERING_STYLE.equals(style1.getType())) {
			return areEqual(style1.getBasedOn(), style2.getBasedOn()) &&
				   areEqual(style1.getRPr(), style2.getRPr()) &&
			       areEqual(style1.getPPr(), style2.getPPr());
		}
		else if (TABLE_STYLE.equals(style1.getType())) {
			return areEqual(style1.getBasedOn(), style2.getBasedOn()) &&
			       areEqual(style1.getTblPr(), style2.getTblPr()) &&
				   areEqual(style1.getTcPr(), style2.getTcPr()) &&
				   areEqual(style1.getTblStylePr(), style2.getTblStylePr());
			
		}
		throw new IllegalArgumentException("Invalid style type: " + style1.getType());
	}

	public static boolean areEqual(PPr pPr1, PPr pPr2) {
		return (pPr1 == pPr2) ||
			((pPr1 != null) && (pPr2 != null) &&
			  areEqual((PPrBase)pPr1, (PPrBase)pPr2) &&
			  areEqual(pPr1.getRPr(), pPr2.getRPr()) &&
			  areEqual(pPr1.getSectPr(), pPr2.getSectPr())			  
			);
	}
	
	/**
	 * @param sectPr1
	 * @param sectPr2
	 * @return
	 * @since 3.2
	 */
	public static boolean areEqual(SectPr sectPr1, SectPr sectPr2) {

		log.warn("TODO: implementation for SectPr is incomplete; contributions welcome");						
		
		return (sectPr1 == sectPr2) ||
				((sectPr1 != null) && (sectPr2 != null) &&	
						areEqual(sectPr1.getFormProt() , sectPr2.getFormProt()) &&
						areEqual(sectPr1.getVAlign() , sectPr2.getVAlign()) &&
						areEqual(sectPr1.getNoEndnote(), sectPr2.getNoEndnote()) &&
						areEqual(sectPr1.getTitlePg() , sectPr2.getTitlePg()) &&
						areEqual(sectPr1.getTextDirection(), sectPr2.getTextDirection()) &&
						areEqual(sectPr1.getBidi(), sectPr2.getBidi()) &&
						areEqual(sectPr1.getRtlGutter(), sectPr2.getRtlGutter()) 
						
// TODO						
//						areEqual(sectPr1.getDocGrid() , sectPr2.getDocGrid()) &&
//						areEqual(sectPr1.getEGHdrFtrReferences(), sectPr2.getEGHdrFtrReferences()) &&
//						areEqual(sectPr1.getFootnotePr(), sectPr2.getFootnotePr()) &&
//						areEqual(sectPr1.getEndnotePr(), sectPr2.getEndnotePr()) &&
//						areEqual(sectPr1.getType() , sectPr2.getType()) &&
//						areEqual(sectPr1.getPgSz() , sectPr2.getPgSz()) &&
//						areEqual(sectPr1.getPgMar(), sectPr2.getPgMar()) &&
//						areEqual(sectPr1.getPaperSrc(), sectPr2.getPaperSrc()) &&
//						areEqual(sectPr1.getPgBorders() , sectPr2.getPgBorders()) &&
//						areEqual(sectPr1.getLnNumType(), sectPr2.getLnNumType()) &&
//						areEqual(sectPr1.getPgNumType(), sectPr2.getPgNumType()) &&
//						areEqual(sectPr1.getCols() , sectPr2.getCols()) &&
//						areEqual(sectPr1.getPrinterSettings(), sectPr2.getPrinterSettings()) &&
//						areEqual(sectPr1.getFootnoteColumns(), sectPr2.getFootnoteColumns()) 
					  
					);
												
	}

	public static boolean areEqual(PPrBase pPrBase1, PPrBase pPrBase2) {
		return (pPrBase1 == pPrBase2) ||
			(   (pPrBase1 != null) && (pPrBase2 != null) &&
				areEqual(pPrBase1.getPStyle(), pPrBase2.getPStyle()) &&	
				areEqual(pPrBase1.getKeepNext(), pPrBase2.getKeepNext()) &&	
				areEqual(pPrBase1.getKeepLines(), pPrBase2.getKeepLines()) &&	
				areEqual(pPrBase1.getPageBreakBefore(), pPrBase2.getPageBreakBefore()) &&	
				areEqual(pPrBase1.getFramePr(), pPrBase2.getFramePr()) &&	
				areEqual(pPrBase1.getWidowControl(), pPrBase2.getWidowControl()) &&	
				areEqual(pPrBase1.getNumPr(), pPrBase2.getNumPr()) &&	
				areEqual(pPrBase1.getSuppressLineNumbers(), pPrBase2.getSuppressLineNumbers()) &&	
				areEqual(pPrBase1.getPBdr(), pPrBase2.getPBdr()) &&	
				areEqual(pPrBase1.getShd(), pPrBase2.getShd()) &&	
				areEqual(pPrBase1.getTabs(), pPrBase2.getTabs()) &&	
				areEqual(pPrBase1.getSuppressAutoHyphens(), pPrBase2.getSuppressAutoHyphens()) &&	
				areEqual(pPrBase1.getKinsoku(), pPrBase2.getKinsoku()) &&	
				areEqual(pPrBase1.getWordWrap(), pPrBase2.getWordWrap()) &&	
				areEqual(pPrBase1.getOverflowPunct(), pPrBase2.getOverflowPunct()) &&	
				areEqual(pPrBase1.getTopLinePunct(), pPrBase2.getTopLinePunct()) &&	
				areEqual(pPrBase1.getAutoSpaceDE(), pPrBase2.getAutoSpaceDE()) &&	
				areEqual(pPrBase1.getAutoSpaceDN(), pPrBase2.getAutoSpaceDN()) &&	
				areEqual(pPrBase1.getBidi(), pPrBase2.getBidi()) &&	
				areEqual(pPrBase1.getAdjustRightInd(), pPrBase2.getAdjustRightInd()) &&	
				areEqual(pPrBase1.getSnapToGrid(), pPrBase2.getSnapToGrid()) &&	
				areEqual(pPrBase1.getSpacing(), pPrBase2.getSpacing()) &&	
				areEqual(pPrBase1.getInd(), pPrBase2.getInd()) &&	
				areEqual(pPrBase1.getContextualSpacing(), pPrBase2.getContextualSpacing()) &&	
				areEqual(pPrBase1.getMirrorIndents(), pPrBase2.getMirrorIndents()) &&	
				areEqual(pPrBase1.getSuppressOverlap(), pPrBase2.getSuppressOverlap()) &&	
				areEqual(pPrBase1.getJc(), pPrBase2.getJc()) &&	
				areEqual(pPrBase1.getTextDirection(), pPrBase2.getTextDirection()) &&	
				areEqual(pPrBase1.getTextAlignment(), pPrBase2.getTextAlignment()) &&	
				areEqual(pPrBase1.getTextboxTightWrap(), pPrBase2.getTextboxTightWrap()) &&	
				areEqual(pPrBase1.getOutlineLvl(), pPrBase2.getOutlineLvl()) &&
				areEqual(pPrBase1.getCnfStyle(), pPrBase2.getCnfStyle())	
			);
	}

	public static boolean areEqual(RPr rPr1, RPr rPr2) {
		return ((rPr1 == rPr2) ||
				((rPr1 != null) && (rPr2 != null) &&
				 areEqual(rPr1.getRStyle(), rPr2.getRStyle()) &&
				 areEqual(rPr1.getRFonts(), rPr2.getRFonts()) &&
				 areEqual(rPr1.getB(), rPr2.getB()) &&
				 areEqual(rPr1.getBCs(), rPr2.getBCs()) &&
				 areEqual(rPr1.getI(), rPr2.getI()) &&
				 areEqual(rPr1.getICs(), rPr2.getICs()) &&
				 areEqual(rPr1.getCaps(), rPr2.getCaps()) &&
				 areEqual(rPr1.getSmallCaps(), rPr2.getSmallCaps()) &&
				 areEqual(rPr1.getStrike(), rPr2.getStrike()) &&
				 areEqual(rPr1.getDstrike(), rPr2.getDstrike()) &&
				 areEqual(rPr1.getOutline(), rPr2.getOutline()) &&
				 areEqual(rPr1.getShadow(), rPr2.getShadow()) &&
				 areEqual(rPr1.getEmboss(), rPr2.getEmboss()) &&
				 areEqual(rPr1.getImprint(), rPr2.getImprint()) &&
				 areEqual(rPr1.getSnapToGrid(), rPr2.getSnapToGrid()) &&
				 areEqual(rPr1.getVanish(), rPr2.getVanish()) &&
				 areEqual(rPr1.getColor(), rPr2.getColor()) &&
			 	 areEqual(rPr1.getSpacing(), rPr2.getSpacing()) &&
				 areEqual(rPr1.getW(), rPr2.getW()) &&
				 areEqual(rPr1.getKern(), rPr2.getKern()) &&
				 areEqual(rPr1.getPosition(), rPr2.getPosition()) &&
				 areEqual(rPr1.getSz(), rPr2.getSz()) &&
				 areEqual(rPr1.getSzCs(), rPr2.getSzCs()) &&
				 areEqual(rPr1.getHighlight(), rPr2.getHighlight()) &&
				 areEqual(rPr1.getU(), rPr2.getU()) &&
				 areEqual(rPr1.getEffect(), rPr2.getEffect()) &&
				 areEqual(rPr1.getBdr(), rPr2.getBdr()) &&
				 areEqual(rPr1.getShd(), rPr2.getShd()) &&
				 areEqual(rPr1.getVertAlign(), rPr2.getVertAlign()) &&
				 areEqual(rPr1.getRtl(), rPr2.getRtl()) &&
				 areEqual(rPr1.getCs(), rPr2.getCs()) &&
				 areEqual(rPr1.getEm(), rPr2.getEm()) &&
				 areEqual(rPr1.getSpecVanish(), rPr2.getSpecVanish()) &&
				 areEqual(rPr1.getOMath(), rPr2.getOMath())
				 
				 // rPr1.getLang()  ??
				 )
			    );
	}

	public static boolean areEqual(ParaRPr rPr1, ParaRPr rPr2) {
		return ((rPr1 == rPr2) ||
				((rPr1 != null) && (rPr2 != null) &&
				 areEqual(rPr1.getRStyle(), rPr2.getRStyle()) &&
				 areEqual(rPr1.getRFonts(), rPr2.getRFonts()) &&
				 areEqual(rPr1.getB(), rPr2.getB()) &&
				 areEqual(rPr1.getBCs(), rPr2.getBCs()) &&
				 areEqual(rPr1.getI(), rPr2.getI()) &&
				 areEqual(rPr1.getICs(), rPr2.getICs()) &&
				 areEqual(rPr1.getCaps(), rPr2.getCaps()) &&
				 areEqual(rPr1.getSmallCaps(), rPr2.getSmallCaps()) &&
				 areEqual(rPr1.getStrike(), rPr2.getStrike()) &&
				 areEqual(rPr1.getDstrike(), rPr2.getDstrike()) &&
				 areEqual(rPr1.getOutline(), rPr2.getOutline()) &&
				 areEqual(rPr1.getShadow(), rPr2.getShadow()) &&
				 areEqual(rPr1.getEmboss(), rPr2.getEmboss()) &&
				 areEqual(rPr1.getImprint(), rPr2.getImprint()) &&
				 areEqual(rPr1.getSnapToGrid(), rPr2.getSnapToGrid()) &&
				 areEqual(rPr1.getVanish(), rPr2.getVanish()) &&
				 areEqual(rPr1.getColor(), rPr2.getColor()) &&
			 	 areEqual(rPr1.getSpacing(), rPr2.getSpacing()) &&
				 areEqual(rPr1.getW(), rPr2.getW()) &&
				 areEqual(rPr1.getKern(), rPr2.getKern()) &&
				 areEqual(rPr1.getPosition(), rPr2.getPosition()) &&
				 areEqual(rPr1.getSz(), rPr2.getSz()) &&
				 areEqual(rPr1.getSzCs(), rPr2.getSzCs()) &&
				 areEqual(rPr1.getHighlight(), rPr2.getHighlight()) &&
				 areEqual(rPr1.getU(), rPr2.getU()) &&
				 areEqual(rPr1.getEffect(), rPr2.getEffect()) &&
				 areEqual(rPr1.getBdr(), rPr2.getBdr()) &&
				 areEqual(rPr1.getShd(), rPr2.getShd()) &&
				 areEqual(rPr1.getVertAlign(), rPr2.getVertAlign()) &&
				 areEqual(rPr1.getRtl(), rPr2.getRtl()) &&
				 areEqual(rPr1.getCs(), rPr2.getCs()) &&
				 areEqual(rPr1.getEm(), rPr2.getEm()) &&
				 areEqual(rPr1.getSpecVanish(), rPr2.getSpecVanish()) &&
				 areEqual(rPr1.getOMath(), rPr2.getOMath())
				 )
			    );
	}

	public static boolean areEqual(CTTblPrBase tblPr1, CTTblPrBase tblPr2) {
		return ((tblPr1 == tblPr2) ||
				((tblPr1 != null) && (tblPr2 != null) &&
				 areEqual(tblPr1.getTblStyle(), tblPr2.getTblStyle()) &&
				 areEqual(tblPr1.getTblpPr(), tblPr2.getTblpPr()) &&
				 areEqual(tblPr1.getTblOverlap(), tblPr2.getTblOverlap()) &&
				 areEqual(tblPr1.getTblStyleRowBandSize(), tblPr2.getTblStyleRowBandSize()) &&
				 areEqual(tblPr1.getTblStyleColBandSize(), tblPr2.getTblStyleColBandSize()) &&
				 areEqual(tblPr1.getTblW(), tblPr2.getTblW()) &&
				 areEqual(tblPr1.getJc(), tblPr2.getJc()) &&
				 areEqual(tblPr1.getTblCellSpacing(), tblPr2.getTblCellSpacing()) &&
				 areEqual(tblPr1.getTblInd(), tblPr2.getTblInd()) &&
				 areEqual(tblPr1.getTblBorders(), tblPr2.getTblBorders()) &&
				 areEqual(tblPr1.getShd(), tblPr2.getShd()) &&
				 areEqual(tblPr1.getTblLayout(), tblPr2.getTblLayout()) &&
				 areEqual(tblPr1.getTblCellMar(), tblPr2.getTblCellMar()) &&
				 areEqual(tblPr1.getTblLook(), tblPr2.getTblLook())
				)
			   );
	}

	public static boolean areEqual(CTTblLook tblLook1, CTTblLook tblLook2) {
		return ((tblLook1 == tblLook2) || 
				((tblLook1 !=null) && (tblLook2 !=null) &&
						areEqual(tblLook1.getVal(), tblLook2.getVal()) &&
						areEqual(tblLook1.getFirstColumn(), tblLook2.getFirstColumn()) &&
						areEqual(tblLook1.getFirstRow(), tblLook2.getFirstRow()) &&
						areEqual(tblLook1.getLastColumn(), tblLook2.getLastColumn()) &&
						areEqual(tblLook1.getLastRow(), tblLook2.getLastRow()) &&
						areEqual(tblLook1.getNoHBand(), tblLook2.getNoHBand()) &&
						areEqual(tblLook1.getNoVBand(), tblLook2.getNoVBand())
						)
						);
	}
						
	
	private static boolean areEqual(STOnOff oo1, STOnOff oo2) {
		return (oo1 == oo2);
	}

	public static boolean areEqual(TcPr tcPr1, TcPr tcPr2) {
		return ((tcPr1 == tcPr2) ||
				((tcPr1 != null) && (tcPr2 != null) &&
				 areEqual(tcPr1.getCnfStyle(), tcPr2.getCnfStyle()) &&
				 areEqual(tcPr1.getTcW(), tcPr2.getTcW()) &&
				 areEqual(tcPr1.getGridSpan(), tcPr2.getGridSpan()) &&
				 areEqual(tcPr1.getHMerge(), tcPr2.getHMerge()) &&
				 areEqual(tcPr1.getVMerge(), tcPr2.getVMerge()) &&
				 areEqual(tcPr1.getTcBorders(), tcPr2.getTcBorders()) &&
				 areEqual(tcPr1.getShd(), tcPr2.getShd()) &&
				 areEqual(tcPr1.getNoWrap(), tcPr2.getNoWrap()) &&
				 areEqual(tcPr1.getTcMar(), tcPr2.getTcMar()) &&
				 areEqual(tcPr1.getTextDirection(), tcPr2.getTextDirection()) &&
				 areEqual(tcPr1.getTcFitText(), tcPr2.getTcFitText()) &&
				 areEqual(tcPr1.getVAlign(), tcPr2.getVAlign()) &&
				 areEqual(tcPr1.getHideMark(), tcPr2.getHideMark())
				)
			   );
	}

	public static boolean areEqual(List<CTTblStylePr> tblStylePrList1, List<CTTblStylePr> tblStylePrList2) {
		if (tblStylePrList1 == tblStylePrList2)
			return true;
		if (((tblStylePrList1 == null) && (tblStylePrList2 != null)) || ((tblStylePrList1 != null) && (tblStylePrList2 == null)))
			return false;
		if (tblStylePrList1.size() != tblStylePrList2.size())
			return false;
		
		for (int i=0; i<tblStylePrList1.size(); i++)
			if (!areEqual(tblStylePrList1.get(i), tblStylePrList2.get(i)))
				return false;
		return true;
	}

	public static boolean areEqual(CTTblStylePr ctTblStylePr1, CTTblStylePr ctTblStylePr2) {
		return ((ctTblStylePr1 == ctTblStylePr2) ||
				((ctTblStylePr1 != null) && (ctTblStylePr2 != null) &&
				 areEqual(ctTblStylePr1.getPPr(), ctTblStylePr2.getPPr()) &&
				 areEqual(ctTblStylePr1.getRPr(), ctTblStylePr2.getRPr()) &&
				 areEqual(ctTblStylePr1.getTblPr(), ctTblStylePr2.getTblPr()) &&
				 areEqual(ctTblStylePr1.getTrPr(), ctTblStylePr2.getTrPr()) &&
				 areEqual(ctTblStylePr1.getType(), ctTblStylePr2.getType())
			)
		   );
	}

	public static boolean areEqual(OutlineLvl outlineLvl1, OutlineLvl outlineLvl2) {
		return (outlineLvl1 == outlineLvl2) ||
		       ((outlineLvl1 != null) && (outlineLvl2 != null) &&
		        areEqual(outlineLvl1.getVal(), outlineLvl2.getVal()));
	}

	public static boolean areEqual(CTTextboxTightWrap textboxTightWrap1, CTTextboxTightWrap textboxTightWrap2) {
		return (textboxTightWrap1 == textboxTightWrap2) ||
	       ((textboxTightWrap1 != null) && (textboxTightWrap2 != null) &&
	    	((textboxTightWrap1.getVal() == textboxTightWrap2.getVal()) ||
	    	 ((textboxTightWrap1.getVal() != null) &&
	    	   (textboxTightWrap1.getVal().equals(textboxTightWrap2.getVal()))
	    	 ) 		
	    	)
	       );
	}

	public static boolean areEqual(TextAlignment textAlignment1, TextAlignment textAlignment2) {
		return (textAlignment1 == textAlignment2) ||
	       ((textAlignment1 != null) && (textAlignment2 != null) &&
	    	((textAlignment1.getVal() == textAlignment2.getVal()) ||
	    	 ((textAlignment1.getVal() != null) &&
	    	   (textAlignment1.getVal().equals(textAlignment2.getVal()))
	    	 ) 		
	    	)
	       );
	}

	public static boolean areEqual(TextDirection textDirection1, TextDirection textDirection2) {
		return (textDirection1 == textDirection2) ||
	       ((textDirection1 != null) && (textDirection2 != null) &&
	        areEqual(textDirection1.getVal(), textDirection2.getVal()));
	}

	public static boolean areEqual(Jc jc1, Jc jc2) {
		return (jc1 == jc2) ||
	       ((jc1 != null) && (jc2 != null) &&
	    	((jc1.getVal() == jc2.getVal()) ||
	    	 ((jc1.getVal() != null) &&
	    	   (jc1.getVal().equals(jc2.getVal()))
	    	 ) 		
	    	)
	       );
	}

	public static boolean areEqual(Ind ind1, Ind ind2) {
		return ((ind1 == ind2) ||
			((ind1 != null) && (ind2 != null) &&
			 areEqual(ind1.getFirstLine(), ind2.getFirstLine()) &&
			 areEqual(ind1.getFirstLineChars(), ind2.getFirstLineChars()) &&
			 areEqual(ind1.getHanging(), ind2.getHanging()) &&
			 areEqual(ind1.getHangingChars(), ind2.getHangingChars()) &&
			 areEqual(ind1.getLeft(), ind2.getLeft()) &&
			 areEqual(ind1.getLeftChars(), ind2.getLeftChars()) &&
			 areEqual(ind1.getRight(), ind2.getRight()) &&
			 areEqual(ind1.getRightChars(), ind2.getRightChars())
			)
		);
	}

	public static boolean areEqual(Spacing spacing1, Spacing spacing2) {
		return ((spacing1 == spacing2) ||
				((spacing1 != null) && (spacing2 != null) &&
				 areEqual(spacing1.getAfter(), spacing2.getAfter()) &&
				 areEqual(spacing1.getAfterLines(), spacing2.getAfterLines()) &&
				 areEqual(spacing1.getBefore(), spacing2.getBefore()) &&
				 areEqual(spacing1.getBeforeLines(), spacing2.getBeforeLines()) &&
				 areEqual(spacing1.getLine(), spacing2.getLine()) &&
				 ((spacing1.getLineRule() == spacing2.getLineRule()) ||
				  ((spacing1.getLineRule() != null) && 
				   (spacing1.getLineRule().equals(spacing2.getLineRule()))))
				 )
				);
	}

	public static boolean areEqual(Tabs tabs1, Tabs tabs2) {
		if (tabs1 == tabs2)
			return true;
		if (((tabs1 != null) && (tabs2 == null)) || ((tabs1 == null) && (tabs2 != null)))
			return false;
		if (tabs1.getTab() == tabs2.getTab())
			return true;
		if (tabs1.getTab().size() != tabs2.getTab().size())
			return false;
		for (int i=0; i<tabs1.getTab().size(); i++)
			if (!areEqual(tabs1.getTab().get(i), tabs2.getTab().get(i)))
				return false;
		return true;
	}

	public static boolean areEqual(CTTabStop ctTabStop1, CTTabStop ctTabStop2) {
		return ((ctTabStop1 == ctTabStop2) ||
				((ctTabStop1 != null) && (ctTabStop2 != null) &&
				  areEqual(ctTabStop1.getPos(), ctTabStop2.getPos()) &&
						
				 ((ctTabStop1.getVal() == ctTabStop2.getVal()) ||
				  ((ctTabStop1.getVal() != null) && 
				   (ctTabStop1.getVal().equals(ctTabStop2.getVal())))) &&
				   
				 ((ctTabStop1.getLeader() == ctTabStop2.getLeader()) ||
				  ((ctTabStop1.getLeader() != null) && 
				   (ctTabStop1.getLeader().equals(ctTabStop2.getLeader()))))
				 )
			   );
	}

	public static boolean areEqual(CTShd shd1, CTShd shd2) {
		return ((shd1 == shd2) ||
				((shd1 != null) && (shd2 != null) &&
				 areEqual(shd1.getColor(), shd2.getColor()) &&
				 areEqual(shd1.getThemeTint(), shd2.getThemeTint()) &&
				 areEqual(shd1.getThemeShade(), shd2.getThemeShade()) &&
				 areEqual(shd1.getFill(), shd2.getFill()) &&
				 areEqual(shd1.getThemeFillTint(), shd2.getThemeFillTint()) &&
				 areEqual(shd1.getThemeFillShade(), shd2.getThemeFillShade()) &&
				 areEqual(shd1.getThemeColor(), shd2.getThemeColor()) &&
				 areEqual(shd1.getThemeFill(), shd2.getThemeFill()) &&
						
				 ((shd1.getVal() == shd2.getVal()) ||
				  ((shd1.getVal() != null) && 
				   (shd1.getVal().equals(shd2.getVal()))))
				 )
			   );
	}

	public static boolean areEqual(PBdr pBdr1, PBdr pBdr2) {
		return ((pBdr1 == pBdr2) ||
				((pBdr1 != null) && (pBdr2 != null) &&
				 areEqual(pBdr1.getTop(), pBdr2.getTop()) && 
				 areEqual(pBdr1.getLeft(), pBdr2.getLeft()) &&
				 areEqual(pBdr1.getBottom(), pBdr2.getBottom()) &&
				 areEqual(pBdr1.getRight(), pBdr2.getRight()) &&
				 areEqual(pBdr1.getBetween(), pBdr2.getBetween()) &&
				 areEqual(pBdr1.getBar(), pBdr2.getBar())
				)
			   );
	}

	public static boolean areEqual(CTBorder border1, CTBorder border2) {
		return ((border1 == border2) ||
				((border1 != null) && (border2 != null) &&
				 areEqual(border1.getColor(), border2.getColor()) &&
				 areEqual(border1.getSpace(), border2.getSpace()) && 
				 areEqual(border1.getSz(), border2.getSz()) && 
				 areEqual(border1.getThemeColor(), border2.getThemeColor()) && 
				 areEqual(border1.getThemeShade(), border2.getThemeShade()) && 
				 areEqual(border1.getThemeTint(), border2.getThemeTint()) &&
				 
				 ((border1.getVal() == border2.getVal()) ||
						  ((border1.getVal() != null) && 
						   (border1.getVal().equals(border2.getVal()))))
				 
				)
			   );
	}

	public static boolean areEqual(NumPr numPr1, NumPr numPr2) {
		// Comparing the numbering format would require looking into the 
		// numbering definitions part, for this reason only the id is 
		// checked
		return ((numPr1 == numPr2) ||
				((numPr1 != null) && (numPr2 != null) &&
				 ((numPr1.getNumId() == numPr2.getNumId()) ||
				  ((numPr1.getNumId() != null) && (numPr2.getNumId() != null) &&
				  areEqual(numPr1.getNumId().getVal(), numPr2.getNumId().getVal()))
				 )
				)
			   );
	}

	public static boolean areEqual(CTFramePr framePr1, CTFramePr framePr2) {
		return ((framePr1 == framePr2) ||
				((framePr1 != null) && (framePr2 != null) &&
				 areEqual(framePr1.getDropCap(), framePr2.getDropCap()) &&
				 areEqual(framePr1.getLines(), framePr2.getLines()) &&
				 areEqual(framePr1.getW(), framePr2.getW()) &&
				 areEqual(framePr1.getH(), framePr2.getH()) &&
				 areEqual(framePr1.getVSpace(), framePr2.getVSpace()) && 
				 areEqual(framePr1.getHSpace(), framePr2.getHSpace()) &&
				 areEqual(framePr1.getWrap(), framePr2.getWrap()) &&
				 areEqual(framePr1.getHAnchor(), framePr2.getHAnchor()) &&
				 areEqual(framePr1.getVAnchor(), framePr2.getVAnchor()) &&
				 areEqual(framePr1.getX(), framePr2.getX()) &&
				 areEqual(framePr1.getXAlign(), framePr2.getXAlign()) && 
				 areEqual(framePr1.getY(), framePr2.getY()) &&
				 areEqual(framePr1.getYAlign(), framePr2.getYAlign()) &&
				 areEqual(framePr1.getHRule(), framePr2.getHRule()) &&
				 (framePr1.isAnchorLock() == framePr2.isAnchorLock())
				 )
			    );
	}

	public static boolean areEqual(CTCnf cnfStyle1, CTCnf cnfStyle2) {
		return (cnfStyle1 == cnfStyle2) ||
			   ((cnfStyle1 != null) && (cnfStyle2 != null) &&
		       areEqual(cnfStyle1.getVal(), cnfStyle2.getVal())
		       );
	}

	public static boolean areEqual(STHeightRule hRule1, STHeightRule hRule2) {
		return (hRule1 == hRule2) ||
		   ((hRule1 != null) && 
		    (hRule1.equals(hRule2)));
	}

	public static boolean areEqual(STYAlign yAlign1, STYAlign yAlign2) {
		return (yAlign1 == yAlign2) ||
		   ((yAlign1 != null) && 
		    (yAlign1.equals(yAlign2)));
	}

	public static boolean areEqual(STXAlign xAlign1, STXAlign xAlign2) {
		return (xAlign1 == xAlign2) ||
		   ((xAlign1 != null) && 
		    (xAlign1.equals(xAlign2)));
	}

	public static boolean areEqual(STVAnchor vAnchor1, STVAnchor vAnchor2) {
		return (vAnchor1 == vAnchor2) ||
		   ((vAnchor1 != null) && 
		    (vAnchor1.equals(vAnchor2)));
	}

	public static boolean areEqual(STHAnchor hAnchor1, STHAnchor hAnchor2) {
		return (hAnchor1 == hAnchor2) ||
		   ((hAnchor1 != null) && 
		    (hAnchor1.equals(hAnchor2)));
	}

	public static boolean areEqual(STWrap wrap1, STWrap wrap2) {
		return (wrap1 == wrap2) ||
		   ((wrap1 != null) && 
		    (wrap1.equals(wrap2)));
	}

	public static boolean areEqual(STDropCap dropCap1, STDropCap dropCap2) {
		return (dropCap1 == dropCap2) ||
		   ((dropCap1 != null) && 
		    (dropCap1.equals(dropCap2)));
	}

	public static boolean areEqual(PStyle pStyle1, PStyle pStyle2) {
		return (pStyle1 == pStyle2) ||
	       ((pStyle1 != null) && (pStyle2 != null) &&
	        areEqual(pStyle1.getVal(), pStyle2.getVal()));
	}

	public static boolean areEqual(BasedOn basedOn1, BasedOn basedOn2) {
		return (basedOn1 == basedOn2) ||
	       ((basedOn1 != null) && (basedOn2 != null) &&
	        areEqual(basedOn1.getVal(), basedOn2.getVal()));
	}

	public static boolean areEqual(RFonts rFonts1, RFonts rFonts2) {
		//Comparing the ascii version should be enough in most cases 
		return (rFonts1 == rFonts2) ||
		       ((rFonts1 != null) && (rFonts2 != null) &&
			   (areEqual(rFonts1.getAscii(), rFonts2.getAscii()))
			    );
	}

	public static boolean areEqual(RStyle rStyle1, RStyle rStyle2) {
		return (rStyle1 == rStyle2) ||
	       ((rStyle1 != null) && (rStyle2 != null) &&
		   (areEqual(rStyle1.getVal(), rStyle2.getVal()))
		    );
	}

	public static boolean areEqual(CTEm em1, CTEm em2) {
		return (em1 == em2) ||
	       ((em1 != null) && (em2 != null) &&
		   (areEqual(em1.getVal(), em2.getVal()))
		    );
	}

	public static boolean areEqual(CTVerticalAlignRun vertAlign1, CTVerticalAlignRun vertAlign2) {
		return (vertAlign1 == vertAlign2) ||
	       ((vertAlign1 != null) && (vertAlign2 != null) &&
		   (areEqual(vertAlign1.getVal(), vertAlign2.getVal()))
		    );
	}

	public static boolean areEqual(CTTextEffect effect1, CTTextEffect effect2) {
		return (effect1 == effect2) ||
	       ((effect1 != null) && (effect2 != null) &&
		   (areEqual(effect1.getVal(), effect2.getVal()))
		    );
	}

	public static boolean areEqual(U u1, U u2) {
		return (u1 == u2) ||
	       ((u1 != null) && (u2 != null) &&
		    areEqual(u1.getVal(), u2.getVal()) &&
		    areEqual(u1.getColor(), u2.getColor())
		    );
	}

	public static boolean areEqual(Highlight highlight1, Highlight highlight2) {
		return (highlight1 == highlight2) ||
	       ((highlight1 != null) && (highlight2 != null) &&
		   (areEqual(highlight1.getVal(), highlight2.getVal()))
		    );
	}

	public static boolean areEqual(CTSignedHpsMeasure measure1, CTSignedHpsMeasure measure2) {
		return (measure1 == measure2) ||
	       ((measure1 != null) && (measure2 != null) &&
		   (areEqual(measure1.getVal(), measure2.getVal()))
		    );
	}

	public static boolean areEqual(HpsMeasure measure1, HpsMeasure measure2) {
		return (measure1 == measure2) ||
	       ((measure1 != null) && (measure2 != null) &&
		   (areEqual(measure1.getVal(), measure2.getVal()))
		    );
	}

	public static boolean areEqual(CTTextScale scale1, CTTextScale scale2) {
		return (scale1 == scale2) ||
	       ((scale1 != null) && (scale2 != null) &&
		   (areEqual(scale1.getVal(), scale2.getVal()))
		    );
	}

	public static boolean areEqual(CTSignedTwipsMeasure measure1, CTSignedTwipsMeasure measure2) {
		return (measure1 == measure2) ||
	       ((measure1 != null) && (measure2 != null) &&
		   (areEqual(measure1.getVal(), measure2.getVal()))
		    );
	}

	public static boolean areEqual(Color color1, Color color2) {
		return (color1 == color2) ||
	       ((color1 != null) && (color2 != null) &&
		   (areEqual(color1.getVal(), color2.getVal()))
		    );
	}

	public static boolean areEqual(CTTblLayoutType tblLayout1, CTTblLayoutType tblLayout2) {
		return (tblLayout1 == tblLayout2) ||
	       ((tblLayout1 != null) && (tblLayout2 != null) &&
		   (areEqual(tblLayout1.getType(), tblLayout2.getType()))
		    );
	}

	public static boolean areEqual(CTTblCellMar margin1, CTTblCellMar margin2) {
		return (margin1 == margin2) ||
	       ((margin1 != null) && (margin2 != null) &&
		    areEqual(margin1.getBottom(), margin2.getBottom()) &&
		    areEqual(margin1.getLeft(), margin2.getLeft()) &&
		    areEqual(margin1.getRight(), margin2.getRight()) &&
		    areEqual(margin1.getTop(), margin2.getTop())
		    );
	}

	public static boolean areEqual(TblBorders borders1, TblBorders borders2) {
		return (borders1 == borders2) ||
	       ((borders1 != null) && (borders2 != null) &&
		    areEqual(borders1.getBottom(), borders2.getBottom()) &&
		    areEqual(borders1.getLeft(), borders2.getLeft()) &&
		    areEqual(borders1.getRight(), borders2.getRight()) &&
		    areEqual(borders1.getTop(), borders2.getTop()) &&
		    areEqual(borders1.getInsideH(), borders2.getInsideH()) &&
		    areEqual(borders1.getInsideV(), borders2.getInsideV())
		    );
	}

	public static boolean areEqual(TblStyleColBandSize bandSize1, TblStyleColBandSize bandSize2) {
		return (bandSize1 == bandSize2) ||
	       ((bandSize1 != null) && (bandSize2 != null) &&
		    areEqual(bandSize1.getVal(), bandSize2.getVal())
		    );
	}

	public static boolean areEqual(TblStyleRowBandSize bandSize1, TblStyleRowBandSize bandSize2) {
		return (bandSize1 == bandSize2) ||
	       ((bandSize1 != null) && (bandSize2 != null) &&
		    areEqual(bandSize1.getVal(), bandSize2.getVal())
		    );
	}

	public static boolean areEqual(CTTblOverlap overlap1, CTTblOverlap overlap2) {
		return (overlap1 == overlap2) ||
	       ((overlap1 != null) && (overlap2 != null) &&
		    areEqual(overlap1.getVal(), overlap2.getVal())
		    );
	}

	public static boolean areEqual(CTTblPPr tblpPr1, CTTblPPr tblpPr2) {
		return (tblpPr1 == tblpPr2) ||
	       ((tblpPr1 != null) && (tblpPr2 != null) &&
			areEqual(tblpPr1.getLeftFromText(), tblpPr2.getLeftFromText()) &&
			areEqual(tblpPr1.getRightFromText(), tblpPr2.getRightFromText()) &&
			areEqual(tblpPr1.getTopFromText(), tblpPr2.getTopFromText()) &&
			areEqual(tblpPr1.getBottomFromText(), tblpPr2.getBottomFromText()) &&
			areEqual(tblpPr1.getVertAnchor(), tblpPr2.getVertAnchor()) &&
			areEqual(tblpPr1.getHorzAnchor(), tblpPr2.getHorzAnchor()) &&
			areEqual(tblpPr1.getTblpXSpec(), tblpPr2.getTblpXSpec()) &&
			areEqual(tblpPr1.getTblpX(), tblpPr2.getTblpX()) &&
			areEqual(tblpPr1.getTblpYSpec(), tblpPr2.getTblpYSpec()) &&
			areEqual(tblpPr1.getTblpY(), tblpPr2.getTblpY())		
		    );
	}

	public static boolean areEqual(TblStyle style1, TblStyle style2) {
		return (style1 == style2) ||
	       ((style1 != null) && (style2 != null) &&
		    areEqual(style1.getVal(), style2.getVal())
		    );
	}

	public static boolean areEqual(CTVerticalJc vAlign1, CTVerticalJc vAlign2) {
		return (vAlign1 == vAlign2) ||
	       ((vAlign1 != null) && (vAlign2 != null) &&
		    areEqual(vAlign1.getVal(), vAlign2.getVal())
		    );
	}

	public static boolean areEqual(TcMar margin1, TcMar margin2) {
		return (margin1 == margin2) ||
	       ((margin1 != null) && (margin2 != null) &&
		    areEqual(margin1.getBottom(), margin2.getBottom()) &&
		    areEqual(margin1.getLeft(), margin2.getLeft()) &&
		    areEqual(margin1.getRight(), margin2.getRight()) &&
		    areEqual(margin1.getTop(), margin2.getTop())
		    );
	}

	public static boolean areEqual(TcBorders borders1, TcBorders borders2) {
		return (borders1 == borders2) ||
	       ((borders1 != null) && (borders2 != null) &&
		    areEqual(borders1.getBottom(), borders2.getBottom()) &&
		    areEqual(borders1.getLeft(), borders2.getLeft()) &&
		    areEqual(borders1.getRight(), borders2.getRight()) &&
		    areEqual(borders1.getTop(), borders2.getTop()) &&
		    areEqual(borders1.getInsideH(), borders2.getInsideH()) &&
		    areEqual(borders1.getInsideV(), borders2.getInsideV()) &&
		    areEqual(borders1.getTl2Br(), borders2.getTl2Br()) &&
		    areEqual(borders1.getTr2Bl(), borders2.getTr2Bl())
		    );
	}

	public static boolean areEqual(VMerge merge1, VMerge merge2) {
		return (merge1 == merge2) ||
	       ((merge1 != null) && (merge2 != null) &&
		    areEqual(merge1.getVal(), merge2.getVal())
		    );
	}

	public static boolean areEqual(HMerge merge1, HMerge merge2) {
		return (merge1 == merge2) ||
	       ((merge1 != null) && (merge2 != null) &&
		    areEqual(merge1.getVal(), merge2.getVal())
		    );
	}

	public static boolean areEqual(GridSpan gridSpan1, GridSpan gridSpan2) {
		return (gridSpan1 == gridSpan2) ||
	       ((gridSpan1 != null) && (gridSpan2 != null) &&
		    areEqual(gridSpan1.getVal(), gridSpan2.getVal())
		    );
	}

	public static boolean areEqual(TrPr trPr1, TrPr trPr2) {
	List<JAXBElement<?>> defs1 = null;
	List<JAXBElement<?>> defs2 = null;
	JAXBElement<?> defs1element = null;
	Object defs2value = null;
	QName qName = null;
	String qNameLocal = null;
		if (trPr1 == trPr2)
			return true;
		if (((trPr1 != null) && (trPr2 == null)) || ((trPr1 == null) && (trPr2 != null)))
			return false;
		defs1 = trPr1.getCnfStyleOrDivIdOrGridBefore();
		defs2 = trPr2.getCnfStyleOrDivIdOrGridBefore();
		if (defs1 == defs2) 
			return true;
		//getCnfStyleOrDivIdOrGridBefore() is allways != null
		if (defs1.size() != defs2.size())
			return false;
		if (defs1.isEmpty())
			return true;

		for (int i=0; i<defs1.size(); i++) {
			defs1element = defs1.get(i);
			qName = defs1element.getName();
			defs2value = findTrValue(defs2, qName);
			if (defs2value == null)
				return false;
			qNameLocal = qName.getLocalPart();
			if ("gridAfter".equals(qNameLocal)) {
				if (!areEqual((GridAfter)defs1element.getValue(), (GridAfter)defs2value))
					return false;
			}
			else if ("cantSplit".equals(qNameLocal)) {
				if (!areEqual((BooleanDefaultTrue)defs1element.getValue(), (BooleanDefaultTrue)defs2value))
					return false;
			}
			else if ("wBefore".equals(qNameLocal)) {
				if (!areEqual((TblWidth)defs1element.getValue(), (TblWidth)defs2value))
					return false;
			}
			else if ("jc".equals(qNameLocal)) {
				if (!areEqual((Jc)defs1element.getValue(), (Jc)defs2value))
					return false;
			}
			else if ("cnfStyle".equals(qNameLocal)) {
				if (!areEqual((CTCnf)defs1element.getValue(), (CTCnf)defs2value))
					return false;
			}
			else if ("gridBefore".equals(qNameLocal)) {
				if (!areEqual((GridBefore)defs1element.getValue(), (GridBefore)defs2value))
					return false;
			}
			else if ("hidden".equals(qNameLocal)) {
				if (!areEqual((BooleanDefaultTrue)defs1element.getValue(), (BooleanDefaultTrue)defs2value))
					return false;
			}
			else if ("trHeight".equals(qNameLocal)) {
				if (!areEqual((CTHeight)defs1element.getValue(), (CTHeight)defs2value))
					return false;
			}
			else if ("wAfter".equals(qNameLocal)) {
				if (!areEqual((TblWidth)defs1element.getValue(), (TblWidth)defs2value))
					return false;
			}
			else if ("tblHeader".equals(qNameLocal)) {
				if (!areEqual((BooleanDefaultTrue)defs1element.getValue(), (BooleanDefaultTrue)defs2value))
					return false;
			}
			else if ("tblCellSpacing".equals(qNameLocal)) {
				if (!areEqual((TblWidth)defs1element.getValue(), (TblWidth)defs2value))
					return false;
			}
		}
		return true;
	}

	private static Object findTrValue(List<JAXBElement<?>> defs, QName qName) {
		for (int i=0; i<defs.size(); i++)
			if (qName.equals(defs.get(i).getName())) 
				return defs.get(i).getValue();
		return null;
	}

	public static boolean areEqual(GridBefore value1, GridBefore value2) {
		return (value1 == value2) ||
	       ((value1 != null) && (value2 != null) &&
		   (areEqual(value1.getVal(), value2.getVal()))
		    );
	}

	public static boolean areEqual(GridAfter value1, GridAfter value2) {
		return (value1 == value2) ||
	       ((value1 != null) && (value2 != null) &&
		   (areEqual(value1.getVal(), value2.getVal()))
		    );
	}

	public static boolean areEqual(CTHeight height1, CTHeight height2) {
		return (height1 == height2) ||
	       ((height1 != null) && (height2 != null) &&
		    areEqual(height1.getVal(), height2.getVal()) &&
		    areEqual(height1.getHRule(), height2.getHRule())
		    );
	}

	public static boolean areEqual(TblWidth width1, TblWidth width2) {
		return (width1 == width2) ||
	       ((width1 != null) && (width2 != null) &&
		    areEqual(width1.getW(), width2.getW()) &&
		    areEqual(width1.getType(), width2.getType())
		    );
	}

	public static boolean areEqual(CTShortHexNumber number1, CTShortHexNumber number2) {
		return (number1 == number2) ||
	       ((number1 != null) && (number2 != null) &&
		   (areEqual(number1.getVal(), number2.getVal()))
		    );
	}

	public static boolean areEqual(STTblOverlap val1, STTblOverlap val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(STTblStyleOverrideType val1, STTblStyleOverrideType val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(STTblLayoutType val1, STTblLayoutType val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(UnderlineEnumeration val1, UnderlineEnumeration val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(STTextEffect val1, STTextEffect val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(STEm val1, STEm val2) {
		return (val1 == val2) ||
		       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(STVerticalAlignRun val1, STVerticalAlignRun val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(STVerticalJc val1, STVerticalJc val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	public static boolean areEqual(STThemeColor themeColor1, STThemeColor themeColor2) {
		return (themeColor1 == themeColor2) ||
			   ((themeColor1 != null) && 
			    (themeColor1.equals(themeColor2)));
	}

	public static boolean areEqual(BooleanDefaultTrue booleanDefaultTrue1, BooleanDefaultTrue booleanDefaultTrue2) {
		return (booleanDefaultTrue1 != null ? booleanDefaultTrue1.isVal() : false) ==
			   (booleanDefaultTrue2 != null ? booleanDefaultTrue2.isVal() : false);
	}

	public static boolean areEqual(Boolean bool1, Boolean bool2) {
		return (bool1 != null ? bool1.booleanValue() : false) ==
			   (bool2 != null ? bool2.booleanValue() : false);
	}

	public static boolean areEqual(BigInteger val1, BigInteger val2) {
		return (val1 == val2) ||
		       ((val1 != null) && (val1.equals(val2)));
	}

	protected static boolean areEqual(Integer val1, Integer val2) {
		return (val1 == val2) ||
	       ((val1 != null) && (val1.equals(val2)));
	}

	protected static boolean areEqual(String val1, String val2) {
		return (val1 == val2) ||
		       ((val1 != null) && (val1.equals(val2)));
	}
	


/////////////////////////////////////////////
//isEmpty-Methods
/////////////////////////////////////////////
	
	
	public static boolean isEmpty(Style style) {
		if (style == null) 
			return true;

		if (isEmpty(style.getStyleId())) {
			log.warn("style currently has no id");
		}
		
		if (CHARACTER_STYLE.equals(style.getType())) {
			return isEmpty(style.getRPr());
		}
		else if (PARAGRAPH_STYLE.equals(style.getType()) || 
				 NUMBERING_STYLE.equals(style.getType())) {
			return isEmpty(style.getPPr()) &&
				   isEmpty(style.getRPr());
		}
		else if (TABLE_STYLE.equals(style.getType())) {
			return isEmpty(style.getPPr()) &&
				   isEmpty(style.getRPr()) &&
				   isEmpty(style.getTblPr()) &&
				   isEmpty(style.getTcPr()) &&
				   isEmpty(style.getTblStylePr());
			
		}
		//throw new IllegalArgumentException("Invalid style type: " + style.getType());
		else {

			log.warn("style type is currently unknown or null");
            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(style));
            }
			
			return isEmpty(style.getPPr()) &&
					   isEmpty(style.getRPr()) &&
					   isEmpty(style.getTblPr()) &&
					   isEmpty(style.getTcPr()) &&
					   isEmpty(style.getTblStylePr());
		}
	}

	public static boolean isEmpty(PPr pPr) {
		return (pPr == null) ||
			(isEmpty((PPrBase)pPr) &&
			 isEmpty(pPr.getRPr()) &&
			 isEmpty(pPr.getSectPr() )
			);
	}

	public static boolean isEmpty(PPrBase pPrBase) {
		return (pPrBase == null) ||
			(   isEmpty(pPrBase.getPStyle()) &&	
				isEmpty(pPrBase.getKeepNext()) &&	
				isEmpty(pPrBase.getKeepLines()) &&	
				isEmpty(pPrBase.getPageBreakBefore()) &&	
				isEmpty(pPrBase.getFramePr()) &&	
				isEmpty(pPrBase.getWidowControl()) &&	
				isEmpty(pPrBase.getNumPr()) &&	
				isEmpty(pPrBase.getSuppressLineNumbers()) &&	
				isEmpty(pPrBase.getPBdr()) &&	
				isEmpty(pPrBase.getShd()) &&	
				isEmpty(pPrBase.getTabs()) &&	
				isEmpty(pPrBase.getSuppressAutoHyphens()) &&	
				isEmpty(pPrBase.getKinsoku()) &&	
				isEmpty(pPrBase.getWordWrap()) &&	
				isEmpty(pPrBase.getOverflowPunct()) &&	
				isEmpty(pPrBase.getTopLinePunct()) &&	
				isEmpty(pPrBase.getAutoSpaceDE()) &&	
				isEmpty(pPrBase.getAutoSpaceDN()) &&	
				isEmpty(pPrBase.getBidi()) &&	
				isEmpty(pPrBase.getAdjustRightInd()) &&	
				isEmpty(pPrBase.getSnapToGrid()) &&	
				isEmpty(pPrBase.getSpacing()) &&	
				isEmpty(pPrBase.getInd()) &&	
				isEmpty(pPrBase.getContextualSpacing()) &&	
				isEmpty(pPrBase.getMirrorIndents()) &&	
				isEmpty(pPrBase.getSuppressOverlap()) &&	
				isEmpty(pPrBase.getJc()) &&	
				isEmpty(pPrBase.getTextDirection()) &&	
				isEmpty(pPrBase.getTextAlignment()) &&	
				isEmpty(pPrBase.getTextboxTightWrap()) &&	
				isEmpty(pPrBase.getOutlineLvl()) &&
				isEmpty(pPrBase.getCnfStyle())	
			);
	}

	/**
	 * isEmpty returns true if rPr is null, or each of its
	 * properties is in turn, empty 
	 * 
	 * @param rPr
	 * @return
	 */
	public static boolean isEmpty(RPr rPr) {
		return ((rPr == null) ||
				(isEmpty(rPr.getRStyle()) &&
				 isEmpty(rPr.getRFonts()) &&
				 isEmpty(rPr.getB()) &&
				 isEmpty(rPr.getBCs()) &&
				 isEmpty(rPr.getI()) &&
				 isEmpty(rPr.getICs()) &&
				 isEmpty(rPr.getCaps()) &&
				 isEmpty(rPr.getSmallCaps()) &&
				 isEmpty(rPr.getStrike()) &&
				 isEmpty(rPr.getDstrike()) &&
				 isEmpty(rPr.getOutline()) &&
				 isEmpty(rPr.getShadow()) &&
				 isEmpty(rPr.getEmboss()) &&
				 isEmpty(rPr.getImprint()) &&
				 isEmpty(rPr.getSnapToGrid()) &&
				 isEmpty(rPr.getVanish()) &&
				 isEmpty(rPr.getColor()) &&
			 	 isEmpty(rPr.getSpacing()) &&
				 isEmpty(rPr.getW()) &&
				 isEmpty(rPr.getKern()) &&
				 isEmpty(rPr.getPosition()) &&
				 isEmpty(rPr.getSz()) &&
				 isEmpty(rPr.getSzCs()) &&
				 isEmpty(rPr.getHighlight()) &&
				 isEmpty(rPr.getU()) &&
				 isEmpty(rPr.getEffect()) &&
				 isEmpty(rPr.getBdr()) &&
				 isEmpty(rPr.getShd()) &&
				 isEmpty(rPr.getVertAlign()) &&
				 isEmpty(rPr.getRtl()) &&
				 isEmpty(rPr.getCs()) &&
				 isEmpty(rPr.getEm()) &&
				 isEmpty(rPr.getSpecVanish()) &&
				 isEmpty(rPr.getOMath())
				 )
			    );
	}

	public static boolean isEmpty(ParaRPr rPr) {
		return ((rPr == null) ||
				(isEmpty(rPr.getRStyle()) &&
				 isEmpty(rPr.getRFonts()) &&
				 isEmpty(rPr.getB()) &&
				 isEmpty(rPr.getBCs()) &&
				 isEmpty(rPr.getI()) &&
				 isEmpty(rPr.getICs()) &&
				 isEmpty(rPr.getCaps()) &&
				 isEmpty(rPr.getSmallCaps()) &&
				 isEmpty(rPr.getStrike()) &&
				 isEmpty(rPr.getDstrike()) &&
				 isEmpty(rPr.getOutline()) &&
				 isEmpty(rPr.getShadow()) &&
				 isEmpty(rPr.getEmboss()) &&
				 isEmpty(rPr.getImprint()) &&
				 isEmpty(rPr.getSnapToGrid()) &&
				 isEmpty(rPr.getVanish()) &&
				 isEmpty(rPr.getColor()) &&
			 	 isEmpty(rPr.getSpacing()) &&
				 isEmpty(rPr.getW()) &&
				 isEmpty(rPr.getKern()) &&
				 isEmpty(rPr.getPosition()) &&
				 isEmpty(rPr.getSz()) &&
				 isEmpty(rPr.getSzCs()) &&
				 isEmpty(rPr.getHighlight()) &&
				 isEmpty(rPr.getU()) &&
				 isEmpty(rPr.getEffect()) &&
				 isEmpty(rPr.getBdr()) &&
				 isEmpty(rPr.getShd()) &&
				 isEmpty(rPr.getVertAlign()) &&
				 isEmpty(rPr.getRtl()) &&
				 isEmpty(rPr.getCs()) &&
				 isEmpty(rPr.getEm()) &&
				 isEmpty(rPr.getSpecVanish()) &&
				 isEmpty(rPr.getOMath())
				 )
			    );
	}

	public static boolean isEmpty(CTTblPrBase tblPr) {
		return ((tblPr == null) ||
				(isEmpty(tblPr.getTblStyle()) &&
				 isEmpty(tblPr.getTblpPr()) &&
				 isEmpty(tblPr.getTblOverlap()) &&
				 isEmpty(tblPr.getTblStyleRowBandSize()) &&
				 isEmpty(tblPr.getTblStyleColBandSize()) &&
				 isEmpty(tblPr.getTblW()) &&
				 isEmpty(tblPr.getJc()) &&
				 isEmpty(tblPr.getTblCellSpacing()) &&
				 isEmpty(tblPr.getTblInd()) &&
				 isEmpty(tblPr.getTblBorders()) &&
				 isEmpty(tblPr.getShd()) &&
				 isEmpty(tblPr.getTblLayout()) &&
				 isEmpty(tblPr.getTblCellMar()) &&
				 isEmpty(tblPr.getTblLook())
				)
			   );
	}

	public static boolean isEmpty(TcPr tcPr) {
		return ((tcPr == null) ||
				(isEmpty(tcPr.getCnfStyle()) &&
				 isEmpty(tcPr.getTcW()) &&
				 isEmpty(tcPr.getGridSpan()) &&
				 isEmpty(tcPr.getHMerge()) &&
				 isEmpty(tcPr.getVMerge()) &&
				 isEmpty(tcPr.getTcBorders()) &&
				 isEmpty(tcPr.getShd()) &&
				 isEmpty(tcPr.getNoWrap()) &&
				 isEmpty(tcPr.getTcMar()) &&
				 isEmpty(tcPr.getTextDirection()) &&
				 isEmpty(tcPr.getTcFitText()) &&
				 isEmpty(tcPr.getVAlign()) &&
				 isEmpty(tcPr.getHideMark())
				)
			   );
	}

	public static boolean isEmpty(List<CTTblStylePr> tblStylePrList) {
		if ((tblStylePrList == null) || (tblStylePrList.isEmpty()))
			return true;
		
		for (int i=0; i<tblStylePrList.size(); i++)
			if (!isEmpty(tblStylePrList.get(i)))
				return false;
		return true;
	}

	public static boolean isEmpty(CTTblStylePr ctTblStylePr) {
		return ((ctTblStylePr == null) ||
				(isEmpty(ctTblStylePr.getPPr()) &&
				 isEmpty(ctTblStylePr.getRPr()) &&
				 isEmpty(ctTblStylePr.getTblPr()) &&
				 isEmpty(ctTblStylePr.getTrPr()) &&
				 (ctTblStylePr.getType() == null)
			)
		   );
	}

	public static boolean isEmpty(OutlineLvl outlineLvl) {
		return (outlineLvl == null) || (outlineLvl.getVal() == null);
	}

	public static boolean isEmpty(CTTextboxTightWrap textboxTightWrap) {
		return (textboxTightWrap == null) || (textboxTightWrap.getVal() == null);
	}

	public static boolean isEmpty(TextAlignment textAlignment) {
		return (textAlignment == null) || (textAlignment.getVal() == null);
	}

	public static boolean isEmpty(TextDirection textDirection) {
		return (textDirection == null) || (textDirection.getVal() == null);
	}

	public static boolean isEmpty(Jc jc) {
		return (jc == null) || (jc.getVal() == null);
	}

	public static boolean isEmpty(Ind ind) {
		return ((ind == null) ||
			(isEmpty(ind.getFirstLine()) &&
			 isEmpty(ind.getFirstLineChars()) &&
			 isEmpty(ind.getHanging()) &&
			 isEmpty(ind.getHangingChars()) &&
			 isEmpty(ind.getLeft()) &&
			 isEmpty(ind.getLeftChars()) &&
			 isEmpty(ind.getRight()) &&
			 isEmpty(ind.getRightChars())
			)
		);
	}

	public static boolean isEmpty(Spacing spacing) {
		return ((spacing == null) ||
				(isEmpty(spacing.getAfter()) &&
				 isEmpty(spacing.getAfterLines()) &&
				 isEmpty(spacing.getBefore()) &&
				 isEmpty(spacing.getBeforeLines()) &&
				 isEmpty(spacing.getLine()) &&
				 (spacing.getLineRule() == null)
				 )
				);
	}

	public static boolean isEmpty(Tabs tabs) {
		if ((tabs == null) || (tabs.getTab() == null) || (tabs.getTab().isEmpty()))
			return true;
		
		for (int i=0; i<tabs.getTab().size(); i++)
			if (!isEmpty(tabs.getTab().get(i)))
				return false;
		return true;
	}

	public static boolean isEmpty(CTTabStop ctTabStop) {
		return ((ctTabStop == null) ||
				(isEmpty(ctTabStop.getPos()) &&
				 (ctTabStop.getVal() == null) &&
				 (ctTabStop.getLeader() == null)
				 )
			   );
	}

	public static boolean isEmpty(CTShd shd) {
		return ((shd == null) ||
				(isEmpty(shd.getColor()) &&
				 isEmpty(shd.getThemeTint()) &&
				 isEmpty(shd.getThemeShade()) &&
				 isEmpty(shd.getFill()) &&
				 isEmpty(shd.getThemeFillTint()) &&
				 isEmpty(shd.getThemeFillShade()) &&
				 (shd.getThemeColor() == null) &&
				 (shd.getThemeFill() == null) &&
				 (shd.getVal() == null)
				 )
			   );
	}

	public static boolean isEmpty(PBdr pBdr) {
		return ((pBdr == null) ||
				(isEmpty(pBdr.getTop()) && 
				 isEmpty(pBdr.getLeft()) &&
				 isEmpty(pBdr.getBottom()) &&
				 isEmpty(pBdr.getRight()) &&
				 isEmpty(pBdr.getBetween()) &&
				 isEmpty(pBdr.getBar())
				)
			   );
	}

	public static boolean isEmpty(CTBorder border) {
		return ((border == null) ||
				(isEmpty(border.getColor()) &&
				 isEmpty(border.getSpace()) && 
				 isEmpty(border.getSz()) && 
				 (border.getThemeColor() == null) && 
				 isEmpty(border.getThemeShade()) && 
				 isEmpty(border.getThemeTint()) &&
				 (border.getVal() == null)				 
				)
			   );
	}

	public static boolean isEmpty(NumPr numPr) {
		// Comparing the numbering format would require looking into the 
		// numbering definitions part, for this reason only the id is 
		// checked
		return (numPr == null) || 
			   (numPr.getNumId() == null) || 
			   (isEmpty(numPr.getNumId().getVal()));
	}

	public static boolean isEmpty(CTFramePr framePr) {
		return ((framePr == null) ||
				((framePr.getDropCap() == null) &&
				 isEmpty(framePr.getLines()) &&
				 isEmpty(framePr.getW()) &&
				 isEmpty(framePr.getH()) &&
				 isEmpty(framePr.getVSpace()) && 
				 isEmpty(framePr.getHSpace()) &&
				 (framePr.getWrap() == null) &&
				 (framePr.getHAnchor() == null) &&
				 (framePr.getVAnchor() == null) &&
				 isEmpty(framePr.getX()) &&
				 (framePr.getXAlign() == null) && 
				 isEmpty(framePr.getY()) &&
				 (framePr.getYAlign() == null) &&
				 (framePr.getHRule() == null) &&
				 (framePr.isAnchorLock())
				 )
			    );
	}

	public static boolean isEmpty(CTCnf cnfStyle) {
		return (cnfStyle == null) ||
		       isEmpty(cnfStyle.getVal()
		       );
	}

	public static boolean isEmpty(PStyle pStyle) {
		return (pStyle == null) ||
	       isEmpty(pStyle.getVal());
	}

	/**
	 * isEmpty (non sensitive to presence of possible hint ie
	 * would still return true)
	 * 
	 * @param rFonts
	 * @return
	 */
	public static boolean isEmpty(RFonts rFonts) {
				
		return (rFonts == null) ||
				(isEmpty(rFonts.getAscii())
						&& isEmpty(rFonts.getAsciiTheme())
						&& isEmpty(rFonts.getCs())
						&& isEmpty(rFonts.getCstheme())
						&& isEmpty(rFonts.getEastAsia())
						&& isEmpty(rFonts.getEastAsiaTheme())
						&& isEmpty(rFonts.getHAnsi())
						&& isEmpty(rFonts.getHAnsiTheme())
						);
	}

	public static boolean isEmpty(STHint stHint) {
		return (stHint == null) ;
	}

	public static boolean isEmpty(STTheme stTheme) {
		return (stTheme == null) ;
	}
	
	public static boolean isEmpty(RStyle rStyle) {
		return (rStyle == null) ||
	       isEmpty(rStyle.getVal());
	}

	public static boolean isEmpty(CTEm em) {
		return (em == null) || (em.getVal() == null);
	}

	public static boolean isEmpty(CTVerticalAlignRun vertAlign) {
		return (vertAlign == null) || (vertAlign.getVal() == null);
	}

	public static boolean isEmpty(CTTextEffect effect) {
		return (effect == null) || (effect.getVal() == null);
	}

	public static boolean isEmpty(U u) {
		return (u == null) ||
	       ((u.getVal() == null) &&
		    isEmpty(u.getColor())
		    );
	}

	public static boolean isEmpty(Highlight highlight) {
		return (highlight == null) || isEmpty(highlight.getVal());
	}

	public static boolean isEmpty(CTSignedHpsMeasure measure) {
		return (measure == null) || isEmpty(measure.getVal());
	}

	public static boolean isEmpty(HpsMeasure measure) {
		return (measure == null) || isEmpty(measure.getVal());
	}

	public static boolean isEmpty(CTTextScale scale) {
		return (scale == null) || isEmpty(scale.getVal());
	}

	public static boolean isEmpty(CTSignedTwipsMeasure measure) {
		return (measure == null) || isEmpty(measure.getVal());
	}

	public static boolean isEmpty(Color color) {
		return (color == null) || isEmpty(color.getVal());
	}

	public static boolean isEmpty(CTTblLook tblLook) {
		return (tblLook == null) || 
				(tblLook.getFirstColumn()==null &&
				tblLook.getFirstRow()==null && 
				tblLook.getLastColumn()==null && 
				tblLook.getLastRow()==null && 
				tblLook.getNoHBand()==null && 
				tblLook.getNoVBand()==null &&
				tblLook.getVal()==null
				);
	}
	
	public static boolean isEmpty(CTTblLayoutType tblLayout) {
		return (tblLayout == null) || (tblLayout.getType() == null);
	}

	public static boolean isEmpty(CTTblCellMar margin) {
		return (margin == null) ||
	       (isEmpty(margin.getBottom()) &&
		    isEmpty(margin.getLeft()) &&
		    isEmpty(margin.getRight()) &&
		    isEmpty(margin.getTop())
		    );
	}

	public static boolean isEmpty(TblBorders borders) {
		return (borders == null) ||
	       (isEmpty(borders.getBottom()) &&
		    isEmpty(borders.getLeft()) &&
		    isEmpty(borders.getRight()) &&
		    isEmpty(borders.getTop()) &&
		    isEmpty(borders.getInsideH()) &&
		    isEmpty(borders.getInsideV())
		    );
	}

	public static boolean isEmpty(TblStyleColBandSize bandSize) {
		return (bandSize == null) || isEmpty(bandSize.getVal());
	}

	public static boolean isEmpty(TblStyleRowBandSize bandSize) {
		return (bandSize == null) || isEmpty(bandSize.getVal());
	}

	public static boolean isEmpty(CTTblOverlap overlap) {
		return (overlap == null) || (overlap.getVal() == null);
	}

	public static boolean isEmpty(CTTblPPr tblpPr) {
		return (tblpPr == null) ||
	       (isEmpty(tblpPr.getLeftFromText()) &&
			isEmpty(tblpPr.getRightFromText()) &&
			isEmpty(tblpPr.getTopFromText()) &&
			isEmpty(tblpPr.getBottomFromText()) &&
			isEmpty(tblpPr.getVertAnchor() == null) &&
			isEmpty(tblpPr.getHorzAnchor() == null) &&
			isEmpty(tblpPr.getTblpXSpec() == null) &&
			isEmpty(tblpPr.getTblpX()) &&
			isEmpty(tblpPr.getTblpYSpec() == null) &&
			isEmpty(tblpPr.getTblpY())		
		    );
	}

	public static boolean isEmpty(TblStyle style) {
		return (style == null) || isEmpty(style.getVal());
	}

	public static boolean isEmpty(CTVerticalJc vAlign) {
		return (vAlign == null) || (vAlign.getVal() == null);
	}

	public static boolean isEmpty(TcMar margin) {
		return (margin == null) ||
	       (isEmpty(margin.getBottom()) &&
		    isEmpty(margin.getLeft()) &&
		    isEmpty(margin.getRight()) &&
		    isEmpty(margin.getTop())
		    );
	}

	public static boolean isEmpty(TcBorders borders) {
		return (borders == null) ||
	       (isEmpty(borders.getBottom()) &&
		    isEmpty(borders.getLeft()) &&
		    isEmpty(borders.getRight()) &&
		    isEmpty(borders.getTop()) &&
		    isEmpty(borders.getInsideH()) &&
		    isEmpty(borders.getInsideV()) &&
		    isEmpty(borders.getTl2Br()) &&
		    isEmpty(borders.getTr2Bl())
		    );
	}

	public static boolean isEmpty(VMerge merge) {
		return (merge == null) || isEmpty(merge.getVal());
	}

	public static boolean isEmpty(HMerge merge) {
		return (merge == null) || isEmpty(merge.getVal());
	}

	public static boolean isEmpty(GridSpan gridSpan) {
		return (gridSpan == null) || isEmpty(gridSpan.getVal());
	}

	public static boolean isEmpty(TrPr trPr) {
		return (trPr == null) || 
			(trPr.getCnfStyleOrDivIdOrGridBefore() == null) || 
			(trPr.getCnfStyleOrDivIdOrGridBefore().isEmpty());
	}

	public static boolean isEmpty(CTHeight height) {
		//HRule is ignored
		return (height == null) || isEmpty(height.getVal());
	}

	public static boolean isEmpty(TblWidth width) {
		//type is ignored
		return (width == null) || isEmpty(width.getW());
	}

	public static boolean isEmpty(CTShortHexNumber number) {
		return (number == null) || isEmpty(number.getVal());
	}

	public static boolean isEmpty(BooleanDefaultTrue booleanDefaultTrue) {
		return (booleanDefaultTrue == null);  // we want to apply eg <w:i w:val="0"/>
		//|| (!booleanDefaultTrue.isVal());
	}

	public static boolean isEmpty(Boolean bool) {
		return (bool == null) || (!bool.booleanValue());
	}

	protected static boolean isEmpty(BigInteger val) {
		return (val == null); // want to apply 0 value!
		//|| (val.equals(BigInteger.ZERO));
	}

	protected static boolean isEmpty(Integer val) {
		return (val == null); // want to apply 0 value!
		//|| (val.intValue() == 0);
	}

	protected static boolean isEmpty(String val) {
		return (val == null) || (val.length() == 0);
	}

	protected static boolean isEmpty(STThemeColor val) {
		return (val == null) || (val.equals(STThemeColor.NONE));
	}
	
	protected static boolean isEmpty(SectPr val) {
		
		if (val==null) return true;
		
		log.debug("TODO: isEmpty(SectPr) implementation is quite basic");
		
		return false;
	}
	
/////////////////////////////////////////////
//apply-Methods
//
// see similar ImmutablePropertyResolver
//	
/////////////////////////////////////////////

	
	/**
	 * @param source
	 * @param destination
	 * @return
	 * @since 3.3.0
	 */
	public static Style apply(PPr source, Style destination) {

		if (!isEmpty(source)) {
			
			if (destination == null) {
				destination = Context.getWmlObjectFactory().createStyle();
				// what style type, though?
			} 
		
			if (CHARACTER_STYLE.equals(destination.getType())) {
				
				log.warn("Can't apply PPr to a character style!");
				
			}
			else  {
				
				destination.setPPr(apply(source, destination.getPPr()));
			}
		}
		return destination;
	}

	/**
	 * @param source
	 * @param destination
	 * @return
	 * @since 3.3.0
	 */
	public static Style apply(RPr source, Style destination) {

		if (!isEmpty(source)) {
			
			if (destination == null) {
				destination = Context.getWmlObjectFactory().createStyle();
				// what style type, though?
			} 
			destination.setRPr(apply(source, destination.getRPr()));
		}
		return destination;
	}
	
	
	/**
	 * Note that this method does not climb the hierarchy
	 * to take any account of what these styles are basedOn
	 * (other than to set the basedOn value)
	 * 
	 * @param source
	 * @param destination
	 */
	public static Style apply(Style source, Style destination) {

		if (isEmpty(source)) {
			
			log.debug("empty source style");
			
		} else {
			
			if (destination == null) {
				log.debug("new destination style");
				destination = Context.getWmlObjectFactory().createStyle();
				if (source.getType()!=null) {
					destination.setType(source.getType());
				}
			} else {
				if (areEqual(source.getType(), destination.getType())) {
					// good, as expected
				} else if (destination.getType()==null) {
					log.warn("Setting destination style type from source type " + source.getType());
					destination.setType(source.getType());
				} else {
					throw new RuntimeException("Source style type " + source.getType()
							+ " does not match destination type " + destination.getType());
					// Maybe there are some scenarios where you want to apply
					// say the rpr component of a p style to a run level style
					// but wait until need is proven.
					// You could still do that, by using apply(RPr, RPr).
					// Better to do type like checking here, and force explicit intention
				}
			}
		
			if (CHARACTER_STYLE.equals(source.getType())) {
				
				destination.setRPr(apply(source.getRPr(), destination.getRPr()));
			}
			else if (PARAGRAPH_STYLE.equals(source.getType()) || 
					 NUMBERING_STYLE.equals(source.getType())) {
				
				destination.setPPr(apply(source.getPPr(), destination.getPPr()));
				destination.setRPr(apply(source.getRPr(), destination.getRPr()));
			}
			else if (TABLE_STYLE.equals(source.getType())) {
				
				destination.setTblPr(apply(source.getTblPr(), destination.getTblPr()));
				destination.setTcPr(apply(source.getTcPr(), destination.getTcPr()));
				
				apply(source.getTblStylePr(), destination.getTblStylePr());
				
				destination.setPPr(apply(source.getPPr(), destination.getPPr()));
				destination.setRPr(apply(source.getRPr(), destination.getRPr()));
			}
			else {

				log.warn("source style type is currently unknown or null");
				
				destination.setTblPr(apply(source.getTblPr(), destination.getTblPr()));
				destination.setTcPr(apply(source.getTcPr(), destination.getTcPr()));
				
				apply(source.getTblStylePr(), destination.getTblStylePr());
				
				destination.setPPr(apply(source.getPPr(), destination.getPPr()));
				destination.setRPr(apply(source.getRPr(), destination.getRPr()));
			}
		}
		return destination;
	}

	public static PPr apply(PPr source, PPr destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createPPr();
			apply((PPrBase)source, (PPrBase)destination);
			destination.setRPr(apply(source.getRPr(), destination.getRPr()));
			destination.setSectPr(apply(source.getSectPr(), destination.getSectPr()));
		}
		return destination;
	}

	public static void apply(PPrBase source, PPrBase destination) {
		
	//PPrBase as a Base class isn't instantiated
		if (!isEmpty((PPrBase)source)) {
			destination.setPStyle(apply(source.getPStyle(), destination.getPStyle()));	
			destination.setKeepNext(apply(source.getKeepNext(), destination.getKeepNext()));	
			destination.setKeepLines(apply(source.getKeepLines(), destination.getKeepLines()));	
			destination.setPageBreakBefore(apply(source.getPageBreakBefore(), destination.getPageBreakBefore()));	
			destination.setFramePr(apply(source.getFramePr(), destination.getFramePr()));	
			destination.setWidowControl(apply(source.getWidowControl(), destination.getWidowControl()));	
			destination.setNumPr(apply(source.getNumPr(), destination.getNumPr()));	
			destination.setSuppressLineNumbers(apply(source.getSuppressLineNumbers(), destination.getSuppressLineNumbers()));	
			destination.setPBdr(apply(source.getPBdr(), destination.getPBdr()));	
			destination.setShd(apply(source.getShd(), destination.getShd()));	
			destination.setTabs(apply(source.getTabs(), destination.getTabs()));	
			destination.setSuppressAutoHyphens(apply(source.getSuppressAutoHyphens(), destination.getSuppressAutoHyphens()));	
			destination.setKinsoku(apply(source.getKinsoku(), destination.getKinsoku()));	
			destination.setWordWrap(apply(source.getWordWrap(), destination.getWordWrap()));	
			destination.setOverflowPunct(apply(source.getOverflowPunct(), destination.getOverflowPunct()));	
			destination.setTopLinePunct(apply(source.getTopLinePunct(), destination.getTopLinePunct()));	
			destination.setAutoSpaceDE(apply(source.getAutoSpaceDE(), destination.getAutoSpaceDE()));	
			destination.setAutoSpaceDN(apply(source.getAutoSpaceDN(), destination.getAutoSpaceDN()));	
			destination.setBidi(apply(source.getBidi(), destination.getBidi()));	
			destination.setAdjustRightInd(apply(source.getAdjustRightInd(), destination.getAdjustRightInd()));	
			destination.setSnapToGrid(apply(source.getSnapToGrid(), destination.getSnapToGrid()));
			
			destination.setSpacing(apply(source.getSpacing(), destination.getSpacing()));	
			
			destination.setInd(apply(source.getInd(), destination.getInd()));
			
			destination.setContextualSpacing(apply(source.getContextualSpacing(), destination.getContextualSpacing()));	
			destination.setMirrorIndents(apply(source.getMirrorIndents(), destination.getMirrorIndents()));	
			destination.setSuppressOverlap(apply(source.getSuppressOverlap(), destination.getSuppressOverlap()));	
			destination.setJc(apply(source.getJc(), destination.getJc()));	
			destination.setTextDirection(apply(source.getTextDirection(), destination.getTextDirection()));	
			destination.setTextAlignment(apply(source.getTextAlignment(), destination.getTextAlignment()));	
			destination.setTextboxTightWrap(apply(source.getTextboxTightWrap(), destination.getTextboxTightWrap()));	
			destination.setOutlineLvl(apply(source.getOutlineLvl(), destination.getOutlineLvl()));
			destination.setCnfStyle(apply(source.getCnfStyle(), destination.getCnfStyle()));	
		}
	}
	
	/**
	 * @param source
	 * @param destination
	 * @return
	 * @since 3.2
	 */
	public static SectPr apply(SectPr source, SectPr destination) {
		
		if (!isEmpty(source)) {
			
			if (destination == null) {
				destination = Context.getWmlObjectFactory().createSectPr();
			}
			
			destination.setFormProt(apply(source.getFormProt() , destination.getFormProt()));
			destination.setVAlign(apply(source.getVAlign() , destination.getVAlign()) );
			destination.setNoEndnote(apply(source.getNoEndnote(), destination.getNoEndnote()) );
			destination.setTitlePg(apply(source.getTitlePg() , destination.getTitlePg()) );
			destination.setTextDirection(apply(source.getTextDirection(), destination.getTextDirection()) );
			destination.setBidi(apply(source.getBidi(), destination.getBidi()) );
			destination.setRtlGutter(apply(source.getRtlGutter(), destination.getRtlGutter() ));
					
			log.warn("TODO: implementation is incomplete");
					
// TODO						
//						destination.setDocGrid(apply(source.getDocGrid() , destination.getDocGrid()) );
//						destination.setEGHdrFtrReferences(apply(source.getEGHdrFtrReferences(), destination.getEGHdrFtrReferences()) );
//						destination.setFootnotePr(apply(source.getFootnotePr(), destination.getFootnotePr()) );
//						destination.setEndnotePr(apply(source.getEndnotePr(), destination.getEndnotePr()) );
//						destination.setType(apply(source.getType() , destination.getType()) );
//						destination.setPgSz(apply(source.getPgSz() , destination.getPgSz()) );
//						destination.setPgMar(apply(source.getPgMar(), destination.getPgMar()) );
//						destination.setPaperSrc(apply(source.getPaperSrc(), destination.getPaperSrc()) );
//						destination.setPgBorders(apply(source.getPgBorders() , destination.getPgBorders()) );
//						destination.setLnNumType(apply(source.getLnNumType(), destination.getLnNumType()) );
//						destination.setPgNumType(apply(source.getPgNumType(), destination.getPgNumType()) );
//						destination.setCols(apply(source.getCols() , destination.getCols()) );
//						destination.setPrinterSettings(apply(source.getPrinterSettings(), destination.getPrinterSettings()) );
//						destination.setFootnoteColumns(apply(source.getFootnoteColumns(), destination.getFootnoteColumns()) 
					  
		}
		return destination;												
	}	

	public static RPr apply(RPr source, RPr destination) {
		
		boolean hint = false;
		if (source!=null
				&& source.getRFonts()!=null
				&& !isEmpty(source.getRFonts().getHint())) {
			hint = true; 
			log.debug("source rPr contains rFonts with hint");
		}

		if (isEmpty(source) && !hint ) {
			log.debug("no source rPr to apply");			
		} else {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createRPr();
			
			destination.setLang(apply(source.getLang(), destination.getLang()));
			destination.setRStyle(apply(source.getRStyle(), destination.getRStyle()));
			destination.setRFonts(apply(source.getRFonts(), destination.getRFonts()));
			destination.setB(apply(source.getB(), destination.getB()));
			destination.setBCs(apply(source.getBCs(), destination.getBCs()));
			destination.setI(apply(source.getI(), destination.getI()));
			destination.setICs(apply(source.getICs(), destination.getICs()));
			destination.setCaps(apply(source.getCaps(), destination.getCaps()));
			destination.setSmallCaps(apply(source.getSmallCaps(), destination.getSmallCaps()));
			destination.setStrike(apply(source.getStrike(), destination.getStrike()));
			destination.setDstrike(apply(source.getDstrike(), destination.getDstrike()));
			destination.setOutline(apply(source.getOutline(), destination.getOutline()));
			destination.setShadow(apply(source.getShadow(), destination.getShadow()));
			destination.setEmboss(apply(source.getEmboss(), destination.getEmboss()));
			destination.setImprint(apply(source.getImprint(), destination.getImprint()));
			destination.setSnapToGrid(apply(source.getSnapToGrid(), destination.getSnapToGrid()));
			destination.setVanish(apply(source.getVanish(), destination.getVanish()));
			destination.setColor(apply(source.getColor(), destination.getColor()));
			destination.setSpacing(apply(source.getSpacing(), destination.getSpacing()));
			destination.setW(apply(source.getW(), destination.getW()));
			destination.setKern(apply(source.getKern(), destination.getKern()));
			destination.setPosition(apply(source.getPosition(), destination.getPosition()));
			destination.setSz(apply(source.getSz(), destination.getSz()));
			destination.setSzCs(apply(source.getSzCs(), destination.getSzCs()));
			destination.setHighlight(apply(source.getHighlight(), destination.getHighlight()));
			destination.setU(apply(source.getU(), destination.getU()));
			destination.setEffect(apply(source.getEffect(), destination.getEffect()));
			destination.setBdr(apply(source.getBdr(), destination.getBdr()));
			destination.setShd(apply(source.getShd(), destination.getShd()));
			destination.setVertAlign(apply(source.getVertAlign(), destination.getVertAlign()));
			destination.setRtl(apply(source.getRtl(), destination.getRtl()));
			destination.setCs(apply(source.getCs(), destination.getCs()));
			destination.setEm(apply(source.getEm(), destination.getEm()));
			destination.setSpecVanish(apply(source.getSpecVanish(), destination.getSpecVanish()));
			destination.setOMath(apply(source.getOMath(), destination.getOMath()));
		}
		return destination;
	}
	
	public static CTLanguage apply(CTLanguage source, CTLanguage destination) {
		// TODO refine this?
		return ((source == null) ? destination : source);
	}
	

	public static RPr apply(ParaRPr source, RPr destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createRPr();
			
			destination.setRStyle(apply(source.getRStyle(), destination.getRStyle()));
			destination.setRFonts(apply(source.getRFonts(), destination.getRFonts()));
			destination.setB(apply(source.getB(), destination.getB()));
			destination.setBCs(apply(source.getBCs(), destination.getBCs()));
			destination.setI(apply(source.getI(), destination.getI()));
			destination.setICs(apply(source.getICs(), destination.getICs()));
			destination.setCaps(apply(source.getCaps(), destination.getCaps()));
			destination.setSmallCaps(apply(source.getSmallCaps(), destination.getSmallCaps()));
			destination.setStrike(apply(source.getStrike(), destination.getStrike()));
			destination.setDstrike(apply(source.getDstrike(), destination.getDstrike()));
			destination.setOutline(apply(source.getOutline(), destination.getOutline()));
			destination.setShadow(apply(source.getShadow(), destination.getShadow()));
			destination.setEmboss(apply(source.getEmboss(), destination.getEmboss()));
			destination.setImprint(apply(source.getImprint(), destination.getImprint()));
			destination.setSnapToGrid(apply(source.getSnapToGrid(), destination.getSnapToGrid()));
			destination.setVanish(apply(source.getVanish(), destination.getVanish()));
			destination.setColor(apply(source.getColor(), destination.getColor()));
			destination.setSpacing(apply(source.getSpacing(), destination.getSpacing()));
			destination.setW(apply(source.getW(), destination.getW()));
			destination.setKern(apply(source.getKern(), destination.getKern()));
			destination.setPosition(apply(source.getPosition(), destination.getPosition()));
			destination.setSz(apply(source.getSz(), destination.getSz()));
			destination.setSzCs(apply(source.getSzCs(), destination.getSzCs()));
			destination.setHighlight(apply(source.getHighlight(), destination.getHighlight()));
			destination.setU(apply(source.getU(), destination.getU()));
			destination.setEffect(apply(source.getEffect(), destination.getEffect()));
			destination.setBdr(apply(source.getBdr(), destination.getBdr()));
			destination.setShd(apply(source.getShd(), destination.getShd()));
			destination.setVertAlign(apply(source.getVertAlign(), destination.getVertAlign()));
			destination.setRtl(apply(source.getRtl(), destination.getRtl()));
			destination.setCs(apply(source.getCs(), destination.getCs()));
			destination.setEm(apply(source.getEm(), destination.getEm()));
			destination.setSpecVanish(apply(source.getSpecVanish(), destination.getSpecVanish()));
			destination.setOMath(apply(source.getOMath(), destination.getOMath()));
		}
		return destination;
	}

	public static ParaRPr apply(RPr source, ParaRPr destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createParaRPr();
			
			destination.setRStyle(apply(source.getRStyle(), destination.getRStyle()));
			destination.setRFonts(apply(source.getRFonts(), destination.getRFonts()));
			destination.setB(apply(source.getB(), destination.getB()));
			destination.setBCs(apply(source.getBCs(), destination.getBCs()));
			destination.setI(apply(source.getI(), destination.getI()));
			destination.setICs(apply(source.getICs(), destination.getICs()));
			destination.setCaps(apply(source.getCaps(), destination.getCaps()));
			destination.setSmallCaps(apply(source.getSmallCaps(), destination.getSmallCaps()));
			destination.setStrike(apply(source.getStrike(), destination.getStrike()));
			destination.setDstrike(apply(source.getDstrike(), destination.getDstrike()));
			destination.setOutline(apply(source.getOutline(), destination.getOutline()));
			destination.setShadow(apply(source.getShadow(), destination.getShadow()));
			destination.setEmboss(apply(source.getEmboss(), destination.getEmboss()));
			destination.setImprint(apply(source.getImprint(), destination.getImprint()));
			destination.setSnapToGrid(apply(source.getSnapToGrid(), destination.getSnapToGrid()));
			destination.setVanish(apply(source.getVanish(), destination.getVanish()));
			destination.setColor(apply(source.getColor(), destination.getColor()));
			destination.setSpacing(apply(source.getSpacing(), destination.getSpacing()));
			destination.setW(apply(source.getW(), destination.getW()));
			destination.setKern(apply(source.getKern(), destination.getKern()));
			destination.setPosition(apply(source.getPosition(), destination.getPosition()));
			destination.setSz(apply(source.getSz(), destination.getSz()));
			destination.setSzCs(apply(source.getSzCs(), destination.getSzCs()));
			destination.setHighlight(apply(source.getHighlight(), destination.getHighlight()));
			destination.setU(apply(source.getU(), destination.getU()));
			destination.setEffect(apply(source.getEffect(), destination.getEffect()));
			destination.setBdr(apply(source.getBdr(), destination.getBdr()));
			destination.setShd(apply(source.getShd(), destination.getShd()));
			destination.setVertAlign(apply(source.getVertAlign(), destination.getVertAlign()));
			destination.setRtl(apply(source.getRtl(), destination.getRtl()));
			destination.setCs(apply(source.getCs(), destination.getCs()));
			destination.setEm(apply(source.getEm(), destination.getEm()));
			destination.setSpecVanish(apply(source.getSpecVanish(), destination.getSpecVanish()));
			destination.setOMath(apply(source.getOMath(), destination.getOMath()));
		}
		return destination;
	}

	public static ParaRPr apply(ParaRPr source, ParaRPr destination) {

		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createParaRPr();
			
			destination.setRStyle(apply(source.getRStyle(), destination.getRStyle()));
			destination.setRFonts(apply(source.getRFonts(), destination.getRFonts()));
			destination.setB(apply(source.getB(), destination.getB()));
			destination.setBCs(apply(source.getBCs(), destination.getBCs()));
			destination.setI(apply(source.getI(), destination.getI()));
			destination.setICs(apply(source.getICs(), destination.getICs()));
			destination.setCaps(apply(source.getCaps(), destination.getCaps()));
			destination.setSmallCaps(apply(source.getSmallCaps(), destination.getSmallCaps()));
			destination.setStrike(apply(source.getStrike(), destination.getStrike()));
			destination.setDstrike(apply(source.getDstrike(), destination.getDstrike()));
			destination.setOutline(apply(source.getOutline(), destination.getOutline()));
			destination.setShadow(apply(source.getShadow(), destination.getShadow()));
			destination.setEmboss(apply(source.getEmboss(), destination.getEmboss()));
			destination.setImprint(apply(source.getImprint(), destination.getImprint()));
			destination.setSnapToGrid(apply(source.getSnapToGrid(), destination.getSnapToGrid()));
			destination.setVanish(apply(source.getVanish(), destination.getVanish()));
			destination.setColor(apply(source.getColor(), destination.getColor()));
			destination.setSpacing(apply(source.getSpacing(), destination.getSpacing()));
			destination.setW(apply(source.getW(), destination.getW()));
			destination.setKern(apply(source.getKern(), destination.getKern()));
			destination.setPosition(apply(source.getPosition(), destination.getPosition()));
			destination.setSz(apply(source.getSz(), destination.getSz()));
			destination.setSzCs(apply(source.getSzCs(), destination.getSzCs()));
			destination.setHighlight(apply(source.getHighlight(), destination.getHighlight()));
			destination.setU(apply(source.getU(), destination.getU()));
			destination.setEffect(apply(source.getEffect(), destination.getEffect()));
			destination.setBdr(apply(source.getBdr(), destination.getBdr()));
			destination.setShd(apply(source.getShd(), destination.getShd()));
			destination.setVertAlign(apply(source.getVertAlign(), destination.getVertAlign()));
			destination.setRtl(apply(source.getRtl(), destination.getRtl()));
			destination.setCs(apply(source.getCs(), destination.getCs()));
			destination.setEm(apply(source.getEm(), destination.getEm()));
			destination.setSpecVanish(apply(source.getSpecVanish(), destination.getSpecVanish()));
			destination.setOMath(apply(source.getOMath(), destination.getOMath()));
		}
		return destination;
	}
	
	public static CTTblPrBase apply(CTTblPrBase source, CTTblPrBase destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createTblPr();
			
			destination.setBidiVisual(apply(source.getBidiVisual(), destination.getBidiVisual()));
			
			destination.setTblStyle(apply(source.getTblStyle(), destination.getTblStyle()));
			destination.setTblpPr(apply(source.getTblpPr(), destination.getTblpPr()));
			destination.setTblOverlap(apply(source.getTblOverlap(), destination.getTblOverlap()));
			destination.setTblStyleRowBandSize(apply(source.getTblStyleRowBandSize(), destination.getTblStyleRowBandSize()));
			destination.setTblStyleColBandSize(apply(source.getTblStyleColBandSize(), destination.getTblStyleColBandSize()));
			destination.setTblW(apply(source.getTblW(), destination.getTblW()));
			destination.setJc(apply(source.getJc(), destination.getJc()));
			destination.setTblCellSpacing(apply(source.getTblCellSpacing(), destination.getTblCellSpacing()));
			destination.setTblInd(apply(source.getTblInd(), destination.getTblInd()));
			destination.setTblBorders(apply(source.getTblBorders(), destination.getTblBorders()));
			destination.setShd(apply(source.getShd(), destination.getShd()));
			destination.setTblLayout(apply(source.getTblLayout(), destination.getTblLayout()));
			destination.setTblCellMar(apply(source.getTblCellMar(), destination.getTblCellMar()));
			destination.setTblLook(apply(source.getTblLook(), destination.getTblLook()));
		}
		return destination;
	}

	public static TcPr apply(TcPr source, TcPr destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createTcPr();
			
			destination.setCnfStyle(apply(source.getCnfStyle(), destination.getCnfStyle()));
			destination.setTcW(apply(source.getTcW(), destination.getTcW()));
			destination.setGridSpan(apply(source.getGridSpan(), destination.getGridSpan()));
			destination.setHMerge(apply(source.getHMerge(), destination.getHMerge()));
			destination.setVMerge(apply(source.getVMerge(), destination.getVMerge()));
			destination.setTcBorders(apply(source.getTcBorders(), destination.getTcBorders()));
			destination.setShd(apply(source.getShd(), destination.getShd()));
			destination.setNoWrap(apply(source.getNoWrap(), destination.getNoWrap()));
			destination.setTcMar(apply(source.getTcMar(), destination.getTcMar()));
			destination.setTextDirection(apply(source.getTextDirection(), destination.getTextDirection()));
			destination.setTcFitText(apply(source.getTcFitText(), destination.getTcFitText()));
			destination.setVAlign(apply(source.getVAlign(), destination.getVAlign()));
			destination.setHideMark(apply(source.getHideMark(), destination.getHideMark()));
		}
		return destination;
	}

	public static void apply(List<CTTblStylePr> source, List<CTTblStylePr> destination) {
		CTTblStylePr destinationTblStylePr = null;
		if (!isEmpty(source)) {
			// not sure about this, but if the source defines a content model it should
			// replace the destination as a whole, and not parts of it.
			destination.clear();
			for (int i=0; i<source.size(); i++) {
				destinationTblStylePr = apply(source.get(i), null);
				if (destinationTblStylePr != null) {
					destination.add(destinationTblStylePr);
				}
			}
		}
	}

	public static CTTblLook apply(CTTblLook source, CTTblLook destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblLook();
			destination.setFirstColumn(apply(source.getFirstColumn(), destination.getFirstColumn()));
			destination.setFirstRow(apply(source.getFirstRow(), destination.getFirstRow()));
			destination.setLastColumn(apply(source.getLastColumn(), destination.getLastColumn()));
			destination.setLastRow(apply(source.getLastRow(), destination.getLastRow()));
			destination.setNoHBand(apply(source.getNoHBand(), destination.getNoHBand()));
			destination.setNoVBand(apply(source.getNoVBand(), destination.getNoVBand()));
			destination.setVal(apply(source.getVal(), destination.getVal()));
		}
		return destination;
	}
	
	private static STOnOff apply(STOnOff source, STOnOff destination) {
		return (source == null ? destination : source);
	}

	public static CTTblStylePr apply(CTTblStylePr source, CTTblStylePr destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblStylePr();
			destination.setPPr(apply(source.getPPr(), destination.getPPr()));
			destination.setRPr(apply(source.getRPr(), destination.getRPr()));
			destination.setTblPr(apply(source.getTblPr(), destination.getTblPr()));
			destination.setTcPr(apply(source.getTcPr(), destination.getTcPr()));
			destination.setTrPr(apply(source.getTrPr(), destination.getTrPr()));
			destination.setType(source.getType()); //enum
		}
		return destination;
	}

	public static OutlineLvl apply(OutlineLvl source, OutlineLvl destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createPPrBaseOutlineLvl();
			destination.setVal(source.getVal()); //atomic
		}
		return destination;
	}

	public static CTTextboxTightWrap apply(CTTextboxTightWrap source, CTTextboxTightWrap destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createCTTextboxTightWrap();
			destination.setVal(source.getVal()); //enum
		}
		return destination;
	}

	public static TextAlignment apply(TextAlignment source, TextAlignment destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createPPrBaseTextAlignment();
			destination.setVal(source.getVal()); //atomic
		}
		return destination;
	}

	public static TextDirection apply(TextDirection source, TextDirection destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createTextDirection();
			destination.setVal(source.getVal()); //atomic
		}
		return destination;
	}

	public static Jc apply(Jc source, Jc destination) {
		if (!isEmpty(source)) {
			if (destination == null) 
				destination = Context.getWmlObjectFactory().createJc();
			destination.setVal(source.getVal()); //enum
		}
		return destination;
	}

	public static Ind apply(Ind source, Ind destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createPPrBaseInd();
			
			destination.setFirstLine(apply(source.getFirstLine(), destination.getFirstLine()));
			destination.setFirstLineChars(apply(source.getFirstLineChars(), destination.getFirstLineChars()));
			destination.setHanging(apply(source.getHanging(), destination.getHanging()));
			destination.setHangingChars(apply(source.getHangingChars(), destination.getHangingChars()));
			destination.setLeft(apply(source.getLeft(), destination.getLeft()));
			destination.setLeftChars(apply(source.getLeftChars(), destination.getLeftChars()));
			destination.setRight(apply(source.getRight(), destination.getRight()));
			destination.setRightChars(apply(source.getRightChars(), destination.getRightChars()));
		}
		return destination;
	}

	public static Spacing apply(Spacing source, Spacing destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createPPrBaseSpacing();
			
			destination.setAfter(apply(source.getAfter(), destination.getAfter()));
			destination.setAfterLines(apply(source.getAfterLines(), destination.getAfterLines()));
			destination.setBefore(apply(source.getBefore(), destination.getBefore()));
			destination.setBeforeLines(apply(source.getBeforeLines(), destination.getBeforeLines()));
			destination.setLine(apply(source.getLine(), destination.getLine()));
			destination.setLineRule(apply(source.getLineRule(), destination.getLineRule()));
		}
		return destination;
	}

	public static STLineSpacingRule apply(STLineSpacingRule source, STLineSpacingRule destination) {
		// defaults to auto
		return (source == null ? STLineSpacingRule.AUTO : source);
	}
	
	public static Tabs apply(Tabs source, Tabs destination) {
	CTTabStop sourceTabStop = null;
	CTTabStop destinationTabStop = null;
		//Tabs are relative to each other, therefore if there are any tabs in the source 
		//they should replace those in the destination and not be added to the destination.
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createTabs();
			
			destination.getTab().clear();
			for (int i=0; i<source.getTab().size(); i++) {
				sourceTabStop = source.getTab().get(i);
				destinationTabStop = Context.getWmlObjectFactory().createCTTabStop();
				destinationTabStop.setLeader(sourceTabStop.getLeader()); //enum
				destinationTabStop.setPos(sourceTabStop.getPos()); //atomic
				destinationTabStop.setVal(sourceTabStop.getVal()); //enum
				
				if (destinationTabStop != null) {
					destination.getTab().add(destinationTabStop);
				}
			}
		}
		return destination;
	}

	public static CTShd apply(CTShd source, CTShd destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTShd();
			
			destination.setColor(apply(source.getColor(), destination.getColor()));
			destination.setFill(apply(source.getFill(), destination.getFill()));
			destination.setVal(apply(source.getVal(), destination.getVal()));
			
			destination.setThemeTint(apply(source.getThemeTint(), destination.getThemeTint()));
			destination.setThemeShade(apply(source.getThemeShade(), destination.getThemeShade()));
			destination.setThemeFillTint(apply(source.getThemeFillTint(), destination.getThemeFillTint()));
			destination.setThemeFillShade(apply(source.getThemeFillShade(), destination.getThemeFillShade()));
			destination.setThemeColor(source.getThemeColor()); //enum
			destination.setThemeFill(source.getThemeFill()); //enum
		}
		return destination;
	}

	public static PBdr apply(PBdr source, PBdr destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createPPrBasePBdr();
			
			destination.setTop(apply(source.getTop(), destination.getTop())); 
			destination.setLeft(apply(source.getLeft(), destination.getLeft()));
			destination.setBottom(apply(source.getBottom(), destination.getBottom()));
			destination.setRight(apply(source.getRight(), destination.getRight()));
			destination.setBetween(apply(source.getBetween(), destination.getBetween()));
			destination.setBar(apply(source.getBar(), destination.getBar()));
		}
		return destination;
	}

	public static CTBorder apply(CTBorder source, CTBorder destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTBorder();
			
			destination.setColor(apply(source.getColor(), destination.getColor()));
			destination.setSpace(apply(source.getSpace(), destination.getSpace())); 
			destination.setSz(apply(source.getSz(), destination.getSz())); 
			destination.setThemeColor(source.getThemeColor()); 
			destination.setThemeShade(apply(source.getThemeShade(), destination.getThemeShade())); 
			destination.setThemeTint(apply(source.getThemeTint(), destination.getThemeTint()));
			destination.setVal(apply(source.getVal(), destination.getVal()));
		}
		return destination;
	}

	public static NumPr apply(NumPr source, NumPr destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createPPrBaseNumPr();
			
			if ((source.getNumId() != null) || (source.getIlvl() != null)) {
				destination.setNumId(source.getNumId());
				destination.setIlvl(source.getIlvl());
			}
		}
		return destination;
	}

	public static CTFramePr apply(CTFramePr source, CTFramePr destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTFramePr();
			
			destination.setDropCap(source.getDropCap());
			destination.setLines(apply(source.getLines(), destination.getLines()));
			destination.setW(apply(source.getW(), destination.getW()));
			destination.setH(apply(source.getH(), destination.getH()));
			destination.setVSpace(apply(source.getVSpace(), destination.getVSpace())); 
			destination.setHSpace(apply(source.getHSpace(), destination.getHSpace()));
			destination.setWrap(source.getWrap());
			destination.setHAnchor(source.getHAnchor());
			destination.setVAnchor(source.getVAnchor());
			destination.setX(apply(source.getX(), destination.getX()));
			destination.setXAlign(source.getXAlign()); 
			destination.setY(apply(source.getY(), destination.getY()));
			destination.setYAlign(source.getYAlign());
			destination.setHRule(source.getHRule());
			destination.setAnchorLock(source.isAnchorLock());
		}
		return destination;
	}

	public static CTCnf apply(CTCnf source, CTCnf destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTCnf();
			destination.setVal(apply(source.getVal(), destination.getVal()));
		}
		return destination;
	}

	public static PStyle apply(PStyle source, PStyle destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createPPrBasePStyle();
			destination.setVal(apply(source.getVal(), destination.getVal()));
		}
		return destination;
	}

	public static RFonts apply(RFonts source, RFonts destination) {
		
		if (destination == null)
			destination = Context.getWmlObjectFactory().createRFonts();
		
		if (isEmpty(source) ) {
			
			if (source!=null && source.getHint()!=null) {
				destination.setHint(source.getHint());
			} else {
				//return null; // causes RunFontSelectorChinese2Test to fail
				// if we don't return null, it creates empty rFonts element
				// see comment at line 401 of RunFontSelector
			}
			
		} else {
			
			if (source.getAscii() != null) {
				destination.setAscii(source.getAscii());
				destination.setAsciiTheme(null); 
				// theme trumps non theme, but here destination is "lower" than source
				// see http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/rFonts.html 
			}
			if (source.getCs() != null) {
				destination.setCs(source.getCs());
				destination.setCstheme(null);
			}
			if (source.getEastAsia() != null) {
				destination.setEastAsia(source.getEastAsia());
				destination.setEastAsiaTheme(null);				
			}
			if (source.getHAnsi() != null) {
				destination.setHAnsi(source.getHAnsi());
				destination.setHAnsiTheme(null);				
			}
			

			if (source.getAsciiTheme() != null) {
				destination.setAsciiTheme(source.getAsciiTheme());
				destination.setAscii(null);  // do this, since theme trumps non theme
			}
			if (source.getCstheme() != null) {
				destination.setCstheme(source.getCstheme());
				destination.setCs(null);				
			}
			if (source.getEastAsiaTheme() != null) {
				destination.setEastAsiaTheme(source.getEastAsiaTheme());
				destination.setEastAsia(null);				
			}
			if (source.getHAnsiTheme() != null) {
				destination.setHAnsiTheme(source.getHAnsiTheme());
				destination.setHAnsi(null);				
			}

			if (source.getHint() != null) {
				destination.setHint(source.getHint());
			}
		}
		return destination;
	}

	public static RStyle apply(RStyle source, RStyle destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createRStyle();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTEm apply(CTEm source, CTEm destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTEm();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTVerticalAlignRun apply(CTVerticalAlignRun source, CTVerticalAlignRun destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTVerticalAlignRun();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTTextEffect apply(CTTextEffect source, CTTextEffect destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTextEffect();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static U apply(U source, U destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createU();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static Highlight apply(Highlight source, Highlight destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createHighlight();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTSignedHpsMeasure apply(CTSignedHpsMeasure source, CTSignedHpsMeasure destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTSignedHpsMeasure();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static HpsMeasure apply(HpsMeasure source, HpsMeasure destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createHpsMeasure();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTTextScale apply(CTTextScale source, CTTextScale destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTextScale();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTSignedTwipsMeasure apply(CTSignedTwipsMeasure source, CTSignedTwipsMeasure destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTSignedTwipsMeasure();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static Color apply(Color source, Color destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createColor();

			destination.setThemeColor(apply(source.getThemeColor(), destination.getThemeColor()));
			destination.setThemeShade(apply(source.getThemeShade(), destination.getThemeShade()));
			destination.setThemeTint(apply(source.getThemeTint(), destination.getThemeTint()));
			destination.setVal(apply(source.getVal(), destination.getVal()));
		}
		return destination;
	}

	private static STThemeColor apply(STThemeColor source, STThemeColor destination) {
		return (isEmpty(source) ? destination : source);
	}

	public static CTTblLayoutType apply(CTTblLayoutType source, CTTblLayoutType destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblLayoutType();
			
			destination.setType(source.getType());
		}
		return destination;
	}

	public static CTTblCellMar apply(CTTblCellMar source, CTTblCellMar destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblCellMar();
			
			destination.setBottom(apply(source.getBottom(), destination.getBottom()));
			destination.setLeft(apply(source.getLeft(), destination.getLeft()));
			destination.setRight(apply(source.getRight(), destination.getRight()));
			destination.setTop(apply(source.getTop(), destination.getTop()));
		}
		return destination;
	}

	public static TblBorders apply(TblBorders source, TblBorders destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createTblBorders();
			
			destination.setBottom(apply(source.getBottom(), destination.getBottom()));
			destination.setLeft(apply(source.getLeft(), destination.getLeft()));
			destination.setRight(apply(source.getRight(), destination.getRight()));
			destination.setTop(apply(source.getTop(), destination.getTop()));
			destination.setInsideH(apply(source.getInsideH(), destination.getInsideH()));
			destination.setInsideV(apply(source.getInsideV(), destination.getInsideV()));
		}
		return destination;
	}

	public static TblStyleColBandSize apply(TblStyleColBandSize source, TblStyleColBandSize destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblPrBaseTblStyleColBandSize();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static TblStyleRowBandSize apply(TblStyleRowBandSize source, TblStyleRowBandSize destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblPrBaseTblStyleRowBandSize();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTTblOverlap apply(CTTblOverlap source, CTTblOverlap destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblOverlap();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTTblPPr apply(CTTblPPr source, CTTblPPr destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblPPr();
			
			destination.setLeftFromText(apply(source.getLeftFromText(), destination.getLeftFromText()));
			destination.setRightFromText(apply(source.getRightFromText(), destination.getRightFromText()));
			destination.setTopFromText(apply(source.getTopFromText(), destination.getTopFromText()));
			destination.setBottomFromText(apply(source.getBottomFromText(), destination.getBottomFromText()));
			destination.setVertAnchor(source.getVertAnchor());
			destination.setHorzAnchor(source.getHorzAnchor());
			destination.setTblpXSpec(source.getTblpXSpec());
			destination.setTblpX(apply(source.getTblpX(), destination.getTblpX()));
			destination.setTblpYSpec(source.getTblpYSpec());
			destination.setTblpY(apply(source.getTblpY(), destination.getTblpY()));		
		}
		return destination;
	}

	public static TblStyle apply(TblStyle source, TblStyle destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTTblPrBaseTblStyle();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static CTVerticalJc apply(CTVerticalJc source, CTVerticalJc destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTVerticalJc();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static TcMar apply(TcMar source, TcMar destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createTcMar();
			
			destination.setBottom(apply(source.getBottom(), destination.getBottom()));
			destination.setLeft(apply(source.getLeft(), destination.getLeft()));
			destination.setRight(apply(source.getRight(), destination.getRight()));
			destination.setTop(apply(source.getTop(), destination.getTop()));
		}
		return destination;
	}

	public static TcBorders apply(TcBorders source, TcBorders destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createTcPrInnerTcBorders();
				
			destination.setBottom(apply(source.getBottom(), destination.getBottom()));
			destination.setLeft(apply(source.getLeft(), destination.getLeft()));
			destination.setRight(apply(source.getRight(), destination.getRight()));
			destination.setTop(apply(source.getTop(), destination.getTop()));
			destination.setInsideH(apply(source.getInsideH(), destination.getInsideH()));
			destination.setInsideV(apply(source.getInsideV(), destination.getInsideV()));
			destination.setTl2Br(apply(source.getTl2Br(), destination.getTl2Br()));
			destination.setTr2Bl(apply(source.getTr2Bl(), destination.getTr2Bl()));
		}
		return destination;
	}

	public static VMerge apply(VMerge source, VMerge destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createTcPrInnerVMerge();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static HMerge apply(HMerge source, HMerge destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createTcPrInnerHMerge();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static GridSpan apply(GridSpan source, GridSpan destination) {
		if (!isEmpty(source)) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createTcPrInnerGridSpan();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	public static TrPr apply(TrPr source, TrPr destination) {
	List<JAXBElement<?>> defsSource = null;
	List<JAXBElement<?>> defsDestination = null;
	JAXBElement<?> defsSourceElement = null;
		if (isEmpty(source))
			return destination;
		if (!isEmpty(destination)) {
			defsSource = source.getCnfStyleOrDivIdOrGridBefore();
			defsDestination = destination.getCnfStyleOrDivIdOrGridBefore();

			for (int i=0; i<defsSource.size(); i++) {
				defsSourceElement = defsSource.get(i);
				// If there's an element with this name already, remove it
				for (int j=0; j<defsDestination.size(); j++) {
					if (defsSourceElement.getName().equals(defsDestination.get(j).getName())) {
						defsDestination.remove(j);
						break;
					}
				}
				// Now add the element
				defsDestination.add(XmlUtils.deepCopy(defsSourceElement));
			}
		}
		
		return destination;
	}

	public static TblWidth apply(TblWidth source, TblWidth destination) {
		
		if ((source != null) && (!isEmpty(source.getW()))) {
			if (destination == null) 	
				destination = Context.getWmlObjectFactory().createTblWidth();
			
			destination.setW(source.getW());
			
			// If @w:type is omitted, then its value shall be assumed to be dxa (twentieths of a point)
			// http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/tblW_2.html
			destination.setType(source.getType());
			
		}
		return destination;
	}

	public static CTShortHexNumber apply(CTShortHexNumber source, CTShortHexNumber destination) {
		if ((source != null) && (!isEmpty(source.getVal()))) {
			if (destination == null)
				destination = Context.getWmlObjectFactory().createCTShortHexNumber();
			
			destination.setVal(source.getVal());
		}
		return destination;
	}

	/**
	 * Tbl, Tc borders; PBdr, and RPr border.
	 * http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ST_Border.html
	 *  
	 * @param source
	 * @param destination
	 * @return
	 */
	private static STBorder apply(STBorder source, STBorder destination) {
		return (source == null  ? destination : source);
	}

	/**
	 * http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ST_Shd.html
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	public static STShd apply(STShd source, STShd destination) {
		return (source == null  ? destination : source);
	}
	
	/**
	 * http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ST_TblOverlap.html
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	public static STTblOverlap apply(STTblOverlap source, STTblOverlap destination) {
		return (source == null  ? destination : source);
	}

	public static STTblLayoutType apply(STTblLayoutType source, STTblLayoutType destination) {
		return (source == null ? destination : source);
	}

	public static UnderlineEnumeration apply(UnderlineEnumeration source, UnderlineEnumeration destination) {
		return (source == null  ? destination : source);
	}

	public static STTextEffect apply(STTextEffect source, STTextEffect destination) {
		return (source == null  ? destination : source);
	}

	public static STEm apply(STEm source, STEm destination) {
		return (source == null  ? destination : source);
	}

	public static STVerticalAlignRun apply(STVerticalAlignRun source, STVerticalAlignRun destination) {
		return (source == null  ? destination : source);
	}

	public static STVerticalJc apply(STVerticalJc source, STVerticalJc destination) {
		return (source == null  ? destination : source);
	}
	
	public static BooleanDefaultTrue apply(BooleanDefaultTrue source, BooleanDefaultTrue destination) {
		return (source == null ? destination : source);
	}

	public static Boolean apply(Boolean source, Boolean destination) {
		return (source == null ? destination : source);
	}

	// Need to honour eg <w:ind w:firstLine="0"/>
	protected static BigInteger apply(BigInteger source, BigInteger destination) {
		return (source == null  ? destination : source);
	}

//	protected static Integer apply(Integer source, Integer destination) {
//		return (source == null || source.intValue() == 0 ? destination : source);
//	}

	protected static String apply(String source, String destination) {
		return (source == null  ? destination : source);
	}
}
