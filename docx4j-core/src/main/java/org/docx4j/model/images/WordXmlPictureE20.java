/*
 *  Copyright 2010, Plutext Pty Ltd.
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
package org.docx4j.model.images;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.relationships.Relationship;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Generate HTML/XSLFO from 
 * 
 * E20 example:
 * 
				<w:drawing>
					<wp:inline distT="0" distB="0" distL="0" distR="0">
						<wp:extent cx="3238500" cy="2362200" />
						<wp:effectExtent l="19050" t="0" r="0" b="0" />
						<wp:docPr id="1" name="Picture 1" />
						<wp:cNvGraphicFramePr>
							<a:graphicFrameLocks xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" 
								noChangeAspect="1" />
						</wp:cNvGraphicFramePr>
						<a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
							<a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
								<pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
									<pic:nvPicPr>
										<pic:cNvPr id="0" name="Picture 1" />
										<pic:cNvPicPr>
											<a:picLocks noChangeAspect="1" noChangeArrowheads="1" />
										</pic:cNvPicPr>
									</pic:nvPicPr>
									<pic:blipFill>
										<a:blip r:embed="rId5" />
:
									</pic:blipFill>
									<pic:spPr bwMode="auto">
:
									</pic:spPr>
								</pic:pic>
							</a:graphicData>
						</a:graphic>
					</wp:inline>
				</w:drawing>
				
			<w:drawing>
					<wp:anchor distT="0" distB="0" distL="114300" distR="114300" simplePos="0" 
					relativeHeight="251662336" behindDoc="0" locked="0" layoutInCell="1" allowOverlap="1">
						<wp:simplePos x="0" y="0" />
						<wp:positionH relativeFrom="column">
							<wp:posOffset>3400425</wp:posOffset>
						</wp:positionH>
						<wp:positionV relativeFrom="paragraph">
							<wp:posOffset>1991360</wp:posOffset>
						</wp:positionV>
						<wp:extent cx="552450" cy="209550" />
						<wp:effectExtent l="38100" t="0" r="19050" b="38100" />
						<wp:wrapTopAndBottom />
						<wp:docPr id="4" name="Picture 1" descr="D:\\stuff\\untitled.bmp" />
						<wp:cNvGraphicFramePr>
							<a:graphicFrameLocks xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" noChangeAspect="1" />
						</wp:cNvGraphicFramePr>
						<a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
							<a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
								<pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
									<pic:nvPicPr>
										<pic:cNvPr id="0" name="Picture 1" descr="D:\\stuff\\untitled.bmp" />
										<pic:cNvPicPr>
											<a:picLocks noChangeAspect="1" noChangeArrowheads="1" />
										</pic:cNvPicPr>
									</pic:nvPicPr>
									<pic:blipFill>
										<a:blip r:embed="rId7" cstate="print">
											<a:clrChange>
												<a:clrFrom>
													<a:srgbClr val="FFFFFF" />
												</a:clrFrom>
												<a:clrTo>
													<a:srgbClr val="FFFFFF">
														<a:alpha val="0" />
													</a:srgbClr>
												</a:clrTo>
											</a:clrChange>
											<a:biLevel thresh="50000" />
										</a:blip>
										<a:srcRect l="35365" t="28689" r="52846" b="65300" />
										<a:stretch>
											<a:fillRect />
										</a:stretch>
									</pic:blipFill>
									<pic:spPr bwMode="auto">
										<a:xfrm rot="21023481">
											<a:off x="0" y="0" />
											<a:ext cx="552450" cy="209550" />
										</a:xfrm>
										<a:prstGeom prst="rect">
											<a:avLst />
										</a:prstGeom>
										<a:noFill />
										<a:ln w="9525">
											<a:noFill />
											<a:miter lim="800000" />
											<a:headEnd />
											<a:tailEnd />
										</a:ln>
									</pic:spPr>
								</pic:pic>
							</a:graphicData>
						</a:graphic>
					</wp:anchor>
				</w:drawing>				
 * 
 *
 */
public class WordXmlPictureE20 extends AbstractWordXmlPicture {
	
