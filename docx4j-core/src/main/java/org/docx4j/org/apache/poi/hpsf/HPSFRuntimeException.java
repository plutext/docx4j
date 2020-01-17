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
 * <p>This exception is the superclass of all other unchecked
 * exceptions thrown in this package. It supports a nested "reason"
 * throwable, i.e. an exception that caused this one to be thrown.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class HPSFRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = -7804271670232727159L;
	/** <p>The underlying reason for this exception - may be
     * <code>null</code>.</p> */
    private Throwable reason;



    /**
     * <p>Creates a new {@link HPSFRuntimeException}.</p>
     */
    public HPSFRuntimeException()
    {
        super();
    }



    /**
     * <p>Creates a new {@link HPSFRuntimeException} with a message
     * string.</p>
     *
     * @param msg The message string.
     */
    public HPSFRuntimeException(final String msg)
    {
        super(msg);
    }



    /**
     * <p>Creates a new {@link HPSFRuntimeException} with a
     * reason.</p>
     *
     * @param reason The reason, i.e. a throwable that indirectly
     * caused this exception.
     */
    public HPSFRuntimeException(final Throwable reason)
    {
        super();
        this.reason = reason;
    }



    /**
     * <p>Creates a new {@link HPSFRuntimeException} with a message
     * string and a reason.</p>
     *
     * @param msg The message string.
     * @param reason The reason, i.e. a throwable that indirectly
     * caused this exception.
     */
    public HPSFRuntimeException(final String msg, final Throwable reason)
    {
        super(msg);
        this.reason = reason;
    }



    /**
     * <p>Returns the {@link Throwable} that caused this exception to
     * be thrown or <code>null</code> if there was no such {@link
     * Throwable}.</p>
     *
     * @return The reason
     */
    public Throwable getReason()
    {
        return reason;
    }



//    /**
//     * @see Throwable#printStackTrace()
//     */
//    public void printStackTrace()
//    {
//        printStackTrace(System.err);
//    }



//    /**
//     * @see Throwable#printStackTrace(java.io.PrintStream)
//     */
//    public void printStackTrace(final PrintStream p)
//    {
//        final Throwable reason = getReason();
//        super.printStackTrace(p);
//        if (reason != null)
//        {
//            p.println("Caused by:");
//            reason.printStackTrace(p);
//        }
//    }



//    /**
//     * @see Throwable#printStackTrace(java.io.PrintWriter)
//     */
//    public void printStackTrace(final PrintWriter p)
//    {
//        final Throwable reason = getReason();
//        super.printStackTrace(p);
//        if (reason != null)
//        {
//            p.println("Caused by:");
//            reason.printStackTrace(p);
//        }
//    }

}
