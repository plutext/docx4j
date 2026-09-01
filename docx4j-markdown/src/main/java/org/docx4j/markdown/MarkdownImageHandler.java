package org.docx4j.markdown;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Pluggable handling of markdown images (cf the ConversionImageHandler
 * precedent on the export side).
 *
 * <p>The default implementation ({@link DefaultMarkdownImageHandler}) embeds
 * data URIs and local files, and deliberately does NOT fetch remote URLs
 * (SSRF / supply-chain posture); a caller who wants remote images fetched
 * supplies a handler which does so.</p>
 */
public interface MarkdownImageHandler {

	/**
	 * @param destination the image destination as written in the markdown
	 *        (a path, data URI, or URL)
	 * @param title the image title, if any (often null)
	 * @param altText the image's alt text (may be empty)
	 * @param pkg the package being built into, to which an image part may be added
	 * @return an object to add to a run in the current paragraph (typically a w:drawing
	 *         containing wp:inline), or null to decline, in which case the
	 *         importer falls back to the image's alt text hyperlinked to the
	 *         destination
	 */
	Object toRunContent(String destination, String title, String altText,
			WordprocessingMLPackage pkg);

}
