package org.docx4j.model.styles;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.Style.BasedOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

/**
 * Represent a style hierarchy as a tree.
 * 
 * TODO - need a way to update/refresh.
 * 
 * This is useful for creating certain representations
 * (eg CSS).
 * 
 * @author jharrop
 *
 */
public class StyleTree {
	
	private static Logger log = LoggerFactory.getLogger(StyleTree.class);

	/**
	 * Tree of table styles
	 */
	private Tree<AugmentedStyle> tableTree = new Tree<AugmentedStyle>();
	public Tree<AugmentedStyle> getTableStylesTree() {
		return tableTree;
	}
	
	/**
	 * Tree of paragraph styles
	 */
	private Tree<AugmentedStyle> pTree = new Tree<AugmentedStyle>();
	public Tree<AugmentedStyle> getParagraphStylesTree() {
		return pTree;
	}

	/**
	 * Tree of character styles
	 */
	private Tree<AugmentedStyle> cTree = new Tree<AugmentedStyle>();
	public Tree<AugmentedStyle> getCharacterStylesTree() {
		return cTree;
	}

	private StyleTree() {};
	
	String ROOT_NAME = "DocDefaults";
	
	
	/**
	 * Build a StyleTree for stylesInUse.
	 * 
	 * @param stylesInUse styles actually in use in the main document part,
	 *                    headers/footers, footnotes/endnotes
	 * @param allStyles   styles defined in the style definitions part
	 */
	public StyleTree(Set<String> stylesInUse, Map<String, Style> allStyles, DocDefaults docDefaults, Style normal) {

		// Set up Table style tree
		init(stylesInUse, allStyles, docDefaults, normal, null, null);

	}

	/**
	 * Build a StyleTree for stylesInUse.
	 * 
	 * @param stylesInUse styles actually in use in the main document part,
	 *                    headers/footers, footnotes/endnotes
	 * @param allStyles   styles defined in the style definitions part
	 * @since 11.1.3
	 */
	public StyleTree(Set<String> stylesInUse, Map<String, Style> allStyles, DocDefaults docDefaults, Style normal,
			Style defaultCharStyle, Style defaultTableStyle) {

		init(stylesInUse, allStyles, docDefaults, normal, defaultCharStyle, defaultTableStyle);
	}

	/**
	 * Build a StyleTree for stylesInUse.
	 * 
	 * @param stylesInUse styles actually in use in the main document part,
	 *                    headers/footers, footnotes/endnotes
	 * @param allStyles   styles defined in the style definitions part
	 */
	public void init(Set<String> stylesInUse, Map<String, Style> allStyles, DocDefaults docDefaults, Style normal,
			Style defaultCharStyle, Style defaultTableStyle) {

		// Set up Table style tree
		if (defaultTableStyle == null) {
			tableTree.setRootElement(new Node<AugmentedStyle>(tableTree, "table-root", null));
		} else {
			AugmentedStyle as = new AugmentedStyle(defaultTableStyle);
			tableTree.setRootElement(new Node<AugmentedStyle>(tableTree, "table-root", as));
		}
		for (String styleId : stylesInUse) {
        	if (tableTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.warn("Couldn't find style: " + styleId);
                	continue;
                } else if (style.getType()==null) {
                    if(log.isWarnEnabled()) {
                        log.warn("missing type: " + XmlUtils.marshaltoString(style));
                    }
                } else
        		// Is it a table style?
        		if (style.getType().equals("table")) {                
	            	// Need to create a node for this
	        		this.addNode(styleId, allStyles, tableTree);
        		}
        	}
        }
		

		// Set up Paragraph style tree 
        try {
			createVirtualStylesForDocDefaults(docDefaults, normal);
		} catch (Docx4JException e) {
			// shouldn't happen
			log.error(e.getMessage(), e);
			return;
		}
        Style rootStyle = this.styleDocDefaults;
        if (rootStyle==null) {
        	pTree.setRootElement(new Node<AugmentedStyle>(pTree, "p-root", null));
        } else {
        	AugmentedStyle as = new AugmentedStyle(rootStyle);        	
        	pTree.setRootElement(new Node<AugmentedStyle>(pTree, ROOT_NAME, as));        	
        }
        	
