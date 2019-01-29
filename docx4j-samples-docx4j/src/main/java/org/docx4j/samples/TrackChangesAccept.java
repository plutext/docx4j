/*
 *  Copyright 2017, Plutext Pty Ltd.
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


import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Document;
import org.w3c.dom.Element;


/**
 * Accept changes (r:ins, r:del) in the Main Document Part, via XSLT
 * 
 * @author jharrop
 * @since 3.3.6
 */
public class TrackChangesAccept extends AbstractSample {
	
	
	
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/OUT_CompareRsid_Newer.docx";
		}
		System.out.println(inputfilepath);	    	
		boolean SAVE = false;
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));
		
		// Load the XLST
		Source xsltSource  = new StreamSource(
				ResourceUtils.getResource(
						"AcceptChanges.xslt")
				);
		Templates xslt = XmlUtils.getTransformerTemplate(xsltSource);
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		DOMResult contentAccepted = new DOMResult(); 
		
		// perform the transformation
		mdp.transform(xslt, null, contentAccepted);
		
		// replace the contents in the WordprocessingMLPackage
		org.w3c.dom.Document domDoc = (org.w3c.dom.Document)contentAccepted.getNode();
		mdp.setContents(
				mdp.unmarshal(domDoc.getDocumentElement()));
		
		System.out.println(mdp.getXML());
				
		// Save it
		if (SAVE) {
			String outputfilepath = System.getProperty("user.dir") + "/OUT_TrackChangesAccept.docx";
			Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
			
			System.out.println("Saved: " + outputfilepath);
		}
	}
		

}
