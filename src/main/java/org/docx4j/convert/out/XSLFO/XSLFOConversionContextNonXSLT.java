package org.docx4j.convert.out.XSLFO;

import org.docx4j.convert.out.pdf.viaXSLFO.PdfConversionContext;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.model.styles.StyleTree;

public class XSLFOConversionContextNonXSLT extends PdfConversionContext {
	protected StyleTree styleTree;

	public XSLFOConversionContextNonXSLT(PdfSettings settings) {
		super(settings);
		this.styleTree = initializeStyleTree(settings);
	}

	private StyleTree initializeStyleTree(PdfSettings settings) {
		//catching and swallowing an exception here isn't good,
		//that would cause later on a NPE
		return getWmlPackage().getMainDocumentPart().getStyleTree();
	}

	public StyleTree getStyleTree() {
		return styleTree;
	}
}
