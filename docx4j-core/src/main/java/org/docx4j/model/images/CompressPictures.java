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
package org.docx4j.model.images;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTRelativeRect;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ContentAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Word's Compress Pictures on a package (CR-029): each DrawingML picture's cropped areas
 * deleted from its bitmap, and the bitmap resampled to a resolution for the largest size it
 * is shown at. The document then carries smaller pictures, whatever it is converted to or
 * opened in; Word does the same when it saves, unless told not to.
 *
 * <pre>
 * CompressPictures.Report r = CompressPictures.compress(pkg, "220", true);
 * </pre>
 *
 * <p>What is left alone: a JPEG and a PNG are rewritten as the same format, so other
 * formats (GIF, TIFF, BMP, metafiles) are untouched; a picture whose bitmap ImageIO cannot
 * read; a VML picture ({@code w:pict}); and the crop of a bitmap which pictures crop
 * differently (it is still resampled, for the largest share any of them shows).</p>
 *
 * @since 17.3.0
 */
public final class CompressPictures {

	private static final Logger log = LoggerFactory.getLogger(CompressPictures.class);

	private CompressPictures() {
	}

	/** What changed. */
	public static final class Report {
		/** The picture parts rewritten. */
		public final List<String> parts = new ArrayList<String>();
		/** Their bytes before and after, summed. */
		public long bytesBefore, bytesAfter;
		/** How many pictures had their crops deleted. */
		public int cropsDeleted;

		@Override
		public String toString() {
			return parts.size() + " picture part(s) rewritten, " + bytesBefore + " -> " + bytesAfter
					+ " bytes; " + cropsDeleted + " crop(s) deleted";
		}
	}

	/** One use of a bitmap: the picture, where it is shown, how big. */
	private static final class Use {
		final Pic pic;
		final double widthPt, heightPt;

		Use(Pic pic, CTPositiveSize2D extent) {
			this.pic = pic;
			this.widthPt = extent == null ? 0 : extent.getCx() / 12700d;
			this.heightPt = extent == null ? 0 : extent.getCy() / 12700d;
		}

		PictureCrop crop() {
			CTBlipFillProperties bf = pic.getBlipFill();
			return bf == null ? PictureCrop.NONE : PictureCrop.of(bf.getSrcRect());
		}
	}

	/**
	 * @param pkg the package, changed in place
	 * @param resolution a value of {@link PictureCompression#RESOLUTION_PROPERTY}; null for
	 *        {@code document} (Word's Compress Pictures with the document's own settings)
	 * @param deleteCroppedAreas as Word's "Delete cropped areas of pictures"
	 */
	public static Report compress(WordprocessingMLPackage pkg, String resolution, boolean deleteCroppedAreas)
			throws Docx4JException {

		PictureCompression.Resolution res = PictureCompression.resolution(pkg, resolution == null ? "document" : resolution);
		Map<BinaryPart, List<Use>> uses = new IdentityHashMap<BinaryPart, List<Use>>();

		for (Part part : pkg.getParts().getParts().values()) {
			List<Object> content = storyContent(part);
			if (content == null) continue;
			new TraversalUtil(content, new TraversalUtil.CallbackImpl() {
				@Override
				public List<Object> apply(Object o) {
					Pic pic = null;
					CTPositiveSize2D extent = null;
					if (o instanceof Inline) {
						Inline i = (Inline) o;
						extent = i.getExtent();
						if (i.getGraphic() != null && i.getGraphic().getGraphicData() != null) pic = i.getGraphic().getGraphicData().getPic();
					} else if (o instanceof Anchor) {
						Anchor a = (Anchor) o;
						extent = a.getExtent();
						if (a.getGraphic() != null && a.getGraphic().getGraphicData() != null) pic = a.getGraphic().getGraphicData().getPic();
					}
					if (pic == null || pic.getBlipFill() == null || pic.getBlipFill().getBlip() == null) return null;
					String rid = pic.getBlipFill().getBlip().getEmbed();
					if (rid == null || rid.isEmpty() || part.getRelationshipsPart() == null) return null;
					Part target = part.getRelationshipsPart().getPart(rid);
					if (!(target instanceof BinaryPart)) return null;
					List<Use> list = uses.get(target);
					if (list == null) uses.put((BinaryPart) target, list = new ArrayList<Use>());
					list.add(new Use(pic, extent));
					return null;
				}
			});
		}

		Report report = new Report();
		for (Map.Entry<BinaryPart, List<Use>> e : uses.entrySet()) {
			compress(e.getKey(), e.getValue(), res, deleteCroppedAreas, report);
		}
		log.info("Compress Pictures: " + report);
		return report;
	}

