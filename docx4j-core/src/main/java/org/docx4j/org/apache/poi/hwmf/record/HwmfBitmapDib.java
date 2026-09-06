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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.RecordFormatException;

/**
 * The DeviceIndependentBitmap Object defines an image in device-independent bitmap (DIB) format.
 */
public class HwmfBitmapDib implements GenericRecord {
    // arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_HEIGHT_WIDTH = 10_000;
    protected static int MAX_HEIGHT_WIDTH = DEFAULT_MAX_HEIGHT_WIDTH;

    private static final Logger LOG = LoggerFactory.getLogger(HwmfBitmapDib.class);
    private static final int BMP_HEADER_SIZE = 14;

    public enum BitCount {
        /**
         * The image SHOULD be in either JPEG or PNG format. Neither of these formats includes
         *  a color table, so this value specifies that no color table is present. See [JFIF] and [RFC2083]
         *  for more information concerning JPEG and PNG compression formats.
         */
        BI_BITCOUNT_0(0x0000),
        /**
         * Each pixel in the bitmap is represented by a single bit. If the bit is clear, the pixel is displayed
         *  with the color of the first entry in the color table; if the bit is set, the pixel has the color of the
         *  second entry in the table.
         */
        BI_BITCOUNT_1(0x0001),
        /**
         * Each pixel in the bitmap is represented by a 4-bit index into the color table, and each byte
         *  contains 2 pixels.
         */
        BI_BITCOUNT_2(0x0004),
        /**
         * Each pixel in the bitmap is represented by an 8-bit index into the color table, and each byte
         *  contains 1 pixel.
         */
        BI_BITCOUNT_3(0x0008),
        /**
         * Each pixel in the bitmap is represented by a 16-bit value.
         * <br>
         * If the Compression field of the BitmapInfoHeader Object is BI_RGB, the Colors field of DIB
         *  is NULL. Each WORD in the bitmap array represents a single pixel. The relative intensities of
         *  red, green, and blue are represented with 5 bits for each color component. The value for blue
         *  is in the least significant 5 bits, followed by 5 bits each for green and red. The most significant
         *  bit is not used. The color table is used for optimizing colors on palette-based devices, and
         *  contains the number of entries specified by the ColorUsed field of the BitmapInfoHeader
         *  Object.
         * <br>
         * If the Compression field of the BitmapInfoHeader Object is BI_BITFIELDS, the Colors field
         *  contains three DWORD color masks that specify the red, green, and blue components,
         *  respectively, of each pixel. Each WORD in the bitmap array represents a single pixel.
         * <br>
         * When the Compression field is set to BI_BITFIELDS, bits set in each DWORD mask MUST be
         *  contiguous and SHOULD NOT overlap the bits of another mask.
         */
        BI_BITCOUNT_4(0x0010),
        /**
         * The bitmap has a maximum of 2^24 colors, and the Colors field of DIB is
         *  NULL. Each 3-byte triplet in the bitmap array represents the relative intensities of blue, green,
         *  and red, respectively, for a pixel. The Colors color table is used for optimizing colors used on
         *  palette-based devices, and MUST contain the number of entries specified by the ColorUsed
         *  field of the BitmapInfoHeader Object.
         */
        BI_BITCOUNT_5(0x0018),
        /**
         * The bitmap has a maximum of 2^24 colors.
         * <br>
         * If the Compression field of the BitmapInfoHeader Object is set to BI_RGB, the Colors field
         *  of DIB is set to NULL. Each DWORD in the bitmap array represents the relative intensities of
         *  blue, green, and red, respectively, for a pixel. The high byte in each DWORD is not used. The
         *  Colors color table is used for optimizing colors used on palette-based devices, and MUST
         *  contain the number of entries specified by the ColorUsed field of the BitmapInfoHeader
         *  Object.
         * <br>
         * If the Compression field of the BitmapInfoHeader Object is set to BI_BITFIELDS, the Colors
         *  field contains three DWORD color masks that specify the red, green, and blue components,
         *  respectively, of each pixel. Each DWORD in the bitmap array represents a single pixel.
         * <br>
         * When the Compression field is set to BI_BITFIELDS, bits set in each DWORD mask must be
         *  contiguous and should not overlap the bits of another mask. All the bits in the pixel do not
         *  need to be used.
         */
        BI_BITCOUNT_6(0x0020);

