package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
		org.w3c.dom.Document domDoc = w3cDomDocumentFromByteArray( bytes);
		
		assertTrue(this.isAbsent(domDoc, "//fo:table-header"));
		assertTrue(this.isPresent(domDoc, "//fo:table-body"));
		
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
		org.w3c.dom.Document domDoc = w3cDomDocumentFromByteArray( bytes);
		
		assertTrue(this.isAbsent(domDoc, "//fo:table-body[following-sibling::fo:table-header]"));
		assertTrue(this.isPresent(domDoc, "//fo:table-header[following-sibling::fo:table-body]"));
		
	}
	    
}
