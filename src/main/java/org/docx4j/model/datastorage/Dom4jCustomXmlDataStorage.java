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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(java.io.OutputStream)
	 */
	public void marshal(OutputStream os) throws Docx4JException {
		// Used when saving to zip file
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");			
			XMLWriter writer = new XMLWriter( os, format );
			writer.write( customXmlDom4jDoc );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Docx4JException("Error writing dom4j doc to OutputStream", e);
		} 
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(org.w3c.dom.Document)
	 */
	public void marshal(org.w3c.dom.Document doc) throws Docx4JException {
		// TODO Auto-generated method stub
		// Required to save to JCR
		
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#unmarshal(java.io.InputStream)
	 */
	public Object unmarshal(InputStream is) throws Docx4JException {
		// TODO Auto-generated method stub
		return null;
	}

	public void unmarshal(Document customXmlDom4jDoc)  {
		this.customXmlDom4jDoc = customXmlDom4jDoc;
	}
	
}
