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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thedeanda.lorem.LoremIpsum;

/**
 * Proves the guarantee on one document: no token of the original survives in
 * the anonymised output.
 * <p>
 * A token is a run of three or more letters (any script), lower-cased. The
 * extraction takes, from every XML part, every text node, the values of the
 * attributes known to carry names, descriptions, instructions and identities
 * (see {@link #ATTRIBUTES}), and every external relationship target. A token of
 * the output which is also a token of the original is a leak unless it is in
 * the vocabulary the anonymiser itself writes:
 * <ul>
 * <li>a substring of a lorem ipsum word (the Latin scramble maps letters
 * positionally onto lorem text, so an output word is always a fragment of one
 * lorem word) - the blind spot is an original word which is itself such a
 * fragment ({@code sit}, {@code amet}, {@code class}); such a word is in the
 * lorem vocabulary and carries nothing;</li>
 * <li>a field keyword or format-switch word ({@link FieldInstructions#KEYWORDS});</li>
 * <li>a date or number picture ({@code yyyy}, {@code MMMM});</li>
 * <li>a word of a built-in style name (KnownStyles.xml and the document's own
 * latent-style list), which Word identifies built-in styles by;</li>
 * <li>the fixed words the tool writes: Author, Sheet, example, invalid,
 * http, None, General, true, false, app.xml's "Microsoft Office Word", and
 * the altChunk marker's "altChunk removed by docx4j-docx-anon".</li>
 * </ul>
 * Digits are not tokens: they are randomised, and a 4-digit run would collide
 * by chance often enough to make the check useless.
 *
 * @since 17.2.0
 */
public class Verify {

	private static final Logger log = LoggerFactory.getLogger(Verify.class);

	/** A token of the original found in the output. */
	public static class Leak {
		public final String partName;
		public final String where;
		public final String token;

		Leak(String partName, String where, String token) {
			this.partName = partName;
			this.where = where;
			this.token = token;
		}

		@Override
		public String toString() {
			return partName + " " + where + ": \"" + token + "\"";
		}
	}

	/** (element local name, attribute local name) pairs whose values are extracted. "*" matches any element. */
	public static final Set<String> ATTRIBUTES = new HashSet<String>(Arrays.asList(
			"*/author", "*/initials", "*/contact", "*/userId",
			"alias/val", "tag/val",
			"hyperlink/tooltip", "hyperlink/docLocation", "hlinkClick/tooltip", "hlinkHover/tooltip",
			"docVar/name", "docVar/val",
			"fldSimple/instr",
			"docPr/name", "docPr/descr", "docPr/title",
			"cNvPr/name", "cNvPr/descr", "cNvPr/title",
			"imagedata/title", "shape/alt", "shape/title", "shape/href", "textpath/string",
			"listItem/displayText", "listItem/value",
			"name/val", "aliases/val", "docPart/val",
			"attr/val", "attr/name",
			"lvlText/val",
			"prSet/phldrT",
			"bookmarkStart/name", "moveFromRangeStart/name", "moveToRangeStart/name",
			"default/val", "listEntry/val", "helpText/val", "statusText/val",
			"placeholder/val",
			"connectString/val", "query/val", "mailSubject/val", "addressFieldName/val",
			"theme/name", "clrScheme/name", "fontScheme/name", "fmtScheme/name",
			"pivotSource/name", "strDim/name", "numDim/name", "lvl/name",
			"Relationship/Target"));

	private static final Pattern PICTURE = Pattern.compile("^(d+|m+|y+|h+|s+)$");

	private static final Set<String> FIXED_WORDS = new HashSet<String>(Arrays.asList(
			"author", "sheet", "example", "invalid", "http", "none", "general", "true", "false",
			"microsoft", "office", "word",
			// the altChunk marker paragraph (Placeholders.ALTCHUNK_REMOVED)
			"altchunk", "removed", "docx", "anon"));

	private static String loremJoined;

	/** The tokens of one package: per part, token to the first place it was seen. */
	public static class Extraction {
		public final Map<String, Map<String, String>> byPart = new LinkedHashMap<String, Map<String, String>>();
		/** words of the document's own latent-style names: built-in, allowed */
		public final Set<String> styleWords = new HashSet<String>();

		public Set<String> allTokens() {
			Set<String> all = new HashSet<String>();
			for (Map<String, String> m : byPart.values()) all.addAll(m.keySet());
			return all;
		}

		void add(String partName, String token, String where) {
			Map<String, String> m = byPart.get(partName);
			if (m == null) {
				m = new HashMap<String, String>();
				byPart.put(partName, m);
			}
			String existing = m.get(token);
			// prefer a place where a built-in style word is not allowed, so the check is not
			// disarmed by the same word turning up in a style name first
			if (existing == null || (STYLE_NAME_PLACES.contains(existing) && !STYLE_NAME_PLACES.contains(where))) {
				m.put(token, where);
			}
		}
	}

	/**
	 * The check: extract both, compare.
	 */
	public static List<Leak> noOriginalText(OpcPackage original, OpcPackage anonymised) {
		return compare(extract(original), extract(anonymised));
	}

