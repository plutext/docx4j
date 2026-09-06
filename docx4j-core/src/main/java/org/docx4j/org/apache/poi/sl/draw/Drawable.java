/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
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
import java.awt.RenderingHints;

import org.docx4j.org.apache.poi.util.Internal;


public interface Drawable {
    class DrawableHint extends RenderingHints.Key {
        protected DrawableHint(int id) {
            super(id);
        }

        public boolean isCompatibleValue(Object val) {
            return true;
        }

        public String toString() {
            switch (intKey()) {
            case 1: return "DRAW_FACTORY";
            case 2: return "GROUP_TRANSFORM";
            case 3: return "IMAGE_RENDERER";
            case 4: return "TEXT_RENDERING_MODE";
            case 5: return "GRADIENT_SHAPE";
            case 6: return "PRESET_GEOMETRY_CACHE";
            case 7: return "FONT_HANDLER";
            case 8: return "FONT_FALLBACK";
            case 9: return "FONT_MAP";
            case 10: return "GSAVE";
            case 11: return "GRESTORE";
            case 12: return "CURRENT_SLIDE";
            case 13: return "BUFFERED_IMAGE";
            case 14: return "DEFAULT_CHARSET";
            case 15: return "EMF_FORCE_HEADER_BOUNDS";
            case 16: return "CACHE_IMAGE_SOURCE";
            default: return "UNKNOWN_ID "+intKey();
            }
        }
    }

    /**
     * {@link DrawFactory} which will be used to draw objects into this graphics context
     */
    DrawableHint DRAW_FACTORY = new DrawableHint(1);

    /**
     * Key will be internally used to store affine transformation temporarily within group shapes
     */
    @Internal
    DrawableHint GROUP_TRANSFORM = new DrawableHint(2);

    /**
     * Use a custom image renderer of an instance of {@link ImageRenderer}
     */
    DrawableHint IMAGE_RENDERER = new DrawableHint(3);

    /**
     *  how to render text:
     *
     *  {@link #TEXT_AS_CHARACTERS} (default) means to draw via
     *   {@link java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)}.
     *   This mode draws text as characters. Use it if the target graphics writes the actual
     *   character codes instead of glyph outlines (PDFGraphics2D, SVGGraphics2D, etc.)
     *
     *   {@link #TEXT_AS_SHAPES} means to render via
     *   {@link java.awt.font.TextLayout#draw(java.awt.Graphics2D, float, float)}.
     *   This mode draws glyphs as shapes and provides some advanced capabilities such as
     *   justification and font substitution. Use it if the target graphics is an image.
     *
     */
    DrawableHint TEXT_RENDERING_MODE = new DrawableHint(4);

    /**
     * PathGradientPaint needs the shape to be set.
     * It will be achieved through setting it in the rendering hints
     */
    DrawableHint GRADIENT_SHAPE = new DrawableHint(5);


    /**
     * Internal key for caching the preset geometries
     */
    DrawableHint PRESET_GEOMETRY_CACHE = new DrawableHint(6);

    /**
     * draw text via {@link java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)}
     */
    int TEXT_AS_CHARACTERS = 1;

    /**
     * draw text via {@link java.awt.font.TextLayout#draw(java.awt.Graphics2D, float, float)}
     */
    int TEXT_AS_SHAPES = 2;

    /**
     * Use this object to resolve unknown / missing fonts when rendering slides.
     * The font handler must be of type {@link DrawFontManager}.<p>
     *
     * In case a {@code FONT_HANDLER} is register, {@code FONT_FALLBACK} and {@code FONT_MAP} are ignored
     */
    DrawableHint FONT_HANDLER = new DrawableHint(7);

    /**
     * Key for a font fallback map of type {@code Map<String,String>} which maps
     * the original font family (key) to the fallback font family (value).
     * In case there is also a {@code FONT_MAP} registered, the original font
     * is first mapped via the font_map and then the fallback font is determined
     */
    DrawableHint FONT_FALLBACK = new DrawableHint(8);

    /**
     * Key for a font map of type {@code Map<String,String>} which maps
     * the original font family (key) to the mapped font family (value)
     */
    DrawableHint FONT_MAP = new DrawableHint(9);

    DrawableHint GSAVE = new DrawableHint(10);
    DrawableHint GRESTORE = new DrawableHint(11);

    /**
     * The Common SL Draw API works sometimes cascading, but there are places
     * where the current slide context need to be evaluated, e.g. when slide numbers
     * are printed. In this situation we need to have a way to access the current slide
     */
    DrawableHint CURRENT_SLIDE = new DrawableHint(12);

    /**
     * Stores a reference (WEAK_REFERENCE) to the buffered image, if the rendering is
     * based on a buffered image
     */
    DrawableHint BUFFERED_IMAGE = new DrawableHint(13);

    /**
     * Sets the default charset to render text elements.
     * Opposed to other windows libraries in POI this simply defaults to Windows-1252.
     * The rendering value is of type {@link java.nio.charset.Charset}
     */
    DrawableHint DEFAULT_CHARSET = new DrawableHint(14);

    /**
     * A boolean value to force the usage of the bounding box, which is specified in the EMF header.
     * Defaults to {@code FALSE} - in this case the records are scanned for window and
     * viewport records to determine the initial bounding box by using the following
     * condition: {@code isValid(viewport) ? viewport : isValid(window) ? window : headerBounds }
     * <p>
     * This is a workaround switch, which might be removed in future releases, when the bounding box
     * determination for the special cases is fixed.
     * In most cases it's recommended to leave the default value.
     */
    DrawableHint EMF_FORCE_HEADER_BOUNDS = new DrawableHint(15);

    /**
     * A boolean value to instruct the bitmap image renderer to keep the original image bytes.
     * Defaults to {@code false} if unset.
     */
    DrawableHint CACHE_IMAGE_SOURCE = new DrawableHint(16);


    /**
     * Apply 2-D transforms before drawing this shape. This includes rotation and flipping.
     *
     * @param graphics the graphics whose transform matrix will be modified
     */
    void applyTransform(Graphics2D graphics);

    /**
     * Draw this shape into the supplied canvas
     *
     * @param graphics the graphics to draw into
     */
    void draw(Graphics2D graphics);

    /**
     * draw any content within this shape (image, text, etc.).
     *
     * @param graphics the graphics to draw into
     */
    void drawContent(Graphics2D graphics);
}
