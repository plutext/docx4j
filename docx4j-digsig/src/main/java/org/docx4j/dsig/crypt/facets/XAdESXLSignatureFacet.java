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

//import static org.docx4j.dsig.crypt.facets.XAdESSignatureFacet.insertXChild;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.xml.security.binding.xmldsig.CanonicalizationMethodType;
import org.apache.xml.security.c14n.Canonicalizer;
//import org.apache.xmlbeans.XmlException;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RespID;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
//import org.docx4j.org.etsi.uri.x01903.v13.QualifyingPropertiesDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.w3.x2000.x09.xmldsig.CanonicalizationMethodType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.docx4j.dsig.crypt.DSigJAXBContext;
import org.docx4j.dsig.crypt.services.RevocationData;
import org.docx4j.dsig.DigitalSignatureException;
import org.docx4j.org.etsi.uri.x01903.v13.CRLIdentifierType;
import org.docx4j.org.etsi.uri.x01903.v13.CRLRefType;
import org.docx4j.org.etsi.uri.x01903.v13.CRLRefsType;
import org.docx4j.org.etsi.uri.x01903.v13.CRLValuesType;
import org.docx4j.org.etsi.uri.x01903.v13.CertIDListType;
import org.docx4j.org.etsi.uri.x01903.v13.CertIDType;
import org.docx4j.org.etsi.uri.x01903.v13.CertificateValuesType;
import org.docx4j.org.etsi.uri.x01903.v13.CompleteCertificateRefsType;
import org.docx4j.org.etsi.uri.x01903.v13.CompleteRevocationRefsType;
import org.docx4j.org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.docx4j.org.etsi.uri.x01903.v13.EncapsulatedPKIDataType;
import org.docx4j.org.etsi.uri.x01903.v13.OCSPIdentifierType;
import org.docx4j.org.etsi.uri.x01903.v13.OCSPRefType;
import org.docx4j.org.etsi.uri.x01903.v13.OCSPRefsType;
import org.docx4j.org.etsi.uri.x01903.v13.OCSPValuesType;
import org.docx4j.org.etsi.uri.x01903.v13.ObjectFactory;
import org.docx4j.org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.ResponderIDType;
import org.docx4j.org.etsi.uri.x01903.v13.RevocationValuesType;
import org.docx4j.org.etsi.uri.x01903.v13.UnsignedPropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.UnsignedSignaturePropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.XAdESTimeStampType;
import org.docx4j.org.etsi.uri.x01903.v141.ValidationDataType;
//import org.docx4j.jaxb.Context;

/**
 * XAdES-X-L v1.4.1 signature facet. This signature facet implementation will
 * upgrade a given XAdES-BES/EPES signature to XAdES-X-L.
 * 
 * (Not just -X-L; also -T, -C, -X.)
 * 
 * We don't inherit from XAdESSignatureFacet as we also want to be able to use
 * this facet out of the context of a signature creation. This signature facet
 * assumes that the signature is already XAdES-BES/EPES compliant.
 * 
 * This implementation has been tested against an implementation that
 * participated multiple ETSI XAdES plugtests.
 * 
 * @author Frank Cornelis
 * @see XAdESSignatureFacet
 */
public class XAdESXLSignatureFacet extends SignatureFacet {

//    private static final POILogger LOG = POILogFactory.getLogger(XAdESXLSignatureFacet.class);
	private static Logger LOG = LoggerFactory.getLogger(XAdESXLSignatureFacet.class);	
	
	private static ObjectFactory etsi13Factory = new ObjectFactory();
	
	

    private final CertificateFactory certificateFactory;

    public XAdESXLSignatureFacet() {
        try {
            this.certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("X509 JCA error: " + e.getMessage(), e);
        }
    }

