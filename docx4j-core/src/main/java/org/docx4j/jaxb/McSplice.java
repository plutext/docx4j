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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTCustomXmlBlock;
import org.docx4j.wml.CTCustomXmlCell;
import org.docx4j.wml.CTCustomXmlRow;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTSdtContentCell;
import org.docx4j.wml.CTSdtContentRow;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The splice of CR-051 section 3.2: each {@code mc:AlternateContent} given is replaced, at its
 * index in its live parent's content list, by the content of the branch a rule selects, and a
 * record of it is returned ({@link ResolvedAlternateContent}).  One implementation, two callers:
 * the parts at load, with {@link #WORD_READS} (the branch Word reads, REQ-305's element kept
 * whole); and the exporters' preprocessing step on their copy, with the drawing rule (CR-051 D6).
 *
 * <p>The elements are applied outermost first, through the live parent.  An element inside a
 * branch this splice discarded, or inside an element it kept whole, is skipped: it stays whole in
 * there, so the record still holds its branches.  An element with no branch at all is removed
 * and recorded.  Content moved out of a branch is re-parented by its new list
 * ({@code ArrayListDocx4j}), so a nested element already answers with its new parent when its
 * turn comes.</p>
 *
 * <p>Scope: the containers of CR-051 section 3.1, which the schema admits the element in since
 * 17.3.4: the block containers ({@link #isBlockContainer}), and with D2 (a) the row and cell
 * containers ({@link #isRowContainer}, {@link #isCellContainer}).  Run and paragraph content is
 * never in scope.</p>
 *
 * @since 17.3.4 (CR-051 phase 1)
 */
public final class McSplice {

	private static final Logger log = LoggerFactory.getLogger(McSplice.class);

	private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	private McSplice() {}

	/** Which branch's content replaces an element. */
	public interface Rule {
		/**
		 * The {@link AlternateContent.Choice} or {@link AlternateContent.Fallback} whose content
		 * replaces the element, or null to keep the element whole.  Not asked for an element with
		 * no branch at all, which the splice removes.
		 */
		Object select(AlternateContent ac);
	}

	/**
	 * The load-time rule (CR-051 section 3.2): the branch Word reads, which is
	 * {@link McSelection#selectedBranch(McPurpose, AlternateContent) McPurpose.TEXT}'s (the first
	 * Choice docx4j understands, else the Fallback); an element with no understood Choice and no
	 * Fallback is kept whole (WD REQ-305), since the selection would promote its first Choice.
	 */
	public static final Rule WORD_READS = new Rule() {
		@Override
		public Object select(AlternateContent ac) {
			boolean understood = false;
			for (AlternateContent.Choice choice : ac.getChoice()) {
				if (McSelection.understands(choice)) {
					understood = true;
					break;
				}
			}
			if (!understood && ac.getFallback() == null) return null;
			return McSelection.selectedBranch(McPurpose.TEXT, ac);
		}
	};

	// --- scope ---------------------------------------------------------------------------------------

	/** A block container (CR-051 section 3.1's table): the body (a glossary entry's too), a header or footer, a cell, a note, a comment, a text box, a block-level content control's or custom XML element's content. */
	public static boolean isBlockContainer(Object o) {
		o = XmlUtils.unwrap(o);
		return o instanceof Body || o instanceof Hdr || o instanceof Ftr || o instanceof Tc || o instanceof CTFtnEdn
				|| o instanceof Comments.Comment || o instanceof CTTxbxContent || o instanceof SdtContentBlock
				|| o instanceof CTCustomXmlBlock;
	}

	/** A row container (D2 (a)): a table, a row-level content control's or custom XML element's content. */
	public static boolean isRowContainer(Object o) {
		o = XmlUtils.unwrap(o);
		return o instanceof Tbl || o instanceof CTSdtContentRow || o instanceof CTCustomXmlRow;
	}

	/** A cell container (D2 (a)): a row, a cell-level content control's or custom XML element's content. */
	public static boolean isCellContainer(Object o) {
		o = XmlUtils.unwrap(o);
		return o instanceof Tr || o instanceof CTSdtContentCell || o instanceof CTCustomXmlCell;
	}

	/** Whether an element whose parent this is lies in the splice's scope. */
	public static boolean inScope(Object container) {
		return isBlockContainer(container) || isRowContainer(container) || isCellContainer(container);
	}

	/**
	 * The container an element stands in once the elements around it are resolved: its parent,
	 * or where that is a {@link AlternateContent.Choice} or {@link AlternateContent.Fallback}, the
	 * first ancestor that is not a Choice, a Fallback or an {@link AlternateContent}.  An element
	 * directly inside another's branch is in scope when that container is (CR-051 section 3.2,
	 * nested elements): once the outer element is spliced, the inner one stands in the container
	 * and is spliced there in its turn, or stays whole inside a discarded branch.
	 *
	 * @since 17.3.4 (CR-051 phase 1, stage B)
	 */
	public static Object effectiveContainer(Object o) {
		Object p = o instanceof Child ? ((Child) o).getParent() : null;
		for (int i = 0; i < 256 && p != null; i++) {
			if (!(p instanceof AlternateContent.Choice || p instanceof AlternateContent.Fallback || p instanceof AlternateContent)) return p;
			p = p instanceof Child ? ((Child) p).getParent() : null;
		}
		return p;
	}

	/** Whether the object lies inside any {@code mc:AlternateContent}, going up its parents. */
	public static boolean insideAlternateContent(Object o) {
		Object p = o instanceof Child ? ((Child) o).getParent() : null;
		for (int i = 0; i < 256 && p != null; i++) {
			if (p instanceof AlternateContent) return true;
			p = p instanceof Child ? ((Child) p).getParent() : null;
		}
		return false;
	}

	// --- the splice ----------------------------------------------------------------------------------

	/**
	 * Splices these elements by this rule, outermost first through each one's live parent, and
	 * returns a record of each one taken out, in the order applied.  Elements whose live parent
	 * holds no content list, or no longer holds them, are skipped with a log line.
	 */
	public static List<ResolvedAlternateContent> apply(Collection<AlternateContent> elements, Rule rule) {
		List<ResolvedAlternateContent> records = new ArrayList<ResolvedAlternateContent>();
		if (elements == null || elements.isEmpty()) return records;

		// outermost first: by depth, so that an element inside another's branch comes after it
		List<AlternateContent> ordered = new ArrayList<AlternateContent>(elements);
		final Map<AlternateContent, Integer> depths = new IdentityHashMap<AlternateContent, Integer>();
		for (AlternateContent ac : ordered) depths.put(ac, depth(ac));
		Collections.sort(ordered, new Comparator<AlternateContent>() {
			@Override
			public int compare(AlternateContent a, AlternateContent b) {
				return depths.get(a).compareTo(depths.get(b));
			}
		});

		Map<Object, Boolean> decided = new IdentityHashMap<Object, Boolean>();
		for (AlternateContent ac : ordered) {
			if (insideDecided(ac, decided)) {
				// in a discarded branch, or in an element kept whole: it stays whole in there
				continue;
			}
			Object parent = ac.getParent();
			List<Object> list = contentOf(parent);
			if (list == null) {
				log.warn("mc:AlternateContent in " + (parent == null ? "null" : parent.getClass().getName())
						+ ", which holds no content list; left whole");
				continue;
			}
			int index = indexIn(list, ac);
			if (index < 0) {
				log.debug("mc:AlternateContent no longer in its parent's list; skipped");
				continue;
			}
			String containerName = elementName(parent, parent instanceof Child ? ((Child) parent).getParent() : null);
			String address = address(parent);

			if (ac.getChoice().isEmpty() && ac.getFallback() == null) {
				// no branch at all: nothing shows in any reader; Word's save leaves it out
				list.remove(index);
				records.add(new ResolvedAlternateContent(ac, null, containerName, address, index, 0));
				decided.put(ac, Boolean.TRUE);
				continue;
			}

			Object branch = rule.select(ac);
			if (branch == null) {
				decided.put(ac, Boolean.TRUE); // kept whole (REQ-305 at load)
				continue;
			}
			List<Object> content = branch instanceof AlternateContent.Choice
					? ((AlternateContent.Choice) branch).getAny()
					: ((AlternateContent.Fallback) branch).getAny();
			List<Object> promoted = new ArrayList<Object>(content.size());
			for (Object o : content) promoted.add(bind(o, parent));

			list.remove(index);
			list.addAll(index, promoted);
			records.add(new ResolvedAlternateContent(ac, branch, containerName, address, index, promoted.size()));
			decided.put(ac, Boolean.TRUE);
		}
		return records;
	}

	/** The number of parents above the object. */
	private static int depth(Object o) {
		int n = 0;
		Object p = o instanceof Child ? ((Child) o).getParent() : null;
		while (p != null && n < 1024) {
			n++;
			p = p instanceof Child ? ((Child) p).getParent() : null;
		}
		return n;
	}

	private static boolean insideDecided(AlternateContent ac, Map<Object, Boolean> decided) {
		Object p = ac.getParent();
		for (int i = 0; i < 1024 && p != null; i++) {
			if (decided.containsKey(p)) return true;
			p = p instanceof Child ? ((Child) p).getParent() : null;
		}
		return false;
	}

	/**
	 * A branch's child, bound for its new container: a {@code w:customXml} directly inside a
	 * branch comes back from JAXB as a DOM element (its classes carry no {@code @XmlRootElement},
	 * CR-051 section 7), so it is unmarshalled as the container's kind of custom XML element,
	 * wrapped as the container's binding expects.  Anything else is given as it is.
	 */
	private static Object bind(Object o, Object container) {
		if (!(o instanceof org.w3c.dom.Element)) return o;
		org.w3c.dom.Element el = (org.w3c.dom.Element) o;
		if (McBranchSdt.isBranchSdt(el)) {
			// a w:sdt directly in the branch, not yet bound (CR-051 section 11.3): by the container's kind
			try {
				jakarta.xml.bind.Unmarshaller u = McBranchSdt.unmarshaller(Context.jc);
				Object bound = McBranchSdt.bind(el, container, u);
				McBranchSdt.rebindAll((McBranchSdt.Collector) u.getListener(), u);
				return McBranchSdt.wrapFor(bound, container);
			} catch (Exception e) {
				log.warn("w:sdt promoted from an mc:AlternateContent branch could not be bound; left as a DOM element: " + e.getMessage());
				return McBranchSdt.restore(el);
			}
		}
		if (!W_NS.equals(el.getNamespaceURI()) || !"customXml".equals(el.getLocalName())) {
			log.warn("DOM element " + el.getNodeName() + " promoted from an mc:AlternateContent branch, as it is");
			return o;
		}
		Class<?> type = isRowContainer(container) ? CTCustomXmlRow.class
				: isCellContainer(container) ? CTCustomXmlCell.class : CTCustomXmlBlock.class;
		try {
			Object value = Context.jc.createUnmarshaller().unmarshal(el, type).getValue();
			return new JAXBElement(new QName(W_NS, "customXml"), type, value);
		} catch (Exception e) {
			log.warn("w:customXml promoted from an mc:AlternateContent branch could not be bound as " + type.getSimpleName()
					+ "; left as a DOM element: " + e.getMessage());
			return o;
		}
	}

	/**
	 * The live content list of a container, or null where it has none: a {@link ContentAccessor},
	 * a {@link AlternateContent.Choice}'s or {@link AlternateContent.Fallback}'s {@code getAny()},
	 * a note, a comment, a custom XML element, a content control (its {@code w:sdtContent}'s
	 * list), a VML text box ({@code v:textbox}), a list given as a parent itself, and the list of
	 * a footnotes, endnotes or comments root.  The one helper for code that edits through a
	 * block's parent (CR-051 section 10.3, item 1): inside a branch the parent is a Choice or a
	 * Fallback, which is no {@code ContentAccessor}.
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> contentOf(Object container) {
		Object c = XmlUtils.unwrap(container);
		if (c instanceof ContentAccessor) return ((ContentAccessor) c).getContent();
		if (c instanceof List) return (List<Object>) c;
		if (c instanceof AlternateContent.Choice) return ((AlternateContent.Choice) c).getAny();
		if (c instanceof AlternateContent.Fallback) return ((AlternateContent.Fallback) c).getAny();
		if (c instanceof CTFtnEdn) return ((CTFtnEdn) c).getContent();
		if (c instanceof Comments.Comment) return ((Comments.Comment) c).getContent();
		if (c instanceof CTCustomXmlBlock) return ((CTCustomXmlBlock) c).getContent();
		if (c instanceof CTCustomXmlRow) return ((CTCustomXmlRow) c).getContent();
		if (c instanceof CTCustomXmlCell) return ((CTCustomXmlCell) c).getContent();
		if (c instanceof SdtElement) return ((SdtElement) c).getSdtContent() == null ? null : ((SdtElement) c).getSdtContent().getContent();
		if (c instanceof org.docx4j.wml.SdtContent) return ((org.docx4j.wml.SdtContent) c).getContent();
		if (c instanceof org.docx4j.vml.CTTextbox) return ((org.docx4j.vml.CTTextbox) c).getTxbxContent() == null ? null : ((org.docx4j.vml.CTTextbox) c).getTxbxContent().getContent();
		if (c instanceof CTFootnotes) return (List) ((CTFootnotes) c).getFootnote();
		if (c instanceof CTEndnotes) return (List) ((CTEndnotes) c).getEndnote();
		if (c instanceof Comments) return (List) ((Comments) c).getComment();
		return null;
	}

	/** The index of the object in the list, wrapped or not; -1 where absent. */
	private static int indexIn(List<Object> list, Object o) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == o) return i;
			if (list.get(i) instanceof JAXBElement && ((JAXBElement<?>) list.get(i)).getValue() == o) return i;
		}
		return -1;
	}

	// --- the address ---------------------------------------------------------------------------------

	/**
	 * The object's address from the root of its tree: each step an element name with, where the
	 * parent holds a content list, the index in it ({@code w:document/w:body[0]/w:tbl[3]/w:tr[0]/w:tc[1]}).
	 */
	public static String address(Object o) {
		List<String> steps = new ArrayList<String>();
		for (int i = 0; o != null && i < 1024; i++) {
			Object parent = o instanceof Child ? ((Child) o).getParent() : null;
			if (parent instanceof JAXBElement && ((JAXBElement<?>) parent).getValue() == o) {
				// the root wrapper a part's value answers as its parent: one step, not two
				parent = parent instanceof Child ? ((Child) parent).getParent() : null;
			}
			String name = elementName(o, parent);
			List<Object> list = parent == null ? null : contentOf(parent);
			int index = list == null ? -1 : indexIn(list, o);
			steps.add(0, index >= 0 ? name + "[" + index + "]" : name);
			o = parent;
		}
		StringBuilder sb = new StringBuilder();
		for (String s : steps) {
			if (sb.length() > 0) sb.append('/');
			sb.append(s);
		}
		return sb.toString();
	}

	/** The object's element name with its conventional prefix, as the wrapper or annotation names it; the class's simple name failing that. */
	static String elementName(Object o, Object parent) {
		List<Object> list = parent == null ? null : contentOf(parent);
		if (list != null) {
			for (Object item : list) {
				if (item instanceof JAXBElement && ((JAXBElement<?>) item).getValue() == o) {
					return prefixed(((JAXBElement<?>) item).getName());
				}
			}
		}
		if (o instanceof JAXBElement) return prefixed(((JAXBElement<?>) o).getName());
		QName declared = parent == null ? null : declaredName(parent, o);
		if (declared != null) return prefixed(declared);
		if (o instanceof CTFtnEdn) {
			return parent instanceof CTEndnotes ? "w:endnote" : parent instanceof CTFootnotes ? "w:footnote" : "w:footnote";
		}
		if (o instanceof Comments.Comment) return "w:comment";
		if (o instanceof Tc) return "w:tc";
		if (o instanceof Tr) return "w:tr";
		if (o instanceof Tbl) return "w:tbl";
		if (o instanceof Body) return parent instanceof org.docx4j.wml.CTDocPart ? "w:docPartBody" : "w:body";
		if (o instanceof SdtContentBlock || o instanceof CTSdtContentRow || o instanceof CTSdtContentCell) return "w:sdtContent";
		if (o instanceof CTCustomXmlBlock || o instanceof CTCustomXmlRow || o instanceof CTCustomXmlCell) return "w:customXml";
		if (o instanceof CTTxbxContent) return "w:txbxContent";
		XmlRootElement root = o.getClass().getAnnotation(XmlRootElement.class);
		if (root != null) {
			String ns = root.namespace();
			if ("##default".equals(ns)) {
				jakarta.xml.bind.annotation.XmlSchema schema = o.getClass().getPackage() == null ? null
						: o.getClass().getPackage().getAnnotation(jakarta.xml.bind.annotation.XmlSchema.class);
				ns = schema == null ? "" : schema.namespace();
			}
			return prefixed(new QName(ns, root.name()));
		}
		return o.getClass().getSimpleName();
	}

	/**
	 * The element name the parent's class declares for this child, read from the annotation of the
	 * field holding it ({@code @XmlElement}, or the matching member of {@code @XmlElements} for a
	 * list of several kinds): the DrawingML and VML chain to a text box ({@code wp:inline},
	 * {@code a:graphic}, {@code wps:wsp}, {@code wps:txbx}, {@code v:textbox}) is held in such
	 * fields, not in content lists.  Null where no annotated field holds it.
	 */
	private static QName declaredName(Object parent, Object o) {
		Object p = XmlUtils.unwrap(parent);
		for (Class<?> c = p.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
			for (java.lang.reflect.Field f : c.getDeclaredFields()) {
				if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
				Object value;
				try {
					f.setAccessible(true);
					value = f.get(p);
				} catch (Exception e) {
					continue;
				}
				boolean holds = value == o;
				if (!holds && value instanceof List) {
					for (Object item : (List<?>) value) {
						if (item == o) { holds = true; break; }
						// a lax any-element list holds a wrapper for a class with no root element (wps:wsp, v:textbox)
						if (item instanceof JAXBElement && ((JAXBElement<?>) item).getValue() == o) return ((JAXBElement<?>) item).getName();
					}
				}
				if (!holds) continue;
				jakarta.xml.bind.annotation.XmlElement one = f.getAnnotation(jakarta.xml.bind.annotation.XmlElement.class);
				jakarta.xml.bind.annotation.XmlElements several = f.getAnnotation(jakarta.xml.bind.annotation.XmlElements.class);
				if (one == null && several != null) {
					for (jakarta.xml.bind.annotation.XmlElement e : several.value()) {
						if (e.type() == jakarta.xml.bind.annotation.XmlElement.DEFAULT.class || e.type().isInstance(o)) { one = e; break; }
					}
				}
				if (one == null && (f.getAnnotation(jakarta.xml.bind.annotation.XmlElementRef.class) != null
						|| f.getAnnotation(jakarta.xml.bind.annotation.XmlElementRefs.class) != null
						|| f.getAnnotation(jakarta.xml.bind.annotation.XmlAnyElement.class) != null
						|| f.getAnnotation(jakarta.xml.bind.annotation.XmlAttribute.class) != null
						|| f.getAnnotation(jakarta.xml.bind.annotation.XmlTransient.class) != null)) return null;
				// no annotation at all: JAXB names the element after the field, in the package's namespace
				String local = one == null || "##default".equals(one.name()) ? f.getName() : one.name();
				String ns = one == null ? "##default" : one.namespace();
				if ("##default".equals(ns)) {
					jakarta.xml.bind.annotation.XmlSchema schema = c.getPackage() == null ? null
							: c.getPackage().getAnnotation(jakarta.xml.bind.annotation.XmlSchema.class);
					ns = schema == null ? "" : schema.namespace();
				}
				return new QName(ns, local);
			}
		}
		return null;
	}

	private static String prefixed(QName name) {
		String ns = name.getNamespaceURI();
		if (ns == null || ns.isEmpty()) return name.getLocalPart();
		String prefix = NamespacePrefixMappings.getPreferredPrefixStatic(ns, null, true);
		return prefix == null || prefix.isEmpty() ? name.getLocalPart() : prefix + ":" + name.getLocalPart();
	}

	// --- mc:Ignorable --------------------------------------------------------------------------------

	/**
	 * The prefixes the root's {@code mc:Ignorable} must name after these records' splices: the
	 * conventional prefix of each namespace the promoted Choices' {@code Requires} names (the
	 * marshal writes the promoted content's elements with the conventional prefixes), or the
	 * {@code Requires} prefix itself where it resolves to no namespace.  In document order, no
	 * duplicates.
	 */
	public static Set<String> ignorablePrefixes(List<ResolvedAlternateContent> records) {
		Set<String> prefixes = new LinkedHashSet<String>();
		for (ResolvedAlternateContent r : records) {
			if (!(r.getBranch() instanceof AlternateContent.Choice)) continue;
			AlternateContent.Choice choice = (AlternateContent.Choice) r.getBranch();
			String requires = choice.getRequires();
			if (requires == null || requires.trim().isEmpty()) continue;
			String[] named = requires.trim().split("\\s+");
			List<String> uris = McSelection.requiresNamespaces(choice);
			for (int i = 0; i < named.length; i++) {
				String uri = i < uris.size() ? uris.get(i) : null;
				String conventional = uri == null ? null : NamespacePrefixMappings.getPreferredPrefixStatic(uri, named[i], true);
				prefixes.add(conventional == null || conventional.isEmpty() ? named[i] : conventional);
			}
		}
		return prefixes;
	}

	/**
	 * Adds to the root's {@code mc:Ignorable} (through its {@code setIgnorable(String)}, where its
	 * class has one) each of these prefixes it does not already name.  Returns whether it changed.
	 */
	public static boolean addIgnorable(Object root, Collection<String> prefixes) {
		root = XmlUtils.unwrap(root);
		if (root == null || prefixes == null || prefixes.isEmpty()) return false;
		String current = NamespacePrefixMapperUtils.ignorableOf(root);
		Set<String> listed = new LinkedHashSet<String>();
		if (current != null && !current.trim().isEmpty()) listed.addAll(java.util.Arrays.asList(current.trim().split("\\s+")));
		int before = listed.size();
		listed.addAll(prefixes);
		if (listed.size() == before) return false;
		StringBuilder sb = new StringBuilder();
		for (String p : listed) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(p);
		}
		try {
			java.lang.reflect.Method m = root.getClass().getMethod("setIgnorable", String.class);
			m.invoke(root, sb.toString());
			return true;
		} catch (NoSuchMethodException e) {
			log.debug(root.getClass().getName() + " has no setIgnorable(String); mc:Ignorable left as it is");
			return false;
		} catch (Exception e) {
			log.warn("setIgnorable on " + root.getClass().getName() + ": " + e);
			return false;
		}
	}
}
