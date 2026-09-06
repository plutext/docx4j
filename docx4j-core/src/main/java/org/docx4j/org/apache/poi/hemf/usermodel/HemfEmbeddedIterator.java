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

package org.docx4j.org.apache.poi.hemf.usermodel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfComment;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataFormat;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataGeneric;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataMultiformats;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataPlus;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataWMF;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfRecord;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusImage;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusBitmapDataType;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImage;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObject;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfEmbedded;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfEmbeddedType;
import org.docx4j.org.apache.poi.poifs.filesystem.FileMagic;
import org.docx4j.org.apache.poi.util.IOUtils;

public class HemfEmbeddedIterator implements Iterator<HwmfEmbedded> {
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private final Deque<Iterator<?>> iterStack = new ArrayDeque<>();
    private Object current;

    /**
     * @param length the max record length allowed for HemfEmbeddedIterator
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for HemfEmbeddedIterator
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public HemfEmbeddedIterator(HemfPicture emf) {
        this(emf.getRecords().iterator());
    }

    public HemfEmbeddedIterator(Iterator<HemfRecord> recordIterator) {
        iterStack.add(recordIterator);
    }

    @Override
    public boolean hasNext() {
        return moveNext();
    }

    private boolean moveNext() {
        if (iterStack.isEmpty()) {
            return false;
        }

        if (current != null) {
            // don't search twice and potentially skip items
            return true;
        }

        Iterator<?> iter;
        do {
            iter = iterStack.peek();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof EmfComment) {
                    HemfComment.EmfCommentData cd = ((EmfComment)obj).getCommentData();
                    if (
                        cd instanceof EmfCommentDataWMF ||
                        cd instanceof EmfCommentDataGeneric
                    ) {
                        current = obj;
                        return true;
                    }

                    if (cd instanceof EmfCommentDataMultiformats) {
                        Iterator<?> iter2 = ((EmfCommentDataMultiformats)cd).getFormats().iterator();
                        if (iter2.hasNext()) {
                            iterStack.push(iter2);
                            continue;
                        }
                    }

                    if (cd instanceof EmfCommentDataPlus) {
                        Iterator<?> iter2 = ((EmfCommentDataPlus)cd).getRecords().iterator();
                        if (iter2.hasNext()) {
                            iter = iter2;
                            iterStack.push(iter2);
                            continue;
                        }
                    }
                }

                if (obj instanceof EmfCommentDataFormat) {
                    current = obj;
                    return true;
                }

                if (obj instanceof EmfPlusObject && ((EmfPlusObject)obj).getObjectType() == EmfPlusObjectType.IMAGE) {
                    current = obj;
                    return true;
                }

                if (obj instanceof HwmfFill.WmfStretchDib) {
                    HwmfBitmapDib bitmap = ((HwmfFill.WmfStretchDib) obj).getBitmap();
                    if (bitmap.isValid()) {
                        current = obj;
                        return true;
                    }
                }
            }
            iterStack.pop();
        } while (!iterStack.isEmpty());

        return false;
    }

    @Override
    public HwmfEmbedded next() {
        HwmfEmbedded emb;
        if ((emb = checkEmfCommentDataWMF()) != null) {
            return emb;
        }
        if ((emb = checkEmfCommentDataGeneric()) != null) {
            return emb;
        }
        if ((emb = checkEmfCommentDataFormat()) != null) {
            return emb;
        }
        if ((emb = checkEmfPlusObject()) != null) {
            return emb;
        }
        if ((emb = checkWmfStretchDib()) != null) {
            return emb;
        }

        throw new NoSuchElementException("no further embedded wmf records found.");
    }

    private HwmfEmbedded checkEmfCommentDataWMF() {
        if (!(current instanceof EmfComment && ((EmfComment)current).getCommentData() instanceof EmfCommentDataWMF)) {
            return null;
        }

        EmfCommentDataWMF wmf = (EmfCommentDataWMF)((EmfComment)current).getCommentData();
        HwmfEmbedded emb = new HwmfEmbedded();
        emb.setEmbeddedType(HwmfEmbeddedType.WMF);
        emb.setData(wmf.getWMFData());
        current = null;
        return emb;
    }

    private HwmfEmbedded checkEmfCommentDataGeneric() {
        if (!(current instanceof EmfComment && ((EmfComment)current).getCommentData() instanceof EmfCommentDataGeneric)) {
            return null;
        }
        EmfCommentDataGeneric cdg = (EmfCommentDataGeneric)((EmfComment)current).getCommentData();
        HwmfEmbedded emb = new HwmfEmbedded();
        emb.setEmbeddedType(HwmfEmbeddedType.UNKNOWN);
        emb.setData(cdg.getPrivateData());
        current = null;
        return emb;
    }

    private HwmfEmbedded checkEmfCommentDataFormat() {
        if (!(current instanceof EmfCommentDataFormat)) {
            return null;
        }
        EmfCommentDataFormat cdf = (EmfCommentDataFormat)current;
        HwmfEmbedded emb = new HwmfEmbedded();
        boolean isEmf = (cdf.getSignature() == HemfComment.EmfFormatSignature.ENHMETA_SIGNATURE);
        emb.setEmbeddedType(isEmf ? HwmfEmbeddedType.EMF : HwmfEmbeddedType.EPS);
        emb.setData(cdf.getRawData());
        current = null;
        return emb;
    }

    private HwmfEmbedded checkWmfStretchDib() {
        if (!(current instanceof HwmfFill.WmfStretchDib)) {
            return null;
        }
        HwmfEmbedded emb = new HwmfEmbedded();
        emb.setData(((HwmfFill.WmfStretchDib) current).getBitmap().getBMPData());
        emb.setEmbeddedType(HwmfEmbeddedType.BMP);
        current = null;
        return emb;
    }

    private HwmfEmbedded checkEmfPlusObject() {
        if (!(current instanceof EmfPlusObject)) {
            return null;
        }

        EmfPlusObject epo = (EmfPlusObject)current;
        assert(epo.getObjectType() == EmfPlusObjectType.IMAGE);
        EmfPlusImage img = epo.getObjectData();
        assert(img.getImageDataType() != null);

        final HwmfEmbedded emb = getEmfPlusImageData();
        if (emb == null) {
            return null;
        }

        final HwmfEmbeddedType et;
        switch (img.getImageDataType()) {
            case BITMAP:
                if (img.getBitmapType() == EmfPlusBitmapDataType.COMPRESSED) {
                    switch (FileMagic.valueOf(emb.getRawData())) {
                        case JPEG:
                            et = HwmfEmbeddedType.JPEG;
                            break;
                        case GIF:
                            et = HwmfEmbeddedType.GIF;
                            break;
                        case PNG:
                            et = HwmfEmbeddedType.PNG;
                            break;
                        case TIFF:
                            et = HwmfEmbeddedType.TIFF;
                            break;
                        default:
                            et = HwmfEmbeddedType.BITMAP;
                            break;
                    }
                } else {
                    et = HwmfEmbeddedType.PNG;
                    compressGDIBitmap(img, emb, et);
                }
                break;
            case METAFILE:
                assert(img.getMetafileType() != null);
                switch (img.getMetafileType()) {
                    case Wmf:
                    case WmfPlaceable:
                        et = HwmfEmbeddedType.WMF;
                        break;
                    case Emf:
                    case EmfPlusDual:
                    case EmfPlusOnly:
                        et = HwmfEmbeddedType.EMF;
                        break;
                    default:
                        et = HwmfEmbeddedType.UNKNOWN;
                        break;
                }
                break;
            default:
                et = HwmfEmbeddedType.UNKNOWN;
                break;
        }
        emb.setEmbeddedType(et);

        return emb;
    }

    /**
     * Compress GDIs internal format to something useful
     */
    private void compressGDIBitmap(EmfPlusImage img, HwmfEmbedded emb, HwmfEmbeddedType et) {
        BufferedImage bi = img.readGDIImage(emb.getRawData());
        try {
            UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
            // use HwmfEmbeddedType literal for conversion
            ImageIO.write(bi, et.toString(), bos);
            emb.setData(bos.toByteArray());
        } catch (IOException e) {
            // TODO: throw appropriate exception
            throw new IllegalStateException(e);
        }
    }


