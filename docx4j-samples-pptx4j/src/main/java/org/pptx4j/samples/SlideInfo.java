/*
 *  Copyright 2014, Plutext Pty Ltd.
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;







/**
 * @author jharrop
 *
 */
public class SlideInfo {
	
	protected static Logger log = LoggerFactory.getLogger(SlideInfo.class);
			
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/table.pptx";
		
		PresentationMLPackage presentationMLPackage = 
			(PresentationMLPackage)OpcPackage.load(new java.io.File(inputfilepath));
		
		for (int i=0 ; i<presentationMLPackage.getMainPresentationPart().getSlideCount(); i++) {

			SlidePart slidePart = presentationMLPackage.getMainPresentationPart().getSlide(i);
			
			SlideLayoutPart slideLayoutPart = slidePart.getSlideLayoutPart();
			
			//System.out.println(slp.getSourceRelationships().get(0).getTarget());
			System.out.println(slidePart.getPartName().getName());
			
			String layoutName = slideLayoutPart.getJaxbElement().getCSld().getName(); 
			
			System.out.println(".. uses layout: " + slideLayoutPart.getPartName().getName() + " with cSld/@name='" + layoutName + "'");
			
			System.out.println("   .. which uses master: " + slideLayoutPart.getSlideMasterPart().getPartName().getName());
			
		}
		
		
	}	
}
