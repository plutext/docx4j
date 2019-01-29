package org.docx4j.samples;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.CommentFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.ContentAccessor;
import org.jvnet.jaxb2_commons.ppp.Child;


public class CommentsDeleter {

	public static void main(String[] args) throws Exception {
	
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(System.getProperty("user.dir") + "/CommentsDeleter.docx"));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
				.getJaxbElement();
		Body body = wmlDocumentEl.getBody();
	
		CommentFinder cf = new CommentFinder();
	    new TraversalUtil(body, cf);
	
	    for (Child commentElement : cf.getCommentElements()) {
	        System.out.println(commentElement.getClass().getName());
	        Object parent = commentElement.getParent();
        	List<Object> theList = ((ContentAccessor)parent).getContent();
        	boolean removeResult = remove(theList, commentElement );
	        System.out.println(removeResult);
	    }	
	}

	private static boolean remove(List<Object> theList, Object bm) 	{
		// Can't just remove the object from the parent,
		// since in the parent, it may be wrapped in a JAXBElement
		for (Object ox : theList) {
			if (XmlUtils.unwrap(ox).equals(bm)) {
				return theList.remove(ox);
			}
		}
		return false;
	}
	
}
