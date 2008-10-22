/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.packages;


import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.convert.out.xmlPackage.XmlPackage;
import org.docx4j.fonts.Substituter;
import org.docx4j.fonts.FontUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import com.lowagie.text.pdf.BaseFont;







/**
 * @author jharrop
 *
 */
public class WordprocessingMLPackage extends Package {
	
	// What is a Word document these days?
	//
	// Well, a package is a logical entity which holds a collection of parts	
	// And a word document is exactly a WordProcessingML package	
	// Which has a Main Document Part, and optionally, a Glossary Document Part

	/* So its a Word doc if:
	 * 1. _rels/.rels tells you where to find an office document
	 * 2. [Content_Types].xml tells you that office document is   
	 *    of content type application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml
	
	 * A minimal docx has:
	 * 
	 * [Content_Types].xml containing:
	 * 1. <Default Extension="rels" ...
	 * 2. <Override PartName="/word/document.xml"...
	 * 
	 * _rels/.rels with a target for word/document.xml
	 * 
	 * word/document.xml
	 */
	
	protected static Logger log = Logger.getLogger(WordprocessingMLPackage.class);
		
	
	// Main document
	protected MainDocumentPart mainDoc;
	
	// (optional) Glossary document
	protected GlossaryDocumentPart glossaryDoc;
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public WordprocessingMLPackage() {
		super();
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT));
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public WordprocessingMLPackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT));
	}
	
	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * from an existing File.
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public static WordprocessingMLPackage load(java.io.File docxFile) throws Docx4JException {
		
		LoadFromZipFile loader = new LoadFromZipFile();
		return (WordprocessingMLPackage)loader.get(docxFile);		
	}

	/**
	 * Convenience method to save a WordprocessingMLPackage
	 * to a File.
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public void save(java.io.File docxFile) throws Docx4JException {
		
		SaveToZipFile saver = new SaveToZipFile(this); 
		saver.save(docxFile);
	}
	
	
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			log.info("Set shortcut for docPropsCorePart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			log.info("Set shortcut for docPropsExtendedPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_CUSTOM)) {
			docPropsCustomPart = (DocPropsCustomPart)part;
			log.info("Set shortcut for docPropsCustomPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.DOCUMENT)) {
			mainDoc = (MainDocumentPart)part;
			log.info("Set shortcut for mainDoc");
			return true;			
		} else {	
			return false;
		}
	}
	
	public MainDocumentPart getMainDocumentPart() {
		return mainDoc;
	}
	
    
    /**
     * Use an XSLT to alter the contents of this package.
     * The output of the transformation must be valid
     * pck:package/pck:part format, as emitted by Word 2007.
     * 
     * @param xslt
     * @param transformParameters
     * @throws Exception
     */    
    public void transform(java.io.InputStream xslt, 
			  Map<String, Object> transformParameters) throws Exception {

    	// Prepare in the input document
    	
		XmlPackage worker = new XmlPackage(this);
		org.docx4j.xmlPackage.Package pkg = worker.get();
    	
		JAXBContext jc = Context.jcXmlPackage;
		Marshaller marshaller=jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();
		marshaller.marshal(pkg, doc);
    	
		javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(jc );
		
		// Perform the transformation
		org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
		

		org.docx4j.wml.Package wmlPackageEl = (org.docx4j.wml.Package)result.getResult(); 

		org.docx4j.wml.Document wmlDocument = null;
		org.docx4j.wml.Styles wmlStyles = null;
		for (org.docx4j.wml.Package.Part p : wmlPackageEl.getPart() ) {
			
			if (p.getXmlData().getDocument()!= null) {
				wmlDocument = p.getXmlData().getDocument();
			}				
			if (p.getXmlData().getStyles()!= null) {
				wmlStyles = p.getXmlData().getStyles();
			}				
		}

		// This code assumes all the existing rels etc of 
		// the existing main document part are still relevant.
		if (wmlDocument==null) {
			log.warn("Couldn't get main document part from package transform result!");			
		} else {
			this.getMainDocumentPart().setJaxbElement(wmlDocument);
		}	
				
		if (wmlStyles==null) {
			log.warn("Couldn't get style definitions part from package transform result!");			
		} else {
			this.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement(wmlStyles);
		}
    	
    }
    
    public void filter( FilterSettings filterSettings ) throws Exception {

		java.io.InputStream xslt 
			= org.docx4j.utils.ResourceUtils.getResource(
					"org/docx4j/openpackaging/packages/filter.xslt");
    	
    	transform(xslt, filterSettings.getSettings() );
    	
    }

	/** Create an html version of the document, using CSS font family
	 *  stacks.  This is appropriate if the HTML is intended for
	 *  viewing in a web browser, rather than an intermediate step
	 *  on the way to generating PDF output. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public void html(javax.xml.transform.Result result) throws Exception {

    	html(result, true);
    }

    public void html(javax.xml.transform.Result result, boolean fontFamilyStack) throws Exception {

		// Prep parameters
    	HtmlSettings htmlSettings = new HtmlSettings();
    	htmlSettings.setFontFamilyStack(fontFamilyStack);
    	
		html(result, htmlSettings);
    }
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public void html(javax.xml.transform.Result result, HtmlSettings htmlSettings) throws Exception {
    	
    	/*
    	 * Given that word2html.xsl is freely available, use a
    	 * version of it adapted to process the
    	 * pck:package/pck:part stuff emitted by Word 2007.
    	 * 
    	 */    	
		XmlPackage worker = new XmlPackage(this);
		org.docx4j.xmlPackage.Package pkg = worker.get();
    	
		JAXBContext jc = Context.jcXmlPackage;
		Marshaller marshaller=jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();

		marshaller.marshal(pkg, doc);
		
		log.info("wordDocument created for PDF rendering!");


		// Get the xslt file - Works in Eclipse - note absence of leading '/'
		java.io.InputStream xslt = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/openpackaging/packages/wordml2html-2007.xslt");
		
		// Prep parameters
		if (htmlSettings==null) {
			htmlSettings = new HtmlSettings();
			// ..Ensure that the font names in the XHTML have been mapped to these matches
			//     possibly via an extension function in the XSLT
		}
		
		if (htmlSettings.getFontSubstituter()==null) {
			if (fontSubstituter==null) {
				log.debug("Creating new Substituter.");
				setFontSubstituter(new Substituter());
			} else {
				log.debug("Using existing Substituter.");
			}
			htmlSettings.setFontSubstituter(fontSubstituter);
		}
		
		// Now do the transformation
		org.docx4j.XmlUtils.transform(doc, xslt, htmlSettings.getSettings(), result);
		
		log.info("wordDocument transformed to xhtml ..");
    	
    }
    
    
    
    
    public void setFontSubstituter(Substituter fs) throws Exception {
    	if (fs == null) {
    		throw new IllegalArgumentException("Font Substituter cannot be null.");
    	}
		// 1.  Get a list of all the fonts in the document
		java.util.Map fontsInUse = this.getMainDocumentPart().fontsInUse();
		
		// 2.  For each font, find the closest match on the system (use OO's VCL.xcu to do this)
		//     - do this in a general way, since docx4all needs this as well to display fonts		
		fontSubstituter = fs;
		org.docx4j.wml.Fonts fonts = null;
		FontTablePart fontTablePart= this.getMainDocumentPart().getFontTablePart();	
		
		if (fontTablePart==null) {
			log.warn("FontTable missing; creating default part.");
			fontTablePart= new org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart();
			fontTablePart.unmarshalDefaultFonts();
			fontTablePart.processEmbeddings();
		}
		
		fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();
		fontSubstituter.populateFontMappings(fontsInUse, fonts);    	
    	
    }

    private Substituter fontSubstituter;
    
	/** Create a pdf version of the document. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * 
	 * */     
    public void pdf(OutputStream os) throws Exception {
    	
    	/*
    	 * There are 2 broad approaches we could use to render the document
    	 * as a PDF:
    	 * 
    	 * 1.  XSL-FO
    	 * 2.  XHTML to PDF
    	 * 
    	 * Given that a word2html.xsl is already freely available, we use 
    	 * the second approach.
    	 * 
    	 * The question then is how the stylesheet is made to work with
    	 * our main document and style definition parts.
    	 * 
    	 * it processes pck:package/pck:part
    	 * 
    	 */
				
        // Put the html in result
		org.w3c.dom.Document xhtmlDoc = org.docx4j.XmlUtils.neww3cDomDocument();
		javax.xml.transform.dom.DOMResult result = new javax.xml.transform.dom.DOMResult(xhtmlDoc);
		html(result, false); // false -> don't use HTML fonts.
				
		// Now render the XHTML
		org.xhtmlrenderer.pdf.ITextRenderer renderer = new org.xhtmlrenderer.pdf.ITextRenderer();
				
		// 4.  Use addFont code like that below as necessary for the fonts
		
			// See https://xhtmlrenderer.dev.java.net/r7/users-guide-r7.html#xil_32
		org.xhtmlrenderer.extend.FontResolver resolver = renderer.getFontResolver();		
				
		Map fontMappings = fontSubstituter.getFontMappings();
		Map fontsInUse = this.getMainDocumentPart().fontsInUse();
		Iterator fontMappingsIterator = fontsInUse.entrySet().iterator();
		while (fontMappingsIterator.hasNext()) {
	        Map.Entry pairs = (Map.Entry)fontMappingsIterator.next();
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	pairs = (Map.Entry)fontMappingsIterator.next();
	        }
	        
	        String fontName = (String)pairs.getKey();
	        embed(renderer, Substituter.normalise(fontName), fontMappings);	        
	        // For any font we embed, also embed the bold, italic, and bold italic substitute
	        // .. at present, we can't tell which of these forms are actually used, so add them all
	        embed(renderer, Substituter.normalise(fontName + Substituter.BOLD), fontMappings);
	        embed(renderer, Substituter.normalise(fontName + Substituter.ITALIC), fontMappings);
	        embed(renderer, Substituter.normalise(fontName + Substituter.BOLD_ITALIC), fontMappings);
	        
	    }
		
	    // TESTING
