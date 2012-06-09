/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.model;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.Converter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Model {
	
	protected WordprocessingMLPackage wordMLPackage;
	public void setWordMLPackage(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}
	
	/**
	 * Build the model from a DOM node.
	 * This is useful if the model is being built via
	 * XSLT, for the purposes of conversion to some other format. 
	 * 
	 * It is assumed the children of the structure being transformed
	 * have already been transformed, so they can be attached to
	 * the resulting structure and returned as-is. 
	 * 
	 * @param node
	 * @param children 
	 * @throws TransformerException
	 */
	public abstract void build(Node node,  NodeList children  ) 
		throws TransformerException;
	
	/**
	 * Generate a JAXB OpenXML object from this structure.
	 * 
	 * For conversion to other formats (eg HTML, XSL FO), refer to
	 * the ModelConverter implementations.
	 */
	public abstract Object toJAXB();
	
}
