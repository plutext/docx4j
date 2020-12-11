/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.docx4j.org.apache.xml.security.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A class that buffers without synchronizing its methods
 * @author raul
 */
public class UnsyncBufferedOutputStream extends OutputStream {
    static final int size = 8*1024;
    
    private int pointer = 0;
    private final OutputStream out;

    private final byte[] buf;
    
    /**
     * Creates a buffered output stream without synchronization
     * @param out the outputstream to buffer
     */
    public UnsyncBufferedOutputStream(OutputStream out) {
        buf = new byte[size];
        this.out = out;
    }

    /** @inheritDoc */
    public void write(byte[] arg0) throws IOException {
        write(arg0, 0, arg0.length);
    }

    /** @inheritDoc */
    public void write(byte[] arg0, int arg1, int len) throws IOException {
        int newLen = pointer+len;
        if (newLen > size) {
            flushBuffer();		
            if (len > size) {
                out.write(arg0, arg1,len);
                return;
            }
            newLen = len;
        }
        System.arraycopy(arg0, arg1, buf, pointer, len);
        pointer = newLen;
    }

    private void flushBuffer() throws IOException {
        if (pointer > 0) {
            out.write(buf, 0, pointer);
        }
        pointer = 0;

    }

    /** @inheritDoc */
    public void write(int arg0) throws IOException {		
        if (pointer >= size) {
            flushBuffer();
        }
        buf[pointer++] = (byte)arg0;

    }

    /** @inheritDoc */	
    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }

    /** @inheritDoc */
    public void close() throws IOException {
        flush();
        out.close();
    }

}
