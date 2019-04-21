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
 * For no wrap (ie in front or behind), we use fo:block with @position=absolute|fixed,
 * with z-index to specify under or over.
 * 
 * position=absolute is relative to a context block.
 * position=fixed is relative to page. 
 *  
 * @author jharrop
 *
 */
public abstract class FOPictWriterNoWrapImpl extends FOPictWriterAbstract {
	
	protected static Logger log = LoggerFactory.getLogger(FOPictWriterNoWrapImpl.class);
		
	public FOPictWriterNoWrapImpl() {
		super();
	}
	
	
	/* (non-Javadoc)
	 * @see org.docx4j.convert.out.fo.FOPictWriterAbstract#handleVTextBoxNoWrap(org.docx4j.convert.out.common.AbstractWmlConversionContext, org.w3c.dom.Node, org.w3c.dom.Document, org.docx4j.vml.VmlShapeElements, java.util.Map)
	 */
	public Node handleVTextBoxNoWrap(AbstractWmlConversionContext context,
			Node modelContent, Document doc,
			org.docx4j.vml.VmlShapeElements shape,
			Map<String, String> props) {

		String mso_position_vertical_relative = props.get("mso-position-vertical-relative");
		String mso_position_vertical = props.get("mso-position-vertical");
		String position = props.get("position");
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


		// not wrap, so use
		// block-container .. text does not wrap around
		// .. see https://www.ecrion.com/help/products/xfrenderingserver/xfultrascalehelp4/absolut_positioning.htm

		// not wrap: can use fo:block-container/@position= fixed to approximate relative to page
		log.debug("textbox - over/behind docx text");
		Element ret = doc.createElementNS(XSL_FO, "block-container");
		XmlUtils.treeCopy(modelContent.getChildNodes(), ret);

		setBorders(ret); // for now, solid borders, so we can see the text box


		String mso_position_horizontal_relative = props.get("mso-position-horizontal-relative");
		String mso_position_horizontal = props.get("mso-position-horizontal"); // eg center
		

		if (mso_position_horizontal_relative==null) {
			log.warn("No support for mso_position_horizontal_relative==null");
		} else if (!mso_position_horizontal_relative.equals("text") ) {
			log.warn("No support for mso_position_horizontal_relative==" + mso_position_horizontal_relative.equals("text"));
		} else {  // mso_position_horizontal_relative.equals("text")

			float boxWidth = parsePtsVal(props.get("width"));

			ret.setAttribute("width",  props.get("width") );
			ret.setAttribute("height",  props.get("height") );

			if (mso_position_horizontal==null) {

				log.warn("No support for mso_position_horizontal==null");

			} else if ( mso_position_horizontal.equals("left")) {

				ret.setAttribute("left",  "0pt");

			} else if ( mso_position_horizontal.equals("center")) {

				// convert width:186.95pt to margin setting
				int marginLeft = Math.round((writableWidthPts - boxWidth)/2);

				ret.setAttribute("left", marginLeft + "pt");

			} else if ( mso_position_horizontal.equals("right")) {

				// page width - box width
				int marginLeft = Math.round((writableWidthPts - boxWidth));
				ret.setAttribute("left", marginLeft + "pt");

			} else if ( mso_position_horizontal.equals("absolute")) {
				// eg in Word UI, "Absolute position to right of column"

				// eg margin-left:108pt
				ret.setAttribute("margin-left", props.get("margin-left") );
			}

		}

		ret.setAttribute("z-index", props.get("z-index") );


		String margin_top = props.get("margin-top");
		if (  (position!=null && position.equals("absolute"))
				|| (mso_position_vertical_relative!=null && mso_position_vertical_relative.equals("text"))) {

			ret.setAttribute("position", "absolute");

			// @top
			if (margin_top==null) {
				// ret.setAttribute("top", "0pt");
			} else {
				//float marginTop = parsePtsVal(margin_top);
				ret.setAttribute("top", margin_top);
			}

			log.debug(XmlUtils.w3CDomNodeToString(ret));

			return ret;
			
		} else if ( mso_position_vertical_relative==null) {

			if(log.isWarnEnabled()) {
                log.warn(XmlUtils.marshaltoString(shape));
            }
			return context.getMessageWriter().message(context,
					"mso_position_vertical_relative==null.  What to do?");
			
		} else if ( mso_position_vertical_relative.equals("page")
				|| mso_position_vertical_relative.equals("top-margin-area")
				|| mso_position_vertical_relative.equals("bottom-margin-area")) {

			ret.setAttribute("position", "fixed");  // relative to page

			if ( mso_position_vertical_relative.equals("page")) {

				if (mso_position_vertical.equals("top")) {

					// @top
					if (margin_top==null) {
						// ret.setAttribute("top", "0pt");
					} else {
						//float marginTop = parsePtsVal(margin_top);
						ret.setAttribute("top", margin_top);
					}

				} else if (mso_position_vertical.equals("bottom")) {

					//float boxHeight = parsePtsVal(props.get("height"));
					//int top = Math.round(writableHeightPts - boxHeight);

					int top = Math.round(writableHeightPts );
					ret.setAttribute("top", top + "pt");

				} else {
					log.warn("No support for mso_position_vertical==" + mso_position_vertical);
				}

				log.debug(XmlUtils.w3CDomNodeToString(ret));
				return ret;

			} else if ( mso_position_vertical_relative.equals("top-margin-area")) {

				// @top
				if (margin_top==null) {
					// ret.setAttribute("top", "0pt");
				} else {
					//float marginTop = parsePtsVal(margin_top);
					ret.setAttribute("top", margin_top);
				}

				log.debug(XmlUtils.w3CDomNodeToString(ret));
				return ret;

			} else if (  mso_position_vertical_relative.equals("bottom-margin-area")) {

				//float boxHeight = parsePtsVal(props.get("height"));
				//int top = Math.round(writableHeightPts - boxHeight);

				int top = Math.round(writableHeightPts );
				ret.setAttribute("top", top + "pt");

				log.debug(XmlUtils.w3CDomNodeToString(ret));
				return ret;

			} else {

				// Can't happen
				return context.getMessageWriter().message(context,
						"TODO (how did we get here?) mso-position-vertical-relative=" + mso_position_vertical_relative);

			}
		}  else {

			return context.getMessageWriter().message(context,
					"TODO support no-wrap + mso-position-vertical-relative=" + mso_position_vertical_relative);

		}

		

	}
}
