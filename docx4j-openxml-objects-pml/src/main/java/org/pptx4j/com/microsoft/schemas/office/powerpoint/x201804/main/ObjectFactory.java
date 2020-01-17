
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201804.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x201804.main package. 
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

    private final static QName _Classification_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2018/4/main", "classification");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x201804.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTClassificationOutcome }
     * 
     */
    public CTClassificationOutcome createCTClassificationOutcome() {
        return new CTClassificationOutcome();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTClassificationOutcome }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2018/4/main", name = "classification")
    public JAXBElement<CTClassificationOutcome> createClassification(CTClassificationOutcome value) {
        return new JAXBElement<CTClassificationOutcome>(_Classification_QNAME, CTClassificationOutcome.class, null, value);
    }

}
