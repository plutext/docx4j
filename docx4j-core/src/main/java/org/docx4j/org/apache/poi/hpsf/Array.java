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
class Array
{
    static class ArrayDimension
    {
        static final int SIZE = 8;

        private int _indexOffset;
        private long _size;

        ArrayDimension( byte[] data, int offset )
        {
            _size = LittleEndian.getUInt( data, offset );
            _indexOffset = LittleEndian.getInt( data, offset
                    + LittleEndian.INT_SIZE );
        }
    }

    static class ArrayHeader
    {
        private ArrayDimension[] _dimensions;
        private int _type;

        ArrayHeader( byte[] data, int startOffset )
        {
            int offset = startOffset;

            _type = LittleEndian.getInt( data, offset );
            offset += LittleEndian.INT_SIZE;

            long numDimensionsUnsigned = LittleEndian.getUInt( data, offset );
            offset += LittleEndian.INT_SIZE;

            if ( !( 1 <= numDimensionsUnsigned && numDimensionsUnsigned <= 31 ) )
                throw new IllegalPropertySetDataException(
                        "Array dimension number " + numDimensionsUnsigned
                                + " is not in [1; 31] range" );
            int numDimensions = (int) numDimensionsUnsigned;

            _dimensions = new ArrayDimension[numDimensions];
            for ( int i = 0; i < numDimensions; i++ )
            {
                _dimensions[i] = new ArrayDimension( data, offset );
                offset += ArrayDimension.SIZE;
            }
        }

        long getNumberOfScalarValues()
        {
            long result = 1;
            for ( ArrayDimension dimension : _dimensions )
                result *= dimension._size;
            return result;
        }

        int getSize()
        {
            return LittleEndian.INT_SIZE * 2 + _dimensions.length
                    * ArrayDimension.SIZE;
        }

        int getType()
        {
            return _type;
        }
    }

    private ArrayHeader _header;
    private TypedPropertyValue[] _values;

    Array()
    {
    }

    Array( final byte[] data, final int offset )
    {
        read( data, offset );
    }

    int read( final byte[] data, final int startOffset )
    {
        int offset = startOffset;

        _header = new ArrayHeader( data, offset );
        offset += _header.getSize();

        long numberOfScalarsLong = _header.getNumberOfScalarValues();
        if ( numberOfScalarsLong > Integer.MAX_VALUE )
            throw new UnsupportedOperationException(
                    "Sorry, but POI can't store array of properties with size of "
                            + numberOfScalarsLong + " in memory" );
        int numberOfScalars = (int) numberOfScalarsLong;

        _values = new TypedPropertyValue[numberOfScalars];
        final int type = _header._type;
        if ( type == Variant.VT_VARIANT )
        {
            for ( int i = 0; i < numberOfScalars; i++ )
            {
                TypedPropertyValue typedPropertyValue = new TypedPropertyValue();
                offset += typedPropertyValue.read( data, offset );
            }
        }
        else
        {
            for ( int i = 0; i < numberOfScalars; i++ )
            {
                TypedPropertyValue typedPropertyValue = new TypedPropertyValue(
                        type, null );
                offset += typedPropertyValue.readValuePadded( data, offset );
            }
        }

        return offset - startOffset;
    }
}
