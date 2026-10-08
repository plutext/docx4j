package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Sign, save, load, remove the signatures, save: nothing of the signature should be left
 * in the zip, and the result should load.  (That Office then opens it without a repair
 * prompt has to be checked by hand.)
 */
@RunWith(Parameterized.class)
public class RemoveSignaturesTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> files() {
		List<Object[]> params = new ArrayList<Object[]>();
		for (String name : new String[] { "sign-me-please.docx", "table.pptx", "comments.xlsx" }) {
			params.add(new Object[] { name });
		}
		return params;
	}

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final File file;

	public RemoveSignaturesTest(String name) {
		this.file = new File("src/test/resources/corpus", name);
	}

	@Test
	public void nothingOfTheSignatureIsLeft() throws Exception {

		File signed = tmp.newFile("signed" + TestSupport.extension(file));
		TestSupport.sign(file, signed, TestSupport.newSigner("DigSig RemoveSignaturesTest"));
		assertEquals(1, TestSupport.signatureEntries(signed).size());

		OpcPackage pkg = OpcPackage.load(signed);
		SignatureHelper helper = new SignatureHelper(pkg);
		assertTrue(helper.isPackageSigned());
		helper.removeSignatureParts(pkg);
		assertFalse(helper.isPackageSigned());

		File removed = tmp.newFile("removed" + TestSupport.extension(file));
		pkg.save(removed);

		Map<String, byte[]> entries = TestSupport.entries(removed);
		for (String name : entries.keySet()) {
			assertFalse(name, name.startsWith("_xmlsignatures"));
		}
		assertFalse(new String(entries.get("_rels/.rels"), StandardCharsets.UTF_8).contains("digital-signature"));

		OpcPackage reloaded = OpcPackage.load(removed);
		SignatureHelper reloadedHelper = new SignatureHelper(reloaded);
		assertFalse(reloadedHelper.isPackageSigned());
		assertEquals(Collections.emptyList(), reloadedHelper.getSignatureParts());
	}

}
