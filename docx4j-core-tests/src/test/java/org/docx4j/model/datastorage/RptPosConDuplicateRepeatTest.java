package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * The same repeat content control used twice (copy-pasted, so identical tags),
 * with od:RptPosCon controls inserting ", " between entries and " et "/" and "
 * before the last one.
 *
 * When both occurrences sit in a single paragraph, each must still evaluate
 * positions against its own instances only, not the union of both occurrences'
 * instances.
 *
 * https://github.com/plutext/docx4j/issues/690
 */
public class RptPosConDuplicateRepeatTest {

	@Test
	public void testDuplicateRepeatsInOneParagraph() throws Exception {

		String inputfilepath = System.getProperty("user.dir")
				+ "/src/test/resources/OpenDoPE/RptPosCon-issue690.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));

		// Process conditionals and repeats
		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
		wordMLPackage = odh.preprocess();

		// Apply the bindings
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

		// The data part contains ranks 1, 2, 3 (rank plus rankDate)

		// Both repeats in the one paragraph
		assertEquals("First repeat 1, 2 et 3. Second repeat in the same paragraph "
				+ "1er juin 2024, 2&#232;me juin 2024 and 3&#232;me juin 2024.",
				paragraphTexts.get(0));

		// The already-working case: same two repeats, in separate paragraphs
		assertEquals("First repeat 1, 2 et 3. ", paragraphTexts.get(2));
		assertEquals("Second repeat in an other paragraph "
				+ "1er juin 2024, 2&#232;me juin 2024 and 3&#232;me juin 2024.",
				paragraphTexts.get(3));
	}

}
