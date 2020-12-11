/*
 *  Copyright 2007-2012, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.io3;



import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;

import org.docx4j.Docx4jProperties;
import org.docx4j.Version;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.extended.Properties;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Save a Package object using the PartStore
 * defined on the package.
 * 
 * @author jharrop
 * @since 3.0
 */
public class Save {
	
	private static Logger log = LoggerFactory.getLogger(Save.class);				
	
	public Save(OpcPackage p) {
		
		this.p = p;
//		sourcePartStore = p.getSourcePartStore(); 
		PartStore targetPartStore;
		if (p.getSourcePartStore()==null) // eg a newly created package
		{
			log.debug("sourcePartStore undefined");
			targetPartStore = new ZipPartStore();
		} else {
			targetPartStore = p.getSourcePartStore();
			targetPartStore.setSourcePartStore(p.getSourcePartStore());
		}
		p.setTargetPartStore(targetPartStore);
		
	}

	public Save(OpcPackage p, PartStore targetPartStore) {
		
		this.p = p;
//		sourcePartStore = p.getSourcePartStore(); 
		if (p.getSourcePartStore()==null) {
			log.debug("sourcePartStore undefined");
		} else {
			targetPartStore.setSourcePartStore(p.getSourcePartStore());
		}
//		this.targetPartStore = targetPartStore;
		p.setTargetPartStore(targetPartStore);
//		p.setSourcePartStore(targetPartStore);
		
	}
	
	// The package to save
	public OpcPackage p;
	
//	private PartStore sourcePartStore;	
//	private PartStore targetPartStore;	
//
//	/**
//	 * If we're writing to a different place
//	 */
//	public void setSourcePartStore(PartStore partStore) {
//		this.sourcePartStore = partStore;
//	}
	
	
	/**
	 * This HashMap is intended to prevent loops.
	 */
	protected HashMap<String, String> handled;

//	/* Save a Package as a Zip file in the file system */
//	public boolean save(String filepath) throws Docx4JException  {
//		log.info("SAVING to " +  filepath );		
//		try {
//			if (filepath.toLowerCase().endsWith(".xml") ) {
//				return saveFlatOPC(new FileOutputStream(filepath));
//			} else return save(new FileOutputStream(filepath));
//		} catch (FileNotFoundException e) {
//			throw new Docx4JException("Failed to save package to path " + filepath, e);
//		}
//	}
//
//	/* Save a Package as a Zip file in the file system */
//	public boolean save(java.io.File docxFile) throws Docx4JException  {
//		log.info("Saving to" +  docxFile.getPath() );		
//		try {
//			if (docxFile.getPath().toLowerCase().endsWith(".xml") ) {
//				return saveFlatOPC(new FileOutputStream(docxFile));
//			} else return save(new FileOutputStream(docxFile));
//		} catch (FileNotFoundException e) {
//			throw new Docx4JException("Failed to save package to path " + docxFile.getPath(), e);
//		}
//	}
//	
//	public boolean saveFlatOPC(OutputStream realOS) throws Docx4JException {
//		
//		try {
//			FlatOpcXmlCreator worker = new FlatOpcXmlCreator(p);
//			org.docx4j.xmlPackage.Package pkg = worker.get();
//			
//			// Now marshall it
//			JAXBContext jc = Context.jcXmlPackage;
//			Marshaller marshaller=jc.createMarshaller();
//			
//			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//			NamespacePrefixMapperUtils.setProperty(marshaller, 
//					NamespacePrefixMapperUtils.getPrefixMapper());			
//			
//			marshaller.marshal(pkg, realOS);
//			return true;
//		} catch (Exception e) {
//			throw new Docx4JException("Failed to save Flat OPC ", e);
//		} 			
//		
//	}
	
	
	/**
	 * @param realOS - responsibility of the caller to close this.
	 * @return
	 * @throws Docx4JException
	 */
	public boolean save(OutputStream realOS) throws Docx4JException  {		
		handled = new HashMap<String, String>();
		 try {
			 
				p.getTargetPartStore().setOutputStream(realOS);
			 

			// 3. Save [Content_Types].xml
			ContentTypeManager ctm = p.getContentTypeManager();
			p.getTargetPartStore().saveContentTypes(ctm);
			
			// 4. Start with _rels/.rels

//			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
//			  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
//			  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
//			  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
//			</Relationships>		
			
			String partName = "_rels/.rels";
			RelationshipsPart rp = p.getRelationshipsPart();
			saveRawXmlPart(rp );
			
			// 5. Now recursively 
//			addPartsFromRelationships(out, "", rp );
			addPartsFromRelationships(rp );
	    
			
			p.getTargetPartStore().finishSave();
			
//			if (realOS!=null) {
//				realOS.close();
//			}
	    } catch (Exception e) {
			e.printStackTrace() ;
			if (e instanceof Docx4JException) {
				throw (Docx4JException)e;
			} else {
				throw new Docx4JException("Failed to save package", e);
			}
	    }

	    log.debug("...Done!" );		

		 return true;
	}


//	public void  saveRawXmlPart(Part part) throws Docx4JException {
//		
//		// This is a neater signature and should be used where possible!
//		
//		String partName = part.getPartName().getName().substring(1);
//
//		saveRawXmlPart(part, partName);
//	}
	
