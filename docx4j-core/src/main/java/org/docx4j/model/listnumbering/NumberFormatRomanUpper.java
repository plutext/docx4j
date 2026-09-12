package org.docx4j.model.listnumbering;

// After Roman.java by Fred Swartz, 2006-12-29, placed in the public domain (MIT
// licence per the footer at
// http://leepoint.net/notes-java/examples/components/romanNumerals/romanNumeral.html).

public class NumberFormatRomanUpper extends NumberFormatRomanAbstract {
	
	
	NumberFormatRomanUpper() {
		
		ROMAN_VALUE_TABLE = ROMAN_VALUE_TABLE_UPPER;
	}
	
	
 // This could be alternatively be done with parallel arrays.
 // Another alternative would be Pair<Integer, String>
 static RomanValue[] ROMAN_VALUE_TABLE_UPPER = {	 
     new RomanValue(1000, "M"),
     new RomanValue( 900, "CM"),
     new RomanValue( 500, "D"),
     new RomanValue( 400, "CD"),
     new RomanValue( 100, "C"),
     new RomanValue(  90, "XC"),
     new RomanValue(  50, "L"),
     new RomanValue(  40, "XL"),
     new RomanValue(  10, "X"),
     new RomanValue(   9, "IX"),
     new RomanValue(   5, "V"),
     new RomanValue(   4, "IV"),
     new RomanValue(   1, "I")
 };
 
}