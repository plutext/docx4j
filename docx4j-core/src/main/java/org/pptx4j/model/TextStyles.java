/*
 *  Copyright 2010, Plutext Pty Ltd.
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

package org.pptx4j.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.dml.BaseStyles.FontScheme;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextListStyle;
import org.docx4j.dml.CTTextParagraphProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.DocDefaults.RPrDefault;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Style.Name;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.pptx4j.pml.CTSlideMasterTextStyles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextStyles {
	
	private final static Logger log = LoggerFactory.getLogger(TextStyles.class);
	
	
	/*
	 * Given:
	 * 
	 *  [insert example]
	 * 
	 * we're aiming to create something like:
	 * 
//			<w:style w:type="paragraph" w:styleId="Heading1">
//				<w:name w:val="heading 1" />
//				<w:basedOn w:val="Normal" />
//				<w:next w:val="Normal" />
//				<w:link w:val="Heading1Char" />
//				<w:uiPriority w:val="9" />
//				<w:qFormat />
//				<w:pPr>
//					<w:keepNext />
//					<w:keepLines />
//					<w:numPr>
//						<w:numId w:val="3" />
//					</w:numPr>
//					<w:spacing w:before="480" w:after="0" />
//					<w:outlineLvl w:val="0" />
//				</w:pPr>
//				<w:rPr>
//					<w:rFonts w:asciiTheme="majorHAnsi" w:eastAsiaTheme="majorEastAsia" w:hAnsiTheme="majorHAnsi" w:cstheme="majorBidi" />
//					<w:b />
//					<w:bCs />
//					<w:color w:val="365F91" w:themeColor="accent1" w:themeShade="BF" />
//					<w:sz w:val="28" />
//					<w:szCs w:val="28" />
//				</w:rPr>
//			</w:style>
	 * 
	 * We'll also do something like 
	 * StyleDefinitionsPart.createVirtualStylesForDocDefaults()
	 * 
	 */
	
	public static Style convertToWordStyle(CTTextParagraphProperties lvlPPr, 
			String id, String name,
			String basedOn,
			FontScheme fontScheme) {
		
		ObjectFactory factory = Context.getWmlObjectFactory();
		Style style = factory.createStyle();
		
//		<w:style w:type="paragraph" w:styleId="Heading1">
		style.setType("paragraph");
		style.setStyleId(id);
		
		System.out.println("created " + id);
		
//		<w:name w:val="heading 1" />
		Name styleName = factory.createStyleName();
		styleName.setVal(name);
		style.setName(styleName);
		
//		<w:basedOn w:val="Normal" />
		Style.BasedOn basedon = factory.createStyleBasedOn();
		basedon.setVal(basedOn);
		style.setBasedOn(basedon);
		
//		<w:pPr>
		if (lvlPPr ==null) {
			log.warn("Empty style: " + id);
			if (log.isDebugEnabled() ) {
				
				log.debug( XmlUtils.marshaltoString(lvlPPr, true, true, Context.jc, 
						"URI", "lvl1pPr", // BEWARE: could be lvl2 etc
						CTTextParagraphProperties.class));
				
				log.debug("Converted to: " +  XmlUtils.marshaltoString(style, true, true));
			}
			return style; 
		}
		
		style.setPPr(
				getWmlPPr(lvlPPr) );
		
		style.setRPr(
				getWmlRPr(lvlPPr, fontScheme) );
				
		
		
		if (log.isDebugEnabled() ) {
			
			log.debug( XmlUtils.marshaltoString(lvlPPr, true, true, Context.jc, 
					"URI", "lvl1pPr", 
					CTTextParagraphProperties.class));
			
			log.debug("Converted to: " +  XmlUtils.marshaltoString(style, true, true));
		}
		
		return style;
	}
	
	public static PPr getWmlPPr(CTTextParagraphProperties lvlPPr ) {
		
		ObjectFactory factory = Context.getWmlObjectFactory();
		PPr pPr = factory.createPPr();		
		
//		*marL eg 342900 EMU
//		*indent eg -342900
		
//		*algn l|ctr|r
		if (lvlPPr.getAlgn()!=null) {
			Jc jc = factory.createJc();
			String algn = lvlPPr.getAlgn().value();
			log.debug("algn: " + algn);
			if (  algn.equals("l")) {
				jc.setVal(JcEnumeration.LEFT);
			} else if (algn.equals("ctr")) {
				jc.setVal(JcEnumeration.CENTER);
			} else if (algn.equals("r")) {
				jc.setVal(JcEnumeration.RIGHT);
	//		} else if (value.getCssText().toLowerCase().equals("justify")) {
	//			jc.setVal(JcEnumeration.BOTH);
			} else {
				log.warn("How to handle algn: " + algn);
			}		
			pPr.setJc( jc );
		}
		
//		defTabSz eg 914400 EMU = 1 inch
//		hangingPunct?

		// spcBef %
//		<w:spacing w:before="480" w:after="0" />
			
	//		*buFont, buChar
//	    <a:buFont charset="0" pitchFamily="34" typeface="Arial"/>
//	    <a:buChar char="•"/>

		return pPr;
	}
	
	public static RPr getWmlRPr(CTTextParagraphProperties lvlPPr,
		FontScheme fontScheme) {
		
		ObjectFactory factory = Context.getWmlObjectFactory();

//		<w:rPr>
		RPr rPr = factory.createRPr();

		if (lvlPPr.getDefRPr()!=null) {  
		
//		*sz hundreds of pt
//		<w:sz w:val="28" />
			if (lvlPPr.getDefRPr().getSz()!=null) {				
				rPr.setSz( convertFontSize(lvlPPr.getDefRPr().getSz()) );
			}
			
//		kern
//		solidFill
		
//		*latin				
//		<w:rFonts w:asciiTheme="majorHAnsi" w:eastAsiaTheme="majorEastAsia" w:hAnsiTheme="majorHAnsi" w:cstheme="majorBidi" />
			if (lvlPPr.getDefRPr().getLatin()!=null ) {
				RFonts rFonts = factory.createRFonts();
				if (lvlPPr.getDefRPr().getLatin().getTypeface().startsWith("+mj") ) {					
					rFonts.setAscii( fontScheme.getMajorFont().getLatin().getTypeface() );
				} else if (lvlPPr.getDefRPr().getLatin().getTypeface().startsWith("+mn") ) {					
					rFonts.setAscii( fontScheme.getMinorFont().getLatin().getTypeface() );
				}  
				rPr.setRFonts(rFonts);
			}			
		}
	
		return rPr;
	}	
	
	private static HpsMeasure convertFontSize(Integer in) {
		ObjectFactory factory = Context.getWmlObjectFactory();
		HpsMeasure sz = factory.createHpsMeasure();
		int halfPts = Math.round(in/50); 
		sz.setVal( BigInteger.valueOf(halfPts) );
		return sz;
	}
	
	// From Main Presentation Part
	public static List<Style> generateWordStylesFromPresentationPart(CTTextListStyle textStyles, String suffix,  
			FontScheme fontScheme) {
		
		List<Style> styles = new ArrayList<Style>();
		if (textStyles == null) return styles;
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl1PPr(), "Lvl1" + suffix, "Lvl1" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl2PPr(), "Lvl2" + suffix, "Lvl2" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl3PPr(), "Lvl3" + suffix, "Lvl3" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl4PPr(), "Lvl4" + suffix, "Lvl4" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl5PPr(), "Lvl5" + suffix, "Lvl5" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl6PPr(), "Lvl6" + suffix, "Lvl6" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl7PPr(), "Lvl7" + suffix, "Lvl7" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl8PPr(), "Lvl8" + suffix, "Lvl8" + suffix, "DocDefaults", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl9PPr(), "Lvl9" + suffix, "Lvl9" + suffix, "DocDefaults", fontScheme));
		
		return styles;
	}

	
	// From Master
	public static List<Style> generateWordStylesForMaster(CTSlideMasterTextStyles masterTextStyles, int mastern, FontScheme fontScheme) {
		
		List<Style> styles = new ArrayList<Style>();
		if (masterTextStyles == null) return styles;
		// <p:titleStyle>
		styles.addAll(generateLvlNMasterStyle(masterTextStyles.getTitleStyle(), "Master" + mastern + "Title", fontScheme));
		// <p:bodyStyle>
		styles.addAll(generateLvlNMasterStyle(masterTextStyles.getBodyStyle(),  "Master" + mastern + "Body", fontScheme));
		// <p:otherStyle>
		styles.addAll(generateLvlNMasterStyle(masterTextStyles.getOtherStyle(), "Master" + mastern + "Other", fontScheme));
		
		return styles;
	}

	private static List<Style> generateLvlNMasterStyle(CTTextListStyle textStyles, String suffix,  
			FontScheme fontScheme) {
		
		List<Style> styles = new ArrayList<Style>();
		if (textStyles == null) textStyles = new CTTextListStyle();
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl1PPr(), "Lvl1" + suffix, "Lvl1" + suffix, "Lvl1", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl2PPr(), "Lvl2" + suffix, "Lvl2" + suffix, "Lvl2", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl3PPr(), "Lvl3" + suffix, "Lvl3" + suffix, "Lvl3", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl4PPr(), "Lvl4" + suffix, "Lvl4" + suffix, "Lvl4", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl5PPr(), "Lvl5" + suffix, "Lvl5" + suffix, "Lvl5", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl6PPr(), "Lvl6" + suffix, "Lvl6" + suffix, "Lvl6", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl7PPr(), "Lvl7" + suffix, "Lvl7" + suffix, "Lvl7", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl8PPr(), "Lvl8" + suffix, "Lvl8" + suffix, "Lvl8", fontScheme));
		styles.add(TextStyles.convertToWordStyle(textStyles.getLvl9PPr(), "Lvl9" + suffix, "Lvl9" + suffix, "Lvl9", fontScheme));
		
		return styles;
	}
	
