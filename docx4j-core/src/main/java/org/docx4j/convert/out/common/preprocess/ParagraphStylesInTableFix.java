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

import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.finders.TcFinder;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.model.table.TableModel;
import org.docx4j.model.table.TableStyleConditions;
import org.docx4j.model.table.TableStyleConditions.Look;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTCompatSetting;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.CTTrPrBase;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STTblStyleOverrideType;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
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
 *
 * <p>Since 17.1.1 the synthetic style also carries the table style's <em>conditional</em>
 * formatting ({@code w:tblStylePr}: the bold of a header row or first column, the
 * properties of a band) - one synthetic style per combination of the conditions a paragraph
 * is under, resolved by {@link TableStyleConditions} from the table's {@code w:tblLook},
 * the {@code w:cnfStyle} caches and the paragraph's position, and applied between the
 * table style's own {@code w:pPr}/{@code w:rPr} and the paragraph style, in the order
 * ECMA-376-1 &#xa7;17.7.6 fixes.  This is the only place the conditional {@code w:pPr}
 * and {@code w:rPr} are applied: {@code PropertyResolver} knows nothing of tables, so
 * there is nothing for this to double up with, and both the FO and the HTML exporters (and
 * both pathways of each) resolve the paragraph through the style this writes.</p>
 *
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

		/* w:compat/w:useWord2002TableStyleRules ("Emulate Word 2002 Table Style Rules",
		 * ECMA-376-1 17.15.1) would switch this whole step off: Word 2002 did not put a
		 * table style's w:pPr and w:rPr above docDefaults for the paragraphs of the table,
		 * which is exactly what the synthetic style built below does.  It is deliberately
		 * NOT read: measured over the three corpora, Word 365's own PDFs of the three
		 * mode-11 documents which state it apply the table style's properties anyway, and
		 * skipping this step for them cost 0.951 -> 0.105, 0.948 -> 0.248 and
		 * 0.930 -> 0.842 of Word's lines.  See word-layout-settings.md §4(e).
		 * @since 17.1.0 */

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
				CTCompatSetting original
					= dsp.getWordCompatSetting("overrideTableStyleFontSizeAndJustification");
				if (original==null) {
					styleRenamer.overrideTableStyleFontSizeAndJustification=defaultSetting;
					// TODO,consider making the function return the default value?
				} else {
					// Snapshot the incoming value.  getWordCompatSetting returns the live
					// CTCompatSetting from the settings part, and the setWordCompatSetting
					// call below mutates that same object in place (setVal), so holding the
					// reference would let our own write clobber the value the StyleRenamer
					// needs to read.
					CTCompatSetting snapshot = Context.getWmlObjectFactory().createCTCompatSetting();
					snapshot.setUri(original.getUri());
					snapshot.setName(original.getName());
					snapshot.setVal(original.getVal());
					styleRenamer.overrideTableStyleFontSizeAndJustification = snapshot;
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
        
        
		try {
	        styleRenamer.propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
	        // the resolver the walk below resolves paragraph and table styles through.
	        // (Until 17.1.1 creating it wrote into the styles part - the w:sz 20 document
	        //  default - so it had to be done before the styles were read here.)
	        styleRenamer.setStyles(styles);
			new TraversalUtil(wmlPackage.getMainDocumentPart().getContents(), styleRenamer);

			/* Headers and footers too.  A table in a header is styled by its w:tblStyle
			 * exactly as one in the body is, and letterhead tables are common: measured
			 * on a document whose header table uses TableGridLight
			 * (<w:pPr><w:spacing w:after="0" w:line="240"/></w:pPr>), Word's four
			 * baselines in the row-spanning cell are 104.2 / 116.9 / 129.4 / 142.1 - a
			 * pitch of 12.7pt, the line box alone - where ours were 147.0 / 167.7 /
			 * 188.3 / 208.9, a pitch of 20.7pt, because docDefaults' w:after="200" (8pt)
			 * survived on all 16 header paragraphs.  That is 128pt of drift, and it also
			 * made the header extent 341.3pt.  @since 17.1.0 */
			RelationshipsPart relPart = wmlPackage.getMainDocumentPart().getRelationshipsPart();
			if (relPart!=null) {
				for (Relationship rs : relPart.getRelationships().getRelationship()) {
					List<Object> content = null;
					if (Namespaces.HEADER.equals(rs.getType())) {
						content = ((HeaderPart)relPart.getPart(rs)).getJaxbElement().getContent();
					} else if (Namespaces.FOOTER.equals(rs.getType())) {
						content = ((FooterPart)relPart.getPart(rs)).getJaxbElement().getContent();
					}
					if (content!=null) {
						new TraversalUtil(content, styleRenamer);
					}
				}
			}
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
		} catch (RuntimeException e) {
			// eg a header part which isn't loadable; the body is done, don't lose that
			log.error(e.getMessage(), e);
		}

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
	    
		
	    private LinkedList<TableContext> tblStack = new LinkedList<TableContext>();
	    // We don’t have to treat nested tables in any special way, 
	    // since a nested table does not inherit any of the properties of its parent table.

	    /**
	     * What a table's paragraphs need to know about it: the table style it resolves to
	     * (the w:basedOn chain merged), the conditional formats its w:tblLook asks for, its
	     * band sizes, and where each row and cell sits, so that a paragraph's conditions
	     * can be worked out (TableStyleConditions.resolve) as the walk reaches it.
	     * @since 17.1.1
	     */
	    private class TableContext {

	    	final Tbl tbl;
	    	/** the table style id, or null where the table has none to apply */
	    	final String tableStyleId;
	    	/** the merged w:basedOn chain, or an empty style */
	    	final Style tableStyle;
	    	final Look look;
	    	final int rowBandSize;
	    	final int colBandSize;
	    	final int rowCount;
	    	final int colCount;
	    	final IdentityHashMap<Tr, Integer> rowIndex = new IdentityHashMap<Tr, Integer>();
	    	/** {first grid column, span} per cell */
	    	final IdentityHashMap<Tc, int[]> cellColumns = new IdentityHashMap<Tc, int[]>();
	    	Tr currentTr;
	    	Tc currentTc;

	    	TableContext(Tbl tbl) {
	    		this.tbl = tbl;
	    		this.tableStyleId = tableStyleId(tbl.getTblPr());
	    		this.tableStyle = tableStyleContrib(tableStyleId);

	    		// the table's own tblPr decides the look and band sizes; the style's is the fallback
	    		CTTblPrBase tblPr = tbl.getTblPr();
	    		CTTblPrBase stylePr = tableStyle.getTblPr();
	    		if (tblPr != null && tblPr.getTblLook() != null) {
	    			look = TableStyleConditions.look(tblPr);
	    		} else if (stylePr != null && stylePr.getTblLook() != null) {
	    			look = TableStyleConditions.look(stylePr);
	    		} else {
	    			look = Look.DEFAULT;
	    		}
	    		rowBandSize = (tblPr != null && tblPr.getTblStyleRowBandSize() != null)
	    				? TableStyleConditions.rowBandSize(tblPr) : TableStyleConditions.rowBandSize(stylePr);
	    		colBandSize = (tblPr != null && tblPr.getTblStyleColBandSize() != null)
	    				? TableStyleConditions.colBandSize(tblPr) : TableStyleConditions.colBandSize(stylePr);

	    		// rows and their cells, as TableModel counts them: nested tables excluded,
	    		// a cell's column its w:gridBefore plus the spans before it
	    		TableModel.TrFinder trFinder = new TableModel.TrFinder();
	    		new TraversalUtil(tbl, trFinder);
	    		List<Tr> rows = trFinder.getTrList();
	    		int cols = 0;
	    		for (int r = 0; r < rows.size(); r++) {
	    			Tr tr = rows.get(r);
	    			rowIndex.put(tr, Integer.valueOf(r));
	    			int c = gridBeforeOrAfter(tr, "gridBefore");
	    			TcFinder tcFinder = new TcFinder();
	    			new TraversalUtil(tr, tcFinder);
	    			for (Tc tc : tcFinder.tcList) {
	    				int span = 1;
	    				if (tc.getTcPr() != null && tc.getTcPr().getGridSpan() != null
	    						&& tc.getTcPr().getGridSpan().getVal() != null) {
	    					span = Math.max(1, tc.getTcPr().getGridSpan().getVal().intValue());
	    				}
	    				cellColumns.put(tc, new int[] { c, span });
	    				c += span;
	    			}
	    			c += gridBeforeOrAfter(tr, "gridAfter");
	    			if (c > cols) cols = c;
	    		}
	    		rowCount = rows.size();
	    		colCount = cols;
	    	}

	    	/** The conditions this paragraph (in the current row and cell) is under. */
	    	EnumSet<STTblStyleOverrideType> conditionsFor(P p) {
	    		Integer r = currentTr == null ? null : rowIndex.get(currentTr);
	    		int[] cc = currentTc == null ? null : cellColumns.get(currentTc);
	    		if (r == null || cc == null) {
	    			return EnumSet.noneOf(STTblStyleOverrideType.class);
	    		}
	    		return TableStyleConditions.resolve(look, rowBandSize, colBandSize,
	    				r.intValue(), rowCount, cc[0], cc[1], colCount,
	    				TableStyleConditions.rowCnf(currentTr.getTrPr()),
	    				currentTc.getTcPr() == null ? null : currentTc.getTcPr().getCnfStyle(),
	    				p.getPPr() == null ? null : p.getPPr().getCnfStyle());
	    	}
	    }

	    private static int gridBeforeOrAfter(Tr tr, String name) {
	    	if (tr.getTrPr() == null) return 0;
	    	for (jakarta.xml.bind.JAXBElement<?> el : tr.getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
	    		if (name.equals(el.getName().getLocalPart())) {
	    			Object v = el.getValue();
	    			java.math.BigInteger val = null;
	    			if (v instanceof CTTrPrBase.GridBefore) val = ((CTTrPrBase.GridBefore) v).getVal();
	    			else if (v instanceof CTTrPrBase.GridAfter) val = ((CTTrPrBase.GridAfter) v).getVal();
	    			return val == null ? 0 : Math.max(0, val.intValue());
	    		}
	    	}
	    	return 0;
	    }

	    /** The merged w:basedOn chain of each table style, by id (null id: an empty style). */
	    private Map<String, Style> tableStyleContribs = new HashMap<String, Style>();
		
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
	    
		public static boolean isCyclic(Style s, List<Style> hierarchy) throws CyclicStylesException  {
			
			if (hierarchy.contains(s)
					|| hierarchy.size()>32) { // hardcoded limit on deep basedOn hierarchies 
				
				if (log.isDebugEnabled()) {
					for (Style style : hierarchy) {
						if (style.equals(s)) {
							log.debug(s.getStyleId() + " <--- cycle starts here");
						} else {
							log.debug(style.getStyleId());
						}
			        }				
					log.debug(s.getStyleId());
				}			
				if (Docx4jProperties.getProperty("docx4j.openpackaging.exceptions.CyclicStylesException.throw", false) ) {
					throw new CyclicStylesException("Cycle detected in style basedOn hierarchy");
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	    
		/**
		 * The id of the table style a table resolves to - its own w:tblStyle, else the
		 * document's default table style - or null where there is none to apply (no
		 * default, or one named "Normal Table", which Word ignores).
		 */
		private String tableStyleId(TblPr tblPr) {
			if (tblPr!=null && tblPr.getTblStyle()!=null) {
				return tblPr.getTblStyle().getVal();
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
					String tableStyle = defaultTableStyle.getStyleId();
					// shouldn't happen, but just in case..
					if (tableStyle==null) {
                        if(log.isErrorEnabled()) {
                            log.error("Default table style has no ID!");
                            log.error(XmlUtils.marshaltoString(defaultTableStyle));
                        }
						return null;						
					}
					return tableStyle;
				}
			}
		}

		/**
		 * The table style's w:basedOn chain merged into one style (root first, so a child
		 * overrides its parent; conditional formats merge per condition, see
		 * StyleUtil.apply(List, List)), cached by id.  An empty style for a null id, a
		 * missing style, or a chain ending in "Normal Table".
		 */
		private Style tableStyleContrib(String tableStyle) {

			Style cached = tableStyleContribs.get(tableStyle);
			if (cached != null) return cached;

			Style tableStyleContrib = null;
			List<Style> tblStyles = new ArrayList<Style>();
			if (tableStyle!=null) {
				String currentStyle = tableStyle;
	    		do {
	    			log.debug(currentStyle);			    			
	    			Style thisStyle = allStyles.get(currentStyle);
	    			
	    			if (thisStyle==null) {
	    				log.info("Missing " + currentStyle);
	    				currentStyle = null;
	    			} else {
	    			
	    				try {
							if (isCyclic(thisStyle, tblStyles)) {
								log.warn("Cycle above detected in style basedOn hierarchy for: " + thisStyle.getStyleId() + " - stopping");						    					
								break;
							}
						} catch (CyclicStylesException e) {
							throw new RuntimeException(e);
						}
	    				
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
	    			Style styleToApply = tblStyles.get(i);
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
			tableStyleContribs.put(tableStyle, tableStyleContrib);
			return tableStyleContrib;
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
		 *
		 * Since 17.1.1 the table style's contribution includes the conditional formats
		 * (w:tblStylePr) the paragraph is under, so there is one such style per
		 * (paragraph style, table style, applicable conditions).
		 *
		 * @param conditions the conditional formats the paragraph is under
		 *        (TableStyleConditions), possibly empty
		 * @throws CyclicStylesException 
		 */
		private String getCellPStyle(String styleVal, boolean pStyleIsDefault,
				Set<STTblStyleOverrideType> conditions) throws CyclicStylesException {
			
			// Font size and jc for the style (which could be the default style), 
			// without following its based on values
			Style expressStyle = allStyles.get(styleVal);
			Jc expressStyleJc = null;
			HpsMeasure expressStyleFontSize = null;
			if (expressStyle ==null) {
				log.warn("No default paragraph style.");
			} else {
				if (expressStyle.getPPr()!=null) {
					expressStyleJc = expressStyle.getPPr().getJc();
				}
				if (expressStyle.getRPr()!=null) {
					expressStyleFontSize=expressStyle.getRPr().getSz();
				}
			}
			// Font size and jc for the style following its based on values
			PPr effectivePPr = propertyResolver.getEffectivePPr(styleVal);
			Jc effectiveJc = effectivePPr.getJc();
			
			RPr effectiveRPr = propertyResolver.getEffectiveRPr(styleVal);
			HpsMeasure effectiveFontSize = null;
			if (effectiveRPr!=null) {
				effectiveFontSize=effectiveRPr.getSz();
			}
			
			TableContext ctx = tblStack.peek();
			String tableStyle = ctx.tableStyleId;
			if (tableStyle == null) {
				return null;
			}
			// The table style's w:basedOn chain, merged
			Style tableStyleContrib = ctx.tableStyle;

			/* The conditional formats this paragraph is under, restricted to the ones the
			 * table style actually gives a w:pPr or w:rPr - in precedence order, so that
			 * the same set always names the same style.  A paragraph under none of them
			 * keeps the pre-17.1.1 style id, so nothing changes for a table style without
			 * conditional formatting. */
			List<CTTblStylePr> applicable = new ArrayList<CTTblStylePr>();
			EnumSet<STTblStyleOverrideType> named = EnumSet.noneOf(STTblStyleOverrideType.class);
			for (CTTblStylePr pr : TableStyleConditions.applicable(tableStyleContrib, conditions)) {
				if (TableStyleConditions.formatsText(pr)) {
					applicable.add(pr);
					if (pr.getType() != STTblStyleOverrideType.WHOLE_TABLE) named.add(pr.getType());
				}
			}
			String conditionKey = TableStyleConditions.key(named);

			String resultStyleID = styleVal+"-"+tableStyle;
			if (conditionKey.length() > 0) {
				resultStyleID = resultStyleID + "-" + conditionKey;
			}
			if (tableStyle.endsWith("-BR") && conditionKey.length() == 0) {
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
				if (isCyclic(thisStyle, hierarchy)) {
					log.warn("Cycle above detected in style basedOn hierarchy for: " + thisStyle.getStyleId() + " - stopping");					
					break;
				}
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
			
			
			// Next, the table style: its own w:pPr/w:rPr (the whole table), then the
			// conditional formats this paragraph is under, in precedence order
			// (ECMA-376-1 17.7.6; TableStyleConditions.PRECEDENCE)
			PPr tableStylePPr = StyleUtil.apply(tableStyleContrib.getPPr(), (PPr)null);
			RPr tableStyleRPr = StyleUtil.apply(tableStyleContrib.getRPr(), (RPr)null);
			for (CTTblStylePr pr : applicable) {
				if (log.isDebugEnabled()) {
					log.debug("Applying conditional " + pr.getType() + "\n" + XmlUtils.marshaltoString(pr, true, true));
				}
				tableStylePPr = StyleUtil.apply(pr.getPPr(), tableStylePPr);
				tableStyleRPr = StyleUtil.apply(pr.getRPr(), tableStyleRPr);
			}

			// What do the table styles contribute?
			Jc tableStyleJc = tableStylePPr==null ? null : tableStylePPr.getJc();
			
			HpsMeasure tableStyleFontSize = null;
			if (tableStyleRPr!=null) {
				tableStyleFontSize=tableStyleRPr.getSz();
			}
			
			// Now we can apply to table style contrib on top of docDefaults
			// As these are different style types:-
			newStyle.setPPr(StyleUtil.apply(tableStylePPr, newStyle.getPPr()));
			newStyle.setRPr(StyleUtil.apply(tableStyleRPr, newStyle.getRPr()));
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
										
						/* A paragraph naming no style is the default paragraph style's: that is how
						 * Word writes it, and since 17.1.1 (CR-015 phase 2) PropertyResolver resolves
						 * it so for the paragraph AND its runs.  Until 17.1.1 this preprocess wrote the
						 * default style's id onto every such paragraph, in a table or not, which
						 * shielded the exporters from the resolver's run-properties gap - and hid it
						 * from the corpus.  Outside a table the paragraph is now left as written
						 * (CR-015 phase 2b); inside one it still gets the synthetic style that carries
						 * the table style's conditional formatting, which the resolver cannot know. */
						String newStyle=defaultParagraphStyle;
						if (tblStack.size()>0) {
							p.getPPr().setPStyle(Context.getWmlObjectFactory().createPPrBasePStyle() );	
							// We're in a table
							String resultStyle;
							try {
								resultStyle = getCellPStyle(newStyle, true, tblStack.peek().conditionsFor(p));
							} catch (CyclicStylesException e) {
								throw new RuntimeException(e);
							}
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
							String newStyle;
							try {
								newStyle = getCellPStyle(styleVal, 
														styleVal.equals(defaultParagraphStyle),
														tblStack.peek().conditionsFor(p));
							} catch (CyclicStylesException e) {
								throw new RuntimeException(e);
							}
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
					
					// the table this row or cell belongs to: a nested table pushes its own
					// context before its rows are reached, and pops it before its parent's next
					TableContext enclosing = tblStack.peek();
					if (o instanceof Tbl) {
						tblStack.push(new TableContext((Tbl)o));
					} else if (enclosing != null && o instanceof Tr) {
						enclosing.currentTr = (Tr)o;
						enclosing.currentTc = null;
					} else if (enclosing != null && o instanceof Tc) {
						enclosing.currentTc = (Tc)o;
					}

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

					if (o instanceof Tbl) {
						tblStack.pop();
					} else if (enclosing != null && o instanceof Tr) {
						enclosing.currentTr = null;
						enclosing.currentTc = null;
					} else if (enclosing != null && o instanceof Tc) {
						enclosing.currentTc = null;
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
