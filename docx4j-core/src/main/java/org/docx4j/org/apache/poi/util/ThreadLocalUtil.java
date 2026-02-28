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

import java.util.ArrayList;
import java.util.List;

/**
 * Small utility to allow to remove references held in ThreadLocals.
 *
 * This is sometimes necessary, e.g. when returning threads into a global
 * thread pool.
 *
 * For each usage of ThreadLocal, a cleaner is registered via
 * registerCleaner().
 */
public class ThreadLocalUtil {
    private final static List<Runnable> registeredCleaners = new ArrayList<>();

    private ThreadLocalUtil() {
    }

    /**
     * Clear {@link ThreadLocal}s of the current thread.
     *
     * This can be used to clean out a thread before "returning"
     * it to a thread-pool or a Web-Container like Tomcat.
     *
     * Usually org.apache.xmlbeans.ThreadLocalUtil#clearAllThreadLocals()
     * should be called as well to clear out some more ThreadLocals which
     * are created by the XMLBeans library internally.
     */
    public static void clearAllThreadLocals() {
        // run all registered cleaners
        registeredCleaners.forEach(Runnable::run);
    }

    /**
     * Intended for internal use only so other modules of Apache POi
     * can add cleaners.
     *
     * @param cleaner a runnable which clears some thread-local that is
     *                located outside of the "poi" module.
     */
    @Internal
    public static void registerCleaner(Runnable cleaner) {
        registeredCleaners.add(cleaner);
    }
}
