/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.convert.out.common.preprocess;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SdtElement;

/**
 * Workaround for https://issues.apache.org/bugzilla/show_bug.cgi?id=54094  
 * You can disable this step if you are using FOP post 1.1
 * 
 * The problem could arise
 * anywhere a w:p with page break before 
 * is converted to fo:list-block
 * (typically that means it would also have had numbering)
 * 
 * @since 3.0.1
 * 
 */
public class FopWorkaroundReplacePageBreakInEachList {
	
	private WordprocessingMLPackage wmlPackage;
	
	public static void process(WordprocessingMLPackage wmlPackage) {
		
		FopWorkaroundReplacePageBreakInEachList worker = new FopWorkaroundReplacePageBreakInEachList(wmlPackage);

		worker.process();
				
	}
	
	private FopWorkaroundReplacePageBreakInEachList(WordprocessingMLPackage wmlPackage) {
		this.wmlPackage = wmlPackage;
	}
	
	private PropertyResolver propertyResolver = null;;
	
	private void  process() {

		propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
		
		List<Object> newContent = process(wmlPackage.getMainDocumentPart().getContent());
		
		wmlPackage.getMainDocumentPart().getJaxbElement().getBody().getContent().clear();
		wmlPackage.getMainDocumentPart().getJaxbElement().getBody().getContent().addAll(newContent);
	}
	
	private List process(List contentIn) {
				
		
		List<Object> newContent = new ArrayList<Object>();
		
		boolean inList = false;
		// At present we make a new list-block for
		// each list-item!! 		
		
		for(Object o :  contentIn) { 
			
			if (o instanceof P) {
				P p = (P)o;
				PPr effPPr = getEffectivePPr(p);
				
//				if (inList) {
//					
//					if (isListItem(effPPr)) {
//						// still in list; continue
//						
//					} else {
//						inList = false; // now continue
//					}
//					
//				} else {

					if (isListItem(effPPr)) {
						inList = true;
						
						if (hasBreakBefore(effPPr)) {
							// insert a p containing a break
							P newP = new P();
							newP.setPPr(new PPr());
							newP.getPPr().setPageBreakBefore(new BooleanDefaultTrue());
							newContent.add(newP);
							
							// turn off PageBreakBefore in this p
							PPr pPr = p.getPPr();
							if (pPr == null) {
								pPr = Context.getWmlObjectFactory().createPPr();
								p.setPPr(pPr);
							}
							BooleanDefaultTrue val = new BooleanDefaultTrue();
							val.setVal(Boolean.FALSE);
							pPr.setPageBreakBefore(val);
						}
					} else {
						// inList still false; // now continue
					}
					
				newContent.add(o);
				
			} else if (o instanceof SdtElement) {
				
				newContent.add(o);

				// process the contents of the sdt
				SdtElement sdt = (SdtElement)o;
				List<Object> recursiveL = process(sdt.getSdtContent().getContent());
				sdt.getSdtContent().getContent().clear();
				sdt.getSdtContent().getContent().addAll(recursiveL);
				
			} else {
				
				newContent.add(o);
			}
		}
		return newContent;
	}
	
	private PPr getEffectivePPr(P p) {

		PPr pPrDirect = p.getPPr();
        return propertyResolver.getEffectivePPr(pPrDirect);  
	}
	
	
	private boolean isListItem(PPr pPr) {

		return (pPr!=null && pPr.getNumPr()!=null );
		
	}
	
	private static boolean hasBreakBefore(PPr pPr) {
		
		return (pPr!=null 
				&& pPr.getPageBreakBefore()!=null
				&& pPr.getPageBreakBefore().isVal()!=false 
				 );
	}
	
	

}
