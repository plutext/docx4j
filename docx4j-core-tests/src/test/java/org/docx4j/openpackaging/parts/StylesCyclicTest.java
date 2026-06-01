/*
 *  Copyright 2026, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.
 *
 *     docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
 *     you may not use this file except in compliance with the License. 
 *
 *     You may obtain a copy of the License at 
 *
 *         http://www.apache.org/licenses/LICENSE-2.0 
 *
 *     Unless required by applicable law or agreed to in writing, software 
 *     distributed under the License is distributed on an "AS IS" BASIS, 
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *     See the License for the specific language governing permissions and 
 *     limitations under the License.
 */

package org.docx4j.openpackaging.parts;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.StyleBasedOnHelper;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for cyclic style definitions which unless detected could 
 * lead to StackOverflows in PropertyResolver.
 * 
 */
public class StylesCyclicTest {

	@BeforeClass
    public static void setUpClass() {
        // Exception is ignored
		Docx4jProperties.setProperty("docx4j.openpackaging.exceptions.CyclicStylesException.throw", true);
    }
	
    @Test
    public void testPPrStack() throws Exception {

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

        String openXML = "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:docDefaults>"
                + "<w:rPrDefault>"
                + "<w:rPr>"
                + "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" w:eastAsiaTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\"/>"
                + "<w:sz w:val=\"22\"/>"
                + "<w:szCs w:val=\"22\"/>"
                + "<w:lang w:bidi=\"ar-SA\" w:eastAsia=\"en-US\" w:val=\"en-US\"/>"
                + "</w:rPr>"
                + "</w:rPrDefault>"
                + "<w:pPrDefault>"
                + "<w:pPr>"
                + "<w:spacing w:after=\"200\" w:line=\"276\" w:lineRule=\"auto\"/>"
                + "</w:pPr>"
                + "</w:pPrDefault>"
                + "</w:docDefaults>"
                + "<w:style w:default=\"true\" w:styleId=\"Normal\" w:type=\"paragraph\">"
                + "<w:name w:val=\"Normal\"/>"
                + "<w:basedOn w:val=\"Style1\"/>"
                + "</w:style>"
                + "<w:style w:styleId=\"Style1\" w:type=\"paragraph\">"
                + "<w:name w:val=\"Style1\"/>"
                + "<w:basedOn w:val=\"Normal\"/>"
                + "</w:style>"
                + "<w:style w:styleId=\"Style2\" w:type=\"paragraph\">"
                + "<w:name w:val=\"Style2\"/>"
                + "<w:basedOn w:val=\"Style1\"/>"
                + "</w:style>"
                + "<w:style w:default=\"true\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
                + "<w:name w:val=\"Default Paragraph Font\"/>"
                + "</w:style>"
                + "</w:styles>";

        Styles styles = (Styles) XmlUtils.unmarshalString(openXML);
        mdp.getStyleDefinitionsPart().setContents(styles);

        // This call triggers the property resolution which previously could
        // lead to a StackOverflowError in PropertyResolver.fillPPrStack.
        // The test should fail if a StackOverflowError occurs.
        PropertyResolver pr = null;
        try {
        	pr = mdp.getPropertyResolver();
        } catch (StackOverflowError e) {
            org.junit.Assert.fail("StackOverflowError thrown in PropertyResolver.fillPPrStack: " + e);
        }
        
        // Test 2: StackOverflowError in StyleTree?
        try {
    		mdp.addStyledParagraphOfText("Style1", "Use it");
            mdp.getStyleTree();
        } catch (StackOverflowError e) {
            org.junit.Assert.fail("StackOverflowError thrown in StyleTree: " + e);
        }
        
        // Test 3: StyleBasedOnHelper
        try {
        	StyleBasedOnHelper helper = new StyleBasedOnHelper(pr);
        	Style thisStyle = mdp.getStyleDefinitionsPart().getStyleById("Style1");
        	helper.isBasedOn(thisStyle, "foo");
        } catch (StackOverflowError e) {
            org.junit.Assert.fail("StackOverflowError thrown in StyleBasedOnHelper: " + e);
        }
    }
    
    // <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/>

