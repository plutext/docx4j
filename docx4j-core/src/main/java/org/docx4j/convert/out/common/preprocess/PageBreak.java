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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;

/**
 * @author alberto
 *
 */
public class PageBreak {
	
	private static Logger log = LoggerFactory.getLogger(PageBreak.class);
	
	/**
	 * If a page-break w:br w:type="page" is found within a run with some formatting applied to it
	 * then it will be generated into an fo:inline tag. This page break will be ignored by fop. This class
	 * moves the page-breaks to the enclosing block. 
	 */
	public static void process(WordprocessingMLPackage wmlPackage) {
	Body body = wmlPackage.getMainDocumentPart().getJaxbElement().getBody();
		//TODO: Convert to visitor
		movePageBreaks(body);
	}
	
	private static void movePageBreaks(Body body) {
		
		List<Object> elts = body.getContent();
		for (Object o : elts) {
			if (o instanceof P) {
				updateParagraph((P)o);
			}
		}				
	}
	
	private static void updateParagraph(P paragraph) {
		
		boolean containsPageBreak = checkPageBreak(paragraph.getContent());
		if (containsPageBreak) {
			if (paragraph.getPPr() == null) {
				paragraph.setPPr(new PPr());
			}
			paragraph.getPPr().setPageBreakBefore(new BooleanDefaultTrue());
		}
	}

	private static boolean checkPageBreak(List<Object> content) {
		
		int foundIdx = -1;
		Object ce = null;
		if ((content != null) && (!content.isEmpty())) {
			for (int i=0; (foundIdx == -1) && (i<content.size()); i++) {
				ce = content.get(i);
				if (ce instanceof R) {
					if (checkPageBreak(((R)ce).getContent())) {
						if ((((R)ce).getContent() == null) || (((R)ce).getContent().isEmpty())) {
							foundIdx = i;
						}
						else {
							return true;
						}
					}
				}
				else if (ce instanceof Br) {
					if (STBrType.PAGE.equals(((Br)ce).getType())) {
						foundIdx = i;
					}
				}
			}
			if (foundIdx > -1) {
				content.remove(foundIdx);
			}
		}
		return foundIdx > -1;
	}
	
}