        for (String styleId : stylesInUse ) {
        	if (pTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.warn("Couldn't find style: " + styleId);
                	// See BrokenStyleRemediator for some causes of this, and potential fix
                	continue;
                } 
                                
        		// Is it a paragraph style?
        		if (style.getType()!=null 
        				&& style.getType().equals("paragraph")) {                
	            	// Need to create a node for this
        			log.debug("Adding '" +  styleId + "' to paragraph tree" );
	        		this.addNode(styleId, allStyles, pTree);
        		}
        	} else {
        		log.debug(styleId + " is already in paragraph tree");
        	}
        }
        
		// Set up Character style tree
		if (defaultCharStyle == null) {
			cTree.setRootElement(new Node<AugmentedStyle>(cTree, "c-root", null));
		} else {
			AugmentedStyle as = new AugmentedStyle(defaultCharStyle);
			cTree.setRootElement(new Node<AugmentedStyle>(cTree, "c-root", as));
		}
		for (String styleId : stylesInUse) {
        	if (cTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.warn("Couldn't find style: " + styleId);
                	continue;
                } 	        		
        		// Is it a character style?
        		if (style.getType()!=null 
        				&& style.getType().equals("character")) {                
	            	// Need to create a node for this
	        		this.addNode(styleId, allStyles, cTree);
        		}
        	} else {
        		log.debug(styleId + " is already in character tree");
        	}
        }        
	}
	
	
	private Node<AugmentedStyle> addNode(String styleId, Map<String, Style> allStyles,
			Tree<AugmentedStyle> tree) {

		log.debug(styleId);
    	Style style = allStyles.get(styleId);
        if (style == null ) {
        	log.error("Couldn't find style: " + styleId);
        	return null;
        } 	        		
		
    	
    	// Find its parent
    	if (style.getBasedOn()==null) {

			// Make it basedOn DocDefaults.
    		// But we have to clone it first, so we don't alter the document proper
    		Style clonedStyle = XmlUtils.deepCopy(style);
    		
    		BasedOn based = Context.getWmlObjectFactory().createStyleBasedOn();
    		based.setVal(ROOT_NAME);		
    		clonedStyle.setBasedOn(based);
    		    		
        	AugmentedStyle as = new AugmentedStyle(clonedStyle);        	
        	Node<AugmentedStyle> n = 
        		 new Node<AugmentedStyle>(tree, styleId, as); 
    		    		
    		// You can have more than 1 node which isn't based on anything
			log.debug("Style " + styleId + " is not based on anything.");
			tree.getRootElement().addChild(n);
			
        	return n;
						
    	} else if (style.getBasedOn().getVal()!=null) {
    		
        	AugmentedStyle as = new AugmentedStyle(style);        	
        	Node<AugmentedStyle> n = 
        		 new Node<AugmentedStyle>(tree, styleId, as); 
    		
        	String basedOnStyleName = style.getBasedOn().getVal();   
        	log.debug("..based on " + basedOnStyleName);        	
        	if (tree.get(basedOnStyleName)==null) {
//            	log.debug("..can disregard that null, but it shouldn't happen again for this style");        	
        		Node<AugmentedStyle> parent = addNode(basedOnStyleName, allStyles, tree);
        		if (parent!=null) {
        			parent.addChild(n);
        		}
        	} else {
        		tree.get(basedOnStyleName).addChild(n);
        	}

        	return n;
        	
    	} else {
    		log.error("No basedOn set for: " + style.getStyleId() );
        	return null;
    	}
    	
		
	}
	
