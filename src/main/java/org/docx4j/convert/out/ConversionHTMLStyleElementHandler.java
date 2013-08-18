package org.docx4j.convert.out;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * How to generate the style element. The default implementation is inline.
 */
public interface ConversionHTMLStyleElementHandler {
	public Element createStyleElement(OpcPackage opcPackage, Document document, String styleDefinition);
}
