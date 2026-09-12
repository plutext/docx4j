package org.docx4j.model.listnumbering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code w:numFmt decimalZero}: 01, 02, ... 09, 10, 11 (a single digit gets a
 * leading zero; nothing else changes).
 */
public class NumberFormatDecimalZero extends LabelFormatter {
	
	protected static Logger log = LoggerFactory.getLogger(NumberFormatDecimalZero.class);
	
	public String format( int in ) {

		if (in<10) {
			return "0" + in;
		} else {
			return "" + in;
		}
	}
	
}
