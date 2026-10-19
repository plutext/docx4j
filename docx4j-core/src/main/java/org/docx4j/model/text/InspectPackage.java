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
package org.docx4j.model.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.w3c.dom.Element;

/**
 * A package as the inspector reads it (CR-050): its parts by name, each XML part's DOM (a JAXB part
 * marshalled, its elements linked to their objects), each binary part's bytes, and its relationships
 * with their targets resolved. Nothing in the package is changed.
 */
final class InspectPackage {

	/** A relationship as stored. */
	static final class Rel {
		final String id, type, target;
		final boolean external;
		final Element element;
		Rel(String id, String type, String target, boolean external, Element element) {
			this.id = id;
			this.type = type == null ? "" : type;
			this.target = target;
			this.external = external;
			this.element = element;
		}
	}

	final OpcPackage pkg;
	final List<String> chain;
	final InspectRun run;
	private final Map<String, Part> parts = new TreeMap<String, Part>();
	private final Map<String, InspectXml.Dom> doms = new HashMap<String, InspectXml.Dom>();
	private final Map<String, List<Rel>> rels = new HashMap<String, List<Rel>>();
	/** Every linked element of every DOM read, to its JAXB object. */
	private final Map<Element, Object> jaxb = new IdentityHashMap<Element, Object>();
	/** The parts (and relationship parts) inspected, so that a global stop can list the rest. */
	final Set<String> done = new HashSet<String>();

	InspectPackage(OpcPackage pkg, List<String> chain, InspectRun run) {
		this.pkg = pkg;
		this.chain = new ArrayList<String>(chain);
		this.run = run;
		for (Part p : pkg.getParts().getParts().values()) parts.put(p.getPartName().getName(), p);
	}

	/** The part names, sorted, relationship parts aside. */
	List<String> partNames() {
		return new ArrayList<String>(parts.keySet());
	}

	/** What an inspection reads: every part and every relationship part. */
	List<String> sources() {
		List<String> out = new ArrayList<String>();
		if (pkg.getRelationshipsPart() != null) out.add(pkg.getRelationshipsPart().getPartName().getName());
		for (Map.Entry<String, Part> e : parts.entrySet()) {
			out.add(e.getKey());
			RelationshipsPart rp = e.getValue().getRelationshipsPart(false);
			if (rp != null) out.add(rp.getPartName().getName());
		}
		return out;
	}

	/** {@code chain|part|path} */
	String location(String part, String path) {
		return String.join(">", chain) + "|" + part + "|" + (path == null ? "" : path);
	}

	boolean has(String part) {
		return part != null && parts.containsKey(part);
	}

	Part part(String name) {
		return name == null ? null : parts.get(name);
	}

	String contentType(String part) {
		Part p = parts.get(part);
		return p == null || p.getContentType() == null ? null : p.getContentType();
	}

	/** A part's DOM, read once and counted against the element budget; null for a part that is not XML. */
	InspectXml.Dom dom(String name) {
		if (doms.containsKey(name)) return doms.get(name);
		InspectXml.Dom dom = null;
		Part p = parts.get(name);
		if (p == null && pkg.getRelationshipsPart() != null && pkg.getRelationshipsPart().getPartName().getName().equals(name)) {
			p = pkg.getRelationshipsPart();
		}
		if (p == null) p = relationshipsPart(name);
		try {
			if (p instanceof JaxbXmlPart) {
				JaxbXmlPart<?> jp = (JaxbXmlPart<?>) p;
				Object root = jp.getJaxbElement();
				if (root != null) dom = InspectXml.marshal(root, jp.getJAXBContext(), run);
			} else if (p instanceof CustomXmlDataStoragePart) {
				CustomXmlDataStoragePart cp = (CustomXmlDataStoragePart) p;
				if (cp.getData() != null) dom = InspectXml.of(cp.getData().getDocument());
			} else if (p instanceof XmlPart) {
				dom = InspectXml.of(((XmlPart) p).getDocument());
			} else if (p instanceof BinaryPart && looksLikeXml(((BinaryPart) p).getBytes())) {
				dom = InspectXml.parse(((BinaryPart) p).getBytes());
			}
		} catch (InspectRun.Stop s) {
			throw s;
		} catch (Exception e) {
			dom = null;
		}
		doms.put(name, dom);
		if (dom != null) {
			jaxb.putAll(dom.jaxb);
			run.elements(dom.elements);
		}
		return dom;
	}