        int flag;
        BitCount(int flag) {
            this.flag = flag;
        }
        static BitCount valueOf(int flag) {
            for (BitCount bc : values()) {
                if (bc.flag == flag) return bc;
            }
            return null;
        }
    }

    public enum Compression {
        /**
         * The bitmap is in uncompressed red green blue (RGB) format that is not compressed
         * and does not use color masks.
         */
        BI_RGB(0x0000),
        /**
         * An RGB format that uses run-length encoding (RLE) compression for bitmaps
         * with 8 bits per pixel. The compression uses a 2-byte format consisting of a count byte
         * followed by a byte containing a color index.
         */
        BI_RLE8(0x0001),
        /**
         * An RGB format that uses RLE compression for bitmaps with 4 bits per pixel. The
         * compression uses a 2-byte format consisting of a count byte followed by two word-length
         * color indexes.
         */
        BI_RLE4(0x0002),
        /**
         * The bitmap is not compressed and the color table consists of three DWORD
         * color masks that specify the red, green, and blue components, respectively, of each pixel.
         * This is valid when used with 16 and 32-bits per pixel bitmaps.
         */
        BI_BITFIELDS(0x0003),
        /**
         * The image is a JPEG image, as specified in [JFIF]. This value SHOULD only be used in
         * certain bitmap operations, such as JPEG pass-through. The application MUST query for the
         * pass-through support, since not all devices support JPEG pass-through. Using non-RGB
         * bitmaps MAY limit the portability of the metafile to other devices. For instance, display device
         * contexts generally do not support this pass-through.
         */
        BI_JPEG(0x0004),
        /**
         * The image is a PNG image, as specified in [RFC2083]. This value SHOULD only be
         * used certain bitmap operations, such as JPEG/PNG pass-through. The application MUST query
         * for the pass-through support, because not all devices support JPEG/PNG pass-through. Using
         * non-RGB bitmaps MAY limit the portability of the metafile to other devices. For instance,
         * display device contexts generally do not support this pass-through.
         */
        BI_PNG(0x0005),
        /**
         * The image is an uncompressed CMYK format.
         */
        BI_CMYK(0x000B),
        /**
         * A CMYK format that uses RLE compression for bitmaps with 8 bits per pixel.
         * The compression uses a 2-byte format consisting of a count byte followed by a byte containing
         * a color index.
         */
        BI_CMYKRLE8(0x000C),
        /**
         * A CMYK format that uses RLE compression for bitmaps with 4 bits per pixel.
         * The compression uses a 2-byte format consisting of a count byte followed by two word-length
         * color indexes.
         */
        BI_CMYKRLE4(0x000D);

        int flag;
        Compression(int flag) {
            this.flag = flag;
        }
        static Compression valueOf(int flag) {
            for (Compression c : values()) {
                if (c.flag == flag) return c;
            }
            return null;
        }
    }


    private int headerSize;
    private int headerWidth;
    private int headerHeight;
    private int headerPlanes;
    private BitCount headerBitCount;
    private Compression headerCompression;
    private long headerImageSize = -1;
    @SuppressWarnings("unused")
    private int headerXPelsPerMeter = -1;
    @SuppressWarnings("unused")
    private int headerYPelsPerMeter = -1;
    private long headerColorUsed = -1;
    @SuppressWarnings("unused")
    private long headerColorImportant = -1;
    private Color[] colorTable;
    @SuppressWarnings("unused")
    private int colorMaskR,colorMaskG,colorMaskB;

    // size of header and color table, for start of image data calculation
    private int introSize;
    private byte[] imageData;

