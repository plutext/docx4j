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

import java.util.ArrayList;
import java.util.List;

import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;

/**
 * Holder for vector-type properties
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
class Vector
{
    private final short _type;

    private TypedPropertyValue[] _values;

    public Vector( short type ) {
        this._type = type;
    }

    public void read( LittleEndianByteArrayInputStream lei ) {
        final long longLength = lei.readUInt();

        if ( longLength > Integer.MAX_VALUE ) {
            throw new UnsupportedOperationException( "Vector is too long -- " + longLength );
        }
        final int length = (int) longLength;

        //BUG-61295 -- avoid OOM on corrupt file.  Build list instead
        //of allocating array of length "length".
        //If the length is corrupted and crazily big but < Integer.MAX_VALUE,
        //this will trigger a RuntimeException "Buffer overrun" in lei.checkPosition
        List<TypedPropertyValue> values = new ArrayList<>();
        int paddedType = (_type == Variant.VT_VARIANT) ? 0 : _type;
        for ( int i = 0; i < length; i++ ) {
            TypedPropertyValue value = new TypedPropertyValue(paddedType, null);
            if (paddedType == 0) {
                value.read(lei);
            } else {
                value.readValue(lei);
            }
            values.add(value);
        }
        _values = values.toArray(new TypedPropertyValue[0]);
    }

    public TypedPropertyValue[] getValues(){
        return _values;
    }
}
