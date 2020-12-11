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
/**
 * Implementors of this interface allow client code to 'delay' writing to a certain section of a
 * data output stream.<br/>
 * A typical application is for writing BIFF records when the size is not known until well after
 * the header has been written.  The client code can call {@link #createDelayedOutput(int)}
 * to reserve two bytes of the output for the 'ushort size' header field.  The delayed output can
 * be written at any stage.
 *
 * @author Josh Micich
 */
public interface DelayableLittleEndianOutput extends LittleEndianOutput {
	/**
	 * Creates an output stream intended for outputting a sequence of <tt>size</tt> bytes.
	 */
	LittleEndianOutput createDelayedOutput(int size);
}
