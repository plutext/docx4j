package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr;

public class SectPrFinder extends CallbackImpl {
	
	private List<SectPr> sectPrList = new ArrayList<SectPr>(); 
	
	public SectPrFinder(MainDocumentPart mdp) {
		// Handle the body level one
		if (mdp.getJaxbElement().getBody().getSectPr()!=null) {
			sectPrList.add(mdp.getJaxbElement().getBody().getSectPr());
		}
	}
	
	private SectPrFinder(){};
	
	/**
	 * @return the sectPrList
	 */
	public List<SectPr> getSectPrList() {
		return sectPrList;
	}

	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof P) {
			P p = (P)o;
			if ( p.getPPr()!=null && p.getPPr().getSectPr()!=null ) {
				sectPrList.add(p.getPPr().getSectPr());
			}
		}
		return null;
	}
	
	public boolean shouldTraverse(Object o) {
		return !(o instanceof P); 
	}
	
}