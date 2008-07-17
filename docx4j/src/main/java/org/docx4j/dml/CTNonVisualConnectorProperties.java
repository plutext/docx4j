
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_NonVisualConnectorProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NonVisualConnectorProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cxnSpLocks" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ConnectorLocking" minOccurs="0"/>
 *         &lt;element name="stCxn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Connection" minOccurs="0"/>
 *         &lt;element name="endCxn" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Connection" minOccurs="0"/>
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtension" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NonVisualConnectorProperties", propOrder = {
    "cxnSpLocks",
    "stCxn",
    "endCxn",
    "ext"
})
public class CTNonVisualConnectorProperties
    implements Child
{

    protected CTConnectorLocking cxnSpLocks;
    protected CTConnection stCxn;
    protected CTConnection endCxn;
    protected CTOfficeArtExtension ext;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cxnSpLocks property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnectorLocking }
     *     
     */
    public CTConnectorLocking getCxnSpLocks() {
        return cxnSpLocks;
    }

    /**
     * Sets the value of the cxnSpLocks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnectorLocking }
     *     
     */
    public void setCxnSpLocks(CTConnectorLocking value) {
        this.cxnSpLocks = value;
    }

    /**
     * Gets the value of the stCxn property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnection }
     *     
     */
    public CTConnection getStCxn() {
        return stCxn;
    }

    /**
     * Sets the value of the stCxn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnection }
     *     
     */
    public void setStCxn(CTConnection value) {
        this.stCxn = value;
    }

    /**
     * Gets the value of the endCxn property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnection }
     *     
     */
    public CTConnection getEndCxn() {
        return endCxn;
    }

    /**
     * Sets the value of the endCxn property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnection }
     *     
     */
    public void setEndCxn(CTConnection value) {
        this.endCxn = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public CTOfficeArtExtension getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public void setExt(CTOfficeArtExtension value) {
        this.ext = value;
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
