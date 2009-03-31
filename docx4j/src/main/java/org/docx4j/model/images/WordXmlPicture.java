package org.docx4j.model.images;

import java.io.OutputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.log4j.Logger;
import org.docx4j.convert.out.ConvertUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Generate HTML/XSLFO from 
 * 
 * Originally from OpenXmlView project.
 * TODO - add Microsoft Public Licence
 * 
 * TODO - integrate with our other image handling stuff
 * 
 * Amended .. can generate HTML element, or XSL FO.
 * 
 * @author dev
 *
 */
public class WordXmlPicture {
	
	protected static Logger log = Logger.getLogger(WordXmlPicture.class);
	
	Document document;
    Node imageElement = null;
    Node linkElement = null;

    /* Extension function to create an HTML <img> element
     * from "E2.0 images" 
     *      //w:drawing/wp:inline
     *     |//w:drawing/wp:anchor
     */    
    public static DocumentFragment createHtmlImgE20(WordprocessingMLPackage wmlPackage,
    		String imageDirPath,
    		NodeIterator pictureData, NodeIterator picSize,
    		NodeIterator picLink, NodeIterator linkData) {

    	WordXmlPicture picture = createWordXmlPicture( wmlPackage,
        		 imageDirPath, pictureData,  picSize,
        		 picLink,  linkData);
    	
        Document d = picture.createHtmlImageElement();

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
    }

