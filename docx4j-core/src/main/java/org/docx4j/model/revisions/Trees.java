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
package org.docx4j.model.revisions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.McSplice;
import org.docx4j.mce.AlternateContent;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathPara;
import org.docx4j.math.CTR;
import org.docx4j.wml.Body;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTCustomXmlBlock;
import org.docx4j.wml.CTCustomXmlCell;
import org.docx4j.wml.CTCustomXmlRow;
import org.docx4j.wml.CTCustomXmlRun;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTMathRunTrackChange;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Comments;
import org.docx4j.wml.DelText;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.jvnet.jaxb.lang.Child;

/**
 * The JAXB tree as the revision code walks it (CR-043): the live lists that hold run-level and
 * block-level content, the names of their members, their text, and a reflective walk of
 * everything under a node for what a removal takes with it.
 */
final class Trees {

	private Trees() {}

	static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	static final String R_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

	/** What a list member is: a {@code JAXBElement}'s value, or the member itself. */
	static Object unwrap(Object item) {
		return item instanceof JAXBElement ? ((JAXBElement<?>) item).getValue() : item;
	}

	private static final Map<Class<?>, String> ROOT_NAMES = new ConcurrentHashMap<>();

	/** A list member's element name: its {@code JAXBElement}'s, else its class's {@code @XmlRootElement}. */
	static String name(Object item) {
		if (item instanceof JAXBElement) return ((JAXBElement<?>) item).getName().getLocalPart();
		if (item == null) return "";
		return ROOT_NAMES.computeIfAbsent(item.getClass(), c -> {
			XmlRootElement root = c.getAnnotation(XmlRootElement.class);
			return root == null ? "" : root.name();
		});
	}

	/** Where {@code item} stands in {@code list}, by identity (the list's own indexOf compares values too). */
	static int indexOf(List<Object> list, Object item) {
		if (list == null) return -1;
		for (int i = 0; i < list.size(); i++) if (list.get(i) == item) return i;
		return -1;
	}

	/** Sets the parent pointer of a list member (its value when it is a {@code JAXBElement}). */
	static void setParent(Object item, Object parent) {
		Object v = unwrap(item);
		if (v instanceof Child) ((Child) v).setParent(parent);
	}

	static void setParents(Collection<Object> items, Object parent) {
		for (Object item : items) setParent(item, parent);
	}

	/** The parent pointer of a list member, or null. */
	static Object parentOf(Object item) {
		Object v = unwrap(item);
		return v instanceof Child ? ((Child) v).getParent() : null;
	}

	// --- the revision holders --------------------------------------------------------------------

	enum Revision { INS, DEL, MOVE_FROM, MOVE_TO }

	/** The run-level revision a list member is - {@code w:ins}, {@code w:del}, {@code w:moveFrom}, {@code w:moveTo} - or null. */
	static Revision revisionOf(Object item) {
		Object v = unwrap(item);
		if (v instanceof RunIns) return Revision.INS;
		if (v instanceof RunDel) return Revision.DEL;
		if (v instanceof RunTrackChange) {
			String name = name(item);
			if ("moveFrom".equals(name)) return Revision.MOVE_FROM;
			if ("moveTo".equals(name)) return Revision.MOVE_TO;
		}
		return null;
	}

	// --- live content lists ----------------------------------------------------------------------

	/**
	 * The live list of run-level content a run holder keeps: a paragraph, a hyperlink, an inline
	 * content control, a smart tag, custom XML, a simple field, {@code w:dir}/{@code w:bdo}, and the
	 * run-level revisions; null for anything else.
	 */
	static List<Object> runItems(Object v) {
		if (v instanceof P) return ((P) v).getContent();
		if (v instanceof RunIns) return ((RunIns) v).getCustomXmlOrSmartTagOrSdt();
		if (v instanceof RunDel) return ((RunDel) v).getCustomXmlOrSmartTagOrSdt();
		if (v instanceof RunTrackChange) return ((RunTrackChange) v).getAccOrBarOrBox();
		if (v instanceof P.Hyperlink) return ((P.Hyperlink) v).getContent();
		if (v instanceof org.docx4j.wml.SdtRun) return sdtItems((SdtElement) v);
		if (v instanceof CTSmartTagRun) return ((CTSmartTagRun) v).getContent();
		if (v instanceof CTCustomXmlRun) return ((CTCustomXmlRun) v).getContent();
		if (v instanceof CTSimpleField) return ((CTSimpleField) v).getContent();
		if (v instanceof P.Bdo) return ((P.Bdo) v).getContent();
		if (v instanceof P.Dir) return ((P.Dir) v).getContent();
		// a paragraph-level mc:AlternateContent: the branch Word reads (CR-051 section 10.3, item 3;
		// Lister lists the other branches' changes as twins)
		if (v instanceof AlternateContent) return branchItems((AlternateContent) v);
		return null;
	}

