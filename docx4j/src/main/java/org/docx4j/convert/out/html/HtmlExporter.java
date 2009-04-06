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
import org.docx4j.convert.out.Output;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
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

public class HtmlExporter extends  AbstractHtmlExporter {
	
	
	protected static Logger log = Logger.getLogger(HtmlExporter.class);
	
	public static void log(String message ) {
		
		log.info(message);
	}
	
	static Templates xslt;			
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/convert/out/html/DocX2Html.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
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
    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result,
    		String imageDirPath) throws Exception {
    	
    	html(wmlPackage, result, true, imageDirPath);
    }

    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, boolean fontFamilyStack,
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
    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, 
    		HtmlSettings htmlSettings) throws Exception {
    	
    	/*
    	 * Given that word2html.xsl is freely available, use a
    	 * version of it adapted to process the
    	 * pck:package/pck:part stuff emitted by Word 2007.
    	 * 
    	 */    	
		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wmlPackage);
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
			log.debug("FontMapper set.. ");
		}
		
		htmlSettings.setWmlPackage(wmlPackage);
		
		
		// Now do the transformation
		log.debug("About to transform...");
		org.docx4j.XmlUtils.transform(doc, xslt, htmlSettings.getSettings(), result);
		
		log.info("wordDocument transformed to xhtml ..");
    	
    }

}