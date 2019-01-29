/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.docx4j.org.apache.xml.security.exceptions;

import java.text.MessageFormat;

import org.docx4j.org.apache.xml.security.utils.I18n;

/**
 * The mother of all Exceptions in this bundle. It allows exceptions to have
 * their messages translated to the different locales.
 *
 * The <code>xmlsecurity_en.properties</code> file contains this line:
 * <pre>
 * xml.WrongElement = Can't create a {0} from a {1} element
 * </pre>
 *
 * Usage in the Java source is:
 * <pre>
 * {
 *    Object exArgs[] = { Constants._TAG_TRANSFORMS, "BadElement" };
 *
 *    throw new XMLSecurityException("xml.WrongElement", exArgs);
 * }
 * </pre>
 *
 * Additionally, if another Exception has been caught, we can supply it, too>
 * <pre>
 * try {
 *    ...
 * } catch (Exception oldEx) {
 *    Object exArgs[] = { Constants._TAG_TRANSFORMS, "BadElement" };
 *
 *    throw new XMLSecurityException("xml.WrongElement", exArgs, oldEx);
 * }
 * </pre>
 *
 *
 * @author Christian Geuer-Pollmann
 */
public class XMLSecurityException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** Field msgID */
    protected String msgID;

    /**
     * Constructor XMLSecurityException
     *
     */
    public XMLSecurityException() {
        super("Missing message string");

        this.msgID = null;
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     */
    public XMLSecurityException(String msgID) {
        super(I18n.getExceptionMessage(msgID));

        this.msgID = msgID;
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     * @param exArgs
     */
    public XMLSecurityException(String msgID, Object exArgs[]) {

        super(MessageFormat.format(I18n.getExceptionMessage(msgID), exArgs));

        this.msgID = msgID;
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param originalException
     */
    public XMLSecurityException(Exception originalException) {

        super(originalException.getMessage(), originalException);
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     * @param originalException
     */
    public XMLSecurityException(Exception originalException, String msgID) {
        super(I18n.getExceptionMessage(msgID, originalException), originalException);

        this.msgID = msgID;
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     * @param exArgs
     * @param originalException
     */
    public XMLSecurityException(Exception originalException, String msgID, Object exArgs[]) {
        super(MessageFormat.format(I18n.getExceptionMessage(msgID), exArgs), originalException);

        this.msgID = msgID;
    }

    /**
     * Method getMsgID
     *
     * @return the messageId
     */
    public String getMsgID() {
        if (msgID == null) {
            return "Missing message ID";
        } 
        return msgID;      
    }

    /** @inheritDoc */
    public String toString() {
        String s = this.getClass().getName();
        String message = super.getLocalizedMessage();

        if (message != null) {
            message = s + ": " + message;
        } else {
            message = s;
        }

        if (super.getCause() != null) {
            message = message + "\nOriginal Exception was " + super.getCause().toString();
        }

        return message;
    }

    /**
     * Method printStackTrace
     *
     */
    public void printStackTrace() {
        synchronized (System.err) {
            super.printStackTrace(System.err);
        }
    }

    /**
     * Method getOriginalException
     *
     * @return the original exception
     */
    public Exception getOriginalException() {
        if (this.getCause() instanceof Exception) {
            return (Exception)this.getCause();
        }
        return null;
    }
}
