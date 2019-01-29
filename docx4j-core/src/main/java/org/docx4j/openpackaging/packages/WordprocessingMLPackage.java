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


import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.DocumentModel;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Styles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;







/**
 * @author jharrop
 *
 */
public class WordprocessingMLPackage extends OpcPackage {
	
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
	
	protected static Logger log = LoggerFactory.getLogger(WordprocessingMLPackage.class);
		
	
	// Main document
	protected MainDocumentPart mainDoc;
	
	// (optional) Glossary document
	protected GlossaryDocumentPart glossaryDoc;
	
	private ProtectDocument documentProtectionSettings = new ProtectDocument(this);
	public ProtectDocument getProtectionSettings() {
		return documentProtectionSettings;
	}
	
	
	private DocumentModel documentModel;
	public DocumentModel getDocumentModel() {
		if (documentModel==null) {
			documentModel = new DocumentModel(this);
		}
		return documentModel;
	}
	
	private HeaderFooterPolicy headerFooterPolicy;	
	@Deprecated	
	public HeaderFooterPolicy getHeaderFooterPolicy() {
		int last = getDocumentModel().getSections().size();
		if (last>0) {
			// Should always be the case, since we add one,
			// even if the document contains no sectPr
			return getDocumentModel().getSections().get(last-1).getHeaderFooterPolicy();
		} else {
			log.error("Unexpected - zero sections?!");
			return null;
		}
	}
	
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
	 * from an existing File (.docx zip or .xml Flat OPC).
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public static WordprocessingMLPackage load(java.io.File docxFile) throws Docx4JException {
		
		return (WordprocessingMLPackage)OpcPackage.load(docxFile);
	}

	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * from an existing stream(.docx zip or .xml Flat OPC).
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public static WordprocessingMLPackage load(InputStream is) throws Docx4JException {
		
		return (WordprocessingMLPackage)OpcPackage.load(is);
	}
	
	
	public boolean setPartShortcut(Part part, String relationshipType) {
		
//		log.info("setPartShortcut for part " + part.getClass().getName() );
		
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			log.debug("Set shortcut for docPropsCorePart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			log.debug("Set shortcut for docPropsExtendedPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_CUSTOM)) {
			docPropsCustomPart = (DocPropsCustomPart)part;
			log.debug("Set shortcut for docPropsCustomPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.DOCUMENT)) {
			mainDoc = (MainDocumentPart)part;
			log.debug("Set shortcut for mainDoc");
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
     * @param is
     * @param transformParameters
     * @throws Exception
     */    
    public void transform(Templates xslt,
			  Map<String, Object> transformParameters) throws Exception {

    	// Prepare in the input document
    	
		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(this);
		org.docx4j.xmlPackage.Package pkg = worker.get();
    	
		JAXBContext jc = Context.jcXmlPackage;
		Marshaller marshaller=jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();
		marshaller.marshal(pkg, doc);
    			
//		javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(jc );
		
		// Use constructor which takes Unmarshaller, rather than JAXBContext,
		// so we can set JaxbValidationEventHandler
		Unmarshaller u = jc.createUnmarshaller();
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
		javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(u );
		
		// Perform the transformation		
		org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
		

//		javax.xml.bind.JAXBElement je = (javax.xml.bind.JAXBElement)result.getResult();
//		org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)je.getValue();
		org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)XmlUtils.unwrap(result.getResult());
		
		org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl); 
		
		ContentTypeManager ctm = new ContentTypeManager();
		
		WordprocessingMLPackage tmpPkg = (WordprocessingMLPackage)xmlPackage.get(); 
		
		Part tmpDocPart = tmpPkg.getMainDocumentPart();
		Part tmpStylesPart = tmpPkg.getMainDocumentPart().getStyleDefinitionsPart();
		
		// This code assumes all the existing rels etc of 
		// the existing main document part are still relevant.
//		if (wmlDocument==null) {
//			log.warn("Couldn't get main document part from package transform result!");			
//		} else {
//			this.getMainDocumentPart().setJaxbElement(wmlDocument);
//		}	
		this.getMainDocumentPart().setJaxbElement(
				((JaxbXmlPart<Document>) tmpDocPart).getJaxbElement() );
//				
//		if (wmlStyles==null) {
//			log.warn("Couldn't get style definitions part from package transform result!");			
//		} else {
//			this.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement(wmlStyles);
//		}
		this.getMainDocumentPart().getStyleDefinitionsPart(true).setJaxbElement(
				((JaxbXmlPart<Styles>) tmpStylesPart).getJaxbElement() );
    	
    }
    
