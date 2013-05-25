/*
 *  Copyright 2013, Plutext Pty Ltd.
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

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.wml.P;
import org.w3c.dom.Node;

/**
 * @author jharrop
 * @since 3.0.0
 */
public class HyperlinkModel extends Model {
	
	public static final String MODEL_ID = "w:hyperlink";
	
	private final static Logger log = Logger.getLogger(HyperlinkModel.class);

	private P.Hyperlink hyperlink;
	/**
	 * @return the hyperlink
	 */
	public P.Hyperlink getHyperlink() {
		return hyperlink;
	}
	
	Node content;
	public Node getContent() {
		return content;
	}
	
	@Override
	public void build(Object unmarshalledNode, Node content) throws TransformerException {

		try {
			hyperlink = (P.Hyperlink)unmarshalledNode;
			this.content = content;
			
		} catch (ClassCastException e) {
			throw new TransformerException("Node is not of the type Hyperlink it is " + unmarshalledNode.getClass().getName());
		}
		
	}

	@Override
	public Object toJAXB() {
		// TODO Auto-generated method stub
		return null;
	}

}
