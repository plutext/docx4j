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

package org.docx4j.openpackaging.io;



import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.Docx4jProperties;
import org.docx4j.Version;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.extended.Properties;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * Save a Package object to a Zip file or output stream
 * 
 * Use org.docx4j.openpackaging.io3.Save instead, also 
 * available via Docx4J facade (since 3.0.2).
 * 
 * @author jharrop
 *
 */
@Deprecated
public class SaveToZipFile {
	
	private static Logger log = LoggerFactory.getLogger(SaveToZipFile.class);				
	
	public SaveToZipFile(OpcPackage p) {
		
		this.p = p;
		
	}
		
	// The package to save
	public OpcPackage p;
	
	/**
	 * This HashMap is intended to prevent loops.
	 */
	private HashMap<String, String> handled;

	/* Save a Package as a Zip file in the file system */
	public boolean save(String filepath) throws Docx4JException  {
		log.info("SAVING to " +  filepath );		
		try {
			if (filepath.toLowerCase().endsWith(".xml") ) {
				return saveFlatOPC(new FileOutputStream(filepath));
			} else return save(new FileOutputStream(filepath));
		} catch (FileNotFoundException e) {
			throw new Docx4JException("Failed to save package to path " + filepath, e);
		}
	}

	/* Save a Package as a Zip file in the file system */
	public boolean save(java.io.File docxFile) throws Docx4JException  {
		log.info("Saving to" +  docxFile.getPath() );		
		try {
			if (docxFile.getPath().toLowerCase().endsWith(".xml") ) {
				return saveFlatOPC(new FileOutputStream(docxFile));
			} else return save(new FileOutputStream(docxFile));
		} catch (FileNotFoundException e) {
			throw new Docx4JException("Failed to save package to path " + docxFile.getPath(), e);
		}
	}
	
	public boolean saveFlatOPC(OutputStream realOS) throws Docx4JException {
		
		try {
			FlatOpcXmlCreator worker = new FlatOpcXmlCreator(p);
			org.docx4j.xmlPackage.Package pkg = worker.get();
			
			// Now marshall it
			JAXBContext jc = Context.jcXmlPackage;
			Marshaller marshaller=jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
			
			marshaller.marshal(pkg, realOS);
			return true;
		} catch (Exception e) {
			throw new Docx4JException("Failed to save Flat OPC ", e);
		} 			
		
	}
	
	/* Save a Package as a Zip file in the outputstream provided */
	public boolean save(OutputStream realOS) throws Docx4JException  {		
		handled = new HashMap<String, String>();
		 try {

			ZipOutputStream out = new ZipOutputStream(realOS);
			
			
			// 3. Save [Content_Types].xml
			ContentTypeManager ctm = p.getContentTypeManager();
	        out.putNextEntry(new ZipEntry("[Content_Types].xml"));
	        ctm.marshal(out);
	        out.closeEntry();
	        
			// 4. Start with _rels/.rels

//			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
//			  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
//			  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
//			  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
//			</Relationships>		
			
			String partName = "_rels/.rels";
			RelationshipsPart rp = p.getRelationshipsPart();
			//deprecatedSaveRawXmlPart(out, partName, rp.getDocument() );
			// 2008 06 12 - try this neater method
			saveRawXmlPart(out, rp, partName );
			
			
			// 5. Now recursively 
//			addPartsFromRelationships(out, "", rp );
			addPartsFromRelationships(out, rp );
	    
			
	        // Complete the ZIP file
			// Don't forget to do this or everything will appear
			// to work, but when you open the zip file you'll get an error
			// "End-of-central-directory signature not found."
	        out.close();
	        realOS.close();
	    } catch (Exception e) {
			e.printStackTrace() ;
			if (e instanceof Docx4JException) {
				throw (Docx4JException)e;
			} else {
				throw new Docx4JException("Failed to save package", e);
			}
	    }

	    log.info("...Done!" );		

		 return true;
	}


