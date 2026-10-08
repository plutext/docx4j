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

package org.docx4j.dsig.crypt.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;














//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
//import org.docx4j.org.apache.poi.util.XmlSort;
import org.apache.xml.security.binding.xmldsig.TransformType;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
//import org.apache.xmlbeans.XmlCursor;
//import org.apache.xmlbeans.XmlException;
//import org.apache.xmlbeans.XmlObject;
//import org.apache.xmlbeans.XmlOptions;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature.RelationshipReferenceDocument;
//import org.openxmlformats.schemas.xpackage.x2006.relationships.CTRelationship;
//import org.openxmlformats.schemas.xpackage.x2006.relationships.CTRelationships;
//import org.openxmlformats.schemas.xpackage.x2006.relationships.RelationshipsDocument;
//import org.openxmlformats.schemas.xpackage.x2006.relationships.STTargetMode;
//import org.w3.x2000.x09.xmldsig.TransformDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.docx4j.dsig.crypt.DSigJAXBContext;
import org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature.CTRelationshipReference;
import org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature.Docx4JRelationshipReferences;
import org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature.ObjectFactory;

import static org.docx4j.dsig.crypt.facets.SignatureFacet.OO_DIGSIG_NS;
import static org.docx4j.dsig.crypt.facets.SignatureFacet.XML_NS;


/**
 * JSR105 implementation of the RelationshipTransform transformation.
 * 
 * <p>
 * Specs: http://openiso.org/Ecma/376/Part2/12.2.4#26
 * </p>
 */
public class RelationshipTransformService extends TransformService {

    public static final String TRANSFORM_URI = "http://schemas.openxmlformats.org/package/2006/RelationshipTransform";

    private final List<String> sourceIds;

//    private static final POILogger LOG = POILogFactory.getLogger(RelationshipTransformService.class);
	private static Logger LOG = LoggerFactory.getLogger(RelationshipTransformService.class);	
	
	private static final ObjectFactory factory = new org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature.ObjectFactory();

    
    /**
     * Relationship Transform parameter specification class.
     */
    public static class RelationshipTransformParameterSpec implements TransformParameterSpec {
        List<String> sourceIds = new ArrayList<String>();
        public void addRelationshipReference(String relationshipId) {
            sourceIds.add(relationshipId);
        }
        public boolean hasSourceIds() {
            return !sourceIds.isEmpty();
        }
    }
    
    
    public RelationshipTransformService() {
        super();
        LOG.debug( "constructor");
        this.sourceIds = new ArrayList<String>();
    }

    /**
     * Register the provider for this TransformService
     * 
     * @see javax.xml.crypto.dsig.TransformService
     */
    public static synchronized void registerDsigProvider() {
        // the xml signature classes will try to find a special TransformerService,
        // which is of course unknown to JCE before ...
        final String dsigProvider = "POIXmlDsigProvider";
        if (Security.getProperty(dsigProvider) == null) {
            Provider p = new Provider(dsigProvider, 1.0, dsigProvider){
                static final long serialVersionUID = 1L;
            };
            p.put("TransformService." + TRANSFORM_URI, RelationshipTransformService.class.getName());
            p.put("TransformService." + TRANSFORM_URI + " MechanismType", "DOM");
            Security.addProvider(p);
        }
    }
    
    
    @Override
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        LOG.debug("init(params)");
        if (!(params instanceof RelationshipTransformParameterSpec)) {
            throw new InvalidAlgorithmParameterException();
        }
        RelationshipTransformParameterSpec relParams = (RelationshipTransformParameterSpec) params;
        for (String sourceId : relParams.sourceIds) {
            this.sourceIds.add(sourceId);
        }
    }

