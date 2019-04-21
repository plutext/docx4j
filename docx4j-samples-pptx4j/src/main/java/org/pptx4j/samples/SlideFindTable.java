/*
 *  Copyright 2015, Plutext Pty Ltd.
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
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTTable;
import org.docx4j.dml.CTTableRow;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;


/**
 * Given a slide, find the first table, then duplicate a table row.
 * 
 * 
 * @author jharrop
 *
 */
public class SlideFindTable  {


	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/table.pptx";

		PresentationMLPackage pMLPackage = 
			(PresentationMLPackage)OpcPackage.load(new java.io.File(inputfilepath));

		SlidePart slide = (SlidePart)pMLPackage.getParts().get(new PartName("/ppt/slides/slide1.xml") );
		
		// First, find the tables
		ClassFinder dmlTableFinder = new ClassFinder(org.docx4j.dml.CTTable.class);
		
		new TraversalUtil(slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(), dmlTableFinder);
		
		/* structure will contain:
		 * 
			    org.pptx4j.pml.CTGraphicalObjectFrame
			        org.docx4j.dml.CTTable
			            org.docx4j.dml.CTTableRow
			                org.docx4j.dml.CTTableCell
			                org.docx4j.dml.CTTableCell
			            org.docx4j.dml.CTTableRow
			                org.docx4j.dml.CTTableCell
			                org.docx4j.dml.CTTableCell
			            org.docx4j.dml.CTTableRow
			                org.docx4j.dml.CTTableCell
			                org.docx4j.dml.CTTableCell
                		 */

		if (dmlTableFinder.results.isEmpty() ) {

			System.out.println(
					"No org.docx4j.dml.CTTable found on this slide" );
			return;
		}
		
		CTTable theTable = (CTTable)dmlTableFinder.results.get(0);
		
		// Now copy a table row
		// First, get the row to copy
		CTTableRow srcRow = theTable.getTr().get(0);
		
		// Next, copy it
		CTTableRow clonedRow = XmlUtils.deepCopy(srcRow);
		
		// Finally, insert the copy in the list
		theTable.getTr().add(clonedRow); // standard Java list API
		
		// Save the result
		pMLPackage.save(new File( System.getProperty("user.dir") + "/OUT_SlideFindTable.pptx"));
		
} 
	
	

	static class ClassFinder extends CallbackImpl {
		  
		  protected Class<?> typeToFind;
		  
		  public ClassFinder(Class<?> typeToFind) {
			  this.typeToFind = typeToFind;
		  }
			
			public List<Object> results = new ArrayList<Object>(); 
			
			@Override
			public List<Object> apply(Object o) {
				
				// Adapt as required
				if (o.getClass().equals(typeToFind)) {
					results.add(o);
				}
				return null;
			}
			
			public List<Object> getChildren(Object o) {
				
				if (o instanceof org.pptx4j.pml.CTGraphicalObjectFrame) {
					org.docx4j.dml.Graphic graphic =  ((org.pptx4j.pml.CTGraphicalObjectFrame)o).getGraphic();
					if (graphic!=null  
							&& graphic.getGraphicData()!=null) {
						return graphic.getGraphicData().getAny();
					} else {
						return null;
					}
				}
				
				return TraversalUtil.getChildrenImpl(o);
			}
			
	}
	
	
	
}	
