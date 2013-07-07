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

package org.docx4j.convert.out.flatOpcXml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Output;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationships;
import org.docx4j.relationships.Relationship;
import org.w3c.dom.Document;

/**
 * Convert a Package object to org.docx4j.xmlPackage.Package
 * (ie the "pkg" single XML file format, sometimes called
 *  Flat OPC format).
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

 * @author jharrop
 *
 */
public class FlatOpcXmlCreator implements Output {
	
	private static Logger log = LoggerFactory.getLogger(FlatOpcXmlCreator.class);				
	
	public FlatOpcXmlCreator(OpcPackage p) {
		
		this.packageIn = p;
	}
		
	// The package to save
	public OpcPackage packageIn;
	
	/**
	 * This HashMap is intended to prevent loops.
	 */
	private HashMap<String, String> handled = new HashMap<String, String>();
	
	private static org.docx4j.xmlPackage.ObjectFactory factory = new org.docx4j.xmlPackage.ObjectFactory();
	
	private org.docx4j.xmlPackage.Package pkgResult;
	
	
	public org.docx4j.xmlPackage.Package get() throws Docx4JException  {		
		
		 try {

			pkgResult = factory.createPackage();
			
			// In pkg format, we don't save [Content_Types].xml

			// Start with _rels/.rels

//			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
//			  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
//			  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
//			  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
//			</Relationships>		
			
			RelationshipsPart rp = packageIn.getRelationshipsPart();
			saveRawXmlPart(rp ); 			
			
			// Now recursively 
			addPartsFromRelationships(rp );
	    
	    } catch (Exception e) {
			e.printStackTrace() ;
			if (e instanceof Docx4JException) {
				throw (Docx4JException)e;
			} else {
				throw new Docx4JException("Failed to save package", e);
			}
	    }

	    log.info("...Done!" );		

		 return pkgResult;
	}

	public void marshal(OutputStream os) throws Docx4JException {
		
		if (pkgResult==null) {
			if (packageIn==null) {
				throw new Docx4JException("No zipped package to convert to Flat OPC Package");
			} else {
				get();
			}
		}
		
		try {
			JAXBContext jc = Context.jcXmlPackage;
			Marshaller marshaller=jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				NamespacePrefixMapperUtils.setProperty(marshaller, 
						NamespacePrefixMapperUtils.getPrefixMapper());
	
			// .. marshall it 
			marshaller.marshal(pkgResult, os);				
		} catch (JAXBException e) {
			throw new Docx4JException("Couldn't marshall Flat OPC Package", e);
		}			
		
	}

//	public void  saveRawXmlPart(Part part) throws Docx4JException {
//		
//		// This is a neater signature and should be used where possible!
//		
//		
//		// Don't drop leading "/" for XmlPackage representation.
//		// It is needed if Word is to consume the result.
//		//String partName = part.getPartName().getName().substring(1);
//		
//		saveRawXmlPart(part);
//	}

	public void  saveRawXmlPart(Part part) throws Docx4JException {
		
		org.docx4j.xmlPackage.Part partResult = createRawXmlPart(part);
        pkgResult.getPart().add(partResult);
		
	}
	
