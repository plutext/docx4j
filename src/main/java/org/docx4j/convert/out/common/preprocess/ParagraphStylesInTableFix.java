/*
   (c) Plutext Pty Ltd, 2014
   
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
package org.docx4j.convert.out.common.preprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.PPrBase.PStyle;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A typical case this would fix is where the spacing between paragraphs 
 * is wrong in the PDF|XHTML output (because that is set in DocDefaults).
 * 
 * In a cell, a paragraph uses the table's paragraph properties,
 * plus the relevant paragraph style (Normal, by default).
 * The relevant paragraph style trumps the values from the
 * table's paragraph properties, so that would mean giving
 * the doc defaults (which we've made part of our styles) priority
 * over the table's paragraph properties, which is wrong.
 * TO avoid this, this preprocessor creates a new style, which encapsulates the
 * paragraph style, with DocDefaults given lower priority 
 * than table style.  This created style has no w:basedOn setting.
 * This preprocessor is required if paragraphs in tables are being styled incorrectly.
 * @since 3.0.2
 */
public class ParagraphStylesInTableFix {
	
	protected static Logger log = LoggerFactory.getLogger(ParagraphStylesInTableFix.class);	
	
	
	/** Move bookmarks into a paragraph
	 * 
	 */
	public static void process(WordprocessingMLPackage wmlPackage) {
		
		StyleRenamer styleRenamer = new StyleRenamer();
		
		try {
			styleRenamer.setDefaultParagraphStyle(wmlPackage.getMainDocumentPart()
					.getStyleDefinitionsPart().getDefaultParagraphStyle().getStyleId());
		} catch (NullPointerException npe) {
			log.warn("No default paragraph style!!");
		}
		
		Style defaultTableStyle = wmlPackage.getMainDocumentPart()
				.getStyleDefinitionsPart().getDefaultTableStyle();
		if (defaultTableStyle != null) {
			styleRenamer.setDefaultTableStyle(defaultTableStyle);
		}
		
		Styles styles = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
        styleRenamer.setNewStyles(styles);
        
        styleRenamer.propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
        
		try {
			new TraversalUtil(wmlPackage.getMainDocumentPart().getContents(), styleRenamer);
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}
		
		// TODO, headers/footers as well
		

	}	
	
	public static class StyleRenamer extends CallbackImpl {
		
		protected static Logger log = LoggerFactory.getLogger(StyleRenamer.class);
		
		private PropertyResolver propertyResolver;
		
		
		/* We need to know the default styles so that we can handle
		 * implicit usage. 
		 */   
		
	    private String defaultParagraphStyle;
		public void setDefaultParagraphStyle(String defaultParagraphStyle) {
			this.defaultParagraphStyle = defaultParagraphStyle;
		}


		private Style defaultTableStyle;    // Need the actual Style here (see below) 
		public void setDefaultTableStyle(Style defaultTableStyle) {
			this.defaultTableStyle = defaultTableStyle;
		}
		
	    
		/*
		 * Style with ID DefaultParagraphFont is never used implicitly.
		 * A style is IGNORED if its name is "Default Paragraph Font",
		 * whether inside table or not.
		 */
//	    private String docDefaultsCharacterStyle="DocDefaultsChar";
	    
		
	    private LinkedList<Tbl> tblStack = new LinkedList<Tbl>();
	    // We don’t have to treat nested tables in any special way, 
	    // since a nested table does not inherit any of the properties of its parent table.
		
	    private Styles newStyles=null;
	    private Map<String,Style> allStyles=null;
	    public void setNewStyles(Styles newStyles) {
	    	
	    	this.newStyles = newStyles;
	    	allStyles = new HashMap<String,Style>(); 
	    	cellPStyles = new HashSet<String>(); 
	    	
	    	for (Style s : newStyles.getStyle()) {
	    		allStyles.put(s.getStyleId(), s);
	    	}
	    }
	    private Set<String> cellPStyles=null;
	    
	    
	    /*
	     * w:compatSetting[w:name="overrideTableStyleFontSizeAndJustification"]
	     * is defined in [MS-DOCX] 
	     * 
	     * If this value is true, then the style hierarchy of the document is evaluated as specified 
	     * in [ISO/IEC29500-1:2011] section 17.7.2.
	     * 
			If this value is false, which is the default, then the following additional rules apply:
			
			If the default paragraph style (as specified in [ISO/IEC29500-1:2011] section 17.7.4.17) 
			specifies a font size of 11pt or 12pt, then that setting will not override the font size 
			specified by the table style for paragraphs in tables.
			
			If the default paragraph style (as specified in [ISO/IEC29500-1:2011] section 17.7.4.17) 
			specifies a justification of left, then that setting will not override the justification 
			specified by the table style for paragraphs in tables.
			
			TODO:
			- output docx should use value true.
			- our explicit p style in cell should take into account what the setting is on the
			  relevant input docx 
	     */
	    
