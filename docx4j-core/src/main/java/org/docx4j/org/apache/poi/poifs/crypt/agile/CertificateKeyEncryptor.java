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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.*;

public class CertificateKeyEncryptor {

    /**
     * A base64-encoded value that specifies the encrypted form of the intermediate key,
     * which is encrypted with the public key contained within the X509Certificate attribute.
     */
    private byte[] encryptedKeyValue;

    /**
     * A base64-encoded value that specifies a DER-encoded X.509 certificate (1) used to encrypt the intermediate key.
     * The certificate (1) MUST contain only the public portion of the public-private key pair.
     */
    private byte[] x509Certificate;

    /**
     * A base64-encoded value that specifies the HMAC of the binary data obtained by base64-decoding the X509Certificate
     * attribute. The hashing algorithm used to derive the HMAC MUST be the hashing algorithm specified for the
     * Encryption.keyData element. The secret key used to derive the HMAC MUST be the intermediate key. If the
     * intermediate key is reset, any CertificateKeyEncryptor elements are also reset to contain the new intermediate
     * key, except that the certVerifier attribute MUST match the value calculated using the current intermediate key,
     * to verify that the CertificateKeyEncryptor element actually encrypted the current intermediate key. If a
     * CertificateKeyEncryptor element does not have a correct certVerifier attribute, it MUST be discarded.
     */
    private byte[] certVerifier;

    public CertificateKeyEncryptor(Element certificateKey) {
        if (certificateKey == null) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor");
        }
        encryptedKeyValue = getBinAttr(certificateKey, "encryptedKeyValue");
        x509Certificate = getBinAttr(certificateKey, "X509Certificate");
        certVerifier = getBinAttr(certificateKey, "certVerifier");
    }

    void write(Element encryption) {
        Document doc = encryption.getOwnerDocument();
        Element keyEncryptor = (Element) encryption.appendChild(doc.createElementNS(ENC_NS, "keyEncryptor"));
        keyEncryptor.setAttribute("uri", KeyEncryptor.CERT_NS);
        Element encryptedKey = (Element) keyEncryptor.appendChild(doc.createElementNS(KeyEncryptor.CERT_NS, "c:encryptedKey"));

        setBinAttr(encryptedKey, "encryptedKeyValue", encryptedKeyValue);
        setBinAttr(encryptedKey, "x509Certificate", x509Certificate);
        setBinAttr(encryptedKey, "certVerifier", certVerifier);
    }

    public byte[] getEncryptedKeyValue() {
        return encryptedKeyValue;
    }

    public void setEncryptedKeyValue(byte[] encryptedKeyValue) {
        this.encryptedKeyValue = encryptedKeyValue;
    }

    public byte[] getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(byte[] x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public byte[] getCertVerifier() {
        return certVerifier;
    }

    public void setCertVerifier(byte[] certVerifier) {
        this.certVerifier = certVerifier;
    }
}
