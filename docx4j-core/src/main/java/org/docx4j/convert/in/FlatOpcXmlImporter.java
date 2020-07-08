/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.convert.in;



import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.coverPageProps.CoverPageProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.io.Load;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCoverPagePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.PresentationML.JaxbPmlPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.JaxbSmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BibliographyPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.QuestionsPart;
import org.docx4j.openpackaging.parts.opendope.StandardisedAnswersPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;



/**
 * Create a Package object from an
 * XmlPackage object.
 * 
 * Microsoft Word and Powerpoint both support
 * saving to this format.  Excel doesn't, but you 
 * can still have a spreadsheet in this format if you want to. 
 * 
 * "<?xml version=""1.0"" standalone=""yes""?>
 * <?mso-application progid=""Word.Document""?>
 * 
 * <pkg:package xmlns:pkg=""http://schemas.microsoft.com/office/2006/xmlPackage"">
 * 
 * <pkg:part pkg:name= 
 * 			 pkg:contentType= 
 * 			 pkg:padding=""512"">   (@padding is only on rels)
 * 
 * 		<pkg:xmlData>
 * 
 * <pkg:part pkg:name=""/word/media/image2.jpeg"" 
 * 			 pkg:contentType=""image/jpeg"" 
 * 			 pkg:compression=""store"">  (@compression is only on binary parts)
 * 
 * 		<pkg:binaryData>  
 * 
 * @author jharrop
 *
 */
public class FlatOpcXmlImporter  {
	
	private static Logger log = LoggerFactory.getLogger(FlatOpcXmlImporter.class);

	public FlatOpcXmlImporter(InputStream is) throws JAXBException {
		
		JAXBContext jc = Context.jcXmlPackage;
		Unmarshaller u = jc.createUnmarshaller();
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		// Guard against XXE
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
        XMLStreamReader xsr = null;
        try {
			xsr = xif.createXMLStreamReader(is);
		} catch (XMLStreamException e) {
			throw new JAXBException(e);
		}		

		// JAXB RI unmarshalls to JAXBElement; MOXy gives Package directly
        
//		org.docx4j.xmlPackage.Package flatOpcXml = 
//        (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(xsr)).getValue(); 

		org.docx4j.xmlPackage.Package flatOpcXml = null;
		try {
			flatOpcXml = (org.docx4j.xmlPackage.Package)XmlUtils.unwrap(u.unmarshal(xsr));
		} catch ( javax.xml.bind.UnmarshalException e) {
			if (e.getMessage()!=null
					&& e.getMessage().contains("http://schemas.microsoft.com/office/word/2003/wordml")) {
				throw new IllegalArgumentException("Word 2003 XML is not supported. Use a docx or Flat OPC XML instead, or look at the Word2003XmlConverter proof of concept.");	
				// So as not to change existing throws clause
			}
			throw e;
		}
		
		init(flatOpcXml);
		
	}
	
	public FlatOpcXmlImporter(org.docx4j.xmlPackage.Package flatOpcXml) {
				
		init(flatOpcXml);
	}
	
	private void init(org.docx4j.xmlPackage.Package flatOpcXml) {
		
		parts = new HashMap<String, org.docx4j.xmlPackage.Part>();
		for(org.docx4j.xmlPackage.Part p : flatOpcXml.getPart() ) {
			if (log.isDebugEnabled()) {
				log.debug("Adding " + p.getName());
			}
			parts.put(p.getName(), p);
		}
		
	}

	private HashMap<String, org.docx4j.xmlPackage.Part>parts;
	
	
	private ContentTypeManager ctm;

	/**
	 * This HashMap is intended to prevent loops.
	 */
	protected HashMap<String, String> handled = new HashMap<String, String>();
	
	private OpcPackage packageResult; 
		
