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

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.property.DirectoryProperty;
import org.docx4j.org.apache.poi.poifs.property.Property;
import org.docx4j.org.apache.poi.poifs.property.PropertyTable;
import org.docx4j.org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;
import org.docx4j.org.apache.poi.poifs.storage.ListManagedBlock;
import org.docx4j.org.apache.poi.poifs.storage.RawDataBlockList;
import org.docx4j.org.apache.poi.poifs.storage.SmallBlockTableReader;
import org.docx4j.org.apache.poi.util.HexDump;
import org.docx4j.org.apache.poi.util.IntList;

/**
 * A very low level debugging tool, for printing out core 
 *  information on the headers and FAT blocks.
 * You probably only want to use this if you're trying
 *  to understand POIFS, or if you're trying to track
 *  down the source of corruption in a file.
 */
public class POIFSHeaderDumper {
    /**
     * Display the entries of multiple POIFS files
     *
     * @param args the names of the files to be displayed
     */
    public static void main(final String args[]) throws Exception {
        if (args.length == 0) {
            System.err.println("Must specify at least one file to view");
            System.exit(1);
        }

        for (int j = 0; j < args.length; j++) {
            viewFile(args[j]);
        }
    }

    public static void viewFile(final String filename) throws Exception {
        System.out.println("Dumping headers from: " + filename);
        InputStream inp = new FileInputStream(filename);

        // Header
        HeaderBlock header_block = new HeaderBlock(inp);
        displayHeader(header_block);

        // Raw blocks
        POIFSBigBlockSize bigBlockSize = header_block.getBigBlockSize();
        RawDataBlockList data_blocks = new RawDataBlockList(inp, bigBlockSize);
        displayRawBlocksSummary(data_blocks);

        // Main FAT Table
        BlockAllocationTableReader batReader =
                new BlockAllocationTableReader(
                        header_block.getBigBlockSize(),
                        header_block.getBATCount(),
                        header_block.getBATArray(),
                        header_block.getXBATCount(),
                        header_block.getXBATIndex(),
                        data_blocks);
        displayBATReader("Big Blocks", batReader);

        // Properties Table
        PropertyTable properties =
                new PropertyTable(header_block, data_blocks);

        // Mini Fat
        BlockAllocationTableReader sbatReader = 
                SmallBlockTableReader._getSmallDocumentBlockReader(
                        bigBlockSize, data_blocks, properties.getRoot(),
                        header_block.getSBATStart()
                        );
        displayBATReader("Small Blocks", sbatReader);

        // Summary of the properties
        displayPropertiesSummary(properties);
    }

    public static void displayHeader(HeaderBlock header_block) throws Exception {
        System.out.println("Header Details:");
        System.out.println(" Block size: " + header_block.getBigBlockSize().getBigBlockSize());
        System.out.println(" BAT (FAT) header blocks: " + header_block.getBATArray().length);
        System.out.println(" BAT (FAT) block count: " + header_block.getBATCount());
        if (header_block.getBATCount() > 0) 
            System.out.println(" BAT (FAT) block 1 at: " + header_block.getBATArray()[0]);
        System.out.println(" XBAT (FAT) block count: " + header_block.getXBATCount());
        System.out.println(" XBAT (FAT) block 1 at: " + header_block.getXBATIndex());
        System.out.println(" SBAT (MiniFAT) block count: " + header_block.getSBATCount());
        System.out.println(" SBAT (MiniFAT) block 1 at: " + header_block.getSBATStart());
        System.out.println(" Property table at: " + header_block.getPropertyStart());
        System.out.println("");
    }

    public static void displayRawBlocksSummary(RawDataBlockList data_blocks) throws Exception {
        System.out.println("Raw Blocks Details:");
        System.out.println(" Number of blocks: " + data_blocks.blockCount());

        Method gbm = data_blocks.getClass().getSuperclass().getDeclaredMethod("get", int.class);
        gbm.setAccessible(true);

        for(int i=0; i<Math.min(16, data_blocks.blockCount()); i++) {
            ListManagedBlock block = (ListManagedBlock)gbm.invoke(data_blocks, Integer.valueOf(i));
            byte[] data = new byte[Math.min(48, block.getData().length)];
            System.arraycopy(block.getData(), 0, data, 0, data.length);

            System.out.println(" Block #" + i + ":");
            System.out.println(HexDump.dump(data, 0, 0));
        }

        System.out.println("");
    }

    public static void displayBATReader(String type, BlockAllocationTableReader batReader) throws Exception {
        System.out.println("Sectors, as referenced from the "+type+" FAT:");
        Field entriesF = batReader.getClass().getDeclaredField("_entries");
        entriesF.setAccessible(true);
        IntList entries = (IntList)entriesF.get(batReader);

        for(int i=0; i<entries.size(); i++) {
            int bn = entries.get(i);
            String bnS = Integer.toString(bn);
            if(bn == POIFSConstants.END_OF_CHAIN) {
                bnS = "End Of Chain";
            } else if(bn == POIFSConstants.DIFAT_SECTOR_BLOCK) {
                bnS = "DI Fat Block";
            } else if(bn == POIFSConstants.FAT_SECTOR_BLOCK) {
                bnS = "Normal Fat Block";
            } else if(bn == POIFSConstants.UNUSED_BLOCK) {
                bnS = "Block Not Used (Free)";
            }

            System.out.println("  Block  # " + i + " -> " + bnS);
        }

        System.out.println("");
    }

    public static void displayPropertiesSummary(PropertyTable properties) {
        System.out.println("Mini Stream starts at " + properties.getRoot().getStartBlock());
        System.out.println("Mini Stream length is " + properties.getRoot().getSize());
        System.out.println();
        
        System.out.println("Properties and their block start:");
        displayProperties(properties.getRoot(), "");
        System.out.println("");
    }
    public static void displayProperties(DirectoryProperty prop, String indent) {
        String nextIndent = indent + "  ";
        System.out.println(indent + "-> " + prop.getName());
        for (Property cp : prop) {
            if (cp instanceof DirectoryProperty) {
                displayProperties((DirectoryProperty)cp, nextIndent);
            } else {
                System.out.println(nextIndent + "=> " + cp.getName());
                System.out.print(nextIndent + "   " + cp.getSize() + " bytes in ");
                if (cp.shouldUseSmallBlocks()) {
                    System.out.print("mini");
                } else {
                    System.out.print("main");
                }
                System.out.println(" stream, starts at " + cp.getStartBlock());
            }
        }
    }
}
