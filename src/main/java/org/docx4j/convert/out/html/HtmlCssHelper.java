/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.html;

import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;

/** These is an utility class with some common functions for the 
 *  HTML-exporters and the SvgExporter.
 * 
 */
public class HtmlCssHelper {

	private static Logger log = Logger.getLogger(HtmlCssHelper.class);
	
    public static void createCssForStyles(OpcPackage opcPackage, StyleTree styleTree, StringBuffer result) {

		// First iteration - table styles
		result.append("\n /* TABLE STYLES */ \n");    	
		Tree<AugmentedStyle> tableTree = styleTree.getTableStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : tableTree.toList() ) {
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:table;" );
    		
    		// TblPr
    		if (s.getTblPr()==null) {
    		} else {
    			log.debug("Applying tblPr..");
            	createCss(s.getTblPr(), result);
            	
    		}
    		
    		// TblStylePr - STTblStyleOverrideType stuff
    		if (s.getTblStylePr()==null) {
    		} else {
    			log.debug("Applying tblStylePr.. TODO!");
    			// Its a list, created automatically
            	createCss(s.getTblStylePr(), result);
    		}
    		
    		
    		// TrPr - eg jc, trHeight, wAfter, tblCellSpacing
    		if (s.getTrPr()==null) {
    		} else {
    			log.debug("Applying trPr.. TODO!");
            	createCss( s.getTrPr(), result);
    		}
    		
    		// TcPr - includes includes TcPrInner.TcBorders, CTShd, TcMar, CTVerticalJc
    		if (s.getTcPr()==null) {
    		} else {
    			log.debug("Applying tcPr.. ");
            	createCss( s.getTcPr(), result);
    		}
    		    		
        	if (s.getPPr()==null) {
        		log.debug("null pPr for style " + s.getStyleId());
        	} else {
        		HtmlCssHelper.createCss(opcPackage, s.getPPr(), result, false );
        	}
        	if (s.getRPr()==null) {
        		log.debug("null rPr for style " + s.getStyleId());
        	} else {
            	HtmlCssHelper.createCss(opcPackage, s.getRPr(), result);
        	}
        	result.append( "}\n" );         	
    	}
		
		// Second iteration - paragraph level pPr *and rPr*
		result.append("\n /* PARAGRAPH STYLES */ \n");    	
		Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : pTree.toList() ) {
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:block;" );
        	if (s.getPPr()==null) {
        		log.debug("null pPr for style " + s.getStyleId());
        	} else {
        		HtmlCssHelper.createCss(opcPackage, s.getPPr(), result, false );
        	}
        	if (s.getRPr()==null) {
        		log.debug("null rPr for style " + s.getStyleId());
        	} else {
            	HtmlCssHelper.createCss(opcPackage, s.getRPr(), result);
        	}
        	result.append( "}\n" );        	
    	}
		    	
	    // Third iteration, character styles
		result.append("\n /* CHARACTER STYLES */ ");
		//result.append("\n /* These come last, so they have more weight than the paragraph _rPr component styles */ \n ");
		
		Tree<AugmentedStyle> cTree = styleTree.getCharacterStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : cTree.toList() ) {
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:inline;" );
        	if (s.getRPr()==null) {
        		log.warn("! null rPr for character style " + s.getStyleId());
        	} else {
            	HtmlCssHelper.createCss(opcPackage, s.getRPr(), result);
        	}
        	result.append( "}\n" );        	
    	}	
    }
    
    protected static void createCss(CTTblPrBase  tblPr, StringBuffer result) {
    	
		if (tblPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    
    protected static void createCss(List<CTTblStylePr> tblStylePrList, StringBuffer result) {
    	// STTblStyleOverrideType
    	
		if (tblStylePrList==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblStylePrList);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    
    protected static void createCss(TrPr trPr, StringBuffer result) {
    	// includes jc, trHeight, wAfter, tblCellSpacing
    	
		if (trPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(trPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    
    protected static void createCss(TcPr tcPr, StringBuffer result) {
    	// includes TcPrInner.TcBorders, CTShd, TcMar, CTVerticalJc
    	
		if (tcPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tcPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    
    public static void createCss(OpcPackage opcPackage, PPr pPr, StringBuffer result, boolean ignoreBorders) {
    	
		if (pPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(opcPackage, pPr);    	
    	for( Property p :  properties ) {
    		
			if (ignoreBorders &&
					((p instanceof PBorderTop)
							|| (p instanceof PBorderBottom))) {
				continue;
			}
			
			if (p instanceof PShading) {
    	    	// To close the gap between divs, we need to avoid
    	    	// CSS margin collapse.    	    	
    	    	// To do that, we add a border the same color as 
    	    	// the background color				
				String fill = ((CTShd)p.getObject()).getFill();				
				result.append("border-color: #" + fill + "; border-style:solid; border-width:1px;");
			}
    		
    		result.append(p.getCssProperty());
    	}    
    }
    
    
    public static void createCss(OpcPackage opcPackage, RPr rPr, StringBuffer result) {

    	List<Property> properties = PropertyFactory.createProperties(opcPackage, rPr);
    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}
    }

}
