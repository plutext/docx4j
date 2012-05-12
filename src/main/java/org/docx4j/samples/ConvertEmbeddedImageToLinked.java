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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.docx4j.wml.Body;


/**
 * Takes existing images internal to the package,
 * and points at them with TargetMode="External",
 * and r:link (instead of r:embed).
 * 
 * Also saves the target part as a file.
 * 
 * @author jharrop
 *
 */
public class ConvertEmbeddedImageToLinked {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 
	
	public String generateTargetUri( String username, String hash, String extension )  {
		
		// See spec 8.3.3
		
		return null;
		
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String BASE_DIR = System.getProperty("user.dir");

		//String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/jpeg.docx";
		String inputfilepath = BASE_DIR + "/png1.docx";
		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/AutoOpen.docm";
		
		boolean saveImages = true;
		boolean saveResultingDoc = true;
		
		String outputfilepath = BASE_DIR + "/imageLinked.docx";		
		
		
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

			if ( r.getType().equals( Namespaces.IMAGE ) ) {
				// 	.. externalise
				r.setTargetMode("External");
				
				String target = r.getTarget();
				System.out.println("target: " + target);
				
				// TODO .. change name to username_hash.type
				
				
				if (saveImages) {
					// Save the image as a file in the specified location
					
					File f = new File(BASE_DIR + "/word/" + target);
					if (f.exists()) {
						System.out.println("Overwriting existing object: " + f.getPath() );
					} else if ( !f.getParentFile().exists() ) {
						f.getParentFile().mkdirs();
					}					
					
					Part p = relsPart.getPart(r);
					FileOutputStream fos = new FileOutputStream( f );
					((BinaryPart)p).writeDataToOutputStream(fos);
					fos.close();
					
				}
				
			}			
		}
	
		
		
		
		
		// Change r:embed to r:link
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getEGBlockLevelElts();
		
		walkJAXBElements(bodyChildren);		
		
				
		// Save it
		
		if (saveResultingDoc) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
		}
	}
	
	static void walkJAXBElements(List <Object> bodyChildren){
	
		for (Object o : bodyChildren ) {

			if ( o instanceof javax.xml.bind.JAXBElement) {
			
				System.out.println( o.getClass().getName() );
				System.out.println( ((JAXBElement)o).getName() );
				System.out.println( ((JAXBElement)o).getDeclaredType().getName() + "\n\n");
					
			} else if (o instanceof org.docx4j.wml.P) {
				System.out.println( "Paragraph object: ");
				
				walkList( ((org.docx4j.wml.P)o).getParagraphContent());
			}
		}
	}
	
	static void walkList(List children){
		
		for (Object o : children ) {					
			System.out.println("  " + o.getClass().getName() );
			if ( o instanceof javax.xml.bind.JAXBElement) {
				System.out.println("      " +  ((JAXBElement)o).getName() );
				System.out.println("      " +  ((JAXBElement)o).getDeclaredType().getName());
				
				// TODO - unmarshall directly to Text.
				if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Text") ) {
					org.docx4j.wml.Text t = (org.docx4j.wml.Text)((JAXBElement)o).getValue();
					System.out.println("      " +  t.getValue() );
					
				} else if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Drawing") ) {
					convertLinkToEmbed( (org.docx4j.wml.Drawing)((JAXBElement)o).getValue() );
				}
				
				
				
			} else if (o instanceof org.w3c.dom.Node) {
				System.out.println(" IGNORED " + ((org.w3c.dom.Node)o).getNodeName() );					
			} else if ( o instanceof org.docx4j.wml.R) {
				org.docx4j.wml.R  run = (org.docx4j.wml.R)o;
				walkList(run.getRunContent());				
				
			} else {
				
				System.out.println(" IGNORED " + o.getClass().getName() );
				
			}
//			else if ( o instanceof org.docx4j.jaxb.document.Text) {
//				org.docx4j.jaxb.document.Text  t = (org.docx4j.jaxb.document.Text)o;
//				System.out.println("      " +  t.getValue() );					
//			}
		}
	}

	
	static void convertLinkToEmbed( org.docx4j.wml.Drawing d ) {
	
		System.out.println(" describeDrawing " );
		
		if ( d.getAnchorOrInline().get(0) instanceof Anchor ) {
			
			System.out.println(" ENCOUNTERED w:drawing/wp:anchor " );
			// That's all for now...
			
		} else if ( d.getAnchorOrInline().get(0) instanceof Inline ) {
			
			// Extract w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed
			
			Inline inline = (Inline )d.getAnchorOrInline().get(0);
			
			Pic pic = inline.getGraphic().getGraphicData().getPic();
						
			System.out.println( "*** image relationship: " +  pic.getBlipFill().getBlip().getEmbed() );
			
			if (pic.getBlipFill().getBlip().getEmbed()!=null) {
				
				String relId = pic.getBlipFill().getBlip().getEmbed();
				// Add r:link
				pic.getBlipFill().getBlip().setLink(relId);
				// Remove r:embed
				pic.getBlipFill().getBlip().setEmbed(null);
				
			}
			
			
		} else {
			
			System.out.println(" Didn't get Inline :(  How to handle " + d.getAnchorOrInline().get(0).getClass().getName() );
		}
		
	}

}
