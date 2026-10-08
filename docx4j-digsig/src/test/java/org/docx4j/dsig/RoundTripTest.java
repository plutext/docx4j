package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * For each corpus file: sign, save, and validate what was saved through the public API;
 * then alter a signed part and expect the signature to be invalid.
 */
@RunWith(Parameterized.class)
public class RoundTripTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> files() {
		List<Object[]> params = new ArrayList<Object[]>();
		for (File f : TestSupport.corpus()) {
			params.add(new Object[] { f.getName(), f });
		}
		return params;
	}

	private static KeyStore.PrivateKeyEntry signer;

	@BeforeClass
	public static void createSigner() throws Exception {
		signer = TestSupport.newSigner("DigSig RoundTripTest");
	}

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final File file;

	public RoundTripTest(String name, File file) {
		this.file = file;
	}

	@Test
	public void signedFileIsValidAndComplete() throws Exception {

		File signed = tmp.newFile("signed" + TestSupport.extension(file));
		TestSupport.sign(file, signed, signer);

		List<XmlSignaturePart> signatures = SignatureHelper.getSignaturesStatusTrusted(signed);
		assertEquals(1, signatures.size());
		assertEquals(SignatureStatus.VALID, signatures.get(0).getSignatureStatus(null));
		assertEquals(Collections.emptyList(), signatures.get(0).getUnsignedRequiredParts());
	}

	@Test
	public void alteredPartInvalidatesSignature() throws Exception {

		File signed = tmp.newFile("signed" + TestSupport.extension(file));
		TestSupport.sign(file, signed, signer);

		// Still well-formed, and the same after canonicalisation; but parts are digested as bytes
		String part = TestSupport.aSignedXmlPart(signed);
		File altered = tmp.newFile("altered" + TestSupport.extension(file));
		TestSupport.rewrite(signed, altered, part, bytes -> {
			byte[] longer = java.util.Arrays.copyOf(bytes, bytes.length + 1);
			longer[bytes.length] = '\n';
			return longer;
		});

		List<XmlSignaturePart> signatures = SignatureHelper.getSignaturesStatusTrusted(altered);
		assertEquals(part, SignatureStatus.INVALID, signatures.get(0).getSignatureStatus(null));
	}

	@Test
	public void alteredRelationshipInvalidatesSignature() throws Exception {

		File signed = tmp.newFile("signed" + TestSupport.extension(file));
		TestSupport.sign(file, signed, signer);

		// A rels part is digested after the relationship transform and C14N, which a trailing 
		// newline would survive.  So change a Target, to one which still resolves to the same part.
		String rels = null;
		for (String uri : TestSupport.signedParts(signed, TestSupport.signatureEntries(signed).get(0)).keySet()) {
			if (uri.startsWith("/_rels/") || !uri.contains(".rels?")) continue;
			rels = uri.substring(1, uri.indexOf('?'));
			break;
		}
		assertTrue("a signed rels part other than the package's", rels != null);

		File altered = tmp.newFile("altered" + TestSupport.extension(file));
		TestSupport.rewrite(signed, altered, rels, bytes -> {
			String xml = new String(bytes, StandardCharsets.UTF_8);
			int target = xml.indexOf("Target=\"") + "Target=\"".length();
			return (xml.substring(0, target) + "./" + xml.substring(target)).getBytes(StandardCharsets.UTF_8);
		});

		List<XmlSignaturePart> signatures = SignatureHelper.getSignaturesStatusTrusted(altered);
		assertEquals(rels, SignatureStatus.INVALID, signatures.get(0).getSignatureStatus(null));
	}

}
