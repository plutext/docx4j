package org.docx4j.model.datastorage;

import java.io.InputStream;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public interface DocxFetcher {
	
	public InputStream getDocxFromIRI(String iri) throws Docx4JException;	

}
