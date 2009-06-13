package org.docx4j.model.datastorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * WARNING: most of this class is unimplemented,
 * since we are progressively removing use of dom4j
 * from docx4j.
 * 
 * All it can do right now is what it is supposed to
 * when the package is saved as a zip file (ie not JCR),
 * and not when the package is loaded.
 */
@Deprecated
public class Dom4jCustomXmlDataStorage implements CustomXmlDataStorage {
	
	Document customXmlDom4jDoc;	

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#factory()
	 */
	public CustomXmlDataStorage factory() {
		return new Dom4jCustomXmlDataStorage();
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#getXPath(java.lang.String)
	 */
	public String getXPath(String xpath, String prefixMappings) throws Docx4JException {
		// Not implemented yet
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(java.io.OutputStream)
	 */
	public void writeDocument(OutputStream os) throws Docx4JException {
		// Used when saving to zip file
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");			
			XMLWriter writer = new XMLWriter( os, format );
			writer.write( customXmlDom4jDoc );
		} catch (Exception e) {
			throw new Docx4JException("Error writing dom4j doc to OutputStream", e);
		} 
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(org.w3c.dom.Document)
	 */
	public org.w3c.dom.Document getDocument() throws Docx4JException {
		// Required to save to JCR
		// Not implemented yet
		throw new UnsupportedOperationException();		
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#unmarshal(java.io.InputStream)
	 */
	public void setDocument(InputStream is) throws Docx4JException {
		// Not implemented yet
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#unmarshal(java.io.InputStream)
	 */
	public void setDocument(org.w3c.dom.Document doc ) throws Docx4JException {
		// Not implemented yet
		throw new UnsupportedOperationException();
	}
	
	
	public void setDocument(Document customXmlDom4jDoc)  {
		this.customXmlDom4jDoc = customXmlDom4jDoc;
	}
	
	public void setNamespaceContext(String prefixMappings) throws Docx4JException {
		// Not implemented yet
		throw new UnsupportedOperationException();		
	}
	
	
}
