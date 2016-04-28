
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SizeRelH complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SizeRelH">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pctWidth" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositivePercentage"/>
 *       &lt;/sequence>
 *       &lt;attribute name="relativeFrom" use="required" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}ST_SizeRelFromH" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SizeRelH", propOrder = {
    "pctWidth"
})
public class CTSizeRelH {

    protected int pctWidth;
    @XmlAttribute(name = "relativeFrom", required = true)
    protected STSizeRelFromH relativeFrom;

    /**
     * Gets the value of the pctWidth property.
     * 
     */
    public int getPctWidth() {
        return pctWidth;
    }

    /**
     * Sets the value of the pctWidth property.
     * 
     */
    public void setPctWidth(int value) {
        this.pctWidth = value;
    }

    /**
     * Gets the value of the relativeFrom property.
     * 
     * @return
     *     possible object is
     *     {@link STSizeRelFromH }
     *     
     */
    public STSizeRelFromH getRelativeFrom() {
        return relativeFrom;
    }

    /**
     * Sets the value of the relativeFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSizeRelFromH }
     *     
     */
    public void setRelativeFrom(STSizeRelFromH value) {
        this.relativeFrom = value;
    }

}
