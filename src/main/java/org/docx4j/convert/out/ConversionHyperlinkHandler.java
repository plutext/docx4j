package org.docx4j.convert.out;

import org.docx4j.model.fields.HyperlinkModel;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;

/** 
 * The ConversionHyperlinkHandler gets called everytime a converter generates a hyperlink.<br/>
 * The ConversionHyperlinkHandler gets passed via the conversion settings and the implementor
 * may change the passed model before it gets converted to a link. 
 * 
 */
public interface ConversionHyperlinkHandler {
	public void handleHyperlink(HyperlinkModel hyperlinkModel, OpcPackage opcPackage, Part currentPart) throws Docx4JException;
}
