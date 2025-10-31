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
import org.docx4j.utils.XPathFactoryUtil;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Test XPath expression handling using XPath 2.0.
 * You need Saxon for this.
 * @since 11.5.7
 */
public class XpathrefXPath2BooleanTest {
	
	public static CustomXmlDataStoragePart xmlPart;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		XPathFactoryUtil.setxPathFactory(new net.sf.saxon.xpath.XPathFactoryImpl());
		
		Docx4jProperties.setProperty("org.opendope.conditions.Xpathref.XPathBoolean", "cast2");
		Docx4jProperties.setProperty("org.docx4j.openpackaging.parts.XmlPart.xpath2.typechecking", "cast2");
				
		String xml = "<Template><fileNumber>xxxx</fileNumber><Sender class='1'><id>3</id><val>some</val><val2>true</val2></Sender><Receiver/></Template>";
		
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

	public boolean evaluate(String xpath, String prefixMappings) throws Exception {
		
		// mimic org.opendope.conditions.Xpathref.xpathEval(WordprocessingMLPackage, Map<String, CustomXmlPart>, String, String, String)
		if (xpath.contains("preceding-sibling")) {
			xpath = xpath.replace("][1]", "]"); // replace segment eg phase[1][1] to match map				
		}
		
		return xmlPart.cachedXPathGetBoolean(xpath, prefixMappings);
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
		boolean result = evaluate("//Sender/val2");
		assertTrue( result);
	}

