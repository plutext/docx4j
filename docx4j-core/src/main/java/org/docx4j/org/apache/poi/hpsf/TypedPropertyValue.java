/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.docx4j.org.apache.poi.hpsf;

//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.docx4j.TraversalUtil;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class TypedPropertyValue
{
//    private static final POILogger logger = POILogFactory
//            .getLogger( TypedPropertyValue.class );

	private static Logger logger = LoggerFactory.getLogger(TraversalUtil.class);

	
    private int _type;

    private Object _value;

    TypedPropertyValue()
    {
    }

    TypedPropertyValue( byte[] data, int startOffset )
    {
        read( data, startOffset );
    }

    TypedPropertyValue( int type, Object value )
    {
        _type = type;
        _value = value;
    }

    Object getValue()
    {
        return _value;
    }

    int read( byte[] data, int startOffset )
    {
        int offset = startOffset;

        _type = LittleEndian.getShort( data, offset );
        offset += LittleEndian.SHORT_SIZE;

        short padding = LittleEndian.getShort( data, offset );
        offset += LittleEndian.SHORT_SIZE;
        if ( padding != 0 )
        {
            logger.warn(  "TypedPropertyValue padding at offset "
                    + offset + " MUST be 0, but it's value is " + padding );
        }

        offset += readValue( data, offset );

        return offset - startOffset;
    }

    int readValue( byte[] data, int offset )
    {
        switch ( _type )
        {
        case Variant.VT_EMPTY:
        case Variant.VT_NULL:
            _value = null;
            return 0;

        case Variant.VT_I2:
            _value = Short.valueOf( LittleEndian.getShort( data, offset ) );
            return 4;

        case Variant.VT_I4:
            _value = Integer.valueOf( LittleEndian.getInt( data, offset ) );
            return 4;

        case Variant.VT_R4:
            _value = Short.valueOf( LittleEndian.getShort( data, offset ) );
            return 4;

        case Variant.VT_R8:
            _value = Double.valueOf( LittleEndian.getDouble( data, offset ) );
            return 8;

        case Variant.VT_CY:
            _value = new Currency( data, offset );
            return Currency.SIZE;

        case Variant.VT_DATE:
            _value = new Date( data, offset );
            return Date.SIZE;

        case Variant.VT_BSTR:
            _value = new CodePageString( data, offset );
            return ( (CodePageString) _value ).getSize();

        case Variant.VT_ERROR:
            _value = Long.valueOf( LittleEndian.getUInt( data, offset ) );
            return 4;

        case Variant.VT_BOOL:
            _value = new VariantBool( data, offset );
            return VariantBool.SIZE;

        case Variant.VT_DECIMAL:
            _value = new Decimal( data, offset );
            return Decimal.SIZE;

        case Variant.VT_I1:
            _value = Byte.valueOf( data[offset] );
            return 1;

        case Variant.VT_UI1:
            _value = Short.valueOf( LittleEndian.getUByte( data, offset ) );
            return 2;

        case Variant.VT_UI2:
            _value = Integer.valueOf( LittleEndian.getUShort( data, offset ) );
            return 4;

        case Variant.VT_UI4:
            _value = Long.valueOf( LittleEndian.getUInt( data, offset ) );
            return 4;

        case Variant.VT_I8:
            _value = Long.valueOf( LittleEndian.getLong( data, offset ) );
            return 8;

        case Variant.VT_UI8:
            _value = LittleEndian.getByteArray( data, offset, 8 );
            return 8;

        case Variant.VT_INT:
            _value = Integer.valueOf( LittleEndian.getInt( data, offset ) );
            return 4;

        case Variant.VT_UINT:
            _value = Long.valueOf( LittleEndian.getUInt( data, offset ) );
            return 4;

        case Variant.VT_LPSTR:
            _value = new CodePageString( data, offset );
            return ( (CodePageString) _value ).getSize();

        case Variant.VT_LPWSTR:
            _value = new UnicodeString( data, offset );
            return ( (UnicodeString) _value ).getSize();

        case Variant.VT_FILETIME:
            _value = new Filetime( data, offset );
            return Filetime.SIZE;

        case Variant.VT_BLOB:
            _value = new Blob( data, offset );
            return ( (Blob) _value ).getSize();

        case Variant.VT_STREAM:
        case Variant.VT_STORAGE:
        case Variant.VT_STREAMED_OBJECT:
        case Variant.VT_STORED_OBJECT:
            _value = new IndirectPropertyName( data, offset );
            return ( (IndirectPropertyName) _value ).getSize();

        case Variant.VT_BLOB_OBJECT:
            _value = new Blob( data, offset );
            return ( (Blob) _value ).getSize();

        case Variant.VT_CF:
            _value = new ClipboardData( data, offset );
            return ( (ClipboardData) _value ).getSize();

        case Variant.VT_CLSID:
            _value = new GUID( data, offset );
            return GUID.SIZE;

        case Variant.VT_VERSIONED_STREAM:
            _value = new VersionedStream( data, offset );
            return ( (VersionedStream) _value ).getSize();

        case Variant.VT_VECTOR | Variant.VT_I2:
        case Variant.VT_VECTOR | Variant.VT_I4:
        case Variant.VT_VECTOR | Variant.VT_R4:
        case Variant.VT_VECTOR | Variant.VT_R8:
        case Variant.VT_VECTOR | Variant.VT_CY:
        case Variant.VT_VECTOR | Variant.VT_DATE:
        case Variant.VT_VECTOR | Variant.VT_BSTR:
        case Variant.VT_VECTOR | Variant.VT_ERROR:
        case Variant.VT_VECTOR | Variant.VT_BOOL:
        case Variant.VT_VECTOR | Variant.VT_VARIANT:
        case Variant.VT_VECTOR | Variant.VT_I1:
        case Variant.VT_VECTOR | Variant.VT_UI1:
        case Variant.VT_VECTOR | Variant.VT_UI2:
        case Variant.VT_VECTOR | Variant.VT_UI4:
        case Variant.VT_VECTOR | Variant.VT_I8:
        case Variant.VT_VECTOR | Variant.VT_UI8:
        case Variant.VT_VECTOR | Variant.VT_LPSTR:
        case Variant.VT_VECTOR | Variant.VT_LPWSTR:
        case Variant.VT_VECTOR | Variant.VT_FILETIME:
        case Variant.VT_VECTOR | Variant.VT_CF:
        case Variant.VT_VECTOR | Variant.VT_CLSID:
            _value = new Vector( (short) ( _type & 0x0FFF ) );
            return ( (Vector) _value ).read( data, offset );

        case Variant.VT_ARRAY | Variant.VT_I2:
        case Variant.VT_ARRAY | Variant.VT_I4:
        case Variant.VT_ARRAY | Variant.VT_R4:
        case Variant.VT_ARRAY | Variant.VT_R8:
        case Variant.VT_ARRAY | Variant.VT_CY:
        case Variant.VT_ARRAY | Variant.VT_DATE:
        case Variant.VT_ARRAY | Variant.VT_BSTR:
        case Variant.VT_ARRAY | Variant.VT_ERROR:
        case Variant.VT_ARRAY | Variant.VT_BOOL:
        case Variant.VT_ARRAY | Variant.VT_VARIANT:
        case Variant.VT_ARRAY | Variant.VT_DECIMAL:
        case Variant.VT_ARRAY | Variant.VT_I1:
        case Variant.VT_ARRAY | Variant.VT_UI1:
        case Variant.VT_ARRAY | Variant.VT_UI2:
        case Variant.VT_ARRAY | Variant.VT_UI4:
        case Variant.VT_ARRAY | Variant.VT_INT:
        case Variant.VT_ARRAY | Variant.VT_UINT:
            _value = new Array();
            return ( (Array) _value ).read( data, offset );

        default:
            throw new UnsupportedOperationException(
                    "Unknown (possibly, incorrect) TypedPropertyValue type: "
                            + _type );
        }
    }

    int readValuePadded( byte[] data, int offset )
    {
        int nonPadded = readValue( data, offset );
        return ( nonPadded & 0x03 ) == 0 ? nonPadded : nonPadded
                + ( 4 - ( nonPadded & 0x03 ) );
    }
}
