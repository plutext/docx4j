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

/**
 * Java's Boolean.parseBoolean is used
 * 
 * @author jharrop
 *
 */
public class XpathrefJavaBooleanTest {
	
	public static CustomXmlDataStoragePart xmlPart;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// Use Boolean.parseBoolean
		Docx4jProperties.setProperty("org.opendope.conditions.Xpathref.XPathBoolean", false);
		
		String xml = "<Template><fileNumber>xxxx</fileNumber><Sender class='1'><id>3</id><val>some</val></Sender><Receiver/></Template>";
		
		CustomXmlDataStorage dataStorage = new CustomXmlDataStorageImpl();
		dataStorage.setDocument(
				loadXMLFromString(xml));
		
		xmlPart = new CustomXmlDataStoragePart();
		xmlPart.setData(dataStorage);

		
	}
	
	public boolean evaluate(String xpath) throws Exception {
		
		// mimic org.docx4j.model.datastorage.BindingHandler.xpathGetString(WordprocessingMLPackage, Map<String, CustomXmlPart>, String, String, String)
		if (xpath.contains("preceding-sibling")) {
			xpath = xpath.replace("][1]", "]"); // replace segment eg phase[1][1] to match map				
		}
		
		/*
		 * Step 1:
		 * 
				XPath's string function converts an object to a string as follows:
				
				A node-set is converted to a string by returning the string-value of the node in the node-set that is first in document order. If the node-set is empty, an empty string is returned.
				
				A number is converted to a string as follows
				
					NaN is converted to the string NaN
					
					positive zero is converted to the string 0
					
					negative zero is converted to the string 0
					
					positive infinity is converted to the string Infinity
					
					negative infinity is converted to the string -Infinity
					
					if the number is an integer, the number is represented in decimal form as a Number with no decimal point and no leading zeros, preceded by a minus sign (-) if the number is negative
					
					otherwise, the number is represented in decimal form as a Number including a decimal point with at least one digit before the decimal point and at least one digit after the decimal point, preceded by a minus sign (-) if the number is negative; there must be no leading zeros before the decimal point apart possibly from the one required digit immediately before the decimal point; beyond the one required digit after the decimal point there must be as many, but only as many, more digits as are needed to uniquely distinguish the number from all other IEEE 754 numeric values.
				
				The boolean false value is converted to the string false. The boolean true value is converted to the string true.
				
				An object of a type other than the four basic types is converted to a string in a way that is dependent on that type.

		 * Step 2: Java's Boolean.parseBoolean parses that string:
		 * 
			it returns true if and only if the string argument is not null and is equal, ignoring case, to the string "true".
			
			Key Semantics
			Case-Insensitive: The comparison is not case-sensitive. This means "true", "True", "TRUE", "tRuE", and any other combination of uppercase and lowercase letters will all return true.
			
			Default Value (false): For all other strings, the method returns false. This includes:
			
				"0" or "1"
				An empty string ("")

			So:
			
				- Booleans should convert as expected
				- A node-set only converts to true if the string-value of the first node in the node-set is "true" (case-insensitive). 
				- ALL numbers convert to false.
				
		 */
		
		return Boolean.parseBoolean(
				xmlPart.cachedXPathGetString(xpath, null));
	}

	/*********************************************************
	 * 
	 *  Where results differ from XPath's boolean conversion
	 * 
	 *********************************************************/

	@Test
	public void string3True() throws Exception {
		
		boolean result = evaluate("string('false')");
		assertFalse( result);
	}
	
	@Test
	public void string2True() throws Exception {
		
		// non-empty
		boolean result = evaluate("string('hello')");
		assertFalse( result);
	}

	@Test
	public void nodesetTrue() throws Exception {
		
		// non-empty
		boolean result = evaluate("//Sender");
		assertFalse( result);
	}
	
	@Test // https://github.com/plutext/docx4j/issues/235
	public void booleanEqualsInPredicate() throws Exception {
		
		boolean result = evaluate("//Sender[@class='1']/id");
		System.out.println(result);
		assertFalse( result);
	}

	@Test // https://github.com/plutext/docx4j/issues/235
	public void booleanEqualsInPredicate2() throws Exception {
		
		boolean result = evaluate("substring('abcdefg', 6 - number(//Sender[@class='1']/id))");		
		assertFalse( result);
	}
	
	@Test
	public void simpleNumberTrue() throws Exception {
		
		boolean result = evaluate("number('2')");
		assertFalse( result);
	}
	
	@Test
	public void simpleNumber2True() throws Exception {
		
		boolean result = evaluate("count(//Sender)");
		assertFalse( result);
	}
	
	@Test
	public void simpleNumberNegativeTrue() throws Exception {
		
		boolean result = evaluate("number('-1')");
		assertFalse( result);
	}
	
	@Test
	public void complexNumber() throws Exception {
		
		boolean result = evaluate("count(//*[self::Sender or self::Intermediary])");
		// = 1
		assertFalse( result);
	}
	
	@Test
	public void testConvertNumber2() throws Exception {
		
		boolean result = evaluate("count(//Sender[@class='1'])+1.1");
		// = 2.1
		assertFalse( result);
	}
	
	@Test
	public void testConvertNumber() throws Exception {
		
		boolean result = evaluate("count(//Sender[@class='1'])");
		// = 1
		assertFalse( result);
	}
		
	
/*********************************************************
 * 
 *  Number tests
 * 
 *********************************************************/
	
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
	public void booleanTextEquals() throws Exception {
		
		boolean result = evaluate("//fileNumber[1]/text()= 'xxxx'");
		assertTrue( result);
	}

	@Test
	public void booleanTextNotEquals() throws Exception {
		
		boolean result = evaluate("//fileNumber[1]/text() != 'xyz'");
		assertTrue( result);
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
