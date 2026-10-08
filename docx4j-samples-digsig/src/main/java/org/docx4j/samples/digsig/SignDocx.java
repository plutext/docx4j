package org.docx4j.samples.digsig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;

import org.docx4j.dsig.crypt.CommitmentType;
import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.SignatureHelper;
import org.docx4j.org.etsi.uri.x01903.v13.SignatureProductionPlaceType;

/**
 * Example of signing a Word document.
 * 
 * To try this you'll need a key (eg a PKCS12 key).  For more details,
 * please see see https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html
 *
 */
public class SignDocx {

	/** The PKCS#12 file and its password: -Dpfx=... -Dpfx.password=... (see README.md) */
    private static char password[] = System.getProperty("pfx.password", "").toCharArray();
	
	/** Validate signature after signing? */
	static boolean alsoValidate = true;

	public static void main(String[] args) throws Exception {
		
		// Load the docx
		String inputfilepath = System.getProperty("user.dir") + "/sign me please.docx";
		
		// Save signed file
		String outputfilepath = System.getProperty("user.dir") + "/OUT_signed_NEW.docx";
		
		
		WordprocessingMLPackage pkg = Docx4J.load(new java.io.File(inputfilepath));
		pkg.getMainDocumentPart().addParagraphOfText("but wait, there's more...");

		
		// Create SignatureDetail from your certificate 
//        File pfx1 = new File(System.getProperty("pfx", "test.pfx"));
        File pfx1 = new File(System.getProperty("pfx", "test.pfx"));
        FileInputStream fis = new FileInputStream(pfx1);
        
        SignatureHelper signing= new SignatureHelper(pkg);
        SignatureDetail details = signing.configureSignature(fis, password);

        {
	        // OPTIONAL
	        details.setSignatureComments("my comment");
	        
	        // OPTIONAL        
	        details.setXadesRole("my role");
	        
	        // OPTIONAL
	        SignatureProductionPlaceType signatureProductionPlace = new SignatureProductionPlaceType();
	        signatureProductionPlace.setCity("Sydney");
	        signatureProductionPlace.setStateOrProvince("NSW");
	        signatureProductionPlace.setPostalCode("3000");
	        signatureProductionPlace.setCountryName("Australia");
	        details.setSignatureProductionPlace(signatureProductionPlace);        
	        
	        // OPTIONAL
	        details.setCommitmentType(CommitmentType.CREATED);
        }

        // Sign the docx
        signing.sign();
        
        // Save the result
        Docx4J.save(pkg, new File(outputfilepath), Docx4J.FLAG_SAVE_ZIP_FILE);
        
        
        
        
        
        // check the signature is valid?
        if (alsoValidate) {
        	
	        System.out.println("Signed. \n\n Validating.. \n\n");
	
	        System.out.println(
	        		signing.isPackageSigned()
	        		);
	        
	        // Validate
			InputStream is = new FileInputStream(new java.io.File(outputfilepath));
	        List<XmlSignaturePart> parts = SignatureHelper.getSignaturesStatusTrusted(is);
	        // to check the validity of the certificates, you would need to pass your implementation of CertificateTrustChecker;
	        
	        for (XmlSignaturePart sp : parts) {        
	        	System.out.println(sp.getPartName().getName() + " ---> " + sp.getSignatureStatus(null));
	        }
        }
        
	}

}
