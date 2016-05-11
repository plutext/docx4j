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
package org.docx4j.org.apache.poi.poifs.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentNode;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSStream;
import org.docx4j.org.apache.poi.poifs.property.NPropertyTable;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;

/**
 * Dump internal structure of a OLE2 file into file system
 */
public class POIFSDump {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Must specify at least one file to dump");
            System.exit(1);
        }
        
        boolean dumpProps = false, dumpMini = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-dumprops") ||
                args[i].equalsIgnoreCase("-dump-props") ||
                args[i].equalsIgnoreCase("-dump-properties")) {
                dumpProps = true;
                continue;
            }
            if (args[i].equalsIgnoreCase("-dumpmini") ||
                args[i].equalsIgnoreCase("-dump-mini") ||
                args[i].equalsIgnoreCase("-dump-ministream") ||
                args[i].equalsIgnoreCase("-dump-mini-stream")) {
                dumpMini = true;
                continue;
            }
            
            System.out.println("Dumping " + args[i]);
            FileInputStream is = new FileInputStream(args[i]);
            NPOIFSFileSystem fs = new NPOIFSFileSystem(is);
            is.close();

            DirectoryEntry root = fs.getRoot();
            File file = new File(root.getName());
            file.mkdir();

            dump(root, file);
            
            if (dumpProps) {
                HeaderBlock header = getHeaderBlock(fs);
                dump(fs, header.getPropertyStart(), "properties", file);
            }
            if (dumpMini) {
                NPropertyTable props = getPropertyTable(fs);
                int startBlock = props.getRoot().getStartBlock(); 
                if (startBlock == POIFSConstants.END_OF_CHAIN) {
                    System.err.println("No Mini Stream in file");
                } else {
                    dump(fs, startBlock, "mini-stream", file);
                }
            }
        }
   }
    
    protected static HeaderBlock getHeaderBlock(NPOIFSFileSystem fs) throws Exception {
        Field headerF = NPOIFSFileSystem.class.getDeclaredField("_header");
        headerF.setAccessible(true);
        HeaderBlock header = (HeaderBlock)headerF.get(fs);
        return header;
    }
    protected static NPropertyTable getPropertyTable(NPOIFSFileSystem fs) throws Exception {
        Field ptF = NPOIFSFileSystem.class.getDeclaredField("_property_table");
        ptF.setAccessible(true);
        NPropertyTable table = (NPropertyTable)ptF.get(fs);
        return table;
    }

    public static void dump(DirectoryEntry root, File parent) throws IOException {
        for(Iterator<Entry> it = root.getEntries(); it.hasNext();){
            Entry entry = it.next();
            if(entry instanceof DocumentNode){
                DocumentNode node = (DocumentNode)entry;
                DocumentInputStream is = new DocumentInputStream(node);
                byte[] bytes = new byte[node.getSize()];
                is.read(bytes);
                is.close();

                OutputStream out = new FileOutputStream(new File(parent, node.getName().trim()));
                try {
                	out.write(bytes);
                } finally {
                	out.close();
                }
            } else if (entry instanceof DirectoryEntry){
                DirectoryEntry dir = (DirectoryEntry)entry;
                File file = new File(parent, entry.getName());
                file.mkdir();
                dump(dir, file);
            } else {
                System.err.println("Skipping unsupported POIFS entry: " + entry);
            }
        }
    }
    public static void dump(NPOIFSFileSystem fs, int startBlock, String name, File parent) throws IOException {
        File file = new File(parent, name);
        FileOutputStream out = new FileOutputStream(file);
        NPOIFSStream stream = new NPOIFSStream(fs, startBlock);
        
        byte[] b = new byte[fs.getBigBlockSize()];
        for (ByteBuffer bb : stream) {
            int len = bb.remaining();
            bb.get(b);
            out.write(b, 0, len);
        }
        out.close();
    }
}
