/*
 *  Copyright 2016, Plutext Pty Ltd.
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
package org.docx4j.openpackaging.parts.digitalsignature;

//import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.NotImplementedException;
import org.apache.jcp.xml.dsig.internal.dom.DOMKeyInfo;
import org.apache.xml.security.binding.xmldsig.KeyInfoType;
import org.apache.xml.security.binding.xmldsig.ManifestType;
import org.apache.xml.security.binding.xmldsig.ObjectType;
import org.apache.xml.security.binding.xmldsig.ReferenceType;
//import org.docx4j.dsig.crypt.SignatureDocument;
//import org.docx4j.dsig.crypt.XmlException;
import org.apache.xml.security.binding.xmldsig.SignatureType;
//import org.docx4j.org.apache.poi.util.DocumentHelper;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.docx4j.XmlUtils;
//import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
//import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.apache.xml.security.Init;
import org.docx4j.org.apache.xml.security.c14n.Canonicalizer;
import org.docx4j.relationships.Relationship;
//import org.plutext.jaxb.xmldsig.SignatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.docx4j.dsig.crypt.DSigJAXBContext;
import org.docx4j.dsig.crypt.KeyInfoKeySelector;
import org.docx4j.dsig.crypt.SignatureConfig;
import org.docx4j.dsig.crypt.facets.OOXMLSignatureFacet;
import org.docx4j.dsig.crypt.facets.SignatureFacet;
import org.docx4j.dsig.DigitalSignatureException;

/**
 * @author jharrop
 * @since 2.8

 */
public class XmlSignaturePart extends XmlPart {
	
	private static Logger log = LoggerFactory.getLogger(XmlSignaturePart.class);		

	private static final String SECURE_VALIDATION = "org.apache.jcp.xml.dsig.secureValidation";

