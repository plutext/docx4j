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
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** These is an utility class with some common functions for the 
 *  HTML-exporters and the SvgExporter.
 * 
 */
public class HtmlCssHelper {

	private static Logger log = LoggerFactory.getLogger(HtmlCssHelper.class);
	
	//Temporary maps that get used in applyAttributes, they are kept here to be able to reuse it
	private static ThreadLocal<Map<String, Property>> threadLocalTempMap = new ThreadLocal<Map<String, Property>>();
		
    public static void createDefaultCss(boolean hasDefaultHeader, boolean hasDefaultFooter, StringBuilder result) {
    	//TODO: This method needs to be replaced with something similar to the LayoutMasterSetBuilder of fo
		result.append("/*paged media */ div.header {display: none }");
		result.append("div.footer {display: none } /*@media print { */");
		if (hasDefaultHeader) {
			result.append("div.header {display: block; position: running(header) }");
		}
		if (hasDefaultFooter) {
			result.append("div.footer {display: block; position: running(footer) }");
		}
		
		result.append("@page { size: A4; margin: 10%; @top-center {");
		result.append("content: element(header) } @bottom-center {");
		result.append("content: element(footer) } }");

		result.append("/*element styles*/ .del  {text-decoration:line-through;color:red;} ");
		result.append(".ins {text-decoration:none;background:#c0ffc0;padding:1px;}");
    	
    }
	
    public static void createCssForStyles(OpcPackage opcPackage, StyleTree styleTree, StringBuilder result) {

		// First iteration - table styles
		result.append("\n /* TABLE STYLES */ \n");    	
		Tree<AugmentedStyle> tableTree = styleTree.getTableStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : tableTree.toList() ) {
    		
    		if (n.getData()==null) {
    			if (n.equals(tableTree.getRootElement() )) {
    				// that's ok
    			} else {
    				log.error("Node<AugmentedStyle> unexpectedly null data" );
    			}
    			continue;
    		}
    		
    		Style s = n.getData().getStyle();

    		result.append( "table."+ s.getStyleId()  + " {display:table;" );
    		
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
        		HtmlCssHelper.createCss(opcPackage, s.getPPr(), result, false, false );
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
    		
    		if (n.getData()==null) {
    			if (n.equals(pTree.getRootElement() )) {
    				// shouldn't happen in paragraph case, but still, that's ok
    			} else {
    				log.error("Node<AugmentedStyle> unexpectedly null data" );
    			}
    			continue;
    		}
    		
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:block;" );  // not just p, also inherit on ul|ol
        	if (s.getPPr()==null) {
        		log.debug("null pPr for style " + s.getStyleId());
        	} else {
        		HtmlCssHelper.createCss(opcPackage, s.getPPr(), result, false, false );
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
    		
    		if (n.getData()==null) {
    			if (n.equals(cTree.getRootElement() )) {
    				// that's ok
    			} else {
    				log.error("Node<AugmentedStyle> unexpectedly null data" );
    			}
    			continue;
    		}
    		
    		Style s = n.getData().getStyle();

    		result.append( "span."+ s.getStyleId()  + " {display:inline;" );
        	if (s.getRPr()==null) {
        		log.warn("! null rPr for character style " + s.getStyleId());
        	} else {
            	HtmlCssHelper.createCss(opcPackage, s.getRPr(), result);
        	}
        	result.append( "}\n" );        	
    	}	
    }
    
    protected static void createCss(CTTblPrBase  tblPr, StringBuilder result) {
    	
		if (tblPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblPr);    	
    	for( Property p :  properties ) {
    		appendNonNull(result, p);
    	}    
    }
    
    protected static void createCss(List<CTTblStylePr> tblStylePrList, StringBuilder result) {
    	// STTblStyleOverrideType
    	
		if (tblStylePrList==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblStylePrList);    	
    	for( Property p :  properties ) {
    		appendNonNull(result, p);
    	}    
    }
    
    protected static void createCss(TrPr trPr, StringBuilder result) {
    	// includes jc, trHeight, wAfter, tblCellSpacing
    	
		if (trPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(trPr);    	
    	for( Property p :  properties ) {
    		appendNonNull(result, p);
    	}    
    }
    
    protected static void createCss(TcPr tcPr, StringBuilder result) {
    	// includes TcPrInner.TcBorders, CTShd, TcMar, CTVerticalJc
    	
		if (tcPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tcPr);    	
    	for( Property p :  properties ) {
    		appendNonNull(result, p);
    	}    
    }
    
    public static void createCss(OpcPackage opcPackage, PPr pPr, StringBuilder result, boolean ignoreBorders, boolean isListItem) {
    	
    	if (isListItem) {
    		result.append("display: list-item;");
    	}
    	
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

	    	if (isListItem
	    			&& p instanceof Indent) {
	    		// Avoid indent settings which would overwrite the bullet 
	    		// eg "position: relative; margin-left: 0.5in;text-indent: -0.25in;
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
    		
    		appendNonNull(result, p);
    	}    
    }
    
    public static void createCss(OpcPackage opcPackage, RPr rPr, StringBuilder result) {

    	List<Property> properties = PropertyFactory.createProperties(opcPackage, rPr);
    	
    	for( Property p :  properties ) {
    		appendNonNull(result, p);
    	}
    }

    private static void appendNonNull(StringBuilder result, Property p) {
		String prop = p.getCssProperty();
		if (prop!=null) {
			result.append(prop);
		}
    }
    
	public static void applyAttributes(List<Property> properties, Element node) {
	Map<String, Property> tempAttributeMap = null;
	StringBuilder buffer = null;
		if ((properties != null) && (!properties.isEmpty())) {
			tempAttributeMap = getTempMap();
			if ((properties != null) && (!properties.isEmpty())) {
				buffer = new StringBuilder();
				for (int i=0; i<properties.size(); i++) {
					tempAttributeMap.put(properties.get(i).getCssName(), properties.get(i));
				}
				for (Property property : tempAttributeMap.values()) {
					buffer.append(property.getCssProperty());
				}
				tempAttributeMap.clear();
				appendStyle(node, buffer.toString());
			}
		}
	}

	public static void appendStyle(Element node, String newValue) {
	String style = node.getAttribute("style");
		if ((style != null) && (style.length() > 0)) {
			node.setAttribute("style", style + newValue);
		}
		else {
			node.setAttribute("style", newValue);
		}
	}
	
	protected static Map<String, Property> getTempMap() {
	Map<String, Property> ret = threadLocalTempMap.get();
		if (ret == null) {
			ret = new TreeMap<String, Property>();
			threadLocalTempMap.set(ret);
		}
		return ret;
	}

}
