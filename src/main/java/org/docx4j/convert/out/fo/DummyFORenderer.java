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
package org.docx4j.convert.out.fo;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.vml.CTShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/** The Dummy FO Renderer doesn't render anything, it just outputs
 *  the fo document to the OutputStream. 
 * 
 */
public class DummyFORenderer implements FORenderer {
	protected static Logger log = LoggerFactory.getLogger(DummyFORenderer.class);
	protected static final FORenderer INSTANCE = new DummyFORenderer();
	
	public static FORenderer getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void render(String foDocument, FOSettings settings,
			boolean twoPass,
			List<SectionPageInformation> pageNumberInformation,
			OutputStream outputStream) throws Docx4JException {
	Writer writer = null;
		if (twoPass) {
			log.warn("Using the DummyFORenderer with a two pass conversion, there might be placeholders in the output");
		}
		try {
			writer = new OutputStreamWriter(outputStream, "UTF-8");
			writer.write(foDocument);
			writer.flush();
		} catch (Exception e) {
			throw new Docx4JException("Exception while storing fo document to OutputStream: " + e.getMessage(), e);
		}
	}

	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";
	
	@Override  // same as FOP for now
	public Node handleVTextBox(AbstractWmlConversionContext context,
			Node modelContent, Document doc, 
			org.docx4j.vml.CTShape shape,
			Map<String, String> props, 
			boolean wrap) {
	
		String mso_position_vertical_relative = props.get("mso-position-vertical-relative");
//		String mso_position_vertical = props.get("mso-position-vertical");
//		String z_index = props.get("z_index"); // negative -> behind
//
//		// TODO: use these
//		String mso_position_horizontal_relative = props.get("mso-position-horizontal-relative");
//		String mso_position_horizontal = props.get("mso-position-horizontal"); // eg center
		
		String margin_top = props.get("margin-top");
		String margin_left = props.get("margin-left");
//		String width = props.get("width");
//		String height = props.get("height");

		if (mso_position_vertical_relative!=null) {

			if ( mso_position_vertical_relative.equals("text")) {
				Element ret=null;
				
				// Position is relative to paragraph.
				if (wrap) {

					// Just lay it out in the flow; graceful degradation
					ret = doc.createElementNS(XSL_FO, "block");  
					XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
					return ret;
				}
				// Otherwise, text is not wrapped ...
				// .. but how do we use z-index?
				// http://www.w3.org/TR/xsl/#common-absolute-position-properties
				ret = doc.createElementNS(XSL_FO, "block-container");  
				ret.setAttribute("absolute-position", "absolute");
				if (margin_top!=null) {
					ret.setAttribute("top", margin_top);						
				}
				if (margin_left!=null) { // TODO consider mso_position_horizontal
					ret.setAttribute("left", margin_left);						
				}
				// TODO how to get @right from width?
				// TODO how to get @bottom from height?
				
				XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
				return ret;
				
				// could fo:block-container/@absolute-position (= fixed)
				// be used in 2 pass to emulate wrapped top/bottom?
				
			}  else {

				return context.getMessageWriter().message(context, 
						"TODO: support for mso-position-vertical-relative=" + mso_position_vertical_relative);
				
			}
			
		}
		
		// FOP doesn't support fo:float, so we can't do anything except above
		return context.getMessageWriter().message(context, 
				"v:textbox to fo:float is not supported by Apache FOP.  Try another.");
			
	}	
}
	

