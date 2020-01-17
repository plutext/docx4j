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
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix
 * 
 * @author jharrop
 *
 */
public class PStyle11PtInTableOverrideFalseTest extends PStyleTableAbstract {
	
	protected static Logger log = LoggerFactory.getLogger(ParagraphStylesInTableFix.class);	
	
/* Contrary to the Microsoft documentation, 
 * 11pt does not trigger the exception in Word 2010.
 * (Nor does 20, 21, 23, 25, 26)
 * Our implementation (code and these tests) reflects that.
 */
	
	@Before
    public void init() {	
		
		OVERRIDE = false;
		// .. so
		EXPECTED_RESULT = 40; // table trumps para
		
		initTbls(true);
		initOtherXml(22);  // 11pt
		
	}
	
	
	@Test
	@Ignore
	public void testTblStyle_DocDefaults() throws Exception {
		// NB createVirtualStylesForDocDefaults() puts 10pt there, if nothing is specified!
		
		test(mdpXml_tblStyle, styles_inRPrDefault);
	
	}

	@Test 
	public void testTblStyle_Normal() throws Exception {
		
		test(mdpXml_tblStyle, styles_inNormal, 22);
		
	}

	@Test 
	@Ignore
	public void testTblStyle_DefaultParagraphFont() throws Exception {

		test(mdpXml_tblStyle, styles_inDefaultParagraphFont);
		
	}

	@Test 
	@Ignore
	public void testTblStyle_AllSilent() throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().setContents(
				(Document)XmlUtils.unmarshalString(mdpXml_tblStyle) );
		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().setContents(
				(Styles)XmlUtils.unmarshalString(styles_no_font_sz) );

		setSetting(wordMLPackage, OVERRIDE); 

		wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_PStyleInTableTest.docx"));
		
//		// NB createVirtualStylesForDocDefaults() puts 10pt there, if nothing is specified!
//		// So we need to delete that!
//		wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().createVirtualStylesForDocDefaults();
//		Style dd = wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getStyleById("DocDefaults");
//		dd.getRPr().setSz(null);
//		dd.getRPr().setSzCs(null);
		
		ParagraphStylesInTableFix.process(wordMLPackage);
		
		Style s = getStyle(wordMLPackage, getStyleName());
		Assert.assertTrue(s.getRPr().getSz().getVal().intValue()==EXPECTED_RESULT); 
	}
	
	@Test 
	@Ignore
	public void testTblStyle_BasedOnNormal() throws Exception {
		
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
		Assert.assertTrue(ours.getRPr().getSz().getVal().intValue()==22); 
	}

	@Test 
	@Ignore
	public void testTblStyle_BasedOn_Normal11() throws Exception {
		
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
		Assert.assertTrue(ours.getRPr().getSz().getVal().intValue()==22); 
	}

	// public void testTblStyle_P12() throws Exception 
	
}
