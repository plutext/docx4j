
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_MediaBookmarkTarget complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MediaBookmarkTarget"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="spid" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_DrawingElementId" /&gt;
 *       &lt;attribute name="bmkName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MediaBookmarkTarget")
public class CTMediaBookmarkTarget implements Child
{

    @XmlAttribute(name = "spid", required = true)
    protected long spid;
    @XmlAttribute(name = "bmkName", required = true)
    protected String bmkName;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the spid property.
     * 
     */
    public long getSpid() {
        return spid;
    }

    /**
     * Sets the value of the spid property.
     * 
     */
    public void setSpid(long value) {
        this.spid = value;
    }

    /**
     * Gets the value of the bmkName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBmkName() {
        return bmkName;
    }

    /**
     * Sets the value of the bmkName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBmkName(String value) {
        this.bmkName = value;
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
