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

import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@Internal
public class UnimplementedHemfRecord implements HemfRecordWithoutProperties {

    private HemfRecordType recordType;

    @Override
    public HemfRecordType getEmfRecordType() {
        return recordType;
    }

    @Override
    public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
        recordType = HemfRecordType.getById(recordId);
        long skipped = IOUtils.skipFully(leis, recordSize);
        if (skipped < recordSize) {
            throw new IOException("End of stream reached before record read");
        }
        return skipped;
    }
}
