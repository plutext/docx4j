/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
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
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.PresentationML.JaxbPmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BibliographyPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.QuestionsPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
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
	
	private static Logger log = Logger.getLogger(FlatOpcXmlImporter.class);

	public FlatOpcXmlImporter(InputStream is) throws JAXBException {
		
		JAXBContext jc = Context.jcXmlPackage;
		Unmarshaller u = jc.createUnmarshaller();
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		org.docx4j.xmlPackage.Package flatOpcXml = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
				new javax.xml.transform.stream.StreamSource(is))).getValue(); 

		init(flatOpcXml);
		
	}
	
	public FlatOpcXmlImporter(org.docx4j.xmlPackage.Package flatOpcXml) {
				
		init(flatOpcXml);
	}
	
	private void init(org.docx4j.xmlPackage.Package flatOpcXml) {
		
		parts = new HashMap<String, org.docx4j.xmlPackage.Part>();
		for(org.docx4j.xmlPackage.Part p : flatOpcXml.getPart() ) {
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
		// TODO:
		// Get package rels:
		// <pkg:part pkg:name="/_rels/.rels" pkg:contentType="application/vnd.openxmlformats-package.relationships+xml" pkg:padding="512">
		// .. and find rel of Type officeDocument
		// .. <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
		// and follow that to its target,
		// and see what content type is set on that.
		// But the following (well known locations) will do for now.
		if (parts.get("/word/document.xml")!=null) {
			packageResult = new  WordprocessingMLPackage(ctm);
		} else if (parts.get("/ppt/presentation.xml")!=null) {
			packageResult = new  PresentationMLPackage(ctm);
		} else {
			throw new Docx4JException("Unrecognised package");
		}
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
		//rp.setPackageRelationshipPart(true);		
		
		
		log.info( "Object created for: " + partName);
		//log.info( rp.toString());
		
		// 5. Now recursively 
//		(i) create new Parts for each thing listed
//		in the relationships
//		(ii) add the new Part to the package
//		(iii) cross the PartName off unusedZipEntries
		addPartsFromRelationships(packageResult, rp );
		
		Load.registerCustomXmlDataStorageParts(packageResult);
		 
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
			
			rp = createRelationshipsPart(part);
			rp.setSourceP(p);
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new Docx4JException("Error getting document from XmlPackage:" + partName, e);
			
		} 
		
		return rp;
	}

	public static RelationshipsPart createRelationshipsPart(org.docx4j.xmlPackage.Part part) throws InvalidFormatException, JAXBException {
		
		RelationshipsPart rp = new RelationshipsPart( new PartName(part.getName() ) );
		org.w3c.dom.Element el = part.getXmlData().getAny();		
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
					
				} else if (part instanceof org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart ) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcXmlDSig);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );										
					
				} else if (part instanceof JaxbPmlPart) {

					// Presentation type part
					
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(org.pptx4j.jaxb.Context.jcPML);
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
						
						if (o instanceof org.opendope.conditions.Conditions) {
							
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
							javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							dbf.setNamespaceAware(true);
							org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();
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
						javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						dbf.setNamespaceAware(true);
						org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();
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
					javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					dbf.setNamespaceAware(true);
					org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();
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
	

	public static void main(String[] args) throws Exception {
		
		// Converting an pkg to a docx and back again (ie round trip)
		// is a reasonable test 
		
		// So read an existing pkg into a docx
		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/pkg.pkg";
		
		java.io.FileInputStream fin = new java.io.FileInputStream(inputfilepath);
		
		JAXBContext jc = Context.jcXmlPackage;

		Unmarshaller u = jc.createUnmarshaller();
					
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		Object o = u.unmarshal( 
				new javax.xml.transform.stream.StreamSource(fin ) );
		org.docx4j.xmlPackage.Package xmlPackage 
			= (org.docx4j.xmlPackage.Package)((JAXBElement)o).getValue();
				
		org.docx4j.convert.in.FlatOpcXmlImporter inWorker = 
			new org.docx4j.convert.in.FlatOpcXmlImporter(xmlPackage);
		
		WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)inWorker.get();
		
		// Ok, now spit it out again
			
		org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator outWorker 
			= new org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator(wordMLPackage);
		
		org.docx4j.xmlPackage.Package result = outWorker.get();
		
		boolean suppressDeclaration = true;
		boolean prettyprint = true;
		
		log.debug( 
				org.docx4j.XmlUtils.
					marshaltoString(result, suppressDeclaration, prettyprint, 
							org.docx4j.jaxb.Context.jcXmlPackage) );
	}	
	
	
}