
package org.docx4j.com.microsoft.schemas.office.drawing.x201612.diagram;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextListStyle;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x201612.diagram package. 
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

    private final static QName _SpPr_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2016/12/diagram", "spPr");
    private final static QName _LstStyle_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2016/12/diagram", "lstStyle");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x201612.diagram
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTShapeProperties }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2016/12/diagram", name = "spPr")
    public JAXBElement<CTShapeProperties> createSpPr(CTShapeProperties value) {
        return new JAXBElement<CTShapeProperties>(_SpPr_QNAME, CTShapeProperties.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTextListStyle }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2016/12/diagram", name = "lstStyle")
    public JAXBElement<CTTextListStyle> createLstStyle(CTTextListStyle value) {
        return new JAXBElement<CTTextListStyle>(_LstStyle_QNAME, CTTextListStyle.class, null, value);
    }

}
