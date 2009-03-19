package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.xmlPackage.XmlPackage;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.apache.xml.dtm.ref.DTMNodeProxy;

public class Conversion extends org.docx4j.convert.out.pdf.PdfConversion {
	
	public Conversion(WordprocessingMLPackage wordMLPackage) {
		super(wordMLPackage);
	}
	
	// Get the xslt file - Works in Eclipse - note absence of leading '/'
	static Source xslt;
	
	static {
		try {
			xslt = new StreamSource(
					org.docx4j.utils.ResourceUtils.getResource(
							"org/docx4j/convert/out/pdf/viaXSLFO/docx2fo.xslt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// TODO resolve WARN  [fop.apps.FOUserAgent] Glyph "Č" (0x10c, Ccaron) 
	// not available in font "Helvetica".

	

	/** Create a pdf version of the document, using XSL FO. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * 
	 * */     
    public void output(OutputStream os) throws Docx4JException {
    	
    	// See http://xmlgraphics.apache.org/fop/0.95/embedding.html
    	// (reuse if you plan to render multiple documents!)
    	FopFactory fopFactory = FopFactory.newInstance();
    	
    	try {
    	  Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, os);
    	  Document domDoc = XmlPackage.getFlatDomDocument(wordMLPackage);	

    	  // Resulting SAX events (the generated FO) must be piped through to FOP
    	  Result result = new SAXResult(fop.getDefaultHandler());
    	   
    	  // Uncomment this line to see the generated formatting objects
//  		Result result =
//			new javax.xml.transform.stream.StreamResult(System.out);		
    	  
    	  java.util.HashMap<String, Object> settings = new java.util.HashMap<String, Object>();
			settings.put("wmlPackage", wordMLPackage);
    	  
    	  XmlUtils.transform(domDoc, xslt, settings, result);
    	  
    	} catch (Exception e) {
    		throw new Docx4JException("FOP issues", e);
    	} finally {
    	  //Clean-up
    	  try {
			os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}		
		
	}
	
    public static DocumentFragment createBlockForPPr( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults ) {
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

    	
    	log.info("style '" + pStyleVal );    	
    	log.info("pPrNode:" + pPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        try {
        	
        	// Get the pPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
			Unmarshaller u = Context.jc.createUnmarshaller();			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			Object jaxb = u.unmarshal(pPrNodeIt.nextNode());
			
			PPr pPr = null;
			try {
				pPr =  (PPr)jaxb;
			} catch (ClassCastException e) {
		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
			}        	
        	
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			log.info("Document: " + document.getClass().getName() );

			Node foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");			
			document.appendChild(foBlockElement);
			
			if (pPr==null) {
				Text err = document.createTextNode( "Couldn't cast " + jaxb.getClass().getName() + " to PPr!" );
				foBlockElement.appendChild(err);
				
			} else {
			       
				if ( pPr.getJc()!=null) {				
					((Element)foBlockElement).setAttribute("text-align", 
							pPr.getJc().getVal().value() );
				}
				// TODO - other pPr props.
				
				// Our fo:block wraps whatever result tree fragment
				// our style sheet produced when it applied-templates
				// to the child nodes
				Node n = childResults.nextNode();
				
//				log.info("Node we are importing: " + n.getClass().getName() );
//				foBlockElement.appendChild(
//						document.importNode(n, true) );
				/*
				 * Node we'd like to import is of type org.apache.xml.dtm.ref.DTMNodeProxy
				 * which causes
				 * org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation.
				 * 
				 * See http://osdir.com/ml/text.xml.xerces-j.devel/2004-04/msg00066.html
				 * 
				 * So instead of importNode, use 
				 */
				treeCopy( (DTMNodeProxy)n,  foBlockElement );
			
			}
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
    }

    public static DocumentFragment createBlockForRPr( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults ) {
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

    	
    	log.info("pPrNode:" + rPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        try {
        	
        	// Get the pPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
			Unmarshaller u = Context.jc.createUnmarshaller();			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			Object jaxb = u.unmarshal(rPrNodeIt.nextNode());
			
			RPr rPr = null;
			try {
				rPr =  (RPr)jaxb;
			} catch (ClassCastException e) {
		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
			}        	
        	
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			log.info("Document: " + document.getClass().getName() );

			Node foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");			
			document.appendChild(foBlockElement);
			
			if (rPr==null) {
				Text err = document.createTextNode( "Couldn't cast " + jaxb.getClass().getName() + " to PPr!" );
				foBlockElement.appendChild(err);
				
			} else {
			    
				// bold
				
				if ( rPr.getB()!=null ) {				
					((Element)foBlockElement).setAttribute("font-weight", 
							"bold" );
				}
				// TODO - other rPr props.
				
				// Our fo:block wraps whatever result tree fragment
				// our style sheet produced when it applied-templates
				// to the child nodes
				Node n = childResults.nextNode();
				treeCopy( (DTMNodeProxy)n,  foBlockElement );			
			}
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
    }
    
    
    private static void treeCopy( org.apache.xml.dtm.ref.DTMNodeProxy sourceNode, Node destParent ) {
    	
    	log.debug("node type" + sourceNode.getNodeType());
    	
            switch (sourceNode.getNodeType() ) {

            	case Node.DOCUMENT_NODE: // type 9
            
                    // recurse on each child
                    NodeList nodes = sourceNode.getChildNodes();
                    if (nodes != null) {
                        for (int i=0; i<nodes.getLength(); i++) {
                        	treeCopy((DTMNodeProxy)nodes.item(i), destParent);
                        }
                    }
                    break;
                case Node.ELEMENT_NODE:
                    
                    // Copy of the node itself
            		Node newChild = destParent.getOwnerDocument().createElementNS(
            				sourceNode.getNamespaceURI(), sourceNode.getLocalName() );                    
            		destParent.appendChild(newChild);
            		
            		// .. its attributes
                	NamedNodeMap atts = sourceNode.getAttributes();
                	for (int i = 0 ; i < atts.getLength() ; i++ ) {
                		
                		Attr attr = (Attr)atts.item(i);
                		
                		((Element)newChild).setAttributeNS(attr.getNamespaceURI(), 
                				attr.getLocalName(), attr.getValue() );
                		    		
                	}

                    // recurse on each child
                    NodeList children = sourceNode.getChildNodes();
                    if (children != null) {
                        for (int i=0; i<children.getLength(); i++) {
                        	treeCopy( (DTMNodeProxy)children.item(i), newChild);
                        }
                    }

                    break;

                case Node.TEXT_NODE:
                	Node textNode = destParent.getOwnerDocument().createTextNode(sourceNode.getNodeValue());       
                	destParent.appendChild(textNode);
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
    	
    }
    
