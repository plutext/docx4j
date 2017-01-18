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

    private String getLocalName(Node sourceNode) {
    	
    	if (sourceNode.getLocalName()==null) {
    		// eg element was created using createElement() 
    		return sourceNode.getNodeName();
    	
    	} else {
    		return sourceNode.getLocalName();
    	}
    	
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
            	
            	try {
	                histgrams.peek().update(
	                		sourceNode.getNamespaceURI(),
	                		getLocalName(sourceNode),
	                		/* qname */ sourceNode.getNodeName() );
	                histgrams.push(new Histgram());
	                
            	} catch (java.lang.IllegalArgumentException iae) {
            	
            		log.error(sourceNode.getClass().getName());
            		log.error("sourceNode.getNodeName(): " + sourceNode.getNodeName());
            		log.error("sourceNode.getNamespaceURI(): " + sourceNode.getNamespaceURI());
            		log.error("sourceNode.getLocalName(): " + sourceNode.getLocalName());
            		log.error("sourceNode.getPrefix(): " + sourceNode.getPrefix());
            		log.error("java.vendor="+System.getProperty("java.vendor"));
            		log.error("java.version="+System.getProperty("java.version"));
            		
            		throw iae;            		
            	}
            	

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
