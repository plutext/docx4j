package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;

public class TableIndentInheritance extends AbstractXSLFOTest {

	/*
	 * <fo:table start-indent="-0.17in">
	 * needs <fo:table-body  start-indent="0in">
	 * because otherwise the start-indent value is inherited 
	 * by the content, which we don't want.
	 */
	//@Test
	public  void testTblIndentInheritance() throws Exception {
		
		boolean save = true;
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().setJaxbElement(
				(Document)XmlUtils.unmarshalString(table1XML));		
		
		
    	FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(wordMLPackage);
		
		OutputStream os = null;
		if (save) {
			
			os = new FileOutputStream(new File(System.getProperty("user.dir") + "/OUT_testTblIndentInheritance.pdf"));
			wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_testTblIndentInheritance.docx"));
		} else {
			// want the fo document as the result.
			foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
			
			// exporter writes to an OutputStream.		
			os = new ByteArrayOutputStream(); 
		}
		
    	

		//Don't care what type of exporter you use
//		Docx4J.toFO(foSettings, os, Docx4J.FLAG_NONE);
		//Prefer the exporter, that uses a xsl transformation
		Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);

		if (save) {
			
		} else {
			byte[] bytes = ((ByteArrayOutputStream)os).toByteArray();
			System.out.println(new String(bytes, "UTF-8"));
		
			// Now use XPath to assert it has a table-body
			org.w3c.dom.Document domDoc = w3cDomDocumentFromByteArray( bytes);
			
			assertTrue(this.isAbsent(domDoc, "//fo:table-header"));
			assertTrue(this.isPresent(domDoc, "//fo:table-body"));
		}
		
	}
	

	private static String table1XML = "<w:document mc:Ignorable=\"w14 wp14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
            + "<w:body>"
            + "<w:p>"
                + "<w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/>"
                    +"</w:rPr>"
                +"</w:pPr>"
            +"</w:p>"
            + "<w:tbl>"
                + "<w:tblPr>"
                    + "<w:tblW w:type=\"dxa\" w:w=\"9360\"/>"
                    + "<w:tblInd w:type=\"dxa\" w:w=\"-252\"/>"
                    + "<w:tblBorders>"
                        + "<w:top w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>" // auto becomes "#000000"
                        + "<w:left w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                        + "<w:bottom w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                        + "<w:right w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                        + "<w:insideH w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                        + "<w:insideV w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\"/>"
                    +"</w:tblBorders>"
                    + "<w:tblLayout w:type=\"fixed\"/>"
                    + "<w:tblLook w:firstColumn=\"1\" w:firstRow=\"1\" w:lastColumn=\"1\" w:lastRow=\"1\" w:noHBand=\"0\" w:noVBand=\"0\" w:val=\"01E0\"/>"
                +"</w:tblPr>"
                + "<w:tblGrid>"
                    + "<w:gridCol w:w=\"1577\"/>"
                    + "<w:gridCol w:w=\"7783\"/>"
                +"</w:tblGrid>"
                + "<w:tr w:rsidTr=\"00B413E2\">"
                    + "<w:trPr>"
                        + "<w:trHeight w:val=\"338\"/>"
                    +"</w:trPr>"
                    + "<w:tc>"
                        + "<w:tcPr>"
                            + "<w:tcW w:type=\"dxa\" w:w=\"1577\"/>"
