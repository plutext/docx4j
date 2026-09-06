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

package org.docx4j.org.apache.poi.hemf.record.emfplus;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

import org.docx4j.org.apache.poi.sl.draw.BitmapImageRenderer;
import org.docx4j.org.apache.poi.util.IOUtils;

@SuppressWarnings("unused")
public class HemfPlusGDIImageRenderer extends BitmapImageRenderer {
    private int width;
    private int height;
    private int stride;
    private HemfPlusImage.EmfPlusPixelFormat pixelFormat;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public HemfPlusImage.EmfPlusPixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(HemfPlusImage.EmfPlusPixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public boolean canRender(String contentType) {
        return true;
    }

    @Override
    public void loadImage(InputStream data, String contentType) throws IOException {
        img = readGDIImage(IOUtils.toByteArray(data));
    }

    @Override
    public void loadImage(byte[] data, String contentType) {
        img = readGDIImage(data);
    }

    /**
     * Converts the gdi pixel data to a buffered image
     * @param data the image data of all EmfPlusImage parts
     * @return the BufferedImage
     */
    public BufferedImage readGDIImage(final byte[] data) {
        int[] nBits, bOffs;
        switch (pixelFormat) {
            case ARGB_32BPP:
                nBits = new int[]{8, 8, 8, 8};
                bOffs = new int[]{2, 1, 0, 3};
                break;
            case RGB_24BPP:
                nBits = new int[]{8, 8, 8};
                bOffs = new int[]{2, 1, 0};
                break;
            default:
                throw new IllegalStateException("not yet implemented");
        }

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ComponentColorModel cm = new ComponentColorModel
            (cs, nBits, pixelFormat.isAlpha(), pixelFormat.isPreMultiplied(), Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        PixelInterleavedSampleModel csm =
            new PixelInterleavedSampleModel(cm.getTransferType(), width, height, cm.getNumComponents(), stride, bOffs);

        DataBufferByte dbb = new DataBufferByte(data, data.length);
        WritableRaster raster = (WritableRaster) Raster.createRaster(csm, dbb, null);

        return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
    }

}
