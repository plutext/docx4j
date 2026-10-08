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
package org.docx4j.dsig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.digitalsignature.CertificateTrustChecker;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureOriginPart;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.docx4j.openpackaging.parts.digitalsignature.TrustCertificateUnconditionally;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.slf4j.LoggerFactory;

import org.docx4j.dsig.crypt.SignatureConfig;
import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.crypt.SignatureInfo;
import org.docx4j.dsig.anchor.SpreadsheetMLVmlPartSignatureLineHelper;
import org.docx4j.dsig.anchor.WordMLPictHelper;

/**
 * @author jharrop
 *
 */
public class SignatureHelper {
	
	private static Logger log = LoggerFactory.getLogger(SignatureHelper.class);	
	
	
	private SignatureConfig signatureConfig = new SignatureConfig();
	
	public SignatureConfig getSignatureConfig() {
		return signatureConfig;
	}

	private OpcPackage pkg;
	
	
	static {
		SignatureInfo.initXmlProvider();	
		
		/* otherwise you'll get 
		 * 
		 * java.security.NoSuchAlgorithmException: http://schemas.openxmlformats.org/package/2006/RelationshipTransform algorithm and DOM mechanism not available
			at javax.xml.crypto.dsig.TransformService.getInstance(Unknown Source)
			at org.apache.jcp.xml.dsig.internal.dom.DOMTransform.<init>(DOMTransform.java:85)
		 */
		
		
	}
	
	/**
	 * 
	 */
	public SignatureHelper(OpcPackage pkg) {
		this.pkg = pkg;
		signatureConfig.setOpcPackage(pkg);
		
	}
	

	/**  
	 * Provide details of a PKCS12 key, in preparation for signing this package
	 * 
	 * @param privateKeyIS
	 * @param password
	 * @throws Docx4JException
	 */
	public SignatureDetail configureSignature(InputStream PKCS12stream, String password) throws Docx4JException  {

		return configureSignature( PKCS12stream, password.toCharArray());
	}
	
	/**
	 * Provide details of a PKCS12 key, in preparation for signing this package.
	 * 
	 * @param privateKeyIS
	 * @param password
	 * @param signatureLine
	 * @throws Docx4JException
	 */
	public SignatureDetail configureSignature(InputStream PKCS12stream, char[] password) throws Docx4JException  {

		try {
			
	        KeyStore keystore = KeyStore.getInstance("PKCS12"); // a new keystore
	        try {
	        	keystore.load(PKCS12stream, password); // now containing one entry
	        } catch (IOException ioe) {

	        	if (ioe.getMessage().contains("javax.crypto.BadPaddingException")) {
	        		throw new Docx4JException("Wrong password for certificate?", ioe);
	        	} else {
	        		throw ioe;
	        	}
	        }
	        	        
	        String alias = keystore.aliases().nextElement();
	        
	        PasswordProtection pp = new PasswordProtection(password);
	        
	        return configureSignature( (KeyStore.PrivateKeyEntry)keystore.getEntry(alias, pp));
	        
	        //keystore.deleteEntry(alias); // no need for this
	        
		} catch (Docx4JException e) {
        	throw e;
		} catch (Exception e) {
        	throw new Docx4JException(e.getMessage(), e);
        }		
	}
	
	

	/**
	 * Provide details of a key to be used for signing this package
	 * 
	 * @param pkEntry
	 */
	public SignatureDetail configureSignature(KeyStore.PrivateKeyEntry pkEntry) {

		SignatureDetail signatureDetail = new SignatureDetail(pkEntry);
		
        signatureConfig.getSignatureDetailList().get().add(signatureDetail);
        
        return signatureDetail;
	}
	
	/**
	 * Set the extra info required to visibly sign this package
	 * with a textual signature
	 * (associating it with a signature line on the document surface)
	 * 
	 * @param signatureDetail
	 * @param signatureLine
	 * @param validSigLnImg   the image (Word uses EMF, but PNG tested ok with Word 2010
	 * and MUST be used since we can't manipulate an EMF, yet), 
	 * displayed in the document; should be the signature line plus the signature.
	 * @param signatureText
	 */
	public void setVisualSignature(SignatureDetail signatureDetail, CTSignatureLine signatureLine, 
			byte[] validSigLnImg, String signatureText) {
		
		signatureDetail.setVisualSignature(signatureLine, validSigLnImg, signatureText);
	}

