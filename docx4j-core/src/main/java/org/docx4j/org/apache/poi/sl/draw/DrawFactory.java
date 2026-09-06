/* NOTICE: This file is a docx4j shim, not a copy of an Apache POI source file.
 *
 * Apache POI's org.apache.poi.sl.draw.DrawFactory is the entry point of the
 * whole "sl" (slideshow) drawing layer, which docx4j does not repackage. The
 * HWMF/HEMF code calls exactly one thing on it:
 *
 *     DrawFactory.getInstance(graphics).getFontManager(graphics)
 *
 * so this shim keeps that shape (leaving the copied HWMF/HEMF sources
 * unmodified) while providing only font-manager lookup. Written by Plutext Pty
 * Ltd for docx4j; licensed under the Apache License, Version 2.0, the same
 * terms as the Apache POI code it stands in for.
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

/**
 * Minimal stand-in for POI's {@code org.apache.poi.sl.draw.DrawFactory}, providing
 * only what the repackaged HWMF/HEMF code needs: resolution of the
 * {@link DrawFontManager} for a given {@link Graphics2D}.
 * <p>
 * The font manager is resolved in the same order POI uses:
 * <ol>
 *   <li>the {@link Drawable#FONT_HANDLER} rendering hint on the graphics context;</li>
 *   <li>a factory registered for the current thread via {@link #setDefaultFactory(DrawFactory)};</li>
 *   <li>docx4j's {@link Docx4jDrawFontManager}, which maps typeface names through
 *       {@code org.docx4j.fonts.PhysicalFonts}.</li>
 * </ol>
 */
public class DrawFactory {

    private static final ThreadLocal<DrawFactory> defaultFactory = new ThreadLocal<>();

    /**
     * Set a custom draw factory for the current thread.
     * This is a fallback, for operations where usercode can't set a graphics context.
     * Preferably use the rendering hint {@link Drawable#DRAW_FACTORY} to set the factory.
     *
     * @param factory the custom factory or {@code null} to reset/remove the default factory
     */
    public static void setDefaultFactory(DrawFactory factory) {
        if (factory == null) {
            defaultFactory.remove();
        } else {
            defaultFactory.set(factory);
        }
    }

    /**
     * Returns the {@code DrawFactory} for the given graphics context, registering it
     * as the {@link Drawable#DRAW_FACTORY} hint if there wasn't one already.
     *
     * @param graphics the graphics context, may be {@code null}
     * @return the draw factory, never {@code null}
     */
    public static DrawFactory getInstance(Graphics2D graphics) {
        // first try to find the factory over the rendering hint
        DrawFactory factory = null;
        boolean isHint = false;
        if (graphics != null) {
            factory = (DrawFactory)graphics.getRenderingHint(Drawable.DRAW_FACTORY);
            isHint = (factory != null);
        }
        // secondly try the thread local default
        if (factory == null) {
            factory = defaultFactory.get();
        }
        // and at last, use the default factory
        if (factory == null) {
            factory = new DrawFactory();
        }
        if (graphics != null && !isHint) {
            graphics.setRenderingHint(Drawable.DRAW_FACTORY, factory);
        }
        return factory;
    }

    /**
     * @param graphics the graphics context, may be {@code null}
     * @return the font manager set as the {@link Drawable#FONT_HANDLER} hint, or
     *         docx4j's default one
     */
    public DrawFontManager getFontManager(Graphics2D graphics) {
        DrawFontManager fontHandler = (graphics == null)
            ? null : (DrawFontManager)graphics.getRenderingHint(Drawable.FONT_HANDLER);
        return (fontHandler != null) ? fontHandler : new Docx4jDrawFontManager();
    }
}
