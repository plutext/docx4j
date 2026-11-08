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
package org.docx4j.jaxb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTColumns;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtContent;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A child given more than once where the schema allows one, read as Word reads it (CR-079 phases 2 and 3; WD REQ-322). JAXB keeps
 * the last copy of a single-valued child, so the first is lost; the unmarshal listener calls {@link #repeated} as each child is
 * completed, while its parent still holds the copy before it.
 * <ul>
 * <li>{@code w:body} in a {@code w:document}, and {@code w:sdtContent} in a control at any level: joined in order, the earlier's
 * content at the head of the later's. A joined body's final {@code w:sectPr} is {@link #merge merged}.</li>
 * <li>{@code w:txbxContent} in a VML box: joined in reverse, the later's content first (D6). In {@code wps:txbx} JAXB's last is
 * Word's, so nothing.</li>
 * <li>{@code w:pPr} in a paragraph, {@code w:rPr} in a run, {@code w:sectPr} in a body or a {@code w:pPr}, {@code w:tblPr} in a table,
 * and a child given twice in one parent, as the rounds sectpr-merge and one-parent-word-check measured them: {@code w:ind},
 * {@code w:spacing}, {@code w:numPr} and a paragraph mark's {@code w:rPr} in one {@code w:pPr}, {@code w:rFonts} in one {@code w:rPr},
 * {@code w:sectPr} in one {@code w:pPr}, and {@code w:pgSz}, {@code w:pgMar} and {@code w:cols} in one {@code w:sectPr} (sm-53, sm-70,
 * sm-72, sm-73, op-10 to op-21): the later applied to the earlier, as {@link #merge} has it, the children REQ-322 names merging in turn.
 * {@code w:jc}, {@code w:sz}, {@code w:b} and {@code w:vanish} need nothing: JAXB's last is Word's.</li>
 * </ul>
 * The main document part's alone, as every measurement is of {@code word/document.xml}.
 *
 * @since 17.3.6
 */
public final class RepeatedChildren {

	private static final Logger log = LoggerFactory.getLogger(RepeatedChildren.class);

	private RepeatedChildren() {}

	/**
	 * Called with each object the unmarshal completes and its parent. Where the parent already holds another of the target's kind,
	 * the two are joined or merged into the target, which JAXB then sets in place of the earlier.
	 *
	 * @return whether the target repeated one before it
	 */
	public static boolean repeated(Object target, Object parent) {
		if (parent == null) return false;
		if (target instanceof PPr) {
			if (parent instanceof P) return mergeInto(((P) parent).getPPr(), target);
		} else if (target instanceof RPr) {
			if (parent instanceof R) return mergeInto(((R) parent).getRPr(), target);
		} else if (target instanceof ParaRPr) {
			if (parent instanceof PPr) return mergeInto(((PPr) parent).getRPr(), target);
		} else if (target instanceof PPrBase.Ind) {
			if (parent instanceof PPrBase) return mergeInto(((PPrBase) parent).getInd(), target);
		} else if (target instanceof PPrBase.Spacing) {
			if (parent instanceof PPrBase) return mergeInto(((PPrBase) parent).getSpacing(), target);
		} else if (target instanceof PPrBase.NumPr) {
			if (parent instanceof PPrBase) return mergeInto(((PPrBase) parent).getNumPr(), target);
		} else if (target instanceof RFonts) {
			if (parent instanceof RPr) return mergeInto(((RPr) parent).getRFonts(), target);
		} else if (target instanceof SectPr) {
			if (parent instanceof Body) return mergeInto(((Body) parent).getSectPr(), target);
			if (parent instanceof PPr) return mergeInto(((PPr) parent).getSectPr(), target);
		} else if (target instanceof SectPr.PgSz) {
			if (parent instanceof SectPr) return mergeInto(((SectPr) parent).getPgSz(), target);
		} else if (target instanceof SectPr.PgMar) {
			if (parent instanceof SectPr) return mergeInto(((SectPr) parent).getPgMar(), target);
		} else if (target instanceof CTColumns) {
			if (parent instanceof SectPr) return mergeInto(((SectPr) parent).getCols(), target);
		} else if (target instanceof TblPr) {
			if (parent instanceof Tbl) return mergeInto(((Tbl) parent).getTblPr(), target);
		} else if (target instanceof Body) {
			if (parent instanceof Document) {
				Body earlier = ((Document) parent).getBody();
				if (earlier == null || earlier == target) return false;
				joinBodies(earlier, (Body) target);
				return true;
			}
		} else if (target instanceof SdtContent) {
			if (parent instanceof SdtElement) {
				SdtContent earlier = ((SdtElement) parent).getSdtContent();
				if (earlier == null || earlier == target) return false;
				moveContent(earlier.getContent(), ((SdtContent) target).getContent(), (Child) target, true);
				return true;
			}
		} else if (target instanceof CTTxbxContent) {
			if (parent instanceof org.docx4j.vml.CTTextbox) {
				CTTxbxContent earlier = ((org.docx4j.vml.CTTextbox) parent).getTxbxContent();
				if (earlier == null || earlier == target) return false;
				moveContent(earlier.getContent(), ((CTTxbxContent) target).getContent(), (Child) target, false);
				return true;
			}
		}
		return false;
	}

	private static boolean mergeInto(Object earlier, Object later) {
		if (earlier == null || earlier == later) return false;
		merge(earlier, later);
		return true;
	}

	/**
	 * Two bodies joined in order, the earlier's content at the head of the later's, and their final {@code w:sectPr} merged, a body
	 * with none passed over (WD REQ-322). For a second {@code w:body} in a {@code w:document}, and for {@code w:document}s joined in a
	 * branch at the root.
	 */
	public static void joinBodies(Body earlier, Body later) {
		moveContent(earlier.getContent(), later.getContent(), later, true);
		SectPr first = earlier.getSectPr(), last = later.getSectPr();
		if (first == null) return;
		if (last == null) {
			first.setParent(later);
			later.setSectPr(first);
		} else {
			merge(first, last);
		}
	}

	private static void moveContent(List<Object> from, List<Object> to, Child owner, boolean atHead) {
		for (Object o : from) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof Child) ((Child) u).setParent(owner);
		}
		List<Object> moved = new ArrayList<Object>(from);
		from.clear();
		if (atHead) to.addAll(0, moved); else to.addAll(moved);
	}

	/** The children merged one level in, as REQ-322 names them, a property container holding them merging too. */
	private static final Set<Class<?>> MERGED = new HashSet<Class<?>>(Arrays.asList(PPr.class, RPr.class, ParaRPr.class, SectPr.class,
			TblPr.class, PPrBase.Ind.class, PPrBase.Spacing.class, PPrBase.NumPr.class, RFonts.class, SectPr.PgSz.class, SectPr.PgMar.class,
			CTColumns.class));

	/** REQ-322 says nothing of it, and one file fits neither reading (sm-61): it stays as JAXB leaves it, the later's. */
	private static final Set<String> AS_JAXB_LEAVES = new HashSet<String>(Arrays.asList("TblW"));

	/**
	 * The later copy applied to the earlier, the result in the later (WD REQ-322): what the later states wins and what it doesn't
	 * state stays, for attributes and child elements alike, and the children REQ-322 names merge in turn. An on/off element with no
	 * {@code w:val} states on, and so wins. Only the earlier's {@code w:headerReference} and {@code w:footerReference} stand, any on
	 * the later being ignored, part and all, an earlier with none leaving none. {@code w:tblW} stays the later's.
	 */
	public static void merge(Object earlier, Object later) {
		if (earlier == null || later == null || earlier.getClass() != later.getClass()) return;
		for (Accessor a : accessors(later.getClass())) {
			try {
				Object mine = a.getter.invoke(later);
				Object theirs = a.getter.invoke(earlier);
				if (theirs == null) continue;
				if (mine == null) {
					if (theirs instanceof Child) ((Child) theirs).setParent(later);
					a.setter.invoke(later, theirs);
				} else if (MERGED.contains(mine.getClass())) {
					merge(theirs, mine);
				}
			} catch (ReflectiveOperationException e) {
				log.warn("Couldn't merge " + a.getter.getName() + " of a " + later.getClass().getSimpleName() + ": " + e);
			}
		}
		if (later instanceof SectPr) {
			List<org.docx4j.wml.CTRel> refs = ((SectPr) later).getEGHdrFtrReferences();
			refs.clear();
			for (org.docx4j.wml.CTRel r : ((SectPr) earlier).getEGHdrFtrReferences()) {
				r.setParent(later);
				refs.add(r);
			}
		}
	}

	private static final class Accessor {
		final Method getter, setter;

		Accessor(Method getter, Method setter) {
			this.getter = getter;
			this.setter = setter;
		}
	}

	private static final Map<Class<?>, List<Accessor>> ACCESSORS = new ConcurrentHashMap<Class<?>, List<Accessor>>();

	/** Each property with a public getter and setter of one type: its attributes and single children. A list has no setter, so isn't one. */
	private static List<Accessor> accessors(Class<?> c) {
		List<Accessor> found = ACCESSORS.get(c);
		if (found != null) return found;
		found = new ArrayList<Accessor>();
		for (Method get : c.getMethods()) {
			if (get.getParameterCount() != 0 || get.getDeclaringClass() == Object.class) continue;
			String name = get.getName();
			String property = name.startsWith("get") ? name.substring(3) : name.startsWith("is") ? name.substring(2) : null;
			if (property == null || property.isEmpty() || "Parent".equals(property) || "Class".equals(property) || AS_JAXB_LEAVES.contains(property)) continue;
			try {
				found.add(new Accessor(get, c.getMethod("set" + property, get.getReturnType())));
			} catch (NoSuchMethodException none) {
				// a list, or read-only
			}
		}
		ACCESSORS.put(c, found);
		return found;
	}
}
