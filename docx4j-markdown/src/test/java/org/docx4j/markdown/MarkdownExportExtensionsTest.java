package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.junit.Test;

/**
 * Phase 4 (export extensions) assertions: span degradation, tracked-changes
 * policies, image extraction.
 */
public class MarkdownExportExtensionsTest {

	private static final String W_NS =
			"xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String export(WordprocessingMLPackage pkg) throws Exception {
		return new MarkdownExporter().export(pkg);
	}

	@Test
	public void gridSpanPadsColumns() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String tblXml = "<w:tbl " + W_NS + ">"
				+ "<w:tr><w:tc><w:tcPr><w:gridSpan w:val=\"2\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>span</w:t></w:r></w:p></w:tc></w:tr>"
				+ "<w:tr><w:tc><w:p><w:r><w:t>a</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>b</w:t></w:r></w:p></w:tc></w:tr>"
				+ "</w:tbl>";
		pkg.getMainDocumentPart().getContent().add(
				(Tbl) XmlUtils.unmarshalString(tblXml, Context.jc, Tbl.class));

		String md = export(pkg);
		// the spanning cell wins top-left; a padding cell keeps 2 columns
		assertEquals("|span||\n|---|---|\n|a|b|\n", md);
	}

	@Test
	public void vMergeContinuationIsEmpty() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String tblXml = "<w:tbl " + W_NS + ">"
				+ "<w:tr><w:tc><w:p><w:r><w:t>h1</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>h2</w:t></w:r></w:p></w:tc></w:tr>"
				+ "<w:tr><w:tc><w:tcPr><w:vMerge w:val=\"restart\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>top</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>r1</w:t></w:r></w:p></w:tc></w:tr>"
				+ "<w:tr><w:tc><w:tcPr><w:vMerge/></w:tcPr><w:p/></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>r2</w:t></w:r></w:p></w:tc></w:tr>"
				+ "</w:tbl>";
		pkg.getMainDocumentPart().getContent().add(
				(Tbl) XmlUtils.unmarshalString(tblXml, Context.jc, Tbl.class));

		assertEquals("|h1|h2|\n|---|---|\n|top|r1|\n||r2|\n", export(pkg));
	}

	@Test
	public void multiParagraphCellFlattensWithBr() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String tblXml = "<w:tbl " + W_NS + ">"
				+ "<w:tr><w:tc><w:p><w:r><w:t>one</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>two</w:t></w:r></w:p></w:tc></w:tr>"
				+ "</w:tbl>";
		pkg.getMainDocumentPart().getContent().add(
				(Tbl) XmlUtils.unmarshalString(tblXml, Context.jc, Tbl.class));

		assertTrue(export(pkg).contains("one<br>two"));
	}

	private static P trackedP() throws Exception {
		String pXml = "<w:p " + W_NS + ">"
				+ "<w:r><w:t xml:space=\"preserve\">keep </w:t></w:r>"
				+ "<w:ins w:id=\"1\" w:author=\"a\" w:date=\"2026-01-01T00:00:00Z\">"
				+ "<w:r><w:t>added</w:t></w:r></w:ins>"
				+ "<w:del w:id=\"2\" w:author=\"a\" w:date=\"2026-01-01T00:00:00Z\">"
				+ "<w:r><w:delText xml:space=\"preserve\"> removed</w:delText></w:r></w:del>"
				+ "</w:p>";
		return (P) XmlUtils.unmarshalString(pXml, Context.jc, P.class);
	}

	@Test
	public void trackedChangesAcceptByDefault() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(trackedP());
		assertEquals("keep added\n", export(pkg));
	}

	@Test
	public void trackedChangesMarkup() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(trackedP());
		String md = new MarkdownExporter(new MarkdownExportOptions()
				.setTrackedChangesPolicy(MarkdownExportOptions.TrackedChangesPolicy.MARKUP))
				.export(pkg);
		assertEquals("keep added~~ removed~~\n", md);
	}

	/** 1x1 px PNG. */
	private static final String PNG_BASE64 =
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

	@Test
	public void imageExtractionToDirectory() throws Exception {
		WordprocessingMLPackage pkg = new MarkdownImporter().createPackage(
				"![tiny](data:image/png;base64," + PNG_BASE64 + ")\n");

		File dir = Files.createTempDirectory("md-images").toFile();
		String md = new MarkdownExporter(new MarkdownExportOptions()
				.setImageDirPath(dir.getAbsolutePath())
				.setImageTargetUri("images"))
				.export(pkg);

		assertTrue(md.contains("![tiny](images/image1.png)"));
		File written = new File(dir, "image1.png");
		assertTrue(written.exists());
		assertEquals(java.util.Base64.getDecoder().decode(PNG_BASE64).length, written.length());
	}

	@Test
	public void imageAsDataUriByDefault() throws Exception {
		WordprocessingMLPackage pkg = new MarkdownImporter().createPackage(
				"![tiny](data:image/png;base64," + PNG_BASE64 + ")\n");
		String md = export(pkg);
		assertTrue(md.contains("![tiny](data:image/png;base64," + PNG_BASE64 + ")"));
		assertFalse(md.contains("hyperlink"));
	}

}
