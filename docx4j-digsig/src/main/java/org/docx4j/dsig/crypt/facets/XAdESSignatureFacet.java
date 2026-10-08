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

package org.docx4j.dsig.crypt.facets;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.xml.security.binding.xmldsig.DigestMethodType;
import org.apache.xml.security.binding.xmldsig.X509IssuerSerialType;
import org.docx4j.XmlUtils;
//import org.docx4j.jaxb.Context;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.xml.security.Init;
import org.docx4j.org.apache.xml.security.c14n.Canonicalizer;
//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
//import org.apache.xmlbeans.XmlCursor;
//import org.apache.xmlbeans.XmlObject;
//import org.apache.xmlbeans.XmlString;
//import org.docx4j.org.etsi.uri.x01903.v13.QualifyingPropertiesDocument; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.w3.x2000.x09.xmldsig.DigestMethodType;
//import org.w3.x2000.x09.xmldsig.X509IssuerSerialType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.docx4j.dsig.crypt.DSigJAXBContext;
import org.docx4j.dsig.crypt.SignatureConfig;
import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.crypt.services.SignaturePolicyService;
import org.docx4j.dsig.DigitalSignatureException;
import org.docx4j.org.etsi.uri.x01903.v13.AnyType;
import org.docx4j.org.etsi.uri.x01903.v13.CertIDListType;
import org.docx4j.org.etsi.uri.x01903.v13.CertIDType;
import org.docx4j.org.etsi.uri.x01903.v13.ClaimedRolesListType;
import org.docx4j.org.etsi.uri.x01903.v13.CommitmentTypeIndicationType;
import org.docx4j.org.etsi.uri.x01903.v13.DataObjectFormatType;
import org.docx4j.org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.docx4j.org.etsi.uri.x01903.v13.IdentifierType;
import org.docx4j.org.etsi.uri.x01903.v13.ObjectFactory;
import org.docx4j.org.etsi.uri.x01903.v13.ObjectIdentifierType;
import org.docx4j.org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.SigPolicyQualifiersListType;
import org.docx4j.org.etsi.uri.x01903.v13.SignaturePolicyIdType;
import org.docx4j.org.etsi.uri.x01903.v13.SignaturePolicyIdentifierType;
import org.docx4j.org.etsi.uri.x01903.v13.SignedDataObjectPropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.SignedPropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.SignedSignaturePropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.SignerRoleType;

/**
 * XAdES Signature Facet. Implements XAdES v1.4.1 which is compatible with XAdES
 * v1.3.2. The implemented XAdES format is XAdES-BES/EPES. It's up to another
 * part of the signature service to upgrade the XAdES-BES to a XAdES-X-L.
 * 
 * This implementation has been tested against an implementation that
 * participated multiple ETSI XAdES plugtests.
 * 
 * @author Frank Cornelis
 * @see <a href="http://en.wikipedia.org/wiki/XAdES">XAdES</a>
 * 
 */
public class XAdESSignatureFacet extends SignatureFacet {

//    private static final POILogger LOG = POILogFactory.getLogger(XAdESSignatureFacet.class);
	private static Logger LOG = LoggerFactory.getLogger(XAdESSignatureFacet.class);	

    private static final String XADES_TYPE = "http://uri.etsi.org/01903#SignedProperties";
    
    private static final String XADES_PROOF_OF_ORIGIN = "http://uri.etsi.org/01903/v1.2.2#ProofOfOrigin";
    
    private Map<String, String> dataObjectFormatMimeTypes = new HashMap<String, String>();


