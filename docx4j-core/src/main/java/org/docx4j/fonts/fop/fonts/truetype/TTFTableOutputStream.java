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
 * An interface for writing a TrueType table to an output stream.
 */
public interface TTFTableOutputStream {

    /**
     * Streams a table from the given byte array.
     *
     * @param ttfData the source of the table to stream from
     * @param offset the position in the byte array where the table starts
     * @param size the size of the table in bytes
     */
    void streamTable(byte[] ttfData, int offset, int size) throws IOException;
}
