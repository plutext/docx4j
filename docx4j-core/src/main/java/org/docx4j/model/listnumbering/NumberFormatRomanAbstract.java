package org.docx4j.model.listnumbering;

// After Roman.java by Fred Swartz, 2006-12-29, placed in the public domain (MIT
// licence per the footer at
// http://leepoint.net/notes-java/examples/components/romanNumerals/romanNumeral.html).

public abstract class NumberFormatRomanAbstract extends LabelFormatter {
 // This could be alternatively be done with parallel arrays.
 // Another alternative would be Pair<Integer, String>
  RomanValue[] ROMAN_VALUE_TABLE; 
 
 public String format(int n) {
     if (n >= 4000  || n < 1) {
         throw new NumberFormatException("Numbers must be in range 1-3999");
     }
     StringBuffer result = new StringBuffer(10);
     
     //... Start with largest value, and work toward smallest.
     for (RomanValue equiv : ROMAN_VALUE_TABLE) {
         //... Remove as many of this value as possible (maybe none).
         while (n >= equiv.intVal) {
             n -= equiv.intVal;            // Subtract value.
             result.append(equiv.romVal);  // Add roman equivalent.
         }
     }
     return result.toString();
 }
 
 protected static class RomanValue {
     //... No need to make this fields private because they are
     //    used only in this private value class.
     int    intVal;     // Integer value.
     String romVal;     // Equivalent roman numeral.
     
     RomanValue(int dec, String rom) {
         this.intVal = dec;
         this.romVal = rom;
     }
 }
}