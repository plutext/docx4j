package org.docx4j.samples.digsig;

import java.io.File;
import java.util.List;

import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;

import org.docx4j.dsig.SignatureHelper;
import org.docx4j.dsig.SignaturesNotPresentException;

/**
 * Example of validating the signatures in a docx, pptx or xlsx (or their macro-enabled
 * and template variants), whether signed by Office or by DigSig.
 *
 * The file is passed as a file, not as a loaded package, so that what is validated is
 * exactly what is on disk.
 *
 * Each signature's status is one of VALID, VALID_PARTIAL (valid, but some part of the file
 * is not covered), RECOVERABLE (valid, but the certificate is not trusted),
 * RECOVERABLE_PARTIAL, or INVALID: the same distinctions as Office's signature pane.
 */
public class ValidateSignature {

	public static void main(String[] args) throws Exception {

	    String inputfilepath = args.length > 0 ? args[0] : System.getProperty("user.dir") + "/OUT_signed.pptx";

        List<XmlSignaturePart> parts;
		try {
			// Treats every certificate as trusted, so the status reflects the signature alone.
			// To check the certificate too, pass a CertificateTrustChecker to getSignaturesStatus
			// (see IsSignerTrusted); or write your own, if you have particular rules.
			parts = SignatureHelper.getSignaturesStatusTrusted(new File(inputfilepath));
		} catch (SignaturesNotPresentException e) {
			System.out.println("Not signed.");
			return;
		}

        for (XmlSignaturePart sp : parts) {
        	System.out.println(sp.getPartName().getName()
        			+ " signed " + sp.getSigningTime()
        			+ " by " + sp.getSigner().getSubjectX500Principal()
        			+ " ---> " + sp.getSignatureStatus(null));

        	// A partial signature: which parts of the file does it leave out?
        	for (String unsigned : sp.getUnsignedRequiredParts()) {
        		System.out.println("    not covered: " + unsigned);
        	}
        }
	}

}
