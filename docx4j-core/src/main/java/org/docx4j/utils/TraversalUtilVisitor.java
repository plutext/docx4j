/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
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
package org.docx4j.utils;

import java.util.List;

/**
 * 
 * Extend this class to specify what should be done when a
 * particular object (eg P or Table) is visited during the
 * traversal.
 *
 *  @author alberto
 */
public abstract class TraversalUtilVisitor<T> {
// Doing this as a class instead of an interface ensures that it is part of the inheritance hierarchy.
	
	/**
	 * 
	 * @param element
	 * @param parent (logical?)parent in the of the element 
	 * @param siblings List of the element (this includes the element itself). 
	 * 	               This list can't be changed while the visitor is running(!)
	 */
	public void apply(T element, Object parent, List<Object> siblings) {
		apply(element);
	}

	/** Apply method if there is no need to access the surrounding elements
	 * 
	 * @param element
	 */
	public void apply(T element) {
		throw new UnsupportedOperationException("At least one of the methods needs to be subclassed");
	}
}
