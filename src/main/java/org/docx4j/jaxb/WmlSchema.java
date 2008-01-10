/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.jaxb;

import javax.xml.bind.JAXBContext;

/**
 * @author jharrop
 *
 */
public class WmlSchema {

	public static javax.xml.validation.Schema schema;
	
	static {
		
		try {		
			javax.xml.validation.SchemaFactory sf = 
				javax.xml.validation.SchemaFactory.newInstance(
				      javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

			javax.xml.validation.Schema schema = sf.newSchema(new java.io.File("/home/jharrop/workspace200711/docx4j-001/src/main/resources/wml-local-subset.xsd"));			
		} catch (Exception ex) {
			ex.printStackTrace();
		}				
	}
	
	
	
}
