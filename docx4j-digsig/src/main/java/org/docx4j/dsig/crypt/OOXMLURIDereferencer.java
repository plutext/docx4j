/* NOTICE: This file has been changed by Plutext Pty Ltd for use with docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt  
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */ 

package org.docx4j.dsig.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
//import org.docx4j.org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.docx4j.org.apache.poi.openxml4j.opc.PackagePart;
//import org.docx4j.org.apache.poi.openxml4j.opc.PackagePartName;
//import org.docx4j.org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import org.docx4j.dsig.crypt.SignatureConfig.SignatureConfigurable;

/**
 * JSR105 URI dereferencer for Office Open XML documents.
 */
public class OOXMLURIDereferencer implements URIDereferencer, SignatureConfigurable {

	private static Logger LOG = LoggerFactory.getLogger(OOXMLURIDereferencer.class);	
	

    private SignatureConfig signatureConfig;
    private URIDereferencer baseUriDereferencer;

    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }


    public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
        if (baseUriDereferencer == null) {
            baseUriDereferencer = signatureConfig.getSignatureFactory().getURIDereferencer();
        }
        
        if (null == uriReference) {
            throw new NullPointerException("URIReference cannot be null");
        }
        if (null == context) {
            throw new NullPointerException("XMLCrytoContext cannot be null");
        }

        URI uri;
        try {
            uri = new URI(uriReference.getURI());
        } catch (URISyntaxException e) {
            throw new URIReferenceException("could not URL decode the uri: "+uriReference.getURI(), e);
        }
        
        
        
        
