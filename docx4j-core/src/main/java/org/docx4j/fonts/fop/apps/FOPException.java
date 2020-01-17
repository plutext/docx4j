/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

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

/* $Id: FOPException.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.apps;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Exception thrown when FOP has a problem.
 */
public class FOPException extends SAXException {

    private static final String EXCEPTION_SEPARATOR = "\n---------\n";

    private String systemId;
    private int line;
    private int column;

    private String localizedMessage;

    /**
     * Constructs a new FOP exception with the specified detail message.
     * @param message the detail message.
     */
    public FOPException(String message) {
        super(message);
    }

    /**
     * Constructs a new FOP exception with the specified detail message and location.
     * @param message the detail message
     * @param systemId the system id of the FO document which is associated with the exception
     *                 may be null.
     * @param line line number in the FO document which is associated with the exception.
     * @param column clolumn number in the line which is associated with the exception.
     */
    public FOPException(String message, String systemId, int line, int column) {
        super(message);
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    /**
     * Constructs a new FOP exception with the specified detail message and location.
     * @param message the detail message.
     * @param locator the locator holding the location.
     */
    public FOPException(String message, Locator locator) {
        super(message);
        setLocator(locator);
    }


    /**
     * Constructs a new FOP exception with the specified cause.
     * @param cause the cause.
     */
    public FOPException(Exception cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message  the detail message
     * @param cause the cause
     */
    public FOPException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Set a location associated with the exception.
     * @param locator the locator holding the location.
     */
    public void setLocator(Locator locator) {
        if (locator != null) {
            this.systemId = locator.getSystemId();
            this.line = locator.getLineNumber();
            this.column = locator.getColumnNumber();
        }
    }

    /**
     * Set a location associated with the exception.
     * @param systemId the system id of the FO document which is associated with the exception;
     *                 may be null.
     * @param line line number in the FO document which is associated with the exception.
     * @param column column number in the line which is associated with the exception.
     */
    public void setLocation(String systemId, int line, int column) {
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    /**
     * Indicate whether a location was set.
     * @return whether a location was set
     */
    public boolean isLocationSet() {
        // TODO: this is actually a dangerous assumption: A line
        // number of 0 or -1 might be used to indicate an unknown line
        // number, while the system ID might still be of use.
        return line > 0;
    }

    /**
     * Returns the detail message string of this FOP exception.
     * If a location was set, the message is prepended with it in the
     * form
     * <pre>
     *  SystemId:LL:CC: &amp;the message&amp;
     * </pre>
     * (the format used by most GNU tools)
     * @return the detail message string of this FOP exception
     */
    public String getMessage() {
        if (isLocationSet()) {
            return systemId + ":" + line + ":" + column + ": " + super.getMessage();
        } else {
            return super.getMessage();
        }
    }

    /**
     * Attempts to recast the exception as other Throwable types.
     * @return the exception recast as another type if possible, otherwise null.
     */
    protected Throwable getRootException() {
        Throwable result = getException();

        if (result instanceof SAXException) {
            result = ((SAXException)result).getException();
        }
        if (result instanceof java.lang.reflect.InvocationTargetException) {
            result
                = ((java.lang.reflect.InvocationTargetException)result).getTargetException();
        }
        if (result != getException()) {
            return result;
        }
        return null;
    }

    /**
     * Prints this FOP exception and its backtrace to the standard error stream.
     */
    public void printStackTrace() {
        synchronized (System.err) {
            super.printStackTrace();
            if (getException() != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                getException().printStackTrace();
            }
            if (getRootException() != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace();
            }
        }
    }

    /**
     * Prints this FOP exception and its backtrace to the specified print stream.
     * @param stream PrintStream to use for output
     */
    public void printStackTrace(java.io.PrintStream stream) {
        synchronized (stream) {
            super.printStackTrace(stream);
            if (getException() != null) {
                stream.println(EXCEPTION_SEPARATOR);
                getException().printStackTrace(stream);
            }
            if (getRootException() != null) {
                stream.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace(stream);
            }
        }
    }

    /**
     * Prints this FOP exception and its backtrace to the specified print writer.
     * @param writer PrintWriter to use for output
     */
    public void printStackTrace(java.io.PrintWriter writer) {
        synchronized (writer) {
            super.printStackTrace(writer);
            if (getException() != null) {
                writer.println(EXCEPTION_SEPARATOR);
                getException().printStackTrace(writer);
            }
            if (getRootException() != null) {
                writer.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace(writer);
            }
        }
    }

    /**
     * Sets the localized message for this exception.
     * @param msg the localized message
     */
    public void setLocalizedMessage(String msg) {
        this.localizedMessage = msg;
    }

    /** {@inheritDoc} */
    public String getLocalizedMessage() {
        if (this.localizedMessage != null) {
            return this.localizedMessage;
        } else {
            return super.getLocalizedMessage();
        }
    }



}
