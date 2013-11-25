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


package org.docx4j.samples;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBaseStylesOverride;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.io.Load;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;

/**
 * Determine how the root element of this part is represented.
 * 
 * @author Jason Harrop
 */
public class PartWhoAmI {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		String path_in = System.getProperty("user.dir") + "/themeOverride1.xml";
		
		
		Object o = XmlUtils.unmarshal(new FileInputStream(path_in));
		
		System.out.println(o.getClass().getName());
		
		if (o instanceof javax.xml.bind.JAXBElement) {
			System.out.println("Unwrapped " + ((JAXBElement)o).getDeclaredType().getName() );
			System.out.println("name: " + ((JAXBElement)o).getName() );
			o =  ((JAXBElement)o).getValue();
			System.out.println(o.getClass().getName());
		} 
				
	}
		
}