//    @Override
//    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
//        LOG.debug( "init(parent,context)");
//        LOG.debug( "parent java type: " + parent.getClass().getName());
//        DOMStructure domParent = (DOMStructure) parent;
//        Node parentNode = domParent.getNode();
//        
//        try {
//            TransformDocument transDoc = TransformDocument.Factory.parse(parentNode);
//            XmlObject xoList[] = transDoc.getTransform().selectChildren(RelationshipReferenceDocument.type.getDocumentElementName());
//            if (xoList.length == 0) {
//                LOG.log(POILogger.WARN, "no RelationshipReference/@SourceId parameters present");
//            }
//            for (XmlObject xo : xoList) {
//                String sourceId = ((CTRelationshipReference)xo).getSourceId();
//                LOG.debug( "sourceId: ", sourceId);
//                this.sourceIds.add(sourceId);
//            }
//        } catch (XmlException e) {
//            throw new InvalidAlgorithmParameterException(e);
//        }
//    }

    @Override
    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        LOG.debug( "init(parent,context)");
        LOG.debug( "parent java type: " + parent.getClass().getName());
        DOMStructure domParent = (DOMStructure) parent;
        Node parentNode = domParent.getNode();
        
    	TransformType tt;
		try {
			Unmarshaller u = DSigJAXBContext.jcXmlDSig.createUnmarshaller();					
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			
			tt = (TransformType)XmlUtils.unwrap(u.unmarshal( parentNode ));
			
		} catch (JAXBException e) {
			throw new InvalidAlgorithmParameterException(e);
		}
    	for (Object o : tt.getContent()) {
    		
    		o = XmlUtils.unwrap(o);
    		
    		//if (n.getLocalName().equals("RelationshipReference")) {
    		if (o instanceof CTRelationshipReference) {  // org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature.CTRelationshipReference
    			
    			String sourceId = ((CTRelationshipReference)o).getSourceId();
    			if (sourceId==null) {
                    LOG.warn( "no RelationshipReference/@SourceId parameters present");        				
    			} else {
                    LOG.debug( "sourceId: "+ sourceId);
                    this.sourceIds.add(sourceId);        				
    			}
    		}
    	}
    }
    
    
    @Override
    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
    	
    	/*  Invoked in signing process:
    	 * 
				at org.docx4j.dsig.crypt.services.RelationshipTransformService.marshalParams(RelationshipTransformService.java:216)
				at org.apache.jcp.xml.dsig.internal.dom.DOMTransform.marshal(DOMTransform.java:120)
				at org.apache.jcp.xml.dsig.internal.dom.Marshaller$11.marshalObject(Marshaller.java:214)
				at org.apache.jcp.xml.dsig.internal.dom.Marshaller$11.marshalObject(Marshaller.java:210)
				at org.apache.jcp.xml.dsig.internal.dom.XmlWriterToTree.marshalStructure(XmlWriterToTree.java:176)
				at org.apache.jcp.xml.dsig.internal.dom.DOMReference.marshal(DOMReference.java:327)
				at org.apache.jcp.xml.dsig.internal.dom.DOMManifest.marshal(DOMManifest.java:137)
				at org.apache.jcp.xml.dsig.internal.dom.Marshaller$12.marshalObject(Marshaller.java:223)
				at org.apache.jcp.xml.dsig.internal.dom.Marshaller$12.marshalObject(Marshaller.java:219)
				at org.apache.jcp.xml.dsig.internal.dom.XmlWriterToTree.marshalStructure(XmlWriterToTree.java:176)
				at org.apache.jcp.xml.dsig.internal.dom.DOMXMLObject.marshal(DOMXMLObject.java:177)
				at org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignature.marshal(DOMXMLSignature.java:229)
				at org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignature.sign(DOMXMLSignature.java:329)
				at org.docx4j.dsig.crypt.SignatureInfo.preSign(SignatureInfo.java:508)
				at org.docx4j.dsig.crypt.SignatureInfo.confirmSignature(SignatureInfo.java:314)
				
	    	 */
    	
