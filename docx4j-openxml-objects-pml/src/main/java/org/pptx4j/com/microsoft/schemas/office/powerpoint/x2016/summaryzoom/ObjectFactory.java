
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom package. 
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

    private final static QName _SummaryZm_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2016/summaryzoom", "summaryZm");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTSummaryZoom }
     * 
     */
    public CTSummaryZoom createCTSummaryZoom() {
        return new CTSummaryZoom();
    }

    /**
     * Create an instance of {@link CTSummaryZoomObject }
     * 
     */
    public CTSummaryZoomObject createCTSummaryZoomObject() {
        return new CTSummaryZoomObject();
    }

    /**
     * Create an instance of {@link CTGridLayout }
     * 
     */
    public CTGridLayout createCTGridLayout() {
        return new CTGridLayout();
    }

    /**
     * Create an instance of {@link CTFixedLayout }
     * 
     */
    public CTFixedLayout createCTFixedLayout() {
        return new CTFixedLayout();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSummaryZoom }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2016/summaryzoom", name = "summaryZm")
    public JAXBElement<CTSummaryZoom> createSummaryZm(CTSummaryZoom value) {
        return new JAXBElement<CTSummaryZoom>(_SummaryZm_QNAME, CTSummaryZoom.class, null, value);
    }

}