    @Override
    public void postSign(Document document) throws MarshalException, DigitalSignatureException {
        LOG.debug( "XAdES-X-L post sign phase");
        
        

        //QualifyingPropertiesDocument qualDoc = null;
        QualifyingPropertiesType qualProps = null;

        // check for XAdES-BES
        NodeList qualNl = document.getElementsByTagNameNS(XADES_132_NS, "QualifyingProperties");
        if (qualNl.getLength() == 1) {
            try {
                //qualDoc = QualifyingPropertiesDocument.Factory.parse(qualNl.item(0));
        		Unmarshaller u = DSigJAXBContext.jcXmlDSig.createUnmarshaller();						
        		//u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());            	
            	qualProps = (QualifyingPropertiesType)XmlUtils.unwrap(u.unmarshal(qualNl.item(0)));
            } catch (JAXBException e) {
                throw new MarshalException(e);
            }
            //qualProps = qualDoc.getQualifyingProperties();
        } else {
            throw new MarshalException("no XAdES-BES extension present");
        }

        // create basic XML container structure
        
        /*
         * 
			<xsd:complexType name="UnsignedSignaturePropertiesType">
				<xsd:choice maxOccurs="unbounded">
					<xsd:element name="CounterSignature" type="CounterSignatureType"/>
					<xsd:element name="SignatureTimeStamp" > <!-- type="XAdESTimeStampType" -->
					<xsd:element name="CompleteCertificateRefs" type="CompleteCertificateRefsType"/>
					<xsd:element name="CompleteRevocationRefs" type="CompleteRevocationRefsType"/>
					<xsd:element name="AttributeCertificateRefs" type="CompleteCertificateRefsType"/>
					<xsd:element name="AttributeRevocationRefs" type="CompleteRevocationRefsType"/>
					<xsd:element name="SigAndRefsTimeStamp" > <!-- type="XAdESTimeStampType" -->
					<xsd:element name="RefsOnlyTimeStamp" > <!-- type="XAdESTimeStampType" -->
					<xsd:element name="CertificateValues" type="CertificateValuesType"/>
					<xsd:element name="RevocationValues" type="RevocationValuesType"/>
					<xsd:element name="AttrAuthoritiesCertValues" type="CertificateValuesType"/>
					<xsd:element name="AttributeRevocationValues" type="RevocationValuesType"/>
					<xsd:element name="ArchiveTimeStamp" > <!-- type="XAdESTimeStampType" -->
					<xsd:any namespace="##other"/>
				</xsd:choice>
				<xsd:attribute name="Id" type="xsd:ID" use="optional"/>
			</xsd:complexType>
			<!-- End UnsignedSignatureProperties-->         
			
			* We have support here for writing something like:
			* 
			    <xd:UnsignedProperties>
			        <xd:UnsignedSignatureProperties>
			            <xd:SignatureTimeStamp Id="time-stamp-03e70e78-67bc-4027-b670-7453a55ad216">
			                <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
			                <xd:EncapsulatedTimeStamp Id="time-stamp-token-4115be8e-b578-46d2-bbbb-c2b46050d233">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</xd:EncapsulatedTimeStamp>
			            </xd:SignatureTimeStamp>
			            <xd:CompleteCertificateRefs>
			                <xd:CertRefs/>
			            </xd:CompleteCertificateRefs>
			            <xd:CompleteRevocationRefs/>
			            <xd:SigAndRefsTimeStamp Id="time-stamp-7a2aed17-6228-4421-9851-8785e6867736">
			                <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
			                <xd:EncapsulatedTimeStamp Id="time-stamp-token-13059a1c-1377-4b29-a6db-5c5750b454eb">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</xd:EncapsulatedTimeStamp>
			            </xd:SigAndRefsTimeStamp>
			            <xd:CertificateValues>
			                <xd:EncapsulatedX509Certificate>MIIFGDCCBACgAwIBAgIQf9Hy8UJoFT4iDZtsk+3cSjANBgkqhkiG9w0BAQUFADB7MQswCQYDVQQGEwJHQjEbMBkGA1UECBMSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHEwdTYWxmb3JkMRowGAYDVQQKExFDT01PRE8gQ0EgTGltaXRlZDEhMB8GA1UEAxMYQ09NT0RPIENvZGUgU2lnbmluZyBDQSAyMB4XDTE0MDgyMDAwMDAwMFoXDTE3MDgxOTIzNTk1OVowgYcxCzAJBgNVBAYTAkFVMQ0wCwYDVQQRDAQyNjgwMQwwCgYDVQQIDANOU1cxETAPBgNVBAcMCEdyaWZmaXRoMRQwEgYDVQQJDAtQTyBCb3ggMTk5NDEYMBYGA1UECgwPUGx1dGV4dCBQdHkgTHRkMRgwFgYDVQQDDA9QbHV0ZXh0IFB0eSBMdGQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCn4dI8RsnMJQcWyxPrh8TWGSvfKUajzitqXlvlFjB8OLAjUCdO+gq7FG2fit5s4wKn7KuA6qoLYwikBGcP0QvpwrfUu33i/p4VoVC2p+7uKvwNm/2Pkk8LmCz1M667WbWaW7ZtdFO7a0sspF/b8zFrsSlKjpJu+FYQMVA4Vfqn+eTn7eXsvu3k4MDKnRZm9L1T3jlmO2I671Ft4XcbXFqjPeUbAgGLXJQyO7zTPPRQUfNDTawJg/TT3cr0qvA7YcANqLIuj8iR9Ul6uL6JWsHZ+9V0SetY/xShQslHoL8JssiBDA8/8e/KxLbuOsko+2FapCD/W9f3I1NCr8OOB5z7AgMBAAGjggGJMIIBhTAfBgNVHSMEGDAWgBQexbEsfYfaAmh8JbwMB4Q/ts/e8TAdBgNVHQ4EFgQUIOmhSv5Q35tKUp6haWhjVn/o/uswDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwEwYDVR0lBAwwCgYIKwYBBQUHAwMwEQYJYIZIAYb4QgEBBAQDAgQQMEYGA1UdIAQ/MD0wOwYMKwYBBAGyMQECAQMCMCswKQYIKwYBBQUHAgEWHWh0dHBzOi8vc2VjdXJlLmNvbW9kby5uZXQvQ1BTMEEGA1UdHwQ6MDgwNqA0oDKGMGh0dHA6Ly9jcmwuY29tb2RvY2EuY29tL0NPTU9ET0NvZGVTaWduaW5nQ0EyLmNybDByBggrBgEFBQcBAQRmMGQwPAYIKwYBBQUHMAKGMGh0dHA6Ly9jcnQuY29tb2RvY2EuY29tL0NPTU9ET0NvZGVTaWduaW5nQ0EyLmNydDAkBggrBgEFBQcwAYYYaHR0cDovL29jc3AuY29tb2RvY2EuY29tMA0GCSqGSIb3DQEBBQUAA4IBAQCvVOTz5X1UkFN5va7zzFlO5QcNE0pFkqHd6FwLbIxP332SYGC+JhxM8ky5Upfig4JSmcLxalXwk98OMFJT+vtLWDWqPcM+aSn7AdTfJs5Hytu/UvuUd4J7EKqUZ4633DuVTEDkaFKNypEwFng5pV2f/cEONE/hGXqx8TbImx/FPpbl+Tt6X2U5MSAAgXih8m4Ff3OP3zMDrrQvsAScvZ6Gnp6WWEOZtr/tthin/05n+C2IKxBWeVslHW2pYX+vXBqt9lwPB+id/mfALAko3ltSTG+zSz7wys2aLMpR8ROGuBhFeRmgp6Dq6/xZTL3Z5nrx1yDnr+yPB3FAEeq87tkZ</xd:EncapsulatedX509Certificate>
			            </xd:CertificateValues>
			            <xd:RevocationValues/>
			        </xd:UnsignedSignatureProperties>
			    </xd:UnsignedProperties>
    
    			*
			* but note that Word 2016 only writes:
			* 
			    <xd:UnsignedProperties>
			        <xd:UnsignedSignatureProperties>
			            <xd:CertificateValues>
			                <xd:EncapsulatedX509Certificate>MIIFGDCCBACgAwIBAgIQf9Hy8UJoFT4iDZtsk+3cSjANBgkqhkiG9w0BAQUFADB7MQswCQYDVQQGEwJHQjEbMBkGA1UECBMSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHEwdTYWxmb3JkMRowGAYDVQQKExFDT01PRE8gQ0EgTGltaXRlZDEhMB8GA1UEAxMYQ09NT0RPIENvZGUgU2lnbmluZyBDQSAyMB4XDTE0MDgyMDAwMDAwMFoXDTE3MDgxOTIzNTk1OVowgYcxCzAJBgNVBAYTAkFVMQ0wCwYDVQQRDAQyNjgwMQwwCgYDVQQIDANOU1cxETAPBgNVBAcMCEdyaWZmaXRoMRQwEgYDVQQJDAtQTyBCb3ggMTk5NDEYMBYGA1UECgwPUGx1dGV4dCBQdHkgTHRkMRgwFgYDVQQDDA9QbHV0ZXh0IFB0eSBMdGQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCn4dI8RsnMJQcWyxPrh8TWGSvfKUajzitqXlvlFjB8OLAjUCdO+gq7FG2fit5s4wKn7KuA6qoLYwikBGcP0QvpwrfUu33i/p4VoVC2p+7uKvwNm/2Pkk8LmCz1M667WbWaW7ZtdFO7a0sspF/b8zFrsSlKjpJu+FYQMVA4Vfqn+eTn7eXsvu3k4MDKnRZm9L1T3jlmO2I671Ft4XcbXFqjPeUbAgGLXJQyO7zTPPRQUfNDTawJg/TT3cr0qvA7YcANqLIuj8iR9Ul6uL6JWsHZ+9V0SetY/xShQslHoL8JssiBDA8/8e/KxLbuOsko+2FapCD/W9f3I1NCr8OOB5z7AgMBAAGjggGJMIIBhTAfBgNVHSMEGDAWgBQexbEsfYfaAmh8JbwMB4Q/ts/e8TAdBgNVHQ4EFgQUIOmhSv5Q35tKUp6haWhjVn/o/uswDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwEwYDVR0lBAwwCgYIKwYBBQUHAwMwEQYJYIZIAYb4QgEBBAQDAgQQMEYGA1UdIAQ/MD0wOwYMKwYBBAGyMQECAQMCMCswKQYIKwYBBQUHAgEWHWh0dHBzOi8vc2VjdXJlLmNvbW9kby5uZXQvQ1BTMEEGA1UdHwQ6MDgwNqA0oDKGMGh0dHA6Ly9jcmwuY29tb2RvY2EuY29tL0NPTU9ET0NvZGVTaWduaW5nQ0EyLmNybDByBggrBgEFBQcBAQRmMGQwPAYIKwYBBQUHMAKGMGh0dHA6Ly9jcnQuY29tb2RvY2EuY29tL0NPTU9ET0NvZGVTaWduaW5nQ0EyLmNydDAkBggrBgEFBQcwAYYYaHR0cDovL29jc3AuY29tb2RvY2EuY29tMA0GCSqGSIb3DQEBBQUAA4IBAQCvVOTz5X1UkFN5va7zzFlO5QcNE0pFkqHd6FwLbIxP332SYGC+JhxM8ky5Upfig4JSmcLxalXwk98OMFJT+vtLWDWqPcM+aSn7AdTfJs5Hytu/UvuUd4J7EKqUZ4633DuVTEDkaFKNypEwFng5pV2f/cEONE/hGXqx8TbImx/FPpbl+Tt6X2U5MSAAgXih8m4Ff3OP3zMDrrQvsAScvZ6Gnp6WWEOZtr/tthin/05n+C2IKxBWeVslHW2pYX+vXBqt9lwPB+id/mfALAko3ltSTG+zSz7wys2aLMpR8ROGuBhFeRmgp6Dq6/xZTL3Z5nrx1yDnr+yPB3FAEeq87tkZ</xd:EncapsulatedX509Certificate>
			            </xd:CertificateValues>
			            <xd:RevocationValues/>
			        </xd:UnsignedSignatureProperties>
			    </xd:UnsignedProperties>
			* 
			*/
        UnsignedPropertiesType unsignedProps = qualProps.getUnsignedProperties();
        if (unsignedProps == null) {
            // unsignedProps = qualProps.addNewUnsignedProperties();
        	unsignedProps = new UnsignedPropertiesType();
        	qualProps.setUnsignedProperties(unsignedProps);
        }
        UnsignedSignaturePropertiesType unsignedSigProps = unsignedProps.getUnsignedSignatureProperties();
        if (unsignedSigProps == null) {
            //unsignedSigProps = unsignedProps.addNewUnsignedSignatureProperties();
        	unsignedSigProps =  new UnsignedSignaturePropertiesType();
        	unsignedProps.setUnsignedSignatureProperties(unsignedSigProps);
        }
        

        // Time-Stamp (XAdES-T): Includes time-stamp to provide protection against repudiation
        // <SignatureTimeStamp>
        NodeList nlSigVal = document.getElementsByTagNameNS(XML_DIGSIG_NS, "SignatureValue");
        if (nlSigVal.getLength() != 1) {
            throw new IllegalArgumentException("SignatureValue is not set.");
        }
        
        RevocationData tsaRevocationDataXadesT = new RevocationData();
        LOG.debug( "creating XAdES-T time-stamp");
		XAdESTimeStampType xAdESTimeStampType = createXAdESTimeStamp
		    (Collections.singletonList(nlSigVal.item(0)), tsaRevocationDataXadesT);
		JAXBElement<XAdESTimeStampType> signatureTimeStamp = etsi13Factory.createSignatureTimeStamp(xAdESTimeStampType);
        unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs()
        	.add(signatureTimeStamp);        

        // xadesv141::TimeStampValidationData
        if (tsaRevocationDataXadesT.hasRevocationDataEntries()) {
            ValidationDataType validationData = createValidationData(tsaRevocationDataXadesT);
            //insertXChild(unsignedSigProps, validationData);
            unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs()
            	.add(validationData);
        }
        
        /*
			From https://msdn.microsoft.com/en-us/library/dd922734(v=office.12).aspx
		
			 If the information as specified in [XAdES] contains a time stamp as specified by the requirements for XAdES-T, 
			 the time stamp information MUST be specified as an EncapsulatedTimeStamp element containing DER encoded ASN.1. data.
			 
			 If the information as specified in [XAdES] contains references to validation data, 
			 the certificates used in the certificate chain, except for the signing certificate, 
			 MUST be contained within the CompleteCertificateRefs element as specified in [XAdES] section 7.4.1. 
			 
			 In addition, for the signature to be considered a well-formed XAdES-C signature, 
			 a CompleteRevocationRefs element MUST be present, as specified in [XAdES] section 7.4.2.
			 
			 If the information as specified in [XAdES] contains time stamps on references to validation data, 
			 the SigAndRefsTimestamp element as specified in [XAdES] section 7.5.1 and [XAdES] section 7.5.1.1 MUST be used. 
			 The SigAndRefsTimestamp element MUST specify the time stamp information as an EncapsulatedTimeStamp element containing DER encoded ASN.1. data.
			 
			 If the information as specified in [XAdES] contains properties for data validation values, 
			 the CertificateValues and RevocationValues elements MUST be constructed as specified in [XAdES] section 7.6.1 
			 and [XAdES] section 7.6.2. Except for the signing certificate, all certificates used in the validation chain 
			 MUST be entered into the CertificateValues element.         * 
         */

        // https://www.w3.org/TR/XAdES/#Syntax_for_XAdES-C_form
        RevocationData revocationData = null;
        List<X509Certificate> certChain = signatureConfig.getCurrentSignatureDetail().getSigningCertificateChain();
        if (signatureConfig.getRevocationDataService() == null) {
            /*
             * Without revocation data service we cannot construct the XAdES-C
             * extension.
             */
        	LOG.info("No revocation data service set; cannot construct the XAdES-C extension"); 

        } else {

	        // XAdES-C: complete certificate refs
	        // <CompleteCertificateRefs
	        CompleteCertificateRefsType completeCertificateRefsType = new CompleteCertificateRefsType();
	        JAXBElement<CompleteCertificateRefsType> completeCertificateRefs = etsi13Factory.createCompleteCertificateRefs(completeCertificateRefsType);
	        unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs()
	        	.add(completeCertificateRefs);
	
	        //CertIDListType certIdList = completeCertificateRefs.addNewCertRefs();
	        CertIDListType certIdList = new CertIDListType();
	        completeCertificateRefsType.setCertRefs(certIdList);
	        
	        /*
	         * We skip the signing certificate itself according to section
	         * 4.4.3.2 of the XAdES 1.4.1 specification.
	         */
	        int chainSize = certChain.size();
	        if (chainSize > 1) {
	            for (X509Certificate cert : certChain.subList(1, chainSize)) {
	                //CertIDType certId = certIdList.addNewCert();
	            	CertIDType certId = new CertIDType();
	            	certIdList.getCert().add(certId);
	            	
	                XAdESSignatureFacet.setCertID(certId, signatureConfig, false, cert);
	            }
	        }
	
	        // XAdES-C: complete revocation refs
	        // <CompleteRevocationRefs
	        CompleteRevocationRefsType completeRevocationRefsType = new CompleteRevocationRefsType();
	        JAXBElement<CompleteRevocationRefsType> completeRevocationRefs = etsi13Factory.createCompleteRevocationRefs(completeRevocationRefsType);
	        unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs()
	        	.add(completeRevocationRefs);
	        
	        revocationData = createRevocationData(certChain, completeRevocationRefsType);
	        
	        // XAdES-X type 1 (SigAndRefsTimeStamp) timestamp
	        List<Node> timeStampNodesXadesX1 = new ArrayList<Node>();
	        timeStampNodesXadesX1.add(nlSigVal.item(0));
	        
	        Document dummy = nlSigVal.item(0).getOwnerDocument(); // may as well import into this 
	
	        timeStampNodesXadesX1.add(importNode(dummy,signatureTimeStamp));
	        timeStampNodesXadesX1.add(importNode(dummy,completeCertificateRefs));
	        timeStampNodesXadesX1.add(importNode(dummy,completeRevocationRefs));
	        
	        // <SigAndRefsTimeStamp
	        LOG.debug( "creating XAdES-X time-stamp");
	        RevocationData tsaRevocationDataXadesX1 = new RevocationData();
	        XAdESTimeStampType timeStampXadesX1 = createXAdESTimeStamp(timeStampNodesXadesX1, tsaRevocationDataXadesX1);
	        JAXBElement<XAdESTimeStampType> sigAndRefsTimeStamp= etsi13Factory.createSigAndRefsTimeStamp(timeStampXadesX1);
			
	        // ??
	        if (tsaRevocationDataXadesX1.hasRevocationDataEntries()) {
	            ValidationDataType timeStampXadesX1ValidationData = createValidationData(tsaRevocationDataXadesX1);
	            //insertXChild(unsignedSigProps, timeStampXadesX1ValidationData);
	            unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs().add(timeStampXadesX1ValidationData);
	        }
	
	        unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs().add(sigAndRefsTimeStamp);
        }
        
        /*  http://mailman.vse.cz/pipermail/sc34wg4/2015-June/003516.html
         * 
			On Behalf Of MURATA  Makoto  
			Sent: Friday, June 5, 2015 2:48 PM  
			To: e-SC34-WG4 at ecma-international.org   
			Subject: Re: Japanese position on the introduction of XAdES to OPC.  
			 
			Please look at the page "State transitions of the XAdES profiles" in  Lecture_on_XAdES_20140923.pdf 
			in WG4 N 289 (in 2014).  http://isotc.iso.org/livelink/livelink?func=ll&objId=16821707&objAction=Open   
			
			It clearly shows that XAdES-X-L (and XAdES-A) can be created from  XAdES-T.  
			It is not required to create XAdES-C before creating  XAdES-X-L.  
			
			In other words, XAdES-X-L signatures WITHOUT REFERENCES TO VALIDATION DATA are perfectly legitimate 
			XAdES signatures.  
			
			However,  JNSA experts believe that MS Office reports such signatures as errors (??).  If the upcoming revision 
			explicitly allows the use of the current  version of XAdES, it will disallow this behaviour thus making 
			MS Office non-conformant.   
			
			I understand why this misinterpretation happened.  The existing XAdES  specifications are extremely unclear 
			about the relationship of conformance levels.  
			
			However, those who are involved in XAdES  implementations (and interoperability testing) agree that 
			XAdES-X-L signatures without references to validation data are perfectly legitimate. 

         */
        
/* TODO: add this for XAdES-X-L, which Office 2013 & 2016 support.  Requires CertificateTrustChecker?
 * at which point add values (Docx4jProperties.getProperty("docx4j.dsig.XAdES.Level", 1)==2) {
 *         
        // <CertificateValues
        // XAdES-X-L
        CertificateValuesType certificateValuesType = new CertificateValuesType();
        JAXBElement<CertificateValuesType> certificateValues = etsi13Factory.createCertificateValues(certificateValuesType);
        unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs().add(certificateValues);
        for (X509Certificate certificate : certChain) {
        	EncapsulatedPKIDataType encapsulatedPKIDataType = new EncapsulatedPKIDataType();
        	certificateValuesType.getEncapsulatedX509CertificateOrOtherCertificate().add(encapsulatedPKIDataType);
            try {
            	encapsulatedPKIDataType.setValue(certificate.getEncoded());
            } catch (CertificateEncodingException e) {
                throw new RuntimeException("certificate encoding error: " + e.getMessage(), e);
            }
        }
        
        // <RevocationValues
        RevocationValuesType revocationValuesType = new RevocationValuesType();
        JAXBElement<RevocationValuesType> revocationValues = etsi13Factory.createRevocationValues(revocationValuesType);
        unsignedSigProps.getCounterSignatureOrSignatureTimeStampOrCompleteCertificateRefs().add(revocationValues);
        
        if (revocationData!=null) {
        	createRevocationValues(revocationValuesType, revocationData);
        }
 */        
        
//        System.out.println("\n\n ** " + XmlUtils.marshaltoString(qualProps, false, false, 
//        		DSigJJAXBContext.jcXmlDSig,  "http://uri.etsi.org/01903/v1.3.2#", "QualifyingProperties", QualifyingPropertiesType.class ) + "\n\n");
        LOG.debug("\n\n " + XmlUtils.marshaltoString(qualProps, DSigJAXBContext.jcXmlDSig) + "\n\n");

        // marshal XAdES-X-L
        Node n = document.importNode( 
	        		XmlUtils.marshaltoW3CDomDocument(qualProps, DSigJAXBContext.jcXmlDSig).getDocumentElement(), 
	        		true);
        qualNl.item(0).getParentNode().replaceChild(n, qualNl.item(0));
    }

