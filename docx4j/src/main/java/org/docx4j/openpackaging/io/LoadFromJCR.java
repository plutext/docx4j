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


import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.TransientRepository;

import org.apache.log4j.Logger;

import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Relationship;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;


/**
 * Create a Package object from JCR.  Note that
 * the package is assumed to be 'unzipped' in JCR.
 * That is, each of its components are discrete
 * nodes.  Typically, something is required at 
 * the JCR end to ensure that this happened when
 * the docx was saved to JCR.
 * 
 * @author jharrop
 *
 */
public class LoadFromJCR extends Load {
	
	private static Logger log = Logger.getLogger(LoadFromJCR.class);	

	// Testing
	public static void main(String[] args) throws Exception {

        Repository repository = new TransientRepository();
        Session jcrSession = repository.login(
                new SimpleCredentials("username", "password".toCharArray()));

        // You'll need to run SaveToJCR for this to exist
        String nodePath = "customers/contoso/docs/exampledoc1";
        
        try {
		
    		LoadFromJCR loader = new LoadFromJCR();
    		loader.get(jcrSession, nodePath);	
        	
        } finally {
        	jcrSession.logout();
        }
        	
	}
	
	
	public LoadFromJCR() {
		this(new ContentTypeManagerImpl() );
	}

	public LoadFromJCR(ContentTypeManager ctm) {
		this.ctm = ctm;
	}

	
	public Package get(Session jcrSession, String nodePath) {
        try {
            Node root = jcrSession.getRootNode();
            Node docxNode = root.getNode(nodePath);
            return get(jcrSession, docxNode);
	    } catch (Exception e) {
			e.printStackTrace() ;
			return null;
	    }
	}
	
	public Package get(Session jcrSession, Node docxNode ) {

		Package p = null;
		
		// 1. The idea is to walk the tree of relationships, getting
//		everything we need from JCR.  But I'd like to know
//		whether there are any orphans, so first we make
//		a HashMap containing the names of all the nodes,
		// so we can tick them off.
		
		HashMap unusedJCRNodes = new HashMap();
        try {
	        Node docxContentNode = docxNode.getNode("jcr:content");

            // Its a flat structure since we've URL Encoded '/'
            NodeIterator nodeIterator = docxContentNode.getNodes();
	        while(nodeIterator.hasNext()) {
	            Node n=nodeIterator.nextNode();
	            log.info(n.getName());
				unusedJCRNodes.put(decodeSlashes(n.getName()), new Boolean(true) );				
	        }		
		
			// 2. Create a new Package
	//		Eventually, you'll only be able to create an Excel package etc
	//		but only the WordML package exists at present
			Document ctmDocument = getDocumentFromJCRPart(jcrSession, docxContentNode, "[Content_Types].xml");
			debugPrint(ctmDocument);
			ctm.parseContentTypesFile(ctmDocument);
			p = ctm.createPackage();
						
			
			// 3. Get [Content_Types].xml
	//		Once we've got this, then we can look up the content type for
	//		each PartName, and use it in the Part constructor.
			p.setContentTypeManager(ctm);
			unusedJCRNodes.put("[Content_Types].xml", new Boolean(false));
			
			// 4. Start with _rels/.rels
	
	//		<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
	//		  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
	//		  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
	//		  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
	//		</Relationships>		
			
			String partName = "_rels/.rels";
			RelationshipsPart rp = getRelationshipsPartFromJCR(p, jcrSession, docxContentNode,  partName);
			p.setRelationships(rp);
			rp.setPackageRelationshipPart(true);
			unusedJCRNodes.put(partName, new Boolean(false));
			
			
			log.info( "Object created for: " + partName);
			//log.info( rp.toString());
			
			// 5. Now recursively 
	//		(i) create new Parts for each thing listed
	//		in the relationships
	//		(ii) add the new Part to the package
	//		(iii) cross the PartName off unusedJCRNodes
			addPartsFromRelationships(jcrSession, docxContentNode, p, rp, unusedJCRNodes );
			
			
			// 6. Check unusedJCRNodes is empty
			 Iterator myVeryOwnIterator = unusedJCRNodes.keySet().iterator();
			 while(myVeryOwnIterator.hasNext()) {
			     String key = (String)myVeryOwnIterator.next();
			     log.info( key + "  " + unusedJCRNodes.get(key));
			 }
			 
			 
	    } catch (Exception e) {
			e.printStackTrace() ;
	    }
	    
	 return p;
		
	}
	
