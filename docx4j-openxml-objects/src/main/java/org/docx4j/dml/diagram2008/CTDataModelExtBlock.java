
package org.docx4j.dml.diagram2008;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DataModelExtBlock complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataModelExtBlock"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="relId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="minVer" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataModelExtBlock")
public class CTDataModelExtBlock implements Child
{

    @XmlAttribute(name = "relId")
    protected String relId;
    @XmlAttribute(name = "minVer")
    @XmlSchemaType(name = "anyURI")
    protected String minVer;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the relId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelId() {
        return relId;
    }

    /**
     * Sets the value of the relId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelId(String value) {
        this.relId = value;
    }

    /**
     * Gets the value of the minVer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinVer() {
        return minVer;
    }

    /**
     * Sets the value of the minVer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinVer(String value) {
        this.minVer = value;
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
