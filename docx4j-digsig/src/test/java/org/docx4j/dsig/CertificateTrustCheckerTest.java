package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.docx4j.openpackaging.parts.digitalsignature.CertificateTrustCheckResult;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.docx4j.dsig.DefaultCertificateTrustChecker.Revocation;
import org.docx4j.dsig.DefaultCertificateTrustChecker.ValidityCheckedAt;

/**
 * DefaultCertificateTrustChecker, against a certificate authority made for the run: a root,
 * an intermediate, and leaves.  Revocation is NONE throughout, so nothing goes to the network.
 */
public class CertificateTrustCheckerTest {

	private static final long DAY = 86400000L;
	private static final File CORPUS = new File("src/test/resources/corpus");

	private static KeyPair rootKey, intermediateKey, leafKey, oldLeafKey;
	private static X509Certificate root, intermediate, leaf, oldLeaf;

	@BeforeClass
	public static void makeCertificateAuthority() throws Exception {

		long now = System.currentTimeMillis();

		rootKey = keyPair();
		root = certificate("DigSig Test Root CA", rootKey.getPublic(), "DigSig Test Root CA", rootKey,
				new Date(now - 3 * 365 * DAY), new Date(now + 10 * 365 * DAY), true);

		intermediateKey = keyPair();
		intermediate = certificate("DigSig Test Intermediate CA", intermediateKey.getPublic(), "DigSig Test Root CA", rootKey,
				new Date(now - 3 * 365 * DAY), new Date(now + 10 * 365 * DAY), true);

		leafKey = keyPair();
		leaf = certificate("DigSig Test Signer", leafKey.getPublic(), "DigSig Test Intermediate CA", intermediateKey,
				new Date(now - DAY), new Date(now + 365 * DAY), false);

		// valid for a year which ended a year ago
		oldLeafKey = keyPair();
		oldLeaf = certificate("DigSig Test Former Signer", oldLeafKey.getPublic(), "DigSig Test Intermediate CA", intermediateKey,
				new Date(now - 2 * 365 * DAY), new Date(now - 365 * DAY), false);
	}

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	// ---- the checker on its own

	@Test
	public void pathToAnchorThroughIntermediateInChain() throws Exception {
		assertEquals(CertificateTrustCheckResult.TRUSTED,
				trusting(root).isTrusted(Arrays.asList(leaf, intermediate), new Date()));
	}

	@Test
	public void intermediateMissingFromChain() throws Exception {
		assertEquals(CertificateTrustCheckResult.UNTRUSTED,
				trusting(root).isTrusted(Collections.singletonList(leaf), new Date()));
	}

	@Test
	public void intermediateAsAnchorIsEnough() throws Exception {
		assertEquals(CertificateTrustCheckResult.TRUSTED,
				trusting(intermediate).isTrusted(Collections.singletonList(leaf), new Date()));
	}

	@Test
	public void rootNotAnAnchor() throws Exception {
		X509Certificate otherRoot = certificate("Some Other Root CA", keyPair().getPublic(), "Some Other Root CA", keyPair(),
				new Date(0), new Date(System.currentTimeMillis() + 365 * DAY), true);
		assertEquals(CertificateTrustCheckResult.UNTRUSTED,
				trusting(otherRoot).isTrusted(Arrays.asList(leaf, intermediate), new Date()));
	}

	@Test
	public void noAnchorsAtAll() throws Exception {
		assertEquals(CertificateTrustCheckResult.UNTRUSTED,
				new DefaultCertificateTrustChecker(new HashSet<TrustAnchor>()).setRevocation(Revocation.NONE)
					.isTrusted(Arrays.asList(leaf, intermediate), new Date()));
	}

	@Test
	public void expiredSinceSigning_trustedAtSigningTime() throws Exception {
		Date whenSigned = new Date(System.currentTimeMillis() - 18 * 30 * DAY);
		assertEquals(CertificateTrustCheckResult.TRUSTED,
				trusting(root).isTrusted(Arrays.asList(oldLeaf, intermediate), whenSigned));
	}

	@Test
	public void expiredSinceSigning_hardFailNow() throws Exception {
		Date whenSigned = new Date(System.currentTimeMillis() - 18 * 30 * DAY);
		assertEquals(CertificateTrustCheckResult.HARD_FAIL,
				trusting(root).setValidityCheckedAt(ValidityCheckedAt.NOW).isTrusted(Arrays.asList(oldLeaf, intermediate), whenSigned));
	}

	@Test
	public void signedAfterExpiry() throws Exception {
		Date whenSigned = new Date(System.currentTimeMillis() - 30 * DAY);
		assertEquals(CertificateTrustCheckResult.HARD_FAIL,
				trusting(root).isTrusted(Arrays.asList(oldLeaf, intermediate), whenSigned));
	}

	@Test
	public void signedBeforeValid() throws Exception {
		Date whenSigned = new Date(System.currentTimeMillis() - 30 * DAY);
		assertEquals(CertificateTrustCheckResult.HARD_FAIL,
				trusting(root).isTrusted(Arrays.asList(leaf, intermediate), whenSigned));
	}

	@Test
	public void noSigningTimeMeansNow() throws Exception {
		assertEquals(CertificateTrustCheckResult.HARD_FAIL,
				trusting(root).isTrusted(Arrays.asList(oldLeaf, intermediate), null));
		assertEquals(CertificateTrustCheckResult.TRUSTED,
				trusting(root).isTrusted(Arrays.asList(leaf, intermediate), null));
	}

