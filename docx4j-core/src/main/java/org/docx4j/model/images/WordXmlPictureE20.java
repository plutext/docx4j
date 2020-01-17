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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
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

