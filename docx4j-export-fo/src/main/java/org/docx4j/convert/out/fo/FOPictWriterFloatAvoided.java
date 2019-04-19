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
import org.docx4j.convert.out.fo.renderers.AbstractFORenderer;
import org.docx4j.model.structure.PageDimensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * FOPictWriter intended to be used in conjunction with a 
 * renderer which doesn't support fo:float (eg old versions of Apache FOP).
 * 
 * Note that despite its name, this currently only handles v:textbox.
 * 
 * Images (ie ./v:shape/v:imagedata) are handled differently, by legacy code.
 * 
 * 
 * @author jharrop
 *
 */
public  class FOPictWriterFloatAvoided extends  FOPictWriterNoWrapImpl {
	
	protected static Logger log = LoggerFactory.getLogger(FOPictWriterFloatAvoided.class);
		
	public FOPictWriterFloatAvoided() {
		super();
	}
	
//	@Override
//	public  boolean foRendererSupportsFoFloat() {
//		return false; 
//	}
	
	public Node handleVTextBoxWrapped(AbstractWmlConversionContext context,
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

			// Floats define a block that is "out of line" in drifts to the top/left/right side of the page;
			// the text *flows around it*.

			log.debug("textbox - wrapped text");

			// position:absolute;margin-left:25pt;margin-top:176.3pt;width:125.25pt;height:30pt
//					&& position.equals("absolute")) {
				
//			if (mso_position_vertical_relative==null
//					&& position==null) {
//
//                if(log.isWarnEnabled()) {
//                    try {
//                        log.warn(XmlUtils.marshaltoString(shape));
//                    } catch (Exception e) {
//                        log.warn(e.getMessage());
//                    }
//                }
//				return context.getMessageWriter().message(context,
//						"mso_position_vertical_relative==null.  What to do?");
//
//			} else {

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

							//  old FOP does not support float, so the best we can do is ...

							if (marginTop<=0) {
								// The text box is above the para
								marginTopZeroCase( props, ret, writableWidthPts);
								XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
							} else {
								// The text box is next to or below the para
								// We'll assume here it is next to (since we don't know how long the para is)
								// Implement with a table (simulating float) .. simple mindedly put the textbox either left or right.
								// Obviously this won't work if the para had 2 text boxes, one to left and other to right..

								ret = doc.createElementNS(XSL_FO, "table");
								if (marginTopPositiveCase(foRenderer, props, doc, ret, writableWidthPts, modelContent.getChildNodes())) {

								} else {
									// Couldn't do it.
									ret = doc.createElementNS(XSL_FO, "block");
									setBorders(ret); // for now, solid borders, so we can see the text box
									XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
								}
							}
					}

					log.debug(XmlUtils.w3CDomNodeToString(ret));

