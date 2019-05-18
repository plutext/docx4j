
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201710.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x201710.main package. 
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

    private final static QName _ReadonlyRecommended_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2017/10/main", "readonlyRecommended");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x201710.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTReadonlyRecommended }
     * 
     */
    public CTReadonlyRecommended createCTReadonlyRecommended() {
        return new CTReadonlyRecommended();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTReadonlyRecommended }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2017/10/main", name = "readonlyRecommended")
    public JAXBElement<CTReadonlyRecommended> createReadonlyRecommended(CTReadonlyRecommended value) {
        return new JAXBElement<CTReadonlyRecommended>(_ReadonlyRecommended_QNAME, CTReadonlyRecommended.class, null, value);
    }

}