	/**
	 * Set the extra info required to visibly sign this package
	 * with a graphical signature
	 * (associating it with a signature line on the document surface)
	 * 
	 * @param signatureDetail
	 * @param signatureLine
	 * @param validSigLnImg   the image (Word uses EMF, but PNG tested ok with Word 2010
	 * and MUST be used since we can't manipulate an EMF, yet), 
	 * displayed in the document; should be the signature line plus the signature.
	 * @param signatureImage  the signature itself (not actually displayed), but stored
	 */
	public void setVisualSignature(SignatureDetail signatureDetail, CTSignatureLine signatureLine, 
			byte[] validSigLnImg, byte[] signatureImage) {
		
		signatureDetail.setVisualSignature(signatureLine, validSigLnImg, signatureImage);
	}
	
	
	/**
	 * Sign it, using the key provided via configureSignature
	 */
	public void sign() throws Docx4JException {

		if (signatureConfig.getSignatureDetailList().get().isEmpty()) throw new Docx4JException("No key info has been provided for signing. Invoke configureSignature first." ); 
		
		
		
		try {
			
			// Word 2013 defaults to DocSecurity=0 for a signed docx!
			// But I guess it 2 would make sense, and other values are possible.
			// No point in setting it to anything here!
	        
	        // adding the signature document to the package
	        SignatureInfo si = new SignatureInfo();
	        si.setSignatureConfig(signatureConfig);
	        si.confirmSignature();
	        
		} catch (Exception e) {
	    	throw new Docx4JException(e.getMessage(), e);
	    }
		
	}
	

	/**
	 * Signed if SignatureOriginPart has rels to one or more XmlSignatureParts
	 * @return
	 */
	public boolean isPackageSigned() {
		
		// effectively same as SignatureInfo.getSignatureParts()
		
		if (this.pkg.getRelationshipsPart()!=null) {
			Relationship rel = this.pkg.getRelationshipsPart().getRelationshipByType(Namespaces.DIGITAL_SIGNATURE_ORIGIN);
			if (rel==null) return false;
			SignatureOriginPart signatureOriginPart = (SignatureOriginPart)this.pkg.getRelationshipsPart().getPart(rel);
			
			return (signatureOriginPart.getRelationshipsPart()!=null
					&& signatureOriginPart.getRelationshipsPart().getRelationshipsByType(Namespaces.DIGITAL_SIGNATURE).size()>0);
		}
		
		return false;
				
		
	}
    
    /**
     * Set the signature status for each signature. This mimics what you see in the Word signatures pane,
     * and checks the validity of the signature (hash), whether it is complete or partial,
     * and the certificate used to sign with.
     * 
     * @param zippedOfficeFile a docx/pptx/xlsx file (not docx4j package), to ensure the input has not been modified in docx4j
     * @param certificateTrustChecker 
     * @return
     * @throws SignaturesNotPresentException
     * @throws DigitalSignatureException
     */
    public static List<XmlSignaturePart> getSignaturesStatus(InputStream zippedOfficeFile, CertificateTrustChecker certificateTrustChecker) 
    		throws SignaturesNotPresentException, DigitalSignatureException {
    	
    	OpcPackage pkg;
		try {
			pkg = OpcPackage.load(zippedOfficeFile);
		} catch (Docx4JException e1) {
			throw new DigitalSignatureException(e1.getMessage(), e1);
		}
		return getSignaturesStatus(pkg, certificateTrustChecker);
    }

    /**
     * Set the signature status for each signature. This mimics what you see in the Word signatures pane,
     * and checks the validity of the signature (hash), whether it is complete or partial,
     * and the certificate used to sign with.
     * 
     * @param zippedOfficeFile a docx/pptx/xlsx file (not docx4j package), to ensure the input has not been modified in docx4j
     * @param certificateTrustChecker 
     * @return
     * @throws SignaturesNotPresentException
     * @throws DigitalSignatureException
     * @since 17.2.1
     */
    public static List<XmlSignaturePart> getSignaturesStatus(File zippedOfficeFile, CertificateTrustChecker certificateTrustChecker) 
    		throws SignaturesNotPresentException, DigitalSignatureException {
    	
    	OpcPackage pkg;
		try {
			pkg = OpcPackage.load(zippedOfficeFile);
		} catch (Docx4JException e1) {
			throw new DigitalSignatureException(e1.getMessage(), e1);
		}
		return getSignaturesStatus(pkg, certificateTrustChecker);
    }
    
