package org.docx4j.dsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.function.Consumer;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.parts.digitalsignature.SignatureStatus;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validation runs with Santuario's secure validation off while unmarshalling (its limit of
 * 30 references per Manifest can't be raised), so the checks it would have made are ours.
 * Each test edits the signature part of a signed file and expects validation to refuse it,
 * for the reason given, rather than merely find the signature invalid.
 */
public class HostileSignatureTest {

	private static final String MAX_REFERENCES = "docx4j.dsig.validation.maxReferences";

	private static final String C14N = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

	@ClassRule
	public static TemporaryFolder tmp = new TemporaryFolder();

	private static File signed;
	private static String signatureEntry;

	@BeforeClass
	public static void signOne() throws Exception {
		signed = tmp.newFile("signed.xlsx");
		TestSupport.sign(new File("src/test/resources/corpus/comments.xlsx"), signed,
				TestSupport.newSigner("DigSig HostileSignatureTest"));
		signatureEntry = TestSupport.signatureEntries(signed).get(0);
	}

	@Test
	public void untouchedSignatureIsValid() throws Exception {
		assertEquals(SignatureStatus.VALID, TestSupport.statuses(signed).get("/" + signatureEntry));
	}

	@Test
	public void md5DigestRefused() throws Exception {
		assertRefused("DigestMethod http://www.w3.org/2001/04/xmldsig-more#md5 is not allowed", doc ->
			first(doc, "DigestMethod").setAttribute("Algorithm", "http://www.w3.org/2001/04/xmldsig-more#md5"));
	}

	@Test
	public void md5SignatureMethodRefused() throws Exception {
		assertRefused("SignatureMethod http://www.w3.org/2001/04/xmldsig-more#rsa-md5 is not allowed", doc ->
			first(doc, "SignatureMethod").setAttribute("Algorithm", "http://www.w3.org/2001/04/xmldsig-more#rsa-md5"));
	}

	@Test
	public void hmacSignatureMethodRefused() throws Exception {
		assertRefused("SignatureMethod http://www.w3.org/2000/09/xmldsig#hmac-sha1 is not allowed", doc ->
			first(doc, "SignatureMethod").setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#hmac-sha1"));
	}

	@Test
	public void xsltTransformRefused() throws Exception {
		assertRefused("Transform http://www.w3.org/TR/1999/REC-xslt-19991116 is not allowed", doc ->
			first(doc, "Transform").setAttribute("Algorithm", "http://www.w3.org/TR/1999/REC-xslt-19991116"));
	}

	@Test
	public void thirdTransformRefused() throws Exception {
		assertRefused("More than 2 transforms", doc -> {
			Element transforms = first(doc, "Transforms");
			while (transforms.getElementsByTagNameNS(TestSupport.XMLDSIG, "Transform").getLength() < 3) {
				Element extra = doc.createElementNS(TestSupport.XMLDSIG, "Transform");
				extra.setAttribute("Algorithm", C14N);
				transforms.appendChild(extra);
			}
		});
	}

	@Test
	public void retrievalMethodRefused() throws Exception {
		assertRefused("RetrievalMethod is not allowed", doc -> {
			Element retrievalMethod = doc.createElementNS(TestSupport.XMLDSIG, "RetrievalMethod");
			retrievalMethod.setAttribute("URI", "#idPackageObject");
			first(doc, "KeyInfo").appendChild(retrievalMethod);
		});
	}

	@Test
	public void duplicateIdRefused() throws Exception {
		assertRefused("Duplicate Id 'idPackageObject'", doc -> {
			Element object = doc.createElementNS(TestSupport.XMLDSIG, "Object");
			object.setAttribute("Id", "idPackageObject");
			doc.getDocumentElement().appendChild(object);
		});
	}

	@Test
	public void referenceOutsidePackageRefused() throws Exception {
		assertRefused("Reference URI 'http://example.com/part.xml' is neither", doc -> {
			Element manifest = first(doc, "Manifest");
			((Element) manifest.getElementsByTagNameNS(TestSupport.XMLDSIG, "Reference").item(0))
					.setAttribute("URI", "http://example.com/part.xml");
		});
	}

	@Test
	public void tooManyReferencesRefused() throws Exception {
		Docx4jProperties.setProperty(MAX_REFERENCES, "5");
		try {
			assertRefused("at most 5 are allowed", doc -> {});
		} finally {
			Docx4jProperties.getProperties().remove(MAX_REFERENCES);
		}
	}

	private static Element first(Document doc, String localName) {
		return (Element) doc.getElementsByTagNameNS(TestSupport.XMLDSIG, localName).item(0);
	}

	private void assertRefused(String reason, Consumer<Document> edit) throws Exception {

		File hostile = tmp.newFile();
		TestSupport.rewrite(signed, hostile, signatureEntry, bytes -> {
			try {
				Document doc = TestSupport.parse(bytes);
				edit.accept(doc);
				return TestSupport.serialize(doc);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		try {
			SignatureStatus status = TestSupport.statuses(hostile).get("/" + signatureEntry);
			fail("Expected to be refused (" + reason + ") but got " + status);
		} catch (DigitalSignatureException e) {
			assertTrue(e.getMessage(), e.getMessage().contains(reason));
		}
	}

}
