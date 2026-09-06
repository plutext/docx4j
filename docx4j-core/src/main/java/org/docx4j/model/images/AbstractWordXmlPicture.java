package org.docx4j.model.images;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Generate HTML/XSLFO  
 */
public abstract class AbstractWordXmlPicture {
	
	protected static Logger log = LoggerFactory.getLogger(AbstractWordXmlPicture.class);
	
	WordprocessingMLPackage wmlPackage;
    protected Dimensions dimensions;

	/** The picture's bytes, where they are a Windows metafile this build can draw
	 *  (see {@link #renderMetafile}); null otherwise.  Until 17.0.6 this field was
	 *  declared and tested but never assigned, so the WMF branch below was dead code
	 *  and both WMF and EMF fell through to an &lt;img&gt;/fo:external-graphic naming
	 *  a file neither a browser nor FOP can decode (CR-011 §2). */
	protected BinaryPart metaFile;

	/** The metafile rendered as SVG, where that succeeded.  @since 17.0.6 */
	protected Document metaFileSvg;

	/** Set on the pictures the XSL-FO exporters build, not the HTML ones: the two
	 *  want a metafile represented differently.  @since 17.0.6 */
	protected boolean forXslFo;

	protected final static String IMAGE_URL = "http://docxwave.appspot.com/image?";

    public static DocumentFragment getHtmlDocumentFragment(AbstractWordXmlPicture picture) {

    	DocumentFragment docfrag=null;
    	Document d=null;
    	try {
        	if (picture==null) {
    			log.warn("picture was null!");
    				d = XmlUtils.getNewDocumentBuilder().newDocument();
    			Element span = d.createElement("span");
    			span.setAttribute("style", "color:red;");
    			d.appendChild(span);

    			Text err = d.createTextNode( "[null img]" );
    			span.appendChild(err);

        	} else if (picture.metaFileSvg!=null) {
				// A metafile, drawn as inline SVG: vectors, and text a browser can select.
				d = picture.metaFileSvg;
			} else {
				// Usual case; also a metafile represented as a PNG (@src is the PNG)
			    d = picture.createHtmlImageElement();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
				d = XmlUtils.getNewDocumentBuilder().newDocument();
			Element span = d.createElement("span");
			span.setAttribute("style", "color:red;");
			d.appendChild(span);
			
			Text err = d.createTextNode( e.getMessage() );
			span.appendChild(err);
		}
		docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());
		return docfrag;
    }
	
	
	public Document createHtmlImageElement()
    {

        try {
            // Create a DOM builder and parse the fragment
            Document document = XmlUtils.getNewDocumentBuilder().newDocument();
            Element imageElement  = document.createElement("img");

            if (src !=null && !src.equals(""))
            {
            	imageElement.setAttribute("src", src);
            }

            if (id !=null && !id.equals("") )
            {
            	imageElement.setAttribute("id", id);
            }

            if (alt !=null && !alt.equals("") )
            {
            	imageElement.setAttribute("alt", alt);
            }

            if (style !=null && !style.equals("") )
            {
            	imageElement.setAttribute("style", style);
            }

            if (dimensions.width>0)
            {
            	imageElement.setAttribute("width",  Integer.toString((int) Math.round(dimensions.width)));
            }

            if (dimensions.height>0)
            {
            	imageElement.setAttribute("height", Integer.toString((int) Math.round(dimensions.height)));
            }

            if (hlinkRef !=null && !hlinkRef.equals(""))
            {
            	Element linkElement = document.createElement("a");

                linkElement.setAttribute( "href", hlinkRef);

                if (targetFrame !=null && !targetFrame.equals(""))
                {
                	linkElement.setAttribute( "target", targetFrame);
                }

                if (tooltip !=null && !tooltip.equals(""))
                {
                	linkElement.setAttribute( "title", tooltip);
                }

                linkElement.appendChild(imageElement);

                imageElement = linkElement;
            }
            
            document.appendChild(imageElement);
            
            return document;
            
        } catch (Exception e) {
        	e.printStackTrace();
        	log.error(e.getMessage(), e);
            return null;
        }
        
    }