	/**
	 * @param certChain
	 * @param completeRevocationRefs
	 * @return
	 * @throws MarshalException
	 * @throws DigitalSignatureException
	 */
	protected RevocationData createRevocationData(
			List<X509Certificate> certChain,
			CompleteRevocationRefsType completeRevocationRefs)
			throws MarshalException, DigitalSignatureException {
		
		RevocationData revocationData = null;
        if (signatureConfig.getRevocationDataService() != null) {
	        revocationData = signatureConfig.getRevocationDataService().getRevocationData(certChain);
	        if (revocationData.hasCRLs()) {
	            //CRLRefsType crlRefs = completeRevocationRefs.addNewCRLRefs();
	        	CRLRefsType crlRefs = new CRLRefsType();
	        	completeRevocationRefs.setCRLRefs(crlRefs);
	        	
	            completeRevocationRefs.setCRLRefs(crlRefs);
	
	            for (byte[] encodedCrl : revocationData.getCRLs()) {
	                //CRLRefType crlRef = crlRefs.addNewCRLRef();
	            	CRLRefType crlRef = new CRLRefType();
	            	crlRefs.getCRLRef().add(crlRef);
	            	
	                X509CRL crl;
	                try {
	                    crl = (X509CRL) this.certificateFactory
	                            .generateCRL(new ByteArrayInputStream(encodedCrl));
	                } catch (CRLException e) {
	                    throw new RuntimeException("CRL parse error: "
	                            + e.getMessage(), e);
	                }
	
	                //CRLIdentifierType crlIdentifier = crlRef.addNewCRLIdentifier();
	                CRLIdentifierType crlIdentifier = new CRLIdentifierType();
	                crlRef.setCRLIdentifier(crlIdentifier);
	                
	                String issuerName = crl.getIssuerDN().getName().replace(",", ", ");
	                crlIdentifier.setIssuer(issuerName);
	                Calendar cal = Calendar.getInstance();
	                cal.setTime(crl.getThisUpdate());
	                
	                javax.xml.datatype.XMLGregorianCalendar xmlGregorianCalendar = null;
	        		try {
	        			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
	        		} catch (DatatypeConfigurationException e) {
	        			throw new MarshalException(e);
	        		}
	                xmlGregorianCalendar.setYear(cal.get(Calendar.YEAR));
	                xmlGregorianCalendar.setDay(cal.get(Calendar.DAY_OF_MONTH));
	                xmlGregorianCalendar.setHour(cal.get(Calendar.HOUR_OF_DAY));
	                xmlGregorianCalendar.setMinute(cal.get(Calendar.MINUTE));
	                xmlGregorianCalendar.setSecond(cal.get(Calendar.SECOND));
	                
	                crlIdentifier.setIssueTime(xmlGregorianCalendar);
	                crlIdentifier.setNumber(getCrlNumber(crl));
	
	                //DigestAlgAndValueType digestAlgAndValue = crlRef.addNewDigestAlgAndValue();
	                DigestAlgAndValueType digestAlgAndValue = new DigestAlgAndValueType();
	                crlRef.setDigestAlgAndValue(digestAlgAndValue);
	                XAdESSignatureFacet.setDigestAlgAndValue(digestAlgAndValue, encodedCrl, signatureConfig.getDigestAlgo());
	            }
	        }
	        if (revocationData.hasOCSPs()) {
	            //OCSPRefsType ocspRefs = completeRevocationRefs.addNewOCSPRefs();
	        	OCSPRefsType ocspRefs = new OCSPRefsType();
	        	completeRevocationRefs.setOCSPRefs(ocspRefs);
	        	
	            for (byte[] ocsp : revocationData.getOCSPs()) {
	                try {
	                    //OCSPRefType ocspRef = ocspRefs.addNewOCSPRef();
	                	OCSPRefType ocspRef = new OCSPRefType();
	                	ocspRefs.getOCSPRef().add(ocspRef);
	    
	                    //DigestAlgAndValueType digestAlgAndValue = ocspRef.addNewDigestAlgAndValue();
	                	DigestAlgAndValueType digestAlgAndValue = new DigestAlgAndValueType();
	                	ocspRef.setDigestAlgAndValue(digestAlgAndValue);
	                    XAdESSignatureFacet.setDigestAlgAndValue(digestAlgAndValue, ocsp, signatureConfig.getDigestAlgo());
	    
	                    //OCSPIdentifierType ocspIdentifier = ocspRef.addNewOCSPIdentifier();
	                    OCSPIdentifierType ocspIdentifier = new OCSPIdentifierType();
	                    ocspRef.setOCSPIdentifier(ocspIdentifier);
	                    
	                    OCSPResp ocspResp = new OCSPResp(ocsp);
	                    
	                    BasicOCSPResp basicOcspResp = (BasicOCSPResp)ocspResp.getResponseObject();
	                    
	                    Calendar cal = Calendar.getInstance();
	                    cal.setTime(basicOcspResp.getProducedAt());
	                    
	                    javax.xml.datatype.XMLGregorianCalendar xmlGregorianCalendar = null;
	            		try {
	            			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
	            		} catch (DatatypeConfigurationException e) {
	            			throw new XMLSignatureException(e);
	            		}
	                    xmlGregorianCalendar.setYear(cal.get(Calendar.YEAR));
	                    xmlGregorianCalendar.setDay(cal.get(Calendar.DAY_OF_MONTH));
	                    xmlGregorianCalendar.setHour(cal.get(Calendar.HOUR_OF_DAY));
	                    xmlGregorianCalendar.setMinute(cal.get(Calendar.MINUTE));
	                    xmlGregorianCalendar.setSecond(cal.get(Calendar.SECOND));
	                    
	                    ocspIdentifier.setProducedAt(xmlGregorianCalendar);
	    
	                    //ResponderIDType responderId = ocspIdentifier.addNewResponderID();
	                    ResponderIDType responderId = new ResponderIDType();
	                    ocspIdentifier.setResponderID(responderId);
	    
	                    RespID respId = basicOcspResp.getResponderId();
	                    ResponderID ocspResponderId = respId.toASN1Primitive();//.toASN1Object();  BC 1.52-> 1.54
	                    DERTaggedObject derTaggedObject = (DERTaggedObject)ocspResponderId.toASN1Primitive();
	                    if (2 == derTaggedObject.getTagNo()) {
	                        ASN1OctetString keyHashOctetString = (ASN1OctetString)derTaggedObject.getExplicitBaseObject();
	                        byte key[] = keyHashOctetString.getOctets();
	                        responderId.setByKey(key);
	                    } else {
	                        X500Name name = X500Name.getInstance(derTaggedObject.getExplicitBaseObject());
	                        String nameStr = name.toString();
	                        responderId.setByName(nameStr);
	                    }
	                } catch (Exception e) {
	                    throw new RuntimeException("OCSP decoding error: " + e.getMessage(), e);
	                }
	            }
	        }
        }
		return revocationData;
	}
    
//    private Node importNode(Document document, Object o, String local, Class declaredType) {
//    	
//    	return document.importNode(
//    			XmlUtils.marshaltoW3CDomDocument(o, DSigJJAXBContext.jcXmlDSig, "http://uri.etsi.org/01903/v1.3.2#", 
//    					local, declaredType).getDocumentElement(), 
//    			true);
//    }
    

