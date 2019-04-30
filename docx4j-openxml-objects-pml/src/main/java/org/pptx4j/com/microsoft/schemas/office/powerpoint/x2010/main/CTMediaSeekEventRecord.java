
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_MediaSeekEventRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MediaSeekEventRecord"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="time" use="required" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}ST_UniversalTimeOffset" /&gt;
 *       &lt;attribute name="objId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_DrawingElementId" /&gt;
 *       &lt;attribute name="seek" use="required" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}ST_UniversalTimeOffset" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MediaSeekEventRecord")
public class CTMediaSeekEventRecord implements Child
{

    @XmlAttribute(name = "time", required = true)
    protected String time;
    @XmlAttribute(name = "objId", required = true)
    protected long objId;
    @XmlAttribute(name = "seek", required = true)
    protected String seek;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the objId property.
     * 
     */
    public long getObjId() {
        return objId;
    }

    /**
     * Sets the value of the objId property.
     * 
     */
    public void setObjId(long value) {
        this.objId = value;
    }

    /**
     * Gets the value of the seek property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeek() {
        return seek;
    }

    /**
     * Sets the value of the seek property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeek(String value) {
        this.seek = value;
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
