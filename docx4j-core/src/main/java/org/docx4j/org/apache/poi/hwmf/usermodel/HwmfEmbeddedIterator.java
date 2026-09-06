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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.hwmf.record.HwmfEscape;
import org.docx4j.org.apache.poi.hwmf.record.HwmfEscape.EscapeFunction;
import org.docx4j.org.apache.poi.hwmf.record.HwmfEscape.WmfEscapeEMF;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill.HwmfImageRecord;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecord;

public class HwmfEmbeddedIterator implements Iterator<HwmfEmbedded> {

    private final Deque<Iterator<?>> iterStack = new ArrayDeque<>();
    private Object current;

    public HwmfEmbeddedIterator(HwmfPicture wmf) {
        this(wmf.getRecords().iterator());
    }

    public HwmfEmbeddedIterator(Iterator<HwmfRecord> recordIterator) {
        iterStack.add(recordIterator);
    }

    @Override
    public boolean hasNext() {
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
                if (obj instanceof HwmfImageRecord) {
                    current = obj;
                    return true;
                }
                if (obj instanceof HwmfEscape && ((HwmfEscape)obj).getEscapeFunction() == EscapeFunction.META_ESCAPE_ENHANCED_METAFILE) {
                    WmfEscapeEMF emfData = ((HwmfEscape)obj).getEscapeData();
                    if (emfData.isValid()) {
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
        if ((emb = checkHwmfImageRecord()) != null) {
            return emb;
        }
        if ((emb = checkHwmfEscapeRecord()) != null) {
            return emb;
        }

        throw new NoSuchElementException("no further embedded emf records found.");
    }

    private HwmfEmbedded checkHwmfImageRecord() {
        if (!(current instanceof HwmfImageRecord)) {
            return null;
        }

        HwmfImageRecord hir = (HwmfImageRecord)current;
        current = null;

        HwmfEmbedded emb = new HwmfEmbedded();
        emb.setEmbeddedType(HwmfEmbeddedType.BMP);
        emb.setData(hir.getBMPData());

        return emb;
    }


    private HwmfEmbedded checkHwmfEscapeRecord() {
        if (!(current instanceof HwmfEscape)) {
            return null;
        }

        final HwmfEmbedded emb = new HwmfEmbedded();
        emb.setEmbeddedType(HwmfEmbeddedType.EMF);

        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            WmfEscapeEMF img;
            do {
                final HwmfEscape esc = (HwmfEscape)current;
                assert(esc.getEscapeFunction() == EscapeFunction.META_ESCAPE_ENHANCED_METAFILE);

                img = esc.getEscapeData();
                assert(img.isValid());

                bos.write(img.getEmfData());
                current = null;
            } while (img.getRemainingBytes() > 0 && hasNext() && (current instanceof HwmfEscape));

            emb.setData(bos.toByteArray());
            return emb;

        } catch (IOException ignored) {
            // UnsynchronizedByteArrayOutputStream doesn't throw IOException
            return null;
        }
    }
}
