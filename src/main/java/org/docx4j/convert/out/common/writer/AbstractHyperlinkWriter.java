package org.docx4j.convert.out.common.writer;

import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.HyperlinkModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractHyperlinkWriter implements ModelConverter, AbstractFldSimpleWriter.FldSimpleNodeWriterHandler {
	protected static final String HYPERLINK_NAME = "HYPERLINK";

//ModelConverter	
	@Override
	public String getID() {
		return HyperlinkModel.MODEL_ID;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, Model model, TransformState state, Document doc) throws TransformerException {
	HyperlinkModel hyperlinkModel = (HyperlinkModel)model;
	Node ret = null;
		ret = toNode(context, hyperlinkModel, doc);
		return ret;
	}

	protected abstract Node toNode(AbstractWmlConversionContext context, HyperlinkModel model, Document doc) throws TransformerException;

	@Override
	public TransformState createTransformState() {
		return null;
	}

	
//AbstractFldSimpleWriter.FldSimpleNodeWriterHandler	
	@Override
	public String getName() {
		return HYPERLINK_NAME;
	}

	@Override
	public int getProcessType() {
		return PROCESS_NONE;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
	HyperlinkModel hyperlinkModel = new HyperlinkModel();
		hyperlinkModel.setup(context.getWmlPackage(), context.getCurrentPart());
		hyperlinkModel.build(model, model.getContent());
		return toNode(context, hyperlinkModel, null, doc);
	}

}
