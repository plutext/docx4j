package org.docx4j.convert.out.pdf.viaHTML;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.docx4j.convert.out.Output;
import org.docx4j.fonts.Substituter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.lowagie.text.DocumentException;

public class Conversion extends org.docx4j.convert.out.pdf.PdfConversion {
	
	public Conversion(WordprocessingMLPackage wordMLPackage) {
		super(wordMLPackage);
	}

	/** Create a pdf version of the document. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * 
	 * */     
    public void output(OutputStream os) throws Docx4JException {
    	
    					
        try {
			// Put the html in result
			org.w3c.dom.Document xhtmlDoc = org.docx4j.XmlUtils.neww3cDomDocument();
			javax.xml.transform.dom.DOMResult result = new javax.xml.transform.dom.DOMResult(xhtmlDoc);
			org.docx4j.convert.out.html.HtmlExporter.html(wordMLPackage, result, false,
					System.getProperty("java.io.tmpdir") ); // false -> don't use HTML fonts.
					
			// Now render the XHTML
			org.xhtmlrenderer.pdf.ITextRenderer renderer = new org.xhtmlrenderer.pdf.ITextRenderer();
					
			// 4.  Use addFont code like that below as necessary for the fonts
			
				// See https://xhtmlrenderer.dev.java.net/guide/users-guide-r7.html#xil_32
			org.xhtmlrenderer.extend.FontResolver resolver = renderer.getFontResolver();		
					
			Map fontMappings = wordMLPackage.getFontSubstituter().getFontMappings();
			Map fontsInUse = wordMLPackage.getMainDocumentPart().fontsInUse();
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
		} catch (Exception e) {
			throw new Docx4JException("Failed creating PDF via HTML " ,e);
		}
		
		
	}
	

}
