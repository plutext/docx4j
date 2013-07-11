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

package org.docx4j.openpackaging.parts.WordprocessingML;

import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.w14.CTDefaultImageDpi;
import org.docx4j.w14.CTLongHexNumber;
import org.docx4j.w14.CTOnOff;
import org.docx4j.w15.CTGuid;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTSettings;



public final class DocumentSettingsPart extends JaxbXmlPartXPathAware<CTSettings> { 
	
	private final static Logger log = LoggerFactory.getLogger(DocumentSettingsPart.class);
	
	// This unmarshalls as a JAXBElement; so we override getJaxbElement()
	
	public DocumentSettingsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DocumentSettingsPart() throws InvalidFormatException {
		super(new PartName("/word/settings.xml"));
		init();
	}
	
	public void init() {		
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_SETTINGS));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SETTINGS);
				
	}
		
	@Override
	protected void setMceIgnorable() {

		boolean needW14 = false;
		if (this.jaxbElement.getDocId14()!=null) {
			needW14 = true;
		} else if (this.jaxbElement.getConflictMode() !=null) {
			needW14 = true;
		} else if (this.jaxbElement.getDiscardImageEditingData() !=null) {
			needW14 = true;
		} else if (this.jaxbElement.getDefaultImageDpi() !=null) {
			needW14 = true;
		}
		
		boolean needW15 = false;		
		if (this.jaxbElement.getChartTrackingRefBased()!=null) {
			needW15 = true;
		} else if (this.jaxbElement.getDocId15() !=null) {
			needW15 = true;
		}
		
		String mceIgnorableVal = "";
		if (needW14) {
			mceIgnorableVal = "w14";
		}
		
		if (needW15) {
			mceIgnorableVal += " w15";
		} 
		log.warn(mceIgnorableVal);
		
		this.jaxbElement.setIgnorable(mceIgnorableVal);
    }

}
