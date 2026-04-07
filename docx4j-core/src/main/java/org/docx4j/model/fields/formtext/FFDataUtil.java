/**
 *  Copyright 2026, Plutext Pty Ltd.
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

 **/
package org.docx4j.model.fields.formtext;

import org.docx4j.XmlUtils;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.CTFFTextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

public class FFDataUtil {

	private static Logger log = LoggerFactory.getLogger(FFDataUtil.class);	
	
	/**
	 * Datafield name is expected to be in textInput/default. 
	 * @param ffData
	 */
	public static String getDatafieldNameFromFFData(CTFFData ffData) {

		// name element is empty, but we do have textInput/default 
		/*
		    <w:ffData>
		        <w:name w:val=""/>
		        <w:enabled/>
		        <w:calcOnExit w:val="false"/>
		        <w:textInput>
		            <w:default w:val="sellerFax"/>
		        </w:textInput>
		    </w:ffData>
		 */
		
		if (ffData==null) {
			log.debug("null w:ffData");
			return null;
		}
		
		for (JAXBElement<?> j : ffData.getNameOrEnabledOrCalcOnExit() ) {
//					System.out.println(j.getName().getLocalPart());
//					if (j.getName().getLocalPart().equals("name")) {
//						name = (CTFFName)XmlUtils.unwrap(j);						
//					}
			if (j.getName().getLocalPart().equals("textInput")) {
				CTFFTextInput textInput = (CTFFTextInput)XmlUtils.unwrap(j);
				CTFFTextInput.Default def = textInput.getDefault();
				if (def==null) {
					log.debug("w:textInput but no w:default");
					return null;
				} else {
					log.debug("w:ffData/w:default " + def.getVal());	
					return def.getVal();
				}
			}
		}
		
		log.debug("No w:textInput element");
		return null;
	}	
	
}
