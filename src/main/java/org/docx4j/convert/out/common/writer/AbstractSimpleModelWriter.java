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
import org.docx4j.convert.out.common.ModelConverter;
import org.docx4j.model.AbstractSimpleModel;
import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractSimpleModelWriter<T> implements ModelConverter {
	protected String modelID = null;
	
	protected AbstractSimpleModelWriter(String modelID) {
		this.modelID = modelID;
	}

	@Override
	public String getID() {
		return modelID;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, Model model,
			TransformState state, Document doc) throws TransformerException {
	AbstractSimpleModel<T> simpleModel = (AbstractSimpleModel<T>)model;
		return toNode(context, 
				      simpleModel.getModelData(), simpleModel.getContent(),
				      state, doc);
	}

	protected abstract Node toNode(AbstractWmlConversionContext context, 
			T modelData, Node modelContent, 
			TransformState state, Document doc) throws TransformerException;

	@Override
	public TransformState createTransformState() {
		return null;
	}

}
