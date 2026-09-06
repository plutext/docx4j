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

package org.docx4j.org.apache.poi.hwmf.record;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * The BinaryRasterOperation Enumeration section lists the binary raster-operation codes.
 * Rasteroperation codes define how metafile processing combines the bits from the selected
 * pen with the bits in the destination bitmap.
 *
 * Each raster-operation code represents a Boolean operation in which the values of the pixels in the
 * selected pen and the destination bitmap are combined. Following are the two operands used in these
 * operations.
 *
 * <table>
 * <caption>Raster-operation code meaning</caption>
 * <tr><th>Operand</th><th>Meaning</th></tr>
 * <tr><td>P</td><td>Selected pen</td></tr>
 * <tr><td>D</td><td>Destination bitmap</td></tr>
 * </table>
 *
 * Following are the Boolean operators used in these operations.
 * <table>
 * <caption>Boolean operator meaning</caption>
 * <tr><th>Operand</th><th>Meaning</th></tr>
 * <tr><td>a</td><td>Bitwise AND</td></tr>
 * <tr><td>n</td><td>Bitwise NOT (inverse)</td></tr>
 * <tr><td>o</td><td>Bitwise OR</td></tr>
 * <tr><td>x</td><td>Bitwise exclusive OR (XOR)</td></tr>
 * </table>
 *
 * All Boolean operations are presented in reverse Polish notation. For example, the following
 * operation replaces the values of the pixels in the destination bitmap with a combination of the pixel
 * values of the pen and the selected brush: DPo.
 *
 * Each raster-operation code is a 32-bit integer whose high-order word is a Boolean operation index and
 * whose low-order word is the operation code. The 16-bit operation index is a zero-extended, 8-bit
 * value that represents all possible outcomes resulting from the Boolean operation on two parameters
 * (in this case, the pen and destination values). For example, the operation indexes for the DPo and
 * DPan operations are shown in the following list.
 *
 * <table>
 * <caption>Raster-operation examples</caption>
 * <tr><th>P</th><th>D</th><th>DPo</th><th>DPan</th></tr>
 * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
 * <tr><td>0</td><td>1</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>1</td><td>1</td><td>0</td></tr>
 * </table>
 *
 */
public enum HwmfBinaryRasterOp {
    /** 0, Pixel is always 0 */
    R2_BLACK(0x0001, HwmfBinaryRasterOp::R2_BLACK),
    /** DPon, Pixel is the inverse of the R2_MERGEPEN color. */
    R2_NOTMERGEPEN(0x0002, HwmfBinaryRasterOp::R2_NOTMERGEPEN),
    /** DPna, Pixel is a combination of the screen color and the inverse of the pen color. */
    R2_MASKNOTPEN(0x0003, HwmfBinaryRasterOp::R2_MASKNOTPEN),
    /** Pn, Pixel is the inverse of the pen color. */
    R2_NOTCOPYPEN(0x0004, HwmfBinaryRasterOp::R2_NOTCOPYPEN),
    /** PDna, Pixel is a combination of the colors common to both the pen and the inverse of the screen. */
    R2_MASKPENNOT(0x0005, HwmfBinaryRasterOp::R2_MASKPENNOT),
    /** Dn, Pixel is the inverse of the screen color. */
    R2_NOT(0x0006, HwmfBinaryRasterOp::R2_NOT),
    /** DPx, Pixel is a combination of the colors in the pen or in the screen, but not in both. */
    R2_XORPEN(0x0007, HwmfBinaryRasterOp::R2_XORPEN),
    /** DPan, Pixel is the inverse of the R2_MASKPEN color. */
    R2_NOTMASKPEN(0x0008, HwmfBinaryRasterOp::R2_NOTMASKPEN),
    /** DPa, Pixel is a combination of the colors common to both the pen and the screen. */
    R2_MASKPEN(0x0009, HwmfBinaryRasterOp::R2_MASKPEN),
    /** DPxn, Pixel is the inverse of the R2_XORPEN color. */
    R2_NOTXORPEN(0x000A, HwmfBinaryRasterOp::R2_NOTXORPEN),
    /** D, Pixel remains unchanged. */
    R2_NOP(0x000B, HwmfBinaryRasterOp::R2_NOP),
    /** DPno, Pixel is a combination of the colors common to both the screen and the inverse of the pen. */
    R2_MERGENOTPEN(0x000C, HwmfBinaryRasterOp::R2_MERGENOTPEN),
    /** P, Pixel is the pen color. */
    R2_COPYPEN(0x000D, HwmfBinaryRasterOp::R2_COPYPEN),
    /** PDno, Pixel is a combination of the pen color and the inverse of the screen color.*/
    R2_MERGEPENNOT(0x000E, HwmfBinaryRasterOp::R2_MERGEPENNOT),
    /** DPo, Pixel is a combination of the pen color and the screen color. */
    R2_MERGEPEN(0x000F, HwmfBinaryRasterOp::R2_MERGEPEN),
    /** 1, Pixel is always 1 */
    R2_WHITE(0x0010, HwmfBinaryRasterOp::R2_WHITE);

