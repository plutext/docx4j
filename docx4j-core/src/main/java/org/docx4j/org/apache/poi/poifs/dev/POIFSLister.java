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
import java.io.IOException;
import java.util.Iterator;

import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentNode;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A lister of the entries in POIFS files.
 *
 * Much simpler than {@link POIFSViewer}
 */
public class POIFSLister {
   /**
    * Display the entries of multiple POIFS files
    *
    * @param args the names of the files to be displayed
    */
   public static void main(final String args[]) throws IOException {
      if (args.length == 0) {
         System.err.println("Must specify at least one file to view");
         System.exit(1);
      }

      boolean withSizes = false;
      boolean newPOIFS = true;
      for (int j = 0; j < args.length; j++) {
         if (args[j].equalsIgnoreCase("-size") || args[j].equalsIgnoreCase("-sizes")) {
            withSizes = true;
         } else if (args[j].equalsIgnoreCase("-old") || args[j].equalsIgnoreCase("-old-poifs")) {
            newPOIFS = false;
         } else {
            if(newPOIFS) {
               viewFile(args[j], withSizes);
            } else {
               viewFileOld(args[j], withSizes);
            }
         }
      }
   }

   public static void viewFile(final String filename, boolean withSizes) throws IOException {
      NPOIFSFileSystem fs = new NPOIFSFileSystem(new File(filename));
      displayDirectory(fs.getRoot(), "", withSizes);
   }

   public static void viewFileOld(final String filename, boolean withSizes) throws IOException {
      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
      displayDirectory(fs.getRoot(), "", withSizes);
   }

   public static void displayDirectory(DirectoryNode dir, String indent, boolean withSizes) {
      System.out.println(indent + dir.getName() + " -");
      String newIndent = indent + "  ";

      boolean hadChildren = false;
      for(Iterator<Entry> it = dir.getEntries(); it.hasNext();) {
         hadChildren = true;
         Entry entry = it.next();
         if (entry instanceof DirectoryNode) {
            displayDirectory((DirectoryNode) entry, newIndent, withSizes);
         } else {
            DocumentNode doc = (DocumentNode) entry;
            String name = doc.getName();
            String size = "";
            if (name.charAt(0) < 10) {
               String altname = "(0x0" + (int) name.charAt(0) + ")" + name.substring(1);
               name = name.substring(1) + " <" + altname + ">";
            }
            if (withSizes) {
               size = " [" + doc.getSize() + " / 0x" + 
                      Integer.toHexString(doc.getSize()) + "]";
            }
            System.out.println(newIndent + name + size);
         }
      }
      if (!hadChildren) {
         System.out.println(newIndent + "(no children)");
      }
   }
}
