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

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractPictWriter;
import org.docx4j.vml.wordprocessingDrawing.STWrapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Note that despite its name, this currently only handles v:textbox.
 * 
 * Images (ie ./v:shape/v:imagedata) are handled differently, by legacy code.
 * 
 * 
 * @author jharrop
 *
 */
public class PictWriter extends AbstractPictWriter {
	
	protected static Logger log = LoggerFactory.getLogger(PictWriter.class);
	
	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	public PictWriter() {
		super();
	}
	
	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, 
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {

		org.docx4j.wml.Pict pict = (org.docx4j.wml.Pict)unmarshalledNode;
		
		/*
            <w:pict>
              <v:shapetype id="_x0000_t202" coordsize="21600,21600" o:spt="202" path="m,l,21600r21600,l21600,xe">
                <v:stroke joinstyle="miter"/>
                <v:path gradientshapeok="t" o:connecttype="rect"/>
              </v:shapetype>
              <v:shape id="Text Box 2" o:spid="_x0000_s1026" 
              			type="#_x0000_t202" 
              			style="position:absolute;margin-left:0;margin-top:0;width:186.95pt;height:110.55pt;z-index:251659264;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top" o:gfxdata=" yuck!">
                <v:textbox style="mso-fit-shape-to-text:t">
                  <w:txbxContent>
                    <w:p >
                      <w:r>
                        <w:t>Content</w:t>
                      </w:r>
                    </w:p>
                  </w:txbxContent>
                </v:textbox>
              </v:shape>
            </w:pict>
		 */

		// Get the shape
		org.docx4j.vml.CTShape shape = null;
		for (Object o : pict.getAnyAndAny() ) {
			
			o = XmlUtils.unwrap(o);
//			System.out.println(o.getClass().getName());
			if (o instanceof org.docx4j.vml.CTShape) {
				shape = (org.docx4j.vml.CTShape)o;
				break;
			}
		}
		if (shape==null) {
			return context.getMessageWriter().message(context, 
					"Couldn't find v:shape in w:pict.");
		}

		org.docx4j.vml.CTTextbox textBox = null;
		org.docx4j.vml.wordprocessingDrawing.CTWrap w10Wrap = null;  
		for (Object o : shape.getPathOrFormulasOrHandles() ) {
			
			o = XmlUtils.unwrap(o);
			
			if (o instanceof org.docx4j.vml.CTTextbox) {
				textBox = (org.docx4j.vml.CTTextbox)o;
			}
			if (o instanceof org.docx4j.vml.wordprocessingDrawing.CTWrap) {
				w10Wrap = (org.docx4j.vml.wordprocessingDrawing.CTWrap)o;
			}
		}
		if (textBox==null) {
			return context.getMessageWriter().message(context, 
					"Couldn't find v:textbox in w:shape.");
		}

		Map<String, String> props = getProperties(textBox.getStyle());
		
		
		boolean wrap = true;
		if (w10Wrap!=null 
				&& w10Wrap.getType()!=null) {
			
			if (w10Wrap.getType().equals(STWrapType.TOP_AND_BOTTOM)
					|| w10Wrap.getType().equals(STWrapType.SQUARE)
					|| w10Wrap.getType().equals(STWrapType.TIGHT)) {
				
				wrap = true;
			} else {
				wrap = false;					
			}
		}
			
		FORenderer foRenderer  = ((FOSettings)context.getConversionSettings()).getCustomFoRenderer();
		log.debug(foRenderer.getClass().getName());
//		boolean isFop = (foRenderer==null || (foRenderer instanceof ApacheFORenderer));
		return foRenderer.handleVTextBox(context, modelContent, doc, shape, props, wrap);
	}

	
	private Map<String, String> getProperties(String s) {
		
		Map<String, String> map = new HashMap<String, String>();
		for(final String entry : s.split(";")) {
		    final String[] parts = entry.split(":");
		    assert(parts.length == 2) : "Invalid entry: " + entry;
		    map.put(parts[0], parts[1]);
		}		
		return map;
		
	}

}
