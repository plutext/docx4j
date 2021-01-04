
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
 * <p>Java class for CT_Fci complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Fci"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="fciName" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_String" /&gt;
 *       &lt;attribute name="fciIndex" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_ShortHexNumber" /&gt;
 *       &lt;attribute name="swArg" type="{http://schemas.microsoft.com/office/word/2006/wordml}ST_ShortHexNumber" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Fci")
public class CTFci implements Child
{

    @XmlAttribute(name = "fciName", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String fciName;
    @XmlAttribute(name = "fciIndex", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] fciIndex;
    @XmlAttribute(name = "swArg", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] swArg;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fciName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFciName() {
        return fciName;
    }

    /**
     * Sets the value of the fciName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFciName(String value) {
        this.fciName = value;
    }

    /**
     * Gets the value of the fciIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getFciIndex() {
        return fciIndex;
    }

    /**
     * Sets the value of the fciIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFciIndex(byte[] value) {
        this.fciIndex = value;
    }

    /**
     * Gets the value of the swArg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getSwArg() {
        return swArg;
    }

    /**
     * Sets the value of the swArg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwArg(byte[] value) {
        this.swArg = value;
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
