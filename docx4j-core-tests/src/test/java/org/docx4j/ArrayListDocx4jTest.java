package org.docx4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.jaxb.Context;
import org.docx4j.list.ArrayListDocx4j;
import org.docx4j.mce.AlternateContent;
import org.docx4j.wml.Body;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTCustomXmlBlock;
import org.docx4j.wml.CTCustomXmlRun;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.CTSdtRow;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.DelText;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.junit.Test;

import javax.xml.namespace.QName;

/**
 * ArrayListDocx4j: the content lists set the parent of what is added, find a child through its JAXBElement
 * wrapper, and refuse a child that is never legal under the list's parent, at the point of the mistake and with
 * the fix in the message (CR-064, D1 to D7). Each container set is tested with its refused children and with
 * children it accepts, since the lists stay open to everything else (mc:AlternateContent, range markup, the
 * wrapped forms).
 */
public class ArrayListDocx4jTest {

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	/**
	 * Our list semantics are that contains, indexOf, remove
	 * etc should work for an object, irrespective
	 * of whether it is wrapped in a JAXBElement or not.
	 */
	@Test
	public void testContainsJAXBElement() {
		Body b = new Body();
		ArrayListDocx4j list = new ArrayListDocx4j(b);
		CTAltChunk altChunk = new CTAltChunk();
		JAXBElement<CTAltChunk> altChunkWrapper = F.createBodyAltChunk(altChunk);
		list.add(altChunkWrapper);
		assertTrue(list.contains(altChunk));
	}

	// ---- run-level containers (D3, D5, D6): P, P.Hyperlink, CTSimpleField, RunIns, RunDel, CTSmartTagRun, CTCustomXmlRun, CTSdtContentRun

