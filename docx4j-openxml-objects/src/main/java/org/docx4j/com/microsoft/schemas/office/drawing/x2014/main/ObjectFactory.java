
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2014.main package. 
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

    private final static QName _CreationId_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/main", "creationId");
    private final static QName _PredDERef_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/main", "predDERef");
    private final static QName _CxnDERefs_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/main", "cxnDERefs");
    private final static QName _RowId_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/main", "rowId");
    private final static QName _ColId_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/main", "colId");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2014.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTCreationId }
     * 
     */
    public CTCreationId createCTCreationId() {
        return new CTCreationId();
    }

    /**
     * Create an instance of {@link CTPredecessorDrawingElementReference }
     * 
     */
    public CTPredecessorDrawingElementReference createCTPredecessorDrawingElementReference() {
        return new CTPredecessorDrawingElementReference();
    }

    /**
     * Create an instance of {@link CTConnectableReferences }
     * 
     */
    public CTConnectableReferences createCTConnectableReferences() {
        return new CTConnectableReferences();
    }

    /**
     * Create an instance of {@link CTIdentifier }
     * 
     */
    public CTIdentifier createCTIdentifier() {
        return new CTIdentifier();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCreationId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/main", name = "creationId")
    public JAXBElement<CTCreationId> createCreationId(CTCreationId value) {
        return new JAXBElement<CTCreationId>(_CreationId_QNAME, CTCreationId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPredecessorDrawingElementReference }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/main", name = "predDERef")
    public JAXBElement<CTPredecessorDrawingElementReference> createPredDERef(CTPredecessorDrawingElementReference value) {
        return new JAXBElement<CTPredecessorDrawingElementReference>(_PredDERef_QNAME, CTPredecessorDrawingElementReference.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTConnectableReferences }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/main", name = "cxnDERefs")
    public JAXBElement<CTConnectableReferences> createCxnDERefs(CTConnectableReferences value) {
        return new JAXBElement<CTConnectableReferences>(_CxnDERefs_QNAME, CTConnectableReferences.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTIdentifier }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/main", name = "rowId")
    public JAXBElement<CTIdentifier> createRowId(CTIdentifier value) {
        return new JAXBElement<CTIdentifier>(_RowId_QNAME, CTIdentifier.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTIdentifier }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/main", name = "colId")
    public JAXBElement<CTIdentifier> createColId(CTIdentifier value) {
        return new JAXBElement<CTIdentifier>(_ColId_QNAME, CTIdentifier.class, null, value);
    }

}
