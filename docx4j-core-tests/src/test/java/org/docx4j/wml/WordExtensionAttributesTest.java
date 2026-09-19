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
package org.docx4j.wml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-023: the extension attributes and element Word writes on ISO elements are admitted
 * in the main schema, so a load-and-save keeps them.  Before, the packages existed but
 * wml.xsd had no reference where Word writes them, and JAXB dropped them silently (no
 * anyAttribute): every tracked change's UTC date, every numbering definition's
 * restart flag, the durable id of every w:num.
 *
 * The three that the repository's documents carry are checked by a round trip
 * (every JAXB part forced to unmarshal first - an untouched part is written back
 * from its bytes, which would hide the loss); the four that no file carries
 * (Jason's Word 365 build wrote none of them, CR-023 section 16) are admitted on
 * [MS-DOCX]'s word and checked by building and marshalling the objects.
 */
public class WordExtensionAttributesTest {

	private static final String TRACKED = "tracked-changes-equations.docx";   // w16du:dateUtc x39, w16cid:durableId on w:num
	private static final String NUMBERED = "numPicBullet-word2019-pict.docx";  // w15:restartNumberingAfterBreak

	@Test
	public void roundTripKeepsDateUtcAndDurableId() throws Exception {
		byte[] in = bytes(ResourceUtils.getResource(TRACKED));
		byte[] out = loadTouchEverythingAndSave(in);
		assertEquals(39, count(in, "w16du:dateUtc"));
		assertEquals("w16du:dateUtc was dropped before CR-023", 39, count(out, "w16du:dateUtc"));
		assertEquals(1, count(in, "w16cid:durableId"));
		assertEquals("w16cid:durableId on w:num was dropped before CR-023", 1, count(out, "w16cid:durableId"));
		// and the prefixes the document's mc:Ignorable names are all declared on the re-save
		assertDeclares(out, "word/document.xml");
		assertDeclares(out, "word/numbering.xml");
	}

	@Test
	public void roundTripKeepsRestartNumberingAfterBreak() throws Exception {
		byte[] in = bytes(ResourceUtils.getResource(NUMBERED));
		byte[] out = loadTouchEverythingAndSave(in);
		assertEquals(1, count(in, "w15:restartNumberingAfterBreak"));
		assertEquals("w15:restartNumberingAfterBreak was dropped before CR-023", 1, count(out, "w15:restartNumberingAfterBreak"));
	}

	@Test
	public void typedAccessors() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(ResourceUtils.getResource(TRACKED));

		// every w:ins and w:del in the body (here inside equations, CR-010's model) carries dateUtc,
		// an XMLGregorianCalendar like w:date
		List<Object> changes = pkg.getMainDocumentPart().getJAXBNodesViaXPath("//w:ins|//w:del", false);
		assertEquals(39, changes.size());
		for (Object o : changes) {
			CTTrackChange c = (CTTrackChange) XmlUtils.unwrap(o);
			assertNotNull(c.getDateUtc());
			assertEquals("Z", c.getDateUtc().toXMLFormat().substring(c.getDateUtc().toXMLFormat().length() - 1));
			assertNotNull(c.getDate());
		}

		Numbering numbering = pkg.getMainDocumentPart().getNumberingDefinitionsPart().getJaxbElement();
		assertEquals("0", numbering.getAbstractNum().get(0).getRestartNumberingAfterBreak());
		assertTrue(numbering.getNum().get(0).getDurableId().compareTo(BigInteger.ZERO) > 0);
	}

	@Test
	public void extensionsAdmittedOnTheSpecificationsWordMarshal() throws Exception {
		// no fixture: Jason's Word 365 build wrote none of these (CR-023 section 16)
		ObjectFactory f = Context.getWmlObjectFactory();

		P p = f.createP();
		p.setNoSpellErr("1");
		String xml = XmlUtils.marshaltoString(p, true, false);
		assertTrue(xml, xml.contains("w14:noSpellErr=\"1\""));

		SdtPr sdtPr = f.createSdtPr();
		sdtPr.setFormattingAllowed("1");
		xml = XmlUtils.marshaltoString(sdtPr, true, false);
		assertTrue(xml, xml.contains("w16sdtfl:formattingAllowed=\"1\""));

		CTDataBinding binding = f.createCTDataBinding();
		binding.setXpath("/ns0:root[1]");
		binding.setStoreItemID("{00000000-0000-0000-0000-000000000000}");
		binding.setStoreItemChecksum("AAAA");
		xml = XmlUtils.marshaltoString(binding, true, false);
		assertTrue(xml, xml.contains("w16sdtdh:storeItemChecksum=\"AAAA\""));

		org.docx4j.w15symex.ObjectFactory se = new org.docx4j.w15symex.ObjectFactory();
		org.docx4j.w15symex.CTSymEx symEx = se.createCTSymEx();
		symEx.setFont("Segoe UI Emoji");
		symEx.setChar("0001F43C");
		R r = f.createR();
		r.getContent().add(se.createSymEx(symEx));
		xml = XmlUtils.marshaltoString(r, true, false);
		assertTrue(xml, xml.contains("<w16se:symEx "));
		// and it unmarshals back into the run's content as the typed class
		R again = (R) XmlUtils.unmarshalString(xml);
		assertTrue(XmlUtils.unwrap(again.getContent().get(0)) instanceof org.docx4j.w15symex.CTSymEx);

		org.docx4j.cei.ObjectFactory cei = new org.docx4j.cei.ObjectFactory();
		org.docx4j.cei.CTCommentEntityInfo info = cei.createCTCommentEntityInfo();
		info.setEntityType(1L);
		org.docx4j.w16cex.CTCommentExtensible comment = new org.docx4j.w16cex.CTCommentExtensible();
		comment.setDurableId("3F0E3D6A");
		org.docx4j.w16.CTExtensionList extLst = new org.docx4j.w16.CTExtensionList();
		org.docx4j.w16.CTExtension ext = new org.docx4j.w16.CTExtension();
		ext.setUri("{CEI}");
		ext.setAny(cei.createCommentEntityInfo(info));
		extLst.getExt().add(ext);
		comment.setExtLst(extLst);
		org.docx4j.w16cex.CTCommentsExtensible comments = new org.docx4j.w16cex.CTCommentsExtensible();
		comments.getCommentExtensible().add(comment);
		xml = XmlUtils.marshaltoString(new org.docx4j.w16cex.ObjectFactory().createCommentsExtensible(comments), true, false);
		assertTrue(xml, xml.contains("<cei:commentEntityInfo ") && xml.contains("cei:entityType=\"1\""));
		// typed on the way back in, through w16:CT_Extension's lax wildcard
		org.docx4j.w16cex.CTCommentsExtensible reloaded =
				(org.docx4j.w16cex.CTCommentsExtensible) XmlUtils.unwrap(XmlUtils.unmarshalString(xml));
		assertTrue(XmlUtils.unwrap(reloaded.getCommentExtensible().get(0).getExtLst().getExt().get(0).getAny())
				instanceof org.docx4j.cei.CTCommentEntityInfo);
	}

	// ---- helpers

	private static byte[] loadTouchEverythingAndSave(byte[] in) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(in));
		for (Part p : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			if (p instanceof JaxbXmlPart) ((JaxbXmlPart<?>) p).getContents();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		return bos.toByteArray();
	}

	/** Occurrences of the token as an element or attribute name in the package's XML parts. */
	private static int count(byte[] zip, String token) throws Exception {
		int n = 0;
		Pattern pattern = Pattern.compile("[<\\s]" + Pattern.quote(token) + "[=\\s>/]");
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (!e.getName().endsWith(".xml")) continue;
				Matcher m = pattern.matcher(new String(bytes(zis), StandardCharsets.UTF_8));
				while (m.find()) n++;
			}
		}
		return n;
	}

	private static void assertDeclares(byte[] zip, String partName) throws Exception {
		String xml = null;
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (e.getName().equals(partName)) xml = new String(bytes(zis), StandardCharsets.UTF_8);
			}
		}
		if (xml == null) fail("no " + partName);
		Matcher m = Pattern.compile("mc:Ignorable=\"([^\"]*)\"").matcher(xml);
		assertTrue(partName + " has no mc:Ignorable", m.find());
		for (String prefix : m.group(1).split(" ")) {
			assertTrue(partName + " does not declare xmlns:" + prefix, xml.contains("xmlns:" + prefix + "=\""));
		}
	}

	private static byte[] bytes(InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
		return bos.toByteArray();
	}
}
