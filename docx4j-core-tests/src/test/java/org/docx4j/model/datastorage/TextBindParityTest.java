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

	static class CheckboxCheckedFinder extends CallbackImpl {
		String checkedVal = null;
		@Override
		public List<Object> apply(Object o) {
			if (o instanceof org.docx4j.wml.SdtRun || o instanceof org.docx4j.wml.SdtBlock) {
				org.docx4j.wml.SdtPr sdtPr = ((org.docx4j.wml.SdtElement)o).getSdtPr();
				if (sdtPr!=null) {
					org.docx4j.w14.CTSdtCheckbox cb = (org.docx4j.w14.CTSdtCheckbox)
							sdtPr.getByClass(org.docx4j.w14.CTSdtCheckbox.class);
					if (cb!=null && cb.getChecked()!=null) {
						checkedVal = cb.getChecked().getVal();
					}
				}
			}
			return null;
		}
	}

	static class DrawingFinder extends CallbackImpl {
		// descr (or "(none)") -> blip r:embed
		java.util.Map<String,String> drawings = new java.util.LinkedHashMap<String,String>();
		@Override
		public List<Object> apply(Object o) {
			if (o instanceof org.docx4j.wml.Drawing) {
				for (Object di : ((org.docx4j.wml.Drawing)o).getAnchorOrInline()) {
					di = org.docx4j.XmlUtils.unwrap(di);
					if (!(di instanceof org.docx4j.dml.wordprocessingDrawing.Inline)) continue;
					org.docx4j.dml.wordprocessingDrawing.Inline inline
							= (org.docx4j.dml.wordprocessingDrawing.Inline)di;
					String descr = inline.getDocPr()==null ? null : inline.getDocPr().getDescr();
					String embed = null;
					if (inline.getGraphic()!=null && inline.getGraphic().getGraphicData()!=null) {
						for (Object any : inline.getGraphic().getGraphicData().getAny()) {
							any = org.docx4j.XmlUtils.unwrap(any);
							if (any instanceof org.docx4j.dml.picture.Pic) {
								org.docx4j.dml.picture.Pic pic = (org.docx4j.dml.picture.Pic)any;
								if (pic.getBlipFill()!=null && pic.getBlipFill().getBlip()!=null) {
									embed = pic.getBlipFill().getBlip().getEmbed();
								}
							}
						}
					}
					drawings.put(descr==null || descr.length()==0 ? "(none)" : descr, embed);
				}
			}
			return null;
		}
	}

	static class AltChunkFinder extends CallbackImpl {
		List<String> ids = new java.util.ArrayList<String>();
		@Override
		public List<Object> apply(Object o) {
			if (o instanceof org.docx4j.wml.CTAltChunk) {
				ids.add(((org.docx4j.wml.CTAltChunk)o).getId());
			}
			return null;
		}
	}

	private WordprocessingMLPackage process(String implementation) throws Exception {

		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		BindingHandler.setHyperlinkStyle("Hyperlink");
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

			// smoke-test that the result still marshals/saves
			wordMLPackage.save(new java.io.ByteArrayOutputStream());

			return wordMLPackage;

		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
			BindingHandler.setHyperlinkStyle(null);
		}
	}

	static class SdtPrInspector extends CallbackImpl {
		String tagContains;
		org.docx4j.wml.SdtPr found = null;
		SdtPrInspector(String tagContains) { this.tagContains = tagContains; }
		@Override
		public List<Object> apply(Object o) {
			if (o instanceof org.docx4j.wml.SdtElement) {
				org.docx4j.wml.SdtPr sdtPr = ((org.docx4j.wml.SdtElement)o).getSdtPr();
				if (sdtPr!=null && sdtPr.getTag()!=null
						&& sdtPr.getTag().getVal().contains(tagContains)) {
					found = sdtPr;
				}
			}
			return null;
		}
		boolean has(Class<?> clazz) {
			for (Object o : found.getRPrOrAliasOrLock()) {
				if (clazz.isInstance(org.docx4j.XmlUtils.unwrap(o))) return true;
			}
			return false;
		}
		boolean hasElement(String localPart) {
			for (Object o : found.getRPrOrAliasOrLock()) {
				if (o instanceof jakarta.xml.bind.JAXBElement
						&& ((jakarta.xml.bind.JAXBElement<?>)o).getName().getLocalPart().equals(localPart)) {
					return true;
				}
			}
			return false;
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

		// date cc formatted per w:date settings (phase 3)
		assertTrue(implementation + ": date cc not bound",
				allText.contains("Date: 19/08/2026"));
		assertFalse(implementation, allText.contains("DATEOLD"));

		// checkbox cc glyph + w14:checked updated (phase 3)
		assertTrue(implementation + ": checkbox cc not bound",
				allText.contains("Check: ☒"));
		assertFalse(implementation, allText.contains("CHKOLD"));
		CheckboxCheckedFinder checkedFinder = new CheckboxCheckedFinder();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), checkedFinder);
		assertEquals(implementation + ": w14:checked not updated", "1", checkedFinder.checkedVal);

		// picture ccs (phase 4)
		DrawingFinder drawingFinder = new DrawingFinder();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), drawingFinder);
		// KEEPME (w:picture + dataBinding) and KEEPME2 (od:Handler=picture):
		// the authored drawings survive, with just the image rel replaced
		for (String descr : new String[]{"KEEPME", "KEEPME2"}) {
			String embed = drawingFinder.drawings.get(descr);
			assertTrue(implementation + ": authored drawing " + descr + " lost",
					drawingFinder.drawings.containsKey(descr));
			assertTrue(implementation + ": " + descr + " blip not rebound: " + embed,
					embed!=null && !embed.startsWith("rId99"));
			assertTrue(implementation + ": " + descr + " embed rel doesn't resolve",
					pkg.getMainDocumentPart().getRelationshipsPart()
							.getRelationshipByID(embed)!=null);
		}
		// KEEPME3 (od:Handler=picture + width=auto): content replaced with a
		// freshly sized image
		assertFalse(implementation + ": width= variant should replace the drawing",
				drawingFinder.drawings.containsKey("KEEPME3"));
		assertTrue(implementation + ": width= variant image missing",
				drawingFinder.drawings.containsKey("(none)"));

		// FlatOPC + XHTML injected as altChunks (phase 5; XHTML falls back to
		// altChunk here since docx4j-ImportXHTML is not on the test classpath)
		assertFalse(implementation, allText.contains("FLATOLD"));
		assertFalse(implementation, allText.contains("XHTMLOLD"));
		AltChunkFinder altChunkFinder = new AltChunkFinder();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), altChunkFinder);
		assertEquals(implementation + ": expected 2 altChunks", 2, altChunkFinder.ids.size());
		String allChunks = "";
		for (String id : altChunkFinder.ids) {
			org.docx4j.openpackaging.parts.Part chunk = pkg.getMainDocumentPart()
					.getRelationshipsPart().getPart(
							pkg.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id));
			assertTrue(implementation + ": altChunk rel " + id + " doesn't resolve",
					chunk instanceof org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart);
			allChunks += new String(
					((org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart)chunk).getBytes());
		}
		assertTrue(implementation + ": FlatOPC content missing", allChunks.contains("FLATOPCTEXT"));
		assertTrue(implementation + ": XHTML content missing", allChunks.contains("XHTMLCONTENT"));

		// sdtPr hygiene (phase 6)
		// hyperlink in bound content: w:dataBinding and w:text stripped
		assertTrue(implementation + ": hyperlink not created",
				allText.contains("docx4java.org"));
		assertFalse(implementation, allText.contains("LINKOLD"));
		SdtPrInspector linkSdt = new SdtPrInspector("XLink");
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), linkSdt);
		assertTrue(implementation, linkSdt.found!=null);
		assertFalse(implementation + ": w:dataBinding not stripped (Word 2007 fix)",
				linkSdt.has(org.docx4j.wml.CTDataBinding.class));
		assertFalse(implementation + ": w:text not stripped (Word 2007 fix)",
				linkSdt.has(org.docx4j.wml.CTSdtText.class));

		// restored placeholder: w:showingPlcHdr added
		SdtPrInspector emptySdt = new SdtPrInspector("XEmpty");
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), emptySdt);
		assertTrue(implementation, emptySdt.found!=null);
		assertTrue(implementation + ": w:showingPlcHdr not added",
				emptySdt.hasElement("showingPlcHdr"));

		// w:placeholder stripped from bound sdts
		SdtPrInspector rankSdt = new SdtPrInspector("od:xpath=Ff9025b08");
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), rankSdt);
		assertTrue(implementation, rankSdt.found!=null);
		assertFalse(implementation + ": w:placeholder not stripped",
				rankSdt.has(org.docx4j.wml.CTPlaceholder.class));

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
