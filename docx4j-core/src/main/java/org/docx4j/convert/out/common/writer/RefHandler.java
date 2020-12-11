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
package org.docx4j.convert.out.common.writer;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RefHandler implements AbstractFldSimpleWriter.FldSimpleNodeWriterHandler {
	protected int outputType = -1;
	
	public RefHandler(int outputType) {
		this.outputType = outputType;
	}
	
	@Override
	public String getName() { return "REF"; }

	@Override
	public int getProcessType() {
		return PROCESS_NONE;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
	Node ret = model.getContent();
	AbstractHyperlinkWriterModel hyperlinkModel = null;
		if (FormattingSwitchHelper.hasSwitch("\\h", model.getFldParameters())) {
			hyperlinkModel = new AbstractHyperlinkWriterModel();
			hyperlinkModel.build(context, model, model.getContent()); //the bookmark is the target, \h gets ignored
			ret = HyperlinkUtil.toNode(outputType, context, hyperlinkModel, model.getContent(), doc);
		}
		return ret;
	}
}