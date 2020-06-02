package org.docx4j.samples.documents4j.local;

import org.docx4j.documents4j.local.Documents4jLocalServices;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocSdtUtils;
import org.docx4j.toc.TocStyles;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;

/**
 * This is an end-to-end example showing you how to 
 * to add and populate a ToC, then update it.
 * 
 * It uses Word to populate (and/or update) the ToC,
 * driven by documents4j.
 * 
 * Since Word does the heavy lifting, the full range of
 * switches in the ToC instruction can be used.
 * 
 * For this to work, you'll need a conversion script
 * which instructs Word to update ToC.
 * 
 * A sample override script (which updates ToC) is in docx4j-samples-resources.
 * 
 * To use it, set the docx4j.properties key: com.documents4j.conversion.msoffice.word_convert.vbs
 * 
 * or you can set it programmatically using System.setProperty
 */
public class TocOperations  { 
	
	
	static String outputDir = System.getProperty("user.dir");
	
    public static final String TOC_STYLE_MASK = "TOC%s";
    
    public static void main(String[] args) throws Exception{
    	
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        
        for(int i = 1; i < 10; i++){
            documentPart.getPropertyResolver().activateStyle(String.format(TOC_STYLE_MASK, i));
        }
        
        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");

        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Title", "Title lvl 3");
        fillPageWithContent(documentPart, "Title test");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        documentPart.addStyledParagraphOfText("Heading1", "Hello 11");
        fillPageWithContent(documentPart, "Hello 11");

        // Add the TOC
        SdtBlock sdt = TocSdtUtils.createSdt();
        SdtContentBlock sdtContent = TocSdtUtils.createSdtContent();
        sdt.setSdtContent(sdtContent);
        
        // .. ToC Heading (optional)
        String tocHeading = "My TOC Heading";
        TocStyles tocStyles = TocStyles.getTocStyles(wordMLPackage.getMainDocumentPart());
        TocSdtUtils.addTocHeading(documentPart, tocStyles, sdtContent, tocHeading);
        
        // .. ToC Instruction (the important bit)
    	P p = new P();
    	p.getContent().addAll( 
    			TocSdtUtils.getTocInstruction(" TOC \\o \"1-3\" \\h \\z \\u "));
        sdtContent.getContent().add(p);
        
        sdtContent.getContent().add(TocSdtUtils.getLastParagraph());
        
        documentPart.getContent().add(sdt);
        
        
        // Now use documents4j to populate it
		Documents4jLocalServices exporter = new Documents4jLocalServices();
		
		WordprocessingMLPackage newPkg = exporter.updateDocx(wordMLPackage);
		//System.out.println(newPkg.getMainDocumentPart().getXML());
                
    }

    private static void fillPageWithContent(MainDocumentPart documentPart, String content){
        for(int i = 0; i < 10; i++){
            documentPart.addStyledParagraphOfText("Normal", content + " paragraph " + i);
        }
    }
}
