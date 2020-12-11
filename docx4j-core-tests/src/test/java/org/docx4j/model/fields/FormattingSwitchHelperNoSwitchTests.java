package org.docx4j.model.fields;

import static org.junit.Assert.assertTrue;

import javax.xml.transform.TransformerException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Test;

public class FormattingSwitchHelperNoSwitchTests {
	
	// In all cases, output = input 
	
	@Test
	public void testNone1() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "mary smith");
	   doit("MERGEFIELD", data, "mary smith");
	   doit("DOCPROPERTY", data, "mary smith");
	} 
	 
	@Test
	public void testNone2() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "mary\"smith");
	   doit("MERGEFIELD", data, "mary\"smith");
	   doit("DOCPROPERTY", data, "mary\"smith");
	} 
	 
	@Test
	public void testNone3() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"mary smith\"");
	   doit("MERGEFIELD", data, "\"mary smith\"");
	   doit("DOCPROPERTY", data, "\"mary smith\"");
	} 
	 
	@Test
	public void testNone4() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"marysmith\"");
	   doit("MERGEFIELD", data, "\"marysmith\"");
	   doit("DOCPROPERTY", data, "\"marysmith\"");
	} 
	 
	@Test
	public void testNone5() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"mary smith\"");
	   doit("MERGEFIELD", data, "\"mary smith\"");
	   doit("DOCPROPERTY", data, "\"mary smith\"");
	} 
	 
	@Test
	public void testNone6() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"Mary Smith\"");
	   doit("MERGEFIELD", data, "\"Mary Smith\"");
	   doit("DOCPROPERTY", data, "\"Mary Smith\"");
	} 
	 
	@Test
	public void testNone7() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"Mary Smith\"");
	   doit("MERGEFIELD", data, "\"Mary Smith\"");
	   doit("DOCPROPERTY", data, "\"Mary Smith\"");
	} 
	 
	@Test
	public void testNone8() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"Mary Smith\" Capoop");
	   doit("MERGEFIELD", data, "\"Mary Smith\" Capoop");
	   doit("DOCPROPERTY", data, "\"Mary Smith\" Capoop");
	} 
	 
	@Test
	public void testNone9() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"Mary SmiTH\"");
	   doit("MERGEFIELD", data, "\"Mary SmiTH\"");
	   doit("DOCPROPERTY", data, "\"Mary SmiTH\"");
	} 
	 
	@Test
	public void testNone10() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"mary SmiTH\"");
	   doit("MERGEFIELD", data, "\"mary SmiTH\"");
	   doit("DOCPROPERTY", data, "\"mary SmiTH\"");
	} 
	 
	@Test
	public void testNone11() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "mary smith");
	   doit("MERGEFIELD", data, "mary smith");
	   doit("DOCPROPERTY", data, "mary smith");
	} 
	 
	@Test
	public void testNone12() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "Mary SmiTH");
	   doit("MERGEFIELD", data, "Mary SmiTH");
	   doit("DOCPROPERTY", data, "Mary SmiTH");
	} 
	 
	@Test
	public void testNone13() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"mary smith\"");
	   doit("MERGEFIELD", data, "\"mary smith\"");
	   doit("DOCPROPERTY", data, "\"mary smith\"");
	} 
	 
	@Test
	public void testNone14() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "01");
	   doit("MERGEFIELD", data, "01");
	   doit("DOCPROPERTY", data, "01");
	} 
	 
	@Test
	public void testNone15() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.1");
	   doit("MERGEFIELD", data, "0.1");
	   doit("DOCPROPERTY", data, "0.1");
	} 
	 
	@Test
	public void testNone16() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.0");
	   doit("MERGEFIELD", data, "0.0");
	   doit("DOCPROPERTY", data, "0.0");
	} 
	 
	@Test
	public void testNone17() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00");
	   doit("MERGEFIELD", data, "0.00");
	   doit("DOCPROPERTY", data, "0.00");
	} 
	 
	@Test
	public void testNone18() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.");
	   doit("MERGEFIELD", data, "0.");
	   doit("DOCPROPERTY", data, "0.");
	} 
	 
	@Test
	public void testNone19() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.1 A\"");
	   doit("MERGEFIELD", data, "\"0.1 A\"");
	   doit("DOCPROPERTY", data, "\"0.1 A\"");
	} 
	 
	@Test
	public void testNone20() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.1 1\"");
	   doit("MERGEFIELD", data, "\"0.1 1\"");
	   doit("DOCPROPERTY", data, "\"0.1 1\"");
	} 
	 
	@Test
	public void testNone21() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.1 .\"");
	   doit("MERGEFIELD", data, "\"0.1 .\"");
	   doit("DOCPROPERTY", data, "\"0.1 .\"");
	} 
	 
	@Test
	public void testNone22() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00 0\"");
	   doit("MERGEFIELD", data, "\"0.00 0\"");
	   doit("DOCPROPERTY", data, "\"0.00 0\"");
	} 
	 
	@Test
	public void testNone23() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00 1\"");
	   doit("MERGEFIELD", data, "\"0.00 1\"");
	   doit("DOCPROPERTY", data, "\"0.00 1\"");
	} 
	 
	@Test
	public void testNone24() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00 A\"");
	   doit("MERGEFIELD", data, "\"0.00 A\"");
	   doit("DOCPROPERTY", data, "\"0.00 A\"");
	} 
	 
	@Test
	public void testNone25() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.1 A");
	   doit("MERGEFIELD", data, "0.1 A");
	   doit("DOCPROPERTY", data, "0.1 A");
	} 
	 
	@Test
	public void testNone26() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.1 1");
	   doit("MERGEFIELD", data, "0.1 1");
	   doit("DOCPROPERTY", data, "0.1 1");
	} 
	 
	@Test
	public void testNone27() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.1 .");
	   doit("MERGEFIELD", data, "0.1 .");
	   doit("DOCPROPERTY", data, "0.1 .");
	} 
	 
	@Test
	public void testNone28() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00 0");
	   doit("MERGEFIELD", data, "0.00 0");
	   doit("DOCPROPERTY", data, "0.00 0");
	} 
	 
	@Test
	public void testNone29() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00 1");
	   doit("MERGEFIELD", data, "0.00 1");
	   doit("DOCPROPERTY", data, "0.00 1");
	} 
	 
	@Test
	public void testNone30() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00 A");
	   doit("MERGEFIELD", data, "0.00 A");
	   doit("DOCPROPERTY", data, "0.00 A");
	} 
	 
	@Test
	public void testNone31() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0000123456");
	   doit("MERGEFIELD", data, "0000123456");
	   doit("DOCPROPERTY", data, "0000123456");
	} 
	 
	@Test
	public void testNone32() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "000012345.006");
	   doit("MERGEFIELD", data, "000012345.006");
	   doit("DOCPROPERTY", data, "000012345.006");
	} 
	 
	@Test
	public void testNone33() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0000123AA456");
	   doit("MERGEFIELD", data, "0000123AA456");
	   doit("DOCPROPERTY", data, "0000123AA456");
	} 
	 
	@Test
	public void testNone34() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0000123AA45.006");
	   doit("MERGEFIELD", data, "0000123AA45.006");
	   doit("DOCPROPERTY", data, "0000123AA45.006");
	} 
	 
	@Test
	public void testNone35() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "123");
	   doit("MERGEFIELD", data, "123");
	   doit("DOCPROPERTY", data, "123");
	} 
	 
	@Test
	public void testNone36() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "-123");
	   doit("MERGEFIELD", data, "-123");
	   doit("DOCPROPERTY", data, "-123");
	} 
	 
	@Test
	public void testNone37() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "123.");
	   doit("MERGEFIELD", data, "123.");
	   doit("DOCPROPERTY", data, "123.");
	} 
	 
	@Test
	public void testNone38() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "-123.");
	   doit("MERGEFIELD", data, "-123.");
	   doit("DOCPROPERTY", data, "-123.");
	} 
	 
	@Test
	public void testNone39() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0");
	   doit("MERGEFIELD", data, "0");
	   doit("DOCPROPERTY", data, "0");
	} 
	 
	@Test
	public void testNone40() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "00");
	   doit("MERGEFIELD", data, "00");
	   doit("DOCPROPERTY", data, "00");
	} 
	 
	@Test
	public void testNone41() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.");
	   doit("MERGEFIELD", data, "0.");
	   doit("DOCPROPERTY", data, "0.");
	} 
	 
	@Test
	public void testNone42() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "01");
	   doit("MERGEFIELD", data, "01");
	   doit("DOCPROPERTY", data, "01");
	} 
	 
	@Test
	public void testNone43() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.1");
	   doit("MERGEFIELD", data, "0.1");
	   doit("DOCPROPERTY", data, "0.1");
	} 
	 
	@Test
	public void testNone44() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.0");
	   doit("MERGEFIELD", data, "0.0");
	   doit("DOCPROPERTY", data, "0.0");
	} 
	 
	@Test
	public void testNone45() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00");
	   doit("MERGEFIELD", data, "0.00");
	   doit("DOCPROPERTY", data, "0.00");
	} 
	 
	@Test
	public void testNone46() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00 0");
	   doit("MERGEFIELD", data, "0.00 0");
	   doit("DOCPROPERTY", data, "0.00 0");
	} 
	 
	@Test
	public void testNone47() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00 1");
	   doit("MERGEFIELD", data, "0.00 1");
	   doit("DOCPROPERTY", data, "0.00 1");
	} 
	 
	@Test
	public void testNone48() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00 A");
	   doit("MERGEFIELD", data, "0.00 A");
	   doit("DOCPROPERTY", data, "0.00 A");
	} 
	 
	@Test
	public void testNone49() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0.00 W");
	   doit("MERGEFIELD", data, "0.00 W");
	   doit("DOCPROPERTY", data, "0.00 W");
	} 
	 
	@Test
	public void testNone50() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"01\"");
	   doit("MERGEFIELD", data, "\"01\"");
	   doit("DOCPROPERTY", data, "\"01\"");
	} 
	 
	@Test
	public void testNone51() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.1\"");
	   doit("MERGEFIELD", data, "\"0.1\"");
	   doit("DOCPROPERTY", data, "\"0.1\"");
	} 
	 
	@Test
	public void testNone52() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.0\"");
	   doit("MERGEFIELD", data, "\"0.0\"");
	   doit("DOCPROPERTY", data, "\"0.0\"");
	} 
	 
	@Test
	public void testNone53() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00\"");
	   doit("MERGEFIELD", data, "\"0.00\"");
	   doit("DOCPROPERTY", data, "\"0.00\"");
	} 
	 
	@Test
	public void testNone54() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00 0\"");
	   doit("MERGEFIELD", data, "\"0.00 0\"");
	   doit("DOCPROPERTY", data, "\"0.00 0\"");
	} 
	 
	@Test
	public void testNone55() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00 1\"");
	   doit("MERGEFIELD", data, "\"0.00 1\"");
	   doit("DOCPROPERTY", data, "\"0.00 1\"");
	} 
	 
	@Test
	public void testNone56() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00 A\"");
	   doit("MERGEFIELD", data, "\"0.00 A\"");
	   doit("DOCPROPERTY", data, "\"0.00 A\"");
	} 
	 
	@Test
	public void testNone57() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "\"0.00 W\"");
	   doit("MERGEFIELD", data, "\"0.00 W\"");
	   doit("DOCPROPERTY", data, "\"0.00 W\"");
	} 
	 
	@Test
	public void testNone58() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0000123456");
	   doit("MERGEFIELD", data, "0000123456");
	   doit("DOCPROPERTY", data, "0000123456");
	} 
	 
	@Test
	public void testNone59() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "000012345.006");
	   doit("MERGEFIELD", data, "000012345.006");
	   doit("DOCPROPERTY", data, "000012345.006");
	} 
	 
	@Test
	public void testNone60() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0000123AA456");
	   doit("MERGEFIELD", data, "0000123AA456");
	   doit("DOCPROPERTY", data, "0000123AA456");
	} 
	 
	@Test
	public void testNone61() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("", "0000123AA45.006");
	   doit("MERGEFIELD", data, "0000123AA45.006");
	   doit("DOCPROPERTY", data, "0000123AA45.006");
	} 

	
	
	// ---------------------------------------------------------------------------------------
	
	private void doit(String fieldname, SwitchTestData triple, String expectedResult)  throws TransformerException, Docx4JException {
		
		String instr = fieldname + " foo " + triple.format;
		String result = getFormat(instr, triple.val);
		System.out.println(result);
		assertTrue(result.equals(expectedResult));
	}
	
	private String getFormat(String instr, String val) throws TransformerException, Docx4JException {
			
		FldSimpleModel fsm = new FldSimpleModel();
		fsm.build(instr);
		return FormattingSwitchHelper.applyFormattingSwitch(null, fsm, val);		
	}
		
	private static class SwitchTestData {

		String format;
		String val;
		
		public String toString() {
			return "format " + format + " to data " + val;
		}

		public SwitchTestData(String format, String val) {

			this.format = format;
			this.val = val;
		}
	}
	
	
}
