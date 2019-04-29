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


package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.wordprocessingDrawing package. 
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

    private final static QName _Inline_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "inline");
    private final static QName _Anchor_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "anchor");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.wordprocessingDrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Inline }
     * 
     */
    public Inline createInline() {
        return new Inline();
    }

    /**
     * Create an instance of {@link Anchor }
     * 
     */
    public Anchor createAnchor() {
        return new Anchor();
    }

    /**
     * Create an instance of {@link CTEffectExtent }
     * 
     */
    public CTEffectExtent createCTEffectExtent() {
        return new CTEffectExtent();
    }

    /**
     * Create an instance of {@link CTWrapPath }
     * 
     */
    public CTWrapPath createCTWrapPath() {
        return new CTWrapPath();
    }

    /**
     * Create an instance of {@link CTWrapNone }
     * 
     */
    public CTWrapNone createCTWrapNone() {
        return new CTWrapNone();
    }

    /**
     * Create an instance of {@link CTWrapSquare }
     * 
     */
    public CTWrapSquare createCTWrapSquare() {
        return new CTWrapSquare();
    }

    /**
     * Create an instance of {@link CTWrapTight }
     * 
     */
    public CTWrapTight createCTWrapTight() {
        return new CTWrapTight();
    }

    /**
     * Create an instance of {@link CTWrapThrough }
     * 
     */
    public CTWrapThrough createCTWrapThrough() {
        return new CTWrapThrough();
    }

    /**
     * Create an instance of {@link CTWrapTopBottom }
     * 
     */
    public CTWrapTopBottom createCTWrapTopBottom() {
        return new CTWrapTopBottom();
    }

    /**
     * Create an instance of {@link CTPosH }
     * 
     */
    public CTPosH createCTPosH() {
        return new CTPosH();
    }

    /**
     * Create an instance of {@link CTPosV }
     * 
     */
    public CTPosV createCTPosV() {
        return new CTPosV();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Inline }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", name = "inline")
    public JAXBElement<Inline> createInline(Inline value) {
        return new JAXBElement<Inline>(_Inline_QNAME, Inline.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Anchor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", name = "anchor")
    public JAXBElement<Anchor> createAnchor(Anchor value) {
        return new JAXBElement<Anchor>(_Anchor_QNAME, Anchor.class, null, value);
    }

}