//        InputStream dataStream=null;
//        
//		PartStore partStore = signatureConfig.getOpcPackage().getSourcePartStore();
//		if (partStore==null) {
//			LOG.info("No PartStore defined for this package (it was probably created, not loaded). " );
//			throw new NotImplementedException("Can only validate pre-existing signature");
//			// until canonicalisation is improved
//		} 			
//		
//		try {
//			dataStream = partStore.loadPart( path.substring(1));
//	        return new OctetStreamData(dataStream, uri.toString(), null);
//		} catch (Docx4JException e) {
//			throw new URIReferenceException(e);
//		} finally {
//			IOUtils.closeQuietly(dataStream);
//		}        
        
        Part part = null;
        
        String path = uri.getPath(); 
        if (path.endsWith(".rels")) // special handling for rels parts        
        {
        	String sourcePartName = RelationshipsPart.inferSourcePartName(path);
        	LOG.debug(path + " -> " + sourcePartName);
        	if (sourcePartName.equals("/")) {
                part = signatureConfig.getOpcPackage().getRelationshipsPart(); 
        	} else {
        		part = findPart(sourcePartName).getRelationshipsPart();
        	}
        } else {
          part = findPart(path);        	
        }

        if (part==null) {
            LOG.debug("\n\n" + uri + " - delegating to base DOM URI dereferencer");
            return this.baseUriDereferencer.dereference(uriReference, context);
        }
        
		/* If we are reading from a file, we want to use the raw content
        // because XML parts aren't usually canonicalised,
        // and using the raw content ensures JAXB etc don't alter it.
         * 
         * Compare Open Packaging Convention which says:

			[Note: Performing a canonicalization transform ensures that SignedInfo content can be validated even if the
			content has been regenerated using, for example, different entity structures, attribute ordering, or character
			encoding.
			
			Producers and consumers should also use canonicalization transforms for references to parts that hold XML
			documents. [S6.4]These transforms are defined using the Transform element. end note]
         * 
         * In any case, our starting point should be what is on disk.  Transformation happens later..
         */
        InputStream dataStream=null;
        if (signatureConfig.isOnlyValidation() ) {
			try {
				dataStream = getInputStreamFromPartStore(part);
				
				// since the part could have been changed via docx4j since loading,
				// the SignatureHelper API requires a docx/pptx/xlsx file (as opposed to a docx4j pkg)
				
			} catch (Docx4JException e1) {
				LOG.warn(e1.getMessage(), e1);
			}
        }
        // if we are signing, we don't use the stuff in the part store, since
        // the content may have been changed by docx4j. 
        // And that would mean what we sign is different to the file docx4j saves,
        // resulting in an invalid signature!
		
		if (dataStream!=null) {
			LOG.debug("OK .. read raw part content from PartStore");
		} else {   

			// where the the SignatureHelper API has been used, this shouldn't happen,
			// since there will be a PartStore.  
			
		      try {
		      	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		      	
		  		if (part instanceof BinaryPart ) {
		  			
		  			dataStream = new ByteArrayInputStream(
		  							((BinaryPart)part).getBytes());
		  			
		  		} else if (part instanceof JaxbXmlPart) {
		  			
		  	        if (signatureConfig.isOnlyValidation() ) {
		  	        	LOG.debug("Hmm .. have to use marshalled content..");
		  	        }
		  			
		  			/* Something in Java 6 re-orders the attributes! 
		  			 * Somewhat surprisingly, Java 7 and 8 don't seem to
		  			 * 
		  			 * JAXB? they are sorted alphabetically in Java 6 and in the order of declaration on Java 7 
		  			 * (in both cases using the built-in JDK JAXB provider).  
		  			 * If you use com.sun.xml.bind:jaxb-impl-2.2.2 as a Maven dependency 
		  			 * as well as jakarta.xml.bind:jaxb-api-2.2.2, the Marshaller 
		  			 * behaves the correct way - serializing attributes in the order they appear in the code.
		  			 * 
		  			 * Note also https://java.net/jira/browse/JAXB-657
		  			 */
		  			
			  			((JaxbXmlPart)part).marshal( baos );
			  			
			  			if (LOG.isDebugEnabled()) {
			  				
			  				byte[] bytes = baos.toByteArray();
			  				LOG.debug( new String(bytes));
			  				dataStream = new ByteArrayInputStream(bytes);  				
			  				
			  			} else 
			  			{  		
			  				dataStream = new ByteArrayInputStream(baos.toByteArray());
			  			}
			  			
		  		} else if (part instanceof CustomXmlDataStoragePart) {
		  			
		  			// what Save does with this sort of part
		  			((CustomXmlDataStoragePart)part).getData().writeDocument(baos);
		  			dataStream = new ByteArrayInputStream(baos.toByteArray());
		  			
		  		} else if (!(part instanceof XmlPart)) {
		  			
		  			throw new Docx4JException("don't know how to digest a " + part.getClass().getName());
		  			
		  		}  else {
		  	        if (signatureConfig.isOnlyValidation() ) {
		  				LOG.debug("Hmm .. have to use org.w3c.dom.Document content..");
		  				// NB there is no way to specify the order of the attributes in the DOM!
		  	        }

					Document doc =  ((XmlPart)part).getDocument();
		
		  				//  With Crimson, this gives:    				
							//  Exception in thread "main" java.lang.AbstractMethodError: org.apache.crimson.tree.XmlDocument.getXmlStandalone()Z
							//	at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(DOM2TO.java:373)
							//	at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:127)
							//	at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:94)
							//	at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transformIdentity(TransformerImpl.java:662)
							//	at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:708)
							//	at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:313)
							//	at org.docx4j.model.datastorage.CustomXmlDataStorageImpl.writeDocument(CustomXmlDataStorageImpl.java:174)
		  				DOMSource source = new DOMSource(doc);
		  				XmlUtils.getTransformerFactory().newTransformer().transform(source,
		  						 new StreamResult(baos) );    			
		      			dataStream = new ByteArrayInputStream(baos.toByteArray());
		  		}
		
		//          // workaround for office 2007 pretty-printed .rels files
		//          if (part.getPartName().toString().endsWith(".rels")) {
		//              // although xmlsec has an option to ignore line breaks, currently this
		//              // only affects .rels files, so we only modify these
		//              // http://stackoverflow.com/questions/4728300
		//              ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//              for (int ch; (ch = dataStream.read()) != -1; ) {
		//                  if (ch == 10 || ch == 13) continue;
		//                  bos.write(ch);
		//              }
		//              dataStream = new ByteArrayInputStream(bos.toByteArray());
		          
		      } catch (Exception e) {
		          throw new URIReferenceException("Can't digest " + part.getPartName().getName() + ": " + e.getMessage(), e);
		      }
		}
      
      return new OctetStreamData(dataStream, uri.toString(), null);
        
    }
    
