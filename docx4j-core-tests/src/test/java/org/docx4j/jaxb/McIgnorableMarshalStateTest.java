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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.junit.Test;

/**
 * CR-049: a marshal through {@code XmlUtils} leaves the shared prefix mapper's Ignorable value as it
 * found it, and declares what its own root's {@code mc:Ignorable} names plus the Choice namespaces its
 * caller passes.  Before, a {@code wml.Document}'s prefixes stayed on the thread and every later marshal
 * pre-declared them, while any other root declared neither its Ignorable prefixes nor its Choice
 * prefixes.  Each case runs on a thread of its own, so it starts from the value a fresh thread has (none).
 *
 * <p>What is observable: the reference implementation declares every namespace of the main context on
 * a root it marshals, so a borrowed declaration shows on a root of another context (a relationships
 * part's {@code Relationships}); and the canonicalize path trims every namespace its content does not
 * use except those the mapper's value names, so a missing one shows there.</p>
 */
public class McIgnorableMarshalStateTest {

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	private static McIgnorableNamespaceDeclarator mapper() throws Exception {
		return (McIgnorableNamespaceDeclarator) NamespacePrefixMapperUtils.getPrefixMapper();
	}

	/** Runs the body on a new thread (a fresh thread-local value) and rethrows what it threw. */
	private static <T> T onFreshThread(Callable<T> body) throws Exception {
		AtomicReference<T> result = new AtomicReference<T>();
		AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		Thread t = new Thread(() -> {
			try {
				result.set(body.call());
			} catch (Throwable e) {
				thrown.set(e);
			}
		});
		t.start();
		t.join();
		if (thrown.get() instanceof Exception) throw (Exception) thrown.get();
		if (thrown.get() instanceof Error) throw (Error) thrown.get();
		return result.get();
	}

	/** Runs the body with docx4j.jaxb.marshal.canonicalize set as given, then puts the property back. */
	private static <T> T canonicalized(boolean on, Callable<T> body) throws Exception {
		boolean before = Docx4jProperties.getProperty("docx4j.jaxb.marshal.canonicalize", false);
		Docx4jProperties.setProperty("docx4j.jaxb.marshal.canonicalize", on);
		try {
			return body.call();
		} finally {
			Docx4jProperties.setProperty("docx4j.jaxb.marshal.canonicalize", before);
		}
	}

	private static String startTag(String xml) {
		int start = xml.indexOf('<', xml.startsWith("<?") ? xml.indexOf("?>") : 0);
		return xml.substring(start, xml.indexOf('>', start) + 1);
	}

	private static Document document(String ignorable) {
		Document d = F.createDocument();
		d.setBody(F.createBody());
		d.setIgnorable(ignorable);
		return d;
	}

	/** A root of another context: the main context's namespaces are not declared on it unless the mapper pre-declares them. */
	private static org.docx4j.relationships.Relationships rels() {
		return new org.docx4j.relationships.ObjectFactory().createRelationships();
	}

	private static void assertDeclaresNeither(String what, String xml) {
		String tag = startTag(xml);
		assertFalse(what + ": " + tag, tag.contains("xmlns:w14"));
		assertFalse(what + ": " + tag, tag.contains("xmlns:w15"));
	}

	/** A document's prefixes are its own: what is marshalled after it, by each of the methods, declares none of them. */
	@Test
	public void whatFollowsADocumentBorrowsNothing() throws Exception {
		onFreshThread(() -> {
			XmlUtils.marshaltoString(document("w14 w15"), true, false);
			assertDeclaresNeither("marshaltoString", XmlUtils.marshaltoString(rels(), true, false, Context.jcRelationships));
			assertDeclaresNeither("marshaltoW3CDomDocument",
					XmlUtils.w3CDomNodeToString(XmlUtils.marshaltoW3CDomDocument(rels(), Context.jcRelationships)));
			try (InputStream in = XmlUtils.marshaltoInputStream(rels(), true, Context.jcRelationships)) {
				assertDeclaresNeither("marshaltoInputStream", new String(in.readAllBytes(), StandardCharsets.UTF_8));
			}
			assertNull("the thread is left as it was", mapper().getMcIgnorable());
			return null;
		});
	}

