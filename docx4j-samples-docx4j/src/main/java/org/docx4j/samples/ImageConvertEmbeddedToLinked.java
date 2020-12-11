/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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
import java.io.FileOutputStream;
import java.util.List;

import org.docx4j.dml.CTBlip;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.Body;


/**
 * Takes existing images internal to the package,
 * and points at them with TargetMode="External",
 * and r:link (instead of r:embed).
 * 
 * Optionally saves the target part as a file.
 * 
 * @author jharrop
 *
 */
public class ImageConvertEmbeddedToLinked {
		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String BASE_DIR = System.getProperty("user.dir");

		String inputfilepath = BASE_DIR + "/sample-docs/sample-docx.docx";
		
		boolean saveImages = true;
		boolean saveResultingDoc = true;
		
		String outputfilepath = BASE_DIR + "/OUT_ConvertEmbeddedImageToLinked.docx";		
		
		
		// Open a document from the file system
		// Load the Package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// Change the rels to TargetMode="External"
		// Fetch rels part
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		RelationshipsPart relsPart = documentPart.getRelationshipsPart();
		Relationships rels = relsPart.getRelationships();
		List<Relationship> relsList = rels.getRelationship();
		
		// For each image rel
		for (Relationship r : relsList) {

			System.out.println(r.getTargetMode());
			if ( r.getType().equals( Namespaces.IMAGE )
					&& (r.getTargetMode()==null
							|| r.getTargetMode().equalsIgnoreCase("internal"))) {
				
				String target = r.getTarget();
				System.out.println("target: " + target);
				
				if (saveImages) {
					// Save the image as a file in the specified location
					
					File f = new File(BASE_DIR + "/" + target);
					if (f.exists()) {
						System.out.println("Overwriting existing object: " + f.getPath() );
					} else if ( !f.getParentFile().exists() ) {
						System.out.println("creating " + f.getParentFile().getAbsolutePath() );
						f.getParentFile().mkdirs();
						//f = new File(BASE_DIR + "/" + target);
					}					
					
					Part p = relsPart.getPart(r);
					FileOutputStream fos = new FileOutputStream( f );
					((BinaryPart)p).writeDataToOutputStream(fos);
					fos.close();
					
				}
				// 	.. externalise - after getPart!
				r.setTargetMode("External");
				
			}			
		}
			
		// Change r:embed to r:link
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
						
		SingleTraversalUtilVisitorCallback imageVisitor 
			= new SingleTraversalUtilVisitorCallback(
					new TraversalUtilBlipVisitor());
		imageVisitor.walkJAXBElements(body);
				
				
		// Save it
		
		if (saveResultingDoc) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
		}
	}
	
	

	/** 
	 * <w:drawing>
		<wp:inline distT="0" distB="0" distL="0" distR="0">
			<a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
				<a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
					<pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
						<pic:blipFill>
							<a:blip r:embed="rId5" />	
	 */
	public static class TraversalUtilBlipVisitor extends TraversalUtilVisitor<CTBlip> {
		
		@Override
		public void apply(CTBlip element, Object parent, List<Object> siblings) {

			if (element.getEmbed()!=null) {
				
				String relId = element.getEmbed();
				// Add r:link
				element.setLink(relId);
				// Remove r:embed
				element.setEmbed(null);
				
				System.out.println("Converted a:blip with relId " + relId);
				
			}
		}
	
	}
	
	/** 
		<w:pict>
			:
			<v:shape id="_x0000_i1025" type="#_x0000_t75" 
				style="width:428.25pt;height:321pt">
				<v:imagedata r:id="rId4" o:title="" />  <---- no link/embed attribute 
			</v:shape>
		</w:pict>
	 */
	
	
}
