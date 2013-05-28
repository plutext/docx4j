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
		
		final Conversion c = new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);


		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		
		c.outputXSLFO(baos, new PdfSettings() );

		byte[] bytes = baos.toByteArray();
		System.out.println(new String(bytes, "UTF-8"));
	
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
		
		final Conversion c = new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);


		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		
		c.outputXSLFO(baos, new PdfSettings() );

		byte[] bytes = baos.toByteArray();
		System.out.println(new String(bytes, "UTF-8"));
	
		// Now use XPath to assert it has a table-body
		org.w3c.dom.Document domDoc = w3cDomDocumentFromByteArray( bytes);
		
		assertTrue(this.isAbsent(domDoc, "//fo:table-body[following-sibling::fo:table-header]"));
		assertTrue(this.isPresent(domDoc, "//fo:table-header[following-sibling::fo:table-body]"));
		
	}
	    
}
