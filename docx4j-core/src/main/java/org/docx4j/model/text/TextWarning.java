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
package org.docx4j.model.text;

import java.util.Objects;

/**
 * Where a result is not the whole document (WD section 8, REQ-801): a kind, the address it is at
 * where there is one, and its selector: for {@code mce.branches_differ} the name of the branch
 * selected, for {@code altchunk.not_expanded}, {@code content.not_read} and {@code image.not_described}
 * the part's name (the chunk's, since 17.3.5, WD 0.47; it was the chunk's content type, which the
 * warning still carries), for {@code part.not_read} the part's name, for {@code paragraph.drop_cap_joined}
 * the name of the part the paragraphs are in.
 * Never the document's text (REQ-802).
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextWarning {

	/** Both branches of an {@code mc:AlternateContent} hold text, and the texts differ (REQ-307). */
	public static final String MCE_BRANCHES_DIFFER = "mce.branches_differ";
	/** A field with no cached result (REQ-505). */
	public static final String FIELD_NO_RESULT = "field.no_result";
	/** A data-bound control whose surface differs from its bound value (REQ-509). */
	public static final String SDT_BINDING_DIFFERS = "sdt.binding_differs";
	/** An {@code altChunk} not expanded (REQ-513). */
	public static final String ALTCHUNK_NOT_EXPANDED = "altchunk.not_expanded";
	/**
	 * A chart, a SmartArt data part, or an embedded package or a part inside it, that can't be read: it
	 * contributes nothing (REQ-511, REQ-512, REQ-801).
	 */
	public static final String CONTENT_NOT_READ = "content.not_read";
	/**
	 * A part the reading consulted and needed for this document but does not emit (the styles, the numbering, the
	 * settings) that can't be read: the text is read as for a package that lacks it (REQ-801, WD 0.66). Once a part;
	 * its selector is the part's name.
	 *
	 * @since 17.3.6
	 */
	public static final String PART_NOT_READ = "part.not_read";
	/** An image the {@link ImageDescriber} failed to describe: it has no description (REQ-514, REQ-801). */
	public static final String IMAGE_NOT_DESCRIBED = "image.not_described";
	/**
	 * A drop cap's frame paragraph joined to the paragraph after it, two {@code w:p} read as one (REQ-521): at the
	 * frame paragraph's address.
	 *
	 * @since 17.3.5 (CR-069)
	 */
	public static final String DROP_CAP_JOINED = "paragraph.drop_cap_joined";

	private final String kind;
	private final String address;
	private final String branch;
	private final String contentType;
	private final String partName;

	public TextWarning(String kind, String address, String branch) {
		this(kind, address, branch, null);
	}

	public TextWarning(String kind, String address, String branch, String contentType) {
		this(kind, address, branch, contentType, null);
	}

	public TextWarning(String kind, String address, String branch, String contentType, String partName) {
		this.kind = Objects.requireNonNull(kind);
		this.address = address;
		this.branch = branch;
		this.contentType = contentType;
		this.partName = partName;
	}

	public String getKind() { return kind; }
	/** The address of the block it is in (WD REQ-518's grammar), or null. */
	public String getAddress() { return address; }
	/** For {@code mce.branches_differ}, the selected branch ({@code mc:Choice Requires="wps"}, {@code mc:Fallback}); else null. */
	public String getBranch() { return branch; }
	/** For {@code altchunk.not_expanded}, the chunk's content type as the package declares it ({@code application/rtf}); else null. */
	public String getContentType() { return contentType; }
	/**
	 * For {@code content.not_read} and {@code part.not_read}, the name of the part that can't be read (for a
	 * part inside an embedded package, the embedded package part's); for {@code image.not_described}, the image
	 * part's ({@code /word/media/image1.png}); for {@code altchunk.not_expanded}, the chunk's
	 * ({@code /word/afchunk.rtf}), so that a host can read it itself (since 17.3.5, CR-070); for
	 * {@code paragraph.drop_cap_joined}, the part the paragraphs are in ({@code /word/document.xml}; since
	 * 17.3.5, CR-069); else null.
	 */
	public String getPartName() { return partName; }
	/**
	 * The selector of REQ-801: the branch, else the part's name, else the content type (a warning made
	 * without a part name); null for none. {@code altchunk.not_expanded}'s is its part name since
	 * 17.3.5 (WD 0.47), its content type before.
	 */
	public String getSelector() {
		return branch != null ? branch : partName != null ? partName : contentType;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TextWarning)) return false;
		TextWarning w = (TextWarning) o;
		return kind.equals(w.kind) && Objects.equals(address, w.address) && Objects.equals(branch, w.branch)
				&& Objects.equals(contentType, w.contentType) && Objects.equals(partName, w.partName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, address, branch, contentType, partName);
	}

	@Override
	public String toString() {
		return kind + (address == null ? "" : " at " + address) + (branch == null ? "" : " (" + branch + ")")
				+ (contentType == null ? "" : " (" + contentType + ")") + (partName == null ? "" : " (" + partName + ")");
	}
}
