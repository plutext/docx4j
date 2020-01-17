package org.docx4j.model.fields;

import static org.junit.Assert.assertTrue;

import javax.xml.transform.TransformerException;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FormattingSwitchHelperDateTests {
	
	static boolean wasDateFormatInferencerUSA = false;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Tests assume USA date format
		wasDateFormatInferencerUSA = Docx4jProperties.getProperty("docx4j.Fields.Dates.DateFormatInferencer.USA", false);		
		Docx4jProperties.setProperty("docx4j.Fields.Dates.DateFormatInferencer.USA", true);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Docx4jProperties.setProperty("docx4j.Fields.Dates.DateFormatInferencer.USA", wasDateFormatInferencerUSA);
	}
	
	@Test
	public void testDate1() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@  ", "4/15/2013");
	   doit("MERGEFIELD", data, "Error! Switch argument not specified.");
	   doit("DOCPROPERTY", data, "Error! Switch argument not specified.");
	} 
	 
	@Test
	public void testDate2() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ M/d/yyyy", "4/15/2013");
	   doit("MERGEFIELD", data, "4/15/2013");
	   doit("DOCPROPERTY", data, "4/15/2013");
	} 
	 
	@Test
	public void testDate3() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"dddd, MMMM dd, yyyy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "Monday, April 15, 2013");
	   doit("DOCPROPERTY", data, "Monday, April 15, 2013");
	} 
	 
	@Test
	public void testDate4() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"MMMM d, yyyy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "April 15, 2013");
	   doit("DOCPROPERTY", data, "April 15, 2013");
	} 
	 
	@Test
	public void testDate5() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ M/d/yy", "4/15/2013");
	   doit("MERGEFIELD", data, "4/15/13");
	   doit("DOCPROPERTY", data, "4/15/13");
	} 
	 
	@Test
	public void testDate6() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ yyyy-MM-dd", "4/15/2013");
	   doit("MERGEFIELD", data, "2013-04-15");
	   doit("DOCPROPERTY", data, "2013-04-15");
	} 
	 
	@Test
	public void testDate7() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ d-MMM-yy", "4/15/2013");
	   doit("MERGEFIELD", data, "15-Apr-13");
	   doit("DOCPROPERTY", data, "15-Apr-13");
	} 
	 
	@Test
	public void testDate8() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ M.d.yyyy", "4/15/2013");
	   doit("MERGEFIELD", data, "4.15.2013");
	   doit("DOCPROPERTY", data, "4.15.2013");
	} 
	 
	@Test
	public void testDate9() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"MMM. d, yy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "Apr. 15, 13");
	   doit("DOCPROPERTY", data, "Apr. 15, 13");
	} 
	 
	@Test
	public void testDate10() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"d MMMM yyyy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "15 April 2013");
	   doit("DOCPROPERTY", data, "15 April 2013");
	} 
	 
	@Test
	public void testDate11() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"MMMM yy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "April 13");
	   doit("DOCPROPERTY", data, "April 13");
	} 
	 
	@Test
	public void testDate12() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ MMM-yy", "4/15/2013");
	   doit("MERGEFIELD", data, "Apr-13");
	   doit("DOCPROPERTY", data, "Apr-13");
	} 

	
	@Test
	public void testDate13() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"dddd, MMMM dd, yyyy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "Monday, April 15, 2013");
	   doit("DOCPROPERTY", data, "Monday, April 15, 2013");
	} 
	
	
	@Test
	public void testDate14() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"MMMM d, yyyy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "April 15, 2013");
	   doit("DOCPROPERTY", data, "April 15, 2013");
	} 
	 
	@Test
	public void testDate15() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"MMM. d, yy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "Apr. 15, 13");
	   doit("DOCPROPERTY", data, "Apr. 15, 13");
	} 
	 
	@Test
	public void testDate16() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"d MMMM yyyy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "15 April 2013");
	   doit("DOCPROPERTY", data, "15 April 2013");
	} 
	 
	@Test
	public void testDate17() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ \"MMMM yy\"", "4/15/2013");
	   doit("MERGEFIELD", data, "April 13");
	   doit("DOCPROPERTY", data, "April 13");
	} 

	// No quotes ....
	
	@Test
	public void testDateNoQuotes13() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ dddd, MMMM dd, yyyy", "4/15/2013");
	   doit("MERGEFIELD", data, "Monday,");
	   doit("DOCPROPERTY", data, "Monday,");
	} 
	
	
	@Test
	public void testDateNoQuotes14() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ MMMM d, yyyy", "4/15/2013");
	   doit("MERGEFIELD", data, "April");
	   doit("DOCPROPERTY", data, "April");
	} 
	 
	@Test
	public void testDateNoQuotes15() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ MMM. d, yy", "4/15/2013");
	   doit("MERGEFIELD", data, "Apr.");
	   doit("DOCPROPERTY", data, "Apr.");
	} 
	 
	@Test
	public void testDateNoQuotes16() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ d MMMM yyyy", "4/15/2013");
	   doit("MERGEFIELD", data, "15");
	   doit("DOCPROPERTY", data, "15");
	} 
	 
	@Test
	public void testDateNoQuotes17() throws TransformerException, Docx4JException {
	   SwitchTestData data = new SwitchTestData("\\@ MMMM yy", "4/15/2013");
	   doit("MERGEFIELD", data, "April");
	   doit("DOCPROPERTY", data, "April");
	} 
	 
	
	// TODO: where no time is passed in..
	// for DOCPROPERTY, Word uses 12:00 AM
	// for DATE and MERGEFIELD, Word uses current time
	// (for =, Word also used current date) 
	
	
