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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase.PBdr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * @author jharrop, alberto
 *
 */
public class Containerization {

	private static Logger log = LoggerFactory.getLogger(Containerization.class);

	public final static String TAG_SHADING = "XSLT_Shd";
	public final static String TAG_BORDERS = "XSLT_PBdr";

	public final static String TAG_RPR = "XSLT_RPr"; // TODO: support this one in HTML output, via TagSingleBox approach
	
	public static void process(WordprocessingMLPackage wmlPackage) {
		//TODO: Convert to visitor 
		//behaviour here like TraversalUtil.visit with onlyBody = false
		
		MainDocumentPart mainDocument = null;
		RelationshipsPart relPart = null;
		List<Relationship> relList = null;
		List<Object> elementList = null;
		
		mainDocument = wmlPackage.getMainDocumentPart();
		groupAdjacentBorders(mainDocument.getJaxbElement().getBody().getContent());

		relPart = mainDocument.getRelationshipsPart();
		relList = relPart.getRelationships().getRelationship();
		for (Relationship rs : relList) {
			elementList = null;
			if (Namespaces.HEADER.equals(rs.getType())) {
				elementList = ((HeaderPart) relPart.getPart(rs))
						.getJaxbElement().getContent();
			} else if (Namespaces.FOOTER.equals(rs.getType())) {
				elementList = ((FooterPart) relPart.getPart(rs))
						.getJaxbElement().getContent();
			} else if (Namespaces.ENDNOTES.equals(rs.getType())) {
				//elementList = ((EndnotesPart) relPart.getPart(rs)).getContent();
				elementList = new ArrayList();
				elementList.addAll(
						((EndnotesPart) relPart.getPart(rs)).getJaxbElement().getEndnote() );
			} else if (Namespaces.FOOTNOTES.equals(rs.getType())) {
				//elementList =  ((FootnotesPart) relPart.getPart(rs)).getContent();
				elementList = new ArrayList();
				elementList.addAll(
						((FootnotesPart) relPart.getPart(rs)).getJaxbElement().getFootnote() );
			} else if (Namespaces.COMMENTS.equals(rs.getType())) {
				elementList = new ArrayList();
				for (Comment comment : ((CommentsPart) relPart
						.getPart(rs)).getJaxbElement().getComment()) {
					elementList.addAll(comment.getEGBlockLevelElts());
				}
			}
			if ((elementList != null) && (!elementList.isEmpty())) {
				groupAdjacentBorders(elementList);
			}
		}
	}
	

	protected static void groupAdjacentBorders(List<Object> content) {
		
		List<Object> groupedContent = null;
		groupedContent = groupBodyContent(content);
		if (groupedContent != null) {
			content.clear();
			content.addAll(groupedContent);
		}
	}
		
	private static void groupTable(Tbl table) {
		
		List<Object> cellElts = null;
		Tr tr = null;
		Tc tc = null;
		for (Object elemTr:table.getContent()) {
			if (elemTr instanceof JAXBElement) {
				elemTr = ((JAXBElement)elemTr).getValue();
			}
			if (elemTr instanceof Tr) {
				tr = (Tr)elemTr;
				if (tr.getContent() != null) {
					for (Object elemCe:tr.getContent()) {
						if (elemCe instanceof JAXBElement) {
							elemCe = ((JAXBElement)elemCe).getValue();
						}
						if (elemCe instanceof Tc) {
							tc = (Tc)elemCe;
							if (tc.getContent() != null) {
								cellElts = groupBodyContent(tc.getContent());
								if (cellElts != null) {
									tc.getContent().clear();
									tc.getContent().addAll(cellElts);
								}
							}
						}
					}
				}
			}
		}
	}

	private static List<Object> groupBodyContent(List<Object> bodyElts) {
		
		List<Object> resultElts = new ArrayList<Object>();
		List<Object> paragraphElts = null;
		SdtBlock sdtBorders = null;
		SdtBlock sdtShading = null;
		PBdr lastBorders = null;
		PBdr currentBorders = null;
		CTShd lastShading = null;
		CTShd currentShading = null;
		P paragraph = null;
		
		for (Object o : bodyElts) {
			Object unwrapped;
			if (o instanceof JAXBElement) {
				unwrapped = ((JAXBElement)o).getValue();
			} else {
				unwrapped = o;
			}
			if (unwrapped instanceof P) {
				paragraph = (P)unwrapped;
				paragraphElts = groupRuns(paragraph.getContent());
				paragraph.getContent().clear();
				if (paragraphElts != null) {
					paragraph.getContent().addAll(paragraphElts);
				}

				currentBorders = null;
				currentShading = null;
				if (paragraph.getPPr() != null ) {
					// TODO: use effective ppr properties! 
					// ie take styles into account				
					currentBorders = paragraph.getPPr().getPBdr();
					currentShading = paragraph.getPPr().getShd();
				}

				if ( bordersChanged(currentBorders, lastBorders )) {
					// could mean null to borders; borders to null; or bordersA to bordersB
					if (currentBorders == null) {
						sdtBorders = null;
					} else {
						sdtBorders = createSdt(TAG_BORDERS);
						resultElts.add(sdtBorders);
					}
				}
				
				if (shadingChanged(currentShading, lastShading )) {
					// handle change to shading before addElement
					if (currentShading == null) {
						sdtShading = null;
					} else {
						sdtShading = createSdt(TAG_SHADING);
						
						// need to set margins, so there isn't a white strip
						// between paragraphs.  hmm, model.properties.paragraph
						// won't translate this.  so do it at the fo level
						if (sdtBorders!=null) {
							sdtBorders.getSdtContent().getContent().add(sdtShading);
						}
						else {
							resultElts.add(sdtShading);
						}
					}
				}
			}
			else if (unwrapped instanceof Tbl) {
				groupTable((Tbl)unwrapped);
			}
			if (sdtShading!=null) {
				sdtShading.getSdtContent().getContent().add(o);
			}
			else if (sdtBorders!=null) {
				sdtBorders.getSdtContent().getContent().add(o);
			}
			else {
				resultElts.add(o);
			}
			
			// setup for next loop
			lastBorders = currentBorders;
			lastShading = currentShading;  
		}
		return resultElts;
	}
	