    @Override
    public void preSign(
          Document document
        , List<Reference> references
        , List<XMLObject> objects)
    throws XMLSignatureException, DigitalSignatureException {
        LOG.debug( "preSign");
        
        SignatureDetail signatureDetail = signatureConfig.getCurrentSignatureDetail();

        // QualifyingProperties
        //QualifyingPropertiesDocument qualDoc = QualifyingPropertiesDocument.Factory.newInstance();
        //QualifyingPropertiesType qualifyingProperties = qualDoc.addNewQualifyingProperties();
        QualifyingPropertiesType qualifyingProperties = new QualifyingPropertiesType(); 
        qualifyingProperties.setTarget("#" + signatureConfig.getPackageSignatureId());
        
        // SignedProperties
        //SignedPropertiesType signedProperties = qualifyingProperties.addNewSignedProperties();
        SignedPropertiesType signedProperties = new SignedPropertiesType();
        signedProperties.setId(signatureConfig.getXadesSignatureId());
        
        qualifyingProperties.setSignedProperties(signedProperties);

        // SignedSignatureProperties
        SignedSignaturePropertiesType signedSignatureProperties = new SignedSignaturePropertiesType();
        signedProperties.setSignedSignatureProperties(signedSignatureProperties);
        
        // SigningTime
        
        // In UTC, like mdssi:SignatureTime (OOXMLSignatureFacet).  Until 2026 the local
        // wall-clock fields were written with a Z suffix, so the two disagreed by the UTC offset.
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(signatureConfig.getExecutionTime());
        calendar.set(Calendar.MILLISECOND, 0);
        
        javax.xml.datatype.XMLGregorianCalendar xmlGregorianCalendar = null;
		try {
			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new XMLSignatureException(e);
		}
        xmlGregorianCalendar.setFractionalSecond(null);
        
        signedSignatureProperties.setSigningTime(xmlGregorianCalendar);

        // SigningCertificate
        if (signatureDetail.getSigningCertificateChain() == null
            || signatureDetail.getSigningCertificateChain().isEmpty()) {
            throw new RuntimeException("no signing certificate chain available");
        }
        //CertIDListType signingCertificates = signedSignatureProperties.addNewSigningCertificate();
        CertIDListType signingCertificates = new CertIDListType();
        signedSignatureProperties.setSigningCertificate(signingCertificates);
        
        //CertIDType certId = signingCertificates.addNewCert();
        CertIDType certId = new CertIDType();
        signingCertificates.getCert().add(certId);
        
        X509Certificate certificate = signatureDetail.getSigningCertificateChain().get(0);
        setCertID(certId, signatureConfig, signatureConfig.isXadesIssuerNameNoReverseOrder(), certificate);
        
        /*
          <xd:SignatureProductionPlace>
            <xd:City></xd:City>
            <xd:StateOrProvince></xd:StateOrProvince>
            <xd:PostalCode></xd:PostalCode>
            <xd:CountryName></xd:CountryName>
          </xd:SignatureProductionPlace>
          
         */
        signedSignatureProperties.setSignatureProductionPlace(
        		signatureDetail.getSignatureProductionPlace());

        /* ClaimedRole
         * 
          <xd:SignerRole>
            <xd:ClaimedRoles>
              <xd:ClaimedRole> </xd:ClaimedRole>
            </xd:ClaimedRoles>
          </xd:SignerRole>
          
         */
        String role = signatureDetail.getXadesRole();
        if (role != null && !role.isEmpty()) {
            //SignerRoleType signerRole = signedSignatureProperties.addNewSignerRole();
        	SignerRoleType signerRole = new SignerRoleType();
            signedSignatureProperties.setSignerRole(signerRole);
            
            //ClaimedRolesListType claimedRolesList = signerRole.addNewClaimedRoles();
            ClaimedRolesListType claimedRolesList = new ClaimedRolesListType();
            signerRole.setClaimedRoles(claimedRolesList);
            
            claimedRolesList.getClaimedRole().add(role);
        }

        // XAdES-EPES
        SignaturePolicyService policyService = signatureConfig.getSignaturePolicyService();
        if (policyService != null) {
            //SignaturePolicyIdentifierType signaturePolicyIdentifier = signedSignatureProperties.addNewSignaturePolicyIdentifier();
        	SignaturePolicyIdentifierType signaturePolicyIdentifier = new SignaturePolicyIdentifierType();
        	signedSignatureProperties.setSignaturePolicyIdentifier(signaturePolicyIdentifier);
            
            //SignaturePolicyIdType signaturePolicyId = signaturePolicyIdentifier.addNewSignaturePolicyId();
        	SignaturePolicyIdType signaturePolicyId = new SignaturePolicyIdType();
        	signaturePolicyIdentifier.setSignaturePolicyId(signaturePolicyId);

            //ObjectIdentifierType objectIdentifier = signaturePolicyId.addNewSigPolicyId();
        	ObjectIdentifierType objectIdentifier = new ObjectIdentifierType();
        	signaturePolicyId.setSigPolicyId(objectIdentifier);
            objectIdentifier.setDescription(policyService.getSignaturePolicyDescription());
            
            //IdentifierType identifier = objectIdentifier.addNewIdentifier();
            IdentifierType identifier = new IdentifierType();
            objectIdentifier.setIdentifier(identifier);
            identifier.setValue(policyService.getSignaturePolicyIdentifier());

            byte[] signaturePolicyDocumentData = policyService.getSignaturePolicyDocument();
            //DigestAlgAndValueType sigPolicyHash = signaturePolicyId.addNewSigPolicyHash();
            DigestAlgAndValueType sigPolicyHash = new DigestAlgAndValueType();
            signaturePolicyId.setSigPolicyHash(sigPolicyHash);
            
            setDigestAlgAndValue(sigPolicyHash, signaturePolicyDocumentData, signatureConfig.getDigestAlgo());

            String signaturePolicyDownloadUrl = policyService.getSignaturePolicyDownloadUrl();
            if (null != signaturePolicyDownloadUrl) {
                //SigPolicyQualifiersListType sigPolicyQualifiers = signaturePolicyId.addNewSigPolicyQualifiers();
            	SigPolicyQualifiersListType sigPolicyQualifiers = new SigPolicyQualifiersListType();
            	signaturePolicyId.setSigPolicyQualifiers(sigPolicyQualifiers);
            	
                //AnyType sigPolicyQualifier = sigPolicyQualifiers.addNewSigPolicyQualifier();
            	AnyType sigPolicyQualifier = new AnyType();
            	sigPolicyQualifiers.getSigPolicyQualifier().add(sigPolicyQualifier);
            	
//                XmlString spUriElement = XmlString.Factory.newInstance();
//                spUriElement.setStringValue(signaturePolicyDownloadUrl);
//                insertXChild(sigPolicyQualifier, spUriElement);
            	
            	// ???
            	sigPolicyQualifier.getContent().add(signaturePolicyDownloadUrl);
            }
        } else if (signatureConfig.isXadesSignaturePolicyImplied()) {
        	
        	SignaturePolicyIdentifierType signaturePolicyIdentifier = new SignaturePolicyIdentifierType();
        	signaturePolicyIdentifier.setSignaturePolicyImplied(""); //empty element

        	signedSignatureProperties.setSignaturePolicyIdentifier(signaturePolicyIdentifier);

        }

        // <xd:SignedDataObjectProperties>
        if (dataObjectFormatMimeTypes.isEmpty()
        		&& signatureDetail.getCommitmentType()==null) {
        	
        	// No need to add 
        	
        } else {
        	SignedDataObjectPropertiesType signedDataObjectProperties = new SignedDataObjectPropertiesType();
        	signedProperties.setSignedDataObjectProperties(signedDataObjectProperties);
        	
        	if (signatureDetail.getCommitmentType()!=null) {
        		
        		/*
			          <xd:CommitmentTypeIndication>
			            <xd:CommitmentTypeId>
			              <xd:Identifier>http://uri.etsi.org/01903/v1.2.2#ProofOfOrigin</xd:Identifier>
			              <xd:Description>Created and approved this document</xd:Description>
			            </xd:CommitmentTypeId>
			            <xd:AllSignedDataObjects/>
			          </xd:CommitmentTypeIndication>
        		 */
        		
        		ObjectFactory etsi13Factory = new ObjectFactory();
        		
        		CommitmentTypeIndicationType commitmentTypeIndicationType = etsi13Factory.createCommitmentTypeIndicationType();
        		
//        		JAXBElement<CommitmentTypeIndicationType> commitmentTypeIndication 
//        			= etsi13Factory.createCommitmentTypeIndication(commitmentTypeIndicationType);
        		
        		
        		ObjectIdentifierType objectIdentifierType = etsi13Factory.createObjectIdentifierType();
        		
        		IdentifierType identifierType = new IdentifierType();
        		identifierType.setValue(XADES_PROOF_OF_ORIGIN);
        		objectIdentifierType.setIdentifier(identifierType);
        		
        		objectIdentifierType.setDescription(signatureDetail.getCommitmentType().getValue());
        		
        		commitmentTypeIndicationType.setCommitmentTypeId(objectIdentifierType);
        		signedDataObjectProperties.getCommitmentTypeIndication().add(commitmentTypeIndicationType);

        		// <xd:AllSignedDataObjects/>
        		commitmentTypeIndicationType.setAllSignedDataObjects(new String());
        		
        	}
        	
            // DataObjectFormat
        	if (!dataObjectFormatMimeTypes.isEmpty()) {
        		
	            List<DataObjectFormatType> dataObjectFormats = signedDataObjectProperties.getDataObjectFormat();
	            
	            for (Map.Entry<String, String> dataObjectFormatMimeType : this.dataObjectFormatMimeTypes.entrySet()) {
	            	
	                DataObjectFormatType dataObjectFormat = new DataObjectFormatType(); //DataObjectFormatType.Factory.newInstance();
	                dataObjectFormat.setObjectReference("#" + dataObjectFormatMimeType.getKey());
	                dataObjectFormat.setMimeType(dataObjectFormatMimeType.getValue());
	                dataObjectFormats.add(dataObjectFormat);
	            }
        	}
        }

        
        // add XAdES ds:Object
        
        //Document qualifyingPropertiesTmpDoc = XmlUtils.marshaltoW3CDomDocument(qualifyingProperties, Context.jcXmlDSig); 
        //System.out.println(XmlUtils.w3CDomNodeToString(qualifyingPropertiesTmpDoc));        
        //Element qualDocElSrc = qualifyingPropertiesTmpDoc.getDocumentElement();
        
        // Get rid of the superfluous namespaces - done again in Office2010SignatureFacet!
        Element qualDocElSrc = null;
        try {
			Document qualifyingPropertiesTmpDoc = XmlUtils.marshaltoW3CDomDocument(qualifyingProperties, DSigJAXBContext.jcXmlDSig);
			LOG.debug("Input to Canonicalizer: " + XmlUtils.w3CDomNodeToString(qualifyingPropertiesTmpDoc));
			
			Init.init();
			Canonicalizer c = Canonicalizer.getInstance(CanonicalizationMethod.EXCLUSIVE);
			byte[] bytes = c.canonicalizeSubtree(qualifyingPropertiesTmpDoc);
			Document myDoc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(bytes));
//			System.out.println("\n\n!! " + XmlUtils.w3CDomNodeToString(myDoc));
			qualDocElSrc = myDoc.getDocumentElement();
			
			
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        Element qualDocEl = (Element)document.importNode(qualDocElSrc, true);
		Element signedPropertiesEl =  (Element)qualDocEl.getChildNodes().item(0);
		signedPropertiesEl.setIdAttribute("Id", true);  // 2022 
        
        /*
		  <Object>
		    <xd:QualifyingProperties xmlns:xd="http://uri.etsi.org/01903/v1.3.2#" 
		                             Target="#idPackageSignature">
		      <xd:SignedProperties Id="idSignedProperties">
		        <xd:SignedSignatureProperties>
                 */
        
        
        List<XMLStructure> xadesObjectContent = new ArrayList<XMLStructure>();
        xadesObjectContent.add(new DOMStructure(qualDocEl));
        XMLObject xadesObject = getSignatureFactory().newXMLObject(xadesObjectContent, null, null, null);
        objects.add(xadesObject);

        // add XAdES ds:Reference
        List<Transform> transforms = new ArrayList<Transform>();
        
        Transform exclusiveTransform = newTransform(CanonicalizationMethod.INCLUSIVE); // POI
        //Transform exclusiveTransform = newTransform(CanonicalizationMethod.EXCLUSIVE);
        transforms.add(exclusiveTransform);
        
        Reference reference = newReference
            ("#"+signatureConfig.getXadesSignatureId(), transforms, XADES_TYPE, null, null);
        references.add(reference);
    }

