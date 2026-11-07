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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.xml.bind.JAXBContext;

import org.junit.Test;

/**
 * The published sets (WD REQ-306; CR-046 section 2.2): their format, docx4j's understood set equal to
 * its file, every namespace docx4j's reader can reach in the traversal set, and what separates the two.
 */
public class UnderstoodNamespacesTest {

	/** Bound by docx4j but never reached by its reader of document content (the resource's README). */
	private static final Set<String> NOT_DOCUMENT_CONTENT = new LinkedHashSet<String>(Arrays.asList(
			"", "http://www.w3.org/XML/1998/namespace",
			"http://schemas.openxmlformats.org/package/2006/content-types",
			"http://opendope.org/answers", "http://opendope.org/components", "http://opendope.org/conditions",
			"http://opendope.org/questions", "http://opendope.org/xpaths", "http://opendope.org/SmartArt/DataHierarchy"));

	private static final String TRAVERSAL_NAMESPACES_RESOURCE = "org/docx4j/jaxb/traversal-namespaces.txt";

	private static List<String> lines(String resource) throws Exception {
		try (InputStream is = McSelection.class.getClassLoader().getResourceAsStream(resource)) {
			String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			assertTrue("ends with LF", text.endsWith("\n"));
			assertTrue("LF only", !text.contains("\r"));
			return Arrays.asList(text.substring(0, text.length() - 1).split("\n", -1));
		}
	}

	/** One absolute URI per line, no blank lines, no duplicates; the set docx4j selects with is exactly the file's. */
	@Test
	public void publishedListIsTheSet() throws Exception {
		Set<String> seen = wellFormed(McSelection.UNDERSTOOD_NAMESPACES_RESOURCE);
		wellFormed(TRAVERSAL_NAMESPACES_RESOURCE);
		assertEquals(new ArrayList<String>(seen), new ArrayList<String>(McSelection.understoodNamespaces()));
		assertTrue(McSelection.understoodNamespaces().contains("http://schemas.microsoft.com/office/word/2010/wordprocessingShape"));
		assertTrue(McSelection.understoodNamespaces().contains("urn:schemas-microsoft-com:vml"));
	}

	private static Set<String> wellFormed(String resource) throws Exception {
		Set<String> seen = new LinkedHashSet<String>();
		for (String line : lines(resource)) {
			assertTrue("blank or padded line: '" + line + "'", !line.isEmpty() && line.equals(line.trim()));
			assertTrue("not absolute: " + line, new URI(line).isAbsolute());
			assertTrue("duplicate: " + line, seen.add(line));
		}
		return seen;
	}

	/** REQ-306: the sets differ by what was measured: 13 a reader reaches that Word doesn't understand, 9 the other way about. */
	@Test
	public void theSetsDifferByWhatWordUnderstands() throws Exception {
		Set<String> traversal = new LinkedHashSet<String>(lines(TRAVERSAL_NAMESPACES_RESOURCE));
		Set<String> understood = McSelection.understoodNamespaces();
		assertEquals(114, understood.size());
		assertEquals(118, traversal.size());
		Set<String> readerOnly = new TreeSet<String>(traversal);
		readerOnly.removeAll(understood);
		assertEquals(13, readerOnly.size());
		assertTrue(readerOnly.contains("http://purl.org/dc/elements/1.1/"));
		assertTrue(readerOnly.contains("http://schemas.microsoft.com/office/2006/encryption"));
		Set<String> wordOnly = new TreeSet<String>(understood);
		wordOnly.removeAll(traversal);
		assertEquals(9, wordOnly.size());
		assertTrue(wordOnly.contains("http://schemas.microsoft.com/office/word/2010/wordprocessingInk"));
		assertTrue(wordOnly.contains("http://www.w3.org/2001/XMLSchema-instance"));
	}

	/**
	 * REQ-306: the traversal set includes every namespace docx4j's reader can traverse, which is every namespace
	 * its JAXB contexts bind but those that never hold document content. Measured through the
	 * reference implementation's known namespaces.
	 */
	@Test
	public void everyBoundNamespaceIsInTheTraversalSet() throws Exception {
		Set<String> bound = new TreeSet<String>();
		for (Class<?> c : new Class<?>[] { Context.class, org.pptx4j.jaxb.Context.class, org.xlsx4j.jaxb.Context.class }) {
			for (java.lang.reflect.Field f : c.getFields()) {
				if (!JAXBContext.class.isAssignableFrom(f.getType()) || f.get(null) == null) continue;
				Object jc = f.get(null);
				Method known;
				try {
					known = jc.getClass().getMethod("getKnownNamespaceURIs");
				} catch (NoSuchMethodException e) {
					assumeTrue("not the reference implementation", false);
					return;
				}
				@SuppressWarnings("unchecked")
				Collection<String> uris = (Collection<String>) known.invoke(jc);
				bound.addAll(uris);
			}
		}
		Set<String> missing = new TreeSet<String>(bound);
		missing.removeAll(lines(TRAVERSAL_NAMESPACES_RESOURCE));
		missing.removeAll(NOT_DOCUMENT_CONTENT);
		assertTrue("bound but not in the published traversal set (add them there first): " + missing, missing.isEmpty());
	}
}
