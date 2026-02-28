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
package org.docx4j.org.apache.poi.poifs.dev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentNode;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSStream;
import org.docx4j.org.apache.poi.poifs.property.PropertyTable;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;
import org.docx4j.org.apache.poi.util.IOUtils;

/**
 * Dump internal structure of a OLE2 file into file system
 */
public final class POIFSDump {

    private POIFSDump() {}

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Must specify at least one file to dump");
            System.exit(1);
        }
        
        boolean dumpProps = false, dumpMini = false;
        for (String filename : args) {
            if (filename.equalsIgnoreCase("-dumprops") ||
                    filename.equalsIgnoreCase("-dump-props") ||
                    filename.equalsIgnoreCase("-dump-properties")) {
                dumpProps = true;
                continue;
            }
            if (filename.equalsIgnoreCase("-dumpmini") ||
                    filename.equalsIgnoreCase("-dump-mini") ||
                    filename.equalsIgnoreCase("-dump-ministream") ||
                    filename.equalsIgnoreCase("-dump-mini-stream")) {
                dumpMini = true;
                continue;
            }

            System.out.println("Dumping " + filename);
            try (InputStream is = Files.newInputStream(Path.of(filename));
                 POIFSFileSystem fs = new POIFSFileSystem(is)) {
                DirectoryEntry root = fs.getRoot();
                String filenameWithoutPath = new File(filename).getName();
                File dumpDir = new File(filenameWithoutPath + "_dump");
                File file = IOUtils.newFile(dumpDir, root.getName());
                if (!file.exists() && !file.mkdirs()) {
                    throw new IOException("Could not create directory " + file);
                }
    
                dump(root, file);
    
                if (dumpProps) {
                    HeaderBlock header = fs.getHeaderBlock();
                    dump(fs, header.getPropertyStart(), "properties", file);
                }
                if (dumpMini) {
                    PropertyTable props = fs.getPropertyTable();
                    int startBlock = props.getRoot().getStartBlock();
                    if (startBlock == POIFSConstants.END_OF_CHAIN) {
                        System.err.println("No Mini Stream in file");
                    } else {
                        dump(fs, startBlock, "mini-stream", file);
                    }
                }
            }
        }
    }
    
    public static void dump(DirectoryEntry root, File parent) throws IOException {
        for(Iterator<Entry> it = root.getEntries(); it.hasNext();){
            Entry entry = it.next();
            if(entry instanceof DocumentNode){
                final DocumentNode node = (DocumentNode) entry;
                final byte[] bytes;
                try (DocumentInputStream is = new DocumentInputStream(node)) {
                   bytes = IOUtils.toByteArray(is);
                }
                try (OutputStream out = Files.newOutputStream(
                        IOUtils.newFile(parent, node.getName().trim()).toPath())) {
                    out.write(bytes);
                }
            } else if (entry instanceof DirectoryEntry){
                DirectoryEntry dir = (DirectoryEntry)entry;
                File file = IOUtils.newFile(parent, entry.getName());
                if (!file.exists() && !file.mkdirs()) {
                    throw new IOException("Could not create directory " + file);
                }
                dump(dir, file);
            } else {
                System.err.println("Skipping unsupported POIFS entry: " + entry);
            }
        }
    }

    public static void dump(POIFSFileSystem fs, int startBlock, String name, File parent) throws IOException {
        File file = IOUtils.newFile(parent, name);
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            POIFSStream stream = new POIFSStream(fs, startBlock);

            byte[] b = IOUtils.safelyAllocate(fs.getBigBlockSize(), POIFSFileSystem.getMaxRecordLength());
            for (ByteBuffer bb : stream) {
                int len = bb.remaining();
                bb.get(b);
                out.write(b, 0, len);
            }
        }
    }
}
