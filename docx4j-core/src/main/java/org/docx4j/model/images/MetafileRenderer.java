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

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Draws a Windows metafile (WMF, EMF, EMF+) onto any {@link Graphics2D}.
 *
 * <p>A metafile is a recording of GDI drawing calls, not a bitmap, so the only
 * general way to turn one into something a PDF or a browser can show is to replay
 * it.  {@link #draw} is therefore the primitive; SVG (vectors, for
 * {@code fo:instream-foreign-object} or inline HTML) and {@link #toImage} (a
 * raster fallback) are both built on it.</p>
 *
 * <p>Implementations must be safe on hostile input: a metafile arrives from a
 * document, so nothing it contains may take a conversion down.  A file which
 * cannot be parsed at all yields {@code null} from {@link #getSizeInPoints} and
 * {@link #toImage} so the caller can fall back; one which fails part way through
 * a {@link #draw} keeps whatever was painted before the failure.</p>
 *
 * @see PoiMetafileRenderer the implementation over the repackaged Apache POI
 *      HWMF/HEMF code
 * @since 17.1.0 (CR-011 phase 2)
 */
public interface MetafileRenderer {

	/**
	 * Whether this renderer handles the given content type.
	 *
	 * @param contentType eg {@code image/x-wmf}, {@code image/x-emf}, {@code image/emf}
	 */
	boolean canRender(String contentType);

	/**
	 * The metafile's own size, in points (1/72 inch).
	 *
	 * @return null if the file cannot be parsed, or states no usable size
	 */
	Dimension2D getSizeInPoints(byte[] data);

	/**
	 * Replays the metafile onto the target, scaled to fill {@code bounds}.
	 *
	 * <p>Never throws: a file which cannot be parsed paints nothing, and one which
	 * fails part way through keeps what it had already painted.</p>
	 *
	 * <p>Rendering hints are set on the target (antialiasing, and the hints the
	 * renderer itself reads, such as the font handler) and not restored.</p>
	 */
	void draw(byte[] data, Graphics2D target, Rectangle2D bounds);

	/**
	 * Rasterises the metafile at the given resolution, at its own size.
	 *
	 * @param dpi pixels per inch; 96 is the Windows screen resolution the metafile
	 *            was most likely authored at
	 * @return an ARGB image with a transparent background, or null if the file
	 *         cannot be parsed
	 */
	BufferedImage toImage(byte[] data, double dpi);
}
