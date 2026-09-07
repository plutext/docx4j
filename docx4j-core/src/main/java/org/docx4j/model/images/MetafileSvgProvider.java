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

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Turns a Windows metafile into an SVG document, keeping it as vectors.
 *
 * <p>SVG is what both output formats want: {@code fo:instream-foreign-object} in
 * XSL-FO (FOP draws it into the PDF as vectors, and its text as text where it can
 * resolve the font), and an inline {@code <svg>} in HTML.  Producing it needs a
 * {@code Graphics2D} that records rather than paints, which docx4j takes from
 * Batik ({@code batik-svggen}) - a dependency of {@code docx4j-export-fo}, not of
 * {@code docx4j-core}.  So this is an SPI: core asks for a provider, and
 * docx4j-export-fo supplies one through {@link ServiceLoader}.</p>
 *
 * <p>Where no provider is on the classpath - docx4j-core on its own - HTML output
 * falls back to a PNG, which core can produce with ImageIO alone
 * ({@link MetafileRenderer#toImage}), and {@code MetafilePart.toSVG()} throws.</p>
 *
 * @since 17.1.0 (CR-011 phase 2)
 */
public interface MetafileSvgProvider {

	/**
	 * Replays the metafile into a new SVG document, sized to fill the given frame.
	 *
	 * @param data      the WMF / EMF / EMF+ bytes
	 * @param widthPt   the width the document gives the picture, in points; the
	 *                  metafile's own width where the document states none (&lt;= 0)
	 * @param heightPt  likewise the height
	 * @return the SVG document, or null if the metafile could not be rendered
	 */
	Document toSvgDocument(byte[] data, double widthPt, double heightPt);

	// ------------------------------------------------------------------ lookup

	/** cache: null means "not looked up yet", NONE means "looked up, none there" */
	final class Holder {
		private Holder() {}
		static final Logger log = LoggerFactory.getLogger(MetafileSvgProvider.class);
		static volatile MetafileSvgProvider provider;
		static volatile boolean resolved;
	}

	/**
	 * The provider on the classpath, or null.
	 *
	 * <p>Looked up once.  {@link #setProvider} overrides it, for a caller who wants
	 * their own SVG generator or wants to turn the feature off.</p>
	 */
	static MetafileSvgProvider getProvider() {
		if (!Holder.resolved) {
			synchronized (Holder.class) {
				if (!Holder.resolved) {
					MetafileSvgProvider found = null;
					try {
						for (MetafileSvgProvider p : ServiceLoader.load(MetafileSvgProvider.class)) {
							found = p;
							break;
						}
						if (found == null) {
							// TCCL didn't have it (eg a container which isolates the app);
							// try the classloader docx4j-core itself came from
							for (MetafileSvgProvider p : ServiceLoader.load(MetafileSvgProvider.class,
									MetafileSvgProvider.class.getClassLoader())) {
								found = p;
								break;
							}
						}
					} catch (Throwable t) {
						Holder.log.warn("No metafile SVG provider: " + t);
					}
					if (found == null) {
						Holder.log.debug("No MetafileSvgProvider on the classpath;"
								+ " metafiles will be rasterised (add docx4j-export-fo for SVG).");
					} else {
						Holder.log.debug("Metafile SVG provider: " + found.getClass().getName());
					}
					Holder.provider = found;
					Holder.resolved = true;
				}
			}
		}
		return Holder.provider;
	}

	/** Overrides the discovered provider; null disables SVG output for metafiles. */
	static void setProvider(MetafileSvgProvider p) {
		synchronized (Holder.class) {
			Holder.provider = p;
			Holder.resolved = true;
		}
	}

	/**
	 * Convenience: the SVG for this metafile, or null where there is no provider or
	 * the metafile could not be rendered.  Never throws.
	 */
	static Document toSvg(byte[] data, double widthPt, double heightPt) {
		MetafileSvgProvider p = getProvider();
		if (p == null || data == null) return null;
		try {
			return p.toSvgDocument(data, widthPt, heightPt);
		} catch (Throwable t) {
			Holder.log.warn("Metafile could not be rendered as SVG: " + t);
			return null;
		}
	}
}
