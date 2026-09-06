package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Word does not re-evaluate a DOCPROPERTY when it opens or prints a document: it paints
 * the result the field cached, and only an explicit update changes it.
 *
 * <p>Measured against Word 365 on a document whose {@code docProps/custom.xml} says
 * {@code invoice_nr=1018} and {@code mwst-nr=CHE-258.324.254} while {@code document.xml}
 * caches {@code 10518} and {@code CHE-XXX.xxx.xxx.xxx}: Word prints the cached text on
 * all three of the lines involved, and printing the evaluated one was that document's
 * whole parity loss.  8 documents of three corpora hold a DOCPROPERTY whose cached
 * result and current value differ.</p>
 *
 * @since 17.0.6
 */
public class DocPropertyCachedResultTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String PROPERTY = "docx4j.convert.out.fields.docPropertyCachedResult";

	private static final String FIELD =
			"<w:p><w:fldSimple w:instr=\" DOCPROPERTY  Company  \\* MERGEFORMAT \">"
			+ "<w:r><w:t>the cached company</w:t></w:r></w:fldSimple></w:p>";

	private static String fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getDocPropsExtendedPart().getJaxbElement().setCompany("the current company");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + FIELD + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	@Test
	public void theCachedResultIsWhatIsPainted() throws Exception {
		for (int flag : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			String fo = fo(flag);
			assertTrue("the cached result is not in the FO", fo.contains("the cached company"));
			assertFalse("the property was re-evaluated where Word paints the cached result",
					fo.contains("the current company"));
		}
	}

	/** The property puts the old behaviour back. */
	@Test
	public void thePropertyRestoresEvaluation() throws Exception {
		String was = Docx4jProperties.getProperty(PROPERTY);
		try {
			Docx4jProperties.setProperty(PROPERTY, "false");
			assertTrue("the property did not restore evaluation",
					fo(Docx4J.FLAG_NONE).contains("the current company"));
		} finally {
			Docx4jProperties.setProperty(PROPERTY, was == null ? "true" : was);
		}
	}
}
