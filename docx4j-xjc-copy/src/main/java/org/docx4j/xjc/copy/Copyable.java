/* Copyright © 2026, Oracle and/or its affiliates. */
package org.docx4j.xjc.copy;

/**
 * Interface implemented by XJC-generated classes to provide a fast deep-copy
 * without marshalling/unmarshalling.
 */
public interface Copyable {

	/**
	 * Deep-copy this object and its descendants.
	 *
	 * <p>The returned copy and its descendants will have all parent pointers set.
	 */
	Object copy();

	/**
	 * Deep-copy this object and its descendants to the provided target object.
	 *
	 * <p>The returned copy and its descendants will have all parent pointers set.
	 */
	Object copyTo(Object target);
}