					return ret;

				} else {

					return context.getMessageWriter().message(context,
							"TODO for fo:float INcapable renderer, support no-wrap + mso-position-vertical-relative=" + mso_position_vertical_relative);

				}
				
	}

	/**
	 * Wrap case, for renderers which don't support fo:float (eg Apache FOP)
	 *
	 * @param props
	 * @param ret
	 * @param widthPts
	 */
	private void marginTopZeroCase(Map<String, String> props, Element ret, float widthPts ) {

//			mso-position-horizontal:center
//			mso-position-horizontal-relative:text

		String mso_position_horizontal_relative = props.get("mso-position-horizontal-relative");
		String mso_position_horizontal = props.get("mso-position-horizontal"); // eg center


		if (mso_position_horizontal_relative==null) {
			log.warn("No support for mso_position_horizontal_relative==null");
		} else if (!mso_position_horizontal_relative.equals("text") ) {
			log.warn("No support for mso_position_horizontal_relative==" + mso_position_horizontal_relative.equals("text"));
		} else {  // mso_position_horizontal_relative.equals("text")

			// strategy is to use @margin-left for positioning
			// (since the margin is outside the block border).
			// This works for FOP.  If it didn't, I guess we could try using a table

			float boxWidth = parsePtsVal(props.get("width"));

			if (mso_position_horizontal==null) {

				log.warn("No support for mso_position_horizontal==null");

			} else if ( mso_position_horizontal.equals("left")) {

				ret.setAttribute("margin-left",  "0pt");

				// page width - box width
				int marginRight = Math.round((widthPts - boxWidth));
				ret.setAttribute("margin-right", marginRight + "pt");

			} else if ( mso_position_horizontal.equals("center")) {

				// convert width:186.95pt to margin setting
				int marginLeft = Math.round((widthPts - boxWidth)/2);

				ret.setAttribute("margin-left", marginLeft + "pt");
				ret.setAttribute("margin-right", marginLeft + "pt");

			} else if ( mso_position_horizontal.equals("right")) {

				ret.setAttribute("margin-right",  "0pt");
				// page width - box width
				int marginLeft = Math.round((widthPts - boxWidth));
				ret.setAttribute("margin-left", marginLeft + "pt");

			} else if ( mso_position_horizontal.equals("absolute")) {
				// eg in Word UI, "Absolute position to right of column"

				// eg margin-left:108pt
				ret.setAttribute("margin-left", props.get("margin-left") );

				float ml = parsePtsVal(props.get("margin-left"));
				int mRight = Math.round((widthPts - (boxWidth+ml)));
				ret.setAttribute("margin-right",  mRight + "pt");
			}

		}

	}

	/**
	 * Wrap case, for renderers which don't support fo:float (eg Apache FOP).  Try to degrade gracefully.
	 *
	 * @param props
	 * @param doc
	 * @param ret
	 * @param widthPts
	 * @param childNodes
	 * @return
	 */
	private boolean marginTopPositiveCase(FORenderer foRenderer, Map<String, String> props, Document doc, Element ret, float widthPts,
			NodeList childNodes) {

//			mso-position-horizontal:center
//			mso-position-horizontal-relative:text

		String mso_position_horizontal_relative = props.get("mso-position-horizontal-relative");
		String mso_position_horizontal = props.get("mso-position-horizontal"); // eg center


		if (mso_position_horizontal_relative==null) {
			log.warn("No support for mso_position_horizontal_relative==null");
			return false;
		} else if (!mso_position_horizontal_relative.equals("text") ) {
			log.warn("No support for mso_position_horizontal_relative==" + mso_position_horizontal_relative.equals("text"));
			return false;
		} else {  // mso_position_horizontal_relative.equals("text")

			// strategy is to use @margin-left for positioning
			// (since the margin is outside the block border).
			// This works for FOP.  If it didn't, I guess we could try using a table

			float boxWidth = parsePtsVal(props.get("width"));

			if (mso_position_horizontal==null) {

				log.warn("No support for mso_position_horizontal==null");
				return false;
			}
			float ml = parsePtsVal(props.get("margin-left"));

			if ( mso_position_horizontal.equals("absolute")) {
				// eg in Word UI, "Absolute position to right of column"

				// our simple minded approach here is to treat as left, right or centre ..

				if (ml/widthPts < 0.334) {
					// fake left
					mso_position_horizontal = "left";
				} else if (ml/widthPts < 0.665) {
					// fake right
					mso_position_horizontal = "right";
				} else {
					// fake center
					mso_position_horizontal = "center";
				}
			}

			if ( mso_position_horizontal.equals("left")) {

				Element tcol1 = doc.createElementNS(XSL_FO, "table-column");
				ret.appendChild(tcol1);
				tcol1.setAttribute("column-number", "1"); // optional?
				// width of left cell is ml + boxWidth
				int col1W = Math.round(ml + boxWidth);
				tcol1.setAttribute("column-width", col1W +"pt");

				Element tcol2 = doc.createElementNS(XSL_FO, "table-column");
				ret.appendChild(tcol2);
				tcol2.setAttribute("column-number", "2"); // optional?
				// width of left cell is ml + boxWidth
				int col2W = Math.round(widthPts-col1W);
				tcol2.setAttribute("column-width", col2W +"pt");
				
				/*
		          <fo:table-body start-indent="0in">
		            <fo:table-row>
		              <fo:table-cell>

						<!-- textbox content goes here -->
		                
		              </fo:table-cell>
		              <fo:table-cell>

						<!-- paragraph content goes here 
						
								HELP FIXME we don't have access to that here! 
								
								Not so easy to pass it .. so use a post-processing step which gets it?
						
						-->
						
		              </fo:table-cell>
		            </fo:table-row>
		          </fo:table-body>	
		          */
				Element tbody = doc.createElementNS(XSL_FO, "table-body");
				ret.appendChild(tbody);
				Element trow = doc.createElementNS(XSL_FO, "table-row");
				tbody.appendChild(trow);

				// Cell 1 is the text box
				Element tc1 = doc.createElementNS(XSL_FO, "table-cell");
				trow.appendChild(tc1);
				Element block = doc.createElementNS(XSL_FO, "block");


				setBorders(block); // for now, solid borders, so we can see the text box
				tc1.appendChild(block);
				XmlUtils.treeCopy(childNodes, block);

				// Cell 2 is the p content proper (which we don't have)
				Element tc2 = doc.createElementNS(XSL_FO, "table-cell");
				trow.appendChild(tc2);
//					tc2.appendChild(
//							doc.createComment("CONTENT GOES HERE"));
				Element placeholder = doc.createElementNS(XSL_FO, "block");
				placeholder.setTextContent("#TEXTBOX#"); // magic string
				tc2.appendChild(placeholder);

				// TODO here the architecture needs further thought
				if (foRenderer instanceof AbstractFORenderer) {

					((AbstractFORenderer)foRenderer).TEXTBOX_POSTPROCESSING_REQUIRED = true;
						// currently done in the render step in FORendererApacheFOP
						// Maybe it should be done in AbstractFOExporter.postprocess,
						// but then we'd need one of those per renderer vendor?
				} else {
					log.warn("TODO: implement TEXTBOX_POSTPROCESSING_REQUIRED for " + foRenderer.getClass().getName() );
				}

				return true;

			} else if ( mso_position_horizontal.equals("center")) {

				// use 3 cells
				// No, can't do it...
				// best we could do would be to put the textbox centred
				// below the text.
				log.warn("Can't support mso_position_horizontal:center");

				return false;

			} else if ( mso_position_horizontal.equals("right")) {

				Element tcol1 = doc.createElementNS(XSL_FO, "table-column");
				ret.appendChild(tcol1);
				tcol1.setAttribute("column-number", "1"); // optional?
				// width of left cell is ml + boxWidth
				int col1W = Math.round(widthPts-boxWidth);
				tcol1.setAttribute("column-width", col1W +"pt");

				Element tcol2 = doc.createElementNS(XSL_FO, "table-column");
				ret.appendChild(tcol2);
				tcol2.setAttribute("column-number", "2"); // optional?
				// width of left cell is ml + boxWidth
				int col2W = Math.round(boxWidth);
				tcol2.setAttribute("column-width", col2W +"pt");

				Element tbody = doc.createElementNS(XSL_FO, "table-body");
				ret.appendChild(tbody);
				Element trow = doc.createElementNS(XSL_FO, "table-row");
				tbody.appendChild(trow);

				// Cell 1 is the p content proper (which we don't have)
				Element tc1 = doc.createElementNS(XSL_FO, "table-cell");
				trow.appendChild(tc1);
//					tc2.appendChild(
//							doc.createComment("CONTENT GOES HERE"));
				Element placeholder = doc.createElementNS(XSL_FO, "block");
				placeholder.setTextContent("#TEXTBOX#"); // magic string
				tc1.appendChild(placeholder);

				// Cell 2 is the text box
				Element tc2 = doc.createElementNS(XSL_FO, "table-cell");
				trow.appendChild(tc2);
				Element block = doc.createElementNS(XSL_FO, "block");
				setBorders(block); // for now, solid borders, so we can see the text box
				tc2.appendChild(block);
				XmlUtils.treeCopy(childNodes, block);

				// TODO here the architecture needs further thought
				if (foRenderer instanceof AbstractFORenderer) {

					((AbstractFORenderer)foRenderer).TEXTBOX_POSTPROCESSING_REQUIRED = true;
						// currently done in the render step in FORendererApacheFOP
						// Maybe it should be done in AbstractFOExporter.postprocess,
						// but then we'd need one of those per renderer vendor?
				} else {
					log.warn("TODO: implement TEXTBOX_POSTPROCESSING_REQUIRED for " + foRenderer.getClass().getName() );
				}

				return true;

			}
			return false;

		}

	}
		
}
