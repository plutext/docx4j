package org.docx4j.model;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

public class AbstractSimpleModel<T> extends Model {
	protected T modelData = null;
	protected Node content = null;
	
	public T getModelData() {
		return modelData;
	}
	
	public Node getContent() {
		return content;
	}
	
	@Override
	public void build(Object unmarshalledNode, Node content) throws TransformerException {
		this.content = content;
		try {
			modelData = (T)unmarshalledNode;
		} catch (ClassCastException e) {
			throw new TransformerException("Node is not of the expected type it is " + unmarshalledNode.getClass().getName());
		}
	}

	@Override
	public Object toJAXB() {
		return null;
	}

}
