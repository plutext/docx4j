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

import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Node;

/**
 * Interface to provide access to the data stored in a 
 * CustomXmlDataStoragePart.
 * 
 * This approach gives the user flexibility as to whether
 * they use eg JAXB or DOM or dom4j to represent
 * and manipulate their data. 
 */
public interface CustomXmlDataStorage {
	
	/**
	 * Create this object.
	 * 
	 * @return
	 */
	public CustomXmlDataStorage factory();
	
	/**
	 * Get the data pointed to by the xpath.
	 * (return what if it points at a non-existent??)
	 * 
	 * @param xpath
	 * @return
	 */
	public String xpathGetString(String xpath, String prefixMappings) throws Docx4JException;
		
	public List<Node> xpathGetNodes(String xpathString, String prefixMappings);
	
		
	/**
	 * Set the contents of this object from the input stream
	 * 
	 * @param is
	 * @return
	 * @throws Docx4JException
	 */
	public void setDocument( java.io.InputStream is ) throws Docx4JException;

	
	public void setDocument( org.w3c.dom.Document doc ) throws Docx4JException;
	
	
	/**
	 * Write the contents of this object to the output stream
	 * 
	 * @param os
	 * @throws Docx4JException
	 */
	public void writeDocument(java.io.OutputStream os) throws Docx4JException;	

	/**
	 * Write the contents of this object to the org.w3c.dom.Document
	 * 
	 * @param os
	 * @throws Docx4JException
	 */
	public org.w3c.dom.Document getDocument() throws Docx4JException;	

	
}
