/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.model.structure;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.pagination.Paginate;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.GlossaryDocument;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.After;
import org.junit.Test;

/**
 * A {@code w14:paraId} on every paragraph and row saved (CR-077), counted on the saved bytes, from
 * Word's save of a file with ids on everything: every route that adds a paragraph or a row
 * leaves none without one, none repeated, and Word's ids where they were.
 */
public class ParaIdsTest {

	/** Word's save: tables, content controls, custom XML, a header, comments and footnotes. */
	private static final String HOSTS = "unknown-elements/76t18-hosts-control-word365.docx";

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	private interface Route { void on(WordprocessingMLPackage pkg, MainDocumentPart main) throws Exception; }

	@After public void propertyBack() {
		Docx4jProperties.getProperties().remove(ParaIds.PROPERTY);
	}

	// ---------------------------------------------------------------- the routes

	@Test public void everyRouteOnWordsSave() throws Exception {
		Map<String, Route> routes = new LinkedHashMap<String, Route>();
		routes.put("load and save", (pkg, main) -> main.getContent());
		routes.put("createP", (pkg, main) -> main.getContent().add(F.createP()));
		routes.put("addParagraphOfText", (pkg, main) -> main.addParagraphOfText("x"));
		routes.put("addStyledParagraphOfText", (pkg, main) -> main.addStyledParagraphOfText("Heading1", "x"));
		routes.put("createParagraphOfText", (pkg, main) -> main.getContent().add(main.createParagraphOfText("x")));
		routes.put("addParagraph", (pkg, main) -> main.addParagraph("<w:p " + W + "><w:r><w:t>x</w:t></w:r></w:p>"));
		routes.put("addObject", (pkg, main) -> main.addObject(F.createP()));
		routes.put("a fragment", (pkg, main) -> main.getContent().add(XmlUtils.unmarshalString("<w:p " + W + "><w:r><w:t>x</w:t></w:r></w:p>")));
		routes.put("createTable", (pkg, main) -> main.getContent().add(TblFactory.createTable(2, 3, 1500)));
		routes.put("createTr", (pkg, main) -> firstTable(main).getContent().add(row()));
		routes.put("a column", (pkg, main) -> {
			for (Object o : firstTable(main).getContent()) {
				if (XmlUtils.unwrap(o) instanceof Tr) ((Tr) XmlUtils.unwrap(o)).getContent().add(cell());
			}
		});
		routes.put("a row copied", (pkg, main) -> {
			Tbl t = firstTable(main);
			t.getContent().add(XmlUtils.deepCopy(t.getContent().get(t.getContent().size() - 1)));
		});
		routes.put("a paragraph copied", (pkg, main) -> main.getContent().add(XmlUtils.deepCopy(firstParagraph(main))));
		routes.put("generateToc", (pkg, main) -> {
			P p = firstParagraph(main);
			if (p.getPPr() == null) p.setPPr(new PPr());
			PPrBase.PStyle style = F.createPPrBasePStyle();
			style.setVal("Heading1");
			p.getPPr().setPStyle(style);
			new org.docx4j.toc.TocGenerator(pkg).generateToc(0, " TOC \\o \"1-3\" \\h \\z \\u ", true);
		});
		Map<String, byte[]> before = parts(bytes(HOSTS));
		for (Map.Entry<String, Route> route : routes.entrySet()) {
			WordprocessingMLPackage pkg = load(bytes(HOSTS));
			route.getValue().on(pkg, pkg.getMainDocumentPart());
			Census after = new Census(parts(save(pkg)).get("word/document.xml"));
			assertEquals(route.getKey(), 0, after.none);
			assertEquals(route.getKey() + after.repeated, 0, after.repeated.size());
			assertTrue(route.getKey(), after.ids.containsAll(new Census(before.get("word/document.xml")).ids));
		}
	}

	/** Load repairs write a paragraph too, and copy the outer paragraph's id. */
	@Test public void loadRepairs() throws Exception {
		for (Function<String, String> source : new ArrayList<Function<String, String>>(List.of(
				x -> x.substring(0, x.lastIndexOf("<w:sectPr")) + "<w:br/>" + x.substring(x.lastIndexOf("<w:sectPr")),
				x -> x.replaceFirst("<w:r( [^>]*)?><w:t( [^>]*)?>([^<]*)</w:t></w:r>",
						"<w:r><w:t>a</w:t><w:p><w:r><w:t>b</w:t></w:r></w:p><w:t>c</w:t></w:r>")))) {
			WordprocessingMLPackage pkg = load(withDocument(bytes(HOSTS), source));
			pkg.getMainDocumentPart().getContent();
			Census after = new Census(parts(save(pkg)).get("word/document.xml"));
			assertEquals(0, after.none);
			assertTrue(after.repeated.toString(), after.repeated.isEmpty());
		}
	}

