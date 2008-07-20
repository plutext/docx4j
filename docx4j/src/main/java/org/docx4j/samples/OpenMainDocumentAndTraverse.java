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


import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


public class OpenMainDocumentAndTraverse {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		//String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/jpeg.docx";
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/fonts-modesOfApplication.docx";
		
		boolean save = false;
		String outputfilepath = "/home/dev/tmp/test-out.docx";		
		
		
		// Open a document from the file system
		// 1. Load the Package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// 2. Fetch the document part 		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// Display its contents 
		System.out.println( "\n\n OUTPUT " );
		System.out.println( "====== \n\n " );	
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getEGBlockLevelElts();
		
		walkJAXBElements(bodyChildren);
			
//		// Change something
//		org.docx4j.wml.P p = (org.docx4j.wml.P)((JAXBElement)bodyChildren.get(2)).getValue();
		org.docx4j.wml.P p = (org.docx4j.wml.P)bodyChildren.get(2);
		
		//walkList(p.getParagraphContent());
		
		org.docx4j.wml.PPr pPr = p.getPPr();
		
		if (pPr!=null && pPr.getPStyle()!=null) {
			System.out.println( "Style: " + pPr.getPStyle().getVal() );
		}		
		
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		org.docx4j.wml.R  run = factory.createR();
		org.docx4j.wml.Text  t = factory.createText();
				
		
		t.setValue("SOMETHING NEW, with added JAXB convenience!");
		
		run.getRunContent().add(t);		
		
		org.docx4j.wml.RPr  runProps = factory.createRPr();
		
		run.setRPr( runProps); 
		
		org.docx4j.wml.BooleanDefaultTrue val = factory.createBooleanDefaultTrue();
		val.setVal(Boolean.valueOf(true));
		runProps.setB( val );
		
		// or relying on the default value, could just do:
		// runProps.setB( factory.createBooleanDefaultTrue() );
		
		p.getParagraphContent().add(run);
		
//		System.out.println( "/n/n What does that look like? /n/n");
//		walkList(p.getParagraphContent());
				
		// Save it
		
		if (save) {		
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
				
				if (((org.docx4j.wml.P)o).getPPr() != null
						&& ((org.docx4j.wml.P)o).getPPr().getRPr() != null
						&& ((org.docx4j.wml.P)o).getPPr().getRPr().getB() !=null) {
					System.out.println( "For a ParaRPr bold!");
				}
				
				
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
					describeDrawing( (org.docx4j.wml.Drawing)((JAXBElement)o).getValue() );
				}
				
				
				
			} else if (o instanceof org.w3c.dom.Node) {
				System.out.println(" IGNORED " + ((org.w3c.dom.Node)o).getNodeName() );					
			} else if ( o instanceof org.docx4j.wml.R) {
				org.docx4j.wml.R  run = (org.docx4j.wml.R)o;
				if (run.getRPr()!=null) {
					System.out.println("      " +   "Properties...");
					if (run.getRPr().getB()!=null) {
						System.out.println("      " +   "B not null ");						
						System.out.println("      " +   "--> " + run.getRPr().getB().isVal() );
					} else {
						System.out.println("      " +   "B null.");												
					}
				}
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
	
	static void describeDrawing( org.docx4j.wml.Drawing d ) {
	
		System.out.println(" describeDrawing " );
		
		if ( d.getAnchorOrInline().get(0) instanceof org.docx4j.dml.Anchor ) {
			
			System.out.println(" ENCOUNTERED w:drawing/wp:anchor " );
			// That's all for now...
			
		} else if ( d.getAnchorOrInline().get(0) instanceof org.docx4j.dml.Inline ) {
			
			// Extract w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed
			
			org.docx4j.dml.Inline inline = (org.docx4j.dml.Inline )d.getAnchorOrInline().get(0);
			
			org.docx4j.dml.Pic pic = inline.getGraphic().getGraphicData().getPic();
						
			System.out.println( "image relationship: " +  pic.getBlipFill().getBlip().getEmbed() );
			
			
		} else {
			
			System.out.println(" Didn't get Inline :(  How to handle " + d.getAnchorOrInline().get(0).getClass().getName() );
		}
		
	}

}
