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
 * A bound sdt inside a repeat whose xpath addresses a specific item of the
 * repeated collection via a position function predicate at the repeat step -
 * eg .../ranks[last()]/rank - must be left un-contextualized by XPathEnhancer,
 * so every instance shows that specific item's value.
 *
 * (A numeric predicate - .../ranks[1]/rank - still means the current instance,
 * per the sample-based authoring convention.)
 *
 * https://github.com/plutext/docx4j/discussions/691
 */
public class LastItemBindingInRepeatTest {

	@Test
	public void testLastItemBinding() throws Exception {

		String inputfilepath = System.getProperty("user.dir")
				+ "/src/test/resources/OpenDoPE/ranks-last-binding.docx";

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

		// the repeated field is bound to .../ranks[last()]/rank[1], so every
		// instance shows the LAST item's value (3); the RptPosCon separators
		// remain position-dependent.  The second repeat (rankDate, bound the
		// normal way) still shows per-instance values.
		assertEquals("First repeat 3, 3 et 3. Second repeat in the same paragraph "
				+ "1er juin 2024, 2&#232;me juin 2024 and 3&#232;me juin 2024.",
				paragraphTexts.get(0));
		assertEquals("First repeat 3, 3 et 3. ", paragraphTexts.get(2));
	}

}