	public OpcPackage get() throws Docx4JException {
		
		// 2. Create a new Package
		//		Eventually, you'll also be able to create an Excel package etc
		//		but only the WordML package exists at present
		
		ctm = new ContentTypeManager();
		
		ctm.addDefaultContentType("rels", "application/vnd.openxmlformats-package.relationships+xml");
		ctm.addDefaultContentType("xml", "application/xml");

		// Later, we'll add each content type to ctm as an override as we encounter it
		// eg
		//  <Override PartName="/word/document.xml"     
		//			ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
		
		// Create the right type of package
		RelationshipsPart tmpRp = new RelationshipsPart();
		org.docx4j.xmlPackage.Part part = parts.get("/_rels/.rels");
		if (part == null) {
			throw new Docx4JException("Couldn't find pkg rels /_rels/.rels" );
		}
		try {
			populateRelationshipsPart(tmpRp, part.getXmlData().getAny());
		} catch (JAXBException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		Relationship r = tmpRp.getRelationshipByType(Namespaces.DOCUMENT);
		String target = r.getTarget();
		if (target.startsWith("/")) {
			// Word Online as at November 2018.  Buggy?!
			// eg <Relationship Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="/word/document2.xml" Id="rId1" />
			log.debug("target " + target + " already starts with '/'.  Word Online docx? " );
		} else {
			target = "/" + target;
		}
		org.docx4j.xmlPackage.Part officeDocument = parts.get(target);
		if (officeDocument==null) {
			throw new Docx4JException("Couldn't find part named " + r.getTarget());
		}
		
		packageResult = ctm.createPackage(officeDocument.getContentType());
		log.info("Creating " + packageResult.getClass().getName() );
		
		// 4. Start with _rels/.rels

//		<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
//		  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
//		  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
//		  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
//		</Relationships>		
		
		String partName = "/_rels/.rels"; // note leading '/'
		RelationshipsPart rp = getRelationshipsPartFromXmlPackage(packageResult, partName);				
		packageResult.setRelationships(rp);
		
		// 5. Now recursively 
//		(i) create new Parts for each thing listed
//		in the relationships
//		(ii) add the new Part to the package
//		(iii) cross the PartName off unusedZipEntries
		addPartsFromRelationships(packageResult, rp );
		
		Load.registerCustomXmlDataStorageParts(packageResult);
		 
		packageResult.setNew(false);
		
		 return packageResult;
		
	}

	
	private RelationshipsPart getRelationshipsPartFromXmlPackage(Base p, String partName) 
			throws Docx4JException {
		
		RelationshipsPart rp = null;
		
		try {
			
			org.docx4j.xmlPackage.Part part = parts.get(partName);
			
			if (part == null) {
				return null;
			}
			
			rp = p.getRelationshipsPart(true);
			populateRelationshipsPart(rp,  part.getXmlData().getAny());
									
		} catch (Exception e) {
			e.printStackTrace();
			throw new Docx4JException("Error getting document from XmlPackage:" + partName, e);
			
		} 
		
		return rp;
	}

	public static RelationshipsPart populateRelationshipsPart(RelationshipsPart rp, org.w3c.dom.Element el) throws InvalidFormatException, JAXBException {
		
		rp.setRelationships( (Relationships)rp.unmarshal(el) );
		
		return rp;		
	}
	
	/* recursively 
	(i) create new Parts for each thing listed
	in the relationships
	(ii) add the new Part to the package
	(iii) cross the PartName off unusedZipEntries
	*/
	private void addPartsFromRelationships( Base source, RelationshipsPart rp)
		throws Docx4JException {
		
		OpcPackage pkg = source.getPackage();				
		
//		for (Iterator it = rp.iterator(); it.hasNext(); ) {
//			Relationship r = (Relationship)it.next();
//			log.info("For Relationship Id=" + r.getId() + " Source is " 
//					+ r.getSource().getPartName() 
//					+ ", Target is " + r.getTargetURI() );
//			try {
//				
//				getPart(pkg, rp, r);
//				
//			} catch (Exception e) {
//				throw new Docx4JException("Failed to add parts from relationships", e);
//			}
//		}
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.debug("For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() );
			try {				
				getPart(pkg, rp, r);
			} catch (Exception e) {
				throw new Docx4JException("Failed to add parts from relationships", e);
			}
		}
		
		
		
	}

	/**
	 * Get a Part (except a relationships part), and all its related parts.  
	 * This can be called directly from outside the library, in which case 
	 * the Part will not be owned by a Package until the calling code makes it so.  
	 * 
	 * @param zf
	 * @param source
	 * @param unusedZipEntries
	 * @param pkg
	 * @param r
	 * @param resolvedPartUri
	 * @throws Docx4JException
	 * @throws InvalidFormatException
	 */
	private void getPart( OpcPackage pkg, RelationshipsPart rp, Relationship r)
			throws Docx4JException, InvalidFormatException, URISyntaxException {
		
		Base source = null;
		String resolvedPartUri = null;
		
		if (r.getTargetMode() == null
				|| !r.getTargetMode().equals("External") ) {
			
			// Usual case
			
//			source = r.getSource();
//			resolvedPartUri = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();		
			source = rp.getSourceP();
			resolvedPartUri = URIHelper.resolvePartUri(rp.getSourceURI(), new URI(r.getTarget() ) ).toString();		

			// Don't drop leading "/' in Xml Package
			// resolvedPartUri = resolvedPartUri.substring(1);				

			
		} else {			
			// EXTERNAL
			/* "When set to External, the target attribute may be a relative
			 *  reference or a URI.  If the target attribute is a relative
			 *  reference, then that reference is interpreted relative to the
			 *  location of the package."
			 */

			log.info("Encountered external resource " + r.getTarget() 
					   + " of type " + r.getType() );
			
			// As of 1 May 2008, we don't do anything with these yet.
			// No need to create a Part out of them until such time as
			// we need to read the contents. (External resources don't
			// seem to necessarily be described in [ContentTypes].xml )
			
			// When the document is saved, the relationship is simply
			// written again.
			
			return;
		}
		
		if (handled.get(resolvedPartUri)!=null) return;
		
		String relationshipType = r.getType();		
			
		Part part = getRawPart(ctm, resolvedPartUri,r);
		rp.loadPart(part, r);
		handled.put(resolvedPartUri, resolvedPartUri);
		

		// The source Part (or Package) might have a convenience
		// method for this
		if (source.setPartShortcut(part, relationshipType ) ) {
			log.debug("Convenience method established from " + source.getPartName() 
					+ " to " + part.getPartName());
		}
		
//		log.info(".. added." );
		
		RelationshipsPart rrp = getRelationshipsPart( part);
		if (rrp!=null) {
			// recurse via this parts relationships, if it has any
			addPartsFromRelationships( part, rrp );
//			String relPart = PartName.getRelationshipsPartName(
//					part.getPartName().getName().substring(1) );
		}
	}

	/**
	 * Get the Relationships Part (if there is one) for a given Part.  
	 * Otherwise return null.
	 * 
	 * @param zf
	 * @param part
	 * @return
	 * @throws InvalidFormatException
	 */
	public RelationshipsPart getRelationshipsPart(Part part)
		throws Docx4JException, InvalidFormatException {
		
		// recurse via this parts relationships, if it has any
		
		String relPart = PartName.getRelationshipsPartName(
				part.getPartName().getName() );

		RelationshipsPart rrp = getRelationshipsPartFromXmlPackage(part,  relPart);
		
		if (rrp!=null) {
			part.setRelationships(rrp);
		} else {
			log.debug("No relationships " + relPart );	
			return null;
		}
		return rrp;
	}
	
	
	public Part getRawPart(ContentTypeManager ctm, String resolvedPartUri, Relationship rel)
	throws Docx4JException {
		
		org.docx4j.xmlPackage.Part pkgPart = parts.get(resolvedPartUri);
		if (pkgPart == null) {
			log.error("Missing part: " + resolvedPartUri);
			return null;
		}
		
		return getRawPart(ctm, pkgPart, rel);
	}
	/**
	 * Get a Part (except a relationships part), but not its relationships part
	 * or related parts.  Useful if you need quick access to just this part.
	 * This can be called directly from outside the library, in which case 
	 * the Part will not be owned by a Package until the calling code makes it so.  
	 * @see  To get a Part and all its related parts, and add all to a package, use
	 * getPart.
	 * @param zf
	 * @param resolvedPartUri
	 * @return
	 * @throws URISyntaxException
	 * @throws InvalidFormatException
	 */
	public static Part getRawPart(ContentTypeManager ctm, org.docx4j.xmlPackage.Part pkgPart, Relationship rel)
			throws Docx4JException {
		
		Part part = null;
		
		try {
			org.w3c.dom.Element el = null; 

			try {
				
				String contentType = pkgPart.getContentType();
				log.debug("contentType: " + contentType);
				
				
				if (pkgPart.getXmlData()!=null) {
					// if its not binary
					el = pkgPart.getXmlData().getAny();
				}
								
//				 part = ctm.newPartForContentType(contentType, resolvedPartUri,rel);
				 part = ctm.newPartForContentType(contentType, pkgPart.getName(), rel);
				 part.setContentType( new ContentType(contentType) );
				 
//				 ctm.addOverrideContentType(new java.net.URI(resolvedPartUri), 
				 ctm.addOverrideContentType(new java.net.URI(pkgPart.getName()), 
						 contentType);
				
				if (part instanceof org.docx4j.openpackaging.parts.ThemePart) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcThemePart);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsCorePart ) {
					
						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsCore);
						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );
						
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart ) {

						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsCustom);
						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );
						
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart ) {

						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsExtended);
						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );

				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart ) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcCustomXmlProperties);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );					
					
