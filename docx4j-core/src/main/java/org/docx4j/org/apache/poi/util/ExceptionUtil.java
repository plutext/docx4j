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
 * Utility methods for dealing with exceptions/throwables
 *
 * @since 5.2.4
 */
public class ExceptionUtil {
    private ExceptionUtil() {}

    /**
     * It is important never to catch all <code>Throwable</code>s. Some like
     * {@link InterruptedException} should be rethrown. Based on
     * <a href="https://www.scala-lang.org/api/2.13.10/scala/util/control/NonFatal$.html">scala.util.control.NonFatal</a>.
     *
     * @param throwable to check
     * @return whether the <code>Throwable</code> is a fatal error
     */
    public static boolean isFatal(Throwable throwable) {
        //similar to https://www.scala-lang.org/api/2.13.8/scala/util/control/NonFatal$.html
        return (throwable instanceof VirtualMachineError
                || throwable instanceof ThreadDeath
                || throwable instanceof InterruptedException
                || throwable instanceof  LinkageError);
    }

    /**
     * Designed to be used in conjunction with {@link #isFatal(Throwable)}.
     * This method should be used with care.
     * <p>
     *     The input throwable is thrown if it is an <code>Error</code> or a <code>RuntimeException</code>.
     *     Otherwise, the method wraps the throwable in a RuntimeException and rethrows that.
     * </p>
     *
     * @param throwable to check
     * @throws Error the input throwable if it is an <code>Error</code>.
     * @throws RuntimeException the input throwable if it is an <code>RuntimeException</code>
     * Otherwise wraps the throwable in a RuntimeException.
     */
    public static void rethrow(Throwable throwable) throws Error, RuntimeException {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        throw new RuntimeException(throwable);
    }
}
