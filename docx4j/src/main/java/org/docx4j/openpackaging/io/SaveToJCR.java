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
import java.io.IOException;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.version.*;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.TransientRepository;  // Testing only

import org.apache.log4j.Logger;

import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Relationship;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import org.merlin.io.DOMSerializerEngine;
import org.merlin.io.OutputEngineInputStream;

import java.util.Calendar;

/**
 * Save a Package object to the JCR
 * 
 * However, note that this class doesn't save as a single
 * zipped up node in JCR - only as discrete unzipped
 * parts (ie a JCR node for each part).
 * 
 * @author jharrop
 *
 */
public class SaveToJCR {
	
	private static Logger log = Logger.getLogger(SaveToJCR.class);		

	
	public SaveToJCR(Package p, Session jcrSession) {
		
		this.p = p;
		this.jcrSession = jcrSession;
		
	}
	
	// The package to save
	public Package p;
			
	public Session jcrSession;
	
	/* Save a Package to baseNode in JCR.
	 * baseNode must exist an be a suitable node type.             
	 * eg Node exampledoc1 = docs.addNode("exampledoc1.docx", "nt:file");            
          Node baseNode = exampledoc1.addNode("jcr:content", "nt:unstructured");
       Consider passing instead the folder node and desired filename.
	 *  */
	public boolean save(Node baseNode) throws Docx4JException {
		
		
		 try {
			log.info("Saving to" +  baseNode.getPath() );
			
//	        // Create the ZIP file
//	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filepath));

			// 3. Get [Content_Types].xml
			ContentTypeManager ctm = p.getContentTypeManager();
			saveRawXmlPart(baseNode, "[Content_Types].xml", ctm.getDocument() );
	        
			// 4. Start with _rels/.rels

//			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
//			  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
//			  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
//			  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
//			</Relationships>		
			
			String partName = "_rels/.rels";
			RelationshipsPart rp = p.getRelationshipPart();
			// TODO - replace with saveRawXmlPart(baseNode, rp)
			// once we know that partName resolves correctly
			saveRawXmlPart(baseNode, partName, rp.getDocument() );
			
			
			// 5. Now recursively 
//			addPartsFromRelationships(baseNode, "", rp );
			addPartsFromRelationships(baseNode, rp );

//			jcrSession.save();
	    
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
	
	public String encodeSlashes( String partName) {
		
		log.debug( "Incoming: " + partName);
		log.debug( "Encoded as: " + java.net.URLEncoder.encode( partName ));
		
		return java.net.URLEncoder.encode( partName );
		
	}


	public String saveRawXmlPart(Node baseNode, Part part) throws Docx4JException {
		
		// This is a neater signature and should be used where possible!
		
		String partName = part.getPartName().getName().substring(1);
		
		org.dom4j.Document xml = part.getDocument();
		
		return saveRawXmlPart(baseNode, partName, xml);  
		
	}
	
	/* @displayName - a human readable description for this content
	 * object.  Pass null if you don't want the displayName property set. 
	 * Returns the version number of the resource.
	 */
	public String saveRawXmlPart(Node baseNode, String partName, org.dom4j.Document xml) throws Docx4JException {

		try {
            // NB: Nodetypes used are required for interoperability with 
            // org.apache.jackrabbit.server.   Three things are required 
			// for interoperability:
			// 1. Same node types and hierarchy (dump the structure
			//    of a document PUT via WebDAV to see what is required)
			// 2. Same handling of directories (ie ignore)
			// 3. Same encoding of file names (which is the same as
			//    2, since we encode the '/' which would otherwise
			//    be a directory.
		
			// Create an nt:file node to represent each file name
			// Encode '/' so it is a flat structure

			Node fileNode;			
			try {
				// Does it exist already?
				fileNode = baseNode.getNode(encodeSlashes(partName));
				log.info(encodeSlashes(partName) + " found .. ");				
			} catch (PathNotFoundException pnf) {
				log.info(encodeSlashes(partName) + " not found .. so adding");
		        fileNode = baseNode.addNode( encodeSlashes(partName), "nt:file" );
		    }	        
			
	        		
			// Then a jcr:content node for the content
	        // The jcr:content child node can be of any node type to allow maximum flexibility, 
	        // but the nt:resource node type is the common choice for jcr:content nodes.
			// (use nt:resource for binary content).
			Node contentNode; 
			try {
				// Does it exist already?
				contentNode = fileNode.getNode("jcr:content");
				log.info(" jcr:content found ");
				
				if (contentNode.isNodeType("mix:versionable")) {
					log.info(" it is mix:versionable .. so checkout");					
					// check it out
					contentNode.checkout();
				} else {
			        contentNode.addMixin("mix:versionable");					
					log.info(" made mix:versionable ");					
				}
				
			} catch (PathNotFoundException pnf) {
				log.info("jcr:content not found .. so adding");
				contentNode = fileNode.addNode("jcr:content", "nt:unstructured");
		        contentNode.addMixin("mix:versionable");
		    }	        	        
	                	
	        
	        // Need to convert the Document to an input stream
	        // There are at least three possible ways to do it:
	        // 1. PipedInputStream http://www.biglist.com/lists/xsl-list/archives/199908/msg00554.html
	        // 2. Use an intermediate byte array
	        // 3. Use XSLT! http://lists.xml.org/archives/xml-dev/200408/msg00072.html
	        // In fact I use the library
	        // See http://www.ibm.com/developerworks/java/library/j-io1/
	        // Though the byte array would work ok as well.

	        // For now, convert dom4j node to real W3C dom node
	        // (TODO write org.merlin.io.DOM4JSerializerEngine!)
	        org.w3c.dom.Document w3cDoc 
	        	= new org.dom4j.io.DOMWriter().write(xml);
	        DOMSerializerEngine engine 
	        	= new DOMSerializerEngine( (org.w3c.dom.Node)w3cDoc.getDocumentElement() );

	        // javax.jcr.nodetype.ConstraintViolationException: no matching property definition found for {http://www.jcp.org/jcr/1.0}data
	        contentNode.setProperty("jcr:mimeType", "text/plain");
//	        contentNode.setProperty("jcr:encoding", "");
	        contentNode.setProperty("jcr:data", new OutputEngineInputStream(engine) );
	        Calendar lastModified = Calendar.getInstance();
	        lastModified.setTimeInMillis(lastModified.getTimeInMillis());
	        contentNode.setProperty("jcr:lastModified", lastModified);	
			jcrSession.save();
			
			Version firstVersion = contentNode.checkin();

			log.info( "PUT SUCCESS: " + partName + " VERSION " + contentNode.getBaseVersion().getName());		
			
			return contentNode.getBaseVersion().getName();
				// WARNING: this is JCR implementation specific
			
		} catch (Exception e ) {
			e.printStackTrace();
			throw new Docx4JException("Failed to put " + partName, e);
		}
				
	}
	
	/* recursively 
		(i) get each Part listed in the relationships
		(ii) add the Part to the zip file
		(iii) traverse its relationship
	*/
	public void addPartsFromRelationships( 
			Node baseNode, RelationshipsPart rp )  throws Docx4JException  {
		
		for (Iterator it = rp.iterator(); it.hasNext(); ) {
			Relationship r = (Relationship)it.next();
			log.info("For Relationship Id=" + r.getId() 
					+ " Source is " + r.getSource().getPartName() 
					+ ", Target is " + r.getTargetURI() );
			try {
				String resolvedPartUri = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();
				// Now drop leading "/'
				resolvedPartUri = resolvedPartUri.substring(1);				
				
				// Now normalise it .. ie abc/def/../ghi
				// becomes abc/ghi
				// to deal with word/../customXml/item1.xml
				// in my UNHCR Arabic doc
//				target = (new java.net.URI(target)).normalize().toString();
//				log.info("Normalised, it is " + target );
				
				
				// TODO - if this is already in our hashmap, skip
				// to the next				
				if (!false) {
					log.info("Getting part /" + resolvedPartUri );
					Part part = p.getPart(new PartName("/" + resolvedPartUri));
					log.info( part.getClass());
					
					savePart(baseNode, part);
					
				}

			} catch (Exception e) {
				throw new Docx4JException("Failed to add parts from relationships", e);
			}
		}
		
		
	}

	/**
	 * Save a Part (not a relationship part though) to JCR, along with its relationship
	 * part (if any), and any parts referred to in its relationships, 
	 * and so on, recursively.
	 * @param baseNode
	 * @param target
	 * @param part
	 * @throws Docx4JException
	 */
	public void savePart(Node baseNode, Part part)
			throws Docx4JException {
		
		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);
		if (part instanceof BinaryPart) {
			log.info(".. saving binary stuff" );
			saveRawBinaryPart( baseNode, part );
			
		} else {
			log.info(".. saving " );
			saveRawXmlPart( baseNode, part );
		}
		
		// recurse via this parts relationships, if it has any
		if (part.getRelationshipPart()!= null ) {
			RelationshipsPart rrp = part.getRelationshipPart(); // **
			String relPart = PartName.getRelationshipsPartName(resolvedPartUri);
			log.info("Found relationships " + relPart );
			saveRawXmlPart(baseNode,  relPart, rrp.getDocument() );
			log.info("Recursing ... " );
			addPartsFromRelationships( baseNode, rrp );
		} else {
			log.info("No relationships for " + resolvedPartUri );					
		}
	}

	/* put binary parts */
	protected void saveRawBinaryPart(Node baseNode, Part part) throws Docx4JException {

		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);

		InputStream bin = ((BinaryPart)part).getBinaryData();
		
		try {
		
			// Create an nt:file node to represent each file name
			// Encode '/' so it is a flat structure
			Node fileNode;			
			try {
				// Does it exist already?
				fileNode = baseNode.getNode(encodeSlashes(resolvedPartUri));
				log.info(encodeSlashes(resolvedPartUri) + " found .. ");				
			} catch (PathNotFoundException pnf) {
				log.info(encodeSlashes(resolvedPartUri) + " not found .. so adding");
		        fileNode = baseNode.addNode( encodeSlashes(resolvedPartUri), "nt:file" );
		    }	        			
	        //Node fileNode = baseNode.addNode( encodeSlashes(partName), "nt:file" );

			Node contentNode; 
			try {
				// Does it exist already?
				contentNode = fileNode.getNode("jcr:content");
				log.info(" jcr:content found ");
				if (contentNode.isNodeType("mix:versionable")) {
					log.info(" it is mix:versionable.. so checkout");					
					// check it out
					contentNode.checkout();
				} else {
			        contentNode.addMixin("mix:versionable");					
					log.info(" made mix:versionable");					
				}
			} catch (PathNotFoundException pnf) {
				log.info("jcr:content not found .. so adding");
				contentNode = fileNode.addNode("jcr:content", "nt:unstructured");
		        contentNode.addMixin("mix:versionable");
		    }	        	        
			
	        //Node contentNode = fileNode.addNode("jcr:content", "nt:unstructured");	        
	        //contentNode.addMixin("mix:versionable");
	        
	        contentNode.setProperty("jcr:mimeType", "application/octet-stream");
//	        contentNode.setProperty("jcr:encoding", "");
	        contentNode.setProperty("jcr:data", bin );
	        Calendar lastModified = Calendar.getInstance();
	        lastModified.setTimeInMillis(lastModified.getTimeInMillis());
	        contentNode.setProperty("jcr:lastModified", lastModified);	
			jcrSession.save();
			
			Version firstVersion = contentNode.checkin();
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}
	}
	
	
	
