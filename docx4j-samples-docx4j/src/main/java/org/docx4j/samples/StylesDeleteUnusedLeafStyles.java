/*
 *  Copyright 2019, Plutext Pty Ltd.
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import org.docx4j.XmlUtils;
import org.docx4j.model.styles.Node;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;


/**
 * Microsoft Word places a hard limit on the number of styles it allows.
 * 
 * This demo code deletes unused "leaf" styles, that is, styles which
 * others aren't dependent on.
 *
 */
public class StylesDeleteUnusedLeafStyles {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir")
					+ "/one.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		StyleDefinitionsPart stylesPart = wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart();

		System.out.println("BEFORE");		
		report( stylesPart.getContents().getStyle() );		
		
		System.out.println("styles which are in use:");
		Set<String> stylesInUse = wordMLPackage.getMainDocumentPart().getStylesInUse();
		for (String s : stylesInUse) {
			System.out.println(s);
		}
		
		// Our style trees usually only cover styles which are in use
		// To trim unused styles, we're interested in the ones which are NOT is use
		
		// Setup
		Styles styles = wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart(false).getJaxbElement();		
    	Set<String> stylesNotInUse = new HashSet<String>();
		for ( org.docx4j.wml.Style s : styles.getStyle() ) {	
			if (stylesInUse.contains(s.getStyleId())) {
//				System.out.println("ignoring " + s.getStyleId() );
			} else {
				stylesNotInUse.add(s.getStyleId());
			}
		}
		Map<String, Style> allStyles = new HashMap<String, Style>();
		for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
			allStyles.put(s.getStyleId(), s);	
		}
		
		// For docx4j before 8.1.3|11.1.3 
//		StyleTree st = new StyleTree(stylesNotInUse, allStyles,
//				styles.getDocDefaults(), allStyles.get("Normal"));
    	
		// Need docx4j 8.1.3|11.1.3 for this
		StyleTree st = new StyleTree(stylesNotInUse, allStyles,
				styles.getDocDefaults(), allStyles.get("Normal"), 
				stylesPart.getDefaultCharacterStyle(), stylesPart.getDefaultTableStyle());
				
		System.out.println("PARAGRAPH STYLES");
		walk(st.getParagraphStylesTree().getRootElement(),"");

		// You can't use this code to delete character styles unless you have docx4j 8.1.3|11.1.3
		System.out.println("\nCHARACTER STYLES");
		walk(st.getCharacterStylesTree().getRootElement(),"");
		
		// now delete the deletableStyles
		Styles stylesClone = XmlUtils.deepCopy(styles);
		
		styles.getStyle().clear();
		for ( org.docx4j.wml.Style s : stylesClone.getStyle() ) {
			
			if (deletableStyles.contains(s.getStyleId())) {
				// we can drop this one
			} else {
				styles.getStyle().add(s);
			}
		}
		
		System.out.println("AFTER");
		
		report( styles.getStyle() );
		
		// Save the document now if you want
		
	}

	private static void report( List<Style> styles) {

		int pCount = 0;
		int cCount = 0;
		int nCount = 0;
		int tCount = 0;
		
		
		for (Style s :  styles) {
			
			if (s.getType().equals("paragraph")) pCount++;
			if (s.getType().equals("character")) cCount++;
			if (s.getType().equals("numbering")) nCount++;
			if (s.getType().equals("table")) tCount++;

		}
		
		System.out.println("p: " + pCount);
		System.out.println("c: " + cCount);
		System.out.println("n: " + nCount);
		System.out.println("t: " + tCount);
		
		System.out.println(pCount+cCount+nCount+tCount);		
	}
	
	static Set<String> deletableStyles = new HashSet<String>();
	
    private static void walk(Node<AugmentedStyle> element, String tabs) {
    	if (element==null) {return;}
    	Style s = element.getData().getStyle();
    	
    	List<Node<AugmentedStyle>> children = element.getChildren();
    	
    	if (children.isEmpty()) {
    		// No styles are dependent on this one, so it could be deleted
    		deletableStyles.add(s.getStyleId());
        	System.out.println(tabs+s.getStyleId() + " - deleting " );
    	} else {
        	System.out.println(tabs+s.getStyleId() );
    	
	        for (Node<AugmentedStyle> data : element.getChildren()) {
	            walk(data, tabs + "  ");
	        }
    	}
    }
	

}
