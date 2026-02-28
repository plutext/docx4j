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
package org.docx4j.org.apache.poi.hssf.record.crypto;

import org.docx4j.org.apache.poi.util.ThreadLocalUtil;

public final class Biff8EncryptionKey {
    /**
     * Stores the BIFF8 encryption/decryption password for the current thread.  This has been done
     * using a {@link ThreadLocal} in order to avoid further overloading the various public APIs
     * (e.g. {@link HSSFWorkbook}) that need this functionality.
     */
    private static final ThreadLocal<String> _userPasswordTLS = new ThreadLocal<>();
    static {
        // allow to clear all thread-locals via ThreadLocalUtil
        ThreadLocalUtil.registerCleaner(_userPasswordTLS::remove);
    }

    /**
     * Sets the BIFF8 encryption/decryption password for the current thread.
     *
     * @param password pass <code>null</code> to clear user password (and use default)
     */
    public static void setCurrentUserPassword(String password) {
        if (password == null) {
            _userPasswordTLS.remove();
        } else {
            _userPasswordTLS.set(password);
        }
    }

    /**
     * @return the BIFF8 encryption/decryption password for the current thread.
     * <code>null</code> if it is currently unset.
     */
    public static String getCurrentUserPassword() {
        return _userPasswordTLS.get();
    }
}
