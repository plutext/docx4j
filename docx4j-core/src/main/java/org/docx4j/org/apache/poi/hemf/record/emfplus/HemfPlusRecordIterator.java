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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.RecordFormatException;

public class HemfPlusRecordIterator implements Iterator<HemfPlusRecord> {

    private final LittleEndianInputStream leis;
    private final int startIdx;
    private final int limit;
    private HemfPlusRecord currentRecord;

    public HemfPlusRecordIterator(LittleEndianInputStream leis) {
        this(leis, -1);
    }

    public HemfPlusRecordIterator(LittleEndianInputStream leis, int limit) {
        this.leis = leis;
        this.limit = limit;
        startIdx = leis.getReadIndex();
        //queue the first non-header record
        currentRecord = _next();
    }

    @Override
    public boolean hasNext() {
        return currentRecord != null;
    }

    @Override
    public HemfPlusRecord next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        HemfPlusRecord toReturn = currentRecord;
        // add the size for recordId/flags/recordSize/dataSize = 12 bytes
        final boolean isEOF = (limit == -1 || (leis.getReadIndex()-startIdx)+12 > limit);
        currentRecord = isEOF ? null : _next();
        return toReturn;
    }

    private HemfPlusRecord _next() {
        if (currentRecord != null && HemfPlusRecordType.eof == currentRecord.getEmfPlusRecordType()) {
            return null;
        }
        // A 16-bit unsigned integer that identifies this record type
        int recordId = leis.readUShort();
        // A 16-bit unsigned integer that provides information about how the operation is
        // to be performed, and about the structure of the record.
        int flags = leis.readUShort();
        // A 32-bit unsigned integer that specifies the 32-bit-aligned size of the entire
        // record in bytes, including the 12-byte record header and record-specific data.
        int recordSize = (int)leis.readUInt();
        // A 32-bit unsigned integer that specifies the 32-bit-aligned number of bytes of data
        // in the record-specific data that follows. This number does not include the size of
        // the invariant part of this record.
        int dataSize = (int)leis.readUInt();

        HemfPlusRecordType type = HemfPlusRecordType.getById(recordId);
        if (type == null) {
            throw new RecordFormatException("Undefined record of type:"+recordId);
        }
        final HemfPlusRecord record = type.constructor.get();

        try {
            long readBytes = record.init(leis, dataSize, recordId, flags);
            assert (readBytes <= recordSize-12);
            leis.skipFully((int)(recordSize-12-readBytes));
        } catch (IOException e) {
            throw new RecordFormatException(e);
        }

        return record;
    }

    /**
     * This method is not yet supported.
     *
     * @throws UnsupportedOperationException this method is not yet supported
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }

}
