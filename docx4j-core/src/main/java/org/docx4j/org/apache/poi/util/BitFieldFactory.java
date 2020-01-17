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

import java.util.*;

/**
 * Returns immutable Btfield instances.
 *
 * @author Jason Height (jheight at apache dot org)
 */

public class BitFieldFactory {
    private static Map<Integer, BitField> instances = new HashMap<Integer, BitField>();

    public static BitField getInstance(int mask) {
      BitField f = instances.get(Integer.valueOf(mask));
      if (f == null) {
        f = new BitField(mask);
        instances.put(Integer.valueOf(mask), f);
      }
      return f;
    }
}