    /**
     * Gives back the JAXB DigestAlgAndValue data structure.
     *
     * @param digestAlgAndValue the parent for the new digest element 
     * @param data the data to be digested
     * @param digestAlgo the digest algorithm
     * @throws DigitalSignatureException 
     */
    protected static void setDigestAlgAndValue(
            DigestAlgAndValueType digestAlgAndValue,
            byte[] data,
            HashAlgorithm digestAlgo) throws DigitalSignatureException {
        //DigestMethodType digestMethod = digestAlgAndValue.addNewDigestMethod();
    	DigestMethodType digestMethod = new DigestMethodType();
    	digestAlgAndValue.setDigestMethod(digestMethod);
    	
        digestMethod.setAlgorithm(SignatureConfig.getDigestMethodUri(digestAlgo));
        
        MessageDigest messageDigest = CryptoFunctions.getMessageDigest(digestAlgo);
        byte[] digestValue = messageDigest.digest(data);
        digestAlgAndValue.setDigestValue(digestValue);
    }

    /**
     * Gives back the JAXB CertID data structure.
     * @throws DigitalSignatureException 
     */
    protected static void setCertID
        (CertIDType certId, SignatureConfig signatureConfig, boolean issuerNameNoReverseOrder, X509Certificate certificate) throws DigitalSignatureException {
        //X509IssuerSerialType issuerSerial = certId.addNewIssuerSerial();
    	X509IssuerSerialType issuerSerial = new X509IssuerSerialType();
    	certId.setIssuerSerial(issuerSerial);
    	
        String issuerName;
        if (issuerNameNoReverseOrder) {
            /*
             * Make sure the DN is encoded using the same order as present
             * within the certificate. This is an Office2010 work-around.
             * Should be reverted back.
             * 
             * XXX: not correct according to RFC 4514.
             */
            // TODO: check if issuerName is different on getTBSCertificate
            // issuerName = PrincipalUtil.getIssuerX509Principal(certificate).getName().replace(",", ", ");
            issuerName = certificate.getIssuerDN().getName().replace(",", ", ");
        } else {
            issuerName = certificate.getIssuerX500Principal().toString();
        }
        issuerSerial.setX509IssuerName(issuerName);
        issuerSerial.setX509SerialNumber(certificate.getSerialNumber());

        byte[] encodedCertificate;
        try {
            encodedCertificate = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("certificate encoding error: "
                    + e.getMessage(), e);
        }
        //DigestAlgAndValueType certDigest = certId.addNewCertDigest(); 
        DigestAlgAndValueType certDigest = new DigestAlgAndValueType(); 
        certId.setCertDigest(certDigest);
        
        setDigestAlgAndValue(certDigest, encodedCertificate, signatureConfig.getXadesDigestAlgo());
    }

    /**
     * Adds a mime-type for the given ds:Reference (referred via its @URI). This
     * information is added via the xades:DataObjectFormat element.
     * 
     * @param dsReferenceUri
     * @param mimetype
     */
    public void addMimeType(String dsReferenceUri, String mimetype) {
        this.dataObjectFormatMimeTypes.put(dsReferenceUri, mimetype);
    }

//    protected static void insertXChild(XmlObject root, XmlObject child) {
//        XmlCursor rootCursor = root.newCursor();
//        rootCursor.toEndToken();
//        XmlCursor childCursor = child.newCursor();
//        childCursor.toNextToken();
//        childCursor.moveXml(rootCursor);
//        childCursor.dispose();
//        rootCursor.dispose();
//    }

}