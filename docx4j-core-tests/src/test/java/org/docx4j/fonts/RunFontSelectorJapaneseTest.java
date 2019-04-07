package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.docx4j.XmlUtils;
import org.docx4j.dml.Theme;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.CTCharacterSpacing;
import org.docx4j.wml.CTKinsoku;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTSettings;
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

public class RunFontSelectorJapaneseTest {
	
	protected static Logger log = LoggerFactory.getLogger(RunFontSelector.class); // same logger	
	
	static String[] expectedFont = { "MS Gothic", "MS Mincho"};
	static String[] win10Base = { "MS Gothic", "Century"};
	
	@Test
	public  void testFont() throws Exception {
		
		if (System.getProperty("os.name")!=null
				&& System.getProperty("os.name").toLowerCase().startsWith("Windows")) {
			// OK, assume fonts present
		} else {
			log.info("Skipping RunFontSelector test, since required fonts likely missing (non-Windows OS)");
			return;
		}		
		
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
		
		// Settings 
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		dsp.setJaxbElement(createSettings());
		mdp.addTargetPart(dsp);
		
		
		Document document = (Document)XmlUtils.unmarshalString(documentXML);		
		wordMLPackage.getMainDocumentPart().setJaxbElement(document);		


		if (save) {
			wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_RunFontSelectorTests.docx"));
		} 
		
		RunFontSelector rfs = createRunFontSelector(wordMLPackage);
		
		// For each w:p, test w:r/w:t
		for (int i=0; i<document.getContent().size(); i++) {
			
			P p = (P)document.getContent().get(i);
			
			PPr pPr = p.getPPr();
			RPr rPr = ((R)p.getContent().get(0)).getRPr();
			
			Text wmlText = (Text)XmlUtils.unwrap(((R)p.getContent().get(0)).getContent().get(0));
			
			log.debug(wmlText.getValue());
			
			Object result = rfs.fontSelector(pPr, rPr, wmlText);
//			System.out.println(result.getClass().getName());
			
			DocumentFragment df = (DocumentFragment)result;
			
			log.debug(XmlUtils.w3CDomNodeToString(df));
			
			Element foInline = (Element)df.getFirstChild();
			System.out.println(i + ": " + foInline.getAttribute("font-family"));
			
			if (PhysicalFonts.get("MS Mincho")==null
					&& System.getProperty("os.name").startsWith("Windows")
					&& System.getProperty("os.version").equals("6.2")) {
				/* On Windows 10, MS Mincho, the expected result, is not installed by default. 
				 * See further http://answers.microsoft.com/en-us/windows/forum/windows_10-start/some-fonts-are-missing-after-upgrade/95839dfa-0df2-4bc0-875a-fd6b57e61fe4?auth=1 
				 * */ 
				
//				System.out.println(System.getProperty("os.name"));
//				System.out.println(System.getProperty("os.version"));
				
				assertEquals(win10Base[i], foInline.getAttribute("font-family"));
			} else {
				assertEquals(expectedFont[i], foInline.getAttribute("font-family"));				
			}
		}
		
	}	
	
	String documentXML ="<w:document mc:Ignorable=\"w14 wp14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<w:body>"

            
            + "<w:p>"
            + "<w:pPr>"
		            + "<w:pStyle w:val=\"TitleinJapanese\"/>" // MS Gothic
		        +"</w:pPr>"            
                + "<w:r>"
                    + "<w:rPr>"

                        + "<w:rFonts w:hint=\"eastAsia\"/>"
                    +"</w:rPr>"
                    + "<w:t>シック</w:t>"
                +"</w:r>"
            +"</w:p>"
                
            + "<w:p>"
                + "<w:r>"
                    + "<w:rPr>"
                        + "<w:rFonts w:hint=\"eastAsia\"/>"
                    +"</w:rPr>"
                    + "<w:t>明朝</w:t>"
                +"</w:r>"
            +"</w:p>"
                

        +"</w:body>"
    +"</w:document>";	
	
