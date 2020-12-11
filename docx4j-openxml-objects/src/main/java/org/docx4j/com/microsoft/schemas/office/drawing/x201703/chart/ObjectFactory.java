
package org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart package. 
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

    private final static QName _DataDisplayOptions16_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2017/03/chart", "dataDisplayOptions16");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTDataDisplayOptions16 }
     * 
     */
    public CTDataDisplayOptions16 createCTDataDisplayOptions16() {
        return new CTDataDisplayOptions16();
    }

    /**
     * Create an instance of {@link CTBooleanFalse }
     * 
     */
    public CTBooleanFalse createCTBooleanFalse() {
        return new CTBooleanFalse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTDataDisplayOptions16 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2017/03/chart", name = "dataDisplayOptions16")
    public JAXBElement<CTDataDisplayOptions16> createDataDisplayOptions16(CTDataDisplayOptions16 value) {
        return new JAXBElement<CTDataDisplayOptions16>(_DataDisplayOptions16_QNAME, CTDataDisplayOptions16 .class, null, value);
    }

}
