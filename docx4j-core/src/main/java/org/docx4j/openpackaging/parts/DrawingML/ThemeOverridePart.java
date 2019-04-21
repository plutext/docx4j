/*
 *  Copyright 2013, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.DrawingML;

import javax.xml.bind.JAXBElement;

import org.docx4j.dml.CTBaseStylesOverride;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * You get one of these if you paste a chart from Excel 2010 into a Powerpoint 2010
 * slide, choosing to "keep source formatting".
 * 
 * To create JAXBElement<CTBaseStylesOverride>:
 * 
 *    org.docx4j.dml.ObjectFactory of = new org.docx4j.dml.ObjectFactory();
 *    org.docx4j.dml.CTBaseStylesOverride bso = of.createCTBaseStylesOverride();
 *    JAXBElement<CTBaseStylesOverride> themeOverride = of.createThemeOverride(bso);
 */
public final class ThemeOverridePart extends JaxbXmlPartXPathAware<JAXBElement<CTBaseStylesOverride>> {
	
	private static Logger log = LoggerFactory.getLogger(ThemeOverridePart.class);		
	
	public ThemeOverridePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ThemeOverridePart() throws InvalidFormatException {
		super(new PartName("/ppt/theme/themeOverride1.xml"));
		init();
	}
	
	
	public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_THEME_OVERRIDE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.THEME_OVERRIDE);
		
//		setJAXBContext(Context.jc);						
		
		
	}

	
}
