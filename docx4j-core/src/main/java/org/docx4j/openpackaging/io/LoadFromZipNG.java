/*
 *  Copyright 2007-2009, Plutext Pty Ltd.
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



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import static java.lang.Math.toIntExact;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.docProps.coverPageProps.CoverPageProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.PackageRelsUtil;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.DocPropsCoverPagePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Create a Package object from a Zip file or input stream.
 * 
 * This class is a refactoring of LoadFromZipFile, which
 * couldn't read from an input stream
 * 
 * @author jharrop
 * 
 */
@Deprecated
public class LoadFromZipNG extends Load {
	
	//public HashMap<String, ByteArray> partByteArrays = new HashMap<String, ByteArray>();	
	
	private static Logger log = LoggerFactory.getLogger(LoadFromZipNG.class);

	// Testing
	public static void main(String[] args) throws Exception {
		String filepath = System.getProperty("user.dir") + "/sample-docs/FontEmbedded.docx";
		log.info("Path: " + filepath );
		LoadFromZipNG loader = new LoadFromZipNG();
		loader.get(filepath);		
	}

	 // HashMap containing the names of all the zip entries,
	// so we can tell whether there are any orphans
//	public HashMap unusedZipEntries = new HashMap();

	
	public LoadFromZipNG() {
//		this(new ContentTypeManager() );
	}

//	public LoadFromZipNG(ContentTypeManager ctm) {
//		this.ctm = ctm;
//	}
	
	
	public OpcPackage get(String filepath) throws Docx4JException {
		return get(new File(filepath));
	}
	
	public static byte[] getBytesFromInputStream(InputStream is, long size)
		throws Exception {
		
		if (size == -1) {
			
			log.debug("entry.getSize() -1");
			
			BufferedInputStream bufIn = new BufferedInputStream(is);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(baos);
			int c = bufIn.read();
			while (c != -1) {
				bos.write(c);
				c = bufIn.read();
			}
			bos.flush();
			baos.flush();		
			//bufIn.close(); //don't do that, since it closes the ZipInputStream after we've read an entry!
			bos.close();
			return baos.toByteArray();
			
        } else {
        	
        	if (log.isDebugEnabled()) {
        		log.info("entry.getSize()=" + size );
        	}
        	
        	int sizeInt = toIntExact(size);
            byte[] targetArray = new byte[sizeInt];
            int read = is.read(targetArray);
            int offset = read;
            while (read != -1 && (sizeInt-offset!=0)) {
               read = is.read(targetArray,offset, sizeInt-offset);
               offset += read;
            }
            return targetArray;
         }			
	} 			
	
