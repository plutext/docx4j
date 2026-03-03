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

package org.docx4j.org.apache.poi.poifs.crypt.agile;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A sequence of KeyEncryptor elements. Exactly one KeyEncryptors element MUST be present, and the KeyEncryptors element
 * MUST contain at least one KeyEncryptor.
 */
public class KeyEncryptor {
    static final String PASS_NS = "http://schemas.microsoft.com/office/2006/keyEncryptor/password";
    static final String CERT_NS = "http://schemas.microsoft.com/office/2006/keyEncryptor/certificate";

    private PasswordKeyEncryptor passwordKeyEncryptor;
    private CertificateKeyEncryptor certificateKeyEncryptor;

    public KeyEncryptor() {

    }

    public KeyEncryptor(Element keyEncryptor) {
        if (keyEncryptor == null) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor");
        }
        NodeList nl = keyEncryptor.getElementsByTagNameNS("*", "encryptedKey");
        // usually only one encryptor is set, so iterate and overwrite the encryptor members
        for (int i=0; i<nl.getLength(); i++) {
            Element el = (Element)nl.item(i);
            String nsUri = el.getNamespaceURI();
            if (PASS_NS.equals(nsUri)) {
                passwordKeyEncryptor = new PasswordKeyEncryptor(el);
            } else if (CERT_NS.equals(nsUri)) {
                certificateKeyEncryptor = new CertificateKeyEncryptor(el);
            }
        }
    }

    void write(Element keyEncryptors) {
        if (passwordKeyEncryptor != null) {
            passwordKeyEncryptor.write(keyEncryptors);
        } else if (certificateKeyEncryptor != null) {
            certificateKeyEncryptor.write(keyEncryptors);
        }
    }



    public PasswordKeyEncryptor getPasswordKeyEncryptor() {
        return passwordKeyEncryptor;
    }

    public void setPasswordKeyEncryptor(PasswordKeyEncryptor passwordKeyEncryptor) {
        this.passwordKeyEncryptor = passwordKeyEncryptor;
    }

    public CertificateKeyEncryptor getCertificateKeyEncryptor() {
        return certificateKeyEncryptor;
    }

    public void setCertificateKeyEncryptor(CertificateKeyEncryptor certificateKeyEncryptor) {
        this.certificateKeyEncryptor = certificateKeyEncryptor;
    }
}
