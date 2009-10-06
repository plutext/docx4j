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
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConvertUtils;
import org.docx4j.convert.out.Output;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.images.WordXmlPicture;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public abstract class AbstractHtmlExporter implements Output {
	
	
	protected static Logger log = Logger.getLogger(AbstractHtmlExporter.class);
	
	
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
	
	
	public abstract void output(javax.xml.transform.Result result) throws Docx4JException;
	
	
	
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
    public abstract void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result,
    		String imageDirPath) throws Exception;
    
    public abstract void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, 
    		boolean fontFamilyStack,
    		String imageDirPath) throws Exception; 
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public abstract void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, 
    		HtmlSettings htmlSettings) throws Exception;     

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
    
    

    
    /**
	 * The method used by the XSLT extension function during HTML export.
	 * 
	 * If there is no number, it returns an empty span element.
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
        	ResultTriple triple = org.docx4j.model.listnumbering.Emulator.getNumber(
        			wmlPackage, pStyleVal, numId, levelId);   
        	
//    		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
//    			"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        	
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			DocumentFragment docfrag = document.createDocumentFragment();

			if (triple==null) {
        		log.info("computed number ResultTriple was null");
    			Node spanElement = document.createElement("span");

    			// It would be nice to include a comment in the
    			// output HTML, but Sun's Xalan copy-of ignores it.
    			
//        		Comment c = document.createComment("computed number ResultTriple was null");
//        		spanElement.appendChild(c);
    			    			
    			document.appendChild(spanElement);
        		docfrag.appendChild(document.getDocumentElement());
    			return docfrag;
        	}

	    	if (triple.getNumString()==null) {
	    		log.error("computed NumString was null!");
    			Node spanElement = document.createElement("span");
    			
    			// It would be nice to include a comment in the
    			// output HTML, but Sun's Xalan copy-of ignores it.
    			
//        		Comment c = document.createComment("computed number triple.getNumString() was null");
//        		spanElement.appendChild(c);
        		    			
    			document.appendChild(spanElement);
    			docfrag.appendChild(document.getDocumentElement());
    			return docfrag;
	    	}
			
			Node spanElement = document.createElement("span");			
			document.appendChild(spanElement);
			Text number = document.createTextNode( triple.getNumString() );
			spanElement.appendChild(number);			
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
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
		
		String docxWiki = null;	// edit | open	
		public void setDocxWiki(String docxWiki) {
			this.docxWiki = docxWiki;
		}

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
			settings.put("docxWiki", docxWiki);
//			settings.put("docxWikiSdtID", docxWikiSdtID);
//			settings.put("docxWikiSdtVersion", docxWikiSdtVersion);
			settings.put("docID", docID);
			settings.put("fontMapper", fontMapper);
			settings.put("imageDirPath", imageDirPath);
			settings.put("conditionalComments", conditionalComments);
			
			
			return settings;
		}
		
	}
	


}