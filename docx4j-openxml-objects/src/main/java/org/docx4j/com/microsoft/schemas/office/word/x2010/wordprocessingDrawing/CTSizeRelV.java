
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SizeRelV complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SizeRelV">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pctHeight" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositivePercentage"/>
 *       &lt;/sequence>
 *       &lt;attribute name="relativeFrom" use="required" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing}ST_SizeRelFromV" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SizeRelV", propOrder = {
    "pctHeight"
})
public class CTSizeRelV {

    protected int pctHeight;
    @XmlAttribute(name = "relativeFrom", required = true)
    protected STSizeRelFromV relativeFrom;

    /**
     * Gets the value of the pctHeight property.
     * 
     */
    public int getPctHeight() {
        return pctHeight;
    }

    /**
     * Sets the value of the pctHeight property.
     * 
     */
    public void setPctHeight(int value) {
        this.pctHeight = value;
    }

    /**
     * Gets the value of the relativeFrom property.
     * 
     * @return
     *     possible object is
     *     {@link STSizeRelFromV }
     *     
     */
    public STSizeRelFromV getRelativeFrom() {
        return relativeFrom;
    }

    /**
     * Sets the value of the relativeFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSizeRelFromV }
     *     
     */
    public void setRelativeFrom(STSizeRelFromV value) {
        this.relativeFrom = value;
    }

}
