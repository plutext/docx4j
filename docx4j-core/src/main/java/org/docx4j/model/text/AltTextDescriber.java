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
 * The default {@link ImageDescriber} (WD REQ-514): the author's alt text, {@code wp:docPr/@descr},
 * else the picture's {@code @name}; for VML, {@code v:shape/@alt}; nothing for a picture marked
 * decorative. Every description it gives is the author's.
 *
 * @since 17.3.4 (CR-046 phase 2)
 */
public class AltTextDescriber implements ImageDescriber {

	/** The shared instance (it holds no state). */
	public static final AltTextDescriber INSTANCE = new AltTextDescriber();

	@Override
	public ImageDescription describe(TextImage image) {
		if (image == null || image.isDecorative()) return null;
		if (image.getDescr() != null && !image.getDescr().isEmpty()) return ImageDescription.author(image.getDescr());
		if (image.getName() != null && !image.getName().isEmpty()) return ImageDescription.author(image.getName());
		return null;
	}
}
