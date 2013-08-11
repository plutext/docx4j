package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public class DateTimeFormattingSwitchTests extends AbstractFormattingSwitchTest {
	
	public DateTimeFormattingSwitchTests() {
		
		formattingSwitch = "\\@"; 
		
		// For \@ date formatting, Word seems to give same results for
		// DATE, DOCPROPERTY, and MERGEFIELD,
		// but to have a different code path for =
		
//		initDate();
//		initEqual(); // not implemented really
		initDOCPROPERTY();		
//		initMERGEFIELD();		
	}
			
	private void initDate() {
		instruction = "DATE ";		
		
		// This date needs to be parsed using US formatting
		// docx4j.Fields.Dates.DateFormatInferencer.USA=true

		quads.add(new SwitchTestQuad("4/15/2013", " ", "4/15/2013", "4/15/2013"));
		
		// Examples from 4ed spec
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy", "4/15/2013", "4/15/2013"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"dddd, MMMM dd, yyyy\"", "Monday, April 15, 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM d, yyyy\"", "April 15, 2013"));

		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yy", "4/15/13"));
		quads.add(new SwitchTestQuad("4/15/2013", "yyyy-MM-dd", "2013-04-15"));
		quads.add(new SwitchTestQuad("4/15/2013", "d-MMM-yy", "15-Apr-13"));
		quads.add(new SwitchTestQuad("4/15/2013", "M.d.yyyy", "4.15.2013"));
		
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMM. d, yy\"", "Apr. 15, 13"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"d MMMM yyyy\"", "15 April 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM yy\"", "April 13"));
		quads.add(new SwitchTestQuad("4/15/2013", "MMM-yy", "Apr-13"));
		
		// Word drops the rest if the quotes are missing
		quads.add(new SwitchTestQuad("4/15/2013", "dddd, MMMM dd, yyyy","Monday,")); 		
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM d, yyyy", "January 3, 2006", "January 3, 2006")); // nope
		quads.add(new SwitchTestQuad("4/15/2013", "MMM. d, yy", "Apr.")); // nope
		quads.add(new SwitchTestQuad("4/15/2013", "d MMMM yyyy", "15")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM yy", "April")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm am/pm", "1/3/2006 5:28 PM","1/3/2006 5:28 PM")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm:ss am/pm","1/3/2006 5:28:34 PM", "1/3/2006 5:28:34 PM")); //nope
		
		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm am/pm\"", "1/3/2006 5:28 PM","4/15/2013 5:28 PM")); 
		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm:ss am/pm\"","1/3/2006 5:28:34 PM", "4/15/2013 5:28:34 PM")); //nope

		// If no time specified, Word uses current time!
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm am/pm", "5:28 PM", "5:28 PM"));
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm:ss am/pm", "5:28:34 PM", "5:28:34 PM"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm am/pm\"", "5:28 PM", "5:28 PM"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm:ss am/pm\"", "5:28:34 PM", "5:28:34 PM"));

		quads.add(new SwitchTestQuad("4/15/2013", "HH:mm", "17:28", "17:28"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"'Today is 'HH:mm:ss\"", "Today is 17:28:34", "Today is 17:28:34"));
		
		// add examples with time
	}

	private void initDOCPROPERTY() {
		instruction = "DOCPROPERTY "; 
		
		// This date needs to be parsed using US formatting
		// docx4j.Fields.Dates.DateFormatInferencer.USA=true

		quads.add(new SwitchTestQuad("4/15/2013", " ", "4/15/2013", "4/15/2013"));
		
		// Examples from 4ed spec
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy", "4/15/2013", "4/15/2013"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"dddd, MMMM dd, yyyy\"", "Monday, April 15, 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM d, yyyy\"", "April 15, 2013"));

		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yy", "4/15/13"));
		quads.add(new SwitchTestQuad("4/15/2013", "yyyy-MM-dd", "2013-04-15"));
		quads.add(new SwitchTestQuad("4/15/2013", "d-MMM-yy", "15-Apr-13"));
		quads.add(new SwitchTestQuad("4/15/2013", "M.d.yyyy", "4.15.2013"));
		
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMM. d, yy\"", "Apr. 15, 13"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"d MMMM yyyy\"", "15 April 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM yy\"", "April 13"));
		quads.add(new SwitchTestQuad("4/15/2013", "MMM-yy", "Apr-13"));
		
		// Word drops the rest if the quotes are missing
		quads.add(new SwitchTestQuad("4/15/2013", "dddd, MMMM dd, yyyy","Monday,")); 		
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM d, yyyy", "January 3, 2006", "January 3, 2006")); // nope
		quads.add(new SwitchTestQuad("4/15/2013", "MMM. d, yy", "Apr.")); // nope
		quads.add(new SwitchTestQuad("4/15/2013", "d MMMM yyyy", "15")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM yy", "April")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm am/pm", "1/3/2006 5:28 PM","1/3/2006 5:28 PM")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm:ss am/pm","1/3/2006 5:28:34 PM", "1/3/2006 5:28:34 PM")); //nope
		
		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm am/pm\"", "1/3/2006 5:28 PM","4/15/2013 5:28 PM")); 
		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm:ss am/pm\"","1/3/2006 5:28:34 PM", "4/15/2013 5:28:34 PM")); //nope

		// If no time specified, Word uses current time!
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm am/pm", "5:28 PM", "5:28 PM"));
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm:ss am/pm", "5:28:34 PM", "5:28:34 PM"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm am/pm\"", "5:28 PM", "5:28 PM"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm:ss am/pm\"", "5:28:34 PM", "5:28:34 PM"));

		quads.add(new SwitchTestQuad("4/15/2013", "HH:mm", "17:28", "17:28"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"'Today is 'HH:mm:ss\"", "Today is 17:28:34", "Today is 17:28:34"));
		
		// add examples with time
	}
	
	private void initMERGEFIELD() {
		instruction = "MERGEFIELD "; 
		
		// This date needs to be parsed using US formatting
		// docx4j.Fields.Dates.DateFormatInferencer.USA=true
		
		// Examples from 4ed spec
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy", "4/15/2013", "4/15/2013"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"dddd, MMMM dd, yyyy\"", "Monday, April 15, 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM d, yyyy\"", "April 15, 2013"));

		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yy", "4/15/13"));
		quads.add(new SwitchTestQuad("4/15/2013", "yyyy-MM-dd", "2013-04-15"));
		quads.add(new SwitchTestQuad("4/15/2013", "d-MMM-yy", "15-Apr-13"));
		quads.add(new SwitchTestQuad("4/15/2013", "M.d.yyyy", "4.15.2013"));
		
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMM. d, yy\"", "Apr. 15, 13"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"d MMMM yyyy\"", "15 April 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM yy\"", "April 13"));
		quads.add(new SwitchTestQuad("4/15/2013", "MMM-yy", "Apr-13"));
		
		// Word drops the rest if the quotes are missing
		quads.add(new SwitchTestQuad("4/15/2013", "dddd, MMMM dd, yyyy","Monday,")); 		
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM d, yyyy", "January 3, 2006", "January 3, 2006")); // nope
		quads.add(new SwitchTestQuad("4/15/2013", "MMM. d, yy", "Apr.")); // nope
		quads.add(new SwitchTestQuad("4/15/2013", "d MMMM yyyy", "15")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM yy", "April")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm am/pm", "1/3/2006 5:28 PM","1/3/2006 5:28 PM")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm:ss am/pm","1/3/2006 5:28:34 PM", "1/3/2006 5:28:34 PM")); //nope
		
		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm am/pm\"", "1/3/2006 5:28 PM","4/15/2013 5:28 PM")); 
		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm:ss am/pm\"","1/3/2006 5:28:34 PM", "4/15/2013 5:28:34 PM")); //nope

		// If no time specified, Word uses current time!
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm am/pm", "5:28 PM", "5:28 PM"));
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm:ss am/pm", "5:28:34 PM", "5:28:34 PM"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm am/pm\"", "5:28 PM", "5:28 PM"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm:ss am/pm\"", "5:28:34 PM", "5:28:34 PM"));

		quads.add(new SwitchTestQuad("4/15/2013", "HH:mm", "17:28", "17:28"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"'Today is 'HH:mm:ss\"", "Today is 17:28:34", "Today is 17:28:34"));
		
		// add examples with time
	}
	
	
	private void initEqual() {
		instruction = "="; 
		
		// Haven't made much effort with "="for DATEs		
		// If Word recognises a date, it'll substitute TODAY's date, then format it
		
		quads.add(new SwitchTestQuad("foo", "M/d/yyyy", "!Undefined Bookmark, FOO"));
		
		// This date needs to be parsed using US formatting
		// docx4j.Fields.Dates.DateFormatInferencer.USA=true
		
		// Examples from 4ed spec
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy", "8/10/2013", "4/15/2013"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"dddd, MMMM dd, yyyy\"",
				"Monday, April 15, 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM d, yyyy\"", "April 15, 2013"));

		// Word drops the rest if the quotes are missing
		quads.add(new SwitchTestQuad("4/15/2013", "dddd, MMMM dd, yyyy","!Syntax Error, MMMM")); 		
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM d, yyyy", "!Syntax Error, D")); // nope
		
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yy", "4/15/13"));
		quads.add(new SwitchTestQuad("4/15/2013", "yyyy-MM-dd", "2013-04-15"));
		quads.add(new SwitchTestQuad("4/15/2013", "d-MMM-yy", "15-Apr-13"));
		quads.add(new SwitchTestQuad("4/15/2013", "M.d.yyyy", "4.15.2013"));
		
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMM. d, yy\"", "Apr. 15, 13"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"d MMMM yyyy\"", "15 April 2013"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"MMMM yy\"", "April 13"));

		// Word drops the rest if the quotes are missing
		quads.add(new SwitchTestQuad("4/15/2013", "MMM. d, yy",  "!Syntax Error, D")); 
		quads.add(new SwitchTestQuad("4/15/2013", "d MMMM yyyy",  "!Syntax Error, MMMM")); 
		quads.add(new SwitchTestQuad("4/15/2013", "MMMM yy", "!Syntax Error, YY")); 
		
		quads.add(new SwitchTestQuad("4/15/2013", "MMM-yy", "Apr-13"));
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm am/pm", "!Syntax Error, H:H"));
		quads.add(new SwitchTestQuad("4/15/2013", "M/d/yyyy h:mm:ss am/pm","!Syntax Error, H:H")); 

		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm am/pm\"", "1/3/2006 5:28 PM","1/3/2006 5:28 PM")); //nope
		quads.add(new SwitchTestQuad("4/15/2013", "\"M/d/yyyy h:mm:ss am/pm\"","1/3/2006 5:28:34 PM", "1/3/2006 5:28:34 PM")); //nope

		// If no time specified, Word uses current time!
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm am/pm", "!Syntax Error, AM"));
		quads.add(new SwitchTestQuad("4/15/2013", "h:mm:ss am/pm",  "!Syntax Error, AM"));

		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm am/pm\"", "5:28 PM", "5:28 PM"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"h:mm:ss am/pm\"", "5:28:34 PM", "5:28:34 PM"));

		quads.add(new SwitchTestQuad("4/15/2013", "HH:mm", "17:28", "17:28"));
		quads.add(new SwitchTestQuad("4/15/2013", "\"'Today is 'HH:mm:ss\"", "Today is 17:28:34", "Today is 17:28:34"));
		
		// add examples with time
	}
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
		DateTimeFormattingSwitchTests fst = new DateTimeFormattingSwitchTests();
//		fst.generateSampleDocx("test_DateTimeFormatting.docx");
		
		fst.generateJUnitTest();
		
//		fst.testFormatting();
//		
//		if (fst.instruction.equals("DATE ") ) {
//			fst.generateSampleDocx("test_DATE_DateFormatting.docx");
//		} else if (fst.instruction.equals("DOCPROPERTY ") ) {
//			fst.generateSampleDocx("test_DOCPROPERTY_DateFormatting.docx");
//		} else if (fst.instruction.equals("MERGEFIELD ") ) {
//			fst.generateSampleDocx("test_MERGEFIELD_DateFormatting.docx");
//		} else {
//			fst.generateSampleDocx("test_DateFormatting.docx");			
//		}
		
	}

	
	
}
