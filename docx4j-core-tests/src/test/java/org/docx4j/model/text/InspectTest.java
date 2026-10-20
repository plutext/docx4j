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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.anon.PartsAnalyzer;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ActiveXControlXmlPart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart;
import org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectBinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Text;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * CR-050 phase 1: what the conformance suite's exact comparison does not pin on its own. The inspector
 * is read-only and fetches nothing; it finds what the anonymiser inventories; it is bounded, and says
 * so when a bound stops it; locations name their package; membership follows the options; and the
 * counts do not move when runs are split. The suite's rows are {@code ExtractionConformanceTest}'s.
 */
public class InspectTest {

	static final String NS = " xmlns:w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'"
			+ " xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships'"
			+ " xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:v='urn:schemas-microsoft-com:vml'";
	static final String REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/";
	static final String[] FIXTURES = { "extraction-probe.docx", "extraction-probe-external.docm", "extraction-probe-structure.docx",
			"extraction-probe-inspect.docx" };

	static Path fixtures() {
		return ViewAgreementTest.resources().resolve("text-extraction/phase0");
	}

	static WordprocessingMLPackage fixture(String name) throws Exception {
		return WordprocessingMLPackage.load(fixtures().resolve(name).toFile());
	}

	// --- building packages ----------------------------------------------------------------------------

	/** A package built in the test: parts added first, for their relationship ids, then the body. */
	static final class Doc {
		final WordprocessingMLPackage pkg;
		final MainDocumentPart mdp;
		final StringBuilder body = new StringBuilder();
		String sectPr = "<w:sectPr/>";

		Doc() throws Exception {
			pkg = WordprocessingMLPackage.createPackage();
			mdp = pkg.getMainDocumentPart();
		}

		Doc p(String runs) {
			body.append("<w:p>").append(runs).append("</w:p>");
			return this;
		}

		String chunk(String name, String type, byte[] data) throws Exception {
			AlternativeFormatInputPart part = new AlternativeFormatInputPart(new PartName(name));
			part.setContentType(new ContentType(type));
			part.setBinaryData(data);
			String id = mdp.addTargetPart(part).getId();
			body.append("<w:altChunk r:id='").append(id).append("'/>");
			return id;
		}

		void embed(String name, byte[] docx) throws Exception {
			EmbeddedPackagePart part = new EmbeddedPackagePart(new PartName(name));
			part.setContentType(new ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
			part.setRelationshipType(REL + "package");
			part.setBinaryData(docx);
			String id = mdp.addTargetPart(part).getId();
			p("<w:r><w:object><o:OLEObject Type='Embed' ProgID='Word.Document.12' ShapeID='_x0000_i1' DrawAspect='Icon' ObjectID='_1' r:id='"
					+ id + "'/></w:object></w:r>");
		}

		WordprocessingMLPackage done() throws Exception {
			mdp.setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document" + NS + "><w:body>" + body + sectPr + "</w:body></w:document>"));
			return pkg;
		}

		byte[] bytes() throws Exception {
			done();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pkg.save(out);
			return out.toByteArray();
		}
	}

	static String run(String text) {
		return "<w:r><w:t xml:space='preserve'>" + text + "</w:t></w:r>";
	}

	static String hidden(String text) {
		return "<w:r><w:rPr><w:vanish/></w:rPr><w:t>" + text + "</w:t></w:r>";
	}

	static List<InspectFinding> of(Inspection i, String kind) {
		List<InspectFinding> out = new ArrayList<InspectFinding>();
		for (InspectFinding f : i.findings()) if (f.getKind().equals(kind)) out.add(f);
		return out;
	}

	static Set<String> reasons(Inspection i) {
		Set<String> out = new HashSet<String>();
		for (Inspection.Entry e : i.skipped()) out.add(e.getReason());
		return out;
	}

	// --- read-only, and nothing fetched ----------------------------------------------------------------

	/** REQ-204's snapshot, declarations included (CR-049), is the same after an inspection. */
	@Test
	public void inspectionChangesNothing() throws Exception {
		for (String name : FIXTURES) {
			WordprocessingMLPackage pkg = fixture(name);
			String before = TextsPhase2Test.snapshot(pkg);
			Inspect.of(pkg);
			assertEquals(name, before, TextsPhase2Test.snapshot(pkg));
		}
	}

