package org.docx4j.convert.out.XSLFO;

import java.io.File;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix
 * 
 * @author jharrop
 *
 */
public class PStyle12PtInTableGridOverrideTrueTest extends PStyleTableAbstract {
	
	protected static Logger log = LoggerFactory.getLogger(ParagraphStylesInTableFix.class);	
	
/* Test cases:
 * 
 * One set of tests for each of overrideTableStyleFontSizeAndJustification=0|1
 * 
 * A subset for the cases:
 * - w:tblStyle  sets font size 20 directly   
 * - w:tblStyle  where stylename is Table Normal, sets font size 20 directly   
 * (no need for w:tblPr sets font size 20 directly since rPr there is not valid content)
 * 
 * For each subset, the following:
 * - DocDefaults set 12pt
 * - Normal sets 12pt
 * - DefaultParagraphFont sets 12
 * - all silent
 * - style based on normal sets 12
 * - direct formatting sets 12
 * 
 */
	
	@Before
    public void init() {	
	
		OVERRIDE = true;
		// .. so
		EXPECTED_RESULT = 24;
		
		initTbls(true);
		initOtherXml();		
	}
	


	@Test
	public void testTblStyle_DocDefaults() throws Exception {
		// DocumentDefaults value is trumped by table style
		
		test(mdpXml_tblStyle, styles_inRPrDefault, 40);
	
	}

	@Test
	public void testTblStyle_Normal() throws Exception {
	
		/* Where Normal is basedOn our DocDefaults style,  
		 * Word *does* override the table style! 
		 * 
		 * It doesn't if there is just a normal Normal element
		 * which sets the font size.  And that is what we should be testing here.
		 */
		
		test(mdpXml_tblStyle, styles_inNormal);
	}

	@Test 
	public void testTblStyle_DefaultParagraphFont() throws Exception {
		// Value in DefaultParagraphFont is to be ignored
		
		test(mdpXml_tblStyle, styles_inDefaultParagraphFont, 40);
	}

	/**
	 * table says 40, p says nothing
	 * should override, but nothing explicit, so we don't
	 * 
	 * @throws Exception
	 */
	@Test 
	public void testTblStyle_AllSilent() throws Exception {
	
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().setContents(
				(Document)XmlUtils.unmarshalString(mdpXml_tblStyle) );
		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().setContents(
				(Styles)XmlUtils.unmarshalString(styles_no_font_sz) );
		
//		// NB createVirtualStylesForDocDefaults() puts 10pt there, if nothing is specified!
//		// So we need to delete that!
//		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().createVirtualStylesForDocDefaults();
//		Style dd = wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getStyleById("DocDefaults");
//		dd.getRPr().setSz(null);
//		dd.getRPr().setSzCs(null);
		
		setSetting(wordMLPackage, OVERRIDE);  // table style should get overridden
	
		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
		ParagraphStylesInTableFix.process(wordMLPackage);
		
		Style s = getStyle(wordMLPackage, getStyleName());
		this.assertSz(s, 40); // nothing explicit to override with, so use table val
		
	}
	
	@Test 
	public void testTblStyle_BasedOnNormal() throws Exception {
		
		// Compat setting says Paragraph style overrides table style		
	
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().setContents(
				(Document)XmlUtils.unmarshalString(mdpXml_tblStyle) );
		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().setContents(
				(Styles)XmlUtils.unmarshalString(styles_in_basedOn_Normal) );
		
		// Use our style!
		List<Object> xpathResults = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath("//w:p", true);
		PPr ppr = Context.getWmlObjectFactory().createPPr();
		((P)xpathResults.get(0)).setPPr(ppr);
		PStyle ps = Context.getWmlObjectFactory().createPPrBasePStyle();
		ps.setVal("testStyle");
		ppr.setPStyle(ps);
		
		setSetting(wordMLPackage, OVERRIDE);  // table style should get overridden
	
		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
		ParagraphStylesInTableFix.process(wordMLPackage);
		
//		// Revert style and save: 
//		ppr.setPStyle(ps); // doesn't work - wrong ref!
//		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
		Style ours = null;
		for (Style s : wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle()) {
			if ("testStyle-TableGrid-BR".equals(s.getStyleId())) {
				ours = s;
				break;
			}
		}
		
//		Style s = getStyle(wordMLPackage, STYLE_NAME);
		Assert.assertTrue(ours.getRPr().getSz().getVal().intValue()==24); 
	}

	
	@Test 
	public void testTblStyle_BasedOn_Normal12() throws Exception {
		
		// Compat setting says Paragraph style overrides table style		
	
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().setContents(
				(Document)XmlUtils.unmarshalString(mdpXml_tblStyle) );
		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().setContents(
				(Styles)XmlUtils.unmarshalString(styles_basedOn_Normal) );
		
		// Use our style!
		List<Object> xpathResults = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath("//w:p", true);
		PPr ppr = Context.getWmlObjectFactory().createPPr();
		((P)xpathResults.get(0)).setPPr(ppr);
		PStyle ps = Context.getWmlObjectFactory().createPPrBasePStyle();
		ps.setVal("testStyle");
		ppr.setPStyle(ps);
		
		setSetting(wordMLPackage, OVERRIDE); 

		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
		ParagraphStylesInTableFix.process(wordMLPackage);
		
//		// Revert style and save: 
//		ppr.setPStyle(ps); // doesn't work - wrong ref!
//		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
		Style ours = null;
		for (Style s : wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle()) {
			if ("testStyle-TableGrid-BR".equals(s.getStyleId())) {
				ours = s;
				break;
			}
		}
		
//		Style s = getStyle(wordMLPackage, STYLE_NAME);
		Assert.assertTrue(ours.getRPr().getSz().getVal().intValue()==EXPECTED_RESULT); 
	}

	@Test
	//@Ignore
	public void testTblStyle_P12() throws Exception {
		
		WordprocessingMLPackage wordMLPackage = test(mdpXml_direct_12pt, styles_no_font_sz, 40);
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
		/* In this case, our result correctly preserves the direct rPr formatting
		 * (so the contents of Normal-TableGrid-BR is irrelevant)
		 * 
	        <w:tc>
	          <w:p>
	            <w:pPr>
	              <w:pStyle w:val="Normal-TableGrid-BR"/>
	            </w:pPr>
	            <w:r>
	              <w:rPr>
	                <w:sz w:val="24"/>
	                <w:szCs w:val="24"/>
	              </w:rPr>
	              <w:t xml:space="preserve">some latin text here </w:t>
	            </w:r>
	          </w:p>
	        </w:tc>
         */
		
		List<Object> xpathResults = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath("//w:rPr", true);
		RPr rPr = ((RPr)xpathResults.get(0));
		Assert.assertTrue(rPr.getSz().getVal().intValue()==24); 
	
		
	}
	
}
