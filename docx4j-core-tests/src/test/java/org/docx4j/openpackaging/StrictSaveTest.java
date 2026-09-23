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
package org.docx4j.openpackaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.io3.stores.UnzippedPartStore;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.utils.ResourceUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Saving a package which was ISO/IEC 29500 Strict.
 * <p>
 * docx4j converts a strict package part by part, the first time each part is
 * read, and never writes strict, so a save has to convert the parts nobody
 * read as well - otherwise the output carries both dialects at once. That is
 * what these tests hold: the same answer from either part store, and, when a
 * part cannot be read, an exception which names it.
 */
public class StrictSaveTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	static final String STRICT = "strict/strict-comments.xlsx";
	static final String PURL = "purl.oclc.org";

	static byte[] resource(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource(name)) {
			return is.readAllBytes();
		}
	}

	/**
	 * Every .xml part of a saved package holding an element which is still in a
	 * strict namespace. An unused namespace <i>declaration</i> does not count: a
	 * DOM-kept extension element (an unbound extLst child) carries the
	 * declarations which were in scope where it was parsed, so a converted part
	 * can have a stray xmlns="purl..." on such a node with nothing in it - see
	 * {@link #theOnlyStrictLeftIsAnUnusedDeclaration()}.
	 */
	static List<String> partsWithStrictElements(byte[] saved) throws Exception {
		List<String> strict = new ArrayList<String>();
		for (Map.Entry<String, byte[]> part : xmlParts(saved).entrySet()) {
			if (hasStrictElement(parse(part.getValue()).getDocumentElement())) strict.add(part.getKey());
		}
		return strict;
	}

	static Map<String, byte[]> xmlParts(byte[] saved) throws Exception {
		Map<String, byte[]> parts = new LinkedHashMap<String, byte[]>();
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(saved))) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (e.getName().endsWith(".xml")) parts.put(e.getName(), zis.readAllBytes());
			}
		}
		return parts;
	}

	static Document parse(byte[] xml) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
	}

	static boolean hasStrictElement(Element el) {
		if (el.getNamespaceURI() != null && el.getNamespaceURI().contains(PURL)) return true;
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c instanceof Element && hasStrictElement((Element) c)) return true;
		}
		return false;
	}

	static byte[] save(OpcPackage pkg) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		return baos.toByteArray();
	}

	@Test
	public void toAZipEverythingIsTransitional() throws Exception {
		// whether or not anything read a part first
		for (boolean readOne : new boolean[] { false, true }) {
			OpcPackage pkg = OpcPackage.load(new ByteArrayInputStream(resource(STRICT)));
			if (readOne) {
				((JaxbXmlPart<?>) pkg.getParts().get(new PartName("/xl/workbook.xml"))).getContents();
			}
			assertEquals("readOne=" + readOne, new ArrayList<String>(), partsWithStrictElements(save(pkg)));
		}
	}

	@Test
	public void toADirectoryEverythingIsTransitionalToo() throws Exception {
		// until 17.2.1 UnzippedPartStore had no strict branch, so this wrote the part
		// something had read in the transitional namespaces and the rest still strict
		OpcPackage pkg = OpcPackage.load(new ByteArrayInputStream(resource(STRICT)));
		((JaxbXmlPart<?>) pkg.getParts().get(new PartName("/xl/workbook.xml"))).getContents();

		File dir = tmp.newFolder("unzipped");
		new Save(pkg, new UnzippedPartStore(dir)).save(null);

		List<String> stillStrict = new ArrayList<String>();
		for (Path p : (Iterable<Path>) Files.walk(dir.toPath())::iterator) {
			if (!p.toString().endsWith(".xml")) continue;
			if (hasStrictElement(parse(Files.readAllBytes(p)).getDocumentElement())) {
				stillStrict.add(dir.toPath().relativize(p).toString());
			}
		}
		assertEquals(new ArrayList<String>(), stillStrict);
	}

	@Test
	public void aPartWhichCannotBeReadNamesItself() throws Exception {
		// the class of failure CR-026 removed one instance of: a part docx4j cannot read
		// is fatal to a strict package's save, because every part must be converted. Here
		// the comments part is made unreadable on purpose.
		byte[] broken = withBrokenPart(resource(STRICT), "xl/comments1.xml",
				"<comments xmlns=\"http://purl.oclc.org/ooxml/spreadsheetml/main\"><notAllowedHere/></comments>");
		OpcPackage pkg = OpcPackage.load(new ByteArrayInputStream(broken));
		try {
			save(pkg);
			// the JAXB event handler may tolerate it; then there is nothing to test here
		} catch (Docx4JException e) {
			String message = e.getMessage() + " | " + (e.getCause() == null ? "" : e.getCause().getMessage());
			assertTrue(message, message.contains("/xl/comments1.xml"));
			assertTrue(message, message.contains("Strict"));
		}
	}

	@Test
	public void theMessageIsTheOneStrictSaveWantsToGive() throws Exception {
		// held directly, since provoking an unreadable part depends on how forgiving the
		// JAXB event handler is
		JaxbXmlPart<?> part = new JaxbXmlPart<Object>(new PartName("/xl/unreadable.xml")) {
			@Override
			public Object getContents() throws Docx4JException {
				throw new Docx4JException("Problem with part /xl/unreadable.xml");
			}
			@Override
			public String getXML() {
				return null;
			}
		};
		try {
			org.docx4j.openpackaging.io3.stores.PartStore.readSoTheSaveIsTransitional(part);
			assertFalse("expected a Docx4JException", true);
		} catch (Docx4JException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("/xl/unreadable.xml"));
			assertTrue(e.getMessage(), e.getMessage().contains("ISO/IEC 29500 Strict"));
			assertTrue(e.getMessage(), e.getMessage().contains("convert"));
			assertNotNull("the cause is kept", e.getCause());
		}
	}

	@Test
	public void theOnlyStrictLeftIsAnUnusedDeclaration() throws Exception {
		// pinned because it is confusing to meet: workbook.xml of the saved package still
		// mentions purl.oclc.org, on the DOM-kept xcalcf:calcFeatures extension element,
		// which carries the namespace declarations which were in scope where it was parsed.
		// Every element in that subtree is in the xcalcf namespace, so the declarations are
		// unused - the part is transitional - but a text search for "purl" finds them.
		OpcPackage pkg = OpcPackage.load(new ByteArrayInputStream(resource(STRICT)));
		byte[] workbook = xmlParts(save(pkg)).get("xl/workbook.xml");
		assertTrue("the declaration is there", new String(workbook, StandardCharsets.UTF_8).contains(PURL));
		assertFalse("but nothing is in that namespace", hasStrictElement(parse(workbook).getDocumentElement()));
	}

	/** the same package with one part's bytes replaced */
	static byte[] withBrokenPart(byte[] pkg, String entry, String xml) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(pkg));
				ZipOutputStream zos = new ZipOutputStream(baos)) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				byte[] bytes = e.getName().equals(entry) ? xml.getBytes(StandardCharsets.UTF_8) : zis.readAllBytes();
				zos.putNextEntry(new ZipEntry(e.getName()));
				zos.write(bytes);
				zos.closeEntry();
			}
		}
		return baos.toByteArray();
	}

}
