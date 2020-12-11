
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TracksInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TracksInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="trackLst" type="{http://schemas.microsoft.com/office/powerpoint/2017/3/main}CT_TrackList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="displayLoc" use="required" type="{http://schemas.microsoft.com/office/powerpoint/2017/3/main}ST_DisplayLocation" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TracksInfo", propOrder = {
    "trackLst"
})
public class CTTracksInfo implements Child
{

    protected CTTrackList trackLst;
    @XmlAttribute(name = "displayLoc", required = true)
    protected STDisplayLocation displayLoc;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the trackLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackList }
     *     
     */
    public CTTrackList getTrackLst() {
        return trackLst;
    }

    /**
     * Sets the value of the trackLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackList }
     *     
     */
    public void setTrackLst(CTTrackList value) {
        this.trackLst = value;
    }

    /**
     * Gets the value of the displayLoc property.
     * 
     * @return
     *     possible object is
     *     {@link STDisplayLocation }
     *     
     */
    public STDisplayLocation getDisplayLoc() {
        return displayLoc;
    }

    /**
     * Sets the value of the displayLoc property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDisplayLocation }
     *     
     */
    public void setDisplayLoc(STDisplayLocation value) {
        this.displayLoc = value;
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
