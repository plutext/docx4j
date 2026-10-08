/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.RendererHints;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTColumn;
import org.docx4j.wml.CTColumns;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.junit.After;
import org.junit.Test;

/**
 * Columns of unequal width: on a renderer with the column-widths hook (fork CR-026) the
 * section's w:col widths go on the region body as fox:column-widths and fox:column-gaps
 * and the stretch stays in the flow; on Apache FOP the one-row table as before.  The
 * {@link RendererHints} are set by hand here; the master-set code asks
 * {@link FopCapabilities}, so the attribute itself is asserted through
 * {@link LayoutMasterSetBuilder#markColumnWidths} only indirectly, by the table route.
 * @since 17.3.2
 */
public class ColumnWidthsTest {

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	@After
	public void restoreHints() {
		RendererHints.set(Collections.<String>emptySet());
	}

	/** 10598's section: 2509 / 1025 / 7216 twips on an 11910 - 560 - 600 page. */
	/** @param withTable a nested w:tbl in the stretch, which the one-row table cannot take (it stays in the flow) */
	private static WordprocessingMLPackage pkg(boolean withTable) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		if (sectPr == null) {
			sectPr = F.createSectPr();
			pkg.getMainDocumentPart().getJaxbElement().getBody().setSectPr(sectPr);
		}
		SectPr.PgSz pgSz = F.createSectPrPgSz();
		pgSz.setW(BigInteger.valueOf(11910)); pgSz.setH(BigInteger.valueOf(16840));
		sectPr.setPgSz(pgSz);
		SectPr.PgMar pgMar = F.createSectPrPgMar();
		pgMar.setLeft(BigInteger.valueOf(560)); pgMar.setRight(BigInteger.valueOf(600));
		pgMar.setTop(BigInteger.valueOf(568)); pgMar.setBottom(BigInteger.valueOf(280));
		sectPr.setPgMar(pgMar);
		CTColumns cols = F.createCTColumns();
		cols.setNum(BigInteger.valueOf(2)); cols.setEqualWidth(false); cols.setSpace(BigInteger.valueOf(154));
		CTColumn c1 = F.createCTColumn(); c1.setW(BigInteger.valueOf(2509)); c1.setSpace(BigInteger.valueOf(1025));
		CTColumn c2 = F.createCTColumn(); c2.setW(BigInteger.valueOf(7216));
		cols.getCol().add(c1); cols.getCol().add(c2);
		sectPr.setCols(cols);
		for (int i = 0; i < 6; i++) {
			pkg.getMainDocumentPart().addParagraphOfText("Paragraph " + i + " of the unequal columns, long enough to wrap in the narrow one.");
		}
		if (withTable) {
			org.docx4j.wml.Tbl tbl = F.createTbl();
			org.docx4j.wml.Tr tr = F.createTr();
			org.docx4j.wml.Tc tc = F.createTc();
			org.docx4j.wml.P p = F.createP();
			org.docx4j.wml.R r = F.createR();
			org.docx4j.wml.Text txt = F.createText();
			txt.setValue("a table in the stretch");
			r.getContent().add(txt);
			p.getContent().add(r);
			tc.getContent().add(p);
			tr.getContent().add(tc);
			tbl.getContent().add(tr);
			pkg.getMainDocumentPart().getContent().add(pkg.getMainDocumentPart().getContent().size() - 1, tbl);
			pkg.getMainDocumentPart().addParagraphOfText("And a paragraph after the table.");
		}
		return pkg;
	}

	private static String fo(WordprocessingMLPackage pkg) throws Exception {
		FOSettings settings = Docx4J.createFOSettings();
		settings.setOpcPackage(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toFO(settings, os, Docx4J.FLAG_NONE);
		return os.toString("UTF-8");
	}

	@Test
	public void aStretchThatFitsAPageIsAOneRowTableWithOrWithoutTheHook() throws Exception {
		RendererHints.set(Collections.<String>emptySet());
		String fo = fo(pkg(false));
		assertTrue("the one-row table: " + fo, fo.contains("<fo:table") || fo.contains("<table"));
		assertFalse("no column widths on the region body: " + fo, fo.contains("column-widths"));
		RendererHints.set(Arrays.asList("column-widths"));
		fo = fo(pkg(false));
		assertTrue("still the table under the hook (it fits a page; the renderer does not balance unequal columns yet): " + fo,
				fo.contains("<fo:table") || fo.contains("<table"));
		assertFalse("so no widths: " + fo, fo.contains("column-widths"));
	}

	@Test
	public void aStretchTheTableCannotTakeGetsTheWidthsUnderTheHook() throws Exception {
		RendererHints.set(Collections.<String>emptySet());
		String fo = fo(pkg(true));
		assertFalse("without the hook, equal columns and no widths: " + fo, fo.contains("column-widths"));
		assertTrue(fo.contains("column-count=\"2\""));
		RendererHints.set(Arrays.asList("column-widths"));
		fo = fo(pkg(true));
		assertTrue("the widths: " + fo, fo.contains("fox:column-widths=\"125.45pt 360.8pt\""));
		assertTrue("the gap: " + fo, fo.contains("fox:column-gaps=\"51.25pt\""));
		assertTrue(fo.contains("column-count=\"2\""));
	}
}
