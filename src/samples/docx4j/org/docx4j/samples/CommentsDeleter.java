package org.docx4j.samples;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R.CommentReference;
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
	
	    for (Child commentElement : cf.commentElements) {
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
	
	static class CommentFinder extends CallbackImpl {

	    List<Child> commentElements = new ArrayList<Child>();

	    @Override
	    public List<Object> apply(Object o) {

	        if (o instanceof javax.xml.bind.JAXBElement
                    && (((JAXBElement)o).getName().getLocalPart().equals("commentReference")
                    		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeStart")
                    		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeEnd")	                            		
                    		)) {
	        		System.out.println(((JAXBElement)o).getName().getLocalPart());
	                commentElements.add( (Child)XmlUtils.unwrap(o) );
	            } else 
	        if (o instanceof CommentReference || 
	            o instanceof CommentRangeStart || 
	            o instanceof CommentRangeEnd) {
        		System.out.println(o.getClass().getName());
	            commentElements.add((Child)o);
	        }
	        return null;
	    }

	        @Override // to setParent
	        public void walkJAXBElements(Object parent) {

	            List children = getChildren(parent);
	            if (children != null) {

	                for (Object o : children) {

	                    if (o instanceof javax.xml.bind.JAXBElement
	                            && (((JAXBElement)o).getName().getLocalPart().equals("commentReference")
	                            		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeStart")
	                            		|| ((JAXBElement)o).getName().getLocalPart().equals("commentRangeEnd")	                            		
	                            		)) {
	                    	
	                    	((Child)((JAXBElement)o).getValue()).setParent(XmlUtils.unwrap(parent));
	                    } else {                        
	                        o = XmlUtils.unwrap(o);
		                    if (o instanceof Child) {
		                        ((Child)o).setParent(XmlUtils.unwrap(parent));
		                    }
	                    }


	                    this.apply(o);

	                    if (this.shouldTraverse(o)) {
	                        walkJAXBElements(o);
	                    }

	                }
	            }
	        }           
	    }	
}
