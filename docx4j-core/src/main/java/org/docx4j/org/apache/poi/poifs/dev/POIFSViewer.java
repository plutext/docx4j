
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
import java.util.List;

import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A simple viewer for POIFS files
 */

public final class POIFSViewer {

    private POIFSViewer() {}

    /**
     * Display the contents of multiple POIFS files
     *
     * @param args the names of the files to be displayed
     */

    public static void main(final String[] args) {
        if (args.length == 0) {
            System.err.println("Must specify at least one file to view");
            System.exit(1);
        }
        boolean printNames = (args.length > 1);

        for (String arg : args) {
            viewFile(arg, printNames);
        }
    }

    private static void viewFile(String filename, boolean printName) {
        if (printName) {
            StringBuilder flowerbox = new StringBuilder();

            flowerbox.append(".");
            for (int j = 0; j < filename.length(); j++) {
                flowerbox.append("-");
            }
            flowerbox.append(".");
            System.out.println(flowerbox);
            System.out.println("|" + filename + "|");
            System.out.println(flowerbox);
        }
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new File(filename));
            List<String> strings = POIFSViewEngine.inspectViewable(fs, true, 0, "  ");
            for (String s : strings) {
                System.out.print(s);
            }
            fs.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}