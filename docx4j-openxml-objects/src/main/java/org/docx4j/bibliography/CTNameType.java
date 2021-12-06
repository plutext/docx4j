
package org.docx4j.bibliography;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_NameType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NameType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NameList" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameListType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NameType", propOrder = {
    "nameList"
})
public class CTNameType {

    @XmlElement(name = "NameList", required = true)
    protected CTNameListType nameList;

    /**
     * Gets the value of the nameList property.
     * 
     * @return
     *     possible object is
     *     {@link CTNameListType }
     *     
     */
    public CTNameListType getNameList() {
        return nameList;
    }

    /**
     * Sets the value of the nameList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNameListType }
     *     
     */
    public void setNameList(CTNameListType value) {
        this.nameList = value;
    }

}
