package org.docx4j.model.images;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart.SvgDocument;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Generate HTML/XSLFO  
 */
public abstract class AbstractWordXmlPicture {
	
	protected static Logger log = LoggerFactory.getLogger(AbstractWordXmlPicture.class);
	
	WordprocessingMLPackage wmlPackage;
    protected Dimensions dimensions;
    
    // TODO: partially implemented
    private BinaryPart metaFile;
	
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
    		
        	} else if (picture.metaFile==null) {
				// Usual case    	
			    d = picture.createHtmlImageElement();
			} else if (picture.metaFile instanceof MetafileWmfPart) {
				
				SvgDocument svgdoc = ((MetafileWmfPart)picture.metaFile).toSVG();
				d = svgdoc.getDomDocument();
				
			} 
			else if (picture.metaFile instanceof MetafileEmfPart) {
				
				 d = XmlUtils.getNewDocumentBuilder().newDocument();
				
				//log.info("Document: " + document.getClass().getName() );

				Node span = d.createElement("span");			
				d.appendChild(span);
				
				Text err = d.createTextNode( "[TODO emf image]" );
				span.appendChild(err);
				
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
            	imageElement.setAttribute("width",  Integer.toString(dimensions.width));
            }

            if (dimensions.height>0)
            {
            	imageElement.setAttribute("height", Integer.toString(dimensions.height));
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

	protected Document createXslFoImageElement()
    {

        try {
            // Create a DOM builder and parse the fragment
            Document document = XmlUtils.getNewDocumentBuilder().newDocument();
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
            if (dimensions.width>0)
            {
            	imageElement.setAttribute("content-width",  Integer.toString(dimensions.width)+dimensions.widthUnit);
            }

            if (dimensions.height>0)
            {
            	imageElement.setAttribute("content-height", Integer.toString(dimensions.height)+dimensions.heightUnit);
            }
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
			uri = handlePart(imageHandler, this, rel, (BinaryPart)part);
			if (uri != null) {
				this.setSrc(uri);
			}
		}
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
    	
    	public int height;
    	public String heightUnit;
    	
    	public int width;
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

