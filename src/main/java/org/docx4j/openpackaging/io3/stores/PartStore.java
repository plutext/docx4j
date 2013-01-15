/*
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.openpackaging.io3.stores;

import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.XmlPart;

/**
 * Load/save parts from some storage system.
 * 
 * @author jharrop
 * @since 3.0
 */
public interface PartStore {
	
	/**
	 * The current content of a part will be as follows:
	 * 
	 * - for a JaxbXmlPart, whatever is in the
	 *   part store, unless
	 *   it has been unmarshalled (in which case
	 *   it is that part's JaxbXmlElement)
	 * 
	 * - for a BinaryPart, whatever is in the
	 *   part store, unless it has fetched (in which
	 *   case it is that part's bytebuffer)
	 *      
	 * - for any other part (including BinaryPart,
	 *   CustomXmlDataStoragePart, XmlPart), 
	 *   as contained in that part.
	 * 
	 */
	
	/**
	 * Set this if its different to the target part store
	 * (ie this object)
	 */
	public void setSourcePartStore(PartStore partStore);
		
	public InputStream loadPart(String partName) throws Docx4JException;
		
	public void setOutputStream(OutputStream os) throws Docx4JException;
	
	public void saveContentTypes(ContentTypeManager ctm) throws Docx4JException;
	
	public void saveJaxbXmlPart(JaxbXmlPart part) throws Docx4JException;
	
	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part) throws Docx4JException;	
	
	public void saveXmlPart(XmlPart part) throws Docx4JException;
	
	public void saveBinaryPart(Part part) throws Docx4JException;	
	
	public void finishSave() throws Docx4JException;

}
