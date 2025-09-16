/*
 *  Copyright 2025, Plutext Pty Ltd.
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

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTSdtDocPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

/**
 *  Modern versions of Word typically insert a ToC within an SDT (aka content control).
 *  
 *  For example:
 * 
    <w:sdt>
      <w:sdtPr>
        <w:docPartObj>
          <w:docPartGallery w:val="Table of Contents"/>
          
 * This was not the case for now-ancient versions of Word.  
 * 
 * And nor is it the case for a table of figures even now.  For example:
 * 
    <w:p>
      <w:fldSimple w:instr=" TOC \h \z \c &quot;Figure&quot; ">
 *
 *  or
 * 
      <w:r>
        <w:fldChar w:fldCharType="begin"/>
      </w:r>
      <w:r>
        <w:instrText xml:space="preserve"> TOC \h \z \c "Figure" </w:instrText>
      </w:r>
      <w:r>
        <w:fldChar w:fldCharType="separate"/>
        :
 * 
 * The purpose of this class is to find a "bare" TOC and wrap it in a content control.
 * 
 * Once this has been done, you can use TocGenerator's updateToc method on it.
 * 
 */
public class TocIntoSdt  { 

	private static Logger log = LoggerFactory.getLogger(TocIntoSdt.class);
	   
    public SdtBlock process(WordprocessingMLPackage wordMLPackage) throws TocException {
        
        List<Object> topLevelContent = wordMLPackage.getMainDocumentPart().getContent();
        
        // Step 1:  Find TOC
    	TocFinder finder = new TocFinder();
		new TraversalUtil(topLevelContent, finder);
        
//		log.debug("First : " + XmlUtils.marshaltoString(finder.pFirst));
//		log.debug("Last : " + XmlUtils.marshaltoString(finder.pLast));
				
        // Step 2:  Encapsulate
		// find index of pFirst and pLast
		int fromIndex = topLevelContent.indexOf(finder.pFirst);
		int toIndex = topLevelContent.indexOf(finder.pLast);

		if (fromIndex<0 || toIndex<0) {
			throw new TocException("Couldn't find TOC field");
		}
		
        // Create new list containing the range
        List<Object> rangeList = new ArrayList<Object>(topLevelContent.subList(fromIndex, toIndex+1));
		
		SdtBlock sdtBlock = TocSdtUtils.createSdt();
        SdtContentBlock sdtContent = TocSdtUtils.createSdtContent();
        sdtBlock.setSdtContent(sdtContent);
		
        sdtContent.getContent().addAll(rangeList);

        // Remove that range from the original list
        topLevelContent.subList(fromIndex, toIndex+1).clear();
        
        topLevelContent.add(fromIndex, sdtBlock);

        //log.debug(wordMLPackage.getMainDocumentPart().getXML());
        
        return sdtBlock;
    }
    

    
    static class TocFinder  extends CallbackImpl {
        	        	
        	// The first and last paragraphs in the TOC
        	P pFirst = null;
        	P pLast = null;  // if not null, we've found the TOC
        	
        	int depth=0;
        	
        	String tocInstruction = null;
        	
        	private SdtBlock currentSDT = null;
        	private boolean inToC = false;
        	
        	P currentP = null;
        	
        	@Override
        	public List<Object> apply(Object wrapped) {

				Object unwrapped = XmlUtils.unwrap(wrapped); // always unwrap is ok here
        		
        		// Track which P we are in
        		if (unwrapped instanceof P) {
        			currentP = (P)unwrapped;
        		} // NB Would hold stale value if encounter other block level objects etc Tbl, Sdt, but not a problem here
        		
        		// We're looking for a TOC which isn't already inside an SDT
        		if (currentSDT ==null
        				|| !isDocPartToC(currentSDT)) {
        			
        			if (unwrapped /*important */ instanceof org.docx4j.wml.FldChar) {
        				FldChar fldChar = (FldChar)unwrapped;
        				log.debug("FldChar " + fldChar.getFldCharType());
        				if (fldChar.getFldCharType().equals(STFldCharType.BEGIN) ) {
        					log.debug("Found a BEGIN, depth " + depth);
        					if (depth==0) {
        						pFirst = currentP;  // will keep getting set until it is actually the TOC one
        					}
        					depth++;
        				}
        				if (fldChar.getFldCharType().equals(STFldCharType.END) ) {
        					log.debug("Found an END, depth " + depth);
        					if (inToC && depth==1) {
        						pLast = currentP;
        					}
        					depth--;
        				}
        			}        			
        			else if (wrapped /*important */ instanceof JAXBElement 
        					&& ((JAXBElement)wrapped).getName().getLocalPart().equals("instrText")) {
        				
        				Text instr = (Text)unwrapped;
        				if (instr.getValue().contains("TOC")) {
        					tocInstruction = instr.getValue();
        					inToC = true;
        					log.debug("found complex field instruction!");
        					
//        					removeLastHeadingP();					
        					
        				}
        			} else if (( unwrapped instanceof CTSimpleField) 
	        					|| (wrapped instanceof JAXBElement // shouldn't happen since always unwrapped
	        							&& ((JAXBElement)wrapped).getName().getLocalPart().equals("fldSimple"))) {

        				CTSimpleField fldSimple = (CTSimpleField)XmlUtils.unwrap(wrapped);
        				
        				if (fldSimple.getInstr().contains("TOC")) {
        					tocInstruction = fldSimple.getInstr();
        					inToC = true;
        					log.debug("found simple field instruction!");

    						pFirst = currentP;
    						pLast = currentP;
        					
//        					removeLastHeadingP();					
        				}
        			}
        			
        		}
        		
        		return null; 
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
        												
        				this.apply(o); // pass wrapped (important)

    					o = XmlUtils.unwrap(o); // always unwrap is ok here
        				
        				if (this.shouldTraverse(o)) {
        					
        					if (o instanceof org.docx4j.wml.SdtBlock  ) {
        												
        						currentSDT = (SdtBlock)o;
        						walkJAXBElements(o);
        						currentSDT = null;
        					} else {
        						walkJAXBElements(o);
        					}
        				}
        				
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
        		
        		if (pLast!=null) {
        			return false;
        		}
        		
        		return true;
        	}

        }

    }


