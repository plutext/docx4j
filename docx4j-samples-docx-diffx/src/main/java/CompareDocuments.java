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



import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.diff.Differencer;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;

import com.topologi.diffx.Docx4jDriver;


/**
 * This sample compares the 2 input documents, and renders
 * the result using PDF viewer.
 *
 */
public class CompareDocuments {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 
	
	static boolean DOCX_SAVE = true;

	static boolean PDF_SAVE = false;
	
	
	/**
	 * Split up the problem to try to solve more quickly
	 * (you might try this when you have 500 entries or more)
	 */
	static boolean DIVIDE_AND_CONQUER = false;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String newerfilepath = System.getProperty("user.dir") + "/test2.docx";
		String olderfilepath = System.getProperty("user.dir") + "/test.docx";
		
		// 1. Load the Packages
		WordprocessingMLPackage newerPackage = WordprocessingMLPackage.load(new java.io.File(newerfilepath));
		WordprocessingMLPackage olderPackage = WordprocessingMLPackage.load(new java.io.File(olderfilepath));
		
		Body newerBody = ((Document)newerPackage.getMainDocumentPart().getJaxbElement()).getBody();
		Body olderBody = ((Document)olderPackage.getMainDocumentPart().getJaxbElement()).getBody();
		
		
		// 2. Do the differencing
		java.io.StringWriter sw = new java.io.StringWriter();
		javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(
				sw);
		Calendar changeDate = null;
		
		Differencer pd = null;
		if (DIVIDE_AND_CONQUER) {

			System.out.println("Differencing with DIVIDE_AND_CONQUER..");
			
			java.io.StringWriter swInterim = new java.io.StringWriter();
			
			Docx4jDriver.diff( XmlUtils.marshaltoW3CDomDocument(newerBody).getDocumentElement(),
					XmlUtils.marshaltoW3CDomDocument(olderBody).getDocumentElement(),
					swInterim);
				// The signature which takes Reader objects appears to be broken
			
			// Now, feed it through diff to wml XSLT
			pd = new Differencer();
			pd.toWML(swInterim.toString(), result, "someone", changeDate,
					newerPackage.getMainDocumentPart().getRelationshipsPart(),
					olderPackage.getMainDocumentPart().getRelationshipsPart() 
					);
			
		} else {

			System.out.println("Differencing without dividing..");
			
			pd = new Differencer();
			pd.setRelsDiffIdentifier("blagh"); // not necessary in this case 
			pd.diff(newerBody, olderBody, result, "someone", changeDate,
					newerPackage.getMainDocumentPart().getRelationshipsPart(),
					olderPackage.getMainDocumentPart().getRelationshipsPart() 
					);
		}
		
		// 3. Get the result
		String contentStr = sw.toString();
//		System.out.println("Result: \n\n " + contentStr);
		Body newBody = (Body) XmlUtils.unwrap(
				XmlUtils.unmarshalString(contentStr));
		
		// 4. Display the result as a PDF
		// To do this, we'll replace the body in the newer document
		((Document)newerPackage.getMainDocumentPart().getJaxbElement()).setBody(newBody);

		if (DIVIDE_AND_CONQUER) {
			// No image support at present
		} else {
			handleRels(pd, newerPackage.getMainDocumentPart()); // TODO: that needs work, for more complex input
		}
		
		
		if (DOCX_SAVE) {
			newerPackage.save(new File(System.getProperty("user.dir") +"/OUT_CompareDocuments.docx"));
		}
		
		if (PDF_SAVE) {
			
			newerPackage.setFontMapper(new IdentityPlusMapper());	
			
			boolean saveFO = false;
			String outputfilepath = System.getProperty("user.dir") +  "/OUT_CompareDocuments.pdf";
			
			// FO exporter setup (required)
			// .. the FOSettings object
	    	FOSettings foSettings = Docx4J.createFOSettings();
			if (saveFO) {
				foSettings.setFoDumpFile(new java.io.File(System.getProperty("user.dir") +  "/OUT_CompareDocuments..fo"));
			}
			foSettings.setOpcPackage(newerPackage);
			// Document format: 
			// The default implementation of the FORenderer that uses Apache Fop will output
			// a PDF document if nothing is passed via 
			// foSettings.setApacheFopMime(apacheFopMime)
			// apacheFopMime can be any of the output formats defined in org.apache.fop.apps.MimeConstants or
			// FOSettings.INTERNAL_FO_MIME if you want the fo document as the result.
			
			
			// exporter writes to an OutputStream.		
			OutputStream os = new java.io.FileOutputStream(outputfilepath);
	    	
	
			//Don't care what type of exporter you use
			Docx4J.toFO(foSettings, os, Docx4J.FLAG_NONE);
			
			System.out.println("Saved " + System.getProperty("user.dir")  +  "/OUT_CompareDocuments.pdf");
		}
				
	}

	/**
		 In the general case, you need to handle relationships.
		 Although not necessary in this simple example, 
		 we do it anyway for the purposes of illustration.
	 * @throws InvalidFormatException 
		 
	 */
	private static void handleRels(Differencer pd, MainDocumentPart newMDP) throws InvalidFormatException {
		
		RelationshipsPart rp = newMDP.getRelationshipsPart(); 
		System.out.println("before: \n" + rp.getXML());		
		
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
		Map<Relationship, Part> newRels = pd.getComposedRels();
		for (Relationship nr : newRels.keySet()) {	
			
			if (nr.getTargetMode()!=null 
					&& nr.getTargetMode().equals("External")) {
				
				newMDP.getRelationshipsPart().getRelationships().getRelationship().add(nr);
				
			} else {
				
				Part part = newRels.get(nr);
				if (part==null) {
					System.out.println("ERROR! Couldn't find part for rel " + nr.getId() + "  " + nr.getTargetMode() );
				} else {
					
					if (part instanceof BinaryPart) { // ensure contents are loaded, before moving to new pkg
						((BinaryPart)part).getBuffer();
					}
					
					newMDP.addTargetPart(part, AddPartBehaviour.RENAME_IF_NAME_EXISTS, nr.getId());
				}
			}
		}
		
		System.out.println("after: \n" + rp.getXML());
		
	}
	
	
		

}
