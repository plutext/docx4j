/*
 *  Copyright 2010, Plutext Pty Ltd.
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
package org.docx4j.convert.out.html;

import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public abstract class SdtTagHandler {
	
	private static Logger log = LoggerFactory.getLogger(SdtTagHandler.class);
	
	public abstract Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
			HashMap<String, String> tagMap,
			NodeIterator childResults) throws TransformerException;

	public abstract Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
			HashMap<String, String> tagMap,
			Node resultSoFar) throws TransformerException;
	

	protected Node attachContents(DocumentFragment docfrag, Node xhtmlDiv,
			Node resultSoFar) {
		
		XmlUtils.treeCopy(resultSoFar, xhtmlDiv);
		
		return docfrag;
		
	}
	
	protected SdtPr.Alias getAlias(SdtPr sdtPr) {
		
		for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
			
			o = XmlUtils.unwrap(o);
			if (o instanceof SdtPr.Alias) return (SdtPr.Alias)o; 
			
		}
		return null;
	}
	
	protected Node attachContents(DocumentFragment docfrag, Node xhtmlDiv,
			NodeIterator childResults) {

			// init
			Node n = childResults.nextNode();
			do {

				// getNumberXmlNode creates a span node, which is empty
				// if there is no numbering.
				// Let's get rid of any such <span/>.

				// What we actually get is a document node
				if (n.getNodeType() == Node.DOCUMENT_NODE) {
					log.debug("handling DOCUMENT_NODE");
					// Do just enough of the handling here
					NodeList nodes = n.getChildNodes();
					if (nodes != null) {
						for (int i = 0; i < nodes.getLength(); i++) {

							if (((Node) nodes.item(i)).getLocalName().equals(
									"span")
									&& !((Node) nodes.item(i)).hasChildNodes()) {
								// ignore
								log.debug(".. ignoring <span/> ");
							} else {
								XmlUtils.treeCopy((Node) nodes.item(i),
										xhtmlDiv);
							}
						}
					}
				} else {

					/*
					 * Node we'd like to import is of type
					 * org.apache.xml.dtm.ref.DTMNodeProxy which causes
					 * org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The
					 * implementation does not support the requested type of
					 * object or operation.
					 * 
					 * See
					 * http://osdir.com/ml/text.xml.xerces-j.devel/2004-04/msg00066
					 * .html
					 * 
					 * So instead of importNode, use
					 */
					XmlUtils.treeCopy(n, xhtmlDiv);
				}
				// next
				n = childResults.nextNode();

			} while (n != null);
			
			return docfrag;

	}
	
}