    public int init(LittleEndianInputStream leis, int recordSize) throws IOException {
        leis.mark(10000);

        // need to read the header to calculate start of bitmap data correct
        introSize = readHeader(leis);
        assert(introSize == headerSize);
        introSize += readColors(leis);
        assert(introSize < 10000);

        leis.reset();

        // The size and format of this data is determined by information in the DIBHeaderInfo field. If
        // it is a BitmapCoreHeader, the size in bytes MUST be calculated as follows:

        int bodySize = ((((headerWidth * headerPlanes *
                (headerBitCount == null ? 0 : headerBitCount.flag) + 31) & ~31) / 8) * Math.abs(headerHeight));

        // This formula SHOULD also be used to calculate the size of aData when DIBHeaderInfo is a
        // BitmapInfoHeader Object, using values from that object, but only if its Compression value is
        // BI_RGB, BI_BITFIELDS, or BI_CMYK.
        // Otherwise, the size of aData MUST be the BitmapInfoHeader Object value ImageSize.

        assert( headerSize != 0x0C || bodySize == headerImageSize);

        if (headerSize == 0x0C ||
            headerCompression == Compression.BI_RGB ||
            headerCompression == Compression.BI_BITFIELDS ||
            headerCompression == Compression.BI_CMYK) {
            int fileSize = Math.min(introSize+bodySize,recordSize);
            imageData = IOUtils.safelyAllocate(fileSize, HwmfPicture.getMaxRecordLength());
            leis.readFully(imageData, 0, introSize);
            leis.skipFully(recordSize-fileSize);
            // emfs are sometimes truncated, read as much as possible
            int readBytes = leis.read(imageData, introSize, fileSize-introSize);
            return introSize+(recordSize-fileSize)+readBytes;
        } else {
            imageData = IOUtils.safelyAllocate(recordSize, HwmfPicture.getMaxRecordLength());
            leis.readFully(imageData);
            return recordSize;
        }
    }

    protected int readHeader(LittleEndianInputStream leis) {
        int size = 0;

        /*
         * DIBHeaderInfo (variable): Either a BitmapCoreHeader Object or a
         * BitmapInfoHeader Object that specifies information about the image.
         *
         * The first 32 bits of this field is the HeaderSize value.
         * If it is 0x0000000C, then this is a BitmapCoreHeader; otherwise, this is a BitmapInfoHeader.
         */
        headerSize = leis.readInt();
        size += LittleEndianConsts.INT_SIZE;

        if (headerSize == 0x0C) {
            // BitmapCoreHeader
            // A 16-bit unsigned integer that defines the width of the DIB, in pixels.
            headerWidth = leis.readUShort();
            // A 16-bit unsigned integer that defines the height of the DIB, in pixels.
            headerHeight = leis.readUShort();
            // A 16-bit unsigned integer that defines the number of planes for the target
            // device. This value MUST be 0x0001.
            headerPlanes = leis.readUShort();
            // A 16-bit unsigned integer that defines the format of each pixel, and the
            // maximum number of colors in the DIB.
            headerBitCount = BitCount.valueOf(leis.readUShort());
            size += 4*LittleEndianConsts.SHORT_SIZE;
        } else {
            // fix header size, sometimes this is invalid
            headerSize = 40;

            // BitmapInfoHeader
            // A 32-bit signed integer that defines the width of the DIB, in pixels.
            // This value MUST be positive.
            // This field SHOULD specify the width of the decompressed image file,
            // if the Compression value specifies JPEG or PNG format.
            headerWidth = leis.readInt();
            // A 32-bit signed integer that defines the height of the DIB, in pixels.
            // This value MUST NOT be zero.
            // - If this value is positive, the DIB is a bottom-up bitmap,
            //   and its origin is the lower-left corner.
            //   This field SHOULD specify the height of the decompressed image file,
            //   if the Compression value specifies JPEG or PNG format.
            // - If this value is negative, the DIB is a top-down bitmap,
            //   and its origin is the upper-left corner. Top-down bitmaps do not support compression.
            headerHeight = leis.readInt();
            // A 16-bit unsigned integer that defines the number of planes for the target
            // device. This value MUST be 0x0001.
            headerPlanes = leis.readUShort();
            // A 16-bit unsigned integer that defines the format of each pixel, and the
            // maximum number of colors in the DIB.
            headerBitCount = BitCount.valueOf(leis.readUShort());
            // A 32-bit unsigned integer that defines the compression mode of the DIB.
            // This value MUST NOT specify a compressed format if the DIB is a top-down bitmap,
            // as indicated by the Height value.
            headerCompression = Compression.valueOf((int)leis.readUInt());
            // A 32-bit unsigned integer that defines the size, in bytes, of the image.
            // If the Compression value is BI_RGB, this value SHOULD be zero and MUST be ignored.
            // If the Compression value is BI_JPEG or BI_PNG, this value MUST specify the size of the JPEG
            // or PNG image buffer, respectively.
            headerImageSize = leis.readUInt();
            // A 32-bit signed integer that defines the horizontal resolution,
            // in pixels-per-meter, of the target device for the DIB.
            headerXPelsPerMeter = leis.readInt();
            // A 32-bit signed integer that defines the vertical resolution,
            headerYPelsPerMeter = leis.readInt();
            // A 32-bit unsigned integer that specifies the number of indexes in the
            // color table used by the DIB
            // in pixelsper-meter, of the target device for the DIB.
            headerColorUsed = leis.readUInt();
            // A 32-bit unsigned integer that defines the number of color indexes that are
            // required for displaying the DIB. If this value is zero, all color indexes are required.
            headerColorImportant = leis.readUInt();
            size += 8*LittleEndianConsts.INT_SIZE+2*LittleEndianConsts.SHORT_SIZE;
        }
        return size;
    }

