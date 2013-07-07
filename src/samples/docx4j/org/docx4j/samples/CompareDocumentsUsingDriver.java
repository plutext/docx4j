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


import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.diff.Differencer;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;

import com.topologi.diffx.Docx4jDriver;


/**
 * This sample compares the 2 input documents, and renders
 * the result using PDF viewer.
 *
 */
public class CompareDocumentsUsingDriver {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String newerfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docxv2.docx";
		String olderfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.docx";
		//String olderfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml"; // TODO FIXME breaks if we use this
						
		// 1. Load the Packages
		WordprocessingMLPackage newerPackage = WordprocessingMLPackage.load(new java.io.File(newerfilepath));
		WordprocessingMLPackage olderPackage = WordprocessingMLPackage.load(new java.io.File(olderfilepath));
		
		Body newerBody = ((Document)newerPackage.getMainDocumentPart().getJaxbElement()).getBody();
		Body olderBody = ((Document)olderPackage.getMainDocumentPart().getJaxbElement()).getBody();
		
		System.out.println("Differencing..");
		
		// 2. Do the differencing
		java.io.StringWriter sw = new java.io.StringWriter();
		

		Docx4jDriver.diff( XmlUtils.marshaltoW3CDomDocument(newerBody).getDocumentElement(),
				XmlUtils.marshaltoW3CDomDocument(olderBody).getDocumentElement(),
				   sw);
			// The signature which takes Reader objects appears to be broken
		
		// 3. Get the result
		String contentStr = sw.toString();
		System.out.println("Result: \n\n " + contentStr);
		Body newBody = (Body) org.docx4j.XmlUtils
				.unmarshalString(contentStr);
		
		// 4. Display the result as a PDF
		// To do this, we'll replace the body in the newer document
		((Document)newerPackage.getMainDocumentPart().getJaxbElement()).setBody(newBody);

		 // In the general case, you need to handle relationships.  Not done here!
		
//		RelationshipsPart rp = newerPackage.getMainDocumentPart().getRelationshipsPart(); 
//		handleRels(pd, rp);						
		
		
		newerPackage.setFontMapper(new IdentityPlusMapper());		
		org.docx4j.convert.out.pdf.PdfConversion c 
			= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(newerPackage);

		OutputStream os = new java.io.FileOutputStream(System.getProperty("user.dir") +  "/COMPARED.pdf");			
		c.output(os, new PdfSettings() );
		System.out.println("Saved " + System.getProperty("user.dir") +  "/COMPARED.pdf");
				
	}

	/**
		 In the general case, you need to handle relationships.
		 Although not necessary in this simple example, 
		 we do it anyway for the purposes of illustration.
		 
	 */
	private static void handleRels(Differencer pd, RelationshipsPart rp) {
		// Since we are going to add rels appropriate to the docs being 
		// compared, for neatness and to avoid duplication
		// (duplication of internal part names is fatal in Word,
		//  and export xslt makes images internal, though it does avoid duplicating
		//  a part ), 
		// remove any existing rels which point to images
		List<Relationship> relsToRemove = new ArrayList<Relationship>();
		for (Relationship r : rp.getRelationships().getRelationship() ) {
			//  Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
			if (r.getType().equals(Namespaces.IMAGE)) {
				relsToRemove.add(r);
			}
		}						
		for (Relationship r : relsToRemove) {				
			rp.removeRelationship(r);
		}
		
		// Now add the rels we composed
		List<Relationship> newRels = pd.getComposedRels();
		for (Relationship nr : newRels) {							
			rp.addRelationship(nr);
		}
	}
	
	
		

}