	/**
	 * Nothing named by a chunk, a field or a relationship is fetched, whether docx4j-ImportXHTML is on
	 * the class path or not (CR-050 section 3): a listener on the loopback interface counts the
	 * requests. What would be fetched is reported instead.
	 */
	@Test
	public void nothingIsFetched() throws Exception {
		final List<String> requests = Collections.synchronizedList(new ArrayList<String>());
		HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> {
			requests.add(exchange.getRequestURI().getPath());
			byte[] body = "LEAKED".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.start();
		try {
			String base = "http://" + server.getAddress().getAddress().getHostAddress() + ":" + server.getAddress().getPort() + "/";
			Doc inner = new Doc();
			inner.chunk("/word/inner.htm", "text/html", ("<html><body><img src='" + base + "inner.png'></body></html>").getBytes(StandardCharsets.UTF_8));
			inner.p("<w:fldSimple w:instr=' INCLUDEPICTURE \"" + base + "inner-field.png\" \\d '>" + run("INNER") + "</w:fldSimple>");
			Doc d = new Doc();
			d.chunk("/word/c1.htm", "text/html", ("<html><head><link rel='stylesheet' href='" + base + "h.css'><script src='" + base
					+ "h.js'></script></head><body onload='go()'><p>HTML_CHUNK</p><img src='" + base + "h.png'></body></html>").getBytes(StandardCharsets.UTF_8));
			d.chunk("/word/c2.xhtml", "application/xhtml+xml", ("<html xmlns='http://www.w3.org/1999/xhtml'><head><link rel='stylesheet' href='"
					+ base + "x.css'/></head><body><p>XHTML_CHUNK</p><p><img src='" + base + "x.png' alt='x'/></p></body></html>").getBytes(StandardCharsets.UTF_8));
			d.chunk("/word/c3.mht", "message/rfc822", ("MIME-Version: 1.0\r\nContent-Type: multipart/related; boundary=\"B\"\r\n\r\n--B\r\n"
					+ "Content-Type: text/html; charset=\"utf-8\"\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\n"
					+ "<html><head><link rel=3D\"stylesheet\" href=3D\"" + base + "m.css\"></head><body><p>MHT_CHUNK</p></body></html>\r\n--B--\r\n")
					.getBytes(StandardCharsets.UTF_8));
			d.p("<w:fldSimple w:instr=' INCLUDEPICTURE \"" + base + "f.png\" \\d '>" + run("PIC") + "</w:fldSimple>");
			d.p("<w:fldSimple w:instr=' INCLUDETEXT \"" + base + "t.docx\" '>" + run("INCLUDED") + "</w:fldSimple>");
			d.p("<w:fldSimple w:instr=' HYPERLINK \"" + base + "page\" '>" + run("LINK") + "</w:fldSimple>");
			d.embed("/word/embeddings/inner.docx", inner.bytes());
			Relationship image = new org.docx4j.relationships.ObjectFactory().createRelationship();
			image.setType(REL + "image");
			image.setTarget(base + "linked.png");
			image.setTargetMode("External");
			d.mdp.getRelationshipsPart().addRelationship(image);
			WordprocessingMLPackage pkg = d.done();
			Inspection i = Inspect.of(pkg);
			assertEquals("requests", Collections.emptyList(), requests);
			assertEquals(3, of(i, InspectFinding.ALTCHUNK_EXTERNAL_REF).size());
			assertEquals(1, of(i, InspectFinding.ALTCHUNK_SCRIPT).size());
			assertEquals(3, of(i, InspectFinding.FIELD_EXTERNAL).size()); // two here, one in the embedded document
			assertEquals(1, of(i, InspectFinding.HYPERLINK_WEB).size());
			assertEquals(1, of(i, InspectFinding.RELATIONSHIP_EXTERNAL).size());
			assertTrue(i.skipped().toString(), i.skipped().contains(new Inspection.Entry(Inspection.DEPTH, "/word/embeddings/inner.docx|/word/inner.htm|")));
			// and the text the inspector's own reading of the document gives is unchanged by it
			assertEquals("requests", Collections.emptyList(), requests);
		} finally {
			server.stop(0);
		}
	}

	// --- the anonymiser's inventory -------------------------------------------------------------------

	/**
	 * Every part the anonymiser treats as a macro, an ActiveX control, custom XML, an embedding, a chunk
	 * or the people part is a finding at that part, and every external relationship it reads
	 * ({@code Verify.extract}'s walk: the package's and every part's relationships of TargetMode External)
	 * is a finding at its relationship (CR-050 section 3).
	 */
	@Test
	public void theAnonymisersInventoryIsFound() throws Exception {
		int parts = 0, relationships = 0;
		for (String name : FIXTURES) {
			WordprocessingMLPackage pkg = fixture(name);
			Inspection i = Inspect.of(pkg);
			Map<String, Set<String>> at = new LinkedHashMap<String, Set<String>>();
			for (InspectFinding f : i.findings()) {
				if (!f.getLocation().getChain().isEmpty()) continue;
				at.computeIfAbsent(f.getLocation().getPart(), k -> new HashSet<String>()).add(f.getKind());
			}
			Set<Part> embedded = new HashSet<Part>();
			MainDocumentPart mdp = pkg.getMainDocumentPart();
			for (Relationship r : mdp.getRelationshipsPart().getRelationships().getRelationship()) {
				if (r.getType().endsWith("/package") || r.getType().endsWith("/oleObject")) embedded.add(mdp.getRelationshipsPart().getPart(r));
			}
			for (Part p : pkg.getParts().getParts().values()) {
				String kind = p instanceof VbaProjectBinaryPart ? InspectFinding.PART_MACRO
						: p instanceof ActiveXControlXmlPart ? InspectFinding.PART_ACTIVEX
						: p instanceof CustomXmlDataStoragePart ? InspectFinding.CUSTOM_XML
						: embedded.contains(p) ? InspectFinding.OBJECT_EMBEDDED
						: p instanceof AlternativeFormatInputPart ? InspectFinding.ALTCHUNK
						: p instanceof PeoplePart ? InspectFinding.PEOPLE : null;
				if (kind == null) continue;
				parts++;
				String part = p.getPartName().getName();
				assertTrue(name + ": the anonymiser treats " + part, PartsAnalyzer.classify(p) != PartsAnalyzer.Treatment.SAFE);
				assertTrue(name + ": " + kind + " at " + part + " in " + at.get(part), at.getOrDefault(part, Collections.<String>emptySet()).contains(kind));
			}
			List<Base> sources = new ArrayList<Base>();
			sources.add(pkg);
			sources.addAll(pkg.getParts().getParts().values());
			for (Base b : sources) {
				RelationshipsPart rp = b.getRelationshipsPart();
				if (rp == null || rp.getRelationships() == null) continue;
				for (Relationship r : rp.getRelationships().getRelationship()) {
					if (!"External".equals(r.getTargetMode())) continue;
					relationships++;
					boolean found = false;
					for (InspectFinding f : i.findings()) {
						found |= f.getLocation().getChain().isEmpty() && f.getLocation().getPart().equals(rp.getPartName().getName())
								&& r.getTarget().equals(f.getSubject())
								&& (f.getKind().equals(InspectFinding.RELATIONSHIP_EXTERNAL) || f.getKind().equals(InspectFinding.HYPERLINK_WEB));
					}
					assertTrue(name + ": " + r.getTarget(), found);
				}
			}
		}
		assertEquals("parts checked", 19, parts);
		assertTrue("relationships checked: " + relationships, relationships >= 8);
	}

	// --- bounds (REQ-907) -----------------------------------------------------------------------------

	/**
	 * 800 chunks of 100 KB, each under the payload limit and 80 MB in all, end PARTIAL at the total: the
	 * chunks not decoded are skipped, and the counts are lower bounds. (CR-050 section 3 has 2,000 chunks
	 * of 40 KB, written before D11: each chunk an {@code altchunk} finding, D11's first limit of 1,000
	 * findings stopped those first, at 40 MB.) Plain-text chunks, decoded and counted as HTML chunks are:
	 * with an HTML reader, an HTML chunk is also imported (CR-053 phase 3), which the total does not need.
	 */
	@Test
	public void theTotalBytesStopTheInspection() throws Exception {
		Doc d = new Doc();
		byte[] page = page(100 * 1024);
		for (int n = 0; n < 800; n++) d.chunk(String.format("/word/c%04d.txt", n), "text/plain", page);
		Inspection i = Inspect.of(d.done());
		assertEquals(Inspection.Status.PARTIAL, i.status());
		assertTrue(reasons(i).toString(), reasons(i).contains(Inspection.LIMIT_TOTAL_BYTES));
		assertTrue(i.counts(null, Inspection.Format.PLAIN).isLowerBound());
		int decoded = of(i, InspectFinding.ALTCHUNK).size();
		assertTrue("chunks decoded: " + decoded, decoded > 600 && decoded < 700);
		int stopped = 0;
		for (Inspection.Entry e : i.skipped()) if (e.getReason().equals(Inspection.LIMIT_TOTAL_BYTES) && e.getLocation().startsWith("|/word/c")) stopped++;
		assertEquals("every chunk not decoded is skipped", 800 - decoded + 1, stopped);
	}

	private static byte[] page(int size) {
		StringBuilder sb = new StringBuilder("<html><body>");
		while (sb.length() < size - 20) sb.append("<p>A_CHUNK_LINE</p>\n");
		return sb.append("</body></html>").toString().getBytes(StandardCharsets.UTF_8);
	}

	/** A payload over 16 MB is stopped while it is decoded (a zip's entries as they decompress), and the rest is inspected. */
	@Test
	public void aPayloadOverItsLimitStopsAlone() throws Exception {
		Doc big = new Doc();
		BinaryPart filler = new BinaryPart(new PartName("/word/media/filler.bin"));
		filler.setContentType(new ContentType("application/octet-stream"));
		filler.setRelationshipType(REL + "image");
		filler.setBinaryData(new byte[17 * 1024 * 1024]);
		big.mdp.addTargetPart(filler);
		big.p(hidden("BIG_HIDDEN"));
		byte[] bigDocx = big.bytes();
		assertTrue("the embedded package compresses", bigDocx.length < 1024 * 1024);
		Doc d = new Doc();
		byte[] html = new byte[17 * 1024 * 1024];
		Arrays.fill(html, (byte) ' ');
		d.chunk("/word/a-big.htm", "text/html", html);
		d.chunk("/word/b-small.htm", "text/html", "<html><body><script>void 0</script></body></html>".getBytes(StandardCharsets.UTF_8));
		d.embed("/word/embeddings/big.docx", bigDocx);
		d.p(hidden("OUTER_HIDDEN"));
		Inspection i = Inspect.of(d.done());
		assertEquals(Inspection.Status.PARTIAL, i.status());
		assertTrue(i.skipped().toString(), i.skipped().contains(new Inspection.Entry(Inspection.LIMIT_PAYLOAD_BYTES, "|/word/a-big.htm|")));
		assertTrue(i.skipped().toString(), i.skipped().contains(new Inspection.Entry(Inspection.LIMIT_PAYLOAD_BYTES, "|/word/embeddings/big.docx|")));
		// without an HTML reader, the small chunk is skipped too, its script found (CR-053 phase 3)
		if (!HtmlChunkImport.readerPresent()) {
			assertTrue(i.skipped().toString(), i.skipped().contains(new Inspection.Entry(Inspection.NOT_DECODED, "|/word/b-small.htm|")));
		}
		assertEquals(HtmlChunkImport.readerPresent() ? 2 : 3, i.skipped().size());
		assertEquals(1, of(i, InspectFinding.ALTCHUNK_SCRIPT).size());
		assertEquals("the embedded package is not inspected", 1, of(i, InspectFinding.TEXT_HIDDEN).size());
	}

	/** Past {@value Inspect#MAX_FINDINGS} findings the inspection stops, PARTIAL, what it had not inspected skipped. */
	@Test
	public void theFindingsLimitStopsTheInspection() throws Exception {
		Doc d = new Doc();
		for (int n = 0; n < Inspect.MAX_FINDINGS + 200; n++) d.p(hidden("HIDDEN_" + n));
		Inspection i = Inspect.of(d.done());
		assertEquals(Inspect.MAX_FINDINGS, i.findings().size());
		assertEquals(Inspection.Status.PARTIAL, i.status());
		assertTrue(i.skipped().toString(), i.skipped().contains(new Inspection.Entry(Inspection.LIMIT_FINDINGS, "|/word/document.xml|")));
		assertTrue(i.counts(null, Inspection.Format.PLAIN).isLowerBound());
		assertNull("a stopped report is not read through the text model for its addresses", i.findings().get(0).getAddress());
	}

	/**
	 * The elements walked are counted while a part is read, and past the budget the inspection stops
	 * (a budget of 500 here, so that the test needs no document of five million elements).
	 */
	@Test
	public void theElementsLimitStopsTheInspection() throws Exception {
		Doc d = new Doc();
		for (int n = 0; n < 200; n++) d.p(run("PARAGRAPH_" + n));
		Inspection i = Inspect.of(d.done(), new InspectRun(Inspect.MAX_FINDINGS, Inspect.MAX_PAYLOAD_BYTES, Inspect.MAX_TOTAL_BYTES, 500));
		assertEquals(Inspection.Status.PARTIAL, i.status());
		assertTrue(i.skipped().toString(), i.skipped().contains(new Inspection.Entry(Inspection.LIMIT_ELEMENTS, "|/word/document.xml|")));
		Inspection whole = Inspect.of(d.pkg);
		assertEquals(Inspection.Status.COMPLETE, whole.status());
	}

	/** The skipped entries are listed to 1,000, and the rest counted. */
	@Test
	public void theSkippedEntriesAreBounded() throws Exception {
		Doc d = new Doc();
		for (int n = 0; n < 1100; n++) d.chunk(String.format("/word/r%04d.rtf", n), "text/rtf", "{\\rtf1 x}".getBytes(StandardCharsets.UTF_8));
		Inspection i = Inspect.of(d.done());
		assertEquals(Inspect.MAX_SKIPPED, i.skipped().size());
		assertTrue("beyond: " + i.skippedBeyond(), i.skippedBeyond() >= 100);
		assertEquals(Inspection.Status.PARTIAL, i.status());
	}

	// --- locations ------------------------------------------------------------------------------------

	/** Two embedded documents each holding a VBA project and hidden text at one path: four findings at four locations. */
	@Test
	public void nestedLocationsNameTheirPackage() throws Exception {
		Doc d = new Doc();
		for (String name : new String[] { "/word/embeddings/one.docx", "/word/embeddings/two.docx" }) {
			Doc inner = new Doc();
			BinaryPart vba = new BinaryPart(new PartName("/word/vbaProject.bin"));
			vba.setContentType(new ContentType("application/vnd.ms-office.vbaProject"));
			vba.setRelationshipType("http://schemas.microsoft.com/office/2006/relationships/vbaProject");
			vba.setBinaryData(new byte[] { 1, 2, 3, 4 });
			inner.mdp.addTargetPart(vba);
			inner.p(hidden("NESTED_HIDDEN"));
			d.embed(name, inner.bytes());
		}
		Inspection i = Inspect.of(d.done());
		Set<InspectFinding.Location> locations = new HashSet<InspectFinding.Location>();
		for (String kind : new String[] { InspectFinding.PART_MACRO, InspectFinding.TEXT_HIDDEN }) {
			List<InspectFinding> found = of(i, kind);
			assertEquals(kind, 2, found.size());
			for (InspectFinding f : found) {
				assertEquals(1, f.getLocation().getChain().size());
				locations.add(f.getLocation());
			}
		}
		assertEquals(4, locations.size());
		InspectFinding h = of(i, InspectFinding.TEXT_HIDDEN).get(0);
		assertEquals("/w:document[1]/w:body[1]/w:p[1]/w:r[1]", h.getLocation().getPath());
		assertEquals("options: hidden: true; embedded: true", h.getReveal().toString());
		assertEquals("body/0/object:0/body/0", h.getAddress());
		assertEquals(Group.OMITTED, h.group(new TextOptions().embedded(true), Inspection.Format.PLAIN, null));
		assertEquals(Group.UNSEEN, h.group(new TextOptions().embedded(true).hidden(true), Inspection.Format.PLAIN, null));
	}

	/** The revision-session identifiers are one finding, their number its summary (no fixture holds them: Word writes them on every save). */
	@Test
	public void rsids() throws Exception {
		Doc d = new Doc();
		d.p(run("RSIDS"));
		WordprocessingMLPackage pkg = d.done();
		DocumentSettingsPart settings = pkg.getMainDocumentPart().getDocumentSettingsPart(true);
		settings.setJaxbElement((org.docx4j.wml.CTSettings) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:settings" + NS + "><w:rsids>"
				+ "<w:rsidRoot w:val='00A1B2C3'/><w:rsid w:val='00A1B2C3'/><w:rsid w:val='00D4E5F6'/><w:rsid w:val='00778899'/>"
				+ "</w:rsids></w:settings>")));
		Inspection i = Inspect.of(pkg);
		List<InspectFinding> rsids = of(i, InspectFinding.RSIDS);
		assertEquals(1, rsids.size());
		assertEquals("4", rsids.get(0).getSummary());
		assertEquals("/w:settings[1]/w:rsids[1]", rsids.get(0).getLocation().getPath());
		assertEquals(Group.INFO, rsids.get(0).group(null, Inspection.Format.PLAIN, null));
		assertEquals(Reveal.Form.UNEXTRACTABLE, rsids.get(0).getReveal().getForm());
	}

