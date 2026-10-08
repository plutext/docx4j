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

import static org.docx4j.dsig.crypt.facets.SignatureFacet.XML_DIGSIG_NS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;

import org.apache.commons.io.IOUtils;
import org.apache.jcp.xml.dsig.internal.dom.DOMReference;
import org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo;
import org.apache.xml.security.Init;
import org.apache.xml.security.binding.xmldsig.SignatureType;
import org.apache.xml.security.utils.Base64;
import org.docx4j.XmlUtils;
//import org.docx4j.jaxb.Context;
//import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureOriginPart;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.util.DocumentHelper;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import org.docx4j.dsig.crypt.SignatureConfig.SignatureConfigurable;
import org.docx4j.dsig.crypt.facets.SignatureFacet;
import org.docx4j.dsig.crypt.services.RelationshipTransformService;
import org.docx4j.dsig.DigitalSignatureException;
import org.docx4j.dsig.SignaturesNotPresentException;


/**
 * <p>This class is the default entry point for XML signatures and can be used for
 * validating an existing signed office document and signing a office document.</p>
 * 
 * <p><b>Validating a signed office document</b></p>
 * 
 * <pre>
 * OPCPackage pkg = OPCPackage.open(..., PackageAccess.READ);
 * SignatureConfig sic = new SignatureConfig();
 * sic.setOpcPackage(pkg);
 * SignatureInfo si = new SignatureInfo();
 * si.setSignatureConfig(sic);
 * boolean isValid = si.validate();
 * ...
 * </pre>
 * 
 * <p><b>Signing an office document</b></p>
 * 
 * <pre>
 * // loading the keystore - pkcs12 is used here, but of course jks &amp; co are also valid
 * // the keystore needs to contain a private key and it's certificate having a
 * // 'digitalSignature' key usage
 * char password[] = "test".toCharArray();
 * File file = new File("test.pfx");
 * KeyStore keystore = KeyStore.getInstance("PKCS12");
 * FileInputStream fis = new FileInputStream(file);
 * keystore.load(fis, password);
 * fis.close();
 * 
 * // extracting private key and certificate
 * String alias = "xyz"; // alias of the keystore entry
 * Key key = keystore.getKey(alias, password);
 * X509Certificate x509 = (X509Certificate)keystore.getCertificate(alias);
 * 
 * // filling the SignatureConfig entries (minimum fields, more options are available ...)
 * SignatureConfig signatureConfig = new SignatureConfig();
 * signatureConfig.setKey(keyPair.getPrivate());
 * signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));
 * OPCPackage pkg = OPCPackage.open(..., PackageAccess.READ_WRITE);
 * signatureConfig.setOpcPackage(pkg);
 * 
 * // adding the signature document to the package
 * SignatureInfo si = new SignatureInfo();
 * si.setSignatureConfig(signatureConfig);
 * si.confirmSignature();
 * // optionally verify the generated signature
 * boolean b = si.verifySignature();
 * assert (b);
 * // write the changes back to disc
 * pkg.close();
 * </pre>
 * 
 * <p><b>Implementation notes:</b></p>
 * 
 * <p>Although there's a XML signature implementation in the Oracle JDKs 6 and higher,
 * compatibility with IBM JDKs is also in focus (... but maybe not thoroughly tested ...).
 * Therefore we are using the Apache Santuario libs (xmlsec) instead of the built-in classes,
 * as the compatibility seems to be provided there.</p>
 * 
 * <p>To use SignatureInfo and its sibling classes, you'll need to have the following libs
 * in the classpath:</p>
 * <ul>
 * <li>BouncyCastle bcpkix and bcprov (tested against 1.51)</li>
 * <li>Apache Santuario "xmlsec" (tested against 2.0.1)</li>
 * <li>and slf4j-api (tested against 1.7.7)</li>
 * </ul>
 */
public class SignatureInfo implements SignatureConfigurable {

	private static Logger LOG = LoggerFactory.getLogger(SignatureInfo.class);	
    private static boolean isInitialized = false;
    
    private SignatureConfig signatureConfig;


    
    /**
     * Constructor initializes xml signature environment, if it hasn't been initialized before
     */
    public SignatureInfo() {
        initXmlProvider();        
    }
    
    /**
     * @return the signature config
     */
    public SignatureConfig getSignatureConfig() {
        return signatureConfig;
    }

