
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTTextBody;


/**
 * <p>Java class for CT_Text complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Text"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="txData" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_TextData"/&gt;
 *           &lt;element name="rich" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Text", propOrder = {
    "txData",
    "rich"
})
public class CTText {

    protected CTTextData txData;
    protected CTTextBody rich;

    /**
     * Gets the value of the txData property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextData }
     *     
     */
    public CTTextData getTxData() {
        return txData;
    }

    /**
     * Sets the value of the txData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextData }
     *     
     */
    public void setTxData(CTTextData value) {
        this.txData = value;
    }

    /**
     * Gets the value of the rich property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getRich() {
        return rich;
    }

    /**
     * Sets the value of the rich property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setRich(CTTextBody value) {
        this.rich = value;
    }

}