	// --- membership and counts (REQ-905, REQ-903) -------------------------------------------------------

	/** White text in a hidden run, deleted text, text deleted in an insertion and inserted text, under each view and option. */
	@Test
	public void membershipFollowsTheOptions() throws Exception {
		Doc d = new Doc();
		d.p("<w:r><w:rPr><w:vanish/><w:color w:val='FFFFFF'/></w:rPr><w:t>WHITE_HIDDEN</w:t></w:r>");
		d.p("<w:del w:id='1' w:author='A' w:date='2026-10-19T00:00:00Z'><w:r><w:delText>DELETED_TEXT</w:delText></w:r></w:del>");
		d.p("<w:ins w:id='2' w:author='A' w:date='2026-10-19T00:00:00Z'><w:del w:id='3' w:author='B' w:date='2026-10-19T00:00:00Z'>"
				+ "<w:r><w:delText>DELETED_IN_INSERTION</w:delText></w:r></w:del></w:ins>");
		d.p("<w:ins w:id='4' w:author='A' w:date='2026-10-19T00:00:00Z'><w:r><w:t>INSERTED​MARK</w:t></w:r></w:ins>");
		Inspection i = Inspect.of(d.done());
		TextOptions defaults = new TextOptions(), hidden = new TextOptions().hidden(true);
		TextOptions original = new TextOptions().view(TextView.ORIGINAL), markup = new TextOptions().view(TextView.MARKUP);
		Inspection.Format plain = Inspection.Format.PLAIN;
		InspectFinding white = of(i, InspectFinding.TEXT_NEAR_BACKGROUND).get(0);
		assertEquals(Group.OMITTED, white.group(defaults, plain, null));
		assertEquals(Group.UNSEEN, white.group(hidden, plain, null));
		assertEquals("options: hidden: true", of(i, InspectFinding.TEXT_HIDDEN).get(0).getReveal().toString());
		List<InspectFinding> deleted = of(i, InspectFinding.REVISION_DELETED);
		assertEquals(2, deleted.size());
		InspectFinding plainDeletion = deleted.get(0), nested = deleted.get(1);
		assertEquals(Group.OMITTED, plainDeletion.group(defaults, plain, null));
		assertEquals(Group.UNSEEN, plainDeletion.group(original, plain, null));
		assertEquals(Group.UNSEEN, plainDeletion.group(markup, plain, null));
		assertEquals("body/1", plainDeletion.getAddress());
		assertEquals(Group.OMITTED, nested.group(original, plain, null));
		assertEquals(Group.UNSEEN, nested.group(markup, plain, null));
		assertEquals("options: view: markup", nested.getReveal().toString());
		InspectFinding inserted = of(i, InspectFinding.CHARS_ZERO_WIDTH).get(0);
		assertEquals(Reveal.INCLUDED, inserted.getReveal());
		assertEquals(Group.UNSEEN, inserted.group(defaults, plain, null));
		assertEquals("inserted content is not in the original view", Group.OMITTED, inserted.group(original, plain, null));
		assertEquals(Group.UNSEEN, inserted.group(markup, plain, null));
		assertEquals(2, of(i, InspectFinding.REVISION_AUTHOR).size());
	}