	private static List<Object> groupRuns(List<Object> paragraphElts) {
		
		List<Object> resultElts = new ArrayList<Object>();
		SdtBlock currentBlock = null;
		CTBorder lastBorder = null;
		CTBorder currentBorder = null;
		R run = null;
		
//			java.util.Stack stack = new java.util.Stack();
//			stack.push(newList);
		
		for (Object o : paragraphElts) {
			if (o instanceof R) {
				run = (R)o;
				currentBorder = null;
				if (run.getRPr() != null ) {
					currentBorder = run.getRPr().getBdr();
				}
				
				if ( borderChanged(currentBorder, lastBorder )) {
					appendRun(currentBlock, resultElts);
					currentBlock = null;
					// could mean null to borders; borders to null; or bordersA to bordersB
					if (currentBorder != null) {
						currentBlock = createSdt(TAG_RPR, run.getRPr());
					}
				}
			}
			if (currentBlock!=null) {
				currentBlock.getSdtContent().getContent().add(o);
			}
			else {
				resultElts.add(o);
			}
			
			// setup for next loop
			lastBorder = currentBorder;
		}
		appendRun(currentBlock, resultElts);
		return resultElts;
	}

	private static void appendRun(SdtBlock currentBlock, List<Object> resultElts) {
		
		List<Object> blkElements = null;
		R run = null;
		RPr blockRPr = null;
		if (currentBlock != null) {
			blkElements = currentBlock.getSdtContent().getContent();
			if (blkElements.size() == 1) {
				//If there is only one element, there is no need to use a sdtBlock
				resultElts.add(blkElements.get(0));
			}
			else {
				resultElts.add(currentBlock);
				//Remove the borders of the child elements 
				//(and the shading if it is the same as that of the container)
				blockRPr = findBlockRPr(currentBlock);
				for (Object elem:blkElements) {
					if (elem instanceof R) {
						run = (R)elem;
						if (run.getRPr() != null) {
							run.getRPr().setBdr(null);
							if (!shadingChanged(blockRPr.getShd(), run.getRPr().getShd())) {
								run.getRPr().setShd(null);
							}
						}
					}
				}
			}
		}
	}

	private static RPr findBlockRPr(SdtBlock currentBlock) {
		for (Object obj:currentBlock.getSdtPr().getRPrOrAliasOrLock()) {
			if (obj instanceof RPr) {
				return (RPr)obj;
			}
		}
		return null;
	}

	private static SdtBlock createSdt(String tagVal) {
		return createSdt(tagVal, null);
	}

	private static SdtBlock createSdt(String tagVal, RPr rPr) {
		
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
		if (rPr != null) {
			sdtPr.getRPrOrAliasOrLock().add((RPr)XmlUtils.deepCopy(rPr));
			/* 
			 * ECMA-376 says "specifies the set of run properties which shall be applied to
			 *  the text entered into the parent structured document tag in replacement of 
			 *  placeholder text. When placeholder text is present in a structured document 
			 *  tag, its formatting is often different than the desired underlying formatting, 
			 *  and this element specifies the formatting which shall be used for non-placeholder 
			 *  text contents when they are initially added to the control. "
			 * 
			 * Note that docx2fo.xslt is co-opting this to do something else. 
			 */
		}
		return sdtBlock;
		
	}

	private static boolean bordersChanged(PBdr currentBorders, PBdr lastBorders ) {
		if (currentBorders != lastBorders) { 
			if ((currentBorders != null) && (lastBorders != null)) {
				// Only test top and bottom for now ..
				return borderChanged(currentBorders.getTop(), lastBorders.getTop()) ||
					   borderChanged(currentBorders.getBottom(), lastBorders.getBottom()); 
			}
			//one was null
			return true;
		}
		//both null or both identical
		return false;
	}

	private static boolean borderChanged(CTBorder currentBorder, CTBorder lastBorder) {
		if (currentBorder != lastBorder) { 
			if ((currentBorder != null) && (lastBorder != null)) {
				return !currentBorder.getVal().equals(lastBorder.getVal());
			}
			//one was null
			return true;
		}
		//both null or both identical
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
		
}
