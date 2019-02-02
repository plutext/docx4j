package org.docx4j.org.apache.xml.serializer;

import java.lang.reflect.InvocationTargetException;

//import org.apache.xml.serializer.NamespaceMappings.MappingRecord;
import org.xml.sax.ContentHandler;

public class NamespaceMappingsAccessor {
	
	public static void popNamespaces(NamespaceMappings namespaceMappings, int elemDepth, ContentHandler saxHandler) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		java.lang.reflect.Method popNamespaces = NamespaceMappings.class.getDeclaredMethod("popNamespaces", int.class, ContentHandler.class);
		popNamespaces.setAccessible(true);
		popNamespaces.invoke(namespaceMappings, elemDepth, saxHandler);
		
	}

	public static boolean popNamespace(NamespaceMappings namespaceMappings, String prefix) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		java.lang.reflect.Method popNamespace = NamespaceMappings.class.getDeclaredMethod("popNamespace", String.class);
		popNamespace.setAccessible(true);
		return (Boolean) popNamespace.invoke(namespaceMappings, prefix);
		
	}
	
	public static void reset(NamespaceMappings namespaceMappings) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {

		java.lang.reflect.Method reset = NamespaceMappings.class.getDeclaredMethod("reset");
		reset.setAccessible(true);
		reset.invoke(namespaceMappings);
		
	}
	
//	public static Docx4jMappingRecord getMappingFromPrefix(NamespaceMappings namespaceMappings, String prefix) {
//
//		java.lang.reflect.Method getMappingFromPrefix = NamespaceMappings.class.getDeclaredMethod("getMappingFromPrefix", String.class);
//		getMappingFromPrefix.setAccessible(true);
//		return (MappingRecord) getMappingFromPrefix.invoke(namespaceMappings, prefix);
//		
//	}
	
}
