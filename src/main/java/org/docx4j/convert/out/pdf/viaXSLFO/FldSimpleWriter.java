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
package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.properties.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FldSimpleWriter extends AbstractFldSimpleWriter {
	protected static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	
	protected static class PageHandler implements FldSimpleNodeWriterHandler {
		@Override
		public String getName() { return "PAGE"; }
		@Override
		public int getProcessType() { return PROCESS_APPLY_STYLE; }

		@Override
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
			return doc.createElementNS(FO_NS, "fo:page-number");
		}
	}
	
	// NB see super class for definition of other handlers.
	
	protected FldSimpleWriter() {
		super(FO_NS, "fo:inline");
	}

	@Override
	protected void registerHandlers() {
		super.registerHandlers();
		registerHandler(new PageHandler());
	}

	@Override
	protected void applyProperties(List<Property> properties, Node node) {
		Conversion.applyFoAttributes(properties, (Element)node);
	}
}
