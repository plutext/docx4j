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
     * @param styleId
     * @return
     */
    public Node<T> get(String styleId) {
    	
//    	if (log.isDebugEnabled()) {
//    		Node<T> result = (Node<T>)nodes.get(name);
//    		if (result==null) {
//    			log.warn("Null result for " + name);
//    		} 
//    		return result;
//    	}
    	return (Node<T>)nodes.get(styleId);
    	
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
    	nodes.put(rootElement.styleId, rootElement);	        
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
    		sb.append(n.styleId + "\n");	    		
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
    
    /**
     * Distance to root
     * 
     * @param node
     * @return
     * @since 11.5.1
     */
    public int getDistanceFromRoot(Node<T> node) {
    	
    	return climb(node).size();

    }
    
    /**
     * Simple metric for the distance between 2 nodes.
     * 
     * @param node1
     * @param node2
     * @return
     * @since 11.5.1
     */
    public int getDistance(Node<T> node1, Node<T> node2) {
    	
    	/*
    	 * Algorithm:
    	 * - count how far down each node is
    	 * - for deepest, climb the tree til they are same level
    	 * - now, iteratively, climb both by 1 until they meet
    	 */
    	
    	int distance =0;
    	
    	// count how far down each node is
    	List<Node<T>> branch1 = climb(node1);
    	List<Node<T>> branch2 = climb(node2);
    	
    	int len1 = branch1.size();
    	int len2 = branch2.size();
    	
    	if (len1==len2) {
    		// same depth
    	} else if (len1>len2) {
    		distance = len1-len2;
    		for (int i=0; i<distance; i++) {
    			branch1.remove(0); // remove the first
    		}
    	} else {
    		distance = len2-len1;
    		for (int i=0; i<distance; i++) {
    			branch2.remove(0); // remove the first
    		}    		
    	}
    	
    	// now they are the same length
    	if (branch1.size()!=branch2.size()) {
    		throw new RuntimeException("branches should be the same size!");
    	}
    	
    	for (int j = branch1.size(); j>0; j--) {
    		if (branch1.get(j)==branch2.get(j)) {
    			// converged!
    			break;
    		}
    		// nope
    		distance+=2;
    	}
    	
    	return distance;
    }
}	    
