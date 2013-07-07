package org.docx4j.model.listnumbering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberFormatLowerLetter extends NumberFormat {
	
	protected static Logger log = LoggerFactory.getLogger(NumberFormatLowerLetter.class);
	
	public String format( int in ) {
		
		String str = Integer.toString(in);
		StringBuilder out = new StringBuilder();
		
		for (int i = 0; i < str.length(); i++) {
			int dig = Character.digit(str.charAt(i), 36);
			char cdig = Character.forDigit(dig + 9, 36);
			log.debug(str.charAt(i) + " --> " + cdig);
			out.append(cdig);
		}		
		return out.toString();		
	}
	
	  public static void main (String [] args)
	   {
		   
		  NumberFormatLowerLetter format = new NumberFormatLowerLetter();
		  
		  format.format(1);
		  format.format(2);
		  log.debug(format.format(3));
		  
		  
		  
	   }

}