	public XmlSignaturePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();		
	}

	public XmlSignaturePart() throws InvalidFormatException {
		super(new PartName("/_xmlsignatures/sig1.xml"));
		init();		
	}
	
	public void init() {		
			
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DIGITAL_SIGNATURE_XML_SIGNATURE_PART));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DIGITAL_SIGNATURE);
				
	}
	
	@Override
	public Document getDocument() throws Docx4JException {
		return doc;
	}
	
	JAXBElement<SignatureType> jaxbElement = null;
	
	private JAXBElement<SignatureType> getJaxbElement() throws JAXBException {
		
		if (jaxbElement==null) {
		
			Unmarshaller u = DSigJAXBContext.jcXmlDSig.createUnmarshaller();					
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
	
			jaxbElement = (JAXBElement<SignatureType>)u.unmarshal( doc );
		}
		
		return jaxbElement;
	}
	
	
		
	private CertificateTrustChecker certificateTrustChecker;
	public void setCertificateTrustChecker(
			CertificateTrustChecker certificateTrustChecker) {
		this.certificateTrustChecker = certificateTrustChecker;
	}
	
	/*
	 * http://www.w3.org/TR/xmldsig-core/#sec-CoreValidation
	 * 
	 * The REQUIRED steps of core validation include 
	 * 
	 * (1) reference validation, the verification of the digest contained in each Reference in SignedInfo, and 
	 * 
	 * (2) the cryptographic signature validation of the signature calculated over SignedInfo.
	 * 
	 * Check its non partial
	 * 
	 * Certificate path validation   http://tools.ietf.org/html/rfc5280
	 * 
	 * Signing date - assume the XAdES stuff is done in 
	 */


	// Aspects to validity
	private Boolean validSig = null;
	private Boolean isComplete = null;
	private CertificateTrustCheckResult isTrusted = CertificateTrustCheckResult.UNKNOWN;
	

	private SignatureStatus signatureStatus = SignatureStatus.UNKNOWN;
	
    /**
     * Set the signature status for each signature. This mimics what you see in the Word signatures pane,
     * and checks the validity of the signature (hash), whether it is complete or partial,
     * and the certificate used to sign with.
     * 
	 * If the value is not UNKOWN, it is cached.
	 * 
	 * @param signatureConfig
	 * @return
	 * @throws DigitalSignatureException
	 */
	public SignatureStatus getSignatureStatus(SignatureConfig signatureConfig) throws DigitalSignatureException {
		
		jaxbElement = null;
		
		if (!signatureStatus.equals(SignatureStatus.UNKNOWN)) {
			return signatureStatus;
		}
		
		if (validSig==null) {
			validate( signatureConfig);	
		}
		if (!validSig) {			
			signatureStatus = SignatureStatus.INVALID;
			log.debug("Invalid signature; not checking whether the certificate is trusted etc");
			return signatureStatus;
		}
		
		// if we get here, we know the signature is valid.
		// now, is it partial only?
		// and what about the certificate?

		if (isComplete==null) {
			List<String> unsignedButRequired = getUnsignedRequiredParts();
			isComplete = unsignedButRequired.isEmpty();
		}
		
		if (isTrusted.equals(CertificateTrustCheckResult.UNKNOWN)) {	
			
			if (this.certificateTrustChecker==null) {
//				throw new DigitalSignatureException("No CertificateTrustChecker set.");
				log.warn("No CertificateTrustChecker set; can't check certificate");
				
			} else {
				try {
					isTrusted = this.certificateTrustChecker.isTrusted(getSignerChain(), getSigningTime());
				} catch (/* CertPathValidatorException | CertPathBuilderException */ Exception e) {
					throw new DigitalSignatureException(e.getMessage(), e);
				}
			}
		}
		
		
//		System.out.println(isTrusted);
		
		if (isTrusted.equals(CertificateTrustCheckResult.TRUSTED)) {
			
			if (isComplete) {
				signatureStatus = SignatureStatus.VALID;
			} else {
				signatureStatus =  SignatureStatus.VALID_PARTIAL;
			}	
			
		} else if (isTrusted.equals(CertificateTrustCheckResult.SOFT_FAIL)
				|| isTrusted.equals(CertificateTrustCheckResult.SELF_SIGNED)				
				|| isTrusted.equals(CertificateTrustCheckResult.UNTRUSTED)				
				|| isTrusted.equals(CertificateTrustCheckResult.UNKNOWN)) {
			
			// TODO: revisit UNKNOWN
			
			if (isComplete) {
				signatureStatus =  SignatureStatus.RECOVERABLE;
			} else {
				signatureStatus =  SignatureStatus.RECOVERABLE_PARTIAL;
			}						
		} else /* CertificateTrustCheckResult.HARD_FAIL */ {
			
			signatureStatus =  SignatureStatus.INVALID;
		}
		return signatureStatus;
	}
	
                
        /**
         * @return the signer certificate
         * @throws DigitalSignatureException 
         */
        public X509Certificate getSigner() throws DigitalSignatureException {
        	
            try {
	        	// First, get the X509Certificate certificate
	        	KeyInfoType kit = ((SignatureType)XmlUtils.unwrap(this.getJaxbElement())).getKeyInfo();
	        	Document kitdoc = marshaltoW3CDomDocument(kit, DSigJAXBContext.jcXmlDSig,
	        			 "http://www.w3.org/2000/09/xmldsig#", "KeyInfo", KeyInfoType.class);
	        	
	        	log.debug(XmlUtils.w3CDomNodeToString(kitdoc));
	            //DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
	        	
	        	DOMKeyInfo ki = new DOMKeyInfo(kitdoc.getDocumentElement(), null, null);
	    	
	            KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
	            keySelector.select(ki, KeySelector.Purpose.VERIFY, null, null);
	            
	            return keySelector.getSigner();
	            
	        } catch (Exception e) {
	            String s = "error extracting X509Certificate";
	            log.error( s, e);
	            throw new DigitalSignatureException(s, e);
	        }                
        	
        }
            
        
        /**
         * The certificates in the signature's KeyInfo, the signer first.  Office writes only the
         * signer's; we write the chain we were given.
         * 
         * @since 17.2.1
         */
        public List<X509Certificate> getSignerChain() throws DigitalSignatureException {
        	
        	List<X509Certificate> chain = new ArrayList<X509Certificate>();
        	try {
	        	CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        	NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "X509Certificate");
	        	for (int i=0; i<nl.getLength(); i++) {
	        		byte[] der = java.util.Base64.getMimeDecoder().decode(nl.item(i).getTextContent().trim());
	        		chain.add((X509Certificate)cf.generateCertificate(new ByteArrayInputStream(der)));
	        	}
        	} catch (Exception e) {
	            throw new DigitalSignatureException("error extracting X509Certificate chain", e);
        	}
        	if (chain.isEmpty()) {
        		throw new DigitalSignatureException("No X509Certificate in the signature's KeyInfo");
        	}
        	return chain;
        }
        
        /**
         * The time the signature says it was made: XAdES SigningTime if present, else the OPC
         * SignatureTime; null if neither.  Office writes both, and so do we.  It is a claim by
         * the signer, not proof (an XAdES-T timestamp would be proof; not read here).
         * 
         * @since 17.2.1
         */
        public Date getSigningTime() {
        	
        	String value = null;
        	NodeList nl = doc.getElementsByTagNameNS("http://uri.etsi.org/01903/v1.3.2#", "SigningTime");
        	if (nl.getLength()>0) {
        		value = nl.item(0).getTextContent();
        	} else {
        		nl = doc.getElementsByTagNameNS("http://schemas.openxmlformats.org/package/2006/digital-signature", "Value");
        		if (nl.getLength()>0) {
        			value = nl.item(0).getTextContent();
        		}
        	}
        	if (value==null) return null;
        	try {
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(value.trim()).toGregorianCalendar().getTime();
			} catch (Exception e) {
				log.warn("Can't read signing time '" + value + "': " + e.getMessage());
				return null;
			}
        }
            
        private InputStream getInputStreamFromPartStore() throws Docx4JException {
    		
    		InputStream is = null;

    		PartStore partStore = this.getPackage().getSourcePartStore();
    		
//    			if (partStore==null) {
//    				LOG.info("No PartStore defined for this package (it was probably created, not loaded). " );
//    				LOG.info(part.getPartName().getName() + ": did you initialise its contents to something?");
//    				return null;
//    				// or we could create it, with a bit of effort;
//    				// as to which see http://stackoverflow.com/questions/1090458/instantiating-a-generic-class-in-java
//    			} 			
    			
    			try {
    				String name = this.getPartName().getName();
    									
    				is = partStore.loadPart( name.substring(1));
    				/* With docx4j's usual ZipPartStore, 
    				 * that creates a new InputStream every time,
    				 * so we don't need to worry about whether 
    				 * the IS has been read or not already.
    				 */
    				if (is==null) {
//    					LOG.warn(name + " missing from part store");
    				} else {
    					
    					// No need to do this, since the is won't be re-used (assuming our usual part store)
//    					if (is.markSupported() ) {
//    						// When reading from zip, we use a ByteArrayInputStream,
//    						// which does support this.
//    							is.reset();	
//    					}
    					
    				}
//    			} catch (IOException e) {
//    				LOG.error("Can't reset InputStream for part " + part.getPartName());
//    				throw new Docx4JException(e.getMessage(), e);
//    			} catch (Docx4JException e) {
//    				log.error(e.getMessage(), e);
    			} finally {
    				IOUtils.closeQuietly(is);
    			}			
    		
    		return is;
    	}    
    	
        /**
         * You probably want to use getSignatureStatus instead!
         * 
		 * This doesn't check whether the signature itself is trusted
		 * (ie its chain of trust), or whether the signature is partial or complete.
		 * 
		 * Its just a subset of getSignatureStatus.
		 * 
		 * @param  signatureConfig  if null, a new SignatureConfig object will be created
         * @return true, when the xml signature is valid, false otherwise
         * 
         * @throws DigitalSignatureException if the signature can't be extracted or if its malformed
         */
        @SuppressWarnings("unchecked")
        private boolean validate(SignatureConfig signatureConfig) throws DigitalSignatureException {
        	
        	if (signatureConfig==null) {
        		signatureConfig = new SignatureConfig();
//        		SignatureDetail currentSignatureDetail = ;
//        		signatureConfig.setCurrentSignatureDetail( currentSignatureDetail);
        	}
        	
            signatureConfig.init(true);
            /* avoid
					 org.apache.xml.security.utils.resolver.ResourceResolverException: Could not find a resolver for URI /word/document.xml?ContentType=application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml and Base null
						at org.apache.xml.security.utils.resolver.ResourceResolver.internalGetInstance(ResourceResolver.java:117)
						at org.apache.xml.security.utils.resolver.ResourceResolver.getInstance(ResourceResolver.java:74)
						at org.apache.jcp.xml.dsig.internal.dom.DOMURIDereferencer.dereference(DOMURIDereferencer.java:110)            
					 */

        	// Adapted from POI code, from crypt.dsig.SignatureInfo
        	
            KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
            try {
            	
//            	Document doc = DocumentHelper.readDocument(this.getInputStream());
            	
            	// This canonicalises it appropriately
//            	Document doc = marshaltoW3CDomDocument(XmlUtils.unwrap(this.getJaxbElement()), this.jc,
//            			 "http://www.w3.org/2000/09/xmldsig#", "Signature", SignatureType.class);
            	
            	
//            	InputStream is = this.getInputStreamFromPartStore();
//            	DocumentBuilder builder = XmlUtils.getNewDocumentBuilder();
//            	
//            	Document doc = builder.parse(is);
            	
            	log.debug(XmlUtils.w3CDomNodeToString(doc));
            	
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList nl = (NodeList)xpath.compile("//*[@Id]").evaluate(doc, XPathConstants.NODESET);
                for (int i=0; i<nl.getLength(); i++) {
                    ((Element)nl.item(i)).setIdAttribute("Id", true);
                }
                
                DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, doc);
                domValidateContext.setProperty("org.jcp.xml.dsig.validateManifests", Boolean.TRUE);
                domValidateContext.setURIDereferencer(signatureConfig.getUriDereferencer());
                brokenJvmWorkaround(domValidateContext);
                
                /* Santuario 3's secure validation limits a Manifest to 30 references, which
                 * is fewer than the parts in the simplest pptx, and that limit can't be configured. 
                 * So unmarshal without it, having made its unmarshal-time checks 
                 * (and stricter ones) ourselves. */
                SignatureStructureCheck.check(doc);
                domValidateContext.setProperty(SECURE_VALIDATION, Boolean.FALSE);
    
                XMLSignatureFactory xmlSignatureFactory = signatureConfig.getSignatureFactory();
                XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
                	// org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignature (extends DOMStructure)

                // The checks Santuario makes while validating (as opposed to unmarshalling) stay on
                domValidateContext.setProperty(SECURE_VALIDATION, Boolean.TRUE);
                
                // TODO: replace with property when xml-sec patch is applied
                for (Reference ref : (List<Reference>)xmlSignature.getSignedInfo().getReferences()) {
                    SignatureFacet.brokenJvmWorkaround(ref);
                }
                for (XMLObject xo : (List<XMLObject>)xmlSignature.getObjects()) {
                    for (XMLStructure xs : (List<XMLStructure>)xo.getContent()) {
                        if (xs instanceof Manifest) {
                           for (Reference ref : (List<Reference>)((Manifest)xs).getReferences()) {
                               SignatureFacet.brokenJvmWorkaround(ref);
                               
                               
//                               // null System.out.println(ref.getId());
//                               if (ref.getDereferencedData()!=null) {
//                            	   System.out.println(ref.getDereferencedData().toString());
//                               }
                           }
                        }
                    }
                }
                
                validSig = xmlSignature.validate(domValidateContext);
                
                return validSig;
            } catch (Exception e) {
                String s = "error in marshalling and validating the signature: " + e.getMessage();
                log.error( s, e);
                throw new DigitalSignatureException(s, e);
            }
        }
        
        private ObjectType getObjectById(String id) throws JAXBException {

        	SignatureType st = (SignatureType)XmlUtils.unwrap(this.getJaxbElement());
        	for (ObjectType o : st.getObject() ) {
        		if (o.getId().equals(id)) return o;
        	}
        	return null;
        }
        
        /**
         * You probably want to use getSignatureStatus instead!
         * 
		 * This doesn't check whether the signature itself is trusted
		 * (ie its chain of trust), or whether the signature is partial or complete.
		 * 
		 * It just identifies any parts which cause this to be a partial signature; 
		 * its a subset of getSignatureStatus.
         * 
         * @return
         */
        public List<String> getUnsignedRequiredParts() {
        	
        	// TODO: better to return the actual parts?
        	
        	/* get the parts signed from:
        	 * 
				  <Object Id="idPackageObject" xmlns:mdssi="http://schemas.openxmlformats.org/package/2006/digital-signature">
				    <Manifest>
				      <Reference URI="/word/styles.xml?ContentType=application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml">
				      
             * if you skipped images (or other files); see OOXMLSignatureFacet
             *    
             * you can alter their content without invalidating
             * the signatures (!),
             * but Word will warn its a "valid partial signature"
             * 
             * so the idea here is to identify   				      
             */
        	ObjectType idPackageObject=null;
			try {
				idPackageObject = getObjectById("idPackageObject");
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
			}
        	ManifestType manifest = (ManifestType)XmlUtils.unwrap(idPackageObject.getContent().get(0));
        	
        	Set<String> referenceURIs = new HashSet<String>();
        	for( ReferenceType reference : manifest.getReference()) {
        		referenceURIs.add(reference.getURI());
        	}        	
        	
        	// Now check there's nothing extra in the actual parts
        	Set<String> actualReferences = getReferences();
        	
        	// Now identify any differences
        	List<String> unsigned = new ArrayList<String>();
        	
        	for (String actual : actualReferences) {
        		
        		if (!referenceURIs.contains(actual)) {
        			unsigned.add(actual);
        			log.info(actual + " is unsigned ");
        		}
        	}
        	
        	return unsigned;
        	
        }

        private void brokenJvmWorkaround(XMLValidateContext context) {
            // workaround for https://bugzilla.redhat.com/show_bug.cgi?id=1155012
            Provider bcProv = Security.getProvider("BC");
            if (bcProv != null) {
                context.setProperty("org.jcp.xml.dsig.internal.dom.SignatureProvider", bcProv);
            }        
        }
        
		
        