//	    xhtmlDoc = org.docx4j.XmlUtils.neww3cDomDocument();
//	    try {
//			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			dbf.setNamespaceAware(true);
//			dbf.newDocumentBuilder().newDocument();
//	    	
//			xhtmlDoc = dbf.newDocumentBuilder().parse(new File("C:\\Users\\jharrop\\workspace\\docx4all\\sample-docs\\comic.html"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        } 	    
	    
		renderer.setDocument(xhtmlDoc, null);
		renderer.layout();
		
		renderer.createPDF(os);
		
	}
	/**
	 * @param renderer
	 * @param fontName
	 * @param fm
	 */
	private void embed(org.xhtmlrenderer.pdf.ITextRenderer renderer,
			String fontName, Map fontMappings) {
		Substituter.FontMapping fm = (Substituter.FontMapping)fontMappings.get( fontName );
		
		if (fm == null) {
			log.warn("No mapping found for: " + fontName);
		} else if (fm.getPhysicalFont()!=null) {
			try {
				if (fm.getPhysicalFont().getEmbeddedFile().endsWith(".pfb")) {
					
//						String afm = fm.getPhysicalFont().getEmbeddedFile().substring(5, fm.getPhysicalFont().getEmbeddedFile().length()-4 ) + ".afm";  // drop the 'file:'
					String afm = FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile());
					afm = afm.substring(0, afm.length()-4 ) + ".afm";  // drop the 'file:'
					log.info("Looking for: " + afm);
					
					// Given the check in substituter, we expect to find one or the other.
					File f = new File(afm);
			        if (f.exists()) {				
			        	log.info("Got it");
			        	renderer.getFontResolver().addFont(afm, BaseFont.CP1252, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile()));  // drop the 'file:'	
						log.info("Substituting " + fontName + " with embedding " + fm.getPhysicalFont().getFamilyName() + " from " + fm.getPhysicalFont().getEmbeddedFile() );
			        } else {
			        	// Should we be doing afm first, or pfm?
						String pfm = FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile());
						pfm = pfm.substring(0, pfm.length()-4 ) + ".pfm";  // drop the 'file:'
						log.info("Looking for: " + pfm);
						f = new File(pfm);
				        if (f.exists()) {				
				        	log.info("Got it");
				        	renderer.getFontResolver().addFont(pfm, BaseFont.CP1252, true, FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile() ));  // drop the 'file:'
							log.info("Substituting " + fontName + " with embedding " + fm.getPhysicalFont().getFamilyName() + " from " + fm.getPhysicalFont().getEmbeddedFile() );
				        } else {
				        	// Shouldn't happen.
				        	log.error("Couldn't find afm or pfm corresponding to " + fm.getPhysicalFont().getEmbeddedFile());
				        }
			        }
				} else {				
					renderer.getFontResolver().addFont(FontUtils.pathFromURL(fm.getPhysicalFont().getEmbeddedFile()), true);
					log.info("Substituting " + fontName + " with embedding " + fm.getPhysicalFont().getFamilyName() + " from " + fm.getPhysicalFont().getEmbeddedFile() );
				}
			} catch (java.io.IOException e) {
			
			/* 
			 * [AWT-EventQueue-0] INFO  packages.WordprocessingMLPackage - Substituting symbol with standardsymbolsl from file:/usr/share/fonts/type1/gsfonts/s050000l.pfb 
java.io.IOException: Unsupported font type
at org.xhtmlrenderer.pdf.ITextFontResolver.addFont(ITextFontResolver.java:199)

.pfb not supported, even with iText 2.0.8

			 */
				e.printStackTrace();
				log.warn("Shouldn't happen - should have been detected upstream ... " +  e.getMessage() + ": " + fm.getPhysicalFont().getEmbeddedFile()); 
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Shouldn't happen - should have been detected upstream ... " + e.getMessage()); 
			}
		} else {
			log.warn("Can't addFont for: " + fontName); 
		}
	}	
	

	public static WordprocessingMLPackage createPackage() throws InvalidFormatException {
		
				
		// Create a package
		WordprocessingMLPackage wmlPack = new WordprocessingMLPackage();

		// Create main document part
		Part wordDocumentPart = new org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart();		
		
		// Create main document part content
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		
		org.docx4j.wml.Body  body = factory.createBody();
		
		org.docx4j.wml.Document wmlDocumentEl = factory.createDocument();
		wmlDocumentEl.setBody(body);
				
		// Put the content in the part
		((org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart)wordDocumentPart).setJaxbElement(wmlDocumentEl);
						
		// Add the main document part to the package relationships
		// (creating it if necessary)
		wmlPack.addTargetPart(wordDocumentPart);
				
		// Create a styles part
		Part stylesPart = new org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart();
		try {
			((org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart) stylesPart)
					.unmarshalDefaultStyles();
			
			// Add the styles part to the main document part relationships
			// (creating it if necessary)
			wordDocumentPart.addTargetPart(stylesPart); // NB - add it to main doc part, not package!			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();			
		}
		// Return the new package
		return wmlPack;
		
	}
	
	public static class FilterSettings {
		
		Boolean removeProofErrors = Boolean.FALSE;		
		public void setRemoveProofErrors(boolean val) {
			removeProofErrors = new Boolean(val);
		}

		Boolean removeContentControls = Boolean.FALSE;		
		public void setRemoveContentControls(boolean val) {
			removeContentControls = new Boolean(val);
		}
		
		Boolean removeRsids = Boolean.FALSE;		
		public void setRemoveRsids(boolean val) {
			removeRsids = new Boolean(val);
		}
		
		Boolean tidyForDocx4all = Boolean.FALSE;		
		public void setTidyForDocx4all(boolean val) {
			tidyForDocx4all = new Boolean(val);
		}
		
		
		Map<String, Object> getSettings() {
			Map<String, Object> settings = new java.util.HashMap<String, Object>();
			
			settings.put("removeProofErrors", removeProofErrors);
			settings.put("removeContentControls", removeContentControls);
			settings.put("removeRsids", removeRsids);
			settings.put("tidyForDocx4all", tidyForDocx4all);
			
			return settings;
		}
		
		
	}

	public static class HtmlSettings {
		
		Boolean fontFamilyStack = Boolean.FALSE;		
		public void setFontFamilyStack(boolean val) {
			fontFamilyStack = new Boolean(val);
		}
		
		String docxWiki = null;	// edit | open	
		public void setDocxWiki(String docxWiki) {
			this.docxWiki = docxWiki;
		}

		String docxWikiSdtID = null;	
		public void setDocxWikiSdtID(String docxWikiSdtID) {
			this.docxWikiSdtID = docxWikiSdtID;
		}
		
		String docID = null;
		public void setDocID(String docID) {
			this.docID = docID;
		}
		
		
		Substituter fontSubstituter = null;		
		public void setFontSubstituter(Substituter fontSubstituter) {
			this.fontSubstituter = fontSubstituter;
		}
		public Substituter getFontSubstituter() {
			return fontSubstituter;
		}
		
		
		Map<String, Object> getSettings() {
			Map<String, Object> settings = new java.util.HashMap<String, Object>();
			
			settings.put("fontFamilyStack", fontFamilyStack);
			settings.put("docxWiki", docxWiki);
			settings.put("docxWikiSdtID", docxWikiSdtID);
			settings.put("docID", docID);
			settings.put("substituterInstance", fontSubstituter);
			
			return settings;
		}
		
	}
	
}
