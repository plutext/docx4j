package org.docx4j.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TableStyleConditions.Look;
import org.docx4j.sharedtypes.STOnOff;
import org.docx4j.wml.CTCnf;
import org.docx4j.wml.CTTblLook;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.Style;
import org.docx4j.wml.TrPr;
import org.junit.Test;

/**
 * The arithmetic of a table style's conditional formatting (ECMA-376-1 17.7.6): which
 * conditions a cell is under, from {@code w:tblLook}, {@code w:cnfStyle} and position, and
 * the order the matching {@code w:tblStylePr} entries are applied in.
 *
 * @since 17.1.1
 */
public class TableStyleConditionsTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final Look ALL = new Look(true, true, true, true, true, true);

	private static EnumSet<STTblStyleOverrideType> set(STTblStyleOverrideType... types) {
		EnumSet<STTblStyleOverrideType> s = EnumSet.noneOf(STTblStyleOverrideType.class);
		for (STTblStyleOverrideType t : types) s.add(t);
		return s;
	}

	private static CTCnf cnf(String val) {
		CTCnf c = Context.getWmlObjectFactory().createCTCnf();
		c.setVal(val);
		return c;
	}

	// ---- w:tblLook

	@Test
	public void lookFromAttributes() {
		CTTblLook l = Context.getWmlObjectFactory().createCTTblLook();
		l.setFirstRow(STOnOff.ONE);
		l.setLastRow(STOnOff.ZERO);
		l.setFirstColumn(STOnOff.TRUE);
		l.setLastColumn(STOnOff.OFF);
		l.setNoHBand(STOnOff.ZERO);
		l.setNoVBand(STOnOff.ONE);
		l.setVal("0000"); // the attributes win over the bitmask where both are present
		Look look = Look.of(l);
		assertTrue(look.firstRow);
		assertFalse(look.lastRow);
		assertTrue(look.firstColumn);
		assertFalse(look.lastColumn);
		assertTrue(look.hBand);
		assertFalse(look.vBand);
	}

	@Test
	public void lookFromLegacyBitmask() {
		CTTblLook l = Context.getWmlObjectFactory().createCTTblLook();
		l.setVal("04A0"); // Word's default: firstRow | firstColumn | noVBand
		Look look = Look.of(l);
		assertTrue(look.firstRow);
		assertFalse(look.lastRow);
		assertTrue(look.firstColumn);
		assertFalse(look.lastColumn);
		assertTrue(look.hBand);
		assertFalse(look.vBand);

		l.setVal("01E0"); // firstRow | lastRow | firstColumn | lastColumn, both bandings on
		look = Look.of(l);
		assertTrue(look.firstRow && look.lastRow && look.firstColumn && look.lastColumn);
		assertTrue(look.hBand && look.vBand);

		l.setVal("0600"); // noHBand | noVBand, nothing else
		look = Look.of(l);
		assertFalse(look.firstRow || look.lastRow || look.firstColumn || look.lastColumn);
		assertFalse(look.hBand || look.vBand);
	}

	@Test
	public void lookAbsentIsWordsDefault() {
		Look look = Look.of(null);
		assertTrue(look.firstRow);
		assertTrue(look.firstColumn);
		assertTrue(look.hBand);
		assertFalse(look.lastRow || look.lastColumn || look.vBand);
		assertEquals(Look.DEFAULT.toString(), TableStyleConditions.look(null).toString());
	}

	// ---- w:cnfStyle

	@Test
	public void cnfBits() {
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW), TableStyleConditions.fromCnf("100000000000"));
		assertEquals(set(STTblStyleOverrideType.LAST_ROW), TableStyleConditions.fromCnf("010000000000"));
		assertEquals(set(STTblStyleOverrideType.FIRST_COL), TableStyleConditions.fromCnf("001000000000"));
		assertEquals(set(STTblStyleOverrideType.LAST_COL), TableStyleConditions.fromCnf("000100000000"));
		assertEquals(set(STTblStyleOverrideType.BAND_1_VERT), TableStyleConditions.fromCnf("000010000000"));
		assertEquals(set(STTblStyleOverrideType.BAND_2_VERT), TableStyleConditions.fromCnf("000001000000"));
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ), TableStyleConditions.fromCnf("000000100000"));
		assertEquals(set(STTblStyleOverrideType.BAND_2_HORZ), TableStyleConditions.fromCnf("000000010000"));
		assertEquals(set(STTblStyleOverrideType.NW_CELL), TableStyleConditions.fromCnf("000000001000"));
		assertEquals(set(STTblStyleOverrideType.NE_CELL), TableStyleConditions.fromCnf("000000000100"));
		assertEquals(set(STTblStyleOverrideType.SW_CELL), TableStyleConditions.fromCnf("000000000010"));
		assertEquals(set(STTblStyleOverrideType.SE_CELL), TableStyleConditions.fromCnf("000000000001"));
		assertTrue(TableStyleConditions.fromCnf("000000000000").isEmpty());
		assertTrue(TableStyleConditions.fromCnf((String) null).isEmpty());
		assertTrue(TableStyleConditions.fromCnf((CTCnf) null).isEmpty());
	}

	// ---- band arithmetic from position

	@Test
	public void bandsSkipTheFirstRowAndColumnWhichHaveTheirOwnCondition() {
		// 4 rows x 3 cols, band size 1, Word's default look
		Look look = Look.DEFAULT;
		// row 0: header, no band
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.NW_CELL),
				TableStyleConditions.atPosition(look, 1, 1, 0, 4, 0, 1, 3));
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW),
				TableStyleConditions.atPosition(look, 1, 1, 0, 4, 1, 1, 3));
		// row 1 is the first banded row: band1
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.FIRST_COL),
				TableStyleConditions.atPosition(look, 1, 1, 1, 4, 0, 1, 3));
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ),
				TableStyleConditions.atPosition(look, 1, 1, 1, 4, 1, 1, 3));
		assertEquals(set(STTblStyleOverrideType.BAND_2_HORZ),
				TableStyleConditions.atPosition(look, 1, 1, 2, 4, 2, 1, 3));
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ),
				TableStyleConditions.atPosition(look, 1, 1, 3, 4, 2, 1, 3));
		// no vertical banding under the default look
		assertFalse(TableStyleConditions.atPosition(look, 1, 1, 2, 4, 2, 1, 3)
				.contains(STTblStyleOverrideType.BAND_1_VERT));
	}

	@Test
	public void bandsCountFromRowZeroWhereThereIsNoFirstRowCondition() {
		Look look = new Look(false, false, false, false, true, true);
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.BAND_1_VERT),
				TableStyleConditions.atPosition(look, 1, 1, 0, 4, 0, 1, 3));
		assertEquals(set(STTblStyleOverrideType.BAND_2_HORZ, STTblStyleOverrideType.BAND_2_VERT),
				TableStyleConditions.atPosition(look, 1, 1, 1, 4, 1, 1, 3));
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.BAND_1_VERT),
				TableStyleConditions.atPosition(look, 1, 1, 2, 4, 2, 1, 3));
	}

	@Test
	public void bandSizes() {
		Look look = new Look(true, false, true, false, true, true);
		// row band size 2: rows 1,2 -> band1; 3,4 -> band2; 5 -> band1
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 1, 6, 1, 1, 8).contains(STTblStyleOverrideType.BAND_1_HORZ));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 2, 6, 1, 1, 8).contains(STTblStyleOverrideType.BAND_1_HORZ));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 3, 6, 1, 1, 8).contains(STTblStyleOverrideType.BAND_2_HORZ));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 4, 6, 1, 1, 8).contains(STTblStyleOverrideType.BAND_2_HORZ));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 5, 6, 1, 1, 8).contains(STTblStyleOverrideType.BAND_1_HORZ));
		// column band size 3: cols 1,2,3 -> band1; 4,5,6 -> band2; 7 -> band1
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 1, 6, 1, 1, 8).contains(STTblStyleOverrideType.BAND_1_VERT));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 1, 6, 3, 1, 8).contains(STTblStyleOverrideType.BAND_1_VERT));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 1, 6, 4, 1, 8).contains(STTblStyleOverrideType.BAND_2_VERT));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 1, 6, 6, 1, 8).contains(STTblStyleOverrideType.BAND_2_VERT));
		assertTrue(TableStyleConditions.atPosition(look, 2, 3, 1, 6, 7, 1, 8).contains(STTblStyleOverrideType.BAND_1_VERT));

		CTTblPrBase tblPr = Context.getWmlObjectFactory().createTblPr();
		assertEquals(1, TableStyleConditions.rowBandSize(tblPr));
		assertEquals(1, TableStyleConditions.colBandSize(null));
		CTTblPrBase.TblStyleRowBandSize rbs = Context.getWmlObjectFactory().createCTTblPrBaseTblStyleRowBandSize();
		rbs.setVal(java.math.BigInteger.valueOf(2));
		tblPr.setTblStyleRowBandSize(rbs);
		assertEquals(2, TableStyleConditions.rowBandSize(tblPr));
	}

	@Test
	public void lastRowAndColumnAndTheCorners() {
		// 3x3, every look on
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.NW_CELL),
				TableStyleConditions.atPosition(ALL, 1, 1, 0, 3, 0, 1, 3));
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.LAST_COL, STTblStyleOverrideType.NE_CELL),
				TableStyleConditions.atPosition(ALL, 1, 1, 0, 3, 2, 1, 3));
		assertEquals(set(STTblStyleOverrideType.LAST_ROW, STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.SW_CELL),
				TableStyleConditions.atPosition(ALL, 1, 1, 2, 3, 0, 1, 3));
		assertEquals(set(STTblStyleOverrideType.LAST_ROW, STTblStyleOverrideType.LAST_COL, STTblStyleOverrideType.SE_CELL),
				TableStyleConditions.atPosition(ALL, 1, 1, 2, 3, 2, 1, 3));
		// the middle: the first banded row and column, since the outer ones are excluded
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.BAND_1_VERT),
				TableStyleConditions.atPosition(ALL, 1, 1, 1, 3, 1, 1, 3));
		// a spanning cell reaching the last column is in it
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.LAST_COL, STTblStyleOverrideType.NE_CELL),
				TableStyleConditions.atPosition(ALL, 1, 1, 0, 3, 1, 2, 3));
		// last row and column are not banded
		assertFalse(TableStyleConditions.atPosition(ALL, 1, 1, 2, 3, 1, 1, 3).contains(STTblStyleOverrideType.BAND_1_HORZ));
		assertFalse(TableStyleConditions.atPosition(ALL, 1, 1, 2, 3, 1, 1, 3).contains(STTblStyleOverrideType.BAND_2_HORZ));
	}

	// ---- gating, and the caches

	@Test
	public void lookGatesEverything() {
		EnumSet<STTblStyleOverrideType> all = EnumSet.allOf(STTblStyleOverrideType.class);
		all.remove(STTblStyleOverrideType.WHOLE_TABLE);
		EnumSet<STTblStyleOverrideType> gated = TableStyleConditions.gate(EnumSet.copyOf(all), Look.NONE);
		assertTrue(gated.isEmpty());
		gated = TableStyleConditions.gate(EnumSet.copyOf(all), Look.DEFAULT);
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.FIRST_COL,
				STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.BAND_2_HORZ, STTblStyleOverrideType.NW_CELL),
				gated);
	}

	@Test
	public void cachesAreCombinedPerAxisAndGated() {
		// Word's layout: the row bits on w:tr, the column bits on w:tc, the row bits again on w:p
		EnumSet<STTblStyleOverrideType> r = TableStyleConditions.resolve(Look.DEFAULT, 1, 1,
				5, 9, 0, 1, 4, cnf("000000100000"), cnf("001000000000"), cnf("000000100000"));
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.FIRST_COL), r);

		// the caches win over the position where they are present ...
		r = TableStyleConditions.resolve(Look.DEFAULT, 1, 1,
				5, 9, 0, 1, 4, cnf("100000000000"), cnf("001000000000"), null);
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.NW_CELL), r);

		// ... but a stale cache cannot switch on what the look has turned off
		r = TableStyleConditions.resolve(new Look(false, false, true, false, true, false), 1, 1,
				0, 9, 0, 1, 4, cnf("100000000000"), cnf("001000000000"), null);
		assertEquals(set(STTblStyleOverrideType.FIRST_COL), r);

		// an axis with no cache is computed: no w:tc cache here, column 0 is the first column
		r = TableStyleConditions.resolve(Look.DEFAULT, 1, 1,
				2, 9, 0, 1, 4, cnf("000000010000"), null, null);
		assertEquals(set(STTblStyleOverrideType.BAND_2_HORZ, STTblStyleOverrideType.FIRST_COL), r);

		// no caches at all: position alone
		r = TableStyleConditions.resolve(Look.DEFAULT, 1, 1, 0, 9, 3, 1, 4, null, null, null);
		assertEquals(set(STTblStyleOverrideType.FIRST_ROW), r);

		// an empty paragraph cache (Word writes 000000000000) does not silence the row's
		r = TableStyleConditions.resolve(Look.DEFAULT, 1, 1,
				3, 9, 1, 1, 4, cnf("000000100000"), cnf("000000000000"), cnf("000000000000"));
		assertEquals(set(STTblStyleOverrideType.BAND_1_HORZ), r);
	}

	@Test
	public void rowCnfIsReadFromTheTrPr() {
		TrPr trPr = Context.getWmlObjectFactory().createTrPr();
		assertNull(TableStyleConditions.rowCnf(trPr));
		trPr.getCnfStyleOrDivIdOrGridBefore().add(
				Context.getWmlObjectFactory().createCTTrPrBaseCnfStyle(cnf("100000000000")));
		assertEquals("100000000000", TableStyleConditions.rowCnf(trPr).getVal());
		assertFalse(TableStyleConditions.hasTblHeader(trPr));
		trPr.getCnfStyleOrDivIdOrGridBefore().add(
				Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(Context.getWmlObjectFactory().createBooleanDefaultTrue()));
		assertTrue(TableStyleConditions.hasTblHeader(trPr));
	}

	// ---- precedence

	private static Style styleWith(String... types) throws Exception {
		StringBuilder sb = new StringBuilder("<w:style " + W + " w:type=\"table\" w:styleId=\"T\">");
		for (String t : types) {
			sb.append("<w:tblStylePr w:type=\"" + t + "\"><w:rPr><w:b/></w:rPr></w:tblStylePr>");
		}
		sb.append("</w:style>");
		return (Style) XmlUtils.unmarshalString(sb.toString());
	}

	@Test
	public void applicableEntriesComeInPrecedenceOrderWhateverTheStyleOrder() throws Exception {
		// the style lists them backwards; the result must be the 17.7.6 order
		Style s = styleWith("seCell", "swCell", "neCell", "nwCell", "lastRow", "firstRow", "lastCol",
				"firstCol", "band2Horz", "band1Horz", "band2Vert", "band1Vert", "wholeTable");
		EnumSet<STTblStyleOverrideType> all = EnumSet.allOf(STTblStyleOverrideType.class);
		List<CTTblStylePr> out = TableStyleConditions.applicable(s, all);
		assertEquals(13, out.size());
		for (int i = 0; i < out.size(); i++) {
			assertEquals(TableStyleConditions.PRECEDENCE[i], out.get(i).getType());
		}
		// only the conditions the cell is under, wholeTable always
		out = TableStyleConditions.applicable(s, set(STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.BAND_1_VERT));
		assertEquals(3, out.size());
		assertEquals(STTblStyleOverrideType.WHOLE_TABLE, out.get(0).getType());
		assertEquals(STTblStyleOverrideType.BAND_1_VERT, out.get(1).getType());
		assertEquals(STTblStyleOverrideType.FIRST_ROW, out.get(2).getType());
		// nothing defined: nothing
		assertTrue(TableStyleConditions.applicable(styleWith(), all).isEmpty());
		assertTrue(TableStyleConditions.applicable(null, all).isEmpty());
	}

	@Test
	public void laterConditionsOverrideEarlierOnes() throws Exception {
		String xml = "<w:style " + W + " w:type=\"table\" w:styleId=\"T\">"
				+ "<w:tblStylePr w:type=\"firstRow\"><w:pPr><w:jc w:val=\"center\"/></w:pPr>"
				+ "<w:rPr><w:b/><w:color w:val=\"FF0000\"/><w:sz w:val=\"20\"/></w:rPr></w:tblStylePr>"
				+ "<w:tblStylePr w:type=\"firstCol\"><w:pPr><w:jc w:val=\"right\"/></w:pPr>"
				+ "<w:rPr><w:i/><w:color w:val=\"00FF00\"/></w:rPr></w:tblStylePr>"
				+ "<w:tblStylePr w:type=\"nwCell\"><w:rPr><w:color w:val=\"0000FF\"/></w:rPr></w:tblStylePr>"
				+ "<w:tblStylePr w:type=\"band1Horz\"><w:rPr><w:sz w:val=\"16\"/><w:u w:val=\"single\"/></w:rPr></w:tblStylePr>"
				+ "</w:style>";
		Style s = (Style) XmlUtils.unmarshalString(xml);

		// the top-left cell: firstCol, then firstRow, then nwCell
		List<CTTblStylePr> app = TableStyleConditions.applicable(s,
				set(STTblStyleOverrideType.FIRST_COL, STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.NW_CELL));
		org.docx4j.wml.RPr rPr = TableStyleConditions.conditionalRPr(app);
		org.docx4j.wml.PPr pPr = TableStyleConditions.conditionalPPr(app);
		assertTrue(rPr.getB().isVal());          // firstRow
		assertTrue(rPr.getI().isVal());          // firstCol survives, firstRow does not restate it
		assertEquals("0000FF", rPr.getColor().getVal()); // nwCell, last, wins
		assertEquals(20, rPr.getSz().getVal().intValue());
		assertEquals(org.docx4j.wml.JcEnumeration.CENTER, pPr.getJc().getVal()); // firstRow after firstCol

		// a first-column cell in an odd band: band1Horz first, firstCol over it
		app = TableStyleConditions.applicable(s, set(STTblStyleOverrideType.BAND_1_HORZ, STTblStyleOverrideType.FIRST_COL));
		rPr = TableStyleConditions.conditionalRPr(app);
		assertEquals(16, rPr.getSz().getVal().intValue());  // from the band
		assertEquals("00FF00", rPr.getColor().getVal());    // firstCol
		assertTrue(rPr.getI().isVal());
		assertNull(rPr.getB());
		assertEquals(org.docx4j.wml.JcEnumeration.RIGHT, TableStyleConditions.conditionalPPr(app).getJc().getVal());

		assertNull(TableStyleConditions.conditionalTcPr(app));
		assertNull(TableStyleConditions.conditionalTrPr(app));

		assertEquals("firstCol-firstRow-nwCell", TableStyleConditions.key(
				set(STTblStyleOverrideType.NW_CELL, STTblStyleOverrideType.FIRST_ROW, STTblStyleOverrideType.FIRST_COL)));
		assertEquals("", TableStyleConditions.key(null));
	}
}
