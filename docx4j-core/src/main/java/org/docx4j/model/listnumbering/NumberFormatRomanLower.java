package org.docx4j.model.listnumbering;


// After Roman.java by Fred Swartz, 2006-12-29, placed in the public domain (MIT
// licence per the footer at
// http://leepoint.net/notes-java/examples/components/romanNumerals/romanNumeral.html).

public class NumberFormatRomanLower extends NumberFormatRomanAbstract {
	
	
	NumberFormatRomanLower() {
		
		ROMAN_VALUE_TABLE = ROMAN_VALUE_TABLE_LOWER;
	}
	
	 // This could be alternatively be done with parallel arrays.
	 // Another alternative would be Pair<Integer, String>
 static RomanValue[] ROMAN_VALUE_TABLE_LOWER = {
     new RomanValue(1000, "m"),
     new RomanValue( 900, "cm"),
     new RomanValue( 500, "d"),
     new RomanValue( 400, "cd"),
     new RomanValue( 100, "c"),
     new RomanValue(  90, "xc"),
     new RomanValue(  50, "l"),
     new RomanValue(  40, "xl"),
     new RomanValue(  10, "x"),
     new RomanValue(   9, "ix"),
     new RomanValue(   5, "v"),
     new RomanValue(   4, "iv"),
     new RomanValue(   1, "i")
 };
 
}