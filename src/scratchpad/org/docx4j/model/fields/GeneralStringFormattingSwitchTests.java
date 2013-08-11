package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public class GeneralStringFormattingSwitchTests extends AbstractFormattingSwitchTest {
	
	public GeneralStringFormattingSwitchTests() {
		
		formattingSwitch = "\\*"; 
		
//		initUsername();
//		initMERGEFIELD();
		initDOCPROPERTY();
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
	quads.add(new SwitchTestQuad("mary smith", "Caps", "Mary Smith"));
	quads.add(new SwitchTestQuad("mary\"smith", "Caps", "Mary\"Smith"));
	
	// these extra quotes are preserved in the output
	quads.add(new SwitchTestQuad("\"mary smith\"", "Caps", "\"Mary Smith\""));  // missed first cap because of the extra quote
	quads.add(new SwitchTestQuad("\"marysmith\"", "Caps", "\"Marysmith\""));
	quads.add(new SwitchTestQuad("\"mary smith\"", "FirstCap", "\"Mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "Lower", "\"mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "Upper", "\"MARY SMITH\""));

	quads.add(new SwitchTestQuad("\"Mary Smith\" Capoop", "Upper", "\"MARY SMITH\" CAPOOP"));
	
	// Without instruction, there is no change
	quads.add(new SwitchTestQuad("\"Mary SmiTH\"", "MERGEFORMAT", "\"Mary SmiTH\""));
	quads.add(new SwitchTestQuad("\"mary SmiTH\"", "MERGEFORMAT", "\"mary SmiTH\""));

	// TODO literals, punctuation characters

	// Without quotes..
	quads.add(new SwitchTestQuad("mary smith", "Upper", "MARY SMITH")); // no quotes
	quads.add(new SwitchTestQuad("Mary SmiTH", "MERGEFORMAT", "Mary SmiTH"));

	quads.add(new SwitchTestQuad("\"mary smith\"", "madeupswitch", "Error! Unknown switch argument"));

	quads.add(new SwitchTestQuad("01", "MERGEFORMAT", "01")); // do not treat as a number
	quads.add(new SwitchTestQuad("0.1", "MERGEFORMAT", "0.1")); 
	quads.add(new SwitchTestQuad("0.0", "MERGEFORMAT", "0.0")); // FIXME
	quads.add(new SwitchTestQuad("0.00", "MERGEFORMAT", "0.00")); 
	quads.add(new SwitchTestQuad("0.", "MERGEFORMAT", "0.")); // do not treat as a number


	quads.add(new SwitchTestQuad("\"0.1 A\"", "MERGEFORMAT", "\"0.1 A\"")); 
	quads.add(new SwitchTestQuad("\"0.1 1\"", "MERGEFORMAT", "\"0.1 1\"")); 
	quads.add(new SwitchTestQuad("\"0.1 .\"", "MERGEFORMAT", "\"0.1 .\"")); 
	quads.add(new SwitchTestQuad("\"0.00 0\"", "MERGEFORMAT", "\"0.00 0\"")); 
	quads.add(new SwitchTestQuad("\"0.00 1\"", "MERGEFORMAT", "\"0.00 1\"")); 
	quads.add(new SwitchTestQuad("\"0.00 A\"", "MERGEFORMAT", "\"0.00 A\"")); 
	
	// No quotes...
	quads.add(new SwitchTestQuad("0.1 A", "MERGEFORMAT", "0.1 A")); 
	quads.add(new SwitchTestQuad("0.1 1", "MERGEFORMAT", "0.1 1")); 
	quads.add(new SwitchTestQuad("0.1 .", "MERGEFORMAT", "0.1 .")); 
	quads.add(new SwitchTestQuad("0.00 0", "MERGEFORMAT", "0.00 0")); 
	quads.add(new SwitchTestQuad("0.00 1", "MERGEFORMAT", "0.00 1")); 
	quads.add(new SwitchTestQuad("0.00 A", "MERGEFORMAT", "0.00 A")); 
	
	quads.add(new SwitchTestQuad("0000123456", "MERGEFORMAT", "0000123456")); // do not treat as a number
	quads.add(new SwitchTestQuad("000012345.006", "MERGEFORMAT", "000012345.006")); // do not treat as a number
	
	quads.add(new SwitchTestQuad("0000123AA456", "MERGEFORMAT", "0000123AA456"));
	quads.add(new SwitchTestQuad("0000123AA45.006", "MERGEFORMAT", "0000123AA45.006")); 
	
}


private void initMERGEFIELD() {
	instruction = "MERGEFIELD ";   
	// NB since the value isn't embedded in the field, you don't need to quote it
	quads.add(new SwitchTestQuad("mary smith", "Caps", "Mary Smith"));  // TODO FIX in simple field code path
//	quads.add(new SwitchTestQuad("mary\"smith", "Caps", "\"Mary Smith\"")); // can't use this in a txt mailmerge

	// these extra quotes are preserved in the output
	quads.add(new SwitchTestQuad("\"mary smith\"", "Caps", "\"Mary Smith\""));  // missed first cap because of the extra quote
	quads.add(new SwitchTestQuad("\"marysmith\"", "Caps", "\"Marysmith\""));
	quads.add(new SwitchTestQuad("\"mary smith\"", "FirstCap", "\"Mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "Lower", "\"mary smith\""));
	quads.add(new SwitchTestQuad("\"Mary Smith\"", "Upper", "\"MARY SMITH\""));
	
//	quads.add(new SwitchTestQuad("\"Mary Smith\" Capoop", "Upper", "\"MARY SMITH\""));	 // can't use this in a txt mailmerge
	
	// Without instruction, there is no change
	quads.add(new SwitchTestQuad("\"Mary SmiTH\"", "", "\"Mary SmiTH\""));
	quads.add(new SwitchTestQuad("\"mary SmiTH\"", "", "\"mary SmiTH\""));

	// TODO literals, punctuation characters

	// Without quotes..
	quads.add(new SwitchTestQuad("mary smith", "Upper", "MARY SMITH")); // no quotes
	quads.add(new SwitchTestQuad("Mary SmiTH", "", "Mary SmiTH"));

	quads.add(new SwitchTestQuad("\"mary smith\"", "madeupswitch", "Error! Unknown switch argument"));

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
	
}

	
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
		GeneralStringFormattingSwitchTests fst = new GeneralStringFormattingSwitchTests();
		
//		fst.generateJUnitTest();
		
		fst.testFormatting();
		
		if (fst.instruction.equals("DOCPROPERTY ") ) {
			fst.generateSampleDocx("test_DOCPROPERTY_GeneralFormatting.docx");			
		} else if (fst.instruction.equals("MERGEFIELD ") ) {
			fst.generateSampleDocx("test_MERGEFIELD_GeneralFormatting.docx");			
		} else {
			fst.generateSampleDocx("test_USERNAME_GeneralFormatting.docx");			
		}
		
	}

	
	
}
