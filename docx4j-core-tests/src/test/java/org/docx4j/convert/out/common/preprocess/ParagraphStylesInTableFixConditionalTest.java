package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.finders.ClassFinder;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.Test;

/**
 * {@link ParagraphStylesInTableFix} with a table style's conditional formatting
 * ({@code w:tblStylePr}, issue #546): one synthetic paragraph style per combination of the
 * conditions a paragraph is under, with the {@code w:basedOn} chain merged per condition,
 * gated by {@code w:tblLook}, in the precedence order of ECMA-376-1 17.7.6, below the
 * paragraph's own style and below direct formatting.
 *
 * @since 17.1.1
 */
public class ParagraphStylesInTableFixConditionalTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"20\"/></w:rPr></w:rPrDefault>"
			+ "<w:pPrDefault><w:pPr><w:spacing w:after=\"200\"/></w:pPr></w:pPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"NoBold\"><w:name w:val=\"No Bold\"/>"
			+ "<w:basedOn w:val=\"Normal\"/><w:rPr><w:b w:val=\"0\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			// the base: 9pt everywhere; header bold red and repeating; first column italic;
			// odd bands underlined; last row struck; the top-left corner blue
			+ "<w:style w:type=\"table\" w:styleId=\"Base\"><w:name w:val=\"Base\"/><w:basedOn w:val=\"TableNormal\"/>"
			+ "<w:rPr><w:sz w:val=\"18\"/></w:rPr>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/><w:color w:val=\"FF0000\"/></w:rPr>"
			+ "<w:trPr><w:tblHeader/></w:trPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"firstCol\"><w:rPr><w:i/></w:rPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"band1Horz\"><w:rPr><w:u w:val=\"single\"/></w:rPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"lastRow\"><w:rPr><w:strike/></w:rPr></w:tblStylePr>"
			+ "<w:tblStylePr w:type=\"nwCell\"><w:rPr><w:color w:val=\"0000FF\"/></w:rPr></w:tblStylePr>"
			+ "</w:style>"
			// a child restating one condition, as a corpus style based on PlainTable1 does
			+ "<w:style w:type=\"table\" w:customStyle=\"1\" w:styleId=\"Child\"><w:name w:val=\"Child\"/>"
			+ "<w:basedOn w:val=\"Base\"/>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b w:val=\"0\"/></w:rPr></w:tblStylePr>"
			+ "</w:style>"
			+ "</w:styles>";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String p(String pStyle, String runRPr, String text) {
		return "<w:p>" + (pStyle == null ? "" : "<w:pPr><w:pStyle w:val=\"" + pStyle + "\"/></w:pPr>")
				+ "<w:r>" + (runRPr == null ? "" : "<w:rPr>" + runRPr + "</w:rPr>") + "<w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String tc(String content) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>" + content + "</w:tc>";
	}

	/** Table 1: Child, the attribute form of w:tblLook with lastRow on, no w:cnfStyle, a nested table in (1,1). */
	private static final String TABLE1 = "<w:tbl><w:tblPr><w:tblStyle w:val=\"Child\"/>"
			+ "<w:tblLook w:val=\"0000\" w:firstRow=\"1\" w:lastRow=\"1\" w:firstColumn=\"1\" w:lastColumn=\"0\" w:noHBand=\"0\" w:noVBand=\"1\"/>"
			+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("t1 r0 c0")) + tc(p("t1 r0 c1")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t1 r1 c0")) + tc(
					p(null, "<w:u w:val=\"none\"/>", "t1 r1 c1 direct")
					+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"Base\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>"
					+ "<w:tr>" + tc(p("nested r0 c0")) + "</w:tr></w:tbl>"
					+ p("t1 r1 c1 after")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t1 r2 c0")) + tc(p("t1 r2 c1")) + "</w:tr>"
			+ "</w:tbl>";

	/** Table 2: Base, the legacy bitmask 0000 - no first row/column, both bandings on. */
	private static final String TABLE2 = "<w:tbl><w:tblPr><w:tblStyle w:val=\"Base\"/>"
			+ "<w:tblLook w:val=\"0000\"/>"
			+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("t2 r0 c0")) + tc(p("t2 r0 c1")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t2 r1 c0")) + tc(p("t2 r1 c1")) + "</w:tr>"
			+ "</w:tbl>";

	/** Table 3: Base, no w:tblLook at all (Word's default look), a w:cnfStyle cache which
	 *  contradicts the position on row 1, and a paragraph style which restates bold. */
	private static final String TABLE3 = "<w:tbl><w:tblPr><w:tblStyle w:val=\"Base\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc(p("t3 r0 c0")) + tc(p("NoBold", null, "t3 r0 c1")) + "</w:tr>"
			+ "<w:tr><w:trPr><w:cnfStyle w:val=\"100000000000\"/></w:trPr>"
			+ tc(p("t3 r1 c0")) + tc(p("t3 r1 c1")) + "</w:tr>"
			+ "<w:tr>" + tc(p("t3 r2 c0")) + tc(p("t3 r2 c1")) + "</w:tr>"
			+ "</w:tbl>";

	private static final String DOC = "<w:document " + W + "><w:body>"
			+ p("outside") + TABLE1 + p("between") + TABLE2 + p("between") + TABLE3
			+ "</w:body></w:document>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(DOC));
		pkg.getMainDocumentPart().getStyleDefinitionsPart()
				.setContents((Styles) XmlUtils.unmarshalString(STYLES));
		ParagraphStylesInTableFix.process(pkg);
		return pkg;
	}

	private static Style style(WordprocessingMLPackage pkg, String id) throws Exception {
		return pkg.getMainDocumentPart().getStyleDefinitionsPart().getStyleById(id);
	}

	/** The paragraph whose text starts with the given prefix. */
	private static P para(WordprocessingMLPackage pkg, String text) throws Exception {
		ClassFinder finder = new ClassFinder(P.class);
		new org.docx4j.TraversalUtil(pkg.getMainDocumentPart().getContents(), finder);
		for (Object o : finder.results) {
			P p = (P) o;
			if (org.docx4j.TextUtils.getText(p).startsWith(text)) return p;
		}
		throw new AssertionError("no paragraph " + text);
	}

	private static String pStyle(P p) {
		return p.getPPr() == null || p.getPPr().getPStyle() == null ? null : p.getPPr().getPStyle().getVal();
	}

	private static R firstRun(P p) {
		for (Object o : p.getContent()) {
			if (o instanceof R) return (R) o;
		}
		return null;
	}

	@Test
	public void oneStylePerConditionCombination() throws Exception {
		WordprocessingMLPackage pkg = pkg();

		assertEquals("Normal", pStyle(para(pkg, "outside")));
		// the top-left cell is under firstCol, firstRow and nwCell, named in precedence order
		assertEquals("Normal-Child-firstCol-firstRow-nwCell-BR", pStyle(para(pkg, "t1 r0 c0")));
		assertEquals("Normal-Child-firstRow-BR", pStyle(para(pkg, "t1 r0 c1")));
		// row 1 is the first banded row
		assertEquals("Normal-Child-band1Horz-firstCol-BR", pStyle(para(pkg, "t1 r1 c0")));
		assertEquals("Normal-Child-band1Horz-BR", pStyle(para(pkg, "t1 r1 c1 direct")));
		assertEquals("Normal-Child-band1Horz-BR", pStyle(para(pkg, "t1 r1 c1 after")));
		// the last row is not banded, and swCell is not defined so is not named
		assertEquals("Normal-Child-firstCol-lastRow-BR", pStyle(para(pkg, "t1 r2 c0")));
		assertEquals("Normal-Child-lastRow-BR", pStyle(para(pkg, "t1 r2 c1")));
	}

	@Test
	public void precedenceAndInheritanceInTheSyntheticStyle() throws Exception {
		WordprocessingMLPackage pkg = pkg();

		// top-left: the Base's 9pt everywhere; firstCol's italic; firstRow's bold restated
		// off by Child (inheritance merges per condition, so Child's firstRow keeps Base's
		// colour); then nwCell's blue over firstRow's red
		RPr rPr = style(pkg, "Normal-Child-firstCol-firstRow-nwCell-BR").getRPr();
		assertEquals(18, rPr.getSz().getVal().intValue());
		assertTrue(rPr.getI().isVal());
		assertFalse(rPr.getB().isVal());
		assertEquals("0000FF", rPr.getColor().getVal());
		assertNull(rPr.getU());

		rPr = style(pkg, "Normal-Child-firstRow-BR").getRPr();
		assertFalse(rPr.getB().isVal());
		assertEquals("FF0000", rPr.getColor().getVal());
		assertNull(rPr.getI());

		rPr = style(pkg, "Normal-Child-band1Horz-firstCol-BR").getRPr();
		assertEquals(UnderlineEnumeration.SINGLE, rPr.getU().getVal());
		assertTrue(rPr.getI().isVal());
		assertNull(rPr.getB());
		assertNull(rPr.getColor());

		rPr = style(pkg, "Normal-Child-firstCol-lastRow-BR").getRPr();
		assertTrue(rPr.getStrike().isVal());
		assertTrue(rPr.getI().isVal());
		assertNull(rPr.getU());

		// the synthetic style is a paragraph style with no w:basedOn, docDefaults folded in
		Style s = style(pkg, "Normal-Child-firstRow-BR");
		assertEquals("paragraph", s.getType());
		assertNull(s.getBasedOn());
		assertEquals(200, s.getPPr().getSpacing().getAfter().intValue());
	}

	@Test
	public void tblLookGatesTheConditions() throws Exception {
		WordprocessingMLPackage pkg = pkg();

		// table 2: w:val="0000" - no first row or column, so row 0 is simply the first band
		assertEquals("Normal-Base-band1Horz-BR", pStyle(para(pkg, "t2 r0 c0")));
		assertEquals("Normal-Base-band1Horz-BR", pStyle(para(pkg, "t2 r0 c1")));
		assertEquals("Normal-Base-BR", pStyle(para(pkg, "t2 r1 c0")));   // band2Horz is not defined
		RPr rPr = style(pkg, "Normal-Base-band1Horz-BR").getRPr();
		assertNull(rPr.getB());
		assertNull(rPr.getI());
		assertEquals(UnderlineEnumeration.SINGLE, rPr.getU().getVal());
		// a table under no condition at all keeps the old style id and gets the Base's 9pt
		assertEquals(18, style(pkg, "Normal-Base-BR").getRPr().getSz().getVal().intValue());
	}

	@Test
	public void noTblLookIsWordsDefaultAndTheCacheWins() throws Exception {
		WordprocessingMLPackage pkg = pkg();

		// table 3 has no w:tblLook: first row, first column and row banding, as Word assumes
		assertEquals("Normal-Base-firstCol-firstRow-nwCell-BR", pStyle(para(pkg, "t3 r0 c0")));
		assertTrue(style(pkg, "Normal-Base-firstCol-firstRow-nwCell-BR").getRPr().getB().isVal());
		// row 1 carries a w:cnfStyle saying firstRow: the cache is believed over the position
		assertEquals("Normal-Base-firstCol-firstRow-nwCell-BR", pStyle(para(pkg, "t3 r1 c0")));
		assertEquals("Normal-Base-firstRow-BR", pStyle(para(pkg, "t3 r1 c1")));
		// row 2 has none, and is the second row after the header: band 2, which is not defined
		assertEquals("Normal-Base-firstCol-BR", pStyle(para(pkg, "t3 r2 c0")));
		assertEquals("Normal-Base-BR", pStyle(para(pkg, "t3 r2 c1")));
	}

	@Test
	public void paragraphStyleAndDirectFormattingWin() throws Exception {
		WordprocessingMLPackage pkg = pkg();

		// the paragraph's own style sits above the condition: NoBold's b=0 over firstRow's b
		P p = para(pkg, "t3 r0 c1");
		assertEquals("NoBold-Base-firstRow-BR", pStyle(p));
		RPr rPr = style(pkg, "NoBold-Base-firstRow-BR").getRPr();
		assertFalse(rPr.getB().isVal());
		assertEquals("FF0000", rPr.getColor().getVal()); // what the style does not restate survives

		// direct formatting sits above everything: the run's u=none over band1Horz's u=single
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
		P direct = para(pkg, "t1 r1 c1 direct");
		RPr effective = resolver.getEffectiveRPr(firstRun(direct).getRPr(), direct.getPPr());
		assertEquals(UnderlineEnumeration.NONE, effective.getU().getVal());
		assertEquals(18, effective.getSz().getVal().intValue());
		// and a run with none gets the band's underline
		P plain = para(pkg, "t1 r1 c1 after");
		effective = resolver.getEffectiveRPr(firstRun(plain).getRPr(), plain.getPPr());
		assertEquals(UnderlineEnumeration.SINGLE, effective.getU().getVal());
		// while the header row's effective run properties are bold-off (Child) and red
		P header = para(pkg, "t1 r0 c1");
		effective = resolver.getEffectiveRPr(firstRun(header).getRPr(), header.getPPr());
		assertFalse(effective.getB().isVal());
		assertEquals("FF0000", effective.getColor().getVal());
	}

	@Test
	public void nestedTableUsesItsOwnStyleAndPosition() throws Exception {
		WordprocessingMLPackage pkg = pkg();

		// the nested table is Base (bold header) at row 0 of its own table, whatever the
		// outer cell's conditions
		assertEquals("Normal-Base-firstCol-firstRow-nwCell-BR", pStyle(para(pkg, "nested r0 c0")));
		assertTrue(style(pkg, "Normal-Base-firstCol-firstRow-nwCell-BR").getRPr().getB().isVal());
		// and the outer cell's paragraph after it is still in the outer table's band
		assertEquals("Normal-Child-band1Horz-BR", pStyle(para(pkg, "t1 r1 c1 after")));
	}

	@Test
	public void theEffectiveTableStyleInheritsConditionsPerType() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();

		ClassFinder finder = new ClassFinder(Tbl.class);
		new org.docx4j.TraversalUtil(pkg.getMainDocumentPart().getContents(), finder);
		Tbl child = (Tbl) finder.results.get(0);
		Style effective = resolver.getEffectiveTableStyle(child.getTblPr());
		// Child restates firstRow only, and inherits the other four from Base
		assertEquals(5, effective.getTblStylePr().size());
		org.docx4j.wml.CTTblStylePr firstRow = org.docx4j.model.table.TableStyleConditions.lookup(
				effective, org.docx4j.wml.STTblStyleOverrideType.FIRST_ROW);
		assertNotNull(firstRow);
		assertFalse(firstRow.getRPr().getB().isVal());              // Child
		assertEquals("FF0000", firstRow.getRPr().getColor().getVal()); // Base
		assertTrue(org.docx4j.model.table.TableStyleConditions.hasTblHeader(firstRow.getTrPr())); // Base
		assertNotNull(org.docx4j.model.table.TableStyleConditions.lookup(
				effective, org.docx4j.wml.STTblStyleOverrideType.BAND_1_HORZ));

		// the rows and cells are as written: the preprocess renames styles, nothing else
		List<Object> rows = child.getContent();
		assertEquals(3, rows.size());
		Tr row = (Tr) XmlUtils.unwrap(rows.get(0));
		assertEquals(2, row.getContent().size());
		Tc cell = (Tc) XmlUtils.unwrap(row.getContent().get(0));
		assertNotNull(cell.getTcPr().getTcW());
	}
}
