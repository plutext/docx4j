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
package org.docx4j.org.apache.xml.security.signature;

import org.docx4j.org.apache.xml.security.exceptions.XMLSecurityException;

/**
 * All XML Signature related exceptions inherit herefrom.
 *
 * @see MissingResourceFailureException InvalidDigestValueException InvalidSignatureValueException
 * @author Christian Geuer-Pollmann
 */
public class XMLSignatureException extends XMLSecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor XMLSignatureException
     *
     */
    public XMLSignatureException() {
        super();
    }
    
    public XMLSignatureException(Exception ex) {
        super(ex);
    }

    /**
     * Constructor XMLSignatureException
     *
     * @param msgID
     */
    public XMLSignatureException(String msgID) {
        super(msgID);
    }

    /**
     * Constructor XMLSignatureException
     *
     * @param msgID
     * @param exArgs
     */
    public XMLSignatureException(String msgID, Object exArgs[]) {
        super(msgID, exArgs);
    }

    /**
     * Constructor XMLSignatureException
     *
     * @param originalException
     * @param msgID
     */
    public XMLSignatureException(Exception originalException, String msgID) {
        super(originalException, msgID);
    }

    /**
     * Constructor XMLSignatureException
     *
     * @param originalException
     * @param msgID
     * @param exArgs
     */
    public XMLSignatureException(Exception originalException, String msgID, Object exArgs[]) {
        super(originalException, msgID, exArgs);
    }
}
