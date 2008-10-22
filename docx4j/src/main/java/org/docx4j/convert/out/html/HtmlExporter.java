package org.docx4j.convert.out.html;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.docx4j.convert.out.xmlPackage.XmlPackage;
import org.docx4j.fonts.Substituter;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class HtmlExporter {
	
	
	protected static Logger log = Logger.getLogger(HtmlExporter.class);
	
	/** Create an html version of the document, using CSS font family
	 *  stacks.  This is appropriate if the HTML is intended for
	 *  viewing in a web browser, rather than an intermediate step
	 *  on the way to generating PDF output. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public static void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result) throws Exception {

    	html(wmlPackage, result, true);
    }

    public static void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, boolean fontFamilyStack) throws Exception {

		// Prep parameters
    	HtmlSettings htmlSettings = new HtmlSettings();
    	htmlSettings.setFontFamilyStack(fontFamilyStack);
    	
		html(wmlPackage, result, htmlSettings);
    }
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public static void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, HtmlSettings htmlSettings) throws Exception {
    	
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
		 *  We need an option to leave images out of the pkg.
		 *  
		 */
		
		JAXBContext jc = Context.jcXmlPackage;
		Marshaller marshaller=jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();

		marshaller.marshal(pkg, doc);
		
        // Load the link relationship tables
//        Hashtable linkTable = new Hashtable();
//        Hashtable imageTable = new Hashtable();
//
//        Metro.PackageRelationshipCollection linkRels = wordDoc.GetRelationshipsByType(LinkRelationshipUri);
//        Metro.PackageRelationshipCollection imageRels = wordDoc.GetRelationshipsByType(ImageRelationshipUri);
//
//        ReadRelationshipCollectionIntoHashtable(linkRels, linkTable);
//        ReadRelationshipCollectionIntoHashtable(imageRels, imageTable);
//
//        System.IO.MemoryStream html = null;
//
//        // link the hyperlinks from the relationship tables in
//        HandleLinks(mainDoc, nsm, linkTable);
//        HandleImages(mainDoc, nsm, imageTable, linkTable);
//        HandleThemeFonts(mainDoc, nsm);
//        HandleNumberedLists(mainDoc, nsm);
		
		
		// Get the xslt file - Works in Eclipse - note absence of leading '/'
//		java.io.InputStream xslt = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/openpackaging/packages/wordml2html-2007.xslt");
		java.io.InputStream xslt = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/convert/out/html/DocX2Html.xslt");
		
		// Prep parameters
		if (htmlSettings==null) {
			htmlSettings = new HtmlSettings();
			// ..Ensure that the font names in the XHTML have been mapped to these matches
			//     possibly via an extension function in the XSLT
		}

		

		if (htmlSettings.getFontSubstituter()==null) {
			if (wmlPackage.getFontSubstituter()==null) {
				log.debug("Creating new Substituter.");
				wmlPackage.setFontSubstituter(new Substituter());
			} else {
				log.debug("Using existing Substituter.");
			}
			htmlSettings.setFontSubstituter(wmlPackage.getFontSubstituter());
		}
		
		// Now do the transformation
		org.docx4j.XmlUtils.transform(doc, xslt, htmlSettings.getSettings(), result);
		
		log.info("wordDocument transformed to xhtml ..");
    	
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