//    protected static Style createVirtualStylesForDocDefaults(FontScheme fontScheme) {
//    	
//		ObjectFactory factory = Context.getWmlObjectFactory();
//		Style style = factory.createStyle();
//    	
//    	String ROOT_NAME = "DocDefaults";
//    	
//    	style.setStyleId(ROOT_NAME);
//    	style.setType("paragraph");
//		
//		Name styleName = factory.createStyleName();
//		styleName.setVal(ROOT_NAME);
//		style.setName(styleName);
//		
////		<w:rPr>
//		RPr rPr = factory.createRPr();
//		style.setRPr(rPr);
//
////		<w:rFonts w:asciiTheme="majorHAnsi" w:eastAsiaTheme="majorEastAsia" w:hAnsiTheme="majorHAnsi" w:cstheme="majorBidi" />
//		RFonts rFonts = factory.createRFonts();
//		rFonts.setAscii( fontScheme.getMinorFont().getLatin().getTypeface() );
//		rPr.setRFonts(rFonts);
//	
//		return style;
//    	
//    }	
    
    protected static DocDefaults generateDocDefaults(FontScheme fontScheme) {
    	
		ObjectFactory factory = Context.getWmlObjectFactory();
		DocDefaults docDefaults = factory.createDocDefaults();

		RPrDefault rPrDefault = factory.createDocDefaultsRPrDefault();
		
		docDefaults.setRPrDefault(rPrDefault);
		
//		<w:rPr>
		RPr rPr = factory.createRPr();
		rPrDefault.setRPr(rPr);

//		<w:rFonts w:asciiTheme="majorHAnsi" w:eastAsiaTheme="majorEastAsia" w:hAnsiTheme="majorHAnsi" w:cstheme="majorBidi" />
		RFonts rFonts = factory.createRFonts();
		rFonts.setAscii( fontScheme.getMinorFont().getLatin().getTypeface() );
		rPr.setRFonts(rFonts);
	
		return docDefaults;
    	
    }	
    
    public static DocDefaults generateDocDefaults() {
    	return docDefaults;
    }
    
    private static DocDefaults docDefaults;
    
	public static List<Style> generateStyles(PresentationMLPackage presentationMLPackage) 
		throws InvalidFormatException {
		
		ThemePart tp = (ThemePart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/theme/theme1.xml"));
		FontScheme fontScheme = tp.getFontScheme();
		List<Style> styles = new ArrayList<Style>();
		
		TextStyles.generateDocDefaults(fontScheme);
		
		// presentation.xml
		MainPresentationPart pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/presentation.xml"));
		styles.addAll(
				TextStyles.generateWordStylesFromPresentationPart(
						pp.getJaxbElement().getDefaultTextStyle(),
						"", fontScheme));

		// master
		SlideMasterPart master = (SlideMasterPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/slideMasters/slideMaster1.xml"));
		styles.addAll(
				TextStyles.generateWordStylesForMaster(
						master.getJaxbElement().getTxStyles(), 
						1, fontScheme));
				
		return styles;
		
	}	
    
	public static RPr getWmlRPr(CTTextCharacterProperties in) {
		
		ObjectFactory factory = Context.getWmlObjectFactory();
		RPr rPr = factory.createRPr();

		if (in==null) {
			System.out.println("Was passed null");
			return rPr;
		}
		
//        <a:rPr  i="true" />		
		if (in.isI()!=null && in.isI()) {
			rPr.setI( new BooleanDefaultTrue() );
		}
		
//        <a:rPr  b="true"
		if (in.isB()!=null && in.isB()) {
			rPr.setB( new BooleanDefaultTrue() );
		}
		
//        <a:rPr  u="sng" 
		if (in.getU()!=null) {
			U u = factory.createU(); 
			u.setVal(UnderlineEnumeration.SINGLE);
			rPr.setU(u);
		}
//        <a:rPr  sz="4000" 
		if (in.getSz()!=null) {
			rPr.setSz(
					convertFontSize(in.getSz() ) );
		}
		
		return rPr;

	}
}
