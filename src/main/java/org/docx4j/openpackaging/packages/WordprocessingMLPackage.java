/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.openpackaging.packages;


import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.docx4j.fonts.Substituter;
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
import org.docx4j.openpackaging.parts.relationships.Namespaces;







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
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public void html(javax.xml.transform.Result result, boolean fontFamilyStack) throws Exception {
    	
    	/*
    	 * Given that word2html.xsl is freely available, we use the second
    	 * approach.
    	 * 
    	 * The question then is how the stylesheet is made to work with
    	 * our main document and style definition parts.
    	 * 
    	 * I've adapted the stylesheet to process the
    	 * pck:package/pck:part stuff emitted by Word 2007.
    	 * 
    	 */
    	
		// so, put the 2 parts together into a single document 
    	// The JAXB object org.docx4j.wml.Package is
    	// custom built for this purpose.
    	
    	// Create a org.docx4j.wml.Package object
    	org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
    	org.docx4j.wml.Package pkg = factory.createPackage();
    	
    	// Set its parts
    	
    	// .. the main document part
    	org.docx4j.wml.Package.Part pkgPartDocument = factory.createPackagePart();
    	    	
		MainDocumentPart documentPart = getMainDocumentPart(); 
		
    	pkgPartDocument.setName(documentPart.getPartName().getName());
    	pkgPartDocument.setContentType(documentPart.getContentType() );
		
    	org.docx4j.wml.Package.Part.XmlData XmlDataDoc = factory.createPackagePartXmlData();
    	
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		
		XmlDataDoc.setDocument(wmlDocumentEl);
		pkgPartDocument.setXmlData(XmlDataDoc);
		pkg.getPart().add(pkgPartDocument);
				
    	// .. the style part
    	org.docx4j.wml.Package.Part pkgPartStyles = factory.createPackagePart();

    	org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart stylesPart = documentPart.getStyleDefinitionsPart();
    	
    	pkgPartDocument.setName(stylesPart.getPartName().getName());
    	pkgPartDocument.setContentType(stylesPart.getContentType() );
    	
    	org.docx4j.wml.Package.Part.XmlData XmlDataStyles = factory.createPackagePartXmlData();
    	
    	org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)stylesPart.getJaxbElement();
    	
		XmlDataStyles.setStyles(styles);
		pkgPartStyles.setXmlData(XmlDataStyles);
		pkg.getPart().add(pkgPartStyles);    	
    	
    	// Now marshall it
		JAXBContext jc = Context.jc;
		Marshaller marshaller=jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();

		marshaller.marshal(pkg, doc);
		
		log.info("wordDocument created for PDF rendering!");

