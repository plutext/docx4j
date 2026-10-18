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

/**
 * Gives an image its text (WD REQ-514): one describer per extraction, {@link AltTextDescriber} by
 * default. A host may replace or wrap the default, a captioning model for instance, through
 * {@link TextOptions#imageDescriber(ImageDescriber)}; what it generates it marks
 * {@link ImageDescription.Provenance#GENERATED}, and plain text then writes
 * {@code [image, generated: ...]}.
 *
 * <p>A describer reads what the document says of a picture, and may read its part; an SVG's
 * {@code <text>} elements and the glyph runs of EMF and WMF are not the document's text and the
 * default never reads them (REQ-514).</p>
 *
 * @since 17.3.4 (CR-046 phase 2)
 */
@FunctionalInterface
public interface ImageDescriber {

	/** The image's description, or null for none. */
	ImageDescription describe(TextImage image);
}
