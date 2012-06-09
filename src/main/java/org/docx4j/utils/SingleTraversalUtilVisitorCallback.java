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
 
package org.docx4j.utils;

import java.util.List;

/** 
 * Use this if there is only a single object type (eg just P's)
 * you are interested in doing something with.
 * 
 * For an example of use, see sample ImageConvertEmbeddedToLinked
 * 
 * @author alberto */
public class SingleTraversalUtilVisitorCallback extends AbstractTraversalUtilVisitorCallback {
	
	protected TraversalUtilVisitor visitor = null;
	protected Class visitorClass = null;
	
	public SingleTraversalUtilVisitorCallback(TraversalUtilVisitor visitor) {
		this.visitor = visitor;
		visitorClass = findClassParameter(visitor.getClass());
		if (visitorClass == null) {
			throw new IllegalArgumentException("Can't identify the parameter class of the visitor " + visitor.getClass().getName());
		}
	}
	
	@Override
	protected List<Object> apply(Object child, Object parent, List siblings) {
		if (visitorClass.isAssignableFrom(child.getClass())) {
			visitor.apply(child, parent, siblings);
		}
		return null;
	}

}
