/* NOTICE: This file is a docx4j shim, not a copy of an Apache POI source file.
 *
 * It replaces Apache POI's org.apache.poi.sl.draw.DrawPictureShape.getImageRenderer(..),
 * which is the only thing the HWMF/HEMF code needed from that class - the rest of
 * DrawPictureShape belongs to POI's slideshow ("sl") layer, which docx4j does not
 * repackage. POI itself carries a TODO to move this method out of the slideshow code.
 *
 * POI resolves further renderers through java.util.ServiceLoader; docx4j instead
 * dispatches explicitly to the three renderers it has, which avoids a service-loader
 * lookup that does not work on the module path anyway.
 *
 * Written by Plutext Pty Ltd for docx4j; licensed under the Apache License,
 * Version 2.0, the same terms as the Apache POI code it stands in for.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.sl.draw;

import java.awt.Graphics2D;

import org.docx4j.org.apache.poi.hemf.draw.HemfImageRenderer;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfImageRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chooses an {@link ImageRenderer} for a content type.
 *
 * @since 17.0.6 (CR-011 phase 1)
 */
public final class ImageRendererFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ImageRendererFactory.class);

    private ImageRendererFactory() {}

    /**
     * Returns an {@link ImageRenderer} able to render the given content type.
     * <p>
     * A renderer set as the {@link Drawable#IMAGE_RENDERER} rendering hint wins, if it
     * can render the content type. Otherwise the bitmap (ImageIO), WMF and EMF renderers
     * are tried in turn. If none matches, the bitmap renderer is returned, because it
     * degrades gracefully on data it cannot decode.
     *
     * @param graphics the graphics context, may be {@code null}
     * @param contentType the mime type of the image data, e.g. {@code image/x-wmf}
     * @return the image renderer, never {@code null}
     */
    public static ImageRenderer getImageRenderer(Graphics2D graphics, String contentType) {
        final ImageRenderer hinted = (graphics != null)
            ? (ImageRenderer)graphics.getRenderingHint(Drawable.IMAGE_RENDERER) : null;
        if (hinted != null && hinted.canRender(contentType)) {
            return hinted;
        }

        final BitmapImageRenderer fallback = new BitmapImageRenderer();
        if (fallback.canRender(contentType)) {
            return fallback;
        }

        final HwmfImageRenderer wmf = new HwmfImageRenderer();
        if (wmf.canRender(contentType)) {
            return wmf;
        }

        final HemfImageRenderer emf = new HemfImageRenderer();
        if (emf.canRender(contentType)) {
            return emf;
        }

        // the fallback is the BitmapImageRenderer, at least it gracefully handles invalid images
        LOG.atWarn().log("No suitable image renderer found for content-type '{}'", contentType);
        return fallback;
    }
}
