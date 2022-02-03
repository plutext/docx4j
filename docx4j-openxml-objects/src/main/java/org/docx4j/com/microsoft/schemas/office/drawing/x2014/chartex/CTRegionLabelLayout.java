
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_RegionLabelLayout complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RegionLabelLayout"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="val" use="required" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_RegionLabelLayout" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RegionLabelLayout")
public class CTRegionLabelLayout {

    @XmlAttribute(name = "val", required = true)
    protected STRegionLabelLayout val;

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link STRegionLabelLayout }
     *     
     */
    public STRegionLabelLayout getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRegionLabelLayout }
     *     
     */
    public void setVal(STRegionLabelLayout value) {
        this.val = value;
    }

}
