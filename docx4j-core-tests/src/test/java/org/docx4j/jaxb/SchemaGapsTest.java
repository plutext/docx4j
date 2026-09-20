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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart;
import org.docx4j.utils.ResourceUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The five schema gaps of CR-018: attributes Word writes which the model in
 * {@code xsd/} did not carry, so a re-marshalled part lost them, and the w14
 * checkbox value which was modelled as a string.
 *
 * <p>{@code loadAndSave.docx} (Word 365, 2026) is the test vector for the three
 * {@code mc:Ignorable} parts and for a {@code w15:person} without
 * {@code contact}; the rest are expressed as XML strings.</p>
 *
 * @since 17.2.0
 */
public class SchemaGapsTest {

	private static final String W14_NS = "http://schemas.microsoft.com/office/word/2010/wordml";

	private static byte[] saved;

	/** loadAndSave.docx, loaded and saved by docx4j once for the whole class. */
	@BeforeClass
	public static void loadAndSave() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(
				ResourceUtils.getResource("loadAndSave.docx"));

		// what the parts must have unmarshalled, before anything is written
		assertNotNull("CommentsPart missing from the test document",
				pkg.getMainDocumentPart().getCommentsPart());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		saved = baos.toByteArray();
	}

	/** The named part of the saved docx, as a String. */
	private static String savedPart(String entryName) throws Exception {

		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(saved));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.getName().equals(entryName)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[8192];
				int n;
				while ((n = zis.read(buf)) > 0) {
					baos.write(buf, 0, n);
				}
				return new String(baos.toByteArray(), StandardCharsets.UTF_8);
			}
		}
		throw new AssertionError(entryName + " not in the saved docx");
	}

	/** The start tag of the root element. */
	private static String rootTag(String xml) {
		int start = xml.indexOf("<", xml.indexOf("?>") + 2);
		return xml.substring(start, xml.indexOf(">", start) + 1);
	}

	// ---------------------------------------------------------------
	// Item 1: mc:Ignorable on w:comments, w15:commentsEx and w15:people
	// ---------------------------------------------------------------

	/**
	 * Word writes {@code mc:Ignorable="w14 w15 ..."} on {@code w:comments} and
	 * {@code w14:paraId} on the paragraphs it covers.  Before CR-018 the model
	 * had no {@code ignorable} property, so the save dropped the attribute and
	 * kept the {@code w14:paraId} it covers - which a Word 2007-2010 reader
	 * then sees in a namespace it is not told to ignore.
	 */
	@Test
	public void commentsPartKeepsMcIgnorable() throws Exception {

		String xml = savedPart("word/comments.xml");
		String root = rootTag(xml);

		assertTrue("mc:Ignorable lost from w:comments: " + root,
				root.contains("mc:Ignorable="));
		assertTrue("w14 not among the ignorable namespaces: " + root,
				root.matches(".*mc:Ignorable=\"[^\"]*\\bw14\\b.*"));
		assertTrue("w14 namespace not declared on w:comments: " + root,
				root.contains(W14_NS));
		// the attribute the mc:Ignorable covers is still there
		assertTrue("w14:paraId lost from the comment", xml.contains("w14:paraId"));
	}

	@Test
	public void commentsPartIgnorableUnmarshalled() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(
				ResourceUtils.getResource("loadAndSave.docx"));
		CommentsPart cp = pkg.getMainDocumentPart().getCommentsPart();
		assertNotNull(cp);
		assertNotNull("w:comments/@mc:Ignorable not unmarshalled",
				cp.getJaxbElement().getIgnorable());
		assertTrue(cp.getJaxbElement().getIgnorable().contains("w14"));
	}

	@Test
	public void commentsExtendedPartKeepsMcIgnorable() throws Exception {

		String root = rootTag(savedPart("word/commentsExtended.xml"));
		assertTrue("mc:Ignorable lost from w15:commentsEx: " + root,
				root.contains("mc:Ignorable="));
		assertTrue("w14 namespace not declared on w15:commentsEx: " + root,
				root.contains(W14_NS));
	}

	@Test
	public void peoplePartKeepsMcIgnorable() throws Exception {

		String root = rootTag(savedPart("word/people.xml"));
		assertTrue("mc:Ignorable lost from w15:people: " + root,
				root.contains("mc:Ignorable="));
		assertTrue("w14 namespace not declared on w15:people: " + root,
				root.contains(W14_NS));
	}

	// ---------------------------------------------------------------
	// Item 2: wp:docPr/@title
	// ---------------------------------------------------------------

	private static final String INLINE_WITH_TITLE =
			"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<w:body><w:p><w:r><w:drawing>"
			+ "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">"
			+ "<wp:extent cx=\"100\" cy=\"100\"/>"
			+ "<wp:docPr id=\"1\" name=\"Picture 1\" descr=\"the description\" title=\"the title\"/>"
			+ "<a:graphic><a:graphicData uri=\"http://example.com/g\"/></a:graphic>"
			+ "</wp:inline>"
			+ "</w:drawing></w:r></w:p></w:body></w:document>";

	private static org.docx4j.dml.CTNonVisualDrawingProps docPrOf(String documentXml)
			throws Exception {

		org.docx4j.wml.Document doc = (org.docx4j.wml.Document)
				XmlUtils.unwrap(XmlUtils.unmarshalString(documentXml));
		org.docx4j.wml.P p = (org.docx4j.wml.P) doc.getBody().getContent().get(0);
		org.docx4j.wml.R r = (org.docx4j.wml.R) p.getContent().get(0);
		org.docx4j.wml.Drawing drawing = (org.docx4j.wml.Drawing)
				XmlUtils.unwrap(r.getContent().get(0));
		org.docx4j.dml.wordprocessingDrawing.Inline inline =
				(org.docx4j.dml.wordprocessingDrawing.Inline)
				XmlUtils.unwrap(drawing.getAnchorOrInline().get(0));
		return inline.getDocPr();
	}

	/**
	 * {@code title} is in ECMA-376 4th edition (Transitional) and Office JS
	 * writes it as {@code InlinePicture.altTextTitle}; before CR-018 the model
	 * had only {@code descr}, so a round trip dropped it.
	 */
	@Test
	public void docPrTitleUnmarshalsAndRoundTrips() throws Exception {

		org.docx4j.dml.CTNonVisualDrawingProps docPr = docPrOf(INLINE_WITH_TITLE);
		assertEquals("the title", docPr.getTitle());
		assertEquals("the description", docPr.getDescr());

		String remarshalled = XmlUtils.marshaltoString(docPr, true, false,
				org.docx4j.jaxb.Context.jc,
				"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing",
				"docPr", org.docx4j.dml.CTNonVisualDrawingProps.class);
		assertTrue("title lost on marshal: " + remarshalled,
				remarshalled.contains("title=\"the title\""));
	}

	/** An absent title reads as "", as an absent descr does. */
	@Test
	public void docPrWithoutTitleIsEmpty() throws Exception {

		org.docx4j.dml.CTNonVisualDrawingProps docPr = docPrOf(
				INLINE_WITH_TITLE.replace(" title=\"the title\"", ""));
		assertEquals("", docPr.getTitle());
	}

	// ---------------------------------------------------------------
	// Item 3: w15:person/@contact optional
	// ---------------------------------------------------------------

	/**
	 * Word omits {@code contact} in every {@code w15:people} part seen -
	 * including this repository's own {@code loadAndSave.docx} - so the
	 * schema's {@code use="required"} had the model claim a value which is not
	 * there.
	 */
	@Test
	public void personWithoutContactLoadsAndSaves() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(
				ResourceUtils.getResource("loadAndSave.docx"));
		PeoplePart pp = (PeoplePart) pkg.getParts().get(
				new org.docx4j.openpackaging.parts.PartName("/word/people.xml"));
		assertNotNull("people.xml missing from the test document", pp);

		assertEquals(1, pp.getJaxbElement().getPerson().size());
		org.docx4j.w15.CTPerson person = pp.getJaxbElement().getPerson().get(0);
		assertEquals("Buck Cronk", person.getAuthor());
		assertNull("Word did not write w15:contact here", person.getContact());

		String people = savedPart("word/people.xml");
		assertTrue("w15:author lost", people.contains("w15:author=\"Buck Cronk\""));
		assertFalse("a w15:contact was invented", people.contains("w15:contact="));
	}

	// ---------------------------------------------------------------
	// Item 4: xsd:anyAttribute on ds:datastoreItem
	// ---------------------------------------------------------------

	/**
	 * Anything Word adds to {@code ds:datastoreItem} was dropped on
	 * re-marshal; the {@code xsd:anyAttribute} gives JAXB a
	 * {@code getOtherAttributes()} to hold it.
	 */
	@Test
	public void datastoreItemKeepsForeignAttributes() throws Exception {

		String xml = "<ds:datastoreItem"
				+ " xmlns:ds=\"http://schemas.openxmlformats.org/officeDocument/2006/customXml\""
				+ " xmlns:x=\"http://example.com/ns\""
				+ " ds:itemID=\"{9A8B7C6D-5E4F-3A2B-1C0D-9E8F7A6B5C4D}\""
				+ " x:foreign=\"kept\">"
				+ "<ds:schemaRefs/></ds:datastoreItem>";

		org.docx4j.customXmlProperties.DatastoreItem dsi =
				(org.docx4j.customXmlProperties.DatastoreItem)
				XmlUtils.unmarshalString(xml, Context.jcCustomXmlProperties);

		assertEquals("{9A8B7C6D-5E4F-3A2B-1C0D-9E8F7A6B5C4D}", dsi.getItemID());
		assertEquals("kept",
				dsi.getOtherAttributes().get(new QName("http://example.com/ns", "foreign")));

		String remarshalled = XmlUtils.marshaltoString(dsi, true, false,
				Context.jcCustomXmlProperties);
		assertTrue("the foreign attribute was dropped on marshal: " + remarshalled,
				remarshalled.contains("kept"));
	}

	// ---------------------------------------------------------------
	// Item 5: w14 CT_OnOff/@val is a boolean
	// ---------------------------------------------------------------

	private static boolean checkedVal(String lexical) throws Exception {

		String xml = "<w14:checked xmlns:w14=\"" + W14_NS + "\" w14:val=\"" + lexical + "\"/>";
		org.docx4j.w14.CTOnOff onOff = (org.docx4j.w14.CTOnOff)
				XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.w14.CTOnOff.class);
		return onOff.isVal();
	}

	/**
	 * w14's {@code ST_OnOff} is the four strings true, false, 0 and 1, which is
	 * exactly {@code xsd:boolean}'s lexical space, so all four now read as a
	 * boolean rather than as the string Word happened to write.
	 */
	@Test
	public void checkedAcceptsAllFourLexicalValues() throws Exception {

		assertTrue("w14:val=\"true\"", checkedVal("true"));
		assertTrue("w14:val=\"1\"", checkedVal("1"));
		assertFalse("w14:val=\"false\"", checkedVal("false"));
		assertFalse("w14:val=\"0\"", checkedVal("0"));
	}

	/** No w14:val at all is true, as w:BooleanDefaultTrue is. */
	@Test
	public void checkedDefaultsToTrue() throws Exception {

		String xml = "<w14:checked xmlns:w14=\"" + W14_NS + "\"/>";
		org.docx4j.w14.CTOnOff onOff = (org.docx4j.w14.CTOnOff)
				XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.w14.CTOnOff.class);
		assertTrue(onOff.isVal());
	}

	/** setVal(Boolean) writes w14:val, still in the w14 namespace. */
	@Test
	public void checkedMarshalsInTheW14Namespace() throws Exception {

		org.docx4j.w14.CTOnOff onOff = new org.docx4j.w14.CTOnOff();
		onOff.setVal(Boolean.TRUE);

		String xml = XmlUtils.marshaltoString(onOff, true, false, Context.jc,
				W14_NS, "checked", org.docx4j.w14.CTOnOff.class);
		assertTrue("w14:val not written: " + xml, xml.contains(":val=\"true\""));
		assertTrue("not in the w14 namespace: " + xml, xml.contains(W14_NS));
	}
}