    protected int readColors(LittleEndianInputStream leis) throws IOException {
        if (headerBitCount == null) {
            return 0;
        }

        switch (headerBitCount) {
        default:
        case BI_BITCOUNT_0:
            // no table
            return 0;
        case BI_BITCOUNT_1:
            // 2 colors
            return readRGBQuad(leis, (int)(headerColorUsed == 0 ? 2 : Math.min(headerColorUsed,2)));
        case BI_BITCOUNT_2:
            // 16 colors
            return readRGBQuad(leis, (int)(headerColorUsed == 0 ? 16 : Math.min(headerColorUsed,16)));
        case BI_BITCOUNT_3:
            // 256 colors
            return readRGBQuad(leis, (int)(headerColorUsed == 0 ? 256 : Math.min(headerColorUsed,256)));
        case BI_BITCOUNT_4:
            switch (headerCompression) {
            case BI_RGB:
                colorMaskB = 0x1F;
                colorMaskG = 0x1F<<5;
                colorMaskR = 0x1F<<10;
                return 0;
            case BI_BITFIELDS:
                colorMaskB = leis.readInt();
                colorMaskG = leis.readInt();
                colorMaskR = leis.readInt();
                return 3*LittleEndianConsts.INT_SIZE;
            default:
                throw new IOException("Invalid compression option ("+headerCompression+") for bitcount ("+headerBitCount+").");
            }
        case BI_BITCOUNT_5:
        case BI_BITCOUNT_6:
            switch (headerCompression) {
            case BI_RGB:
                colorMaskR=0xFF;
                colorMaskG=0xFF;
                colorMaskB=0xFF;
                return 0;
            case BI_BITFIELDS:
                colorMaskB = leis.readInt();
                colorMaskG = leis.readInt();
                colorMaskR = leis.readInt();
                return 3*LittleEndianConsts.INT_SIZE;
            default:
                throw new IOException("Invalid compression option ("+headerCompression+") for bitcount ("+headerBitCount+").");
            }
        }
    }

    protected int readRGBQuad(LittleEndianInputStream leis, int count) throws IOException {
        int size = 0;
        colorTable = new Color[count];
        for (int i=0; i<count; i++) {
            int blue = leis.readUByte();
            int green = leis.readUByte();
            int red = leis.readUByte();
            @SuppressWarnings("unused")
            int reserved = leis.readUByte();
            colorTable[i] = new Color(red, green, blue);
            size += 4 * LittleEndianConsts.BYTE_SIZE;
        }
        return size;
    }

