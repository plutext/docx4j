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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractHyperlinkWriter extends AbstractSimpleWriter implements AbstractFldSimpleWriter.FldSimpleNodeWriterHandler {
	public static final String WRITER_ID = "w:hyperlink";
	
	protected static final String HYPERLINK_NAME = "HYPERLINK";

	protected AbstractHyperlinkWriter() {
		super(WRITER_ID);
	}
	
//ModelConverter	
	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, Node content, TransformState state, Document doc) throws TransformerException {
	AbstractHyperlinkWriterModel hyperlinkModel = new AbstractHyperlinkWriterModel();
	Node ret = null;
		hyperlinkModel.build(context, unmarshalledNode, content);
		ret = toNode(context, hyperlinkModel, doc);
		return ret;
	}

	protected abstract Node toNode(AbstractWmlConversionContext context, AbstractHyperlinkWriterModel model, Document doc) throws TransformerException;

	
//AbstractFldSimpleWriter.FldSimpleNodeWriterHandler	
	@Override
	public String getName() {
		return HYPERLINK_NAME;
	}

	@Override
	public int getProcessType() {
		return PROCESS_NONE;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
	AbstractHyperlinkWriterModel hyperlinkModel = new AbstractHyperlinkWriterModel();
		hyperlinkModel.build(context, model, model.getContent());
		return toNode(context, hyperlinkModel, doc);
	}

}
