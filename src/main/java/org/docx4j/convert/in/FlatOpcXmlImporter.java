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



import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.io.Load;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationships;
import org.docx4j.relationships.Relationship;



/**
 * Create a Package object from an
 * XmlPackage object.
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

	
	public FlatOpcXmlImporter(org.docx4j.xmlPackage.Package xmlPackage) {
				
		parts = new HashMap<String, org.docx4j.xmlPackage.Part>();
		
		for(org.docx4j.xmlPackage.Part p : xmlPackage.getPart() ) {
			
			parts.put(p.getName(), p);
			
		}
		
	}

	private HashMap<String, org.docx4j.xmlPackage.Part>parts;
	
	
	private ContentTypeManager ctm;
	
	private Package wmlPackageResult; 
		
	public Package get() throws Docx4JException {
		
		// 2. Create a new Package
		//		Eventually, you'll also be able to create an Excel package etc
		//		but only the WordML package exists at present
		
		ctm = new ContentTypeManagerImpl();
		
		ctm.addDefaultContentType("rels", "application/vnd.openxmlformats-package.relationships+xml");
		ctm.addDefaultContentType("xml", "application/xml");

		// Later, we'll add each content type to ctm as an override as we encounter it
		// eg
		//  <Override PartName="/word/document.xml"     
		//			ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
		
		
		wmlPackageResult = new  WordprocessingMLPackage(ctm);
		
		
		// 4. Start with _rels/.rels

//		<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
//		  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
//		  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
//		  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
//		</Relationships>		
		
		String partName = "/_rels/.rels"; // note leading '/'
		
		RelationshipsPart rp = getRelationshipsPartFromXmlPackage(wmlPackageResult, partName);		
		wmlPackageResult.setRelationships(rp);
		//rp.setPackageRelationshipPart(true);		
		
		
		log.info( "Object created for: " + partName);
		//log.info( rp.toString());
		
		// 5. Now recursively 
//		(i) create new Parts for each thing listed
//		in the relationships
//		(ii) add the new Part to the package
//		(iii) cross the PartName off unusedZipEntries
		addPartsFromRelationships(wmlPackageResult, rp );
		 
		 return wmlPackageResult;
		
	}
	
	private RelationshipsPart getRelationshipsPartFromXmlPackage(Base p, String partName) 
			throws Docx4JException {
		
		RelationshipsPart rp = null;
		
		try {
			
			org.docx4j.xmlPackage.Part part = parts.get(partName);
			
			if (part == null) {
				return null;
			}
			
			org.w3c.dom.Element el = part.getXmlData().getAny();
			
			rp = new RelationshipsPart(new PartName(partName) );
			// PartName already starts with a '/', so no need to add it
			rp.setSourceP(p);
			
			rp.setRelationships( (Relationships)rp.unmarshal(el) );
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new Docx4JException("Error getting document from XmlPackage:" + partName, e);
			
		} 
		
		return rp;
	}

	public org.dom4j.Element convertW3CtoDom4J( org.w3c.dom.Element element) throws Exception {
		
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);
		org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();

		doc.appendChild(  doc.importNode(element, true) );

		// Convert w3c document to dom4j document
		org.dom4j.io.DOMReader xmlReader = new org.dom4j.io.DOMReader();

		org.dom4j.Document doc2 = xmlReader.read(doc);

		return doc2.getRootElement();
		
	}	
		
	
	/* recursively 
	(i) create new Parts for each thing listed
	in the relationships
	(ii) add the new Part to the package
	(iii) cross the PartName off unusedZipEntries
	*/
	private void addPartsFromRelationships( Base source, RelationshipsPart rp)
		throws Docx4JException {
		
		Package pkg = source.getPackage();				
		
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
			
			log.info("For Relationship Id=" + r.getId() 
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
	private void getPart( Package pkg, RelationshipsPart rp, Relationship r)
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

			log.warn("Encountered external resource " + r.getTarget() 
					   + " of type " + r.getType() );
			
			// As of 1 May 2008, we don't do anything with these yet.
			// No need to create a Part out of them until such time as
			// we need to read the contents. (External resources don't
			// seem to necessarily be described in [ContentTypes].xml )
			
			// When the document is saved, the relationship is simply
			// written again.
			
			return;
		}
		
		String relationshipType = r.getType();		
			
		Part part = getRawPart(ctm, resolvedPartUri);
		rp.loadPart(part);

		// The source Part (or Package) might have a convenience
		// method for this
		if (source.setPartShortcut(part, relationshipType ) ) {
			log.info("Convenience method established from " + source.getPartName() 
					+ " to " + part.getPartName());
		}
		
		log.info(".. added." );
		
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
			log.info("No relationships " + relPart );	
			return null;
		}
		return rrp;
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
	public Part getRawPart(ContentTypeManager ctm, String resolvedPartUri)
			throws Docx4JException {
		
		Part part = null;
		
		try {
			org.docx4j.xmlPackage.Part pkgPart = parts.get(resolvedPartUri);
			org.w3c.dom.Element el = null; 

			try {
				
				
				if (pkgPart == null) {
					log.error("Missing part: " + resolvedPartUri);
					return null;
				}
				
				String contentType = pkgPart.getContentType();
				log.debug("contentType: " + contentType);
				
				
				if (pkgPart.getXmlData()!=null) {
					// if its not binary
					el = pkgPart.getXmlData().getAny();
				}
								
				 part = ctm.newPartForContentType(contentType, resolvedPartUri);
				 part.setContentType( new ContentType(contentType) );
				 
				 ctm.addOverrideContentType(new java.net.URI(resolvedPartUri), 
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
					
				} else if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {

					// MainDocument part, Styles part, Font part etc
					
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jc);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( el );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.Dom4jXmlPart) {
					
					((org.docx4j.openpackaging.parts.Dom4jXmlPart)part).setDocument( convertW3CtoDom4J(el).getDocument() );

//				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart) {
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart) {
					
					log.debug("Detected BinaryPart " + part.getClass().getName() );
					((BinaryPart)part).setBinaryData( pkgPart.getBinaryData() );	
					
				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) {

					CustomXmlDataStorage data = Load
							.getCustomXmlDataStorageClass().factory();

					// Need an inputStream
					DOMSource source = new DOMSource(el);
					StringWriter xmlAsWriter = new StringWriter();
					StreamResult result = new StreamResult(xmlAsWriter);
					TransformerFactory.newInstance().newTransformer()
							.transform(source, result);
					ByteArrayInputStream inputStream = new ByteArrayInputStream(
							xmlAsWriter.toString().getBytes("UTF-8"));

					data.unmarshal(inputStream); // Not really JAXB, that's just our method name
					((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) part)
							.setData(data);

				} else {
					// Shouldn't happen, since ContentTypeManagerImpl should
					// return an instance of one of the above, or throw an
					// Exception.
					
					log.error("No suitable part found for: " + resolvedPartUri);
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
				log.error("Part unrecognised: " + resolvedPartUri);
				part = new BinaryPart( new PartName(resolvedPartUri)); // /?
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
		
		System.out.println( 
				org.docx4j.XmlUtils.
					marshaltoString(result, suppressDeclaration, prettyprint, 
							org.docx4j.jaxb.Context.jcXmlPackage) );
	}	
	
	
}