	/** A length for XSL-FO: as many decimals as it takes, at most two, and no trailing
	 *  zeros.  Word states a picture's size in EMU, so 857250 EMU is 67.5pt, not 67.
	 *  @since 17.0.6 */
	protected static String length(double v) {
		String s = String.format(java.util.Locale.ROOT, "%.2f", v);
		while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}

	protected Document createXslFoImageElement()
    {

        try {
            // Create a DOM builder and parse the fragment
            Document document = XmlUtils.getNewDocumentBuilder().newDocument();

            if (metaFileSvg!=null) {
            	/* A Windows metafile, replayed as SVG: FOP draws it into the PDF as
            	 * vectors (and its text as text, where it can resolve the font).
            	 * Until 17.0.6 an fo:external-graphic named the .wmf/.emf itself,
            	 * which FOP has no loader for.  @since 17.0.6, CR-011 */
            	Element ifo = document.createElementNS("http://www.w3.org/1999/XSL/Format",
            			"fo:instream-foreign-object");
            	document.appendChild(ifo);
            	sizeGraphic(ifo);
            	ifo.appendChild(document.importNode(metaFileSvg.getDocumentElement(), true));
            	return document;
            }

            Element imageElement  = document.createElementNS("http://www.w3.org/1999/XSL/Format",
			"fo:external-graphic");

            if (src !=null && !src.equals(""))
            {
            	imageElement.setAttribute("src", src);
            } else {
            	log.error("@src missing!");
            }

//            if (id !=null && !id.equals("") )
//            {
//                setAttribute("id", id);
//            }
//
//            if (alt !=null && !alt.equals("") )
//            {
//                setAttribute("alt", alt);
//            }
//
//            if (style !=null && !style.equals("") )
//            {
//                setAttribute("style", style);
//            }
//
            sizeGraphic(imageElement);
//
//            if (hlinkRef !=null && !hlinkRef.equals(""))
//            {
//                linkElement = document.createElement("a");
//
//                setAttribute(linkElement, "href", hlinkRef);
//
//                if (targetFrame !=null && !targetFrame.equals(""))
//                {
//                    setAttribute(linkElement, "target", targetFrame);
//                }
//
//                if (tooltip !=null && !tooltip.equals(""))
//                {
//                    setAttribute(linkElement, "title", tooltip);
//                }
//
//                linkElement.appendChild(imageElement);
//
//                imageElement = linkElement;
//            }

            document.appendChild(imageElement);

            return document;

        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return null;
        }

    }

	/** content-width / content-height / scaling on an fo:external-graphic or
	 *  fo:instream-foreign-object: the frame the document gives the picture.
	 *  (WordLayoutFixups and TableWriter read these back.)  @since 17.0.6 */
	private void sizeGraphic(Element imageElement) {

            if (dimensions==null) return;

            if (dimensions.width>0)
            {
            	imageElement.setAttribute("content-width",  length(dimensions.width)+dimensions.widthUnit);
            }

            if (dimensions.height>0)
            {
            	imageElement.setAttribute("content-height", length(dimensions.height)+dimensions.heightUnit);
            }

            if (dimensions.width>0 && dimensions.height>0)
            {
            	/* Word draws a picture filling the frame the document declares (wp:extent,
            	 * or the VML shape's width/height), whatever the stored bitmap's own aspect
            	 * ratio: a crop (a:srcRect) or a deliberate stretch makes the two differ.
            	 * XSL-FO's default scaling is "uniform", so where both content-width and
            	 * content-height are given FOP scales by the smaller factor and leaves the
            	 * frame part empty - a 4:3 photograph cropped to 1.9:1 came out 493pt wide
            	 * in Word and 346pt here, and two such pictures then needed a page each.
            	 * 651 of 1737 pictures across 50 documents of a long-document corpus
            	 * declare an extent whose aspect differs from the bitmap's by over 2%.
            	 * The crop itself is not reproduced (§9.1): the picture is stretched into
            	 * the frame rather than cropped to it, which is the same geometry.
            	 */
            	imageElement.setAttribute("scaling", "non-uniform");
            }
    }

