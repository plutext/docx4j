
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main package. 
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

    private final static QName _TracksInfo_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2017/3/main", "tracksInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTTracksInfo }
     * 
     */
    public CTTracksInfo createCTTracksInfo() {
        return new CTTracksInfo();
    }

    /**
     * Create an instance of {@link CTTrack }
     * 
     */
    public CTTrack createCTTrack() {
        return new CTTrack();
    }

    /**
     * Create an instance of {@link CTTrackList }
     * 
     */
    public CTTrackList createCTTrackList() {
        return new CTTrackList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTTracksInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2017/3/main", name = "tracksInfo")
    public JAXBElement<CTTracksInfo> createTracksInfo(CTTracksInfo value) {
        return new JAXBElement<CTTracksInfo>(_TracksInfo_QNAME, CTTracksInfo.class, null, value);
    }

}
