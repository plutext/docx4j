/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4J.

    Docx4J is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4J.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4j.document.wordprocessingml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;



/**
 *
 * @author Jason Harrop
 */
public final class StructuredDocumentTag {

	private static Logger log = Logger.getLogger(StructuredDocumentTag.class);	
	
	
	/*
	 * 
	 *<w:sdt>
        <w:sdtPr>
          <w:id w:val="417087989"/>
          <w:placeholder>
            <w:docPart w:val="2AC728E2BA4F4E608A865EC25F64AC3D"/>
          </w:placeholder>
        </w:sdtPr>
        <w:sdtContent>
          <w:p w:rsidR="00B46EA8" w:rsidRPr="00B46EA8" w:rsidRDefault="00B46EA8" w:rsidP="00B46EA8">
          :
	 */


	// Note well that this doc is NOT the sdt doc.
	// That is only created on the fly when required!
	protected Document sdtContent; // eg a sequence of paragraphs - we don't need to parse this
	// TODO - re-implement that as a List of block level elements?

	protected SdtProperties sdtPr;

	
	public StructuredDocumentTag() {
					
		sdtContent = DocumentHelper.createDocument();
		Namespace nsWordprocessingML = new Namespace("w",
				"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		Element sdtContentRoot = sdtContent.addElement(new QName("sdtContent",
				nsWordprocessingML));
	
		// and we create a new sdtPr
		sdtPr = new SdtProperties();
		
	}
	
	/* Construct an SDT.  If the incoming Document 
	 * already has an SDT has its root element, use that ID, locking etc;
	 * otherwise, generate new ID etc. */
	public StructuredDocumentTag( String source ) {
		
		// Convert the string to a document
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(source);			
		} catch (DocumentException e) {
			log.error("DocumentException", e); 
			e.printStackTrace() ;
			//throw e;
		}
		construct(doc.getRootElement());
		
		log.info("Constructed!");
		
	}
	
	/* Construct and SDT.  If the incoming Document 
	 * already has an SDT has its root element, use that ID, locking etc;
	 * otherwise, generate new ID etc. */
	public StructuredDocumentTag( Document source ) {
		construct(source.getRootElement());
	}

	public StructuredDocumentTag( Element e ) {
		construct(e);
	}
	
	
	/* Construct and SDT.  If the incoming Document 
	 * already has an SDT has its root element, use that ID, locking etc;
	 * otherwise, generate new ID etc. */
	private void construct( Element root  ) {
		
		//Element root = source.getRootElement();

		sdtContent = DocumentHelper.createDocument();
		Namespace nsWordprocessingML = new Namespace("w",
				"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		Element sdtContentRoot = sdtContent.addElement(new QName("sdtContent",
				nsWordprocessingML));
		List bodyChildren = sdtContentRoot.content();			
		
		if (root.getName()=="sdt") {
			log.debug("This is already an sdt");
			
			// parse the SdtProperties
			sdtPr = new SdtProperties(root.element("sdtPr") );
						
			// parse the SdtContent
			for (Object o2 : root.element("sdtContent").content() ) {
				// content() gets everything eg inc Namespace,
				// as opposed to element()
				if (o2 instanceof Element) {
					bodyChildren.add( ((Element)o2).createCopy() );
					// fails silently if you createCopy() on a Namespace
					// object cast to an Element!!
				} else  {
					//log.info("adding: " + o2.getClass().getName() );
					bodyChildren.add( o2);
				}
			}		
			
		} else {
			log.debug("This is not an sdt yet ..");
			
			for (Object o2 : root.content() ) {
				if (o2 instanceof Element) {
					bodyChildren.add( ((Element)o2).createCopy() );
				} else  {
					log.debug("adding: " + o2.getClass().getName() );
					bodyChildren.add( o2);
				}
			}		
		
			// and we create a new sdtPr
			sdtPr = new SdtProperties();			
		}				
		
	}
	
	public String getId() {
		return sdtPr.getId();
	}

	public void setId(String id) {
		sdtPr.setId(id);
	}
	
	public void setTag(long tag) {
		sdtPr.setTag(tag);
	}
	public long getTag() {
		return sdtPr.getTag();
	}
	
	public Document getSdtContent() {
		return sdtContent;
	}
	
	public void addElementToSdtContent( Element e) {
		sdtContent.getRootElement().add( e.createCopy() );
	}
	
	public void addNamespacesToSdtContent( Map namespaceMap) {
		
		Iterator i = namespaceMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			log.info("Adding namespace " + (String)e.getKey());
			sdtContent.getRootElement().add( (Namespace)e.getValue() );
		} 					
		
	}
	
	
	public Document getDocument() {
		Document sdtDoc = DocumentHelper.createDocument();
		Namespace nsWordprocessingML = new Namespace("w",
				"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		Element sdtRoot = sdtDoc.addElement(new QName("sdt",
				nsWordprocessingML));
		
		// Attach sdtpr
		Document sdtPrDoc = sdtPr.getSdtPropertiesDoc();
		Element sdtPrDocRoot = sdtPrDoc.getRootElement();
		sdtRoot.add(sdtPrDocRoot.createCopy());		
		
		// attach sdtContent document
		sdtRoot.add(sdtContent.getRootElement().createCopy());		
		
		return sdtDoc;
	}
	
	
	// 

		
	/**
	 * 
	 */
//	public StructuredDocumentTag( Element e) {
//		
//		sdtContent = new ArrayList();
//
////		log.warn("    TODO - unmarshall the attributes on w:p");
//		
//	    Iterator elementIterator = e.elementIterator();
//	    while(elementIterator.hasNext()){
//	      Element element = (Element)elementIterator.next();
//	      
//		  if (element.getName()=="pPr" ) {
////			  pPr = new ParagraphProperties(element);
//			  sdtContent.add( pPr );			  
//		  } else if (element.getName()=="r" ) {
//			  sdtContent.add( new Run(element) );
//		  } else {
//		      log.warn("    Adding raw XML for '" + element.getName() + "'");
//		      sdtContent.add( element );
//		  }
//	    }
//		
//	}
	
//	public PStyle getStyle() {
//		
//		if (pPr !=null ) {
//			return pPr.getStyle();
//		} else {
//			return null;
//		}
//		
//	}
//	
//	/* Create an XML element from the Java object tree
//	 * 
//	 */
//	public Element marshall() {		
//		
//		Element p = new DefaultElement(new QName(Constants.PARAGRAPH_BODY_TAG_NAME,
//				Constants.namespaceWord));
//		
//		List children = p.content();
//		
//		log.warn("    TODO - marshall the attributes on w:p");
//		
//		// iterate through the body ArrayList
//		for (Object o : paragraphContents ) {
//			
//			if ( o instanceof Run ) {
//				children.add( ((Run)o).marshall() );
//			} else if ( o instanceof ParagraphProperties ) {
//				children.add( ((ParagraphProperties)o).marshall() );
//			} else {
//				children.add( ((Element)o).createCopy() );
//			}
//			
//			
//		}
//		
//		return p;		
//	}
	
		
	}


