/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.fo.renderers;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FORenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFORenderer implements FORenderer {
	
	protected static Logger log = LoggerFactory.getLogger(AbstractFORenderer.class);
	
	// TODO this needs to be rethought in an architectural sense,
	// since PictWriter is here altering the behaviour of 
	// downstream renderer, and we don't want that!
	public boolean TEXTBOX_POSTPROCESSING_REQUIRED = false;
	
	static Templates xslt_POSTPROCESSING;			
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/convert/out/fo/renderers/AbstractFORenderer_POSTPROCESSING.xslt"));
			xslt_POSTPROCESSING = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
	}	
	

}