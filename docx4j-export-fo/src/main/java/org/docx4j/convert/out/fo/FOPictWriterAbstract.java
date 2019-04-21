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
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.writer.AbstractPictWriter;
import org.docx4j.convert.out.fo.renderers.AbstractFORenderer;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.vml.VmlAllCoreAttributes;
import org.docx4j.vml.wordprocessingDrawing.STVerticalAnchor;
import org.docx4j.vml.wordprocessingDrawing.STWrapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Note that despite its name, this currently only handles v:textbox.
 *
 * Images (ie ./v:shape/v:imagedata) are handled differently, by legacy code.
 * 
 * Microsoft Word supports 5 "wrapping styles" for text box.
 * 
 * 1.  "in line with text": (this is a TODO here)
 * 
 * 2,3. square, tight: (these we handle with fo:float) 
 *
 * 4,5. behind, in front: (these we handle with position=absolute|fixed and z-index
 * 
 * (There are some additional wrapping styles for images, eg top and bottom, which
 *  is straightforward, and through, as to which see https://wordribbon.tips.net/T009382_Understanding_Through_Text_Wrapping.html
 *  
 *
 * @author jharrop
 *
 */
public abstract class FOPictWriterAbstract extends AbstractPictWriter {

	protected static Logger log = LoggerFactory.getLogger(FOPictWriterAbstract.class);

	protected static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	public FOPictWriterAbstract() {
		super();
	}

//	public abstract boolean foRendererSupportsFoFloat();

	
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

		// Get the shape or rectangle...
		org.docx4j.vml.VmlShapeElements shape = null;
		for (Object o : pict.getAnyAndAny() ) {

			o = XmlUtils.unwrap(o);
//			System.out.println(o.getClass().getName());
			if (o instanceof org.docx4j.vml.VmlShapeElements
					&& !(o instanceof org.docx4j.vml.CTShapetype)) {
				shape = (org.docx4j.vml.VmlShapeElements)o;
				log.debug("Found " + shape.getClass().getName());
				break;
			}
		}
		if (shape==null) {
			return context.getMessageWriter().message(context,
					"Couldn't find v:shape (or v:rectangle etc) in w:pict.");
		}

		org.docx4j.vml.CTTextbox textBox = null;
		org.docx4j.vml.wordprocessingDrawing.CTWrap w10Wrap = null;
		for (Object o : shape.getEGShapeElements() ) {

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


		

		Map<String, String> props = null;
		if (shape instanceof VmlAllCoreAttributes) {			
			props = getProperties(((VmlAllCoreAttributes)shape).getStyle());
		} else {
			log.warn(shape.getClass().getName() + " does not implement VmlAllCoreAttributes, so can't access @style if present");
			return context.getMessageWriter().message(context,
					shape.getClass().getName() + " does not implement VmlAllCoreAttributes, so can't access @style if present");
		}


//		// temp
//		if (props.size()==0) {
//			System.out.println(XmlUtils.marshaltoString(pict));
//		}


		boolean wrap = true;
		if (w10Wrap!=null) {

			if (w10Wrap.getType()!=null) {
				if ( w10Wrap.getType().equals(STWrapType.SQUARE)
					 || w10Wrap.getType().equals(STWrapType.TIGHT)
					 || w10Wrap.getType().equals(STWrapType.THROUGH) ) {

					// use fo:float
					return handleVTextBoxWrapped(context, modelContent, doc, shape, props);

				} else if (w10Wrap.getType().equals(STWrapType.TOP_AND_BOTTOM)) {
					
					// TODO, just as part of normal block flow
					log.warn("TODO: Add support for STWrapType.TOP_AND_BOTTOM");
					
				} else if ( w10Wrap.getType().equals(STWrapType.NONE) ) {

					// use block @position=absolute|fixed
					return handleVTextBoxNoWrap(context, modelContent, doc, shape, props);
				}
			}

			// the no wrap page top case
			// (do we need to handle this here?)
			if (w10Wrap.getAnchory()!=null
					&& (w10Wrap.getAnchory().equals(STVerticalAnchor.PAGE)  )) {

				return handleVTextBoxNoWrap(context, modelContent, doc, shape, props);
			}

		}

		String position = props.get("position");
		if (position!=null
				&& position.equals("absolute")) {
			return handleVTextBoxNoWrap(context, modelContent, doc, shape, props);
		}
		
		if (props.get("mso-position-horizontal-relative")!=null
				&& props.get("mso-position-horizontal-relative").equals("char")) {
			// in line with text
			log.warn("TODO: No support for mso-position-horizontal-relative:char77");
		}
		
		return handleVTextBoxWrapped(context, modelContent, doc, shape, props);
	}


	abstract public Node handleVTextBoxNoWrap(AbstractWmlConversionContext context,
			Node modelContent, Document doc,
			org.docx4j.vml.VmlShapeElements shape,
			Map<String, String> props);
	
	abstract public Node handleVTextBoxWrapped(AbstractWmlConversionContext context,
			Node modelContent, Document doc,
			org.docx4j.vml.VmlShapeElements shape,
			Map<String, String> props);
	


	protected void setBorders(Element ret) {
		ret.setAttribute("border-left-style", "solid");
		ret.setAttribute("border-top-style", "solid");
		ret.setAttribute("border-bottom-style", "solid");
		ret.setAttribute("border-right-style", "solid");

	}

	private Map<String, String> getProperties(String s) {
		
		
		if (log.isDebugEnabled()) {
			log.debug(s);
		}


		Map<String, String> map = new HashMap<String, String>();

		if (s==null) {
			log.warn("shape has no @style");
			return map;
		}

		for(final String entry : s.split(";")) {
		    final String[] parts = entry.split(":");
		    assert(parts.length == 2) : "Invalid entry: " + entry;
		    map.put(parts[0], parts[1]);
		}
		return map;

	}

	protected float parsePtsVal(String pts) {

		if (pts==null) {
			log.warn("No val!");
			return -99; // or exception?

		} else if (pts.contains("pt")) {

			pts = pts.substring(0, pts.indexOf("pt"));
			return Float.parseFloat(pts);

		} else if (pts.equals("0")) {

			return 0;

		} else {

			log.warn("Unit is not points! " + pts);
			return -99; // or exception?

		}

	}


}
