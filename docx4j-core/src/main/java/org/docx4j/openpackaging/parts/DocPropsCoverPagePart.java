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
package org.docx4j.openpackaging.parts;

import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.docProps.coverPageProps.CoverPageProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


public class DocPropsCoverPagePart extends JaxbCustomXmlDataStoragePart<CoverPageProperties> {
	
	/*
		<CoverPageProperties xmlns="http://schemas.microsoft.com/office/2006/coverPageProps">
		  <PublishDate/>
		  <Abstract>This document is about ...</Abstract>
		  <CompanyAddress/>
		  <CompanyPhone/>
		  <CompanyFax/>
		  <CompanyEmail/>
		</CoverPageProperties>	
	 */
	
	private static Logger log = LoggerFactory.getLogger(DocPropsCoverPagePart.class);
	
	public DocPropsCoverPagePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	
	public void init() {		
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE));

		// Used when this Part is added to a rels 
		setRelationshipType("http://schemas.microsoft.com/‌office/‌2006/‌coverPageProps"); // hardcoding Namespaces.PROPERTIES_COVERPAGE for ease of integration in MergeDocx
		
		setJAXBContext(Context.jc);						
		
		
	}
	
}

	
