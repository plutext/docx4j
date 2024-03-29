/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.dml.compatibility;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.compatibility package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LegacyDrawing_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/compatibility", "legacyDrawing");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.compatibility
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTCompat }
     * 
     */
    public CTCompat createCTCompat() {
        return new CTCompat();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCompat }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/compatibility", name = "legacyDrawing")
    public JAXBElement<CTCompat> createLegacyDrawing(CTCompat value) {
        return new JAXBElement<CTCompat>(_LegacyDrawing_QNAME, CTCompat.class, null, value);
    }

}
