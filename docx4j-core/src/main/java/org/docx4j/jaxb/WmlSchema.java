/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.jaxb;


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
