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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

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
import org.docx4j.openpackaging.samples.DemoCore;

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
	
	public LoadFromZipFile() {
		this(new ContentTypeManagerImpl() );
	}

	public LoadFromZipFile(ContentTypeManager ctm) {
		this.ctm = ctm;
	}
	
	
	public Package get(String filepath) throws InvalidFormatException {
		return get(new File(filepath));
	}
	
	public Package get(File f) throws InvalidFormatException {
		log.info("Filepath = " + f.getPath() );
		
		ZipFile zf = null;
		try {
			if (!f.exists()) {
				log.info( "Couldn't find " + f.getPath() );
			}
			zf = new ZipFile(f);
		} catch (IOException ioe) {
			ioe.printStackTrace() ;
		}
		
		//dumpZipFileContents(zf);

		// 1. The idea is to walk the tree of relationships, getting
//		everything we need from the zip file.  But I'd like to know
//		whether there are any orphans, so first we make
//		a HashMap containing the names of all the zip file 
//		entries, so we can tick them off.
		
		HashMap unusedZipEntries = new HashMap();
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
			ctmDocument = getDocumentFromZippedPart(zf, "[Content_Types].xml");
		} catch (DocumentException e) {
			// Shouldn't happen
			e.printStackTrace();
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
		rp.setPackageRelationshipPart(true);		
		unusedZipEntries.put(partName, new Boolean(false));
		
		
		log.info( "Object created for: " + partName);
		//log.info( rp.toString());
		
		// 5. Now recursively 
//		(i) create new Parts for each thing listed
//		in the relationships
//		(ii) add the new Part to the package
//		(iii) cross the PartName off unusedZipEntries
		addPartsFromRelationships(zf, p, rp, unusedZipEntries );
		
		
		// 6. Check unusedZipEntries is empty
		 Iterator myVeryOwnIterator = unusedZipEntries.keySet().iterator();
		 while(myVeryOwnIterator.hasNext()) {
		     String key = (String)myVeryOwnIterator.next();
		     log.info( key + "  " + unusedZipEntries.get(key));
		 }
		 
		 return p;
		
	}
	
	private RelationshipsPart getRelationshipsPartFromZip(Base p, ZipFile zf, String partName) 
			throws InvalidFormatException {
			Document contents = null;
			try {
				contents = getDocumentFromZippedPart( zf,  partName);
			} catch (DocumentException e) {
				// Shouldn't happen, since this is an XML part.
				e.printStackTrace();
			} 
		// debugPrint(contents);
		// TODO - why don't any of the part names in this document start with "/"?
		return new RelationshipsPart( p, new PartName("/" + partName), contents );	
	}
	
	private Document getDocumentFromZippedPart(ZipFile zf, String partName) 
		throws DocumentException {
		
		InputStream in = null;
		try {			
			in = zf.getInputStream( zf.getEntry(partName ) );
		} catch (IOException e) {
			e.printStackTrace() ;
		}				
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
	private void addPartsFromRelationships(ZipFile zf, Base source, RelationshipsPart rp,
			HashMap unusedZipEntries ) {
		
		Package pkg = source.getPackage();				
		
		for (Iterator it = rp.iterator(); it.hasNext(); ) {
			Relationship r = (Relationship)it.next();
			log.info("For Relationship Id=" + r.getId() + " Source is " 
					+ r.getSource().getPartName() 
					+ ", Target is " + r.getTargetURI() );
			try {
				String target = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();
				
				// Now drop leading "/'
				target = target.substring(1);				
				
				// Now normalise it .. ie abc/def/../ghi
				// becomes abc/ghi
				// Maybe this isn't necessary with a zip file,
				// - ZipFile class may be smart enough to do it.
				// But it is certainly necessary in the JCR case.
				target = (new java.net.URI(target)).normalize().toString();
				log.info("Normalised, it is " + target );				

				Part part = null;
				
				try {
				
					Document contents = getDocumentFromZippedPart( zf,  target);
	
					// Get a subclass of Part appropriate for this content type				
					part = ctm.getPart("/" + target);
					part.setDocument(contents );	
				
				} catch (DocumentException e) {
					// Deal with:
//					23.07.2007 15:30:37 *INFO * LoadFromJCR: Fetching word%2FattachedToolbars.bin (LoadFromJCR.java, line 212)
//					org.dom4j.DocumentException: Error on line 1 of document  : Content is not allowed in prolog. Nested exception: Content is not allowed in prolog.
//						at org.dom4j.io.SAXReader.read(SAXReader.java:482)
//						at org.dom4j.io.SAXReader.read(SAXReader.java:343)
//						at au.com.xn.openpackaging.io.LoadFromJCR.getDocumentFromJCRPart(LoadFromJCR.java:242)
					
					// Untested in the zip case as of 20071113					
					
					InputStream in = null;					
					try {			
						in = zf.getInputStream( zf.getEntry(target ) );
						part = new BinaryPart( new PartName("/" + target));
						
						((BinaryPart)part).setBinaryData(in);
						log.info("Stored as BinaryData" );
						
					} catch (IOException ioe) {
						ioe.printStackTrace() ;
					}				
					
				}
				
				pkg.addPart(part);
				part.setPackage(pkg); 
				
				// The source Part (or Package) might have a convenience
				// method for this
				if (source.setPartShortcut(part, r.getRelationshipType() ) ) {
					log.info("Convenience method established from " + source.getPartName() 
							+ " to " + part.getPartName());
				}
				
				unusedZipEntries.put(target, new Boolean(false));
				log.info(".. added." );
				
				// recurse via this parts relationships, if it has any
				String relPart = PartName.getRelationshipsPartName(target);
				if (zf.getEntry(relPart) !=null ) {
					log.info("Found relationships " + relPart );
					log.info("Recursing ... " );
					RelationshipsPart rrp = getRelationshipsPartFromZip(part,  zf,  relPart);
					part.setRelationships(rrp);
					unusedZipEntries.put(relPart, new Boolean(false));					
					addPartsFromRelationships(zf, part, rrp, unusedZipEntries );
				} else {
					log.info("No relationships " + relPart );					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
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