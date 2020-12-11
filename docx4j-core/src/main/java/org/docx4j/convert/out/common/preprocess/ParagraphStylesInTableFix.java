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

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTCompatSetting;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



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
	
	private static final CTCompatSetting defaultSetting; // see comments on overrideTableStyleFontSizeAndJustification at line 350 below
	
	static {
		
		defaultSetting=Context.getWmlObjectFactory().createCTCompatSetting();
		defaultSetting.setVal("0"); // default is false
	}

	public static void process(WordprocessingMLPackage wmlPackage) {

		/* Are we invoked from FOPAreaTreeHelper?
		 * 
		 * 	at org.docx4j.convert.out.fo.FOPAreaTreeHelper.getAreaTreeViaFOP(FOPAreaTreeHelper.java:187)
			at org.docx4j.convert.out.fo.LayoutMasterSetBuilder.fixExtents(LayoutMasterSetBuilder.java:136)
			at org.docx4j.convert.out.fo.LayoutMasterSetBuilder.getLayoutMasterSetFragment(LayoutMasterSetBuilder.java:97)
			at org.docx4j.convert.out.fo.XsltFOFunctions.getLayoutMasterSetFragment(XsltFOFunctions.java:82)

		 */
		Throwable t = new Throwable();
		StackTraceElement[] trace = t.getStackTrace();
//		boolean inFOPAreaTreeHelper=false;
		for (int i=0; i < trace.length; i++) {
			if (trace[i].getClassName().contains("FOPAreaTreeHelper")) {
				return;  // don't do this, especially changing overrideTableStyleFontSizeAndJustification!
			}
		}
		
		StyleRenamer styleRenamer = new StyleRenamer();

		try { // see comments on overrideTableStyleFontSizeAndJustification at line 350 below
			DocumentSettingsPart dsp = wmlPackage.getMainDocumentPart().getDocumentSettingsPart();
			if (dsp==null) {
				dsp = new DocumentSettingsPart();
				wmlPackage.getMainDocumentPart().addTargetPart(dsp);
				
				dsp.setContents( Context.getWmlObjectFactory().createCTSettings() );
				
				// no need to set styleRenamer.overrideTableStyleFontSizeAndJustification,
				// since the default is what we want in this case
				
			} else {
				styleRenamer.overrideTableStyleFontSizeAndJustification 
					= dsp.getWordCompatSetting("overrideTableStyleFontSizeAndJustification");
				if (styleRenamer.overrideTableStyleFontSizeAndJustification==null) {
					styleRenamer.overrideTableStyleFontSizeAndJustification=defaultSetting;
					// TODO,consider making the function return the default value?
				}
			}

			// For our output docx, we always want:-
			dsp.setWordCompatSetting("overrideTableStyleFontSizeAndJustification", "1");
			// since the p styles we make/use take the table style into account 
			
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
		}
		
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
        
        styleRenamer.propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
        // do that first, since it creates virtual styles for DocDefaults,
        // which we need to include in the below
        styleRenamer.setStyles(styles);
        
		try {
			new TraversalUtil(wmlPackage.getMainDocumentPart().getContents(), styleRenamer);
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}
		
		// TODO, headers/footers as well
		
//		System.out.println(wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getXML());
		

	}	
	
	public static class StyleRenamer extends CallbackImpl {
		
		protected static Logger log = LoggerFactory.getLogger(StyleRenamer.class);
		
		CTCompatSetting overrideTableStyleFontSizeAndJustification=defaultSetting; // see comments on overrideTableStyleFontSizeAndJustification at line 350 below
		
		private PropertyResolver propertyResolver;
		
		
		/* We need to know the default styles so that we can handle
		 * implicit usage. 
		 */   
		
	    private String defaultParagraphStyle;
		public void setDefaultParagraphStyle(String defaultParagraphStyle) {
			
			
			log.debug(defaultParagraphStyle);
			
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
		
//	    private Styles newStyles=null;
	    private Map<String,Style> allStyles=null;
	    public void setStyles(Styles newStyles) {
	    	
//	    	this.newStyles = newStyles;
	    	allStyles = new HashMap<String,Style>(); 
//	    	cellPStyles = new HashSet<String>(); 
	    	
	    	for (Style s : newStyles.getStyle()) {
//	    		System.out.println(s.getStyleId());
	    		allStyles.put(s.getStyleId(), s);
	    	}
	    }
	    private Set<String> cellPStyles=new HashSet<String>(); 
	    
	    
	    private boolean isFalse(CTCompatSetting overrideTableStyleFontSizeAndJustification) {
	    	
	    	return  ( overrideTableStyleFontSizeAndJustification.getVal().equals("0")
					|| overrideTableStyleFontSizeAndJustification.getVal().toLowerCase().equals("false")
					|| overrideTableStyleFontSizeAndJustification.getVal().toLowerCase().equals("no")
					);
	    }
	    
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
		private String getCellPStyle(String styleVal, boolean pStyleIsDefault) {
			
			// Font size and jc for the style (which could be the default style), 
			// without following its based on values
			Style expressStyle = allStyles.get(styleVal);
			Jc expressStyleJc = expressStyle.getPPr()==null ? null : expressStyle.getPPr().getJc();
			HpsMeasure expressStyleFontSize = null;
			if (expressStyle.getRPr()!=null) {
				expressStyleFontSize=expressStyle.getRPr().getSz();
			}
			// Font size and jc for the style following its based on values
			PPr effectivePPr = propertyResolver.getEffectivePPr(styleVal);
			Jc effectiveJc = effectivePPr.getJc();
			
			RPr effectiveRPr = propertyResolver.getEffectiveRPr(styleVal);
			HpsMeasure effectiveFontSize = null;
			if (effectiveRPr!=null) {
				effectiveFontSize=effectiveRPr.getSz();
			}
			
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
                        if(log.isErrorEnabled()) {
                            log.error("Default table style has no ID!");
                            log.error(XmlUtils.marshaltoString(tableStyle));
                        }
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
				if (log.isDebugEnabled()) {
					log.debug("adding to hierarchy: " + currentStyle);
				}
				if (thisStyle.getBasedOn()!=null) {
					currentStyle = thisStyle.getBasedOn().getVal();
				} else {
					currentStyle = null;
				}
			} while (currentStyle != null);
			
	
			
			Style newStyle = Context.getWmlObjectFactory().createStyle();
			newStyle.setType("paragraph");
			
			Style styleToApply;
			
			// First, docDefaults
			StyleUtil.apply(
					this.propertyResolver.getDocumentDefaultPPr(),
					newStyle);
			StyleUtil.apply(
					this.propertyResolver.getDocumentDefaultRPr(),
					newStyle);
			// or we could have just cloned those thing, and used the clones
			
			
			// Next, table style - first/temporarily in tableStyleContrib
			Style tableStyleContrib = null;
			List<Style> tblStyles = new ArrayList<Style>();
			if (tableStyle!=null) {
				currentStyle = tableStyle;
	    		do {
	    			log.debug(currentStyle);			    			
	    			Style thisStyle = allStyles.get(currentStyle);
	    			
	    			if (thisStyle==null) {
	    				log.info("Missing " + currentStyle);
	    				currentStyle = null;
	    			} else {
	    			
		    			if ( thisStyle.getName() !=null  // Google Docs Nov 2014 creates table styles without a w:name element 
		    					&& "Normal Table".equals(thisStyle.getName().getVal())) {
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
	    			
	    			}
	    		} while (currentStyle != null);

	    		for (int i = tblStyles.size()-1; i>=0; i--) {
	    			styleToApply = tblStyles.get(i);
                    if(log.isDebugEnabled()) {
                        log.debug("Applying " + styleToApply.getStyleId() + "\n" + XmlUtils.marshaltoString(styleToApply, true, true));

                    }
	    			
	    			tableStyleContrib = StyleUtil.apply(styleToApply, tableStyleContrib);
                    if(log.isDebugEnabled()) {
                        log.debug(XmlUtils.marshaltoString(tableStyleContrib, true, true));
                    }
	    		}
			}
			
			if (tableStyleContrib==null) {
				// will happen if the style was Normal Table, since we break above..
				// .. so just make an empty object, to avoid having to do isNull tests below..
				tableStyleContrib = Context.getWmlObjectFactory().createStyle();
			}

			// What do the table styles contribute?
			Jc tableStyleJc = tableStyleContrib.getPPr()==null ? null : tableStyleContrib.getPPr().getJc();
			
			HpsMeasure tableStyleFontSize = null;
			if (tableStyleContrib.getRPr()!=null) {
				tableStyleFontSize=tableStyleContrib.getRPr().getSz();
			}
			
			// Now we can apply to table style contrib on top of docDefaults
			// As these are different style types:-
			newStyle.setPPr(StyleUtil.apply(tableStyleContrib.getPPr(), newStyle.getPPr()));
			newStyle.setRPr(StyleUtil.apply(tableStyleContrib.getRPr(), newStyle.getRPr()));
            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(newStyle, true, true));
                log.debug("hierarchy.size(): " + hierarchy.size());
            }
			
			
			// Finally, rest of list in reverse
            
			for (int i = hierarchy.size()-1; i>=0; i--) 
				// NB 2016 04 09: for 3.3.0, this changed to -1,
				// since 
			{
				styleToApply = hierarchy.get(i);
                if(log.isDebugEnabled()) {
                    log.debug("Applying " + styleToApply.getStyleId() +
                            "\n" + XmlUtils.marshaltoString(styleToApply, true, true));
                }
				StyleUtil.apply(styleToApply, newStyle);
                if(log.isDebugEnabled()) {
                    log.debug("Result: " +
                            "\n" + XmlUtils.marshaltoString(newStyle, true, true));
                }
			}
			
		    /*
		     * w:compatSetting[w:name="overrideTableStyleFontSizeAndJustification"]
		     * is defined in [MS-DOCX] 
		     * 
		     * If this value is true, then the style hierarchy of the document is evaluated as specified 
		     * in [ISO/IEC29500-1:2011] section 17.7.2.
		     */
			if (isFalse(overrideTableStyleFontSizeAndJustification)) {
				
				log.info("giving TableStyleFontSizeAndJustification primacy, as per this docx w:compatSetting");
				
			     /* 
					If this value is false, which is the default, then the following additional rules apply:
					
					If the default paragraph style (as specified in [ISO/IEC29500-1:2011] section 17.7.4.17) 
					specifies a font size of 11pt or 12pt, then that setting will not override the font size 
					specified by the table style for paragraphs in tables.
					
						// That's wrong; this additional rule only applies if the font size is 12pt (not 11pt!).
						// Tested in Word 2010 
					
					If the default paragraph style (as specified in [ISO/IEC29500-1:2011] section 17.7.4.17) 
					specifies a justification of left, then that setting will not override the justification 
					specified by the table style for paragraphs in tables.
					  
				 * The philosophy seems to be that inside a table cell, Normal didn't apply.
				 * 
				 * NB: there are fairly comprehensive test cases in src/test/java
				 * for the behaviour with font size (where in each case the expected
				 * result is set on the basis of what Word does). There aren't any at the moment for
				 * justification (which is assumed to follow the same logic we have here).
				 * 
				 * Where Normal is basedOn our DocDefaults style, Word *does* override the table style!
				 * We'll ignore that for now, because the next version of docx4j (v3.3) 
				 * stops creating a DocDefaults style. 
			     */
				
				// Font size
				if (log.isDebugEnabled()) {
					log.debug("styleVal: " + styleVal);
					log.debug("defaultParagraphStyle: " + defaultParagraphStyle);
				}
				if ((!styleVal.equals(defaultParagraphStyle)) 
						&& expressStyleFontSize!=null) {
					// Not normal (but assume based on it)

					// if style is not default, use it 
					// (whether its basedOn normal or not)
					// provided it contributes fontSize (its not enough for it to be inherited from Normal,
					// but TODO if A is basedOn B basedOn Normal, something contributed by B should be used) 
					// .. so nothing to do
					
					
				} else if (tableStyleFontSize!=null) {
					
					// the exception
					if (effectiveFontSize!=null
							&& effectiveFontSize.getVal().intValue()==24 )
					newStyle.getRPr().setSz(tableStyleFontSize); //use this!
					// What about SzCs?
					
				} else {
					
					// the table style doesn't set it
					// .. so nothing to do
					
					// OR if no Sz setting in the style,
					// we could set explicitly
				}
					

				// Justification
				if ((!styleVal.equals(defaultParagraphStyle)) 
						&& expressStyleJc!=null
						) {
					// if style is not default, use it (whether its basedOn normal or not)
					// .. so nothing to do
					
				} else if (tableStyleJc!=null) {
					
					// the exception
					if (effectiveJc!=null
							&& effectiveJc.getVal().equals(JcEnumeration.LEFT))
						newStyle.getPPr().setJc(tableStyleJc); //use this!
					
				} else {
					
					// the table style doesn't set it
					// .. so nothing to do
				}
				
			} else {
				log.debug("allowing default paragraph style to overrideTableStyleFontSizeAndJustification, as per this docx w:compatSetting");
				
			}
			
			Style.Name name = Context.getWmlObjectFactory().createStyleName();
			name.setVal(resultStyleID);
			newStyle.setName(name);
			
			newStyle.setStyleId(resultStyleID);
//			newStyles.getStyle().add(newStyle);
			cellPStyles.add(resultStyleID); 
			
			// required for PDF (but not XHTML) output
			propertyResolver.activateStyle(newStyle);

            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(newStyle, true, true));
            }
			
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
							// We're in a table
							String resultStyle = getCellPStyle(newStyle, true);
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
							String newStyle = getCellPStyle(styleVal, 
													styleVal.equals(defaultParagraphStyle));
							if (newStyle==null) {
								log.debug("getCellPStyle returned null, so leave as is");
							} else {
								p.getPPr().getPStyle().setVal(newStyle);
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
