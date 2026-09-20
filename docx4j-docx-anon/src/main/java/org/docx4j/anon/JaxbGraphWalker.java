/*
 *  Copyright 2026, Plutext Pty Ltd.
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
package org.docx4j.anon;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visits every object reachable from a JAXB root through its public getters:
 * lists, JAXBElement wrappers, and every generated class (org.docx4j.*,
 * org.pptx4j.*, org.xlsx4j.*, org.glox4j.*, org.opendope.*).
 * <p>
 * The anonymiser uses this rather than {@link org.docx4j.TraversalUtil} because
 * the guarantee (CR-019) is "no text from the original in any text-bearing
 * part", and TraversalUtil's children rules are written for the content tree
 * (paragraphs, runs, tables, the drawings it knows): they do not reach
 * paragraph and run properties (where w:rPrChange and w:pPrChange carry
 * authors), settings, chart caches, diagram data, or any DrawingML text
 * TraversalUtil's handleGraphicData does not list. A reflective walk reaches
 * all of it, at the cost of some speed, which the anonymiser can afford.
 * <p>
 * Pre-order: a visitor sees a parent before its children, and sees each object
 * once (an identity set guards against cycles and against the convenience
 * getters which return an object also held in a list, such as
 * {@code SdtPr.getTag()}). DOM nodes ({@code xs:any} content) are handed to the
 * visitor and not descended into; the visitor decides what to do with them.
 *
 * @since 17.2.0
 */
public class JaxbGraphWalker {

	private static final Logger log = LoggerFactory.getLogger(JaxbGraphWalker.class);

	/** What the visitor wants done with the object it was just shown. */
	public enum Action {
		/** visit its children */
		CONTINUE,
		/** do not visit its children */
		SKIP_CHILDREN,
		/**
		 * remove it from its parent: from the list it sits in, or by invoking the
		 * setter matching the getter it came from with null; its children are not visited
		 */
		REMOVE,
		/**
		 * replace it in the list it sits in with {@link Visitor#replacement()} (when it
		 * came from a getter, it is set to null instead); its children are not visited
		 */
		REPLACE
	}

	public interface Visitor {
		/**
		 * @param o       the object, unwrapped from any JAXBElement
		 * @param wrapper the JAXBElement it came wrapped in, or null; its name tells
		 *                w:instrText from w:t (both are wml Text)
		 */
		Action visit(Object o, JAXBElement<?> wrapper);

		/** The object to put in place of the one just answered with {@link Action#REPLACE}. */
		default Object replacement() {
			return null;
		}
	}

	private final Visitor visitor;
	private final Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

	public JaxbGraphWalker(Visitor visitor) {
		this.visitor = visitor;
	}

	/** Walks from root (the root itself is visited too). */
	public void walk(Object root) {
		if (root == null) return;
		Object unwrapped = root;
		JAXBElement<?> wrapper = null;
		if (root instanceof JAXBElement) {
			wrapper = (JAXBElement<?>) root;
			unwrapped = wrapper.getValue();
			if (unwrapped == null) return;
		}
		Action a = visitUnseen(unwrapped, wrapper);
		if (a == Action.CONTINUE) {
			walkChildren(unwrapped);
		} else if (a == Action.REMOVE || a == Action.REPLACE) {
			log.warn(a + " of a root object is ignored: " + unwrapped.getClass().getName());
		}
	}

