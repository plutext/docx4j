package org.docx4j.convert.out.common.writer;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.AbstractSimpleModel;
import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractSimpleModelWriter<T> implements ModelConverter {
	protected String modelID = null;
	
	protected AbstractSimpleModelWriter(String modelID) {
		this.modelID = modelID;
	}

	@Override
	public String getID() {
		return modelID;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, Model model,
			TransformState state, Document doc) throws TransformerException {
	AbstractSimpleModel<T> simpleModel = (AbstractSimpleModel<T>)model;
		return toNode(context, 
				      simpleModel.getModelData(), simpleModel.getContent(),
				      state, doc);
	}

	protected abstract Node toNode(AbstractWmlConversionContext context, 
			T modelData, Node modelContent, 
			TransformState state, Document doc) throws TransformerException;

	@Override
	public TransformState createTransformState() {
		return null;
	}

}
