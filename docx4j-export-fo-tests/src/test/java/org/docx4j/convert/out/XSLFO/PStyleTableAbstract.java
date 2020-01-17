package org.docx4j.convert.out.XSLFO;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * w:compatSetting[w:name="overrideTableStyleFontSizeAndJustification"]
 * is defined in [MS-DOCX] 
 * 
 * If this value is true, then the style hierarchy of the document is evaluated as specified 
 * in [ISO/IEC29500-1:2011] section 17.7.2.

	If this value is false, which is the default, then the following additional rules apply:
	
	If the default paragraph style (as specified in [ISO/IEC29500-1:2011] section 17.7.4.17) 
	specifies a font size of 11pt or 12pt, then that setting will not override the font size 
	specified by the table style for paragraphs in tables.
	
		// That's wrong; this additional rule only applies if the font size is 12pt (not 11pt!).
		// Tested in Word 2010 
	
	If the default paragraph style (as specified in [ISO/IEC29500-1:2011] section 17.7.4.17) 
	specifies a justification of left, then that setting will not override the justification 
	specified by the table style for paragraphs in tables.
	  
 * The philosophy seems to be that inside a table cell, Normal didn't apply.
 * 
 * NB: there are fairly comprehensive test cases in src/test/java
 * for the behaviour with font size (where in each case the expected
 * result is set on the basis of what Word does). There aren't any at the moment for
 * justification (which is assumed to follow the same logic we have here).
 * 
 * Where Normal is basedOn our DocDefaults style, Word *does* override the table style!
 * We'll ignore that for now, because the next version of docx4j (v3.3) 
 * stops creating a DocDefaults style. 
 */
public abstract class PStyleTableAbstract {
	
	protected static Logger log = LoggerFactory.getLogger(PStyleTableAbstract.class);	
	
	
	protected static boolean OVERRIDE;
	protected static int EXPECTED_RESULT;
	
//	protected static String STYLE_NAME = "Normal-TableGrid-BR";
	protected String getStyleName() {
		   return "Normal-TableGrid-BR";
	}
	
	static String styles_inRPrDefault;
	static String styles_inNormal;
	static String styles_inDefaultParagraphFont;
	static String styles_in_basedOn_Normal;
	static String styles_basedOn_Normal;
	static String styles_no_font_sz;

	static String table_styles;
	static String mdpXml_tblStyle;
	static String mdpXml_direct_12pt;	
	
	protected WordprocessingMLPackage test(String documentXml, String styleXml) throws Exception {
		return test( documentXml,  styleXml,  EXPECTED_RESULT);
	}
	
	protected WordprocessingMLPackage test(String documentXml, String styleXml,  int expectedResult) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().setContents(
				(Document)XmlUtils.unmarshalString(documentXml) );
		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().setContents(
				(Styles)XmlUtils.unmarshalString(styleXml) );
		
		setSetting(wordMLPackage, OVERRIDE); 
		if (OVERRIDE) {
			log.info("table style should get overridden by Normal");
		} else {
			log.info("table style should NOT get overridden by Normal");			
		}

		/* Where Normal is basedOn our DocDefaults style,  
		 * Word *does* override the table style! 
		 * 
		 * It doesn't if there is just a normal DocDefaults element
		 * which sets the font size.  And that is what we should be testing here.
		 * 
		 * So save the docx
		 * 
		 * Note that createVirtualStylesForDocDefaults() puts 10pt there, if nothing is specified,
		 * so we save the docx here before that method has run.
		 * 
		 * TODO 2016 01 18 revisit this, since createVirtualStylesForDocDefaults has gone now
		 * (its only in StyleTree)
		 */
		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
//		this.saveDocx(wordMLPackage, null);
		
		ParagraphStylesInTableFix.process(wordMLPackage);
				
//		// Now remove the style, and save the docx, to check in Word
//		Style s = getStyle(wordMLPackage, STYLE_NAME);
//		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle().remove(s);
//		
//		getStyle(wordMLPackage, STYLE_NAME).setBasedOn(null);
//		
//		this.saveDocx(wordMLPackage, null);
		
