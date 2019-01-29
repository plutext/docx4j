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

package org.docx4j.org.apache.poi.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Josh Micich
 */
public final class LittleEndianOutputStream extends FilterOutputStream implements LittleEndianOutput {
	public LittleEndianOutputStream(OutputStream out) {
		super(out);
	}

	@Override
    public void writeByte(int v) {
		try {
			out.write(v);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
    public void writeDouble(double v) {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
    public void writeInt(int v) {
		int b3 = (v >>> 24) & 0xFF;
		int b2 = (v >>> 16) & 0xFF;
		int b1 = (v >>>  8) & 0xFF;
		int b0 = (v >>>  0) & 0xFF;
		try {
			out.write(b0);
			out.write(b1);
			out.write(b2);
			out.write(b3);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
    public void writeLong(long v) {
		writeInt((int)(v >>  0));
		writeInt((int)(v >> 32));
	}

	@Override
    public void writeShort(int v) {
		int b1 = (v >>>  8) & 0xFF;
		int b0 = (v >>>  0) & 0xFF;
		try {
			out.write(b0);
			out.write(b1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
    public void write(byte[] b) {
		// suppress IOException for interface method
		try {
			super.write(b);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
    public void write(byte[] b, int off, int len) {
		// suppress IOException for interface method
		try {
			super.write(b, off, len);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
