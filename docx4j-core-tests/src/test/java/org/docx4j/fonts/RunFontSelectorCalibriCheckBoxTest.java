package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map.Entry;

import org.docx4j.XmlUtils;
import org.docx4j.dml.Theme;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

public class RunFontSelectorCalibriCheckBoxTest {
	
	protected static Logger log = LoggerFactory.getLogger(RunFontSelector.class); // same logger	
	
	final static String FONT_WORD_2016_USES = "Segoe UI Symbol";
		
	@Test
	public  void testFont() throws Exception {
		
		boolean save = false;
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		// Styles part
		StyleDefinitionsPart sdp = mdp.getStyleDefinitionsPart();
		Styles styles = (Styles)XmlUtils.unmarshalString(stylesXML);	
		sdp.setJaxbElement(styles);

		// Theme part
		ThemePart themePart = new ThemePart();		
		Theme theme = (Theme)XmlUtils.unmarshalString(themeXML);			
		themePart.setJaxbElement(theme);
		mdp.addTargetPart(themePart);
		
//		// Settings 
//		DocumentSettingsPart dsp = new DocumentSettingsPart();
//		dsp.setJaxbElement(createSettings());
//		mdp.addTargetPart(dsp);
		
		
		Document document = (Document)XmlUtils.unmarshalString(documentXML);		
		wordMLPackage.getMainDocumentPart().setJaxbElement(document);		


		Mapper fontMapper = wordMLPackage.getFontMapper();
//		PhysicalFont font = PhysicalFonts.get("noto sans symbols regular"); // Glyph 10065 (0x2751) not available in font Noto Sans Symbols Regular
		PhysicalFont font = PhysicalFonts.get("dejavu sans");
		if (font==null) {
			System.out.println("Missing PhysicalFont.");
		}
		
		//fontMapper.put("Calibri", font);
		fontMapper.put(FONT_WORD_2016_USES, font); 
		// static String[] expectedFont = { "MS Gothic"}; // Word sometime prior to 2016? 
		 String[] expectedFont = { fontMapper.get(FONT_WORD_2016_USES).getName() }; 
		
//		for(Entry<String, PhysicalFont> entry : PhysicalFonts.getPhysicalFonts().entrySet() ) {
//			System.out.println(entry.getKey());
//		}
		
		if (save) {
			wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_RunFontSelectorTests.docx"));
		} 
		
		RunFontSelector rfs = createRunFontSelector(wordMLPackage);
		
		// For each w:p, test w:r/w:t
		for (int i=0; i<document.getContent().size(); i++) {
			
			P p = (P)document.getContent().get(i);
			System.out.println(XmlUtils.marshaltoString(p));
					
			PPr pPr = p.getPPr();
			RPr rPr = ((R)p.getContent().get(0)).getRPr();
			
			Text wmlText = (Text)XmlUtils.unwrap(((R)p.getContent().get(0)).getContent().get(0));
			
			log.debug(wmlText.getValue());
			
			Object result = rfs.fontSelector(pPr, rPr, wmlText);
//			System.out.println(result.getClass().getName());
			
			DocumentFragment df = (DocumentFragment)result;
			
			log.warn(XmlUtils.w3CDomNodeToString(df));
			System.out.println(XmlUtils.w3CDomNodeToString(df));
			
			Element foInline = (Element)df.getFirstChild();
			System.out.println("@font-family='" + foInline.getAttribute("font-family"));
			
			assertEquals(expectedFont[i], foInline.getAttribute("font-family"));
		}
		
	}	
	
	String documentXML = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<w:body>"

                        + "<w:p>"
                            + "<w:pPr>"
                                + "<w:jc w:val=\"center\"/>"
                                + "<w:rPr>" // should make no difference
                                    + "<w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>"
                                +"</w:rPr>"
                            +"</w:pPr>"
                            + "<w:r>"
                                + "<w:rPr>"
                                + "<w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>"
                                +"</w:rPr>"
                                + "<w:t>❑</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                            

        +"</w:body>"
    +"</w:document>";	
	
	static String stylesXML = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
			