	/** The content of the branch the text model reads ({@link McPurpose#TEXT}), the live list; null where there is none. */
	static List<Object> branchItems(AlternateContent ac) {
		Object branch = McSelection.selectedBranch(McPurpose.TEXT, ac);
		return branch == null ? null : McSplice.contentOf(branch);
	}

	/** The last paragraph's {@code w14:paraId} in this content, looking into a branch (CR-051 section 10.3, item 8); null where none has one. */
	static String lastParaId(List<Object> items) {
		String id = null;
		for (Object o : items) {
			Object v = unwrap(o);
			if (v instanceof P && ((P) v).getParaId() != null) id = ((P) v).getParaId();
			else if (v instanceof AlternateContent) {
				List<Object> branch = branchItems((AlternateContent) v);
				String inner = branch == null ? null : lastParaId(branch);
				if (inner != null) id = inner;
			}
		}
		return id;
	}

	/**
	 * The live list of block-level content (or rows, or cells) a container keeps: a body, a header,
	 * a footer, a note, a comment, a cell, a text box, a block, row or cell content control or custom
	 * XML element, a table (its rows) and a row (its cells); null for anything else.
	 */
	static List<Object> blockItems(Object v) {
		if (v instanceof Body) return ((Body) v).getContent();
		if (v instanceof Hdr) return ((Hdr) v).getContent();
		if (v instanceof Ftr) return ((Ftr) v).getContent();
		if (v instanceof CTFtnEdn) return ((CTFtnEdn) v).getContent();
		if (v instanceof Comments.Comment) return ((Comments.Comment) v).getContent();
		if (v instanceof Tc) return ((Tc) v).getContent();
		if (v instanceof Tbl) return ((Tbl) v).getContent();
		if (v instanceof Tr) return ((Tr) v).getContent();
		if (v instanceof CTTxbxContent) return ((CTTxbxContent) v).getContent();
		if (v instanceof org.docx4j.wml.SdtBlock || v instanceof org.docx4j.wml.CTSdtRow
				|| v instanceof org.docx4j.wml.CTSdtCell) return sdtItems((SdtElement) v);
		if (v instanceof CTCustomXmlBlock) return ((CTCustomXmlBlock) v).getContent();
		if (v instanceof CTCustomXmlRow) return ((CTCustomXmlRow) v).getContent();
		if (v instanceof CTCustomXmlCell) return ((CTCustomXmlCell) v).getContent();
		// a block-, row- or cell-level mc:AlternateContent: the branch Word reads (CR-051 section 10.3, item 3)
		if (v instanceof AlternateContent) return branchItems((AlternateContent) v);
		return null;
	}

	/** Either kind of list: what a container or a run holder keeps. */
	static List<Object> children(Object v) {
		List<Object> block = blockItems(v);
		return block != null ? block : runItems(v);
	}

	private static List<Object> sdtItems(SdtElement sdt) {
		return sdt.getSdtContent() == null ? null : sdt.getSdtContent().getContent();
	}

	/** The object whose list is {@code items} for a holder (the {@code w:sdtContent} of a control), for parent pointers. */
	static Object listOwner(Object v) {
		if (v instanceof SdtElement) return ((SdtElement) v).getSdtContent();
		return v;
	}

	/** A content control or custom XML element: what Word's {@code w:customXmlInsRangeStart} form marks inserted. */
	static boolean isHolder(Object v) {
		return v instanceof SdtElement || v instanceof CTCustomXmlRun || v instanceof CTCustomXmlBlock
				|| v instanceof CTCustomXmlRow || v instanceof CTCustomXmlCell;
	}

	// --- text ------------------------------------------------------------------------------------

