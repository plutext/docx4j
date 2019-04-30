
package org.docx4j.com.microsoft.schemas.office.word.x2012.wordprocessingDrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.word.x2012.wordprocessingDrawing package. 
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

    private final static QName _WebVideoPr_QNAME = new QName("http://schemas.microsoft.com/office/word/2012/wordprocessingDrawing", "webVideoPr");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.word.x2012.wordprocessingDrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTWebVideoPr }
     * 
     */
    public CTWebVideoPr createCTWebVideoPr() {
        return new CTWebVideoPr();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWebVideoPr }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2012/wordprocessingDrawing", name = "webVideoPr")
    public JAXBElement<CTWebVideoPr> createWebVideoPr(CTWebVideoPr value) {
        return new JAXBElement<CTWebVideoPr>(_WebVideoPr_QNAME, CTWebVideoPr.class, null, value);
    }

}
