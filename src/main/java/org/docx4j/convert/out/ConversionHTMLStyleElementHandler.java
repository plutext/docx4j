package org.docx4j.convert.out;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface ConversionHTMLStyleElementHandler {
	public Element createStyleElement(OpcPackage opcPackage, Document document, String styleDefinition);
}
