
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201510.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ClientRevision complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ClientRevision"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="id" use="required" type="{http://schemas.microsoft.com/office/powerpoint/2015/10/main}ST_ClientID" /&gt;
 *       &lt;attribute name="v" type="{http://schemas.microsoft.com/office/powerpoint/2015/10/main}ST_ClientRevisionNumber" default="0" /&gt;
 *       &lt;attribute name="vWet" type="{http://schemas.microsoft.com/office/powerpoint/2015/10/main}ST_ClientRevisionNumber" default="0" /&gt;
 *       &lt;attribute name="dt" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ClientRevision")
public class CTClientRevision implements Child
{

    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "v")
    protected Long v;
    @XmlAttribute(name = "vWet")
    protected Long vWet;
    @XmlAttribute(name = "dt", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dt;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the v property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getV() {
        if (v == null) {
            return  0L;
        } else {
            return v;
        }
    }

    /**
     * Sets the value of the v property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setV(Long value) {
        this.v = value;
    }

    /**
     * Gets the value of the vWet property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getVWet() {
        if (vWet == null) {
            return  0L;
        } else {
            return vWet;
        }
    }

    /**
     * Sets the value of the vWet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVWet(Long value) {
        this.vWet = value;
    }

    /**
     * Gets the value of the dt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDt() {
        return dt;
    }

    /**
     * Sets the value of the dt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDt(XMLGregorianCalendar value) {
        this.dt = value;
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
