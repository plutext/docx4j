package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Files signed by Office (src/test/resources/goldens).  Each must validate, and
 * each one altered after signing (TAMPERED in its name) must not.
 *
 * The coverage test is the one which predicts whether Office would call our
 * signature "partial": we must sign the parts and relationships Office signed.
 */
@RunWith(Parameterized.class)
public class GoldensTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> files() {
		List<Object[]> params = new ArrayList<Object[]>();
		for (File f : TestSupport.goldens()) {
			params.add(new Object[] { f.getName(), f });
		}
		return params;
	}

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final File golden;

	public GoldensTest(String name, File golden) {
		this.golden = golden;
	}

	@Test
	public void officeSignatureValidates() throws Exception {

		for (XmlSignaturePart sp : SignatureHelper.getSignaturesStatusTrusted(golden)) {
			if (TestSupport.isTampered(golden)) {
				assertEquals(SignatureStatus.INVALID, sp.getSignatureStatus(null));
			} else {
				assertEquals(SignatureStatus.VALID, sp.getSignatureStatus(null));
				assertEquals(Collections.emptyList(), sp.getUnsignedRequiredParts());
			}
		}
	}

	@Test
	public void weSignWhatOfficeSigned() throws Exception {

		assumeFalse(TestSupport.isTampered(golden));

		// The golden without its signatures stands in for the file Office was given
		File unsigned = tmp.newFile("unsigned" + TestSupport.extension(golden));
		OpcPackage pkg = OpcPackage.load(golden);
		new SignatureHelper(pkg).removeSignatureParts(pkg);
		pkg.save(unsigned);

		File signed = tmp.newFile("signed" + TestSupport.extension(golden));
		KeyStore.PrivateKeyEntry signer = TestSupport.newSigner("DigSig GoldensTest");
		TestSupport.sign(unsigned, signed, signer);

		assertEquals(
				TestSupport.signedParts(golden, TestSupport.signatureEntries(golden).get(0)),
				TestSupport.signedParts(signed, TestSupport.signatureEntries(signed).get(0)));
	}

}
