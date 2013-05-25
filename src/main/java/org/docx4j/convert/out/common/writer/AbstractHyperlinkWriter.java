/*
 *  Copyright 2013, Plutext Pty Ltd.
 *   
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
package org.docx4j.convert.out.common.writer;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.Converter;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.HyperlinkModel;
import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.docx4j.wml.P;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * @author jharrop
 * @since 3.0.0
 */
public abstract class AbstractHyperlinkWriter implements ModelConverter {
	
	private final static Logger log = Logger.getLogger(AbstractHyperlinkWriter.class);

	@Override
	public Node toNode(AbstractWmlConversionContext context, Model hyperlinkModel, 
			TransformState state, Document doc) throws TransformerException {
		
	    HyperlinkModel hm = (HyperlinkModel)hyperlinkModel;
	    P.Hyperlink hyperlink = hm.getHyperlink();
	    return toNode(context, hm, doc);
	}

	protected abstract Node toNode(AbstractWmlConversionContext context, HyperlinkModel hm, Document doc) 
			throws TransformerException ;

	@Override
	public String getID() {
		return HyperlinkModel.MODEL_ID;
	}
	
	@Override
	public TransformState createTransformState() {
		// not used
		return null;
	}
	
    public String resolveHref(AbstractWmlConversionContext context, String id  )  {
    	
    	org.docx4j.relationships.Relationship rel = context.getWmlPackage().getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id);
    	
    	if ( rel != null) {
    		
        	// TODO resolve ServerRelativePath, if its not a full URL 

    		return rel.getTarget();
    		
    	} else {
    		
    		log.error("Couldn't resolve hyperlink for rel " + id);    		
    		return "";    		
    	}
    }
	
    /**
     * Where a collection of docx files are all being converted, hyperlinks between
     * them can be converted to the new format (ie html or pdf) as well.
     * 
     * @param filename
     * @param extMappings
     * @return
     */
    protected static String convertExtension(String filename, Map<String,String> extMappings) {
    
		if (extMappings == null)
			return filename;
		int pos = filename.lastIndexOf(".");
		if (pos < 0)
			return filename;

		String ext = filename.substring(pos+1);
		String newExt = extMappings.get(ext);
		if (newExt == null) {
			return filename;
		} else {
			return filename.substring(0, pos+1) + newExt;
		}
	}
    
//	public static void main(String[] args) throws Exception {
//		
//    	Map<String, String> extensionMappings = new HashMap<String, String>(); 
//    	extensionMappings.put("docx", "html");
//
//    	System.out.println(convertExtension("some.docx.", extensionMappings));
//	}
    
}