/*		
 * 		We want to use plain old Xalan J, not xsltc
 * 
 * 		Following would not be necessary provided Xalan is on the classpath
 * 
		System.setProperty("javax.xml.transform.TransformerFactory", "FQCN");

		examples of FQCN:
		
		  org.apache.xalan.processor.TransformerFactoryImpl (this is the one we want)
		  com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl
		  org.apache.xalan.xsltc.trax.TransformerFactoryImpl
		  net.sf.saxon.TransformerFactoryImpl
		  
		  (unfortunately, there is no com.sun.org.apache.xalan.processor.TransformerFactoryImpl,
		   so we have to bundle xalan jar, which is 2.7 MB
		   
		   But we can make it smaller:
		   
		   	org/apache/xalan/lib$ rm sql -rf
		    org/apache/xalan$ rm xsltc -rf

          That gets us from 2.7 MB to 1.85 MB.
          
          But Sun already has:
          
			com.sun.org.apache.xpath;			
			com.sun.org.apache.xml.internal.dtm;			
			com.sun.org.apache.xalan.internal.extensions|lib|res|templates
		   
		  So why not refactor Xalan and leave all that out as well?
		  
		  
		HOWEVER, docx4all encounters http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6396599
		
			java.util.prefs.FileSystemPreferences syncWorld
			WARNING: Couldn't flush user prefs: java.util.prefs.BackingStoreException: java.lang.IllegalArgumentException: Not supported: indent-number
		
		every 30 seconds

		The workaround implemented is to remove META-INF/services from the xalan jar 
		to prevent xalan being picked up as the default provider for jaxp transform,
		so we have to use it explicitly.
		
		.. which means 

			System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
			
*/		
		
		
		javax.xml.transform.TransformerFactory tfactory = javax.xml.transform.TransformerFactory.newInstance();
		String originalFactory = tfactory.getClass().getName();
		System.out.println("original TransformerFactory: " + originalFactory);
		// com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl resolves the syncWorld problem
		// net.sf.saxon.TransformerFactoryImpl is no good.
		
		System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
		
		// Now transform this into XHTML
		tfactory = javax.xml.transform.TransformerFactory.newInstance();
		javax.xml.transform.dom.DOMSource domSource = new javax.xml.transform.dom.DOMSource(doc);

		// Get the xslt file
		java.io.InputStream is = null;
			// Works in Eclipse - note absence of leading '/'
			is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/openpackaging/packages/wordml2html-2007.xslt");
				
		// Use the factory to create a template containing the xsl file
		javax.xml.transform.Templates template = tfactory.newTemplates(
				new javax.xml.transform.stream.StreamSource(is));
		// Use the template to create a transformer
		javax.xml.transform.Transformer xformer = template.newTransformer();
		
		
		// Finished with the factory, so set it back again!
		// The "Not supported: indent-number" problem will only occur if a user creates 
		// a new document during the time between these 2 calls to setProperty
		// (and syncWorld is called?)
		System.setProperty("javax.xml.transform.TransformerFactory", originalFactory);
		
		if (!xformer.getClass().getName().equals("org.apache.xalan.transformer.TransformerImpl")) {
			log.error("Detected " + xformer.getClass().getName() 
					+ ", but require org.apache.xalan.transformer.TransformerImpl. " +
							"Ensure Xalan 2.7.0 is on your classpath!" );
		}
		// com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl won't work
		// with our extension function.

		
		// 3.  Ensure that the font names in the XHTML have been mapped to these matches
		//     possibly via an extension function in the XSLT
		if (fontSubstituter==null) {
			setFontSubstituter(new Substituter());
		}
		xformer.setParameter("substituterInstance", fontSubstituter);
		xformer.setParameter("fontFamilyStack", fontFamilyStack);
		
		//DEBUGGING 
		// use the identity transform if you want to send wordDocument;
		// otherwise you'll get the XHTML
		//javax.xml.transform.Transformer xformer = tfactory.newTransformer();
		
		xformer.transform(domSource, result);

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
    	 * For now, I've just edited it a little to accept our parts wrapped
    	 * in a <w:wordDocument> element.  Since that's a completely
    	 * arbitrary format, it may be better in due course to process
    	 * pck:package/pck:part
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
		Iterator fontMappingsIterator = fontMappings.entrySet().iterator();
	    while (fontMappingsIterator.hasNext()) {
	        Map.Entry pairs = (Map.Entry)fontMappingsIterator.next();
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	pairs = (Map.Entry)fontMappingsIterator.next();
	        }
	        
	        String fontName = (String)pairs.getKey();
	        Substituter.FontMapping fm = (Substituter.FontMapping)pairs.getValue();
	        
			log.info("Substituting " + fontName + " with " + fm.getTripletName() + " from " + fm.getEmbeddedFile() );
			if (fm.getEmbeddedFile()!=null) {
				try {
					renderer.getFontResolver().addFont(fm.getEmbeddedFile(), true);
				} catch (com.lowagie.text.DocumentException e) {
					log.error("Shouldn't happen - should have been detected upstream ... " + e.getMessage()); 
				} catch (java.io.IOException e) {
				
				/* 
				 * [AWT-EventQueue-0] INFO  packages.WordprocessingMLPackage - Substituting symbol with standardsymbolsl from file:/usr/share/fonts/type1/gsfonts/s050000l.pfb 
java.io.IOException: Unsupported font type
	at org.xhtmlrenderer.pdf.ITextFontResolver.addFont(ITextFontResolver.java:199)
				 */
					log.warn("Shouldn't happen - should have been detected upstream ... " +  e.getMessage() + ": " + fm.getEmbeddedFile()); 
				}
			} else {
				log.warn("Can't addFont for: " + fontName); 
			}
	    }
		
		renderer.setDocument(xhtmlDoc, null);
		renderer.layout();
		
		renderer.createPDF(os);
		
	}	
	

	public static WordprocessingMLPackage createTestPackage() throws InvalidFormatException {
		
				
		// Create a package
		WordprocessingMLPackage wmlPack = new WordprocessingMLPackage();

		// Create main document part
		Part wordDocumentPart = new org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart();		
		
		// Create main document part content
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();

		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("Hello world, from docx4j");

		org.docx4j.wml.R  run = factory.createR();
		run.getRunContent().add(t);		
		
		org.docx4j.wml.P  para = factory.createP();
		para.getParagraphContent().add(run);
		
		org.docx4j.wml.Body  body = factory.createBody();
		body.getBlockLevelElements().add(para);
		
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
	
}
