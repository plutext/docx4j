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

import java.io.IOException;
import java.util.List;

/**
 * Implementers provide find method for searching native operating system
 * for available fonts.
 */
public interface FontFinder {

    /**
     * Finds a list of font files.
     *
     * @return list of font files. List&lt;URL&gt; in the case of the
     *         FontFinder, and List&lt;File&gt; in the case of the
     *         FonrDirFinders.
     * @throws IOException
     *             In case of an I/O problem
     */
    List find() throws IOException;

}
