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


import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFormatException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import javax.xml.bind.JAXBContext;

//import org.apache.jackrabbit.core.TransientRepository;

import org.apache.log4j.Logger;

import org.docx4j.JcrNodeMapper.NodeMapper;
import org.docx4j.jaxb.Context;
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

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;

// Aim to remove dependency on any XML API from 
// this package.
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

//	// Testing - Jackrabbit only
//	public static void main(String[] args) throws Exception {
//
//        Repository repository = new TransientRepository();
//        Session jcrSession = repository.login(
//                new SimpleCredentials("username", "password".toCharArray()));
//
//        // You'll need to run SaveToJCR for this to exist
//        String nodePath = "customers/contoso/docs/exampledoc1";
//        
//        try {
//		
//    		org.docx4j.JcrNodeMapper.JackrabbitJcrNodeMapper nodeMapper = new org.docx4j.JcrNodeMapper.JackrabbitJcrNodeMapper(); 
//    		LoadFromJCR loader = new LoadFromJCR(nodeMapper);
//    		loader.get(jcrSession, nodePath);	
//        	
//        } finally {
//        	jcrSession.logout();
//        }
//        	
//	}
	
	public static final String CM_NAMESPACE = "cm:";
	
	 // HashMap containing the names of all the nodes,
		// so we can tell whether there are any orphans
	public HashMap unusedJCRNodes = null;
	
	public NodeMapper nodeMapper = null;
	
	public LoadFromJCR(NodeMapper nodeMapper) {
		this(new ContentTypeManagerImpl(), nodeMapper );
	}

	public LoadFromJCR(ContentTypeManager ctm, NodeMapper nodeMapper) {
		
		// Purpose is to be able to pass in the appropriate subclass of CTM
		this.ctm = ctm;
		this.nodeMapper = nodeMapper;
	}

	
	public Package get(Session jcrSession, String nodePath) throws Docx4JException {
        try {
            Node root = jcrSession.getRootNode();
            Node docxNode = root.getNode(nodePath);
            return get(jcrSession, docxNode);
	    } catch (Exception e) {
			e.printStackTrace() ;
			throw new Docx4JException("Failed to get package");
	    }
	}
	
	public Package get(Session jcrSession, Node docxNode ) throws Docx4JException  {
		
		Package p = null;
		
		// 1. The idea is to walk the tree of relationships, getting
//		everything we need from JCR. 
		
		unusedJCRNodes = new HashMap();
        try {
//	        Node docxContentNode = docxNode.getNode("jcr:content");
			Node docxContentNode = nodeMapper.getContentNode(docxNode);

            // Its a flat structure since we've URL Encoded '/'
            NodeIterator nodeIterator = docxContentNode.getNodes();
	        while(nodeIterator.hasNext()) {
	            Node n=nodeIterator.nextNode();
	            log.info(n.getName());
				unusedJCRNodes.put(decodeSlashes(nodeMapper, n.getName()), new Boolean(true) );				
	        }		
		
			// 2. Create a new Package
	//		Eventually, you'll only be able to create an Excel package etc
	//		but only the WordML package exists at present
//	        Document ctmDocument = null; 
//	        ctmDocument = deprecatedGetDocumentFromJCRPart(jcrSession, nodeMapper, docxContentNode, "[Content_Types].xml");	        	
//			debugPrint(ctmDocument);
	        
	        Node contentTypesPartNode = getPartNode(jcrSession, nodeMapper, docxContentNode, "[Content_Types].xml");

	        // Document ctmDocument = deprecatedGetDocumentFromJCRPart(jcrSession, nodeMapper, contentTypesPartNode);			
			InputStream in = getInputStreamFromJCRPart(nodeMapper, contentTypesPartNode);
			SAXReader xmlReader = new SAXReader();
			Document ctmDocument = null;
			try {
				ctmDocument = xmlReader.read(in);
				debugPrint(ctmDocument);				
			} catch (DocumentException e) {
				e.printStackTrace() ;
				throw e;
			}
	        
	        
			ctm.parseContentTypesFile(ctmDocument);
			p = ctm.createPackage();
						
			
			// 3. Get [Content_Types].xml
	//		Once we've got this, then we can look up the content type for
	//		each PartName, and use it in the Part constructor.

//			p.setContentTypeManager(ctm); - 20080111 - done by ctm.createPackage();
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
			//rp.setPackageRelationshipPart(true);
			unusedJCRNodes.put(partName, new Boolean(false));
			
			
			log.info( "Object created for: " + partName);
			//log.info( rp.toString());
			
			// 5. Now recursively 
	//		(i) create new Parts for each thing listed
	//		in the relationships
	//		(ii) add the new Part to the package
	//		(iii) cross the PartName off unusedJCRNodes
			addPartsFromRelationships(jcrSession, docxContentNode, p, rp );
			
			
			// 6. Check unusedJCRNodes is empty
			 Iterator myVeryOwnIterator = unusedJCRNodes.keySet().iterator();
			 while(myVeryOwnIterator.hasNext()) {
			     String key = (String)myVeryOwnIterator.next();
			     log.info( key + "  " + unusedJCRNodes.get(key));
			 }
			 
			 
	    } catch (Exception e) {
			e.printStackTrace() ;
			throw new Docx4JException("Failed to get package", e);			
	    }
	    
	 return p;
		
	}
	
	public RelationshipsPart getRelationshipsPartFromJCR(Base p, Session jcrSession, Node docxNode, String partName) 
			throws InvalidFormatException, Docx4JException {
		
		try {
//			InputStream is = getInputStreamFromJCRPart( jcrSession, nodeMapper, 
//					docxNode,  partName);
			
			Node contentNode = getPartNode(jcrSession,nodeMapper,  
					docxNode,  partName);
			InputStream is = getInputStreamFromJCRPart( nodeMapper, contentNode); 
			

			return new RelationshipsPart(p, new PartName("/" + partName), is );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Docx4JException("Error getting document from JCR Part:" + partName, e);
		} 
	
	}

	public static String encodeSlashes(NodeMapper nodeMapper, String partName) {
		
		// TODO - fix Jackrabbit stuff so that it creates directories
		// rather than encoding slashes!
		
		if (nodeMapper instanceof org.docx4j.JcrNodeMapper.AlfrescoJcrNodeMapper) {
			
//			log.info("incoming path:" + partName);
						
			// Split it into segments, and add CM_NAMESPACE prefix
			StringBuffer retVal = new StringBuffer();
			String[] partNameSegments = partName.split("/");

			for (short i = 0 ; i < partNameSegments.length; ++i) {
				if (i>0) retVal.append("/");
				retVal.append(CM_NAMESPACE + org.docx4j.JcrNodeMapper.ISO9075.encode(partNameSegments[i]) );
			}
//			log.info("created path:" + retVal.toString());
			return retVal.toString();
		} else {
//			log.info( "Incoming: " + partName);
//			log.info( "Encoded as: " + java.net.URLEncoder.encode( partName ));
			return java.net.URLEncoder.encode( partName );
		}
		
	}
	
	public String decodeSlashes(NodeMapper nodeMapper, String partName) {
		
//		log.info( "Incoming: " + partName);
//		log.info( "Decoded as: " + java.net.URLDecoder.decode( partName ));
		
		if (nodeMapper instanceof org.docx4j.JcrNodeMapper.AlfrescoJcrNodeMapper) {
			
//			log.info("incoming path:" + partName);
			
			// Split it into segments, and remove CM_NAMESPACE prefix
			StringBuffer retVal = new StringBuffer();
			String[] partNameSegments = partName.split("/");

			for (short i = 0 ; i < partNameSegments.length; ++i) {
				if (i>0) retVal.append("/");
				retVal.append( org.docx4j.JcrNodeMapper.ISO9075.decode(partNameSegments[i].substring(CM_NAMESPACE.length())) );
			}
//			log.info("created path:" + retVal.toString());
			return retVal.toString();
		} else {
			return java.net.URLDecoder.decode( partName );
		}
		
	}
	