    @Test
    public void testBasedOnHeading() throws Exception {

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

        String openXML = "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:docDefaults/>"
                + "<w:style w:default=\"true\" w:styleId=\"Normal\" w:type=\"paragraph\">"
                	+ "<w:name w:val=\"Normal\"/>"
                + "</w:style>"
                + "<w:style w:styleId=\"NotHeading1\" w:type=\"paragraph\">"
	                + "<w:name w:val=\"notheading 1\"/>"
	                + "<w:basedOn w:val=\"NotHeading2\"/>"
                + "</w:style>"
                + "<w:style w:styleId=\"NotHeading2\" w:type=\"paragraph\">"
	                + "<w:name w:val=\"notheading 2\"/>"
	                + "<w:basedOn w:val=\"NotHeading1\"/>"
                + "</w:style>"
            + "</w:styles>";

        Styles styles = (Styles) XmlUtils.unmarshalString(openXML);
        mdp.getStyleDefinitionsPart().setContents(styles);

        PropertyResolver pr = null;
        try {
        	pr = mdp.getPropertyResolver();
        } catch (StackOverflowError e) {
            org.junit.Assert.fail("StackOverflowError thrown in PropertyResolver.fillPPrStack: " + e);
        }
        
        // Test 
        try {
        	StyleBasedOnHelper helper = new StyleBasedOnHelper(pr);
        	Style thisStyle = mdp.getStyleDefinitionsPart().getStyleById("NotHeading2");
        	helper.getBasedOnHeading(thisStyle);
        } catch (StackOverflowError e) {
            org.junit.Assert.fail("StackOverflowError thrown in StyleBasedOnHelper: " + e);
        }
    }
    
    
    @Test
    public void testRPrStack() throws Exception {

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

        String openXML = "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:docDefaults>"
                + "<w:rPrDefault>"
                + "<w:rPr>"
                + "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" w:eastAsiaTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\"/>"
                + "</w:rPr>"
                + "</w:rPrDefault>"
                + "<w:pPrDefault>"
                + "<w:pPr>"
                + "<w:spacing w:after=\"200\" w:line=\"276\" w:lineRule=\"auto\"/>"
                + "</w:pPr>"
                + "</w:pPrDefault>"
                + "</w:docDefaults>"
                + "<w:style w:styleId=\"Style1\" w:type=\"character\">"
                + "<w:name w:val=\"Style1\"/>"
                + "<w:basedOn w:val=\"DefaultParagraphFont\"/>"
                + "</w:style>"
                + "<w:style w:default=\"true\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
                + "<w:name w:val=\"Default Paragraph Font\"/>"
                + "<w:basedOn w:val=\"Style1\"/>"
                + "</w:style>"
                + "</w:styles>";

        Styles styles = (Styles) XmlUtils.unmarshalString(openXML);
        mdp.getStyleDefinitionsPart().setContents(styles);

//        String openXML = "<w:p  xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
//                + "<w:r>"
//                        + "<w:rPr>"
//                                + "<w:rStyle w:val=\"Style1\"/>"
//                        + "</w:rPr>"
//                        + "<w:t>run</w:t>"
//                + "</w:r>"
//        + "</w:p>";    
        
        try {
            mdp.getPropertyResolver();
        } catch (StackOverflowError e) {
            org.junit.Assert.fail("StackOverflowError thrown in PropertyResolver.fillRPrStack: " + e);
        }
    }

    @Test
    public void testTableStyleStack() throws Exception {

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

        String openXML = "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
//                + "<w:docDefaults>"
//                + "<w:rPrDefault>"
//                + "<w:rPr>"
//                + "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" w:eastAsiaTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\"/>"
//                + "</w:rPr>"
//                + "</w:rPrDefault>"
//                + "<w:pPrDefault>"
//                + "<w:pPr>"
//                + "<w:spacing w:after=\"200\" w:line=\"276\" w:lineRule=\"auto\"/>"
//                + "</w:pPr>"
//                + "</w:pPrDefault>"
//                + "</w:docDefaults>"
                + "<w:style w:styleId=\"Style1\" w:type=\"table\">"
	                + "<w:name w:val=\"Style1\"/>"
	                + "<w:basedOn w:val=\"myTableStyle\"/>"
                + "</w:style>"
                + "<w:style w:default=\"true\" w:styleId=\"myTableStyle\" w:type=\"table\">"
	                + "<w:name w:val=\"myTableStyle\"/>"
	                + "<w:basedOn w:val=\"Style1\"/>"
                + "</w:style>"
	                
                + "<w:style w:styleId=\"Style2\" w:type=\"paragraph\">"
	                + "<w:name w:val=\"Style2\"/>"
	                + "<w:basedOn w:val=\"Style3\"/>"
                + "</w:style>"
                + "<w:style w:styleId=\"Style3\" w:type=\"paragraph\">"
	                + "<w:name w:val=\"Style3\"/>"
	                + "<w:basedOn w:val=\"Style2\"/>"
                + "</w:style>"
                
                
                
                + "</w:styles>";

        Styles styles = (Styles) XmlUtils.unmarshalString(openXML);
        mdp.getStyleDefinitionsPart().setContents(styles);
        
        TblPr tblpr = Context.getWmlObjectFactory().createTblPr(); 
            // Create object for tblStyle
            CTTblPrBase.TblStyle tblprbasetblstyle = Context.getWmlObjectFactory().createCTTblPrBaseTblStyle(); 
            tblpr.setTblStyle(tblprbasetblstyle); 
                tblprbasetblstyle.setVal( "myTableStyle");         
        try {
        	PropertyResolver pr = mdp.getPropertyResolver();
        	pr.getEffectiveTableStyle(tblpr);
        } catch (StackOverflowError e) {
            org.junit.Assert.fail("StackOverflowError thrown in PropertyResolver.fillRPrStack: " + e);
        }
        
	      String tableXML = "<w:tbl  xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
			      + "<w:tblPr>"
			              + "<w:tblStyle w:val=\"myTableStyle\"/>"
			              + "<w:tblW w:type=\"auto\" w:w=\"0\"/>"
			              + "<w:tblLook w:firstColumn=\"1\" w:firstRow=\"1\" w:lastColumn=\"0\" w:lastRow=\"0\" w:noHBand=\"0\" w:noVBand=\"1\" w:val=\"04A0\"/>"
			      + "</w:tblPr>"
			      + "<w:tblGrid>"
			              + "<w:gridCol w:w=\"9576\"/>"
			      + "</w:tblGrid>"
			      + "<w:tr>"
			              + "<w:tc>"
			                      + "<w:p>"
			                      		+ "<w:pPr>"
			                      			+ "<w:pStyle w:val=\"Style2\"/>"
										+ "</w:pPr>"	              
			                              + "<w:r>"
			                                      + "<w:t>R1c1</w:t>"
			                              + "</w:r>"
			                      + "</w:p>"
			              + "</w:tc>"
			      + "</w:tr>"
			+ "</w:tbl>";

	        Tbl tbl= (Tbl) XmlUtils.unmarshalString(tableXML);
	        mdp.getContent().add(tbl);
	        

	        ParagraphStylesInTableFix.process(wordMLPackage);
    }
    
}
