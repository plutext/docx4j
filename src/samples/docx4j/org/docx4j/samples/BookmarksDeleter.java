package org.docx4j.samples;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;


public class BookmarksDeleter {
	
	protected static Logger log = LoggerFactory.getLogger(BookmarksDeleter.class);
	

	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(System.getProperty("user.dir")
						+ "/BookmarksDeleter.docx"));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// Before..
		// System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
				.getJaxbElement();
		Body body = wmlDocumentEl.getBody();

		fixRange(body.getContent(), "CTBookmark", "CTMarkupRange");
		
		// After
		System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));
		
		// or save the docx...
	}

	private static void fixRange(List<Object> paragraphs, String startElement,
			String endElement) throws Exception {

		RangeTraverser rt = new RangeTraverser(startElement, endElement);
		new TraversalUtil(paragraphs, rt);
		
		for (CTBookmark bm : rt.starts) {
			try {
				// Can't just remove the object from the parent,
				// since in the parent, it may be wrapped in a JAXBElement
				List<Object> theList = null;
				if (bm.getParent() instanceof List) {
					theList = (List)bm.getParent(); // eg body.getContent()
				} else {
					theList = ((ContentAccessor)(bm.getParent())).getContent();
				}
				Object deleteMe = null;
				for (Object ox : theList) {
					if (XmlUtils.unwrap(ox).equals(bm)) {
						deleteMe = ox;
						break;
					}
				}
				if (deleteMe!=null) {
					theList.remove(deleteMe);						
				}
			} catch (ClassCastException cce) {
				log.error(cce.getMessage(), cce);
			}
		}

		for (CTMarkupRange mr : rt.ends) {
			try {
				// Can't just remove the object from the parent,
				// since in the parent, it may be wrapped in a JAXBElement
				List<Object> theList = null;
				if (mr.getParent() instanceof List) {
					theList = (List)mr.getParent(); // eg body.getContent()
				} else {
					theList = ((ContentAccessor)(mr.getParent())).getContent();
				}
				Object deleteMe = null;
				for (Object ox : theList) {
					if (XmlUtils.unwrap(ox).equals(mr)) {
						deleteMe = ox;
						break;
					}
				}
				if (deleteMe!=null) {
					theList.remove(deleteMe);						
				}
			} catch (ClassCastException cce) {
				log.info(mr.getParent().getClass().getName());
				log.error(cce.getMessage(), cce);
			}
		}
		
		// NB: this leaves begin|separate|end behind!
		// It would be better to delete the whole field, or just leave it,
		// so this is commented out.  Result in Word will be "bookmark undefined!"
//		for (Text instrText : rt.refs) {
//			try {
//				List<Object> theList = ((ContentAccessor)(instrText.getParent())).getContent();
//				Object deleteMe = null;
//				for (Object ox : theList) {
//					if (XmlUtils.unwrap(ox).equals(instrText)) {
//						deleteMe = ox;
//						break;
//					}
//				}
//				if (deleteMe!=null) {
//					theList.remove(deleteMe);						
//				}
//			} catch (ClassCastException cce) {
//				log.info(instrText.getParent().getClass().getName());
//				log.error(cce);
//			}
//		}
		
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
	
    static class RangeTraverser extends CallbackImpl {
		
    	List<CTBookmark> starts = new ArrayList<CTBookmark>();
    	List<CTMarkupRange> ends   = new ArrayList<CTMarkupRange>();
    	List<Text> refs   = new ArrayList<Text>();
    	
    	String startElement; 
    	String endElement; 
    	
    	RangeTraverser(String startElement, String endElement) {
    		
    		this.startElement = "org.docx4j.wml." + startElement;
    		this.endElement   = "org.docx4j.wml." + endElement;
    	}

    	@Override
		public List<Object> apply(Object o) {
    		
			if (o.getClass().getName().equals(startElement)) {
				if (o instanceof CTBookmark) { 
					CTBookmark bookmark = (CTBookmark)o;
						starts.add(bookmark);
				} 
			}
			
			if (o.getClass().getName().equals(endElement)) {
				if (o instanceof CTMarkupRange) { 
					CTMarkupRange bookmark = (CTMarkupRange)o;
						ends.add(bookmark);
				} 
			}

			if (startElement.equals("org.docx4j.wml.CTBookmark") 
					&& o instanceof javax.xml.bind.JAXBElement
					&& ((JAXBElement)o).getName().getLocalPart().equals("instrText")) {
				refs.add( (Text)XmlUtils.unwrap(o) );
			}
			
			return null;
		}
    	
    	@Override // to setParent
		public void walkJAXBElements(Object parent) {
    		
//    		System.out.println("parent is " + parent.getClass().getName() );
			
			List children = getChildren(parent);
			if (children != null) {

				for (Object o : children) {
					
					if (o instanceof javax.xml.bind.JAXBElement
							&& ((JAXBElement)o).getName().getLocalPart().equals("instrText")) {
						// preserve this, but set its parent
						Text t = (Text)XmlUtils.unwrap(o);
						t.setParent(parent);
					} else {    					
						o = XmlUtils.unwrap(o);
					}
					
					// workaround for broken getParent (since 3.0.0)
					if (o instanceof Child) {
						((Child)o).setParent(parent);
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