	protected void handleImageRel(ConversionImageHandler imageHandler, String imgRelId, Part sourcePart) {
	Relationship rel = sourcePart.getRelationshipsPart().getRelationshipByID(imgRelId);
	Part part = null;
	String uri = null;
	boolean ignoreImage = false;
		setID(imgRelId);

		part = sourcePart.getRelationshipsPart().getPart(rel);
		/* a part == null is ok if it is an external image,
		 * and hasn't been loaded (loadExternalTargets == false)
		 * but the relationship can be external,
		 * but the part avaiable (loadExternalTargets == true)
		 */
		if ((part != null) && (!(part instanceof BinaryPart))) {
			log.error("Invalid part type id: " + imgRelId + ", class = " + part.getClass().getName());
			ignoreImage = true;
		}
		if (!ignoreImage) {
			if (renderMetafile(imageHandler, rel, (BinaryPart)part)) {
				return; // drawn as SVG, or rasterised through the image handler
			}
			uri = handlePart(imageHandler, this, rel, (BinaryPart)part);
			if (uri != null) {
				this.setSrc(uri);
			}
		}
	}

	// ------------------------------------------------------- Windows metafiles (CR-011)

	/** Pixels per inch a metafile is rasterised at where SVG is not available. */
	private static final String METAFILE_DPI_PROPERTY = "docx4j.convert.out.metafile.dpi";

	/**
	 * A WMF / EMF / EMF+ picture, drawn rather than handed on as bytes nothing
	 * downstream can decode.
	 *
	 * <p>Word draws every picture; before 17.0.6 docx4j passed a metafile through to
	 * the output untouched, so the HTML named a {@code .wmf}/{@code .emf} a browser
	 * cannot show and the FO named one FOP has no loader for (CR-011 §2).  Now the
	 * bytes are replayed:</p>
	 * <ul>
	 * <li><b>XSL-FO</b>: as SVG in {@code fo:instream-foreign-object}, so the picture
	 *     reaches the PDF as vectors.</li>
	 * <li><b>HTML</b>: as an inline {@code <svg>} where the image handler embeds
	 *     images in the document anyway (a data URI handler), and otherwise as a PNG
	 *     put through that same handler - so a handler which writes files writes a
	 *     {@code .png} beside the others, and the {@code <img>} points at it.</li>
	 * </ul>
	 *
	 * <p>Everything degrades: no SVG provider on the classpath (docx4j-core without
	 * docx4j-export-fo) means a PNG, and a metafile that cannot be drawn at all means
	 * this returns false and the caller emits what it always did - which, for the FO
	 * pathway, is the {@code fo:external-graphic} that
	 * {@code WordLayoutFixups.reserveUnpaintablePictures} converts with ImageMagick or
	 * replaces with a transparent placeholder of the right size.</p>
	 *
	 * @return true if the picture has been dealt with (@src set, or SVG built)
	 * @since 17.0.6
	 */
	private boolean renderMetafile(ConversionImageHandler imageHandler, Relationship rel, BinaryPart part) {

		if (part == null) return false;
		byte[] data = metafileBytes(part);
		if (data == null) return false;

		this.metaFile = part;
		double w = lengthInPoints(dimensions == null ? 0 : dimensions.width,
				dimensions == null ? null : dimensions.widthUnit);
		double h = lengthInPoints(dimensions == null ? 0 : dimensions.height,
				dimensions == null ? null : dimensions.heightUnit);

		if (forXslFo || inlines(imageHandler)) {
			metaFileSvg = MetafileSvgProvider.toSvg(data, w, h);
			if (metaFileSvg != null) return true;
		}
		if (forXslFo) {
			// no SVG: leave it to the existing fo:external-graphic fallbacks
			this.metaFile = null;
			return false;
		}

		// HTML, as PNG
		int dpi = org.docx4j.Docx4jProperties.getProperty(METAFILE_DPI_PROPERTY, 96);
		byte[] png = PoiMetafileRenderer.getInstance().toPngBytes(data, dpi);
		if (png == null) {
			this.metaFile = null;
			return false;
		}
		String uri = handlePart(imageHandler, this, rel, pngPart(part, png));
		if (uri == null) {
			this.metaFile = null;
			return false;
		}
		setSrc(uri);
		return true;
	}

