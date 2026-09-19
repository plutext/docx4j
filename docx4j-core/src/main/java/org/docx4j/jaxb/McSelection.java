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
import java.util.List;
import java.util.Set;

import org.docx4j.Docx4jProperties;
import org.docx4j.mce.AlternateContent;
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
 * @since 17.1.1 (CR-021 phase 1)
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
	 * The branch docx4j draws: an {@link AlternateContent.Choice} or the
	 * {@link AlternateContent.Fallback}, or null for an element with no branch at all.
	 */
	public static Object selectedBranch(AlternateContent ac) {
		if (ac == null) return null;
		for (AlternateContent.Choice choice : ac.getChoice()) {
			if (prefersChoice(choice.getRequires())) {
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
	 * @since 17.1.1 (CR-021 phase 3)
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
}