	/** D2: headers, footers, notes and comments as well. */
	@Test public void theOtherParts() throws Exception {
		WordprocessingMLPackage pkg = load(bytes(HOSTS));
		Map<String, Integer> gaps = new LinkedHashMap<String, Integer>();
		for (org.docx4j.openpackaging.parts.Part part : pkg.getParts().getParts().values()) {
			if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart) {
				((org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart) part).getContent().add(F.createP());
				gaps.put(part.getPartName().getName().substring(1), 1);
			} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart) {
				((org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart) part).getJaxbElement().getComment().get(0).getContent().add(F.createP());
				gaps.put(part.getPartName().getName().substring(1), 1);
			} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart) {
				List<org.docx4j.wml.CTFtnEdn> notes = ((org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart) part).getJaxbElement().getFootnote();
				notes.get(notes.size() - 1).getContent().add(F.createP());
				gaps.put(part.getPartName().getName().substring(1), 1);
			}
		}
		assertEquals(gaps.toString(), 3, gaps.size());
		Map<String, byte[]> saved = parts(save(pkg));
		for (String name : gaps.keySet()) assertEquals(name, 0, new Census(saved.get(name)).none);
	}

	@Test public void aFooterAndEndnotes() throws Exception {
		for (String file : new String[] { "unknown-elements/76t29-madeup-in-ftr-word365.docx", "unknown-elements/76t30-madeup-in-endnote-word365.docx" }) {
			WordprocessingMLPackage pkg = load(bytes(file));
			for (org.docx4j.openpackaging.parts.Part part : pkg.getParts().getParts().values()) {
				if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.FooterPart) {
					((org.docx4j.openpackaging.parts.WordprocessingML.FooterPart) part).getContent().add(F.createP());
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart) {
					List<org.docx4j.wml.CTFtnEdn> notes = ((org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart) part).getJaxbElement().getEndnote();
					notes.get(notes.size() - 1).getContent().add(F.createP());
				}
			}
			Map<String, byte[]> saved = parts(save(pkg));
			for (Map.Entry<String, byte[]> e : saved.entrySet()) {
				if (e.getKey().matches("word/(footer\\d*|endnotes)\\.xml")) assertEquals(e.getKey(), 0, new Census(e.getValue()).none);
			}
		}
	}

	/** A new footer with no ids cost document.xml all of its (python's third round), so it takes them too. */
	@Test public void aNewPart() throws Exception {
		WordprocessingMLPackage pkg = load(bytes(HOSTS));
		pkg.getMainDocumentPart().getContent();
		org.docx4j.openpackaging.parts.WordprocessingML.FooterPart footer = new org.docx4j.openpackaging.parts.WordprocessingML.FooterPart();
		org.docx4j.wml.Ftr ftr = F.createFtr();
		ftr.getContent().add(F.createP());
		footer.setJaxbElement(ftr);
		pkg.getMainDocumentPart().addTargetPart(footer);
		byte[] part = parts(save(pkg)).get(footer.getPartName().getName().substring(1));
		assertEquals(0, new Census(part).none);
		assertEquals(1, new Census(part).ids.size());
	}

	/** A new part in a document with no ids stays so, and a main document part isn't read for this. */
	@Test public void aNewPartWithNoIdsAround() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.FooterPart footer = new org.docx4j.openpackaging.parts.WordprocessingML.FooterPart();
		org.docx4j.wml.Ftr ftr = F.createFtr();
		ftr.getContent().add(F.createP());
		footer.setJaxbElement(ftr);
		pkg.getMainDocumentPart().addTargetPart(footer);
		assertEquals(0, new Census(parts(save(pkg)).get(footer.getPartName().getName().substring(1))).ids.size());

		pkg = load(bytes(HOSTS));
		footer = new org.docx4j.openpackaging.parts.WordprocessingML.FooterPart();
		ftr = F.createFtr();
		ftr.getContent().add(F.createP());
		footer.setJaxbElement(ftr);
		assertFalse(pkg.getMainDocumentPart().isUnmarshalled());
		footer.setPackage(pkg);
		assertEquals(0, ParaIds.assign(footer));
		assertFalse(pkg.getMainDocumentPart().isUnmarshalled());
	}

	/** D4: the first keeps the id, as Word's does. */
	@Test public void theFirstOfADuplicateKeepsIt() throws Exception {
		WordprocessingMLPackage pkg = load(bytes(HOSTS));
		MainDocumentPart main = pkg.getMainDocumentPart();
		P first = firstParagraph(main);
		String id = first.getParaId();
		P copy = XmlUtils.deepCopy(first);
		main.getContent().add(copy);
		assertEquals(1, ParaIds.assign(main));
		assertEquals(id, first.getParaId());
		assertFalse(id.equals(copy.getParaId()));
	}

	/** D6, and the id's form: eight hex digits, upper case, below 0x7FFFFFFF, no w14:textId. */
	@Test public void aNewIdsForm() throws Exception {
		WordprocessingMLPackage pkg = load(bytes(HOSTS));
		P p = F.createP();
		pkg.getMainDocumentPart().getContent().add(p);
		ParaIds.assign(pkg.getMainDocumentPart());
		assertTrue(p.getParaId(), p.getParaId().matches("[0-9A-F]{8}"));
		assertTrue(Long.parseLong(p.getParaId(), 16) < 0x7FFFFFFFL);
		assertNull(p.getTextId());
	}

	/** The tree takes the ids, so a second save writes the same. */
	@Test public void aSecondSave() throws Exception {
		WordprocessingMLPackage pkg = load(bytes(HOSTS));
		pkg.getMainDocumentPart().addParagraphOfText("x");
		assertArrayEquals(parts(save(pkg)).get("word/document.xml"), parts(save(pkg)).get("word/document.xml"));
	}

	// ---------------------------------------------------------------- where it does nothing

	/** A document that uses no ids stays so. */
	@Test public void noIdsStaysSo() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addParagraphOfText("x");
		pkg.getMainDocumentPart().getContent().add(TblFactory.createTable(2, 2, 1500));
		assertEquals(0, new Census(parts(save(pkg)).get("word/document.xml")).ids.size());
	}

	/** A part nobody read is copied as bytes. */
	@Test public void aPartNotRead() throws Exception {
		byte[] gap = withDocument(bytes(HOSTS), x -> x.replaceFirst(" w14:paraId=\"[^\"]*\"", ""));
		assertArrayEquals(parts(gap).get("word/document.xml"), parts(save(load(gap))).get("word/document.xml"));
	}

	@Test public void theProperty() throws Exception {
		Docx4jProperties.setProperty(ParaIds.PROPERTY, false);
		WordprocessingMLPackage pkg = load(bytes(HOSTS));
		pkg.getMainDocumentPart().addParagraphOfText("x");
		assertEquals(1, new Census(parts(save(pkg)).get("word/document.xml")).none);
	}

	/** A Fallback is left alone: it mostly has no id, and where it has one it repeats its Choice's. */
	@Test public void aFallback() throws Exception {
		String pair = "<w:p w14:paraId=\"0ABCDEF1\"><w:r><mc:AlternateContent>"
				+ "<mc:Choice Requires=\"wps\"><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"1\" cy=\"1\"/>"
				+ "<wp:docPr id=\"1\" name=\"b\"/><a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:cNvSpPr/><wps:spPr/><wps:txbx><w:txbxContent><w:p w14:paraId=\"0ABCDEF2\"><w:r><w:t>box</w:t></w:r></w:p>"
				+ "</w:txbxContent></wps:txbx><wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></mc:Choice>"
				+ "<mc:Fallback><w:pict><v:shape><v:textbox><w:txbxContent><w:p><w:r><w:t>box</w:t></w:r></w:p>"
				+ "<w:p w14:paraId=\"0ABCDEF2\"><w:r><w:t>box</w:t></w:r></w:p></w:txbxContent></v:textbox></v:shape></w:pict></mc:Fallback>"
				+ "</mc:AlternateContent></w:r></w:p>";
		String ns = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:v=\"urn:schemas-microsoft-com:vml\"";
		WordprocessingMLPackage pkg = load(withDocument(bytes(HOSTS), x -> x.replaceFirst("<w:body>",
				Matcher.quoteReplacement("<w:body>" + pair.replace("<w:p w14:paraId=\"0ABCDEF1\">", "<w:p w14:paraId=\"0ABCDEF1\"" + ns + ">")))));
		pkg.getMainDocumentPart().addParagraphOfText("x");
		String saved = new String(parts(save(pkg)).get("word/document.xml"), StandardCharsets.UTF_8);
		Matcher fallback = Pattern.compile("<mc:Fallback.*?</mc:Fallback>", Pattern.DOTALL).matcher(saved);
		assertTrue(fallback.find());
		assertEquals(fallback.group(), 1, count(fallback.group(), "w14:paraId"));
		assertEquals(0, new Census(saved.replace(fallback.group(), "").getBytes(StandardCharsets.UTF_8)).none);
	}

	/** Word writes no id in a glossary. */
	@Test public void aGlossary() throws Exception {
		GlossaryDocumentPart part = new GlossaryDocumentPart();
		part.unmarshal(new ByteArrayInputStream(("<w:glossaryDocument " + W + " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\">"
				+ "<w:docParts><w:docPart><w:docPartBody><w:p w14:paraId=\"0ABCDEF1\"/><w:p/></w:docPartBody></w:docPart></w:docParts>"
				+ "</w:glossaryDocument>").getBytes(StandardCharsets.UTF_8)));
		ParaIds.beforeSave(part);
		assertEquals(0, ParaIds.assign(part));
		String xml = XmlUtils.marshaltoString(part.getJaxbElement(), true, false);
		assertEquals(xml, 1, count(xml, "w14:paraId"));
	}

	// ---------------------------------------------------------------- the other routes

	@Test public void flatOpc() throws Exception {
		WordprocessingMLPackage pkg = load(bytes(HOSTS));
		pkg.getMainDocumentPart().addParagraphOfText("x");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out, Docx4J.FLAG_SAVE_FLAT_XML);
		Matcher part = Pattern.compile("pkg:name=\"/word/document\\.xml\".*?</pkg:part>", Pattern.DOTALL)
				.matcher(new String(out.toByteArray(), StandardCharsets.UTF_8));
		assertTrue(part.find());
		assertEquals(0, new Census(part.group().getBytes(StandardCharsets.UTF_8)).none);
	}

	/** Paginate's ids reach paragraphs, and a save gives the rows theirs. */
	@Test public void afterPaginatesIds() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart main = pkg.getMainDocumentPart();
		main.getContent().add(TblFactory.createTable(2, 2, 1500));
		main.addParagraphOfText("x");
		assertTrue(Paginate.assignParaIds(main) > 0);
		assertNull(((Tr) XmlUtils.unwrap(((Tbl) XmlUtils.unwrap(main.getContent().get(main.getContent().size() - 2))).getContent().get(0))).getParaId());
		assertEquals(0, new Census(parts(save(pkg)).get("word/document.xml")).none);
	}

	// ---------------------------------------------------------------- helpers

	private static byte[] bytes(String name) throws Exception {
		try (InputStream is = ResourceUtils.getResource(name)) {
			assertNotNull(name, is);
			return is.readAllBytes();
		}
	}

	private static WordprocessingMLPackage load(byte[] docx) throws Exception {
		return WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
	}

	private static byte[] save(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		return out.toByteArray();
	}

	private static Map<String, byte[]> parts(byte[] docx) throws Exception {
		Map<String, byte[]> parts = new LinkedHashMap<String, byte[]>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx))) {
			for (ZipEntry e; (e = zip.getNextEntry()) != null; ) parts.put(e.getName(), zip.readAllBytes());
		}
		return parts;
	}

	private static byte[] withDocument(byte[] docx, Function<String, String> edit) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(out)) {
			for (Map.Entry<String, byte[]> e : parts(docx).entrySet()) {
				zip.putNextEntry(new ZipEntry(e.getKey()));
				zip.write(e.getKey().equals("word/document.xml")
						? edit.apply(new String(e.getValue(), StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8) : e.getValue());
				zip.closeEntry();
			}
		}
		return out.toByteArray();
	}

	private static Tbl firstTable(MainDocumentPart main) {
		for (Object o : main.getContent()) if (XmlUtils.unwrap(o) instanceof Tbl) return (Tbl) XmlUtils.unwrap(o);
		throw new IllegalStateException("no table");
	}

	private static P firstParagraph(MainDocumentPart main) {
		for (Object o : main.getContent()) if (XmlUtils.unwrap(o) instanceof P) return (P) XmlUtils.unwrap(o);
		throw new IllegalStateException("no paragraph");
	}

	private static Tc cell() {
		Tc c = F.createTc();
		c.getContent().add(F.createP());
		return c;
	}

	private static Tr row() {
		Tr r = F.createTr();
		r.getContent().add(cell());
		return r;
	}

	private static int count(String in, String what) {
		int n = 0;
		for (int i = in.indexOf(what); i >= 0; i = in.indexOf(what, i + what.length())) n++;
		return n;
	}

	/** Each w:p and w:tr of a part outside a Fallback: how many have no id, and which ids repeat. */
	private static final class Census {
		int none;
		final Set<String> ids = new HashSet<String>();
		final List<String> repeated = new ArrayList<String>();

		Census(byte[] part) {
			String xml = new String(part, StandardCharsets.UTF_8).replaceAll("(?s)<mc:Fallback.*?</mc:Fallback>", "");
			Matcher m = Pattern.compile("<w:(?:p|tr)(?: [^>]*)?/?>").matcher(xml);
			while (m.find()) {
				Matcher id = Pattern.compile("w14:paraId=\"([^\"]*)\"").matcher(m.group());
				if (!id.find()) none++;
				else if (!ids.add(id.group(1))) repeated.add(id.group(1));
			}
		}
	}
}
