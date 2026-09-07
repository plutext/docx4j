package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.Document;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Test;

/**
 * <code>w:compat/w:useWord2002TableStyleRules</code>, "Emulate Word 2002 Table Style Rules"
 * (ECMA-376-1 §17.15.1).
 *
 * <p>Word 2002 did not put a table style's <code>w:pPr</code> and <code>w:rPr</code> above
 * <code>docDefaults</code> for the paragraphs of the table, which is exactly what
 * {@link ParagraphStylesInTableFix} builds a synthetic style for.  So a document asking for
 * the Word 2002 rules gets none of it.</p>
 *
 * <p><b>docx4j deliberately does not read it.</b>  Keying this step on the flag was
 * measured over the three corpora and rejected: three mode-11 documents carry a Word-2003
 * compat block - one without the flag and two with it - and Word 365's own PDFs of all
 * three apply the table style's properties, so skipping this step for them cost
 * 0.951 -&gt; 0.105 of Word's lines (with eleven of its fifty pages), 0.948 -&gt; 0.248 (with
 * two of thirteen) and 0.930 -&gt; 0.842.  See word-layout-settings.md §4(e).  This test records that the step is
 * unconditional; the flag still resolves through
 * <code>org.docx4j.model.CompatibilityOptions</code>, so re-keying it is one line the day a
 * Word golden says which way.</p>
 *
 * @since 17.0.6
 */
public class UseWord2002TableStyleRulesTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** One table using the TableGrid style, one paragraph in its only cell. */
	private static final String DOC = "<w:document " + W + "><w:body>"
			+ "<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"5000\"/></w:tblGrid>"
			+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"5000\" w:type=\"dxa\"/></w:tcPr>"
			+ "<w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
			+ "</w:body></w:document>";

	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:sz w:val=\"20\"/></w:rPr></w:rPrDefault>"
			+ "<w:pPrDefault><w:pPr><w:spacing w:after=\"200\"/></w:pPr></w:pPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\">"
			+ "<w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"TableGrid\"><w:name w:val=\"Table Grid\"/>"
			+ "<w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>"
			+ "<w:rPr><w:sz w:val=\"16\"/></w:rPr></w:style>"
			+ "</w:styles>";

	/** @param flag null = the document states nothing */
	private static WordprocessingMLPackage pkg(int mode, Boolean flag) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(DOC));
		pkg.getMainDocumentPart().getStyleDefinitionsPart()
				.setContents((Styles) XmlUtils.unmarshalString(STYLES));
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart(true);
		if (flag != null) {
			CTCompat compat = (CTCompat) XmlUtils.unwrap(XmlUtils.unmarshalString(
					"<w:compat " + W + "><w:useWord2002TableStyleRules w:val=\""
							+ (flag.booleanValue() ? "1" : "0") + "\"/></w:compat>",
					org.docx4j.jaxb.Context.jc, CTCompat.class));
			dsp.getContents().setCompat(compat);
		}
		dsp.setWordCompatSetting("compatibilityMode", Integer.toString(mode));
		return pkg;
	}

	/** Whether the preprocess step built a synthetic style for the table style. */
	private static boolean hasSyntheticStyle(WordprocessingMLPackage pkg) throws Exception {
		ParagraphStylesInTableFix.process(pkg);
		for (Style s : pkg.getMainDocumentPart().getStyleDefinitionsPart()
				.getContents().getStyle()) {
			if (s.getStyleId() != null && s.getStyleId().contains("TableGrid")
					&& !"TableGrid".equals(s.getStyleId())) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void theStepRunsForAModernDocument() throws Exception {
		assertTrue("a mode-15 document states nothing and gets the modern rules",
				hasSyntheticStyle(pkg(15, null)));
		assertTrue("and so does a mode-12 one, where the flag's default is off",
				hasSyntheticStyle(pkg(12, null)));
	}

	/** Stating the flag changes nothing: Word 365 was measured not honouring it. */
	@Test
	public void theFlagDoesNotSwitchTheStepOff() throws Exception {
		assertTrue(hasSyntheticStyle(pkg(15, Boolean.TRUE)));
		assertTrue(hasSyntheticStyle(pkg(11, Boolean.TRUE)));
	}

	/** Nor does the mode: a mode-11 document gets the modern rules either way. */
	@Test
	public void aModeElevenDocumentGetsTheModernRules() throws Exception {
		assertTrue(hasSyntheticStyle(pkg(11, null)));
		assertTrue(hasSyntheticStyle(pkg(11, Boolean.FALSE)));
	}

	/** But the flag still resolves, so re-keying the step to it is one line. */
	@Test
	public void theFlagStillResolves() throws Exception {
		assertTrue(org.docx4j.model.CompatibilityOptions.of(pkg(11, Boolean.TRUE))
				.is(org.docx4j.model.CompatibilityOptions.Flag.USE_WORD2002_TABLE_STYLE_RULES));
		assertFalse(org.docx4j.model.CompatibilityOptions.of(pkg(11, null))
				.is(org.docx4j.model.CompatibilityOptions.Flag.USE_WORD2002_TABLE_STYLE_RULES));
	}
}