	private static void compress(BinaryPart part, List<Use> uses, PictureCompression.Resolution res,
			boolean deleteCroppedAreas, Report report) {

		String type = part.getContentType();
		boolean jpeg = "image/jpeg".equals(type), png = "image/png".equals(type);
		if (!jpeg && !png) return;

		// the crop is deleted only where every picture of this bitmap crops it the same way
		PictureCrop crop = uses.get(0).crop();
		boolean sameCrop = true;
		for (Use u : uses) {
			if (!u.crop().signature().equals(crop.signature())) sameCrop = false;
		}
		PictureCrop cut = (deleteCroppedAreas && sameCrop) ? crop.positive() : PictureCrop.NONE;

		// the size the (cut) bitmap must have: the largest any picture shows it at. A picture
		// whose crop stays shows only part of the bitmap in its frame, so it needs more of it.
		double w = 0, h = 0;
		for (Use u : uses) {
			PictureCrop left = cut.isNone() ? u.crop().positive() : PictureCrop.NONE;
			double fx = 1 - left.left - left.right, fy = 1 - left.top - left.bottom;
			if (u.widthPt > 0) w = Math.max(w, u.widthPt / fx);
			if (u.heightPt > 0) h = Math.max(h, u.heightPt / fy);
		}

		byte[] before = part.getBytes();
		PictureCompression.Result r = PictureCompression.compress(before, cut, w, h, res);
		if (r == null) return;
		if (jpeg != r.contentType.equals("image/jpeg")) {
			// a transparent JPEG? ImageIO says otherwise: keep the format the part declares
			log.debug(part.getPartName() + ": format would change; left as it is");
			return;
		}
		part.setBinaryData(r.bytes);
		report.parts.add(part.getPartName().getName());
		report.bytesBefore += before.length;
		report.bytesAfter += r.bytes.length;

		if (!cut.isNone()) {
			// what is left of the crop: an edge which moved out, relative to the cut bitmap
			double sx = 1 - cut.left - cut.right, sy = 1 - cut.top - cut.bottom;
			for (Use u : uses) {
				PictureCrop c = u.crop();
				CTRelativeRect rest = null;
				if (c.pads()) {
					rest = new CTRelativeRect();
					rest.setL((int) Math.round(Math.min(0, c.left) / sx * 100000));
					rest.setR((int) Math.round(Math.min(0, c.right) / sx * 100000));
					rest.setT((int) Math.round(Math.min(0, c.top) / sy * 100000));
					rest.setB((int) Math.round(Math.min(0, c.bottom) / sy * 100000));
				}
				u.pic.getBlipFill().setSrcRect(rest);
				report.cropsDeleted++;
			}
		}
	}

	/** The block content of a story part, or null for any other part. */
	private static List<Object> storyContent(Part part) throws Docx4JException {
		if (part instanceof MainDocumentPart) return ((MainDocumentPart) part).getContent();
		if (part instanceof HeaderPart) return ((HeaderPart) part).getContent();
		if (part instanceof FooterPart) return ((FooterPart) part).getContent();
		if (part instanceof FootnotesPart || part instanceof EndnotesPart || part instanceof CommentsPart) {
			Object contents = ((JaxbXmlPart<?>) part).getContents();
			List<Object> blocks = new ArrayList<Object>();
			if (contents instanceof org.docx4j.wml.CTFootnotes) {
				for (Object o : ((org.docx4j.wml.CTFootnotes) contents).getFootnote()) addContent(o, blocks);
			} else if (contents instanceof org.docx4j.wml.CTEndnotes) {
				for (Object o : ((org.docx4j.wml.CTEndnotes) contents).getEndnote()) addContent(o, blocks);
			} else if (contents instanceof org.docx4j.wml.Comments) {
				for (Object o : ((org.docx4j.wml.Comments) contents).getComment()) addContent(o, blocks);
			}
			return blocks;
		}
		return null;
	}

	private static void addContent(Object o, List<Object> blocks) {
		if (o instanceof ContentAccessor) blocks.addAll(((ContentAccessor) o).getContent());
	}
}