	private Action visitUnseen(Object o, JAXBElement<?> wrapper) {
		if (!seen.add(o)) return Action.SKIP_CHILDREN;
		return visitor.visit(o, wrapper);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void walkChildren(Object parent) {

		if (parent instanceof List) {
			List list = (List) parent;
			// iterate a snapshot: the visitor may ask for removals
			for (Object child : new ArrayList<Object>(list)) {
				walkListChild(list, child);
			}
			return;
		}

		if (!isModelObject(parent)) return;

		for (Method getter : accessors(parent.getClass())) {
			Object value;
			try {
				value = getter.invoke(parent);
			} catch (Exception e) {
				log.debug(getter + " threw " + e);
				continue;
			}
			if (value == null) continue;

			if (value instanceof List) {
				List list = (List) value;
				for (Object child : new ArrayList<Object>(list)) {
					walkListChild(list, child);
				}
			} else {
				Object unwrapped = value;
				JAXBElement<?> wrapper = null;
				if (value instanceof JAXBElement) {
					wrapper = (JAXBElement<?>) value;
					unwrapped = wrapper.getValue();
					if (unwrapped == null) continue;
				}
				if (!isVisitable(unwrapped)) continue;
				Action a = visitUnseen(unwrapped, wrapper);
				if (a == Action.CONTINUE) {
					walkChildren(unwrapped);
				} else if (a == Action.REMOVE || a == Action.REPLACE) {
					setNull(parent, getter);
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private void walkListChild(List list, Object child) {
		if (child == null) return;
		Object unwrapped = child;
		JAXBElement<?> wrapper = null;
		if (child instanceof JAXBElement) {
			wrapper = (JAXBElement<?>) child;
			unwrapped = wrapper.getValue();
			if (unwrapped == null) return;
		}
		if (!isVisitable(unwrapped)) return;
		Action a = visitUnseen(unwrapped, wrapper);
		if (a == Action.CONTINUE) {
			walkChildren(unwrapped);
		} else if (a == Action.REMOVE) {
			removeByIdentity(list, child);
		} else if (a == Action.REPLACE) {
			Object replacement = visitor.replacement();
			if (replacement == null) {
				removeByIdentity(list, child);
			} else {
				replaceByIdentity(list, child, replacement);
				seen.add(replacement instanceof JAXBElement ? ((JAXBElement<?>) replacement).getValue() : replacement);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void replaceByIdentity(List list, Object child, Object replacement) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == child) {
				list.set(i, replacement);
				return;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static void removeByIdentity(List list, Object child) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == child) {
				list.remove(i);
				return;
			}
		}
	}

	private static void setNull(Object parent, Method getter) {
		String setterName = "set" + getter.getName().substring(3);
		try {
			Method setter = parent.getClass().getMethod(setterName, getter.getReturnType());
			setter.invoke(parent, new Object[] { null });
		} catch (Exception e) {
			log.warn("cannot null " + getter.getName() + " on " + parent.getClass().getName() + ": " + e);
		}
	}

	/** Objects the visitor is shown: model objects, lists, DOM nodes, strings in mixed content. */
	private static boolean isVisitable(Object o) {
		return isModelObject(o)
				|| o instanceof List
				|| o instanceof org.w3c.dom.Node
				|| o instanceof String;
	}

	/** A generated (or hand-written) content-model class: recurse into its getters. */
	static boolean isModelObject(Object o) {
		if (o == null) return false;
		String p = o.getClass().getName();
		return (p.startsWith("org.docx4j.")
					|| p.startsWith("org.pptx4j.")
					|| p.startsWith("org.xlsx4j.")
					|| p.startsWith("org.glox4j.")
					|| p.startsWith("org.opendope."))
				&& !p.startsWith("org.docx4j.openpackaging.")
				&& !p.startsWith("org.docx4j.anon.")
				&& !p.endsWith("ObjectFactory")
				&& !(o instanceof Enum);
	}

	private static final Map<Class<?>, List<Method>> ACCESSORS = new ConcurrentHashMap<Class<?>, List<Method>>();

	/**
	 * The public no-arg getters of a class which can lead to further model objects
	 * (anything but scalars), computed once per class.
	 */
	private static List<Method> accessors(Class<?> c) {
		List<Method> cached = ACCESSORS.get(c);
		if (cached != null) return cached;

		// lists first: a convenience getter (SdtPr.getTag()) returns an object which also
		// sits in a list, and a removal must come off the list, not through a setter the
		// convenience getter may not have
		List<Method> lists = new ArrayList<Method>();
		List<Method> singles = new ArrayList<Method>();
		for (Method m : c.getMethods()) {
			if (m.getParameterCount() != 0) continue;
			if (Modifier.isStatic(m.getModifiers())) continue;
			if (m.isBridge() || m.isSynthetic()) continue;
			if (m.getDeclaringClass() == Object.class) continue;
			String name = m.getName();
			if (!name.startsWith("get") || name.length() < 4) continue;
			if (name.equals("getParent") || name.equals("getClass")) continue;
			Class<?> rt = m.getReturnType();
			if (isScalar(rt)) continue;
			if (List.class.isAssignableFrom(rt)) {
				lists.add(m);
			} else {
				singles.add(m);
			}
		}
		List<Method> result = new ArrayList<Method>(lists);
		result.addAll(singles);
		ACCESSORS.put(c, result);
		return result;
	}

	private static boolean isScalar(Class<?> rt) {
		return rt.isPrimitive()
				|| rt == String.class
				|| rt.isEnum()
				|| Number.class.isAssignableFrom(rt)
				|| rt == Boolean.class
				|| rt == Character.class
				|| rt == byte[].class
				|| rt.isArray()
				|| javax.xml.datatype.XMLGregorianCalendar.class.isAssignableFrom(rt)
				|| javax.xml.datatype.Duration.class.isAssignableFrom(rt)
				|| rt == javax.xml.namespace.QName.class
				|| rt == java.util.Map.class;
	}

}
