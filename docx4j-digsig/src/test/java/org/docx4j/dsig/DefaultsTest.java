package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * What a signature contains when nothing is configured but the key.
 */
public class DefaultsTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/** As Office itself signs (see goldens/minimal.xlsx); the default was SHA-1 until 2026 */
	@Test
	public void sha256Throughout() throws Exception {

		File signed = tmp.newFile("signed.xlsx");
		TestSupport.sign(new File("src/test/resources/corpus/simple.xlsx"), signed, TestSupport.newSigner("DigSig DefaultsTest"));

		assertEquals(algorithms(new File("src/test/resources/goldens/minimal.xlsx")), algorithms(signed));
	}

	/** Office writes the two the same; ours disagreed by the UTC offset until 2026 */
	@Test
	public void xadesSigningTimeEqualsOpcSignatureTime() throws Exception {

		File signed = tmp.newFile("signed.docx");
		TestSupport.sign(new File("src/test/resources/corpus/sign-me-please.docx"), signed, TestSupport.newSigner("DigSig DefaultsTest"));

		Document doc = TestSupport.parse(TestSupport.entries(signed).get(TestSupport.signatureEntries(signed).get(0)));
		String xades = doc.getElementsByTagNameNS("http://uri.etsi.org/01903/v1.3.2#", "SigningTime").item(0).getTextContent();
		String opc = doc.getElementsByTagNameNS(TestSupport.OPC_DSIG, "Value").item(0).getTextContent();
		assertEquals(opc, xades);
		assertTrue(xades, xades.endsWith("Z"));
	}

	private static Set<String> algorithms(File signed) throws Exception {

		Document doc = TestSupport.parse(TestSupport.entries(signed).get(TestSupport.signatureEntries(signed).get(0)));
		Set<String> algorithms = new TreeSet<String>();
		for (String localName : new String[] { "SignatureMethod", "DigestMethod", "CanonicalizationMethod", "Transform" }) {
			NodeList nl = doc.getElementsByTagNameNS(TestSupport.XMLDSIG, localName);
			for (int i = 0; i < nl.getLength(); i++) {
				algorithms.add(localName + " " + ((Element) nl.item(i)).getAttribute("Algorithm"));
			}
		}
		return algorithms;
	}

}