	public RelationshipsPart getRelationshipsPartFromJCR(Base p, Session jcrSession, Node docxNode, String partName) 
			throws InvalidFormatException {
		Document contents=null;
		try {
			contents = getDocumentFromJCRPart( jcrSession, docxNode,  partName);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("\n\n" + partName + "\n ===================");
		debugPrint(contents);		
		
		// TODO - why don't any of the part names in this document start with "/"?
		return new RelationshipsPart(p, new PartName("/" + partName), contents );
	
	}

	// TODO - put this (and its duplicate in SaveToJCR) somewhere
	// more sensible
	public static String encodeSlashes( String partName) {
		
//		log.info( "Incoming: " + partName);
//		log.info( "Encoded as: " + java.net.URLEncoder.encode( partName ));
		
		return java.net.URLEncoder.encode( partName );
		
	}
	
	public String decodeSlashes( String partName) {
		
//		log.info( "Incoming: " + partName);
//		log.info( "Decoded as: " + java.net.URLDecoder.decode( partName ));
		
		return java.net.URLDecoder.decode( partName );
		
	}
	
	public static Document getDocumentFromJCRPart(Session jcrSession, Node docxNode, String partName) 
		throws DocumentException {
		
		InputStream in = null;
		try {
			try {
				log.info("Fetching " + encodeSlashes(partName));
				Node fileNode = docxNode.getNode(encodeSlashes(partName));
				Node contentNode = fileNode.getNode("jcr:content");
				
				Property jcrData = contentNode.getProperty("jcr:data");
				in = jcrData.getStream();
			} catch (PathNotFoundException e) {
				e.printStackTrace();				
			}
		} catch (RepositoryException e) {
			e.printStackTrace();				
		}
		
		SAXReader xmlReader = new SAXReader();
		Document contents = null;
		try {
			contents = xmlReader.read(in);
//			log.info("\n\n" + partName + "\n ===================");
//			debugPrint(contents);
			
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
	(iii) cross the PartName off unusedJCRNodes
	*/
	private void addPartsFromRelationships(Session jcrSession, 
			Node docxNode, Base source, RelationshipsPart rp,
			HashMap unusedJCRNodes ) {
		
		Package pkg = source.getPackage();		
		
		for (Iterator it = rp.iterator(); it.hasNext(); ) {
			Relationship r = (Relationship)it.next();
			log.info("For Relationship Id=" + r.getId() 
					+ " Source is " + r.getSource().getPartName() 
					+ ", Target is " + r.getTargetURI() );
			try {
				String target = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();
				
				// Now drop leading "/'
				target = target.substring(1);				

				// Now normalise it .. ie abc/def/../ghi
				// becomes abc/ghi
//				target = (new java.net.URI(target)).normalize().toString();
//				log.info("Normalised, it is " + target );				
				
				Part part = null;
				try {
					Document contents = getDocumentFromJCRPart( jcrSession, 
							docxNode,  target);
					
					log.info("Root node is: " + contents.getRootElement().getName());					

						// Get a subclass of Part appropriate for this content type
//						if (parentRef==null) {
							// Usual case
							part = ctm.getPart("/" + target); 
//						} else {
//							// since the Target might look like ../customXml/item1.xml
//							part = ctm.getPart(parentRef); 
//						}
						part.setDocument(contents );					
					
				} catch (DocumentException e) {
					// Deal with:
//					23.07.2007 15:30:37 *INFO * LoadFromJCR: Fetching word%2FattachedToolbars.bin (LoadFromJCR.java, line 212)
//					org.dom4j.DocumentException: Error on line 1 of document  : Content is not allowed in prolog. Nested exception: Content is not allowed in prolog.
//						at org.dom4j.io.SAXReader.read(SAXReader.java:482)
//						at org.dom4j.io.SAXReader.read(SAXReader.java:343)
//						at au.com.xn.openpackaging.io.LoadFromJCR.getDocumentFromJCRPart(LoadFromJCR.java:242)					
					InputStream in = null;
					try {
						log.info("Fetching " + encodeSlashes(target));
						Node fileNode = docxNode.getNode(encodeSlashes(target));
						Node contentNode = fileNode.getNode("jcr:content");
						
						Property jcrData = contentNode.getProperty("jcr:data");
						in = jcrData.getStream();
						
						part = new BinaryPart( new PartName("/" + target));
						
						((BinaryPart)part).setBinaryData(in);
						log.info("Stored as BinaryData" );
						
					} catch (RepositoryException re) {
						re.printStackTrace();				
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
				
				unusedJCRNodes.put(target, new Boolean(false));
				log.info(".. added part '" + part.getPartName() + "'" );
				
				// recurse via this parts relationships, if it has any
				String relPart = PartName.getRelationshipsPartName(target);
				try {
					docxNode.getNode(encodeSlashes(relPart)); // if null, 
					//throws javax.jcr.PathNotFoundException
					
					log.info("Found relationships " + relPart );
					log.info("Recursing ... " );
					RelationshipsPart rrp = getRelationshipsPartFromJCR( 
							part, jcrSession, docxNode,  relPart);
					part.setRelationships(rrp);
					log.info("set its RelationshipsPart to '" + rrp.getPartName() + "'");
					unusedJCRNodes.put(relPart, new Boolean(false));					
					addPartsFromRelationships(jcrSession, docxNode, part, rrp, unusedJCRNodes );					
				} catch (javax.jcr.PathNotFoundException e) {
					log.info("No relationships " + relPart );										
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
		
	
	
}