	/** On the fixtures: a picture's description, a comment, the properties and an embedded workbook's hidden row. */
	@Test
	public void membershipOnTheFixtures() throws Exception {
		Set<Inspection.Capability> none = EnumSet.noneOf(Inspection.Capability.class);
		Inspection probe = Inspect.of(fixture("extraction-probe.docx"));
		InspectFinding description = null;
		for (InspectFinding f : of(probe, InspectFinding.ALT_TEXT_DESCRIPTION)) if (f.getReveal().getForm() == Reveal.Form.OPTIONS) description = f;
		assertNotNull(description);
		assertEquals(Group.OMITTED, description.group(null, Inspection.Format.PLAIN, none));
		assertEquals(Group.UNSEEN, description.group(null, Inspection.Format.MARKDOWN, none));
		assertEquals(Group.UNSEEN, description.group(new TextOptions().altText(true), Inspection.Format.PLAIN, none));
		InspectFinding comment = of(probe, InspectFinding.COMMENT).get(0);
		assertEquals(Group.OMITTED, comment.group(null, Inspection.Format.PLAIN, none));
		assertEquals(Group.INFO, comment.group(new TextOptions().view(TextView.MARKUP), Inspection.Format.PLAIN, none));
		InspectFinding properties = of(probe, InspectFinding.PROPERTIES).get(0);
		assertEquals(Group.INFO, properties.group(null, Inspection.Format.BLOCKS, none));
		assertEquals(Group.OMITTED, properties.group(null, Inspection.Format.MARKDOWN, none));
		assertEquals(Group.INFO, properties.group(new TextOptions().metadata(true), Inspection.Format.PLAIN, none));
		assertNull(properties.getAddress());
		Inspection inspect = Inspect.of(fixture("extraction-probe-inspect.docx"));
		InspectFinding row = of(inspect, InspectFinding.EMBEDDED_ROW_HIDDEN).get(0);
		assertEquals(Group.OMITTED, row.group(null, Inspection.Format.PLAIN, none));
		assertEquals(Group.UNSEEN, row.group(new TextOptions().embedded(true), Inspection.Format.PLAIN, none));
		assertEquals(Arrays.asList("/word/embeddings/probe66.xlsx"), row.getLocation().getChain());
	}

