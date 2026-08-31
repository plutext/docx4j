package org.docx4j.markdown;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures the styles the import mapping references exist in the target
 * package: styles known to docx4j (KnownStyles.xml) are activated on demand;
 * for the code styles (which Word has no built-in equivalent of) minimal
 * definitions are added if the template doesn't already provide them.
 *
 * <p>StyleIds (not display names) are used throughout, since styleIds are
 * stable across localized Word UIs.  An existing style in the caller's
 * package (styles template) is never replaced.</p>
 */
class ImportStyles {

	private static final Logger log = LoggerFactory.getLogger(ImportStyles.class);

	static final String HEADING_PREFIX = "Heading";
	static final String QUOTE = "Quote";
	static final String HYPERLINK = "Hyperlink";
	static final String LIST_PARAGRAPH = "ListParagraph";
	/** Character style for inline code; created if absent. */
	static final String CODE_CHAR = "CodeChar";
	/** Paragraph style for code blocks; created if absent. */
	static final String SOURCE_CODE = "SourceCode";

	private static final String W_NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String CODE_CHAR_XML =
			"<w:style " + W_NS + " w:type=\"character\" w:styleId=\"" + CODE_CHAR + "\">"
			+ "<w:name w:val=\"Code Char\"/>"
			+ "<w:rPr>"
			+ "<w:rFonts w:ascii=\"Consolas\" w:hAnsi=\"Consolas\" w:cs=\"Consolas\"/>"
			+ "<w:sz w:val=\"20\"/><w:szCs w:val=\"20\"/>"
			+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F2F2F2\"/>"
			+ "</w:rPr>"
			+ "</w:style>";

	private static final String SOURCE_CODE_XML =
			"<w:style " + W_NS + " w:type=\"paragraph\" w:styleId=\"" + SOURCE_CODE + "\">"
			+ "<w:name w:val=\"Source Code\"/>"
			+ "<w:basedOn w:val=\"Normal\"/>"
			+ "<w:pPr>"
			+ "<w:keepLines/>"
			+ "<w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/>"
			+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F2F2F2\"/>"
			+ "</w:pPr>"
			+ "<w:rPr>"
			+ "<w:rFonts w:ascii=\"Consolas\" w:hAnsi=\"Consolas\" w:cs=\"Consolas\"/>"
			+ "<w:noProof/>"
			+ "<w:sz w:val=\"20\"/><w:szCs w:val=\"20\"/>"
			+ "</w:rPr>"
			+ "</w:style>";

	private final PropertyResolver resolver;

	ImportStyles(WordprocessingMLPackage pkg) throws Docx4JException {
		this.resolver = pkg.getMainDocumentPart().getPropertyResolver();
	}

	/**
	 * Activate a style from docx4j's KnownStyles.xml (eg Heading1, Quote,
	 * Hyperlink, ListParagraph).  No-op if already live in the package.
	 */
	void ensureKnown(String styleId) {
		if (!resolver.activateStyle(styleId)) {
			log.warn("Could not activate style {}", styleId);
		}
	}

	void ensureCodeChar() throws Docx4JException {
		ensureCustom(CODE_CHAR, CODE_CHAR_XML);
	}

	void ensureSourceCode() throws Docx4JException {
		ensureCustom(SOURCE_CODE, SOURCE_CODE_XML);
	}

	private void ensureCustom(String styleId, String xml) throws Docx4JException {
		if (resolver.getStyle(styleId) != null) {
			return; // the template's definition wins
		}
		try {
			Style style = (Style) XmlUtils.unmarshalString(xml, Context.jc, Style.class);
			resolver.activateStyle(style);
		} catch (jakarta.xml.bind.JAXBException e) {
			throw new Docx4JException("Could not create style " + styleId, e);
		}
	}

}
