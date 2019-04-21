/*
 *  Copyright 2007-2012, Plutext Pty Ltd.
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



import java.io.File;

import org.apache.commons.io.FileUtils;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * Convert a pptx to 'Flat OPC XML' format,
 * which Word/Powerpoint can happily read, and which 
 * is convenient for editing in an XML editor.
 * 
 * @author jharrop
 *
 */
public class ConvertOutFlatOpenPackage  {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample.pptx";
		OpcPackage pptxPackage = OpcPackage.load(new java.io.File(inputfilepath));
		
		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(pptxPackage);
		
		org.docx4j.xmlPackage.Package result = worker.get();
		
		boolean suppressDeclaration = true;
		boolean prettyprint = true;
		
		
		String data = 
				org.docx4j.XmlUtils.
					marshaltoString(result, suppressDeclaration, prettyprint, 
							org.docx4j.jaxb.Context.jcXmlPackage);
		
		FileUtils.writeStringToFile(
				new File(System.getProperty("user.dir") + "/pptx.xml"), 
				data);
		

		
	}

}
