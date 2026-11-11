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

	/** The XSLT's node of the wp:inline or wp:anchor, whose unmarshalled copy has no parent: where a picture's
	 *  paragraph is read from (effectClip), during the extension call that made this.  @since 17.3.7 */
	private Node inlineNode;
    
    private WordXmlPictureE20(WordprocessingMLPackage wmlPackage, Object anchorOrInline) {
    	
    	this.wmlPackage = wmlPackage;
    	
		if (anchorOrInline != null) {
			if (anchorOrInline instanceof Inline) {
				this.inline = (Inline) anchorOrInline;				
			} else if (anchorOrInline instanceof Anchor) {
				this.anchor = (Anchor) anchorOrInline;				
			} else if (anchorOrInline instanceof NodeIterator) { // from Xalan/XSLT
				Node n = ((NodeIterator) anchorOrInline).nextNode();
				this.inlineNode = n;
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
    	return createWordXmlPictureFromE20(wmlPackage, imageHandler, anchorOrInline, sourcePart, false);
    }

    /** @param forXslFo true for the XSL-FO exporters: a Windows metafile picture is
     *  represented differently in FO than in HTML (CR-011).  @since 17.1.0 */
    private static WordXmlPictureE20 createWordXmlPictureFromE20(
    		WordprocessingMLPackage wmlPackage,
    		ConversionImageHandler imageHandler,
    		Object anchorOrInline,
    		Part sourcePart,
    		boolean forXslFo) {

    	WordXmlPictureE20 converter = new WordXmlPictureE20(wmlPackage, anchorOrInline);
    	converter.forXslFo = forXslFo;

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
    	converter.crop = PictureCrop.of(pic.getBlipFill().getSrcRect()); // CR-029
    	
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
        		 context.getImageHandler(), wpInline, sourcePart, true);
    	
        Document d = converter.withEffects(converter.createXslFoImageElement());
        converter.stampAnchorHints(d, context);
        converter.stampEffectExtent(d);

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
        		 context.getImageHandler(), wpInline, sourcePart, true);
    	
        Document d = converter.withEffects(converter.createXslFoImageElement());
        converter.stampAnchorHints(d, context);
        converter.stampEffectExtent(d);

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
    }
    
    // ---------------------------------------------------------------- effects (CR-081)

    /**
     * The picture in its frame with its effects (pic:spPr: its outline, a:prstGeom or a:custGeom, which clips it; the
     * fill behind it; its line, a:ln, wholly outside it; a:effectLst: outer and inner shadow, glow, soft edge,
     * reflection), as Word draws them (CR-081 phases 1 and 2): an fo:instream-foreign-object of the picture's frame
     * holding one SVG, the effect pictures and the frame round the picture itself, as a data: URI of what the image
     * handler wrote for it (so it is resampled, cropped and re-encoded as one with no effects is, and a JPEG passes
     * through to the PDF).  d as it is where the picture is its bitmap alone (a rectangle with no line, no effects, and
     * no fill behind or an opaque bitmap), or it can't be drawn: no ShapeDrawer (docx4j-chart-render) on the
     * classpath, a metafile.  The anchor hints and the effect extent then go on the instream-foreign-object as on the
     * external-graphic, so the picture is placed and wrapped as before.  Until 17.3.7 a picture's effects, outline and
     * line weren't drawn.  @since 17.3.7
     */
    Document withEffects(Document d) {
    	if (!forXslFo || metaFileSvg != null || d == null || d.getDocumentElement() == null || dimensions == null) return d;
    	Pic pic = getPic();
    	if (!PictureEffects.framed(pic) && !PictureEffects.fillBehind(pic)) return d;
    	double w = lengthInPoints(dimensions.width, dimensions.widthUnit);
    	double h = lengthInPoints(dimensions.height, dimensions.heightUnit);
    	try {
    		Document doc = XmlUtils.getNewDocumentBuilder().newDocument();
    		// the picture as the image handler wrote it (resampled, cropped and re-encoded as CR-029 has it), as a data:
    		// URI (the FOP fork admits only those in an SVG); else the part's own bytes, cropped in the SVG
    		String href = PictureEffects.dataUri(getSrc());
    		org.w3c.dom.Element svg = PictureEffects.svg(doc, wmlPackage, pic, w, h, picturePart, crop, href, href != null && isCropApplied(),
    				effectClip(), "p-");
    		if (svg == null) return d;
    		svg.setAttribute("width", length(w) + "pt");
    		svg.setAttribute("height", length(h) + "pt");
    		org.w3c.dom.Element ifo = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:instream-foreign-object");
    		ifo.setAttribute("content-width", length(w) + "pt");
    		ifo.setAttribute("content-height", length(h) + "pt");
    		ifo.setAttribute("scaling", "non-uniform");
    		ifo.appendChild(svg);
    		doc.appendChild(ifo);
    		return doc;
    	} catch (Exception e) {
    		log.warn("A picture's effects couldn't be drawn: " + e);
    		return d;
    	}
    }

    /**
     * The picture in its frame with its effects in HTML, in the img's place (as {@link #withEffects(Document)} has
     * them): an inline svg of the picture's frame (its points as CSS pixels, 4/3 a point: CR-080 D14), its overflow
     * visible, labelled as the img would be, its ids the picture's own (its wp:docPr id); an inline picture's effect
     * extent round it as margin, as the PDF output pads it.  @since 17.3.7
     */
    @Override
    protected org.w3c.dom.Element createHtmlEffectsElement(Document document) {
    	if (forXslFo || metaFileSvg != null || dimensions == null) return null;
    	Pic pic = getPic();
    	if (!PictureEffects.framed(pic) && !PictureEffects.fillBehind(pic)) return null;
    	double w = lengthInPoints(dimensions.width, dimensions.widthUnit);
    	double h = lengthInPoints(dimensions.height, dimensions.heightUnit);
    	String href = getSrc() == null || getSrc().isEmpty() ? null : getSrc();
    	org.w3c.dom.Element svg = PictureEffects.svg(document, wmlPackage, pic, w, h, picturePart, crop, href, false, effectClip(), htmlIds());
    	if (svg == null) return null;
    	svg.setAttribute("width", Long.toString(cssPixels(w, "pt")));
    	svg.setAttribute("height", Long.toString(cssPixels(h, "pt")));
    	if (id != null && !id.isEmpty()) svg.setAttribute("id", id);
    	svg.setAttribute("role", "img");
    	svg.setAttribute("aria-label", label());
    	StringBuilder css = new StringBuilder("overflow:visible");
    	if (inline != null && inline.getEffectExtent() != null) {
    		org.docx4j.dml.wordprocessingDrawing.CTEffectExtent e = inline.getEffectExtent();
    		css.append(";margin:").append(cssPixels(Math.max(0, e.getT()) / EMU_PER_PT, "pt")).append("px ")
    			.append(cssPixels(Math.max(0, e.getR()) / EMU_PER_PT, "pt")).append("px ")
    			.append(cssPixels(Math.max(0, e.getB()) / EMU_PER_PT, "pt")).append("px ")
    			.append(cssPixels(Math.max(0, e.getL()) / EMU_PER_PT, "pt")).append("px");
    	}
    	if (style != null && !style.isEmpty()) css.append(';').append(style);
    	svg.setAttribute("style", css.toString());
    	return svg;
    }

    /** The prefix of the picture's SVG ids in an HTML page, from its wp:docPr id (as a shape's: HtmlShapes) */
    private String htmlIds() {
    	org.docx4j.dml.CTNonVisualDrawingProps pr = inline != null ? inline.getDocPr() : anchor != null ? anchor.getDocPr() : null;
    	return "docx4j-picture-" + (pr == null ? "0" : Long.toString(pr.getId())) + "-";
    }

    /** Whether a drawing (its wp:inline's DOM node, as the XSLT gives it) is all its paragraph shows; null where it is
     *  in no paragraph */
    static Boolean aloneInParagraph(Node inlineNode) {
    	final String w = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    	Node drawing = inlineNode.getParentNode();
    	Node p = drawing;
    	while (p != null && !(w.equals(p.getNamespaceURI()) && "p".equals(p.getLocalName()))) p = p.getParentNode();
    	if (p == null) return null;
    	return !showsOtherThan(p, drawing, w);
    }

    private static boolean showsOtherThan(Node n, Node drawing, String w) {
    	for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
    		if (c == drawing || c.getNodeType() != Node.ELEMENT_NODE) continue;
    		String name = c.getLocalName();
    		if (w.equals(c.getNamespaceURI())) {
    			if ("t".equals(name) || "delText".equals(name)) {
    				if ("t".equals(name) && !c.getTextContent().trim().isEmpty()) return true;
    				continue;
    			}
    			if ("drawing".equals(name) || "pict".equals(name) || "object".equals(name) || "sym".equals(name) || "tab".equals(name)
    					|| "br".equals(name) || "cr".equals(name)) return true;
    			if ("pPr".equals(name) || "rPr".equals(name) || "del".equals(name)) continue;
    		}
    		if (showsOtherThan(c, drawing, w)) return true;
    	}
    	return false;
    }

    /** As {@link #aloneInParagraph(Node)}, from the JAXB parents of a wp:inline (the visitor's) */
    static Boolean aloneInParagraph(Inline inline) {
    	Object drawing = inline.getParent();
    	Object p = drawing;
    	while (p instanceof org.jvnet.jaxb.lang.Child && !(p instanceof org.docx4j.wml.P)) p = ((org.jvnet.jaxb.lang.Child) p).getParent();
    	if (!(p instanceof org.docx4j.wml.P) || drawing == null) return null;
    	return !showsOtherThan(((org.docx4j.wml.P) p).getContent(), drawing);
    }

    private static boolean showsOtherThan(java.util.List<Object> content, Object drawing) {
    	for (Object o : content) {
    		Object v = XmlUtils.unwrap(o);
    		if (v == drawing || v instanceof org.docx4j.wml.PPr || v instanceof org.docx4j.wml.RPr || v instanceof org.docx4j.wml.RunDel) continue;
    		if (v instanceof org.docx4j.wml.Text) {
    			String t = ((org.docx4j.wml.Text) v).getValue();
    			if (t != null && !t.trim().isEmpty() && !(o instanceof JAXBElement && "delText".equals(((JAXBElement<?>) o).getName().getLocalPart()))) return true;
    			continue;
    		}
    		if (v instanceof org.docx4j.wml.Drawing || v instanceof org.docx4j.wml.Pict || v instanceof org.docx4j.wml.CTObject
    				|| v instanceof org.docx4j.wml.R.Sym || v instanceof org.docx4j.wml.R.Tab || v instanceof org.docx4j.wml.Br
    				|| v instanceof org.docx4j.wml.R.Cr) return true;
    		if (v instanceof org.docx4j.wml.ContentAccessor && showsOtherThan(((org.docx4j.wml.ContentAccessor) v).getContent(), drawing)) return true;
    		if (v instanceof org.docx4j.wml.SdtElement && ((org.docx4j.wml.SdtElement) v).getSdtContent() != null
    				&& showsOtherThan(((org.docx4j.wml.SdtElement) v).getSdtContent().getContent(), drawing)) return true;
    	}
    	return false;
    }

    /** The picture's label: its alt text, else its wp:docPr's description, else its name, as a shape's (HtmlShapes) */
    private String label() {
    	if (alt != null && !alt.trim().isEmpty()) return alt.trim();
    	org.docx4j.dml.CTNonVisualDrawingProps pr = inline != null ? inline.getDocPr() : anchor != null ? anchor.getDocPr() : null;
    	if (pr != null && pr.getDescr() != null && !pr.getDescr().trim().isEmpty()) return pr.getDescr().trim();
    	if (pr != null && pr.getName() != null && !pr.getName().trim().isEmpty()) return pr.getName().trim();
    	return "Picture";
    }

    /**
     * Where an inline picture's effects are cut: Word cuts them at its paragraph, across the page (CR-081 phase 0
     * findings §3; phase 1's fx91: a picture in a paragraph that goes on below it keeps a shadow 12.6pt below it and a
     * reflection 48pt, with no effect extent).  docx4j can't cut at a paragraph's box (XSL-FO clips an inline object's
     * overflow only at its own viewport: Enterprise CR-001 §6.6 item 65, which this works round), so it cuts only a
     * picture that is all its paragraph shows, whose line is the paragraph: at the effect extent's top and bottom (Word's line has
     * the text's descent below, 2.3pt at 12pt, and some of the paragraph's space before above: fxr50-24's reflection
     * shows 27.9pt more above the extent than docx4j's).  Null, no cut: an anchored picture, as Word's, and an inline
     * one sharing its paragraph.
     */
    private double[] effectClip() {
    	if (inline == null) return null;
    	Boolean alone = null;
    	try {
    		alone = inlineNode != null ? aloneInParagraph(inlineNode) : aloneInParagraph(inline);
    	} catch (RuntimeException e) {
    		log.debug("The picture's paragraph wasn't read: " + e);
    	}
    	if (!Boolean.TRUE.equals(alone)) return null;
    	org.docx4j.dml.wordprocessingDrawing.CTEffectExtent e = inline.getEffectExtent();
    	if (e == null) return new double[] { 0, 0 };
    	return new double[] { Math.max(0, e.getT()) / EMU_PER_PT, Math.max(0, e.getB()) / EMU_PER_PT };
    }

    /**
     * Word lays an inline picture out at its extent plus its effect extent (wp:effectExtent: the margin
     * its shadow, glow or soft edge needs on each side, ECMA-376 20.4.2.6), so the line holding it is
     * taller by the top and bottom extents and the text after it starts further right by the left and
     * right ones. Measured 2026-10-29 on the Enterprise manual: an 87.3pt screenshot with 4.5pt on every
     * side sits in a 98.3pt line in Word's PDF (87.3 + 9 + the paragraph's 1.15 leading), ours was 88.4,
     * and the page below it held two lines more than Word's. The extents go on the fo:external-graphic
     * (or fo:instream-foreign-object) as padding, which FOP adds to the viewport; the picture line's box
     * (WordLayoutFixups.pictureHeightPt) counts the padding. An anchored picture's effect extent is left
     * to the anchor's own wrap geometry.
     *
     * <p>The extents are added to any padding the picture has already: a crop which moves an edge out
     * (CR-029) pads the picture inside its frame, and the effect extent lies outside the frame.  Until
     * 17.3.7 the effect extent replaced that padding, so such a picture was drawn at its frame's top left
     * and its frame shrank to the bitmap's part (15_en-US_sdt_tbl_469: a 199pt band above a picture lost;
     * CR-081 phase 2 findings §5).</p>
     *
     * @since 17.3.5
     */
    void stampEffectExtent(Document d) {
    	if (d==null || d.getDocumentElement()==null || inline==null || inline.getEffectExtent()==null) return;
    	org.docx4j.dml.wordprocessingDrawing.CTEffectExtent e = inline.getEffectExtent();
    	org.w3c.dom.Element g = d.getDocumentElement();
    	addPadding(g, "padding-top", e.getT());
    	addPadding(g, "padding-bottom", e.getB());
    	addPadding(g, "padding-left", e.getL());
    	addPadding(g, "padding-right", e.getR());
    }

    /** The side's padding grown by emu (where positive): added to a padding in points it has already */
    private static void addPadding(org.w3c.dom.Element g, String side, long emu) {
    	if (emu <= 0) return;
    	double pt = emu / EMU_PER_PT;
    	String had = g.getAttribute(side).trim();
    	if (had.endsWith("pt")) {
    		try {
    			pt += Double.parseDouble(had.substring(0, had.length() - 2));
    		} catch (NumberFormatException ex) {
    			log.debug("A picture's " + side + " not read: " + had);
    		}
    	}
    	g.setAttribute(side, fmt(pt) + "pt");
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
    /** Where the object goes when its anchor is in a table cell and Word lays it out in the
     *  cell: "&lt;h&gt; &lt;v&gt;", h being "edge:" (from the cell's left edge) or "text:" (from
     *  its text, inside the left cell margin) followed by the offset in pt or "@" and the
     *  alignment, v "top:" and the offset in pt from the cell's top; "-" for a reference
     *  Word's reading in a cell is not known for, which keeps the column/page reading.
     *  @since 17.3.3 */
    public static final String HINT_ANCHOR_CELL = "docx4j-anchor-cell";
    /** A square, tight or through wrap's side: "both", "left", "right" or "largest" (the
     *  wrapText of wp:wrapSquare and its kin).  @since 17.3.3 */
    public static final String HINT_ANCHOR_SIDES = "docx4j-anchor-sides";
    /** Which of the three wraps text may run beside: "square", "tight" or "through" (the anchor's
     *  wp:wrapSquare, wp:wrapTight or wp:wrapThrough; HINT_ANCHOR says "square" for all three).
     *  @since 17.3.7 */
    public static final String HINT_ANCHOR_WRAP = "docx4j-anchor-wrap";
    /** On an object Word lays out in a table cell and wraps the cell's text round (square,
     *  tight or through): how far below the object's foot the cell's text reaches, in pt -
     *  its distB in mode 15, 0 below.  Word grows the row to that.  @since 17.3.3 */
    public static final String HINT_ANCHOR_CELL_FOOT = "docx4j-anchor-cell-foot";
    /** A square or top-and-bottom wrap's effect extent (wp:effectExtent), "l t r b" in pt, which Word
     *  adds to the object's box before its distances when it wraps text round it; tight and
     *  through wrap their polygon in the extent's box, without it (CR-080 phase 4's wrap probes,
     *  2026-11-10: the text beside a 144pt picture at 226.18 with r = 1pt, at 225.22 with none
     *  or with wrapTight; at 228.1 with 3pt).  Only where some side is not 0.  @since 17.3.7 */
    public static final String HINT_ANCHOR_EFFECT = "docx4j-anchor-effect";

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
	    	/* The effect extent is part of the object's box (ECMA-376 20.4.2.6: "added to each edge"),
	    	 * which an alignment places: measured on CR-080 phase 4's wrap probes (Word 16.0.20326),
	    	 * a 144pt picture or shape with r = 1pt aligned right has its right edge 1pt in from the
	    	 * column's (522.3 on a column ending at 523.3), and centred, 0.5pt left of the extent
	    	 * centred (225.15 for 225.65).  An offset places the extent itself (posOffset 0 with
	    	 * l = 3pt: the picture at the column's edge).  The vertical alignments are taken alike
	    	 * (unmeasured).  @since 17.3.7 */
	    	org.docx4j.dml.wordprocessingDrawing.CTEffectExtent ee = anchor.getEffectExtent();
	    	double eL = ee==null ? 0 : Math.max(0, ee.getL() / EMU_PER_PT);
	    	double eT = ee==null ? 0 : Math.max(0, ee.getT() / EMU_PER_PT);
	    	double eR = ee==null ? 0 : Math.max(0, ee.getR() / EMU_PER_PT);
	    	double eB = ee==null ? 0 : Math.max(0, ee.getB() / EMU_PER_PT);

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
	    			case CENTER: x = (refLeft + refRight - (eL + w + eR)) / 2 + eL; break;
	    			case RIGHT: case OUTSIDE: x = refRight - w - eR; break;
	    			default: x = refLeft + eL; // left, inside
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
	    				case CENTER: py = (refTop + refBottom - (eT + h + eB)) / 2 + eT; break;
	    				case BOTTOM: case OUTSIDE: py = refBottom - h - eB; break;
	    				default: py = refTop + eT; // top, inside
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
	    	if ((anchor.getWrapSquare()!=null || anchor.getWrapTopAndBottom()!=null) && eL + eT + eR + eB > 0) {
	    		g.setAttribute(HINT_ANCHOR_EFFECT, fmt(eL) + " " + fmt(eT) + " " + fmt(eR) + " " + fmt(eB));
	    	}
	    	org.docx4j.dml.wordprocessingDrawing.STWrapText wrapText = anchor.getWrapSquare()!=null ? anchor.getWrapSquare().getWrapText()
	    			: anchor.getWrapTight()!=null ? anchor.getWrapTight().getWrapText()
	    			: anchor.getWrapThrough()!=null ? anchor.getWrapThrough().getWrapText() : null;
	    	if (wrapText!=null) {
	    		g.setAttribute(HINT_ANCHOR_SIDES, wrapText==org.docx4j.dml.wordprocessingDrawing.STWrapText.BOTH_SIDES
	    				? "both" : wrapText.value());
	    	}
	    	if ("square".equals(kind)) {
	    		g.setAttribute(HINT_ANCHOR_WRAP, anchor.getWrapSquare()!=null ? "square" : anchor.getWrapTight()!=null ? "tight" : "through");
	    	}
	    	g.setAttribute(HINT_ANCHOR_COL, fmt(colW));
	    	g.setAttribute(HINT_ANCHOR_ML, fmt(mL));
	    	/* In a table cell Word measures the position from the cell: "page" from the cell's
	    	 * left edge and top, "margin" and "column" from its text and top.  Measured on Word's
	    	 * own PDFs: a 125.7pt wrapSquare picture at margin 30.3 / 6.5pt in a cell whose text
	    	 * starts at x=14.4 and whose top is at 114.26 is drawn at (44.7, 120.71); a header
	    	 * logo at page 6.8 / 3.1pt in a cell whose edge is at 47.8 and top at 23.0, at
	    	 * (54.75, 26.1); and in mode 15 the same holds where layoutInCell is 0 - a
	    	 * full-page picture at page -421.55 / -98pt in a cell whose edge is at 386.8 and top
	    	 * at 98 is drawn at (-34.75, 0), and a text box at column 54.15 / page 25.8pt at
	    	 * 131.55, 25.8 below its cell's top.  Word 2013 treats every anchor in a cell as
	    	 * laid out in it; below mode 15 the attribute is taken at its word (not measured).
	    	 * The fixups apply it where the anchor turns out to be in a cell.  @since 17.3.3 */
	    	int mode = org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart
	    			.getCompatibilityMode(context.getWmlPackage());
	    	if (anchor.isLayoutInCell() || mode >= 15) {
	    		String cellH = "-", cellV = "-";
	    		if (relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.PAGE
	    				|| relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.MARGIN
	    				|| relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.COLUMN
	    				|| relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.CHARACTER || relH==null) {
	    			String ref = relH==org.docx4j.dml.wordprocessingDrawing.STRelFromH.PAGE ? "edge:" : "text:";
	    			if (ph!=null && ph.getAlign()!=null) cellH = ref + "@" + ph.getAlign().value();
	    			else cellH = ref + fmt(ph!=null && ph.getPosOffset()!=null ? ph.getPosOffset() / EMU_PER_PT : 0);
	    		}
	    		if (relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.PAGE
	    				|| relV==org.docx4j.dml.wordprocessingDrawing.STRelFromV.MARGIN) {
	    			// an alignment other than top needs the cell's height, which is not known
	    			if (pv.getAlign()==null) cellV = "top:" + fmt(off);
	    			else if (pv.getAlign()==org.docx4j.dml.wordprocessingDrawing.STAlignV.TOP
	    					|| pv.getAlign()==org.docx4j.dml.wordprocessingDrawing.STAlignV.INSIDE) cellV = "top:0";
	    		}
	    		if (!"-".equals(cellH) || !"-".equals(cellV)) g.setAttribute(HINT_ANCHOR_CELL, cellH + " " + cellV);
	    		/* Word grows the row so that an object its cell lays out and wraps the cell's
	    		 * text round ends inside the cell, below it the cell's bottom margin.  Measured on
	    		 * the anchor-in-cell-row-height probes (a 100 x 60pt picture in the second cell of
	    		 * a one-line row): square, tight, square behind the text (a letterhead's logo) and,
	    		 * in mode 15, square with layoutInCell 0 take the row to the picture's foot (146.7
	    		 * where the line ends at 100.6); in mode 15 its distB below that too (10pt: 156.5),
	    		 * in mode 14 not (146.7); wrapNone in front or behind, none.  topAndBottom puts
	    		 * the paragraph below the picture, which the flow already does.  @since 17.3.3 */
	    		if ("square".equals(kind)) g.setAttribute(HINT_ANCHOR_CELL_FOOT, fmt(mode >= 15 ? emu(anchor.getDistB()) : 0));
	    	}
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
    	/* wp:extent is in EMU, and 12700 EMU is exactly one point.  Integer division
    	 * threw the fraction away, so a picture Word sizes at 67.5pt was declared 67
    	 * (as "67px", which FOP reads as 67pt at its default 72dpi) and everything below
    	 * it moved up half a point; 291 such attributes in 75 of 156 corpus documents.
    	 * The unit is now stated as what it has always really been.  @since 17.1.0 */
    	if (size2d.getCx()!=0) {
    		dimensions.width= size2d.getCx() / (double)extentToPixelConversionFactor;
    		dimensions.widthUnit = "pt";
    	}
    	if (size2d.getCy()!=0) {
    		dimensions.height= size2d.getCy() / (double)extentToPixelConversionFactor;
    		dimensions.heightUnit = "pt";
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