    /**
     * @param signatureConfig the signature config, needs to be set before a SignatureInfo object is used
     */
    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }

//    /**
//     * @return true if all signatures are valid
//     * @throws SignaturesNotPresentException 
//     */
//    public boolean verifySignatures() throws SignaturesNotPresentException {
//
//    	List<XmlSignaturePart> signatureParts = getSignatureParts();
//    	if (signatureParts.isEmpty()) throw new SignaturesNotPresentException("No signatures present");
//    	
//        // http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
//        for (XmlSignaturePart sp : getSignatureParts()){
//
//        	boolean isValid = sp.validate(signatureConfig);
//            if (!isValid) return false;
//        }
//        
//        return true;
//    }

//    public List<XmlSignaturePart> verifySignatureParts() throws SignaturesNotPresentException, DigitalSignatureException {
//    	
//    	List<XmlSignaturePart> signatureParts = getSignatureParts();
//    	if (signatureParts.isEmpty()) throw new SignaturesNotPresentException("No signatures present");
//    	
//        // http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
//        for (XmlSignaturePart sp : getSignatureParts()){
//
//        	sp.validate(signatureConfig);
//        }
//        return signatureParts;
//    }
    
//    public boolean verifySignature(XmlSignaturePart sp) {
//        return sp.validate(signatureConfig);
//    }
    
    /**
     * add the xml signature to the document
     *
     * @throws XMLSignatureException
     * @throws MarshalException
     * @throws DigitalSignatureException 
     */
    public void confirmSignature() throws XMLSignatureException, MarshalException, DigitalSignatureException {
    	
//    	for (KeyStore.PrivateKeyEntry pke  : signatureConfig.getSignatureDetailList().get()) {
//    		
//    		signatureConfig.setCurrentPrivateKeyEntry(pke);
    	
    	for (SignatureDetail signatureDetail : signatureConfig.getSignatureDetailList().get()) {
    		
    		signatureConfig.setCurrentSignatureDetail(signatureDetail);
    		
	        Document document = DocumentHelper.createDocument();
	        
	        // operate
	        DigestInfo digestInfo = preSign(document, null);
	
	        // setup: key material, signature value
	        byte[] signatureValue = signDigest(digestInfo.digestValue);
	        
	        // operate: postSign
	        postSign(document, signatureValue);
    	}
    }

    /**
     * Sign (encrypt) the digest with the private key.
     * Currently only rsa is supported.
     *
     * @param digest the hashed input
     * @return the encrypted hash
     * @throws DigitalSignatureException 
     */
    public byte[] signDigest(byte digest[]) throws DigitalSignatureException {
        Cipher cipher = CryptoFunctions.getCipher(signatureConfig.getCurrentSignatureDetail().getKey(), CipherAlgorithm.rsa
            , ChainingMode.ecb, null, Cipher.ENCRYPT_MODE, "PKCS1Padding");
            
        try {
            ByteArrayOutputStream digestInfoValueBuf = new ByteArrayOutputStream();
            digestInfoValueBuf.write(signatureConfig.getHashMagic());
            digestInfoValueBuf.write(digest);
            byte[] digestInfoValue = digestInfoValueBuf.toByteArray();
            byte[] signatureValue = cipher.doFinal(digestInfoValue);
            return signatureValue;
        } catch (Exception e) {
            throw new DigitalSignatureException(e.getMessage(), e);
        }
    }
    
    public List<XmlSignaturePart> getSignatureParts() throws DigitalSignatureException {

        signatureConfig.init(true);
        OpcPackage pkg = signatureConfig.getOpcPackage();
    	
		List<XmlSignaturePart> signatureParts = new ArrayList<XmlSignaturePart>(); 

		Relationship originRel = pkg.getRelationshipsPart().getRelationshipByType(Namespaces.DIGITAL_SIGNATURE_ORIGIN);
		SignatureOriginPart originSigsPart = null; 
        if (originRel == null) {
        	return signatureParts;
        }
        
    	originSigsPart = (SignatureOriginPart)pkg.getRelationshipsPart().getPart(originRel);
        
    	for (Relationship r : originSigsPart.getRelationshipsPart().getRelationships().getRelationship() ) {
    		if (r.getType().equals(Namespaces.DIGITAL_SIGNATURE)) {
    			signatureParts.add((XmlSignaturePart)originSigsPart.getRelationshipsPart().getPart(r));
    		}
    	}
    	return signatureParts;
    }
    
    /**
     * Initialize the xml signing environment and the bouncycastle provider 
     */
    public static synchronized void initXmlProvider() {
        if (isInitialized) return;
        isInitialized = true;
        
        try {
            Init.init();
            RelationshipTransformService.registerDsigProvider();
            CryptoFunctions.registerBouncyCastle();
        } catch (Exception e) {
            throw new RuntimeException("Xml & BouncyCastle-Provider initialization failed", e);
        }
    }
    
    /**
     * Helper method for adding informations before the signing.
     * Normally {@link #confirmSignature()} is sufficient to be used.
     * @throws DigitalSignatureException 
     */
    @SuppressWarnings("unchecked")
	public DigestInfo preSign(Document document, List<DigestInfo> digestInfos)
			throws XMLSignatureException, MarshalException, DigitalSignatureException {
    	
        signatureConfig.init(false);
        
        // set the mdssi namespace via SignatureMarshalListener         
        EventTarget target = (EventTarget)document;
        EventListener creationListener = signatureConfig.getSignatureMarshalListener();
        if (creationListener != null) {
            if (creationListener instanceof SignatureMarshalListener) {
                ((SignatureMarshalListener)creationListener).setEventTarget(target);
            }
            SignatureMarshalListener.setListener(target, creationListener, true);
        }
        
        /*
         * Signature context construction.
         */
        XMLSignContext xmlSignContext = new DOMSignContext(signatureConfig.getCurrentSignatureDetail().getKey(), document);
        if (LOG.isDebugEnabled()) {
        	xmlSignContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        }
        URIDereferencer uriDereferencer = signatureConfig.getUriDereferencer();
        if (null != uriDereferencer) {
            xmlSignContext.setURIDereferencer(uriDereferencer);
        }

        for (Map.Entry<String,String> me : signatureConfig.getNamespacePrefixes().entrySet()) {
            xmlSignContext.putNamespacePrefix(me.getKey(), me.getValue());
        }
        xmlSignContext.setDefaultNamespacePrefix("");
        // signatureConfig.getNamespacePrefixes().get(XML_DIGSIG_NS));
        
        brokenJvmWorkaround(xmlSignContext);
        
        XMLSignatureFactory signatureFactory = signatureConfig.getSignatureFactory();
        LOG.debug("XMLSignatureFactory: " +  signatureFactory.getClass().getName() );

        /*
         * Add ds:References that come from signing client local files.
         * 
         *   JBH: nothing done here ie digestInfos list appears to be empty!
         * 
         */
        List<Reference> references = new ArrayList<Reference>();
        for (DigestInfo digestInfo : safe(digestInfos)) {
        	
//        	(new Throwable()).printStackTrace();
        	
        	
            byte[] documentDigestValue = digestInfo.digestValue;

            String uri = new File(digestInfo.description).getName();
            LOG.debug(uri + " - generating references");
            		
            		
            
            Reference reference = SignatureFacet.newReference
                (uri, null, 
                		null, null, documentDigestValue, signatureConfig);
            
            references.add(reference);
            
        }
        

        /*
         * Invoke the signature facets.
         * 
         * For example, OOXMLSignatureFacet addManifestReferences is where we identify the parts to be digested (signed).
         */
        List<XMLObject> objects = new ArrayList<XMLObject>();
        for (SignatureFacet signatureFacet : signatureConfig.getSignatureFacets()) {
            LOG.debug( "\n\n invoking signature facet: " + signatureFacet.getClass().getSimpleName() + "\n\n");
            signatureFacet.preSign(document, references, objects);
        }
        
        LOG.debug( "\n\n signature facets preSign all done \n\n");        
        
        /*
         * ds:SignedInfo
         */
        SignedInfo signedInfo;
        try {
            SignatureMethod signatureMethod = signatureFactory.newSignatureMethod
                (signatureConfig.getSignatureMethodUri(), null);
            
            
//            List prefixes = new ArrayList();
//            prefixes.add(ExcC14NParameterSpec.DEFAULT);
//            ExcC14NParameterSpec params = new ExcC14NParameterSpec(prefixes);            
            
            CanonicalizationMethod canonicalizationMethod = signatureFactory
                .newCanonicalizationMethod(
                		signatureConfig.getCanonicalizationMethod(),
                (C14NMethodParameterSpec) null);
//    		CanonicalizationMethod.EXCLUSIVE, // EXCLUSIVE removed unused namespaces which MOXy otherwise writes 
//                		params);
            
            
            signedInfo = signatureFactory.newSignedInfo(
                canonicalizationMethod, signatureMethod, references);
            
            // DOMSignedInfo doesn't pretty print, so be sure not to do that in XmlSignaturePart JAXB marshalling
            
        } catch (GeneralSecurityException e) {
            throw new XMLSignatureException(e);
        } 

        
        /*
         * JSR105 ds:Signature creation
         */
        LOG.debug( "\n\n Assembling signature document \n\n");        
        String signatureValueId = signatureConfig.getPackageSignatureId() + "-signature-value";
        javax.xml.crypto.dsig.XMLSignature xmlSignature = signatureFactory
            .newXMLSignature(signedInfo, null, objects, signatureConfig.getPackageSignatureId(),
            signatureValueId);
        
        
        /* At this point
         * 
         *  DOMSignContext context = (DOMSignContext)xmlSignContext;  
         *  Node contextParent = context.getParent();
         *  
         * contextParent is null, so we can't manipulate the XML to be signed that way. 
         * 
         * We could possibly do it via:
         * 
		        for (XMLObject obj : objects) {
		        	System.out.println(obj.getClass().getName());
		        	
		        	org.apache.jcp.xml.dsig.internal.dom.DOMXMLObject domObj = (org.apache.jcp.xml.dsig.internal.dom.DOMXMLObject)obj;
		
		//            List<XMLStructure> content = DOMXMLObject.getXmlObjectContent(obj);
		//            for (XMLStructure xs : content) {
		//
		//            }
		        }
         *
         *  but where are the regular DOM objects?
         *  
         *  It turns out the way to do it is via SignatureMarshalListener.
         */


        /*
         * ds:Signature Marshalling -  lot happens here!
         */
        LOG.debug( "\n\n Signing - with " + xmlSignature.getClass().getName() + "\n\n");         
        xmlSignature.sign(xmlSignContext);
        
        

        /*
         * Calculate digest for Manifest/Reference
         */
        LOG.debug( "\n\n Calculate digest for Manifest/Reference \n\n");        
        for (XMLObject object : objects) {
        	
            //LOG.debug("object java type: " + object.getClass().getName());
            List<XMLStructure> objectContentList = object.getContent();
            
            for (XMLStructure objectContent : objectContentList) {
            	
                //LOG.debug("object content java type: " + objectContent.getClass().getName());
                if (!(objectContent instanceof Manifest)) continue;
                
                Manifest manifest = (Manifest) objectContent;
                List<Reference> manifestReferences = manifest.getReferences();
                for (Reference manifestReference : manifestReferences) {
                	
                    if (manifestReference.getDigestValue() != null) continue;

                    LOG.debug("digesting " + manifestReference.getURI()); 
                    DOMReference manifestDOMReference = (DOMReference)manifestReference;
                    manifestDOMReference.digest(xmlSignContext);
                }
            }
        }

        /*
         * SignedInfo: Completion of undigested References
         */
        LOG.debug( "\n\n SignedInfo: Completion of undigested References \n\n");        
        List<Reference> signedInfoReferences = signedInfo.getReferences();
        for (Reference signedInfoReference : signedInfoReferences) {
            DOMReference domReference = (DOMReference)signedInfoReference;

            if (LOG.isDebugEnabled()) {
	            Data data = domReference.getDereferencedData();
	            if (data instanceof org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData) {
		            org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData std = (org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData)data;
		            LOG.debug(XmlUtils.w3CDomNodeToString(std.getRoot()));	            	
	            } else {
	            	LOG.debug(data.getClass().getName());
	            }
            }
            
            // ds:Reference with external digest value
            if (domReference.getDigestValue() != null) continue;
            
            LOG.debug("digesting " + domReference.getURI()); 
            domReference.digest(xmlSignContext);
        }

        /*
         * Calculation of XML signature digest value.
         */
        LOG.debug( "\n\n Calculation of XML signature digest value. \n\n");        
        DOMSignedInfo domSignedInfo = (DOMSignedInfo)signedInfo;
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        domSignedInfo.canonicalize(xmlSignContext, dataStream);
        byte[] octets = dataStream.toByteArray();

        /*
         * TODO: we could be using DigestOutputStream here to optimize memory
         * usage.
         */

        MessageDigest md = CryptoFunctions.getMessageDigest(signatureConfig.getDigestAlgo());
        byte[] digestValue = md.digest(octets);
        
        
        String description = signatureConfig.getSignatureDescription();
        return new DigestInfo(digestValue, signatureConfig.getDigestAlgo(), description);
    }

    /**
     * Helper method for adding informations after the signing.
     * Normally {@link #confirmSignature()} is sufficient to be used.
     * @throws DigitalSignatureException 
     */
    public void postSign(Document document, byte[] signatureValue)
    throws MarshalException, DigitalSignatureException {
        LOG.debug( "postSign");

        /*
         * Check ds:Signature node.
         */
        String signatureId = signatureConfig.getPackageSignatureId();
        if (!signatureId.equals(document.getDocumentElement().getAttribute("Id"))) {
            throw new RuntimeException("ds:Signature not found for @Id: " + signatureId);
        }

        /*
         * Insert signature value into the ds:SignatureValue element
         */
        NodeList sigValNl = document.getElementsByTagNameNS(XML_DIGSIG_NS, "SignatureValue");
        if (sigValNl.getLength() != 1) {
            throw new RuntimeException("preSign has to be called before postSign");
        }
        sigValNl.item(0).setTextContent(Base64.encode(signatureValue));

        /*
         * Allow signature facets to inject their own stuff.
         */
        for (SignatureFacet signatureFacet : signatureConfig.getSignatureFacets()) {
            signatureFacet.postSign(document);
        }

        writeDocument(document);
    }

    /**
     * Write XML signature into the OPC package.  This method will re-use existing
     * origins part if there is one.  It won't update any existing signature part.
     *
     * @param document the xml signature document
     * @throws MarshalException
     */
    protected void writeDocument(Document document) throws MarshalException {

        LOG.debug( "output signed Office OpenXML document");

        OpcPackage pkg = signatureConfig.getOpcPackage();

        try {
            
	        XmlSignaturePart sigPart = new XmlSignaturePart();
	        
	        /* It is tricky to use JAXB here, since it is liable to move the namespace declarations around,
	         * so they are different from what is signed.
	         * 
	         * I did have JAXB in Java 8 working, but something seems to have changed.
	         * 
	         * And getting MOXy to write the same thing is proving hard.
	         * 
	         * So using JAXB here is brittle. 
	         * 
	         * 
	         *      sigPart.setSignedDocument(document);		
	         *      
	         * which guarantees we save what we signed.
	         */

	        /*
			Unmarshaller u = DSigJAXBContext.jcXmlDSig.createUnmarshaller();	
			//u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			sigPart.setContents((JAXBElement<SignatureType>)u.unmarshal(document));
			*/

	        sigPart.setDocument(document);
			
			Relationship originRel = pkg.getRelationshipsPart().getRelationshipByType(Namespaces.DIGITAL_SIGNATURE_ORIGIN);
			SignatureOriginPart originSigsPart = null; 
	        if (originRel == null) {
	            // touch empty marker file
	        	originSigsPart = new SignatureOriginPart();
		        pkg.addTargetPart(originSigsPart);
	        } else {
	        	originSigsPart = (SignatureOriginPart)pkg.getRelationshipsPart().getPart(originRel);
	        }
	        
	        originSigsPart.addTargetPart(sigPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS );
	        
		} catch (Exception e) {
	        throw new MarshalException(e);
		}
    }
    
    /**
     * Helper method for null lists, which are converted to empty lists
     *
     * @param other the reference to wrap, if null
     * @return if other is null, an empty lists is returned, otherwise other is returned
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> safe(List<T> other) {
        return other == null ? Collections.EMPTY_LIST : other;
    }

    private void brokenJvmWorkaround(XMLSignContext context) {
        // workaround for https://bugzilla.redhat.com/show_bug.cgi?id=1155012
        Provider bcProv = Security.getProvider("BC");
        if (bcProv != null) {
            context.setProperty("org.jcp.xml.dsig.internal.dom.SignatureProvider", bcProv);
        }        
    }
    
}
