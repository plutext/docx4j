/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.fonts.fop.fonts.autodetect;

import java.io.File;
import java.util.List;

/**
 * Native font finder base class
 */
public abstract class NativeFontDirFinder implements FontFinder {

    /**
     * Generic method used by Mac and Unix font finders.
     * @return list of natively existing font directories
     * {@inheritDoc}
     */
    public List find() {
        List fontDirList = new java.util.ArrayList();
        String[] searchableDirectories = getSearchableDirectories();
        if (searchableDirectories != null) {
            for (int i = 0; i < searchableDirectories.length; i++) {
                File fontDir = new File(searchableDirectories[i]);
                if (fontDir.exists() && fontDir.canRead()) {
                    fontDirList.add(fontDir);
                }
            }
        }
        return fontDirList;
    }

    /**
     * Returns an array of directories to search for fonts in.
     * @return an array of directories
     */
    protected abstract String[] getSearchableDirectories();

}