    public boolean isValid() {
        // the recordsize ended before the image data
        if (imageData == null) {
            return false;
        }

        // ignore all black mono-brushes
        if (this.headerBitCount == BitCount.BI_BITCOUNT_1) {
            if (colorTable == null) {
                return false;
            }

            for (Color c : colorTable) {
                if (!Color.BLACK.equals(c)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    public InputStream getBMPStream() {
        return new ByteArrayInputStream(getBMPData());
    }

    public byte[] getBMPData() {
        if (headerWidth <= 0 || headerHeight <= 0) {
            return null;
        }

        if (imageData == null) {
            throw new RecordFormatException("used to throw exception: bitmap not initialized ... need to call init() before");
        }

        // sometimes there are missing bytes after the imageData which will be 0-filled
        int imageSize = (int)Math.max(imageData.length, introSize+headerImageSize);

        // create the image data and leave the parsing to the ImageIO api
        byte[] buf = IOUtils.safelyAllocate(BMP_HEADER_SIZE + (long)imageSize, HwmfPicture.getMaxRecordLength());

        // https://en.wikipedia.org/wiki/BMP_file_format #  Bitmap file header
        buf[0] = (byte)'B';
        buf[1] = (byte)'M';
        // the full size of the bmp
        LittleEndian.putInt(buf, 2, BMP_HEADER_SIZE+imageSize);
        // the next 4 bytes are unused
        LittleEndian.putInt(buf, 6, 0);
        // start of image = BMP header length + dib header length + color tables length
        LittleEndian.putInt(buf, 10, BMP_HEADER_SIZE + introSize);
        // fill the "known" image data
        System.arraycopy(imageData, 0, buf, BMP_HEADER_SIZE, imageData.length);

        return buf;
    }

    public BufferedImage getImage() {
        return getImage(null, null, false);
    }

    public BufferedImage getImage(Color foreground, Color background, boolean hasAlpha) {
        BufferedImage bi;
        try {
            bi = ImageIO.read(getBMPStream());
        } catch (IOException|RuntimeException e) {
            LOG.atError().log("invalid bitmap data - returning placeholder image");
            return getPlaceholder();
        }

        if (foreground != null && background != null && headerBitCount == BitCount.BI_BITCOUNT_1) {
            IndexColorModel cmOld = (IndexColorModel)bi.getColorModel();
            int fg = foreground.getRGB();
            int bg = background.getRGB() & (hasAlpha ? 0xFFFFFF : 0xFFFFFFFF);
            boolean ordered = (cmOld.getRGB(0) & 0xFFFFFF) == (bg & 0xFFFFFF);
            int transPixel = ordered ? 0 : 1;
            int[] cmap = ordered ? new int[]{ bg, fg } : new int[]{ fg, bg };
            int transferType = bi.getData().getTransferType();

            IndexColorModel cmNew = new IndexColorModel(1, 2, cmap, 0, hasAlpha, transPixel, transferType);
            bi = new BufferedImage(cmNew, bi.getRaster(), false, null);
        }

        return bi;
    }

    @Override
    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("headerSize", () -> headerSize);
        m.put("width", () -> headerWidth);
        m.put("height", () -> headerHeight);
        m.put("planes", () -> headerPlanes);
        m.put("bitCount", () -> headerBitCount);
        m.put("compression", () -> headerCompression);
        m.put("imageSize", () -> headerImageSize);
        m.put("xPelsPerMeter", () -> headerXPelsPerMeter);
        m.put("yPelsPerMeter", () -> headerYPelsPerMeter);
        m.put("colorUsed", () -> headerColorUsed);
        m.put("colorImportant", () -> headerColorImportant);
        m.put("image", this::getImage);
        m.put("bmpData", this::getBMPData);
        return Collections.unmodifiableMap(m);
    }

    protected BufferedImage getPlaceholder() {
        if (headerHeight <= 0 || headerWidth <= 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        if (headerHeight > MAX_HEIGHT_WIDTH || headerWidth > MAX_HEIGHT_WIDTH) {
            throw new RecordFormatException("The width or height specified in the header exceed the current "
                    + "limit. Height: " + headerHeight + ", width: " + headerWidth +
                    ", Max width/height: " + MAX_HEIGHT_WIDTH +
                    ". Limits can be adjusted via 'HwmfBitmapDib.setMaxHeightWidth'");
        }

        BufferedImage bi = new BufferedImage(headerWidth, headerHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, headerWidth, headerHeight);

        final int arcs = Math.min(headerWidth, headerHeight) / 7;

        Color bg = Color.LIGHT_GRAY;
        Color fg = Color.GRAY;
        LinearGradientPaint lgp = new LinearGradientPaint(0f, 0f, 5, 5,
                new float[] {0,.1f,.1001f}, new Color[] {fg,fg,bg}, MultipleGradientPaint.CycleMethod.REFLECT);
        g.setComposite(AlphaComposite.SrcOver.derive(0.4f));
        g.setPaint(lgp);
        g.fillRoundRect(0, 0, headerWidth-1, headerHeight-1, arcs, arcs);

        g.setColor(Color.DARK_GRAY);
        g.setComposite(AlphaComposite.Src);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(0, 0, headerWidth-1, headerHeight-1, arcs, arcs);
        g.dispose();
        return bi;
    }

    /**
     * Adjust limit to prevent broken images from exceeding available
     * memory when being drawn.
     *
     * @param length the max number of pixel of width/height to allow for images
     */
    public static void setMaxHeightWidth(int length) {
        MAX_HEIGHT_WIDTH = length;
    }

    /**
     * @return the max number of pixel of width/height to allow for images
     */
    public static int getMaxHeightWidth() {
        return MAX_HEIGHT_WIDTH;
    }
}
