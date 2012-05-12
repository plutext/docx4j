/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: WindowsFontDirFinder.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.autodetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * FontFinder for native Windows platforms
 */
public class WindowsFontDirFinder implements FontFinder {

    /**
     * Attempts to read windir environment variable on windows
     * (disclaimer: This is a bit dirty but seems to work nicely)
     */
    private String getWinDir(String osName) throws IOException {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        if (osName.startsWith("Windows 9")) {
            process = runtime.exec("command.com /c echo %windir%");
        } else {
            process = runtime.exec("cmd.exe /c echo %windir%");
        }
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        return bufferedReader.readLine();
    }

    /**
     * {@inheritDoc}
     * @return a list of detected font files
     */
    public List find() {
        List fontDirList = new java.util.ArrayList();
        String windir = null;
        try {
            windir = System.getProperty("env.windir");
        } catch (SecurityException e) {
            // should continue if this fails
        }
        String osName = System.getProperty("os.name");
        if (windir == null) {
            try {
                windir = getWinDir(osName);
            } catch (IOException e) {
                // should continue if this fails
            }
        }
        File osFontsDir = null, psFontsDir = null;
        if (windir != null) {
            // remove any trailing '/'
            if (windir.endsWith("/")) {
                windir = windir.substring(0, windir.length() - 1);
            }
            osFontsDir = new File(windir + File.separator + "FONTS");
            if (osFontsDir.exists() && osFontsDir.canRead()) {
                fontDirList.add(osFontsDir);
            }
            psFontsDir = new File(windir.substring(0, 2) + File.separator + "PSFONTS");
            if (psFontsDir.exists() && psFontsDir.canRead()) {
                fontDirList.add(psFontsDir);
            }
        } else {
            String windowsDirName = osName.endsWith("NT") ? "WINNT" : "WINDOWS";
            // look for true type font folder
            for (char driveLetter = 'C'; driveLetter <= 'E'; driveLetter++) {
                osFontsDir = new File(
                        driveLetter + ":"
                        + File.separator + windowsDirName
                        + File.separator + "FONTS");
                if (osFontsDir.exists() && osFontsDir.canRead()) {
                    fontDirList.add(osFontsDir);
                    break;
                }
            }
            // look for type 1 font folder
            for (char driveLetter = 'C'; driveLetter <= 'E'; driveLetter++) {
                psFontsDir = new File(driveLetter + ":" + File.separator + "PSFONTS");
                if (psFontsDir.exists() && psFontsDir.canRead()) {
                    fontDirList.add(psFontsDir);
                    break;
                }
            }
        }
        return fontDirList;
    }
}
