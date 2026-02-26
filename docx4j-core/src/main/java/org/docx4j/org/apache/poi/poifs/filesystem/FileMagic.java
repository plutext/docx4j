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

package org.apache.poi.poifs.filesystem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.poi.poifs.storage.HeaderBlockConstants;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LocaleUtil;

/**
 * The file magic number, i.e. the file identification based on the first bytes
 * of the file
 */
public enum FileMagic {
    /** OLE2 / BIFF8+ stream used for Office 97 and higher documents */
    OLE2(HeaderBlockConstants._signature),
    /** OOXML / ZIP stream - The first 4 bytes of an OOXML file, used in detection */
    OOXML(0x50, 0x4b, 0x03, 0x04),
    /** XML file - The first 5 bytes of a raw XML file, used in detection */
    XML(0x3c, 0x3f, 0x78, 0x6d, 0x6c),
    /** BIFF2 raw stream - for Excel 2 */
    BIFF2(
        0x09, 0x00, // sid=0x0009
        0x04, 0x00, // size=0x0004
        0x00, 0x00, // unused
        '?', 0x00  // '?' = multiple values
    ),
    /** BIFF3 raw stream - for Excel 3 */
    BIFF3(
        0x09, 0x02, // sid=0x0209
        0x06, 0x00, // size=0x0006
        0x00, 0x00, // unused
        '?', 0x00  // '?' = multiple values
    ),
    /** BIFF4 raw stream - for Excel 4 */
    BIFF4(new byte[]{
        0x09, 0x04, // sid=0x0409
        0x06, 0x00, // size=0x0006
        0x00, 0x00, // unused
        '?', 0x00  // '? = multiple values
    },new byte[]{
        0x09, 0x04, // sid=0x0409
        0x06, 0x00, // size=0x0006
        0x00, 0x00, // unused
        0x00, 0x01
    }),
    /** Old MS Write raw stream */
    MSWRITE(
        new byte[]{0x31, (byte)0xbe, 0x00, 0x00 },
        new byte[]{0x32, (byte)0xbe, 0x00, 0x00 }),
    /** RTF document */
    RTF("{\\rtf"),
    /** PDF document */
    PDF("%PDF"),
    /** Some different HTML documents */
    HTML("<!DOCTYP",
         "<html","\n\r<html","\r\n<html","\r<html","\n<html",
         "<HTML","\r\n<HTML","\n\r<HTML","\r<HTML","\n<HTML"),
    WORD2(0xdb, 0xa5, 0x2d, 0x00),
    /** JPEG image */
    JPEG(
        new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xDB },
        new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, '?', '?', 'J', 'F', 'I', 'F', 0x00, 0x01 },
        new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xEE },
        new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE1, '?', '?', 'E', 'x', 'i', 'f', 0x00, 0x00 }),
    /** GIF image */
    GIF("GIF87a","GIF89a"),
    /** PNG Image */
    PNG(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A),
    /** TIFF Image */
    TIFF("II*\u0000", "MM\u0000*" ),
    /** WMF image with a placeable header */
    WMF(0xD7, 0xCD, 0xC6, 0x9A),
    /** EMF image */
    EMF(1, 0, 0, 0,
        '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?',
        '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?',
        ' ', 'E', 'M', 'F'),
    /** BMP image */
    BMP('B','M'),
    // keep UNKNOWN always as last enum!
    /** UNKNOWN magic */
    UNKNOWN(new byte[0]);

    // update this if a longer pattern is added
    static final int MAX_PATTERN_LENGTH = 44;

    final byte[][] magic;

    FileMagic(long magic) {
        this.magic = new byte[1][8];
        LittleEndian.putLong(this.magic[0], 0, magic);
    }

    FileMagic(int... magic) {
        byte[] one = new byte[magic.length];
        for (int i=0; i<magic.length; i++) {
            one[i] = (byte)(magic[i] & 0xFF);
        }
        this.magic = new byte[][]{ one };
    }

    FileMagic(byte[]... magic) {
        this.magic = magic;
    }

    FileMagic(String... magic) {
        this.magic = new byte[magic.length][];
        int i=0;
        for (String s : magic) {
            this.magic[i++] = s.getBytes(LocaleUtil.CHARSET_1252);
        }
    }

    public static FileMagic valueOf(byte[] magic) {
        for (FileMagic fm : values()) {
            for (byte[] ma : fm.magic) {
                // don't try to match if the given byte-array is too short
                // for this pattern anyway
                if(magic.length < ma.length) {
                    continue;
                }

                if (findMagic(ma, magic)) {
                    return fm;
                }
            }
        }
        return UNKNOWN;
    }

    private static boolean findMagic(byte[] expected, byte[] actual) {
        int i=0;
        for (byte expectedByte : expected) {
            if (actual[i++] != expectedByte && expectedByte != '?') {
                return false;
            }
        }
        return true;
    }


    /**
     * Get the file magic of the supplied {@link File}<p>
     *
     * Even if this method returns {@link FileMagic#UNKNOWN} it could potentially mean,
     *  that the ZIP stream has leading junk bytes
     *
     * @param inp a file to be identified
     */
    public static FileMagic valueOf(final File inp) throws IOException {
        try (InputStream fis = Files.newInputStream(inp.toPath())) {
            // read as many bytes as possible, up to the required number of bytes
            byte[] data = new byte[MAX_PATTERN_LENGTH];
            int read = IOUtils.readFully(fis, data, 0, MAX_PATTERN_LENGTH);
            if(read == -1) {
                return FileMagic.UNKNOWN;
            }

            // only use the bytes that could be read
            data = Arrays.copyOf(data, read);

            return FileMagic.valueOf(data);
        }
    }


    /**
     * Get the file magic of the supplied InputStream (which MUST
     *  support mark and reset).<p>
     *
     * If unsure if your InputStream does support mark / reset,
     *  use {@link #prepareToCheckMagic(InputStream)} to wrap it and make
     *  sure to always use that, and not the original!<p>
     *
     * Even if this method returns {@link FileMagic#UNKNOWN} it could potentially mean,
     *  that the ZIP stream has leading junk bytes
     *
     * @param inp An InputStream which supports either mark/reset
     */
    public static FileMagic valueOf(InputStream inp) throws IOException {
        if (!inp.markSupported()) {
            throw new IOException("getFileMagic() only operates on streams which support mark(int)");
        }

        // Grab the first bytes of this stream
        byte[] data = IOUtils.peekFirstNBytes(inp, MAX_PATTERN_LENGTH);

        return FileMagic.valueOf(data);
    }


    /**
     * Checks if an {@link InputStream} can be reset (i.e. used for checking the header magic) and wraps it if not
     *
     * @param stream stream to be checked for wrapping
     * @return a mark enabled stream
     */
    public static InputStream prepareToCheckMagic(InputStream stream) {
        if (stream.markSupported()) {
            return stream;
        }
        // we used to process the data via a PushbackInputStream, but user code could provide a too small one
        // so we use a BufferedInputStream instead now
        return new BufferedInputStream(stream);
    }
}