	@Test
	public void selfSigned() throws Exception {
		X509Certificate selfSigned = (X509Certificate) TestSupport.newSigner("DigSig self-signed").getCertificate();
		assertEquals(CertificateTrustCheckResult.SELF_SIGNED,
				trusting(root).isTrusted(Collections.singletonList(selfSigned), new Date()));
	}

	@Test
	public void selfSignedButAnchored() throws Exception {
		X509Certificate selfSigned = (X509Certificate) TestSupport.newSigner("DigSig self-signed").getCertificate();
		assertEquals(CertificateTrustCheckResult.TRUSTED,
				trusting(root).addTrustAnchor(selfSigned).isTrusted(Collections.singletonList(selfSigned), new Date()));
	}

	@Test
	public void anchorsFromKeyStore() throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null, null);
		ks.setCertificateEntry("root", root);
		DefaultCertificateTrustChecker checker = new DefaultCertificateTrustChecker(ks).setRevocation(Revocation.NONE);
		assertEquals(1, checker.getTrustAnchors().size());
		assertEquals(CertificateTrustCheckResult.TRUSTED, checker.isTrusted(Arrays.asList(leaf, intermediate), new Date()));
	}

	@Test
	public void jdkCacertsLoad() throws Exception {
		DefaultCertificateTrustChecker checker = new DefaultCertificateTrustChecker().setRevocation(Revocation.NONE);
		assertTrue("the JDK trusts some roots", checker.getTrustAnchors().size() > 10);
		// our CA is not among them
		assertEquals(CertificateTrustCheckResult.UNTRUSTED, checker.isTrusted(Arrays.asList(leaf, intermediate), new Date()));
	}

	@Test
	public void oldSingleArgumentMethodJudgesNow() throws Exception {
		assertEquals(CertificateTrustCheckResult.HARD_FAIL, trusting(root).isTrusted(oldLeaf));
		assertEquals(CertificateTrustCheckResult.UNTRUSTED, trusting(root).isTrusted(leaf)); // no intermediate
	}

	// ---- through a signed file and the public API

	@Test
	public void signedFileIsValidWhenRootTrusted() throws Exception {

		File signed = tmp.newFile("signed.xlsx");
		TestSupport.sign(new File(CORPUS, "simple.xlsx"), signed,
				new KeyStore.PrivateKeyEntry(leafKey.getPrivate(), new Certificate[] { leaf, intermediate }));

		List<XmlSignaturePart> parts = SignatureHelper.getSignaturesStatus(signed, trusting(root));
		assertEquals(SignatureStatus.VALID, parts.get(0).getSignatureStatus(null));

		// what the signature carries, and what the checker was told
		assertEquals(Arrays.asList(leaf, intermediate), parts.get(0).getSignerChain());
		Date signingTime = parts.get(0).getSigningTime();
		assertNotNull(signingTime);
		assertTrue(Math.abs(signingTime.getTime() - System.currentTimeMillis()) < 60000);

		// the same file, with nothing trusted
		parts = SignatureHelper.getSignaturesStatus(signed, new DefaultCertificateTrustChecker(new HashSet<TrustAnchor>()).setRevocation(Revocation.NONE));
		assertEquals(SignatureStatus.RECOVERABLE, parts.get(0).getSignatureStatus(null));
	}

	@Test
	public void selfSignedFileIsRecoverableUntilTrusted() throws Exception {

		KeyStore.PrivateKeyEntry signer = TestSupport.newSigner("DigSig self-signed");
		File signed = tmp.newFile("signed.pptx");
		TestSupport.sign(new File(CORPUS, "table.pptx"), signed, signer);

		assertEquals(SignatureStatus.RECOVERABLE,
				SignatureHelper.getSignaturesStatus(signed, trusting(root)).get(0).getSignatureStatus(null));
		assertEquals(SignatureStatus.VALID,
				SignatureHelper.getSignaturesStatus(signed, trusting(root).addTrustAnchor((X509Certificate) signer.getCertificate())).get(0).getSignatureStatus(null));
	}

	/** Recorded, not asserted: depends on what the JDK's cacerts holds and on the chains those certificates carry */
	@Test
	public void officeGoldensAgainstJdkCacerts() throws Exception {
		DefaultCertificateTrustChecker checker = new DefaultCertificateTrustChecker().setRevocation(Revocation.NONE);
		for (File golden : TestSupport.goldens()) {
			for (XmlSignaturePart sp : SignatureHelper.getSignaturesStatus(golden, checker)) {
				System.out.println("CERT " + golden.getName() + " signed " + sp.getSigningTime() + " by "
						+ sp.getSigner().getSubjectX500Principal() + " -> " + sp.getSignatureStatus(null));
			}
		}
	}

	// ---- making certificates

	private static DefaultCertificateTrustChecker trusting(X509Certificate anchor) {
		return new DefaultCertificateTrustChecker(Collections.singleton(new TrustAnchor(anchor, null))).setRevocation(Revocation.NONE);
	}

	private static KeyPair keyPair() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		return kpg.generateKeyPair();
	}

	private static X509Certificate certificate(String subject, java.security.PublicKey subjectKey, String issuer, KeyPair issuerKey,
			Date notBefore, Date notAfter, boolean ca) throws Exception {

		JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
				new X500Name("CN=" + issuer), BigInteger.valueOf(System.nanoTime()), notBefore, notAfter, new X500Name("CN=" + subject), subjectKey);
		builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(ca));
		builder.addExtension(Extension.keyUsage, true,
				new KeyUsage(ca ? KeyUsage.keyCertSign | KeyUsage.cRLSign : KeyUsage.digitalSignature | KeyUsage.nonRepudiation));

		return new JcaX509CertificateConverter().getCertificate(
				builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey.getPrivate())));
	}

}
