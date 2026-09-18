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
import java.math.BigInteger;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.STBorder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
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
        		appendBorderHangTrio(opcPackage, s, result);
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
    	createCss(opcPackage, pPr, result, ignoreBorders, isListItem, null);
    }

    /**
     * A left paragraph border against a hanging indent: the hang moves from the margin
     * into the padding, so that the border stands where Word stands it.
     *
     * <p>Word draws a left border relative to the <em>leftmost</em> text edge -
     * min(w:left, w:left - hanging) - less w:space, so with a hanging indent the bar is
     * left of the first line and every line clears it by w:space.  In the CSS box model
     * the border sits at the margin edge: with {@code margin-left} = left and
     * {@code text-indent} = -hanging the first line starts inside the margin, left of
     * the bar, and the bar is drawn through it (a requirement's "REQ-013" to the left of
     * the bar and its text to the right).  The Indent and PBorderLeft properties are
     * independent, so the reconciliation is here, where the paragraph's CSS is
     * assembled: {@code margin-left} = left - hanging, {@code padding-left} = space +
     * hanging, {@code text-indent} = -hanging as before.  Nothing changes without a left
     * border, or with a positive firstLine.</p>
     *
     * <p>The border may come from the paragraph style while the indent is the
     * paragraph's own (an inline {@code margin-left} would then override the class
     * rule's shifted one and undo it), so the caller may hand in the <em>effective</em>
     * {@code w:pBdr}; the pPr's own is used where that is null.</p>
     *
     * @param effectivePPr the effective paragraph properties, or null to use the pPr's own
     * @since 17.1.1 (CR-003, the hanging-indent-across-a-border defect)
     */
    public static void createCss(OpcPackage opcPackage, PPr pPr, StringBuilder result, boolean ignoreBorders,
    		boolean isListItem, PPr effectivePPr) {
    	if (isListItem) {
    		result.append("display: list-item;");
    	}
		if (pPr==null) {
			return;
		}
		PPrBase.PBdr effectivePBdr = effectivePPr==null ? null : effectivePPr.getPBdr();
		CTBorder leftBorder = leftBorder(effectivePBdr!=null ? effectivePBdr : pPr.getPBdr());
		// the indent: the pPr's own, else the effective one (a paragraph with its own left
		// border and an inherited hang must shift too, or its inline padding undoes the rule)
		Ind shiftInd = pPr.getInd()!=null ? pPr.getInd()
				: (effectivePPr!=null ? effectivePPr.getInd() : null);
		BigInteger hanging = (shiftInd!=null) ? shiftInd.getHanging() : null;
		boolean shiftHang = leftBorder!=null && hanging!=null && hanging.intValue() > 0 && !isListItem;
		boolean indentEmitted = false;
    	List<Property> properties = PropertyFactory.createProperties(opcPackage, pPr);
    	for( Property p :  properties ) {
	    	if (shiftHang && p instanceof Indent) {
	    		// the hang moves from the margin into the padding (see above)
	    		Ind ind = (Ind) p.getObject();
	    		int left = ind.getLeft()!=null ? ind.getLeft().intValue() : 0;
	    		result.append("position: relative; ");
	    		result.append(Property.composeCss("margin-left", UnitsOfMeasurement.twipToBest(left - hanging.intValue())));
	    		BigInteger right = ind.getRight()!=null ? ind.getRight() : ind.getEnd();
	    		if (right!=null) {
	    			result.append(Property.composeCss("margin-right", UnitsOfMeasurement.twipToBest(right.intValue())));
	    		}
	    		result.append(Property.composeCss("text-indent", "-" + UnitsOfMeasurement.twipToBest(hanging.intValue())));
	    		indentEmitted = true;
	    		continue;
	    	}

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
    	if (shiftHang) {
    		if (!indentEmitted) {
    			// the indent is inherited: state the shifted margin here too, so that this
    			// element's padding and its class rule's margin agree
    			int left = shiftInd.getLeft()!=null ? shiftInd.getLeft().intValue() : 0;
    			result.append(Property.composeCss("margin-left", UnitsOfMeasurement.twipToBest(left - hanging.intValue())));
    			result.append(Property.composeCss("text-indent", "-" + UnitsOfMeasurement.twipToBest(hanging.intValue())));
    		}
    		// after the border's own padding-left (w:space), so that this one wins
    		int space = leftBorder.getSpace()!=null ? leftBorder.getSpace().intValue() : 0;   // points
    		result.append(Property.composeCss("padding-left",
    				UnitsOfMeasurement.twipToBest(space * 20 + hanging.intValue())));
    	}
    }

    /**
     * The margin-left / text-indent / padding-left trio of a bordered, hanging paragraph
     * style, from the style's <em>effective</em> pPr, appended to the end of its class
     * rule so that it wins over whatever the rule emitted from the style's own pPr.
     *
     * <p>The trio is self-consistent only when all three come from one rule.  A style
     * whose own pPr carries a left border but inherits its hanging indent (Recommendation
     * basedOn Requirement, its own border in another colour) emitted the border and
     * {@code padding-left} = w:space from its own pPr, and, later in the stylesheet at
     * equal specificity, that padding overrode the base rule's shifted one: the first
     * line started 56.7pt into the margin with 8pt of padding, and the ID was clipped off
     * the container's edge.  So a style whose own pPr contributes either a left border
     * or an indent gets the trio from its effective values, whatever its base emitted.</p>
     *
     * @since 17.1.1 (CR-003, the derived bordered style)
     */
    static void appendBorderHangTrio(OpcPackage opcPackage, Style s, StringBuilder result) {
    	PPr own = s.getPPr();
    	boolean contributes = leftBorder(own.getPBdr())!=null
    			|| (own.getInd()!=null && own.getInd().getHanging()!=null);
    	if (!contributes || !(opcPackage instanceof WordprocessingMLPackage)) return;
    	try {
    		PPr effective = ((WordprocessingMLPackage) opcPackage).getMainDocumentPart()
    				.getPropertyResolver().getEffectivePPr(s.getStyleId());
    		CTBorder border = effective==null ? null : leftBorder(effective.getPBdr());
    		BigInteger hanging = (effective!=null && effective.getInd()!=null) ? effective.getInd().getHanging() : null;
    		if (border==null || hanging==null || hanging.intValue() <= 0) return;
    		int left = effective.getInd().getLeft()!=null ? effective.getInd().getLeft().intValue() : 0;
    		int space = border.getSpace()!=null ? border.getSpace().intValue() : 0;
    		result.append(Property.composeCss("margin-left", UnitsOfMeasurement.twipToBest(left - hanging.intValue())));
    		result.append(Property.composeCss("text-indent", "-" + UnitsOfMeasurement.twipToBest(hanging.intValue())));
    		result.append(Property.composeCss("padding-left", UnitsOfMeasurement.twipToBest(space * 20 + hanging.intValue())));
    	} catch (Exception e) {
    		log.warn("effective pPr for style " + s.getStyleId() + ": " + e.getMessage());
    	}
    }

    /** The left border of a w:pBdr where it draws one (a value other than nil/none), else null.
     *  @since 17.1.1 */
    static CTBorder leftBorder(PPrBase.PBdr pBdr) {
    	if (pBdr==null || pBdr.getLeft()==null || pBdr.getLeft().getVal()==null) return null;
    	STBorder val = pBdr.getLeft().getVal();
    	if (val==STBorder.NIL || val==STBorder.NONE) return null;
    	return pBdr.getLeft();
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
