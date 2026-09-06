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

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.docx4j.org.apache.poi.hwmf.record.HwmfBinaryRasterOp;

/**
 * HWMFs Raster Operation for Binary arguments (Source / Destination)
 */
public class HwmfROP2Composite implements Composite {

    private final HwmfBinaryRasterOp op;

    public HwmfROP2Composite(HwmfBinaryRasterOp op) {
        this.op = op;
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new ROP2Context(op);
    }

    private static class ROP2Context implements CompositeContext {
        private final HwmfBinaryRasterOp op;

        public ROP2Context(HwmfBinaryRasterOp op) {
            this.op = op;
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = Math.min(src.getWidth(), dstIn.getWidth());
            int h = Math.min(src.getHeight(), dstIn.getHeight());

            int[] srcPixels = new int[w];
            int[] dstPixels = new int[w];

            for (int y = 0; y < h; y++) {
                src.getDataElements(0, y, w, 1, srcPixels);
                dstIn.getDataElements(0, y, w, 1, dstPixels);
                op.process(srcPixels, dstPixels);
                dstOut.setDataElements(0, y, w, 1, dstPixels);
            }
        }

        @Override
        public void dispose() {
        }
    }
}
