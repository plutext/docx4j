
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ShowEventRecordList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShowEventRecordList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="triggerEvt" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_TriggerEventRecord"/&gt;
 *           &lt;element name="playEvt" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaPlaybackEventRecord"/&gt;
 *           &lt;element name="stopEvt" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaPlaybackEventRecord"/&gt;
 *           &lt;element name="pauseEvt" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaPlaybackEventRecord"/&gt;
 *           &lt;element name="resumeEvt" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaPlaybackEventRecord"/&gt;
 *           &lt;element name="seekEvt" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaSeekEventRecord"/&gt;
 *           &lt;element name="nullEvt" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_NullEventRecord"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ShowEventRecordList", propOrder = {
    "triggerEvtOrPlayEvtOrStopEvt"
})
public class CTShowEventRecordList implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "stopEvt", namespace = "http://schemas.microsoft.com/office/powerpoint/2010/main", type = JAXBElement.class),
        @XmlElementRef(name = "pauseEvt", namespace = "http://schemas.microsoft.com/office/powerpoint/2010/main", type = JAXBElement.class),
        @XmlElementRef(name = "nullEvt", namespace = "http://schemas.microsoft.com/office/powerpoint/2010/main", type = JAXBElement.class),
        @XmlElementRef(name = "resumeEvt", namespace = "http://schemas.microsoft.com/office/powerpoint/2010/main", type = JAXBElement.class),
        @XmlElementRef(name = "triggerEvt", namespace = "http://schemas.microsoft.com/office/powerpoint/2010/main", type = JAXBElement.class),
        @XmlElementRef(name = "playEvt", namespace = "http://schemas.microsoft.com/office/powerpoint/2010/main", type = JAXBElement.class),
        @XmlElementRef(name = "seekEvt", namespace = "http://schemas.microsoft.com/office/powerpoint/2010/main", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> triggerEvtOrPlayEvtOrStopEvt;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the triggerEvtOrPlayEvtOrStopEvt property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the triggerEvtOrPlayEvtOrStopEvt property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTriggerEvtOrPlayEvtOrStopEvt().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CTMediaPlaybackEventRecord }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMediaPlaybackEventRecord }{@code >}
     * {@link JAXBElement }{@code <}{@link CTNullEventRecord }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMediaPlaybackEventRecord }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTriggerEventRecord }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMediaPlaybackEventRecord }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMediaSeekEventRecord }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getTriggerEvtOrPlayEvtOrStopEvt() {
        if (triggerEvtOrPlayEvtOrStopEvt == null) {
            triggerEvtOrPlayEvtOrStopEvt = new ArrayList<JAXBElement<?>>();
        }
        return this.triggerEvtOrPlayEvtOrStopEvt;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
