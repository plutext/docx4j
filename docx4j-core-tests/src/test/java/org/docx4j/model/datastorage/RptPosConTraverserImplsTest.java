package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * od:RptPosCon in BindingTraverserNonXSLT and BindingTraverserStAX must produce
 * the same output as bind.xslt (see RptPosConDuplicateRepeatTest, which covers
 * the default BindingTraverserXSLT).
 *
 * Uses the issue 690 template: the same repeat control twice in one paragraph
 * (identical tags), and the same pair again in separate paragraphs, with
 * RptPosCon separators ", " and " et "/" and ".
 */
public class RptPosConTraverserImplsTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";

	private List<String> process(String implementation) throws Exception {

		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			String inputfilepath = System.getProperty("user.dir")
					+ "/src/test/resources/OpenDoPE/RptPosCon-issue690.docx";

			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
					.load(new java.io.File(inputfilepath));

			OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
			wordMLPackage = odh.preprocess();

			BindingHandler bh = new BindingHandler(wordMLPackage);
			bh.setStartingIdForNewBookmarks(odh.getNextBookmarkId());
			bh.applyBindings(wordMLPackage.getMainDocumentPart());

			List<String> paragraphTexts = new ArrayList<String>();
			for (Object o : wordMLPackage.getMainDocumentPart().getContent()) {
				if (XmlUtils.unwrap(o) instanceof P) {
					StringWriter sw = new StringWriter();
					TextUtils.extractText(o, sw);
					paragraphTexts.add(sw.toString());
				}
			}
			return paragraphTexts;

		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	private void assertExpectedOutput(List<String> paragraphTexts) {

		// Both repeats in the one paragraph
		assertEquals("First repeat 1, 2 et 3. Second repeat in the same paragraph "
				+ "1er juin 2024, 2&#232;me juin 2024 and 3&#232;me juin 2024.",
				paragraphTexts.get(0));

		// Same two repeats, in separate paragraphs
		assertEquals("First repeat 1, 2 et 3. ", paragraphTexts.get(2));
		assertEquals("Second repeat in an other paragraph "
				+ "1er juin 2024, 2&#232;me juin 2024 and 3&#232;me juin 2024.",
				paragraphTexts.get(3));
	}

	@Test
	public void testNonXSLT() throws Exception {
		assertExpectedOutput(process("BindingTraverserNonXSLT"));
	}

	@Test
	public void testStAX() throws Exception {
		assertExpectedOutput(process("BindingTraverserStAX"));
	}

}
