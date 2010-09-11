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
package org.docx4j.convert.out;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.PPrBase.PBdr;

public class Containerization {

	private static Logger log = Logger.getLogger(Containerization.class);

	public final static String TAG_SHADING = "XSLT_Shd";
	public final static String TAG_BORDERS = "XSLT_PBdr";
	
	/**
	 * In Word, adjacent paragraphs with the same borders are enclosed in a single border
	 * (unless bullets/numbering apply).
	 * 
	 * Similarly with shading.  (If the 2 paragraphs are shaded different colors, then the
	 * color of the first extends to the start of the second, so there is no white strip
	 * between them).
	 * 
	 * To do the same in HTML and PDF output, we put matching paragraphs into a content
	 * control, and set the border/shading on that.  This gives us an appropriate
	 * div or fo:block.
	 */
	public static List<Object> groupAdjacentBorders(MainDocumentPart mdp) {
		
		List<Object> newList = new ArrayList<Object>(); 
		SdtBlock sdtBorders = null;
		SdtBlock sdtShading = null;
		
		PBdr lastBorders = null;
		PBdr currentBorders = null;
		CTShd lastShading = null;
		CTShd currentShading = null;
		
//		java.util.Stack stack = new java.util.Stack();
//		stack.push(newList);
		
		for (Object o : mdp.getJaxbElement().getBody().getEGBlockLevelElts() ) {
			
			if (!(o instanceof org.docx4j.wml.P)) {
				
				// Just add to whatever our current container is
				addO(o, newList, sdtBorders, sdtShading );
				
				continue;
			}
			
			// So its a paragraph.  Handle this.
			
			// Does the paragraph have borders/shading set?
			// Simple minded approach for now
			// TODO: use effective ppr properties! that will slow things down ..
			org.docx4j.wml.P p = (org.docx4j.wml.P)o;

			currentBorders = null;
			if (p.getPPr() != null ) {
				currentBorders = p.getPPr().getPBdr();
			}
			
			if ( bordersChanged(currentBorders, lastBorders )) {
				
				// could mean null to borders; borders to null; or bordersA to bordersB
				if (currentBorders == null) {
					sdtBorders = null;
				} else {
					sdtBorders = createSdt(TAG_BORDERS);					
					addBordersSdt(sdtBorders, newList );
				}
			}
				
				
			currentShading = null;
			if (p.getPPr() != null ) {
				currentShading = p.getPPr().getShd();
			}
			if ( !shadingChanged(currentShading, lastShading )) {
				
				addO(o, newList, sdtBorders, sdtShading );
				
			} else {
				
				// handle change to shading before addElement
				if (currentShading == null) {
					sdtShading = null;
				} else {
					sdtShading = createSdt(TAG_SHADING);
					
					// need to set margins, so there isn't a white strip
					// between paragraphs.  hmm, model.properties.paragraph
					// won't translate this.  so do it at the fo level
					
					addShadingSdt( sdtShading, newList, sdtBorders );
				}

				// Finally
				addO(o, newList, sdtBorders, sdtShading );
			}
			
			// setup for next loop
			lastBorders = currentBorders;
			lastShading = currentShading;  
		}				
		
		 // end for
		
		return newList;
		
	}
	
	private static SdtBlock createSdt(String tagVal) {
		
		// .. so create content control!
		SdtBlock sdtBlock = Context.getWmlObjectFactory().createSdtBlock();

		SdtPr sdtPr = Context.getWmlObjectFactory().createSdtPr();
		sdtBlock.setSdtPr(sdtPr);

		SdtContentBlock sdtContent = Context.getWmlObjectFactory().createSdtContentBlock();
		sdtBlock.setSdtContent(sdtContent);

		// For borders/shading, we'll use the values in this first paragraph.
		// We'll use a tag, so the XSLT can detect that its supposed to do something special.
		Tag tag = Context.getWmlObjectFactory().createTag();
		tag.setVal(tagVal);
		
		sdtPr.setTag(tag);
		
		return sdtBlock;
		
	}

	private static boolean bordersChanged(PBdr currentBorders, PBdr lastBorders ) {
		
		if (currentBorders==null && lastBorders==null)
			return false;
			
		if (currentBorders==null && lastBorders!=null)
			return true;
		if (currentBorders!=null && lastBorders==null)
			return true;

		// Only test top and bottom for now ..
		if (currentBorders.getTop()==null || lastBorders.getTop()==null )
			return true;		
		if (currentBorders.getTop().getVal().compareTo(lastBorders.getTop().getVal())!=0 )
			return true;
		
		if (currentBorders.getBottom()==null || lastBorders.getBottom()==null )
			return true;
		if (currentBorders.getBottom().getVal().compareTo(lastBorders.getBottom().getVal())!=0 )
			return true;
		
		return false;
	}

	private static boolean shadingChanged(CTShd currentShading, CTShd lastShading) {
		
		if (currentShading==null && lastShading==null)
			return false;
			
		if (currentShading==null && lastShading!=null)
			return true;
		if (currentShading!=null && lastShading==null)
			return true;
			
		// TODO: enhance this
		if (currentShading.getFill()==null || lastShading.getFill()==null)
			return true;
		
		return !currentShading.getFill().equals(lastShading.getFill());
	}


	private static void addO(Object o, List<Object> newList, SdtBlock sdtBorders, SdtBlock sdtShading ) {

		if (sdtShading!=null) {
			sdtShading.getSdtContent().getEGContentBlockContent().add(o);
			return;
		}
		if (sdtBorders!=null) {
			sdtBorders.getSdtContent().getEGContentBlockContent().add(o);
			return;
		}

		newList.add(o);
	}

	private static void addShadingSdt(SdtBlock sdtShading, List<Object> newList, SdtBlock sdtBorders ) {

		if (sdtBorders!=null) {
			sdtBorders.getSdtContent().getEGContentBlockContent().add(sdtShading);
			return;
		}
		newList.add(sdtShading);
	}

	private static void addBordersSdt(SdtBlock sdtBorders, List<Object> newList ) {

		// We don't nest these.  We could if we implemented a stack
		// for borders, and a stack for shading.  
		// Haven't thought through whether that would work well or not 
		// (from a Word / PDF fidelity point of view)
		newList.add(sdtBorders);
	}
	
		
}
