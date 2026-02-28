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

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.xml.XMLConstants;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class EncryptionDocument {
    static final String ENC_NS = "http://schemas.microsoft.com/office/2006/encryption";

    private KeyData keyData;

    /**
     * All ECMA-376 documents [ECMA-376] encrypted by Microsoft Office using agile encryption will have a DataIntegrity
     * element present. The schema allows for a DataIntegrity element to not be present because the encryption schema
     * can be used by applications that do not create ECMA-376 documents [ECMA-376].
     */
    private DataIntegrity dataIntegrity;

    private final List<KeyEncryptor> keyEncryptors = new ArrayList<>();

    public EncryptionDocument() {

    }

    public void parse(Document doc) {
        Element encryption = doc.getDocumentElement();
        if (!ENC_NS.equals(encryption.getNamespaceURI()) || !"encryption".equals(encryption.getLocalName())) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor");
        }
        keyData = new KeyData(encryption);
        dataIntegrity = new DataIntegrity(encryption);

        // The KeyEncryptor element, which MUST be used when encrypting password-protected agile encryption documents,
        // is either a PasswordKeyEncryptor or a CertificateKeyEncryptor. Exactly one PasswordKeyEncryptor MUST be
        // present. Zero or more CertificateKeyEncryptor elements are contained within the KeyEncryptors element.
        Element keyEncryptors = getTag(encryption, ENC_NS, "keyEncryptors");
        if (keyEncryptors == null) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor");
        }
        NodeList ke = keyEncryptors.getElementsByTagNameNS(ENC_NS, "keyEncryptor");
        for (int i=0; i<ke.getLength(); i++) {
            this.keyEncryptors.add(new KeyEncryptor((Element)ke.item(i)));
        }
    }

    public void write(Document doc) {
        doc.setXmlStandalone(true);
        Element encryption = (Element)doc.appendChild(doc.createElementNS(ENC_NS, "encryption"));
        if (keyData != null) {
            keyData.write(encryption);
        }
        if (dataIntegrity != null) {
            dataIntegrity.write(encryption);
        }
        Element keyEncryptors = (Element)encryption.appendChild(doc.createElementNS(ENC_NS, "keyEncryptors"));
        boolean hasPass = false;
        boolean hasCert = false;
        for (KeyEncryptor ke : this.keyEncryptors) {
            ke.write(keyEncryptors);
            hasPass |= ke.getPasswordKeyEncryptor() != null;
            hasCert |= ke.getCertificateKeyEncryptor() != null;
        }
        if (hasPass) {
            encryption.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:p", KeyEncryptor.PASS_NS);
        }
        if (hasCert) {
            encryption.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:c", KeyEncryptor.CERT_NS);
        }

    }


    public KeyData getKeyData() {
        return keyData;
    }

    public void setKeyData(KeyData keyData) {
        this.keyData = keyData;
    }

    public DataIntegrity getDataIntegrity() {
        return dataIntegrity;
    }

    public void setDataIntegrity(DataIntegrity dataIntegrity) {
        this.dataIntegrity = dataIntegrity;
    }

    public List<KeyEncryptor> getKeyEncryptors() {
        return keyEncryptors;
    }

    static Element getTag(Element el, String ns, String name) {
        if (el == null) {
            return null;
        }
        NodeList nl = el.getElementsByTagNameNS(ns, name);
        return (nl.getLength() > 0) ? (Element)nl.item(0) : null;
    }

    static Integer getIntAttr(Element el, String name) {
        String at = el.getAttribute(name);
        return (at.isEmpty()) ? null : Integer.valueOf(at);
    }

    static byte[] getBinAttr(Element el, String name) {
        String at = el.getAttribute(name);
        return (at.isEmpty()) ? null : Base64.getDecoder().decode(at);
    }

    static void setIntAttr(Element el, String name, Integer val) {
        setAttr(el, name, val == null ? null : val.toString());
    }

    static void setAttr(Element el, String name, String val) {
        if (val != null) {
            el.setAttribute(name, val);
        }
    }

    static void setBinAttr(Element el, String name, byte[] val) {
        if (val != null) {
            setAttr(el, name, Base64.getEncoder().encodeToString(val));
        }
    }
}
