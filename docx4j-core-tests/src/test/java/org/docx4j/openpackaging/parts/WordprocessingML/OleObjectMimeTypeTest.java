package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @since 11.5.10
 */
public class OleObjectMimeTypeTest {
	
	@Test
    public void draggedPDF() throws Docx4JException {
		testCombinations("dragged pdf.docx", null, "application/pdf");
    }	

	@Test
    public void createPDF() throws Docx4JException {
		testCombinations("create new pdf.docx", null, "application/pdf");
    }	
	
	@Test
    public void draggedTXT() throws Docx4JException {
		testCombinations("dragged txt.docx", null, "text/plain");
    }	

	@Test
    public void insertedDOC() throws Docx4JException {
		testCombinations("inserted doc.docx","Microsoft_Word_97_-_2003_Document.doc", "application/msword");
    }	

	@Test
    public void createBitmap() throws Docx4JException {
		testCombinations("create new bitmap.docx", null, "image/bmp");
    }	
	
	// Word doesn't generally insert a docx as an OLE object
//	@Test
//    public void insertedDOCX() throws Docx4JException {
//		testCombinations("inserted docx.docx","application/pdf");
//    }	
	
	private void testCombinations(String filename, String embedding, String expectedMime) throws Docx4JException {

		// trustCLSID
		test(filename, embedding, expectedMime,  true, true);	    	
		if (/* special case */ expectedMime.equals("text/plain")) {
			// FileMagic can't detect by looking at Ole10Native!
			test(filename, embedding, "application/octet-stream", true, false);	    	
		} else {
			test(filename, embedding, expectedMime,  true, false);	    				
		}

		// don't trustCLSID
		test(filename, embedding, expectedMime,  false, true);	    	
		if (/* special case */ expectedMime.equals("text/plain")) {
			// FileMagic can't detect by looking at Ole10Native!
			test(filename, embedding, "application/octet-stream", false, false);	    	
		} else {
			test(filename, embedding, expectedMime,  false, false);	    				
		}
		
	}
	
	private void test(String filename, String embedding, String expectedMime, boolean trustCLSID, boolean useTika) throws Docx4JException {

		String inputfilepath = System.getProperty("user.dir") 
				+ "/src/test/resources/OLE/"+ filename;	    	
			
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				
		
		if (embedding==null) {
			embedding = "/word/embeddings/oleObject1.bin";
		} else {
			embedding = "/word/embeddings/" + embedding;
		}
		OleObjectBinaryPart p = (OleObjectBinaryPart)wordMLPackage.getParts().get(new PartName(embedding) );
		OleObjectBinaryPart.setTRUST_CLSID(trustCLSID);
		OleObjectBinaryPart.setUSE_TIKA_IF_AVAILABLE(useTika);
		
		String result = p.detectMimeType();
		System.out.println(result + "\n");
		assertEquals(expectedMime,  result);
		
	}
	
	
}