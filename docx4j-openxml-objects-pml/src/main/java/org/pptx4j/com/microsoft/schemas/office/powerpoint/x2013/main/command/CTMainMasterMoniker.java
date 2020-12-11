
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_MainMasterMoniker complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MainMasterMoniker"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="cId" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}ST_CreationId" /&gt;
 *       &lt;attribute name="sldId" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_SlideMasterId" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MainMasterMoniker")
public class CTMainMasterMoniker implements Child
{

    @XmlAttribute(name = "cId")
    protected Long cId;
    @XmlAttribute(name = "sldId", required = true)
    protected long sldId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCId() {
        return cId;
    }

    /**
     * Sets the value of the cId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCId(Long value) {
        this.cId = value;
    }

    /**
     * Gets the value of the sldId property.
     * 
     */
    public long getSldId() {
        return sldId;
    }

    /**
     * Sets the value of the sldId property.
     * 
     */
    public void setSldId(long value) {
        this.sldId = value;
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
