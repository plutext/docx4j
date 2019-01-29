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

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.P.Hyperlink;


/**
 * Fun with hyperlinks to external resources
 * eg web pages.
 * 
 * For an example of an internal hyperlink,
 * see the BookmarkAdd sample.
 * 
 * @author Jason Harrop
 */
public class HyperlinkTest {
	
	/*
	 * <w:p>
	 * 	<w:r>
	 * 		<w:t xml:space="preserve">Here is an example of a </w:t>
	 *  </w:r>
	 *  <w:hyperlink r:id="rId4" w:history="1">
	 *  	<w:r>
	 *  		<w:rPr>
	 *  			<w:rStyle w:val="Hyperlink"/>
	 *  		</w:rPr>
	 *  		<w:t>hyperlink</w:t>
	 *  	</w:r>
	 *  </w:hyperlink>
	 *  <w:r>
	 *  	<w:t xml:space="preserve">.  </w:t>
	 *  </w:r>
	 * </w:p>
	 * 
	 * 
	 * word/_rels/document.xml.rels contains:
	 * 
	 * <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink" 
	 * 		Target="http://dev.plutext.org/" TargetMode="External"/>

	 */

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart(); 
		
		// Create hyperlink
		Hyperlink link = createHyperlink(mdp, "http://slashdot.org");
		
		// Add it to a paragraph
		org.docx4j.wml.P paragraph = Context.getWmlObjectFactory().createP();
		paragraph.getContent().add( link );
		mdp.addObject(paragraph);
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_HyperlinkTest.docx") );
		
		// Uncomment to display the result as Flat OPC XML
//		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
//		worker.marshal(System.out);				
				
	}
	
	public static Hyperlink createHyperlink(MainDocumentPart mdp, String url) {
		
		try {

			// We need to add a relationship to word/_rels/document.xml.rels
			// but since its external, we don't use the 
			// usual wordMLPackage.getMainDocumentPart().addTargetPart
			// mechanism
			org.docx4j.relationships.ObjectFactory factory =
				new org.docx4j.relationships.ObjectFactory();
			
			org.docx4j.relationships.Relationship rel = factory.createRelationship();
			rel.setType( Namespaces.HYPERLINK  );
			rel.setTarget(url);
			rel.setTargetMode("External");  
									
			mdp.getRelationshipsPart().addRelationship(rel);
			
			// addRelationship sets the rel's @Id
			
			String hpl = "<w:hyperlink r:id=\"" + rel.getId() + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >" +
            "<w:r>" +
            "<w:rPr>" +
            "<w:rStyle w:val=\"Hyperlink\" />" +  // TODO: enable this style in the document!
            "</w:rPr>" +
            "<w:t>Link</w:t>" +
            "</w:r>" +
            "</w:hyperlink>";

//			return (Hyperlink)XmlUtils.unmarshalString(hpl, Context.jc, P.Hyperlink.class);
			return (Hyperlink)XmlUtils.unmarshalString(hpl);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
		
}

