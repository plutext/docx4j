/*
 *  Copyright 2007-2019, Plutext Pty Ltd.
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



import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Demonstrate adding a note to a slide; for this you add a NotesSlidePart to
 * the SlidePart.
 */
public class SlideNotes  {
	
	protected static Logger log = LoggerFactory.getLogger(SlideNotes.class);
		
	public static void main(String[] args) throws Exception {

		// Where will we save our new .ppxt?
		String outputfilepath = System.getProperty("user.dir") + "/sample-docs/OUT_SlideNotes.pptx";
		
		// Create skeletal package, including a MainPresentationPart and a SlideLayoutPart
		PresentationMLPackage presentationMLPackage = PresentationMLPackage.createPackage(); 
		
		// Need references to these parts to create a slide
		// Please note that these parts *already exist* - they are
		// created by createPackage() above.  See that method
		// for instruction on how to create and add a part.
		MainPresentationPart pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/presentation.xml"));		
		SlideLayoutPart layoutPart = (SlideLayoutPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/slideLayouts/slideLayout1.xml"));
		
		// Slide 1
		// create a slide
		SlidePart slidePart = new SlidePart(new PartName("/ppt/slides/slide1.xml"));
		slidePart.setContents( SlidePart.createSld() );		
		pp.addSlide(0, slidePart);
		
		// Slide layout part
		slidePart.addTargetPart(layoutPart);
				
		// Create and add shape
		Shape sample = ((Shape)XmlUtils.unmarshalString(SAMPLE_SHAPE, Context.jcPML) );
		slidePart.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(sample);
		
		// Now add notes slide.
		NotesMasterPart nmp = pp.getNotesMasterPart(true);
		NotesSlidePart notesSlidePart = slidePart.createNotesSlidePart(nmp);
		notesSlidePart.addNoteTextPara( "here is my note", "en-AU");
		
//		// Slide 2
//		// create a slide
//		slidePart = new SlidePart(new PartName("/ppt/slides/slide2.xml"));
//		slidePart.setContents( SlidePart.createSld() );		
//		pp.addSlide(1, slidePart);
//		
//		// Slide layout part
//		slidePart.addTargetPart(layoutPart);
//				
//		// Create and add shape
//		slidePart.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(sample);
//		
//		// Now add notes slide.
//		notesSlidePart = slidePart.createNotesSlidePart(nmp);
//		notesSlidePart.addNoteTextPara( "note on second slide", "en-AU");
		
		// All done: save it
		presentationMLPackage.save(new java.io.File(outputfilepath));

		System.out.println("\n\n done .. saved " + outputfilepath);
		
	}	
	
	
	
	
	private static String SAMPLE_SHAPE = 			
		"<p:sp   xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
		+ "<p:nvSpPr>"
		+ "<p:cNvPr id=\"4\" name=\"Title 3\" />"
		+ "<p:cNvSpPr>"
			+ "<a:spLocks noGrp=\"1\" />"
		+ "</p:cNvSpPr>"
		+ "<p:nvPr>"
			+ "<p:ph type=\"title\" />"
		+ "</p:nvPr>"
	+ "</p:nvSpPr>"
	+ "<p:spPr />"
	+ "<p:txBody>"
		+ "<a:bodyPr />"
		+ "<a:lstStyle />"
		+ "<a:p>"
			+ "<a:r>"
				+ "<a:rPr lang=\"en-US\" smtClean=\"0\" />"
				+ "<a:t>Hello World</a:t>"
			+ "</a:r>"
			+ "<a:endParaRPr lang=\"en-US\" />"
		+ "</a:p>"
	+ "</p:txBody>"
+ "</p:sp>";
	

	
}
