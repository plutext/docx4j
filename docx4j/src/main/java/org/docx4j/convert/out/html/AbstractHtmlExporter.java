package org.docx4j.convert.out.html;

import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.convert.out.Output;
import org.docx4j.fonts.Mapper;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.run.Font;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
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
	 *  stacks.  
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

			Element spanElement = document.createElement("span");
			
			String styleVal = "";
			
    		if (triple.getIndent()!=null) {
    			Indent indent = new Indent(triple.getIndent());
				styleVal = indent.getCssProperty();
    		}
			
    		// Set the font
    		if (triple.getNumFont()!=null) {
    			String font = Font.getPhysicalFont(wmlPackage, triple.getNumFont() );
    			if (font!=null) {
    				styleVal += Property.composeCss(Font.CSS_NAME, font );
    			}
    		}

    		if (!styleVal.equals("") ) {
				spanElement.setAttribute("style", styleVal);
    		}
    		if (triple.getBullet()!=null ) {
    			spanElement.setTextContent(triple.getBullet() + " " );    						
    		} else if (triple.getNumString()==null) {
	    		log.error("computed NumString was null!");
    			spanElement.setTextContent("?");    						
    			
    			// It would be nice to include a comment in the
    			// output HTML, but Sun's Xalan copy-of ignores it.
    			
//        		Comment c = document.createComment("computed number triple.getNumString() was null");
//        		spanElement.appendChild(c);
	    	} else {
				Text number = document.createTextNode( triple.getNumString() + " " );
				spanElement.appendChild(number);				    		
	    	}
			
			document.appendChild(spanElement);
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

		private Map<String, Object> settings;
		
		public HtmlSettings() {
			settings = new java.util.HashMap<String, Object>();
			settings.put("conditionalComments", Boolean.FALSE);
			settings.put("fontFamilyStack",     Boolean.FALSE);
			settings.put("imageDirPath", "");
			
//			settings.put("docxWikiSdtID", docxWikiSdtID);
//			settings.put("docxWikiSdtVersion", docxWikiSdtVersion);
			
		}
		
		public Map<String, Object> getSettings() {
			return settings;
		}
		
		public void setWmlPackage(OpcPackage wmlPackage) {
			settings.put("wmlPackage", wmlPackage);
		}
		public OpcPackage getWmlPackage() {
			return (OpcPackage)settings.get("wmlPackage");
		}
		
		public void setConditionalComments(Boolean conditionalComments) {
			settings.put("conditionalComments", conditionalComments);
		}
		
		public void setFontFamilyStack(boolean val) {
			settings.put("fontFamilyStack", new Boolean(val));
		}

		public void setDocxWikiMenu(String docxWikiMenu) {
			settings.put("docxWikiMenu", docxWikiMenu);
		}
		
		public void setDocxWiki(String docxWiki) { // edit | open
			settings.put("docxWiki", docxWiki);
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
		
		public void setDocID(String docID) {
			settings.put("docID", docID);
		}
				
		public void setFontMapper(Mapper fontMapper) {
			settings.put("fontMapper", fontMapper);
		}
		public Mapper getFontMapper() {
			return (Mapper)settings.get("fontMapper");
		}
		
		// If this is set to something, images in
		// internal binary parts will be saved to this directory;
		// otherwise they won't
		public void setImageDirPath(String imageDirPath) {
			settings.put("imageDirPath", imageDirPath);
		}
		public String getImageDirPath() {
			return (String)settings.get("imageDirPath");
		}
				
	}
	


}