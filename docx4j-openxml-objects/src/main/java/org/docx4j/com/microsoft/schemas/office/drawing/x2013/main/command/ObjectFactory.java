
package org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command package. 
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

    private final static QName _SpMkLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2013/main/command", "spMkLst");
    private final static QName _GrpMkLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2013/main/command", "grpMkLst");
    private final static QName _GraphicFrameMkLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2013/main/command", "graphicFrameMkLst");
    private final static QName _CxnSpMkLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2013/main/command", "cxnSpMkLst");
    private final static QName _PicMkLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2013/main/command", "picMkLst");
    private final static QName _InkMkLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2013/main/command", "inkMkLst");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTShapeMonikerList }
     * 
     */
    public CTShapeMonikerList createCTShapeMonikerList() {
        return new CTShapeMonikerList();
    }

    /**
     * Create an instance of {@link CTGroupShapeMonikerList }
     * 
     */
    public CTGroupShapeMonikerList createCTGroupShapeMonikerList() {
        return new CTGroupShapeMonikerList();
    }

    /**
     * Create an instance of {@link CTGraphicFrameMonikerList }
     * 
     */
    public CTGraphicFrameMonikerList createCTGraphicFrameMonikerList() {
        return new CTGraphicFrameMonikerList();
    }

    /**
     * Create an instance of {@link CTConnectorMonikerList }
     * 
     */
    public CTConnectorMonikerList createCTConnectorMonikerList() {
        return new CTConnectorMonikerList();
    }

    /**
     * Create an instance of {@link CTPictureMonikerList }
     * 
     */
    public CTPictureMonikerList createCTPictureMonikerList() {
        return new CTPictureMonikerList();
    }

    /**
     * Create an instance of {@link CTInkMonikerList }
     * 
     */
    public CTInkMonikerList createCTInkMonikerList() {
        return new CTInkMonikerList();
    }

    /**
     * Create an instance of {@link CTShapeMoniker }
     * 
     */
    public CTShapeMoniker createCTShapeMoniker() {
        return new CTShapeMoniker();
    }

    /**
     * Create an instance of {@link CTGroupShapeMoniker }
     * 
     */
    public CTGroupShapeMoniker createCTGroupShapeMoniker() {
        return new CTGroupShapeMoniker();
    }

    /**
     * Create an instance of {@link CTGraphicFrameMoniker }
     * 
     */
    public CTGraphicFrameMoniker createCTGraphicFrameMoniker() {
        return new CTGraphicFrameMoniker();
    }

    /**
     * Create an instance of {@link CTConnectorMoniker }
     * 
     */
    public CTConnectorMoniker createCTConnectorMoniker() {
        return new CTConnectorMoniker();
    }

    /**
     * Create an instance of {@link CTPictureMoniker }
     * 
     */
    public CTPictureMoniker createCTPictureMoniker() {
        return new CTPictureMoniker();
    }

    /**
     * Create an instance of {@link CTInkMoniker }
     * 
     */
    public CTInkMoniker createCTInkMoniker() {
        return new CTInkMoniker();
    }

    /**
     * Create an instance of {@link CTChangesData }
     * 
     */
    public CTChangesData createCTChangesData() {
        return new CTChangesData();
    }

    /**
     * Create an instance of {@link CTShapeChanges }
     * 
     */
    public CTShapeChanges createCTShapeChanges() {
        return new CTShapeChanges();
    }

    /**
     * Create an instance of {@link CTGroupShapeChanges }
     * 
     */
    public CTGroupShapeChanges createCTGroupShapeChanges() {
        return new CTGroupShapeChanges();
    }

    /**
     * Create an instance of {@link CTConnectorChanges }
     * 
     */
    public CTConnectorChanges createCTConnectorChanges() {
        return new CTConnectorChanges();
    }

    /**
     * Create an instance of {@link CTPictureChanges }
     * 
     */
    public CTPictureChanges createCTPictureChanges() {
        return new CTPictureChanges();
    }

    /**
     * Create an instance of {@link CTInkChanges }
     * 
     */
    public CTInkChanges createCTInkChanges() {
        return new CTInkChanges();
    }

    /**
     * Create an instance of {@link CTGraphicFrameChanges }
     * 
     */
    public CTGraphicFrameChanges createCTGraphicFrameChanges() {
        return new CTGraphicFrameChanges();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeMonikerList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2013/main/command", name = "spMkLst")
    public JAXBElement<CTShapeMonikerList> createSpMkLst(CTShapeMonikerList value) {
        return new JAXBElement<CTShapeMonikerList>(_SpMkLst_QNAME, CTShapeMonikerList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGroupShapeMonikerList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2013/main/command", name = "grpMkLst")
    public JAXBElement<CTGroupShapeMonikerList> createGrpMkLst(CTGroupShapeMonikerList value) {
        return new JAXBElement<CTGroupShapeMonikerList>(_GrpMkLst_QNAME, CTGroupShapeMonikerList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTGraphicFrameMonikerList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2013/main/command", name = "graphicFrameMkLst")
    public JAXBElement<CTGraphicFrameMonikerList> createGraphicFrameMkLst(CTGraphicFrameMonikerList value) {
        return new JAXBElement<CTGraphicFrameMonikerList>(_GraphicFrameMkLst_QNAME, CTGraphicFrameMonikerList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTConnectorMonikerList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2013/main/command", name = "cxnSpMkLst")
    public JAXBElement<CTConnectorMonikerList> createCxnSpMkLst(CTConnectorMonikerList value) {
        return new JAXBElement<CTConnectorMonikerList>(_CxnSpMkLst_QNAME, CTConnectorMonikerList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPictureMonikerList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2013/main/command", name = "picMkLst")
    public JAXBElement<CTPictureMonikerList> createPicMkLst(CTPictureMonikerList value) {
        return new JAXBElement<CTPictureMonikerList>(_PicMkLst_QNAME, CTPictureMonikerList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTInkMonikerList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2013/main/command", name = "inkMkLst")
    public JAXBElement<CTInkMonikerList> createInkMkLst(CTInkMonikerList value) {
        return new JAXBElement<CTInkMonikerList>(_InkMkLst_QNAME, CTInkMonikerList.class, null, value);
    }

}
