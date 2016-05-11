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

//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class UnicodeString {
//    private final static POILogger logger = 
//            POILogFactory.getLogger( UnicodeString.class );
	private static Logger logger = LoggerFactory.getLogger(UnicodeString.class);
	

    private byte[] _value;

    UnicodeString(byte[] data, int offset) {
        int length = LittleEndian.getInt( data, offset );
        int dataOffset = offset + LittleEndian.INT_SIZE;
        
        if (! validLength(length, data, dataOffset)) {
            // If the length looks wrong, this might be because the offset is sometimes expected 
            // to be on a 4 byte boundary. Try checking with that if so, rather than blowing up with
            // and  ArrayIndexOutOfBoundsException below
            boolean valid = false;
            int past4byte = offset % 4;
            if (past4byte != 0) {
                offset = offset + past4byte;
                length = LittleEndian.getInt( data, offset );
                dataOffset = offset + LittleEndian.INT_SIZE;
                
                valid = validLength(length, data, dataOffset);
            }
            
            if (!valid) {
                throw new IllegalPropertySetDataException(
                        "UnicodeString started at offset #" + offset +
                        " is not NULL-terminated" );
            }
        }

        if ( length == 0 )
        {
            _value = new byte[0];
            return;
        }

        _value = LittleEndian.getByteArray( data, dataOffset, length * 2 );
    }
    
    /**
     * Checks to see if the specified length seems valid,
     *  given the amount of data available still to read,
     *  and the requirement that the string be NULL-terminated
     */
    boolean validLength(int length, byte[] data, int offset) {
        if (length == 0) {
            return true;
        }

        int endOffset = offset + (length * 2);
        if (endOffset <= data.length) {
            // Data Length is OK, ensure it's null terminated too
            if (data[endOffset-1] == 0 && data[endOffset-2] == 0) {
                // Length looks plausible
                return true;
            }
        }

        // Something's up/invalid with that length for the given data+offset
        return false;
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE + _value.length;
    }

    byte[] getValue()
    {
        return _value;
    }

    String toJavaString()
    {
        if ( _value.length == 0 )
            return null;

        String result = StringUtil.getFromUnicodeLE( _value, 0,
                _value.length >> 1 );

        final int terminator = result.indexOf( '\0' );
        if ( terminator == -1 )
        {
            logger.warn(
                    "String terminator (\\0) for UnicodeString property value not found."
                            + "Continue without trimming and hope for the best." );
            return result;
        }
        if ( terminator != result.length() - 1 )
        {
            logger.warn(
                    "String terminator (\\0) for UnicodeString property value occured before the end of string. "
                            + "Trimming and hope for the best." );
        }
        return result.substring( 0, terminator );
    }
}
