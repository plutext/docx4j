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

import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class ClipboardData
{
//    private static final POILogger logger = POILogFactory
//            .getLogger( ClipboardData.class );
	private static Logger logger = LoggerFactory.getLogger(ClipboardData.class);

    private int _format;
    private byte[] _value;

    ClipboardData( byte[] data, int offset )
    {
        int size = LittleEndian.getInt( data, offset );

        if ( size < 4 )
        {
            logger.warn( "ClipboardData at offset ",
                    Integer.valueOf( offset ), " size less than 4 bytes "
                            + "(doesn't even have format field!). "
                            + "Setting to format == 0 and hope for the best" );
            _format = 0;
            _value = new byte[0];
            return;
        }

        _format = LittleEndian.getInt( data, offset + LittleEndian.INT_SIZE );
        _value = LittleEndian.getByteArray( data, offset
                + LittleEndian.INT_SIZE * 2, size - LittleEndian.INT_SIZE );
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE * 2 + _value.length;
    }

    byte[] getValue()
    {
        return _value;
    }

    byte[] toByteArray()
    {
        byte[] result = new byte[getSize()];
        LittleEndian.putInt( result, 0 * LittleEndian.INT_SIZE,
                LittleEndian.INT_SIZE + _value.length );
        LittleEndian.putInt( result, 1 * LittleEndian.INT_SIZE, _format );
        System.arraycopy( _value, 0, result, LittleEndian.INT_SIZE
                + LittleEndian.INT_SIZE, _value.length );
        return result;
    }

    int write( OutputStream out ) throws IOException
    {
        LittleEndian.putInt( LittleEndian.INT_SIZE + _value.length, out );
        LittleEndian.putInt( _format, out );
        out.write( _value );
        return 2 * LittleEndian.INT_SIZE + _value.length;
    }
}
