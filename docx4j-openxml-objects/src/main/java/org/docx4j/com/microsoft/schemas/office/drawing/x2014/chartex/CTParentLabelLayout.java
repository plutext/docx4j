
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ParentLabelLayout complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ParentLabelLayout"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="val" use="required" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_ParentLabelLayout" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ParentLabelLayout")
public class CTParentLabelLayout {

    @XmlAttribute(name = "val", required = true)
    protected STParentLabelLayout val;

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link STParentLabelLayout }
     *     
     */
    public STParentLabelLayout getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link STParentLabelLayout }
     *     
     */
    public void setVal(STParentLabelLayout value) {
        this.val = value;
    }

}
