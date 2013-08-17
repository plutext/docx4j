package org.docx4j.convert.out.fo;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractHyperlinkWriter;
import org.docx4j.convert.out.common.writer.HyperlinkUtil;
import org.docx4j.model.fields.HyperlinkModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class HyperlinkWriter extends AbstractHyperlinkWriter {

	@Override
	protected Node toNode(AbstractWmlConversionContext context, HyperlinkModel model, Document doc) throws TransformerException {
		return HyperlinkUtil.toNode(HyperlinkUtil.FO_OUTPUT, context, model, model.getContent(), doc);
	}

}
