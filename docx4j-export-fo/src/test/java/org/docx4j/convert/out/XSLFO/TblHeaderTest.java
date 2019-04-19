package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXParseException;

public class TblHeaderTest extends AbstractXSLFOTest {

	/**
	 * "fo:table" content model is: (marker*,table-column*,table-header?,table-footer?,table-body+)
	 * 
	 * so a table with just a table header should have
	 * that converted to table-body row.
	 */
	@Test
	public  void testTblHeaderOne() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/src/test/resources/tables/tblHeaderTestOne.docx";
		WordprocessingMLPackage wordMLPackage= WordprocessingMLPackage.load(new java.io.File(inputfilepath));	
		
    	FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(wordMLPackage);
		
		// want the fo document as the result.
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		
		// exporter writes to an OutputStream.		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    	

		//Don't care what type of exporter you use
//		Docx4J.toFO(foSettings, os, Docx4J.FLAG_NONE);
		//Prefer the exporter, that uses a xsl transformation
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);

		byte[] bytes = baos.toByteArray();
//		System.out.println(new String(bytes, "UTF-8"));
	
		// Now use XPath to assert it has a table-body
		try {
			org.w3c.dom.Document domDoc = w3cDomDocumentFromByteArray( bytes);
			assertTrue(this.isAbsent(domDoc, "//fo:table-header"));
			assertTrue(this.isPresent(domDoc, "//fo:table-body"));
		} catch (SAXParseException e) {
			
			Assert.fail(e.getMessage());
			Assert.fail(new String(bytes, "UTF-8"));
		}
		
		
	}
	
	/**
	 * "fo:table" content model is: (marker*,table-column*,table-header?,table-footer?,table-body+)
	 * 
	 * so a table with body rows before header rows should have
	 * those body rows converted to header rows.
	 */
	@Test
	public  void testTblHeaderTwo() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/src/test/resources/tables/tblHeaderTestTwo.docx";
		WordprocessingMLPackage wordMLPackage= WordprocessingMLPackage.load(new java.io.File(inputfilepath));	
		
    	FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(wordMLPackage);
		
		// want the fo document as the result.
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		
		// exporter writes to an OutputStream.		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    	

		//Don't care what type of exporter you use
//		Docx4J.toFO(foSettings, os, Docx4J.FLAG_NONE);
		//Prefer the exporter, that uses a xsl transformation
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);

		byte[] bytes = baos.toByteArray();
//		System.out.println(new String(bytes, "UTF-8"));
	
		// Now use XPath to assert it has a table-body
		try {
			org.w3c.dom.Document domDoc = w3cDomDocumentFromByteArray( bytes);
			assertTrue(this.isAbsent(domDoc, "//fo:table-body[following-sibling::fo:table-header]"));
			assertTrue(this.isPresent(domDoc, "//fo:table-header[following-sibling::fo:table-body]"));
		} catch (SAXParseException e) {
			
			Assert.fail(e.getMessage());
			Assert.fail(new String(bytes, "UTF-8"));
		}
		
		
		
	}
	    
}
