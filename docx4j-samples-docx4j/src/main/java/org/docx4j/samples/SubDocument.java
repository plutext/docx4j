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

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTRel;


/**
 * Master and Subdocuments.
 * 
 * A subdocument is just an external part.
 * 
 * So this sample is very similar to the hyperlink one.
 * 
 * In not sure where Master Document Tools are
 * in Word 2010 ribbons, but you can add them
 * via Word > Options
 * 
 * @author Jason Harrop
 */
public class SubDocument  {
	
	/*
	 *
	    <w:p>
	      <w:pPr>
	        <w:pStyle w:val="Heading1"/>
	      </w:pPr>
	      <w:subDoc r:id="rId4"/>
	      <w:r>
	        <w:t>Another heading</w:t>
	      </w:r>
	    </w:p>
	    
	 * word/_rels/document.xml.rels contains:
	 * 
	 * <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/subDocument" 
	 * 		Target="http://dev.plutext.org/" TargetMode="External"/>

	 */

	public static void main(String[] args) throws Exception {
		
		// Create the master doc, and specify
		// the subdoc 
		String subdocx = ".\\sample-docs\\word\\sample-docx.xml";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart(); 
		
		// Link to subdoc
		JAXBElement<CTRel> subdoc = createSubdoc(mdp, subdocx);
		
		// Add it to a paragraph
		org.docx4j.wml.ObjectFactory wmlFactory = Context.getWmlObjectFactory(); 
		org.docx4j.wml.P paragraph = wmlFactory.createP();
		
		paragraph.getContent().add( subdoc );
		mdp.addObject(paragraph);
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_SubDocumentMASTER.docx") );
		
	}
	
	public static JAXBElement<CTRel> createSubdoc(MainDocumentPart mdp, 
			String subdocName) {
		
		try {

			// We need to add a relationship to word/_rels/document.xml.rels
			// but since its external, we don't use the 
			// usual wordMLPackage.getMainDocumentPart().addTargetPart
			// mechanism
			org.docx4j.relationships.ObjectFactory factory =
				new org.docx4j.relationships.ObjectFactory();
			
			org.docx4j.relationships.Relationship rel = factory.createRelationship();
			rel.setType( Namespaces.SUBDOCUMENT  );
			rel.setTarget(subdocName);
			rel.setTargetMode("External");  
									
			mdp.getRelationshipsPart().addRelationship(rel);
			
			// addRelationship sets the rel's @Id
			

            org.docx4j.wml.ObjectFactory wmlOF = new org.docx4j.wml.ObjectFactory();              
            
            CTRel ctRel = wmlOF.createCTRel();
            ctRel.setId(rel.getId());      

			return wmlOF.createPSubDoc(ctRel);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
		
}

