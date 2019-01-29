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
        

package org.docx4j.org.apache.poi.poifs.storage;

import java.io.IOException;

/**
 * An interface for blocks managed by a list that works with a
 * BlockAllocationTable to keep block sequences straight
 *
 * @author Marc Johnson (mjohnson at apache dot org
 */

public interface ListManagedBlock
{

    /**
     * Get the data from the block
     *
     * @return the block's data as a byte array
     *
     * @exception IOException if there is no data
     */

    public byte [] getData()
        throws IOException;
}   // end public interface ListManagedBlock

