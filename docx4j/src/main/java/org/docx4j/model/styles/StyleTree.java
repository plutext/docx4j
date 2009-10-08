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
	 * @param stylesInUse
	 * @param allStyles
	 */
	public StyleTree(List<String> stylesInUse, Map<String, Style> allStyles) {

		// Set up Paragraph style tree 
        // but first, add Normal.  (Doesn't matter if its already there)
		stylesInUse.add("Normal");
        for (String styleId : stylesInUse ) {
        	if (pTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.error("Couldn't find style: " + styleId);
                	continue;
                } 	        		
        		// Is it a paragraph style?
        		if (style.getType().equals("paragraph")) {                
	            	// Need to create a node for this
	        		this.addNode(styleId, allStyles, pTree);
        		}
        	}
        }
        
		// Set up Character style tree 
        // but first, add DefaultParagraphFont.  (Doesn't matter if its already there)
		stylesInUse.add("DefaultParagraphFont");
        for (String styleId : stylesInUse ) {
        	if (cTree.get(styleId)==null) {
        		
            	Style style = allStyles.get(styleId);
                if (style == null ) {
                	log.error("Couldn't find style: " + styleId);
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
	
	
	private Tree<AugmentedStyle>.Node<AugmentedStyle> addNode(String styleId, Map<String, Style> allStyles,
			Tree<AugmentedStyle> tree) {

		log.debug(styleId);
    	Style style = allStyles.get(styleId);
        if (style == null ) {
        	log.error("Couldn't find style: " + styleId);
        	return null;
        } 	        		
		
    	AugmentedStyle as = new AugmentedStyle(style);        	
    	Tree<AugmentedStyle>.Node<AugmentedStyle> n = 
    		 tree.new Node<AugmentedStyle>(styleId, as); 
    	
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
        		Tree<AugmentedStyle>.Node<AugmentedStyle> parent = addNode(basedOnStyleName, allStyles, tree);
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
    	
		StyleTree st = new StyleTree(stylesInUse, allStyles);
		
		System.out.println("\nParagraph styles\n");
		System.out.println(st.pTree.toString());
		System.out.println("\nCharacter styles\n");
		System.out.println(st.cTree.toString());
		
		System.out.println("\nParagraph classes\n");
		it = st.pTree.nodes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        
	        // Eclipse is fine with this, but not Ant.
	        // Underlying problem is http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6665356 (fixed in  7(b44) )   
	        // Preview Milestone 4 is b66.  
	        // Which fixes this problem (remember, you need jdk1.7.0/jre/lib/endorsed/jaxb-*
	        
	        Tree<AugmentedStyle>.Node<AugmentedStyle> n 
	        	= (Tree<AugmentedStyle>.Node<AugmentedStyle>)pairs.getValue();
	        List<Tree<AugmentedStyle>.Node<AugmentedStyle>> classVals =  st.pTree.climb(n);
	        
	        System.out.println(n.name + ":'" + 
	        		getHtmlClassAttributeValue(st.pTree, n)
	        		+ "'");
	    }

		System.out.println("\nRun classes\n");
		it = st.cTree.nodes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        	        
	        Tree<AugmentedStyle>.Node<AugmentedStyle> n 
	        	= (Tree<AugmentedStyle>.Node<AugmentedStyle>)pairs.getValue();
	        List<Tree<AugmentedStyle>.Node<AugmentedStyle>> classVals =  st.cTree.climb(n);
	        
	        System.out.println(n.name + ":'" + 
	        		getHtmlClassAttributeValue(st.cTree, n)
	        		+ "'");
	    }
	    
	}
	
	public static String getHtmlClassAttributeValue(Tree<AugmentedStyle> tree,
			Tree<AugmentedStyle>.Node<AugmentedStyle> n) {

        List<Tree<AugmentedStyle>.Node<AugmentedStyle>> classVals =  tree.climb(n);
    	StringBuffer sb = new StringBuffer();
        for (Tree<AugmentedStyle>.Node<AugmentedStyle> valNode : classVals) {
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
	
	/**
	 * Represents a Tree of Objects of generic type T. The Tree is represented as
	 * a single rootElement which points to a List<Node<T>> of children. There is
	 * no restriction on the number of children that a particular node may have.
	 * This Tree provides a method to serialize the Tree into a List by doing a
	 * pre-order traversal. It has several methods to allow easy updation of Nodes
	 * in the Tree.
	 * 
	 * Original code from http://sujitpal.blogspot.com/2006/05/java-data-structure-generic-tree.html
	 */
	public class Tree<T> {
	 
	    private Node<T> rootElement;
	     
	    
//	    private Map<String, Node<T>> nodes = new HashMap<String, Node<T>>(); // weird compile errors on put with this
	    private Map<String, Object> nodes = new HashMap<String, Object>();
	    
	    /**
	     * Quick access to any node in the tree.
	     * @param name
	     * @return
	     */
	    public Node<T> get(String name) {
	    	
	    	if (log.isDebugEnabled()) {
	    		Node<T> result = (Node<T>)nodes.get(name);
	    		if (result==null) {
	    			log.warn("Null result for " + name);
	    		} 
	    		return result;
	    	}
	    	return (Node<T>)nodes.get(name);
	    	
	    }
	 
	    /**
	     * Return the root Node of the tree.
	     * @return the root element.
	     */
	    public Node<T> getRootElement() {
	        return this.rootElement;
	    }
	 
	    /**
	     * Set the root Element for the tree.
	     * @param rootElement the root element to set.
	     */
	    public void setRootElement(Node<T> rootElement) {
	        this.rootElement = rootElement;
        	nodes.put(rootElement.name, rootElement);	        
	    }
	     
	    /**
	     * Returns the Tree<T> as a List of Node<T> objects. The elements of the
	     * List are generated from a pre-order traversal of the tree.
	     * @return a List<Node<T>>.
	     */
	    public List<Node<T>> toList() {
	        List<Node<T>> list = new ArrayList<Node<T>>();
	        walk(rootElement, list);
	        return list;
	    }
	     
	    /**
	     * Returns a String representation of the Tree. The elements are generated
	     * from a pre-order traversal of the Tree.
	     * @return the String representation of the Tree.
	     */
	    public String toString() {
	    	
	    	StringBuffer sb = new StringBuffer();
	    	for (Node<T> n : toList() ) {
	    		sb.append(n.name + "\n");	    		
	    	}
	    	
	        return sb.toString();
	    }
	     
	    /**
	     * Walks the Tree in pre-order style. This is a recursive method, and is
	     * called from the toList() method with the root element as the first
	     * argument. It appends to the second argument, which is passed by reference     
	     * as it recurses down the tree.
	     * @param element the starting element.
	     * @param list the output of the walk.
	     */
	    private void walk(Node<T> element, List<Node<T>> list) {
	    	if (element==null) {return;}
	        list.add(element);
	        for (Node<T> data : element.getChildren()) {
	            walk(data, list);
	        }
	    }

	    public List<Node<T>> climb(Node<T> n) {
	        List<Node<T>> list = new ArrayList<Node<T>>();
	        climb(n, list);
	        return list;
	    }	    
	    private List<Node<T>> climb(Node<T> n, List<Node<T>> list) {
	        list.add(n);
	    	if (n.equals(this.rootElement)) {
	    		return list;
	    	} else {
	    		Node<T> parent = n.getParent();
		        climb(parent, list);
	    	}
	        return list;
	    }
	    
		/**
		 * Represents a node of the Tree<T> class. The Node<T> is also a container, and
		 * can be thought of as instrumentation to determine the location of the type T
		 * in the Tree<T>.
		 */
		public class Node<T> {
		 
		    /**
		     * Nodes have names
		     */
		    private String name;
			
		    public T data;
		    public List<Node<T>> children;
		    
		    private Node<T> parent;
			public Node<T> getParent() {
				return parent;
			}
			public void setParent(Node<T> parent) {
				this.parent = parent;
			}
		    		 
		    /**
		     * Convenience ctor to create a Node<T> with an instance of T.
		     * @param data an instance of T.
		     */
		    public Node(String name, T data) {
		        //this();
		    	this.name = name;
		        setData(data);
		    }
		     
		    /**
		     * Return the children of Node<T>. The Tree<T> is represented by a single
		     * root Node<T> whose children are represented by a List<Node<T>>. Each of
		     * these Node<T> elements in the List can have children. The getChildren()
		     * method will return the children of a Node<T>.
		     * @return the children of Node<T>
		     */
		    public List<Node<T>> getChildren() {
		        if (this.children == null) {
		            return new ArrayList<Node<T>>();
		        }
		        return this.children;
		    }
//		 
//		    /**
//		     * Sets the children of a Node<T> object. See docs for getChildren() for
//		     * more information.
//		     * @param children the List<Node<T>> to set.
//		     */
//		    public void setChildren(List<Node<T>> children) {
//		        this.children = children;
//		    }
//		 
//		    /**
//		     * Returns the number of immediate children of this Node<T>.
//		     * @return the number of immediate children.
//		     */
//		    public int getNumberOfChildren() {
//		        if (children == null) {
//		            return 0;
//		        }
//		        return children.size();
//		    }
		     
		    /**
		     * Adds a child to the list of children for this Node<T>. The addition of
		     * the first child will create a new List<Node<T>>.
		     * @param child a Node<T> object to set.
		     */
		    public void addChild(Node<T> child) {
		    	
		    	
		    	
		        if (children == null) {
		            children = new ArrayList<Node<T>>();
		        }
		        if (!children.contains(child)) {
		        	children.add(child);
		        	nodes.put(child.name, child);
		        	child.setParent(this);
		        }
		    }
		     
//		    /**
//		     * Inserts a Node<T> at the specified position in the child list. Will     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
//		     * @param index the position to insert at.
//		     * @param child the Node<T> object to insert.
//		     * @throws IndexOutOfBoundsException if thrown.
//		     */
//		    public void insertChildAt(int index, Node<T> child) throws IndexOutOfBoundsException {
//		        if (index == getNumberOfChildren()) {
//		            // this is really an append
//		            addChild(child);
//		            return;
//		        } else {
//		            children.get(index); //just to throw the exception, and stop here
//		            children.add(index, child);
//		        }
//		    }
//		     
//		    /**
//		     * Remove the Node<T> element at index index of the List<Node<T>>.
//		     * @param index the index of the element to delete.
//		     * @throws IndexOutOfBoundsException if thrown.
//		     */
//		    public void removeChildAt(int index) throws IndexOutOfBoundsException {
//		        children.remove(index);
//		    }
		 
		    public T getData() {
		        return this.data;
		    }
		 
		    public void setData(T data) {
		        this.data = data;
		    }
		     
		    public String toString() {
		        StringBuilder sb = new StringBuilder();
		        sb.append("{").append(getData().toString()).append(",[");
		        int i = 0;
		        for (Node<T> e : getChildren()) {
		            if (i > 0) {
		                sb.append(",");
		            }
		            sb.append(e.getData().toString());
		            i++;
		        }
		        sb.append("]").append("}");
		        return sb.toString();
		    }

		}

	}

}