	protected static Logger log = LoggerFactory.getLogger(WordXmlPictureE20.class);

	// It'll be one or the other of these
	private Inline inline;
	private Anchor anchor;
    
    private WordXmlPictureE20(WordprocessingMLPackage wmlPackage, Object anchorOrInline) {
    	
    	this.wmlPackage = wmlPackage;
    	
		if (anchorOrInline != null) {
			if (anchorOrInline instanceof Inline) {
				this.inline = (Inline) anchorOrInline;				
			} else if (anchorOrInline instanceof Anchor) {
				this.anchor = (Anchor) anchorOrInline;				
			} else if (anchorOrInline instanceof NodeIterator) { // from Xalan/XSLT
				Node n = ((NodeIterator) anchorOrInline).nextNode();
				if (n != null) {
					Object jaxb = null;
					try {
						Unmarshaller u = Context.jc.createUnmarshaller();
						u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
						jaxb = u.unmarshal(n);
					} catch (JAXBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						if (jaxb instanceof JAXBElement) {

							JAXBElement jb = (JAXBElement) jaxb;
							if (jb.getDeclaredType()
									.getName()
									.equals("org.docx4j.dml.wordprocessingDrawing.Inline")) {
								this.inline = (Inline) jb.getValue();
							} else if (jb
									.getDeclaredType()
									.getName()
									.equals("org.docx4j.dml.wordprocessingDrawing.Anchor")) {
								this.anchor = (Anchor) jb.getValue();
							} else {
								log.error("UNEXPECTED "
										+ XmlUtils.JAXBElementDebug(jb));
								return;
							}
						} else if (jaxb instanceof Inline) {
							this.inline = (Inline) jaxb;
						} else if (jaxb instanceof Anchor) {
							this.anchor = (Anchor) jaxb;
						} else {
							log.error(jaxb.getClass().getName());
							return;
						}
					} catch (ClassCastException e) {
						log.error("Couldn't cast " + jaxb.getClass().getName()
								+ " to Anchor or Inline ");
					}
				}
			}
		}
    	
    }

    
    private static void debug(WordXmlPictureE20 converter) {
    	
    	if (converter.inline!=null)
    		log.error(XmlUtils.marshaltoString(converter.inline, true, true,
    				Context.jc, "foo", "bar", Inline.class ));
    	else 
    		log.error(XmlUtils.marshaltoString(converter.anchor, true, true,
    				Context.jc, "foo", "bar", Anchor.class ));
    }
    
    /**
     * @param wmlPackage
     * @param imageDirPath - images won't be saved if this is not set
     * @param pictureData
     * @param picSize
     * @param picLink
     * @param linkData
     * @return
     */
    private static WordXmlPictureE20 createWordXmlPictureFromE20(
    		WordprocessingMLPackage wmlPackage,
    		ConversionImageHandler imageHandler,
    		Object anchorOrInline,
    		Part sourcePart) {

    	WordXmlPictureE20 converter = new WordXmlPictureE20(wmlPackage, anchorOrInline);
    	
    	converter.readDimensions();
    	converter.readHyperlink();
    	
    	
    	Pic pic = converter.getPic();
    	if (pic==null) {
    		log.error("pic missing!!");
    		debug(converter);
    		return null;    		
    	}
    	
    	if (pic.getBlipFill()==null
    			|| pic.getBlipFill().getBlip()==null) {
    		log.error("blip missing!!");
    		return null;    		    		
    	}
    	
    	CTBlip blip = pic.getBlipFill().getBlip();
    	
    	String imgRelId = blip.getEmbed();
    	if ((imgRelId == null) || (imgRelId.length() == 0)) {
    		imgRelId = blip.getLink();
    	}
    	if ((imgRelId != null) && (imgRelId.length() > 0)) {
    		converter.handleImageRel(imageHandler, imgRelId, sourcePart);
    	}
    	else {
    		log.error("not linked or embedded?!");
    	}

		return converter;
	}
    
