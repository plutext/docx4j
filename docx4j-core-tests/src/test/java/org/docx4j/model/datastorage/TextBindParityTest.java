package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * CR-binding-traverser-parity phase 1: text binding must behave the same under
 * all three binding traverser implementations:
 *
 * - the sdtPr's w:rPr is applied to the generated runs (previously lost under
 *   NonXSLT/StAX),
 * - an empty result restores the placeholder ("Click here to enter text."),
 * - a bound sdt whose content is a w:tbl gets its value (previously silently
 *   left unbound under NonXSLT/StAX).
 *
 * The template is the issue 690 one plus: bold w:rPr on the first repeat's rank
 * value sdt; an sdt bound to a non-existent node (initial content OLDVALUE);
 * and a tbl-shaped sdt bound to header/employer (cell placeholder
 * TBLPLACEHOLDER, data value "Employer Name").
 */
public class TextBindParityTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";

	static class BoldRunFinder extends CallbackImpl {
		String lookFor;
		boolean foundBold = false;
		BoldRunFinder(String lookFor) { this.lookFor = lookFor; }
		@Override
		public List<Object> apply(Object o) {
			if (o instanceof Text && lookFor.equals(((Text)o).getValue())) {
				Object parent = ((org.jvnet.jaxb.lang.Child)o).getParent();
				if (parent instanceof R) {
					R r = (R)parent;
					if (r.getRPr()!=null && r.getRPr().getB()!=null) {
						foundBold = true;
					}
				}
			}
			return null;
		}
	}

	private WordprocessingMLPackage process(String implementation) throws Exception {

		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			String inputfilepath = System.getProperty("user.dir")
					+ "/src/test/resources/OpenDoPE/text-bind-parity.docx";

			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
					.load(new java.io.File(inputfilepath));

			OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
			wordMLPackage = odh.preprocess();

			BindingHandler bh = new BindingHandler(wordMLPackage);
			bh.setStartingIdForNewBookmarks(odh.getNextBookmarkId());
			bh.applyBindings(wordMLPackage.getMainDocumentPart());

			return wordMLPackage;

		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	private String assertExpectedOutput(WordprocessingMLPackage pkg, String implementation)
			throws Exception {

		StringWriter sw = new StringWriter();
		TextUtils.extractText(pkg.getMainDocumentPart().getContents(), sw);
		String allText = sw.toString();

		// binding + RptPosCon sanity
		assertTrue(implementation, allText.contains("First repeat 1, 2 et 3."));

		// empty result restores the placeholder
		assertTrue(implementation + ": placeholder not restored",
				allText.contains("Click here to enter text."));
		assertFalse(implementation, allText.contains("OLDVALUE"));

		// tbl-shaped bind
		assertTrue(implementation + ": tbl-shaped sdt not bound",
				allText.contains("Employer Name"));
		assertFalse(implementation, allText.contains("TBLPLACEHOLDER"));

		// w15:dataBinding (Word 2013) bound like a w:dataBinding
		assertTrue(implementation + ": w15:dataBinding not bound",
				allText.contains("W15: Doe"));
		assertFalse(implementation, allText.contains("W15OLD"));

		// sdtPr rPr applied: rank instance 2's bound run is bold
		BoldRunFinder finder = new BoldRunFinder("2");
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
		assertTrue(implementation + ": sdtPr w:rPr not applied to bound run",
				finder.foundBold);

		return allText;
	}

	@Test
	public void testAllImplementationsAgree() throws Exception {

		String xslt = assertExpectedOutput(process("BindingTraverserXSLT"), "XSLT");
		String nonXslt = assertExpectedOutput(process("BindingTraverserNonXSLT"), "NonXSLT");
		String stax = assertExpectedOutput(process("BindingTraverserStAX"), "StAX");

		assertEquals("NonXSLT vs XSLT", xslt, nonXslt);
		assertEquals("StAX vs XSLT", xslt, stax);
	}

}
