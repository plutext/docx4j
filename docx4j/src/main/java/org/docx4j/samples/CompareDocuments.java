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
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.diff.ParagraphDifferencer;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.SdtContentBlock;


/**
 * This sample compares the 2 input documents, and renders
 * the result using PDF viewer.
 *
 */
public class CompareDocuments {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String olderfilepath = "/home/dev/workspace/docx4j/sample-docs/differencing_older.docx";
		String newerfilepath = "/home/dev/workspace/docx4j/sample-docs/differencing_newer.docx";
		
				
		// 1. Load the Packages
		WordprocessingMLPackage olderPackage = WordprocessingMLPackage.load(new java.io.File(olderfilepath));
		WordprocessingMLPackage newerPackage = WordprocessingMLPackage.load(new java.io.File(newerfilepath));
		
		Body olderBody = ((Document)olderPackage.getMainDocumentPart().getJaxbElement()).getBody();
		Body newerBody = ((Document)newerPackage.getMainDocumentPart().getJaxbElement()).getBody();
		
		// 2. Do the differencing
		java.io.StringWriter sw = new java.io.StringWriter();
		javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(
				sw);
		Calendar changeDate = null;

		ParagraphDifferencer pd = new ParagraphDifferencer();
		pd.setRelsDiffIdentifier("blagh"); // not necessary in this case 
		pd.diff(olderBody, newerBody, result, "someone", changeDate,
				olderPackage.getMainDocumentPart().getRelationshipsPart(), 
				newerPackage.getMainDocumentPart().getRelationshipsPart());
		
		// 3. Get the result
		String contentStr = sw.toString();
		System.out.println("Result: \n\n " + contentStr);
		Body newBody = (Body) org.docx4j.XmlUtils
				.unmarshalString(contentStr);
		
		// 4. Display the result as a PDF
		// To do this, we'll replace the body in the older document
		((Document)olderPackage.getMainDocumentPart().getJaxbElement()).setBody(newBody);

		RelationshipsPart rp = olderPackage.getMainDocumentPart().getRelationshipsPart(); 
		handleRels(pd, rp);						
		
		
		olderPackage.setFontMapper(new IdentityPlusMapper());		
		org.docx4j.convert.out.pdf.PdfConversion c 
//			= new org.docx4j.convert.out.pdf.viaHTML.Conversion(olderPackage);
			= new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(olderPackage);
//			= new org.docx4j.convert.out.pdf.viaIText.Conversion(olderPackage);		
			c.view();
				
	}

	/**
		 In the general case, you need to handle relationships.
		 Although not necessary in this simple example, 
		 we do it anyway for the purposes of illustration.
		 
	 */
	private static void handleRels(ParagraphDifferencer pd, RelationshipsPart rp) {
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
