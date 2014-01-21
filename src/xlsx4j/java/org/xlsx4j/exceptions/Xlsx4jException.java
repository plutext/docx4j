package org.xlsx4j.exceptions;

/**
 * @author jharrop
 * @since 3.0.1
 */
public class Xlsx4jException  extends Exception {

	public Xlsx4jException(String msg) {
		super(msg);
	}
	
	public Xlsx4jException(String msg, Exception e) {
		super(msg, e);
	}

	public Xlsx4jException(String msg, Throwable t) {
		super(msg, t);
	}
	
}