	@Test
	public void aParagraphRefusesTheRunLevelMistakes() {
		P p = new P();
		assertRefused(p, new Tbl(), "table", "sibling");
		assertRefused(p, new P(), "paragraph", "nest");
		assertRefused(p, new Text(), "Text", "run (R)");
		assertRefused(p, F.createRT(new Text()), "wrapped Text (D1: the wrapper is unwrapped before the test)", "run (R)");
		assertRefused(p, new Drawing(), "Drawing", "run (R)");
		assertRefused(p, new Br(), "Br", "run (R)");
		assertRefused(p, new DelText(), "DelText (not a Text subclass; §8.1)", "run (R)");
		assertRefused(p, new JAXBElement<DelText>(new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "delText"), DelText.class, new DelText()), "wrapped DelText", "run (R)");
		assertRefused(p, new R.Tab(), "a run-level Tab (§8.1)", "run (R)");
		assertRefused(p, new SdtBlock(), "a block-level sdt in a paragraph (§8.1)", "SdtRun");
		assertRefused(p, new CTSdtRow(), "a row-level sdt in a paragraph", "table (Tbl)");
		assertRefused(p, new CTSdtCell(), "a cell-level sdt in a paragraph", "table row (Tr)");
		assertRefused(p, new PPr(), "PPr", "setPPr");
		assertRefused(p, "hello", "String", "Text");
		// accepted: a run, a hyperlink, a wrapped hyperlink, a bookmark start, a run-level sdt, mc:AlternateContent
		R r = new R(); p.getContent().add(r); assertSame(p, r.getParent());
		P.Hyperlink h = new P.Hyperlink(); p.getContent().add(F.createPHyperlink(h)); assertSame(p, h.getParent());
		p.getContent().add(F.createBodyBookmarkStart(new CTBookmark()));
		p.getContent().add(new SdtRun());
		p.getContent().add(new AlternateContent());
		assertEquals(5, p.getContent().size());
	}

	@Test
	public void theOtherRunLevelContainersRefuseWhatAParagraphRefuses() {
		P.Hyperlink h = new P.Hyperlink(); CTSimpleField fld = new CTSimpleField(); CTSmartTagRun st = new CTSmartTagRun();
		CTCustomXmlRun cxr = new CTCustomXmlRun(); CTSdtContentRun scr = new CTSdtContentRun();
		Object[][] containers = { {h, h.getContent()}, {fld, fld.getContent()}, {st, st.getContent()}, {cxr, cxr.getContent()}, {scr, scr.getContent()} };
		for (Object[] pair : containers) {
			Object c = pair[0]; @SuppressWarnings("unchecked") List<Object> content = (List<Object>) pair[1];
			assertRefused(content, c, new Tbl(), "table", "sibling");
			assertRefused(content, c, new P(), "paragraph", "nest");
			assertRefused(content, c, new Text(), "Text", "run (R)");
			assertRefused(content, c, new Drawing(), "Drawing", "run (R)");
			assertRefused(content, c, new Br(), "Br", "run (R)");
			assertRefused(content, c, new DelText(), "DelText", "run (R)");
			assertRefused(content, c, new R.Tab(), "Tab", "run (R)");
			assertRefused(content, c, new SdtBlock(), "SdtBlock", "SdtRun");
			assertRefused(content, c, "hello", "String", "Text");
			R r = new R(); content.add(r); assertSame(c, r.getParent());
			assertEquals(1, content.size());
		}
		// RunIns and RunDel are not ContentAccessors; their list is customXmlOrSmartTagOrSdt
		RunIns ins = new RunIns();
		assertRefused(ins.getCustomXmlOrSmartTagOrSdt(), ins, new P(), "paragraph", "nest");
		assertRefused(ins.getCustomXmlOrSmartTagOrSdt(), ins, new Text(), "Text", "run (R)");
		ins.getCustomXmlOrSmartTagOrSdt().add(new R());
		RunDel del = new RunDel();
		assertRefused(del.getCustomXmlOrSmartTagOrSdt(), del, new Tbl(), "table", "sibling");
		del.getCustomXmlOrSmartTagOrSdt().add(new R());
	}

	/** the message names the container, so the reader knows which object was wrong */
	@Test
	public void theMessageNamesTheContainer() {
		assertTrue(refusal(new P.Hyperlink().getContent(), new Text()).contains("hyperlink"));
		assertTrue(refusal(new CTSimpleField().getContent(), new Text()).contains("field"));
		assertTrue(refusal(new Tc().getContent(), new R()).contains("table cell"));
		assertTrue(refusal(new Hdr().getContent(), new R()).contains("header"));
		assertTrue(refusal(new Body().getContent(), new R()).contains("the body"));
	}

	// ---- a run (D5, D6)

	@Test
	public void aRunRefusesARunAParagraphATableItsPropertiesAndAString() {
		R r = new R();
		assertRefused(r, new R(), "run", "nest");
		assertRefused(r, new P(), "paragraph", "nest");
		assertRefused(r, new Tbl(), "table", "table");
		assertRefused(r, new RPr(), "RPr", "setRPr");
		assertRefused(r, new SdtRun(), "an sdt in a run (§8.1)", "beside the run");
		assertRefused(r, new SdtBlock(), "a block sdt in a run", "beside the run");
		assertRefused(r, "hello", "String", "createRT");
		// accepted: wrapped Text, a Drawing, a Br, a Tab
		Text t = new Text(); r.getContent().add(F.createRT(t)); assertSame(r, t.getParent());
		r.getContent().add(new Drawing());
		r.getContent().add(new Br());
		r.getContent().add(F.createRTab(new R.Tab()));
		assertEquals(4, r.getContent().size());
	}

	// ---- block-level containers (D2, D5, D6): Body, Tc, SdtContentBlock, CTTxbxContent, Hdr, Ftr, CTFtnEdn, Comments.Comment, CTCustomXmlBlock

	@Test
	public void blockLevelContainersRefuseRunLevelContentCellsRowsAndProperties() {
		Body body = new Body(); Tc tc = new Tc(); SdtContentBlock scb = new SdtContentBlock(); CTTxbxContent txbx = new CTTxbxContent();
		Hdr hdr = new Hdr(); Ftr ftr = new Ftr(); CTFtnEdn ftn = new CTFtnEdn(); Comments.Comment comment = new Comments.Comment(); CTCustomXmlBlock cxb = new CTCustomXmlBlock();
		Object[][] containers = { {body, body.getContent()}, {tc, tc.getContent()}, {scb, scb.getContent()}, {txbx, txbx.getContent()}, {hdr, hdr.getContent()},
				{ftr, ftr.getContent()}, {ftn, ftn.getContent()}, {comment, comment.getContent()}, {cxb, cxb.getContent()} };
		for (Object[] pair : containers) {
			Object c = pair[0]; @SuppressWarnings("unchecked") List<Object> content = (List<Object>) pair[1];
			assertRefused(content, c, new R(), "run", "paragraph (P)");
			assertRefused(content, c, new Text(), "Text", "run (R)");
			assertRefused(content, c, F.createRT(new Text()), "wrapped Text", "run (R)");
			assertRefused(content, c, new Drawing(), "Drawing", "run (R)");
			assertRefused(content, c, new Br(), "Br", "run (R)");
			assertRefused(content, c, new DelText(), "DelText", "run (R)");
			assertRefused(content, c, new R.Tab(), "Tab", "run (R)");
			assertRefused(content, c, new SdtRun(), "a run-level sdt in block content (§8.1)", "paragraph (P)");
			assertRefused(content, c, new CTSdtRow(), "a row-level sdt in block content", "table (Tbl)");
			assertRefused(content, c, new CTSdtCell(), "a cell-level sdt in block content", "table row (Tr)");
			assertRefused(content, c, new Tc(), "cell", "row (Tr)");
			assertRefused(content, c, new Tr(), "row", "table (Tbl)");
			assertRefused(content, c, new SectPr(), "SectPr", "setSectPr");
			assertRefused(content, c, "hello", "String", "Text");
			// accepted: a paragraph, a table, a block-level sdt, an altChunk, a bookmark, mc:AlternateContent
			P p = new P(); content.add(p); assertSame(c, p.getParent());
			Tbl t = new Tbl(); content.add(t); assertSame(c, t.getParent());
			content.add(new SdtBlock());
			content.add(F.createBodyAltChunk(new CTAltChunk()));
			content.add(F.createBodyBookmarkStart(new CTBookmark()));
			content.add(new AlternateContent());
			assertEquals(6, content.size());
		}
		assertRefused(new Tc(), new TcPr(), "TcPr", "setTcPr");
		// the message names the container (§8.1): a TcPr in the body is not "a cell's content"
		assertTrue(refusal(new Body().getContent(), new TcPr()).contains("the body"));
		assertTrue(refusal(new Hdr().getContent(), new TcPr()).contains("a header"));
	}

	// ---- tables (D4, D5, D6)

	@Test
	public void aTableHoldsRowsAndARowHoldsCells() {
		Tbl tbl = new Tbl();
		assertRefused(tbl, new Tc(), "cell", "row (Tr)");
		assertRefused(tbl, new P(), "paragraph", "cell (Tc)");
		assertRefused(tbl, new R(), "run", "cell (Tc)");
		assertRefused(tbl, new Tbl(), "table", "cell (Tc)");
		assertRefused(tbl, new TblPr(), "TblPr", "setTblPr");
		assertRefused(tbl, "hello", "String", "cell (Tc)");
		assertRefused(tbl, new SdtBlock(), "a block-level sdt in a table (§8.1)", "CTSdtRow");
		assertRefused(tbl, new SdtRun(), "a run-level sdt in a table", "CTSdtRow");
		assertRefused(tbl, new CTSdtCell(), "a cell-level sdt in a table", "CTSdtRow");
		Tr tr = new Tr(); tbl.getContent().add(tr); assertSame(tbl, tr.getParent());
		tbl.getContent().add(new CTSdtRow());
		tbl.getContent().add(F.createBodyBookmarkStart(new CTBookmark()));
		tbl.getContent().add(new AlternateContent());
		assertEquals(4, tbl.getContent().size());

		assertRefused(tr, new P(), "paragraph", "cell (Tc)");
		assertRefused(tr, new R(), "run", "cell (Tc)");
		assertRefused(tr, new Tbl(), "table", "cell (Tc)");
		assertRefused(tr, new TrPr(), "TrPr", "setTrPr");
		assertRefused(tr, "hello", "String", "cell (Tc)");
		assertRefused(tr, new SdtBlock(), "a block-level sdt in a row (§8.1)", "CTSdtCell");
		assertRefused(tr, new CTSdtRow(), "a row-level sdt in a row", "CTSdtCell");
		tr.getContent().add(new CTSdtCell());
		Tc tc = new Tc(); tr.getContent().add(F.createTrTc(tc)); assertSame(tr, tc.getParent());
		tr.getContent().add(new AlternateContent());
		assertEquals(3, tr.getContent().size());

		// a nested table goes in a cell, which is a block-level container
		Tbl nested = new Tbl(); tc.getContent().add(nested); assertSame(tc, nested.getParent());
	}

	// ---- sdt content (unchanged rule)

	@Test
	public void sdtContentRefusesBareSdtContent() {
		SdtContentBlock sc = new SdtContentBlock();
		assertRefused(sc.getContent(), sc, new SdtContentBlock(), "SdtContent", "Sdt");
		SdtBlock sdt = new SdtBlock(); sc.getContent().add(sdt); assertSame(sc, sdt.getParent());
	}

	// ---- the exception type and the other add paths (D7)

	@Test
	public void theRefusalIsAnIllegalArgumentExceptionOnEveryAddPath() {
		P p = new P();
		try { p.getContent().add(new Text()); fail(); } catch (IllegalArgumentException expected) {}
		try { p.getContent().add(0, new Text()); fail(); } catch (IllegalArgumentException expected) {}
		try { p.getContent().addAll(List.of(new R(), new Text())); fail(); } catch (IllegalArgumentException expected) {}
		try { p.getContent().addAll(0, List.of(new Text())); fail(); } catch (IllegalArgumentException expected) {}
		p.getContent().add(new R());
		try { p.getContent().set(0, new Text()); fail(); } catch (IllegalArgumentException expected) {}
		assertEquals("the list is unchanged by a refused add", 1, p.getContent().size());
	}

	// ---- helpers

	private static void assertRefused(ContentAccessor c, Object o, String what, String fixWord) {
		assertRefused(c.getContent(), c, o, what, fixWord);
	}

	private static void assertRefused(List<Object> list, Object container, Object o, String what, String fixWord) {
		int before = list.size();
		try {
			list.add(o);
			fail("a " + what + " was added to " + container.getClass().getSimpleName());
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().startsWith("You can't"));
			assertTrue("the message says what to do instead: " + expected.getMessage(), expected.getMessage().contains(fixWord));
		}
		assertEquals(before, list.size());
	}

	private static String refusal(List<Object> content, Object o) {
		try { content.add(o); fail(); return null; } catch (IllegalArgumentException e) { return e.getMessage(); }
	}
}
