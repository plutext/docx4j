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

import org.apache.log4j.Logger;

import org.docx4j.Namespaces;
import org.docx4j.document.wordprocessingml.Run;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;



/**
 *
 * @author Julien Chable, Jason Harrop
 */
public final class Paragraph {

	private static Logger log = Logger.getLogger(Paragraph.class);		
	

	protected ArrayList paragraphContents;

	protected ParagraphProperties pPr;
	
	
	
	/**
	 * 
	 */
	public Paragraph( Element e) {
		
		paragraphContents = new ArrayList();

		// log.debug("    TODO - unmarshall the attributes on w:p");
		// <w:p w:rsidR="007B6A23" w:rsidRDefault="00B81547">
		
	    Iterator elementIterator = e.elementIterator();
	    while(elementIterator.hasNext()){
	      Element element = (Element)elementIterator.next();
	      
		  if (element.getName()=="pPr" ) {
			  pPr = new ParagraphProperties(element);
			  paragraphContents.add( pPr );			  
		  } else if (element.getName()=="r" ) {
		      paragraphContents.add( new Run(element) );
		  } else {
		      //log.info("    Adding raw XML for '" + element.getName() + "'");
		      paragraphContents.add( element );
		  }
	    }
		
	}
	
	public PStyle getStyle() {
		
		if (pPr !=null ) {
			return pPr.getStyle();
		} else {
			return null;
		}
		
	}
	
	/* Convenience method to extract the text from the
	 * Runs comprising the paragraph.
	 */
	public String getText() {
		
		StringBuffer sb = new StringBuffer();
				
		// iterate through the body ArrayList
		for (Object o : paragraphContents ) {			
			if ( o instanceof Run ) {
				sb.append(((Run)o).getText() );
			} 
		}
		
		return sb.toString();
		
	}
	
	
	/* Create an XML element from the Java object tree
	 * 
	 */
	public Element marshall() {		
		
		Element p = new DefaultElement(new QName(Constants.PARAGRAPH_BODY_TAG_NAME,
				Namespaces.namespaceWord));
		
		List children = p.content();
		
		log.warn("    TODO - marshall the attributes on w:p");
		
		// iterate through the body ArrayList
		for (Object o : paragraphContents ) {
			
			if ( o instanceof Run ) {
				children.add( ((Run)o).marshall() );
			} else if ( o instanceof ParagraphProperties ) {
				children.add( ((ParagraphProperties)o).marshall() );
			} else {
				children.add( ((Element)o).createCopy() );
			}
			
			
		}
		
		return p;		
	}
	
	

	public void addRun(Run run) {
		paragraphContents.add(run);
	}

	/*
	 * 
	 */
	public List getParagraphContents() {
		return paragraphContents;
	}
	
	/**
	private Element addParameterStyleXmlCode(Element paraProperties) {
		return Paragraph.addParameterStyleXmlCode(paraProperties,
				paragraphStyleName);
	}

	private static Element addParameterStyleXmlCode(Element paraProperties,
			String styleName) {
		Element paraStyle= paraProperties.addElement(new QName(WordprocessingML.PARAGRAPH_STYLE, WordDocument.namespaceWord));
		paraStyle.addAttribute(new QName(WordprocessingML.PROPERTIES_VALUE_TAG_NAME, WordDocument.namespaceWord), styleName);

		return paraStyle;
	}

	public static void addDefaultStyleXmlCode(Element paraNode) {
		// check paragraph has property node
		Element propertiesNode = hasProperties(paraNode);

		if (propertiesNode != null) {
			// add to properties
			addParameterStyleXmlCode(propertiesNode,
					ParagraphBuilder.DEFAULT_PARAGRAPH_STYLE);
		} else {
			// add properties node (w:pPr) and then the style as child
			propertiesNode=paraNode.addElement(new QName(WordprocessingML.PARAGRAPH_PROPERTIES_TAG_NAME, WordDocument.namespaceWord));
			addParameterStyleXmlCode(propertiesNode,
					ParagraphBuilder.DEFAULT_PARAGRAPH_STYLE);
		}
	}

	public static Element hasProperties(Element nodeToCheck) {
		return hasTag(nodeToCheck,
				WordprocessingML.PARAGRAPH_PROPERTIES_TAG_NAME);
	}

	public static Element hasStyleName(Element nodeToCheck) {
		Element propertiesNode = Paragraph.hasProperties(nodeToCheck);
		if (propertiesNode == null) {
			// no properties for paragraph, therefore no style
			return null;
		}
		return Paragraph.hasTag(propertiesNode,
				WordprocessingML.PARAGRAPH_STYLE);
	}

	private static Element hasTag(Element nodeToCheck, String valueToSearchFor) {
		List listOfElements=nodeToCheck.elements();
		for (Iterator iter = listOfElements.iterator(); iter.hasNext();) {
			Element element = (Element) iter.next();
			if (element.getName().equals(valueToSearchFor)){
				//tag found
				return element;
				}
			}

		return null; // all children of paragraph have been checked and none
						// has the required tag

	}
	*/

	/**
	 * build a paragraph in open XML
	 *
	 * @return
	 */
//	public Element build() {
	//	DocumentFactory factory=DocumentFactory.getInstance();
	//	Element paragraph=factory.createElement(new QName(WordprocessingML.PARAGRAPH_BODY_TAG_NAME, WordDocument.namespaceWord));
	//	Element paragraph=insertionPoint.addElement(new QName(WordprocessingML.PARAGRAPH_BODY_TAG_NAME, WordDocument.namespaceWord));

		// add paragraph properties
		//addParagraphProperties( paragraph);

		// in a read only document, is this paragraph write enabled ?
		//if (readOnlyPermission != null) {
		//	readOnlyPermission.addReadOnlyStartTag( paragraph);
		//}
		// add Runs
		//addRunsInParagraph( paragraph);

		// in a read only document, is this paragraph write enabled ?
		//if (readOnlyPermission != null) {
		//	readOnlyPermission.addReadOnlyEndTag( paragraph);
		//}
		//return paragraph;
	//}

	private void addRunsInParagraph(Element paragraph) {
//#ifdef JAVA5
		//for (Run r : runs) {
//#else
/*
			Iterator iter = runs.iterator(); while (iter.hasNext()) {
				Run r =	  (Run) iter.next();
*/
//#endif
			//Element run=paragraph.addElement(new QName(WordprocessingML.PARAGRAPH_RUN_TAG_NAME, WordDocument.namespaceWord));

			// properties
			//Element runProps=run.addElement(new QName(WordprocessingML.RUN_PROPERTIES_TAG_NAME, WordDocument.namespaceWord));

		//}
	}

}
