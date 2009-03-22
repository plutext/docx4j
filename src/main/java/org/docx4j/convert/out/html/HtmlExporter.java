package org.docx4j.convert.out.html;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.log4j.Logger;
import org.docx4j.convert.out.Output;
import org.docx4j.convert.out.xmlPackage.XmlPackage;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
import org.docx4j.listnumbering.Emulator;
import org.docx4j.listnumbering.Emulator.ResultTriple;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class HtmlExporter implements Output {
	
	
	protected static Logger log = Logger.getLogger(HtmlExporter.class);
	
	public static void log(String message ) {
		
		log.info(message);
	}
	
	// Get the xslt file - Works in Eclipse - note absence of leading '/'
	static java.io.InputStream xslt;
		
	static {
		try {
			xslt = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/convert/out/html/DocX2Html.xslt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	// Implement the interface.  Everything in this class was
	// static, until now.
	
	WordprocessingMLPackage wmlPackage;
	public void setWmlPackage(WordprocessingMLPackage wmlPackage) {
		this.wmlPackage = wmlPackage;
	}

	HtmlSettings htmlSettings;
	public void setHtmlSettings(HtmlSettings htmlSettings) {
		this.htmlSettings = htmlSettings;
	}
	
	
	public void output(javax.xml.transform.Result result) throws Docx4JException {
		
		if (wmlPackage==null) {
			throw new Docx4JException("Must setWmlPackage");
		}
		
		if (htmlSettings==null) {
			log.debug("Using empty HtmlSettings");
			htmlSettings = new HtmlSettings();			
		}		
		
		try {
			html(wmlPackage, result, htmlSettings);
		} catch (Exception e) {
			throw new Docx4JException("Failed to create HTML output", e);
		}		
		
	}
	
	// End interface
	
	/** Create an html version of the document, using CSS font family
	 *  stacks.  This is appropriate if the HTML is intended for
	 *  viewing in a web browser, rather than an intermediate step
	 *  on the way to generating PDF output. The Microsoft Conditional
	 *  Comments (supportMisalignedColumns, supportAnnotations,
	 *  and mso) which are defined in the XSLT are not inserted.
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public static void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result,
    		String imageDirPath) throws Exception {
    	
    	html(wmlPackage, result, true, imageDirPath);
    }

    public static void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, boolean fontFamilyStack,
    		String imageDirPath) throws Exception {

		// Prep parameters
    	HtmlSettings htmlSettings = new HtmlSettings();
    	htmlSettings.setFontFamilyStack(fontFamilyStack);
    	
    	if (imageDirPath==null) {
    		imageDirPath = "";
    	}
    	htmlSettings.setImageDirPath(imageDirPath);    	
    	
		html(wmlPackage, result, htmlSettings);
    }
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public static void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, 
    		HtmlSettings htmlSettings) throws Exception {
    	
    	/*
    	 * Given that word2html.xsl is freely available, use a
    	 * version of it adapted to process the
    	 * pck:package/pck:part stuff emitted by Word 2007.
    	 * 
    	 */    	
		XmlPackage worker = new XmlPackage(wmlPackage);
		org.docx4j.xmlPackage.Package pkg = worker.get();

		/*  OpenXMLViewer merges the following parts into the XML:
		 *  
		 *    settings, styles, numbering, theme, fontTable, webSettings
 
		 *  hyperlinks and images are handled separately.
		 *  
		 *  We need an option to leave images out of the pkg?
		 *  
		 */
		
		
		JAXBContext jc = Context.jcXmlPackage;
		Marshaller marshaller=jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();

		marshaller.marshal(pkg, doc);

		//log.error( org.docx4j.XmlUtils.marshaltoString(pkg, true, true, jc)  );
		
		
//    	org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart stylesPart =
//    		wmlPackage.getMainDocumentPart().getStyleDefinitionsPart();
//    	log.error( org.docx4j.XmlUtils.marshaltoString(stylesPart.getJaxbElement(), true, true)  );
		
        // Load the link relationship tables
		
		
		
//        Hashtable imageTable = new Hashtable();
//
//        Metro.PackageRelationshipCollection imageRels = wordDoc.GetRelationshipsByType(ImageRelationshipUri);
//
//        ReadRelationshipCollectionIntoHashtable(imageRels, imageTable);
//
//        System.IO.MemoryStream html = null;
//
//        // link the hyperlinks from the relationship tables in
//        HandleImages(mainDoc, nsm, imageTable, linkTable);
//        HandleThemeFonts(mainDoc, nsm);
//        HandleNumberedLists(mainDoc, nsm);
		
			
		// Prep parameters
		if (htmlSettings==null) {
			htmlSettings = new HtmlSettings();
			// ..Ensure that the font names in the XHTML have been mapped to these matches
			//     possibly via an extension function in the XSLT
		}

		

		if (htmlSettings.getFontMapper()==null) {
			
			if (wmlPackage.getFontMapper()==null) {
				log.debug("Creating new Substituter.");
//				wmlPackage.setFontSubstituter(new SubstituterImplPanose());
				wmlPackage.setFontMapper(new IdentityPlusMapper());
			} else {
				log.debug("Using existing Substituter.");
			}
			htmlSettings.setFontMapper(wmlPackage.getFontMapper());
		}
		
		htmlSettings.setWmlPackage(wmlPackage);
		
		
		// Now do the transformation
		org.docx4j.XmlUtils.transform(doc, xslt, htmlSettings.getSettings(), result);
		
		log.info("wordDocument transformed to xhtml ..");
    	
    }
    

    /* 
    
		<w:hyperlink r:id="rId4" w:history="true">
			<w:r>
				<w:rPr>
				    <w:rStyle w:val="Hyperlink"/>
				</w:rPr>
				<w:t>hyperlink</w:t>
			</w:r>
		</w:hyperlink>
	
	  Micrososoft C# code replaces w:hyperlink with 
	  a new node 
	  
	      <w:hlink w:dest=".." [other attributes cloned] />
	      
	  before the XSLT is called.
	
	  But we use an extension function instead.
                    
                    */    
    public static String resolveHref( WordprocessingMLPackage wmlPackage, String id  )  {
    	
    	org.docx4j.relationships.Relationship rel = wmlPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id);
    	
    	if ( rel != null) {
    		
        	// TODO resolve ServerRelativePath, if its not a full URL 

    		return rel.getTarget();
    		
    	} else {
    		
    		log.error("Couldn't resolve hyperlink for rel " + id);    		
    		return "";    		
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
    
    /**
	 * The method used by the XSLT extension function during HTML export.
	 * 
	 * @param em
	 * @param levelId
	 * @param numId
	 * @return
	 */
    public static DocumentFragment getNumberXmlNode(WordprocessingMLPackage wmlPackage,
    		String pStyleVal, String numId, String levelId) {
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	log.info("numbering, using style '" + pStyleVal + "'; numId=" + numId + "; ilvl " + levelId);
    	System.out.println("numbering, using style '" + pStyleVal + "'; numId=" + numId + "; ilvl " + levelId);
    	
    	
        // Create a DOM builder and parse the fragment
        try {
        	ResultTriple triple = org.docx4j.listnumbering.Emulator.getNumber(
        			wmlPackage, pStyleVal, numId, levelId);   
        	
        	if (triple==null) {
        		log.info("computed number was null");
        		System.out.println("computed number was null!");
        		return null;
        	}

        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			       
			Node spanElement = document.createElement("span");			
			document.appendChild(spanElement);

	    	if (triple.getNumString()==null) {
	    		log.error("computed NumString was null!");
	    		System.out.println("computed NumString was null!");
	    	}
			
			Text number = document.createTextNode( triple.getNumString() );
			spanElement.appendChild(number);
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
    }
    
    
    /* Extension function to create an <img> element
     * from "E2.0 images" 
     *      //w:drawing/wp:inline
     *     |//w:drawing/wp:anchor
     */
    public static DocumentFragment createImgE20(WordprocessingMLPackage wmlPackage,
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
            String linkRelId = getAttributeValueNS(picLinkNode, "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");

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
            String imgRelId = getAttributeValueNS(linkDataNode, "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed");  // Microsoft code had r:link here

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
    	
        Document d = picture.createImageElement();

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
        
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
            String imgRelId = getAttributeValueNS(imageDataNode, "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id"); 

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
    	
        Document d = picture.createImageElement();

		DocumentFragment docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());

		return docfrag;
        
    }
    
    
    static String getAttributeValue(Node node, String name)
    {
    	
//    	log.debug("node " + node.getNodeName() );
//    	
//    	log.debug("@" + name);
        String value = "";
        
        Node attribute = node.getAttributes().getNamedItem(name);
        
//        if (attribute == null) {
//        	log.debug("@" + name + " missing");
//        }
        
        if (attribute != null &&  ((org.w3c.dom.Attr)attribute).getNodeValue()!= null)
        {
            value = ((org.w3c.dom.Attr)attribute).getNodeValue();
        }
//        log.debug(" = " + value);

        return value;
    }
    
    static String getAttributeValueNS(Node node, String namespaceURI, String localname)
    {
    	
//    	log.debug("node " + node.getNodeName() );
//    	
//    	log.debug("@" + localname);
        String value = "";
        
        Node attribute = node.getAttributes().getNamedItemNS(namespaceURI, localname);
        
//        if (attribute == null) {
//        	log.debug("@" + localname + " missing");
//        }
        
        if (attribute != null &&  ((org.w3c.dom.Attr)attribute).getNodeValue()!= null)
        {
            value = ((org.w3c.dom.Attr)attribute).getNodeValue();
        }
//        log.debug(" = " + value);

        return value;
    }

    
    
//    // Parses a string containing XML and returns a DocumentFragment
//    // containing the nodes of the parsed XML.
//    public static DocumentFragment parseXml(String fragment) {
//        // Wrap the fragment in an arbitrary element
//        fragment = "<p>"+fragment+"</p>";
//        try {
//            // Create a DOM builder and parse the fragment
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            
//            //Document d = factory.newDocumentBuilder().newDocument();
//            
//            Document d = factory.newDocumentBuilder().parse(
//                new InputSource(new StringReader(fragment)));
//        
//            // Create the document fragment node to hold the new nodes
//            DocumentFragment docfrag = d.createDocumentFragment();
//            
//            docfrag.appendChild(d.getDocumentElement());        
//            return docfrag;
//        } catch (Exception e) {
//        	log.error(e);
//            return null;
//        }
//    }    
    
	public static class HtmlSettings {
		
		private WordprocessingMLPackage wmlPackage = null;
		public void setWmlPackage(WordprocessingMLPackage wmlPackage) {
			this.wmlPackage = wmlPackage;
		}
		public WordprocessingMLPackage getWmlPackage() {
			return wmlPackage;
		}
		
		Boolean conditionalComments = Boolean.FALSE; 
		public void setConditionalComments(Boolean conditionalComments) {
			this.conditionalComments = conditionalComments;
		}
		
		Boolean fontFamilyStack = Boolean.FALSE;		
		public void setFontFamilyStack(boolean val) {
			fontFamilyStack = new Boolean(val);
		}

		String docxWikiMenu = null;		
		public void setDocxWikiMenu(String docxWikiMenu) {
			this.docxWikiMenu = docxWikiMenu;
		}
		
//		String docxWiki = null;	// edit | open	
//		public void setDocxWiki(String docxWiki) {
//			this.docxWiki = docxWiki;
//		}
//
//		String docxWikiSdtID = null;	
//		public void setDocxWikiSdtID(String docxWikiSdtID) {
//			this.docxWikiSdtID = docxWikiSdtID;
//		}
//
//		String docxWikiSdtVersion = null;	
//		public void setDocxWikiSdtVersion(String docxWikiSdtVersion) {
//			this.docxWikiSdtVersion = docxWikiSdtVersion;
//		}		
		
		String docID = null;
		public void setDocID(String docID) {
			this.docID = docID;
		}
		
		
		Mapper fontMapper = null;		
		public void setFontMapper(Mapper fontMapper) {
			this.fontMapper = fontMapper;
		}
		public Mapper getFontMapper() {
			return fontMapper;
		}
		
		// If this is set to something, images in
		// internal binary parts will be saved to this directory;
		// otherwise they won't
		private String imageDirPath = "";
		public void setImageDirPath(String imageDirPath) {
			this.imageDirPath = imageDirPath;
		}
		public String getImageDirPath() {
			return imageDirPath;
		}
		
		
		Map<String, Object> getSettings() {
			Map<String, Object> settings = new java.util.HashMap<String, Object>();
			
			settings.put("wmlPackage", wmlPackage);
			settings.put("fontFamilyStack", fontFamilyStack);
			settings.put("docxWikiMenu", docxWikiMenu);
//			settings.put("docxWiki", docxWiki);
//			settings.put("docxWikiSdtID", docxWikiSdtID);
//			settings.put("docxWikiSdtVersion", docxWikiSdtVersion);
			settings.put("docID", docID);
			settings.put("fontMapper", fontMapper);
			settings.put("imageDirPath", imageDirPath);
			settings.put("conditionalComments", conditionalComments);
			
			
			return settings;
		}
		
	}
	
    /// <summary>
    /// Internal helper class for working with Word Document conversions.
    /// </summary>
    /// <id guid="1660a411-54c2-4d53-8631-a7a2b703fcb8" />
    /// <owner alias="ROrleth" />
    public static class WordXmlPicture
    {
    	Document document;
        Node imageElement = null;
        Node linkElement = null;
    	
    	void setAttribute(String name, String value) {
    		
    		setAttribute( imageElement, name, value );
    		
    	}
    	void setAttribute(Node element, String name, String value) {
    		
        	org.w3c.dom.Attr tmpAtt = document.createAttribute(name);
        	tmpAtt.setValue(value);
        	element.getAttributes().setNamedItem(tmpAtt);
        	
        	log.debug("<" + element.getLocalName() + "@"+ name + "=\"" + value);
    		
    	}
    	
        /// <id guid="100b714f-5397-4420-958b-e03c2d021f7c" />
        /// <owner alias="ROrleth" />
    	Document createImageElement()
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
    	
        /// <id guid="233b126d-66d0-476e-bcd1-ce30bdc3e65b" />
        /// <owner alias="ROrleth" />
        void readStandardAttributes(Node fromNode)
        {
            this.id = getAttributeValue(fromNode, "id");
            this.pType = getAttributeValue(fromNode, "type");
            this.alt = getAttributeValue(fromNode, "alt");
            this.style = getAttributeValue(fromNode, "style");
        }

        /// <id guid="048da999-6fbe-41b9-9639-de0e084f3da3" />
        /// <owner alias="ROrleth" />
        void readLinkAttributes(Node fromNode)
        {
            this.targetFrame = getAttributeValue(fromNode, "tgtFrame");
            this.tooltip = getAttributeValue(fromNode, "tooltip");
        }

        private final int extentToPixelConversionFactor = 12700;

        /// <id guid="cb8dfd67-57bb-4ebc-af9d-f6062d25b9ba" />
        /// <owner alias="ROrleth" />
        void readSizeAttributes(Node fromNode)
        {
            String temp = null;
            temp = getAttributeValue(fromNode, "cx");
            if (temp !=null && !temp.equals("") )
            {
                setWidth ( Integer.parseInt(temp) / extentToPixelConversionFactor );
                	
                	//Convert.ToUInt32(temp, CultureInfo.InvariantCulture) / ExtentToPixelConversionFactor;
            }
            temp = getAttributeValue(fromNode, "cy");
            if (temp !=null && !temp.equals("") )
            {
                setHeight( Integer.parseInt(temp) / extentToPixelConversionFactor );
//                this.height = Convert.ToUInt32(temp, CultureInfo.InvariantCulture) / ExtentToPixelConversionFactor;
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
        // / <summary>
        // / The attribute of the 'v:image' node in the Word
        // / Document which coresponds to the 'w:name' attribute of
        // / the 'w:bindata' node.
        /// </summary>
        /// <remarks>
        /// This is what ties the picture reference to the image data.
        /// </remarks>
        /// <value>
        /// </value>
        /// <id guid="bacbce3f-8a98-46a7-9dcf-049acfc3c1b3" />
        /// <owner alias="ROrleth" />
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



}