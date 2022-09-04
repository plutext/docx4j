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

/* $Id$ */

package org.docx4j.fonts.fop.fonts.autodetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * FontFinder for native Windows platforms
 */
public class WindowsFontDirFinder implements FontDirFinder {

    /**
     * Attempts to read windir environment variable on windows
     * (disclaimer: This is a bit dirty but seems to work nicely)
     */
    private static String getWinDir(String osName) throws IOException {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        if (osName.startsWith("Windows 9")) {
            process = runtime.exec("command.com /c echo %windir%");
        } else {
            process = runtime.exec("cmd.exe /c echo %windir%");
        }
        InputStreamReader isr = null;
        BufferedReader bufferedReader = null;
        String dir = "";
        try {
            isr = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(isr);
            dir = bufferedReader.readLine();
        } finally {
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(isr);
        }
        return dir;
    }

    /**
     * {@inheritDoc}
     * @return a list of detected font files
     */
    public List<File> find() {
        List<File> fontDirList = new java.util.ArrayList<File>();
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
        File osFontsDir = null;
        File psFontsDir = null;
        if (windir != null) {
            // remove any trailing '/'
            if (!windir.isEmpty() && windir.charAt(windir.length() - 1) == '/') {
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
