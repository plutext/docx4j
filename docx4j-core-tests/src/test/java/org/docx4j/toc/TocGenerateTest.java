package org.docx4j.toc;

import java.io.File;

import junit.framework.Assert;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.SdtBlock;
import org.junit.Test;

public class TocGenerateTest {

    public static final String TOC_STYLE_MASK = "TOC%s";
	
	@Test
	public void testGeneral() throws TocException, Exception {
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);		
        tocGenerator.generateToc( 0, " TOC \\o \"1-3\" \\h \\z \\u ", true);
        
        
        SdtBlock sdt = getTocSDT(wordMLPackage);
        
//        System.out.println(sdt.getSdtContent().getContent().size());        
//        System.out.println(wordMLPackage.getMainDocumentPart().getXML());
                
        // Title p + instruction p +  3 entries + end p
        Assert.assertEquals(6, sdt.getSdtContent().getContent().size());
	}

	@Test
	public void testHeading() throws TocException, Exception {
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
        tocGenerator.generateToc( 0, "TOC \\o \"1-3\"", true);
        
        
        SdtBlock sdt = getTocSDT(wordMLPackage);
        
//        System.out.println(sdt.getSdtContent().getContent().size());        
        System.out.println(wordMLPackage.getMainDocumentPart().getXML());
                
        // Title p + instruction p +  3 entries + end p
        Assert.assertEquals(6, sdt.getSdtContent().getContent().size());
	}

//	@Test
//	public void testHyperlink() throws TocException, Exception {
//		
//		WordprocessingMLPackage wordMLPackage = createPkg();
//		
//        TocGenerator.generateToc(wordMLPackage, 0, "TOC \\h", true);        
//        
//        SdtBlock sdt = getTocSDT(wordMLPackage);
//                        
//        // Title p + instruction p +  3 entries + end p
//        Assert.assertEquals(6, sdt.getSdtContent().getContent().size());
//	}

	@Test
	public void testOutlineLevel() throws TocException, Exception {
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
        tocGenerator.generateToc( 0, "TOC \\u", true);
        
        
        SdtBlock sdt = getTocSDT(wordMLPackage);
        
//        System.out.println(sdt.getSdtContent().getContent().size());        
//        System.out.println(wordMLPackage.getMainDocumentPart().getXML());
//      System.out.println(wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getXML());
                
        // Title p + instruction p +  3 entries + end p
        Assert.assertEquals(6, sdt.getSdtContent().getContent().size());
	}
	
	
	@Test
	public void testHeadingTrumpsOutline() throws TocException, Exception {
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
        tocGenerator.generateToc( 0, "TOC \\o \"1-2\" \\u", true);
        
        
        SdtBlock sdt = getTocSDT(wordMLPackage);
        
//        System.out.println(sdt.getSdtContent().getContent().size());        
        System.out.println(wordMLPackage.getMainDocumentPart().getXML());
                
        // Title p + instruction p +  2 entries + end p
        Assert.assertEquals(5, sdt.getSdtContent().getContent().size());
	}

//	@Test
//	public void testToCHeadingNull() throws TocException, Exception {
//		
//		Toc.setTocHeadingText(null); // Word is ok with this
//		
//		WordprocessingMLPackage wordMLPackage = createPkg();
//		
//        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
//        tocGenerator.generateToc( 0, " TOC \\o \"1-3\" \\h \\z \\u ", true);
//        
//        SdtBlock sdt = getTocSDT(wordMLPackage);
//        
////        System.out.println(sdt.getSdtContent().getContent().size());        
//        System.out.println(wordMLPackage.getMainDocumentPart().getXML());
//        
//        Docx4J.save(wordMLPackage, new File("testToCHeadingNull.docx"));
//	}
//
//	@Test
//	public void testToCHeadingEmpty() throws TocException, Exception {
//		
//		Toc.setTocHeadingText(""); // Word is ok with this
//		
//		WordprocessingMLPackage wordMLPackage = createPkg();
//		
//        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
//        tocGenerator.generateToc( 0, " TOC \\o \"1-3\" \\h \\z \\u ", true);
//        
//        SdtBlock sdt = getTocSDT(wordMLPackage);
//        
////        System.out.println(sdt.getSdtContent().getContent().size());        
//        System.out.println(wordMLPackage.getMainDocumentPart().getXML());
//        
//        Docx4J.save(wordMLPackage, new File("testToCHeadingEmpty.docx"));
//	}
//
//	@Test
//	public void testToCHeadingSet() throws TocException, Exception {
//		
//		Toc.setTocHeadingText("Alpha");
//		
//		WordprocessingMLPackage wordMLPackage = createPkg();
//		
//        TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
//        tocGenerator.generateToc( 0, " TOC \\o \"1-3\" \\h \\z \\u ", true);
//        
//        SdtBlock sdt = getTocSDT(wordMLPackage);
//        
////        System.out.println(sdt.getSdtContent().getContent().size());        
//        System.out.println(wordMLPackage.getMainDocumentPart().getXML());
//        
//        Docx4J.save(wordMLPackage, new File("testToCHeadingSet.docx"));
//	}
	
	private SdtBlock getTocSDT(WordprocessingMLPackage wordMLPackage) {

        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        Document wmlDocumentEl = (Document)documentPart.getJaxbElement();
        Body body =  wmlDocumentEl.getBody();

    	TocFinder finder = new TocFinder();
		new TraversalUtil(body.getContent(), finder);
		
		return finder.tocSDT;		
	}
    
    private WordprocessingMLPackage createPkg() throws Exception{
    	
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        
        for(int i = 1; i < 10; i++){
            documentPart.getPropertyResolver().activateStyle(String.format(TOC_STYLE_MASK, i));
        }
        
        documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading1", ""); // Word omits empty entries from ToC
        fillPageWithContent(documentPart, "Hello 1");
        documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
        fillPageWithContent(documentPart, "Hello 2");
        documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
        fillPageWithContent(documentPart, "Hello 3");
        
        return wordMLPackage;
    }

    private static void fillPageWithContent(MainDocumentPart documentPart, String content){
        for(int i = 0; i < 2; i++){
            documentPart.addStyledParagraphOfText("Normal", content + " paragraph " + i);
        }
    }
}
