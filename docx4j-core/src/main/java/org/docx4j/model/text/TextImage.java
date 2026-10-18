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

import org.docx4j.openpackaging.parts.Part;

/**
 * A picture as the document gives it, for an {@link ImageDescriber} (WD REQ-514): its alt text
 * ({@code wp:docPr/@descr}, or VML's {@code v:shape/@alt}), its name and title, whether it is marked
 * decorative ({@code adec:decorative}), and its image part where the package holds one. The title is
 * never read as text (REQ-514's note on {@code @title}); it is here for a describer that wants it.
 *
 * @since 17.3.4 (CR-046 phase 2)
 */
public final class TextImage {

	final String descr;
	final String name;
	final String title;
	final boolean decorative;
	final boolean vml;
	final Part part;
	final Object source;

	TextImage(String descr, String name, String title, boolean decorative, boolean vml, Part part, Object source) {
		this.descr = descr;
		this.name = name;
		this.title = title;
		this.decorative = decorative;
		this.vml = vml;
		this.part = part;
		this.source = source;
	}

	/** The alt text: {@code wp:docPr/@descr} (or a nested picture's {@code pic:cNvPr/@descr}), VML {@code v:shape/@alt}; null for none. */
	public String getDescr() { return descr; }
	/** {@code wp:docPr/@name} ({@code pic:cNvPr/@name}); null for a VML picture. */
	public String getName() { return name; }
	/** {@code wp:docPr/@title}; null for none. */
	public String getTitle() { return title; }
	/** True for a picture marked decorative ({@code adec:decorative val="1"} in {@code wp:docPr}'s extensions). */
	public boolean isDecorative() { return decorative; }
	/** True for a VML picture ({@code v:shape} with {@code v:imagedata}). */
	public boolean isVml() { return vml; }
	/**
	 * The image part ({@code a:blip/@r:embed}, {@code v:imagedata/@r:id}), or null for a linked or missing one.
	 * An SVG picture with no PNG copy ({@code a:blip} with only its {@code asvg:svgBlip}) gives its SVG part,
	 * which docx4j loads as a {@code DefaultXmlPart} of content type {@code image/svg+xml} (issue 699).
	 */
	public Part getPart() { return part; }
	/** The JAXB object the picture is read from: a {@code wp:inline}, {@code wp:anchor}, {@code pic:pic} or {@code v:shape}. */
	public Object getSource() { return source; }
}
