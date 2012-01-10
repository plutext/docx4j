package org.docx4j.model.listnumbering;

import org.apache.log4j.Logger;

/**
 * This format is 01, 02, 03 etc.
 * Not sure what Word does after 99?
 *
 */
public class NumberFormatDecimalZero extends NumberFormat {
	
	protected static Logger log = Logger.getLogger(NumberFormatDecimalZero.class);
	
	public String format( int in ) {

		if (in<10) {
			return "0" + in;
		} else {
			return "" + in;
		}
	}
	
}