//                            + "<w:tcBorders>"
//                                + "<w:top w:val=\"nil\"/>"
//                                + "<w:left w:val=\"nil\"/>"
//                                + "<w:bottom w:val=\"nil\"/>"
//                                + "<w:right w:val=\"nil\"/>"
//                            +"</w:tcBorders>"
                            + "<w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"/>"
                        +"</w:tcPr>"
                        + "<w:p>"
                            + "<w:pPr>"
                                + "<w:spacing w:line=\"480\" w:lineRule=\"auto\"/>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/>"
                                    + "<w:b/>"
                                +"</w:rPr>"
                            +"</w:pPr>"
                            + "<w:r>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/>"
                                    + "<w:b/>"
                                +"</w:rPr>"
                                + "<w:t>First Name</w:t>"
                            +"</w:r>"
                        +"</w:p>"
                    +"</w:tc>"
                    + "<w:tc>"
                        + "<w:tcPr>"
                            + "<w:tcW w:type=\"dxa\" w:w=\"7783\"/>"
                            + "<w:tcBorders>"
                                + "<w:top w:val=\"nil\"/>"
                                + "<w:left w:val=\"nil\"/>"
                                + "<w:bottom w:val=\"nil\"/>"
                                + "<w:right w:val=\"nil\"/>"
                            +"</w:tcBorders>"
                            + "<w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"/>"
                        +"</w:tcPr>"
                        + "<w:p>"
                            + "<w:pPr>"
                                + "<w:spacing w:line=\"480\" w:lineRule=\"auto\"/>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Courier New\" w:cs=\"Courier New\" w:hAnsi=\"Courier New\"/>"
                                +"</w:rPr>"
                            +"</w:pPr>"
                            + "<w:r>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Courier New\" w:cs=\"Courier New\" w:hAnsi=\"Courier New\"/>"
                                +"</w:rPr>"
                                + "<w:t>____________________________________________________</w:t>"
                            +"</w:r>"
                        +"</w:p>"
                    +"</w:tc>"
                +"</w:tr>"
                + "<w:tr w:rsidTr=\"00B413E2\">"
                    + "<w:trPr>"
                        + "<w:trHeight w:val=\"322\"/>"
                    +"</w:trPr>"
                    + "<w:tc>"
                        + "<w:tcPr>"
                            + "<w:tcW w:type=\"dxa\" w:w=\"1577\"/>"
                            + "<w:tcBorders>"
                                + "<w:top w:val=\"nil\"/>"
                                + "<w:left w:val=\"nil\"/>"
                                + "<w:bottom w:val=\"nil\"/>"
                                + "<w:right w:val=\"nil\"/>"
                            +"</w:tcBorders>"
                            + "<w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"/>"
                        +"</w:tcPr>"
                        + "<w:p>"
                            + "<w:pPr>"
                                + "<w:spacing w:line=\"480\" w:lineRule=\"auto\"/>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/>"
                                    + "<w:b/>"
                                +"</w:rPr>"
                            +"</w:pPr>"
                            + "<w:r>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/>"
                                    + "<w:b/>"
                                +"</w:rPr>"
                                + "<w:t>Last Name</w:t>"
                            +"</w:r>"
                        +"</w:p>"
                    +"</w:tc>"
                    + "<w:tc>"
                        + "<w:tcPr>"
                            + "<w:tcW w:type=\"dxa\" w:w=\"7783\"/>"
                            + "<w:tcBorders>"
                                + "<w:top w:val=\"nil\"/>"
                                + "<w:left w:val=\"nil\"/>"
                                + "<w:bottom w:val=\"nil\"/>"
                                + "<w:right w:val=\"nil\"/>"
                            +"</w:tcBorders>"
                            + "<w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"/>"
                        +"</w:tcPr>"
                        + "<w:p>"
                            + "<w:pPr>"
                                + "<w:spacing w:line=\"480\" w:lineRule=\"auto\"/>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Courier New\" w:cs=\"Courier New\" w:hAnsi=\"Courier New\"/>"
                                +"</w:rPr>"
                            +"</w:pPr>"
                            + "<w:r>"
                                + "<w:rPr>"
                                    + "<w:rFonts w:ascii=\"Courier New\" w:cs=\"Courier New\" w:hAnsi=\"Courier New\"/>"
                                +"</w:rPr>"
                                + "<w:t>____________________________________________________</w:t>"
                            +"</w:r>"
                        +"</w:p>"
                    +"</w:tc>"
                +"</w:tr>"
            +"</w:tbl>"
            + "<w:p>"
                + "<w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/>"
                    +"</w:rPr>"
                +"</w:pPr>"
            +"</w:p>"
            + "<w:p>"
                + "<w:pPr>"
                    + "<w:jc w:val=\"center\"/>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/>"
                        + "<w:b/>"
                        + "<w:sz w:val=\"19\"/>"
                        + "<w:szCs w:val=\"19\"/>"
                    +"</w:rPr>"
                +"</w:pPr>"
                + "<w:bookmarkStart w:id=\"0\" w:name=\"_GoBack\"/>"
                + "<w:bookmarkEnd w:id=\"0\"/>"
            +"</w:p>"
            + "<w:sectPr>"
                + "<w:pgSz w:h=\"15840\" w:w=\"12240\"/>"
                + "<w:pgMar w:bottom=\"1440\" w:footer=\"720\" w:gutter=\"0\" w:header=\"720\" w:left=\"1800\" w:right=\"1800\" w:top=\"1440\"/>"
                + "<w:cols w:space=\"720\"/>"
                + "<w:docGrid w:linePitch=\"360\"/>"
            +"</w:sectPr>"
        +"</w:body>"
    +"</w:document>";	
	    
}
