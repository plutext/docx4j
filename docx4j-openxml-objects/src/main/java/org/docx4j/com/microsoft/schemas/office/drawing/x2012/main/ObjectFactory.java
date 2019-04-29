
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2012.main package. 
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

    private final static QName _BackgroundPr_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/main", "backgroundPr");
    private final static QName _NonVisualGroupProps_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/main", "nonVisualGroupProps");
    private final static QName _ObjectPr_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/main", "objectPr");
    private final static QName _SignatureLine_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2012/main", "signatureLine");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2012.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTBackgroundPr }
     * 
     */
    public CTBackgroundPr createCTBackgroundPr() {
        return new CTBackgroundPr();
    }

    /**
     * Create an instance of {@link CTNonVisualGroupProps }
     * 
     */
    public CTNonVisualGroupProps createCTNonVisualGroupProps() {
        return new CTNonVisualGroupProps();
    }

    /**
     * Create an instance of {@link CTObjectPr }
     * 
     */
    public CTObjectPr createCTObjectPr() {
        return new CTObjectPr();
    }

    /**
     * Create an instance of {@link CTSignatureLine }
     * 
     */
    public CTSignatureLine createCTSignatureLine() {
        return new CTSignatureLine();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBackgroundPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/main", name = "backgroundPr")
    public JAXBElement<CTBackgroundPr> createBackgroundPr(CTBackgroundPr value) {
        return new JAXBElement<CTBackgroundPr>(_BackgroundPr_QNAME, CTBackgroundPr.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTNonVisualGroupProps }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/main", name = "nonVisualGroupProps")
    public JAXBElement<CTNonVisualGroupProps> createNonVisualGroupProps(CTNonVisualGroupProps value) {
        return new JAXBElement<CTNonVisualGroupProps>(_NonVisualGroupProps_QNAME, CTNonVisualGroupProps.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTObjectPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/main", name = "objectPr")
    public JAXBElement<CTObjectPr> createObjectPr(CTObjectPr value) {
        return new JAXBElement<CTObjectPr>(_ObjectPr_QNAME, CTObjectPr.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSignatureLine }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2012/main", name = "signatureLine")
    public JAXBElement<CTSignatureLine> createSignatureLine(CTSignatureLine value) {
        return new JAXBElement<CTSignatureLine>(_SignatureLine_QNAME, CTSignatureLine.class, null, value);
    }

}
