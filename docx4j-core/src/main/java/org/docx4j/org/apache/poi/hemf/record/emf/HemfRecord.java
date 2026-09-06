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

package org.docx4j.org.apache.poi.hemf.record.emf;


import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecord;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@Internal
public interface HemfRecord extends GenericRecord {

    HemfRecordType getEmfRecordType();

    /**
     * Init record from stream
     *
     * @param leis the little endian input stream
     * @param recordSize the size limit for this record
     * @param recordId the id of the {@link HemfRecordType}
     *
     * @return count of processed bytes
     *
     * @throws IOException when the inputstream is malformed
     */
    long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException;

    /**
     * Draws the record, the default redirects to the parent WMF record drawing
     * @param ctx the drawing context
     */
    default void draw(HemfGraphics ctx) {
        if (this instanceof HwmfRecord) {
            ((HwmfRecord) this).draw(ctx);
        }
    }

    interface RenderBounds {
        HemfGraphics.EmfRenderState getState();
        void setState(HemfGraphics.EmfRenderState state);

        Rectangle2D getWindow();
        Rectangle2D getViewport();
        Rectangle2D getBounds();
    }

    default void calcBounds(RenderBounds holder) {
    }

    /**
     * Sets the header reference, in case the record needs to refer to it
     * @param header the emf header
     */
    default void setHeader(HemfHeader header) {}

    @Override
    default HemfRecordType getGenericRecordType() {
        return getEmfRecordType();
    }
}

interface HemfRecordWithoutProperties extends HemfRecord {
    default Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }

}
