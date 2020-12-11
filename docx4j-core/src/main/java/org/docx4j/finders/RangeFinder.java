package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;

/**
 * This is a simplified version of 
 * org.docx4j.model.datastorage.BookmarkRenumber.RangeTraverser
 */
public class RangeFinder extends CallbackImpl {
	
	List<CTBookmark> starts = new ArrayList<CTBookmark>();
	/**
	 * @return the starts
	 */
	public List<CTBookmark> getStarts() {
		return starts;
	}

	List<CTMarkupRange> ends   = new ArrayList<CTMarkupRange>();
	/**
	 * @return the ends
	 */
	public List<CTMarkupRange> getEnds() {
		return ends;
	}

	List<Text> refs   = new ArrayList<Text>();
	
	/**
	 * @return the refs
	 */
	public List<Text> getRefs() {
		return refs;
	}

	String startElement; 
	String endElement; 


	public RangeFinder(String startElement, String endElement) {
		
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
	
}
