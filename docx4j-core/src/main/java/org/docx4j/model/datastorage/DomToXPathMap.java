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
    
    
    public Map<String, String> getPathMap() {
		return pathMap;
	}

	/**
     * count the number of child nodes; used for pre-calculation
     * of (1) repeat xpaths, and (2) certain simple conditions.
     * 
     * By default, an entry counts the number of children which
     * are the same element as the first element child, since this
     * is what we need for repeats.
     * 
     * If there are elements with different names, the count
     * is put in the map with PREFIX_ALL_NODES prefix.
     * 
     * @since 3.3.6
     */
    private Map<String, Integer> countMap = null; 
    
    public static final String PREFIX_ALL_NODES = "_all_";
    
    public Map<String, Integer> getCountMap() {
		return countMap;
	}

	public DomToXPathMap(Document document) {
    	this.document = document;
    }
    
    public void map() {

        histgrams.clear();
        histgrams.push(new Histgram());
        
        pathMap = new HashMap<String, String>(); 
        countMap = new HashMap<String, Integer>(); 
        
        walkTree(document);
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
            	
            	String nxpath = getXPath();

                // recurse on each child
                NodeList children = sourceNode.getChildNodes();
                int childrenLength = children.getLength();

                
                if (children == null 
                		|| childrenLength==0) {
                	
                	// Record the fact this is an empty leaf node
                	pathMap.put(nxpath, "");                	
                	countMap.put(nxpath, 0);
                } else {
                	String childName = null;
                	boolean singleChild = true; // do all children have the same name? true until proven otherwise
                	int actualCount=0;
                	int countOtherElements=0;
                	int countTextNodes = 0;
                    for (int i=0; i<childrenLength; i++) {
                    	
                    	// counting children for repeats and condition pre-calc
                    	if (children.item(i).getNodeType()==Node.TEXT_NODE) {
                    		countTextNodes++;
                    	} else if ( children.item(i).getNodeType()==Node.ELEMENT_NODE) {
                    		if (childName==null /* init*/) {
                    			childName = getLocalName((Node)children.item(i));
                    		}
                    		if (getLocalName((Node)children.item(i)).equals(childName)) {
                    			actualCount++;
                    		} else {
                    			singleChild = false; // count not useful for REPEAT purpose, but still useful for condition eval
                    			countOtherElements++;
                    		}
                    	}
                    	// Note we are ignoring comments and PIs here.  Putting those in your XML part is unsupported.
                    	
                    	walkTree( (Node)children.item(i));
                    }
                    if (singleChild) {
                    	// all children have the same name
                    	countMap.put(nxpath, actualCount);                    	
                    	if (log.isDebugEnabled()) {
                    		log.debug(nxpath + "= " + actualCount);
                    	}
                    } else {
                    	// store count for condition count( pre calculation;
                    	// it is convenient to store it in the same map
                    	
                    	// we don't do mixed content, so only count text nodes if
                    	// there is no element content (to avoid whitespace treated as significant as a text node between 2 real nodes and counting)
                    	// TODO: detect mixed content and warn?
                    	int val;
                    	if (actualCount + countOtherElements>0) {
                        	val = actualCount + countOtherElements;                    		
                    	} else {
                        	val = countTextNodes;                    		                    		
                    	}
                    	
                    	countMap.put(PREFIX_ALL_NODES + nxpath, val);  
                    	/* NB XPath spec says 
                    	 * 
                    	 *   The count function returns the number of nodes in the argument node-set.
                    	 *   
                    	 * which I suspect includes text nodes.  
                    	 */
                    	if (log.isDebugEnabled()) {
                    		log.debug(PREFIX_ALL_NODES + " {} : {} = {} + {} + {} ", nxpath, val,  actualCount, countOtherElements, countTextNodes );
                    	}
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
