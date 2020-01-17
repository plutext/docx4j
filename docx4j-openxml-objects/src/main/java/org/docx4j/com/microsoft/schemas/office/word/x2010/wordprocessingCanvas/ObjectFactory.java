
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas package. 
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

    private final static QName _Wpc_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas", "wpc");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTWordprocessingCanvas }
     * 
     */
    public CTWordprocessingCanvas createCTWordprocessingCanvas() {
        return new CTWordprocessingCanvas();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWordprocessingCanvas }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas", name = "wpc")
    public JAXBElement<CTWordprocessingCanvas> createWpc(CTWordprocessingCanvas value) {
        return new JAXBElement<CTWordprocessingCanvas>(_Wpc_QNAME, CTWordprocessingCanvas.class, null, value);
    }

}
