package org.docx4j.model.datastorage;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;

import junit.framework.Assert;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XpathrefXPathBooleanTest {
	
	public static CustomXmlDataStoragePart xmlPart;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Docx4jProperties.setProperty("org.opendope.conditions.Xpathref.XPathBoolean", true);
		
		/*
		 *  https://www.w3.org/TR/1999/REC-xpath-19991116/#section-Boolean-Functions
		 *  
			The boolean function converts its argument to a boolean as follows:
			
			+ a number is true if and only if it is neither positive or negative zero nor NaN
			
			+ a node-set is true if and only if it is non-empty
			
			+ a string is true if and only if its length is non-zero
			
			an object of a type other than the four basic types is converted to a boolean in a way that is dependent on that type
			
			See also https://www.w3.org/TR/xpath-31/#id-ebv
		 */
		
		String xml = "<Template><fileNumber>xxxx</fileNumber><Sender class='1'><id>3</id><val>some</val></Sender><Receiver/></Template>";
		
		CustomXmlDataStorage dataStorage = new CustomXmlDataStorageImpl();
		dataStorage.setDocument(
				loadXMLFromString(xml));
		
		xmlPart = new CustomXmlDataStoragePart();
		xmlPart.setData(dataStorage);

		
	}
	
	public boolean evaluate(String xpath) throws Exception {
		
		// mimic org.opendope.conditions.Xpathref.xpathEval(WordprocessingMLPackage, Map<String, CustomXmlPart>, String, String, String)
		if (xpath.contains("preceding-sibling")) {
			xpath = xpath.replace("][1]", "]"); // replace segment eg phase[1][1] to match map				
		}
		
		return xmlPart.cachedXPathGetBoolean(xpath, null);
	}

