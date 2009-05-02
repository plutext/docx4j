/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j;


import org.apache.log4j.Logger;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.DOMWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Deprecated
public class Dom4jUtils {
	// Dom4J shouldn't be used in docx4j
	
	private static Logger log = Logger.getLogger(Dom4jUtils.class);	
		

 	public static void treeCopy( NodeList sourceNodes, org.dom4j.Element destParent ) { 		
        for (int i=0; i<sourceNodes.getLength(); i++) {
        	treeCopy((Node)sourceNodes.item(i), destParent);
        } 		
 	}
     
     
	public static void treeCopy( Node sourceNode, org.dom4j.Element destParent ) {
	    	
    	//log.debug("node type" + sourceNode.getNodeType());
    	
        switch (sourceNode.getNodeType() ) {

        	case Node.DOCUMENT_NODE: // type 9
        
                // recurse on each child
                NodeList nodes = sourceNode.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                    	//treeCopy((DTMNodeProxy)nodes.item(i), destParent);
                    	treeCopy((Node)nodes.item(i), destParent);
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                
                // Copy of the node itself
        		log.debug("copying: " + sourceNode.getNodeName() );
            	
        		Element newChild = null;
            	if (sourceNode.getNamespaceURI()==null) {
            		// eg p
            		newChild = destParent.addElement(sourceNode.getNodeName());
            	} else {
            	
            		newChild = destParent.addElement(sourceNode.getNodeName(), 
            			//sourceNode.getPrefix(),
            			sourceNode.getNamespaceURI());
            	//destParent.add(newChild);
            	}
            	
//        		Node newChild = destParent.getOwnerDocument().createElementNS(
//        				sourceNode.getNamespaceURI(), sourceNode.getLocalName() );                    
//        		destParent.appendChild(newChild);
        		
        		// .. its attributes
            	NamedNodeMap atts = sourceNode.getAttributes();
            	for (int i = 0 ; i < atts.getLength() ; i++ ) {
            		
            		Attr attr = (Attr)atts.item(i);
            		
//	                		log.debug("attr.getNamespaceURI(): " + attr.getNamespaceURI());
//	                		log.debug("attr.getLocalName(): " + attr.getLocalName());
            		
        			if (attr.getNamespaceURI()==null) {
        				newChild.addAttribute(attr.getLocalName(), attr.getValue());
        			} else 	if (attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) {
                		; // this is a namespace declaration. not our problem
        			} else 	if (attr.getLocalName() == null) {
        				log.debug("weird attribute in ns " + attr.getNamespaceURI() + " had no local name!!");
            		} else {
        				Namespace n = new Namespace(attr.getPrefix(), attr.getNamespaceURI());
        				QName attName = new QName(attr.getLocalName(), n);
        				newChild.addAttribute(attName, attr.getValue());
            		}
            	}

                // recurse on each child
                NodeList children = sourceNode.getChildNodes();
                if (children != null) {
                    for (int i=0; i<children.getLength(); i++) {
                    	//treeCopy( (DTMNodeProxy)children.item(i), newChild);
                    	treeCopy( (Node)children.item(i), newChild);
                    }
                }

                break;

            case Node.TEXT_NODE:
//            	Node textNode = destParent.getOwnerDocument().createTextNode(sourceNode.getNodeValue());       
//            	destParent.appendChild(textNode);
            	destParent.setText( sourceNode.getNodeValue() );
                break;

//                case Node.CDATA_SECTION_NODE:
//                    writer.write("<![CDATA[" +
//                                 node.getNodeValue() + "]]>");
//                    break;
//
//                case Node.COMMENT_NODE:
//                    writer.write(indentLevel + "<!-- " +
//                                 node.getNodeValue() + " -->");
//                    writer.write(lineSeparator);
//                    break;
//
//                case Node.PROCESSING_INSTRUCTION_NODE:
//                    writer.write("<?" + node.getNodeName() +
//                                 " " + node.getNodeValue() +
//                                 "?>");
//                    writer.write(lineSeparator);
//                    break;
//
//                case Node.ENTITY_REFERENCE_NODE:
//                    writer.write("&" + node.getNodeName() + ";");
//                    break;
//
//                case Node.DOCUMENT_TYPE_NODE:
//                    DocumentType docType = (DocumentType)node;
//                    writer.write("<!DOCTYPE " + docType.getName());
//                    if (docType.getPublicId() != null)  {
//                        System.out.print(" PUBLIC \"" +
//                            docType.getPublicId() + "\" ");
//                    } else {
//                        writer.write(" SYSTEM ");
//                    }
//                    writer.write("\"" + docType.getSystemId() + "\">");
//                    writer.write(lineSeparator);
//                    break;
            }
        }
	
	public static DocumentFragment asDocumentFragment(org.dom4j.Document dom4jDoc) {
		
		// Since it is expected that it is XSLT (Xalan) which is
		// receiving this DF, we don't actually care what
		// document the DF belongs to!
		
    	try {
    		
    		DOMWriter writer = new DOMWriter();
    		Document w3cDoc = writer.write(dom4jDoc);
    		
    		log.debug(XmlUtils.w3CDomNodeToString(w3cDoc));
    		
			DocumentFragment docfrag = w3cDoc.createDocumentFragment();
						
			XmlUtils.treeCopy((Node)w3cDoc, (Node)docfrag);
			return docfrag;
			
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e);
			return null;
		}
	}
	
	
}