	/** The counts are the tally of the findings' groups, and splitting every run in two moves none of them (REQ-906). */
	@Test
	public void countsSurviveSplitRuns() throws Exception {
		TextOptions[] options = { null, new TextOptions().hidden(true), new TextOptions().embedded(true), new TextOptions().comments(true),
				new TextOptions().view(TextView.ORIGINAL), new TextOptions().view(TextView.MARKUP) };
		for (String name : FIXTURES) {
			WordprocessingMLPackage pkg = fixture(name);
			Inspection before = Inspect.of(pkg);
			int split = split(pkg.getMainDocumentPart().getContent());
			assertTrue(name + ": runs split " + split, split > 0);
			Inspection after = Inspect.of(pkg);
			for (TextOptions o : options) {
				for (Inspection.Format format : Inspection.Format.values()) {
					Inspection.Counts c = before.counts(o, format);
					int[] tally = new int[Group.values().length];
					for (InspectFinding f : before.findings()) tally[f.group(o, format, null).ordinal()]++;
					for (Group g : Group.values()) assertEquals(name + " " + g, tally[g.ordinal()], c.get(g));
					assertEquals(name + " " + format + " " + (o == null ? "defaults" : o.getView()), c, after.counts(o, format));
				}
			}
		}
	}

	/** Every run of one {@code w:t} or {@code w:delText} of two code points or more, split into two runs. */
	private static int split(List<Object> content) {
		int n = 0;
		for (int i = 0; i < content.size(); i++) {
			Object o = content.get(i);
			Object v = XmlUtils.unwrap(o);
			if (v instanceof R && o == v && ((R) v).getContent().size() == 1 && ((R) v).getContent().get(0) instanceof JAXBElement) {
				JAXBElement<?> e = (JAXBElement<?>) ((R) v).getContent().get(0);
				String local = e.getName().getLocalPart();
				if (e.getValue() instanceof Text && (local.equals("t") || local.equals("delText"))) {
					String s = ((Text) e.getValue()).getValue();
					int cps = s == null ? 0 : s.codePointCount(0, s.length());
					if (cps >= 2) {
						int mid = s.offsetByCodePoints(0, cps / 2);
						R second = XmlUtils.deepCopy((R) v);
						((Text) e.getValue()).setValue(s.substring(0, mid));
						((Text) e.getValue()).setSpace("preserve");
						Text t2 = (Text) ((JAXBElement<?>) second.getContent().get(0)).getValue();
						t2.setValue(s.substring(mid));
						t2.setSpace("preserve");
						content.add(i + 1, second);
						i++;
						n++;
						continue;
					}
				}
			}
			if (v instanceof ContentAccessor) n += split(((ContentAccessor) v).getContent());
			else if (v instanceof SdtElement && ((SdtElement) v).getSdtContent() != null) n += split(((SdtElement) v).getSdtContent().getContent());
		}
		return n;
	}

