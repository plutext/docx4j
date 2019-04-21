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

package org.docx4j.org.apache.poi.util;


public final class PngUtils {

    /**
     * File header for PNG format.
     */
    private static final byte[] PNG_FILE_HEADER =
        new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

    private PngUtils() {
        // no instances of this class
    }

    /**
     * Checks if the offset matches the PNG header.
     *
     * @param data the data to check.
     * @param offset the offset to check at.
     * @return {@code true} if the offset matches.
     */
    public static boolean matchesPngHeader(byte[] data, int offset) {
        if (data == null || data.length - offset < PNG_FILE_HEADER.length) {
            return false;
        }

        for (int i = 0; i < PNG_FILE_HEADER.length; i++) {
            if (PNG_FILE_HEADER[i] != data[i + offset]) {
                return false;
            }
        }

        return true;
    }
}
