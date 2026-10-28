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
package org.docx4j.model.fields;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.jvnet.jaxb.lang.Child;

/**
 * What the tier-1 evaluators read from the document itself (CR-067 phase 1): a bookmark's text (REF), the
 * number of the note a bookmark holds a reference to (NOTEREF), the section a node is in (SECTION), and a
 * core-property date parsed (CREATEDATE, SAVEDATE). Read from the main document part; a caller with its own
 * view of the document (the text model's walk) fills the context itself.
 *
 * @since 17.3.5
 */
final class FieldSources {

	private FieldSources() {
	}

	/** A bookmark's text (its {@code w:t} in document order), by name in lower case, from the main part. */
	static Map<String, String> bookmarkTexts(WordprocessingMLPackage pkg) {
		final Map<String, String> out = new HashMap<String, String>();
		if (pkg == null || pkg.getMainDocumentPart() == null || pkg.getMainDocumentPart().getJaxbElement() == null) return out;
		final Map<BigInteger, String> open = new HashMap<BigInteger, String>();
		final Map<String, StringBuilder> texts = new HashMap<String, StringBuilder>();
		walk(pkg.getMainDocumentPart().getJaxbElement().getBody(), new Visitor() {
			public void bookmarkStart(CTBookmark b) {
				String name = b.getName() == null ? null : b.getName().toLowerCase(Locale.ROOT);
				if (name == null) return;
				open.put(b.getId(), name);
				if (!texts.containsKey(name)) texts.put(name, new StringBuilder());
			}
			public void bookmarkEnd(CTMarkupRange e) {
				open.remove(e.getId());
			}
			public void text(String t) {
				for (String name : open.values()) texts.get(name).append(t);
			}
			public void noteReference(String kind, CTFtnEdnRef ref) {
			}
		});
		for (Map.Entry<String, StringBuilder> e : texts.entrySet()) out.put(e.getKey(), e.getValue().toString());
		return out;
	}

	/**
	 * A bookmark's runs (copies holding their {@code w:t} text and run properties, other run content dropped), by
	 * name in lower case, from the main part: what a REF's result is made of, since Word keeps the bookmark's
	 * formatting in it (measured 2026-10-28, CR-067 section 11.3).
	 */
	static Map<String, List<R>> bookmarkRuns(WordprocessingMLPackage pkg) {
		final Map<String, List<R>> out = new HashMap<String, List<R>>();
		if (pkg == null || pkg.getMainDocumentPart() == null || pkg.getMainDocumentPart().getJaxbElement() == null) return out;
		final Map<BigInteger, String> open = new HashMap<BigInteger, String>();
		walk(pkg.getMainDocumentPart().getJaxbElement().getBody(), new Visitor() {
			public void bookmarkStart(CTBookmark b) {
				String name = b.getName() == null ? null : b.getName().toLowerCase(Locale.ROOT);
				if (name == null) return;
				open.put(b.getId(), name);
				if (!out.containsKey(name)) out.put(name, new ArrayList<R>());
			}
			public void bookmarkEnd(CTMarkupRange e) {
				open.remove(e.getId());
			}
			public void text(String t) {
			}
			public void run(R r) {
				if (open.isEmpty()) return;
				R copy = null;
				for (Object c : r.getContent()) {
					Object u = c instanceof JAXBElement ? ((JAXBElement<?>) c).getValue() : c;
					String n = c instanceof JAXBElement ? ((JAXBElement<?>) c).getName().getLocalPart() : null;
					if (u instanceof Text && (n == null || "t".equals(n))) {
						if (copy == null) {
							copy = new R();
							if (r.getRPr() != null) copy.setRPr(XmlUtils.deepCopy(r.getRPr()));
						}
						copy.getContent().add(XmlUtils.deepCopy(c));
					}
				}
				if (copy != null) for (String name : open.values()) out.get(name).add(XmlUtils.deepCopy(copy));
			}
			public void noteReference(String kind, CTFtnEdnRef ref) {
			}
		});
		return out;
	}

	/**
	 * The number of the footnote or endnote whose reference a bookmark holds, by bookmark name in lower case:
	 * references counted in document order per kind (Word's default numbering, from 1; a reference with a
	 * custom mark is not counted).
	 */
	static Map<String, String> noteNumbers(WordprocessingMLPackage pkg) {
		final Map<String, String> out = new HashMap<String, String>();
		if (pkg == null || pkg.getMainDocumentPart() == null || pkg.getMainDocumentPart().getJaxbElement() == null) return out;
		final Map<BigInteger, String> open = new HashMap<BigInteger, String>();
		final int[] counts = new int[2];
		walk(pkg.getMainDocumentPart().getJaxbElement().getBody(), new Visitor() {
			public void bookmarkStart(CTBookmark b) {
				if (b.getName() != null) open.put(b.getId(), b.getName().toLowerCase(Locale.ROOT));
			}
			public void bookmarkEnd(CTMarkupRange e) {
				open.remove(e.getId());
			}
			public void text(String t) {
			}
			public void noteReference(String kind, CTFtnEdnRef ref) {
				if (ref.getId() != null && ref.getId().signum() <= 0) return; // a separator's
				int k = "footnoteReference".equals(kind) ? 0 : 1;
				if (!ref.isCustomMarkFollows()) counts[k]++;
				String number = String.valueOf(counts[k]);
				for (String name : open.values()) if (!out.containsKey(name)) out.put(name, number);
			}
		});
		return out;
	}

