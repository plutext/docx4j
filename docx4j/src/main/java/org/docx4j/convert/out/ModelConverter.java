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
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Node;

/**
 * Note that ModelConverter (aka Writers) must be 
 * registered with eg viaXSLFO.Conversion;
 * (further, the models themselves must be 
 *  put in the modelClasses hashmap in
 *  convert.out.Converter.java, either directly,
 *  or by using Converter.getInstance().registerModelConverter)
 */
public abstract class ModelConverter {

	protected WordprocessingMLPackage wordMLPackage;
	public void setWordMLPackage(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}
	/**
	 * @return the wordMLPackage
	 */
	public WordprocessingMLPackage getWordMLPackage() {
		return wordMLPackage;
	}

	public void start() {
	}

	public void stop() {
	}

	private Converter mainConverter;

	public void setMainConverter(Converter c) {
		mainConverter = c;
	}

	public Converter getMainConverter() {
		return mainConverter;
	}

	public abstract Node toNode(Model m, TransformState state) throws TransformerException;


}