		Style s = getStyle(wordMLPackage, getStyleName());

		if (s==null) {
			log.warn("missing style " + getStyleName());
			Assert.fail("missing style " + getStyleName());
		} else {		
			assertSz(s, expectedResult);
		}
		
		return wordMLPackage;
		
	}
	
	protected void assertSz(Style s, int expectedResult) {
		
		if (s.getRPr()!=null
				&& s.getRPr().getSz()!=null
				&& s.getRPr().getSz().getVal()!=null) {
			int actualResult = s.getRPr().getSz().getVal().intValue();
			Assert.assertTrue(actualResult==expectedResult);
		} else if (s.getRPr()!=null
				&& s.getRPr().getSz()==null) {
			log.warn("null Sz: " + XmlUtils.marshaltoString(s));
			Assert.fail("null Sz");
		}
		
	}

	protected void assertSzNull(Style s) {
		
		if (s.getRPr()==null) {
			
		} else if (s.getRPr()!=null) {
			
			Assert.assertNull(s.getRPr().getSz());
		}
		
	}
	
	
	protected void setSetting(WordprocessingMLPackage wmlPackage, boolean val) throws Docx4JException {
		
			DocumentSettingsPart dsp = wmlPackage.getMainDocumentPart().getDocumentSettingsPart();
			if (dsp==null) {
				dsp = new DocumentSettingsPart();
				wmlPackage.getMainDocumentPart().addTargetPart(dsp);
				
				dsp.setContents( Context.getWmlObjectFactory().createCTSettings() );
			} 

			if (val) {
				dsp.setWordCompatSetting("overrideTableStyleFontSizeAndJustification", "1");
			} else {
				dsp.setWordCompatSetting("overrideTableStyleFontSizeAndJustification", "0");				
			}
	}
	
	protected Style getStyle(WordprocessingMLPackage wordMLPackage, String stylename) throws Docx4JException {
		
		for (Style s : wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle()) {
			if (stylename.equals(s.getStyleId())) {
				return s;
			}
		}
		return null;
	}	
	
	void saveDocx(WordprocessingMLPackage wordMLPackage, String pStyle) throws Docx4JException, JAXBException {
		
		PStyle ps = null;
		if (pStyle!=null) {
			ps = Context.getWmlObjectFactory().createPPrBasePStyle();
			ps.setVal(pStyle);
		}
				
		List<Object> xpathResults = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath("//w:p", true);
		((P)xpathResults.get(0)).getPPr().setPStyle(ps);
		
		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
	}
	
	

	static void initOtherXml() {
		initOtherXml(24);
	}
	
	static void initOtherXml(int fontSizeHps) {

	styles_inRPrDefault = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
	        + "<w:docDefaults>"
	              + "<w:rPrDefault>"
	                    + "<w:rPr>"
	                          + "<w:sz w:val=\"" + fontSizeHps + "\"/>"
	                          + "<w:szCs w:val=\"" + fontSizeHps + "\"/>"
	                    + "</w:rPr>"
	              + "</w:rPrDefault>"
	              + "<w:pPrDefault>"
	                    + "<w:pPr>"
	                          + "<w:spacing w:after=\"120\"/>"
	                    + "</w:pPr>"
	              + "</w:pPrDefault>"
	        + "</w:docDefaults>"
	        + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"Normal\"/>"
	              + "<w:qFormat/>"
	              + "<w:rPr>"
	                    + "<w:rFonts w:ascii=\"Times New Roman\"/>"
	              + "</w:rPr>"
	        + "</w:style>"
	        + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
	              + "<w:name w:val=\"Default Paragraph Font\"/>"
	              + "<w:uiPriority w:val=\"1\"/>"
	              + "<w:semiHidden/>"
	              + "<w:unhideWhenUsed/>"
	        + "</w:style>"
	        + table_styles
	  + "</w:styles>";

	styles_inNormal = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
	        + "<w:docDefaults>"
	              + "<w:rPrDefault>"
	                    + "<w:rPr>"
	                    	+ "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
	                    + "</w:rPr>"
	              + "</w:rPrDefault>"
	              + "<w:pPrDefault>"
	                    + "<w:pPr>"
	                          + "<w:spacing w:after=\"120\"/>"
	                    + "</w:pPr>"
	              + "</w:pPrDefault>"
	        + "</w:docDefaults>"
	        + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"Normal\"/>"
	              + "<w:qFormat/>"
	              + "<w:rPr>"
		              + "<w:sz w:val=\"" + fontSizeHps + "\"/>"
		              + "<w:szCs w:val=\"" + fontSizeHps + "\"/>"
	              + "</w:rPr>"
	        + "</w:style>"
	        + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
	              + "<w:name w:val=\"Default Paragraph Font\"/>"
	              + "<w:uiPriority w:val=\"1\"/>"
	              + "<w:semiHidden/>"
	              + "<w:unhideWhenUsed/>"
	        + "</w:style>"
	        + table_styles
	  + "</w:styles>";

	styles_inDefaultParagraphFont = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
	        + "<w:docDefaults>"
	              + "<w:rPrDefault>"
	                    + "<w:rPr>"
	                    	+ "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
	                    + "</w:rPr>"
	              + "</w:rPrDefault>"
	              + "<w:pPrDefault>"
	                    + "<w:pPr>"
	                          + "<w:spacing w:after=\"120\"/>"
	                    + "</w:pPr>"
	              + "</w:pPrDefault>"
	        + "</w:docDefaults>"
	        + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"Normal\"/>"
	              + "<w:qFormat/>"
	        + "</w:style>"
	        + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
	              + "<w:name w:val=\"Default Paragraph Font\"/>"
	              + "<w:uiPriority w:val=\"1\"/>"
	              + "<w:semiHidden/>"
	              + "<w:unhideWhenUsed/>"
	              + "<w:rPr>"
	            	+ "<w:sz w:val=\"" + fontSizeHps + "\"/>"
	            	+ "<w:szCs w:val=\"" + fontSizeHps + "\"/>"
	            + "</w:rPr>"
	        + "</w:style>"
	        + table_styles
	  + "</w:styles>";

	styles_in_basedOn_Normal = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
	        + "<w:docDefaults>"
	              + "<w:rPrDefault>"
	                    + "<w:rPr>"
	                    	+ "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
	                    + "</w:rPr>"
	              + "</w:rPrDefault>"
	              + "<w:pPrDefault>"
	                    + "<w:pPr>"
	                          + "<w:spacing w:after=\"120\"/>"
	                    + "</w:pPr>"
	              + "</w:pPrDefault>"
	        + "</w:docDefaults>"
	        + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"Normal\"/>"
	              + "<w:qFormat/>"
	              + "<w:rPr>"
	                    + "<w:rFonts w:ascii=\"Times New Roman\"/>"
	              + "</w:rPr>"
	        + "</w:style>"
	        + "<w:style w:styleId=\"testStyle\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"testStyle\"/>"
	              + "<w:basedOn w:val=\"Normal\"/>"
	              + "<w:next w:val=\"Normal\"/>"
//	              + "<w:link w:val=\"Heading1Char\"/>"
	              + "<w:uiPriority w:val=\"9\"/>"
	              + "<w:qFormat/>"
	              + "<w:rsid w:val=\"00841CD9\"/>"
	              + "<w:pPr>"
	                    + "<w:keepNext/>"
	                    + "<w:keepLines/>"
	                    + "<w:spacing w:before=\"480\"/>"
	              + "</w:pPr>"
	              + "<w:rPr>"
	                    + "<w:rFonts w:asciiTheme=\"majorHAnsi\" w:cstheme=\"majorBidi\" w:eastAsiaTheme=\"majorEastAsia\" w:hAnsiTheme=\"majorHAnsi\"/>"
	                    + "<w:b/>"
	                    + "<w:bCs/>"
	                    + "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
	                    + "<w:sz w:val=\"" + fontSizeHps + "\"/>"
	                    + "<w:szCs w:val=\"" + fontSizeHps + "\"/>"
	              + "</w:rPr>"
	        + "</w:style>"
	        + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
	              + "<w:name w:val=\"Default Paragraph Font\"/>"
	              + "<w:uiPriority w:val=\"1\"/>"
	              + "<w:semiHidden/>"
	              + "<w:unhideWhenUsed/>"
	        + "</w:style>"
	        + table_styles
	  + "</w:styles>";
		
	styles_basedOn_Normal = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
	        + "<w:docDefaults>"
	              + "<w:rPrDefault>"
	                    + "<w:rPr>"
	                    	+ "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
	                    + "</w:rPr>"
	              + "</w:rPrDefault>"
	              + "<w:pPrDefault>"
	                    + "<w:pPr>"
	                          + "<w:spacing w:after=\"120\"/>"
	                    + "</w:pPr>"
	              + "</w:pPrDefault>"
	        + "</w:docDefaults>"
	        + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"Normal\"/>"
	              + "<w:qFormat/>"
	              + "<w:rPr>"
		                  + "<w:rFonts w:asciiTheme=\"majorHAnsi\" w:cstheme=\"majorBidi\" w:eastAsiaTheme=\"majorEastAsia\" w:hAnsiTheme=\"majorHAnsi\"/>"
		                  + "<w:b/>"
		                  + "<w:bCs/>"
		                  + "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
		                  + "<w:sz w:val=\"" + fontSizeHps + "\"/>"
		                  + "<w:szCs w:val=\"" + fontSizeHps + "\"/>"
		            + "</w:rPr>"
	        + "</w:style>"
	        + "<w:style w:styleId=\"testStyle\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"testStyle\"/>"
	              + "<w:basedOn w:val=\"Normal\"/>"
	              + "<w:next w:val=\"Normal\"/>"
//	              + "<w:link w:val=\"Heading1Char\"/>"
	              + "<w:uiPriority w:val=\"9\"/>"
	              + "<w:qFormat/>"
	              + "<w:rsid w:val=\"00841CD9\"/>"
	              + "<w:pPr>"
	                    + "<w:keepNext/>"
	                    + "<w:keepLines/>"
	                    + "<w:spacing w:before=\"480\"/>"
	              + "</w:pPr>"
	        + "</w:style>"
	        + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
	              + "<w:name w:val=\"Default Paragraph Font\"/>"
	              + "<w:uiPriority w:val=\"1\"/>"
	              + "<w:semiHidden/>"
	              + "<w:unhideWhenUsed/>"
	        + "</w:style>"
	        + table_styles
	  + "</w:styles>";
	
	styles_no_font_sz = "<w:styles mc:Ignorable=\"w14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
	        + "<w:docDefaults>"
	              + "<w:rPrDefault>"
	                    + "<w:rPr>"
	                    	+ "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
	                    + "</w:rPr>"
	              + "</w:rPrDefault>"
	              + "<w:pPrDefault>"
	                    + "<w:pPr>"
	                          + "<w:spacing w:after=\"120\"/>"
	                    + "</w:pPr>"
	              + "</w:pPrDefault>"
	        + "</w:docDefaults>"
	        + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
	              + "<w:name w:val=\"Normal\"/>"
	              + "<w:qFormat/>"
	        + "</w:style>"
	        + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
	              + "<w:name w:val=\"Default Paragraph Font\"/>"
	              + "<w:uiPriority w:val=\"1\"/>"
	              + "<w:semiHidden/>"
	              + "<w:unhideWhenUsed/>"
	        + "</w:style>"
	        + table_styles
	  + "</w:styles>";
	
	
	}
	


	
	static void initTbls(boolean useTableGrid) {
		
		String styleId;

		if (useTableGrid) {
			styleId = "TableGrid";
			table_styles = 
					"<w:style w:default=\"1\" w:styleId=\"TableNormal\" w:type=\"table\">"
				        + "<w:name w:val=\"Normal Table\"/>"
				  + "</w:style>"
				  + "<w:style w:styleId=\"TableGrid\" w:type=\"table\">"
				        + "<w:name w:val=\"Table Grid\"/>"
				        + "<w:basedOn w:val=\"TableNormal\"/>"
				        + "<w:rPr>"
				            // Table style sets to 20pt
					        + "<w:sz w:val=\"40\"/>"
					        + "<w:szCs w:val=\"40\"/>"
					  + "</w:rPr>"
				  + "</w:style>";
		} else {
			styleId = "TableNormal";
			table_styles = 
					"<w:style w:default=\"1\" w:styleId=\"TableNormal\" w:type=\"table\">"
				        + "<w:name w:val=\"Normal Table\"/>"
				        + "<w:rPr>"
				            // Table style sets to 20pt
					        + "<w:sz w:val=\"40\"/>"
					        + "<w:szCs w:val=\"40\"/>"
					  + "</w:rPr>"
				  + "</w:style>";
			
		}
		
		mdpXml_tblStyle = "<w:document  xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" >" 
		        + "<w:body>"
		              + "<w:tbl>"
		                    + "<w:tblPr>"
		                          + "<w:tblStyle w:val=\"" + styleId + "\"/>"
		                    + "</w:tblPr>"
		                    + "<w:tr >"
		                          + "<w:tc>"
		                                + "<w:p>"
		                                      + "<w:r>"
		                                      + "<w:t xml:space=\"preserve\">some latin text here </w:t>"
		                                      + "</w:r>"
		                                + "</w:p>"
		                          + "</w:tc>"
		                    + "</w:tr>"
		              + "</w:tbl>"
		        + "</w:body>"
		  + "</w:document>";
		
		mdpXml_direct_12pt = "<w:document  xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" >" 
		        + "<w:body>"
		              + "<w:tbl>"
		                    + "<w:tblPr>"
	                          + "<w:tblStyle w:val=\"" + styleId + "\"/>"
		                    + "</w:tblPr>"
		                    + "<w:tr >"
		                          + "<w:tc>"
		                                + "<w:p>"
		                                      + "<w:r>"
		                                      	  // Direct formatting of 12pt
			                                      + "<w:rPr>"
				                                      + "<w:sz w:val=\"24\"/>"
				                                      + "<w:szCs w:val=\"24\"/>"
				                                + "</w:rPr>"
		                                      + "<w:t xml:space=\"preserve\">some latin text here </w:t>"
		                                      + "</w:r>"
		                                + "</w:p>"
		                          + "</w:tc>"
		                    + "</w:tr>"
		              + "</w:tbl>"
		        + "</w:body>"
		  + "</w:document>";
		

//	static String mdpXml_tblPr = "<w:document  xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" >" 
//	        + "<w:body>"
//	              + "<w:tbl>"
//	                    + "<w:tblPr>"
//			        	   + "<w:rPr><!-- Not valid -->"
//			    	            // Table style sets to 20pt
//			    		        + "<w:sz w:val=\"40\"/>"
//			    		        + "<w:szCs w:val=\"40\"/>"
//			    		  + "</w:rPr>"
//	                    + "</w:tblPr>"
//	                    + "<w:tr >"
//	                          + "<w:tc>"
//	                                + "<w:p>"
//	                                      + "<w:r>"
//	                                      + "<w:t xml:space=\"preserve\">some latin text here </w:t>"
//	                                      + "</w:r>"
//	                                + "</w:p>"
//	                          + "</w:tc>"
//	                    + "</w:tr>"
//	              + "</w:tbl>"
//	        + "</w:body>"
//	  + "</w:document>";


	
	}





}
