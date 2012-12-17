package org.docx4j.convert.in.xhtml;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import static org.junit.Assert.*;

import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPrInner;
import org.docx4j.wml.Tr;
import org.junit.Test;

import java.util.List;

public class XHTMLImporterTest {

	@Test public void testVerticalCellMerging() throws Docx4JException {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		String xhtml =
			"<div><table>" +
				"<tr><td>1</td><td rowspan='2'>2</td></tr>" +
				"<tr><td>3</td></tr>" +
			"</table></div>";
		List<Object> converted = XHTMLImporter.convert(xhtml, "", wordMLPackage);

		List<Object> tbl = ((Tbl) converted.get(1)).getContent();
		assertEquals("table has two rows", tbl.size(), 2);

		List<Object> tr1 = ((Tr) tbl.get(0)).getContent();
		List<Object> tr2 = ((Tr) tbl.get(1)).getContent();
		assertEquals("two cells in each row", tr1.size(), 2);
		assertEquals("two cells in each row", tr2.size(), 2);

		TcPrInner.VMerge vMerge1_1 = ((Tc) tr1.get(0)).getTcPr().getVMerge();
		TcPrInner.VMerge vMerge2_1 = ((Tc) tr2.get(0)).getTcPr().getVMerge();
		assertNull("no vMerge in first column", vMerge1_1);
		assertNull("no vMerge in first column", vMerge2_1);

		TcPrInner.VMerge vMerge1_2 = ((Tc) tr1.get(1)).getTcPr().getVMerge();
		TcPrInner.VMerge vMerge2_2 = ((Tc) tr2.get(1)).getTcPr().getVMerge();
		assertNotNull("vMerge exists in second column", vMerge1_2);
		assertNotNull("vMerge exists in second column", vMerge2_2);

		assertEquals("vMerge val is 'restart' in first row", vMerge1_2.getVal(), "restart");
		assertNull("vMerge val is null in second row", vMerge2_2.getVal());
	}
}