	public CTSettings createSettings() {

		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

		CTSettings settings = wmlObjectFactory.createCTSettings(); 
//		JAXBElement<org.docx4j.wml.CTSettings> settingsWrapped = wmlObjectFactory.createSettings(settings); 

		// Create object for characterSpacingControl
	    CTCharacterSpacing characterspacing = wmlObjectFactory.createCTCharacterSpacing(); 
	    settings.setCharacterSpacingControl(characterspacing); 
	        characterspacing.setVal(org.docx4j.wml.STCharacterSpacing.COMPRESS_PUNCTUATION);
	    // Create object for noLineBreaksAfter
	    CTKinsoku kinsoku = wmlObjectFactory.createCTKinsoku(); 
	    settings.setNoLineBreaksAfter(kinsoku); 
	        kinsoku.setVal( "$([\\{£¥‘“〈《「『【〔＄（［｛｢￡￥"); 
	        kinsoku.setLang( "ja-JP"); 
	    // Create object for noLineBreaksBefore
	    CTKinsoku kinsoku2 = wmlObjectFactory.createCTKinsoku(); 
	    settings.setNoLineBreaksBefore(kinsoku2); 
	        kinsoku2.setVal( " !%),-.:;?]}¢°’”‰′″℃、。々〉》」』】〕゛゜ゝゞ・ヽヾ！％），．：；？］｝｡｣､･ﾞﾟ￠"); 
	        kinsoku2.setLang( "ja-JP"); 
	            
	    // Create object for themeFontLang
	    CTLanguage language = wmlObjectFactory.createCTLanguage(); 
	    settings.setThemeFontLang(language); 
	        language.setVal( "en-US"); 
	        language.setBidi( "km-KH"); 
	        language.setEastAsia( "ja-JP"); 


		return settings;
		}	
	
	static String stylesXML = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
			
            + "<w:docDefaults>"
			      + "<w:rPrDefault>"
			            + "<w:rPr>"
			                + "<w:rFonts w:ascii=\"Century\" w:cs=\"Times New Roman\" w:eastAsia=\"MS Mincho\" w:hAnsi=\"Century\"/>"
			                + "<w:lang w:bidi=\"ar-SA\" w:eastAsia=\"ja-JP\" w:val=\"en-US\"/>"
			            +"</w:rPr>"
			      +"</w:rPrDefault>"
                  + "<w:pPrDefault/>"
            +"</w:docDefaults>"

              + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
              + "<w:name w:val=\"Normal\"/>"
              + "<w:rsid w:val=\"00117BB2\"/>"
              + "<w:pPr>"
                  + "<w:widowControl w:val=\"0\"/>"
                  + "<w:jc w:val=\"both\"/>"
              +"</w:pPr>"
              + "<w:rPr>"
                  + "<w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
                  + "<w:kern w:val=\"2\"/>"
                  + "<w:sz w:val=\"17\"/>"
                  + "<w:szCs w:val=\"22\"/>"
              +"</w:rPr>"
            +"</w:style>"

            + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
                  + "<w:name w:val=\"Default Paragraph Font\"/>"
                  + "<w:uiPriority w:val=\"1\"/>"
                  + "<w:semiHidden/>"
                  + "<w:unhideWhenUsed/>"
            +"</w:style>"
                  
			+"<w:style w:customStyle=\"1\" w:styleId=\"TitleinJapanese\" w:type=\"paragraph\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			+ "<w:name w:val=\"Title in Japanese\"/>"
			+ "<w:basedOn w:val=\"Normal\"/>"
			+ "<w:link w:val=\"TitleinJapanese0\"/>"
			+ "<w:qFormat/>"
			+ "<w:rsid w:val=\"001328A1\"/>"
			+ "<w:pPr>"
			    + "<w:jc w:val=\"center\"/>"
			+"</w:pPr>"
			+ "<w:rPr>"
			    + "<w:rFonts w:ascii=\"MS Gothic\" w:eastAsia=\"MS Gothic\" w:hAnsi=\"MS Gothic\"/>"
			    + "<w:sz w:val=\"20\"/>"
			    + "<w:szCs w:val=\"20\"/>"
			+"</w:rPr>"
			+"</w:style>"            