    /* Extension function to create an XSL FO <fo:external-graphic> element
     * from "E2.0 images" 
     *      //w:drawing/wp:inline
     *     |//w:drawing/wp:anchor
     */    
    public static DocumentFragment createXslFoImgE20(WordprocessingMLPackage wmlPackage,
    		String imageDirPath,
    		NodeIterator pictureData, NodeIterator picSize,
    		NodeIterator picLink, NodeIterator linkData) {

    	WordXmlPicture picture = createWordXmlPicture( wmlPackage,
        		 imageDirPath, pictureData,  picSize,
        		 picLink,  linkData);
    	
        Document d = picture.createXslFoImageElement();

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
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
    public static WordXmlPicture createWordXmlPicture(WordprocessingMLPackage wmlPackage,
    		String imageDirPath,
    		NodeIterator pictureData, NodeIterator picSize,
    		NodeIterator picLink, NodeIterator linkData) {
    	
    	    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator
    	    	
    	WordXmlPicture picture = new WordXmlPicture();
    	picture.readStandardAttributes( pictureData.nextNode() );
    	
    	Node picSizeNode = picSize.nextNode();
    	if ( picSizeNode!=null ) {
            picture.readSizeAttributes(picSizeNode);    		
    	}

    	Node picLinkNode = picLink.nextNode();
        if (picLinkNode != null)
        {
            String linkRelId = ConvertUtils.getAttributeValueNS(picLinkNode, "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");

            if ( linkRelId!=null && !linkRelId.equals("") ) 
            {
            	Relationship rel = wmlPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(linkRelId);
            	
            	if (rel.getTargetMode() == null
            			|| rel.getTargetMode().equals("Internal") ) {
            		
            		picture.setHlinkReference("TODO - save this object");
            		
            	} else {
                    picture.setHlinkReference( rel.getTarget() );            	
            	}
            }

            picture.readLinkAttributes(picLinkNode);
        }
    	
    	Node linkDataNode = linkData.nextNode();
        if (linkDataNode == null) {
        	log.warn("Couldn't find a:blip!");
        } else {
            String imgRelId = ConvertUtils.getAttributeValueNS(linkDataNode, "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed");  // Microsoft code had r:link here

            if (imgRelId!=null && !imgRelId.equals(""))
            {
            	Relationship rel = wmlPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(imgRelId);
            	
            	if (rel.getTargetMode() == null
            			|| rel.getTargetMode().equals("Internal") ) {            		
            		
            		if ( !imageDirPath.equals("") ) {
            			// Need to save the image 
            			
            			try {
							// To create directory:
							FileObject folder = getFileSystemManager().resolveFile(imageDirPath);
							if (!folder.exists() ) {
							    folder.createFolder();
							}
							
							// Get the part
							Part part = wmlPackage.getMainDocumentPart().getRelationshipsPart().getPart(rel);
							
							// Construct a file name from the part name	
							String partname = part.getPartName().toString();
							String filename = partname.substring(partname.lastIndexOf("/") + 1);
							log.debug("image file name: " + filename);
							
							FileObject fo = folder.resolveFile(filename);
							if (fo.exists() ) {
								
								log.warn("Overwriting (!) existing file!"); 
								
							} else {
							   fo.createFile();
							}
//							System.out.println("URL: " + fo.getURL().toExternalForm() );
//							System.out.println("String: " + fo.toString() );
							
							// Save the file
			       			OutputStream out = fo.getContent().getOutputStream();
			    	        java.nio.ByteBuffer bb = ((BinaryPart)part).getBuffer();
			    	        bb.clear();
			    	        byte[] bytes = new byte[bb.capacity()];
			    	        bb.get(bytes, 0, bytes.length);
			    	        	        
			    	        out.write( bytes );
							
							// Set the attribute
			    	        String src = fixImgSrcURL( fo );
	                		picture.setSrc( src );  
			    	        log.info("Wrote @src='" + src);
							
						} catch (Exception e) {
							log.error(e);
						}
            			
            		} else {
                		picture.setSrc("BinaryPart_" + rel.getTarget() );
            		}
            		
            	} else {
                    picture.setSrc( rel.getTarget() );            	
            	}

            }

            // if the relationship isn't found, produce a warning
            //if (String.IsNullOrEmpty(picture.Src))
            //{
            //    this.embeddedPicturesDropped++;
            //}
        }
    	
        return picture;        
    }
    

    private static FileSystemManager fileSystemManager;
    private static ReadWriteLock aLock = new ReentrantReadWriteLock(true);
    
    public static FileSystemManager getFileSystemManager()
    {
        aLock.readLock().lock();

        try
        {
            if (fileSystemManager == null)
            {
                try
                {
                    StandardFileSystemManager fm = new StandardFileSystemManager();
                    fm.setCacheStrategy(CacheStrategy.MANUAL);
                    fm.init();
                    fileSystemManager = fm;
                }
                catch (Exception exc)
                {
                    throw new RuntimeException(exc);
                }
            }

            return fileSystemManager;
        }
        finally
        {
            aLock.readLock().unlock();
        }
    }
    
	/* imageDirPath is anything VFSJFileChooser can resolve into a FileObject.
	// That's enough for saving the image.
	// In order for a web browser to display it, the URI Scheme has to
	// be something a web browser can understand.
	// So at that point, webdav:// will have to become http://,
	// and smb:// become file:// ... */
    static String fixImgSrcURL( FileObject fo)
    {
    	String itemUrl = null;
		try {
			itemUrl = fo.getURL().toExternalForm();
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(itemUrl);
    	
        if (
            itemUrl.toLowerCase().startsWith("file://") ||
            itemUrl.toLowerCase().startsWith("http://") ||
            itemUrl.toLowerCase().startsWith("https://"))
        	
        {
        	
            return itemUrl;
        }
        
        if ( itemUrl.toLowerCase().startsWith("webdav://")  ) {
        	
        	// TODO - convert to http:, dropping username / password
        	return itemUrl;
        	
        }
        log.warn("How to handle scheme: " + itemUrl );
        
    	return itemUrl;
        
    }
    
    /* Extension function to create an <img> element
     * from "E1.0 images"
     *  
     *      //w:pict
     */
    public static DocumentFragment createImgE10(WordprocessingMLPackage wmlPackage,
    		String imageDirPath,
    		NodeIterator shape, NodeIterator imageData) {
    	
    	// NB as at 2008 10 24, this code has not been tested
    	    	
    	WordXmlPicture picture = new WordXmlPicture();
    	picture.readStandardAttributes( shape.nextNode() );
    	
    	Node imageDataNode = imageData.nextNode();
    	if ( imageDataNode==null ) {    		
        	log.warn("Couldn't find v:imagedata!");
        } else {
            String imgRelId = ConvertUtils.getAttributeValueNS(imageDataNode, "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id"); 

            if (imgRelId!=null && !imgRelId.equals(""))
            {
            	Relationship rel = wmlPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(imgRelId);
            	
            	if (rel.getTargetMode() == null
            			|| rel.getTargetMode().equals("Internal") ) {
            		
            		picture.setSrc("TODO - save object " + rel.getTarget() );
            		
            	} else {
                    picture.setSrc( rel.getTarget() );            	
            	}

            }

            // if the relationship isn't found, produce a warning
            //if (String.IsNullOrEmpty(picture.Src))
            //{
            //    this.embeddedPicturesDropped++;
            //}
        }
    	
        Document d = picture.createHtmlImageElement();

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
        
    }
    
    
	
	void setAttribute(String name, String value) {
		
		setAttribute( imageElement, name, value );
		
	}
	void setAttribute(Node element, String name, String value) {
		
    	org.w3c.dom.Attr tmpAtt = document.createAttribute(name);
    	tmpAtt.setValue(value);
    	element.getAttributes().setNamedItem(tmpAtt);
    	
    	log.debug("<" + element.getLocalName() + " @"+ name + "=\"" + value);
		
	}
	
    /// <id guid="100b714f-5397-4420-958b-e03c2d021f7c" />
    /// <owner alias="ROrleth" />
	public Document createHtmlImageElement()
    {

        try {
            // Create a DOM builder and parse the fragment
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            document = factory.newDocumentBuilder().newDocument();
            
            
            imageElement = document.createElement("img");

            if (src !=null && !src.equals(""))
            {
            	setAttribute("src", src);
            }

            if (id !=null && !id.equals("") )
            {
                setAttribute("id", id);
            }

            if (alt !=null && !alt.equals("") )
            {
                setAttribute("alt", alt);
            }

            if (style !=null && !style.equals("") )
            {
                setAttribute("style", style);
            }

            if (widthSet)
            {
                setAttribute("width",  Integer.toString(width));
            }

            if (heightSet)
            {
                setAttribute("height", Integer.toString(height));
            }

            if (hlinkRef !=null && !hlinkRef.equals(""))
            {
                linkElement = document.createElement("a");

                setAttribute(linkElement, "href", hlinkRef);

                if (targetFrame !=null && !targetFrame.equals(""))
                {
                    setAttribute(linkElement, "target", targetFrame);
                }

                if (tooltip !=null && !tooltip.equals(""))
                {
                    setAttribute(linkElement, "title", tooltip);
                }

                linkElement.appendChild(imageElement);

                imageElement = linkElement;
            }
            
            document.appendChild(imageElement);
            
            return document;
            
        } catch (Exception e) {
        	log.error(e);
            return null;
        }
        
    }

	public Document createXslFoImageElement()
    {

        try {
            // Create a DOM builder and parse the fragment
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            document = factory.newDocumentBuilder().newDocument();
                        
            imageElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
			"fo:external-graphic"); 	

            if (src !=null && !src.equals(""))
            {
            	setAttribute("src", src);
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
            if (widthSet)
            {
                setAttribute("content-width",  Integer.toString(width));
            }

            if (heightSet)
            {
                setAttribute("content-height", Integer.toString(height));
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
        	log.error(e);
            return null;
        }
        
    }
	
    /// <id guid="233b126d-66d0-476e-bcd1-ce30bdc3e65b" />
    /// <owner alias="ROrleth" />
    public void readStandardAttributes(Node fromNode)
    {
        this.id = ConvertUtils.getAttributeValue(fromNode, "id");
        this.pType = ConvertUtils.getAttributeValue(fromNode, "type");
        this.alt = ConvertUtils.getAttributeValue(fromNode, "alt");
        this.style = ConvertUtils.getAttributeValue(fromNode, "style");
    }

    /// <id guid="048da999-6fbe-41b9-9639-de0e084f3da3" />
    /// <owner alias="ROrleth" />
    public void readLinkAttributes(Node fromNode)
    {
        this.targetFrame = ConvertUtils.getAttributeValue(fromNode, "tgtFrame");
        this.tooltip = ConvertUtils.getAttributeValue(fromNode, "tooltip");
    }

    private final int extentToPixelConversionFactor = 12700;

    /// <id guid="cb8dfd67-57bb-4ebc-af9d-f6062d25b9ba" />
    /// <owner alias="ROrleth" />
    public void readSizeAttributes(Node fromNode)
    {
        String temp = null;
        temp = ConvertUtils.getAttributeValue(fromNode, "cx");
        if (temp !=null && !temp.equals("") )
        {
            setWidth ( Integer.parseInt(temp) / extentToPixelConversionFactor );
            	
            	//Convert.ToUInt32(temp, CultureInfo.InvariantCulture) / ExtentToPixelConversionFactor;
        }
        temp = ConvertUtils.getAttributeValue(fromNode, "cy");
        if (temp !=null && !temp.equals("") )
        {
            setHeight( Integer.parseInt(temp) / extentToPixelConversionFactor );
//            this.height = Convert.ToUInt32(temp, CultureInfo.InvariantCulture) / ExtentToPixelConversionFactor;
        }

    }

    private int width;
    private boolean widthSet;

    /// <summary>
    /// Width in pixels
    /// </summary>
    /// <id guid="f01d0577-7f05-4c1b-8dcf-ad36b93bbc3c" />
    /// <owner alias="ROrleth" />
    public int getWidth() {
    	return this.width;
    }
    public void setWidth(int value) {
		this.widthSet = true;
		this.width = value;
	}

    // / <summary>
    // / WidthSet - returns true if the width has been set intentionally
    /// </summary>
    /// <id guid="d3cb7ab3-a36a-455f-90aa-539904f2781e" />
    /// <owner alias="ROrleth" />
    public boolean getWidthSet() 
    {
    	return this.widthSet;
    }

    private int height;
    private boolean heightSet;

    /// <summary>
    /// Height in pixels
    /// </summary>
    /// <id guid="8a499702-53a6-430d-b0d1-7ef10e7711f1" />
    /// <owner alias="ROrleth" />
    public int getHeight() {
		return this.height;
	}

	public void setHeight(int value) {
		this.heightSet = true;
		this.height = value;
	}

	// / <summary>
	// / HeightSet - returns true if the height has been set intentionally
	// / </summary>
	// / <id guid="ad9b2c47-ce49-4104-97c1-8fe130d40fcd" />
	// / <owner alias="ROrleth" />
	public boolean getHeightSet() {
		return this.heightSet;
	}

	private String targetFrame;

	// / <summary>
	// / Target frame property
	// / </summary>
	// / <id guid="1acdad51-bba9-4876-9c53-b0753094c3e9" />
	// / <owner alias="ROrleth" />
	public String getTargetFrame() {
		return this.targetFrame;
	}

	public void setTargetFrame(String value) {
		this.targetFrame = value;
	}

    private String tooltip;

    // / <summary>
    // / tooltip property
    // / </summary>
    // / <id guid="c7b612aa-9970-49be-9569-44c62e4d1aa5" />
    // / <owner alias="ROrleth" />
    public String getTooltip() {
		return this.tooltip;
	}

	public void setTooltip(String value) {
		this.tooltip = value;
	}

	private String hlinkRef;

	// / <summary>
	// / store the hyperlink that the picture points to, if applicable
	// / </summary>
	// / <id guid="862f74dc-b0a2-44b9-8d0c-4c6d78abaeca" />
	// / <owner alias="ROrleth" />
	public String getHlinkReference() {
		return this.hlinkRef;
	}

	public void setHlinkReference(String value) {
		this.hlinkRef = value;
	}

    private String alt;
    // / <summary>
    // / The attribute of the v:shape node which maps to the
    // / 'alt' attribute of and HTML 'img' tag.
    // / </summary>
    // / <remarks>
    /// Also known as the 'alternate text' property of an
    /// HTML image.
    /// </remarks>
    /// <value>
    /// </value>
    /// <id guid="712de8d5-b603-4c01-a231-183c9de68db5" />
    /// <owner alias="ROrleth" />
    public String getAlt() {
		return this.alt;
	}

	public void setAlt(String value) {
		this.alt = value;
	}

    private byte[] data;
    // / <summary>
    // / The decoded data from the corresponding 'w:bindata'
    /// node of the Word Document.
    /// </summary>
    /// <remarks>
    /// This property is set by the conversion process.
    /// </remarks>
    /// <value>
    /// </value>
    /// <id guid="130108bf-d980-4753-b674-4d489acf485c" />
    /// <owner alias="ROrleth" />
    public byte[] getData() {
		return this.data;
	}

	public void setData(byte[] value) {
		this.data = value;
	}

	private String id;

	// / <summary>
	// / The identifier of the picture unique only within the scope of
	// / the Word Document.
	// / </summary>
	// / <value>
	// / </value>
	// / <id guid="e0d6cf93-79f7-4a38-884c-6b494b244664" />
	// / <owner alias="ROrleth" />
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

	
	
    private String style;
    // / <summary>
    // / The attribute of the v:shape node which maps to the
    /// 'style' attribute of and HTML 'img' tag.
    /// </summary>
    /// <value>
    /// </value>
    /// <id guid="700b62da-d914-4a40-aa96-1437d2b314e1" />
    /// <owner alias="ROrleth" />
    public String getStyle() {
		return this.style;
	}

	public void setStyle(String value) {
		this.style = value;
	}

    private String pType;
    // / <summary>
    // / The type of the picture as specified by the attribute of the
	// v:shape node
    /// within the Word Document.
    /// </summary>
    /// <remarks>
    /// This value is used as an identifier for a v:type node, which used to specify
    /// properties of the picture within the Word Document.
    /// </remarks>
    /// <value>
    /// </value>
    /// <id guid="78bf5c95-1d55-423c-bc34-92d926203e83" />
    /// <owner alias="ROrleth" />
    public String getPType() {
		return this.pType;
	}

	public void setPType(String value) {
		this.pType = value;
	}
}