	/** The same after a canonicalized document, whose marshaltoString nests a marshaltoW3CDomDocument call. */
	@Test
	public void whatFollowsACanonicalizedDocumentBorrowsNothing() throws Exception {
		onFreshThread(() -> {
			String doc = canonicalized(true, () -> XmlUtils.marshaltoString(document("w14 w15"), true, false));
			assertTrue("the document declares its own: " + startTag(doc), startTag(doc).contains("xmlns:w14"));
			assertNull(mapper().getMcIgnorable());
			assertDeclaresNeither("after", XmlUtils.marshaltoString(rels(), true, false, Context.jcRelationships));
			return null;
		});
	}

	/** Any root declares what its own mc:Ignorable names, though its content uses none of it: canonicalization keeps the prefixes (CR-024's rule). */
	@Test
	public void anyRootDeclaresItsOwnIgnorablePrefixes() throws Exception {
		onFreshThread(() -> canonicalized(true, () -> {
			Hdr hdr = F.createHdr();
			hdr.setIgnorable("w14");
			assertTrue(startTag(XmlUtils.marshaltoString(hdr, true, false)), startTag(XmlUtils.marshaltoString(hdr, true, false)).contains("xmlns:w14"));
			org.docx4j.wml.CTSettings settings = F.createCTSettings();
			settings.setIgnorable("w14");
			assertTrue(startTag(XmlUtils.marshaltoString(settings, true, false)).contains("xmlns:w14"));
			org.docx4j.wml.CTFootnotes footnotes = F.createCTFootnotes();
			footnotes.setIgnorable("w14");
			assertTrue(startTag(XmlUtils.marshaltoString(footnotes, true, false)).contains("xmlns:w14"));
			assertNull(mapper().getMcIgnorable());
			return null;
		}));
	}

	/** A part's Choice namespaces reach getXML(): a header part whose kept Choice requires wps declares it. */
	@Test
	public void getXmlDeclaresTheChoiceNamespaces() throws Exception {
		onFreshThread(() -> canonicalized(true, () -> {
			HeaderPart part = new HeaderPart();
			part.setJaxbElement(F.createHdr());
			part.addMcChoiceNamespace("wps");
			String xml = part.getXML();
			assertTrue(startTag(xml), startTag(xml).contains("xmlns:wps"));
			assertNull(mapper().getMcIgnorable());
			return null;
		}));
	}

	/**
	 * Flat OPC sets each part's prefixes and leaves none on the thread: after the main document part is
	 * written (whose prefixes the old code left behind, as it did the last part's of a whole export), and
	 * after a whole Flat OPC save.
	 */
	@Test
	public void flatOpcLeavesNothingBehind() throws Exception {
		onFreshThread(() -> {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			pkg.getMainDocumentPart().getJaxbElement().setIgnorable("w14 w15");
			pkg.getMainDocumentPart().addParagraphOfText("Flat OPC");
			new org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator(pkg).createRawXmlPart(pkg.getMainDocumentPart());
			assertNull(mapper().getMcIgnorable());
			assertDeclaresNeither("after a Flat OPC part", XmlUtils.marshaltoString(rels(), true, false, Context.jcRelationships));
			org.docx4j.Docx4J.save(pkg, new java.io.ByteArrayOutputStream(), org.docx4j.Docx4J.FLAG_SAVE_FLAT_XML);
			assertNull(mapper().getMcIgnorable());
			assertDeclaresNeither("after a Flat OPC save", XmlUtils.marshaltoString(rels(), true, false, Context.jcRelationships));
			return null;
		});
	}

	/** A marshal nested in another declares nothing of the outer one's value, and puts it back when it is done (D1). */
	@Test
	public void aNestedMarshalBorrowsNothingAndRestores() throws Exception {
		onFreshThread(() -> {
			mapper().setMcIgnorable("w14 w15"); // an enclosing marshal's value
			try {
				assertDeclaresNeither("nested", XmlUtils.marshaltoString(rels(), true, false, Context.jcRelationships));
				assertEquals("w14 w15", mapper().getMcIgnorable());
				String doc = canonicalized(true, () -> XmlUtils.marshaltoString(document("w16se"), true, false));
				assertTrue(startTag(doc), startTag(doc).contains("xmlns:w16se"));
				assertDeclaresNeither("a nested document declares its own", doc);
				assertEquals("w14 w15", mapper().getMcIgnorable());
			} finally {
				mapper().setMcIgnorable(null);
			}
			return null;
		});
	}
}