	public static org.docx4j.xmlPackage.Part createRawXmlPart(Part part) throws Docx4JException {
		
		String partName = part.getPartName().getName();
        org.docx4j.xmlPackage.Part partResult = factory.createPart();
        
        if (partName.startsWith("/")) {       
        	partResult.setName(partName);
        } else {
        	partResult.setName("/" + partName);
//        	log.error("@pkg:name must start with '/', or Word 2007 won't open it");
//        	throw new Docx4JException("@pkg:name must start with '/', or Word 2007 won't open it");
        }
        String ct = part.getContentType();
        if (ct == null) {
        	// NB - Word can't consume it if the content type is not set 
        	// on the rels parts.
        	log.error("Content type not set! ");
        } else {
        	partResult.setContentType( ct );
        }
        org.docx4j.xmlPackage.XmlData dataResult = factory.createXmlData();
        partResult.setXmlData(dataResult);

		org.w3c.dom.Document w3cDoc = null;
        
		if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {

			try {
				javax.xml.parsers.DocumentBuilderFactory dbf
					= javax.xml.parsers.DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				w3cDoc = dbf.newDocumentBuilder().newDocument();
				
				((org.docx4j.openpackaging.parts.JaxbXmlPart)part).marshal( w3cDoc, 
						NamespacePrefixMapperUtils.getPrefixMapper() );
					/* Force the RelationshipsPart to be marshalled using
					 * the normal non-rels part NamespacePrefixMapper,
					 * since otherwise (because we'd be using 2 namespace
					 * prefix mappers?) we end up with errant xmlns="",
					 * which is wrong and stops Word 2007 from loading the
					 * document.
					 * 
					 * Note that xmlPackage.xsd defines:
					 * 	<xsd:complexType name="CT_XmlData">
							<xsd:sequence>
								<xsd:any processContents="skip" />
							</xsd:sequence>
					 *
					 * Note also that marshaltoString uses 
					 * just the normal non-rels part NamespacePrefixMapper,
					 * so if/when this is marshalled again, that could
					 * have been causing problems as well?? 
					 */
		        dataResult.setAny( w3cDoc.getDocumentElement() );		        
				log.info( "PUT SUCCESS: " + partName);		
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Problem saving part " + partName, e);
				throw new Docx4JException("Problem saving part " + partName, e);
			} 		        
		} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) {
		        
			try {
				dataResult.setAny(
						((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).getData().getDocument().getDocumentElement());
				log.info("PUT SUCCESS: " + partName);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Problem saving part " + partName, e);
				throw new Docx4JException("Problem saving part " + partName, e);
			} 		        
							
		} else if (part instanceof org.docx4j.openpackaging.parts.XmlPart) {

		       Document doc =  ((org.docx4j.openpackaging.parts.XmlPart)part).getDocument();		       
		       dataResult.setAny( doc.getDocumentElement() );
		       
		} else {
			// Shouldn't happen, since ContentTypeManagerImpl should
			// return an instance of one of the above, or throw an
			// Exception.
			
			log.error("PROBLEM - No suitable part found for: " + partName);
		}		
		
		return partResult;
		
	}
	
	
	/* recursively 
		(i) get each Part listed in the relationships
		(ii) add the Part to the zip file
		(iii) traverse its relationship
	*/
	public void addPartsFromRelationships(RelationshipsPart rp )
	 throws Docx4JException {
		
//		for (Iterator it = rp.iterator(); it.hasNext(); ) {
//			Relationship r = (Relationship)it.next();
//			log.info("For Relationship Id=" + r.getId() + " Source is " + r.getSource().getPartName() + ", Target is " + r.getTargetURI() );

		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.info("For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() );
		
//			if (!r.getTargetMode().equals(TargetMode.INTERNAL) ) {
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				
				// ie its EXTERNAL
				// As at 1 May 2008, we don't have a Part for these;
				// there is just the relationship.

				log.warn("Encountered external resource " + r.getTarget() 
						   + " of type " + r.getType() );
				
				// So
				continue;				
			}
			
			try {
				//String resolvedPartUri = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();

				String resolvedPartUri = URIHelper.resolvePartUri(rp.getSourceURI(), new URI(r.getTarget() ) ).toString();		
				
				// Now drop leading "/'
				resolvedPartUri = resolvedPartUri.substring(1);				
				
				// Now normalise it .. ie abc/def/../ghi
				// becomes abc/ghi
				// Maybe this isn't necessary with a zip file,
				// - ZipFile class may be smart enough to do it.
				// But it is certainly necessary in the JCR case.
//				target = (new java.net.URI(target)).normalize().toString();
//				log.info("Normalised, it is " + target );				
				
//				Document contents = getDocumentFromZippedPart( zf,  target);
				
				// TODO - if this is already in our hashmap, skip
				// to the next				
				if (!false) {
					log.info("Getting part /" + resolvedPartUri );
					
					Part part = packageIn.getParts().get(new PartName("/" + resolvedPartUri));
					
					if (part==null) {
						log.error("Part " + resolvedPartUri + " not found!");
					} else {
						log.info(part.getClass().getName() );
					}
					
					savePart(part);
					
				}
					
			} catch (Exception e) {
				throw new Docx4JException("Failed to add parts from relationships", e);				
			}
		}
		
		
	}


	/**
	 * @param out
	 * @param resolvedPartUri
	 * @param part
	 * @throws Docx4JException
	 * @throws IOException
	 */
	public void savePart(Part part)
			throws Docx4JException, IOException {
		
		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);
		
		if (handled.get(resolvedPartUri)!=null) {
			log.debug(".. duplicate save avoided .." );
			return;
		}
		
		if (part instanceof BinaryPart ) {
			log.info(".. saving binary stuff" );
			saveRawBinaryPart( part );
			
		} else {
			log.info(".. saving " );					
			saveRawXmlPart( part );
		}
		handled.put(resolvedPartUri, resolvedPartUri);		
		
		// recurse via this parts relationships, if it has any
		if (part.getRelationshipsPart()!= null ) {
			RelationshipsPart rrp = part.getRelationshipsPart();
			log.info("Found relationships " + rrp.getPartName() );
			String relPart = PartName.getRelationshipsPartName(resolvedPartUri);
			log.info("Cf constructed name " + relPart );
			
			saveRawXmlPart( rrp);
			//, "/" + relPart );  // '/' necessary for Xml Pkg format.
			addPartsFromRelationships( rrp );
		} else {
			log.info("No relationships for " + resolvedPartUri );					
		}
	}
	
	protected void saveRawBinaryPart(Part part) throws Docx4JException {

		
		org.docx4j.xmlPackage.Part partResult = createRawBinaryPart(part);
        pkgResult.getPart().add(partResult);

		
	}
	
	public static org.docx4j.xmlPackage.Part createRawBinaryPart(Part part) throws Docx4JException {
		
		String resolvedPartUri = part.getPartName().getName();
		
		// Don't drop leading "/" for XmlPackage representation.
		// It is needed if Word is to consume the result.
		//String resolvedPartUri = part.getPartName().getName().substring(1);

        org.docx4j.xmlPackage.Part partResult = factory.createPart();
        partResult.setName(resolvedPartUri);
        partResult.setContentType( part.getContentType() );

		try {

			partResult.setCompression("store");
	        
            java.nio.ByteBuffer bb = ((BinaryPart)part).getBuffer();
            byte[] bytes = null;
            bytes = new byte[bb.limit()];
            bb.get(bytes);	        

			partResult.setBinaryData( bytes );
			
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}

		log.info( "PUT SUCCESS: " + resolvedPartUri);		
		return partResult;
	}	
	
	/* It is sometimes useful to wrap a part in an appropriate pkg:part */
	public static String wrapInXmlPart(String xml, String partName, String contentType) {
		
		return  "<pkg:part pkg:name=\"" + partName + "\""  
			 				+ " pkg:contentType=\"" + contentType + "\"" 
			 				+ " xmlns:pkg=\"http://schemas.microsoft.com/office/2006/xmlPackage\">"
				  		+ "<pkg:xmlData>"
				  			+ xml
				  		+ "</pkg:xmlData>"
				  	+ "</pkg:part>";
		
	}

	public static String wrapInBinaryPart(byte[] base64, String partName, String contentType) {
		
		try {
			return "<pkg:part pkg:name=\""
					+ partName
					+ "\""
					+ " pkg:contentType=\""
					+ contentType
					+ "\""
					// + " pkg:compression=\"store\"" 
					+ " xmlns:pkg=\"http://schemas.microsoft.com/office/2006/xmlPackage\">"
					+ "<pkg:binaryData>" + new String(base64, "UTF-8")
					+ "</pkg:binaryData>" + "</pkg:part>";
		} catch (UnsupportedEncodingException e) {
			// I assume system supports UTF-8 !!
			log.error(e.getMessage(), e);
			return null;
		}
		
	}
	
	/**
	 * Return the WordML package in Flat OPC format, as a W3C DOM document
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Document getFlatDomDocument(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		
		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
		org.docx4j.xmlPackage.Package pkg = worker.get();
		
		org.w3c.dom.Document doc;
		try {
			JAXBContext jc = Context.jcXmlPackage;
			Marshaller marshaller=jc.createMarshaller();
			doc = org.docx4j.XmlUtils.neww3cDomDocument();

			marshaller.marshal(pkg, doc);
		} catch (JAXBException e) {
			throw new Docx4JException("Couldn't marshal Flat OPC to DOM", e);
		}
		
		return doc;
		
	}
	
	// Implement the interface
	public void output(javax.xml.transform.Result result) throws Docx4JException {
		
		// Do this via identity transform
		
		javax.xml.transform.TransformerFactory tfactory = XmlUtils.getTransformerFactory();
	
		try {
			Transformer serializer = tfactory.newTransformer();
			serializer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform( new DOMSource( getFlatDomDocument( (WordprocessingMLPackage)packageIn)) , result );				
		} catch (Exception e) {
			throw new Docx4JException("Failed to create Flat OPC output", e);
		} 
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/fonts-modesOfApplication.docx";
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
		
		org.docx4j.xmlPackage.Package result = worker.get();
		
		boolean suppressDeclaration = true;
		boolean prettyprint = true;
		
		System.out.println( 
				org.docx4j.XmlUtils.
					marshaltoString(result, suppressDeclaration, prettyprint, 
							org.docx4j.jaxb.Context.jcXmlPackage) );
		
		// Note - We don't bother adding:
		// 1. mso-application PI
		// 2. @padding on rels?
		// Since Word 2007 is happy to consume without either of these
		
	
	}	
	
	
}
