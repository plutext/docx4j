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

import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;

@Internal
class Decimal
{
    static final int SIZE = 16;

    private short field_1_wReserved;
    private byte field_2_scale;
    private byte field_3_sign;
    private int field_4_hi32;
    private long field_5_lo64;

    Decimal( final byte[] data, final int startOffset )
    {
        int offset = startOffset;

        field_1_wReserved = LittleEndian.getShort( data, offset );
        offset += LittleEndian.SHORT_SIZE;

        field_2_scale = data[offset];
        offset += LittleEndian.BYTE_SIZE;

        field_3_sign = data[offset];
        offset += LittleEndian.BYTE_SIZE;

        field_4_hi32 = LittleEndian.getInt( data, offset );
        offset += LittleEndian.INT_SIZE;

        field_5_lo64 = LittleEndian.getLong( data, offset );
        offset += LittleEndian.LONG_SIZE;
    }
}
