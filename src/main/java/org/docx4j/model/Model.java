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
package org.docx4j.model;

import javax.xml.transform.TransformerException;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.w3c.dom.Node;

public abstract class Model {
	
	private WordprocessingMLPackage wordMLPackage = null;
	private Part currentPart = null;
	
	public void setup(WordprocessingMLPackage wordMLPackage, Part currentPart) {
		this.wordMLPackage = wordMLPackage;
		this.currentPart = currentPart;
	}
	
	public WordprocessingMLPackage getWordMLPackage() {
		return wordMLPackage;
	}
	
	protected Part getCurrentPart() {
		return currentPart;
	}
	
	/**
	 * Build the model from the unmarshalled DOM node.
	 * This is useful if the model is being built via
	 * XSLT, for the purposes of conversion to some other format. 
	 * 
	 * It is assumed the content of the structure being transformed
	 * have already been transformed, so they can be attached to
	 * the resulting structure and returned as-is. 
	 * 
	 * @param node the unmarshalled DOM node.
	 * @param content 
	 * @throws TransformerException
	 */
	public abstract void build(Object node,  Node content  ) 
		throws TransformerException;
	
	/**
	 * Generate a JAXB OpenXML object from this structure.
	 * 
	 * For conversion to other formats (eg HTML, XSL FO), refer to
	 * the ModelConverter implementations.
	 */
	public abstract Object toJAXB();
	
}
