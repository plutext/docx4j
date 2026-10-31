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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.org.apache.poi.poifs.filesystem.Ole10Native;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CR-070 (WD 0.47): an object run's {@link TextObject} (REQ-512's members), the embedded-object filter
 * (REQ-522), and the part name on {@code altchunk.not_expanded} (REQ-513, REQ-801).
 */
public class EmbeddedObjectsTest {

	private static final String REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/";
	private static final String OLE_OBJECT = "application/vnd.openxmlformats-officedocument.oleObject";
	private static final String ACTIVEX = "application/vnd.ms-office.activeX+xml";

	private static WordprocessingMLPackage probe;

	@BeforeClass
	public static void load() throws Exception {
		Path dir = ViewAgreementTest.resources().resolve("text-extraction/phase0");
		probe = WordprocessingMLPackage.load(dir.resolve("extraction-probe.docx").toFile());
	}

	private static List<TextRun> objects(TextResult result) {
		List<TextRun> out = new ArrayList<TextRun>();
		for (TextBlock b : result.blocks()) for (TextRun r : b.getRuns()) if (r.getKind() == TextRun.Kind.OBJECT) out.add(r);
		return out;
	}

	private static String text(TextResult result) {
		return PlainText.of(result);
	}

	/** An OLE 1.0 package holding a text file, as Word embeds one (ProgID Package, an Ole10Native stream). */
	private static byte[] ole10(String label, String fileName, String command, String content) throws Exception {
		Ole10Native nativeData = new Ole10Native(label, fileName, command, content.getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream nativeBytes = new ByteArrayOutputStream();
		nativeData.writeOut(nativeBytes);
		try (POIFSFileSystem fs = new POIFSFileSystem()) {
			Ole10Native.createOleMarkerEntry(fs);
			fs.createDocument(new ByteArrayInputStream(nativeBytes.toByteArray()), Ole10Native.OLE10_NATIVE);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			fs.writeFilesystem(out);
			return out.toByteArray();
		}
	}

	private static String object(String type, String progId, String rId) {
		return "<w:r><w:object><v:shape id=\"_x0000_" + rId + "\" style=\"width:64pt;height:32pt\"/>"
				+ "<o:OLEObject Type=\"" + type + "\" ProgID=\"" + progId + "\" ShapeID=\"_x0000_" + rId + "\" r:id=\"" + rId + "\"/>"
				+ "</w:object></w:r>";
	}

	/** Two OLE 1.0 packages in one paragraph, and a third in the next. */
	private static WordprocessingMLPackage packages() throws Exception {
		return MiniDocx.body("<w:p><w:r><w:t>First</w:t></w:r>" + object("Embed", "Package", "rIdA") + object("Embed", "Package", "rIdB") + "</w:p>"
				+ "<w:p><w:r><w:t>Second</w:t></w:r>" + object("Embed", "Package", "rIdC") + "</w:p>")
				.part("embeddings/oleObject1.bin", OLE_OBJECT, REL + "oleObject", "rIdA", ole10("notes.txt", "C:\\docs\\notes.txt", "C:\\tmp\\notes.txt", "NOTES_TEXT\r\n"))
				.part("embeddings/oleObject2.bin", OLE_OBJECT, REL + "oleObject", "rIdB", ole10("other.txt", "C:\\docs\\other.txt", "C:\\tmp\\other.txt", "OTHER_TEXT\r\n"))
				.part("embeddings/oleObject3.bin", OLE_OBJECT, REL + "oleObject", "rIdC", ole10("third.txt", "C:\\docs\\third.txt", "C:\\tmp\\third.txt", "THIRD_TEXT\r\n"))
				.load();
	}

	// --- the object (REQ-512) ----------------------------------------------------------------------------

	/** REQ-512's members on the probe's objects: an embedded workbook, document, OLE 1.0 package, ActiveX control and presentation. */
	@Test
	public void probeObjectsDescribed() {
		List<TextRun> runs = objects(Texts.of(probe, new TextOptions()));
		assertEquals(5, runs.size());
		TextObject xlsx = runs.get(0).getObject(), docx = runs.get(1).getObject(), ole = runs.get(2).getObject(),
				control = runs.get(3).getObject(), pptx = runs.get(4).getObject();

		assertEquals(TextObject.Kind.EMBED, xlsx.getKind());
		assertEquals("Excel.Sheet.12", xlsx.getProgId());
		assertEquals("/word/embeddings/probe39.xlsx", xlsx.getPartName());
		assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsx.getContentType());
		assertTrue(xlsx.getPart() instanceof EmbeddedPackagePart);
		assertSame(xlsx.getPart(), probe.getMainDocumentPart().getRelationshipsPart().getPart(xlsx.getRelationshipId()));
		assertNull(xlsx.getPackageLabel());
		assertTrue(xlsx.getSource() instanceof org.docx4j.wml.CTObject);

		assertEquals(TextObject.Kind.EMBED, docx.getKind());
		assertEquals("/word/embeddings/probe40.docx", docx.getPartName());
		assertEquals("Word.Document.12", docx.getProgId());

		// an OLE 1.0 package: its native data's names, as Tika hands such a package on
		assertEquals(TextObject.Kind.EMBED, ole.getKind());
		assertEquals("Package", ole.getProgId());
		assertEquals("/word/embeddings/probe41.bin", ole.getPartName());
		assertEquals(OLE_OBJECT, ole.getContentType());
		assertEquals("probe.txt", ole.getPackageLabel());
		assertEquals("probe.txt", ole.getPackageFileName());
		assertEquals("probe.txt", ole.getPackageCommand());

		// an ActiveX control (w:control in w:object): its part, no ProgID
		assertEquals(TextObject.Kind.CONTROL, control.getKind());
		assertNull(control.getProgId());
		assertNull(runs.get(3).getProgId());
		assertEquals("/word/activeX/activeX42.xml", control.getPartName());
		assertEquals(ACTIVEX, control.getContentType());
		assertTrue(control.getSource() instanceof org.docx4j.wml.CTObject);

		assertEquals("PowerPoint.Show.12", pptx.getProgId());
		assertTrue(pptx.getPartName().endsWith(".pptx"));
		for (TextRun r : runs) assertEquals(r.getProgId(), r.getObject().getProgId());
	}

