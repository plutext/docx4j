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
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTSettings;

/**
 * Creates a WordprocessingML document from scratch,
 * and attaches a template (for example, instead of Normal.dot)
 * 
 * Be sure to set String templatePath. 
 * 
 * In Flat OPC terms, aim is to produce:
 * 
  <pkg:part pkg:name="/word/settings.xml" pkg:contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml">
    <pkg:xmlData>
      <w:settings xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main">
        <w:attachedTemplate r:id="rId1"/>
      </w:settings>
    </pkg:xmlData>
  </pkg:part>
 *   <pkg:part pkg:name="/word/_rels/settings.xml.rels" pkg:contentType="application/vnd.openxmlformats-package.relationships+xml">
    <pkg:xmlData>
      <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/attachedTemplate" Target="file:///C:\Users\jharrop\AppData\Roaming\Microsoft\Templates\fabdocx-release-20101002B.dotm" TargetMode="External"/>
      </Relationships>
    </pkg:xmlData>
  </pkg:part>

 * 
 * @author Jason Harrop
 */
public class TemplateAttach extends AbstractSample {

	public static void main(String[] args) throws Exception {
		
		String templatePath = "file:///C:\\Users\\jsmith\\AppData\\Roaming\\Microsoft\\Templates\\yours.dotm";
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/OUT_TemplateAttach.docx";	    	
		}
		
		boolean save = 
			(inputfilepath == null ? false : true);
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		wordMLPackage.getMainDocumentPart()
			.addStyledParagraphOfText("Title", "Hello world");

		wordMLPackage.getMainDocumentPart().addParagraphOfText("from docx4j!");
		
		// Create settings part, and init content
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		CTSettings settings = Context.getWmlObjectFactory().createCTSettings();
		dsp.setJaxbElement(settings);
		wordMLPackage.getMainDocumentPart().addTargetPart(dsp);
		
		// Create external rel
		RelationshipsPart rp = RelationshipsPart.createRelationshipsPartForPart(dsp); 		
		org.docx4j.relationships.Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType( Namespaces.ATTACHED_TEMPLATE  );
		rel.setTarget(templatePath);
		rel.setTargetMode("External");  		
		rp.addRelationship(rel); // addRelationship sets the rel's @Id
		
		settings.setAttachedTemplate(
				(CTRel)XmlUtils.unmarshalString("<w:attachedTemplate xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + rel.getId() + "\"/>", Context.jc, CTRel.class)
				);
		 
		// or (yuck)... 
//		CTRel id = new CTRel();
//		id.setId( rel.getId() );
//		JAXBElement<CTRel> je = new JAXBElement<CTRel>(
//				new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "attachedTemplate"), 
//				CTRel.class, null, id);
//		settings.setAttachedTemplate(je.getValue());
		
		// Now save it
		if (save) {
			wordMLPackage.save(new java.io.File(inputfilepath) );
		} else {
			System.out.println( XmlUtils.marshaltoString(dsp.getJaxbElement(), true, true, dsp.getJAXBContext()) );
		}
	}
	
	
}
