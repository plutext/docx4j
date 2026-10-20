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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.junit.Test;

/**
 * The binder path a {@link JaxbXmlPartXPathAware} part takes when StAX rejects it (CR-051 section
 * 1, the binder-path defect; section 9, finding 1): the part is parsed to a DOM, sent through
 * {@code mc-preprocessor.xslt} and unmarshalled through a binder.  Until 17.3.4 no
 * {@link Docx4jUnmarshallerListener} was set on that binder ("TODO: binder doesn't support
 * listener"), so such a part lost the listener's bookkeeping: the prefixes each
 * {@code mc:Choice} requires (declared on the root at save, or Word repairs the file) and each
 * drawing's {@code docPr} id.
 */
public class BinderPathListenerTest {

	private static final String NS = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\"";

	/** A w14 element where the schema does not admit one: StAX rejects the part, so it takes the binder path. */
	private static final String REJECTED = "<w:p><w:pPr><w14:nonsense/></w:pPr><w:r><w:t>rejected content before</w:t></w:r></w:p>";

	/** A text box in Word's form: a wps Choice (its drawing docPr id 2) and a VML Fallback. */
	private static final String TEXT_BOX = "<w:p><w:r><mc:AlternateContent><mc:Choice Requires=\"wps\"><w:drawing>"
			+ "<wp:inline><wp:extent cx=\"1270000\" cy=\"635000\"/><wp:docPr id=\"2\" name=\"Text Box 2\"/>"
			+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
			+ "<wps:wsp><wps:cNvSpPr txBox=\"1\"/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1270000\" cy=\"635000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></wps:spPr>"
			+ "<wps:txbx><w:txbxContent><w:p><w:r><w:t>in the box</w:t></w:r></w:p></w:txbxContent></wps:txbx>"
			+ "<wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></mc:Choice>"
			+ "<mc:Fallback><w:pict><v:shape id=\"Text Box 2\" type=\"#_x0000_t202\" style=\"width:100pt;height:50pt\">"
			+ "<v:textbox><w:txbxContent><w:p><w:r><w:t>in the box</w:t></w:r></w:p></w:txbxContent></v:textbox>"
			+ "</v:shape></w:pict></mc:Fallback></mc:AlternateContent></w:r></w:p>";

	static byte[] docx(String body) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			put(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
					+ "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
					+ "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
					+ "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
					+ "</Types>");
			put(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
					+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
					+ "</Relationships>");
			put(zip, "word/_rels/document.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>");
			put(zip, "word/document.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<w:document" + NS + "><w:body>" + body + "</w:body></w:document>");
		}
		return bytes.toByteArray();
	}

	private static void put(ZipOutputStream zip, String name, String xml) throws Exception {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(xml.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

	static String documentXml(byte[] docx) throws Exception {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx))) {
			for (ZipEntry e; (e = zip.getNextEntry()) != null; ) {
				if (e.getName().equals("word/document.xml")) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[8192];
					for (int n; (n = zip.read(buf)) > 0; ) out.write(buf, 0, n);
					return new String(out.toByteArray(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new AssertionError("no word/document.xml");
	}

	private static boolean hasBinder(JaxbXmlPartXPathAware<?> part) throws Exception {
		Field f = JaxbXmlPartXPathAware.class.getDeclaredField("binder");
		f.setAccessible(true);
		return f.get(part) != null;
	}

	@Test
	public void rejectedPartKeepsTheListenersBookkeeping() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx(REJECTED + TEXT_BOX)));
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		assertNotNull(mdp.getContents());
		assertTrue("the probe took the binder path (StAX rejected w14:nonsense in w:pPr)", hasBinder(mdp));
		assertEquals("the paragraphs survived the preprocessor", 2, mdp.getContent().size());

		// the Choice's Requires prefix was recorded for declaration at save
		assertTrue("wps recorded from the Choice's Requires, but got '" + mdp.getMcChoiceNamespaces() + "'",
				mdp.getMcChoiceNamespaces().contains("wps"));

		// the drawing's docPr id was registered, so the next id dispensed is not 2
		assertEquals("docPr id 2 is in use, so 3 is the next free id", 3L, pkg.getDrawingPropsIdTracker().generateId());

		// and a save declares the prefix on the root, which Word needs to open the file
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.save(pkg, out);
		String xml = documentXml(out.toByteArray());
		String root = xml.substring(xml.indexOf("<w:document"), xml.indexOf('>', xml.indexOf("<w:document")));
		assertTrue("xmlns:wps declared on the root: " + root, root.contains("xmlns:wps="));
	}
}
