package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public class GeneralStringFormattingSwitchTests extends AbstractFormattingSwitchTest {
	
	public GeneralStringFormattingSwitchTests() {
		
		instruction = "USERNAME ";		
		formattingSwitch = "\\*"; 
		
		init();
	}
			
	private void init() {
		
		quads.add(new SwitchTestQuad("\"mary smith\"", "Caps", "Mary Smith"));
		quads.add(new SwitchTestQuad("\"marysmith\"", "Caps", "Marysmith"));
		quads.add(new SwitchTestQuad("\"mary smith\"", "FirstCap", "Mary smith"));
		quads.add(new SwitchTestQuad("\"Mary Smith\"", "Lower", "mary smith"));
		quads.add(new SwitchTestQuad("\"Mary Smith\"", "Upper", "MARY SMITH"));
		
		// Without instruction, there is no change
		quads.add(new SwitchTestQuad("\"Mary SmiTH\"", "", "Mary SmiTH"));
		quads.add(new SwitchTestQuad("\"mary SmiTH\"", "", "mary SmiTH"));

		// TODO literals, punctuation characters

		// Without quotes, second word is dropped
		quads.add(new SwitchTestQuad("mary smith", "Upper", "Mary")); // but instruction still applied
		quads.add(new SwitchTestQuad("Mary SmiTH", "", "Mary"));

		quads.add(new SwitchTestQuad("\"mary smith\"", "madeupswitch", "Error! Unknown switch argument"));
		
	}
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
		GeneralStringFormattingSwitchTests fst = new GeneralStringFormattingSwitchTests();
//		fst.generateSampleDocx();
		
		fst.testFormatting();
	}

	
	
}
