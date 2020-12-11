
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart.ac;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.docx4j.dml.chart.CTMultiLvlStrData;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart.ac package. 
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

    private final static QName _MultiLvlStrLit_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2014/chart/ac", "multiLvlStrLit");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart.ac
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTMultiLvlStrData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2014/chart/ac", name = "multiLvlStrLit")
    public JAXBElement<CTMultiLvlStrData> createMultiLvlStrLit(CTMultiLvlStrData value) {
        return new JAXBElement<CTMultiLvlStrData>(_MultiLvlStrLit_QNAME, CTMultiLvlStrData.class, null, value);
    }

}
