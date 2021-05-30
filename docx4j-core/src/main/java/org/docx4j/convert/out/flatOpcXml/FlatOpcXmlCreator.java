/*
 *  Copyright 2007-2021, Plutext Pty Ltd.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.FileUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Output;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.XmlSerializerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private HashMap<org.docx4j.xmlPackage.Part, byte[]> associatedContent 
		= new HashMap<org.docx4j.xmlPackage.Part, byte[]>();
	
	
	private static org.docx4j.xmlPackage.ObjectFactory factory = new org.docx4j.xmlPackage.ObjectFactory();
	
	private org.docx4j.xmlPackage.Package pkgResult;
		
	@Deprecated
	public org.docx4j.xmlPackage.Package get() throws Docx4JException {
	
			throw new Docx4JException("Deprecated.");
	}
	
	/**
	 * @throws Docx4JException
	 * @since 8.2.10
	 */	
	public void populate() throws Docx4JException  {		
		
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
			if (e instanceof Docx4JException) {
				throw (Docx4JException)e;
			} else {
				throw new Docx4JException("Failed to save package", e);
			}
	    }

	    log.debug("...Done!" );		
	}

	public void marshal(OutputStream os) throws Docx4JException {
		
		if (pkgResult==null) {
			if (packageIn==null) {
				throw new Docx4JException("No zipped package to convert to Flat OPC Package");
			} else {
				populate();
			}
		}

		// Word requires the namespaces to be declared in each part in
		// the Flat OPC XML.  That is, you can't just declare them once on the root element!
		
		// It used to be sufficient just to marshall the pkgResult.
		// ie no need for special processing at the package level.
		
		// But nowadays (whether because of changes to JAXB marshalling, or the fact that
		// ignorables are now in multiple parts, so it makes sense to elevate the namespaces),
		// we have to ensure the namespaces are declared in each part 

		
		try {
			
			JAXBContext jc = Context.jcXmlPackage;
			Marshaller marshaller=jc.createMarshaller();
			
			// TODO 6.1.0 JAXB_FORMATTED_OUTPUT here doesn't work.
			// Changing org/docx4j/org/apache/xml/serializer/docx4j_xalan_output_xml.properties
			// to indent=yes doesn't help either.
			// But you can get formatted output using the approach demo'd in the main method below.
			
			/*
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				NamespacePrefixMapperUtils.setProperty(marshaller, 
						NamespacePrefixMapperUtils.getPrefixMapper());
	
			// .. marshall it 
			marshaller.marshal(pkgResult, os);
			
							*/
			
			byte[] pkgPartTagClose = "</pkg:part>".getBytes();

			byte[] xmlDataTagOpen = "<pkg:xmlData>".getBytes();
			byte[] xmlDataTagClose = "</pkg:xmlData>".getBytes();
			
			os.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>".getBytes());
			if (packageIn instanceof WordprocessingMLPackage) {
				os.write("<?mso-application progid=\"Word.Document\"?>".getBytes());
			} else if (packageIn instanceof PresentationMLPackage) {
				os.write("<?mso-application progid=\"PowerPoint.Show\"?>".getBytes());
			}
			os.write("<pkg:package xmlns:pkg=\"http://schemas.microsoft.com/office/2006/xmlPackage\" >".getBytes() );
			
			for (org.docx4j.xmlPackage.Part p :  pkgResult.getPart()) {
								
				if (associatedContent.get(p)==null) {
					
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
					marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
					NamespacePrefixMapperUtils.setProperty(marshaller, 
							NamespacePrefixMapperUtils.getPrefixMapper());
		
					// .. marshall the entire pkg:part object 
					marshaller.marshal(p, os);
					
					
				} else {
					
					String pkgPartTagOpen = "<pkg:part pkg:name=\"" + p.getName() + "\" " +  "pkg:contentType=\"" + p.getContentType() + "\">";
					os.write( pkgPartTagOpen.getBytes() );
					
					os.write( xmlDataTagOpen );
					os.write(associatedContent.get(p));
					os.write( xmlDataTagClose );
					
					os.write(pkgPartTagClose);
					
				}
				
			}

			os.write("</pkg:package>".getBytes() );

		} catch (Exception e) {
			throw new Docx4JException("Couldn't marshall Flat OPC Package", e);
		}			
		
	}


	public void  saveRawXmlPart(Part part) throws Docx4JException {
		
		org.docx4j.xmlPackage.Part partResult = createRawXmlPart(part);
        pkgResult.getPart().add(partResult);
		
	}
	
	private byte[] marshalBytes(Object o, JAXBContext jc, String ignorables) {
		try {

			Marshaller marshaller = jc.createMarshaller();

			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());	
			((McIgnorableNamespaceDeclarator)NamespacePrefixMapperUtils.getPrefixMapper()).setMcIgnorable(ignorables);
			
			org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().newDocument();
			marshaller.marshal(o, doc);
			
			// For FlatOPC, always canonicalize, since we want to
			// trim namespaces to make the file as small as possible
			// (since a key use case is OpenDoPE's UpdateXmlFromDocumentSurface).
			

//			if (true /* always canonicalize! */
//					|| Docx4jProperties.getProperty("docx4j.jaxb.marshal.canonicalize", false)) {

			return XmlUtils.trimNamespaces(doc, ignorables);
				
				//log.debug(new String(bytes, "UTF-8"));
				
//				/*MOXy issue where it looks like trimNamespaces drops w namespace!
//				 * 
//					DEBUG org.docx4j.XmlUtils .trimNamespaces line 700 - Input to Canonicalizer: <w:abstractNumId xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" w:val="0"/>
//					DEBUG org.docx4j.XmlUtils .marshaltoW3CDomDocument line 903 - <w:abstractNumId w:val="0"></w:abstractNumId>
//					[Fatal Error] :1:28: The prefix "w" for element "w:abstractNumId" is not bound.	
//					
//					where in fact the real problem is a missng @XmlRootElement annotation on the parent node
//					
//						<w:num xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" w:numId="1"><w:abstractNumId w:val="0"></w:abstractNumId></w:num>					
//					
//					which Sun/Oracle reports.  Once fixed, MOXy is happy as well.
//				*/
				
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	}
	
	/*
	 * Only retained for use by AlteredParts 
	 */
	@Deprecated
	public static org.docx4j.xmlPackage.Part getRawXmlPart(Part part) throws Docx4JException {
		
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

		org.w3c.dom.Document w3cDoc = null;
        
		if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {

	        org.docx4j.xmlPackage.XmlData dataResult = factory.createXmlData();
	        partResult.setXmlData(dataResult);
			
			String mceIgnorable = ((JaxbXmlPart)part).getMceIgnorable(); 

			try {
//				w3cDoc = XmlUtils.getNewDocumentBuilder().newDocument();
//				((org.docx4j.openpackaging.parts.JaxbXmlPart)part).marshal( w3cDoc, 
//						NamespacePrefixMapperUtils.getPrefixMapper() );
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

				JaxbXmlPart jaxbXmlPart = (org.docx4j.openpackaging.parts.JaxbXmlPart)part;
				w3cDoc = marshaltoW3CDomDocument(jaxbXmlPart.getJaxbElement(), 
						jaxbXmlPart.getJAXBContext(), mceIgnorable + jaxbXmlPart.getMcChoiceNamespaces());
				
		        dataResult.setAny( w3cDoc.getDocumentElement() );		        
				log.debug( "PUT SUCCESS: " + partName);		
			} catch (Exception e) {
				log.error("Problem saving part " + partName, e);
				throw new Docx4JException("Problem saving part " + partName, e);
			}
			
			return partResult;
			
		} else {
			
			log.debug("Handling: " + partName);
			return getRawXmlPartCommon(part, partResult);
		}
	}

	private static org.docx4j.xmlPackage.Part getRawXmlPartCommon(Part part, org.docx4j.xmlPackage.Part partResult) throws Docx4JException {

        org.docx4j.xmlPackage.XmlData dataResult = factory.createXmlData();
        partResult.setXmlData(dataResult);
		
		if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) {
	        
			try {
				dataResult.setAny(
						((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).getData().getDocument().getDocumentElement());
			} catch (Exception e) {
				log.error("Problem saving part ", e);
				throw new Docx4JException("Problem saving part ", e);
			} 		        
							
		} else if (part instanceof org.docx4j.openpackaging.parts.XmlPart) {

		       Document doc =  ((org.docx4j.openpackaging.parts.XmlPart)part).getDocument();		       
		       dataResult.setAny( doc.getDocumentElement() );
		       
		} else {
			// Shouldn't happen, since ContentTypeManagerImpl should
			// return an instance of one of the above, or throw an
			// Exception.
			
			log.error("PROBLEM - No suitable part found.  " );
		}		
		
		return partResult;
		
	}
	/*
	 * Only retained for use by AlteredParts 
	 */
	@Deprecated
	private static org.w3c.dom.Document marshaltoW3CDomDocument(Object o, JAXBContext jc, String ignorables) {
		try {

			Marshaller marshaller = jc.createMarshaller();

			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());	
			((McIgnorableNamespaceDeclarator)NamespacePrefixMapperUtils.getPrefixMapper()).setMcIgnorable(ignorables);
			
			org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().newDocument();
			marshaller.marshal(o, doc);
			
			// For FlatOPC, always canonicalize, since we want to
			// trim namespaces to make the file as small as possible
			// (since a key use case is OpenDoPE's UpdateXmlFromDocumentSurface).
			
			// Word requires the namespaces to be declared in each part in
			// the Flat OPC XML.  That is, you can't just declare them once on the root element!

//			if (true /* always canonicalize! */
//					|| Docx4jProperties.getProperty("docx4j.jaxb.marshal.canonicalize", false)) {

				byte[] bytes = XmlUtils.trimNamespaces(doc, ignorables);
				
				//log.debug(new String(bytes, "UTF-8"));
				/*MOXy issue where it looks like trimNamespaces drops w namespace!
				 * 
					DEBUG org.docx4j.XmlUtils .trimNamespaces line 700 - Input to Canonicalizer: <w:abstractNumId xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" w:val="0"/>
					DEBUG org.docx4j.XmlUtils .marshaltoW3CDomDocument line 903 - <w:abstractNumId w:val="0"></w:abstractNumId>
					[Fatal Error] :1:28: The prefix "w" for element "w:abstractNumId" is not bound.	
					
					where in fact the real problem is a missng @XmlRootElement annotation on the parent node
					
						<w:num xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" w:numId="1"><w:abstractNumId w:val="0"></w:abstractNumId></w:num>					
					
					which Sun/Oracle reports.  Once fixed, MOXy is happy as well.
				*/
				DocumentBuilder builder = XmlUtils.getDocumentBuilderFactory().newDocumentBuilder();
				return builder.parse(new ByteArrayInputStream(bytes));
				
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	}	
	
	public  org.docx4j.xmlPackage.Part createRawXmlPart(Part part) throws Docx4JException {
		
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

		if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {

	        org.docx4j.xmlPackage.XmlData dataResult = factory.createXmlData();
	        partResult.setXmlData(dataResult);
			
			String mceIgnorable = ((JaxbXmlPart)part).getMceIgnorable(); 

			try {

				JaxbXmlPart jaxbXmlPart = (org.docx4j.openpackaging.parts.JaxbXmlPart)part;
				byte[] bytes = marshalBytes(jaxbXmlPart.getJaxbElement(), 
						jaxbXmlPart.getJAXBContext(), mceIgnorable + jaxbXmlPart.getMcChoiceNamespaces());
					
				// We do this to avoid wasteful marshal-unmarshall-marshal
				associatedContent.put(partResult, bytes);
				
			} catch (Exception e) {
				log.error("Problem saving part " + partName, e);
				throw new Docx4JException("Problem saving part " + partName, e);
			} 		        
		} else {
			
			log.debug("Handling: " + partName);
			return getRawXmlPartCommon(part, partResult);
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
			
			log.debug("For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() );
		
//			if (!r.getTargetMode().equals(TargetMode.INTERNAL) ) {
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				
				//log.debug("Encountered external resource " + r.getTarget() + " of type " + r.getType() );
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
					log.debug("Getting part /" + resolvedPartUri );
					
					Part part = packageIn.getParts().get(new PartName("/" + resolvedPartUri));
					
					if (part==null) {
						log.error("Part " + resolvedPartUri + " not found!");
					} else {
						log.debug(part.getClass().getName() );
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
			log.debug(".. saving binary stuff" );
			saveRawBinaryPart( part );
			
		} else {
			log.debug(".. saving " );					
			saveRawXmlPart( part );
		}
		handled.put(resolvedPartUri, resolvedPartUri);		
		
		// recurse via this parts relationships, if it has any
		if (part.getRelationshipsPart()!= null
				&& part.getRelationshipsPart().getJaxbElement()!=null
				&& part.getRelationshipsPart().getJaxbElement().getRelationship()!=null
				&& part.getRelationshipsPart().getJaxbElement().getRelationship().size()>0) {
			RelationshipsPart rrp = part.getRelationshipsPart();
			log.debug("Found relationships " + rrp.getPartName() );
			String relPart = PartName.getRelationshipsPartName(resolvedPartUri);
			log.debug("Cf constructed name " + relPart );
			
			saveRawXmlPart( rrp);
			//, "/" + relPart );  // '/' necessary for Xml Pkg format.
			addPartsFromRelationships( rrp );
		} else {
			log.debug("No relationships for " + resolvedPartUri );					
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
	        
			partResult.setBinaryData( ((BinaryPart)part).getBytes() );
			
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}

		log.debug( "PUT SUCCESS: " + resolvedPartUri);		
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
	
//	/**
//	 * Return the WordML package in Flat OPC format, as a W3C DOM document
//	 * 
//	 * @return
//	 * @throws Exception
//	 */
//	public static Document getFlatDomDocument(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
//		
//		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
//		org.docx4j.xmlPackage.Package pkg = worker.populate();
//		
//		org.w3c.dom.Document doc;
//		try {
//			JAXBContext jc = Context.jcXmlPackage;
//			Marshaller marshaller=jc.createMarshaller();
//			doc = org.docx4j.XmlUtils.neww3cDomDocument();
//
//			marshaller.marshal(pkg, doc);
//		} catch (JAXBException e) {
//			throw new Docx4JException("Couldn't marshal Flat OPC to DOM", e);
//		}
//		
//		return doc;
//		
//	}
	
	// Implement the interface
	public void output(javax.xml.transform.Result result) throws Docx4JException {
		
		// To do this we need to manually serialise
		throw new Docx4JException("Not implemented"); 
				
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
//		String inputfilepath = System.getProperty("user.dir") + "/ole_tests/OUT_wmv_f.pptx";
		String inputfilepath = System.getProperty("user.dir") + "/ole_tests/wmv_CT.pptx";
		OpcPackage wordMLPackage = OpcPackage.load(new java.io.File(inputfilepath));
		
		FlatOpcXmlCreator opcXmlCreator = new FlatOpcXmlCreator(wordMLPackage);
		opcXmlCreator.populate();
		
		
		OutputStream outStream = null; // TOTO
		opcXmlCreator.marshal(outStream);
		
		// Note - We don't bother adding:
		// 1. mso-application PI
		// 2. @padding on rels?
		// Since Word 2007 is happy to consume without either of these
		
	
	}	
	
	
}
