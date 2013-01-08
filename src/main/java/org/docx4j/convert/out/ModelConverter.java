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
package org.docx4j.convert.out;

import javax.xml.transform.TransformerException;

import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Note that ModelConverter (aka Writers) must be 
 * registered in the corresponding instance (PDF, HTML...)
 * of the ModelRegistry
 */
public interface ModelConverter {
	/** Return the common ID of the Model/Converter/TransformState.  
	 * 
	 * @return
	 */
	public String getID();
	
	/** Generate the corresponding document fragment
	 * 
	 * @param context
	 * @param model
	 * @param state
	 * @param doc
	 * @return
	 * @throws TransformerException
	 */
	public Node toNode(AbstractWmlConversionContext context, Model model, TransformState state, Document doc) throws TransformerException;

	/** Create a new instance of the TransformState it uses.<br>
	 *  It may return <code>null</code>, then the passed value in toNode will be <code>null</code>.  
	 * 
	 * @return
	 */
	public TransformState createTransformState();
}
