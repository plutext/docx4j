package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tr;
import org.junit.Test;
import org.jvnet.jaxb.lang.Child;

/**
 * OpenDoPEReverter must not merge two occurrences of the same repeat whose
 * expansions are immediately adjacent (no other object between them to reset
 * its previousRepeatID tracking) — previously the second occurrence's first
 * instance, which carries that occurrence's template sdt id, was deleted, so
 * the control was lost from the reverted template.
 *
 * Occurrences are now distinguished by the od:RptOcc param which
 * OpenDoPEHandler stamps into the od:rptd tag.
 *
 * Uses invoice.docx, duplicating its row-level repeat (od:repeat=x2) as an
 * adjacent sibling row, as a user would get by copy-pasting the repeat row.
 */
public class OpenDoPEReverterAdjacentRepeatTest {

	static class SdtFinder extends CallbackImpl {
		String tagContains;
		java.util.List<Object> found = new java.util.ArrayList<Object>();
		SdtFinder(String tagContains) { this.tagContains = tagContains; }
		@Override
		public List<Object> apply(Object o) {
			SdtPr sdtPr = OpenDoPEHandler.getSdtPr(o);
			if (sdtPr != null && sdtPr.getTag() != null
					&& sdtPr.getTag().getVal().contains(tagContains)) {
				found.add(o);
			}
			return null;
		}
	}

	private WordprocessingMLPackage loadTemplateWithDuplicatedRepeatRow(boolean spacerRowBetween) throws Exception {

		String inputfilepath = System.getProperty("user.dir")
				+ "/src/test/resources/OpenDoPE/invoice.docx";
		WordprocessingMLPackage template = WordprocessingMLPackage.load(new java.io.File(inputfilepath));

		// find the row-level repeat sdt
		SdtFinder finder = new SdtFinder("od:repeat=");
		new TraversalUtil(template.getMainDocumentPart().getContent(), finder);
		Object repeatSdt = finder.found.get(0);

		// locate it (possibly JAXBElement-wrapped) in its parent's content list
		Object parent = ((Child) repeatSdt).getParent();
		@SuppressWarnings("unchecked")
		List<Object> siblings = (parent instanceof org.docx4j.wml.ContentAccessor)
				? ((org.docx4j.wml.ContentAccessor) parent).getContent()
				: (List<Object>) parent;
		int idx = -1;
		for (int i = 0; i < siblings.size(); i++) {
			if (XmlUtils.unwrap(siblings.get(i)) == repeatSdt) { idx = i; break; }
		}

		// duplicate it as an adjacent sibling, with its own sdt id (as Word would assign on paste)
		Object copy = XmlUtils.deepCopy(siblings.get(idx), org.docx4j.jaxb.Context.jc);
		SdtElement copySdt = (SdtElement) XmlUtils.unwrap(copy);
		copySdt.getSdtPr().setId();
		if (copy instanceof Child) { ((Child) copy).setParent(parent); }

		int insertAt = idx + 1;
		if (spacerRowBetween) {
			// a plain row between the two occurrences
			Tr spacer = (Tr) XmlUtils.unwrap(XmlUtils.deepCopy(
					XmlUtils.unwrap(siblings.get(idx - 1)), org.docx4j.jaxb.Context.jc));
			siblings.add(insertAt, spacer);
			insertAt++;
		}
		siblings.add(insertAt, copy);

		return template;
	}

	private void roundTrip(boolean spacerRowBetween) throws Exception {

		WordprocessingMLPackage template = loadTemplateWithDuplicatedRepeatRow(spacerRowBetween);

		WordprocessingMLPackage instance = (WordprocessingMLPackage) template.clone();
		OpenDoPEHandler odh = new OpenDoPEHandler(instance);
		instance = odh.preprocess();

		// 3 items in the data, 2 occurrences of the repeat
		SdtFinder rptdFinder = new SdtFinder("od:rptd=");
		new TraversalUtil(instance.getMainDocumentPart().getContent(), rptdFinder);
		assertEquals(6, rptdFinder.found.size());

		OpenDoPEReverter reverter = new OpenDoPEReverter(template, instance);
		assertTrue("revert() reported failure", reverter.revert());

		SdtFinder repeatFinder = new SdtFinder("od:repeat=");
		new TraversalUtil(instance.getMainDocumentPart().getContent(), repeatFinder);
		assertEquals("repeat controls restored", 2, repeatFinder.found.size());
	}

	@Test
	public void testAdjacentOccurrences() throws Exception {
		roundTrip(false);
	}

	@Test
	public void testOccurrencesSeparatedByPlainRow() throws Exception {
		roundTrip(true);
	}

}
