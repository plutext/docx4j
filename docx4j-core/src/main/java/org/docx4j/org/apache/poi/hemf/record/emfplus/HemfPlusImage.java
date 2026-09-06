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

import static org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readARGB;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.docx4j.org.apache.poi.hemf.usermodel.HemfPicture;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.poifs.filesystem.FileMagic;
import org.docx4j.org.apache.poi.sl.draw.ImageRenderer;
import org.docx4j.org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusImage {
    /** The ImageDataType enumeration defines types of image data formats. */
    public enum EmfPlusImageDataType {
        /** The type of image is not known. */
        UNKNOWN(0x00000000),
        /** Specifies a bitmap image. */
        BITMAP(0x00000001),
        /** Specifies a metafile image. */
        METAFILE(0x00000002),
        /** POI-specific - marks an unfinished/continuable image part */
        CONTINUED(-1);

        public final int id;

        EmfPlusImageDataType(int id) {
            this.id = id;
        }

        public static EmfPlusImageDataType valueOf(int id) {
            for (EmfPlusImageDataType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    @SuppressWarnings("unused")
    public enum EmfPlusPixelFormat {
        UNDEFINED(0X00000000),
        INDEXED_1BPP(0X00030101),
        INDEXED_4BPP(0X00030402),
        INDEXED_8BPP(0X00030803),
        GRAYSCALE_16BPP(0X00101004),
        RGB555_16BPP(0X00021005),
        RGB565_16BPP(0X00021006),
        ARGB1555_16BPP(0X00061007),
        RGB_24BPP(0X00021808),
        RGB_32BPP(0X00022009),
        ARGB_32BPP(0X0026200A),
        PARGB_32BPP(0X000E200B),
        RGB_48BPP(0X0010300C),
        ARGB_64BPP(0X0034400D),
        PARGB_64BPP(0X001A400E),
        ;

        private static final BitField CANONICAL = BitFieldFactory.getInstance(0x00200000);
        private static final BitField EXTCOLORS = BitFieldFactory.getInstance(0x00100000);
        private static final BitField PREMULTI = BitFieldFactory.getInstance(0x00080000);
        private static final BitField ALPHA = BitFieldFactory.getInstance(0x00040000);
        private static final BitField GDI = BitFieldFactory.getInstance(0x00020000);
        private static final BitField PALETTE = BitFieldFactory.getInstance(0x00010000);
        private static final BitField BPP = BitFieldFactory.getInstance(0x0000FF00);
        private static final BitField INDEX = BitFieldFactory.getInstance(0x000000FF);

        public final int id;

        EmfPlusPixelFormat(int id) {
            this.id = id;
        }

        public static EmfPlusPixelFormat valueOf(int id) {
            for (EmfPlusPixelFormat wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }

        /**
         * The pixel format enumeration index.
         */
        public int getGDIEnumIndex() {
            return id == -1 ? -1 : INDEX.getValue(id);
        }

        /**
         * The total number of bits per pixel.
         */
        public int getBitsPerPixel() {
            return id == -1 ? -1 : BPP.getValue(id);
        }

        /**
         * If set, the pixel values are indexes into a palette.
         * If clear, the pixel values are actual colors.
         */
        public boolean isPaletteIndexed() {
            return id != -1 && PALETTE.isSet(id);
        }

        /**
         * If set, the pixel format is supported in Windows GDI.
         * If clear, the pixel format is not supported in Windows GDI.
         */
        public boolean isGDISupported() {
            return id != -1 && GDI.isSet(id);
        }

        /**
         * If set, the pixel format includes an alpha transparency component.
         * If clear, the pixel format does not include a component that specifies transparency.
         */
        public boolean isAlpha() {
            return id != -1 && ALPHA.isSet(id);
        }

        /**
         * If set, each color component in the pixel has been premultiplied by the pixel's alpha transparency value.
         * If clear, each color component is multiplied by the pixel's alpha transparency value when the source pixel
         * is blended with the destination pixel.
         */
        public boolean isPreMultiplied() {
            return id != -1 && PREMULTI.isSet(id);
        }

        /**
         * If set, the pixel format supports extended colors in 16-bits per channel.
         * If clear, extended colors are not supported.
         */
        public boolean isExtendedColors() {
            return id != -1 && EXTCOLORS.isSet(id);
        }

        /**
         * If set, the pixel format is "canonical", which means that 32 bits per pixel are
         * supported, with 24-bits for color components and an 8-bit alpha channel.
         * If clear, the pixel format is not canonical.
         */
        public boolean isCanonical() {
            return id != -1 && CANONICAL.isSet(id);
        }
    }

    public enum EmfPlusBitmapDataType {
        PIXEL(0x00000000),
        COMPRESSED(0x00000001);

        public final int id;

        EmfPlusBitmapDataType(int id) {
            this.id = id;
        }

        public static EmfPlusBitmapDataType valueOf(int id) {
            for (EmfPlusBitmapDataType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    public enum EmfPlusMetafileDataType {
        Wmf(0x00000001),
        WmfPlaceable(0x00000002),
        Emf(0x00000003),
        EmfPlusOnly(0x00000004),
        EmfPlusDual(0x00000005);

        public final int id;

        EmfPlusMetafileDataType(int id) {
            this.id = id;
        }

        public static EmfPlusMetafileDataType valueOf(int id) {
            for (EmfPlusMetafileDataType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }


    /**
     * The WrapMode enumeration defines how the pattern from a texture or gradient brush is tiled
     * across a shape or at shape boundaries, when it is smaller than the area being filled.
     */
    public enum EmfPlusWrapMode {
        WRAP_MODE_TILE(0x00000000),
        WRAP_MODE_TILE_FLIP_X(0x00000001),
        WRAP_MODE_TILE_FLIP_Y(0x00000002),
        WRAP_MODE_TILE_FLIP_XY(0x00000003),
        WRAP_MODE_CLAMP(0x00000004)
        ;

        public final int id;

        EmfPlusWrapMode(int id) {
            this.id = id;
        }

        public static EmfPlusWrapMode valueOf(int id) {
            for (EmfPlusWrapMode wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    public enum EmfPlusObjectClamp {
        /** The object is clamped to a rectangle. */
        RectClamp(0x00000000),
        /** The object is clamped to a bitmap. */
        BitmapClamp(0x00000001)
        ;

        public final int id;

        EmfPlusObjectClamp(int id) {
            this.id = id;
        }

        public static EmfPlusObjectClamp valueOf(int id) {
            for (EmfPlusObjectClamp wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }


    public static class EmfPlusImage implements EmfPlusObjectData {
        private static final int MAX_OBJECT_SIZE = 50_000_000;
        private static final String GDI_CONTENT = "GDI";

        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private EmfPlusImageDataType imageDataType;
        private int bitmapWidth;
        private int bitmapHeight;
        private int bitmapStride;
        private EmfPlusPixelFormat pixelFormat;
        private EmfPlusBitmapDataType bitmapType;
        private byte[] imageData;
        private EmfPlusMetafileDataType metafileType;
        private int metafileDataSize;

        public EmfPlusImageDataType getImageDataType() {
            return imageDataType;
        }

        public byte[] getImageData() {
            return imageData;
        }

        public EmfPlusPixelFormat getPixelFormat() {
            return pixelFormat;
        }

        public EmfPlusBitmapDataType getBitmapType() {
            return bitmapType;
        }

        public int getBitmapWidth() {
            return bitmapWidth;
        }

        public int getBitmapHeight() {
            return bitmapHeight;
        }

        public int getBitmapStride() {
            return bitmapStride;
        }

        public EmfPlusMetafileDataType getMetafileType() {
            return metafileType;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            leis.mark(LittleEndianConsts.INT_SIZE);
            long size = graphicsVersion.init(leis);

            if (isContinuedRecord()) {
                // CONTINUABLE is not always correctly set, so we check the version field if this record is continued
                imageDataType = EmfPlusImageDataType.CONTINUED;
                leis.reset();
                size = 0;
            } else {
                imageDataType = EmfPlusImageDataType.valueOf(leis.readInt());
                size += LittleEndianConsts.INT_SIZE;
            }

            if (imageDataType == null) {
                imageDataType = EmfPlusImageDataType.UNKNOWN;
            }

            int fileSize;
            switch (imageDataType) {
                default:
                case UNKNOWN:
                case CONTINUED:
                    bitmapWidth = -1;
                    bitmapHeight = -1;
                    bitmapStride = -1;
                    bitmapType = null;
                    pixelFormat = null;

                    fileSize = (int) (dataSize);
                    break;

                case BITMAP:
                    // A 32-bit signed integer that specifies the width in pixels of the area occupied by the bitmap.
                    // If the image is compressed, according to the Type field, this value is undefined and MUST be ignored.
                    bitmapWidth = leis.readInt();
                    // A 32-bit signed integer that specifies the height in pixels of the area occupied by the bitmap.
                    // If the image is compressed, according to the Type field, this value is undefined and MUST be ignored.
                    bitmapHeight = leis.readInt();
                    // A 32-bit signed integer that specifies the byte offset between the beginning of one scan-line
                    // and the next. This value is the number of bytes per pixel, which is specified in the PixelFormat
                    // field, multiplied by the width in pixels, which is specified in the Width field.
                    // The value of this field MUST be a multiple of four. If the image is compressed, according to the
                    // Type field, this value is undefined and MUST be ignored.
                    bitmapStride = leis.readInt();
                    // A 32-bit unsigned integer that specifies the format of the pixels that make up the bitmap image.
                    //  The supported pixel formats are specified in the PixelFormat enumeration
                    int pixelFormatInt = leis.readInt();
                    // A 32-bit unsigned integer that specifies the metafileType of data in the BitmapData field.
                    // This value MUST be defined in the BitmapDataType enumeration
                    bitmapType = EmfPlusBitmapDataType.valueOf(leis.readInt());
                    size += 5 * LittleEndianConsts.INT_SIZE;

                    pixelFormat = (bitmapType == EmfPlusBitmapDataType.PIXEL)
                            ? EmfPlusPixelFormat.valueOf(pixelFormatInt)
                            : EmfPlusPixelFormat.UNDEFINED;
                    assert (pixelFormat != null);

                    fileSize = (int) (dataSize - size);

                    break;

                case METAFILE:
                    // A 32-bit unsigned integer that specifies the type of metafile that is embedded in the
                    // MetafileData field. This value MUST be defined in the MetafileDataType enumeration
                    metafileType = EmfPlusMetafileDataType.valueOf(leis.readInt());

                    // A 32-bit unsigned integer that specifies the size in bytes of the
                    // metafile data in the MetafileData field.
                    metafileDataSize = leis.readInt();

                    size += 2 * LittleEndianConsts.INT_SIZE;

                    // ignore metafileDataSize, which might ignore a (placeable) header in front
                    // and also use the remaining bytes, which might contain padding bytes ...
                    fileSize = (int) (dataSize - size);
                    break;
            }

            assert (fileSize <= dataSize - size);

            imageData = IOUtils.toByteArray(leis, fileSize, MAX_OBJECT_SIZE);

            // TODO: remove padding bytes between placeable WMF header and body?

            return size + fileSize;
        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        public Rectangle2D getBounds(List<? extends EmfPlusObjectData> continuedObjectData) {
            try {
                switch (getImageDataType()) {
                    case BITMAP:
                        if (getBitmapType() == EmfPlusBitmapDataType.PIXEL) {
                            return new Rectangle2D.Double(0, 0, bitmapWidth, bitmapHeight);
                        } else {
                            try(UnsynchronizedByteArrayInputStream is = UnsynchronizedByteArrayInputStream.builder().setByteArray(getRawData(continuedObjectData)).get()) {
                                BufferedImage bi = ImageIO.read(is);
                                return new Rectangle2D.Double(bi.getMinX(), bi.getMinY(), bi.getWidth(), bi.getHeight());
                            }
                        }
                    case METAFILE:
                        try(UnsynchronizedByteArrayInputStream bis = UnsynchronizedByteArrayInputStream.builder().setByteArray(getRawData(continuedObjectData)).get()) {
                            switch (getMetafileType()) {
                                case Wmf:
                                case WmfPlaceable:
                                    HwmfPicture wmf = new HwmfPicture(bis);
                                    return wmf.getBounds();
                                case Emf:
                                case EmfPlusDual:
                                case EmfPlusOnly:
                                    HemfPicture emf = new HemfPicture(bis);
                                    return emf.getBounds();
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ignored) {
            }
            return new Rectangle2D.Double(1,1,1,1);
        }

        /**
         * @param continuedObjectData list of object data
         * @return byte array
         * @throws IllegalStateException if the data cannot be read
         */
        public byte[] getRawData(List<? extends EmfPlusObjectData> continuedObjectData) {
            try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                bos.write(getImageData());
                if (continuedObjectData != null) {
                    for (EmfPlusObjectData od : continuedObjectData) {
                        bos.write(((EmfPlusImage)od).getImageData());
                    }
                }
                return bos.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();

            byte[] data = getRawData(continuedObjectData);

            String contentType = getContentType(data);
            ImageRenderer imgr = (GDI_CONTENT.equals(contentType))
                    ? getGDIRenderer() : ctx.getImageRenderer(contentType);

            try {
                imgr.loadImage(data, contentType);
            } catch (IOException ignored) {
                imgr = null;
            }

            prop.setEmfPlusImage(imgr);
        }

        /**
         * Converts the gdi pixel data to a buffered image
         * @param data the image data of all EmfPlusImage parts
         * @return the BufferedImage
         */
        public BufferedImage readGDIImage(final byte[] data) {
            return getGDIRenderer().readGDIImage(data);
        }

        private HemfPlusGDIImageRenderer getGDIRenderer() {
            if (getImageDataType() != EmfPlusImageDataType.BITMAP || getBitmapType() != EmfPlusBitmapDataType.PIXEL) {
                throw new IllegalStateException("image data is not a GDI image");
            }
            HemfPlusGDIImageRenderer renderer = new HemfPlusGDIImageRenderer();
            renderer.setWidth(getBitmapWidth());
            renderer.setHeight(getBitmapHeight());
            renderer.setStride(getBitmapStride());
            renderer.setPixelFormat(getPixelFormat());
            return renderer;
        }

        private String getContentType(final byte[] data) {
            PictureType pictureType = PictureType.UNKNOWN;
            switch (getImageDataType()) {
                case BITMAP:
                    if (getBitmapType() == EmfPlusBitmapDataType.PIXEL) {
                        return GDI_CONTENT;
                    }

                    switch (FileMagic.valueOf(data)) {
                        case GIF:
                            pictureType = PictureType.GIF;
                            break;
                        case TIFF:
                            pictureType = PictureType.TIFF;
                            break;
                        case PNG:
                            pictureType = PictureType.PNG;
                            break;
                        case JPEG:
                            pictureType = PictureType.JPEG;
                            break;
                        case BMP:
                            pictureType = PictureType.BMP;
                            break;
                    }
                    break;

                case METAFILE:
                    assert (getMetafileType() != null);
                    switch (getMetafileType()) {
                        case Wmf:
                        case WmfPlaceable:
                            pictureType = PictureType.WMF;
                            break;

                        case Emf:
                        case EmfPlusDual:
                        case EmfPlusOnly:
                            pictureType = PictureType.EMF;
                            break;
                    }
                    break;
            }

            return pictureType.contentType;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public EmfPlusObjectType getGenericRecordType() {
            return EmfPlusObjectType.IMAGE;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String,Supplier<?>> m = new LinkedHashMap<>();

            m.put("graphicsVersion", this::getGraphicsVersion);
            m.put("imageDataType", this::getImageDataType);
            m.put("bitmapWidth", this::getBitmapWidth);
            m.put("bitmapHeight", this::getBitmapHeight);
            m.put("bitmapStride", this::getBitmapStride);
            m.put("pixelFormat", this::getPixelFormat);
            m.put("bitmapType", this::getBitmapType);
            m.put("imageData", this::getImageData);
            m.put("metafileType", this::getMetafileType);
            m.put("metafileDataSize", () -> metafileDataSize);

            return Collections.unmodifiableMap(m);
        }
    }

    public static class EmfPlusImageAttributes implements EmfPlusObjectData {
        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private EmfPlusWrapMode wrapMode;
        private Color clampColor;
        private EmfPlusObjectClamp objectClamp;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            // An EmfPlusGraphicsVersion object that specifies the version of operating system graphics that
            // was used to create this object.
            long size = graphicsVersion.init(leis);

            // A 32-bit field that is not used and MUST be ignored.
            leis.skipFully(LittleEndianConsts.INT_SIZE);

            // A 32-bit unsigned integer that specifies how to handle edge conditions with a value from the WrapMode enumeration
            wrapMode = EmfPlusWrapMode.valueOf(leis.readInt());

            // An EmfPlusARGB object that specifies the edge color to use when the WrapMode value is WrapModeClamp.
            // This color is visible when the source rectangle processed by an EmfPlusDrawImage record is larger than the image itself.
            clampColor = readARGB(leis.readInt());

            // A 32-bit signed integer that specifies the object clamping behavior. It is not used until this object
            // is applied to an image being drawn. This value MUST be one of the values defined in the following table.
            objectClamp = EmfPlusObjectClamp.valueOf(leis.readInt());

            // A value that SHOULD be set to zero and MUST be ignored upon receipt.
            leis.skipFully(LittleEndianConsts.INT_SIZE);

            return size + 5*LittleEndianConsts.INT_SIZE;
        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        public EmfPlusWrapMode getWrapMode() {
            return wrapMode;
        }

        public Color getClampColor() {
            return clampColor;
        }

        public EmfPlusObjectClamp getObjectClamp() {
            return objectClamp;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "graphicsVersion", this::getGraphicsVersion,
                "wrapMode", this::getWrapMode,
                "clampColor", this::getClampColor,
                "objectClamp", this::getObjectClamp
            );
        }

        @Override
        public EmfPlusObjectType getGenericRecordType() {
            return EmfPlusObjectType.IMAGE_ATTRIBUTES;
        }
    }
}
