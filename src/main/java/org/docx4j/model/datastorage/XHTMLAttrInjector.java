package org.docx4j.model.datastorage;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XHTMLAttrInjector {
	
	public static Logger log = LoggerFactory.getLogger(XHTMLAttrInjector.class);		
	
	
	protected static String injectAttrs(String content, String classVal, String styleVal) throws ParserConfigurationException, SAXException, IOException {
		
		// Convert xhtml string to DOM
    	int firstChar = content.codePointAt(0);
    	if (firstChar==0xFEFF) {
    		log.info("Removing BOM..");
    		content = content.substring(1);
    	}	
    	
    	InputSource is = new InputSource();
    	is.setCharacterStream(new StringReader(content));

    	Document doc = XmlUtils.getNewDocumentBuilder().parse(is);    	
		
		// traverse + inject (need traverse, to get p in table)
    	injectAttrs(doc, classVal, styleVal);
		
		// convert back to string again
    	return XmlUtils.w3CDomNodeToString(doc);
	}
	
	public static void injectAttrs( Node sourceNode, String classVal, String styleVal ) {
			    	
    	log.debug("node type" + sourceNode.getNodeType());
    	
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
                    	injectAttrs((Node)nodes.item(i), classVal, styleVal );
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                
                // Do it...
            	Element el = (Element)sourceNode;
            	if (el.getLocalName()==null) {}
            	else if (el.getLocalName().equals("div")
            			|| el.getLocalName().equals("p")
            			|| el.getLocalName().equals("ol")
            			|| el.getLocalName().equals("ul")) {
            		            		
            		// don't do anything if @class or @style already present
            		if (el.getAttribute("class").trim().equals("")
            				&& el.getAttribute("style").trim().equals("") ) {
            		
            			// do it..
	            		if (classVal!=null) {
	            			el.setAttribute("class", classVal);
	            		}
	            		if (styleVal!=null) {
	            			el.setAttribute("style", styleVal);
	            		}
            		
            		}
//            	} else if (el.getLocalName().equals("li")) {
//
//            		if (el.getAttribute("class").trim().equals("")
//            				&& el.getAttribute("style").trim().equals("") ) {
//            			
//                		// Wrap its contents in a span
//
//                		// Clone contents
//                		List<Node> clonedChildren = new ArrayList<Node>();
//                		List<Node> deletions = new ArrayList<Node>();
//                        NodeList children = sourceNode.getChildNodes();
//                        if (children != null) {
//                            for (int i=0; i<children.getLength(); i++) {
//                            	clonedChildren.add(children.item(i).cloneNode(true));
//                            	deletions.add(children.item(i));
//                            }
//                        }
//                        
//                        // Delete contents
//                        for (Node del : deletions) {
//                        	el.removeChild(del);
//                        }
//                        
//                        // Add span
//                        Element newSpan = el.getOwnerDocument().createElement("span");
//                        el.appendChild(newSpan);
//	            		if (classVal!=null) {
//	            			newSpan.setAttribute("class", classVal);
//	            		}
//	            		if (styleVal!=null) {
//	            			newSpan.setAttribute("style", styleVal);
//	            		}
//	            		
//	            		// Now re-attach children
//                        for (Node newChild : clonedChildren) {
//                        	newSpan.appendChild(newChild);
//                        }
//            		
//            		}
            		
            	}

                // recurse on each child
                NodeList children = sourceNode.getChildNodes();
                if (children != null) {
                    for (int i=0; i<children.getLength(); i++) {
                    	injectAttrs((Node)children.item(i), classVal, styleVal );
                    }
                }

                break;

            case Node.TEXT_NODE:
            	
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
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		
//		String content = "<p/>";
		String content = "<div><p>p1</p><p>p2</p><table><tr><td><p>p1</p></td></tr></table></div>";
		
		
		
		System.out.println( injectAttrs(content, "foo", "bar") );

	}

}
