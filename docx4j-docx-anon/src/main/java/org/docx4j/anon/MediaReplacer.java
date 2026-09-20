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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageBmpPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageTiffPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces every image with a placeholder: raster parts (png, gif, jpeg, bmp,
 * tiff) have their bytes replaced in place with a 2x2 PNG (Word reads the
 * bytes, not the extension, and scales the pixels to the drawing's extent);
 * every other image class (EMF and WMF, which can carry text as glyph runs -
 * see CR-011 - EPS, JPEG XR, WebP) is removed and every relationship which
 * pointed at it retargeted to one shared placeholder PNG part, so the drawing
 * still shows a picture; an SVG part (XML, so possibly text) becomes a blank
 * 2x2 SVG.
 *
 * @since 17.2.0
 */
public class MediaReplacer {

	private static final Logger log = LoggerFactory.getLogger(MediaReplacer.class);

	/** a 2x2 PNG */
	static final byte[] PNG_IMAGE_DATA = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAIAAAACAgMAAAAP2OW3AAAADFBMVEUDAP//AAAA/wb//AAD4Tw1AAAACXBIWXMAAAsTAAALEwEAmpwYAAAADElEQVQI12NwYNgAAAF0APHJnpmVAAAAAElFTkSuQmCC");

	static final String BLANK_SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"2\" height=\"2\" viewBox=\"0 0 2 2\"/>";

	public static final String PLACEHOLDER_PART_NAME = "/word/media/anon-placeholder.png";
	/** the labelled image a removed OLE object's or control's picture becomes */
	public static final String OBJECT_PLACEHOLDER_PART_NAME = "/word/media/anon-object-removed.png";

	private final WordprocessingMLPackage pkg;
	private final Set<String> objectPreviews;
	private ImagePngPart placeholder;
	private ImagePngPart objectPlaceholder;

	public MediaReplacer(WordprocessingMLPackage pkg) {
		this(pkg, Collections.<String>emptySet());
	}

	/**
	 * @param objectPreviews "partName#relId" of the pictures which stood in for a
	 *                       removed OLE object or control: these get the labelled placeholder
	 */
	public MediaReplacer(WordprocessingMLPackage pkg, Set<String> objectPreviews) {
		this.pkg = pkg;
		this.objectPreviews = objectPreviews;
	}

	/** true if some relationship to this part is a removed object's picture */
	private boolean isObjectPreview(Part p) {
		if (objectPreviews.isEmpty()) return false;
		List<Base> sources = new ArrayList<Base>();
		sources.add(pkg);
		sources.addAll(pkg.getParts().getParts().values());
		for (Base source : sources) {
			RelationshipsPart rp = source.getRelationshipsPart();
			if (rp == null || rp.getRelationships() == null) continue;
			for (Relationship r : rp.getRelationships().getRelationship()) {
				if ("External".equals(r.getTargetMode())) continue;
				if (isObjectPreview(source, r) && rp.isTarget(p.getPartName(), r)) return true;
			}
		}
		return false;
	}

	private boolean isObjectPreview(Base source, Relationship r) {
		String sourceName = source instanceof Part ? ((Part) source).getPartName().getName() : "";
		return objectPreviews.contains(sourceName + "#" + r.getId());
	}

	/**
	 * @return a line for the report saying what was done
	 */
	public String replace(Part p) throws Docx4JException {

		if (p instanceof ImagePngPart
				|| p instanceof ImageGifPart
				|| p instanceof ImageJpegPart
				|| p instanceof ImageBmpPart
				|| p instanceof ImageTiffPart) {
			if (isObjectPreview(p)) {
				((BinaryPart) p).setBinaryData(Placeholders.objectRemovedPng());
				return "replaced with the labelled placeholder (it was a removed object's picture)";
			}
			((BinaryPart) p).setBinaryData(PNG_IMAGE_DATA);
			return "replaced with 2x2 pixels";
		}

		if (p instanceof DefaultXmlPart) {
			// an SVG
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				((DefaultXmlPart) p).setDocument(
						dbf.newDocumentBuilder().parse(new ByteArrayInputStream(BLANK_SVG.getBytes(StandardCharsets.UTF_8))));
			} catch (Exception e) {
				throw new Docx4JException("blank svg", e);
			}
			return "replaced with a blank svg";
		}

		// a metafile, eps, jpeg xr, webp, or an image docx4j could not read:
		// point every relationship at the shared placeholder png (the labelled one where
		// the picture stood for a removed object) and drop the part
		boolean object = retargetToPlaceholder(p);
		return "relationships retargeted to " + (object ? OBJECT_PLACEHOLDER_PART_NAME : PLACEHOLDER_PART_NAME) + "; part removed";
	}

	/** @return true if any relationship went to the labelled object placeholder */
	private boolean retargetToPlaceholder(Part p) throws Docx4JException {

		boolean object = false;
		PartName oldName = p.getPartName();

		List<Base> sources = new ArrayList<Base>();
		sources.add(pkg);
		sources.addAll(pkg.getParts().getParts().values());
		for (Base source : sources) {
			RelationshipsPart rp = source.getRelationshipsPart();
			if (rp == null || rp.getRelationships() == null) continue;
			// a snapshot: creating a placeholder part adds a relationship to the main document part
			for (Relationship r : new ArrayList<Relationship>(rp.getRelationships().getRelationship())) {
				if ("External".equals(r.getTargetMode())) continue;
				if (rp.isTarget(oldName, r)) {
					boolean preview = isObjectPreview(source, r);
					ImagePngPart png = preview ? objectPlaceholder() : placeholder();
					object |= preview;
					r.setTarget(relativize(rp.getSourceURI(), png.getPartName().getURI()));
				}
			}
		}

		pkg.getParts().remove(oldName);
		pkg.getContentTypeManager().removeOverrideContentType(oldName);
		return object;
	}

	private ImagePngPart placeholder() throws Docx4JException {
		if (placeholder == null) placeholder = placeholderPart(PLACEHOLDER_PART_NAME, PNG_IMAGE_DATA);
		return placeholder;
	}

	private ImagePngPart objectPlaceholder() throws Docx4JException {
		if (objectPlaceholder == null) objectPlaceholder = placeholderPart(OBJECT_PLACEHOLDER_PART_NAME, Placeholders.objectRemovedPng());
		return objectPlaceholder;
	}

	private ImagePngPart placeholderPart(String name, byte[] bytes) throws Docx4JException {
		Part existing = pkg.getParts().get(new PartName(name));
		if (existing instanceof ImagePngPart) return (ImagePngPart) existing;
		ImagePngPart part = new ImagePngPart(new PartName(name));
		part.setBinaryData(bytes);
		// registers the part, its content type and a relationship from the main document part
		pkg.getMainDocumentPart().addTargetPart(part, AddPartBehaviour.REUSE_EXISTING);
		return part;
	}

	static String relativize(URI source, URI target) {
		String rel = URIHelper.relativizeURI(source, target).toString();
		if (source.getPath().equals("/") && rel.startsWith("/")) {
			rel = rel.substring(1);
		}
		return rel;
	}

	/**
	 * Removes a part from the package: every relationship which points at it (from
	 * the package or any part), its own targets recursively, its content-type
	 * override, and the part itself.
	 *
	 * @return the names of the parts removed (the part and, recursively, its targets)
	 */
	static List<PartName> removePart(WordprocessingMLPackage pkg, Part p) {

		List<PartName> removed = new ArrayList<PartName>();
		PartName name = p.getPartName();

		List<Base> sources = new ArrayList<Base>();
		sources.add(pkg);
		sources.addAll(pkg.getParts().getParts().values());
		for (Base source : sources) {
			RelationshipsPart rp = source.getRelationshipsPart();
			if (rp == null || rp.getRelationships() == null) continue;
			List<Relationship> toGo = new ArrayList<Relationship>();
			for (Relationship r : rp.getRelationships().getRelationship()) {
				if ("External".equals(r.getTargetMode())) continue;
				try {
					if (rp.isTarget(name, r)) toGo.add(r);
				} catch (Exception e) {
					log.debug("cannot resolve " + r.getTarget() + ": " + e);
				}
			}
			for (Relationship r : toGo) {
				rp.removeRelationship(r);
			}
		}

		// the part's own targets go with it (an embedded package's, an ActiveX part's binary)
		if (p.getRelationshipsPart() != null && p.getRelationshipsPart().getRelationships() != null) {
			for (Relationship r : new ArrayList<Relationship>(p.getRelationshipsPart().getRelationships().getRelationship())) {
				if ("External".equals(r.getTargetMode())) continue;
				Part target = p.getRelationshipsPart().getPart(r);
				if (target != null && target != p && stillReferencedElsewhere(pkg, target, p) == false) {
					removed.addAll(removePart(pkg, target));
				}
			}
		}

		pkg.getParts().remove(name);
		pkg.getContentTypeManager().removeOverrideContentType(name);
		removed.add(name);
		return removed;
	}

	/** true if some part other than {@code except} (or the package) has a relationship to {@code target} */
	private static boolean stillReferencedElsewhere(WordprocessingMLPackage pkg, Part target, Part except) {
		List<Base> sources = new ArrayList<Base>();
		sources.add(pkg);
		sources.addAll(pkg.getParts().getParts().values());
		for (Base source : sources) {
			if (source == except) continue;
			RelationshipsPart rp = source.getRelationshipsPart();
			if (rp == null || rp.getRelationships() == null) continue;
			for (Relationship r : rp.getRelationships().getRelationship()) {
				if ("External".equals(r.getTargetMode())) continue;
				try {
					if (rp.isTarget(target.getPartName(), r)) return true;
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return false;
	}

}
