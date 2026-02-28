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
package org.docx4j.org.apache.poi.hpsf;

import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

@Internal
public class Blob {

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 10_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private byte[] _value;

    /**
     * @param length the max record length allowed for Blob
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for Blob
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public void read( LittleEndianInput lei ) {
        int size = lei.readInt();
        _value = IOUtils.safelyAllocate(size, MAX_RECORD_LENGTH);
        if ( size > 0 ) {
            lei.readFully(_value);
        }
    }
}