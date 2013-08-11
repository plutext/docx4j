package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public class NoFormattingSwitchTests extends AbstractFormattingSwitchTest {
	
	public NoFormattingSwitchTests() {
		
		formattingSwitch = ""; 
		
//		initUsername();
		initMERGEFIELD();
//		initDOCPROPERTY();
	}
			
	private void initUsername() {
		instruction = "USERNAME ";		

		
		quads.add(new SwitchTestQuad("\"mary smith\"", "Caps", "Mary Smith"));
		quads.add(new SwitchTestQuad("\"marysmith\"", "Caps", "Marysmith"));
		quads.add(new SwitchTestQuad("\"mary smith\"", "FirstCap", "Mary smith"));
		quads.add(new SwitchTestQuad("\"Mary Smith\"", "Lower", "mary smith"));
		quads.add(new SwitchTestQuad("\"Mary Smith\"", "Upper", "MARY SMITH"));

		quads.add(new SwitchTestQuad("\"Mary Smith\" Capoop", "Upper", "\"MARY SMITH\""));
		
		quads.add(new SwitchTestQuad("mary smith", "Upper", "MARY")); // but instruction still applied
		
		// Without instruction, you get an error		
		quads.add(new SwitchTestQuad("\"Mary SmiTH\"", "", "Error! Switch argument not specified."));
		// .. but MERGEFORMAT is enough
		quads.add(new SwitchTestQuad("\"Mary SmiTH\"", " MERGEFORMAT", "Mary SmiTH"));
		
	}
	
	
private void initDOCPROPERTY() {
	instruction = "DOCPROPERTY "; 
	// NB since the value isn't embedded in the field, you don't need to quote it
	quads.add(new SwitchTestQuad("mary smith", "", "Mary Smith"));
	quads.add(new SwitchTestQuad("mary\"smith", "", "Mary\"Smith"));
	
	// these extra quotes are preserved in the output
	quads.add(new SwitchTestQuad("\"mary smith\"", "", "\"Mary Smith\""));  // missed first cap because of the extra quote
	quads.add(new SwitchTestQuad("\"marysmith\"", "", "\"Marysmith\""));
	quads.add(new SwitchTestQuad("\"mary smith\"", "", "\"Mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "", "\"mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "", "\"MARY SMITH\""));

	quads.add(new SwitchTestQuad("\"Mary Smith\" Capoop", "", "\"MARY SMITH\" CAPOOP"));
	
	// Without instruction, there is no change
	quads.add(new SwitchTestQuad("\"Mary SmiTH\"", "", "\"Mary SmiTH\""));
	quads.add(new SwitchTestQuad("\"mary SmiTH\"", "", "\"mary SmiTH\""));

	// TODO literals, punctuation characters

	// Without quotes..
	quads.add(new SwitchTestQuad("mary smith", "", "MARY SMITH")); // no quotes
	quads.add(new SwitchTestQuad("Mary SmiTH", "", "Mary SmiTH"));

	quads.add(new SwitchTestQuad("\"mary smith\"", "", "Error! Unknown switch argument"));

	quads.add(new SwitchTestQuad("01", "", "01")); // do not treat as a number
	quads.add(new SwitchTestQuad("0.1", "", "0.1")); 
	quads.add(new SwitchTestQuad("0.0", "", "0.0")); // FIXME
	quads.add(new SwitchTestQuad("0.00", "", "0.00")); 
	quads.add(new SwitchTestQuad("0.", "", "0.")); // do not treat as a number


	quads.add(new SwitchTestQuad("\"0.1 A\"", "", "\"0.1 A\"")); 
	quads.add(new SwitchTestQuad("\"0.1 1\"", "", "\"0.1 1\"")); 
	quads.add(new SwitchTestQuad("\"0.1 .\"", "", "\"0.1 .\"")); 
	quads.add(new SwitchTestQuad("\"0.00 0\"", "", "\"0.00 0\"")); 
	quads.add(new SwitchTestQuad("\"0.00 1\"", "", "\"0.00 1\"")); 
	quads.add(new SwitchTestQuad("\"0.00 A\"", "", "\"0.00 A\"")); 
	
	// No quotes...
	quads.add(new SwitchTestQuad("0.1 A", "", "0.1 A")); 
	quads.add(new SwitchTestQuad("0.1 1", "", "0.1 1")); 
	quads.add(new SwitchTestQuad("0.1 .", "", "0.1 .")); 
	quads.add(new SwitchTestQuad("0.00 0", "", "0.00 0")); 
	quads.add(new SwitchTestQuad("0.00 1", "", "0.00 1")); 
	quads.add(new SwitchTestQuad("0.00 A", "", "0.00 A")); 
	
	quads.add(new SwitchTestQuad("0000123456", "", "0000123456")); // do not treat as a number
	quads.add(new SwitchTestQuad("000012345.006", "", "000012345.006")); // do not treat as a number
	
	quads.add(new SwitchTestQuad("0000123AA456", "", "0000123AA456"));
	quads.add(new SwitchTestQuad("0000123AA45.006", "", "0000123AA45.006")); 
	
	quads.add(new SwitchTestQuad("123", "", "123"));
	quads.add(new SwitchTestQuad("-123", "", "-123"));
	quads.add(new SwitchTestQuad("123.", "", "123"));
	quads.add(new SwitchTestQuad("-123.", "", "-123"));
	quads.add(new SwitchTestQuad("0", "", "0"));
	quads.add(new SwitchTestQuad("00", "", "00"));
	quads.add(new SwitchTestQuad("0.", "", "0")); 
	
	quads.add(new SwitchTestQuad("01", "", "01")); 
	quads.add(new SwitchTestQuad("0.1", "", "0.1")); 
	quads.add(new SwitchTestQuad("0.0", "", "0.0"));
	quads.add(new SwitchTestQuad("0.00", "", "0.00")); 
	
	
	// Our behaviour on these is different to Word, because
	// splitParameters splits on the space, whereas Word returns the whole lot.
	quads.add(new SwitchTestQuad("0.00 0", "", "0.00 0")); 
	quads.add(new SwitchTestQuad("0.00 1", "", "0.00 1")); 
	quads.add(new SwitchTestQuad("0.00 A", "", "0.00 A")); 
	quads.add(new SwitchTestQuad("0.00 W", "", "0.00 W")); 
	
	quads.add(new SwitchTestQuad("\"01\"", "", "01")); 
	quads.add(new SwitchTestQuad("\"0.1\"", "", "0.1")); 
	quads.add(new SwitchTestQuad("\"0.0\"", "", "0.0"));
	quads.add(new SwitchTestQuad("\"0.00\"", "", "0.00")); 
	quads.add(new SwitchTestQuad("\"0.00 0\"", "", "0.00 0")); 
	quads.add(new SwitchTestQuad("\"0.00 1\"", "", "0.00 1")); 
	quads.add(new SwitchTestQuad("\"0.00 A\"", "", "0.00 A")); 
	quads.add(new SwitchTestQuad("\"0.00 W\"", "", "0.00 W")); 

	
	quads.add(new SwitchTestQuad("0000123456", "", "0000123456"));
	quads.add(new SwitchTestQuad("000012345.006", "", "000012345.006")); 
	
	quads.add(new SwitchTestQuad("0000123AA456", "", "0000123AA456"));
	quads.add(new SwitchTestQuad("0000123AA45.006", "", "0000123AA45.006"));	
}


private void initMERGEFIELD() {
	instruction = "MERGEFIELD ";   
	// NB since the value isn't embedded in the field, you don't need to quote it
	quads.add(new SwitchTestQuad("mary smith", "", "Mary Smith"));

	//	quads.add(new SwitchTestQuad("mary\"smith", "", "Mary\"Smith"));
	quads.add(new SwitchTestQuad("SPACER", "", "SPACER"));
	
	// these extra quotes are preserved in the output
	quads.add(new SwitchTestQuad("\"mary smith\"", "", "\"Mary Smith\""));  // missed first cap because of the extra quote
	quads.add(new SwitchTestQuad("\"marysmith\"", "", "\"Marysmith\""));
	quads.add(new SwitchTestQuad("\"mary smith\"", "", "\"Mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "", "\"mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "", "\"MARY SMITH\""));

//	quads.add(new SwitchTestQuad("\"Mary Smith\" Capoop", "", "\"MARY SMITH\" CAPOOP"));
	quads.add(new SwitchTestQuad("SPACER", "", "SPACER"));
	
	// Without instruction, there is no change
	quads.add(new SwitchTestQuad("\"Mary SmiTH\"", "", "\"Mary SmiTH\""));
	quads.add(new SwitchTestQuad("\"mary SmiTH\"", "", "\"mary SmiTH\""));

	// TODO literals, punctuation characters

	// Without quotes..
	quads.add(new SwitchTestQuad("mary smith", "", "MARY SMITH")); // no quotes
	quads.add(new SwitchTestQuad("Mary SmiTH", "", "Mary SmiTH"));

	quads.add(new SwitchTestQuad("\"mary smith\"", "", "Error! Unknown switch argument"));

	quads.add(new SwitchTestQuad("01", "", "01")); // do not treat as a number
	quads.add(new SwitchTestQuad("0.1", "", "0.1")); 
	quads.add(new SwitchTestQuad("0.0", "", "0.0")); // FIXME
	quads.add(new SwitchTestQuad("0.00", "", "0.00")); 
	quads.add(new SwitchTestQuad("0.", "", "0.")); // do not treat as a number


	quads.add(new SwitchTestQuad("\"0.1 A\"", "", "\"0.1 A\"")); 
	quads.add(new SwitchTestQuad("\"0.1 1\"", "", "\"0.1 1\"")); 
	quads.add(new SwitchTestQuad("\"0.1 .\"", "", "\"0.1 .\"")); 
	quads.add(new SwitchTestQuad("\"0.00 0\"", "", "\"0.00 0\"")); 
	quads.add(new SwitchTestQuad("\"0.00 1\"", "", "\"0.00 1\"")); 
	quads.add(new SwitchTestQuad("\"0.00 A\"", "", "\"0.00 A\"")); 
	
	// No quotes...
	quads.add(new SwitchTestQuad("0.1 A", "", "0.1 A")); 
	quads.add(new SwitchTestQuad("0.1 1", "", "0.1 1")); 
	quads.add(new SwitchTestQuad("0.1 .", "", "0.1 .")); 
	quads.add(new SwitchTestQuad("0.00 0", "", "0.00 0")); 
	quads.add(new SwitchTestQuad("0.00 1", "", "0.00 1")); 
	quads.add(new SwitchTestQuad("0.00 A", "", "0.00 A")); 
	
	quads.add(new SwitchTestQuad("0000123456", "", "0000123456")); // do not treat as a number
	quads.add(new SwitchTestQuad("000012345.006", "", "000012345.006")); // do not treat as a number
	
	quads.add(new SwitchTestQuad("0000123AA456", "", "0000123AA456"));
	quads.add(new SwitchTestQuad("0000123AA45.006", "", "0000123AA45.006")); 
	
	quads.add(new SwitchTestQuad("123", "", "123"));
	quads.add(new SwitchTestQuad("-123", "", "-123"));
	quads.add(new SwitchTestQuad("123.", "", "123"));
	quads.add(new SwitchTestQuad("-123.", "", "-123"));
	quads.add(new SwitchTestQuad("0", "", "0"));
	quads.add(new SwitchTestQuad("00", "", "00"));
	quads.add(new SwitchTestQuad("0.", "", "0")); 
	
	quads.add(new SwitchTestQuad("01", "", "01")); 
	quads.add(new SwitchTestQuad("0.1", "", "0.1")); 
	quads.add(new SwitchTestQuad("0.0", "", "0.0"));
	quads.add(new SwitchTestQuad("0.00", "", "0.00")); 
	
	
	// Our behaviour on these is different to Word, because
	// splitParameters splits on the space, whereas Word returns the whole lot.
	quads.add(new SwitchTestQuad("0.00 0", "", "0.00 0")); 
	quads.add(new SwitchTestQuad("0.00 1", "", "0.00 1")); 
	quads.add(new SwitchTestQuad("0.00 A", "", "0.00 A")); 
	quads.add(new SwitchTestQuad("0.00 W", "", "0.00 W")); 
	
	quads.add(new SwitchTestQuad("\"01\"", "", "01")); 
	quads.add(new SwitchTestQuad("\"0.1\"", "", "0.1")); 
	quads.add(new SwitchTestQuad("\"0.0\"", "", "0.0"));
	quads.add(new SwitchTestQuad("\"0.00\"", "", "0.00")); 
	quads.add(new SwitchTestQuad("\"0.00 0\"", "", "0.00 0")); 
	quads.add(new SwitchTestQuad("\"0.00 1\"", "", "0.00 1")); 
	quads.add(new SwitchTestQuad("\"0.00 A\"", "", "0.00 A")); 
	quads.add(new SwitchTestQuad("\"0.00 W\"", "", "0.00 W")); 

	
	quads.add(new SwitchTestQuad("0000123456", "", "0000123456"));
	quads.add(new SwitchTestQuad("000012345.006", "", "000012345.006")); 
	
	quads.add(new SwitchTestQuad("0000123AA456", "", "0000123AA456"));
	quads.add(new SwitchTestQuad("0000123AA45.006", "", "0000123AA45.006"));	
	
	quads.add(new SwitchTestQuad("€180,000.00 EUR", "", "180000"));
	
}

	
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
		NoFormattingSwitchTests fst = new NoFormattingSwitchTests();
		
//		fst.generateJUnitTest();
		
		fst.testFormatting();
		
		if (fst.instruction.equals("DOCPROPERTY ") ) {
			fst.generateSampleDocx("test_DOCPROPERTY_NoFormatting.docx");			
		} else if (fst.instruction.equals("MERGEFIELD ") ) {
			fst.generateSampleDocx("test_MERGEFIELD_NoFormatting.docx");			
		} else {
			fst.generateSampleDocx("test_USERNAME_NoFormatting.docx");			
		}
		
	}

	
	
}