    private  String dump(Node node, String indent) throws RepositoryException {
        StringBuilder sb=new StringBuilder();
        String sep=",";
        sb.append(indent + node.getName());
        sb.append("["+node.getPath());
        PropertyIterator propIterator=node.getProperties();
        while(propIterator.hasNext()) {
            Property prop=propIterator.nextProperty();
            sb.append(sep);
            sb.append("\n" + indent + "@"+prop.getName()+"=\""+prop.getString()+"\"");
        }
        sb.append("]");
        
        NodeIterator nodeIterator = node.getNodes();
        while(nodeIterator.hasNext()) {
            Node n=nodeIterator.nextNode();
            sb.append( "\n\n" + dump( n, indent + "    " ));
            
        }
        
        
        return sb.toString();
    }
    
  
 	
	private void debugPrint( Document coreDoc) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
		    XMLWriter writer = new XMLWriter( System.out, format );
		    writer.write( coreDoc );
		} catch (Exception e ) {
			e.printStackTrace();
		}	    
	}
		
	// Testing
	public static void main(String[] args) throws Exception {
		Session jcrSession = null;
        try {

        	//We need:
        	
        	// 1. a Session
	        Repository repository = new TransientRepository();
	        jcrSession = repository.login(
	                new SimpleCredentials("username", "password".toCharArray()));
	        
	        // 2. a Package
    		Package p = Package.createTestPackage();
    		log.info( "Test package created..");

    		// 3. a Node to save to
    		Node root = jcrSession.getRootNode();
            Node customers = root.addNode("customers", "nt:folder");
            Node customer = customers.addNode("contoso", "nt:folder");
            Node docs = customer.addNode("docs", "nt:folder");
            Node exampledoc1 = docs.addNode("exampledoc1.docx", "nt:file");            
            // This node required for interoperability with 
            // org.apache.jackrabbit.server
	        Node contentNode = exampledoc1.addNode("jcr:content", "nt:unstructured");
	        
	        // Now we can do the actual save.
	        SaveToJCR saver = new SaveToJCR(p, jcrSession);
	        saver.save(contentNode );		
	        
        } catch(Exception e) {
            e.printStackTrace();	        
        } finally {
        	jcrSession.logout();
        }
	}

	
	
}