	public void  saveRawXmlPart(Part part) throws Docx4JException {
		
		try {
						
			if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {
				
				// From docx4j 2.7.1, support setting some basic metadata
				// from docx4j.properties
				if (part instanceof DocPropsCorePart) {
					
					boolean dcWrite= Boolean.parseBoolean(
							Docx4jProperties.getProperties().getProperty("docx4j.dc.write", "false"));
					if (dcWrite) {	
						
//						p.setSourcePartStore(sourcePartStore);
						
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
						
//						p.setSourcePartStore(targetPartStore);
						
					}
				}				
				if (part instanceof DocPropsExtendedPart) {
					boolean appWrite= Boolean.parseBoolean(
							Docx4jProperties.getProperties().getProperty("docx4j.App.write", "false"));
					if (appWrite) {
//						p.setSourcePartStore(sourcePartStore);

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

//						p.setSourcePartStore(targetPartStore);						
					}
				}

				p.getTargetPartStore().saveJaxbXmlPart(
						((JaxbXmlPart)part));
//				log.info( "success writing part: " +  zipEntryName);		
		        

			} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) {

				p.getTargetPartStore().saveCustomXmlDataStoragePart(
						(CustomXmlDataStoragePart)part);
//				log.info( "success writing part: " +  zipEntryName);		

			} else if (part instanceof org.docx4j.openpackaging.parts.XmlPart) {

				p.getTargetPartStore().saveXmlPart(
						(XmlPart)part);
				
//				log.info( "success writing part: " + zipEntryName);		
						
			} else {
				// Shouldn't happen, since ContentTypeManagerImpl should
				// return an instance of one of the above, or throw an
				// Exception.
				
				log.error("PROBLEM - No suitable part found for: " + part.getPartName());
			}		
		
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw new Docx4JException("Problem saving part " + part.getPartName(), e);
		} 
		
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
			
			if (r.getType().equals(Namespaces.HYPERLINK)) {				
				continue;  // whether internal or external								
			}
			
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				
				// ie its EXTERNAL
				// As at 1 May 2008, we don't have a Part for these;
				// there is just the relationship.

				log.debug("Encountered external resource " + r.getTarget() 
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
						throw new RuntimeException("Part " + resolvedPartUri + " not found!");
					} else {
						log.debug(part.getClass().getName() );
					}

					if (part.getPackage()==null) {
						log.warn("Part " + resolvedPartUri + " is not attached to any package");						
					} else if (!part.getPackage().equals(p)) {
						log.warn("Part " + resolvedPartUri + " is attached to some other package");
					}
					
					savePart(part);
					
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
			p.getTargetPartStore().saveBinaryPart( part );
			
		} else {
			log.debug(".. saving " );					
			saveRawXmlPart( part );
		}
		handled.put(resolvedPartUri, resolvedPartUri);
		
		// recurse via this parts relationships, if it has any
		RelationshipsPart rrp = part.getRelationshipsPart(false); //don't create
		if (rrp!= null ) {
			
			//log.debug("Found relationships " + rrp.getPartName() );
			
			// Only save it if it actually has rels in it
			if (rrp.getRelationships().getRelationship().size()>0) {
				
//				String relPart = PartName.getRelationshipsPartName(resolvedPartUri);
//				//log.debug("Cf constructed name " + relPart );
//				
//				saveRawXmlPart(rrp, relPart );

				saveRawXmlPart(rrp );
				
				addPartsFromRelationships(rrp );
			}
		} 
//		else {
//			log.debug("No relationships for " + resolvedPartUri );					
//		}
	}
	
	
	
	
}