    @Deprecated
	// since its questionable whether this
	// is important enough to live in WordprocessingMLPackage,
	// and in any case probably should be replaced with a TraversalUtil
	// approach (which wouldn't involve marshal/unmarshall, and 
	// so should be more efficient).    
    public void filter( FilterSettings filterSettings ) throws Exception {

    	if (filterTemplate==null) { // first use
			Source xsltSource = new StreamSource(
				org.docx4j.utils.ResourceUtils.getResource(
						"org/docx4j/openpackaging/packages/filter.xslt"));
			filterTemplate = XmlUtils.getTransformerTemplate(xsltSource);
    	}
    	transform(filterTemplate, filterSettings.getSettings() );
    	
    }

    static Templates filterTemplate;
    
/* There should be a mapper per document,
 * but PhysicalFonts should be system wide.
 * 
 * The only way PhysicalFonts will change
 * is if fonts are added/removed while
 * docx4j is executing (which can happen eg if an
 * obfuscated font part is read)
 */

    public void setFontMapper(Mapper fm) throws Exception {
    	setFontMapper( fm,  true);
    }
    
    /**
     * @param fm
     * @param populate
     * @throws Exception
     * 
     * @since 3.0.1
     */
    public void setFontMapper(Mapper fm, boolean populate) throws Exception {
    	log.debug("setFontMapper invoked");
    	
    	/* The two font mappers included in docx4j (IdentityPlusMapper
    	 * and BestMatchingMapper), both populate physical fonts statically
    	 * when they are constructed.
    	 * 
    	 * So by now, we know what physical fonts exist on the 
    	 * system.
    	 * 
    	 * A mapper is per document.
    	 * 
    	 * This method:
    	 * - adds fonts embedded in the docx to the mapper
    	 * - optionally, populates the mapper for fonts actually used in the docx,  
    	 * 
    	 * Since an embedded font is document specific, it must NOT be added to
    	 * PhysicalFonts! It is ok to have a PhysicalFont object representing it,
    	 * but that should be stored in the document specific mapper object.   
    	 */
    	
    	if (fm == null) {
    		throw new IllegalArgumentException("Font Substituter cannot be null.");
    	}
		fontMapper = fm;
		org.docx4j.wml.Fonts fonts = null;

		// 1.  Get a list of all the fonts in the document
		Set<String> fontsInUse = this.getMainDocumentPart().fontsInUse();
		
//		if ( fm.getClass().getName().equals("org.docx4j.fonts.BestMatchingMapper") ) {
			
			// 2.  For each font, find the closest match on the system (use OO's VCL.xcu to do this)
			//     - do this in a general way, since docx4all needs this as well to display fonts		
			FontTablePart fontTablePart= this.getMainDocumentPart().getFontTablePart();				
			if (fontTablePart==null) {
				log.warn("FontTable missing; creating default part.");
				fontTablePart= new org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart();
				fontTablePart.unmarshalDefaultFonts();
			}
			
			fontTablePart.processEmbeddings(fontMapper);
			
			fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();
//		}
		
		if (populate) {
			fontMapper.populateFontMappings(fontsInUse, fonts);
		}
    	
    }

