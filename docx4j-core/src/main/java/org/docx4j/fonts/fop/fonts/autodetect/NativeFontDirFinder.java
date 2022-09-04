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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Native font finder base class
 */
public abstract class NativeFontDirFinder implements FontDirFinder {

    /**
     * Generic method used by Mac and Unix font finders.
     * @return list of natively existing font directories
     * {@inheritDoc}
     */
    public List<File> find() {
        List<File> fontDirList = new java.util.ArrayList<File>();
        String[] searchableDirectories = getSearchableDirectories();
        if (searchableDirectories != null) {
            fontDirList = Arrays.stream(searchableDirectories).map(File::new).filter(fontDir -> fontDir.exists() && fontDir.canRead()).collect(Collectors.toList());
        }
        return fontDirList;
    }

    /**
     * Returns an array of directories to search for fonts in.
     * @return an array of directories
     */
    protected abstract String[] getSearchableDirectories();

}
