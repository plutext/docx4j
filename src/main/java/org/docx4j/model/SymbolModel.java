/*
 *  Copyright 2009, Plutext Pty Ltd.
 *   
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

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.wml.R.Sym;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SymbolModel extends Model {
	private final static Logger log = Logger.getLogger(SymbolModel.class);

	private Sym sym;
	/**
	 * @return the sym
	 */
	public Sym getSym() {
		return sym;
	}
	
	@Override
	public void build(Node node, NodeList children) throws TransformerException {

		try {
			sym = (Sym) XmlUtils.unmarshal(node);
		} catch (JAXBException e) {
			throw new TransformerException("Node: " + node.getNodeName() + "="
					+ node.getNodeValue(), e);
		}
		
	}

	@Override
	public Object toJAXB() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class SymbolModelTransformState implements TransformState { }

}
