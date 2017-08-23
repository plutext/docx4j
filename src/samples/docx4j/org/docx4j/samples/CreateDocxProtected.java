/*
 *  Copyright 2015, Plutext Pty Ltd.
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

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.ProtectDocument;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.STDocProtect;

/**
 * Protect document against editing in Word.
 * 
 * Note: encyption, signing are different concepts. For encryption, use Docx4J.save 
 * with Docx4J.FLAG_SAVE_ENCRYPTED_AGILE. For signing, you need the Enterprise edition.
 * 
 * WARNING: this doc protect functionality may give a false sense of security, since it only affects
 * the behaviour of Word's user interface. A mischevious user could still edit the document
 * in some other program, and subsequent users would *not* be warned it has been tampered with. 
 * 
 * So if you protect the document against editing, you should also sign it. A signature will warn
 * if the docx has been tampered with (unless the signature is removed entirely, which is easy enough).
 * 
 * (Encrypting the docx when you
 * save it doesn't help much in this scenario, since you have to share the password with viewers - there
 * are no separate passwords for viewing versus editing). 
 * 
 * @author Jason Harrop
 * @since 3.3.0
 */
public class CreateDocxProtected extends AbstractSample {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");
		
		ProtectDocument protection = new ProtectDocument(wordMLPackage);
		protection.restrictEditing(STDocProtect.READ_ONLY, "foobaa");
		
		
		String filename = System.getProperty("user.dir") + "/protect.docx";
		Docx4J.save(wordMLPackage, new java.io.File(filename), Docx4J.FLAG_SAVE_ZIP_FILE); 
		
		// To save encrypted, you'd use FLAG_SAVE_ENCRYPTED_AGILE, for example:
		// Docx4J.save(wordMLPackage, new java.io.File(filename), Docx4J.FLAG_SAVE_ENCRYPTED_AGILE, "foo"); 
		
		
		System.out.println("Saved " + filename);
						
	}
	
}
