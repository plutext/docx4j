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

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public abstract class AbstractMessageWriter {

	protected static Logger log = Logger.getLogger(AbstractMessageWriter.class);
	
	public DocumentFragment notImplemented(NodeIterator nodes, String message) {

		Node n = nodes.nextNode();
		log.warn("NOT IMPLEMENTED: support for "+ n.getNodeName() + "; " + message);
		
		if (log.isDebugEnabled() ) {
			
			if (message==null) message="";
			
			log.debug( XmlUtils.w3CDomNodeToString(n)  );

			// Return something which will show up in the HTML
			return message("NOT IMPLEMENTED: support for " + n.getNodeName() + " - " + message);
		} else {
			
			// Put it in a comment node instead?
			
			return null;
		}
	}
	
	public DocumentFragment message(String message) {
		
		if (!log.isDebugEnabled()) return null;

		String documentFragment = getOutputPrefix() 
			+ message
			+ getOutputSuffix();  
		
		System.out.println(documentFragment);

		javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);
		StringReader reader = new StringReader(documentFragment);
		InputSource inputSource = new InputSource(reader);
		Document doc = null;
		try {
			doc = dbf.newDocumentBuilder().parse(inputSource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader.close();

		DocumentFragment docfrag = doc.createDocumentFragment();
		docfrag.appendChild(doc.getDocumentElement());
		return docfrag;		
	}

	protected abstract String getOutputPrefix();
	
	protected abstract String getOutputSuffix();
}