      +"</w:styles>";
	
	
	String themeXML = "<a:theme name=\"Office ​​テーマ\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
            + "<a:themeElements>"
            + "<a:clrScheme name=\"Office\">"
                + "<a:dk1>"
                    + "<a:sysClr lastClr=\"000000\" val=\"windowText\"/>"
                +"</a:dk1>"
                + "<a:lt1>"
                    + "<a:sysClr lastClr=\"FFFFFF\" val=\"window\"/>"
                +"</a:lt1>"
                + "<a:dk2>"
                    + "<a:srgbClr val=\"1F497D\"/>"
                +"</a:dk2>"
                + "<a:lt2>"
                    + "<a:srgbClr val=\"EEECE1\"/>"
                +"</a:lt2>"
                + "<a:accent1>"
                    + "<a:srgbClr val=\"4F81BD\"/>"
                +"</a:accent1>"
                + "<a:accent2>"
                    + "<a:srgbClr val=\"C0504D\"/>"
                +"</a:accent2>"
                + "<a:accent3>"
                    + "<a:srgbClr val=\"9BBB59\"/>"
                +"</a:accent3>"
                + "<a:accent4>"
                    + "<a:srgbClr val=\"8064A2\"/>"
                +"</a:accent4>"
                + "<a:accent5>"
                    + "<a:srgbClr val=\"4BACC6\"/>"
                +"</a:accent5>"
                + "<a:accent6>"
                    + "<a:srgbClr val=\"F79646\"/>"
                +"</a:accent6>"
                + "<a:hlink>"
                    + "<a:srgbClr val=\"0000FF\"/>"
                +"</a:hlink>"
                + "<a:folHlink>"
                    + "<a:srgbClr val=\"800080\"/>"
                +"</a:folHlink>"
            +"</a:clrScheme>"
            + "<a:fontScheme name=\"Office\">"
                + "<a:majorFont>"
                    + "<a:latin typeface=\"Arial\"/>"
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
                    + "<a:font script=\"Geor\" typeface=\"Sylfaen\"/>"
                +"</a:majorFont>"
                + "<a:minorFont>"
                    + "<a:latin typeface=\"Century\"/>"
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
                    + "<a:font script=\"Geor\" typeface=\"Sylfaen\"/>"
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
                                    + "<a:tint val=\"50000\"/>"
                                    + "<a:satMod val=\"300000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                            + "<a:gs pos=\"35000\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:tint val=\"37000\"/>"
                                    + "<a:satMod val=\"300000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                            + "<a:gs pos=\"100000\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:tint val=\"15000\"/>"
                                    + "<a:satMod val=\"350000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                        +"</a:gsLst>"
                        + "<a:lin ang=\"16200000\" scaled=\"1\"/>"
                    +"</a:gradFill>"
                    + "<a:gradFill rotWithShape=\"1\">"
                        + "<a:gsLst>"
                            + "<a:gs pos=\"0\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:shade val=\"51000\"/>"
                                    + "<a:satMod val=\"130000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                            + "<a:gs pos=\"80000\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:shade val=\"93000\"/>"
                                    + "<a:satMod val=\"130000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                            + "<a:gs pos=\"100000\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:shade val=\"94000\"/>"
                                    + "<a:satMod val=\"135000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                        +"</a:gsLst>"
                        + "<a:lin ang=\"16200000\" scaled=\"0\"/>"
                    +"</a:gradFill>"
                +"</a:fillStyleLst>"
                + "<a:lnStyleLst>"
                    + "<a:ln algn=\"ctr\" cap=\"flat\" cmpd=\"sng\" w=\"9525\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"phClr\">"
                                + "<a:shade val=\"95000\"/>"
                                + "<a:satMod val=\"105000\"/>"
                            +"</a:schemeClr>"
                        +"</a:solidFill>"
                        + "<a:prstDash val=\"solid\"/>"
                    +"</a:ln>"
                    + "<a:ln algn=\"ctr\" cap=\"flat\" cmpd=\"sng\" w=\"25400\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"phClr\"/>"
                        +"</a:solidFill>"
                        + "<a:prstDash val=\"solid\"/>"
                    +"</a:ln>"
                    + "<a:ln algn=\"ctr\" cap=\"flat\" cmpd=\"sng\" w=\"38100\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"phClr\"/>"
                        +"</a:solidFill>"
                        + "<a:prstDash val=\"solid\"/>"
                    +"</a:ln>"
                +"</a:lnStyleLst>"
                + "<a:effectStyleLst>"
                    + "<a:effectStyle>"
                        + "<a:effectLst>"
                            + "<a:outerShdw blurRad=\"40000\" dir=\"5400000\" dist=\"20000\" rotWithShape=\"0\">"
                                + "<a:srgbClr val=\"000000\">"
                                    + "<a:alpha val=\"38000\"/>"
                                +"</a:srgbClr>"
                            +"</a:outerShdw>"
                        +"</a:effectLst>"
                    +"</a:effectStyle>"
                    + "<a:effectStyle>"
                        + "<a:effectLst>"
                            + "<a:outerShdw blurRad=\"40000\" dir=\"5400000\" dist=\"23000\" rotWithShape=\"0\">"
                                + "<a:srgbClr val=\"000000\">"
                                    + "<a:alpha val=\"35000\"/>"
                                +"</a:srgbClr>"
                            +"</a:outerShdw>"
                        +"</a:effectLst>"
                    +"</a:effectStyle>"
                    + "<a:effectStyle>"
                        + "<a:effectLst>"
                            + "<a:outerShdw blurRad=\"40000\" dir=\"5400000\" dist=\"23000\" rotWithShape=\"0\">"
                                + "<a:srgbClr val=\"000000\">"
                                    + "<a:alpha val=\"35000\"/>"
                                +"</a:srgbClr>"
                            +"</a:outerShdw>"
                        +"</a:effectLst>"
                        + "<a:scene3d>"
                            + "<a:camera prst=\"orthographicFront\">"
                                + "<a:rot lat=\"0\" lon=\"0\" rev=\"0\"/>"
                            +"</a:camera>"
                            + "<a:lightRig dir=\"t\" rig=\"threePt\">"
                                + "<a:rot lat=\"0\" lon=\"0\" rev=\"1200000\"/>"
                            +"</a:lightRig>"
                        +"</a:scene3d>"
                        + "<a:sp3d>"
                            + "<a:bevelT h=\"25400\" w=\"63500\"/>"
                        +"</a:sp3d>"
                    +"</a:effectStyle>"
                +"</a:effectStyleLst>"
                + "<a:bgFillStyleLst>"
                    + "<a:solidFill>"
                        + "<a:schemeClr val=\"phClr\"/>"
                    +"</a:solidFill>"
                    + "<a:gradFill rotWithShape=\"1\">"
                        + "<a:gsLst>"
                            + "<a:gs pos=\"0\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:tint val=\"40000\"/>"
                                    + "<a:satMod val=\"350000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                            + "<a:gs pos=\"40000\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:tint val=\"45000\"/>"
                                    + "<a:shade val=\"99000\"/>"
                                    + "<a:satMod val=\"350000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                            + "<a:gs pos=\"100000\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:shade val=\"20000\"/>"
                                    + "<a:satMod val=\"255000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                        +"</a:gsLst>"
                        + "<a:path path=\"circle\">"
                            + "<a:fillToRect b=\"180000\" l=\"50000\" r=\"50000\" t=\"-80000\"/>"
                        +"</a:path>"
                    +"</a:gradFill>"
                    + "<a:gradFill rotWithShape=\"1\">"
                        + "<a:gsLst>"
                            + "<a:gs pos=\"0\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:tint val=\"80000\"/>"
                                    + "<a:satMod val=\"300000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                            + "<a:gs pos=\"100000\">"
                                + "<a:schemeClr val=\"phClr\">"
                                    + "<a:shade val=\"30000\"/>"
                                    + "<a:satMod val=\"200000\"/>"
                                +"</a:schemeClr>"
                            +"</a:gs>"
                        +"</a:gsLst>"
                        + "<a:path path=\"circle\">"
                            + "<a:fillToRect b=\"50000\" l=\"50000\" r=\"50000\" t=\"50000\"/>"
                        +"</a:path>"
                    +"</a:gradFill>"
                +"</a:bgFillStyleLst>"
            +"</a:fmtScheme>"
        +"</a:themeElements>"
        + "<a:objectDefaults>"
            + "<a:txDef>"
                + "<a:spPr bwMode=\"auto\">"
                    + "<a:solidFill>"
                        + "<a:srgbClr val=\"FFFFFF\"/>"
                    +"</a:solidFill>"
                    + "<a:ln w=\"9525\">"
                        + "<a:solidFill>"
                            + "<a:srgbClr val=\"FFFFFF\"/>"
                        +"</a:solidFill>"
                        + "<a:miter lim=\"800000\"/>"
                        + "<a:headEnd/>"
                        + "<a:tailEnd/>"
                    +"</a:ln>"
                +"</a:spPr>"
                + "<a:bodyPr anchor=\"t\" anchorCtr=\"0\" bIns=\"45720\" lIns=\"91440\" rIns=\"91440\" rot=\"0\" tIns=\"45720\" upright=\"1\" vert=\"horz\" wrap=\"square\">"
                    + "<a:noAutofit/>"
                +"</a:bodyPr>"
                + "<a:lstStyle/>"
            +"</a:txDef>"
        +"</a:objectDefaults>"
        + "<a:extraClrSchemeLst/>"
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
