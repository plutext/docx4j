package org.docx4j.convert.out;

import javax.xml.transform.TransformerException;

import org.docx4j.model.Model;
import org.docx4j.model.SymbolModel;
import org.docx4j.model.TransformState;
import org.docx4j.wml.R;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/*
 * Convert the character reference to a string, 
 * since XSLT doesn't like us putting &#x and @w:char and ';' together
 * 
 *  @author Jason Harrop
 *  
*/
public abstract class AbstractSymbolWriter implements ModelConverter {

	protected static class SymbolModelTransformState implements TransformState {
	}
	  

	@Override
	public Node toNode(AbstractWmlConversionContext context, Model symbolModel, TransformState state, Document doc) throws TransformerException {
	    SymbolModel sm = (SymbolModel)symbolModel;
	    R.Sym sym = sm.getSym();
	    return toNode(context, sym, doc);
	}

	protected abstract Node toNode(AbstractWmlConversionContext context, R.Sym sym, Document doc) throws TransformerException ;

	@Override
	public String getID() {
		return SymbolModel.MODEL_ID;
	}
	
	@Override
	public TransformState createTransformState() {
		//as the SymbolWriter doesn't use it's transform state
		//it could return here null
		return new SymbolModelTransformState();
	}
}
