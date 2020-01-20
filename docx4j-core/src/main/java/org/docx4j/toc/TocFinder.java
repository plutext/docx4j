/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
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
package org.docx4j.toc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.wml.CTSdtDocPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Don't attempt to reuse this; create a new TocFinder object each time you invoke it.
 *
 */
public class TocFinder  extends CallbackImpl {
	
	private static Logger log = LoggerFactory.getLogger(TocFinder.class);
		
	SdtBlock tocSDT = null;
	/**
	 * @return
	 * @since 3.3.1
	 */
	public SdtBlock getTocSDT() {
		return tocSDT;
	}


	/**
	 * The first sectPr after the ToC, so we can
	 * calculate right tab setting
	 */
	SectPr sectPr = null; 
	
	/**
	 * Paragraphs before the ToC proper are preserved. 
	 */
	List<P> tocHeadingP = new ArrayList<P>();
	
	String tocInstruction = null;
	
	private SdtBlock currentSDT = null;
	private boolean foundToC = false;
	
	@Override
	public List<Object> apply(Object o) {
		
		// We require the TOC to be in an SDT
		if (currentSDT !=null) {
			
			if (o instanceof P
					&& isDocPartToC(currentSDT)
					&& !foundToC // since heading should be before instruction 
					) {
				
				// Reuse the existing heading paragraph(s), since they could have direct/adhoc formatting
				// we need to preserve
				tocHeadingP.add((P)o);
				
//				System.out.println(XmlUtils.marshaltoString(o));

			} else if (o instanceof JAXBElement 
					&& ((JAXBElement)o).getName().getLocalPart().equals("instrText")) {
				
				Text instr = (Text)XmlUtils.unwrap(o);
				if (instr.getValue().contains("TOC")) {
					tocInstruction = instr.getValue();
					tocSDT = currentSDT;
					foundToC = true;
					log.debug("found the sdt!");
					
					removeLastHeadingP();					
					
				}
			} else if (o instanceof JAXBElement 
					&& ((JAXBElement)o).getName().getLocalPart().equals("fldSimple")) {
//				System.out.println("\n\n Found a wrapped fldSimple");

				CTSimpleField fldSimple = (CTSimpleField)XmlUtils.unwrap(o);
				
				if (fldSimple.getInstr().contains("TOC")) {
					tocInstruction = fldSimple.getInstr();
					tocSDT = currentSDT;
					foundToC = true;
					log.debug("found the sdt!");
					
					removeLastHeadingP();					
				}
				
				
			} else if (o instanceof CTSimpleField ) {

//				System.out.println("\n\n Found a fldSimple");

				CTSimpleField fldSimple = (CTSimpleField)o;
				if (fldSimple.getInstr().contains("TOC")) {
					tocInstruction = fldSimple.getInstr();
					tocSDT = currentSDT;
					foundToC = true;
					log.debug("found the sdt!");
					
					removeLastHeadingP();					
				}
				
			}
			
		}
		
		if (foundToC) // look for sectPr
		{
			if (o instanceof P) {
				P p = (P)o;
				if ( p.getPPr()!=null && p.getPPr().getSectPr()!=null ) {
					sectPr = p.getPPr().getSectPr();
				}
			}
			
			
		}
		
		return null; 
	}
	
	/**
	 * When we add the heading paragraphs, the last one added will be a false positive
	 * (it contains the TOC field instruction), so remove it.
	 */
	private void removeLastHeadingP() {
		
		if (tocHeadingP.size()>0) {
			tocHeadingP.remove(tocHeadingP.size()-1);
		}
	}
	
	private boolean isDocPartToC(SdtBlock currentSDT) {
		
		SdtPr sdtPr = currentSDT.getSdtPr();
		
		CTSdtDocPart docPart = getDocPartObj(sdtPr);
		
		if (docPart!=null 
				&& docPart.getDocPartGallery()!=null
				&& docPart.getDocPartGallery().getVal()!=null
				&& docPart.getDocPartGallery().getVal().equals("Table of Contents")) {
			
			return true;
		}
		
		return false;
	}
	
	private CTSdtDocPart getDocPartObj(SdtPr sdtPr) {
		
		if (sdtPr==null) return null;
		
    	for (Object o : sdtPr.getRPrOrAliasOrLock()) {
    		
    		if ( XmlUtils.unwrap(o) instanceof CTSdtDocPart) {
    			return (CTSdtDocPart)XmlUtils.unwrap(o);
    		} 
    	}
    	
        return null;				
	}
	
	// Depth first
	public void walkJAXBElements(Object parent) {
		
		List children = getChildren(parent);
		if (children != null) {

			for (Object o : children) {
												
				this.apply(o);
				
				if (this.shouldTraverse(o)) {
					
					o = XmlUtils.unwrap(o); // in case the SdtBlock is in a JAXBElement
					if (o instanceof org.docx4j.wml.SdtBlock  ) {
												
						currentSDT = (SdtBlock)o;
						walkJAXBElements(o);
						currentSDT = null;
					} else {
						walkJAXBElements(o);
					}
				}
				
				if (sectPr!=null) break;

			}
		}
	}

	public List<Object> getChildren(Object o) {
		return TraversalUtil.getChildrenImpl(o);
	}


	/**
	 * Decide whether this node's children should be traversed.
	 * 
	 * @return whether the children of this node should be visited
	 */
	public boolean shouldTraverse(Object o) {
		
		if (foundToC && o instanceof P) {
			return false;
		}
		
		return (sectPr==null);
	}

}

