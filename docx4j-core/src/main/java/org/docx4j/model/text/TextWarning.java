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
 * where there is one, for {@code mce.branches_differ} the name of the branch selected, and for
 * {@code altchunk.not_expanded} the chunk's content type. Never the document's text (REQ-802).
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

	private final String kind;
	private final String address;
	private final String branch;
	private final String contentType;

	public TextWarning(String kind, String address, String branch) {
		this(kind, address, branch, null);
	}

	public TextWarning(String kind, String address, String branch, String contentType) {
		this.kind = Objects.requireNonNull(kind);
		this.address = address;
		this.branch = branch;
		this.contentType = contentType;
	}

	public String getKind() { return kind; }
	/** The address of the block it is in (WD REQ-518's grammar), or null. */
	public String getAddress() { return address; }
	/** For {@code mce.branches_differ}, the selected branch ({@code mc:Choice Requires="wps"}, {@code mc:Fallback}); else null. */
	public String getBranch() { return branch; }
	/** For {@code altchunk.not_expanded}, the chunk's content type as the package declares it ({@code application/rtf}); else null. */
	public String getContentType() { return contentType; }

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TextWarning)) return false;
		TextWarning w = (TextWarning) o;
		return kind.equals(w.kind) && Objects.equals(address, w.address) && Objects.equals(branch, w.branch)
				&& Objects.equals(contentType, w.contentType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, address, branch, contentType);
	}

	@Override
	public String toString() {
		return kind + (address == null ? "" : " at " + address) + (branch == null ? "" : " (" + branch + ")")
				+ (contentType == null ? "" : " (" + contentType + ")");
	}
}
