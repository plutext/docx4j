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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** The Dummy FO Renderer doesn't render anything, it just outputs
 *  the fo document to the OutputStream. 
 * 
 */
public class FORendererDummy extends AbstractFORenderer { // implements FORenderer {
	
	protected static Logger log = LoggerFactory.getLogger(FORendererDummy.class);
	protected static final FORenderer INSTANCE = new FORendererDummy();
	
	public static FORenderer getInstance() {
		return INSTANCE;
	}
	
	//@Override
	public void render(String foDocument, FOSettings settings,
			boolean twoPass,
			List<SectionPageInformation> pageNumberInformation,
			OutputStream outputStream) throws Docx4JException {
		
		Writer writer = null;
		if (twoPass) {
			log.warn("Using the DummyFORenderer with a two pass conversion, there might be placeholders in the output");
		}
		try {
			writer = new OutputStreamWriter(outputStream, "UTF-8");
			writer.write(foDocument);
			writer.flush();
		} catch (Exception e) {
			throw new Docx4JException("Exception while storing fo document to OutputStream: " + e.getMessage(), e);
		}
	}


	
	
}
	

