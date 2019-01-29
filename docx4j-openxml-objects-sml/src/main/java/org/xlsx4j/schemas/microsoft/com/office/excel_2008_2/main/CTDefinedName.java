
package org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * <p>Java class for CT_DefinedName complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DefinedName">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="argumentDescriptions" type="{http://schemas.microsoft.com/office/excel/2008/2/main}CT_DefinedNameArgumentDescriptions" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DefinedName", propOrder = {
    "argumentDescriptions"
})
public class CTDefinedName implements org.jvnet.jaxb2_commons.ppp.Child
{

    protected CTDefinedNameArgumentDescriptions argumentDescriptions;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the argumentDescriptions property.
     * 
     * @return
     *     possible object is
     *     {@link CTDefinedNameArgumentDescriptions }
     *     
     */
    public CTDefinedNameArgumentDescriptions getArgumentDescriptions() {
        return argumentDescriptions;
    }

    /**
     * Sets the value of the argumentDescriptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDefinedNameArgumentDescriptions }
     *     
     */
    public void setArgumentDescriptions(CTDefinedNameArgumentDescriptions value) {
        this.argumentDescriptions = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
