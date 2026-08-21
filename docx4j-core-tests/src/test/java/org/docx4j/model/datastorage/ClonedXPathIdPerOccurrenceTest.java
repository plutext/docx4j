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
 * Cloned xpath ids must be unique per repeat occurrence.
 *
 * Where the same repeat control is used twice (identical tags, so identical
 * descendant binding tag ids), the two expansions previously generated the
 * same cloned xpath ids (eg Ff..._0), and the later occurrence's entries
 * overwrote the earlier one's in the xpaths map ("New xpath entry overwrites
 * existing different xpath").  The binding traversers resolve bindings from
 * that map by tag id (not from w:dataBinding), so where the copies' bindings
 * had been edited to differ, the earlier occurrence was bound using the later
 * occurrence's xpaths.
 *
 * The template here is the issue 690 one, with only the FIRST occurrence's
 * binding changed to /templateSource/body/root/body/rankList/ranks[last()]/rank
 * (every instance should show the last item), while the copy in the later
 * paragraph keeps the stock per-instance binding.
 */
public class ClonedXPathIdPerOccurrenceTest {

	@Test
	public void testDivergedCopiesBindIndependently() throws Exception {

		String inputfilepath = System.getProperty("user.dir")
				+ "/src/test/resources/OpenDoPE/duplicate-repeat-diverged-binding.docx";

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

		// first occurrence: bound to the last item, so 3, 3 et 3
		assertEquals("First repeat 3, 3 et 3. Second repeat in the same paragraph "
				+ "1er juin 2024, 2&#232;me juin 2024 and 3&#232;me juin 2024.",
				paragraphTexts.get(0));

		// the (unedited) copy in the later paragraph: per-instance values
		assertEquals("First repeat 1, 2 et 3. ", paragraphTexts.get(2));
	}

}
