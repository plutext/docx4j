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

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;


/**
 * Program elements annotated &#64;Internal are intended for
 * POI internal use only. Such elements are not public by design
 * and likely to be removed in future versions of POI  or access
 * to such elements will be changed from 'public' to 'default' or less.
 *
 * @author Yegor Kozlov
 * @since POI-3.6
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {
    String value() default "";    
}
