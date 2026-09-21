package org.docx4j.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.junit.Test;
import org.pageseeder.diffx.config.DiffConfig;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Captures the current diff output as golden files, so that the planned
 * migration from the bundled com.topologi.diffx fork to
 * org.pageseeder.diffx:pso-diffx can be verified against it
 * (see docx4j-diffx/PAGESEEDER-MIGRATION.md, Phase 0/3).
 *
 * Two kinds of output are captured for each pair of test paragraphs:
 *
 *   raw_p/  - the raw pre-XSLT Diff-X XML, as produced inside
 *             Differencer.getDiffxOutput() (Main.diff with the same
 *             DiffXConfig)
 *   wml_p/  - the final tracked-changes WML from Differencer.diff
 *             (preProcess=false, the path used by the public 7-arg diff)
 *
 * plus, for a body pair (exercising Docx4jDriver's divide-and-conquer):
 *
 *   body.raw.xml - raw Docx4jDriver.diff output
 *   body.wml.xml - final WML from Differencer.diff(Body,Body,...)
 *
 * To (re)generate the golden files, run with -Dgolden.regenerate=true :
 *
 *   mvn test -pl docx4j-diffx-tests -Dtest=GoldenOutputTest -Dgolden.regenerate=true
 *
 * Notes on determinism: w:id values come from the static counter
 * Differencer.nextId, which is reset before each diff below.  The w:date
 * attribute is fixed by passing a fixed Calendar.  Namespace declarations
 * are stripped before the comparison (and from the files as written): the
 * marshaller declares every namespace the JAXB context knows on the root,
 * so the list grows with each schema docx4j binds and the auto-assigned
 * nsNN prefixes shift with it (2026-09-21: six new prefixes from CR-018 and
 * CR-023 and the c173 to c16r3 rename failed both paragraph tests).  The
 * diff's meaning is in the elements and attributes.  The preProcess=true
 * variant of Differencer.diff is NOT captured: it word-splits
 * P.toString() which (P not overriding toString) contains an object
 * hashcode, so its run restructuring is not reproducible across JVM runs.
 */
public class GoldenOutputTest {

	final static String BASE_DIR = "src/test/resources/org/docx4j/diff/";
	final static String GOLDEN_DIR = BASE_DIR + "golden/";

	final static String[] testparagraphs = { "t2R", "t2RR", "t3L", "t3R", "t4"};

	final static boolean REGENERATE = Boolean.getBoolean("golden.regenerate");

	final static String AUTHOR = "golden-author";

	static Calendar fixedDate() {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.clear();
		cal.set(2009, Calendar.MARCH, 11, 17, 57, 0);
		return cal;
	}

	@Test
	public void rawParagraphDiff() throws Exception {

		for (int i=0; i<testparagraphs.length-1; i++){
			for (int j=0; j<testparagraphs.length; j++){

				P pl = Differencer.loadParagraph(BASE_DIR + testparagraphs[i]);
				P pr = Differencer.loadParagraph(BASE_DIR + testparagraphs[j]);

				// Replicates Differencer.getDiffxOutput (private)
				String leftXml = XmlUtils.marshaltoString(pl, true, false);
				String rightXml = XmlUtils.marshaltoString(pr, true, false);

				DiffConfig diffxConfig = Docx4jDriver.legacyConfig();

				Writer out = new StringWriter();
				Docx4jDriver.diff(toNode(leftXml), toNode(rightXml), out, diffxConfig);

				check("raw_p/" + testparagraphs[i] + "_" + testparagraphs[j] + ".xml",
						out.toString());
			}
		}
	}

	@Test
	public void wmlParagraphDiff() throws Exception {

		for (int i=0; i<testparagraphs.length-1; i++){
			for (int j=0; j<testparagraphs.length; j++){

				P pl = Differencer.loadParagraph(BASE_DIR + testparagraphs[i]);
				P pr = Differencer.loadParagraph(BASE_DIR + testparagraphs[j]);

				Differencer.nextId = 0;

				StringWriter sw = new StringWriter();
				Differencer pd = new Differencer();
				pd.diff(pl, pr, new StreamResult(sw), AUTHOR, fixedDate(),
						null, null);

				check("wml_p/" + testparagraphs[i] + "_" + testparagraphs[j] + ".xml",
						sw.toString());
			}
		}
	}

	@Test
	public void bodyDiff() throws Exception {

		// older body: all five test paragraphs, in order
		Body older = newBody();
		for (String name : testparagraphs) {
			older.getContent().add(Differencer.loadParagraph(BASE_DIR + name));
		}

		// newer body: t2RR deleted, t3L and t3R swapped
		Body newer = newBody();
		for (String name : new String[]{ "t2R", "t3R", "t3L", "t4"}) {
			newer.getContent().add(Differencer.loadParagraph(BASE_DIR + name));
		}

		// raw divide-and-conquer output, as invoked by Differencer.diffWorker
		Node newerNode = XmlUtils.marshaltoW3CDomDocument(newer).getDocumentElement();
		Node olderNode = XmlUtils.marshaltoW3CDomDocument(older).getDocumentElement();

		Writer raw = new StringWriter();
		Docx4jDriver.diff(newerNode, olderNode, raw);
		check("body.raw.xml", raw.toString());

		// final WML
		Differencer.nextId = 0;

		StringWriter sw = new StringWriter();
		Differencer pd = new Differencer();
		pd.diff(newer, older, new StreamResult(sw), AUTHOR, fixedDate(),
				null, null);
		check("body.wml.xml", sw.toString());
	}

	private static Body newBody() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Body body = pkg.getMainDocumentPart().getJaxbElement().getBody();
		body.getContent().clear();
		return body;
	}

	/**
	 * As Differencer.toNode(Reader, true) (private).
	 */
	private static Node toNode(String xml) throws Exception {
		return XmlUtils.getNewDocumentBuilder().parse(
				new InputSource(new StringReader(xml)));
	}

	/** xmlns:prefix="uri" and xmlns="uri" declarations, which are not the diff's meaning */
	private static final java.util.regex.Pattern XMLNS = java.util.regex.Pattern.compile(" xmlns(:[A-Za-z0-9_.-]+)?=\"[^\"]*\"");

	static String normalize(String xml) {
		return XMLNS.matcher(xml.replace("\r\n", "\n")).replaceAll("");
	}

	private static void check(String relPath, String actual) throws Exception {

		String normalized = normalize(actual);
		File golden = new File(GOLDEN_DIR + relPath);

		if (REGENERATE) {
			golden.getParentFile().mkdirs();
			Files.write(golden.toPath(), normalized.getBytes(StandardCharsets.UTF_8));
			System.out.println("regenerated " + golden.getPath());
			return;
		}

		assertTrue("Golden file missing: " + golden.getPath()
				+ " - generate it with -Dgolden.regenerate=true",
				golden.exists());

		// normalised too, so a golden written before 2026-09-21 (with the declarations) still compares
		String expected = normalize(new String(Files.readAllBytes(golden.toPath()),
				StandardCharsets.UTF_8));
		assertEquals("Diff output changed for " + relPath, expected, normalized);
	}

}