    private Pic getPic() {
    	
    	if (inline!=null) {
        	if (inline.getGraphic()==null
        			|| inline.getGraphic().getGraphicData()==null
        			|| inline.getGraphic().getGraphicData().getPic()==null) {
        		log.error("pic missing!!");
        		return null;    		
        	}
        	return inline.getGraphic().getGraphicData().getPic();
    	}

    	if (anchor!=null) {
        	if (anchor.getGraphic()==null
        			|| anchor.getGraphic().getGraphicData()==null
        			|| anchor.getGraphic().getGraphicData().getPic()==null) {
        		log.error("pic missing!!");
        		return null;    		
        	}
        	return anchor.getGraphic().getGraphicData().getPic();
    	}
    	
    	log.error("Anchor and inline both null!");
    	return null;
    }


    
    /** Extension function to create an HTML <img> element
     * from "E2.0 images" 
     *      //w:drawing/wp:inline
     *     |//w:drawing/wp:anchor
     * @param context
     * @param wpInline
     * @return
     */
    public static DocumentFragment createHtmlImgE20(
    		AbstractWmlConversionContext context,
    		Object wpInline) {

    	
    	return createHtmlImgE20(context, wpInline, null);
    }

    /** Extension function to create an HTML <img> element
     * from "E2.0 images" 
     *      //w:drawing/wp:inline
     *     |//w:drawing/wp:anchor
     * with a custom ID.
     * @param context
     * @param wpInline
     * @return
     */
    public static DocumentFragment createHtmlImgE20(
    		AbstractWmlConversionContext context,
    		Object wpInline, String id) {

    	Part sourcePart = context.getCurrentPart();
    	
    	WordXmlPictureE20 converter = createWordXmlPictureFromE20(context.getWmlPackage(),
        		 context.getImageHandler(), wpInline, sourcePart );
    	
    	if (id!=null) {
    		converter.setID(id);
    	}
    	
    	return getHtmlDocumentFragment(converter);
    }
    
    /** Extension function to create an XSL FO <fo:external-graphic> element
     * from "E2.0 images" 
     *      //w:drawing/wp:inline
     *     |//w:drawing/wp:anchor
     * @param wmlPackage
     * @param imageDirPath
     * @param pictureData
     * @param picSize
     * @param picLink
     * @param linkData
     * @return
     */
    public static DocumentFragment createXslFoImgE20(
    		AbstractWmlConversionContext context,
    		NodeIterator wpInline) {

    	Part sourcePart = context.getCurrentPart();
    	
    	WordXmlPictureE20 converter = createWordXmlPictureFromE20(context.getWmlPackage(),
        		 context.getImageHandler(), wpInline, sourcePart);
    	
        Document d = converter.createXslFoImageElement();
        converter.stampAnchorHints(d, context);

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
    }

    /**
     * for XSLFOExporterNonXSLT
     * @since 3.0
     */
    public static DocumentFragment createXslFoImgE20(
    		AbstractWmlConversionContext context,
    		Object wpInline) {

    	Part sourcePart = context.getCurrentPart();
    	
    	WordXmlPictureE20 converter = createWordXmlPictureFromE20(context.getWmlPackage(),
        		 context.getImageHandler(), wpInline, sourcePart);
    	
        Document d = converter.createXslFoImageElement();
        converter.stampAnchorHints(d, context);

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
    }
    
    // ---------------------------------------------------------------- anchored pictures

    /** Hint attributes describing a wp:anchor, for export-fo's WordLayoutFixups
     *  (which turns them into fo:float / block-containers and removes them).
     *  Lengths are in points; x is measured from the column's left edge, y is
     *  "p:<pt>" from the anchor paragraph's top or "page:<pt>" from the page top.
     *  @since 17.0.5 */
    public static final String HINT_ANCHOR = "docx4j-anchor";
    public static final String HINT_ANCHOR_W = "docx4j-anchor-w";
    public static final String HINT_ANCHOR_H = "docx4j-anchor-h";
    public static final String HINT_ANCHOR_X = "docx4j-anchor-x";
    public static final String HINT_ANCHOR_Y = "docx4j-anchor-y";
    public static final String HINT_ANCHOR_DIST = "docx4j-anchor-dist";
    public static final String HINT_ANCHOR_BEHIND = "docx4j-anchor-behind";
    public static final String HINT_ANCHOR_COL = "docx4j-anchor-col";
    public static final String HINT_ANCHOR_ML = "docx4j-anchor-ml";