		/**
		 * In a cell, a paragraph uses the table's paragraph properties,
		 * plus the relevant paragraph style (Normal, by default).
		 * The relevant paragraph style trumps the values from the
		 * table's paragraph properties, so that would mean giving
		 * the doc defaults (which we've made part of our styles) priority
		 * over the table's paragraph properties, which is wrong.
		 * TO avoid this, we create a new style, which encapsulates the
		 * paragraph style, with DocDefaults given lower priority 
		 * than table style.  This created style has no w:basedOn setting.
		 */
		private String getCellPStyle(String styleVal) {

			
			String tableStyle=null;
			TblPr tblPr = tblStack.peek().getTblPr(); 
			if (tblPr!=null && tblPr.getTblStyle()!=null) {
				tableStyle = tblPr.getTblStyle().getVal();
			} else if (defaultTableStyle==null) {
				log.warn("No default table style defined in docx Style Definitions part"); 
				return null;						
			} else {
				if (defaultTableStyle.getName()!=null
						&& defaultTableStyle.getName().getVal()!=null
						&& defaultTableStyle.getName().getVal().equals("Normal Table")) {
					// Word 2010 x64 ignores any table style with that name!
					log.debug("Ignoring style with name 'Normal Table' (mimicking Word)"); 
					return null;
				} else {
					// We have a default table style
					tableStyle = defaultTableStyle.getStyleId();
					// shouldn't happen, but just in case..
					if (tableStyle==null) {
						log.error("Default table style has no ID!"); 
						log.error(XmlUtils.marshaltoString(tableStyle));
						return null;						
					}
				}
			}
			String resultStyleID = styleVal+"-"+tableStyle;
			if (tableStyle.endsWith("-BR")) {
				// don't want to add this twice
			} else {
				resultStyleID = resultStyleID +"-BR";
			}
					
			if (cellPStyles.contains(resultStyleID)) return resultStyleID;
			
			List<Style> hierarchy = new ArrayList<Style>();
			
			Style basedOn = null;
			String currentStyle = styleVal;
			do {
				Style thisStyle = allStyles.get(currentStyle);
				hierarchy.add(thisStyle);
				if (thisStyle.getBasedOn()!=null) {
					currentStyle = thisStyle.getBasedOn().getVal();
				} else {
					currentStyle = null;
				}
			} while (currentStyle != null);
			
			Style newStyle = Context.getWmlObjectFactory().createStyle();
			newStyle.setType("paragraph");
			
			// First, docDefaults
			Style styleToApply = hierarchy.get(hierarchy.size()-1); // DocDefault
			log.debug("DocDefault");
			log.debug(XmlUtils.marshaltoString(styleToApply, true, true));
			StyleUtil.apply(styleToApply, newStyle);
			log.debug("Result");
			log.debug(XmlUtils.marshaltoString(newStyle, true, true));
			
			// Next, table style
			List<Style> tblStyles = new ArrayList<Style>();
			if (tableStyle!=null) {
				currentStyle = tableStyle;
	    		do {
	    			log.debug(currentStyle);			    			
	    			Style thisStyle = allStyles.get(currentStyle);
	    			
	    			if (thisStyle.getName().getVal().equals("Normal Table")) {
	    				// Very surprising, but testing using Word 2010 SP1,
	    				// it turns out that table style with name "Normal Table" 
	    				// is IGNORED (whatever its ID, and whether default or not)!! 
	    				// Change the name to something
	    				// else, and it is given effect! GO figure..
	    				//TBD how localisation affects this.
	    				// In theory, this style could be based on
	    				// another.  Haven't tested to see whether that is
	    				// honoured or not. Assume not.
	    				break;
	    			}
	    			
	    			tblStyles.add(thisStyle);
	    			
	    			if (thisStyle.getBasedOn()!=null) {
	    				currentStyle = thisStyle.getBasedOn().getVal();
	    			} else {
	    				currentStyle = null;
	    			}
	    		} while (currentStyle != null);

	    		for (int i = tblStyles.size()-1; i>=0; i--) {
	    			styleToApply = tblStyles.get(i);
	    			log.debug("Applying " + styleToApply.getStyleId() + "\n" + XmlUtils.marshaltoString(styleToApply, true, true));
	    			
	    			StyleUtil.apply(styleToApply, newStyle);
	    			log.debug(XmlUtils.marshaltoString(newStyle, true, true));
	    		}
			}
			
			
			// Finally, rest of list in reverse
			for (int i = hierarchy.size()-2; i>=0; i--) {
				styleToApply = hierarchy.get(i);
				StyleUtil.apply(styleToApply, newStyle);
			}
			
			Style.Name name = Context.getWmlObjectFactory().createStyleName();
			name.setVal(resultStyleID);
			newStyle.setName(name);
			
			newStyle.setStyleId(resultStyleID);
			newStyles.getStyle().add(newStyle);
			cellPStyles.add(resultStyleID); 
			
			// required for PDF (but not XHTML) output
			propertyResolver.activateStyle(newStyle);

			log.debug(XmlUtils.marshaltoString(newStyle, true, true));
			
			return resultStyleID;
		}
	    
		
		@Override
		public List<Object> apply(Object o) {
			
			
//			if (o instanceof Tbl) {
//				
//				Tbl tbl = (Tbl)o;
//				
//				if ( tbl.getTblPr()==null ) {
//					tbl.setTblPr( Context.getWmlObjectFactory().createTblPr() );
//				}
//				
//				if (tbl.getTblPr().getTblStyle()==null) {
//					// Make table style explicit
//					tbl.getTblPr().setTblStyle(Context.getWmlObjectFactory().createCTTblPrBaseTblStyle() );					
//					tbl.getTblPr().getTblStyle().setVal(defaultTableStyle);	
//					
//				} 
//			}
			
			if (o instanceof P) {
				
				P p = (P)o;
				
				if ( p.getPPr()==null ) {
					p.setPPr( Context.getWmlObjectFactory().createPPr() );
				}
				
				if (p.getPPr().getPStyle()==null) {
										
//						String newStyle = styleMapping.get(defaultParagraphStyle); 
//						if (newStyle==null) {
//							newStyle=defaultParagraphStyle;
//						}
						String newStyle=defaultParagraphStyle;
						p.getPPr().setPStyle(Context.getWmlObjectFactory().createPPrBasePStyle() );	
						if (tblStack.size()==0) {						
							p.getPPr().getPStyle().setVal(newStyle);
						} else {
							String resultStyle = getCellPStyle(newStyle);
							if (resultStyle==null) {
								p.getPPr().getPStyle().setVal(newStyle);								
							} else {
								p.getPPr().getPStyle().setVal(resultStyle);
							}
						}
					//}
				} else {				
					PStyle pstyle = p.getPPr().getPStyle();					
					String styleVal = pstyle.getVal();
					if (styleVal!=null) {
						if (tblStack.size()>0) {						
							log.debug("Fixing " + pstyle.getVal());
							if (getCellPStyle(styleVal)==null) {
								log.debug("getCellPStyle returned null, so leave as is");
							} else {
								p.getPPr().getPStyle().setVal(getCellPStyle(styleVal));
							}
						}
					}
				}
				
			}
			
			return null;
		}
		
