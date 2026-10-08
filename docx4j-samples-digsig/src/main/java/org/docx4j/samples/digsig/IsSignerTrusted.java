package org.docx4j.samples.digsig;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import org.docx4j.openpackaging.parts.digitalsignature.CertificateTrustChecker;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;

import org.docx4j.dsig.DefaultCertificateTrustChecker;
import org.docx4j.dsig.DefaultCertificateTrustChecker.Revocation;
import org.docx4j.dsig.SignatureHelper;

/**
 * Example of validating a signed document (docx, pptx or xlsx), including whether the
 * signer's certificate is trusted.
 *
 * SignatureHelper.getSignaturesStatusTrusted treats every certificate as trusted, so it
 * can only ever say VALID or INVALID on the strength of the signature arithmetic.  To get
 * the result Office would give, pass a CertificateTrustChecker: DefaultCertificateTrustChecker
 * builds a certification path from the signer to a trust anchor, as Office does against the
 * Windows certificate store.
 *
 * Which anchors to trust is your decision: the JDK's cacerts (as here), a key store of your
 * own, the Windows store, or a certificate you add.  To see what the JDK is doing, set the
 * VM argument -Djava.security.debug=certpath
 */
public class IsSignerTrusted {

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/OUT_signed_NEW.docx";

		// Trust what the JDK trusts
		DefaultCertificateTrustChecker checker = new DefaultCertificateTrustChecker();

		// Alternatives:
		// - a key store of your own (on Windows, what Office trusts):
		//     KeyStore ks = KeyStore.getInstance("Windows-ROOT"); ks.load(null, null);
		//     checker = new DefaultCertificateTrustChecker(ks);
		// - plus a certificate of your own, eg a self-signed test certificate:
		//     checker.addTrustAnchor(certificate);
		// - no network: checker.setRevocation(Revocation.NONE);
		// - judge the certificate's validity now rather than at the signing time:
		//     checker.setValidityCheckedAt(ValidityCheckedAt.NOW);

		for (XmlSignaturePart sp : SignatureHelper.getSignaturesStatus(new File(inputfilepath), checker)) {

			X509Certificate signer = sp.getSigner();
			System.out.println(sp.getPartName().getName() + ": signed " + sp.getSigningTime()
					+ " by " + signer.getSubjectX500Principal()
					+ " (issued by " + signer.getIssuerX500Principal() + ")");

			// VALID, VALID_PARTIAL, RECOVERABLE, RECOVERABLE_PARTIAL or INVALID, as in Office's signature pane
			System.out.println("  ---> " + sp.getSignatureStatus(null));
		}
	}

}
