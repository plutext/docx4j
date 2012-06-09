/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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
 * Represents a node of the Tree<T> class. The Node<T> is also a container, and
 * can be thought of as instrumentation to determine the location of the type T
 * in the Tree<T>.
 */
public class Node<T> {
 
    /**
     * Nodes have names
     */
    protected String name;
    
    protected Tree<T> tree;
	
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
    public Node(Tree<T> tree, String name, T data) {
        //this();
    	this.name = name;
        setData(data);
        this.tree = tree;
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
        	tree.nodes.put(child.name, child);
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

