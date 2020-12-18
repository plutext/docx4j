/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.docx4j.fonts.fop.util;

import org.apache.commons.logging.Log;
import org.docx4j.fonts.fop.apps.FOPException;

/**
 * Convenience Logging utility methods used in FOP
 */
public final class LogUtil {

    private LogUtil() {
    }

    /**
     * Convenience method that handles any error appropriately
     * @param log log
     * @param errorStr error string
     * @param strict validate strictly
     * @throws FOPException fop exception
     */
    public static void handleError(Log log, String errorStr, boolean strict) throws FOPException {
        handleException(log, new FOPException(errorStr), strict);
    }

    /**
     * Convenience method that handles any error appropriately
     * @param log log
     * @param e exception
     * @param strict validate strictly
     * @throws FOPException fop exception
     */
    public static void handleException(Log log, Exception e, boolean strict) throws FOPException {
        if (strict) {
            if (e instanceof FOPException) {
                throw (FOPException)e;
            }
            throw new FOPException(e);
        }
        log.error(e.getMessage());
    }
}
