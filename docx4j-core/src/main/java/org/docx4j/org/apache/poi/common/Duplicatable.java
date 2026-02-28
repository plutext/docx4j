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

package org.docx4j.org.apache.poi.common;

/**
 * This is an alternative to the {@link Cloneable} interface without its side-effects.
 * A class implementing Duplicatable provides a deep-copy of itself - usually this is done via a copy-constructor,
 * which is invoked with a self-reference by the copy method.
 * References to child objects are duplicated - references to parents are kept as-is and
 * might need to be replaced by the parent copy operation.
 *
 * @see <a href="https://www.artima.com/intv/bloch.html#part13">Copy Constructor versus Cloning</a>
 */
public interface Duplicatable {
    // Providing a generics interface Duplicatable<T extends Duplicatable<T>> pushes too many
    // changes to the implementing classes and the benefit of providing a subtype-specific copy method
    // is not sufficient
    /**
     * @return a deep copy of the implementing class / instance
     */
    Duplicatable copy();
}
