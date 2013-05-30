
package org.docx4j.schemas.microsoft.com.office.word_2006.wordml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.schemas.microsoft.com.office.word_2006.wordml package. 
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

    private final static QName _VbaSuppData_QNAME = new QName("http://schemas.microsoft.com/office/word/2006/wordml", "vbaSuppData");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.schemas.microsoft.com.office.word_2006.wordml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTVbaSuppData }
     * 
     */
    public CTVbaSuppData createCTVbaSuppData() {
        return new CTVbaSuppData();
    }

    /**
     * Create an instance of {@link CTMcd }
     * 
     */
    public CTMcd createCTMcd() {
        return new CTMcd();
    }

    /**
     * Create an instance of {@link CTDocEvents }
     * 
     */
    public CTDocEvents createCTDocEvents() {
        return new CTDocEvents();
    }

    /**
     * Create an instance of {@link CTMcds }
     * 
     */
    public CTMcds createCTMcds() {
        return new CTMcds();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTVbaSuppData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2006/wordml", name = "vbaSuppData")
    public JAXBElement<CTVbaSuppData> createVbaSuppData(CTVbaSuppData value) {
        return new JAXBElement<CTVbaSuppData>(_VbaSuppData_QNAME, CTVbaSuppData.class, null, value);
    }

}