	/** REQ-512: an OLE 1.0 package's label, file name and command, each where the native data has it. */
	@Test
	public void packageNames() throws Exception {
		TextObject o = objects(Texts.of(packages(), new TextOptions())).get(0).getObject();
		assertEquals("notes.txt", o.getPackageLabel());
		assertEquals("C:\\docs\\notes.txt", o.getPackageFileName());
		assertEquals("C:\\tmp\\notes.txt", o.getPackageCommand());
		assertEquals("rIdA", o.getRelationshipId());
		assertTrue(o.getPart() instanceof BinaryPart);
		// a Package whose binary holds no Ole10Native stream: no names, and no failure
		WordprocessingMLPackage other = MiniDocx.body("<w:p>" + object("Embed", "Package", "rIdX") + "</w:p>")
				.part("embeddings/oleObject1.bin", OLE_OBJECT, REL + "oleObject", "rIdX", "not an OLE file".getBytes(StandardCharsets.UTF_8)).load();
		TextObject none = objects(Texts.of(other, new TextOptions())).get(0).getObject();
		assertNull(none.getPackageLabel());
		assertNull(none.getPackageFileName());
		assertNull(none.getPackageCommand());
		assertEquals("/word/embeddings/oleObject1.bin", none.getPartName());
	}

	/** REQ-512: a linked object has its relationship id and ProgID, and no part; it is never read, nor asked about. */
	@Test
	public void linkedObject() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p><w:r><w:t>Linked</w:t></w:r>" + object("Link", "Excel.Sheet.12", "rIdL") + "</w:p>")
				.external("rIdL", REL + "oleObject", "file:///C:/data/book.xlsx").load();
		List<Object> asked = new ArrayList<Object>();
		TextResult r = Texts.of(pkg, new TextOptions().embedded(true).embeddedFilter((o, a) -> asked.add(o)));
		TextObject o = objects(r).get(0).getObject();
		assertEquals(TextObject.Kind.LINK, o.getKind());
		assertEquals("rIdL", o.getRelationshipId());
		assertEquals("Excel.Sheet.12", o.getProgId());
		assertNull(o.getPart());
		assertNull(o.getPartName());
		assertNull(o.getContentType());
		assertEquals(new ArrayList<Object>(), asked);
		assertEquals("Linked\n", text(r));
		assertTrue(r.warnings().toString(), r.warnings().isEmpty());
	}

	/** REQ-512: a w:control standing in w:pict, and an o:OLEObject whose part is an ActiveX control's, are controls. */
	@Test
	public void controls() throws Exception {
		String ocx = "<ax:ocx xmlns:ax=\"http://schemas.microsoft.com/office/2006/activeX\" ax:classid=\"{8BD21D40-EC42-11CE-9E0D-00AA006002F3}\" ax:persistence=\"persistPropertyBag\"/>";
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p><w:r><w:pict><v:shape id=\"_x0000_i1025\" style=\"width:20pt;height:10pt\"/>"
				+ "<w:control r:id=\"rIdX\" w:name=\"CheckBox1\" w:shapeid=\"_x0000_i1025\"/></w:pict></w:r>"
				+ object("Embed", "Forms.CheckBox.1", "rIdY") + "</w:p>")
				.part("activeX/activeX1.xml", ACTIVEX, REL + "control", "rIdX", ocx)
				.part("activeX/activeX2.xml", ACTIVEX, REL + "control", "rIdY", ocx).load();
		List<TextRun> runs = objects(Texts.of(pkg, new TextOptions().embedded(true)));
		assertEquals(2, runs.size());
		TextObject pict = runs.get(0).getObject(), ole = runs.get(1).getObject();
		assertEquals(TextObject.Kind.CONTROL, pict.getKind());
		assertEquals("rIdX", pict.getRelationshipId());
		assertEquals("/word/activeX/activeX1.xml", pict.getPartName());
		assertEquals(ACTIVEX, pict.getContentType());
		assertTrue(pict.getSource() instanceof org.docx4j.wml.CTControl);
		assertEquals(TextObject.Kind.CONTROL, ole.getKind());
		assertEquals("Forms.CheckBox.1", ole.getProgId());
		assertEquals("/word/activeX/activeX2.xml", ole.getPartName());
	}

	// --- the filter (REQ-522) ----------------------------------------------------------------------------

	/** REQ-522: no filter reads every object (REQ-512), as a null filter does. */
	@Test
	public void noFilterReadsEvery() throws Exception {
		String all = "First\nNOTES_TEXT\nOTHER_TEXT\nSecond\nTHIRD_TEXT\n";
		assertEquals(all, text(Texts.of(packages(), new TextOptions().embedded(true))));
		assertEquals(all, text(Texts.of(packages(), new TextOptions().embedded(true).embeddedFilter(null))));
		assertNull(new TextOptions().getEmbeddedFilter());
	}

	/**
	 * REQ-522: asked once per object, before the reading, with the run's object and the address its content
	 * is read at; a declined object contributes nothing and keeps its run; the addresses don't move.
	 */
	@Test
	public void filterDeclinesOneOfTwo() throws Exception {
		WordprocessingMLPackage pkg = packages();
		Map<String, TextObject> asked = new LinkedHashMap<String, TextObject>();
		TextResult r = Texts.of(pkg, new TextOptions().embedded(true).embeddedFilter((o, address) -> {
			asked.put(address, o);
			return !"notes.txt".equals(o.getPackageLabel());
		}));
		assertEquals(Arrays.asList("body/0/object:0", "body/0/object:1", "body/1/object:0"), new ArrayList<String>(asked.keySet()));
		assertEquals("First\nOTHER_TEXT\nSecond\nTHIRD_TEXT\n", text(r));
		// the declined object keeps its run and its description, the one the filter was given
		List<TextRun> runs = objects(r);
		assertEquals(3, runs.size());
		assertSame(runs.get(0).getObject(), asked.get("body/0/object:0"));
		assertSame(runs.get(1).getObject(), asked.get("body/0/object:1"));
		// the second object's content is where it would be had the first been read
		List<String> addresses = new ArrayList<String>();
		for (TextBlock b : r.blocks()) if (b.getStory() == TextBlock.Story.EMBEDDED) addresses.add(b.getAddress());
		assertEquals(Arrays.asList("body/0/object:1/0", "body/1/object:0/0"), addresses);
		assertTrue(r.warnings().toString(), r.warnings().isEmpty());
		// without embedded: true nothing is read, and the filter is never asked
		asked.clear();
		Texts.of(pkg, new TextOptions().embeddedFilter((o, address) -> asked.put(address, o) == null));
		assertTrue(asked.isEmpty());
	}

	/** REQ-522 over the probe: a host that reads workbooks itself; one level, so the embedded document's own object is never asked about. */
	@Test
	public void filterOverTheProbe() {
		List<String> asked = new ArrayList<String>();
		TextResult r = Texts.of(probe, new TextOptions().embedded(true).embeddedFilter((o, address) -> {
			asked.add(o.getProgId());
			return !o.getContentType().contains("spreadsheetml");
		}));
		assertEquals(Arrays.asList("Excel.Sheet.12", "Word.Document.12", "Package", "PowerPoint.Show.12"), asked);
		String text = text(r);
		assertFalse(text.contains("EMB39_XLSX_CELL"));
		assertTrue(text.contains("EMB40_DOCX_TEXT"));
		assertTrue(text.contains("OLE41_NATIVE_TEXT"));
		assertTrue(text.contains("EMB53_SLIDE_ONE"));
		assertFalse(text.contains("EMB40_NESTED_TEXT"));
		assertEquals("Excel.Sheet.12", objects(r).get(0).getProgId());
	}

	/** REQ-522: a filter that fails declines, with content.not_read naming the object's part at the paragraph; the reading goes on. */
	@Test
	public void failingFilterDeclines() throws Exception {
		TextResult r = Texts.of(packages(), new TextOptions().embedded(true).embeddedFilter((o, address) -> {
			if (address.equals("body/0/object:1")) throw new IllegalStateException("host failure");
			return true;
		}));
		assertEquals("First\nNOTES_TEXT\nSecond\nTHIRD_TEXT\n", text(r));
		assertEquals(Arrays.asList(new TextWarning(TextWarning.CONTENT_NOT_READ, "body/0", null, null, "/word/embeddings/oleObject2.bin")),
				r.warnings());
		assertEquals("/word/embeddings/oleObject2.bin", r.warnings().get(0).getSelector());
		assertEquals(3, objects(r).size());
	}

	// --- altchunk.not_expanded's part name (REQ-513, REQ-801) --------------------------------------------

	/**
	 * REQ-513, REQ-801: the warning carries the chunk's content type, and its part name as its selector; a
	 * chunk in a chunk names the chunk holding it, the part the document has.
	 */
	@Test
	public void chunkWarningNamesThePart() {
		TextResult r = Texts.of(probe, new TextOptions());
		Map<String, TextWarning> byPart = new LinkedHashMap<String, TextWarning>();
		for (TextWarning w : r.warnings()) if (w.getKind().equals(TextWarning.ALTCHUNK_NOT_EXPANDED)) byPart.put(w.getPartName(), w);
		TextWarning rtf = byPart.get("/word/chunk45.rtf");
		assertEquals("application/rtf", rtf.getContentType());
		assertEquals("/word/chunk45.rtf", rtf.getSelector());
		TextWarning nested = byPart.get("/word/chunk47.docx");
		assertEquals("text/html", nested.getContentType());
		assertTrue(nested.getAddress(), nested.getAddress().matches("body/\\d+/altchunk:0/body/\\d+"));
		// altchunks: false: every chunk, by its own part
		for (TextWarning w : Texts.of(probe, new TextOptions().altchunks(false)).warnings()) {
			if (!w.getKind().equals(TextWarning.ALTCHUNK_NOT_EXPANDED)) continue;
			assertTrue(w.toString(), w.getSelector().startsWith("/word/"));
			assertEquals(w.getPartName(), w.getSelector());
		}
	}
}
