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
 *
 * @author jharrop
 *
 */
public abstract class FOPictWriterAbstract extends AbstractPictWriter {

	protected static Logger log = LoggerFactory.getLogger(FOPictWriterAbstract.class);

	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	public FOPictWriterAbstract() {
		super();
	}

	public abstract boolean foRendererSupportsFoFloat();

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

			if (w10Wrap.getType()!=null
					&& (w10Wrap.getType().equals(STWrapType.TOP_AND_BOTTOM)
					|| w10Wrap.getType().equals(STWrapType.SQUARE)
					|| w10Wrap.getType().equals(STWrapType.TIGHT)
					|| w10Wrap.getType().equals(STWrapType.THROUGH))) {

				wrap = false;
			}

			// the no wrap page top case
			if (w10Wrap.getAnchory()!=null
					&& (w10Wrap.getAnchory().equals(STVerticalAnchor.PAGE)  )) {

				wrap = false;
			}

		}

		return handleVTextBox(context, modelContent, doc, shape, props, wrap);
	}



	public Node handleVTextBox(AbstractWmlConversionContext context,
			Node modelContent, Document doc,
			org.docx4j.vml.VmlShapeElements shape,
			Map<String, String> props,
			boolean wrap) {

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


		if (wrap) {
			// Floats define a block that is "out of line" in drifts to the top/left/right side of the page;
			// the text *flows around it*.

			log.debug("textbox - wrapped text");

			if (mso_position_vertical_relative==null) {

                if(log.isWarnEnabled()) {
                    try {
                        log.warn(XmlUtils.marshaltoString(shape));
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
				return context.getMessageWriter().message(context,
						"mso_position_vertical_relative==null.  What to do?");

			} else {

				if ( mso_position_vertical_relative.equals("text")) {

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


						if (foRendererSupportsFoFloat()) {

							// TODO not tested; need a renderer which supports float

							String mso_position_horizontal_relative = props.get("mso-position-horizontal-relative");
							String mso_position_horizontal = props.get("mso-position-horizontal"); // eg center


							if (mso_position_horizontal_relative==null) {
								log.warn("No support for mso_position_horizontal_relative==null");
								XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
								return ret;
							} else if (!mso_position_horizontal_relative.equals("text") ) {
								log.warn("No support for mso_position_horizontal_relative==" + mso_position_horizontal_relative.equals("text"));
								XmlUtils.treeCopy(modelContent.getChildNodes(), ret);
								return ret;
							} else {  // mso_position_horizontal_relative.equals("text")


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
						} else {
							//  FOP does not support float, so the best we can do is ...

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
					}

					log.debug(XmlUtils.w3CDomNodeToString(ret));

					return ret;

				}  else if (foRendererSupportsFoFloat()) {

					// NB supportsFloat code is untested.

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

				} else {

					return context.getMessageWriter().message(context,
							"TODO for fo:float INcapable renderer, support no-wrap + mso-position-vertical-relative=" + mso_position_vertical_relative);

				}

			}

		} else {
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
			if (mso_position_vertical_relative==null) {
				if(log.isWarnEnabled()) {
                    log.warn(XmlUtils.marshaltoString(shape));
                }
				return context.getMessageWriter().message(context,
						"mso_position_vertical_relative==null.  What to do?");

			} else {

				if ( mso_position_vertical_relative.equals("text")) {

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

	}

	private void setBorders(Element ret) {
		ret.setAttribute("border-left-style", "solid");
		ret.setAttribute("border-top-style", "solid");
		ret.setAttribute("border-bottom-style", "solid");
		ret.setAttribute("border-right-style", "solid");

	}

	private Map<String, String> getProperties(String s) {

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

	private float parsePtsVal(String pts) {

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

	/**
	 * Wrap case, for renderers which don't support fo:float (eg Apache FOP)
	 *
	 * @param props
	 * @param ret
	 * @param widthPts
	 */
	private void marginTopZeroCase(Map<String, String> props, Element ret, float widthPts ) {

//		mso-position-horizontal:center
//		mso-position-horizontal-relative:text

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

//		mso-position-horizontal:center
//		mso-position-horizontal-relative:text

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
//				tc2.appendChild(
//						doc.createComment("CONTENT GOES HERE"));
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
//				tc2.appendChild(
//						doc.createComment("CONTENT GOES HERE"));
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
