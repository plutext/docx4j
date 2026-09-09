package org.docx4j.model.structure;

import static org.junit.Assert.assertEquals;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.SectPr;
import org.junit.Test;

/**
 * Word's page takes the headers and footers of the section it begins in.  A continuous
 * section beginning mid-page therefore shows the previous section's, whatever it
 * declares itself (six corpus documents, up to 0.20 of line parity when that was
 * changed); one which begins a page of its own - its first paragraph breaks the page -
 * shows its own.  Measured on a 179-page document whose continuous body section does,
 * and declares its own running header: Word paints it on every body page, where docx4j
 * painted the front matter's.
 */
public class ContinuousSectionHeadersTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static Relationship header(WordprocessingMLPackage pkg, String name) throws Exception {
		HeaderPart hp = new HeaderPart(new PartName("/word/" + name + ".xml"));
		hp.setPackage(pkg);
		hp.setJaxbElement((Hdr) XmlUtils.unmarshalString(
				"<w:hdr " + W + "><w:p><w:r><w:t>" + name + "</w:t></w:r></w:p></w:hdr>", Context.jc, Hdr.class));
		return pkg.getMainDocumentPart().addTargetPart(hp);
	}

	private static SectPr sectPr(String type, Relationship header) {
		SectPr sectPr = Context.getWmlObjectFactory().createSectPr();
		if (type != null) {
			SectPr.Type t = Context.getWmlObjectFactory().createSectPrType();
			t.setVal(type);
			sectPr.setType(t);
		}
		if (header != null) {
			HeaderReference ref = Context.getWmlObjectFactory().createHeaderReference();
			ref.setId(header.getId());
			ref.setType(HdrFtrRef.DEFAULT);
			sectPr.getEGHdrFtrReferences().add(ref);
		}
		return sectPr;
	}

	@Test
	public void aContinuousSectionWhichStartsAPageShowsItsOwnHeader() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Relationship a = header(pkg, "headerA"), b = header(pkg, "headerB");
		HeaderFooterPolicy first = new HeaderFooterPolicy(sectPr(null, a), null,
				pkg.getMainDocumentPart().getRelationshipsPart(), null);
		assertEquals("/word/headerA.xml", first.getDefaultHeader().getPartName().getName());
		HeaderFooterPolicy continuous = new HeaderFooterPolicy(sectPr("continuous", b), first,
				pkg.getMainDocumentPart().getRelationshipsPart(), null, true);
		assertEquals("the section's own header, not the one before",
				"/word/headerB.xml", continuous.getDefaultHeader().getPartName().getName());
	}

	@Test
	public void aContinuousSectionBeginningMidPageShowsThePreviousOnes() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Relationship a = header(pkg, "headerA"), b = header(pkg, "headerB");
		HeaderFooterPolicy first = new HeaderFooterPolicy(sectPr(null, a), null,
				pkg.getMainDocumentPart().getRelationshipsPart(), null);
		HeaderFooterPolicy continuous = new HeaderFooterPolicy(sectPr("continuous", b), first,
				pkg.getMainDocumentPart().getRelationshipsPart(), null);
		assertEquals("no page begins in it, so its own header shows nowhere",
				"/word/headerA.xml", continuous.getDefaultHeader().getPartName().getName());
	}

	@Test
	public void aContinuousSectionDeclaringNoneInherits() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Relationship a = header(pkg, "headerA");
		HeaderFooterPolicy first = new HeaderFooterPolicy(sectPr(null, a), null,
				pkg.getMainDocumentPart().getRelationshipsPart(), null);
		HeaderFooterPolicy continuous = new HeaderFooterPolicy(sectPr("continuous", null), first,
				pkg.getMainDocumentPart().getRelationshipsPart(), null);
		assertEquals("/word/headerA.xml", continuous.getDefaultHeader().getPartName().getName());
	}
}
