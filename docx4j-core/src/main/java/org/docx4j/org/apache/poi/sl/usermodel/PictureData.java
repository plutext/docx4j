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

package org.docx4j.org.apache.poi.sl.usermodel;

import java.awt.Dimension;
import java.io.IOException;

public interface PictureData {
    
    enum PictureType {
        /** Extended windows meta file */
        EMF(2,2,"image/x-emf",".emf"),
        /** Windows Meta File */
        WMF(3,3,"image/x-wmf",".wmf"),
        /** Mac PICT format */
        PICT(4,4,"image/x-pict",".pict"),
        /** JPEG format */
        JPEG(5,5,"image/jpeg",".jpg"),
        /** PNG format */
        PNG(6,6,"image/png",".png"),
        /** Device independent bitmap */
        DIB(7,7,"image/dib",".dib"),
        /** GIF image format */
        GIF(-1,8,"image/gif",".gif"),
        /** Tag Image File (.tiff) */
        TIFF(17,9,"image/tiff",".tif"),
        /** Encapsulated Postscript (.eps) */
        EPS(-1,10,"image/x-eps",".eps"),
        /** Windows Bitmap (.bmp) */
        BMP(-1,11,"image/x-ms-bmp",".bmp"),
        /** WordPerfect graphics (.wpg) */
        WPG(-1,12,"image/x-wpg",".wpg"),
        /** Microsoft Windows Media Photo image (.wdp) */
        WDP(-1,13,"image/vnd.ms-photo",".wdp"),
        /** Scalable vector graphics (.svg) - supported by Office 2016 and higher */
        SVG(-1, -1, "image/svg+xml", ".svg"),
        /** Unknown picture type - specific to escher bse record */
        UNKNOWN(1, -1, "", ".dat"),
        /** Picture type error - specific to escher bse record */
        ERROR(0, -1, "", ".dat"),
        /** JPEG in the YCCK or CMYK color space. */
        CMYKJPEG( 18, -1, "image/jpeg", ".jpg"),
        /** client defined blip type - native-id 32 to 255 */
        CLIENT( 32, -1, "", ".dat")
        ;
        
        public final int nativeId, ooxmlId;
        public final String contentType, extension;

        PictureType(int nativeId, int ooxmlId,String contentType,String extension) {
            this.nativeId = nativeId;
            this.ooxmlId = ooxmlId;
            this.contentType = contentType;
            this.extension = extension;
        }
        
        public static PictureType forNativeID(int nativeId) {
            for (PictureType ans : values()) {
                if (ans.nativeId == nativeId) return ans;
            }
            return nativeId >= CLIENT.nativeId ? CLIENT : UNKNOWN;
        }

        public static PictureType forOoxmlID(int ooxmlId) {
            for (PictureType ans : values()) {
                if (ans.ooxmlId == ooxmlId) return ans;
            }
            return null;
        }
    }
    
    
    /**
     * Returns content type (mime type) of this picture.
     *
     * @return content type of this picture.
     */
    String getContentType();
    
    /**
     * @return the picture type
     */
    PictureType getType();

    /**
     * Returns the binary data of this Picture
     * @return picture data
     */
    byte[] getData();

    /**
     * Sets the binary picture data
     * <p>
     * The format of the data must match the format of {@link #getType()}. Failure to match the picture data may result
     * in data loss.
     *
     * @param data picture data
     */
    void setData(byte[] data) throws IOException;
    
    /**
     * Gets the checksum - the checksum can be of various length -
     * mostly it's 8 (XSLF) or 16 (HSLF) bytes long.  
     * @return the checksum
     */
    byte[] getChecksum();
    
    /**
     * Return the original image dimensions in points
     * (for formats supported by BufferedImage).
     *
     * Will return a Dimension with a default width of 200x200 if the format unsupported.
     */
    Dimension getImageDimension();
    
    /**
     * Return the original image dimensions in pixels
     * @see PictureData#getImageDimension()
     */
    Dimension getImageDimensionInPixels();
}