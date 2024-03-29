
package org.docx4j.com.microsoft.schemas.office.drawing.x201611.main;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x201611.main package. 
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

    private final static QName _PicAttrSrcUrl_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2016/11/main", "picAttrSrcUrl");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x201611.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTPictureAttributionSourceURL }
     * 
     */
    public CTPictureAttributionSourceURL createCTPictureAttributionSourceURL() {
        return new CTPictureAttributionSourceURL();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPictureAttributionSourceURL }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2016/11/main", name = "picAttrSrcUrl")
    public JAXBElement<CTPictureAttributionSourceURL> createPicAttrSrcUrl(CTPictureAttributionSourceURL value) {
        return new JAXBElement<CTPictureAttributionSourceURL>(_PicAttrSrcUrl_QNAME, CTPictureAttributionSourceURL.class, null, value);
    }

}
