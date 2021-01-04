
package org.docx4j.com.microsoft.schemas.office.word.x2006.wordml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Mcd complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Mcd"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="macroName" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" /&gt;
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" /&gt;
 *       &lt;attribute name="menuHelp" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" /&gt;
 *       &lt;attribute name="bEncrypt" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" /&gt;
 *       &lt;attribute name="cmg" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_UcharHexNumber" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Mcd")
public class CTMcd implements Child
{

    @XmlAttribute(name = "macroName", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String macroName;
    @XmlAttribute(name = "name", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String name;
    @XmlAttribute(name = "menuHelp", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String menuHelp;
    @XmlAttribute(name = "bEncrypt", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String bEncrypt;
    @XmlAttribute(name = "cmg", namespace = "http://schemas.microsoft.com/office/word/2006/wordml")
    protected String cmg;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the macroName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMacroName() {
        return macroName;
    }

    /**
     * Sets the value of the macroName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMacroName(String value) {
        this.macroName = value;
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
     * Gets the value of the menuHelp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMenuHelp() {
        return menuHelp;
    }

    /**
     * Sets the value of the menuHelp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMenuHelp(String value) {
        this.menuHelp = value;
    }

    /**
     * Gets the value of the bEncrypt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBEncrypt() {
        return bEncrypt;
    }

    /**
     * Sets the value of the bEncrypt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBEncrypt(String value) {
        this.bEncrypt = value;
    }

    /**
     * Gets the value of the cmg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmg() {
        return cmg;
    }

    /**
     * Sets the value of the cmg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmg(String value) {
        this.cmg = value;
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
