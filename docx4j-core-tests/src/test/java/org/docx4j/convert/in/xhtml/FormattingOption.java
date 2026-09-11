package org.docx4j.convert.in.xhtml;

/**
 * Test-only stand-in for docx4j-ImportXHTML's enum of the same name, so that
 * {@link org.docx4j.model.datastorage.XHTMLImporterFormatting} (which resolves it by
 * reflection) can be exercised without docx4j-ImportXHTML on the test classpath.
 * Deliberately NOT accompanied by a stand-in XHTMLImporterImpl: the binding tests rely
 * on that class being absent to exercise the altChunk fallback.
 */
public enum FormattingOption {
	CLASS_TO_STYLE_ONLY, CLASS_PLUS_OTHER, IGNORE_CLASS;
}
