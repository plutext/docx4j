/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.samples;


import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


public class Sample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		//String inputfilepath = "/home/jharrop/tmp/simple.docx";
		String inputfilepath = "/home/jharrop/tmp/numbering.docx";
		String outputfilepath = "/home/jharrop/tmp/simple-out.docx";
		
		
		// Open a document from the file system
		// 1. Load the Package
		LoadFromZipFile loader = new LoadFromZipFile();
		WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)loader.get(inputfilepath);	
		
		// 2. Fetch the document part 
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();			
		//MainDocumentPart documentPart = (MainDocumentPart)wordMLPackage.getPart( new PartName( "/word/document.xml" ) );
		// Display its contents 
		System.out.println( "\n\n OUTPUT " );
		System.out.println( "====== \n\n " );	
		
//		org.docx4j.jaxb.document.Document wmlDocumentEl = documentPart.getDocumentObj();
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getBlockLevelElements();
		
		walkJAXBElements(bodyChildren);
			
//		// Change something
		org.docx4j.wml.P p = (org.docx4j.wml.P)((JAXBElement)bodyChildren.get(2)).getValue();
		
		//walkList(p.getParagraphContent());
		
		org.docx4j.wml.PPr pPr = p.getPPr();
		
		if (pPr!=null) {
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
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(outputfilepath);
		
	}
	
	static void walkJAXBElements(List <Object> bodyChildren){
	
		for (Object o : bodyChildren ) {
						
			if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.P") ) {
				System.out.println( "Paragraph object: ");
				org.docx4j.wml.P p = (org.docx4j.wml.P)((JAXBElement)o).getValue();
				
//				if (p.getPPr()!=null) {
//					System.out.println( "Properties...");					
//				}
				
				walkList(p.getParagraphContent());
				
				
			} else {
				System.out.println( o.getClass().getName() );
				System.out.println( ((JAXBElement)o).getName() );
				System.out.println( ((JAXBElement)o).getDeclaredType().getName() + "\n\n");
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
				}
				
			} else if ( o instanceof org.apache.xerces.dom.ElementNSImpl) {
				System.out.println("      " +  ((org.apache.xerces.dom.ElementNSImpl)o).getNodeName() );					
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
			} 
//			else if ( o instanceof org.docx4j.jaxb.document.Text) {
//				org.docx4j.jaxb.document.Text  t = (org.docx4j.jaxb.document.Text)o;
//				System.out.println("      " +  t.getValue() );					
//			}
		}
	}
	

}
