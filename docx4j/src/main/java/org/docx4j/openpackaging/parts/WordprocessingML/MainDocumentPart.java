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

package org.docx4j.openpackaging.parts.WordprocessingML;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.docx4j.Namespaces;
import org.docx4j.document.wordprocessingml.Constants;
import org.docx4j.document.wordprocessingml.Paragraph;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.PartName;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;



public class MainDocumentPart extends DocumentPart  {
	
	private java.util.ArrayList body; // [1.2.2]

	private static Logger log = Logger.getLogger(MainDocumentPart.class);
	
	
	/** The body ArrayList is a list of objects representing the block level
	 * contents of the body of the document - the main document editing surface.
	 * 
	 * Unless and until we have a dedicated object to represent a particular bit 
	 * of block level markup, we just store the XML snippet.  That way, 100%
	 * fidelity can be ensured.
	 * 
	 * Here's the plan for dealing with each bit of block level content:
	 * 
	 * High Priority
	 * -------------
	 * p (Paragraph)
	 * sectPr (Document Final Section Properties)
	 * proofErr (Proofing Error Anchor)
	 * tbl (Table)
	 * 
	 * Medium Priority
	 * ---------------
	 * BookmarkStart/End
	 * CommentRangeStart/End
	 * Revisions (16 elements)
	 * 
	 * Lower Priority
	 * --------------
	 * altChunk
	 * CustomXml
	 * sdt (Block-Level Structured Document Tag)
	 * RangePermissions
	 * Math
	 * 
*/
	
	 /** 
	 * @throws InvalidFormatException
	 */
	//public MainDocumentPart(Package pack, PackagePartName partUri) {
	public MainDocumentPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}

	public ArrayList getBody() {
		return body;
	}
	
	public void setDocument(Document document) {
		this.document = document;
		unmarshall(document);
	}
	
	public Document getDocument() {
		return marshall();
	}
	
	
	/* Create a Java object tree from the XML document 
	 * which looks something like:
	 * 
	 * <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" ..>
		 * 		<w:body>
	 *			<w:p ..>
	 */
	private void unmarshall(Document doc) {
		
		try {
			//debugPrint(doc);
			body = new ArrayList();
		    Element root = doc.getRootElement();
		    Node bodyNode = root.element("body");
			// We iterate through the block level elements.
			// If its a p, we make a p object.
			// Otherwise, we just stick the XML fragment into the ArrayList.
		    Iterator elementIterator = ((Element)bodyNode).elementIterator();
		    while(elementIterator.hasNext()){
		      Element element = (Element)elementIterator.next();
		      
			  if (element.getName()=="p" ) {
				  Paragraph p = new Paragraph(element);
					  body.add( p );
			  } else {
			      log.info("Adding raw XML for '" + element.getName() + "'");
				  body.add( element );
			  }
		    }
		} catch (Exception e ) {
			e.printStackTrace();
		}
		
	}
	

	/* 
	 * If you set the objects in the following order:
	 * 
	 * -- Lists
	 * -- List Overrides
	 * -- Styles
	 * -- *then* Body
	 * 
	 * then for example, you can check that all the styles
	 * referenced in the body actually exist.  And all the numbering lists
	 * references in the styles exist.
	 * 
	 * That's why we don't have a constructor which builds the MainDocumentPart
	 * with just a body, though maybe we should also.
	 * 
	 * And for now, we just do the Body first. 
	 */

	private void debugPrint( Document coreDoc) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
		    XMLWriter writer = new XMLWriter( System.out, format );
		    writer.write( coreDoc );
		} catch (Exception e ) {
			e.printStackTrace();
		}	    
	}
	
	
	
	
	/* Create an XML document from the Java object tree
	 * 
	 */
	private Document marshall() {
		
		// create a new Document with root element
		// <w:document 
		//		xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" 
		//		xmlns:ve="http://schemas.openxmlformats.org/markup-compatibility/2006" 
		//		xmlns:o="urn:schemas-microsoft-com:office:office" 
		//		xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" 
		//		xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" 
		//		xmlns:v="urn:schemas-microsoft-com:vml" 
		//		xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" 
		//		xmlns:w10="urn:schemas-microsoft-com:office:word" 
		//		xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml">
		// 		<w:body>
		//			<w:p ..>

		Document doc = DocumentHelper.createDocument();
		Namespace nsWordprocessingML = new Namespace("w",
				"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		Element elDocument = doc.addElement(new QName("document",
				nsWordprocessingML));
		Element elBody = elDocument.addElement(new QName(Constants.WORD_DOC_BODY_TAG_NAME,
				Namespaces.namespaceWord));
		
		List bodyChildren = elBody.content();
		
		
		// iterate through the body ArrayList
		for (Object o : body ) {
			
			if ( o instanceof Paragraph ) {
				bodyChildren.add( ((Paragraph)o).marshall() );
			} else {
				bodyChildren.add( ((Element)o).createCopy() );
			}
		}
		return doc;
	}
			
	
}

	
