package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public class NumericFormattingSwitchTests extends AbstractFormattingSwitchTest {
	
	public NumericFormattingSwitchTests() {
		
		instruction = "=";
		formattingSwitch = "\\#"; 
		
		init();
	}
			
	private void init() {
		
		
		// if no numeric-formatting-switch is present, a numeric result is formatted 
		// without leading spaces or trailing fractional zeros. 
		// If the result is negative, a leading minus sign is present. 
		quads.add(new SwitchTestQuad("123.4500", "", "123.45"));
		quads.add(new SwitchTestQuad("-123.4500", "", "-123.45"));
		// If the result is a whole number, no radix point is present. 
		quads.add(new SwitchTestQuad("123", "", "123"));
		quads.add(new SwitchTestQuad("-123", "", "-123"));
		quads.add(new SwitchTestQuad("123.", "", "123"));
		quads.add(new SwitchTestQuad("-123.", "", "-123"));
		quads.add(new SwitchTestQuad("0", "", "0"));
		quads.add(new SwitchTestQuad("00", "", "0"));
		
		// Examples in spec
		// 0 Specifies the requisite numeric positions to display in the result. 
		// If the result does not include a digit in that position, 0 is displayed.
		
		quads.add(new SwitchTestQuad("9", "00.00", "09.00"));
		quads.add(new SwitchTestQuad("9.006", "00.00", "09.01"));  // rounds
		// # Specifies the requisite numeric positions to display in the result. 
		// If the result does not include a digit in that position, a space is displayed. Extra fractional digits are rounded off. 
		quads.add(new SwitchTestQuad("15", "$###", "$ 15", "$15"));
		quads.add(new SwitchTestQuad("9", "##.##", "9.", "9"));
		quads.add(new SwitchTestQuad("9.006", "##.##", "9.01"));
		quads.add(new SwitchTestQuad("9.006", "##.##0000", "09.006000", "9.006"));
		quads.add(new SwitchTestQuad("9.006", "##.##00#0", "09.0060 0", "9.006"));  // check this

		quads.add(new SwitchTestQuad("123.456", "####.0000", "123.4560"));
		quads.add(new SwitchTestQuad("123.456", "0000.####", "0123.456"));
		quads.add(new SwitchTestQuad("123.456", "#.0000", "123.4560"));
		quads.add(new SwitchTestQuad("123.456", "0.####", "123.456"));
		
		// x Drops digits to the left of the x placeholder. 
		// If the placeholder is to the right of the decimal point, the result is rounded to that place.
		// NOT IMPLEMENTED quads.add(new SwitchTestQuad("11111492", "x##", "492"));
		quads.add(new SwitchTestQuad("123456", "x#.#x", "56.", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("123.456", "x#.#x", "23.46", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("0.125678", "0.00x", "0.126"));
		quads.add(new SwitchTestQuad("0.75", ".x", ".8"));

		quads.add(new SwitchTestQuad("0.75", ".000x", ".750", ".7500"));
		quads.add(new SwitchTestQuad("0.75", ".###x", ".75"));
		quads.add(new SwitchTestQuad(".75", ".###x", ".75"));

		quads.add(new SwitchTestQuad("-0.75", ".###x", ".75", FieldFormattingException.class)); // NB! If you don't have at least one # or 0 before the ., Word's result is unexpected
		quads.add(new SwitchTestQuad("-.75", ".###x", ".75", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("-0.75", ".###", ".75", FieldFormattingException.class)); 
		quads.add(new SwitchTestQuad("-.75", ".###", ".75", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("-0.75", ".000", ".750", FieldFormattingException.class)); 
		quads.add(new SwitchTestQuad("-.75", ".000", ".750", FieldFormattingException.class));
		
		quads.add(new SwitchTestQuad("0.125678", "0.00xx", "0.1257")); // it is the last x which is honoured
		quads.add(new SwitchTestQuad("0.125678", "0.x0x", "0.126"));   // it is the last x which is honoured
		
		// . Indicates the radix-point position.  The radix-point character displayed is locale-specific.
		quads.add(new SwitchTestQuad("95.4", "$###.00", "$ 95.40", "$95.40"));
		quads.add(new SwitchTestQuad("95.4", ".00", ".40", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("95.4", ".##", ".4", FieldFormattingException.class));
		
		// , Separates groups of three digits. The separator character displayed is locale-specific.
		quads.add(new SwitchTestQuad("2456800", "$#,###,###", "$2,456,800"));
		
		// - Prepends a minus sign to a negative result, or prepends a space if the result is positive or 0.  
		quads.add(new SwitchTestQuad("-10", "-##", "-10"));
		quads.add(new SwitchTestQuad("10", "-##", " 10", "10"));

		// + Prepends a plus sign to a positive result, a minus sign to a negative result, or a space if the result is 0. 
		quads.add(new SwitchTestQuad("10", "+##", "+10", "10"));
		quads.add(new SwitchTestQuad("-10", "+##", "-10"));
		
		// Other character: Includes the specified character in the result at that position. 
		quads.add(new SwitchTestQuad("33", "##%", "33%"));
		// Note that these don't have to be at the end of the string
		quads.add(new SwitchTestQuad("33", "#%#", "3%3", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("33", "#$#", "3$3", FieldFormattingException.class));
		
		// 'text' includes text in the result. 
		quads.add(new SwitchTestQuad("3.89", "\"$##0.00 'is the sales tax'\"", "$  3.89 is the sales tax", "$3.89 is the sales tax"));
		// .. its only necessary to get special characters into the result
		quads.add(new SwitchTestQuad("3.89", "\"$##0.00 'is the ##.00 goodness'\"", "$  3.89 is the ##.00 goodness", "$3.89 is the ##.00 goodness"));
		quads.add(new SwitchTestQuad("3.89", "\"$##0.00 is the '##.00' goodness\"", "$  3.89 is the ##.00 oodness", "$3.89 is the ##.00 goodness")); // note missing 'g'!

		// literals don't start a new number; spaces don't either
		// The number can only appear once, so those things only insert other stuff
		quads.add(new SwitchTestQuad("34", "#'y'#", "3y4", FieldFormattingException.class));
/*
		quads.add(new SwitchTestQuad("34", "\" # # \"", "3 4", "34"));
		quads.add(new SwitchTestQuad("34", "\" $## $## \"", "$  $34", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("34", "\" # 'y' # \"", "3 y 4", FieldFormattingException.class));

		quads.add(new SwitchTestQuad("3.89", "\"$##0.00 'is the  goodness'  +###  \"", "$  3.89 is the goodness +", FieldFormattingException.class));
		quads.add(new SwitchTestQuad("123.456789", "\"0.00 'is sales tax' -000000 \"", "123.45 is sales tax  678900'", FieldFormattingException.class));

		// How are spaces after literals handled? Respected
		quads.add(new SwitchTestQuad("34", "\" # 'y'     # \"", " 3 y     4", FieldFormattingException.class));

		// How are spaces in literals handled? Respected
		quads.add(new SwitchTestQuad("34", "\" # 'y     ' # \"", " 3 y      4", FieldFormattingException.class));
		
		// the following give syntax errors in Word since they need to be in quote marks
		// A space is an error unless we're in quotes
		//quads.add(new SwitchTestQuad("34", "# #", "33"));
		//quads.add(new SwitchTestQuad("34", "$## $##", "33"));
		//quads.add(new SwitchTestQuad("34", "# 'y' #", "3y3"));
		//quads.add(new SwitchTestQuad("15", "$ ###", "$ 15"));

		// an escaped quote mark
		quads.add(new SwitchTestQuad("123", "\" 0 ' \\\" '  \"", "123 \" "));
		quads.add(new SwitchTestQuad("123", "\" 0  \\\"   \"", "123 \" "));
		// Not sure how to include a single quote literally?
		//quads.add(new SwitchTestQuad("123", "\" 0 ' \' '  \"", "123.45 is sales tax  678900'"));

		// Characters which are special in Java
		quads.add(new SwitchTestQuad("123", "\"E ## \"", "123"));
		quads.add(new SwitchTestQuad("123", "##E ", "123"));
		quads.add(new SwitchTestQuad("123", "##'E' ", "123"));

		quads.add(new SwitchTestQuad("123", "##;-##", "123"));
		quads.add(new SwitchTestQuad("123", "##%", "123%"));

		quads.add(new SwitchTestQuad("123", "\"\u2030 ## \"", "‰ 123"));
		quads.add(new SwitchTestQuad("123", "\"## \u2030 \"", "123 ‰"));

		quads.add(new SwitchTestQuad("123", "\"\u00A4 ## \"", "¤ 123"));
		quads.add(new SwitchTestQuad("123", "\"## \u00A4 \"", "123 ¤"));
		
		// NOT IMPLEMENTED: numbered-item
		
		// NOT IMPLEMENTED:  positive-result ; negative-result  Specifies different sets of picture items for positive 
		//and negative results. A zero value uses the positive picture. 
//		quads.add(new SwitchTestQuad("1234.56", "$#,##0.00;-$#,##0.00", "$1,234.56"));
//		quads.add(new SwitchTestQuad("-1234.56", "$#,##0.00;-$#,##0.00", "-$1,234.56"));
		
		// NOT IMPLEMENTED: positive-result ; negative-result ; zero-result  Specifies different sets of picture items for positive, 
		// negative, and zero results. 
//		quads.add(new SwitchTestQuad("1234.56", "$#,##0.00;-$#,##0.00;$0", "$1,234.56"));
//		quads.add(new SwitchTestQuad("-1234.56", "$#,##0.00;-$#,##0.00;$0", "-$1,234.56"));
//		quads.add(new SwitchTestQuad("0", "$#,##0.00;-$#,##0.00;$0", "$0"));

		// Although the spec says input must be a number, Word will parse stuff like:
		//quads.add(new SwitchTestQuad("€180,000.00 EUR", "00", "123"));
			// syntax error with =, but might work with DOCPROPERTY?
			 * */
		
	}
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
//		generateSampleDocx();
		
		NumericFormattingSwitchTests nfst = new NumericFormattingSwitchTests();
		nfst.testFormatting();
	}

	
	
}
