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
 * The accepted view of the main document part's tracked changes, for a conversion that
 * should lay out what the document says rather than its markup (CR-012:
 * {@code ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES}):
 *
 * <ul>
 * <li>{@code w:del} and {@code w:moveFrom} content is removed; {@code w:ins} and
 *     {@code w:moveTo} are unwrapped into their parent.</li>
 * <li>A paragraph whose mark is deleted ({@code w:pPr/w:rPr/w:del}) is joined with the
 *     paragraph after it: the joined paragraph keeps the first one's content first and
 *     its {@code w14:paraId}, and takes the second one's properties, the mark that
 *     survives being the second's.</li>
 * <li>A table row whose {@code w:trPr} says {@code w:del} is removed.</li>
 * </ul>
 *
 * Formatting revisions ({@code w:rPrChange}, {@code w:pPrChange} and the like) need
 * nothing: the current properties are what the document shows.  Text boxes are reached
 * through their runs; headers, footers and notes are not touched.  Meant for the working
 * copy the preprocessing makes ({@code PP_COMMON_DEEP_COPY}).
 *
 * @since 17.1.1
 */
public class AcceptTrackedChanges {

	private static Logger log = LoggerFactory.getLogger(AcceptTrackedChanges.class);

	public static void process(WordprocessingMLPackage wmlPackage) {
		process(wmlPackage.getMainDocumentPart().getJaxbElement().getBody().getContent(),
				wmlPackage.getMainDocumentPart().getJaxbElement().getBody());
	}

	/**
	 * Accept the tracked changes in this content list (recursively), whose items belong
	 * to {@code container}.
	 */
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
			List<Object> children = TraversalUtil.getChildrenImpl(v);
			if (children == null) return;
			for (Object child : children) descend(XmlUtils.unwrap(child));
		}
	}
}
