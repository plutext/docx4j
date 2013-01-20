
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DataBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="DataBindingName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="FileBinding" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="ConnectionID" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="FileBindingName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DataBindingLoadMode" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataBinding", propOrder = {
    "any"
})
public class CTDataBinding implements Child
{

    @XmlAnyElement(lax = true)
    protected Object any;
    @XmlAttribute(name = "DataBindingName")
    protected String dataBindingName;
    @XmlAttribute(name = "FileBinding")
    protected Boolean fileBinding;
    @XmlAttribute(name = "ConnectionID")
    @XmlSchemaType(name = "unsignedInt")
    protected Long connectionID;
    @XmlAttribute(name = "FileBindingName")
    protected String fileBindingName;
    @XmlAttribute(name = "DataBindingLoadMode", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long dataBindingLoadMode;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

    /**
     * Gets the value of the dataBindingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataBindingName() {
        return dataBindingName;
    }

    /**
     * Sets the value of the dataBindingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataBindingName(String value) {
        this.dataBindingName = value;
    }

    /**
     * Gets the value of the fileBinding property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFileBinding() {
        return fileBinding;
    }

    /**
     * Sets the value of the fileBinding property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFileBinding(Boolean value) {
        this.fileBinding = value;
    }

    /**
     * Gets the value of the connectionID property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getConnectionID() {
        return connectionID;
    }

    /**
     * Sets the value of the connectionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setConnectionID(Long value) {
        this.connectionID = value;
    }

    /**
     * Gets the value of the fileBindingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileBindingName() {
        return fileBindingName;
    }

    /**
     * Sets the value of the fileBindingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileBindingName(String value) {
        this.fileBindingName = value;
    }

    /**
     * Gets the value of the dataBindingLoadMode property.
     * 
     */
    public long getDataBindingLoadMode() {
        return dataBindingLoadMode;
    }

    /**
     * Sets the value of the dataBindingLoadMode property.
     * 
     */
    public void setDataBindingLoadMode(long value) {
        this.dataBindingLoadMode = value;
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
