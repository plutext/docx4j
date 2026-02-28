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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.docx4j.org.apache.poi.util.CodePageUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class CodePageString
{
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;
    
    private static Logger logger = LoggerFactory.getLogger(CodePageString.class);

    private byte[] _value;

    /**
     * @param length the max record length allowed for CodePageString
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for CodePageString
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public void read( LittleEndianByteArrayInputStream lei ) {
        int offset = lei.getReadIndex();
        int size = lei.readInt();
        _value = IOUtils.safelyAllocate(size, MAX_RECORD_LENGTH);
        if (size == 0) {
            return;
        }

        // If Size is zero, this field MUST be zero bytes in length. If Size is
        // nonzero and the CodePage property set's CodePage property has the value CP_WINUNICODE
        // (0x04B0), then the value MUST be a null-terminated array of 16-bit Unicode characters,
        // followed by zero padding to a multiple of 4 bytes. If Size is nonzero and the property set's
        // CodePage property has any other value, it MUST be a null-terminated array of 8-bit characters
        // from the code page identified by the CodePage property, followed by zero padding to a
        // multiple of 4 bytes. The string represented by this field MAY contain embedded or additional
        // trailing null characters and an OLEPS implementation MUST be able to handle such strings.

        lei.readFully(_value);
        if (_value[size - 1] != 0 ) {
            // TODO Some files, such as TestVisioWithCodepage.vsd, are currently
            // triggering this for values that don't look like codepages
            // See Bug #52258 for details
            logger.warn("CodePageString started at offset #{} is not NULL-terminated", offset);
        }

        TypedPropertyValue.skipPadding(lei);
    }

    String getJavaValue( int codepage ) throws UnsupportedEncodingException
    {
        String result;
        if ( codepage == -1 )
            result = new String( _value );
        else
            result = CodePageUtil.getStringFromCodePage(_value, codepage);
        final int terminator = result.indexOf( '\0' );
        if ( terminator == -1 )
        {
            logger.warn(
                    "String terminator (\\0) for CodePageString property value not found."
                            + "Continue without trimming and hope for the best." );
            return result;
        }
        if ( terminator != result.length() - 1 )
        {
            logger.warn(
                    "String terminator (\\0) for CodePageString property value occured before the end of string. "
                            + "Trimming and hope for the best." );
        }
        return result.substring( 0, terminator );
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE + _value.length;
    }

    void setJavaValue( String string, int codepage )
            throws UnsupportedEncodingException
    {
        String stringNT = string + "\0";
        if ( codepage == -1 )
            _value = stringNT.getBytes();
        else
            _value = CodePageUtil.getBytesInCodePage(stringNT, codepage);
    }

    int write( OutputStream out ) throws IOException
    {
        LittleEndian.putInt( _value.length, out );
        out.write( _value );
        return LittleEndian.INT_SIZE + _value.length;
    }
}