	enum View {
		/** The document with every change accepted: inserted text counts, deleted text does not. */
		ACCEPTED,
		/** The document with every change rejected. */
		ORIGINAL
	}

	/** The text of a run's item, Office JS's way; null for an item that is no text. */
	static String itemText(Object item) {
		Object v = unwrap(item);
		String name = name(item);
		if (v instanceof Text && ("t".equals(name) || "delText".equals(name))) {
			String s = ((Text) v).getValue();
			return s == null ? "" : s;
		}
		if (v instanceof DelText) {
			String s = ((DelText) v).getValue();
			return s == null ? "" : s;
		}
		if (v instanceof R.Tab || "tab".equals(name)) return "\t";
		if (v instanceof Br || "br".equals(name) || "cr".equals(name)) return "\n";
		if ("noBreakHyphen".equals(name)) return "‑";
		if ("softHyphen".equals(name)) return "­";
		if (v instanceof R.Sym) {
			String code = ((R.Sym) v).getChar();
			if (code != null && code.matches("[0-9A-Fa-f]{4}")) return String.valueOf((char) Integer.parseInt(code, 16));
			return "";
		}
		if (v instanceof org.docx4j.math.CTText) {
			String s = ((org.docx4j.math.CTText) v).getValue();
			return s == null ? "" : s;
		}
		return null;
	}

	/** True when a run (or math run) holds text: a comment reference alone does not. */
	static boolean hasText(R run) {
		for (Object item : run.getContent()) if (itemText(item) != null) return true;
		return false;
	}

	/** The text of run-level content in a view: runs, math, and what run holders hold. */
	static String text(Object item, View view) {
		StringBuilder out = new StringBuilder();
		appendText(item, view, out);
		return out.toString();
	}

	static void appendText(Object item, View view, StringBuilder out) {
		Object v = unwrap(item);
		if (v instanceof R) {
			for (Object o : ((R) v).getContent()) {
				String s = itemText(o);
				if (s != null) out.append(s);
			}
			return;
		}
		if (v instanceof CTR) {
			for (Object o : ((CTR) v).getContent()) {
				Object ov = unwrap(o);
				if (ov instanceof CTMathRunTrackChange) {
					boolean ins = "ins".equals(name(o));
					if (ins == (view == View.ACCEPTED)) for (Object inner : ((CTMathRunTrackChange) ov).getRest()) appendText(inner, view, out);
					continue;
				}
				String s = itemText(o);
				if (s != null) out.append(s);
			}
			return;
		}
		if (v instanceof CTOMath || v instanceof CTOMathPara || (v != null && v.getClass().getPackage() == CTR.class.getPackage())) {
			walk(v, new Visitor() {
				@Override public boolean enter(Object value, Object listItem) {
					if (value != v && value instanceof CTR) { appendText(value, view, out); return false; }
					if (value instanceof org.docx4j.math.CTCtrlPr) return false;
					return true;
				}
			});
			return;
		}
		Revision revision = revisionOf(item);
		if (revision != null) {
			boolean shown = view == View.ACCEPTED
					? revision == Revision.INS || revision == Revision.MOVE_TO
					: revision == Revision.DEL || revision == Revision.MOVE_FROM;
			if (!shown) return;
		}
		List<Object> items = runItems(v);
		if (items != null) for (Object o : items) appendText(o, view, out);
	}

	/** A paragraph's text in a view. */
	static String paragraphText(P p, View view) {
		return text(p, view);
	}

	// --- the reflective walk ---------------------------------------------------------------------

	/** What {@link #walk} reports. */
	abstract static class Visitor {
		/**
		 * A JAXB object reached; false to skip what is under it. {@code item} is how it stands in its
		 * list or field: the {@code JAXBElement} around it, or itself.
		 */
		boolean enter(Object value, Object item) { return true; }
		/** A relationship id in an {@code r:} attribute. */
		void relationship(String id) {}
	}

	private static final Map<Class<?>, Field[]> FIELDS = new ConcurrentHashMap<>();
	private static final Field[] NONE = new Field[0];

	/**
	 * Every JAXB object under {@code root} (itself included), in document order as far as the
	 * classes declare their fields in schema order, which XJC's do; and the {@code r:} relationship
	 * ids on them and on DOM content held as {@code xsd:any}.
	 */
	static void walk(Object root, Visitor visitor) {
		visit(root, root, visitor, 0);
	}