	// --- the rest of the contract ---------------------------------------------------------------------

	/** A row or cell deleted (inserted) by its own mark, its runs in no {@code w:del} ({@code w:ins}), is out of the accepted (original) view, as the text model reads it. */
	@Test
	public void rowAndCellMarksDecideMembership() throws Exception {
		String mark = " w:author='A' w:date='2026-10-19T00:00:00Z'/>";
		Doc d = new Doc();
		d.body.append("<w:tbl><w:tblGrid><w:gridCol w:w='2000'/><w:gridCol w:w='2000'/></w:tblGrid>"
				+ "<w:tr><w:tc><w:p>" + run("KEEP") + "</w:p></w:tc><w:tc><w:p>" + run("KEEP") + "</w:p></w:tc></w:tr>"
				+ "<w:tr><w:trPr><w:del w:id='1'" + mark + "</w:trPr><w:tc><w:p>" + hidden("ROW_DELETED") + "</w:p></w:tc><w:tc><w:p/></w:tc></w:tr>"
				+ "<w:tr><w:trPr><w:ins w:id='2'" + mark + "</w:trPr><w:tc><w:p>" + hidden("ROW_INSERTED") + "</w:p></w:tc><w:tc><w:p/></w:tc></w:tr>"
				+ "<w:tr><w:tc><w:tcPr><w:cellDel w:id='3'" + mark + "</w:tcPr><w:p>" + hidden("CELL_DELETED") + "</w:p></w:tc>"
				+ "<w:tc><w:tcPr><w:cellIns w:id='4'" + mark + "</w:tcPr><w:p>" + hidden("CELL_INSERTED") + "</w:p></w:tc></w:tr></w:tbl>");
		WordprocessingMLPackage pkg = d.done();
		TextOptions hidden = new TextOptions().hidden(true), original = new TextOptions().hidden(true).view(TextView.ORIGINAL);
		String accepted = Texts.plainText(pkg, hidden), rejected = Texts.plainText(pkg, original);
		Inspection i = Inspect.of(pkg);
		Map<String, InspectFinding> byText = new LinkedHashMap<String, InspectFinding>();
		for (InspectFinding f : of(i, InspectFinding.TEXT_HIDDEN)) byText.put(f.getExcerpt(), f);
		assertEquals(Arrays.asList("ROW_DELETED", "ROW_INSERTED", "CELL_DELETED", "CELL_INSERTED"), new ArrayList<String>(byText.keySet()));
		for (InspectFinding f : byText.values()) {
			boolean deleted = f.getExcerpt().endsWith("_DELETED");
			assertEquals(f.getExcerpt(), !deleted, accepted.contains(f.getExcerpt()));
			assertEquals(f.getExcerpt(), deleted, rejected.contains(f.getExcerpt()));
			assertEquals(f.getExcerpt(), deleted ? Group.OMITTED : Group.UNSEEN, f.group(hidden, Inspection.Format.PLAIN, null));
			assertEquals(f.getExcerpt(), deleted ? Group.UNSEEN : Group.OMITTED, f.group(original, Inspection.Format.PLAIN, null));
		}
		assertEquals("options: view: original; hidden: true", byText.get("ROW_DELETED").getReveal().toString());
		assertEquals("body/0/1/0/0", byText.get("ROW_DELETED").getAddress());
		assertEquals("body/0/3/0/0", byText.get("CELL_DELETED").getAddress());
		assertEquals("a row's or a cell's mark alone is no deleted text", 0, of(i, InspectFinding.REVISION_DELETED).size());
	}

