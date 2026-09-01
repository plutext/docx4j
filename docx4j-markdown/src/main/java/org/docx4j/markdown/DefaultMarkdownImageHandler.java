package org.docx4j.markdown;

import java.io.File;
import java.util.Base64;
import java.util.Locale;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Drawing;
import org.docx4j.jaxb.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embeds data-URI and local-file images via BinaryPartAbstractImage;
 * declines (returns null) for anything else — notably remote http(s) URLs,
 * which are never fetched (the importer then emits hyperlinked alt text).
 */
public class DefaultMarkdownImageHandler implements MarkdownImageHandler {

	private static final Logger log = LoggerFactory.getLogger(DefaultMarkdownImageHandler.class);

	private final String baseDir;
	private long nextId = 1;

	public DefaultMarkdownImageHandler() {
		this(null);
	}

	/**
	 * @param baseDir directory against which relative image paths resolve;
	 *        null means relative paths are not resolved (declined)
	 */
	public DefaultMarkdownImageHandler(String baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public Object toRunContent(String destination, String title, String altText,
			WordprocessingMLPackage pkg) {

		try {
			String lower = destination.toLowerCase(Locale.ROOT);

			if (lower.startsWith("data:")) {
				int comma = destination.indexOf(',');
				if (comma < 0 || !destination.substring(0, comma).toLowerCase(Locale.ROOT).contains("base64")) {
					log.warn("Unsupported data URI (not base64); emitting alt text");
					return null;
				}
				byte[] bytes = Base64.getMimeDecoder().decode(destination.substring(comma + 1));
				return inline(BinaryPartAbstractImage.createImagePart(pkg, bytes), altText);
			}

			if (lower.startsWith("http:") || lower.startsWith("https:")) {
				return null; // never fetched; hyperlinked alt text instead
			}

			File file;
			if (lower.startsWith("file:")) {
				file = new File(new java.net.URI(destination));
			} else {
				file = new File(destination);
				if (!file.isAbsolute()) {
					if (baseDir == null) {
						return null;
					}
					file = new File(baseDir, destination);
				}
			}
			if (!file.exists()) {
				log.warn("Image not found: {}; emitting alt text", file);
				return null;
			}
			return inline(BinaryPartAbstractImage.createImagePart(pkg, file), altText);

		} catch (Exception e) {
			log.warn("Could not embed image {}; emitting alt text", destination, e);
			return null;
		}
	}

	private Object inline(BinaryPartAbstractImage imagePart, String altText) throws Exception {
		long id = nextId++;
		Inline inline = imagePart.createImageInline(null, altText, id, (int) id, false);
		Drawing drawing = Context.getWmlObjectFactory().createDrawing();
		drawing.getAnchorOrInline().add(inline);
		return drawing;
	}

}
