package org.docx4j.model.listnumbering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberFormatLowerLetter extends NumberFormat
{
	protected static Logger log = LoggerFactory.getLogger(NumberFormatLowerLetter.class);
	
	public String format( int in )
	{
		char result = Character.forDigit(in + 9, 36);
		log.debug(in + " --> " + result);
		
		return Character.toString(result);		
	}
}
