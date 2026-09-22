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
package org.docx4j.anon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.docx4j.XmlUtils;
import org.docx4j.anon.AnonymizeResult.Action;
import org.docx4j.anon.AnonymizeResult.PartAction;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.ActiveXControlXmlPart;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.PresentationML.CommentAuthorsPart;
import org.docx4j.openpackaging.parts.PresentationML.CommentsPart;
import org.docx4j.openpackaging.parts.PresentationML.FontDataPart;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.PresentationML.TableStylesPart;
import org.docx4j.openpackaging.parts.PresentationML.TagsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.junit.Test;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.CTCommentAuthorList;
import org.pptx4j.pml.CTCommentList;
import org.pptx4j.pml.Presentation;
import org.pptx4j.pml.Shape;
import org.pptx4j.pml.Sld;
import org.pptx4j.pml.TagLst;

/**
 * The pptx probes of CR-019 phase 2: one generated deck per gap (in the style
 * of {@link AnonymizeProbesTest}: built here, never a customer file), each
 * planted with words that must not survive. The generic check is
 * {@link #allXml}: every XML part and relationships part of the output,
 * searched for the planted words; the specific checks are the structure the
 * tool promises to keep.
 */
public class AnonymizePptxProbesTest {

	static final String P = "http://schemas.openxmlformats.org/presentationml/2006/main";
	static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
	static final String R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
	static final String NS = "xmlns:a=\"" + A + "\" xmlns:r=\"" + R + "\" xmlns:p=\"" + P + "\"";

	// ---- helpers

	/** an empty deck with one slide, its layout and master */
	static PresentationMLPackage deck() throws Exception {
		PresentationMLPackage pkg = PresentationMLPackage.createPackage();
		SlidePart slide = new SlidePart(new PartName("/ppt/slides/slide1.xml"));
		slide.setContents(SlidePart.createSld());
		pkg.getMainPresentationPart().addSlide(0, slide);
		slide.addTargetPart(layout(pkg));
		return pkg;
	}

	static SlidePart slide(PresentationMLPackage pkg) throws Exception {
		return (SlidePart) pkg.getParts().get(new PartName("/ppt/slides/slide1.xml"));
	}

