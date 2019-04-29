
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.diagram;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTNonVisualDrawingProps;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2010.diagram package. 
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

    private final static QName _CNvPr_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2010/diagram", "cNvPr");
    private final static QName _RecolorImg_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2010/diagram", "recolorImg");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2010.diagram
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTBoolean }
     * 
     */
    public CTBoolean createCTBoolean() {
        return new CTBoolean();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTNonVisualDrawingProps }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2010/diagram", name = "cNvPr")
    public JAXBElement<CTNonVisualDrawingProps> createCNvPr(CTNonVisualDrawingProps value) {
        return new JAXBElement<CTNonVisualDrawingProps>(_CNvPr_QNAME, CTNonVisualDrawingProps.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2010/diagram", name = "recolorImg")
    public JAXBElement<CTBoolean> createRecolorImg(CTBoolean value) {
        return new JAXBElement<CTBoolean>(_RecolorImg_QNAME, CTBoolean.class, null, value);
    }

}