	public OpcPackage get(File f) throws Docx4JException {
		log.info("Filepath = " + f.getPath() );
		
		ZipFile zf = null;
		try {
			if (!f.exists()) {
				log.info( "Couldn't find " + f.getPath() );
			}
			zf = new ZipFile(f);
		} catch (IOException ioe) {
			ioe.printStackTrace() ;
			throw new Docx4JException("Couldn't get ZipFile", ioe);
		}
				
		HashMap<String, ByteArray> partByteArrays = new HashMap<String, ByteArray>();
		Enumeration entries = zf.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			log.info( "\n\n" + entry.getName() + "\n" );
			InputStream in = null;
			try {			
				byte[] bytes =  getBytesFromInputStream( zf.getInputStream(entry), entry.getSize() );
				partByteArrays.put(entry.getName(), new ByteArray(bytes) );
			} catch (Exception e) {
				e.printStackTrace() ;
			}	
		}
		 // At this point, we've finished with the zip file
		 try {
			 zf.close();
		 } catch (IOException exc) {
			 exc.printStackTrace();
		 }
		 
		
		return process(partByteArrays);
	}

	public OpcPackage get(InputStream is) throws Docx4JException {

		HashMap<String, ByteArray> partByteArrays = new HashMap<String, ByteArray>();	
       try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
				byte[] bytes =  getBytesFromInputStream( zis, entry.getSize() );
				//log.debug("Extracting " + entry.getName());
				partByteArrays.put(entry.getName(), new ByteArray(bytes) );
            }
            zis.close();
        } catch (Exception e) {
            throw new Docx4JException("Error processing zip file (is it a zip file?)", e);
        }	
	            
		// At this point, we're finished with the zip input stream
        // TODO, so many of the below methods could be renamed.
        // If performance is ok, LoadFromJCR could be refactored to
        // work the same way
		
		return process(partByteArrays);
	}
	
	private OpcPackage process(HashMap<String, ByteArray> partByteArrays) throws Docx4JException {
		
		long startTime = System.currentTimeMillis();				

		// 2. Create a new Package
		//		Eventually, you'll also be able to create an Excel package etc
		//		but only the WordML package exists at present
		
		ContentTypeManager ctm = new ContentTypeManager();

		try {
			InputStream is = getInputStreamFromZippedPart( partByteArrays,  "[Content_Types].xml");		
			ctm.parseContentTypesFile(is);
		} catch (IOException e) {
			throw new Docx4JException("Couldn't get [Content_Types].xml from ZipFile", e);
		} catch (NullPointerException e) {
			throw new Docx4JException("Couldn't get [Content_Types].xml from ZipFile", e);
		}
		
		// .. now find the name of the main part
		RelationshipsPart rp = RelationshipsPart.createPackageRels();
		populatePackageRels(partByteArrays, rp);
		
		String mainPartName = PackageRelsUtil.getNameOfMainPart(rp);
		String pkgContentType = ctm.getContentType(new PartName("/" + mainPartName));

		// 2. Create a new Package; this'll return the appropriate subclass
		OpcPackage p = ctm.createPackage(pkgContentType);
		log.info("Instantiated package of type " + p.getClass().getName() );

		p.setRelationships(rp);
		rp.setSourceP(p); //
		
		// 5. Now recursively 
//		(i) create new Parts for each thing listed
//		in the relationships
//		(ii) add the new Part to the package
//		(iii) cross the PartName off unusedZipEntries
		addPartsFromRelationships(partByteArrays, p, rp, ctm );
		
		
		// 6. Check unusedZipEntries is empty
//		if (log.isDebugEnabled()) {
//			 Iterator myVeryOwnIterator = unusedZipEntries.keySet().iterator();
//			 while(myVeryOwnIterator.hasNext()) {
//			     String key = (String)myVeryOwnIterator.next();
//			     log.info( key + "  " + unusedZipEntries.get(key));
//			 }
//		}
		
		registerCustomXmlDataStorageParts(p);
		 
		long endTime = System.currentTimeMillis();				
		log.info("package read;  elapsed time: " + Math.round((endTime-startTime)) + " ms" );
		
		 return p;
	}
	
	private void populatePackageRels(HashMap<String, ByteArray> partByteArrays, RelationshipsPart rp) 
			throws Docx4JException {
		
		InputStream is = null;
		try {
			is =  getInputStreamFromZippedPart( partByteArrays,  "_rels/.rels");
			if (is==null) {
				throw new Docx4JException("_rels/.rels appears to be missing from this package!");
			}
			rp.unmarshal(is);
			
		} catch (Exception e) {
			throw new Docx4JException("Error getting document from Zipped Part: _rels/.rels " , e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	//private RelationshipsPart getRelationshipsPartFromZip(Base p, ZipFile zf, String partName) 
	private RelationshipsPart getRelationshipsPartFromZip(Base p, HashMap<String, ByteArray> partByteArrays, String partName) 
			throws Docx4JException {
		
		RelationshipsPart rp = null;
		
		InputStream is = null;
		try {
			is =  getInputStreamFromZippedPart( partByteArrays,  partName);
			//thePart = new RelationshipsPart( p, new PartName("/" + partName), is );
//			rp = new RelationshipsPart(new PartName("/" + partName) );
//			rp.setSourceP(p);
			rp = p.getRelationshipsPart(true);
			rp.unmarshal(is);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Docx4JException("Error getting document from Zipped Part:" + partName, e);
			
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		}
		
		return rp;
	// debugPrint(contents);
	// TODO - why don't any of the part names in this document start with "/"?
	}

	private static InputStream getInputStreamFromZippedPart(HashMap<String, ByteArray> partByteArrays,
			String partName) throws IOException {
		
        ByteArray bytes = partByteArrays.get(partName);
        if (bytes == null) throw new IOException("part '" + partName + "' not found");
		return bytes.getInputStream();
	}
	
		
	/* recursively 
	(i) create new Parts for each thing listed
	in the relationships
	(ii) add the new Part to the package
	(iii) cross the PartName off unusedZipEntries
	*/
	//private void addPartsFromRelationships(ZipFile zf, Base source, RelationshipsPart rp)
	private void addPartsFromRelationships(HashMap<String, ByteArray> partByteArrays, 
			Base source, RelationshipsPart rp, ContentTypeManager ctm)
		throws Docx4JException {
		
		OpcPackage pkg = source.getPackage();				
		
//		for (Iterator it = rp.iterator(); it.hasNext(); ) {
//			Relationship r = (Relationship)it.next();
//			log.info("For Relationship Id=" + r.getId() + " Source is " 
//					+ r.getSource().getPartName() 
//					+ ", Target is " + r.getTargetURI() );
//			try {
//				
//				getPart(zf, pkg, rp, r);
//				
//			} catch (Exception e) {
//				throw new Docx4JException("Failed to add parts from relationships", e);
//			}
//		}
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.debug("\n For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget()
					+ ", type: " + r.getType() );
					
				// This is usually the first logged comment for
				// a part, so start with a line break.
			try {				
				getPart(partByteArrays, pkg, rp, r, ctm);
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
	//private void getPart(ZipFile zf, Package pkg, RelationshipsPart rp, Relationship r)
	private void getPart(HashMap<String, ByteArray> partByteArrays, OpcPackage pkg, RelationshipsPart rp, 
			Relationship r, ContentTypeManager ctm)
			throws Docx4JException, InvalidFormatException, URISyntaxException {
		
		Base source = null;
		String resolvedPartUri = null;
		
		if (r.getType().equals(Namespaces.HYPERLINK)) {
			// Could be Internal or External
			// Example of Internal is w:drawing/wp:inline/wp:docPr/a:hlinkClick
			log.info("Encountered (but not loading) hyperlink " + r.getTarget()  );				
			return;			
		} else 
			if (r.getTargetMode() == null
				|| !r.getTargetMode().equals("External") ) {
			
			// Usual case
			
			source = rp.getSourceP();
			resolvedPartUri = URIHelper.resolvePartUri(rp.getSourceURI(), new URI(r.getTarget() ) ).toString();		

			// Now drop leading "/'
			resolvedPartUri = resolvedPartUri.substring(1);				

			// Now normalise it .. ie abc/def/../ghi
			// becomes abc/ghi
			// Maybe this isn't necessary with a zip file,
			// - ZipFile class may be smart enough to do it.
			// But it is certainly necessary in the JCR case.
//			resolvedPartUri = (new java.net.URI(resolvedPartUri)).normalize().toString();
//			log.info("Normalised, it is " + resolvedPartUri );				
			
		} else {			
			// EXTERNAL			
			if (loadExternalTargets && 
					r.getType().equals( Namespaces.IMAGE ) ) {
					// It could instead be, for example, of type hyperlink,
					// and we don't want to try to fetch that
				log.info("Loading external resource " + r.getTarget() 
						   + " of type " + r.getType() );
				BinaryPart bp = ExternalResourceUtils.getExternalResource(r.getTarget());
				pkg.getExternalResources().put(bp.getExternalTarget(), bp);			
			} else {				
				log.info("Encountered (but not loading) external resource " + r.getTarget() 
						   + " of type " + r.getType() );				
			}						
			return;
		}
		
		
		String relationshipType = r.getType();		
		Part part;
		
		if (pkg.handled.get(resolvedPartUri)!=null) {
			
			// The source Part (or Package) might have a convenience
			// method for this
			part = pkg.getParts().getParts().get(new PartName("/" + resolvedPartUri));
			if (source.setPartShortcut(part, relationshipType ) ) {
				log.debug("Convenience method established from " + source.getPartName() 
						+ " to " + part.getPartName());
			}	
			
			// v3.2.1: also note this additional source rel 
			part.getSourceRelationships().add(r);  
			
			return;
		}
		
		part = getRawPart(partByteArrays, ctm, resolvedPartUri, r); // will throw exception if null

		// The source Part (or Package) might have a convenience
		// method for this
		if (source.setPartShortcut(part, relationshipType ) ) {
			log.debug("Convenience method established from " + source.getPartName() 
					+ " to " + part.getPartName());
		}

		
		if (part instanceof BinaryPart
				|| part instanceof DefaultXmlPart) {
			// The constructors of other parts should take care of this...
			part.setRelationshipType(relationshipType);
		}
		rp.loadPart(part, r);
		pkg.handled.put(resolvedPartUri, resolvedPartUri);

		
//		unusedZipEntries.put(resolvedPartUri, new Boolean(false));
		
		RelationshipsPart rrp = getRelationshipsPart(partByteArrays, part);
		if (rrp!=null) {
			// recurse via this parts relationships, if it has any
			addPartsFromRelationships(partByteArrays, part, rrp, ctm );
//			String relPart = PartName.getRelationshipsPartName(
//					part.getPartName().getName().substring(1) );
//			unusedZipEntries.put(relPart, new Boolean(false));					
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
	//public RelationshipsPart getRelationshipsPart(ZipFile zf, Part part)
	public RelationshipsPart getRelationshipsPart(HashMap<String, ByteArray> partByteArrays, 
			Part part)
	throws Docx4JException, InvalidFormatException {
		
		RelationshipsPart rrp = null;
		// recurse via this parts relationships, if it has any
		//String relPart = PartName.getRelationshipsPartName(target);
		String relPart = PartName.getRelationshipsPartName(
				part.getPartName().getName().substring(1) );
		
		if (partByteArrays.get(relPart) !=null ) {
			log.debug("Found relationships " + relPart );
			rrp = getRelationshipsPartFromZip(part,  partByteArrays,  relPart);
			part.setRelationships(rrp);
		} else {
			log.debug("No relationships " + relPart );	
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
	 * @param partByteArrays
	 * @param ctm
	 * @param resolvedPartUri
	 * @param rel
	 * @return
	 * @throws Docx4JException including if result is null
	 */
	public static Part getRawPart(HashMap<String, ByteArray> partByteArrays,
			ContentTypeManager ctm, String resolvedPartUri, Relationship rel)	
			throws Docx4JException {
		
		Part part = null;
		
		InputStream is = null;
		try {
			try {
				log.debug("resolved uri: " + resolvedPartUri);
				is = getInputStreamFromZippedPart( partByteArrays,  resolvedPartUri);
				
				// Get a subclass of Part appropriate for this content type	
				// This will throw UnrecognisedPartException in the absence of
				// specific knowledge. Hence it is important to get the is
				// first, as we do above.
				part = ctm.getPart("/" + resolvedPartUri, rel);				

				log.info("ctm returned " + part.getClass().getName() );
				
				if (part instanceof org.docx4j.openpackaging.parts.ThemePart) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcThemePart);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsCorePart ) {

						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsCore);
						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
						
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart ) {

						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsCustom);
						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
						
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart ) {

						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsExtended);
						((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart ) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcCustomXmlProperties);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );

//				} else if (part instanceof org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart ) {
//
//					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcXmlDSig);
//					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part.getClass().getName().equals("org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart") ) {

//					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcXmlDSig);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );					
					
				} else if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {

					// MainDocument part, Styles part, Font part etc
					
					//((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jc);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart) {
					
					log.debug("Detected BinaryPart " + part.getClass().getName() );
					((BinaryPart)part).setBinaryData(is);

				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart ) {
					
					// Is it a part we know?
					try {
						
				        XMLInputFactory xif = XMLInputFactory.newInstance();
				        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
				        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
				        XMLStreamReader xsr = xif.createXMLStreamReader(is);									
						
						Unmarshaller u = Context.jc.createUnmarshaller();
						Object o = u.unmarshal( xsr );						
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
							
							log.error("TODO: handle known CustomXmlPart part  " + o.getClass().getName());

							CustomXmlDataStorage data = getCustomXmlDataStorageClass().factory();					
							is.reset();
							data.setDocument(is); // Not necessarily JAXB, that's just our method name
							((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).setData(data);						
							
						}
						
					} catch (javax.xml.bind.UnmarshalException ue) {

						log.warn("No JAXB model for this CustomXmlDataStorage part; " + ue.getMessage()  );
						
						CustomXmlDataStorage data = getCustomXmlDataStorageClass().factory();	
						is.reset();
						data.setDocument(is); // Not necessarily JAXB, that's just our method name
						((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).setData(data);						
					}					

				} else if (part instanceof org.docx4j.openpackaging.parts.XmlPart ) {
					
//					try {
						((XmlPart)part).setDocument(is);
						
					// Experimental 22/6/2011; don't fall back to binary (which we used to) 
						
//					} catch (Docx4JException d) {
//						// This isn't an XML part after all,
//						// even though ContentTypeManager detected it as such
//						// So get it as a binary part
//						part = getBinaryPart(partByteArrays, ctm, resolvedPartUri);
//						log.warn("Could not parse as XML, so using BinaryPart for " 
//								+ resolvedPartUri);						
//						((BinaryPart)part).setBinaryData(is);
//					}
					
				} else {
					// Shouldn't happen, since ContentTypeManagerImpl should
					// return an instance of one of the above, or throw an
					// Exception.
					
					log.error("No suitable part found for: " + resolvedPartUri);
					part = null;					
				}
			
			} catch (PartUnrecognisedException e) {
				log.error("PartUnrecognisedException shouldn't happen anymore!", e);
				// Try to get it as a binary part
				part = getBinaryPart(partByteArrays, ctm, resolvedPartUri);
				log.warn("Using BinaryPart for " + resolvedPartUri);
				
				((BinaryPart)part).setBinaryData(is);
			}
		} catch (Exception ex) {
			// IOException, URISyntaxException
			ex.printStackTrace();
			throw new Docx4JException("Failed to getPart", ex);			
			
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		}
		
        if (part == null) {
            throw new Docx4JException("cannot find part " + resolvedPartUri + " from rel "+ rel.getId() + "=" + rel.getTarget());
        }
		
		return part;
	}
	
	//public static Part getBinaryPart(ZipFile zf, ContentTypeManager ctm, String resolvedPartUri)
	public static Part getBinaryPart(HashMap<String, ByteArray> partByteArrays, 
			ContentTypeManager ctm, String resolvedPartUri)	
			throws Docx4JException {

		Part part = null;
		InputStream in = null;					
		try {			
			//in = zf.getInputStream( zf.getEntry(resolvedPartUri ) );
			in = partByteArrays.get(resolvedPartUri).getInputStream();
			part = new BinaryPart( new PartName("/" + resolvedPartUri));
			
			// Set content type
			part.setContentType(
					new ContentType(
							ctm.getContentType(new PartName("/" + resolvedPartUri)) ) );
			
			((BinaryPart)part).setBinaryData(in);
			log.info("Stored as BinaryData" );
			
		} catch (Exception ioe) {
			ioe.printStackTrace() ;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		}
		return part;
	}	
		
	public static class ByteArray implements Serializable {
		
		private static final long serialVersionUID = -784146312250361899L;
		// 4469266984448028582L; 
		
		private byte[] bytes;
		public byte[] getBytes() {
			return bytes;
		}

		private String mimetype;
		public String getMimetype() {
			return mimetype;			
		}
		
		public ByteArray(byte[] bytes) {
			this.bytes = bytes;
			//log.info("Added " + bytes.length  );
		}
		
		
		public ByteArray(ByteBuffer bb, String mimetype ) {
			
			bb.clear();
			bytes = new byte[bb.capacity()];
			bb.get(bytes, 0, bytes.length);
			
			this.mimetype = mimetype;
		}
		
		
		public InputStream getInputStream() {
			return new ByteArrayInputStream(bytes);
		}

		public int getLength() {
			return bytes.length;			
		}
		
	}
	
	
	
}
