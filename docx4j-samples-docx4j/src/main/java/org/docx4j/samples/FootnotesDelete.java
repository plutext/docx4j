/*
 *  Copyright 2018, Plutext Pty Ltd.
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

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.R;

/**
 * Remove all footnotes 
 * 
 * @author Jason Harrop
 */
public class FootnotesDelete  {
	
	static org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File("manyFootnotes.docx"));
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		// Setup FootnotesPart if necessary,
		// along with DocumentSettings
		FootnotesPart footnotesPart = mdp.getFootnotesPart();
		if (footnotesPart==null) { 
			System.out.println("No FootnotesPart, so nothing to do. " );
			return;
		}
		
		// Delete the footnotes part.
		mdp.getRelationshipsPart().removePart(footnotesPart.getPartName());
		
		// Now go through the document, deleting...
		Body body =  mdp.getJaxbElement().getBody();
		
		/* Can't do it this way for 2 reasons:
		 * 
		 * 1. AbstractTraversalUtilVisitorCallback includes XmlUtils.unwrap (can work around that)
		 * 2. java.util.ConcurrentModificationException
		 *
		SingleTraversalUtilVisitorCallback refVisitor 
			= new SingleTraversalUtilVisitorCallback(
					new TraversalUtilFootnoteRefVisitor());
		refVisitor.walkJAXBElements(body);
		*/
		
		ClassFinder finder = new ClassFinder(CTFtnEdnRef.class);
		try {
			new TraversalUtil(wordMLPackage.getMainDocumentPart().getContents(), finder);
		} catch (Docx4JException e) {
			e.printStackTrace();
		}  
		
		List results = finder.results;
		System.out.println("Found " + results.size());

		int i = 1;
		for (Object o : results) {
			CTFtnEdnRef result1 = (CTFtnEdnRef)o;
			//System.out.println(result1.getParent().getClass().getName());
			
			R r = (R)result1.getParent();
			
			// Its actually wrapped in JAXBEelement, so won't work
			// System.out.println(r.getContent().remove(result1));
			
			if (r.getContent().size()==1) {
				r.getContent().clear();
			} else {
				System.out.println("Cowardly keeping " + i);
			}
			i++;
			if ((i % 100) == 0) System.out.println(i);
		}
		
		if (mdp.getDocumentSettingsPart()!=null
				&& mdp.getDocumentSettingsPart().getJaxbElement().getFootnotePr()!=null) {
			// Word 2016 can't open the document if this is still present!
			mdp.getDocumentSettingsPart().getJaxbElement().setFootnotePr(null);
		}
		
		// Save it
		String filename = System.getProperty("user.dir") + "/OUT_FootnotesRemove.docx";
		wordMLPackage.save(new java.io.File(filename) );
		System.out.println("Saved " + filename);
						
	}
	
	public static class TraversalUtilFootnoteRefVisitor extends TraversalUtilVisitor<JAXBElement<org.docx4j.wml.CTFtnEdnRef>> {
		
		static int i =0;
		@Override
		public void apply(JAXBElement<org.docx4j.wml.CTFtnEdnRef> element, Object parent, List<Object> siblings) {

			siblings.remove(element);
			i++;
			System.out.println(i);
		}
	
	}
	
}
