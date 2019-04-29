
package org.docx4j.dml.diagram2008;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.dml.diagram2008 package. 
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

    private final static QName _Drawing_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2008/diagram", "drawing");
    private final static QName _DataModelExt_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2008/diagram", "dataModelExt");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.dml.diagram2008
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTDrawing }
     * 
     */
    public CTDrawing createCTDrawing() {
        return new CTDrawing();
    }

    /**
     * Create an instance of {@link CTDataModelExtBlock }
     * 
     */
    public CTDataModelExtBlock createCTDataModelExtBlock() {
        return new CTDataModelExtBlock();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDrawing }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2008/diagram", name = "drawing")
    public JAXBElement<CTDrawing> createDrawing(CTDrawing value) {
        return new JAXBElement<CTDrawing>(_Drawing_QNAME, CTDrawing.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDataModelExtBlock }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2008/diagram", name = "dataModelExt")
    public JAXBElement<CTDataModelExtBlock> createDataModelExt(CTDataModelExtBlock value) {
        return new JAXBElement<CTDataModelExtBlock>(_DataModelExt_QNAME, CTDataModelExtBlock.class, null, value);
    }

}