//	// Deprecate, since it encourages inefficient use of JCR API,
//	// which can't be afforded when using Alfresco
//	private static InputStream getInputStreamFromJCRPart(Session jcrSession, NodeMapper nodeMapper, Node docxNode, String partName) 
//		throws DocumentException, RepositoryException, PathNotFoundException {
//		
//		InputStream in = null;
//		log.info("Fetching " + encodeSlashes(nodeMapper, partName));
//		Node fileNode = docxNode.getNode(encodeSlashes(nodeMapper, partName));
////		Node contentNode = fileNode.getNode("jcr:content");
//		Node contentNode = nodeMapper.getContentNode(fileNode);
//		
////		Property jcrData = contentNode.getProperty("jcr:data"); 
//		Property jcrData = nodeMapper.getJcrData(contentNode);		
//		in = jcrData.getStream();
//		
//		return in;		
//	}
	
	// Newer, preferred form
	public static InputStream getInputStreamFromJCRPart(NodeMapper nodeMapper, 
			Node contentNode) 
		throws DocumentException, RepositoryException, PathNotFoundException {
	
		try {		
			
	//		Property jcrData = contentNode.getProperty("jcr:data");
			Property jcrData = nodeMapper.getJcrData(contentNode);
			return jcrData.getStream();
		} catch (PathNotFoundException e) {		
			e.printStackTrace();
			return null;
		}
	}
	
	public static Node getPartNode(Session jcrSession,
			NodeMapper nodeMapper, Node docxNode, String partName) 
		throws DocumentException, RepositoryException, PathNotFoundException {
		
		long startTime = 0;
		try {
			log.info("Fetching " + encodeSlashes(nodeMapper, partName));
	        startTime = System.currentTimeMillis();    	        
			
			Node fileNode = docxNode.getNode(encodeSlashes(nodeMapper, partName));
//			Node contentNode = fileNode.getNode("jcr:content");
			Node contentNode = nodeMapper.getContentNode(fileNode);
						
	        long endTime = System.currentTimeMillis();
	        long duration = endTime - startTime;	        
	        log.info("Finding part node took " + duration + "ms");
	        return contentNode;
			
		} catch (PathNotFoundException e) {
			
			e.printStackTrace();	
			return null;
		}		
	}
	
	
