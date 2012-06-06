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