    /** The property WordLayoutFixups (docx4j-export-fo) is enabled by. */
    private static final String WORD_LAYOUT_FIXUPS_PROPERTY = "docx4j.convert.out.fo.wordLayoutFixups";

    private static final double EMU_PER_PT = 12700;

    /**
     * Word places an anchored picture relative to its paragraph, column or page
     * and wraps text around it; the FO for that (a side float, a block-container,
     * an absolutely positioned block-container) can only be built once the
     * paragraph's fo:block exists, so the anchor's geometry travels on the
     * fo:external-graphic as hint attributes for export-fo's WordLayoutFixups.
     *
     * @since 17.0.5
     */
    void stampAnchorHints(Document d, AbstractWmlConversionContext context) {
    	if (d==null || d.getDocumentElement()==null) return;
    	stampAnchorHints(d.getDocumentElement(), anchor, context);
    }

    /**
     * As above, for any element standing in for an anchored object: since 17.0.5
     * a DrawingML shape's text box (wps:wsp/wps:txbx) is positioned this way too.
     *
     * @since 17.0.5
     */
    public static void stampAnchorHints(org.w3c.dom.Element g, Anchor anchor, AbstractWmlConversionContext context) {
    	if (anchor==null || g==null || context==null) return;
    	if (!org.docx4j.Docx4jProperties.getProperty(WORD_LAYOUT_FIXUPS_PROPERTY, true)) return;
    	try {
	    	org.docx4j.model.structure.PageDimensions pd = context.getSections().getCurrentSection().getPageDimensions();

	    	String kind;
	    	if (anchor.getWrapTopAndBottom()!=null) kind = "topAndBottom";
	    	else if (anchor.getWrapSquare()!=null || anchor.getWrapTight()!=null || anchor.getWrapThrough()!=null) kind = "square";
	    	else kind = "none";

	    	CTPositiveSize2D extent = anchor.getExtent();
	    	if (extent==null) return;
	    	double w = extent.getCx() / EMU_PER_PT;
	    	double h = extent.getCy() / EMU_PER_PT;

	    	double colW = pd.getWritableWidthTwips() / 20d;
	    	double pageW = pd.getPgSz().getW().doubleValue() / 20d;
	    	double pageH = pd.getPgSz().getH().doubleValue() / 20d;
	    	double mL = pd.getPgMar().getLeft().doubleValue() / 20d;
	    	double mR = pd.getPgMar().getRight().doubleValue() / 20d;
	    	double mT = pd.getPgMar().getTop().doubleValue() / 20d;
	    	double mB = pd.getPgMar().getBottom().doubleValue() / 20d;

	    	// horizontal: the reference box in column coordinates, then align or offset
	    	org.docx4j.dml.wordprocessingDrawing.CTPosH ph = anchor.getPositionH();
	    	double refLeft = 0, refRight = colW;
	    	org.docx4j.dml.wordprocessingDrawing.STRelFromH relH = ph==null ? null : ph.getRelativeFrom();
	    	if (relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.PAGE) {
	    		refLeft = -mL; refRight = pageW - mL;
	    	} else if (relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.LEFT_MARGIN
	    			|| relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.INSIDE_MARGIN) {
	    		refLeft = -mL; refRight = 0;
	    	} else if (relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.RIGHT_MARGIN
	    			|| relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.OUTSIDE_MARGIN) {
	    		refLeft = colW; refRight = colW + mR;
	    	} // margin, column, character: the column (character: from the column start, an approximation)
	    	double x = refLeft;
	    	if (ph!=null && ph.getAlign()!=null) {
	    		switch (ph.getAlign()) {
	    			case CENTER: x = (refLeft + refRight - w) / 2; break;
	    			case RIGHT: case OUTSIDE: x = refRight - w; break;
	    			default: x = refLeft; // left, inside
	    		}
	    	} else if (ph!=null && ph.getPosOffset()!=null) {
	    		x = refLeft + ph.getPosOffset() / EMU_PER_PT;
	    	}

	    	// vertical: from the paragraph's top, or from the page's top
	    	org.docx4j.dml.wordprocessingDrawing.CTPosV pv = anchor.getPositionV();
	    	org.docx4j.dml.wordprocessingDrawing.STRelFromV relV = pv==null ? null : pv.getRelativeFrom();
	    	double off = (pv!=null && pv.getPosOffset()!=null) ? pv.getPosOffset() / EMU_PER_PT : 0;
	    	String y;
	    	if (relV==null || relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.PARAGRAPH
	    			|| relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.LINE) {
	    		y = "p:" + fmt(off); // line: from the paragraph's top, an approximation
	    	} else {
	    		double refTop = 0, refBottom = pageH;
	    		if (relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.MARGIN) {
	    			refTop = mT; refBottom = pageH - mB;
	    		} else if (relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.TOP_MARGIN
	    				|| relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.INSIDE_MARGIN) {
	    			refTop = 0; refBottom = mT;
	    		} else if (relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.BOTTOM_MARGIN
	    				|| relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.OUTSIDE_MARGIN) {
	    			refTop = pageH - mB; refBottom = pageH;
	    		}
	    		double py = refTop + off;
	    		if (pv.getAlign()!=null) {
	    			switch (pv.getAlign()) {
	    				case CENTER: py = (refTop + refBottom - h) / 2; break;
	    				case BOTTOM: case OUTSIDE: py = refBottom - h; break;
	    				default: py = refTop; // top, inside
	    			}
	    		}
	    		y = "page:" + fmt(py);
	    	}

	    	g.setAttribute(HINT_ANCHOR, kind);
	    	g.setAttribute(HINT_ANCHOR_W, fmt(w));
	    	g.setAttribute(HINT_ANCHOR_H, fmt(h));
	    	g.setAttribute(HINT_ANCHOR_X, fmt(x));
	    	g.setAttribute(HINT_ANCHOR_Y, y);
	    	g.setAttribute(HINT_ANCHOR_DIST, fmt(emu(anchor.getDistL())) + " " + fmt(emu(anchor.getDistR()))
	    			+ " " + fmt(emu(anchor.getDistT())) + " " + fmt(emu(anchor.getDistB())));
	    	if (anchor.isBehindDoc()) g.setAttribute(HINT_ANCHOR_BEHIND, "1");
	    	g.setAttribute(HINT_ANCHOR_COL, fmt(colW));
	    	g.setAttribute(HINT_ANCHOR_ML, fmt(mL));
    	} catch (Exception e) {
    		log.warn("Anchored picture left in the flow: " + e.getMessage(), e);
    	}
    }

