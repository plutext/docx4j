package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * More than one signature in a file.  The Enterprise 8.x manual said "1 or more signatures".
 *
 * These tests record what happens today (CR-002 phase 0), which is not always what one
 * would want: where an expectation is INVALID, that is a known limitation, and the test
 * is there so that a change in it is noticed.
 */
public class MultipleSignaturesTest {

	private static final File CORPUS = new File("src/test/resources/corpus");

	private static KeyStore.PrivateKeyEntry first, second;

	@BeforeClass
	public static void createSigners() throws Exception {
		first = TestSupport.newSigner("DigSig first signer");
		second = TestSupport.newSigner("DigSig second signer");
	}

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	// (a) two signers configured, one sign()

	@Test
	public void twoSignersInOneSign_docx() throws Exception {
		assertEquals(statuses(SignatureStatus.VALID, SignatureStatus.VALID),
				twoSignersInOneSign("sign-me-please.docx"));
	}

	@Test
	public void twoSignersInOneSign_pptx() throws Exception {
		assertEquals(statuses(SignatureStatus.VALID, SignatureStatus.VALID),
				twoSignersInOneSign("table.pptx"));
	}

	@Test
	public void twoSignersInOneSign_xlsx() throws Exception {
		assertEquals(statuses(SignatureStatus.VALID, SignatureStatus.VALID),
				twoSignersInOneSign("comments.xlsx"));
	}

	// (b) a file we signed, loaded, signed again and saved

	/**
	 * Both survive.  Until CR-033 the earlier signature did not, but only because sign()
	 * added a trial notice to the main document part each time, which changed
	 * /word/document.xml; the notice went with the move into docx4j.
	 */
	@Test
	public void signedAgain_docx() throws Exception {
		assertEquals(statuses(SignatureStatus.VALID, SignatureStatus.VALID),
				signedAgain("sign-me-please.docx"));
	}

	@Test
	public void signedAgain_pptx() throws Exception {
		assertEquals(statuses(SignatureStatus.VALID, SignatureStatus.VALID),
				signedAgain("table.pptx"));
	}

	@Test
	public void signedAgain_xlsx() throws Exception {
		assertEquals(statuses(SignatureStatus.VALID, SignatureStatus.VALID),
				signedAgain("comments.xlsx"));
	}

	// (c) a file signed by Office, then signed by us

	/**
	 * Office's signature does not survive: we digest and save what docx4j marshals, and
	 * that is not byte for byte what Office wrote.  Signing the bytes on disk
	 * (CR-002 phase 6) is what would fix this.
	 */
	@Test
	public void officeSignatureInvalidatedWhenWeSignToo() throws Exception {

		for (File golden : TestSupport.goldens()) {
			if (TestSupport.isTampered(golden)) continue;

			File signed = tmp.newFile("signed-" + golden.getName());
			TestSupport.sign(golden, signed, second);

			List<String> officeSignatures = TestSupport.signatureEntries(golden);
			Map<String, SignatureStatus> statuses = TestSupport.statuses(signed);
			assertEquals(golden.getName(), officeSignatures.size() + 1, statuses.size());
			for (Map.Entry<String, SignatureStatus> e : statuses.entrySet()) {
				boolean ours = !officeSignatures.contains(e.getKey().substring(1));
				assertEquals(golden.getName() + e.getKey(),
						ours ? SignatureStatus.VALID : SignatureStatus.INVALID, e.getValue());
			}
		}
	}

	private List<SignatureStatus> twoSignersInOneSign(String name) throws Exception {
		File in = new File(CORPUS, name);
		File signed = tmp.newFile("two" + TestSupport.extension(in));
		TestSupport.sign(in, signed, first, second);
		return inSigningOrder(signed);
	}

	private List<SignatureStatus> signedAgain(String name) throws Exception {
		File in = new File(CORPUS, name);
		File once = tmp.newFile("once" + TestSupport.extension(in));
		TestSupport.sign(in, once, first);
		File twice = tmp.newFile("twice" + TestSupport.extension(in));
		TestSupport.sign(once, twice, second);
		return inSigningOrder(twice);
	}

	/** Signature part names sort in the order the signatures were added (sig1.xml, sig12.xml) */
	private static List<SignatureStatus> inSigningOrder(File signed) throws Exception {
		return new ArrayList<SignatureStatus>(TestSupport.statuses(signed).values());
	}

	private static List<SignatureStatus> statuses(SignatureStatus... statuses) {
		return java.util.Arrays.asList(statuses);
	}

}
