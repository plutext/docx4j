/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

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
import java.util.Collections;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;

/** An ImageHandler for HTML destined for an email body: images are
 *  referenced as cid: URIs (RFC 2392), eg src="cid:xxx@docx4j",
 *  and collected in memory so the caller can attach them to the
 *  message afterwards (a multipart/related, with each image in a
 *  MIME part carrying a matching Content-ID header).
 *
 *  Usage:
 *
 *    CidConversionImageHandler imageHandler = new CidConversionImageHandler();
 *    htmlSettings.setImageHandler(imageHandler);
 *    Docx4J.toHTML(htmlSettings, out, Docx4J.FLAG_NONE);
 *    for (CidImage image : imageHandler.getImages()) { ... attach ... }
 *
 *  See the ConvertOutHtmlToEmail sample for assembly with Jakarta Mail.
 *  (Spring users can instead pass each CidImage to
 *  MimeMessageHelper.addInline.)
 *
 *  An image used several times in the document yields several cid:
 *  references but only one CidImage.  Prefer this over
 *  DataUriConversionImageHandler for email: notably, Gmail clips
 *  messages whose HTML source exceeds about 100KB, which base64
 *  embedding will typically exceed immediately.
 *
 *  As with the other handlers, external (linked) images are left as
 *  their original URL (which many mail clients block by default), and
 *  image formats mail clients can't render (eg WMF, EMF, TIFF) are
 *  attached as-is, so won't display.
 *
 *  @since 17.0.1
 */
public class CidConversionImageHandler extends AbstractConversionImageHandler {

	/** An image to be attached: bytes plus the Content-ID its cid:
	 *  reference points at.
	 */
	public static class CidImage {

		private final String contentId;
		private final String contentType;
		private final byte[] bytes;
		private final String name;

		CidImage(String contentId, String contentType, byte[] bytes, String name) {
			this.contentId = contentId;
			this.contentType = contentType;
			this.bytes = bytes;
			this.name = name;
		}

		/** The Content-ID, without angle brackets; the HTML references it
		 *  as "cid:" + getContentId() */
		public String getContentId() {
			return contentId;
		}

		/** The Content-ID header value, ie the Content-ID wrapped in the
		 *  angle brackets RFC 2045 requires; pass this to
		 *  MimeBodyPart.setContentID */
		public String getContentIdHeader() {
			return "<" + contentId + ">";
		}

		public String getContentType() {
			return contentType;
		}

		public byte[] getBytes() {
			return bytes;
		}

		/** A filename hint for the attachment, eg image1.png */
		public String getName() {
			return name;
		}
	}

	protected List<CidImage> images = new ArrayList<CidImage>();
	protected String domain;

	public CidConversionImageHandler() {
		this("docx4j");
	}

	/** @param domain, used in the generated Content-IDs (which RFC 2045
	 *  requires to be globally unique), eg "example.com"
	 */
	public CidConversionImageHandler(String domain) {
		super("", false);  // imageDirPath is unused; we override handleInternalImage
		this.domain = domain;
	}

	@Override
	protected String handleInternalImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart binaryPart) throws Docx4JException {
		byte[] bytes = getImageData(binaryPart);
		String name = getImageName(binaryPart);
		String contentId = uuid + "." + (images.size() + 1) + "." + name + "@" + domain;
		images.add(new CidImage(contentId, binaryPart.getContentType(), bytes, name));
		return "cid:" + contentId;
	}

	@Override
	protected String createStoredImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException {
		// unreachable, since handleInternalImage is overridden above
		throw new Docx4JException("CidConversionImageHandler never stores images");
	}

	/** The images referenced so far, one per distinct image, in the order
	 *  first encountered */
	public List<CidImage> getImages() {
		return Collections.unmodifiableList(images);
	}

	@Override
	public void clear() {
		super.clear();
		images.clear();
	}
}