	public void  saveRawXmlPart(ZipOutputStream out, Part part) throws Docx4JException {
		
		// This is a neater signature and should be used where possible!
		
		String partName = part.getPartName().getName().substring(1);

		saveRawXmlPart(out, part, partName);
	}
	
	public void  saveRawXmlPart(ZipOutputStream out, Part part, String zipEntryName) throws Docx4JException {
		
		try {
						
			if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {
				
				// From docx4j 2.7.1, support setting some basic metadata
				// from docx4j.properties
				if (part instanceof DocPropsCorePart) {
					
					boolean dcWrite= Boolean.parseBoolean(
							Docx4jProperties.getProperties().getProperty("docx4j.dc.write", "false"));
					if (dcWrite) {					
						CoreProperties cp = ((DocPropsCorePart)part).getJaxbElement();
						
						// Only set creator if not already present
						String creator= Docx4jProperties.getProperties().getProperty("docx4j.dc.creator.value", 
								"docx4j " + Version.getDocx4jVersion());
						if (cp.getCreator()==null
								&& part.getPackage().isNew() ) {
							org.docx4j.docProps.core.dc.elements.ObjectFactory of = new org.docx4j.docProps.core.dc.elements.ObjectFactory(); 
							cp.setCreator(of.createSimpleLiteral() );
							cp.getCreator().getContent().add(creator);
						} 
						
						String modifier= Docx4jProperties.getProperties().getProperty(
								"docx4j.dc.lastModifiedBy.value", "docx4j " + Version.getDocx4jVersion());
						cp.setLastModifiedBy(modifier);
					}
				}				
				if (part instanceof DocPropsExtendedPart) {
					boolean appWrite= Boolean.parseBoolean(
							Docx4jProperties.getProperties().getProperty("docx4j.App.write", "false"));
					if (appWrite) {
						Properties cp = ((DocPropsExtendedPart)part).getJaxbElement();
						
						// Only set if not already present (v6.1.0)
						if (cp.getApplication()==null) {
							cp.setApplication( 
									Docx4jProperties.getProperties().getProperty("docx4j.Application", "docx4j")
									);
							
							// of the form XX.YYYY where X and Y represent numerical values
							// Since -SNAPSHOT will cause Word 2010 x64 to treat the docx as corrupt! 
							// we retain docx4j.AppVersion, instead of using Version.getDocx4jVersion()
							String version = Docx4jProperties.getProperties().getProperty("docx4j.AppVersion");
							if ( version!=null ) {
								cp.setAppVersion(version);
							}
						}
						
					}
				}

		        // Add ZIP entry to output stream.
		        out.putNextEntry(new ZipEntry(zipEntryName));		        

		        ((org.docx4j.openpackaging.parts.JaxbXmlPart)part).marshal( out );
		        
		        // Complete the entry
		        out.closeEntry();
				log.info( "success writing part: " +  zipEntryName);		
		        

			} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) {

		        // Add ZIP entry to output stream.
		        out.putNextEntry(new ZipEntry(zipEntryName));		        

		        ((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).getData().writeDocument( out );
		        
		        // Complete the entry
		        out.closeEntry();
				log.info( "success writing part: " +  zipEntryName);		

			} else if (part instanceof org.docx4j.openpackaging.parts.XmlPart) {
				
		        // Add ZIP entry to output stream.
		        out.putNextEntry(new ZipEntry(zipEntryName));		        

		       Document doc =  ((org.docx4j.openpackaging.parts.XmlPart)part).getDocument();
		       
				/*
				 * With Crimson, this gives:
				 * 
					Exception in thread "main" java.lang.AbstractMethodError: org.apache.crimson.tree.XmlDocument.getXmlStandalone()Z
						at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(DOM2TO.java:373)
						at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:127)
						at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:94)
						at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transformIdentity(TransformerImpl.java:662)
						at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:708)
						at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:313)
						at org.docx4j.model.datastorage.CustomXmlDataStorageImpl.writeDocument(CustomXmlDataStorageImpl.java:174)
				 * 
				 */
				DOMSource source = new DOMSource(doc);
				 XmlUtils.getTransformerFactory().newTransformer().transform(source, 
						 new StreamResult(out) );
		       
		        
		        // Complete the entry
		        out.closeEntry();
				log.info( "success writing part: " + zipEntryName);		
						
			} else {
				// Shouldn't happen, since ContentTypeManagerImpl should
				// return an instance of one of the above, or throw an
				// Exception.
				
				log.error("PROBLEM - No suitable part found for: " + zipEntryName);
			}		
		
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw new Docx4JException("Problem saving part " + zipEntryName, e);
		} 
		
	}
	
	
	/* recursively 
		(i) get each Part listed in the relationships
		(ii) add the Part to the zip file
		(iii) traverse its relationship
	*/
	public void addPartsFromRelationships(ZipOutputStream out,  RelationshipsPart rp )
	 throws Docx4JException {
		
//		for (Iterator it = rp.iterator(); it.hasNext(); ) {
//			Relationship r = (Relationship)it.next();
//			log.info("For Relationship Id=" + r.getId() + " Source is " + r.getSource().getPartName() + ", Target is " + r.getTargetURI() );
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.debug("For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() );
			
			if (r.getType().equals(Namespaces.HYPERLINK)) {				
				continue;  // whether internal or external								
			}
			
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
				
				if (!false) {
					log.debug("Getting part /" + resolvedPartUri );
					
					//Part part = p.getParts().get(new PartName("/" + resolvedPartUri));
					Part part = rp.getPart(r);
						// 2012 09 26: If the part is actually attached to
						// a different package, using this you can still get it.
						// Use this 'feature' at your own risk!
					
					if (part==null) {
						log.error("Part " + resolvedPartUri + " not found!");
					} else {
						log.debug(part.getClass().getName() );
					}

					if (part.getPackage()==null) {
						log.warn("Packae is not set on Part " + resolvedPartUri);
					} else if (!part.getPackage().equals(p)) {
						log.warn("Part " + resolvedPartUri + " is attached to some other package");
					}
					
					savePart(out, part);
					
				}
					
			} catch (Exception e) {
				throw new Docx4JException("Failed to add parts from relationships of " + rp.getSourceP().getPartName(), e);				
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
	public void savePart(ZipOutputStream out, Part part)
			throws Docx4JException, IOException {
		
		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);
		
		if (handled.get(resolvedPartUri)!=null) {
			log.debug(".. duplicate save avoided .." );
			return;
		}
				
		if (part instanceof BinaryPart ) {
			log.debug(".. saving binary stuff" );
			saveRawBinaryPart( out, part );
			
		} else {
			log.debug(".. saving " );					
			saveRawXmlPart( out, part );
		}
		handled.put(resolvedPartUri, resolvedPartUri);
		
		// recurse via this parts relationships, if it has any
		RelationshipsPart rrp = part.getRelationshipsPart(false); //don't create
		if (rrp!= null ) {
			
			//log.debug("Found relationships " + rrp.getPartName() );
			
			// Only save it if it actually has rels in it
			if (rrp.getRelationships().getRelationship().size()>0) {
				
				String relPart = PartName.getRelationshipsPartName(resolvedPartUri);
				//log.debug("Cf constructed name " + relPart );
				
				saveRawXmlPart(out, rrp, relPart );
				
				addPartsFromRelationships(out, rrp );
			}
		} 
//		else {
//			log.debug("No relationships for " + resolvedPartUri );					
//		}
	}
	
	protected void saveRawBinaryPart(ZipOutputStream out, Part part) throws Docx4JException {

		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);

		try {
	        // Add ZIP entry to output stream.
	        out.putNextEntry(new ZipEntry(resolvedPartUri));
	        out.write( ((BinaryPart)part).getBytes() );

			// Complete the entry
	        out.closeEntry();
			
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}
		
		log.info( "success writing part: " + resolvedPartUri);		
		
	}
	
	
	
}
