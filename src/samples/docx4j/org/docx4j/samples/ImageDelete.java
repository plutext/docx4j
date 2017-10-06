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

package org.docx4j.samples;


import java.io.File;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.R;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.picture.Pic;


public class ImageDelete  {
	
	public static void main(String[] args) throws Exception {

	    String inputfilepath = System.getProperty("user.dir") + "/picture_cc.docx";
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));
		
		// 1. find the picture
	       String xpath = "//w:drawing";
	       List<Object> list = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath(xpath, true);
	       
	       Object toBeRemoved = list.get(0);
	       Drawing drawing = (Drawing)XmlUtils.unwrap(toBeRemoved);
	       
		// 2. get the relId
		Inline inline = (Inline)drawing.getAnchorOrInline().get(0);
		Graphic g = inline.getGraphic();
		GraphicData graphicdata = g.getGraphicData();
		Object o = graphicdata.getAny().get(0);
		Pic pic = (Pic)XmlUtils.unwrap(o);
		String relId = pic.getBlipFill().getBlip().getEmbed();
		
		// 3. delete the rel
		Relationship rel = wordMLPackage.getMainDocumentPart().getRelationshipsPart(false).getRelationshipByID(relId);
		wordMLPackage.getMainDocumentPart().getRelationshipsPart(false).removeRelationship(rel);
		
		// 4. remove the drawing
		System.out.println(drawing.getParent().getClass().getName());
		R r = (R)drawing.getParent();
		r.getContent().remove(toBeRemoved);
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_ImageDelete.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
		
		System.out.println("Saved: " + outputfilepath);
	}
		

}