            + "<w:docDefaults>"
			   + "<w:rPrDefault>"
			            + "<w:rPr>"
			                + "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" w:eastAsiaTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\"/>"
			                + "<w:sz w:val=\"22\"/>"
			                + "<w:szCs w:val=\"22\"/>"
			                + "<w:lang w:bidi=\"ar-SA\" w:eastAsia=\"en-US\" w:val=\"en-US\"/>"
			            +"</w:rPr>"
			        +"</w:rPrDefault>"
			        + "<w:pPrDefault>"
			            + "<w:pPr>"
			                + "<w:spacing w:after=\"200\" w:line=\"276\" w:lineRule=\"auto\"/>"
			            +"</w:pPr>"
			        +"</w:pPrDefault>"
			    +"</w:docDefaults>"

              + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
                  + "<w:name w:val=\"Normal\"/>"
                  + "<w:qFormat/>"
                  + "<w:pPr>"
                        + "<w:jc w:val=\"both\"/>"
                  +"</w:pPr>"

                  + "<w:rPr>"
                  + "<w:rFonts w:ascii=\"Calibri\" w:cs=\"Calibri\" w:eastAsia=\"Calibri\" w:hAnsi=\"Calibri\"/>"
                  +"</w:rPr>"
            +"</w:style>"

            + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
                  + "<w:name w:val=\"Default Paragraph Font\"/>"
                  + "<w:uiPriority w:val=\"1\"/>"
                  + "<w:semiHidden/>"
                  + "<w:unhideWhenUsed/>"
            +"</w:style>"

