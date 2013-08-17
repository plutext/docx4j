package org.docx4j.convert.out.fo;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractSimpleModelWriter;
import org.docx4j.model.BrModel;
import org.docx4j.model.TransformState;
import org.docx4j.wml.Br;
import org.docx4j.wml.STBrType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BrWriter extends AbstractSimpleModelWriter<Br> {
	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	public BrWriter() {
		super(BrModel.MODEL_ID);
	}
	
	@Override
	protected Node toNode(AbstractWmlConversionContext context, Br modelData,
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {
	Element ret = doc.createElementNS(XSL_FO, "block");
		
		if (modelData.getType()!=null 
				&& modelData.getType().equals(STBrType.PAGE)) {
		
			ret.setAttribute("break-before", "page");
		
		} else {
		
			ret.setAttribute("white-space-treatment", "preserve");
		}
		return ret;
	}

}
