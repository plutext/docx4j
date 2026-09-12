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
package org.docx4j.model.listnumbering;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.PPrBase.Ind;
import org.junit.Test;

public class ListNumberIndTest {
	
	final static String BASE_DIR = "src/test/java/org/docx4j/model/listnumbering/ind/";

	final static String[] testdocs = { 
		"abstract_style_with.xml", 
		"abstract_style_without.xml", 
		"abstract_nostyle_ppr.xml", 
		"abstract_nostyle_noppr.xml", 
		"override_nostyle_ppr.xml",
		"abstract_style_ind_only.xml",
		"abstract_style_basedon.xml"};  
	
	/* @since 17.1.0 the first is 2880, the level's own w:ind, where it used to be 11, the
	 * w:ind of the paragraph style the level's w:pStyle names.  A w:lvl/w:pPr states the
	 * properties applied to a paragraph at that level (ECMA-376 17.9.24); the w:pStyle
	 * only links the level to a style, and reading the style's w:ind in place of the
	 * level's put an indent from a style the paragraph does not use into every paragraph
	 * whose own w:numPr named the numbering.  Measured against Word (CR-001 §2.8), which
	 * draws such a bullet on the level's indent.  Where the level states none - the second
	 * document here - the linked style's is still the fallback.  A paragraph which does use
	 * the style gets the style's w:ind over the level's from the style chain anyway. */
	/* @since 17.1.1 (CR-014 phase 3) the last two: a level with no w:ind of its own takes
	 * the linked style's (11), and follows w:basedOn to find one (31) - the style chain
	 * is followed here as it is for every other property a style contributes. */
	final static String[] expected = { 
		"2880", 
		"12", 
		"13", 
		null, 
		"23",
		"11",
		"31"};  
		
	
	public static void main(String[] args) throws Exception {

		//testDiffDocx();
	}
	
	@Test
	public void testGetInd() throws Exception {

		for (int i=0; i<testdocs.length; i++){
			
			// Get the document
			
			String filename = testdocs[i];
			System.out.println("Reading " + filename);
			
			try {
				JAXBContext jc = Context.jcXmlPackage;
				Unmarshaller u = jc.createUnmarshaller();
				u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
				
				Object o = u.unmarshal(
						new javax.xml.transform.stream.StreamSource(new FileInputStream(BASE_DIR + filename)));
				
				org.docx4j.xmlPackage.Package wmlPackageEl = null;
				if (o instanceof org.docx4j.xmlPackage.Package) {
					// MOXy
					wmlPackageEl = (org.docx4j.xmlPackage.Package)o;
				} else {
					wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)o).getValue(); 
				}

				org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl); 

				WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage)xmlPackage.get(); 
				
				// Get the Ind value
				NumberingDefinitionsPart ndp = wmlPackage.getMainDocumentPart().getNumberingDefinitionsPart();
				// Force initialisation of maps
				ndp.getEmulator();
				
				Ind ind = ndp.getInd("1", "0");
				
				if (ind!=null) {
					assertEquals( ind.getLeft().toString(), expected[i] );
					System.out.println( "<w:ind w:left='" + ind.getLeft().toString() + "\n\n" );
				} else {
					assertEquals( ind, expected[i] );					
					System.out.println( "w:ind was null\n\n" );
				}
				
			} catch (Exception exc) {
				exc.printStackTrace();
				throw new RuntimeException(exc);
			}
						
		}
		
		
	}
	
}
