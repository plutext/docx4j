package org.docx4j.convert.in.xhtml;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import static org.junit.Assert.*;

import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPrInner;
import org.docx4j.wml.Tr;
import org.junit.Test;
import org.junit.Before;

import java.math.BigInteger;
import java.util.List;

public class XHTMLImporterTest {

	private WordprocessingMLPackage wordMLPackage;

	@Before
	public void setup() throws InvalidFormatException {
		wordMLPackage = WordprocessingMLPackage.createPackage();
	}

	@Test public void testVerticalCellMerging() throws Docx4JException {

		List<Object> tbl = table(
				"<tr><td>1</td><td rowspan='2'>2</td></tr>" +
				"<tr><td>3</td></tr>"
			);
		assertEquals("table has two rows", 2, tbl.size());

		List<Object> tr1 = ((Tr) tbl.get(0)).getContent();
		List<Object> tr2 = ((Tr) tbl.get(1)).getContent();
		assertEquals("two cells in each row", 2, tr1.size());
		assertEquals("two cells in each row", 2, tr2.size());

		assertNull("no vMerge in first column", vmerge(tr1, 0));
		assertNull("no vMerge in first column", vmerge(tr2, 0));

		TcPrInner.VMerge vMerge1_2 = vmerge(tr1, 1);
		TcPrInner.VMerge vMerge2_2 = vmerge(tr2, 1);
		assertNotNull("vMerge exists in second column", vMerge1_2);
		assertNotNull("vMerge exists in second column", vMerge2_2);

		assertEquals("vMerge val is 'restart' in first row", "restart", vMerge1_2.getVal());
		assertNull("vMerge val is null in second row", vMerge2_2.getVal());
	}

	@Test public void testRepeatedVerticalCellMerging() throws Docx4JException {

		List<Object> tbl = table(
				"<tr><td rowspan='2'>1</td><td rowspan='2'>2</td><td>3</td></tr>" +
				"<tr><td>4</td></tr>"
			);
		List<Object> tr2 = ((Tr) tbl.get(1)).getContent();
		assertEquals("three cells in second row", 3, tr2.size());

		assertNotNull("vMerge exists in first column", vmerge(tr2, 0));
		assertNotNull("vMerge exists in second column", vmerge(tr2, 1));
		assertNull("no vMerge in third column", vmerge(tr2, 2));
	}

	@Test public void testTrailingRepeatedVerticalCellMerging() throws Docx4JException {

		List<Object> tbl = table(
				"<tr><td>1</td><td rowspan='2'>2</td><td rowspan='2'>3</td></tr>" +
				"<tr><td>4</td></tr>"
			);
		List<Object> tr2 = ((Tr) tbl.get(1)).getContent();
		assertEquals("three cells in second row", 3, tr2.size());

		assertNull("no vMerge in first column", vmerge(tr2, 0));
		assertNotNull("vMerge exists in second column", vmerge(tr2, 1));
		assertNotNull("vMerge exists in third column", vmerge(tr2, 2));
	}

	@Test public void testVerticalAndHorizontalCellMerging() throws Docx4JException {

		List<Object> tbl = table(
				"<tr><td rowspan='2' colspan='2'>1</td><td>2</td></tr>" +
				"<tr><td>3</td></tr>"
			);
		List<Object> tr2 = ((Tr) tbl.get(1)).getContent();
		assertEquals("two cells in second row", 2, tr2.size());

		assertNotNull("vMerge exists in first column", vmerge(tr2, 0));
		assertNull("no vMerge in second column", vmerge(tr2, 1));

		TcPrInner.GridSpan gridSpan = ((Tc) tr2.get(0)).getTcPr().getGridSpan();
		assertNotNull("gridSpan exists in first column", gridSpan);
		assertEquals(BigInteger.valueOf(2), gridSpan.getVal());
	}

	private List<Object> convert(String xhtml) throws Docx4JException {
		return XHTMLImporter.convert(xhtml, "", wordMLPackage);
	}

	private List<Object> table(String tableContent) throws Docx4JException {
		List<Object> converted = convert("<div><table>" +tableContent + "</table></div>");
		return ((Tbl) converted.get(1)).getContent();
	}

	private TcPrInner.VMerge vmerge(List<Object> row, int col) {
		return ((Tc) row.get(col)).getTcPr().getVMerge();
	}
}
