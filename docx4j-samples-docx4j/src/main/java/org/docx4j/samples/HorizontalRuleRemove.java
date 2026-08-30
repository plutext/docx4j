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
package org.docx4j.samples;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.CTRect;
import org.docx4j.vml.officedrawing.STTrueFalse;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.jvnet.jaxb.lang.Child;

/**
 * Remove horizontal rules (the lines Word inserts when you type --- and press
 * enter, or which HTML import produces for &lt;hr/&gt;) from a docx.
 *
 * These are represented as a VML rect with o:hr="t", inside w:pict, eg:
 *
 * <pre>
 * &lt;w:p&gt;
 *   &lt;w:r&gt;
 *     &lt;w:pict&gt;
 *       &lt;v:rect o:hr="t" o:hrstd="t" o:hralign="center" style="width:0;height:1.5pt"/&gt;
 *     &lt;/w:pict&gt;
 *   &lt;/w:r&gt;
 * &lt;/w:p&gt;
 * </pre>
 *
 * The pict is removed from its run; where that leaves the paragraph with no
 * remaining content (the usual case - the rule is a paragraph of its own), the
 * whole paragraph is removed.  The main document part and any header/footer
 * parts are processed.
 */
public class HorizontalRuleRemove extends AbstractSample {

	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
		}
		try {
			getOutputFilePath(args);
		} catch (IllegalArgumentException e) {
			outputfilepath = System.getProperty("user.dir") + "/OUT_HorizontalRuleRemove.docx";
		}

		WordprocessingMLPackage wordMLPackage
				= WordprocessingMLPackage.load(new java.io.File(inputfilepath));

		int removed = process(wordMLPackage.getMainDocumentPart());

		// headers and footers too
		RelationshipsPart rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
		for (Relationship r : rp.getRelationships().getRelationship()) {
			if (r.getType().equals(Namespaces.HEADER)) {
				removed += process((HeaderPart) rp.getPart(r));
			} else if (r.getType().equals(Namespaces.FOOTER)) {
				removed += process((FooterPart) rp.getPart(r));
			}
		}

		System.out.println("Removed " + removed + " horizontal rule(s)");
		wordMLPackage.save(new java.io.File(outputfilepath));
		System.out.println("Saved: " + outputfilepath);
	}

	/**
	 * @return the number of horizontal rules removed from this part
	 */
	public static int process(ContentAccessor part) {

		// collect first, mutate after, so we don't disturb the traversal
		final List<P> candidates = new ArrayList<P>();
		new TraversalUtil(part.getContent(), new CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) {
					candidates.add((P) o);
				}
				return null;
			}
		});

		int removed = 0;
		for (P p : candidates) {
			if (removeHorizontalRules(p) > 0) {
				removed++;
				if (isEffectivelyEmpty(p)) {
					removeFromParent(p);
				}
			}
		}
		return removed;
	}

	/**
	 * Remove any horizontal-rule w:pict from the paragraph's runs.
	 *
	 * @return the number of picts removed
	 */
	private static int removeHorizontalRules(P p) {

		int removed = 0;
		for (Object o : p.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (!(u instanceof R)) continue;
			R run = (R) u;
			List<Object> toRemove = new ArrayList<Object>();
			for (Object rc : run.getContent()) {
				Object ru = XmlUtils.unwrap(rc);
				if (ru instanceof Pict && containsHrRect((Pict) ru)) {
					toRemove.add(rc);
				}
			}
			run.getContent().removeAll(toRemove);
			removed += toRemove.size();
		}
		return removed;
	}

	private static boolean containsHrRect(Pict pict) {

		for (Object o : pict.getAnyAndAny()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof CTRect) {
				STTrueFalse hr = ((CTRect) u).getHr();
				if (STTrueFalse.T.equals(hr) || STTrueFalse.TRUE.equals(hr)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * True if nothing displayable remains: no content, or only runs with no
	 * remaining content.
	 */
	private static boolean isEffectivelyEmpty(P p) {

		for (Object o : p.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof R) {
				if (!((R) u).getContent().isEmpty()) return false;
			} else {
				return false;
			}
		}
		return true;
	}

	private static void removeFromParent(P p) {

		Object parent = ((Child) p).getParent();
		List<Object> siblings;
		if (parent instanceof ContentAccessor) {
			siblings = ((ContentAccessor) parent).getContent();
		} else if (parent instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> l = (List<Object>) parent;
			siblings = l;
		} else {
			System.err.println("TODO: remove paragraph from " + parent.getClass().getName());
			return;
		}
		for (Object o : siblings) {
			if (XmlUtils.unwrap(o) == p) {
				siblings.remove(o);
				return;
			}
		}
	}

}