	@Test
	public void nodesetCastException() throws Exception {
		
		// 3sometrue
        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean");	
		
		// non-empty
		boolean result = evaluate("//Sender");
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
	public void string2CastError() throws Exception {
		
		// org.docx4j.openpackaging.exceptions.Docx4JException: Exception executing xs:boolean(string('hello'));net.sf.saxon.trans.XPathException: The string "hello" cannot be cast to a boolean

        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean");	
		
		// non-empty
		boolean result = evaluate("string('hello')");
	}

	@Test
	public void string3False() throws Exception {
		
		boolean result = evaluate("string('false')");
		assertFalse( result);
	}

	@Test
	public void stringWhitespace() throws Exception {

		// org.docx4j.openpackaging.exceptions.Docx4JException: Exception executing xs:boolean(string('hello'));net.sf.saxon.trans.XPathException: The string "hello" cannot be cast to a boolean

        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean");	
		
		// note carefully!
		boolean result = evaluate("string('  ')");  // whitespace
	}
	
	@Test
	public void stringEmpty() throws Exception {

        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean");	
		
		boolean result = evaluate("string('')");
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
	 *  mixed type comparison
	 * 
	 *********************************************************/

	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void mixedType1() throws Exception {

        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean");	
		
		// net.sf.saxon.trans.XPathException: In {fn:boolean(...) = "foo"}: cannot compare xs:boolean to xs:string
		boolean result = evaluate("boolean(/Template)='foo'");
	}

	@Test
	public void mixedType1B() throws Exception {
		
		boolean result = evaluate("boolean(/Template/Sender/val2)='true'");
		assertTrue( result);  // would still fail; (cannot compare xs:boolean to xs:string) but for our workaround
	}
	
	@Test
	public void mixedType2() throws Exception {
		
        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("ValidationException");	
		
		// Saxon: the string (conversion from node-set) cannot be cast to a boolean
		// In XPath 2.0+,  a string can only be cast to xs:boolean if its lexical value is exactly "true", "false", "1", or "0" (case-sensitive).
		boolean result = evaluate("fn:boolean(/Template)='foo'", "xmlns:fn='http://www.w3.org/2001/XMLSchema'");

	}

	@Test
	public void mixedType2A() throws Exception {
		
        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean"); // foo
		
		boolean result = evaluate("fn:boolean(/Template/Sender/val2)='foo'", "xmlns:fn='http://www.w3.org/2001/XMLSchema'");

	}

	@Test
	public void mixedType2B() throws Exception {
		
		// javax.xml.xpath.XPathExpressionException: net.sf.saxon.trans.XPathException: Cannot compare xs:boolean to xs:string
		boolean result = evaluate("fn:boolean(/Template/Sender/val2)='true'", "xmlns:fn='http://www.w3.org/2001/XMLSchema'");
		assertTrue( result);  // true = 'true' would still fail; (cannot compare xs:boolean to xs:string) but for our workaround
	}

	@Test
	public void mixedType2C() throws Exception {
		
		boolean result = evaluate("fn:boolean(/Template/Sender/val2/text())='true'", "xmlns:fn='http://www.w3.org/2001/XMLSchema'");
		assertTrue( result);  // true = 'true' would still fail; (cannot compare xs:boolean to xs:string) but for our workaround
	}

	@Test
	public void mixedType2D() throws Exception {
		
		boolean result = evaluate("fn:boolean(//val2/text())=fn:boolean('true')", "xmlns:fn='http://www.w3.org/2001/XMLSchema'");
		assertTrue( result);  
	}

	@Test
	public void mixedType2E() throws Exception {
		
		boolean result = evaluate("fn:boolean(//val2/text())=boolean('true')", "xmlns:fn='http://www.w3.org/2001/XMLSchema'");
		assertTrue( result);  
	}
	
	
	@Test
	public void mixedType3() throws Exception {
		
		// Saxon is happy with this
		boolean result = evaluate("boolean(/Template)=boolean('foo')");
		assertTrue( result);
	}
	

	@Test
	public void mixedType4() throws Exception {
		
		boolean result = evaluate("fn:boolean(true() )=boolean('true')", "xmlns:fn='http://www.w3.org/2001/XMLSchema'");
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
		assertTrue( result);
	}

	@Test
	public void complexBoolean2() throws Exception {
		
		boolean result = evaluate("boolean(//Sender) and count(//Receiver)>0");
		assertTrue( result);
	}

	@Test
	public void complexBoolean3() throws Exception {
		
		boolean result = evaluate("not(//MickeyMouse) or count(//Receiver)>0");
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
		assertTrue( result);
	}

	@Test
	public void booleanStringEquals() throws Exception {
		
		boolean result = evaluate("string(//fileNumber[1])= 'xxxx'");
		assertTrue( result);
	}
	
	@Test 
	public void booleanEqualsInPredicate() throws Exception {
		
        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean");  // String 3 	
		
		boolean result = evaluate("//Sender[@class='1']/id");
	}
	
	@Test 
	public void booleanEqualsInPredicate2() throws Exception {

        thrown.expect(Docx4JException.class); 
        thrown.expectMessage("cannot be cast to a boolean");  // String 'cdefg'	
		
		boolean result = evaluate("substring('abcdefg', 6 - number(//Sender[@class='1']/id))");		
	}

	@Test
	public void booleanStringNotEquals() throws Exception {
		
		boolean result = evaluate("string(//fileNumber[1])!= 'xxxxNot'");
		assertTrue( result);
	}
	
	@Test
	public void booleanStringEqualsComplex() throws Exception {
		
		boolean result = evaluate("string(//fileNumber[1])= 'xxxx' and string(//fileNumber[1])= 'xxxx'");
		assertTrue( result);
	}

	@Test
	public void booleanStringLengthGreaterThan() throws Exception {
		
		boolean result = evaluate("string-length(//fileNumber[1])>0");
		assertTrue( result);
	}

	@Test
	public void localeDeBooleanStringLengthGreaterThan() throws Exception {
		
		Locale deLocale = new Locale("de", "DE");
		Locale.setDefault(deLocale);
		
		boolean result = evaluate("string-length(//fileNumber[1])>0");
		assertTrue( result);
	}
	
	
	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilder builder = XmlUtils.getNewDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}	
}
