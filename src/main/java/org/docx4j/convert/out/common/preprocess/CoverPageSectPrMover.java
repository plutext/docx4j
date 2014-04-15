/*
 *  Copyright 2010, Plutext Pty Ltd.
 *   
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

import java.util.List;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SectPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jharrop
 *
 */
public class CoverPageSectPrMover {
	
	private static Logger log = LoggerFactory.getLogger(CoverPageSectPrMover.class);
	
	/**
	 * If the first w:p starts with a w:sectPr, this is moved into a
	 * new following w:p.  This prevents the creation of an empty fo:flow,
	 * which causes a validation exception in FOP.
	 */
	public static void process(WordprocessingMLPackage wmlPackage) {
		
	Body body = wmlPackage.getMainDocumentPart().getJaxbElement().getBody();
		moveSectPr(body);
	}
	
	private static void moveSectPr(Body body) {
		
		if (body==null
				|| body.getContent().size()==0) {
			log.warn("w:document/w:body null or empty");
			return;
		}
		
		Object o = body.getContent().get(0);
		
		if (o instanceof P) {
			SectPr sectPr = cutSectPr((P)o);
			
			if (sectPr!=null) {
				pasteSectPr(body.getContent(), sectPr);
				log.info("Moved sectPr to new P");
				return;
			}
			
		} else if (o instanceof SdtElement) {
			Object o2 = ((SdtElement)o).getSdtContent().getContent().get(0);
			
			if (o2!=null && o2 instanceof P) {

				SectPr sectPr = cutSectPr((P)o2);
				
				if (sectPr!=null) {
					pasteSectPr(((SdtElement)o).getSdtContent().getContent(), sectPr);
					log.info("Moved sectPr to new P inside content control");
					return;
				}
				
			}
		}
		log.info("No need to move sectPr ");
				
	}
	
	private static  SectPr cutSectPr(P p) {
		
		if (p.getPPr()!=null
				&& p.getPPr().getSectPr()!=null) {
			
			SectPr sectPr = p.getPPr().getSectPr();
			p.getPPr().setSectPr(null);
			return sectPr;
			
		}
		return null;
		
	}
	
	private static void pasteSectPr(List<Object> contentList, SectPr sectPr) {
		
		P p = new P();
		PPr ppr = Context.getWmlObjectFactory().createPPr();
		p.setPPr(ppr);
		ppr.setSectPr(sectPr);
		
		contentList.add(1, p);
		
	}
		
}
