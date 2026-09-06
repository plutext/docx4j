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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.RecordFormatException;

public class HemfRecordIterator implements Iterator<HemfRecord> {

    static final int HEADER_SIZE = 2*LittleEndianConsts.INT_SIZE;

    private final LittleEndianInputStream stream;
    private HemfRecord currentRecord;

    public HemfRecordIterator(LittleEndianInputStream leis) {
        stream = leis;
        //queue the first non-header record
        currentRecord = _next();
    }

    @Override
    public boolean hasNext() {
        return currentRecord != null;
    }

    @Override
    public HemfRecord next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        HemfRecord toReturn = currentRecord;
        currentRecord = (currentRecord instanceof HemfMisc.EmfEof) ? null : _next();
        return toReturn;
    }

    private HemfRecord _next() {
        if (currentRecord != null && HemfRecordType.eof == currentRecord.getEmfRecordType()) {
            return null;
        }

        final int readIndex = stream.getReadIndex();

        final long recordId, recordSize;
        try {
            recordId = stream.readUInt();
            recordSize = stream.readUInt();
        } catch (RuntimeException e) {
            // EOF
            return null;
        }

        HemfRecordType type = HemfRecordType.getById(recordId);
        if (type == null) {
            throw new RecordFormatException("Undefined record of type: "+recordId+" at "+Integer.toHexString(readIndex));
        }
        final HemfRecord record = type.constructor.get();

        try {
            long remBytes = recordSize - HEADER_SIZE;
            long readBytes = record.init(stream, remBytes, recordId);
            if (readBytes > remBytes) {
                throw new RecordFormatException("Record limit exceeded - readBytes: "+readBytes+" / remBytes: "+remBytes);
            }
            stream.skipFully((int) (remBytes - readBytes));
        } catch (RecordFormatException e) {
            throw e;
        } catch (IOException|RuntimeException e) {
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