//    public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
//        if (baseUriDereferencer == null) {
//            baseUriDereferencer = signatureConfig.getSignatureFactory().getURIDereferencer();
//        }
//        
//        if (null == uriReference) {
//            throw new NullPointerException("URIReference cannot be null");
//        }
//        if (null == context) {
//            throw new NullPointerException("XMLCrytoContext cannot be null");
//        }
//
//        URI uri;
//        try {
//            uri = new URI(uriReference.getURI());
//        } catch (URISyntaxException e) {
//            throw new URIReferenceException("could not URL decode the uri: "+uriReference.getURI(), e);
//        }
//
//        Part part = findPart(uri);
//        if (part == null) {
//            LOG.debug("cannot resolve, delegating to base DOM URI dereferencer", uri);
//            return this.baseUriDereferencer.dereference(uriReference, context);
//        }
//        
//        // We have to use the original input stream, because what JAXB marshals is different
//        // to the original content, so it digests differently
///*
//        InputStream dataStream;
//        try {
//            //dataStream = part.getInputStream();
//        	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
//        	
//    		if (part instanceof BinaryPart ) {
//    			
//    			dataStream = new ByteArrayInputStream(
//    							((BinaryPart)part).getBytes());
//    			
//    		} else if (part instanceof JaxbXmlPart) {
//    			((JaxbXmlPart)part).marshal( baos );
//    			dataStream = new ByteArrayInputStream(baos.toByteArray());
//    		}  else {
//    			   Document doc =  ((XmlPart)part).getDocument();
//
//    				//  With Crimson, this gives:    				
//					//  Exception in thread "main" java.lang.AbstractMethodError: org.apache.crimson.tree.XmlDocument.getXmlStandalone()Z
//					//	at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(DOM2TO.java:373)
//					//	at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:127)
//					//	at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:94)
//					//	at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transformIdentity(TransformerImpl.java:662)
//					//	at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:708)
//					//	at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:313)
//					//	at org.docx4j.model.datastorage.CustomXmlDataStorageImpl.writeDocument(CustomXmlDataStorageImpl.java:174)
//    				DOMSource source = new DOMSource(doc);
//    				XmlUtils.getTransformerFactory().newTransformer().transform(source,
//    						 new StreamResult(baos) );    			
//        			dataStream = new ByteArrayInputStream(baos.toByteArray());
//    		}
//
////            // workaround for office 2007 pretty-printed .rels files
////            if (part.getPartName().toString().endsWith(".rels")) {
////                // although xmlsec has an option to ignore line breaks, currently this
////                // only affects .rels files, so we only modify these
////                // http://stackoverflow.com/questions/4728300
////                ByteArrayOutputStream bos = new ByteArrayOutputStream();
////                for (int ch; (ch = dataStream.read()) != -1; ) {
////                    if (ch == 10 || ch == 13) continue;
////                    bos.write(ch);
////                }
////                dataStream = new ByteArrayInputStream(bos.toByteArray());
//            
//        } catch (Exception e) {
//            throw new URIReferenceException("I/O error: " + e.getMessage(), e);
//        }
//        */
//        
//        InputStream dataStream;
//		try {
//			dataStream = getInputStream( part);
//		} catch (Docx4JException e) {
//			throw new URIReferenceException(e);
//		}
//        
//        return new OctetStreamData(dataStream, uri.toString(), null);
//    }
//    
//	private InputStream getInputStream(Part part) throws Docx4JException {
//		
//		// Lazy unmarshal
//		InputStream is = null;
//		PartStore partStore = part.getPackage().getSourcePartStore();
//		if (partStore==null) {
//			LOG.info("No PartStore defined for this package (it was probably created, not loaded). " );
//			LOG.info(part.getPartName().getName() + ": did you initialise its contents to something?");
//			
//			throw new NotImplementedException("Can only validate pre-existing signature");
//			// until canonicalisation is improved
//		} 			
//		
//		try {
//			String name = part.getPartName().getName();				
//			return partStore.loadPart( name.substring(1));
//		} finally {
//			IOUtils.closeQuietly(is);
//		}			
//	}       
//
    private Part findPart(String path) {
//        LOG.debug("dereference " + uri);
//
//        String path = uri.getPath();
        if (path == null || "".equals(path)) {
//            LOG.debug( "empty part name ");
            return null;
        }
        
        PartName ppn;
        try {
//            ppn = PackagingURIHelper.createPartName(path);
        	ppn = new PartName(path);
        } catch (InvalidFormatException e) {
            LOG.warn("illegal part name (not expected) " + path);
            return null;
        }
        
        return signatureConfig.getOpcPackage().getParts().get(ppn);
    }
    

    private InputStream getInputStreamFromPartStore(Part part) throws Docx4JException {
		
		InputStream is = null;

		PartStore partStore = part.getPackage().getSourcePartStore();
		
			if (partStore==null) {
				LOG.info("No PartStore defined for this package (it was probably created, not loaded). " );
				LOG.info(part.getPartName().getName() + ": did you initialise its contents to something?");
				return null;
				// or we could create it, with a bit of effort;
				// as to which see http://stackoverflow.com/questions/1090458/instantiating-a-generic-class-in-java
			} 			
			
			try {
				String name = part.getPartName().getName();
									
				is = partStore.loadPart( name.substring(1));
				/* With docx4j's usual ZipPartStore, 
				 * that creates a new InputStream every time,
				 * so we don't need to worry about whether 
				 * the IS has been read or not already.
				 */
				if (is==null) {
					LOG.warn(name + " missing from part store");
				} else {
					
					// No need to do this, since the is won't be re-used (assuming our usual part store)
//					if (is.markSupported() ) {
//						// When reading from zip, we use a ByteArrayInputStream,
//						// which does support this.
//							is.reset();	
//					}
					
				}
//			} catch (IOException e) {
//				LOG.error("Can't reset InputStream for part " + part.getPartName());
//				throw new Docx4JException(e.getMessage(), e);
//			} catch (Docx4JException e) {
//				log.error(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(is);
			}			
		
		return is;
	}    
}
