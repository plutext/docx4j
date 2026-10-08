package org.docx4j.samples.digsig;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.docx4j.vml.officedrawing.CTSignatureLine;

import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.SignatureHelper;

/**
 * Example of signing an Excel workbook with a visible signature (a signature line).
 * 
 * If the worksheet already has a signature line (Insert > Signature Line in Excel), 
 * that line is signed; otherwise one is added, then signed.
 * 
 * To try this you'll need a key (eg a PKCS12 key), and two PNG images:
 * what the sheet should show for a line awaiting signature, and what it should show 
 * once signed (the line with the signature on it).  Excel itself uses EMF images, 
 * but accepts PNG. 
 * 
 * PowerPoint has no signature lines.
 */
public class SignVisibleXlsx {

	/** The PKCS#12 file and its password: -Dpfx=... -Dpfx.password=... (see README.md) */
    private static char password[] = System.getProperty("pfx.password", "").toCharArray();
	
	/** Validate signature after signing? */
	static boolean alsoValidate = true;
	
	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/workbook to sign.xlsx";
		String outputfilepath = System.getProperty("user.dir") + "/OUT_signed_visible.xlsx";

		byte[] unsignedLineImage = FileUtils.readFileToByteArray(new File(System.getProperty("user.dir") + "/signature-line.png"));
		byte[] signedLineImage = FileUtils.readFileToByteArray(new File(System.getProperty("user.dir") + "/signature-line-signed.png"));

		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(new File(inputfilepath));
		WorksheetPart sheet = pkg.getWorkbookPart().getWorksheet(0);
		
        SignatureHelper helper = new SignatureHelper(pkg);
        
        // Use the sheet's signature line if it has one, or add one
        CTSignatureLine sigLine;
        List<CTSignatureLine> existing = helper.getXlsxSignatureLines(sheet);
        if (existing.isEmpty()) {
        	
            sigLine = helper.createCTSignatureLine("the boss", true);
            
            // Where it goes: from column, offset, row, offset; to column, offset, row, offset.
            // Here, columns B to E, rows 5 to 11.
            helper.createXlsxSignatureLine(sheet, sigLine, unsignedLineImage, "1, 0, 4, 0, 5, 0, 11, 0", null);
            
        } else {
        	sigLine = existing.get(0);
        }

		// Create SignatureDetail from your certificate 		
        SignatureDetail details = helper.configureSignature(
        		new FileInputStream(new File(System.getProperty("pfx", "test.pfx"))), password);

        details.setSignatureComments("this is the purpose");

        // Associate the signature with the line, giving the signature as text
        // (to give it as an image instead, pass the image's bytes as the last argument)
        details.setVisualSignature(sigLine, signedLineImage, "my signature");
        
        // InvalidSigLnImg will be generated from signedLineImage, if you don't set it.

        // Sign
        helper.sign();
        
        // Save the result
        pkg.save(new File(outputfilepath));
        
        // check the signature is valid?
        if (alsoValidate) {
        	
	        // to check the validity of the certificate, pass your implementation of 
        	// CertificateTrustChecker to getSignaturesStatus
	        for (XmlSignaturePart sp : SignatureHelper.getSignaturesStatusTrusted(new File(outputfilepath))) {        
	        	System.out.println(sp.getPartName().getName() + " ---> " + sp.getSignatureStatus(null));
	        }
        }
	}

}
