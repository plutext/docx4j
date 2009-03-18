package org.docx4j.convert.out;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public interface Output {
		
	public abstract void output(javax.xml.transform.Result result) throws Docx4JException;

}