//	private static Document deprecatedGetDocumentFromJCRPart(Session jcrSession, NodeMapper nodeMapper, Node docxNode, String partName) 
//	throws DocumentException, RepositoryException, PathNotFoundException {
//	
//	InputStream in = null;
//	log.info("Fetching " + encodeSlashes(nodeMapper, partName));
//	Node fileNode = docxNode.getNode(encodeSlashes(nodeMapper, partName));
//	
////	Node contentNode = fileNode.getNode("jcr:content");
//	Node contentNode = nodeMapper.getContentNode(fileNode);
//	
////	Property jcrData = contentNode.getProperty("jcr:data"); 
//	Property jcrData = nodeMapper.getJcrData(contentNode);
//	
//	in = jcrData.getStream();
//	
//	SAXReader xmlReader = new SAXReader();
//	Document contents = null;
//	try {
//		contents = xmlReader.read(in);
////		log.info("\n\n" + partName + "\n ===================");
////		debugPrint(contents);
//		
//	} catch (DocumentException e) {
//		// Will land here for binary files eg gif file
//		// These do get handled ..
//		log.error("DocumentException on " + partName + " . Check this is binary content."); 
//		//e.printStackTrace() ;
//		throw e;
//	}
//	return contents;		
//}
	
	/* recursively 
	(i) create new Parts for each thing listed
	in the relationships
	(ii) add the new Part to the package
	(iii) cross the PartName off unusedJCRNodes
	*/
	public void addPartsFromRelationships(Session jcrSession, 
			Node docxNode, Base source, RelationshipsPart rp) throws Docx4JException {
		
		Package pkg = source.getPackage();		
		
		for (Iterator it = rp.iterator(); it.hasNext(); ) {
			Relationship r = (Relationship)it.next();
			log.info("For Relationship Id=" + r.getId() 
					+ " Source is " + r.getSource().getPartName() 
					+ ", Target is " + r.getTargetURI() );
			try {				
				getPart(jcrSession, docxNode, pkg, rp, r);
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
	 * @param jcrSession
	 * @param docxNode
	 * @param pkg
	 * @param rp
	 * @param r
	 * @throws Docx4JException
	 * @throws RepositoryException
	 * @throws InvalidFormatException
	 */
	public void getPart(Session jcrSession, Node docxNode, 
			Package pkg, RelationshipsPart rp, Relationship r)	
			throws Docx4JException, RepositoryException, InvalidFormatException {
		
		Base source = r.getSource();
		String resolvedPartUri = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();		

		// Now drop leading "/'
		resolvedPartUri = resolvedPartUri.substring(1);				

		// Now normalise it .. ie abc/def/../ghi
		// becomes abc/ghi
//		target = (new java.net.URI(target)).normalize().toString();
//		log.info("Normalised, it is " + target );				
		
		String relationshipType = r.getRelationshipType();		
		
		Part part = getRawPart(jcrSession, nodeMapper, docxNode, ctm, resolvedPartUri);
		rp.loadPart(part);
		
		
		// The source Part (or Package) might have a convenience
		// method for this
		if (source.setPartShortcut(part, relationshipType ) ) {
			log.info("Convenience method established from " + source.getPartName() 
					+ " to " + part.getPartName());
		}				
		
		unusedJCRNodes.put(resolvedPartUri, new Boolean(false));
		log.info(".. added part '" + part.getPartName() + "'" );
		
		RelationshipsPart rrp = getRelationshipsPart(jcrSession, docxNode, part);
		if (rrp!=null) {
			// recurse via this parts relationships, if it has any
			addPartsFromRelationships(jcrSession, docxNode, part, rrp );					
			String relPart = PartName.getRelationshipsPartName(
					part.getPartName().getName().substring(1) );
			unusedJCRNodes.put(relPart, new Boolean(false));
		}
	}


	/**
	 * Get the Relationships Part (if there is one) for a given Part.  
	 * Otherwise return null.
	 *  
	 * @param jcrSession
	 * @param docxNode
	 * @param part
	 * @param rrp
	 * @param relPart
	 * @return
	 * @throws RepositoryException
	 * @throws InvalidFormatException
	 */
	public RelationshipsPart getRelationshipsPart(Session jcrSession,
			Node docxNode, Part part)
			throws Docx4JException, RepositoryException, InvalidFormatException {
		//String relPart = PartName.getRelationshipsPartName(target);
		String relPart = PartName.getRelationshipsPartName(
				part.getPartName().getName().substring(1) );
		RelationshipsPart rrp = null;
		try {
			docxNode.getNode(encodeSlashes(nodeMapper, relPart)); // if null, 
			//throws javax.jcr.PathNotFoundException
			
			log.info("Found relationships " + relPart );
			log.info("Recursing ... " );
			rrp = getRelationshipsPartFromJCR( 
					part, jcrSession, docxNode,  relPart);
			part.setRelationships(rrp);
			log.info("set its RelationshipsPart to '" + rrp.getPartName() + "'");
		} catch (javax.jcr.PathNotFoundException e) {
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
	 * @param jcrSession
	 * @param docxNode
	 * @param resolvedPartUri
	 * @return
	 * 
	 * @throws URISyntaxException
	 * @throws InvalidFormatException
	 */
	public static Part getRawPart(Session jcrSession, NodeMapper nodeMapper, Node docxNode, ContentTypeManager ctm, String resolvedPartUri)
			throws Docx4JException {
		
		Part part = null;

		InputStream is = null;		
		try {
						
			try {
				
				// Get the input stream first, so it is available
				// even if getPart throws PartUnrecognisedException.
				Node contentNode = getPartNode(jcrSession,nodeMapper,  
						docxNode,  resolvedPartUri);
				is = getInputStreamFromJCRPart( nodeMapper, contentNode); 

				part = ctm.getPart("/" + resolvedPartUri);
								
				
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
				
				} else if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {
					
					// MainDocument part, Styles part, Font part etc

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jc);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.Dom4jXmlPart) {
					
					((org.docx4j.openpackaging.parts.Dom4jXmlPart)part).setDocument( is );

				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart) {
					
					log.debug("Detected ObfuscatedFontPart");
					((BinaryPart)part).setBinaryData(is);
					log.info("Stored as BinaryData" );
										
				} else {
					// Shouldn't happen, since ContentTypeManagerImpl should
					// return an instance of one of the above, or throw an
					// Exception.
					
					log.error("No suitable part found for: " + resolvedPartUri);
					return null;					
				}
			} catch (PartUnrecognisedException e) {
				
				// Try to get it as a binary part				
				part = getBinaryPart(jcrSession, nodeMapper, docxNode, ctm, resolvedPartUri);
				((BinaryPart)part).setBinaryData(is);
					
			}
		} catch (Exception ex) {
			// PathNotFoundException, ValueFormatException, RepositoryException, URISyntaxException
			ex.printStackTrace();
			throw new Docx4JException("Failed to getPart", ex);
		} 
		return part;
	}
		
	public static Part getBinaryPart(Session jcrSession, NodeMapper nodeMapper, Node docxNode,
			ContentTypeManager ctm, String resolvedPartUri)
			throws Docx4JException {

		Part part = null;

		try {

			InputStream in = null;
			log.info("Fetching " + encodeSlashes(nodeMapper, resolvedPartUri));
			Node fileNode = docxNode.getNode(encodeSlashes(nodeMapper, resolvedPartUri));
			
//			Node contentNode = fileNode.getNode("jcr:content");
			Node contentNode = nodeMapper.getContentNode(fileNode);

//			Property jcrData = contentNode.getProperty("jcr:data"); 
			Property jcrData = nodeMapper.getJcrData(contentNode);			
			in = jcrData.getStream();

			part = new BinaryPart(new PartName("/" + resolvedPartUri));

			((BinaryPart) part).setBinaryData(in);
			log.info("Stored as BinaryData");

		} catch (Exception ex) {
			// PathNotFoundException, ValueFormatException, RepositoryException,
			// URISyntaxException
			ex.printStackTrace();
			throw new Docx4JException("Failed to getPart", ex);
		}
		return part;
	}

	
	
}