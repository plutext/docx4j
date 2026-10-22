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
package org.docx4j.convert.out.common.preprocess;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.McMode;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.McSplice;
import org.docx4j.jaxb.ResolvedAlternateContent;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves each block-, row- and cell-level {@code mc:AlternateContent} of the exporters' working
 * copy to the branch the exporter draws (CR-051 D6: {@code ConversionFeatures.PP_COMMON_RESOLVE_ALTERNATE_CONTENT}),
 * so that no later preprocessing step and no exporter meets such an element, in either
 * {@link org.docx4j.jaxb.McLoadMode} mode.  In {@code word} mode a loaded part holds none but
 * those WD REQ-305 keeps whole; in {@code preserve} mode, and for content that never passed a
 * part's unmarshal (CR-051 section 3.4: fragments from {@code XmlUtils}, the trees OpenDoPE's
 * binding sets, an {@code altChunk}'s conversion, diffx's output), it holds them whole, and the
 * exporters' sites that read a container's list by type would skip their content (CR-051 section
 * 10.2).
 *
 * <p>The rule is the host-aware drawing rule, {@link McSelection#selectedBranch(AlternateContent, Part)}
 * with the part: the one the XSLT templates apply, which takes the first Choice for REQ-305's
 * element.  The finder is a {@link TraversalUtil} walk in {@link McMode#DRAW} of each part the copy
 * holds (the main document part, the headers, the footers, the footnotes, the endnotes and the
 * comments), recording each element whose container ({@link McSplice#effectiveContainer}) is in
 * the splice's scope; DRAW descends into the drawn branch only, so an element in a discarded
 * branch is never recorded.  The splice is {@link McSplice#apply}, outermost first through the live
 * parent.  Run twice (the FO header and footer pre-pass runs {@code Preprocess} again, in place),
 * the second run finds nothing.</p>
 *
 * <p>Meant for the working copy the preprocessing makes ({@code PP_COMMON_DEEP_COPY}).  With that
 * feature off it resolves the caller's package permanently, which in {@code preserve} mode loses
 * what the mode keeps: an INFO line then names each part it changed.</p>
 *
 * @since 17.3.4 (CR-051 phase 1)
 */
public class ResolveAlternateContent {

	private static Logger log = LoggerFactory.getLogger(ResolveAlternateContent.class);

	/**
	 * Resolves the package's drawn parts.
	 *
	 * @param wmlPackage the working copy (or, with the deep copy off, the caller's package)
	 * @param inPlace true where the package is the caller's own, not a copy: each part changed is then logged at INFO
	 * @return the number of elements resolved
	 */
	public static int process(WordprocessingMLPackage wmlPackage, boolean inPlace) {
		int n = 0;
		for (JaxbXmlPart<?> part : drawnParts(wmlPackage)) {
			List<ResolvedAlternateContent> records = process(part);
			n += records.size();
			if (!records.isEmpty() && inPlace) {
				log.info(part.getPartName().getName() + ": " + records.size()
						+ " mc:AlternateContent resolved to the branch the exporter draws, in the caller's own package (PP_COMMON_DEEP_COPY is off)");
			}
		}
		if (log.isDebugEnabled()) log.debug("Resolved " + n + " mc:AlternateContent for the conversion");
		return n;
	}

	/** Resolves one part's elements; the records of what was taken out, empty where it held none. */
	public static List<ResolvedAlternateContent> process(final JaxbXmlPart<?> part) {
		Object root = part.getJaxbElement();
		if (root == null) return new ArrayList<ResolvedAlternateContent>();
		final List<AlternateContent> found = new ArrayList<AlternateContent>();
		new TraversalUtil(root, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof AlternateContent && McSplice.inScope(McSplice.effectiveContainer(o))) {
					found.add((AlternateContent) o);
				}
				return null;
			}
		}, McMode.DRAW);
		if (found.isEmpty()) return new ArrayList<ResolvedAlternateContent>();
		List<ResolvedAlternateContent> records = McSplice.apply(found, new McSplice.Rule() {
			@Override
			public Object select(AlternateContent ac) {
				return McSelection.selectedBranch(ac, part);
			}
		});
		// the prefixes of the promoted Choices, declared on the root as a save would need (CR-051 section 3.2)
		McSplice.addIgnorable(root, McSplice.ignorablePrefixes(records));
		return records;
	}

	/**
	 * The parts both exporter families draw, as the copy holds them: the main document part, then
	 * the headers, footers, footnotes, endnotes and comments it relates to.  The glossary document
	 * is not drawn.
	 */
	static List<JaxbXmlPart<?>> drawnParts(WordprocessingMLPackage pkg) {
		Set<JaxbXmlPart<?>> parts = new LinkedHashSet<JaxbXmlPart<?>>();
		MainDocumentPart main = pkg == null ? null : pkg.getMainDocumentPart();
		if (main == null) return new ArrayList<JaxbXmlPart<?>>();
		parts.add(main);
		RelationshipsPart rels = main.getRelationshipsPart(false);
		if (rels != null && rels.getRelationships() != null) {
			for (Relationship rel : rels.getRelationships().getRelationship()) {
				if ("External".equals(rel.getTargetMode())) continue;
				Part part = rels.getPart(rel);
				if (part instanceof HeaderPart || part instanceof FooterPart || part instanceof FootnotesPart
						|| part instanceof EndnotesPart || part instanceof CommentsPart) {
					parts.add((JaxbXmlPart<?>) part);
				}
			}
		}
		return new ArrayList<JaxbXmlPart<?>>(parts);
	}
}
