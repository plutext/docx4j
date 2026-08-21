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
 * "Position AND data" conditional content inside a repeat composes by nesting:
 * an od:condition sdt wrapping an od:RptPosCon sdt (or vice versa) renders its
 * content only where both hold, because the two mechanisms make independent
 * keep/omit decisions at different pipeline stages (conditions during
 * OpenDoPEHandler preprocessing, RptPosCon during binding).
 *
 * This pins that composition down, for each binding traverser implementation.
 *
 * The template is the issue 690 one, with three markers added inside the rank
 * repeat (ranks 1, 2, 3), after the " et " separator:
 * - *BIG*: condition (rank &gt; 2) wrapping RptPosCon (position()=last())
 *          - appears once, in the last instance
 * - !NO!:  condition (rank &gt; 5) wrapping RptPosCon (position()=last())
 *          - never appears (data test fails)
 * - #TOP#: RptPosCon (position()=last()) wrapping condition (rank &gt; 2),
 *          in the second (separate-paragraph) copy of the repeat
 *          - appears once, in the last instance
 */
public class NestedConditionRptPosConTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";

	private List<String> process(String implementation) throws Exception {

		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			String inputfilepath = System.getProperty("user.dir")
					+ "/src/test/resources/OpenDoPE/nested-condition-rptposcon.docx";

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

		// *BIG* once (after the last instance); !NO! nowhere
		assertEquals("First repeat 1, 2 et 3*BIG*. Second repeat in the same paragraph "
				+ "1er juin 2024, 2&#232;me juin 2024 and 3&#232;me juin 2024.",
				paragraphTexts.get(0));

		// #TOP# once (after the last instance)
		assertEquals("First repeat 1, 2 et 3#TOP#. ", paragraphTexts.get(2));
	}

	@Test
	public void testXSLT() throws Exception {
		assertExpectedOutput(process("BindingTraverserXSLT"));
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