	/** Text in a control showing its placeholder is in the output under {@code placeholders: true} only, as the text model reads it. */
	@Test
	public void placeholderContentNeedsPlaceholders() throws Exception {
		Doc d = new Doc();
		d.p("<w:sdt><w:sdtPr><w:showingPlcHdr/></w:sdtPr><w:sdtContent><w:r><w:rPr><w:color w:val='FFFFFF'/></w:rPr>"
				+ "<w:t>WHITE_PLACEHOLDER</w:t></w:r></w:sdtContent></w:sdt>");
		WordprocessingMLPackage pkg = d.done();
		TextOptions none = new TextOptions().placeholders(false);
		assertTrue(Texts.plainText(pkg, new TextOptions()).contains("WHITE_PLACEHOLDER"));
		assertFalse(Texts.plainText(pkg, none).contains("WHITE_PLACEHOLDER"));
		Inspection i = Inspect.of(pkg);
		InspectFinding white = of(i, InspectFinding.TEXT_NEAR_BACKGROUND).get(0);
		assertEquals(Reveal.INCLUDED, white.getReveal());
		assertEquals(Group.UNSEEN, white.group(null, Inspection.Format.PLAIN, null));
		assertEquals(Group.OMITTED, white.group(none, Inspection.Format.PLAIN, null));
		assertEquals(1, of(i, InspectFinding.SDT_PLACEHOLDER).size());
	}

