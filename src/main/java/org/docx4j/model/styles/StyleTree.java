package org.docx4j.model.styles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;

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
	
	private static Logger log = Logger.getLogger(StyleTree.class);

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


	/**
	 * Build a StyleTree for stylesInUse. 
	 * 
	 * @param stylesInUse styles actually in use in the main document part, headers/footers, footnotes/endnotes 
	 * @param allStyles styles defined in the style definitions part
	 */
	public StyleTree(List<String> stylesInUse, Map<String, Style> allStyles,
			String defaultParagraphStyleId, String defaultCharacterStyleId) {
		
		
		// Set up Table style tree 
        for (String styleId : stylesInUse ) {
        	if (tableTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.warn("Couldn't find style: " + styleId);
                	continue;
                } 	        		
        		// Is it a table style?
        		if (style.getType().equals("table")) {                
	            	// Need to create a node for this
	        		this.addNode(styleId, allStyles, tableTree);
        		}
        	}
        }
		

		// Set up Paragraph style tree 
        // but first, add Normal.  (Doesn't matter if its already there)
		stylesInUse.add(defaultParagraphStyleId);
        for (String styleId : stylesInUse ) {
        	if (pTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.warn("Couldn't find style: " + styleId);
                	continue;
                } 	        		
        		// Is it a paragraph style?
        		if (style.getType().equals("paragraph")) {                
	            	// Need to create a node for this
        			log.debug("Adding '" +  styleId + "' to paragraph tree" );
	        		this.addNode(styleId, allStyles, pTree);
        		}
        	}
        }
        
		// Set up Character style tree 
        // but first, add DefaultParagraphFont.  (Doesn't matter if its already there)
		stylesInUse.add(defaultCharacterStyleId);
        for (String styleId : stylesInUse ) {
        	if (cTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.warn("Couldn't find style: " + styleId);
                	continue;
                } 	        		
        		// Is it a character style?
        		if (style.getType().equals("character")) {                
	            	// Need to create a node for this
	        		this.addNode(styleId, allStyles, cTree);
        		}
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
		
    	AugmentedStyle as = new AugmentedStyle(style);        	
    	Node<AugmentedStyle> n = 
    		 new Node<AugmentedStyle>(tree, styleId, as); 
    	
    	// Find its parent
    	if (style.getBasedOn()==null) {
    		
    		// This must be the root element
			log.debug("Style " + styleId + " is a root style.");
			if (tree.getRootElement()==null) {
				tree.setRootElement(n);
			} else {
				// Sanity check
				String root = tree.getRootElement().getData().getStyle().getStyleId();
				log.debug("Existing root:" + root);
				if (styleId.equals(root)) {
					// ok
				} else {
					log.warn("overwriting root node: " + styleId);
					tree.setRootElement(n);
				}
			}
			
    	} else if (style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();   
        	log.debug("..based on " + basedOnStyleName);        	
        	if (tree.get(basedOnStyleName)==null) {
//            	log.debug("..can disregard that null, but it shouldn't happen again for this style");        	
        		Node<AugmentedStyle> parent = addNode(basedOnStyleName, allStyles, tree);
        		parent.addChild(n);
        	} else {
        		tree.get(basedOnStyleName).addChild(n);
        	}
        	
    	} else {
    		log.error("No basedOn set for: " + style.getStyleId() );
    	}
    	
    	return n;
		
	}
	
	
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/StyleResolution.xml";
		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		

		// Setup
    	List<String> stylesInUse = new ArrayList<String>();
    	Map<String, String> stylesInUseMap = wmlPackage.getMainDocumentPart().getStylesInUse();
		Iterator it = stylesInUseMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String styleId = (String)pairs.getKey();
	        stylesInUse.add(styleId);
	    }
    	
		Map<String, Style> allStyles = new HashMap<String, Style>();
		Styles styles = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();		
		for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
			allStyles.put(s.getStyleId(), s);	
			log.debug("live style: " + s.getStyleId() );
		}
    	
		StyleTree st = new StyleTree(stylesInUse, allStyles,
				wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle().getStyleId(),
				wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getDefaultCharacterStyle().getStyleId());
		
		log.debug("\nParagraph styles\n");
		log.debug(st.pTree.toString());
		log.debug("\nCharacter styles\n");
		log.debug(st.cTree.toString());
		
		log.debug("\nParagraph classes\n");
		it = st.pTree.nodes.entrySet().iterator();
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
	
	public static String getHtmlClassAttributeValue(Tree<AugmentedStyle> tree,
			Node<AugmentedStyle> n) {
    	if (n==null) {
    		log.error("Null node passed");
    		return null;
    	}
        List<Node<AugmentedStyle>> classVals =  tree.climb(n);
    	StringBuffer sb = new StringBuffer();
        for (Node<AugmentedStyle> valNode : classVals) {        	
        	sb.append(valNode.name + " ");	    		
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

}
