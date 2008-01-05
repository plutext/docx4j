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

package org.docx4j.openpackaging.io;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;

import org.docx4j.jaxbcontexts.DocumentContext;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Relationship;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.samples.DemoCore;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import org.apache.log4j.Logger;


/**
 * Create a Package object from a Zip file.
 * 
 * @author jharrop
 *
 */
public class LoadFromZipFile extends Load {
	
	private static Logger log = Logger.getLogger(LoadFromZipFile.class);

	// Testing
	public static void main(String[] args) throws Exception {
		DemoCore demoCore = new DemoCore();
		String filepath = demoCore.getTestRootPath() + "sample.docx";
		LoadFromZipFile loader = new LoadFromZipFile();
		loader.get(filepath);		
	}

	 // HashMap containing the names of all the zip entries,
	// so we can tell whether there are any orphans
	public HashMap unusedZipEntries = null;
	
	public LoadFromZipFile() {
		this(new ContentTypeManagerImpl() );
	}

	public LoadFromZipFile(ContentTypeManager ctm) {
		this.ctm = ctm;
	}
	
	
	public Package get(String filepath) throws Docx4JException {
		return get(new File(filepath));
	}
	
	public Package get(File f) throws Docx4JException {
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
		
		//dumpZipFileContents(zf);

		// 1. The idea is to walk the tree of relationships, getting
//		everything we need from the zip file.  But I'd like to know
//		whether there are any orphans, so first we make
//		a HashMap containing the names of all the zip file 
//		entries, so we can tick them off.
		
		unusedZipEntries = new HashMap();
		Enumeration entries = zf.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			unusedZipEntries.put(entry.getName(), new Boolean(true) );	
		}
		
		// 2. Create a new Package
		//		Eventually, you'll also be able to create an Excel package etc
		//		but only the WordML package exists at present
		
		Document ctmDocument = null;
		try {
			ctmDocument = deprecatedGetDocumentFromZippedPart(zf, "[Content_Types].xml");
		} catch (Exception e) {
			// Shouldn't happen
			throw new Docx4JException("Couldn't get [Content_Types].xml", e);
		}
		debugPrint(ctmDocument);
		ctm.parseContentTypesFile(ctmDocument);		
		Package p = ctm.createPackage();
		
		// 3. Get [Content_Types].xml
//		Once we've got this, then we can look up the content type for
//		each PartName, and use it in the Part constructor.
		p.setContentTypeManager(ctm);
		unusedZipEntries.put("[Content_Types].xml", new Boolean(false));
		
		// 4. Start with _rels/.rels

//		<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
//		  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
//		  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
//		  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
//		</Relationships>		
		
		String partName = "_rels/.rels";
		RelationshipsPart rp = getRelationshipsPartFromZip(p, zf,  partName);		
		p.setRelationships(rp);
		//rp.setPackageRelationshipPart(true);		
		unusedZipEntries.put(partName, new Boolean(false));
		
		
		log.info( "Object created for: " + partName);
		//log.info( rp.toString());
		
		// 5. Now recursively 
//		(i) create new Parts for each thing listed
//		in the relationships
//		(ii) add the new Part to the package
//		(iii) cross the PartName off unusedZipEntries
		addPartsFromRelationships(zf, p, rp );
		
		
		// 6. Check unusedZipEntries is empty
		 Iterator myVeryOwnIterator = unusedZipEntries.keySet().iterator();
		 while(myVeryOwnIterator.hasNext()) {
		     String key = (String)myVeryOwnIterator.next();
		     log.info( key + "  " + unusedZipEntries.get(key));
		 }
		 
