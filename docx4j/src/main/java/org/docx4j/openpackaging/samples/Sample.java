/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4J.

    Docx4J is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4J.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4j.openpackaging.samples;


import java.util.ArrayList;

import org.docx4j.document.wordprocessingml.Paragraph;
import org.docx4j.document.wordprocessingml.Run;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.dom4j.Element;


public class Sample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = "/home/jharrop/tmp/simple.docx";
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
		ArrayList bodyChildren = documentPart.getBody();
		for (Object o : bodyChildren ) {			
			if ( o instanceof Paragraph ) {
				System.out.println( "Paragraph object: " + ((Paragraph)o).getText() );
			} else {
				System.out.println( "xml element: " + ((Element)o).getName() );
			}
		}
		
		
		// Change something
		Paragraph para = (Paragraph)bodyChildren.get(2);
			/* TODO: Paragraph class needs appropriate methods editing its existing runs. 
			 * Here we just add a new run of text to it.  */
		Run newRun = new Run("SOMETHING NEW, with added convenience!");
		para.addRun(newRun);			
		
		// Save it
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(outputfilepath);	
		

	}

}
