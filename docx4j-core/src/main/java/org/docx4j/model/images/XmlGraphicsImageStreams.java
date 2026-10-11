/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.images;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Has xmlgraphics-commons close the stream an image preloader read a picture's header from,
 * instead of keeping it open for the image loader to reuse.
 *
 * <p>Kept open, that stream holds the picture's file (and ImageIO's cache file beside it)
 * open until garbage collection - and JEuclid's MathML preloader, which parses every picture
 * to see whether it is MathML, keeps the last one reachable past that.  On Windows an open file
 * can't be deleted, so the temporary image files a conversion wrote (a TOC update, a PDF)
 * stayed behind, locked (issue 609).</p>
 *
 * <p>xmlgraphics reads its system property once, when its AbstractImageSessionContext is
 * first used, so docx4j sets it from the static initializers of the classes that use the
 * image loader: BinaryPartAbstractImage, for a picture's size, and the FO renderer.  It is
 * left alone where already set, either way.</p>
 *
 * @since 17.3.3
 */
public final class XmlGraphicsImageStreams {

	private static final Logger log = LoggerFactory.getLogger(XmlGraphicsImageStreams.class);

	/** xmlgraphics-commons' system property; true closes each preloaded stream at once */
	public static final String NO_SOURCE_REUSE
			= "org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext.no-source-reuse";

	static {
		try {
			if (System.getProperty(NO_SOURCE_REUSE) == null) {
				System.setProperty(NO_SOURCE_REUSE, "true");
			}
		} catch (SecurityException e) {
			log.debug("Couldn't set " + NO_SOURCE_REUSE + ": " + e.getMessage());
		}
	}

	/** Sets the property, the first time this class is used; afterwards does nothing */
	public static void init() {
	}

	private XmlGraphicsImageStreams() {
	}
}
