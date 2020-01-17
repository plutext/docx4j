package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.SdtElement;

public class SdtFinder extends CallbackImpl {
	
	private List<SdtElement> sdtList;
	
	public List<SdtElement> getSdtList() {
		return sdtList;
	}

	public SdtFinder() {		
		sdtList = new ArrayList<SdtElement>();
	}
	
	@Override
	public List<Object> apply(Object wrapped) {
		
		Object o = XmlUtils.unwrap(wrapped);		
		if (o instanceof SdtElement) {
			
			sdtList.add(((SdtElement)o));
			
		}
		return null;
	}
	

}