//	@Test
//	public void testDate18() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ M/d/yyyy h:mm am/pm", "4/15/2013");
//	   doit("MERGEFIELD", data, "1/3/2006 5:28 PM");
//	   doit("DOCPROPERTY", data, "1/3/2006 5:28 PM");
//	} 
//	 
//	@Test
//	public void testDate19() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ M/d/yyyy h:mm:ss am/pm", "4/15/2013");
//	   doit("MERGEFIELD", data, "1/3/2006 5:28:34 PM");
//	   doit("DOCPROPERTY", data, "1/3/2006 5:28:34 PM");
//	} 
//	 
//	@Test
//	public void testDate20() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ \"M/d/yyyy h:mm am/pm\"", "4/15/2013");
//	   doit("MERGEFIELD", data, "4/15/2013 5:28 PM");
//	   doit("DOCPROPERTY", data, "4/15/2013 5:28 PM");
//	} 
//	 
//	@Test
//	public void testDate21() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ \"M/d/yyyy h:mm:ss am/pm\"", "4/15/2013");
//	   doit("MERGEFIELD", data, "4/15/2013 5:28:34 PM");
//	   doit("DOCPROPERTY", data, "4/15/2013 5:28:34 PM");
//	} 
//	 
//	@Test
//	public void testDate22() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ h:mm am/pm", "4/15/2013");
//	   doit("MERGEFIELD", data, "5:28 PM");
//	   doit("DOCPROPERTY", data, "5:28 PM");
//	} 
//	 
//	@Test
//	public void testDate23() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ h:mm:ss am/pm", "4/15/2013");
//	   doit("MERGEFIELD", data, "5:28:34 PM");
//	   doit("DOCPROPERTY", data, "5:28:34 PM");
//	} 
//	 
//	@Test
//	public void testDate24() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ \"h:mm am/pm\"", "4/15/2013");
//	   doit("MERGEFIELD", data, "5:28 PM");
//	   doit("DOCPROPERTY", data, "5:28 PM");
//	} 
//	 
//	@Test
//	public void testDate25() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ \"h:mm:ss am/pm\"", "4/15/2013");
//	   doit("MERGEFIELD", data, "5:28:34 PM");
//	   doit("DOCPROPERTY", data, "5:28:34 PM");
//	} 
//	 
//	@Test
//	public void testDate26() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ HH:mm", "4/15/2013");
//	   doit("MERGEFIELD", data, "17:28");
//	   doit("DOCPROPERTY", data, "17:28");
//	} 
//	 
//	@Test
//	public void testDate27() throws TransformerException, Docx4JException {
//	   SwitchTestData data = new SwitchTestData("\\@ \"'Today is 'HH:mm:ss\"", "4/15/2013");
//	   doit("MERGEFIELD", data, "Today is 17:28:34");
//	   doit("DOCPROPERTY", data, "Today is 17:28:34");
//	} 
	
	// ---------------------------------------------------------------------------------------
	
	private void doit(String fieldname, SwitchTestData triple, String expectedResult)  throws TransformerException, Docx4JException {
		
		String instr = fieldname + " foo " + triple.format;
		String result = getFormat(instr, triple.val);
//		System.out.println(result);
		//assertTrue(result.equals(expectedResult));
		org.junit.Assert.assertEquals(expectedResult, result);
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