	/**
	 * The part's bytes if they are a metafile this build can draw, else null.
	 *
	 * <p>The declared content type is only a hint - Word writes it from the file
	 * extension it was given - so the bytes themselves decide, and a part typed
	 * {@code image/x-emf} holding a PNG is left alone as much as the reverse is
	 * picked up.  The signature is read from the buffer without copying it, so a
	 * document full of JPEGs pays four bytes a picture for the check.</p>
	 */
	private static byte[] metafileBytes(BinaryPart part) {
		try {
			java.nio.ByteBuffer bb = part.getBuffer();
			if (bb == null) return null;
			bb = bb.duplicate();
			int n = Math.min(bb.remaining(), PoiMetafileRenderer.SNIFF_LENGTH);
			if (n < 4) return null;
			byte[] head = new byte[n];
			bb.get(head);
			if (PoiMetafileRenderer.sniff(head) == null) return null;
			return part.getBytes();
		} catch (Exception e) {
			log.warn("Could not read picture bytes: " + e.getMessage());
			return null;
		}
	}

	/** Whether this handler embeds images in the output document rather than writing
	 *  files - in which case an inline &lt;svg&gt; is what it would want. */
	private static boolean inlines(ConversionImageHandler imageHandler) {
		return imageHandler != null && imageHandler.isInline();
	}

	/** The rendered PNG dressed as a part, so it goes through the caller's own
	 *  {@link ConversionImageHandler} - which writes the file, or encodes the data
	 *  URI, exactly as it does for a real PNG in the package. */
	private static BinaryPart pngPart(BinaryPart source, byte[] png) {
		try {
			String name = (source.getPartName() == null)
					? "/word/media/metafile.png"
					: source.getPartName().getName() + ".png";
			BinaryPart p = new BinaryPart(new org.docx4j.openpackaging.parts.PartName(name));
			p.setContentType(new org.docx4j.openpackaging.contenttype.ContentType(
					org.docx4j.openpackaging.contenttype.ContentTypes.IMAGE_PNG));
			p.setBinaryData(png);
			return p;
		} catch (Exception e) {
			log.warn("Could not wrap the rendered metafile: " + e.getMessage());
			return source;
		}
	}

	/** A length as XSL-FO/CSS states it, in points; 0 where it is not stated. */
	protected static double lengthInPoints(double v, String unit) {
		if (!(v > 0)) return 0;
		if (unit == null) return v;
		if (unit.equals("pt") || unit.equals("px")) return v; // px: docx4j's 72dpi convention
		if (unit.equals("in")) return v * 72d;
		if (unit.equals("cm")) return v * 72d / 2.54d;
		if (unit.equals("mm")) return v * 72d / 25.4d;
		if (unit.equals("pc")) return v * 12d;
		return v;
	}