//				} else if (part instanceof org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart ) {
//
//					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcXmlDSig);
//					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );										
										
				} else if (part.getClass().getName().equals("org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart") ) {

//										((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcXmlDSig);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );					
					
				} else if (part instanceof JaxbPmlPart) {

					// Presentation type part
					
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(org.pptx4j.jaxb.Context.jcPML);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );

				} else if (part instanceof JaxbSmlPart) {

					// Spreadsheet type part
					
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(org.xlsx4j.jaxb.Context.jcSML);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {

					// MainDocument part, Styles part, Font part etc
					
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jc);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );
					
//				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart) {
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart) {
					
					log.debug("Detected BinaryPart " + part.getClass().getName() );
					((BinaryPart)part).setBinaryData( pkgPart.getBinaryData() );	
					
				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) {
					
					// Is it a part we know?
					try {
						Unmarshaller u = Context.jc.createUnmarshaller();
						Object o = u.unmarshal( el );						
						log.debug(o.getClass().getName());
						
						PartName name = part.getPartName();
						
						if (o instanceof CoverPageProperties) {
							
							part = new DocPropsCoverPagePart(name);							
							((DocPropsCoverPagePart)part).setJaxbElement(
									(CoverPageProperties)o);
							
						} else if (o instanceof org.opendope.conditions.Conditions) {
							
							part = new ConditionsPart(name);
							((ConditionsPart)part).setJaxbElement(
									(org.opendope.conditions.Conditions)o);
							
							
						} else if (o instanceof org.opendope.xpaths.Xpaths) {
							
							part = new XPathsPart(name);
							((XPathsPart)part).setJaxbElement(
									(org.opendope.xpaths.Xpaths)o);

						} else if (o instanceof org.opendope.questions.Questionnaire) {
							
							part = new QuestionsPart(name);
							((QuestionsPart)part).setJaxbElement(
									(org.opendope.questions.Questionnaire)o);

						} else if (o instanceof org.opendope.answers.Answers) {
							
							part = new StandardisedAnswersPart(name);
							((StandardisedAnswersPart)part).setJaxbElement(
									(org.opendope.answers.Answers)o);
							
						} else if (o instanceof org.opendope.components.Components) {
							
							part = new ComponentsPart(name);
							((ComponentsPart)part).setJaxbElement(
									(org.opendope.components.Components)o);

						} else if (o instanceof JAXBElement<?> 
								&& XmlUtils.unwrap(o) instanceof org.docx4j.bibliography.CTSources) {
							part = new BibliographyPart(name);
							((BibliographyPart)part).setJaxbElement(
									(JAXBElement<org.docx4j.bibliography.CTSources>)o);
														
						} else {
							
							log.warn("No known part after all for CustomXmlPart " + o.getClass().getName());

							CustomXmlDataStorage data = Load.getCustomXmlDataStorageClass().factory();

							// Copy el into a new document
							org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().newDocument();
							//XmlUtils.treeCopy(el, doc);
							org.w3c.dom.Node copy = doc.importNode(el, true);
							// Word doesn't like the xml namespace to be bound. At some point in a process 
							// from docx -> package-> flatopc -> package -> docx, it is added to the custom xml root element.
							try {
								copy.getAttributes().removeNamedItemNS("http://www.w3.org/2000/xmlns/","xml");
							} catch (DOMException e) {}
							doc.appendChild(copy);							
							data.setDocument(doc); 
							
							((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) part)
									.setData(data);
							
						}
						
					} catch (javax.xml.bind.UnmarshalException ue) {
						
						// No ...
						CustomXmlDataStorage data = Load.getCustomXmlDataStorageClass().factory();

						// Copy el into a new document
						org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().newDocument();
						//XmlUtils.treeCopy(el, doc);
						org.w3c.dom.Node copy = doc.importNode(el, true);
						try {
							copy.getAttributes().removeNamedItemNS("http://www.w3.org/2000/xmlns/","xml");
						} catch (DOMException e) {}
						doc.appendChild(copy);							
						data.setDocument(doc); 
						
						((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) part)
								.setData(data);
					}					
					
					
				} else if (part instanceof org.docx4j.openpackaging.parts.XmlPart ) {
					
					// Copy el into a new document
					org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().newDocument();
					//XmlUtils.treeCopy(el, doc);
					org.w3c.dom.Node copy = doc.importNode(el, true);
					try {
						copy.getAttributes().removeNamedItemNS("http://www.w3.org/2000/xmlns/","xml");
						// May not be required here. Not tested.
					} catch (DOMException e) {}
					doc.appendChild(copy);							
					
					((XmlPart)part).setDocument(doc); 

				} else {
					// Shouldn't happen, since ContentTypeManagerImpl should
					// return an instance of one of the above, or throw an
					// Exception.
					
					log.error("No suitable part found for: " + pkgPart.getName());
					part = null;					
				}
			} catch (java.lang.IllegalArgumentException e) {

				if (el!=null) {
					log.error(e.getMessage());
					log.error(XmlUtils.w3CDomNodeToString(el));
				}
				throw e;				
				
			} catch (PartUnrecognisedException e) {

				// Try to get it as a binary part
				log.error("Part unrecognised: " + pkgPart.getName());
				part = new BinaryPart( new PartName(pkgPart.getName())); // /?
				((BinaryPart)part).setBinaryData( pkgPart.getBinaryData() );
			}
		} catch (Exception ex) {
			// IOException, URISyntaxException
			ex.printStackTrace();
			throw new Docx4JException("Failed to getPart", ex);			
			
		} 
		return part;
	}
	
	
}
