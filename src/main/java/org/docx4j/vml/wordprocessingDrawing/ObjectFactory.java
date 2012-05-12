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


package org.docx4j.vml.wordprocessingDrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.vml.wordprocessingDrawing package. 
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

    private final static QName _Bordertop_QNAME = new QName("urn:schemas-microsoft-com:office:word", "bordertop");
    private final static QName _Anchorlock_QNAME = new QName("urn:schemas-microsoft-com:office:word", "anchorlock");
    private final static QName _Wrap_QNAME = new QName("urn:schemas-microsoft-com:office:word", "wrap");
    private final static QName _Borderleft_QNAME = new QName("urn:schemas-microsoft-com:office:word", "borderleft");
    private final static QName _Borderbottom_QNAME = new QName("urn:schemas-microsoft-com:office:word", "borderbottom");
    private final static QName _Borderright_QNAME = new QName("urn:schemas-microsoft-com:office:word", "borderright");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.vml.wordprocessingDrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTBorder }
     * 
     */
    public CTBorder createCTBorder() {
        return new CTBorder();
    }

    /**
     * Create an instance of {@link CTWrap }
     * 
     */
    public CTWrap createCTWrap() {
        return new CTWrap();
    }

    /**
     * Create an instance of {@link CTAnchorLock }
     * 
     */
    public CTAnchorLock createCTAnchorLock() {
        return new CTAnchorLock();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBorder }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:word", name = "bordertop")
    public JAXBElement<CTBorder> createBordertop(CTBorder value) {
        return new JAXBElement<CTBorder>(_Bordertop_QNAME, CTBorder.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTAnchorLock }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:word", name = "anchorlock")
    public JAXBElement<CTAnchorLock> createAnchorlock(CTAnchorLock value) {
        return new JAXBElement<CTAnchorLock>(_Anchorlock_QNAME, CTAnchorLock.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWrap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:word", name = "wrap")
    public JAXBElement<CTWrap> createWrap(CTWrap value) {
        return new JAXBElement<CTWrap>(_Wrap_QNAME, CTWrap.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBorder }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:word", name = "borderleft")
    public JAXBElement<CTBorder> createBorderleft(CTBorder value) {
        return new JAXBElement<CTBorder>(_Borderleft_QNAME, CTBorder.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBorder }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:word", name = "borderbottom")
    public JAXBElement<CTBorder> createBorderbottom(CTBorder value) {
        return new JAXBElement<CTBorder>(_Borderbottom_QNAME, CTBorder.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBorder }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:word", name = "borderright")
    public JAXBElement<CTBorder> createBorderright(CTBorder value) {
        return new JAXBElement<CTBorder>(_Borderright_QNAME, CTBorder.class, null, value);
    }

}
