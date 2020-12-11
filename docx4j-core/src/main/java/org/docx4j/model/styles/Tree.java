package org.docx4j.model.styles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static Logger log = LoggerFactory.getLogger(Tree.class);	
 
    private Node<T> rootElement;
         
//	    private Map<String, Node<T>> nodes = new HashMap<String, Node<T>>(); // weird compile errors on put with this
    protected Map<String, Object> nodes = new HashMap<String, Object>();
    
    /**
     * Quick access to any node in the tree.
     * @param name
     * @return
     */
    public Node<T> get(String name) {
    	
//    	if (log.isDebugEnabled()) {
//    		Node<T> result = (Node<T>)nodes.get(name);
//    		if (result==null) {
//    			log.warn("Null result for " + name);
//    		} 
//    		return result;
//    	}
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
    		if (parent==null) { // no basedOn    			
    			return list; 
    		}
	        climb(parent, list);
    	}
        return list;
    }
}	    
