package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.junit.Test;

/** The accepted view a conversion can ask for (CR-012): what AcceptTrackedChanges leaves. */
public class AcceptTrackedChangesTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";

	private static Body accept(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + NS + "><w:body>" + body + "</w:body></w:document>"));
		AcceptTrackedChanges.process(pkg);
		return pkg.getMainDocumentPart().getJaxbElement().getBody();
	}

	private static String xml(Object o) {
		return XmlUtils.marshaltoString(o, true, false);
	}

	@Test
	public void deletionsGo_insertionsStay() throws Exception {
		Body body = accept("<w:p><w:r><w:t xml:space=\"preserve\">keep </w:t></w:r>"
				+ "<w:del w:id=\"1\" w:author=\"a\"><w:r><w:delText>gone </w:delText></w:r></w:del>"
				+ "<w:ins w:id=\"2\" w:author=\"a\"><w:r><w:t xml:space=\"preserve\">added </w:t></w:r></w:ins>"
				+ "<w:moveFrom w:id=\"3\" w:author=\"a\"><w:r><w:t>moved-from </w:t></w:r></w:moveFrom>"
				+ "<w:moveTo w:id=\"4\" w:author=\"a\"><w:r><w:t>moved-to</w:t></w:r></w:moveTo></w:p>");
		P p = (P) XmlUtils.unwrap(body.getContent().get(0));
		String x = xml(p);
		assertFalse(x, x.contains("w:del"));
		assertFalse(x, x.contains("w:ins"));
		assertFalse(x, x.contains("moveFrom"));
		assertFalse(x, x.contains("moveTo"));
		assertEquals("keep added moved-to", TextUtils.getText(p));
		assertEquals("the runs sit in the paragraph", 3, p.getContent().size());
		for (Object o : p.getContent()) {
			assertTrue(((org.docx4j.wml.R) o).getParent() == p);
		}
	}

	@Test
	public void aDeletedParagraphMarkJoinsWithTheNext() throws Exception {
		Body body = accept("<w:p w14:paraId=\"AAAA0001\"><w:pPr><w:rPr><w:del w:id=\"1\" w:author=\"a\"/></w:rPr></w:pPr>"
				+ "<w:r><w:t xml:space=\"preserve\">first </w:t></w:r></w:p>"
				+ "<w:p w14:paraId=\"BBBB0002\"><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:t>second</w:t></w:r></w:p>"
				+ "<w:p w14:paraId=\"CCCC0003\"><w:r><w:t>third</w:t></w:r></w:p>");
		assertEquals(2, body.getContent().size());
		P joined = (P) XmlUtils.unwrap(body.getContent().get(0));
		assertEquals("first second", TextUtils.getText(joined));
		assertEquals("keyed by the first", "AAAA0001", joined.getParaId());
		assertNotNull("the second's properties: its mark survives", joined.getPPr().getJc());
		assertTrue("no deleted mark left", joined.getPPr().getRPr() == null || joined.getPPr().getRPr().getDel() == null);
		assertEquals("CCCC0003", ((P) XmlUtils.unwrap(body.getContent().get(1))).getParaId());
	}

	@Test
	public void aRunOfDeletedMarksJoinsThemAll() throws Exception {
		Body body = accept("<w:p><w:pPr><w:rPr><w:del w:id=\"1\" w:author=\"a\"/></w:rPr></w:pPr><w:r><w:t>a</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:rPr><w:del w:id=\"2\" w:author=\"a\"/></w:rPr></w:pPr><w:r><w:t>b</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>c</w:t></w:r></w:p>");
		assertEquals(1, body.getContent().size());
		assertEquals("abc", TextUtils.getText(XmlUtils.unwrap(body.getContent().get(0))));
	}

	/**
	 * A deleted mark with text left and a table next: the text joins the table's first cell's first
	 * paragraph, as Word 365's Accept All does (CR-043 section 17, the text-left probe's case 1,
	 * 23 October 2026). Until 17.3.5 the paragraph stayed before the table.
	 */
	@Test
	public void aDeletedMarkBeforeATableJoinsItsFirstCell() throws Exception {
		Body body = accept("<w:p><w:pPr><w:rPr><w:del w:id=\"1\" w:author=\"a\"/></w:rPr></w:pPr><w:r><w:t>a</w:t></w:r></w:p>"
				+ "<w:tbl><w:tr><w:tc><w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>");
		assertEquals(1, body.getContent().size());
		Tbl tbl = (Tbl) XmlUtils.unwrap(body.getContent().get(0));
		assertEquals("acell", TextUtils.getText(tbl));
	}

	@Test
	public void aDeletedRowGoes_andCellContentIsAccepted() throws Exception {
		Body body = accept("<w:tbl><w:tr><w:tc><w:p><w:r><w:t>one</w:t></w:r>"
				+ "<w:del w:id=\"1\" w:author=\"a\"><w:r><w:delText>x</w:delText></w:r></w:del></w:p></w:tc></w:tr>"
				+ "<w:tr><w:trPr><w:del w:id=\"2\" w:author=\"a\"/></w:trPr><w:tc><w:p><w:r><w:t>two</w:t></w:r></w:p></w:tc></w:tr></w:tbl>");
		Tbl tbl = (Tbl) XmlUtils.unwrap(body.getContent().get(0));
		assertEquals(1, tbl.getContent().size());
		String x = xml(tbl);
		assertFalse(x, x.contains("w:del"));
		assertTrue(x, x.contains("one"));
	}

	@Test
	public void contentControlsAndTextBoxesAreReached() throws Exception {
		Body body = accept("<w:sdt><w:sdtContent><w:p><w:r><w:t>in</w:t></w:r>"
				+ "<w:del w:id=\"1\" w:author=\"a\"><w:r><w:delText>x</w:delText></w:r></w:del></w:p></w:sdtContent></w:sdt>"
				+ "<w:p><w:r><w:pict><v:shape xmlns:v=\"urn:schemas-microsoft-com:vml\"><v:textbox><w:txbxContent>"
				+ "<w:p><w:r><w:t>box</w:t></w:r><w:del w:id=\"2\" w:author=\"a\"><w:r><w:delText>y</w:delText></w:r></w:del></w:p>"
				+ "</w:txbxContent></v:textbox></v:shape></w:pict></w:r></w:p>");
		String x = xml(body);
		assertFalse(x, x.contains("w:del"));
		assertTrue(x, x.contains("box"));
		assertTrue(x, x.contains(">in<"));
	}

	@Test
	public void nothingToDoIsNothingChanged() throws Exception {
		String plain = "<w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:t>a</w:t></w:r></w:p><w:p><w:r><w:t>b</w:t></w:r></w:p>";
		Body body = accept(plain);
		List<Object> content = body.getContent();
		assertEquals(2, content.size());
		assertEquals("a", TextUtils.getText(XmlUtils.unwrap(content.get(0))));
	}

	/**
	 * Since 17.3.3 (CR-043 D2) every story is accepted, and the conversion's working copy is
	 * accepted without touching the document it was copied from (which shares its content types,
	 * relationship objects and the parts not copied deep).
	 */
	@Test
	public void everyStoryOnTheWorkingCopyOnly() throws Exception {
		WordprocessingMLPackage original;
		try (java.io.InputStream is = org.docx4j.utils.ResourceUtils.getResource("revisions/cr043/cr043-tracked.docx")) {
			original = WordprocessingMLPackage.load(is);
		}
		int before = org.docx4j.model.revisions.TrackedChanges.in(original).size();
		int parts = original.getParts().getParts().size();
		String types = types(original);
		String rels = rels(original);

		java.util.Set<String> features = new java.util.HashSet<String>();
		features.add(org.docx4j.convert.out.ConversionFeatures.PP_COMMON_DEEP_COPY);
		features.add(org.docx4j.convert.out.ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES);
		WordprocessingMLPackage copy = org.docx4j.convert.out.common.Preprocess.process(original, features);

		assertTrue(copy != original);
		assertEquals("headers, footers, endnotes and text boxes accepted too", 0,
				org.docx4j.model.revisions.TrackedChanges.in(copy).size());
		assertEquals(before, org.docx4j.model.revisions.TrackedChanges.in(original).size());
		assertEquals(parts, original.getParts().getParts().size());
		assertEquals(types, types(original));
		assertEquals(rels, rels(original));
	}

	/** The main part's relationships, by id: target and type. */
	private static String rels(WordprocessingMLPackage pkg) {
		java.util.TreeMap<String, String> out = new java.util.TreeMap<String, String>();
		for (org.docx4j.relationships.Relationship r : pkg.getMainDocumentPart().getRelationshipsPart().getRelationships().getRelationship()) {
			out.put(r.getId(), r.getTarget() + " " + r.getType());
		}
		return out.toString();
	}

	private static String types(WordprocessingMLPackage pkg) throws Exception {
		java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
		pkg.getContentTypeManager().marshal(os);
		// the Default and Override entries: the declarations vary with the marshaller
		return os.toString("UTF-8").replaceAll(" xmlns(:\\w+)?=\"[^\"]*\"", "");
	}
}