/*********************************************************
 * 
 *  Number tests
 * 
 *********************************************************/
	
	@Test
	public void simpleNumberTrue() throws Exception {
		
		boolean result = evaluate("number('2')");
		assertTrue( result);
	}

	@Test
	public void simpleNumber2True() throws Exception {
		
		boolean result = evaluate("count(//Sender)");
		assertTrue( result);
	}

	@Test
	public void simpleNumberNegativeTrue() throws Exception {
		
		boolean result = evaluate("number('-1')");
		assertTrue( result);
	}
	
	@Test
	public void simpleNumberFalse() throws Exception {
		
		// Only zero or NaN returns false
		boolean result = evaluate("count(//MissingNode)");
		assertFalse( result);
	}

	@Test
	public void simpleNumber2False() throws Exception {
		
		// Only zero or NaN returns false
		boolean result = evaluate("number('0')");
		assertFalse( result);
	}
	
	@Test
	public void simpleNumber3False() throws Exception {
		
		// Only zero or NaN returns false
		boolean result = evaluate("number('NaN')");
		assertFalse( result);
	}

	@Test
	public void simpleNumber4False() throws Exception {
		
		// Only zero or NaN returns false
		boolean result = evaluate("number('parseme')");
		assertFalse( result);
	}
	
	/*********************************************************
	 * 
	 *  Node-set tests
	 * 
	 *********************************************************/
	
	@Test
	public void nodesetTrue() throws Exception {
		
		// non-empty
		boolean result = evaluate("//Sender");
		assertTrue( result);
	}

	@Test
	public void nodesetEmpty() throws Exception {
		
		// empty
		boolean result = evaluate("//MissingNode");
		assertFalse( result);
	}

	/*********************************************************
	 * 
	 *  String tests
	 * 
	 *********************************************************/

	@Test
	public void string1True() throws Exception {
		
		// non-empty
		boolean result = evaluate("string('true')");
		assertTrue( result);
	}
	
	@Test
	public void string2True() throws Exception {
		
		// non-empty
		boolean result = evaluate("string('hello')");
		assertTrue( result);
	}

	@Test
	public void string3True() throws Exception {
		
		// note carefully!
		boolean result = evaluate("string('false')");
		assertTrue( result);
	}

	@Test
	public void stringFalse() throws Exception {
		
		// non-empty
		boolean result = evaluate("string('')");
		assertFalse( result);
	}

	/*********************************************************
	 * 
	 *  boolean tests
	 * 
	 *********************************************************/

	@Test
	public void simpleBoolean() throws Exception {
		
		boolean result = evaluate("true()");
		assertTrue( result);
	}

	@Test
	public void simpleBoolean2() throws Exception {
		
		boolean result = evaluate("false()");
		assertFalse( result);
	}
	
	@Test
	public void simpleBoolean3() throws Exception {
		
		boolean result = evaluate("not(//MissingNode)");
		assertTrue( result);
	}

	/*********************************************************
	 * 
	 *  miscellaneous examples
	 * 
	 *********************************************************/
	
	@Test
	public void complexBoolean() throws Exception {
		
		boolean result = evaluate("count(//Sender)>0 or count(//Intermediary)>0");
		System.out.println(result);
		assertTrue( result);
	}

	@Test
	public void complexBoolean2() throws Exception {
		
		boolean result = evaluate("boolean(//Sender) and count(//Receiver)>0");
		System.out.println(result);
		assertTrue( result);
	}

	@Test
	public void complexBoolean3() throws Exception {
		
		boolean result = evaluate("not(//MickeyMouse) or count(//Receiver)>0");
		System.out.println(result);
		assertTrue( result);
	}
	
	@Test
	public void complexNumber() throws Exception {
		
		boolean result = evaluate("count(//*[self::Sender or self::Intermediary])");
		// = 1
		assertTrue( result);
	}
	
	@Test
	public void testConvertNumber() throws Exception {
		
		boolean result = evaluate("count(//Sender[@class='1'])");
		// = 1
		assertTrue( result);
	}

	@Test
	public void testConvertNumber2() throws Exception {
		
		boolean result = evaluate("count(//Sender[@class='1'])+1.1");
		// = 2.1
		assertTrue( result);
	}

	@Test
	public void booleanContains() throws Exception {
		
		boolean result = evaluate("contains(//fileNumber/text(), 'xx')");
		System.out.println(result);
		assertTrue( result);
	}

	@Test
	public void booleanStringEquals() throws Exception {
		
		boolean result = evaluate("string(//fileNumber[1])= 'xxxx'");
		System.out.println(result);
		assertTrue( result);
	}
	
	@Test // https://github.com/plutext/docx4j/issues/235
	public void booleanEqualsInPredicate() throws Exception {
		
		boolean result = evaluate("//Sender[@class='1']/id");
		System.out.println(result);
		assertTrue( result);
	}
	
	@Test // https://github.com/plutext/docx4j/issues/235
	public void booleanEqualsInPredicate2() throws Exception {
		
		boolean result = evaluate("substring('abcdefg', 6 - number(//Sender[@class='1']/id))");		
		assertTrue( result);
	}

	@Test
	public void booleanStringNotEquals() throws Exception {
		
		boolean result = evaluate("string(//fileNumber[1])!= 'xxxxNot'");
		System.out.println(result);
		assertTrue( result);
	}
	
	@Test
	public void booleanStringEqualsComplex() throws Exception {
		
		boolean result = evaluate("string(//fileNumber[1])= 'xxxx' and string(//fileNumber[1])= 'xxxx'");
		System.out.println(result);
		assertTrue( result);
	}

	@Test
	public void booleanStringLengthGreaterThan() throws Exception {
		
		boolean result = evaluate("string-length(//fileNumber[1])>0");
		System.out.println(result);
		assertTrue( result);
	}

	@Test
	public void localeDeBooleanStringLengthGreaterThan() throws Exception {
		
		Locale deLocale = new Locale("de", "DE");
		Locale.setDefault(deLocale);
		
		boolean result = evaluate("string-length(//fileNumber[1])>0");
		System.out.println(result);
		assertTrue( result);
	}
	
	
	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilder builder = XmlUtils.getNewDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}	
}