	/** A note the text model reaches only through a used header is read with the headers; one referenced only from an unused header, never. */
	@Test
	public void notesReferencedFromHeaders() throws Exception {
		Doc d = new Doc();
		d.p(run("BODY"));
		FootnotesPart notes = new FootnotesPart();
		notes.setJaxbElement((org.docx4j.wml.CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:footnotes" + NS + ">"
				+ "<w:footnote w:id='1'><w:p>" + run("UNUSED_HEADER_NOTE") + "</w:p></w:footnote>"
				+ "<w:footnote w:id='2'><w:p>" + run("USED_HEADER_NOTE") + "</w:p></w:footnote></w:footnotes>")));
		d.mdp.addTargetPart(notes);
		String used = header(d, "/word/header1.xml", "2"), unused = header(d, "/word/header2.xml", "1");
		assertNotNull(unused);
		d.sectPr = "<w:sectPr><w:headerReference w:type='default' r:id='" + used + "'/></w:sectPr>";
		WordprocessingMLPackage pkg = d.done();
		TextOptions headers = new TextOptions().include(TextOptions.Include.HEADERS);
		assertFalse(Texts.plainText(pkg, new TextOptions()).contains("USED_HEADER_NOTE"));
		assertTrue(Texts.plainText(pkg, headers).contains("USED_HEADER_NOTE"));
		assertFalse(Texts.plainText(pkg, headers).contains("UNUSED_HEADER_NOTE"));
		Inspection i = Inspect.of(pkg);
		List<InspectFinding> note = of(i, InspectFinding.NOTE);
		assertEquals(1, note.size());
		assertEquals("USED_HEADER_NOTE", note.get(0).getExcerpt());
		assertEquals("options: include: headers", note.get(0).getReveal().toString());
		assertEquals(Group.OMITTED, note.get(0).group(null, Inspection.Format.PLAIN, null));
		assertEquals(Group.INFO, note.get(0).group(headers, Inspection.Format.PLAIN, null));
		List<String> unusedStories = new ArrayList<String>();
		for (InspectFinding f : of(i, InspectFinding.STORY_UNUSED)) unusedStories.add(f.getExcerpt());
		assertTrue(unusedStories.toString(), unusedStories.contains("UNUSED_HEADER_NOTE"));
	}

	private static String header(Doc d, String name, String noteId) throws Exception {
		HeaderPart h = new HeaderPart(new PartName(name));
		h.setJaxbElement((org.docx4j.wml.Hdr) XmlUtils.unmarshalString("<w:hdr" + NS + "><w:p>" + run("HEADER")
				+ "<w:r><w:footnoteReference w:id='" + noteId + "'/></w:r></w:p></w:hdr>"));
		return d.mdp.addTargetPart(h).getId();
	}

	/** SmartArt data its drawing part does not show: none in Word's own save of the probe (case 35), one point's where the drawing is stale. */
	@Test
	public void smartArtDataTheDrawingDoesNotShow() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(ViewAgreementTest.resources()
				.resolve("text-extraction/extraction-probe.word-resave.docx").toFile());
		assertTrue(of(Inspect.of(pkg), InspectFinding.SMARTART_DATA).isEmpty());
		JaxbXmlPart<?> drawing = (JaxbXmlPart<?>) pkg.getParts().get(new PartName("/word/diagrams/drawing1.xml"));
		String xml = XmlUtils.marshaltoString(drawing.getJaxbElement(), true, false, drawing.getJAXBContext());
		assertTrue(xml.contains("SMA35_CHILD_TWO"));
		drawing.unmarshal(new ByteArrayInputStream(xml.replace("SMA35_CHILD_TWO", "SMA35_STALE").getBytes(StandardCharsets.UTF_8)));
		List<InspectFinding> data = of(Inspect.of(pkg), InspectFinding.SMARTART_DATA);
		assertEquals(1, data.size());
		InspectFinding f = data.get(0);
		assertEquals("SMA35_CHILD_TWO", f.getExcerpt());
		assertEquals("/word/diagrams/data1.xml", f.getLocation().getPart());
		assertEquals("/dgm:dataModel[1]/dgm:ptLst[1]/dgm:pt[2]", f.getLocation().getPath());
		assertEquals("body/105/smartart:0/2", f.getAddress());
		assertEquals(Visible.NO, f.getVisible());
		assertEquals(Reveal.INCLUDED, f.getReveal());
		assertEquals(Group.UNSEEN, f.group(null, Inspection.Format.PLAIN, null));
		assertEquals(Group.OMITTED, f.group(new TextOptions().smartart(false), Inspection.Format.PLAIN, null));
	}

	/**
	 * A face whose cmap has 65,535 encoding records, each its own subtable mapping every code point
	 * (format 12, from glyph 1 on), costs one face's budget: read record by record, it would be 72
	 * thousand million steps.
	 */
	@Test(timeout = 30000)
	public void aHostileCmapCostsOneFacesBudget() throws Exception {
		byte[] font = Files.readAllBytes(ViewAgreementTest.resources().resolve("text-extraction/support/inspect-fonts/inspect-probe-plain.ttf"));
		int dir = -1;
		for (int t = 0, n = ByteBuffer.wrap(font).getShort(4) & 0xffff; t < n; t++) {
			if (new String(font, 12 + 16 * t, 4, StandardCharsets.ISO_8859_1).equals("cmap")) dir = 12 + 16 * t;
		}
		int records = 65535, sub = 28;
		ByteBuffer cmap = ByteBuffer.allocate(4 + 8 * records + sub * records);
		cmap.putShort((short) 0).putShort((short) records);
		for (int r = 0; r < records; r++) cmap.putShort((short) 3).putShort((short) 10).putInt(4 + 8 * records + sub * r);
		for (int r = 0; r < records; r++) cmap.putShort((short) 12).putShort((short) 0).putInt(sub).putInt(0).putInt(1).putInt(0).putInt(0x10FFFF).putInt(1);
		byte[] hostile = Arrays.copyOf(font, font.length + cmap.capacity());
		System.arraycopy(cmap.array(), 0, hostile, font.length, cmap.capacity());
		ByteBuffer.wrap(hostile).putInt(dir + 8, font.length).putInt(dir + 12, cmap.capacity());
		InspectFace face = InspectFace.read(hostile);
		assertEquals(Integer.valueOf(1), face.glyph(0));
		assertEquals(Integer.valueOf(2), face.glyph(1));
	}

	/**
	 * A chunk is read in its own encoding (UTF-16 with a byte order mark, a declared charset), and a
	 * {@code javascript:} URI is a handler, not a reference. UTF-16 without a byte order mark is read as
	 * its bytes say, as Word reads it (WD 0.28, CR-053 {@code r11-9}): its handler is no script.
	 */
	@Test
	public void chunkEncodingsAndScriptURIs() throws Exception {
		Doc d = new Doc();
		d.chunk("/word/bom.htm", "text/html", "\uFEFF<html><body><script>go()</script></body></html>".getBytes(StandardCharsets.UTF_16LE));
		d.chunk("/word/be.htm", "text/html", "<html><body onload='go()'></body></html>".getBytes(StandardCharsets.UTF_16BE));
		d.chunk("/word/js.htm", "text/html", "<html><body><a href=' java\tscript:go()'>j</a></body></html>".getBytes(StandardCharsets.UTF_8));
		d.chunk("/word/plain.htm", "text/html", "<html><body><p>caf\u00e9</p></body></html>".getBytes(StandardCharsets.UTF_8));
		Inspection i = Inspect.of(d.done());
		List<String> scripts = new ArrayList<String>();
		for (InspectFinding f : of(i, InspectFinding.ALTCHUNK_SCRIPT)) scripts.add(f.getLocation().getPart());
		assertEquals(Arrays.asList("/word/bom.htm", "/word/js.htm"), scripts);
		assertTrue(of(i, InspectFinding.ALTCHUNK_EXTERNAL_REF).isEmpty());
		String declared = "<html><head><meta charset=windows-1252></head><body><p>caf\u00e9</p></body></html>";
		assertEquals(declared, InspectPayloads.decode(declared.getBytes("windows-1252")));
		String xhtml = "<?xml version='1.0' encoding='ISO-8859-1'?><html><p>\u00e9</p></html>";
		assertEquals(xhtml, InspectPayloads.decode(xhtml.getBytes(StandardCharsets.ISO_8859_1)));
		assertTrue(InspectPayloads.scanTags("<a href='JavaScript:go()'>j</a>").script);
	}

	/** The tag scanner reads a chunk of script elements in one pass: 16 MB of them is quick. */
	@Test(timeout = 30000)
	public void theTagScannerIsLinear() {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 16 << 20) sb.append("<SCRIPT>x</SCRIPT>");
		assertTrue(InspectPayloads.scanTags(sb.toString()).script);
	}

	/** Every inspection states REQ-904's limitations, each starting with its identifier. */
	@Test
	public void limitationsAreStated() throws Exception {
		Inspection i = Inspect.of(fixture("extraction-probe-structure.docx"));
		List<String> ids = new ArrayList<String>();
		for (String l : i.limitations()) ids.add(l.substring(0, l.indexOf(':')));
		assertEquals(Arrays.asList("layout.covered", "layout.position", "layout.clipped", "background.image", "font.careful_swap",
				"payload.opaque", "chart.workbook", "intent"), ids);
	}

	/** The tag scanner, for where jsoup is absent, finds what jsoup's parser does; neither reads a comment or a script's body as markup. */
	@Test
	public void theTagScannerAgreesWithJsoup() {
		String[] pages = { "<p>none</p>", "<SCRIPT>x</SCRIPT>", "<body ONLOAD=go()>", "<img src=http://h/i.png>",
				"<img src='#frag'><a href=\"mailto:x@y\">m</a><a href='data:,x'>d</a>", "<table><tr><td background=\"b.png\">t</td></tr></table>",
				"<!-- <img src='http://h/c.png'> --><p>c</p>", "<script>document.write('<img src=http://h/w.png>')</script>",
				"<a href = 'rel/page.htm' >r</a><link rel=stylesheet href=s.css>" };
		for (String page : pages) {
			InspectPayloads.Scan tags = InspectPayloads.scanTags(page);
			InspectPayloads.Scan jsoup = InspectPayloads.html(page);
			assertEquals(page, jsoup.script, tags.script);
			assertEquals(page, jsoup.refs, tags.refs);
		}
		assertTrue(InspectPayloads.jsoup());
		assertEquals(Arrays.asList("http://h/i.png"), InspectPayloads.scanTags(pages[3]).refs);
		assertFalse(InspectPayloads.scanTags(pages[6]).script);
		assertTrue(InspectPayloads.scanTags(pages[7]).refs.isEmpty());
	}
}
