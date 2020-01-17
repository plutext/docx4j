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

    Vector( byte[] data, int startOffset, short type )
    {
        this._type = type;
        read( data, startOffset );
    }

    Vector( short type )
    {
        this._type = type;
    }

    int read( byte[] data, int startOffset )
    {
        int offset = startOffset;

        final long longLength = LittleEndian.getUInt( data, offset );
        offset += LittleEndian.INT_SIZE;

        if ( longLength > Integer.MAX_VALUE )
            throw new UnsupportedOperationException( "Vector is too long -- "
                    + longLength );
        final int length = (int) longLength;

        _values = new TypedPropertyValue[length];

        if ( _type == Variant.VT_VARIANT )
        {
            for ( int i = 0; i < length; i++ )
            {
                TypedPropertyValue value = new TypedPropertyValue();
                offset += value.read( data, offset );
                _values[i] = value;
            }
        }
        else
        {
            for ( int i = 0; i < length; i++ )
            {
                TypedPropertyValue value = new TypedPropertyValue( _type, null );
                // be aware: not padded here
                offset += value.readValue( data, offset );
                _values[i] = value;
            }
        }
        return offset - startOffset;
    }

    TypedPropertyValue[] getValues(){
        return _values;
    }
}