      +"</w:styles>";
	
	
	String themeXML = "<a:theme name=\"Office ??\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:thm15=\"http://schemas.microsoft.com/office/thememl/2012/main\">"
            + "<a:themeElements>"
                + "<a:clrScheme name=\"Office\">"
                    + "<a:dk1>"
                        + "<a:sysClr lastClr=\"000000\" val=\"windowText\"/>"
                    +"</a:dk1>"
                    + "<a:lt1>"
                        + "<a:sysClr lastClr=\"FFFFFF\" val=\"window\"/>"
                    +"</a:lt1>"
                    + "<a:dk2>"
                        + "<a:srgbClr val=\"44546A\"/>"
                    +"</a:dk2>"
                    + "<a:lt2>"
                        + "<a:srgbClr val=\"E7E6E6\"/>"
                    +"</a:lt2>"
                    + "<a:accent1>"
                        + "<a:srgbClr val=\"5B9BD5\"/>"
                    +"</a:accent1>"
                    + "<a:accent2>"
                        + "<a:srgbClr val=\"ED7D31\"/>"
                    +"</a:accent2>"
                    + "<a:accent3>"
                        + "<a:srgbClr val=\"A5A5A5\"/>"
                    +"</a:accent3>"
                    + "<a:accent4>"
                        + "<a:srgbClr val=\"FFC000\"/>"
                    +"</a:accent4>"
                    + "<a:accent5>"
                        + "<a:srgbClr val=\"4472C4\"/>"
                    +"</a:accent5>"
                    + "<a:accent6>"
                        + "<a:srgbClr val=\"70AD47\"/>"
                    +"</a:accent6>"
                    + "<a:hlink>"
                        + "<a:srgbClr val=\"0563C1\"/>"
                    +"</a:hlink>"
                    + "<a:folHlink>"
                        + "<a:srgbClr val=\"954F72\"/>"
                    +"</a:folHlink>"
                +"</a:clrScheme>"
                + "<a:fontScheme name=\"Office\">"
                + "<a:majorFont>"
                + "<a:latin typeface=\"Cambria\"/>"
                + "<a:ea typeface=\"\"/>"
                + "<a:cs typeface=\"\"/>"
                + "<a:font script=\"Jpan\" typeface=\"ＭＳ ゴシック\"/>"
                + "<a:font script=\"Hang\" typeface=\"맑은 고딕\"/>"
                + "<a:font script=\"Hans\" typeface=\"宋体\"/>"
                + "<a:font script=\"Hant\" typeface=\"新細明體\"/>"
                + "<a:font script=\"Arab\" typeface=\"Times New Roman\"/>"
                + "<a:font script=\"Hebr\" typeface=\"Times New Roman\"/>"
                + "<a:font script=\"Thai\" typeface=\"Angsana New\"/>"
                + "<a:font script=\"Ethi\" typeface=\"Nyala\"/>"
                + "<a:font script=\"Beng\" typeface=\"Vrinda\"/>"
                + "<a:font script=\"Gujr\" typeface=\"Shruti\"/>"
                + "<a:font script=\"Khmr\" typeface=\"MoolBoran\"/>"
                + "<a:font script=\"Knda\" typeface=\"Tunga\"/>"
                + "<a:font script=\"Guru\" typeface=\"Raavi\"/>"
                + "<a:font script=\"Cans\" typeface=\"Euphemia\"/>"
                + "<a:font script=\"Cher\" typeface=\"Plantagenet Cherokee\"/>"
                + "<a:font script=\"Yiii\" typeface=\"Microsoft Yi Baiti\"/>"
                + "<a:font script=\"Tibt\" typeface=\"Microsoft Himalaya\"/>"
                + "<a:font script=\"Thaa\" typeface=\"MV Boli\"/>"
                + "<a:font script=\"Deva\" typeface=\"Mangal\"/>"
                + "<a:font script=\"Telu\" typeface=\"Gautami\"/>"
                + "<a:font script=\"Taml\" typeface=\"Latha\"/>"
                + "<a:font script=\"Syrc\" typeface=\"Estrangelo Edessa\"/>"
                + "<a:font script=\"Orya\" typeface=\"Kalinga\"/>"
                + "<a:font script=\"Mlym\" typeface=\"Kartika\"/>"
                + "<a:font script=\"Laoo\" typeface=\"DokChampa\"/>"
                + "<a:font script=\"Sinh\" typeface=\"Iskoola Pota\"/>"
                + "<a:font script=\"Mong\" typeface=\"Mongolian Baiti\"/>"
                + "<a:font script=\"Viet\" typeface=\"Times New Roman\"/>"
                + "<a:font script=\"Uigh\" typeface=\"Microsoft Uighur\"/>"
            +"</a:majorFont>"
            + "<a:minorFont>"
                + "<a:latin typeface=\"Calibri\"/>"
                + "<a:ea typeface=\"\"/>"
                + "<a:cs typeface=\"\"/>"
                + "<a:font script=\"Jpan\" typeface=\"ＭＳ 明朝\"/>"
                + "<a:font script=\"Hang\" typeface=\"맑은 고딕\"/>"
                + "<a:font script=\"Hans\" typeface=\"宋体\"/>"
                + "<a:font script=\"Hant\" typeface=\"新細明體\"/>"
                + "<a:font script=\"Arab\" typeface=\"Arial\"/>"
                + "<a:font script=\"Hebr\" typeface=\"Arial\"/>"
                + "<a:font script=\"Thai\" typeface=\"Cordia New\"/>"
                + "<a:font script=\"Ethi\" typeface=\"Nyala\"/>"
                + "<a:font script=\"Beng\" typeface=\"Vrinda\"/>"
                + "<a:font script=\"Gujr\" typeface=\"Shruti\"/>"
                + "<a:font script=\"Khmr\" typeface=\"DaunPenh\"/>"
                + "<a:font script=\"Knda\" typeface=\"Tunga\"/>"
                + "<a:font script=\"Guru\" typeface=\"Raavi\"/>"
                + "<a:font script=\"Cans\" typeface=\"Euphemia\"/>"
                + "<a:font script=\"Cher\" typeface=\"Plantagenet Cherokee\"/>"
                + "<a:font script=\"Yiii\" typeface=\"Microsoft Yi Baiti\"/>"
                + "<a:font script=\"Tibt\" typeface=\"Microsoft Himalaya\"/>"
                + "<a:font script=\"Thaa\" typeface=\"MV Boli\"/>"
                + "<a:font script=\"Deva\" typeface=\"Mangal\"/>"
                + "<a:font script=\"Telu\" typeface=\"Gautami\"/>"
                + "<a:font script=\"Taml\" typeface=\"Latha\"/>"
                + "<a:font script=\"Syrc\" typeface=\"Estrangelo Edessa\"/>"
                + "<a:font script=\"Orya\" typeface=\"Kalinga\"/>"
                + "<a:font script=\"Mlym\" typeface=\"Kartika\"/>"
                + "<a:font script=\"Laoo\" typeface=\"DokChampa\"/>"
                + "<a:font script=\"Sinh\" typeface=\"Iskoola Pota\"/>"
                + "<a:font script=\"Mong\" typeface=\"Mongolian Baiti\"/>"
                + "<a:font script=\"Viet\" typeface=\"Arial\"/>"
                + "<a:font script=\"Uigh\" typeface=\"Microsoft Uighur\"/>"
            +"</a:minorFont>"
                +"</a:fontScheme>"
                + "<a:fmtScheme name=\"Office\">"
                    + "<a:fillStyleLst>"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"phClr\"/>"
                        +"</a:solidFill>"
                        + "<a:gradFill rotWithShape=\"1\">"
                            + "<a:gsLst>"
                                + "<a:gs pos=\"0\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:lumMod val=\"110000\"/>"
                                        + "<a:satMod val=\"105000\"/>"
                                        + "<a:tint val=\"67000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                                + "<a:gs pos=\"50000\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:lumMod val=\"105000\"/>"
                                        + "<a:satMod val=\"103000\"/>"
                                        + "<a:tint val=\"73000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                                + "<a:gs pos=\"100000\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:lumMod val=\"105000\"/>"
                                        + "<a:satMod val=\"109000\"/>"
                                        + "<a:tint val=\"81000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                            +"</a:gsLst>"
                            + "<a:lin ang=\"5400000\" scaled=\"0\"/>"
                        +"</a:gradFill>"
                        + "<a:gradFill rotWithShape=\"1\">"
                            + "<a:gsLst>"
                                + "<a:gs pos=\"0\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:satMod val=\"103000\"/>"
                                        + "<a:lumMod val=\"102000\"/>"
                                        + "<a:tint val=\"94000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                                + "<a:gs pos=\"50000\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:satMod val=\"110000\"/>"
                                        + "<a:lumMod val=\"100000\"/>"
                                        + "<a:shade val=\"100000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                                + "<a:gs pos=\"100000\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:lumMod val=\"99000\"/>"
                                        + "<a:satMod val=\"120000\"/>"
                                        + "<a:shade val=\"78000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                            +"</a:gsLst>"
                            + "<a:lin ang=\"5400000\" scaled=\"0\"/>"
                        +"</a:gradFill>"
                    +"</a:fillStyleLst>"
                    + "<a:lnStyleLst>"
                        + "<a:ln algn=\"ctr\" cap=\"flat\" cmpd=\"sng\" w=\"6350\">"
                            + "<a:solidFill>"
                                + "<a:schemeClr val=\"phClr\"/>"
                            +"</a:solidFill>"
                            + "<a:prstDash val=\"solid\"/>"
                            + "<a:miter lim=\"800000\"/>"
                        +"</a:ln>"
                        + "<a:ln algn=\"ctr\" cap=\"flat\" cmpd=\"sng\" w=\"12700\">"
                            + "<a:solidFill>"
                                + "<a:schemeClr val=\"phClr\"/>"
                            +"</a:solidFill>"
                            + "<a:prstDash val=\"solid\"/>"
                            + "<a:miter lim=\"800000\"/>"
                        +"</a:ln>"
                        + "<a:ln algn=\"ctr\" cap=\"flat\" cmpd=\"sng\" w=\"19050\">"
                            + "<a:solidFill>"
                                + "<a:schemeClr val=\"phClr\"/>"
                            +"</a:solidFill>"
                            + "<a:prstDash val=\"solid\"/>"
                            + "<a:miter lim=\"800000\"/>"
                        +"</a:ln>"
                    +"</a:lnStyleLst>"
                    + "<a:effectStyleLst>"
                        + "<a:effectStyle>"
                            + "<a:effectLst/>"
                        +"</a:effectStyle>"
                        + "<a:effectStyle>"
                            + "<a:effectLst/>"
                        +"</a:effectStyle>"
                        + "<a:effectStyle>"
                            + "<a:effectLst>"
                                + "<a:outerShdw algn=\"ctr\" blurRad=\"57150\" dir=\"5400000\" dist=\"19050\" rotWithShape=\"0\">"
                                    + "<a:srgbClr val=\"000000\">"
                                        + "<a:alpha val=\"63000\"/>"
                                    +"</a:srgbClr>"
                                +"</a:outerShdw>"
                            +"</a:effectLst>"
                        +"</a:effectStyle>"
                    +"</a:effectStyleLst>"
                    + "<a:bgFillStyleLst>"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"phClr\"/>"
                        +"</a:solidFill>"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"phClr\">"
                                + "<a:tint val=\"95000\"/>"
                                + "<a:satMod val=\"170000\"/>"
                            +"</a:schemeClr>"
                        +"</a:solidFill>"
                        + "<a:gradFill rotWithShape=\"1\">"
                            + "<a:gsLst>"
                                + "<a:gs pos=\"0\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:tint val=\"93000\"/>"
                                        + "<a:satMod val=\"150000\"/>"
                                        + "<a:shade val=\"98000\"/>"
                                        + "<a:lumMod val=\"102000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                                + "<a:gs pos=\"50000\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:tint val=\"98000\"/>"
                                        + "<a:satMod val=\"130000\"/>"
                                        + "<a:shade val=\"90000\"/>"
                                        + "<a:lumMod val=\"103000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                                + "<a:gs pos=\"100000\">"
                                    + "<a:schemeClr val=\"phClr\">"
                                        + "<a:shade val=\"63000\"/>"
                                        + "<a:satMod val=\"120000\"/>"
                                    +"</a:schemeClr>"
                                +"</a:gs>"
                            +"</a:gsLst>"
                            + "<a:lin ang=\"5400000\" scaled=\"0\"/>"
                        +"</a:gradFill>"
                    +"</a:bgFillStyleLst>"
                +"</a:fmtScheme>"
            +"</a:themeElements>"
            + "<a:objectDefaults/>"
            + "<a:extraClrSchemeLst/>"
            + "<a:extLst>"
                + "<a:ext uri=\"{05A4C25C-085E-4340-85A3-A5531E510DB2}\">"
                    + "<thm15:themeFamily id=\"{62F939B6-93AF-4DB8-9C6B-D6C7DFDC589F}\" name=\"Office Theme\" vid=\"{4A3C46E8-61CC-4603-A589-7422A47A8E4A}\"/>"
                +"</a:ext>"
            +"</a:extLst>"
        +"</a:theme>";

	
	// copied from FOConversionContext
	private static RunFontSelector createRunFontSelector(WordprocessingMLPackage wmlPackage) {
		
		return new RunFontSelector(wmlPackage, 
				
			new RunFontCharacterVisitor() {
			
	    		DocumentFragment df;			
				StringBuilder sb = new StringBuilder(1024); 
				Element span;
				
				String lastFont;
				String fallbackFontName; 
				
				private org.w3c.dom.Document document;
				@Override
				public void setDocument(org.w3c.dom.Document document) {
					this.document = document;
					 df = document.createDocumentFragment();
				}
				
				private boolean spanReusable = true;
				public boolean isReusable() {
					return spanReusable;
				}
	
				public void addCharacterToCurrent(char c) {
			    	sb.append(c);		
				}
				
				@Override
				public void addCodePointToCurrent(int cp) {
					sb.append(
							new String(Character.toChars(cp)));
				}				
	
				public void finishPrevious() {
					
			    	if (sb.length()>0) {
			    		if (span==null) { // init
			    			span = runFontSelector.createElement(document);	
			    			// so that spaces have correct font set
			    			if (lastFont!=null) {
								runFontSelector.setAttribute(span, lastFont); 			    				
			    			}
			    		}
				    	df.appendChild(span);   
				    	span.setTextContent(sb.toString()); 
//				    	log.info("span: " + sb.toString()); 
				    	sb.setLength(0);
			    	}		
				}
	
				public void createNew() {
					span = runFontSelector.createElement(document);			
				}
	
				public void setMustCreateNewFlag(boolean val) {
					spanReusable = !val;
				}
	
				public void fontAction(String fontname) {
					
//					System.out.println(fontname);
//    				Throwable t = new Throwable();
//    				t.printStackTrace();
					
					
					if (fontname==null) {
						runFontSelector.setAttribute(span, fallbackFontName); 						
					} else {
						runFontSelector.setAttribute(span, fontname); 
						lastFont = fontname;
					}
				}

				@Override
				public Object getResult() {
					span=null; // ready for next time
					return df;
				}

				private RunFontSelector runFontSelector;
				@Override
				public void setRunFontSelector(RunFontSelector runFontSelector) {
					this.runFontSelector = runFontSelector;
				}

				@Override
				public void setFallbackFont(String fontname) {
					fallbackFontName = fontname;
					
				}

				
			}, RunFontActionType.XSL_FO);

	}	
}
