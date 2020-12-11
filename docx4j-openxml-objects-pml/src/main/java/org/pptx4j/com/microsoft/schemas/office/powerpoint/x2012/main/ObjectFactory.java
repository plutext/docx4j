
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main package. 
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

    private final static QName _PrstTrans_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2012/main", "prstTrans");
    private final static QName _PresenceInfo_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2012/main", "presenceInfo");
    private final static QName _ThreadingInfo_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2012/main", "threadingInfo");
    private final static QName _SldGuideLst_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2012/main", "sldGuideLst");
    private final static QName _NotesGuideLst_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2012/main", "notesGuideLst");
    private final static QName _ChartTrackingRefBased_QNAME = new QName("http://schemas.microsoft.com/office/powerpoint/2012/main", "chartTrackingRefBased");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTPresetTransition }
     * 
     */
    public CTPresetTransition createCTPresetTransition() {
        return new CTPresetTransition();
    }

    /**
     * Create an instance of {@link CTPresenceInfo }
     * 
     */
    public CTPresenceInfo createCTPresenceInfo() {
        return new CTPresenceInfo();
    }

    /**
     * Create an instance of {@link CTCommentThreading }
     * 
     */
    public CTCommentThreading createCTCommentThreading() {
        return new CTCommentThreading();
    }

    /**
     * Create an instance of {@link CTExtendedGuideList }
     * 
     */
    public CTExtendedGuideList createCTExtendedGuideList() {
        return new CTExtendedGuideList();
    }

    /**
     * Create an instance of {@link CTChartTrackingRefBased }
     * 
     */
    public CTChartTrackingRefBased createCTChartTrackingRefBased() {
        return new CTChartTrackingRefBased();
    }

    /**
     * Create an instance of {@link CTParentCommentIdentifier }
     * 
     */
    public CTParentCommentIdentifier createCTParentCommentIdentifier() {
        return new CTParentCommentIdentifier();
    }

    /**
     * Create an instance of {@link CTExtendedGuide }
     * 
     */
    public CTExtendedGuide createCTExtendedGuide() {
        return new CTExtendedGuide();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPresetTransition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2012/main", name = "prstTrans")
    public JAXBElement<CTPresetTransition> createPrstTrans(CTPresetTransition value) {
        return new JAXBElement<CTPresetTransition>(_PrstTrans_QNAME, CTPresetTransition.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPresenceInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2012/main", name = "presenceInfo")
    public JAXBElement<CTPresenceInfo> createPresenceInfo(CTPresenceInfo value) {
        return new JAXBElement<CTPresenceInfo>(_PresenceInfo_QNAME, CTPresenceInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCommentThreading }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2012/main", name = "threadingInfo")
    public JAXBElement<CTCommentThreading> createThreadingInfo(CTCommentThreading value) {
        return new JAXBElement<CTCommentThreading>(_ThreadingInfo_QNAME, CTCommentThreading.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTExtendedGuideList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2012/main", name = "sldGuideLst")
    public JAXBElement<CTExtendedGuideList> createSldGuideLst(CTExtendedGuideList value) {
        return new JAXBElement<CTExtendedGuideList>(_SldGuideLst_QNAME, CTExtendedGuideList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTExtendedGuideList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2012/main", name = "notesGuideLst")
    public JAXBElement<CTExtendedGuideList> createNotesGuideLst(CTExtendedGuideList value) {
        return new JAXBElement<CTExtendedGuideList>(_NotesGuideLst_QNAME, CTExtendedGuideList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTChartTrackingRefBased }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/powerpoint/2012/main", name = "chartTrackingRefBased")
    public JAXBElement<CTChartTrackingRefBased> createChartTrackingRefBased(CTChartTrackingRefBased value) {
        return new JAXBElement<CTChartTrackingRefBased>(_ChartTrackingRefBased_QNAME, CTChartTrackingRefBased.class, null, value);
    }

}
