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
package org.docx4j.org.apache.xml.security.utils;

import org.docx4j.org.apache.xml.security.exceptions.XMLSecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class SignatureElementProxy
 *
 * @author Brent Putman (putmanb@georgetown.edu)
 */
public abstract class Signature11ElementProxy extends ElementProxy {
    
    protected Signature11ElementProxy() {
    }
    
    /**
     * Constructor Signature11ElementProxy
     *
     * @param doc
     */
    public Signature11ElementProxy(Document doc) {
        if (doc == null) {
            throw new RuntimeException("Document is null");
        }

        setDocument(doc);
        setElement(XMLUtils.createElementInSignature11Space(doc, this.getBaseLocalName()));
    }

    /**
     * Constructor Signature11ElementProxy
     *
     * @param element
     * @param baseURI
     * @throws XMLSecurityException
     */
    public Signature11ElementProxy(Element element, String baseURI) throws XMLSecurityException {
        super(element, baseURI);

    }

    /** @inheritDoc */
    public String getBaseNamespace() {
        return Constants.SignatureSpec11NS;
    }
}
