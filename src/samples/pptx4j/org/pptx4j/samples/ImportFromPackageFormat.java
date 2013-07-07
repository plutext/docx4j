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

package org.pptx4j.samples;



import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.PresentationMLPackage;

/* 
 * This is an example of loading a file from the pkg format Powerpoint 2007
 * */
public class ImportFromPackageFormat {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/xls.xml";
		
		java.io.FileInputStream fin = new java.io.FileInputStream(inputfilepath);
							
		// Do we want to save output? 
		boolean save = true;
		// If so, where to?
		String outputfilepath = inputfilepath + ".pptx";
		
		try {
			JAXBContext jc = Context.jcXmlPackage;
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
					new javax.xml.transform.stream.StreamSource(new FileInputStream(inputfilepath)))).getValue(); 

			org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl); 

			PresentationMLPackage pmlPackage = (PresentationMLPackage)xmlPackage.get(); 
			
			// Now that its loaded properly, lets just save it
			
			if (save) {
				SaveToZipFile saver = new SaveToZipFile(pmlPackage);
				saver.save(outputfilepath);
				System.out.println( "\n\n .. written to " + outputfilepath);
			} else {
				// Display its contents 
				System.out.println( "\n\n..done " );
			}
			
			
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
				
	}
	
	

}
