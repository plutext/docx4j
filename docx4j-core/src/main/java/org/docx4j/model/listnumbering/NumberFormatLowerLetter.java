package org.docx4j.model.listnumbering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberFormatLowerLetter extends NumberFormat
{
	protected static Logger log = LoggerFactory.getLogger(NumberFormatLowerLetter.class);
	
	
	// from https://stackoverflow.com/questions/11969840/how-to-convert-a-base-10-number-to-alphabetic-like-ordered-list-in-html
	public String format( int num ) {

	    String result = "";
	    while (num > 0) {
	      num--; // 1 => a, not 0 => a
	      int remainder = num % 26;
	      char digit = (char) (remainder + 97);
	      result = digit + result;
	      num = (num - remainder) / 26;
	    }

	    return result;
	  }
}
