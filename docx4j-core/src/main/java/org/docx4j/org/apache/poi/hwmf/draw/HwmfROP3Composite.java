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

package org.docx4j.org.apache.poi.hwmf.draw;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayDeque;
import java.util.Deque;

import org.docx4j.org.apache.poi.hwmf.record.HwmfTernaryRasterOp;

/**
 * HWMFs Raster Operation for Ternary arguments (Source / Destination / Pattern)
 */
public class HwmfROP3Composite implements Composite {
    private final HwmfTernaryRasterOp rop3;
    private final byte[] mask;
    private final int mask_width;
    private final int mask_height;
    private final int foreground;
    private final int background;
    private final Point2D startPnt;
    private final boolean hasPattern;

    public HwmfROP3Composite(AffineTransform at, Shape shape, HwmfTernaryRasterOp rop3, BufferedImage bitmap, Color background, Color foreground) {
        this.rop3 = rop3;
        if (bitmap == null) {
            mask_width = 1;
            mask_height = 1;
            mask = new byte[]{1};
        } else {
            mask_width = bitmap.getWidth();
            mask_height = bitmap.getHeight();
            mask = new byte[mask_width * mask_height];
            bitmap.getRaster().getDataElements(0, 0, mask_width, mask_height, mask);
        }
        this.background = background.getRGB();
        this.foreground = foreground.getRGB();

        Rectangle2D bnds = at.createTransformedShape(shape.getBounds2D()).getBounds2D();
        startPnt = new Point2D.Double(bnds.getMinX(),bnds.getMinY());
        hasPattern = rop3.calcCmd().contains("P");
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new Rop3Context();
    }

    private class Rop3Context implements CompositeContext {
        private final Deque<int[]> stack = new ArrayDeque<>();
//        private Integer origOffsetX, origOffsetY;

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = Math.min(src.getWidth(), dstIn.getWidth());
            int h = Math.min(src.getHeight(), dstIn.getHeight());

            int startX = (int)startPnt.getX();
            int startY = (int)startPnt.getY();
            int offsetY = dstIn.getSampleModelTranslateY();
            int offsetX = dstIn.getSampleModelTranslateX();

            final int[] srcPixels = new int[w];
            final int[] dstPixels = new int[w];
            final int[] patPixels = hasPattern ? new int[w] : null;

            for (int y = 0; y < h; y++) {
                dstIn.getDataElements(0, y, w, 1, dstPixels);
                src.getDataElements(0, y, w, 1, srcPixels);

                fillPattern(patPixels, y, startX, startY, offsetX, offsetY);

                rop3.process(stack, dstPixels, srcPixels, patPixels);
                assert(stack.size() == 1);

                int[] dstOutPixels = stack.pop();

                dstOut.setDataElements(0, y, w, 1, dstOutPixels);
            }
        }

        private void fillPattern(int[] patPixels, int y, int startX, int startY, int offsetX, int offsetY) {
            if (patPixels != null) {
                int offY2 = (startY+y+offsetY) % mask_height;
                offY2 = (offY2 < 0) ? mask_height + offY2 : offY2;
                int maskBase = offY2 * mask_width;
                for (int i=0; i<patPixels.length; i++) {
                    int offX2 = (startX+i+offsetX) % mask_width;
                    offX2 = (offX2 < 0) ? mask_width + offX2 : offX2;
                    patPixels[i] = mask[maskBase + offX2] == 0 ? background : foreground;
                }
            }
        }

        @Override
        public void dispose() {
        }
    }
}
