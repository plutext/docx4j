/*
 *  Copyright 2011, Plutext Pty Ltd.
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
package org.docx4j.model.datastorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.RPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Word (2007) can't open a docx if it has more than one
 * comment with the same ID.  Similarly for footnoteReference
 * and endnoteReference.
 * 
 * Bookmarks: duplicate, missing (or half missing) bookmarks
 * don't seem to trouble Word, so we don't check for these.
 * 
 * Since docx4j 3.0, two content control integrity checks are
 * also done here:
 * 
 * 1. w:tr/w:sdt must contain w:tc, and w:tc must be non-empty
 * 
 * 2. w:tc/w:sdt must be non-empty
 * 
 * Note that the w:sdts can be nested, so simple parent/child
 * checks aren't sufficient.
 * 
 * @author jharrop
 *
 */
public class OpenDoPEIntegrity {
	
	private static Logger log = LoggerFactory.getLogger(OpenDoPEIntegrity.class);	
	
	private HashMap<String, String> commentRangeStart;  // value doesn't matter
	private HashMap<String, String> commentRangeEnd;
	private HashMap<String, String> commentReference;
	
	private HashMap<String, String> footnoteReference;
	private HashMap<String, String> endnoteReference;
	
	static Templates xslt;			
	private static XPathFactory xPathFactory;
	private static XPath xPath;
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/model/datastorage/OpenDoPEIntegrity.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
		xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();		
	}

	
	public static void log(String message ) {
		
		log.info(message);
	}

		
		public void process(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
			
			commentRangeStart = new HashMap<String, String>();
			commentRangeEnd = new HashMap<String, String>();
			commentReference = new HashMap<String, String>();
			
			footnoteReference = new HashMap<String, String>();
			endnoteReference = new HashMap<String, String>();
			

			// A component can apply in both the main document part,
			// and in headers/footers. See further
			// http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html
			
			process(wordMLPackage.getMainDocumentPart());
	
			// Add headers/footers
			RelationshipsPart rp = wordMLPackage.getMainDocumentPart()
					.getRelationshipsPart();
			for (Relationship r : rp.getRelationships().getRelationship()) {
	
				if (r.getType().equals(Namespaces.HEADER)) {
					process((HeaderPart) rp.getPart(r));
				} else if (r.getType().equals(Namespaces.FOOTER)) {
					process((FooterPart) rp.getPart(r));
				}
			}
		}
	
		private void process(JaxbXmlPart part) throws Docx4JException {
			
			org.docx4j.openpackaging.packages.OpcPackage pkg 
				= part.getPackage();		
				// Binding is a concept which applies more broadly
				// than just Word documents.
			
			org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
					part.getJaxbElement() ); 	
			
			JAXBContext jc = Context.jc;
			try {
				// Use constructor which takes Unmarshaller, rather than JAXBContext,
				// so we can set JaxbValidationEventHandler
				Unmarshaller u = jc.createUnmarshaller();
				u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
				javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(u );
				
				Map<String, Object> transformParameters = new HashMap<String, Object>();
				transformParameters.put("OpenDoPEIntegrity", this);			
						
				org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
				
				part.setJaxbElement(result);
			} catch (Exception e) {
				throw new Docx4JException("Problems ensuring integrity", e);			
			}
					
		}

		public static boolean encountered(OpenDoPEIntegrity odIntegrityInstance, String elementName, String id) {
			
			boolean previouslyEncountered = false;
			
			if (elementName.equals("commentRangeStart")) {
			
				previouslyEncountered = odIntegrityInstance.commentRangeStart.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.commentRangeStart.put(id, id); 
				}
				return previouslyEncountered; 
			}
			
			if (elementName.equals("commentRangeEnd")) {
				
				previouslyEncountered = odIntegrityInstance.commentRangeEnd.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.commentRangeEnd.put(id, id); 
				}
				return previouslyEncountered; 
			}
			
			if (elementName.equals("commentReference")) {
				
				previouslyEncountered = odIntegrityInstance.commentReference.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.commentReference.put(id, id); 
				}
				return previouslyEncountered; 
			}
				 
			if (elementName.equals("footnoteReference")) {
				
				previouslyEncountered = odIntegrityInstance.footnoteReference.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.footnoteReference.put(id, id); 
				}
				return previouslyEncountered; 
			}
				
			if (elementName.equals("endnoteReference")) {
				
				previouslyEncountered = odIntegrityInstance.endnoteReference.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.endnoteReference.put(id, id); 
				}
				return previouslyEncountered; 
			}
			
			log.error("Unexpected elementName: " + elementName);
			return false;
		}

}
