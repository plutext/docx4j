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

package org.docx4j.fonts.fop.fonts.truetype;

import java.io.IOException;

/**
 * An interface for writing individual glyphs from the glyf table of a TrueType font to an output stream.
 */
public interface TTFGlyphOutputStream {

    /**
     * Begins the streaming of glyphs.
     */
    void startGlyphStream() throws IOException;

    /**
     * Streams an individual glyph from the given byte array.
     *
     * @param glyphData the source of the glyph data to stream from
     * @param offset the position in the glyph data where the glyph starts
     * @param size the size of the glyph data in bytes
     */
    void streamGlyph(byte[] glyphData, int offset, int size) throws IOException;

    /**
     * Ends the streaming of glyphs.
     */
    void endGlyphStream() throws IOException;

}
