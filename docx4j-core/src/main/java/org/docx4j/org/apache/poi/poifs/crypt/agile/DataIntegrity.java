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

import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.ENC_NS;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.getBinAttr;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.getTag;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.setBinAttr;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A complex type that specifies data used to verify whether the encrypted data passes an integrity check.
 * It MUST be generated using the method specified in section 2.3.4.14
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/dd924068(v=office.12).aspx">DataIntegrity Generation</a>
 */
public class DataIntegrity {
    /**
     * A base64-encoded value that specifies an encrypted key used in calculating the encryptedHmacValue.
     */
    private byte[] encryptedHmacKey;

    /**
     * A base64-encoded value that specifies an HMAC derived from encryptedHmacKey and the encrypted data.
     */
    private byte[] encryptedHmacValue;

    public DataIntegrity() {

    }

    public DataIntegrity(Element parent) {
        Element dataIntegrity = getTag(parent, ENC_NS, "dataIntegrity");
        if (dataIntegrity == null) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor");
        }
        encryptedHmacKey = getBinAttr(dataIntegrity, "encryptedHmacKey");
        encryptedHmacValue = getBinAttr(dataIntegrity, "encryptedHmacValue");
    }

    void write(Element encryption) {
        Document doc = encryption.getOwnerDocument();
        Element dataIntegrity = (Element)encryption.appendChild(doc.createElementNS(ENC_NS, "dataIntegrity"));
        setBinAttr(dataIntegrity, "encryptedHmacKey", encryptedHmacKey);
        setBinAttr(dataIntegrity, "encryptedHmacValue", encryptedHmacValue);
    }

    public byte[] getEncryptedHmacKey() {
        return encryptedHmacKey;
    }

    public void setEncryptedHmacKey(byte[] encryptedHmacKey) {
        this.encryptedHmacKey = encryptedHmacKey;
    }

    public byte[] getEncryptedHmacValue() {
        return encryptedHmacValue;
    }

    public void setEncryptedHmacValue(byte[] encryptedHmacValue) {
        this.encryptedHmacValue = encryptedHmacValue;
    }
}