	static SlideLayoutPart layout(PresentationMLPackage pkg) throws Exception {
		return (SlideLayoutPart) pkg.getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));
	}

	static Part part(OpcPackage pkg, String name) throws Exception {
		return pkg.getParts().get(new PartName(name));
	}

	/** a p:sp with a title placeholder and the given text */
	static Shape titleShape(int id, String text) throws Exception {
		return (Shape) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<p:sp " + NS + "><p:nvSpPr><p:cNvPr id=\"" + id + "\" name=\"Title " + id + "\"/>"
				+ "<p:cNvSpPr><a:spLocks noGrp=\"1\"/></p:cNvSpPr><p:nvPr><p:ph type=\"title\"/></p:nvPr></p:nvSpPr>"
				+ "<p:spPr/><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang=\"en-US\"/><a:t>" + text
				+ "</a:t></a:r></a:p></p:txBody></p:sp>", Context.jcPML));
	}

	/** a free text box with one paragraph of runs (each run's markup given) */
	static Shape textBox(int id, String runs) throws Exception {
		return (Shape) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<p:sp " + NS + "><p:nvSpPr><p:cNvPr id=\"" + id + "\" name=\"TextBox " + id + "\"/>"
				+ "<p:cNvSpPr txBox=\"1\"/><p:nvPr/></p:nvSpPr>"
				+ "<p:spPr><a:xfrm><a:off x=\"1000000\" y=\"1000000\"/><a:ext cx=\"4000000\" cy=\"600000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></p:spPr>"
				+ "<p:txBody><a:bodyPr/><a:lstStyle/><a:p>" + runs + "</a:p></p:txBody></p:sp>", Context.jcPML));
	}

	static void addShape(SlidePart slide, Object shape) throws Exception {
		slide.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(shape);
	}

	static Relationship externalRel(Part source, String type, String target) {
		Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType(type);
		rel.setTarget(target);
		rel.setTargetMode("External");
		source.getRelationshipsPart(true).addRelationship(rel);
		return rel;
	}

	static BinaryPart binaryPart(String name, String contentType, String relType, byte[] bytes) throws Exception {
		BinaryPart p = new BinaryPart(new PartName(name));
		p.setContentType(new ContentType(contentType));
		p.setRelationshipType(relType);
		p.setBinaryData(bytes);
		return p;
	}

	static DefaultXmlPart xmlPart(String name, String contentType, String relType, String xml) throws Exception {
		DefaultXmlPart p = new DefaultXmlPart(new PartName(name));
		p.setContentType(new ContentType(contentType));
		p.setRelationshipType(relType);
		p.setDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		return p;
	}

	/** Every XML part and relationships part of the package, as one string. */
	static String allXml(OpcPackage pkg) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(pkg.getRelationshipsPart().getXML());
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof JaxbXmlPart) {
				Object c = ((JaxbXmlPart<?>) p).getContents();
				if (c != null) sb.append(((JaxbXmlPart<?>) p).getXML());
			} else if (p instanceof XmlPart) {
				sb.append(XmlUtils.w3CDomNodeToString(((XmlPart) p).getDocument()));
			}
			RelationshipsPart rp = p.getRelationshipsPart();
			if (rp != null && rp.getRelationships() != null) sb.append(rp.getXML());
		}
		return sb.toString();
	}

	static void assertGone(String all, String... words) {
		String lower = all.toLowerCase(Locale.ROOT);
		for (String w : words) {
			assertFalse("'" + w + "' survived", lower.contains(w.toLowerCase(Locale.ROOT)));
		}
	}

	static void assertClean(AnonymizeResult r) {
		assertTrue(r.summary(), r.isClean());
		assertEquals("verified: " + r.getLeaks(), Boolean.TRUE, r.getVerified());
	}

	static PartAction action(AnonymizeResult r, String partName) {
		return AnonymizeCorpusTest.action(r, partName);
	}

	static PresentationMLPackage reload(PresentationMLPackage pkg) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		return (PresentationMLPackage) OpcPackage.load(new ByteArrayInputStream(baos.toByteArray()));
	}

	// ---- gap 1: identity - legacy comments and authors, 2018 comments and authors

	static PresentationMLPackage commentsDeck() throws Exception {
		PresentationMLPackage pkg = deck();
		MainPresentationPart pp = pkg.getMainPresentationPart();
		SlidePart slide = slide(pkg);
		addShape(slide, titleShape(2, "Zorbling strategy"));

		// legacy (2007) comments: one author with presence info, one comment
		CommentAuthorsPart authors = new CommentAuthorsPart(new PartName("/ppt/commentAuthors.xml"));
		authors.setContents((CTCommentAuthorList) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<p:cmAuthorLst " + NS + "><p:cmAuthor id=\"0\" name=\"Jane Reviewer\" initials=\"JR\" lastIdx=\"1\" clrIdx=\"0\">"
				+ "<p:extLst><p:ext uri=\"{19B8F6BF-5375-455C-9EA6-DF929625EA0E}\">"
				+ "<p15:presenceInfo xmlns:p15=\"http://schemas.microsoft.com/office/powerpoint/2012/main\" userId=\"jane.reviewer@zorbling.example\" providerId=\"AD\"/>"
				+ "</p:ext></p:extLst></p:cmAuthor></p:cmAuthorLst>", Context.jcPML)));
		pp.addTargetPart(authors);

		CommentsPart comments = new CommentsPart(new PartName("/ppt/comments/comment1.xml"));
		comments.setContents((CTCommentList) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<p:cmLst " + NS + "><p:cm authorId=\"0\" dt=\"2024-06-15T10:30:00.000\" idx=\"1\">"
				+ "<p:pos x=\"10\" y=\"10\"/><p:text>Please check the Quibblex figures</p:text></p:cm></p:cmLst>", Context.jcPML)));
		slide.addTargetPart(comments);

		// 2018 comments: not bound, DOM parts
		String p188 = "http://schemas.microsoft.com/office/powerpoint/2018/8/main";
		DefaultXmlPart modernAuthors = xmlPart("/ppt/authors.xml", ContentTypes.PRESENTATIONML_MODERN_COMMENT_AUTHORS,
				"http://schemas.microsoft.com/office/2018/10/relationships/authors",
				"<p188:authorLst xmlns:p188=\"" + p188 + "\"><p188:author id=\"{D66C2D0B-8267-47C3-1395-767C976A08A1}\""
				+ " name=\"Bob Editor\" initials=\"BE\" userId=\"S::bob.editor@zorbling.example::e3c6b3cb\" providerId=\"AD\"/></p188:authorLst>");
		pp.addTargetPart(modernAuthors);
		// [MS-PPTX] CT_Comment: the anchor is required and comes first, then pos, then the
		// replies, then the comment's own text (PowerPoint refuses the other order)
		long sldId = pp.getContents().getSldIdLst().getSldId().get(0).getId();
		DefaultXmlPart modernComments = xmlPart("/ppt/comments/modernComment_1.xml", ContentTypes.PRESENTATIONML_MODERN_COMMENTS,
				"http://schemas.microsoft.com/office/2018/10/relationships/comments",
				"<p188:cmLst xmlns:p188=\"" + p188 + "\" xmlns:a=\"" + A + "\"><p188:cm id=\"{8F3CEB8C-E68C-084F-8002-53BA267FBCD2}\""
				+ " authorId=\"{D66C2D0B-8267-47C3-1395-767C976A08A1}\" created=\"2026-05-18T23:01:55.033\">"
				+ "<pc:sldMkLst xmlns:pc=\"http://schemas.microsoft.com/office/powerpoint/2013/main/command\"><pc:docMk/><pc:sldMk cId=\"0\" sldId=\"" + sldId + "\"/></pc:sldMkLst>"
				+ "<p188:pos x=\"1\" y=\"1\"/>"
				+ "<p188:replyLst><p188:reply id=\"{1F3CEB8C-E68C-084F-8002-53BA267FBCD3}\" authorId=\"{D66C2D0B-8267-47C3-1395-767C976A08A1}\""
				+ " created=\"2026-05-19T08:00:00.000\"><p188:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>Reply on Zorbling</a:t></a:r></a:p></p188:txBody></p188:reply></p188:replyLst>"
				+ "<p188:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang=\"en-US\"/>"
				+ "<a:t>Modern comment about Quibblex</a:t></a:r></a:p></p188:txBody>"
				+ "</p188:cm></p188:cmLst>");
		slide.addTargetPart(modernComments);
		return pkg;
	}

	@Test
	public void commentsAndAuthors() throws Exception {
		PresentationMLPackage pkg = commentsDeck();
		CommentAuthorsPart authors = (CommentAuthorsPart) part(pkg, "/ppt/commentAuthors.xml");
		CommentsPart comments = (CommentsPart) part(pkg, "/ppt/comments/comment1.xml");
		XmlPart modernAuthors = (XmlPart) part(pkg, "/ppt/authors.xml");
		XmlPart modernComments = (XmlPart) part(pkg, "/ppt/comments/modernComment_1.xml");

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Zorbling", "Quibblex", "Jane", "Reviewer", "Bob", "Editor", "zorbling.example", "2024-06-15", "2026-05-1");

		// the parts survive, renamed consistently (which author is 1 depends on the part order)
		assertEquals(Action.SCRUBBED, action(r, "/ppt/commentAuthors.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/ppt/comments/comment1.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/ppt/authors.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/ppt/comments/modernComment_1.xml").action);
		String legacyName = authors.getContents().getCmAuthor().get(0).getName();
		assertTrue(legacyName, legacyName.matches("Author [12]"));
		assertTrue(authors.getContents().getCmAuthor().get(0).getInitials().matches("A[12]"));
		String legacyXml = authors.getXML();
		assertTrue(legacyXml, legacyXml.contains("userId=\"" + legacyName + "\""));
		assertTrue(legacyXml, legacyXml.contains("providerId=\"None\""));
		assertEquals(Names.FIXED_DATE.toXMLFormat(), comments.getContents().getCm().get(0).getDt().toXMLFormat());
		String modern = XmlUtils.w3CDomNodeToString(modernAuthors.getDocument());
		String modernName = legacyName.equals("Author 1") ? "Author 2" : "Author 1";
		assertTrue(modern, modern.contains("name=\"" + modernName + "\""));
		assertTrue(modern, modern.contains("userId=\"" + modernName + "\""));
		assertTrue(modern, modern.contains("providerId=\"None\""));
		String cm = XmlUtils.w3CDomNodeToString(modernComments.getDocument());
		assertTrue(cm, cm.contains("created=\"" + Names.FIXED_DATE.toXMLFormat() + "\""));
		assertTrue("the reply is still there", cm.contains("p188:reply "));
		assertEquals(2, r.getAuthorsRenamed());
		reload(pkg);
	}

	// ---- gaps 2 and 3: text everywhere, notes, external targets, tags, sections, shows, protection, names

	static PresentationMLPackage textDeck() throws Exception {
		PresentationMLPackage pkg = deck();
		MainPresentationPart pp = pkg.getMainPresentationPart();
		SlidePart slide = slide(pkg);

		addShape(slide, titleShape(2, "Zorbling Corp results"));
		// a hyperlink run with a tooltip, and a run in Cyrillic
		Relationship link = externalRel(slide, Namespaces.HYPERLINK, "https://zorbling.example.com/secret");
		addShape(slide, textBox(3,
				"<a:r><a:rPr lang=\"en-US\"><a:hlinkClick r:id=\"" + link.getId() + "\" tooltip=\"Visit Zorbling\"/></a:rPr><a:t>Quibblex revenue</a:t></a:r>"
				+ "<a:r><a:rPr lang=\"ru-RU\"/><a:t>Секретный отчёт</a:t></a:r>"));
		slide.getContents().getCSld().setName("Acme custom slide");
		layout(pkg).getContents().getCSld().setName("Acme layout");

		// speaker notes
		NotesMasterPart nmp = pp.getNotesMasterPart(true);
		NotesSlidePart notes = slide.createNotesSlidePart(nmp);
		notes.addNoteTextPara("Remind them about the Grendlewick deal", "en-AU");

		// a tags part, referenced from the slide
		TagsPart tags = new TagsPart(new PartName("/ppt/tags/tag1.xml"));
		tags.setContents((TagLst) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<p:tagLst " + NS + "><p:tag name=\"ACMEADDIN\" val=\"customer=Zorbling\"/></p:tagLst>", Context.jcPML)));
		Relationship tagRel = slide.addTargetPart(tags);
		slide.getContents().getCSld().setCustDataLst((org.pptx4j.pml.CTCustomerDataList) XmlUtils.unmarshalString(
				"<p:custDataLst " + NS + "><p:tags r:id=\"" + tagRel.getId() + "\"/></p:custDataLst>", Context.jcPML,
				org.pptx4j.pml.CTCustomerDataList.class));

		// section names, a custom show, the modify verifier, in the presentation part
		Presentation pres = pp.getContents();
		pres.setCustShowLst((org.pptx4j.pml.CTCustomShowList) XmlUtils.unmarshalString(
				"<p:custShowLst " + NS + "><p:custShow name=\"Board only show\" id=\"0\"><p:sldLst><p:sld r:id=\""
				+ pp.getContents().getSldIdLst().getSldId().get(0).getRid() + "\"/></p:sldLst></p:custShow></p:custShowLst>",
				Context.jcPML, org.pptx4j.pml.CTCustomShowList.class));
		pres.setModifyVerifier((org.pptx4j.pml.CTModifyVerifier) XmlUtils.unmarshalString(
				"<p:modifyVerifier " + NS + " algorithmName=\"SHA-512\" hashValue=\"c2VjcmV0aGFzaA==\" saltValue=\"c2FsdA==\" spinValue=\"100000\"/>",
				Context.jcPML, org.pptx4j.pml.CTModifyVerifier.class));
		pres.setExtLst((org.pptx4j.pml.CTExtensionList) XmlUtils.unmarshalString(
				"<p:extLst " + NS + "><p:ext uri=\"{521415D9-36F7-43E2-AB2F-B90AF26B5E84}\">"
				+ "<p14:sectionLst xmlns:p14=\"http://schemas.microsoft.com/office/powerpoint/2010/main\">"
				+ "<p14:section name=\"Confidential Grendlewick section\" id=\"{2B8C7A5E-1E2F-4E8B-9C7D-3A1B5C6D7E8F}\"><p14:sldIdLst>"
				+ "<p14:sldId id=\"" + pp.getContents().getSldIdLst().getSldId().get(0).getId() + "\"/></p14:sldIdLst></p14:section>"
				+ "</p14:sectionLst></p:ext></p:extLst>", Context.jcPML, org.pptx4j.pml.CTExtensionList.class));

		// a table style with a name
		TableStylesPart tableStyles = new TableStylesPart(new PartName("/ppt/tableStyles.xml"));
		tableStyles.setContents(new org.docx4j.dml.ObjectFactory().createTblStyleLst((org.docx4j.dml.CTTableStyleList) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<a:tblStyleLst xmlns:a=\"" + A + "\" def=\"{5C22544A-7EE6-4342-B048-85BDC9FD1C3A}\">"
				+ "<a:tblStyle styleId=\"{5C22544A-7EE6-4342-B048-85BDC9FD1C3A}\" styleName=\"Zorbling House Table\"/></a:tblStyleLst>",
				Context.jcPML))));
		pp.addTargetPart(tableStyles);
		return pkg;
	}

	@Test
	public void textNotesTargetsTagsSectionsAndNames() throws Exception {
		PresentationMLPackage pkg = textDeck();
		MainPresentationPart pp = pkg.getMainPresentationPart();
		Presentation pres = pp.getContents();
		SlidePart slide = slide(pkg);
		TagsPart tags = (TagsPart) part(pkg, "/ppt/tags/tag1.xml");
		Relationship link = null;
		for (Relationship rel : slide.getRelationshipsPart().getRelationships().getRelationship()) {
			if ("External".equals(rel.getTargetMode())) link = rel;
		}
		assertNotNull(link);

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Zorbling", "Quibblex", "Grendlewick", "Acme", "ACMEADDIN", "Board only", "Confidential",
				"Секретный", "Visit", "zorbling.example.com", "secret", "c2VjcmV0aGFzaA", "c2FsdA", "modifyVerifier");

		// structure: the hyperlink is still a hyperlink to somewhere invalid, the tag, section and
		// custom show survive with other names, the notes slide is still there
		assertTrue(link.getTarget(), link.getTarget().startsWith(MetadataScrubber.EXTERNAL_PLACEHOLDER));
		assertEquals(1, r.getExternalTargetsReplaced());
		assertNotNull(part(pkg, "/ppt/tags/tag1.xml"));
		assertEquals(1, tags.getContents().getTag().size());
		assertEquals(1, pres.getCustShowLst().getCustShow().size());
		assertNull(pres.getModifyVerifier());
		assertTrue(pp.getXML(), pp.getXML().contains("p14:section "));
		assertNotNull(part(pkg, "/ppt/notesSlides/notesSlide1.xml"));
		assertTrue(r.hasCyrillic);
		assertTrue(r.getNotes().toString(), r.getNotes().contains("p:modifyVerifier removed (password hash and salt)"));
		reload(pkg);
	}

	// ---- gap 4: media, objects, fonts - STRICT removes with a footprint, KEEP keeps and reports

	/** an entry of a zip resource (the embedded workbook of loadAndSave.pptx) */
	static byte[] zipEntry(String resource, String entry) throws Exception {
		try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
				new ByteArrayInputStream(AnonymizeProbesTest.resource(resource)))) {
			java.util.zip.ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (e.getName().equals(entry)) return zis.readAllBytes();
			}
		}
		throw new AssertionError(entry + " not in " + resource);
	}

	/** the media deck with everything, including the parts no real bytes exist for (the tests) */
	static PresentationMLPackage mediaDeck() throws Exception {
		return mediaDeck(true);
	}

	/**
	 * A deck with an OLE object (a real embedded workbook, EMF preview), a video (a real
	 * mp4, poster picture, p14:media), a transition sound (a real wav) and, if asked, an
	 * ActiveX control and an embedded font whose bytes are junk (docx4j never reads them;
	 * PowerPoint would, so the deck for the PowerPoint check leaves them out).
	 */
	static PresentationMLPackage mediaDeck(boolean withUnreadable) throws Exception {
		PresentationMLPackage pkg = deck();
		MainPresentationPart pp = pkg.getMainPresentationPart();
		SlidePart slide = slide(pkg);
		addShape(slide, titleShape(2, "Objects and media"));

		// an OLE object: the embedding (a package the tool cannot read) and its preview picture
		EmbeddedPackagePart xlsx = new EmbeddedPackagePart(new PartName("/ppt/embeddings/Microsoft_Excel_Worksheet1.xlsx"));
		xlsx.setContentType(new ContentType(ContentTypes.SPREADSHEETML_WORKBOOK));
		xlsx.setBinaryData(zipEntry("loadAndSave.pptx", "ppt/embeddings/Microsoft_Excel_Worksheet.xlsx"));
		Relationship xlsxRel = slide.addTargetPart(xlsx);
		// the preview is an EMF, as PowerPoint writes it
		org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart preview =
				new org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart(new PartName("/ppt/media/image1.emf"));
		preview.setBinaryData(AnonymizeProbesTest.resource("anon/probe.emf"));
		Relationship previewRel = slide.addTargetPart(preview);
		addShape(slide, XmlUtils.unmarshalString(
				"<p:graphicFrame " + NS + "><p:nvGraphicFramePr><p:cNvPr id=\"4\" name=\"Object 3\"/><p:cNvGraphicFramePr/><p:nvPr/></p:nvGraphicFramePr>"
				+ "<p:xfrm><a:off x=\"2000000\" y=\"2000000\"/><a:ext cx=\"3000000\" cy=\"2000000\"/></p:xfrm>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/presentationml/2006/ole\">"
				+ "<mc:AlternateContent xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
				+ "<mc:Choice xmlns:v=\"urn:schemas-microsoft-com:vml\" Requires=\"v\">"
				+ "<p:oleObj spid=\"_x0000_s1026\" name=\"Zorbling Worksheet\" r:id=\"" + xlsxRel.getId() + "\" imgW=\"3000000\" imgH=\"2000000\" progId=\"Excel.Sheet.12\"><p:embed/></p:oleObj>"
				+ "</mc:Choice><mc:Fallback>"
				+ "<p:oleObj name=\"Zorbling Worksheet\" r:id=\"" + xlsxRel.getId() + "\" imgW=\"3000000\" imgH=\"2000000\" progId=\"Excel.Sheet.12\"><p:embed/>"
				+ "<p:pic><p:nvPicPr><p:cNvPr id=\"0\" name=\"\"/><p:cNvPicPr/><p:nvPr/></p:nvPicPr>"
				+ "<p:blipFill><a:blip r:embed=\"" + previewRel.getId() + "\"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>"
				+ "<p:spPr><a:xfrm><a:off x=\"2000000\" y=\"2000000\"/><a:ext cx=\"3000000\" cy=\"2000000\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></p:spPr></p:pic>"
				+ "</p:oleObj></mc:Fallback></mc:AlternateContent></a:graphicData></a:graphic></p:graphicFrame>",
				Context.jcPML, org.pptx4j.pml.CTGraphicalObjectFrame.class));

		// a video: the media part, its poster picture, a:videoFile and p14:media
		BinaryPart mp4 = binaryPart("/ppt/media/media1.mp4", "video/mp4",
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/video", AnonymizeProbesTest.resource("anon/probe.mp4"));
		Relationship videoRel = slide.addTargetPart(mp4);
		Relationship mediaRel = rel("http://schemas.microsoft.com/office/2007/relationships/media", "../media/media1.mp4");
		slide.getRelationshipsPart().addRelationship(mediaRel);
		ImagePngPart poster = new ImagePngPart(new PartName("/ppt/media/image2.png"));
		poster.setBinaryData(MediaReplacer.PNG_IMAGE_DATA);
		Relationship posterRel = slide.addTargetPart(poster);
		addShape(slide, XmlUtils.unmarshalString(
				"<p:pic " + NS + "><p:nvPicPr><p:cNvPr id=\"5\" name=\"Zorbling launch video\"/><p:cNvPicPr/>"
				+ "<p:nvPr><a:videoFile r:link=\"" + videoRel.getId() + "\"/><p:extLst><p:ext uri=\"{DAA4B4D4-6D71-4841-9C94-3DE7FCFB9230}\">"
				+ "<p14:media xmlns:p14=\"http://schemas.microsoft.com/office/powerpoint/2010/main\" r:embed=\"" + mediaRel.getId() + "\"/>"
				+ "</p:ext></p:extLst></p:nvPr></p:nvPicPr>"
				+ "<p:blipFill><a:blip r:embed=\"" + posterRel.getId() + "\"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>"
				+ "<p:spPr><a:xfrm><a:off x=\"6000000\" y=\"2000000\"/><a:ext cx=\"2000000\" cy=\"1500000\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></p:spPr></p:pic>",
				Context.jcPML, org.pptx4j.pml.Pic.class));

		// a transition sound
		BinaryPart wav = binaryPart("/ppt/media/audio1.wav", "audio/x-wav",
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/audio", AnonymizeProbesTest.resource("anon/probe.wav"));
		Relationship wavRel = slide.addTargetPart(wav);
		slide.getContents().setTransition((org.pptx4j.pml.CTSlideTransition) XmlUtils.unmarshalString(
				"<p:transition " + NS + "><p:fade/><p:sndAc><p:stSnd><p:snd r:embed=\"" + wavRel.getId() + "\" name=\"zorbling-jingle.wav\"/></p:stSnd></p:sndAc></p:transition>",
				Context.jcPML, org.pptx4j.pml.CTSlideTransition.class));

		if (!withUnreadable) return pkg;

		// an ActiveX control
		ActiveXControlXmlPart activeX = new ActiveXControlXmlPart(new PartName("/ppt/activeX/activeX1.xml"));
		activeX.setDocument(new ByteArrayInputStream(("<ax:ocx xmlns:ax=\"http://schemas.microsoft.com/office/2006/activeX\" "
				+ "ax:classid=\"{8BD21D10-EC42-11CE-9E0D-00AA006002F3}\" ax:persistence=\"persistPropertyBag\">"
				+ "<ax:ocxPr ax:name=\"Value\" ax:value=\"Zorbling secret control value\"/></ax:ocx>").getBytes(StandardCharsets.UTF_8)));
		Relationship axRel = slide.addTargetPart(activeX);
		slide.getContents().getCSld().setControls((org.pptx4j.pml.CTControlList) XmlUtils.unmarshalString(
				"<p:controls " + NS + "><p:control spid=\"_x0000_s1027\" name=\"Zorbling TextBox1\" r:id=\"" + axRel.getId() + "\" imgW=\"100\" imgH=\"100\"/></p:controls>",
				Context.jcPML, org.pptx4j.pml.CTControlList.class));

		// an embedded font
		FontDataPart font = new FontDataPart(new PartName("/ppt/fonts/font1.fntdata"));
		font.setBinaryData("not really a font".getBytes(StandardCharsets.UTF_8));
		Relationship fontRel = pp.addTargetPart(font);
		pp.getContents().setEmbeddedFontLst((org.pptx4j.pml.CTEmbeddedFontList) XmlUtils.unmarshalString(
				"<p:embeddedFontLst " + NS + "><p:embeddedFont><p:font typeface=\"Zorbling Sans\" pitchFamily=\"34\" charset=\"0\"/>"
				+ "<p:regular r:id=\"" + fontRel.getId() + "\"/></p:embeddedFont></p:embeddedFontLst>",
				Context.jcPML, org.pptx4j.pml.CTEmbeddedFontList.class));

		return pkg;
	}

	static Relationship rel(String type, String target) {
		Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType(type);
		rel.setTarget(target);
		return rel;
	}

	@Test
	public void mediaObjectsAndFontsStrict() throws Exception {
		PresentationMLPackage pkg = mediaDeck();
		SlidePart slide = slide(pkg);
		MainPresentationPart pp = pkg.getMainPresentationPart();

		AnonymizeResult r = new Anonymize(pkg).go();
		assertClean(r);
		String all = allXml(pkg);
		assertGone(all, "Zorbling", "jingle", "secret control", "oleObj", "videoFile", "p14:media", "sndAc", "p:control",
				"embeddedFontLst", "activeX", "fntdata", "media1.mp4", "audio1.wav", "embeddings", "image1.emf");
		assertGone(pp.getXML(), "embeddedFont");

		// the parts are gone
		assertNull(part(pkg, "/ppt/embeddings/Microsoft_Excel_Worksheet1.xlsx"));
		assertNull(part(pkg, "/ppt/media/media1.mp4"));
		assertNull(part(pkg, "/ppt/media/audio1.wav"));
		assertNull(part(pkg, "/ppt/activeX/activeX1.xml"));
		assertNull(part(pkg, "/ppt/fonts/font1.fntdata"));
		assertEquals(Action.REMOVED, action(r, "/ppt/embeddings/Microsoft_Excel_Worksheet1.xlsx").action);
		assertEquals(Action.REMOVED, action(r, "/ppt/fonts/font1.fntdata").action);

		// the footprints: the OLE frame is now a picture with the labelled placeholder; the
		// video's poster is a plain picture; the slide still has its transition
		String slideXml = slide.getXML();
		assertTrue(slideXml, slideXml.contains("name=\"Object 4\""));
		assertFalse(slideXml, slideXml.contains("graphicFrame"));
		Part labelled = part(pkg, MediaReplacer.objectPlaceholderPartName(pkg));
		assertNotNull("the labelled placeholder, under /ppt/media", labelled);
		assertEquals("/ppt/media/anon-object-removed.png", labelled.getPartName().getName());
		assertNull("the EMF preview is gone", part(pkg, "/ppt/media/image1.emf"));
		assertEquals(Action.REPLACED, action(r, "/ppt/media/image1.emf").action);
		assertTrue(action(r, "/ppt/media/image1.emf").reason, action(r, "/ppt/media/image1.emf").reason.contains("anon-object-removed"));
		assertNotNull(part(pkg, "/ppt/media/image2.png"));
		assertEquals("the poster is plain pixels", Action.REPLACED, action(r, "/ppt/media/image2.png").action);
		assertTrue(slideXml, slideXml.contains("<p:transition"));
		assertTrue(slideXml, slideXml.contains("<p:fade"));
		assertNull(pp.getContents().getEmbeddedFontLst());
		assertNull(slide.getContents().getCSld().getControls());

		// no relationship of the slide points at a part which is gone
		for (Relationship rel : slide.getRelationshipsPart().getRelationships().getRelationship()) {
			if ("External".equals(rel.getTargetMode())) continue;
			assertNotNull(rel.getTarget() + " dangles", slide.getRelationshipsPart().getPart(rel));
		}
		reload(pkg);
	}

	@Test
	public void mediaObjectsAndFontsKeep() throws Exception {
		PresentationMLPackage pkg = mediaDeck();
		AnonymizeResult r = new Anonymize(pkg, Anonymize.Mode.KEEP).go();
		assertFalse(r.isClean());
		assertNotNull(part(pkg, "/ppt/embeddings/Microsoft_Excel_Worksheet1.xlsx"));
		assertNotNull(part(pkg, "/ppt/media/media1.mp4"));
		assertNotNull(part(pkg, "/ppt/fonts/font1.fntdata"));
		boolean embedding = false, video = false, font = false;
		for (PartAction a : r.getKeptUnsafe()) {
			if (a.partName.startsWith("/ppt/embeddings/")) embedding = true;
			if (a.partName.endsWith(".mp4")) video = true;
			if (a.partName.endsWith(".fntdata")) font = true;
		}
		assertTrue(r.getKeptUnsafe().toString(), embedding && video && font);
		// the markup stays with its parts, but the text and names were still scrambled
		String slideXml = slide(pkg).getXML();
		assertTrue(slideXml, slideXml.contains("oleObj"));
		assertTrue(slideXml, slideXml.contains("videoFile"));
		assertGone(slideXml, "Zorbling");
		reload(pkg);
	}

	// ---- the package kinds

	@Test
	public void onlyTheThreePackageKinds() throws Exception {
		// docx, pptx and xlsx (since phase 3); anything else is refused
		try {
			new Anonymize((OpcPackage) null, Anonymize.Mode.STRICT);
			assertTrue("expected IllegalArgumentException", false);
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("docx, pptx and xlsx"));
		}
		assertNotNull(new Anonymize(org.docx4j.openpackaging.packages.SpreadsheetMLPackage.createPackage()));
	}

	@Test
	public void slideshowMainPartIsRecognised() throws Exception {
		// a .ppsx: its main part's content type differs, and until 17.2.1 loaded as a BinaryPart
		PresentationMLPackage pkg = deck();
		addShape(slide(pkg), titleShape(2, "Zorbling show"));
		pkg.getContentTypeManager().addOverrideContentType(new PartName("/ppt/presentation.xml"), ContentTypes.PRESENTATIONML_SLIDESHOW);
		PresentationMLPackage again = reload(pkg);
		assertTrue(part(again, "/ppt/presentation.xml") instanceof MainPresentationPart);
		AnonymizeResult r = new Anonymize(again).go();
		assertClean(r);
		assertGone(allXml(again), "Zorbling");
		assertTrue(part(again, "/ppt/presentation.xml") instanceof MainPresentationPart);
	}

}
