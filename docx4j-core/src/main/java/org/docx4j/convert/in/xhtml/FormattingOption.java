package org.docx4j.convert.in.xhtml;

/**
 * CLASS_TO_STYLE_ONLY: a Word style matching a class attribute will
 * be used, and nothing else
 * 
 * CLASS_PLUS_OTHER: a Word style matching a class attribute will
 * be used; other css will be translated to direct formatting
 * 
 * IGNORE_CLASS: css will be translated to direct formatting
 *
 */
public enum FormattingOption {

	CLASS_TO_STYLE_ONLY, CLASS_PLUS_OTHER, IGNORE_CLASS;
}
