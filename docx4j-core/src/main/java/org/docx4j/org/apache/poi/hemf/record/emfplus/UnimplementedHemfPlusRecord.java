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
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@Internal
public class UnimplementedHemfPlusRecord implements HemfPlusRecord {

    private HemfPlusRecordType recordType;
    private int flags;

    @Override
    public HemfPlusRecordType getEmfPlusRecordType() {
        return recordType;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
        recordType = HemfPlusRecordType.getById(recordId);
        this.flags = flags;
        long skipped = IOUtils.skipFully(leis, dataSize);
        if (skipped < dataSize) {
            throw new IOException("End of stream reached before record read");
        }
        return skipped;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "flags", this::getFlags
        );
    }
}