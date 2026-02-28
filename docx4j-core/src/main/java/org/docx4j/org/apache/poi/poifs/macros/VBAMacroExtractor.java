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

package org.docx4j.org.apache.poi.poifs.macros;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.StringUtil;

/**
 * This tool extracts out the source of all VBA Modules of an office file,
 *  both OOXML (eg XLSM) and OLE2/POIFS (eg DOC), to STDOUT or a directory.
 *
 * @since 3.15-beta2
 */
public class VBAMacroExtractor {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Use:");
            System.err.println("   VBAMacroExtractor <office.doc> [output]");
            System.err.println();
            System.err.println("If an output directory is given, macros are written there");
            System.err.println("Otherwise they are output to the screen");
            System.exit(1);
        }

        File input = new File(args[0]);
        File output = null;
        if (args.length > 1) {
            output = new File(args[1]);
        }

        VBAMacroExtractor extractor = new VBAMacroExtractor();
        extractor.extract(input, output);
    }

    /**
      * Extracts the VBA modules from a macro-enabled office file and writes them
      * to files in {@code outputDir}.
      *
      * Creates the {@code outputDir}, directory, including any necessary but
      * nonexistent parent directories, if {@code outputDir} does not exist.
      * If {@code outputDir} is null, writes the contents to standard out instead.
      *
      * @param input  the macro-enabled office file.
      * @param outputDir  the directory to write the extracted VBA modules to.
      * @param extension  file extension of the extracted VBA modules
      * @since 3.15-beta2
      */
    public void extract(File input, File outputDir, String extension) throws IOException {
        if (! input.exists()) throw new FileNotFoundException(input.toString());
        System.err.print("Extracting VBA Macros from " + input + " to ");
        if (outputDir != null) {
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Output directory " + outputDir + " could not be created");
            }
            System.err.println(outputDir);
        } else {
            System.err.println("STDOUT");
        }

        final Map<String,String> macros;
        try (VBAMacroReader reader = new VBAMacroReader(input)) {
            macros = reader.readMacros();
        }

        final String divider = "---------------------------------------";
        for (Entry<String, String> entry : macros.entrySet()) {
            String moduleName = entry.getKey();
            String moduleCode = entry.getValue();
            if (outputDir == null) {
                System.out.println(divider);
                System.out.println(moduleName);
                System.out.println();
                System.out.println(moduleCode);
            } else {
                File out = IOUtils.newFile(outputDir, moduleName + extension);
                try (OutputStream fout = Files.newOutputStream(out.toPath());
                     OutputStreamWriter fwriter = new OutputStreamWriter(fout, StringUtil.UTF8)) {
                    fwriter.write(moduleCode);
                }
                System.out.println("Extracted " + out);
            }
        }
        if (outputDir == null) {
            System.out.println(divider);
        }
    }

    /**
      * Extracts the VBA modules from a macro-enabled office file and writes them
      * to {@code .vba} files in {@code outputDir}.
      *
      * Creates the {@code outputDir}, directory, including any necessary but
      * nonexistent parent directories, if {@code outputDir} does not exist.
      * If {@code outputDir} is null, writes the contents to standard out instead.
      *
      * @param input  the macro-enabled office file.
      * @param outputDir  the directory to write the extracted VBA modules to.
      * @since 3.15-beta2
      */
    public void extract(File input, File outputDir) throws IOException {
        extract(input, outputDir, ".vba");
    }
}
