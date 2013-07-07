package org.docx4j.model.listnumbering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This format is 01, 02, 03 etc.
 * Not sure what Word does after 99?
 *
 */
public class NumberFormatDecimalZero extends NumberFormat {
	
	protected static Logger log = LoggerFactory.getLogger(NumberFormatDecimalZero.class);
	
	public String format( int in ) {

		if (in<10) {
			return "0" + in;
		} else {
			return "" + in;
		}
	}
	
}
