/*
 *  Copyright 2007-2009, Plutext Pty Ltd.
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
    private final static QName _WordFieldCodes_QNAME = new QName("urn:schemas-microsoft-com:office:office", "WordFieldCodes");
    private final static QName _Callout_QNAME = new QName("urn:schemas-microsoft-com:office:office", "callout");
    private final static QName _Extrusion_QNAME = new QName("urn:schemas-microsoft-com:office:office", "extrusion");
    private final static QName _Shapelayout_QNAME = new QName("urn:schemas-microsoft-com:office:office", "shapelayout");
    private final static QName _OLEObject_QNAME = new QName("urn:schemas-microsoft-com:office:office", "OLEObject");
    private final static QName _Skew_QNAME = new QName("urn:schemas-microsoft-com:office:office", "skew");
    private final static QName _Complex_QNAME = new QName("urn:schemas-microsoft-com:office:office", "complex");
    private final static QName _Shapedefaults_QNAME = new QName("urn:schemas-microsoft-com:office:office", "shapedefaults");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.vml.officedrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTShapeLayout }
     * 
     */
    public CTShapeLayout createCTShapeLayout() {
        return new CTShapeLayout();
    }

    /**
     * Create an instance of {@link CTLock }
     * 
     */
    public CTLock createCTLock() {
        return new CTLock();
    }

    /**
     * Create an instance of {@link CTCallout }
     * 
     */
    public CTCallout createCTCallout() {
        return new CTCallout();
    }

    /**
     * Create an instance of {@link CTComplex }
     * 
     */
    public CTComplex createCTComplex() {
        return new CTComplex();
    }

    /**
     * Create an instance of {@link CTRegroupTable }
     * 
     */
    public CTRegroupTable createCTRegroupTable() {
        return new CTRegroupTable();
    }

    /**
     * Create an instance of {@link CTExtrusion }
     * 
     */
    public CTExtrusion createCTExtrusion() {
        return new CTExtrusion();
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
     * Create an instance of {@link CTProxy }
     * 
     */
    public CTProxy createCTProxy() {
        return new CTProxy();
    }

    /**
     * Create an instance of {@link CTR }
     * 
     */
    public CTR createCTR() {
        return new CTR();
    }

    /**
     * Create an instance of {@link CTColorMRU }
     * 
     */
    public CTColorMRU createCTColorMRU() {
        return new CTColorMRU();
    }

    /**
     * Create an instance of {@link CTColorMenu }
     * 
     */
    public CTColorMenu createCTColorMenu() {
        return new CTColorMenu();
    }

    /**
     * Create an instance of {@link CTSkew }
     * 
     */
    public CTSkew createCTSkew() {
        return new CTSkew();
    }

    /**
     * Create an instance of {@link CTIdMap }
     * 
     */
    public CTIdMap createCTIdMap() {
        return new CTIdMap();
    }

    /**
     * Create an instance of {@link CTOLEObject }
     * 
     */
    public CTOLEObject createCTOLEObject() {
        return new CTOLEObject();
    }

    /**
     * Create an instance of {@link CTShapeDefaults }
     * 
     */
    public CTShapeDefaults createCTShapeDefaults() {
        return new CTShapeDefaults();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "WordFieldCodes")
    public JAXBElement<String> createWordFieldCodes(String value) {
        return new JAXBElement<String>(_WordFieldCodes_QNAME, String.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link CTExtrusion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "extrusion")
    public JAXBElement<CTExtrusion> createExtrusion(CTExtrusion value) {
        return new JAXBElement<CTExtrusion>(_Extrusion_QNAME, CTExtrusion.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link CTComplex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "complex")
    public JAXBElement<CTComplex> createComplex(CTComplex value) {
        return new JAXBElement<CTComplex>(_Complex_QNAME, CTComplex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeDefaults }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:schemas-microsoft-com:office:office", name = "shapedefaults")
    public JAXBElement<CTShapeDefaults> createShapedefaults(CTShapeDefaults value) {
        return new JAXBElement<CTShapeDefaults>(_Shapedefaults_QNAME, CTShapeDefaults.class, null, value);
    }

}
