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
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class ClipboardData {
	//arbitrarily selected; may need to increase
	private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
	private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

	private static Logger logger = LoggerFactory.getLogger(ClipboardData.class);

    private int _format;
    private byte[] _value;

    /**
     * @param length the max record length allowed for ClipboardData
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for ClipboardData
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public void read( LittleEndianByteArrayInputStream lei ) {
        int offset = lei.getReadIndex();
        long size = lei.readInt();

        if ( size < 4 ) {
            logger.warn("ClipboardData at offset {} size less than 4 bytes (doesn't even have format " +
                    "field!). Setting to format == 0 and hope for the best", offset);
            _format = 0;
            _value = new byte[0];
            return;
        }

        _format = lei.readInt();
        _value = IOUtils.safelyAllocate(size - LittleEndianConsts.INT_SIZE, MAX_RECORD_LENGTH);
        lei.readFully(_value);
    }

    byte[] getValue()
    {
        return _value;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[LittleEndianConsts.INT_SIZE*2+_value.length];
        LittleEndian.putInt(result, 0, LittleEndianConsts.INT_SIZE + _value.length);
        LittleEndian.putInt(result, 4, _format);
        System.arraycopy(_value, 0, result, 8, _value.length);
        return result;
    }

    public void setValue( byte[] value ) {
        _value = value.clone();
    }
}