	private static boolean looksLikeXml(byte[] b) {
		int i = 0;
		if (b == null) return false;
		if (b.length >= 3 && (b[0] & 0xff) == 0xef && (b[1] & 0xff) == 0xbb && (b[2] & 0xff) == 0xbf) i = 3;
		while (i < b.length && Character.isWhitespace(b[i])) i++;
		return i < b.length && b[i] == '<';
	}

	/** A relationship part by its name, found through its source part. */
	private RelationshipsPart relationshipsPart(String name) {
		for (Part p : parts.values()) {
			RelationshipsPart rp = p.getRelationshipsPart(false);
			if (rp != null && rp.getPartName().getName().equals(name)) return rp;
		}
		return null;
	}

	/** A part's root element, or null. */
	Element xml(String name) {
		InspectXml.Dom d = dom(name);
		return d == null ? null : d.root();
	}

	/** An element's canonical path in its part. */
	String path(String part, Element e) {
		InspectXml.Dom d = dom(part);
		return d == null ? "" : d.paths.of(e);
	}

	/** The JAXB object an element was marshalled from, or null. */
	Object jaxb(Element e) {
		return jaxb.get(e);
	}

	/** A binary part's bytes, or null. */
	byte[] bytes(String name) {
		Part p = parts.get(name);
		return p instanceof BinaryPart ? ((BinaryPart) p).getBytes() : null;
	}

	/** The name of the relationships part of a part ({@code /} the package). */
	String relsPart(String part) {
		RelationshipsPart rp = "/".equals(part) ? pkg.getRelationshipsPart() : parts.containsKey(part) ? parts.get(part).getRelationshipsPart(false) : null;
		if (rp != null) return rp.getPartName().getName();
		int slash = part.lastIndexOf('/');
		return part.substring(0, slash + 1) + "_rels/" + part.substring(slash + 1) + ".rels";
	}

	/** A part's relationships ({@code /} the package's), as stored, their internal targets resolved. */
	List<Rel> rels(String part) {
		List<Rel> out = rels.get(part);
		if (out != null) return out;
		out = new ArrayList<Rel>();
		RelationshipsPart rp = "/".equals(part) ? pkg.getRelationshipsPart() : parts.containsKey(part) ? parts.get(part).getRelationshipsPart(false) : null;
		if (rp != null) {
			Element root = xml(rp.getPartName().getName());
			String base = "/".equals(part) ? "/" : part.substring(0, part.lastIndexOf('/') + 1);
			for (Element e : InspectXml.children(root)) {
				if (!InspectXml.is(e, InspectXml.REL, "Relationship")) continue;
				boolean external = "External".equals(InspectXml.attr(e, null, "TargetMode"));
				String target = InspectXml.attr(e, null, "Target");
				if (!external && target != null) target = target.startsWith("/") ? normalize(target) : normalize(base + target);
				out.add(new Rel(InspectXml.attr(e, null, "Id"), InspectXml.attr(e, null, "Type"), target, external, e));
			}
		}
		rels.put(part, out);
		return out;
	}

	/** A path with its {@code .} and {@code ..} steps resolved (posixpath.normpath). */
	static String normalize(String path) {
		List<String> steps = new ArrayList<String>();
		for (String s : path.split("/")) {
			if (s.isEmpty() || s.equals(".")) continue;
			if (s.equals("..")) {
				if (!steps.isEmpty()) steps.remove(steps.size() - 1);
				continue;
			}
			steps.add(s);
		}
		return "/" + String.join("/", steps);
	}

	/** The target of a part's relationship by id, or null. */
	String target(String part, String id) {
		if (id == null) return null;
		for (Rel r : rels(part)) if (id.equals(r.id)) return r.target;
		return null;
	}

	/** The internal targets of a part's relationships whose type ends so. */
	List<String> byType(String part, String suffix) {
		List<String> out = new ArrayList<String>();
		if (part == null) return out;
		for (Rel r : rels(part)) if (!r.external && r.type.endsWith(suffix) && r.target != null) out.add(r.target);
		return out;
	}

	/** The main part: the package relationship of type {@code officeDocument}'s target. */
	String main() {
		for (Rel r : rels("/")) if (!r.external && r.type.endsWith("/officeDocument")) return r.target;
		return null;
	}
}