/*	
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/StyleResolution.xml";
		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		

		// Setup
    	Set<String> stylesInUse = wmlPackage.getMainDocumentPart().getStylesInUse();

    	
		Map<String, Style> allStyles = new HashMap<String, Style>();
		Styles styles = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart(false).getJaxbElement();		
		for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
			allStyles.put(s.getStyleId(), s);	
			log.debug("live style: " + s.getStyleId() );
		}
		
    	
		StyleTree st = new StyleTree(stylesInUse, allStyles,
				styles.getDocDefaults(), allStyles.get("Normal"));
		
		log.debug("\nParagraph styles\n");
		log.debug(st.pTree.toString());
		log.debug("\nCharacter styles\n");
		log.debug(st.cTree.toString());
		
		log.debug("\nParagraph classes\n");
		Iterator it = st.pTree.nodes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        
	        // Eclipse is fine with this, but not Ant.
	        // Underlying problem is http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6665356 (fixed in  7(b44) )   
	        // Preview Milestone 4 is b66.  
	        // Which fixes this problem (remember, you need jdk1.7.0/jre/lib/endorsed/jaxb-*
	        
	        Node<AugmentedStyle> n 
	        	= (Node<AugmentedStyle>)pairs.getValue();
	        List<Node<AugmentedStyle>> classVals =  st.pTree.climb(n);
	        
	        log.debug(n.name + ":'" + 
	        		getHtmlClassAttributeValue(st.pTree, n)
	        		+ "'");
	    }

	    log.debug("\nRun classes\n");
		it = st.cTree.nodes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        	        
	        Node<AugmentedStyle> n 
	        	= (Node<AugmentedStyle>)pairs.getValue();
	        List<Node<AugmentedStyle>> classVals =  st.cTree.climb(n);
	        
	        log.debug(n.name + ":'" + 
	        		getHtmlClassAttributeValue(st.cTree, n)
	        		+ "'");
	    }
	    
	}
*/
	
	public static String getHtmlClassAttributeValue(Tree<AugmentedStyle> tree,
			Node<AugmentedStyle> n) {
    	if (n==null) {
    		log.error("Null node passed");
    		return null;
    	}
        List<Node<AugmentedStyle>> classVals =  tree.climb(n);
    	StringBuffer sb = new StringBuffer();
        for (Node<AugmentedStyle> valNode : classVals) { 
        	// Avoid including root node (eg dummy character root node)
        	if (valNode.getData()!=null) {
        		sb.append(valNode.name + " ");
        	}
        }
        return sb.toString();
	}
	
	
	public class AugmentedStyle {
		
		private Style s;		
		
		public AugmentedStyle(Style s) {
			this.s = s;
		}

		public Style getStyle() {
			return s;
		}
		
	}
	
	Style styleDocDefaults;
	
	/**
	 * Manufacture a paragraph style from the following, so it can be used as the 
	 * root of our paragraph style tree.
	 * 
	 * 	<w:docDefaults>
			<w:rPrDefault>
				<w:rPr>
					<w:rFonts w:asciiTheme="minorHAnsi" w:eastAsiaTheme="minorHAnsi" w:hAnsiTheme="minorHAnsi" w:cstheme="minorBidi" />
					<w:sz w:val="22" />
					<w:szCs w:val="22" />
					<w:lang w:val="en-US" w:eastAsia="en-US" w:bidi="ar-SA" />
				</w:rPr>
			</w:rPrDefault>
			<w:pPrDefault>
				<w:pPr>
					<w:spacing w:after="200" w:line="276" w:lineRule="auto" />
				</w:pPr>
			</w:pPrDefault>
		</w:docDefaults>
		
		BEWARE: in a table, paragraph style ppr generally trumps table style ppr (but see below).
		The effect of including w:docDefaults in the style hierarchy
		is that they trump table style ppr, but they should not!
		
	 * There is no need for a doc defaults character style.
	 * The reason for this is that Word seems to ignore Default Paragraph Style!
	 * So the run formatting comes from paragraph style + explicit character style (if any),
	 * plus direct formatting.
	 * 
	 * ------------------
	 * 		
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
		

		Note org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix		
		

	 */
    private void createVirtualStylesForDocDefaults(DocDefaults docDefaults, Style normal) throws Docx4JException {
    	
    	if (styleDocDefaults!=null) return; // been done already
    	
    	styleDocDefaults = Context.getWmlObjectFactory().createStyle();
    	styleDocDefaults.setStyleId(ROOT_NAME);
    	
    	styleDocDefaults.setType("paragraph");
    	
		org.docx4j.wml.Style.Name n = Context.getWmlObjectFactory().createStyleName();
    	n.setVal(ROOT_NAME);
    	styleDocDefaults.setName(n);
    					
		if (docDefaults == null) {
			log.info("No DocDefaults present");
			// The only way this can happen is if the
			// styles definition part is missing the docDefaults element
			// (these are present in docs created from Word, and
			// in our default styles, so maybe the user created it using
			// some 3rd party program?)
			try {

				docDefaults = (DocDefaults) XmlUtils
						.unmarshalString(StyleDefinitionsPart.docDefaultsString);
			} catch (JAXBException e) {
				throw new Docx4JException("Problem unmarshalling "
						+ StyleDefinitionsPart.docDefaultsString, e);
			}
		}

		// Setup documentDefaultPPr
		PPr documentDefaultPPr;
		if (docDefaults.getPPrDefault() == null) {
			log.info("No PPrDefault present");
			try {
				documentDefaultPPr = (PPr) XmlUtils
						.unmarshalString(StyleDefinitionsPart.pPrDefaultsString);
			} catch (JAXBException e) {
				throw new Docx4JException("Problem unmarshalling "
						+ StyleDefinitionsPart.pPrDefaultsString, e);
			}

		} else {
			documentDefaultPPr = docDefaults.getPPrDefault().getPPr();
			if (documentDefaultPPr==null) {
				documentDefaultPPr = Context.getWmlObjectFactory().createPPr();
			}
		}
		
		// If the docDefaults have no setting for w:spacing
		// then add it:
		if (documentDefaultPPr.getSpacing()==null) {
			Spacing spacing = Context.getWmlObjectFactory().createPPrBaseSpacing();
			documentDefaultPPr.setSpacing(spacing);
			spacing.setBefore(BigInteger.ZERO);
			spacing.setAfter(BigInteger.ZERO);
			spacing.setLine(BigInteger.valueOf(240));
		}

		// Setup documentDefaultRPr
		RPr documentDefaultRPr;
		if (docDefaults.getRPrDefault() == null) {
			log.info("No RPrDefault present");
			try {
				documentDefaultRPr = (RPr) XmlUtils
						.unmarshalString(StyleDefinitionsPart.rPrDefaultsString);
					// that includes font size 10
			} catch (JAXBException e) {
				throw new Docx4JException("Problem unmarshalling "
						+ StyleDefinitionsPart.rPrDefaultsString, e);
			}
		} else {
			documentDefaultRPr = docDefaults.getRPrDefault().getRPr();
			if (documentDefaultRPr==null) {
				documentDefaultRPr = Context.getWmlObjectFactory().createRPr();
			}
			// If default font size is not specified, set it to match Word default when unspecified (ie 10)
			// It is useful to have this explicitly, especially for XHTML Import
//	        <w:sz w:val="20"/>
//	        <w:szCs w:val="20"/>			
			if (documentDefaultRPr.getSz()==null) {
				HpsMeasure s10pt = Context.getWmlObjectFactory().createHpsMeasure();
				s10pt.setVal(BigInteger.valueOf(20));
				documentDefaultRPr.setSz(s10pt);
			}
			if (documentDefaultRPr.getSzCs()==null) {
				HpsMeasure s10pt = Context.getWmlObjectFactory().createHpsMeasure();
				s10pt.setVal(BigInteger.valueOf(20));
				documentDefaultRPr.setSzCs(s10pt);
			}
		}
    	
		styleDocDefaults.setPPr(documentDefaultPPr);
		styleDocDefaults.setRPr(documentDefaultRPr);
		
		// Now point Normal at this
//		Style normal = getDefaultParagraphStyle();
		if (normal==null) {
			log.info("No default paragraph style!!");
			normal = Context.getWmlObjectFactory().createStyle();
			normal.setType("paragraph");
			normal.setStyleId("Normal");
			
			n = Context.getWmlObjectFactory().createStyleName();
			n.setVal("Normal");
			normal.setName(n);
//			this.getJaxbElement().getStyle().add(normal);	
			
			normal.setDefault(Boolean.TRUE); // @since 3.2.0
		}
		

        if(log.isDebugEnabled()) {
            log.debug("Set virtual style, id '" + styleDocDefaults.getStyleId() + "', name '" + styleDocDefaults.getName().getVal() + "'");
            log.debug(XmlUtils.marshaltoString(styleDocDefaults, true, true));
        }
		
		
    	
    }
	

}
