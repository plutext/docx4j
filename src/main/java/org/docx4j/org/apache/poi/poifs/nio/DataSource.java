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

package org.docx4j.org.apache.poi.poifs.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Common definition of how we read and write bytes
 */
public abstract class DataSource {
   public abstract ByteBuffer read(int length, long position) throws IOException;
   public abstract void write(ByteBuffer src, long position) throws IOException;
   public abstract long size() throws IOException;
   /** Close the underlying stream */
   public abstract void close() throws IOException;
   /** Copies the contents to the specified OutputStream */
   public abstract void copyTo(OutputStream stream) throws IOException;
}
