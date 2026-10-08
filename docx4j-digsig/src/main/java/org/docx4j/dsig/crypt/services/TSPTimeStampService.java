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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.docx4j.dsig.crypt.SignatureConfig;
import org.docx4j.dsig.DigitalSignatureException;

/**
 * A TSP time-stamp service implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class TSPTimeStampService implements TimeStampService {

	private static Logger LOG = LoggerFactory.getLogger(TSPTimeStampService.class);	

    private SignatureConfig signatureConfig;

    /**
     * Maps the digest algorithm to corresponding OID value.
     */
    public ASN1ObjectIdentifier mapDigestAlgoToOID(HashAlgorithm digestAlgo) {
        switch (digestAlgo) {
        case sha1:   return X509ObjectIdentifiers.id_SHA1;
        case sha256: return NISTObjectIdentifiers.id_sha256;
        case sha384: return NISTObjectIdentifiers.id_sha384;
        case sha512: return NISTObjectIdentifiers.id_sha512;
        default:
            throw new IllegalArgumentException("unsupported digest algo: " + digestAlgo);
        }
    }

    @SuppressWarnings("unchecked")
    public byte[] timeStamp(byte[] data, RevocationData revocationData)
            throws Exception {
    	
    	if (signatureConfig.getTspUrl()==null) {
    		throw new DigitalSignatureException("No timestamp provider (TSP) URL set in signatureConfig");
    	}
    	
        // digest the message
        MessageDigest messageDigest = CryptoFunctions.getMessageDigest(signatureConfig.getTspDigestAlgo());
        byte[] digest = messageDigest.digest(data);

        // generate the TSP request
        BigInteger nonce = new BigInteger(128, new SecureRandom());
        TimeStampRequestGenerator requestGenerator = new TimeStampRequestGenerator();
        requestGenerator.setCertReq(true);
        String requestPolicy = signatureConfig.getTspRequestPolicy();
        if (requestPolicy != null) {
            requestGenerator.setReqPolicy(new ASN1ObjectIdentifier(requestPolicy));
        }
        ASN1ObjectIdentifier digestAlgoOid = mapDigestAlgoToOID(signatureConfig.getTspDigestAlgo());
        TimeStampRequest request = requestGenerator.generate(digestAlgoOid, digest, nonce);
        byte[] encodedRequest = request.getEncoded();

        // create the HTTP POST request
        Proxy proxy = Proxy.NO_PROXY;
        if (signatureConfig.getProxyUrl() != null) {
            URL proxyUrl = new URL(signatureConfig.getProxyUrl());
            String host = proxyUrl.getHost();
            int port = proxyUrl.getPort();
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, (port == -1 ? 80 : port)));
        }
        
        HttpURLConnection huc = (HttpURLConnection)new URL(signatureConfig.getTspUrl()).openConnection(proxy);
        
        if (signatureConfig.getTspUser() != null) {
            String userPassword = signatureConfig.getTspUser() + ":" + signatureConfig.getTspPass();
            String encoding = DatatypeConverter.printBase64Binary(userPassword.getBytes(Charset.forName("iso-8859-1")));
            huc.setRequestProperty("Authorization", "Basic " + encoding);
        }

        huc.setRequestMethod("POST");
        huc.setConnectTimeout(20000);
        huc.setReadTimeout(20000);
        huc.setDoOutput(true); // also sets method to POST.
        huc.setRequestProperty("User-Agent", signatureConfig.getUserAgent());
        huc.setRequestProperty("Content-Type", signatureConfig.isTspOldProtocol()
            ? "application/timestamp-request"
            : "application/timestamp-query"); // "; charset=ISO-8859-1");
        
        OutputStream hucOut = huc.getOutputStream();
        /* https://freetsa.org/tsr
         * SSL handshake failed: sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1355)
         */
        hucOut.write(encodedRequest);
        
        // invoke TSP service
        huc.connect();
        
        int statusCode = huc.getResponseCode();
        if (statusCode != 200) {
            LOG.error( "Error contacting TSP server " +signatureConfig.getTspUrl());
            throw new IOException("Error contacting TSP server " + signatureConfig.getTspUrl());
        }

        // HTTP input validation
        String contentType = huc.getHeaderField("Content-Type");
        if (null == contentType) {
            throw new RuntimeException("missing Content-Type header");
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(huc.getInputStream(), bos);
        LOG.debug( "response content: ", bos.toString());
        
        if (!contentType.startsWith(signatureConfig.isTspOldProtocol() 
            ? "application/timestamp-response"
            : "application/timestamp-reply"
        )) {
            throw new RuntimeException("invalid Content-Type: " + contentType);
        }
        
        if (bos.size() == 0) {
            throw new RuntimeException("Content-Length is zero");
        }

        // TSP response parsing and validation
        byte[] responseBytes = bos.toByteArray();
        TimeStampResponse timeStampResponse = new TimeStampResponse(responseBytes);
        try {
        	timeStampResponse.validate(request);
        } catch ( org.bouncycastle.tsp.TSPValidationException e) {
        	LOG.error(e.getMessage(), e);
        	LOG.error(new String(responseBytes));
        	throw e;
        }

        if (0 != timeStampResponse.getStatus()) {
            LOG.debug( "status: " + timeStampResponse.getStatus());
            LOG.debug("status string: " + timeStampResponse.getStatusString());
            PKIFailureInfo failInfo = timeStampResponse.getFailInfo();
            if (null != failInfo) {
                LOG.debug( "fail info int value: " + failInfo.intValue());
                if (/*PKIFailureInfo.unacceptedPolicy*/(1 << 8) == failInfo.intValue()) {
                    LOG.debug( "unaccepted policy");
                }
            }
            throw new RuntimeException("timestamp response status != 0: "
                    + timeStampResponse.getStatus());
        }
        TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
        SignerId signerId = timeStampToken.getSID();
        BigInteger signerCertSerialNumber = signerId.getSerialNumber();
        X500Name signerCertIssuer = signerId.getIssuer();
        LOG.debug( "signer cert serial number: " + signerCertSerialNumber);
        LOG.debug( "signer cert issuer: " + signerCertIssuer);

        // TSP signer certificates retrieval
        Collection<X509CertificateHolder> certificates = timeStampToken.getCertificates().getMatches(null);
        
        X509CertificateHolder signerCert = null;
        Map<X500Name, X509CertificateHolder> certificateMap = new HashMap<X500Name, X509CertificateHolder>();
        for (X509CertificateHolder certificate : certificates) {
            if (signerCertIssuer.equals(certificate.getIssuer())
                && signerCertSerialNumber.equals(certificate.getSerialNumber())) {
                signerCert = certificate;
            }
            certificateMap.put(certificate.getSubject(), certificate);
        }

        // TSP signer cert path building
        if (signerCert == null) {
            throw new RuntimeException("TSP response token has no signer certificate");
        }
        List<X509Certificate> tspCertificateChain = new ArrayList<X509Certificate>();
        JcaX509CertificateConverter x509converter = new JcaX509CertificateConverter();
        x509converter.setProvider("BC");
        X509CertificateHolder certificate = signerCert;
        do {
            LOG.debug( "adding to certificate chain: " + certificate.getSubject());
            tspCertificateChain.add(x509converter.getCertificate(certificate));
            if (certificate.getSubject().equals(certificate.getIssuer())) {
                break;
            }
            certificate = certificateMap.get(certificate.getIssuer());
        } while (null != certificate);

        // verify TSP signer signature
        X509CertificateHolder holder = new X509CertificateHolder(tspCertificateChain.get(0).getEncoded());
        DefaultCMSSignatureAlgorithmNameGenerator nameGen = new DefaultCMSSignatureAlgorithmNameGenerator();
        DefaultSignatureAlgorithmIdentifierFinder sigAlgoFinder = new DefaultSignatureAlgorithmIdentifierFinder();
        DefaultDigestAlgorithmIdentifierFinder hashAlgoFinder = new DefaultDigestAlgorithmIdentifierFinder();
        BcDigestCalculatorProvider calculator = new BcDigestCalculatorProvider();
        BcRSASignerInfoVerifierBuilder verifierBuilder = new BcRSASignerInfoVerifierBuilder(nameGen, sigAlgoFinder, hashAlgoFinder, calculator);
        SignerInformationVerifier verifier = verifierBuilder.build(holder);
        
        timeStampToken.validate(verifier);

        // verify TSP signer certificate
        if (signatureConfig.getTspValidator() != null) {
            signatureConfig.getTspValidator().validate(tspCertificateChain, revocationData);
        }

        LOG.debug( "time-stamp token time: "
                + timeStampToken.getTimeStampInfo().getGenTime());

        byte[] timestamp = timeStampToken.getEncoded();
        return timestamp;
    }

    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }
}