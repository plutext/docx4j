package org.docx4j.convert.out.html;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * The non-XSLT (visitor) HTML exporter emits equations as native MathML — no
 * Microsoft OMML2MML.XSL required. See
 * docs/developer/change-requests/CR-math-omml-mathml.md.
 */
public class HtmlMathMLTest {

	@Test
	public void displayEquationEmitsMathML() throws Exception {

		// 2010-sample1.docx holds a display equation (the quadratic formula)
		String path = System.getProperty("user.dir")
				+ "/src/test/resources/2010/2010-sample1.docx";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new File(path));

		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
		String html = os.toString("UTF-8");

		assertTrue("output should contain a MathML <math> element:\n" + html,
				html.contains("<math"));
		assertTrue("MathML namespace should be declared",
				html.contains("http://www.w3.org/1998/Math/MathML"));
		assertTrue("the equation's fraction should be an <mfrac>",
				html.contains("<mfrac>"));
		assertTrue("the square root should be an <msqrt>",
				html.contains("<msqrt>"));
	}
}
