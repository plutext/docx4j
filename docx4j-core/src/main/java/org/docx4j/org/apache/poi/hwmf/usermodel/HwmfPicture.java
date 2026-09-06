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

package org.docx4j.org.apache.poi.hwmf.usermodel;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphicsState;
import org.docx4j.org.apache.poi.hwmf.record.HwmfHeader;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPlaceableHeader;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecord;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecordType;
import org.docx4j.org.apache.poi.hwmf.record.HwmfWindowing.WmfSetWindowExt;
import org.docx4j.org.apache.poi.hwmf.record.HwmfWindowing.WmfSetWindowOrg;
import org.docx4j.org.apache.poi.util.Dimension2DDouble;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.LocaleUtil;
import org.docx4j.org.apache.poi.util.RecordFormatException;
import org.docx4j.org.apache.poi.util.Units;

public class HwmfPicture implements Iterable<HwmfRecord>, GenericRecord {
    /** Max. record length - processing longer records will throw an exception */
    public static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    public static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private static final Logger LOG = LoggerFactory.getLogger(HwmfPicture.class);

    final List<HwmfRecord> records = new ArrayList<>();
    final HwmfPlaceableHeader placeableHeader;
    final HwmfHeader header;
    /** The default charset */
    private Charset defaultCharset = LocaleUtil.CHARSET_1252;

    /**
     * @param length the max record length allowed for HwmfPicture
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for HwmfPicture
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * @param inputStream The InputStream to read data from
     * @throws IOException If reading data from the file fails
     * @throws IllegalStateException a number of runtime exceptions can be thrown, especially if there are problems with the
     * input format
     */
    public HwmfPicture(InputStream inputStream) throws IOException {

        try (LittleEndianInputStream leis = new LittleEndianInputStream(inputStream)) {
            placeableHeader = HwmfPlaceableHeader.readHeader(leis);
            header = new HwmfHeader(leis);

            for (;;) {
                long recordSize;
                int recordFunction;
                try {
                    // recordSize in DWORDs
                    long recordSizeLong = leis.readUInt()*2;
                    if (recordSizeLong > Integer.MAX_VALUE) {
                        throw new RecordFormatException("record size can't be > "+Integer.MAX_VALUE);
                    } else if (recordSizeLong < 0L) {
                        throw new RecordFormatException("record size can't be < 0");
                    }
                    recordSize = (int)recordSizeLong;
                    recordFunction = leis.readShort();
                } catch (Exception e) {
                    LOG.atError().log("unexpected eof - wmf file was truncated");
                    break;
                }
                // 4 bytes (recordSize) + 2 bytes (recordFunction)
                int consumedSize = 6;
                HwmfRecordType wrt = HwmfRecordType.getById(recordFunction);
                if (wrt == null) {
                    throw new IOException("unexpected record type: "+recordFunction);
                }
                if (wrt == HwmfRecordType.eof) {
                    break;
                }
                if (wrt.constructor == null) {
                    throw new IOException("unsupported record type: "+recordFunction);
                }

                final HwmfRecord wr = wrt.constructor.get();
                records.add(wr);

                consumedSize += wr.init(leis, recordSize, recordFunction);
                int remainingSize = (int)(recordSize - consumedSize);
                if (remainingSize < 0) {
                    throw new RecordFormatException("read too many bytes. record size: "+recordSize + "; comsumed size: "+consumedSize);
                } else if(remainingSize > 0) {
                    long skipped = IOUtils.skipFully(leis, remainingSize);
                    if (skipped != (long)remainingSize) {
                        throw new RecordFormatException("Tried to skip "+remainingSize + " but skipped: "+skipped);
                    }
                }

                if (wr instanceof HwmfCharsetAware) {
                    ((HwmfCharsetAware)wr).setCharsetProvider(this::getDefaultCharset);
                }
            }
        }
    }

    public List<HwmfRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public void draw(Graphics2D ctx) {
        Dimension2D dim = getSize();
        int width = Units.pointsToPixel(dim.getWidth());
        // keep aspect ratio for height
        int height = Units.pointsToPixel(dim.getHeight());
        Rectangle2D bounds = new Rectangle2D.Double(0,0,width,height);
        draw(ctx, bounds);
    }

