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
package org.docx4j.model.datastorage;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * CR-063 D11 (check 71): what the binders write for a picture control ({@code w:picture}) whose bound
 * node is empty. Word's form, as Word writes it for such a control on opening (and for a control bound
 * through its XML Mapping Pane, or made by Office JS's {@code insertContentControl('Picture')}):
 * {@code w:showingPlcHdr}, and the content one run holding an inline drawing, 1270000 x 1270000 EMU,
 * of a placeholder picture. Word's own picture is a plain pale-blue 200 x 200 PNG (the icon on it is
 * drawn by Word); docx4j's is the same blue with a grey, dash-edged inner square
 * ({@code picture-placeholder.png}, Jason's choice, 2026-10-25). In Word the picture is seen only until
 * the file is opened, since Word replaces an empty-node picture control's content with its own form
 * whatever the file holds (check 71, cases 1 and 2); elsewhere (docx4j's PDF and HTML output, other
 * editors) it is what shows. The image part is added once per source part and reused; a control
 * already showing a placeholder drawing (the flag on, a blip in the content, docx4j's or Word's own)
 * is left as it is, so a rebinding changes nothing (core-ts's rule).
 *
 * <p>Before this, the XSLT binder left the content empty (Word accepted it and filled its form) and the
 * NonXSLT and StAX binders wrote the text placeholder run, which is content Word repairs (check 71,
 * bisect).</p>
 *
 * @since 17.3.5
 */
public final class PicturePlaceholder {

	private static final Logger log = LoggerFactory.getLogger(PicturePlaceholder.class);

	/** The placeholder picture, on the classpath. */
	public static final String RESOURCE = "org/docx4j/model/datastorage/picture-placeholder.png";

	/** The drawing's extent, Word's (check 71: 1270000 EMU, 1.39 inch, square). */
	public static final long EXTENT = 1270000L;

	private static final AtomicInteger nextId = new AtomicInteger(1);
	private static byte[] bytes;

	private PicturePlaceholder() {}

	/** The placeholder picture's bytes, read once. */
	static synchronized byte[] bytes() throws Exception {
		if (bytes == null) {
			InputStream is = ResourceUtils.getResource(RESOURCE);
			try {
				bytes = IOUtils.toByteArray(is);
			} finally {
				is.close();
			}
		}
		return bytes;
	}

	/** True where a bound value is nothing to show: null, empty or whitespace. */
	public static boolean isEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}

	/**
	 * The relationship from the source part to the image part holding the placeholder picture: the one
	 * already there (a related image part whose bytes are the picture's), else the part added.
	 */
	public static String ensureImageRelId(WordprocessingMLPackage pkg, Part sourcePart) throws Exception {
		byte[] picture = bytes();
		RelationshipsPart rels = sourcePart.getRelationshipsPart();
		if (rels != null) {
			for (Relationship rel : rels.getRelationships().getRelationship()) {
				if (!Namespaces.IMAGE.equals(rel.getType())) continue;
				Part target = rels.getPart(rel);
				if (target instanceof BinaryPartAbstractImage) {
					byte[] held = ((BinaryPartAbstractImage) target).getBytes();
					if (held != null && held.length == picture.length && Arrays.equals(held, picture)) {
						return rel.getId();
					}
				}
			}
		}
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, sourcePart, picture);
		log.info("Picture placeholder image part added: " + part.getPartName().getName());
		return part.getRelLast().getId();
	}

	/** The run holding the placeholder drawing (Word's form: extent 1270000 EMU square), the image part ensured. */
	public static R placeholderRun(WordprocessingMLPackage pkg, Part sourcePart) throws Exception {
		String relId = ensureImageRelId(pkg, sourcePart);
		int id = nextId.getAndIncrement();
		Inline inline = BinaryPartAbstractImage.createImageInline("Picture " + id, null, relId, id, id, EXTENT, EXTENT, false);
		ObjectFactory factory = Context.getWmlObjectFactory();
		Drawing drawing = factory.createDrawing();
		drawing.getAnchorOrInline().add(inline);
		R run = factory.createR();
		run.getContent().add(drawing);
		return run;
	}

	/**
	 * The content for a control's {@code w:sdtContent}: the run, in a paragraph where the control is
	 * block-level (as Word's form has it: check 71, cases 5 and 8).
	 */
	public static Object placeholderContent(WordprocessingMLPackage pkg, Part sourcePart, boolean blockLevel) throws Exception {
		R run = placeholderRun(pkg, sourcePart);
		if (!blockLevel) return run;
		P p = Context.getWmlObjectFactory().createP();
		p.getContent().add(run);
		return p;
	}

	/**
	 * The content as a DOM fragment, for bind.xslt, shaped for the control's place as
	 * {@code xpathInjectImage} shapes an image: {@code w:p} under a body or cell, {@code w:tc/w:p} under
	 * a row, the run in a paragraph, and under {@code w:sdtContent} as its child says.
	 */
	public static DocumentFragment placeholderFragment(WordprocessingMLPackage pkg, Part sourcePart,
			String sdtParent, String contentChild) throws Exception {
		R run = placeholderRun(pkg, sourcePart);
		ObjectFactory factory = Context.getWmlObjectFactory();
		Object out;
		if ("tr".equals(sdtParent)) {
			P p = factory.createP();
			p.getContent().add(run);
			Tc tc = factory.createTc();
			tc.getContent().add(p);
			out = tc;
		} else if ("body".equals(sdtParent) || "tc".equals(sdtParent)
				|| ("sdtContent".equals(sdtParent) && "p".equals(contentChild))) {
			P p = factory.createP();
			p.getContent().add(run);
			out = p;
		} else {
			out = run; // a paragraph, or sdtContent whose child is a run
		}
		Document document = XmlUtils.marshaltoW3CDomDocument(out);
		DocumentFragment frag = document.createDocumentFragment();
		frag.appendChild(document.getDocumentElement());
		return frag;
	}

	/** True where the control's {@code w:sdtPr} has {@code w:showingPlcHdr} (and it isn't false). */
	public static boolean isShowingPlaceholder(SdtPr sdtPr) {
		if (sdtPr == null) return false;
		for (Object o : sdtPr.getRPrOrAliasOrLock()) {
			if (o instanceof jakarta.xml.bind.JAXBElement
					&& "showingPlcHdr".equals(((jakarta.xml.bind.JAXBElement<?>) o).getName().getLocalPart())) {
				Object v = ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
				return !(v instanceof org.docx4j.wml.BooleanDefaultTrue) || ((org.docx4j.wml.BooleanDefaultTrue) v).isVal();
			}
		}
		return false;
	}

	/** {@code w:showingPlcHdr} set or removed. */
	public static void setShowingPlaceholder(SdtPr sdtPr, boolean showing) {
		if (sdtPr != null) sdtPr.setShowingPlcHdr(showing);
	}
}