	private static void visit(Object item, Object value, Visitor visitor, int depth) {
		if (value == null || depth > 400) return;
		if (value instanceof JAXBElement) { visit(value, ((JAXBElement<?>) value).getValue(), visitor, depth + 1); return; }
		if (value instanceof List) {
			for (Object o : new ArrayList<Object>((List<?>) value)) visit(o, unwrap(o), visitor, depth + 1);
			return;
		}
		if (value instanceof org.w3c.dom.Node) { visitDom((org.w3c.dom.Node) value, visitor); return; }
		if (!isJaxb(value.getClass())) return;
		if (!visitor.enter(value, item)) return;
		for (Field f : fieldsOf(value.getClass())) {
			Object fv;
			try {
				fv = f.get(value);
			} catch (IllegalAccessException e) {
				continue;
			}
			if (fv == null) continue;
			if (fv instanceof String) {
				XmlAttribute a = f.getAnnotation(XmlAttribute.class);
				if (a != null && R_NS.equals(a.namespace())) visitor.relationship((String) fv);
				continue;
			}
			visit(fv, fv, visitor, depth + 1);
		}
	}

	private static void visitDom(org.w3c.dom.Node node, Visitor visitor) {
		if (node.getAttributes() != null) {
			for (int i = 0; i < node.getAttributes().getLength(); i++) {
				org.w3c.dom.Node a = node.getAttributes().item(i);
				if (R_NS.equals(a.getNamespaceURI())) visitor.relationship(a.getNodeValue());
			}
		}
		for (org.w3c.dom.Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) visitDom(c, visitor);
		}
	}

	private static boolean isJaxb(Class<?> c) {
		Package p = c.getPackage();
		return p != null && (p.getName().startsWith("org.docx4j.") || p.getName().startsWith("org.pptx4j.")
				|| p.getName().startsWith("org.xlsx4j.") || p.getName().startsWith("org.opendope."));
	}

	static Field[] fieldsOf(Class<?> c) {
		return FIELDS.computeIfAbsent(c, k -> {
			List<Class<?>> chain = new ArrayList<>();
			for (Class<?> x = k; x != null && x != Object.class; x = x.getSuperclass()) chain.add(0, x);
			List<Field> out = new ArrayList<>();
			for (Class<?> x : chain) {
				for (Field f : x.getDeclaredFields()) {
					int m = f.getModifiers();
					if (Modifier.isStatic(m) || Modifier.isTransient(m)) continue;
					if ("parent".equals(f.getName())) continue;
					Class<?> t = f.getType();
					if (t.isPrimitive() || Number.class.isAssignableFrom(t) || t == Boolean.class || t.isEnum()
							|| XMLGregorianCalendar.class.isAssignableFrom(t) || t == QName.class || t == byte[].class) continue;
					try {
						f.setAccessible(true);
					} catch (RuntimeException e) {
						continue; // a module that does not open the package: what is in it is not walked
					}
					out.add(f);
				}
			}
			return out.isEmpty() ? NONE : out.toArray(NONE);
		});
	}

	/** True when a JAXB object holds nothing: every member null, or an empty list. */
	static boolean isEmpty(Object value) {
		if (value == null) return true;
		for (Field f : fieldsOf(value.getClass())) {
			Object fv;
			try {
				fv = f.get(value);
			} catch (IllegalAccessException e) {
				return false;
			}
			if (fv == null) continue;
			if (fv instanceof List && ((List<?>) fv).isEmpty()) continue;
			return false;
		}
		// members the walk skips (numbers, booleans, enums) count too
		for (Class<?> x = value.getClass(); x != null && x != Object.class; x = x.getSuperclass()) {
			for (Field f : x.getDeclaredFields()) {
				int m = f.getModifiers();
				if (Modifier.isStatic(m) || Modifier.isTransient(m) || "parent".equals(f.getName())) continue;
				Class<?> t = f.getType();
				if (t.isPrimitive()) continue;
				if (!(Number.class.isAssignableFrom(t) || t == Boolean.class || t.isEnum()
						|| XMLGregorianCalendar.class.isAssignableFrom(t))) continue;
				try {
					f.setAccessible(true);
					if (f.get(value) != null) return false;
				} catch (RuntimeException | IllegalAccessException e) {
					return false;
				}
			}
		}
		return true;
	}
}
