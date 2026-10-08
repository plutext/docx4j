/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.openpackaging.parts.digitalsignature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.crypto.dsig.XMLSignature;

import org.docx4j.Docx4jProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.docx4j.dsig.DigitalSignatureException;

/**
 * Checks made on a signature part before Santuario unmarshals it.
 *
 * Santuario 3 makes these sorts of checks itself in "secure validation" mode, but that
 * mode also limits a Manifest to 30 references, which is fewer than the parts in the
 * simplest pptx. So XmlSignaturePart unmarshals with secure validation off, and what
 * Santuario would have checked while unmarshalling is checked here instead, against
 * what an OOXML package signature actually contains.
 *
 * @since 17.2.1
 */
class SignatureStructureCheck {

	/** docx4j property: the most ds:Reference elements a signature part may contain */
	static final String MAX_REFERENCES_PROPERTY = "docx4j.dsig.validation.maxReferences";

	static final int MAX_REFERENCES_DEFAULT = 10000;

	/** An OOXML signature uses the relationship transform followed by C14N, or C14N alone */
	static final int MAX_TRANSFORMS = 2;

	private static final String XMLDSIG = XMLSignature.XMLNS;
	private static final String XMLDSIG_MORE = "http://www.w3.org/2001/04/xmldsig-more#";
	private static final String XMLENC = "http://www.w3.org/2001/04/xmlenc#";

	private static final Set<String> TRANSFORMS = new HashSet<String>(Arrays.asList(
			"http://schemas.openxmlformats.org/package/2006/RelationshipTransform",
			"http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
			"http://www.w3.org/2001/10/xml-exc-c14n#"));

	/** What SignatureConfig can sign with; so no MD5 */
	private static final Set<String> DIGEST_METHODS = new HashSet<String>(Arrays.asList(
			XMLDSIG + "sha1",
			XMLDSIG_MORE + "sha224",
			XMLENC + "sha256",
			XMLDSIG_MORE + "sha384",
			XMLENC + "sha512",
			XMLENC + "ripemd160"));

	/**
	 * The RSA methods SignatureConfig can sign with, plus their ECDSA equivalents
	 * (we can't sign with an EC key, but Office can).  No MD5, no HMAC.
	 */
	private static final Set<String> SIGNATURE_METHODS = new HashSet<String>(Arrays.asList(
			XMLDSIG + "rsa-sha1",
			XMLDSIG_MORE + "rsa-sha224",
			XMLDSIG_MORE + "rsa-sha256",
			XMLDSIG_MORE + "rsa-sha384",
			XMLDSIG_MORE + "rsa-sha512",
			XMLDSIG_MORE + "rsa-ripemd160",
			XMLDSIG_MORE + "ecdsa-sha1",
			XMLDSIG_MORE + "ecdsa-sha224",
			XMLDSIG_MORE + "ecdsa-sha256",
			XMLDSIG_MORE + "ecdsa-sha384",
			XMLDSIG_MORE + "ecdsa-sha512"));

	/**
	 * @throws DigitalSignatureException if the signature contains something an OOXML package
	 * signature has no reason to contain
	 */
	static void check(Document doc) throws DigitalSignatureException {

		if (doc.getElementsByTagNameNS(XMLDSIG, "RetrievalMethod").getLength()>0) {
			throw new DigitalSignatureException("RetrievalMethod is not allowed");
		}

		checkAlgorithms(doc, "SignatureMethod", SIGNATURE_METHODS);
		checkAlgorithms(doc, "DigestMethod", DIGEST_METHODS);

		NodeList references = doc.getElementsByTagNameNS(XMLDSIG, "Reference");
		int maxReferences = Docx4jProperties.getProperty(MAX_REFERENCES_PROPERTY, MAX_REFERENCES_DEFAULT);
		if (references.getLength()>maxReferences) {
			throw new DigitalSignatureException(references.getLength() + " references; at most " + maxReferences
					+ " are allowed (docx4j property " + MAX_REFERENCES_PROPERTY + ")");
		}
		for (int i=0; i<references.getLength(); i++) {
			checkReference((Element)references.item(i));
		}

		checkIdsUnique(doc.getDocumentElement(), new HashSet<String>());
	}

	private static void checkAlgorithms(Document doc, String localName, Set<String> allowed) throws DigitalSignatureException {

		NodeList nl = doc.getElementsByTagNameNS(XMLDSIG, localName);
		for (int i=0; i<nl.getLength(); i++) {
			String algorithm = ((Element)nl.item(i)).getAttribute("Algorithm");
			if (!allowed.contains(algorithm)) {
				throw new DigitalSignatureException(localName + " " + algorithm + " is not allowed");
			}
		}
	}

	private static void checkReference(Element reference) throws DigitalSignatureException {

		// Same-document (an Object in this signature), or a part in the package
		String uri = reference.getAttribute("URI");
		if (!uri.startsWith("#") && !uri.startsWith("/")) {
			throw new DigitalSignatureException("Reference URI '" + uri + "' is neither in the signature nor a part name");
		}

		int transforms = 0;
		for (Node child = reference.getFirstChild(); child!=null; child = child.getNextSibling()) {
			if (!isDSig(child, "Transforms")) continue;
			for (Node t = child.getFirstChild(); t!=null; t = t.getNextSibling()) {
				if (!isDSig(t, "Transform")) continue;
				String algorithm = ((Element)t).getAttribute("Algorithm");
				if (!TRANSFORMS.contains(algorithm)) {
					throw new DigitalSignatureException("Transform " + algorithm + " is not allowed");
				}
				if (++transforms>MAX_TRANSFORMS) {
					throw new DigitalSignatureException("More than " + MAX_TRANSFORMS + " transforms in Reference " + uri);
				}
			}
		}
	}

	private static boolean isDSig(Node n, String localName) {
		return n.getNodeType()==Node.ELEMENT_NODE
				&& XMLDSIG.equals(n.getNamespaceURI())
				&& localName.equals(n.getLocalName());
	}

	/** XmlSignaturePart makes every Id attribute an ID, so each must identify one element */
	private static void checkIdsUnique(Element el, Set<String> seen) throws DigitalSignatureException {

		if (el.hasAttribute("Id") && !seen.add(el.getAttribute("Id"))) {
			throw new DigitalSignatureException("Duplicate Id '" + el.getAttribute("Id") + "'");
		}
		for (Node child = el.getFirstChild(); child!=null; child = child.getNextSibling()) {
			if (child.getNodeType()==Node.ELEMENT_NODE) {
				checkIdsUnique((Element)child, seen);
			}
		}
	}

}
