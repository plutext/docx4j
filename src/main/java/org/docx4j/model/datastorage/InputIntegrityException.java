package org.docx4j.model.datastorage;

public class InputIntegrityException extends RuntimeException {

	public InputIntegrityException(String msg) {
		super(msg);
	}
	
	public InputIntegrityException(String msg, Exception e) {
		super(msg, e);
	}

//	public InputIntegrityException(String msg, Throwable t) {
//		super(msg, t);
//	}
	
}