    private static List<XmlSignaturePart> getSignaturesStatus(OpcPackage pkg, CertificateTrustChecker certificateTrustChecker) 
    		throws SignaturesNotPresentException, DigitalSignatureException {
    	
    	SignatureHelper helper = new SignatureHelper(pkg);
    	    	
    	List<XmlSignaturePart> signatureParts = helper.getSignatureParts();
    	if (signatureParts.isEmpty()) throw new SignaturesNotPresentException("No signatures present");
    	
        for (XmlSignaturePart sp : helper.getSignatureParts()) {
        	
        	sp.setCertificateTrustChecker(certificateTrustChecker);
        	try {
        		SignatureStatus status = sp.getSignatureStatus(helper.signatureConfig);
            	log.debug(sp.getPartName().getName() + " ---> " + status);
        	} catch (Exception e) {
        		e.printStackTrace();
            	log.debug(sp.getPartName().getName() + " ---> ???" );        		
        	}
        	
        }
        return signatureParts;
    }
    
    
    
    /**
     * Set the signature status for each signature, without checking the validity of the 
     * certificate itself. This is a subset of what you see in the Word signatures pane,
     * since it checks the validity of the signature (hash), and whether it is complete or partial,
     * BUT NOT the certificate used to sign with.
     * 
     * @param zippedOfficeFile a docx/pptx/xlsx file (not docx4j package), to ensure the input has not been modified in docx4j
     * @param certificateTrustChecker 
     * @return
     * @throws SignaturesNotPresentException
     * @throws DigitalSignatureException
     */
    public static List<XmlSignaturePart> getSignaturesStatusTrusted(InputStream zippedOfficeFile) throws SignaturesNotPresentException, DigitalSignatureException {
    	
    	return getSignaturesStatus( zippedOfficeFile, new TrustCertificateUnconditionally());
    }
    
    /**
     * Set the signature status for each signature, without checking the validity of the 
     * certificate itself. This is a subset of what you see in the Word signatures pane,
     * since it checks the validity of the signature (hash), and whether it is complete or partial,
     * BUT NOT the certificate used to sign with.
     * 
     * @param zippedOfficeFile a docx/pptx/xlsx file (not docx4j package), to ensure the input has not been modified in docx4j
     * @return
     * @throws SignaturesNotPresentException
     * @throws DigitalSignatureException
     * @since 17.2.1
     */
    public static List<XmlSignaturePart> getSignaturesStatusTrusted(File zippedOfficeFile) throws SignaturesNotPresentException, DigitalSignatureException {
    	
    	return getSignaturesStatus( zippedOfficeFile, new TrustCertificateUnconditionally());
    }
    

	
	/**
	 * Remove all signature parts from this package
	 * @param pkg
	 */
	public void removeSignatureParts(OpcPackage pkg) {
		
		List<XmlSignaturePart> partsToRemove = new ArrayList<XmlSignaturePart>(); 
		
		for (Part p : pkg.getParts().getParts().values()) {
			
			if (p instanceof XmlSignaturePart) {
				partsToRemove.add( (XmlSignaturePart)p);
			}			
		}
		
		for (XmlSignaturePart p : partsToRemove) {
			pkg.getParts().getParts().remove(p.getPartName() );
		}
		
		Relationship originRel = pkg.getRelationshipsPart().getRelationshipByType(Namespaces.DIGITAL_SIGNATURE_ORIGIN);
		SignatureOriginPart originSigsPart = null; 
        if (originRel != null) {
        	originSigsPart = (SignatureOriginPart)pkg.getRelationshipsPart().getPart(originRel);
        	pkg.getParts().getParts().remove(originSigsPart.getPartName() );
    		pkg.getRelationshipsPart().removeRelationship(originRel);        	
        }
		
	}
	
