
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape package. 
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

    private final static QName _Wsp_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordprocessingShape", "wsp");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTWordprocessingShape }
     * 
     */
    public CTWordprocessingShape createCTWordprocessingShape() {
        return new CTWordprocessingShape();
    }

    /**
     * Create an instance of {@link CTTextboxInfo }
     * 
     */
    public CTTextboxInfo createCTTextboxInfo() {
        return new CTTextboxInfo();
    }

    /**
     * Create an instance of {@link CTLinkedTextboxInformation }
     * 
     */
    public CTLinkedTextboxInformation createCTLinkedTextboxInformation() {
        return new CTLinkedTextboxInformation();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWordprocessingShape }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape", name = "wsp")
    public JAXBElement<CTWordprocessingShape> createWsp(CTWordprocessingShape value) {
        return new JAXBElement<CTWordprocessingShape>(_Wsp_QNAME, CTWordprocessingShape.class, null, value);
    }

}
