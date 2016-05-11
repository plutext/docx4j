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

package org.docx4j.org.apache.poi.hpsf;

/**
 * <p>This exception is thrown when there is an illegal value set in a
 * {@link PropertySet}. For example, a {@link Variant#VT_BOOL} must
 * have a value of <code>-1 (true)</code> or <code>0 (false)</code>.
 * Any other value would trigger this exception. It supports a nested
 * "reason" throwable, i.e. an exception that caused this one to be
 * thrown.</p>
 *
 * @author Drew Varner(Drew.Varner atDomain sc.edu)
 */
public class IllegalPropertySetDataException extends HPSFRuntimeException
{

    /**
     * <p>Constructor</p>
     */
    public IllegalPropertySetDataException()
    {
        super();
    }



    /**
     * <p>Constructor</p>
     * 
     * @param msg The exception's message string
     */
    public IllegalPropertySetDataException(final String msg)
    {
        super(msg);
    }



    /**
     * <p>Constructor</p>
     * 
     * @param reason This exception's underlying reason
     */
    public IllegalPropertySetDataException(final Throwable reason)
    {
        super(reason);
    }



    /**
     * <p>Constructor</p>
     * 
     * @param msg The exception's message string
     * @param reason This exception's underlying reason
     */
    public IllegalPropertySetDataException(final String msg,
                                           final Throwable reason)
    {
        super(msg, reason);
    }

}
