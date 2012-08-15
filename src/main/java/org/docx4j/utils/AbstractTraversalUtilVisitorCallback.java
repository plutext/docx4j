package org.docx4j.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;

/** @author alberto */
public abstract class AbstractTraversalUtilVisitorCallback extends TraversalUtil.CallbackImpl {
	
	/**
	 * Get the actual type arguments a child class has used to extend a generic base class.
	 * based on http://www.artima.com/weblogs/viewpost.jsp?thread=208860
	 */
	protected Class findClassParameter(Class childClass) {
    Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
    Type type = childClass;
	    // start walking up the inheritance hierarchy until we hit baseClass
	    while (! getTypeClass(type).equals(TraversalUtilVisitor.class)) {
	    	if (type instanceof Class) {
	    		// there is no useful information for us in raw types, so just keep going.
	    		type = ((Class) type).getGenericSuperclass();
	    	}
	    	else {
	    		ParameterizedType parameterizedType = (ParameterizedType) type;
	    		Class<?> rawType = (Class)parameterizedType.getRawType();
		        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
		        for (int i = 0; i < actualTypeArguments.length; i++) {
		          resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
		        }
		  
		        if (!rawType.equals(TraversalUtilVisitor.class)) {
		          type = rawType.getGenericSuperclass();
		        }
	    	}
	    }
	    
	    // finally, for each actual type argument provided to baseClass, determine (if possible)
	    // the raw class for that type argument.
	    Type baseType =
	    	(type instanceof Class ?
	    		((Class)type).getTypeParameters()[0] :
	    		((ParameterizedType)type).getActualTypeArguments()[0]);
	    
	    // resolve types by chasing down type variables.
	    while (resolvedTypes.containsKey(baseType)) {
	    	baseType = resolvedTypes.get(baseType);
	    }
	    return getTypeClass(baseType);
	}

    
	protected Class getTypeClass(Type type) {
    	return (type instanceof Class ?
    			(Class)type :
    			(type instanceof ParameterizedType ?
    			 getTypeClass(((ParameterizedType) type).getRawType()) : null));
	}  

	// Depth first
	@Override
	public void walkJAXBElements(Object parent) {
		List children = getChildren(parent);
		if (children != null) {
			for (Object o : children) {
				// if its wrapped in javax.xml.bind.JAXBElement, get its
				// value; this is ok, provided the results of the Callback
				// won't be marshalled
				o = XmlUtils.unwrap(o);
				this.apply(o, parent, children);
				if (this.shouldTraverse(o)) {
					walkJAXBElements(o);
				}
			}
		}
	}

	@Override
	public final List<Object> apply(Object o) {
		throw new UnsupportedOperationException("Invalid apply method - Abstract traversal util should use apply(Object child, Object parent, List siblings)");
	}
	
	protected abstract List<Object> apply(Object child, Object parent, List children);

}
