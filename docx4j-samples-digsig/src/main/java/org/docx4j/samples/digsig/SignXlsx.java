package org.docx4j.samples.digsig;

import java.io.File;
import java.io.FileInputStream;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;

import org.docx4j.dsig.crypt.CommitmentType;
import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.SignatureHelper;
import org.docx4j.org.etsi.uri.x01903.v13.SignatureProductionPlaceType;

/**
 * Example of signing an Excel workbook, invisibly (no signature line).
 *
 * Signing is the same for docx, pptx and xlsx: SignatureHelper takes any package.  The
 * signature covers every part of the file (sheets, shared strings, styles, charts, drawings,
 * comments, pivot caches, a VBA project in an xlsm), as Excel's own signature does; Excel
 * reports it as valid and complete.
 *
 * For a visible signature, on a signature line in a sheet, see SignVisibleXlsx.
 *
 * To try this you'll need a key (eg a PKCS12 key).  For more details,
 * please see https://docs.oracle.com/javase/8/docs/api/java/security/KeyStore.html
 */
public class SignXlsx {

	/** The PKCS#12 file and its password: -Dpfx=... -Dpfx.password=... (see README.md) */
    private static char password[] = System.getProperty("pfx.password", "").toCharArray();

	/** Validate signature after signing? */
	static boolean alsoValidate = true;

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sign me please.xlsx";
		String outputfilepath = System.getProperty("user.dir") + "/OUT_signed.xlsx";

		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(new File(inputfilepath));

		// Any change you want to make, make before signing; the signature covers what is saved.
		// For example: pkg.getWorkbookPart().getWorksheet(0) ...

		// Create SignatureDetail from your certificate
        FileInputStream fis = new FileInputStream(new File(System.getProperty("pfx", "test.pfx")));
        SignatureHelper signing = new SignatureHelper(pkg);
        SignatureDetail details = signing.configureSignature(fis, password);

        // OPTIONAL: what Excel shows as the purpose for signing, and the XAdES signer details
        details.setSignatureComments("my comment");
        details.setXadesRole("my role");
        SignatureProductionPlaceType signatureProductionPlace = new SignatureProductionPlaceType();
        signatureProductionPlace.setCity("Sydney");
        signatureProductionPlace.setStateOrProvince("NSW");
        signatureProductionPlace.setPostalCode("2000");
        signatureProductionPlace.setCountryName("Australia");
        details.setSignatureProductionPlace(signatureProductionPlace);
        details.setCommitmentType(CommitmentType.CREATED);

        // OPTIONAL: the digest.  SHA-256 is the default, as in current Office;
        // signing.getSignatureConfig().setDigestAlgo(HashAlgorithm.sha512) for another.

        // Sign
        signing.sign();

        // Save the result
        pkg.save(new File(outputfilepath));

        // Check the signature is valid?
        if (alsoValidate) {

	        System.out.println("Signed. \n\n Validating.. \n\n");

	        // getSignaturesStatusTrusted treats every certificate as trusted; to get the result
	        // Office would give, pass a CertificateTrustChecker to getSignaturesStatus instead
	        // (see IsSignerTrusted)
	        for (XmlSignaturePart sp : SignatureHelper.getSignaturesStatusTrusted(new File(outputfilepath))) {
	        	System.out.println(sp.getPartName().getName() + " ---> " + sp.getSignatureStatus(null));
	        }
        }
	}

}
