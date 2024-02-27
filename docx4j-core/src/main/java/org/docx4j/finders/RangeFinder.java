package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;

/**
 * This is a simplified version of 
 * org.docx4j.model.datastorage.BookmarkRenumber.RangeTraverser
 */
public class RangeFinder extends CallbackImpl {

	public RangeFinder() {
	}
	
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


	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof CTBookmark) { 
			CTBookmark bookmark = (CTBookmark)o;
				starts.add(bookmark);
		} else /* need this else because CTBookmark extends CTMarkupRange */ 
		if (o instanceof CTMarkupRange) { 
			
			CTMarkupRange bookmark = (CTMarkupRange)o;
				ends.add(bookmark);
		} 

		return null;
	}
	
}
