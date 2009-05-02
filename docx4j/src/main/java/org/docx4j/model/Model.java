package org.docx4j.model;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.Converter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Model {
	
	public abstract void build(Converter inst, Node node,  NodeList children  ) 
		throws TransformerException;
	
}