    public Mapper getFontMapper() {
    	if (fontMapper==null) {
    		fontMapper = new IdentityPlusMapper();
    		// This invokes populateFontMappings
    		try {
				setFontMapper(fontMapper);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		return fontMapper;
	}

	private Mapper fontMapper;
	
	

	/**
	 * Creates a WordprocessingMLPackage, using default page size and orientation.
	 * From 2.7.1, these are read from docx4j.properties, or if not found, default
	 * to A4 portrait.
	 * 
	 * The WordprocessingMLPackage contains a MainDocumentPart (with content), 
	 * Styles part, DocPropsCorePart part, and DocPropsExtendedPart.
	 */
	public static WordprocessingMLPackage createPackage() throws InvalidFormatException {
		
		String papersize= Docx4jProperties.getProperties().getProperty("docx4j.PageSize", "A4");
		log.info("Using paper size: " + papersize);
		
		String landscapeString = Docx4jProperties.getProperties().getProperty("docx4j.PageOrientationLandscape", "false");
		boolean landscape= Boolean.parseBoolean(landscapeString);
		log.info("Landscape orientation: " + landscape);
		
		return createPackage(
				PageSizePaper.valueOf(papersize), landscape); 
	}
	
	/**
	 * Creates a WordprocessingMLPackage, containing a MainDocumentPart (with content), 
	 * Styles part, DocPropsCorePart part, and DocPropsExtendedPart.
	 * 
	 * The content contains sectPr specifying paper size and orientation.
	 * 
	 * @param sz
	 * @param landscape
	 * @return
	 * @throws InvalidFormatException
	 */
	public static WordprocessingMLPackage createPackage(PageSizePaper sz, boolean landscape ) throws InvalidFormatException {
		
				
		// Create a package
		WordprocessingMLPackage wmlPack = new WordprocessingMLPackage();

		// Create main document part
		MainDocumentPart wordDocumentPart = new MainDocumentPart();		
		
		// Create main document part content
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.Body  body = factory.createBody();		
		org.docx4j.wml.Document wmlDocumentEl = factory.createDocument();
		
		wmlDocumentEl.setBody(body);
		
		// Create a basic sectPr using our Page model
		PageDimensions page = new PageDimensions();
		page.setPgSize(sz, landscape);
		
		SectPr sectPr = factory.createSectPr();
		body.setSectPr(sectPr);
		sectPr.setPgSz(  page.getPgSz() );
		sectPr.setPgMar( page.getPgMar() );
				
		// Put the content in the part
		wordDocumentPart.setJaxbElement(wmlDocumentEl);
						
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
			//e.printStackTrace();	
			log.error(e.getMessage(), e);
		}
		
		// Metadata: docx4j 2.7.1 can populate some of this from docx4j.properties
		// See SaveToZipFile
		DocPropsCorePart core = new DocPropsCorePart();
		org.docx4j.docProps.core.ObjectFactory coreFactory = new org.docx4j.docProps.core.ObjectFactory();
		core.setJaxbElement(coreFactory.createCoreProperties() );
		wmlPack.addTargetPart(core);
			
		
		DocPropsExtendedPart app = new DocPropsExtendedPart();
		org.docx4j.docProps.extended.ObjectFactory extFactory = new org.docx4j.docProps.extended.ObjectFactory();
		app.setJaxbElement(extFactory.createProperties() );
		wmlPack.addTargetPart(app);	
		
    	// DocumentSettingsPart (/word/settings.xml)
        // Set overrideTableStyleFontSizeAndJustification to true,
        // like Word 2010/2013/2016.  Since v3.3.0
    	DocumentSettingsPart dsp = new DocumentSettingsPart();
    	wmlPack.getMainDocumentPart().addTargetPart(dsp);
    	dsp.setJaxbElement(new CTSettings());
    	dsp.setOverrideTableStyleFontSizeAndJustification(true);
				
		// Return the new package
		return wmlPack;
		
	}
	
	/**
	 * Attach a template to this document.
	 * This is just an easy way to access the same method in DocumentSettingsPart 
	 * (which lives under the MainDocumentPart)
	 * 
	 * @param templatePath
	 * @since 6.1.0
	 */
	public void attachTemplate(String templatePath) {
		
		this.getMainDocumentPart().attachTemplate(templatePath);
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		try {
			FontTablePart ftp = this.getMainDocumentPart().getFontTablePart();
			if (ftp != null) {
				ftp.deleteEmbeddedFontTempFiles();
			}
		} finally {
			super.finalize();
		}
		
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
		
		Boolean removeBookmarks = Boolean.FALSE;		
		public void setRemoveBookmarks(boolean val) {
			removeBookmarks = new Boolean(val);
		}

		
		public Map<String, Object> getSettings() {
			Map<String, Object> settings = new java.util.HashMap<String, Object>();
			
			settings.put("removeProofErrors", removeProofErrors);
			settings.put("removeContentControls", removeContentControls);
			settings.put("removeRsids", removeRsids);
			settings.put("removeBookmarks", removeBookmarks);
			settings.put("tidyForDocx4all", tidyForDocx4all);
			
			return settings;
		}
		
		
	}
	
	/**
	 * Reinit fields so this pkg object can be re-used.
	 * @since 3.3.7
	 */
	public void reset() {
		super.reset();

		mainDoc = null;
		glossaryDoc = null;

		documentProtectionSettings = new ProtectDocument(this);
		documentModel = null;
		headerFooterPolicy = null;
		fontMapper=null;
		log.info("reset complete");

	}
}