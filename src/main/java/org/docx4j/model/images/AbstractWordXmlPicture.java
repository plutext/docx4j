package org.docx4j.model.images;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafilePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart.SvgDocument;
import org.docx4j.relationships.Relationship;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Generate HTML/XSLFO  
 */
public abstract class AbstractWordXmlPicture {
	
	protected static Logger log = Logger.getLogger(AbstractWordXmlPicture.class);
	
	WordprocessingMLPackage wmlPackage;
    Dimensions dimensions;
    private MetafilePart metaFile;
	
	protected final static String IMAGE_URL = "http://docxwave.appspot.com/image?";
	
    public static DocumentFragment getHtmlDocumentFragment(AbstractWordXmlPicture picture) {
    	
    	DocumentFragment docfrag=null;
    	Document d=null;
    	try {
        	if (picture==null) {
    			log.warn("picture was null!");
            	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
    			 try {
    				d = factory.newDocumentBuilder().newDocument();
    			} catch (ParserConfigurationException e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
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
				
			} else if (picture.metaFile instanceof MetafileEmfPart) {
				
	        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
				 d = factory.newDocumentBuilder().newDocument();
				
				//log.info("Document: " + document.getClass().getName() );

				Node span = d.createElement("span");			
				d.appendChild(span);
				
				Text err = d.createTextNode( "[TODO emf image]" );
				span.appendChild(err);
				
			}
		} catch (Exception e) {
			log.error(e);
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			 try {
				d = factory.newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            Document document = factory.newDocumentBuilder().newDocument();
            
            
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
        	log.error(e);
            return null;
        }
        
    }

	protected Document createXslFoImageElement()
    {

        try {
            // Create a DOM builder and parse the fragment
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            Document document = factory.newDocumentBuilder().newDocument();
                        
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
        	log.error(e);
            return null;
        }
        
    }
	

	/**
	 * @param imageDirPath
	 * @param picture
	 * @param part
	 * @return uri for the image we've saved, or null
	 */
	protected static String handlePart(String imageDirPath, AbstractWordXmlPicture picture,
			Part part) {
		try {

			if (imageDirPath.equals("")) {
				
				// TODO: this isn't going to work for XSL FO!
				// So for XSL FO, you always need an imageDirPath! 

				// <img
				// src="data:image/gif;base64,R0lGODlhEAAOALMAAOazToeHh0tLS/7LZv/0jvb29t/f3//Ub/
				//
				// which is nice, except it doesn't work in IE7,
				// and is limited to 32KB in IE8!

				java.nio.ByteBuffer bb = ((BinaryPart) part)
						.getBuffer();
				bb.clear();
				byte[] bytes = new byte[bb.capacity()];
				bb.get(bytes, 0, bytes.length);
				
				byte[] encoded = Base64.encodeBase64(bytes, true);

				picture
						.setSrc("data:" + part.getContentType()
								+ ";base64,"
								+ (new String(encoded, "UTF-8")));
				
				return null;

			} else {
				// Need to save the image

				// To create directory:
				FileObject folder = getFileSystemManager()
						.resolveFile(imageDirPath);
				if (!folder.exists()) {
					folder.createFolder();
				}

				// Construct a file name from the part name
				String partname = part.getPartName().toString();
				String filename = partname.substring(partname
						.lastIndexOf("/") + 1);
				log.debug("image file name: " + filename);

				FileObject fo = folder.resolveFile(filename);
				if (fo.exists()) {

					log.warn("Overwriting (!) existing file!");

				} else {
					fo.createFile();
				}
				// System.out.println("URL: " +
				// fo.getURL().toExternalForm() );
				// System.out.println("String: " + fo.toString() );

				// Save the file
				OutputStream out = fo.getContent()
						.getOutputStream();
				// instance of org.apache.commons.vfs.provider.DefaultFileContent$FileContentOutputStream
				// which extends MonitorOutputStream
			    // which in turn extends BufferedOutputStream
			    // which in turn extends FilterOutputStream.
				
				String src;
				try {
					java.nio.ByteBuffer bb = ((BinaryPart) part)
							.getBuffer();
					bb.clear();
					byte[] bytes = new byte[bb.capacity()];
					bb.get(bytes, 0, bytes.length);

					out.write(bytes);
					
					// Set the attribute
					src = fixImgSrcURL(fo);
					picture.setSrc(src);
					log.info("Wrote @src='" + src);
					return src;
				} finally {
					try {
						fo.close();
						// That Closes this file, and its content.
						// Closing the content in turn
						// closes any open stream.
						// out.flush() is unnecessary, since 
						// FilterOutputStream's close() does do flush() first.
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}					
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return null;
	}

	private static FileSystemManager fileSystemManager;
	private static ReadWriteLock aLock = new ReentrantReadWriteLock(true);

	public static FileSystemManager getFileSystemManager() {
		aLock.readLock().lock();

		try {
			if (fileSystemManager == null) {
				try {
					StandardFileSystemManager fm = new StandardFileSystemManager();
					fm.setCacheStrategy(CacheStrategy.MANUAL);
					fm.init();
					fileSystemManager = fm;
				} catch (Exception exc) {
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
    
	/**
	 * imageDirPath is anything VFSJFileChooser can resolve into a FileObject. 
	 * That's enough for saving the image. In order for a web browser to
	 * display it, the URI Scheme has to be something a web browser can
	 * understand. So at that point, webdav:// will have to become http://, 
	 * and smb:// become file:// ...
	 */
    static String fixImgSrcURL( FileObject fo)
    {   	
    	String itemUrl = null;
		try {
			itemUrl = fo.getURL().toExternalForm();
			log.debug(itemUrl);

			String itemUrlLower = itemUrl.toLowerCase();			
	        if (itemUrlLower.startsWith("http://") 
	        		 || itemUrlLower.startsWith("https://")) {
				return itemUrl;
			} else if (itemUrlLower.startsWith("file://")) {
				// we'll convert file protocol to relative reference
				// if this is html output
				
				if (fo.getParent() == null) {
					return itemUrl;					
				} else if (fo.getParent().getURL().toExternalForm().equalsIgnoreCase(
						    getFileSystemManager().resolveFile(System.getProperty("java.io.tmpdir")).getURL().toExternalForm() )) {
					
					// The image is being stored in the system temp directory,
					// so assume this is a pdf export, and preserve the absolute
					// file path

					// org.apache.commons.vfs.provider.local.LocalFile has a
					// method doIsSameFile, but the point of using FileObject is
					// that it won't necessarily be a local file. 
					
					return itemUrl;						
				} else {
		             // Otherwise, assume it is an html export and return a relative path
					return  fo.getParent().getName().getBaseName() 
								+ "/" + fo.getName().getBaseName();
				}
				
			} else if (itemUrlLower.startsWith("webdav://")) {
				// TODO - convert to http:, dropping username / password
				return itemUrl;
			} 			
	        log.warn("How to handle scheme: " + itemUrl );        
		} catch (FileSystemException e) {
			log.error("Problem fixing Img Src URL", e);
		}		    	
    	return itemUrl;        
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
    class Dimensions {
    	
    	int height;
    	String heightUnit;
    	
    	int width;
    	String widthUnit;
    	
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

