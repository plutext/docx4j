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

@Internal
public class Array {
    private static final int DEFAULT_MAX_NUMBER_OF_ARRAY_SCALARS = 100_000;
    private static int MAX_NUMBER_OF_ARRAY_SCALARS = DEFAULT_MAX_NUMBER_OF_ARRAY_SCALARS;

    public static int getMaxNumberOfArrayScalars() {
        return MAX_NUMBER_OF_ARRAY_SCALARS;
    }

    public static void setMaxNumberOfArrayScalars(final int maxNumberOfArrayScalars) {
        MAX_NUMBER_OF_ARRAY_SCALARS = maxNumberOfArrayScalars;
    }

    static class ArrayDimension {
        private long _size;
        @SuppressWarnings("unused")
        private int _indexOffset;

        void read( LittleEndianByteArrayInputStream lei ) {
            _size = lei.readUInt();
            _indexOffset = lei.readInt();
        }
    }

    static class ArrayHeader {
        private ArrayDimension[] _dimensions;
        private int _type;

        void read( LittleEndianByteArrayInputStream lei ) {
            _type = lei.readInt();

            long numDimensionsUnsigned = lei.readUInt();

            if ( !( 1 <= numDimensionsUnsigned && numDimensionsUnsigned <= 31 ) ) {
                String msg = "Array dimension number "+numDimensionsUnsigned+" is not in [1; 31] range";
                throw new IllegalPropertySetDataException(msg);
            }

            int numDimensions = (int) numDimensionsUnsigned;

            _dimensions = new ArrayDimension[numDimensions];
            for ( int i = 0; i < numDimensions; i++ ) {
                ArrayDimension ad = new ArrayDimension();
                ad.read(lei);
                _dimensions[i] = ad;
            }
        }

        long getNumberOfScalarValues() {
            long result = 1;
            for ( ArrayDimension dimension : _dimensions ) {
                result *= dimension._size;
            }
            return result;
        }

        int getType() {
            return _type;
        }
    }

    private final ArrayHeader _header = new ArrayHeader();
    private TypedPropertyValue[] _values;

    public void read( LittleEndianByteArrayInputStream lei ) {
        _header.read(lei);

        long numberOfScalarsLong = _header.getNumberOfScalarValues();
        if ( numberOfScalarsLong > Integer.MAX_VALUE ) {
            String msg =
                "Sorry, but POI can't store array of properties with size of " +
                numberOfScalarsLong + " in memory";
            throw new UnsupportedOperationException(msg);
        }
        int numberOfScalars = (int) numberOfScalarsLong;

        IOUtils.safelyAllocateCheck(numberOfScalars, getMaxNumberOfArrayScalars());

        _values = new TypedPropertyValue[numberOfScalars];
        int paddedType = (_header._type == Variant.VT_VARIANT) ? 0 : _header._type;
        for ( int i = 0; i < numberOfScalars; i++ ) {
            TypedPropertyValue typedPropertyValue = new TypedPropertyValue(paddedType, null);
            typedPropertyValue.read(lei);
            _values[i] = typedPropertyValue;
            if (paddedType != 0) {
                TypedPropertyValue.skipPadding(lei);
            }
        }
    }

    public TypedPropertyValue[] getValues(){
        return _values;
    }
}