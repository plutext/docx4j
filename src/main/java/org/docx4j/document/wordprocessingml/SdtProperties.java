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

import org.docx4j.Namespaces;


/**
 *
 * @author Jason Harrop
 */
public final class SdtProperties {

	private static Logger log = Logger.getLogger(SdtProperties.class);	
	
	/*
        <w:id w:val="417087989"/>
        <w:placeholder>
          <w:docPart w:val="2AC728E2BA4F4E608A865EC25F64AC3D"/>
        </w:placeholder>
		*/
		
		// ST_DecimalNumber which is an xsd:integer
		String id;
		
		// We'll store this in the programmatic tag element
		long tag = -1;
		

		public SdtProperties( ) {
			
			id = Integer.toString(Math.abs(new java.util.Random().nextInt()));
						
		}

		public SdtProperties(Element sdtPrElement ) {
			
			id = sdtPrElement.element("id").attributeValue("val");
			log.debug("id: " + id);

			if (sdtPrElement.element("tag")!=null ) {
				tag = Long.parseLong(sdtPrElement.element("tag").attributeValue("val"));
			} else {
				log.error("Encountered sdtPr without <tag>, which is OK in terms of the OOXML spec, but not this software.");
			}
			
			// later, might get locking value etc
			
		}
		
		
		public Document getSdtPropertiesDoc() {

			Document sdtContent = DocumentHelper.createDocument();
			Namespace nsWordprocessingML = new Namespace("w",
					"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
			Element sdtPropertiesRoot = sdtContent.addElement(new QName("sdtPr",
					nsWordprocessingML));
			
			// w:id
			Element idEl = sdtPropertiesRoot.addElement(new QName("id",
					nsWordprocessingML));
			idEl.addAttribute(new QName("val", Namespaces.namespaceWord), id);
			
			// w:tag
			if (tag>-1) {
				Element tagEl = sdtPropertiesRoot.addElement(new QName("tag",
						nsWordprocessingML));
				tagEl.addAttribute(new QName("val", Namespaces.namespaceWord), String.valueOf(tag) );
			}
			

			return sdtContent;			
			
		}
		
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			if (!this.id.equals(id)) {
				log.debug("Changing SDT ID from " + this.id + " to " + id);
			}
			this.id = id;
		}
		
		public void setTag(long tag) {
			this.tag=tag;
		}	
		
		public long getTag() {
			return tag;
		}
	
		    
	//		public SdtProperties( Element e) {
	//			
	////			paragraphContents = new ArrayList();
	//
	////			log.warn("    TODO - unmarshall the attributes on w:p");
	//			
	//		    Iterator elementIterator = e.elementIterator();
	//		    while(elementIterator.hasNext()){
	//		      Element element = (Element)elementIterator.next();
	//		      
	//			  if (element.getName()=="pPr" ) {
	////				  pPr = new ParagraphProperties(element);
	////				  paragraphContents.add( pPr );			  
	//			  } else if (element.getName()=="r" ) {
	////			      paragraphContents.add( new Run(element) );
	//			  } else {
	//			      log.warn("    Adding raw XML for '" + element.getName() + "'");
	////			      paragraphContents.add( element );
	//			  }
	//		    }
				


		
	}


