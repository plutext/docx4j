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
package org.docx4j.textextraction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * {@code part.not_read} (WD 0.66 REQ-801, CR-046 section 10): a styles, numbering or settings part that the reading
 * needs and cannot read is read as if the package lacked it, and the result says so, once, naming the part. The
 * fixture is text-extraction/part-not-read (build_part_not_read.py); its expected text and warnings are the ports'
 * oracle too.
 */
public class PartNotReadTest {

	static Path dir() {
		return CompactProbeTest.resources().resolve("text-extraction/part-not-read");
	}

	private static TextOptions options(TextView view) {
		return new TextOptions().listLabels(true).smallCaps(TextOptions.SmallCaps.UPPER).view(view);
	}

	private static TextResult read(String fixture, TextView view) throws Exception {
		return Texts.of(WordprocessingMLPackage.load(dir().resolve(fixture).toFile()), options(view));
	}

	private static String warnings(TextResult r) {
		StringBuilder sb = new StringBuilder();
		for (TextWarning w : r.warnings()) sb.append(sb.length() == 0 ? "" : "; ").append(w.getKind()).append(' ').append(w.getSelector());
		return sb.toString();
	}

	/** Every fixture in every view: the text and the warnings expected (expected/, expected-warnings.tsv). */
	@Test
	public void eachFixtureReadsAsExpected() throws Exception {
		List<String> rows = Files.readAllLines(dir().resolve("expected-warnings.tsv"), StandardCharsets.UTF_8);
		assertEquals("fixture\tview\twarnings", rows.get(0));
		assertEquals(8 * 3, rows.size() - 1);
		for (String row : rows.subList(1, rows.size())) {
			String[] f = row.split("\t", -1);
			TextResult r = read(f[0], TextView.valueOf(f[1].toUpperCase(Locale.ROOT)));
			String expected = Files.readString(dir().resolve("expected/" + f[0].replace(".docx", "") + "." + f[1] + ".txt"),
					StandardCharsets.UTF_8);
			assertEquals(row, expected, PlainText.of(r));
			assertEquals(row, f[2], warnings(r));
		}
	}

	/** The rule itself: a part not well formed reads as the same part absent, in every view. */
	@Test
	public void aPartThatCannotBeReadReadsAsAPartThatIsAbsent() throws Exception {
		for (String part : new String[] { "styles", "numbering", "settings" }) {
			for (TextView v : TextView.values()) {
				TextResult broken = read("part-not-read-" + part + ".docx", v);
				assertEquals(part + " " + v, PlainText.of(read("part-not-read-" + part + "-absent.docx", v)), PlainText.of(broken));
				assertEquals(part + " " + v, "part.not_read /word/" + part + ".xml", warnings(broken));
			}
		}
	}

	/**
	 * Fix (a) of WD 0.65, outside the text model too: the numbering emulator resolves a paragraph's own numbering
	 * with no styles where the styles part cannot be read. Before 17.3.6 its property resolver failed on the part,
	 * so no paragraph had a number, a directly numbered one included.
	 */
	@Test
	public void theEmulatorCountsDirectNumberingWithoutReadableStyles() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(dir().resolve("part-not-read-styles.docx").toFile());
		List<Object> body = pkg.getMainDocumentPart().getContent();
		Emulator.NumberingResult first = Emulator.getNumber(pkg, ((P) body.get(2)).getPPr());
		assertNotNull(first);
		assertEquals("1.", first.getNumString());
		assertEquals("2.", Emulator.getNumber(pkg, ((P) body.get(3)).getPPr()).getNumString());
		assertEquals(null, Emulator.getNumber(pkg, ((P) body.get(0)).getPPr()));   // numbered by its style, which is lost
	}

	/** The reading changes nothing: an untouched save keeps the unreadable part's bytes (CR-065 D1). */
	@Test
	public void theUnreadablePartIsKept() throws Exception {
		Path fixture = dir().resolve("part-not-read-styles.docx");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(fixture.toFile());
		Texts.of(pkg, options(TextView.ACCEPTED));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		assertArrayEquals(entry(Files.readAllBytes(fixture), "word/styles.xml"), entry(bos.toByteArray(), "word/styles.xml"));
	}

	/** One level down, the warning names the embedded package's part, the one the host has (REQ-801's rule). */
	@Test
	public void insideAnEmbeddedDocumentTheWarningNamesItsPart() throws Exception {
		WordprocessingMLPackage outer = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = outer.getMainDocumentPart();
		EmbeddedPackagePart embedded = new EmbeddedPackagePart(new PartName("/word/embeddings/styles-not-read.docx"));
		embedded.setContentType(new ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
		embedded.setBinaryData(Files.readAllBytes(dir().resolve("part-not-read-styles.docx")));
		String rId = mdp.addTargetPart(embedded).getId();
		mdp.getContent().add(XmlUtils.unmarshalString("<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:r><w:object><o:OLEObject Type=\"Embed\" ProgID=\"Word.Document.12\" r:id=\"" + rId + "\"/></w:object></w:r></w:p>"));
		TextResult r = Texts.of(outer, options(TextView.ACCEPTED).embedded(true));
		assertEquals("part.not_read /word/embeddings/styles-not-read.docx", warnings(r));
		assertTrue(PlainText.of(r), PlainText.of(r).contains("1.\tfirst item"));
	}

	private static byte[] entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry e; (e = zis.getNextEntry()) != null; ) if (e.getName().equals(name)) return zis.readAllBytes();
		}
		throw new AssertionError(name + " is not in the package");
	}
}
