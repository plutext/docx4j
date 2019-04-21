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


package org.docx4j.vml.presentationDrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.vml.presentationDrawing package. 
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

    private final static QName _Textdata_QNAME = new QName("urn:schemas-microsoft-com:office:powerpoint", "textdata");
    private final static QName _Iscomment_QNAME = new QName("urn:schemas-microsoft-com:office:powerpoint", "iscomment");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.vml.presentationDrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTRel }
     * 
     */
    public CTRel createCTRel() {
        return new CTRel();
    }

    /**
     * Create an instance of {@link CTEmpty }
     * 
     */
    public CTEmpty createCTEmpty() {
        return new CTEmpty();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTRel }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:powerpoint", name = "textdata")
    public JAXBElement<CTRel> createTextdata(CTRel value) {
        return new JAXBElement<CTRel>(_Textdata_QNAME, CTRel.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTEmpty }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:powerpoint", name = "iscomment")
    public JAXBElement<CTEmpty> createIscomment(CTEmpty value) {
        return new JAXBElement<CTEmpty>(_Iscomment_QNAME, CTEmpty.class, null, value);
    }

}
