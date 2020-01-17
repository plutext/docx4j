/*
 *  Copyright 2013, Plutext Pty Ltd.
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
import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.SectPrFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.SectPr;


public class HeaderFooterRemove  {

	public static void main(String[] args) throws Exception {
		
		// A docx or a dir containing docx files
		String inputpath = System.getProperty("user.dir") + "/testHF.docx";

		StringBuilder sb = new StringBuilder(); 
		
		File dir = new File(inputpath);
		
		if (dir.isDirectory()) {
			
			String[] files = dir.list();
			
			for (int i = 0; i<files.length; i++  ) {
				
				if (files[i].endsWith("docx")) {
					sb.append("\n\n" + files[i] + "\n");
					removeHFFromFile(new java.io.File(inputpath + "/" + files[i]));		
				}
			}
			
		} else if (inputpath.endsWith("docx")) {
			sb.append("\n\n" + inputpath + "\n");
			removeHFFromFile(new java.io.File(inputpath ));		
		}
		
		System.out.println(sb.toString());
		
	}

	public static void removeHFFromFile(File f) throws Exception {


		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(f);
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		// Remove from sectPr
		SectPrFinder finder = new SectPrFinder(mdp);
	    new TraversalUtil(mdp.getContent(), finder);
	    for (SectPr sectPr : finder.getSectPrList()) {
	    	sectPr.getEGHdrFtrReferences().clear();
	    }
	    
	    // Remove rels
	    List<Relationship> hfRels = new ArrayList<Relationship>(); 
	    for (Relationship rel : mdp.getRelationshipsPart().getRelationships().getRelationship() ) {
	    	
	    	if (rel.getType().equals(Namespaces.HEADER)
	    			|| rel.getType().equals(Namespaces.FOOTER)) {
	    		hfRels.add(rel);
	    	}
	    }
	    for (Relationship rel : hfRels ) {
	    	mdp.getRelationshipsPart().removeRelationship(rel);
	    }

		wordMLPackage.save(f);
	    
		
	}

}
