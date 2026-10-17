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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.docx4j.Docx4jProperties;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.parts.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one place docx4j decides which branch of an {@code mc:AlternateContent} it
 * draws (ECMA-376 Part 3, markup compatibility).
 *
 * <p>Word draws the first {@code mc:Choice} whose {@code Requires} names only
 * namespaces it understands, and reaches the {@code mc:Fallback} only when it
 * understands none of them (Part 3, 10.2.1).  docx4j applies the same rule with
 * the namespaces named in {@code docx4j.jaxb.mc.preferChoice}: the first Choice
 * whose {@code Requires} prefixes are all listed there, else the Fallback, else -
 * an element with Choices and no Fallback - the first Choice, with a warning.</p>
 *
 * <p><b>The property's default is empty</b>, so the Fallback is drawn.  Measured
 * over the real-document corpora for 17.1.0 (every element there is a {@code wps}
 * text box with a VML fallback), preferring {@code wps} moved two documents and both
 * fell; and the FO exporter does not yet draw an inline {@code wps} text box at all
 * (CR-021 §8.5).  Set {@code docx4j.jaxb.mc.preferChoice=wps} to take the Choice
 * where the fallback is missing or wrong for your documents.  {@code wpg},
 * {@code w14}, {@code wp14} and {@code v} are deliberately not suggested: a Choice
 * docx4j cannot draw is worse than the fallback the producer wrote for exactly
 * that case.</p>
 *
 * <p>Callers: {@link org.docx4j.TraversalUtil} in its {@link McMode#READ} mode, the
 * visitor exporters (FO, HTML), {@code docx2fo.xslt} and {@code docx2xhtml-core.xslt}
 * through {@link org.docx4j.utils.XSLTUtils#mcPrefersChoice}, {@link org.docx4j.TextUtils},
 * the markdown exporter; for PresentationML the placeholder walks of the slide, layout
 * and master parts ({@link #selectedContent}) and {@code pptx2svginhtml.xslt}.  The
 * property is the one switch for every format: a PowerPoint slide's
 * {@code Requires="a14"} Choice (an equation, ink) is drawn with
 * {@code docx4j.jaxb.mc.preferChoice=a14}, its picture Fallback otherwise.</p>
 *
 * <p>A chartex chart's Choice (a waterfall, a histogram, ...) has a rule of its own, which the
 * outputs that draw apply through {@link #selectedBranch(AlternateContent, Part)}: by default it
 * is drawn where a chartex renderer draws it, its Fallback otherwise
 * ({@link org.docx4j.model.chart.ChartExBranch}, CR-039).</p>
 *
 * <p>Reading for text has a rule of its own, the ISO rule over the published understood set
 * ({@link McPurpose#TEXT}, {@link #selectedBranch(McPurpose, AlternateContent)}; CR-046): a
 * branch docx4j can read is not always one it can draw.</p>
 *
 * @since 17.2.0 (CR-021 phase 1)
 */
public final class McSelection {

	private static final Logger log = LoggerFactory.getLogger(McSelection.class);

	/** The property naming the {@code Requires} prefixes docx4j claims to understand,
	 *  whitespace-separated; empty (the default) prefers the Fallback. */
	public static final String PROPERTY = "docx4j.jaxb.mc.preferChoice";

	private McSelection() {}

	/** The value of {@link #PROPERTY}, empty when unset. */
	public static String preferredChoiceRequires() {
		return Docx4jProperties.getProperty(PROPERTY, "");
	}

	/**
	 * Whether an {@code mc:Choice} with this {@code Requires} is one docx4j draws in
	 * preference to the {@code mc:Fallback}: every prefix it names must be listed in
	 * {@link #PROPERTY}.
	 */
	public static boolean prefersChoice(String requires) {
		if (requires == null || requires.trim().length() == 0) return false;
		String preferred = preferredChoiceRequires();
		if (preferred == null || preferred.trim().length() == 0) return false;
		Set<String> ok = new HashSet<String>(Arrays.asList(preferred.trim().split("\\s+")));
		for (String needed : requires.trim().split("\\s+")) {
			if (!ok.contains(needed)) return false;
		}
		return true;
	}

	/**
	 * Whether the object is the document's own markup rather than a branch of an
	 * mc:AlternateContent: true where its paragraph is reached first going up, false where
	 * an mc:Choice or mc:Fallback is, or where the object has no parents to ask (one
	 * unmarshalled on its own, as the XSLT pathway's are).  The FO exporter needs it for a
	 * VML text box: where the VML is a DrawingML shape's Fallback, Word lays out the Choice,
	 * and the two measure a text-relative position from different places.
	 *
	 * @since 17.3.1
	 */
	public static boolean isOwnContent(Object o) {
		Object p = o;
		for (int i = 0; i < 64 && p instanceof org.jvnet.jaxb.lang.Child; i++) {
			p = ((org.jvnet.jaxb.lang.Child) p).getParent();
			if (p instanceof AlternateContent || p instanceof AlternateContent.Fallback
					|| p instanceof AlternateContent.Choice) return false;
			if (p instanceof org.docx4j.wml.P) return true;
		}
		return false;
	}

	/**
	 * The branch docx4j draws: an {@link AlternateContent.Choice} or the
	 * {@link AlternateContent.Fallback}, or null for an element with no branch at all.
	 */
	public static Object selectedBranch(AlternateContent ac) {
		return selectedBranch(ac, null);
	}

	/**
	 * The branch an output draws, for an element in the host's content: as
	 * {@link #selectedBranch(AlternateContent)}, and a chartex chart's Choice where
	 * {@link org.docx4j.model.chart.ChartExBranch} takes it (by default, where a chartex renderer
	 * draws the chart).  A null host is {@link #selectedBranch(AlternateContent)}.
	 *
	 * @param host the part whose relationships the element's content refers to (an output's
	 *     current part)
	 * @since 17.3.3 (CR-039)
	 */
	public static Object selectedBranch(AlternateContent ac, Part host) {
		if (ac == null) return null;
		for (AlternateContent.Choice choice : ac.getChoice()) {
			if (prefersChoice(choice.getRequires()) || org.docx4j.model.chart.ChartExBranch.takesChoice(host, choice)) {
				if (log.isDebugEnabled()) log.debug("mc:AlternateContent: selecting Choice Requires=" + choice.getRequires());
				return choice;
			}
		}
		if (ac.getFallback() != null) return ac.getFallback();
		if (!ac.getChoice().isEmpty()) {
			log.warn("mc:AlternateContent with no mc:Fallback: taking its first mc:Choice (Requires="
					+ ac.getChoice().get(0).getRequires() + ")");
			return ac.getChoice().get(0);
		}
		return null;
	}

	/** The content of {@link #selectedBranch}; empty (never null) when there is none. */
	public static List<Object> select(AlternateContent ac) {
		Object branch = selectedBranch(ac);
		if (branch instanceof AlternateContent.Choice) return ((AlternateContent.Choice) branch).getAny();
		if (branch instanceof AlternateContent.Fallback) return ((AlternateContent.Fallback) branch).getAny();
		return Collections.emptyList();
	}

	/**
	 * A content list with each {@code mc:AlternateContent} in it replaced by the content
	 * of the branch docx4j draws ({@link #select}), recursively - so a caller iterating a
	 * shape tree or a paragraph's runs by {@code instanceof} sees the chosen branch's
	 * objects in place of the element, and never both branches.  Other entries are
	 * passed through as they are (wrapped or not).  Never null.
	 *
	 * @since 17.2.0 (CR-021 phase 3)
	 */
	public static List<Object> selectedContent(List<Object> content) {
		if (content == null) return new ArrayList<Object>();
		List<Object> result = new ArrayList<Object>(content.size());
		for (Object o : content) {
			Object u = o;
			if (u instanceof jakarta.xml.bind.JAXBElement) u = ((jakarta.xml.bind.JAXBElement<?>) u).getValue();
			if (u instanceof AlternateContent) {
				result.addAll(selectedContent(select((AlternateContent) u)));
			} else {
				result.add(o);
			}
		}
		return result;
	}

	/**
	 * The branches a tree walk in this mode visits, as the Choice and Fallback objects
	 * themselves (their content is the next level down): in {@link McMode#READ} the one
	 * {@link #selectedBranch}; in {@link McMode#ALL} every Choice in document order, then
	 * the Fallback if there is one.
	 */
	public static List<Object> branches(AlternateContent ac, McMode mode) {
		List<Object> list = new ArrayList<Object>();
		if (ac == null) return list;
		if (mode == McMode.ALL) {
			list.addAll(ac.getChoice());
			if (ac.getFallback() != null) list.add(ac.getFallback());
		} else {
			Object branch = selectedBranch(ac);
			if (branch != null) list.add(branch);
		}
		return list;
	}

	// --- the TEXT purpose (CR-046 section 2.2) -----------------------------------------------------

	/** The published understood set, on the classpath (its README states the rule). */
	public static final String UNDERSTOOD_NAMESPACES_RESOURCE = "org/docx4j/jaxb/understood-namespaces.txt";

	private static volatile Set<String> understood;

	/**
	 * The understood set a reader of text selects with (WD REQ-306): the namespaces whose content
	 * docx4j's reader can reach, as published in {@value #UNDERSTOOD_NAMESPACES_RESOURCE}, which the
	 * docx4j family's ports copy. Unmodifiable, in the file's order.
	 *
	 * @since 17.3.4 (CR-046 phase 1)
	 */
	public static Set<String> understoodNamespaces() {
		Set<String> set = understood;
		if (set != null) return set;
		Set<String> read = new LinkedHashSet<String>();
		try (java.io.InputStream is = McSelection.class.getResourceAsStream("understood-namespaces.txt")) {
			if (is == null) throw new IllegalStateException("Missing resource " + UNDERSTOOD_NAMESPACES_RESOURCE);
			java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
			for (String line; (line = reader.readLine()) != null; ) {
				if (!line.trim().isEmpty()) read.add(line.trim());
			}
		} catch (java.io.IOException e) {
			throw new IllegalStateException("Couldn't read " + UNDERSTOOD_NAMESPACES_RESOURCE, e);
		}
		understood = set = Collections.unmodifiableSet(read);
		return set;
	}

	/**
	 * What each Choice's {@code Requires} prefixes resolved to where it was unmarshalled, by identity:
	 * JAXB keeps the attribute's string, not the declarations in scope at the element.
	 */
	private static final Map<AlternateContent.Choice, List<String>> REQUIRES_NAMESPACES =
			Collections.synchronizedMap(new WeakHashMap<AlternateContent.Choice, List<String>>());

	/**
	 * Records the namespace URIs a Choice's {@code Requires} prefixes resolve to, through the
	 * declarations in force at the element; the unmarshaller listener calls it while its reader is
	 * at the element. A null context records nothing.
	 *
	 * @since 17.3.4 (CR-046 phase 1)
	 */
	public static void recordRequiresNamespaces(AlternateContent.Choice choice, javax.xml.namespace.NamespaceContext context) {
		if (choice == null || context == null) return;
		List<String> prefixes = requiresPrefixes(choice.getRequires());
		if (prefixes.isEmpty()) return;
		List<String> uris = new ArrayList<String>(prefixes.size());
		for (String prefix : prefixes) {
			String uri = context.getNamespaceURI(prefix);
			uris.add(uri == null || uri.isEmpty() ? null : uri);
		}
		REQUIRES_NAMESPACES.put(choice, Collections.unmodifiableList(uris));
	}

	/**
	 * The namespace URIs a Choice's {@code Requires} names, one per prefix, in order; null for a
	 * prefix that resolves to nothing; empty for an empty or whitespace-only {@code Requires}.
	 *
	 * <p>Matched by URI (WD REQ-303): for a Choice unmarshalled from a part, through the namespace
	 * declarations in scope at the element, recorded at unmarshal. Where none were recorded - a
	 * Choice built in code or deep-copied, one unmarshalled from a DOM (the binder and
	 * {@code mc-preprocessor.xslt} pathways) - docx4j's conventional prefix table
	 * ({@link NamespacePrefixMappings}) is used instead, as REQ-303 permits; a producer that binds a
	 * namespace to an unconventional prefix is then not understood there.</p>
	 *
	 * @since 17.3.4 (CR-046 phase 1)
	 */
	public static List<String> requiresNamespaces(AlternateContent.Choice choice) {
		if (choice == null) return Collections.emptyList();
		List<String> recorded = REQUIRES_NAMESPACES.get(choice);
		if (recorded != null) return recorded;
		List<String> prefixes = requiresPrefixes(choice.getRequires());
		List<String> uris = new ArrayList<String>(prefixes.size());
		NamespacePrefixMappings table = new NamespacePrefixMappings();
		for (String prefix : prefixes) {
			String uri = table.getNamespaceURI(prefix);
			uris.add(uri == null || uri.isEmpty() ? null : uri);
		}
		return uris;
	}

	private static List<String> requiresPrefixes(String requires) {
		if (requires == null || requires.trim().isEmpty()) return Collections.emptyList();
		return Arrays.asList(requires.trim().split("\\s+"));
	}

	/**
	 * Whether a reader of text understands this Choice (WD REQ-302 to REQ-304): its {@code Requires}
	 * names at least one prefix, and every prefix resolves to a namespace in
	 * {@link #understoodNamespaces()}.
	 *
	 * @since 17.3.4 (CR-046 phase 1)
	 */
	public static boolean understands(AlternateContent.Choice choice) {
		List<String> uris = requiresNamespaces(choice);
		if (uris.isEmpty()) return false; // REQ-304
		Set<String> set = understoodNamespaces();
		for (String uri : uris) if (uri == null || !set.contains(uri)) return false;
		return true;
	}

	/**
	 * The branch selected for a purpose: for {@link McPurpose#TEXT}, the first Choice
	 * {@link #understands understood}, else the Fallback, else the first Choice of an element with
	 * Choices only (WD REQ-305: a reading rule; CR-021 keeps such an element whole at load); for
	 * {@link McPurpose#DRAW}, {@link #selectedBranch(AlternateContent)}. Null for an element with no
	 * branch at all. {@link McPurpose#ALL} selects every branch: use {@link #branches(McPurpose, AlternateContent)}.
	 *
	 * @since 17.3.4 (CR-046 phase 1)
	 */
	public static Object selectedBranch(McPurpose purpose, AlternateContent ac) {
		if (purpose == McPurpose.ALL) throw new IllegalArgumentException("McPurpose.ALL selects every branch: use branches()");
		if (purpose != McPurpose.TEXT) return selectedBranch(ac);
		if (ac == null) return null;
		for (AlternateContent.Choice choice : ac.getChoice()) {
			if (understands(choice)) return choice;
		}
		if (ac.getFallback() != null) return ac.getFallback();
		if (!ac.getChoice().isEmpty()) {
			if (log.isDebugEnabled()) log.debug("mc:AlternateContent with no understood mc:Choice and no mc:Fallback: reading its first mc:Choice (Requires="
					+ ac.getChoice().get(0).getRequires() + ")");
			return ac.getChoice().get(0);
		}
		return null;
	}

	/** The content of {@link #selectedBranch(McPurpose, AlternateContent)}; empty (never null) when there is none. @since 17.3.4 */
	public static List<Object> select(McPurpose purpose, AlternateContent ac) {
		Object branch = selectedBranch(purpose, ac);
		if (branch instanceof AlternateContent.Choice) return ((AlternateContent.Choice) branch).getAny();
		if (branch instanceof AlternateContent.Fallback) return ((AlternateContent.Fallback) branch).getAny();
		return Collections.emptyList();
	}

	/**
	 * The branches a walk for this purpose visits: every Choice then the Fallback for
	 * {@link McPurpose#ALL}, else the one selected.
	 *
	 * @since 17.3.4 (CR-046 phase 1)
	 */
	public static List<Object> branches(McPurpose purpose, AlternateContent ac) {
		if (purpose == McPurpose.ALL) return branches(ac, McMode.ALL);
		List<Object> list = new ArrayList<Object>();
		Object branch = selectedBranch(purpose, ac);
		if (branch != null) list.add(branch);
		return list;
	}
}
