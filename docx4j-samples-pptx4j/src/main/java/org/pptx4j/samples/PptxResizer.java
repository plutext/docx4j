/*
 *  Copyright 2017, Plutext Pty Ltd.
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
package org.pptx4j.samples;


import java.io.File;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTGroupTransform2D;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.model.SlideSizesWellKnown;
import org.pptx4j.pml.CTGraphicalObjectFrame;
import org.pptx4j.pml.CommonSlideData;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Pic;
import org.pptx4j.pml.Presentation;
import org.pptx4j.pml.Presentation.SldSz;
import org.pptx4j.pml.Shape;


/**
 * This is a proof of concept of resizing slides (ie including their contents).
 * 
 * It does 2 things:
 * 
 * 1.  alters sldSz in presentation.xml,
 * 2.  calculates new values for xfrm elements in slides, layouts and masters
 * 
 * This is enough to handle simple content.  Additional handling is probably
 * required for SmartArt and other long tail features.
 * 
 * @author jharrop
 *
 */
public class PptxResizer {
	
	public static SlideSizesWellKnown targetSize = SlideSizesWellKnown.A4;
	public static boolean landscape = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {


		String inputfilepath = System.getProperty("user.dir")
			+ "/B5.pptx";

		PptxResizer resize = new PptxResizer();
		
		resize.pMLPackage = 
			(PresentationMLPackage)OpcPackage.load(new java.io.File(inputfilepath));
		
		// first, get existing dimensions
		Presentation presentation = resize.pMLPackage.getMainPresentationPart().getContents();
		SldSz sldSzASIS = presentation.getSldSz();
		int cxASIS = sldSzASIS.getCx();
		int cyASIS = sldSzASIS.getCy();
		
		// generate new dimensions
		SldSz sldSzTOBE = MainPresentationPart.createSlideSize(targetSize, landscape);
		int cxTOBE = sldSzTOBE.getCx();
		int cyTOBE = sldSzTOBE.getCy();

		
		if (cxASIS==cxTOBE
				&& cyASIS==cyTOBE) {
			System.out.println("1:1, so nothing to do");
			return;
		}

		presentation.setSldSz(sldSzTOBE);
		
		
		resize.xScale = cxTOBE/(float)cxASIS;
		resize.yScale = cyTOBE/(float)cyASIS;
		
		resize.handleParts();
		
		resize.pMLPackage.save(new File(System.getProperty("user.dir")
				+ "/OUT_Resizer.pptx"));
		
	}
	
	PresentationMLPackage pMLPackage = null;
	float xScale ;
	float yScale ;
	
	
	private void handleParts() throws Docx4JException {
		
		for (Part p : pMLPackage.getParts().getParts().values()) {
			
			System.out.println("\n\n" + p.getPartName().getName());
			
			if (p instanceof SlidePart) {
				
				SlidePart slide = (SlidePart)p;
				handlePart(slide.getContents().getCSld());
				
			} else if (p instanceof SlideLayoutPart) {
				
				SlideLayoutPart part = (SlideLayoutPart)p;
				handlePart(part.getContents().getCSld());
				
			} else if (p instanceof SlideMasterPart) {
				
				SlideMasterPart part = (SlideMasterPart)p;
				handlePart(part.getContents().getCSld());
				
			} 
		}
		
	}
	
	private void handlePart(CommonSlideData cSld) {
		
		
		// first, handle the root node
		handleGroupShape(cSld.getSpTree());
		
		// next, its descendants
		new TraversalUtil(cSld.getSpTree(), 

		new Callback() {

			public List<Object> apply(Object o) {

				
				if (o instanceof org.pptx4j.pml.Shape) {
					handleShape((Shape)o);
				} else if (o instanceof GroupShape) {
					handleGroupShape((GroupShape)o);					
				} else if (o instanceof CTGraphicalObjectFrame) {
					
					CTGraphicalObjectFrame pic = (CTGraphicalObjectFrame)o;
					if (pic.getXfrm()!=null) {
						handleXfrm(pic.getXfrm());
					}
				} else if (o instanceof org.pptx4j.pml.Pic) {
					
					Pic pic = (Pic)o;
					if (pic.getSpPr()!=null 
							&& pic.getSpPr().getXfrm()!=null) {
						handleXfrm(pic.getSpPr().getXfrm());
					}

				} else {
					System.out.println( o.getClass().getName() );					
				}
				
				return null;
			}

			public boolean shouldTraverse(Object o) {
				return true;
			}

			// Depth first
			public void walkJAXBElements(Object parent) {

				List children = getChildren(parent);
				if (children != null) {

					for (Object o : children) {

						// if its wrapped in javax.xml.bind.JAXBElement, get its
						// value
						o = XmlUtils.unwrap(o);

						this.apply(o);

						if (this.shouldTraverse(o)) {
							walkJAXBElements(o);
						}

					}
				}
			}

			public List<Object> getChildren(Object o) {
				return TraversalUtil.getChildrenImpl(o);
			}

		}

		);

	}
	
	private void handleGroupShape(GroupShape groupShape) {
		
		if (groupShape.getGrpSpPr()!=null
				&& groupShape.getGrpSpPr().getXfrm()!=null) {
			
			handleXfrm(groupShape.getGrpSpPr().getXfrm());
		}
		
	}
	
	private void handleShape(Shape shape) {
		
		if (shape.getSpPr()!=null
				&& shape.getSpPr().getXfrm()!=null) {
			
			handleXfrm(shape.getSpPr().getXfrm());
			
		}
	}

	private void handleXfrm(CTGroupTransform2D xfrm) {
	
		// offset
		CTPoint2D offset = xfrm.getOff();
		offset.setX(Math.round(offset.getX()*xScale));
		offset.setY(Math.round(offset.getY()*yScale));
		
		// extent
		CTPositiveSize2D ext = xfrm.getExt();
		ext.setCx(Math.round(ext.getCx()*xScale));
		ext.setCy(Math.round(ext.getCy()*yScale));
	}
	
	private void handleXfrm(CTTransform2D xfrm) {
		
		// offset
		CTPoint2D offset = xfrm.getOff();
		offset.setX(Math.round(offset.getX()*xScale));
		offset.setY(Math.round(offset.getY()*yScale));
		
		// extent
		CTPositiveSize2D ext = xfrm.getExt();
		ext.setCx(Math.round(ext.getCx()*xScale));
		ext.setCy(Math.round(ext.getCy()*yScale));
	}
	
}
