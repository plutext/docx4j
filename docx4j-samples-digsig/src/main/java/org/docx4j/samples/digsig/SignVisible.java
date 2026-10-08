package org.docx4j.samples.digsig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.docx4j.wml.P;

import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.SignatureHelper;

/**
 * Example of signing a Word document with a signature which is VISIBLE on 
 * the document surface.
 * 
 * To try this you'll need a key (eg a PKCS12 key).  For more details,
 * please see see https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html
 *
 */
public class SignVisible {

	/** The PKCS#12 file and its password: -Dpfx=... -Dpfx.password=... (see README.md) */
    private static char password[] = System.getProperty("pfx.password", "").toCharArray();
	
	/** Validate signature after signing? */
	static boolean alsoValidate = true;

	/** Provide an image of "signature on signature line"?  If not, use text */
	static boolean useSigLnImg = true;
	

	// The image to add
	static File file = new File(System.getProperty("user.dir") + "/greentick.png" );
	
	
	public static void main(String[] args) throws Exception {
		
		// Load the docx
		String inputfilepath = System.getProperty("user.dir") + "/stuff to sign.docx";
		
		// Save signed file
		String outputfilepath = System.getProperty("user.dir") + "/OUT_signed_0.docx";

		
		WordprocessingMLPackage pkg = Docx4J.load(new java.io.File(inputfilepath));
		pkg.getMainDocumentPart().addParagraphOfText("but wait, there's more...");

		// Create SignatureDetail from your certificate 		
        File pfx1 = new File(System.getProperty("pfx", "test.pfx"));
        FileInputStream fis = new FileInputStream(pfx1);
        
        SignatureHelper helper= new SignatureHelper(pkg);
        
        CTSignatureLine sigLine = helper.createCTSignatureLine("the boss", true);
        P p = helper.createDocxSignatureLineP(sigLine, addImage(pkg), null, true);
        pkg.getMainDocumentPart().addObject(p);
        
        SignatureDetail details = helper.configureSignature(fis, password);

        details.setSignatureComments("this is the purpose");

        if (useSigLnImg) {
        	
	        details.setVisualSignature(sigLine, 
	        		IOUtils.toByteArray( 
	        				new java.io.FileInputStream( 
	        						new File(System.getProperty("user.dir") + "/image1.png" ) )),
	        		IOUtils.toByteArray( 
	        				new java.io.FileInputStream(file )));
	        
	        // InvalidSigLnImg will be generated, if you don't pass it.
//	        details.setInvalidSigLnImg(IOUtils.toString( 
//	        		new java.io.FileInputStream(
//	        				new File(System.getProperty("user.dir") + "/png-base64.txt" ) )));
	        
        } else {
        	
	        // or with signature text
	        details.setVisualSignature(sigLine, 
	        		IOUtils.toByteArray( 
	        				new java.io.FileInputStream( 
	        						new File(System.getProperty("user.dir") + "/image1.png" ) )),
	        		"my signature");
        }
        

        // Sign
        helper.sign();
        
        // Save the result
        Docx4J.save(pkg, new File(outputfilepath), Docx4J.FLAG_SAVE_ZIP_FILE);
        
        
        
        
        
        // check the signature is valid?
        if (alsoValidate) {
        	
	        System.out.println("Signed. \n\n Validating.. \n\n");
	
	        // Validate
			InputStream is = new FileInputStream(new java.io.File(outputfilepath));
	        List<XmlSignaturePart> parts = SignatureHelper.getSignaturesStatusTrusted(is);
	        // to check the validity of the certificates, you would need to pass your implementation of CertificateTrustChecker;
	        
	        for (XmlSignaturePart sp : parts) {        
	        	System.out.println(sp.getPartName().getName() + " ---> " + sp.getSignatureStatus(null));
	        }
        }
        
        
	}

	
	public static String addImage(WordprocessingMLPackage wordMLPackage)
			throws Exception {

		// a dummy image
		byte[] bytes = Base64
				.decodeBase64("iVBORw0KGgoAAAANSUhEUgAAAAIAAAACAgMAAAAP2OW3AAAADFBMVEUDAP//AAAA/wb//AAD4Tw1AAAACXBIWXMAAAsTAAALEwEAmpwYAAAADElEQVQI12NwYNgAAAF0APHJnpmVAAAAAElFTkSuQmCC");

		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage
				.createImagePart(wordMLPackage, bytes);

		return imagePart.getRelLast().getId();
	}
	
}
