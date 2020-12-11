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


package org.docx4j.dml.spreadsheetdrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.spreadsheetdrawing package. 
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

    private final static QName _From_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", "from");
    private final static QName _To_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", "to");
    private final static QName _WsDr_QNAME = new QName("http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", "wsDr");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.spreadsheetdrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTMarker }
     * 
     */
    public CTMarker createCTMarker() {
        return new CTMarker();
    }

    /**
     * Create an instance of {@link CTDrawing }
     * 
     */
    public CTDrawing createCTDrawing() {
        return new CTDrawing();
    }

    /**
     * Create an instance of {@link CTAnchorClientData }
     * 
     */
    public CTAnchorClientData createCTAnchorClientData() {
        return new CTAnchorClientData();
    }

    /**
     * Create an instance of {@link CTShapeNonVisual }
     * 
     */
    public CTShapeNonVisual createCTShapeNonVisual() {
        return new CTShapeNonVisual();
    }

    /**
     * Create an instance of {@link CTShape }
     * 
     */
    public CTShape createCTShape() {
        return new CTShape();
    }

    /**
     * Create an instance of {@link CTConnectorNonVisual }
     * 
     */
    public CTConnectorNonVisual createCTConnectorNonVisual() {
        return new CTConnectorNonVisual();
    }

    /**
     * Create an instance of {@link CTConnector }
     * 
     */
    public CTConnector createCTConnector() {
        return new CTConnector();
    }

    /**
     * Create an instance of {@link CTPictureNonVisual }
     * 
     */
    public CTPictureNonVisual createCTPictureNonVisual() {
        return new CTPictureNonVisual();
    }

    /**
     * Create an instance of {@link CTPicture }
     * 
     */
    public CTPicture createCTPicture() {
        return new CTPicture();
    }

    /**
     * Create an instance of {@link CTGraphicalObjectFrameNonVisual }
     * 
     */
    public CTGraphicalObjectFrameNonVisual createCTGraphicalObjectFrameNonVisual() {
        return new CTGraphicalObjectFrameNonVisual();
    }

    /**
     * Create an instance of {@link CTGraphicalObjectFrame }
     * 
     */
    public CTGraphicalObjectFrame createCTGraphicalObjectFrame() {
        return new CTGraphicalObjectFrame();
    }

    /**
     * Create an instance of {@link CTGroupShapeNonVisual }
     * 
     */
    public CTGroupShapeNonVisual createCTGroupShapeNonVisual() {
        return new CTGroupShapeNonVisual();
    }

    /**
     * Create an instance of {@link CTGroupShape }
     * 
     */
    public CTGroupShape createCTGroupShape() {
        return new CTGroupShape();
    }

    /**
     * Create an instance of {@link CTTwoCellAnchor }
     * 
     */
    public CTTwoCellAnchor createCTTwoCellAnchor() {
        return new CTTwoCellAnchor();
    }

    /**
     * Create an instance of {@link CTOneCellAnchor }
     * 
     */
    public CTOneCellAnchor createCTOneCellAnchor() {
        return new CTOneCellAnchor();
    }

    /**
     * Create an instance of {@link CTAbsoluteAnchor }
     * 
     */
    public CTAbsoluteAnchor createCTAbsoluteAnchor() {
        return new CTAbsoluteAnchor();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarker }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", name = "from")
    public JAXBElement<CTMarker> createFrom(CTMarker value) {
        return new JAXBElement<CTMarker>(_From_QNAME, CTMarker.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMarker }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", name = "to")
    public JAXBElement<CTMarker> createTo(CTMarker value) {
        return new JAXBElement<CTMarker>(_To_QNAME, CTMarker.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDrawing }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing", name = "wsDr")
    public JAXBElement<CTDrawing> createWsDr(CTDrawing value) {
        return new JAXBElement<CTDrawing>(_WsDr_QNAME, CTDrawing.class, null, value);
    }

}
