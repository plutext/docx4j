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


package org.docx4j.vml.officedrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.vml.officedrawing package. 
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

    private final static QName _Lock_QNAME = new QName("urn:schemas-microsoft-com:office:office", "lock");
    private final static QName _Signatureline_QNAME = new QName("urn:schemas-microsoft-com:office:office", "signatureline");
    private final static QName _Callout_QNAME = new QName("urn:schemas-microsoft-com:office:office", "callout");
    private final static QName _Fill_QNAME = new QName("urn:schemas-microsoft-com:office:office", "fill");
    private final static QName _OLEObject_QNAME = new QName("urn:schemas-microsoft-com:office:office", "OLEObject");
    private final static QName _Skew_QNAME = new QName("urn:schemas-microsoft-com:office:office", "skew");
    private final static QName _Clippath_QNAME = new QName("urn:schemas-microsoft-com:office:office", "clippath");
    private final static QName _Complex_QNAME = new QName("urn:schemas-microsoft-com:office:office", "complex");
    private final static QName _Left_QNAME = new QName("urn:schemas-microsoft-com:office:office", "left");
    private final static QName _Shapedefaults_QNAME = new QName("urn:schemas-microsoft-com:office:office", "shapedefaults");
    private final static QName _Top_QNAME = new QName("urn:schemas-microsoft-com:office:office", "top");
    private final static QName _Ink_QNAME = new QName("urn:schemas-microsoft-com:office:office", "ink");
    private final static QName _Bottom_QNAME = new QName("urn:schemas-microsoft-com:office:office", "bottom");
    private final static QName _Column_QNAME = new QName("urn:schemas-microsoft-com:office:office", "column");
    private final static QName _Extrusion_QNAME = new QName("urn:schemas-microsoft-com:office:office", "extrusion");
    private final static QName _Diagram_QNAME = new QName("urn:schemas-microsoft-com:office:office", "diagram");
    private final static QName _Shapelayout_QNAME = new QName("urn:schemas-microsoft-com:office:office", "shapelayout");
    private final static QName _Right_QNAME = new QName("urn:schemas-microsoft-com:office:office", "right");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.vml.officedrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTProxy }
     * 
     */
    public CTProxy createCTProxy() {
        return new CTProxy();
    }

    /**
     * Create an instance of {@link CTOLEObject }
     * 
     */
    public CTOLEObject createCTOLEObject() {
        return new CTOLEObject();
    }

    /**
     * Create an instance of {@link CTRelation }
     * 
     */
    public CTRelation createCTRelation() {
        return new CTRelation();
    }

    /**
     * Create an instance of {@link CTInk }
     * 
     */
    public CTInk createCTInk() {
        return new CTInk();
    }

    /**
     * Create an instance of {@link CTRegroupTable }
     * 
     */
    public CTRegroupTable createCTRegroupTable() {
        return new CTRegroupTable();
    }

    /**
     * Create an instance of {@link CTRelationTable }
     * 
     */
    public CTRelationTable createCTRelationTable() {
        return new CTRelationTable();
    }

    /**
     * Create an instance of {@link CTR }
     * 
     */
    public CTR createCTR() {
        return new CTR();
    }

    /**
     * Create an instance of {@link CTIdMap }
     * 
     */
    public CTIdMap createCTIdMap() {
        return new CTIdMap();
    }

    /**
     * Create an instance of {@link CTColorMenu }
     * 
     */
    public CTColorMenu createCTColorMenu() {
        return new CTColorMenu();
    }

    /**
     * Create an instance of {@link CTColorMru }
     * 
     */
    public CTColorMru createCTColorMru() {
        return new CTColorMru();
    }

    /**
     * Create an instance of {@link CTComplex }
     * 
     */
    public CTComplex createCTComplex() {
        return new CTComplex();
    }

    /**
     * Create an instance of {@link CTSignatureLine }
     * 
     */
    public CTSignatureLine createCTSignatureLine() {
        return new CTSignatureLine();
    }

    /**
     * Create an instance of {@link CTLock }
     * 
     */
    public CTLock createCTLock() {
        return new CTLock();
    }

    /**
     * Create an instance of {@link CTShapeDefaults }
     * 
     */
    public CTShapeDefaults createCTShapeDefaults() {
        return new CTShapeDefaults();
    }

    /**
     * Create an instance of {@link CTClipPath }
     * 
     */
    public CTClipPath createCTClipPath() {
        return new CTClipPath();
    }

    /**
     * Create an instance of {@link CTRules }
     * 
     */
    public CTRules createCTRules() {
        return new CTRules();
    }

    /**
     * Create an instance of {@link CTEntry }
     * 
     */
    public CTEntry createCTEntry() {
        return new CTEntry();
    }

    /**
     * Create an instance of {@link CTCallout }
     * 
     */
    public CTCallout createCTCallout() {
        return new CTCallout();
    }

    /**
     * Create an instance of {@link CTFill }
     * 
     */
    public CTFill createCTFill() {
        return new CTFill();
    }

    /**
     * Create an instance of {@link CTExtrusion }
     * 
     */
    public CTExtrusion createCTExtrusion() {
        return new CTExtrusion();
    }

    /**
     * Create an instance of {@link CTStrokeChild }
     * 
     */
    public CTStrokeChild createCTStrokeChild() {
        return new CTStrokeChild();
    }

    /**
     * Create an instance of {@link CTShapeLayout }
     * 
     */
    public CTShapeLayout createCTShapeLayout() {
        return new CTShapeLayout();
    }

    /**
     * Create an instance of {@link CTSkew }
     * 
     */
    public CTSkew createCTSkew() {
        return new CTSkew();
    }

    /**
     * Create an instance of {@link CTDiagram }
     * 
     */
    public CTDiagram createCTDiagram() {
        return new CTDiagram();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTLock }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "lock")
    public JAXBElement<CTLock> createLock(CTLock value) {
        return new JAXBElement<CTLock>(_Lock_QNAME, CTLock.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSignatureLine }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "signatureline")
    public JAXBElement<CTSignatureLine> createSignatureline(CTSignatureLine value) {
        return new JAXBElement<CTSignatureLine>(_Signatureline_QNAME, CTSignatureLine.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCallout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "callout")
    public JAXBElement<CTCallout> createCallout(CTCallout value) {
        return new JAXBElement<CTCallout>(_Callout_QNAME, CTCallout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTFill }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "fill")
    public JAXBElement<CTFill> createFill(CTFill value) {
        return new JAXBElement<CTFill>(_Fill_QNAME, CTFill.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTOLEObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "OLEObject")
    public JAXBElement<CTOLEObject> createOLEObject(CTOLEObject value) {
        return new JAXBElement<CTOLEObject>(_OLEObject_QNAME, CTOLEObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSkew }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "skew")
    public JAXBElement<CTSkew> createSkew(CTSkew value) {
        return new JAXBElement<CTSkew>(_Skew_QNAME, CTSkew.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTClipPath }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "clippath")
    public JAXBElement<CTClipPath> createClippath(CTClipPath value) {
        return new JAXBElement<CTClipPath>(_Clippath_QNAME, CTClipPath.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "complex")
    public JAXBElement<CTComplex> createComplex(CTComplex value) {
        return new JAXBElement<CTComplex>(_Complex_QNAME, CTComplex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStrokeChild }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "left")
    public JAXBElement<CTStrokeChild> createLeft(CTStrokeChild value) {
        return new JAXBElement<CTStrokeChild>(_Left_QNAME, CTStrokeChild.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeDefaults }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "shapedefaults")
    public JAXBElement<CTShapeDefaults> createShapedefaults(CTShapeDefaults value) {
        return new JAXBElement<CTShapeDefaults>(_Shapedefaults_QNAME, CTShapeDefaults.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStrokeChild }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "top")
    public JAXBElement<CTStrokeChild> createTop(CTStrokeChild value) {
        return new JAXBElement<CTStrokeChild>(_Top_QNAME, CTStrokeChild.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInk }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "ink")
    public JAXBElement<CTInk> createInk(CTInk value) {
        return new JAXBElement<CTInk>(_Ink_QNAME, CTInk.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStrokeChild }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "bottom")
    public JAXBElement<CTStrokeChild> createBottom(CTStrokeChild value) {
        return new JAXBElement<CTStrokeChild>(_Bottom_QNAME, CTStrokeChild.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStrokeChild }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "column")
    public JAXBElement<CTStrokeChild> createColumn(CTStrokeChild value) {
        return new JAXBElement<CTStrokeChild>(_Column_QNAME, CTStrokeChild.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTExtrusion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "extrusion")
    public JAXBElement<CTExtrusion> createExtrusion(CTExtrusion value) {
        return new JAXBElement<CTExtrusion>(_Extrusion_QNAME, CTExtrusion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDiagram }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "diagram")
    public JAXBElement<CTDiagram> createDiagram(CTDiagram value) {
        return new JAXBElement<CTDiagram>(_Diagram_QNAME, CTDiagram.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeLayout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "shapelayout")
    public JAXBElement<CTShapeLayout> createShapelayout(CTShapeLayout value) {
        return new JAXBElement<CTShapeLayout>(_Shapelayout_QNAME, CTShapeLayout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTStrokeChild }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "right")
    public JAXBElement<CTStrokeChild> createRight(CTStrokeChild value) {
        return new JAXBElement<CTStrokeChild>(_Right_QNAME, CTStrokeChild.class, null, value);
    }

}
