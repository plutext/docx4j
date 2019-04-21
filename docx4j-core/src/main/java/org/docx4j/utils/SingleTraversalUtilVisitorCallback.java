package org.docx4j.utils;

import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** 
 * Use this if there is only a single object type (eg just P's)
 * you are interested in doing something with.
 * 
 * For an example of use, see sample ImageConvertEmbeddedToLinked
 * 
 * @author alberto */
public class SingleTraversalUtilVisitorCallback extends AbstractTraversalUtilVisitorCallback {
	
	protected static Logger log = LoggerFactory.getLogger(SingleTraversalUtilVisitorCallback.class);
	
	
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
		
		if (visitorClass==null) {			
			log.warn("visitorClass==null for some element with parent " + parent.getClass().getName() );
			//log.warn(XmlUtils.marshaltoString(parent));
		} else if (child==null) {
            if(log.isWarnEnabled()) {
                log.warn("child==null for some element with parent " + parent.getClass().getName());
                // eg <w:t/>
                log.warn(XmlUtils.marshaltoString(parent));
            }
		} else if (visitorClass.isAssignableFrom(child.getClass())) {
			visitor.apply(child, parent, siblings);
		}
		return null;
	}

}
