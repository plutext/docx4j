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

package org.docx4j.org.apache.poi.hemf.draw;

import static org.docx4j.org.apache.poi.hwmf.draw.HwmfImageRenderer.getOuterBounds;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.usermodel.HemfPicture;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphicsState;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfImageRenderer;
import org.docx4j.org.apache.poi.sl.draw.BitmapImageRenderer;
import org.docx4j.org.apache.poi.sl.draw.Drawable;
import org.docx4j.org.apache.poi.sl.draw.EmbeddedExtractor;
import org.docx4j.org.apache.poi.sl.draw.ImageRenderer;
import org.docx4j.org.apache.poi.sl.usermodel.PictureData;
import org.docx4j.org.apache.poi.util.Units;

@SuppressWarnings("unused")
public class HemfImageRenderer implements ImageRenderer, EmbeddedExtractor {
    HemfPicture image;
    double alpha;
    boolean charsetInitialized = false;

    @Override
    public boolean canRender(String contentType) {
        return PictureData.PictureType.EMF.contentType.equalsIgnoreCase(contentType);
    }

    @Override
    public void loadImage(InputStream data, String contentType) throws IOException {
        if (!PictureData.PictureType.EMF.contentType.equals(contentType)) {
            throw new IOException("Invalid picture type");
        }
        image = new HemfPicture(data);
    }

    @Override
    public void loadImage(byte[] data, String contentType) throws IOException {
        if (!PictureData.PictureType.EMF.contentType.equals(contentType)) {
            throw new IOException("Invalid picture type");
        }
        image = new HemfPicture(UnsynchronizedByteArrayInputStream.builder().setByteArray(data).get());
    }

    @Override
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public BufferedImage getImage() {
        return getImage(getDimension());
    }

    @Override
    public BufferedImage getImage(Dimension2D dim) {
        if (image == null) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage bufImg = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        image.draw(g, new Rectangle2D.Double(0,0,dim.getWidth(),dim.getHeight()));
        g.dispose();

        return BitmapImageRenderer.setAlpha(bufImg, alpha);
    }

    @Override
    public boolean drawImage(Graphics2D graphics, Rectangle2D anchor) {
        return drawImage(graphics, anchor, null);
    }

    @Override
    public boolean drawImage(Graphics2D graphics, Rectangle2D anchor, Insets clip) {
        if (image == null) {
            return false;
        }

        Charset cs = (Charset)graphics.getRenderingHint(Drawable.DEFAULT_CHARSET);
        if (cs != null && !charsetInitialized) {
            setDefaultCharset(cs);
        }

        HwmfGraphicsState graphicsState = new HwmfGraphicsState();
        graphicsState.backup(graphics);

        try {
            if (clip != null) {
                graphics.clip(anchor);
            } else {
                clip = new Insets(0, 0, 0, 0);
            }

            image.draw(graphics, getOuterBounds(anchor, clip));
        } finally {
            graphicsState.restore(graphics);
        }

        return true;
    }

    @Override
    public GenericRecord getGenericRecord() {
        return image;
    }

    @Override
    public Iterable<EmbeddedPart> getEmbeddings() {
        return HwmfImageRenderer.getEmbeddings(image.getEmbeddings());
    }

    @Override
    public Rectangle2D getNativeBounds() {
        return image.getBounds();
    }

    @Override
    public Rectangle2D getBounds() {
        return Units.pointsToPixel(image == null ? new Rectangle2D.Double() : image.getBoundsInPoints());
    }

    @Override
    public void setDefaultCharset(Charset defaultCharset) {
        image.setDefaultCharset(defaultCharset);
        charsetInitialized = true;
    }
}