		@Override
		public void walkJAXBElements(Object parent) {
			
			List children = getChildren(parent);
			if (children != null) {

				for (Object o : children) {
					
					o = XmlUtils.unwrap(o);
					
					// workaround for broken getParent (since 3.0.0)
					if (o instanceof Child) {
						if (parent instanceof SdtBlock) {
							((Child)o).setParent( ((SdtBlock)parent).getSdtContent() );
						} else {
							((Child)o).setParent(parent);
						}
					}
					
					this.apply(o);
					
					if (o instanceof Tbl) {
						tblStack.push((Tbl)o);
					}

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

					if (o instanceof Tbl) {
						tblStack.pop();
					}
					
				}
			}
		}

		
	}
	
	
	
}

/*
 * 
Default table styles:

  <w:style w:type="table" w:styleId="TableGrid">
    <w:name w:val="Table Grid"/>
    <w:basedOn w:val="TableNormal"/>
    <w:uiPriority w:val="59"/>
    <w:rsid w:val="00081E3C"/>
    <w:pPr>
      <w:spacing w:after="0" w:line="240" w:lineRule="auto"/>
    </w:pPr>
    <w:tblPr>
      <w:tblInd w:w="0" w:type="dxa"/>
      <w:tblBorders>
        <w:top w:val="single" w:sz="4" w:space="0" w:color="auto"/>
        <w:left w:val="single" w:sz="4" w:space="0" w:color="auto"/>
        <w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/>
        <w:right w:val="single" w:sz="4" w:space="0" w:color="auto"/>
        <w:insideH w:val="single" w:sz="4" w:space="0" w:color="auto"/>
        <w:insideV w:val="single" w:sz="4" w:space="0" w:color="auto"/>
      </w:tblBorders>
      <w:tblCellMar>
        <w:top w:w="0" w:type="dxa"/>
        <w:left w:w="108" w:type="dxa"/>
        <w:bottom w:w="0" w:type="dxa"/>
        <w:right w:w="108" w:type="dxa"/>
      </w:tblCellMar>
    </w:tblPr>
  </w:style>

  <w:style w:type="table" w:default="1" w:styleId="TableNormal">
    <w:name w:val="Normal Table"/>
    <w:uiPriority w:val="99"/>
    <w:semiHidden/>
    <w:unhideWhenUsed/>
    <w:tblPr>
      <w:tblInd w:w="0" w:type="dxa"/>
      <w:tblCellMar>
        <w:top w:w="0" w:type="dxa"/>
        <w:left w:w="108" w:type="dxa"/>
        <w:bottom w:w="0" w:type="dxa"/>
        <w:right w:w="108" w:type="dxa"/>
      </w:tblCellMar>
    </w:tblPr>
  </w:style>
  
Word creates tables which use 

        <w:tblStyle w:val="TableGrid"/>

(Note that it includes w:pPr)

But TableNormal is magic/weird in that Word 2010 x64 seems to ignore its contents, even if used explicitly,
if its w:name is 'Normal Table':

        <w:name w:val="Normal Table"/>

!! Go figure...
        
Its @w:default and @w:styleId are not relevant to this oddity...

          <w:style w:type="table" w:default="1" w:styleId="TableNormal">
 */ 