//    	(new Throwable()).printStackTrace();
    	
        LOG.debug( "marshallParams(parent,context)");
        DOMStructure domParent = (DOMStructure) parent;
        Element parentNode = (Element)domParent.getNode();
        // parentNode.setAttributeNS(XML_NS, "xmlns:mdssi", XML_DIGSIG_NS);
        Document doc = parentNode.getOwnerDocument();
        
        /* Looks messy when MOXy eventually gets it
         * 
            <ns1:RelationshipReference xmlns:ns1="http://schemas.openxmlformats.org/package/2006/digital-signature" SourceId="rId3"/>
            <ns2:RelationshipReference xmlns:ns2="http://schemas.openxmlformats.org/package/2006/digital-signature" SourceId="rId2"/>
            <ns3:RelationshipReference xmlns:ns3="http://schemas.openxmlformats.org/package/2006/digital-signature" SourceId="rId1"/>
            <ns4:RelationshipReference xmlns:ns4="http://schemas.openxmlformats.org/package/2006/digital-signature" SourceId="rId6"/>
            <ns5:RelationshipReference xmlns:ns5="http://schemas.openxmlformats.org/package/2006/digital-signature" SourceId="rId5"/>
            <ns6:RelationshipReference xmlns:ns6="http://schemas.openxmlformats.org/package/2006/digital-signature" SourceId="rId4"/>
         * 
        
		        for (String sourceId : this.sourceIds) {
		//            RelationshipReferenceDocument relRef = RelationshipReferenceDocument.Factory.newInstance();
		//            relRef.addNewRelationshipReference().setSourceId(sourceId);
		//            Node n = relRef.getRelationshipReference().getDomNode();
		//            n = doc.importNode(n, true);
		//            parentNode.appendChild(n);
		        	
		        	Element e = doc.createElementNS(OO_DIGSIG_NS // mdssi
		        			, "RelationshipReference");
		        	//e.setAttributeNS(XML_NS, "xmlns:mdssi", OO_DIGSIG_NS);
		        	e.setAttribute("SourceId", sourceId);
		        	parentNode.appendChild(e);
		        }
		        
		    so do it in a JAXB way.
         */
        
        Docx4JRelationshipReferences d4jRelRefs = new Docx4JRelationshipReferences();
        for (String sourceId : this.sourceIds) {
        	CTRelationshipReference relRef = new CTRelationshipReference();
        	relRef.setSourceId(sourceId);
        	d4jRelRefs.getRelationshipReference().add(relRef);
        }
        
		Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(d4jRelRefs, DSigJAXBContext.jcXmlDSig);
		
        Element e = (Element)doc.importNode(tmpDoc.getDocumentElement(), true);
       
        
        NodeList children = e.getChildNodes();
        for( int i=children.getLength(); i>0;  i--) {
            parentNode.appendChild(children.item(i-1));        	
        }
        
        
        
        
        
    }
    
    public AlgorithmParameterSpec getParameterSpec() {
        LOG.debug( "getParameterSpec");
        return null;
    }

    public Data transform(Data data, XMLCryptoContext context) throws TransformException {
        LOG.debug( "transform(data,context)");
        LOG.debug( "data java type: " + data.getClass().getName());
        OctetStreamData octetStreamData = (OctetStreamData) data;
        LOG.debug( "URI: " + octetStreamData.getURI());
        InputStream octetStream = octetStreamData.getOctetStream();
        
        // org.openxmlformats.schemas.xpackage.x2006.relationships.CTRelationships
        // = org.docx4j.relationships.Relationships
        
        Relationships rels;
		try {
			rels = (Relationships)XmlUtils.unmarshal(octetStream, Context.jcRelationships);
		} catch (JAXBException e1) {
          throw new TransformException(e1.getMessage(), e1);
		}
		
//        RelationshipsDocument relDoc;
//        try {
//        	
//            relDoc = RelationshipsDocument.Factory.parse(octetStream);
//        } catch (Exception e) {
//            throw new TransformException(e.getMessage(), e);
//        }
//        LOG.debug( "relationships document", relDoc);
//        
//        Relationships rels = relDoc.getRelationships();
		
        //List<Relationship> relList = rels.getRelationship();
        Iterator<Relationship> relIter = rels.getRelationship().iterator();
        while (relIter.hasNext()) {
            Relationship rel = relIter.next();
            /*
             * See: ISO/IEC 29500-2:2008(E) - 13.2.4.24 Relationships Transform
             * Algorithm.
             */
            if (!this.sourceIds.contains(rel.getId())) {
                LOG.debug( "removing element: " + rel.getId());
                relIter.remove();
            } else {
//                if (!rel.isSetTargetMode()) {
//                    rel.setTargetMode(STTargetMode.INTERNAL);
//                }
            	if (rel.getTargetMode()==null) {
            		rel.setTargetMode("Internal");
            	}
            }
        }
        
        /* JAXB would marshall to something like:
         * 
         *    <rel:Relationships xmlns:rel="http://schemas.openxmlformats.org/package/2006/relationships"><rel:Relationship Id="rId1" Target="word/document.xml" TargetMode="Internal" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"></rel:Relationship></rel:Relationships>
         * 
         * But that does not meet the requirements of the Relationships Transform Algorithm in the spec.
         * 
         * So, since it is so simple, we'll manually write a string.
         * 
         * An alternative may be to use JAXB's XmlAdapter:
         * 
         *     https://jaxb.java.net/nonav/2.2.4/docs/api/javax/xml/bind/annotation/adapters/XmlAdapter.html
         *     
         * But that seems to be overcomplicating matters.
         * 
         */
        
        // TODO: remove non element nodes ???
        LOG.debug( "# Relationship elements", rels.getRelationship().size());
        
        // Step 2: sort by Id value
		 Collections.sort(rels.getRelationship(), new Comparator<Relationship>(){
		      public int compare(Relationship r1, Relationship r2) {
		          String id1 = r1.getId();
		          String id2 = r2.getId();
		          return id1.compareTo(id2);
		      }
		  });
		 
		 StringBuilder sb = new StringBuilder();
		 sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
		 for(Relationship r : rels.getRelationship()) {
			sb.append("<Relationship"
					+ " Id=\"" +r.getId() +"\""
					+ " Target=\"" +r.getTarget() +"\""
					+ " TargetMode=\"" +r.getTargetMode() +"\""
					+ " Type=\"" +r.getType() +"\" />"); 
		 }		 
		 sb.append("</Relationships>");
		 
		 try {
			return new OctetStreamData(
					 new ByteArrayInputStream(sb.toString().getBytes("UTF-8")) );
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TransformException(e.getMessage(), e);
		}
        
//        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
////            XmlOptions xo = new XmlOptions();
////            xo.setSaveNoXmlDecl();
////            relDoc.save(bos, xo);
//            
//            return new OctetStreamData(
//            		XmlUtils.marshaltoInputStream(rels, true, Context.jcRelationships) );
//            
//            //return new OctetStreamData(new ByteArrayInputStream(bos.toByteArray()));
//        } catch (Exception e) {
//            throw new TransformException(e.getMessage(), e);
//        }
    }

    public Data transform(Data data, XMLCryptoContext context, OutputStream os) throws TransformException {
        LOG.debug( "transform(data,context,os)");
        return null;
    }

    public boolean isFeatureSupported(String feature) {
        LOG.debug( "isFeatureSupported(feature)");
        return false;
    }
}
