package org.docx4j.samples;
import java.io.StringWriter;

import org.docx4j.model.bookmarks.BookmarksIntegrity;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;


/**
 * Perform certain bookmark integrity checks
 */
public class BookmarksDuplicateCheck {
		
	/**
	 * Whether to attempt 
	 */
	private static boolean remediate = true;
	

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(System.getProperty("user.dir")
						+ "/your.docx"));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		
		BookmarksIntegrity bm = new BookmarksIntegrity();
		
		StringWriter sw = new StringWriter();
		bm.setWriter(sw);
		
		
		bm.check(documentPart, remediate);
		
		
		System.out.println(sw.toString());
		
		if (remediate) {
			// You could save the wordMLPackage
		}
		
		
	}
	
	
}
