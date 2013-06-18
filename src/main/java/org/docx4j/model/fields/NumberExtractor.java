package org.docx4j.model.fields;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4jProperties;

/**
 * Extract a number from a string.
 * 
 * The number can have a decimal point.
 * 
 * 
 * @author jharrop
 *
 */
public class NumberExtractor {
	
	static String regex = "([\\+-]?(\\.\\d+))|" +  // optional +/-, then decimal place, then digits
			"([\\+-]?[0](\\.\\d+))|" +             // optional +/-, then 0, then decimal place, then digits
			"([\\+-]?(\\d+)(\\.\\d*)?)";   // optional +/-, then digit, then optional (decimal place, then zero or more digits)

	static String decimalSymbolFallback;
	static String groupingSeparator;

	static Pattern pattern = Pattern.compile(regex);
	
	static {
		
		decimalSymbolFallback = Docx4jProperties.getProperty("docx4j.Fields.Numbers.DecimalSymbol", ".");
		groupingSeparator = Docx4jProperties.getProperty("docx4j.Fields.Numbers.GroupingSeparator", ",");
	}
	
	private String decimalSymbol;
	
	public NumberExtractor(String decimalSymbol) {
		
		if (decimalSymbol==null) {
			this.decimalSymbol = decimalSymbolFallback;
		} else {
			this.decimalSymbol = decimalSymbol;
		}
		
	}
	
	/**
	 * Convert radix point to ".",
	 * and remove thousands separator,
	 * in preparation for our regex.
	 */
	private String prepare(String string) {
		
		string = string.replaceAll(groupingSeparator, "");
		
		if (decimalSymbol.equals(".")) {
			// do nothing
			return string;
		} else {
			return string.replace(decimalSymbol, ".");
		}
		
	}
	
	/**
	 * @param string
	 * @return
	 * @throws java.lang.IllegalStateException if no match
	 */
	public String extractNumber(String string) throws java.lang.IllegalStateException {

		try {
			Matcher makeMatch = pattern.matcher(
					prepare(string));
			makeMatch.find();
			return makeMatch.group();
		} catch (java.lang.IllegalStateException noMatch) {
			// This is what Word does
			return string;
		}
	}
	 
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		NumberExtractor nex = new NumberExtractor(".");
		System.out.println(nex.extractNumber("€ HEH EUR"));
		
	}

}
