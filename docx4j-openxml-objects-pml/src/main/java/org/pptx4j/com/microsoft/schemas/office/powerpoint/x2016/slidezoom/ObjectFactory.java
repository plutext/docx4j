
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.slidezoom;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.slidezoom package. 
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

    private final static QName _SldZm_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2016/slidezoom", "sldZm");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.slidezoom
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTSlideZoom }
     * 
     */
    public CTSlideZoom createCTSlideZoom() {
        return new CTSlideZoom();
    }

    /**
     * Create an instance of {@link CTSlideZoomObject }
     * 
     */
    public CTSlideZoomObject createCTSlideZoomObject() {
        return new CTSlideZoomObject();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSlideZoom }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2016/slidezoom", name = "sldZm")
    public JAXBElement<CTSlideZoom> createSldZm(CTSlideZoom value) {
        return new JAXBElement<CTSlideZoom>(_SldZm_QNAME, CTSlideZoom.class, null, value);
    }

}