    public List<XmlSignaturePart> getSignatureParts() {

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
     * Setup a visible signature (docx)
     * 
     * @param signatureline
     * @param imageRelID
     * @param alt
     * @param isFirstEmbedding
     * @return
     * @throws Exception
     */
    public P createDocxSignatureLineP(CTSignatureLine signatureline,  
			String imageRelID, String alt,  boolean isFirstEmbedding) throws Exception {
    	
    	/*
    	 * TODO: help user to create signature line object, and image
    	 */
    	
    	P p = new P();
    	R run = new R();
    	p.getContent().add(run);
    	
    	JAXBElement<org.docx4j.wml.Pict> pict = WordMLPictHelper.getPict( signatureline, imageRelID, alt, isFirstEmbedding);
    	
    	run.getContent().add(pict);
    	
    	return p;
    }
    
    public CTSignatureLine createCTSignatureLine(String suggestedSigner, boolean showSignDate ) {

	    CTSignatureLine signatureline = WordMLPictHelper.vmlofficedrawingObjectFactory.createCTSignatureLine(); 
        signatureline.setExt(org.docx4j.vml.STExt.EDIT);
        signatureline.setIssignatureline(org.docx4j.vml.officedrawing.STTrueFalse.T);
        signatureline.setProvid( "{00000000-0000-0000-0000-000000000000}"); 
        signatureline.setSuggestedsigner( suggestedSigner);
        if (showSignDate) {
        	signatureline.setShowsigndate(org.docx4j.vml.officedrawing.STTrueFalse.T);
        } else {
        	signatureline.setShowsigndate(org.docx4j.vml.officedrawing.STTrueFalse.F);        	
        }

        // match Object[@Id="idOfficeObject"]/SignatureProperties/SignatureProperty/SignatureInfoV1/SetupID
        signatureline.setId( "{" + java.util.UUID.randomUUID().toString().toUpperCase() +"}");  
        
        return signatureline;
    	
    }

    /**
     * Setup a visible signature (xlsx): add a signature line to a worksheet.
     * To sign it, pass the same CTSignatureLine to setVisualSignature. 
     * 
     * @param sheet
     * @param signatureline see createCTSignatureLine
     * @param image what the sheet shows until the line is signed (Excel uses EMF; PNG is accepted) 
     * @param anchor where it goes, as 8 numbers: from column, offset, row, offset; 
     * to column, offset, row, offset.  For example "1, 0, 1, 0, 5, 0, 7, 8" 
     * @param alt alternative text; if null, Excel's own
     * @since 17.2.1
     */
    public void createXlsxSignatureLine(WorksheetPart sheet, CTSignatureLine signatureline,  
			byte[] image, String anchor, String alt) throws Docx4JException {
    	
    	try {
    		new SpreadsheetMLVmlPartSignatureLineHelper().addSignatureLine(sheet, signatureline, image, anchor, alt);
    	} catch (Xlsx4jException e) {
    		throw new Docx4JException(e.getMessage(), e);
    	}
    }

    /**
     * The signature lines on a worksheet, whether or not they have been signed;
     * so that a line which Excel (or createXlsxSignatureLine) put there can be passed 
     * to setVisualSignature.  A line is matched to its signature by getId().
     *   
     * @since 17.2.1
     */
    public List<CTSignatureLine> getXlsxSignatureLines(WorksheetPart sheet) throws Docx4JException {
    	
    	try {
    		return new SpreadsheetMLVmlPartSignatureLineHelper().getSignatureLines(sheet);
    	} catch (Xlsx4jException e) {
    		throw new Docx4JException(e.getMessage(), e);
    	}
    }
    
    
    
//    /**
//     * Get all signature lines in this document; a signature line can be associated with an XML Signature Part
//     * @return
//     */
//    public List<CTSignatureLine> getSignatureLines() {
//    	
//    	/*
//            <o:signatureline v:ext="edit" id="{95FBD063-BB50-47B0-B1D4-11BAE2C2AF54}" provid="{00000000-0000-0000-0000-000000000000}" o:suggestedsigner="me" issignatureline="t"/>
//            
//            copied into:
//
//			  <Object Id="idOfficeObject">
//			    <SignatureProperties>
//			      <SignatureProperty Id="idOfficeV1Details" Target="#idPackageSignature">
//			        <SignatureInfoV1 xmlns="http://schemas.microsoft.com/office/2006/digsig">
//			          <SetupID>{95FBD063-BB50-47B0-B1D4-11BAE2C2AF54}</SetupID>
//			  
//			          <SignatureProviderId>{00000000-0000-0000-0000-000000000000}</SignatureProviderId>
//			          
//			We need to feed this to OOXMLSignatureFacet.addSignatureInfo    
//			
//			do that via signatureConfig
//			
//			At present we have List<KeyStore.PrivateKeyEntry>. Each of those should be paired with a CTSignatureLine? 
//    	 * 
//    	 */
//    	
//    	return null;
//    }

}
