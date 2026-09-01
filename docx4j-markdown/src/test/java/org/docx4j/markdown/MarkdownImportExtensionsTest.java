package org.docx4j.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.junit.Test;

/**
 * Phase 2 (import extensions + images) mapping assertions.
 */
public class MarkdownImportExtensionsTest {

	private WordprocessingMLPackage convert(String md) throws Exception {
		return new MarkdownImporter().createPackage(md);
	}

	private static List<Object> content(WordprocessingMLPackage pkg) {
		return pkg.getMainDocumentPart().getContent();
	}

	private static String text(Object o) {
		StringBuilder sb = new StringBuilder();
		org.docx4j.TraversalUtil.visit(o, new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object child) {
				child = XmlUtils.unwrap(child);
				if (child instanceof Text) {
					sb.append(((Text) child).getValue());
				}
				return null;
			}
		});
		return sb.toString();
	}

	// ------------------------------------------------------------- tables

	private static final String TABLE_MD =
			"| Name | Qty |\n|------|----:|\n| ant  | 1   |\n| bee  | 22  |\n";

	@Test
	public void gfmTable() throws Exception {
		WordprocessingMLPackage pkg = convert(TABLE_MD);
		Tbl tbl = (Tbl) content(pkg).get(0);
		assertEquals("TableGrid", tbl.getTblPr().getTblStyle().getVal());
		assertEquals(2, tbl.getTblGrid().getGridCol().size());

		List<Tr> rows = new ArrayList<>();
		for (Object o : tbl.getContent()) {
			rows.add((Tr) XmlUtils.unwrap(o));
		}
		assertEquals(3, rows.size());

		// header row: marked tblHeader, cells bold
		Tr header = rows.get(0);
		assertNotNull(header.getTrPr());
		boolean tblHeader = false;
		for (Object o : header.getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
			if (((jakarta.xml.bind.JAXBElement<?>) o).getName().getLocalPart().equals("tblHeader")) {
				tblHeader = true;
			}
		}
		assertTrue(tblHeader);
		P headerCellP = firstCellP(header, 0);
		R headerRun = (R) headerCellP.getContent().get(0);
		assertTrue(headerRun.getRPr().getB().isVal());

		// per-column alignment from the delimiter row: Qty column is right-aligned
		P qtyCellP = firstCellP(rows.get(1), 1);
		assertEquals(JcEnumeration.RIGHT, qtyCellP.getPPr().getJc().getVal());
		P nameCellP = firstCellP(rows.get(1), 0);
		assertNull(nameCellP.getPPr()); // LEFT needs no jc

		assertEquals("ant", text(nameCellP));
		assertEquals("22", text(firstCellP(rows.get(2), 1)));
	}

	private static P firstCellP(Tr tr, int cellIndex) {
		List<Tc> cells = new ArrayList<>();
		for (Object o : tr.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof Tc) {
				cells.add((Tc) u);
			}
		}
		return (P) cells.get(cellIndex).getContent().get(0);
	}

	@Test
	public void tableGridColumnsHaveWidths() throws Exception {
		// Word tolerates widthless w:gridCol, but the FO/HTML table writers
		// NPE'd on it and both PDF pathways (and HTML) silently dropped the
		// table — so emit an equal split of the section's writable width
		WordprocessingMLPackage pkg = convert(TABLE_MD);
		Tbl tbl = (Tbl) content(pkg).get(0);
		for (org.docx4j.wml.TblGridCol col : tbl.getTblGrid().getGridCol()) {
			assertNotNull(col.getW());
		}
		assertEquals(9026 / 2, // A4 with 1in margins, 2 columns
				tbl.getTblGrid().getGridCol().get(0).getW().intValue());
	}

	@Test
	public void tableSurvivesHtmlExport() throws Exception {
		WordprocessingMLPackage pkg = convert(TABLE_MD);
		String html = toHtml(pkg);
		assertTrue(html.contains("<table"));
		assertTrue(html.contains("ant"));
		assertTrue(html.contains("22"));
	}

	@Test
	public void widthlessGridColsToleratedByExport() throws Exception {
		// other producers (and hand-built docs) may omit w:w: the table
		// writers must not drop the table over it
		WordprocessingMLPackage pkg = convert(TABLE_MD);
		Tbl tbl = (Tbl) content(pkg).get(0);
		for (org.docx4j.wml.TblGridCol col : tbl.getTblGrid().getGridCol()) {
			col.setW(null);
		}
		String html = toHtml(pkg);
		assertTrue(html.contains("<table"));
		assertTrue(html.contains("ant"));
	}

	private static String toHtml(WordprocessingMLPackage pkg) throws Exception {
		org.docx4j.convert.out.HTMLSettings settings = org.docx4j.Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		org.docx4j.Docx4J.toHTML(settings, out, org.docx4j.Docx4J.FLAG_EXPORT_PREFER_NONXSL);
		return out.toString("UTF-8");
	}

	@Test
	public void extensionsCanBeDisabled() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		new MarkdownImporter(new MarkdownImportOptions()
				.setExtensions(EnumSet.noneOf(MarkdownImportOptions.Extension.class)))
				.importToMainDocumentPart(TABLE_MD, pkg);
		// without the tables extension, the pipes are just paragraph text
		for (Object o : content(pkg)) {
			assertTrue(o instanceof P);
		}
	}

	// ------------------------------------------------------------- strikethrough

	@Test
	public void strikethrough() throws Exception {
		WordprocessingMLPackage pkg = convert("keep ~~gone~~ end\n");
		P p = (P) content(pkg).get(0);
		R struck = (R) p.getContent().get(1);
		assertTrue(struck.getRPr().getStrike().isVal());
		assertEquals("gone", text(struck));
	}

	// ------------------------------------------------------------- task lists

	@Test
	public void taskListItems() throws Exception {
		WordprocessingMLPackage pkg = convert("- [x] done\n- [ ] todo\n");
		P done = (P) content(pkg).get(0);
		// still a real numbered (bullet) list item
		assertNotNull(done.getPPr().getNumPr());
		assertTrue(text(done).startsWith("☒ "));
		P todo = (P) content(pkg).get(1);
		assertTrue(text(todo).startsWith("☐ "));
	}

	// ------------------------------------------------------------- footnotes

	@Test
	public void realFootnotes() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"Body.[^a] And again.[^a]\n\n[^a]: The *note* text\n");

		FootnotesPart footnotesPart = pkg.getMainDocumentPart().getFootnotesPart();
		assertNotNull(footnotesPart);

		// separator, continuationSeparator, and our note
		List<CTFtnEdn> footnotes = footnotesPart.getJaxbElement().getFootnote();
		assertEquals(3, footnotes.size());
		CTFtnEdn note = footnotes.get(2);
		assertEquals(BigInteger.ONE, note.getId());
		assertEquals(" The note text", text(note.getContent().get(0)));
		P notePara = (P) note.getContent().get(0);
		assertEquals("FootnoteText", notePara.getPPr().getPStyle().getVal());

		// both references point at the same footnote
		List<BigInteger> refIds = new ArrayList<>();
		org.docx4j.TraversalUtil.visit(content(pkg), new org.docx4j.TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object child) {
				child = XmlUtils.unwrap(child);
				if (child instanceof CTFtnEdnRef) {
					refIds.add(((CTFtnEdnRef) child).getId());
				}
				return null;
			}
		});
		assertEquals(2, refIds.size());
		assertEquals(BigInteger.ONE, refIds.get(0));
		assertEquals(BigInteger.ONE, refIds.get(1));

		// settings part got footnotePr
		assertNotNull(pkg.getMainDocumentPart().getDocumentSettingsPart()
				.getContents().getFootnotePr());
	}

	// ------------------------------------------------------------- front matter

	@Test
	public void yamlFrontMatterToCoreProperties() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"---\ntitle: My Title\nauthor: A Writer\nkeywords:\n  - alpha\n  - beta\nunknown: x\n---\n\nBody\n");

		org.docx4j.docProps.core.CoreProperties props =
				pkg.getDocPropsCorePart().getJaxbElement();
		assertEquals("My Title", props.getTitle().getValue().getContent().get(0));
		assertEquals("A Writer", props.getCreator().getContent().get(0));
		assertEquals("alpha, beta", props.getKeywords());

		// and the front matter block produced no body content
		assertEquals(1, content(pkg).size());
		assertEquals("Body", text(content(pkg).get(0)));
	}

	// ------------------------------------------------------------- images

	/** 1x1 px PNG. */
	private static final String PNG_DATA_URI = "data:image/png;base64,"
			+ "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

	@Test
	public void dataUriImageEmbedded() throws Exception {
		WordprocessingMLPackage pkg = convert("![tiny](" + PNG_DATA_URI + ")\n");
		P p = (P) content(pkg).get(0);
		R r = (R) p.getContent().get(0);
		assertTrue(XmlUtils.unwrap(r.getContent().get(0)) instanceof org.docx4j.wml.Drawing);
		// and an image part was added
		boolean imagePart = false;
		for (org.docx4j.openpackaging.parts.Part part
				: pkg.getParts().getParts().values()) {
			if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage) {
				imagePart = true;
			}
		}
		assertTrue(imagePart);
	}

	@Test
	public void remoteImageNotFetched() throws Exception {
		WordprocessingMLPackage pkg = convert("![alt](https://example.com/x.png)\n");
		P p = (P) content(pkg).get(0);
		P.Hyperlink h = (P.Hyperlink) p.getContent().get(0);
		assertEquals("alt", text(h));
	}

	// ------------------------------------------------------------- validity

	@Test
	public void extensionsOutputMarshalsAndSaves() throws Exception {
		WordprocessingMLPackage pkg = convert(
				"---\ntitle: T\n---\n\n# H\n\n" + TABLE_MD
				+ "\n~~strike~~ note[^n]\n\n- [x] task\n\n[^n]: nb\n");
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		pkg.save(baos);
		assertTrue(baos.size() > 0);
	}

}