    public void draw(Graphics2D ctx, Rectangle2D graphicsBounds) {
        HwmfGraphicsState state = new HwmfGraphicsState();
        state.backup(ctx);
        try {
            Rectangle2D wmfBounds = getBounds();
            Rectangle2D innerBounds = getInnnerBounds();
            if (innerBounds == null) {
                innerBounds = wmfBounds;
            }

            // scale output bounds to image bounds
            ctx.translate(graphicsBounds.getCenterX(), graphicsBounds.getCenterY());
            ctx.scale(graphicsBounds.getWidth()/innerBounds.getWidth(), graphicsBounds.getHeight()/innerBounds.getHeight());
            ctx.translate(-innerBounds.getCenterX(), -innerBounds.getCenterY());


            HwmfGraphics g = new HwmfGraphics(ctx, innerBounds);
            HwmfDrawProperties prop = g.getProperties();
            prop.setViewportOrg(innerBounds.getX(), innerBounds.getY());
            prop.setViewportExt(innerBounds.getWidth(), innerBounds.getHeight());

            int idx = 0;
            for (HwmfRecord r : records) {
                prop = g.getProperties();
                Shape propClip = prop.getClip();
                Shape ctxClip = ctx.getClip();
                if (!Objects.equals(propClip, ctxClip)) {
                    int a = 5;
                }
                r.draw(g);
                idx++;
            }
        } finally {
            state.restore(ctx);
        }
    }

    /**
     * Returns the bounding box in device-independent units. Usually this is taken from the placeable header.
     *
     * @return the bounding box
     *
     * @throws IllegalStateException if neither WmfSetWindowOrg/Ext nor the placeableHeader are set
     */
    public Rectangle2D getBounds() {
        if (placeableHeader != null) {
            return placeableHeader.getBounds();
        }
        Rectangle2D inner = getInnnerBounds();
        if (inner != null) {
            return inner;
        }
        throw new IllegalStateException("invalid wmf file - window records are incomplete.");
    }

    /**
     * Returns the bounding box in device-independent units taken from the WmfSetWindowOrg/Ext records
     *
     * @return the bounding box or null, if the WmfSetWindowOrg/Ext records aren't set
     */
    public Rectangle2D getInnnerBounds() {
        WmfSetWindowOrg wOrg = null;
        WmfSetWindowExt wExt = null;
        for (HwmfRecord r : getRecords()) {
            if (r instanceof WmfSetWindowOrg) {
                wOrg = (WmfSetWindowOrg)r;
            } else if (r instanceof WmfSetWindowExt) {
                wExt = (WmfSetWindowExt)r;
            }
            if (wOrg != null && wExt != null) {
                return new Rectangle2D.Double(wOrg.getX(), wOrg.getY(), wExt.getSize().getWidth(), wExt.getSize().getHeight());
            }
        }
        return null;
    }


    public HwmfPlaceableHeader getPlaceableHeader() {
        return placeableHeader;
    }

    public HwmfHeader getHeader() {
        return header;
    }

    /**
     * Return the image bound in points
     *
     * @return the image bound in points
     */
    public Rectangle2D getBoundsInPoints() {
        double inch = (placeableHeader == null) ? 1440 : placeableHeader.getUnitsPerInch();
        Rectangle2D bounds = getBounds();

        //coefficient to translate from WMF dpi to 72dpi
        double coeff = Units.POINT_DPI/inch;
        return AffineTransform.getScaleInstance(coeff, coeff).createTransformedShape(bounds).getBounds2D();
    }


    /**
     * Return the image size in points
     *
     * @return the image size in points
     */
    public Dimension2D getSize() {
        Rectangle2D bounds = getBoundsInPoints();
        return new Dimension2DDouble(bounds.getWidth(), bounds.getHeight());
    }

    public Iterable<HwmfEmbedded> getEmbeddings() {
        return () -> new HwmfEmbeddedIterator(HwmfPicture.this);
    }

    @Override
    public Iterator<HwmfRecord> iterator() {
        return getRecords().iterator();
    }

    @Override
    public Spliterator<HwmfRecord> spliterator() {
        return getRecords().spliterator();
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }

    @Override
    public List<? extends GenericRecord> getGenericChildren() {
        return getRecords();
    }

    public void setDefaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public Charset getDefaultCharset() {
        return defaultCharset;
    }
}
