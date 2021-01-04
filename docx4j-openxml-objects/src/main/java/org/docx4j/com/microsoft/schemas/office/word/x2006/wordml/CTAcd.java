
package org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Acd complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Acd"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="argValue" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_String" /&gt;
 *       &lt;attribute name="fciBasedOn" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_String" /&gt;
 *       &lt;attribute name="fciIndexBasedOn" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_ShortHexNumber" /&gt;
 *       &lt;attribute name="acdName" use="required" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_String" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Acd")
public class CTAcd implements Child
{

    @XmlAttribute(name = "argValue", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String argValue;
    @XmlAttribute(name = "fciBasedOn", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String fciBasedOn;
    @XmlAttribute(name = "fciIndexBasedOn", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] fciIndexBasedOn;
    @XmlAttribute(name = "acdName", namespace = "http://schemas.microsoft.com/office/word/2006/wordml", required = true)
    protected String acdName;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the argValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArgValue() {
        return argValue;
    }

    /**
     * Sets the value of the argValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArgValue(String value) {
        this.argValue = value;
    }

    /**
     * Gets the value of the fciBasedOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFciBasedOn() {
        return fciBasedOn;
    }

    /**
     * Sets the value of the fciBasedOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFciBasedOn(String value) {
        this.fciBasedOn = value;
    }

    /**
     * Gets the value of the fciIndexBasedOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getFciIndexBasedOn() {
        return fciIndexBasedOn;
    }

    /**
     * Sets the value of the fciIndexBasedOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFciIndexBasedOn(byte[] value) {
        this.fciIndexBasedOn = value;
    }

    /**
     * Gets the value of the acdName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcdName() {
        return acdName;
    }

    /**
     * Sets the value of the acdName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcdName(String value) {
        this.acdName = value;
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