//        public void marshal(java.io.OutputStream os) throws JAXBException {
//        	
//        	if (signedDomDocument==null) {
//        		
//        		log.warn("Can't write signed DOM, JAXB might mangle this..");
//        		super.marshal(os);
//        		
//        	} else {
//        		
//        		// Try to avoid JAXB changing anything!
//
//        		DOMSource source = new DOMSource(this.signedDomDocument);
//        		try {
//					XmlUtils.getTransformerFactory().newTransformer().transform(source,
//						 new StreamResult(os) );
//				} catch (Exception e) {
//					throw new JAXBException(e.getMessage(), e);
//				}
//        	}
//        	
//        	
//        }
		

//		/**
//    	 * Marshal the content tree rooted at <tt>jaxbElement</tt> into an output
//    	 * stream
//    	 * 
//    	 * @param os
//    	 *            XML will be added to this stream.
//    	 * @param namespacePrefixMapper
//    	 *            namespacePrefixMapper
//    	 * 
//    	 * @throws JAXBException
//    	 *             If any unexpected problem occurs during the marshalling.
//    	 */
//        @Override
//        public void marshal(java.io.OutputStream os, Object namespacePrefixMapper) throws JAXBException {
//        	
//    		try {
//    			Marshaller marshaller = jc.createMarshaller();
//    			
//                // In SignatureInfo, DOMSignedInfo doesn't pretty print, so be sure not to do that here after signing!
//    			marshaller.setProperty("jaxb.formatted.output", false);
//    			
//    			NamespacePrefixMapperUtils.setProperty(marshaller, namespacePrefixMapper);
//    			
//    			log.debug("marshalling " + this.getClass().getName() );	
//    			getContents();
//
//	    		// Instead of marshaller.marshal(jaxbElement, os)
//    			// in the sig part we always clean up the namespaces via canonicalization
//    			// For signing, we only need to do this to this part.  All other parts can be marshalled as usual.
//    			
//	    		Document doc = XmlUtils.marshaltoW3CDomDocument(jaxbElement, jc);
//	    		
//	    		log.debug("Input to Canonicalizer: " + XmlUtils.w3CDomNodeToString(doc));
//	    		
//	    		Init.init();
//	    		Canonicalizer c = Canonicalizer.getInstance(CanonicalizationMethod.EXCLUSIVE);
//	    		byte[] bytes = c.canonicalizeSubtree(doc, "xd mdssi dssi"); // so these are declared at the root level; consistent with how we had it at signing time thanks to SignatureMarshalListener
//	    		IOUtils.write(bytes, os);
//
//    		} catch (Docx4JException e) {
//    			log.error(e.getMessage(), e);
//    			throw new JAXBException(e);  // avoid change to method signature
//    		} catch (JAXBException e) {
//    			log.error(e.getMessage(), e);
//    			throw e;
//    		} catch (Exception e) {
//    			log.error(e.getMessage(), e);
//    			throw new JAXBException(e);  // avoid change to method signature
//    		}
//    	}
        
    	/** The method from XmlUtils, but with canonicalisation.  */
    	private static org.w3c.dom.Document marshaltoW3CDomDocument(Object o, JAXBContext jc,
    			String uri, String local, Class declaredType) throws JAXBException {

    		try {

    			Marshaller marshaller = jc.createMarshaller();
    			
                // In SignatureInfo, DOMSignedInfo doesn't pretty print, so be sure not to do that here after signing!
    			marshaller.setProperty("jaxb.formatted.output", false);
    			
    			org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().newDocument();

    			NamespacePrefixMapperUtils.setProperty(marshaller, 
    					NamespacePrefixMapperUtils.getPrefixMapper());			

    			// See http://weblogs.java.net/blog/kohsuke/archive/2006/03/why_does_jaxb_p.html
    			marshaller.marshal( 
    					new JAXBElement(new QName(uri,local), declaredType, o ),
    					doc);

	    		Init.init();
	    		Canonicalizer c = Canonicalizer.getInstance(CanonicalizationMethod.EXCLUSIVE);
	    		byte[] bytes = c.canonicalizeSubtree(doc, "xd mdssi dssi"); // so these are declared at the root level; consistent with how we had it at signing time thanks to SignatureMarshalListener

	    		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(bytes));
	    		
    		} catch (Exception e) {
    		    throw new JAXBException(e);
    		}
    	}
        

        private static final String RELS_CONTENT_TYPE = "application/vnd.openxmlformats-package.relationships+xml";
        
        /** Document properties, which Office does not sign; and the signatures themselves */
        private static final Set<String> CONTENT_TYPES_NOT_SIGNED = new HashSet<String>(java.util.Arrays.asList(
        		"application/vnd.openxmlformats-package.core-properties+xml",
        		"application/vnd.openxmlformats-officedocument.extended-properties+xml",
        		"application/vnd.openxmlformats-officedocument.custom-properties+xml",
        		"application/vnd.openxmlformats-package.digital-signature-origin",
        		"application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml",
        		"application/vnd.openxmlformats-package.digital-signature-certificate"));

        /**
         * What a complete signature covers, as Manifest reference URIs: every part in the 
         * package other than its properties, thumbnail and signatures, and the relationships 
         * part of each of those parts (and of the package).
         * 
         * This is deliberately worked out from the parts in the package, and not by following 
         * relationships as OOXMLSignatureFacet does when signing, so that a part which 
         * signing fails to reach is noticed here.
         */
        private Set<String> getReferences() {

        	Set<String> manifestReferences  = new HashSet<String>();
        	
        	addRelsReference(this.getPackage().getRelationshipsPart(), manifestReferences);
        	
        	for (Part p : this.getPackage().getParts().getParts().values()) {
        		
        		if (!isRequired(p)) continue;
        		
        		manifestReferences.add(p.getPartName().getName() + "?ContentType=" + p.getContentType());
        		addRelsReference(p.getRelationshipsPart(false), manifestReferences);
        	}
    		return manifestReferences;
        }
        
        private void addRelsReference(RelationshipsPart rp, Set<String> manifestReferences) {
        	
        	if (rp==null) return;
        	
            for (Relationship relationship : rp.getJaxbElement().getRelationship()) {
            	if (OOXMLSignatureFacet.isSignedRelationship(relationship.getType())) {
                	manifestReferences.add(rp.getPartName().getName() + "?ContentType=" + RELS_CONTENT_TYPE);
                	return;
            	}
            }
        }
        
        private boolean isRequired(Part p) {
        	
        	if (p instanceof RelationshipsPart) return false; // handled with its source part
        	
        	String contentType = p.getContentType();
        	if (contentType==null || CONTENT_TYPES_NOT_SIGNED.contains(contentType)) return false;
        	
        	String partName = p.getPartName().getName();
        	if (partName.startsWith("/_xmlsignatures/")) return false;

        	for (Relationship source : p.getSourceRelationships()) {
        		if (source.getType().endsWith("/metadata/thumbnail")) return false;
        	}
        	return true;
        }

    	
        	
}