    private static double emu(Long v) {
    	return v==null ? 0 : v / EMU_PER_PT;
    }

    private static String fmt(double pt) {
    	String s = String.format(java.util.Locale.ROOT, "%.2f", pt);
    	if (s.endsWith("0")) s = s.substring(0, s.length()-1);
    	if (s.endsWith("0")) s = s.substring(0, s.length()-1);
    	if (s.endsWith(".")) s = s.substring(0, s.length()-1);
    	if (s.equals("-0")) s = "0";
    	return s;
    }

    private void readDimensions() {
    	CTPositiveSize2D size2d = getExtent();
    	if (size2d==null) {
    		log.warn("wp:inline/wp:extent missing!");
    		return;
    	}
    	dimensions = new Dimensions();
    	if (size2d.getCx()!=0) {
    		dimensions.width= (int) size2d.getCx() / extentToPixelConversionFactor;
    		dimensions.widthUnit = "px";
    	}
    	if (size2d.getCy()!=0) {
    		dimensions.height= (int) size2d.getCy() / extentToPixelConversionFactor;
    		dimensions.heightUnit = "px";
    	}    	
    }
    
    
    private CTPositiveSize2D getExtent() {
    	
    	if (inline!=null) {
        	return inline.getExtent();
    	}

    	if (anchor!=null) {
        	return anchor.getExtent();
    	}
    	
    	log.error("Anchor and inline both null!");
    	return null;
    }
    
    
    private final int extentToPixelConversionFactor = 12700;
    
