package org.docx4j.utils;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/** 
 * Use this if there is more than one object type (eg Tables and Paragraphs)
 * you are interested in doing something with during the traversal.
 * 
 * @author alberto
 */
public class CompoundTraversalUtilVisitorCallback extends
		AbstractTraversalUtilVisitorCallback {

	Map<Class, List<TraversalUtilVisitor>> visitorMap = null;

	public CompoundTraversalUtilVisitorCallback(
			List<TraversalUtilVisitor> visitorList) {

		visitorMap = setupTraversalVistorMap(visitorList);
	}

	protected Map<Class, List<TraversalUtilVisitor>> setupTraversalVistorMap(
			List<TraversalUtilVisitor> visitorList) {

		Map<Class, List<TraversalUtilVisitor>> ret = new HashMap<Class, List<TraversalUtilVisitor>>();
		Class visitorClass = null;
		List<TraversalUtilVisitor> classVisitorList = null;
		for (TraversalUtilVisitor visitor : visitorList) {
			visitorClass = findClassParameter(visitor.getClass());
			if (visitorClass == null) {
				throw new IllegalArgumentException(
						"Can't identify the parameter class of the visitor "
								+ visitor.getClass().getName());
			}
			classVisitorList = ret.get(visitorClass);
			if (classVisitorList == null) {
				classVisitorList = new LinkedList<TraversalUtilVisitor>();
				ret.put(visitorClass, classVisitorList);
			}
			classVisitorList.add(visitor);
		}
		return ret;
	}

	@Override
	protected List<Object> apply(Object child, Object parent, List siblings) {
		for (final Entry<Class, List<TraversalUtilVisitor>> entrySet : visitorMap.entrySet()) {
			final Class currentClass = entrySet.getKey();
			if (currentClass.isAssignableFrom(child.getClass())) {
				List<TraversalUtilVisitor> classVisitorList = entrySet.getValue();
				if (null != classVisitorList) {
					for (TraversalUtilVisitor visitor : classVisitorList) {
						visitor.apply(child, parent, siblings);
					}
				}
			}
		}
		return null;
	}

}