	/**
	 * The section (from 1) the node is in, by the paragraphs with a {@code w:sectPr} before its top-level
	 * ancestor in the body; null where the part is not the main document part or the node is not in its body.
	 */
	static Integer sectionOf(Part part, Object node) {
		if (!(part instanceof MainDocumentPart) || node == null) return null;
		MainDocumentPart main = (MainDocumentPart) part;
		if (main.getJaxbElement() == null || main.getJaxbElement().getBody() == null) return null;
		Body body = main.getJaxbElement().getBody();
		Object top = node;
		Object parent = parentOf(top);
		while (parent != null && parent != body) {
			top = parent;
			parent = parentOf(top);
		}
		if (parent != body) return null;
		int section = 1;
		for (Object o : body.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u == top) return section;
			if (u instanceof P && ((P) u).getPPr() != null && ((P) u).getPPr().getSectPr() != null) section++;
		}
		return null;
	}

	private static Object parentOf(Object o) {
		return o instanceof Child ? ((Child) o).getParent() : null;
	}

	/**
	 * A W3CDTF or ISO 8601 date-time ({@code 2024-02-03T04:05:06Z}, with an offset, with no offset, or a date alone), or
	 * null. A value with no offset and a date alone are UTC, as Word reads a core property's (measured 2026-10-28, CR-067
	 * section 12.2: created {@code 2024-02-03} showed 11:00 at UTC+11, modified {@code 2025-06-07T08:09:10} showed 18:09
	 * at UTC+10); not the machine's zone, which a revision's {@code w:date} and a date control's value mean instead.
	 */
	static Date parseW3CDTF(String s) {
		if (s == null) return null;
		String t = s.trim();
		if (t.isEmpty()) return null;
		try {
			return Date.from(java.time.OffsetDateTime.parse(t).toInstant());
		} catch (Exception e) {
		}
		try {
			return Date.from(java.time.LocalDateTime.parse(t).toInstant(java.time.ZoneOffset.UTC));
		} catch (Exception e) {
		}
		try {
			return Date.from(java.time.LocalDate.parse(t).atStartOfDay(java.time.ZoneOffset.UTC).toInstant());
		} catch (Exception e) {
		}
		try {
			javax.xml.datatype.XMLGregorianCalendar c = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(t);
			if (c.getTimezone() == javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED) c.setTimezone(0);
			return c.toGregorianCalendar().getTime();
		} catch (Exception e) {
			return null;
		}
	}

	interface Visitor {
		void bookmarkStart(CTBookmark b);
		void bookmarkEnd(CTMarkupRange e);
		void text(String t);
		void noteReference(String kind, CTFtnEdnRef ref);
		/** A run, before its content is walked. */
		default void run(R r) {
		}
	}

	/** Document order over the content lists, bookmarks and run content included. */
	private static void walk(Object node, Visitor v) {
		if (node == null) return;
		String name = null;
		Object u = node;
		if (node instanceof JAXBElement) {
			name = ((JAXBElement<?>) node).getName().getLocalPart();
			u = ((JAXBElement<?>) node).getValue();
		}
		if (u instanceof CTBookmark) {
			v.bookmarkStart((CTBookmark) u);
			return;
		}
		if (u instanceof CTMarkupRange) {
			if (name == null || "bookmarkEnd".equals(name)) v.bookmarkEnd((CTMarkupRange) u);
			return;
		}
		if (u instanceof CTFtnEdnRef) {
			v.noteReference(name == null ? "footnoteReference" : name, (CTFtnEdnRef) u);
			return;
		}
		if (u instanceof Text) {
			if (name == null || "t".equals(name)) v.text(((Text) u).getValue());
			return;
		}
		if (u instanceof ContentAccessor) {
			if (u instanceof R) v.run((R) u);
			List<Object> content = new ArrayList<Object>(((ContentAccessor) u).getContent());
			for (Object c : content) walk(c, v);
		} else if (u instanceof org.docx4j.wml.SdtElement) {
			org.docx4j.wml.SdtContent sc = ((org.docx4j.wml.SdtElement) u).getSdtContent();
			if (sc != null) for (Object c : new ArrayList<Object>(sc.getContent())) walk(c, v);
		} else if (u instanceof org.docx4j.wml.RunIns) {
			for (Object c : new ArrayList<Object>(((org.docx4j.wml.RunIns) u).getCustomXmlOrSmartTagOrSdt())) walk(c, v);
		} else if (u instanceof org.docx4j.wml.CTSmartTagRun) {
			for (Object c : new ArrayList<Object>(((org.docx4j.wml.CTSmartTagRun) u).getContent())) walk(c, v);
		}
	}
}
