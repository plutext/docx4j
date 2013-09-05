package org.pptx4j;

public class Pptx4jException extends Exception {

	public Pptx4jException(String msg) {
		super(msg);
	}
	
	public Pptx4jException(String msg, Exception e) {
		super(msg, e);
	}

	public Pptx4jException(String msg, Throwable t) {
		super(msg, t);
	}
	
}