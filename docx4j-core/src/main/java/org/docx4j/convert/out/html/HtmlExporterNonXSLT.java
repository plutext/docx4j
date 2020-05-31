/**
 * 
 */
package org.docx4j.convert.out.html;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractVisitorExporterGenerator;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * Running Xalan extension functions on Android is problematic:
 * 
 *   http://stackoverflow.com/questions/10579339/is-it-possible-to-call-a-java-extension-function-from-xalan-on-android
 *   
 * so this uses TraversalUtils to generate HTML output
 * without any need for Xalan or XSLT.
 * 
 * We could use a simple JAXB model of HTML, but instead
 * this class uses org.w3c.dom to construct the HTML document.
 * 
 * This class might be neater if it used CompoundTraversalUtilVisitorCallback,
 * but it would be less obvious what is going on.  
 * 
 * @author jharrop
 * @deprecated
 */
public class HtmlExporterNonXSLT {
	protected static final int DEFAULT_OUTPUT_SIZE = 102400;

	private static Logger log = LoggerFactory.getLogger(HtmlExporterNonXSLT.class);

	protected static String inputfilepath;	
	protected static String outputfilepath;
	
	protected HTMLSettings htmlSettings = null;
	
	public HtmlExporterNonXSLT(WordprocessingMLPackage wordMLPackage, 
			ConversionImageHandler conversionImageHandler) {
		
		htmlSettings = new HTMLSettings();
		htmlSettings.setOpcPackage(wordMLPackage);
		htmlSettings.setImageHandler(conversionImageHandler);
	}
	
	/**
	 * Generate HTML for the entire MainDocumentPart.
	 * @return
	 */
	public org.w3c.dom.Document export() {
	ByteArrayOutputStream outStream = new ByteArrayOutputStream(DEFAULT_OUTPUT_SIZE);
	Document ret = null;
		try {
			Docx4J.toHTML(htmlSettings, outStream, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
			ret = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(outStream.toByteArray()));
		} catch (Docx4JException e) {
			log.error("Exception exporting document: " + e.getMessage(), e);
		} catch (SAXException e) {
			log.error("Exception parsing document: " + e.getMessage(), e);
		} catch (IOException e) {
			log.error("Exception parsing document: " + e.getMessage(), e);
		}
		return ret;
	}
	
	public String getCss() {
		WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage)htmlSettings.getOpcPackage();
		StringBuilder buffer = new StringBuilder();
		HtmlCssHelper.createCssForStyles(wmlPackage, 
										 wmlPackage.getMainDocumentPart().getStyleTree(),
										 buffer);
		return buffer.toString();
	}
	
	/** Generate HTML for the specified content.<br>
	 * Don't expect this method to work, the conversion process relies on a structure 
	 * that has been preprocessed and is based on the complete document. Some examples 
	 * where this method probably fails with a NPE:
	 * <ul>
	 * <li>images</li>
	 * <li>fields</li>
	 * <li>bookmarks</li>
	 * <li>links</li>
	 * </ul>  
	 * 
	 * @param blockLevelContent
	 * @return
	 */
	public org.w3c.dom.Document export(Object blockLevelContent, String cssClass, String cssId) {
		HTMLConversionContext conversionContext = 
				new HTMLConversionContext(htmlSettings, null, null);
	Document document = XmlUtils.neww3cDomDocument();	
    Element parentNode = document.createElement("div");
    AbstractVisitorExporterGenerator<HTMLConversionContext> generator = null;
    	if (cssClass!=null) {
    		parentNode.setAttribute("class", cssClass);
    	}
    	if (cssId!=null) {
    		parentNode.setAttribute("id", cssId);
    	}
    	document.appendChild(parentNode);
    	generator = HTMLExporterVisitorGenerator.GENERATOR_FACTORY.
    			createInstance(conversionContext, document, parentNode);
    	new TraversalUtil(blockLevelContent, generator);
		return document;
	}	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {


		inputfilepath = System.getProperty("user.dir")
//				+ "/hr.docx";
//		+ "/sample-docs/word/sample-docx.docx";
//		+ "/sample-docs/word/2003/word2003-vml.docx";
//				+ "/table-nested.docx";
		+ "/hlink.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		
		HtmlExporterNonXSLT withoutXSLT = new HtmlExporterNonXSLT(wordMLPackage, new HTMLConversionImageHandler("c:\\temp", "/bar", true) );
		
		log.info(XmlUtils.w3CDomNodeToString(
				withoutXSLT.export()));

    	// Wondering where <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    	// comes from? See http://stackoverflow.com/questions/1409091/how-do-i-prevent-the-java-xml-transformer-using-html-method-from-adding-meta
	}

}
