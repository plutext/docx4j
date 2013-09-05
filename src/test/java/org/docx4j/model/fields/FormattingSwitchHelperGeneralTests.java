package org.docx4j.model.fields;

import static org.junit.Assert.assertTrue;

import javax.xml.transform.TransformerException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Test;

public class FormattingSwitchHelperGeneralTests {
	

	@Test
	public void testStringNotSpecified() throws TransformerException, Docx4JException {
		// For DOCPROPERTY and MERGEFIELD, = (and presumably all others), \* without arg in Word 2010 sp1
		// results in Error! Switch argument not specified, .
		SwitchTestData triple = new SwitchTestData("\\* ", "mary smith", "Error! Switch argument not specified.");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringCaps() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* Caps", "mary smith", "Mary Smith");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringUpper() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* Upper", "mary smith", "MARY SMITH");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone1() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "Mary SmiTH", "Mary SmiTH");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

//	@Test
//	public void testStringmadeupswitch1() throws TransformerException, Docx4JException {
//		   SwitchTestData triple = new SwitchTestData("\\* madeupswitch", "\"mary smith\"", "Error! Unknown switch argument");
//		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
//	}
//
//	@Test
//	public void testStringmadeupswitch2() throws TransformerException, Docx4JException {
//		   SwitchTestData triple = new SwitchTestData("\\* madeupswitch", "mary smith", "Error! Unknown switch argument");
//		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
//	}

	@Test
	public void testStringNone2() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "01", "01");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone3() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.1", "0.1");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone4() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.0", "0.0");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone5() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.00", "0.00");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone6() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.", "0.");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone7() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.1 A", "0.1 A");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone8() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.1 1", "0.1 1");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone9() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.1 .", "0.1 .");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone10() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.00 0", "0.00 0");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone11() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.00 1", "0.00 1");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone12() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0.00 A", "0.00 A");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone13() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0000123456", "0000123456");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone14() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "000012345.006", "000012345.006");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone15() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0000123AA456", "0000123AA456");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone16() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "0000123AA45.006", "0000123AA45.006");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringCapsQuote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* Caps", "\"mary smith\"", "\"Mary Smith\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringCaps2Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* Caps", "\"marysmith\"", "\"Marysmith\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringFirstCapQuote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* FirstCap", "\"mary smith\"", "\"Mary smith\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringLowerQuote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* Lower", "\"Mary Smith\"", "\"mary smith\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringUpperQuote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* Upper", "\"Mary Smith\"", "\"MARY SMITH\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone1Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"Mary SmiTH\"", "\"Mary SmiTH\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone2Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"mary SmiTH\"", "\"mary SmiTH\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone3Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"0.1 A\"", "\"0.1 A\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone4Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"0.1 1\"", "\"0.1 1\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone5Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"0.1 .\"", "\"0.1 .\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone6Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"0.00 0\"", "\"0.00 0\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone7Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"0.00 1\"", "\"0.00 1\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}

	@Test
	public void testStringNone8Quote() throws TransformerException, Docx4JException {
		   SwitchTestData triple = new SwitchTestData("\\* MERGEFORMAT", "\"0.00 A\"", "\"0.00 A\"");
		doit("MERGEFIELD", triple); doit("DOCPROPERTY", triple);
	}
	
	
	// ---------------------------------------------------------------------------------------
	
	private void doit(String fieldname, SwitchTestData triple)  throws TransformerException, Docx4JException {
		
		String instr = fieldname + " foo " + triple.format;
		String result = getFormat(instr, triple.val);
		assertTrue(result.equals(triple.expectedResult));
	}
	
	private String getFormat(String instr, String val) throws TransformerException, Docx4JException {
			
		FldSimpleModel fsm = new FldSimpleModel();
		fsm.build(instr);
		return FormattingSwitchHelper.applyFormattingSwitch(null, fsm, val);		
	}
	
	
	private static class SwitchTestData {

		String format;
		String val;
		String expectedResult;

		public SwitchTestData(String format, String val, String expectedResult) {

			this.format = format;
			this.val = val;
			this.expectedResult = expectedResult;
		}
	}
	
	
}
