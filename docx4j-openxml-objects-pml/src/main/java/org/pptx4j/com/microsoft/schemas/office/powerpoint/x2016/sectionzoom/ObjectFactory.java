
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.sectionzoom;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.sectionzoom package. 
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

    private final static QName _SectionZm_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2016/sectionzoom", "sectionZm");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.sectionzoom
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTSectionZoom }
     * 
     */
    public CTSectionZoom createCTSectionZoom() {
        return new CTSectionZoom();
    }

    /**
     * Create an instance of {@link CTSectionZoomObject }
     * 
     */
    public CTSectionZoomObject createCTSectionZoomObject() {
        return new CTSectionZoomObject();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSectionZoom }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2016/sectionzoom", name = "sectionZm")
    public JAXBElement<CTSectionZoom> createSectionZm(CTSectionZoom value) {
        return new JAXBElement<CTSectionZoom>(_SectionZm_QNAME, CTSectionZoom.class, null, value);
    }

}
