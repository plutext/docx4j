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

package org.docx4j.openpackaging;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.io3.Load3;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * Demonstrate replacing the contents of WordprocessingMLPackage.
 *
 */
public class PackageObjectReuse {
	
	public static void main(String[] args) throws Exception {

	    String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.docx";
		
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));
		
		// Make pkg2
		WordprocessingMLPackage wordMLPackage2 = (WordprocessingMLPackage)wordMLPackage.clone();
		wordMLPackage2.getMainDocumentPart().addParagraphOfText("This is pkg 2");

		// Now we'll load pkg2 back into original
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage2);
		saver.save(baos);		
		final ZipPartStore partLoader = new ZipPartStore( new ByteArrayInputStream(baos.toByteArray() ));
		final Load3 loader = new Load3(partLoader);
		loader.reuseExistingOpcPackage(wordMLPackage); // reuse existing object
		OpcPackage resultPackage = loader.get();
		
		// Should say its pkg 2
		System.out.println(
				((WordprocessingMLPackage)resultPackage).getMainDocumentPart().getXML());
		
		// Should be the same
		System.out.println(wordMLPackage.hashCode());
		System.out.println(resultPackage.hashCode());
		System.out.println(wordMLPackage.hashCode()==resultPackage.hashCode());

	}
	

}
