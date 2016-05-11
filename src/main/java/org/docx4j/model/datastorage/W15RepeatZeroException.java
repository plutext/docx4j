package org.docx4j.model.datastorage;

import org.docx4j.openpackaging.exceptions.Docx4JException;

/**
 * In the case where the item is to be repeated zero times
 * (ie the bound XML is empty), Word 2016 Preview leaves
 * the document surface unaltered (eg if there were 3 rows
 * there, it would leave those 3 rows).
 *
 */
public class W15RepeatZeroException extends Docx4JException {

	public W15RepeatZeroException(String msg) {
		super(msg);
	}
	
	public W15RepeatZeroException(String msg, Exception e) {
		super(msg, e);
	}

	public W15RepeatZeroException(String msg, Throwable t) {
		super(msg, t);
	}
	
}