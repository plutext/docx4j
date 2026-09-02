package org.docx4j.convert.out.mathml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.math.CTOMath;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * Drives {@link OmmlToMathML} over the committed OMML corpus (Word's output for
 * the W3C MathML Test Suite inputs; see
 * docs/developer/change-requests/math-corpus-tools/). Each case is converted and
 * checked for well-formedness and the expected MathML construct.
 */
public class OmmlToMathMLTest {

	private static final String DIR = "/org/docx4j/convert/out/mathml/omml/";

	/** All corpus cases: every one must convert to well-formed MathML. */
	private static final String[] CASES = {
		"mfenced3", "mfencedAdelims6", "mfrac1", "mfrac2", "mfracAbevelled16",
		"mi1", "mimathvariant13", "mmultiscripts1", "mn1", "mo1", "mover1",
		"mover3", "mphantomB1", "mrootB1", "ms1", "msqrt5", "msub1", "msubsup1",
		"msup1", "mtable1", "mtext1", "munder1", "munder2", "rec-enclose3",
		"rec-mtable1"
	};

	private String convert(String caseName) throws Exception {
		try (InputStream in = getClass().getResourceAsStream(DIR + caseName + ".omml.xml")) {
			assertNotNull("missing fixture " + caseName, in);
			String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			CTOMath oMath = (CTOMath) XmlUtils.unmarshalString(xml, Context.jc, CTOMath.class);
			return new OmmlToMathML().toMathMLString(oMath);
		}
	}

	@Test
	public void everyCaseConvertsToWellFormedMathML() throws Exception {
		for (String c : CASES) {
			String mathml = convert(c);
			assertNotNull(c, mathml);
			assertTrue(c + " should be a <math> root: " + mathml,
					mathml.contains("<math xmlns=\"http://www.w3.org/1998/Math/MathML\""));
			// well-formed: parses back
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.newDocumentBuilder().parse(
					new InputSource(new ByteArrayInputStream(mathml.getBytes(StandardCharsets.UTF_8))));
		}
	}

	@Test
	public void simpleFractionIsExact() throws Exception {
		assertEquals(
			"<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mn>1</mn><mn>2</mn></mfrac></math>",
			convert("mfrac1"));
	}

	@Test
	public void superscriptIsExact() throws Exception {
		assertEquals(
			"<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mi>x</mi><mn>2</mn></msup></math>",
			convert("msup1"));
	}

	@Test
	public void constructsMapToExpectedElements() throws Exception {
		assertContains("mfracAbevelled16", "bevelled=\"true\"");
		assertContains("msqrt5", "<msqrt>");
		assertContains("rec-enclose3", "<msqrt>");           // menclose radical -> sqrt
		assertContains("mrootB1", "<mroot>");
		assertContains("msub1", "<msub>");
		assertContains("msubsup1", "<msubsup>");             // n-ary integral
		assertContains("mover3", "<munderover>");            // n-ary with under/over limits
		assertContains("mtable1", "<mtable>");
		assertContains("rec-mtable1", "<mtable>");
		assertContains("mmultiscripts1", "<mmultiscripts>");
		assertContains("mmultiscripts1", "<mprescripts");
		assertContains("mover1", "accent=\"true\"");         // accent (hat)
		assertContains("mphantomB1", "<mphantom>");
		assertContains("mtext1", "<mtext>Theorem");   // normal (non-italic) text -> mtext
		assertContains("mimathvariant13", "mathvariant=\"double-struck\"");
		assertContains("mfenced3", "<mo>(</mo>");
	}

	private void assertContains(String caseName, String needle) throws Exception {
		String mathml = convert(caseName);
		assertTrue(caseName + " should contain '" + needle + "' but was:\n" + mathml,
				mathml.contains(needle));
	}
}
