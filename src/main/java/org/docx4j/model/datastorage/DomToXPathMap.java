package org.docx4j.model.datastorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.docx4j.model.datastorage.xpathtracker.Histgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomToXPathMap {
	
	private static Logger log = LoggerFactory.getLogger(DomToXPathMap.class);	
		
    private final Stack<Histgram> histgrams = new Stack<Histgram>();

    private Document document;
    
    private Map<String, String> pathMap = null; 
    
    public DomToXPathMap(Document document) {
    	this.document = document;
    }
    
    public Map<String, String> map() {

        histgrams.clear();
        histgrams.push(new Histgram());
        
        pathMap = new HashMap<String, String>(); 
        
        walkTree(document);
        
        return pathMap;
    }

	
	public void walkTree( Node sourceNode ) {
			    	
//    	log.debug("node type" + sourceNode.getNodeType());
    	
        switch (sourceNode.getNodeType() ) {

	    	case Node.DOCUMENT_NODE: // type 9
        	case Node.DOCUMENT_FRAGMENT_NODE: // type 11
        
//        		log.debug("DOCUMENT:" + w3CDomNodeToString(sourceNode) );
//        		if (sourceNode.getChildNodes().getLength()==0) {
//        			log.debug("..no children!");
//        		}
        		
                // recurse on each child
                NodeList nodes = sourceNode.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                    	log.debug("child " + i + "of DOCUMENT_NODE");
                    	//treeCopy((DTMNodeProxy)nodes.item(i), destParent);
                    	walkTree((Node)nodes.item(i));
                    }
                }
                break;
            case Node.ELEMENT_NODE:
            	
                histgrams.peek().update(
                		sourceNode.getNamespaceURI(),
                		sourceNode.getLocalName(),
                		/* qname */ sourceNode.getNodeName() );
                histgrams.push(new Histgram());
            	
//        		// .. its attributes
//            	NamedNodeMap atts = sourceNode.getAttributes();
//            	for (int i = 0 ; i < atts.getLength() ; i++ ) {
//            		
//            		Attr attr = (Attr)atts.item(i);
//
////            		log.debug("attr.getNodeName(): " + attr.getNodeName());
////            		log.debug("attr.getNamespaceURI(): " + attr.getNamespaceURI());
////            		log.debug("attr.getLocalName(): " + attr.getLocalName());
////            		log.debug("attr.getPrefix(): " + attr.getPrefix());
//            		
//            		if ( attr.getNodeName().startsWith("xmlns:")) {
//            			/* A document created from a dom4j document using dom4j 1.6.1's io.domWriter
//                			does this ?!
//                			attr.getNodeName(): xmlns:w 
//                			attr.getNamespaceURI(): null
//                			attr.getLocalName(): null
//                			attr.getPrefix(): null
//                			
//                			unless i'm doing something wrong, this is another reason to
//                			remove use of dom4j from docx4j
//            			*/ 
//                		; 
//                		// this is a namespace declaration. not our problem
//            		} else if (attr.getNamespaceURI()==null) {
//                		//log.debug("attr.getLocalName(): " + attr.getLocalName() + "=" + attr.getValue());
//            			((org.w3c.dom.Element)newChild).setAttribute(
//                				attr.getName(), attr.getValue() );
//            		} else if ( attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) {
//                		; // this is a namespace declaration. not our problem
//            		} else if ( attr.getNodeName()!=null ) {
//            				// && attr.getNodeName().equals("xml:space")) {
//            				// restrict this fix to xml:space only, if necessary
//
//            			// Necessary when invoked from BindingTraverserXSLT,
//            			// com.sun.org.apache.xerces.internal.dom.AttrNSImpl
//            			// otherwise it was becoming w:space="preserve"!
//            			
//						/* eg xml:space
//						 * 
//							attr.getNodeName(): xml:space
//							attr.getNamespaceURI(): http://www.w3.org/XML/1998/namespace
//							attr.getLocalName(): space
//							attr.getPrefix(): xml
//						 */
//            			
//                		((org.w3c.dom.Element)newChild).setAttributeNS(attr.getNamespaceURI(), 
//                				attr.getNodeName(), attr.getValue() );	                			
//            		} else  {
//                		((org.w3c.dom.Element)newChild).setAttributeNS(attr.getNamespaceURI(), 
//                				attr.getLocalName(), attr.getValue() );	                			
//            		}
//            	}

                // recurse on each child
                NodeList children = sourceNode.getChildNodes();
                
                if (children == null 
                		|| children.getLength()==0) {
                	
                	// Record the fact this is an empty leaf node
                	String xpath = getXPath();
                	pathMap.put(xpath, "");                	
                	
                } else {
                    for (int i=0; i<children.getLength(); i++) {
                    	walkTree( (Node)children.item(i));
                    }
                }
                
                histgrams.pop();
                

                break;

            case Node.TEXT_NODE:
            	
            	// better than doing getTextContent() at the element level??
            	            	
            	String xpath = getXPath();
            	String existing = pathMap.get(xpath);
            	if (existing==null) {
//            		if (sourceNode.getNodeValue().endsWith("\n")
//            				|| sourceNode.getNodeValue().endsWith("\r")) {
            			pathMap.put(xpath, sourceNode.getNodeValue());  // some whitespace is significant
            	} else {
            		// Happens a lot
            		//log.debug("concat..");
            		pathMap.put(xpath, existing + sourceNode.getNodeValue());
            		
            	}
//            	log.debug("Put " + xpath + "=" + sourceNode.getNodeValue());
            	
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
	
    /**
     * Gets the XPath to the current element.
     */
    public String getXPath() {
        StringBuilder buf = new StringBuilder();
        for (Histgram h : histgrams) {
            h.appendPath(buf);
        }
        return buf.toString();
    }
    
}
