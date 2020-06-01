package org.docx4j.samples.documents4j.local;

import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.documents4j.local.Documents4jLocalExporter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocGenerator;
import org.docx4j.toc.TocSdtUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;

public class TocOperations  { 
	
	static boolean update = false;
	static boolean createPDF = false;
	
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
        
    	P p = new P();
    	p.getContent().addAll( 
    			TocSdtUtils.getTocInstruction(" TOC \\o \"1-3\" \\h \\z \\u "));
        sdtContent.getContent().add(p);
        sdtContent.getContent().add(TocSdtUtils.getLastParagraph());
        documentPart.getContent().add(sdt);
        
        
        // TODO toc heading
        
        // Now use documents4j to populate it
		Documents4jLocalExporter exporter = new Documents4jLocalExporter();
		
		WordprocessingMLPackage newPkg = exporter.updateDocx(wordMLPackage);
		System.out.println(newPkg.getMainDocumentPart().getXML());
        
        
        
//        wordMLPackage.save(new java.io.File(outputDir + "/OUT_TocSample_Generated.docx") );
        
        
//        if (update) {
//        	
//	        documentPart.addStyledParagraphOfText("Heading2", "Hello 12");
//	        fillPageWithContent(documentPart, "Hello 12");
//	        documentPart.addStyledParagraphOfText("Heading1", "Hello 21");
//	        fillPageWithContent(documentPart, "Hello 21");
//	        documentPart.addStyledParagraphOfText("Heading2", "Hello 22");
//	        fillPageWithContent(documentPart, "Hello 22");
//	        documentPart.addStyledParagraphOfText("Heading3", "Hello 23");
//	        fillPageWithContent(documentPart, "Hello 23");
//	        
//	        tocGenerator.updateToc(false);
//	        
//	        wordMLPackage.save(new java.io.File(outputDir + "/OUT_TocSample_Updated.docx") );
//        }
        
        if (createPDF) {
			
			OutputStream os = new java.io.FileOutputStream(outputDir + "/OUT_TocSample_Updated.pdf");
			Docx4J.toPDF(wordMLPackage, os);
        }
    }

    private static void fillPageWithContent(MainDocumentPart documentPart, String content){
        for(int i = 0; i < 10; i++){
            documentPart.addStyledParagraphOfText("Normal", content + " paragraph " + i);
        }
    }
}
