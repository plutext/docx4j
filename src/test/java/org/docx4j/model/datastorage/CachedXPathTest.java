package org.docx4j.model.datastorage;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;

import junit.framework.Assert;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class CachedXPathTest {
	
	public static CustomXmlDataStoragePart xmlPart;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		String xml = "<Template><fileNumber>xxxx</fileNumber><Sender class='1'/><Receiver/></Template>";
		
		CustomXmlDataStorage dataStorage = new CustomXmlDataStorageImpl();
		dataStorage.setDocument(
				loadXMLFromString(xml));
		
		xmlPart = new CustomXmlDataStoragePart();
		xmlPart.setData(dataStorage);
		
	}
	

	@Test
	public void simpleNumber() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("count(//Sender)", null);
		System.out.println(result);
		Assert.assertEquals("1", result);
	}

	@Test
	public void simpleString() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("//fileNumber", null);
		System.out.println(result);
		Assert.assertEquals("xxxx", result);
	}
	
	
	@Test
	public void complexBoolean() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("count(//Sender)>0 or count(//Intermediary)>0", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}

	@Test
	public void complexBoolean2() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("boolean(//Sender) and count(//Receiver)>0", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}

	@Test
	public void complexBoolean3() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("not(//MickeyMouse) or count(//Receiver)>0", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}
	
	@Test
	public void complexNumber() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("count(//*[self::Sender or self::Intermediary])", null);

		System.out.println(result);
		Assert.assertEquals("1", result);
	}

	@Test
	public void simpleBoolean() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("not(//foo)", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}

	
	@Test
	public void testConvertNumber() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("count(//Sender[@class='1'])", null);
		System.out.println(result);
		Assert.assertEquals("1", result);
	}

	@Test
	public void testConvertNumber2() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("count(//Sender[@class='1'])+1.1", null);
		System.out.println(result);
		Assert.assertEquals("2.1", result);
	}

	@Test
	public void booleanContains() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("contains(//fileNumber/text(), 'xx')", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}

	@Test
	public void booleanStringEquals() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("string(//fileNumber[1])= 'xxxx'", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}

	@Test
	public void booleanStringNotEquals() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("string(//fileNumber[1])!= 'xxxxNot'", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}
	
	@Test
	public void booleanStringEqualsComplex() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("string(//fileNumber[1])= 'xxxx' and string(//fileNumber[1])= 'xxxx'", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}

	@Test
	public void booleanStringLengthGreaterThan() throws Exception {
		
		String result = xmlPart.cachedXPathGetString("string-length(//fileNumber[1])>0", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}

	@Test
	public void localeDeBooleanStringLengthGreaterThan() throws Exception {
		
		Locale deLocale = new Locale("de", "DE");
		Locale.setDefault(deLocale);
		
		String result = xmlPart.cachedXPathGetString("string-length(//fileNumber[1])>0", null);
		System.out.println(result);
		Assert.assertEquals("true", result);
	}
	
	
	@Test
	@Ignore
	public void test() throws Exception {
		
		// org.apache.xpath.domapi.XPathStylesheetDOM3Exception: Prefix must resolve to a namespace: ns0
		
		String xml = "<case xmlns=\"http://gctrack.gao.gov/templates/case-data\"><fileNumber>xxxx</fileNumber></case>";
		
		CustomXmlDataStorage dataStorage = new CustomXmlDataStorageImpl();
		dataStorage.setDocument(
				loadXMLFromString(xml));
		
		CustomXmlDataStoragePart xmlPart = new CustomXmlDataStoragePart();
		xmlPart.setData(dataStorage);
				
		String result = xmlPart.cachedXPathGetString("/ns0:case/ns0:fileNumber", null);
	}
	
	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilder builder = XmlUtils.getNewDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}	
}