    private final int opIndex;
    private final BiConsumer<int[],int[]> op;

    HwmfBinaryRasterOp(int opIndex, BiConsumer<int[],int[]> op) {
        this.opIndex = opIndex;
        this.op = op;
    }

    public int getOpIndex() {
        return opIndex;
    }

    public static HwmfBinaryRasterOp valueOf(int opIndex) {
        for (HwmfBinaryRasterOp bb : HwmfBinaryRasterOp.values()) {
            if (bb.opIndex == opIndex) {
                return bb;
            }
        }
        return null;
    }

    public void process(int[] srcPixels, int[] dstPixels) {
        op.accept(srcPixels, dstPixels);
    }

    /** 0, Pixel is always 0 */
    private static void R2_BLACK(int[] srcPixels, int[] dstPixels) {
        Arrays.fill(dstPixels, 0xFF000000);
    }

    /** DPon, Pixel is the inverse of the R2_MERGEPEN color */
    private static void R2_NOTMERGEPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | (~(dstPixels[x] | srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** DPna, Pixel is a combination of the screen color and the inverse of the pen color. */
    private static void R2_MASKNOTPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | ((dstPixels[x] & ~srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** Pn, Pixel is the inverse of the pen color. */
    private static void R2_NOTCOPYPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | (~srcPixels[x] & 0x00FFFFFF);
        }
    }

    /** PDna, Pixel is a combination of the colors common to both the pen and the inverse of the screen. */
    private static void R2_MASKPENNOT(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | ((srcPixels[x] & ~dstPixels[x]) & 0x00FFFFFF);
        }
    }

    /** Dn, Pixel is the inverse of the screen color. */
    private static void R2_NOT(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | (~dstPixels[x] & 0x00FFFFFF);
        }
    }

    /** DPx, Pixel is a combination of the colors in the pen or in the screen, but not in both. */
    private static void R2_XORPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | ((dstPixels[x] ^ srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** DPan, Pixel is the inverse of the R2_MASKPEN color. */
    private static void R2_NOTMASKPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | (~(dstPixels[x] & srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** DPa, Pixel is a combination of the colors common to both the pen and the screen. */
    private static void R2_MASKPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | ((dstPixels[x] & srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** DPxn, Pixel is the inverse of the R2_XORPEN color. */
    private static void R2_NOTXORPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | (~(dstPixels[x] ^ srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** D, Pixel remains unchanged. */
    private static void R2_NOP(int[] srcPixels, int[] dstPixels) {
    }

    /** DPno, Pixel is a combination of the colors common to both the screen and the inverse of the pen. */
    private static void R2_MERGENOTPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | ((dstPixels[x] | ~srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** P, Pixel is the pen color. */
    private static void R2_COPYPEN(int[] srcPixels, int[] dstPixels) {
        // TODO: keep targets alpha?
        System.arraycopy(srcPixels, 0, dstPixels, 0, srcPixels.length);
    }

    /** PDno, Pixel is a combination of the pen color and the inverse of the screen color. */
    private static void R2_MERGEPENNOT(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | ((srcPixels[x] & ~dstPixels[x]) & 0x00FFFFFF);
        }
    }

    /** DPo, Pixel is a combination of the pen color and the screen color. */
    private static void R2_MERGEPEN(int[] srcPixels, int[] dstPixels) {
        for (int x = 0; x < srcPixels.length; x++) {
            dstPixels[x] = (dstPixels[x] & 0xFF000000) | ((dstPixels[x] | srcPixels[x]) & 0x00FFFFFF);
        }
    }

    /** 1, Pixel is always 1 */
    private static void R2_WHITE(int[] srcPixels, int[] dstPixels) {
        Arrays.fill(dstPixels, 0xFFFFFFFF);
    }
}
