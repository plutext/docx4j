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
 * An image's description (WD REQ-514) and where it came from: the document's author, or a
 * describer that generated it. Plain text writes it {@code [image: ...]} or
 * {@code [image, generated: ...]} under {@code alt_text: true} (REQ-707).
 *
 * @since 17.3.4 (CR-046 phase 2)
 */
public final class ImageDescription {

	/** Where a description came from. */
	public enum Provenance {
		/** The document's own text: {@code wp:docPr/@descr} or {@code @name}, VML {@code @alt}. */
		AUTHOR,
		/** Made by a describer (a captioning model, say), not written by the document's author. */
		GENERATED
	}

	private final String text;
	private final Provenance provenance;

	public ImageDescription(String text, Provenance provenance) {
		this.text = Objects.requireNonNull(text);
		this.provenance = Objects.requireNonNull(provenance);
	}

	/** An author's description. */
	public static ImageDescription author(String text) {
		return new ImageDescription(text, Provenance.AUTHOR);
	}

	/** A generated description. */
	public static ImageDescription generated(String text) {
		return new ImageDescription(text, Provenance.GENERATED);
	}

	public String getText() { return text; }
	public Provenance getProvenance() { return provenance; }

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ImageDescription)) return false;
		ImageDescription d = (ImageDescription) o;
		return text.equals(d.text) && provenance == d.provenance;
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, provenance);
	}

	@Override
	public String toString() {
		return provenance == Provenance.GENERATED ? "generated: " + text : text;
	}
}
