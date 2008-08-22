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


import javax.xml.bind.JAXBContext;

public class Context {
	
	public static JAXBContext jc;
	public static JAXBContext jcThemePart;
	public static JAXBContext jcDocPropsCore;
	public static JAXBContext jcDocPropsCustom;
	public static JAXBContext jcDocPropsExtended;
	public static JAXBContext jcXmlPackage;
	public static JAXBContext jcRelationships;
	
	static {
		
		try {		
			jc = JAXBContext.newInstance("org.docx4j.wml:org.docx4j.dml");
			jcThemePart = JAXBContext.newInstance("org.docx4j.dml");
			jcDocPropsCore = JAXBContext.newInstance("org.docx4j.docProps.core:org.docx4j.docProps.core.dc.elements:org.docx4j.docProps.core.dc.terms");
			jcDocPropsCustom = JAXBContext.newInstance("org.docx4j.docProps.custom");
			jcDocPropsExtended = JAXBContext.newInstance("org.docx4j.docProps.extended");
			jcXmlPackage = JAXBContext.newInstance("org.docx4j.xmlPackage");
			jcRelationships = JAXBContext.newInstance("org.docx4j.relationships");
		} catch (Exception ex) {
			ex.printStackTrace();
		}				
	}
		
}