    private void readHyperlink() {
    	if (getDocPr()!=null
    			&& getDocPr().getHlinkClick()!=null) {
    		
    		String linkRelId = getDocPr().getHlinkClick().getId();
    		
            if ( linkRelId!=null && !linkRelId.equals("") ) 
            {
            	Relationship rel = wmlPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(linkRelId);
            	
            	if (rel.getTargetMode() == null
            			|| rel.getTargetMode().equals("Internal") ) {
            		
            		setHlinkReference("TODO - save this object");
            	} else {
                    setHlinkReference( rel.getTarget() );            	
            	}
            }
            
            targetFrame = getDocPr().getHlinkClick().getTgtFrame();
            tooltip = getDocPr().getHlinkClick().getTooltip();
    	}
    }

    private CTNonVisualDrawingProps getDocPr() {
    	
    	if (inline!=null) {
        	if (inline.getDocPr()==null) {
        		log.error("DocPr missing!!");
        		return null;    		
        	}
        	return inline.getDocPr();
    	}

    	if (anchor!=null) {
        	if (anchor.getDocPr()==null) {
        		log.error("DocPr missing!!");
        		return null;    		
        	}
        	return anchor.getDocPr();
    	}
    	
    	log.error("Anchor and inline both null!");
    	return null;
    }
    

//    private byte[] data;
//    // / <summary>
//    // / The decoded data from the corresponding 'w:bindata'
//    /// node of the Word Document.
//    /// </summary>
//    /// <remarks>
//    /// This property is set by the conversion process.
//    /// </remarks>
//    /// <value>
//    /// </value>
//    /// <id guid="130108bf-d980-4753-b674-4d489acf485c" />
//    /// <owner alias="ROrleth" />
//    public byte[] getData() {
//		return this.data;
//	}
//
//	public void setData(byte[] value) {
//		this.data = value;
//	}
//
//	private String id;
//
//	// / <summary>
//	// / The identifier of the picture unique only within the scope of
//	// / the Word Document.
//	// / </summary>
//	// / <value>
//	// / </value>
//	// / <id guid="e0d6cf93-79f7-4a38-884c-6b494b244664" />
//	// / <owner alias="ROrleth" />
//	public String getID() {
//		return this.id;
//	}
//
//	public void setID(String value) {
//		this.id = value;
//	}
//
//    private String src;
//    public String getSrc() {
//		return this.src;
//	}
//	public void setSrc(String value) {
//		this.src = value;
//	}
//
//	
//	
//    private String style;
//    // / <summary>
//    // / The attribute of the v:shape node which maps to the
//    /// 'style' attribute of and HTML 'img' tag.
//    /// </summary>
//    /// <value>
//    /// </value>
//    /// <id guid="700b62da-d914-4a40-aa96-1437d2b314e1" />
//    /// <owner alias="ROrleth" />
//    public String getStyle() {
//		return this.style;
//	}
//
//	public void setStyle(String value) {
//		this.style = value;
//	}
//
//    private String pType;
//    // / <summary>
//    // / The type of the picture as specified by the attribute of the
//	// v:shape node
//    /// within the Word Document.
//    /// </summary>
//    /// <remarks>
//    /// This value is used as an identifier for a v:type node, which used to specify
//    /// properties of the picture within the Word Document.
//    /// </remarks>
//    /// <value>
//    /// </value>
//    /// <id guid="78bf5c95-1d55-423c-bc34-92d926203e83" />
//    /// <owner alias="ROrleth" />
//    public String getPType() {
//		return this.pType;
//	}
//
//	public void setPType(String value) {
//		this.pType = value;
//	}
}