	/**
	 * The tokens of the anonymised package which the original also had, less the
	 * anonymiser's own vocabulary.
	 */
	public static List<Leak> compare(Extraction original, Extraction anonymised) {
		Set<String> originalTokens = original.allTokens();
		Set<String> allowedStyleWords = new HashSet<String>(anonymised.styleWords);
		allowedStyleWords.addAll(knownStyleWords());

		List<Leak> leaks = new ArrayList<Leak>();
		for (Map.Entry<String, Map<String, String>> part : anonymised.byPart.entrySet()) {
			for (Map.Entry<String, String> e : part.getValue().entrySet()) {
				String token = e.getKey();
				if (!originalTokens.contains(token)) continue;
				if (isAllowed(token, e.getValue(), allowedStyleWords)) continue;
				leaks.add(new Leak(part.getKey(), e.getValue(), token));
			}
		}
		return leaks;
	}

	/** the places a built-in style name legitimately appears */
	private static final Set<String> STYLE_NAME_PLACES = new HashSet<String>(Arrays.asList(
			"name/@val", "aliases/@val", "instrText/text()", "delInstrText/text()", "fldSimple/@instr"));

	static boolean isAllowed(String token, String where, Set<String> styleWords) {
		if (FIXED_WORDS.contains(token)) return true;
		if (FieldInstructions.KEYWORDS.contains(token.toUpperCase(Locale.ROOT))) return true;
		if (PICTURE.matcher(token).matches()) return true;
		if (STYLE_NAME_PLACES.contains(where) && styleWords.contains(token)) return true;
		return isLoremFragment(token);
	}

	/** true if the token is a substring of a lorem ipsum word */
	static synchronized boolean isLoremFragment(String token) {
		if (loremJoined == null) {
			StringBuilder sb = new StringBuilder(" ");
			try (InputStream is = LoremIpsum.class.getResourceAsStream("lorem.txt")) {
				if (is != null) {
					BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
					String line;
					while ((line = r.readLine()) != null) {
						sb.append(line.trim().toLowerCase(Locale.ROOT)).append(' ');
					}
				}
			} catch (Exception e) {
				log.warn("lorem word list not readable: " + e);
			}
			loremJoined = sb.toString();
		}
		return loremJoined.contains(token);
	}

	private static Set<String> knownStyleWords;

	static synchronized Set<String> knownStyleWords() {
		if (knownStyleWords == null) {
			Set<String> words = new HashSet<String>();
			for (Style s : StyleDefinitionsPart.getKnownStyles().values()) {
				if (s.getName() != null) words.addAll(tokens(s.getName().getVal()));
			}
			knownStyleWords = words;
		}
		return knownStyleWords;
	}

	/** Every XML part's text nodes and named attributes, and every external target. */
	public static Extraction extract(OpcPackage pkg) {
		Extraction x = new Extraction();

		externalTargets(pkg, x);
		for (Part p : new ArrayList<Part>(pkg.getParts().getParts().values())) {
			externalTargets(p, x);
			Document doc = null;
			try {
				if (p instanceof JaxbXmlPart) {
					JaxbXmlPart<?> jp = (JaxbXmlPart<?>) p;
					Object contents = jp.getContents();
					if (contents == null) continue;
					doc = XmlUtils.marshaltoW3CDomDocument(contents, jp.getJAXBContext());
				} else if (p instanceof XmlPart) {
					doc = ((XmlPart) p).getDocument();
				}
			} catch (Exception e) {
				log.warn("cannot extract " + p.getPartName().getName() + ": " + e);
				x.add(p.getPartName().getName(), " unreadable", "part");
			}
			if (doc == null) continue;
			extract(p.getPartName().getName(), doc.getDocumentElement(), x);
		}
		return x;
	}

	private static void externalTargets(Base base, Extraction x) {
		RelationshipsPart rp = base.getRelationshipsPart();
		if (rp == null || rp.getRelationships() == null) return;
		String partName = rp.getPartName() == null ? "_rels/.rels" : rp.getPartName().getName();
		for (Relationship r : rp.getRelationships().getRelationship()) {
			if ("External".equals(r.getTargetMode())) {
				for (String t : tokens(r.getTarget())) {
					x.add(partName, t, "Relationship/@Target");
				}
			}
		}
	}

	private static void extract(String partName, Node n, Extraction x) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			Element el = (Element) n;
			String eln = local(el);
			NamedNodeMap attrs = el.getAttributes();
			for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
				Attr a = (Attr) attrs.item(i);
				String an = local(a);
				if (eln.equals("lsdException") && an.equals("name")) {
					x.styleWords.addAll(tokens(a.getValue()));
					continue;
				}
				if (ATTRIBUTES.contains(eln + "/" + an) || ATTRIBUTES.contains("*/" + an)) {
					for (String t : tokens(a.getValue())) {
						x.add(partName, t, eln + "/@" + an);
					}
				}
			}
		}
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
				String where = local(n) + "/text()";
				for (String t : tokens(c.getNodeValue())) {
					x.add(partName, t, where);
				}
			} else if (c.getNodeType() == Node.ELEMENT_NODE) {
				extract(partName, c, x);
			}
		}
	}

	private static String local(Node n) {
		return n.getLocalName() == null ? n.getNodeName() : n.getLocalName();
	}

	/** Runs of three or more letters, lower-cased. */
	public static List<String> tokens(String s) {
		List<String> result = new ArrayList<String>();
		if (s == null) return result;
		int i = 0, n = s.length();
		while (i < n) {
			int cp = s.codePointAt(i);
			if (Character.isLetter(cp)) {
				int start = i;
				int letters = 0;
				while (i < n && Character.isLetter(cp = s.codePointAt(i))) {
					i += Character.charCount(cp);
					letters++;
				}
				if (letters >= 3) result.add(s.substring(start, i).toLowerCase(Locale.ROOT));
			} else {
				i += Character.charCount(cp);
			}
		}
		return result;
	}

}
