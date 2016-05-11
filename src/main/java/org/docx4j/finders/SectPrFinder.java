package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr;

public class SectPrFinder extends CallbackImpl {
	
	/*
	 * There can be a body level sectPr.
	 * 
	 * Other sectPr are typically in a top level paragraph.
	 * 
	 * They can also be in a rich text content control.
	 * 
	 * (Word won't let you copy/paste a sectPr into a table cell.
	 *  And if you put one there in the XML, on opening, Word will
	 *  display the table, but gets a bit confused - it may show it
	 *  twice the second time on a separate page.  On save, the 
	 *  sectPr will be retained.  If you put the sectPr in a
	 *  content control in a tc, the behaviour is similar.  )
	 *  
	 * If you put a sectPr in a text box, Word can open the
	 * docx, and shows it as a page break.   But trying to 
	 * do that from within the Word UI results in:
	 * "You cannot put section breaks into a header, footer,
	 *  footnote, endnote, comment, text box, callout, or macro."
	 * 
	 * Based on the above, this finder looks for the sectPr
	 * only:
	 * - body level sectPr
	 * - top level P
	 * - inside a p in a content control (but not in a 
	 *   table)  
	 */
	
	
	private List<SectPr> sectPrList = new ArrayList<SectPr>(); 
	
	private SectPr bodyLevel = null;
	
	public SectPrFinder(MainDocumentPart mdp) {
		
		// Handle the body level one
		if (mdp.getJaxbElement().getBody().getSectPr()!=null) {
			bodyLevel = mdp.getJaxbElement().getBody().getSectPr();
		}
		
	}
	
	private SectPrFinder(){};
	
	/**
	 * Note that the body level sectPr (if there is one)
	 * is at the start of the list.
	 * 
	 * @return the sectPrList
	 */
	@Deprecated
	public List<SectPr> getSectPrList() {
		
		if (bodyLevel!=null ) {
			sectPrList.remove(bodyLevel); 		
			sectPrList.add(0, bodyLevel);
		}
		
		return sectPrList;
	}

	/**
	 * The body level sectPr (if there is one)
	 * is at the end of the list.
	 * 
	 * @return the sectPrList
	 */
	public List<SectPr> getOrderedSectPrList() {
		
		if (bodyLevel!=null ) {
			sectPrList.remove(bodyLevel); // in case user has previously called this method or the deprecated getSectPrList()			
			sectPrList.add(bodyLevel);
		}
		
		return sectPrList;
	}
	
	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof P) {
			P p = (P)o;
			if ( p.getPPr()!=null && p.getPPr().getSectPr()!=null ) {
//				System.out.println("Found sectpr!");
				sectPrList.add(p.getPPr().getSectPr());
			}
		}
		return null;
	}
	
	public boolean shouldTraverse(Object o) {
		
//		System.out.println(o.getClass().getName() );
		
		return !(o instanceof P
				|| o instanceof org.docx4j.wml.Tbl); 
	}
	
}