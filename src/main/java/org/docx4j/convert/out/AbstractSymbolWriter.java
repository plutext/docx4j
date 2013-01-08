/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
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