    private HwmfEmbedded getEmfPlusImageData() {
        EmfPlusObject epo = (EmfPlusObject)current;
        assert(epo.getObjectType() == EmfPlusObjectType.IMAGE);

        final int objectId = epo.getObjectId();

        final HwmfEmbedded emb = new HwmfEmbedded();

        // totalSize is only set, if there are multiple chunks
        final int totalSize = epo.getTotalObjectSize() == 0
            ? ((EmfPlusImage)epo.getObjectData()).getImageData().length
            : epo.getTotalObjectSize();
        IOUtils.safelyAllocateCheck(totalSize, MAX_RECORD_LENGTH);

        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().setBufferSize(totalSize).get()) {
            boolean hasNext = false;
            do {
                EmfPlusImage img = epo.getObjectData();
                assert(img.getImageDataType() != null);
                assert(!hasNext || img.getImageDataType() == HemfPlusImage.EmfPlusImageDataType.CONTINUED);
                bos.write(img.getImageData());
                current = null;
                hasNext = moveNext() &&
                    (current instanceof EmfPlusObject) &&
                    ((epo = (EmfPlusObject) current).getObjectId() == objectId) &&
                    bos.size() < totalSize-16;
            } while (hasNext);

            emb.setData(bos.toByteArray());
            return emb;
        } catch (IOException ignored) {
            // UnsynchronizedByteArrayOutputStream doesn't throw IOException
            return null;
        }
    }
}
