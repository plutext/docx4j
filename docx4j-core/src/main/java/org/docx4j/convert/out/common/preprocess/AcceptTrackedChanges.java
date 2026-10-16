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
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.McMode;
import org.docx4j.model.revisions.TrackedChanges;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tr;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The accepted view of a document's tracked changes, for a conversion that should lay out what the
 * document says rather than its markup (CR-012:
 * {@code ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES}).
 *
 * <p>Since 17.3.3 this is {@link TrackedChanges#acceptAllForConversion(WordprocessingMLPackage)}
 * (CR-043): every story is accepted - the main document, headers, footers, footnotes, endnotes and
 * comments (CR-043 D2), with text boxes in both {@code mc:AlternateContent} branches, content
 * controls and equations - as Word's Accept All does it: deletions go, insertions and moves stay, a
 * deleted paragraph mark joins its paragraph with the next (the joined paragraph keeps the first's
 * {@code w14:paraId}, by which Paginate keys it, and takes the next's properties), deleted rows
 * go, formatting records are dropped (the current formatting is what the document shows), a field
 * or note goes whole with its reference. Before 17.3.3 only the main document part's runs, marks
 * and rows were accepted. Meant for the working copy the preprocessing makes
 * ({@code PP_COMMON_DEEP_COPY}): no part, relationship or content type is removed from it, since it
 * shares them with the document it was copied from.</p>
 *
 * @since 17.2.0
 */
public class AcceptTrackedChanges {

	private static Logger log = LoggerFactory.getLogger(AcceptTrackedChanges.class);

	public static void process(WordprocessingMLPackage wmlPackage) {
		int n = TrackedChanges.acceptAllForConversion(wmlPackage);
		if (log.isDebugEnabled()) log.debug("Accepted " + n + " tracked changes for the conversion");
	}

	/**
	 * Accept the tracked changes in this content list (recursively), whose items belong
	 * to {@code container}: runs, deleted paragraph marks and deleted rows only, as before 17.3.3.
	 *
	 * @deprecated since 17.3.3: use {@link TrackedChanges#acceptAll(WordprocessingMLPackage)}, which
	 * accepts every kind of change in every story, as Word does (CR-043)
	 */
	@Deprecated
	public static void process(List<Object> content, Object container) {

		if (content == null) return;

		for (int i = 0; i < content.size(); i++) {
			Object o = content.get(i);
			String name = (o instanceof JAXBElement ? ((JAXBElement<?>) o).getName().getLocalPart() : null);
			Object v = XmlUtils.unwrap(o);

			if (v instanceof RunDel || (v instanceof RunTrackChange && "moveFrom".equals(name))) {
				content.remove(i--);
				continue;
			}
			if (v instanceof RunIns || (v instanceof RunTrackChange && "moveTo".equals(name))) {
				List<Object> inner = new ArrayList<Object>(v instanceof RunIns
						? ((RunIns) v).getCustomXmlOrSmartTagOrSdt() : ((RunTrackChange) v).getAccOrBarOrBox());
				content.remove(i);
				content.addAll(i, inner);
				for (Object item : inner) {
					Object iv = XmlUtils.unwrap(item);
					if (iv instanceof Child) ((Child) iv).setParent(container);
				}
				i--; // the unwrapped items are looked at in turn
				continue;
			}
			if (v instanceof Tr && ((Tr) v).getTrPr() != null && ((Tr) v).getTrPr().getDel() != null) {
				content.remove(i--);
				continue;
			}
			descend(v);
		}

		// paragraph marks: joined after the runs are settled, so the joined content is the
		// accepted content
		for (int i = 0; i < content.size() - 1; i++) {
			Object v = XmlUtils.unwrap(content.get(i));
			if (!(v instanceof P) || !markDeleted((P) v)) continue;
			Object nextV = XmlUtils.unwrap(content.get(i + 1));
			if (!(nextV instanceof P)) continue;
			P p = (P) v;
			P next = (P) nextV;
			for (Object item : next.getContent()) {
				Object iv = XmlUtils.unwrap(item);
				if (iv instanceof Child) ((Child) iv).setParent(p);
			}
			p.getContent().addAll(next.getContent());
			p.setPPr(next.getPPr());
			if (p.getPPr() != null) p.getPPr().setParent(p);
			content.remove(i + 1);
			i--; // the surviving mark may be deleted too
			if (log.isDebugEnabled()) log.debug("Joined paragraph " + p.getParaId() + " with the next: its mark is deleted");
		}
	}

	private static boolean markDeleted(P p) {
		return p.getPPr() != null && p.getPPr().getRPr() != null && p.getPPr().getRPr().getDel() != null;
	}

	private static void descend(Object v) {

		if (v instanceof ContentAccessor) {
			process(((ContentAccessor) v).getContent(), v);
		} else if (v instanceof SdtElement) {
			if (((SdtElement) v).getSdtContent() != null) {
				process(((SdtElement) v).getSdtContent().getContent(), ((SdtElement) v).getSdtContent());
			}
		} else if (v != null && !(v instanceof String)) {
			// a run's drawing, say: down to the text boxes in it
			// CR-021: ALL - rewrites the exporter's copy, so every branch is accepted
			List<Object> children = TraversalUtil.getChildrenImpl(v, McMode.ALL);
			if (children == null) return;
			for (Object child : children) descend(XmlUtils.unwrap(child));
		}
	}
}