    private Node importNode(Document document, Object o) {
    	    	
    	return document.importNode(
    			XmlUtils.marshaltoW3CDomDocument(o, DSigJAXBContext.jcXmlDSig).getDocumentElement(), 
    			true);
    }
    
    public static byte[] getC14nValue(List<Node> nodeList, String c14nAlgoId) {
        ByteArrayOutputStream c14nValue = new ByteArrayOutputStream();
        try {
            for (Node node : nodeList) {
                /*
                 * Re-initialize the c14n else the namespaces will get cached
                 * and will be missing from the c14n resulting nodes.
                 */
                Canonicalizer c14n = Canonicalizer.getInstance(c14nAlgoId);
                
//                c14nValue.write(c14n.canonicalizeSubtree(node));
                c14n.canonicalizeSubtree(node,c14nValue);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("c14n error: " + e.getMessage(), e);
        }
        return c14nValue.toByteArray();
    }

    private BigInteger getCrlNumber(X509CRL crl) {
        try {
            byte[] crlNumberExtensionValue = crl.getExtensionValue(Extension.cRLNumber.getId());
            if (null == crlNumberExtensionValue) {
                return null;
            }

            @SuppressWarnings("resource")
            ASN1InputStream asn1InputStream = new ASN1InputStream(crlNumberExtensionValue);
            ASN1OctetString octetString = (ASN1OctetString)asn1InputStream.readObject();
            byte[] octets = octetString.getOctets();
            asn1InputStream = new ASN1InputStream(octets);
            ASN1Integer integer = (ASN1Integer)asn1InputStream.readObject();
            BigInteger crlNumber = integer.getPositiveValue();
            return crlNumber;
        } catch (Exception e) {
            throw new RuntimeException("I/O error: " + e.getMessage(), e);
        }
    }

    private XAdESTimeStampType createXAdESTimeStamp(
            List<Node> nodeList,
            RevocationData revocationData) {
        byte[] c14nSignatureValueElement = getC14nValue(nodeList, signatureConfig.getXadesCanonicalizationMethod());

        return createXAdESTimeStamp(c14nSignatureValueElement, revocationData);
    }

    private XAdESTimeStampType createXAdESTimeStamp(byte[] data, RevocationData revocationData)  {
        // create the time-stamp
        byte[] timeStampToken;
        try {
            timeStampToken = signatureConfig.getTspService().timeStamp(data, revocationData);
        } catch (Exception e) {
            throw new RuntimeException("error while creating a time-stamp: "
                    + e.getMessage(), e);
        }

        // create a XAdES time-stamp container
        XAdESTimeStampType xadesTimeStamp = new XAdESTimeStampType(); 
        
	        xadesTimeStamp.setId("time-stamp-" + UUID.randomUUID().toString());
	        //CanonicalizationMethodType c14nMethod = xadesTimeStamp.addNewCanonicalizationMethod();
	        CanonicalizationMethodType c14nMethod = new CanonicalizationMethodType();
	        xadesTimeStamp.setCanonicalizationMethod(c14nMethod);
	        
	        c14nMethod.setAlgorithm(signatureConfig.getXadesCanonicalizationMethod());
	
	        // embed the time-stamp
	        //EncapsulatedPKIDataType encapsulatedTimeStamp = xadesTimeStamp.addNewEncapsulatedTimeStamp();
	        EncapsulatedPKIDataType encapsulatedTimeStamp = new EncapsulatedPKIDataType();
	        xadesTimeStamp.getEncapsulatedTimeStampOrXMLTimeStamp().add(encapsulatedTimeStamp);
	        
	        encapsulatedTimeStamp.setValue(timeStampToken);
	        encapsulatedTimeStamp.setId("time-stamp-token-" + UUID.randomUUID().toString());
	
	        return xadesTimeStamp;
        
    }

    private ValidationDataType createValidationData(
            RevocationData revocationData) {
        //ValidationDataType validationData = ValidationDataType.Factory.newInstance();
    	ValidationDataType validationData = new ValidationDataType(); 
        
    	//RevocationValuesType revocationValues = validationData.addNewRevocationValues();
    	RevocationValuesType revocationValues = new RevocationValuesType();
    	validationData.setRevocationValues(revocationValues);
        
    	createRevocationValues(revocationValues, revocationData);
        return validationData;
    }

    private void createRevocationValues(
            RevocationValuesType revocationValues, RevocationData revocationData) {
        if (revocationData.hasCRLs()) {
            //CRLValuesType crlValues = revocationValues.addNewCRLValues();
        	CRLValuesType crlValues = new CRLValuesType(); 
        	revocationValues.setCRLValues(crlValues);
        	
            for (byte[] crl : revocationData.getCRLs()) {
                //EncapsulatedPKIDataType encapsulatedCrlValue = crlValues.addNewEncapsulatedCRLValue();
            	EncapsulatedPKIDataType encapsulatedCrlValue = new EncapsulatedPKIDataType();
            	crlValues.getEncapsulatedCRLValue().add(encapsulatedCrlValue);
                encapsulatedCrlValue.setValue(crl);
            }
        }
        if (revocationData.hasOCSPs()) {
            //OCSPValuesType ocspValues = revocationValues.addNewOCSPValues();
        	OCSPValuesType ocspValues = new OCSPValuesType();
        	revocationValues.setOCSPValues(ocspValues);
            for (byte[] ocsp : revocationData.getOCSPs()) {
                //EncapsulatedPKIDataType encapsulatedOcspValue = ocspValues.addNewEncapsulatedOCSPValue(); //?
            	EncapsulatedPKIDataType encapsulatedOcspValue = new EncapsulatedPKIDataType();
            	ocspValues.getEncapsulatedOCSPValue().add(encapsulatedOcspValue);
                encapsulatedOcspValue.setValue(ocsp);
            }
        }
    }
}
