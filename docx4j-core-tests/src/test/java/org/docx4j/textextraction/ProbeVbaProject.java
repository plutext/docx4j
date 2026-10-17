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
import java.util.*;
import java.util.zip.ZipFile;
import org.docx4j.org.apache.poi.poifs.filesystem.*;
import org.docx4j.org.apache.poi.poifs.macros.VBAMacroReader;
import org.docx4j.org.apache.poi.util.RLEDecompressingInputStream;

/** Rebuilds POI's SimpleMacro project with attribute-only modules and one comment marker.
 * No procedures or executable code are retained. Usage: ProbeVbaProject seed.docm output.bin.
 */
public final class ProbeVbaProject {
    public static void main(String[] args) throws Exception {
        try (ZipFile seed = new ZipFile(args[0])) {
            byte[] project = seed.getInputStream(seed.getEntry("word/vbaProject.bin")).readAllBytes();
            Files.write(Path.of(args[1]), build(project));
        }
    }

    static byte[] build(byte[] seed) throws Exception {
        Map<String, String> sources;
        try (VBAMacroReader reader = new VBAMacroReader(new ByteArrayInputStream(seed))) {
            sources = reader.readMacros();
        }
        try (POIFSFileSystem old = new POIFSFileSystem(new ByteArrayInputStream(seed));
             POIFSFileSystem fresh = new POIFSFileSystem()) {
            DirectoryNode vba = (DirectoryNode) old.getRoot().getEntry("VBA");
            byte[] dir;
            try (InputStream in = vba.createDocumentInputStream("dir")) {
                dir = RLEDecompressingInputStream.decompress(in.readAllBytes());
            }
            // MODULEOFFSET records (0x0031, size 4). All rebuilt modules start at zero.
            int offsets = 0;
            for (int i = 0; i + 10 <= dir.length; i++) {
                if (dir[i] == 0x31 && dir[i + 1] == 0 && dir[i + 2] == 4
                        && dir[i + 3] == 0 && dir[i + 4] == 0 && dir[i + 5] == 0) {
                    Arrays.fill(dir, i + 6, i + 10, (byte) 0);
                    offsets++;
                }
            }
            if (offsets != sources.size()) throw new IOException("Unexpected module directory");
            for (String name : new String[] {"PROJECT", "PROJECTwm"}) {
                try (InputStream in = old.getRoot().createDocumentInputStream(name)) {
                    fresh.createDocument(in, name);
                }
            }
            DirectoryEntry rebuilt = fresh.createDirectory("VBA");
            rebuilt.createDocument("dir", new ByteArrayInputStream(compress(dir)));
            // MS-OVBA reserved header with version 0xffff: no performance cache.
            rebuilt.createDocument("_VBA_PROJECT", new ByteArrayInputStream(
                    new byte[] {(byte) 0xcc, 0x61, (byte) 0xff, (byte) 0xff, 0, 0, 0}));
            for (Map.Entry<String, String> module : sources.entrySet()) {
                StringBuilder text = new StringBuilder();
                for (String line : module.getValue().split("\\r?\\n")) {
                    if (line.startsWith("Attribute ")) text.append(line).append("\r\n");
                }
                if (module.getKey().equals("Module1")) text.append("Option Explicit\r\n' VBA51_MODULE_MARK\r\n");
                rebuilt.createDocument(module.getKey(), new ByteArrayInputStream(
                        compress(text.toString().getBytes(StandardCharsets.ISO_8859_1))));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            fresh.writeFilesystem(out);
            byte[] bytes = out.toByteArray();
            check(bytes);
            return bytes;
        }
    }

    /** MS-OVBA compressed chunks containing literals only (one flag byte per eight literals). */
    static byte[] compress(byte[] source) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(1);
        for (int start = 0; start < source.length; start += 3600) {
            int length = Math.min(3600, source.length - start);
            int size = length + (length + 7) / 8;
            int header = 0xb000 | (size - 1);
            out.write(header & 255); out.write(header >>> 8);
            for (int i = 0; i < length; i += 8) {
                out.write(0);
                out.write(source, start + i, Math.min(8, length - i));
            }
        }
        return out.toByteArray();
    }

    static void check(byte[] project) throws Exception {
        try (VBAMacroReader reader = new VBAMacroReader(new ByteArrayInputStream(project))) {
            Map<String, String> modules = reader.readMacros();
            String source = modules.get("Module1");
            if (source == null || !source.contains("' VBA51_MODULE_MARK"))
                throw new IOException("Missing VBA module marker");
            for (String text : modules.values()) {
                for (String line : text.split("\\r?\\n")) {
                    if (!line.isBlank() && !line.startsWith("Attribute ")
                            && !line.equals("Option Explicit") && !line.startsWith("'"))
                        throw new IOException("Executable VBA in probe: " + line);
                }
            }
        }
    }
}
