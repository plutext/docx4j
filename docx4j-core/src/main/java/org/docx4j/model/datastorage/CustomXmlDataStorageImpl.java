/*
 *  Copyright 2009, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.model.datastorage;

import java.io.OutputStream;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.XmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomXmlDataStorageImpl extends XmlPart implements CustomXmlDataStorage {
	// Note: Most of the CustomXmlDataStorage implementation is actually in XmlPart
	
	// Note: this looks like it is a part, given that it extends XmlPart. 
	// However, it is actually a representation of the data in a 
	// org.docx4j.openpackaging.parts.CustomXmlDataStoragePart
	// We've done it this way so that users could represent their data
	// using JAXP, JAXB, or even dom4j.
	// It only extends XmlPart because the code there is a JAXP
	// representation.

	public CustomXmlDataStorageImpl() throws InvalidFormatException {
		super();
		// TODO Auto-generated constructor stub
	}

	private static Logger log = LoggerFactory.getLogger(CustomXmlDataStorageImpl.class);	
	
	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#factory()
	 */
	public CustomXmlDataStorage factory() {
		try {
			return new CustomXmlDataStorageImpl();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(java.io.OutputStream)
	 */
	public void writeDocument(OutputStream os) throws Docx4JException {
		// Used when saving to zip file
		/*
		 * With Crimson, this gives:
		 * 
			Exception in thread "main" java.lang.AbstractMethodError: org.apache.crimson.tree.XmlDocument.getXmlStandalone()Z
				at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(DOM2TO.java:373)
				at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:127)
				at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:94)
				at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transformIdentity(TransformerImpl.java:662)
				at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:708)
				at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:313)
				at org.docx4j.model.datastorage.CustomXmlDataStorageImpl.writeDocument(CustomXmlDataStorageImpl.java:174)
		 * 
		 */
		 try {
			DOMSource source = new DOMSource(doc);
			 XmlUtils.getTransformerFactory().newTransformer().transform(source, 
					 new StreamResult(os) );
		} catch (Exception e) {
			throw new Docx4JException("Problems saving to OutputStream", e);
		} 
		
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(org.w3c.dom.Document)
	 */
	public org.w3c.dom.Document getDocument() throws Docx4JException {
		// Used when saving to JCR
		return doc;
	}

	
//	public void setNamespaceContext(String prefixMappings) throws Docx4JException {		
//		xPath.setNamespaceContext(new XmlNamespaceContext(prefixMappings) );		
//	}
	
	
}