	/**
	 * @param imageHandler
	 * @param picture
	 * @param relationship
	 * @param part
	 * @return uri for the image we've saved, or null
	 */
	protected String handlePart(ConversionImageHandler imageHandler, AbstractWordXmlPicture picture, Relationship relationship, BinaryPart binaryPart) {
	String uri = null;
		try {
			uri = imageHandler.handleImage(picture, relationship, binaryPart);
		}
		catch (Docx4JException de) {
			if (relationship != null) {
				log.error("Exception handling image id: " + relationship.getId() + ", target '" + relationship.getTarget() + "': " + de.toString(), de);
			}
			else {
				log.error("Exception handling image: " + de.toString(), de);
			}
		}
		return uri;
	}
	
//	void setAttribute(Node imageElement, String name, String value) {
//		
//		setAttribute( document, imageElement, name, value );
//		
//	}
//	void setAttribute(Document document, Element element, String name, String value) {
//		
//		
//    	org.w3c.dom.Attr tmpAtt = document.createAttribute(name);
//    	tmpAtt.setValue(value);
//    	element.getAttributes().setNamedItem(tmpAtt);
//    	
//    	log.debug("<" + element.getLocalName() + " @"+ name + "=\"" + value);
//		
//	}
	
		
    
    
    
    /**
     * Values as parsed from E10 CSS.
     *
     */
    public class Dimensions {
    	
    	/** @since 17.0.6 a fractional length: Word's wp:extent is in EMU, and rounding it
    	 *  to a whole unit lost up to half a point per picture (an image declared 67.5pt
    	 *  tall came out 67), which moves every line below it. */
    	public double height;
    	public String heightUnit;
    	
    	/** @since 17.0.6 fractional; see {@link #height} */
    	public double width;
    	public String widthUnit;
    	
    //  /**
    //  * If the docx does not explicitly size the
    //  * image, check that it will fit on the page 
    //  */
    // private void ensureFitsPage(ImageInfo imageInfo, PageDimensions page) {
    //
    // 	
//     	CxCy cxcy = BinaryPartAbstractImage.CxCy.scale(imageInfo, page);    
    // 	
//     	if (cxcy.isScaled() ) {
//     		log.info("Scaled to fit page width");
//     		this.setWidth( Math.round(cxcy.getCx()/extentToPixelConversionFactor) );
//     		this.setHeight( Math.round(cxcy.getCy()/extentToPixelConversionFactor) );    
//     		// That gives pixels, which is ok for HTML, but for XSL FO, we want pt or mm etc
//     	}
    // 	
    // }
    	
    }
    
    
    

	// Hyperlink stuff - Only in E20?
	protected String hlinkRef;
	public String getHlinkReference() {
		return this.hlinkRef;
	}
	public void setHlinkReference(String value) {
		this.hlinkRef = value;
	}
	protected String targetFrame;
	public String getTargetFrame() {
		return this.targetFrame;
	}
	public void setTargetFrame(String value) {
		this.targetFrame = value;
	}

    protected String tooltip;
    public String getTooltip() {
		return this.tooltip;
	}
	public void setTooltip(String value) {
		this.tooltip = value;
	}

	// Alt - only in E10?
    protected String alt;
    // / The attribute of the v:shape node which maps to the
    // / 'alt' attribute of and HTML 'img' tag.
    public String getAlt() {
		return this.alt;
	}

	public void setAlt(String value) {
		this.alt = value;
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

	protected String id;

	// / The identifier of the picture unique only within the scope of
	// / the Word Document.
	public String getID() {
		return this.id;
	}

	public void setID(String value) {
		this.id = value;
	}

    private String src;
    public String getSrc() {
		return this.src;
	}
	public void setSrc(String value) {
		this.src = value;
	}

	
	
    protected String style;
    // / The attribute of the v:shape node which maps to the
    /// 'style' attribute of and HTML 'img' tag.
    public String getStyle() {
		return this.style;
	}

	public void setStyle(String value) {
		this.style = value;
	}

    protected String pType;
    /**
     * The type of the picture as specified by the attribute of the v:shape node 
     * within the Word Document. This value is used as an identifier for a v:type 
     * node, which used to specify 
     * properties of the picture within the Word Document.
     * @return
     */
    public String getPType() {
		return this.pType;
	}

	public void setPType(String value) {
		this.pType = value;
	}
}

