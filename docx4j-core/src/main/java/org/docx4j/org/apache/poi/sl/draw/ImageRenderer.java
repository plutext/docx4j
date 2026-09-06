/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.docx4j.org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.util.Dimension2DDouble;

/**
 * Classes can implement this interfaces to support other formats, for
 * example, use Apache Batik to render WMF (since POI 4.0, there's an internal WMF/EMF/EMF+ renderer in POI),
 * PICT can be rendered using Apple QuickTime API for Java:
 *
 * <pre>
 * <code>
 * public class MyImageRendener implements ImageRendener {
 *     InputStream data;
 *
 *     public boolean drawImage(Graphics2D graphics,Rectangle2D anchor,Insets clip) {
 *         // draw image
 *       DataInputStream is = new DataInputStream(data);
 *       org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore wmfStore =
 *               new org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore();
 *       try {
 *           wmfStore.read(is);
 *       } catch (IOException e){
 *           return;
 *       }
 *
 *       float scale = (float)anchor.width/wmfStore.getWidthPixels();
 *
 *       org.apache.batik.transcoder.wmf.tosvg.WMFPainter painter =
 *               new org.apache.batik.transcoder.wmf.tosvg.WMFPainter(wmfStore, 0, 0, scale);
 *       graphics.translate(anchor.x, anchor.y);
 *       painter.paint(graphics);
 *     }
 *
 *     public void loadImage(InputStream data, String contentType) throws IOException {
 *         if ("image/wmf".equals(contentType)) {
 *             this.data = data;
 *             // use Apache Batik to handle WMF
 *         } else {
 *             super.loadImage(data,contentType);
 *         }
 *     }
 * }
 * </code>
 * </pre>
 *
 * and then pass this class to your instance of java.awt.Graphics2D:
 *
 * <pre>
 * <code>
 * graphics.setRenderingHint(Drawable.IMAGE_RENDERER, new MyImageRendener());
 * </code>
 * </pre>
 */
public interface ImageRenderer {
    /**
     * Determines if this image renderer implementation supports the given contentType
     * @param contentType the image content type
     * @return if the content type is supported
     */
    boolean canRender(String contentType);

    /**
     * Load and buffer the image
     *
     * @param data the raw image stream
     * @param contentType the content type
     */
    void loadImage(InputStream data, String contentType) throws IOException;

    /**
     * Load and buffer the image
     *
     * @param data the raw image bytes
     * @param contentType the content type
     */
    void loadImage(byte[] data, String contentType) throws IOException;

    /**
     * @return the format-specific / not-normalized bounds of the image
     */
    Rectangle2D getNativeBounds();

    /**
     * @return the bounds of the buffered image in pixel
     */
    Rectangle2D getBounds();

    /**
     * @return the dimension of the buffered image in pixel
     */
    default Dimension2D getDimension() {
        Rectangle2D r = getBounds();
        return new Dimension2DDouble(Math.abs(r.getWidth()), Math.abs(r.getHeight()));
    }

    /**
     * @param alpha the alpha [0..1] to be added to the image (possibly already containing an alpha channel)
     */
    void setAlpha(double alpha);

    /**
     * @return the image as buffered image or null if image could not be loaded
     */
    BufferedImage getImage();

    /**
     * @param dim the dimension in pixels of the returned image
     * @return the image as buffered image or null if image could not be loaded
     *
     * @since POI 3.15-beta2
     */
    BufferedImage getImage(Dimension2D dim);

    /**
     * Render picture data into the supplied graphics
     *
     * @return true if the picture data was successfully rendered
     */
    boolean drawImage(Graphics2D graphics, Rectangle2D anchor);

    /**
     * Render picture data into the supplied graphics
     *
     * @return true if the picture data was successfully rendered
     */
    boolean drawImage(Graphics2D graphics, Rectangle2D anchor, Insets clip);

    default GenericRecord getGenericRecord() { return null; }

    /**
     * Sets the default charset to render text elements.
     * Opposed to other windows libraries in POI this simply defaults to Windows-1252.
     *
     * @param defaultCharset the default charset
     */
    default void setDefaultCharset(Charset defaultCharset) {}


    /**
     * Dis-/Enables caching of input data for later retrieval.
     * Opposed to {@link #getImage()}, which returns a {@link BufferedImage}, the cached image can be later
     * used to embedded the original, unmodified data
     * @param enable dis-/enables caching - this is an optional operation. {@code false} removes already cached data
     */
    default void setCacheInput(boolean enable) {}

    /**
     * @return the cached image data
     */
    default byte[] getCachedImage() { return null; }

    /**
     * @return the cached content type
     */
    default String getCachedContentType() { return null; }
}