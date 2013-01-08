package org.docx4j.convert.out.html;

import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.model.styles.StyleTree;

public class HTMLConversionContextNonXSLT extends HTMLConversionContext {
	protected StyleTree styleTree;

	public HTMLConversionContextNonXSLT(HtmlSettings settings) {
		super(settings);
		this.styleTree = initializeStyleTree(settings);
	}

	private StyleTree initializeStyleTree(HtmlSettings settings) {
		//catching and swallowing an exception here isn't good,
		//that would cause later on a NPE
		return getWmlPackage().getMainDocumentPart().getStyleTree();
	}

	public StyleTree getStyleTree() {
		return styleTree;
	}
}
