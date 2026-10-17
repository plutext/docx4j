/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.textextraction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.docx4j.org.apache.poi.poifs.filesystem.*;

/** Reuses the Word-authored MSForms Label storage in LegacyForms.docx, changing its caption only. */
final class ProbeActiveXControl {
    static final String CLASS_ID = "{978C9E23-D4B0-11CE-BF2D-00AA003F40D0}";
    static final String CAPTION = "ACX42_CAPTION";
    static byte[] build() throws Exception {
        Path fixture = Path.of("docx4j-core-tests/src/test/resources/LegacyForms.docx");
        byte[] seed;
        try (ZipFile zip = new ZipFile(fixture.toFile())) {
            seed = zip.getInputStream(zip.getEntry("word/activeX/activeX3.bin")).readAllBytes();
        }
        try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(seed))) {
            byte[] original;
            try (InputStream in = fs.getRoot().createDocumentInputStream("contents")) { original = in.readAllBytes(); }
            // MS-OFORMS LabelControl: version, cbLabel, PropMask, colors, compressed caption, size, font.
            // This is intentionally specific to the checked fixture; fail if its layout ever changes.
            if (original.length != 64 || ushort(original, 2) != 32 || original[4] != 0x2b
                    || original[16] != 6 || original[19] != (byte) 0x80
                    || !new String(original, 20, 6, StandardCharsets.US_ASCII).equals("Label1"))
                throw new IOException("LegacyForms Label storage layout changed");
            byte[] caption = CAPTION.getBytes(StandardCharsets.US_ASCII);
            int padded = (caption.length + 3) & ~3;
            int delta = padded - 8;
            byte[] contents = new byte[original.length + delta];
            System.arraycopy(original, 0, contents, 0, 20);
            int cbLabel = ushort(original, 2) + delta;
            contents[2] = (byte) cbLabel; contents[3] = (byte) (cbLabel >>> 8);
            contents[16] = (byte) caption.length;
            System.arraycopy(caption, 0, contents, 20, caption.length);
            System.arraycopy(original, 28, contents, 20 + padded, original.length - 28);
            fs.getRoot().getEntry("contents").delete();
            fs.createDocument(new ByteArrayInputStream(contents), "contents");
            ByteArrayOutputStream out = new ByteArrayOutputStream(); fs.writeFilesystem(out);
            byte[] bytes = out.toByteArray();
            check(bytes);
            return bytes;
        }
    }
    static int ushort(byte[] b, int offset) { return (b[offset] & 255) | ((b[offset + 1] & 255) << 8); }
    static void check(byte[] bytes) throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(bytes));
             InputStream in = fs.getRoot().createDocumentInputStream("contents")) {
            byte[] contents = in.readAllBytes();
            int length = contents[16] & 255;
            if (!new String(contents, 20, length, StandardCharsets.US_ASCII).equals(CAPTION)
                    || ushort(contents, 2) != 32 + ((length + 3) & ~3) - 8)
                throw new IOException("ActiveX Label caption or size mismatch");
            if (!fs.getRoot().hasEntry("\u0001CompObj")) throw new IOException("Missing control class information");
        }
    }
}
