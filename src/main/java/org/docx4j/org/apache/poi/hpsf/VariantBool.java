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
//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class VariantBool
{
//    private final static POILogger logger = POILogFactory
//            .getLogger( VariantBool.class );
	private static Logger logger = LoggerFactory.getLogger(VariantBool.class);

    static final int SIZE = 2;

    private boolean _value;

    VariantBool( byte[] data, int offset )
    {
        short value = LittleEndian.getShort( data, offset );
        if ( value == 0x0000 )
        {
            _value = false;
            return;
        }

        if ( value == 0xffff )
        {
            _value = true;
            return;
        }

        logger.warn(  "VARIANT_BOOL value '",
                Short.valueOf( value ), "' is incorrect" );
        _value = value != 0;
    }

    boolean getValue()
    {
        return _value;
    }

    void setValue( boolean value )
    {
        this._value = value;
    }
}
