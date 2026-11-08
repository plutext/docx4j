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
package org.docx4j.anon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The command line: exit 0 and a JSON report when clean, 1 in KEEP mode with
 * something kept, 2 on a usage error, 3 when the load refuses the package.
 */
public class AnonymizeCliTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File copy(String resource) throws Exception {
		File f = tmp.newFile(resource.substring(resource.lastIndexOf('/') + 1));
		Files.write(f.toPath(), AnonymizeProbesTest.resource(resource));
		return f;
	}

	@Test
	public void strictCleanExitsZero() throws Exception {
		File in = copy("anon/chart.docx");
		File out = new File(tmp.getRoot(), "out.docx");
		File json = new File(tmp.getRoot(), "report.json");
		int exit = AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--json", json.getPath(), "--no-fonts" });
		assertEquals(0, exit);
		assertTrue(out.length() > 0);
		String report = new String(Files.readAllBytes(json.toPath()), StandardCharsets.UTF_8);
		assertTrue(report, report.contains("\"clean\": true"));
		assertTrue(report, report.contains("\"verified\": true"));
		WordprocessingMLPackage.load(out); // reloads
	}

	@Test
	public void keepWithAnEmbeddingExitsOne() throws Exception {
		File in = copy("OLE/inserted doc.docx");
		File out = new File(tmp.getRoot(), "out.docx");
		int exit = AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--keep", "--no-fonts" });
		assertEquals(1, exit);
		assertTrue("the output is still written", out.length() > 0);
	}

	@Test
	public void pptxByPackageKind() throws Exception {
		// the package decides the format, not the extension
		File in = copy("anon/table.pptx");
		File out = new File(tmp.getRoot(), "out.pptx");
		File json = new File(tmp.getRoot(), "report.json");
		int exit = AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--json", json.getPath() });
		assertEquals(0, exit);
		String report = new String(Files.readAllBytes(json.toPath()), StandardCharsets.UTF_8);
		assertTrue(report, report.contains("\"clean\": true"));
		assertTrue(report, report.contains("/ppt/slides/slide1.xml"));
		assertTrue(org.docx4j.openpackaging.packages.OpcPackage.load(out) instanceof org.docx4j.openpackaging.packages.PresentationMLPackage);
	}

	@Test
	public void xlsxWithNumbersKept() throws Exception {
		File in = copy("cr022-sparklines.xlsx");
		File out = new File(tmp.getRoot(), "out.xlsx");
		File json = new File(tmp.getRoot(), "report.json");
		int exit = AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--keep-numbers", "--json", json.getPath() });
		assertEquals(0, exit);
		String report = new String(Files.readAllBytes(json.toPath()), StandardCharsets.UTF_8);
		assertTrue(report, report.contains("\"clean\": true"));
		assertTrue(report, report.contains("/xl/worksheets/sheet1.xml"));
		assertTrue(org.docx4j.openpackaging.packages.OpcPackage.load(out) instanceof org.docx4j.openpackaging.packages.SpreadsheetMLPackage);
	}

	@Test
	public void usage() {
		assertEquals(2, AnonymizeCli.run(new String[] {}));
		assertEquals(2, AnonymizeCli.run(new String[] { "a.docx" }));
		assertEquals(2, AnonymizeCli.run(new String[] { "a.docx", "b.docx", "--bogus" }));
		assertEquals(2, AnonymizeCli.run(new String[] { "a.docx", "b.docx", "--repair" }));
		assertEquals(2, AnonymizeCli.run(new String[] { "a.docx", "b.docx", "--repair", "bogus" }));
	}

	/** A package Word opens only through its repair prompt is refused, as the library refuses it, and the hint names --repair (17.3.6, CR-075). */
	@Test
	public void aPackageWordRepairsIsRefusedWithAHint() throws Exception {
		File in = copy("package-wiring/dup-entry-document.docx");
		File out = new File(tmp.getRoot(), "out.docx");
		String err = stderr(() -> assertEquals(3, AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--no-fonts" })));
		assertTrue(err, err.contains("Refused: the zip holds more than one entry of a name"));
		assertTrue(err, err.contains("add --repair word"));
	}

	/** Under --repair word the copy is the package as Word's repair gives it, the repair listed; it then loads with no repair mode. */
	@Test
	public void underRepairWordTheRepairedPackageIsAnonymised() throws Exception {
		File in = copy("package-wiring/dup-entry-document.docx");
		File out = new File(tmp.getRoot(), "out.docx");
		String err = stderr(() -> assertEquals(0, AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--repair", "word", "--no-fonts" })));
		assertTrue(err, err.contains("docx4j anon: load finding package.duplicate_entry word/document.xml (2)"));
		WordprocessingMLPackage again = WordprocessingMLPackage.load(out);
		assertTrue(again.getLoadFindings().toString(), again.getLoadFindings().isEmpty());
	}

	/** An External main relationship beside an internal one takes docx4j's own repair: the hint says all, under word too. */
	@Test
	public void anExternalMainRelationshipTakesRepairAll() throws Exception {
		String od = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
		String rels = "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
				+ "<Relationship Id=\"rId1\" Type=\"" + od + "\" Target=\"word/document.xml\"/>"
				+ "<Relationship Id=\"rId2\" Type=\"" + od + "\" Target=\"http://example.com/x.docx\" TargetMode=\"External\"/>"
				+ "</Relationships>";
		File in = new File(tmp.getRoot(), "external.docx");
		Files.write(in.toPath(), withEntry(AnonymizeProbesTest.resource("package-wiring/control-one-main.docx"), "_rels/.rels", rels));
		File out = new File(tmp.getRoot(), "out.docx");
		for (String[] repair : new String[][] { {}, { "--repair", "word" } }) {
			String[] args = new String[3 + repair.length];
			args[0] = in.getPath();
			args[1] = out.getPath();
			args[2] = "--no-fonts";
			System.arraycopy(repair, 0, args, 3, repair.length);
			String err = stderr(() -> assertEquals(3, AnonymizeCli.run(args)));
			assertTrue(err, err.contains("add --repair all"));
		}
		String err = stderr(() -> assertEquals(0, AnonymizeCli.run(new String[] { in.getPath(), out.getPath(), "--repair", "all", "--no-fonts" })));
		assertTrue(err, err.contains("load finding package.external_main_part"));
		WordprocessingMLPackage.load(out);
	}

	private interface Body {
		void run() throws Exception;
	}

	/** What body writes to System.err. */
	private static String stderr(Body body) throws Exception {
		PrintStream old = System.err;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		System.setErr(new PrintStream(buf, true, "UTF-8"));
		try {
			body.run();
		} finally {
			System.setErr(old);
		}
		return buf.toString("UTF-8");
	}

	/** The zip with one entry's content replaced. */
	private static byte[] withEntry(byte[] zip, String name, String content) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip)); ZipOutputStream out = new ZipOutputStream(bytes)) {
			for (ZipEntry e; (e = in.getNextEntry()) != null;) {
				out.putNextEntry(new ZipEntry(e.getName()));
				out.write(e.getName().equals(name) ? content.getBytes(StandardCharsets.UTF_8) : in.readAllBytes());
				out.closeEntry();
			}
		}
		return bytes.toByteArray();
	}

}
