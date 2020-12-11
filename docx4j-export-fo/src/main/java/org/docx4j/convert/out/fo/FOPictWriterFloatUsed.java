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

import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.model.structure.PageDimensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * FOPictWriter intended to be used in conjunction with a 
 * renderer which supports fo:float.
 * 
 * For FOP's float support, see https://xmlgraphics.apache.org/fop/fo.html#floats
 * 
 * Note that despite its name, this currently only handles v:textbox.
 * 
 * This class handles wrap=square|tight|through, by using fo:float
 * 
 * @fo:float really only gives us 3 options to work with:
 * =before, to float to top of page (a before float)
 * =start, to float left (a side float)
 * = end, to float right
 * 
 * @author jharrop
 *
 */
public  class FOPictWriterFloatUsed extends FOPictWriterNoWrapImpl {
	
	protected static Logger log = LoggerFactory.getLogger(FOPictWriterFloatUsed.class);
		
	public FOPictWriterFloatUsed() {
		super();
	}
	
//	@Override
//	public  boolean foRendererSupportsFoFloat() {
//		return true;
//	}
//
//	

	public Node handleVTextBoxWrapped(AbstractWmlConversionContext context,
			Node modelContent, Document doc,
			org.docx4j.vml.VmlShapeElements shape,
			Map<String, String> props) {

		log.debug("textbox - wrapped text");
		//  * 2,3. square, tight: (these we handle with fo:float) 

		
		String mso_position_vertical_relative = props.get("mso-position-vertical-relative");
		String mso_position_vertical = props.get("mso-position-vertical");
//		String z_index = props.get("z_index"); // negative -> behind
//

//		String width = props.get("width");
//		String height = props.get("height");


		// How to get current page width??
		ConversionSectionWrapper csw = context.getSections().getCurrentSection();
		PageDimensions pageDimensions = csw.getPageDimensions();

		// pgSz.getW().intValue() - (pgMar.getLeft().intValue() + pgMar.getRight().intValue());
		int writableWidthTwips = pageDimensions.getWritableWidthTwips();
		float writableWidthPts = writableWidthTwips/20;
		int writableHeightTwips = pageDimensions.getWritableHeightTwips();
		float writableHeightPts = writableHeightTwips/20;

		// TODO - avoid reference to FORenderer here! See
		FORenderer foRenderer  = ((FOSettings)context.getConversionSettings()).getCustomFoRenderer();
		log.debug(foRenderer.getClass().getName());


	// Floats define a block that is "out of line" in drifts to the top/left/right side of the page;
	// the text *flows around it*.

		if ( mso_position_vertical_relative !=null
				&& mso_position_vertical_relative.equals("text")) {

			// Just lay it out in the flow; graceful degradation
			Element ret = doc.createElementNS(XSL_FO, "block");
			setBorders(ret); // for now, solid borders, so we can see the text box

			String margin_top = props.get("margin-top");
			if (margin_top==null) {
				log.error("margin top prop not found.  What to do?");
				XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
				return ret;
			} else {
				float marginTop = parsePtsVal(margin_top);


					String mso_position_horizontal_relative = props.get("mso-position-horizontal-relative");
					String mso_position_horizontal = props.get("mso-position-horizontal"); // eg center

					
//							if (mso_position_horizontal_relative==null) {
//								log.warn("No support for mso_position_horizontal_relative==null");
//								XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
//								return ret;
//							} else if (!mso_position_horizontal_relative.equals("text") ) {
//								log.warn("No support for mso_position_horizontal_relative==" + mso_position_horizontal_relative.equals("text"));
//								XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
//								return ret;
//							} else {  // mso_position_horizontal_relative.equals("text")


						float ml = parsePtsVal(props.get("margin-left"));

						if (mso_position_horizontal==null) {

							log.warn("No support for mso_position_horizontal==null");
							XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
							return ret;
						}
						else if ( mso_position_horizontal.equals("absolute")) {
							// eg in Word UI, "Absolute position to right of column"

							// We can only float left or right (not center etc)
							log.warn("Degrading absolute position to left/right");
							if (ml/writableWidthPts <= 0.5) {
								mso_position_horizontal = "left";
							} else {
								mso_position_horizontal = "right";
							}
						}

						ret = doc.createElementNS(XSL_FO, "float");
						setBorders(ret); // for now, solid borders, so we can see the text box

						ret.setAttribute("width",  props.get("width") );
						ret.setAttribute("height",  props.get("height") );

						// TODO margin-top, eg  margin-top:176.3pt;
						
						if ( mso_position_horizontal.equals("left")) {

							ret.setAttribute("float",  "start");

						} else if ( mso_position_horizontal.equals("center")) {

							log.warn("Degrading center to right");
							ret.setAttribute("float",  "end");

						} else if ( mso_position_horizontal.equals("right")) {

							ret.setAttribute("float",  "end");
						}
						XmlUtils.treeCopy(modelContent.getChildNodes(), ret);

					
			}

			log.debug(XmlUtils.w3CDomNodeToString(ret));

			return ret;

		}  else {

				
			if  (mso_position_vertical_relative.equals("page")
					|| mso_position_vertical_relative.equals("top-margin-area")
					|| mso_position_vertical_relative.equals("bottom-margin-area")) {

				if ( mso_position_vertical_relative.equals("page")) {

					if (mso_position_vertical.equals("top")) {

						Element ret = doc.createElementNS(XSL_FO, "float");
						ret.setAttribute("float", "before");
						setBorders(ret); // for now, solid borders, so we can see the text box

						log.debug(XmlUtils.w3CDomNodeToString(ret));
						return ret;

					} else if (mso_position_vertical.equals("bottom")) {

						Element ret = doc.createElementNS(XSL_FO, "footnote");
						Element footnoteBody = doc.createElementNS(XSL_FO, "footnote-body");
						ret.appendChild(footnoteBody);
						Element block = doc.createElementNS(XSL_FO, "block");
						footnoteBody.appendChild(block);
						setBorders(block); // for now, solid borders, so we can see the text box

						XmlUtils.treeCopy(modelContent.getChildNodes(), block);
						return ret;


					} else {

						log.warn("No support for mso_position_vertical==" + mso_position_vertical);
						return context.getMessageWriter().message(context,
								"TODO for fo:float capable renderer, support no-wrap + mso-position-vertical=" + mso_position_vertical);
					}


				} else if ( mso_position_vertical_relative.equals("top-margin-area")) {

					Element ret = doc.createElementNS(XSL_FO, "float");
					ret.setAttribute("float", "before");
					setBorders(ret); // for now, solid borders, so we can see the text box

					log.debug(XmlUtils.w3CDomNodeToString(ret));
					return ret;

				} else if (  mso_position_vertical_relative.equals("bottom-margin-area")) {

					Element ret = doc.createElementNS(XSL_FO, "footnote");
					Element footnoteBody = doc.createElementNS(XSL_FO, "footnote-body");
					ret.appendChild(footnoteBody);
					Element block = doc.createElementNS(XSL_FO, "block");
					footnoteBody.appendChild(block);
					setBorders(block); // for now, solid borders, so we can see the text box

					XmlUtils.treeCopy(modelContent.getChildNodes(), block);
					return ret;

				} else {

					// Can't happen
					return context.getMessageWriter().message(context,
							"TODO (how did we get here?) mso-position-vertical-relative=" + mso_position_vertical_relative);

				}
			}  else {

				return context.getMessageWriter().message(context,
						"TODO for fo:float capable renderer, support no-wrap + mso-position-vertical-relative=" + mso_position_vertical_relative);

			}
		}
				
	}	
	
}