		 return p;
		
	}
	
	private RelationshipsPart getRelationshipsPartFromZip(Base p, ZipFile zf, String partName) 
			throws Docx4JException {
//			Document contents = null;
//			try {
//				contents = getDocumentFromZippedPart( zf,  partName);
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new Docx4JException("Error getting document from Zipped Part", e);
//				
//			} 
//		// debugPrint(contents);
//		// TODO - why don't any of the part names in this document start with "/"?
//		return new RelationshipsPart( p, new PartName("/" + partName), contents );	
		try {
			InputStream is =  getInputStreamFromZippedPart( zf,  partName);
			return new RelationshipsPart( p, new PartName("/" + partName), is );	
		} catch (Exception e) {
			e.printStackTrace();
			throw new Docx4JException("Error getting document from Zipped Part:" + partName, e);
			
		} 
	// debugPrint(contents);
	// TODO - why don't any of the part names in this document start with "/"?
	}

	private static InputStream getInputStreamFromZippedPart(ZipFile zf, String partName) 
	throws DocumentException, IOException {
	
	InputStream in = null;
	in = zf.getInputStream( zf.getEntry(partName ) );
	return in;		
}
	
	
	private static Document deprecatedGetDocumentFromZippedPart(ZipFile zf, String partName) 
		throws DocumentException, IOException {
		
		InputStream in = null;
		in = zf.getInputStream( zf.getEntry(partName ) );
		SAXReader xmlReader = new SAXReader();
		Document contents = null;
		try {
			contents = xmlReader.read(in);
		} catch (DocumentException e) {
			// Will land here for binary files eg gif file
			// These do get handled ..
			log.error("DocumentException on " + partName + " . Check this is binary content."); 
			//e.printStackTrace() ;
			throw e;
		}
		return contents;		
	}
	
	/* recursively 
	(i) create new Parts for each thing listed
	in the relationships
	(ii) add the new Part to the package
	(iii) cross the PartName off unusedZipEntries
	*/
	private void addPartsFromRelationships(ZipFile zf, Base source, RelationshipsPart rp)
		throws Docx4JException {
		
		Package pkg = source.getPackage();				
		
		for (Iterator it = rp.iterator(); it.hasNext(); ) {
			Relationship r = (Relationship)it.next();
			log.info("For Relationship Id=" + r.getId() + " Source is " 
					+ r.getSource().getPartName() 
					+ ", Target is " + r.getTargetURI() );
			try {
				
				getPart(zf, pkg, rp, r);
				
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
//	private void getPart(ZipFile zf, Base source, 
//			Package pkg, String resolvedPartUri, String relationshipType)
	private void getPart(ZipFile zf, Package pkg, RelationshipsPart rp, Relationship r)
			throws Docx4JException, InvalidFormatException {
		
		Base source = r.getSource();
		String resolvedPartUri = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();		

		// Now drop leading "/'
		resolvedPartUri = resolvedPartUri.substring(1);				

		// Now normalise it .. ie abc/def/../ghi
		// becomes abc/ghi
		// Maybe this isn't necessary with a zip file,
		// - ZipFile class may be smart enough to do it.
		// But it is certainly necessary in the JCR case.
//		resolvedPartUri = (new java.net.URI(resolvedPartUri)).normalize().toString();
//		log.info("Normalised, it is " + resolvedPartUri );				
		
		String relationshipType = r.getRelationshipType();		
			
		Part part = getRawPart(zf, ctm, resolvedPartUri);
		rp.loadPart(part);

		// The source Part (or Package) might have a convenience
		// method for this
		if (source.setPartShortcut(part, relationshipType ) ) {
			log.info("Convenience method established from " + source.getPartName() 
					+ " to " + part.getPartName());
		}
		
		unusedZipEntries.put(resolvedPartUri, new Boolean(false));
		log.info(".. added." );
		
		RelationshipsPart rrp = getRelationshipsPart(zf, part);
		if (rrp!=null) {
			// recurse via this parts relationships, if it has any
			addPartsFromRelationships(zf, part, rrp );
			String relPart = PartName.getRelationshipsPartName(
					part.getPartName().getName().substring(1) );
			unusedZipEntries.put(relPart, new Boolean(false));					
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
	public RelationshipsPart getRelationshipsPart(ZipFile zf, Part part)
	throws Docx4JException, InvalidFormatException {
		RelationshipsPart rrp = null;
		// recurse via this parts relationships, if it has any
		//String relPart = PartName.getRelationshipsPartName(target);
		String relPart = PartName.getRelationshipsPartName(
				part.getPartName().getName().substring(1) );
		
		if (zf.getEntry(relPart) !=null ) {
			log.info("Found relationships " + relPart );
			log.info("Recursing ... " );
			rrp = getRelationshipsPartFromZip(part,  zf,  relPart);
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
	public static Part getRawPart(ZipFile zf, ContentTypeManager ctm, String resolvedPartUri)
			throws Docx4JException {
		Part part = null;
		
		try {
			try {

				// Get a subclass of Part appropriate for this content type				
				part = ctm.getPart("/" + resolvedPartUri);
				
				InputStream is = getInputStreamFromZippedPart( zf,  resolvedPartUri);

				if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(DocumentContext.jc);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.DomXmlPart) {
					
					((org.docx4j.openpackaging.parts.DomXmlPart)part).setDocument( is );
					
				} else {
					// Shouldn't happen, since ContentTypeManagerImpl should
					// return an instance of one of the above, or throw an
					// Exception.
					
					log.error("No suitable part found for: " + resolvedPartUri);
					return null;					
				}
			
			} catch (DocumentException e) {

				// Try to get it as a binary part
				return getBinaryPart(zf, ctm, resolvedPartUri);
				
			}
		} catch (Exception ex) {
			// IOException, URISyntaxException
			ex.printStackTrace();
			throw new Docx4JException("Failed to getPart", ex);			
		} 
		return part;
	}
	
	public static Part getBinaryPart(ZipFile zf, ContentTypeManager ctm, String resolvedPartUri)
			throws Docx4JException {

		Part part = null;
		InputStream in = null;					
		try {			
			in = zf.getInputStream( zf.getEntry(resolvedPartUri ) );
			part = new BinaryPart( new PartName("/" + resolvedPartUri));
			
			((BinaryPart)part).setBinaryData(in);
			log.info("Stored as BinaryData" );
			
		} catch (IOException ioe) {
			ioe.printStackTrace() ;
		}	
		return part;
	}	
	
	private void dumpZipFileContents(ZipFile zf) {
		Enumeration entries = zf.entries();
		// Enumerate through the Zip entries until we find the one named
		// '[Content_Types].xml'.
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			log.info( "\n\n" + entry.getName() + "\n" );
			InputStream in = null;
			try {			
				in = zf.getInputStream(entry);
			} catch (IOException e) {
				e.printStackTrace() ;
			}				
			SAXReader xmlReader = new SAXReader();
			Document xmlDoc = null;
			try {
				xmlDoc = xmlReader.read(in);
			} catch (DocumentException e) {
				// Will land here for binary files eg gif file
				e.printStackTrace() ;
			}
			debugPrint(xmlDoc);
			
		}
		
	}
	
	
	
	
	
}