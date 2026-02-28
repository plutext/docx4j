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

import java.math.BigInteger;

import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Internal
public class TypedPropertyValue {
	private static Logger logger = LoggerFactory.getLogger(TypedPropertyValue.class);

    private int _type;
    private Object _value;

    public TypedPropertyValue( int type, Object value ) {
        _type = type;
        _value = value;
    }

    public Object getValue() {
        return _value;
    }

    public void read( LittleEndianByteArrayInputStream lei ) {
        _type = lei.readShort();
        short padding = lei.readShort();
        if ( padding != 0 ) {
            logger.warn("TypedPropertyValue padding at offset " + lei.getReadIndex() + " MUST be 0, but it's value is " +  padding);
        }
        readValue( lei );
    }

    public void readValue( LittleEndianByteArrayInputStream lei ) {
        switch ( _type ) {
        case Variant.VT_EMPTY:
        case Variant.VT_NULL:
            _value = null;
            break;

        case Variant.VT_I1:
            _value = lei.readByte();
            break;

        case Variant.VT_UI1:
            _value =  lei.readUByte();
            break;

        case Variant.VT_I2:
            _value = lei.readShort();
            break;

        case Variant.VT_UI2:
            _value = lei.readUShort();
            break;
            
        case Variant.VT_INT:
        case Variant.VT_I4:
            _value = lei.readInt();
            break;

        case Variant.VT_UINT:
        case Variant.VT_UI4:
        case Variant.VT_ERROR:
            _value = lei.readUInt();
            break;

        case Variant.VT_I8:
            _value = lei.readLong();
            break;

        case Variant.VT_UI8: {
            byte[] biBytesLE = new byte[LittleEndianConsts.LONG_SIZE];
            lei.readFully(biBytesLE);

            // first byte needs to be 0 for unsigned BigInteger
            byte[] biBytesBE = new byte[9];
            int i=biBytesLE.length;
            for (byte b : biBytesLE) {
                if (i<=8) {
                    biBytesBE[i] = b;
                }
                i--;
            }
            _value = new BigInteger(biBytesBE);
            break;
        }

            
        case Variant.VT_R4:
            _value = Float.intBitsToFloat(lei.readInt());
            break;
            
        case Variant.VT_R8:
            _value = lei.readDouble();
            break;

        case Variant.VT_CY:
            Currency cur = new Currency();
            cur.read(lei);
            _value = cur;
            break;
            

        case Variant.VT_DATE:
            Date date = new Date();
            date.read(lei);
            _value = date;
            break;

        case Variant.VT_BSTR:
        case Variant.VT_LPSTR:
            CodePageString cps = new CodePageString();
            cps.read(lei);
            _value = cps;
            break;

        case Variant.VT_BOOL:
            VariantBool vb = new VariantBool();
            vb.read(lei);
            _value = vb;
            break;

        case Variant.VT_DECIMAL:
            Decimal dec = new Decimal();
            dec.read(lei);
            _value = dec;
            break;

        case Variant.VT_LPWSTR:
            UnicodeString us = new UnicodeString();
            us.read(lei);
            _value = us;
            break;

        case Variant.VT_FILETIME:
            Filetime ft = new Filetime();
            ft.read(lei);
            _value = ft;
            break;

        case Variant.VT_BLOB:
        case Variant.VT_BLOB_OBJECT:
            Blob blob = new Blob();
            blob.read(lei);
            _value = blob;
            break;

        case Variant.VT_STREAM:
        case Variant.VT_STORAGE:
        case Variant.VT_STREAMED_OBJECT:
        case Variant.VT_STORED_OBJECT:
            IndirectPropertyName ipn = new IndirectPropertyName();
            ipn.read(lei);
            _value = ipn;
            break;

        case Variant.VT_CF:
            ClipboardData cd = new ClipboardData();
            cd.read(lei);
            _value = cd;
            break;

        case Variant.VT_CLSID:
            GUID guid = new GUID();
            guid.read(lei);
            _value = lei;
            break;

        case Variant.VT_VERSIONED_STREAM:
            VersionedStream vs = new VersionedStream();
            vs.read(lei);
            _value = vs;
            break;

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
            Vector vec = new Vector( (short) ( _type & 0x0FFF ) );
            vec.read(lei);
            _value = vec;
            break;

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
            Array arr = new Array();
            arr.read(lei);
            _value = arr;
            break;

        default:
            String msg = "Unknown (possibly, incorrect) TypedPropertyValue type: " + _type;
            throw new UnsupportedOperationException(msg);
        }
    }

    static void skipPadding( LittleEndianByteArrayInputStream lei ) {
        final int offset = lei.getReadIndex();
        int skipBytes = (4 - (offset & 3)) & 3;
        for (int i=0; i<skipBytes; i++) {
            lei.mark(1);
            int b = lei.read();
            if (b != 0) {
                lei.reset();
                break;
            }
        }
    }
}
